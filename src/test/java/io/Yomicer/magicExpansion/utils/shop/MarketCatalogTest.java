package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

class MarketCatalogTest {
    @TempDir Path directory;
    private final List<String> warnings = new ArrayList<>();
    private final List<ShopManager.Shop> shops = new ArrayList<>();
    private ShopFileStore files;
    private MarketCatalog.Store store;
    private boolean plugins = true;
    private String missing;
    private final MarketCatalog.Resolver resolver = new MarketCatalog.Resolver() {
        public boolean pluginEnabled(String name) { return plugins; }
        public ItemStack resolve(MarketCatalog.ItemSpec spec) {
            if (spec.slimefun() != null && spec.slimefun().equals(missing)) return null;
            ItemStack item = new ItemStack(spec.material() == null ? Material.DIAMOND : Material.valueOf(spec.material()));
            item.setAmount(spec.amount());
            return item;
        }
    };
    @BeforeEach void start() throws Exception {
        MockBukkit.mock();
        files = new ShopFileStore(directory, warnings::add);
        files.load();
        store = new MarketCatalog.Store() {
            public List<ShopManager.Shop> shops() { return shops; }
            public boolean available(ShopManager.Shop shop) { return files.isAvailable(shop); }
            public boolean save(ShopManager.Shop shop) { return files.save(shop); }
            public void register(ShopManager.Shop shop) { shops.add(shop); }
        };
    }
    @AfterEach void stop() { MockBukkit.unmock(); }
    private String fixture() {
        return """
                version: 1
                shops:
                  - name: Example
                    offers:
                      - id: example.cargo.v1
                        result: {slimefun: CARGO_NODE, amount: 16}
                        cost: [{material: EMERALD, amount: 8}]
                        personalLimit: 16
                """;
    }
    private MarketCatalog.Result install(String text) throws Exception {
        return MarketCatalog.install(MarketCatalog.parse(text), resolver, store, warnings::add);
    }
    @Test void bundledCatalogHasUniqueOffersAndOnlyBoundedBulkExceptions() throws Exception {
        String source = Files.readString(Path.of("src/main/resources/market_catalog.yml"));
        var definition = MarketCatalog.parse(source);
        assertTrue(definition.sections().size() >= 19);
        var offers = definition.sections().stream().flatMap(s -> s.offers().stream()).toList();
        assertTrue(offers.size() >= 207);
        assertEquals(offers.size(), new HashSet<>(offers.stream().map(MarketCatalog.Offer::id).toList()).size());
        assertEquals(56, offers.stream().filter(o -> !o.plugins().isEmpty()).count());
        assertTrue(offers.stream().filter(o -> o.personalLimit() > 1).count() > 6);
        assertTrue(offers.stream().filter(o -> o.personalLimit() > 1).allMatch(o -> o.personalLimit() <= 16));
        assertTrue(offers.stream().allMatch(o -> o.personalLimit() > 0 && !o.costs().isEmpty()));
        var node = offers.stream().filter(o -> o.result().slimefun().equals("CARGO_NODE")).findFirst().orElseThrow();
        assertEquals(256, node.result().amount() * node.personalLimit());
    }
    @Test void populationIsIdempotentAndDoesNotRewriteInstalledFiles() throws Exception {
        assertEquals(1, install(fixture()).added());
        byte[] original = Files.readAllBytes(directory.resolve("Example.yml"));
        var result = install(fixture());
        assertEquals(0, result.added()); assertEquals(1, result.existing());
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("Example.yml")));
    }
    @Test void generatorPreservesBothPlayersCountersAndAdministrativePriceEdits() throws Exception {
        install(fixture());
        var shop = shops.getFirst(); var trade = shop.trades.getFirst();
        UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        trade.personalUsed.put(a, 16); trade.personalUsed.put(b, 3); trade.globalUsed=19;
        trade.costItems = new ArrayList<>(List.of(new ItemStack(Material.GOLD_INGOT, 23)));
        trade.personalLimit=20;
        assertTrue(files.save(shop), warnings::toString);
        install(fixture());
        assertSame(trade, shop.trades.getFirst());
        assertEquals(Map.of(a,16,b,3), trade.personalUsed);
        assertEquals(19,trade.globalUsed); assertEquals(20,trade.personalLimit);
        assertEquals(Material.GOLD_INGOT,trade.costItems.getFirst().getType());
        assertEquals(23,trade.costItems.getFirst().getAmount());
    }
    @Test void deletedCatalogOfferRemainsDeletedAfterPopulateAndRestart() throws Exception {
        install(fixture()); var shop=shops.getFirst(); shop.trades.clear();
        assertTrue(files.save(shop), warnings::toString);
        shops.clear(); shops.addAll(files.load());
        var result=install(fixture());
        assertEquals(1,result.existing()); assertEquals(0,result.added());
        assertTrue(shops.getFirst().trades.isEmpty());
        assertTrue(shops.getFirst().catalogHistory.contains("example.cargo.v1"));
    }
    @Test void newOfferAppendsWithoutChangingOldObjectOrUsage() throws Exception {
        install(fixture()); var old=shops.getFirst().trades.getFirst();
        UUID owner=UUID.randomUUID();old.personalUsed.put(owner,9);assertTrue(files.save(shops.getFirst()));
        assertEquals(1,install(fixture().replace("example.cargo.v1","example.second.v1")).added());
        assertSame(old,shops.getFirst().trades.getFirst()); assertEquals(9,old.personalUsed.get(owner));
        assertEquals(2,shops.getFirst().catalogHistory.size());
    }
    @Test void missingPluginSkipsWholeShopWithoutCreatingAnEmptyFile() throws Exception {
        plugins=false;
        var result=install(fixture().replace("offers:","requiresPlugins: [OptionalAddon]\n    offers:"));
        assertEquals(1,result.optionalSkipped());assertEquals(0,result.added());
        assertTrue(shops.isEmpty());assertFalse(Files.exists(directory.resolve("Example.yml")));
    }
    @Test void missingCostCannotBecomeAFreeOrPartiallyPaidTrade() throws Exception {
        missing="MISSING_COST";
        var result=install(fixture().replace("{material: EMERALD, amount: 8}","{material: EMERALD, amount: 8}, {slimefun: MISSING_COST, amount: 1}"));
        assertEquals(1,result.unresolved());assertEquals(0,result.added());assertTrue(shops.isEmpty());
    }
    @Test void missingRewardDoesNotSpendTheCatalogIdentity() throws Exception {
        missing="CARGO_NODE"; assertEquals(1,install(fixture()).unresolved());
        missing=null; assertEquals(1,install(fixture()).added());
    }
    @Test void changedShopFileRefusesAppendAndRollsBackMemory() throws Exception {
        install(fixture()); var shop=shops.getFirst();
        String external=Files.readString(directory.resolve("Example.yml"))+"\n# External edit\n";
        Files.writeString(directory.resolve("Example.yml"),external);
        var result=install(fixture().replace("example.cargo.v1","example.new.v1"));
        assertEquals(1,result.failedShops());assertEquals(0,result.added());
        assertEquals(1,shop.trades.size()); assertEquals(1,shop.catalogHistory.size());
        assertEquals(external,Files.readString(directory.resolve("Example.yml")));
    }
    @Test void unreadableExistingFileCannotBeReplacedWithFreshCounters() throws Exception {
        Path path=directory.resolve("Example.yml");Files.writeString(path,"trades: [broken\n");
        files.load();
        assertEquals(1,install(fixture()).failedShops());assertTrue(shops.isEmpty());
        assertEquals("trades: [broken\n",Files.readString(path));
    }
    @Test void duplicateInstalledIdentityRefusesBeforeAnyAppend() throws Exception {
        install(fixture());var duplicate=new ShopManager.Shop();duplicate.name="Other";
        duplicate.catalogHistory.add("example.cargo.v1");shops.add(duplicate);
        assertThrows(IOException.class,()->install(fixture().replace("example.cargo.v1","example.new.v1")));
        assertEquals(1,shops.getFirst().trades.size());
    }
    @Test void duplicateDefinitionIdsAndUnknownKeysAreRejected() {
        assertThrows(IOException.class,()->MarketCatalog.parse(fixture().replace("personalLimit: 16","personalLimit: 16\n        typo: true")));
        assertThrows(IOException.class,()->MarketCatalog.parse(fixture()+"  - name: Other\n    offers:\n      - id: example.cargo.v1\n        result: {material: STONE}\n        cost: [{material: EMERALD}]\n"));
    }
    @Test void invalidCountsAndLimitsNeverBecomeUnlimited() {
        for(String bad:List.of("0","-1","1.5","2147483648","'16'")) {
            assertThrows(IOException.class,()->MarketCatalog.parse(fixture().replace("personalLimit: 16","personalLimit: "+bad)));
        }
        assertThrows(IOException.class,()->MarketCatalog.parse(fixture().replace("amount: 16","amount: 0")));
        assertThrows(IOException.class,()->MarketCatalog.parse(fixture().replace("amount: 16","amount: 65")));
    }
    @Test void emptyCostsAndAmbiguousItemReferencesAreRejected() {
        assertThrows(IOException.class,()->MarketCatalog.parse(fixture().replace("[{material: EMERALD, amount: 8}]","[]")));
        assertThrows(IOException.class,()->MarketCatalog.parse(fixture().replace("slimefun: CARGO_NODE","slimefun: CARGO_NODE, material: STONE")));
    }
    @Test void losingImportHistoryIsAReadOnlyErrorNotAReconstruction() throws Exception {
        install(fixture());Path path=directory.resolve("Example.yml");
        var yaml=new org.bukkit.configuration.file.YamlConfiguration();yaml.load(path.toFile());
        yaml.set("marketCatalogHistory",null);yaml.save(path.toFile());
        byte[] original=Files.readAllBytes(path);
        assertTrue(files.load().isEmpty());assertFalse(files.save(shops.getFirst()));
        assertArrayEquals(original,Files.readAllBytes(path));
    }
}
