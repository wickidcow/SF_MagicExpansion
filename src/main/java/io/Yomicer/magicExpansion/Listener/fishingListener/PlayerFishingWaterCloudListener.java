package io.Yomicer.magicExpansion.Listener.fishingListener;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.misc.baitbag.BaitBagMenu;
import io.Yomicer.magicExpansion.items.misc.moreLure.MoreLure;
import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.WaterCloudRodProficiency;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
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
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 水云间系列专属钓鱼监听
 * 与 PlayerFishingListener 相互独立,各自处理各自的鱼竿钓鱼事件
 */
public class PlayerFishingWaterCloudListener implements Listener {

    // 拥有熟练度系统的水云间鱼竿 ID(后续新竿按需加入)
    private static final Set<String> PROFICIENCY_ROD_IDS = Set.of(
            "FISHING_ROD_BETWEEN_WATER_CLOUD_REED"
    );

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
            "风起云涌，竿头微颤，那是",
            "水面静谧，忽而涟漪荡开，原来是",
            "一竿入水，钓起半江浮光，竟是",
            "云影徘徊，不知惊扰了何物，乃是",
            "渔线轻收，似有故人相赠，此物为",
            "水云之间，有灵物破水而来，名为",
            "竿梢一点，惊碎了满池星辰，化作",
            "长夜无声，唯有此物随波而至，乃是",
            "碧波深处，一抹流光浮现，那是",
            "收竿之时，风月俱在，钓起了",
            "浮标沉浮，似在诉说古老的故事，那是",
            "水天一色，竟从虚无中钓出了"
    );

    // 水云间特殊事件提示词(钓到特殊钓物时)
    // 优化点：增强了史诗感和画面感，用词更华丽。
    private static final List<String> SHUIYUNJIAN_SPECIAL_PHRASES = List.of(
            "深渊低语，千年的秘密破水而出，乃是",
            "天地变色，水云翻涌，这一竿竟钓起了",
            "寒芒乍现，连星辰都黯然失色，那是",
            "亘古的沉默被打破，传说中的奇珍现身：",
            "云海震荡，似乎有什么东西正在苏醒，那是",
            "此物出世，引得江河逆流，名为",
            "一股浩然气息自水底升起，那竟是"
    );

    // 水云间消耗鱼饵提示词(前缀 + 饵名 + ！)
    // 优化点：改为四字或短句结尾，留出气口给物品名，意境优美。
    private static final List<String> SHUIYUNJIAN_CONSUME_PHRASES = List.of(
            "轻抚入水，化作涟漪：",
            "付与长风，归于水云：",
            "指尖轻捻，许你一场风月：",
            "沉入水底，去寻那未知的归处：",
            "随波逐流，静候佳音：",
            "此饵入水，万籁俱寂，唯有",
            "投石问路，化作一声叹息："
    );

    // 水云间最后一组鱼饵提示词(接在饵名之后)
    // 优化点：强调“缘分已尽”、“空留余韵”，带有淡淡的惆怅感。
    private static final List<String> SHUIYUNJIAN_LAST_PHRASES = List.of(
            "，这是最后的馈赠，自此缘尽。",
            "，散入水中，此后再无。",
            "，用尽于此，唯余江上清风。",
            "，随风而逝，水云间终成空。",
            "，既是最后一枚，便以此祭奠这满江月色。"
    );

    /**
     * 入口: 水云间鱼竿钓鱼事件
     */
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.FISHING) {
            // 抛竿时按熟练度等级应用 LURE 附魔(提升上钩速度)
            applyHookSpeed(e.getPlayer());
            return;
        }
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

        // 2. 获取当前鱼饵(饵料袋优先,其次副手/背包)
        Set<String> supportedKeys = fishingRod.getLootTable().keySet();
        String bagKey = BaitBagMenu.tryConsumeFromBag(player, supportedKeys);
        Lure activeLure = bagKey != null
                ? SHUIYUNJIAN_LURES.stream().filter(l -> l.getKey().equals(bagKey)).findFirst().orElse(null)
                : getActiveLure(player, fishingRod);

        // 3. 没有鱼饵 → 按原版普通鱼竿走,不干预原版钓获
        if (activeLure == null) {
            return;
        }

        // 4. 使用原本的战利品池加权随机逻辑获取钓获物
        ItemStack drop = getCaughtDrop(player, fishingRod, activeLure);
        if (drop == null) return;

        // 5. 移除原版钓获实体,改为掉落本次战利品
        Entity caught = e.getCaught();
        if (caught instanceof Item item) {
            item.remove();
        }

        // 6. 消耗鱼饵(饵料袋已消耗则跳过,水云间提示)
        if (bagKey == null) {
            consumeLure(player, activeLure);
        } else {
            // 饵料袋消耗: 同样弹出水云间消耗提示
            String prefix = SHUIYUNJIAN_CONSUME_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_CONSUME_PHRASES.size()));
            player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r"
                    + ItemStackHelper.getDisplayName(activeLure.getItem()) + ColorGradient.getRandomGradientName(" ！"));
        }

        // 7. 在鱼钩位置生成掉落物
        spawnDrop(player, e.getHook().getLocation(), drop);

        // 8. 熟练度系统:按钓获品质给鱼竿增加熟练度
        grantProficiency(player, fishingRod, activeLure, drop);

        // 9. 钓起的物品中被标记为特殊事件入口的 → 特殊事件;否则普通事件
        ItemStack specialCatch = getSpecialCatchForLure(activeLure);
        if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
            triggerSpecialEvent(player, activeLure, drop);
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
                    player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r" + ItemStackHelper.getDisplayName(item) + ColorGradient.getRandomGradientName(" ！"));
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
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop) + ColorGradient.getRandomGradientName(" ！！"));
    }

    /**
     * 特殊钓物 → 特殊事件(专属提示,后续可继续加表现)
     */
    private void triggerSpecialEvent(Player player, Lure activeLure, ItemStack drop) {
        // 先识别触发了哪个鱼饵的特殊事件,再分发给对应的一对一处理方法
        switch (activeLure.getKey()) {
            case "CuiXia" -> handleCuiXiaSpecialEvent(player, drop);
            case "WeiChen" -> handleWeiChenSpecialEvent(player, drop);
            case "RongHuo" -> handleRongHuoSpecialEvent(player, drop);
            case "YueJin" -> handleYueJinSpecialEvent(player, drop);
            case "XingHe" -> handleXingHeSpecialEvent(player, drop);
            default -> playSpecialEventEffects(player, drop);
        }
    }

    /**
     * 淬霞特殊事件(TODO:后续在此方法中完善淬霞独有的特殊效果)
     */
    private void handleCuiXiaSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /**
     * 微尘特殊事件(TODO:后续在此方法中完善微尘独有的特殊效果)
     */
    private void handleWeiChenSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /**
     * 熔火特殊事件(TODO:后续在此方法中完善熔火独有的特殊效果)
     */
    private void handleRongHuoSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /**
     * 跃金特殊事件(TODO:后续在此方法中完善跃金独有的特殊效果)
     */
    private void handleYueJinSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /**
     * 星核特殊事件(TODO:后续在此方法中完善星核独有的特殊效果)
     */
    private void handleXingHeSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /**
     * 特殊事件通用表现:传送门音效 + 庆祝烟花 + 专属提示语
     */
    private void playSpecialEventEffects(Player player, ItemStack drop) {
        // 原版获得经验音效
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
        // 从玩家脚底发射一枚无伤害的庆祝烟花
        spawnCelebrationFirework(player);
        String message = SHUIYUNJIAN_SPECIAL_PHRASES.get(new Random().nextInt(SHUIYUNJIAN_SPECIAL_PHRASES.size()));
        player.sendMessage(ColorGradient.getRandomGradientName(message) + " §r" + ItemStackHelper.getDisplayName(drop) + ColorGradient.getRandomGradientName(" ！！"));
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

    /**
     * 是否为拥有熟练度系统的水云间鱼竿
     */
    private boolean isProficiencyRod(SlimefunItem sfItem) {
        if (sfItem == null) return false;
        String id = sfItem.getId();
        // SlimefunItem.getId() 返回带命名空间前缀的完整 ID(如 MAGIC_EXPANSION_FISHING_ROD_BETWEEN_WATER_CLOUD_REED),
        // 这里同时兼容裸 ID 与带前缀 ID
        return PROFICIENCY_ROD_IDS.contains(id)
                || PROFICIENCY_ROD_IDS.stream().anyMatch(suffix -> id.endsWith("_" + suffix));
    }

    /**
     * 抛竿时按熟练度等级应用 LURE 附魔(每 2 级 +1,封顶 5),提升上钩速度
     */
    private void applyHookSpeed(Player player) {
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (sfItem instanceof FishingRodWaterCloud && isProficiencyRod(sfItem)) {
            WaterCloudRodProficiency.applyLureEnchant(rod);
            player.getInventory().setItemInMainHand(rod);
        }
    }

    /**
     * 熟练度系统:按钓获品质(特殊/稀有/普通)给鱼竿增加熟练度
     * 获得熟练度时动作栏显示进度条 2 秒;升级时播放音效并发送意境提示
     */
    private void grantProficiency(Player player, FishingRodWaterCloud fishingRod, Lure activeLure, ItemStack drop) {
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (!(sfItem instanceof FishingRodWaterCloud) || !isProficiencyRod(sfItem)) {
            return;
        }

        ItemStack specialCatch = getSpecialCatchForLure(activeLure);

        int xp;
        if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
            xp = WaterCloudRodProficiency.XP_SPECIAL;
        } else {
            xp = WaterCloudRodProficiency.XP_COMMON;
        }

        int oldLevel = WaterCloudRodProficiency.getLevel(rod);
        int newLevel = WaterCloudRodProficiency.addProficiency(rod, xp);
        WaterCloudRodProficiency.updateLore(rod);
        WaterCloudRodProficiency.applyLureEnchant(rod);
        player.getInventory().setItemInMainHand(rod);

        // 动作栏进度条,2 秒后消失
        int level = WaterCloudRodProficiency.getLevel(rod);
        int xpNow = WaterCloudRodProficiency.getXp(rod);
        player.sendActionBar("§b熟练度: §f" + WaterCloudRodProficiency.getLevelName(level)
                + " §e" + WaterCloudRodProficiency.getPipeBar(level, xpNow)
                + " " + WaterCloudRodProficiency.getPercent(level, xpNow) + " §a+" + xp);
        Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> {
            if (player.isOnline()) {
                player.sendActionBar("");
            }
        }, 40L);

        // 升级反馈
        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            int index = Math.max(0, Math.min(newLevel - 1, WaterCloudRodProficiency.LEVEL_UP_MESSAGES.length - 1));
            player.sendMessage(ColorGradient.getRandomGradientName("✦ " + WaterCloudRodProficiency.LEVEL_UP_MESSAGES[index]));
        }
    }
}
