package io.Yomicer.magicExpansion.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ADataContainer;
import org.bukkit.inventory.ItemStack;

/** Capacity and insertion for the existing cargo keys, without world drops or source consumption. */
public final class CargoStorage {
    private CargoStorage() {}

    private record Destination(int slot, long count, int accepted, boolean existing) {}

    public static int capacity(ADataContainer data, ItemStack item, int requested, int slots) {
        synchronized (data) {
            Destination destination = find(data, item, requested, slots);
            return destination == null ? 0 : destination.accepted();
        }
    }

    /** Returns the quantity committed; callers must leave every unaccepted item at its source. */
    public static int store(ADataContainer data, ItemStack item, int requested, int slots) {
        return store(data, item, requested, slots, false);
    }

    public static int storeExact(ADataContainer data, ItemStack item, int requested, int slots) {
        return store(data, item, requested, slots, true);
    }

    private static int store(ADataContainer data, ItemStack item, int requested, int slots, boolean exact) {
        synchronized (data) {
            Destination destination = find(data, item, requested, slots);
            if (destination == null || destination.accepted() == 0
                    || (exact && destination.accepted() != requested)) return 0;
            int slot = destination.slot();
            if (!destination.existing()) {
                String encoded = SameItemJudge.itemToBase64(item);
                if (encoded == null) return 0;
                data.setData("item_type_" + slot, encoded);
                data.setData("item_max_" + slot, "-1");
            }
            // find() bounds acceptance by Long.MAX_VALUE - count before any write.
            data.setData("item_count_" + slot, Long.toString(destination.count() + destination.accepted()));
            return destination.accepted();
        }
    }

    private static Destination find(ADataContainer data, ItemStack item, int requested, int slots) {
        if (item == null || item.getType().isAir() || requested <= 0) return null;
        int empty = -1;
        for (int i = 0; i < slots; i++) {
            String type = data.getData("item_type_" + i);
            String count = data.getData("item_count_" + i);
            String maximum = data.getData("item_max_" + i);
            if (blank(type) && blank(count) && blank(maximum)) {
                if (empty == -1) empty = i;
                continue;
            }
            // Incomplete or unreadable records remain occupied and byte-for-byte intact.
            if (blank(type)) continue;
            ItemStack stored;
            try {
                stored = SameItemJudge.itemFromBase64(type);
            } catch (RuntimeException unreadable) {
                continue;
            }
            if (stored == null || !SameItemJudge.isSimilarSafe(item, stored)) continue;
            try {
                long current = Long.parseLong(count);
                long limit = blank(maximum) ? -1 : Long.parseLong(maximum);
                if (current < 0 || limit < -1) return null;
                long ceiling = limit == -1 ? Long.MAX_VALUE : limit;
                int accepted = current >= ceiling ? 0 : (int) Math.min(requested, ceiling - current);
                return new Destination(i, current, accepted, true);
            } catch (NumberFormatException unreadable) {
                return null;
            }
        }
        return empty == -1 ? null : new Destination(empty, 0, requested, false);
    }

    private static boolean blank(String value) {
        return value == null || value.isEmpty();
    }
}
