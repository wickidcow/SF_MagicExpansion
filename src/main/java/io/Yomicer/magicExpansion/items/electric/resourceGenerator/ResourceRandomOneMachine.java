package io.Yomicer.magicExpansion.items.electric.resourceGenerator;

import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.Yomicer.magicExpansion.utils.PendingMachineOutput;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class ResourceRandomOneMachine extends AbstractElectricResourceMachine {

    private static final String PENDING_OUTPUT_KEY = "random_one_pending_output";

    private static final int[] BACKGROUND_SLOTS = new int[] { 0, 4, 8, 9, 13, 17 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 10, 11, 12, 14, 15, 16};
    private static final int[] INPUT_BORDER_SLOTS = new int[] {1, 2, 3, 5, 6, 7};

    private static final int[] OUTPUT_SLOTS = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private ItemStack material;

    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientName("Information"), getGradientName("type:"), getGradientName("Addon: MagicExpansion"));



    public ResourceRandomOneMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

    }




    @Override
    public void postRegister() {
        registerRecipe(getCraftSecond(), new ItemStack[] { this.material }, getItemStackOutputs());
    }

    @Override
    public void tick(BlockMenu menu, Block b) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
        if (data == null) return;
        PendingMachineOutput.Store pending = pendingStore(data);
        if (pending.read() != null) {
            // A recorded batch already owns the completed operation, including when
            // the original save threw after updating the block-data cache.
            if (PendingMachineOutput.hasValidRecord(pending)) {
                getMachineProcessor().endOperation(b);
            }
            PendingMachineOutput.flush(pending, item -> menu.pushItem(item, getOutputSlots()));
            return;
        }
        super.tick(menu, b);
    }

    @Override
    protected boolean canCompleteOutputs(BlockMenu menu, ItemStack[] outputs) {
        // Only one result is selected. Its complete stack is saved before delivery;
        // batches larger than the physical menu are drained without another cycle.
        return true;
    }

    @Override
    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        List<ItemStack> validOutputs = new ArrayList<>();

        // 收集所有非 null 的输出
        for (ItemStack output : outputs) {
            if (output != null && !output.getType().isAir() && output.getAmount() > 0) {
                validOutputs.add(output);
            }
        }

        // 如果有有效的输出
        if (!validOutputs.isEmpty()) {
            SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
            if (data == null) throw new IllegalStateException("Machine block data is unavailable");
            ItemStack selected = validOutputs.size() == 1 ? validOutputs.get(0)
                    : validOutputs.get(new Random().nextInt(validOutputs.size()));
            PendingMachineOutput.Store pending = pendingStore(data);
            PendingMachineOutput.stage(pending, selected);
            getMachineProcessor().endOperation(b);
            PendingMachineOutput.flush(pending, item -> menu.pushItem(item, getOutputSlots()));
        }
    }

    private static PendingMachineOutput.Store pendingStore(SlimefunBlockData data) {
        return new PendingMachineOutput.Store() {
            @Override public String read() { return data.getData(PENDING_OUTPUT_KEY); }
            @Override public void write(String value) { data.setData(PENDING_OUTPUT_KEY, value); }
            @Override public void clear() { data.removeData(PENDING_OUTPUT_KEY); }
        };
    }

    @Override
    protected void onBreak(BlockBreakEvent event, BlockMenu menu, Location location) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(location);
        if (data != null && data.getData(PENDING_OUTPUT_KEY) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Collect the pending output, or repair its saved record, before breaking this machine.");
            return;
        }
        super.onBreak(event, menu, location);
    }

    @Override
	public MachineRecipe findNextRecipe(BlockMenu menu) {

        int maxedSlots = 0;
        for (int slots : getOutputSlots()) {
            ItemStack item = menu.getItemInSlot(slots);
            if (item != null && item.getMaxStackSize() == item.getAmount()) {
                maxedSlots += 1;
            }
        }

        if (maxedSlots == getOutputSlots().length) { return null; }

        MachineRecipe recipe = recipes.get(0);

        return recipe;

    }


	@Override
	public List<ItemStack> getDisplayRecipes() {
	    List<ItemStack> display = new ArrayList<>();

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Output Items ⇩"),getGradientName("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientName("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s"),getGradientName("type")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Output Items ⇩"),getGradientName("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientName("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s"),getGradientName("type")));
        // 将输出物品数组中的所有物品添加到显示列表
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
