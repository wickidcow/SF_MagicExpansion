package io.Yomicer.magicExpansion.utils.shop;

import io.Yomicer.magicExpansion.MagicExpansion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class ShopManager {
    private static final ShopFileStore files = new ShopFileStore(
            MagicExpansion.getInstance().getDataFolder().toPath().resolve("portable_shops"),
            message -> MagicExpansion.getInstance().getLogger().severe(message));

    public static class Trade {
        /** Stable catalog identity. Null for preexisting or manually created trades. */
        public String catalogId;
        public List<String> catalogPlugins = new ArrayList<>();
        public ItemStack result;
        public List<ItemStack> costItems = new ArrayList<>();
        public int globalLimit = 0;   // Zero retains the existing unlimited convention.
        public int personalLimit = 0;
        public int globalUsed = 0;
        public Map<UUID, Integer> personalUsed = new HashMap<>();
    }

    public static class Shop {
        public String name;
        public List<Trade> trades = new ArrayList<>();
        /** Includes deleted catalog offers so populate cannot recreate their unused allowance. */
        public java.util.Set<String> catalogHistory = new java.util.LinkedHashSet<>();
    }

    private static final List<Shop> shops = new ArrayList<>();

    public static void load() {
        try {
            List<Shop> loaded = files.load();
            shops.clear();
            shops.addAll(loaded);
        } catch (IOException | RuntimeException error) {
            // Do not replace live definitions with an empty directory snapshot after an I/O failure.
            MagicExpansion.getInstance().getLogger().severe(
                    "Portable shops are read-only because the directory could not be loaded: " + error.getMessage());
        }
    }

    public static void saveShop(Shop shop) {
        trySaveShop(shop);
    }

    public static boolean trySaveShop(Shop shop) {
        return files.save(shop);
    }

    public static boolean isAvailable(Shop shop) {
        return files.isAvailable(shop);
    }

    public static void reload() {
        load();
    }

    public static List<Shop> getShops() {
        return shops;
    }

    public static Shop getShop(String name) {
        for (Shop shop : shops) {
            if (shop.name.equals(name) && files.isAvailable(shop)) return shop;
        }
        return null;
    }

    public static void createShop(String name) {
        tryCreateShop(name);
    }

    public static boolean tryCreateShop(String name) {
        for (Shop existing : shops) {
            if (existing.name.equals(name)) return false;
        }
        Shop shop = new Shop();
        shop.name = name;
        if (!files.save(shop)) return false;
        shops.add(shop);
        return true;
    }

    public static void deleteShop(String name) {
        tryDeleteShop(name);
    }

    public static boolean tryDeleteShop(String name) {
        Shop shop = getShop(name);
        if (shop == null || !files.delete(shop)) return false;
        shops.remove(shop);
        return true;
    }

    public static boolean canPurchase(UUID playerId, Trade trade) {
        if (playerId == null || trade == null || !CatalogItems.isAvailable(trade)) return false;
        if (trade.globalLimit > 0 && trade.globalUsed >= trade.globalLimit) return false;
        if (trade.personalLimit > 0) {
            int used = trade.personalUsed.getOrDefault(playerId, 0);
            if (used >= trade.personalLimit) return false;
        }
        return true;
    }

    /** Compatibility entry point. Reward issuers must use tryRecordPurchase and check its result. */
    public static void recordPurchase(UUID playerId, Shop shop, Trade trade) {
        tryRecordPurchase(playerId, shop, trade);
    }

    /** Persist the purchase before delivering rewards; stale/read-only shops cannot authorize one. */
    public static boolean tryRecordPurchase(UUID playerId, Shop shop, Trade trade) {
        if (playerId == null || shop == null || trade == null || !isAvailable(shop)
                || !shop.trades.contains(trade) || !canPurchase(playerId, trade)) return false;
        return ShopPurchaseLedger.commit(playerId, trade, () -> trySaveShop(shop));
    }

    public static void resetUsage(Shop shop, Trade trade) {
        trade.globalUsed = 0;
        trade.personalUsed.clear();
        saveShop(shop);
    }

    public static void saveAll() {
        for (Shop shop : shops) {
            saveShop(shop);
        }
    }
}
