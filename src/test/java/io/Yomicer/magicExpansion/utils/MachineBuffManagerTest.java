package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MachineBuffManagerTest {
    private final Location location = new Location(null, 12, 64, -8);

    @AfterEach
    void cleanup() {
        MachineBuffManager.remove(location);
    }

    @Test
    void activeBuffScalesProduction() {
        MachineBuffManager.apply(location, 300, 2.25D);
        assertEquals(225L, MachineBuffManager.scaleAmount(location, 100L));
        assertTrue(MachineBuffManager.hasBuff(location));
    }

    @Test
    void multiplierIsCappedAtLegalGen2Maximum() {
        MachineBuffManager.apply(location, 300, 99.0D);
        assertEquals(375L, MachineBuffManager.scaleAmount(location, 100L));
    }

    @Test
    void scalingSaturatesInsteadOfOverflowing() {
        MachineBuffManager.apply(location, 300, MachineBuffManager.MAX_MULTIPLIER);
        assertEquals(Long.MAX_VALUE,
                MachineBuffManager.scaleAmount(location, Long.MAX_VALUE / 2L));
    }

    @Test
    void invalidMultiplierDoesNotCreateABuff() {
        MachineBuffManager.apply(location, 300, Double.NaN);
        assertEquals(100L, MachineBuffManager.scaleAmount(location, 100L));
        assertFalse(MachineBuffManager.hasBuff(location));
    }

    @Test
    void removingBuffRestoresNormalRate() {
        MachineBuffManager.apply(location, 300, 2.0D);
        MachineBuffManager.remove(location);
        assertEquals(100L, MachineBuffManager.scaleAmount(location, 100L));
    }
}
