package io.Yomicer.magicExpansion.utils.quickMachine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class QuickCraftSafetyTest {

    @Test
    void normalRecipeAvailabilityAndMaximumAreCalculatedExactly() {
        Map<String, Integer> available = Map.of(
                "mc:IRON_INGOT", 17,
                "sf:REINFORCED_ALLOY_INGOT", 9);
        Map<String, Integer> recipe = Map.of(
                "mc:IRON_INGOT", 3,
                "sf:REINFORCED_ALLOY_INGOT", 2);

        assertTrue(QuickCraftSafety.hasEnoughMaterials(available, recipe, 4));
        assertFalse(QuickCraftSafety.hasEnoughMaterials(available, recipe, 5));
        assertEquals(4, QuickCraftSafety.calculateMaxCraftableAmount(available, recipe));
    }

    @Test
    void emptyOrMalformedRecipesCannotBecomeFreeInfiniteCrafts() {
        assertFalse(QuickCraftSafety.hasEnoughMaterials(Map.of(), Map.of(), 1));
        assertEquals(0, QuickCraftSafety.calculateMaxCraftableAmount(Map.of(), Map.of()));

        assertFalse(QuickCraftSafety.hasEnoughMaterials(
                Map.of("mc:STONE", 64), Map.of("mc:STONE", 0), 1));
        assertEquals(0, QuickCraftSafety.calculateMaxCraftableAmount(
                Map.of("mc:STONE", 64), Map.of("mc:STONE", 0)));

        assertFalse(QuickCraftSafety.hasEnoughMaterials(
                Map.of("mc:STONE", 64), Map.of("", 1), 1));
    }

    @Test
    void nonpositiveCraftCountsAreRejected() {
        Map<String, Integer> recipe = Map.of("mc:STONE", 1);
        Map<String, Integer> available = Map.of("mc:STONE", 64);

        assertFalse(QuickCraftSafety.hasEnoughMaterials(available, recipe, 0));
        assertFalse(QuickCraftSafety.hasEnoughMaterials(available, recipe, -1));
        assertEquals(-1, QuickCraftSafety.totalRequiredAmount(1, 0));
        assertEquals(-1, QuickCraftSafety.totalRequiredAmount(1, -1));
    }

    @Test
    void requiredAmountMultiplicationCannotWrapAround() {
        assertEquals(-1, QuickCraftSafety.totalRequiredAmount(Integer.MAX_VALUE, 2));

        Map<String, Integer> available = Map.of("mc:STONE", Integer.MAX_VALUE);
        Map<String, Integer> recipe = Map.of("mc:STONE", 50_000);
        assertFalse(QuickCraftSafety.hasEnoughMaterials(available, recipe, 50_000));
    }

    @Test
    void absentOrNegativeInventoryCountsNeverSatisfyARecipe() {
        Map<String, Integer> recipe = Map.of("mc:STONE", 1);
        assertFalse(QuickCraftSafety.hasEnoughMaterials(Map.of(), recipe, 1));
        assertFalse(QuickCraftSafety.hasEnoughMaterials(Map.of("mc:STONE", -1), recipe, 1));
        assertEquals(0, QuickCraftSafety.calculateMaxCraftableAmount(Map.of("mc:STONE", -1), recipe));
    }

    @Test
    void exactIntegerBoundaryRemainsValid() {
        assertEquals(Integer.MAX_VALUE,
                QuickCraftSafety.totalRequiredAmount(Integer.MAX_VALUE, 1));
        assertTrue(QuickCraftSafety.hasEnoughMaterials(
                Map.of("mc:STONE", Integer.MAX_VALUE),
                Map.of("mc:STONE", Integer.MAX_VALUE),
                1));
        assertEquals(1, QuickCraftSafety.calculateMaxCraftableAmount(
                Map.of("mc:STONE", Integer.MAX_VALUE),
                Map.of("mc:STONE", Integer.MAX_VALUE)));
    }
}
