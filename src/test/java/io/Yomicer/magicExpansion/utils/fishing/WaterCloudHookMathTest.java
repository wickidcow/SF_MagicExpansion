package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Numeric parity and fail-closed behavior before opt-in event integration. */
class WaterCloudHookMathTest {

    @Test
    void waitingBiteChanceMatchesTheUpstreamLevelBonuses() {
        assertEquals(WaterCloudHookMath.Stage.LIGHT_BITE,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.WAITING, 1, 1, 0.0999).stage());
        assertEquals(WaterCloudHookMath.Stage.WAITING,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.WAITING, 1, 1, 0.1000).stage());
        assertEquals(WaterCloudHookMath.Stage.LIGHT_BITE,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.WAITING, 1, 8, 0.1749).stage());
        assertEquals(WaterCloudHookMath.Stage.WAITING,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.WAITING, 1, 8, 0.1750).stage());
    }

    @Test
    void lightBiteUpgradesOrExpiresAfterEightSecondsExactlyOnce() {
        var result = WaterCloudHookMath.advance(WaterCloudHookMath.Stage.LIGHT_BITE, 2, 1, 0.19);
        assertEquals(WaterCloudHookMath.Stage.FULL_BITE, result.stage());
        assertFalse(result.escaped());
        result = WaterCloudHookMath.advance(WaterCloudHookMath.Stage.LIGHT_BITE, 7, 1, 0.99);
        assertEquals(WaterCloudHookMath.Stage.LIGHT_BITE, result.stage());
        result = WaterCloudHookMath.advance(WaterCloudHookMath.Stage.LIGHT_BITE, 8, 1, 0.99);
        assertEquals(WaterCloudHookMath.Stage.WAITING, result.stage());
        assertTrue(result.escaped());
        assertEquals(WaterCloudHookMath.Stage.FULL_BITE,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.LIGHT_BITE, 1, 8, 0.2799).stage());
        assertEquals(WaterCloudHookMath.Stage.LIGHT_BITE,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.LIGHT_BITE, 1, 8, 0.2800).stage());
    }

    @Test
    void fullBiteReelRatesAndChargeClampsCannotGiveOutOfRangeChances() {
        assertEquals(0.01, WaterCloudHookMath.reelCatchChance(WaterCloudHookMath.Stage.WAITING, 100), 0.00001);
        assertEquals(0.40, WaterCloudHookMath.reelCatchChance(WaterCloudHookMath.Stage.LIGHT_BITE, 100), 0.00001);
        assertEquals(0.10, WaterCloudHookMath.reelCatchChance(WaterCloudHookMath.Stage.FULL_BITE, 0), 0.00001);
        assertEquals(0.55, WaterCloudHookMath.reelCatchChance(WaterCloudHookMath.Stage.FULL_BITE, 50), 0.00001);
        assertEquals(1.0, WaterCloudHookMath.reelCatchChance(WaterCloudHookMath.Stage.FULL_BITE, 100), 0.00001);
        assertEquals(1.0, WaterCloudHookMath.reelCatchChance(WaterCloudHookMath.Stage.FULL_BITE, Double.POSITIVE_INFINITY), 0.00001,
                "Non-finite charge should not award a valid 100% catch");
    }

    @Test
    void aimChangesChargeOnlyInsideTheOriginalFifteenDegreeWindow() {
        assertEquals(1.25, WaterCloudHookMath.nextCharge(0, 15, 1), 0.00001);
        assertEquals(1.25, WaterCloudHookMath.nextCharge(0, -15, 1), 0.00001);
        assertEquals(0.0, WaterCloudHookMath.nextCharge(0, 15.001, 8), 0.00001);
        assertEquals(1.40, WaterCloudHookMath.nextCharge(0, 0, 8), 0.00001);
        assertEquals(100, WaterCloudHookMath.nextCharge(99.9, 0, 8), 0.00001);
        assertEquals(0, WaterCloudHookMath.nextCharge(Double.NaN, Double.NaN, 8), 0.00001);
    }

    @Test
    void invalidRollsAndElapsedLimitsFailClosedWithoutExtraRewards() {
        assertEquals(WaterCloudHookMath.Stage.WAITING,
                WaterCloudHookMath.advance(WaterCloudHookMath.Stage.WAITING, 2, 8, Double.NaN).stage());
        var escaped = WaterCloudHookMath.advance(
                WaterCloudHookMath.Stage.LIGHT_BITE, 8, 8, Double.NaN);
        assertTrue(escaped.escaped(), "Invalid sample must not bypass timeout");
        assertFalse(WaterCloudHookMath.sessionExpired(599_999L));
        assertTrue(WaterCloudHookMath.sessionExpired(600_000L));
        assertFalse(WaterCloudHookMath.fullBiteExpired(29_999L, 20.0));
        assertTrue(WaterCloudHookMath.fullBiteExpired(30_000L, 20.0));
        assertTrue(WaterCloudHookMath.fullBiteExpired(0L, 20.1));
        assertTrue(WaterCloudHookMath.fullBiteExpired(0L, Double.NaN));
        assertTrue(WaterCloudHookMath.fullBiteExpired(0L, -1));
    }
}
