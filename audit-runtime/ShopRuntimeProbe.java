package audit;

import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import io.Yomicer.magicExpansion.utils.shop.ShopManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Disposable fixture: real shop methods and Bukkit inventories, with a no-network player facade. */
public final class ShopRuntimeProbe extends JavaPlugin {
    private final List<String> results = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                boolean original = Boolean.getBoolean("shop.audit.original");
                portableDuplicate(original);
                pageSelection(original);
                blackMarketDuplicate(original);
                if (!original) {
                    paidTradePreservesData();
                    offHandIsNotPayment();
                    freeBlackMarketStillWorks();
                    clampedPageSelectsVisibleTrade();
                }
                String marker = original ? "SHOP_ORIGINAL_DEFECTS_REPRODUCED" : "SHOP_RUNTIME_PRESERVATION_PASS";
                Files.writeString(Path.of("shop-result.txt"), marker + "\n" + String.join("\n", results) + "\n");
                getLogger().info(marker + " checks=" + results.size());
            } catch (Throwable error) {
                getLogger().log(java.util.logging.Level.SEVERE, "SHOP_RUNTIME_FAIL", error);
            } finally {
                Bukkit.shutdown();
            }
        }, 40L);
    }

    private void portableDuplicate(boolean original) throws Exception {
        Fixture p = new Fixture();
        ShopManager.Shop shop = shop("audit-duplicate", trade(Material.NETHER_STAR, 1,
                List.of(item(Material.DIAMOND, 40), item(Material.DIAMOND, 40))));
        p.storage.setItem(0, item(Material.DIAMOND, 50));
        ItemStack[] before = copy(p.storage);
        ShopGUI.openShopTrades(p.player, shop.name, 0);
        purchase(p, shop.name);
        if (original) {
            require(count(p.storage, Material.DIAMOND) == 0 && count(p.storage, Material.NETHER_STAR) == 1,
                    "Original portable-shop underpayment was not reproduced");
        } else {
            require(Arrays.equals(before, p.storage.getStorageContents()), "Rejected payment changed storage");
            require(shop.trades.getFirst().globalUsed == 0 && shop.trades.getFirst().personalUsed.isEmpty(),
                    "Rejected payment changed purchase counters");
        }
        results.add("portable_duplicate_cost:" + (original ? "defect_reproduced" : "unchanged_on_rejection"));
    }

    private void pageSelection(boolean original) throws Exception {
        Fixture p = new Fixture();
        ShopManager.Shop shop = manyTrades("audit-pages");
        p.storage.setItem(0, item(Material.DIAMOND, 1));
        p.storage.setItem(1, item(Material.GOLD_INGOT, 1));
        ShopGUI.openShopTrades(p.player, shop.name, 1);
        require(p.view.getItem(10).getType() == Material.NETHER_STAR, "Fixture did not show second-page trade");
        purchase(p, shop.name);
        if (original) {
            require(count(p.storage, Material.EMERALD) == 1 && count(p.storage, Material.NETHER_STAR) == 0,
                    "Original first-page purchase defect was not reproduced");
        } else {
            require(count(p.storage, Material.NETHER_STAR) == 1 && count(p.storage, Material.DIAMOND) == 1,
                    "Purchase did not match the displayed page");
            require(shop.trades.get(0).globalUsed == 0 && shop.trades.get(28).globalUsed == 1,
                    "Purchase counted the wrong trade");
        }
        results.add("second_page:" + (original ? "defect_reproduced" : "displayed_trade_selected"));
    }

    private void blackMarketDuplicate(boolean original) throws Exception {
        Fixture p = new Fixture();
        setBlackMarket(p, false);
        p.storage.setItem(0, item(Material.DIAMOND, 50));
        ItemStack[] before = copy(p.storage);
        blackPurchase(p);
        if (original) {
            require(count(p.storage, Material.NETHER_STAR) == 1 && count(p.storage, Material.DIAMOND) == 0,
                    "Original black-market underpayment was not reproduced");
        } else {
            require(Arrays.equals(before, p.storage.getStorageContents()), "Black-market rejection changed items");
            require(!BlackMarketManager.hasPurchased(p.uuid, 0), "Rejected black-market trade consumed its limit");
            p.storage.setItem(0, item(Material.DIAMOND, 64));
            p.storage.setItem(1, item(Material.DIAMOND, 30));
            blackPurchase(p);
            require(count(p.storage, Material.DIAMOND) == 14 && count(p.storage, Material.NETHER_STAR) == 1,
                    "Successful black-market payment was not exact");
            require(BlackMarketManager.hasPurchased(p.uuid, 0), "Successful black-market trade lost its limit");
        }
        results.add("black_market_duplicate:" + (original ? "defect_reproduced" : "rejected_then_paid_exactly"));
    }

    private void paidTradePreservesData() throws Exception {
        Fixture p = new Fixture();
        ItemStack original = richItem(Material.DIAMOND, 31);
        ItemStack cost = original.clone(); cost.setAmount(7);
        ShopManager.Trade trade = trade(Material.NETHER_STAR, 1, List.of(cost, cost));
        trade.result = richItem(Material.NETHER_STAR, 1);
        ItemStack reward = trade.result.clone();
        ShopManager.Shop shop = shop("audit-metadata", trade);
        p.storage.setItem(0, original.clone());
        ShopGUI.openShopTrades(p.player, shop.name, 0);
        purchase(p, shop.name);
        ItemStack remainder = original.clone(); remainder.setAmount(17);
        require(remainder.equals(p.storage.getItem(0)), "Old item metadata or remaining amount changed");
        require(reward.equals(p.storage.getItem(1)), "Reward metadata changed");
        require(cost.getAmount() == 7 && original.getAmount() == 31, "Cost template was mutated");
        ShopManager.reload();
        ShopManager.Trade reloaded = ShopManager.getShop(shop.name).trades.getFirst();
        require(reloaded.result.equals(reward) && reloaded.costItems.getFirst().equals(cost), "Saved definitions changed");
        require(reloaded.globalUsed == 1 && reloaded.personalUsed.get(p.uuid) == 1, "Saved purchase limits changed");
        results.add("rich_items_and_saved_limits:exact");
    }

    private void offHandIsNotPayment() throws Exception {
        Fixture p = new Fixture();
        ShopManager.Shop shop = shop("audit-offhand", trade(Material.NETHER_STAR, 1,
                List.of(item(Material.DIAMOND, 40), item(Material.DIAMOND, 40))));
        p.storage.setItem(0, item(Material.DIAMOND, 50));
        p.extra[4] = item(Material.DIAMOND, 40);
        ShopGUI.openShopTrades(p.player, shop.name, 0);
        purchase(p, shop.name);
        require(count(p.storage, Material.DIAMOND) == 50 && p.extra[4].getAmount() == 40,
                "Payment consumed the off-hand or a partial storage stack");
        require(count(p.storage, Material.NETHER_STAR) == 0, "Unpaid reward granted");
        results.add("offhand:untouched");
    }

    private void freeBlackMarketStillWorks() throws Exception {
        Fixture p = new Fixture(); setBlackMarket(p, true); blackPurchase(p);
        require(count(p.storage, Material.NETHER_STAR) == 1 && BlackMarketManager.hasPurchased(p.uuid, 0),
                "Existing free black-market behavior changed");
        results.add("free_black_market:preserved");
    }

    private void clampedPageSelectsVisibleTrade() throws Exception {
        Fixture p = new Fixture(); ShopManager.Shop shop = manyTrades("audit-clamped");
        p.storage.setItem(0, item(Material.IRON_INGOT, 1));
        ShopGUI.openShopTrades(p.player, shop.name, Integer.MAX_VALUE);
        require(p.view.getItem(10).getType() == Material.DIAMOND_BLOCK, "Last page was not displayed");
        purchase(p, shop.name);
        require(count(p.storage, Material.DIAMOND_BLOCK) == 1 && shop.trades.get(56).globalUsed == 1,
                "Clamped page selected a different trade");
        results.add("clamped_page:display_and_purchase_agree");
    }

    private static ShopManager.Shop manyTrades(String name) {
        ShopManager.Shop shop = new ShopManager.Shop(); shop.name = name;
        for (int i = 0; i < 60; i++) shop.trades.add(trade(Material.EMERALD, 1, List.of(item(Material.DIAMOND, 1))));
        shop.trades.set(28, trade(Material.NETHER_STAR, 1, List.of(item(Material.GOLD_INGOT, 1))));
        shop.trades.set(56, trade(Material.DIAMOND_BLOCK, 1, List.of(item(Material.IRON_INGOT, 1))));
        ShopManager.getShops().add(shop); return shop;
    }

    private static ShopManager.Shop shop(String name, ShopManager.Trade trade) {
        ShopManager.Shop shop = new ShopManager.Shop(); shop.name = name; shop.trades.add(trade);
        ShopManager.getShops().add(shop); return shop;
    }

    private static ShopManager.Trade trade(Material result, int amount, List<ItemStack> costs) {
        ShopManager.Trade trade = new ShopManager.Trade(); trade.result = item(result, amount);
        trade.costItems = costs; trade.globalLimit = 10; trade.personalLimit = 10; return trade;
    }

    private static void setBlackMarket(Fixture p, boolean free) {
        BlackMarketManager.forceRefresh();
        var trade = new BlackMarketManager.BlackMarketTrade(); trade.result = item(Material.NETHER_STAR, 1);
        trade.costs = List.of(item(Material.DIAMOND, 40), item(Material.DIAMOND, 40)); trade.isFree = free;
        var trades = BlackMarketManager.getTodayTrades(p.player); trades.clear(); trades.add(trade);
    }

    private static void purchase(Fixture p, String shop) throws Exception {
        Method method = ShopGUI.class.getDeclaredMethod("handlePurchase", Player.class, String.class, int.class, ItemStack.class);
        method.setAccessible(true); method.invoke(new ShopGUI(), p.player, shop, 10, p.view.getItem(10));
    }

    private static void blackPurchase(Fixture p) throws Exception {
        Method method = ShopGUI.class.getDeclaredMethod("handleBlackMarketPurchase", Player.class, int.class);
        method.setAccessible(true); method.invoke(new ShopGUI(), p.player, 0);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static long count(Inventory inventory, Material material) {
        return Arrays.stream(inventory.getStorageContents()).filter(i -> i != null && i.getType() == material)
                .mapToLong(ItemStack::getAmount).sum();
    }

    private static ItemStack[] copy(Inventory inventory) {
        return Arrays.stream(inventory.getStorageContents()).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new);
    }

    private static ItemStack item(Material type, int amount) { return new ItemStack(type, amount); }

    private static ItemStack richItem(Material type, int amount) {
        ItemStack item = item(type, amount); var meta = item.getItemMeta();
        meta.displayName(Component.text("Preserved old item")); meta.lore(List.of(Component.text("Owner-written lore")));
        var data = meta.getPersistentDataContainer();
        data.set(NamespacedKey.fromString("slimefun:slimefun_item"), PersistentDataType.STRING, "UNREGISTERED_OLD_ADDON");
        data.set(NamespacedKey.fromString("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);
        data.set(NamespacedKey.fromString("other:storage_count"), PersistentDataType.LONG, 9_007_199_254_740_993L);
        data.set(NamespacedKey.fromString("other:opaque"), PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 4});
        item.setItemMeta(meta); return item;
    }

    private static final class Fixture {
        final UUID uuid = UUID.randomUUID();
        final Inventory storage = Bukkit.createInventory(null, 36);
        final ItemStack[] extra = new ItemStack[7];
        Inventory view;
        final PlayerInventory inventory = (PlayerInventory) Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(),
                new Class<?>[] {PlayerInventory.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getContents")) {
                        ItemStack[] all = Arrays.copyOf(storage.getStorageContents(), 43);
                        System.arraycopy(extra, 0, all, 36, extra.length); return all;
                    }
                    if (method.getName().equals("setItem") && args[0] instanceof Integer slot && slot >= 36) {
                        extra[slot - 36] = (ItemStack) args[1]; return null;
                    }
                    try { return storage.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(storage, args); }
                    catch (InvocationTargetException error) { throw error.getCause(); }
                });
        final Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "getInventory" -> inventory;
                    case "getWorld" -> Bukkit.getWorlds().getFirst();
                    case "getLocation" -> Bukkit.getWorlds().getFirst().getSpawnLocation();
                    case "getName" -> "ShopAudit";
                    case "getServer" -> Bukkit.getServer();
                    case "openInventory" -> { view = (Inventory) args[0]; yield null; }
                    case "sendMessage", "playSound" -> null;
                    case "hashCode" -> uuid.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "ShopAudit:" + uuid;
                    default -> throw new UnsupportedOperationException("Unexpected fixture call: " + method);
                });
    }
}
