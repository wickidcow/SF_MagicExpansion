package audit;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.generators.FishEnergyGenerator;
import io.Yomicer.magicExpansion.items.generators.NumberEnergyGenerator;
import io.Yomicer.magicExpansion.items.generators.TypeEnergyGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.FishKeys;
import io.Yomicer.magicExpansion.utils.AcceleratableMachine;
import io.Yomicer.magicExpansion.utils.MachineBuffManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Real-server behavior checks for MagicExpansion's special generator families. */
public final class GeneratorBehaviorProbe extends JavaPlugin {
    private final List<Chunk> heldChunks = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runProbe();
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, "GENERATOR_BEHAVIOR_FATAL", error);
                publish("GENERATOR_BEHAVIOR_FAIL\nfatal=" + error + "\n");
                Bukkit.shutdown();
            }
        }, 60L);
    }

    @Override
    public void onDisable() {
        for (Chunk chunk : heldChunks) chunk.removePluginChunkTicket(this);
    }

    private void runProbe() {
        List<String> passes = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        int index = 0;

        testType("POWER_FIRE_UNSTABLE", BlockFace.DOWN, Material.FIRE, 1, 26_000,
                ++index, passes, failures);
        testType("POWER_FIRE_STABILITY", BlockFace.DOWN, Material.FIRE, 260, 65_726,
                ++index, passes, failures);
        testType("POWER_COLOR_EGG_BLOCK", BlockFace.UP, Material.GLOWSTONE, 1, 65_726,
                ++index, passes, failures);
        testNumber(++index, passes, failures);
        testFish(++index, passes, failures);

        StringBuilder result = new StringBuilder();
        result.append(failures.isEmpty() ? "GENERATOR_BEHAVIOR_PASS\n" : "GENERATOR_BEHAVIOR_FAIL\n");
        result.append("cases=5\n");
        result.append("passed=").append(passes.size()).append('\n');
        for (String pass : passes) result.append("pass=").append(pass).append('\n');
        for (String failure : failures) result.append("failure=").append(failure).append('\n');
        publish(result.toString());

        getLogger().info((failures.isEmpty() ? "GENERATOR_BEHAVIOR_PASS" : "GENERATOR_BEHAVIOR_FAIL")
                + " passed=" + passes.size() + " failures=" + failures.size());
        Bukkit.shutdown();
    }

    private void testType(String id, BlockFace face, Material source, int min, int max,
                          int index, List<String> passes, List<String> failures) {
        Location location = location(index);
        Fixture fixture = null;
        try {
            SlimefunItem item = SlimefunItem.getById(id);
            check(item instanceof TypeEnergyGenerator, id + " is not TypeEnergyGenerator");
            TypeEnergyGenerator generator = (TypeEnergyGenerator) item;
            fixture = createFixture(item, location);

            Block sourceBlock = fixture.block().getRelative(face);
            sourceBlock.setType(Material.AIR, false);
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " generated without required " + source + " " + face);

            sourceBlock.setType(source == Material.FIRE ? Material.STONE : Material.DIRT, false);
            if (source == Material.FIRE) {
                // Deliberately wrong non-fire block immediately below.
                check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                        id + " generated from wrong source block");
            } else {
                check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                        id + " generated from wrong source block");
            }

            sourceBlock.setType(source, false);
            for (int sample = 0; sample < 32; sample++) {
                int generated = generator.getGeneratedOutput(location, fixture.data());
                check(generated >= min && generated <= max,
                        id + " output outside configured range: " + generated + " not " + min + ".." + max);
            }

            sourceBlock.setType(Material.AIR, false);
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " kept generating after required source was removed");
            passes.add(id + " source gate and output range");
        } catch (Throwable error) {
            failures.add(id + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
        } finally {
            if (fixture != null) cleanup(location);
        }
    }

    private void testNumber(int index, List<String> passes, List<String> failures) {
        String id = "POWER_COLOR_EGG_KEY";
        Location location = location(index);
        Fixture fixture = null;
        try {
            SlimefunItem item = SlimefunItem.getById(id);
            check(item instanceof NumberEnergyGenerator, id + " is not NumberEnergyGenerator");
            NumberEnergyGenerator generator = (NumberEnergyGenerator) item;
            fixture = createFixture(item, location);
            BlockMenu menu = fixture.menu();

            applyPattern(menu, "000000000");
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " generated for empty invalid key");

            applyPattern(menu, "100000000");
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " generated for invalid key");

            applyPattern(menu, "110011111");
            for (int sample = 0; sample < 24; sample++) {
                int generated = generator.getGeneratedOutput(location, fixture.data());
                check(generated >= 2_600 && generated <= 65_726,
                        id + " primary key output outside range: " + generated);
            }

            applyPattern(menu, "111011011");
            int secondary = generator.getGeneratedOutput(location, fixture.data());
            int expectedSecondary = (int) ((65_726 + 2_600) * 0.35D);
            check(secondary == expectedSecondary,
                    id + " secondary key output mismatch: expected " + expectedSecondary + ", got " + secondary);

            applyPattern(menu, "000000000");
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " continued generating after key was cleared");
            passes.add(id + " invalid/primary/secondary binary patterns");
        } catch (Throwable error) {
            failures.add(id + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
        } finally {
            if (fixture != null) cleanup(location);
        }
    }

    private void testFish(int index, List<String> passes, List<String> failures) {
        String id = "POWER_FISH_ELECTRIC";
        Location location = location(index);
        Fixture fixture = null;
        try {
            SlimefunItem item = SlimefunItem.getById(id);
            check(item instanceof FishEnergyGenerator, id + " is not FishEnergyGenerator");
            check(item instanceof AcceleratableMachine, id + " does not advertise acceleration support");
            FishEnergyGenerator generator = (FishEnergyGenerator) item;
            fixture = createFixture(item, location);
            BlockMenu menu = fixture.menu();

            menu.replaceExistingItem(13, null);
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " generated with no fish");

            ItemStack normal = fish("MYSTIC_EEL", 2.0D, "COMMON_FISH");
            menu.replaceExistingItem(13, normal);
            check(generator.getGeneratedOutput(location, fixture.data()) == 100_000,
                    id + " did not apply Mystic Eel base power x weight");

            ItemStack rare = fish("MYSTIC_EEL", 2.0D, "RARE_FISH");
            menu.replaceExistingItem(13, rare);
            check(generator.getGeneratedOutput(location, fixture.data()) == 700_000,
                    id + " did not apply fish weight-rarity multiplier");

            MachineBuffManager.apply(location, 300L, 2.0D);
            check(generator.getGeneratedOutputLong(location, fixture.data()) == 1_400_000L,
                    id + " did not apply the live acceleration-fish multiplier");
            MachineBuffManager.remove(location);
            check(generator.getGeneratedOutputLong(location, fixture.data()) == 700_000L,
                    id + " kept acceleration output after the buff was removed");

            ItemStack maxWeight = fish("MYSTIC_EEL", 21.0D, "MAX_WEIGHT_FISH");
            menu.replaceExistingItem(13, maxWeight);
            long expectedMaxWeight = 10_498_950_000L;
            check(generator.getGeneratedOutputLong(location, fixture.data()) == expectedMaxWeight,
                    id + " long output overflowed for max-weight Mystic Eel");
            check(generator.getGeneratedOutput(location, fixture.data()) == Integer.MAX_VALUE,
                    id + " legacy int output should clamp instead of wrapping negative");

            ItemStack invalid = fish("NOT_A_REAL_FISH", 2.0D, "COMMON_FISH");
            menu.replaceExistingItem(13, invalid);
            check(generator.getGeneratedOutput(location, fixture.data()) == 0,
                    id + " generated from an unknown fish type");

            passes.add(id + " empty/valid/rarity/acceleration/long-boundary/invalid fish data");
        } catch (Throwable error) {
            failures.add(id + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
        } finally {
            if (fixture != null) cleanup(location);
        }
    }

    private ItemStack fish(String type, double weight, String rarity) {
        ItemStack item = new ItemStack(Material.COD);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(FishKeys.FISH_TYPE, PersistentDataType.STRING, type);
        meta.getPersistentDataContainer().set(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE, weight);
        meta.getPersistentDataContainer().set(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING, rarity);
        item.setItemMeta(meta);
        return item;
    }

    private void applyPattern(BlockMenu menu, String pattern) {
        check(pattern.length() == 9, "Generator key pattern must contain nine bits");
        for (int i = 0; i < 9; i++) {
            menu.replaceExistingItem(i, pattern.charAt(i) == '1' ? new ItemStack(Material.SNOWBALL) : null);
        }
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
        check(material.isBlock(), id(item) + " template is not a block");
        block.setType(material, false);
        SlimefunBlockData data = blocks.createBlock(location, item.getId());
        BlockMenu menu = data.getBlockMenu();
        check(data.isDataLoaded(), id(item) + " block data is not loaded");
        check(menu != null, id(item) + " menu did not initialize");
        Slimefun.getTickerTask().pauseMachineTicker(location);
        return new Fixture(blocks, block, data, menu);
    }

    private void cleanup(Location location) {
        try {
            MachineBuffManager.remove(location);
            Slimefun.getTickerTask().resumeMachineTicker(location);
            BlockDataController blocks = Slimefun.getDatabaseManager().getBlockDataController();
            blocks.removeBlockData(location);
            for (BlockFace face : new BlockFace[] {BlockFace.UP, BlockFace.DOWN}) {
                location.getBlock().getRelative(face).setType(Material.AIR, false);
            }
            location.getBlock().setType(Material.AIR, false);
        } catch (Throwable error) {
            getLogger().log(Level.WARNING, "Generator fixture cleanup failed at " + location, error);
        }
    }

    private Location location(int index) {
        World world = Bukkit.getWorlds().getFirst();
        return new Location(world, 14_000 + index * 5, 80, 14_000);
    }

    private String id(SlimefunItem item) {
        return item == null ? "<null>" : item.getId();
    }

    private void publish(String text) {
        try {
            Files.writeString(Path.of("generator-behavior-result.txt"), text);
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Could not save generator behavior evidence", error);
        }
    }

    private static void check(boolean condition, String explanation) {
        if (!condition) throw new IllegalStateException(explanation);
    }

    private record Fixture(BlockDataController blocks, Block block, SlimefunBlockData data, BlockMenu menu) {}
}
