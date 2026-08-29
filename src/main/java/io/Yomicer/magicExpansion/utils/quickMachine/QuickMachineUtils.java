package io.Yomicer.magicExpansion.utils.quickMachine;

import io.Yomicer.magicExpansion.utils.CreateItem;
import io.Yomicer.magicExpansion.utils.MagicExpansionSlimefunItemCache;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static io.Yomicer.magicExpansion.utils.GiveItem.giveOrDropItem;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;
import static io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils.isItemSimilar;

public class QuickMachineUtils {

    public static boolean consumeMaterials(Player player, Map<String, Integer> recipe, int amount) {
        PlayerInventory inventory = player.getInventory();

        // D2: 消耗材料原子化——先深克隆背包存储内容作为快照，
        // 中途移除失败时按快照整体回滚，保证材料不凭空丢失
        ItemStack[] storageSnapshot = inventory.getStorageContents();
        ItemStack[] snapshotCopy = new ItemStack[storageSnapshot.length];
        for (int i = 0; i < storageSnapshot.length; i++) {
            snapshotCopy[i] = storageSnapshot[i] == null ? null : storageSnapshot[i].clone();
        }

        // D2: 第一步——整体预检所有材料是否足够，不足直接失败且不修改背包
        if (!hasEnoughMaterials(getPlayerInventoryItems(player), recipe, amount)) {
            player.sendMessage("§c材料不足，请检查你的背包！");
            return false;
        }

        for (Map.Entry<String, Integer> requiredEntry : recipe.entrySet()) {
            String requiredKey = requiredEntry.getKey();
            int requiredAmountPerCraft = requiredEntry.getValue();
            int totalRequiredAmount = requiredAmountPerCraft * amount;

            if (requiredKey.startsWith("mc:")) {
                // 原版物品
                Material material = Material.matchMaterial(requiredKey.substring(3));
                if (material != null) {
                    ItemStack targetItem = new ItemStack(material);
                    if (!removeVanillaItemsFromInventory(inventory, targetItem, totalRequiredAmount)) {
                        player.sendMessage("§c未能移除足够的原版材料，请检查你的背包！");
                        // D2: 中途失败——按快照回滚已移除的材料
                        inventory.setStorageContents(snapshotCopy);
                        return false;
                    }
                }
            } else if (requiredKey.startsWith("sf:")) {
                // Slimefun 物品
                String slimefunId = requiredKey.substring(3);
                SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);
                if (slimefunItem != null) {
                    ItemStack targetItem = slimefunItem.getItem().clone();
                    if (!removeSlimefunItemsFromInventory(inventory, targetItem, totalRequiredAmount)) {
                        player.sendMessage("§c未能移除足够的Slimefun材料，请检查你的背包！");
                        // D2: 中途失败——按快照回滚已移除的材料
                        inventory.setStorageContents(snapshotCopy);
                        return false;
                    }
                }
            }
        }

        // 所有材料都已成功移除
        return true;
    }

    /**
     * 从玩家背包中移除指定数量的原版物品
     *
     * @param inventory 玩家背包
     * @param targetItem 要移除的物品
     * @param amount     要移除的数量
     * @return 是否成功移除了指定数量的物品
     */
    private static boolean removeVanillaItemsFromInventory(PlayerInventory inventory, ItemStack targetItem, int amount) {
        ItemStack[] contents = inventory.getStorageContents(); // 获取存储槽位（不包括盔甲槽）
        int remainingAmount = amount;

        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack itemInSlot = contents[i];
            if (itemInSlot != null && itemInSlot.isSimilar(targetItem)) {
                int removed = Math.min(itemInSlot.getAmount(), remainingAmount);
                itemInSlot.setAmount(itemInSlot.getAmount() - removed);
                remainingAmount -= removed;

                if (itemInSlot.getAmount() <= 0) {
                    contents[i] = null; // 如果物品数量为 0，清空该槽位
                }
            }
        }

        inventory.setStorageContents(contents); // 更新存储槽位内容

        return remainingAmount == 0; // 如果剩余数量为 0，表示成功移除所有物品
    }

    /**
     * 从玩家背包中移除指定数量的 Slimefun 物品
     *
     * @param inventory 玩家背包
     * @param targetItem 要移除的物品
     * @param amount     要移除的数量
     * @return 是否成功移除了指定数量的物品
     */
    private static boolean removeSlimefunItemsFromInventory(PlayerInventory inventory, ItemStack targetItem, int amount) {
        ItemStack[] contents = inventory.getStorageContents(); // 获取存储槽位（不包括盔甲槽）
        int remainingAmount = amount;

        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack itemInSlot = contents[i];
            if (itemInSlot != null && isItemSimilar(itemInSlot, targetItem, false)) {
                int removed = Math.min(itemInSlot.getAmount(), remainingAmount);
                itemInSlot.setAmount(itemInSlot.getAmount() - removed);
                remainingAmount -= removed;

                if (itemInSlot.getAmount() <= 0) {
                    contents[i] = null; // 如果物品数量为 0，清空该槽位
                }
            }
        }

        inventory.setStorageContents(contents); // 更新存储槽位内容

        return remainingAmount == 0; // 如果剩余数量为 0，表示成功移除所有物品
    }


   /**
     * 获取配方结果
     * 根据配方类型和配方数组获取配方结果
     *
     * @param recipe      配方数组
     * @param recipeType  配方类型
     * @return 配方结果
     */
    public static Pair<ItemStack, Integer> getRecipeResult(Map<String, Integer> recipe, RecipeType recipeType) {
        for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            if (item.getRecipeType() == recipeType) {
                Map<String, Integer> recipeMap = convertRecipeToMap(item.getRecipe());
                if (recipeMap.equals(recipe)) {
                    // 返回匹配的产物及其数量
                    ItemStack output = item.getRecipeOutput().clone();
                    int outputAmount = output != null ? output.getAmount() : 1; // 默认数量为 1
                    return new Pair<>(output, outputAmount);
                }
            }
        }
        return null; // 如果未找到匹配的物品，返回 null
    }


    /**
     * 整合配方材料，合并相同物品
     * 将配方数组转换为 Map
     *
     * @param recipe 配方数组
     * @return 配方 Map
     */
    public static Map<String, Integer> convertRecipeToMap(ItemStack[] recipe) {
        Map<String, Integer> recipeMap = new HashMap<>();
        for (ItemStack requiredItem : recipe) {
            if (requiredItem == null || requiredItem.getType() == Material.AIR) {
                continue; // 忽略空槽位
            }

            int requiredAmount = requiredItem.getAmount();

            // 判断是否为 SlimefunItem
            String itemKey;
            SlimefunItem slimefunItem = SlimefunItem.getByItem(requiredItem);
            if (slimefunItem != null) {
                // 如果是 SlimefunItem，使用其 ID 作为键
                itemKey = "sf:" + slimefunItem.getId();
            } else {
                // 如果不是 SlimefunItem，则默认为原版物品
                itemKey = "mc:" + requiredItem.getType().name();
            }

            // 整合相同物品的数量
            recipeMap.put(itemKey, recipeMap.getOrDefault(itemKey, 0) + requiredAmount);
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
    public static String getDisplayName(ItemStack item) {
        Material material = item.getType();
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);

        if (slimefunItem != null) {
            // 如果是 SlimefunItem，返回其 ID
            return slimefunItem.getItemName();
        } else {
            // 如果是原版物品，返回其英文名称
            return material.name();
        }
    }

    /**
     * 输出所需配方材料名称，汉化原本物品+粘液名称
     * 获取配方材料显示名称
     * 根据物品键获取显示名称
     *
     * @param itemKey 物品键
     * @return 显示名称
     */
    public static String getIngredientDisplayName(String itemKey) {
        if (itemKey.startsWith("mc:")) {
            // 如果是原版物品，提取 Material 名称并返回
            String materialName = itemKey.substring(3); // 去掉前缀 "mc:"
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                // 创建一个临时的 ItemStack
                ItemStack itemStack = new ItemStack(material);
                return ItemStackHelper.getName(itemStack);
            }
            return "未知原版物品 (" + materialName + ")";
        } else if (itemKey.startsWith("sf:")) {
            // 如果是 Slimefun 物品，提取 ID 并查找对应的 SlimefunItem
            String slimefunId = itemKey.substring(3); // 去掉前缀 "sf:"
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);
            if (slimefunItem != null) {
                return slimefunItem.getItemName(); // 返回 Slimefun 物品的显示名称
            }
            return "未知 Slimefun 物品 (" + slimefunId + ")";
        } else {
            return "未知物品类型 (" + itemKey + ")";
        }
    }



    /**
     * 计算玩家最多可以合成的数量
     */
    public static int calculateMaxCraftableAmount(Map<String, Integer> playerItems, Map<String, Integer> recipe) {
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
     * 获取玩家背包中的所有物品及其数量
     * 并且给一个键
     *
     * @param player 玩家对象
     * @return 一个 Map，键为物品类型或 SlimefunItem ID，值为该物品的数量
     */
    public static Map<String, Integer> getPlayerInventoryItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<String, Integer> itemCounts = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                // 判断是否为 SlimefunItem
                String itemKey;
                SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
                if (slimefunItem != null) {
                    // 如果是 SlimefunItem，使用其 ID 作为键
                    itemKey = "sf:" + slimefunItem.getId();
                } else {
                    // 如果不是 SlimefunItem，则默认为原版物品
                    itemKey = "mc:" + item.getType().name();
                }

                // 整合玩家背包中的物品数量
                itemCounts.put(itemKey, itemCounts.getOrDefault(itemKey, 0) + item.getAmount());
            }
        }

        return itemCounts;
    }



    /**
     * 检查玩家是否有足够的物品来制作配方
     *
     * @param playerItems 玩家背包中的所有物品及其数量
     * @param recipe      配方
     * @param amount      合成数量
     * @return 如果玩家有足够的物品，则返回 true，否则返回 false
     */
    public static boolean hasEnoughMaterials(Map<String, Integer> playerItems, Map<String, Integer> recipe, int amount) {
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
    public static boolean hasEnoughMaterials(Map<String, Integer> playerItems, Map<String, Integer> recipe) {
        return hasEnoughMaterials(playerItems, recipe, 1); // 调用带 amount 参数的方法，默认值为 1
    }

    /**
     * 获取配方结果及其数量
     *
     * @param recipe      配方
     * @param recipeType  配方类型
     * @return 包含配方结果的 Pair，其中 Pair 的第一个元素为 ItemStack，第二个元素为该配方结果的数量
     */
    private static Pair<ItemStack, Integer> getRecipeResultWithAmount(Map<String, Integer> recipe, RecipeType recipeType) {
        for (SlimefunItem item : MagicExpansionSlimefunItemCache.getAllSlimefunItems()) {
            if (item.getRecipeType() == recipeType) {
                Map<String, Integer> recipeMap = convertRecipeToMap(item.getRecipe());
                if (recipeMap.equals(recipe)) {
                    ItemStack output = item.getRecipeOutput().clone();
                    int outputAmount = output != null ? output.getAmount() : 1; // 默认数量为 1
                    return new Pair<>(output, outputAmount);
                }
            }
        }
        return null; // 如果未找到匹配的物品，返回 null
    }


    /**
     * 将符合条件的配方添加到菜单中，并支持分页
     * 刷新逻辑
     *
     * @param player 玩家对象
     * @param menu   菜单对象
     * @param page   当前页码（从 0 开始）
     */
    public static void addAvailableRecipesToMenu(Player player, ChestMenu menu, int page, List<Map<String, Integer>> ReceivedRecipes, RecipeType recipeType) {
        // 获取玩家背包中的所有物品及其数量
        Map<String, Integer> playerItems = getPlayerInventoryItems(player);

        // 清空菜单内容
        for (int i = 0; i < menu.getSize(); i++) {
            menu.addItem(i, null);
        }
        menu.addItem(49, new CustomItemStack(doGlow(Material.NETHER_STAR),"§x§F§D§B§7§D§4使用说明",
                        "§e右键 §b点击菜单内的物品，查看单次合成所需的材料",
                        "§e左键 §b点击菜单内的物品,制作一次",
                        "§b按住 §eShift 右键 §b点击菜单内的物品，可以一次制作32个物品",
                        "§b按住 §eShift 左键 §b点击菜单内的物品，制作所有可制作的物品"),
                (player1, slot, item, action) -> {
                    return false;
                });

        // 筛选出玩家可以制作的配方
        List<Map<String, Integer>> availableRecipes = ReceivedRecipes.stream()
                .filter(recipe -> hasEnoughMaterials(playerItems, recipe))
                .toList();

        // 分页逻辑
        int itemsPerPage = 45; // 每页显示的最大数量
        int totalPages = (int) Math.ceil((double) availableRecipes.size() / itemsPerPage);
        page = Math.max(0, Math.min(page, totalPages - 1)); // 确保页码在有效范围内

        // 当前页的配方
        List<Map<String, Integer>> currentPageRecipes = availableRecipes.stream()
                .skip((long) page * itemsPerPage)
                .limit(itemsPerPage)
                .toList();

        // 添加配方到菜单中
        int slot = 0;
        for (Map<String, Integer> recipe : currentPageRecipes) {
            // 获取配方的产物及其单次合成的数量
            Pair<ItemStack, Integer> recipeResult = getRecipeResultWithAmount(recipe, recipeType);
            if (recipeResult == null) continue;

            ItemStack resultItem = recipeResult.getFirstValue().clone(); // 提取产物并克隆以避免修改原对象
            ItemStack resultItemDisplay = recipeResult.getFirstValue().clone(); // 提取产物并克隆以避免修改原对象
            // 假设 resultItemDisplay 是已经克隆的 ItemStack 对象
            if (resultItemDisplay.hasItemMeta()) { // 确保 ItemStack 不为空且有 ItemMeta
                ItemMeta itemMeta = resultItemDisplay.getItemMeta(); // 获取 ItemMeta
//                    String originalName = itemMeta.getDisplayName(); // 获取原始显示名称
//                    String newName = "§x§F§D§B§7§D§4"+originalName + "§e [产物]"; // 添加后缀 [产物]
//                    itemMeta.setDisplayName(newName); // 设置新的显示名称
                    // 准备 Lore 列表
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    // 添加“所需材料”标题
                    lore.add("§a所需材料:");
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
//                String newName = name + "§e [产物]";
                ItemMeta meta = Bukkit.getItemFactory().getItemMeta(resultItemDisplay.getType());
//                meta.setDisplayName(newName);
                // 准备 Lore 列表
                List<String> lore = new ArrayList<>();
                lore.add("");
                // 添加“所需材料”标题
                lore.add("§a所需材料:");
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

            // 创建一个局部变量 currentSlot 来存储当前槽位
            int currentSlot = slot++;
            menu.addItem(currentSlot, resultItemDisplay, (p, s, item, action) -> {
                if (action.isRightClicked() && !action.isShiftClicked()) {
                    // 右键点击：显示制作一份所需的材料
                    p.sendMessage("§a配方: §b" + getDisplayName(resultItem));
                    p.sendMessage("§a所需材料:");
                    for (Map.Entry<String, Integer> ingredient : recipe.entrySet()) {
                        String itemKey = ingredient.getKey(); // 材料键值（如 "mc:COBBLESTONE" 或 "sf:some_item_id"）
                        int requiredAmount = ingredient.getValue(); // 所需数量

                        // 获取材料的显示名称
                        String displayName = getIngredientDisplayName(itemKey);

                        // 发送消息
                        p.sendMessage(" - §e" + displayName + " x " + requiredAmount);
                    }
                } else if (!action.isRightClicked() && !action.isShiftClicked()) {
                    // 左键点击：消耗一份材料并给予一份产品
                    Map<String, Integer> currentPlayerItems = getPlayerInventoryItems(p);
                    if (!hasEnoughMaterials(currentPlayerItems, recipe)) {
                        p.sendMessage("§c材料不足，无法合成！");
                        return false;
                    }

                    // 消耗材料
                    if (!consumeMaterials(p, recipe, 1)) {
                        // 如果材料不足，发送提示信息并终止后续逻辑
                        p.sendMessage("§c材料数据错误，请使用正确的材料！");
                        return false; // 或者根据需求抛出异常、中断操作等
                    }

                    // 给予产品
                    resultItem.setAmount(outputAmountPerCraft); // 设置产物数量

                    giveOrDropItem(p, resultItem); // 给予玩家物品

                    // 提示信息
                    String productName = getDisplayName(resultItem);
                    p.sendMessage("§a你成功合成了 §b" + outputAmountPerCraft + " §a个 §b" + productName + "§a。");

                    // 刷新菜单
                    addAvailableRecipesToMenu(p, menu, 0, ReceivedRecipes, recipeType);
                } else if (!action.isRightClicked() && action.isShiftClicked()) {
                    // Shift + 左键点击：批量合成
                    Map<String, Integer> currentPlayerItems = getPlayerInventoryItems(p);
                    int maxCraftableAmount = calculateMaxCraftableAmount(currentPlayerItems, recipe);
                    if (maxCraftableAmount > 52000){
                        maxCraftableAmount = 52000;
                    }
                    if (maxCraftableAmount <= 0) {
                        p.sendMessage("§c材料不足，无法合成！");
                        return false;
                    }

                    // 消耗材料
                    if(!consumeMaterials(p, recipe, maxCraftableAmount)){
                        // 如果材料不足，发送提示信息并终止后续逻辑
                        p.sendMessage("§c材料数据错误，请使用正确的材料！");
                        return false; // 或者根据需求抛出异常、中断操作等
                    }

                    // 给予产品
                    int totalOutputAmount = maxCraftableAmount * outputAmountPerCraft; // 计算总产物数量
                    resultItem.setAmount(totalOutputAmount); // 设置产物数量

                    giveOrDropItem(p, resultItem); // 给予玩家物品

                    // 提示信息
                    String productName = getDisplayName(resultItem);
                    p.sendMessage("§a你成功合成了 §b" + totalOutputAmount + " §a个 §b" + productName + "§a。");

                    // 刷新菜单
                    addAvailableRecipesToMenu(p, menu, 0, ReceivedRecipes, recipeType);
                } else if (action.isRightClicked() && action.isShiftClicked()) {
                    // Shift + 右键点击：固定合成 32 份
                    Map<String, Integer> currentPlayerItems = getPlayerInventoryItems(p);
                    int requiredCrafts = 32;

                    // 检查是否可以合成 32 份
                    if (!hasEnoughMaterials(currentPlayerItems, recipe, requiredCrafts)) {
                        p.sendMessage("§c材料不足，无法合成 32 份产物！");
                        return false;
                    }

                    // 消耗材料
                    if(!consumeMaterials(p, recipe, requiredCrafts)){
                        // 如果材料不足，发送提示信息并终止后续逻辑
                        p.sendMessage("§c材料数据错误，请使用正确的材料！");
                        return false; // 或者根据需求抛出异常、中断操作等
                    }

                    // 给予产品
                    int totalOutputAmount = requiredCrafts * outputAmountPerCraft; // 计算总产物数量
                    resultItem.setAmount(totalOutputAmount); // 设置产物数量

                    giveOrDropItem(p, resultItem); // 给予玩家物品

                    // 提示信息
                    String productName = getDisplayName(resultItem);
                    p.sendMessage("§a你成功合成了 §b" + totalOutputAmount + " §a个 §b" + productName + "§a。");

                    // 刷新菜单
                    addAvailableRecipesToMenu(p, menu, 0, ReceivedRecipes, recipeType);
                }
                return false; // 不消耗物品
            });
        }

        // 添加分页按钮
        if (page > 0) {
            // 上一页按钮
            final int previousPage = page - 1; // 使用局部变量存储上一页的页码
            menu.addItem(45, CreateItem.createItem(Material.ARROW, "§a上一页"), (p, s, item, action) -> {
                addAvailableRecipesToMenu(p, menu, previousPage,ReceivedRecipes, recipeType); // 切换到上一页
                return false;
            });
        }

        if (page < totalPages - 1) {
            // 下一页按钮
            final int nextPage = page + 1; // 使用局部变量存储下一页的页码
            menu.addItem(53, CreateItem.createItem(Material.ARROW, "§a下一页"), (p, s, item, action) -> {
                addAvailableRecipesToMenu(p, menu, nextPage,ReceivedRecipes, recipeType); // 切换到下一页
                return false;
            });
        }

        // 如果没有符合条件的配方，显示提示
        if (availableRecipes.isEmpty()) {
            menu.addItem(0, CreateItem.createItem(Material.BARRIER, "§c没有可制作的配方"), (p, s, item, action) -> {
                p.sendMessage("§c你的背包中没有足够的材料！");
                return false;
            });
        }
    }


}
