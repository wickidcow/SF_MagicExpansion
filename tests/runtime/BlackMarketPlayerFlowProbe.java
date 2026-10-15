package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager.BlackMarketTrade;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/** Real server/shop/storage code; only client identity and inventory handoff are synthetic. */
public final class BlackMarketPlayerFlowProbe extends JavaPlugin {
    private Path market, evidence;
    private Method click;
    private int[] slots;
    private final ShopGUI gui = new ShopGUI();
    private int reveals, paid, free, duplicates, paymentFailures, refundChecks;
    private String stage = "initialization", phase;
    private final Deque<Step> pending = new ArrayDeque<>();
    private BukkitTask task;
    private long maximumScenarioNanos;
    @FunctionalInterface private interface Step { void run() throws Exception; }

    @Override public void onEnable() {
        if (!Boolean.getBoolean("blackmarket.offer.audit") || !Bukkit.getOnlinePlayers().isEmpty()) {
            throw new IllegalStateException("Disposable audit authorization and no online players required");
        }
        Bukkit.getScheduler().runTaskLater(this, this::prepare, 40L);
    }

    private void prepare() {
        phase = System.getProperty("blackmarket.flow.phase", "invalid");
        try {
            check(Set.of("buy", "restart").contains(phase), "Explicit phase required");
            check(BlackMarketManager.isAvailable(), "Market did not initialize");
            market = MagicExpansion.getInstance().getDataFolder().toPath().resolve("black_market");
            evidence = getDataFolder().toPath();
            Files.createDirectories(evidence);
            click = ShopGUI.class.getDeclaredMethod("handleBlackMarketClick", Player.class, int.class);
            click.setAccessible(true);
            Field slotField = ShopGUI.class.getDeclaredField("BLACK_MARKET_SLOTS");
            slotField.setAccessible(true);
            slots = ((int[]) slotField.get(null)).clone();
            if (phase.equals("buy")) {
                check(!Files.exists(evidence.resolve("cycle-original.yml")), "Refuse reused seed fixture");
                Files.copy(market.resolve("cycle.yml"), evidence.resolve("cycle-original.yml"));
                pending.add(() -> seedDeterministicPlayer("deterministic", false));
                for (int p = 0; p < 20; p++) enqueuePurchases("generated-player-" + p);
                enqueuePurchases("deterministic");
                pending.add(this::verifyRefundOnRefusedReceipt);
            } else {
                check(Files.exists(Path.of("flow-buy-result.txt")), "First boot evidence is missing");
                for (int p = 0; p < 20; p++) enqueueRestart("generated-player-" + p);
                enqueueRestart("deterministic");
            }
            pending.add(this::complete);
            // One scenario per two ticks. Do not put hundreds of synchronous disk
            // transactions and assertion reads into a single artificial mega-tick.
            task = Bukkit.getScheduler().runTaskTimer(this, () -> {
                try {
                    check(!pending.isEmpty(), "Unexpected empty audit queue");
                    long start = System.nanoTime();
                    pending.removeFirst().run();
                    maximumScenarioNanos = Math.max(maximumScenarioNanos, System.nanoTime() - start);
                } catch (Throwable error) { fail(error); }
            }, 1L, 2L);
        } catch (Throwable error) { fail(error); }
    }

    private void complete() throws Exception {
        if (phase.equals("buy")) {
            check(reveals == 210 && paid + free == 210 && duplicates == 210 && paid > 0 && free > 0
                    && paymentFailures == paid && refundChecks == 1, "Missing required purchase paths");
        } else check(duplicates == 210, "Missing post-restart duplicate checks");
        check(Arrays.equals(Files.readAllBytes(evidence.resolve("cycle-original.yml")),
                Files.readAllBytes(market.resolve("cycle.yml"))), "Tests must not reset the cycle");
        String report = "PLAYER_FLOW_PASSED\nphase=" + phase + "\npid=" + ProcessHandle.current().pid()
                + "\nserver=" + Bukkit.getVersion() + "\nreveals=" + reveals + " paid=" + paid + " free=" + free
                + " duplicateRejections=" + duplicates + " insufficientPayment=" + paymentFailures
                + " refusedReceiptRefunds=" + refundChecks + "\nmaximumScenarioMs=" + maximumScenarioNanos / 1_000_000.0 + "\n";
        Files.writeString(Path.of("flow-" + phase + "-result.txt"), report);
        getLogger().info(report);
        task.cancel();
        Bukkit.shutdown();
    }

    private void fail(Throwable error) {
        if (task != null) task.cancel();
        while (error instanceof InvocationTargetException && error.getCause() != null) error = error.getCause();
        error.printStackTrace();
        try { Files.writeString(Path.of("flow-" + phase + "-fatal.txt"), stage + ": " + error); } catch (Exception ignored) { }
        Bukkit.shutdown();
    }

    private void enqueuePurchases(String name) {
        Client client = new Client(name);
        pending.add(() -> {
            stage = name + "/open";
            client.offers = BlackMarketManager.getTodayTrades(client.player);
            check(client.offers.size() == 10, "Player did not receive ten offers: " + name);
            ShopGUI.openBlackMarket(client.player);
            check(client.menu != null && client.menu.getHolder() instanceof ShopGUI.ShopHolder, "Market GUI did not open");
        });
        for (int index = 0; index < 10; index++) {
            int slot = index;
            pending.add(() -> buyOffer(client, name, slot));
        }
        pending.add(() -> {
            check(readLedger(client.id).getIntegerList("purchased").size() == 10, "Not all slots purchased");
            Files.copy(ledger(client.id), evidence.resolve(client.id + ".yml"));
        });
    }

    private void buyOffer(Client client, String name, int index) throws Exception {
        stage = name + "/" + index;
        BlackMarketTrade offer = client.offers.get(index);
        ItemStack expectedReward = nativeStack(offer.result);
        client.storage.clear();
        invokeClick(client, index);
        check(BlackMarketManager.isRevealed(client.id, index), "Reveal not recorded");
        check(!BlackMarketManager.hasPurchased(client.id, index) && empty(client.storage), "Reveal issued or charged items");
        check(readLedger(client.id).getIntegerList("revealed").contains(index), "Reveal not persisted");
        reveals++;
        if (!offer.isFree) {
            byte[] before = Files.readAllBytes(ledger(client.id));
            invokeClick(client, index);
            check(empty(client.storage) && !BlackMarketManager.hasPurchased(client.id, index), "Unpaid offer succeeded");
            check(Arrays.equals(before, Files.readAllBytes(ledger(client.id))), "Failed payment changed receipt");
            for (ItemStack cost : offer.costs) check(client.storage.addItem(nativeStack(cost)).isEmpty(), "Payment fixture overflow");
            paymentFailures++;
        }
        client.requireReceipt = index;
        invokeClick(client, index);
        client.requireReceipt = -1;
        check(BlackMarketManager.hasPurchased(client.id, index), "Purchase was not recorded: " + name + "/" + index);
        check(readLedger(client.id).getIntegerList("purchased").contains(index), "Purchase not durable");
        assertContents(client.storage, List.of(expectedReward));
        check(expectedReward.equals(offer.result), "GUI or payment mutated the original offer");
        if (offer.isFree) free++; else paid++;
        byte[] receipt = Files.readAllBytes(ledger(client.id));
        invokeClick(client, index);
        assertContents(client.storage, List.of(expectedReward));
        check(Arrays.equals(receipt, Files.readAllBytes(ledger(client.id))), "Duplicate click changed receipt");
        duplicates++;
    }

    private void enqueueRestart(String name) {
        Client client = new Client(name);
        pending.add(() -> {
            stage = "restart/" + name;
            client.original = Files.readAllBytes(evidence.resolve(client.id + ".yml"));
            check(Arrays.equals(client.original, Files.readAllBytes(ledger(client.id))), "Saved ledger changed between boots");
            client.offers = BlackMarketManager.getTodayTrades(client.player);
            check(client.offers.size() == 10, "Offers did not load after restart");
            ShopGUI.openBlackMarket(client.player);
            check(client.menu != null, "Purchased market did not reopen after restart");
        });
        for (int index = 0; index < 10; index++) {
            int slot = index;
            pending.add(() -> {
                check(BlackMarketManager.hasPurchased(client.id, slot) && BlackMarketManager.isRevealed(client.id, slot), "Lost slot history");
                invokeClick(client, slot);
                check(empty(client.storage), "Restart allowed a second reward");
                duplicates++;
            });
        }
        pending.add(() -> check(Arrays.equals(client.original, Files.readAllBytes(ledger(client.id))),
                "Restart/duplicate clicks rerolled or rewrote offers"));
    }

    private void verifyRefundOnRefusedReceipt() throws Exception {
        stage = "refused-receipt";
        seedDeterministicPlayer("refused-receipt", true);
        Client client = new Client("refused-receipt");
        List<BlackMarketTrade> offers = BlackMarketManager.getTodayTrades(client.player);
        check(offers.size() == 10 && !offers.get(0).isFree, "Missing paid failure fixture");
        invokeClick(client, 0);
        List<ItemStack> costs = offers.get(0).costs.stream().map(BlackMarketPlayerFlowProbe::nativeStack).toList();
        for (ItemStack cost : costs) check(client.storage.addItem(nativeStack(cost)).isEmpty(), "Refund fixture overflow");
        // An external writer is simulated only in this generated test folder.
        Files.writeString(ledger(client.id), "\n# external audit writer\n", StandardOpenOption.APPEND);
        byte[] external = Files.readAllBytes(ledger(client.id));
        invokeClick(client, 0);
        check(!BlackMarketManager.hasPurchased(client.id, 0), "Failed receipt authorized reward");
        assertContents(client.storage, costs);
        check(Arrays.equals(external, Files.readAllBytes(ledger(client.id))), "Failed receipt erased external evidence");
        check(!readLedger(client.id).getIntegerList("purchased").contains(0), "Failed save wrote a purchase");
        check(BlackMarketManager.getTodayTrades(client.player).isEmpty(), "Unsafe player was not isolated");
        check(BlackMarketManager.getTodayTrades(new Client("deterministic").player).size() == 10, "Fault locked healthy players");
        refundChecks++;
    }

    private void seedDeterministicPlayer(String name, boolean paidOnly) throws Exception {
        ItemStack template = BlackMarketManager.getHardPool().keySet().stream()
                .filter(i -> i != null && i.getClass().getSimpleName().equals("SlimefunItemStack"))
                .findFirst().orElseThrow();
        ItemStack rich = nativeStack(template);
        var meta = rich.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("audit", "binary"), PersistentDataType.BYTE_ARRAY, new byte[]{1, 3, 5, 7});
        rich.setItemMeta(meta);
        List<BlackMarketTrade> trades = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BlackMarketTrade trade = new BlackMarketTrade();
            trade.result = nativeStack(rich); trade.result.setAmount(2);
            trade.isFree = !paidOnly && i % 2 == 0; trade.isHard = true;
            ItemStack cost = nativeStack(rich); cost.setAmount(3);
            trade.costs = trade.isFree ? List.of() : List.of(cost, nativeStack(cost));
            trades.add(trade);
        }
        Class<?> type = Class.forName("io.Yomicer.magicExpansion.utils.shop.BlackMarketFileStore", true, BlackMarketManager.class.getClassLoader());
        Class<?> cycleType = Class.forName(type.getName() + "$Cycle", true, type.getClassLoader());
        Class<?> stateType = Class.forName(type.getName() + "$PlayerState", true, type.getClassLoader());
        Constructor<?> ctor = type.getDeclaredConstructor(Path.class); ctor.setAccessible(true);
        Object store = ctor.newInstance(market);
        Method load = type.getDeclaredMethod("loadOrCreate", long.class); load.setAccessible(true);
        Object cycle = load.invoke(store, System.currentTimeMillis());
        Method loadPlayer = type.getDeclaredMethod("loadPlayer", UUID.class, cycleType); loadPlayer.setAccessible(true);
        check(loadPlayer.invoke(store, id(name), cycle) == null, "Refuse replacing existing player fixture");
        Constructor<?> state = stateType.getDeclaredConstructor(List.class, Set.class, Set.class); state.setAccessible(true);
        Method save = type.getDeclaredMethod("savePlayer", UUID.class, cycleType, stateType); save.setAccessible(true);
        save.invoke(store, id(name), cycle, state.newInstance(trades, Set.of(), Set.of()));
    }

    // Payments originate in native inventories; preserve full item data for fixtures.
    // Actual shop reward/payment/refund handlers remain unchanged and exercised.
    private static ItemStack nativeStack(ItemStack item) { return ItemStack.deserialize(item.serialize()); }
    private void invokeClick(Client c, int index) throws Exception { click.invoke(gui, c.player, slots[index]); }
    private static UUID id(String name) { return UUID.nameUUIDFromBytes(("blackmarket-native-flow:" + name).getBytes(StandardCharsets.UTF_8)); }
    private Path ledger(UUID id) { return market.resolve("players/" + id + ".yml"); }
    private YamlConfiguration readLedger(UUID id) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration(); yaml.loadFromString(Files.readString(ledger(id)));
        check(id.toString().equals(yaml.getString("player")), "Wrong ledger owner"); return yaml;
    }
    private static boolean empty(Inventory inventory) {
        return Arrays.stream(inventory.getStorageContents()).allMatch(i -> i == null || i.getType().isAir() || i.getAmount() == 0);
    }
    private static void assertContents(Inventory inventory, List<ItemStack> expected) {
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack i : inventory.getStorageContents()) if (i != null && !i.getType().isAir()) remaining.add(nativeStack(i));
        for (ItemStack item : expected) {
            int needed = item.getAmount();
            for (ItemStack stored : remaining) if (needed > 0 && stored.isSimilar(item)) {
                int taken = Math.min(needed, stored.getAmount()); needed -= taken; stored.setAmount(stored.getAmount() - taken);
            }
            check(needed == 0, "Lost item data or quantity: " + item);
        }
        check(remaining.stream().allMatch(i -> i.getAmount() == 0), "Unexpected reward/payment remainder");
    }
    private static void check(boolean pass, String message) { if (!pass) throw new AssertionError(message); }

    private final class Client {
        final UUID id;
        final Inventory storage = Bukkit.createInventory(null, 36);
        final PlayerInventory inventory;
        final Player player;
        Inventory menu;
        List<BlackMarketTrade> offers;
        byte[] original;
        int requireReceipt = -1;
        Client(String name) {
            id = id(name);
            inventory = (PlayerInventory) Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(), new Class<?>[]{PlayerInventory.class}, (p, m, a) -> {
                if (m.getName().equals("addItem") && requireReceipt >= 0) {
                    check(readLedger(id).getIntegerList("purchased").contains(requireReceipt), "Reward arrived before durable receipt");
                }
                return switch (m.getName()) {
                    case "getStorageContents", "getContents" -> storage.getStorageContents();
                    case "setStorageContents", "setContents" -> { storage.setStorageContents((ItemStack[]) a[0]); yield null; }
                    case "addItem" -> storage.addItem((ItemStack[]) a[0]);
                    case "toString" -> "NativeStorageForSyntheticClient";
                    default -> throw new UnsupportedOperationException("Unexpected inventory call: " + m.getName());
                };
            });
            player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class}, (p, m, a) -> switch (m.getName()) {
                case "getUniqueId" -> id;
                case "getInventory" -> inventory;
                case "getLocation" -> Bukkit.getWorlds().get(0).getSpawnLocation();
                case "getWorld" -> Bukkit.getWorlds().get(0);
                case "openInventory" -> { menu = (Inventory) a[0]; yield null; }
                case "playSound", "sendMessage" -> null;
                case "getName", "toString" -> name;
                case "isOnline" -> true;
                case "isOp", "hasPermission" -> false;
                default -> throw new UnsupportedOperationException("Unexpected player call: " + m.getName());
            });
        }
    }
}
