package io.Yomicer.magicExpansion.utils.fishing;

import io.Yomicer.magicExpansion.utils.WaterCloudRodEffects;
import java.util.Objects;

/**
 * Scheduler- and world-independent Water Cloud alternate-fishing state rules.
 *
 * <p>Phase one only: this class does not register Bukkit events, schedule tasks,
 * create drops, debit bait, change experience bars, or enable the alternate
 * fishing system. Any future handler must own player/hook changes on the
 * correct region and retain FishingCompatibility and bait-payment gates.
 */
public final class WaterCloudHookMath {

    public static final int LIGHT_BITE_WINDOW_SECONDS = 8;
    public static final int SESSION_LIMIT_SECONDS = 600;
    public static final int FULL_BITE_LIMIT_SECONDS = 30;
    public static final double MAX_HOOK_DISTANCE_BLOCKS = 20.0;
    public static final double AIM_THRESHOLD_DEGREES = 15.0;
    public static final double BASE_CHARGE_PER_TICK = 1.25;
    public static final double MAX_CHARGE = 100.0;

    public enum Stage { WAITING, LIGHT_BITE, FULL_BITE }

    /** A light bite can expire back to WAITING with a single escape signal. */
    public record Transition(Stage stage, boolean escaped) {
        public Transition {
            Objects.requireNonNull(stage, "stage");
        }
    }

    private WaterCloudHookMath() {}

    /**
     * Apply exactly one upstream one-second bite roll. The caller supplies a
     * random sample in [0,1); no random state or global task is retained here.
     * Invalid samples simply cannot award an early bite, but do not bypass
     * light-bite expiration.
     */
    public static Transition advance(Stage current, int stateSeconds, int rodLevel, double roll) {
        Objects.requireNonNull(current, "current");
        boolean validRoll = Double.isFinite(roll) && roll >= 0.0 && roll < 1.0;
        return switch (current) {
            case WAITING -> new Transition(
                    validRoll && roll < 0.10 + WaterCloudRodEffects.getNewBiteChanceBonus(rodLevel)
                            ? Stage.LIGHT_BITE : Stage.WAITING, false);
            case LIGHT_BITE -> {
                if (validRoll && roll < 0.20
                        + WaterCloudRodEffects.getNewLightBiteChanceBonus(rodLevel)) {
                    yield new Transition(Stage.FULL_BITE, false);
                }
                if (stateSeconds >= LIGHT_BITE_WINDOW_SECONDS) {
                    yield new Transition(Stage.WAITING, true);
                }
                yield new Transition(Stage.LIGHT_BITE, false);
            }
            case FULL_BITE -> new Transition(Stage.FULL_BITE, false);
        };
    }

    /** The original 1% / 40% / (10% + charge × 0.9%) reel formula. */
    public static double reelCatchChance(Stage stage, double charge) {
        Objects.requireNonNull(stage, "stage");
        return switch (stage) {
            case WAITING -> 0.01;
            case LIGHT_BITE -> 0.40;
            case FULL_BITE -> 0.10 + 0.90 * (clampCharge(charge) / MAX_CHARGE);
        };
    }

    /** Charge only while accurately aimed; never overflow or process NaNs. */
    public static double nextCharge(double currentCharge, double aimErrorDegrees, int rodLevel) {
        double held = clampCharge(currentCharge);
        if (!Double.isFinite(aimErrorDegrees)
                || Math.abs(aimErrorDegrees) > AIM_THRESHOLD_DEGREES) {
            return held;
        }
        double increment = BASE_CHARGE_PER_TICK
                + WaterCloudRodEffects.getNewChargeSpeedBonus(rodLevel) / 20.0;
        return Math.min(MAX_CHARGE, held + increment);
    }

    /** A stale session must be discarded without paying any fish reward. */
    public static boolean sessionExpired(long elapsedMillis) {
        return elapsedMillis >= SESSION_LIMIT_SECONDS * 1000L;
    }

    /** Full bites time out or escape when the hook moves beyond 20 blocks. */
    public static boolean fullBiteExpired(long elapsedMillis, double hookDistanceBlocks) {
        return elapsedMillis >= FULL_BITE_LIMIT_SECONDS * 1000L
                || !Double.isFinite(hookDistanceBlocks)
                || hookDistanceBlocks < 0.0
                || hookDistanceBlocks > MAX_HOOK_DISTANCE_BLOCKS;
    }

    private static double clampCharge(double charge) {
        return Double.isFinite(charge) ? Math.max(0.0, Math.min(MAX_CHARGE, charge)) : 0.0;
    }
}
