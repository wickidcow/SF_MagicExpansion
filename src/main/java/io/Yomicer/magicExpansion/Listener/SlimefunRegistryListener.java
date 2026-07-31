package io.Yomicer.magicExpansion.Listener;

import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlimefunRegistryListener implements Listener {


    // 预加载的 增强型合成台 配方列表(静态常量)
    public static final List<Map<String, Integer>> ENHANCED_CRAFTING_TABLE_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> ARMOR_FORGE_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> ANCIENT_ALTAR_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> GOLD_PAN_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> HEATED_PRESSURE_CHAMBER_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> MAGIC_WORKBENCH_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> ORE_CRUSHER_RECIPES = new ArrayList<>();

    @EventHandler
    public void onSlimefunRegistryFinalized(SlimefunItemRegistryFinalizedEvent event) {

        loadRecipes(ENHANCED_CRAFTING_TABLE_RECIPES, RecipeType.ENHANCED_CRAFTING_TABLE);
        loadRecipes(ARMOR_FORGE_RECIPES, RecipeType.ARMOR_FORGE);
        loadRecipes(ANCIENT_ALTAR_RECIPES, RecipeType.ANCIENT_ALTAR);
        loadRecipes(GOLD_PAN_RECIPES, RecipeType.GOLD_PAN);
        loadRecipes(HEATED_PRESSURE_CHAMBER_RECIPES, RecipeType.HEATED_PRESSURE_CHAMBER);
        loadRecipes(MAGIC_WORKBENCH_RECIPES, RecipeType.MAGIC_WORKBENCH);
        loadRecipes(ORE_CRUSHER_RECIPES, RecipeType.ORE_CRUSHER);

    }

    private void loadRecipes(List<Map<String, Integer>> Recipes, RecipeType type) {

        // 在类加载时预加载所有 SMELTERY 类型的配方
        List<SlimefunItem> smelteryItems = Slimefun.getRegistry().getAllSlimefunItems().stream()
                .filter(item -> item.getRecipeType() == type)
                .toList();

        for (SlimefunItem item : smelteryItems) {
            ItemStack[] recipe = item.getRecipe();
            Map<String, Integer> recipeMap = new HashMap<>();

            // 遍历配方中的每个物品
            for (ItemStack requiredItem : recipe) {
                if (requiredItem == null || requiredItem.getType() == Material.AIR) {
                    continue; // 忽略空槽位
                }

                int requiredAmount = requiredItem.getAmount();

                // 判断是否为 SlimefunItem
                String itemKey;
                SlimefunItem slimefunItem = SlimefunItem.getByItem(requiredItem);
                if (slimefunItem != null) {
                    // 如果是 SlimefunItem,使用其 ID 作为键
                    itemKey = "sf:" + slimefunItem.getId();
                } else {
                    // 如果不是 SlimefunItem,则默认为原版物品
                    itemKey = "mc:" + requiredItem.getType().name();
                }

                // 整合相同物品的数量
                recipeMap.put(itemKey, recipeMap.getOrDefault(itemKey, 0) + requiredAmount);
            }

            // 将整合后的配方存入列表
            Recipes.add(recipeMap);
        }

    }

}
