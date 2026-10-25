package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class WaterCloudBaitPaymentTest {

    @Test
    void successfulBagPaymentNeverAlsoChargesInventory() {
        AtomicInteger bagCalls = new AtomicInteger();
        AtomicInteger inventoryCalls = new AtomicInteger();
        var paid = WaterCloudBaitPayment.charge(true,
                () -> { bagCalls.incrementAndGet(); return true; },
                () -> { inventoryCalls.incrementAndGet(); return true; });

        assertEquals(WaterCloudBaitPayment.Source.BAG, paid);
        assertEquals(1, bagCalls.get());
        assertEquals(0, inventoryCalls.get(), "Bait must never be charged twice");
    }

    @Test
    void missingBagFallsBackToOnlyMatchingInventoryBait() {
        AtomicInteger inventoryCalls = new AtomicInteger();
        var paid = WaterCloudBaitPayment.charge(true,
                () -> false,
                () -> { inventoryCalls.incrementAndGet(); return true; });

        assertEquals(WaterCloudBaitPayment.Source.INVENTORY, paid);
        assertEquals(1, inventoryCalls.get());
    }

    @Test
    void unselectedBagIsNeverTouched() {
        AtomicInteger bagCalls = new AtomicInteger();
        var paid = WaterCloudBaitPayment.charge(false,
                () -> { bagCalls.incrementAndGet(); return true; },
                () -> true);

        assertEquals(WaterCloudBaitPayment.Source.INVENTORY, paid);
        assertEquals(0, bagCalls.get());
    }

    @Test
    void noPayableBaitCannotCreateAReward() {
        assertEquals(WaterCloudBaitPayment.Source.NONE,
                WaterCloudBaitPayment.charge(true, () -> false, () -> false));
        assertEquals(WaterCloudBaitPayment.Source.NONE,
                WaterCloudBaitPayment.charge(false, () -> true, () -> false));
    }
}
