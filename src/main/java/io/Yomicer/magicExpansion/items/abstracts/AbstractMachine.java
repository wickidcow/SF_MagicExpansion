package io.Yomicer.magicExpansion.items.abstracts;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractMachine extends AbstractTickingContainer implements MachineProcessHolder<CraftingOperation>, RecipeDisplayItem {

    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this);

    protected AbstractMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        processor.setProgressBar(getProgressBar());
    }

    @Nonnull
    @Override
    public MachineProcessor<CraftingOperation> getMachineProcessor() {
        return processor;
    }

    @Nullable
    public abstract MachineRecipe findNextRecipe(BlockMenu menu);

    @Nonnull
    protected abstract ItemStack getProgressBar();

    protected boolean checkCraftPreconditions(Block b) {
        return true;
    }

    protected boolean onCraftFinish(BlockMenu menu, ItemStack[] ingredients) {
        return true;
    }

    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        for (ItemStack output: outputs) {
            if (output != null) {
                menu.pushItem(output.clone(), getOutputSlots());
            }
        }
    }

    /**
     * Refuse inventories which are too small for the machine's configured physical slots.
     * This also protects already-loaded legacy menus until they are recreated on restart.
     */
    protected boolean isMenuLayoutValid(BlockMenu menu) {
        int size = menu.toInventory().getSize();
        return slotsWithinMenu(size, getProgressSlot())
                && slotsWithinMenu(size, getInputSlots())
                && slotsWithinMenu(size, getOutputSlots());
    }

    static boolean slotsWithinMenu(int size, int... slots) {
        if (size <= 0) {
            return false;
        }
        for (int slot : slots) {
            if (slot < 0 || slot >= size) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the physical output reservation before a completed operation is charged.
     * Configured-output machines use null placeholders and manage their own pending batch.
     * Overrides must retain any result they cannot insert immediately.
     */
    protected boolean canCompleteOutputs(BlockMenu menu, ItemStack[] outputs) {
        if (!isMenuLayoutValid(menu)) {
            return false;
        }
        ItemStack[] physicalResults = java.util.Arrays.stream(outputs)
                .filter(item -> item != null && !item.getType().isAir())
                .toArray(ItemStack[]::new);
        return InvUtils.fitAll(menu.toInventory(), physicalResults, getOutputSlots());
    }

    @Override
    protected void onBreak(BlockBreakEvent e, BlockMenu menu, Location l) {
        super.onBreak(e, menu, l);

        // An older undersized menu must not throw while its block is being removed.
        int size = menu.toInventory().getSize();
        for (int slot : getInputSlots()) {
            if (slotsWithinMenu(size, slot)) {
                menu.dropItems(l, slot);
            }
        }
        for (int slot : getOutputSlots()) {
            if (slotsWithinMenu(size, slot)) {
                menu.dropItems(l, slot);
            }
        }

        processor.endOperation(e.getBlock());
    }

    @Override
    protected void tick(BlockMenu menu, Block b) {
        if (!isMenuLayoutValid(menu)) {
            // Preserve operation, stored outputs, and energy instead of throwing each tick.
            return;
        }
        CraftingOperation currentOp = processor.getOperation(b);

        if (currentOp != null) {
            if (currentOp.isFinished() && !canCompleteOutputs(menu, currentOp.getResults())) {
                // Cargo or a player may have filled the output while this operation
                // was processing. Keep the completed operation pending rather than
                // deleting its result or burning power while blocked.
                return;
            }

            if (checkCraftPreconditions(b)) {
                if(!currentOp.isFinished()) {
                    processor.updateProgressBar(menu, getProgressSlot(), currentOp);
                    currentOp.addProgress(1);
                } else {
                    menu.replaceExistingItem(getProgressSlot(), new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "));

                    boolean isFinished = onCraftFinish(menu, currentOp.getIngredients());
                    if (isFinished) {
                        addOutputs(menu, b, currentOp.getResults());
                    }

                    processor.endOperation(b);
                }
            }
        } else {
            MachineRecipe next = findNextRecipe(menu);

            if (next != null) {
                currentOp = new CraftingOperation(next);
                processor.startOperation(b, currentOp);

                processor.updateProgressBar(menu, getProgressSlot(), currentOp);
            }
        }
    }

    protected int getProgressSlot() {
        return 13;
    }
}
