package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ADataContainer;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class CargoStorageTest {
    private MemoryData data;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        data = new MemoryData();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void fullWarehouseRefusesANewTypeWithoutChangingStoredDataOrSource() {
        String stone = SameItemJudge.itemToBase64(new ItemStack(Material.STONE));
        for (int i = 0; i < 1145; i++) {
            data.setData("item_type_" + i, stone);
            data.setData("item_count_" + i, "64");
            data.setData("item_max_" + i, "-1");
        }
        Map<String, String> before = Map.copyOf(data.getAllData());
        ItemStack source = new ItemStack(Material.DIAMOND, 64);
        assertEquals(0, CargoStorage.capacity(data, source, 64, 1145));
        assertEquals(0, CargoStorage.store(data, source, 64, 1145));
        assertEquals(before, data.getAllData());
        assertEquals(64, source.getAmount());
    }

    @Test
    void fullWarehouseStillAcceptsAnExistingType() {
        seed(new ItemStack(Material.DIAMOND), 50, -1);
        assertEquals(64, CargoStorage.store(data, new ItemStack(Material.DIAMOND, 64), 64, 1));
        assertEquals("114", data.getData("item_count_0"));
    }

    @Test
    void limitAcceptsOnlyTheRemainderAndDoesNotMutateTheSource() {
        seed(new ItemStack(Material.DIAMOND), 95, 100);
        ItemStack source = new ItemStack(Material.DIAMOND, 64);
        assertEquals(5, CargoStorage.store(data, source, 64, 1));
        assertEquals("100", data.getData("item_count_0"));
        assertEquals(64, source.getAmount());
        assertEquals(0, CargoStorage.store(data, source, 64, 1));
    }

    @Test
    void exactInsertionRefusesChangedCapacityWithoutPartiallyConsumingAFragment() {
        seed(new ItemStack(Material.DIAMOND), 95, 100);
        Map<String, String> before = Map.copyOf(data.getAllData());
        assertEquals(0, CargoStorage.storeExact(data, new ItemStack(Material.DIAMOND), 6, 1));
        assertEquals(before, data.getAllData());
        assertEquals(5, CargoStorage.storeExact(data, new ItemStack(Material.DIAMOND), 5, 1));
    }

    @Test
    void unlimitedCountsCannotOverflowOrBecomeNegative() {
        seed(new ItemStack(Material.DIAMOND), Long.MAX_VALUE - 3, -1);
        assertEquals(3, CargoStorage.store(data, new ItemStack(Material.DIAMOND, 64), 64, 1));
        assertEquals(Long.toString(Long.MAX_VALUE), data.getData("item_count_0"));
        assertEquals(0, CargoStorage.store(data, new ItemStack(Material.DIAMOND), 1, 1));
    }

    @Test
    void largeFragmentCountsAreStoredWithoutWorldDropsOrStackCaps() {
        assertEquals(Integer.MAX_VALUE,
                CargoStorage.store(data, new ItemStack(Material.DIAMOND), Integer.MAX_VALUE, 1));
        assertEquals(Integer.toString(Integer.MAX_VALUE), data.getData("item_count_0"));
    }

    @Test
    void zeroCountReservationsKeepTheirExistingLimitAndTemplate() {
        ItemStack item = historicalItem("old-owner");
        seed(item, 0, 100);
        String original = data.getData("item_type_0");
        assertEquals(3, CargoStorage.store(data, item, 3, 1));
        assertEquals(original, data.getData("item_type_0"));
        assertEquals("100", data.getData("item_max_0"));
    }

    @Test
    void historicalItemIdentityAndTypedMetadataRemainDistinct() {
        ItemStack old = historicalItem("old-owner");
        assertEquals(7, CargoStorage.store(data, old, 7, 1));
        ItemStack restored = SameItemJudge.itemFromBase64(data.getData("item_type_0"));
        assertEquals(old, restored);
        assertEquals(0, CargoStorage.store(data, historicalItem("different-owner"), 1, 1));
        assertEquals(0, CargoStorage.store(data, new ItemStack(Material.DIAMOND), 1, 1));
        assertEquals("7", data.getData("item_count_0"));
    }

    @Test
    void unreadableAndIncompleteRecordsRemainOccupiedAndUnchanged() {
        data.setData("item_type_0", "not-base64");
        data.setData("item_count_0", "9007199254740993");
        data.setData("item_count_1", "42");
        data.setData("item_max_2", "100");
        Map<String, String> before = Map.copyOf(data.getAllData());
        assertEquals(0, CargoStorage.store(data, new ItemStack(Material.DIAMOND), 1, 3));
        assertEquals(before, data.getAllData());
    }

    @Test
    void malformedMatchedCountsOrLimitsDoNotResetStoredItems() {
        seed(new ItemStack(Material.DIAMOND), 10, 100);
        for (String invalid : new String[] {"broken", "-2", "9223372036854775808"}) {
            data.setData("item_max_0", invalid);
            Map<String, String> before = Map.copyOf(data.getAllData());
            assertEquals(0, CargoStorage.store(data, new ItemStack(Material.DIAMOND), 1, 2));
            assertEquals(before, data.getAllData());
        }
        data.setData("item_max_0", "-1");
        data.setData("item_count_0", "broken");
        assertEquals(0, CargoStorage.store(data, new ItemStack(Material.DIAMOND), 1, 2));
        assertEquals("broken", data.getData("item_count_0"));
    }

    private void seed(ItemStack item, long count, long limit) {
        data.setData("item_type_0", SameItemJudge.itemToBase64(item));
        data.setData("item_count_0", Long.toString(count));
        data.setData("item_max_0", Long.toString(limit));
    }

    private ItemStack historicalItem(String owner) {
        ItemStack item = new ItemStack(Material.DIAMOND, 3);
        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        pdc.set(NamespacedKey.fromString("slimefun:slimefun_item"), PersistentDataType.STRING, "UNREGISTERED_OLD_ID");
        pdc.set(NamespacedKey.fromString("magicexpansion:owner"), PersistentDataType.STRING, owner);
        pdc.set(NamespacedKey.fromString("magicexpansion:amount"), PersistentDataType.LONG, 9007199254740993L);
        item.setItemMeta(meta);
        return item;
    }

    private static final class MemoryData extends ADataContainer {
        private MemoryData() {
            super("cargo-test");
            setIsDataLoaded(true);
        }

        @Override
        public void setData(String key, String value) {
            setCacheInternal(key, value, true);
        }

        @Override
        public void removeData(String key) {
            removeCacheInternal(key);
        }
    }
}
