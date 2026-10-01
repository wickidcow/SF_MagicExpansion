package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ShopPageTest {
    @Test
    void selectsTheTradeActuallyDisplayedOnEachPage() {
        assertEquals(0, ShopGUI.purchaseIndex(0, 10, 60));
        assertEquals(27, ShopGUI.purchaseIndex(0, 43, 60));
        assertEquals(28, ShopGUI.purchaseIndex(1, 10, 60));
        assertEquals(55, ShopGUI.purchaseIndex(1, 43, 60));
        assertEquals(56, ShopGUI.purchaseIndex(2, 10, 60));
        assertEquals(59, ShopGUI.purchaseIndex(2, 13, 60));
    }

    @Test
    void rejectsNavigationEmptySlotsAndOffPageIndices() {
        for (int slot : new int[] {-1, 0, 4, 9, 17, 45, 49, 53, 54, 80}) {
            assertEquals(-1, ShopGUI.purchaseIndex(0, slot, 60));
        }
        assertEquals(-1, ShopGUI.purchaseIndex(2, 14, 60));
        assertEquals(-1, ShopGUI.purchaseIndex(0, 10, 0));
    }

    @Test
    void invalidPagesCannotWrapToAnEarlierTrade() {
        assertEquals(-1, ShopGUI.purchaseIndex(-1, 10, 100));
        assertEquals(-1, ShopGUI.purchaseIndex(Integer.MAX_VALUE, 10, Integer.MAX_VALUE));
        assertEquals(-1, ShopGUI.purchaseIndex(100, 10, 100));
    }
}
