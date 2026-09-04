package io.Yomicer.magicExpansion.Listener.fishingListener;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.misc.moreLure.MoreLure;
import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Dedicated fishing listener for the Water Cloud rod series
 * Independent from PlayerFishingListener; each listener handles its own rod family.
 */
public class PlayerFishingWaterCloudListener implements Listener {

    // Water Cloud lure list; the active lure determines special-catch behavior.
    private static final List<MoreLure> SHUIYUNJIAN_LURES = List.of(
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA, "CuiXia"),   // 淬霞
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN, "WeiChen"), // 微尘
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO, "RongHuo"), // 熔火
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN, "YueJin"),   // 跃金
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE, "XingHe")    // 星核
    );

    // Water Cloud normal-catch messages.
    private static final List<String> SHUIYUNJIAN_PHRASES = List.of(
            "Bamboo shadows shatter the moon across the river.",
            "Where water meets cloud, something takes the hook.",
            "The bobber breaks a thousand reflected peaks.",
            "The rod trembles, as though an old friend has arrived.",
            "The moon sinks below, and the hook lifts its glow.",
            "This cast draws up half a river of sunset.",
            "The line enters the clouds and startles the river stars.",
            "Still water hides a living current.",
            "The wind rests on the water while ripples linger.",
            "Reel in, and do not waste this river of autumn.",
            "A ring of ripples opens between water and cloud.",
            "The night is so still even the fish forget to breathe.",
            "Chime—the water opens into a ring of moonlight."
    );

    // Water Cloud special-catch messages.
    private static final List<String> SHUIYUNJIAN_SPECIAL_PHRASES = List.of(
            "Water and cloud surge—you hooked something enormous: ",
            "A great catch breaks the surface and scatters the reeds: ",
            "A star-core glimmers, and the water flashes: ",
            "Something from beyond the sky took the hook: ",
            "This cast stirred the whole realm of water and cloud: "
    );

    // Water Cloud lure-consumption messages.
    private static final List<String> SHUIYUNJIAN_CONSUME_PHRASES = List.of(
            "One lure enters the water, and all grows still: ",
            "Between water and cloud, this lure returns to the current: ",
            "The rod rises, the lure falls, and moonlit wind enters the water: ",
            "This lure sinks into the depths of water and cloud: ",
            "Let stream and mountain witness one lure spent: ",
            "The lure falls silently, and ripples open: ",
            "Another piece of moonlit wind is given to the current: "
    );

    // Water Cloud final-lure messages.
    private static final List<String> SHUIYUNJIAN_LAST_PHRASES = List.of(
            " The last one has sunk into water and cloud with this cast.",
            " It has gone to the current; none remain.",
            " The last is spent; only the sound of water remains.",
            " All are gone, and water and cloud lose a trace of color."
    );

    /**
     * Entry point for Water Cloud fishing events.
     */
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        fishingUtil(e);
    }

    /**
     * Main Water Cloud fishing flow.
     * No lure: leave vanilla fishing untouched.
     * With a lure: roll the configured loot pool; tagged catches trigger special handling.
     */
    private void fishingUtil(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = e.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);

        // 1. Verify this is a Water Cloud fishing rod.
        if (!(sfItem instanceof FishingRodWaterCloud fishingRod)) return;

        // Release 11: Water Cloud catches require the hook to be in water.
        if (!isHookInWater(e.getHook().getLocation())) return;

        // 2. Resolve the active lure: offhand first, then inventory.
        Lure activeLure = getActiveLure(player, fishingRod);

        // 3. No lure: preserve vanilla fishing behavior.
        if (activeLure == null) return;

        // 4. Roll the existing weighted loot pool.
        ItemStack drop = getCaughtDrop(player, fishingRod, activeLure);
        if (drop == null) return;

        // 5. Remove the vanilla catch entity and spawn the selected reward.
        Entity caught = e.getCaught();
        if (caught instanceof Item item) {
            item.remove();
        }

        // 6. Consume the lure and show the Water Cloud message.
        consumeLure(player, activeLure);

        // 7. Spawn the reward at the hook location.
        spawnDrop(player, e.getHook().getLocation(), drop);

        // 8. Route tagged catches to the special event; otherwise use normal handling.
        ItemStack specialCatch = getSpecialCatchForLure(activeLure);
        if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
            triggerSpecialEvent(player, drop);
        } else {
            handleNormalCatch(player, drop);
        }
    }

    /**
     * Find the active lure: offhand first, then inventory.
     */
    private Lure getActiveLure(Player player, FishingRodWaterCloud fishingRod) {
        Set<String> supportedKeys = fishingRod.getLootTable().keySet();

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null) {
            Lure lure = SHUIYUNJIAN_LURES.stream()
                    .filter(l -> supportedKeys.contains(l.getKey()))
                    .filter(l -> SlimefunUtils.isItemSimilar(offHand, l.getItem(), true))
                    .findFirst()
                    .orElse(null);
            if (lure != null) return lure;
        }

        return SHUIYUNJIAN_LURES.stream()
                .filter(l -> supportedKeys.contains(l.getKey()))
                .filter(l -> l.hasLure(player))
                .findFirst()
                .orElse(null);
    }

    /**
     * Select this catch using the existing weighted loot logic.
     */
    private ItemStack getCaughtDrop(Player player, FishingRodWaterCloud fishingRod, Lure activeLure) {
        List<WeightedItem> pool = fishingRod.getLootPoolForLure(activeLure);
        return getRandomItemFromWeightedPool(pool != null ? pool : getDefaultLootPool());
    }

    private ItemStack getRandomItemFromWeightedPool(List<WeightedItem> pool) {
        int total = pool.stream().mapToInt(WeightedItem::getWeight).sum();
        int r = new Random().nextInt(total), current = 0;
        for (WeightedItem w : pool) if ((current += w.getWeight()) > r) return w.getItem().clone();
        return pool.get(0).getItem().clone();
    }

    private List<WeightedItem> getDefaultLootPool() {
        return List.of(
                new WeightedItem(new ItemStack(Material.COD), 50),
                new WeightedItem(new ItemStack(Material.INK_SAC), 30)
        );
    }

    /**
     * Map the active lure to its special catch.
     * Return a clone rather than mutating the shared MagicExpansionItems instance.
     * Catch modifiers can change stack size, so shared item instances must never be returned directly.
     */
    private ItemStack getSpecialCatchForLure(Lure lure) {
        ItemStack special = switch (lure.getKey()) {
            case "CuiXia", "WeiChen", "RongHuo", "YueJin", "XingHe" -> MagicExpansionItems.REED_TASSEL;
            case "JianJia", "LuXue", "WeiLu", "BaiLu", "LuYa" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU;
            case "NingShuang", "LuoXu", "BingPo", "ChuJi", "ChuiLun" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU;
            case "FengSi", "YanYu", "LianBai", "XiaoFeng", "XieYing" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN;
            default -> null;
        };
        return special != null ? special.clone() : null;
    }

    /**
     * Spawn the reward at the hook location.(沿用原本的掉落方式)
     */
    private void spawnDrop(Player player, Location hookLocation, ItemStack drop) {
        if (drop == null) return;
        Item rewardItem = player.getWorld().dropItem(hookLocation, drop);
        rewardItem.setPickupDelay(0);
        Vector direction = player.getLocation().add(0, 2, 0).toVector()
                .subtract(hookLocation.toVector())
                .normalize()
                .multiply(2.5);
        rewardItem.setVelocity(direction);
        rewardItem.setGlowing(true);
    }

    /**
     * Consume the lure and show the Water Cloud message.
     */
    private void consumeLure(Player player, Lure lure) {
        PlayerInventory inv = player.getInventory();
        ItemStack requiredItem = lure.getItem();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && SlimefunUtils.isItemSimilar(item, requiredItem, true)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    String prefix = SHUIYUNJIAN_CONSUME_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_CONSUME_PHRASES.size()));
                    player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r" + ItemStackHelper.getDisplayName(item) + ColorGradient.getRandomGradientName("!"));
                } else {
                    inv.setItem(i, null);
                    String suffix = SHUIYUNJIAN_LAST_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_LAST_PHRASES.size()));
                    player.sendMessage(ItemStackHelper.getDisplayName(item) + ColorGradient.getRandomGradientName(suffix));
                }
                player.updateInventory();
                break;
            }
        }
    }

    /**
     * Normal catch: show the Water Cloud catch message.
     */
    private void handleNormalCatch(Player player, ItemStack drop) {
        // Vanilla experience pickup sound.
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        String message = SHUIYUNJIAN_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop) + ColorGradient.getRandomGradientName("!"));
    }

    /**
     * Special catch: show the dedicated message and effects.
     */
    private void triggerSpecialEvent(Player player, ItemStack drop) {
        // Vanilla experience pickup sound.
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        // Launch a harmless celebration firework from the player.
        spawnCelebrationFirework(player);
        String message = SHUIYUNJIAN_SPECIAL_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_SPECIAL_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop) + ColorGradient.getRandomGradientName("!"));
    }

    /**
     * Launch a decorative, harmless celebration firework.
     */
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
    /**
     * Checks whether the fishing hook is in water. The downward probe covers hooks
     * resting exactly on the top edge of a water block.
     */
    private boolean isHookInWater(Location location) {
        if (location.getBlock().getType() == Material.WATER) {
            return true;
        }
        return location.clone().subtract(0, 0.1, 0).getBlock().getType() == Material.WATER;
    }

}
