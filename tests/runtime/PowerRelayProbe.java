package audit;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.generators.EnergyInputGenerator;
import io.Yomicer.magicExpansion.items.generators.EnergyOutputGenerator;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Real-server Power Card input/output relay conservation checks. */
public final class PowerRelayProbe extends JavaPlugin {
    private final List<Chunk> heldChunks = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runProbe();
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, "POWER_RELAY_FATAL", error);
                publish("POWER_RELAY_FAIL\nfatal=" + error + "\n");
                Bukkit.shutdown();
            }
        }, 60L);
    }

    @Override
    public void onDisable() {
        for (Chunk chunk : heldChunks) {
            chunk.removePluginChunkTicket(this);
        }
    }

    private void runProbe() {
        List<String> passes = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        try {
            testInputRelay();
            passes.add("input relay Long.MAX boundary, exact charge removal, completion move");
        } catch (Throwable error) {
            failures.add("input relay: " + error.getClass().getSimpleName() + ": " + error.getMessage());
        }

        try {
            testOutputRelay();
            passes.add("output relay free-space bound, card decrement, empty-card move");
        } catch (Throwable error) {
            failures.add("output relay: " + error.getClass().getSimpleName() + ": " + error.getMessage());
        }

        StringBuilder result = new StringBuilder();
        result.append(failures.isEmpty() ? "POWER_RELAY_PASS\n" : "POWER_RELAY_FAIL\n");
        result.append("passed=").append(passes.size()).append('\n');
        for (String pass : passes) result.append("pass=").append(pass).append('\n');
        for (String failure : failures) result.append("failure=").append(failure).append('\n');
        publish(result.toString());
        Bukkit.shutdown();
    }

    private void testInputRelay() {
        SlimefunItem item = SlimefunItem.getById("MAGIC_POWER_INPUT_MACHINE");
        check(item instanceof EnergyInputGenerator, "MAGIC_POWER_INPUT_MACHINE is not EnergyInputGenerator");
        EnergyInputGenerator relay = (EnergyInputGenerator) item;
        Location location = new Location(Bukkit.getWorlds().getFirst(), 16_000, 80, 16_000);
        Fixture fixture = createFixture(item, location);

        try {
            ItemStack card = card("POWER_CARD", Long.MAX_VALUE, Long.MAX_VALUE - 5L);
            fixture.menu().replaceExistingItem(19, card);
            ((EnergyNetComponent) relay).setCharge(location, 100L, fixture.data());

            check(item.getBlockTicker() != null && item.getBlockTicker().isSynchronized(),
                    "Input relay ticker must be synchronized because it mutates menu/hologram state");

            item.getBlockTicker().tick(fixture.block(), item, fixture.data());

            ItemStack charging = fixture.menu().getItemInSlot(19);
            check(charging != null, "Input relay moved card before completing the boundary fill");
            check(stored(charging) == Long.MAX_VALUE, "Input relay overflowed or missed Long.MAX_VALUE");
            check(((EnergyNetComponent) relay).getChargeLong(location, fixture.data()) == 95L,
                    "Input relay did not remove exactly five joules from machine storage");

            item.getBlockTicker().tick(fixture.block(), item, fixture.data());
            check(fixture.menu().getItemInSlot(19) == null, "Completed card remained in input slot");
            ItemStack completed = fixture.menu().getItemInSlot(25);
            check(completed != null && stored(completed) == Long.MAX_VALUE,
                    "Completed card was not moved intact to output slot");
        } finally {
            cleanup(location);
        }
    }

    private void testOutputRelay() {
        SlimefunItem item = SlimefunItem.getById("MAGIC_POWER_OUTPUT_MACHINE");
        check(item instanceof EnergyOutputGenerator, "MAGIC_POWER_OUTPUT_MACHINE is not EnergyOutputGenerator");
        EnergyOutputGenerator relay = (EnergyOutputGenerator) item;
        Location location = new Location(Bukkit.getWorlds().getFirst(), 16_010, 80, 16_000);
        Fixture fixture = createFixture(item, location);

        try {
            int capacity = relay.getCapacity();
            ((EnergyNetComponent) relay).setCharge(location, (long) capacity - 10L, fixture.data());
            fixture.menu().replaceExistingItem(19, card("POWER_CARD", Long.MAX_VALUE, Long.MAX_VALUE));

            int generated = relay.getGeneratedOutput(location, fixture.data());
            check(generated == 10, "Output relay ignored its ten-joule free-space boundary: " + generated);
            ItemStack card = fixture.menu().getItemInSlot(19);
            check(card != null && stored(card) == Long.MAX_VALUE - 10L,
                    "Output relay did not decrement the card by exactly ten joules");

            ((EnergyNetComponent) relay).setCharge(location, 0L, fixture.data());
            fixture.menu().replaceExistingItem(19, card("POWER_CARD", 1_000L, 1_000L));
            generated = relay.getGeneratedOutput(location, fixture.data());
            check(generated == 1_000, "Output relay did not release all available card energy");
            check(stored(fixture.menu().getItemInSlot(19)) == 0L,
                    "Output relay card should be empty after full discharge");

            generated = relay.getGeneratedOutput(location, fixture.data());
            check(generated == 0, "Empty Power Card generated energy");
            check(fixture.menu().getItemInSlot(19) == null, "Empty card remained in input slot");
            ItemStack completed = fixture.menu().getItemInSlot(25);
            check(completed != null && stored(completed) == 0L,
                    "Empty card was not moved intact to output slot");

            fixture.menu().replaceExistingItem(25, null);
            fixture.menu().replaceExistingItem(19, card("POWER_CARD", 1_000L, -1L));
            check(relay.getGeneratedOutput(location, fixture.data()) == 0,
                    "Relay generated from invalid negative stored energy");
            check(stored(fixture.menu().getItemInSlot(19)) == -1L,
                    "Invalid historical data was silently rewritten");
        } finally {
            cleanup(location);
        }
    }

    private ItemStack card(String id, long maximum, long stored) {
        SlimefunItem sf = SlimefunItem.getById(id);
        check(sf != null, "Missing registered card " + id);
        ItemStack item = sf.getItem().clone();
        item.setAmount(1);
        var meta = item.getItemMeta();
        MagicExpansion plugin = MagicExpansion.getInstance();
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "max_power"), PersistentDataType.LONG, maximum);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "now_power"), PersistentDataType.LONG, stored);
        item.setItemMeta(meta);
        return item;
    }

    private long stored(ItemStack item) {
        check(item != null && item.hasItemMeta(), "Energy card metadata is missing");
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(
                new NamespacedKey(MagicExpansion.getInstance(), "now_power"),
                PersistentDataType.LONG,
                Long.MIN_VALUE);
    }

    private Fixture createFixture(SlimefunItem item, Location location) {
        World world = location.getWorld();
        check(world != null, "Fixture world is null");
        Chunk chunk = world.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        chunk.addPluginChunkTicket(this);
        heldChunks.add(chunk);

        BlockDataController blocks = Slimefun.getDatabaseManager().getBlockDataController();
        blocks.getChunkData(chunk);
        Block block = location.getBlock();
        Material material = item.getItem().getType();
        check(material.isBlock(), item.getId() + " template is not a block");
        block.setType(material, false);
        SlimefunBlockData data = blocks.createBlock(location, item.getId());
        BlockMenu menu = data.getBlockMenu();
        check(data.isDataLoaded(), item.getId() + " block data did not load");
        check(menu != null, item.getId() + " menu did not initialize");
        Slimefun.getTickerTask().pauseMachineTicker(location);
        return new Fixture(blocks, block, data, menu);
    }

    private void cleanup(Location location) {
        try {
            Slimefun.getTickerTask().resumeMachineTicker(location);
            Slimefun.getDatabaseManager().getBlockDataController().removeBlockData(location);
            location.getBlock().setType(Material.AIR, false);
        } catch (Throwable error) {
            getLogger().log(Level.WARNING, "Power relay fixture cleanup failed at " + location, error);
        }
    }

    private void publish(String text) {
        try {
            Files.writeString(Path.of("power-relay-result.txt"), text);
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Could not save power relay evidence", error);
        }
    }

    private static void check(boolean condition, String explanation) {
        if (!condition) throw new IllegalStateException(explanation);
    }

    private record Fixture(BlockDataController blocks, Block block, SlimefunBlockData data, BlockMenu menu) {}
}
