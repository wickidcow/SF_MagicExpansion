package io.Yomicer.magicExpansion.utils.compat;

import java.util.Locale;
import org.bukkit.entity.Entity;

/**
 * Local entity-name helper used to avoid a hard dependency on GuizhanLibPlugin.
 */
public final class EntityHelper {

    private EntityHelper() {}

    public static String getDisplayName(Entity entity) {
        return getName(entity);
    }

    public static String getName(Entity entity) {
        if (entity == null) {
            return "Unknown Entity";
        }

        String customName = entity.getCustomName();
        if (customName != null && !customName.isBlank()) {
            return customName;
        }

        String[] words = entity.getType().name().toLowerCase(Locale.ROOT).split("_");
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
