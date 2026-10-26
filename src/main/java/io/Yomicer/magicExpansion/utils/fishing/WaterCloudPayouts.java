package io.Yomicer.magicExpansion.utils.fishing;

import java.util.List;
import org.bukkit.inventory.ItemStack;

/**
 * Preserve entire catch stacks when a level bonus awards a second catch.
 * Do not double via setAmount(min(64, amount*2)): that loses paid items
 * above half a stack and cannot represent unstackable fish buckets.
 */
public final class WaterCloudPayouts {

    private WaterCloudPayouts() {
    }

    /** Exactly one independently cloned stack per catch, or two on a bonus. */
    public static List<ItemStack> copies(ItemStack reward, boolean doubled) {
        if (reward == null || reward.getType().isAir() || reward.getAmount() <= 0) {
            return List.of();
        }
        return doubled
                ? List.of(reward.clone(), reward.clone())
                : List.of(reward.clone());
    }
}
