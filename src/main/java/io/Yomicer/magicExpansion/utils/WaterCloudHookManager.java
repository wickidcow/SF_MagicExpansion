package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Optional Water Cloud fishing state machine ported from upstream.
 *
 * It is disabled by default in Legacy and is automatically suppressed while
 * PyroFishingPro or BetterFishing is the primary fishing provider. All session
 * exits restore any temporary XP-bar state before the session is discarded.
 */
public final class WaterCloudHookManager {

    private static final double SHAKE_RADIUS = 0.9;
    private static final long MAX_SESSION_SECONDS = 600;
    private static final long FULL_BITE_LIMIT_SECONDS = 30;
    private static final double MAX_HOOK_DISTANCE = 20.0;
    private static final double AIM_THRESHOLD_DEGREES = 15.0;
    private static final double CHARGE_PER_TICK = 1.25;
    private static final double MAX_CHARGE = 100.0;
    private static final int ACTION_BAR_REFRESH_SECONDS = 3;
    private static final double BASE_CATCH_CHANCE = 0.10;
    private static final double CHARGE_CATCH_FACTOR = 0.9 / MAX_CHARGE;
    private static final int SPLASH_SOUND_INTERVAL = 10;

    private static final Map<UUID, HookSession> SESSIONS = new HashMap<>();
    private static BukkitTask tickTask;
    private static BukkitTask aimTask;
    private static BukkitTask shakeTask;
    private static AutoReelHandler autoReelHandler;

    public interface AutoReelHandler {
        void onAutoReel(Player player, Location hookLocation);
    }

    private WaterCloudHookManager() {
    }

    public static boolean isEnabled() {
        return FishingIntegrationManager.shouldUseMagicStateMachine();
    }

    public static void setEnabled(boolean enabled) {
        MagicExpansion plugin = MagicExpansion.getInstance();
        plugin.getConfig().set("fishing-system.new-system", enabled);
        plugin.saveConfig();

        if (!enabled || !FishingIntegrationManager.shouldUseMagicStateMachine()) {
            cancelAllSessions();
        }
        FishingIntegrationManager.logStatus();
    }

    public static void startTicking(MagicExpansion plugin) {
        stopTicking();
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, WaterCloudHookManager::tick, 20L, 20L);
        aimTask = plugin.getServer().getScheduler().runTaskTimer(plugin, WaterCloudHookManager::aimTick, 1L, 1L);
        shakeTask = plugin.getServer().getScheduler().runTaskTimer(plugin, WaterCloudHookManager::shakeTick, 5L, 5L);
    }

    public static void stopTicking() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        if (aimTask != null) {
            aimTask.cancel();
            aimTask = null;
        }
        if (shakeTask != null) {
            shakeTask.cancel();
            shakeTask = null;
        }
        cancelAllSessions();
    }

    public static void cancelAllSessions() {
        for (HookSession session : SESSIONS.values()) {
            Player player = session.player;
            if (player != null && player.isOnline()) {
                restoreExpBar(player, session);
            }
        }
        SESSIONS.clear();
    }

    public static void startSession(Player player, FishHook hook, ItemStack rod) {
        if (!isEnabled()) {
            return;
        }

        UUID id = player.getUniqueId();
        HookSession old = SESSIONS.remove(id);
        if (old != null) {
            restoreExpBar(player, old);
            if (old.hook.isValid()) {
                old.hook.remove();
            }
        }
        SESSIONS.put(id, new HookSession(player, hook, rod));
    }

    @Nullable
    public static HookSession getSession(Player player) {
        return SESSIONS.get(player.getUniqueId());
    }

    public static void onQuit(Player player) {
        HookSession session = SESSIONS.remove(player.getUniqueId());
        if (session != null) {
            restoreExpBar(player, session);
        }
    }

    public static boolean onReel(Player player) {
        HookSession session = SESSIONS.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }

        restoreExpBar(player, session);
        if (!session.hook.isValid()) {
            return false;
        }

        double chance = session.state == WaterCloudHookState.FULL_BITE
                ? BASE_CATCH_CHANCE + session.charge * CHARGE_CATCH_FACTOR
                : session.state.getCatchRate();

        if (ThreadLocalRandom.current().nextDouble() < chance) {
            return true;
        }

        playEscapedFeedback(player);
        return false;
    }

    private static void tick() {
        if (!isEnabled()) {
            if (!SESSIONS.isEmpty()) {
                cancelAllSessions();
            }
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, HookSession>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            HookSession session = iterator.next().getValue();
            Player player = session.player;

            if (player == null || !player.isOnline() || !session.hook.isValid()) {
                if (player != null && player.isOnline()) {
                    restoreExpBar(player, session);
                }
                iterator.remove();
                continue;
            }

            if ((now - session.startTime) / 1000L > MAX_SESSION_SECONDS) {
                restoreExpBar(player, session);
                iterator.remove();
                continue;
            }

            session.stateTicks++;
            rollState(session, player);
            refreshActionBar(session, player);
        }
    }

    private static void rollState(HookSession session, Player player) {
        WaterCloudHookState state = session.state;
        if (state == WaterCloudHookState.WAITING) {
            double chance = state.getBiteChance() + WaterCloudRodEffects.getNewBiteChanceBonus(session.rodLevel);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                enterState(session, player, WaterCloudHookState.LIGHT_BITE);
            }
        } else if (state == WaterCloudHookState.LIGHT_BITE) {
            double chance = state.getBiteChance() + WaterCloudRodEffects.getNewLightBiteChanceBonus(session.rodLevel);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                enterState(session, player, WaterCloudHookState.FULL_BITE);
            } else if (session.stateTicks >= state.getMaxSeconds()) {
                playEscapedFeedback(player);
                enterState(session, player, WaterCloudHookState.WAITING);
            }
        }
    }

    private static void enterState(HookSession session, Player player, WaterCloudHookState next) {
        session.state = next;
        session.stateTicks = 0;
        session.actionBarTicks = 0;

        if (next == WaterCloudHookState.FULL_BITE) {
            if (!session.expTakenOver) {
                session.savedExp = player.getExp();
                session.savedLevel = player.getLevel();
                session.expTakenOver = true;
                player.setLevel(0);
                player.setExp(0.0f);
            }
            session.charge = 0;
            session.fullBiteStartTime = System.currentTimeMillis();
            player.sendMessage("§b✦ " + WaterCloudHookPhrases.getRandom(next));
        }

        if (next.getSound() != null) {
            player.playSound(player.getLocation(), next.getSound(), 0.6f, 1.0f);
        }
        if (next.getParticle() != null && session.hook.isValid()) {
            player.getWorld().spawnParticle(next.getParticle(), session.hook.getLocation().add(0, 0.2, 0),
                    next.getParticleCount(), 0.15, 0.05, 0.15, 0.01);
        }
    }

    private static void refreshActionBar(HookSession session, Player player) {
        if (session.state == WaterCloudHookState.FULL_BITE) {
            return;
        }
        session.actionBarTicks++;
        if (session.actionBarTicks % ACTION_BAR_REFRESH_SECONDS == 1) {
            player.sendActionBar(session.state.getActionBarColor() + WaterCloudHookPhrases.getRandom(session.state));
        }
    }

    private static void aimTick() {
        if (!isEnabled()) {
            return;
        }

        // Work from a snapshot because a fully charged session removes itself.
        for (HookSession session : List.copyOf(SESSIONS.values())) {
            if (session.state != WaterCloudHookState.FULL_BITE) {
                continue;
            }

            Player player = session.player;
            FishHook hook = session.hook;
            if (player == null || !player.isOnline() || !hook.isValid()) {
                if (player != null && player.isOnline()) {
                    restoreExpBar(player, session);
                }
                continue;
            }

            if (player.getLocation().distance(hook.getLocation()) > MAX_HOOK_DISTANCE
                    || System.currentTimeMillis() - session.fullBiteStartTime > FULL_BITE_LIMIT_SECONDS * 1000L) {
                finishByEscape(player, session);
                continue;
            }

            long now = System.currentTimeMillis();
            if (now - session.lastSplashTime >= SPLASH_SOUND_INTERVAL * 50L) {
                session.lastSplashTime = now;
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 0.7f, 1.1f);
            }

            Location eye = player.getEyeLocation();
            Location hookLoc = hook.getLocation();
            double dx = hookLoc.getX() - eye.getX();
            double dz = hookLoc.getZ() - eye.getZ();
            double angle = Math.toDegrees(Math.atan2(dx, dz));
            double diff = ((-(angle + eye.getYaw()) + 540) % 360) - 180;

            player.setExp((float) Math.max(0.0, Math.min(1.0, session.charge / MAX_CHARGE)));
            player.sendActionBar(buildAimBar(diff, session.charge));

            if (Math.abs(diff) <= AIM_THRESHOLD_DEGREES) {
                session.charge = Math.min(MAX_CHARGE, session.charge + CHARGE_PER_TICK
                        + WaterCloudRodEffects.getNewChargeSpeedBonus(session.rodLevel) / 20.0);
                if (session.charge >= MAX_CHARGE) {
                    onChargeFull(player, session);
                }
            }
        }
    }

    private static String buildAimBar(double difference, double charge) {
        int position = (int) Math.round(((difference + 180.0) / 360.0) * 20.0);
        position = Math.max(0, Math.min(20, position));
        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i <= 20; i++) {
            if (i == position) {
                bar.append(i == 10 ? "§a█" : "§e█");
            } else if (i == 10) {
                bar.append("§a|");
            } else {
                bar.append("§8░");
            }
        }
        bar.append("§7] §b").append((int) charge).append("%");
        return bar.toString();
    }

    private static void onChargeFull(Player player, HookSession session) {
        SESSIONS.remove(player.getUniqueId());
        restoreExpBar(player, session);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
        Location hookLocation = session.hook.getLocation();
        if (session.hook.isValid()) {
            session.hook.remove();
        }
        if (autoReelHandler != null) {
            autoReelHandler.onAutoReel(player, hookLocation);
        }
    }

    private static void finishByEscape(Player player, HookSession session) {
        playEscapedFeedback(player);
        restoreExpBar(player, session);
        session.state = WaterCloudHookState.WAITING;
        session.stateTicks = 0;
        session.actionBarTicks = 0;
        session.charge = 0;
    }

    private static void restoreExpBar(Player player, HookSession session) {
        if (session.expTakenOver) {
            session.expTakenOver = false;
            player.setExp(session.savedExp);
            player.setLevel(session.savedLevel);
        }
    }

    private static void playEscapedFeedback(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.7f, 0.9f);
        player.sendActionBar("§7" + WaterCloudHookPhrases.getRandomEscaped());
    }

    private static void shakeTick() {
        if (!isEnabled()) {
            return;
        }

        for (HookSession session : SESSIONS.values()) {
            if (session.state != WaterCloudHookState.FULL_BITE) {
                continue;
            }
            Player player = session.player;
            FishHook hook = session.hook;
            if (player == null || !player.isOnline() || !hook.isValid()) {
                continue;
            }
            double dx = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * SHAKE_RADIUS;
            double dz = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * SHAKE_RADIUS;
            hook.teleport(hook.getLocation().add(dx, 0, dz));
            player.getWorld().spawnParticle(Particle.SPLASH, hook.getLocation().add(0, 0.1, 0),
                    2, 0.1, 0, 0.1, 0.01);
        }
    }

    public static void setAutoReelHandler(AutoReelHandler handler) {
        autoReelHandler = handler;
    }

    public static final class HookSession {
        private final Player player;
        private final FishHook hook;
        private final ItemStack rod;
        private final int rodLevel;
        private final long startTime;
        private WaterCloudHookState state = WaterCloudHookState.WAITING;
        private int stateTicks;
        private int actionBarTicks;
        private double charge;
        private long fullBiteStartTime;
        private long lastSplashTime;
        private float savedExp;
        private int savedLevel;
        private boolean expTakenOver;

        private HookSession(Player player, FishHook hook, ItemStack rod) {
            this.player = player;
            this.hook = hook;
            this.rod = rod;
            this.rodLevel = WaterCloudRodProficiency.getLevel(rod);
            this.startTime = System.currentTimeMillis();
        }

        public WaterCloudHookState getState() { return state; }
        public FishHook getHook() { return hook; }
        public ItemStack getRod() { return rod; }
        public double getCharge() { return charge; }
    }
}
