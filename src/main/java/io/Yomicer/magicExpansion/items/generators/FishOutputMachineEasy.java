package io.Yomicer.magicExpansion.items.generators;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.items.misc.CargoCore;
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
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

import static io.Yomicer.magicExpansion.items.generators.FishOutputMachine.FISH_OUTPUT_MAP;
import static io.Yomicer.magicExpansion.items.misc.fish.Fish.WeightRarity;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getRandomGradientName;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemToBase64;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class FishOutputMachineEasy extends MenuBlock implements EnergyNetComponent, RecipeDisplayItem {

    private final int Capacity;
    public static final int ENERGY_CONSUMPTION = 260;
    private static final int FishSlot = 49;
    private static final int VoidTouchSlot = 50;
    private static final NamespacedKey KEY_X = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
    private static final NamespacedKey KEY_Y = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
    private static final NamespacedKey KEY_Z = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
    private static final NamespacedKey KEY_WORLD = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

//    // 1. 定义所有鱼类型与输出物品的映射(集中管理,易扩展)
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
//        // [合金灵鱼]用于生产:强化合金锭
//        put("ReinforcedAlloyFish", SlimefunItems.REINFORCED_ALLOY_INGOT);
//
//        // [硬化灵鱼]用于生产:硬化金属
//        put("HardenedMetalFish", SlimefunItems.HARDENED_METAL_INGOT);
//
//        // [大马士革灵鱼]用于生产:大马士革钢锭
//        put("DamascusSoulFish", SlimefunItems.DAMASCUS_STEEL_INGOT);
//
//        // [钢魄鱼]用于生产:钢锭
//        put("SteelSoulFish", SlimefunItems.STEEL_INGOT);
//
//        // [青铜古影鱼]用于生产:青铜锭
//        put("BronzeAncientFish", SlimefunItems.BRONZE_INGOT);
//
//        // [硬铝天翔鱼]用于生产:硬铝锭
//        put("HardlightAluFish", SlimefunItems.DURALUMIN_INGOT);
//
//        // [银铜灵鱼]用于生产:银铜合金锭
//        put("SilverCopperFish", SlimefunItems.BILLON_INGOT);
//
//        // [黄铜鸣音鱼]用于生产:黄铜锭
//        put("BrassResonanceFish", SlimefunItems.BRASS_INGOT);
//
//        // [铝黄铜灵鱼]用于生产:铝黄铜锭
//        put("AluminumBrassFish", SlimefunItems.ALUMINUM_BRASS_INGOT);
//
//        // [铝青铜灵鱼]用于生产:铝青铜锭
//        put("AluminumBronzeFish", SlimefunItems.ALUMINUM_BRONZE_INGOT);
//
//        // [科林斯青铜灵鱼]用于生产:科林斯青铜锭
//        put("CorinthianBronzeFish", SlimefunItems.CORINTHIAN_BRONZE_INGOT);
//
//        // [焊锡灵鱼]用于生产:焊锡锭
//        put("SolderFlowFish", SlimefunItems.SOLDER_INGOT);
//
//        // [镍魄鱼]用于生产:镍锭
//        put("NickelSpiritFish", SlimefunItems.NICKEL_INGOT);
//
//        // [钴焰鱼]用于生产:钴锭
//        put("CobaltFlameFish", SlimefunItems.COBALT_INGOT);
//
//        // [硅铁灵鱼]用于生产:硅铁
//        put("SiliconIronFish", SlimefunItems.FERROSILICON);
//
//        // [碳魂鱼]用于生产:碳块
//        put("CarbonSoulFish", SlimefunItems.CARBON_CHUNK);
//
//        // [镀金灵鱼]用于生产:镀金铁锭
//        put("GildedIronFish", SlimefunItems.GILDED_IRON);
//
//        // [红石合金灵鱼]用于生产:红石合金锭
//        put("RedstoneAlloyFish", SlimefunItems.REDSTONE_ALLOY);
//
//        // [镎影鱼]用于生产:镎
//        put("NeptuniumShadowFish", SlimefunItems.NEPTUNIUM);
//
//        // [钚心鱼]用于生产:钚
//        put("PlutoniumCoreFish", SlimefunItems.PLUTONIUM);
//
//
//
//
//
//    }};


    public FishOutputMachineEasy(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int Capacity) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                FishOutputMachineEasy.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }
    protected void tick(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) {
            return;
        }

        if (getCharge(block.getLocation()) < getEnergyConsumption()) {
            if(inv != null && inv.hasViewer()) {
                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡ Machine Stopped ⚡"),
                                getGradientName("Check that the machine has enough power.")),
                        (player1, slot, item, action) -> false);
                return;
            }
            return;
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
                    if (multiplier == 7) multiplier = 3;
                    if (multiplier == 15) multiplier = 8;
                    if (multiplier == 9999) multiplier = 888;
                    long amount = (long) (weight * multiplier); // 使用 long 防止中间结果溢出
                    if (amount <= 0) {
                        amount = 1;
                    } else if (amount > Integer.MAX_VALUE) {
                        amount = Integer.MAX_VALUE; // 超过上限则截断
                    }

                    baseOutput.setAmount((int) amount);
                    outItems = baseOutput;

                }
            }

        }
        if (inv != null && inv.hasViewer() && outItems != null) {
            inv.addItem(48, new CustomItemStack(doGlow(Material.SOUL_LANTERN), getGradientName("⚡ Machine Running ⚡"),
                            getGradientName("Production continues while external storage has capacity."),
                            getGradientName("Current Output: ")+ ItemStackHelper.getDisplayName(outItems),
                            getGradientName("Current Rate: ")+ "§r" +getRandomGradientName(calculateRealAmount(outItems) + " items/tick")),
                    (player1, slot, item, action) -> false);
        } else {
            if (inv != null && inv.hasViewer()) {
                inv.addItem(48, new CustomItemStack(doGlow(Material.LANTERN), getGradientName("⚡ Machine Stopped ⚡"),
                                getGradientName("Check that the fish type is supported.")),
                        (player1, slot, item, action) -> false);
            }
        }

        ItemStack VoidTouchSlotItem = inv.getItemInSlot(VoidTouchSlot);
        if (VoidTouchSlotItem != null && !VoidTouchSlotItem.getType().isAir() && outItems != null){
            // 网络量子存储：直接存入量子存储物品（最大值/溢出保护封装在 NetworkStorage 中）
            if (VoidTouchSlotItem.getAmount() == 1 && NetworkStorage.isQuantumStorageItem(VoidTouchSlotItem)) {
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

        if (outItems != null) {
            // No external storage: cap production to the available output-slot capacity.
            int fit = NetworkStorage.calculateFitAmount(inv, getOutputSlots(), outItems);
            if (outItems.getAmount() > fit) {
                outItems.setAmount(fit);
            }
            if (outItems.getAmount() > 0) {
                pushAllItems(inv, outItems, getOutputSlots());
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
        if (item == null || item.getType() == Material.AIR) return false;

        ItemStack prototype = item.clone();
        prototype.setAmount(1);

        // 遍历所有存储槽位
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) continue;

            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem != null && storedItem.getType() != Material.AIR) {
                    storedItem.setAmount(1); // 确保只比较类型

                    // 比较物品是否相同
                    if (SlimefunUtils.isItemSimilar(prototype, storedItem, true)) {
                        // 检查当前数量
                        String countStr = data.getData("item_count_" + i);
                        if (countStr != null && !countStr.isEmpty()) {
                            try {
                                long count = Long.parseLong(countStr);
                                if (count > 0) {
                                    return true; // 有该物品且数量>0
                                }
                            } catch (Exception e) {
                                continue;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        return false;
    }
    /**
     * 只向已有物品的槽位存储(不创建新槽位)
     */
    private void storeItemToExistingSlot(SlimefunBlockData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemStack prototype = item.clone();
        prototype.setAmount(1);
        int amountToStore = item.getAmount();

        // 查找匹配的已有槽位
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) continue;

            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem != null && storedItem.getType() != Material.AIR) {
                    storedItem.setAmount(1); // 确保只比较类型

                    if (SlimefunUtils.isItemSimilar(prototype, storedItem, true)) {
                        // 找到匹配的槽位,增加数量
                        long currentCount = 0;
                        String countStr = data.getData("item_count_" + i);
                        if (countStr != null && !countStr.isEmpty()) {
                            try {
                                currentCount = Long.parseLong(countStr);
                            } catch (Exception e) {
                                continue;
                            }
                        }

                        // 计算新数量
                        long newCount = currentCount + amountToStore;
                        data.setData("item_count_" + i, String.valueOf(newCount));

                        // 检查是否有数量限制
                        String maxStr = data.getData("item_max_" + i);
                        if (maxStr != null && !maxStr.isEmpty()) {
                            try {
                                long maxCount = Long.parseLong(maxStr);
                                if (maxCount != -1 && newCount > maxCount) {
                                    // 如果超过上限,调整到上限
                                    newCount = maxCount;
                                    data.setData("item_count_" + i, String.valueOf(newCount));
                                }
                            } catch (Exception e) {
                                // 最大数量解析失败,忽略
                            }
                        }

                        // 存储成功,返回
                        return;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        // 如果没有找到匹配的槽位,什么也不做(不存储新物品)
    }

    private static final int MAX_STORED_ITEMS = 1145; // 最多支持 18 种不同物品

    private int calculateRealAmount(ItemStack item) {
        int totalAmount = item.getAmount(); // 这就是原始总数量
        int maxStackSize = 64;
        int realAmount = 0;

        // 模拟 dropItemInBatches 的分批逻辑,累加每一批的数量
        while (totalAmount > 0) {
            int batchSize = Math.min(totalAmount, maxStackSize);
            realAmount += batchSize;      // 累加这一批
            totalAmount -= batchSize;     // 减去已处理的
        }

        return realAmount;
    }

    protected void pushAllItems(BlockMenu menu, ItemStack item, int[] outputSlots) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            return;
        }

        int totalAmount = item.getAmount();  // 总共有多少个
        int perPush = 64;                    // 每次塞64个

        while (totalAmount > 0) {
            ItemStack toPush = item.clone();
            toPush.setAmount(Math.min(totalAmount, perPush));  // 最后一次可能不足64

            menu.pushItem(toPush, outputSlots);  // 直接塞!不管有没有被拒绝(暴力!)

            totalAmount -= perPush;  // 每次减64,不管实际推进去多少(简单粗暴)
        }
    }

    public int getEnergyConsumption() {
        return ENERGY_CONSUMPTION;
    }



    @Override
    protected void setup(BlockMenuPreset var1) {
        var1.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,getGradientName("Place a Void Touch in this slot")),new int[] {

                40
        });
        var1.drawBackground(new CustomItemStack(Material.IRON_CHAIN,getGradientName("Void Touch Slot")),new int[] {

                41
        });
        var1.drawBackground(new CustomItemStack(Material.END_CRYSTAL,getGradientName("Machine Status")),new int[] {

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
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Instructions ⇩"),getGradientName("Please read carefully.")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("Usage:"),getGradientName("to the machine slot")
                ,getGradientName("type type")
                ,getGradientName("amount")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Instructions ⇩"),getGradientName("Please read carefully.")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("Max Stored Items"),getGradientName("each machine")
                ,getGradientName("Produces resources through Max Stored Items.")
                ,getGradientName("Used by the MagicExpansion fishing system.")));
        display.add(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71"),getGradientName("Max Stored Items")));
        display.add(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71"),getGradientName("Max Stored Items")));

//        display.add(new CustomItemStack(Material.PUFFERFISH_BUCKET,CopperDustFish.getDisplayName(),getGradientName("Items produced per second: "+ " weight × magic-fish rarity ")));
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
                case MYTHICAL -> Material.NAUTILUS_SHELL;       // 神话 - 下界之星(最稀有)
                default -> Material.COD_BUCKET;
            };

            display.add(new CustomItemStack(
                    displayMaterial,
                    fish.getDisplayName(),
                    getGradientName("Output per second: weight × Magic Fish weight rarity.")
            ));
            display.add(output);
        }
        return display;
    }



    private static void addDisplay(List<ItemStack> l,Material m, String s, ItemStack i){
        l.add(new CustomItemStack(m, s, getGradientName("Output per second: " + " weight × Magic Fish rarity")));
        l.add(i);
    }

}

