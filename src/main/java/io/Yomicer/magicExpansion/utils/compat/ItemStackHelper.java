package io.Yomicer.magicExpansion.utils.compat;

import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Small local replacement for the display-name methods that were previously
 * supplied by GuizhanLibPlugin.
 */
public final class ItemStackHelper {

    private ItemStackHelper() {}

    public static String getDisplayName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Air";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }

        return humanize(item.getType());
    }

    public static String getName(ItemStack item) {
        return getDisplayName(item);
    }

    private static String humanize(Material material) {
        String[] words = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
