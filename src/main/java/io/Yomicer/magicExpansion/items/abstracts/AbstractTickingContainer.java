package io.Yomicer.magicExpansion.items.abstracts;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractTickingContainer extends AbstractContainer {

    protected AbstractTickingContainer(ItemGroup itemGroup, SlimefunItemStack item , RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        super.preRegister();

        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return AbstractTickingContainer.this.isSynchronized();
            }

            @Override
            public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                BlockMenu menu = data.getBlockMenu();
                if (menu != null) {
                    AbstractTickingContainer.this.tick(menu, b, data);
                }
            }

        });
    }

    /**
     * Data-aware ticker hook. The default keeps all existing machine behaviour intact while allowing
     * hot-path machines to reuse the already resolved Slimefun block data instead of looking it up again.
     */
    protected void tick(BlockMenu menu, Block b, SlimefunBlockData data) {
        tick(menu, b);
    }

    protected abstract void tick(BlockMenu menu, Block b);

    protected boolean isSynchronized() {
        return false;
    }

}
