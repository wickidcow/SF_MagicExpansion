package io.Yomicer.magicExpansion.utils.shop;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ShopManager {

    public static class Trade {
        public ItemStack result;
        public List<ItemStack> costItems = new ArrayList<>();
        public int globalLimit = 0;   // 0 means unlimited
        public int personalLimit = 0; // 0 means unlimited
        public int globalUsed = 0;
        public Map<UUID, Integer> personalUsed = new HashMap<>();
    }

    public static class Shop {
        public String name;
        public List<Trade> trades = new ArrayList<>();
    }

    private static final List<Shop> shops = new ArrayList<>();

    public static void load() {
        File dir = getShopDirectory();
        if (!ensureDirectory(dir)) {
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) {
            MagicExpansion.getInstance().getLogger().warning("Could not list Magic Market shop files. Keeping the currently loaded shops.");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        List<Shop> loadedShops = new ArrayList<>();

        for (File file : files) {
            try {
                loadedShops.add(loadShop(file));
            } catch (InvalidConfigurationException | ShopFormatException e) {
                quarantineCorruptShopFile(file, e);
            } catch (IOException e) {
                MagicExpansion.getInstance().getLogger().severe(
                        "Could not read Magic Market shop file '" + file.getName() + "'. "
                                + "The file was left untouched and the shop was not loaded: " + e.getMessage()
                );
            } catch (RuntimeException e) {
                MagicExpansion.getInstance().getLogger().severe(
                        "Unexpected error while loading Magic Market shop file '" + file.getName() + "'. "
                                + "The file was left untouched and the shop was not loaded."
                );
                e.printStackTrace();
            }
        }

        shops.clear();
        shops.addAll(loadedShops);
    }

    private static Shop loadShop(File file) throws IOException, InvalidConfigurationException, ShopFormatException {
        YamlConfiguration config = new YamlConfiguration();
        config.load(file);

        Shop shop = new Shop();
        shop.name = stripYamlSuffix(file.getName());

        Object rawTrades = config.get("trades");
        if (rawTrades == null) {
            return shop;
        }
        if (!(rawTrades instanceof List<?> tradeList)) {
            throw new ShopFormatException("'trades' must be a YAML list");
        }

        for (int index = 0; index < tradeList.size(); index++) {
            Object rawTrade = tradeList.get(index);
            if (!(rawTrade instanceof Map<?, ?> tradeMap)) {
                throw new ShopFormatException("trade " + index + " is not a map");
            }

            Trade trade = new Trade();

            Object resultObj = tradeMap.get("result");
            if (resultObj != null && !(resultObj instanceof ItemStack)) {
                throw new ShopFormatException("trade " + index + " has an invalid result item");
            }
            trade.result = resultObj == null ? null : ((ItemStack) resultObj).clone();

            Object costObj = tradeMap.get("cost");
            if (costObj != null && !(costObj instanceof List<?>)) {
                throw new ShopFormatException("trade " + index + " has an invalid cost list");
            }
            if (costObj instanceof List<?> rawCosts) {
                for (int costIndex = 0; costIndex < rawCosts.size(); costIndex++) {
                    Object rawCost = rawCosts.get(costIndex);
                    if (rawCost != null && !(rawCost instanceof ItemStack)) {
                        throw new ShopFormatException(
                                "trade " + index + " cost " + costIndex + " is not an item"
                        );
                    }
                    trade.costItems.add(rawCost == null ? null : ((ItemStack) rawCost).clone());
                }
            }

            trade.globalLimit = getInt(tradeMap.get("globalLimit"), 0);
            trade.personalLimit = getInt(tradeMap.get("personalLimit"), 0);
            trade.globalUsed = getInt(tradeMap.get("globalUsed"), 0);

            Object personalUsedObj = tradeMap.get("personalUsed");
            if (personalUsedObj != null && !(personalUsedObj instanceof Map<?, ?>)) {
                throw new ShopFormatException("trade " + index + " has an invalid personalUsed map");
            }
            if (personalUsedObj instanceof Map<?, ?> personalUsed) {
                for (Map.Entry<?, ?> entry : personalUsed.entrySet()) {
                    if (!(entry.getKey() instanceof String uuidString)) {
                        continue;
                    }
                    try {
                        trade.personalUsed.put(UUID.fromString(uuidString), getInt(entry.getValue(), 0));
                    } catch (IllegalArgumentException ignored) {
                        MagicExpansion.getInstance().getLogger().warning(
                                "Ignoring invalid player UUID '" + uuidString + "' in shop '" + shop.name + "'."
                        );
                    }
                }
            }

            shop.trades.add(trade);
        }

        return shop;
    }

    private static int getInt(Object obj, int def) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return def;
    }

    private static File getShopDirectory() {
        return new File(MagicExpansion.getInstance().getDataFolder(), "portable_shops");
    }

    private static File getShopFile(String shopName) {
        String safeName = shopName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return new File(getShopDirectory(), safeName + ".yml");
    }

    public static void saveShop(Shop shop) {
        if (shop == null || shop.name == null) {
            return;
        }

        File dir = getShopDirectory();
        if (!ensureDirectory(dir)) {
            return;
        }

        File file = getShopFile(shop.name);
        YamlConfiguration config = new YamlConfiguration();

        List<Map<String, Object>> tradeList = new ArrayList<>();
        for (Trade trade : new ArrayList<>(shop.trades)) {
            if (trade == null) {
                continue;
            }

            Map<String, Object> tradeMap = new LinkedHashMap<>();
            tradeMap.put("result", trade.result);
            tradeMap.put("cost", trade.costItems == null ? Collections.emptyList() : new ArrayList<>(trade.costItems));
            tradeMap.put("globalLimit", trade.globalLimit);
            tradeMap.put("personalLimit", trade.personalLimit);
            tradeMap.put("globalUsed", trade.globalUsed);

            Map<String, Integer> personalUsed = new LinkedHashMap<>();
            if (trade.personalUsed != null) {
                for (Map.Entry<UUID, Integer> entry : new HashMap<>(trade.personalUsed).entrySet()) {
                    if (entry.getKey() != null) {
                        personalUsed.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
            tradeMap.put("personalUsed", personalUsed);
            tradeList.add(tradeMap);
        }
        config.set("trades", tradeList);

        try {
            saveYamlAtomically(file, config.saveToString());
        } catch (IOException e) {
            MagicExpansion.getInstance().getLogger().severe(
                    "Could not save Magic Market shop '" + shop.name + "'. "
                            + "The previous shop file was left intact where possible: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private static void saveYamlAtomically(File file, String yaml) throws IOException {
        Path parent = file.toPath().getParent();
        if (parent == null) {
            throw new IOException("shop file has no parent directory");
        }

        Path temp = Files.createTempFile(parent, "." + file.getName() + ".", ".tmp");
        try {
            Files.writeString(temp, yaml, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static boolean ensureDirectory(File dir) {
        if (dir.isDirectory()) {
            return true;
        }
        if (dir.exists()) {
            MagicExpansion.getInstance().getLogger().severe(
                    "Magic Market shop path is not a directory: " + dir.getAbsolutePath()
            );
            return false;
        }
        if (!dir.mkdirs()) {
            MagicExpansion.getInstance().getLogger().severe(
                    "Could not create Magic Market shop directory: " + dir.getAbsolutePath()
            );
            return false;
        }
        return true;
    }

    private static void quarantineCorruptShopFile(File file, Exception cause) {
        File quarantineDir = new File(file.getParentFile(), "quarantine");
        if (!ensureDirectory(quarantineDir)) {
            MagicExpansion.getInstance().getLogger().severe(
                    "Magic Market shop file '" + file.getName() + "' is invalid and could not be quarantined. "
                            + "It was left untouched and was not loaded: " + cause.getMessage()
            );
            return;
        }

        String baseName = stripYamlSuffix(file.getName());
        File target = new File(quarantineDir, baseName + ".corrupt-" + System.currentTimeMillis() + ".yml");
        int suffix = 1;
        while (target.exists()) {
            target = new File(quarantineDir, baseName + ".corrupt-" + System.currentTimeMillis() + "-" + suffix++ + ".yml");
        }

        try {
            Files.move(file.toPath(), target.toPath());
            MagicExpansion.getInstance().getLogger().severe(
                    "Magic Market skipped corrupt shop file '" + file.getName() + "': " + cause.getMessage()
                            + ". The original file was preserved at " + target.getAbsolutePath()
            );
        } catch (IOException moveFailure) {
            MagicExpansion.getInstance().getLogger().severe(
                    "Magic Market shop file '" + file.getName() + "' is invalid and could not be quarantined. "
                            + "It was left untouched and was not loaded: " + cause.getMessage()
            );
            moveFailure.printStackTrace();
        }
    }

    private static String stripYamlSuffix(String name) {
        return name.endsWith(".yml") ? name.substring(0, name.length() - 4) : name;
    }

    public static void reload() {
        load();
    }

    public static List<Shop> getShops() {
        return shops;
    }

    public static Shop getShop(String name) {
        for (Shop s : shops) {
            if (s.name.equals(name)) return s;
        }
        return null;
    }

    public static void createShop(String name) {
        if (getShop(name) == null) {
            Shop shop = new Shop();
            shop.name = name;
            shops.add(shop);
            saveShop(shop);
        }
    }

    public static void deleteShop(String name) {
        shops.removeIf(s -> s.name.equals(name));
        File file = getShopFile(name);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            MagicExpansion.getInstance().getLogger().warning(
                    "Could not delete Magic Market shop file '" + file.getName() + "': " + e.getMessage()
            );
        }
    }

    public static boolean canPurchase(UUID playerId, Trade trade) {
        if (trade.globalLimit > 0 && trade.globalUsed >= trade.globalLimit) return false;
        if (trade.personalLimit > 0) {
            int used = trade.personalUsed.getOrDefault(playerId, 0);
            if (used >= trade.personalLimit) return false;
        }
        return true;
    }

    public static void recordPurchase(UUID playerId, Shop shop, Trade trade) {
        if (trade.globalLimit > 0) {
            trade.globalUsed++;
        }
        if (trade.personalLimit > 0) {
            trade.personalUsed.put(playerId, trade.personalUsed.getOrDefault(playerId, 0) + 1);
        }
        saveShop(shop);
    }

    public static void resetUsage(Shop shop, Trade trade) {
        trade.globalUsed = 0;
        trade.personalUsed.clear();
        saveShop(shop);
    }

    public static void saveAll() {
        for (Shop shop : new ArrayList<>(shops)) {
            saveShop(shop);
        }
    }

    private static final class ShopFormatException extends Exception {
        private ShopFormatException(String message) {
            super(message);
        }
    }
}
