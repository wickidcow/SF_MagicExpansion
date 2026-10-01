package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

/** Real Bukkit YAML, real item metadata and real temporary files; no live-world coverage claimed. */
class ShopFileStoreTest {
    @TempDir Path temporary;
    private Path directory;
    private List<String> errors;
    private ShopFileStore store;

    @BeforeEach void setUp() {
        MockBukkit.mock();
        directory = temporary.resolve("portable_shops");
        errors = new ArrayList<>();
        store = new ShopFileStore(directory, errors::add);
    }

    @AfterEach void tearDown() { MockBukkit.unmock(); }

    @Test void newDirectoryAndExplicitEmptyShopRemainValid() throws Exception {
        assertTrue(store.load().isEmpty());
        var shop = shop("Empty");
        assertTrue(store.save(shop), () -> errors.toString());
        assertTrue(store.isAvailable(shop));
        assertTrue(store.load().getFirst().trades.isEmpty());
        assertTrue(errors.isEmpty());
    }

    @Test void corruptYamlCannotBecomeAnEmptyShopOrBeOverwritten() throws Exception {
        Path path = write("Broken.yml", "trades: [unterminated\n");
        byte[] original = Files.readAllBytes(path);
        // Show why the historical API was unsafe for deciding whether a shop is empty.
        assertTrue(YamlConfiguration.loadConfiguration(path.toFile()).getMapList("trades").isEmpty());
        assertTrue(store.load().isEmpty());
        assertFalse(store.save(shop("Broken")));
        assertArrayEquals(original, Files.readAllBytes(path));
        assertFalse(errors.isEmpty());
    }

    @Test void blankOrWrongRootCannotEraseUnknownData() throws Exception {
        for (String content : List.of("", "# comment only\n", "owner: keep-me\n", "trades: wrong\n", "trades: {}\n")) {
            Path path = write("Root.yml", content);
            assertTrue(store.load().isEmpty());
            assertFalse(store.save(shop("Root")));
            assertEquals(content, Files.readString(path));
        }
    }

    @Test void malformedUtf8IsPreserved() throws Exception {
        Path path = write("Bytes.yml", "trades: []\n");
        byte[] original = {(byte) 0xff, (byte) 0xfe};
        Files.write(path, original);
        assertTrue(store.load().isEmpty());
        assertFalse(store.save(shop("Bytes")));
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    @Test void healthyFilesRemainUsableBesideAnUnreadableShop() throws Exception {
        write("Good.yml", "trades: []\n");
        Path bad = write("Bad.yml", "trades: [\n");
        byte[] original = Files.readAllBytes(bad);
        var shops = store.load();
        assertEquals(1, shops.size());
        assertEquals("Good", shops.getFirst().name);
        assertTrue(store.save(shops.getFirst()));
        assertArrayEquals(original, Files.readAllBytes(bad));
    }

    @Test void invalidTradeRowsAreRejectedAsAWhole() throws Exception {
        for (Object invalid : List.of("bad", 42, Map.of("result", "missing item"),
                Map.of("cost", "not list"), Map.of("cost", List.of("not item")))) {
            var config = new YamlConfiguration();
            config.set("trades", List.of(Map.of("globalUsed", 5), invalid));
            Path path = write("Rows.yml", config.saveToString());
            byte[] original = Files.readAllBytes(path);
            assertTrue(store.load().isEmpty());
            assertFalse(store.save(shop("Rows")));
            assertArrayEquals(original, Files.readAllBytes(path));
        }
    }

    @Test void fractionalOverflowAndTextCountsDoNotResetToZero() throws Exception {
        for (Object bad : List.of(2147483648L, -2147483649L, 1.5D, "12", Double.NaN)) {
            var config = new YamlConfiguration();
            config.set("trades", List.of(Map.of("globalUsed", bad)));
            Path path = write("Count.yml", config.saveToString());
            byte[] original = Files.readAllBytes(path);
            assertTrue(store.load().isEmpty(), "Value should be refused: " + bad);
            assertFalse(store.save(shop("Count")));
            assertArrayEquals(original, Files.readAllBytes(path));
        }
    }

    @Test void invalidOwnerUuidIsNotSilentlyDropped() throws Exception {
        for (Object usage : List.of("wrong", Map.of("not-a-uuid", 3), Map.of("1-1-1-1-1", 4),
                Map.of(UUID.randomUUID().toString(), "wrong"))) {
            var config = new YamlConfiguration();
            config.set("trades", List.of(Map.of("personalUsed", usage)));
            Path path = write("Owner.yml", config.saveToString());
            byte[] original = Files.readAllBytes(path);
            assertTrue(store.load().isEmpty());
            assertArrayEquals(original, Files.readAllBytes(path));
        }
    }

    @Test void duplicateUuidSpellingsDoNotMergeCounts() throws Exception {
        String owner = "afeb57c3-aeed-4900-b0a1-012345abcdef";
        Map<String, Integer> usage = new LinkedHashMap<>();
        usage.put(owner, 4);
        usage.put(owner.toUpperCase(java.util.Locale.ROOT), 5);
        var config = new YamlConfiguration();
        config.set("trades", List.of(Map.of("personalUsed", usage)));
        write("Owner.yml", config.saveToString());
        assertTrue(store.load().isEmpty());
    }

    @Test void exactOldItemsCostsCountersAndOwnersSurviveASeparateStoreReopen() throws Exception {
        store.load();
        var shop = richShop("Old");
        ItemStack reward = shop.trades.getFirst().result.clone();
        ItemStack cost = shop.trades.getFirst().costItems.getFirst().clone();
        Map<UUID, Integer> usage = new HashMap<>(shop.trades.getFirst().personalUsed);
        assertTrue(store.save(shop), () -> errors.toString());
        var reopened = new ShopFileStore(directory, errors::add).load().getFirst().trades.getFirst();
        assertEquals(reward, reopened.result);
        assertEquals(cost, reopened.costItems.getFirst());
        assertEquals(usage, reopened.personalUsed);
        assertEquals(19, reopened.globalUsed);
        assertEquals(43, reopened.globalLimit);
        assertEquals(25, reopened.personalLimit);
        var pdc = reopened.result.getItemMeta().getPersistentDataContainer();
        assertEquals(9_007_199_254_740_993L, pdc.get(key("old:count"), PersistentDataType.LONG));
        assertEquals("UNREGISTERED_OLD_ID", pdc.get(key("slimefun:slimefun_item"), PersistentDataType.STRING));
    }

    @Test void savingNeverMutatesCallerItemsOrCostLists() throws Exception {
        store.load();
        var shop = richShop("Clone");
        ItemStack result = shop.trades.getFirst().result.clone();
        List<ItemStack> costs = shop.trades.getFirst().costItems.stream().map(ItemStack::clone).toList();
        assertTrue(store.save(shop), () -> errors.toString());
        assertEquals(result, shop.trades.getFirst().result);
        assertEquals(costs, shop.trades.getFirst().costItems);
    }

    @Test void unknownRootAndTradeFieldsSurviveNormalSave() throws Exception {
        var config = new YamlConfiguration();
        config.set("owner-extension.payload", List.of("keep", 123L));
        config.set("trades", List.of(Map.of("globalUsed", 8, "future-field", Map.of("opaque", "unchanged"))));
        write("Extended.yml", config.saveToString());
        // YAML scalars have no Java Integer/Long tag; compare the actual persisted baseline.
        var persistedBefore = new YamlConfiguration();
        persistedBefore.load(directory.resolve("Extended.yml").toFile());
        var shop = store.load().getFirst();
        shop.trades.getFirst().globalUsed = 9;
        assertTrue(store.save(shop), () -> errors.toString());
        var reloaded = new YamlConfiguration();
        reloaded.load(directory.resolve("Extended.yml").toFile());
        assertEquals(persistedBefore.get("owner-extension.payload"), reloaded.get("owner-extension.payload"));
        assertEquals(Map.of("opaque", "unchanged"), reloaded.getMapList("trades").getFirst().get("future-field"));
    }

    @Test void loadedFilenameIsPreservedEvenWithYmlInItsStem() throws Exception {
        Path path = write("one.yml.two.yml", "trades: []\n");
        var shop = store.load().getFirst();
        assertEquals("one.yml.two", shop.name);
        assertTrue(store.save(shop), () -> errors.toString());
        assertTrue(Files.exists(path));
        assertFalse(Files.exists(directory.resolve("one.two.yml")));
    }

    @Test void externalEditCannotBeOverwrittenByAStaleSnapshot() throws Exception {
        Path path = write("Edit.yml", "trades: []\n");
        var shop = store.load().getFirst();
        String external = "trades: []\nowner-note: keep this\n";
        Files.writeString(path, external);
        assertFalse(store.save(shop));
        assertFalse(store.isAvailable(shop));
        assertEquals(external, Files.readString(path));
    }

    @Test void externallyDeletedFileIsNotRecreated() throws Exception {
        Path path = write("Deleted.yml", "trades: []\n");
        var shop = store.load().getFirst();
        Files.delete(path);
        assertFalse(store.save(shop));
        assertFalse(Files.exists(path));
    }

    @Test void reloadRetiresOldReferencesAndDoesNotRenameExistingShops() throws Exception {
        Path path = write("Name.yml", "trades: []\n");
        var old = store.load().getFirst();
        var fresh = store.load().getFirst();
        assertFalse(store.isAvailable(old));
        assertFalse(store.save(old));
        assertTrue(store.isAvailable(fresh));
        assertEquals("trades: []\n", Files.readString(path));
    }

    @Test void changedObjectNameCannotMoveOrOverwriteItsFile() throws Exception {
        Path path = write("Name.yml", "trades: []\n");
        var shop = store.load().getFirst();
        shop.name = "Other";
        assertFalse(store.save(shop));
        assertEquals("trades: []\n", Files.readString(path));
        assertFalse(Files.exists(directory.resolve("Other.yml")));
    }

    @Test void newNamesCannotAliasExistingFilesThroughSanitizing() throws Exception {
        Path existing = write("one_two.yml", "trades: []\n");
        store.load();
        assertFalse(store.save(shop("one:two")));
        assertFalse(store.save(shop("../outside")));
        assertFalse(store.save(shop("")));
        assertEquals("trades: []\n", Files.readString(existing));
        assertFalse(Files.exists(temporary.resolve("outside.yml")));
    }

    @Test void unknownObjectCannotReplaceLoadedShopWithSameName() throws Exception {
        Path path = write("Same.yml", "trades: []\n");
        store.load();
        assertFalse(store.save(richShop("Same")));
        assertEquals("trades: []\n", Files.readString(path));
    }

    @Test void invalidInMemoryTradeCannotEraseOriginal() throws Exception {
        Path path = write("Memory.yml", "trades: []\n");
        var shop = store.load().getFirst();
        shop.trades.add(null);
        assertFalse(store.save(shop));
        assertEquals("trades: []\n", Files.readString(path));
    }

    @Test void failureBeforeCommitLeavesOriginalAndRemovesTemporaryFile() throws Exception {
        Path path = write("IO.yml", "trades: []\n");
        store = new ShopFileStore(directory, errors::add, (staged, target) -> {
            assertTrue(Files.size(staged) > 0);
            assertEquals("trades: []\n", Files.readString(target));
            throw new IOException("injected disk failure");
        });
        var shop = store.load().getFirst();
        shop.trades.add(new ShopManager.Trade());
        assertFalse(store.save(shop));
        assertEquals("trades: []\n", Files.readString(path));
        assertFalse(store.isAvailable(shop));
        try (var files = Files.list(directory)) { assertEquals(List.of(path), files.toList()); }
    }

    @Test void unsupportedAtomicReplaceDoesNotFallBackToTruncatingOriginal() throws Exception {
        Path path = write("Atomic.yml", "trades: []\n");
        store = new ShopFileStore(directory, errors::add, (staged, target) -> {
            throw new AtomicMoveNotSupportedException(staged.toString(), target.toString(), "injected");
        });
        assertFalse(store.save(shop("Atomic"))); // No successful load yet.
        var shop = store.load().getFirst();
        assertFalse(store.save(shop));
        assertEquals("trades: []\n", Files.readString(path));
    }

    @Test void brokenDirectoryCannotMasqueradeAsAnEmptySuccessfulReload() throws Exception {
        Path path = write("Retain.yml", "trades: []\n");
        var shop = store.load().getFirst();
        Path moved = temporary.resolve("retained-directory");
        Files.move(directory, moved);
        Files.writeString(directory, "not a directory");
        assertThrows(IOException.class, store::load);
        assertFalse(store.isAvailable(shop));
        assertFalse(store.save(shop));
        assertEquals("trades: []\n", Files.readString(moved.resolve(path.getFileName())));
        assertEquals("not a directory", Files.readString(directory));
    }

    @Test void corruptFileCanBeRepairedExplicitlyThenReloaded() throws Exception {
        Path path = write("Repair.yml", "trades: [\n");
        assertTrue(store.load().isEmpty());
        Files.writeString(path, "trades: []\n");
        var repaired = store.load().getFirst();
        assertTrue(store.isAvailable(repaired));
        assertTrue(store.save(repaired));
    }

    @Test void deleteOnlyRemovesExactLoadedFileAndRetiresTheObject() throws Exception {
        Path path = write("Delete.yml", "trades: []\n");
        var shop = store.load().getFirst();
        assertTrue(store.delete(shop));
        assertFalse(Files.exists(path));
        assertFalse(store.save(shop));
        assertFalse(Files.exists(path));
    }

    @Test void deleteRefusesExternalChangesAndUnknownOrBrokenSources() throws Exception {
        Path path = write("Delete.yml", "trades: []\n");
        var shop = store.load().getFirst();
        Files.writeString(path, "trades: []\nextra: preserved\n");
        assertFalse(store.delete(shop));
        assertFalse(store.delete(shop("Delete")));
        assertEquals("trades: []\nextra: preserved\n", Files.readString(path));
    }

    @Test void symbolicLinksDoNotRedirectShopWritesOrDeletes() throws Exception {
        Files.createDirectories(directory);
        Path outside = temporary.resolve("outside.yml");
        Files.writeString(outside, "trades: []\n");
        Path link = directory.resolve("Link.yml");
        Files.createSymbolicLink(link, outside);
        assertTrue(store.load().isEmpty());
        assertFalse(store.save(shop("Link")));
        assertFalse(store.delete(shop("Link")));
        assertTrue(Files.isSymbolicLink(link));
        assertEquals("trades: []\n", Files.readString(outside));
    }

    @Test void symbolicLinkDirectoryIsNotFollowed() throws Exception {
        Path outside = temporary.resolve("outside");
        Files.createDirectories(outside);
        Files.createSymbolicLink(directory, outside);
        assertThrows(IOException.class, store::load);
        assertFalse(store.save(shop("Nope")));
        try (var files = Files.list(outside)) { assertEquals(0, files.count()); }
    }

    @Test void unsupportedMockContainerIsRefusedWithoutReplacingExistingData() throws Exception {
        // MockBukkit emits an unsafe Java YAML tag for nested PDC. Real Paper is covered
        // by the server regression; do not weaken production YAML validation for this fixture.
        store.load();
        var shop = richShop("UnsupportedMock");
        assertTrue(store.save(shop), () -> errors.toString());
        byte[] original = Files.readAllBytes(directory.resolve("UnsupportedMock.yml"));
        ItemStack item = shop.trades.getFirst().result;
        var meta = item.getItemMeta();
        var data = meta.getPersistentDataContainer();
        var nested = data.getAdapterContext().newPersistentDataContainer();
        nested.set(key("old:nested-owner"), PersistentDataType.STRING, "unchanged");
        data.set(key("old:nested"), PersistentDataType.TAG_CONTAINER, nested);
        item.setItemMeta(meta);
        ItemStack before = item.clone();
        assertFalse(store.save(shop));
        assertEquals(before, item);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("UnsupportedMock.yml")));
        assertFalse(store.isAvailable(shop));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Global tag is not allowed")), () -> errors.toString());
    }

    @Test void mockFloatTypeCoercionIsRefusedWithoutMutatingSourceOrFile() throws Exception {
        store.load();
        var shop = richShop("LossyMock");
        assertTrue(store.save(shop), () -> errors.toString());
        byte[] original = Files.readAllBytes(directory.resolve("LossyMock.yml"));
        ItemStack item = shop.trades.getFirst().result;
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);
        item.setItemMeta(meta);
        ItemStack before = item.clone();
        var encoded = new YamlConfiguration();
        encoded.set("item", item);
        var decoded = new YamlConfiguration();
        decoded.loadFromString(encoded.saveToString());
        var decodedData = decoded.getItemStack("item").getItemMeta().getPersistentDataContainer();
        assertFalse(decodedData.has(key("slimefun:item_charge"), PersistentDataType.FLOAT));
        assertTrue(decodedData.has(key("slimefun:item_charge"), PersistentDataType.DOUBLE));
        assertNotEquals(item, decoded.getItemStack("item"));
        assertFalse(store.save(shop));
        assertEquals(before, item);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("LossyMock.yml")));
        assertFalse(store.isAvailable(shop));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Trade data changed during serialization")), () -> errors.toString());
    }

    @Test void mockArrayReferenceEqualityCannotBypassExactRoundTripGuard() throws Exception {
        store.load();
        var shop = richShop("ArrayMock");
        assertTrue(store.save(shop), () -> errors.toString());
        byte[] original = Files.readAllBytes(directory.resolve("ArrayMock.yml"));
        ItemStack item = shop.trades.getFirst().result;
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key("old:bytes"), PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 4});
        item.setItemMeta(meta);
        ItemStack before = item.clone();
        var encoded = new YamlConfiguration();
        encoded.set("item", item);
        var decoded = new YamlConfiguration();
        decoded.loadFromString(encoded.saveToString());
        // The pinned mock compares its backing Map values by equals, so arrays compare
        // by reference even though their bytes survive. Production must not special-case it.
        assertArrayEquals(new byte[] {0, -1, 4}, decoded.getItemStack("item").getItemMeta()
                .getPersistentDataContainer().get(key("old:bytes"), PersistentDataType.BYTE_ARRAY));
        assertNotEquals(item, decoded.getItemStack("item"));
        assertFalse(store.save(shop));
        assertEquals(before, item);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("ArrayMock.yml")));
        assertFalse(store.isAvailable(shop));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Trade data changed during serialization")), () -> errors.toString());
    }

    private Path write(String name, String contents) throws IOException {
        Files.createDirectories(directory);
        Path path = directory.resolve(name);
        Files.writeString(path, contents, StandardCharsets.UTF_8);
        return path;
    }

    private static ShopManager.Shop shop(String name) {
        var shop = new ShopManager.Shop(); shop.name = name; return shop;
    }

    private static ShopManager.Shop richShop(String name) {
        var shop = shop(name);
        var trade = new ShopManager.Trade();
        trade.result = richItem(37);
        trade.costItems = List.of(richItem(7), richItem(9));
        trade.globalLimit = 43; trade.personalLimit = 25; trade.globalUsed = 19;
        trade.personalUsed.put(UUID.fromString("f55799ac-2dd7-42f0-91a2-123456abcdef"), 17);
        shop.trades.add(trade);
        return shop;
    }

    private static NamespacedKey key(String text) { return java.util.Objects.requireNonNull(NamespacedKey.fromString(text)); }

    private static ItemStack richItem(int amount) {
        var item = new ItemStack(Material.DIAMOND, amount);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Owner's original item"));
        meta.lore(List.of(Component.text("Existing lore; preserve exactly")));
        var data = meta.getPersistentDataContainer();
        data.set(key("slimefun:slimefun_item"), PersistentDataType.STRING, "UNREGISTERED_OLD_ID");
        // FLOAT, byte-array and nested PDC success are tested on real Paper; this mock has distinct type/equality behavior.
        data.set(key("old:count"), PersistentDataType.LONG, 9_007_199_254_740_993L);
        item.setItemMeta(meta);
        return item;
    }
}
