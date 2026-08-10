package io.Yomicer.magicExpansion.Listener.fishingListener;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.misc.moreLure.MoreLure;
import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.FishingIntegrationManager;
import io.Yomicer.magicExpansion.utils.WaterCloudHookManager;
import io.Yomicer.magicExpansion.utils.WaterCloudRodEffects;
import io.Yomicer.magicExpansion.utils.WaterCloudRodProficiency;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Water Cloud fishing integration.
 *
 * PyroFishingPro/BetterFishing remain authoritative when present. In external
 * provider mode this listener never removes or replaces their caught entity;
 * it only applies MagicExpansion rod proficiency, Magic lure preservation,
 * and separate MagicExpansion bonus rewards.
 */
public class PlayerFishingWaterCloudListener implements Listener {

    private static final List<MoreLure> WATER_CLOUD_LURES = List.of(
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA, "CuiXia"),
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN, "WeiChen"),
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO, "RongHuo"),
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN, "YueJin"),
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE, "XingHe")
    );

    private static final Set<String> JUNK_MATERIALS = Set.of(
            "COBBLESTONE", "FLINT", "GRAVEL", "SAND", "STRING", "BONE"
    );

    private static final List<String> CATCH_PHRASES = List.of(
            "Bamboo shadows shatter the moon across the river.",
            "Where water meets cloud, something takes the hook.",
            "The bobber breaks a thousand reflected peaks.",
            "The rod trembles, as though an old friend has arrived.",
            "The moon sinks below, and the hook lifts its glow.",
            "This cast draws up half a river of sunset."
    );

    private static final List<String> SPECIAL_PHRASES = List.of(
            "Water and cloud surge—you hooked something extraordinary: ",
            "A rare catch breaks the surface and scatters the reeds: ",
            "The water flashes around an unusual catch: ",
            "Something uncommon answered the Water Cloud rod: "
    );

    private static final List<String> CONSUME_PHRASES = List.of(
            "One Magic lure enters the current: ",
            "Between water and cloud, this lure is spent: ",
            "The rod rises and one lure returns to the river: "
    );

    private static final List<String> PRESERVE_PHRASES = List.of(
            "✦ Water Cloud preserved the Magic lure.",
            "✦ The current returns the Magic lure unharmed.",
            "✦ The lure resonates with the rod and is not consumed."
    );

    private static final List<String> BONUS_PHRASES = List.of(
            "✦ Water Cloud blessing: an additional Magic catch!",
            "✦ The rod resonates and draws a second Magic reward!",
            "✦ The current yields an extra MagicExpansion catch!"
    );

    private static final List<WeightedItem> JUNK_POOL = List.of(
            new WeightedItem(new ItemStack(Material.COBBLESTONE), 1),
            new WeightedItem(new ItemStack(Material.GRAVEL), 1),
            new WeightedItem(new ItemStack(Material.CLAY_BALL), 1),
            new WeightedItem(new ItemStack(Material.STRING), 1),
            new WeightedItem(new ItemStack(Material.BONE), 1),
            new WeightedItem(new ItemStack(Material.STICK), 1),
            new WeightedItem(new ItemStack(Material.LILY_PAD), 1),
            new WeightedItem(new ItemStack(Material.LEATHER_BOOTS), 1),
            new WeightedItem(new ItemStack(Material.COD), 1),
            new WeightedItem(new ItemStack(Material.SALMON), 1),
            new WeightedItem(new ItemStack(Material.PUFFERFISH), 1),
            new WeightedItem(new ItemStack(Material.TROPICAL_FISH), 1)
    );

    public PlayerFishingWaterCloudListener() {
        WaterCloudHookManager.setAutoReelHandler(this::onAutoReel);
    }

    /**
     * MagicExpansion-owned fishing path. It is skipped completely when an
     * external fishing provider is primary.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (FishingIntegrationManager.isExternalProviderActive()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (!(sfItem instanceof FishingRodWaterCloud fishingRod)) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            if (WaterCloudHookManager.isEnabled()) {
                FishHook hook = event.getHook();
                hook.setMaxWaitTime(Integer.MAX_VALUE / 2);
                WaterCloudHookManager.startSession(player, hook, rod);
            } else {
                applyHookSpeed(rod, event.getHook());
            }
            return;
        }

        if (WaterCloudHookManager.isEnabled()) {
            if (event.getState() == PlayerFishEvent.State.REEL_IN) {
                if (WaterCloudHookManager.onReel(player)) {
                    processCustomStateCatch(player, fishingRod, event.getHook().getLocation(), false);
                }
            } else if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                // Defensive fallback if vanilla unexpectedly produces a catch while the state machine owns the hook.
                if (event.getCaught() instanceof Item caughtItem) {
                    caughtItem.remove();
                }
                processCustomStateCatch(player, fishingRod, event.getHook().getLocation(), false);
            }
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            processLegacyCatch(event, player, fishingRod, rod);
        }
    }

    /**
     * External-provider additive path. MONITOR is observation-only with respect
     * to the provider's catch: MagicExpansion never cancels the event or edits
     * the caught entity.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExternalProviderCatch(PlayerFishEvent event) {
        if (!FishingIntegrationManager.isExternalProviderActive()
                || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (!(sfItem instanceof FishingRodWaterCloud fishingRod)) {
            return;
        }

        Lure activeLure = getActiveLure(player, fishingRod);
        int level = WaterCloudRodProficiency.getLevel(rod);

        if (activeLure != null) {
            if (ThreadLocalRandom.current().nextDouble() < WaterCloudRodEffects.getOldLurePreserveChance(level)) {
                player.sendMessage(PRESERVE_PHRASES.get(ThreadLocalRandom.current().nextInt(PRESERVE_PHRASES.size())));
            } else {
                consumeLure(player, activeLure);
            }

            // Never duplicate or mutate an external plugin's custom fish. A double-catch proc
            // instead creates a separate MagicExpansion reward from this rod's own loot table.
            if (ThreadLocalRandom.current().nextDouble() < WaterCloudRodEffects.getOldDoubleCatchChance(level)) {
                ItemStack bonus = getCaughtDrop(fishingRod, activeLure, level, false);
                if (bonus != null) {
                    spawnDrop(player, event.getHook().getLocation(), bonus);
                    player.sendMessage(BONUS_PHRASES.get(ThreadLocalRandom.current().nextInt(BONUS_PHRASES.size())));
                    ItemStack special = getSpecialCatchForLure(activeLure);
                    if (special != null && SlimefunUtils.isItemSimilar(bonus, special, true)) {
                        triggerSpecialEvent(player, bonus);
                    }
                }
            }
        }

        ItemStack providerCatch = event.getCaught() instanceof Item item ? item.getItemStack() : null;
        grantProficiency(player, rod, activeLure, providerCatch);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        WaterCloudHookManager.onQuit(event.getPlayer());
    }

    private void onAutoReel(Player player, Location hookLocation) {
        if (FishingIntegrationManager.isExternalProviderActive()) {
            return;
        }
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (sfItem instanceof FishingRodWaterCloud fishingRod) {
            processCustomStateCatch(player, fishingRod, hookLocation, true);
        }
    }

    private void processLegacyCatch(PlayerFishEvent event, Player player, FishingRodWaterCloud fishingRod, ItemStack rod) {
        Lure activeLure = getActiveLure(player, fishingRod);
        if (activeLure == null) {
            grantProficiency(player, rod, null,
                    event.getCaught() instanceof Item item ? item.getItemStack() : null);
            return;
        }

        int level = WaterCloudRodProficiency.getLevel(rod);
        ItemStack drop = getCaughtDrop(fishingRod, activeLure, level, true);
        if (drop == null) {
            return;
        }

        Entity caught = event.getCaught();
        if (caught instanceof Item item) {
            item.remove();
        }

        consumeOrPreserveLure(player, activeLure, level);
        spawnDrop(player, event.getHook().getLocation(), drop);

        if (ThreadLocalRandom.current().nextDouble() < WaterCloudRodEffects.getOldDoubleCatchChance(level)) {
            spawnDrop(player, event.getHook().getLocation(), drop.clone());
            player.sendMessage(BONUS_PHRASES.get(ThreadLocalRandom.current().nextInt(BONUS_PHRASES.size())));
        }

        grantProficiency(player, rod, activeLure, drop);
        finishCatchEffects(player, activeLure, drop);
    }

    private void processCustomStateCatch(Player player, FishingRodWaterCloud fishingRod,
                                         Location hookLocation, boolean fullCharge) {
        ItemStack rod = player.getInventory().getItemInMainHand();
        int level = WaterCloudRodProficiency.getLevel(rod);
        Lure activeLure = getActiveLure(player, fishingRod);

        ItemStack drop;
        if (activeLure != null && fullCharge
                && ThreadLocalRandom.current().nextDouble() < 0.10 + WaterCloudRodEffects.getNewRareBonus(level)) {
            drop = getSpecialCatchForLure(activeLure);
        } else if (activeLure != null) {
            drop = getCaughtDrop(fishingRod, activeLure, level, false);
        } else {
            drop = getRandomItemFromWeightedPool(JUNK_POOL);
        }

        if (drop == null) {
            return;
        }

        if (activeLure != null) {
            consumeLure(player, activeLure);
        }

        spawnDrop(player, hookLocation, drop);
        if (activeLure != null
                && ThreadLocalRandom.current().nextDouble() < WaterCloudRodEffects.getNewDoubleCatchChance(level)) {
            spawnDrop(player, hookLocation, drop.clone());
            player.sendMessage(BONUS_PHRASES.get(ThreadLocalRandom.current().nextInt(BONUS_PHRASES.size())));
        }

        grantProficiency(player, rod, activeLure, drop);
        finishCatchEffects(player, activeLure, drop);
    }

    private Lure getActiveLure(Player player, FishingRodWaterCloud fishingRod) {
        Set<String> supportedKeys = fishingRod.getLootTable().keySet();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (offHand != null) {
            Lure offHandLure = WATER_CLOUD_LURES.stream()
                    .filter(lure -> supportedKeys.contains(lure.getKey()))
                    .filter(lure -> SlimefunUtils.isItemSimilar(offHand, lure.getItem(), true))
                    .findFirst()
                    .orElse(null);
            if (offHandLure != null) {
                return offHandLure;
            }
        }

        return WATER_CLOUD_LURES.stream()
                .filter(lure -> supportedKeys.contains(lure.getKey()))
                .filter(lure -> lure.hasLure(player))
                .findFirst()
                .orElse(null);
    }

    private ItemStack getCaughtDrop(FishingRodWaterCloud fishingRod, Lure activeLure,
                                    int rodLevel, boolean reduceJunk) {
        List<WeightedItem> pool = fishingRod.getLootPoolForLure(activeLure);
        if (pool == null || pool.isEmpty()) {
            pool = List.of(
                    new WeightedItem(new ItemStack(Material.COD), 50),
                    new WeightedItem(new ItemStack(Material.INK_SAC), 30)
            );
        }

        if (reduceJunk) {
            double reduction = WaterCloudRodEffects.getOldJunkReduction(rodLevel);
            if (reduction > 0) {
                List<WeightedItem> adjusted = new ArrayList<>();
                for (WeightedItem weighted : pool) {
                    if (isJunkItem(weighted.getItem())) {
                        adjusted.add(new WeightedItem(weighted.getItem(),
                                Math.max(1, (int) Math.round(weighted.getWeight() * (1.0 - reduction)))));
                    } else {
                        adjusted.add(weighted);
                    }
                }
                pool = adjusted;
            }
        }

        return getRandomItemFromWeightedPool(pool);
    }

    private boolean isJunkItem(ItemStack item) {
        return item != null && SlimefunItem.getByItem(item) == null && JUNK_MATERIALS.contains(item.getType().name());
    }

    private ItemStack getRandomItemFromWeightedPool(List<WeightedItem> pool) {
        if (pool == null || pool.isEmpty()) {
            return null;
        }
        int total = pool.stream().mapToInt(WeightedItem::getWeight).sum();
        if (total <= 0) {
            return pool.get(0).getItem().clone();
        }
        int random = ThreadLocalRandom.current().nextInt(total);
        int current = 0;
        for (WeightedItem weighted : pool) {
            current += weighted.getWeight();
            if (current > random) {
                return weighted.getItem().clone();
            }
        }
        return pool.get(0).getItem().clone();
    }

    private ItemStack getSpecialCatchForLure(Lure lure) {
        if (lure == null) {
            return null;
        }
        return switch (lure.getKey()) {
            case "CuiXia" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA;
            case "WeiChen" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN;
            case "RongHuo" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO;
            case "YueJin" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN;
            case "XingHe" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE;
            default -> null;
        };
    }

    private void consumeOrPreserveLure(Player player, Lure lure, int level) {
        if (ThreadLocalRandom.current().nextDouble() < WaterCloudRodEffects.getOldLurePreserveChance(level)) {
            player.sendMessage(PRESERVE_PHRASES.get(ThreadLocalRandom.current().nextInt(PRESERVE_PHRASES.size())));
        } else {
            consumeLure(player, lure);
        }
    }

    private void consumeLure(Player player, Lure lure) {
        PlayerInventory inventory = player.getInventory();
        ItemStack required = lure.getItem();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || !SlimefunUtils.isItemSimilar(item, required, true)) {
                continue;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                inventory.setItem(slot, null);
            }

            String prefix = CONSUME_PHRASES.get(ThreadLocalRandom.current().nextInt(CONSUME_PHRASES.size()));
            player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r" + ItemStackHelper.getDisplayName(required));
            return;
        }
    }

    private void applyHookSpeed(ItemStack rod, FishHook hook) {
        int ticks = WaterCloudRodEffects.getOldHookSpeedTicks(WaterCloudRodProficiency.getLevel(rod));
        if (ticks > 0) {
            hook.setMaxWaitTime(Math.max(100, 600 - ticks));
        }
    }

    private void grantProficiency(Player player, ItemStack rod, Lure activeLure, ItemStack caught) {
        int xp = WaterCloudRodProficiency.XP_COMMON;
        ItemStack special = getSpecialCatchForLure(activeLure);

        if (caught != null && special != null && SlimefunUtils.isItemSimilar(caught, special, true)) {
            xp = WaterCloudRodProficiency.XP_SPECIAL;
        } else if (caught != null && SlimefunItem.getByItem(caught) != null) {
            xp = WaterCloudRodProficiency.XP_RARE;
        }

        int oldLevel = WaterCloudRodProficiency.getLevel(rod);
        int newLevel = WaterCloudRodProficiency.addProficiency(rod, xp);
        WaterCloudRodProficiency.updateLoreWithReward(rod);
        player.getInventory().setItemInMainHand(rod);
        player.sendActionBar(WaterCloudRodProficiency.getActionBarProgress(rod));

        if (newLevel > oldLevel) {
            String message = WaterCloudRodProficiency.LEVEL_UP_MESSAGES[newLevel - 1];
            player.sendMessage("§b✦ Water Cloud rod reached level " + newLevel + ": §f"
                    + WaterCloudRodProficiency.getLevelName(newLevel));
            player.sendMessage("§7" + message);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        }
    }

    private void spawnDrop(Player player, Location hookLocation, ItemStack drop) {
        if (drop == null || drop.getType() == Material.AIR) {
            return;
        }
        Item reward = player.getWorld().dropItem(hookLocation, drop);
        reward.setPickupDelay(0);
        Vector direction = player.getLocation().add(0, 2, 0).toVector()
                .subtract(hookLocation.toVector())
                .normalize()
                .multiply(2.5);
        reward.setVelocity(direction);
        reward.setGlowing(true);
    }

    private void finishCatchEffects(Player player, Lure lure, ItemStack drop) {
        ItemStack special = getSpecialCatchForLure(lure);
        if (special != null && SlimefunUtils.isItemSimilar(drop, special, true)) {
            triggerSpecialEvent(player, drop);
        } else {
            handleNormalCatch(player, drop);
        }
    }

    private void handleNormalCatch(Player player, ItemStack drop) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        String message = CATCH_PHRASES.get(ThreadLocalRandom.current().nextInt(CATCH_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop));
    }

    private void triggerSpecialEvent(Player player, ItemStack drop) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        spawnCelebrationFirework(player);
        String message = SPECIAL_PHRASES.get(ThreadLocalRandom.current().nextInt(SPECIAL_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop));
    }

    private void spawnCelebrationFirework(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BURST)
                .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                .withFade(Color.WHITE)
                .build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}
