package io.Yomicer.magicExpansion.Listener.fishingListener;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.misc.baitbag.BaitBagMenu;
import io.Yomicer.magicExpansion.items.misc.moreLure.MoreLure;
import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.WaterCloudHookManager;
import io.Yomicer.magicExpansion.utils.WaterCloudRodEffects;
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
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 水云间系列专属钓鱼监听
 * 与 PlayerFishingListener 相互独立,各自处理各自的鱼竿钓鱼事件
 */
public class PlayerFishingWaterCloudListener implements Listener {

    // D(C5): 共享静态随机数实例，避免每次钓获都新建 Random
    private static final Random RANDOM = new Random();

    public PlayerFishingWaterCloudListener() {
        // 蓄满自动收竿回调: 由新系统管理器触发
        WaterCloudHookManager.setAutoReelHandler(this::onAutoReel);
    }

    /**
     * 蓄满自动收竿回调: 走新系统中鱼产出链(必中鱼 + 蓄满 10% 特殊钓物判定)
     */
    private void onAutoReel(Player player, Location hookLocation) {
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (sfItem instanceof FishingRodWaterCloud fishingRod) {
            processCatch(player, fishingRod, hookLocation, true);
        }
    }

    // 拥有熟练度系统的水云间鱼竿 ID(后续新竿按需加入)
    private static final Set<String> PROFICIENCY_ROD_IDS = Set.of(
            "FISHING_ROD_BETWEEN_WATER_CLOUD_REED",
            "FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG",
            "FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU"
    );

    // 水云间系列鱼饵(当前鱼饵作为特殊事件判定依据)
    private static final List<MoreLure> SHUIYUNJIAN_LURES = List.of(
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA, "CuiXia"),   // 淬霞
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN, "WeiChen"), // 微尘
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO, "RongHuo"), // 熔火
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN, "YueJin"),   // 跃金
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE, "XingHe"),   // 星核
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA, "JianJia"), // 蒹葭(芦花钓专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE, "LuXue"),   // 芦雪(占位)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU, "WeiLu"),   // 苇露(占位)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU, "BaiLu"),   // 白露(占位)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA, "LuYa"),     // 芦芽(占位)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG, "NingShuang"), // 凝霜(寒江雪专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU, "LuoXu"),           // 落絮(寒江雪专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO, "BingPo"),         // 冰魄(寒江雪专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI, "ChuJi"),           // 初霁(寒江雪专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN, "ChuiLun"),       // 垂纶(寒江雪专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI, "FengSi"),             // 风丝(细雨·斜风专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU, "YanYu"),               // 烟雨(细雨·斜风专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI, "LianBai"),           // 涟白(细雨·斜风专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG, "XiaoFeng"),         // 晓风(细雨·斜风专用)
            new MoreLure(MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING, "XieYing")            // 斜影(细雨·斜风专用)
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

    // 省饵触发提示(旧版, 魔法二代渐变)
    private static final List<String> LURE_PRESERVE_PHRASES = List.of(
            "✦ 水云护饵，此饵未损分毫！",
            "✦ 灵光一闪，鱼饵被水云轻轻送回",
            "✦ 鱼饵与水云共鸣，安然无恙"
    );

    // 双倍鱼获触发提示(新旧通用, 魔法二代渐变)
    private static final List<String> DOUBLE_CATCH_PHRASES = List.of(
            "✦ 水云赐福，渔获翻倍！",
            "✦ 此竿通灵，钓一得二！",
            "✦ 双倍的收获，水云在微笑"
    );

    // 鱼饵池杂物登记(杂物概率降低奖励的作用对象): 材质名集合
    // 后续在鱼饵池中新增杂物(含粘液科技物品)时, 原版物品加入 JUNK_MATERIALS, 粘液物品加入 JUNK_SLIMEFUN_IDS 即可
    private static final Set<String> JUNK_MATERIALS = Set.of(
            "COBBLESTONE",  // 圆石
            "FLINT",        // 燧石
            "GRAVEL",       // 沙砾
            "SAND",         // 沙子
            "STRING",       // 线
            "BONE"          // 骨头
    );

    // 鱼饵池杂物登记(粘液科技物品 ID 集合, 可扩展)
    private static final Set<String> JUNK_SLIMEFUN_IDS = Set.of();

    /**
     * 入口: 水云间鱼竿钓鱼事件
     */
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.FISHING) {
            // 抛竿: 旧系统按熟练度缩短原版等待上限(上钩速度, 非附魔方式)
            applyHookSpeed(e.getPlayer(), e.getHook());

            // 新钓鱼系统: 登记会话并隔离原版判定(原版永不咬钩, 由状态机接管)
            Player player = e.getPlayer();
            ItemStack rod = player.getInventory().getItemInMainHand();
            SlimefunItem sfItem = SlimefunItem.getByItem(rod);
            if (WaterCloudHookManager.isEnabled() && sfItem instanceof FishingRodWaterCloud) {
                FishHook hook = e.getHook();
                hook.setMaxWaitTime(Integer.MAX_VALUE / 2);
                WaterCloudHookManager.startSession(player, hook, rod);
            }
            return;
        }

        // 新钓鱼系统: 收竿(REEL_IN)由状态机判定; CAUGHT_FISH 兜底
        Player player = e.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (WaterCloudHookManager.isEnabled() && sfItem instanceof FishingRodWaterCloud fishingRod) {
            if (e.getState() == PlayerFishEvent.State.REEL_IN) {
                handleNewSystemReel(e, player, fishingRod);
            } else if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                // 兜底: 理论上被隔离后不会触发
                if (e.getCaught() instanceof Item item) {
                    item.remove();
                }
                processCatch(player, fishingRod, e.getHook().getLocation(), false);
            }
            return;
        }

        fishingUtil(e);
    }

    /**
     * 断线清理新钓鱼系统会话
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        WaterCloudHookManager.onQuit(e.getPlayer());
    }

    /**
     * 新系统收竿: 按当前咬钩状态判定中鱼
     * 中鱼 → 走新产出链; 未中鱼 → 脱钩反馈(管理器内部已播放)
     */
    private void handleNewSystemReel(PlayerFishEvent e, Player player, FishingRodWaterCloud fishingRod) {
        if (WaterCloudHookManager.onReel(player)) {
            processCatch(player, fishingRod, e.getHook().getLocation(), false);
        }
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

        // 2. 锁定鱼饵(袋优先只读探测, 防丢袋; 消耗延后)
        Set<String> supportedKeys = fishingRod.getLootTable().keySet();
        String bagKey = BaitBagMenu.peekFromBag(player, supportedKeys);
        Lure activeLure = bagKey != null
                ? SHUIYUNJIAN_LURES.stream().filter(l -> l.getKey().equals(bagKey)).findFirst().orElse(null)
                : getActiveLure(player, fishingRod);

        // 3. 没有鱼饵 → 按原版普通鱼竿走,不干预原版钓获
        if (activeLure == null) {
            return;
        }
        int rodLevel = WaterCloudRodProficiency.getLevel(rod);
        Random random = RANDOM; // D(C5): 改用共享随机数实例

        // 4. 钓获(旧版杂物降低: 池中原版物品=杂物, 权重削减)
        ItemStack drop = getCaughtDrop(player, fishingRod, activeLure, rodLevel, true);
        if (drop == null) return;

        // 5. 移除原版钓获实体,改为掉落本次战利品
        Entity caught = e.getCaught();
        if (caught instanceof Item item) {
            item.remove();
        }

        // 6. 消耗鱼饵(省饵触发则不消耗并提示; 袋丢则尝试背包同款, 再没有则不扣)
        boolean preserved = random.nextDouble() < WaterCloudRodEffects.getOldLurePreserveChance(rodLevel);
        if (!preserved) {
            if (bagKey != null) {
                if (!BaitBagMenu.consumeFromBagByKey(player, bagKey)) {
                    consumeLure(player, activeLure);
                } else {
                    // 饵料袋消耗: 同样弹出水云间消耗提示
                    String prefix = SHUIYUNJIAN_CONSUME_PHRASES.get(random.nextInt(SHUIYUNJIAN_CONSUME_PHRASES.size()));
                    player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r"
                            + ItemStackHelper.getDisplayName(activeLure.getItem()) + ColorGradient.getRandomGradientName(" ！"));
                }
            } else {
                consumeLure(player, activeLure);
            }
        } else {
            player.sendMessage(ColorGradient.getGradientNameVer2(
                    LURE_PRESERVE_PHRASES.get(random.nextInt(LURE_PRESERVE_PHRASES.size()))));
        }

        // 7. 双倍鱼获(旧版, 触发提示)
        if (random.nextDouble() < WaterCloudRodEffects.getOldDoubleCatchChance(rodLevel)) {
            drop.setAmount(Math.min(64, drop.getAmount() * 2));
            player.sendMessage(ColorGradient.getGradientNameVer2(
                    DOUBLE_CATCH_PHRASES.get(random.nextInt(DOUBLE_CATCH_PHRASES.size()))));
        }

        // 8. 在鱼钩位置生成掉落物
        spawnDrop(player, e.getHook().getLocation(), drop);

        // 9. 熟练度系统:按钓获品质给鱼竿增加熟练度
        grantProficiency(player, fishingRod, activeLure, drop);

        // 10. 特殊事件判定
        ItemStack specialCatch = getSpecialCatchForLure(activeLure);
        if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
            triggerSpecialEvent(player, activeLure, drop);
        } else {
            handleNormalCatch(player, drop);
        }
    }

    /**
     * 新系统中鱼产出链(手动/自动/兜底三路共用, 支持无鱼饵杂物池)
     * 鱼饵: 袋优先只读锁定 → 消耗延后(防丢袋); 无饵 → 杂物池
     * 奖励: 蓄满特殊判定(10% + 满级 +5%) / 双倍鱼获(触发提示)
     */
    private void processCatch(Player player, FishingRodWaterCloud fishingRod, Location hookLocation, boolean fullCharge) {
        Set<String> supportedKeys = fishingRod.getLootTable().keySet();
        int rodLevel = WaterCloudRodProficiency.getLevel(player.getInventory().getItemInMainHand());
        Random random = RANDOM; // D(C5): 改用共享随机数实例

        // 1. 锁定鱼饵(袋优先只读探测; 消耗延后, 袋子中途丢失不影响本次鱼获池)
        String bagKey = BaitBagMenu.peekFromBag(player, supportedKeys);
        Lure activeLure = bagKey != null
                ? SHUIYUNJIAN_LURES.stream().filter(l -> l.getKey().equals(bagKey)).findFirst().orElse(null)
                : getActiveLure(player, fishingRod);

        // 2. 钓获: 蓄满特殊判定(10% + 满级特殊+5%) → 战利品池 → 无饵杂物池
        ItemStack drop;
        double rareChance = WaterCloudRodEffects.getNewRareBonus(rodLevel);
        if (activeLure != null && fullCharge && random.nextDouble() < 0.10 + rareChance) {
            drop = getSpecialCatchForLure(activeLure);
        } else {
            drop = activeLure != null
                    ? getCaughtDrop(player, fishingRod, activeLure, rodLevel, false)
                    : getJunkDrop();
        }
        if (drop == null) return;

        // 3. 消耗鱼饵(按锁定的 key; 袋丢 → 背包同款 → 都没有则不扣)
        if (activeLure != null) {
            if (bagKey != null) {
                if (!BaitBagMenu.consumeFromBagByKey(player, bagKey)) {
                    consumeLure(player, activeLure);
                } else {
                    String prefix = SHUIYUNJIAN_CONSUME_PHRASES.get(random.nextInt(SHUIYUNJIAN_CONSUME_PHRASES.size()));
                    player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r"
                            + ItemStackHelper.getDisplayName(activeLure.getItem()) + ColorGradient.getRandomGradientName(" ！"));
                }
            } else {
                consumeLure(player, activeLure);
            }
        }

        // 4. 双倍鱼获(新版, 触发提示)
        if (activeLure != null && random.nextDouble() < WaterCloudRodEffects.getNewDoubleCatchChance(rodLevel)) {
            drop.setAmount(Math.min(64, drop.getAmount() * 2));
            player.sendMessage(ColorGradient.getGradientNameVer2(
                    DOUBLE_CATCH_PHRASES.get(random.nextInt(DOUBLE_CATCH_PHRASES.size()))));
        }

        // 5. 在鱼钩位置生成掉落物
        spawnDrop(player, hookLocation, drop);

        // 6. 熟练度系统(仅芦花钓)
        grantProficiency(player, fishingRod, activeLure, drop);

        // 7. 特殊事件判定(无饵时直接普通事件)
        if (activeLure != null) {
            ItemStack specialCatch = getSpecialCatchForLure(activeLure);
            if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
                triggerSpecialEvent(player, activeLure, drop);
                return;
            }
        }
        handleNormalCatch(player, drop);
    }

    /**
     * 无鱼饵时的杂物池: 垃圾(圆石/沙砾等) + 原版钓物, 概率均分
     */
    private static final List<WeightedItem> JUNK_POOL = List.of(
            new WeightedItem(new ItemStack(Material.COBBLESTONE), 1),
            new WeightedItem(new ItemStack(Material.GRAVEL), 1),
            new WeightedItem(new ItemStack(Material.CLAY_BALL), 1),
            new WeightedItem(new ItemStack(Material.STRING), 1),
            new WeightedItem(new ItemStack(Material.BONE), 1),
            new WeightedItem(new ItemStack(Material.POTION), 1),
            new WeightedItem(new ItemStack(Material.STICK), 1),
            new WeightedItem(new ItemStack(Material.LILY_PAD), 1),
            new WeightedItem(new ItemStack(Material.VINE), 1),
            new WeightedItem(new ItemStack(Material.LEATHER_BOOTS), 1),
            new WeightedItem(new ItemStack(Material.COD), 1),
            new WeightedItem(new ItemStack(Material.SALMON), 1),
            new WeightedItem(new ItemStack(Material.PUFFERFISH), 1),
            new WeightedItem(new ItemStack(Material.TROPICAL_FISH), 1)
    );

    /**
     * 从杂物池均分随机取一个钓获物
     */
    private ItemStack getJunkDrop() {
        return getRandomItemFromWeightedPool(JUNK_POOL);
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
     * 获取本次钓获物(旧版杂物降低: 池中原版物品=杂物, 按熟练度削减权重; 不写死材质, 后续改池自动适配)
     */
    private ItemStack getCaughtDrop(Player player, FishingRodWaterCloud fishingRod, Lure activeLure, int rodLevel, boolean reduceJunk) {
        List<WeightedItem> pool = fishingRod.getLootPoolForLure(activeLure);
        if (pool == null) {
            pool = getDefaultLootPool();
        }
        if (reduceJunk) {
            double reduction = WaterCloudRodEffects.getOldJunkReduction(rodLevel);
            if (reduction > 0) {
                List<WeightedItem> adjusted = new ArrayList<>();
                for (WeightedItem w : pool) {
                    if (isJunkItem(w.getItem())) {
                        // 登记过的杂物条目, 权重削减(保底 1)
                        adjusted.add(new WeightedItem(w.getItem(),
                                Math.max(1, (int) Math.round(w.getWeight() * (1 - reduction)))));
                    } else {
                        adjusted.add(w);
                    }
                }
                pool = adjusted;
            }
        }
        return applyGen2Attributes(getRandomItemFromWeightedPool(pool), fishingRod);
    }

    /**
     * 二代鱼属性注入：若钓获物是二代鱼种子，则按当前鱼竿稀有度档位生成带属性的成品；
     * 非二代鱼原样返回。属性由 {@link FishAttributeGenerator} 统一生成。
     */
    private ItemStack applyGen2Attributes(ItemStack item, FishingRodWaterCloud fishingRod) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        // 仅有 PDC 标记的二代鱼种子才会被识别（普通物品/特殊钓物不参与）
        if (!FishAttributeGenerator.isGen2Fish(item)) {
            return item;
        }
        // 根据鱼竿 weightBoost 映射到属性生成档位(0~4)
        int boostIndex = Math.max(0, Math.min(4, (int) Math.round(fishingRod.getWeightBoost())));
        // 读取种子类型id
        String typeId = item.getItemMeta().getPersistentDataContainer()
                .get(FishAttributeGenerator.GEN2_TYPE, PersistentDataType.STRING);
        Gen2Fish type = Gen2Fish.byId(typeId);
        if (type == null) {
            return item;
        }
        return FishAttributeGenerator.generate(type, boostIndex);
    }

    /**
     * 杂物识别: 显式集合登记(材质名 + 粘液物品 ID), 不按是否原版物品判定
     * 后续在鱼饵池新增杂物时, 只需在 JUNK_MATERIALS / JUNK_SLIMEFUN_IDS 登记
     */
    private boolean isJunkItem(ItemStack item) {
        SlimefunItem sf = SlimefunItem.getByItem(item);
        if (sf != null) {
            return JUNK_SLIMEFUN_IDS.contains(sf.getId());
        }
        return JUNK_MATERIALS.contains(item.getType().name());
    }

    private ItemStack getRandomItemFromWeightedPool(List<WeightedItem> pool) {
        // D(C2): 池为空或总权重 <= 0 时返回 null，防止 nextInt(0) 抛异常（调用方均已有 null 兜底）
        if (pool == null || pool.isEmpty()) {
            return null;
        }
        int total = pool.stream().mapToInt(WeightedItem::getWeight).sum();
        if (total <= 0) {
            return null;
        }
        int r = RANDOM.nextInt(total), current = 0; // D(C5): 改用共享随机数实例
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
        ItemStack special = switch (lure.getKey()) {
            case "CuiXia"  -> MagicExpansionItems.REED_TASSEL;  // 淬霞 → 芦穗(特殊钓物, 青竹竿钓取)
            case "WeiChen" -> MagicExpansionItems.REED_TASSEL;  // 微尘 → 芦穗
            case "RongHuo" -> MagicExpansionItems.REED_TASSEL;  // 熔火 → 芦穗
            case "YueJin"  -> MagicExpansionItems.REED_TASSEL;  // 跃金 → 芦穗
            case "XingHe"  -> MagicExpansionItems.REED_TASSEL;  // 星核 → 芦穗
            case "JianJia", "LuXue", "WeiLu", "BaiLu", "LuYa" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU; // 芦花钓各饵 → 白芦羽(专属特殊钓物)
            case "NingShuang", "LuoXu", "BingPo", "ChuJi", "ChuiLun" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU; // 寒江雪各饵 → 雪魄珠
            case "FengSi", "YanYu", "LianBai", "XiaoFeng", "XieYing" -> MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN;      // 细雨·斜风各饵 → 雨披针
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
                    // D(C5): 改用共享随机数实例
                    String prefix = SHUIYUNJIAN_CONSUME_PHRASES.get(RANDOM.nextInt(SHUIYUNJIAN_CONSUME_PHRASES.size()));
                    player.sendMessage(ColorGradient.getRandomGradientName(prefix) + " §r" + ItemStackHelper.getDisplayName(item) + ColorGradient.getRandomGradientName(" ！"));
                } else {
                    inv.setItem(i, null);
                    // D(C5): 改用共享随机数实例
                    String suffix = SHUIYUNJIAN_LAST_PHRASES.get(RANDOM.nextInt(SHUIYUNJIAN_LAST_PHRASES.size()));
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
        // D(C5): 改用共享随机数实例
        String message = SHUIYUNJIAN_PHRASES.get(RANDOM.nextInt(SHUIYUNJIAN_PHRASES.size()));
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
            case "JianJia" -> handleJianJiaSpecialEvent(player, drop);
            case "LuXue" -> handleLuXueSpecialEvent(player, drop);
            case "WeiLu" -> handleWeiLuSpecialEvent(player, drop);
            case "BaiLu" -> handleBaiLuSpecialEvent(player, drop);
            case "LuYa"  -> handleLuYaSpecialEvent(player, drop);
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
     * 蒹葭特殊事件(TODO:后续在此方法中完善蒹葭独有的特殊效果)
     */
    private void handleJianJiaSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /** 芦雪特殊事件(占位, 复用通用表现) */
    private void handleLuXueSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /** 苇露特殊事件(占位, 复用通用表现) */
    private void handleWeiLuSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /** 白露特殊事件(占位, 复用通用表现) */
    private void handleBaiLuSpecialEvent(Player player, ItemStack drop) {
        playSpecialEventEffects(player, drop);
    }

    /** 芦芽特殊事件(占位, 复用通用表现) */
    private void handleLuYaSpecialEvent(Player player, ItemStack drop) {
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
        // D(C5): 改用共享随机数实例
        String message = SHUIYUNJIAN_SPECIAL_PHRASES.get(RANDOM.nextInt(SHUIYUNJIAN_SPECIAL_PHRASES.size()));
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
     * 抛竿时按熟练度等级缩短原版等待上限(旧系统上钩速度; 每 2 级 -60 tick, 30s 基准 → 满级 18s, 非附魔方式)
     */
    private void applyHookSpeed(Player player, FishHook hook) {
        ItemStack rod = player.getInventory().getItemInMainHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(rod);
        if (sfItem instanceof FishingRodWaterCloud && isProficiencyRod(sfItem)) {
            int ticks = WaterCloudRodEffects.getOldHookSpeedTicks(WaterCloudRodProficiency.getLevel(rod));
            if (ticks > 0) {
                hook.setMaxWaitTime(Math.max(100, 600 - ticks));
            }
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

        // 无鱼饵(杂物池钓获)时无特殊钓物, 避免空指针
        ItemStack specialCatch = activeLure != null ? getSpecialCatchForLure(activeLure) : null;

        int xp;
        if (specialCatch != null && SlimefunUtils.isItemSimilar(drop, specialCatch, true)) {
            xp = WaterCloudRodProficiency.XP_SPECIAL;
        } else {
            xp = WaterCloudRodProficiency.XP_COMMON;
        }

        int oldLevel = WaterCloudRodProficiency.getLevel(rod);
        int newLevel = WaterCloudRodProficiency.addProficiency(rod, xp);
        // 一次读写更新熟练度行 + 属性加成行(避免 §x 渐变行二次往返)
        WaterCloudRodProficiency.updateLoreWithReward(rod);
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
