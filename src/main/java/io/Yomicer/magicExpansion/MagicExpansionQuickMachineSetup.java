package io.Yomicer.magicExpansion;

import io.Yomicer.magicExpansion.Listener.SlimefunRegistryFinalized;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryListener;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.quickMachine.*;
import io.Yomicer.magicExpansion.items.quickMachineBlockVersion.QuickMachineBV;
import io.Yomicer.magicExpansion.items.quickMachineBlockVersion.QuickMachineMBBV;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static io.Yomicer.magicExpansion.MagicExpansionItemSetup.SPECIAL_RECIPE_TYPE;
import static io.Yomicer.magicExpansion.MagicExpansionItemSetup.magicexpansionquickmachine;
import static io.Yomicer.magicExpansion.utils.ConvertItem.AdvancedCreateItem;
import static io.Yomicer.magicExpansion.utils.ConvertItem.BasicCreateItem;

public class MagicExpansionQuickMachineSetup {



    public static void setup(@Nonnull MagicExpansion plugin) {



        //Quick Machine
        // 魔法矿洞
        new MagicExpansionMineralCave(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_MINERAL_CAVE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.SMOOTH_STONE), BasicCreateItem("MAGIC_REDSTONE"),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), BasicCreateItem("MAGIC_REDSTONE"), AdvancedCreateItem("MAGIC_COSMIC_DUST")
        }).register(plugin);

        // 魔法工作台
        new MagicExpansionQuickEnhancedCraftingTable(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_ENHANCED_CRAFTING_TABLE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.CRAFTING_TABLE), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法冶炼炉
        new MagicExpansionQuickSmeltery(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_SMELTERY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.FURNACE), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        /*
        // 魔法冶炼炉2
        new UnplaceableBlock(quickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_SMELTERY2, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                BasicCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC"), AdvancedCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED"), BasicCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC"),
                AdvancedCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED"), new ItemStack(Material.FURNACE), AdvancedCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED"),
                BasicCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC"), AdvancedCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED"), BasicCreateItem("MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC")
        },MagicExpansionItems.MAGIC_EXPANSION_QUICK_SMELTERY).register(plugin);
        */
        // 魔法磨石
        new MagicExpansionQuickGrindStone(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_GRIND_STONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.DISPENSER), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法粉碎机
        new MagicExpansionQuickOreCrusher(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_ORE_CRUSHER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.IRON_BARS), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);

        // 魔法盔甲锻造台
        new MagicExpansionQuickArmorForge(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_ARMOR_FORGE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.ANVIL), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法压缩机
        new MagicExpansionQuickCompressor(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_COMPRESSOR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.PISTON), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法压力机
        new MagicExpansionQuickPressureChamber(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_PRESSURE_CHAMBER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.GLASS), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法工作台
        new MagicExpansionQuickMagicWorkbench(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_MAGIC_WORKBENCH, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.BOOKSHELF), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法淘金机
        new MagicExpansionQuickAutomatedPanningMachine(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_AUTOMATED_PANNING_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), SlimefunItems.GOLD_PAN, AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法远古祭坛
        new MagicExpansionQuickAncientAltar(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_AUTOMATED_ANCIENT_ALTAR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.ANCIENT_ALTAR, MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, SlimefunItems.ANCIENT_ALTAR,
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.ENCHANTING_TABLE), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                SlimefunItems.ANCIENT_ALTAR, BasicCreateItem("MAGIC_REDSTONE"), SlimefunItems.ANCIENT_ALTAR
        }).register(plugin);
        // 破损的魔法碎矿机
        new MagicExpansionQuickOreGrinder(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_ELECTRIC_ORE_GRINDER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.OBSERVER), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);
        // 魔法压力舱
        new MagicExpansionQuickHeatedPressureChamber(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_HEATED_PRESSURE_CHAMBER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), BasicCreateItem("MAGIC_REDSTONE"), new ItemStack(Material.ENDER_EYE)
        }).register(plugin);


        //快捷机器放置版
        new UnplaceableBlock(magicexpansionquickmachine, MagicExpansionItems.QUICK_MACHINE_BV_INFO, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null,null
        }).register(plugin);

        // 魔法增强型工作台 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_ENHANCED_CRAFTING_TABLE_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_ENHANCED_CRAFTING_TABLE,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Enhanced Crafting Table", SlimefunRegistryListener.ENHANCED_CRAFTING_TABLE_RECIPES,RecipeType.ENHANCED_CRAFTING_TABLE).register(plugin);
        // 魔法冶炼炉 放置版
        new QuickMachineMBBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_SMELTERY_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_SMELTERY,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Smeltery", SlimefunRegistryFinalized.SMELTERY_RECIPES).register(plugin);
        // 魔法磨石 放置版
        new QuickMachineMBBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_GRIND_STONE_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_GRIND_STONE,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Grind Stone", SlimefunRegistryFinalized.GRIND_STONE_RECIPES).register(plugin);
        // 魔法粉碎机 放置版
        new QuickMachineMBBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_ORE_CRUSHER_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_ORE_CRUSHER,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Ore Crusher", SlimefunRegistryFinalized.ORE_CRUSHER_RECIPES).register(plugin);
        // 魔法锻造台 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_ARMOR_FORGE_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_ARMOR_FORGE,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Armor Forge", SlimefunRegistryListener.ARMOR_FORGE_RECIPES,RecipeType.ARMOR_FORGE).register(plugin);
        // 魔法压缩机 放置版
        new QuickMachineMBBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_COMPRESSOR_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_COMPRESSOR,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Compressor", SlimefunRegistryFinalized.COMPRESSOR_RECIPES).register(plugin);
        // 魔法压力机 放置版
        new QuickMachineMBBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_PRESSURE_CHAMBER_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_PRESSURE_CHAMBER,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Pressure Chamber", SlimefunRegistryFinalized.PRESSURE_CHAMBER_RECIPES).register(plugin);
        // 魔法工作台[魔法工作台] 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_MAGIC_WORKBENCH_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_MAGIC_WORKBENCH,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Workbench", SlimefunRegistryListener.MAGIC_WORKBENCH_RECIPES,RecipeType.MAGIC_WORKBENCH).register(plugin);
        // 魔法淘金机 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_AUTOMATED_PANNING_MACHINE_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_AUTOMATED_PANNING_MACHINE,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Automated Panning Machine", SlimefunRegistryListener.GOLD_PAN_RECIPES,RecipeType.GOLD_PAN).register(plugin);
        // 魔法远古祭坛 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_AUTOMATED_ANCIENT_ALTAR_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_AUTOMATED_ANCIENT_ALTAR,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Ancient Altar", SlimefunRegistryListener.ANCIENT_ALTAR_RECIPES,RecipeType.ANCIENT_ALTAR).register(plugin);
        // 魔法远古祭坛 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_ELECTRIC_ORE_GRINDER_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_ELECTRIC_ORE_GRINDER,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Damaged Magic Ore Grinder", SlimefunRegistryListener.ORE_CRUSHER_RECIPES,RecipeType.ORE_CRUSHER).register(plugin);
        // 魔法远古祭坛 放置版
        new QuickMachineBV(magicexpansionquickmachine, MagicExpansionItems.QUICK_HEATED_PRESSURE_CHAMBER_BV, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), MagicExpansionItems.MAGIC_EXPANSION_QUICK_HEATED_PRESSURE_CHAMBER,new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS) ,new ItemStack(Material.OAK_PLANKS)
        },"Magic Heated Pressure Chamber", SlimefunRegistryListener.HEATED_PRESSURE_CHAMBER_RECIPES,RecipeType.HEATED_PRESSURE_CHAMBER).register(plugin);





    }




}
