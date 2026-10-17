package io.Yomicer.magicExpansion.utils.energy;

/**
 * Overflow-safe arithmetic for Power Card and energy-relay transfers.
 */
public final class PowerRelayMath {
    private PowerRelayMath() {}

    public static Transfer fill(long storedEnergy, long maximumEnergy, int availableEnergy) {
        if (maximumEnergy <= 0L || storedEnergy < 0L || storedEnergy >= maximumEnergy || availableEnergy <= 0) {
            return new Transfer(storedEnergy, 0);
        }

        long remainingCapacity = maximumEnergy - storedEnergy;
        int transferred = (int) Math.min((long) availableEnergy, remainingCapacity);
        return new Transfer(storedEnergy + transferred, transferred);
    }

    public static Transfer drain(long storedEnergy, int availableCapacity) {
        if (storedEnergy <= 0L || availableCapacity <= 0) {
            return new Transfer(storedEnergy, 0);
        }

        int transferred = (int) Math.min(storedEnergy, (long) availableCapacity);
        return new Transfer(storedEnergy - transferred, transferred);
    }

    public static boolean canSetMaximum(long storedEnergy, long newMaximum) {
        return storedEnergy >= 0L && newMaximum > 0L && storedEnergy <= newMaximum;
    }

    public record Transfer(long storedEnergy, int transferred) {}
}
