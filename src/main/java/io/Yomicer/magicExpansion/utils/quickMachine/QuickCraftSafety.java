package io.Yomicer.magicExpansion.utils.quickMachine;

import java.util.Map;

/**
 * Shared fail-closed arithmetic and availability checks for MagicExpansion quick crafting.
 */
public final class QuickCraftSafety {
    private QuickCraftSafety() {}

    public static boolean hasEnoughMaterials(
            Map<String, Integer> availableItems,
            Map<String, Integer> recipe,
            int crafts) {
        if (availableItems == null || recipe == null || recipe.isEmpty() || crafts <= 0) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            String key = entry.getKey();
            Integer perCraft = entry.getValue();
            if (key == null || key.isBlank() || perCraft == null || perCraft <= 0) {
                return false;
            }

            long required = (long) perCraft * crafts;
            if (required <= 0L || required > Integer.MAX_VALUE) {
                return false;
            }

            int available = Math.max(0, availableItems.getOrDefault(key, 0));
            if ((long) available < required) {
                return false;
            }
        }

        return true;
    }

    public static int calculateMaxCraftableAmount(
            Map<String, Integer> availableItems,
            Map<String, Integer> recipe) {
        if (availableItems == null || recipe == null || recipe.isEmpty()) {
            return 0;
        }

        int maxCraftable = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            String key = entry.getKey();
            Integer perCraft = entry.getValue();
            if (key == null || key.isBlank() || perCraft == null || perCraft <= 0) {
                return 0;
            }

            int available = Math.max(0, availableItems.getOrDefault(key, 0));
            maxCraftable = Math.min(maxCraftable, available / perCraft);
            if (maxCraftable == 0) {
                return 0;
            }
        }

        return maxCraftable == Integer.MAX_VALUE ? 0 : maxCraftable;
    }

    public static int totalRequiredAmount(int perCraft, int crafts) {
        if (perCraft <= 0 || crafts <= 0) {
            return -1;
        }

        long total = (long) perCraft * crafts;
        return total > Integer.MAX_VALUE ? -1 : (int) total;
    }
}
