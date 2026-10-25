package io.Yomicer.magicExpansion.items.misc.fish;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class WaterCloudCatchMaterializerTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack seed(Gen2Fish fish) {
        ItemStack seed = new ItemStack(fish.getMaterial());
        ItemMeta meta = seed.getItemMeta();
        meta.getPersistentDataContainer().set(FishAttributeGenerator.GEN2_TYPE,
                PersistentDataType.STRING, fish.getId());
        seed.setItemMeta(meta);
        return seed;
    }

    @Test
    void rawSeedBecomesAnIndividualFishWithElementalTraits() {
        ItemStack original = seed(Gen2Fish.YUNYIN);
        assertTrue(WaterCloudCatchMaterializer.isRawSeed(original));

        ItemStack generated = WaterCloudCatchMaterializer.materialize(original, 2);
        assertNotSame(original, generated);
        assertEquals(Gen2Fish.YUNYIN.getMaterial(), generated.getType());
        assertEquals(Gen2Fish.YUNYIN, FishAttributeGenerator.getType(generated));
        assertTrue(FishAttributeGenerator.getQuality(generated) >= 0.7);
        assertTrue(FishAttributeGenerator.getQuality(generated) <= 2.5);
        assertEquals(Gen2Fish.YUNYIN.getDefaultTrait(), FishAttributeGenerator.getTrait(generated));
        assertNull(FishAttributeGenerator.getParents(generated));
        double[] values = FishAttributeGenerator.getElements(generated);
        assertNotNull(values);
        assertEquals(5, values.length);
        double total = 0;
        for (double val : values) {
            assertTrue(Double.isFinite(val) && val >= 0);
            total += val;
        }
        assertEquals(1.0, total, 0.002);
        assertFalse(WaterCloudCatchMaterializer.isRawSeed(generated));
        assertTrue(WaterCloudCatchMaterializer.isRawSeed(original),
                "Generation must not mutate the item template stored in the loot pool");
    }

    @Test
    void neverRerollsExistingFishOrUnrelatedItems() {
        ItemStack finished = WaterCloudCatchMaterializer.materialize(seed(Gen2Fish.LANGXI), 3);
        assertSame(finished, WaterCloudCatchMaterializer.materialize(finished, 4));
        ItemStack vanilla = new ItemStack(Material.COD);
        assertSame(vanilla, WaterCloudCatchMaterializer.materialize(vanilla, 4));
    }

    @Test
    void unknownOrPartiallyMigratedDataIsNotOverwritten() {
        ItemStack badId = seed(Gen2Fish.WUYU);
        ItemMeta badMeta = badId.getItemMeta();
        badMeta.getPersistentDataContainer().set(FishAttributeGenerator.GEN2_TYPE,
                PersistentDataType.STRING, "GEN2_UNRECOGNIZED");
        badId.setItemMeta(badMeta);
        assertSame(badId, WaterCloudCatchMaterializer.materialize(badId, 1));

        ItemStack partial = seed(Gen2Fish.WUYU);
        ItemMeta partialMeta = partial.getItemMeta();
        partialMeta.getPersistentDataContainer().set(FishAttributeGenerator.GEN2_ELEMENTS,
                PersistentDataType.STRING, "0.1,0.2,0.3,0.2,0.2");
        partial.setItemMeta(partialMeta);
        assertFalse(WaterCloudCatchMaterializer.isRawSeed(partial));
        assertSame(partial, WaterCloudCatchMaterializer.materialize(partial, 1));

        ItemStack stack = seed(Gen2Fish.WUYU);
        stack.setAmount(2);
        assertSame(stack, WaterCloudCatchMaterializer.materialize(stack, 1),
                "Unexpected stack quantities must not be collapsed to a single fish");
    }
}
