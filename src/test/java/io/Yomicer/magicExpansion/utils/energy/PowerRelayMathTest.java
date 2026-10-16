package io.Yomicer.magicExpansion.utils.energy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PowerRelayMathTest {

    @Test
    void fillDoesNotOverflowAtLongMaximum() {
        long start = Long.MAX_VALUE - 5;
        PowerRelayMath.Transfer transfer = PowerRelayMath.fill(start, Long.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, transfer.storedEnergy());
        assertEquals(5, transfer.transferred());
    }

    @Test
    void fillUsesOnlyAvailableMachineCharge() {
        PowerRelayMath.Transfer transfer = PowerRelayMath.fill(100, 1_000, 250);
        assertEquals(350, transfer.storedEnergy());
        assertEquals(250, transfer.transferred());
    }

    @Test
    void fillStopsExactlyAtConfiguredMaximum() {
        PowerRelayMath.Transfer transfer = PowerRelayMath.fill(950, 1_000, 250);
        assertEquals(1_000, transfer.storedEnergy());
        assertEquals(50, transfer.transferred());
    }

    @Test
    void invalidOrFullCardsDoNotAcceptEnergy() {
        assertEquals(new PowerRelayMath.Transfer(10, 0), PowerRelayMath.fill(10, 0, 100));
        assertEquals(new PowerRelayMath.Transfer(-1, 0), PowerRelayMath.fill(-1, 100, 100));
        assertEquals(new PowerRelayMath.Transfer(100, 0), PowerRelayMath.fill(100, 100, 100));
        assertEquals(new PowerRelayMath.Transfer(10, 0), PowerRelayMath.fill(10, 100, 0));
    }

    @Test
    void drainSupportsStoredEnergyAboveIntegerRange() {
        long start = (long) Integer.MAX_VALUE + 10_000L;
        PowerRelayMath.Transfer transfer = PowerRelayMath.drain(start, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, transfer.transferred());
        assertEquals(10_000L, transfer.storedEnergy());
    }

    @Test
    void drainRespectsMachineFreeSpace() {
        PowerRelayMath.Transfer transfer = PowerRelayMath.drain(5_000, 750);
        assertEquals(750, transfer.transferred());
        assertEquals(4_250, transfer.storedEnergy());
    }

    @Test
    void maximumCannotBeConfiguredBelowStoredEnergy() {
        assertTrue(PowerRelayMath.canSetMaximum(500, 500));
        assertTrue(PowerRelayMath.canSetMaximum(500, 1_000));
        assertFalse(PowerRelayMath.canSetMaximum(501, 500));
        assertFalse(PowerRelayMath.canSetMaximum(-1, 500));
        assertFalse(PowerRelayMath.canSetMaximum(0, 0));
    }
}
