package io.Yomicer.magicExpansion.Listener;

import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SlimefunRegistryFinalized implements Listener {



    // 预加载的 磨石 配方列表(静态常量)
    public static final List<Map<String, Integer>> SMELTERY_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> GRIND_STONE_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> ORE_CRUSHER_RECIPES = new ArrayList<>();

//    public static final List<Map<String, Integer>> ARMOR_FORGE_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> COMPRESSOR_RECIPES = new ArrayList<>();

    public static final List<Map<String, Integer>> PRESSURE_CHAMBER_RECIPES = new ArrayList<>();

//    public static final List<Map<String, Integer>> MAGIC_WORKBENCH_RECIPES = new ArrayList<>();

//    public static final List<Map<String, Integer>> ORE_WASHER_RECIPES = new ArrayList<>();

//    public static final List<Map<String, Integer>> AUTOMATED_PANNING_MACHINE_RECIPES = new ArrayList<>();




    //冶炼炉
    String ID1 = "SMELTERY";
    //磨石
    String ID2 = "GRIND_STONE";
    //粉碎机
    String ID3 = "ORE_CRUSHER";
    //盔甲锻造台
//    String ID4 = "ARMOR_FORGE";
    //压缩机
    String ID5 = "COMPRESSOR";
    //压力机
    String ID6 = "PRESSURE_CHAMBER";
    //魔法工作台
//    String ID7 = "MAGIC_WORKBENCH";




    @EventHandler
    public void onSlimefunRegistryFinalized(SlimefunItemRegistryFinalizedEvent event) {


        loadGrindStoneRecipes(ID1,SMELTERY_RECIPES);
        loadGrindStoneRecipes(ID2,GRIND_STONE_RECIPES);
        loadGrindStoneRecipes(ID3,ORE_CRUSHER_RECIPES);
//        loadGrindStoneRecipes(ID4,ARMOR_FORGE_RECIPES);
        loadGrindStoneRecipes(ID5,COMPRESSOR_RECIPES);
        loadGrindStoneRecipes(ID6,PRESSURE_CHAMBER_RECIPES);
//        loadGrindStoneRecipes(ID7,MAGIC_WORKBENCH_RECIPES);

        Debug.logInfo("load recipes");
//        Debug.logError("Grindstone recipe list:"+ GRIND_STONE_RECIPES);
    }


    /**
     * 获取 GrindStone 实例(或其他 MultiBlockMachine 实例)
     *
     * @return MultiBlockMachine 实例,如果找不到则返回 null
     */
    private static MultiBlockMachine getGrindStoneMachine(String id) {
        try {
            return (MultiBlockMachine) SlimefunItem.getById(id);
        } catch (Exception e) {
            Debug.logInfo("GRIND_STONE:" + e.getMessage());
            return null;
        }
    }

    /**
     * 加载磨石配方并存储到静态常量中
     */
    private static void loadGrindStoneRecipes(String id,List<Map<String, Integer>> recipes) {
        // 获取 GRIND_STONE 实例
        MultiBlockMachine machine = getGrindStoneMachine(id);
        if (machine == null) {
            Debug.logInfo("GRIND_STONE is not registered as a MultiBlockMachine.");
            return;
        }

        // 使用 getRecipeInputList 和 getRecipeOutputList 获取输入和输出物品
        List<ItemStack[]> inputList = getRecipeInputList(machine);
        List<ItemStack> outputList = getRecipeOutputList(machine);

        if (inputList == null || inputList.isEmpty() || outputList == null || outputList.isEmpty()) {
//            Debug.logInfo("Could not load any valid input or output item lists.");
//            System.out.println("Recipe type: "+machine);
            return;
        }

        // 确保输入和输出的配方数量一致
        if (inputList.size() != outputList.size()) {
//            Debug.logInfo("Input and output recipe counts do not match!");
            return;
        }

        int validRecipesCount = 0;

        for (int i = 0; i < inputList.size(); i++) {
            ItemStack[] input = inputList.get(i);
            ItemStack output = outputList.get(i);

            // 确保输入和输出都有效
            if (Arrays.stream(input).allMatch(item -> item == null || item.getType() == Material.AIR)) {
//                Debug.logInfo("Invalid input item: " + Arrays.toString(input));
                continue;
            }

            if (output == null || output.getType() == Material.AIR) {
//                Debug.logInfo("Invalid output item: " + output);
                continue;
            }

            // 创建配方结构
            Map<String, Integer> recipeMap = new LinkedHashMap<>();

            // 处理输入部分
            for (ItemStack requiredItem : input) {
                if (requiredItem == null || requiredItem.getType() == Material.AIR) {
                    continue; // 忽略空槽位
                }

                String itemKey = getUniqueItemKey(requiredItem); // 获取唯一键
                if (itemKey != null) {
                    recipeMap.put(itemKey, recipeMap.getOrDefault(itemKey, 0) + requiredItem.getAmount());
                } else {
//                    Debug.logInfo("Unrecognized input item: " + requiredItem);
                }
            }


            // 将整合后的配方存入列表
            recipes.add(recipeMap);
            validRecipesCount++;

            // 输出加载成功的配方
//            System.out.println("Successfully loaded recipe: " + recipeMap);
        }

//        System.out.println("Loaded " + validRecipesCount + " valid recipes.");
    }

    /**
     * 获取机器的所有输入物品列表
     *
     * @param machine MultiBlockMachine 实例
     * @return 所有输入物品的列表(每个元素是一个 ItemStack 数组)
     */
    public static List<ItemStack[]> getRecipeInputList(MultiBlockMachine machine) {
        if (machine == null) {
            return new ArrayList<>();
        }

        // 获取机器的配方列表
        List<ItemStack[]> recipes = machine.getRecipes();
        List<ItemStack[]> inputs = new ArrayList<>();

        // 遍历配方列表,提取输入物品(偶数索引位置)
        for (int i = 0; i < recipes.size(); i += 2) { // 每两个一组,第一个是输入
            inputs.add(recipes.get(i));
        }

        return inputs;
    }

    /**
     * 获取机器的所有输出物品列表
     *
     * @param machine MultiBlockMachine 实例
     * @return 所有输出物品的列表
     */
    public static List<ItemStack> getRecipeOutputList(MultiBlockMachine machine) {
        if (machine == null) {
            return new ArrayList<>();
        }

        // 获取机器的配方列表
        List<ItemStack[]> recipes = machine.getRecipes();
        List<ItemStack> outputs = new ArrayList<>();

        // 遍历配方列表,提取输出物品(奇数索引位置)
        for (int i = 1; i < recipes.size(); i += 2) { // 每两个一组,第二个是输出
            ItemStack[] outputArray = recipes.get(i);
            if (outputArray != null && outputArray.length > 0) {
                outputs.add(outputArray[0]); // 取第一个作为输出物品
            }
        }

        return outputs;
    }

    /**
     * 获取物品的唯一键(Slimefun ID 或 Minecraft 材质名称)
     *
     * @param item 物品
     * @return 唯一键("sf:<ID>" 或 "mc:<Material>"),如果无法识别则返回 null
     */
    public static String getUniqueItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        try {
            SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
            return slimefunItem != null ? "sf:" + slimefunItem.getId() : "mc:" + item.getType().name();
        } catch (Exception e) {
//            Debug.logInfo("Error while getting item unique key: " + e.getMessage());
            return null;
        }
    }


}
