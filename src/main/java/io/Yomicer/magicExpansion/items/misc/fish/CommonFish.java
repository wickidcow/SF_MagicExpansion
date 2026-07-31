package io.Yomicer.magicExpansion.items.misc.fish;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CommonFish extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public CommonFish(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {

            // 阻止默认行为(放置方块或使用物品)
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            ItemStack item = e.getItem();

            if (!FishItemReader.isFishItem(item)) {
                return; // 不是鱼物品,直接返回
            }

            Player player = e.getPlayer();
            Fish fish = FishItemReader.getFishType(item);

            if (fish == null) {
                player.sendMessage("§cCould not determine this fish type.");
                return;
            }

            double weight = FishItemReader.getFishWeight(item);
            if (weight <= 0) {
                player.sendMessage("§cThis fish has invalid weight data.");
                return;
            }
            Fish.WeightRarity wr = FishItemReader.getWeightRarity(item);
            // 正常显示信息
            player.sendMessage("§6You are holding a §e" + fish.getDisplayName() + "§6.");
            player.sendMessage("§7Weight: §f" + String.format("%.3f", weight) + "kg");
            if (wr != null) {
                player.sendMessage("§7Weight Rarity: " + wr.getColorCode() + wr.getDisplayName());
            }

        };
    }

    /**
     * 安全检查:该物品是否为有效的鱼物品
     */
    public static boolean isFishItem(ItemStack item) {
        if (item == null) return false;
        if (item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.hasDisplayName()) return false; // 可选:检查是否有名字
        var container = meta.getPersistentDataContainer();
        return container.has(FishKeys.FISH_TYPE, PersistentDataType.STRING);
    }
    /**
     * 安全获取鱼的类型
     * @return 对应的 Fish 枚举,失败返回 null
     */
    public static Fish getFishType(ItemStack item) {
        if (!isFishItem(item)) return null;

        try {
            ItemMeta meta = item.getItemMeta();
            String typeName = meta.getPersistentDataContainer()
                    .get(FishKeys.FISH_TYPE, PersistentDataType.STRING);

            if (typeName == null || typeName.trim().isEmpty()) {
                return null;
            }

            return Fish.valueOf(typeName.trim());
        } catch (IllegalArgumentException e) {
            // 枚举值不存在(比如插件更新后旧物品残留)
            return null;
        } catch (Exception e) {
            // 其他意外异常(如容器损坏)
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 安全获取鱼的重量
     * @return 重量(kg),失败返回 0.0
     */
    public static double getFishWeight(ItemStack item) {
        if (!isFishItem(item)) return 0.0;

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return 0.0;

            Double weight = meta.getPersistentDataContainer()
                    .get(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE);

            // 检查是否为有效数值
            if (weight == null || Double.isNaN(weight) || weight < 0) {
                return 0.0;
            }

            // 可选:检查是否在该鱼种的合理范围内
            Fish fish = getFishType(item);
            if (fish != null) {
                if (weight < fish.getMinWeight() || weight > fish.getMaxWeight() * 2) {
                    // 允许略微超出(比如奖励加成),但不能离谱
                    return 0.0;
                }
            }

            return weight;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    /**
     * 安全获取重量稀有度
     * @return WeightRarity 枚举,失败返回 COMMON_FISH
     */
    public static Fish.WeightRarity getWeightRarity(ItemStack item) {
        if (!isFishItem(item)) {
            return Fish.WeightRarity.COMMON_FISH; // 默认值
        }

        try {
            ItemMeta meta = item.getItemMeta();
            String wrName = meta.getPersistentDataContainer()
                    .get(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING);

            if (wrName == null || wrName.trim().isEmpty()) {
                // 如果没有存储,根据当前重量重新计算
                double weight = getFishWeight(item);
                Fish fish = getFishType(item);
                if (fish != null && weight > 0) {
                    return fish.getWeightRarity(weight);
                }
                return Fish.WeightRarity.COMMON_FISH;
            }

            return Fish.WeightRarity.valueOf(wrName.trim());
        } catch (IllegalArgumentException e) {
            // 枚举值不存在,重新计算
            double weight = getFishWeight(item);
            Fish fish = getFishType(item);
            if (fish != null && weight > 0) {
                return fish.getWeightRarity(weight);
            }
            return Fish.WeightRarity.COMMON_FISH;
        } catch (Exception e) {
            e.printStackTrace();
            return Fish.WeightRarity.COMMON_FISH;
        }
    }

}
