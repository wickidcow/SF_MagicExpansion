package io.Yomicer.magicExpansion.items.quickMachine;

import io.Yomicer.magicExpansion.utils.quickMachine.QuickMachineMBUtile;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static io.Yomicer.magicExpansion.Listener.SlimefunRegistryFinalized.GRIND_STONE_RECIPES;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class MagicExpansionQuickGrindStone extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public MagicExpansionQuickGrindStone(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

//    // 预加载的 磨石 配方列表(静态常量)
//    private static final List<Map<String, Integer>> GRIND_STONE_RECIPES = new ArrayList<>();
//
//    /**
//     * 静态初始化块:在类加载时预加载所有 GRIND_STONE 类型的配方
//     */
//    static {
//        loadGrindStoneRecipes();
//    }
//
//    /**
//     * 加载所有 GRIND_STONE 类型的配方
//     */
//    private static void loadGrindStoneRecipes() {
//        // 获取 GrindStone 实例
//        String id = SlimefunItems.GRIND_STONE.getItemId();
//
//            MultiBlockMachine multiBlockMachine = (MultiBlockMachine) MultiBlockMachine.getById(id);
//
//            // 使用 getRecipes() 获取配方
//            List<ItemStack[]> recipes = multiBlockMachine.getRecipes();
//            if (recipes == null || recipes.isEmpty()) {
//                System.out.println("无法找到 GrindStone 的 recipes.");
//                return;
//            }
//
//            // 将配方转换为 Map 格式
//            for (ItemStack[] recipePair : recipes) {
//                if (recipePair == null || recipePair.length < 2) {
//                    continue; // 忽略无效的配方对
//                }
//
//                ItemStack input = recipePair[0];
//                ItemStack output = recipePair[1];
//
//                if (input == null || input.getType() == Material.AIR || output == null || output.getType() == Material.AIR) {
//                    continue; // 忽略无效的配方
//                }
//
//                Map<String, Integer> recipeMap = new HashMap<>();
//
//                // 输入物品
//                String inputKey = getUniqueItemKey(input);
//                if (inputKey != null) {
//                    recipeMap.put(inputKey, input.getAmount());
//                }
//
//                // 输出物品
//                String outputKey = getUniqueItemKey(output);
//                if (outputKey != null) {
//                    recipeMap.put(outputKey, -output.getAmount()); // 负数表示输出
//                }
//
//                if (!recipeMap.isEmpty()) {
//                    GRIND_STONE_RECIPES.add(recipeMap);
//                }
//            }
//    }
//
//    /**
//     * 获取物品的唯一键(Slimefun ID 或 Minecraft 材质名称)
//     *
//     * @param item 物品
//     * @return 唯一键("sf:<ID>" 或 "mc:<Material>"),如果无法识别则返回 null
//     */
//    private static String getUniqueItemKey(ItemStack item) {
//        if (item == null || item.getType() == Material.AIR) {
//            return null;
//        }
//
//        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
//        return slimefunItem != null ? "sf:" + slimefunItem.getId() : "mc:" + item.getType().name();
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
            ChestMenu menu = new ChestMenu(getGradientName("Magic Grind Stone"));
            QuickMachineMBUtile.addAvailableRecipesToMenu(player, menu,0, GRIND_STONE_RECIPES);

            // 设置空槽位是否可点击
            menu.setEmptySlotsClickable(false);
            menu.setPlayerInventoryClickable(false);
            // 显示菜单给玩家
            menu.open(player);


        };
    }

}
