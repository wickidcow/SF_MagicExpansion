package io.Yomicer.magicExpansion.utils.shop;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/** Reads whole shop records or refuses them; never turns an unreadable file into an empty shop. */
final class ShopFileStore {
    @FunctionalInterface
    interface Committer {
        void replace(Path staged, Path target) throws IOException;
    }

    private final Path directory;
    private final Consumer<String> errors;
    private final Committer committer;
    private final Map<ShopManager.Shop, Entry> entries = new IdentityHashMap<>();
    private final Set<ShopManager.Shop> retired = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Path> blocked = new java.util.HashSet<>();
    private boolean ready;

    ShopFileStore(Path directory, Consumer<String> errors) {
        this(directory, errors, (staged, target) -> Files.move(staged, target,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING));
    }

    ShopFileStore(Path directory, Consumer<String> errors, Committer committer) {
        this.directory = directory.toAbsolutePath().normalize();
        this.errors = Objects.requireNonNull(errors);
        this.committer = Objects.requireNonNull(committer);
    }

    synchronized List<ShopManager.Shop> load() throws IOException {
        ready = false;
        checkDirectory();
        // Failure to list the directory must not be interpreted as an empty directory.
        List<Path> paths = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(directory, "*.yml")) {
            for (Path path : stream) paths.add(path);
        }
        Map<ShopManager.Shop, Entry> loaded = new IdentityHashMap<>();
        List<ShopManager.Shop> shops = new ArrayList<>();
        Set<Path> rejected = new java.util.HashSet<>();
        for (Path path : paths) {
            try {
                byte[] original = readRegularFile(path);
                Parsed parsed = parse(original);
                ShopManager.Shop shop = new ShopManager.Shop();
                String filename = path.getFileName().toString();
                shop.name = filename.substring(0, filename.length() - 4);
                shop.trades.addAll(parsed.trades);
                loaded.put(shop, new Entry(path, shop.name, original, parsed.extensions));
                shops.add(shop);
            } catch (IOException | InvalidConfigurationException | RuntimeException | LinkageError error) {
                rejected.add(path);
                report(path, "load", error);
            }
        }
        retired.addAll(entries.keySet());
        entries.clear();
        entries.putAll(loaded);
        blocked.clear();
        blocked.addAll(rejected);
        ready = true;
        return shops;
    }

    synchronized boolean isAvailable(ShopManager.Shop shop) {
        Entry entry = entries.get(shop);
        return ready && entry != null && !entry.locked && Objects.equals(entry.name, shop.name);
    }

    synchronized boolean save(ShopManager.Shop shop) {
        Entry entry = entries.get(shop);
        Path staged = null;
        Path target = directory;
        try {
            if (!ready) throw new IOException("Shop directory has not loaded successfully");
            if (shop == null) throw new IOException("Missing shop");
            if (entry != null) {
                target = entry.path;
                if (entry.locked || !Objects.equals(entry.name, shop.name)) {
                    throw new IOException("Shop is read-only or its identity changed; reload after repair");
                }
            } else {
                if (retired.contains(shop)) throw new IOException("Stale shop reference; reopen the shop after reload");
                target = newPath(shop.name);
                if (blocked.contains(target) || entries.values().stream().anyMatch(e -> e.path.equals(newPathUnchecked(shop.name)))) {
                    throw new IOException("Filename belongs to another or unreadable shop");
                }
                assertUnchanged(target, null);
            }
            checkDirectory();
            byte[] expected = entry == null ? null : entry.original;
            assertUnchanged(target, expected);
            Encoded encoded = encode(shop, entry);
            // Validate the serialized representation before touching the original file.
            Parsed roundTrip = parse(encoded.bytes);
            assertSameTrades(shop.trades, roundTrip.trades);
            staged = Files.createTempFile(directory, ".shop-", ".pending");
            try (FileChannel output = FileChannel.open(staged, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer bytes = ByteBuffer.wrap(encoded.bytes);
                while (bytes.hasRemaining()) output.write(bytes);
                output.force(true);
            }
            assertUnchanged(target, expected);
            committer.replace(staged, target);
            staged = null;
            entries.put(shop, new Entry(target, shop.name, encoded.bytes, encoded.extensions));
            return true;
        } catch (IOException | InvalidConfigurationException | RuntimeException | LinkageError error) {
            if (entry != null) entry.locked = true;
            if (!target.equals(directory)) blocked.add(target);
            report(target, "save", error);
            return false;
        } finally {
            if (staged != null) {
                try {
                    Files.deleteIfExists(staged);
                } catch (IOException cleanupFailure) {
                    report(staged, "temporary-file cleanup", cleanupFailure);
                }
            }
        }
    }

    synchronized boolean delete(ShopManager.Shop shop) {
        Entry entry = entries.get(shop);
        try {
            if (!isAvailable(shop)) throw new IOException("Unknown or read-only shop; original file retained");
            checkDirectory();
            assertUnchanged(entry.path, entry.original);
            Files.delete(entry.path);
            entries.remove(shop);
            retired.add(shop);
            return true;
        } catch (IOException | RuntimeException error) {
            if (entry != null) entry.locked = true;
            report(entry == null ? directory : entry.path, "delete", error);
            return false;
        }
    }

    private void checkDirectory() throws IOException {
        if (Files.isSymbolicLink(directory)) throw new IOException("Refusing a symbolic-link shop directory");
        Files.createDirectories(directory);
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Shop path is not a directory");
    }

    private Path newPath(String name) throws IOException {
        if (name == null || name.isBlank() || !name.equals(name.replaceAll("[\\\\/:*?\"<>|]", "_"))) {
            throw new IOException("Shop name must be nonempty and must not require filename substitution");
        }
        Path path = newPathUnchecked(name);
        if (!Objects.equals(path.getParent(), directory)) throw new IOException("Shop name is outside its directory");
        return path;
    }

    private Path newPathUnchecked(String name) {
        return directory.resolve(name + ".yml").normalize();
    }

    private static byte[] readRegularFile(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isRegularFile()) throw new IOException("Shop source is not a regular file");
        return Files.readAllBytes(path);
    }

    private static void assertUnchanged(Path path, byte[] expected) throws IOException {
        final byte[] actual;
        try {
            actual = readRegularFile(path);
        } catch (NoSuchFileException missing) {
            if (expected == null) return;
            throw new IOException("Loaded shop file was removed externally; refusing to recreate it", missing);
        }
        if (expected == null || !Arrays.equals(expected, actual)) {
            throw new IOException("Shop file changed outside this snapshot; reload before saving");
        }
    }

    private static Parsed parse(byte[] bytes) throws IOException, InvalidConfigurationException {
        String text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(text);
        Object value = configuration.get("trades");
        if (!(value instanceof List<?> rows)) throw new IOException("Expected an explicit trades list");
        List<ShopManager.Trade> trades = new ArrayList<>();
        Map<ShopManager.Trade, Map<String, Object>> extensions = new IdentityHashMap<>();
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> map)) throw new IOException("Trade is not a mapping");
            Map<String, Object> preserved = new LinkedHashMap<>();
            for (var pair : map.entrySet()) {
                if (!(pair.getKey() instanceof String key)) throw new IOException("Trade key is not a string");
                preserved.put(key, pair.getValue());
            }
            ShopManager.Trade trade = new ShopManager.Trade();
            trade.result = item(map.get("result"));
            Object cost = map.get("cost");
            if (cost != null) {
                if (!(cost instanceof List<?> costs)) throw new IOException("Cost is not an item list");
                for (Object stack : costs) trade.costItems.add(item(stack));
            }
            trade.globalLimit = integer(map.get("globalLimit"));
            trade.personalLimit = integer(map.get("personalLimit"));
            trade.globalUsed = integer(map.get("globalUsed"));
            Object usage = map.get("personalUsed");
            if (usage != null) {
                if (!(usage instanceof Map<?, ?> people)) throw new IOException("Personal usage is not a mapping");
                for (var person : people.entrySet()) {
                    if (!(person.getKey() instanceof String key)) throw new IOException("Owner UUID key is not a string");
                    UUID uuid = UUID.fromString(key);
                    if (!uuid.toString().equalsIgnoreCase(key) || trade.personalUsed.containsKey(uuid)) {
                        throw new IOException("Ambiguous or duplicate owner UUID");
                    }
                    trade.personalUsed.put(uuid, integer(person.getValue()));
                }
            }
            trades.add(trade);
            extensions.put(trade, preserved);
        }
        return new Parsed(configuration, trades, extensions);
    }

    private static ItemStack item(Object value) throws IOException {
        if (value == null) return null;
        if (!(value instanceof ItemStack stack)) throw new IOException("Unreadable item record; original retained");
        return stack.clone();
    }

    private static int integer(Object value) throws IOException {
        if (value == null) return 0;
        if (!(value instanceof Number number)) throw new IOException("Counter or limit is not numeric");
        try {
            return new BigDecimal(number.toString()).intValueExact();
        } catch (NumberFormatException | ArithmeticException invalid) {
            throw new IOException("Counter or limit cannot be represented without loss", invalid);
        }
    }

    private static Encoded encode(ShopManager.Shop shop, Entry entry) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        if (entry != null) {
            configuration.loadFromString(StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(entry.original)).toString());
        }
        if (shop.trades == null) throw new IOException("Missing trade list");
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<ShopManager.Trade, Map<String, Object>> extensions = new IdentityHashMap<>();
        for (ShopManager.Trade trade : shop.trades) {
            if (trade == null) throw new IOException("Null trade cannot replace an existing record");
            Map<String, Object> row = new LinkedHashMap<>();
            if (entry != null) row.putAll(entry.extensions.getOrDefault(trade, Map.of()));
            row.put("result", item(trade.result));
            List<ItemStack> costs = new ArrayList<>();
            if (trade.costItems != null) for (ItemStack cost : trade.costItems) costs.add(item(cost));
            row.put("cost", costs);
            row.put("globalLimit", trade.globalLimit);
            row.put("personalLimit", trade.personalLimit);
            row.put("globalUsed", trade.globalUsed);
            if (trade.personalUsed == null) throw new IOException("Missing personal usage mapping");
            Map<String, Integer> usage = new LinkedHashMap<>();
            for (var person : trade.personalUsed.entrySet()) {
                if (person.getKey() == null || person.getValue() == null) throw new IOException("Invalid personal usage entry");
                usage.put(person.getKey().toString(), person.getValue());
            }
            row.put("personalUsed", usage);
            rows.add(row);
            extensions.put(trade, row);
        }
        configuration.set("trades", rows);
        return new Encoded(configuration.saveToString().getBytes(StandardCharsets.UTF_8), extensions);
    }

    private static void assertSameTrades(List<ShopManager.Trade> expected, List<ShopManager.Trade> actual) throws IOException {
        if (expected.size() != actual.size()) throw new IOException("Trade count changed during serialization");
        for (int index = 0; index < expected.size(); index++) {
            ShopManager.Trade left = expected.get(index), right = actual.get(index);
            List<ItemStack> costs = left.costItems == null ? List.of() : left.costItems;
            if (!Objects.equals(left.result, right.result) || !costs.equals(right.costItems)
                    || left.globalLimit != right.globalLimit || left.personalLimit != right.personalLimit
                    || left.globalUsed != right.globalUsed || !left.personalUsed.equals(right.personalUsed)) {
                throw new IOException("Trade data changed during serialization; original retained");
            }
        }
    }

    private void report(Path path, String operation, Throwable failure) {
        errors.accept("Portable shop " + operation + " refused for " + path.getFileName()
                + ": " + failure.getMessage() + ". Original data retained; repair and reload before retrying.");
    }

    private static final class Entry {
        final Path path;
        final String name;
        final byte[] original;
        final Map<ShopManager.Trade, Map<String, Object>> extensions;
        boolean locked;

        Entry(Path path, String name, byte[] original,
                Map<ShopManager.Trade, Map<String, Object>> extensions) {
            this.path = path;
            this.name = name;
            this.original = original;
            this.extensions = extensions;
        }
    }

    private record Parsed(YamlConfiguration configuration, List<ShopManager.Trade> trades,
            Map<ShopManager.Trade, Map<String, Object>> extensions) {}
    private record Encoded(byte[] bytes, Map<ShopManager.Trade, Map<String, Object>> extensions) {}
}
