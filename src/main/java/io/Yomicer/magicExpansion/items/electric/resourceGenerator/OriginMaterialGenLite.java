package io.Yomicer.magicExpansion.items.electric.resourceGenerator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN_LITE;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;

/**
 * 削弱版:基于 OriginMaterialGen 修改,每次只产出 1 个物品.
 */
public class OriginMaterialGenLite extends AbstractElectricResourceMachine implements DistinctiveItem {

    private static final String PENDING_OUTPUT_KEY = "origin_lite_pending";

    private static final int[] BACKGROUND_SLOTS = new int[] { 0, 4, 8, 9, 13, 17 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 10, 11, 12, 14, 15, 16};
    private static final int[] INPUT_BORDER_SLOTS = new int[] {1, 2, 3, 5, 6, 7};

    private static final int[] OUTPUT_SLOTS = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private ItemStack material;

    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientNameVer2("Information"), getGradientNameVer2("type:"), getGradientNameVer2("Addon: MagicExpansion"));

    public OriginMaterialGenLite(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    protected void onBreak(BlockBreakEvent e, BlockMenu menu, Location l) {
        super.onBreak(e, menu, l);
        e.setDropItems(false);
        Location loc = e.getBlock().getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        if (data == null) return;
        String materialName = data.getData("origin_material");
        if (materialName == null) {
            data.removeData("origin_material");
            return;
        }
        ItemStack drop = ORIGIN_MATERIAL_GEN_LITE.clone();
        if (drop == null || !drop.hasItemMeta()) return;
        ItemMeta meta = drop.getItemMeta();
        if (meta != null) {
            Material material = Material.valueOf(materialName);
            String n = ItemStackHelper.getName(new ItemStack(material));
            String originalName = meta.getDisplayName();
            meta.setDisplayName(originalName + " - " + n);
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(getGradientNameVer2("Current Item: " + n));
            meta.setLore(lore);
            NamespacedKey key = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, materialName);
            drop.setItemMeta(meta);
        }
        loc.getWorld().dropItemNaturally(loc, drop);

        long pending = getPending(data);
        if (pending > 0L) {
            try {
                Material pendingMaterial = Material.valueOf(materialName);
                int maxStack = Math.max(1, pendingMaterial.getMaxStackSize());
                while (pending > 0L) {
                    int batch = (int) Math.min((long) maxStack, pending);
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(pendingMaterial, batch));
                    pending -= batch;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        data.removeData(PENDING_OUTPUT_KEY);
        data.removeData("origin_material");
    }

    protected void onPlace(BlockPlaceEvent e, Block b) {
        super.onPlace(e, b);
        ItemStack item = e.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");
        String materialName = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (materialName == null) return;

        Location loc = e.getBlockPlaced().getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        if (data == null) return;
        data.setData("origin_material", materialName);
    }

    @Override
    public void postRegister() {
        registerRecipe(getCraftSecond(), new ItemStack[] { this.material }, getItemStackOutputs());
    }

    @Override
    public MachineRecipe findNextRecipe(BlockMenu menu) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(menu.getBlock().getLocation());
        if (data == null || !flushPending(menu, data)) {
            return null;
        }

        String materialName = data.getData("origin_material");
        if (materialName == null) {
            return null;
        }
        try {
            Material material = Material.valueOf(materialName);
            if (!material.isItem() || material.isAir()) {
                return null;
            }
        } catch (IllegalArgumentException invalid) {
            return null;
        }

        return recipes.isEmpty() ? null : recipes.get(0);
    }

    @Override
    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
        if (data == null) {
            return;
        }

        String materialName = data.getData("origin_material");
        if (materialName == null) {
            menu.replaceExistingItem(4, PROGRESS_STACK.clone());
            return;
        }

        final Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException invalid) {
            menu.replaceExistingItem(4, PROGRESS_STACK.clone());
            return;
        }

        if (!material.isItem() || material.isAir()) {
            return;
        }

        long pending = getPending(data);
        data.setData(PENDING_OUTPUT_KEY,
                Long.toString(pending == Long.MAX_VALUE ? Long.MAX_VALUE : pending + 1L));
        flushPending(menu, data);
        updateInfoStack(menu, new ItemStack(material));
    }

    private boolean flushPending(BlockMenu menu, SlimefunBlockData data) {
        long pending = getPending(data);
        if (pending <= 0L) {
            data.removeData(PENDING_OUTPUT_KEY);
            return true;
        }

        String materialName = data.getData("origin_material");
        if (materialName == null) {
            return false;
        }

        final Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException invalid) {
            return false;
        }
        if (!material.isItem() || material.isAir()) {
            return false;
        }

        int maxStack = Math.max(1, material.getMaxStackSize());
        while (pending > 0L) {
            int batch = (int) Math.min((long) maxStack, pending);
            ItemStack output = new ItemStack(material, batch);
            ItemStack leftover = menu.pushItem(output, getOutputSlots());
            int notStored = leftover == null || leftover.getType().isAir()
                    ? 0
                    : Math.max(0, leftover.getAmount());
            int accepted = batch - notStored;
            if (accepted <= 0) {
                break;
            }
            pending -= accepted;
            if (notStored > 0) {
                break;
            }
        }

        if (pending <= 0L) {
            data.removeData(PENDING_OUTPUT_KEY);
            return true;
        }
        data.setData(PENDING_OUTPUT_KEY, Long.toString(pending));
        return false;
    }

    private static long getPending(SlimefunBlockData data) {
        String raw = data.getData(PENDING_OUTPUT_KEY);
        if (raw == null || raw.isEmpty()) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(raw));
        } catch (NumberFormatException invalid) {
            return 0L;
        }
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> display = new ArrayList<>();

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("Output ⇨"),getGradientNameVer2("Produces 1 item per operation."),getGradientNameVer2("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientNameVer2("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("Output ⇨"),getGradientNameVer2("Produces 1 item per operation."),getGradientNameVer2("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientNameVer2("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
        display.addAll(Arrays.asList(getItemStackOutputs()));

        return display;
    }

    @Override
    protected ItemStack getProgressBar() {
        return PROGRESS_ITEM;
    }

    protected int getProgressSlot() {
        return 13;
    }

    @Override
    protected void setupMenu(BlockMenuPreset preset) {
        preset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE," "), BACKGROUND_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE," "), INPUT_BORDER_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIME_STAINED_GLASS_PANE," "), OUTPUT_BORDER_SLOTS);
        preset.addItem(getProgressSlot(), new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(4, PROGRESS_STACK, ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    protected int[] getInputSlots() {
        return new int[] {};
    }

    @Override
    protected int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    public boolean isSynchronized() {
        return true;
    }

    private void updateInfoStack(BlockMenu menu, ItemStack output) {
        Inventory inv = menu.toInventory();

        if (inv == null || inv.getViewers().isEmpty()) {
            return;
        }

        ItemStack item = PROGRESS_STACK.clone();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getGradientNameVer2("Information"));
        ArrayList lore = new ArrayList<>();
        lore.add(getGradientNameVer2("type:"));
        lore.add(getGradientNameVer2("Addon: MagicExpansion"));
        lore.add(getGradientNameVer2("Current Product: " + ItemStackHelper.getDisplayName(output)));
        lore.add(getGradientNameVer2("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"));
        lore.add(getGradientNameVer2("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s"));
        meta.setLore(lore);
        item.setItemMeta(meta);

        menu.replaceExistingItem(4, item);
    }

    @Override
    public boolean canStack(@NotNull ItemMeta itemMeta, @NotNull ItemMeta itemMeta1) {
        return false;
    }
}
