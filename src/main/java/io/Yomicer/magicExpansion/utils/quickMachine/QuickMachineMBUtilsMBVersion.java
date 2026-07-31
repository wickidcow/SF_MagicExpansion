package io.Yomicer.magicExpansion.utils.quickMachine;

import io.Yomicer.magicExpansion.utils.CreateItem;
import io.Yomicer.magicExpansion.utils.MagicExpansionSlimefunItemCache;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.Yomicer.magicExpansion.Listener.SlimefunRegistryFinalized.getUniqueItemKey;
import static io.Yomicer.magicExpansion.utils.GiveItem.giveOrDropItem;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;
import static io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils.isItemSimilar;

public class QuickMachineMBUtilsMBVersion {
    private static final int[] outputSlots = {35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1};


    /**
     * 静态方法:推送物品到指定槽位,并返回剩余物品(如果有).
     *
     * @param item  要推送的物品
     * @param slots 目标槽位
     * @param menu  当前菜单实例
     * @return 剩余未放置的物品(如果没有剩余则返回 null)
     */
    public static ItemStack pushItemRe(ItemStack item, int[] slots, DirtyChestMenu menu) {
        if (item == null || item.getType() == Material.AIR) {
            throw new IllegalArgumentException("Cannot push null or AIR");
        }

        // 获取物品的最大堆栈大小
        int maxStackSize = Math.min(item.getMaxStackSize(), menu.toInventory().getMaxStackSize());
        int totalAmount = item.getAmount(); // 总共需要放置的物品数量

        ItemStackWrapper wrapper = null;

        for (int slot : slots) {
            if (totalAmount <= 0) {
                break; // 如果已经放置完所有物品,退出循环
            }

            ItemStack stack = menu.getItemInSlot(slot);

            if (stack == null) {
                // 如果槽位为空,直接放置物品
                int amountToPlace = Math.min(totalAmount, maxStackSize);
                ItemStack newItem = item.clone();
                newItem.setAmount(amountToPlace);
                menu.replaceExistingItem(slot, newItem);
                totalAmount -= amountToPlace;
            } else {
                // 检查是否可以堆叠
                if (wrapper == null) {
                    wrapper = ItemStackWrapper.wrap(item);
                }

                if (SlimefunItem.getByItem(item) != null) {
                    if (!SlimefunUtils.isItemSimilar(stack, wrapper, true, false)) {
                        continue; // 物品类型不匹配,跳过该槽位
                    }
                } else if (!ItemUtils.canStack(wrapper, stack)) {
                    continue; // 无法堆叠,跳过该槽位
                }

                // 计算槽位剩余空间
                int spaceLeft = stack.getMaxStackSize() - stack.getAmount();
                if (spaceLeft > 0) {
                    int toAdd = Math.min(spaceLeft, totalAmount);
                    stack.setAmount(stack.getAmount() + toAdd);
                    totalAmount -= toAdd;

                    // 更新槽位内容
                    menu.replaceExistingItem(slot, stack);
                }
            }
        }

        // 返回剩余物品(如果有)
        return totalAmount > 0 ? new CustomItemStack(item, totalAmount) : null;
    }

    private static boolean consumeMaterials(Player player, Map<String, Integer> recipe, int amount,BlockMenu menu) {

        for (Map.Entry<String, Integer> requiredEntry : recipe.entrySet()) {
            String requiredKey = requiredEntry.getKey();
            int requiredAmountPerCraft = requiredEntry.getValue();
            int totalRequiredAmount = requiredAmountPerCraft * amount;

            if (requiredKey.startsWith("mc:")) {
                // 原版物品
                Material material = Material.matchMaterial(requiredKey.substring(3));
                if (material != null) {
                    ItemStack targetItem = new ItemStack(material);
                    if (!removeVanillaItemsFromMenu(menu, targetItem, totalRequiredAmount)) {
                        player.sendMessage("§cCould not remove enough vanilla materials. Check the machine.");
                        return true;
                    }
                }
            } else if (requiredKey.startsWith("sf:")) {
                // Slimefun 物品
                String slimefunId = requiredKey.substring(3);
                SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);
                if (slimefunItem != null) {
                    ItemStack targetItem = slimefunItem.getItem().clone();
                    if (!removeSlimefunItemsFromMenu(menu, targetItem, totalRequiredAmount)) {
                        player.sendMessage("§cCould not remove enough Slimefun materials. Check the machine.");
                        return true;
                    }
                }
            }
        }

        // 所有材料都已成功移除
        return false;
    }

    /**
     * 从玩家背包中移除指定数量的原版物品
     *
     * @param menu 玩家背包
     * @param targetItem 要移除的物品
     * @param amount     要移除的数量
     * @return 是否成功移除了指定数量的物品
     */
    private static boolean removeVanillaItemsFromMenu(BlockMenu menu, ItemStack targetItem, int amount) {
        int remainingAmount = amount; // 剩余需要移除的数量

        // 遍历 BlockMenu 的所有槽位
        for (int slot = 0; slot < menu.getSize() && remainingAmount > 0; slot++) {
            ItemStack itemInSlot = menu.getItemInSlot(slot); // 获取当前槽位的物品

            // 检查槽位中是否有符合条件的物品
            if (itemInSlot != null && itemInSlot.isSimilar(targetItem)) {
                int removed = Math.min(itemInSlot.getAmount(), remainingAmount); // 计算可以移除的数量

                // 更新槽位中的物品数量
                if (removed >= itemInSlot.getAmount()) {
                    menu.replaceExistingItem(slot, null); // 如果全部移除,则清空槽位
                } else {
                    itemInSlot.setAmount(itemInSlot.getAmount() - removed); // 减少物品数量
                    menu.replaceExistingItem(slot, itemInSlot); // 更新槽位内容
                }

                remainingAmount -= removed; // 更新剩余需要移除的数量
            }
        }

        return remainingAmount == 0; // 如果剩余数量为 0,表示成功移除所有物品
    }

    /**
     * 从玩机器背包中移除指定数量的 Slimefun 物品
     *
     * @param menu 机器背包
     * @param targetItem 要移除的物品
     * @param amount     要移除的数量
     * @return 是否成功移除了指定数量的物品
     */
    private static boolean removeSlimefunItemsFromMenu(BlockMenu menu, ItemStack targetItem, int amount) {
        int remainingAmount = amount; // 剩余需要移除的数量

        // 遍历 BlockMenu 的所有槽位
        for (int slot = 0; slot < menu.getSize() && remainingAmount > 0; slot++) {
            ItemStack itemInSlot = menu.getItemInSlot(slot); // 获取当前槽位的物品

            // 检查槽位中是否有符合条件的 Slimefun 物品
            if (itemInSlot != null && isItemSimilar(itemInSlot, targetItem, false)) {
                int removed = Math.min(itemInSlot.getAmount(), remainingAmount); // 计算可以移除的数量

                // 更新槽位中的物品数量
                if (removed >= itemInSlot.getAmount()) {
                    menu.replaceExistingItem(slot, null); // 如果全部移除,则清空槽位
                } else {
                    itemInSlot.setAmount(itemInSlot.getAmount() - removed); // 减少物品数量
                    menu.replaceExistingItem(slot, itemInSlot); // 更新槽位内容
                }

                remainingAmount -= removed; // 更新剩余需要移除的数量
            }
        }

        return remainingAmount == 0; // 如果剩余数量为 0,表示成功移除所有物品
    }



    /**
     * 整合配方材料,合并相同物品
     * 将配方数组转换为 Map
     *
     * @param recipe 配方数组
     * @return 配方 Map
     */
    private static Map<String, Integer> convertRecipeToMap(ItemStack[] recipe) {
        Map<String, Integer> recipeMap = new LinkedHashMap<>();

        // 处理输入部分
        for (ItemStack input : recipe) {
            if (input == null || input.getType() == Material.AIR) {
                continue; // 忽略空槽位
            }

            String itemKey = getUniqueItemKey(input); // 获取唯一键
            if (itemKey != null) {
                recipeMap.put(itemKey, recipeMap.getOrDefault(itemKey, 0) + input.getAmount()); // 统一为正数
            }
        }

        return recipeMap;
    }


    /**
     * 输出粘液物品名字
     * 根据物品类型获取显示名称
     *
     * @param item 物品
     * @return 显示名称
     */
    private static String getDisplayName(ItemStack item) {
        Material material = item.getType();
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);

        if (slimefunItem != null) {
            // 如果是 SlimefunItem,返回其 ID
            return slimefunItem.getItemName();
        } else {
            // 如果是原版物品,返回其英文名称
            return material.name();
        }
    }

    /**
     * 输出所需配方材料名称,汉化原本物品+粘液名称
     * 获取配方材料显示名称
     * 根据物品键获取显示名称
     *
     * @param itemKey 物品键
     * @return 显示名称
     */
    private static String getIngredientDisplayName(String itemKey) {
        if (itemKey.startsWith("mc:")) {
            // 如果是原版物品,提取 Material 名称并返回
            String materialName = itemKey.substring(3); // 去掉前缀 "mc:"
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                // 创建一个临时的 ItemStack
                ItemStack itemStack = new ItemStack(material);
                return ItemStackHelper.getName(itemStack);
            }
            return "item (" + materialName + ")";
        } else if (itemKey.startsWith("sf:")) {
            // 如果是 Slimefun 物品,提取 ID 并查找对应的 SlimefunItem
            String slimefunId = itemKey.substring(3); // 去掉前缀 "sf:"
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);
            if (slimefunItem != null) {
                return slimefunItem.getItemName(); // 返回 Slimefun 物品的显示名称
            }
            return "Slimefun item (" + slimefunId + ")";
        } else {
            return "Item type (" + itemKey + ")";
        }
    }



    /**
     * 计算玩家最多可以合成的数量
     */
    private static int calculateMaxCraftableAmount(Map<String, Integer> playerItems, Map<String, Integer> recipe) {
        int maxCraftableAmount = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> requiredEntry : recipe.entrySet()) {
            String requiredKey = requiredEntry.getKey();
            int requiredAmount = requiredEntry.getValue();

            int availableAmount = playerItems.getOrDefault(requiredKey, 0);
            maxCraftableAmount = Math.min(maxCraftableAmount, availableAmount / requiredAmount);
        }

        return maxCraftableAmount;
    }


    /**
     * 获取机器背包中的所有物品及其数量
     * 并且给一个键
     *
     * @param menu 容器
     * @return 一个 Map,键为物品类型或 SlimefunItem ID,值为该物品的数量
     */
    private static Map<String, Integer> getInventoryItems(BlockMenu menu) {
        Map<String, Integer> itemCounts = new HashMap<>();

        // 遍历槽位 1 到 26
        for (int slot = 0; slot <= 35; slot++) {
            ItemStack item = menu.getItemInSlot(slot); // 获取槽位中的物品

            if (item != null && item.getType() != Material.AIR) {
                // 判断是否为 SlimefunItem
                String itemKey;
                SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
                if (slimefunItem != null) {
                    // 如果是 SlimefunItem,使用其 ID 作为键
                    itemKey = "sf:" + slimefunItem.getId();
                } else {
                    // 如果不是 SlimefunItem,则默认为原版物品
                    itemKey = "mc:" + item.getType().name();
                }

                // 整合菜单中的物品数量
                itemCounts.put(itemKey, itemCounts.getOrDefault(itemKey, 0) + item.getAmount());
            }
        }

        return itemCounts;
    }



    /**
     * 检查玩家是否有足够的物品来制作配方
     *
     * @param playerItems 机器背包中的所有物品及其数量
     * @param recipe      配方
     * @param amount      合成数量
     * @return 如果玩家有足够的物品,则返回 true,否则返回 false
     */
    private static boolean hasEnoughMaterials(Map<String, Integer> playerItems, Map<String, Integer> recipe, int amount) {
        // 检查玩家是否有足够的物品
        for (Map.Entry<String, Integer> requiredEntry : recipe.entrySet()) {
            String requiredKey = requiredEntry.getKey();
            int requiredAmount = requiredEntry.getValue() * amount; // 根据合成数量调整所需材料

            int availableAmount = playerItems.getOrDefault(requiredKey, 0);
            if (availableAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    // 默认 amount=1 的重载方法
    private static boolean hasEnoughMaterials(Map<String, Integer> playerItems, Map<String, Integer> recipe) {
        return hasEnoughMaterials(playerItems, recipe, 1); // 调用带 amount 参数的方法,默认值为 1
    }


    /**
     * 根据目标配方获取对应的输出物品和数量
     *
     * @param targetRecipe 目标配方(包含输入和输出)
     * @return 匹配的输出物品和数量(Pair<ItemStack, Integer>),如果未找到匹配则返回 null
     */
    private static Pair<ItemStack, Integer> getRecipeResultWithAmount(Map<String, Integer> targetRecipe) {
        for (SlimefunItem slimefunItem : MagicExpansionSlimefunItemCache.getAllSlimefunItems()) {
            // 检查是否为 AContainer 类型
            if (slimefunItem instanceof AContainer aContainer) {
                // 获取机器的所有配方
                List<MachineRecipe> machineRecipes = aContainer.getMachineRecipes();

                // 遍历每个 MachineRecipe
                for (MachineRecipe machineRecipe : machineRecipes) {
                    ItemStack[] input = machineRecipe.getInput(); // 获取输入物品数组
                    ItemStack[] output = machineRecipe.getOutput(); // 获取输出物品数组

                    // 将输入和输出合并为一个完整的配方 Map
                    Map<String, Integer> recipeMap = new LinkedHashMap<>();
                    recipeMap.putAll(convertRecipeToMap(input)); // 输入部分

                    // 检查目标配方是否完全匹配当前配方
                    boolean isMatch = targetRecipe.equals(recipeMap);

                    if (isMatch) {
                        // 如果匹配,从输出部分提取第一个物品及其数量
                        ItemStack result = output != null && output.length > 0 ? output[0] : null;
                        int outputAmount = result != null ? result.getAmount() : 1; // 默认数量为 1

                        // 打印完整的配方信息
//                        System.out.println("Recipe matched: recipe=" + recipeMap);
//                        System.out.println("Output item: " + result);

                        return new Pair<>(result, outputAmount);
                    } else {
                        // 打印不匹配的日志
//                        System.out.println("Recipe mismatch: current recipe=" + recipeMap + " vs target recipe=" + targetRecipe);
                    }
                }
            }
        }
        return null; // 如果未找到匹配的物品,返回 null
    }


    // 用于存储每个 BlockMenu 的当前页码
    private static final Map<BlockMenu, Integer> currentPageMap = new LinkedHashMap<>();
    /**
     * 将符合条件的配方添加到菜单中,并支持分页
     * 刷新逻辑
     *
     * @param menu   菜单对象
     */
    public static void addAvailableRecipesToMenu(BlockMenu menu, List<Map<String, Integer>> receivedRecipes) {
        // 获取当前页码(如果没有设置,默认为第一页)
        int currentPage = currentPageMap.getOrDefault(menu, 0);

        // 获取容器中的所有物品及其数量
        Map<String, Integer> chestItems = getInventoryItems(menu);

        // 清空菜单内容
        for (int i = 36; i < 45; i++) {
            menu.addItem(i, new CustomItemStack(doGlow(Material.BARRIER), "&a"), (player, slot, item, action) -> false);
        }
        menu.addItem(45, new CustomItemStack(doGlow(Material.BARRIER), "&a"), (player, slot, item, action) -> false);
        menu.addItem(53, new CustomItemStack(doGlow(Material.BARRIER), "&a"), (player, slot, item, action) -> false);

        // 添加说明按钮
        menu.addItem(49, new CustomItemStack(doGlow(Material.NETHER_STAR), "§x§F§D§B§7§D§4",
                        "§x§F§D§B§7§D§4§l refresh",
                        "§eRight-click: §bView required materials",
                        "§eLeft-click: §bCraft one batch",
                        "§eShift-right-click: §bCraft 32 batches",
                        "§eShift-left-click: §bCraft all possible batches",
                        "§7Slots 1–4 are used for machine input and output."),
                (player1, slot, item, action) -> {
                    addAvailableRecipesToMenu(menu, receivedRecipes);
                    return false;
                });

        // 筛选出玩家可以制作的配方
        List<Map<String, Integer>> availableRecipes = receivedRecipes.stream()
                .filter(recipe -> hasEnoughMaterials(chestItems, recipe))
                .toList();

        // 分页逻辑
        int itemsPerPage = 9; // 每页显示的最大数量
        int totalPages = (int) Math.ceil((double) availableRecipes.size() / itemsPerPage);

        // 检查当前页码是否超出最大页数
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1); // 如果超出,重置为最后一页
            currentPageMap.put(menu, currentPage); // 更新当前页码
        }

        // 确保当前页码在有效范围内
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        // 当前页的配方
        List<Map<String, Integer>> currentPageRecipes = availableRecipes.stream()
                .skip((long) currentPage * itemsPerPage)
                .limit(itemsPerPage)
                .toList();

        // 添加配方到菜单中
        int slot = 36;
        for (Map<String, Integer> recipe : currentPageRecipes) {
            Pair<ItemStack, Integer> recipeResult = getRecipeResultWithAmount(recipe);
            if (recipeResult == null || recipeResult.getFirstValue() == null) {
                continue;
            }

            ItemStack resultItem = recipeResult.getFirstValue().clone(); // 提取产物并克隆以避免修改原对象
            ItemStack resultItemDisplay = recipeResult.getFirstValue().clone(); // 提取产物并克隆以避免修改原对象
            // 假设 resultItemDisplay 是已经克隆的 ItemStack 对象
            if (resultItemDisplay.hasItemMeta()) { // 确保 ItemStack 不为空且有 ItemMeta
                ItemMeta itemMeta = resultItemDisplay.getItemMeta(); // 获取 ItemMeta
//                    String originalName = itemMeta.getDisplayName(); // 获取原始显示名称
//                    String newName = "§x§F§D§B§7§D§4"+originalName + "§e [Output]"; // 添加后缀 [产物]
//                    itemMeta.setDisplayName(newName); // 设置新的显示名称
                    // 准备 Lore 列表
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    // 添加"Required Materials"标题
                    lore.add("§aRequired materials:");
                    // 添加每种材料
                    for (Map.Entry<String, Integer> ingredient : recipe.entrySet()) {
                        String displayName = getIngredientDisplayName(ingredient.getKey());
                        int requiredAmount = ingredient.getValue();
                        lore.add(" §e- " + displayName + " x " + requiredAmount);
                    }
                    // 将新的 Lore 设置到 ItemMeta
                    itemMeta.setLore(lore);
                    resultItemDisplay.setItemMeta(itemMeta); // 将修改后的 ItemMeta 应用回 ItemStack
            }else{
//                String name = "§x§F§D§B§7§D§4"+ItemStackHelper.getName(resultItemDisplay);
//                String newName = name + "§e [Output]";
                ItemMeta meta = Bukkit.getItemFactory().getItemMeta(resultItemDisplay.getType());
//                itemMeta.setDisplayName(newName);
                // 准备 Lore 列表
                List<String> lore = new ArrayList<>();
                lore.add("");
                // 添加"Required Materials"标题
                lore.add("§aRequired materials:");
                // 添加每种材料
                for (Map.Entry<String, Integer> ingredient : recipe.entrySet()) {
                    String displayName = getIngredientDisplayName(ingredient.getKey());
                    int requiredAmount = ingredient.getValue();
                    lore.add(" §e- " + displayName + " x " + requiredAmount);
                }
                // 将新的 Lore 设置到 ItemMeta
                meta.setLore(lore);
                resultItemDisplay.setItemMeta(meta);
            }
            int outputAmountPerCraft = recipeResult.getSecondValue(); // 提取单次合成数量

            // 添加产物到菜单
            menu.addItem(slot++, resultItemDisplay, (p, s, item, action) -> {
                if (action.isRightClicked() && !action.isShiftClicked()) {
                    // 右键点击:显示制作一份所需的材料
                    p.sendMessage("§aRecipe: §b" + getDisplayName(resultItem));
                    p.sendMessage("§aRequired materials:");
                    for (Map.Entry<String, Integer> ingredient : recipe.entrySet()) {
                        String displayName = getIngredientDisplayName(ingredient.getKey());
                        int requiredAmount = ingredient.getValue();
                        p.sendMessage(" - §e" + displayName + " x " + requiredAmount);
                    }
                } else if (!action.isRightClicked() && !action.isShiftClicked()) {
                    // 左键点击:消耗一份材料并给予一份产品
                    if (consumeMaterials(p, recipe, 1, menu)) {
                        p.sendMessage("§cNot enough materials, or the recipe data is invalid.");
                        return false;
                    }

                    // 克隆结果物品
                    ItemStack resultItemClone = resultItem.clone();

                    // 调用 pushItem 方法,尝试将物品放入槽位
                    ItemStack remainingItems = pushItemRe(resultItemClone, outputSlots,menu);

                    // 检查是否有未放置的物品
                    if (remainingItems != null && remainingItems.getAmount() > 0) {
                        // 如果槽位放不下,给予玩家或掉落剩余物品
                        giveOrDropItem(p, new CustomItemStack(remainingItems, remainingItems.getAmount()));
                    }
                    p.sendMessage("§aSuccessfully crafted §b" + outputAmountPerCraft + " §a× §b" + getDisplayName(resultItem) + "§a.");
                    addAvailableRecipesToMenu(menu, receivedRecipes); // 刷新菜单
                } else if (!action.isRightClicked() && action.isShiftClicked()) {
                    // Shift + 左键点击:批量合成
                    int maxCraftable = calculateMaxCraftableAmount(chestItems, recipe);
                    if (maxCraftable > 52000){
                        maxCraftable = 52000;
                    }
                    if (maxCraftable <= 0) {
                        p.sendMessage("§cNot enough materials for bulk crafting.");
                        return false;
                    }

                    if (consumeMaterials(p, recipe, maxCraftable,menu)) {
                        p.sendMessage("§cNot enough materials, or the recipe data is invalid.");
                        return false;
                    }

                    // 计算总输出量
                    int totalOutput = maxCraftable * outputAmountPerCraft;

                    // 克隆结果物品并设置数量为总输出量
                    ItemStack resultItemClone = resultItem.clone();
                    resultItemClone.setAmount(totalOutput);

                    // 调用 pushItem 方法,尝试将物品放入槽位
                    ItemStack remainingItems = pushItemRe(resultItemClone, outputSlots,menu);;

                    // 检查是否有未放置的物品
                    if (remainingItems != null && remainingItems.getAmount() > 0) {
                        // 如果槽位放不下,给予玩家或掉落剩余物品
                        giveOrDropItem(p, remainingItems);
                    }
                    p.sendMessage("§aSuccessfully crafted §b" + totalOutput + " §a× §b" + getDisplayName(resultItem) + "§a.");
                    addAvailableRecipesToMenu(menu, receivedRecipes); // 刷新菜单
                } else if (action.isRightClicked() && action.isShiftClicked()) {
                    // Shift + 右键点击:固定合成 32 份
                    int requiredCrafts = 32;
                    if (!hasEnoughMaterials(chestItems, recipe, requiredCrafts)) {
                        p.sendMessage("§cNot enough materials to craft 32 batches.");
                        return false;
                    }

                    if (consumeMaterials(p, recipe, requiredCrafts,menu)) {
                        p.sendMessage("§cNot enough materials, or the recipe data is invalid.");
                        return false;
                    }

                    // 计算总输出量
                    int totalOutput = requiredCrafts * outputAmountPerCraft;

                    // 克隆结果物品并设置数量为总输出量
                    ItemStack resultItemClone = resultItem.clone();
                    resultItemClone.setAmount(totalOutput);

                    // 调用 pushItem 方法,尝试将物品放入槽位
                    ItemStack remainingItems = pushItemRe(resultItemClone, outputSlots,menu);;

                    // 检查是否有未放置的物品
                    if (remainingItems != null && remainingItems.getAmount() > 0) {
                        // 如果槽位放不下,给予玩家或掉落剩余物品
                        giveOrDropItem(p, remainingItems);
                    }
                    p.sendMessage("§aSuccessfully crafted §b" + totalOutput + " §a× §b" + getDisplayName(resultItem) + "§a.");
                    addAvailableRecipesToMenu(menu, receivedRecipes); // 刷新菜单
                }
                return false;
            });
        }

        // 添加分页按钮
        if (currentPage > 0) {
            // 上一页按钮
            final int previousPage = currentPage - 1; // 使用局部变量存储上一页的页码
            menu.addItem(45, CreateItem.createItem(Material.ARROW, "§aPrevious Page"), (p, s, item, action) -> {
                currentPageMap.put(menu, previousPage); // 更新当前页码
                addAvailableRecipesToMenu(menu, receivedRecipes);
                return false;
            });
        }

        if (currentPage < totalPages - 1) {
            // 下一页按钮
            final int nextPage = currentPage + 1; // 使用局部变量存储下一页的页码
            menu.addItem(53, CreateItem.createItem(Material.ARROW, "§aNext Page"), (p, s, item, action) -> {
                currentPageMap.put(menu, nextPage); // 更新当前页码
                addAvailableRecipesToMenu(menu, receivedRecipes);
                return false;
            });
        }

        // 如果没有符合条件的配方,显示提示
        if (availableRecipes.isEmpty()) {
            menu.addItem(36, CreateItem.createItem(Material.BARRIER, "§c has recipe"), (p, s, item, action) -> {
                p.sendMessage("§cThe machine does not contain enough materials.");
                return false;
            });
        }

        // 保存当前页码到 Map 中
        currentPageMap.put(menu, currentPage);
    }




}
