package io.Yomicer.magicExpansion.items.generators;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.items.misc.CargoCoreMore;
import io.Yomicer.magicExpansion.items.misc.fish.Fish;
import io.Yomicer.magicExpansion.items.misc.fish.FishKeys;
import io.Yomicer.magicExpansion.items.tools.VoidTouch;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.Yomicer.magicExpansion.utils.NetworkStorage;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap; // A2: 缓存层所需

import static io.Yomicer.magicExpansion.items.misc.fish.Fish.*;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getRandomGradientName;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class FishOutputMachine extends MenuBlock implements EnergyNetComponent, RecipeDisplayItem {

    private final int Capacity;
    public static final int ENERGY_CONSUMPTION = 260;
    private static final int FishSlot = 49;
    private static final int VoidTouchSlot = 50;
    private static final NamespacedKey KEY_X = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
    private static final NamespacedKey KEY_Y = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
    private static final NamespacedKey KEY_Z = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
    private static final NamespacedKey KEY_WORLD = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

    // 1. 定义所有鱼类型与输出物品的映射（集中管理，易扩展）
    public static final Map<String, ItemStack> FISH_OUTPUT_MAP = new LinkedHashMap<>() {{
        put("CopperDustFish",     SlimefunItems.COPPER_DUST);
        put("GoldDustFish",       SlimefunItems.GOLD_DUST);
        put("IronDustFish",       SlimefunItems.IRON_DUST);
        put("TinDustFish",        SlimefunItems.TIN_DUST);
        put("SilverDustFish",     SlimefunItems.SILVER_DUST);
        put("AluminumDustFish",   SlimefunItems.ALUMINUM_DUST);
        put("LeadDustFish",       SlimefunItems.LEAD_DUST);
        put("ZincDustFish",       SlimefunItems.ZINC_DUST);
        put("MagnesiumDustFish",  SlimefunItems.MAGNESIUM_DUST);
        // 🔶 煤晶鱼 → 煤炭
        put("CoalFish", new ItemStack(Material.COAL));
        // 💚 翠宝鱼 → 绿宝石
        put("EmeraldFish", new ItemStack(Material.EMERALD));
        // 🔷 靛灵鱼 → 青金石
        put("LapisFish", new ItemStack(Material.LAPIS_LAZULI));
        // 💎 晶耀鱼 → 钻石
        put("DiamondFish", new ItemStack(Material.DIAMOND));
        // 🔴 焰晶鱼 → 下界石英
        put("QuartzFish", new ItemStack(Material.QUARTZ));
        // 🟣 震颤鱼 → 紫水晶碎片
        put("AmethystFish", new ItemStack(Material.AMETHYST_SHARD));
        // ⚫ 铁核鱼 → 铁锭
        put("IronFish", new ItemStack(Material.IRON_INGOT));
        // 🟡 鎏核鱼 → 金锭
        put("GoldFish", new ItemStack(Material.GOLD_INGOT));
        // 🟠 铜脉鱼 → 铜锭
        put("CopperFish", new ItemStack(Material.COPPER_INGOT));
        // 🟠 赤脉鱼 → 红石
        put("RedstoneFish", new ItemStack(Material.REDSTONE));
        // ⚔️ 狱铸鱼 → 下界合金锭
        put("NetheriteFish", new ItemStack(Material.NETHERITE_INGOT));
        // ⚔️ 灯笼鱼 → 萤石粉
        put("GlowStoneDustFish", new ItemStack(Material.GLOWSTONE_DUST));
        // ⚔️ 塑灵鱼 → 塑料纸
        put("ShuLingYu", SlimefunItems.PLASTIC_SHEET);
        // ⚔️ 铀核鱼 → U
        put("UraniumFish", SlimefunItems.URANIUM);
        // ⚔️ 油岩鱼 → 原油桶
        put("OilRockFish", SlimefunItems.OIL_BUCKET);
        // ⚔️ 泡晶鱼 → 起泡锭
        put("FoamCrystalFish", SlimefunItems.BLISTERING_INGOT_3);
        // ⚔️ 黑曜鱼 → 黑金刚石
        put("BlackDiamondFish", SlimefunItems.CARBONADO);
        // ⚔️ 灵咒鱼 → 附魔之瓶
        put("EnchantedBottleFish", new ItemStack(Material.EXPERIENCE_BOTTLE));
        // ⚔️ 晶鳞鱼 → 硫酸盐
        put("SulfateFish", SlimefunItems.SULFATE);
        // ⚔️ 酸晶鱼 → 硅
        put("SiliconFish", SlimefunItems.SILICON);

        // 【合金灵鱼】用于生产：强化合金锭
        put("ReinforcedAlloyFish", SlimefunItems.REINFORCED_ALLOY_INGOT);

        // 【硬化灵鱼】用于生产：硬化金属
        put("HardenedMetalFish", SlimefunItems.HARDENED_METAL_INGOT);

        // 【大马士革灵鱼】用于生产：大马士革钢锭
        put("DamascusSoulFish", SlimefunItems.DAMASCUS_STEEL_INGOT);

        // 【钢魄鱼】用于生产：钢锭
        put("SteelSoulFish", SlimefunItems.STEEL_INGOT);

        // 【青铜古影鱼】用于生产：青铜锭
        put("BronzeAncientFish", SlimefunItems.BRONZE_INGOT);

        // 【硬铝天翔鱼】用于生产：硬铝锭
        put("HardlightAluFish", SlimefunItems.DURALUMIN_INGOT);

        // 【银铜灵鱼】用于生产：银铜合金锭
        put("SilverCopperFish", SlimefunItems.BILLON_INGOT);

        // 【黄铜鸣音鱼】用于生产：黄铜锭
        put("BrassResonanceFish", SlimefunItems.BRASS_INGOT);

        // 【铝黄铜灵鱼】用于生产：铝黄铜锭
        put("AluminumBrassFish", SlimefunItems.ALUMINUM_BRASS_INGOT);

        // 【铝青铜灵鱼】用于生产：铝青铜锭
        put("AluminumBronzeFish", SlimefunItems.ALUMINUM_BRONZE_INGOT);

        // 【科林斯青铜灵鱼】用于生产：科林斯青铜锭
        put("CorinthianBronzeFish", SlimefunItems.CORINTHIAN_BRONZE_INGOT);

        // 【焊锡灵鱼】用于生产：焊锡锭
        put("SolderFlowFish", SlimefunItems.SOLDER_INGOT);

        // 【镍魄鱼】用于生产：镍锭
        put("NickelSpiritFish", SlimefunItems.NICKEL_INGOT);

        // 【钴焰鱼】用于生产：钴锭
        put("CobaltFlameFish", SlimefunItems.COBALT_INGOT);

        // 【硅铁灵鱼】用于生产：硅铁
        put("SiliconIronFish", SlimefunItems.FERROSILICON);

        // 【碳魂鱼】用于生产：碳块
        put("CarbonSoulFish", SlimefunItems.CARBON_CHUNK);

        // 【镀金灵鱼】用于生产：镀金铁锭
        put("GildedIronFish", SlimefunItems.GILDED_IRON);

        // 【红石合金灵鱼】用于生产：红石合金锭
        put("RedstoneAlloyFish", SlimefunItems.REDSTONE_ALLOY);

        // 【镎影鱼】用于生产：镎
        put("NeptuniumShadowFish", SlimefunItems.NEPTUNIUM);

        // 【钚心鱼】用于生产：钚
        put("PlutoniumCoreFish", SlimefunItems.PLUTONIUM);





    }};


    public FishOutputMachine(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int Capacity) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                FishOutputMachine.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }
    protected void tick(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        // A3 修复：区块加载时序窗口内可能取不到菜单（inv == null），入口统一判空返回，避免后续 getItemInSlot 抛 NPE
        if (inv == null) {
            return;
        }

        if (getCharge(block.getLocation()) < getEnergyConsumption()) {
            if(inv != null && inv.hasViewer()) {
                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡机器停止运行⚡"),
                                getGradientName("请检查电力供应是否充足")),
                        (player1, slot, item, action) -> false);
                return;
            }
            return;
        }
//        if(inv != null && inv.hasViewer()) {
//            if (getCharge(block.getLocation()) < getEnergyConsumption()) {
//                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡机器停止运行⚡"),
//                                getGradientName("请检查电力供应是否充足")),
//                        (player1, slot, item, action) -> false);
//                return;
//            }
//        }

        ItemStack fish = null;
        ItemMeta meta = null;
        if (inv != null) {
            fish = inv.getItemInSlot(FishSlot);
            if (fish != null && !fish.getType().isAir()) {
                fish = fish.clone();
            }
        }
        if(fish != null) {
            meta = fish.getItemMeta();
        }
        ItemStack outItems = null;
        if(meta != null) {

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            // 读取PDC数据
            String fishType = pdc.get(FishKeys.FISH_TYPE, PersistentDataType.STRING);
            Double weight = pdc.get(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE);
            String weightRarityName = pdc.get(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING);

            // 基础校验
            if (fishType == null || weight == null || weight == 0.0 || weightRarityName == null) {
                return ;
            }

            // 从映射中查找对应输出物品
            ItemStack baseOutputOrigin = FISH_OUTPUT_MAP.get(fishType);
            if(baseOutputOrigin != null) {
                ItemStack baseOutput = FISH_OUTPUT_MAP.get(fishType).clone();
                if (baseOutput != null) {

                    int multiplier = WeightRarity.getMultiplierByName(weightRarityName);
                    long amount = (long) (weight * multiplier); // 使用 long 防止中间结果溢出
                    if (amount <= 0) {
                        amount = 1;
                    } else if (amount > MAX_OUTPUT_PER_TICK) {
                        // A5 修复：原处直接放行 Integer.MAX_VALUE，现按输出槽容量钳制单 tick 产出上限
                        amount = MAX_OUTPUT_PER_TICK;
                    }

                    baseOutput.setAmount((int) amount);
                    outItems = baseOutput;

                }
            }

        }

        if (inv != null && inv.hasViewer() && outItems != null) {
            inv.addItem(48, new CustomItemStack(doGlow(Material.SOUL_LANTERN), getGradientName("⚡机器正在运行⚡"),
                            getGradientName("本机器会源源不断地生产，即使输出槽已经填满了"),
                            getGradientName("当前产出: ")+ ItemStackHelper.getDisplayName(outItems),
                            getGradientName("当前效率: ")+ "§r" +getRandomGradientName(outItems.getAmount() + "个/tick")), // A1 修复：calculateRealAmount 返回值恒等于 getAmount()，删除冗余循环后直接取值
                    (player1, slot, item, action) -> false);
        } else {
            if (inv != null && inv.hasViewer()) {
                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡机器停止运行⚡"),
                                getGradientName("请检查鱼种是否符合")),
                        (player1, slot, item, action) -> false);
            }
        }

        ItemStack VoidTouchSlotItem = inv.getItemInSlot(VoidTouchSlot);
        if (VoidTouchSlotItem != null && !VoidTouchSlotItem.getType().isAir() && outItems != null){
            // 网络量子存储：直接存入量子存储物品（最大值/溢出保护封装在 NetworkStorage 中）
            if (NetworkStorage.isQuantumStorageItem(VoidTouchSlotItem)) {
                long leftover = NetworkStorage.store(VoidTouchSlotItem, outItems);
                if (leftover < outItems.getAmount()) {
                    inv.replaceExistingItem(VoidTouchSlot, VoidTouchSlotItem);
                    removeCharge(block.getLocation(), getEnergyConsumption());
                }
                return; // 已连接外部存储：只走存储，不做输出格限制、不回落输出格
            }
            // 虚空之触 → 魔法存储终端（原逻辑不变）
            SlimefunItem VoidTouchItem = SlimefunItem.getByItem(VoidTouchSlotItem);
            if (VoidTouchItem != null && VoidTouchItem instanceof VoidTouch) {
                ItemMeta VoidTouchMeta = VoidTouchSlotItem.getItemMeta();
                if (VoidTouchMeta != null) {
                    PersistentDataContainer container = VoidTouchMeta.getPersistentDataContainer();
                    if (container.has(KEY_X, PersistentDataType.INTEGER) &&
                            container.has(KEY_Y, PersistentDataType.INTEGER) &&
                            container.has(KEY_Z, PersistentDataType.INTEGER) &&
                            container.has(KEY_WORLD, PersistentDataType.STRING)) {

                        String worldName = container.get(KEY_WORLD, PersistentDataType.STRING);
                        World world = Bukkit.getWorld(worldName);

                        if (world != null) {
                            int x = container.get(KEY_X, PersistentDataType.INTEGER);
                            int y = container.get(KEY_Y, PersistentDataType.INTEGER);
                            int z = container.get(KEY_Z, PersistentDataType.INTEGER);

                            Location targetLocation = new Location(world, x, y, z);
                            SlimefunItem sfItem = StorageCacheUtils.getSfItem(targetLocation);

                            if (sfItem != null) {
                                if (sfItem instanceof CargoCoreMore) {
                                    if (pushItemToCargoCore(targetLocation, outItems)){
                                        removeCharge(block.getLocation(), getEnergyConsumption());
                                        return;
                                    }
                                } else if (NetworkStorage.isQuantumStorageBlock(sfItem)) {
                                    // 虚空之触绑定网络量子存储方块：直接存入该方块缓存
                                    long leftover = NetworkStorage.storeToQuantumStorageBlock(targetLocation, outItems);
                                    if (leftover < outItems.getAmount()) {
                                        removeCharge(block.getLocation(), getEnergyConsumption());
                                    }
                                    return; // 已连接外部存储：不回落到输出格
                                }
                            }
                        }
                    }
                }
            }
        }

        if (outItems != null && inv != null) {
            // 未连接量子存储：与输出格剩余容量对比，取较小值
            int fit = NetworkStorage.calculateFitAmount(inv, getOutputSlots(), outItems);
            if (outItems.getAmount() > fit) {
                outItems.setAmount(fit);
            }
            if (outItems.getAmount() > 0) {
                pushAllItems(inv,outItems, getOutputSlots());
                // A4 修复：扣电放在确认入库之后（只有实际推入产物才消耗电力）
                removeCharge(block.getLocation(), getEnergyConsumption());
            }
        }

    }

    private boolean pushItemToCargoCore (Location loc, ItemStack item) {
        BlockMenu inv = StorageCacheUtils.getMenu(loc);
        if (inv != null) {
            SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
            if (data == null) return false;
            if (hasStoredItem(data, item)) {
                storeItemToExistingSlot(data, item);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    /**
     * 检查CargoCore中是否已经有该物品
     */
    private boolean hasStoredItem(SlimefunBlockData data, ItemStack item) {
        // A2 修复：改为调用带缓存层的共享实现（见下方 hasStoredItemCached），避免每 tick 1145 次 Base64 反序列化
        return hasStoredItemCached(data, item);
    }
    /**
     * 只向已有物品的槽位存储（不创建新槽位）
     */
    private void storeItemToExistingSlot(SlimefunBlockData data, ItemStack item) {
        // A2 修复：改为调用带缓存层的共享实现（见下方 storeItemToExistingSlotCached）
        storeItemToExistingSlotCached(data, item);
    }

    // ==================== A2 性能修复：CargoCore 槽位扫描缓存 ====================
    // 原实现 hasStoredItem/storeItemToExistingSlot 每 tick 最多进行 2 × 1145 次 itemFromBase64 反序列化。
    // 现按目标方块位置缓存"已解析的槽位列表 + 数量"：读走缓存；写入数量后标 dirty，下一 tick 重建，
    // 缓存约每 20 tick（1 秒）强制刷新一次，兼顾性能与数据一致性。
    private static final long CARGO_CACHE_TTL_MS = 1000; // 缓存刷新间隔 ≈ 20 tick
    private static final ConcurrentHashMap<Location, CargoCoreCache> CARGO_CORE_CACHE = new ConcurrentHashMap<>();

    // 单个目标 CargoCore 的槽位缓存
    private static final class CargoCoreCache {
        long refreshAt;                                   // 下次强制刷新时间戳
        boolean dirty;                                    // 有写入时置 true，下一次访问时重建
        final Map<Integer, CachedSlot> slots = new HashMap<>(); // slotIndex -> 解析结果
    }

    // 缓存的单个槽位解析结果：物品原型（数量 1）+ 当前数量 + 上限
    private static final class CachedSlot {
        ItemStack prototype;
        long count;
        long max = -1; // -1 表示无上限
    }

    // 获取（必要时重建）目标 CargoCore 的槽位缓存；供本类与 Easy/Stack 两个变体共用
    static CargoCoreCache getCargoCoreCache(SlimefunBlockData data) {
        Location loc = data.getLocation();
        // 防止缓存无限增长（机器被破坏后残留的旧条目统一清理）
        if (CARGO_CORE_CACHE.size() > 4096) {
            CARGO_CORE_CACHE.clear();
        }
        long now = System.currentTimeMillis();
        CargoCoreCache cache = CARGO_CORE_CACHE.get(loc);
        if (cache == null || cache.dirty || now >= cache.refreshAt) {
            cache = rebuildCargoCoreCache(data);
            CARGO_CORE_CACHE.put(loc, cache);
        }
        return cache;
    }

    // 从存储数据重建槽位缓存（只解析非空槽位，通常远少于 1145 个）
    private static CargoCoreCache rebuildCargoCoreCache(SlimefunBlockData data) {
        CargoCoreCache cache = new CargoCoreCache();
        cache.refreshAt = System.currentTimeMillis() + CARGO_CACHE_TTL_MS;
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) continue;
            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem == null || storedItem.getType() == Material.AIR) continue;
                storedItem.setAmount(1); // 原型只保留类型信息
                CachedSlot slot = new CachedSlot();
                slot.prototype = storedItem;
                String countStr = data.getData("item_count_" + i);
                if (countStr != null && !countStr.isEmpty()) {
                    try { slot.count = Long.parseLong(countStr); } catch (Exception ignored) {}
                }
                String maxStr = data.getData("item_max_" + i);
                if (maxStr != null && !maxStr.isEmpty()) {
                    try { slot.max = Long.parseLong(maxStr); } catch (Exception ignored) {}
                }
                cache.slots.put(i, slot);
            } catch (Exception ignored) {
                // 单条数据损坏：跳过该槽位
            }
        }
        return cache;
    }

    // A2: 带缓存的"是否已存储该物品"查询
    static boolean hasStoredItemCached(SlimefunBlockData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemStack prototype = item.clone();
        prototype.setAmount(1);
        CargoCoreCache cache = getCargoCoreCache(data);
        for (CachedSlot slot : cache.slots.values()) {
            if (slot.count > 0 && SlimefunUtils.isItemSimilar(prototype, slot.prototype, true)) {
                return true; // 有该物品且数量>0
            }
        }
        return false;
    }

    // A2: 带缓存的"写入已有槽位"实现：写回存储数据并同步缓存计数，随后标 dirty
    static void storeItemToExistingSlotCached(SlimefunBlockData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemStack prototype = item.clone();
        prototype.setAmount(1);
        int amountToStore = item.getAmount();
        CargoCoreCache cache = getCargoCoreCache(data);
        for (Map.Entry<Integer, CachedSlot> entry : cache.slots.entrySet()) {
            CachedSlot slot = entry.getValue();
            if (!SlimefunUtils.isItemSimilar(prototype, slot.prototype, true)) continue;
            // 找到匹配的已有槽位：累加数量并尊重上限
            long newCount = slot.count + amountToStore;
            if (slot.max != -1 && newCount > slot.max) {
                newCount = slot.max; // 超过上限则调整到上限
            }
            data.setData("item_count_" + entry.getKey(), String.valueOf(newCount));
            slot.count = newCount;
            cache.dirty = true; // A2: 写入后标记 dirty，下一 tick 重建缓存保证与存储数据一致
            return;
        }
        // 如果没有找到匹配的槽位，什么也不做（不存储新物品）
    }

    private static final int MAX_STORED_ITEMS = 1145; // 最多支持 18 种不同物品

    // A5: 单 tick 输出上限（输出槽容量级，替代原先放行的 Integer.MAX_VALUE；包内可见供 Easy/Stack 变体共用）
    static final int MAX_OUTPUT_PER_TICK = 6456;

    protected void pushAllItems(BlockMenu menu, ItemStack item, int[] outputSlots) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            return;
        }

        int totalAmount = item.getAmount();  // 总共有多少个
        int perPush = 64;                    // 每次塞64个

        // A4 修复：累加 pushItem 实际接受的量，输出槽满则停止本 tick 推送，
        // 剩余产量由下个 tick 重新生成，不再静默销毁被拒绝的部分
        while (totalAmount > 0) {
            int batch = Math.min(totalAmount, perPush);
            ItemStack toPush = item.clone();
            toPush.setAmount(batch);

            ItemStack leftover = menu.pushItem(toPush, outputSlots); // 返回未放下的部分
            int accepted = batch - (leftover == null ? 0 : leftover.getAmount());
            totalAmount -= accepted;

            if (accepted <= 0) {
                break; // 输出槽已满，剩余留待下 tick
            }
        }
    }

    public int getEnergyConsumption() {
        return ENERGY_CONSUMPTION;
    }



    @Override
    protected void setup(BlockMenuPreset var1) {
        var1.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,getGradientName("请将鱼放入到该槽位中")),new int[] {

                40
        });
        var1.drawBackground(new CustomItemStack(Material.CHAIN,getGradientName("虚空之触槽位")),new int[] {

                41
        });
        var1.drawBackground(new CustomItemStack(Material.END_CRYSTAL,getGradientName("机器工作状态")),new int[] {

                39, 48
        });

    }
    @Nonnull
    @Override
    protected int[] getInputSlots(DirtyChestMenu dirtyChestMenu, ItemStack itemStack) {
        return new int[]{49, 50};
    }

    @Override
    protected int[] getInputSlots() {
        return new int[]{
                49, 50
        };
    }

    @Override
    protected int[] getOutputSlots() {
        return new int[]{
                0,1,2,3,4,5,6,7,8,
                9,10,11,12,13,14,15,16,17,
                18,19,20,21,22,23,24,25,26,
                27,28,29,30,31,32,33,34,35,
                36,37,38, 42,43,44,
                45,46,47, 51,52,53
        };
    }

    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return Capacity;
    }

    @Override
    public @NotNull List<ItemStack> getDisplayRecipes() {

        List<ItemStack> display = new ArrayList<>();
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("使用说明⇩"),getGradientName("请务必仔细阅读")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("使用方法："),getGradientName("将魔法鱼放入到机器槽位中可进行生产")
                ,getGradientName("鱼的种类会影响最终产物种类")
                ,getGradientName("鱼的重量会影响最终产物数量")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("使用说明⇩"),getGradientName("请务必仔细阅读")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("产出量算法："),getGradientName("每个机器只能放置一条魔法鱼")
                ,getGradientName("产出量 = 重量(向下取整) * 魔法鱼稀有程度")
                ,getGradientName("普通/稀有/超级稀有/鱼皇 : 1/7/15/9999")));
        display.add(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71"),getGradientName("支持的鱼类 ⇨")));
        display.add(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71"),getGradientName("产出的产物 ⇨")));

//        display.add(new CustomItemStack(Material.PUFFERFISH_BUCKET,CopperDustFish.getDisplayName(),getGradientName("每秒产出个数："+ " 重量 * 魔法鱼稀有程度 ")));
//        display.add(outputCopperDust);


        for (Map.Entry<String, ItemStack> entry : FISH_OUTPUT_MAP.entrySet()) {
            String fishTypeName = entry.getKey();
            Fish fish = Fish.fromString(fishTypeName);
            if (fish == null) {
                continue; // 跳过无效类型
            }
            ItemStack output = entry.getValue();
            // 根据稀有度选择不同的鱼桶材质
            Material displayMaterial = switch (fish.getRarity()) {
                case COMMON -> Material.COD_BUCKET;           // 普通 - 鳕鱼桶
                case UNCOMMON -> Material.SALMON_BUCKET;     // 不常见 - 鲑鱼桶
                case RARE -> Material.PUFFERFISH_BUCKET;  // 稀有 - 河豚
                case EPIC -> Material.TROPICAL_FISH_BUCKET;     // 史诗 - 热带鱼
                case LEGENDARY -> Material.AXOLOTL_BUCKET;   // 传说 - 用美西螈
                case MYTHICAL -> Material.NAUTILUS_SHELL;       // 神话 - 下界之星（最稀有）
                default -> Material.COD_BUCKET;
            };

            display.add(new CustomItemStack(
                    displayMaterial,
                    fish.getDisplayName(),
                    getGradientName("每秒产出个数：重量 × 魔法鱼体重稀有程度")
            ));
            display.add(output);
        }
        return display;
    }



    private static void addDisplay(List<ItemStack> l,Material m, String s, ItemStack i){
        l.add(new CustomItemStack(m, s, getGradientName("每秒产出个数：" + " 重量 * 魔法鱼稀有程度 ")));
        l.add(i);
    }

}

