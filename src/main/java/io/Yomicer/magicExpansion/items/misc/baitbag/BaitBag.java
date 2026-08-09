package io.Yomicer.magicExpansion.items.misc.baitbag;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * 饵料袋: 右键打开自绘菜单, 存放织梦者/水云间系列鱼饵
 */
public class BaitBag extends SimpleSlimefunItem<ItemUseHandler> {

    public BaitBag(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @Nonnull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(org.bukkit.event.Event.Result.DENY);
            e.setUseBlock(org.bukkit.event.Event.Result.DENY);
            BaitBagMenu.open(e.getPlayer(), e.getItem());
        };
    }
}
