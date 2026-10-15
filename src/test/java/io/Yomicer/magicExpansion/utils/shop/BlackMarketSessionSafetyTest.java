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

class BlackMarketSessionSafetyTest {
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

    @Test void failedReloadCannotReuseThePreviousSnapshotAfterFilesAreRestored() throws Exception {
        var files = new BlackMarketFileStore(root);
        var cycle = files.loadOrCreate(START);
        Path marker = root.resolve("cycle.yml");
        byte[] original = Files.readAllBytes(marker);
        Files.writeString(marker, "format: invalid\n");
        assertThrows(IOException.class, () -> files.loadOrCreate(START + 1));
        Files.write(marker, original); // Synthetic offline restoration; not automatic recovery.

        assertEquals(BlackMarketFileStore.CycleStatus.NOT_LOADED, files.inspectCycle(cycle).status());
        assertThrows(IOException.class, () -> files.loadPlayer(UUID.randomUUID(), cycle));
        assertThrows(IOException.class, () -> files.refresh(cycle, START + 2));
        assertArrayEquals(original, Files.readAllBytes(marker));
        assertEquals(cycle, files.loadOrCreate(START + 3));
    }

    @Test void directoryFailureDuringReloadAlsoInvalidatesThePreviousSnapshot() throws Exception {
        var files = new BlackMarketFileStore(root);
        var cycle = files.loadOrCreate(START);
        byte[] original = Files.readAllBytes(root.resolve("cycle.yml"));
        Path players = root.resolve("players");
        Files.delete(players);
        Files.writeString(players, "preserve invalid directory evidence");
        assertThrows(IOException.class, () -> files.loadOrCreate(START + 1));
        assertEquals("preserve invalid directory evidence", Files.readString(players));
        Files.delete(players);
        Files.createDirectory(players);

        assertThrows(IOException.class, () -> files.loadPlayer(UUID.randomUUID(), cycle));
        assertThrows(IOException.class, () -> files.refresh(cycle, START + 2));
        assertArrayEquals(original, Files.readAllBytes(root.resolve("cycle.yml")));
    }

    @Test void wrongCycleIdCannotRefreshOrInvalidatePurchasedOffers() throws Exception {
        var files = new BlackMarketFileStore(root);
        var cycle = files.loadOrCreate(START);
        UUID owner = UUID.randomUUID();
        assertNull(files.loadPlayer(owner, cycle));
        var saved = new BlackMarketFileStore.PlayerState(offers(), Set.of(2), Set.of(2, 5));
        files.savePlayer(owner, cycle, saved);
        Path marker = root.resolve("cycle.yml");
        Path ledger = root.resolve("players").resolve(owner + ".yml");
        byte[] markerBytes = Files.readAllBytes(marker);
        byte[] ledgerBytes = Files.readAllBytes(ledger);
        var wrong = new BlackMarketFileStore.Cycle(UUID.randomUUID(), cycle.startedAt());

        assertThrows(IOException.class, () -> files.refresh(wrong, START + 1));
        assertArrayEquals(markerBytes, Files.readAllBytes(marker));
        assertArrayEquals(ledgerBytes, Files.readAllBytes(ledger));
        var restored = files.loadPlayer(owner, cycle);
        assertNotNull(restored);
        assertEquals(saved.purchased(), restored.purchased());
        assertEquals(saved.revealed(), restored.revealed());
        for (int i = 0; i < 10; i++) {
            assertEquals(saved.trades().get(i).result, restored.trades().get(i).result);
            assertEquals(saved.trades().get(i).costs, restored.trades().get(i).costs);
        }
    }

    @Test void sameCycleIdWithAnAlteredTimestampCannotRefresh() throws Exception {
        var files = new BlackMarketFileStore(root);
        var cycle = files.loadOrCreate(START);
        byte[] original = Files.readAllBytes(root.resolve("cycle.yml"));
        var wrong = new BlackMarketFileStore.Cycle(cycle.id(), cycle.startedAt() + 1);
        assertThrows(IOException.class, () -> files.refresh(wrong, START + 2));
        assertArrayEquals(original, Files.readAllBytes(root.resolve("cycle.yml")));
    }

    @Test void legitimateRotationStillWorksButStaleSecondRotationIsRejected() throws Exception {
        var files = new BlackMarketFileStore(root);
        var oldCycle = files.loadOrCreate(START);
        long nextStart = START + 4 * 60 * 60 * 1000L;
        var current = files.refresh(oldCycle, nextStart);
        assertNotEquals(oldCycle.id(), current.id());
        assertEquals(nextStart, current.startedAt());
        byte[] marker = Files.readAllBytes(root.resolve("cycle.yml"));
        assertThrows(IOException.class, () -> files.refresh(oldCycle, nextStart + 1));
        assertArrayEquals(marker, Files.readAllBytes(root.resolve("cycle.yml")));
        assertEquals(current, new BlackMarketFileStore(root).loadOrCreate(nextStart + 2));
    }

    @Test void emptyExistingMarkerIsNotBootstrappedAsAFreshMarket() throws Exception {
        Files.createDirectories(root.resolve("players"));
        Path marker = Files.createFile(root.resolve("cycle.yml"));
        var files = new BlackMarketFileStore(root);
        assertThrows(IOException.class, () -> files.loadOrCreate(START));
        assertArrayEquals(new byte[0], Files.readAllBytes(marker));
        assertThrows(IOException.class, () -> files.refresh(null, START + 1));
    }

    private static List<BlackMarketManager.BlackMarketTrade> offers() {
        List<BlackMarketManager.BlackMarketTrade> offers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var trade = new BlackMarketManager.BlackMarketTrade();
            trade.result = new ItemStack(Material.DIAMOND, i + 1);
            var meta = trade.result.getItemMeta();
            assertNotNull(meta);
            meta.getPersistentDataContainer().set(new NamespacedKey("test", "session-identity"),
                    PersistentDataType.STRING, "preserved-offer-" + i);
            trade.result.setItemMeta(meta);
            trade.costs = List.of(new ItemStack(Material.IRON_INGOT, i + 1));
            trade.isFree = false;
            trade.isHard = i % 2 == 0;
            offers.add(trade);
        }
        return offers;
    }
}
