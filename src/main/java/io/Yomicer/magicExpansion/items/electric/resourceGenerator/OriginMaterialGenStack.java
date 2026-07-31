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
        return false;
    }

    @Override
    public MachineRecipe findNextRecipe(BlockMenu menu) {
        if (!hasOutputSpace(menu)) return null;

        for (int slot : INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null) continue;
            if (isValidGenerator(item)) {
                return new MachineRecipe(1, new ItemStack[]{null}, new ItemStack[]{null});
            }
        }
        return null;
    }

    @Override
    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        Location loc = b.getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        double randomFactor = 1.0;
        if (data != null) {
            String multiplierStr = data.getData(MACHINE_MULTIPLIER_KEY);
            if (multiplierStr != null) {
                try {
                    randomFactor = Double.parseDouble(multiplierStr);
                } catch (NumberFormatException e) {
                    randomFactor = 1.0;
                }
            } else {
                randomFactor = 1.0 + Math.random();
                data.setData(MACHINE_MULTIPLIER_KEY, String.valueOf(randomFactor));
            }
        }

        Map<Material, Integer> productionMap = new HashMap<>();
        int totalGenerators = 0;
        int distinctTypes = 0;

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
                    productionMap.put(mat, productionMap.getOrDefault(mat, 0) + count);
                    totalGenerators += count;
                    distinctTypes++;
                } catch (IllegalArgumentException e) {
                }
            }
        }

        if (totalGenerators > 0) {
            for (Map.Entry<Material, Integer> entry : productionMap.entrySet()) {
                Material mat = entry.getKey();
                int baseAmount = entry.getValue();
                int finalAmount = (int) (baseAmount * randomFactor);
                ItemStack singleOutput = new ItemStack(mat, 1);
                for (int i = 0; i < finalAmount; i++) {
                    if (menu.pushItem(singleOutput.clone(), OUTPUT_SLOTS) != null) {
                        break;
                    }
                }
            }
        }

        updateStatusUI(menu, totalGenerators, distinctTypes, productionMap, randomFactor);
    }

    private boolean isValidGenerator(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(ORIGIN_MATERIAL_KEY, PersistentDataType.STRING);
    }

    private boolean hasOutputSpace(BlockMenu menu) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getAmount() < item.getMaxStackSize()) {
                return true;
            }
        }
        return false;
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
