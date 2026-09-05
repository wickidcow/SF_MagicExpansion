package io.Yomicer.magicExpansion.items.electric.resourceGenerator;

import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class ResourceMachine extends AbstractElectricResourceMachine {

    private static final int[] BACKGROUND_SLOTS = new int[] { 0, 4, 8, 9, 13, 17 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 10, 11, 12, 14, 15, 16};
    private static final int[] INPUT_BORDER_SLOTS = new int[] {1, 2, 3, 5, 6, 7};
    private static final int[] INPUT_SLOTS = new int[0];

    private static final int[] OUTPUT_SLOTS = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private ItemStack material;

    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientName("Information"), getGradientName("type:"), getGradientName("Addon: MagicExpansion"));



    public ResourceMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

    }




    @Override
    public void postRegister() {
        registerRecipe(getCraftSecond(), new ItemStack[] { this.material }, getItemStackOutputs());
    }

    @Override
    public void tick(BlockMenu menu, Block b) {
//            updateInfoStack(menu);
        super.tick(menu, b);
    }

    @Override
	public MachineRecipe findNextRecipe(BlockMenu menu) {
        if (recipes.isEmpty()) {
            return null;
        }

        /*
         * Resource machines have no recipe inputs, so the only pre-flight condition here is whether every
         * output slot is completely full. Stop at the first slot that can still accept something instead of
         * scanning all 36 slots on every new operation. This is especially important for large Mine Man farms.
         */
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType().isAir() || item.getAmount() < item.getMaxStackSize()) {
                return recipes.get(0);
            }
        }

        return null;
    }


	@Override
	public List<ItemStack> getDisplayRecipes() {
	    List<ItemStack> display = new ArrayList<>();

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Output Items ⇩"),getGradientName("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientName("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Output Items ⇩"),getGradientName("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientName("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
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

    private static void updateInfoStack(BlockMenu menu) {
        Inventory inv = menu.toInventory();

        if (inv == null || inv.getViewers().isEmpty()) {
            return;
        }
//
//        ItemStack item = PROGRESS_STACK.clone();
//        ItemMeta meta = item.getItemMeta();
//
//        meta.setDisplayName(getGradientName("Information"));
//        ArrayList lore = new ArrayList<>();
//        lore.add(getGradientName("Production interval: " + getCraftSecondDisplay() + " seconds"));
//        item.setLore(lore);
//        item.setItemMeta(meta);
//
//        menu.replaceExistingItem(4, item);
    }
}
