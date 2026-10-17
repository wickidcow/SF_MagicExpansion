package io.Yomicer.magicExpansion.items.electric.resourceGenerator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;

public class OriginMaterialGenStack extends AbstractElectricResourceMachine {

    private static final int[] INPUT_SLOTS = new int[] {
            0, 1, 2, 3,
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30,
            36, 37, 38, 39,
            45, 46, 47, 48
    };

    private static final int[] OUTPUT_SLOTS = new int[] {
            5, 6, 7, 8,
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35,
            41, 42, 43, 44,
            50, 51, 52, 53
    };
    private ItemStack material;
    private static final int[] INFO_BORDER_SLOTS = new int[] { 4, 22, 13, 31, 40, 49 };

    private static final String MACHINE_MULTIPLIER_KEY = "machine_random_multiplier";
    private static final String PENDING_OUTPUT_KEY = "origin_stack_pending";
    private static final String ACTIVE_OUTPUT_KEY = "origin_stack_active";

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private static final NamespacedKey ORIGIN_MATERIAL_KEY = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");

    public OriginMaterialGenStack(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }
    @Override
    protected void onPlace(BlockPlaceEvent e, Block b) {
        super.onPlace(e, b);
        double randomMultiplier = 1.0 + Math.random();

        Location loc = b.getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        if (data != null) {
            data.setData(MACHINE_MULTIPLIER_KEY, String.valueOf(randomMultiplier));
        }
    }

    @Override
    protected int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    protected int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }
    @Override
    public boolean isSynchronized() {
        return true;
    }

    @Override
    public MachineRecipe findNextRecipe(BlockMenu menu) {
        Block block = menu.getBlock();
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());
        if (data == null) {
            return null;
        }

        if (!flushPending(menu, block, data)) {
            return null;
        }

        Map<Material, Long> production = calculateProduction(menu, multiplier(data));
        if (production.isEmpty()) {
            data.removeData(ACTIVE_OUTPUT_KEY);
            return null;
        }

        data.setData(ACTIVE_OUTPUT_KEY, serializeAmounts(production));
        return new MachineRecipe(1, new ItemStack[]{null}, new ItemStack[]{null});
    }

    @Override
    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
        if (data == null) {
            return;
        }

        Map<Material, Long> earned = parseAmounts(data.getData(ACTIVE_OUTPUT_KEY));
        data.removeData(ACTIVE_OUTPUT_KEY);
        if (earned.isEmpty()) {
            return;
        }

        Map<Material, Long> pending = parseAmounts(data.getData(PENDING_OUTPUT_KEY));
        for (Map.Entry<Material, Long> entry : earned.entrySet()) {
            pending.merge(entry.getKey(), entry.getValue(), OriginMaterialGenStack::saturatingAdd);
        }
        data.setData(PENDING_OUTPUT_KEY, serializeAmounts(pending));

        flushPending(menu, b, data);

        Map<Material, Integer> display = new LinkedHashMap<>();
        int totalGenerators = 0;
        for (Map.Entry<Material, Long> entry : earned.entrySet()) {
            int shown = (int) Math.min(Integer.MAX_VALUE, entry.getValue());
            display.put(entry.getKey(), shown);
            totalGenerators += shown;
        }
        updateStatusUI(menu, totalGenerators, display.size(), display, multiplier(data));
    }

    private Map<Material, Long> calculateProduction(BlockMenu menu, double randomFactor) {
        Map<Material, Long> production = new LinkedHashMap<>();

        for (int slot : INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (!isValidGenerator(item)) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            String materialName = meta.getPersistentDataContainer().get(
                    ORIGIN_MATERIAL_KEY, PersistentDataType.STRING);
            if (materialName == null) {
                continue;
            }

            try {
                Material material = Material.valueOf(materialName);
                if (!material.isItem() || material.isAir()) {
                    continue;
                }

                long generated = (long) Math.floor(item.getAmount() * randomFactor);
                if (generated > 0L) {
                    production.merge(material, generated, OriginMaterialGenStack::saturatingAdd);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        return production;
    }

    private boolean flushPending(BlockMenu menu, Block block, SlimefunBlockData data) {
        Map<Material, Long> pending = parseAmounts(data.getData(PENDING_OUTPUT_KEY));
        if (pending.isEmpty()) {
            data.removeData(PENDING_OUTPUT_KEY);
            return true;
        }

        Iterator<Map.Entry<Material, Long>> iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Material, Long> entry = iterator.next();
            long remaining = entry.getValue();
            Material material = entry.getKey();
            int maxStack = Math.max(1, material.getMaxStackSize());

            while (remaining > 0L) {
                int batch = (int) Math.min((long) maxStack, remaining);
                ItemStack output = new ItemStack(material, batch);
                ItemStack leftover = menu.pushItem(output, OUTPUT_SLOTS);
                int notStored = leftover == null || leftover.getType().isAir()
                        ? 0
                        : Math.max(0, leftover.getAmount());
                int accepted = batch - notStored;
                if (accepted <= 0) {
                    break;
                }
                remaining -= accepted;
                if (notStored > 0) {
                    break;
                }
            }

            if (remaining <= 0L) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
                break;
            }
        }

        if (pending.isEmpty()) {
            data.removeData(PENDING_OUTPUT_KEY);
            return true;
        }

        data.setData(PENDING_OUTPUT_KEY, serializeAmounts(pending));
        return false;
    }

    private double multiplier(SlimefunBlockData data) {
        String raw = data.getData(MACHINE_MULTIPLIER_KEY);
        if (raw != null) {
            try {
                double parsed = Double.parseDouble(raw);
                if (Double.isFinite(parsed) && parsed >= 1.0D && parsed < 2.0D) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        double generated = 1.0D + Math.random();
        data.setData(MACHINE_MULTIPLIER_KEY, Double.toString(generated));
        return generated;
    }

    private static Map<Material, Long> parseAmounts(String raw) {
        Map<Material, Long> result = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return result;
        }

        for (String token : raw.split(";")) {
            int separator = token.indexOf('=');
            if (separator <= 0 || separator == token.length() - 1) {
                continue;
            }
            try {
                Material material = Material.valueOf(token.substring(0, separator));
                long amount = Long.parseLong(token.substring(separator + 1));
                if (material.isItem() && !material.isAir() && amount > 0L) {
                    result.merge(material, amount, OriginMaterialGenStack::saturatingAdd);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    private static String serializeAmounts(Map<Material, Long> amounts) {
        StringBuilder encoded = new StringBuilder();
        amounts.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0L)
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Material::name)))
                .forEach(entry -> {
                    if (encoded.length() > 0) {
                        encoded.append(';');
                    }
                    encoded.append(entry.getKey().name())
                            .append('=')
                            .append(entry.getValue());
                });
        return encoded.toString();
    }

    private static long saturatingAdd(long left, long right) {
        if (left <= 0L) return Math.max(0L, right);
        if (right <= 0L) return left;
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    private boolean isValidGenerator(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(
                ORIGIN_MATERIAL_KEY, PersistentDataType.STRING);
    }

    @Override
    protected void onBreak(org.bukkit.event.block.BlockBreakEvent e, BlockMenu menu, Location location) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(location);
        if (data != null) {
            dropPending(location, parseAmounts(data.getData(PENDING_OUTPUT_KEY)));
            data.removeData(PENDING_OUTPUT_KEY);
            data.removeData(ACTIVE_OUTPUT_KEY);
        }
        super.onBreak(e, menu, location);
    }

    private void dropPending(Location location, Map<Material, Long> pending) {
        for (Map.Entry<Material, Long> entry : pending.entrySet()) {
            long remaining = entry.getValue();
            int maxStack = Math.max(1, entry.getKey().getMaxStackSize());
            while (remaining > 0L) {
                int batch = (int) Math.min((long) maxStack, remaining);
                location.getWorld().dropItemNaturally(
                        location, new ItemStack(entry.getKey(), batch));
                remaining -= batch;
            }
        }
    }

    private void updateStatusUI(BlockMenu menu, int total, int types, Map<Material, Integer> productionMap, double randomFactor) {
        int usedOutputSlots = 0;
        for (int slot : OUTPUT_SLOTS) {
            if (menu.getItemInSlot(slot) != null) usedOutputSlots++;
        }
        double fillPercentage = (double) usedOutputSlots / OUTPUT_SLOTS.length * 100;

        // 构建 Slot 4 的 Lore
        List<String> slot4Lore = new ArrayList<>();
        if (total > 0) {
            slot4Lore.add(getGradientNameVer2("Status: Generating"));
        } else {
            slot4Lore.add(getGradientNameVer2("Status: Waiting for input"));
        }
        slot4Lore.add(getGradientNameVer2("Output Usage: " + String.format("%.1f", fillPercentage) + "%"));
        slot4Lore.add(getGradientNameVer2("Used: " + usedOutputSlots + " / " + OUTPUT_SLOTS.length));

        menu.replaceExistingItem(4, new CustomItemStack(
                total > 0 ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                getGradientNameVer2("Machine Status"),
                slot4Lore
        ));


        List<String> combinedLore = new ArrayList<>();
        combinedLore.add(getGradientNameVer2("Amount: " + total));
        combinedLore.add(getGradientNameVer2("Source Multiplier: " + String.format("%.2f", randomFactor)));
        menu.replaceExistingItem(13, new CustomItemStack(Material.REPEATER, getGradientNameVer2("Core Data"), combinedLore));
        updateInfoSlots(menu, productionMap, randomFactor);
    }
    private void updateInfoSlots(BlockMenu menu, Map<Material, Integer> productionMap, double randomFactor) {
        Inventory inv = menu.toInventory();
        if (inv == null || inv.getViewers().isEmpty()) {
            return;
        }
        String[] easterEggs = {
                getGradientNameVer2("Feeling lucky?"),
                getGradientNameVer2("The machine hums softly..."),
                getGradientNameVer2("That should not be possible!"),
                getGradientNameVer2("Increase the multiplier for better results."),
                getGradientNameVer2("Produces randomized resources from stored materials."),
                getGradientNameVer2("This is far too slow..."),
                getGradientNameVer2("A little faster!"),
                getGradientNameVer2("Magic in progress...")
        };

        int[] infoSlots = {31, 40, 49};
        List<Map.Entry<Material, Integer>> entryList = new ArrayList<>(productionMap.entrySet());
        if (entryList.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                String eggText = easterEggs[(int) (Math.random() * easterEggs.length)];
                menu.replaceExistingItem(infoSlots[i], new CustomItemStack(Material.PAPER, getGradientNameVer2("System Log"), getGradientNameVer2(eggText)));
            }
        } else {
            List<String> linesToShow = new ArrayList<>();
            linesToShow.add(getGradientNameVer2("--- Current Output ---"));
            for (Map.Entry<Material, Integer> entry : entryList) {
                Material mat = entry.getKey();
                int count = entry.getValue();
                int outCount = (int) (count * randomFactor);
                String name = ItemStackHelper.getDisplayName(new ItemStack(mat));
                linesToShow.add(getGradientNameVer2(name + ": " + outCount));
            }
            if (entryList.size() > 6) {
                linesToShow.add(getGradientNameVer2("... (Click to view more)"));
            } else {
                linesToShow.add(getGradientNameVer2("(Click to view)"));
            }
            for (int i = 0; i < 3; i++) {
                List<String> slotLore = new ArrayList<>();
                int start = i * 4;
                if (start < linesToShow.size()) {
                    int end = Math.min(start + 4, linesToShow.size());
                    slotLore = linesToShow.subList(start, end);
                    menu.replaceExistingItem(infoSlots[i], new CustomItemStack(Material.WRITABLE_BOOK, getGradientNameVer2("Produces randomized resources from stored materials."), slotLore));
                } else {
                    String eggText = easterEggs[(int) (Math.random() * easterEggs.length)];
                    menu.replaceExistingItem(infoSlots[i], new CustomItemStack(Material.PAPER, getGradientNameVer2("Easter Egg"), (eggText)));
                }
            }
        }
    }

    private void openProductionMenu(Player p, Map<Material, Integer> productionMap) {
        ChestMenu menu = new ChestMenu(getGradientNameVer2("Produces randomized resources from stored materials."));

        menu.setPlayerInventoryClickable(false);
        menu.setEmptySlotsClickable(false);
        ItemStack background = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            menu.addItem(i, background, ChestMenuUtils.getEmptyClickHandler());
        }
        int index = 0;
        int slotIndex = 0;
        for (Map.Entry<Material, Integer> entry : productionMap.entrySet()) {
            if (slotIndex >= 54) break; // 超过菜单大小
            Material mat = entry.getKey();
            int count = entry.getValue();
            ItemStack displayItem = new ItemStack(mat);
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(getGradientNameVer2("Output: " + ItemStackHelper.getDisplayName(displayItem)));
                List<String> lore = new ArrayList<>();
                lore.add(getGradientNameVer2("Amount: " + count));
                lore.add(getGradientNameVer2("Final output is affected by the multiplier."));
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            menu.addItem(slotIndex, displayItem, ChestMenuUtils.getEmptyClickHandler());
            slotIndex++;
        }
        if (productionMap.isEmpty()) {
            menu.addItem(22, new CustomItemStack(Material.BARRIER, getGradientNameVer2("Produces randomized resources from stored materials.")), ChestMenuUtils.getEmptyClickHandler());
        }
        menu.open(p);
    }

    private Map<Material, Integer> calculateProductionMap(BlockMenu menu) {
        Map<Material, Integer> map = new HashMap<>();
        for (int slot : INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null) continue;
            if (!isValidGenerator(item)) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            String materialName = meta.getPersistentDataContainer().get(ORIGIN_MATERIAL_KEY, PersistentDataType.STRING);

            if (materialName != null) {
                try {
                    Material mat = Material.valueOf(materialName);
                    int count = item.getAmount();
                    map.put(mat, map.getOrDefault(mat, 0) + count);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return map;
    }

    @Override
    protected void setupMenu(BlockMenuPreset preset) {
        for (int slot : INFO_BORDER_SLOTS) {
            preset.addItem(slot, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(13, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(22, new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());
        ChestMenu.MenuClickHandler infoClickHandler = (player, slot, item, action) -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof BlockMenu) {
                BlockMenu menu = (BlockMenu) player.getOpenInventory().getTopInventory().getHolder();
                Map<Material, Integer> currentMap = calculateProductionMap(menu);
                openProductionMenu(player, currentMap);
            }
            return false;
        };
        preset.addItem(31, new CustomItemStack(Material.BOOK, getGradientNameVer2("Click to View")), infoClickHandler);
        preset.addItem(40, new CustomItemStack(Material.BOOK, getGradientNameVer2("Click to View")), infoClickHandler);
        preset.addItem(49, new CustomItemStack(Material.BOOK, getGradientNameVer2("Click to View")), infoClickHandler);
    }


    @Override
    protected int getProgressSlot() { return 22; }
    @Override
    public ItemStack getProgressBar() { return PROGRESS_ITEM; }
    @Override
    public void postRegister() {
        registerRecipe(getCraftSecond(), new ItemStack[] { this.material }, getItemStackOutputs());
    }
    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("Insert the required materials into the machine."), getGradientNameVer2("Machine")));
        list.addAll(Arrays.asList(getItemStackOutputs()));
        return list;
    }
}
