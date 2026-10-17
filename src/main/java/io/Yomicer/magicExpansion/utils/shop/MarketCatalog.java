package io.Yomicer.magicExpansion.utils.shop;

import io.Yomicer.magicExpansion.MagicExpansion;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/** Explicit, append-only catalog installation. It never restocks or rewrites an existing offer. */
public final class MarketCatalog {
    private MarketCatalog() {}

    record ItemSpec(String slimefun, String material, int amount) {}
    record Offer(String id, ItemSpec result, List<ItemSpec> costs, int personalLimit, List<String> plugins) {}
    record Section(String name, List<Offer> offers) {}
    record Definition(List<Section> sections) {}

    interface Resolver {
        boolean pluginEnabled(String name);
        ItemStack resolve(ItemSpec spec);
    }
    interface Store {
        List<ShopManager.Shop> shops();
        boolean available(ShopManager.Shop shop);
        boolean save(ShopManager.Shop shop);
        void register(ShopManager.Shop shop);
    }
    record Result(int added, int existing, int optionalSkipped, int unresolved, int failedShops) {
        String message() {
            return "Market catalog: " + added + " offers added, " + existing + " already installed, "
                    + optionalSkipped + " skipped (addon absent), " + unresolved + " unresolved, "
                    + failedShops + " shop saves refused. Existing purchases were not reset.";
        }
    }

    /** Server-thread admin entry point, shared by the command and sunflower button. */
    public static void populate(CommandSender sender) {
        if (!sender.hasPermission("magicexpansion.shop.admin")) {
            sender.sendMessage("You do not have permission to update the market catalog.");
            return;
        }
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Catalog updates require the server thread");
        MagicExpansion plugin = MagicExpansion.getInstance();
        try {
            Path path = plugin.getDataFolder().toPath().resolve("market_catalog.yml");
            if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) plugin.saveResource("market_catalog.yml", false);
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Catalog is not a regular file");
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length > 2_000_000) throw new IOException("Catalog exceeds the 2 MB limit");
            Definition definition = parse(StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString());
            Result result = install(definition, new CatalogItems(), new Store() {
                public List<ShopManager.Shop> shops() { return ShopManager.getShops(); }
                public boolean available(ShopManager.Shop shop) { return ShopManager.isAvailable(shop); }
                public boolean save(ShopManager.Shop shop) { return ShopManager.trySaveShop(shop); }
                public void register(ShopManager.Shop shop) { ShopManager.getShops().add(shop); }
            }, plugin.getLogger()::warning);
            sender.sendMessage(result.message());
            plugin.getLogger().info(result.message());
        } catch (IOException | InvalidConfigurationException | RuntimeException | LinkageError failure) {
            sender.sendMessage("Market catalog update refused: " + failure.getMessage() + ". Purchase history was not reset.");
            plugin.getLogger().warning("Market catalog update refused: " + failure.getMessage());
        }
    }

    static Definition parse(String text) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(text);
        if (!Set.of("version", "shops").containsAll(yaml.getKeys(false))) throw new IOException("Unknown catalog root field");
        if (integer(yaml.get("version"), 1, 1, "version") != 1) throw new IOException("Unsupported catalog version");
        List<?> sections = list(yaml.get("shops"), "shops");
        if (sections.size() > 100) throw new IOException("Too many catalog shops");
        Set<String> names = new HashSet<>(), ids = new HashSet<>();
        List<Section> result = new ArrayList<>();
        for (Object value : sections) {
            Map<?, ?> section = mapping(value, Set.of("name", "requiresPlugins", "offers"));
            String name = string(section.get("name"), "shop name");
            if (!name.matches("[A-Za-z0-9][A-Za-z0-9 ._'-]{0,63}") || name.endsWith(".") || !names.add(name)) {
                throw new IOException("Invalid or duplicate shop name: " + name);
            }
            List<String> plugins = plugins(section.get("requiresPlugins"));
            List<Offer> offers = new ArrayList<>();
            for (Object row : list(section.get("offers"), "offers")) {
                Map<?, ?> offer = mapping(row, Set.of("id", "result", "cost", "personalLimit", "requiresPlugins"));
                String id = string(offer.get("id"), "offer id");
                if (!id.matches("[a-z0-9][a-z0-9._-]{0,127}") || !ids.add(id)) throw new IOException("Invalid or duplicate offer id: " + id);
                if (ids.size() > 2000) throw new IOException("Too many catalog offers");
                List<ItemSpec> costs = new ArrayList<>();
                for (Object cost : list(offer.get("cost"), "cost")) costs.add(item(cost));
                if (costs.isEmpty() || costs.size() > 7) throw new IOException("Offer " + id + " must have one to seven costs");
                LinkedHashSet<String> required = new LinkedHashSet<>(plugins);
                required.addAll(plugins(offer.get("requiresPlugins")));
                int limit = offer.containsKey("personalLimit") ? integer(offer.get("personalLimit"), 1, 4096, "personalLimit") : 1;
                offers.add(new Offer(id, item(offer.get("result")), List.copyOf(costs), limit, List.copyOf(required)));
            }
            result.add(new Section(name, List.copyOf(offers)));
        }
        return new Definition(List.copyOf(result));
    }

    static Result install(Definition definition, Resolver resolver, Store store, Consumer<String> warnings) throws IOException {
        Map<String, ShopManager.Shop> byName = new HashMap<>();
        Map<String, ShopManager.Shop> imported = new HashMap<>();
        for (ShopManager.Shop shop : store.shops()) {
            if (byName.putIfAbsent(shop.name, shop) != null) throw new IOException("Duplicate live shop name");
            for (String id : shop.catalogHistory) {
                if (imported.putIfAbsent(id, shop) != null) throw new IOException("Duplicate installed catalog identity: " + id);
            }
            Set<String> live = new HashSet<>();
            for (ShopManager.Trade trade : shop.trades) {
                if (trade.catalogId != null && (!live.add(trade.catalogId) || imported.get(trade.catalogId) != shop)) {
                    throw new IOException("Invalid installed catalog history: " + trade.catalogId);
                }
            }
        }
        int added = 0, existing = 0, optional = 0, unresolved = 0, failed = 0;
        for (Section section : definition.sections()) {
            ShopManager.Shop shop = byName.get(section.name());
            if (shop != null && !store.available(shop)) {
                warnings.accept("Catalog retained read-only shop: " + section.name());
                failed++;
                continue;
            }
            List<ShopManager.Trade> pending = new ArrayList<>();
            for (Offer offer : section.offers()) {
                if (imported.containsKey(offer.id())) { existing++; continue; }
                if (offer.plugins().stream().anyMatch(name -> !resolver.pluginEnabled(name))) { optional++; continue; }
                ItemStack reward = resolver.resolve(offer.result());
                List<ItemStack> costs = new ArrayList<>();
                for (ItemSpec spec : offer.costs()) costs.add(resolver.resolve(spec));
                if (!valid(reward) || costs.stream().anyMatch(stack -> !valid(stack))) {
                    unresolved++;
                    warnings.accept("Catalog offer skipped without taking payment: " + offer.id() + " (missing or disabled reward/cost item)");
                    continue;
                }
                ShopManager.Trade trade = new ShopManager.Trade();
                trade.catalogId = offer.id();
                trade.catalogPlugins = new ArrayList<>(offer.plugins());
                trade.result = reward.clone();
                trade.costItems = costs.stream().map(ItemStack::clone).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                trade.personalLimit = offer.personalLimit();
                trade.globalLimit = 0;
                pending.add(trade);
            }
            if (pending.isEmpty()) continue;
            boolean newShop = shop == null;
            if (newShop) { shop = new ShopManager.Shop(); shop.name = section.name(); }
            int oldSize = shop.trades.size();
            Set<String> oldHistory = new LinkedHashSet<>(shop.catalogHistory);
            shop.trades.addAll(pending);
            for (ShopManager.Trade trade : pending) shop.catalogHistory.add(trade.catalogId);
            if (!store.save(shop)) {
                shop.trades.subList(oldSize, shop.trades.size()).clear();
                shop.catalogHistory.clear();
                shop.catalogHistory.addAll(oldHistory);
                warnings.accept("Catalog save refused for " + section.name() + "; previous stock and counters retained");
                failed++;
                continue;
            }
            if (newShop) store.register(shop);
            byName.put(shop.name, shop);
            for (ShopManager.Trade trade : pending) imported.put(trade.catalogId, shop);
            added += pending.size();
        }
        return new Result(added, existing, optional, unresolved, failed);
    }

    private static boolean valid(ItemStack item) {
        return item != null && !item.getType().isAir() && item.getAmount() > 0 && item.getAmount() <= item.getMaxStackSize();
    }
    private static ItemSpec item(Object value) throws IOException {
        Map<?, ?> map = mapping(value, Set.of("slimefun", "material", "amount"));
        if (map.containsKey("slimefun") == map.containsKey("material")) throw new IOException("An item needs exactly one slimefun ID or vanilla material");
        String sf = map.containsKey("slimefun") ? string(map.get("slimefun"), "slimefun ID") : null;
        String material = map.containsKey("material") ? string(map.get("material"), "material") : null;
        String id = sf == null ? material : sf;
        if (!id.matches("[A-Z0-9_]{1,128}")) throw new IOException("Item IDs must be exact uppercase registry identifiers: " + id);
        int amount = map.containsKey("amount") ? integer(map.get("amount"), 1, 64, "amount") : 1;
        return new ItemSpec(sf, material, amount);
    }
    private static Map<?, ?> mapping(Object value, Set<String> fields) throws IOException {
        if (!(value instanceof Map<?, ?> map) || !fields.containsAll(map.keySet())) throw new IOException("Invalid mapping or unknown catalog field");
        return map;
    }
    private static List<?> list(Object value, String name) throws IOException {
        if (!(value instanceof List<?> list)) throw new IOException(name + " must be an explicit list");
        return list;
    }
    private static String string(Object value, String name) throws IOException {
        if (!(value instanceof String text) || text.isBlank() || !text.equals(text.strip())) throw new IOException("Invalid " + name);
        return text;
    }
    private static List<String> plugins(Object value) throws IOException {
        if (value == null) return List.of();
        List<String> result = new ArrayList<>();
        for (Object plugin : list(value, "requiresPlugins")) {
            String name = string(plugin, "plugin name");
            if (!name.matches("[A-Za-z0-9_.-]{1,128}") || result.contains(name)) throw new IOException("Invalid or duplicate plugin name");
            result.add(name);
        }
        return List.copyOf(result);
    }
    private static int integer(Object value, int min, int max, String name) throws IOException {
        if (!(value instanceof Number number)) throw new IOException(name + " must be numeric");
        try {
            int n = new BigDecimal(number.toString()).intValueExact();
            if (n < min || n > max) throw new ArithmeticException("out of range");
            return n;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IOException(name + " must be a whole number from " + min + " to " + max, e);
        }
    }
}
