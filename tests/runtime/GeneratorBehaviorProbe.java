package audit;

import static io.Yomicer.magicExpansion.utils.itemUtils.newItem.idDecorator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.electric.entitykillMachinee.EntityKillMachine;
import io.Yomicer.magicExpansion.items.generators.FishEnergyGenerator;
import io.Yomicer.magicExpansion.items.generators.NumberEnergyGenerator;
import io.Yomicer.magicExpansion.items.generators.TypeEnergyGenerator;
import io.Yomicer.magicExpansion.items.misc.ForceTwoToRsc;
import io.Yomicer.magicExpansion.items.misc.fish.FishKeys;
import io.Yomicer.magicExpansion.utils.AcceleratableMachine;
import io.Yomicer.magicExpansion.utils.ConvertItem;
import io.Yomicer.magicExpansion.utils.MachineBuffManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
                requestShutdown();
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
        testForceRscConverter(++index, passes, failures);
        testEntitySuppression(++index, passes, failures, () -> finish(passes, failures));
    }

    private void finish(List<String> passes, List<String> failures) {
        StringBuilder result = new StringBuilder();
        result.append(failures.isEmpty() ? "GENERATOR_BEHAVIOR_PASS\n" : "GENERATOR_BEHAVIOR_FAIL\n");
        result.append("cases=7\n");
        result.append("passed=").append(passes.size()).append('\n');
        for (String pass : passes) result.append("pass=").append(pass).append('\n');
        for (String failure : failures) result.append("failure=").append(failure).append('\n');
        publish(result.toString());

        getLogger().info((failures.isEmpty() ? "GENERATOR_BEHAVIOR_PASS" : "GENERATOR_BEHAVIOR_FAIL")
                + " passed=" + passes.size() + " failures=" + failures.size());
        requestShutdown();
    }

    private void testType(String id, BlockFace face, Material source, int min, int max,
                          int index, List<String> passes, List<String> failures) {
        Location location = location(index);
        Fixture fixture = null;
        try {
            SlimefunItem item = SlimefunItem.getById(idDecorator(id));
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
            SlimefunItem item = SlimefunItem.getById(idDecorator(id));
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
            SlimefunItem item = SlimefunItem.getById(idDecorator(id));
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

    private void testEntitySuppression(
            int index, List<String> passes, List<String> failures, Runnable finish) {
        String id = "PHANTON_SUPPRESSION";
        Location location = location(index);
        Fixture fixture = null;
        Entity phantom = null;
        try {
            SlimefunItem item = SlimefunItem.getById(idDecorator(id));
            check(item instanceof EntityKillMachine, id + " is not EntityKillMachine");
            check(item instanceof EnergyNetComponent, id + " is not an energy component");
            check(item.getBlockTicker() != null, id + " has no ticker");
            fixture = createFixture(item, location);
            EnergyNetComponent energy = (EnergyNetComponent) item;
            energy.setCharge(location, 0L, fixture.data());
            phantom = location.getWorld().spawnEntity(
                    location.clone().add(2.0, 2.0, 0.0), EntityType.PHANTOM);
            final Fixture activeFixture = fixture;
            final Entity activePhantom = phantom;
            // A newly generated chunk needs an entity-tracking tick before isValid
            // and getNearbyEntities can describe the spawned native entity.
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    check(activePhantom.isValid() && !activePhantom.isDead(),
                            id + " phantom fixture was not valid before the zero-energy tick");
                    item.getBlockTicker().tick(activeFixture.block(), item, activeFixture.data());
                    check(activePhantom.isValid() && !activePhantom.isDead(),
                            id + " removed a phantom with zero stored energy");
                    energy.setCharge(location, 1_000L, activeFixture.data());
                    item.getBlockTicker().tick(activeFixture.block(), item, activeFixture.data());
                    check(!activePhantom.isValid() || activePhantom.isDead(),
                            id + " did not remove a phantom with sufficient energy");
                    long remaining = energy.getChargeLong(location, activeFixture.data());
                    check(remaining == 740L,
                            id + " expected exact 260 J debit from 1000 J, got " + remaining);
                    passes.add(id + " zero-power gate and exact suppression debit");
                } catch (Throwable error) {
                    failures.add(id + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
                } finally {
                    activePhantom.remove();
                    cleanup(location);
                    finish.run();
                }
            }, 2L);
        } catch (Throwable error) {
            failures.add(id + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
            if (phantom != null) phantom.remove();
            if (fixture != null) cleanup(location);
            finish.run();
        }
    }

    private void testForceRscConverter(
            int index, List<String> passes, List<String> failures) {
        String id = "TWO_TO_MAGIC_GEO_MACHINE_FORCE";
        Location location = location(index);
        Fixture fixture = null;
        try {
            SlimefunItem item = SlimefunItem.getById(idDecorator(id));
            check(item instanceof ForceTwoToRsc, id + " is not ForceTwoToRsc");
            check(item instanceof EnergyNetComponent, id + " is not an energy component");
            check(item.getBlockTicker() != null, id + " has no ticker");
            fixture = createFixture(item, location);

            EnergyNetComponent energy = (EnergyNetComponent) item;
            BlockMenu menu = fixture.menu();

            ItemStack source = MagicExpansionItems.RSC_MAGIC_REDSTONE.clone();
            source.setAmount(7);
            menu.replaceExistingItem(10, source.clone());

            energy.setCharge(location, 0L, fixture.data());
            item.getBlockTicker().tick(fixture.block(), item, fixture.data());

            ItemStack stillSource = menu.getItemInSlot(10);
            check(stillSource != null
                            && stillSource.getAmount() == 7
                            && SlimefunUtils.isItemSimilar(stillSource, source, true),
                    id + " consumed source with zero energy");
            check(noItems(menu, new int[] {14,15,16,23,24,25,32,33,34,41,42,43}),
                    id + " produced output with zero energy");

            energy.setCharge(location, 100L, fixture.data());
            item.getBlockTicker().tick(fixture.block(), item, fixture.data());

            check(menu.getItemInSlot(10) == null || menu.getItemInSlot(10).getType().isAir(),
                    id + " did not consume the exact source stack");

            ItemStack expected = ConvertItem.stoneCreateItem("MAGIC_REDSTONE");
            check(expected != null && !expected.getType().isAir(),
                    id + " target conversion item is unavailable");
            int total = 0;
            for (int slot : new int[] {14,15,16,23,24,25,32,33,34,41,42,43}) {
                ItemStack stack = menu.getItemInSlot(slot);
                if (stack != null && SlimefunUtils.isItemSimilar(stack, expected, true)) {
                    total += stack.getAmount();
                }
            }
            check(total == 7, id + " expected 7 converted items, got " + total);
            check(energy.getChargeLong(location, fixture.data()) == 74L,
                    id + " expected exact 26 J debit from 100 J");

            passes.add(id + " zero-power gate, exact output and 26 J debit");
        } catch (Throwable error) {
            failures.add(id + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
        } finally {
            if (fixture != null) cleanup(location);
        }
    }

    private boolean noItems(BlockMenu menu, int[] slots) {
        for (int slot : slots) {
            ItemStack stack = menu.getItemInSlot(slot);
            if (stack != null && !stack.getType().isAir()) {
                return false;
            }
        }
        return true;
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

    private void requestShutdown() {
        // Finish the current callback and queued chunk listeners before stopping.
        // Paper treats shutdown with a heartbeat older than 1 second as a hard timeout.
        Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 2L);
    }

    private static void check(boolean condition, String explanation) {
        if (!condition) throw new IllegalStateException(explanation);
    }

    private record Fixture(BlockDataController blocks, Block block, SlimefunBlockData data, BlockMenu menu) {}
}
