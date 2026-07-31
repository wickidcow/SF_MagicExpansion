package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ConvertItem {
    public static ItemStack BasicCreateItem(String selectedItem) {
        // 根据 ID 获取 Slimefun 物品
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);

        // 检查是否成功获取到 Slimefun 物品
        if (slimefunItem == null) {
            Debug.logInfo("Magic 1.0 is not installed; substituted an equivalent material. Original Magic 1.0 ID: "+ selectedItem);
            return MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC; // 返回一个初级
        }

        // 获取物品并创建 ItemStack
        ItemStack itemStack = new ItemStack(slimefunItem.getItem());

        return itemStack;
    }

    public static SlimefunItemStack createItem(String selectedItem) {
        // 根据 ID 获取 Slimefun 物品
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);

        if (slimefunItem == null) {
            Debug.logInfo("Could not find a Slimefun item with ID " + selectedItem + "!");
            return MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC; // 返回默认物品
        }

        // 正确返回 SlimefunItemStack
        return new SlimefunItemStack(
                selectedItem, // 唯一ID
                slimefunItem.getItem().clone()            // 克隆原始物品
        );
    }


    public static ItemStack AdvancedCreateItem(String selectedItem) {
        // 根据 ID 获取 Slimefun 物品
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);

        // 检查是否成功获取到 Slimefun 物品
        if (slimefunItem == null) {
            Debug.logInfo("Magic 1.0 is not installed; substituted an equivalent material. Original Magic 1.0 ID: "+ selectedItem);
            return MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED; // 返回一个进阶
        }

        // 获取物品并创建 ItemStack
        ItemStack itemStack = new ItemStack(slimefunItem.getItem());

        return itemStack;
    }

    public static ItemStack stoneCreateItem(String selectedItem) {
        // 根据 ID 获取 Slimefun 物品
        SlimefunItem slimefunItem = SlimefunItem.getById(selectedItem);

        // 检查是否成功获取到 Slimefun 物品
        if (slimefunItem == null) {
            Debug.logInfo("Could not find a Slimefun item with ID " + selectedItem + "!");
            Debug.logInfo("Using vanilla stone as a fallback.");
            return new ItemStack(Material.STONE); // 返回一个进阶
        }

        // 获取物品并创建 ItemStack
        ItemStack itemStack = new ItemStack(slimefunItem.getItem());

        return itemStack;
    }

    public static Boolean IfItemXist(String i) {
        // 根据 ID 获取 Slimefun 物品
        SlimefunItem slimefunItem = SlimefunItem.getById(i);
        // 检查是否成功获取到 Slimefun 物品
        if (slimefunItem == null) {
            Debug.logInfo("Could not find a Slimefun item with ID " + i + "; skipping registration.");
            return false;
        }
        return true;
    }






}
