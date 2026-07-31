package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.persistence.PersistentDataType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class RecipeBookManager {

    private final MagicExpansion plugin;
    private final HashMap<UUID, String> currentRecipeIds;

    public RecipeBookManager(MagicExpansion plugin) {
        this.plugin = plugin;
        this.currentRecipeIds = new HashMap<>();
    }

    // 创建配方书物品
    public ItemStack createRecipeBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6Magic Altar Recipe Guide");

            List<String> lore = new ArrayList<>();
            lore.add("§7Right-click to view all available recipes.");
            lore.add("§7Learn how to build the altar and craft its items.");
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(
                    plugin.getPluginInitializer().getRecipeBookKey(),
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            book.setItemMeta(meta);
        }

        return book;
    }

    // 检查是否是配方书
    public boolean isRecipeBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(
                plugin.getPluginInitializer().getRecipeBookKey(),
                PersistentDataType.BYTE
        );
    }

    // 打开配方书主界面
    public void openRecipeBook(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Magic Altar Recipe Guide");

        // 清除当前配方ID
        currentRecipeIds.remove(player.getUniqueId());

        int slot = 10;
        for (Map.Entry<String, MagicAltarRecipe> entry : plugin.getPluginInitializer().getAltarManager().getRecipes().entrySet()) {
            if (slot % 9 == 8) slot += 2;

            String recipeId = entry.getKey();
            ItemStack result = entry.getValue().getResult().clone();
            ItemMeta meta = result.getItemMeta();

            if (meta != null) {
                List<String> lore = new ArrayList<>();
                if(meta.hasLore()) {
                    lore.addAll(Objects.requireNonNull(meta.getLore()));
                }
                lore.add("§7Click to view crafting recipes.");
                lore.add("§eRecipe ID: " + recipeId);
                meta.setLore(lore);

                // 添加配方ID到PDC
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "recipe_id"),
                        PersistentDataType.STRING,
                        recipeId
                );

                result.setItemMeta(meta);
            }

            gui.setItem(slot, result);
            slot++;
        }

        // 添加装饰边框
        addBorder(gui);

        player.openInventory(gui);
    }

    // 计算真实数量的方法
    private int calculateRealAmount(ItemStack item) {
        int totalAmount = item.getAmount(); // 这就是原始总数量
        int maxStackSize = 64;
        int realAmount = 0;

        // 模拟 dropItemInBatches 的分批逻辑,累加每一批的数量
        while (totalAmount > 0) {
            int batchSize = Math.min(totalAmount, maxStackSize);
            realAmount += batchSize;      // 累加这一批
            totalAmount -= batchSize;     // 减去已处理的
        }

        return realAmount;
    }

    // 打开配方详情界面
    public void openRecipeDetail(Player player, String recipeId, ItemStack resultItem) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Recipe Details - " + recipeId);

        MagicAltarRecipe recipe = plugin.getPluginInitializer().getAltarManager().getRecipes().get(recipeId);
        if (recipe == null) return;

        // 存储当前配方ID
        currentRecipeIds.put(player.getUniqueId(), recipeId);

        // 中心显示结果物品
        ItemStack displayResult = resultItem.clone();
        ItemMeta meta = displayResult.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ItemStackHelper.getDisplayName(displayResult) + "§r§b x " + calculateRealAmount(displayResult));
            List<String> lore = new ArrayList<>();
            if(meta.hasLore()) {
                lore.addAll(Objects.requireNonNull(meta.getLore()));
            }
            lore.add("§6Crafting Result");
            lore.add("§7Recipe: " + recipeId);
            meta.setLore(lore);
            displayResult.setItemMeta(meta);
        }
        gui.setItem(13, displayResult);

        // 添加查看按钮
        ItemStack dispenserButton = createButton(
                Material.DISPENSER,
                "§bDispenser Item Configuration",
                Arrays.asList("§7Click to view all nine dispensers.", "§7View the item layout for each dispenser.")
        );
        // 添加按钮标识和配方ID
        ItemMeta dispenserMeta = dispenserButton.getItemMeta();
        dispenserMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING,
                "dispenser_list"
        );
        dispenserMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "recipe_id"),
                PersistentDataType.STRING,
                recipeId
        );
        dispenserButton.setItemMeta(dispenserMeta);
        gui.setItem(29, dispenserButton);

        ItemStack baseButton = createButton(
                Material.OBSIDIAN,
                "§6Altar Base Layout",
                Arrays.asList("§7Click to view the 5×5 base.", "§7Shows where each block must be placed.")
        );
        // 添加按钮标识和配方ID
        ItemMeta baseMeta = baseButton.getItemMeta();
        baseMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING,
                "base_layout"
        );
        baseMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "recipe_id"),
                PersistentDataType.STRING,
                recipeId
        );
        baseButton.setItemMeta(baseMeta);
        gui.setItem(31, baseButton);

        ItemStack frameButton = createButton(
                Material.ITEM_FRAME,
                "§eItem Frame",
                Arrays.asList("§7Place it above the center dispenser.", "§7The crafting result appears here.", "§dWhen the output exceeds one item, the extras appear in the center of the altar.", "§cMust be empty!")
        );
        gui.setItem(33, frameButton);

        // 返回按钮
        ItemStack backButton = createHeadButton(
                "MHF_ArrowLeft",
                "§cBack to Recipe List",
                Arrays.asList("§7Click to return to the main menu.")
        );
        // 添加按钮标识
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING,
                "back_to_main"
        );
        backButton.setItemMeta(backMeta);
        gui.setItem(45, backButton);

        // 添加装饰边框
        addBorder(gui);

        player.openInventory(gui);
    }

    // 打开发射器列表界面
    public void openDispenserList(Player player, String recipeId) {
        Inventory gui = Bukkit.createInventory(null, 54, "§bDispenser List - " + recipeId);

        MagicAltarRecipe recipe = plugin.getPluginInitializer().getAltarManager().getRecipes().get(recipeId);
        if (recipe == null) return;

        // 存储当前配方ID
        currentRecipeIds.put(player.getUniqueId(), recipeId);

        // 在3x3网格中显示发射器
        int[] dispenserSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        String[] dispenserNames = {
                "§bTop-left Dispenser", "§bTop-center Dispenser", "§bTop-right Dispenser",
                "§bMiddle-left Dispenser", "§bCenter Dispenser", "§bMiddle-right Dispenser",
                "§bBottom-left Dispenser", "§bBottom-center Dispenser", "§bBottom-right Dispenser"
        };

        for (int i = 0; i < 9; i++) {
            ItemStack dispenserButton = recipe.getDisplayItem(i);

            if (dispenserButton == null) {
                // 空发射器
                dispenserButton = createButton(
                        Material.DROPPER,
                        dispenserNames[i],
                        Arrays.asList("§7This dispenser must be empty.", "§7Click to view its detailed configuration.")
                );
            } else {
                // 确保是发射器材质并更新显示信息
                if (dispenserButton.getType() != Material.DISPENSER) {
                    ItemStack newDispenser = new ItemStack(Material.DISPENSER);
                    ItemMeta meta = newDispenser.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(dispenserNames[i]);
                        List<String> lore = new ArrayList<>();
                        lore.add("§7Click to view its detailed configuration.");
                        meta.setLore(lore);
                        newDispenser.setItemMeta(meta);
                    }
                    dispenserButton = newDispenser;
                }
            }

            // 添加发射器索引和配方ID到PDC
            ItemMeta meta = dispenserButton.getItemMeta();
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "dispenser_index"),
                    PersistentDataType.INTEGER,
                    i
            );
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "recipe_id"),
                    PersistentDataType.STRING,
                    recipeId
            );
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "button_type"),
                    PersistentDataType.STRING,
                    "dispenser_detail"
            );
            dispenserButton.setItemMeta(meta);

            gui.setItem(dispenserSlots[i], dispenserButton);
        }

        // 添加说明
        ItemStack infoButton = createButton(
                Material.OAK_SIGN,
                "§eDispenser Configuration Instructions",
                Arrays.asList(
                        "§7Click each dispenser to view its internal layout.",
                        "§7Every item slot has an exact configuration.",
                        "§7Amounts must match exactly!"
                )
        );
        gui.setItem(49, infoButton);

        // 添加说明
        ItemStack dispenserInfoButton = createButton(
                Material.NETHER_STAR,
                "§eClick a dispenser to view details.",
                Arrays.asList(
                        "§7Each dispenser must match its exact coordinate.",
                        "§7Rotated layouts are not supported.",
                        ColorGradient.getRandomGradientName("To craft, use the Magic Altar Wand on the center dispenser."),
                        "§bTop-left coordinate: (-1, -1)",
                        "§bTop-right coordinate: (1, -1)",
                        "§bBottom-left coordinate: (-1, 1)",
                        "§bBottom-right coordinate: (1, 1)"
                )
        );
        gui.setItem(24, dispenserInfoButton);
        ItemStack pinkGlass = createButton(
                Material.PINK_STAINED_GLASS_PANE,
                "§e",
                Arrays.asList(
                        "§e"
                )
        );
        gui.setItem(14, pinkGlass);
        gui.setItem(15, pinkGlass);
        gui.setItem(16, pinkGlass);
        gui.setItem(23, pinkGlass);
        gui.setItem(25, pinkGlass);
        gui.setItem(32, pinkGlass);
        gui.setItem(33, pinkGlass);
        gui.setItem(34, pinkGlass);






        // 返回按钮
        ItemStack backButton = createHeadButton(
                "MHF_ArrowLeft",
                "§cBack to Recipe Details",
                Arrays.asList("§7Click to return to the recipe details.")
        );
        // 添加按钮标识和配方ID
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING,
                "back_to_detail"
        );
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "recipe_id"),
                PersistentDataType.STRING,
                recipeId
        );
        backButton.setItemMeta(backMeta);
        gui.setItem(45, backButton);

        addBorder(gui);
        player.openInventory(gui);
    }

    // 打开单个发射器物品界面
    public void openDispenserItems(Player player, String recipeId, int dispenserIndex) {
        Inventory gui = Bukkit.createInventory(null, 54, "§bDispenser Items - " + recipeId + " #" + (dispenserIndex + 1));

        MagicAltarRecipe recipe = plugin.getPluginInitializer().getAltarManager().getRecipes().get(recipeId);
        if (recipe == null) return;

        // 存储当前配方ID
        currentRecipeIds.put(player.getUniqueId(), recipeId);

        String[] dispenserNames = {
                "Top-left Dispenser", "Top-center Dispenser", "Top-right Dispenser",
                "Middle-left Dispenser", "Center Dispenser", "Middle-right Dispenser",
                "Bottom-left Dispenser", "Bottom-center Dispenser", "Bottom-right Dispenser"
        };

        // 显示发射器名称
        ItemStack infoButton = createButton(
                Material.OAK_SIGN,
                "§e" + dispenserNames[dispenserIndex] + " Configuration",
                Arrays.asList(
                        "§7This is the internal configuration for " + dispenserNames[dispenserIndex] + ".",
                        "§7Every slot must match exactly!",
                        "§7Empty slots must remain empty."
                )
        );
        gui.setItem(4, infoButton);

        // 在3x3网格中显示发射器槽位
        int[] slotPositions = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            ItemStack requiredItem = recipe.getDispenserSlotItem(dispenserIndex, slotIndex);
            int guiSlot = slotPositions[slotIndex];

            if (requiredItem != null) {
                // 显示需要的物品
                ItemStack displayItem = requiredItem.clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    if(meta.hasLore()){
                        lore.addAll(meta.getLore());
                    }
                    lore.add("§7-------- Divider --------");
                    lore.add("§6Required Item");
                    lore.add("§7Slot: " + (slotIndex + 1));
                    lore.add("§7Amount: " + requiredItem.getAmount());
                    lore.add("§cMust match exactly!");
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                gui.setItem(guiSlot, displayItem);
            } else {
                // 这个槽位应该为空
                ItemStack emptyMarker = createButton(
                        Material.GRAY_STAINED_GLASS_PANE,
                        "§7Slot " + (slotIndex + 1),
                        Arrays.asList("§7This slot must be empty.", "§cIt must not contain any items!")
                );
                gui.setItem(guiSlot, emptyMarker);
            }
        }

        // 返回按钮
        ItemStack backButton = createHeadButton(
                "MHF_ArrowLeft",
                "§cBack to Dispenser List",
                Arrays.asList("§7Click to return to the dispenser list.")
        );
        // 添加按钮标识和配方ID
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING,
                "back_to_dispenser_list"
        );
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "recipe_id"),
                PersistentDataType.STRING,
                recipeId
        );
        backButton.setItemMeta(backMeta);
        gui.setItem(45, backButton);

        // 这个界面不加边框,因为需要显示3x3网格
        player.openInventory(gui);
    }

    // 打开底座布局界面
    public void openBaseLayout(Player player, String recipeId) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Altar Base - " + recipeId);

        MagicAltarRecipe recipe = plugin.getPluginInitializer().getAltarManager().getRecipes().get(recipeId);
        if (recipe == null) return;

        // 存储当前配方ID
        currentRecipeIds.put(player.getUniqueId(), recipeId);

        // 显示5x5底座布局
        int[] baseSlots = {
                10, 11, 12, 13, 14,
                19, 20, 21, 22, 23,
                28, 29, 30, 31, 32,
                37, 38, 39, 40, 41,
                46, 47, 48, 49, 50
        };

        Material[][] baseLayout = recipe.getBaseLayout();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                int index = row * 5 + col;
                Material material = baseLayout[row][col];

                ItemStack blockItem = new ItemStack(material);
                ItemMeta meta = blockItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§f" + ItemStackHelper.getDisplayName(blockItem));
                    meta.setLore(Arrays.asList(
                            "§7Altar Base Block",
                            "§7Position: §e(" + (col-2) + ", " + (row-2) + ")"
                    ));
                    blockItem.setItemMeta(meta);
                }
                gui.setItem(baseSlots[index], blockItem);
            }
        }

        // 添加布局说明
        ItemStack infoButton = createButton(
                Material.OAK_SIGN,
                "§eBase Layout Instructions",
                Arrays.asList(
                        "§7This is the 5×5 altar base layout:",
                        "§7- The layout is centered below the center dispenser.",
                        "§7- Coordinate (0, 0) is the center dispenser.",
                        "§7- All other coordinates are relative to the center.",
                        "§7- The altar must exactly match this layout."
                )
        );
        gui.setItem(8, infoButton);

        // 返回按钮
        ItemStack backButton = createHeadButton(
                "MHF_ArrowLeft",
                "§cBack to Recipe Details",
                Arrays.asList("§7Click to return to the recipe details.")
        );
        // 添加按钮标识和配方ID
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING,
                "back_to_detail"
        );
        backMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "recipe_id"),
                PersistentDataType.STRING,
                recipeId
        );
        backButton.setItemMeta(backMeta);
        gui.setItem(0, backButton);

        // 这个界面不加边框,因为5x5显示不下
        player.openInventory(gui);
    }

    // 创建按钮物品
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    // 创建自定义头颅按钮
    private ItemStack createHeadButton(String texture, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);

            try {
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                URL url = new URL("http://textures.minecraft.net/texture/" + texture);
                textures.setSkin(url);
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            head.setItemMeta(meta);
        }

        return head;
    }

    // 添加GUI边框
    private void addBorder(Inventory gui) {
        ItemStack border = createButton(
                Material.BLACK_STAINED_GLASS_PANE,
                " ",
                new ArrayList<>()
        );

        // 顶部和底部边框
        for (int i = 0; i < 9; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
            if (gui.getItem(i + 45) == null) {
                gui.setItem(i + 45, border);
            }
        }

        // 侧边边框
        for (int i = 0; i < 45; i += 9) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
            if (gui.getItem(i + 8) == null) {
                gui.setItem(i + 8, border);
            }
        }
    }

    // 获取材料显示名称
    private String getMaterialName(Material material) {
        String name = material.name().toLowerCase();
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    // 处理GUI点击事件 - 完全重写版本
    public void handleInventoryClick(Player player, int slot, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        // 检查按钮类型
        if (meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "button_type"),
                PersistentDataType.STRING
        )) {
            String buttonType = meta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "button_type"),
                    PersistentDataType.STRING
            );

            // 获取配方ID(从PDC或当前存储的)
            String recipeId = null;
            if (meta.getPersistentDataContainer().has(
                    new NamespacedKey(plugin, "recipe_id"),
                    PersistentDataType.STRING
            )) {
                recipeId = meta.getPersistentDataContainer().get(
                        new NamespacedKey(plugin, "recipe_id"),
                        PersistentDataType.STRING
                );
            } else {
                recipeId = currentRecipeIds.get(player.getUniqueId());
            }

            // 处理按钮点击
            switch (buttonType) {
                case "dispenser_list":
                    if (recipeId != null) {
                        openDispenserList(player, recipeId);
                    }
                    break;

                case "dispenser_detail":
                    if (meta.getPersistentDataContainer().has(
                            new NamespacedKey(plugin, "dispenser_index"),
                            PersistentDataType.INTEGER
                    )) {
                        int dispenserIndex = meta.getPersistentDataContainer().get(
                                new NamespacedKey(plugin, "dispenser_index"),
                                PersistentDataType.INTEGER
                        );
                        if (recipeId != null) {
                            openDispenserItems(player, recipeId, dispenserIndex);
                        }
                    }
                    break;

                case "base_layout":
                    if (recipeId != null) {
                        openBaseLayout(player, recipeId);
                    }
                    break;

                case "back_to_main":
                    openRecipeBook(player);
                    break;

                case "back_to_detail":
                    if (recipeId != null) {
                        MagicAltarRecipe recipe = plugin.getPluginInitializer().getAltarManager().getRecipes().get(recipeId);
                        if (recipe != null) {
                            openRecipeDetail(player, recipeId, recipe.getResult());
                        }
                    }
                    break;

                case "back_to_dispenser_list":
                    if (recipeId != null) {
                        openDispenserList(player, recipeId);
                    }
                    break;
            }
            return;
        }

        // 检查配方物品点击
        if (meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "recipe_id"),
                PersistentDataType.STRING
        )) {
            String recipeId = meta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "recipe_id"),
                    PersistentDataType.STRING
            );
            MagicAltarRecipe recipe = plugin.getPluginInitializer().getAltarManager().getRecipes().get(recipeId);
            if (recipe != null) {
                openRecipeDetail(player, recipeId, recipe.getResult());
            }
        }
    }
}
