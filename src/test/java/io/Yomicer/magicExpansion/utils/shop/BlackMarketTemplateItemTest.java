package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;

/** Subclass boundary coverage; the native audit additionally uses every real Slimefun template. */
class BlackMarketTemplateItemTest {
    @TempDir Path root;
    private BlackMarketFileStore store;
    private BlackMarketFileStore.Cycle cycle;
    private final UUID owner = UUID.randomUUID();

    @BeforeEach void setUp() throws Exception {
        MockBukkit.mock();
        store = new BlackMarketFileStore(root.resolve("market"));
        cycle = store.loadOrCreate(1_790_000_000_000L);
        assertNull(store.loadPlayer(owner, cycle));
    }

    @AfterEach void tearDown() { MockBukkit.unmock(); }

    @Test void subclassRewardsAndCostsKeepTheirDataWithoutPersistingTheTemplateClass() throws Exception {
        TemplateStack template = new TemplateStack(Material.DIAMOND, 7);
        var meta = template.getItemMeta();
        meta.setDisplayName("Named template");
        meta.setLore(List.of("Keep this lore", "Including its second line"));
        meta.getPersistentDataContainer().set(new NamespacedKey("slimefun", "slimefun_item"),
                PersistentDataType.STRING, "EXISTING_ITEM_ID");
        meta.getPersistentDataContainer().set(new NamespacedKey("audit", "payload"),
                PersistentDataType.BYTE_ARRAY, new byte[]{4, 2, 9, 0});
        template.setItemMeta(meta);
        ItemStack before = new ItemStack(template);
        var trades = offers(template);
        store.savePlayer(owner, cycle, new BlackMarketFileStore.PlayerState(trades, Set.of(2), Set.of(2, 4)));
        Path ledger = root.resolve("market/players/" + owner + ".yml");
        assertFalse(Files.readString(ledger).contains("TemplateStack"));
        assertEquals(before, template, "Do not mutate the registered template");
        var reboot = new BlackMarketFileStore(root.resolve("market"));
        var loaded = reboot.loadPlayer(owner, reboot.loadOrCreate(1_790_000_000_001L));
        assertEquals(Set.of(2), loaded.purchased());
        assertEquals(Set.of(2, 4), loaded.revealed());
        assertEquals(before, loaded.trades().get(0).result);
        assertEquals(List.of(before, before), loaded.trades().get(0).costs);
        assertFalse(loaded.trades().get(0).result instanceof TemplateStack);
        assertArrayEquals(new byte[]{4, 2, 9, 0}, loaded.trades().get(0).result.getItemMeta()
                .getPersistentDataContainer().get(new NamespacedKey("audit", "payload"), PersistentDataType.BYTE_ARRAY));
        loaded.trades().get(0).result.setAmount(1);
        assertEquals(7, template.getAmount(), "Saved/read copies must not alias the template");
    }

    @Test void plainBukkitRewardsRemainReadableWithTheExistingFormat() throws Exception {
        var trades = offers(new ItemStack(Material.IRON_INGOT, 9));
        store.savePlayer(owner, cycle, new BlackMarketFileStore.PlayerState(trades, Set.of(), Set.of()));
        var loaded = store.loadPlayer(owner, cycle);
        assertEquals(trades.get(0).result, loaded.trades().get(0).result);
        assertEquals(trades.get(0).costs, loaded.trades().get(0).costs);
        assertTrue(Files.readString(root.resolve("market/players/" + owner + ".yml")).startsWith("format: 1\n"));
    }

    @Test void invalidAirTemplateStillFailsWithoutCreatingAReceipt() {
        assertThrows(IOException.class, () -> store.savePlayer(owner, cycle,
                new BlackMarketFileStore.PlayerState(offers(new TemplateStack(Material.AIR, 1)), Set.of(), Set.of())));
        assertFalse(Files.exists(root.resolve("market/players/" + owner + ".yml")));
    }

    @Test void invalidZeroQuantityTemplateStillFailsWithoutCreatingAReceipt() {
        assertThrows(IOException.class, () -> store.savePlayer(owner, cycle,
                new BlackMarketFileStore.PlayerState(offers(new TemplateStack(Material.DIAMOND, 0)), Set.of(), Set.of())));
        assertFalse(Files.exists(root.resolve("market/players/" + owner + ".yml")));
    }

    private static List<BlackMarketManager.BlackMarketTrade> offers(ItemStack template) {
        var result = new ArrayList<BlackMarketManager.BlackMarketTrade>();
        for (int i = 0; i < 10; i++) {
            var trade = new BlackMarketManager.BlackMarketTrade();
            trade.result = i == 0 ? template : new ItemStack(Material.EMERALD, i + 1);
            trade.costs = List.of(template, template);
            trade.isFree = false;
            trade.isHard = true;
            result.add(trade);
        }
        return result;
    }

    public static final class TemplateStack extends ItemStack {
        TemplateStack(Material material, int amount) { super(material, amount); }
        @Override public TemplateStack clone() { return (TemplateStack) super.clone(); }
    }
}
