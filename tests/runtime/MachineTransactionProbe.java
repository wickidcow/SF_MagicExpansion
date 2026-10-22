package audit;

import static io.Yomicer.magicExpansion.utils.itemUtils.newItem.idDecorator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.IDCardMachineCN;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeMachineCrude;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeRandomMachineDefault;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.OriginMaterialGenMaker;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.OriginMaterialGenMakerLite;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.OriginMaterialGenMakerUltra;
import io.Yomicer.magicExpansion.items.electric.templateMachine.TemplateMachine;
import io.Yomicer.magicExpansion.items.electric.templateMachine.ItemOriginBackTrackMachine;
import io.Yomicer.magicExpansion.items.electric.resourceGenerator.ResourceMachine;
import io.Yomicer.magicExpansion.items.electric.resourceGenerator.ResourceRandomOneMachine;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricRecipeMachine;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RandomBoxMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeRandomMachine;
import io.Yomicer.magicExpansion.items.misc.CHEST_BLOCK;
import io.Yomicer.magicExpansion.items.electric.resourceGenerator.OriginMaterialGen;
import io.Yomicer.magicExpansion.items.electric.resourceGenerator.OriginMaterialGenStack;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.lang.reflect.Field;
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
                requestShutdown();
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
        int resourceRecipeGuardChecks = 0;
        int specialUntimedTransactions = 0;
        int locationIndex = 0;

        for (AbstractElectricRecipeMachine machine : recipeMachines) {
            try {
                if (machine instanceof RandomBoxMachine randomBox) {
                    auditRandomBoxMachine(randomBox, ++locationIndex, passed, failures);
                    specialUntimedTransactions++;
                    continue;
                }
                if (machine instanceof CHEST_BLOCK chest) {
                    auditStorageChest(chest, ++locationIndex, passed, failures);
                    specialUntimedTransactions++;
                    continue;
                }

                recipeDefinitions += auditRecipes(machine, failures);
                if (machine instanceof ItemOriginBackTrackMachine backtrack) {
                    auditBacktrack(backtrack, ++locationIndex, passed);
                    recipeMachineTransactions++;
                    continue;
                }
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
                        configureOriginMaker(machine, fixture);
                        refill(machine, fixture);
                        List<ItemStack> before = snapshot(fixture.menu(), inputSlots(machine));
                        tick(machine, fixture, false);
                        List<ItemStack> after = snapshot(fixture.menu(), inputSlots(machine));
                        if (!sameContents(before, after)
                                || machine.getMachineProcessor().getOperation(fixture.block()) != null) {
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
                    configureOriginMaker(machine, fixture);
                    if (machine instanceof RecipeMachineCrude) {
                        var randomField = RecipeMachineCrude.class.getDeclaredField("random");
                        randomField.setAccessible(true);
                        // Exercise a known successful random draw in this isolated instance.
                        // The configured success probabilities and gameplay rates are untouched.
                        ((java.util.Random) randomField.get(machine)).setSeed(0L);
                    }
                    refill(machine, fixture);

                    tick(machine, fixture, false); // match, consume exact inputs and start operation
                    CraftingOperation operation = machine.getMachineProcessor().getOperation(fixture.block());
                    check(operation != null, machine.getId() + ": exact configured inputs did not start an operation");
                    if (machine instanceof TemplateMachine) {
                        check(containsOnlyExpectedOutputs(fixture.menu(), inputSlots(machine),
                                        new ItemStack[] {recipe.getInput()[0]}),
                                machine.getId() + ": reusable template or consumed ingredients changed");
                    } else {
                        check(allEmpty(fixture.menu(), inputSlots(machine)),
                                machine.getId() + ": exact recipe inputs were not fully consumed at start");
                    }
                    if (isOriginMaker(machine)) {
                        check(fixture.menu().getItemInSlot(4) == null
                                        || fixture.menu().getItemInSlot(4).getType().isAir(),
                                machine.getId() + ": configured origin material was not consumed exactly once");
                        validateOriginMaker(machine, operation.getResults(), recipe.getOutput());
                    }
                    ItemStack[] actualRecipeOutput = isOriginMaker(machine)
                            ? operation.getResults() : recipe.getOutput();

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

                    if (machine instanceof RecipeRandomMachine || machine instanceof RecipeRandomMachineDefault) {
                        if (!containsOneExpectedOutput(fixture.menu(), outputSlots(machine), recipe.getOutput())) {
                            failures.add(machine.getId() + ": random output did not match any registered result");
                            continue;
                        }
                    } else if (machine instanceof IDCardMachineCN) {
                        validateSymbolOutput(fixture.menu(), outputSlots(machine));
                    } else if (!containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine), actualRecipeOutput)) {
                        failures.add(machine.getId() + ": output inventory does not contain registered recipe results");
                        continue;
                    }

                    recipeMachineTransactions++;
                    passed.add(machine.getId() + ": recipe transaction PASS (ticks=" + recipe.getTicks() + ")");
                } finally {
                    cleanup(location);
                }
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, machine.getId() + ": native transaction failed", error);
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
                if (machine instanceof OriginMaterialGenStack stacked) {
                    auditOriginMaterialStack(stacked, ++locationIndex, passed);
                    resourceTransactions++;
                    continue;
                }
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
                    if (machine instanceof ResourceMachine || machine instanceof ResourceRandomOneMachine) {
                        resourceRecipeGuardChecks += auditUnavailableResourceRecipes(machine, fixture, passed);
                    }
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
                    if (machine instanceof ResourceRandomOneMachine && declared.length == 1
                            && declared[0] != null && declared[0].getAmount()
                            > outputSlots(machine).length * declared[0].getMaxStackSize()) {
                        auditOversizedResourceBatch(machine, fixture, declared[0]);
                        resourceTransactions++;
                        passed.add(machine.getId() + ": 64-item batch conserves 36 stored and 28 pending without double charge");
                        continue;
                    }
                    boolean exactOutput = machine instanceof ResourceRandomOneMachine
                            ? containsOneExpectedOutput(fixture.menu(), outputSlots(machine), declared)
                            : containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine), declared);
                    if (!exactOutput) {
                        failures.add(machine.getId() + ": resource output does not contain configured results");
                        continue;
                    }

                    resourceTransactions++;
                    passed.add(machine.getId() + ": resource transaction PASS (ticks=" + recipe.getTicks() + ")");
                } finally {
                    cleanup(location);
                }
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, machine.getId() + ": native transaction failed", error);
                failures.add(machine.getId() + ": " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }

        try {
            auditOriginMaterialPending(++locationIndex, passed, failures);
        } catch (Throwable error) {
            failures.add("ORIGIN_MATERIAL_GEN: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
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
        result.append("resource_recipe_guard_checks=").append(resourceRecipeGuardChecks).append('\n');
        result.append("special_untimed_transactions=").append(specialUntimedTransactions).append('\n');
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
        requestShutdown();
    }

    private int auditUnavailableResourceRecipes(
            AbstractElectricResourceMachine machine, Fixture fixture, List<String> passed)
            throws ReflectiveOperationException {
        List<MachineRecipe> originalRecipes = machine.getRecipes();
        check(originalRecipes != null && !originalRecipes.isEmpty(),
                machine.getId() + ": recipe-guard control has no registered recipe");
        check(machine.getMachineProcessor().getOperation(fixture.block()) == null,
                machine.getId() + ": recipe-guard fixture already has an operation");

        int[] allSlots = java.util.stream.IntStream.range(0, fixture.menu().toInventory().getSize()).toArray();
        List<ItemStack> inventoryBefore = snapshot(fixture.menu(), allSlots);
        long chargeBefore = charge(machine, fixture);
        Field recipeField = AbstractElectricResourceMachine.class.getDeclaredField("recipes");
        recipeField.setAccessible(true);
        try {
            for (boolean missing : new boolean[] { false, true }) {
                recipeField.set(machine, missing ? null : new ArrayList<MachineRecipe>());
                String state = missing ? "null" : "empty";
                for (int tick = 0; tick < 4; tick++) {
                    tick(machine, fixture, false);
                    check(machine.getMachineProcessor().getOperation(fixture.block()) == null,
                            machine.getId() + ": " + state + " recipes started an operation");
                    check(charge(machine, fixture) == chargeBefore,
                            machine.getId() + ": " + state + " recipes consumed energy");
                    check(sameContents(inventoryBefore, snapshot(fixture.menu(), allSlots)),
                            machine.getId() + ": " + state + " recipes changed the inventory");
                }
                passed.add(machine.getId() + ": " + state
                        + " recipe list idles for four ticks without changing inventory or energy");
            }
        } finally {
            // Restore the exact registered list even if a ticker or assertion failed.
            // The existing normal-output transaction then checks recovery with real recipes.
            recipeField.set(machine, originalRecipes);
        }
        check(machine.findNextRecipe(fixture.menu()) == originalRecipes.get(0),
                machine.getId() + ": restoring recipes did not restore the original selection");
        return 2;
    }

    private void auditRandomBoxMachine(
            RandomBoxMachine machine,
            int locationIndex,
            List<String> passed,
            List<String> failures) {
        Location location = location(locationIndex);
        Fixture fixture = createFixture(machine, location);
        int[] inputs = inputSlots(machine);
        int[] outputs = outputSlots(machine);

        try {
            clearSlots(fixture.menu(), inputs);
            clearSlots(fixture.menu(), outputs);

            SlimefunItem boxItem = SlimefunItem.getById(idDecorator("HONKAI_STAR_RAIL_BOX"));
            check(boxItem != null, "HONKAI_STAR_RAIL_BOX registration missing");
            ItemStack box = boxItem.getItem().clone();
            box.setAmount(1);
            fixture.menu().replaceExistingItem(inputs[0], box);

            tick(machine, fixture, false);

            ItemStack remaining = fixture.menu().getItemInSlot(inputs[0]);
            check(remaining == null || remaining.getType().isAir(),
                    "Honkai scanner did not consume exactly one box");

            // The finalized group is the live reward pool. It includes the box
            // itself as well as the six character results shown in display recipes.
            Map<String, SlimefunItem> rewardPool =
                    io.Yomicer.magicExpansion.Listener.SlimefunRegistryGiftBox.itemMapMihoyoHonkai;
            Set<String> expectedPoolIds = new LinkedHashSet<>();
            expectedPoolIds.add(boxItem.getId());
            for (MachineRecipe recipe : machine.getRecipes()) {
                if (recipe == null || recipe.getOutput() == null) continue;
                for (ItemStack output : recipe.getOutput()) {
                    if (output == null || output.getType().isAir()) continue;
                    String id = Slimefun.getItemDataService().getItemData(output).orElseThrow();
                    check(rewardPool.containsKey(id), "Displayed scanner reward is absent from its live pool: " + id);
                    expectedPoolIds.add(id);
                }
            }
            check(rewardPool.keySet().equals(expectedPoolIds),
                    "Scanner reward pool differs from its displayed results plus the registered box");
            ItemStack[] expectedRewards = rewardPool.values().stream()
                    .map(SlimefunItem::getItem).toArray(ItemStack[]::new);
            check(expectedRewards.length > 0, "Honkai scanner exposes no registered reward templates");
            check(containsOneExpectedOutput(fixture.menu(), outputs, expectedRewards),
                    "Honkai scanner output did not match any finalized registered reward");

            // Backpressure must never consume another box.
            clearSlots(fixture.menu(), inputs);
            clearSlots(fixture.menu(), outputs);
            for (int slot : outputs) {
                fixture.menu().replaceExistingItem(slot, new ItemStack(Material.COBBLESTONE, 64));
            }
            fixture.menu().replaceExistingItem(inputs[0], box.clone());
            tick(machine, fixture, false);

            ItemStack blockedBox = fixture.menu().getItemInSlot(inputs[0]);
            check(blockedBox != null
                            && SlimefunUtils.isItemSimilar(blockedBox, box, true, false)
                            && blockedBox.getAmount() == 1,
                    "Honkai scanner consumed a box while every output slot was blocked");

            passed.add(machine.getId()
                    + ": untimed random box transaction conserves input under backpressure");
        } finally {
            cleanup(location);
        }
    }

    private void auditStorageChest(
            CHEST_BLOCK chest,
            int locationIndex,
            List<String> passed,
            List<String> failures) {
        Location location = location(locationIndex);
        Fixture fixture = createFixture(chest, location);
        int[] inputs = inputSlots(chest);
        int[] outputs = outputSlots(chest);

        try {
            check(inputs.length == 53, "53-slot chest exposes " + inputs.length + " input slots");
            check(outputs.length == 53, "53-slot chest exposes " + outputs.length + " output slots");

            clearSlots(fixture.menu(), inputs);
            ItemStack first = new ItemStack(Material.DIAMOND, 17);
            ItemStack last = new ItemStack(Material.EMERALD, 23);
            fixture.menu().replaceExistingItem(inputs[0], first.clone());
            fixture.menu().replaceExistingItem(inputs[inputs.length - 1], last.clone());

            for (int i = 0; i < 8; i++) {
                tick(chest, fixture, false);
            }

            ItemStack firstAfter = fixture.menu().getItemInSlot(inputs[0]);
            ItemStack lastAfter = fixture.menu().getItemInSlot(inputs[inputs.length - 1]);
            check(first.equals(firstAfter),
                    "53-slot chest mutated first storage slot during inert ticks");
            check(last.equals(lastAfter),
                    "53-slot chest mutated final storage slot during inert ticks");

            passed.add(chest.getId() + ": all 53 storage slots remain inert and persistent");
        } finally {
            cleanup(location);
        }
    }

    private void auditOriginMaterialPending(
            int locationIndex, List<String> passed, List<String> failures) {
        SlimefunItem item = SlimefunItem.getById(idDecorator("ORIGIN_MATERIAL_GEN"));
        check(item instanceof OriginMaterialGen,
                "ORIGIN_MATERIAL_GEN is not OriginMaterialGen");
        OriginMaterialGen machine = (OriginMaterialGen) item;

        Location location = location(locationIndex);
        Fixture fixture = createFixture(machine, location);
        try {
            clearSlots(fixture.menu(), outputSlots(machine));
            fixture.data().setData("origin_material", Material.DIAMOND.name());
            fixture.data().removeData("origin_pending_output");

            refill(machine, fixture);
            tick(machine, fixture, false); // start operation
            finishOperation(machine, fixture); // complete exactly one operation and credit 3,564

            int firstStored = countMaterial(
                    fixture.menu(), outputSlots(machine), Material.DIAMOND);
            String pendingRaw = fixture.data().getData("origin_pending_output");
            long pending = pendingRaw == null ? 0L : Long.parseLong(pendingRaw);

            check(firstStored == 2_304,
                    "ORIGIN_MATERIAL_GEN expected 2304 physically stored items, got "
                            + firstStored);
            check(pending == 1_260L,
                    "ORIGIN_MATERIAL_GEN expected 1260 pending items, got " + pending);
            check((long) firstStored + pending == 3_564L,
                    "ORIGIN_MATERIAL_GEN did not conserve the advertised 3564-item batch");

            clearSlots(fixture.menu(), outputSlots(machine));
            refill(machine, fixture);
            tick(machine, fixture, false); // flush pending, then only start the next cycle

            int flushed = countMaterial(
                    fixture.menu(), outputSlots(machine), Material.DIAMOND);
            check(flushed == 1_260,
                    "ORIGIN_MATERIAL_GEN expected exact 1260-item pending flush, got "
                            + flushed);
            check(fixture.data().getData("origin_pending_output") == null,
                    "ORIGIN_MATERIAL_GEN pending marker remained after complete flush");

            passed.add("ORIGIN_MATERIAL_GEN: 3564-item output conserved through backpressure");
        } finally {
            cleanup(location);
        }
    }

    private void auditOversizedResourceBatch(
            AbstractElectricResourceMachine machine, Fixture fixture, ItemStack declared) {
        int capacity = outputSlots(machine).length
                * Math.min(declared.getMaxStackSize(), fixture.menu().toInventory().getMaxStackSize());
        check(capacity == 36 && declared.getAmount() == 64,
                "Guide batch fixture no longer describes the configured 36-slot/64-item transaction");
        ItemStack first = ItemStack.deserialize(declared.serialize());
        first.setAmount(capacity);
        check(containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine), new ItemStack[] {first}),
                "Oversized resource batch did not store exactly 36 matching guides first");
        refill(machine, fixture);
        long before = charge(machine, fixture);
        tick(machine, fixture, false);
        check(charge(machine, fixture) == before
                        && containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine), new ItemStack[] {first})
                        && machine.getMachineProcessor().getOperation(fixture.block()) == null,
                "Blocked oversized resource batch charged energy, changed output, or started another cycle");
        clearSlots(fixture.menu(), outputSlots(machine));
        tick(machine, fixture, false);
        ItemStack remaining = ItemStack.deserialize(declared.serialize());
        remaining.setAmount(declared.getAmount() - capacity);
        check(containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine), new ItemStack[] {remaining}),
                "Oversized resource batch lost or duplicated its exact 28 pending guides");
        check(charge(machine, fixture) == before
                        && machine.getMachineProcessor().getOperation(fixture.block()) == null,
                "Pending guide flush charged energy or started another cycle");
    }

    private void finishOperation(AbstractElectricResourceMachine machine, Fixture fixture) {
        CraftingOperation operation = machine.getMachineProcessor().getOperation(fixture.block());
        check(operation != null, machine.getId() + ": configured generator did not start");
        for (int tick = 0; tick < operation.getTotalTicks() + 2
                && machine.getMachineProcessor().getOperation(fixture.block()) != null; tick++) {
            refill(machine, fixture);
            tick(machine, fixture, false);
        }
        check(machine.getMachineProcessor().getOperation(fixture.block()) == null,
                machine.getId() + ": configured generator did not finish its operation");
    }

    private void auditOriginMaterialStack(OriginMaterialGenStack machine, int index, List<String> passed) {
        Location location = location(index);
        Fixture fixture = createFixture(machine, location);
        try {
            SlimefunItem childType = SlimefunItem.getById(idDecorator("ORIGIN_MATERIAL_GEN_LITE"));
            check(childType != null, "Stacked origin generator child is not registered");
            ItemStack child = ItemStack.deserialize(childType.getItem().serialize());
            child.setAmount(64);
            var meta = child.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(MagicExpansion.getInstance(),
                    "origin_material"), PersistentDataType.STRING, Material.DIAMOND.name());
            child.setItemMeta(meta);
            clearSlots(fixture.menu(), inputSlots(machine));
            fixture.menu().replaceExistingItem(inputSlots(machine)[0], child);
            fixture.data().setData("machine_random_multiplier", "1.5");
            for (int slot : outputSlots(machine)) {
                fixture.menu().replaceExistingItem(slot, new ItemStack(Material.STONE, 64));
            }
            fixture.menu().replaceExistingItem(outputSlots(machine)[0], new ItemStack(Material.DIAMOND, 32));
            refill(machine, fixture);
            tick(machine, fixture, false);
            finishOperation(machine, fixture);
            check(countMaterial(fixture.menu(), outputSlots(machine), Material.DIAMOND) == 64,
                    "Stacked origin generator did not fill the exact 32 available diamond spaces");
            check("DIAMOND=64".equals(fixture.data().getData("origin_stack_pending")),
                    "Stacked origin generator did not conserve its 96-item batch with 64 pending");
            check(containsOnlyExpectedOutputs(fixture.menu(), inputSlots(machine), new ItemStack[] {child}),
                    "Stacked origin generator consumed or changed its reusable configured child");
            check(fixture.data().getData("origin_stack_active") == null,
                    "Stacked origin generator retained a completed active batch");
            refill(machine, fixture);
            long before = charge(machine, fixture);
            tick(machine, fixture, false);
            check(charge(machine, fixture) == before
                            && machine.getMachineProcessor().getOperation(fixture.block()) == null,
                    "Stacked origin generator charged or started while its pending output was blocked");
            clearSlots(fixture.menu(), outputSlots(machine));
            tick(machine, fixture, false);
            check(containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine),
                            new ItemStack[] {new ItemStack(Material.DIAMOND, 64)})
                            && fixture.data().getData("origin_stack_pending") == null,
                    "Stacked origin generator did not flush exactly 64 retained diamonds");
            passed.add(machine.getId() + ": 96-item batch, reusable child, blocked charge and exact pending flush");
        } finally {
            cleanup(location);
        }
    }

    private int countMaterial(BlockMenu menu, int[] slots, Material material) {
        int total = 0;
        for (int slot : slots) {
            ItemStack stack = menu.getItemInSlot(slot);
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private int auditRecipes(AbstractElectricRecipeMachine machine, List<String> failures) {
        return auditRecipeList(machine.getId(), machine.getRecipes(), failures, true);
    }

    private int auditRecipes(AbstractElectricResourceMachine machine, List<String> failures) {
        check(machine instanceof OriginMaterialGenStack || inputSlots(machine).length == 0,
                machine.getId() + ": generator unexpectedly requires physical inputs");
        return auditRecipeList(machine.getId(), machine.getRecipes(), failures, false);
    }

    private int auditRecipeList(String id, List<MachineRecipe> recipes, List<String> failures, boolean requiresInputs) {
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
                    // Resource display recipes intentionally use a null placeholder.
                    // Configured Origin generators receive separate live fixtures.
                    if (!requiresInputs && (input == null || input.getType().isAir())) continue;
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

    private boolean containsOneExpectedOutput(BlockMenu menu, int[] slots, ItemStack[] expected) {
        for (ItemStack template : expected) {
            if (template != null && !template.getType().isAir()
                    && containsOnlyExpectedOutputs(menu, slots, new ItemStack[] {template})) return true;
        }
        return false;
    }

    private boolean containsOnlyExpectedOutputs(BlockMenu menu, int[] slots, ItemStack[] expected) {
        Map<ItemStack, Long> required = new LinkedHashMap<>();
        Map<ItemStack, Long> actual = new LinkedHashMap<>();
        for (ItemStack stack : expected) addCount(required, stack);
        for (int slot : slots) addCount(actual, menu.getItemInSlot(slot));
        return !required.isEmpty() && required.equals(actual);
    }

    private void addCount(Map<ItemStack, Long> counts, ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return;
        // Compare the full native metadata, including configured PDC, independently
        // of DistinctiveItem.canStack: some correct results deliberately never stack.
        ItemStack identity = ItemStack.deserialize(stack.serialize());
        identity.setAmount(1);
        counts.merge(identity, (long) stack.getAmount(), Long::sum);
    }

    private boolean isOriginMaker(AbstractElectricRecipeMachine machine) {
        return machine instanceof OriginMaterialGenMaker
                || machine instanceof OriginMaterialGenMakerLite
                || machine instanceof OriginMaterialGenMakerUltra;
    }

    private void configureOriginMaker(AbstractElectricRecipeMachine machine, Fixture fixture) {
        if (isOriginMaker(machine)) fixture.menu().replaceExistingItem(4, new ItemStack(Material.DIAMOND));
    }

    private void validateOriginMaker(AbstractElectricRecipeMachine machine, ItemStack[] output, ItemStack[] declared) {
        check(output.length == 1 && output[0] != null && output[0].getAmount() == 1,
                machine.getId() + ": maker did not produce one configured generator");
        check(Slimefun.getItemDataService().getItemData(output[0]).equals(
                        Slimefun.getItemDataService().getItemData(declared[0])),
                machine.getId() + ": maker changed the registered generator identity");
        String encoded = output[0].getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(MagicExpansion.getInstance(), "origin_material"), PersistentDataType.STRING);
        if (machine instanceof OriginMaterialGenMakerUltra) {
            ItemStack decoded = OriginMaterialGenMakerUltra.deserializeItemStack(encoded);
            check(decoded != null && decoded.getType() == Material.DIAMOND && decoded.getAmount() == 1,
                    machine.getId() + ": maker lost its full origin item");
        } else {
            check(Material.DIAMOND.name().equals(encoded), machine.getId() + ": maker lost origin material");
        }
    }

    private void validateSymbolOutput(BlockMenu menu, int[] slots) {
        int count = 0;
        for (int slot : slots) {
            ItemStack actual = menu.getItemInSlot(slot);
            if (actual == null || actual.getType().isAir()) continue;
            check(actual.getType() == Material.SUGAR
                            && Slimefun.getItemDataService().getItemData(actual).orElse("").equals("MOMOTECH_LETTER"),
                    "Character constructor did not produce a real generated symbol");
            check(actual.hasItemMeta() && actual.getItemMeta().hasLore(), "Generated symbol lost its character lore");
            count += actual.getAmount();
        }
        check(count == 1, "Character constructor did not produce exactly one symbol");
    }

    private void auditBacktrack(ItemOriginBackTrackMachine machine, int index, List<String> passed) {
        Location location = location(index);
        Fixture fixture = createFixture(machine, location);
        try {
            SlimefunItem source = SlimefunItem.getById(idDecorator("LIGHT_EXTRACT_BASIC"));
            check(source != null, "Backtrack source fixture is unavailable");
            ItemStack template = ItemStack.deserialize(source.getItem().serialize());
            clearSlots(fixture.menu(), inputSlots(machine));
            clearSlots(fixture.menu(), outputSlots(machine));
            fixture.menu().replaceExistingItem(inputSlots(machine)[0], template);
            refill(machine, fixture);
            tick(machine, fixture, false);
            CraftingOperation operation = machine.getMachineProcessor().getOperation(fixture.block());
            check(operation != null, "Backtrack failed to start from a real Slimefun template");
            check(containsOnlyExpectedOutputs(fixture.menu(), inputSlots(machine), new ItemStack[] {template}),
                    "Backtrack consumed its reusable template");
            ItemStack[] captured = operation.getResults();
            check(captured.length == 1, "Backtrack did not snapshot one ingredient");
            boolean registeredIngredient = false;
            for (ItemStack ingredient : source.getRecipe()) {
                if (ingredient != null && SlimefunUtils.isItemSimilar(captured[0], ingredient, true, false)
                        && captured[0].getAmount() == ingredient.getAmount()) registeredIngredient = true;
            }
            check(registeredIngredient, "Backtrack selected an item outside the source recipe");
            fixture.menu().replaceExistingItem(inputSlots(machine)[0], new ItemStack(Material.STONE));
            for (int tick = 0; tick < operation.getTotalTicks() + 2
                    && machine.getMachineProcessor().getOperation(fixture.block()) != null; tick++) {
                refill(machine, fixture);
                tick(machine, fixture, false);
            }
            check(containsOnlyExpectedOutputs(fixture.menu(), outputSlots(machine), captured),
                    "Backtrack output changed when the template was swapped mid-operation");
            check(fixture.menu().getItemInSlot(inputSlots(machine)[0]).getType() == Material.STONE,
                    "Backtrack consumed the replacement template");
            passed.add(machine.getId() + ": reusable template and snapshotted original ingredient");
        } finally {
            cleanup(location);
        }
    }

    private static boolean addonIs(SlimefunItem item, MagicExpansion plugin) {
        try {
            return item.getAddon() == plugin;
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private void requestShutdown() {
        // Finish the current callback and queued chunk listeners before stopping.
        // Paper treats shutdown with a heartbeat older than 1 second as a hard timeout.
        Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 2L);
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
