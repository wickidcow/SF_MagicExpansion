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

class BlackMarketMissingCycleTest {
    private static final long START = 1_750_000_000_000L;
    @TempDir Path temporary;
    private Path root;

    @BeforeEach void setUp() {
        MockBukkit.mock();
        root = temporary.resolve("black_market");
    }

    @AfterEach void tearDown() {
        MockBukkit.unmock();
    }

    @Test void missingMarkerWithReceiptsLocksRestartWithoutChangingSavedItems() throws Exception {
        var files = new BlackMarketFileStore(root);
        var cycle = files.loadOrCreate(START);
        UUID owner = UUID.randomUUID();
        assertNull(files.loadPlayer(owner, cycle));
        var saved = new BlackMarketFileStore.PlayerState(offers(), Set.of(2, 7), Set.of(2, 5, 7));
        files.savePlayer(owner, cycle, saved);
        Path marker = root.resolve("cycle.yml");
        Path ledger = root.resolve("players").resolve(owner + ".yml");
        byte[] markerBytes = Files.readAllBytes(marker);
        byte[] ledgerBytes = Files.readAllBytes(ledger);
        Files.delete(marker);

        var restarted = new BlackMarketFileStore(root);
        IOException failure = assertThrows(IOException.class, () -> restarted.loadOrCreate(START + 60_000));
        assertTrue(failure.getMessage().contains("refusing to reset purchase limits"));
        assertFalse(Files.exists(marker), "A new cycle would invalidate existing purchase receipts");
        assertArrayEquals(ledgerBytes, Files.readAllBytes(ledger));
        assertThrows(IOException.class, () -> restarted.loadPlayer(owner, cycle));
        assertThrows(IOException.class, () -> restarted.refresh(cycle, START + 60_001));
        try (var entries = Files.list(root)) {
            assertEquals(List.of("players"), entries.map(path -> path.getFileName().toString()).sorted().toList());
        }

        // Restore the exact known-good marker in this synthetic fixture, not a generated replacement.
        Files.write(marker, markerBytes);
        var recovered = new BlackMarketFileStore(root);
        assertEquals(cycle, recovered.loadOrCreate(START + 60_002));
        var restored = recovered.loadPlayer(owner, cycle);
        assertNotNull(restored);
        assertEquals(saved.purchased(), restored.purchased());
        assertEquals(saved.revealed(), restored.revealed());
        for (int i = 0; i < 10; i++) {
            assertEquals(saved.trades().get(i).result, restored.trades().get(i).result);
            assertEquals(saved.trades().get(i).costs, restored.trades().get(i).costs);
            assertEquals(saved.trades().get(i).isFree, restored.trades().get(i).isFree);
            assertEquals(saved.trades().get(i).isHard, restored.trades().get(i).isHard);
        }
        assertArrayEquals(markerBytes, Files.readAllBytes(marker));
        assertArrayEquals(ledgerBytes, Files.readAllBytes(ledger));
    }

    @Test void orphanedMalformedLedgerIsPreservedWithoutParsingOrReplacingIt() throws Exception {
        Path players = Files.createDirectories(root.resolve("players"));
        Path ledger = players.resolve(UUID.randomUUID() + ".yml");
        byte[] damaged = new byte[] {(byte) 0xff, 0, 17};
        Files.write(ledger, damaged);
        var files = new BlackMarketFileStore(root);
        assertThrows(IOException.class, () -> files.loadOrCreate(START));
        assertArrayEquals(damaged, Files.readAllBytes(ledger));
        assertFalse(Files.exists(root.resolve("cycle.yml")));
    }

    @Test void pendingCycleWriteBlocksBootstrapWithoutAnyPlayerRecords() throws Exception {
        Files.createDirectories(root.resolve("players"));
        Path pending = Files.writeString(root.resolve(".black-market-test.pending"), "preserve");
        var files = new BlackMarketFileStore(root);
        assertThrows(IOException.class, () -> files.loadOrCreate(START));
        assertEquals("preserve", Files.readString(pending));
        assertFalse(Files.exists(root.resolve("cycle.yml")));
    }

    @Test void precreatedEmptyDirectoriesStillAllowFirstInstallAndRestart() throws Exception {
        Files.createDirectories(root.resolve("players"));
        var first = new BlackMarketFileStore(root).loadOrCreate(START);
        var second = new BlackMarketFileStore(root).loadOrCreate(START + 1);
        assertEquals(first, second);
    }

    private static List<BlackMarketManager.BlackMarketTrade> offers() {
        List<BlackMarketManager.BlackMarketTrade> offers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var trade = new BlackMarketManager.BlackMarketTrade();
            trade.result = new ItemStack(Material.DIAMOND, i + 1);
            var meta = trade.result.getItemMeta();
            assertNotNull(meta);
            meta.getPersistentDataContainer().set(new NamespacedKey("test", "receipt-identity"),
                    PersistentDataType.STRING, "existing-offer-" + i);
            trade.result.setItemMeta(meta);
            trade.costs = List.of(new ItemStack(Material.IRON_INGOT, i + 1));
            trade.isFree = false;
            trade.isHard = i % 2 == 0;
            offers.add(trade);
        }
        return offers;
    }
}
