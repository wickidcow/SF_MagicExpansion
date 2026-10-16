package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VivariumOutputMathTest {

    @Test
    void zeroInstalledVivariumsProduceNothing() {
        assertEquals(0L, VivariumOutputMath.calculate(21.0D, 9999, 888, 0L, 0L));
    }

    @Test
    void normalAndEasyVivariumsAreCombinedExactly() {
        // Normal: floor(2.5*7)=17 each x3 = 51.
        // Easy: floor(2.5*3)=7 each x4 = 28.
        assertEquals(79L, VivariumOutputMath.calculate(2.5D, 7, 3, 3L, 4L));
    }

    @Test
    void smallPositiveWeightsStillProduceOnePerInstalledMachine() {
        assertEquals(5L, VivariumOutputMath.calculate(0.01D, 1, 1, 2L, 3L));
    }

    @Test
    void productionCanExceedIntegerMaximumWithoutBeingTruncated() {
        long result = VivariumOutputMath.calculate(
                21.0D, 9999, 888, 100_000L, 100_000L);
        assertTrue(result > Integer.MAX_VALUE);
        assertEquals(22_862_700_000L, result);
    }

    @Test
    void multiplicationAndAdditionSaturateAtLongMaximum() {
        assertEquals(Long.MAX_VALUE, VivariumOutputMath.safeMultiply(Long.MAX_VALUE, 2L));
        assertEquals(Long.MAX_VALUE, VivariumOutputMath.safeAdd(Long.MAX_VALUE - 1L, 2L));
        assertEquals(Long.MAX_VALUE,
                VivariumOutputMath.calculate(Double.MAX_VALUE, Integer.MAX_VALUE, 1,
                        Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    void invalidInputsFailClosed() {
        assertEquals(0L, VivariumOutputMath.calculate(Double.NaN, 1, 1, 1, 1));
        assertEquals(0L, VivariumOutputMath.calculate(-1.0D, 1, 1, 1, 1));
        assertEquals(0L, VivariumOutputMath.calculate(1.0D, 0, 1, 1, 1));
        assertEquals(0L, VivariumOutputMath.calculate(1.0D, 1, 1, -1, 1));
    }
}
