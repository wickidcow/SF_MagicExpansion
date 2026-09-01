package io.Yomicer.magicExpansion.items.generators;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.items.misc.CargoCoreMore;
import io.Yomicer.magicExpansion.items.misc.CargoFragment;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.items.generators.FishOutputMachine.FISH_OUTPUT_MAP;
import static io.Yomicer.magicExpansion.items.misc.fish.Fish.WeightRarity;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getRandomGradientName;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class FishOutputMachineStack extends MenuBlock implements EnergyNetComponent, RecipeDisplayItem {

    private final int Capacity;
    public static final int ENERGY_CONSUMPTION = 26000;
    private static final int FishSlot = 49;
    private static final int VoidTouchSlot = 50;
    private static final NamespacedKey KEY_X = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
    private static final NamespacedKey KEY_Y = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
    private static final NamespacedKey KEY_Z = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
    private static final NamespacedKey KEY_WORLD = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");
    private static final NamespacedKey KEY_ITEM = new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json");
    private static final NamespacedKey KEY_AMOUNT = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
    private static final int [] MachineSlots = new int [] {
            0,1,2,3,4,5,6,7,8,
            9,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,
            27,28,29,30,31,32,33,34,35,
            36,37,38, 42,43,44,
            45,46,47, 51,52,53
    };

//    // 1. 定义所有鱼类型与输出物品的映射（集中管理，易扩展）
//    private final Map<String, ItemStack> FISH_OUTPUT_MAP = new LinkedHashMap<>() {{
//        put("CopperDustFish",     SlimefunItems.COPPER_DUST);
//        put("GoldDustFish",       SlimefunItems.GOLD_DUST);
//        put("IronDustFish",       SlimefunItems.IRON_DUST);
//        put("TinDustFish",        SlimefunItems.TIN_DUST);
//        put("SilverDustFish",     SlimefunItems.SILVER_DUST);
//        put("AluminumDustFish",   SlimefunItems.ALUMINUM_DUST);
//        put("LeadDustFish",       SlimefunItems.LEAD_DUST);
//        put("ZincDustFish",       SlimefunItems.ZINC_DUST);
//        put("MagnesiumDustFish",  SlimefunItems.MAGNESIUM_DUST);
//        // 🔶 煤晶鱼 → 煤炭
//        put("CoalFish", new ItemStack(Material.COAL));
//        // 💚 翠宝鱼 → 绿宝石
//        put("EmeraldFish", new ItemStack(Material.EMERALD));
//        // 🔷 靛灵鱼 → 青金石
//        put("LapisFish", new ItemStack(Material.LAPIS_LAZULI));
//        // 💎 晶耀鱼 → 钻石
//        put("DiamondFish", new ItemStack(Material.DIAMOND));
//        // 🔴 焰晶鱼 → 下界石英
//        put("QuartzFish", new ItemStack(Material.QUARTZ));
//        // 🟣 震颤鱼 → 紫水晶碎片
//        put("AmethystFish", new ItemStack(Material.AMETHYST_SHARD));
//        // ⚫ 铁核鱼 → 铁锭
//        put("IronFish", new ItemStack(Material.IRON_INGOT));
//        // 🟡 鎏核鱼 → 金锭
//        put("GoldFish", new ItemStack(Material.GOLD_INGOT));
//        // 🟠 铜脉鱼 → 铜锭
//        put("CopperFish", new ItemStack(Material.COPPER_INGOT));
//        // 🟠 赤脉鱼 → 红石
//        put("RedstoneFish", new ItemStack(Material.REDSTONE));
//        // ⚔️ 狱铸鱼 → 下界合金锭
//        put("NetheriteFish", new ItemStack(Material.NETHERITE_INGOT));
//        // ⚔️ 灯笼鱼 → 萤石粉
//        put("GlowStoneDustFish", new ItemStack(Material.GLOWSTONE_DUST));
//        // ⚔️ 塑灵鱼 → 塑料纸
//        put("ShuLingYu", SlimefunItems.PLASTIC_SHEET);
//        // ⚔️ 铀核鱼 → U
//        put("UraniumFish", SlimefunItems.URANIUM);
//        // ⚔️ 油岩鱼 → 原油桶
//        put("OilRockFish", SlimefunItems.OIL_BUCKET);
//        // ⚔️ 泡晶鱼 → 起泡锭
//        put("FoamCrystalFish", SlimefunItems.BLISTERING_INGOT_3);
//        // ⚔️ 黑曜鱼 → 黑金刚石
//        put("BlackDiamondFish", SlimefunItems.CARBONADO);
//        // ⚔️ 灵咒鱼 → 附魔之瓶
//        put("EnchantedBottleFish", new ItemStack(Material.EXPERIENCE_BOTTLE));
//        // ⚔️ 晶鳞鱼 → 硫酸盐
//        put("SulfateFish", SlimefunItems.SULFATE);
//        // ⚔️ 酸晶鱼 → 硅
//        put("SiliconFish", SlimefunItems.SILICON);
//
//        // 【合金灵鱼】用于生产：强化合金锭
//        put("ReinforcedAlloyFish", SlimefunItems.REINFORCED_ALLOY_INGOT);
//
//        // 【硬化灵鱼】用于生产：硬化金属
//        put("HardenedMetalFish", SlimefunItems.HARDENED_METAL_INGOT);
//
//        // 【大马士革灵鱼】用于生产：大马士革钢锭
//        put("DamascusSoulFish", SlimefunItems.DAMASCUS_STEEL_INGOT);
//
//        // 【钢魄鱼】用于生产：钢锭
//        put("SteelSoulFish", SlimefunItems.STEEL_INGOT);
//
//        // 【青铜古影鱼】用于生产：青铜锭
//        put("BronzeAncientFish", SlimefunItems.BRONZE_INGOT);
//
//        // 【硬铝天翔鱼】用于生产：硬铝锭
//        put("HardlightAluFish", SlimefunItems.DURALUMIN_INGOT);
//
//        // 【银铜灵鱼】用于生产：银铜合金锭
//        put("SilverCopperFish", SlimefunItems.BILLON_INGOT);
//
//        // 【黄铜鸣音鱼】用于生产：黄铜锭
//        put("BrassResonanceFish", SlimefunItems.BRASS_INGOT);
//
//        // 【铝黄铜灵鱼】用于生产：铝黄铜锭
//        put("AluminumBrassFish", SlimefunItems.ALUMINUM_BRASS_INGOT);
//
//        // 【铝青铜灵鱼】用于生产：铝青铜锭
//        put("AluminumBronzeFish", SlimefunItems.ALUMINUM_BRONZE_INGOT);
//
//        // 【科林斯青铜灵鱼】用于生产：科林斯青铜锭
//        put("CorinthianBronzeFish", SlimefunItems.CORINTHIAN_BRONZE_INGOT);
//
//        // 【焊锡灵鱼】用于生产：焊锡锭
//        put("SolderFlowFish", SlimefunItems.SOLDER_INGOT);
//
//        // 【镍魄鱼】用于生产：镍锭
//        put("NickelSpiritFish", SlimefunItems.NICKEL_INGOT);
//
//        // 【钴焰鱼】用于生产：钴锭
//        put("CobaltFlameFish", SlimefunItems.COBALT_INGOT);
//
//        // 【硅铁灵鱼】用于生产：硅铁
//        put("SiliconIronFish", SlimefunItems.FERROSILICON);
//
//        // 【碳魂鱼】用于生产：碳块
//        put("CarbonSoulFish", SlimefunItems.CARBON_CHUNK);
//
//        // 【镀金灵鱼】用于生产：镀金铁锭
//        put("GildedIronFish", SlimefunItems.GILDED_IRON);
//
//        // 【红石合金灵鱼】用于生产：红石合金锭
//        put("RedstoneAlloyFish", SlimefunItems.REDSTONE_ALLOY);
//
//        // 【镎影鱼】用于生产：镎
//        put("NeptuniumShadowFish", SlimefunItems.NEPTUNIUM);
//
//        // 【钚心鱼】用于生产：钚
//        put("PlutoniumCoreFish", SlimefunItems.PLUTONIUM);
//
//
//
//
//
//    }};


    public FishOutputMachineStack(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int Capacity) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                FishOutputMachineStack.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }
    protected void tick(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        // A3 修复：区块加载时序窗口内可能取不到菜单（inv == null），入口统一判空返回，避免下方槽位循环直接 NPE
        if (inv == null) {
            return;
        }

        if(inv != null && inv.hasViewer()) {
            if (getCharge(block.getLocation()) < getEnergyConsumption()) {
                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡机器停止运行⚡"),
                                getGradientName("请检查电力供应是否充足")),
                        (player1, slot, item, action) -> false);
                return;
            }
        }
        long totalNormalAmount = 0;
        long totalEasyAmount = 0;

        for (int slot : MachineSlots) {
            ItemStack item = inv.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            if (sfItem == null) {
                continue;
            }

            int amount = item.getAmount(); // 获取该格子中物品的堆叠数量

            if (sfItem instanceof FishOutputMachine) {
                totalNormalAmount += amount;
            } else if (sfItem instanceof FishOutputMachineEasy) {
                totalEasyAmount += amount;
            }
            // === 第二步：检查是否为 CargoFragment 并解析内部物品 ===
            if (sfItem instanceof CargoFragment){
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                if (pdc.has(KEY_ITEM, PersistentDataType.STRING) && pdc.has(KEY_AMOUNT, PersistentDataType.INTEGER)) {
                    String json = pdc.get(KEY_ITEM, PersistentDataType.STRING);
                    Integer fragmentAmount = pdc.get(KEY_AMOUNT, PersistentDataType.INTEGER);

                    if (json != null && fragmentAmount != null && fragmentAmount > 0) {
                        ItemStack originalItem = itemFromBase64(json);
                        if (originalItem != null) {
                            SlimefunItem originalSfItem = SlimefunItem.getByItem(originalItem);
                            if (originalSfItem != null) {
                                if (originalSfItem instanceof FishOutputMachine) {
                                    totalNormalAmount += fragmentAmount;
                                } else if (originalSfItem instanceof FishOutputMachineEasy) {
                                    totalEasyAmount += fragmentAmount;
                                }
                            }
                        }
                    }
                }
            }
            }
            // === 第三步：检查是否为量子存储物品（内部存有生态缸/简易生态缸时计入） ===
            if (NetworkStorage.isQuantumStorageItem(item)) {
                NetworkStorage.QuantumCache qc = NetworkStorage.getQuantumCache(item.getItemMeta());
                if (qc != null && qc.getItemStack() != null) {
                    SlimefunItem storedSf = SlimefunItem.getByItem(qc.getItemStack());
                    long storedAmount = qc.getAmountLong();
                    if (storedSf instanceof FishOutputMachine) {
                        totalNormalAmount += storedAmount;
                    } else if (storedSf instanceof FishOutputMachineEasy) {
                        totalEasyAmount += storedAmount;
                    }
                }
            }
        }


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
        long outAmount = 0; // 本次产出的总数量（long，可超过 int 上限，供外部存储批量存入）
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

            ItemStack baseOutputOrigin = FISH_OUTPUT_MAP.get(fishType);
            if(baseOutputOrigin != null) {
                ItemStack baseOutput = FISH_OUTPUT_MAP.get(fishType).clone();
                if (baseOutput != null) {
                    int multiplier = WeightRarity.getMultiplierByName(weightRarityName);
                    int multiplierEasy = 0;
                    if (multiplier == 1) multiplierEasy = 1;
                    if (multiplier == 7) multiplierEasy = 3;
                    if (multiplier == 15) multiplierEasy = 8;
                    if (multiplier == 9999) multiplierEasy = 888;

                    long amount = (long) (weight * multiplier);
                    long amountEasy = (long) (weight * multiplierEasy);
                    if (amount <= 0) amount = 1;
                    if (amountEasy <= 0) amountEasy = 1;
                    long finalAmount = safeMul(amount, totalNormalAmount);
                    long finalAmountEasy = safeMul(amountEasy, totalEasyAmount);
                    long finalAmountTotal = safeAdd(finalAmount, finalAmountEasy);
                    if (finalAmountTotal <= 0) {
                        finalAmountTotal = 1;
                    }

                    // 保留模板（数量 1）供物品类型比对与界面名称展示；实际数量由 outAmount 以 long 单独承载，
                    // 从而突破 ItemStack 单次 int 上限（1.20.5+ 大数量构造校验），做到"放多少机器按多少效率全存"
                    baseOutput.setAmount(1);
                    outItems = baseOutput;
                    outAmount = finalAmountTotal;

                }
            }

        }

        if (inv != null && inv.hasViewer() && outItems != null) {
            inv.addItem(48, new CustomItemStack(doGlow(Material.SOUL_LANTERN), getGradientName("⚡机器正在运行⚡"),
                            getGradientName("本机器已取消常规输出口"),
                            getGradientName("当前产出: ")+ ItemStackHelper.getDisplayName(outItems),
                            ItemStackHelper.getDisplayName(MagicExpansionItems.FISH_VIVARIUM_EASY)+getRandomGradientName("：" + totalEasyAmount + "个"),
                            ItemStackHelper.getDisplayName(MagicExpansionItems.FISH_VIVARIUM)+getRandomGradientName("：" + totalNormalAmount + "个"),
                            getGradientName("当前效率")+ "§r" +getRandomGradientName(": " + outAmount + "个/tick")), // 用 long 全量数量展示，不再受 int 上限限制
                    (player1, slot, item, action) -> false);
        } else {
            if (inv != null && inv.hasViewer()) {
                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡机器停止运行⚡"),
                                getGradientName("请检查鱼种是否符合")),
                        (player1, slot, item, action) -> false);
            }
        }
        ItemStack VoidTouchSlotItem = inv.getItemInSlot(VoidTouchSlot);
        if (VoidTouchSlotItem != null && !VoidTouchSlotItem.getType().isAir() && outItems != null && outAmount > 0){
            SlimefunItem VoidTouchItem = SlimefunItem.getByItem(VoidTouchSlotItem);
            // ① 虚空之触：绑定魔法存储终端 / 网络量子存储方块
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

                            if (sfItem instanceof CargoCoreMore) {
                                if (pushItemToCargoCoreLong(targetLocation, outItems, outAmount)){
                                    removeCharge(block.getLocation(), getEnergyConsumption());
                                }
                            } else if (NetworkStorage.isQuantumStorageBlock(sfItem)) {
                                // 新增：虚空之触绑定网络量子存储方块（long 批量存入，可突破 int 上限）
                                long leftover = NetworkStorage.storeToQuantumStorageBlock(targetLocation, outItems, outAmount);
                                if (leftover < outAmount) {
                                    removeCharge(block.getLocation(), getEnergyConsumption());
                                }
                            } else {
                                // 绑定目标已被销毁或不是可存储方块：提示玩家，不扣电
                                showDeadTargetWarning(inv);
                            }
                        }
                    }
                }
            }
            // ② 量子存储物品：直接存入（long 批量存入，最大值/溢出保护封装在 NetworkStorage 中）
            else if (VoidTouchSlotItem.getAmount() == 1 && NetworkStorage.isQuantumStorageItem(VoidTouchSlotItem)) {
                long leftover = NetworkStorage.store(VoidTouchSlotItem, outItems, outAmount);
                if (leftover < outAmount) {
                    inv.replaceExistingItem(VoidTouchSlot, VoidTouchSlotItem);
                    removeCharge(block.getLocation(), getEnergyConsumption());
                }
            }
        }

    }

    // 虚空之触绑定的存储目标已失效时的提示（仅在有玩家打开界面时显示）
    private void showDeadTargetWarning(BlockMenu inv) {
        if (inv != null && inv.hasViewer()) {
            inv.replaceExistingItem(48, new CustomItemStack(doGlow(Material.BARRIER),
                    getGradientName("⚡虚空之触目标失效⚡"),
                    getGradientName("绑定的存储方块已被销毁"),
                    getGradientName("请重新绑定或移除虚空之触")));
        }
    }

    // 乘法溢出保护：结果超过 long 上限时返回 Long.MAX_VALUE
    private static long safeMul(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        if (a > Long.MAX_VALUE / b) {
            return Long.MAX_VALUE;
        }
        return a * b;
    }

    // 加法溢出保护：结果超过 long 上限时返回 Long.MAX_VALUE
    private static long safeAdd(long a, long b) {
        if (b > Long.MAX_VALUE - a) {
            return Long.MAX_VALUE;
        }
        return a + b;
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
     * long 版本：把"物品模板 + long 数量"批量写入 CargoCore 已有槽位，
     * 不依赖 ItemStack#getAmount()（int 上限），可突破 21 亿（以太生态阵列场景）。
     */
    private boolean pushItemToCargoCoreLong (Location loc, ItemStack item, long amountToStore) {
        BlockMenu inv = StorageCacheUtils.getMenu(loc);
        if (inv != null) {
            SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
            if (data == null) return false;
            if (hasStoredItem(data, item)) {
                // 写入共享缓存的 long 版本（复用 CargoCoreCache，溢出用 long 加法保护）
                FishOutputMachine.storeItemToExistingSlotCachedLong(data, item, amountToStore);
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
        // A2 修复：改为调用 FishOutputMachine 中的共享缓存实现，避免每 tick 1145 次 Base64 反序列化
        return FishOutputMachine.hasStoredItemCached(data, item);
    }
    /**
     * 只向已有物品的槽位存储（不创建新槽位）
     */
    private void storeItemToExistingSlot(SlimefunBlockData data, ItemStack item) {
        // A2 修复：改为调用 FishOutputMachine 中的共享缓存实现（写入后标 dirty，下一 tick 重建缓存）
        FishOutputMachine.storeItemToExistingSlotCached(data, item);
    }

    // A5: 单 tick 输出上限（与 FishOutputMachine 保持一致，替代原先放行的 Integer.MAX_VALUE）
    private static final int MAX_OUTPUT_PER_TICK = FishOutputMachine.MAX_OUTPUT_PER_TICK;

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
        var1.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,getGradientName("↓魔法鱼槽位↓")),new int[] {

                40
        });
        var1.drawBackground(new CustomItemStack(Material.CHAIN,getGradientName("虚空之触↓网络量子存储")),new int[] {

                41
        });
        var1.drawBackground(new CustomItemStack(Material.END_CRYSTAL,getGradientName("机器工作状态")),new int[] {

                39, 48
        });

    }
    @Nonnull
    @Override
    protected int[] getInputSlots(DirtyChestMenu dirtyChestMenu, ItemStack itemStack) {
        return new int[]{49, 50,
                0,1,2,3,4,5,6,7,8,
                9,10,11,12,13,14,15,16,17,
                18,19,20,21,22,23,24,25,26,
                27,28,29,30,31,32,33,34,35,
                36,37,38, 42,43,44,
                45,46,47, 51,52,53};
    }

    @Override
    protected int[] getInputSlots() {
        return new int[]{
                49, 50,
                0,1,2,3,4,5,6,7,8,
                9,10,11,12,13,14,15,16,17,
                18,19,20,21,22,23,24,25,26,
                27,28,29,30,31,32,33,34,35,
                36,37,38, 42,43,44,
                45,46,47, 51,52,53
        };
    }

    @Override
    protected int[] getOutputSlots() {
        return new int[]{

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

