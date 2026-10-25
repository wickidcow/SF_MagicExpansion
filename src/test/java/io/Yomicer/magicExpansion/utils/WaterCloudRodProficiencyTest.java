package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class WaterCloudRodProficiencyTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void exactUpstreamThresholdsAndLevelCap() {
        assertEquals(new WaterCloudRodProficiency.Progress(1, 299),
                WaterCloudRodProficiency.advance(1, 298, 1));
        assertEquals(new WaterCloudRodProficiency.Progress(2, 0),
                WaterCloudRodProficiency.advance(1, 299, 1));
        assertEquals(new WaterCloudRodProficiency.Progress(3, 0),
                WaterCloudRodProficiency.advance(1, 0, 900));
        assertEquals(new WaterCloudRodProficiency.Progress(8, 0),
                WaterCloudRodProficiency.advance(1, 0, 100_000));
        assertEquals(new WaterCloudRodProficiency.Progress(8, 0),
                WaterCloudRodProficiency.advance(8, Integer.MAX_VALUE, 5));
        assertEquals(7200, WaterCloudRodProficiency.getRequirement(7));
        assertEquals(0, WaterCloudRodProficiency.getRequirement(8));
    }

    @Test
    void invalidAndOverflowingSavedValuesCannotExceedCap() {
        assertEquals(new WaterCloudRodProficiency.Progress(1, 0),
                WaterCloudRodProficiency.advance(-8, -123, 0));
        assertEquals(new WaterCloudRodProficiency.Progress(8, 0),
                WaterCloudRodProficiency.advance(1, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(new WaterCloudRodProficiency.Progress(8, 0),
                WaterCloudRodProficiency.advance(99, 0, 1));
    }

    @Test
    void preservesOriginalItemMetadataAndExistingUpstreamPdc() {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        var meta = rod.getItemMeta();
        meta.setDisplayName("My named fishing rod");
        meta.addEnchant(Enchantment.LURE, 3, true);
        meta.setLore(List.of("§7An existing enchantment", "§bWater Cloud Proficiency: old"));
        NamespacedKey slimefunId = new NamespacedKey("slimefun", "id");
        meta.getPersistentDataContainer().set(slimefunId, PersistentDataType.STRING,
                "FISHING_ROD_BETWEEN_WATER_CLOUD_REED");
        meta.getPersistentDataContainer().set(WaterCloudRodProficiency.LV_KEY,
                PersistentDataType.INTEGER, 2);
        meta.getPersistentDataContainer().set(WaterCloudRodProficiency.XP_KEY,
                PersistentDataType.INTEGER, 598);
        rod.setItemMeta(meta);

        assertEquals(new WaterCloudRodProficiency.Progress(3, 3),
                WaterCloudRodProficiency.award(rod, WaterCloudRodProficiency.XP_SPECIAL));
        assertEquals("My named fishing rod", rod.getItemMeta().getDisplayName());
        assertEquals(3, rod.getEnchantmentLevel(Enchantment.LURE));
        assertEquals("FISHING_ROD_BETWEEN_WATER_CLOUD_REED",
                rod.getItemMeta().getPersistentDataContainer()
                        .get(slimefunId, PersistentDataType.STRING));
        assertEquals(3, rod.getItemMeta().getPersistentDataContainer()
                .get(WaterCloudRodProficiency.LV_KEY, PersistentDataType.INTEGER));
        assertEquals(3, rod.getItemMeta().getPersistentDataContainer()
                .get(WaterCloudRodProficiency.XP_KEY, PersistentDataType.INTEGER));
        assertTrue(rod.getItemMeta().getLore().contains("§7An existing enchantment"));
        assertEquals(1, rod.getItemMeta().getLore().stream()
                .filter(s -> s.contains("Water Cloud Proficiency:")).count());

        WaterCloudRodProficiency.award(rod, WaterCloudRodProficiency.XP_COMMON);
        assertEquals(1, rod.getItemMeta().getLore().stream()
                .filter(s -> s.contains("Water Cloud Proficiency:")).count(),
                "Repeated catches must update, never accumulate, proficiency lore");
    }

    @Test
    void unlevelledRodStartsAtLevelOneWithoutChangingItOnRead() {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        assertEquals(new WaterCloudRodProficiency.Progress(1, 0),
                WaterCloudRodProficiency.read(rod));
        assertFalse(rod.getItemMeta().getPersistentDataContainer()
                .has(WaterCloudRodProficiency.LV_KEY, PersistentDataType.INTEGER));
        assertEquals(new WaterCloudRodProficiency.Progress(1, 0),
                WaterCloudRodProficiency.award(rod, 0));
        assertFalse(rod.getItemMeta().getPersistentDataContainer()
                .has(WaterCloudRodProficiency.LV_KEY, PersistentDataType.INTEGER));
    }

    @Test
    void onlyThreeAdvancedRodsReceiveProficiency() {
        assertFalse(WaterCloudRodProficiency.supportsRodId(
                "FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO"));
        for (String id : List.of("FISHING_ROD_BETWEEN_WATER_CLOUD_REED",
                "FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG",
                "FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU")) {
            assertTrue(WaterCloudRodProficiency.supportsRodId(id));
            assertTrue(WaterCloudRodProficiency.supportsRodId("MAGIC_EXPANSION_" + id));
        }
        assertFalse(WaterCloudRodProficiency.supportsRodId(null));
        assertFalse(WaterCloudRodProficiency.supportsRodId("NOT_A_MAGIC_ROD"));
    }
}
