package audit;

import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager.BlackMarketTrade;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/** Disposable native-server audit. Never include this plugin in a release. */
public final class BlackMarketOfferProbe extends JavaPlugin {
    private int checked, failures, exactItemFailures;
    private final List<String> details = new ArrayList<>();
    private Class<?> storeClass, cycleClass, stateClass;
    private Constructor<?> storeConstructor, stateConstructor;
    private Method loadCycle, loadPlayer, savePlayer;

    @Override public void onEnable() {
        if (!Boolean.getBoolean("blackmarket.offer.audit") || !Bukkit.getOnlinePlayers().isEmpty()) {
            throw new IllegalStateException("Explicit disposable audit authorization and no players required");
        }
        Bukkit.getScheduler().runTaskLater(this, this::runAudit, 40L);
    }

    private void runAudit() {
        try {
            if (!BlackMarketManager.isAvailable()) throw new IllegalStateException("Fresh market is not enabled");
            ClassLoader loader = BlackMarketManager.class.getClassLoader();
            storeClass = Class.forName("io.Yomicer.magicExpansion.utils.shop.BlackMarketFileStore", true, loader);
            cycleClass = Class.forName(storeClass.getName() + "$Cycle", true, loader);
            stateClass = Class.forName(storeClass.getName() + "$PlayerState", true, loader);
            storeConstructor = storeClass.getDeclaredConstructor(Path.class); storeConstructor.setAccessible(true);
            stateConstructor = stateClass.getDeclaredConstructor(List.class, Set.class, Set.class); stateConstructor.setAccessible(true);
            loadCycle = method("loadOrCreate", long.class);
            loadPlayer = method("loadPlayer", UUID.class, cycleClass);
            savePlayer = method("savePlayer", UUID.class, cycleClass, stateClass);
            Files.createDirectories(getDataFolder().toPath());
            List<ItemStack> simple = BlackMarketManager.getSimplePool();
            List<ItemStack> hard = new ArrayList<>(BlackMarketManager.getHardPool().keySet());
            Field costsField = BlackMarketManager.class.getDeclaredField("costItemPool"); costsField.setAccessible(true);
            @SuppressWarnings("unchecked") List<ItemStack> costs = (List<ItemStack>) costsField.get(null);
            if (simple.size() < 6 || hard.size() < 100 || costs.isEmpty()) throw new IllegalStateException("Incomplete real item pools");
            audit("vanilla-control", offers(new ItemStack(Material.DIAMOND, 2), false));
            if (failures != 0) throw new IllegalStateException("Vanilla control failed: " + details);
            int n = 0;
            for (ItemStack item : simple) audit("simple-" + n++ + "-" + describe(item), offers(item, false));
            n = 0;
            for (ItemStack item : hard) audit("hard-" + n++ + "-" + describe(item), offers(item, false));
            n = 0;
            for (ItemStack item : costs) audit("cost-" + n++ + "-" + describe(item), offers(item, true));
            Method generator = BlackMarketManager.class.getDeclaredMethod("generateTradesForPlayer"); generator.setAccessible(true);
            for (int i = 0; i < 100; i++) {
                @SuppressWarnings("unchecked") List<BlackMarketTrade> generated = (List<BlackMarketTrade>) generator.invoke(null);
                audit("generated-" + i, generated);
            }
            int blocked = 0;
            for (int i = 0; i < 20; i++) {
                UUID owner = UUID.nameUUIDFromBytes(("synthetic-offer-audit-" + i).getBytes(StandardCharsets.UTF_8));
                Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class}, (p, m, a) -> {
                    if (m.getName().equals("getUniqueId")) return owner;
                    if (m.getName().equals("toString")) return "SyntheticOfferAudit";
                    throw new UnsupportedOperationException("Unexpected player call " + m.getName());
                });
                if (BlackMarketManager.getTodayTrades(player).size() != 10) blocked++;
            }
            List<String> result = new ArrayList<>();
            result.add("AUDIT_COMPLETED");
            result.add("server=" + Bukkit.getVersion());
            result.add("plugin=" + Bukkit.getPluginManager().getPlugin("magicexpansion").getDescription().getVersion());
            result.add("simple=" + simple.size() + " hard=" + hard.size() + " costs=" + costs.size());
            result.add("checked=" + checked + " failures=" + failures + " exactItemFailures=" + exactItemFailures + " blockedSyntheticPlayers=" + blocked);
            result.addAll(details);
            if (failures == 0 && blocked == 0) result.add("ALL_NATIVE_OFFERS_PASSED");
            Files.write(Path.of("offer-audit-result.txt"), result);
            result.forEach(getLogger()::info);
        } catch (Throwable error) {
            error.printStackTrace();
            try { Files.writeString(Path.of("offer-audit-fatal.txt"), error.toString()); } catch (Exception ignored) { }
        } finally {
            Bukkit.shutdown();
        }
    }

    private Method method(String name, Class<?>... arguments) throws Exception {
        Method m = storeClass.getDeclaredMethod(name, arguments); m.setAccessible(true); return m;
    }

    private static List<BlackMarketTrade> offers(ItemStack item, boolean asCost) {
        List<BlackMarketTrade> trades = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BlackMarketTrade t = new BlackMarketTrade();
            t.result = i == 0 && !asCost ? item : new ItemStack(Material.DIAMOND, i + 1);
            t.costs = i == 0 && asCost ? List.of(item) : new ArrayList<>();
            t.isFree = !(i == 0 && asCost); t.isHard = false;
            trades.add(t);
        }
        return trades;
    }

    private void audit(String label, List<BlackMarketTrade> offers) {
        checked++;
        try {
            List<ItemStack> originalResults = offers.stream().map(t -> new ItemStack(t.result)).toList();
            List<List<ItemStack>> originalCosts = offers.stream().map(t -> costs(t).stream().map(ItemStack::new).toList()).toList();
            Path directory = getDataFolder().toPath().resolve("case-" + checked);
            Object store = storeConstructor.newInstance(directory);
            long now = System.currentTimeMillis();
            Object cycle = loadCycle.invoke(store, now);
            UUID player = UUID.nameUUIDFromBytes(label.getBytes(StandardCharsets.UTF_8));
            loadPlayer.invoke(store, player, cycle);
            Object state = stateConstructor.newInstance(offers, Set.of(3), Set.of(3, 5));
            savePlayer.invoke(store, player, cycle, state);
            Object restarted = storeConstructor.newInstance(directory);
            Object sameCycle = loadCycle.invoke(restarted, now + 1);
            Object restored = loadPlayer.invoke(restarted, player, sameCycle);
            if (restored == null) throw new AssertionError("Saved state disappeared");
            Method getTrades = stateClass.getDeclaredMethod("trades"); getTrades.setAccessible(true);
            @SuppressWarnings("unchecked") List<BlackMarketTrade> read = (List<BlackMarketTrade>) getTrades.invoke(restored);
            for (int i = 0; i < 10; i++) {
                if (!offers.get(i).result.equals(read.get(i).result) || !costs(offers.get(i)).equals(costs(read.get(i)))
                        || offers.get(i).isFree != read.get(i).isFree || offers.get(i).isHard != read.get(i).isHard) {
                    throw new AssertionError("Item data/quantity/terms changed in offer " + i);
                }
                if (!originalResults.get(i).equals(offers.get(i).result) || !originalCosts.get(i).equals(costs(offers.get(i)))) {
                    throw new AssertionError("The caller's offer template changed in offer " + i);
                }
            }
            for (String field : List.of("purchased", "revealed")) {
                Method accessor = stateClass.getDeclaredMethod(field); accessor.setAccessible(true);
                if (!accessor.invoke(state).equals(accessor.invoke(restored))) throw new AssertionError("Lost " + field);
            }
        } catch (Throwable failure) {
            while (failure instanceof InvocationTargetException && failure.getCause() != null) failure = failure.getCause();
            failures++;
            if (String.valueOf(failure.getMessage()).contains("Invalid or unreadable Black Market item")) exactItemFailures++;
            details.add("FAIL " + label + ": " + failure);
            if (failures <= 3) failure.printStackTrace();
        }
    }

    private static List<ItemStack> costs(BlackMarketTrade trade) {
        return trade.costs == null ? List.of() : trade.costs;
    }

    private static String describe(ItemStack item) {
        return item == null ? "null" : item.getClass().getSimpleName() + ":" + item.getType() + ":" + item.getAmount() + ":" + item;
    }
}
