package io.Yomicer.magicExpansion.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class Utils {

    //发光
    public static ItemStack doGlow(Object item) {
        // 如果传入的是 Material,则创建一个新的 ItemStack
        ItemStack result = (item instanceof Material)
                ? new ItemStack((Material) item)
                : (ItemStack) item;
        // 动态获取 Protection 附魔
        Enchantment protectionEnchant = Enchantment.getByKey(NamespacedKey.minecraft("luck_of_the_sea"));
        // 添加一个不安全的附魔(例如 Protection,等级为 1)
        result.addUnsafeEnchantment(protectionEnchant, 1);

        // 获取物品的元数据并隐藏附魔信息
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            result.setItemMeta(meta);
        }

        return result;
    }

    public static ItemStack doGlowDisplayEnchant(Object item) {
        // 如果传入的是 Material,则创建一个新的 ItemStack
        ItemStack result = (item instanceof Material)
                ? new ItemStack((Material) item)
                : (ItemStack) item;
        // 动态获取 Protection 附魔
        Enchantment protectionEnchant = Enchantment.getByKey(NamespacedKey.minecraft("luck_of_the_sea"));
        // 添加一个不安全的附魔(例如 Protection,等级为 1)
        result.addUnsafeEnchantment(protectionEnchant, 1);

        // 获取物品的元数据并隐藏附魔信息
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            result.setItemMeta(meta);
        }

        return result;
    }


}
