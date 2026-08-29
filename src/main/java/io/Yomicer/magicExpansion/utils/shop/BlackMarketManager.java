package io.Yomicer.magicExpansion.utils.shop;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.*;

public class BlackMarketManager {

    // 修复：改为 ConcurrentHashMap（异步/并发访问保护）
    private static final Map<UUID, List<BlackMarketTrade>> playerTrades = new ConcurrentHashMap<>();
    private static long lastRefreshTime = 0;
    private static final long REFRESH_INTERVAL = 4 * 60 * 60 * 1000L; // 4小时(毫秒)

    // 简单奖励池 (80%概率)
    private static List<ItemStack> simpleRewardPool = new ArrayList<>();

    // 困难奖励池及权重 (20%概率)
    private static Map<ItemStack, Integer> hardRewardPool = new LinkedHashMap<>();

    // 全局概率定义 (简单0.8，困难0.2)
    public static final double SIMPLE_CHANCE = 0.80;
    public static final double HARD_CHANCE = 0.20;

    // 消耗物池
    private static List<ItemStack> costItemPool = new ArrayList<>();

    // 修复：改为 ConcurrentHashMap（每日已购记录需并发访问），并支持持久化
    private static final Map<UUID, Set<Integer>> dailyPurchases = new ConcurrentHashMap<>();
    // 修复：改为 ConcurrentHashMap
    private static final Map<UUID, Set<Integer>> revealedSlots = new ConcurrentHashMap<>();

    // 修复：每日已购数据持久化文件（blackmarket_data.yml），重启后不丢失限购记录
    private static File dataFile;

    public static class BlackMarketTrade {
        public ItemStack result;
        public List<ItemStack> costs;
        public boolean isFree;
        public boolean isHard;
    }


    public static void init() {
        simpleRewardPool.clear();
        hardRewardPool.clear();
        costItemPool.clear();

        MagicExpansion.getInstance().getLogger().info("正在初始化黑市物品池...");

        // 修复：初始化持久化文件并加载已保存的每日限购数据
        dataFile = new File(MagicExpansion.getInstance().getDataFolder(), "blackmarket_data.yml");
        loadPurchasesData();

        simpleRewardPool.add(new ItemStack(Material.IRON_BLOCK));
        simpleRewardPool.add(new ItemStack(Material.GOLD_BLOCK));
        simpleRewardPool.add(new ItemStack(Material.COPPER_BLOCK));
        simpleRewardPool.add(new ItemStack(Material.EMERALD_BLOCK));
        simpleRewardPool.add(new ItemStack(Material.DIAMOND_BLOCK));
        simpleRewardPool.add(new ItemStack(Material.NETHERITE_BLOCK));

        hardRewardPool.put(new ItemStack(Material.DRAGON_EGG), 5);

        costItemPool.add(new ItemStack(Material.IRON_INGOT));
        costItemPool.add(new ItemStack(Material.GOLD_INGOT));
        costItemPool.add(new ItemStack(Material.EMERALD));
        costItemPool.add(new ItemStack(Material.DIAMOND));
        costItemPool.add(new ItemStack(Material.DIRT));
        costItemPool.add(new ItemStack(Material.ROTTEN_FLESH));
        costItemPool.add(new ItemStack(Material.TORCH));
        costItemPool.add(new ItemStack(Material.NETHER_STAR));
        costItemPool.add(new ItemStack(Material.WITHER_SKELETON_SKULL));
        costItemPool.add(MAGIC_EXPANSION_MAGIC_SUGAR_1);
        costItemPool.add(MAGIC_EXPANSION_MAGIC_SUGAR_2);
        costItemPool.add(MAGIC_EXPANSION_MAGIC_SUGAR_3);
        costItemPool.add(MAGIC_EXPANSION_FINAL_STRING_1);
        costItemPool.add(GOLD_ELEMENT);
        costItemPool.add(WOOD_ELEMENT);
        costItemPool.add(WATER_ELEMENT);
        costItemPool.add(FIRE_ELEMENT);
        costItemPool.add(EARTH_ELEMENT);
        costItemPool.add(PURE_ELEMENT_GOLD);
        costItemPool.add(PURE_ELEMENT_WOOD);
        costItemPool.add(PURE_ELEMENT_WATER);
        costItemPool.add(PURE_ELEMENT_FIRE);
        costItemPool.add(PURE_ELEMENT_EARTH);
        costItemPool.add(PURE_ELEMENT_INGOT);
        costItemPool.add(PURE_FIVE_ELEMENT);


        try {

            double min = 0.8;
            double max = 1.2;

            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_1, (int) (60000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_2, (int) (60000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_3, (int) (60000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_4, (int) (25000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_5, (int) (25000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_11, (int) (1000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_12, (int) (1000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_13, (int) (1000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_14, (int) (1000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_15, (int) (100 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_16, (int) (100 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_17, (int) (100 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_18, (int) (100 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_25, (int) (30 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_26, (int) (30 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_27, (int) (30 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_28, (int) (30 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_29, (int) (30 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_30, (int) (12 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_31, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_35, (int) (10 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_36, (int) (9 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_37, (int) (8 * (min + Math.random() * (max - min))));

            hardRewardPool.put(MAGIC_EXPANSION_MAGIC_SUGAR_CANE, (int) (12 * (min + Math.random() * (max - min))));

            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_1, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_2, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_3, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_4, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_5, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_6, (int) (11 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_11, (int) (8 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_12, (int) (8 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_13, (int) (7 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_14, (int) (7 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_FINAL_STRING_15, (int) (6 * (min + Math.random() * (max - min))));

            hardRewardPool.put(INFINITY_FLINT_AND_STEEL, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_RANDOM_SPAWNER, (int) (21000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(VOID_TOUCH, (int) (22000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FIVE_ELEMENT_TOUCH, (int) (23000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_EXPANSION_ENCHANTING_TABLE, (int) (24000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(DEATH_LIFE_BOOK, (int) (19000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(ITEM_NAME_TAG, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(ENCHANTMENT_ERASER, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FISH_WEIGHT_ENHANCER, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(RESEARCH_UNLOCKER_PAPER, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(WORD_CLEAR, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PORTABLE_SHOP, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(DOUBLE_SIDED_TAPE, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PORTABLE_CARGO_TRANSPORTER, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(CUSTOM_SEQUENCE_TOOL, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(CARGO_FRAGMENT_EXTRACT, (int) (21000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MUSIC_TEST, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MAGIC_WAND, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(RESOURCE_MACHINE_WOOD_BASIC, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(RESOURCE_MACHINE_WOOD_ULTRA, (int) (12000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(LIGHT_GEN_BASIC, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(STRING_GEN_BASIC, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(STRING_GEN_ULTRA, (int) (16000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FIVE_ELEMENT_GEN_BASIC, (int) (11000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(SLIME_BOOK_GEN_ADVANCE, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(ORIGIN_MATERIAL_GEN, (int) (12000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FISH_VIVARIUM_EASY, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FISH_VIVARIUM, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FISH_VIVARIUM_STACK, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(WOOD_TRANSFORM_BASIC, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(WOOD_TRANSFORM_ULTRA, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(LIGHT_TRANSFORM_BASIC, (int) (20000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(LIGHT_EXTRACT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(QUARTZ_PURE_MACHINE_BAISC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(QUARTZ_PURE_MACHINE_ULTRA, (int) (13000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE, (int) (25000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_DEFAULT, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA, (int) (15000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(DIRT_MEAL_MACHINE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FISHING_MACHINE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(INGOT_PURE_MACHINE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(CHINESE_CHARACTER_CONSTRUCTOR, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(SEND_ITEMS_TO_PLAYER_MACHINE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(SEND_ITEMS_TO_PLAYER_MACHINE_SF, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(WHITE_SLOTS_CHEST_53, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PHANTON_SUPPRESSION, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(RIGHT_CLICK_MAN, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(SF_TIMINGS_HOLOGRAM, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MIHOYO_STAR_RAY_MACHINE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(CARGO_TERMINAL_RENEW, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(DRAW_MACHINE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(GEO_MINER_PLUS, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FIVE_ELEMENT_MINER, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(ENERGY_CONNECTOR_GLASS_INFO, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(POWER_CORE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PURE_INGOT_POWER_CORE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(BAD_LUCK_CAPACITY, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PANDORA_CAPACITY, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(POWER_FIRE_STABILITY, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(POWER_COLOR_EGG_BLOCK, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(POWER_COLOR_EGG_KEY, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(POWER_FISH_ELECTRIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_NETHERITE_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_MINERAL_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_MINERAL_ULTRA, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_MAGNESIUM_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_ZINC_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_ALUMINUM_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_LEAD_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_SILVER_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_TIN_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_COPPER_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_GOLD_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(MINE_MAN_IRON_INGOT_BASIC, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(FIRE_ZOMBIE, (int) (18000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(WIND_ELF_SPAWN, (int) (18000 * (min + Math.random() * (max - min))));

            hardRewardPool.put(PRE_BUILDING_OAK_TREE, (int) (900 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PRE_BUILDING_MANGROVE_TREE, (int) (9000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PRE_BUILDING_FISHING_PORT, (int) (9000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PRE_BUILDING_KFC_SMALL, (int) (9000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PRE_BUILDING_LARGE_SNOW_KING, (int) (9000 * (min + Math.random() * (max - min))));
            hardRewardPool.put(PRE_BUILDING_KRUSTY_KRAB, (int) (9000 * (min + Math.random() * (max - min))));

            hardRewardPool.put(ITEM_ORIGIN_BACK_TRACK, 1);
            hardRewardPool.put(WEAPON_STAR_SHARDS_SWORD, 1);
            hardRewardPool.put(ORIGIN_MATERIAL_GEN_ULTRA, 1);




        } catch (Throwable e) {
            MagicExpansion.getInstance().getLogger().warning("黑市物品池加载自定义物品时发生严重错误，仅加载原版兜底物品: ");
            e.printStackTrace();
        }

        // 再次兜底
        if (simpleRewardPool.isEmpty()) simpleRewardPool.add(new ItemStack(Material.DIRT));
        if (hardRewardPool.isEmpty()) hardRewardPool.put(new ItemStack(Material.BEDROCK), 1);
        if (costItemPool.isEmpty()) costItemPool.add(new ItemStack(Material.STONE));

        MagicExpansion.getInstance().getLogger().info("黑市物品池初始化完成。简单物品: " + simpleRewardPool.size() + " 困难物品: " + hardRewardPool.size());

        if (lastRefreshTime == 0) {
            lastRefreshTime = System.currentTimeMillis();
        } else {
            checkAndRefresh();
        }
    }







    public static void checkAndRefresh() {
        if (System.currentTimeMillis() - lastRefreshTime >= REFRESH_INTERVAL) {
            forceRefresh();
        }
    }

    public static void forceRefresh() {
        playerTrades.clear();
        lastRefreshTime = System.currentTimeMillis();
        dailyPurchases.clear();
        revealedSlots.clear();
        // 修复：强制刷新时清空持久化数据文件内容（已购记录随刷新作废）
        clearDataFile();
    }

    /**
     * 修复：每日已购数据持久化 —— 保存 dailyPurchases（uuid -> 已购索引列表）与刷新时间
     */
    private static void savePurchasesData() {
        if (dataFile == null) return;
        try {
            YamlConfiguration yml = new YamlConfiguration();
            yml.set("lastRefreshTime", lastRefreshTime);
            for (Map.Entry<UUID, Set<Integer>> entry : dailyPurchases.entrySet()) {
                yml.set("dailyPurchases." + entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            yml.save(dataFile);
        } catch (IOException e) {
            MagicExpansion.getInstance().getLogger().warning("黑市每日限购数据保存失败: " + e.getMessage());
        }
    }

    /**
     * 修复：初始化时从 blackmarket_data.yml 加载每日限购数据
     */
    private static void loadPurchasesData() {
        if (dataFile == null || !dataFile.exists()) return;
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
            // 恢复上次刷新时间，保证重启后刷新周期连续
            long savedTime = yml.getLong("lastRefreshTime", 0L);
            if (savedTime > 0) {
                lastRefreshTime = savedTime;
            }
            ConfigurationSection sec = yml.getConfigurationSection("dailyPurchases");
            if (sec == null) return;
            for (String key : sec.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<Integer> indexes = sec.getIntegerList(key);
                    dailyPurchases.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).addAll(indexes);
                } catch (IllegalArgumentException ignored) {
                    // 非法 UUID 跳过
                }
            }
        } catch (Throwable t) {
            MagicExpansion.getInstance().getLogger().warning("黑市每日限购数据加载失败: " + t.getMessage());
        }
    }

    /**
     * 修复：清空数据文件内容（写入空配置）
     */
    private static void clearDataFile() {
        if (dataFile == null) return;
        try {
            new YamlConfiguration().save(dataFile);
        } catch (IOException e) {
            MagicExpansion.getInstance().getLogger().warning("黑市数据文件清空失败: " + e.getMessage());
        }
    }

    public static String getTimeRemaining() {
        long elapsed = System.currentTimeMillis() - lastRefreshTime;
        long remaining = REFRESH_INTERVAL - elapsed;
        if (remaining < 0) remaining = 0;

        long hours = remaining / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (remaining % (60 * 1000)) / 1000;

        return hours + "小时" + minutes + "分" + seconds + "秒";
    }

    private static List<BlackMarketTrade> generateTradesForPlayer() {
        // 每次生成前检查，确保池子绝对不为空
        if (simpleRewardPool.isEmpty()) simpleRewardPool.add(new ItemStack(Material.DIRT));
        if (hardRewardPool.isEmpty()) hardRewardPool.put(new ItemStack(Material.BEDROCK), 1);
        if (costItemPool.isEmpty()) costItemPool.add(new ItemStack(Material.STONE));

        List<BlackMarketTrade> todayTrades = new ArrayList<>(); // 改为局部变量
        Random random = new Random();

        // 过滤掉可能混入的 null 物品
        List<ItemStack> hardItems = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : hardRewardPool.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                hardItems.add(entry.getKey());
                weights.add(entry.getValue());
            }
        }

        int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) totalWeight = 1;

        for (int i = 0; i < 10; i++) {
            BlackMarketTrade trade = new BlackMarketTrade();
            trade.isHard = random.nextDouble() < HARD_CHANCE;

            if (trade.isHard) {
                if (hardItems.isEmpty()) {
                    trade.isHard = false;
                } else {
                    int randomWeight = random.nextInt(totalWeight);
                    int currentWeight = 0;
                    for (int j = 0; j < hardItems.size(); j++) {
                        currentWeight += weights.get(j);
                        if (randomWeight < currentWeight) {
                            trade.result = hardItems.get(j).clone();
                            break;
                        }
                    }
                    if (trade.result == null) {
                        trade.result = hardItems.get(0).clone();
                    }
                    trade.result.setAmount(random.nextInt(2) + 1);
                }
            }

            if (!trade.isHard) {
                if (simpleRewardPool.isEmpty()) {
                    trade.result = new ItemStack(Material.AIR);
                } else {
                    trade.result = simpleRewardPool.get(random.nextInt(simpleRewardPool.size())).clone();
                    trade.result.setAmount(random.nextInt(16) + 4);
                }
            }

            if (trade.isHard) {
                trade.isFree = random.nextDouble() < 0.10;
            } else {
                trade.isFree = random.nextDouble() < 0.70;
            }

            if (!trade.isFree) {
                trade.costs = new ArrayList<>();
                if (!costItemPool.isEmpty()) {
                    int costTypeCount = random.nextInt(2) + 1;
                    for (int c = 0; c < costTypeCount; c++) {
                        ItemStack costItem = costItemPool.get(random.nextInt(costItemPool.size())).clone();
                        if (costItem != null) {
                            if (trade.isHard) {
                                costItem.setAmount(random.nextInt(15) + 3);
                            } else {
                                costItem.setAmount(random.nextInt(5) + 1);
                            }
                            trade.costs.add(costItem);
                        }
                    }
                }
                if (trade.costs.isEmpty()) {
                    trade.isFree = true;
                }
            }

            todayTrades.add(trade);
        }

        return todayTrades;
    }


    public static List<ItemStack> getSimplePool() {
        return simpleRewardPool;
    }

    public static Map<ItemStack, Integer> getHardPool() {
        return hardRewardPool;
    }


    public static List<BlackMarketTrade> getTodayTrades(Player player) {
        checkAndRefresh();
        // 如果该玩家没有生成过，则为他单独生成一份
        return playerTrades.computeIfAbsent(player.getUniqueId(), k -> generateTradesForPlayer());
    }

    public static boolean hasPurchased(UUID uuid, int index) {
        return dailyPurchases.containsKey(uuid) && dailyPurchases.get(uuid).contains(index);
    }

    public static void recordPurchase(UUID uuid, int index) {
        dailyPurchases.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(index);
        // 修复：购买后立即写盘持久化，防止重启后限购记录丢失
        savePurchasesData();
    }

    public static boolean isRevealed(UUID uuid, int index) {
        return revealedSlots.containsKey(uuid) && revealedSlots.get(uuid).contains(index);
    }

    public static void reveal(UUID uuid, int index) {
        revealedSlots.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(index);
    }

    /**
     * 修复：玩家退出时清理该玩家的黑市会话数据，防止内存泄漏
     */
    public static void cleanup(UUID uuid) {
        playerTrades.remove(uuid);
        dailyPurchases.remove(uuid);
        revealedSlots.remove(uuid);
    }
}
