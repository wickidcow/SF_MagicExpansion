package io.Yomicer.magicExpansion.items.abstracts;

import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class MenuBlock extends SlimefunItem {
    @ParametersAreNonnullByDefault
    protected MenuBlock(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
            @ParametersAreNonnullByDefault
            public void onPlayerBreak(BlockBreakEvent e, ItemStack itemStack, List<ItemStack> list) {
                BlockMenu menu = BlockStorage.getInventory(e.getBlock());
                if (menu != null) {
                    MenuBlock.this.onBreak(e, menu);
                }

            }
        }, new BlockPlaceHandler(false) {
            @ParametersAreNonnullByDefault
            public void onPlayerPlace(BlockPlaceEvent e) {
                MenuBlock.this.onPlace(e, e.getBlockPlaced());
            }
        }});
    }

    public void postRegister() {
        new MenuBlockPreset(this);
    }

    @ParametersAreNonnullByDefault
    protected abstract void setup(BlockMenuPreset var1);

    @ParametersAreNonnullByDefault
    @Nonnull
    protected final int[] getTransportSlots(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        int[] var10000;
        switch (flow) {
            case INSERT -> var10000 = this.getInputSlots(menu, item);
            case WITHDRAW -> var10000 = this.getOutputSlots();
            default -> throw new IncompatibleClassChangeError();
        }

        return var10000;
    }

    @ParametersAreNonnullByDefault
    protected int[] getInputSlots(DirtyChestMenu menu, ItemStack item) {
        return this.getInputSlots();
    }

    protected abstract int[] getInputSlots();

    protected abstract int[] getOutputSlots();

    @ParametersAreNonnullByDefault
    protected void onNewInstance(BlockMenu menu, Block b) {
    }

    @ParametersAreNonnullByDefault
    protected void onBreak(BlockBreakEvent e, BlockMenu menu) {
        Location l = menu.getLocation();
        menu.dropItems(l, this.getInputSlots());
        menu.dropItems(l, this.getOutputSlots());
    }

    @ParametersAreNonnullByDefault
    protected void onPlace(BlockPlaceEvent e, Block b) {
    }


    private String PowerType = "MenuBlock";

    public final MenuBlock setPowerType(String powerType) {
        Preconditions.checkArgument(true, "The powerType must be not null");

        this.PowerType = powerType;
        return this;
    }

    public String getPowerType() {
        return PowerType;
    }


}
