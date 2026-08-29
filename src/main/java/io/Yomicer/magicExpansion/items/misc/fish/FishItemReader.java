package io.Yomicer.magicExpansion.items.misc.fish;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FishItemReader {

    public static boolean isFishItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(FishKeys.FISH_TYPE, PersistentDataType.STRING);
    }

    public static Fish getFishType(ItemStack item) {
        if (!isFishItem(item)) return null;
        String typeName = item.getItemMeta().getPersistentDataContainer()
                .get(FishKeys.FISH_TYPE, PersistentDataType.STRING);
        // 修复(T)：valueOf 对非法/过期枚举名会抛 IllegalArgumentException，包 try-catch 失败返回 null（调用方需判空）
        try {
            return Fish.valueOf(typeName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public static double getFishWeight(ItemStack item) {
        if (!isFishItem(item)) return 0.0;
        // 修复(T)：权重字段缺失时 get 返回 null 会拆箱 NPE，安全返回默认值 0.0
        Double weight = item.getItemMeta().getPersistentDataContainer()
                .get(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE);
        return weight == null ? 0.0 : weight;
    }

    public static Fish.WeightRarity getWeightRarity(ItemStack item) {
        if (!isFishItem(item)) return null;
        String wrName = item.getItemMeta().getPersistentDataContainer()
                .get(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING);
        // 修复(T)：valueOf 对非法/缺失枚举名安全返回 null（调用方需判空处理）
        try {
            return Fish.WeightRarity.valueOf(wrName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

}
