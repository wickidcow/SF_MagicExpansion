package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class WaterCloudRodEffectsTest {

    @Test
    void upstreamOldFishingBonusesMatchEveryLevel() {
        int[] hookTicks = {0, 60, 60, 120, 120, 180, 180, 240};
        double[] junkReduction = {0, 0, 0.03, 0.06, 0.09, 0.12, 0.15, 0.18};
        double[] preserve = {0, 0, 0, 0.08, 0.16, 0.24, 0.32, 0.40};
        double[] doubleCatch = {0, 0, 0.04, 0.08, 0.12, 0.16, 0.20, 0.24};
        for (int level = 1; level <= 8; level++) {
            int i = level - 1;
            assertEquals(hookTicks[i], WaterCloudRodEffects.getOldHookSpeedTicks(level));
            assertEquals(junkReduction[i], WaterCloudRodEffects.getOldJunkReduction(level), 1e-12);
            assertEquals(preserve[i], WaterCloudRodEffects.getOldLurePreserveChance(level), 1e-12);
            assertEquals(doubleCatch[i], WaterCloudRodEffects.getOldDoubleCatchChance(level), 1e-12);
        }
    }

    @Test
    void upstreamAlternateFishingBonusesMatchEveryLevel() {
        double[] bite = {0, 0, 0, 0.015, 0.03, 0.045, 0.06, 0.075};
        double[] lightBite = {0, 0, 0, 0, 0.02, 0.04, 0.06, 0.08};
        double[] charge = {0, 0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0};
        double[] doubleCatch = {0, 0, 0.03, 0.06, 0.09, 0.12, 0.15, 0.18};
        for (int level = 1; level <= 8; level++) {
            int i = level - 1;
            assertEquals(bite[i], WaterCloudRodEffects.getNewBiteChanceBonus(level), 1e-12);
            assertEquals(lightBite[i], WaterCloudRodEffects.getNewLightBiteChanceBonus(level), 1e-12);
            assertEquals(charge[i], WaterCloudRodEffects.getNewChargeSpeedBonus(level), 1e-12);
            assertEquals(doubleCatch[i], WaterCloudRodEffects.getNewDoubleCatchChance(level), 1e-12);
            assertEquals(level == 8 ? 0.05 : 0.0,
                    WaterCloudRodEffects.getNewRareBonus(level), 1e-12);
        }
    }

    @Test
    void corruptedLevelsCannotGrantAboveMaxBonusAndListsAreImmutable() {
        assertEquals(WaterCloudRodEffects.getEffects(1),
                WaterCloudRodEffects.getEffects(Integer.MIN_VALUE));
        assertEquals(WaterCloudRodEffects.getEffects(8),
                WaterCloudRodEffects.getEffects(Integer.MAX_VALUE));
        assertSame(WaterCloudRodEffects.getEffects(8), WaterCloudRodEffects.getEffects(8),
                "Frequently queried bonus rows should be cached");
        List<WaterCloudRodEffects.LevelEffect> effects = WaterCloudRodEffects.getEffects(8);
        assertEquals(9, effects.size());
        assertThrows(UnsupportedOperationException.class, () ->
                effects.add(new WaterCloudRodEffects.LevelEffect(
                        WaterCloudRodEffects.EffectType.NEW_RARE_BONUS, 1.0)));
        for (int level = 1; level <= 8; level++) {
            for (WaterCloudRodEffects.LevelEffect effect : WaterCloudRodEffects.getEffects(level)) {
                assertTrue(Double.isFinite(effect.value()));
                assertTrue(effect.value() >= 0);
                assertEquals(effect.type(), effect.getType());
                assertEquals(effect.value(), effect.getValue());
            }
        }
    }
}
