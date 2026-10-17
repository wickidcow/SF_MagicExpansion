package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class PendingMachineOutputTest {
    @BeforeEach void start() { MockBukkit.mock(); }
    @AfterEach void stop() { MockBukkit.unmock(); }

    @Test void oversizedNonstackableBatchSurvivesRecreatedStoreAndDrainsExactly() {
        Memory store = new Memory();
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 64);
        ItemMeta meta = book.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("test", "identity"), PersistentDataType.STRING, "retained");
        book.setItemMeta(meta);
        PendingMachineOutput.stage(store, book);
        AtomicInteger delivered = new AtomicInteger();
        assertFalse(PendingMachineOutput.flush(store, item -> {
            assertTrue(item.isSimilar(book));
            assertEquals(64, item.getAmount());
            delivered.addAndGet(36);
            item.setAmount(28);
            return item;
        }));
        Memory reloaded = new Memory();
        reloaded.value = store.value;
        assertFalse(PendingMachineOutput.flush(reloaded, item -> {
            assertEquals(28, item.getAmount());
            return item;
        }));
        assertTrue(PendingMachineOutput.flush(reloaded, item -> {
            assertTrue(item.isSimilar(book));
            delivered.addAndGet(item.getAmount());
            return null;
        }));
        assertEquals(64, delivered.get());
        assertNull(reloaded.value);
    }

    @Test void thrownPartialInsertionLeavesHeldRecordWithoutAnotherAttempt() {
        Memory store = new Memory();
        PendingMachineOutput.stage(store, new ItemStack(Material.DIAMOND, 64));
        AtomicInteger attempts = new AtomicInteger();
        assertThrows(IllegalStateException.class, () -> PendingMachineOutput.flush(store, item -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("Destination wrote before failing");
        }));
        String held = store.value;
        assertTrue(held.contains("inflight"));
        assertFalse(PendingMachineOutput.flush(store, item -> { attempts.incrementAndGet(); return null; }));
        assertEquals(1, attempts.get());
        assertEquals(held, store.value);
    }

    @Test void refusedRemainderSaveDoesNotReplayAcceptedItems() {
        Memory store = new Memory();
        PendingMachineOutput.stage(store, new ItemStack(Material.DIAMOND, 64));
        assertThrows(IllegalStateException.class, () -> PendingMachineOutput.flush(store, item -> {
            store.refuseWrite = true;
            item.setAmount(28);
            return item;
        }));
        store.refuseWrite = false;
        assertFalse(PendingMachineOutput.flush(store, item -> fail("Uncertain delivery must remain held")));
    }

    @Test void unreadableSavedOutputRemainsUntouched() {
        Memory store = new Memory();
        store.value = "schema: 1\nstate: ready\nitem: [unterminated";
        String original = store.value;
        assertFalse(PendingMachineOutput.flush(store, item -> fail("Unreadable output must not be delivered")));
        assertEquals(original, store.value);
    }

    @Test void anotherChoiceCannotReplaceThePendingRandomResult() {
        Memory store = new Memory();
        PendingMachineOutput.stage(store, new ItemStack(Material.DIAMOND, 2));
        String original = store.value;
        assertThrows(IllegalStateException.class,
                () -> PendingMachineOutput.stage(store, new ItemStack(Material.GOLD_INGOT, 64)));
        assertEquals(original, store.value);
    }

    @Test void invalidLeftoverRemainsHeld() {
        for (ItemStack returned : new ItemStack[] {
                new ItemStack(Material.GOLD_INGOT, 1), new ItemStack(Material.DIAMOND, 65)}) {
            Memory store = new Memory();
            PendingMachineOutput.stage(store, new ItemStack(Material.DIAMOND, 64));
            assertFalse(PendingMachineOutput.flush(store, item -> returned));
            assertFalse(PendingMachineOutput.flush(store, item -> fail("Invalid acknowledgement must remain held")));
        }
    }

    @Test void ignoredInitialSaveCannotAcknowledgeAnOutput() {
        Memory store = new Memory();
        store.ignoreWrites = true;
        assertThrows(IllegalStateException.class,
                () -> PendingMachineOutput.stage(store, new ItemStack(Material.DIAMOND)));
        assertNull(store.value);
    }

    private static final class Memory implements PendingMachineOutput.Store {
        String value;
        boolean refuseWrite;
        boolean ignoreWrites;
        @Override public String read() { return value; }
        @Override public void write(String next) {
            if (refuseWrite) throw new IllegalStateException("Controlled save failure");
            if (!ignoreWrites) value = next;
        }
        @Override public void clear() { value = null; }
    }
}
