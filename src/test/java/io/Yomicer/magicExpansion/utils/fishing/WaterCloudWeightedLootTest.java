package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockbukkit.mockbukkit.MockBukkit;

class WaterCloudWeightedLootTest {

    @BeforeEach
    void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    void cleanup() {
        MockBukkit.unmock();
    }

    @Test
    void emptyAndNonPositivePoolsLeaveVanillaCatchUntouched() {
        assertNull(WaterCloudWeightedLoot.selectRandom(null));
        assertNull(WaterCloudWeightedLoot.selectRandom(List.of()));
        assertNull(WaterCloudWeightedLoot.selectRandom(List.of(
                new WeightedItem(new ItemStack(Material.COD), 0),
                new WeightedItem(new ItemStack(Material.SALMON), -7))));
        assertEquals(0, WaterCloudWeightedLoot.totalWeight(null));
        assertEquals(0, WaterCloudWeightedLoot.totalWeight(List.of()));
    }

    @Test
    void exactTicketBoundariesKeepOriginalWeightsAndOrder() {
        WeightedItem cod = new WeightedItem(new ItemStack(Material.COD), 50);
        WeightedItem salmon = new WeightedItem(new ItemStack(Material.SALMON), 30);
        List<WeightedItem> pool = List.of(cod, salmon);

        assertEquals(80, WaterCloudWeightedLoot.totalWeight(pool));
        assertEquals(Material.COD, WaterCloudWeightedLoot.selectAt(pool, 0).getType());
        assertEquals(Material.COD, WaterCloudWeightedLoot.selectAt(pool, 49).getType());
        assertEquals(Material.SALMON, WaterCloudWeightedLoot.selectAt(pool, 50).getType());
        assertEquals(Material.SALMON, WaterCloudWeightedLoot.selectAt(pool, 79).getType());
        assertThrows(IllegalArgumentException.class,
                () -> WaterCloudWeightedLoot.selectAt(pool, 80));
        assertThrows(IllegalArgumentException.class,
                () -> WaterCloudWeightedLoot.selectAt(pool, -1));
    }

    @Test
    void largeWeightsCannotOverflowAndNeverModifyTemplates() {
        ItemStack firstTemplate = new ItemStack(Material.COD, 5);
        ItemStack secondTemplate = new ItemStack(Material.SALMON);
        List<WeightedItem> pool = List.of(
                new WeightedItem(firstTemplate, Integer.MAX_VALUE),
                new WeightedItem(secondTemplate, Integer.MAX_VALUE));

        long maxInt = Integer.MAX_VALUE;
        assertEquals(2L * maxInt, WaterCloudWeightedLoot.totalWeight(pool));
        ItemStack selected = WaterCloudWeightedLoot.selectAt(pool, maxInt);
        assertEquals(Material.SALMON, selected.getType());
        assertNotSame(secondTemplate, selected);
        ItemStack clonedFirst = WaterCloudWeightedLoot.selectAt(pool, maxInt - 1);
        assertEquals(5, clonedFirst.getAmount());
        clonedFirst.setAmount(1);
        assertEquals(5, firstTemplate.getAmount(), "Selecting a payout must not mutate the template");
    }

    @Test
    void invalidEntriesAreIgnoredWithoutGivingFreeFish() {
        List<WeightedItem> entries = java.util.Arrays.asList(
                null,
                new WeightedItem(null, 400),
                new WeightedItem(new ItemStack(Material.AIR), 400),
                new WeightedItem(new ItemStack(Material.COD), -100),
                new WeightedItem(new ItemStack(Material.TROPICAL_FISH), 4));
        assertEquals(4, WaterCloudWeightedLoot.totalWeight(entries));
        assertEquals(Material.TROPICAL_FISH,
                WaterCloudWeightedLoot.selectAt(entries, 3).getType());
    }
}
