package io.Yomicer.magicExpansion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Exact numerical bonus schedule from upstream Water Cloud rods, without
 * enabling new fishing behavior until the event and region-safety tests pass.
 *
 * <p>Precomputing eight immutable rows avoids allocating effect lists for
 * every bite/catch on heavily populated servers.
 */
public final class WaterCloudRodEffects {

    public enum EffectType {
        OLD_HOOK_SPEED,
        OLD_JUNK_REDUCTION,
        OLD_LURE_PRESERVE,
        OLD_DOUBLE_CATCH,
        NEW_BITE_SPEED,
        NEW_LIGHT_BITE_SPEED,
        NEW_CHARGE_SPEED,
        NEW_DOUBLE_CATCH,
        NEW_RARE_BONUS
    }

    /** Immutable entry for callers comparing or displaying upstream bonuses. */
    public record LevelEffect(EffectType type, double value) {
        public EffectType getType() {
            return type;
        }

        public double getValue() {
            return value;
        }
    }

    private static final List<List<LevelEffect>> CACHED_LEVELS = buildLevels();

    private WaterCloudRodEffects() {
    }

    private static List<List<LevelEffect>> buildLevels() {
        List<List<LevelEffect>> levels = new ArrayList<>(8);
        for (int level = 1; level <= 8; level++) {
            levels.add(List.of(
                    new LevelEffect(EffectType.OLD_HOOK_SPEED, (level / 2) * 60.0),
                    new LevelEffect(EffectType.OLD_JUNK_REDUCTION,
                            Math.max(0, level - 2) * 0.03),
                    new LevelEffect(EffectType.OLD_LURE_PRESERVE,
                            Math.max(0, level - 3) * 0.08),
                    new LevelEffect(EffectType.OLD_DOUBLE_CATCH,
                            Math.max(0, level - 2) * 0.04),
                    new LevelEffect(EffectType.NEW_BITE_SPEED,
                            Math.max(0, level - 3) * 0.015),
                    new LevelEffect(EffectType.NEW_LIGHT_BITE_SPEED,
                            Math.max(0, level - 4) * 0.02),
                    new LevelEffect(EffectType.NEW_CHARGE_SPEED,
                            Math.max(0, level - 2) * 0.5),
                    new LevelEffect(EffectType.NEW_DOUBLE_CATCH,
                            Math.max(0, level - 2) * 0.03),
                    new LevelEffect(EffectType.NEW_RARE_BONUS,
                            level == 8 ? 0.05 : 0.0)
            ));
        }
        return List.copyOf(levels);
    }

    /** Cache includes all nine upstream effects at each of the eight levels. */
    public static List<LevelEffect> getEffects(int level) {
        return CACHED_LEVELS.get(clampLevel(level) - 1);
    }

    private static int clampLevel(int level) {
        return Math.max(1, Math.min(8, level));
    }

    private static double getValue(int level, EffectType type) {
        return getEffects(level).get(type.ordinal()).value();
    }

    public static int getOldHookSpeedTicks(int level) {
        return (int) getValue(level, EffectType.OLD_HOOK_SPEED);
    }

    public static double getOldJunkReduction(int level) {
        return getValue(level, EffectType.OLD_JUNK_REDUCTION);
    }

    public static double getOldLurePreserveChance(int level) {
        return getValue(level, EffectType.OLD_LURE_PRESERVE);
    }

    public static double getOldDoubleCatchChance(int level) {
        return getValue(level, EffectType.OLD_DOUBLE_CATCH);
    }

    public static double getNewBiteChanceBonus(int level) {
        return getValue(level, EffectType.NEW_BITE_SPEED);
    }

    public static double getNewLightBiteChanceBonus(int level) {
        return getValue(level, EffectType.NEW_LIGHT_BITE_SPEED);
    }

    public static double getNewChargeSpeedBonus(int level) {
        return getValue(level, EffectType.NEW_CHARGE_SPEED);
    }

    public static double getNewDoubleCatchChance(int level) {
        return getValue(level, EffectType.NEW_DOUBLE_CATCH);
    }

    public static double getNewRareBonus(int level) {
        return getValue(level, EffectType.NEW_RARE_BONUS);
    }
}
