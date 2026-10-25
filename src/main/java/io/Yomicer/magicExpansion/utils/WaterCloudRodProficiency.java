package io.Yomicer.magicExpansion.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Item-bound Water Cloud rod progression, ported from Yomicer's fishing system.
 *
 * <p>Uses the original magicexpansion PDC names so already-levelled rods retain
 * their progress. No player-wide state or recurring tasks are required.
 */
public final class WaterCloudRodProficiency {

    public static final int MAX_LEVEL = 8;
    public static final int XP_COMMON = 1;
    public static final int XP_SPECIAL = 5;

    private static final int[] REQUIREMENTS = {300, 600, 1200, 2200, 3200, 5200, 7200};
    private static final Set<String> ELIGIBLE_RODS = Set.of(
            "FISHING_ROD_BETWEEN_WATER_CLOUD_REED",
            "FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG",
            "FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU"
    );
    private static final String[] LEVEL_NAMES = {
            "First Ripple", "Watching the River", "Knowing the Waters",
            "Rising Current", "Moonlit Waters", "Wavewalker",
            "Spirit of the River", "One with the Waters"
    };
    private static final String[] LEVEL_MESSAGES = {
            "The rod stirs, and the river answers.",
            "The current begins to reveal its secrets.",
            "You have learned to read the water.",
            "The wind rises to meet your line.",
            "Moonlight gathers around your rod.",
            "Every wave brings a new possibility.",
            "The river's spirit recognizes your skill.",
            "You and the waters have become one."
    };

    // Same namespace and keys as upstream; no runtime-only metadata or new Slimefun IDs.
    public static final NamespacedKey LV_KEY =
            Objects.requireNonNull(NamespacedKey.fromString("magicexpansion:watercloud_lv"));
    public static final NamespacedKey XP_KEY =
            Objects.requireNonNull(NamespacedKey.fromString("magicexpansion:watercloud_xp"));

    private static final String LORE_PREFIX = "Water Cloud Proficiency:";

    private WaterCloudRodProficiency() {
    }

    public record Progress(int level, int xp) {
    }

    /** The initial Cyan Bamboo rod deliberately does not level up, as upstream intended. */
    public static boolean supportsRodId(String id) {
        if (id == null) {
            return false;
        }
        return ELIGIBLE_RODS.contains(id)
                || ELIGIBLE_RODS.stream().anyMatch(rodId -> id.endsWith("_" + rodId));
    }

    public static Progress read(ItemStack rod) {
        if (rod == null || !rod.hasItemMeta()) {
            return new Progress(1, 0);
        }
        PersistentDataContainer data = rod.getItemMeta().getPersistentDataContainer();
        int level = data.getOrDefault(LV_KEY, PersistentDataType.INTEGER, 1);
        int xp = data.getOrDefault(XP_KEY, PersistentDataType.INTEGER, 0);
        return new Progress(clampLevel(level), Math.max(0, xp));
    }

    public static int getLevel(ItemStack rod) {
        return read(rod).level();
    }

    public static int getRequirement(int level) {
        return level >= MAX_LEVEL ? 0 : REQUIREMENTS[Math.max(1, level) - 1];
    }

    public static String getLevelName(int level) {
        return LEVEL_NAMES[clampLevel(level) - 1];
    }

    public static String getLevelMessage(int level) {
        return LEVEL_MESSAGES[clampLevel(level) - 1];
    }

    /**
     * Pure, overflow-safe advancement function; XP from one catch can cross levels.
     */
    public static Progress advance(int previousLevel, int previousXp, int amount) {
        int level = clampLevel(previousLevel);
        if (level >= MAX_LEVEL) {
            return new Progress(MAX_LEVEL, 0);
        }

        long xp = (long) Math.max(0, previousXp) + Math.max(0, amount);
        while (level < MAX_LEVEL && xp >= REQUIREMENTS[level - 1]) {
            xp -= REQUIREMENTS[level - 1];
            level++;
        }

        return level == MAX_LEVEL
                ? new Progress(MAX_LEVEL, 0)
                : new Progress(level, (int) Math.min(xp, Integer.MAX_VALUE));
    }

    /**
     * Update the held rod's original PDC and only its proficiency lore line.
     * All other metadata, item IDs, enchantments, and lore are preserved.
     */
    public static Progress award(ItemStack rod, int amount) {
        Progress previous = read(rod);
        if (rod == null || amount <= 0) {
            return previous;
        }
        ItemMeta meta = rod.getItemMeta();
        if (meta == null) {
            return previous;
        }
        Progress updated = advance(previous.level(), previous.xp(), amount);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(LV_KEY, PersistentDataType.INTEGER, updated.level());
        data.set(XP_KEY, PersistentDataType.INTEGER, updated.xp());

        List<String> lore = meta.getLore() == null
                ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        lore.removeIf(line -> line != null && isProficiencyLine(line));
        lore.add("§b" + LORE_PREFIX + " §f" + getLevelName(updated.level())
                + " §7(" + (updated.level() == MAX_LEVEL
                ? "MAX" : updated.xp() + "/" + getRequirement(updated.level())) + ")");
        meta.setLore(lore);
        rod.setItemMeta(meta);
        return updated;
    }

    private static boolean isProficiencyLine(String line) {
        // §x hex gradients and ordinary legacy color codes must not hide stale lore.
        String plain = line.replaceAll("(?i)§[0-9a-fk-orx]", "");
        return plain.startsWith(LORE_PREFIX) || plain.contains("熟练度");
    }

    private static int clampLevel(int level) {
        return Math.max(1, Math.min(MAX_LEVEL, level));
    }
}
