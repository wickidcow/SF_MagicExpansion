package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
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
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Disposable real-server catalog/integrity audit for every MagicExpansion item registration.
 *
 * <p>This intentionally verifies registration, item identity, recipes, groups, tickers and
 * energy metadata. It does not claim that every player interaction or every machine recipe
 * has been exercised merely because an item registered successfully.</p>
 */
public final class ItemCatalogProbe extends JavaPlugin {

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
                Bukkit.shutdown();
            }
        }, 60L);
    }

    private void audit() throws Exception {
        MagicExpansion plugin = MagicExpansion.getInstance();
        require(plugin != null && plugin.isEnabled(), "MagicExpansion is not enabled");

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

        if (!missingDeclared.isEmpty()) {
            result.append("missing_ids=").append(String.join(",", missingDeclared)).append('\n');
        }
        if (!extraRegistered.isEmpty()) {
            result.append("extra_ids=").append(String.join(",", extraRegistered)).append('\n');
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
                + " missing=" + missingDeclared.size()
                + " failures=" + failures.size());

        Bukkit.shutdown();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
