package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class NetworkStorageSafetyTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void longIncreaseCannotOverflowNearLongMaximum() {
        NetworkStorage.QuantumCache cache = new NetworkStorage.QuantumCache(
                new ItemStack(Material.STONE),
                Long.MAX_VALUE - 5L,
                Long.MAX_VALUE,
                false,
                true);

        long leftover = cache.increaseAmount(10L);

        assertEquals(Long.MAX_VALUE, cache.getAmountLong());
        assertEquals(5L, leftover);
    }

    @Test
    void integerIncreaseCannotOverflowNearLongMaximum() {
        NetworkStorage.QuantumCache cache = new NetworkStorage.QuantumCache(
                new ItemStack(Material.STONE),
                Long.MAX_VALUE - 2L,
                Long.MAX_VALUE,
                false,
                true);

        int leftover = cache.increaseAmount(10);

        assertEquals(Long.MAX_VALUE, cache.getAmountLong());
        assertEquals(8, leftover);
    }

    @Test
    void voidExcessConsumesOverflowWithoutExceedingLimit() {
        NetworkStorage.QuantumCache cache = new NetworkStorage.QuantumCache(
                new ItemStack(Material.STONE),
                95L,
                100L,
                true,
                true);

        assertEquals(0L, cache.increaseAmount(50L));
        assertEquals(100L, cache.getAmountLong());
    }

    @Test
    void fullNonVoidStorageReturnsTheWholeRemainder() {
        NetworkStorage.QuantumCache cache = new NetworkStorage.QuantumCache(
                new ItemStack(Material.STONE),
                100L,
                100L,
                false,
                true);

        assertEquals(50L, cache.increaseAmount(50L));
        assertEquals(100L, cache.getAmountLong());
    }

    @Test
    void negativeOrZeroIncreasesDoNotMutateStorage() {
        NetworkStorage.QuantumCache cache = new NetworkStorage.QuantumCache(
                new ItemStack(Material.STONE),
                10L,
                100L,
                false,
                true);

        assertEquals(0L, cache.increaseAmount(0L));
        assertEquals(0L, cache.increaseAmount(-10L));
        assertEquals(10L, cache.getAmountLong());
    }
}
