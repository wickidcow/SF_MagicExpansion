package io.Yomicer.magicExpansion.items.abstracts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class AbstractMachineInventoryLayoutTest {

    @Test
    void miningRobotOutputSlotsRequireSixRows() {
        int[] outputSlots = IntStream.rangeClosed(18, 53).toArray();

        assertFalse(AbstractMachine.slotsWithinMenu(18, outputSlots));
        assertFalse(AbstractMachine.slotsWithinMenu(53, outputSlots));
        assertTrue(AbstractMachine.slotsWithinMenu(54, outputSlots));
    }

    @Test
    void invalidSlotsCannotReachThePhysicalInventory() {
        assertFalse(AbstractMachine.slotsWithinMenu(54, -1));
        assertFalse(AbstractMachine.slotsWithinMenu(54, 54));
        assertFalse(AbstractMachine.slotsWithinMenu(0, 0));
        assertTrue(AbstractMachine.slotsWithinMenu(18, 0, 13, 17));
        assertTrue(AbstractMachine.slotsWithinMenu(54));
    }
}
