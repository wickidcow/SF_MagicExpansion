package audit;

import static io.Yomicer.magicExpansion.utils.itemUtils.newItem.idDecorator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.generators.FishOutputMachineStack;
import io.Yomicer.magicExpansion.items.misc.CargoCoreMore;
import io.Yomicer.magicExpansion.items.misc.CargoFragment;
import io.Yomicer.magicExpansion.items.misc.fish.FishKeys;
import io.Yomicer.magicExpansion.items.tools.VoidTouch;
import io.Yomicer.magicExpansion.utils.SameItemJudge;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
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

/** Real-server conservation test for the large-output stacked vivarium path. */
public final class StackedVivariumProbe extends JavaPlugin {
    private static final long EXPECTED_BATCH = 1_374_252_095_126_592L;
    private final List<Chunk> heldChunks = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runProbe();
                publish("STACKED_VIVARIUM_PASS\n"
                        + "batch=" + EXPECTED_BATCH + "\n"
                        + "small_destination=unchanged\n"
                        + "unlimited_destination=exact\n");
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, "STACKED_VIVARIUM_FATAL", error);
                publish("STACKED_VIVARIUM_FAIL\nfatal=" + error + "\n");
            }
            requestShutdown();
        }, 60L);
    }

    @Override
    public void onDisable() {
        for (Chunk chunk : heldChunks) {
            chunk.removePluginChunkTicket(this);
        }
    }

    private void runProbe() {
        SlimefunItem stackItem = SlimefunItem.getById(idDecorator("FISH_VIVARIUM_STACK"));
        SlimefunItem targetItem = SlimefunItem.getById(idDecorator("CARGO_TERMINAL_RENEW"));
        SlimefunItem fragmentItem = SlimefunItem.getById(idDecorator("CARGO_FRAGMENT"));
        SlimefunItem voidTouchItem = SlimefunItem.getById(idDecorator("VOID_TOUCH"));
        SlimefunItem normalVivarium = SlimefunItem.getById(idDecorator("FISH_VIVARIUM"));

        check(stackItem instanceof FishOutputMachineStack, "Missing stacked vivarium registration");
        check(targetItem instanceof CargoCoreMore, "Missing rebuilt Magic Storage registration");
        check(fragmentItem instanceof CargoFragment, "Missing Aether Cargo Chest registration");
        check(voidTouchItem instanceof VoidTouch, "Missing Void Touch registration");
        check(normalVivarium != null, "Missing normal vivarium registration");
        check(stackItem.getBlockTicker() != null, "Stacked vivarium has no ticker");

        Location stackLocation = new Location(Bukkit.getWorlds().getFirst(), 17_000, 80, 17_000);
        Location targetLocation = new Location(Bukkit.getWorlds().getFirst(), 17_010, 80, 17_000);
        Fixture stack = createFixture(stackItem, stackLocation);
        Fixture target = createFixture(targetItem, targetLocation);

        try {
            ItemStack outputTemplate = ItemStack.deserialize(SlimefunItems.COPPER_DUST.serialize());
            outputTemplate.setAmount(1);
            target.data().setData("item_type_0", SameItemJudge.itemToBase64(outputTemplate));
            target.data().setData("item_count_0", "90");
            target.data().setData("item_max_0", "100");

            ItemStack fragment = fragmentItem.getItem().clone();
            fragment.setAmount(1);
            var fragmentMeta = fragment.getItemMeta();
            fragmentMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json"),
                    PersistentDataType.STRING,
                    SameItemJudge.itemToBase64(ItemStack.deserialize(normalVivarium.getItem().serialize())));
            fragmentMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount"),
                    PersistentDataType.INTEGER,
                    Integer.MAX_VALUE);
            fragment.setItemMeta(fragmentMeta);
            stack.menu().replaceExistingItem(0, fragment);

            ItemStack fish = new ItemStack(Material.COD);
            var fishMeta = fish.getItemMeta();
            fishMeta.getPersistentDataContainer().set(
                    FishKeys.FISH_TYPE, PersistentDataType.STRING, "CopperDustFish");
            fishMeta.getPersistentDataContainer().set(
                    FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE, 64.0D);
            fishMeta.getPersistentDataContainer().set(
                    FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING, "MAX_WEIGHT_FISH");
            fish.setItemMeta(fishMeta);
            stack.menu().replaceExistingItem(49, fish);

            ItemStack touch = voidTouchItem.getItem().clone();
            touch.setAmount(1);
            var touchMeta = touch.getItemMeta();
            touchMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MagicExpansion.getInstance(), "touch_x"),
                    PersistentDataType.INTEGER, targetLocation.getBlockX());
            touchMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MagicExpansion.getInstance(), "touch_y"),
                    PersistentDataType.INTEGER, targetLocation.getBlockY());
            touchMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MagicExpansion.getInstance(), "touch_z"),
                    PersistentDataType.INTEGER, targetLocation.getBlockZ());
            touchMeta.getPersistentDataContainer().set(
                    new NamespacedKey(MagicExpansion.getInstance(), "touch_world"),
                    PersistentDataType.STRING, targetLocation.getWorld().getName());
            touch.setItemMeta(touchMeta);
            stack.menu().replaceExistingItem(50, touch);

            EnergyNetComponent energy = (EnergyNetComponent) stackItem;
            long initialCharge = 100_000L;
            energy.setCharge(stackLocation, initialCharge, stack.data());

            check(io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem.getByItem(
                    SameItemJudge.itemFromBase64(fragmentMeta.getPersistentDataContainer().get(
                            new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json"),
                            PersistentDataType.STRING))) instanceof io.Yomicer.magicExpansion.items.generators.FishOutputMachine,
                    "Fragment fixture did not decode into a registered normal vivarium");
            check(com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils.getSlimefunItem(targetLocation) == targetItem,
                    "Bound storage fixture is not visible to the native block cache");
            check(io.Yomicer.magicExpansion.items.generators.FishOutputMachine.FISH_OUTPUT_MAP.containsKey("CopperDustFish"),
                    "Fixture fish is missing from the actual output map");

            // Capacity 100 with 90 already stored cannot accept the huge batch.
            stackItem.getBlockTicker().tick(stack.block(), stackItem, stack.data());
            check("90".equals(target.data().getData("item_count_0")),
                    "Small Cargo Core destination partially accepted stacked-vivarium output");
            check(energy.getChargeLong(stackLocation, stack.data()) == initialCharge,
                    "Failed exact deposit consumed energy");

            // Unlimited storage must receive the complete long batch in one transaction.
            target.data().setData("item_max_0", "-1");
            stackItem.getBlockTicker().tick(stack.block(), stackItem, stack.data());

            long stored = Long.parseLong(target.data().getData("item_count_0"));
            check(stored == 90L + EXPECTED_BATCH,
                    "Unlimited Cargo Core stored " + stored + " instead of " + (90L + EXPECTED_BATCH)
                            + "; charge=" + energy.getChargeLong(stackLocation, stack.data()));
            check(stored > Integer.MAX_VALUE,
                    "Stacked vivarium output was truncated to the legacy int range");
            check(energy.getChargeLong(stackLocation, stack.data())
                            == initialCharge - FishOutputMachineStack.ENERGY_CONSUMPTION,
                    "Successful exact deposit did not consume exactly one machine energy cost");
        } finally {
            cleanup(stackLocation);
            cleanup(targetLocation);
        }
    }

    private Fixture createFixture(SlimefunItem item, Location location) {
        World world = location.getWorld();
        check(world != null, "Fixture world missing");
        Chunk chunk = world.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        chunk.addPluginChunkTicket(this);
        heldChunks.add(chunk);

        BlockDataController blocks = Slimefun.getDatabaseManager().getBlockDataController();
        blocks.getChunkData(chunk);
        Block block = location.getBlock();
        Material material = item.getItem().getType();
        check(material.isBlock(), item.getId() + " is not placeable");
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
            getLogger().log(Level.WARNING, "Stacked vivarium fixture cleanup failed at " + location, error);
        }
    }

    private void publish(String text) {
        try {
            Files.writeString(Path.of("stacked-vivarium-result.txt"), text);
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Could not save stacked vivarium evidence", error);
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
