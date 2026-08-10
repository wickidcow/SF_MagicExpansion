package io.Yomicer.magicExpansion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Water Cloud rod progression bonuses ported from upstream.
 *
 * Vanilla/Magic fishing can use hook speed, junk reduction, lure preservation,
 * and double catches. External fishing providers remain authoritative; in that
 * mode MagicExpansion only uses non-invasive bonuses such as lure preservation
 * and an additional MagicExpansion bonus catch.
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

    public static final class LevelEffect {
        private final EffectType type;
        private final double value;

        public LevelEffect(EffectType type, double value) {
            this.type = type;
            this.value = value;
        }

        public EffectType getType() {
            return type;
        }

        public double getValue() {
            return value;
        }
    }

    private WaterCloudRodEffects() {
    }

    public static List<LevelEffect> getEffects(int level) {
        List<LevelEffect> effects = new ArrayList<>();

        effects.add(new LevelEffect(EffectType.OLD_HOOK_SPEED, Math.max(0, level / 2) * 60.0));
        effects.add(new LevelEffect(EffectType.OLD_JUNK_REDUCTION, Math.max(0, level - 2) * 0.03));
        effects.add(new LevelEffect(EffectType.OLD_LURE_PRESERVE, Math.max(0, level - 3) * 0.08));
        effects.add(new LevelEffect(EffectType.OLD_DOUBLE_CATCH, Math.max(0, level - 2) * 0.04));

        effects.add(new LevelEffect(EffectType.NEW_BITE_SPEED, Math.max(0, level - 3) * 0.015));
        effects.add(new LevelEffect(EffectType.NEW_LIGHT_BITE_SPEED, Math.max(0, level - 4) * 0.02));
        effects.add(new LevelEffect(EffectType.NEW_CHARGE_SPEED, Math.max(0, level - 2) * 0.5));
        effects.add(new LevelEffect(EffectType.NEW_DOUBLE_CATCH, Math.max(0, level - 2) * 0.03));
        effects.add(new LevelEffect(EffectType.NEW_RARE_BONUS, level >= 8 ? 0.05 : 0.0));

        return effects;
    }

    public static int getOldHookSpeedTicks(int level) {
        return (int) sum(level, EffectType.OLD_HOOK_SPEED);
    }

    public static double getOldJunkReduction(int level) {
        return sum(level, EffectType.OLD_JUNK_REDUCTION);
    }

    public static double getOldLurePreserveChance(int level) {
        return sum(level, EffectType.OLD_LURE_PRESERVE);
    }

    public static double getOldDoubleCatchChance(int level) {
        return sum(level, EffectType.OLD_DOUBLE_CATCH);
    }

    public static double getNewBiteChanceBonus(int level) {
        return sum(level, EffectType.NEW_BITE_SPEED);
    }

    public static double getNewLightBiteChanceBonus(int level) {
        return sum(level, EffectType.NEW_LIGHT_BITE_SPEED);
    }

    public static double getNewChargeSpeedBonus(int level) {
        return sum(level, EffectType.NEW_CHARGE_SPEED);
    }

    public static double getNewDoubleCatchChance(int level) {
        return sum(level, EffectType.NEW_DOUBLE_CATCH);
    }

    public static double getNewRareBonus(int level) {
        return sum(level, EffectType.NEW_RARE_BONUS);
    }

    private static double sum(int level, EffectType type) {
        return getEffects(level).stream()
                .filter(effect -> effect.getType() == type)
                .mapToDouble(LevelEffect::getValue)
                .sum();
    }
}
