package io.Yomicer.magicExpansion.utils.fishing;

import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.inventory.ItemStack;

/**
 * Safe weighted selection of one fishing payout.
 *
 * <p>Uses long cumulative weights because dozens of item entries with large
 * configured weights can overflow an int and break nextInt(total). Empty,
 * invalid or zero-weight pools return null, leaving the vanilla catch intact.
 * The loot-table templates are never mutated.
 */
public final class WaterCloudWeightedLoot {

    private WaterCloudWeightedLoot() {
    }

    public static ItemStack selectRandom(List<WeightedItem> entries) {
        long total = totalWeight(entries);
        if (total <= 0L) {
            return null;
        }
        return selectAt(entries, ThreadLocalRandom.current().nextLong(total));
    }

    /** Deterministic ticket API used by tests; returns a cloned template. */
    public static ItemStack selectAt(List<WeightedItem> entries, long ticket) {
        long total = totalWeight(entries);
        if (total <= 0L) {
            return null;
        }
        if (ticket < 0L || ticket >= total) {
            throw new IllegalArgumentException("Weighted draw outside valid range");
        }

        for (WeightedItem entry : entries) {
            if (!isValid(entry)) {
                continue;
            }
            if (ticket < entry.getWeight()) {
                return entry.getItem().clone();
            }
            ticket -= entry.getWeight();
        }
        // Impossible for a fixed, stable source list; do not fabricate a payout.
        return null;
    }

    public static long totalWeight(List<WeightedItem> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0L;
        }
        long total = 0L;
        for (WeightedItem entry : entries) {
            if (isValid(entry)) {
                total += entry.getWeight();
            }
        }
        return total;
    }

    private static boolean isValid(WeightedItem entry) {
        if (entry == null || entry.getWeight() <= 0) {
            return false;
        }
        ItemStack template = entry.getItem();
        return template != null && !template.getType().isAir() && template.getAmount() > 0;
    }
}
