package io.Yomicer.magicExpansion.utils.fishing;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Resolves one Water Cloud bait charge before any replacement catch is paid out.
 * This logic has no scheduling or global state and runs on the owning player event.
 */
public final class WaterCloudBaitPayment {

    public enum Source {
        BAG, INVENTORY, NONE
    }

    private WaterCloudBaitPayment() {
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
