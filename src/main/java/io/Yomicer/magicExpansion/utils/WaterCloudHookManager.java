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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 水云间·新钓鱼状态机会话管理中心
 * - 每玩家一个会话(抛竿登记, 收竿/断线清理)
 * - 每秒 tick: 状态转移概率判定 + 动作栏文案/蓄力条刷新 + 轻咬超时
 * - 每 tick(20Hz): 完全咬钩(蓄力阶段)方位瞄准 → 经验条色块 + 蓄力增长
 * - 每 5 tick: 完全咬钩时鱼钩实体晃动(水面平面内抖动 + 水花粒子)
 * - 蓄力满 → 自动收竿回调; 60 秒防挂机 / 超距(20 格) → 自动脱钩
 * - 全局开关: 由 /mxfishmode 指令切换, 持久化到 config.yml
 */
public final class WaterCloudHookManager {

    /** 完全咬钩时鱼钩实体晃动幅度(方块, ±值) */
    private static final double SHAKE_RADIUS = 0.9;
    /** 会话最大存活秒数(超时兜底清理, 防泄漏) */
    private static final long MAX_SESSION_SECONDS = 600;
    /** 蓄力阶段防挂机时限(秒) */
    private static final long FULL_BITE_LIMIT_SECONDS = 30;
    /** 蓄力阶段玩家与鱼漂的最大距离(格, 超出自动脱钩, 沿用原版钓鱼距离限制) */
    private static final double MAX_HOOK_DISTANCE = 20.0;
    /** 中央判定范围(度, 色块在此范围内蓄力) */
    private static final double AIM_THRESHOLD_DEGREES = 15.0;
    /** 蓄力速率: 每 tick 增加 1.25 (每秒 20 tick × 1.25 = 25%/秒, 4 秒蓄满) */
    private static final double CHARGE_PER_TICK = 1.25;
    /** 蓄力上限 */
    private static final double MAX_CHARGE = 100.0;
    /** 等待/轻咬状态动作栏文案刷新间隔(秒): 每 3 秒一刷 */
    private static final int ACTION_BAR_REFRESH_SECONDS = 3;
    /** 蓄力收竿中鱼概率 = 10% + 蓄力% × 0.9 */
    private static final double BASE_CATCH_CHANCE = 0.10;
    private static final double CHARGE_CATCH_FACTOR = 0.9 / MAX_CHARGE;

    private static final Map<UUID, HookSession> SESSIONS = new HashMap<>();
    private static BukkitTask tickTask;
    private static BukkitTask aimTask;
    private static BukkitTask shakeTask;

    /** 蓄满自动收竿回调(由监听器注册) */
    private static AutoReelHandler autoReelHandler;

    public interface AutoReelHandler {

        /** @param hookLocation 鱼钩位置(生成掉落物用) */
        void onAutoReel(Player player, Location hookLocation);
    }

    private WaterCloudHookManager() {
    }

    // ==================== 全局开关 ====================

    /** 新钓鱼系统是否开启(默认开启, 指令可切换, 持久化 config.yml) */
    public static boolean isEnabled() {
        return MagicExpansion.getInstance().getConfig().getBoolean("fishing-system.new-system", true);
    }

    public static void setEnabled(boolean enabled) {
        MagicExpansion.getInstance().getConfig().set("fishing-system.new-system", enabled);
        MagicExpansion.getInstance().saveConfig();
    }

    // ==================== 调度 ====================

    public static void startTicking(MagicExpansion plugin) {
        stopTicking();
        tickTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, WaterCloudHookManager::tick, 20L, 20L);
        aimTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, WaterCloudHookManager::aimTick, 1L, 1L);
        shakeTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, WaterCloudHookManager::shakeTick, 5L, 5L);
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
        // 关服/卸载: 还原所有被接管的经验条, 避免玩家经验数据被写入 0 级
        for (HookSession session : SESSIONS.values()) {
            Player player = session.player;
            if (player != null && player.isOnline()) {
                restoreExpBar(player, session);
            }
        }
        SESSIONS.clear();
    }

    // ==================== 会话生命周期 ====================

    /** 抛竿: 登记会话(覆盖旧会话, 防御性清理) */
    public static void startSession(Player player, FishHook hook, ItemStack rod) {
        UUID id = player.getUniqueId();
        HookSession old = SESSIONS.remove(id);
        if (old != null && old.hook.isValid()) {
            old.hook.remove();
        }
        SESSIONS.put(id, new HookSession(player, hook, rod));
    }

    @Nullable
    public static HookSession getSession(Player player) {
        return SESSIONS.get(player.getUniqueId());
    }

    /** 断线清理: 还原经验条后移除会话 */
    public static void onQuit(Player player) {
        HookSession session = SESSIONS.remove(player.getUniqueId());
        if (session != null) {
            restoreExpBar(player, session);
        }
    }

    /**
     * 收竿判定: 按当前状态判定中鱼
     * WAITING 1% / LIGHT_BITE 40% / FULL_BITE 10% + 蓄力%×0.9
     * 判定失败 → 脱钩反馈 + 结束会话(不再回静默)
     *
     * @return true = 中鱼; false = 脱钩(已播放脱钩反馈)
     */
    public static boolean onReel(Player player) {
        HookSession session = SESSIONS.remove(player.getUniqueId());
        if (session == null || !session.hook.isValid()) {
            return false;
        }
        double chance;
        if (session.state == WaterCloudHookState.FULL_BITE) {
            chance = BASE_CATCH_CHANCE + session.charge * CHARGE_CATCH_FACTOR;
        } else {
            chance = session.state.getCatchRate();
        }
        restoreExpBar(player, session);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            return true;
        }
        playEscapedFeedback(player, session.hook);
        return false;
    }

    // ==================== 每秒状态机 tick ====================

    private static void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, HookSession>> it = SESSIONS.entrySet().iterator();
        while (it.hasNext()) {
            HookSession session = it.next().getValue();
            Player player = session.player;
            if (player == null || !player.isOnline() || !session.hook.isValid()) {
                it.remove();
                continue;
            }
            // 超时兜底(长时间不收竿, 防泄漏)
            if ((now - session.startTime) / 1000L > MAX_SESSION_SECONDS) {
                restoreExpBar(player, session);
                it.remove();
                continue;
            }
            session.stateTicks++;
            rollState(session, player);
            refreshActionBar(session, player);
        }
    }

    /** 状态转移与超时判定(完全咬钩为蓄力阶段, 无超时, 由 aimTick 的 60 秒防挂机接管) */
    private static void rollState(HookSession session, Player player) {
        WaterCloudHookState state = session.state;
        if (state == WaterCloudHookState.WAITING) {
            // 等待 → 轻咬: 基础 10% + 熟练度等待缩短加成(4 级起每级 +1.5%)
            double chance = state.getBiteChance() + WaterCloudRodEffects.getNewBiteChanceBonus(session.rodLevel);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                enterState(session, player, WaterCloudHookState.LIGHT_BITE);
            }
        } else if (state == WaterCloudHookState.LIGHT_BITE) {
            if (ThreadLocalRandom.current().nextDouble() < state.getBiteChance()
                    + WaterCloudRodEffects.getNewLightBiteChanceBonus(session.rodLevel)) {
                enterState(session, player, WaterCloudHookState.FULL_BITE);
            } else if (session.stateTicks >= state.getMaxSeconds()) {
                // 轻咬 8 秒未升级 → 脱钩, 回到静默
                playEscapedFeedback(player, session.hook);
                enterState(session, player, WaterCloudHookState.WAITING);
            }
        }
    }

    /** 进入新状态: 重置计时 + 音效 + 粒子; 进入完全咬钩时接管经验条并开启蓄力 */
    private static void enterState(HookSession session, Player player, WaterCloudHookState next) {
        session.state = next;
        session.stateTicks = 0;
        session.actionBarTicks = 0;
        if (next == WaterCloudHookState.FULL_BITE) {
            // 蓄力阶段开始: 暂存并接管经验条(等级归零, 经验条用于方位色块)
            if (!session.expTakenOver) {
                session.savedExp = player.getExp();
                session.savedLevel = player.getLevel();
                session.expTakenOver = true;
                player.setLevel(0);
                player.setExp(0.5f);
            }
            session.charge = 0;
            session.fullBiteStartTime = System.currentTimeMillis();
            // 进入完全咬钩: 立即播放溅起水花音效 + 聊天栏状态文案(动作栏让位给瞄准标尺)
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 0.9f, 1.0f);
            player.sendMessage(next.getActionBarColor() + "✦ " + WaterCloudHookPhrases.getRandom(next));
        }
        if (next.getSound() != null) {
            player.playSound(player.getLocation(), next.getSound(), 0.6f, 1.0f);
        }
        if (next.getParticle() != null && session.hook.isValid()) {
            player.getWorld().spawnParticle(next.getParticle(),
                    session.hook.getLocation().add(0, 0.2, 0),
                    next.getParticleCount(), 0.15, 0.05, 0.15, 0.01);
        }
    }

    /** 动作栏: 状态颜色 + 随机文案; 完全咬钩时动作栏由 aimTick 的瞄准标尺接管 */
    private static void refreshActionBar(HookSession session, Player player) {
        WaterCloudHookState state = session.state;
        if (state == WaterCloudHookState.FULL_BITE) {
            return;
        }
        // 等待/轻咬: 进入状态立即显示, 之后每 3 秒刷新一次(文案保持 3 秒不变)
        session.actionBarTicks++;
        if (session.actionBarTicks % ACTION_BAR_REFRESH_SECONDS != 1) {
            return;
        }
        player.sendActionBar(state.getActionBarColor() + WaterCloudHookPhrases.getRandom(state));
    }

    /**
     * 瞄准标尺: 23 字符左右对称([ + 21 格 + ]), 色块 █ 居中时精确位于屏幕正中央
     * 整条标尺采用魔法二代渐变色(青绿→灰蓝→淡紫→紫红→品红);
     * 色块 █ 显示其所在位置的渐变颜色并提亮 45% 突出
     */
    private static String buildAimBar(double exp) {
        int pos = (int) Math.round(Math.max(0.0, Math.min(1.0, exp)) * 20);
        java.util.List<org.bukkit.Color> colors = ColorGradient.createCustomColorListV2();
        int total = 23; // [ + 21 格 + ]
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) {
            double p = (double) i / (total - 1) * (colors.size() - 1);
            int c1 = (int) Math.floor(p);
            int c2 = Math.min(colors.size() - 1, (int) Math.ceil(p));
            double t = p - c1;
            org.bukkit.Color a = colors.get(c1);
            org.bukkit.Color b = colors.get(c2);
            int r = (int) (a.getRed() * (1 - t) + b.getRed() * t);
            int g = (int) (a.getGreen() * (1 - t) + b.getGreen() * t);
            int bl = (int) (a.getBlue() * (1 - t) + b.getBlue() * t);
            boolean isBlock = i >= 1 && i <= 21 && (i - 1) == pos;
            if (isBlock) {
                // 色块提亮 45%, 与背景区分
                r = Math.min(255, r + (255 - r) * 45 / 100);
                g = Math.min(255, g + (255 - g) * 45 / 100);
                bl = Math.min(255, bl + (255 - bl) * 45 / 100);
            }
            sb.append("§x")
                    .append("§").append(ColorGradient.codeColor(r / 16)).append("§").append(ColorGradient.codeColor(r % 16))
                    .append("§").append(ColorGradient.codeColor(g / 16)).append("§").append(ColorGradient.codeColor(g % 16))
                    .append("§").append(ColorGradient.codeColor(bl / 16)).append("§").append(ColorGradient.codeColor(bl % 16));
            if (i == 0) {
                sb.append("[");
            } else if (i == total - 1) {
                sb.append("]");
            } else {
                sb.append((i - 1) == pos ? "█" : "░");
            }
        }
        return sb.toString();
    }

    /** 脱钩反馈: 收回音效 + 动作栏脱钩文案 */
    private static void playEscapedFeedback(Player player, FishHook hook) {
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.7f, 0.9f);
        player.sendActionBar("§7" + WaterCloudHookPhrases.getRandomEscaped());
    }

    // ==================== 20Hz 瞄准与蓄力 ====================

    /** 蓄力阶段溅水音效间隔(tick): 每 0.5 秒一次, 频率稍快 */
    private static final int SPLASH_SOUND_INTERVAL = 10;

    /** 蓄力阶段: 每 tick 计算鱼漂方位 → 经验条色块; 中央范围蓄力; 蓄满自动收竿; 60 秒防挂机 / 超距脱钩 */
    private static void aimTick() {
        for (HookSession session : SESSIONS.values()) {
            if (session.state != WaterCloudHookState.FULL_BITE) {
                continue;
            }
            Player player = session.player;
            FishHook hook = session.hook;
            if (player == null || !player.isOnline() || !hook.isValid()) {
                continue;
            }
            // 超距自动脱钩(沿用原版钓鱼距离限制)
            if (player.getLocation().distance(hook.getLocation()) > MAX_HOOK_DISTANCE) {
                finishByEscape(player, session);
                continue;
            }
            // 60 秒防挂机: 进入蓄力阶段后未蓄满 → 自动脱钩
            if (System.currentTimeMillis() - session.fullBiteStartTime > FULL_BITE_LIMIT_SECONDS * 1000L) {
                finishByEscape(player, session);
                continue;
            }

            // 蓄力期间周期性溅水音效(每 0.5 秒一次, 频率稍快)
            long nowMs = System.currentTimeMillis();
            if (nowMs - session.lastSplashTime >= SPLASH_SOUND_INTERVAL * 50L) {
                session.lastSplashTime = nowMs;
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 0.7f, 1.1f);
            }

            // 方位计算(平面): 鱼漂相对玩家的方向角 - 玩家视角 yaw, 归一化 [-180,180]
            Location playerLoc = player.getEyeLocation();
            Location hookLoc = hook.getLocation();
            double dx = hookLoc.getX() - playerLoc.getX();
            double dz = hookLoc.getZ() - playerLoc.getZ();
            double angle = Math.toDegrees(Math.atan2(dx, dz));
            // yaw 顺时针为正, atan2 逆时针为正(方向相反), 故取 -(angle + yaw); 正对鱼漂 = 0(标尺中央)
            double diff = ((-(angle + playerLoc.getYaw()) + 540) % 360) - 180;
            // 色块在标尺上的位置: 正对鱼漂 = 中央
            double exp = (diff + 180) / 360.0;

            // 经验条 = 蓄力进度(色块方位改由动作栏标尺显示)
            player.setExp((float) Math.max(0.0, Math.min(1.0, session.charge / MAX_CHARGE)));

            // 动作栏瞄准标尺(独占一行, 左右对称居中; 魔法二代渐变色, 色块随位置显示不同渐变颜色)
            player.sendActionBar(buildAimBar(exp));

            // 中央范围蓄力(基础速率 + 熟练度蓄力加速加成, 3 级起每级 +0.5 点/秒)
            if (Math.abs(diff) <= AIM_THRESHOLD_DEGREES) {
                session.charge = Math.min(MAX_CHARGE, session.charge + CHARGE_PER_TICK
                        + WaterCloudRodEffects.getNewChargeSpeedBonus(session.rodLevel) / 20.0);
                if (session.charge >= MAX_CHARGE) {
                    onChargeFull(player, session);
                }
            }
        }
    }

    /** 蓄满: 恢复经验条 + 结束会话 + 触发自动收竿回调 */
    private static void onChargeFull(Player player, HookSession session) {
        SESSIONS.remove(player.getUniqueId());
        restoreExpBar(player, session);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
        Location hookLoc = session.hook.getLocation();
        if (session.hook.isValid()) {
            session.hook.remove();
        }
        if (autoReelHandler != null) {
            autoReelHandler.onAutoReel(player, hookLoc);
        }
    }

    /** 防挂机/超距脱钩: 鱼跑了 → 脱钩反馈 + 还原经验条 + 回到静默等待(鱼漂继续挂着, 重新进入咬钩循环) */
    private static void finishByEscape(Player player, HookSession session) {
        playEscapedFeedback(player, session.hook);
        restoreExpBar(player, session);
        session.state = WaterCloudHookState.WAITING;
        session.stateTicks = 0;
        session.actionBarTicks = 0;
        session.charge = 0;
    }

    /** 还原玩家经验条(仅当被接管时) */
    private static void restoreExpBar(Player player, HookSession session) {
        if (session.expTakenOver) {
            session.expTakenOver = false;
            player.setExp(session.savedExp);
            player.setLevel(session.savedLevel);
        }
    }

    // ==================== 实体晃动(每 5 tick) ====================

    /** 完全咬钩: 鱼钩实体在水面平面内抖动 + 水花粒子, 模拟鱼在水下拉扯 */
    private static void shakeTick() {
        for (HookSession session : SESSIONS.values()) {
            if (session.state != WaterCloudHookState.FULL_BITE) {
                continue;
            }
            Player player = session.player;
            FishHook hook = session.hook;
            if (player == null || !hook.isValid()) {
                continue;
            }
            double dx = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * SHAKE_RADIUS;
            double dz = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * SHAKE_RADIUS;
            hook.teleport(hook.getLocation().add(dx, 0, dz));
            player.getWorld().spawnParticle(Particle.WATER_SPLASH,
                    hook.getLocation().add(0, 0.1, 0), 2, 0.1, 0, 0.1, 0.01);
        }
    }

    // ==================== 回调注册 ====================

    /** 注册蓄满自动收竿回调(由钓鱼监听器在构造时注册) */
    public static void setAutoReelHandler(AutoReelHandler handler) {
        autoReelHandler = handler;
    }

    // ==================== 会话数据 ====================

    public static final class HookSession {

        private final Player player;
        private final FishHook hook;
        private final ItemStack rod;
        private final int rodLevel;
        private final long startTime;
        private WaterCloudHookState state = WaterCloudHookState.WAITING;
        /** 当前状态已持续的秒数(tick() 每秒调用一次, 每次 +1) */
        private int stateTicks = 0;
        /** 动作栏文案刷新计数(等待/轻咬每 3 秒一刷) */
        private int actionBarTicks = 0;
        /** 蓄力值 0~100(进入完全咬钩时归零) */
        private double charge = 0;
        /** 进入完全咬钩的时间戳(60 秒防挂机起点) */
        private long fullBiteStartTime = 0;
        /** 上次溅水音效时间戳(蓄力期间每 0.5 秒一次) */
        private long lastSplashTime = 0;
        /** 被接管前的经验条进度/等级(收竿/脱钩/断线时还原) */
        private float savedExp = 0;
        private int savedLevel = 0;
        private boolean expTakenOver = false;

        private HookSession(Player player, FishHook hook, ItemStack rod) {
            this.player = player;
            this.hook = hook;
            this.rod = rod;
            this.rodLevel = WaterCloudRodProficiency.getLevel(rod);
            this.startTime = System.currentTimeMillis();
        }

        public WaterCloudHookState getState() {
            return state;
        }

        public FishHook getHook() {
            return hook;
        }

        public ItemStack getRod() {
            return rod;
        }

        /** 当前蓄力值 0~100 */
        public double getCharge() {
            return charge;
        }
    }
}
