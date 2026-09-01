package io.Yomicer.magicExpansion.items.misc;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;

/**
 * 雪魄珠: 寒江雪竿钓取的特殊钓物, 不可合成;
 * 获取方式通过自定义 RecipeType(寒江雪竿) 与配方(寒江雪鱼饵)展示。
 */
public class XuePaoZhu extends SlimefunItem {

    public XuePaoZhu(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }
}
