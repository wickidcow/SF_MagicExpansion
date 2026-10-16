package io.Yomicer.magicExpansion.utils.fishing;

/** Overflow-safe production arithmetic for stacked vivariums. */
public final class VivariumOutputMath {
    private VivariumOutputMath() {}

    public static long calculate(
            double weight,
            int normalMultiplier,
            int easyMultiplier,
            long normalMachines,
            long easyMachines) {
        if (!Double.isFinite(weight)
                || weight <= 0.0D
                || normalMultiplier <= 0
                || easyMultiplier <= 0
                || normalMachines < 0L
                || easyMachines < 0L
                || (normalMachines == 0L && easyMachines == 0L)) {
            return 0L;
        }

        long perNormal = scaledPerMachine(weight, normalMultiplier);
        long perEasy = scaledPerMachine(weight, easyMultiplier);
        return safeAdd(safeMultiply(perNormal, normalMachines),
                safeMultiply(perEasy, easyMachines));
    }

    static long scaledPerMachine(double weight, int multiplier) {
        double raw = weight * multiplier;
        if (!Double.isFinite(raw) || raw >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        long value = (long) raw;
        return Math.max(1L, value);
    }

    static long safeMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    static long safeAdd(long left, long right) {
        if (left < 0L || right < 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
