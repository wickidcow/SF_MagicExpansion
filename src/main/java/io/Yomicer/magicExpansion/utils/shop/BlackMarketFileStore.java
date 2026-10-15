package io.Yomicer.magicExpansion.utils.shop;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * A crash-resistant, per-player Black Market ledger.
 *
 * <p>The cycle marker is committed first on rotation. Each player's ten offers,
 * reveals, and purchase receipts are then written together with an atomic file
 * replacement. Old-cycle player records cannot be reused after rotation.
 *
 * <p>All access is from the server thread. The replacement is synchronous on
 * purpose: rewards must never be issued before their purchase receipt is saved.
 */
final class BlackMarketFileStore {
    private static final int FORMAT = 1;
    private static final int OFFERS = 10;
    private static final long FUTURE_CLOCK_TOLERANCE_MS = 60_000L;

    @FunctionalInterface
    interface Committer {
        void replace(Path staged, Path target) throws IOException;
    }

    record Cycle(UUID id, long startedAt) {}

    record PlayerState(List<BlackMarketManager.BlackMarketTrade> trades,
                       Set<Integer> purchased, Set<Integer> revealed) {}

    enum CycleStatus {
        MATCH,
        SAME_CYCLE_DIFFERENT_BYTES,
        DIFFERENT_CYCLE,
        MISSING,
        UNREADABLE,
        NOT_LOADED
    }

    /**
     * Operator diagnostics only. Inspection never refreshes, adopts or rewrites the cycle.
     * A matching YAML cycle with different bytes is still a conflict until deliberately
     * reconciled offline, because another writer may also have modified player receipts.
     */
    record CycleInspection(CycleStatus status, Cycle loadedCycle, Cycle diskCycle, String detail) {}


    private final Path root;
    private final Path players;
    private final Path cycleFile;
    private final Committer committer;
    private byte[] cycleSnapshot;
    private final Map<UUID, byte[]> playerSnapshots = new HashMap<>();
    private final Set<UUID> lockedPlayers = new HashSet<>();

    BlackMarketFileStore(Path root) {
        this(root, (staged, target) -> Files.move(staged, target,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING));
    }

    BlackMarketFileStore(Path root, Committer committer) {
        this.root = Objects.requireNonNull(root).toAbsolutePath().normalize();
        this.players = this.root.resolve("players");
        this.cycleFile = this.root.resolve("cycle.yml");
        this.committer = Objects.requireNonNull(committer);
    }

    synchronized Cycle loadOrCreate(long now) throws IOException {
        // A failed initialization must never leave a previous cycle authorized.
        cycleSnapshot = null;
        checkDirectories();
        playerSnapshots.clear();
        lockedPlayers.clear();
        byte[] original = read(cycleFile);
        if (original == null) {
            BlackMarketBootstrapGuard.requireEmptyStore(root);
            Cycle cycle = new Cycle(UUID.randomUUID(), now);
            byte[] encoded = encodeCycle(cycle);
            replace(cycleFile, null, encoded);
            cycleSnapshot = encoded;
            return cycle;
        }
        Cycle cycle = decodeCycle(original);
        if (cycle.startedAt() > now + FUTURE_CLOCK_TOLERANCE_MS) {
            throw new IOException("Black Market clock moved backward; refusing to reset purchase limits");
        }
        cycleSnapshot = original;
        return cycle;
    }

    synchronized Cycle refresh(Cycle previous, long now) throws IOException {
        checkDirectories();
        if (previous == null || cycleSnapshot == null) {
            throw new IOException("Black Market cycle not loaded");
        }
        requireCycle(previous);
        Cycle fresh = new Cycle(UUID.randomUUID(), now);
        byte[] encoded = encodeCycle(fresh);
        replace(cycleFile, cycleSnapshot, encoded);
        cycleSnapshot = encoded;
        playerSnapshots.clear();
        lockedPlayers.clear();
        return fresh;
    }

    synchronized PlayerState loadPlayer(UUID playerId, Cycle cycle) throws IOException {
        checkDirectories();
        requireCycle(cycle);
        Path path = playerFile(playerId);
        byte[] original = read(path);
        playerSnapshots.put(playerId, original);
        if (original == null) return null;
        try {
            return decodePlayer(original, playerId, cycle);
        } catch (IOException | RuntimeException invalid) {
            lockedPlayers.add(playerId);
            throw invalid;
        }
    }

    synchronized void savePlayer(UUID playerId, Cycle cycle, PlayerState state) throws IOException {
        checkDirectories();
        requireCycle(cycle);
        if (lockedPlayers.contains(playerId)) {
            throw new IOException("Unreadable player ledger is locked until cycle refresh");
        }
        if (!playerSnapshots.containsKey(playerId)) {
            throw new IOException("Player record was not loaded before save");
        }
        Path path = playerFile(playerId);
        byte[] encoded = encodePlayer(playerId, cycle, state);
        replace(path, playerSnapshots.get(playerId), encoded);
        playerSnapshots.put(playerId, encoded);
    }


    synchronized CycleInspection inspectCycle(Cycle activeCycle) {
        if (activeCycle == null || cycleSnapshot == null) {
            return new CycleInspection(CycleStatus.NOT_LOADED, activeCycle, null, null);
        }
        try {
            // Intentionally do not call checkDirectories(): status is strictly read-only.
            final byte[] current = read(cycleFile);
            if (current == null) {
                return new CycleInspection(CycleStatus.MISSING, activeCycle, null, null);
            }
            final Cycle disk = decodeCycle(current);
            if (!disk.equals(activeCycle)) {
                return new CycleInspection(CycleStatus.DIFFERENT_CYCLE, activeCycle, disk, null);
            }
            return new CycleInspection(Arrays.equals(cycleSnapshot, current)
                    ? CycleStatus.MATCH : CycleStatus.SAME_CYCLE_DIFFERENT_BYTES,
                    activeCycle, disk, null);
        } catch (IOException | RuntimeException exception) {
            return new CycleInspection(CycleStatus.UNREADABLE, activeCycle, null,
                    exception.getClass().getSimpleName());
        }
    }

    private void requireCycle(Cycle cycle) throws IOException {
        if (cycle == null || cycleSnapshot == null
                || !Arrays.equals(cycleSnapshot, read(cycleFile))) {
            throw new IOException("Black Market cycle marker changed externally; refusing a purchase");
        }
        Cycle recorded = decodeCycle(cycleSnapshot);
        if (!recorded.equals(cycle)) throw new IOException("Black Market cycle mismatch");
    }

    private void checkDirectories() throws IOException {
        checkDirectory(root);
        checkDirectory(players);
    }

    private static void checkDirectory(Path path) throws IOException {
        if (Files.isSymbolicLink(path)) throw new IOException("Refusing symbolic-link Black Market directory: " + path);
        Files.createDirectories(path);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Not a Black Market directory: " + path);
        }
    }

    private static byte[] read(Path path) throws IOException {
        return BlackMarketStateReader.read(path);
    }

    private void replace(Path target, byte[] expected, byte[] contents) throws IOException {
        byte[] original = read(target);
        if (!Arrays.equals(original, expected)) {
            throw new IOException("Black Market state changed outside this session: " + target);
        }
        Path staged = Files.createTempFile(target.getParent(), ".black-market-", ".pending");
        try {
            try (FileChannel channel = FileChannel.open(staged, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(contents);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            if (!Arrays.equals(read(target), expected)) {
                throw new IOException("Black Market state changed during save: " + target);
            }
            committer.replace(staged, target);
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    private static byte[] encodeCycle(Cycle cycle) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("format", FORMAT);
        yaml.set("id", cycle.id().toString());
        yaml.set("started-at", cycle.startedAt());
        return yaml.saveToString().getBytes(StandardCharsets.UTF_8);
    }

    private static Cycle decodeCycle(byte[] encoded) throws IOException {
        YamlConfiguration yaml = parse(encoded);
        if (integer(yaml.get("format")) != FORMAT) throw new IOException("Unsupported Black Market cycle format");
        UUID id = uuid(yaml.get("id"));
        long started = integer(yaml.get("started-at"));
        if (started <= 0) throw new IOException("Invalid Black Market cycle timestamp");
        return new Cycle(id, started);
    }

    private static byte[] encodePlayer(UUID owner, Cycle cycle, PlayerState state) throws IOException {
        if (state == null || state.trades() == null || state.trades().size() != OFFERS) {
            throw new IOException("Expected exactly ten Black Market offers");
        }
        validateIndices(state.purchased(), state.revealed());

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("format", FORMAT);
        yaml.set("player", owner.toString());
        yaml.set("cycle-id", cycle.id().toString());

        List<Map<String, Object>> offers = new ArrayList<>(OFFERS);
        for (BlackMarketManager.BlackMarketTrade trade : state.trades()) {
            if (trade == null) throw new IOException("Missing Black Market offer");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", item(trade.result));
            row.put("free", trade.isFree);
            row.put("hard", trade.isHard);
            List<ItemStack> costs = new ArrayList<>();
            if (trade.costs != null) for (ItemStack cost : trade.costs) costs.add(item(cost));
            if (!trade.isFree && costs.isEmpty()) throw new IOException("Paid Black Market offer has no cost");
            row.put("costs", costs);
            offers.add(row);
        }
        yaml.set("trades", offers);
        yaml.set("purchased", state.purchased().stream().sorted().toList());
        yaml.set("revealed", state.revealed().stream().sorted().toList());

        byte[] encoded = yaml.saveToString().getBytes(StandardCharsets.UTF_8);
        // Do not commit a record that the next server start could not read.
        PlayerState reread = decodePlayer(encoded, owner, cycle);
        if (reread == null || reread.trades().size() != OFFERS) {
            throw new IOException("Black Market record did not round-trip");
        }
        return encoded;
    }

    private static PlayerState decodePlayer(byte[] encoded, UUID playerId, Cycle cycle) throws IOException {
        YamlConfiguration yaml = parse(encoded);
        if (integer(yaml.get("format")) != FORMAT) throw new IOException("Unsupported player record format");
        if (!uuid(yaml.get("player")).equals(playerId)) throw new IOException("Black Market player mismatch");
        UUID recordedCycle = uuid(yaml.get("cycle-id"));
        if (!recordedCycle.equals(cycle.id())) return null;

        Object raw = yaml.get("trades");
        if (!(raw instanceof List<?> rows) || rows.size() != OFFERS) {
            throw new IOException("Expected exactly ten saved Black Market offers");
        }
        List<BlackMarketManager.BlackMarketTrade> trades = new ArrayList<>(OFFERS);
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> fields)) throw new IOException("Invalid Black Market offer");
            BlackMarketManager.BlackMarketTrade trade = new BlackMarketManager.BlackMarketTrade();
            trade.result = item(fields.get("result"));
            trade.isFree = flag(fields.get("free"));
            trade.isHard = flag(fields.get("hard"));
            Object rawCosts = fields.get("costs");
            if (!(rawCosts instanceof List<?> costs)) throw new IOException("Invalid Black Market costs");
            trade.costs = new ArrayList<>();
            for (Object cost : costs) trade.costs.add(item(cost));
            if (!trade.isFree && trade.costs.isEmpty()) throw new IOException("Missing trade payment");
            trades.add(trade);
        }
        Set<Integer> purchased = indices(yaml.get("purchased"));
        Set<Integer> revealed = indices(yaml.get("revealed"));
        validateIndices(purchased, revealed);
        return new PlayerState(trades, purchased, revealed);
    }

    private static void validateIndices(Set<Integer> purchased, Set<Integer> revealed) throws IOException {
        if (purchased == null || revealed == null || !revealed.containsAll(purchased)) {
            throw new IOException("Black Market purchase receipts must also be revealed");
        }
        for (Integer index : purchased) slot(index);
        for (Integer index : revealed) slot(index);
    }

    private static Set<Integer> indices(Object raw) throws IOException {
        if (!(raw instanceof List<?> rows)) throw new IOException("Missing Black Market slot list");
        Set<Integer> result = new LinkedHashSet<>();
        for (Object row : rows) {
            long number = integer(row);
            if (number < 0 || number >= OFFERS) throw new IOException("Invalid Black Market slot index");
            int index = (int) number;
            if (!result.add(index)) throw new IOException("Duplicate Black Market slot receipt");
        }
        return result;
    }

    private static int slot(Integer index) throws IOException {
        if (index == null || index < 0 || index >= OFFERS) {
            throw new IOException("Invalid Black Market slot index");
        }
        return index;
    }

    private static ItemStack item(Object raw) throws IOException {
        if (!(raw instanceof ItemStack stack) || stack.getType().isAir() || stack.getAmount() <= 0) {
            throw new IOException("Invalid or unreadable Black Market item");
        }
        // Persist a native stack, not a Slimefun template subclass or a wrapper
        // retaining its clone as a nested delegate. The complete public serializer
        // preserves the item data/count without reconstructing by material or ID.
        // Keep the existing YAML round-trip validation and receipt ordering below.
        return ItemStack.deserialize(stack.serialize());
    }

    private static boolean flag(Object raw) throws IOException {
        if (!(raw instanceof Boolean value)) throw new IOException("Missing Black Market flag");
        return value;
    }

    private static long integer(Object raw) throws IOException {
        if (!(raw instanceof Number number)) throw new IOException("Expected numeric Black Market value");
        try {
            return new BigDecimal(number.toString()).longValueExact();
        } catch (NumberFormatException | ArithmeticException invalid) {
            throw new IOException("Invalid numeric Black Market value", invalid);
        }
    }

    private static UUID uuid(Object raw) throws IOException {
        if (!(raw instanceof String string)) throw new IOException("Missing Black Market UUID");
        try {
            UUID value = UUID.fromString(string);
            if (!value.toString().equals(string)) throw new IOException("Noncanonical Black Market UUID");
            return value;
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid Black Market UUID", invalid);
        }
    }

    private static YamlConfiguration parse(byte[] bytes) throws IOException {
        try {
            String yamlText = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yamlText);
            return config;
        } catch (CharacterCodingException | InvalidConfigurationException | RuntimeException invalid) {
            throw new IOException("Unreadable Black Market YAML; original file retained", invalid);
        }
    }

    private Path playerFile(UUID uuid) {
        return players.resolve(Objects.requireNonNull(uuid) + ".yml");
    }
}
