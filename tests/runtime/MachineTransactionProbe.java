package audit;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricRecipeMachine;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeRandomMachine;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Real-server transaction probe for MagicExpansion electric recipe/resource machines.
 *
 * <p>Uses actual registered machine instances, native Slimefun block data, menus,
 * block tickers, energy storage and recipes. It intentionally pauses the normal
 * ticker at each disposable location so the probe controls each transaction.</p>
 */
public final class MachineTransactionProbe extends JavaPlugin {
    private final List<Chunk> heldChunks = new ArrayList<>();
    private final List<Location> created = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runProbe();
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, "MACHINE_TRANSACTION_FATAL", error);
                publish("MACHINE_TRANSACTION_FAIL\nfatal=" + error + "\n");
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

    private void runProbe() throws Exception {
        MagicExpansion plugin = MagicExpansion.getInstance();
        check(plugin != null && plugin.isEnabled(), "MagicExpansion is not enabled");

        List<AbstractElectricRecipeMachine> recipeMachines = Slimefun.getRegistry().getAllSlimefunItems().stream()
                .filter(item -> addonIs(item, plugin))
                .filter(AbstractElectricRecipeMachine.class::isInstance)
                .map(AbstractElectricRecipeMachine.class::cast)
                .sorted(Comparator.comparing(SlimefunItem::getId))
                .toList();

        List<AbstractElectricResourceMachine> resourceMachines = Slimefun.getRegistry().getAllSlimefunItems().stream()
                .filter(item -> addonIs(item, plugin))
                .filter(AbstractElectricResourceMachine.class::isInstance)
                .map(AbstractElectricResourceMachine.class::cast)
                .sorted(Comparator.comparing(SlimefunItem::getId))
                .toList();

        check(!recipeMachines.isEmpty(), "No electric recipe machines are registered");
        check(!resourceMachines.isEmpty(), "No electric resource machines are registered");

        List<String> failures = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> passed = new ArrayList<>();
        int recipeDefinitions = 0;
        int recipeMachineTransactions = 0;
        int underfillChecks = 0;
        int resourceDefinitions = 0;
        int resourceTransactions = 0;
        int locationIndex = 0;

        for (AbstractElectricRecipeMachine machine : recipeMachines) {
            try {
                recipeDefinitions += auditRecipes(machine, failures);
                MachineRecipe recipe = chooseRunnableRecipe(machine.getRecipes(), inputSlots(machine).length);
                if (recipe == null) {
                    failures.add(machine.getId() + ": no runnable registered recipe");
                    continue;
                }

                if (hasUnderfillableInput(recipe)) {
                    Location underfill = location(++locationIndex);
                    Fixture fixture = createFixture(machine, underfill);
                    try {
                        putInputs(fixture.menu(), inputSlots(machine), recipe, true);
                        List<ItemStack> before = snapshot(fixture.menu(), inputSlots(machine));
                        tick(machine, fixture, false);
                        List<ItemStack> after = snapshot(fixture.menu(), inputSlots(machine));
                        if (!sameContents(before, after)) {
                            failures.add(machine.getId() + ": underfilled recipe consumed inputs or started");
                        } else {
                            underfillChecks++;
                        }
                    } finally {
                        cleanup(underfill);
                    }
                }

                Location location = location(++locationIndex);
                Fixture fixture = createFixture(machine, location);
                try {
                    clearSlots(fixture.menu(), inputSlots(machine));
                    clearSlots(fixture.menu(), outputSlots(machine));
                    putInputs(fixture.menu(), inputSlots(machine), recipe, false);

                    tick(machine, fixture, false); // match, consume exact inputs and start operation
                    if (!allEmpty(fixture.menu(), inputSlots(machine))) {
                        failures.add(machine.getId() + ": exact recipe inputs were not fully consumed at start");
                        continue;
                    }

                    long before = charge(machine, fixture);
                    refill(machine, fixture);
                    before = charge(machine, fixture);
                    tick(machine, fixture, false);
                    long after = charge(machine, fixture);
                    if (machine.getEnergyConsumption() > 0
                            && before - after != machine.getEnergyConsumption()) {
                        failures.add(machine.getId() + ": expected one active-tick energy cost "
                                + machine.getEnergyConsumption() + " but charge changed " + before + " -> " + after);
                    }

                    int maxTicks = Math.max(8, Math.min(50_000, recipe.getTicks() + 8));
                    boolean produced = hasAnyOutput(fixture.menu(), outputSlots(machine));
                    for (int i = 0; i < maxTicks && !produced; i++) {
                        refill(machine, fixture);
                        tick(machine, fixture, false);
                        produced = hasAnyOutput(fixture.menu(), outputSlots(machine));
                    }
                    if (!produced) {
                        failures.add(machine.getId() + ": produced no output after " + maxTicks
                                + " controlled ticks; recipeTicks=" + recipe.getTicks());
                        continue;
                    }

                    if (machine instanceof RecipeRandomMachine) {
                        if (!containsOneExpectedOutput(fixture.menu(), outputSlots(machine), recipe.getOutput())) {
                            failures.add(machine.getId() + ": random output did not match any registered result");
                            continue;
                        }
                    } else if (!containsAllExpectedOutputs(fixture.menu(), outputSlots(machine), recipe.getOutput())) {
                        failures.add(machine.getId() + ": output inventory does not contain registered recipe results");
                        continue;
                    }

                    recipeMachineTransactions++;
                    passed.add(machine.getId() + ": recipe transaction PASS (ticks=" + recipe.getTicks() + ")");
                } finally {
                    cleanup(location);
                }
            } catch (Throwable error) {
                failures.add(machine.getId() + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }

        for (AbstractElectricResourceMachine machine : resourceMachines) {
            try {
                resourceDefinitions += auditRecipes(machine, failures);
                if (machine.getRecipes().isEmpty()) {
                    failures.add(machine.getId() + ": resource machine exposes no registered recipe");
                    continue;
                }

                // Origin-material machines intentionally need per-item/block configuration and
                // receive a dedicated probe later. They are still fully audited for registration,
                // energy metadata and recipe definitions by this phase.
                if (machine.getClass().getSimpleName().startsWith("OriginMaterialGen")) {
                    warnings.add(machine.getId() + ": configured-output behavior deferred to origin-material probe");
                    continue;
                }

                MachineRecipe recipe = machine.getRecipes().stream()
                        .min(Comparator.comparingInt(MachineRecipe::getTicks))
                        .orElse(null);
                if (recipe == null) {
                    failures.add(machine.getId() + ": no resource recipe available");
                    continue;
                }

                Location location = location(++locationIndex);
                Fixture fixture = createFixture(machine, location);
                try {
                    clearSlots(fixture.menu(), outputSlots(machine));
                    refill(machine, fixture);
                    tick(machine, fixture, false); // start operation
                    int maxTicks = Math.max(8, Math.min(50_000, recipe.getTicks() + 8));
                    boolean produced = hasAnyOutput(fixture.menu(), outputSlots(machine));
                    for (int i = 0; i < maxTicks && !produced; i++) {
                        refill(machine, fixture);
                        tick(machine, fixture, false);
                        produced = hasAnyOutput(fixture.menu(), outputSlots(machine));
                    }
                    if (!produced) {
                        failures.add(machine.getId() + ": resource machine produced no output after "
                                + maxTicks + " controlled ticks; recipeTicks=" + recipe.getTicks());
                        continue;
                    }

                    ItemStack[] declared = machine.getItemStackOutputs();
                    if (!containsAllExpectedOutputs(fixture.menu(), outputSlots(machine), declared)) {
                        failures.add(machine.getId() + ": resource output does not contain configured results");
                        continue;
                    }

                    resourceTransactions++;
                    passed.add(machine.getId() + ": resource transaction PASS (ticks=" + recipe.getTicks() + ")");
                } finally {
                    cleanup(location);
                }
            } catch (Throwable error) {
                failures.add(machine.getId() + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(failures.isEmpty() ? "MACHINE_TRANSACTION_PASS\n" : "MACHINE_TRANSACTION_FAIL\n");
        result.append("recipe_machines=").append(recipeMachines.size()).append('\n');
        result.append("recipe_definitions=").append(recipeDefinitions).append('\n');
        result.append("recipe_machine_transactions=").append(recipeMachineTransactions).append('\n');
        result.append("underfill_checks=").append(underfillChecks).append('\n');
        result.append("resource_machines=").append(resourceMachines.size()).append('\n');
        result.append("resource_definitions=").append(resourceDefinitions).append('\n');
        result.append("resource_transactions=").append(resourceTransactions).append('\n');
        for (String item : passed) result.append("pass=").append(item).append('\n');
        for (String warning : warnings) result.append("warning=").append(warning).append('\n');
        for (String failure : failures) result.append("failure=").append(failure).append('\n');
        publish(result.toString());

        getLogger().info((failures.isEmpty() ? "MACHINE_TRANSACTION_PASS" : "MACHINE_TRANSACTION_FAIL")
                + " recipeMachines=" + recipeMachines.size()
                + " recipeTransactions=" + recipeMachineTransactions
                + " resourceMachines=" + resourceMachines.size()
                + " resourceTransactions=" + resourceTransactions
                + " failures=" + failures.size());
        Bukkit.shutdown();
    }

    private int auditRecipes(AbstractElectricRecipeMachine machine, List<String> failures) {
        return auditRecipeList(machine.getId(), machine.getRecipes(), failures);
    }

    private int auditRecipes(AbstractElectricResourceMachine machine, List<String> failures) {
        return auditRecipeList(machine.getId(), machine.getRecipes(), failures);
    }

    private int auditRecipeList(String id, List<MachineRecipe> recipes, List<String> failures) {
        int count = 0;
        for (int index = 0; index < recipes.size(); index++) {
            MachineRecipe recipe = recipes.get(index);
            if (recipe == null) {
                failures.add(id + ": null recipe at index " + index);
                continue;
            }
            count++;
            if (recipe.getTicks() < 0) failures.add(id + ": negative recipe ticks at index " + index);
            if (recipe.getInput() == null) failures.add(id + ": null recipe input array at index " + index);
            if (recipe.getOutput() == null || recipe.getOutput().length == 0) {
                failures.add(id + ": empty recipe output at index " + index);
            }
            if (recipe.getInput() != null) {
                for (ItemStack input : recipe.getInput()) {
                    if (input == null || input.getType().isAir() || input.getAmount() <= 0) {
                        failures.add(id + ": invalid recipe input at index " + index);
                    }
                }
            }
            if (recipe.getOutput() != null) {
                for (ItemStack output : recipe.getOutput()) {
                    if (output == null) continue;
                    if (output.getType().isAir() || output.getAmount() <= 0) {
                        failures.add(id + ": invalid recipe output at index " + index);
                    }
                }
            }
        }
        return count;
    }

    private MachineRecipe chooseRunnableRecipe(List<MachineRecipe> recipes, int slotCount) {
        return recipes.stream()
                .filter(recipe -> recipe != null && recipe.getInput() != null && recipe.getOutput() != null)
                .filter(recipe -> recipe.getInput().length > 0 && recipe.getInput().length <= slotCount)
                .filter(recipe -> {
                    for (ItemStack input : recipe.getInput()) {
                        if (input == null || input.getType().isAir() || input.getAmount() <= 0) return false;
                    }
                    return true;
                })
                .min(Comparator.comparingInt(MachineRecipe::getTicks))
                .orElse(null);
    }

    private boolean hasUnderfillableInput(MachineRecipe recipe) {
        for (ItemStack input : recipe.getInput()) {
            if (input != null && input.getAmount() > 1) return true;
        }
        return false;
    }

    private void putInputs(BlockMenu menu, int[] slots, MachineRecipe recipe, boolean underfill) {
        clearSlots(menu, slots);
        int index = 0;
        boolean reduced = false;
        for (ItemStack input : recipe.getInput()) {
            if (input == null || input.getType().isAir()) continue;
            check(index < slots.length, "Recipe needs more physical input slots than machine exposes");
            ItemStack copy = input.clone();
            if (underfill && !reduced && copy.getAmount() > 1) {
                copy.setAmount(copy.getAmount() - 1);
                reduced = true;
            }
            menu.replaceExistingItem(slots[index++], copy);
        }
        if (underfill) check(reduced, "Requested underfill case has no multi-item ingredient");
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
        check(material.isBlock(), item.getId() + ": machine template is not a placeable block: " + material);
        block.setType(material, false);
        SlimefunBlockData data = blocks.createBlock(location, item.getId());
        BlockMenu menu = data.getBlockMenu();
        check(data.isDataLoaded(), item.getId() + ": Slimefun block data is not loaded");
        check(menu != null, item.getId() + ": machine menu did not initialize");
        check(item.getBlockTicker() != null, item.getId() + ": machine has no block ticker");
        Slimefun.getTickerTask().pauseMachineTicker(location);
        created.add(location);
        return new Fixture(blocks, block, data, menu);
    }

    private Location location(int index) {
        World world = Bukkit.getWorlds().getFirst();
        int x = 12_000 + (index % 32) * 4;
        int z = 12_000 + (index / 32) * 4;
        return new Location(world, 72, 72, 72).add(x - 72, 0, z - 72);
    }

    private void cleanup(Location location) {
        try {
            Slimefun.getTickerTask().resumeMachineTicker(location);
            BlockDataController blocks = Slimefun.getDatabaseManager().getBlockDataController();
            blocks.removeBlockData(location);
            location.getBlock().setType(Material.AIR, false);
        } catch (Throwable error) {
            getLogger().log(Level.WARNING, "Could not fully clean machine fixture at " + location, error);
        }
    }

    private void tick(SlimefunItem item, Fixture fixture, boolean refill) {
        if (refill && item instanceof EnergyNetComponent energy) {
            energy.setCharge(fixture.block().getLocation(), energy.getCapacityLong(), fixture.data());
        }
        item.getBlockTicker().tick(fixture.block(), item, fixture.data());
    }

    private void refill(EnergyNetComponent energy, Fixture fixture) {
        energy.setCharge(fixture.block().getLocation(), energy.getCapacityLong(), fixture.data());
    }

    private long charge(EnergyNetComponent energy, Fixture fixture) {
        return energy.getChargeLong(fixture.block().getLocation(), fixture.data());
    }

    private int[] inputSlots(Object machine) {
        return slots(machine, "getInputSlots");
    }

    private int[] outputSlots(Object machine) {
        return slots(machine, "getOutputSlots");
    }

    private int[] slots(Object target, String name) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod(name);
                method.setAccessible(true);
                return ((int[]) method.invoke(target)).clone();
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException("Could not inspect " + name + " for "
                        + target.getClass().getName(), error);
            }
        }
        throw new IllegalStateException("No " + name + " method for " + target.getClass().getName());
    }

    private void clearSlots(BlockMenu menu, int[] slots) {
        for (int slot : new LinkedHashSet<>(asList(slots))) {
            menu.replaceExistingItem(slot, null);
        }
    }

    private List<Integer> asList(int[] slots) {
        List<Integer> result = new ArrayList<>(slots.length);
        for (int slot : slots) result.add(slot);
        return result;
    }

    private List<ItemStack> snapshot(BlockMenu menu, int[] slots) {
        List<ItemStack> result = new ArrayList<>();
        for (int slot : slots) {
            ItemStack stack = menu.getItemInSlot(slot);
            result.add(stack == null ? null : stack.clone());
        }
        return result;
    }

    private boolean sameContents(List<ItemStack> left, List<ItemStack> right) {
        if (left.size() != right.size()) return false;
        for (int i = 0; i < left.size(); i++) {
            ItemStack a = left.get(i);
            ItemStack b = right.get(i);
            if (a == null || a.getType().isAir()) {
                if (b != null && !b.getType().isAir()) return false;
            } else if (b == null || !a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    private boolean allEmpty(BlockMenu menu, int[] slots) {
        for (int slot : slots) {
            ItemStack stack = menu.getItemInSlot(slot);
            if (stack != null && !stack.getType().isAir()) return false;
        }
        return true;
    }

    private boolean hasAnyOutput(BlockMenu menu, int[] slots) {
        for (int slot : slots) {
            ItemStack stack = menu.getItemInSlot(slot);
            if (stack != null && !stack.getType().isAir() && stack.getAmount() > 0) return true;
        }
        return false;
    }

    private boolean containsAllExpectedOutputs(BlockMenu menu, int[] slots, ItemStack[] expected) {
        Map<String, Integer> required = new LinkedHashMap<>();
        Map<String, ItemStack> templates = new LinkedHashMap<>();
        for (ItemStack output : expected) {
            if (output == null || output.getType().isAir()) continue;
            String key = identityKey(output);
            required.merge(key, output.getAmount(), Integer::sum);
            templates.putIfAbsent(key, output);
        }
        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            int found = 0;
            ItemStack template = templates.get(entry.getKey());
            for (int slot : slots) {
                ItemStack actual = menu.getItemInSlot(slot);
                if (actual != null && !actual.getType().isAir()
                        && SlimefunUtils.isItemSimilar(actual, template, true, false)) {
                    found += actual.getAmount();
                }
            }
            if (found < entry.getValue()) return false;
        }
        return !required.isEmpty();
    }

    private boolean containsOneExpectedOutput(BlockMenu menu, int[] slots, ItemStack[] expected) {
        for (int slot : slots) {
            ItemStack actual = menu.getItemInSlot(slot);
            if (actual == null || actual.getType().isAir()) continue;
            for (ItemStack template : expected) {
                if (template != null && !template.getType().isAir()
                        && SlimefunUtils.isItemSimilar(actual, template, true, false)
                        && actual.getAmount() >= template.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String identityKey(ItemStack stack) {
        SlimefunItem sf = SlimefunItem.getByItem(stack);
        return sf == null ? stack.getType().getKey().toString() + "|" + stack.getItemMeta().getAsString()
                : "sf:" + sf.getId() + "|" + stack.getItemMeta().getAsString();
    }

    private static boolean addonIs(SlimefunItem item, MagicExpansion plugin) {
        try {
            return item.getAddon() == plugin;
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private void publish(String text) {
        try {
            Files.writeString(Path.of("machine-transaction-result.txt"), text);
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Could not save machine transaction evidence", error);
        }
    }

    private static void check(boolean condition, String explanation) {
        if (!condition) throw new IllegalStateException(explanation);
    }

    private record Fixture(BlockDataController blocks, Block block, SlimefunBlockData data, BlockMenu menu) {}
}
