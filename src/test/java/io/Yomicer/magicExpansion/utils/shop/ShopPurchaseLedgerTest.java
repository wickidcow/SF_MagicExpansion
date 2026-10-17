package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ShopPurchaseLedgerTest {
    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();
    private ShopManager.Trade trade() {
        var trade = new ShopManager.Trade();
        trade.personalLimit = 1;
        return trade;
    }
    @Test void serializerSeesReceiptBeforeCommitReportsSuccess() {
        var trade = trade();
        assertTrue(ShopPurchaseLedger.commit(alice, trade, () -> {
            assertEquals(1, trade.personalUsed.get(alice));
            assertEquals(0, trade.globalUsed);
            return true;
        }));
        assertEquals(1, trade.personalUsed.get(alice));
    }
    @Test void refusedSaveRestoresOriginalMapAndAllCounters() {
        var trade = trade(); trade.globalLimit = 9; trade.globalUsed = 3;
        trade.personalUsed.put(bob, 1); var original = trade.personalUsed;
        assertFalse(ShopPurchaseLedger.commit(alice, trade, () -> {
            assertEquals(4, trade.globalUsed); assertEquals(1, trade.personalUsed.get(alice));
            return false;
        }));
        assertEquals(3, trade.globalUsed); assertSame(original, trade.personalUsed);
        assertEquals(Map.of(bob, 1), trade.personalUsed);
    }
    @Test void throwingSaveAlsoRollsBackWithoutSwallowingItsFailure() {
        var trade = trade(); trade.globalLimit = 2; var original = trade.personalUsed;
        RuntimeException failure = new IllegalStateException("simulated save exception");
        assertSame(failure, assertThrows(RuntimeException.class,
                () -> ShopPurchaseLedger.commit(alice, trade, () -> { throw failure; })));
        assertSame(original, trade.personalUsed); assertTrue(trade.personalUsed.isEmpty()); assertEquals(0, trade.globalUsed);
    }
    @Test void duplicateDoesNotCallSerializerOrChangeCounters() {
        var trade = trade(); trade.personalUsed.put(alice, 1); var original = trade.personalUsed;
        assertFalse(ShopPurchaseLedger.commit(alice, trade, () -> { fail("Duplicate must not save"); return true; }));
        assertSame(original, trade.personalUsed); assertEquals(Map.of(alice,1), trade.personalUsed);
    }
    @Test void eachPlayerGetsAnIndependentAllowance() {
        var trade = trade();
        assertTrue(ShopPurchaseLedger.commit(alice, trade, () -> true));
        assertTrue(ShopPurchaseLedger.commit(bob, trade, () -> true));
        assertEquals(Map.of(alice,1,bob,1), trade.personalUsed);
    }
    @Test void failedSaveDoesNotSpendThePlayersAllowance() {
        var trade = trade();
        assertFalse(ShopPurchaseLedger.commit(alice, trade, () -> false));
        assertTrue(ShopPurchaseLedger.commit(alice, trade, () -> true));
        assertEquals(Map.of(alice,1), trade.personalUsed);
    }
    @Test void sharedGlobalCapRemainsEnforced() {
        var trade = trade(); trade.globalLimit = 1;
        assertTrue(ShopPurchaseLedger.commit(alice, trade, () -> true));
        assertFalse(ShopPurchaseLedger.commit(bob, trade, () -> { fail("Global limit must not save"); return true; }));
        assertEquals(1, trade.globalUsed); assertEquals(Map.of(alice,1),trade.personalUsed);
    }
    @Test void unlimitedLimitsDoNotInventUsageCounters() {
        var trade = trade(); trade.personalLimit=0; trade.globalLimit=0;
        trade.globalUsed=7; trade.personalUsed.put(bob,4); Map<UUID,Integer> before=new HashMap<>(trade.personalUsed);
        assertTrue(ShopPurchaseLedger.commit(alice, trade, () -> true));
        assertEquals(7,trade.globalUsed); assertEquals(before,trade.personalUsed);
    }
    @Test void maximumLimitsDoNotOverflowOnFinalAllowedPurchase() {
        var trade=trade();trade.globalLimit=Integer.MAX_VALUE;trade.globalUsed=Integer.MAX_VALUE-1;
        trade.personalLimit=Integer.MAX_VALUE;trade.personalUsed.put(alice,Integer.MAX_VALUE-1);
        assertTrue(ShopPurchaseLedger.commit(alice,trade,()->true));
        assertEquals(Integer.MAX_VALUE,trade.globalUsed);assertEquals(Integer.MAX_VALUE,trade.personalUsed.get(alice));
        assertFalse(ShopPurchaseLedger.commit(alice,trade,()->{fail("No overflow purchase");return true;}));
    }
    @Test void nullInputsCannotStartSerialization() {
        var called=new AtomicBoolean();var trade=trade();
        assertThrows(NullPointerException.class,()->ShopPurchaseLedger.commit(null,trade,()->{called.set(true);return true;}));
        assertFalse(called.get());assertTrue(trade.personalUsed.isEmpty());
    }
}
