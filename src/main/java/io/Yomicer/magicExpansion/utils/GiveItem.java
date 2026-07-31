package io.Yomicer.magicExpansion.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class GiveItem {
    public static void giveOrDropItem(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        Location location = player.getLocation();
        ItemStack item1 = item.clone();

        // 尝试将物品放入玩家背包
        Map<Integer, ItemStack> remainingItems = inventory.addItem(item1);

        // 如果背包已满,掉落剩余的物品到地上
        if (!remainingItems.isEmpty()) {
            for (ItemStack remaining : remainingItems.values()) {
                dropItemInBatches(player, location, remaining);
            }
        }
    }

    /**
     * 分批掉落物品(单次最大处理 127 个)
     *
     * @param player   玩家
     * @param location 掉落位置
     * @param item     需要掉落的物品
     */
    private static void dropItemInBatches(Player player, Location location, ItemStack item) {
        int totalAmount = item.getAmount();
        int maxDropSize = 64; // 单次最大掉落数量

        while (totalAmount > 0) {
            // 创建一个新的物品副本,设置当前批次的数量
            ItemStack dropItem = item.clone();
            int currentBatchSize = Math.min(totalAmount, maxDropSize);
            dropItem.setAmount(currentBatchSize);

            // 掉落当前批次的物品
            player.getWorld().dropItemNaturally(location, dropItem);

            // 更新剩余数量
            totalAmount -= currentBatchSize;
        }
    }

}
