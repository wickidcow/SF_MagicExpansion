package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Water Cloud rod proficiency is stored on the rod itself so existing players,
 * inventories, and Slimefun item IDs remain compatible.
 */
public final class WaterCloudRodProficiency {

    public static final NamespacedKey LV_KEY = new NamespacedKey(MagicExpansion.getInstance(), "watercloud_lv");
    public static final NamespacedKey XP_KEY = new NamespacedKey(MagicExpansion.getInstance(), "watercloud_xp");

    public static final int MAX_LEVEL = 8;
    public static final int[] REQUIREMENTS = {300, 600, 1200, 2200, 3200, 5200, 7200};
    public static final String[] LEVEL_NAMES = {
            "First Reflection", "At the Water's Edge", "Knowing the Current", "Wind on the Rod",
            "Moon in Hand", "Walking the Waves", "Spirit of the Waters", "One Rod, One World"
    };
    public static final String[] LEVEL_UP_MESSAGES = {
            "The rod awakens; something stirs between water and cloud.",
            "The rod hums softly, as if the river has answered.",
            "Your timing sharpens and the current feels familiar.",
            "The rod rises with the wind; every cast feels lighter.",
            "Moonlight gathers on the line and the rod grows more responsive.",
            "You move with the water instead of against it.",
            "River and rod answer one another as if alive.",
            "One rod, one world—the Water Cloud path is mastered."
    };

    public static final int XP_COMMON = 1;
    public static final int XP_RARE = 2;
    public static final int XP_SPECIAL = 5;
    public static final int ACTION_BAR_LENGTH = 40;
    public static final int LORE_BAR_LENGTH = 24;

    private static final List<String> REWARD_LINE_PREFIXES = List.of(
            "Fishing provider:", "Hook speed:", "Junk chance:", "Lure preservation:",
            "Double catch:", "Magic bonus catch:", "Bite chance:", "Light-bite chance:",
            "Charge speed:", "Special catch:"
    );

    private WaterCloudRodProficiency() {
    }

    public static int getLevel(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) {
            return 1;
        }
        Integer level = rod.getItemMeta().getPersistentDataContainer().get(LV_KEY, PersistentDataType.INTEGER);
        return level == null ? 1 : Math.max(1, Math.min(MAX_LEVEL, level));
    }

    public static int getXp(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) {
            return 0;
        }
        Integer xp = rod.getItemMeta().getPersistentDataContainer().get(XP_KEY, PersistentDataType.INTEGER);
        return xp == null ? 0 : Math.max(0, xp);
    }

    public static int getRequirement(int level) {
        if (level < 1) {
            return REQUIREMENTS[0];
        }
        if (level >= MAX_LEVEL) {
            return 0;
        }
        return REQUIREMENTS[level - 1];
    }

    public static String getLevelName(int level) {
        return LEVEL_NAMES[Math.max(1, Math.min(MAX_LEVEL, level)) - 1];
    }

    public static int addProficiency(ItemStack rod, int amount) {
        if (rod == null || amount <= 0) {
            return getLevel(rod);
        }

        ItemMeta meta = rod.getItemMeta();
        if (meta == null) {
            return 1;
        }

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

    public static void updateLoreWithReward(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) {
            return;
        }

        int level = getLevel(rod);
        int xp = getXp(rod);
        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());

        lore.removeIf(line -> {
            String plain = stripColorCodes(line);
            return plain.contains("Water Cloud Proficiency:")
                    || REWARD_LINE_PREFIXES.stream().anyMatch(plain::contains);
        });

        lore.add("§bWater Cloud Proficiency: §f" + getLevelName(level) + " §e" + getProgressBar(level, xp));
        lore.addAll(buildRewardLore(level));
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    public static void updateRewardLore(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) {
            return;
        }

        ItemMeta meta = rod.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        lore.removeIf(line -> {
            String plain = stripColorCodes(line);
            return REWARD_LINE_PREFIXES.stream().anyMatch(plain::contains);
        });
        lore.addAll(buildRewardLore(getLevel(rod)));
        meta.setLore(lore);
        rod.setItemMeta(meta);
    }

    public static String getActionBarProgress(ItemStack rod) {
        int level = getLevel(rod);
        int xp = getXp(rod);
        if (level >= MAX_LEVEL) {
            return "§bWater Cloud §f" + getLevelName(level) + " §dMAX";
        }
        return "§bWater Cloud §f" + getLevelName(level) + " §7" + buildPipeBar(ACTION_BAR_LENGTH, level, xp);
    }

    private static List<String> buildRewardLore(int level) {
        List<String> lines = new ArrayList<>();

        if (FishingIntegrationManager.isExternalProviderActive()) {
            lines.add("§7Fishing provider: §f" + FishingIntegrationManager.getPrimaryProvider().getDisplayName());
            double preserve = WaterCloudRodEffects.getOldLurePreserveChance(level);
            double bonus = WaterCloudRodEffects.getOldDoubleCatchChance(level);
            if (preserve > 0) {
                lines.add("§bLure preservation: §f" + Math.round(preserve * 100) + "%");
            }
            if (bonus > 0) {
                lines.add("§dMagic bonus catch: §f" + Math.round(bonus * 100) + "%");
            }
            return lines;
        }

        if (WaterCloudHookManager.isEnabled()) {
            double bite = WaterCloudRodEffects.getNewBiteChanceBonus(level);
            double light = WaterCloudRodEffects.getNewLightBiteChanceBonus(level);
            double charge = WaterCloudRodEffects.getNewChargeSpeedBonus(level);
            double dbl = WaterCloudRodEffects.getNewDoubleCatchChance(level);
            double rare = WaterCloudRodEffects.getNewRareBonus(level);
            if (bite > 0) lines.add("§bBite chance: §f+" + Math.round(bite * 100) + "%");
            if (light > 0) lines.add("§bLight-bite chance: §f+" + Math.round(light * 100) + "%");
            if (charge > 0) lines.add("§bCharge speed: §f+" + charge + "/sec");
            if (dbl > 0) lines.add("§dDouble catch: §f" + Math.round(dbl * 100) + "%");
            if (rare > 0) lines.add("§dSpecial catch: §f+" + Math.round(rare * 100) + "%");
        } else {
            int ticks = WaterCloudRodEffects.getOldHookSpeedTicks(level);
            double junk = WaterCloudRodEffects.getOldJunkReduction(level);
            double preserve = WaterCloudRodEffects.getOldLurePreserveChance(level);
            double dbl = WaterCloudRodEffects.getOldDoubleCatchChance(level);
            if (ticks > 0) lines.add("§bHook speed: §f+" + Math.round(ticks / 600.0 * 100) + "%");
            if (junk > 0) lines.add("§bJunk chance: §f-" + Math.round(junk * 100) + "%");
            if (preserve > 0) lines.add("§bLure preservation: §f" + Math.round(preserve * 100) + "%");
            if (dbl > 0) lines.add("§dDouble catch: §f" + Math.round(dbl * 100) + "%");
        }

        return lines;
    }

    private static String getProgressBar(int level, int xp) {
        int need = getRequirement(level);
        if (need <= 0) {
            return "§dMAX";
        }
        return buildPipeBar(LORE_BAR_LENGTH, level, xp) + " §7(" + xp + "/" + need + ")";
    }

    private static String buildPipeBar(int length, int level, int xp) {
        int need = getRequirement(level);
        if (need <= 0) {
            return "§d" + "|".repeat(length);
        }

        int filled = Math.max(0, Math.min(length, (int) Math.round(xp * (double) length / need)));
        StringBuilder builder = new StringBuilder();
        if (filled > 0) {
            builder.append("§b").append("|".repeat(filled));
        }
        if (filled < length) {
            builder.append("§7").append("|".repeat(length - filled));
        }
        return builder.toString();
    }

    private static String stripColorCodes(String line) {
        return line.replace("§x", "").replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
