package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

/** Zero-length trajectories must not invalidate a paid Water Cloud catch. */
class WaterCloudPayoutVelocityTest {

    @Test
    void regularCatchesKeepOriginalTwoPointFiveSpeedAndDirection() {
        Location angler = new Location(null, 10, 64, 10);
        Location hook = new Location(null, 14, 63, 7);
        Vector velocity = WaterCloudPayoutVelocity.towardsAngler(angler, hook);
        Vector original = new Vector(-4, 3, 3).normalize().multiply(2.5);
        assertEquals(original.getX(), velocity.getX(), 1.0E-12);
        assertEquals(original.getY(), velocity.getY(), 1.0E-12);
        assertEquals(original.getZ(), velocity.getZ(), 1.0E-12);
        assertEquals(2.5, velocity.length(), 1.0E-12);
    }

    @Test
    void coincidentRaisedHookUsesFiniteGentleFallback() {
        Vector velocity = WaterCloudPayoutVelocity.towardsAngler(
                new Location(null, 4, 70, -3),
                new Location(null, 4, 72, -3));
        assertEquals(new Vector(0.0, 0.25, 0.0), velocity);
        assertTrue(Double.isFinite(velocity.lengthSquared()));
    }

    @Test
    void nonFiniteOrNearZeroDifferenceNeverEscapesAsNaN() {
        for (double x : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            Vector velocity = WaterCloudPayoutVelocity.towardsAngler(
                    new Location(null, x, 60, 0),
                    new Location(null, 0, 61, 0));
            assertEquals(new Vector(0.0, 0.25, 0.0), velocity);
        }
        Vector tooSmall = WaterCloudPayoutVelocity.towardsAngler(
                new Location(null, 0, 10, 0),
                new Location(null, 0, 12, 1.0E-8));
        assertEquals(new Vector(0.0, 0.25, 0.0), tooSmall);
    }

    @Test
    void calculationDoesNotMutateInputLocationsAndRejectsNull() {
        Location angler = new Location(null, 10, 64, 10);
        Location hook = new Location(null, -3, 62, 7);
        double ax = angler.getX(), ay = angler.getY(), az = angler.getZ();
        double hx = hook.getX(), hy = hook.getY(), hz = hook.getZ();

        WaterCloudPayoutVelocity.towardsAngler(angler, hook);

        assertEquals(ax, angler.getX());
        assertEquals(ay, angler.getY());
        assertEquals(az, angler.getZ());
        assertEquals(hx, hook.getX());
        assertEquals(hy, hook.getY());
        assertEquals(hz, hook.getZ());
        assertThrows(NullPointerException.class,
                () -> WaterCloudPayoutVelocity.towardsAngler(null, hook));
        assertThrows(NullPointerException.class,
                () -> WaterCloudPayoutVelocity.towardsAngler(angler, null));
    }
}
