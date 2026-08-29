package io.Yomicer.magicExpansion.utils.shop;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
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
import java.util.concurrent.ConcurrentHashMap;

public class ShopGUI implements Listener {

    // 修复：全部 static 会话集合改为 ConcurrentHashMap（异步聊天线程也会访问），支持静态 cleanup 清理
    private static final Map<UUID, String> pendingShopNameCreation = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> playerMainPage = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> adminMainPage = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Integer>> shopTradesPage = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Integer>> adminTradesPage = new ConcurrentHashMap<>();

    private static final Map<UUID, Integer> probPage = new ConcurrentHashMap<>();

    private static final Map<UUID, ShopEditData> currentEditingData = new ConcurrentHashMap<>();
    // 修复：Set 改为并发安全集合
    private static final Set<UUID> safeClose = ConcurrentHashMap.newKeySet();

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
    }
    private static final Map<UUID, ShopEditData> pendingEditData = new ConcurrentHashMap<>();

    /**
     * 修复：玩家退出时遍历清理该玩家在所有会话 Map 中的数据，防止内存泄漏
     */
    public static void cleanup(UUID uuid) {
        pendingShopNameCreation.remove(uuid);
        playerMainPage.remove(uuid);
        adminMainPage.remove(uuid);
        shopTradesPage.remove(uuid);
        adminTradesPage.remove(uuid);
        probPage.remove(uuid);
        currentEditingData.remove(uuid);
        safeClose.remove(uuid);
        pendingEditData.remove(uuid);
    }

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
        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_GREEN + "魔法·集市");
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.BOOK, ChatColor.AQUA + "魔法·集市", Arrays.asList(ChatColor.GRAY + "请选择你要进入的商店")));

        List<ShopManager.Shop> shops = ShopManager.getShops();
        int maxPage = Math.max(0, (shops.size() - 1) / CONTENT_SLOTS.length);
        page = Math.min(page, maxPage);

        int startIndex = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shops.size(); i++) {
            ShopManager.Shop shop = shops.get(startIndex + i);
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + shop.name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "点击查看兑换列表", ChatColor.GRAY + "共有 " + shop.trades.size() + " 个交易"));
            icon.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], icon);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "上一页", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "下一页", null));

        // 新增：每日黑市入口 (放在第49格)
        inv.setItem(49, createInfoItem(Material.WITHER_SKELETON_SKULL, ChatColor.DARK_PURPLE + "每日神秘黑市", Arrays.asList(
                ChatColor.GRAY + "每4小时刷新 10 个神秘商品",
                ChatColor.GRAY + "有概率出现免费好礼",
                ChatColor.GRAY + "每人每款商品限购 1 次",
                ChatColor.GRAY + "每天商品的刷新概率均会变化哦"
        )));

        player.openInventory(inv);
    }

    public static void openShopTrades(Player player, String shopName) {
        Map<String, Integer> pages = shopTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        openShopTrades(player, shopName, pages.getOrDefault(shopName, 0));
    }

    public static void openShopTrades(Player player, String shopName, int page) {
        shopTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(shopName, page);
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null) return;

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_GREEN + shopName);
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.ENDER_CHEST, ChatColor.AQUA + shopName, Arrays.asList(ChatColor.GRAY + "点击物品进行兑换")));

        int maxPage = Math.max(0, (shop.trades.size() - 1) / CONTENT_SLOTS.length);
        page = Math.min(page, maxPage);

        int startIndex = page * CONTENT_SLOTS.length;
        UUID playerId = player.getUniqueId();

        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shop.trades.size(); i++) {
            ShopManager.Trade trade = shop.trades.get(startIndex + i);
            if (trade.result == null) continue;
            ItemStack display = trade.result.clone();
            ItemMeta meta = display.getItemMeta();

            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.AQUA + "兑换所需物品：");
            if (trade.costItems != null && !trade.costItems.isEmpty()) {
                for (ItemStack cost : trade.costItems) {
                    if (cost != null && cost.getType() != Material.AIR) {
                        String name = ItemStackHelper.getDisplayName(cost);
                        lore.add(ChatColor.RED + " - " + name + " x" + cost.getAmount());
                    }
                }
            } else {
                lore.add(ChatColor.RED + " (免费)");
            }

            boolean canBuy = ShopManager.canPurchase(playerId, trade);
            if (trade.globalLimit > 0 || trade.personalLimit > 0) {
                lore.add("");
                if (trade.globalLimit > 0) {
                    lore.add(ChatColor.YELLOW + "全服剩余: " + (canBuy ? ChatColor.AQUA : ChatColor.RED) + (trade.globalLimit - trade.globalUsed));
                }
                if (trade.personalLimit > 0) {
                    int used = trade.personalUsed.getOrDefault(playerId, 0);
                    lore.add(ChatColor.YELLOW + "个人剩余: " + (canBuy ? ChatColor.AQUA : ChatColor.RED) + (trade.personalLimit - used));
                }
                if (!canBuy) {
                    lore.add(ChatColor.DARK_RED + "已达购买上限！");
                }
            }

            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], display);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "上一页", null));
        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "返回", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "下一页", null));

        player.openInventory(inv);
    }

    // ================= 管理员界面 =================
    public static void openAdminMainMenu(Player player) {
        openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0));
    }

    public static void openAdminMainMenu(Player player, int page) {
        adminMainPage.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_RED + "魔法·集市管理");
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.WRITABLE_BOOK, ChatColor.AQUA + "管理员菜单", Arrays.asList(ChatColor.GRAY + "管理全服魔法·集市")));

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
                    ChatColor.GRAY + "左键: 管理内部交易",
                    ChatColor.RED + "右键: 删除商店"
            ));
            icon.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], icon);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "上一页", null));
        inv.setItem(49, createInfoItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "创建新商店", Arrays.asList(ChatColor.GRAY + "点击在聊天框输入名字")));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "下一页", null));

        inv.setItem(51, createInfoItem(Material.SUNFLOWER, ChatColor.AQUA + "热重载商店数据", Arrays.asList(
                ChatColor.GRAY + "点击从文件夹重新读取数据",
                ChatColor.YELLOW + "注意：会覆盖未保存的修改"
        )));

        // 新增：强制刷新黑市按钮
        inv.setItem(52, createInfoItem(Material.WITHER_SKELETON_SKULL, ChatColor.DARK_PURPLE + "强制刷新黑市", Arrays.asList(
                ChatColor.GRAY + "立即刷新今日黑市物品",
                ChatColor.YELLOW + "当前剩余: " + BlackMarketManager.getTimeRemaining()
        )));

        player.openInventory(inv);
    }

    public static void openAdminTradesMenu(Player player, String shopName) {
        Map<String, Integer> pages = adminTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        openAdminTradesMenu(player, shopName, pages.getOrDefault(shopName, 0));
    }

    public static void openAdminTradesMenu(Player player, String shopName, int page) {
        adminTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(shopName, page);
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null) return;

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_RED + "管理: " + shopName);
        fillBorder(inv);

        inv.setItem(4, createInfoItem(Material.ENDER_CHEST, ChatColor.AQUA + "管理: " + shopName, Arrays.asList(ChatColor.GRAY + "中键重置购买次数")));

        int maxPage = Math.max(0, (shop.trades.size() - 1) / CONTENT_SLOTS.length);
        page = Math.min(page, maxPage);

        int startIndex = page * CONTENT_SLOTS.length;
        for (int i = 0; i < CONTENT_SLOTS.length && (startIndex + i) < shop.trades.size(); i++) {
            ShopManager.Trade trade = shop.trades.get(startIndex + i);
            if (trade.result == null) continue;
            ItemStack display = trade.result.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "左键: 编辑交易");
            lore.add(ChatColor.RED + "右键: 删除交易");
            lore.add(ChatColor.BLUE + "中键: 重置全服和个人购买次数");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i], display);
        }

        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "上一页", null));
        inv.setItem(49, createInfoItem(Material.LIME_DYE, ChatColor.GREEN + "新建交易", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "下一页", null));

        player.openInventory(inv);
    }

    public static void openTradeEditor(Player player, String shopName, ShopEditData editData) {
        currentEditingData.put(player.getUniqueId(), editData);

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_RED + "配置: " + shopName);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createBorder());
        }

        inv.setItem(4, createInfoItem(Material.ANVIL, ChatColor.AQUA + "交易编辑器", Arrays.asList(ChatColor.GRAY + "配置兑换与限制")));

        inv.setItem(11, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "右侧放置兑换物→→", null));
        inv.setItem(12, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "右侧放置兑换物→→", null));
        inv.setItem(14, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "←←左侧放置兑换物", null));
        inv.setItem(15, createInfoItem(Material.PINK_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "←←左侧放置兑换物", null));

        if (editData.result != null) {
            inv.setItem(13, editData.result.clone());
        } else {
            inv.setItem(13, null);
        }

        inv.setItem(27, createInfoItem(Material.BLUE_STAINED_GLASS_PANE, ChatColor.BLUE + "放置消耗品", null));
        inv.setItem(35, createInfoItem(Material.BLUE_STAINED_GLASS_PANE, ChatColor.BLUE + "放置消耗品", null));

        for (int i = 28; i <= 34; i++) {
            int costIndex = i - 28;
            if (costIndex < editData.costs.size() && editData.costs.get(costIndex) != null) {
                inv.setItem(i, editData.costs.get(costIndex).clone());
            } else {
                inv.setItem(i, null);
            }
        }

        inv.setItem(40, createInfoItem(Material.COMPARATOR, ChatColor.GOLD + "全服限购: " + (editData.globalLimit == 0 ? "无限制" : editData.globalLimit), Arrays.asList(ChatColor.GRAY + "左键设置")));
        inv.setItem(41, createInfoItem(Material.PAPER, ChatColor.GOLD + "个人限购: " + (editData.personalLimit == 0 ? "无限制" : editData.personalLimit), Arrays.asList(ChatColor.GRAY + "左键设置")));

        inv.setItem(45, createInfoItem(Material.RED_CONCRETE, ChatColor.RED + "删除交易", Collections.singletonList(ChatColor.GRAY + "仅编辑时有效")));
        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "返回(不保存)", null));
        inv.setItem(53, createInfoItem(Material.LIME_CONCRETE, ChatColor.GREEN + "保存交易", null));

        player.openInventory(inv);
    }

    // ================= 黑市界面 =================
    public static void openBlackMarket(Player player) {
        BlackMarketManager.checkAndRefresh();
        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_PURPLE + "每日神秘黑市");

        // 全部铺满黑色玻璃板
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createBorder());
        }

        // 顶部信息
        inv.setItem(4, createInfoItem(Material.WITHER_SKELETON_SKULL, ChatColor.LIGHT_PURPLE + "今日黑市", Arrays.asList(
                ChatColor.GRAY + "每4小时自动刷新",
                ChatColor.GRAY + "每人每款限购1次",
                ChatColor.YELLOW + "距离下次刷新: " + BlackMarketManager.getTimeRemaining()
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
                inv.setItem(slot, createInfoItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_RED + "已购买", null));
                continue;
            }

            if (!BlackMarketManager.isRevealed(playerId, i)) {
                inv.setItem(slot, createInfoItem(Material.PURPLE_SHULKER_BOX, ChatColor.DARK_PURPLE + "神秘黑市盲盒", Arrays.asList(
                        ChatColor.GRAY + "点击揭开惊喜！"
                )));
                continue;
            }

            ItemStack display = trade.result.clone();
            ItemMeta meta = display.getItemMeta();
            // 修复点：如果 meta 为 null，尝试从工厂获取
            if (meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(display.getType());
            }

            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");

                if (trade.isHard) lore.add(ChatColor.GOLD + "稀有物品！");
                else lore.add(ChatColor.GRAY + "普通物品");

                if (trade.isFree) {
                    lore.add(ChatColor.GOLD + "★ 免费兑换 ★");
                } else {
                    lore.add(ChatColor.DARK_RED + "----需要消耗：");
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
                lore.add(ChatColor.GREEN + "再次点击进行兑换");

                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(slot, display);
        }

        inv.setItem(45, createInfoItem(Material.KNOWLEDGE_BOOK, ChatColor.AQUA + "物品概率公示", Arrays.asList(ChatColor.GRAY + "点击查看所有物品的刷出概率")));
        // 返回按钮放在第49格
        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "返回", null));
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

        Inventory inv = Bukkit.createInventory(new ShopHolder(), 54, ChatColor.DARK_AQUA + "黑市物品概率公示");
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
                lore.add(ChatColor.GOLD + "刷新概率: " + String.format("%.5f", prob) + "%");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            inv.setItem(CONTENT_SLOTS[i], displayItem);
        }

        // 翻页按钮
        if (page > 0) inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.YELLOW + "上一页", null));
        if (page < maxPage) inv.setItem(53, createInfoItem(Material.ARROW, ChatColor.YELLOW + "下一页", null));

        inv.setItem(49, createInfoItem(Material.ARROW, ChatColor.GRAY + "返回", null));
        player.openInventory(inv);
    }





    private void handleBlackMarketClick(Player player, int slot) {
        // 改为获取黑市专属槽位索引
        int index = getBlackMarketIndexBySlot(slot);
        if (index == -1 || index >= BlackMarketManager.getTodayTrades(player).size()) return;

        UUID playerId = player.getUniqueId();

        if (BlackMarketManager.hasPurchased(playerId, index)) {
            player.sendMessage(ChatColor.RED + "你今天已经购买过这个商品了！");
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
            player.sendMessage(ChatColor.LIGHT_PURPLE + "盲盒已揭开！");

            openBlackMarket(player);
            return;
        }

        handleBlackMarketPurchase(player, index);
    }

    private void handleBlackMarketPurchase(Player player, int index) {
        UUID playerId = player.getUniqueId();
        BlackMarketManager.BlackMarketTrade trade = BlackMarketManager.getTodayTrades(player).get(index);

        if (!trade.isFree && trade.costs != null) {
            for (ItemStack cost : trade.costs) {
                if (cost == null || cost.getType() == Material.AIR) continue;
                if (!player.getInventory().containsAtLeast(cost, cost.getAmount())) {
                    String costName = ItemStackHelper.getDisplayName(cost);
                    player.sendMessage(ChatColor.RED + "缺少物品: " + costName + " x" + cost.getAmount());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
            }

            for (ItemStack cost : trade.costs) {
                if (cost == null || cost.getType() == Material.AIR) continue;
                ItemStack costToRemove = cost.clone();
                HashMap<Integer, ItemStack> notRemoved = player.getInventory().removeItem(costToRemove);
                if (!notRemoved.isEmpty()) {
                    for (ItemStack leftOver : notRemoved.values()) {
                        removeItems(player.getInventory(), leftOver);
                    }
                }
            }
        }

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(trade.result.clone());
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(ChatColor.YELLOW + "背包已满，部分物品掉落在地上！");
        }

        BlackMarketManager.recordPurchase(playerId, index);

        player.sendMessage(ChatColor.GREEN + "黑市兑换成功！");
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

        if (title.startsWith(ChatColor.DARK_RED + "配置: ")) {
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
        if (title.equals(ChatColor.DARK_GREEN + "魔法·集市")) {
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
        else if (title.equals(ChatColor.DARK_PURPLE + "每日神秘黑市")) {
            if (slot == 49) {
                openPlayerMainMenu(player);
            } else if (slot == 45) {
                openBlackMarketProbabilities(player);
            }else {
                handleBlackMarketClick(player, slot);
            }
        }
        // 黑市概率公示界面
        else if (title.equals(ChatColor.DARK_AQUA + "黑市物品概率公示")) {
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
        else if (title.equals(ChatColor.DARK_RED + "魔法·集市管理")) {
            if (slot == 51) {
                player.closeInventory();
                ShopManager.reload();
                player.sendMessage(ChatColor.GREEN + "魔法·集市数据已热重载！");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
                Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0)), 1L);
                return;
            }
            if (slot == 52) {
                BlackMarketManager.forceRefresh();
                player.sendMessage(ChatColor.DARK_PURPLE + "黑市已强制刷新！");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
                openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0));
                return;
            }
            if (slot == 45) openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0) - 1);
            else if (slot == 53) openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0) + 1);
            else if (slot == 49) {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "请在聊天框输入新商店的名称，输入 'cancel' 取消。");
                pendingShopNameCreation.put(player.getUniqueId(), "");
            } else if (item.getType() == Material.CHEST) {
                String shopName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if (e.isRightClick()) {
                    ShopManager.deleteShop(shopName);
                    openAdminMainMenu(player, adminMainPage.getOrDefault(player.getUniqueId(), 0));
                } else if (e.isLeftClick()) {
                    openAdminTradesMenu(player, shopName);
                }
            }
        }
        // 管理员交易列表
        else if (title.startsWith(ChatColor.DARK_RED + "管理: ")) {
            String shopName = title.replace(ChatColor.DARK_RED + "管理: ", "");
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
            int index = getTradeIndexBySlot(slot);
            ShopManager.Shop shop = ShopManager.getShop(shopName);
            if (shop == null || index == -1 || index >= shop.trades.size()) return;

            if (e.isRightClick()) {
                shop.trades.remove(index);
                ShopManager.saveShop(shop);
                openAdminTradesMenu(player, shopName, adminTradesPage.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(shopName, 0));
            } else if (e.isLeftClick()) {
                ShopManager.Trade t = shop.trades.get(index);
                ShopEditData data = new ShopEditData();
                data.shopName = shopName;
                data.result = t.result;
                data.costs = t.costItems;
                data.globalLimit = t.globalLimit;
                data.personalLimit = t.personalLimit;
                data.isNew = false;
                openTradeEditor(player, shopName, data);
            } else if (e.getClick() == ClickType.MIDDLE) {
                ShopManager.resetUsage(shop, shop.trades.get(index));
                player.sendMessage(ChatColor.GREEN + "已重置该交易的购买次数！");
                openAdminTradesMenu(player, shopName, adminTradesPage.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(shopName, 0));
            }
        }
        // 管理员编辑器
        else if (title.startsWith(ChatColor.DARK_RED + "配置: ")) {
            String shopName = title.replace(ChatColor.DARK_RED + "配置: ", "");
            ShopEditData currentData = readEditDataFromInventory(e.getInventory(), shopName);
            // 修复：currentEditingData 可能不存在（重载/重启/会话丢失），判空防止 NPE
            ShopEditData editingData = currentEditingData.get(player.getUniqueId());
            if (editingData == null) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "编辑会话已失效，请重新打开交易编辑器！");
                return;
            }
            currentData.isNew = editingData.isNew;

            if (slot == 45) {
                ShopManager.Shop shop = ShopManager.getShop(shopName);
                safeClose.add(player.getUniqueId());
                if (currentData.result != null) {
                    shop.trades.removeIf(t -> t.result != null && SlimefunUtils.isItemSimilar(t.result, currentData.result, true));
                    ShopManager.saveShop(shop);
                    player.sendMessage(ChatColor.RED + "交易已删除！");
                }
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
                player.sendMessage(ChatColor.GREEN + "请在聊天框输入全服限购次数 (0表示无限制)，输入 'cancel' 取消。");
                return;
            }
            if (slot == 41) {
                safeClose.add(player.getUniqueId());
                player.closeInventory();
                currentData.editing = "personal";
                pendingEditData.put(player.getUniqueId(), currentData);
                player.sendMessage(ChatColor.GREEN + "请在聊天框输入个人限购次数 (0表示无限制)，输入 'cancel' 取消。");
                return;
            }
            if (slot == 53) {
                ShopManager.Shop shop = ShopManager.getShop(shopName);
                safeClose.add(player.getUniqueId());

                ShopManager.Trade newTrade = new ShopManager.Trade();
                newTrade.result = currentData.result;
                newTrade.costItems = currentData.costs;
                newTrade.globalLimit = currentData.globalLimit;
                newTrade.personalLimit = currentData.personalLimit;

                if (newTrade.result == null || newTrade.result.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "请先在槽位 13 放置兑换产物！");
                    safeClose.remove(player.getUniqueId());
                    openTradeEditor(player, shopName, currentData);
                    return;
                }

                int existIndex = -1;
                for (int i = 0; i < shop.trades.size(); i++) {
                    ShopManager.Trade t = shop.trades.get(i);
                    if (t.result != null && SlimefunUtils.isItemSimilar(t.result, newTrade.result, true)) {
                        existIndex = i;
                        break;
                    }
                }

                if (existIndex != -1) {
                    shop.trades.set(existIndex, newTrade);
                } else {
                    shop.trades.add(newTrade);
                }
                ShopManager.saveShop(shop);
                player.sendMessage(ChatColor.GREEN + "交易保存成功！");
                openAdminTradesMenu(player, shopName);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof ShopHolder)) return;
        String title = e.getView().getTitle();
        if (title.startsWith(ChatColor.DARK_RED + "配置: ")) {
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

        if (title.startsWith(ChatColor.DARK_RED + "配置: ")) {
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
                    player.sendMessage(ChatColor.YELLOW + "检测到未保存的物品，已退回到你的背包！");
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (pendingShopNameCreation.containsKey(uuid)) {
            e.setCancelled(true);
            // 修复：异步聊天线程中只读取输入文本，随后切回主线程再操作 Map 与商店数据（避免异步修改集合/写盘）
            String msg = e.getMessage().trim();
            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
                // 等待标记可能在切线程前被清理（退出/取消），需再次校验
                if (!pendingShopNameCreation.containsKey(uuid)) return;

                if (msg.equalsIgnoreCase("cancel")) {
                    player.sendMessage(ChatColor.YELLOW + "已取消创建商店。");
                    pendingShopNameCreation.remove(uuid);
                } else {
                    boolean exists = false;
                    for (ShopManager.Shop shop : ShopManager.getShops()) {
                        if (shop.name.equals(msg)) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        player.sendMessage(ChatColor.RED + "已存在同名商店 " + msg + " ，请重新输入名称或输入 'cancel' 取消。");
                    } else {
                        ShopManager.createShop(msg);
                        player.sendMessage(ChatColor.GREEN + "商店 " + msg + " 创建成功！");
                        pendingShopNameCreation.remove(uuid);
                        openAdminMainMenu(player);
                    }
                }
            });
            return;
        }

        if (pendingEditData.containsKey(uuid)) {
            e.setCancelled(true);
            // 修复：异步聊天线程中只读取输入文本，随后切回主线程再操作 Map 与数据修改
            String msg = e.getMessage().trim();
            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
                // 等待标记可能在切线程前被清理，需再次校验
                ShopEditData data = pendingEditData.get(uuid);
                if (data == null) return;

                if (msg.equalsIgnoreCase("cancel")) {
                    player.sendMessage(ChatColor.YELLOW + "已取消设置次数。");
                } else {
                    try {
                        int amount = Math.max(0, Integer.parseInt(msg));
                        if (data.editing.equals("global")) {
                            data.globalLimit = amount;
                            player.sendMessage(ChatColor.GREEN + "全服限购次数已设置为: " + (amount == 0 ? "无限制" : amount));
                        } else if (data.editing.equals("personal")) {
                            data.personalLimit = amount;
                            player.sendMessage(ChatColor.GREEN + "个人限购次数已设置为: " + (amount == 0 ? "无限制" : amount));
                        }
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED + "输入无效，必须是数字！");
                    }
                }

                openTradeEditor(player, data.shopName, data);
                pendingEditData.remove(uuid);
            });
            return;
        }
    }

    // ================= 辅助逻辑 =================
    private void handlePurchase(Player player, String shopName, int slot, ItemStack clickedItem) {
        int index = getTradeIndexBySlot(slot);
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null || index == -1 || index >= shop.trades.size()) return;

        ShopManager.Trade trade = shop.trades.get(index);
        if (trade.result == null) return;

        UUID playerId = player.getUniqueId();
        if (!ShopManager.canPurchase(playerId, trade)) {
            player.sendMessage(ChatColor.RED + "已达购买上限！");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        for (ItemStack cost : trade.costItems) {
            if (cost == null || cost.getType() == Material.AIR) continue;
            if (!player.getInventory().containsAtLeast(cost, cost.getAmount())) {
                String costName = ItemStackHelper.getDisplayName(cost);
                player.sendMessage(ChatColor.RED + "缺少物品: " + costName + " x" + cost.getAmount());
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        }

        for (ItemStack cost : trade.costItems) {
            if (cost == null || cost.getType() == Material.AIR) continue;
            ItemStack costToRemove = cost.clone();
            HashMap<Integer, ItemStack> notRemoved = player.getInventory().removeItem(costToRemove);
            if (!notRemoved.isEmpty()) {
                for (ItemStack leftOver : notRemoved.values()) {
                    removeItems(player.getInventory(), leftOver);
                }
            }
        }

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(trade.result.clone());
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(ChatColor.YELLOW + "背包已满，部分物品掉落在地上！");
        }

        ShopManager.recordPurchase(playerId, shop, trade);

        player.sendMessage(ChatColor.GREEN + "兑换成功！");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        openShopTrades(player, shopName, shopTradesPage.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(shopName, 0));
    }

    private void removeItems(org.bukkit.inventory.Inventory inv, ItemStack item) {
        int amountToRemove = item.getAmount();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.isSimilar(item)) {
                if (is.getAmount() > amountToRemove) {
                    is.setAmount(is.getAmount() - amountToRemove);
                    inv.setItem(i, is);
                    return;
                } else {
                    amountToRemove -= is.getAmount();
                    inv.setItem(i, null);
                    if (amountToRemove <= 0) return;
                }
            }
        }
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
            String numStr = name.replace(ChatColor.GOLD + "全服限购: ", "").replaceAll("[^0-9]", "");
            try {
                if (!name.contains("无限制")) data.globalLimit = Integer.parseInt(numStr);
            } catch (Exception ignored) {}
        }

        ItemStack pItem = inv.getItem(41);
        if (pItem != null && pItem.hasItemMeta()) {
            String name = pItem.getItemMeta().getDisplayName();
            String numStr = name.replace(ChatColor.GOLD + "个人限购: ", "").replaceAll("[^0-9]", "");
            try {
                if (!name.contains("无限制")) data.personalLimit = Integer.parseInt(numStr);
            } catch (Exception ignored) {}
        }
        return data;
    }

    public static class ShopHolder implements InventoryHolder {
        @NotNull
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
