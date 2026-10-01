package io.Yomicer.magicExpansion.utils.shop;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShopGUI implements Listener {

    private static final Map<UUID, UUID> pendingShopNameCreation = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<UUID, Integer> playerMainPage = new HashMap<>();
    private static final Map<UUID, Integer> adminMainPage = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> shopTradesPage = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> adminTradesPage = new HashMap<>();

    private static final Map<UUID, Integer> probPage = new HashMap<>();

    private static final Map<UUID, ShopEditData> currentEditingData = new HashMap<>();
    private static final Set<UUID> safeClose = new HashSet<>();

    private static final int[] BLACK_MARKET_SLOTS = {
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33
    };

    private static class ShopEditData {
        String shopName;
        ItemStack result;
        List<ItemStack> costs = new ArrayList<>();
        int globalLimit = 0;
        int personalLimit = 0;
        String editing = "none";
        boolean isNew = true;
        ShopEditSession session;
    }
    private static final Map<UUID, ShopEditData> pendingEditData = new java.util.concurrent.ConcurrentHashMap<>();

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static ItemStack createBorder() {
        return createInfoItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
    }

    private static ItemStack createInfoItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static void fillBorder(Inventory inv) {
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                if (inv.getItem(i) == null) inv.setItem(i, createBorder());
            }
        }
    }

    // ================= 玩家界面 =================
    public static void openPlayerMainMenu(Player player) {
        openPlayerMainMenu(player, playerMainPage.getOrDefault(player.getUniqueId(), 0));
    }

    public static void openPlayerMainMenu(Player player, int page) {
        playerMainPage.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_GREEN + "Magic Market");
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.BOOK, ChatColor.AQUA + "Magic Market", Arrays.asList(ChatColor.GRAY + "Choose a shop to enter.")));

        List<ShopManager.Shop> shops = ShopManager.getShops();
        int maxPage = Math.max(0, (shops.size() - 1) / CONTENT_SLOTS.length);
        page = Math.min(page, maxPage);

        int startIndex = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shops.size(); i++) {
            ShopManager.Shop shop = shops.get(startIndex + i);
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + shop.name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to view available trades.", ChatColor.GRAY + "Total: " + shop.trades.size() + " trades"));
            icon.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], icon);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Previous Page", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Next Page", null));

        // 新增:每日黑市入口 (放在第49格)
        inv.setItem(49, createInfoItem(Material.WITHER_SKELETON_SKULL, ChatColor.DARK_PURPLE + "Daily Mystery Black Market", Arrays.asList(
                ChatColor.GRAY + "Refreshes with 10 mystery items every 4 hours.",
                ChatColor.GRAY + "Free rewards may appear.",
                ChatColor.GRAY + "Each player may purchase each item once.",
                ChatColor.GRAY + "Refresh chances change each day."
        )));

        player.openInventory(inv);
    }

    public static void openShopTrades(Player player, String shopName) {
        Map<String, Integer> pages = shopTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        openShopTrades(player, shopName, pages.getOrDefault(shopName, 0));
    }

    public static void openShopTrades(Player player, String shopName, int page) {
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null) return;

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_GREEN + shopName);
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.ENDER_CHEST, ChatColor.AQUA + shopName, Arrays.asList(ChatColor.GRAY + "Click an item to make the trade.")));

        int maxPage = Math.max(0, (shop.trades.size() - 1) / CONTENT_SLOTS.length);
        page = Math.max(0, Math.min(page, maxPage));
        shopTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(shopName, page);

        int startIndex = page * CONTENT_SLOTS.length;
        UUID playerId = player.getUniqueId();

        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shop.trades.size(); i++) {
            ShopManager.Trade trade = shop.trades.get(startIndex + i);
            if (trade.result == null) continue;
            ItemStack display = trade.result.clone();
            ItemMeta meta = display.getItemMeta();

            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.AQUA + "Required items:");
            if (trade.costItems != null && !trade.costItems.isEmpty()) {
                for (ItemStack cost : trade.costItems) {
                    if (cost != null && cost.getType() != Material.AIR) {
                        String name = ItemStackHelper.getDisplayName(cost);
                        lore.add(ChatColor.RED + " - " + name + " x" + cost.getAmount());
                    }
                }
            } else {
                lore.add(ChatColor.RED + " (Free)");
            }

            boolean canBuy = ShopManager.canPurchase(playerId, trade);
            if (trade.globalLimit > 0 || trade.personalLimit > 0) {
                lore.add("");
                if (trade.globalLimit > 0) {
                    lore.add(ChatColor.YELLOW + "Server-wide Remaining: " + (canBuy ? ChatColor.AQUA : ChatColor.RED) + (trade.globalLimit - trade.globalUsed));
                }
                if (trade.personalLimit > 0) {
                    int used = trade.personalUsed.getOrDefault(playerId, 0);
                    lore.add(ChatColor.YELLOW + "Personal Remaining: " + (canBuy ? ChatColor.AQUA : ChatColor.RED) + (trade.personalLimit - used));
                }
                if (!canBuy) {
                    lore.add(ChatColor.DARK_RED + "Purchase limit reached!");
                }
            }

            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], display);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Previous Page", null));
        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "Back", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Next Page", null));

        player.openInventory(inv);
    }

    // ================= 管理员界面 =================
    public static void openAdminMainMenu(Player player) {
        openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0));
    }

    public static void openAdminMainMenu(Player player, int page) {
        adminMainPage.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_RED + "Magic Market Administration");
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.WRITABLE_BOOK, ChatColor.AQUA + "Administrator Menu", Arrays.asList(ChatColor.GRAY + "Manage the server-wide Magic Market.")));

        List<ShopManager.Shop> shops = ShopManager.getShops();
        int maxPage = Math.max(0, (shops.size() - 1) / CONTENT_SLOTS.length);
        page = Math.min(page, maxPage);

        int startIndex = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shops.size(); i++) {
            ShopManager.Shop shop = shops.get(startIndex + i);
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + shop.name);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Left-click: Manage trades",
                    ChatColor.RED + "Right-click: Delete shop"
            ));
            icon.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], icon);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Previous Page", null));
        inv.setItem(49, createInfoItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "Create New Shop", Arrays.asList(ChatColor.GRAY + "Click, then enter a name in chat.")));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Next Page", null));

        inv.setItem(51, createInfoItem(Material.SUNFLOWER, ChatColor.AQUA + "Reload Shop Data", Arrays.asList(
                ChatColor.GRAY + "Click to reload data from disk.",
                ChatColor.YELLOW + "Warning: Unsaved changes will be overwritten."
        )));

        // 新增:强制刷新黑市按钮
        inv.setItem(52, createInfoItem(Material.WITHER_SKELETON_SKULL, ChatColor.DARK_PURPLE + "Force Refresh Black Market", Arrays.asList(
                ChatColor.GRAY + "Immediately refresh today's Black Market items.",
                ChatColor.YELLOW + "Remaining: " + BlackMarketManager.getTimeRemaining()
        )));

        player.openInventory(inv);
    }

    public static void openAdminTradesMenu(Player player, String shopName) {
        Map<String, Integer> pages = adminTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        openAdminTradesMenu(player, shopName, pages.getOrDefault(shopName, 0));
    }

    public static void openAdminTradesMenu(Player player, String shopName, int page) {
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null) return;

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_RED + "Manage: " + shopName);
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.ENDER_CHEST, ChatColor.AQUA + "Manage: " + shopName, Arrays.asList(ChatColor.GRAY + "Middle-click: Reset purchase counts")));

        int maxPage = Math.max(0, (shop.trades.size() - 1) / CONTENT_SLOTS.length);
        page = Math.max(0, Math.min(page, maxPage));
        adminTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(shopName, page);

        int startIndex = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shop.trades.size(); i++) {
            ShopManager.Trade trade = shop.trades.get(startIndex + i);
            if (trade.result == null) continue;
            ItemStack display = trade.result.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: Edit trade");
            lore.add(ChatColor.RED + "Right-click: Delete trade");
            lore.add(ChatColor.BLUE + "Middle-click: Reset server-wide and personal purchase counts");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], display);
            ((ShopHolder) inv.getHolder()).selections.put(CONTENT_SLOTS[i], new ShopEditSession(shop, trade));
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Previous Page", null));
        inv.setItem(49, createInfoItem(Material.LIME_DYE, ChatColor.GREEN + "Create New Trade", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Next Page", null));

        player.openInventory(inv);
    }

    public static void openTradeEditor(Player player, String shopName, ShopEditData editData) {
        ShopManager.Shop sourceShop = ShopManager.getShop(shopName);
        if (editData.session == null) {
            if (sourceShop == null) return;
            editData.session = new ShopEditSession(sourceShop, null);
        } else if (!editData.session.isCurrent(sourceShop)) {
            // Reopen the detached editor so a new trade's held items can still be returned on close.
            // The save/delete handlers refuse this stale session; do not discard its inventory here.
            player.sendMessage(ChatColor.RED + "This shop changed or is read-only. Close this editor to recover unsaved items, then reopen the shop.");
        }
        currentEditingData.put(player.getUniqueId(), editData);

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_RED + "Configure: " + shopName);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createBorder());
        }

        inv.setItem(4, createInfoItem(Material.ANVIL, ChatColor.AQUA + "Trade Editor", Arrays.asList(ChatColor.GRAY + "Configure trade items and limits.")));

        inv.setItem(11, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place the received item on the right →→", null));
        inv.setItem(12, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place the received item on the right →→", null));
        inv.setItem(14, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "←← Place required items on the left", null));
        inv.setItem(15, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "←← Place required items on the left", null));

        if (editData.result != null) {
            inv.setItem(13, editData.result.clone());
        } else {
            inv.setItem(13, null);
        }

        inv.setItem(27, createInfoItem(Material.BLUE_STAINED_GLASS_PANE, ChatColor.BLUE + "Place required items here.", null));
        inv.setItem(35, createInfoItem(Material.BLUE_STAINED_GLASS_PANE, ChatColor.BLUE + "Place required items here.", null));

        for (int i = 28; i <= 34; i++) {
            int costIndex = i - 28;
            if (costIndex < editData.costs.size() && editData.costs.get(costIndex) != null) {
                inv.setItem(i, editData.costs.get(costIndex).clone());
            } else {
                inv.setItem(i, null);
            }
        }

        inv.setItem(40, createInfoItem(Material.COMPARATOR, ChatColor.GOLD + "Server-wide Limit: " + (editData.globalLimit == 0 ? "Unlimited" : editData.globalLimit), Arrays.asList(ChatColor.GRAY + "Left-click to set")));
        inv.setItem(41, createInfoItem(Material.PAPER, ChatColor.GOLD + "Per-player Limit: " + (editData.personalLimit == 0 ? "Unlimited" : editData.personalLimit), Arrays.asList(ChatColor.GRAY + "Left-click to set")));

        inv.setItem(45, createInfoItem(Material.RED_CONCRETE, ChatColor.RED + "Delete Trade", Collections.singletonList(ChatColor.GRAY + "Available only while editing.")));
        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "Back (Do Not Save)", null));
        inv.setItem(53, createInfoItem(Material.LIME_CONCRETE, ChatColor.GREEN + "Save Trade", null));

        player.openInventory(inv);
    }

    // ================= 黑市界面 =================
    public static void openBlackMarket(Player player) {
        BlackMarketManager.checkAndRefresh();
        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_PURPLE + "Daily Mystery Black Market");

        // 全部铺满黑色玻璃板
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createBorder());
        }

        // 顶部信息
        inv.setItem(4, createInfoItem(Material.WITHER_SKELETON_SKULL, ChatColor.LIGHT_PURPLE + "Today's Black Market", Arrays.asList(
                ChatColor.GRAY + "Automatically refreshes every 4 hours.",
                ChatColor.GRAY + "One purchase per player for each item.",
                ChatColor.YELLOW + "Time Until Refresh: " + BlackMarketManager.getTimeRemaining()
        )));

        // 清空中心 10 个槽位准备放商品
        for (int slot : BLACK_MARKET_SLOTS) {
            inv.setItem(slot, null);
        }

        List<BlackMarketManager.BlackMarketTrade> trades = BlackMarketManager.getTodayTrades(player);
        UUID playerId = player.getUniqueId();

        for (int i = 0; i < trades.size(); i++) {
            int slot = BLACK_MARKET_SLOTS[i];
            BlackMarketManager.BlackMarketTrade trade = trades.get(i);
            if (trade.result == null || trade.result.getType() == Material.AIR) continue;

            if (BlackMarketManager.hasPurchased(playerId, i)) {
                inv.setItem(slot, createInfoItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_RED + "Purchased", null));
                continue;
            }

            if (!BlackMarketManager.isRevealed(playerId, i)) {
                inv.setItem(slot, createInfoItem(Material.PURPLE_SHULKER_BOX, ChatColor.DARK_PURPLE + "Mystery Black Market Box", Arrays.asList(
                        ChatColor.GRAY + "Click to reveal the surprise!"
                )));
                continue;
            }

            ItemStack display = trade.result.clone();
            ItemMeta meta = display.getItemMeta();
            // 修复点:如果 meta 为 null,尝试从工厂获取
            if (meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(display.getType());
            }

            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");

                if (trade.isHard) lore.add(ChatColor.GOLD + "Rare Item!");
                else lore.add(ChatColor.GRAY + "Common Item");

                if (trade.isFree) {
                    lore.add(ChatColor.GOLD + "★ FREE TRADE ★");
                } else {
                    lore.add(ChatColor.DARK_RED + "---- Required Cost:");
                    if (trade.costs != null) {
                        for (ItemStack cost : trade.costs) {
                            if (cost != null && cost.getType() != Material.AIR) {
                                String name = ItemStackHelper.getDisplayName(cost);
                                lore.add(ChatColor.RED + " - " + name + " x" + cost.getAmount());
                            }
                        }
                    }
                }
                lore.add("");
                lore.add(ChatColor.GREEN + "Click again to complete the trade.");

                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(slot, display);
        }

        inv.setItem(45, createInfoItem(Material.KNOWLEDGE_BOOK, ChatColor.AQUA + "Item Probability List", Arrays.asList(ChatColor.GRAY + "Click to view every item's refresh chance.")));
        // 返回按钮放在第49格
        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "Back", null));
        player.openInventory(inv);
    }


    public static void openBlackMarketProbabilities(Player player) {
        openBlackMarketProbabilities(player, probPage.getOrDefault(player.getUniqueId(), 0));
    }

    public static void openBlackMarketProbabilities(Player player, int page) {
        List<ItemStack> simplePool = BlackMarketManager.getSimplePool();
        Map<ItemStack, Integer> hardPool = BlackMarketManager.getHardPool();

        double hardChance = BlackMarketManager.HARD_CHANCE;
        double simpleChance = BlackMarketManager.SIMPLE_CHANCE;

        Map<String, Double> nameToProb = new HashMap<>();
        Map<String, ItemStack> nameToItem = new HashMap<>();

        if (simplePool != null && !simplePool.isEmpty()) {
            double singleProb = simpleChance / simplePool.size();
            for (ItemStack item : simplePool) {
                if (item == null || item.getType() == Material.AIR) continue;
                String name = ItemStackHelper.getDisplayName(item);
                nameToProb.merge(name, singleProb, Double::sum);
                nameToItem.putIfAbsent(name, item.clone());
            }
        }

        double hardTotalWeight = 0;
        if (hardPool != null) {
            for (Integer weight : hardPool.values()) {
                hardTotalWeight += (weight == null ? 1 : weight);
            }
        }

        if (hardPool != null && hardTotalWeight > 0) {
            for (Map.Entry<ItemStack, Integer> entry : hardPool.entrySet()) {
                ItemStack item = entry.getKey();
                if (item == null || item.getType() == Material.AIR) continue;

                double weight = entry.getValue() == null ? 1 : entry.getValue();
                double singleProb = hardChance * (weight / hardTotalWeight);

                String name = ItemStackHelper.getDisplayName(item);
                nameToProb.merge(name, singleProb, Double::sum);
                nameToItem.putIfAbsent(name, item.clone());
            }
        }

        // 将 Map 转为 List 方便分页
        List<Map.Entry<String, Double>> probList = new ArrayList<>(nameToProb.entrySet());

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_AQUA + "Black Market Item Probabilities");
        fillBorder(inv);

        int maxPage = Math.max(0, (probList.size() - 1) / CONTENT_SLOTS.length);
        if (page < 0) page = 0;
        page = Math.min(page, maxPage);
        probPage.put(player.getUniqueId(), page);

        int startIndex = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < probList.size(); i++) {
            Map.Entry<String, Double> entry = probList.get(startIndex + i);
            ItemStack displayItem = nameToItem.get(entry.getKey());
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                double prob = entry.getValue() * 100;
                lore.add(ChatColor.GOLD + "Refresh Chance: " + String.format("%.5f", prob) + "%");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            inv.setItem(CONTENT_SLOTS[i], displayItem);
        }

        // 翻页按钮
        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Previous Page", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "Next Page", null));

        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "Back", null));
        player.openInventory(inv);
    }





    private void handleBlackMarketClick(Player player, int slot) {
        // 改为获取黑市专属槽位索引
        int index = getBlackMarketIndexBySlot(slot);
        if (index == -1 || index >= BlackMarketManager.getTodayTrades(player).size()) return;

        UUID playerId = player.getUniqueId();

        if (BlackMarketManager.hasPurchased(playerId, index)) {
            player.sendMessage(ChatColor.RED + "You have already purchased this item today!");
            return;
        }

        if (!BlackMarketManager.isRevealed(playerId, index)) {
            BlackMarketManager.reveal(playerId, index);
            BlackMarketManager.BlackMarketTrade trade = BlackMarketManager.getTodayTrades(player).get(index);

            org.bukkit.Sound sound;
            if (trade.isFree) {
                sound = org.bukkit.Sound.BLOCK_BELL_USE;
            } else if (trade.isHard) {
                sound = org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL;
            } else {
                sound = org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING;
            }
            player.playSound(player.getLocation(), sound, 1f, 1f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Mystery box revealed!");

            openBlackMarket(player);
            return;
        }

        handleBlackMarketPurchase(player, index);
    }

    private void handleBlackMarketPurchase(Player player, int index) {
        UUID playerId = player.getUniqueId();
        BlackMarketManager.BlackMarketTrade trade = BlackMarketManager.getTodayTrades(player).get(index);

        if (trade.result == null || trade.result.getType().isAir() || trade.result.getAmount() <= 0) return;
        ItemStack reward = trade.result.clone();
        if (!trade.isFree && !takePayment(player, trade.costs)) return;

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so some items were dropped on the ground!");
        }

        BlackMarketManager.recordPurchase(playerId, index);

        player.sendMessage(ChatColor.GREEN + "Black Market trade completed!");
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        openBlackMarket(player);
    }

    // ================= 事件处理 =================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopHolder)) return;

        if (e.getClickedInventory() == null) {
            e.setCancelled(true);
            return;
        }

        String title = e.getView().getTitle();
        Player player = (Player) e.getWhoClicked();

        if (title.startsWith(ChatColor.DARK_RED + "Configure: ")) {
            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }

            if (e.getClickedInventory().getHolder() instanceof ShopHolder) {
                int slot = e.getRawSlot();
                if (slot == 13 || (slot >= 28 && slot <= 34)) {
                    e.setCancelled(false);
                    return;
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(false);
                return;
            }
        } else {
            e.setCancelled(true);
        }

        int slot = e.getRawSlot();
        ItemStack item = e.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        // 玩家商店列表
        if (title.equals(ChatColor.DARK_GREEN + "Magic Market")) {
            if (slot == 49) {
                openBlackMarket(player);
                return;
            }
            if (slot == 45) openPlayerMainMenu(player, playerMainPage.getOrDefault(player.getUniqueId(), 0) - 1);
            else if (slot == 53) openPlayerMainMenu(player, playerMainPage.getOrDefault(player.getUniqueId(), 0) + 1);
            else if (item.getType() == Material.CHEST) {
                String shopName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                openShopTrades(player, shopName);
            }
        }
        // 玩家交易列表
        else if (title.startsWith(ChatColor.DARK_GREEN + "")) {
            if (slot == 49) {
                openPlayerMainMenu(player);
                return;
            }
            if (slot == 45 || slot == 53) {
                String shopName = ChatColor.stripColor(title.replace(ChatColor.DARK_GREEN + "", ""));
                int current = shopTradesPage.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(shopName, 0);
                openShopTrades(player, shopName, slot == 45 ? current - 1 : current + 1);
                return;
            }
            String shopName = ChatColor.stripColor(title.replace(ChatColor.DARK_GREEN + "", ""));
            handlePurchase(player, shopName, slot, item);
        }
        // 黑市界面
        else if (title.equals(ChatColor.DARK_PURPLE + "Daily Mystery Black Market")) {
            if (slot == 49) {
                openPlayerMainMenu(player);
            } else if (slot == 45) {
                openBlackMarketProbabilities(player);
            }else {
                handleBlackMarketClick(player, slot);
            }
        }
        // 黑市概率公示界面
        else if (title.equals(ChatColor.DARK_AQUA + "Black Market Item Probabilities")) {
            if (slot == 49) {
                // 返回到黑市主界面
                openBlackMarket(player);
            } else if (slot == 45) {
                // 上一页
                openBlackMarketProbabilities(player, probPage.getOrDefault(player.getUniqueId(), 0) - 1);
            } else if (slot == 53) {
                // 下一页
                openBlackMarketProbabilities(player, probPage.getOrDefault(player.getUniqueId(), 0) + 1);
            }
        }
        // 管理员商店列表
        else if (title.equals(ChatColor.DARK_RED + "Magic Market Administration")) {
            if (slot == 51) {
                player.closeInventory();
                ShopManager.reload();
                player.sendMessage(ChatColor.GREEN + "Magic Market data reloaded!");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
                Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0)), 1L);
                return;
            }
            if (slot == 52) {
                BlackMarketManager.forceRefresh();
                player.sendMessage(ChatColor.DARK_PURPLE + "Black Market forcibly refreshed!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
                openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0));
                return;
            }
            if (slot == 45) openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0) - 1);
            else if (slot == 53) openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0) + 1);
            else if (slot == 49) {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Enter the new shop name in chat, or type 'cancel' to cancel.");
                pendingShopNameCreation.put(player.getUniqueId(), UUID.randomUUID());
            } else if (item.getType() == Material.CHEST) {
                String shopName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if (e.isRightClick()) {
                    if (!ShopManager.tryDeleteShop(shopName)) {
                        player.sendMessage(ChatColor.RED + "Shop deletion refused; original data retained. Check the console.");
                        return;
                    }
                    openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0));
                } else if (e.isLeftClick()) {
                    openAdminTradesMenu(player, shopName);
                }
            }
        }
        // 管理员交易列表
        else if (title.startsWith(ChatColor.DARK_RED + "Manage: ")) {
            String shopName = title.replace(ChatColor.DARK_RED + "Manage: ", "");
            if (slot == 49) {
                ShopEditData data = new ShopEditData();
                data.shopName = shopName;
                data.isNew = true;
                openTradeEditor(player, shopName, data);
                return;
            }
            if (slot == 45 || slot == 53) {
                int current = adminTradesPage.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(shopName, 0);
                openAdminTradesMenu(player, shopName, slot == 45 ? current - 1 : current + 1);
                return;
            }
            ShopManager.Shop shop = ShopManager.getShop(shopName);
            ShopEditSession selection = ((ShopHolder) e.getInventory().getHolder()).selections.get(slot);
            if (shop == null || selection == null || !selection.isCurrent(shop)) {
                player.sendMessage(ChatColor.RED + "This shop changed or is read-only. Reopen it before editing.");
                return;
            }
            if (e.isRightClick()) {
                if (!selection.delete(shop) || !saveShopForEditor(player, shop)) return;
                openAdminTradesMenu(player, shopName);
            } else if (e.isLeftClick()) {
                ShopManager.Trade t = selection.selected();
                ShopEditData data = new ShopEditData();
                data.shopName = shopName;
                data.result = t.result == null ? null : t.result.clone();
                data.costs = new ArrayList<>();
                if (t.costItems != null) for (ItemStack cost : t.costItems) data.costs.add(cost == null ? null : cost.clone());
                data.globalLimit = t.globalLimit;
                data.personalLimit = t.personalLimit;
                data.isNew = false;
                data.session = selection;
                openTradeEditor(player, shopName, data);
            } else if (e.getClick() == ClickType.MIDDLE) {
                ShopManager.resetUsage(shop, selection.selected());
                if (!ShopManager.isAvailable(shop)) {
                    player.sendMessage(ChatColor.RED + "Purchase-count save failed; check the console before retrying.");
                    return;
                }
                player.sendMessage(ChatColor.GREEN + "Purchase counts for this trade were reset!");
                openAdminTradesMenu(player, shopName);
            }
        }
        // 管理员编辑器
        else if (title.startsWith(ChatColor.DARK_RED + "Configure: ")) {
            String shopName = title.replace(ChatColor.DARK_RED + "Configure: ", "");
            ShopEditData previous = currentEditingData.get(player.getUniqueId());
            if (previous == null) return;
            ShopEditData currentData = readEditDataFromInventory(e.getInventory(), shopName);
            currentData.isNew = previous.isNew;
            currentData.session = previous.session;

            if (slot == 45) {
                ShopManager.Shop shop = ShopManager.getShop(shopName);
                if (currentData.session == null || !currentData.session.delete(shop)) {
                    player.sendMessage(ChatColor.RED + "No unchanged existing trade is selected; nothing was deleted.");
                    return;
                }
                if (!saveShopForEditor(player, shop)) return;
                safeClose.add(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Trade deleted!");
                openAdminTradesMenu(player, shopName);
                return;
            }
            if (slot == 49) {
                openAdminTradesMenu(player, shopName);
                return;
            }
            if (slot == 40) {
                safeClose.add(player.getUniqueId());
                player.closeInventory();
                currentData.editing = "global";
                pendingEditData.put(player.getUniqueId(), currentData);
                player.sendMessage(ChatColor.GREEN + "Enter the server-wide purchase limit in chat (0 for unlimited), or type 'cancel' to cancel.");
                return;
            }
            if (slot == 41) {
                safeClose.add(player.getUniqueId());
                player.closeInventory();
                currentData.editing = "personal";
                pendingEditData.put(player.getUniqueId(), currentData);
                player.sendMessage(ChatColor.GREEN + "Enter the per-player purchase limit in chat (0 for unlimited), or type 'cancel' to cancel.");
                return;
            }
            if (slot == 53) {
                ShopManager.Shop shop = ShopManager.getShop(shopName);
                if (currentData.session == null || !currentData.session.isCurrent(shop)) {
                    player.sendMessage(ChatColor.RED + "This trade changed or is read-only. Reopen it before saving.");
                    return;
                }
                if (currentData.result == null || currentData.result.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "Place the trade result in slot 13 first!");
                    return;
                }
                if (currentData.session.apply(shop, currentData.result, currentData.costs,
                        currentData.globalLimit, currentData.personalLimit) == null) return;
                if (!saveShopForEditor(player, shop)) return;
                safeClose.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Trade saved successfully!");
                openAdminTradesMenu(player, shopName);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopHolder)) return;
        String title = e.getView().getTitle();
        if (title.startsWith(ChatColor.DARK_RED + "Configure: ")) {
            for (int slot : e.getRawSlots()) {
                if (slot != 13 && (slot < 28 || slot > 34)) {
                    e.setCancelled(true);
                    return;
                }
            }
            e.setCancelled(false);
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopHolder)) return;
        String title = e.getView().getTitle();
        Player player = (Player) e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (title.startsWith(ChatColor.DARK_RED + "Configure: ")) {
            ShopEditData data = currentEditingData.get(uuid);
            currentEditingData.remove(uuid);

            if (safeClose.contains(uuid)) {
                safeClose.remove(uuid);
                return;
            }

            if (data != null && data.isNew) {
                boolean hasItem = false;
                List<Integer> slotsToCheck = new ArrayList<>();
                slotsToCheck.add(13);
                for(int i = 28; i <= 34; i++) slotsToCheck.add(i);

                for (int slot : slotsToCheck) {
                    ItemStack item = e.getInventory().getItem(slot);
                    if (item != null && item.getType() != Material.AIR && !item.getType().name().endsWith("STAINED_GLASS_PANE")) {
                        hasItem = true;
                        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                        for (ItemStack drop : overflow.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                        e.getInventory().setItem(slot, null);
                    }
                }
                if (hasItem) {
                    player.sendMessage(ChatColor.YELLOW + "Unsaved items were returned to your inventory!");
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        UUID creation = pendingShopNameCreation.get(uuid);
        ShopEditData editing = pendingEditData.get(uuid);
        if (creation == null && editing == null) return;
        // Cancel the actual chat event now; shared shop/UI state is touched only by the server task.
        e.setCancelled(true);
        String message = e.getMessage().trim();
        Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> applyChatInput(player, creation, editing, message));
    }

    private void applyChatInput(Player player, UUID creation, ShopEditData editing, String message) {
        UUID uuid = player.getUniqueId();
        if (!player.isOnline()) return;
        if (creation != null) {
            if (!creation.equals(pendingShopNameCreation.get(uuid))) return;
            if (message.equalsIgnoreCase("cancel")) {
                pendingShopNameCreation.remove(uuid, creation);
                player.sendMessage(ChatColor.YELLOW + "Shop creation cancelled.");
            } else if (ShopManager.tryCreateShop(message)) {
                pendingShopNameCreation.remove(uuid, creation);
                player.sendMessage(ChatColor.GREEN + "Shop " + message + " created successfully!");
                openAdminMainMenu(player);
            } else {
                player.sendMessage(ChatColor.RED + "Shop creation refused: the name is invalid, already used, or its file is protected. Check the console, choose another name, or type 'cancel'.");
            }
            return;
        }
        if (editing == null || !pendingEditData.remove(uuid, editing)) return;
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "Limit setting cancelled.");
        } else {
            try {
                int amount = Math.max(0, Integer.parseInt(message));
                if (editing.editing.equals("global")) {
                    editing.globalLimit = amount;
                    player.sendMessage(ChatColor.GREEN + "Server-wide purchase limit set to: " + (amount == 0 ? "Unlimited" : amount));
                } else if (editing.editing.equals("personal")) {
                    editing.personalLimit = amount;
                    player.sendMessage(ChatColor.GREEN + "Per-player purchase limit set to: " + (amount == 0 ? "Unlimited" : amount));
                }
            } catch (NumberFormatException ex) {
                player.sendMessage(ChatColor.RED + "Invalid input; enter a number!");
            }
        }
        openTradeEditor(player, editing.shopName, editing);
    }

    private boolean saveShopForEditor(Player player, ShopManager.Shop shop) {
        if (ShopManager.trySaveShop(shop)) return true;
        player.sendMessage(ChatColor.RED + "Shop save failed; the original file was retained. Check the console and repair/reload before retrying.");
        return false;
    }

    // ================= 辅助逻辑 =================
    private void handlePurchase(Player player, String shopName, int slot, ItemStack clickedItem) {
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null) return;
        int page = shopTradesPage.getOrDefault(player.getUniqueId(), Collections.emptyMap()).getOrDefault(shopName, 0);
        int index = purchaseIndex(page, slot, shop.trades.size());
        if (index == -1) return;

        ShopManager.Trade trade = shop.trades.get(index);
        if (trade.result == null || trade.result.getType().isAir() || trade.result.getAmount() <= 0) return;
        ItemStack reward = trade.result.clone();

        UUID playerId = player.getUniqueId();
        if (!ShopManager.canPurchase(playerId, trade)) {
            player.sendMessage(ChatColor.RED + "Purchase limit reached!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (!takePayment(player, trade.costItems)) return;

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so some items were dropped on the ground!");
        }

        ShopManager.recordPurchase(playerId, shop, trade);

        player.sendMessage(ChatColor.GREEN + "Trade completed successfully!");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        openShopTrades(player, shopName, shopTradesPage.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(shopName, 0));
    }

    private boolean takePayment(Player player, List<ItemStack> costs) {
        final ItemStack missing;
        try {
            missing = InventoryPayment.debit(player.getInventory(), costs);
        } catch (IllegalArgumentException invalidCost) {
            player.sendMessage(ChatColor.RED + "This trade has an invalid cost; no items were taken.");
            return false;
        }
        if (missing == null) return true;
        player.sendMessage(ChatColor.RED + "Missing items: " + ItemStackHelper.getDisplayName(missing)
                + " x" + missing.getAmount() + ". No items were taken.");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        return false;
    }

    static int purchaseIndex(int page, int slot, int tradeCount) {
        if (page < 0 || tradeCount <= 0) return -1;
        for (int offset = 0; offset < CONTENT_SLOTS.length; offset++) {
            if (CONTENT_SLOTS[offset] == slot) {
                long index = (long) page * CONTENT_SLOTS.length + offset;
                return index < tradeCount ? (int) index : -1;
            }
        }
        return -1;
    }

    private int getTradeIndexBySlot(int slot) {
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (CONTENT_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    private int getBlackMarketIndexBySlot(int slot) {
        for (int i = 0; i < BLACK_MARKET_SLOTS.length; i++) {
            if (BLACK_MARKET_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    private ShopEditData readEditDataFromInventory(Inventory inv, String shopName) {
        ShopEditData data = new ShopEditData();
        data.shopName = shopName;

        ItemStack result = inv.getItem(13);
        if (result != null && result.getType() != Material.AIR && !result.getType().name().endsWith("STAINED_GLASS_PANE")) {
            data.result = result.clone();
        }

        for (int i = 28; i <= 34; i++) {
            ItemStack cost = inv.getItem(i);
            if (cost != null && cost.getType() != Material.AIR && !cost.getType().name().endsWith("STAINED_GLASS_PANE")) {
                data.costs.add(cost.clone());
            }
        }

        ItemStack gItem = inv.getItem(40);
        if (gItem != null && gItem.hasItemMeta()) {
            String name = gItem.getItemMeta().getDisplayName();
            String numStr = name.replace(ChatColor.GOLD + "Server-wide Limit: ", "").replaceAll("[^0-9]", "");
            try {
                if (!name.contains("Unlimited")) data.globalLimit = Integer.parseInt(numStr);
            } catch (Exception ignored) {}
        }

        ItemStack pItem = inv.getItem(41);
        if (pItem != null && pItem.hasItemMeta()) {
            String name = pItem.getItemMeta().getDisplayName();
            String numStr = name.replace(ChatColor.GOLD + "Per-player Limit: ", "").replaceAll("[^0-9]", "");
            try {
                if (!name.contains("Unlimited")) data.personalLimit = Integer.parseInt(numStr);
            } catch (Exception ignored) {}
        }
        return data;
    }

    public static class ShopHolder implements InventoryHolder {
        private final Map<Integer, ShopEditSession> selections = new HashMap<>();
        @NotNull
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
