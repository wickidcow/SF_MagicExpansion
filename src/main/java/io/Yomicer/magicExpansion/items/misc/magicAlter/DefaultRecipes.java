package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.utils.itemUtils.sfItemUtils.sfItemAmount;

public class DefaultRecipes implements RecipeProvider{


    Material[][] pattern1 = {
            {Material.GLASS, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
    };

    Material[][] customBase = {
            {Material.GLASS, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.OBSIDIAN, Material.MAGMA_BLOCK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK}
    };

    Material[][] enchantAlter = {
            {Material.GLASS, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.BLUE_CONCRETE, Material.BLUE_TERRACOTTA, Material.BLUE_CONCRETE, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.BLUE_TERRACOTTA, Material.ENCHANTING_TABLE, Material.BLUE_TERRACOTTA, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.BLUE_CONCRETE, Material.BLUE_TERRACOTTA, Material.BLUE_CONCRETE, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK},
    };

    Material[][] anvilAlter = {
            {Material.GLASS, Material.FURNACE, Material.ENCHANTING_TABLE, Material.FURNACE, Material.ANVIL},
            {Material.FURNACE, Material.CRYING_OBSIDIAN, Material.CARTOGRAPHY_TABLE, Material.CRYING_OBSIDIAN, Material.FURNACE},
            {Material.ENCHANTING_TABLE, Material.CARTOGRAPHY_TABLE, Material.EMERALD_BLOCK, Material.CARTOGRAPHY_TABLE, Material.ENCHANTING_TABLE},
            {Material.FURNACE, Material.CRYING_OBSIDIAN, Material.CARTOGRAPHY_TABLE, Material.CRYING_OBSIDIAN, Material.FURNACE},
            {Material.ANVIL, Material.FURNACE, Material.ENCHANTING_TABLE, Material.FURNACE, Material.ANVIL},
    };


    @Override
    public void registerRecipes(Map<String, MagicAltarRecipe> recipes) {
        // 配方1:附魔金苹果x1314
        ItemStack[][] recipe1 = new ItemStack[9][9];
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            for (int j = 0; j < 9; j++) {
                recipe1[i][j] = new ItemStack(Material.GOLD_BLOCK, 64);
            }
        }
        for (int j = 0; j < 9; j++) {
            recipe1[4][j] = new ItemStack(Material.APPLE, 64);
        }

        ItemStack result1 = new CustomItemStack(Material.ENCHANTED_GOLDEN_APPLE, "§eThis is a magical apple.", "§bIt tastes wonderful.");
        result1.setAmount(1314);
        recipes.put("enchanted_apple", new MagicAltarRecipe(recipe1, result1));

        // 配方2:烈焰弹x16
        ItemStack[][] recipe2 = new ItemStack[9][9];
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                recipe2[i][0] = new ItemStack(Material.BLAZE_POWDER, 4);
            }
        }
        recipe2[4][0] = new ItemStack(Material.MAGMA_CREAM, 1);

        ItemStack result2 = new ItemStack(Material.FIRE_CHARGE, 16);

        recipes.put("fire_recipe", new MagicAltarRecipe(recipe2, result2, customBase));

        // 配方3:锋利1314520附魔书
        ItemStack[] [] recipe3 = new ItemStack[9][9];
        for (int i = 0; i < 9; i++){
            if(i == 0 || i == 2 || i == 6 || i == 8){
                for (int j = 0; j < 9; j++){
                    recipe3[i][j] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
                }
            }
        }
        for (int i = 0; i < 9; i++){
            if(i == 1 || i == 3 || i == 5 || i == 7){
                for (int j = 0; j < 9; j++){
                    if(j == 0 || j == 2 || j == 4 || j == 6 || j == 8) {
                        recipe3[i][j] = new CustomItemStack(Material.NETHERITE_SWORD, "When the broken blade is reforged, the knight shall return.");
                    }else {
                        recipe3[i][j] = SlimefunItems.STAFF_STORM;
                    }
                }
            }
        }
        recipe3 [4][0] = sfItemAmount(MagicExpansionItems.WIND_ELF_HEAD,64);
        recipe3 [4][1] = sfItemAmount(MagicExpansionItems.DEATH_LIFE_BOOK,8);
        recipe3 [4][2] = sfItemAmount(MagicExpansionItems.FIREZOMBIE_HEAD,64);
        recipe3 [4][3] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
        recipe3 [4][4] = new ItemStack(Material.BOOK);
        recipe3 [4][5] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
        recipe3 [4][6] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe3 [4][7] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
        recipe3 [4][8] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);

        Map<Enchantment, Integer> sharpnessEnchantments = Map.of(
                Enchantment.DAMAGE_ALL, 255
        );
        List<String> sharpnessLore = List.of(
                "§7An enchanted book containing immeasurable power.",
                "§7Only a true champion is said to be capable of wielding it.",
                "§e§l★ §c §fCCLV §7(255)",
                "§8[§6item§8]",
                "§7 is this"
        );
        ItemStack result3 = createCustomEnchantedBook(
                "§d§l§r §7[§c§lLv.255§7]",
                sharpnessLore,
                sharpnessEnchantments
        );
//        ItemStack[][] recipeNull = new ItemStack[9][9];
        recipes.put("ultra_sharpness", new MagicAltarRecipe(recipe3, result3, enchantAlter));


        // 配方4:抢夺1314520附魔书
        ItemStack[] [] recipe4 = new ItemStack[9][9];
        for (int i = 0; i < 9; i++){
            if(i == 0 || i == 2 || i == 6 || i == 8){
                for (int j = 0; j < 9; j++){
                    recipe4[i][j] = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
                }
            }
        }
        for (int i = 0; i < 9; i++){
            if(i == 1 || i == 3 || i == 5 || i == 7){
                for (int j = 0; j < 9; j++){
                    if(j == 0 || j == 2 || j == 4 || j == 6 || j == 8) {
                        recipe4[i][j] = new CustomItemStack(Material.NETHERITE_SWORD, "A torn page inscribed with forbidden power, hidden within the blade.");
                    }else{
                        recipe4[i][j] = SlimefunItems.SWORD_OF_BEHEADING;
                    }
                }
            }
        }
        recipe4 [4][0] = sfItemAmount(MagicExpansionItems.WIND_ELF_HEAD,64);
        recipe4 [4][1] = sfItemAmount(MagicExpansionItems.BASIC_ENCHANT_STONE,1);
        recipe4 [4][2] = sfItemAmount(MagicExpansionItems.FIREZOMBIE_HEAD,64);
        recipe4 [4][3] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
        recipe4 [4][4] = new ItemStack(Material.BOOK);
        recipe4 [4][5] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
        recipe4 [4][6] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe4 [4][7] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
        recipe4 [4][8] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);

        Map<Enchantment, Integer> LootingEnchantments = Map.of(
                Enchantment.LOOT_BONUS_MOBS, 255
        );
        List<String> lootingLore = List.of(
                "§7An enchanted book saturated with forbidden power.",
                "§7Its bearer claims extraordinary spoils from defeated enemies.",
                "§e§l★ §c §fCCLV §7(255)",
                "§8[§6item§8]",
                "§7 is this"
        );
        ItemStack result4 = createCustomEnchantedBook(
                "§d§l§§r §7[§c§lLv.255§7]",
                lootingLore,
                LootingEnchantments
        );
        recipes.put("ultra_looting", new MagicAltarRecipe(recipe4, result4, enchantAlter));


        // 配方5:星陨裁决
        ItemStack[] [] recipe5 = new ItemStack[9][9];

        for (int j = 0; j < 9; j++){
            if(j != 4) {
                recipe5[1][j] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_INGOT, 32);
                recipe5[3][j] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_INGOT, 32);
                recipe5[5][j] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_INGOT, 32);
            } else {
                recipe5[1][j] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT, 8);
                recipe5[3][j] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT, 8);
                recipe5[5][j] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT, 8);
            }
        }

        for (int j = 0; j < 9; j++){
            if(j != 4) {
                recipe5[0][j] = sfItemAmount(SlimefunItems.STAFF_FIRE, 64);
                recipe5[2][j] = sfItemAmount(SlimefunItems.STAFF_FIRE, 64);
            } else {
                recipe5[0][j] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_FIRE, 32);
                recipe5[2][j] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_FIRE, 32);
            }
        }

        for (int i = 6; i < 9; i++) {
            if (i != 7) {
                recipe5[i][0] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_9;
                recipe5[i][1] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_10;
                recipe5[i][2] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_11;
                recipe5[i][3] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_12;
                recipe5[i][4] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_13;
                recipe5[i][5] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_14;
                recipe5[i][6] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_15;
                recipe5[i][7] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_16;
                recipe5[i][8] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_17;
            }
        }
        recipe5[7][0] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_18;
        recipe5[7][1] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_19;
        recipe5[7][2] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_20;
        recipe5[7][3] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_21;
        recipe5[7][4] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_22;
        recipe5[7][5] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_23;
        recipe5[7][6] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_24;
        recipe5[7][7] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_25;
        recipe5[7][8] = MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_26;

        recipe5 [4][0] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][1] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][2] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][3] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][4] = new ItemStack(Material.NETHERITE_SWORD);
        recipe5 [4][5] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][6] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][7] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
        recipe5 [4][8] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);


        recipes.put("weapon_star_shards_sword", new MagicAltarRecipe(recipe5, MagicExpansionItems.WEAPON_STAR_SHARDS_SWORD, anvilAlter));


        // 配方6:创世核心
        ItemStack[] [] recipe6 = new ItemStack[9][9];
        for (int i = 0; i < 9; i++) {
            recipe6[i][0] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_EARTH,64);
            recipe6[i][1] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_WOOD,64);
            recipe6[i][2] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_FIRE,64);
            recipe6[i][3] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_WATER,64);
            recipe6[i][4] = sfItemAmount(MagicExpansionItems.PURE_FIVE_ELEMENT,64);
            recipe6[i][5] = sfItemAmount(MagicExpansionItems.PURE_ELEMENT_GOLD,64);
            recipe6[i][6] = sfItemAmount(MagicExpansionItems.MAGIC_CAPACITY_ULTRA,15);
            recipe6[i][7] = sfItemAmount(MagicExpansionItems.FISHING_ROD_FINAL_STRING,3);
            recipe6[i][8] = sfItemAmount(MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_37,15);
        }

        recipes.put("the_creation_star", new MagicAltarRecipe(recipe6, MagicExpansionItems.WORLD_CORE, pattern1));

    }

    @Override
    public void registerAltarPatterns(List<Material[][]> patternList) {
        patternList.add(pattern1);
        patternList.add(customBase);
        patternList.add(enchantAlter);
        patternList.add(anvilAlter);
    }


    private ItemStack createCustomEnchantedBook(
            String displayName,
            List<String> lore,
            Map<Enchantment, Integer> enchantments
    ) {
        // 创建一本附魔书
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = enchantedBook.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;

            // 添加所有附魔
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                storageMeta.addStoredEnchant(enchantment, level, true);
            }
        }

        // 设置自定义名称
        meta.setDisplayName(displayName);

        // 设置 Lore
        meta.setLore(lore);

        // 应用元数据
        enchantedBook.setItemMeta(meta);
        return enchantedBook;
    }



}
