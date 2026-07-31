package io.Yomicer.magicExpansion.items.misc.fish.fishInterface;

import org.bukkit.inventory.ItemStack;

public class FishManager {

    /**
     * 快速获取鱼类ItemStack
     */
    public static ItemStack getFishItem(BaseFish fish) {
        return fish.copy().build();
    }

    /**
     * 获取带自定义Lore的鱼类
     */
    public static ItemStack getFishItemWithLore(BaseFish fish, String... loreLines) {
        return fish.copy().addLore(loreLines).build();
    }

    /**
     * 获取图鉴专用的鱼类(自动添加图鉴信息)
     */
    public static ItemStack getFishItemForGuide(BaseFish fish) {
        return fish.addLore("", "§7This guide entry is for display only.", "§8— Shows uses and obtainment requirements only —").build();
    }

}
