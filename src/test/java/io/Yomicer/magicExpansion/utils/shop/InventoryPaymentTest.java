package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class InventoryPaymentTest {
    private ServerMock server;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        inventory = server.createInventory(null, 9);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void repeatedCostsCannotSpendTheSameStackTwice() {
        inventory.setItem(0, item(Material.DIAMOND, 50));
        var costs = List.of(item(Material.DIAMOND, 40), item(Material.DIAMOND, 40));
        var before = snapshot(inventory);
        assertTrue(costs.stream().allMatch(cost -> inventory.containsAtLeast(cost, cost.getAmount())),
                "The old independent preflight must reproduce the underpayment");
        ItemStack missing = InventoryPayment.debit(inventory, costs);
        assertNotNull(missing);
        assertEquals(30, missing.getAmount());
        assertArrayEquals(before, inventory.getStorageContents());
    }

    @Test
    void successfulRepeatedCostsDebitTheirCombinedQuantity() {
        inventory.setItem(0, item(Material.DIAMOND, 64));
        inventory.setItem(1, item(Material.DIAMOND, 30));
        assertNull(InventoryPayment.debit(inventory, List.of(item(Material.DIAMOND, 40), item(Material.DIAMOND, 40))));
        assertEmpty(inventory.getItem(0));
        assertEquals(14, inventory.getItem(1).getAmount());
    }

    @Test
    void missingLaterIngredientDoesNotTakeEarlierIngredients() {
        inventory.setItem(0, item(Material.DIAMOND, 20));
        inventory.setItem(1, item(Material.EMERALD, 2));
        var before = snapshot(inventory);
        assertNotNull(InventoryPayment.debit(inventory, List.of(item(Material.DIAMOND, 20), item(Material.EMERALD, 3))));
        assertArrayEquals(before, inventory.getStorageContents());
    }

    @Test
    void insufficientEmptyInventoryIsUnchanged() {
        var before = snapshot(inventory);
        assertNotNull(InventoryPayment.debit(inventory, List.of(item(Material.DIAMOND, 1))));
        assertArrayEquals(before, inventory.getStorageContents());
    }

    @Test
    void freeAndIgnoredCostsDoNotRewriteStorage() {
        inventory.setItem(0, richItem("UNREGISTERED_OLD_ADDON", 17));
        var before = snapshot(inventory);
        assertNull(InventoryPayment.debit(inventory, null));
        assertNull(InventoryPayment.debit(inventory, List.of()));
        assertNull(InventoryPayment.debit(inventory, Arrays.asList(null, item(Material.AIR, 1))));
        assertArrayEquals(before, inventory.getStorageContents());
    }

    @Test
    void exactPaymentLeavesNoZeroAmountStack() {
        inventory.setItem(3, item(Material.GOLD_INGOT, 7));
        assertNull(InventoryPayment.debit(inventory, List.of(item(Material.GOLD_INGOT, 7))));
        assertEmpty(inventory.getItem(3));
    }

    @Test
    void spendsSlotsInTheHistoricalStorageOrder() {
        inventory.setItem(0, item(Material.GOLD_INGOT, 2));
        inventory.setItem(2, item(Material.GOLD_INGOT, 5));
        inventory.setItem(8, item(Material.GOLD_INGOT, 9));
        assertNull(InventoryPayment.debit(inventory, List.of(item(Material.GOLD_INGOT, 4))));
        assertEmpty(inventory.getItem(0));
        assertEquals(3, inventory.getItem(2).getAmount());
        assertEquals(9, inventory.getItem(8).getAmount());
    }

    @Test
    void preservesTypedOldItemMetadataAndUnrelatedStacks() {
        ItemStack old = richItem("UNREGISTERED_OLD_ADDON", 31);
        ItemStack unrelated = richItem("OTHER_ID", 19);
        inventory.setItem(0, old.clone());
        inventory.setItem(1, unrelated.clone());
        ItemStack cost = old.clone();
        cost.setAmount(7);
        assertNull(InventoryPayment.debit(inventory, List.of(cost)));
        ItemStack expected = old.clone();
        expected.setAmount(24);
        assertEquals(expected, inventory.getItem(0));
        assertEquals(unrelated, inventory.getItem(1));
        assertEquals(31, old.getAmount());
        assertEquals(7, cost.getAmount());
        assertTrue(inventory.getItem(0).getItemMeta().getPersistentDataContainer()
                .has(key("slimefun:item_charge"), PersistentDataType.FLOAT));
    }

    @Test
    void vanillaMaterialCannotPayForAnAddonItem() {
        inventory.setItem(0, item(Material.DIAMOND, 64));
        var before = snapshot(inventory);
        assertNotNull(InventoryPayment.debit(inventory, List.of(richItem("OLD_ID", 2))));
        assertArrayEquals(before, inventory.getStorageContents());
    }

    @Test
    void sameMaterialWithDifferentOwnersIsNotInterchangeable() {
        inventory.setItem(0, richItem("OLD_ID", 20));
        ItemStack cost = richItem("OLD_ID", 2);
        var meta = cost.getItemMeta();
        meta.getPersistentDataContainer().set(key("slimefun:owner_uuid"), PersistentDataType.STRING, "other");
        cost.setItemMeta(meta);
        assertNotNull(InventoryPayment.debit(inventory, List.of(cost)));
        assertEquals(20, inventory.getItem(0).getAmount());
    }

    @Test
    void doesNotUseArmorOrOffHandToCompletePayment() {
        var player = server.addPlayer();
        var inv = player.getInventory();
        inv.setItem(0, item(Material.DIAMOND, 50));
        inv.setItemInOffHand(item(Material.DIAMOND, 40));
        var helmet = item(Material.DIAMOND_HELMET, 1);
        inv.setHelmet(helmet);
        assertNotNull(InventoryPayment.debit(inv, List.of(item(Material.DIAMOND, 40), item(Material.DIAMOND, 40))));
        assertEquals(50, inv.getItem(0).getAmount());
        assertEquals(40, inv.getItemInOffHand().getAmount());
        assertEquals(helmet, inv.getHelmet());
    }

    @Test
    void repeatedReferencesInCostListStillRequireRepeatedPayment() {
        ItemStack shared = item(Material.DIAMOND, 30);
        inventory.setItem(0, item(Material.DIAMOND, 40));
        assertNotNull(InventoryPayment.debit(inventory, List.of(shared, shared)));
        assertEquals(30, shared.getAmount());
        assertEquals(40, inventory.getItem(0).getAmount());
    }

    @Test
    void largeRepeatedAmountsCannotOverflowIntoFreeTrades() {
        inventory.setItem(0, item(Material.DIAMOND, 64));
        var before = snapshot(inventory);
        assertNotNull(InventoryPayment.debit(inventory,
                List.of(item(Material.DIAMOND, Integer.MAX_VALUE), item(Material.DIAMOND, Integer.MAX_VALUE))));
        assertArrayEquals(before, inventory.getStorageContents());
    }

    @Test
    void returnedShortfallIsDetachedFromCostDefinition() {
        ItemStack cost = richItem("OLD_ID", 20);
        ItemStack missing = InventoryPayment.debit(inventory, List.of(cost));
        assertNotNull(missing);
        assertNotSame(cost, missing);
        missing.setAmount(1);
        assertEquals(20, cost.getAmount());
    }

    @Test
    void randomizedMaterialTradesConserveCountsAndRejectIncompletePayments() {
        var random = new Random(193742L);
        Material[] kinds = {Material.DIAMOND, Material.EMERALD, Material.IRON_INGOT};
        for (int sample = 0; sample < 1000; sample++) {
            inventory.clear();
            long[] available = new long[kinds.length];
            long[] requested = new long[kinds.length];
            for (int slot = 0; slot < 9; slot++) {
                int kind = random.nextInt(kinds.length), count = random.nextInt(65);
                if (count != 0) inventory.setItem(slot, item(kinds[kind], count));
                available[kind] += count;
            }
            var costs = new java.util.ArrayList<ItemStack>();
            for (int index = 0, count = random.nextInt(10); index < count; index++) {
                int kind = random.nextInt(kinds.length), amount = 1 + random.nextInt(80);
                costs.add(item(kinds[kind], amount));
                requested[kind] += amount;
            }
            var before = snapshot(inventory);
            boolean affordable = true;
            for (int kind = 0; kind < kinds.length; kind++) affordable &= available[kind] >= requested[kind];
            assertEquals(affordable, InventoryPayment.debit(inventory, costs) == null, "sample " + sample);
            if (!affordable) {
                assertArrayEquals(before, inventory.getStorageContents());
            } else {
                for (int kind = 0; kind < kinds.length; kind++) {
                    long actual = 0;
                    for (ItemStack stack : inventory.getStorageContents()) {
                        if (stack != null && stack.getType() == kinds[kind]) actual += stack.getAmount();
                    }
                    assertEquals(available[kind] - requested[kind], actual);
                }
            }
        }
    }

    private static ItemStack[] snapshot(Inventory inv) {
        return Arrays.stream(inv.getStorageContents()).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new);
    }

    private static void assertEmpty(ItemStack item) {
        assertTrue(item == null || item.getType().isAir());
    }

    private static ItemStack item(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    private static NamespacedKey key(String name) {
        return Objects.requireNonNull(NamespacedKey.fromString(name));
    }

    private static ItemStack richItem(String id, int amount) {
        ItemStack item = item(Material.DIAMOND, amount);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Existing named item"));
        meta.lore(List.of(Component.text("Owner lore; keep unchanged")));
        var pdc = meta.getPersistentDataContainer();
        pdc.set(key("slimefun:slimefun_item"), PersistentDataType.STRING, id);
        pdc.set(key("slimefun:owner_uuid"), PersistentDataType.STRING, "2f017f3a-8442-4ef2-9fba-456789abcdef");
        pdc.set(key("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);
        pdc.set(key("other:storage_count"), PersistentDataType.LONG, 9_007_199_254_740_993L);
        pdc.set(key("other:opaque"), PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 4});
        item.setItemMeta(meta);
        return item;
    }
}
