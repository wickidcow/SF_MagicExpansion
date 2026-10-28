package io.Yomicer.magicExpansion.utils.fishing;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Calculates a finite Water Cloud catch trajectory without touching the
 * player's location object or any live Bukkit entity.
 */
public final class WaterCloudPayoutVelocity {

    private static final double CATCH_SPEED = 2.5;
    private static final double MIN_LENGTH_SQUARED = 1.0E-12;

    private WaterCloudPayoutVelocity() {}

    /**
     * Normal catches retain the original 2.5-block/tick direction toward the
     * angler's raised position. Coincident or non-finite coordinates would
     * generate NaN via Vector.normalize(); give those catches a gentle upward
     * velocity instead so a valid paid reward is not lost to an exception.
     */
    public static Vector towardsAngler(Location playerLocation, Location hookLocation) {
        Objects.requireNonNull(playerLocation, "playerLocation");
        Objects.requireNonNull(hookLocation, "hookLocation");

        Vector delta = new Vector(
                playerLocation.getX() - hookLocation.getX(),
                playerLocation.getY() + 2.0 - hookLocation.getY(),
                playerLocation.getZ() - hookLocation.getZ());

        double lengthSquared = delta.lengthSquared();
        if (!Double.isFinite(lengthSquared) || lengthSquared < MIN_LENGTH_SQUARED) {
            return new Vector(0.0, 0.25, 0.0);
        }
        return delta.multiply(CATCH_SPEED / Math.sqrt(lengthSquared));
    }
}
