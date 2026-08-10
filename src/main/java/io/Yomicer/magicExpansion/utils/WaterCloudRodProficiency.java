package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * 水云间·鱼竿熟练度系统
 * 熟练度绑定鱼竿物品(NBT),不绑定玩家;8 个等级,指数式升级曲线
 */
public class WaterCloudRodProficiency {

    public static final NamespacedKey LV_KEY = new NamespacedKey(MagicExpansion.getInstance(), "watercloud_lv");
    public static final NamespacedKey XP_KEY = new NamespacedKey(MagicExpansion.getInstance(), "watercloud_xp");

    public static final int MAX_LEVEL = 8;
    /** 各等级升到下一级所需熟练度(指数式递增,后期分段加值) */
    public static final int[] REQUIREMENTS = {300, 600, 1200, 2200, 3200, 5200, 7200};
    /** 等级名称(古风意境) */
    public static final String[] LEVEL_NAMES = {
            "初见水云", "临渊而望", "知鱼之乐", "竿起风生",
            "水月在手", "踏浪而行", "山水有灵", "一竿一世界"
    };
    /** 每级固定升级提示(不重复) */
    public static final String[] LEVEL_UP_MESSAGES = {
            "此竿初醒,水云之间多了一丝灵性。",
            "竿身轻鸣,仿佛听见了江河的回应。",
            "收放之间,已渐知鱼之乐。",
            "竿起风生,垂纶再无迟滞。",
            "水月在手,此竿愈发通灵。",
            "踏浪而行,江河皆为坦途。",
            "山水有灵,皆与此竿相映。",
            "一竿一世界,水云间再无敌手。"
    };

    /** 普通钓获熟练度 */
    public static final int XP_COMMON = 1;
    /** 稀有钓获熟练度 */
    public static final int XP_RARE = 2;
    /** 特殊钓物熟练度 */
    public static final int XP_SPECIAL = 5;

    /** 动作栏进度条格子数(50 个 |) */
    public static final int ACTION_BAR_LENGTH = 50;
    /** 鱼竿 lore 进度条格子数(33 个 |) */
    public static final int LORE_BAR_LENGTH = 33;

    private WaterCloudRodProficiency() {
    }

    public static int getLevel(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) return 1;
        Integer lv = rod.getItemMeta().getPersistentDataContainer().get(LV_KEY, PersistentDataType.INTEGER);
        return lv == null ? 1 : Math.max(1, Math.min(MAX_LEVEL, lv));
    }

    public static int getXp(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) return 0;
        Integer xp = rod.getItemMeta().getPersistentDataContainer().get(XP_KEY, PersistentDataType.INTEGER);
        return xp == null ? 0 : Math.max(0, xp);
    }

    /** 当前等级升到下一级所需熟练度;满级返回 0 */
    public static int getRequirement(int level) {
        if (level < 1) return REQUIREMENTS[0];
        if (level >= MAX_LEVEL) return 0;
        return REQUIREMENTS[level - 1];
    }

    public static boolean isMaxLevel(int level) {
        return level >= MAX_LEVEL;
    }

    public static String getLevelName(int level) {
        return LEVEL_NAMES[Math.max(1, Math.min(MAX_LEVEL, level)) - 1];
    }

    /** 熟练度百分比(魔法二代渐变色),如 42.86%;满级返回 100.00% */
    public static String getPercent(int level, int xp) {
        int need = getRequirement(level);
        if (need <= 0) return ColorGradient.getGradientNameVer2("100.00%");
        return ColorGradient.getGradientNameVer2(String.format("%.2f%%", Math.min(100.0, xp * 100.0 / need)));
    }

    /** 纯 | 进度条(不带数值):已获取部分使用魔法二代渐变色,未获取部分灰色;满级返回 已臻化境 */
    public static String getPipeBar(int level, int xp) {
        return buildPipeBar(ACTION_BAR_LENGTH, level, xp);
    }

    /** 构建指定长度的 | 进度条:已获取部分使用魔法二代渐变色,未获取部分灰色 */
    private static String buildPipeBar(int length, int level, int xp) {
        int need = getRequirement(level);
        if (need <= 0) return "已臻化境";
        int filled = Math.max(0, Math.min(length, (int) Math.round(xp * (double) length / need)));
        StringBuilder sb = new StringBuilder();
        if (filled >= 2) {
            sb.append(ColorGradient.getGradientNameVer2("|".repeat(filled)));
        } else if (filled == 1) {
            sb.append("§a|");
        }
        for (int i = filled; i < length; i++) sb.append("§7|");
        return sb.toString();
    }

    /** 鱼竿 lore 进度条:渐变 | 展示进度 + 渐变具体数值;满级返回 已臻化境 */
    public static String getProgressBar(int level, int xp) {
        int need = getRequirement(level);
        if (need <= 0) return "已臻化境";
        return buildPipeBar(LORE_BAR_LENGTH, level, xp) + " " + ColorGradient.getGradientNameVer2("(" + xp + "/" + need + ")");
    }

    /**
     * 给鱼竿增加熟练度,返回新等级(可能升级)
     */
    public static int addProficiency(ItemStack rod, int amount) {
        if (rod == null || amount <= 0) return getLevel(rod);
        ItemMeta meta = rod.getItemMeta();
        int level = meta.getPersistentDataContainer().getOrDefault(LV_KEY, PersistentDataType.INTEGER, 1);
        int xp = meta.getPersistentDataContainer().getOrDefault(XP_KEY, PersistentDataType.INTEGER, 0);
        level = Math.max(1, Math.min(MAX_LEVEL, level));
        xp += amount;
        while (level < MAX_LEVEL && xp >= REQUIREMENTS[level - 1]) {
            xp -= REQUIREMENTS[level - 1];
            level++;
        }
        if (level >= MAX_LEVEL) {
            xp = 0;
        }
        meta.getPersistentDataContainer().set(LV_KEY, PersistentDataType.INTEGER, level);
        meta.getPersistentDataContainer().set(XP_KEY, PersistentDataType.INTEGER, xp);
        rod.setItemMeta(meta);
        return level;
    }

    /** 剥离 §x 与所有 § 格式码(渐变行中文字被色码隔开, 直接 contains 匹配会失败) */
    private static String stripColorCodes(String line) {
        return line.replace("§x", "").replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    /**
     * 一次读写完成熟练度行 + 属性加成行更新(钓鱼升级时调用)
     * 避免同一 tick 内对含 §x 渐变行进行二次 getLore/setLore 往返(旧版渐进色正常即因单次往返)
     */
    public static void updateLoreWithReward(ItemStack rod) {
        if (rod == null) return;
        int level = getLevel(rod);
        boolean newSystem = WaterCloudHookManager.isEnabled();
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());

        // 移除旧熟练度行与旧属性加成行(先剥离色码再按文字匹配)
        lore.removeIf(line -> {
            String plain = stripColorCodes(line);
            return plain.contains("熟练度")
                    || REWARD_LINE_PREFIXES.stream().anyMatch(plain::contains);
        });

        // 熟练度行(魔法二代渐变) + 属性加成行
        lore.add("§b熟练度: §f" + getLevelName(level) + " §e" + getProgressBar(level, getXp(rod)));
        List<String> reward = buildRewardLore(level, newSystem);
        if (!reward.isEmpty()) {
            lore.addAll(reward);
        }
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    /**
     * 更新鱼竿 lore 中的熟练度行(保留原有 lore)
     */
    public static void updateLore(ItemStack rod) {
        if (rod == null) return;
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        // 移除旧熟练度行(contains 匹配, 兼容行首颜色格式变化)
        lore.removeIf(line -> line.contains("熟练度"));
        lore.add("§b熟练度: §f" + getLevelName(getLevel(rod)) + " §e" + getProgressBar(getLevel(rod), getXp(rod)));
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    /** 奖励属性行内容标记(用于定位并移除旧奖励段; 行首为渐变颜色码, 用 contains 匹配文字) */
    private static final List<String> REWARD_LINE_PREFIXES = List.of(
            "当前奖励 ·",  // 旧版残留标题行(已不再生成, 清除历史遗留)
            "等待时间：", "轻咬时间：", "蓄力速度：", "双倍鱼获：", "特殊钓物：",
            "上钩速度：", "杂物概率：", "省饵概率："
    );

    /**
     * 更新鱼竿 lore 中的属性加成行(按当前钓鱼系统与等级生成, 无标题行, 直接展示加成; 保留熟练度行与原有 lore)
     * 切换系统/升级后调用, 加成行随系统与等级即时刷新
     */
    public static void updateRewardLore(ItemStack rod) {
        if (rod == null) return;
        int level = getLevel(rod);
        boolean newSystem = WaterCloudHookManager.isEnabled();
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());

        // 移除旧属性加成行(先剥离色码再按文字匹配)
        lore.removeIf(line -> {
            String plain = stripColorCodes(line);
            return REWARD_LINE_PREFIXES.stream().anyMatch(plain::contains);
        });

        List<String> reward = buildRewardLore(level, newSystem);
        if (!reward.isEmpty()) {
            lore.addAll(reward);
        }
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    /** 按当前系统与等级生成属性加成行(内容行魔法二代渐变, 无标题行; 无加成的等级返回空列表) */
    private static List<String> buildRewardLore(int level, boolean newSystem) {
        List<String> lines = new ArrayList<>();
        if (newSystem) {
            double bite = WaterCloudRodEffects.getNewBiteChanceBonus(level);
            double light = WaterCloudRodEffects.getNewLightBiteChanceBonus(level);
            double charge = WaterCloudRodEffects.getNewChargeSpeedBonus(level);
            double dbl = WaterCloudRodEffects.getNewDoubleCatchChance(level);
            double rare = WaterCloudRodEffects.getNewRareBonus(level);
            if (bite > 0) lines.add(ColorGradient.getGradientNameVer2("等待时间：+" + Math.round(bite * 100) + "%"));
            if (light > 0) lines.add(ColorGradient.getGradientNameVer2("轻咬时间：+" + Math.round(light * 100) + "%"));
            if (charge > 0) lines.add(ColorGradient.getGradientNameVer2("蓄力速度：+" + (int) charge + "/秒"));
            if (dbl > 0) lines.add(ColorGradient.getGradientNameVer2("双倍鱼获：" + Math.round(dbl * 100) + "%"));
            if (rare > 0) lines.add(ColorGradient.getGradientNameVer2("特殊钓物：+" + Math.round(rare * 100) + "%（满级）"));
        } else {
            int ticks = WaterCloudRodEffects.getOldHookSpeedTicks(level);
            double junk = WaterCloudRodEffects.getOldJunkReduction(level);
            double preserve = WaterCloudRodEffects.getOldLurePreserveChance(level);
            double dbl = WaterCloudRodEffects.getOldDoubleCatchChance(level);
            if (ticks > 0) lines.add(ColorGradient.getGradientNameVer2("上钩速度：+" + Math.round(ticks / 600.0 * 100) + "%"));
            if (junk > 0) lines.add(ColorGradient.getGradientNameVer2("杂物概率：-" + Math.round(junk * 100) + "%"));
            if (preserve > 0) lines.add(ColorGradient.getGradientNameVer2("省饵概率：" + Math.round(preserve * 100) + "%"));
            if (dbl > 0) lines.add(ColorGradient.getGradientNameVer2("双倍鱼获：" + Math.round(dbl * 100) + "%"));
        }
        return lines;
    }
}
