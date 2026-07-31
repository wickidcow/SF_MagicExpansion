package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MagicExpansionSlimefunItemCache {
    // 静态缓存:存储所有 Slimefun 物品
    private static final List<SlimefunItem> SFITEMCACHE = new ArrayList<>();
    private static final Random RANDOM = new Random(); // 避免重复创建 Random

    static {
        // 在类加载时预加载所有 Slimefun 物品
        loadAllSlimefunItems();
    }

    /**
     * 加载所有 Slimefun 物品到缓存中
     */
    private static void loadAllSlimefunItems() {
        SFITEMCACHE.addAll(Slimefun.getRegistry().getAllSlimefunItems());
    }

    /**
     * 获取缓存中的所有 Slimefun 物品
     *
     * @return 所有 Slimefun 物品的列表
     */
    public static List<SlimefunItem> getAllSlimefunItems() {
        return SFITEMCACHE;
    }

    /**
     * 清空缓存并重新加载所有 Slimefun 物品
     */
    public static void reloadCache() {
        SFITEMCACHE.clear();
        loadAllSlimefunItems();
    }

    public static SlimefunItem getRandomSlimefunItem() {
        if (SFITEMCACHE.isEmpty()) {
            return null;
        }
        return SFITEMCACHE.get(RANDOM.nextInt(SFITEMCACHE.size()));
    }

    public static ItemStack getRandomItemStack() {
        SlimefunItem item = getRandomSlimefunItem();
        return item != null ? item.getItem().clone() : null;
    }
}
