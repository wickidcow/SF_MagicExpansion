package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WaterCloudClassicJunkReductionTest {

    @Test
    void upstreamSixJunkMaterialsUseOriginalRoundAndMinimumOneRule() {
        Material[] junk = {
                Material.COBBLESTONE, Material.FLINT, Material.GRAVEL,
                Material.SAND, Material.STRING, Material.BONE
        };
        for (Material material : junk) {
            assertEquals(66,
                    WaterCloudClassicJunkReduction.adjustedWeight(66, material, false, 1));
            assertEquals(64,
                    WaterCloudClassicJunkReduction.adjustedWeight(66, material, false, 3));
            assertEquals(54,
                    WaterCloudClassicJunkReduction.adjustedWeight(66, material, false, 8));
            assertEquals(1,
                    WaterCloudClassicJunkReduction.adjustedWeight(1, material, false, 8));
        }
    }

    @Test
    void slimefunItemsAndNormalFishingMaterialsAreNeverReweighted() {
        for (Material material : new Material[]{
                Material.COBBLESTONE, Material.FLINT, Material.STRING,
                Material.BONE, Material.NETHER_STAR, Material.COD_BUCKET,
                Material.EMERALD, Material.GHAST_TEAR
        }) {
            assertEquals(100,
                    WaterCloudClassicJunkReduction.adjustedWeight(100, material, true, 8),
                    "Slimefun item in a junk-like material must keep its registered weight");
        }

        for (Material material : new Material[]{
                Material.NETHER_STAR, Material.COD_BUCKET,
                Material.GHAST_TEAR, Material.EMERALD, Material.BAMBOO
        }) {
            assertEquals(100,
                    WaterCloudClassicJunkReduction.adjustedWeight(100, material, false, 8),
                    "Unregistered normal/special fishing loot is not upstream-designated junk");
        }
    }

    @Test
    void invalidWeightsAndOutOfRangeLevelsCannotCreateFreePayouts() {
        assertEquals(0, WaterCloudClassicJunkReduction.adjustedWeight(
                0, Material.SAND, false, 8));
        assertEquals(-10, WaterCloudClassicJunkReduction.adjustedWeight(
                -10, Material.SAND, false, 8));
        assertEquals(100, WaterCloudClassicJunkReduction.adjustedWeight(
                100, null, false, 8));
        assertEquals(100, WaterCloudClassicJunkReduction.adjustedWeight(
                100, Material.SAND, false, Integer.MIN_VALUE));
        assertEquals(82, WaterCloudClassicJunkReduction.adjustedWeight(
                100, Material.SAND, false, Integer.MAX_VALUE));
    }
}
