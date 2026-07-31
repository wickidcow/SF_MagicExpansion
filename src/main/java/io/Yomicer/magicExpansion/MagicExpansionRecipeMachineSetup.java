package io.Yomicer.magicExpansion;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.*;
import io.Yomicer.magicExpansion.items.electric.templateMachine.ItemOriginBackTrackMachine;
import io.Yomicer.magicExpansion.items.electric.templateMachine.TemplateMachine;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.itemUtils.NamedTagBuilder;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static io.Yomicer.magicExpansion.MagicExpansionItemSetup.*;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.*;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_9;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_ALPHA;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.PURE_ELEMENT_EARTH;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.PURE_ELEMENT_INGOT;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.PURE_ELEMENT_WOOD;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.PURE_FIVE_ELEMENT;
import static io.Yomicer.magicExpansion.utils.ConvertItem.*;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;
import static io.Yomicer.magicExpansion.utils.itemUtils.sfItemUtils.sfItemAmount;

public class MagicExpansionRecipeMachineSetup {


    public static void setup(@Nonnull MagicExpansion plugin) {


        //魔法材料工坊
        new RecipeMachinePreBuilding(magicexpansionenergy, MagicExpansionItems.PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.ELECTRIC_PRESS_2, SlimefunItems.ENERGIZED_CAPACITOR,SlimefunItems.ELECTRIC_PRESS_2,
                AdvancedCreateItem("MAGIC_COSMIC_DUST"),MagicExpansionItems.PRE_BUILDINGS_MACHINE,MagicExpansionItems.ELEMENT_INGOT,
                SlimefunItems.CARBON_PRESS_3,SlimefunItems.PROGRAMMABLE_ANDROID_3,SlimefunItems.CARBON_PRESS_3
        })
                .setCapacity(131452)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.COBBLESTONE,64)}, new ItemStack[] {MagicExpansionItems.COBBLESTONE_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COBBLESTONE_1,64)}, new ItemStack[] {MagicExpansionItems.COBBLESTONE_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.STONE,64)}, new ItemStack[] {MagicExpansionItems.STONE_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.STONE_1,64)}, new ItemStack[] {MagicExpansionItems.STONE_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.OAK_LOG,64)}, new ItemStack[] {MagicExpansionItems.OAK_LOG_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.OAK_LOG_1,64)}, new ItemStack[] {MagicExpansionItems.OAK_LOG_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.STONE_BRICKS,64)}, new ItemStack[] {MagicExpansionItems.STONE_BRICKS_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,64)}, new ItemStack[] {MagicExpansionItems.STONE_BRICKS_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.BRICKS,64)}, new ItemStack[] {MagicExpansionItems.BRICKS_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.BRICKS_1,64)}, new ItemStack[] {MagicExpansionItems.BRICKS_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.REDSTONE,64)}, new ItemStack[] {MagicExpansionItems.REDSTONE_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.REDSTONE_1,64)}, new ItemStack[] {MagicExpansionItems.REDSTONE_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.REDSTONE_TORCH,64)}, new ItemStack[] {MagicExpansionItems.REDSTONE_TORCH_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.REDSTONE_TORCH_1,64)}, new ItemStack[] {MagicExpansionItems.REDSTONE_TORCH_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.REPEATER,64)}, new ItemStack[] {MagicExpansionItems.REPEATER_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.REPEATER_1,64)}, new ItemStack[] {MagicExpansionItems.REPEATER_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.COMPARATOR,64)}, new ItemStack[] {MagicExpansionItems.COMPARATOR_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COMPARATOR_1,64)}, new ItemStack[] {MagicExpansionItems.COMPARATOR_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.HOPPER,64)}, new ItemStack[] {MagicExpansionItems.HOPPER_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.HOPPER_1,64)}, new ItemStack[] {MagicExpansionItems.HOPPER_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.STRING,64)}, new ItemStack[] {MagicExpansionItems.STRING_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.STRING_1,64)}, new ItemStack[] {MagicExpansionItems.STRING_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.TRIPWIRE_HOOK,64)}, new ItemStack[] {MagicExpansionItems.TRIPWIRE_HOOK_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.TRIPWIRE_HOOK_1,64)}, new ItemStack[] {MagicExpansionItems.TRIPWIRE_HOOK_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.FURNACE,64)}, new ItemStack[] {MagicExpansionItems.FURNACE_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.FURNACE_1,64)}, new ItemStack[] {MagicExpansionItems.FURNACE_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.IRON_INGOT,64)}, new ItemStack[] {MagicExpansionItems.IRON_INGOT_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT_1,64)}, new ItemStack[] {MagicExpansionItems.IRON_INGOT_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.GLASS,64)}, new ItemStack[] {MagicExpansionItems.GLASS_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.GLASS_1,64)}, new ItemStack[] {MagicExpansionItems.GLASS_2})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT_2,64)}, new ItemStack[] {MagicExpansionItems.IRON_INGOT_3})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.LIGHT,64)}, new ItemStack[] {MagicExpansionItems.LIGHT_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.LIGHT_1,64)}, new ItemStack[] {MagicExpansionItems.LIGHT_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}, new ItemStack[] {MagicExpansionItems.QUARTZ_BLOCK_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.QUARTZ_BLOCK_1,64)}, new ItemStack[] {MagicExpansionItems.QUARTZ_BLOCK_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.WHITE_WOOL,64),new ItemStack(Material.WHITE_DYE,64),
                        new ItemStack(Material.RED_DYE,64),new ItemStack(Material.YELLOW_DYE,64),new ItemStack(Material.BLUE_DYE,64)},
                        new ItemStack[] {MagicExpansionItems.COLOR_WOOL_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,64)}, new ItemStack[] {MagicExpansionItems.COLOR_WOOL_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.WHITE_TERRACOTTA,64),new ItemStack(Material.WHITE_DYE,64),
                                new ItemStack(Material.RED_DYE,64),new ItemStack(Material.YELLOW_DYE,64),new ItemStack(Material.BLUE_DYE,64)},
                        new ItemStack[] {MagicExpansionItems.COLOR_TERRACOTTA_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,64)}, new ItemStack[] {MagicExpansionItems.COLOR_TERRACOTTA_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.WHITE_CONCRETE,64),new ItemStack(Material.WHITE_DYE,64),
                                new ItemStack(Material.RED_DYE,64),new ItemStack(Material.YELLOW_DYE,64),new ItemStack(Material.BLUE_DYE,64)},
                        new ItemStack[] {MagicExpansionItems.COLOR_CONCRETE_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,64)}, new ItemStack[] {MagicExpansionItems.COLOR_CONCRETE_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.WHITE_GLAZED_TERRACOTTA,64),new ItemStack(Material.WHITE_DYE,64),
                                new ItemStack(Material.RED_DYE,64),new ItemStack(Material.YELLOW_DYE,64),new ItemStack(Material.BLUE_DYE,64)},
                        new ItemStack[] {MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_1,64)}, new ItemStack[] {MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_2})
                .addRecipe(2, new ItemStack[] {new ItemStack(Material.CHERRY_LEAVES,64),new ItemStack(Material.WHITE_DYE,64),
                                new ItemStack(Material.RED_DYE,64),new ItemStack(Material.YELLOW_DYE,64),new ItemStack(Material.BLUE_DYE,64)},
                        new ItemStack[] {MagicExpansionItems.COLOR_LEAVES_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,64)}, new ItemStack[] {MagicExpansionItems.COLOR_LEAVES_2})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.OAK_LOG_1,new ItemStack(Material.WHITE_DYE,64),
                                new ItemStack(Material.RED_DYE,64),new ItemStack(Material.YELLOW_DYE,64),new ItemStack(Material.BLUE_DYE,64)},
                        new ItemStack[] {MagicExpansionItems.COLOR_LOG_1})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_LOG_1,64)}, new ItemStack[] {MagicExpansionItems.COLOR_LOG_2})
                .register(plugin);


        //魔法材料工坊-逆向
        new RecipeMachinePreBuilding(magicexpansionenergy, MagicExpansionItems.PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED_REVERSE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.FURNACE),new ItemStack(Material.FURNACE),new ItemStack(Material.FURNACE),
                new ItemStack(Material.FURNACE),MagicExpansionItems.PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,new ItemStack(Material.FURNACE),
                new ItemStack(Material.FURNACE),new ItemStack(Material.FURNACE),new ItemStack(Material.FURNACE)
        })
                .setCapacity(131452)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COBBLESTONE_1}, new ItemStack[] {new ItemStack(Material.COBBLESTONE,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COBBLESTONE_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COBBLESTONE_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.STONE_1}, new ItemStack[] {new ItemStack(Material.STONE,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.STONE_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.STONE_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.OAK_LOG_1}, new ItemStack[] {new ItemStack(Material.OAK_LOG,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.OAK_LOG_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.OAK_LOG_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.STONE_BRICKS_1}, new ItemStack[] {new ItemStack(Material.STONE_BRICKS,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.STONE_BRICKS_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.BRICKS_1}, new ItemStack[] {new ItemStack(Material.BRICKS,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.BRICKS_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.BRICKS_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.REDSTONE_1}, new ItemStack[] {new ItemStack(Material.REDSTONE,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.REDSTONE_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.REDSTONE_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.REDSTONE_TORCH_1}, new ItemStack[] {new ItemStack(Material.REDSTONE_TORCH,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.REDSTONE_TORCH_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.REDSTONE_TORCH_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.REPEATER_1}, new ItemStack[] {new ItemStack(Material.REPEATER,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.REPEATER_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.REPEATER_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COMPARATOR_1}, new ItemStack[] {new ItemStack(Material.COMPARATOR,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COMPARATOR_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COMPARATOR_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.HOPPER_1}, new ItemStack[] {new ItemStack(Material.HOPPER,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.HOPPER_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.HOPPER_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.STRING_1}, new ItemStack[] {new ItemStack(Material.STRING,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.STRING_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.STRING_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.TRIPWIRE_HOOK_1}, new ItemStack[] {new ItemStack(Material.TRIPWIRE_HOOK,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.TRIPWIRE_HOOK_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.TRIPWIRE_HOOK_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.FURNACE_1}, new ItemStack[] {new ItemStack(Material.FURNACE,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.FURNACE_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.FURNACE_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.IRON_INGOT_1}, new ItemStack[] {new ItemStack(Material.IRON_INGOT,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.IRON_INGOT_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.GLASS_1}, new ItemStack[] {new ItemStack(Material.GLASS,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.GLASS_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.GLASS_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.LIGHT_1}, new ItemStack[] {new ItemStack(Material.LIGHT,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.IRON_INGOT_3}, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT_2,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.LIGHT_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.LIGHT_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.QUARTZ_BLOCK_1}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.QUARTZ_BLOCK_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.QUARTZ_BLOCK_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_WOOL_1}, new ItemStack[] {new ItemStack(Material.WHITE_WOOL,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_WOOL_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_TERRACOTTA_1}, new ItemStack[] {new ItemStack(Material.WHITE_TERRACOTTA,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_TERRACOTTA_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_CONCRETE_1}, new ItemStack[] {new ItemStack(Material.WHITE_CONCRETE,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_CONCRETE_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_1}, new ItemStack[] {new ItemStack(Material.WHITE_GLAZED_TERRACOTTA,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_LEAVES_1}, new ItemStack[] {new ItemStack(Material.CHERRY_LEAVES,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_LEAVES_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,64)})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_LOG_1}, new ItemStack[] {MagicExpansionItems.OAK_LOG_1})
                .addRecipe(2, new ItemStack[] {MagicExpansionItems.COLOR_LOG_2}, new ItemStack[] {sfItemAmount(MagicExpansionItems.COLOR_LOG_1,64)})
                .register(plugin);

        //终极魔法建筑工坊
        new RecipeMachinePreBuilding(magicexpansionenergy, MagicExpansionItems.PRE_BUILDINGS_MACHINE_ADVANCED, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.BRICKS_2, SlimefunItems.ENERGIZED_CAPACITOR,MagicExpansionItems.STONE_BRICKS_2,
                MagicExpansionItems.REDSTONE_TORCH_1,MagicExpansionItems.PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,MagicExpansionItems.REPEATER_1,
                MagicExpansionItems.COMPARATOR_1,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.HOPPER_1
        })
                .setCapacity(131452)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(20,new ItemStack[] {MagicExpansionItems.STONE_2,MagicExpansionItems.IRON_INGOT_2,sfItemAmount(MagicExpansionItems.OAK_LOG_1,16),
                        sfItemAmount(MagicExpansionItems.REDSTONE_1,5),MagicExpansionItems.GLASS_1,MagicExpansionItems.COLOR_WOOL_1,
                                MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_VILLAGE_LOVE_AND_TRADE_HOUSE})
                .addRecipe(20,new ItemStack[] { MagicExpansionItems.STONE_2,MagicExpansionItems.IRON_INGOT_1,sfItemAmount(MagicExpansionItems.OAK_LOG_1,16),
                        MagicExpansionItems.REDSTONE_1,MagicExpansionItems.COBBLESTONE_1,MagicExpansionItems.HOPPER_1,
                        new ItemStack(Material.STRING,15),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_SHULKER_FARM})
                .addRecipe(20,new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,48),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,6),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,6),
                        sfItemAmount(MagicExpansionItems.IRON_INGOT_1,8),sfItemAmount(MagicExpansionItems.COLOR_LOG_2,1),sfItemAmount(MagicExpansionItems.REDSTONE_2,1),
                        NamedTagBuilder.nameTag("Mcdonalds"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_MCDONALDS})

                .addRecipe(20,new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,4),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,36),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,2),
                        sfItemAmount(MagicExpansionItems.STONE_2,2),sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),
                        NamedTagBuilder.nameTag("MoonRabbitShop"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_MOON_RABBIT_SHOP})
                .addRecipe(20,new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.COLOR_LOG_1,10),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,2),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),
                        sfItemAmount(MagicExpansionItems.STONE_2,2),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,2),
                        NamedTagBuilder.nameTag("MiddleHorseHouse"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_MIDDLE_HORSE_HOUSE})
                .addRecipe(20,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,14),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,6),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                                sfItemAmount(MagicExpansionItems.COBBLESTONE_1,5),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,8),sfItemAmount(MagicExpansionItems.OAK_LOG_1,5),
                                NamedTagBuilder.nameTag("SiHeYuan"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_SI_HE_YUAN})
                .addRecipe(20,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,23),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,9),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,19),
                                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,5),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,3),sfItemAmount(MagicExpansionItems.STONE_1,2),
                                NamedTagBuilder.nameTag("KFCMiddle"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_KFC_MIDDLE})
                .addRecipe(20,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,25),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,21),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,18),
                                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,3),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,2),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,2),
                                NamedTagBuilder.nameTag("LargeSnowKing"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_LARGE_SNOW_KING})
                .addRecipe(20,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_LOG_2,1),sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_2,1),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,7),
                                sfItemAmount(MagicExpansionItems.OAK_LOG_1,8),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),new ItemStack(Material.DRAGON_EGG),
                                NamedTagBuilder.nameTag("MiddleVilla"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_MIDDLE_VILLA})
                .addRecipe(20,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,1),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,6),
                                sfItemAmount(MagicExpansionItems.GLASS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,5),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,7),
                                NamedTagBuilder.nameTag("SakuraShop"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_SAKURA_SHOP})
                .addRecipe(20,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.OAK_LOG_1,35),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,16),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,3),
                                sfItemAmount(MagicExpansionItems.GLASS_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),sfItemAmount(MagicExpansionItems.LIGHT_1,3),
                                NamedTagBuilder.nameTag("KrustyKrab"),MagicExpansionItems.SPACE_INFINITY_MAGIC},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_KRUSTY_KRAB})
                .register(plugin);

        //泥土园
        new TemplateMachine(magicexpansionrecipemachine, MagicExpansionItems.DIRT_MEAL_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.DIRT, MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.DIRT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.JIN_KE_LA,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.GRASS_BLOCK),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.GRASS_BLOCK,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.GRASS_BLOCK),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.GRASS_BLOCK,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.PODZOL),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.PODZOL,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.PODZOL),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.PODZOL,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.MYCELIUM),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.MYCELIUM,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.MYCELIUM),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.MYCELIUM,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.DIRT_PATH),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.DIRT_PATH,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.DIRT_PATH),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.DIRT_PATH,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.COARSE_DIRT),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.COARSE_DIRT,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.COARSE_DIRT),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.COARSE_DIRT,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.ROOTED_DIRT),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.ROOTED_DIRT,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.ROOTED_DIRT),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.ROOTED_DIRT,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.FARMLAND),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.FARMLAND,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.FARMLAND),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.FARMLAND,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.CLAY),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.CLAY,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.CLAY),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.CLAY,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.WHITE_CONCRETE),
                                new ItemStack(Material.DIRT,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.WHITE_CONCRETE,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.WHITE_CONCRETE),
                                new ItemStack(Material.DIRT,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.WHITE_CONCRETE,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.CRIMSON_NYLIUM),
                                new ItemStack(Material.NETHERRACK,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.CRIMSON_NYLIUM,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.CRIMSON_NYLIUM),
                                new ItemStack(Material.NETHERRACK,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.CRIMSON_NYLIUM,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.WARPED_NYLIUM),
                                new ItemStack(Material.NETHERRACK,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.WARPED_NYLIUM,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.WARPED_NYLIUM),
                                new ItemStack(Material.NETHERRACK,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.WARPED_NYLIUM,64)})
                .addRecipe(3,new ItemStack[] {new ItemStack(Material.SOUL_SOIL),
                                new ItemStack(Material.NETHERRACK,16),new ItemStack(Material.BONE_MEAL,16)},
                        new ItemStack[] {new ItemStack(Material.SOUL_SOIL,16)})
                .addRecipe(0,new ItemStack[] {new ItemStack(Material.SOUL_SOIL),
                                new ItemStack(Material.NETHERRACK,64),MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.SOUL_SOIL,64)})
                .register(plugin);


        //钓鱼
        new TemplateMachine(magicexpansionrecipemachine, MagicExpansionItems.FISHING_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.PROGRAMMABLE_ANDROID_FISHERMAN, MagicExpansionItems.CORE_ORIGIN,SlimefunItems.PROGRAMMABLE_ANDROID_FISHERMAN,
                MagicExpansionItems.ELEMENT_INGOT,new ItemStack(Material.COD),MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(16,new ItemStack[] {new ItemStack(Material.FISHING_ROD),
                                new ItemStack(Material.APPLE,1)},
                        new ItemStack[] {new ItemStack(Material.COD,16)})
                .addRecipe(16,new ItemStack[] {new ItemStack(Material.FISHING_ROD),
                                new ItemStack(Material.CARROT,1)},
                        new ItemStack[] {new ItemStack(Material.SALMON,16)})
                .addRecipe(16,new ItemStack[] {new ItemStack(Material.FISHING_ROD),
                                new ItemStack(Material.SWEET_BERRIES,1)},
                        new ItemStack[] {new ItemStack(Material.TROPICAL_FISH,16)})
                .addRecipe(16,new ItemStack[] {new ItemStack(Material.FISHING_ROD),
                                new ItemStack(Material.SPIDER_EYE,1)},
                        new ItemStack[] {new ItemStack(Material.PUFFERFISH,16)})
                .addRecipe(8,new ItemStack[] {new ItemStack(Material.FISHING_ROD),
                                MagicExpansionItems.JIN_KE_LA},
                        new ItemStack[] {new ItemStack(Material.COD,8),new ItemStack(Material.SALMON,6),
                                new ItemStack(Material.TROPICAL_FISH,4),new ItemStack(Material.PUFFERFISH,2),
                                new ItemStack(Material.INK_SAC,1),new ItemStack(Material.GLOW_INK_SAC,1),
                                new ItemStack(Material.BONE,1),new ItemStack(Material.ENCHANTED_BOOK,1),
                                new ItemStack(Material.SPIDER_EYE,1),new ItemStack(Material.BLAZE_ROD,1)})
                .register(plugin);

        if (IfItemXist("MAGIC_REDSTONE")) {
            //魔法资源开采集-rsc
            new RecipeMachine(magicexpansionrscmagic, MagicExpansionItems.TWO_TO_MAGIC_GEO_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[]{
                    SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.SUGAR),
                    SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.FURNACE), new ItemStack(Material.SUGAR),
                    SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.SUGAR), new ItemStack(Material.SUGAR)
            })
                    .setCapacity(1314)
                    .setConsumption(26)
                    .setProcessingSpeed(1)
                    .addRecipe(1, new ItemStack[]{sfItemAmount(MagicExpansionItems.RSC_MAGIC_REDSTONE, 1)},
                            new ItemStack[]{stoneCreateItem("MAGIC_REDSTONE")})
                    .addRecipe(1, new ItemStack[]{sfItemAmount(MagicExpansionItems.RSC_MAGIC_COSMIC_DUST, 1)},
                            new ItemStack[]{stoneCreateItem("MAGIC_COSMIC_DUST")})
                    .addRecipe(1, new ItemStack[]{sfItemAmount(MagicExpansionItems.RSC_MAGIC_SOUL, 1)},
                            new ItemStack[]{stoneCreateItem("MAGIC_SOUL")})
                    .register(plugin);

        }


        //元素提纯机
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.INGOT_PURE_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.TRASH_CAN, SlimefunItems.ESSENCE_OF_AFTERLIFE,SlimefunItems.BOOSTED_URANIUM,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.ELEMENT_INGOT,
                SlimefunItems.CARBON_PRESS_3,MagicExpansionItems.AMETHYST_SHARD,SlimefunItems.CARBON_PRESS_3
        })
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_IRON})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.COPPER_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_COPPER})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.MAGNESIUM_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_MAGNESIUM})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.GOLD_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_GOLD})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.LEAD_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_LEAD})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.ALUMINUM_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_ALUMINUM})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.SILVER_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_SILVER})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.TIN_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_TIN})
                .addRecipe(2, new ItemStack[] {sfItemAmount(MagicExpansionItems.ZINC_INGOT,64),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8)},
                        new ItemStack[] {MagicExpansionItems.PURE_ZINC})
                .register(plugin);



        new IDCardMachineCN(magicexpansionrecipemachine, MagicExpansionItems.CHINESE_CHARACTER_CONSTRUCTOR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.INK_SAC), new ItemStack(Material.WRITABLE_BOOK),new ItemStack(Material.LEATHER),
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,new ItemStack(Material.LECTERN),MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.PAPER),new ItemStack(Material.BAMBOO),new ItemStack(Material.FEATHER),new ItemStack(Material.INK_SAC)},
                        new ItemStack[] {new CustomItemStack(Material.SUGAR, ColorGradient.getGradientNameVer2("Symbol"), "&fDream", ColorGradient.getGradientNameVer2("The foundation of creating matter from nothing."))})
                .register(plugin);


        new OriginMaterialGenMaker(magicexpansionenergy, ORIGIN_MATERIAL_GEN_MAKER_ALPHA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_RANDOM_SPAWNER, PURE_ELEMENT_INGOT,RESEARCH_UNLOCKER_PAPER,
                SCHRODINGER_FRAME_ONE,new ItemStack(Material.CRAFTING_TABLE),SCHRODINGER_FRAME_INFINITE,
                BASIC_ENCHANT_STONE,WIND_SPIRIT,BASIC_ENCHANT_STONE
        })
                .setCapacity(131452)
                .setConsumption(26000)
                .setProcessingSpeed(1)
                .addRecipe(5, new ItemStack[] {sfItemAmount(PURE_ELEMENT_GOLD,31),sfItemAmount(MAGIC_CAPACITY_ULTRA,1),sfItemAmount(PURE_ELEMENT_WOOD,31),
                                sfItemAmount(PURE_ELEMENT_WATER,31),          sfItemAmount(PURE_ELEMENT_FIRE,31),
                                        sfItemAmount(PURE_FIVE_ELEMENT,9),sfItemAmount(PURE_ELEMENT_EARTH,31),sfItemAmount(MAGIC_EXPANSION_MAGIC_SUGAR_19,9)},
                        new ItemStack[] {MagicExpansionItems.ORIGIN_MATERIAL_GEN})
                .register(plugin);


        new OriginMaterialGenMakerUltra(magicexpansionenergy, MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_BETA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_MAGIC_SUGAR_29, MAGIC_EXPANSION_MAGIC_SUGAR_30,MAGIC_EXPANSION_MAGIC_SUGAR_31,
                new ItemStack(Material.SUGAR),ORIGIN_MATERIAL_GEN_MAKER_ALPHA,new ItemStack(Material.STRING),
                MAGIC_EXPANSION_FINAL_STRING_19,MAGIC_EXPANSION_FINAL_STRING_20,MAGIC_EXPANSION_FINAL_STRING_21
        })
                .setCapacity(131452)
                .setConsumption(26000)
                .setProcessingSpeed(1)
                .addRecipe(5, new ItemStack[] {sfItemAmount(ITEM_ORIGIN_BACK_TRACK,1),sfItemAmount(MAGIC_CAPACITY_ULTRA,1),sfItemAmount(MAGIC_EXPANSION_MAGIC_SUGAR_31,31),
                                sfItemAmount(PURE_ELEMENT_WATER,1),          sfItemAmount(PURE_ELEMENT_FIRE,1),
                                sfItemAmount(PURE_FIVE_ELEMENT,31),sfItemAmount(PURE_ELEMENT_EARTH,31),sfItemAmount(MAGIC_EXPANSION_MAGIC_SUGAR_19,31)},
                        new ItemStack[] {ORIGIN_MATERIAL_GEN_ULTRA})
                .register(plugin);

        //新增  万物演化·源起
        new OriginMaterialGenMakerLite(magicexpansionenergy, MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_LITE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                GOLD_ELEMENT, LIGHT_ENERGY_ALPHA,WOOD_ELEMENT,
                WATER_ELEMENT,REDSTONE_EXECUTE_ELEMENT,FIRE_ELEMENT,
                BASIC_ENCHANT_STONE,EARTH_ELEMENT,WIND_SPIRIT
        })
                .setCapacity(131452)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(5, new ItemStack[] {sfItemAmount(GOLD_ELEMENT,11),sfItemAmount(BAD_LUCK_CAPACITY,1),sfItemAmount(WOOD_ELEMENT,45),
                                sfItemAmount(WATER_ELEMENT,14)              ,sfItemAmount(FIRE_ELEMENT,19),
                                sfItemAmount(FIVE_ELEMENT,8),sfItemAmount(EARTH_ELEMENT,19),sfItemAmount(PURE_ELEMENT_INGOT,10)},
                        new ItemStack[] {ORIGIN_MATERIAL_GEN_LITE})
                .register(plugin);




        //泥土园
        new ItemOriginBackTrackMachine(magicexpansionenergy, MagicExpansionItems.ITEM_ORIGIN_BACK_TRACK, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.PURE_ELEMENT_INGOT, MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.PURE_ELEMENT_INGOT,
                MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.WORLD_CORE,MagicExpansionItems.PURE_ELEMENT_INGOT,
                MagicExpansionItems.BASIC_ENCHANT_STONE,MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_37,MagicExpansionItems.WIND_SPIRIT
        })
                .setCapacity(2147483647)
                .setConsumption(2147483646)
                .setProcessingSpeed(1)
                .addRecipe(5,new ItemStack[] {new CustomItemStack(doGlow(Material.LIGHT), ColorGradient.getGradientName("Insert any Slimefun item."),ColorGradient.getGradientName("The machine will trace the item back to its source recipe."))},
                        new ItemStack[] {new CustomItemStack(doGlow(Material.NETHER_STAR), ColorGradient.getGradientName("After processing, the machine outputs the original recipe materials."))})
                .register(plugin);
















        new RecipeMachineCrude(magicexpansionrecipemachine, MagicExpansionItems.CRUDE_MACHINE_TEST, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.TRASH_CAN, SlimefunItems.TRASH_CAN,SlimefunItems.TRASH_CAN,
                MagicExpansionItems.ELEMENT_INGOT, MAGIC_EXPANSION_MAGIC_SUGAR_3,MagicExpansionItems.ELEMENT_INGOT,
                SlimefunItems.TRASH_CAN,MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.TRASH_CAN
        }, 10, 40)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT,1),sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,1)},
                        new ItemStack[] {sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,2)})
                .register(plugin);










    }

}
