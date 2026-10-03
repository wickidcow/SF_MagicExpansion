package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import io.Yomicer.magicExpansion.utils.shop.ShopManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Disposable real-Paper persistence/event probe; player and view are explicit no-network facades. */
public final class ShopSafetyProbe extends JavaPlugin {
    private static final String BROKEN = "trades: [unterminated\n";
    private static final UUID OWNER = UUID.fromString("f55799ac-2dd7-42f0-91a2-123456abcdef");
    private final List<String> results = new ArrayList<>();
    private Path directory;

    @Override public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> guarded(() -> {
            directory = MagicExpansion.getInstance().getDataFolder().toPath().resolve("portable_shops");
            if (Boolean.getBoolean("shop.safety.original")) {
                ShopManager.Shop loaded = ShopManager.getShop("Blocked");
                require(loaded != null && loaded.trades.isEmpty(), "Original loader did not create its unsafe empty record");
                require(Files.readString(directory.resolve("Blocked.yml")).equals(BROKEN), "Original bytes changed before save");
                ShopManager.saveAll();
                require(!Files.readString(directory.resolve("Blocked.yml")).equals(BROKEN), "Original overwrite control was not reproduced");
                finish("SHOP_FILE_ORIGINAL_OVERWRITE_REPRODUCED", "original");
                return;
            }
            require(ShopManager.getShop("Blocked") == null, "Broken shop remained purchasable");
            ShopManager.saveAll(); ShopManager.deleteShop("Blocked"); ShopManager.createShop("Blocked");
            require(Files.readString(directory.resolve("Blocked.yml")).equals(BROKEN), "Corrupt file was overwritten or deleted");
            results.add("corrupt_source:retained_unavailable");
            if (Integer.getInteger("shop.safety.phase", 1) == 2) {
                verifyDurable();
                finish("SHOP_SAFETY_SECOND_BOOT_PASS", "second");
                return;
            }
            Upstream13Checks.run();
            results.add("upstream13:cargo_identity_capacity_sword_guard_shield_cancelled_attack");
            FishingCompatibilityChecks.run();
            results.add("fishing:both_rod_families_full_compatibility_cancelled_pending_catch_bait_xp");
            richRoundTrip();
            externalConflict();
            editorEvents();
            asyncCreation();
        }), 40L);
    }

    private void richRoundTrip() throws Exception {
        ShopManager.createShop("Rich");
        ShopManager.Shop shop = requireShop("Rich");
        ShopManager.Trade trade = new ShopManager.Trade();
        trade.result = rich(37); trade.costItems = List.of(rich(7), rich(9));
        trade.globalLimit = 43; trade.personalLimit = 25; trade.globalUsed = 19; trade.personalUsed.put(OWNER, 17);
        shop.trades.add(trade); ShopManager.saveShop(shop);
        require(ShopManager.getShop("Rich") != null, "Valid nested item serialization was rejected");
        ItemStack expected = trade.result.clone();
        ShopManager.reload();
        ShopManager.Trade restored = requireShop("Rich").trades.getFirst();
        require(expected.equals(restored.result), "Real Paper nested item data changed on reload");
        require(restored.costItems.equals(List.of(rich(7), rich(9))), "Exact costs changed");
        verifyRich(restored);
        results.add("nested_item_yaml:exact_real_paper_roundtrip");
    }

    private void externalConflict() throws Exception {
        ShopManager.createShop("External");
        ShopManager.Shop stale = requireShop("External");
        String external = "trades: []\nowner-extension: external-edit\n";
        Files.writeString(directory.resolve("External.yml"), external);
        ShopManager.saveShop(stale);
        require(ShopManager.getShop("External") == null, "Conflicting shop was not locked");
        ShopManager.deleteShop("External");
        require(Files.readString(directory.resolve("External.yml")).equals(external), "External edit overwritten");
        ShopManager.reload();
        ShopManager.Shop fresh = requireShop("External");
        require(fresh != stale, "Reload reused a stale shop identity");
        ShopManager.saveShop(stale);
        require(Files.readString(directory.resolve("External.yml")).equals(external), "Retired object overwrote fresh file");
        require(requireShop("External") == fresh, "Stale refusal disabled unrelated fresh identity");
        results.add("external_changes_and_retired_handles:retained");
    }

    private void editorEvents() throws Exception {
        ShopManager.createShop("Editor");
        ShopManager.Shop shop = requireShop("Editor");
        for (int index = 0; index < 60; index++) {
            ShopManager.Trade trade = new ShopManager.Trade();
            trade.result = new ItemStack(Material.NETHER_STAR);
            trade.costItems = List.of(new ItemStack(Material.DIAMOND, 1));
            trade.globalLimit = 100; trade.personalLimit = 50; trade.globalUsed = index;
            shop.trades.add(trade);
        }
        ShopManager.saveShop(shop);
        Fixture player = new Fixture("Editor");
        ShopGUI.openAdminTradesMenu(player.player, "Editor", 1);
        player.click(10, ClickType.LEFT);
        ShopManager.Trade selected = shop.trades.get(28);
        selected.globalUsed = 37; selected.personalUsed.put(OWNER, 11);
        player.top.setItem(28, rich(3));
        player.click(53, ClickType.LEFT);
        require(shop.trades.size() == 60 && shop.trades.get(28) == selected, "Editor replaced selected trade identity");
        require(selected.costItems.equals(List.of(rich(3))), "Second-page editor changed the wrong price");
        require(shop.trades.getFirst().costItems.getFirst().getAmount() == 1, "Same-reward first trade overwritten");
        require(selected.globalUsed == 37 && selected.personalUsed.get(OWNER) == 11, "Concurrent purchase usage reset by editor");
        results.add("real_editor_second_page_and_usage:preserved");

        // The same displayed reward must not make a new trade overwrite an existing definition.
        player.click(49, ClickType.LEFT);
        player.top.setItem(13, new ItemStack(Material.NETHER_STAR));
        player.top.setItem(28, new ItemStack(Material.EMERALD, 2));
        player.click(53, ClickType.LEFT);
        require(shop.trades.size() == 61 && shop.trades.getFirst().costItems.getFirst().getType() == Material.DIAMOND,
                "New same-reward trade overwrote an existing definition");
        results.add("new_same_reward_trade:appended");

        // Delete the selected new trade only, not every trade with its reward.
        ShopGUI.openAdminTradesMenu(player.player, "Editor", 2);
        player.click(14, ClickType.RIGHT);
        require(shop.trades.size() == 60 && shop.trades.contains(selected), "Delete removed unrelated same-reward trades");
        results.add("selected_trade_delete:identity_only");

        // Replacing the loaded shop makes an open editor stale. It cannot overwrite the new snapshot.
        ShopGUI.openAdminTradesMenu(player.player, "Editor", 0);
        player.click(10, ClickType.LEFT);
        player.top.setItem(13, new ItemStack(Material.DIAMOND_BLOCK));
        ShopManager.reload();
        byte[] before = Files.readAllBytes(directory.resolve("Editor.yml"));
        player.click(53, ClickType.LEFT);
        require(Arrays.equals(before, Files.readAllBytes(directory.resolve("Editor.yml"))), "Stale editor wrote after reload");
        require(requireShop("Editor").trades.getFirst().result.getType() == Material.NETHER_STAR, "Stale editor replaced reward");
        player.close();
        results.add("stale_editor_after_reload:no_write");
    }

    private void asyncCreation() throws Exception {
        Fixture player = new Fixture("Editor");
        ShopGUI.openAdminMainMenu(player.player);
        player.click(49, ClickType.LEFT);
        player.chat("ChatOne", "ChatTwo");
        require(ShopManager.getShop("ChatOne") == null && ShopManager.getShop("ChatTwo") == null,
                "Async chat mutated live shop state before the server task");
        Bukkit.getScheduler().runTaskLater(this, () -> guarded(() -> {
            require(requireShop("ChatOne") != null && ShopManager.getShop("ChatTwo") == null,
                    "Queued duplicate chat bypassed its creation token");
            require(player.offThreadEffects == 0, "Chat produced UI effects off the server thread");
            results.add("async_creation:single_main_thread_commit");
            asyncLimit(player);
        }), 2L);
    }

    private void asyncLimit(Fixture player) throws Exception {
        ShopManager.Shop shop = requireShop("Editor");
        ShopGUI.openAdminTradesMenu(player.player, "Editor", 1);
        player.click(10, ClickType.LEFT);
        player.click(40, ClickType.LEFT);
        player.chat("48", "49");
        require(shop.trades.get(28).globalLimit == 100, "Async chat changed persisted definition early");
        Bukkit.getScheduler().runTaskLater(this, () -> guarded(() -> {
            player.click(53, ClickType.LEFT);
            ShopManager.Trade selected = requireShop("Editor").trades.get(28);
            require(selected.globalLimit == 48 && selected.globalUsed == 37 && selected.personalUsed.get(OWNER) == 11,
                    "Limit chat lost its selection, usage, or single-consumption token");
            require(player.offThreadEffects == 0, "Limit editing touched UI off-thread");
            results.add("async_limit:single_main_thread_edit_preserves_usage");
            verifyDurable();
            finish("SHOP_SAFETY_FIRST_BOOT_PASS", "first");
        }), 2L);
    }

    private void verifyDurable() throws Exception {
        verifyRich(requireShop("Rich").trades.getFirst());
        ShopManager.Shop editor = requireShop("Editor");
        require(editor.trades.size() == 60, "Durable trade count changed");
        ShopManager.Trade selected = editor.trades.get(28);
        require(selected.globalLimit == 48 && selected.globalUsed == 37 && selected.personalUsed.get(OWNER) == 11,
                "Durable limits or owner usage changed");
        require(selected.costItems.equals(List.of(rich(3))), "Durable editor item data changed");
        require(requireShop("ChatOne") != null && ShopManager.getShop("ChatTwo") == null, "Durable chat creation changed");
        require(Files.readString(directory.resolve("Blocked.yml")).equals(BROKEN), "Broken bytes changed on restart");
        require(Files.readString(directory.resolve("External.yml")).contains("owner-extension: external-edit"), "Unknown root field erased");
        results.add("persisted_shop_items_identities_and_usage:exact");
    }

    private static void verifyRich(ShopManager.Trade trade) {
        require(trade.result.equals(rich(37)), "Reward item changed");
        require(trade.globalUsed == 19 && trade.globalLimit == 43 && trade.personalLimit == 25
                && trade.personalUsed.get(OWNER) == 17, "Rich shop quotas or owners changed");
        var data = trade.result.getItemMeta().getPersistentDataContainer();
        require(data.get(key("old:count"), PersistentDataType.LONG) == 9_007_199_254_740_993L, "Long storage value changed");
        require(Float.floatToRawIntBits(data.get(key("slimefun:item_charge"), PersistentDataType.FLOAT))
                == Float.floatToRawIntBits(123.4567F), "Charge bits changed");
        require("unchanged".equals(data.get(key("old:nested"), PersistentDataType.TAG_CONTAINER)
                .get(key("old:nested-owner"), PersistentDataType.STRING)), "Nested owner data lost");
    }

    private void guarded(Checked action) {
        try { action.run(); }
        catch (Throwable error) {
            getLogger().log(java.util.logging.Level.SEVERE, "SHOP_SAFETY_RUNTIME_FAIL", error);
            Bukkit.shutdown();
        }
    }

    private void finish(String marker, String phase) throws Exception {
        Files.writeString(Path.of("safety-result-" + phase + ".txt"), marker + "\n" + String.join("\n", results) + "\n");
        getLogger().info(marker + " cases=" + results.size());
        Bukkit.shutdown();
    }

    private static ShopManager.Shop requireShop(String name) {
        ShopManager.Shop shop = ShopManager.getShop(name);
        require(shop != null, "Missing usable shop: " + name);
        return shop;
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static NamespacedKey key(String value) { return java.util.Objects.requireNonNull(NamespacedKey.fromString(value)); }
    private static ItemStack rich(int amount) {
        ItemStack item = new ItemStack(Material.DIAMOND, amount);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Owner's original item")); meta.lore(List.of(Component.text("Existing lore; preserve exactly")));
        var data = meta.getPersistentDataContainer();
        data.set(key("slimefun:slimefun_item"), PersistentDataType.STRING, "UNREGISTERED_OLD_ID");
        data.set(key("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);
        data.set(key("old:count"), PersistentDataType.LONG, 9_007_199_254_740_993L);
        data.set(key("old:bytes"), PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 4});
        var nested = data.getAdapterContext().newPersistentDataContainer();
        nested.set(key("old:nested-owner"), PersistentDataType.STRING, "unchanged");
        data.set(key("old:nested"), PersistentDataType.TAG_CONTAINER, nested);
        item.setItemMeta(meta); return item;
    }

    @FunctionalInterface private interface Checked { void run() throws Exception; }

    private static final class Fixture {
        final UUID uuid = UUID.randomUUID();
        final ShopGUI listener = new ShopGUI();
        final Inventory storage = Bukkit.createInventory(null, 36);
        final String shop;
        Inventory top;
        InventoryView view;
        int offThreadEffects;
        final PlayerInventory inventory = (PlayerInventory) Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(),
                new Class<?>[] {PlayerInventory.class}, (proxy, method, args) -> {
                    try { return storage.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(storage, args); }
                    catch (InvocationTargetException error) { throw error.getCause(); }
                });
        final Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "isOnline" -> true;
                    case "getInventory" -> inventory;
                    case "getOpenInventory" -> view;
                    case "getWorld" -> Bukkit.getWorlds().getFirst();
                    case "getLocation" -> Bukkit.getWorlds().getFirst().getSpawnLocation();
                    case "getName" -> "SafetyAudit";
                    case "getServer" -> Bukkit.getServer();
                    case "openInventory" -> { recordThread(); open((Inventory) args[0]); yield view; }
                    case "closeInventory" -> { recordThread(); close(); yield null; }
                    case "sendMessage", "playSound" -> { recordThread(); yield null; }
                    case "hashCode" -> uuid.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "SafetyAudit:" + uuid;
                    default -> throw new UnsupportedOperationException("Unexpected fixture player call: " + method);
                });

        Fixture(String shop) { this.shop = shop; }
        void recordThread() { if (!Bukkit.isPrimaryThread()) offThreadEffects++; }
        void close() { if (view != null) listener.onInventoryClose(new InventoryCloseEvent(view)); top = null; view = null; }
        void open(Inventory next) {
            close(); top = next;
            String label = ChatColor.stripColor(next.getItem(4).getItemMeta().getDisplayName());
            String title = label.startsWith("Manage: ") ? ChatColor.DARK_RED + label
                    : label.equals("Trade Editor") ? ChatColor.DARK_RED + "Configure: " + shop
                    : ChatColor.DARK_RED + "Magic Market Administration";
            Inventory captured = next;
            view = (InventoryView) Proxy.newProxyInstance(InventoryView.class.getClassLoader(), new Class<?>[] {InventoryView.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getTopInventory" -> captured;
                        case "getBottomInventory" -> inventory;
                        case "getPlayer" -> player;
                        case "getTitle", "getOriginalTitle" -> title;
                        case "title" -> Component.text(title);
                        case "getType" -> InventoryType.CHEST;
                        case "getInventory" -> (int) args[0] < 0 ? null : (int) args[0] < captured.getSize() ? captured : inventory;
                        case "getItem" -> (int) args[0] < captured.getSize() ? captured.getItem((int) args[0]) : storage.getItem((int) args[0] - captured.getSize());
                        case "convertSlot" -> (int) args[0] < captured.getSize() ? args[0] : (int) args[0] - captured.getSize();
                        case "countSlots" -> captured.getSize() + 36;
                        case "getSlotType" -> InventoryType.SlotType.CONTAINER;
                        case "getCursor" -> new ItemStack(Material.AIR);
                        default -> throw new UnsupportedOperationException("Unexpected fixture view call: " + method);
                    });
        }
        void click(int slot, ClickType click) {
            require(view != null, "No open view for click " + slot);
            listener.onInventoryClick(new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, click, InventoryAction.NOTHING));
        }
        void chat(String... messages) {
            CompletableFuture.runAsync(() -> {
                for (String message : messages) {
                    AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, message, new HashSet<>());
                    listener.onChat(event);
                    require(event.isCancelled(), "Pending chat was not cancelled synchronously");
                }
            }).join();
        }
    }
}
