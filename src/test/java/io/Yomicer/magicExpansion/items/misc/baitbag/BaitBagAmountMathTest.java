package io.Yomicer.magicExpansion.items.misc.baitbag;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BaitBagAmountMathTest {

    @Test
    void ordinaryDepositIsAcceptedExactly() {
        var result = BaitBagAmountMath.deposit(100L, 64);
        assertEquals(164L, result.stored());
        assertEquals(64, result.accepted());
        assertEquals(0, result.remainder());
    }

    @Test
    void nearLongMaximumReturnsTheUnacceptedRemainder() {
        var result = BaitBagAmountMath.deposit(Long.MAX_VALUE - 3L, 10);
        assertEquals(Long.MAX_VALUE, result.stored());
        assertEquals(3, result.accepted());
        assertEquals(7, result.remainder());
    }

    @Test
    void fullBagDoesNotConsumeTheOfferedStack() {
        var result = BaitBagAmountMath.deposit(Long.MAX_VALUE, 64);
        assertEquals(Long.MAX_VALUE, result.stored());
        assertEquals(0, result.accepted());
        assertEquals(64, result.remainder());
    }

    @Test
    void legacyNegativeStoredCountsAreTreatedAsZeroForNewDeposits() {
        var result = BaitBagAmountMath.deposit(-50L, 8);
        assertEquals(8L, result.stored());
        assertEquals(8, result.accepted());
        assertEquals(0, result.remainder());
    }

    @Test
    void nonpositiveOffersDoNothing() {
        assertEquals(0, BaitBagAmountMath.deposit(10L, 0).accepted());
        assertEquals(0, BaitBagAmountMath.deposit(10L, -1).accepted());
    }
}
