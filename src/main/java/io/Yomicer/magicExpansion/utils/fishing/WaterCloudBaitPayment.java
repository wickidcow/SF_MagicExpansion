package io.Yomicer.magicExpansion.utils.fishing;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Resolves one Water Cloud bait charge before any replacement catch is paid out.
 * This logic has no scheduling or global state and runs on the owning player event.
 */
public final class WaterCloudBaitPayment {

    public enum Source {
        BAG, INVENTORY, PRESERVED, NONE
    }

    private WaterCloudBaitPayment() {
    }

    /**
     * Preserve one bait only after checking that the SAME selected bait still
     * exists in either a matching bag or the player's inventory. Even a
     * successful preservation roll cannot grant a catch after bait vanishes.
     *
     * <p>Otherwise debit exactly one via the legacy fail-closed transaction.
     * All suppliers are invoked synchronously on the fishing owner thread.
     */
    public static Source chargeOrPreserve(
            boolean preservationRolled,
            boolean bagSelected,
            BooleanSupplier bagAvailable,
            BooleanSupplier inventoryAvailable,
            BooleanSupplier bagDebit,
            BooleanSupplier inventoryDebit) {
        Objects.requireNonNull(bagAvailable, "bagAvailable");
        Objects.requireNonNull(inventoryAvailable, "inventoryAvailable");
        if (preservationRolled) {
            if ((bagSelected && bagAvailable.getAsBoolean())
                    || inventoryAvailable.getAsBoolean()) {
                return Source.PRESERVED;
            }
            return Source.NONE;
        }
        return charge(bagSelected, bagDebit, inventoryDebit);
    }

    public static Source charge(boolean bagSelected, BooleanSupplier bagDebit,
                                BooleanSupplier inventoryDebit) {
        Objects.requireNonNull(bagDebit, "bagDebit");
        Objects.requireNonNull(inventoryDebit, "inventoryDebit");

        if (bagSelected && bagDebit.getAsBoolean()) {
            return Source.BAG;
        }
        if (inventoryDebit.getAsBoolean()) {
            return Source.INVENTORY;
        }
        return Source.NONE;
    }
}
