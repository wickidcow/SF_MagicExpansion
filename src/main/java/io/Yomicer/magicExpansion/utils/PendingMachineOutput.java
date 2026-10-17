package io.Yomicer.magicExpansion.utils;

import java.util.Objects;
import java.util.function.UnaryOperator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/** Saves a selected output before delivery and holds an uncertain delivery for repair. */
public final class PendingMachineOutput {
    private PendingMachineOutput() {}

    public interface Store {
        String read();
        void write(String value);
        void clear();
    }

    public static void stage(Store store, ItemStack output) {
        if (store.read() != null) {
            throw new IllegalStateException("A machine output is already pending");
        }
        String saved = encode(nativeCopy(output), "ready");
        store.write(saved);
        if (!saved.equals(store.read())) {
            throw new IllegalStateException("Pending machine output was not retained");
        }
    }

    public static boolean hasValidRecord(Store store) {
        return decode(store.read()) != null;
    }

    /** Returns false for remaining, unreadable or uncertain output. Never rerolls a choice. */
    public static boolean flush(Store store, UnaryOperator<ItemStack> insert) {
        String raw = store.read();
        if (raw == null) return true;
        Entry entry = decode(raw);
        if (entry == null || !entry.state().equals("ready")) return false;

        ItemStack output = entry.item();
        String held = encode(output, "inflight");
        store.write(held);
        if (!held.equals(store.read())) return false;

        // A throw during insertion or the subsequent save leaves an inflight record.
        // It must not be retried automatically because some items may already exist.
        ItemStack leftover = insert.apply(output.clone());
        if (leftover == null || leftover.getType().isAir() || leftover.getAmount() == 0) {
            store.clear();
            return store.read() == null;
        }
        ItemStack remaining = nativeCopy(leftover);
        if (remaining.getAmount() <= 0 || remaining.getAmount() > output.getAmount()
                || !remaining.isSimilar(output)) return false;
        String saved = encode(remaining, "ready");
        store.write(saved);
        return false;
    }

    private static ItemStack nativeCopy(ItemStack item) {
        Objects.requireNonNull(item, "output");
        if (item.getType().isAir() || item.getAmount() <= 0) {
            throw new IllegalArgumentException("Pending output must be a positive item stack");
        }
        return ItemStack.deserialize(item.serialize());
    }

    private static String encode(ItemStack item, String state) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("schema", 1);
        yaml.set("state", state);
        yaml.set("item", item);
        return yaml.saveToString();
    }

    private static Entry decode(String raw) {
        if (raw == null) return null;
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(raw);
            String state = yaml.getString("state");
            if (yaml.getInt("schema", -1) != 1
                    || !("ready".equals(state) || "inflight".equals(state))) return null;
            ItemStack item = yaml.getItemStack("item");
            return new Entry(nativeCopy(item), state);
        } catch (Exception | LinkageError invalid) {
            return null;
        }
    }

    private record Entry(ItemStack item, String state) {}
}
