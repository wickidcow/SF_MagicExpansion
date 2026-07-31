package io.Yomicer.magicExpansion.items.misc.moreLure;

import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MoreLure extends Lure {

    private final ItemStack item;
    private final String key;

    /**
     * 创建一个简单的鱼饵检测器
     * @param item 要检测的物品(如 MAGIC_SUGAR)
     * @param key  对应 lootTable 中的 key(如 "golden_lure")
     */
    public MoreLure(ItemStack item, String key) {
        this.item = item;
        this.key = key;
    }

    @Override
    public boolean hasLure(Player p) {
        for (ItemStack inventoryItem : p.getInventory().getContents()) {
            if (inventoryItem != null &&
                    SlimefunUtils.isItemSimilar(inventoryItem, item, true)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<WeightedItem> getLootPool() {
        return List.of();
    }

    @Override
    public ItemStack getItem() {
        return item.clone();
    }

}
