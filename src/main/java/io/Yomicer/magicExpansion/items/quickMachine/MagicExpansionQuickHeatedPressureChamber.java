package io.Yomicer.magicExpansion.items.quickMachine;

import io.Yomicer.magicExpansion.utils.log.Debug;
import io.Yomicer.magicExpansion.utils.quickMachine.QuickMachineMBUtile;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static io.Yomicer.magicExpansion.Listener.SlimefunRegistryListener.HEATED_PRESSURE_CHAMBER_RECIPES;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.quickMachine.QuickMachineUtils.addAvailableRecipesToMenu;

public class MagicExpansionQuickHeatedPressureChamber extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public MagicExpansionQuickHeatedPressureChamber(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

//
//    // 预加载的 增强型合成台 配方列表(静态常量)
//    public static final List<Map<String, Integer>> HEATED_PRESSURE_CHAMBER_RECIPES = new ArrayList<>();
//
//    static {
//        // 在类加载时预加载所有 SMELTERY 类型的配方
//        List<SlimefunItem> smelteryItems = Slimefun.getRegistry().getAllSlimefunItems().stream()
//                .filter(item -> item.getRecipeType() == RecipeType.HEATED_PRESSURE_CHAMBER)
//                .toList();
//
//        for (SlimefunItem item : smelteryItems) {
//            ItemStack[] recipe = item.getRecipe();
//            Map<String, Integer> recipeMap = new HashMap<>();
//
//            // 遍历配方中的每个物品
//            for (ItemStack requiredItem : recipe) {
//                if (requiredItem == null || requiredItem.getType() == Material.AIR) {
//                    continue; // 忽略空槽位
//                }
//
//                int requiredAmount = requiredItem.getAmount();
//
//                // 判断是否为 SlimefunItem
//                String itemKey;
//                SlimefunItem slimefunItem = SlimefunItem.getByItem(requiredItem);
//                if (slimefunItem != null) {
//                    // 如果是 SlimefunItem,使用其 ID 作为键
//                    itemKey = "sf:" + slimefunItem.getId();
//                } else {
//                    // 如果不是 SlimefunItem,则默认为原版物品
//                    itemKey = "mc:" + requiredItem.getType().name();
//                }
//
//                // 整合相同物品的数量
//                recipeMap.put(itemKey, recipeMap.getOrDefault(itemKey, 0) + requiredAmount);
//            }
//
//            // 将整合后的配方存入列表
//            HEATED_PRESSURE_CHAMBER_RECIPES.add(recipeMap);
//        }
////        Debug.logWarn("Preloaded HEATED_PRESSURE_CHAMBER recipes: " + HEATED_PRESSURE_CHAMBER_RECIPES);
////        System.out.println("Number of preloaded ANCIENT_ALTAR recipes: " + ANCIENT_ALTAR_RECIPES.size());
//    }



    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e->{
            // 阻止默认行为(放置方块或使用物品)
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);

            // 获取玩家
            Player player = e.getPlayer();
            // 打开菜单并动态加载配方
            ChestMenu menu = new ChestMenu(getGradientName("Magic Heated Pressure Chamber"));
            addAvailableRecipesToMenu(player, menu,0, HEATED_PRESSURE_CHAMBER_RECIPES, RecipeType.HEATED_PRESSURE_CHAMBER);

            // 设置空槽位是否可点击
            menu.setEmptySlotsClickable(false);
            menu.setPlayerInventoryClickable(false);
            // 显示菜单给玩家
            menu.open(player);


        };
    }
}
