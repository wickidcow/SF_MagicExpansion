package io.Yomicer.magicExpansion.Listener.fishingListener;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests numeric parity separately from server-owned fish hook mutations. */
class WaterCloudClassicHookSpeedTest {

    @Test
    void matchesUpstreamHookSpeedTableOnOriginalSixHundredTickCap() {
        int[] expected = {600, 540, 540, 480, 480, 420, 420, 360};
        for (int level = 1; level <= 8; level++) {
            assertEquals(expected[level - 1],
                    WaterCloudClassicHookSpeed.cappedWaitTicks(600, level),
                    "Unexpected classic hook wait cap at level " + level);
        }
    }

    @Test
    void neverLengthensShorterConfiguredOrThirdPartyCap() {
        assertEquals(240, WaterCloudClassicHookSpeed.cappedWaitTicks(240, 8));
        assertEquals(100, WaterCloudClassicHookSpeed.cappedWaitTicks(100, 8));
        assertEquals(599, WaterCloudClassicHookSpeed.cappedWaitTicks(599, 1));
        assertEquals(0, WaterCloudClassicHookSpeed.cappedWaitTicks(0, 8));
        assertEquals(-1, WaterCloudClassicHookSpeed.cappedWaitTicks(-1, 8));
    }

    @Test
    void outOfRangeLevelsCannotCreateOversizedBonuses() {
        assertEquals(600,
                WaterCloudClassicHookSpeed.cappedWaitTicks(600, Integer.MIN_VALUE));
        assertEquals(360,
                WaterCloudClassicHookSpeed.cappedWaitTicks(600, Integer.MAX_VALUE));
    }
}
