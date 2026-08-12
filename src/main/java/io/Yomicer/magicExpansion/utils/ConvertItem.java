package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public final class ConvertItem {

    private static final Set<String> ANNOUNCED_LEGACY_FALLBACKS = new HashSet<>();
    private static final Set<String> ANNOUNCED_MISSING_IDS = new HashSet<>();

    private ConvertItem() {
    }

    /**
     * Resolves Magic 1.0 IDs that are intentionally supported by the Legacy fork
     * even when the original Magic addon is not installed.
     */
    private static SlimefunItemStack getLegacyFallback(String selectedItem) {
        return switch (selectedItem) {
            case "MAGIC_REDSTONE" -> MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC;
            case "MAGIC_COSMIC_DUST" -> MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED;
            default -> null;
        };
    }

    private static String getLegacyFallbackName(String selectedItem) {
        return switch (selectedItem) {
            case "MAGIC_REDSTONE" -> "Basic Universal Magic Material";
            case "MAGIC_COSMIC_DUST" -> "Advanced Universal Magic Material";
            default -> "MagicExpansion compatibility material";
        };
    }

    private static SlimefunItemStack resolveLegacyFallback(String selectedItem) {
        SlimefunItemStack fallback = getLegacyFallback(selectedItem);
        if (fallback != null && ANNOUNCED_LEGACY_FALLBACKS.add(selectedItem)) {
            Debug.logInfo(
                    "Magic 1.0 compatibility: " + selectedItem + " -> " + getLegacyFallbackName(selectedItem)
            );
        }
        return fallback;
    }

    private static void logMissingOnce(String selectedItem, String action) {
        if (ANNOUNCED_MISSING_IDS.add(selectedItem)) {
            Debug.logInfo("Could not find a Slimefun item with ID " + selectedItem + "; " + action + ".");
        }
    }

    public static ItemStack BasicCreateItem(String selectedItem) {
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);
        if (slimefunItem != null) {
            return slimefunItem.getItem().clone();
        }

        SlimefunItemStack legacyFallback = resolveLegacyFallback(selectedItem);
        if (legacyFallback != null) {
            return legacyFallback.clone();
        }

        logMissingOnce(selectedItem, "using the basic compatibility material");
        return MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC.clone();
    }

    public static SlimefunItemStack createItem(String selectedItem) {
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);
        if (slimefunItem != null) {
            return new SlimefunItemStack(selectedItem, slimefunItem.getItem().clone());
        }

        SlimefunItemStack legacyFallback = resolveLegacyFallback(selectedItem);
        if (legacyFallback != null) {
            return legacyFallback;
        }

        logMissingOnce(selectedItem, "using the basic compatibility material");
        return MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC;
    }

    public static ItemStack AdvancedCreateItem(String selectedItem) {
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);
        if (slimefunItem != null) {
            return slimefunItem.getItem().clone();
        }

        SlimefunItemStack legacyFallback = resolveLegacyFallback(selectedItem);
        if (legacyFallback != null) {
            return legacyFallback.clone();
        }

        logMissingOnce(selectedItem, "using the advanced compatibility material");
        return MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED.clone();
    }

    public static ItemStack stoneCreateItem(String selectedItem) {
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);
        if (slimefunItem != null) {
            return slimefunItem.getItem().clone();
        }

        logMissingOnce(selectedItem, "using vanilla stone as a fallback");
        return new ItemStack(Material.STONE);
    }

    public static Boolean IfItemXist(String selectedItem) {
        if (SlimefunItem.getById(selectedItem) != null) {
            return true;
        }

        // Known Magic 1.0 IDs are valid dependencies because MagicExpansion
        // supplies equivalent compatibility materials when Magic 1.0 is absent.
        if (resolveLegacyFallback(selectedItem) != null) {
            return true;
        }

        logMissingOnce(selectedItem, "skipping registration");
        return false;
    }
}
