package io.Yomicer.magicExpansion.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CreateItem {
    public static ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);

        //如果需要设置显示名称或其他元数据,可以使用 ItemMeta:
        var meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createItem(Material material) {
        return createItem(material, null); // 调用原始方法,不命名
    }

    public static ItemStack createItemWithLore(Material material, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null && lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

}
