package io.Yomicer.magicExpansion;

import io.Yomicer.magicExpansion.items.electric.entitykillMachinee.EntityKillMachine;
import io.Yomicer.magicExpansion.items.electric.geoMachine.FiveElementsMiner;
import io.Yomicer.magicExpansion.items.electric.geoMachine.RSCMagicMiner;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RandomBoxMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeRandomMachine;
import io.Yomicer.magicExpansion.items.electric.recipeMachine.RecipeRandomMachineDefault;
import io.Yomicer.magicExpansion.items.electric.resourceGenerator.*;
import io.Yomicer.magicExpansion.items.enchantMachine.EnchantingTable;
import io.Yomicer.magicExpansion.items.misc.*;
import io.Yomicer.magicExpansion.items.misc.FishWeightEnhancer;
import io.Yomicer.magicExpansion.items.misc.fish.CommonFish;
import io.Yomicer.magicExpansion.items.misc.fish.CommonFishHidden;
import io.Yomicer.magicExpansion.items.misc.fish.FishingBook;
import io.Yomicer.magicExpansion.items.misc.fish.PowerEel;
import io.Yomicer.magicExpansion.items.misc.magicAlter.MagicWand;
import io.Yomicer.magicExpansion.items.misc.weapon.StarShardsSword;
import io.Yomicer.magicExpansion.items.preBuildings.PreBuildingTree;
import io.Yomicer.magicExpansion.items.quickMachine.*;
import io.Yomicer.magicExpansion.items.skyBlock.SingleCubeOre;
import io.Yomicer.magicExpansion.items.skyBlock.SingleCubeOrigin;
import io.Yomicer.magicExpansion.items.summonBossItem.FireZombie;
import io.Yomicer.magicExpansion.items.summonBossItem.WindElf;
import io.Yomicer.magicExpansion.items.tools.*;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.Yomicer.magicExpansion.utils.itemUtils.NamedTagBuilder;
import io.Yomicer.magicExpansion.utils.itemUtils.newItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.HiddenItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.Capacitor;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.EnergyConnector;
import io.github.thebusybiscuit.slimefun4.implementation.items.geo.GEOMiner;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.*;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_ALPHA;
import static io.Yomicer.magicExpansion.utils.ColorGradient.*;
import static io.Yomicer.magicExpansion.utils.ConvertItem.*;
import static io.Yomicer.magicExpansion.utils.Language.get;
import static io.Yomicer.magicExpansion.utils.Language.getList;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;
import static io.Yomicer.magicExpansion.utils.itemUtils.sfItemUtils.sfItemAmount;


public final class MagicExpansionItemSetup {

    // ItemGroups
    public static final NestedItemGroup magicexpansion = new NestedItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion"),
            new CustomItemStack(doGlow(Material.LIGHT),"§x§F§D§B§7§D§4§kll§x§F§D§B§7§D§4§x§F§A§7§E§B§3§x§F§F§6§9§B§42.§x§F§F§6§9§B§40§x§F§D§B§7§D§4§kll"),0
    );

    private static final ItemGroup magicexpansioncontribution = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "contribution"), magicexpansion,
            new CustomItemStack(doGlow(Material.COMMAND_BLOCK), ColorGradient.getGradientNameVer2("Contributors")), 0
    );

    private static final ItemGroup magicexpansionresource = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "resource"), magicexpansion,
            new CustomItemStack(doGlow(Material.SNOWBALL), ColorGradient.getGradientName("Resources")), 1
    );

    private static final ItemGroup magicexpansionresourcedlc = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "resourcedlc"), magicexpansion,
            new CustomItemStack(doGlow(Material.TOTEM_OF_UNDYING), getGradientName("Patch Materials")), 2
    );

    private static final ItemGroup magicexpansionspecialitem = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "specialitem"), magicexpansion,
            new CustomItemStack(doGlow(Material.SHEARS), getGradientName("Special Items")), 3
    );

    public static final ItemGroup magicexpansionquickmachine = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "quickmachine"), magicexpansion,
            new CustomItemStack(doGlow(Material.MAGENTA_GLAZED_TERRACOTTA), getGradientName("Quick Machines")), 4
    );

    public static final ItemGroup magicexpansionresourcegenerator = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "resourcegenerator"), magicexpansion,
            new CustomItemStack(doGlow(Material.LANTERN), ColorGradient.getGradientName("Resource Generators")), 5
    );
    public static final ItemGroup magicexpansionrecipemachine = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "recipemachine"), magicexpansion,
            new CustomItemStack(doGlow(Material.SOUL_CAMPFIRE), ColorGradient.getGradientName("Production Machines")), 5
    );

    public static final ItemGroup magicexpansionenergy = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "energy"), magicexpansion,
            new CustomItemStack(doGlow(Material.SOUL_LANTERN), ColorGradient.getGradientName("Special Machines")), 6
    );

    public static final ItemGroup magicexpansionpower = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "power"), magicexpansion,
            new CustomItemStack(CustomHead.getHead("24378b986e358555ee73f09b210d49ec13719de5ea88d75523770d31163f3aef"), ColorGradient.getGradientName("Generators")), 6
    );

    private static final ItemGroup magicexpansionelectricbot = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "electric_bot"), magicexpansion,
            new CustomItemStack(CustomHead.BOT_PINK.getItem(), ColorGradient.getGradientName("Electric Machines")), 6
    );

    private static final ItemGroup magicexpansionboss = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "boss"), magicexpansion,
            new CustomItemStack(doGlow(Material.ENDER_DRAGON_SPAWN_EGG), ColorGradient.getGradientName("Magic Bosses")), 7
    );

    private static final ItemGroup magicexpansionforge = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "forge"), magicexpansion,
            new CustomItemStack(doGlow(Material.ANVIL), ColorGradient.getGradientName("Magic Forging")), 8
    );

    private static final ItemGroup magicexpansionprebuildingresource = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "prebuildingresource"), magicexpansion,
            new CustomItemStack(doGlow(Material.PRISMARINE_CRYSTALS), ColorGradient.getGradientName("Building Materials")), 9
    );

    private static final ItemGroup magicexpansionprebuilding = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "prebuilding"), magicexpansion,
            new CustomItemStack(doGlow(Material.BRICKS), ColorGradient.getGradientName("Prefabricated Buildings")), 10
    );

    public static final ItemGroup magicexpansionhonkai = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "honkai"), magicexpansion,
            new CustomItemStack(CustomHead.SILVER_WOLF.getItem(), ColorGradient.getRandomGradientName("Honkai: Star Rail")), 10
    );

    public static final ItemGroup magicexpansionskyblock = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "skyblock"), magicexpansion,
            new CustomItemStack(CustomHead.getHead("7948942fed672ded57f8cdb169a5076062586e77002ee30e07693c025e6f0db5"), ColorGradient.getGradientName("Magic SkyBlock")), 10
    );

    public static final ItemGroup magicexpansionrscmagic = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "rscmagic"), magicexpansion,
            new CustomItemStack(CustomHead.MAGICSOLO.getItem(), ColorGradient.getRandomGradientName("Minecraft 1.21 Fixes and Optimizations")), 10
    );

    public static final ItemGroup magicexpansionfishing = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "fishing"), magicexpansion,
            new CustomItemStack(doGlow(Material.FISHING_ROD), ColorGradient.getGradientName("Fishing")), 10
    );

    public static final ItemGroup magicexpansionnonsensical = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "nonsensical"), magicexpansion,
            new CustomItemStack(doGlow(Material.DRAGON_EGG), ColorGradient.getRandomGradientName("Novelty Items")), 10
    );
    public static final ItemGroup magicexpansioncommemorate = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "commemorate"), magicexpansion,
            new CustomItemStack(doGlow(Material.FIREWORK_ROCKET), ColorGradient.getRandomGradientName("Souvenirs")), 10
    );
    public static final ItemGroup magicexpansionfood = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "food"), magicexpansion,
            new CustomItemStack(doGlow(Material.CAKE), ColorGradient.getGradientNameVer2("Food")), 10
    );
    public static final ItemGroup magicexpansionfoodresource = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "foodresource"), magicexpansion,
            new CustomItemStack(doGlow(Material.WHEAT), ColorGradient.getGradientNameVer2("Ingredients")), 10
    );

    public static final ItemGroup magicexpansioncooperatecreate = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "cooperatecreate"), magicexpansion,
            new CustomItemStack(CustomHead.getHead("7971e55df39a58faad05742d882e9a05ffa511a0c58e4f687777bb74614006eb"), ColorGradient.getGradientNameVer2("Community Creations")), 10
    );

    private static final ItemGroup magicexpansionupdateinfo = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "updateinfo"), magicexpansion,
            new CustomItemStack(doGlow(Material.PAPER), ColorGradient.getRandomGradientName("MagicExpansion Changelog")), 11
    );

    private static final ItemGroup magicexpansionnamecollection = new SubItemGroup(
            new NamespacedKey(MagicExpansion.getInstance(), "namecollection"), magicexpansion,
            new CustomItemStack(doGlow(Material.FLINT), ColorGradient.getRandomGradientName("Credits")), 11
    );


    // 定义一个新的配方类型
    public static final RecipeType SPECIAL_RECIPE_TYPE = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_special_recipe_type"),
            new CustomItemStack(Material.TOTEM_OF_UNDYING,"§x§F§D§B§7§D§4§x§F§A§7§E§B§3§x§F§F§6§9§B§4§x§F§F§6§9§B§4",getGradientName("Special Recipe Type"),getGradientName("Displayed in the Slimefun guide")), // 图标
            (input, output) -> {});

    // 光能激发器
    public static final RecipeType LIGHT_TRANSFORM_BASIC = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_light_transform_basic"),
            new CustomItemStack(Material.AMETHYST_BLOCK,getGradientName("Light Transform Basic"),getGradientName("Light Transform Basic"),"",getGradientName("Function:"),
                    getGradientName("Energy Requirement: Consumes a small amount of power each second to extract energy from light."),getGradientName(""),getGradientName("\"Finite light, finite energy...\"")),
            (input, output) -> {});

    // 魔法建筑工坊
    public static final RecipeType PRE_BUILDINGS_MACHINE = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_pre_buildings_machine"),
            new CustomItemStack(Material.PINK_GLAZED_TERRACOTTA,getGradientName("Magic Construction Workshop"),getGradientName("A mysterious workshop filled with construction plans."),"",getGradientName("Provide the materials and it can construct the design for you."),
                    getGradientName(""),getGradientName("Function: Create prefabricated buildings"),getGradientName("Energy Requirement: Low power use per second")
                    ,getGradientName(""),getGradientName("Is it magic, or the crystallized wisdom of ancient artisans?"),getGradientName("Either way, this workshop carries secrets lost to time.")),
            (input, output) -> {});

    // 魔法建筑工坊
    public static final RecipeType PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_pre_buildings_resource_machine"),
            new CustomItemStack(Material.SEA_LANTERN,getGradientName("Magic Materials Workshop"),getGradientName("A mysterious workshop filled with precision instruments."),"",getGradientName("Provide the materials and it can compress them for you."),
                    getGradientName(""),getGradientName("Function: Create prefabricated buildings"),getGradientName("Energy Requirement: High power use per second")
                    ,getGradientName(""),getGradientName("Is it magic, or the crystallized wisdom of ancient artisans?"),getGradientName("Either way, this workshop carries secrets lost to time.")),
            (input, output) -> {});

    // 终极魔法建筑工坊
    public static final RecipeType PRE_BUILDINGS_MACHINE_ADVANCED = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_pre_buildings_machine_advance"),
            new CustomItemStack(Material.CARTOGRAPHY_TABLE,getGradientName("Ultimate Magic Construction Workshop"),getGradientName("A mysterious workshop filled with construction plans."),"",getGradientName("Provide the materials and it can construct the design for you."),
                    getGradientName(""),getGradientName("Function: Create prefabricated buildings"),getGradientName("Energy Requirement: High power use per second")
                    ,getGradientName(""),getGradientName("Is it magic, or the crystallized wisdom of ancient artisans?"),getGradientName("Either way, this workshop carries secrets lost to time.")),
            (input, output) -> {});

    // 五行资源采集器
    public static final RecipeType FIVE_ELEMENT_MINER = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_five_element_miner"),
            new CustomItemStack(new CustomItemStack(CustomHead.getHead("846472b7bd8de52a101584ea2dd7db190d417ca548bb6568379acebdd02b1799"),
                    getGradientName("Five Element Miner"),getGradientName(" "),getGradientName("type"),
                    getGradientName("Extract Five-Element resources from the chunk."),getGradientName("Can obtain resources that ordinary pickaxes cannot mine."),getGradientName(" ")
                    ,getGradientName("Requires a GEO resource source"))),
            (input, output) -> {});

    // 五行资源采集器
    public static final RecipeType INGOT_PURE_MACHINE = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_ingot_pure_machine"),
            new CustomItemStack(new CustomItemStack(Material.SMOKER,
                    getGradientName("Ingot Pure Machine"),getGradientName("and?"))),
            (input, output) -> {});

    //  掉落物
    public static final RecipeType MAGICEXPANSION_MOB_DROP = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_mob_drop"),
            new CustomItemStack(Material.TOTEM_OF_UNDYING,ColorGradient.getGradientName("Magic Creature Drop"),getGradientName("Summon a magical creature"),getGradientName("and defeat it"),getGradientName("for a chance to obtain this item.")), // 图标
            (input, output) -> {});

    //  掉落物
    public static final RecipeType ORIGIN_MATERIAL_GEN_MAKER_ALPHA = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_origin_material_gen_maker_alpha"),
            MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_ALPHA, // 图标
            (input, output) -> {});
    public static final RecipeType ORIGIN_MATERIAL_GEN_MAKER_BETA = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_origin_material_gen_maker_beta"),
            MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_BETA, // 图标
            (input, output) -> {});
    public static final RecipeType ORIGIN_MATERIAL_GEN_MAKER_LITE = new RecipeType(
            new NamespacedKey(MagicExpansion.getInstance(), "magicexpansion_origin_material_gen_maker_lite"),
            MagicExpansionItems.ORIGIN_MATERIAL_GEN_MAKER_LITE, // 图标
            (input, output) -> {});

    private MagicExpansionItemSetup() {
    }


    public static void setup(@Nonnull MagicExpansion plugin) {


        //版本信息
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.MAGIC_EXPANSION_INFO, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);

        //magicsolo头颅
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.MAGIC_EXPANSION_AUTHOR, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);

//        registerUIBlockInContribution(MagicExpansionItems.UI_3,plugin);
        registerUIBlockInContribution(MagicExpansionItems.UI_WIKI,plugin);


        Config cfg = new Config(plugin);

        if (cfg.getBoolean("options.auto-update") && MagicExpansion.getInstance().getDescription().getVersion().startsWith("Build ")) {
            registerUIBlockInContribution(MagicExpansionItems.UI_IS_LATEST_BUILD,plugin);
        }else{
            registerUIBlockInContribution(MagicExpansionItems.UI_NOT_LATEST_BUILD,plugin);
        }



//        registerUIBlockInContribution(MagicExpansionItems.UI_4,plugin);


        registerUIBlockInContribution(MagicExpansionItems.UI_5,plugin);
        registerUIBlockInContribution(MagicExpansionItems.UI_6,plugin);
        registerUIBlockInContribution(MagicExpansionItems.UI_7,plugin);
        registerUIBlockInContribution(MagicExpansionItems.UI_8,plugin);
        registerUIBlockInContribution(MagicExpansionItems.UI_9,plugin);
        //UI
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.UI_THX, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.NAZUKICYL_TEST, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.HAIMAN_TEST, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.QIZHIYI_TEST, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansioncontribution, MagicExpansionItems.KOMU_A, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansioncontribution, LENGSHANG_TEST, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);





        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_06_23, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_06_29, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_07_12, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_07_23, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_07_25, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_07_26, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_08_01, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_08_27, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_08_29, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, MagicExpansionItems.UPDATE_LOG_2025_08_30, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_02, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_05, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_09, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_13, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_14, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_18, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //更新日志
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_20, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_27, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_09_29, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_10_06, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_10_07, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_10_14, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_10_23, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_10_25, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_10_26, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_02, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_05, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_07, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_08, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_12, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_21, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_22, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_11_23, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_01, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_06, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_13, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_16, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_19, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_20, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2025_12_31, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_01_02, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_01_23, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_01_27, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_01_28, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_02_19, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_03_10, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_03_13, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_03_17, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_03_22, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_04_18, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_05_21, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_06_24, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_07_03, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_07_12, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_07_30, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        new UnplaceableBlock(magicexpansionupdateinfo, UPDATE_LOG_2026_08_05, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);





        //快捷机器提示
        new UnplaceableBlock(magicexpansionquickmachine, MagicExpansionItems.MAGIC_EXPANSION_QUICK_MACHINE_INFO, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);

        //初级万能魔法合成材料
        new UnplaceableBlock(magicexpansionresourcedlc, MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.RAINBOW_WOOL, AMETHYST_SHARD, SlimefunItems.RAINBOW_WOOL,
                AMETHYST_SHARD, SlimefunItems.MAGIC_EYE_OF_ENDER, AMETHYST_SHARD,
                SlimefunItems.RAINBOW_WOOL, AMETHYST_SHARD, SlimefunItems.RAINBOW_WOOL
        }).register(plugin);

        //进阶万能魔法合成材料
        new UnplaceableBlock(magicexpansionresourcedlc, MagicExpansionItems.MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                DIAMOND, MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC, MagicExpansionItems.QUARTZ,
                MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC, MagicExpansionItems.ELEMENT_INGOT, MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC,
                MagicExpansionItems.NETHERITE_INGOT, MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC, MagicExpansionItems.EMERALD
        }).register(plugin);


        //魔法交互核心
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.MAGIC_EXPANSION_INTERACTIVE_CORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.QUARTZ, SlimefunItems.POWER_CRYSTAL, MagicExpansionItems.QUARTZ,
                SlimefunItems.ADVANCED_CIRCUIT_BOARD, MagicExpansionItems.ELEMENT_INGOT, SlimefunItems.ADVANCED_CIRCUIT_BOARD,
                MagicExpansionItems.QUARTZ, MagicExpansionItems.AMETHYST_SHARD, MagicExpansionItems.QUARTZ
        }).register(plugin);


        // Tools
        //镰刀
        new Scythe(magicexpansionspecialitem, MagicExpansionItems.SCYTHE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT),
                null, new ItemStack(Material.IRON_INGOT), null,
                null, new ItemStack(Material.STICK), null
        }).register(plugin);
        //无尽打火石
        new InfiniteTool(magicexpansionspecialitem, MagicExpansionItems.INFINITY_FLINT_AND_STEEL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.IRON_INGOT,MagicExpansionItems.IRON_INGOT, MagicExpansionItems.IRON_INGOT,
                MagicExpansionItems.IRON_INGOT,new ItemStack(Material.FLINT),MagicExpansionItems.IRON_INGOT,
                MagicExpansionItems.IRON_INGOT,MagicExpansionItems.IRON_INGOT,MagicExpansionItems.IRON_INGOT
        }).register(plugin);
        // 随机实体蛋
        new MagicExpansionRandomSummon(magicexpansionspecialitem, MagicExpansionItems.MAGIC_EXPANSION_RANDOM_SPAWNER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_3, SlimefunItems.ENDER_LUMP_3, SlimefunItems.MAGIC_LUMP_3,
                SlimefunItems.ENDER_LUMP_3, BasicCreateItem("MAGIC_REDSTONE"), SlimefunItems.ENDER_LUMP_3,
                SlimefunItems.MAGIC_LUMP_3, SlimefunItems.ENDER_LUMP_3, SlimefunItems.MAGIC_LUMP_3
        }).register(plugin);

        //虚空之触
        new VoidTouch(magicexpansionspecialitem, MagicExpansionItems.VOID_TOUCH, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_EARTH, MagicExpansionItems.PURE_ELEMENT_INGOT, PURE_ELEMENT_EARTH,
                MagicExpansionItems.AMETHYST_SHARD, MagicExpansionItems.MAGIC_EXPANSION_RANDOM_SPAWNER, MagicExpansionItems.AMETHYST_SHARD,
                PURE_ELEMENT_EARTH, MagicExpansionItems.BASIC_ENCHANT_STONE, PURE_ELEMENT_EARTH
        }).register(plugin);
        //虚空之触-脚本专用
        new VoidTouchScript(magicexpansionspecialitem, MagicExpansionItems.VOID_TOUCH_SCRIPT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_WATER, MagicExpansionItems.PURE_ELEMENT_INGOT, PURE_ELEMENT_WATER,
                MagicExpansionItems.AMETHYST_SHARD, MagicExpansionItems.MAGIC_EXPANSION_RANDOM_SPAWNER, MagicExpansionItems.AMETHYST_SHARD,
                PURE_ELEMENT_WATER, MagicExpansionItems.BASIC_ENCHANT_STONE, PURE_ELEMENT_WATER
        }).register(plugin);
        //五行之触
        new FiveElementTouch(magicexpansionspecialitem, MagicExpansionItems.FIVE_ELEMENT_TOUCH, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.PURE_ELEMENT_INGOT, MagicExpansionItems.PURE_ELEMENT_INGOT, MagicExpansionItems.PURE_ELEMENT_INGOT,
                MagicExpansionItems.VOID_TOUCH, MagicExpansionItems.PURE_FIVE_ELEMENT, MagicExpansionItems.VOID_TOUCH,
                MagicExpansionItems.PURE_ELEMENT_INGOT, MagicExpansionItems.PURE_ELEMENT_INGOT, MagicExpansionItems.PURE_ELEMENT_INGOT
        }).register(plugin);
        //五行之触-逆
        new FiveElementTouchLeftClick(magicexpansionspecialitem, MagicExpansionItems.FIVE_ELEMENT_TOUCH_RIGHT_CLICK, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1, MagicExpansionItems.FIVE_ELEMENT_TOUCH, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1
        }).register(plugin);



        //生死簿
        new DeathLifeBook(magicexpansionspecialitem, MagicExpansionItems.DEATH_LIFE_BOOK, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_FIRE, PURE_FIVE_ELEMENT, COAL,
                FIVE_ELEMENT_TOUCH_RIGHT_CLICK, MAGIC_EXPANSION_MAGIC_SUGAR_10, FIVE_ELEMENT_TOUCH,
                COAL, PURE_FIVE_ELEMENT, PURE_ELEMENT_FIRE
        }).register(plugin);


        //生死簿
        new ItemNameTag(magicexpansionspecialitem, MagicExpansionItems.ITEM_NAME_TAG, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.NAME_TAG), null,
                null, null, null,
                null, null, null
        }).register(plugin);


        // 附魔信息
        new UnplaceableBlock(magicexpansionenergy, MagicExpansionItems.MAGIC_EXPANSION_ENCHANTING_TABLE_INFO, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);

        // 附魔列表
        new UnplaceableBlock(magicexpansionenergy, MagicExpansionItems.MAGIC_EXPANSION_ENCHANTING_TABLE_LIST, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);

        // 魔法附魔台
        new EnchantingTable(magicexpansionenergy, MagicExpansionItems.MAGIC_EXPANSION_ENCHANTING_TABLE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.ENDER_EYE), AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.ENDER_EYE),
                AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.ENCHANTING_TABLE), AdvancedCreateItem("MAGIC_COSMIC_DUST"),
                new ItemStack(Material.ENDER_EYE), AdvancedCreateItem("MAGIC_COSMIC_DUST"), new ItemStack(Material.ENDER_EYE)
        }, getGradientName( "Magic Enchanting Table"),getGradientName("Magic Enchanting Table")).register(plugin);

        // 无厘头魔法糖
        new UnplaceableBlock(magicexpansionnonsensical, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }).register(plugin);
        // 1. 先创建一个数组,按顺序存放所有魔法糖常量
        SlimefunItemStack[] sugarLevels = {
                null, // 占位:让索引从 1 开始
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_2,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_3,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_4,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_5,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_6,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_7,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_8,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_9,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_10,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_11,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_12,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_13,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_14,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_15,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_16,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_17,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_18,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_19,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_20,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_21,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_22,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_23,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_24,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_25,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_26,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_27,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_28,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_29,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_30,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_31,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_32,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_33,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_34,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_35,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_36,
                MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_37,
        };
        // 2. 用 for 循环注册所有升级配方
        for (int n = 1; n <= 2; n++) {
            ItemStack[] recipe = {
                    sugarLevels[n], sugarLevels[n], sugarLevels[n],
                    sugarLevels[n], sugarLevels[n], sugarLevels[n],
                    sugarLevels[n], sugarLevels[n], sugarLevels[n]
            };

            new UnplaceableBlock(
                    magicexpansionnonsensical,
                    sugarLevels[n + 1],
                    RecipeType.ENHANCED_CRAFTING_TABLE,
                    recipe
            ).register(plugin);
        }
        // 2. 用 for 循环注册所有升级配方
        for (int n = 3; n <= 34; n++) {
            ItemStack[] recipe = {
                    sugarLevels[n], sugarLevels[n], sugarLevels[n],
                    sugarLevels[n], sugarLevels[n], sugarLevels[n],
                    sugarLevels[n], sugarLevels[n], sugarLevels[n]
            };

            new HiddenItem(
                    magicexpansionnonsensical,
                    sugarLevels[n + 1],
                    RecipeType.ENHANCED_CRAFTING_TABLE,
                    recipe
            ).register(plugin);
        }
        // 2. 用 for 循环注册所有升级配方
        for (int n = 35; n <= 36; n++) {
            ItemStack[] recipe = {
                    sugarLevels[n], sugarLevels[n], sugarLevels[n],
                    sugarLevels[n], sugarLevels[n], sugarLevels[n],
                    sugarLevels[n], sugarLevels[n], sugarLevels[n]
            };

            new UnplaceableBlock(
                    magicexpansionnonsensical,
                    sugarLevels[n + 1],
                    RecipeType.ENHANCED_CRAFTING_TABLE,
                    recipe
            ).register(plugin);
        }

        // 量子纠缠态·甘蔗
        new UnplaceableBlock(magicexpansionnonsensical, MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_CANE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.SUGAR_CANE),new ItemStack(Material.SUGAR_CANE),new ItemStack(Material.SUGAR_CANE),
                new ItemStack(Material.SUGAR_CANE),MAGIC_EXPANSION_MAGIC_SUGAR_37,new ItemStack(Material.SUGAR_CANE),
                new ItemStack(Material.SUGAR_CANE),new ItemStack(Material.SUGAR_CANE),new ItemStack(Material.SUGAR_CANE)
        }).register(plugin);

        // 递归记忆纤维
        new UnplaceableBlock(magicexpansionnonsensical, MagicExpansionItems.MAGIC_EXPANSION_FINAL_STRING_1, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_MAGIC_SUGAR_21,MAGIC_EXPANSION_MAGIC_SUGAR_21,MAGIC_EXPANSION_MAGIC_SUGAR_21,
                MAGIC_EXPANSION_MAGIC_SUGAR_21,new ItemStack(Material.STRING),MAGIC_EXPANSION_MAGIC_SUGAR_21,
                MAGIC_EXPANSION_MAGIC_SUGAR_21,MAGIC_EXPANSION_MAGIC_SUGAR_21,MAGIC_EXPANSION_MAGIC_SUGAR_21
        }).register(plugin);

        // 1. 先创建一个数组,按顺序存放所有魔法糖常量
        SlimefunItemStack[] stringLevels = {
                null, // 占位:让索引从 1 开始
                MAGIC_EXPANSION_FINAL_STRING_1,
                MAGIC_EXPANSION_FINAL_STRING_2,
                MAGIC_EXPANSION_FINAL_STRING_3,
                MAGIC_EXPANSION_FINAL_STRING_4,
                MAGIC_EXPANSION_FINAL_STRING_5,
                MAGIC_EXPANSION_FINAL_STRING_6,
                MAGIC_EXPANSION_FINAL_STRING_7,
                MAGIC_EXPANSION_FINAL_STRING_8,
                MAGIC_EXPANSION_FINAL_STRING_9,
                MAGIC_EXPANSION_FINAL_STRING_10,
                MAGIC_EXPANSION_FINAL_STRING_11,
                MAGIC_EXPANSION_FINAL_STRING_12,
                MAGIC_EXPANSION_FINAL_STRING_13,
                MAGIC_EXPANSION_FINAL_STRING_14,
                MAGIC_EXPANSION_FINAL_STRING_15,
                MAGIC_EXPANSION_FINAL_STRING_16,
                MAGIC_EXPANSION_FINAL_STRING_17,
                MAGIC_EXPANSION_FINAL_STRING_18,
                MAGIC_EXPANSION_FINAL_STRING_19,
                MAGIC_EXPANSION_FINAL_STRING_20,
                MAGIC_EXPANSION_FINAL_STRING_21

        };
        // 2. 用 for 循环注册所有升级配方
        for (int n = 1; n <= 2; n++) {
            ItemStack[] recipe = {
                    stringLevels[n], stringLevels[n], stringLevels[n],
                    stringLevels[n], stringLevels[n], stringLevels[n],
                    stringLevels[n], stringLevels[n], stringLevels[n]
            };

            new UnplaceableBlock(
                    magicexpansionnonsensical,
                    stringLevels[n + 1],
                    RecipeType.ENHANCED_CRAFTING_TABLE,
                    recipe
            ).register(plugin);
        }
        // 2. 用 for 循环注册所有升级配方
        for (int n = 3; n <= 18; n++) {
            ItemStack[] recipe = {
                    stringLevels[n], stringLevels[n], stringLevels[n],
                    stringLevels[n], stringLevels[n], stringLevels[n],
                    stringLevels[n], stringLevels[n], stringLevels[n]
            };

            new HiddenItem(
                    magicexpansionnonsensical,
                    stringLevels[n + 1],
                    RecipeType.ENHANCED_CRAFTING_TABLE,
                    recipe
            ).register(plugin);
        }
        // 2. 用 for 循环注册所有升级配方
        for (int n = 19; n <= 20; n++) {
            ItemStack[] recipe = {
                    stringLevels[n], stringLevels[n], stringLevels[n],
                    stringLevels[n], stringLevels[n], stringLevels[n],
                    stringLevels[n], stringLevels[n], stringLevels[n]
            };

            new UnplaceableBlock(
                    magicexpansionnonsensical,
                    stringLevels[n + 1],
                    RecipeType.ENHANCED_CRAFTING_TABLE,
                    recipe
            ).register(plugin);
        }

        // 终焉之丝
        new UnplaceableBlock(magicexpansionnonsensical, MagicExpansionItems.FISHING_ROD_FINAL_STRING, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_FINAL_STRING_21,MAGIC_EXPANSION_FINAL_STRING_21,MAGIC_EXPANSION_FINAL_STRING_21,
                MAGIC_EXPANSION_FINAL_STRING_21,new ItemStack(Material.STRING),MAGIC_EXPANSION_FINAL_STRING_21,
                MAGIC_EXPANSION_FINAL_STRING_21,MAGIC_EXPANSION_FINAL_STRING_21,MAGIC_EXPANSION_FINAL_STRING_21
        }).register(plugin);
        // 终焉之丝
        new UnplaceableBlock(magicexpansionnonsensical, MagicExpansionItems.FISHING_ROD_FINAL_HOOK, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.FISHING_ROD),"§bany",getGradientName("requires any bait~")),null,
                null,null,null
        }).register(plugin);

        // 终焉之钩
        new HiddenItem(magicexpansionnonsensical, MagicExpansionItems.FISHING_ROD_FISH_ANYTHING, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.FISHING_ROD),"§bCan be caught with Magic-series fishing rods.",getGradientName("Image shown for reference only.")),null,
                null,null,null
        }).register(plugin);




        // 自定义
        new FishingRod(magicexpansionfishing, MagicExpansionItems.FISHING_ROD_LOG, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,null,new ItemStack(Material.OAK_LOG),
                null,new ItemStack(Material.CHERRY_LOG),new ItemStack(Material.STRING),
                new ItemStack(Material.BIRCH_LOG),null, AMETHYST_SHARD
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 1);
            put(Enchantment.LURE, 5);
        }}, false,
                Map.of(
                        "default", List.of(
                                new WeightedItem(new ItemStack(Material.OAK_LOG), 3),
                                new WeightedItem(new ItemStack(Material.ACACIA_LOG), 3),
                                new WeightedItem(new ItemStack(Material.JUNGLE_LOG), 3),
                                new WeightedItem(new ItemStack(Material.BIRCH_LOG), 3),
                                new WeightedItem(new ItemStack(Material.CHERRY_LOG), 3),
                                new WeightedItem(new ItemStack(Material.SPRUCE_LOG), 3),
                                new WeightedItem(new ItemStack(Material.DARK_OAK_LOG), 3),
                                new WeightedItem(new ItemStack(Material.MANGROVE_LOG), 3),
                                new WeightedItem(new ItemStack(Material.CRIMSON_STEM), 3),
                                new WeightedItem(new ItemStack(Material.WARPED_STEM), 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        )
                ), List.of()).register(plugin);

        // 萌新钓竿
        new FishingRod(magicexpansionfishing, MagicExpansionItems.FISHING_ROD_NEW_PLAYER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,null,STICK,
                null,STICK,new ItemStack(Material.STRING),
                STICK,null,SlimefunItems.MAGIC_SUGAR
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 1);
            put(Enchantment.LURE, 2);
        }}, false,
                Map.of(
                        "magic_sugar", List.of(
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_1, 5),
                                new WeightedItem(SlimefunItems.MAGIC_LUMP_1, 70),
                                new WeightedItem(SlimefunItems.MAGIC_LUMP_2, 10),
                                new WeightedItem(SlimefunItems.MAGIC_LUMP_3, 2),
                                new WeightedItem(SlimefunItems.ENDER_LUMP_1, 70),
                                new WeightedItem(SlimefunItems.ENDER_LUMP_2, 10),
                                new WeightedItem(SlimefunItems.ENDER_LUMP_3, 2),
                                new WeightedItem(SlimefunItems.MAGICAL_GLASS, 8),
                                new WeightedItem(SlimefunItems.MAGICAL_BOOK_COVER, 8),
                                new WeightedItem(SlimefunItems.LAVA_CRYSTAL, 1),
                                new WeightedItem(SlimefunItems.COMMON_TALISMAN, 3),
                                new WeightedItem(SlimefunItems.NECROTIC_SKULL, 1),
                                new WeightedItem(SlimefunItems.ESSENCE_OF_AFTERLIFE, 1),
                                new WeightedItem(SlimefunItems.SYNTHETIC_SHULKER_SHELL, 1),
                                new WeightedItem(SlimefunItems.BLANK_RUNE, 22),
                                new WeightedItem(SlimefunItems.AIR_RUNE, 2),
                                new WeightedItem(SlimefunItems.EARTH_RUNE, 2),
                                new WeightedItem(SlimefunItems.FIRE_RUNE, 2),
                                new WeightedItem(SlimefunItems.WATER_RUNE, 2),
                                new WeightedItem(SlimefunItems.ENDER_RUNE, 2),
                                new WeightedItem(SlimefunItems.LIGHTNING_RUNE, 1),
                                new WeightedItem(SlimefunItems.RAINBOW_RUNE, 1),
                                new WeightedItem(SlimefunItems.SOULBOUND_RUNE, 1),
                                new WeightedItem(SlimefunItems.ENCHANTMENT_RUNE, 1),
                                new WeightedItem(SlimefunItems.VILLAGER_RUNE, 1),
                                new WeightedItem(SlimefunItems.STRANGE_NETHER_GOO, 1),
                                new WeightedItem(SlimefunItems.RAINBOW_LEATHER, 1),
                                new WeightedItem(RANDOM_FISH_COMMON, 1),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 1),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 1),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 1),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 1),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 1),
                                new WeightedItem(FISH_LEGENDARY_EEL_POWER, 1),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 18)
                        ),
                        "bread", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COD),"§bLost Raw Cod",getGradientName("Whose cod is this?")
                                ), 200),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.SALMON),"§bDizzy Raw Salmon",getGradientName("Have you heard of the Salmon Return Formation?")
                                ), 200),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.TROPICAL_FISH),"§bA 1.4 Tropical Fish",getGradientName("How did a tropical fish reach a midstream river?")
                                ), 50),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PUFFERFISH),"§bGreenish Pufferfish",getGradientName("You really should not eat this carelessly.")
                                ), 90),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.POTION),"§bMysterious Potion",getGradientName("Maybe it is safe to drink?")
                                ), 40),
                                new WeightedItem(new ItemStack(Material.HONEY_BOTTLE,15), 30),
                                new WeightedItem(RANDOM_FISH_COMMON, 400),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 100),
                                new WeightedItem(RANDOM_FISH_EPIC, 6),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 6),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 27),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 27),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 27)
                        ),
                        "fishLureBasic", List.of(
                                new WeightedItem(new CustomItemStack(Material.GLOW_BERRIES, "§aFirefly Grass Spike", getGradientName("It glows faintly at night and is said to lure curious fish.")
                                ), 80),
                                new WeightedItem(new CustomItemStack(Material.MOSS_CARPET, "§aWater Moss Tuft", getGradientName("Soft and damp, a favorite hiding place for small fish.")
                                ), 60),
                                new WeightedItem(new CustomItemStack(Material.SLIME_BALL, "§aCroaking Shell", getGradientName("A gentle squeeze makes it croak, fooling fish into hearing one of their own.")
                                ), 40),
                                new WeightedItem(new CustomItemStack(Material.POPPY, "§aDewdrop Lotus Petal", getGradientName("Its fresh morning scent is said to cleanse murky water.")
                                ), 80),
                                new WeightedItem(new CustomItemStack(Material.PRISMARINE_SHARD, "§aFish Scale Dust", ("§fIt glimmers in sunlight as a recognition signal among schools of fish.")
                                ), 50),
                                new WeightedItem(RANDOM_FISH_COMMON, 160),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 140),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 60),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 60),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 50),
                                new WeightedItem(RANDOM_FISH_EPIC, 5),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 5),

                                new WeightedItem(SlimefunItems.REINFORCED_ALLOY_INGOT, 15),
                                new WeightedItem(SlimefunItems.HARDENED_METAL_INGOT, 15),
                                new WeightedItem(SlimefunItems.DAMASCUS_STEEL_INGOT, 15),
                                new WeightedItem(SlimefunItems.STEEL_INGOT, 15),
                                new WeightedItem(SlimefunItems.BRONZE_INGOT, 15),
                                new WeightedItem(SlimefunItems.DURALUMIN_INGOT, 15),
                                new WeightedItem(SlimefunItems.BILLON_INGOT, 15),
                                new WeightedItem(SlimefunItems.BRASS_INGOT, 15),
                                new WeightedItem(SlimefunItems.ALUMINUM_BRASS_INGOT, 15),
                                new WeightedItem(SlimefunItems.ALUMINUM_BRONZE_INGOT, 15),
                                new WeightedItem(SlimefunItems.CORINTHIAN_BRONZE_INGOT, 15),
                                new WeightedItem(SlimefunItems.SOLDER_INGOT, 15),
                                new WeightedItem(SlimefunItems.SYNTHETIC_SAPPHIRE, 15),
                                new WeightedItem(SlimefunItems.SYNTHETIC_DIAMOND, 15),
                                new WeightedItem(SlimefunItems.RAW_CARBONADO, 15),
                                new WeightedItem(SlimefunItems.NICKEL_INGOT, 15),
                                new WeightedItem(SlimefunItems.COBALT_INGOT, 15),
                                new WeightedItem(SlimefunItems.CARBONADO, 15),
                                new WeightedItem(SlimefunItems.FERROSILICON, 15),
                                new WeightedItem(SlimefunItems.COMPRESSED_CARBON, 15),
                                new WeightedItem(SlimefunItems.CARBON, 15),
                                new WeightedItem(SlimefunItems.CARBON_CHUNK, 15),
                                new WeightedItem(SlimefunItems.GOLD_24K, 15),
                                new WeightedItem(SlimefunItems.GILDED_IRON, 15),
                                new WeightedItem(SlimefunItems.SYNTHETIC_EMERALD, 15),
                                new WeightedItem(SlimefunItems.URANIUM, 15),
                                new WeightedItem(SlimefunItems.OIL_BUCKET, 15),
                                new WeightedItem(SlimefunItems.FUEL_BUCKET, 15),
                                new WeightedItem(SlimefunItems.NETHER_ICE, 15),
                                new WeightedItem(SlimefunItems.BLISTERING_INGOT_3, 15),
                                new WeightedItem(SlimefunItems.ENRICHED_NETHER_ICE, 15),
                                new WeightedItem(SlimefunItems.NEPTUNIUM, 15),
                                new WeightedItem(SlimefunItems.PLUTONIUM, 15),
                                new WeightedItem(BASIC_ENCHANT_STONE, 35),
                                new WeightedItem(WIND_SPIRIT, 35),
                                new WeightedItem(SlimefunItems.BOOSTED_URANIUM, 15)
                        ),
                        "fishLureDust", List.of(
                                new WeightedItem(new CustomItemStack(Material.RED_SAND, "§6Ground Copper Sand", getGradientName("Its faint metallic sheen acts as a beacon for Copper Vein Fish.")
                                ), 30),
                                new WeightedItem(new CustomItemStack(Material.RED_DYE, "§6Rust Powder", getGradientName("Rust scraped from abandoned machinery; mineral fish sense it as one of their own.")
                                ), 30),
                                new WeightedItem(new CustomItemStack(Material.GLOW_INK_SAC, "§6Gold Dust Residue", getGradientName("Tailings left after panning, still carrying the magic of a rich ore deposit.")
                                ), 70),
                                new WeightedItem(new CustomItemStack(Material.QUARTZ, "§6Quartz Shards", getGradientName("Crystal fragments from Nether veins that stabilize the bait's energy field.")
                                ), 60),
                                new WeightedItem(new CustomItemStack(Material.COAL, "§6Carbon Crystal Granules", getGradientName("Ancient plant remains from deep underground that provide an energetic base.")
                                ), 30),
                                new WeightedItem(new CustomItemStack(Material.NETHER_STAR, "§dStellar Iron Dust", ("§fExtremely rare dust said to come from meteor cores and greatly improve attraction.")
                                ), 6),
                                new WeightedItem(RANDOM_FISH_COMMON, 90),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 50),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 110),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 10),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 8),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 1)
                        ),
                        "fishLureOre", List.of(
                                new WeightedItem(new CustomItemStack(Material.COPPER_INGOT, "§bNative Copper Vein Fragment", getGradientName("A naturally conductive ore network stripped directly from rock rather than smelted.")
                                ), 20),
                                new WeightedItem(new CustomItemStack(Material.IRON_INGOT, "§bHematite Core", ("§fA high-purity iron core whose intact crystal lattice emits a metallic heartbeat.")
                                ), 30),
                                new WeightedItem(new CustomItemStack(Material.GOLD_INGOT, "§bGold Vein Crystal", getGradientName("A web-like gold crystal formed under pressure, serving as a living beacon of rich ore.")
                                ), 70),
                                new WeightedItem(new CustomItemStack(Material.AMETHYST_SHARD, "§bDeep Rock Crystal Core", getGradientName("A resonant deep-earth crystal that amplifies the range of ore signals.")
                                ), 60),
                                new WeightedItem(new CustomItemStack(Material.COAL_BLOCK, "§bLava Carbon Heart", getGradientName("A wood core carbonized beside lava for centuries, stabilizing ore-core activity with geothermal energy.")
                                ), 30),
                                new WeightedItem(new CustomItemStack(Material.NETHER_STAR, "§5Star Core Fragment", getGradientName("A fragment said to come from a fallen star, able to imitate core-level ore signals.")
                                ), 6),
                                new WeightedItem(RANDOM_FISH_COMMON, 90),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 50),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 10),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 110),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 8),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 1)
                        ),
                        "fishLureAlloyIngot", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.BOWL),"§6Toilet Seat",getGradientName("A sacred ring from an ancient cleansing rite: open to the spirit realm, closed against filth."),getGradientName("Mortals do not know it once guarded a god's most private moments.")
                                ), 23),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.YELLOW_DYE),"§eBanana Peel",getGradientName("It has toppled three kings, two unicorns, and one adventurer who claimed never to fall."),getGradientName("Legend says it came from a golden tree smiling under moonlight, born for fate's stumbles.")
                                ), 23),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.IRON_SHOVEL),"§aPlunger",getGradientName("A copy of the Abyssal Soul-Sucker; every plunge seals whispers from the sewer below."),getGradientName("The truly strong use it not only on pipes, but on the spirit realm."),getGradientName("ber~ber~ber~")
                                ), 23),
                                new WeightedItem(new CustomItemStack(CustomHead.getHead("1421f1514da756c8c6c7c0b83a79265c26c9ece66b3bad8fbd94bd96d7040d7e"),"§bMoray Eel",getGradientName("A living electric whip from deep-sea rifts, carrying the remnant of an ancient thunder god."),getGradientName("Fishers call it \"Wrath of the Black Tide\"; touching it causes convulsions and sparks.")
                                ), 23),
                                new WeightedItem(new CustomItemStack(CustomHead.getHead("a1f71182915f5f862189a81f690acde4f671075db267eb6128fd1b4a84da8d7c"),"§cLengshang's Wheelchair",getGradientName("Legendary gear made for idle players; equip it and even sleep through the final boss."),getGradientName("— You are not overpowered; the wheelchair carried every difficulty for you.")
                                ), 23),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COCOA_BEANS),"§cPoop"
                                ), 23),
                                new WeightedItem(new CustomItemStack(CustomHead.MAGICSOLO.getItem(),getGradientName("magicsolo"),getGradientName("Every dream must end; this fleeting life is empty as a dream."),getGradientName("A fleeting dream of life, dreaming neither the past nor the present.")
                                ), 23),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.GOLDEN_SHOVEL),"§eGolden Spatula",getGradientName("Were you hoping for another unit?"),getGradientName("Or perhaps an emblem?")
                                ), 23),
                                new WeightedItem(RANDOM_FISH_COMMON, 38),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 31),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 21),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 21),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 18),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_ALLOY_INGOT, 5),
                                new WeightedItem(RANDOM_FISH_EPIC, 9),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        ),
                        "default", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.SUGAR_CANE,2),"§bRotten Sugar Cane",getGradientName("How did sugar cane end up in the river?")), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.STICK,2),"§bMojibake",getGradientName("What even is this thing?")), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.INK_SAC,2),"§bFresh Ink Sac",getGradientName("Who throws a perfectly good ink sac into a river?")), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.CAKE,2),"§bDay-Old Cake",getGradientName("Could not finish the cake?")), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.REDSTONE,8),"§b8-bit Redstone",getGradientName("Exactly eight in one handful?")), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.DISPENSER,2),"§bA Dispenser Slimefun Needs",getGradientName("Should you just place it on the ground?")), 8),
                                new WeightedItem(RANDOM_FISH_COMMON, 2),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 1),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COCOA_BEANS),getGradientName("TNT")), 2)


                        )
                ),Arrays.asList(SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.BREAD),
                FISH_LURE_BASIC,FISH_LURE_DUST,FISH_LURE_ORE,FISH_LURE_ALLOY_INGOT)).register(plugin);


        // 风语者之竿
        new FishingRod(magicexpansionfishing, MagicExpansionItems.FISHING_ROD_WIND_SPEAKER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,null,WATER_ELEMENT,
                null,FISHING_ROD_NEW_PLAYER,WIND_SPIRIT,
                FISHING_STICK_STAR_IRON,null,AMETHYST_SHARD
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 5);
            put(Enchantment.LURE, 5);
        }}, false,
                Map.of(
                        "magic_sugar", List.of(
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_1, 100),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_2, 100),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_3, 15),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_4, 15),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_5, 15),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_6, 12),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_7, 12),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_8, 12),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_9, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_10, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_11, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_12, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_13, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_14, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_15, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_16, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_17, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_18, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_19, 7),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_20, 3),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_21, 3),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_22, 3),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_23, 3),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_24, 3),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_25, 2),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_26, 2),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_27, 2),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_28, 2),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_29, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_30, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_31, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_32, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_33, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_34, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_35, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_36, 1),
                                new WeightedItem(MAGIC_EXPANSION_MAGIC_SUGAR_37, 1),
                                new WeightedItem(SlimefunItems.MAGIC_LUMP_1, 100),
                                new WeightedItem(SlimefunItems.MAGIC_LUMP_2, 100),
                                new WeightedItem(SlimefunItems.MAGIC_LUMP_3, 800),
                                new WeightedItem(SlimefunItems.ENDER_LUMP_1, 100),
                                new WeightedItem(SlimefunItems.ENDER_LUMP_2, 100),
                                new WeightedItem(SlimefunItems.ENDER_LUMP_3, 800),
                                new WeightedItem(SlimefunItems.MAGICAL_GLASS, 200),
                                new WeightedItem(SlimefunItems.MAGICAL_BOOK_COVER, 200),
                                new WeightedItem(SlimefunItems.LAVA_CRYSTAL, 100),
                                new WeightedItem(SlimefunItems.COMMON_TALISMAN, 300),
                                new WeightedItem(SlimefunItems.NECROTIC_SKULL, 100),
                                new WeightedItem(SlimefunItems.ESSENCE_OF_AFTERLIFE, 100),
                                new WeightedItem(SlimefunItems.SYNTHETIC_SHULKER_SHELL, 100),
                                new WeightedItem(SlimefunItems.BLANK_RUNE, 100),
                                new WeightedItem(SlimefunItems.AIR_RUNE, 100),
                                new WeightedItem(SlimefunItems.EARTH_RUNE, 100),
                                new WeightedItem(SlimefunItems.FIRE_RUNE, 100),
                                new WeightedItem(SlimefunItems.WATER_RUNE, 100),
                                new WeightedItem(SlimefunItems.ENDER_RUNE, 100),
                                new WeightedItem(SlimefunItems.LIGHTNING_RUNE, 200),
                                new WeightedItem(SlimefunItems.RAINBOW_RUNE, 200),
                                new WeightedItem(SlimefunItems.SOULBOUND_RUNE, 200),
                                new WeightedItem(SlimefunItems.ENCHANTMENT_RUNE, 200),
                                new WeightedItem(SlimefunItems.VILLAGER_RUNE, 200),
                                new WeightedItem(SlimefunItems.STRANGE_NETHER_GOO, 200),
                                new WeightedItem(SlimefunItems.RAINBOW_LEATHER, 200),
                                new WeightedItem(RANDOM_FISH_COMMON, 1500),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 1000),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 1300),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 1300),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 1200),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 300),
                                new WeightedItem(RANDOM_FISH_EPIC, 300),
                                new WeightedItem(FISH_LEGENDARY_EEL_POWER, 300),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1800)
                        ),
                        "bread", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COD,3),"§bLost Raw Cod",getGradientName("Whose cod is this?")
                                ), 20),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.SALMON,3),"§bDizzy Raw Salmon",getGradientName("Have you heard of the Salmon Return Formation?")
                                ), 20),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.TROPICAL_FISH,3),"§bA 1.4 Tropical Fish",getGradientName("How did a tropical fish reach a midstream river?")
                                ), 5),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PUFFERFISH,3),"§bGreenish Pufferfish",getGradientName("You really should not eat this carelessly.")
                                ), 9),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.POTION,3),"§bMysterious Potion",getGradientName("Maybe it is safe to drink?")
                                ), 4),
                                new WeightedItem(new ItemStack(Material.HONEY_BOTTLE,256), 30),
                                new WeightedItem(RANDOM_FISH_COMMON, 10),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 10),
                                new WeightedItem(RANDOM_FISH_EPIC, 5),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 5),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 18),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 18),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 16),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        ),
                        "fishLureBasic", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.GLOW_BERRIES,3), "§aFirefly Grass Spike", getGradientName("It glows faintly at night and is said to lure curious fish.")
                                ), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.MOSS_CARPET,3), "§aWater Moss Tuft", getGradientName("Soft and damp, a favorite hiding place for small fish.")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.SLIME_BALL,3), "§aCroaking Shell", getGradientName("A gentle squeeze makes it croak, fooling fish into hearing one of their own.")
                                ), 4),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.POPPY,3), "§aDewdrop Lotus Petal", getGradientName("Its fresh morning scent is said to cleanse murky water.")
                                ), 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,3), "§aFish Scale Dust", ("§fIt glimmers in sunlight as a recognition signal among schools of fish.")
                                ), 5),
                                new WeightedItem(RANDOM_FISH_COMMON, 20),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 18),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 15),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 15),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 15),
                                new WeightedItem(RANDOM_FISH_EPIC, 8),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 8),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1),

                                new WeightedItem(SlimefunItems.REINFORCED_ALLOY_INGOT, 9),
                                new WeightedItem(SlimefunItems.HARDENED_METAL_INGOT, 9),
                                new WeightedItem(SlimefunItems.DAMASCUS_STEEL_INGOT, 9),
                                new WeightedItem(SlimefunItems.STEEL_INGOT, 9),
                                new WeightedItem(SlimefunItems.BRONZE_INGOT, 9),
                                new WeightedItem(SlimefunItems.DURALUMIN_INGOT, 9),
                                new WeightedItem(SlimefunItems.BILLON_INGOT, 9),
                                new WeightedItem(SlimefunItems.BRASS_INGOT, 9),
                                new WeightedItem(SlimefunItems.ALUMINUM_BRASS_INGOT, 9),
                                new WeightedItem(SlimefunItems.ALUMINUM_BRONZE_INGOT, 9),
                                new WeightedItem(SlimefunItems.CORINTHIAN_BRONZE_INGOT, 9),
                                new WeightedItem(SlimefunItems.SOLDER_INGOT, 9),
                                new WeightedItem(SlimefunItems.SYNTHETIC_SAPPHIRE, 9),
                                new WeightedItem(SlimefunItems.SYNTHETIC_DIAMOND, 9),
                                new WeightedItem(SlimefunItems.RAW_CARBONADO, 9),
                                new WeightedItem(SlimefunItems.NICKEL_INGOT, 9),
                                new WeightedItem(SlimefunItems.COBALT_INGOT, 9),
                                new WeightedItem(SlimefunItems.CARBONADO, 9),
                                new WeightedItem(SlimefunItems.FERROSILICON, 9),
                                new WeightedItem(SlimefunItems.COMPRESSED_CARBON, 9),
                                new WeightedItem(SlimefunItems.CARBON, 9),
                                new WeightedItem(SlimefunItems.CARBON_CHUNK, 9),
                                new WeightedItem(SlimefunItems.GOLD_24K, 9),
                                new WeightedItem(SlimefunItems.GILDED_IRON, 9),
                                new WeightedItem(SlimefunItems.SYNTHETIC_EMERALD, 9),
                                new WeightedItem(SlimefunItems.URANIUM, 9),
                                new WeightedItem(SlimefunItems.OIL_BUCKET, 9),
                                new WeightedItem(SlimefunItems.FUEL_BUCKET, 9),
                                new WeightedItem(SlimefunItems.NETHER_ICE, 9),
                                new WeightedItem(SlimefunItems.BLISTERING_INGOT_3, 9),
                                new WeightedItem(SlimefunItems.ENRICHED_NETHER_ICE, 9),
                                new WeightedItem(SlimefunItems.NEPTUNIUM, 9),
                                new WeightedItem(SlimefunItems.PLUTONIUM, 9),
                                new WeightedItem(BASIC_ENCHANT_STONE, 25),
                                new WeightedItem(WIND_SPIRIT, 25),
                                new WeightedItem(SlimefunItems.BOOSTED_URANIUM, 9)
                        ),
                        "fishLureDust", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.RED_SAND,3), "§6Ground Copper Sand", getGradientName("Its faint metallic sheen acts as a beacon for Copper Vein Fish.")
                                ), 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.RED_DYE,3), "§6Rust Powder", getGradientName("Rust scraped from abandoned machinery; mineral fish sense it as one of their own.")
                                ), 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.GLOW_INK_SAC,3), "§6Gold Dust Residue", getGradientName("Tailings left after panning, still carrying the magic of a rich ore deposit.")
                                ), 7),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.QUARTZ,3), "§6Quartz Shards", getGradientName("Crystal fragments from Nether veins that stabilize the bait's energy field.")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COAL,3), "§6Carbon Crystal Granules", getGradientName("Ancient plant remains from deep underground that provide an energetic base.")
                                ), 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.NETHER_STAR,3), "§dStellar Iron Dust", ("§fExtremely rare dust said to come from meteor cores and greatly improve attraction.")
                                ), 5),
                                new WeightedItem(RANDOM_FISH_COMMON, 12),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 3),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 31),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 1),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 3),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        ),
                        "fishLureOre", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COPPER_INGOT,3), "§bNative Copper Vein Fragment", getGradientName("A naturally conductive ore network stripped directly from rock rather than smelted.")
                                ), 2),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.IRON_INGOT,3), "§bHematite Core", ("§fA high-purity iron core whose intact crystal lattice emits a metallic heartbeat.")
                                ), 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.GOLD_INGOT,3), "§bGold Vein Crystal", getGradientName("A web-like gold crystal formed under pressure, serving as a living beacon of rich ore.")
                                ), 7),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.AMETHYST_SHARD,3), "§bDeep Rock Crystal Core", getGradientName("A resonant deep-earth crystal that amplifies the range of ore signals.")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COAL_BLOCK,3), "§bLava Carbon Heart", getGradientName("A wood core carbonized beside lava for centuries, stabilizing ore-core activity with geothermal energy.")
                                ), 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.NETHER_STAR,3), "§5Star Core Fragment", getGradientName("A fragment said to come from a fallen star, able to imitate core-level ore signals.")
                                ), 5),
                                new WeightedItem(RANDOM_FISH_COMMON, 12),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 3),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 1),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 31),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 3),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 3),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        ),
                        "fishLureAlloyIngot", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.BOWL),"§6Toilet Seat",getGradientName("A sacred ring from an ancient cleansing rite: open to the spirit realm, closed against filth."),getGradientName("Mortals do not know it once guarded a god's most private moments.")
                                ), 15),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.YELLOW_DYE),"§eBanana Peel",getGradientName("It has toppled three kings, two unicorns, and one adventurer who claimed never to fall."),getGradientName("Legend says it came from a golden tree smiling under moonlight, born for fate's stumbles.")
                                ), 15),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.IRON_SHOVEL),"§aPlunger",getGradientName("A copy of the Abyssal Soul-Sucker; every plunge seals whispers from the sewer below."),getGradientName("The truly strong use it not only on pipes, but on the spirit realm."),getGradientName("ber~ber~ber~")
                                ), 15),
                                new WeightedItem(new CustomItemStack(CustomHead.getHead("1421f1514da756c8c6c7c0b83a79265c26c9ece66b3bad8fbd94bd96d7040d7e"),"§bMoray Eel",getGradientName("A living electric whip from deep-sea rifts, carrying the remnant of an ancient thunder god."),getGradientName("Fishers call it \"Wrath of the Black Tide\"; touching it causes convulsions and sparks.")
                                ), 15),
                                new WeightedItem(new CustomItemStack(CustomHead.getHead("a1f71182915f5f862189a81f690acde4f671075db267eb6128fd1b4a84da8d7c"),"§cLengshang's Wheelchair",getGradientName("Legendary gear made for idle players; equip it and even sleep through the final boss."),getGradientName("— You are not overpowered; the wheelchair carried every difficulty for you.")
                                ), 15),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.COCOA_BEANS),"§cPoop"
                                ), 15),
                                new WeightedItem(new CustomItemStack(CustomHead.MAGICSOLO.getItem(),getGradientName("magicsolo"),getGradientName("Every dream must end; this fleeting life is empty as a dream."),getGradientName("A fleeting dream of life, dreaming neither the past nor the present.")
                                ), 15),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.GOLDEN_SHOVEL),"§eGolden Spatula",getGradientName("Were you hoping for another unit?"),getGradientName("Or perhaps an emblem?")
                                ), 15),
                                new WeightedItem(RANDOM_FISH_COMMON, 21),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 25),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 15),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 15),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 13),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_ALLOY_INGOT, 16),
                                new WeightedItem(RANDOM_FISH_EPIC, 10),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 10),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        ),
                        "default", List.of(
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.SUGAR_CANE,8),"§bRotten Sugar Cane",getGradientName("How did sugar cane end up in the river?")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.STICK,8),"§bMojibake",getGradientName("What even is this thing?")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.INK_SAC,8),"§bFresh Ink Sac",getGradientName("Who throws a perfectly good ink sac into a river?")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.CAKE,8),"§bDay-Old Cake",getGradientName("Could not finish the cake?")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.REDSTONE,8),"§b8-bit Redstone",getGradientName("Exactly eight in one handful?")
                                ), 6),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.DISPENSER,8),"§bA Dispenser Slimefun Needs",getGradientName("Should you just place it on the ground?")
                                ), 6),
                                new WeightedItem(RANDOM_FISH_COMMON, 8),
                                new WeightedItem(RANDOM_FISH_UNCOMMON, 8),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 3),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 3),
                                new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 1),
                                new WeightedItem(RANDOM_FISH_EPIC, 3),
                                new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 3),
                                new WeightedItem(FISHING_ROD_FINAL_HOOK, 1),
                                new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                                ), 1)
                        )
                ),Arrays.asList(SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.BREAD),
                FISH_LURE_BASIC,FISH_LURE_DUST,FISH_LURE_ORE,FISH_LURE_ALLOY_INGOT)).register(plugin);

        Boolean FinalLureEnable = cfg.getBoolean("Fish.FishingRod.FISHING_ROD_FINAL_STICK.Enable.FinalLure");
        Map lootTable;
        List USABLE_LURES;
        if(FinalLureEnable){
            lootTable = Map.of(
                    "fishLureFinal", List.of(
                            new WeightedItem(FISHING_ROD_FISH_ANYTHING, 20),
                            new WeightedItem(RANDOM_FISH_COMMON, 1),
                            new WeightedItem(RANDOM_FISH_UNCOMMON, 1),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 1),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 1),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 1),
                            new WeightedItem(RANDOM_FISH_EPIC, 1),
                            new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 1),
                            new WeightedItem(RANDOM_FISH_EPIC_POOL_ALLOY_INGOT, 1),
                            new WeightedItem(RANDOM_FISH_LEGENDARY, 1),
                            new WeightedItem(FISH_LEGENDARY_EEL_POWER, 1),
                            new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,8),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                            ), 1)
                    ),
                    "default", List.of(
                            new WeightedItem(RANDOM_FISH_COMMON, 8),
                            new WeightedItem(RANDOM_FISH_UNCOMMON, 6),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 3),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 3),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 3),
                            new WeightedItem(RANDOM_FISH_EPIC, 3),
                            new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 3),
                            new WeightedItem(RANDOM_FISH_EPIC_POOL_ALLOY_INGOT, 3),
                            new WeightedItem(RANDOM_FISH_LEGENDARY, 3),
                            new WeightedItem(FISH_LEGENDARY_EEL_POWER, 1),
                            new WeightedItem(FISHING_ROD_FINAL_HOOK, 1),
                            new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,5),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                            ), 3)
                    )
            );
            USABLE_LURES =List.of(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
            ));
        } else {
            lootTable = Map.of(
                    "default", List.of(
                            new WeightedItem(RANDOM_FISH_COMMON, 8),
                            new WeightedItem(RANDOM_FISH_UNCOMMON, 6),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_DUST, 3),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_ORE, 3),
                            new WeightedItem(RANDOM_FISH_RARE_POOL_INDUSTRY, 3),
                            new WeightedItem(RANDOM_FISH_EPIC, 3),
                            new WeightedItem(RANDOM_FISH_EPIC_POOL_INDUSTRY, 3),
                            new WeightedItem(RANDOM_FISH_EPIC_POOL_ALLOY_INGOT, 3),
                            new WeightedItem(RANDOM_FISH_LEGENDARY, 3),
                            new WeightedItem(FISH_LEGENDARY_EEL_POWER, 1),
                            new WeightedItem(FISHING_ROD_FINAL_HOOK, 1),
                            new WeightedItem(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,5),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form.")
                            ), 3)
                    )
            );
            USABLE_LURES =List.of(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form."),(""),("§6§lCurrently Disabled")
            ));
        }


        // 终焉鱼杆
        new FishingRod(magicexpansionfishing, MagicExpansionItems.FISHING_ROD_FINAL_STICK, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,null,MAGIC_EXPANSION_MAGIC_SUGAR_CANE,
                null,MAGIC_EXPANSION_MAGIC_SUGAR_CANE,FISHING_ROD_FINAL_STRING,
                MAGIC_EXPANSION_MAGIC_SUGAR_CANE,null, FISHING_ROD_FINAL_HOOK
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 25);
            put(Enchantment.LURE, 5);
        }}, false,
                lootTable,USABLE_LURES).register(plugin);


        // Between Water and Clouds fishing rod
        new FishingRodWaterCloud(magicexpansionfishing, MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,null,STICK,
                null,STICK,STRING_1,
                STICK,null,MAGIC_EXPANSION_MAGIC_SUGAR_1
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 1);
            put(Enchantment.LURE, 2);
        }}, false,
                Map.of(
                        // Tempered Sunset
                        "CuiXia", List.of(
                                new WeightedItem(SlimefunItems.REINFORCED_ALLOY_INGOT, 222),
                                new WeightedItem(SlimefunItems.DAMASCUS_STEEL_INGOT, 222),
                                new WeightedItem(SlimefunItems.STEEL_INGOT, 222),
                                new WeightedItem(SlimefunItems.BRONZE_INGOT, 222),
                                new WeightedItem(SlimefunItems.DURALUMIN_INGOT, 222),
                                new WeightedItem(SlimefunItems.ALUMINUM_BRASS_INGOT, 222),
                                new WeightedItem(SlimefunItems.ALUMINUM_BRONZE_INGOT, 222),
                                new WeightedItem(SlimefunItems.SYNTHETIC_SAPPHIRE, 222),
                                new WeightedItem(SlimefunItems.SYNTHETIC_DIAMOND, 222),
                                new WeightedItem(SlimefunItems.BILLON_INGOT, 222),
                                new WeightedItem(SlimefunItems.BRASS_INGOT, 222),
                                new WeightedItem(SlimefunItems.CORINTHIAN_BRONZE_INGOT, 222),
                                new WeightedItem(SlimefunItems.RAW_CARBONADO, 222),
                                new WeightedItem(SlimefunItems.SOLDER_INGOT, 222),
                                new WeightedItem(SlimefunItems.CARBONADO, 222),
                                new WeightedItem(SlimefunItems.NICKEL_INGOT, 222),
                                new WeightedItem(SlimefunItems.COBALT_INGOT, 222),
                                new WeightedItem(SlimefunItems.FERROSILICON, 222),
                                new WeightedItem(SlimefunItems.CARBON, 222),
                                new WeightedItem(SlimefunItems.COMPRESSED_CARBON, 222),
                                new WeightedItem(SlimefunItems.CARBON_CHUNK, 222),
                                new WeightedItem(SlimefunItems.SILICON, 222),
                                new WeightedItem(SlimefunItems.GILDED_IRON, 222),
                                new WeightedItem(SlimefunItems.SYNTHETIC_EMERALD, 222),
                                new WeightedItem(SlimefunItems.URANIUM, 222),
                                new WeightedItem(SlimefunItems.REDSTONE_ALLOY, 222),
                                new WeightedItem(SlimefunItems.HARDENED_METAL_INGOT, 222),
                                new WeightedItem(new ItemStack(Material.COBBLESTONE,15), 667),
                                new WeightedItem(new ItemStack(Material.FLINT,15), 667),
                                new WeightedItem(new ItemStack(Material.GRAVEL,15), 667),
                                new WeightedItem(new ItemStack(Material.SAND,15), 667),
                                new WeightedItem(new ItemStack(Material.STRING,15), 667),
                                new WeightedItem(new ItemStack(Material.BONE,15), 667),
                                new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA, 10)
                        ),
                        // Motes
                        "WeiChen", List.of(
                                new WeightedItem(SlimefunItems.IRON_DUST, 370),
                                new WeightedItem(SlimefunItems.GOLD_DUST, 370),
                                new WeightedItem(SlimefunItems.COPPER_DUST, 370),
                                new WeightedItem(SlimefunItems.TIN_DUST, 370),
                                new WeightedItem(SlimefunItems.SILVER_DUST, 370),
                                new WeightedItem(SlimefunItems.LEAD_DUST, 370),
                                new WeightedItem(SlimefunItems.ALUMINUM_DUST, 370),
                                new WeightedItem(SlimefunItems.ZINC_DUST, 370),
                                new WeightedItem(SlimefunItems.MAGNESIUM_DUST, 370),
                                new WeightedItem(new ItemStack(Material.COBBLESTONE,15), 1110),
                                new WeightedItem(new ItemStack(Material.FLINT,15), 1110),
                                new WeightedItem(new ItemStack(Material.GRAVEL,15), 1110),
                                new WeightedItem(new ItemStack(Material.SAND,15), 1110),
                                new WeightedItem(new ItemStack(Material.STRING,15), 1110),
                                new WeightedItem(new ItemStack(Material.BONE,15), 1110),
                                new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN, 10)
                        ),
                        // Molten Flame
                        "RongHuo", List.of(
                                new WeightedItem(SlimefunItems.COPPER_INGOT, 381),
                                new WeightedItem(SlimefunItems.TIN_INGOT, 381),
                                new WeightedItem(SlimefunItems.SILVER_INGOT, 381),
                                new WeightedItem(SlimefunItems.LEAD_INGOT, 381),
                                new WeightedItem(SlimefunItems.ALUMINUM_INGOT, 381),
                                new WeightedItem(SlimefunItems.ZINC_INGOT, 381),
                                new WeightedItem(SlimefunItems.MAGNESIUM_INGOT, 381),
                                new WeightedItem(SlimefunItems.SULFATE, 381),
                                new WeightedItem(new ItemStack(Material.COBBLESTONE,15), 1154),
                                new WeightedItem(new ItemStack(Material.FLINT,15), 1154),
                                new WeightedItem(new ItemStack(Material.GRAVEL,15), 1154),
                                new WeightedItem(new ItemStack(Material.SAND,15), 1154),
                                new WeightedItem(new ItemStack(Material.STRING,15), 1154),
                                new WeightedItem(new ItemStack(Material.BONE,15), 1154),
                                new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO, 28)

                        ),
                        // Leaping Gold
                        "YueJin", List.of(
                                new WeightedItem(SlimefunItems.GOLD_24K, 345),
                                new WeightedItem(SlimefunItems.GOLD_22K, 345),
                                new WeightedItem(SlimefunItems.GOLD_20K, 345),
                                new WeightedItem(SlimefunItems.GOLD_18K, 345),
                                new WeightedItem(SlimefunItems.GOLD_16K, 345),
                                new WeightedItem(SlimefunItems.GOLD_14K, 345),
                                new WeightedItem(SlimefunItems.GOLD_12K, 345),
                                new WeightedItem(SlimefunItems.GOLD_10K, 345),
                                new WeightedItem(SlimefunItems.GOLD_8K, 345),
                                new WeightedItem(SlimefunItems.GOLD_6K, 345),
                                new WeightedItem(SlimefunItems.GOLD_4K, 345),
                                new WeightedItem(new ItemStack(Material.COBBLESTONE,15), 1030),
                                new WeightedItem(new ItemStack(Material.FLINT,15), 1030),
                                new WeightedItem(new ItemStack(Material.GRAVEL,15), 1030),
                                new WeightedItem(new ItemStack(Material.SAND,15), 1030),
                                new WeightedItem(new ItemStack(Material.STRING,15), 1030),
                                new WeightedItem(new ItemStack(Material.BONE,15), 1030),
                                new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN, 25)
                        ),
                        // Star Core
                        "XingHe", List.of(
                                new WeightedItem(SlimefunItems.MAGNESIUM_SALT, 345),
                                new WeightedItem(SlimefunItems.OIL_BUCKET, 345),
                                new WeightedItem(SlimefunItems.FUEL_BUCKET, 345),
                                new WeightedItem(SlimefunItems.NETHER_ICE, 345),
                                new WeightedItem(SlimefunItems.BLISTERING_INGOT, 345),
                                new WeightedItem(SlimefunItems.BLISTERING_INGOT_2, 345),
                                new WeightedItem(SlimefunItems.BLISTERING_INGOT_3, 345),
                                new WeightedItem(SlimefunItems.ENRICHED_NETHER_ICE, 345),
                                new WeightedItem(SlimefunItems.NEPTUNIUM, 345),
                                new WeightedItem(SlimefunItems.PLUTONIUM, 345),
                                new WeightedItem(SlimefunItems.BOOSTED_URANIUM, 345),
                                new WeightedItem(new ItemStack(Material.COBBLESTONE,15), 1030),
                                new WeightedItem(new ItemStack(Material.FLINT,15), 1030),
                                new WeightedItem(new ItemStack(Material.GRAVEL,15), 1030),
                                new WeightedItem(new ItemStack(Material.SAND,15), 1030),
                                new WeightedItem(new ItemStack(Material.STRING,15), 1030),
                                new WeightedItem(new ItemStack(Material.BONE,15), 1030),
                                new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE, 25)
                        )
                ),
                Arrays.asList(FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA,FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN,FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO,
                        FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN,FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE)).register(plugin);



        //星辰木
        new UnplaceableBlock(magicexpansionresource, FISHING_STICK_STAR_IRON, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, STICK, new CustomItemStack(Material.PRISMARINE_SHARD, "§aFish Scale Dust", ("§fIt glimmers in sunlight as a recognition signal among schools of fish.")),
                STICK, new CustomItemStack(Material.NETHER_STAR, "§dStellar Iron Dust", ("§fExtremely rare dust said to come from meteor cores and greatly improve attraction.")),STICK,
                new CustomItemStack(Material.IRON_INGOT, "§bHematite Core", ("§fA high-purity iron core whose intact crystal lattice emits a metallic heartbeat.")), STICK, null
        }).register(plugin);


        //魔法擦拭巾
        new EnchantmentEraser(magicexpansionspecialitem, ENCHANTMENT_ERASER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.PAPER), REDSTONE, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //增重鱼饲料
        new FishWeightEnhancer(magicexpansionspecialitem, FISH_WEIGHT_ENHANCER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                FISH_LURE_BASIC, MAGIC_EXPANSION_MAGIC_SUGAR_7, FISH_LURE_ORE,
                new ItemStack(Material.BROWN_MUSHROOM), new ItemStack(Material.BOWL), new ItemStack(Material.RED_MUSHROOM),
                FISH_LURE_DUST, new ItemStack(Material.SUGAR_CANE), FISH_LURE_ALLOY_INGOT
        }).register(plugin);

        //神秘知识点
        new ResearchUnlocker(magicexpansionspecialitem, RESEARCH_UNLOCKER_PAPER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                EARTH_ELEMENT, PURE_ELEMENT_INGOT, WOOD_ELEMENT,
                WATER_ELEMENT, SlimefunGuide.getItem(SlimefunGuideMode.SURVIVAL_MODE), FIRE_ELEMENT,
                SlimefunItems.FILLED_FLASK_OF_KNOWLEDGE, GOLD_ELEMENT, SlimefunItems.FILLED_FLASK_OF_KNOWLEDGE
        }).register(plugin);
        //全息文字清理
        new WordClear(magicexpansionspecialitem, WORD_CLEAR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                REDSTONE, REDSTONE, REDSTONE,
                REDSTONE, SlimefunItems.HOLOGRAM_PROJECTOR, REDSTONE,
                REDSTONE, REDSTONE, REDSTONE
        }).register(plugin);

        //工业空间核心
        new UnplaceableBlock(magicexpansionresource, PRESS_CORE_ALPHA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                INFINITY_FLINT_AND_STEEL, BASIC_ENCHANT_STONE, PURE_ELEMENT_INGOT,
                FIVE_ELEMENT, SPACE_INFINITY_MAGIC, FIVE_ELEMENT,
                PURE_ELEMENT_INGOT, FIVE_ELEMENT, INFINITY_FLINT_AND_STEEL
        }).register(plugin);

        //奇迹 · 市集
        new PortableShop(magicexpansionspecialitem, PORTABLE_SHOP, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_MAGIC_SUGAR_1, MAGIC_EXPANSION_MAGIC_SUGAR_1, MAGIC_EXPANSION_MAGIC_SUGAR_1,
                MAGIC_EXPANSION_MAGIC_SUGAR_1, new ItemStack(Material.BOOK), MAGIC_EXPANSION_MAGIC_SUGAR_1,
                MAGIC_EXPANSION_MAGIC_SUGAR_1, MAGIC_EXPANSION_MAGIC_SUGAR_1, MAGIC_EXPANSION_MAGIC_SUGAR_1
        }).register(plugin);


        //共创说明
        new UnplaceableBlock(magicexpansioncooperatecreate, COOPERATE_CREATION_INFO, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //钓鱼说明
        new UnplaceableBlock(magicexpansionfishing, FISHING_INFO, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //鱼类说明
        new UnplaceableBlock(magicexpansionfishing, FISH_CATEGORY_INFO, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);

        //鱼book
        new FishingBook(magicexpansionfishing, FISHING_BOOK, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null, new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Obtain via Command"), ColorGradient.getGradientName("/mxf guide"),getGradientName("Use this command to receive the Fishing Guide.")), null,
                null, null, null
        }).register(plugin);

        // 普通鱼
        new CommonFish(magicexpansionfishing, RANDOM_FISH_COMMON, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("A very common fish.")),null,
                null,null,null
        }).register(plugin);
        // 罕见鱼
        new CommonFish(magicexpansionfishing, RANDOM_FISH_UNCOMMON, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("A relatively uncommon fish.")),null,
                null,null,null
        }).register(plugin);
        // 稀有鱼
        new CommonFish(magicexpansionfishing, RANDOM_FISH_RARE, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("It appears in hidden locations for only a short time and may favor certain bait.")),null,
                null,null,null
        }).register(plugin);

        new CommonFishHidden(magicexpansionfishing, RANDOM_FISH_RARE_POOL_DUST, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("It appears in hidden locations for only a short time and may favor certain bait.")),null,
                null,null,null
        }).register(plugin);
        new CommonFishHidden(magicexpansionfishing, RANDOM_FISH_RARE_POOL_ORE, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("It appears in hidden locations for only a short time and may favor certain bait.")),null,
                null,null,null
        }).register(plugin);
        new CommonFishHidden(magicexpansionfishing, RANDOM_FISH_RARE_POOL_INDUSTRY, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("It appears in hidden locations for only a short time and may favor certain bait.")),null,
                null,null,null
        }).register(plugin);






        // 史诗鱼
        new CommonFish(magicexpansionfishing, RANDOM_FISH_EPIC, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("Few can prove it exists; encountering it without special bait is nearly impossible.")),null,
                null,null,null
        }).register(plugin);



        new CommonFishHidden(magicexpansionfishing, RANDOM_FISH_EPIC_POOL_INDUSTRY, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("It appears in hidden locations for only a short time and may favor certain bait.")),null,
                null,null,null
        }).register(plugin);
        new CommonFishHidden(magicexpansionfishing, RANDOM_FISH_EPIC_POOL_ALLOY_INGOT, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("It appears in hidden locations for only a short time and may favor certain bait.")),null,
                null,null,null
        }).register(plugin);



        // 传说鱼
        new CommonFish(magicexpansionfishing, RANDOM_FISH_LEGENDARY, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("Known only from ancient legends; legendary bait offers a small chance to encounter it.")),null,
                null,null,null
        }).register(plugin);
        // 传说鱼
        new PowerEel(magicexpansionspecialitem, FISH_LEGENDARY_EEL_POWER, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(doGlow(Material.FISHING_ROD)),getGradientName("Chance to Obtain While Fishing"), ColorGradient.getGradientName("Catch with a Magic 2.0 fishing rod."),getGradientName("Known only from ancient legends; legendary bait offers a small chance to encounter it.")),null,
                null,null,null
        }).register(plugin);
        // 基础饵料
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.SWEET_BERRIES),BONE_MEAL,new ItemStack(Material.CARROT),
                BONE_MEAL,new ItemStack(Material.APPLE),BONE_MEAL,
                new ItemStack(Material.POTATO),BONE_MEAL,new ItemStack(Material.MELON_SLICE)
        }).register(plugin);
        // 混合矿粉饵料
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_DUST, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.COPPER_DUST,SlimefunItems.IRON_DUST,SlimefunItems.GOLD_DUST,
                SlimefunItems.SILVER_DUST,FISH_LURE_BASIC,SlimefunItems.MAGNESIUM_DUST,
                SlimefunItems.ZINC_DUST,SlimefunItems.TIN_DUST,SlimefunItems.ALUMINUM_DUST
        },sfItemAmount(FISH_LURE_DUST,16)).register(plugin);
        // 混合矿物饵料
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_ORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.DIAMOND),new ItemStack(Material.GOLD_INGOT),new ItemStack(Material.IRON_INGOT),
                new ItemStack(Material.EMERALD),FISH_LURE_BASIC,new ItemStack(Material.QUARTZ),
                new ItemStack(Material.AMETHYST_SHARD),new ItemStack(Material.LAPIS_LAZULI),new ItemStack(Material.GLOWSTONE_DUST)
        },sfItemAmount(FISH_LURE_ORE,16)).register(plugin);
        // 混合合金锭
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_ALLOY_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                FISH_LURE_DUST,JIN_KE_LA,FISH_LURE_ORE,
                JIN_KE_LA,FISH_LURE_BASIC,JIN_KE_LA,
                FISH_LURE_ORE,JIN_KE_LA,FISH_LURE_DUST
        },sfItemAmount(FISH_LURE_ALLOY_INGOT,8)).register(plugin);


        // Between Water and Clouds special catches
        new HiddenItem(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);
        new HiddenItem(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);
        new HiddenItem(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);
        new HiddenItem(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);
        new HiddenItem(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);









        // Between Water and Clouds lure recipes

        //1
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.TORCH),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA,64)).register(plugin);

        new UnplaceableBlock(magicexpansioncooperatecreate, FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA_CLONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.TORCH),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA,64)).register(plugin);

        //2
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GUNPOWDER),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN,64)).register(plugin);

        new UnplaceableBlock(magicexpansioncooperatecreate, FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN_CLONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GUNPOWDER),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN,64)).register(plugin);


        //3
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.COAL),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO,64)).register(plugin);

        new UnplaceableBlock(magicexpansioncooperatecreate, FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO_CLONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.COAL),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO,64)).register(plugin);


        //4
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GOLD_INGOT),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN,64)).register(plugin);

        new UnplaceableBlock(magicexpansioncooperatecreate, FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN_CLONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GOLD_INGOT),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN,64)).register(plugin);


        //5
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GLISTERING_MELON_SLICE),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE,64)).register(plugin);

        new UnplaceableBlock(magicexpansioncooperatecreate, FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE_CLONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GLISTERING_MELON_SLICE),SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR,SlimefunItems.MAGIC_SUGAR
        },sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE,64)).register(plugin);













        //新增35种能源连接器(玻璃相关形态)
        new EnergyConnectorHidden(magicexpansionpower, ENERGY_CONNECTOR_GLASS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.GLASS),new ItemStack(Material.GLASS),new ItemStack(Material.GLASS),
                new ItemStack(Material.GLASS),sfItemAmount(SlimefunItems.ENERGY_CONNECTOR,1),new ItemStack(Material.GLASS),
                new ItemStack(Material.GLASS),new ItemStack(Material.GLASS),new ItemStack(Material.GLASS)
        },sfItemAmount(ENERGY_CONNECTOR_GLASS,8)).register(plugin);

        new EnergyConnector(magicexpansionpower, ENERGY_CONNECTOR_GLASS_INFO, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.RAINBOW_GLASS,SlimefunItems.RAINBOW_GLASS,SlimefunItems.RAINBOW_GLASS,
                SlimefunItems.RAINBOW_GLASS,sfItemAmount(SlimefunItems.ENERGY_CONNECTOR,1),SlimefunItems.RAINBOW_GLASS,
                SlimefunItems.RAINBOW_GLASS,SlimefunItems.RAINBOW_GLASS,SlimefunItems.RAINBOW_GLASS
        },sfItemAmount(ENERGY_CONNECTOR_GLASS,8)).register(plugin);
        /*
        new EnergyConnectorHidden(magicexpansionpower, ENERGY_CONNECTOR_TINTED_GLASS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.TINTED_GLASS),new ItemStack(Material.TINTED_GLASS),new ItemStack(Material.TINTED_GLASS),
                new ItemStack(Material.TINTED_GLASS),sfItemAmount(SlimefunItems.ENERGY_CONNECTOR,64),new ItemStack(Material.TINTED_GLASS),
                new ItemStack(Material.TINTED_GLASS),new ItemStack(Material.TINTED_GLASS),new ItemStack(Material.TINTED_GLASS)
        },sfItemAmount(ENERGY_CONNECTOR_TINTED_GLASS,64)).register(plugin);
        */
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_TINTED_GLASS);
        // 染色玻璃
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_WHITE_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_ORANGE_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_MAGENTA_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_LIGHT_BLUE_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_YELLOW_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_LIME_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_PINK_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_GRAY_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_LIGHT_GRAY_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_CYAN_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_PURPLE_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_BLUE_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_BROWN_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_GREEN_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_RED_STAINED_GLASS);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_BLACK_STAINED_GLASS);

        // 染色玻璃板
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_WHITE_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_ORANGE_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_MAGENTA_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_LIGHT_BLUE_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_YELLOW_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_LIME_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_PINK_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_GRAY_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_LIGHT_GRAY_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_CYAN_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_PURPLE_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_BLUE_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_BROWN_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_GREEN_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_RED_STAINED_GLASS_PANE);
        registerHiddenRecipe(plugin, magicexpansionpower, ENERGY_CONNECTOR_BLACK_STAINED_GLASS_PANE);











        // 双面胶
        new DoubleSidedTape(magicexpansionspecialitem, MagicExpansionItems.DOUBLE_SIDED_TAPE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,new ItemStack(Material.SLIME_BALL),null,
                null,new ItemStack(Material.PAPER),null,
                null,null,null
        }).register(plugin);
        // 薛定谔的相框
        new SchrodingerFrame(magicexpansionspecialitem, MagicExpansionItems.SCHRODINGER_FRAME_ONE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,new ItemStack(Material.ITEM_FRAME),null,
                null,new ItemStack(Material.PAPER),null,
                null,null,null
        },true, "schrodinger_frame").register(plugin);
        // 薛定谔的相框
        new SchrodingerFrame(magicexpansionspecialitem, MagicExpansionItems.SCHRODINGER_FRAME_INFINITE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SCHRODINGER_FRAME_ONE,SCHRODINGER_FRAME_ONE,SCHRODINGER_FRAME_ONE,
                SCHRODINGER_FRAME_ONE,MAGIC_EXPANSION_MAGIC_SUGAR_1,SCHRODINGER_FRAME_ONE,
                SCHRODINGER_FRAME_ONE,SCHRODINGER_FRAME_ONE,SCHRODINGER_FRAME_ONE
        },false, "schrodinger_frame_infinite").register(plugin);
        // 以太秘匣传输器
        new CargoFragmentDistributor(magicexpansionenergy, MagicExpansionItems.SEND_ITEMS_TO_PLAYER_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.HOPPER),CARGO_TERMINAL,new ItemStack(Material.HOPPER),
                new ItemStack(Material.HOPPER),FIVE_ELEMENT_TOUCH,new ItemStack(Material.HOPPER),
                new ItemStack(Material.HOPPER),new ItemStack(Material.HOPPER),new ItemStack(Material.HOPPER)
        }).register(plugin);
        // 以太秘匣传输器-SF
        new SfCargoFragmentDistributor(magicexpansionenergy, MagicExpansionItems.SEND_ITEMS_TO_PLAYER_MACHINE_SF, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.HOPPER),CARGO_TERMINAL,new ItemStack(Material.HOPPER),
                new ItemStack(Material.HOPPER),FIVE_ELEMENT_TOUCH,new ItemStack(Material.HOPPER),
                new ItemStack(Material.HOPPER),new ItemStack(Material.SLIME_BALL),new ItemStack(Material.HOPPER)
        }).register(plugin);
        //便携式以太秘匣传输器
        new PortableCargoTransporter(magicexpansionspecialitem, MagicExpansionItems.PORTABLE_CARGO_TRANSPORTER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.HOPPER),CARGO_TERMINAL,new ItemStack(Material.HOPPER),
                new ItemStack(Material.HOPPER),FIVE_ELEMENT_TOUCH,new ItemStack(Material.HOPPER),
                new ItemStack(Material.HOPPER),new ItemStack(Material.REDSTONE_BLOCK),new ItemStack(Material.HOPPER)
        }).register(plugin);

        // 脚本序列化工具
        new CustomSequenceTool(magicexpansionspecialitem, MagicExpansionItems.CUSTOM_SEQUENCE_TOOL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.PROGRAMMABLE_ANDROID_3,SlimefunItems.GPS_TRANSMITTER_4,SlimefunItems.PROGRAMMABLE_ANDROID_3,
                MAGIC_EXPANSION_MAGIC_SUGAR_3,SlimefunItems.PROGRAMMABLE_ANDROID_3,MAGIC_EXPANSION_MAGIC_SUGAR_3,
                SlimefunItems.PROGRAMMABLE_ANDROID_3,MAGIC_EXPANSION_MAGIC_SUGAR_3,SlimefunItems.PROGRAMMABLE_ANDROID_3
        }).register(plugin);

        // 脚本序列化工具
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.WORLD_CORE, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null,new CustomItemStack(new ItemStack(Material.ENCHANTING_TABLE), "§x§E§8§4§2§3§DUltimate Magic Altar","§aMultiblock Structure","§bUse /mxalter guide to receive the recipe book.") ,null,
                null, null,null
        }).register(plugin);





        // 烈火僵尸BOSS
        new FireZombie(magicexpansionboss, MagicExpansionItems.FIRE_ZOMBIE, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);

        // 僵尸类BOSS身体
        new SlimefunItem(magicexpansionboss, MagicExpansionItems.FIREZOMBIE_BODY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.ENDER_LUMP_3,SlimefunItems.LAVA_CRYSTAL,SlimefunItems.ENDER_LUMP_3,
                SlimefunItems.MAGIC_LUMP_3,SlimefunItems.NECROTIC_SKULL,SlimefunItems.MAGIC_LUMP_3,
                SlimefunItems.ENDER_LUMP_3,SlimefunItems.ESSENCE_OF_AFTERLIFE,SlimefunItems.ENDER_LUMP_3
        }).register(plugin);

        // 烈火僵尸头颅
        new SlimefunItem(magicexpansionboss, MagicExpansionItems.FIREZOMBIE_HEAD, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.MOSS_BLOCK),new ItemStack(Material.ROTTEN_FLESH),new ItemStack(Material.MOSS_BLOCK),
                new ItemStack(Material.ROTTEN_FLESH),new ItemStack(Material.MOSS_BLOCK),new ItemStack(Material.ROTTEN_FLESH),
                new ItemStack(Material.MOSS_BLOCK),new ItemStack(Material.ROTTEN_FLESH),new ItemStack(Material.MOSS_BLOCK)
        }).register(plugin);




        new FireZombieMB(magicexpansionboss, MagicExpansionItems.FIRE_ZOMBIE_MB).register(plugin);


        // 风灵BOSS
        new WindElf(magicexpansionboss, MagicExpansionItems.WIND_ELF_SPAWN, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);

        // 僵尸类BOSS身体
        new SlimefunItem(magicexpansionboss, MagicExpansionItems.WIND_ELF_BODY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.WATER_ELEMENT,new ItemStack(Material.STRING),MagicExpansionItems.WATER_ELEMENT,
                new ItemStack(Material.STRING),SlimefunItems.BLANK_RUNE,new ItemStack(Material.STRING),
                MagicExpansionItems.WATER_ELEMENT,new ItemStack(Material.STRING),MagicExpansionItems.WATER_ELEMENT
        }).register(plugin);

        // 烈火僵尸头颅
        new SlimefunItem(magicexpansionboss, MagicExpansionItems.WIND_ELF_HEAD, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.LIGHT_BLUE_DYE),new ItemStack(Material.STRING),new ItemStack(Material.LIGHT_BLUE_DYE),
                new ItemStack(Material.STRING),new ItemStack(Material.LIGHT_BLUE_DYE),new ItemStack(Material.STRING),
                new ItemStack(Material.LIGHT_BLUE_DYE),new ItemStack(Material.STRING),new ItemStack(Material.LIGHT_BLUE_DYE)
        }).register(plugin);

        new WindElfMB(magicexpansionboss, MagicExpansionItems.WIND_ELF_MB).register(plugin);

        //风灵技能
        new UnplaceableBlock(magicexpansionboss, MagicExpansionItems.WIND_ELF_SKILL, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);
        //风灵防御
        new UnplaceableBlock(magicexpansionboss, MagicExpansionItems.WIND_ELF_DEFENSE, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, null, null,
                null, null, null
        }).register(plugin);



        // 星辰铁   1级青金石
        new UnplaceableBlock(magicexpansionforge, MagicExpansionItems.BASIC_ENCHANT_STONE, MAGICEXPANSION_MOB_DROP, new ItemStack[] {
                null, null, null,
                null, newItem.themed(Material.ZOMBIE_SPAWN_EGG, get("Items.BASIC_ENCHANT_STONE_DROP.Name"), getList("Items.BASIC_ENCHANT_STONE_DROP.Lore")),null,
                null, null,null
        }).register(plugin);
        // 风灵之息
        new UnplaceableBlock(magicexpansionforge, MagicExpansionItems.WIND_SPIRIT, MAGICEXPANSION_MOB_DROP, new ItemStack[] {
                null, null, null,
                null, newItem.themed(Material.ALLAY_SPAWN_EGG, get("Items.WIND_SPIRIT_DROP.Name"), getList("Items.WIND_SPIRIT_DROP.Lore")),null,
                null, null,null
        }).register(plugin);


        new StarShardsSword(magicexpansionforge, MagicExpansionItems.WEAPON_STAR_SHARDS_SWORD, RecipeType.NULL, new ItemStack[] {
                null, null, null,
                null,new CustomItemStack(new ItemStack(Material.ENCHANTING_TABLE), "§x§E§8§4§2§3§DUltimate Magic Altar","§aMultiblock Structure","§bUse /mxalter guide to receive the recipe book.") ,null,
                null, null,null
        }).register(plugin);


        new UnplaceableBlock(magicexpansionfoodresource, MagicExpansionItems.MAGIC_CROP_INFO, RecipeType.NULL, new ItemStack[] {
                null, null,null,
                null, null,null,
                null, null,null
        }).register(plugin);

        new MagicCrop(magicexpansionfoodresource, MagicExpansionItems.WHEAT_SEEDS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                AMETHYST_SHARD, MAGIC_EXPANSION_MAGIC_SUGAR_1,AMETHYST_SHARD,
                MAGIC_EXPANSION_MAGIC_SUGAR_1, new ItemStack(Material.WHEAT_SEEDS),MAGIC_EXPANSION_MAGIC_SUGAR_1,
                AMETHYST_SHARD, MAGIC_EXPANSION_MAGIC_SUGAR_1,AMETHYST_SHARD
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 100),
                new MagicCrop.WeightedDrop(WHEAT_SEEDS, 30),
                new MagicCrop.WeightedDrop(MAGIC_EXPANSION_MAGIC_SUGAR_1, 3),
                new MagicCrop.WeightedDrop(HARVEST_WHEAT, 5)
        ),3,9).register(plugin);

        new MagicCrop(magicexpansionfoodresource, POTATO, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                AMETHYST_SHARD, MAGIC_EXPANSION_FINAL_STRING_1,AMETHYST_SHARD,
                MAGIC_EXPANSION_FINAL_STRING_1, new ItemStack(Material.POTATO),MAGIC_EXPANSION_FINAL_STRING_1,
                AMETHYST_SHARD, MAGIC_EXPANSION_FINAL_STRING_1,AMETHYST_SHARD
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.POTATO), 100),
                new MagicCrop.WeightedDrop(POTATO, 80),
                new MagicCrop.WeightedDrop(MAGIC_EXPANSION_FINAL_STRING_1, 1)
        ),12,26).register(plugin);

        new MagicCrop(magicexpansionfoodresource, HYBRID_RICE_SEEDS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.HAY_BLOCK), new ItemStack(Material.HAY_BLOCK),new ItemStack(Material.HAY_BLOCK),
                AMETHYST_SHARD, new ItemStack(Material.WHEAT_SEEDS),AMETHYST_SHARD,
                new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.DIRT),new ItemStack(Material.WATER_BUCKET)
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 80),
                new MagicCrop.WeightedDrop(HARVEST_RICE, 100),
                new MagicCrop.WeightedDrop(HYBRID_RICE_SEEDS, 20),
                new MagicCrop.WeightedDrop(DREAM_KERNEL, 3)
        ),19,30).register(plugin);

        //矿粉相关魔法植物

        new MagicCrop(magicexpansionfoodresource, WHEAT_COPPER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.COPPER_DUST, WOOD_ELEMENT,SlimefunItems.COPPER_DUST,
                AMETHYST_SHARD, new ItemStack(Material.WHEAT_SEEDS),AMETHYST_SHARD,
                SlimefunItems.COPPER_DUST, WOOD_ELEMENT,SlimefunItems.COPPER_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.COPPER_DUST, 83),
                new MagicCrop.WeightedDrop(WHEAT_COPPER, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, RADISH_IRON, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.IRON_DUST, WOOD_ELEMENT,SlimefunItems.IRON_DUST,
                AMETHYST_SHARD, new ItemStack(Material.CARROT),AMETHYST_SHARD,
                SlimefunItems.IRON_DUST, WOOD_ELEMENT,SlimefunItems.IRON_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.CARROT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.IRON_DUST, 83),
                new MagicCrop.WeightedDrop(RADISH_IRON, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, BEET_GOLD, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.GOLD_DUST, WOOD_ELEMENT,SlimefunItems.GOLD_DUST,
                AMETHYST_SHARD, new ItemStack(Material.BEETROOT_SEEDS),AMETHYST_SHARD,
                SlimefunItems.GOLD_DUST, WOOD_ELEMENT,SlimefunItems.GOLD_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.BEETROOT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.GOLD_DUST, 83),
                new MagicCrop.WeightedDrop(BEET_GOLD, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, RADISH_SILVER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.SILVER_DUST, WOOD_ELEMENT,SlimefunItems.SILVER_DUST,
                AMETHYST_SHARD, new ItemStack(Material.CARROT),AMETHYST_SHARD,
                SlimefunItems.SILVER_DUST, WOOD_ELEMENT,SlimefunItems.SILVER_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.CARROT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.SILVER_DUST, 83),
                new MagicCrop.WeightedDrop(RADISH_SILVER, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, WHEAT_ZINC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.ZINC_DUST, WOOD_ELEMENT,SlimefunItems.ZINC_DUST,
                AMETHYST_SHARD, new ItemStack(Material.WHEAT_SEEDS),AMETHYST_SHARD,
                SlimefunItems.ZINC_DUST, WOOD_ELEMENT,SlimefunItems.ZINC_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.ZINC_DUST, 83),
                new MagicCrop.WeightedDrop(WHEAT_ZINC, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, BEET_MAGNESIUM, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGNESIUM_DUST, WOOD_ELEMENT,SlimefunItems.MAGNESIUM_DUST,
                AMETHYST_SHARD, new ItemStack(Material.BEETROOT_SEEDS),AMETHYST_SHARD,
                SlimefunItems.MAGNESIUM_DUST, WOOD_ELEMENT,SlimefunItems.MAGNESIUM_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.BEETROOT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.MAGNESIUM_DUST, 83),
                new MagicCrop.WeightedDrop(BEET_MAGNESIUM, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, POTATO_TIN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.TIN_DUST, WOOD_ELEMENT,SlimefunItems.TIN_DUST,
                AMETHYST_SHARD, new ItemStack(Material.POTATO),AMETHYST_SHARD,
                SlimefunItems.TIN_DUST, WOOD_ELEMENT,SlimefunItems.TIN_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.POTATO), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.TIN_DUST, 83),
                new MagicCrop.WeightedDrop(POTATO_TIN, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, BEET_LEAD, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.LEAD_DUST, WOOD_ELEMENT,SlimefunItems.LEAD_DUST,
                AMETHYST_SHARD, new ItemStack(Material.BEETROOT_SEEDS),AMETHYST_SHARD,
                SlimefunItems.LEAD_DUST, WOOD_ELEMENT,SlimefunItems.LEAD_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.BEETROOT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.LEAD_DUST, 83),
                new MagicCrop.WeightedDrop(BEET_LEAD, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);

        new MagicCrop(magicexpansionfoodresource, WHEAT_ALUMINUM, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.ALUMINUM_DUST, WOOD_ELEMENT,SlimefunItems.ALUMINUM_DUST,
                AMETHYST_SHARD, new ItemStack(Material.WHEAT_SEEDS),AMETHYST_SHARD,
                SlimefunItems.ALUMINUM_DUST, WOOD_ELEMENT,SlimefunItems.ALUMINUM_DUST
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 21),
                new MagicCrop.WeightedDrop(SlimefunItems.ALUMINUM_DUST, 83),
                new MagicCrop.WeightedDrop(WHEAT_ALUMINUM, 16),
                new MagicCrop.WeightedDrop(ORE_DUST_CRYSTAL, 1)
        ),5,11).register(plugin);


        //矿锭相关魔法植物

        new MagicCrop(magicexpansionfoodresource, WHEAT_COPPER_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, WHEAT_COPPER,COAL,
                WHEAT_COPPER, ORE_DUST_CRYSTAL,WHEAT_COPPER,
                COAL, WHEAT_COPPER,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.COPPER_INGOT, 73),
                new MagicCrop.WeightedDrop(WHEAT_COPPER_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, RADISH_IRON_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, RADISH_IRON,COAL,
                RADISH_IRON, ORE_DUST_CRYSTAL,RADISH_IRON,
                COAL, RADISH_IRON,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(new ItemStack(Material.IRON_INGOT), 73),
                new MagicCrop.WeightedDrop(RADISH_IRON_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, BEET_GOLD_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, BEET_GOLD,COAL,
                BEET_GOLD, ORE_DUST_CRYSTAL,BEET_GOLD,
                COAL, BEET_GOLD,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(new ItemStack(Material.GOLD_INGOT), 73),
                new MagicCrop.WeightedDrop(BEET_GOLD_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, RADISH_SILVER_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, RADISH_SILVER,COAL,
                RADISH_SILVER, ORE_DUST_CRYSTAL,RADISH_SILVER,
                COAL, RADISH_SILVER,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.SILVER_INGOT, 73),
                new MagicCrop.WeightedDrop(RADISH_SILVER_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, WHEAT_ZINC_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, WHEAT_ZINC,COAL,
                WHEAT_ZINC, ORE_DUST_CRYSTAL,WHEAT_ZINC,
                COAL, WHEAT_ZINC,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.ZINC_INGOT, 73),
                new MagicCrop.WeightedDrop(WHEAT_ZINC_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, BEET_MAGNESIUM_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, BEET_MAGNESIUM,COAL,
                BEET_MAGNESIUM, ORE_DUST_CRYSTAL,BEET_MAGNESIUM,
                COAL, BEET_MAGNESIUM,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.MAGNESIUM_INGOT, 73),
                new MagicCrop.WeightedDrop(BEET_MAGNESIUM_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, POTATO_TIN_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, POTATO_TIN,COAL,
                POTATO_TIN, ORE_DUST_CRYSTAL,POTATO_TIN,
                COAL, POTATO_TIN,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.TIN_INGOT, 73),
                new MagicCrop.WeightedDrop(POTATO_TIN_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, BEET_LEAD_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, BEET_LEAD,COAL,
                BEET_LEAD, ORE_DUST_CRYSTAL,BEET_LEAD,
                COAL, BEET_LEAD,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.LEAD_INGOT, 73),
                new MagicCrop.WeightedDrop(BEET_LEAD_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);
        new MagicCrop(magicexpansionfoodresource, WHEAT_ALUMINUM_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                COAL, WHEAT_ALUMINUM,COAL,
                WHEAT_ALUMINUM, ORE_DUST_CRYSTAL,WHEAT_ALUMINUM,
                COAL, WHEAT_ALUMINUM,COAL
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 15),
                new MagicCrop.WeightedDrop(SlimefunItems.ALUMINUM_INGOT, 73),
                new MagicCrop.WeightedDrop(WHEAT_ALUMINUM_INGOT, 15),
                new MagicCrop.WeightedDrop(ORE_INGOT_CRYSTAL, 1)
        ),7,18).register(plugin);

        //合金锭相关魔法植物
        new MagicCrop(magicexpansionfoodresource, WHEAT_BRONZE_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                WHEAT_COPPER, POTATO_TIN,WHEAT_COPPER_INGOT,
                null, null,null,
                null, null,null
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 19),
                new MagicCrop.WeightedDrop(new ItemStack(Material.POTATO), 7),
                new MagicCrop.WeightedDrop(SlimefunItems.BRONZE_INGOT, 100),
                new MagicCrop.WeightedDrop(WHEAT_BRONZE_INGOT, 20),
                new MagicCrop.WeightedDrop(WHEAT_COPPER, 3),
                new MagicCrop.WeightedDrop(POTATO_TIN, 3),
                new MagicCrop.WeightedDrop(WHEAT_COPPER_INGOT, 3),
                new MagicCrop.WeightedDrop(ORE_MIX_CRYSTAL, 1)
        ),6,15).register(plugin);
        new MagicCrop(magicexpansionfoodresource, BEET_BRASS_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                WHEAT_COPPER, WHEAT_ZINC,WHEAT_COPPER_INGOT,
                null, null,null,
                null, null,null
        }, List.of(
                new MagicCrop.WeightedDrop(new ItemStack(Material.BEETROOT), 19),
                new MagicCrop.WeightedDrop(new ItemStack(Material.WHEAT), 7),
                new MagicCrop.WeightedDrop(SlimefunItems.BRASS_INGOT, 100),
                new MagicCrop.WeightedDrop(BEET_BRASS_INGOT, 20),
                new MagicCrop.WeightedDrop(WHEAT_COPPER, 3),
                new MagicCrop.WeightedDrop(WHEAT_ZINC, 3),
                new MagicCrop.WeightedDrop(WHEAT_COPPER_INGOT, 3),
                new MagicCrop.WeightedDrop(ORE_MIX_CRYSTAL, 1)
        ),6,15).register(plugin);








        // 材料 魔法材料   魔法铁锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.IRON_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.IRON_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.IRON_DUST, new ItemStack(Material.IRON_INGOT),SlimefunItems.IRON_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.IRON_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法金锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.GOLD_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.GOLD_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.GOLD_DUST, new ItemStack(Material.GOLD_INGOT),SlimefunItems.GOLD_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.GOLD_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法铜锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.COPPER_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.COPPER_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.COPPER_DUST, SlimefunItems.COPPER_INGOT,SlimefunItems.COPPER_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.COPPER_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法锡锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.TIN_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.TIN_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.TIN_DUST, SlimefunItems.TIN_INGOT,SlimefunItems.TIN_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.TIN_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法银锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.SILVER_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.SILVER_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.SILVER_DUST, SlimefunItems.SILVER_INGOT,SlimefunItems.SILVER_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.SILVER_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法铅锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.LEAD_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.LEAD_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.LEAD_DUST, SlimefunItems.LEAD_INGOT,SlimefunItems.LEAD_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.LEAD_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法铝锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.ALUMINUM_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.ALUMINUM_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.ALUMINUM_DUST, SlimefunItems.ALUMINUM_INGOT,SlimefunItems.ALUMINUM_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.ALUMINUM_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法锌锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.ZINC_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.ZINC_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.ZINC_DUST, SlimefunItems.ZINC_INGOT,SlimefunItems.ZINC_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.ZINC_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //魔法镁锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.MAGNESIUM_INGOT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.MAGNESIUM_DUST, SlimefunItems.MAGIC_LUMP_1,
                SlimefunItems.MAGNESIUM_DUST, SlimefunItems.MAGNESIUM_INGOT,SlimefunItems.MAGNESIUM_DUST,
                SlimefunItems.MAGIC_LUMP_1, SlimefunItems.MAGNESIUM_DUST,SlimefunItems.MAGIC_LUMP_1
        }).register(plugin);
        //元素锭
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.ELEMENT_INGOT, RecipeType.SMELTERY, new ItemStack[] {
                MagicExpansionItems.IRON_INGOT, MagicExpansionItems.GOLD_INGOT, MagicExpansionItems.COPPER_INGOT,
                MagicExpansionItems.TIN_INGOT, MagicExpansionItems.SILVER_INGOT, MagicExpansionItems.LEAD_INGOT,
                MagicExpansionItems.ALUMINUM_INGOT, MagicExpansionItems.ZINC_INGOT, MagicExpansionItems.MAGNESIUM_INGOT
        }).register(plugin);

        //魔法红石
        registerVanResource(plugin,MagicExpansionItems.REDSTONE, new ItemStack(Material.REDSTONE));
        registerVanResource(plugin,MagicExpansionItems.DIAMOND, new ItemStack(Material.DIAMOND));
        registerVanResource(plugin,MagicExpansionItems.LAPIS_LAZULI, new ItemStack(Material.LAPIS_LAZULI));
        registerVanResource(plugin,MagicExpansionItems.EMERALD, new ItemStack(Material.EMERALD));
        registerVanResource(plugin,MagicExpansionItems.COAL, new ItemStack(Material.COAL));
        registerVanResource(plugin,MagicExpansionItems.QUARTZ, new ItemStack(Material.QUARTZ));
        registerVanResource(plugin,MagicExpansionItems.AMETHYST_SHARD, new ItemStack(Material.AMETHYST_SHARD));
        registerVanResource(plugin,MagicExpansionItems.NETHERITE_INGOT, new ItemStack(Material.NETHERITE_INGOT));
        registerVanResource(plugin,MagicExpansionItems.BONE, new ItemStack(Material.BONE));
        registerVanResource(plugin,MagicExpansionItems.BONE_MEAL, new ItemStack(Material.BONE_MEAL));
        registerVanResource(plugin,MagicExpansionItems.STICK, new ItemStack(Material.STICK));
        registerVanResource(plugin,MagicExpansionItems.COBBLESTONE, new ItemStack(Material.COBBLESTONE));
        registerVanResource(plugin,MagicExpansionItems.DIRT, new ItemStack(Material.DIRT));
        registerVanResource(plugin,MagicExpansionItems.GLOWSTONE_DUST, new ItemStack(Material.GLOWSTONE_DUST));

        // 魔法橡木木板
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.OAK_PLANKS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_LUMP_1, new ItemStack(Material.OAK_PLANKS) , null,
                null, null ,null,
                null, null,null
        }).register(plugin);

        // 红石火把
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.REDSTONE_TORCH, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, MagicExpansionItems.REDSTONE, null,
                null, MagicExpansionItems.STICK ,null,
                null, null,null
        }).register(plugin);
        // 拉杆
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.LEVER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, MagicExpansionItems.STICK, null,
                null, MagicExpansionItems.COBBLESTONE ,null,
                null, null,null
        }).register(plugin);
        // 按钮
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.BUTTON, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, null , null,
                null, MagicExpansionItems.COBBLESTONE ,null,
                null, null,null
        }).register(plugin);

        // 翠木核心
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.LOG_MIX, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_LOG), new ItemStack(Material.SPRUCE_LOG) , new ItemStack(Material.BIRCH_LOG),
                new ItemStack(Material.JUNGLE_LOG), MagicExpansionItems.BONE ,new ItemStack(Material.ACACIA_LOG),
                new ItemStack(Material.DARK_OAK_LOG), new ItemStack(Material.MANGROVE_LOG),new ItemStack(Material.CHERRY_LOG)
        }).register(plugin);

        // 红石处理元件
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.REDSTONE_EXECUTE_ELEMENT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.REDSTONE_TORCH, MagicExpansionItems.LEVER , MagicExpansionItems.REDSTONE_TORCH,
                MagicExpansionItems.OAK_PLANKS, MagicExpansionItems.REDSTONE ,MagicExpansionItems.OAK_PLANKS,
                MagicExpansionItems.ELEMENT_INGOT, MagicExpansionItems.IRON_INGOT,MagicExpansionItems.ELEMENT_INGOT
        }).register(plugin);

        // 原版机器核心
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.CORE_ORIGIN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT, SlimefunItems.MAGICAL_GLASS , MagicExpansionItems.COBBLESTONE,
                SlimefunItems.ENDER_LUMP_3, MagicExpansionItems.REDSTONE_EXECUTE_ELEMENT ,SlimefunItems.ENDER_LUMP_3,
                MagicExpansionItems.COBBLESTONE, SlimefunItems.MAGICAL_GLASS,MagicExpansionItems.ELEMENT_INGOT
        }).register(plugin);

        //光之核心
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.LIGHT_CORE, RecipeType.SMELTERY, new ItemStack[] {
                MagicExpansionItems.IRON_INGOT, MagicExpansionItems.GOLD_INGOT, MagicExpansionItems.COPPER_INGOT,
                MagicExpansionItems.TIN_INGOT, new ItemStack(Material.GLOWSTONE_DUST), MagicExpansionItems.LEAD_INGOT,
                MagicExpansionItems.ALUMINUM_INGOT, MagicExpansionItems.ZINC_INGOT, MagicExpansionItems.MAGNESIUM_INGOT
        }).register(plugin);

        //光能α
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.LIGHT_ENERGY_ALPHA, LIGHT_TRANSFORM_BASIC, new ItemStack[] {
                new ItemStack(Material.LIGHT,32),MagicExpansionItems.MAGNESIUM_INGOT, null,
                null,null,null,
                null,null,null,
        }).register(plugin);

        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.JIN_KE_LA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.EGG,1),new ItemStack(Material.APPLE,1), new ItemStack(Material.MELON_SLICE,1),
                new ItemStack(Material.SWEET_BERRIES,1),MagicExpansionItems.BONE_MEAL,new ItemStack(Material.GLOW_BERRIES,1),
                new ItemStack(Material.CARROT,1),new ItemStack(Material.WHEAT,1),new ItemStack(Material.ROTTEN_FLESH,1),
        }).register(plugin);

        //石英核心
        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.QUARTZ_CORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT, MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.QUARTZ,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT,
        }).register(plugin);


        //紊乱的发电机核心
        new Capacitor(magicexpansionpower,1314520, MagicExpansionItems.POWER_CORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.ENERGIZED_CAPACITOR, MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.COPPER_INGOT,AdvancedCreateItem("MAGIC_COSMIC_DUST"),MagicExpansionItems.ZINC_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.ELEMENT_INGOT,
        }).register(plugin);

        //幸运电容
        new LuckCapacitor(magicexpansionpower,2147483647, MagicExpansionItems.PURE_INGOT_POWER_CORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.PURE_ELEMENT_INGOT, PURE_FIVE_ELEMENT, MagicExpansionItems.PURE_ELEMENT_INGOT,
                MagicExpansionItems.PURE_ELEMENT_INGOT,SlimefunItems.ENERGIZED_CAPACITOR,MagicExpansionItems.PURE_ELEMENT_INGOT,
                MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.PURE_ELEMENT_INGOT,
        },1,1314521,1).register(plugin);
        //厄运电容
        new LuckCapacitor(magicexpansionpower,2147483647, BAD_LUCK_CAPACITY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_LEAD, PURE_ELEMENT_EARTH, PURE_LEAD,
                PURE_LEAD,PURE_INGOT_POWER_CORE,PURE_LEAD,
                PURE_LEAD,PURE_LEAD,PURE_LEAD,
        },1,1314521,-1).register(plugin);
        //潘多拉电容
        new LuckCapacitor(magicexpansionpower,2147483647, PANDORA_CAPACITY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_INGOT, PURE_ELEMENT_INGOT, PURE_ELEMENT_INGOT,
                PURE_INGOT_POWER_CORE,PURE_FIVE_ELEMENT,BAD_LUCK_CAPACITY,
                PURE_ELEMENT_INGOT,PURE_ELEMENT_INGOT,PURE_ELEMENT_INGOT,
        },1,1314521,2).register(plugin);
        //基础魔法电容
        new LuckCapacitor(magicexpansionpower,2147483647, MAGIC_CAPACITY_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_MAGIC_SUGAR_9, PURE_FIVE_ELEMENT, MAGIC_EXPANSION_MAGIC_SUGAR_9,
                BAD_LUCK_CAPACITY,PANDORA_CAPACITY,PURE_INGOT_POWER_CORE,
                MAGIC_EXPANSION_MAGIC_SUGAR_9,PURE_FIVE_ELEMENT,MAGIC_EXPANSION_MAGIC_SUGAR_9,
        },1145141919,Integer.MAX_VALUE,1).register(plugin);
        //终极魔法电容
        new LuckCapacitor(magicexpansionpower,2147483647, MAGIC_CAPACITY_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_MAGIC_SUGAR_11, PURE_FIVE_ELEMENT, MAGIC_EXPANSION_MAGIC_SUGAR_11,
                POWER_CARD,SlimefunItems.SMALL_CAPACITOR,POWER_CARD,
                MAGIC_EXPANSION_MAGIC_SUGAR_11,PURE_FIVE_ELEMENT,MAGIC_EXPANSION_MAGIC_SUGAR_11,
        },Integer.MAX_VALUE,Integer.MAX_VALUE,1).register(plugin);


        //53格空白容器
        new CHEST_BLOCK(magicexpansionenergy, WHITE_SLOTS_CHEST_53, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.CHEST),REDSTONE,new ItemStack(Material.CHEST),
                null, null, null,
                null, null, null,
        }).register(plugin);
        //以太秘匣提取器
        new CargoFragmentExtract(magicexpansionspecialitem, CARGO_FRAGMENT_EXTRACT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                AMETHYST_SHARD,SEND_ITEMS_TO_PLAYER_MACHINE,AMETHYST_SHARD,
                null, null, null,
                null, null, null,
        }).register(plugin);


        //跨年烟花·元旦
        new NewYearsDayFireworkYuanDan(magicexpansioncommemorate, NEW_YEARS_DAY_FIREWORK_YUAN_DAN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.GUNPOWDER),MAGIC_EXPANSION_MAGIC_SUGAR_1,null,
                null, null, null,
                null, null, null,
        }).register(plugin);
        new BigFireworksYuanDan(magicexpansioncommemorate, NEW_YEARS_DAY_FIREWORK_YUAN_DAN_2, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.GUNPOWDER),new ItemStack(Material.SUGAR),null,
                null, null, null,
                null, null, null,
        }).register(plugin);










        //电卡
        new PowerCard(magicexpansionspecialitem,MagicExpansionItems.POWER_CARD, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.PURE_ELEMENT_INGOT, PURE_FIVE_ELEMENT, MagicExpansionItems.PURE_ELEMENT_INGOT,
                MagicExpansionItems.PURE_INGOT_POWER_CORE,new ItemStack(Material.PAPER),MagicExpansionItems.PURE_INGOT_POWER_CORE,
                MagicExpansionItems.PURE_ELEMENT_INGOT,PURE_FIVE_ELEMENT,MagicExpansionItems.PURE_ELEMENT_INGOT,
        }).register(plugin);


        //无尽空间魔法
        new UnplaceableBlock(magicexpansionprebuildingresource, MagicExpansionItems.SPACE_INFINITY_MAGIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.LIGHT_CORE, MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.LIGHT_CORE,BasicCreateItem("MAGIC_REDSTONE"),MagicExpansionItems.LIGHT_CORE,
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.LIGHT_CORE,MagicExpansionItems.AMETHYST_SHARD,
        }).register(plugin);

        registerBuildingsResource(plugin,MagicExpansionItems.COBBLESTONE_1, Material.COBBLESTONE);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COBBLESTONE_2, MagicExpansionItems.COBBLESTONE_1);
        registerBuildingsResource(plugin,MagicExpansionItems.STONE_1, Material.STONE);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.STONE_2, MagicExpansionItems.STONE_1);
        registerBuildingsResource(plugin,MagicExpansionItems.OAK_LOG_1, Material.OAK_LOG);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.OAK_LOG_2, MagicExpansionItems.OAK_LOG_1);
        registerBuildingsResource(plugin,MagicExpansionItems.STONE_BRICKS_1, Material.STONE_BRICKS);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.STONE_BRICKS_2, MagicExpansionItems.STONE_BRICKS_1);
        registerBuildingsResource(plugin,MagicExpansionItems.BRICKS_1, Material.BRICKS);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.BRICKS_2, MagicExpansionItems.BRICKS_1);
        registerBuildingsResource(plugin,MagicExpansionItems.REDSTONE_1, Material.REDSTONE);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.REDSTONE_2, MagicExpansionItems.REDSTONE_1);
        registerBuildingsResource(plugin,MagicExpansionItems.REDSTONE_TORCH_1, Material.REDSTONE_TORCH);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.REDSTONE_TORCH_2, MagicExpansionItems.REDSTONE_TORCH_1);
        registerBuildingsResource(plugin,MagicExpansionItems.REPEATER_1, Material.REPEATER);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.REPEATER_2, MagicExpansionItems.REPEATER_1);
        registerBuildingsResource(plugin,MagicExpansionItems.COMPARATOR_1, Material.COMPARATOR);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COMPARATOR_2, MagicExpansionItems.COMPARATOR_1);
        registerBuildingsResource(plugin,MagicExpansionItems.HOPPER_1, Material.HOPPER);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.HOPPER_2, MagicExpansionItems.HOPPER_1);
        registerBuildingsResource(plugin,MagicExpansionItems.STRING_1, Material.STRING);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.STRING_2, MagicExpansionItems.STRING_1);
        registerBuildingsResource(plugin,MagicExpansionItems.TRIPWIRE_HOOK_1, Material.TRIPWIRE_HOOK);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.TRIPWIRE_HOOK_2, MagicExpansionItems.TRIPWIRE_HOOK_1);
        registerBuildingsResource(plugin,MagicExpansionItems.FURNACE_1, Material.FURNACE);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.FURNACE_2, MagicExpansionItems.FURNACE_1);
        registerBuildingsResource(plugin,MagicExpansionItems.IRON_INGOT_1, Material.IRON_INGOT);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.IRON_INGOT_2, MagicExpansionItems.IRON_INGOT_1);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.IRON_INGOT_3, MagicExpansionItems.IRON_INGOT_2);
        registerBuildingsResource(plugin,MagicExpansionItems.GLASS_1, Material.GLASS);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.GLASS_2, MagicExpansionItems.GLASS_1);
        registerBuildingsResource(plugin,MagicExpansionItems.LIGHT_1, Material.LIGHT);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.LIGHT_2, MagicExpansionItems.LIGHT_1);
        registerBuildingsResource(plugin,MagicExpansionItems.QUARTZ_BLOCK_1, Material.QUARTZ_BLOCK);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.QUARTZ_BLOCK_2, MagicExpansionItems.QUARTZ_BLOCK_1);
        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                MagicExpansionItems.COLOR_WOOL_1,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        null, new ItemStack(Material.WHITE_DYE,64), null,
                        new ItemStack(Material.RED_DYE,64), new ItemStack(Material.WHITE_WOOL,64), new ItemStack(Material.YELLOW_DYE,64),
                        null, new ItemStack(Material.BLUE_DYE,64), null
                }
        ).register(plugin);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COLOR_WOOL_2, MagicExpansionItems.COLOR_WOOL_1);

        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                MagicExpansionItems.COLOR_TERRACOTTA_1,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        null, new ItemStack(Material.WHITE_DYE,64), null,
                        new ItemStack(Material.RED_DYE,64), new ItemStack(Material.WHITE_TERRACOTTA,64), new ItemStack(Material.YELLOW_DYE,64),
                        null, new ItemStack(Material.BLUE_DYE,64), null
                }
        ).register(plugin);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COLOR_TERRACOTTA_2, MagicExpansionItems.COLOR_TERRACOTTA_1);

        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                MagicExpansionItems.COLOR_CONCRETE_1,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        null, new ItemStack(Material.WHITE_DYE,64), null,
                        new ItemStack(Material.RED_DYE,64), new ItemStack(Material.WHITE_CONCRETE,64), new ItemStack(Material.YELLOW_DYE,64),
                        null, new ItemStack(Material.BLUE_DYE,64), null
                }
        ).register(plugin);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COLOR_CONCRETE_2, MagicExpansionItems.COLOR_CONCRETE_1);

        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_1,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        null, new ItemStack(Material.WHITE_DYE,64), null,
                        new ItemStack(Material.RED_DYE,64), new ItemStack(Material.WHITE_GLAZED_TERRACOTTA,64), new ItemStack(Material.YELLOW_DYE,64),
                        null, new ItemStack(Material.BLUE_DYE,64), null
                }
        ).register(plugin);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_2, MagicExpansionItems.COLOR_GLAZED_TERRACOTTA_1);
        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                MagicExpansionItems.COLOR_LEAVES_1,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        null, new ItemStack(Material.WHITE_DYE,64), null,
                        new ItemStack(Material.RED_DYE,64), new ItemStack(Material.CHERRY_LEAVES,64), new ItemStack(Material.YELLOW_DYE,64),
                        null, new ItemStack(Material.BLUE_DYE,64), null
                }
        ).register(plugin);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COLOR_LEAVES_2, MagicExpansionItems.COLOR_LEAVES_1);
        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                MagicExpansionItems.COLOR_LOG_1,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        null, new ItemStack(Material.WHITE_DYE,64), null,
                        new ItemStack(Material.RED_DYE,64), MagicExpansionItems.OAK_LOG_1, new ItemStack(Material.YELLOW_DYE,64),
                        null, new ItemStack(Material.BLUE_DYE,64), null
                }
        ).register(plugin);
        registerBuildingsResourceSf(plugin,MagicExpansionItems.COLOR_LOG_2, MagicExpansionItems.COLOR_LOG_1);










        //GEO资源

        new MagicGeoResourceDefault(magicexpansionresource, MagicExpansionItems.GOLD_ELEMENT, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the Five Element Miner"),null,
                null,null,null
        },"Metal Element",false,1314,520)
                .register(plugin);

        new MagicGeoResourceDefault(magicexpansionresource, MagicExpansionItems.WOOD_ELEMENT, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the Five Element Miner"),null,
                null,null,null
        },"Wood Element",false,1314,520)
                .register(plugin);

        new MagicGeoResourceDefault(magicexpansionresource, MagicExpansionItems.WATER_ELEMENT, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the Five Element Miner"),null,
                null,null,null
        },"Water Element",false,1314,520)
                .register(plugin);

        new MagicGeoResourceDefault(magicexpansionresource, MagicExpansionItems.FIRE_ELEMENT, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the Five Element Miner"),null,
                null,null,null
        },"Fire Element",false,1314,520)
                .register(plugin);

        new MagicGeoResourceDefault(magicexpansionresource, MagicExpansionItems.EARTH_ELEMENT, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the Five Element Miner"),null,
                null,null,null
        },"Earth Element",false,1314,520)
                .register(plugin);

        new MagicGeoResourceDefault(magicexpansionrscmagic, MagicExpansionItems.RSC_MAGIC_REDSTONE, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the RSC Magic Miner"),null,
                null,null,null
        },"Magic Redstone - RSC",false,1314,520)
                .register(plugin);
        new MagicGeoResourceDefault(magicexpansionrscmagic, MagicExpansionItems.RSC_MAGIC_COSMIC_DUST, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the RSC Magic Miner"),null,
                null,null,null
        },"Cosmic Dust - RSC",false,1314,520)
                .register(plugin);
        new MagicGeoResourceDefault(magicexpansionrscmagic, MagicExpansionItems.RSC_MAGIC_SOUL, FIVE_ELEMENT_MINER, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NETHER_STAR), "&c&lUnavailable","&7Requires the RSC Magic Miner"),null,
                null,null,null
        },"Soul - RSC",false,1314,520)
                .register(plugin);

        new UnplaceableBlock(magicexpansionresource, MagicExpansionItems.FIVE_ELEMENT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,MagicExpansionItems.WOOD_ELEMENT,null,
                MagicExpansionItems.WATER_ELEMENT,MagicExpansionItems.GOLD_ELEMENT,MagicExpansionItems.FIRE_ELEMENT,
                null,MagicExpansionItems.EARTH_ELEMENT,null
        }).register(plugin);



        //纯净元素
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_IRON,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.IRON_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_GOLD,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.GOLD_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_COPPER,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.COPPER_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_MAGNESIUM,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.MAGNESIUM_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_TIN,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.TIN_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_SILVER,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.SILVER_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_LEAD,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.LEAD_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ALUMINUM,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.ALUMINUM_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ZINC,
                INGOT_PURE_MACHINE,
                new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.ZINC_INGOT,64), sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,8), null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ELEMENT_INGOT,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        MagicExpansionItems.PURE_IRON,MagicExpansionItems.PURE_GOLD, MagicExpansionItems.PURE_COPPER,
                        MagicExpansionItems.PURE_MAGNESIUM, MagicExpansionItems.PURE_TIN, MagicExpansionItems.PURE_SILVER,
                        MagicExpansionItems.PURE_LEAD, MagicExpansionItems.PURE_ALUMINUM, MagicExpansionItems.PURE_ZINC,
                }
        ).register(plugin);

        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ELEMENT_GOLD,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        MagicExpansionItems.GOLD_ELEMENT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.PURE_ELEMENT_INGOT,
                        MagicExpansionItems.PURE_GOLD, MagicExpansionItems.PURE_IRON, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ELEMENT_WOOD,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        WOOD_ELEMENT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.PURE_ELEMENT_INGOT,
                        MagicExpansionItems.PURE_COPPER, MagicExpansionItems.PURE_MAGNESIUM, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ELEMENT_WATER,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        WATER_ELEMENT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.PURE_ELEMENT_INGOT,
                        MagicExpansionItems.PURE_LEAD, MagicExpansionItems.PURE_ALUMINUM, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ELEMENT_FIRE,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        FIRE_ELEMENT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.PURE_ELEMENT_INGOT,
                        MagicExpansionItems.PURE_TIN, MagicExpansionItems.PURE_ZINC, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_ELEMENT_EARTH,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        EARTH_ELEMENT,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.PURE_ELEMENT_INGOT,
                        MagicExpansionItems.PURE_COPPER, MagicExpansionItems.PURE_SILVER, null,
                        null, null, null
                }
        ).register(plugin);
        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.PURE_FIVE_ELEMENT,
                RecipeType.SMELTERY,
                new ItemStack[] {
                        MagicExpansionItems.PURE_ELEMENT_GOLD,MagicExpansionItems.PURE_ELEMENT_WOOD,MagicExpansionItems.PURE_ELEMENT_WATER,
                        MagicExpansionItems.PURE_ELEMENT_FIRE, MagicExpansionItems.PURE_ELEMENT_EARTH, null,
                        null, null, null
                }
        ).register(plugin);

        new UnplaceableBlock(
                magicexpansionresource,
                MagicExpansionItems.SPEED_ELEMENT_64,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.POWER_CORE,MagicExpansionItems.PURE_ELEMENT_INGOT,
                        SlimefunItems.TALISMAN_MAGICIAN, MagicExpansionItems.PURE_FIVE_ELEMENT, SlimefunItems.TALISMAN_MAGICIAN,
                        MagicExpansionItems.PURE_ELEMENT_INGOT, SlimefunItems.TALISMAN_WIZARD, MagicExpansionItems.PURE_ELEMENT_INGOT
                }
        ).register(plugin);



        //幻翼抑制器
        new EntityKillMachine(magicexpansionenergy, MagicExpansionItems.PHANTON_SUPPRESSION, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,new ItemStack(Material.PHANTOM_MEMBRANE), MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.LIGHT_ENERGY_ALPHA,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.OAK_PLANKS,MagicExpansionItems.INFINITY_FLINT_AND_STEEL,MagicExpansionItems.OAK_PLANKS
        },1314,260, EntityType.PHANTOM, "Phantom").register(plugin);

        //rsc资源强转换
        new ForceTwoToRsc(magicexpansionrscmagic, MagicExpansionItems.TWO_TO_MAGIC_GEO_MACHINE_FORCE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.REDSTONE), new ItemStack(Material.REDSTONE), new ItemStack(Material.SUGAR),
                new ItemStack(Material.REDSTONE), new ItemStack(Material.FURNACE), new ItemStack(Material.SUGAR),
                new ItemStack(Material.REDSTONE), new ItemStack(Material.SUGAR), new ItemStack(Material.SUGAR)
        },1314,26).register(plugin);








        //木头发生器
        new ResourceMachine(magicexpansionresourcegenerator, MagicExpansionItems.RESOURCE_MACHINE_WOOD_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.REDSTONE,
                MagicExpansionItems.ZINC_INGOT,MagicExpansionItems.LOG_MIX,MagicExpansionItems.ZINC_INGOT,
                MagicExpansionItems.REDSTONE,MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCraftSecond(4)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.OAK_LOG,2),new ItemStack(Material.SPRUCE_LOG,2),new ItemStack(Material.BIRCH_LOG,2),
                        new ItemStack(Material.JUNGLE_LOG,2),new ItemStack(Material.ACACIA_LOG,2),new ItemStack(Material.DARK_OAK_LOG,2),
                        new ItemStack(Material.MANGROVE_LOG,2),new ItemStack(Material.CHERRY_LOG,2),
                        new ItemStack(Material.OAK_LEAVES,2),new ItemStack(Material.SPRUCE_LEAVES,2),new ItemStack(Material.BIRCH_LEAVES,2),
                        new ItemStack(Material.JUNGLE_LEAVES,2),new ItemStack(Material.ACACIA_LEAVES,2),new ItemStack(Material.DARK_OAK_LEAVES,2),
                        new ItemStack(Material.MANGROVE_LEAVES,2),new ItemStack(Material.CHERRY_LEAVES,2)})
                .register(plugin);

        //木头发生器
        new ResourceMachine(magicexpansionresourcegenerator, MagicExpansionItems.RESOURCE_MACHINE_WOOD_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.AMETHYST_SHARD,SlimefunItems.AIR_RUNE,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.RESOURCE_MACHINE_WOOD_BASIC,MagicExpansionItems.PURE_ELEMENT_WOOD,MagicExpansionItems.RESOURCE_MACHINE_WOOD_BASIC,
                MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.PURE_ELEMENT_INGOT
        })
                .setCraftSecond(3)
                .setCapacity(131452)
                .setConsumption(2600)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.OAK_LOG,64),new ItemStack(Material.SPRUCE_LOG,64),new ItemStack(Material.BIRCH_LOG,64),
                        new ItemStack(Material.JUNGLE_LOG,64),new ItemStack(Material.ACACIA_LOG,64),new ItemStack(Material.DARK_OAK_LOG,64),
                        new ItemStack(Material.MANGROVE_LOG,64),new ItemStack(Material.CHERRY_LOG,64),
                        new ItemStack(Material.OAK_LEAVES,64),new ItemStack(Material.SPRUCE_LEAVES,64),new ItemStack(Material.BIRCH_LEAVES,64),
                        new ItemStack(Material.JUNGLE_LEAVES,64),new ItemStack(Material.ACACIA_LEAVES,64),new ItemStack(Material.DARK_OAK_LEAVES,64),
                        new ItemStack(Material.MANGROVE_LEAVES,64),new ItemStack(Material.CHERRY_LEAVES,64)})
                .register(plugin);

        //光源发生器
        new ResourceMachine(magicexpansionresourcegenerator, MagicExpansionItems.LIGHT_GEN_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.GOLD_INGOT,MagicExpansionItems.LIGHT_CORE,MagicExpansionItems.GOLD_INGOT,
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCraftSecond(16)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.LIGHT,1),new ItemStack(Material.LIGHT,2),new ItemStack(Material.LIGHT,3),
                        new ItemStack(Material.LIGHT,4),new ItemStack(Material.LIGHT,5),new ItemStack(Material.LIGHT,6),
                        new ItemStack(Material.LIGHT,7),
                })
                .register(plugin);

        //刷线机
        new ResourceMachine(magicexpansionresourcegenerator, MagicExpansionItems.STRING_GEN_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.OAK_TRAPDOOR),new ItemStack(Material.WATER_BUCKET),new ItemStack(Material.LEVER),
                new ItemStack(Material.TRIPWIRE_HOOK),new ItemStack(Material.STRING),new ItemStack(Material.TRIPWIRE_HOOK),
                new ItemStack(Material.CHEST),new ItemStack(Material.HOPPER),new ItemStack(Material.OAK_PLANKS)
        })
                .setCraftSecond(1)
                .setCapacity(1314)
                .setConsumption(3)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.STRING,2)
                })
                .register(plugin);
        //刷线机
        new ResourceMachine(magicexpansionresourcegenerator, MagicExpansionItems.STRING_GEN_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.AMETHYST_SHARD,SlimefunItems.WATER_RUNE,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.STRING_GEN_BASIC,MagicExpansionItems.WATER_ELEMENT,MagicExpansionItems.STRING_GEN_BASIC,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCraftSecond(1)
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.STRING,64),new ItemStack(Material.STRING,64)
                })
                .register(plugin);

        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_QUARTZ_BASIC, MagicExpansionItems.QUARTZ, Material.QUARTZ_BLOCK,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_AMETHYST_SHARD_BASIC, MagicExpansionItems.AMETHYST_SHARD, Material.AMETHYST_SHARD);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_REDSTONE_BASIC, MagicExpansionItems.REDSTONE, Material.REDSTONE);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_DIAMOND_BASIC, MagicExpansionItems.DIAMOND, Material.DIAMOND);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_LAPIS_LAZULI_BASIC, MagicExpansionItems.LAPIS_LAZULI, Material.LAPIS_LAZULI);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_EMERALD_BASIC, MagicExpansionItems.EMERALD, Material.EMERALD);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_COAL_BASIC, MagicExpansionItems.COAL, Material.COAL);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_NETHERITE_SCRAP_BASIC, MagicExpansionItems.NETHERITE_INGOT, Material.NETHERITE_SCRAP,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_IRON_DUST_BASIC, MagicExpansionItems.IRON_INGOT, SlimefunItems.IRON_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_GOLD_DUST_BASIC, MagicExpansionItems.GOLD_INGOT, SlimefunItems.GOLD_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_COPPER_DUST_BASIC, MagicExpansionItems.COPPER_INGOT, SlimefunItems.COPPER_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_TIN_DUST_BASIC, MagicExpansionItems.TIN_INGOT, SlimefunItems.TIN_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_SILVER_DUST_BASIC, MagicExpansionItems.SILVER_INGOT, SlimefunItems.SILVER_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_LEAD_DUST_BASIC, MagicExpansionItems.LEAD_INGOT, SlimefunItems.LEAD_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_ALUMINUM_DUST_BASIC, MagicExpansionItems.ALUMINUM_INGOT, SlimefunItems.ALUMINUM_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_ZINC_DUST_BASIC, MagicExpansionItems.ZINC_INGOT, SlimefunItems.ZINC_DUST);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_MAGNESIUM_DUST_BASIC, MagicExpansionItems.MAGNESIUM_INGOT, SlimefunItems.MAGNESIUM_DUST);


        //矿锭x9
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_IRON_INGOT_BASIC, MagicExpansionItems.MINE_MAN_IRON_DUST_BASIC, Material.IRON_INGOT,1,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_GOLD_INGOT_BASIC, MagicExpansionItems.MINE_MAN_GOLD_DUST_BASIC, Material.GOLD_INGOT,1,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_COPPER_INGOT_BASIC, MagicExpansionItems.MINE_MAN_COPPER_DUST_BASIC, SlimefunItems.COPPER_INGOT,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_TIN_INGOT_BASIC, MagicExpansionItems.MINE_MAN_TIN_DUST_BASIC, SlimefunItems.TIN_INGOT,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_SILVER_INGOT_BASIC, MagicExpansionItems.MINE_MAN_SILVER_DUST_BASIC, SlimefunItems.SILVER_INGOT,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_LEAD_INGOT_BASIC, MagicExpansionItems.MINE_MAN_LEAD_DUST_BASIC, SlimefunItems.LEAD_INGOT,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_ALUMINUM_INGOT_BASIC, MagicExpansionItems.MINE_MAN_ALUMINUM_DUST_BASIC, SlimefunItems.ALUMINUM_INGOT,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_ZINC_INGOT_BASIC, MagicExpansionItems.MINE_MAN_ZINC_DUST_BASIC, SlimefunItems.ZINC_INGOT,1,1);
        registerBasicElectricMan(plugin, MagicExpansionItems.MINE_MAN_MAGNESIUM_INGOT_BASIC, MagicExpansionItems.MINE_MAN_MAGNESIUM_DUST_BASIC, SlimefunItems.MAGNESIUM_INGOT,1,1);

        //下界合金锭
        new ResourceMachine(magicexpansionelectricbot, MagicExpansionItems.MINE_MAN_NETHERITE_INGOT_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.MINE_MAN_GOLD_INGOT_BASIC,new ItemStack(Material.CRAFTING_TABLE),MagicExpansionItems.MINE_MAN_NETHERITE_SCRAP_BASIC,
                null,null,null,
                null,null,null
        })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.NETHERITE_INGOT,2),new ItemStack(Material.NETHERRACK,12)
                        ,new ItemStack(Material.FLINT,2),new ItemStack(Material.BONE_BLOCK),new ItemStack(Material.CLAY,2)})
                .register(plugin);

        //矿脉机器人
        new ResourceRandomOneMachine(magicexpansionelectricbot, MagicExpansionItems.MINE_MAN_MINERAL_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.PROGRAMMABLE_ANDROID_3,new ItemStack(Material.NETHERITE_PICKAXE),MagicExpansionItems.SINGLE_CUBE_ORE,
                null,null,null,
                null,null,null
        })
                .setCraftSecond(6)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.COAL_ORE,1),new ItemStack(Material.DEEPSLATE_COAL_ORE,1),
                        new ItemStack(Material.IRON_ORE,1),new ItemStack(Material.DEEPSLATE_IRON_ORE,1),new ItemStack(Material.COPPER_ORE,1),
                        new ItemStack(Material.GOLD_ORE,1),new ItemStack(Material.DEEPSLATE_GOLD_ORE,1),
                        new ItemStack(Material.REDSTONE_ORE,1),new ItemStack(Material.DEEPSLATE_REDSTONE_ORE,1),new ItemStack(Material.EMERALD_ORE,1),
                        new ItemStack(Material.DEEPSLATE_EMERALD_ORE,1),new ItemStack(Material.LAPIS_ORE,1),new ItemStack(Material.DEEPSLATE_LAPIS_ORE,1),
                        new ItemStack(Material.DIAMOND_ORE,1),new ItemStack(Material.DEEPSLATE_DIAMOND_ORE,1),new ItemStack(Material.NETHER_GOLD_ORE,1),
                        new ItemStack(Material.NETHER_QUARTZ_ORE,1),new ItemStack(Material.ANCIENT_DEBRIS,1),})
                .register(plugin);
        //矿脉机器人
        new ResourceRandomOneMachine(magicexpansionelectricbot, MagicExpansionItems.MINE_MAN_MINERAL_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.PURE_ELEMENT_GOLD,MagicExpansionItems.PURE_ELEMENT_GOLD,MagicExpansionItems.PURE_ELEMENT_GOLD,
                MagicExpansionItems.MINE_MAN_MINERAL_BASIC,MagicExpansionItems.PURE_FIVE_ELEMENT,MagicExpansionItems.MINE_MAN_MINERAL_BASIC,
                MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.PURE_ELEMENT_INGOT,MagicExpansionItems.PURE_ELEMENT_INGOT
        })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(Material.COAL_ORE,64),new ItemStack(Material.DEEPSLATE_COAL_ORE,64),
                        new ItemStack(Material.IRON_ORE,64),new ItemStack(Material.DEEPSLATE_IRON_ORE,64),new ItemStack(Material.COPPER_ORE,64),
                        new ItemStack(Material.GOLD_ORE,64),new ItemStack(Material.DEEPSLATE_GOLD_ORE,64),
                        new ItemStack(Material.REDSTONE_ORE,64),new ItemStack(Material.DEEPSLATE_REDSTONE_ORE,64),new ItemStack(Material.EMERALD_ORE,64),
                        new ItemStack(Material.DEEPSLATE_EMERALD_ORE,64),new ItemStack(Material.LAPIS_ORE,64),new ItemStack(Material.DEEPSLATE_LAPIS_ORE,64),
                        new ItemStack(Material.DIAMOND_ORE,64),new ItemStack(Material.DEEPSLATE_DIAMOND_ORE,64),new ItemStack(Material.NETHER_GOLD_ORE,64),
                        new ItemStack(Material.NETHER_QUARTZ_ORE,64),new ItemStack(Material.ANCIENT_DEBRIS,64),})
                .register(plugin);

        //矿脉机器人
        new RightClickMan(magicexpansionenergy, MagicExpansionItems.RIGHT_CLICK_MAN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.REDSTONE_TORCH,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.REDSTONE_TORCH,
                MagicExpansionItems.LEVER,SlimefunItems.PROGRAMMABLE_ANDROID,MagicExpansionItems.LEVER,
                MagicExpansionItems.OAK_PLANKS,MagicExpansionItems.OAK_PLANKS,MagicExpansionItems.OAK_PLANKS
        }).register(plugin);


        //五行聚灵阵-基础
        new ResourceRandomOneMachine(magicexpansionresourcegenerator, FIVE_ELEMENT_GEN_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_INGOT,MagicExpansionItems.INGOT_PURE_MACHINE,PURE_ELEMENT_INGOT,
                PURE_FIVE_ELEMENT,PURE_INGOT_POWER_CORE,MAGIC_EXPANSION_MAGIC_SUGAR_7,
                PURE_ELEMENT_INGOT,MagicExpansionItems.FIVE_ELEMENT_MINER,PURE_ELEMENT_INGOT
        })
                .setCraftSecond(3)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {sfItemAmount(GOLD_ELEMENT,1),sfItemAmount(WOOD_ELEMENT,1),
                        sfItemAmount(WATER_ELEMENT,1),sfItemAmount(FIRE_ELEMENT,1),
                        sfItemAmount(EARTH_ELEMENT,1),})
                .register(plugin);
        //印书厂
        new ResourceRandomOneMachine(magicexpansionresourcegenerator, SLIME_BOOK_GEN_ADVANCE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_INGOT,new ItemStack(Material.BOOK),PURE_ELEMENT_INGOT,
                PURE_ELEMENT_WOOD,PURE_INGOT_POWER_CORE,PURE_ELEMENT_WOOD,
                PURE_ELEMENT_INGOT,new ItemStack(Material.SLIME_BALL),PURE_ELEMENT_INGOT
        })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new CustomItemStack(SlimefunGuide.getItem(SlimefunGuideMode.SURVIVAL_MODE),64)})
                .register(plugin);

        new OriginMaterialGen(magicexpansionresourcegenerator, ORIGIN_MATERIAL_GEN, ORIGIN_MATERIAL_GEN_MAKER_ALPHA, new ItemStack[] {
                sfItemAmount(PURE_ELEMENT_GOLD,31),sfItemAmount(MAGIC_CAPACITY_ULTRA,1),sfItemAmount(PURE_ELEMENT_WOOD,31),
                sfItemAmount(PURE_ELEMENT_WATER,31),new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Any Vanilla Item"),  ColorGradient.getGradientNameVer2("The crafting material determines the Evolution Table's final output."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design.")),sfItemAmount(PURE_ELEMENT_FIRE,31),
                sfItemAmount(PURE_FIVE_ELEMENT,9),sfItemAmount(PURE_ELEMENT_EARTH,31),sfItemAmount(MAGIC_EXPANSION_MAGIC_SUGAR_19,9)
        })
                .setCraftSecond(1)
                .setCapacity(131452)
                .setConsumption(26000)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Any Vanilla Item × 99 × 36"),  ColorGradient.getGradientNameVer2("Your eyes are not deceiving you; it really is 99."),  ColorGradient.getGradientNameVer2("Output depends on the material used to craft the Evolution Table."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design."))})
                .register(plugin);

        new OriginMaterialGenUltra(magicexpansionresourcegenerator, ORIGIN_MATERIAL_GEN_ULTRA, ORIGIN_MATERIAL_GEN_MAKER_BETA, new ItemStack[] {
                sfItemAmount(ITEM_ORIGIN_BACK_TRACK,1),sfItemAmount(MAGIC_CAPACITY_ULTRA,1),sfItemAmount(MAGIC_EXPANSION_MAGIC_SUGAR_31,31),
                sfItemAmount(PURE_ELEMENT_WATER,1),new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Any Item"),  ColorGradient.getGradientNameVer2("The crafting material determines the Evolution Table's final output."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design.")),sfItemAmount(PURE_ELEMENT_FIRE,1),
                sfItemAmount(PURE_FIVE_ELEMENT,31),sfItemAmount(PURE_ELEMENT_EARTH,31),sfItemAmount(MAGIC_EXPANSION_MAGIC_SUGAR_19,31)
        })
                .setCraftSecond(1)
                .setCapacity(131452)
                .setConsumption(26000)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Any Item × 64 × 36"),  ColorGradient.getGradientNameVer2("Your eyes are not deceiving you; it really is 64."),  ColorGradient.getGradientNameVer2("Output depends on the material used to craft the Evolution Table."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design."))})
                .register(plugin);





        new OriginMaterialGenLite(magicexpansionresourcegenerator, ORIGIN_MATERIAL_GEN_LITE, ORIGIN_MATERIAL_GEN_MAKER_LITE, new ItemStack[] {
                sfItemAmount(GOLD_ELEMENT,11),sfItemAmount(BAD_LUCK_CAPACITY,1),sfItemAmount(WOOD_ELEMENT,45),
                sfItemAmount(WATER_ELEMENT,14),new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Any Vanilla Item"),  ColorGradient.getGradientNameVer2("The crafting material determines the Evolution Table's final output."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design.")),sfItemAmount(FIRE_ELEMENT,19),
                sfItemAmount(FIVE_ELEMENT,8),sfItemAmount(EARTH_ELEMENT,19),sfItemAmount(PURE_ELEMENT_INGOT,10)
        })
                .setCraftSecond(0)
                .setCapacity(131452)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Any Vanilla Item × 1"),  ColorGradient.getGradientNameVer2("A rather crude Evolution Table."),  ColorGradient.getGradientNameVer2("Output depends on the material used to craft the Evolution Table."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design."))})
                .register(plugin);

        new OriginMaterialGenStack(magicexpansionenergy, ORIGIN_MATERIAL_GEN_LITE_PRESS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                PURE_ELEMENT_INGOT, PANDORA_CAPACITY,PURE_ELEMENT_INGOT,
                BAD_LUCK_CAPACITY,PRESS_CORE_ALPHA,PURE_INGOT_POWER_CORE,
                PURE_ELEMENT_INGOT,CORE_ORIGIN,PURE_ELEMENT_INGOT
        })
                .setCraftSecond(0)
                .setCapacity(131452)
                .setConsumption(26000)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new CustomItemStack(Material.PAPER, ColorGradient.getGradientNameVer2("Can operate multiple Origin Evolution Tables in parallel."),  ColorGradient.getGradientNameVer2("A special Evolution Table that does not produce items by itself."),  ColorGradient.getGradientNameVer2("Output depends on the material used to craft the Evolution Table."), "" , ColorGradient.getGradientNameVer2("Perhaps this was always part of the design."))})
                .register(plugin);




        //木头转换机
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.WOOD_TRANSFORM_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.DIRT,MagicExpansionItems.LOG_MIX,MagicExpansionItems.BONE_MEAL,
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.OAK_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.BIRCH_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.BIRCH_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.ACACIA_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.ACACIA_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.CHERRY_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.CHERRY_LOG,16)},
                        new ItemStack[] {new ItemStack(Material.OAK_LOG,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.OAK_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.BIRCH_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.BIRCH_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.ACACIA_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.ACACIA_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.CHERRY_LEAVES,16)}).addRecipe(4,
                        new ItemStack[] {new ItemStack(Material.CHERRY_LEAVES,16)},
                        new ItemStack[] {new ItemStack(Material.OAK_LEAVES,16)})
                .register(plugin);

        //终极翠木转换机
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.WOOD_TRANSFORM_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.AMETHYST_SHARD,SlimefunItems.AIR_RUNE,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.WOOD_TRANSFORM_BASIC,MagicExpansionItems.WOOD_ELEMENT,MagicExpansionItems.WOOD_TRANSFORM_BASIC,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.OAK_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.BIRCH_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.BIRCH_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.ACACIA_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.ACACIA_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.CHERRY_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.CHERRY_LOG,64)},
                        new ItemStack[] {new ItemStack(Material.OAK_LOG,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.OAK_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.SPRUCE_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.BIRCH_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.BIRCH_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.JUNGLE_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.ACACIA_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.ACACIA_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.DARK_OAK_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.MANGROVE_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.CHERRY_LEAVES,64)}).addRecipe(1,
                        new ItemStack[] {new ItemStack(Material.CHERRY_LEAVES,64)},
                        new ItemStack[] {new ItemStack(Material.OAK_LEAVES,64)})
                .register(plugin);



        //光能激发器
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.LIGHT_TRANSFORM_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.QUARTZ,
                MagicExpansionItems.ALUMINUM_INGOT,MagicExpansionItems.LIGHT_CORE,MagicExpansionItems.LEAD_INGOT,
                MagicExpansionItems.QUARTZ,MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.LIGHT,32),MagicExpansionItems.MAGNESIUM_INGOT},
                        new ItemStack[] {MagicExpansionItems.LIGHT_ENERGY_ALPHA})
                .register(plugin);

        //光能溯源器
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.LIGHT_EXTRACT_BASIC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.OAK_PLANKS,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.OAK_PLANKS,
                MagicExpansionItems.GOLD_INGOT,MagicExpansionItems.LIGHT_CORE,MagicExpansionItems.SILVER_INGOT,
                MagicExpansionItems.OAK_PLANKS,MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.OAK_PLANKS
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(4, new ItemStack[] {new ItemStack(Material.GLOWSTONE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,8)}).addRecipe(1, new ItemStack[] {new ItemStack(Material.SEA_LANTERN,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,12)}).addRecipe(4, new ItemStack[] {new ItemStack(Material.REDSTONE_LAMP,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,10)}).addRecipe(4, new ItemStack[] {new ItemStack(Material.END_ROD,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,4)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.SOUL_LANTERN,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,3)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.LANTERN,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(8, new ItemStack[] {new ItemStack(Material.REDSTONE_TORCH,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,1)}).addRecipe(8, new ItemStack[] {new ItemStack(Material.TORCH,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,1)}).addRecipe(8, new ItemStack[] {new ItemStack(Material.SOUL_TORCH,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(4, new ItemStack[] {new ItemStack(Material.SHROOMLIGHT,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,5)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.OCHRE_FROGLIGHT,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,9)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.VERDANT_FROGLIGHT,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,9)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.PEARLESCENT_FROGLIGHT,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,9)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.CRYING_OBSIDIAN,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,1)}).addRecipe(6, new ItemStack[] {new ItemStack(Material.MAGMA_BLOCK,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,1)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.CAMPFIRE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,3)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.SOUL_CAMPFIRE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,6)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.END_CRYSTAL,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,6)}).addRecipe(2, new ItemStack[] {new ItemStack(Material.BEACON,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,9)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.REDSTONE_ORE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.DEEPSLATE_REDSTONE_ORE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(7, new ItemStack[] {new ItemStack(Material.SMALL_AMETHYST_BUD,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,1)}).addRecipe(7, new ItemStack[] {new ItemStack(Material.MEDIUM_AMETHYST_BUD,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(6, new ItemStack[] {new ItemStack(Material.LARGE_AMETHYST_BUD,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,3)}).addRecipe(5, new ItemStack[] {new ItemStack(Material.AMETHYST_CLUSTER,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,4)}).addRecipe(7, new ItemStack[] {new ItemStack(Material.TORCHFLOWER,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(7, new ItemStack[] {new ItemStack(Material.SEA_PICKLE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)}).addRecipe(4, new ItemStack[] {new ItemStack(Material.NETHER_STAR,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,7)}).addRecipe(7, new ItemStack[] {new ItemStack(Material.GLISTERING_MELON_SLICE,1)},
                        new ItemStack[] {new ItemStack(Material.LIGHT,2)})
                .register(plugin);


        //魔法建筑工坊
        new RecipeMachine(magicexpansionenergy, MagicExpansionItems.PRE_BUILDINGS_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.CORE_ORIGIN,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.OAK_PLANKS,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.OAK_PLANKS
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.OAK_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_OAK_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.SPRUCE_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[]{MagicExpansionItems.PRE_BUILDING_SPRUCE_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.BIRCH_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_BIRCH_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.JUNGLE_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_JUNGLE_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.ACACIA_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_ACACIA_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.DARK_OAK_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_DARK_OAK_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.MANGROVE_PROPAGULE),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_MANGROVE_TREE})
                .addRecipe(5, new ItemStack[] {new ItemStack(Material.CHERRY_SAPLING),MagicExpansionItems.BONE_MEAL},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_CHERRY_TREE})
                .addRecipe(5, new ItemStack[] {MagicExpansionItems.REDSTONE_1,sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),MagicExpansionItems.OAK_LOG_1,
                                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COLOR_WOOL_1,MagicExpansionItems.COLOR_CONCRETE_1,
                                MagicExpansionItems.COLOR_TERRACOTTA_1},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_TAFEI})
                .addRecipe(5, new ItemStack[] {
                                new ItemStack(Material.PINK_CONCRETE,64),new ItemStack(Material.BLACK_CONCRETE,64),new ItemStack(Material.SNOW_BLOCK,64),
                                new ItemStack(Material.SMOOTH_QUARTZ,64),new ItemStack(Material.PINK_CONCRETE_POWDER,64),new ItemStack(Material.PURPLE_CONCRETE_POWDER,64),
                                new ItemStack(Material.CHERRY_PLANKS,64),new ItemStack(Material.PURPLE_CONCRETE,64)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_DORA_PICTURE})
                .addRecipe(5, new ItemStack[] {
                        MagicExpansionItems.REDSTONE_1,sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),MagicExpansionItems.OAK_LOG_1,
                        MagicExpansionItems.LIGHT_1,MagicExpansionItems.COLOR_TERRACOTTA_1,MagicExpansionItems.COLOR_CONCRETE_1,
                        new ItemStack(Material.RED_WOOL)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_WISH_DALE})
                .addRecipe(5, new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,15),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,2),MagicExpansionItems.OAK_LOG_1,
                                MagicExpansionItems.COLOR_WOOL_1,MagicExpansionItems.STONE_1,MagicExpansionItems.GLASS_1,
                                NamedTagBuilder.nameTag("anon")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_ANON})
                .addRecipe(5, new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                        MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.OAK_LEAVES,32)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_HOUSE_OAK})
                .addRecipe(5, new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.SPRUCE_LEAVES,32)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_HOUSE_SPRUCE})
                .addRecipe(5, new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.CHERRY_LEAVES,32)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_HOUSE_CHERRY})
                .addRecipe(5, new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.MANGROVE_LEAVES,32)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_HOUSE_MANGROVE})
                .addRecipe(5, new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.MOSS_BLOCK,32)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_HOUSE_MOSS})
                .addRecipe(5, new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.WHITE_WOOL,32)},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_HOUSE_WIND_CAR})

                .addRecipe(5,new ItemStack[] {
                        sfItemAmount(MagicExpansionItems.COLOR_LOG_1,21),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,7),sfItemAmount(MagicExpansionItems.GLASS_1,2),
                        sfItemAmount(MagicExpansionItems.OAK_LOG_1,2),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),
                        NamedTagBuilder.nameTag("WineBar")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_WINE_BAR})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,2),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,2),sfItemAmount(MagicExpansionItems.LIGHT_1,1),
                                sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,1),NamedTagBuilder.nameTag("FishingPort")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_FISHING_PORT})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,2),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,3),
                                sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,1),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),
                                NamedTagBuilder.nameTag("Bakery")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_BAKERY})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,10),sfItemAmount(MagicExpansionItems.GLASS_1,2),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,6),
                                sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,1),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),
                                NamedTagBuilder.nameTag("KFCSmall")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_KFC_SMALL})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,6),sfItemAmount(MagicExpansionItems.GLASS_1,1),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,2),
                                sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,1),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),
                                NamedTagBuilder.nameTag("MiXueBingCheng")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_LITTLE_MI_XUE})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COBBLESTONE_1,5),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,4),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,2),
                                sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),NamedTagBuilder.nameTag("JapanHouse")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_JAPAN_HOUSE})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,6),sfItemAmount(MagicExpansionItems.GLASS_1,1),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,2),
                                sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,1),new ItemStack(Material.IRON_INGOT,31),NamedTagBuilder.nameTag("FarmBarn")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_FARM_BARN})
                .addRecipe(5,new ItemStack[] {
                                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,3),sfItemAmount(MagicExpansionItems.GLASS_1,3),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,1),
                                sfItemAmount(MagicExpansionItems.OAK_LOG_1,2),new ItemStack(Material.QUARTZ_BLOCK,64),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),
                                NamedTagBuilder.nameTag("FruitShop")},
                        new ItemStack[] {MagicExpansionItems.PRE_BUILDING_FRUIT_SHOP})

                .register(plugin);


        //纯净硅源机
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.QUARTZ_PURE_MACHINE_BAISC, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,SlimefunItems.BIG_CAPACITOR,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.QUARTZ_CORE,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.AMETHYST_SHARD
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.GOLD_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.IRON_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.COPPER_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.LEAD_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.TIN_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.SILVER_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.ZINC_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.ALUMINUM_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.MAGNESIUM_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,2), sfItemAmount(SlimefunItems.SILICON,2)})
                .addRecipe(3, new ItemStack[] {MagicExpansionItems.ELEMENT_INGOT}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,18), sfItemAmount(SlimefunItems.SILICON,18)})
                .addRecipe(9, new ItemStack[] {SlimefunItems.GOLD_4K},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {new ItemStack(Material.GOLD_INGOT)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {new ItemStack(Material.IRON_INGOT)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {new ItemStack(Material.COPPER_INGOT)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.COPPER_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.LEAD_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.TIN_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.SILVER_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.ZINC_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.ALUMINUM_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)}).addRecipe(9, new ItemStack[] {SlimefunItems.MAGNESIUM_INGOT},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK)})
                .register(plugin);

        //终极纯净硅源机
        new RecipeMachine(magicexpansionrecipemachine, MagicExpansionItems.QUARTZ_PURE_MACHINE_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.QUARTZ,MagicExpansionItems.AMETHYST_SHARD,
                MagicExpansionItems.QUARTZ_PURE_MACHINE_BAISC,MagicExpansionItems.FIVE_ELEMENT,MagicExpansionItems.QUARTZ_PURE_MACHINE_BAISC,
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.GOLD_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.IRON_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.COPPER_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.LEAD_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.TIN_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.SILVER_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.ZINC_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.ALUMINUM_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.MAGNESIUM_INGOT,32)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(1, new ItemStack[] {sfItemAmount(MagicExpansionItems.ELEMENT_INGOT,4)}, new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64), sfItemAmount(SlimefunItems.SILICON,64)})
                .addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.GOLD_4K,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {new ItemStack(Material.GOLD_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {new ItemStack(Material.IRON_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {new ItemStack(Material.COPPER_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.COPPER_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.LEAD_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.TIN_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.SILVER_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.ZINC_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.ALUMINUM_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)}).addRecipe(3, new ItemStack[] {sfItemAmount(SlimefunItems.MAGNESIUM_INGOT,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ_BLOCK,64)})
                .register(plugin);

        //一体化·矿粉产线
        new RecipeRandomMachine(magicexpansionrecipemachine, MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.OAK_PLANKS,SlimefunItems.ELECTRIC_GOLD_PAN_3,MagicExpansionItems.OAK_PLANKS,
                SlimefunItems.ELECTRIC_DUST_WASHER_3,MagicExpansionItems.AMETHYST_SHARD,SlimefunItems.ELECTRIC_ORE_GRINDER_3,
                MagicExpansionItems.IRON_INGOT,MagicExpansionItems.IRON_INGOT,MagicExpansionItems.IRON_INGOT
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {sfItemAmount(SlimefunItems.SIFTED_ORE,1)},
                        new ItemStack[] {SlimefunItems.IRON_DUST,SlimefunItems.GOLD_DUST,SlimefunItems.LEAD_DUST,
                                SlimefunItems.SILVER_DUST,SlimefunItems.COPPER_DUST,SlimefunItems.ALUMINUM_DUST,
                                SlimefunItems.MAGNESIUM_DUST,SlimefunItems.TIN_DUST,SlimefunItems.ZINC_DUST,SlimefunItems.STONE_CHUNK})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.GRAVEL)},
                        new ItemStack[] {new ItemStack(Material.IRON_NUGGET,2),new ItemStack(Material.CLAY_BALL,2),
                                new ItemStack(Material.FLINT,2),sfItemAmount(SlimefunItems.SIFTED_ORE,2)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.COBBLESTONE)},
                        new ItemStack[] {new ItemStack(Material.GRAVEL)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BONE)},
                        new ItemStack[] {new ItemStack(Material.BONE_MEAL,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BLAZE_ROD)},
                        new ItemStack[] {new ItemStack(Material.BLAZE_POWDER,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIRT)},
                        new ItemStack[] {SlimefunItems.STONE_CHUNK})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIAMOND)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.CARBON,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.NETHER_WART)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.MAGIC_LUMP_1,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.ENDER_EYE)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.ENDER_LUMP_1,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SAND,1)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ),new ItemStack(Material.NETHER_WART),
                                new ItemStack(Material.BLAZE_POWDER),new ItemStack(Material.GOLD_NUGGET),
                                new ItemStack(Material.GLOWSTONE_DUST),new ItemStack(Material.GHAST_TEAR)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SOIL,4)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ),new ItemStack(Material.NETHER_WART),
                                new ItemStack(Material.BLAZE_POWDER),new ItemStack(Material.GOLD_NUGGET),
                                new ItemStack(Material.GLOWSTONE_DUST),new ItemStack(Material.GHAST_TEAR)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SAND)},
                        new ItemStack[] {SlimefunItems.SALT})
                .register(plugin);
        //一体化·矿粉产线-输入输出分离
        new RecipeRandomMachineDefault(magicexpansionrecipemachine, MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_DEFAULT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,MAGIC_EXPANSION_MAGIC_SUGAR_2,null,
                null,INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE,null,
                null,null,null
        })
                .setCapacity(1314)
                .setConsumption(26)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {sfItemAmount(SlimefunItems.SIFTED_ORE,1)},
                        new ItemStack[] {SlimefunItems.IRON_DUST,SlimefunItems.GOLD_DUST,SlimefunItems.LEAD_DUST,
                                SlimefunItems.SILVER_DUST,SlimefunItems.COPPER_DUST,SlimefunItems.ALUMINUM_DUST,
                                SlimefunItems.MAGNESIUM_DUST,SlimefunItems.TIN_DUST,SlimefunItems.ZINC_DUST,SlimefunItems.STONE_CHUNK})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.GRAVEL)},
                        new ItemStack[] {new ItemStack(Material.IRON_NUGGET,2),new ItemStack(Material.CLAY_BALL,2),
                                new ItemStack(Material.FLINT,2),sfItemAmount(SlimefunItems.SIFTED_ORE,2)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.COBBLESTONE)},
                        new ItemStack[] {new ItemStack(Material.GRAVEL)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BONE)},
                        new ItemStack[] {new ItemStack(Material.BONE_MEAL,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BLAZE_ROD)},
                        new ItemStack[] {new ItemStack(Material.BLAZE_POWDER,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIRT)},
                        new ItemStack[] {SlimefunItems.STONE_CHUNK})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIAMOND)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.CARBON,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.NETHER_WART)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.MAGIC_LUMP_1,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.ENDER_EYE)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.ENDER_LUMP_1,4)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SAND,1)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ),new ItemStack(Material.NETHER_WART),
                                new ItemStack(Material.BLAZE_POWDER),new ItemStack(Material.GOLD_NUGGET),
                                new ItemStack(Material.GLOWSTONE_DUST),new ItemStack(Material.GHAST_TEAR)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SOIL,4)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ),new ItemStack(Material.NETHER_WART),
                                new ItemStack(Material.BLAZE_POWDER),new ItemStack(Material.GOLD_NUGGET),
                                new ItemStack(Material.GLOWSTONE_DUST),new ItemStack(Material.GHAST_TEAR)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SAND)},
                        new ItemStack[] {SlimefunItems.SALT})
                .register(plugin);

        //一体化·矿粉产线·ULTRA
        new RecipeRandomMachine(magicexpansionrecipemachine, MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE,MagicExpansionItems.ELEMENT_INGOT,
                MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE,MagicExpansionItems.SPEED_ELEMENT_64,MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE,
                MagicExpansionItems.AMETHYST_SHARD,MagicExpansionItems.INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE,MagicExpansionItems.AMETHYST_SHARD
        })
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {sfItemAmount(SlimefunItems.SIFTED_ORE,64)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.IRON_DUST,64),sfItemAmount(SlimefunItems.GOLD_DUST,64),sfItemAmount(SlimefunItems.LEAD_DUST,64),
                                sfItemAmount(SlimefunItems.SILVER_DUST,64),sfItemAmount(SlimefunItems.COPPER_DUST,64),sfItemAmount(SlimefunItems.ALUMINUM_DUST,64),
                                sfItemAmount(SlimefunItems.MAGNESIUM_DUST,64),sfItemAmount(SlimefunItems.TIN_DUST,64),sfItemAmount(SlimefunItems.ZINC_DUST,64),sfItemAmount(SlimefunItems.STONE_CHUNK,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.GRAVEL,32)},
                        new ItemStack[] {new ItemStack(Material.IRON_NUGGET,64),new ItemStack(Material.CLAY_BALL,64),
                                new ItemStack(Material.FLINT,64),sfItemAmount(SlimefunItems.SIFTED_ORE,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.COBBLESTONE,64)},
                        new ItemStack[] {new ItemStack(Material.GRAVEL,48)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BONE,16)},
                        new ItemStack[] {new ItemStack(Material.BONE_MEAL,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BLAZE_ROD,16)},
                        new ItemStack[] {new ItemStack(Material.BLAZE_POWDER,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIRT,64)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.STONE_CHUNK,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIAMOND,16)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.CARBON,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.NETHER_WART,16)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.MAGIC_LUMP_1,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.ENDER_EYE,16)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.ENDER_LUMP_1,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SAND,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ,64),new ItemStack(Material.NETHER_WART,64),
                                new ItemStack(Material.BLAZE_POWDER,64),new ItemStack(Material.GOLD_NUGGET,64),
                                new ItemStack(Material.GLOWSTONE_DUST,64),new ItemStack(Material.GHAST_TEAR,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SOIL,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ,64),new ItemStack(Material.NETHER_WART,64),
                                new ItemStack(Material.BLAZE_POWDER,64),new ItemStack(Material.GOLD_NUGGET,64),
                                new ItemStack(Material.GLOWSTONE_DUST,64),new ItemStack(Material.GHAST_TEAR,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SAND,64)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.SALT,64)})
                .register(plugin);

        //一体化·矿粉产线·ULTRA-输入输出分离
        new RecipeRandomMachineDefault(magicexpansionrecipemachine, INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA_DEFAULT, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,MAGIC_EXPANSION_MAGIC_SUGAR_2,null,
                null,INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA,null,
                null,null,null
        })
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .addRecipe(1, new ItemStack[] {sfItemAmount(SlimefunItems.SIFTED_ORE,64)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.IRON_DUST,64),sfItemAmount(SlimefunItems.GOLD_DUST,64),sfItemAmount(SlimefunItems.LEAD_DUST,64),
                                sfItemAmount(SlimefunItems.SILVER_DUST,64),sfItemAmount(SlimefunItems.COPPER_DUST,64),sfItemAmount(SlimefunItems.ALUMINUM_DUST,64),
                                sfItemAmount(SlimefunItems.MAGNESIUM_DUST,64),sfItemAmount(SlimefunItems.TIN_DUST,64),sfItemAmount(SlimefunItems.ZINC_DUST,64),sfItemAmount(SlimefunItems.STONE_CHUNK,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.GRAVEL,32)},
                        new ItemStack[] {new ItemStack(Material.IRON_NUGGET,64),new ItemStack(Material.CLAY_BALL,64),
                                new ItemStack(Material.FLINT,64),sfItemAmount(SlimefunItems.SIFTED_ORE,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.COBBLESTONE,64)},
                        new ItemStack[] {new ItemStack(Material.GRAVEL,48)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BONE,16)},
                        new ItemStack[] {new ItemStack(Material.BONE_MEAL,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.BLAZE_ROD,16)},
                        new ItemStack[] {new ItemStack(Material.BLAZE_POWDER,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIRT,64)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.STONE_CHUNK,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.DIAMOND,16)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.CARBON,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.NETHER_WART,16)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.MAGIC_LUMP_1,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.ENDER_EYE,16)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.ENDER_LUMP_1,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SAND,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ,64),new ItemStack(Material.NETHER_WART,64),
                                new ItemStack(Material.BLAZE_POWDER,64),new ItemStack(Material.GOLD_NUGGET,64),
                                new ItemStack(Material.GLOWSTONE_DUST,64),new ItemStack(Material.GHAST_TEAR,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SOUL_SOIL,64)},
                        new ItemStack[] {new ItemStack(Material.QUARTZ,64),new ItemStack(Material.NETHER_WART,64),
                                new ItemStack(Material.BLAZE_POWDER,64),new ItemStack(Material.GOLD_NUGGET,64),
                                new ItemStack(Material.GLOWSTONE_DUST,64),new ItemStack(Material.GHAST_TEAR,64)})
                .addRecipe(1, new ItemStack[] {new ItemStack(Material.SAND,64)},
                        new ItemStack[] {sfItemAmount(SlimefunItems.SALT,64)})
                .register(plugin);





        // 预制树-说明
        new UnplaceableBlock(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_TREE_INFO, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,null,null,
                null,null,null
        }).register(plugin);

        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_OAK_TREE, "oak_tree", "OAK", "OAK",1, new ItemStack(Material.OAK_SAPLING));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_SPRUCE_TREE, "oak_tree", "OAK", "SPRUCE", 1, new ItemStack(Material.SPRUCE_SAPLING));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_BIRCH_TREE, "oak_tree", "OAK", "BIRCH", 1, new ItemStack(Material.BIRCH_SAPLING));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_JUNGLE_TREE, "oak_tree", "OAK", "JUNGLE", 1, new ItemStack(Material.JUNGLE_SAPLING));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_ACACIA_TREE, "oak_tree", "OAK", "ACACIA", 1, new ItemStack(Material.ACACIA_SAPLING));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_DARK_OAK_TREE, "oak_tree", "OAK", "DARK_OAK", 1, new ItemStack(Material.DARK_OAK_SAPLING));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_MANGROVE_TREE, "oak_tree", "OAK", "MANGROVE", 1, new ItemStack(Material.MANGROVE_PROPAGULE));
        registerPreBuildingTree(plugin, MagicExpansionItems.PRE_BUILDING_CHERRY_TREE, "oak_tree", "OAK", "CHERRY", 1, new ItemStack(Material.CHERRY_SAPLING));

        // 预制建筑-永雏塔菲
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_TAFEI, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                MagicExpansionItems.REDSTONE_1,sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),MagicExpansionItems.OAK_LOG_1,
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COLOR_WOOL_1,MagicExpansionItems.COLOR_CONCRETE_1,
                MagicExpansionItems.COLOR_TERRACOTTA_1,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"tafei",5).register(plugin);
        // 预制建筑-维什戴尔
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_WISH_DALE, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                MagicExpansionItems.REDSTONE_1,sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),MagicExpansionItems.OAK_LOG_1,
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COLOR_TERRACOTTA_1,MagicExpansionItems.COLOR_CONCRETE_1,
                new ItemStack(Material.RED_WOOL),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"wish_dale",5).register(plugin);
        // 预制建筑-千岛爱音
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_ANON, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,15),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,2),MagicExpansionItems.OAK_LOG_1,
                MagicExpansionItems.COLOR_WOOL_1,MagicExpansionItems.STONE_1,MagicExpansionItems.GLASS_1,
                NamedTagBuilder.nameTag("anon"),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"anon",5).register(plugin);


        // 预制建筑-dora像素画
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_DORA_PICTURE, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                new ItemStack(Material.PINK_CONCRETE,64),new ItemStack(Material.BLACK_CONCRETE,64),new ItemStack(Material.SNOW_BLOCK,64),
                new ItemStack(Material.SMOOTH_QUARTZ,64),new ItemStack(Material.PINK_CONCRETE_POWDER,64),new ItemStack(Material.PURPLE_CONCRETE_POWDER,64),
                new ItemStack(Material.CHERRY_PLANKS,64),new ItemStack(Material.PURPLE_CONCRETE,64),new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"dora_picture",5).register(plugin);

        // 预制建筑-橡木小屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_HOUSE_OAK, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.OAK_LEAVES,32),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"oak_house",5).register(plugin);
        // 预制建筑-云杉小屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_HOUSE_SPRUCE, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.SPRUCE_LEAVES,32),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"spruce_house",5).register(plugin);
        // 预制建筑-樱花小屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_HOUSE_CHERRY, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.CHERRY_LEAVES,32),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"cherry_house",5).register(plugin);
        // 预制建筑-红树小屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_HOUSE_MANGROVE, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.MANGROVE_LEAVES,32),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"mangrove_house",5).register(plugin);
        // 预制建筑-苔藓小屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_HOUSE_MOSS, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.MOSS_BLOCK,32),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"moss_house",5).register(plugin);
        // 预制建筑-风车小屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_HOUSE_WIND_CAR, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                MagicExpansionItems.LIGHT_1,MagicExpansionItems.COBBLESTONE_1,new ItemStack(Material.WHITE_WOOL,32),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"wind_car_house",5).register(plugin);


        // 预制建筑-小酒吧
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_WINE_BAR, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,21),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,7),sfItemAmount(MagicExpansionItems.GLASS_1,2),
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,2),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),
                NamedTagBuilder.nameTag("WineBar"),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"wine_bar",5).register(plugin);
        // 预制建筑-钓鱼港口
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_FISHING_PORT, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,2),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,2),sfItemAmount(MagicExpansionItems.LIGHT_1,1),
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,1),NamedTagBuilder.nameTag("FishingPort"),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"fishing_port",5).register(plugin);
        // 预制建筑-烘焙房
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_BAKERY, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,2),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,3),
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,1),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),
                NamedTagBuilder.nameTag("Bakery"),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"bakery",5).register(plugin);
        // 预制建筑-kfc-small
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_KFC_SMALL, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,10),sfItemAmount(MagicExpansionItems.GLASS_1,2),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,6),
                sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,1),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),
                NamedTagBuilder.nameTag("KFCSmall"),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"kfc_little",5).register(plugin);
        // 预制建筑-蜜雪冰城
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_LITTLE_MI_XUE, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,6),sfItemAmount(MagicExpansionItems.GLASS_1,1),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,2),
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,1),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),
                NamedTagBuilder.nameTag("MiXueBingCheng"),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"little_mi_xue",5).register(plugin);
        // 预制建筑-日式町屋
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_JAPAN_HOUSE, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COBBLESTONE_1,5),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,4),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,2),
                sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,1),NamedTagBuilder.nameTag("JapanHouse"),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"japan_house_1",5).register(plugin);
        // 预制建筑-农场谷仓
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_FARM_BARN, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,6),sfItemAmount(MagicExpansionItems.GLASS_1,1),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,2),
                sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,1),new ItemStack(Material.IRON_INGOT,31),NamedTagBuilder.nameTag("FarmBarn"),
                null,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"farm_barn",5).register(plugin);
        // 预制建筑-水果店
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_FRUIT_SHOP, PRE_BUILDINGS_MACHINE, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,3),sfItemAmount(MagicExpansionItems.GLASS_1,3),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,1),
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,2),new ItemStack(Material.QUARTZ_BLOCK,64),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),
                NamedTagBuilder.nameTag("FruitShop"),null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"fruit_shop",5).register(plugin);










        // 预制建筑-村民爱与交易所
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_VILLAGE_LOVE_AND_TRADE_HOUSE, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                MagicExpansionItems.STONE_2,MagicExpansionItems.IRON_INGOT_2,sfItemAmount(MagicExpansionItems.OAK_LOG_1,16),
                sfItemAmount(MagicExpansionItems.REDSTONE_1,5),MagicExpansionItems.GLASS_1,MagicExpansionItems.COLOR_WOOL_1
                ,MagicExpansionItems.SPACE_INFINITY_MAGIC,null,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"village_love_trade_house",60).register(plugin);
        // 预制建筑-潜影贝农场
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_SHULKER_FARM, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                MagicExpansionItems.STONE_2,MagicExpansionItems.IRON_INGOT_1,sfItemAmount(MagicExpansionItems.OAK_LOG_1,16),
                MagicExpansionItems.REDSTONE_1,MagicExpansionItems.COBBLESTONE_1,MagicExpansionItems.HOPPER_1,
                new ItemStack(Material.STRING,15),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"shulker_farm",60).register(plugin);
        // 预制建筑-麦当劳
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_MCDONALDS, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,48),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,6),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,6),
                sfItemAmount(MagicExpansionItems.IRON_INGOT_1,8),sfItemAmount(MagicExpansionItems.COLOR_LOG_2,1),sfItemAmount(MagicExpansionItems.REDSTONE_2,1),
                NamedTagBuilder.nameTag("Mcdonalds"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"mcdonalds",60).register(plugin);

        //先写大型的
        // 预制建筑-月兔商店
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_MOON_RABBIT_SHOP, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,4),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,36),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,2),
                sfItemAmount(MagicExpansionItems.STONE_2,2),sfItemAmount(MagicExpansionItems.OAK_LOG_1,1),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),
                NamedTagBuilder.nameTag("MoonRabbitShop"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"moon_rabbit_shop",60).register(plugin);
        // 预制建筑-中型马厩
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_MIDDLE_HORSE_HOUSE, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,10),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,2),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),
                sfItemAmount(MagicExpansionItems.STONE_2,2),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,1),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,2),
                NamedTagBuilder.nameTag("MiddleHorseHouse"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"middle_horse_house",60).register(plugin);
        // 预制建筑-四合院
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_SI_HE_YUAN, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,14),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,6),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),
                sfItemAmount(MagicExpansionItems.COBBLESTONE_1,5),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,8),sfItemAmount(MagicExpansionItems.OAK_LOG_1,5),
                NamedTagBuilder.nameTag("SiHeYuan"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"si_he_yuan",60).register(plugin);
        // 预制建筑-KFC中型
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_KFC_MIDDLE, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,23),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,9),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,19),
                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,5),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,3),sfItemAmount(MagicExpansionItems.STONE_1,2),
                NamedTagBuilder.nameTag("KFCMiddle"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"kfc_middle",60).register(plugin);
        // 预制建筑-大雪王
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_LARGE_SNOW_KING, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,25),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,21),sfItemAmount(COLOR_LEAVES_1,18),
                sfItemAmount(MagicExpansionItems.COLOR_LOG_1,3),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,2),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,2),
                NamedTagBuilder.nameTag("LargeSnowKing"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"large_snow_king",60).register(plugin);
        // 预制建筑-中型别墅
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_MIDDLE_VILLA, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_LOG_2,1),sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_2,1),sfItemAmount(MagicExpansionItems.STONE_BRICKS_1,7),
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,8),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,2),new ItemStack(Material.DRAGON_EGG),
                NamedTagBuilder.nameTag("MiddleVilla"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"middle_villa_1",60).register(plugin);
        // 预制建筑-樱花商店
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_SAKURA_SHOP, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.COLOR_CONCRETE_1,1),sfItemAmount(MagicExpansionItems.COLOR_TERRACOTTA_1,4),sfItemAmount(MagicExpansionItems.COLOR_LOG_1,6),
                sfItemAmount(MagicExpansionItems.GLASS_1,4),sfItemAmount(MagicExpansionItems.COLOR_LEAVES_1,5),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,7),
                NamedTagBuilder.nameTag("SakuraShop"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"sakura_shop",60).register(plugin);
        // 预制建筑-蟹堡王
        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_KRUSTY_KRAB, PRE_BUILDINGS_MACHINE_ADVANCED, new ItemStack[] {
                sfItemAmount(MagicExpansionItems.OAK_LOG_1,35),sfItemAmount(MagicExpansionItems.COBBLESTONE_1,16),sfItemAmount(MagicExpansionItems.COLOR_WOOL_1,3),
                sfItemAmount(MagicExpansionItems.GLASS_1,7),sfItemAmount(MagicExpansionItems.IRON_INGOT_1,3),sfItemAmount(MagicExpansionItems.LIGHT_1,3),
                NamedTagBuilder.nameTag("KrustyKrab"),MagicExpansionItems.SPACE_INFINITY_MAGIC,new CustomItemStack(CustomHead.getHead("a92974681687689da7dda3f19b7e4a53fe0dd09befd7fa8838744384c9d1ac71"),getGradientName("This is a shapeless recipe."))
        },"krusty_krab",60).register(plugin);





//        // 预制树-橡木树
//        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_OAK_TREE, SPECIAL_RECIPE_TYPE, new ItemStack[] {
//                null,null,null,
//                null,null,null,
//                null,null,null
//        },"oak_tree").register(plugin);
//        // 预制树-云杉树
//        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_SPRUCE_TREE, SPECIAL_RECIPE_TYPE, new ItemStack[] {
//                null,null,null,
//                null,null,null,
//                null,null,null
//        },"oak_tree","OAK","SPRUCE").register(plugin);
//        // 预制树-樱花树
//        new PreBuildingTree(magicexpansionprebuilding, MagicExpansionItems.PRE_BUILDING_CHERRY_TREE, SPECIAL_RECIPE_TYPE, new ItemStack[] {
//                null,null,null,
//                null,null,null,
//                null,null,null
//        },"oak_tree","OAK","CHERRY").register(plugin);






        //MIHOYO角色,要放在激光扫描仪下面
        //星光扫描仪-盲盒拆解器
        new RandomBoxMachine(magicexpansionenergy, MagicExpansionItems.MIHOYO_STAR_RAY_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.IRON_INGOT),MagicExpansionItems.LIGHT_CORE,new ItemStack(Material.IRON_INGOT),
                MagicExpansionItems.LIGHT_CORE,new ItemStack(Material.LIGHT),MagicExpansionItems.LIGHT_CORE,
                new ItemStack(Material.IRON_INGOT),MagicExpansionItems.LIGHT_CORE,new ItemStack(Material.IRON_INGOT)
        })
                .addRecipe(0, new ItemStack[]{MagicExpansionItems.HONKAI_STAR_RAIL_BOX}, new ItemStack[]{MagicExpansionItems.JING_LIU})
                .addRecipe(0, new ItemStack[]{MagicExpansionItems.HONKAI_STAR_RAIL_BOX}, new ItemStack[]{MagicExpansionItems.SILVER_WOLF})
                .addRecipe(0, new ItemStack[]{MagicExpansionItems.HONKAI_STAR_RAIL_BOX}, new ItemStack[]{MagicExpansionItems.KAFKA})
                .addRecipe(0, new ItemStack[]{MagicExpansionItems.HONKAI_STAR_RAIL_BOX}, new ItemStack[]{MagicExpansionItems.SUNDAY})
                .addRecipe(0, new ItemStack[]{MagicExpansionItems.HONKAI_STAR_RAIL_BOX}, new ItemStack[]{MagicExpansionItems.HUOHUO})
                .addRecipe(0, new ItemStack[]{MagicExpansionItems.HONKAI_STAR_RAIL_BOX}, new ItemStack[]{MagicExpansionItems.LUNAE})
                .register(plugin);
        //崩铁
        new HonkaiStarRailBox(magicexpansionhonkai, MagicExpansionItems.HONKAI_STAR_RAIL_BOX, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.IRON_INGOT),new ItemStack(Material.GUNPOWDER),new ItemStack(Material.IRON_INGOT),
                new ItemStack(Material.GUNPOWDER),SlimefunItems.MAGIC_SUGAR,new ItemStack(Material.GUNPOWDER),
                new ItemStack(Material.IRON_INGOT),new ItemStack(Material.GUNPOWDER),new ItemStack(Material.IRON_INGOT)
        }).register(plugin);

        new SlimefunItem(magicexpansionhonkai, MagicExpansionItems.JING_LIU, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(CustomHead.BLUE_GIFT_BOX.getItem(), "&c&lSpecial Drop: &cObtained from the Honkai Star Rail Box"),null,
                null,null,null
        }).register(plugin);

        new SlimefunItem(magicexpansionhonkai, MagicExpansionItems.SILVER_WOLF, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(CustomHead.BLUE_GIFT_BOX.getItem(), "&c&lSpecial Drop: &cObtained from the Honkai Star Rail Box"),null,
                null,null,null
        }).register(plugin);

        new SlimefunItem(magicexpansionhonkai, MagicExpansionItems.KAFKA, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(CustomHead.BLUE_GIFT_BOX.getItem(), "&c&lSpecial Drop: &cObtained from the Honkai Star Rail Box"),null,
                null,null,null
        }).register(plugin);

        new SlimefunItem(magicexpansionhonkai, MagicExpansionItems.SUNDAY, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(CustomHead.BLUE_GIFT_BOX.getItem(), "&c&lSpecial Drop: &cObtained from the Honkai Star Rail Box"),null,
                null,null,null
        }).register(plugin);

        new SlimefunItem(magicexpansionhonkai, MagicExpansionItems.LUNAE, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(CustomHead.BLUE_GIFT_BOX.getItem(), "&c&lSpecial Drop: &cObtained from the Honkai Star Rail Box"),null,
                null,null,null
        }).register(plugin);

        new SlimefunItem(magicexpansionhonkai, MagicExpansionItems.HUOHUO, SPECIAL_RECIPE_TYPE, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(CustomHead.BLUE_GIFT_BOX.getItem(), "&c&lSpecial Drop: &cObtained from the Honkai Star Rail Box"),null,
                null,null,null
        }).register(plugin);



        //空岛系列
        new SingleCubeOrigin(magicexpansionskyblock, MagicExpansionItems.SINGLE_CUBE_ORIGIN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.TIN_INGOT,MagicExpansionItems.GOLD_INGOT, MagicExpansionItems.COPPER_INGOT,
                MagicExpansionItems.ALUMINUM_INGOT,new ItemStack(Material.DIRT),MagicExpansionItems.LEAD_INGOT,
                MagicExpansionItems.ZINC_INGOT,MagicExpansionItems.SILVER_INGOT,MagicExpansionItems.MAGNESIUM_INGOT,
        }).register(plugin);

        new SingleCubeOre(magicexpansionskyblock, MagicExpansionItems.SINGLE_CUBE_ORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.IRON_ORE),new ItemStack(Material.COPPER_ORE), new ItemStack(Material.GOLD_ORE),
                new ItemStack(Material.EMERALD_ORE),MagicExpansionItems.SINGLE_CUBE_ORIGIN,new ItemStack(Material.DIAMOND_ORE),
                new ItemStack(Material.COAL_ORE),new ItemStack(Material.NETHER_QUARTZ_ORE),new ItemStack(Material.REDSTONE_ORE),
        }).register(plugin);




        //空岛系列-钻石工具
        new InfiniteTool(magicexpansionskyblock, MagicExpansionItems.SINGLE_DIAMOND_PICKAXE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.DIAMOND,MagicExpansionItems.DIAMOND, MagicExpansionItems.DIAMOND,
                null,MagicExpansionItems.STICK,null,
                null,MagicExpansionItems.STICK,null,
        }).register(plugin);
        //空岛系列-钻石工具
        new InfiniteTool(magicexpansionskyblock, MagicExpansionItems.SINGLE_DIAMOND_AXE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,MagicExpansionItems.DIAMOND, MagicExpansionItems.DIAMOND,
                null,MagicExpansionItems.STICK,MagicExpansionItems.DIAMOND,
                null,MagicExpansionItems.STICK,null,
        }).register(plugin);
        //空岛系列-钻石工具
        new InfiniteTool(magicexpansionskyblock, MagicExpansionItems.SINGLE_DIAMOND_SHOVEL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,MagicExpansionItems.DIAMOND,null,
                null,MagicExpansionItems.STICK,null,
                null,MagicExpansionItems.STICK,null,
        }).register(plugin);
        //空岛系列-钻石工具
        new InfiniteTool(magicexpansionskyblock, MagicExpansionItems.SINGLE_DIAMOND_HOE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null,MagicExpansionItems.DIAMOND, MagicExpansionItems.DIAMOND,
                null,MagicExpansionItems.STICK,null,
                null,MagicExpansionItems.STICK,null,
        }).register(plugin);




        //粘液刻投影
        new SfTimingsMachine(magicexpansionenergy, MagicExpansionItems.SF_TIMINGS_HOLOGRAM, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.SLIME_BALL),new ItemStack(Material.SLIME_BALL),new ItemStack(Material.SLIME_BALL),
                new ItemStack(Material.SLIME_BALL),SlimefunItems.HOLOGRAM_PROJECTOR,new ItemStack(Material.SLIME_BALL),
                new ItemStack(Material.SLIME_BALL),new ItemStack(Material.SLIME_BALL),new ItemStack(Material.SLIME_BALL)
        }).register(plugin);
        //粘液刻投影
        new TestMusicRightClick(magicexpansionspecialitem, MagicExpansionItems.MUSIC_TEST, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.RABBIT),new ItemStack(Material.COOKED_RABBIT),new ItemStack(Material.RABBIT),
                new ItemStack(Material.COOKED_RABBIT),new ItemStack(Material.SLIME_BALL),new ItemStack(Material.COOKED_RABBIT),
                new ItemStack(Material.RABBIT),new ItemStack(Material.COOKED_RABBIT),new ItemStack(Material.RABBIT)
        }).register(plugin);

        //魔法存储终端
        new CargoCore(magicexpansionenergy, MagicExpansionItems.CARGO_TERMINAL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.CARGO_MANAGER,SlimefunItems.CRAFTER_SMART_PORT,SlimefunItems.CARGO_MANAGER,
                SlimefunItems.CARGO_INPUT_NODE,SlimefunItems.CARGO_MANAGER,SlimefunItems.CARGO_OUTPUT_NODE_2,
                SlimefunItems.CARGO_MANAGER,FIVE_ELEMENT_TOUCH,SlimefunItems.CARGO_MANAGER
        }).register(plugin);

        //测试存储
        new CargoCoreMore(magicexpansionenergy, CARGO_TERMINAL_RENEW, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.CHEST),PURE_FIVE_ELEMENT,new ItemStack(Material.CHEST),
                PURE_ELEMENT_INGOT,CARGO_TERMINAL,PURE_ELEMENT_INGOT,
                new ItemStack(Material.CHEST),PURE_FIVE_ELEMENT,new ItemStack(Material.CHEST)
        }).register(plugin);


        //存储碎片
        new CargoFragment(magicexpansionspecialitem, MagicExpansionItems.CARGO_FRAGMENT, RecipeType.NULL, new ItemStack[] {
                null,null,null,
                null,new CustomItemStack(new ItemStack(Material.NOTE_BLOCK,1), getGradientName("Magic Storage Terminal"), getGradientName("Special drop item")),null,
                null,null,null
        }).register(plugin);



        //抽奖机
        new DrawMachine(magicexpansionenergy, MagicExpansionItems.DRAW_MACHINE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                ELEMENT_INGOT, new ItemStack(Material.DISPENSER), ELEMENT_INGOT,
                REDSTONE, new ItemStack(Material.CAULDRON), REDSTONE,
                ELEMENT_INGOT, REDSTONE, ELEMENT_INGOT
        }).register(plugin);
        //抽奖机
        new MagicWand(magicexpansionspecialitem, MAGIC_WAND, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MAGIC_EXPANSION_MAGIC_SUGAR_2, new ItemStack(Material.BLAZE_ROD), MAGIC_EXPANSION_MAGIC_SUGAR_2,
                MAGIC_EXPANSION_MAGIC_SUGAR_2, new ItemStack(Material.BLAZE_ROD), MAGIC_EXPANSION_MAGIC_SUGAR_2,
                MAGIC_EXPANSION_MAGIC_SUGAR_2, new ItemStack(Material.BLAZE_ROD), MAGIC_EXPANSION_MAGIC_SUGAR_2
        }).register(plugin);













        //GEO资源挖掘机,要放在GEO资源下面

        new GEOMiner(magicexpansionenergy, MagicExpansionItems.GEO_MINER_PLUS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT, SlimefunItems.GEO_MINER, MagicExpansionItems.ELEMENT_INGOT,
                SlimefunItems.GEO_MINER, AMETHYST_SHARD, SlimefunItems.GEO_MINER,
                SlimefunItems.GPS_TRANSMITTER_4, SlimefunItems.GEO_MINER, SlimefunItems.GPS_TRANSMITTER_4
        })
                .setCapacity(1314)
                .setEnergyConsumption(260)
                .setProcessingSpeed(1314)
                .register(plugin);


        new FiveElementsMiner(magicexpansionenergy, MagicExpansionItems.FIVE_ELEMENT_MINER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                MagicExpansionItems.ELEMENT_INGOT, SlimefunItems.PORTABLE_GEO_SCANNER, MagicExpansionItems.ELEMENT_INGOT,
                SlimefunItems.GPS_TELEPORTER_PYLON, ELEMENT_INGOT, SlimefunItems.GPS_TELEPORTER_PYLON,
                MagicExpansionItems.AMETHYST_SHARD, SlimefunItems.GEO_MINER, MagicExpansionItems.NETHERITE_INGOT
        })
                .setCapacity(1314)
                .setEnergyConsumption(260)
                .setProcessingSpeed(1)
                .register(plugin);

        //条件注册,1.21魔法兼容问题
            new RSCMagicMiner(magicexpansionrscmagic, MagicExpansionItems.RSC_MAGIC_MINER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[]{
                    null,SlimefunItems.GEO_MINER, null,
                    null, SlimefunItems.OUTPUT_CHEST, null,
                    new ItemStack(Material.REDSTONE), REDSTONE, new ItemStack(Material.REDSTONE)
            })
                    .setCapacity(1314)
                    .setEnergyConsumption(260)
                    .setProcessingSpeed(1)
                    .register(plugin);



    }




    private static void registerUIBlockInContribution(SlimefunItemStack itemStack, MagicExpansion plugin) {
        new UnplaceableBlock(
                magicexpansioncontribution, // 假设这是某个已定义的贡献对象
                itemStack,
                RecipeType.NULL,
                new ItemStack[] {
                        null, null, null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
    }











    //粘液物品
    private static void  registerBasicElectricMan(
            MagicExpansion plugin,
            SlimefunItemStack item,
            SlimefunItemStack angle,
            SlimefunItemStack output
    ){


        //电力矿工-输出
        new ResourceMachine(
                magicexpansionelectricbot,
                item,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        angle,SlimefunItems.PROGRAMMABLE_ANDROID_3,angle,
                        SlimefunItems.PROGRAMMABLE_ANDROID_MINER,MagicExpansionItems.MAGIC_EXPANSION_MINERAL_CAVE,SlimefunItems.PROGRAMMABLE_ANDROID_MINER,
                        MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT
        })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {sfItemAmount(output,8),new ItemStack(Material.COBBLESTONE,6)
                ,new ItemStack(Material.FLINT,2),new ItemStack(Material.BONE),new ItemStack(Material.CLAY,2)})
                .register(plugin);
    }
    //原版物品
    private static void  registerBasicElectricMan(
            MagicExpansion plugin,
            SlimefunItemStack item,
            SlimefunItemStack angle,
            Material output
    ){


        //电力矿工-输出
        new ResourceMachine(
                magicexpansionelectricbot,
                item,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        angle,SlimefunItems.PROGRAMMABLE_ANDROID_3,angle,
                        SlimefunItems.PROGRAMMABLE_ANDROID_MINER,MagicExpansionItems.MAGIC_EXPANSION_MINERAL_CAVE,SlimefunItems.PROGRAMMABLE_ANDROID_MINER,
                        MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT
                })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(output,6),new ItemStack(Material.COBBLESTONE,6)
                        ,new ItemStack(Material.FLINT,2),new ItemStack(Material.BONE),new ItemStack(Material.CLAY,2)})
                .register(plugin);
    }

    //矿锭-粘液
    private static void  registerBasicElectricMan(
            MagicExpansion plugin,
            SlimefunItemStack item,
            SlimefunItemStack angle,
            SlimefunItemStack output,
            int none,
            int ingotsf
    ){


        //电力矿工-输出
        new ResourceMachine(
                magicexpansionelectricbot,
                item,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        angle,MagicExpansionItems.INFINITY_FLINT_AND_STEEL,null,
                        null,null,null,
                        null,null,null
                })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {sfItemAmount(output,8),new ItemStack(Material.COBBLESTONE,6)
                        ,new ItemStack(Material.FLINT,2),new ItemStack(Material.BONE),new ItemStack(Material.CLAY,2)})
                .register(plugin);
    }
    //矿锭-原版
    private static void  registerBasicElectricMan(
            MagicExpansion plugin,
            SlimefunItemStack item,
            SlimefunItemStack angle,
            Material output,
            int none,
            int none1,
            int ingotVanilla
    ){


        //电力矿工-输出
        new ResourceMachine(
                magicexpansionelectricbot,
                item,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        angle,MagicExpansionItems.INFINITY_FLINT_AND_STEEL,null,
                        null,null,null,
                        null,null,null
                })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(output,6),new ItemStack(Material.COBBLESTONE,6)
                        ,new ItemStack(Material.FLINT,2),new ItemStack(Material.BONE),new ItemStack(Material.CLAY,2)})
                .register(plugin);
    }

    //下届-原版
    private static void  registerBasicElectricMan(
            MagicExpansion plugin,
            SlimefunItemStack item,
            SlimefunItemStack angle,
            Material output,
            int nether
    ){


        //电力矿工-输出
        new ResourceMachine(
                magicexpansionelectricbot,
                item,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        angle,SlimefunItems.PROGRAMMABLE_ANDROID_3,angle,
                        SlimefunItems.PROGRAMMABLE_ANDROID_MINER,MagicExpansionItems.MAGIC_EXPANSION_MINERAL_CAVE,SlimefunItems.PROGRAMMABLE_ANDROID_MINER,
                        MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT,MagicExpansionItems.ELEMENT_INGOT
                })
                .setCraftSecond(2)
                .setCapacity(1314)
                .setConsumption(260)
                .setProcessingSpeed(1)
                .setItemStackOutputs(new ItemStack[] {new ItemStack(output,6),new ItemStack(Material.NETHERRACK,6)
                        ,new ItemStack(Material.SOUL_SAND,2),new ItemStack(Material.BONE_BLOCK),new ItemStack(Material.GOLD_NUGGET,2)})
                .register(plugin);
    }



    // 注册预制树General
    private static void registerVanResource(
            MagicExpansion plugin,
            SlimefunItemStack item,
            ItemStack miditem) {

        new UnplaceableBlock(
                magicexpansionresource,
                item,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {
                        SlimefunItems.MAGIC_LUMP_1,new ItemStack(Material.BONE_MEAL),SlimefunItems.MAGIC_LUMP_1,
                        new ItemStack(Material.BONE_MEAL),miditem,new ItemStack(Material.BONE_MEAL),
                        SlimefunItems.MAGIC_LUMP_1,new ItemStack(Material.BONE_MEAL),SlimefunItems.MAGIC_LUMP_1
                }).register(plugin);
    }

    // 注册预制树General
    private static void registerPreBuildingTree(
            MagicExpansion plugin,
            SlimefunItemStack treeItem,
            String baseTreeKey,
            String baseTreeType,
            String treeType,
            long timeCd,
            ItemStack tree) {

        new PreBuildingTree(
                magicexpansionprebuilding,
                treeItem,
                PRE_BUILDINGS_MACHINE,
                new ItemStack[] {
                        MagicExpansionItems.BONE_MEAL, tree, null,
                        null, null, null,
                        null, null, null
                },
                baseTreeKey,
                baseTreeType,
                treeType.toUpperCase(),
                timeCd
        ).register(plugin);
    }


    private static void registerBuildingsResource(
            MagicExpansion plugin,
            SlimefunItemStack item,
            Material material
    ){
        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                item,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        new ItemStack(material,64), null, null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
    }
    private static void registerBuildingsResourceSf(
            MagicExpansion plugin,
            SlimefunItemStack item,
            SlimefunItemStack itemInput
    ){
        new UnplaceableBlock(
                magicexpansionprebuildingresource,
                item,
                PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED,
                new ItemStack[] {
                        new SlimefunItemStack(itemInput,64), null, null,
                        null, null, null,
                        null, null, null
                }
        ).register(plugin);
    }



    private static void registerHiddenRecipe(MagicExpansion plugin, ItemGroup itemGroup, SlimefunItemStack item) {
        Material type = item.getType();

        ItemStack[] recipe = {
                new ItemStack(type), new ItemStack(type), new ItemStack(type),
                new ItemStack(type), sfItemAmount(SlimefunItems.ENERGY_CONNECTOR, 1), new ItemStack(type),
                new ItemStack(type), new ItemStack(type), new ItemStack(type)
        };

        new EnergyConnectorHidden(itemGroup, item, RecipeType.ENHANCED_CRAFTING_TABLE, recipe, sfItemAmount(item, 8))
                .register(plugin);
    }


}
