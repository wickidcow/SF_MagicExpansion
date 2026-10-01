package io.Yomicer.magicExpansion.utils.shop;

import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Plans the complete barter cost before changing a player's storage slots. */
final class InventoryPayment {
    private InventoryPayment() {}

    /**
     * Returns the unpaid remainder, or null after successful payment. Call only on the
     * inventory's owning server thread. Armour and off-hand slots are never payment sources.
     * This is not a transaction across disk storage, reward delivery or other plugins.
     */
    static ItemStack debit(Inventory inventory, List<ItemStack> costs) {
        Objects.requireNonNull(inventory, "inventory");
        ItemStack[] planned = copy(inventory.getStorageContents());
        if (costs == null || costs.isEmpty()) {
            return null;
        }

        // Reject invalid later entries before any inventory write.
        for (ItemStack cost : costs) {
            if (cost != null && !cost.getType().isAir() && cost.getAmount() <= 0) {
                throw new IllegalArgumentException("Trade costs must have a positive amount");
            }
        }

        boolean changed = false;
        for (ItemStack cost : costs) {
            if (cost == null || cost.getType().isAir()) {
                continue;
            }
            int remaining = cost.getAmount();
            for (int slot = 0; slot < planned.length && remaining > 0; slot++) {
                ItemStack available = planned[slot];
                if (available == null || available.getType().isAir() || available.getAmount() <= 0
                        || !cost.isSimilar(available)) {
                    continue;
                }
                int taken = Math.min(remaining, available.getAmount());
                remaining -= taken;
                int left = available.getAmount() - taken;
                if (left == 0) {
                    planned[slot] = null;
                } else {
                    available.setAmount(left);
                }
                changed = true;
            }
            if (remaining != 0) {
                ItemStack missing = cost.clone();
                missing.setAmount(remaining);
                return missing;
            }
        }
        if (changed) {
            inventory.setStorageContents(planned);
        }
        return null;
    }

    private static ItemStack[] copy(ItemStack[] contents) {
        ItemStack[] result = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++) {
            result[slot] = contents[slot] == null ? null : contents[slot].clone();
        }
        return result;
    }
}
