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
 * 水云间系列专属钓鱼监听
 * 与 PlayerFishingListener 相互独立,各自处理各自的鱼竿钓鱼事件
 */
public class PlayerFishingWaterCloudListener implements Listener {

    // 水云间系列鱼饵(当前鱼饵作为特殊事件判定依据)
    private static final List<MoreLure> SHUIYUNJIAN_LURES = List.of(
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA, "CuiXia"),   // 淬霞
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN, "WeiChen"), // 微尘
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO, "RongHuo"), // 熔火
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN, "YueJin"),   // 跃金
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE, "XingHe")    // 星核
    );

    // 水云间专属钓获提示词(普通钓物)
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

    // 水云间特殊事件提示词(钓到特殊钓物时)
    private static final List<String> SHUIYUNJIAN_SPECIAL_PHRASES = List.of(
            "Water and cloud surge—you hooked something enormous: ",
            "A great catch breaks the surface and scatters the reeds: ",
            "A star-core glimmers, and the water flashes: ",
            "Something from beyond the sky took the hook: ",
            "This cast stirred the whole realm of water and cloud: "
    );

    // 水云间消耗鱼饵提示词(前缀 + 饵名 + ！)
    private static final List<String> SHUIYUNJIAN_CONSUME_PHRASES = List.of(
            "One lure enters the water, and all grows still: ",
            "Between water and cloud, this lure returns to the current: ",
            "The rod rises, the lure falls, and moonlit wind enters the water: ",
            "This lure sinks into the depths of water and cloud: ",
            "Let stream and mountain witness one lure spent: ",
            "The lure falls silently, and ripples open: ",
            "Another piece of moonlit wind is given to the current: "
    );

    // 水云间最后一组鱼饵提示词(接在饵名之后)
    private static final List<String> SHUIYUNJIAN_LAST_PHRASES = List.of(
            " The last one has sunk into water and cloud with this cast.",
            " It has gone to the current; none remain.",
            " The last is spent; only the sound of water remains.",
            " All are gone, and water and cloud lose a trace of color."
    );

    /**
     * 入口: 水云间鱼竿钓鱼事件
     */
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        fishingUtil(e);
    }

    /**
     * 水云间钓鱼主流程
     * 无鱼饵 → 按原版普通鱼竿走(不干预,保留原版钓获)
     * 有鱼饵 → 按战利品池随机钓获;钓起的物品中被标记为特殊事件入口的 → 特殊事件,否则普通事件
     */
    private void fishingUtil(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = e.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);

        // 1. 判定是否水云间系列鱼竿
        if (!(sfItem instanceof FishingRodWaterCloud fishingRod)) return;

        // 2. 获取当前鱼饵(副手优先,其次背包)
        Lure activeLure = getActiveLure(player, fishingRod);

        // 3. 没有鱼饵 → 按原版普通鱼竿走,不干预原版钓获
        if (activeLure == null) return;

        // 4. 使用原本的战利品池加权随机逻辑获取钓获物
        ItemStack drop = getCaughtDrop(player, fishingRod, activeLure);
        if (drop == null) return;

        // 5. 移除原版钓获实体,改为掉落本次战利品
        Entity caught = e.getCaught();
        if (caught instanceof Item item) {
            item.remove();
        }

        // 6. 消耗鱼饵(水云间提示)
        consumeLure(player, activeLure);

        // 7. 在鱼钩位置生成掉落物
        spawnDrop(player, e.getHook().getLocation(), drop);

        // 8. 钓起的物品中被标记为特殊事件入口的 → 特殊事件;否则普通事件
        ItemStack specialCatch = getSpecialCatchForLure(activeLure);
        if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
            triggerSpecialEvent(player, drop);
        } else {
            handleNormalCatch(player, drop);
        }
    }

    /**
     * 获取当前使用的鱼饵(副手优先,其次背包搜索)
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
     * 获取本次钓获物(沿用原本的战利品池加权随机逻辑)
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
     * 当前鱼饵对应的特殊钓物(显式映射,即被标记为特殊事件入口的物品)
     * 修复: 返回 clone 而非 MagicExpansionItems 的 static 单例——
     * 蓄满分支/双倍鱼获会 setAmount 修改 drop, 直接返回单例会被污染并随掉落物扩散。
     */
    private ItemStack getSpecialCatchForLure(Lure lure) {
        return switch (lure.getKey()) {
            case "CuiXia"  -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA;  // 淬霞
            case "WeiChen" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN; // 微尘
            case "RongHuo" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO; // 熔火
            case "YueJin"  -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN;  // 跃金
            case "XingHe"  -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE;  // 星核
            default -> null;
        };
        return special != null ? special.clone() : null;
    }

    /**
     * 在鱼钩位置生成掉落物(沿用原本的掉落方式)
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
     * 消耗鱼饵(水云间提示)
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
     * 普通钓物 → 普通事件(水云间专属钓获提示)
     */
    private void handleNormalCatch(Player player, ItemStack drop) {
        // 原版获得经验音效
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        String message = SHUIYUNJIAN_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop) + ColorGradient.getRandomGradientName("!"));
    }

    /**
     * 特殊钓物 → 特殊事件(专属提示,后续可继续加表现)
     */
    private void triggerSpecialEvent(Player player, ItemStack drop) {
        // 原版获得经验音效
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        // 从玩家脚底发射一枚无伤害的庆祝烟花
        spawnCelebrationFirework(player);
        String message = SHUIYUNJIAN_SPECIAL_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_SPECIAL_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop) + ColorGradient.getRandomGradientName("!"));
    }

    /**
     * 从玩家脚底发射一枚装饰性烟花(无伤害,仅庆祝特效)
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
}
