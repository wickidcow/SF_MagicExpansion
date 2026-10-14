package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

class BlackMarketFileStoreTest {
    private static final long START = 1_750_000_000_000L;

    @TempDir Path temporary;
    private Path directory;
    private BlackMarketFileStore files;

    @BeforeEach void setUp() {
        MockBukkit.mock();
        directory = temporary.resolve("black_market");
        files = new BlackMarketFileStore(directory);
    }

    @AfterEach void tearDown() {
        MockBukkit.unmock();
    }

    @Test void inspectionOnUninitializedStoreDoesNotCreateFiles() {
        var beforeLoad = files.inspectCycle(null);
        assertEquals(BlackMarketFileStore.CycleStatus.NOT_LOADED, beforeLoad.status());
        assertFalse(Files.exists(directory), "Operator inspection must not create a market folder");
    }

    @Test void unchangedCycleInspectionIsReadOnlyAndReportsMatch() throws Exception {
        var cycle = files.loadOrCreate(START);
        Path marker = directory.resolve("cycle.yml");
        byte[] original = Files.readAllBytes(marker);

        var inspection = files.inspectCycle(cycle);

        assertEquals(BlackMarketFileStore.CycleStatus.MATCH, inspection.status());
        assertEquals(cycle, inspection.loadedCycle());
        assertEquals(cycle, inspection.diskCycle());
        assertArrayEquals(original, Files.readAllBytes(marker));
    }

    @Test void semanticallySameCycleRewrittenExternallyStaysLocked() throws Exception {
        var cycle = files.loadOrCreate(START);
        Path marker = directory.resolve("cycle.yml");
        Files.writeString(marker, Files.readString(marker) + "# formatted by another writer\\n");
        byte[] edited = Files.readAllBytes(marker);

        var inspection = files.inspectCycle(cycle);

        assertEquals(BlackMarketFileStore.CycleStatus.SAME_CYCLE_DIFFERENT_BYTES, inspection.status());
        assertEquals(cycle, inspection.diskCycle());
        assertThrows(IOException.class, () -> files.refresh(cycle, START + 1));
        assertThrows(IOException.class, () -> files.loadPlayer(UUID.randomUUID(), cycle));
        assertArrayEquals(edited, Files.readAllBytes(marker));
    }

    @Test void anotherCycleOnDiskIsDiagnosedButNeverAdopted() throws Exception {
        var cycle = files.loadOrCreate(START);
        Path marker = directory.resolve("cycle.yml");
        var newYaml = YamlConfiguration.loadConfiguration(marker.toFile());
        UUID alternate = UUID.randomUUID();
        newYaml.set("id", alternate.toString());
        Files.writeString(marker, newYaml.saveToString());
        byte[] edited = Files.readAllBytes(marker);

        var inspection = files.inspectCycle(cycle);

        assertEquals(BlackMarketFileStore.CycleStatus.DIFFERENT_CYCLE, inspection.status());
        assertEquals(cycle, inspection.loadedCycle());
        assertEquals(alternate, inspection.diskCycle().id());
        assertThrows(IOException.class, () -> files.refresh(cycle, START + 1));
        assertArrayEquals(edited, Files.readAllBytes(marker));
    }

    @Test void unreadableOrMissingMarkerCannotBeSilentlyRecreatedByInspection() throws Exception {
        var cycle = files.loadOrCreate(START);
        Path marker = directory.resolve("cycle.yml");

        Files.writeString(marker, "started-at: [broken\\n");
        byte[] corrupt = Files.readAllBytes(marker);
        var unreadable = files.inspectCycle(cycle);
        assertEquals(BlackMarketFileStore.CycleStatus.UNREADABLE, unreadable.status());
        assertArrayEquals(corrupt, Files.readAllBytes(marker));

        Files.delete(marker);
        var missing = files.inspectCycle(cycle);
        assertEquals(BlackMarketFileStore.CycleStatus.MISSING, missing.status());
        assertFalse(Files.exists(marker));
    }

    @Test void firstInstallCreatesMarkerAndRestartKeepsItsFourHourDeadline() throws Exception {
        BlackMarketFileStore.Cycle initial = files.loadOrCreate(START);
        assertEquals(START, initial.startedAt());
        assertTrue(Files.isRegularFile(directory.resolve("cycle.yml")));

        var restarted = new BlackMarketFileStore(directory);
        assertEquals(initial, restarted.loadOrCreate(START + 60_000));
    }

    @Test void restartingOrReconnectingCannotRerollOffersOrErasePurchaseReceipts() throws Exception {
        var cycle = files.loadOrCreate(START);
        UUID uuid = UUID.randomUUID();
        assertNull(files.loadPlayer(uuid, cycle));
        var state = new BlackMarketFileStore.PlayerState(offers(), Set.of(3, 8), Set.of(3, 5, 8));
        files.savePlayer(uuid, cycle, state);

        var reboot = new BlackMarketFileStore(directory);
        var restoredCycle = reboot.loadOrCreate(START + 3_600_000L);
        var restored = reboot.loadPlayer(uuid, restoredCycle);

        assertEquals(cycle, restoredCycle);
        assertEquals(Set.of(3, 8), restored.purchased());
        assertEquals(Set.of(3, 5, 8), restored.revealed());
        assertEquals(10, restored.trades().size());
        for (int i = 0; i < 10; i++) {
            assertEquals(state.trades().get(i).result, restored.trades().get(i).result);
            assertEquals(state.trades().get(i).costs, restored.trades().get(i).costs);
            assertEquals(state.trades().get(i).isFree, restored.trades().get(i).isFree);
            assertEquals(state.trades().get(i).isHard, restored.trades().get(i).isHard);
        }
        // The next save retains previously purchased slots rather than replacing them.
        reboot.savePlayer(uuid, restoredCycle,
                new BlackMarketFileStore.PlayerState(restored.trades(), Set.of(3, 8, 9),
                        Set.of(3, 5, 8, 9)));
        var thirdSession = new BlackMarketFileStore(directory);
        var reloadedCycle = thirdSession.loadOrCreate(START + 3_600_001L);
        assertEquals(Set.of(3, 8, 9), thirdSession.loadPlayer(uuid, reloadedCycle).purchased());
    }

    @Test void cycleRefreshInvalidatesOldPlayerDataWithoutAccidentallyReusingIt() throws Exception {
        var first = files.loadOrCreate(START);
        UUID uuid = UUID.randomUUID();
        assertNull(files.loadPlayer(uuid, first));
        files.savePlayer(uuid, first,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(2), Set.of(2)));

        var second = files.refresh(first, START + 4 * 60 * 60 * 1000L + 1);
        assertNotEquals(first.id(), second.id());
        assertNull(files.loadPlayer(uuid, second));

        files.savePlayer(uuid, second,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(), Set.of()));
        var reboot = new BlackMarketFileStore(directory);
        var current = reboot.loadOrCreate(second.startedAt() + 20_000);
        assertEquals(second, current);
        assertEquals(Set.of(), reboot.loadPlayer(uuid, current).purchased());
    }

    @Test void unreadableCycleMarkerIsNeverReplacedByAnEmptySession() throws Exception {
        Path marker = directory.resolve("cycle.yml");
        files.loadOrCreate(START);
        String damaged = "started-at: [invalid\n";
        Files.writeString(marker, damaged);
        var restarted = new BlackMarketFileStore(directory);
        assertThrows(IOException.class, () -> restarted.loadOrCreate(START + 1));
        assertEquals(damaged, Files.readString(marker));
    }

    @Test void corruptedPlayerStateLocksThatPlayerInsteadOfResettingPurchases() throws Exception {
        var cycle = files.loadOrCreate(START);
        UUID uuid = UUID.randomUUID();
        assertNull(files.loadPlayer(uuid, cycle));
        files.savePlayer(uuid, cycle,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(3), Set.of(3)));

        Path playerFile = directory.resolve("players").resolve(uuid + ".yml");
        byte[] bad = "trades: [oops".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(playerFile, bad);

        var restarted = new BlackMarketFileStore(directory);
        var restoredCycle = restarted.loadOrCreate(START + 1);
        assertThrows(IOException.class, () -> restarted.loadPlayer(uuid, restoredCycle));
        assertThrows(IOException.class, () -> restarted.savePlayer(uuid, restoredCycle,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(), Set.of())));
        assertArrayEquals(bad, Files.readAllBytes(playerFile));
    }

    @Test void untrustedSlotIndicesAndMissingReceiptListsAreRejected() throws Exception {
        var cycle = files.loadOrCreate(START);
        UUID uuid = UUID.randomUUID();
        assertNull(files.loadPlayer(uuid, cycle));
        var original = new BlackMarketFileStore.PlayerState(offers(), Set.of(3), Set.of(3));
        files.savePlayer(uuid, cycle, original);

        Path playerFile = directory.resolve("players").resolve(uuid + ".yml");
        var yaml = YamlConfiguration.loadConfiguration(playerFile.toFile());
        yaml.set("purchased", List.of(11));
        Files.writeString(playerFile, yaml.saveToString());
        var restarted = new BlackMarketFileStore(directory);
        restarted.loadOrCreate(START + 1);
        assertThrows(IOException.class, () -> restarted.loadPlayer(uuid, cycle));

        yaml.set("purchased", List.of(3));
        yaml.set("revealed", null);
        Files.writeString(playerFile, yaml.saveToString());
        var third = new BlackMarketFileStore(directory);
        third.loadOrCreate(START + 1);
        assertThrows(IOException.class, () -> third.loadPlayer(uuid, cycle));
    }

    @Test void failedAtomicReplaceCannotErasePurchasedSlots() throws Exception {
        var cycle = files.loadOrCreate(START);
        UUID uuid = UUID.randomUUID();
        assertNull(files.loadPlayer(uuid, cycle));
        files.savePlayer(uuid, cycle,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(1), Set.of(1)));
        Path playerFile = directory.resolve("players").resolve(uuid + ".yml");
        byte[] original = Files.readAllBytes(playerFile);

        var cannotReplace = new BlackMarketFileStore(directory, (staged, target) -> {
            if (target.equals(playerFile)) throw new IOException("Simulated read-only filesystem");
            Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        });
        var restoredCycle = cannotReplace.loadOrCreate(START + 1);
        var before = cannotReplace.loadPlayer(uuid, restoredCycle);
        assertThrows(IOException.class, () -> cannotReplace.savePlayer(uuid, restoredCycle,
                new BlackMarketFileStore.PlayerState(before.trades(), Set.of(1, 2), Set.of(1, 2))));
        assertArrayEquals(original, Files.readAllBytes(playerFile));
        assertEquals(Set.of(1), reloadedPurchases(uuid, START + 2));
    }

    private Set<Integer> reloadedPurchases(UUID uuid, long now) throws IOException {
        var verifier = new BlackMarketFileStore(directory);
        var cycle = verifier.loadOrCreate(now);
        return verifier.loadPlayer(uuid, cycle).purchased();
    }

    @Test void externalEditsBetweenReadAndWriteMustNotBeOverwritten() throws Exception {
        var cycle = files.loadOrCreate(START);
        UUID uuid = UUID.randomUUID();
        assertNull(files.loadPlayer(uuid, cycle));
        files.savePlayer(uuid, cycle,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(4), Set.of(4)));
        var reboot = new BlackMarketFileStore(directory);
        var current = reboot.loadOrCreate(START + 1);
        var read = reboot.loadPlayer(uuid, current);
        Path playerFile = directory.resolve("players").resolve(uuid + ".yml");
        Files.writeString(playerFile, Files.readString(playerFile) + "# externally edited\n");
        byte[] modified = Files.readAllBytes(playerFile);
        assertThrows(IOException.class, () -> reboot.savePlayer(uuid, current,
                new BlackMarketFileStore.PlayerState(read.trades(), Set.of(4, 5), Set.of(4, 5))));
        assertArrayEquals(modified, Files.readAllBytes(playerFile));
    }

    @Test void aPurchaseCannotBeSavedBeforeTheRecordHasBeenRead() throws Exception {
        var cycle = files.loadOrCreate(START);
        assertThrows(IOException.class, () -> files.savePlayer(UUID.randomUUID(), cycle,
                new BlackMarketFileStore.PlayerState(offers(), Set.of(), Set.of())));
    }

    private static List<BlackMarketManager.BlackMarketTrade> offers() {
        List<BlackMarketManager.BlackMarketTrade> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var trade = new BlackMarketManager.BlackMarketTrade();
            trade.result = new ItemStack(Material.DIAMOND, i + 1);
            var meta = trade.result.getItemMeta();
            meta.getPersistentDataContainer().set(
                    new NamespacedKey("magicexpansion", "reward_owner"),
                    PersistentDataType.STRING, "player-specific-" + i);
            trade.result.setItemMeta(meta);
            trade.costs = new ArrayList<>();
            if (i % 2 == 0) trade.costs.add(new ItemStack(Material.GOLD_INGOT, 2));
            trade.isFree = i % 2 != 0;
            trade.isHard = i % 3 == 0;
            result.add(trade);
        }
        return result;
    }
}
