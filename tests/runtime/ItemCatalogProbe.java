package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.WaterCloudRodProficiency;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.Yomicer.magicExpansion.items.misc.fish.WaterCloudCatchMaterializer;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricRecipeMachine;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RandomBoxMachine;
import io.Yomicer.magicExpansion.items.misc.CHEST_BLOCK;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Disposable real-server catalog/integrity audit for every MagicExpansion item registration.
 *
 * <p>This intentionally verifies registration, item identity, recipes, groups, tickers and
 * energy metadata. It does not claim that every player interaction or every machine recipe
 * has been exercised merely because an item registered successfully.</p>
 */
public final class ItemCatalogProbe extends JavaPlugin {

    private static final Set<String> INTENTIONALLY_UNREGISTERED_TEMPLATES = Set.of(
            // Legacy compatibility constant; maintained replacement is
            // TWO_TO_MAGIC_GEO_MACHINE_FORCE.
            "MAGIC_EXPANSION_TWO_TO_MAGIC_GEO_MACHINE",
            // Historical contribution-menu placeholder intentionally disabled.
            "MAGIC_EXPANSION_UI_4",
            // Exactly one build-status template is registered at runtime.
            "MAGIC_EXPANSION_UI_IS_LATEST_BUILD",
            "MAGIC_EXPANSION_UI_NOT_LATEST_BUILD");

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                audit();
            } catch (Throwable error) {
                getLogger().log(java.util.logging.Level.SEVERE, "ITEM_CATALOG_FATAL", error);
                try {
                    Files.writeString(Path.of("item-catalog-result.txt"),
                            "ITEM_CATALOG_FAIL\nfatal=" + error + "\n");
                } catch (Exception ignored) {
                    // The console still preserves the original failure.
                }
                requestShutdown();
            }
        }, 60L);
    }

    private void audit() throws Exception {
        MagicExpansion plugin = MagicExpansion.getInstance();
        require(plugin != null && plugin.isEnabled(), "MagicExpansion is not enabled");
        auditGen2FishPayoutMetadata();
        auditWaterCloudRodProficiency();

        List<String> failures = new ArrayList<>();
        Map<String, String> declared = new TreeMap<>();
        Map<String, String> duplicateDeclared = new TreeMap<>();

        for (Field field : MagicExpansionItems.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    || !SlimefunItemStack.class.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            SlimefunItemStack template = (SlimefunItemStack) field.get(null);
            if (template == null) {
                failures.add("null template field: " + field.getName());
                continue;
            }

            String id = template.getItemId();
            if (id == null || id.isBlank()) {
                failures.add("blank item id in template field: " + field.getName());
                continue;
            }

            String previous = declared.putIfAbsent(id, field.getName());
            if (previous != null) {
                duplicateDeclared.put(id, previous + "," + field.getName());
                failures.add("duplicate declared id " + id + " in " + previous + " and " + field.getName());
            }
            if (template.getType().isAir()) {
                failures.add("declared template uses AIR: " + id + " (" + field.getName() + ")");
            }
        }

        List<SlimefunItem> registered = Slimefun.getRegistry().getAllSlimefunItems().stream()
                .filter(item -> {
                    try {
                        return item.getAddon() == plugin;
                    } catch (RuntimeException | LinkageError ignored) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(SlimefunItem::getId))
                .toList();

        require(!registered.isEmpty(), "No MagicExpansion Slimefun items were registered");

        Map<String, SlimefunItem> byId = new LinkedHashMap<>();
        Set<String> registeredIds = new TreeSet<>();
        int ticking = 0;
        int synchronizedTickers = 0;
        int energy = 0;
        int recipesWithInputs = 0;
        int ingredientSlots = 0;
        int simpleHandlerItems = 0;
        int displayRecipeItems = 0;
        int configuredRecipeMachines = 0;
        int configuredResourceMachines = 0;
        int energyProviders = 0;
        List<String> warnings = new ArrayList<>();

        StringBuilder catalog = new StringBuilder();
        catalog.append("id\tclass\tticker\tsynchronized\tenergy_type\tcapacity\trecipe_inputs\n");

        for (SlimefunItem item : registered) {
            String id = item.getId();
            if (byId.putIfAbsent(id, item) != null) {
                failures.add("duplicate registered id: " + id);
                continue;
            }
            registeredIds.add(id);

            if (SlimefunItem.getById(id) != item) {
                failures.add("getById identity mismatch: " + id);
            }

            ItemStack template = item.getItem();
            if (template == null || template.getType().isAir()) {
                failures.add("registered item has missing/AIR template: " + id);
            } else if (SlimefunItem.getByItem(template) != item) {
                failures.add("getByItem template identity mismatch: " + id);
            }

            if (item.getItemGroup() == null || !item.getItemGroup().isRegistered()) {
                failures.add("item group is not registered: " + id);
            }
            if (item.getRecipeType() == null) {
                failures.add("recipe type is null: " + id);
            }

            ItemStack[] recipe = item.getRecipe();
            if (recipe == null || recipe.length != 9) {
                failures.add("recipe is not normalized to 9 slots: " + id
                        + " length=" + (recipe == null ? "null" : recipe.length));
            } else {
                int nonNull = 0;
                for (ItemStack ingredient : recipe) {
                    if (ingredient != null && !ingredient.getType().isAir()) {
                        nonNull++;
                        ingredientSlots++;
                        if (ingredient.getAmount() <= 0) {
                            failures.add("recipe ingredient has nonpositive amount: " + id);
                        }
                    }
                }
                if (nonNull > 0) recipesWithInputs++;
            }

            ItemStack output = item.getRecipeOutput();
            if (output == null || output.getType().isAir() || output.getAmount() <= 0) {
                failures.add("invalid recipe output: " + id);
            }

            boolean hasTicker = item.getBlockTicker() != null;
            boolean syncTicker = hasTicker && item.getBlockTicker().isSynchronized();
            if (hasTicker) ticking++;
            if (syncTicker) synchronizedTickers++;

            String energyType = "";
            long capacity = -1L;
            if (item instanceof EnergyNetComponent component) {
                energy++;
                if (component.getEnergyComponentType() == null) {
                    failures.add("null energy component type: " + id);
                } else {
                    energyType = component.getEnergyComponentType().name();
                }
                capacity = component.getCapacityLong();
                if (capacity < 0L) {
                    failures.add("negative energy capacity: " + id + "=" + capacity);
                }
                if (item instanceof EnergyNetProvider) {
                    energyProviders++;
                    if (capacity <= 0L) {
                        failures.add("energy provider has nonpositive capacity: " + id + "=" + capacity);
                    }
                }
            }

            if (item instanceof SimpleSlimefunItem<?> simple) {
                simpleHandlerItems++;
                try {
                    if (simple.getItemHandler() == null) {
                        failures.add("simple item returned null handler: " + id);
                    }
                } catch (RuntimeException | LinkageError error) {
                    failures.add("simple item handler failed: " + id + " -> "
                            + error.getClass().getSimpleName() + ": " + error.getMessage());
                }
            }

            if (item instanceof RecipeDisplayItem display) {
                displayRecipeItems++;
                try {
                    List<ItemStack> displayRecipes = display.getDisplayRecipes();
                    if (displayRecipes == null) {
                        failures.add("recipe display returned null list: " + id);
                    }
                } catch (RuntimeException | LinkageError error) {
                    failures.add("recipe display failed: " + id + " -> "
                            + error.getClass().getSimpleName() + ": " + error.getMessage());
                }
            }

            if (item instanceof AbstractElectricRecipeMachine machine) {
                configuredRecipeMachines++;

                boolean specialUntimedMachine =
                        item instanceof RandomBoxMachine || item instanceof CHEST_BLOCK;

                if (!specialUntimedMachine) {
                    if (machine.getCapacity() <= 0) {
                        failures.add("recipe machine capacity not configured: " + id + "=" + machine.getCapacity());
                    }
                    if (machine.getEnergyConsumption() <= 0
                            || machine.getEnergyConsumption() >= machine.getCapacity()) {
                        failures.add("recipe machine consumption invalid: " + id + "="
                                + machine.getEnergyConsumption() + "/" + machine.getCapacity());
                    }
                    if (machine.getSpeed() <= 0) {
                        failures.add("recipe machine speed not configured: " + id + "=" + machine.getSpeed());
                    }
                } else {
                    if (machine.getCapacityLong() != 0L || machine.isChargeable()) {
                        failures.add("unpowered special machine exposes an energy buffer: " + id
                                + "=" + machine.getCapacityLong());
                    }
                    warnings.add("special untimed machine intentionally has no electric recipe tuning: " + id);
                }

                if (machine.getRecipes().isEmpty() && !specialUntimedMachine) {
                    warnings.add("recipe machine currently exposes zero registered recipes: " + id);
                }
            }

            if (item instanceof AbstractElectricResourceMachine machine) {
                configuredResourceMachines++;
                if (machine.getCapacity() <= 0) {
                    failures.add("resource machine capacity not configured: " + id + "=" + machine.getCapacity());
                }
                if (machine.getEnergyConsumption() <= 0
                        || machine.getEnergyConsumption() >= machine.getCapacity()) {
                    failures.add("resource machine consumption invalid: " + id + "="
                            + machine.getEnergyConsumption() + "/" + machine.getCapacity());
                }
                if (machine.getSpeed() <= 0) {
                    failures.add("resource machine speed not configured: " + id + "=" + machine.getSpeed());
                }
                if (machine.getItemStackOutputs() == null || machine.getItemStackOutputs().length == 0) {
                    failures.add("resource machine has no configured output templates: " + id);
                }
                if (machine.getRecipes().isEmpty()) {
                    warnings.add("resource machine currently exposes zero registered recipes: " + id);
                }
            }

            int nonNullRecipeSlots = 0;
            if (recipe != null) {
                for (ItemStack ingredient : recipe) {
                    if (ingredient != null && !ingredient.getType().isAir()) nonNullRecipeSlots++;
                }
            }

            catalog.append(id).append('\t')
                    .append(item.getClass().getName()).append('\t')
                    .append(hasTicker).append('\t')
                    .append(syncTicker).append('\t')
                    .append(energyType).append('\t')
                    .append(capacity).append('\t')
                    .append(nonNullRecipeSlots).append('\n');
        }

        Set<String> missingDeclared = new TreeSet<>(declared.keySet());
        missingDeclared.removeAll(registeredIds);

        Set<String> expectedMissing = new TreeSet<>(missingDeclared);
        expectedMissing.retainAll(INTENTIONALLY_UNREGISTERED_TEMPLATES);
        missingDeclared.removeAll(INTENTIONALLY_UNREGISTERED_TEMPLATES);

        for (String id : expectedMissing) {
            warnings.add("intentionally unregistered template: " + id
                    + " field=" + declared.get(id));
        }
        for (String id : missingDeclared) {
            failures.add("declared MagicExpansionItems template was not registered: "
                    + id + " field=" + declared.get(id));
        }

        Set<String> extraRegistered = new TreeSet<>(registeredIds);
        extraRegistered.removeAll(declared.keySet());

        StringBuilder result = new StringBuilder();
        result.append(failures.isEmpty() ? "ITEM_CATALOG_PASS\n" : "ITEM_CATALOG_FAIL\n");
        result.append("declared_templates=").append(declared.size()).append('\n');
        result.append("registered_items=").append(registered.size()).append('\n');
        result.append("registered_unique_ids=").append(registeredIds.size()).append('\n');
        result.append("missing_declared=").append(missingDeclared.size()).append('\n');
        result.append("extra_registered=").append(extraRegistered.size()).append('\n');
        result.append("ticking_items=").append(ticking).append('\n');
        result.append("synchronized_tickers=").append(synchronizedTickers).append('\n');
        result.append("energy_components=").append(energy).append('\n');
        result.append("recipes_with_inputs=").append(recipesWithInputs).append('\n');
        result.append("recipe_ingredient_slots=").append(ingredientSlots).append('\n');
        result.append("duplicate_declared_ids=").append(duplicateDeclared.size()).append('\n');
        result.append("simple_handler_items=").append(simpleHandlerItems).append('\n');
        result.append("recipe_display_items=").append(displayRecipeItems).append('\n');
        result.append("configured_recipe_machines=").append(configuredRecipeMachines).append('\n');
        result.append("configured_resource_machines=").append(configuredResourceMachines).append('\n');
        result.append("energy_providers=").append(energyProviders).append('\n');

        if (!missingDeclared.isEmpty()) {
            result.append("missing_ids=").append(String.join(",", missingDeclared)).append('\n');
        }
        if (!extraRegistered.isEmpty()) {
            result.append("extra_ids=").append(String.join(",", extraRegistered)).append('\n');
        }
        for (String warning : warnings) {
            result.append("warning=").append(warning).append('\n');
        }
        for (String failure : failures) {
            result.append("failure=").append(failure).append('\n');
        }

        Files.writeString(Path.of("item-catalog.tsv"), catalog.toString());
        Files.writeString(Path.of("item-catalog-result.txt"), result.toString());
        getLogger().info((failures.isEmpty() ? "ITEM_CATALOG_PASS" : "ITEM_CATALOG_FAIL")
                + " declared=" + declared.size()
                + " registered=" + registered.size()
                + " ticker=" + ticking
                + " energy=" + energy
                + " providers=" + energyProviders
                + " simpleHandlers=" + simpleHandlerItems
                + " displayItems=" + displayRecipeItems
                + " missing=" + missingDeclared.size()
                + " warnings=" + warnings.size()
                + " failures=" + failures.size());

        requestShutdown();
    }

    /**
     * Real-server assertion for the portable Gen2 catch path: the loot-table
     * seed must become a populated individual fish on every supported runtime.
     */
    private void auditGen2FishPayoutMetadata() {
        ItemStack seed = new ItemStack(Gen2Fish.YUNYIN.getMaterial());
        ItemMeta meta = seed.getItemMeta();
        require(meta != null, "Gen2 seed has no ItemMeta");
        meta.getPersistentDataContainer().set(FishAttributeGenerator.GEN2_TYPE,
                PersistentDataType.STRING, Gen2Fish.YUNYIN.getId());
        seed.setItemMeta(meta);

        require(WaterCloudCatchMaterializer.isRawSeed(seed), "Gen2 seed not recognized");
        ItemStack caught = WaterCloudCatchMaterializer.materialize(seed, 2);
        require(caught != seed, "Gen2 seed was not materialized");
        require(Gen2Fish.YUNYIN == FishAttributeGenerator.getType(caught),
                "Gen2 payout lost its fish type");
        require(FishAttributeGenerator.getElements(caught) != null,
                "Gen2 payout has no elemental attributes");
        require(FishAttributeGenerator.getTrait(caught) == Gen2Fish.YUNYIN.getDefaultTrait(),
                "Gen2 payout lost its fish trait");
        require(FishAttributeGenerator.getQuality(caught) >= 0.7,
                "Gen2 payout has invalid quality");
        require(!WaterCloudCatchMaterializer.isRawSeed(caught),
                "Gen2 payout is still an unprocessed seed");
        require(WaterCloudCatchMaterializer.materialize(caught, 2) == caught,
                "Completed Gen2 fish was rerolled");
        require(WaterCloudCatchMaterializer.isRawSeed(seed),
                "Original Gen2 loot-table seed was modified");
        getLogger().info("GEN2_FISH_PAYOUT_PASS");
    }

    private void auditWaterCloudRodProficiency() {
        // The runtime registry may use the normalized Slimefun item ID; never
        // assume that the Java field name is the complete persisted identity.
        String rodId = MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_REED.getItemId();
        SlimefunItem registered = SlimefunItem.getById(rodId);
        require(registered != null, "Registered Reedflower Water Cloud rod missing");
        ItemStack rod = registered.getItem().clone();
        require(WaterCloudRodProficiency.supportsRodId(registered.getId()),
                "Proficiency not enabled for Reedflower rod");

        var before = rod.getItemMeta().getPersistentDataContainer();
        String savedIdentity = before.get(new org.bukkit.NamespacedKey("slimefun", "id"),
                org.bukkit.persistence.PersistentDataType.STRING);
        var previous = WaterCloudRodProficiency.read(rod);
        require(previous.level() == 1 && previous.xp() == 0,
                "Unlevelled rod does not start at level one");

        var progressed = WaterCloudRodProficiency.award(rod, 300);
        require(progressed.level() == 2 && progressed.xp() == 0,
                "Upstream Water Cloud threshold was not respected");
        require(SlimefunItem.getByItem(rod) == registered,
                "Proficiency mutation changed the rod's Slimefun identity");
        require(java.util.Objects.equals(savedIdentity,
                rod.getItemMeta().getPersistentDataContainer().get(
                        new org.bukkit.NamespacedKey("slimefun", "id"),
                        org.bukkit.persistence.PersistentDataType.STRING)),
                "Proficiency mutation overwrote the Slimefun ID PDC");
        require(rod.getItemMeta().getPersistentDataContainer().get(
                WaterCloudRodProficiency.LV_KEY,
                org.bukkit.persistence.PersistentDataType.INTEGER) == 2,
                "Upstream Water Cloud level PDC not persisted");
        require(rod.getItemMeta().getPersistentDataContainer().get(
                WaterCloudRodProficiency.XP_KEY,
                org.bukkit.persistence.PersistentDataType.INTEGER) == 0,
                "Upstream Water Cloud XP PDC not persisted");
        require(!WaterCloudRodProficiency.supportsRodId(
                "FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO"),
                "Starter Cyan Bamboo rod must not receive upstream proficiency");
        getLogger().info("WATER_CLOUD_PROFICIENCY_PASS");
    }

    private void requestShutdown() {
        // Finish the current callback and queued chunk listeners before stopping.
        // Paper treats shutdown with a heartbeat older than 1 second as a hard timeout.
        Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 2L);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
