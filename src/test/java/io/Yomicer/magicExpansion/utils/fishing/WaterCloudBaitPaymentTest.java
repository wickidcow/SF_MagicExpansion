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
    @Test
    void preservationNeverDeductsBaitAndRequiresTheSelectedLure() {
        AtomicInteger debits = new AtomicInteger();
        var source = WaterCloudBaitPayment.chargeOrPreserve(
                true, true, () -> true, () -> false,
                () -> { debits.incrementAndGet(); return true; },
                () -> { debits.incrementAndGet(); return true; });
        assertEquals(WaterCloudBaitPayment.Source.PRESERVED, source);
        assertEquals(0, debits.get(), "Preserved bait must never be debited");

        assertEquals(WaterCloudBaitPayment.Source.PRESERVED,
                WaterCloudBaitPayment.chargeOrPreserve(
                        true, true, () -> false, () -> true,
                        () -> { throw new AssertionError("No bag debit expected"); },
                        () -> { throw new AssertionError("No inventory debit expected"); }));

        assertEquals(WaterCloudBaitPayment.Source.NONE,
                WaterCloudBaitPayment.chargeOrPreserve(
                        true, true, () -> false, () -> false,
                        () -> { throw new AssertionError("No bag debit expected"); },
                        () -> { throw new AssertionError("No inventory debit expected"); }));
    }

    @Test
    void failedPreservationRollUsesTheOriginalExactDebit() {
        AtomicInteger bagDebits = new AtomicInteger();
        AtomicInteger invDebits = new AtomicInteger();
        var source = WaterCloudBaitPayment.chargeOrPreserve(false, true,
                () -> { throw new AssertionError("Do not probe availability for a normal debit"); },
                () -> { throw new AssertionError("Do not probe availability for a normal debit"); },
                () -> { bagDebits.incrementAndGet(); return false; },
                () -> { invDebits.incrementAndGet(); return true; });
        assertEquals(WaterCloudBaitPayment.Source.INVENTORY, source);
        assertEquals(1, bagDebits.get());
        assertEquals(1, invDebits.get());
    }

}
