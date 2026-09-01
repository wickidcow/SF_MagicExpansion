from pathlib import Path
import re

# Keep GuizhanLibPlugin optional in restored Water Cloud menu code.
p = Path('src/main/java/io/Yomicer/magicExpansion/utils/WaterCloudPoolMenu.java')
s = p.read_text()
s = s.replace('import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;',
              'import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;')
p.write_text(s)

# Restore Release 10 item IDs without replacing the English fork registry.
p = Path('src/main/java/io/Yomicer/magicExpansion/core/MagicExpansionItems.java')
s = p.read_text()
s = s.replace('Version: Build 83', 'Version: Build 90')
translations = {
    '"跃迁储物箱"': '"Page Chest"',
    '"将跃迁储物箱放置在安全位置"': '"Place this chest in a secure location."',
    '"不会与其他箱子合并"': '"Does not merge with adjacent chests."',
    '"共 5 页, 边框显示当前页码"': '"Five pages of storage; the title shows the current page."',
    '"兼容粘液货运(输入/输出节点)"': '"Compatible with Slimefun Cargo input and output nodes."',
    '"界面外的空白处点击翻页"': '"Click outside the inventory window to change pages."',
    '"左键: 上一页    右键: 下一页"': '"Left click: previous page    Right click: next page"',
    '"翻过首尾自动循环"': '"Pages wrap automatically at the beginning and end."',
    '"Shift+右键可放置粘液货运节点"': '"Shift-right-click remains available for Cargo nodes."',
}
for old, new in translations.items():
    s = s.replace(old, new)

rod_marker = '    public static final SlimefunItemStack FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO = createDefaultItem("FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO",Material.FISHING_ROD);\n'
if 'FISHING_ROD_BETWEEN_WATER_CLOUD_REED =' not in s:
    s = s.replace(rod_marker, rod_marker +
        '    public static final SlimefunItemStack FISHING_ROD_BETWEEN_WATER_CLOUD_REED = createDefaultItem("FISHING_ROD_BETWEEN_WATER_CLOUD_REED",Material.FISHING_ROD);\n'
        '    public static final SlimefunItemStack FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG = createDefaultItem("FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG",Material.FISHING_ROD);\n'
        '    public static final SlimefunItemStack FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU = createDefaultItem("FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU",Material.FISHING_ROD);\n')

lure_marker = '    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE",Material.NETHER_STAR);\n'
if 'FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA =' not in s:
    lure_block = '''    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA",Material.WHEAT);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE",Material.WHITE_DYE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU",Material.GLASS_BOTTLE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU",Material.SNOWBALL);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA",Material.BAMBOO);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU",Material.FEATHER);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG",Material.SNOWBALL);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU",Material.FEATHER);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO",Material.ICE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI",Material.LIGHT_BLUE_DYE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN",Material.STICK);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI",Material.STRING);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU",Material.GRAY_DYE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI",Material.LILY_PAD);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG",Material.FEATHER);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING",Material.OAK_SAPLING);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU",Material.GHAST_TEAR);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN",Material.BLAZE_ROD);
'''
    if lure_marker not in s:
        raise SystemExit('Missing XINGHE lure marker')
    s = s.replace(lure_marker, lure_marker + lure_block)

material_marker = '    public static final SlimefunItemStack MAGIC_THREAD = createDefaultItem("MAGIC_THREAD",Material.STRING);\n'
if 'FISH_BREED_POOL =' not in s:
    if material_marker not in s:
        raise SystemExit('Missing MAGIC_THREAD marker')
    s = s.replace(material_marker, material_marker +
        '    public static final SlimefunItemStack REED_TASSEL = createDefaultItem("REED_TASSEL",Material.SUGAR_CANE);\n'
        '    public static final SlimefunItemStack FISH_BREED_POOL = createDefaultItemGlow("FISH_BREED_POOL",Material.CAULDRON);\n')
p.write_text(s)

# Backward-compatible Water Cloud constructor; old cyan-bamboo registration is tier 0.
p = Path('src/main/java/io/Yomicer/magicExpansion/items/tools/FishingRodWaterCloud.java')
s = p.read_text()
ctor = '''    public FishingRodWaterCloud(ItemGroup itemGroup,
                                SlimefunItemStack item,
                                RecipeType recipeType,
                                ItemStack[] recipe,
                                Map<Enchantment, Integer> enchantments,
                                boolean glow,
                                double weightBoost,
'''
if 'Backward-compatible constructor used by pre-Release-10 rod registrations' not in s:
    compat = '''    /** Backward-compatible constructor used by pre-Release-10 rod registrations. */
    public FishingRodWaterCloud(ItemGroup itemGroup,
                                SlimefunItemStack item,
                                RecipeType recipeType,
                                ItemStack[] recipe,
                                Map<Enchantment, Integer> enchantments,
                                boolean glow,
                                Map<String, List<WeightedItem>> lootTable,
                                List<ItemStack> USABLE_LURES) {
        this(itemGroup, item, recipeType, recipe, enchantments, glow, 0.0D, lootTable, USABLE_LURES);
    }

'''
    if ctor not in s:
        raise SystemExit('Missing new FishingRodWaterCloud constructor marker')
    s = s.replace(ctor, compat + ctor)
p.write_text(s)

# Repair partially merged special-catch method and carry Release 10 progression mappings.
p = Path('src/main/java/io/Yomicer/magicExpansion/Listener/fishingListener/PlayerFishingWaterCloudListener.java')
s = p.read_text()
pattern = re.compile(r'    private ItemStack getSpecialCatchForLure\(Lure lure\) \{.*?\n    \}\n\n    /\*\*\n     \* 在鱼钩位置生成掉落物', re.S)
replacement = '''    private ItemStack getSpecialCatchForLure(Lure lure) {
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
     * 在鱼钩位置生成掉落物'''
s, n = pattern.subn(replacement, s, count=1)
if n != 1:
    raise SystemExit('Could not repair getSpecialCatchForLure')
p.write_text(s)

# Replace cache-dependent long CargoCore helper with direct long-safe storage.
p = Path('src/main/java/io/Yomicer/magicExpansion/items/generators/FishOutputMachine.java')
s = p.read_text()
pattern = re.compile(r'    // A2: long 版本写入.*?\n    static void storeItemToExistingSlotCachedLong\(SlimefunBlockData data, ItemStack item, long amountToStore\) \{.*?\n    \}\n\n    private static final int MAX_STORED_ITEMS', re.S)
replacement = '''    // Long-safe write used by large-output machines. It intentionally reuses the
    // fork's direct CargoCore storage format instead of the upstream cache layer.
    static void storeItemToExistingSlotCachedLong(SlimefunBlockData data, ItemStack item, long amountToStore) {
        if (item == null || item.getType() == Material.AIR || amountToStore <= 0) return;
        ItemStack prototype = item.clone();
        prototype.setAmount(1);
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) continue;
            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem == null || storedItem.getType() == Material.AIR) continue;
                storedItem.setAmount(1);
                if (!SlimefunUtils.isItemSimilar(prototype, storedItem, true)) continue;
                long current = 0L;
                String countStr = data.getData("item_count_" + i);
                if (countStr != null && !countStr.isEmpty()) current = Long.parseLong(countStr);
                long next = current > Long.MAX_VALUE - amountToStore ? Long.MAX_VALUE : current + amountToStore;
                String maxStr = data.getData("item_max_" + i);
                if (maxStr != null && !maxStr.isEmpty()) {
                    long max = Long.parseLong(maxStr);
                    if (max != -1L && next > max) next = max;
                }
                data.setData("item_count_" + i, String.valueOf(next));
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private static final int MAX_STORED_ITEMS'''
s, n = pattern.subn(replacement, s, count=1)
if n != 1:
    raise SystemExit('Could not replace CargoCore long helper')
p.write_text(s)

# Add missing long overload for reflected Networks quantum blocks; expose lore refresh for BaitBag.
p = Path('src/main/java/io/Yomicer/magicExpansion/utils/NetworkStorage.java')
s = p.read_text()
if 'storeToQuantumStorageBlock(@NotNull Location location, @NotNull ItemStack prototype, long outputAmount)' not in s:
    marker = '    /**\n     * 向量子存储物品中存入产物（最大值保护 / 数据溢出保护）。'
    overload = '''    /** Store a long quantity into a reflected Networks quantum-storage block. */
    public static long storeToQuantumStorageBlock(@NotNull Location location, @NotNull ItemStack prototype, long outputAmount) {
        if (outputAmount <= 0) return 0;
        try {
            Class<?> cls = Class.forName(QUANTUM_STORAGE_BLOCK_CLASS);
            Object cache = ((Map<?, ?>) cls.getMethod("getCaches").invoke(null)).get(location);
            if (cache == null) return outputAmount;
            ItemStack stored = (ItemStack) cache.getClass().getMethod("getItemStack").invoke(cache);
            if (stored == null || stored.getType().isAir() || !SlimefunUtils.isItemSimilar(prototype, stored, true)) return outputAmount;
            long remaining = outputAmount;
            while (remaining > 0) {
                int batch = (int) Math.min(remaining, Integer.MAX_VALUE);
                ItemStack[] input = new ItemStack[]{prototype.clone()};
                input[0].setAmount(batch);
                cls.getMethod("tryInputItem", Location.class, ItemStack[].class, cache.getClass()).invoke(null, location, input, cache);
                ItemStack leftover = input[0];
                int notStored = leftover == null ? 0 : leftover.getAmount();
                long storedThisBatch = (long) batch - notStored;
                if (storedThisBatch <= 0) break;
                remaining -= storedThisBatch;
            }
            cls.getMethod("syncBlock", Location.class, cache.getClass()).invoke(null, location, cache);
            return remaining;
        } catch (Throwable ignored) {
            return outputAmount;
        }
    }

'''
    if marker not in s:
        raise SystemExit('Missing quantum storage insertion marker')
    s = s.replace(marker, overload + marker)
s = s.replace('    private static void refreshLore(@NotNull ItemMeta meta, @NotNull QuantumCache cache) {',
              '    public static void refreshLore(@NotNull ItemMeta meta, @NotNull QuantumCache cache) {')
p.write_text(s)

# Register Release 10 listeners while preserving fork startup flow.
p = Path('src/main/java/io/Yomicer/magicExpansion/MagicExpansion.java')
s = p.read_text()
if 'import io.Yomicer.magicExpansion.Listener.AccelerationUseListener;' not in s:
    s = s.replace('import io.Yomicer.magicExpansion.Listener.RecipePreLoader;',
                  'import io.Yomicer.magicExpansion.Listener.AccelerationUseListener;\nimport io.Yomicer.magicExpansion.Listener.GuideVirtualGroupClickListener;\nimport io.Yomicer.magicExpansion.Listener.RecipePreLoader;')
if 'import io.Yomicer.magicExpansion.Listener.fishingListener.GuidePoolButtonListener;' not in s:
    s = s.replace('import io.Yomicer.magicExpansion.Listener.fishingListener.PlayerFishingListener;',
                  'import io.Yomicer.magicExpansion.Listener.fishingListener.GuidePoolButtonListener;\nimport io.Yomicer.magicExpansion.Listener.fishingListener.PlayerFishingListener;')
if 'new GuidePoolButtonListener()' not in s:
    s = s.replace('getServer().getPluginManager().registerEvents(new PlayerFishingWaterCloudListener(), this);',
                  'getServer().getPluginManager().registerEvents(new PlayerFishingWaterCloudListener(), this);\n        getServer().getPluginManager().registerEvents(new GuidePoolButtonListener(), this);\n        getServer().getPluginManager().registerEvents(new GuideVirtualGroupClickListener(), this);\n        getServer().getPluginManager().registerEvents(new AccelerationUseListener(), this);')
p.write_text(s)
