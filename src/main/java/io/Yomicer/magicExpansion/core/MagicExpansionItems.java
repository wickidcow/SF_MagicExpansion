package io.Yomicer.magicExpansion.core;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.Yomicer.magicExpansion.utils.itemUtils.MagicStringBuilder;
import io.Yomicer.magicExpansion.utils.itemUtils.MagicSugarBuilder;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;
import static io.Yomicer.magicExpansion.utils.Utils.doGlowDisplayEnchant;
import static io.Yomicer.magicExpansion.utils.Language.get;
import static io.Yomicer.magicExpansion.utils.Language.getList;
import static io.Yomicer.magicExpansion.utils.itemUtils.newItem.*;


public class MagicExpansionItems {

    static Config cfg = new Config(MagicExpansion.getInstance());


    private MagicExpansionItems(){
    }
    //INFO
    public static final SlimefunItemStack MAGIC_EXPANSION_INFO = new SlimefunItemStack(
            "MAGIC_EXPANSION_INFO",
            Material.PAPER,
            getGradientNameVer2("Information"),
            "",
            getGradientNameVer2("Version: Build 83"),
            getGradientNameVer2("Include this information when reporting an issue.")
    );
    //AUTHOR
    public static final SlimefunItemStack MAGIC_EXPANSION_AUTHOR = new SlimefunItemStack(
            "MAGIC_EXPANSION_AUTHOR_MAGICSOLO",
            "8adb25ab9976d89d0bd8118d72c1c06bb907060c1e02a729b652d1e86b1ebbbc",
            getGradientNameVer2("Developer: magicsolo"),
            "",
            getGradientNameVer2("Github: Yomicer"),
            getGradientNameVer2("Author of MagicExpansion")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_MACHINE_INFO = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_MACHINE_INFO",
            Material.PAPER,
            getGradientName("Quick Machine Information"),
            "",
            getGradientName("Supports nearly all compatible recipes."),
            getGradientName("Some vanilla recipes have been adjusted for balance."),
            getGradientName("Adjusted recipes appear as machine easter eggs.")
    );


    //材料
    public static final SlimefunItemStack MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC = new SlimefunItemStack(
            "MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC",
            Material.BARRIER,
            getGradientName("Basic Universal Magic Material"),
            "",
            getGradientName("A compatibility material used when another Magic addon is not installed.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED = new SlimefunItemStack(
            "MAGIC_EXPANSION_TO_MAGIC_ITEM_ADVANCED",
            Material.STRUCTURE_VOID,
            getGradientName("Advanced Universal Magic Material"),
            "",
            getGradientName("A compatibility material used when another Magic addon is not installed.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_INTERACTIVE_CORE = new SlimefunItemStack(
            "MAGIC_EXPANSION_INTERACTIVE_CORE",
            Material.CHAIN_COMMAND_BLOCK,
            getGradientName("Magic Interaction Core"),
            "",
            getGradientName("Handles interactions for Quick Machines.")
    );


    // 工具
    public static final SlimefunItemStack SCYTHE = new SlimefunItemStack(
            "MAGIC_EXPANSION_SCYTHE",
            "8adb25ab9976d89d0bd8118d72c1c06bb907060c1e02a729b652d1e86b1ebbbc",
            getGradientName("Improved Scythe"),
            "",
            "&7Test Skull_Hash",
            "&7Harvests up to five mature crops at once.",
            "&7Improved from the FluffyMachines Scythe.",
            "&7No longer chain-breaks immature crops."
    );

    //虚空之触
    public static final SlimefunItemStack VOID_TOUCH = themedVer2Vertical("VOID_TOUCH",doGlow(Material.WEEPING_VINES),
            get("Items.VOID_TOUCH.Name"),getList("Items.VOID_TOUCH.Lore"));
    //虚空之触-脚本专用
    public static final SlimefunItemStack VOID_TOUCH_SCRIPT = themedVer2Vertical("VOID_TOUCH_SCRIPT",doGlow(Material.WEEPING_VINES),
            get("Items.VOID_TOUCH_SCRIPT.Name"),getList("Items.VOID_TOUCH_SCRIPT.Lore"));
    //虚空之触
    public static final SlimefunItemStack FIVE_ELEMENT_TOUCH = themedVer2Vertical("FIVE_ELEMENT_TOUCH",doGlow(Material.TWISTING_VINES),
            get("Items.FIVE_ELEMENT_TOUCH.Name"),getList("Items.FIVE_ELEMENT_TOUCH.Lore"));
    //虚空之触
    public static final SlimefunItemStack FIVE_ELEMENT_TOUCH_RIGHT_CLICK = themedVer2Vertical("FIVE_ELEMENT_TOUCH_RIGHT_CLICK",doGlow(Material.VINE),
            get("Items.FIVE_ELEMENT_TOUCH_RIGHT_CLICK.Name"),getList("Items.FIVE_ELEMENT_TOUCH_RIGHT_CLICK.Lore"));

    public static final SlimefunItemStack MAGIC_EXPANSION_RANDOM_SPAWNER = new SlimefunItemStack(
            "MAGIC_EXPANSION_RANDOM_SPAWNER",
            doGlow(Material.ENDER_PEARL),
            getGradientName("Random Entity Egg"),
            "",
            getGradientName("Right-click to summon a random entity.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_MINERAL_CAVE = new SlimefunItemStack(
            "MAGIC_EXPANSION_MINERAL_CAVE",
            doGlow(Material.SMOOTH_STONE),
            getGradientName("Magic Mineral Cave"),
            "",
            getGradientName("Right-click to open the Magic Mineral Cave."),
            getGradientName("Click a stone slot to mine it.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_ENHANCED_CRAFTING_TABLE = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_ENHANCED_CRAFTING_TABLE",
            doGlow(Material.CRAFTING_TABLE),
            getGradientName("Magic Enhanced Crafting Table"),
            "",
            getGradientName("Right-click to open the Magic Workbench."),
            getGradientName("Crafts supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_SMELTERY = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_SMELTERY",
            doGlow(Material.BLAST_FURNACE),
            getGradientName("Magic Smeltery"),
            "",
            getGradientName("Right-click to open the Magic Smeltery."),
            getGradientName("Smelts supported recipes directly from materials in your inventory.")
    );
    /*
    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_SMELTERY2 = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_SMELTERY2",
            doGlow(Material.FURNACE),
            getGradientName("Magic Smeltery - Alternate Recipes"),
            "",
            getGradientName("Right-click to open the Magic Smeltery."),
            getGradientName("Smelts supported recipes directly from materials in your inventory."),
            getGradientName("Right-click an output to view the materials required for one craft."),
            getGradientName("Left-click to smelt once."),
            getGradientName("Shift-right-click an output to smelt 32 batches."),
            getGradientName("Shift-left-click an output to smelt every available batch.")
    );
    */

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_GRIND_STONE = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_GRIND_STONE",
            doGlow(Material.DISPENSER),
            getGradientName("Magic Grind Stone"),
            "",
            getGradientName("Right-click to open the Magic Grind Stone."),
            getGradientName("Grinds supported recipes directly from materials in your inventory."),
            getGradientName("Special behavior:"),
            getGradientName("Grinding cobblestone produces stone instead of gravel.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_ORE_CRUSHER = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_ORE_CRUSHER",
            doGlow(Material.IRON_BARS),
            getGradientName("Magic Ore Crusher"),
            "",
            getGradientName("Right-click to open the Magic Ore Crusher."),
            getGradientName("Crushes supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_ARMOR_FORGE = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_ARMOR_FORGE",
            doGlow(Material.ANVIL),
            getGradientName("Magic Armor Forge"),
            "",
            getGradientName("Right-click to open the Magic Armor Forge."),
            getGradientName("Forges supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_COMPRESSOR = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_COMPRESSOR",
            doGlow(Material.PISTON),
            getGradientName("Magic Compressor"),
            "",
            getGradientName("Right-click to open the Magic Compressor."),
            getGradientName("Compresses supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_PRESSURE_CHAMBER = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_PRESSURE_CHAMBER",
            doGlow(Material.GLASS),
            getGradientName("Magic Pressure Chamber"),
            "",
            getGradientName("Right-click to open the Magic Pressure Chamber."),
            getGradientName("Crafts supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_MAGIC_WORKBENCH = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_MAGIC_WORKBENCH",
            doGlow(Material.BOOKSHELF),
            getGradientName("Magic Workbench"),
            "",
            getGradientName("Right-click to open the Magic Workbench."),
            getGradientName("Crafts supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_AUTOMATED_PANNING_MACHINE = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_AUTOMATED_PANNING_MACHINE",
            doGlow(Material.BOWL),
            getGradientName("Magic Automated Panning Machine"),
            "",
            getGradientName("Right-click to open the Magic Automated Panning Machine."),
            getGradientName("Processes panning recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_AUTOMATED_ANCIENT_ALTAR = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_AUTOMATED_ANCIENT_ALTAR",
            doGlow(Material.ENCHANTING_TABLE),
            getGradientName("Magic Altar"),
            "",
            getGradientName("Right-click to open the Magic Altar."),
            getGradientName("Crafts supported recipes directly from materials in your inventory.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_ELECTRIC_ORE_GRINDER = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_ELECTRIC_ORE_GRINDER",
            doGlow(Material.OBSERVER),
            getGradientName("Damaged Magic Ore Grinder"),
            "",
            getGradientName("Right-click to open the Magic Ore Crusher."),
            getGradientName("Grinds supported recipes directly from materials in your inventory."),
            getGradientName("Supports iron ore, gold ore, netherrack, sifted ore, crushed ore, pure ore clusters, and coal.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_QUICK_HEATED_PRESSURE_CHAMBER = new SlimefunItemStack(
            "MAGIC_EXPANSION_QUICK_HEATED_PRESSURE_CHAMBER",
            doGlow(Material.LIGHT_GRAY_STAINED_GLASS),
            getGradientName("Magic Heated Pressure Chamber"),
            "",
            getGradientName("Right-click to open the Magic Heated Pressure Chamber."),
            getGradientName("Crafts supported recipes directly from materials in your inventory.")
    );


    public static final SlimefunItemStack QUICK_MACHINE_BV_INFO = themedVer2Vertical("QUICK_MACHINE_BV_INFO",doGlow(Material.PAPER),
            get("Items.QUICK_MACHINE_BV_INFO.Name"),getList("Items.QUICK_MACHINE_BV_INFO.Lore"));

    public static final SlimefunItemStack QUICK_ENHANCED_CRAFTING_TABLE_BV = themedVer2Vertical("QUICK_ENHANCED_CRAFTING_TABLE_BV",doGlow(Material.CRAFTING_TABLE),
            get("Items.QUICK_ENHANCED_CRAFTING_TABLE_BV.Name"),getList("Items.QUICK_ENHANCED_CRAFTING_TABLE_BV.Lore"));

    public static final SlimefunItemStack QUICK_SMELTERY_BV = themedVer2Vertical("QUICK_SMELTERY_BV",doGlow(Material.BLAST_FURNACE),
            get("Items.QUICK_SMELTERY_BV.Name"),getList("Items.QUICK_SMELTERY_BV.Lore"));

    public static final SlimefunItemStack QUICK_GRIND_STONE_BV = themedVer2Vertical("QUICK_GRIND_STONE_BV",doGlow(Material.DISPENSER),
            get("Items.QUICK_GRIND_STONE_BV.Name"),getList("Items.QUICK_GRIND_STONE_BV.Lore"));

    public static final SlimefunItemStack QUICK_ORE_CRUSHER_BV = themedVer2Vertical("QUICK_ORE_CRUSHER_BV",doGlow(Material.IRON_BARS),
            get("Items.QUICK_ORE_CRUSHER_BV.Name"),getList("Items.QUICK_ORE_CRUSHER_BV.Lore"));

    public static final SlimefunItemStack QUICK_ARMOR_FORGE_BV = themedVer2Vertical("QUICK_ARMOR_FORGE_BV",doGlow(Material.ANVIL),
            get("Items.QUICK_ARMOR_FORGE_BV.Name"),getList("Items.QUICK_ARMOR_FORGE_BV.Lore"));

    public static final SlimefunItemStack QUICK_COMPRESSOR_BV = themedVer2Vertical("QUICK_COMPRESSOR_BV",doGlow(Material.PISTON),
            get("Items.QUICK_COMPRESSOR_BV.Name"),getList("Items.QUICK_COMPRESSOR_BV.Lore"));

    public static final SlimefunItemStack QUICK_PRESSURE_CHAMBER_BV = themedVer2Vertical("QUICK_PRESSURE_CHAMBER_BV",doGlow(Material.GLASS),
            get("Items.QUICK_PRESSURE_CHAMBER_BV.Name"),getList("Items.QUICK_PRESSURE_CHAMBER_BV.Lore"));

    public static final SlimefunItemStack QUICK_MAGIC_WORKBENCH_BV = themedVer2Vertical("QUICK_MAGIC_WORKBENCH_BV",doGlow(Material.BOOKSHELF),
            get("Items.QUICK_MAGIC_WORKBENCH_BV.Name"),getList("Items.QUICK_MAGIC_WORKBENCH_BV.Lore"));

    public static final SlimefunItemStack QUICK_AUTOMATED_PANNING_MACHINE_BV = themedVer2Vertical("QUICK_AUTOMATED_PANNING_MACHINE_BV",doGlow(Material.GOLD_BLOCK),
            get("Items.QUICK_AUTOMATED_PANNING_MACHINE_BV.Name"),getList("Items.QUICK_AUTOMATED_PANNING_MACHINE_BV.Lore"));

    public static final SlimefunItemStack QUICK_AUTOMATED_ANCIENT_ALTAR_BV = themedVer2Vertical("QUICK_AUTOMATED_ANCIENT_ALTAR_BV",doGlow(Material.ENCHANTING_TABLE),
            get("Items.QUICK_AUTOMATED_ANCIENT_ALTAR_BV.Name"),getList("Items.QUICK_AUTOMATED_ANCIENT_ALTAR_BV.Lore"));

    public static final SlimefunItemStack QUICK_ELECTRIC_ORE_GRINDER_BV = themedVer2Vertical("QUICK_ELECTRIC_ORE_GRINDER_BV",doGlow(Material.OBSERVER),
            get("Items.QUICK_ELECTRIC_ORE_GRINDER_BV.Name"),getList("Items.QUICK_ELECTRIC_ORE_GRINDER_BV.Lore"));

    public static final SlimefunItemStack QUICK_HEATED_PRESSURE_CHAMBER_BV = themedVer2Vertical("QUICK_HEATED_PRESSURE_CHAMBER_BV",doGlow(Material.LIGHT_GRAY_STAINED_GLASS),
            get("Items.QUICK_HEATED_PRESSURE_CHAMBER_BV.Name"),getList("Items.QUICK_HEATED_PRESSURE_CHAMBER_BV.Lore"));




    //元旦烟花
    public static final SlimefunItemStack NEW_YEARS_DAY_FIREWORK_YUAN_DAN = themedOrigin("NEW_YEARS_DAY_FIREWORK_YUAN_DAN",doGlowDisplayEnchant(Material.FIREWORK_ROCKET),
            get("Items.NEW_YEARS_DAY_FIREWORK_YUAN_DAN.Name"),getList("Items.NEW_YEARS_DAY_FIREWORK_YUAN_DAN.Lore"));
    public static final SlimefunItemStack NEW_YEARS_DAY_FIREWORK_YUAN_DAN_2 = themedOrigin("NEW_YEARS_DAY_FIREWORK_YUAN_DAN_2",doGlowDisplayEnchant(Material.FIREWORK_ROCKET),
            get("Items.NEW_YEARS_DAY_FIREWORK_YUAN_DAN_2.Name"),getList("Items.NEW_YEARS_DAY_FIREWORK_YUAN_DAN_2.Lore"));



    //武器类↓↓↓↓↓



//    public static final SlimefunItemStack WEAPON_STAR_SHARDS_SWORD = themedOrigin("WEAPON_STAR_SHARDS_SWORD",doGlowDisplayEnchant(Material.NETHERITE_SWORD),
//            get("Items.WEAPON_STAR_SHARDS_SWORD.Name"),getList("Items.WEAPON_STAR_SHARDS_SWORD.Lore"));


    static Long StarShards_BlazingSlash_CD = cfg.getLong("StarShardsSword.StarShards_BlazingSlash_CD");
    static Long StarShards_ArcaneBlast_CD = cfg.getLong("StarShardsSword.StarShards_ArcaneBlast_CD");
    static Long StarShards_AstralShield_CD = cfg.getLong("StarShardsSword.StarShards_AstralShield_CD");
    static Long StarShards_AstralShield_During = cfg.getLong("StarShardsSword.StarShards_AstralShield_During");
    static Long StarShards_InstantBlink_CD = cfg.getLong("StarShardsSword.StarShards_InstantBlink_CD");
    static Double StarShards_Atk_Mix = cfg.getDouble("StarShardsSword.StarShards_Atk_Mix");
    static Double StarShards_Atk_ExtraPercent = cfg.getDouble("StarShardsSword.StarShards_Atk_ExtraPercent");
    static Double StarShards_Atk_Blood = cfg.getDouble("StarShardsSword.StarShards_Atk_Blood");

    public static final SlimefunItemStack WEAPON_STAR_SHARDS_SWORD = new SlimefunItemStack(
            "MAGIC_EXPANSION_WEAPON_STAR_SHARDS_SWORD",
            Material.NETHERITE_SWORD,
            "§x§E§8§4§2§3§DSt§x§D§A§5§5§6§Bar§x§C§C§6§8§9§9fa§x§B§E§7§B§C§7ll Judgment",
            "",
            "§x§D§9§4§2§F§5A legendary weapon said to cut stars from the sky,",
            "§x§A§3§5§7§B§9the void trembles wherever its blade points.",
            "§x§6§A§8§C§E§FIt holds primal arcane power and celestial flame,",
            "§x§F§2§5§4§5§9And only one chosen by fate can awaken its true name.",
            "",
            "§x§A§3§5§7§B§9Rarity: §cMythical",
            "§x§A§3§5§7§B§9Damage Multiplier: §c" + StarShards_Atk_Mix + "x",
            "§x§A§3§5§7§B§9*Execution* §e Deals extra damage equal to " + StarShards_Atk_ExtraPercent*100 + "% of maximum health.",
            "§x§A§3§5§7§B§9*Bleed* §4 Health loss rate: " + StarShards_Atk_Blood*100 + "% (lasts 8 seconds and can stack)",
            "§x§A§3§5§7§B§9*Mercy* §b Attacks usually leave players or creatures with 0.1 health.",
            "",
            "§f• Left-click§7: Blazing Slash (Cooldown: "+StarShards_BlazingSlash_CD+" seconds)",
            "§7Launches a burning blade wave that deals extra fire damage.",
            "§7Creates a small explosion on hit and knocks back nearby enemies.",
            "",
            "§f• Sneak + Left-click§7: Arcane Blast (Cooldown: "+StarShards_ArcaneBlast_CD+" seconds)",
            "§7Charges and releases a cone-shaped arcane pulse through the area ahead.",
            "§7Applies Weakness and Slowness for 4 seconds.",
            "",
            "§f• Right-click§7: Astral Shield (Cooldown: "+StarShards_AstralShield_CD+" seconds)",
            "§7Creates a magical barrier for "+StarShards_AstralShield_During+" seconds, granting immunity to all damage.",
            "§7Ranged attacks received during the shield are reflected toward their source.",
            "",
            "§f• Sneak + Right-click§7: Shadow Blink (Cooldown: "+StarShards_InstantBlink_CD+" seconds)",
            "§7Teleports instantly to the targeted ground, up to 15 blocks away.",
            "§7Releases a shockwave on landing that briefly stuns nearby enemies."
    );






    //武器类↑↑↑↑↑




    public static final SlimefunItemStack MAGIC_EXPANSION_ENCHANTING_TABLE = themedVer2Vertical("ENCHANTING_TABLE",doGlow(Material.ENCHANTING_TABLE),
            get("Items.ENCHANTING_TABLE.Name"),getList("Items.ENCHANTING_TABLE.Lore"));




    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_1 = MagicSugarBuilder.getMagicSugar(1);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_2 = MagicSugarBuilder.getMagicSugar(2);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_3 = MagicSugarBuilder.getMagicSugar(3);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_4 = MagicSugarBuilder.getMagicSugar(4);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_5 = MagicSugarBuilder.getMagicSugar(5);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_6 = MagicSugarBuilder.getMagicSugar(6);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_7 = MagicSugarBuilder.getMagicSugar(7);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_8 = MagicSugarBuilder.getMagicSugar(8);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_9 = MagicSugarBuilder.getMagicSugar(9);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_10 = MagicSugarBuilder.getMagicSugar(10);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_11 = MagicSugarBuilder.getMagicSugar(11);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_12 = MagicSugarBuilder.getMagicSugar(12);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_13 = MagicSugarBuilder.getMagicSugar(13);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_14 = MagicSugarBuilder.getMagicSugar(14);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_15 = MagicSugarBuilder.getMagicSugar(15);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_16 = MagicSugarBuilder.getMagicSugar(16);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_17 = MagicSugarBuilder.getMagicSugar(17);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_18 = MagicSugarBuilder.getMagicSugar(18);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_19 = MagicSugarBuilder.getMagicSugar(19);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_20 = MagicSugarBuilder.getMagicSugar(20);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_21 = MagicSugarBuilder.getMagicSugar(21);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_22 = MagicSugarBuilder.getMagicSugar(22);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_23 = MagicSugarBuilder.getMagicSugar(23);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_24 = MagicSugarBuilder.getMagicSugar(24);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_25 = MagicSugarBuilder.getMagicSugar(25);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_26 = MagicSugarBuilder.getMagicSugar(26);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_27 = MagicSugarBuilder.getMagicSugar(27);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_28 = MagicSugarBuilder.getMagicSugar(28);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_29 = MagicSugarBuilder.getMagicSugar(29);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_30 = MagicSugarBuilder.getMagicSugar(30);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_31 = MagicSugarBuilder.getMagicSugar(31);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_32 = MagicSugarBuilder.getMagicSugar(32);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_33 = MagicSugarBuilder.getMagicSugar(33);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_34 = MagicSugarBuilder.getMagicSugar(34);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_35 = MagicSugarBuilder.getMagicSugar(35);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_36 = MagicSugarBuilder.getMagicSugar(36);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_37 = MagicSugarBuilder.getMagicSugar(37);
    public static final SlimefunItemStack MAGIC_EXPANSION_MAGIC_SUGAR_CANE = new SlimefunItemStack(
            "MAGIC_EXPANSION_MAGIC_SUGAR_CANE",
            doGlow(Material.SUGAR_CANE),
            getGradientName("Quantum-Entangled Sugar Cane"),
            "",
            getGradientName("MagicExpansion's first endgame item."),
            getGradientName("The first true endgame item.")
    );

    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_1 = MagicStringBuilder.getString(1);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_2 = MagicStringBuilder.getString(2);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_3 = MagicStringBuilder.getString(3);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_4 = MagicStringBuilder.getString(4);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_5 = MagicStringBuilder.getString(5);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_6 = MagicStringBuilder.getString(6);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_7 = MagicStringBuilder.getString(7);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_8 = MagicStringBuilder.getString(8);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_9 = MagicStringBuilder.getString(9);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_10 = MagicStringBuilder.getString(10);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_11 = MagicStringBuilder.getString(11);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_12 = MagicStringBuilder.getString(12);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_13 = MagicStringBuilder.getString(13);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_14 = MagicStringBuilder.getString(14);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_15 = MagicStringBuilder.getString(15);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_16 = MagicStringBuilder.getString(16);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_17 = MagicStringBuilder.getString(17);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_18 = MagicStringBuilder.getString(18);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_19 = MagicStringBuilder.getString(19);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_20 = MagicStringBuilder.getString(20);
    public static final SlimefunItemStack MAGIC_EXPANSION_FINAL_STRING_21 = MagicStringBuilder.getString(21);








    public static final SlimefunItemStack FIRE_ZOMBIE = new SlimefunItemStack(
            "MAGIC_EXPANSION_FIRE_ZOMBIE",
            doGlow(Material.ZOMBIE_SPAWN_EGG),
            getGradientName("Flame Zombie"),
            "",
            getGradientName("Right-click to summon the Flame Zombie.")
    );

    public static final SlimefunItemStack FIRE_ZOMBIE_MB = new SlimefunItemStack(
            "MAGIC_EXPANSION_FIRE_ZOMBIE_MB",
            doGlow(CustomHead.getHead("b15f3517f28740470e8f51b4d755be159ee65861c05e2f744fb61bfdccd5c0e1")),
            getGradientName("Flame Zombie [Summon]"),
            "",
            getGradientName("Right-click the center block to summon the Flame Zombie.")
    );

    public static final SlimefunItemStack WIND_ELF_SPAWN = themedVer2Vertical("WIND_ELF_SPAWN",doGlow(Material.ALLAY_SPAWN_EGG),
            get("Items.WIND_ELF_SPAWN.Name"),getList("Items.WIND_ELF_SPAWN.Lore"));

    public static final SlimefunItemStack WIND_ELF_MB = new SlimefunItemStack(
            "MAGIC_EXPANSION_WIND_ELF_MB",
            doGlow(CustomHead.getHead("892fd59703cdfe7db5ea0b35b6792308b35fc37368760aa997726ae12e8bd696")),
            getGradientName("Wind Spirit [Summon]"),
            "",
            getGradientName("Right-click the center block to summon the Wind Spirit.")
    );



    public static final SlimefunItemStack BASIC_ENCHANT_STONE = themedVer2Vertical("BASIC_ENCHANT_STONE",doGlow(Material.IRON_INGOT),
            get("Items.BASIC_ENCHANT_STONE.Name"),getList("Items.BASIC_ENCHANT_STONE.Lore"));

    public static final SlimefunItemStack WIND_SPIRIT = themedVer2Vertical("WIND_SPIRIT",doGlow(Material.STRING),
            get("Items.WIND_SPIRIT.Name"),getList("Items.WIND_SPIRIT.Lore"));

    public static final SlimefunItemStack MAGIC_EXPANSION_ENCHANTING_TABLE_INFO = themedVer2Vertical("ENCHANTING_TABLE_INFO",doGlow(Material.PAPER),
            get("Items.ENCHANTING_TABLE_INFO.Name"),getList("Items.ENCHANTING_TABLE_INFO.Lore"));

    public static final SlimefunItemStack MAGIC_EXPANSION_ENCHANTING_TABLE_LIST = themedVer2Vertical("ENCHANTING_TABLE_LIST",doGlow(Material.PAPER),
            get("Items.ENCHANTING_TABLE_LIST.Name"),getList("Items.ENCHANTING_TABLE_LIST.Lore"));

    public static final SlimefunItemStack FIREZOMBIE_HEAD = themedVer2Vertical("FIREZOMBIE_HEAD", CustomHead.getHead("b15f3517f28740470e8f51b4d755be159ee65861c05e2f744fb61bfdccd5c0e1"),
            get("Items.FIREZOMBIE_HEAD.Name"),getList("Items.FIREZOMBIE_HEAD.Lore"));

    public static final SlimefunItemStack FIREZOMBIE_BODY = themedVer2Vertical("FIREZOMBIE_BODY",Material.MOSS_BLOCK,
            get("Items.FIREZOMBIE_BODY.Name"),getList("Items.FIREZOMBIE_BODY.Lore"));

    public static final SlimefunItemStack WIND_ELF_HEAD = themedVer2Vertical("WIND_ELF_HEAD", CustomHead.getHead("892fd59703cdfe7db5ea0b35b6792308b35fc37368760aa997726ae12e8bd696"),
            get("Items.WIND_ELF_HEAD.Name"),getList("Items.WIND_ELF_HEAD.Lore"));

    public static final SlimefunItemStack WIND_ELF_BODY = themedVer2Vertical("WIND_ELF_BODY",Material.LIGHT_BLUE_CONCRETE,
            get("Items.WIND_ELF_BODY.Name"),getList("Items.WIND_ELF_BODY.Lore"));

    public static final SlimefunItemStack WIND_ELF_SKILL = themedVer2Vertical("WIND_ELF_SKILL",Material.PAPER,
            get("Items.WIND_ELF_SKILL.Name"),getList("Items.WIND_ELF_SKILL.Lore"));
    public static final SlimefunItemStack WIND_ELF_DEFENSE = themedVer2Vertical("WIND_ELF_DEFENSE",Material.PAPER,
            get("Items.WIND_ELF_DEFENSE.Name"),getList("Items.WIND_ELF_DEFENSE.Lore"));




    public static final SlimefunItemStack PRE_BUILDING_TREE_INFO = themedVer2Vertical("PRE_BUILDING_TREE_INFO",Material.PAPER,
            get("Items.PRE_BUILDING_TREE_INFO.Name"),getList("Items.PRE_BUILDING_TREE_INFO.Lore"));

    public static SlimefunItemStack createPreBuildingTreeItem(String treeType, Material saplingMaterial) {
        return themedVer2Vertical(
                "PRE_BUILDING_" + treeType.toUpperCase() + "_TREE",
                saplingMaterial,
                get("Items.PRE_BUILDING_" + treeType.toUpperCase() + "_TREE.Name"),
                getList("Items.PRE_BUILDING_" + treeType.toUpperCase() + "_TREE.Lore")
        );
    }
    public static final SlimefunItemStack PRE_BUILDING_OAK_TREE = createPreBuildingTreeItem("oak", Material.OAK_SAPLING);
    public static final SlimefunItemStack PRE_BUILDING_SPRUCE_TREE = createPreBuildingTreeItem("spruce", Material.SPRUCE_SAPLING);
    public static final SlimefunItemStack PRE_BUILDING_BIRCH_TREE = createPreBuildingTreeItem("birch", Material.BIRCH_SAPLING);
    public static final SlimefunItemStack PRE_BUILDING_DARK_OAK_TREE = createPreBuildingTreeItem("dark_oak", Material.DARK_OAK_SAPLING);
    public static final SlimefunItemStack PRE_BUILDING_ACACIA_TREE = createPreBuildingTreeItem("acacia", Material.ACACIA_SAPLING);
    public static final SlimefunItemStack PRE_BUILDING_JUNGLE_TREE = createPreBuildingTreeItem("jungle", Material.JUNGLE_SAPLING);
    public static final SlimefunItemStack PRE_BUILDING_MANGROVE_TREE = createPreBuildingTreeItem("mangrove", Material.MANGROVE_PROPAGULE);
    public static final SlimefunItemStack PRE_BUILDING_CHERRY_TREE = createPreBuildingTreeItem("cherry", Material.CHERRY_SAPLING);


//    public static final SlimefunItemStack PRE_BUILDING_OAK_TREE = themedVer2Vertical("PRE_BUILDING_OAK_TREE",Material.OAK_SAPLING,
//            get("Items.PRE_BUILDING_OAK_TREE.Name"),getList("Items.PRE_BUILDING_OAK_TREE.Lore"));
//
//    public static final SlimefunItemStack PRE_BUILDING_CHERRY_TREE = themedVer2Vertical("PRE_BUILDING_CHERRY_TREE",Material.CHERRY_SAPLING,
//            get("Items.PRE_BUILDING_CHERRY_TREE.Name"),getList("Items.PRE_BUILDING_CHERRY_TREE.Lore"));

    public static final SlimefunItemStack IRON_INGOT = themedVer2Vertical("IRON_INGOT",Material.WHITE_WOOL,
            get("Resource.IRON_INGOT.Name"),getList("Resource.IRON_INGOT.Lore"));
    public static final SlimefunItemStack GOLD_INGOT = themedVer2Vertical("GOLD_INGOT",Material.YELLOW_WOOL,
            get("Resource.GOLD_INGOT.Name"),getList("Resource.GOLD_INGOT.Lore"));
    public static final SlimefunItemStack COPPER_INGOT = themedVer2Vertical("COPPER_INGOT",Material.ORANGE_WOOL,
            get("Resource.COPPER_INGOT.Name"),getList("Resource.COPPER_INGOT.Lore"));
    public static final SlimefunItemStack TIN_INGOT = themedVer2Vertical("TIN_INGOT",Material.CYAN_WOOL,
            get("Resource.TIN_INGOT.Name"),getList("Resource.TIN_INGOT.Lore"));
    public static final SlimefunItemStack SILVER_INGOT = themedVer2Vertical("SILVER_INGOT",Material.LIGHT_GRAY_WOOL,
            get("Resource.SILVER_INGOT.Name"),getList("Resource.SILVER_INGOT.Lore"));
    public static final SlimefunItemStack LEAD_INGOT = themedVer2Vertical("LEAD_INGOT",Material.BLUE_WOOL,
            get("Resource.LEAD_INGOT.Name"),getList("Resource.LEAD_INGOT.Lore"));
    public static final SlimefunItemStack ALUMINUM_INGOT = themedVer2Vertical("ALUMINUM_INGOT",Material.GRAY_WOOL,
            get("Resource.ALUMINUM_INGOT.Name"),getList("Resource.ALUMINUM_INGOT.Lore"));
    public static final SlimefunItemStack ZINC_INGOT = themedVer2Vertical("ZINC_INGOT",Material.LIME_WOOL,
            get("Resource.ZINC_INGOT.Name"),getList("Resource.ZINC_INGOT.Lore"));
    public static final SlimefunItemStack MAGNESIUM_INGOT = themedVer2Vertical("MAGNESIUM_INGOT",Material.PINK_WOOL,
            get("Resource.MAGNESIUM_INGOT.Name"),getList("Resource.MAGNESIUM_INGOT.Lore"));
    public static final SlimefunItemStack ELEMENT_INGOT = themedVer2Vertical("ELEMENT_INGOT",Material.IRON_INGOT,
            get("Resource.ELEMENT_INGOT.Name"),getList("Resource.ELEMENT_INGOT.Lore"));

    public static final SlimefunItemStack REDSTONE = themedVer2Vertical("REDSTONE",Material.REDSTONE,
            get("Resource.REDSTONE.Name"),getList("Resource.REDSTONE.Lore"));
    public static final SlimefunItemStack DIAMOND = themedVer2Vertical("DIAMOND",Material.DIAMOND,
            get("Resource.DIAMOND.Name"),getList("Resource.DIAMOND.Lore"));
    public static final SlimefunItemStack LAPIS_LAZULI = themedVer2Vertical("LAPIS_LAZULI",Material.LAPIS_LAZULI,
            get("Resource.LAPIS_LAZULI.Name"),getList("Resource.LAPIS_LAZULI.Lore"));
    public static final SlimefunItemStack EMERALD = themedVer2Vertical("EMERALD",Material.EMERALD,
            get("Resource.EMERALD.Name"),getList("Resource.EMERALD.Lore"));
    public static final SlimefunItemStack COAL = themedVer2Vertical("COAL",Material.COAL,
            get("Resource.COAL.Name"),getList("Resource.COAL.Lore"));
    public static final SlimefunItemStack QUARTZ = themedVer2Vertical("QUARTZ",Material.QUARTZ,
            get("Resource.QUARTZ.Name"),getList("Resource.QUARTZ.Lore"));
    public static final SlimefunItemStack AMETHYST_SHARD = themedVer2Vertical("AMETHYST_SHARD",Material.AMETHYST_SHARD,
            get("Resource.AMETHYST_SHARD.Name"),getList("Resource.AMETHYST_SHARD.Lore"));
    public static final SlimefunItemStack NETHERITE_INGOT = themedVer2Vertical("NETHERITE_INGOT",Material.NETHERITE_INGOT,
            get("Resource.NETHERITE_INGOT.Name"),getList("Resource.NETHERITE_INGOT.Lore"));
    public static final SlimefunItemStack BONE = themedVer2Vertical("BONE",Material.BONE,
            get("Resource.BONE.Name"),getList("Resource.BONE.Lore"));
    public static final SlimefunItemStack BONE_MEAL = themedVer2Vertical("BONE_MEAL",Material.BONE_MEAL,
            get("Resource.BONE_MEAL.Name"),getList("Resource.BONE_MEAL.Lore"));
    public static final SlimefunItemStack STICK = themedVer2Vertical("STICK",Material.STICK,
            get("Resource.STICK.Name"),getList("Resource.STICK.Lore"));
    public static final SlimefunItemStack REDSTONE_TORCH = themedVer2Vertical("REDSTONE_TORCH",Material.REDSTONE_TORCH,
            get("Resource.REDSTONE_TORCH.Name"),getList("Resource.REDSTONE_TORCH.Lore"));
    public static final SlimefunItemStack COBBLESTONE = themedVer2Vertical("COBBLESTONE",Material.COBBLESTONE,
            get("Resource.COBBLESTONE.Name"),getList("Resource.COBBLESTONE.Lore"));
    public static final SlimefunItemStack DIRT = themedVer2Vertical("DIRT",Material.DIRT,
            get("Resource.DIRT.Name"),getList("Resource.DIRT.Lore"));
    public static final SlimefunItemStack GLOWSTONE_DUST = themedVer2Vertical("GLOWSTONE_DUST",Material.GLOWSTONE_DUST,
            get("Resource.GLOWSTONE_DUST.Name"),getList("Resource.GLOWSTONE_DUST.Lore"));
    public static final SlimefunItemStack LEVER = themedVer2Vertical("LEVER",Material.LEVER,
            get("Resource.LEVER.Name"),getList("Resource.LEVER.Lore"));
    public static final SlimefunItemStack BUTTON = themedVer2Vertical("BUTTON",Material.STONE_BUTTON,
            get("Resource.BUTTON.Name"),getList("Resource.BUTTON.Lore"));
    public static final SlimefunItemStack OAK_PLANKS = themedVer2Vertical("OAK_PLANKS",Material.OAK_PLANKS,
            get("Resource.OAK_PLANKS.Name"),getList("Resource.OAK_PLANKS.Lore"));
    public static final SlimefunItemStack REDSTONE_EXECUTE_ELEMENT = themedVer2Vertical("REDSTONE_EXECUTE_ELEMENT",Material.NOTE_BLOCK,
            get("Resource.REDSTONE_EXECUTE_ELEMENT.Name"),getList("Resource.REDSTONE_EXECUTE_ELEMENT.Lore"));
    public static final SlimefunItemStack CORE_ORIGIN = themedVer2Vertical("CORE_ORIGIN",Material.HEART_OF_THE_SEA,
            get("Resource.CORE_ORIGIN.Name"),getList("Resource.CORE_ORIGIN.Lore"));
    public static final SlimefunItemStack LOG_MIX = themedVer2Vertical("LOG_MIX",Material.GREEN_GLAZED_TERRACOTTA,
            get("Resource.LOG_MIX.Name"),getList("Resource.LOG_MIX.Lore"));
    public static final SlimefunItemStack LIGHT_CORE = themedVer2Vertical("LIGHT_CORE",Material.LIGHT,
            get("Resource.LIGHT_CORE.Name"),getList("Resource.LIGHT_CORE.Lore"));
    public static final SlimefunItemStack LIGHT_ENERGY_ALPHA = themedVer2Vertical("LIGHT_ENERGY_ALPHA",Material.LIGHT,
            get("Resource.LIGHT_ENERGY_ALPHA.Name"),getList("Resource.LIGHT_ENERGY_ALPHA.Lore"));
    public static final SlimefunItemStack QUARTZ_CORE = themedVer2Vertical("QUARTZ_CORE",Material.QUARTZ_BLOCK,
            get("Resource.QUARTZ_CORE.Name"),getList("Resource.QUARTZ_CORE.Lore"));
    public static final SlimefunItemStack INFINITY_FLINT_AND_STEEL = themedVer2Vertical("INFINITY_FLINT_AND_STEEL",Material.FLINT_AND_STEEL,
            get("Resource.INFINITY_FLINT_AND_STEEL.Name"),getList("Resource.INFINITY_FLINT_AND_STEEL.Lore"));

    //建筑材料
    public static final SlimefunItemStack SPACE_INFINITY_MAGIC = createDefaultResourceGlowV2Vertical("SPACE_INFINITY_MAGIC",Material.NETHER_STAR);


    public static final SlimefunItemStack COBBLESTONE_1 = createDefaultResourceGlowV2Vertical("COBBLESTONE_1",Material.COBBLESTONE);
    public static final SlimefunItemStack COBBLESTONE_2 = createDefaultResourceGlowV2Vertical("COBBLESTONE_2",Material.COBBLESTONE);
    public static final SlimefunItemStack STONE_1 = createDefaultResourceGlowV2Vertical("STONE_1",Material.STONE);
    public static final SlimefunItemStack STONE_2 = createDefaultResourceGlowV2Vertical("STONE_2",Material.STONE);
    public static final SlimefunItemStack OAK_LOG_1 = createDefaultResourceGlowV2Vertical("OAK_LOG_1",Material.OAK_LOG);
    public static final SlimefunItemStack OAK_LOG_2 = createDefaultResourceGlowV2Vertical("OAK_LOG_2",Material.OAK_LOG);
    public static final SlimefunItemStack STONE_BRICKS_1 = createDefaultResourceGlowV2Vertical("STONE_BRICKS_1",Material.STONE_BRICKS);
    public static final SlimefunItemStack STONE_BRICKS_2 = createDefaultResourceGlowV2Vertical("STONE_BRICKS_2",Material.STONE_BRICKS);
    public static final SlimefunItemStack BRICKS_1 = createDefaultResourceGlowV2Vertical("BRICKS_1",Material.BRICKS);
    public static final SlimefunItemStack BRICKS_2 = createDefaultResourceGlowV2Vertical("BRICKS_2",Material.BRICKS);
    public static final SlimefunItemStack REDSTONE_1 = createDefaultResourceGlowV2Vertical("REDSTONE_1",Material.REDSTONE);
    public static final SlimefunItemStack REDSTONE_2 = createDefaultResourceGlowV2Vertical("REDSTONE_2",Material.REDSTONE);
    public static final SlimefunItemStack REDSTONE_TORCH_1 = createDefaultResourceGlowV2Vertical("REDSTONE_TORCH_1",Material.REDSTONE_TORCH);
    public static final SlimefunItemStack REDSTONE_TORCH_2 = createDefaultResourceGlowV2Vertical("REDSTONE_TORCH_2",Material.REDSTONE_TORCH);
    public static final SlimefunItemStack REPEATER_1 = createDefaultResourceGlowV2Vertical("REPEATER_1",Material.REPEATER);
    public static final SlimefunItemStack REPEATER_2 = createDefaultResourceGlowV2Vertical("REPEATER_2",Material.REPEATER);
    public static final SlimefunItemStack COMPARATOR_1 = createDefaultResourceGlowV2Vertical("COMPARATOR_1",Material.COMPARATOR);
    public static final SlimefunItemStack COMPARATOR_2 = createDefaultResourceGlowV2Vertical("COMPARATOR_2",Material.COMPARATOR);
    public static final SlimefunItemStack HOPPER_1 = createDefaultResourceGlowV2Vertical("HOPPER_1",Material.HOPPER);
    public static final SlimefunItemStack HOPPER_2 = createDefaultResourceGlowV2Vertical("HOPPER_2",Material.HOPPER);
    public static final SlimefunItemStack STRING_1 = createDefaultResourceGlowV2Vertical("STRING_1",Material.STRING);
    public static final SlimefunItemStack STRING_2 = createDefaultResourceGlowV2Vertical("STRING_2",Material.STRING);
    public static final SlimefunItemStack TRIPWIRE_HOOK_1 = createDefaultResourceGlowV2Vertical("TRIPWIRE_HOOK_1",Material.TRIPWIRE_HOOK);
    public static final SlimefunItemStack TRIPWIRE_HOOK_2 = createDefaultResourceGlowV2Vertical("TRIPWIRE_HOOK_2",Material.TRIPWIRE_HOOK);
    public static final SlimefunItemStack FURNACE_1 = createDefaultResourceGlowV2Vertical("FURNACE_1",Material.FURNACE);
    public static final SlimefunItemStack FURNACE_2 = createDefaultResourceGlowV2Vertical("FURNACE_2",Material.FURNACE);
    public static final SlimefunItemStack IRON_INGOT_1 = createDefaultResourceGlowV2Vertical("IRON_INGOT_1",Material.IRON_INGOT);
    public static final SlimefunItemStack IRON_INGOT_2 = createDefaultResourceGlowV2Vertical("IRON_INGOT_2",Material.IRON_INGOT);
    public static final SlimefunItemStack IRON_INGOT_3 = createDefaultResourceGlowV2Vertical("IRON_INGOT_3",Material.IRON_INGOT);
    public static final SlimefunItemStack GLASS_1 = createDefaultResourceGlowV2Vertical("GLASS_1",Material.GLASS);
    public static final SlimefunItemStack GLASS_2 = createDefaultResourceGlowV2Vertical("GLASS_2",Material.GLASS);
    public static final SlimefunItemStack LIGHT_1 = createDefaultResourceGlowV2Vertical("LIGHT_1",Material.LIGHT);
    public static final SlimefunItemStack LIGHT_2 = createDefaultResourceGlowV2Vertical("LIGHT_2",Material.LIGHT);
    public static final SlimefunItemStack QUARTZ_BLOCK_1 = createDefaultResourceGlowV2Vertical("QUARTZ_BLOCK_1",Material.QUARTZ_BLOCK);
    public static final SlimefunItemStack QUARTZ_BLOCK_2 = createDefaultResourceGlowV2Vertical("QUARTZ_BLOCK_2",Material.QUARTZ_BLOCK);
    public static final SlimefunItemStack COLOR_WOOL_1 = createDefaultResourceGlowV2Vertical("COLOR_WOOL_1",Material.PINK_WOOL);
    public static final SlimefunItemStack COLOR_WOOL_2 = createDefaultResourceGlowV2Vertical("COLOR_WOOL_2",Material.PINK_WOOL);
    public static final SlimefunItemStack COLOR_TERRACOTTA_1 = createDefaultResourceGlowV2Vertical("COLOR_TERRACOTTA_1",Material.PINK_TERRACOTTA);
    public static final SlimefunItemStack COLOR_TERRACOTTA_2 = createDefaultResourceGlowV2Vertical("COLOR_TERRACOTTA_2",Material.PINK_TERRACOTTA);
    public static final SlimefunItemStack COLOR_CONCRETE_1 = createDefaultResourceGlowV2Vertical("COLOR_CONCRETE_1",Material.PINK_CONCRETE);
    public static final SlimefunItemStack COLOR_CONCRETE_2 = createDefaultResourceGlowV2Vertical("COLOR_CONCRETE_2",Material.PINK_CONCRETE);
    public static final SlimefunItemStack COLOR_GLAZED_TERRACOTTA_1 = createDefaultResourceGlowV2Vertical("COLOR_GLAZED_TERRACOTTA_1",Material.PINK_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack COLOR_GLAZED_TERRACOTTA_2 = createDefaultResourceGlowV2Vertical("COLOR_GLAZED_TERRACOTTA_2",Material.PINK_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack COLOR_LEAVES_1 = createDefaultResourceGlowV2Vertical("COLOR_LEAVES_1",Material.CHERRY_LEAVES);
    public static final SlimefunItemStack COLOR_LEAVES_2 = createDefaultResourceGlowV2Vertical("COLOR_LEAVES_2",Material.CHERRY_LEAVES);
    public static final SlimefunItemStack COLOR_LOG_1 = createDefaultResourceGlowV2Vertical("COLOR_LOG_1",Material.CHERRY_LOG);
    public static final SlimefunItemStack COLOR_LOG_2 = createDefaultResourceGlowV2Vertical("COLOR_LOG_2",Material.CHERRY_LOG);








    public static final SlimefunItemStack JIN_KE_LA = createDefaultResourceGlowV2Vertical("JIN_KE_LA",Material.BONE_MEAL);



    public static final SlimefunItemStack POWER_CORE = themedVer2Vertical("POWER_CORE",new CustomItemStack(CustomHead.getHead("96e0c954d5cab4b9714b8a7eaf9742eeaab7dda7fc2effefb1530099ae1309ac")),
            get("Resource.POWER_CORE.Name"),getList("Resource.POWER_CORE.Lore"));

    public static final SlimefunItemStack PURE_INGOT_POWER_CORE = themedVer2Vertical("PURE_INGOT_POWER_CORE",new CustomItemStack(CustomHead.getHead("b2d47d4f7042f0abe73aa50189baae2343964964a36bb868b444091876b9031b")),
            get("Resource.PURE_INGOT_POWER_CORE.Name"),getList("Resource.PURE_INGOT_POWER_CORE.Lore"));
    public static final SlimefunItemStack BAD_LUCK_CAPACITY = themedVer2Vertical("BAD_LUCK_CAPACITY",new CustomItemStack(CustomHead.getHead("52406e3a7fc1af63e45744b6e122ca23c204145f3889f6ea702173f0f045071c")),
            get("Resource.BAD_LUCK_CAPACITY.Name"),getList("Resource.BAD_LUCK_CAPACITY.Lore"));
    public static final SlimefunItemStack PANDORA_CAPACITY = themedVer2Vertical("PANDORA_CAPACITY",new CustomItemStack(CustomHead.getHead("2c0e552c1f1dbe3f5782f0fbb9c63d7965b7347a5c2f959a1120527a65bac17f")),
            get("Resource.PANDORA_CAPACITY.Name"),getList("Resource.PANDORA_CAPACITY.Lore"));
    public static final SlimefunItemStack MAGIC_CAPACITY_BASIC = themedVer2Vertical("MAGIC_CAPACITY_BASIC",new CustomItemStack(CustomHead.getHead("1e31daea30e571bb8bdfa0c6d2c8ea1bc721a4b12accf7da438b6f0598bf0886")),
            get("Resource.MAGIC_CAPACITY_BASIC.Name"),getList("Resource.MAGIC_CAPACITY_BASIC.Lore"));
    public static final SlimefunItemStack MAGIC_CAPACITY_ULTRA = themedVer2Vertical("MAGIC_CAPACITY_ULTRA",new CustomItemStack(CustomHead.getHead("5b10ed94080a07546a245ec837cfc5814c7658c7ef337be035a11b2e1e03ba16")),
            get("Resource.MAGIC_CAPACITY_ULTRA.Name"),getList("Resource.MAGIC_CAPACITY_ULTRA.Lore"));






    public static final SlimefunItemStack POWER_CARD = themedVer2Vertical("POWER_CARD",Material.PAPER,
            get("Resource.POWER_CARD.Name"),getList("Resource.POWER_CARD.Lore"));


    public static final SlimefunItemStack PRE_BUILDING_TAFEI = createDefaultItemGlow("PRE_BUILDING_TAFEI",Material.PINK_WOOL);


    public static final SlimefunItemStack PRE_BUILDING_VILLAGE_LOVE_AND_TRADE_HOUSE = createDefaultItemGlow("PRE_BUILDING_VILLAGE_LOVE_AND_TRADE_HOUSE",Material.BARREL);
    public static final SlimefunItemStack PRE_BUILDING_SHULKER_FARM = createDefaultItemGlow("PRE_BUILDING_SHULKER_FARM",Material.SHULKER_BOX);
    public static final SlimefunItemStack PRE_BUILDING_MCDONALDS = createDefaultItemGlow("PRE_BUILDING_MCDONALDS",Material.YELLOW_WOOL);





    public static final SlimefunItemStack PRE_BUILDING_HOUSE_OAK = createDefaultItemGlow("PRE_BUILDING_HOUSE_OAK",Material.OAK_LOG);
    public static final SlimefunItemStack PRE_BUILDING_HOUSE_SPRUCE = createDefaultItemGlow("PRE_BUILDING_HOUSE_SPRUCE",Material.SPRUCE_LOG);
    public static final SlimefunItemStack PRE_BUILDING_HOUSE_CHERRY = createDefaultItemGlow("PRE_BUILDING_HOUSE_CHERRY",Material.CHERRY_LOG);
    public static final SlimefunItemStack PRE_BUILDING_HOUSE_MANGROVE = createDefaultItemGlow("PRE_BUILDING_HOUSE_MANGROVE",Material.MANGROVE_LOG);
    public static final SlimefunItemStack PRE_BUILDING_HOUSE_MOSS = createDefaultItemGlow("PRE_BUILDING_HOUSE_MOSS",Material.MOSS_BLOCK);
    public static final SlimefunItemStack PRE_BUILDING_HOUSE_WIND_CAR = createDefaultItemGlow("PRE_BUILDING_HOUSE_WIND_CAR",Material.WHITE_WOOL);
    public static final SlimefunItemStack PRE_BUILDING_DORA_PICTURE = createDefaultItemGlow("PRE_BUILDING_DORA_PICTURE",Material.PINK_CONCRETE_POWDER);
    public static final SlimefunItemStack PRE_BUILDING_WISH_DALE = createDefaultItemGlow("PRE_BUILDING_WISH_DALE",Material.RED_CONCRETE);
    public static final SlimefunItemStack PRE_BUILDING_ANON = createDefaultItemGlow("PRE_BUILDING_ANON",Material.CHERRY_LEAVES);

    public static final SlimefunItemStack PRE_BUILDING_WINE_BAR = createDefaultItemGlow("PRE_BUILDING_WINE_BAR",Material.BARREL);
    public static final SlimefunItemStack PRE_BUILDING_MOON_RABBIT_SHOP = createDefaultItemGlow("PRE_BUILDING_MOON_RABBIT_SHOP",Material.PUMPKIN_PIE);
    public static final SlimefunItemStack PRE_BUILDING_MIDDLE_HORSE_HOUSE = createDefaultItemGlow("PRE_BUILDING_MIDDLE_HORSE_HOUSE",Material.DIAMOND_HORSE_ARMOR);
    public static final SlimefunItemStack PRE_BUILDING_FISHING_PORT = createDefaultItemGlow("PRE_BUILDING_FISHING_PORT",Material.FISHING_ROD);
    public static final SlimefunItemStack PRE_BUILDING_SI_HE_YUAN = createDefaultItemGlow("PRE_BUILDING_SI_HE_YUAN",Material.BLUE_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PRE_BUILDING_BAKERY = createDefaultItemGlow("PRE_BUILDING_BAKERY",Material.WHEAT);
    public static final SlimefunItemStack PRE_BUILDING_KFC_MIDDLE = createDefaultItemGlow("PRE_BUILDING_KFC_MIDDLE",Material.COOKED_CHICKEN);
    public static final SlimefunItemStack PRE_BUILDING_KFC_SMALL = createDefaultItemGlow("PRE_BUILDING_KFC_SMALL",Material.FEATHER);
    public static final SlimefunItemStack PRE_BUILDING_LARGE_SNOW_KING = createDefaultItemGlow("PRE_BUILDING_LARGE_SNOW_KING",Material.SNOW);
    public static final SlimefunItemStack PRE_BUILDING_LITTLE_MI_XUE = createDefaultItemGlow("PRE_BUILDING_LITTLE_MI_XUE",Material.SNOW_BLOCK);
    public static final SlimefunItemStack PRE_BUILDING_MIDDLE_VILLA = createDefaultItemGlow("PRE_BUILDING_MIDDLE_VILLA",Material.OAK_WOOD);
    public static final SlimefunItemStack PRE_BUILDING_JAPAN_HOUSE = createDefaultItemGlow("PRE_BUILDING_JAPAN_HOUSE",Material.CHERRY_PLANKS);
    public static final SlimefunItemStack PRE_BUILDING_FARM_BARN = createDefaultItemGlow("PRE_BUILDING_FARM_BARN",Material.HAY_BLOCK);
    public static final SlimefunItemStack PRE_BUILDING_SAKURA_SHOP = createDefaultItemGlow("PRE_BUILDING_SAKURA_SHOP",Material.CHERRY_LOG);
    public static final SlimefunItemStack PRE_BUILDING_FRUIT_SHOP = createDefaultItemGlow("PRE_BUILDING_FRUIT_SHOP",Material.APPLE);
    public static final SlimefunItemStack PRE_BUILDING_KRUSTY_KRAB = createDefaultItemGlow("PRE_BUILDING_KRUSTY_KRAB",Material.BREAD);





    public static final SlimefunItemStack SF_TIMINGS_HOLOGRAM = createDefaultItemGlow("SF_TIMINGS_HOLOGRAM",Material.QUARTZ_SLAB);
    public static final SlimefunItemStack MUSIC_TEST = createDefaultItemGlow("MUSIC_TEST",Material.RABBIT);
    public static final SlimefunItemStack CARGO_TERMINAL = createDefaultItemGlow("CARGO_TERMINAL",Material.NOTE_BLOCK);
    public static final SlimefunItemStack CARGO_TERMINAL_RENEW = createDefaultItemGlow("CARGO_TERMINAL_RENEW",Material.NOTE_BLOCK);
    public static final SlimefunItemStack CARGO_FRAGMENT = createDefaultItemGlow("CARGO_FRAGMENT",Material.PRISMARINE_CRYSTALS);




    public static final SlimefunItemStack UI_THX = createDefaultItemV2Vertical("UI_THX",Material.PAPER);

//    public static final SlimefunItemStack UI_3 = createDefaultItem("UI_3",Material.LIGHT_GRAY_STAINED_GLASS_PANE);

    //AUTHOR
    public static final SlimefunItemStack UI_WIKI = new SlimefunItemStack(
            "MAGIC_EXPANSION_UI_WIKI",
            Material.CALIBRATED_SCULK_SENSOR,
            getGradientNameVer2("Wiki Website"),
            "",
            getGradientNameVer2("https://github.com/Yomicer/MagicExpansion/wiki"),
            getGradientNameVer2("Original QQ Group: 770346419")
    );
    public static final SlimefunItemStack UI_4 = createDefaultItem("UI_4",Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack UI_5 = createDefaultItem("UI_5",Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack UI_6 = createDefaultItem("UI_6",Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack UI_7 = createDefaultItem("UI_7",Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack UI_8 = createDefaultItem("UI_8",Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack UI_9 = createDefaultItem("UI_9",Material.LIGHT_GRAY_STAINED_GLASS_PANE);

    public static final SlimefunItemStack UI_NOT_LATEST_BUILD = new SlimefunItemStack(
            "MAGIC_EXPANSION_UI_NOT_LATEST_BUILD",
            Material.CALIBRATED_SCULK_SENSOR,
            getGradientNameVer2("Automatic updates are disabled or this is a development build."),
            "",
            getGradientNameVer2("Check the project page for the latest release notes.")
    );
    public static final SlimefunItemStack UI_IS_LATEST_BUILD = new SlimefunItemStack(
            "MAGIC_EXPANSION_UI_IS_LATEST_BUILD",
            Material.NETHER_STAR,
            getGradientNameVer2("MagicExpansion is up to date."),
            "",
            getGradientNameVer2("Tomorrow can be even better.")
    );


    public static final SlimefunItemStack NAZUKICYL_TEST = themedVer2("NAZUKICYL_TEST",new CustomItemStack(CustomHead.getHead("8adb25ab9976d89d0bd8118d72c1c06bb907060c1e02a729b652d1e86b1ebbbc")),
            get("Items.NAZUKICYL_TEST.Name"),getList("Items.NAZUKICYL_TEST.Lore"));
    public static final SlimefunItemStack HAIMAN_TEST = themedVer2("HAIMAN_TEST",new CustomItemStack(CustomHead.getHead("1421f1514da756c8c6c7c0b83a79265c26c9ece66b3bad8fbd94bd96d7040d7e")),
            get("Items.HAIMAN_TEST.Name"),getList("Items.HAIMAN_TEST.Lore"));
    public static final SlimefunItemStack QIZHIYI_TEST = themedVer2("QIZHIYI_TEST",new CustomItemStack(CustomHead.getHead("f92cbe88217460b5b5edad02d6b9a547ac0a194e75c061fba754a815d8f08f9")),
            get("Items.QIZHIYI_TEST.Name"),getList("Items.QIZHIYI_TEST.Lore"));
    public static final SlimefunItemStack KOMU_A = themedVer2("KOMU_A",new CustomItemStack(CustomHead.getHead("2a06b7ef30db3cd6a869e015e59913e4dfc9212688c281fab18b1f2938fc9f42")),
            get("Items.KOMU_A.Name"),getList("Items.KOMU_A.Lore"));
    public static final SlimefunItemStack LENGSHANG_TEST = themedVer2("LENGSHANG_TEST",new CustomItemStack(CustomHead.getHead("20b13024045c5c0d6d2afb059ae9660f7ad020fd895bb75f64fcc02ddb07d327")),
            get("Items.LENGSHANG_TEST.Name"),getList("Items.LENGSHANG_TEST.Lore"));






    public static final SlimefunItemStack MAGIC_CROP_INFO = createDefaultItemGlowV2Vertical("MAGIC_CROP_INFO",Material.PAPER);
    public static final SlimefunItemStack WHEAT_SEEDS = createDefaultItemGlowV2Vertical("WHEAT_SEEDS",Material.WHEAT_SEEDS);
    public static final SlimefunItemStack POTATO = createDefaultItemGlowV2Vertical("POTATO",Material.POTATO);
    public static final SlimefunItemStack HYBRID_RICE_SEEDS = createDefaultItemGlowV2Vertical("HYBRID_RICE_SEEDS",Material.WHEAT_SEEDS);

    //矿粉相关魔法植物
    public static final SlimefunItemStack WHEAT_COPPER = createDefaultItemGlowV2Vertical("WHEAT_COPPER",Material.WHEAT_SEEDS);
    public static final SlimefunItemStack RADISH_IRON = createDefaultItemGlowV2Vertical("RADISH_IRON",Material.CARROT);
    public static final SlimefunItemStack BEET_GOLD = createDefaultItemGlowV2Vertical("BEET_GOLD",Material.BEETROOT_SEEDS);
    public static final SlimefunItemStack RADISH_SILVER = createDefaultItemGlowV2Vertical("RADISH_SILVER",Material.CARROT);
    public static final SlimefunItemStack WHEAT_ZINC = createDefaultItemGlowV2Vertical("WHEAT_ZINC",Material.WHEAT_SEEDS);
    public static final SlimefunItemStack BEET_MAGNESIUM = createDefaultItemGlowV2Vertical("BEET_MAGNESIUM",Material.BEETROOT_SEEDS);
    public static final SlimefunItemStack POTATO_TIN = createDefaultItemGlowV2Vertical("POTATO_TIN",Material.POTATO);
    public static final SlimefunItemStack BEET_LEAD = createDefaultItemGlowV2Vertical("BEET_LEAD",Material.BEETROOT_SEEDS);
    public static final SlimefunItemStack WHEAT_ALUMINUM = createDefaultItemGlowV2Vertical("WHEAT_ALUMINUM",Material.WHEAT_SEEDS);
    //矿锭相关魔法植物
    public static final SlimefunItemStack WHEAT_COPPER_INGOT = createDefaultItemGlowV2Vertical("WHEAT_COPPER_INGOT",Material.WHEAT_SEEDS);
    public static final SlimefunItemStack RADISH_IRON_INGOT = createDefaultItemGlowV2Vertical("RADISH_IRON_INGOT",Material.CARROT);
    public static final SlimefunItemStack BEET_GOLD_INGOT = createDefaultItemGlowV2Vertical("BEET_GOLD_INGOT",Material.BEETROOT_SEEDS);
    public static final SlimefunItemStack RADISH_SILVER_INGOT = createDefaultItemGlowV2Vertical("RADISH_SILVER_INGOT",Material.CARROT);
    public static final SlimefunItemStack WHEAT_ZINC_INGOT = createDefaultItemGlowV2Vertical("WHEAT_ZINC_INGOT",Material.WHEAT_SEEDS);
    public static final SlimefunItemStack BEET_MAGNESIUM_INGOT = createDefaultItemGlowV2Vertical("BEET_MAGNESIUM_INGOT",Material.BEETROOT_SEEDS);
    public static final SlimefunItemStack POTATO_TIN_INGOT = createDefaultItemGlowV2Vertical("POTATO_TIN_INGOT",Material.POTATO);
    public static final SlimefunItemStack BEET_LEAD_INGOT = createDefaultItemGlowV2Vertical("BEET_LEAD_INGOT",Material.BEETROOT_SEEDS);
    public static final SlimefunItemStack WHEAT_ALUMINUM_INGOT = createDefaultItemGlowV2Vertical("WHEAT_ALUMINUM_INGOT",Material.WHEAT_SEEDS);

    //合金锭相关魔法植物
    public static final SlimefunItemStack WHEAT_BRONZE_INGOT = createDefaultItemGlowV2Vertical("WHEAT_BRONZE_INGOT",Material.WHEAT_SEEDS);
    public static final SlimefunItemStack BEET_BRASS_INGOT = createDefaultItemGlowV2Vertical("BEET_BRASS_INGOT",Material.BEETROOT_SEEDS);



    public static final SlimefunItemStack ORE_DUST_CRYSTAL = createDefaultItemGlowV2Vertical("ORE_DUST_CRYSTAL",Material.AMETHYST_SHARD);
    public static final SlimefunItemStack ORE_INGOT_CRYSTAL = createDefaultItemGlowV2Vertical("ORE_INGOT_CRYSTAL",Material.AMETHYST_CLUSTER);
    public static final SlimefunItemStack ORE_MIX_CRYSTAL = createDefaultItemGlowV2Vertical("ORE_MIX_CRYSTAL",Material.PRISMARINE_CRYSTALS);








    //更新日志
    public static final SlimefunItemStack UPDATE_LOG_2025_06_23 = createDefaultItemGlow("UPDATE_LOG_2025_06_23",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_06_29 = createDefaultItemGlow("UPDATE_LOG_2025_06_29",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_07_12 = createDefaultItemGlow("UPDATE_LOG_2025_07_12",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_07_23 = createDefaultItemGlow("UPDATE_LOG_2025_07_23",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_07_25 = createDefaultItemGlow("UPDATE_LOG_2025_07_25",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_07_26 = createDefaultItemGlow("UPDATE_LOG_2025_07_26",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_08_01 = createDefaultItemGlow("UPDATE_LOG_2025_08_01",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_08_27 = createDefaultItemGlow("UPDATE_LOG_2025_08_27",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_08_29 = createDefaultItemGlow("UPDATE_LOG_2025_08_29",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_08_30 = createDefaultItemGlow("UPDATE_LOG_2025_08_30",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_02 = createDefaultItemGlow("UPDATE_LOG_2025_09_02",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_05 = createDefaultItemGlow("UPDATE_LOG_2025_09_05",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_09 = createDefaultItemGlow("UPDATE_LOG_2025_09_09",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_13 = createDefaultItemGlow("UPDATE_LOG_2025_09_13",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_14 = createDefaultItemGlow("UPDATE_LOG_2025_09_14",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_18 = createDefaultItemGlow("UPDATE_LOG_2025_09_18",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_20 = createDefaultItemGlow("UPDATE_LOG_2025_09_20",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_27 = createDefaultItemGlow("UPDATE_LOG_2025_09_27",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_09_29 = createDefaultItemGlow("UPDATE_LOG_2025_09_29",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_10_06 = createDefaultItemGlow("UPDATE_LOG_2025_10_06",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_10_07 = createDefaultItemGlow("UPDATE_LOG_2025_10_07",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_10_14 = createDefaultItemGlow("UPDATE_LOG_2025_10_14",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_10_23 = createDefaultItemGlow("UPDATE_LOG_2025_10_23",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_10_25 = createDefaultItemGlow("UPDATE_LOG_2025_10_25",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_10_26 = createDefaultItemGlow("UPDATE_LOG_2025_10_26",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_02 = createDefaultItemGlow("UPDATE_LOG_2025_11_02",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_05 = createDefaultItemGlow("UPDATE_LOG_2025_11_05",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_07 = createDefaultItemGlow("UPDATE_LOG_2025_11_07",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_08 = createDefaultItemGlow("UPDATE_LOG_2025_11_08",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_12 = createDefaultItemGlow("UPDATE_LOG_2025_11_12",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_21 = createDefaultItemGlow("UPDATE_LOG_2025_11_21",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_22 = createDefaultItemGlow("UPDATE_LOG_2025_11_22",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_11_23 = createDefaultItemGlow("UPDATE_LOG_2025_11_23",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_01 = createDefaultItemGlow("UPDATE_LOG_2025_12_01",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_06 = createDefaultItemGlow("UPDATE_LOG_2025_12_06",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_13 = createDefaultItemGlow("UPDATE_LOG_2025_12_13",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_16 = createDefaultItemGlow("UPDATE_LOG_2025_12_16",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_19 = createDefaultItemGlow("UPDATE_LOG_2025_12_19",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_20 = createDefaultItemGlow("UPDATE_LOG_2025_12_20",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2025_12_31 = createDefaultItemGlow("UPDATE_LOG_2025_12_31",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_01_02 = createDefaultItemGlow("UPDATE_LOG_2026_01_02",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_01_23 = createDefaultItemGlow("UPDATE_LOG_2026_01_23",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_01_27 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_01_27",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_01_28 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_01_28",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_02_19 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_02_19",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_03_10 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_03_10",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_03_13 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_03_13",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_03_17 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_03_17",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_03_22 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_03_22",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_04_18 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_04_18",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_05_21 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_05_21",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_06_24 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_06_24",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_07_03 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_07_03",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_07_12 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_07_12",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_07_30 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_07_30",Material.PAPER);
    public static final SlimefunItemStack UPDATE_LOG_2026_08_05 = createDefaultItemGlowV2Vertical("UPDATE_LOG_2026_08_05",Material.PAPER);



















    public static final SlimefunItemStack ITEM_NAME_TAG = createDefaultItemGlowV2Vertical("ITEM_NAME_TAG",Material.NAME_TAG);





    public static final SlimefunItemStack DEATH_LIFE_BOOK = createDefaultItemGlow("DEATH_LIFE_BOOK",Material.BOOK);

    public static final SlimefunItemStack DRAW_MACHINE = createDefaultItemGlow("DRAW_MACHINE",Material.CAULDRON);
    public static final SlimefunItemStack MAGIC_WAND = createDefaultItemGlow("MAGIC_WAND",Material.BLAZE_ROD);







    public static final SlimefunItemStack FISHING_ROD_LOG = createDefaultItem("FISHING_ROD_LOG",Material.FISHING_ROD);
    public static final SlimefunItemStack FISHING_ROD_NEW_PLAYER = createDefaultItem("FISHING_ROD_NEW_PLAYER",Material.FISHING_ROD);
    public static final SlimefunItemStack FISHING_STICK_STAR_IRON = createDefaultItem("FISHING_STICK_STAR_IRON",Material.STICK);
    public static final SlimefunItemStack FISHING_ROD_WIND_SPEAKER = createDefaultItem("FISHING_ROD_WIND_SPEAKER",Material.FISHING_ROD);
    public static final SlimefunItemStack FISHING_ROD_FINAL_STICK = createDefaultItem("南柯一梦终须醒_浮生若梦皆是空",Material.FISHING_ROD);

    // Between Water and Clouds series
    public static final SlimefunItemStack FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO = createDefaultItem("FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO",Material.FISHING_ROD);
    public static final SlimefunItemStack FISHING_ROD_FINAL_STRING = createDefaultItem("FISHING_ROD_FINAL_STRING",Material.STRING);
    public static final SlimefunItemStack FISHING_ROD_FINAL_HOOK = createDefaultItem("FISHING_ROD_FINAL_HOOK",Material.DIAMOND_PICKAXE);

    public static final SlimefunItemStack FISHING_ROD_FISH_ANYTHING = createDefaultItem("FISHING_ROD_FISH_ANYTHING",Material.APPLE);


    // 双面胶
    public static final SlimefunItemStack DOUBLE_SIDED_TAPE = createDefaultItem("DOUBLE_SIDED_TAPE",Material.PAPER);
    public static final SlimefunItemStack SCHRODINGER_FRAME_ONE = createDefaultItem("SCHRODINGER_FRAME_ONE",Material.ITEM_FRAME);
    public static final SlimefunItemStack SCHRODINGER_FRAME_INFINITE = createDefaultItem("SCHRODINGER_FRAME_INFINITE",Material.GLOW_ITEM_FRAME);


    public static final SlimefunItemStack SEND_ITEMS_TO_PLAYER_MACHINE = createDefaultItem("SEND_ITEMS_TO_PLAYER_MACHINE",Material.BEACON);
    public static final SlimefunItemStack SEND_ITEMS_TO_PLAYER_MACHINE_SF = createDefaultItem("SEND_ITEMS_TO_PLAYER_MACHINE_SF",Material.SLIME_BLOCK);
    public static final SlimefunItemStack PORTABLE_CARGO_TRANSPORTER = createDefaultItem("PORTABLE_CARGO_TRANSPORTER",Material.NETHER_STAR);


    public static final SlimefunItemStack CUSTOM_SEQUENCE_TOOL = createDefaultItem("CUSTOM_SEQUENCE_TOOL",Material.HEART_OF_THE_SEA);


    public static final SlimefunItemStack WORLD_CORE = createDefaultItem("WORLD_CORE",Material.FIREWORK_STAR);
    public static final SlimefunItemStack ITEM_ORIGIN_BACK_TRACK = createDefaultItem("ITEM_ORIGIN_BACK_TRACK",Material.BEACON);





    public static final SlimefunItemStack FISHING_INFO = createDefaultItemGlow("FISHING_INFO",Material.PAPER);
    public static final SlimefunItemStack COOPERATE_CREATION_INFO = createDefaultItemGlowV2Vertical("COOPERATE_CREATION_INFO",Material.PAPER);
    public static final SlimefunItemStack FISH_CATEGORY_INFO = createDefaultItemGlow("FISH_CATEGORY_INFO",Material.PAPER);



    public static final SlimefunItemStack TWO_TO_MAGIC_GEO_MACHINE_FORCE = createDefaultItemGlowV2Vertical("TWO_TO_MAGIC_GEO_MACHINE_FORCE",Material.FURNACE);


    public static final SlimefunItemStack FIVE_ELEMENT_GEN_BASIC = createGeneratorGlowV2Vertical("FIVE_ELEMENT_GEN_BASIC",Material.ENCHANTING_TABLE);
    public static final SlimefunItemStack SLIME_BOOK_GEN_ADVANCE = createGeneratorGlowV2Vertical("SLIME_BOOK_GEN_ADVANCE",Material.BOOKSHELF);



    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN_MAKER_ALPHA = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN_MAKER_ALPHA",Material.CRAFTING_TABLE);
    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN_MAKER_BETA = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN_MAKER_BETA",Material.FLETCHING_TABLE);
    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN_MAKER_LITE = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN_MAKER_LITE",Material.CAMPFIRE);

    //万物演化台
    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN",Material.ENCHANTING_TABLE);
    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN_ULTRA = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN_ULTRA",Material.SCULK_SHRIEKER);

    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN_LITE = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN_LITE",Material.BLAST_FURNACE);

    public static final SlimefunItemStack ORIGIN_MATERIAL_GEN_LITE_PRESS = createGeneratorGlowV2Vertical("ORIGIN_MATERIAL_GEN_LITE_PRESS",Material.GLOWSTONE);



    //下面是有关食物的Item
    public static final SlimefunItemStack FARM_HARVEST_BREAD = createDefaultItemGlowV2Vertical("FARM_HARVEST_BREAD",Material.BREAD);
    public static final SlimefunItemStack HOLY_PIE = createDefaultItemGlowV2Vertical("HOLY_PIE",Material.PUMPKIN_PIE);
    public static final SlimefunItemStack CLOUD_BREAD = createDefaultItemGlowV2Vertical("CLOUD_BREAD",Material.BREAD);
    public static final SlimefunItemStack HARVEST_WHEAT = createDefaultItemGlowV2Vertical("HARVEST_WHEAT",Material.WHEAT);
    public static final SlimefunItemStack WHEAT_FLOUR = createDefaultItemGlowV2Vertical("WHEAT_FLOUR",Material.SUGAR);
    public static final SlimefunItemStack HARVEST_RICE = createDefaultItemGlowV2Vertical("HARVEST_RICE",Material.WHEAT);
    public static final SlimefunItemStack DREAM_KERNEL = createDefaultItemGlowV2Vertical("DREAM_KERNEL",Material.NETHER_STAR);

    public static final SlimefunItemStack PRESS_CORE_ALPHA = createDefaultItemGlowV2Vertical("PRESS_CORE_ALPHA",Material.FIREWORK_STAR);



    public static final SlimefunItemStack ENCHANTMENT_ERASER = createDefaultItemGlowV2Vertical("ENCHANTMENT_ERASER",Material.PAPER);
    public static final SlimefunItemStack FISH_WEIGHT_ENHANCER = createDefaultItemGlowV2Vertical("FISH_WEIGHT_ENHANCER",Material.HONEYCOMB);


    public static final SlimefunItemStack PORTABLE_SHOP = createDefaultItemGlowV2Vertical("PORTABLE_SHOP",Material.KNOWLEDGE_BOOK);



    public static final SlimefunItemStack RESEARCH_UNLOCKER_PAPER = createDefaultItemGlowV2Vertical("RESEARCH_UNLOCKER_PAPER",Material.PAPER);
    public static final SlimefunItemStack WORD_CLEAR = createDefaultItemGlowV2Vertical("WORD_CLEAR",Material.STICK);


    public static final SlimefunItemStack CHINESE_CHARACTER_CONSTRUCTOR = createDefaultItemGlowV2Vertical("CHINESE_CHARACTER_CONSTRUCTOR",Material.SMOKER);








    public static final SlimefunItemStack FISHING_BOOK = createDefaultItemGlow("FISHING_BOOK",Material.KNOWLEDGE_BOOK);

    public static final SlimefunItemStack RANDOM_FISH_COMMON = themedVer2Vertical("RANDOM_FISH_COMMON",Material.COD_BUCKET,
            get("Items.RANDOM_FISH_COMMON.Name"),getList("Items.RANDOM_FISH_COMMON.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_UNCOMMON = themedVer2Vertical("RANDOM_FISH_UNCOMMON",Material.SALMON_BUCKET,
            get("Items.RANDOM_FISH_UNCOMMON.Name"),getList("Items.RANDOM_FISH_UNCOMMON.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_RARE = themedVer2Vertical("RANDOM_FISH_RARE",Material.PUFFERFISH_BUCKET,
            get("Items.RANDOM_FISH_RARE.Name"),getList("Items.RANDOM_FISH_RARE.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_RARE_POOL_DUST = themedVer2Vertical("RANDOM_FISH_RARE_POOL_DUST",Material.PUFFERFISH_BUCKET,
            get("Items.RANDOM_FISH_RARE_POOL_DUST.Name"),getList("Items.RANDOM_FISH_RARE_POOL_DUST.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_RARE_POOL_ORE = themedVer2Vertical("RANDOM_FISH_RARE_POOL_ORE",Material.PUFFERFISH_BUCKET,
            get("Items.RANDOM_FISH_RARE_POOL_ORE.Name"),getList("Items.RANDOM_FISH_RARE_POOL_ORE.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_RARE_POOL_INDUSTRY = themedVer2Vertical("RANDOM_FISH_RARE_POOL_INDUSTRY",Material.PUFFERFISH_BUCKET,
            get("Items.RANDOM_FISH_RARE_POOL_INDUSTRY.Name"),getList("Items.RANDOM_FISH_RARE_POOL_INDUSTRY.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_EPIC = themedVer2Vertical("RANDOM_FISH_EPIC",Material.TROPICAL_FISH_BUCKET,
            get("Items.RANDOM_FISH_EPIC.Name"),getList("Items.RANDOM_FISH_EPIC.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_EPIC_POOL_INDUSTRY = themedVer2Vertical("RANDOM_FISH_EPIC_POOL_INDUSTRY",Material.TROPICAL_FISH_BUCKET,
            get("Items.RANDOM_FISH_EPIC_POOL_INDUSTRY.Name"),getList("Items.RANDOM_FISH_EPIC_POOL_INDUSTRY.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_EPIC_POOL_ALLOY_INGOT = themedVer2Vertical("RANDOM_FISH_EPIC_POOL_ALLOY_INGOT",Material.TROPICAL_FISH_BUCKET,
            get("Items.RANDOM_FISH_EPIC_POOL_ALLOY_INGOT.Name"),getList("Items.RANDOM_FISH_EPIC_POOL_ALLOY_INGOT.Lore"));
    public static final SlimefunItemStack RANDOM_FISH_LEGENDARY = themedVer2Vertical("RANDOM_FISH_LEGENDARY",Material.AXOLOTL_BUCKET,
            get("Items.RANDOM_FISH_LEGENDARY.Name"),getList("Items.RANDOM_FISH_LEGENDARY.Lore"));
    public static final SlimefunItemStack FISH_LEGENDARY_EEL_POWER = themedVer2Vertical("FISH_LEGENDARY_EEL_POWER",Material.AXOLOTL_BUCKET,
            get("Items.FISH_LEGENDARY_EEL_POWER.Name"),getList("Items.FISH_LEGENDARY_EEL_POWER.Lore"));

    public static final SlimefunItemStack FISH_LURE_BASIC = themedVer2Vertical("FISH_LURE_BASIC",Material.WHITE_DYE,
            get("Items.FISH_LURE_BASIC.Name"),getList("Items.FISH_LURE_BASIC.Lore"));
    public static final SlimefunItemStack FISH_LURE_DUST = themedVer2Vertical("FISH_LURE_DUST",Material.YELLOW_DYE,
            get("Items.FISH_LURE_DUST.Name"),getList("Items.FISH_LURE_DUST.Lore"));
    public static final SlimefunItemStack FISH_LURE_ORE = themedVer2Vertical("FISH_LURE_ORE",Material.CYAN_DYE,
            get("Items.FISH_LURE_ORE.Name"),getList("Items.FISH_LURE_ORE.Lore"));
    public static final SlimefunItemStack FISH_LURE_ALLOY_INGOT = themedVer2Vertical("FISH_LURE_ALLOY_INGOT",Material.BROWN_DYE,
            get("Items.FISH_LURE_ALLOY_INGOT.Name"),getList("Items.FISH_LURE_ALLOY_INGOT.Lore"));




    // Between Water and Clouds lures
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA",Material.ORANGE_DYE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA_CLONE = new SlimefunItemStack(
            "FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA_CLONE",
            FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA
    );
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN",Material.GUNPOWDER);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN_CLONE = new SlimefunItemStack(
            "FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN_CLONE",
            FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN
    );
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO",Material.RED_DYE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO_CLONE = new SlimefunItemStack(
            "FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO_CLONE",
            FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO
    );
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN",Material.YELLOW_DYE);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN_CLONE = new SlimefunItemStack(
            "FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN_CLONE",
            FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN
    );
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE = createDefaultItem("FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE",Material.NETHER_STAR);
    public static final SlimefunItemStack FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE_CLONE = new SlimefunItemStack(
            "FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE_CLONE",
            FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE
    );





    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA",Material.WATER_BUCKET);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN",Material.WATER_BUCKET);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO",Material.WATER_BUCKET);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN",Material.WATER_BUCKET);
    public static final SlimefunItemStack FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE = createDefaultItem("FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE",Material.WATER_BUCKET);






    public static final SlimefunItemStack ENERGY_CONNECTOR_GLASS_INFO = createDefaultItemGlow("ENERGY_CONNECTOR_GLASS_INFO",Material.GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS",Material.GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS",Material.GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_TINTED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS",Material.TINTED_GLASS);
    // 染色玻璃(16种)
    public static final SlimefunItemStack ENERGY_CONNECTOR_WHITE_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS",Material.WHITE_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_LIGHT_GRAY_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS",Material.LIGHT_GRAY_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_ORANGE_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.ORANGE_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_MAGENTA_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.MAGENTA_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_LIGHT_BLUE_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.LIGHT_BLUE_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_YELLOW_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.YELLOW_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_LIME_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.LIME_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_PINK_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.PINK_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_GRAY_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.GRAY_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_CYAN_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.CYAN_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_PURPLE_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.PURPLE_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_BLUE_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.BLUE_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_BROWN_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.BROWN_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_GREEN_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.GREEN_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_RED_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.RED_STAINED_GLASS);
    public static final SlimefunItemStack ENERGY_CONNECTOR_BLACK_STAINED_GLASS = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.BLACK_STAINED_GLASS);
    // 染色玻璃板(16种)
    public static final SlimefunItemStack ENERGY_CONNECTOR_WHITE_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.WHITE_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_ORANGE_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.ORANGE_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_MAGENTA_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.MAGENTA_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_LIGHT_BLUE_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_YELLOW_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.YELLOW_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_LIME_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.LIME_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_PINK_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.PINK_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_GRAY_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_LIGHT_GRAY_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_CYAN_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.CYAN_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_PURPLE_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.PURPLE_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_BLUE_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.BLUE_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_BROWN_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.BROWN_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_GREEN_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.GREEN_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_RED_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.RED_STAINED_GLASS_PANE);
    public static final SlimefunItemStack ENERGY_CONNECTOR_BLACK_STAINED_GLASS_PANE = createDefaultItemGlowGlassV2Vertical("ENERGY_CONNECTOR_GLASS", Material.BLACK_STAINED_GLASS_PANE);






    //资源生成器
    public static final SlimefunItemStack RESOURCE_MACHINE_WOOD_BASIC = themedVer2Vertical("RESOURCE_MACHINE_WOOD_BASIC",Material.BAMBOO_BLOCK,
            get("GENERATOR.RESOURCE_MACHINE_WOOD_BASIC.Name"),getList("GENERATOR.RESOURCE_MACHINE_WOOD_BASIC.Lore"));
    public static final SlimefunItemStack RESOURCE_MACHINE_WOOD_ULTRA = themedVer2Vertical("RESOURCE_MACHINE_WOOD_ULTRA",Material.STRIPPED_BAMBOO_BLOCK,
            get("GENERATOR.RESOURCE_MACHINE_WOOD_ULTRA.Name"),getList("GENERATOR.RESOURCE_MACHINE_WOOD_ULTRA.Lore"));
    //光源发生器
    public static final SlimefunItemStack LIGHT_GEN_BASIC = themedVer2Vertical("LIGHT_GEN_BASIC",Material.GLOWSTONE,
            get("GENERATOR.LIGHT_GEN_BASIC.Name"),getList("GENERATOR.LIGHT_GEN_BASIC.Lore"));

    public static final SlimefunItemStack STRING_GEN_BASIC = themedVer2Vertical("STRING_GEN_BASIC",Material.WHITE_WOOL,
            get("GENERATOR.STRING_GEN_BASIC.Name"),getList("GENERATOR.STRING_GEN_BASIC.Lore"));
    public static final SlimefunItemStack STRING_GEN_ULTRA = themedVer2Vertical("STRING_GEN_ULTRA",Material.PINK_WOOL,
            get("GENERATOR.STRING_GEN_ULTRA.Name"),getList("GENERATOR.STRING_GEN_ULTRA.Lore"));




    //电力矿工机器人-石英
    public static final SlimefunItemStack MINE_MAN_QUARTZ_BASIC = themedVer2Vertical("MINE_MAN_QUARTZ_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_QUARTZ_BASIC.Name"),getList("GENERATOR.MINE_MAN_QUARTZ_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_AMETHYST_SHARD_BASIC = themedVer2Vertical("MINE_MAN_AMETHYST_SHARD_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_AMETHYST_SHARD_BASIC.Name"),getList("GENERATOR.MINE_MAN_AMETHYST_SHARD_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_REDSTONE_BASIC = themedVer2Vertical("MINE_MAN_REDSTONE_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_REDSTONE_BASIC.Name"),getList("GENERATOR.MINE_MAN_REDSTONE_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_DIAMOND_BASIC = themedVer2Vertical("MINE_MAN_DIAMOND_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_DIAMOND_BASIC.Name"),getList("GENERATOR.MINE_MAN_DIAMOND_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_LAPIS_LAZULI_BASIC = themedVer2Vertical("MINE_MAN_LAPIS_LAZULI_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_LAPIS_LAZULI_BASIC.Name"),getList("GENERATOR.MINE_MAN_LAPIS_LAZULI_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_EMERALD_BASIC = themedVer2Vertical("MINE_MAN_EMERALD_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_EMERALD_BASIC.Name"),getList("GENERATOR.MINE_MAN_EMERALD_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_COAL_BASIC = themedVer2Vertical("MINE_MAN_COAL_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_COAL_BASIC.Name"),getList("GENERATOR.MINE_MAN_COAL_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_NETHERITE_SCRAP_BASIC = themedVer2Vertical("MINE_MAN_NETHERITE_SCRAP_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_NETHERITE_SCRAP_BASIC.Name"),getList("GENERATOR.MINE_MAN_NETHERITE_SCRAP_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_IRON_DUST_BASIC = themedVer2Vertical("MINE_MAN_IRON_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_IRON_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_IRON_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_GOLD_DUST_BASIC = themedVer2Vertical("MINE_MAN_GOLD_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_GOLD_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_GOLD_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_COPPER_DUST_BASIC = themedVer2Vertical("MINE_MAN_COPPER_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_COPPER_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_COPPER_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_TIN_DUST_BASIC = themedVer2Vertical("MINE_MAN_TIN_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_TIN_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_TIN_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_SILVER_DUST_BASIC = themedVer2Vertical("MINE_MAN_SILVER_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_SILVER_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_SILVER_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_LEAD_DUST_BASIC = themedVer2Vertical("MINE_MAN_LEAD_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_LEAD_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_LEAD_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_ALUMINUM_DUST_BASIC = themedVer2Vertical("MINE_MAN_ALUMINUM_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_ALUMINUM_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_ALUMINUM_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_ZINC_DUST_BASIC = themedVer2Vertical("MINE_MAN_ZINC_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_ZINC_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_ZINC_DUST_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_MAGNESIUM_DUST_BASIC = themedVer2Vertical("MINE_MAN_MAGNESIUM_DUST_BASIC", CustomHead.BOT_GREEN.getItem(),
            get("GENERATOR.MINE_MAN_MAGNESIUM_DUST_BASIC.Name"),getList("GENERATOR.MINE_MAN_MAGNESIUM_DUST_BASIC.Lore"));

    //矿锭
    public static final SlimefunItemStack MINE_MAN_IRON_INGOT_BASIC = themedVer2Vertical("MINE_MAN_IRON_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_IRON_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_IRON_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_GOLD_INGOT_BASIC = themedVer2Vertical("MINE_MAN_GOLD_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_GOLD_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_GOLD_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_COPPER_INGOT_BASIC = themedVer2Vertical("MINE_MAN_COPPER_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_COPPER_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_COPPER_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_TIN_INGOT_BASIC = themedVer2Vertical("MINE_MAN_TIN_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_TIN_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_TIN_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_SILVER_INGOT_BASIC = themedVer2Vertical("MINE_MAN_SILVER_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_SILVER_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_SILVER_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_LEAD_INGOT_BASIC = themedVer2Vertical("MINE_MAN_LEAD_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_LEAD_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_LEAD_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_ALUMINUM_INGOT_BASIC = themedVer2Vertical("MINE_MAN_ALUMINUM_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_ALUMINUM_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_ALUMINUM_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_ZINC_INGOT_BASIC = themedVer2Vertical("MINE_MAN_ZINC_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_ZINC_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_ZINC_INGOT_BASIC.Lore"));
    public static final SlimefunItemStack MINE_MAN_MAGNESIUM_INGOT_BASIC = themedVer2Vertical("MINE_MAN_MAGNESIUM_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_MAGNESIUM_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_MAGNESIUM_INGOT_BASIC.Lore"));

    //合成,下界合金锭
    public static final SlimefunItemStack MINE_MAN_NETHERITE_INGOT_BASIC = themedVer2Vertical("MINE_MAN_NETHERITE_INGOT_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_NETHERITE_INGOT_BASIC.Name"),getList("GENERATOR.MINE_MAN_NETHERITE_INGOT_BASIC.Lore"));

    //合成,矿脉
    public static final SlimefunItemStack MINE_MAN_MINERAL_BASIC = themedVer2Vertical("MINE_MAN_MINERAL_BASIC", CustomHead.BOT_ORANGE.getItem(),
            get("GENERATOR.MINE_MAN_MINERAL_BASIC.Name"),getList("GENERATOR.MINE_MAN_MINERAL_BASIC.Lore"));
    //合成,终极矿脉
    public static final SlimefunItemStack MINE_MAN_MINERAL_ULTRA = themedVer2Vertical("MINE_MAN_MINERAL_ULTRA", CustomHead.BOT_PINK.getItem(),
            get("GENERATOR.MINE_MAN_MINERAL_ULTRA.Name"),getList("GENERATOR.MINE_MAN_MINERAL_ULTRA.Lore"));

    //模拟玩家右键机器人
    public static final SlimefunItemStack RIGHT_CLICK_MAN = themedVer2Vertical("RIGHT_CLICK_MAN", CustomHead.BOT_PURPLE.getItem(),
            get("Items.RIGHT_CLICK_MAN.Name"),getList("Items.RIGHT_CLICK_MAN.Lore"));




    //配方机器
    public static final SlimefunItemStack WOOD_TRANSFORM_BASIC = themedVer2Vertical("WOOD_TRANSFORM_BASIC",Material.STRIPPED_BAMBOO_BLOCK,
            get("RECIPE_MACHINE.WOOD_TRANSFORM_BASIC.Name"),getList("RECIPE_MACHINE.WOOD_TRANSFORM_BASIC.Lore"));
    public static final SlimefunItemStack WOOD_TRANSFORM_ULTRA = themedVer2Vertical("WOOD_TRANSFORM_ULTRA",Material.BAMBOO_BLOCK,
            get("RECIPE_MACHINE.WOOD_TRANSFORM_ULTRA.Name"),getList("RECIPE_MACHINE.WOOD_TRANSFORM_ULTRA.Lore"));

    public static final SlimefunItemStack LIGHT_TRANSFORM_BASIC = themedVer2Vertical("LIGHT_TRANSFORM_BASIC",Material.PINK_GLAZED_TERRACOTTA,
            get("RECIPE_MACHINE.LIGHT_TRANSFORM_BASIC.Name"),getList("RECIPE_MACHINE.LIGHT_TRANSFORM_BASIC.Lore"));
    //光源提取器
    public static final SlimefunItemStack LIGHT_EXTRACT_BASIC = themedVer2Vertical("LIGHT_EXTRACT_BASIC",Material.TINTED_GLASS,
            get("RECIPE_MACHINE.LIGHT_EXTRACT_BASIC.Name"),getList("RECIPE_MACHINE.LIGHT_EXTRACT_BASIC.Lore"));

    //提纯硅
    public static final SlimefunItemStack QUARTZ_PURE_MACHINE_BAISC = themedVer2Vertical("QUARTZ_PURE_MACHINE_BAISC",Material.QUARTZ_PILLAR,
            get("RECIPE_MACHINE.QUARTZ_PURE_MACHINE_BAISC.Name"),getList("RECIPE_MACHINE.QUARTZ_PURE_MACHINE_BAISC.Lore"));
    public static final SlimefunItemStack QUARTZ_PURE_MACHINE_ULTRA = themedVer2Vertical("QUARTZ_PURE_MACHINE_ULTRA",Material.SMOOTH_QUARTZ,
            get("RECIPE_MACHINE.QUARTZ_PURE_MACHINE_ULTRA.Name"),getList("RECIPE_MACHINE.QUARTZ_PURE_MACHINE_ULTRA.Lore"));


    //预制建筑工坊
    public static final SlimefunItemStack PRE_BUILDINGS_MACHINE = themedVer2Vertical("PRE_BUILDINGS_MACHINE",Material.AMETHYST_BLOCK,
            get("RECIPE_MACHINE.PRE_BUILDINGS_MACHINE.Name"),getList("RECIPE_MACHINE.PRE_BUILDINGS_MACHINE.Lore"));


    //终极魔法建筑工坊
    public static final SlimefunItemStack PRE_BUILDINGS_MACHINE_ADVANCED = createDefaultRecipeMachine("PRE_BUILDINGS_MACHINE_ADVANCED",Material.CARTOGRAPHY_TABLE);
    //材料工坊
    public static final SlimefunItemStack PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED = createDefaultRecipeMachine("PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED",Material.SEA_LANTERN);
    public static final SlimefunItemStack PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED_REVERSE = createDefaultRecipeMachine("PRE_BUILDINGS_MACHINE_RESOURCE_ADVANCED_REVERSE",Material.PRISMARINE);

    //材料工坊
    public static final SlimefunItemStack INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE = createDefaultRecipeMachine("INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE",Material.FURNACE);
    public static final SlimefunItemStack INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_DEFAULT = createDefaultRecipeMachine("INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_DEFAULT",Material.FURNACE);
    public static final SlimefunItemStack INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA = createDefaultRecipeMachine("INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA",Material.BLAST_FURNACE);
    public static final SlimefunItemStack INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA_DEFAULT = createDefaultRecipeMachine("INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE_ULTRA_DEFAULT",Material.BLAST_FURNACE);


    public static final SlimefunItemStack INGOT_PURE_MACHINE = createDefaultRecipeMachine("INGOT_PURE_MACHINE",Material.SMOKER);


    public static final SlimefunItemStack TWO_TO_MAGIC_GEO_MACHINE = createDefaultRecipeMachine("TWO_TO_MAGIC_GEO_MACHINE",Material.AMETHYST_BLOCK);


    public static final SlimefunItemStack CRUDE_MACHINE_TEST = createDefaultRecipeMachine("CRUDE_MACHINE_TEST",Material.BLAST_FURNACE);



    //普通机器
    public static final SlimefunItemStack PHANTON_SUPPRESSION = createDefaultMachine("PHANTON_SUPPRESSION",Material.LIGHT_BLUE_GLAZED_TERRACOTTA);



    //魔法泥土园
    public static final SlimefunItemStack DIRT_MEAL_MACHINE = createDefaultTemplateMachine("DIRT_MEAL_MACHINE",Material.VERDANT_FROGLIGHT);
    //魔法捕鱼场
    public static final SlimefunItemStack FISHING_MACHINE = createDefaultTemplateMachine("FISHING_MACHINE",Material.ORANGE_GLAZED_TERRACOTTA);




    //发电机列表
    //不稳定的火力发电机
    public static final SlimefunItemStack POWER_FIRE_UNSTABLE = createDefaultPowerMachine("POWER_FIRE_UNSTABLE",Material.CAMPFIRE);
    public static final SlimefunItemStack POWER_FIRE_STABILITY = createDefaultPowerMachine("POWER_FIRE_STABILITY",Material.SOUL_CAMPFIRE);
    public static final SlimefunItemStack POWER_COLOR_EGG_BLOCK = createDefaultPowerMachine("POWER_COLOR_EGG_BLOCK",Material.SNOW_BLOCK);
    public static final SlimefunItemStack POWER_COLOR_EGG_KEY = createDefaultPowerMachine("POWER_COLOR_EGG_KEY",Material.TINTED_GLASS);



    public static final SlimefunItemStack POWER_FISH_ELECTRIC = createDefaultPowerMachine("POWER_FISH_ELECTRIC",Material.SEA_LANTERN);
    public static final SlimefunItemStack FISH_VIVARIUM_EASY = createDefaultPowerMachine("FISH_VIVARIUM_EASY",Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    public static final SlimefunItemStack FISH_VIVARIUM = createDefaultPowerMachine("FISH_VIVARIUM",Material.LIGHT_BLUE_STAINED_GLASS);
    public static final SlimefunItemStack FISH_VIVARIUM_STACK = createDefaultPowerMachine("FISH_VIVARIUM_STACK",Material.PINK_STAINED_GLASS);
    public static final SlimefunItemStack MAGIC_POWER_INPUT_MACHINE = createDefaultPowerMachine("MAGIC_POWER_INPUT_MACHINE",Material.TINTED_GLASS);
    public static final SlimefunItemStack MAGIC_POWER_OUTPUT_MACHINE = createDefaultPowerMachine("MAGIC_POWER_OUTPUT_MACHINE",Material.YELLOW_STAINED_GLASS);



    public static final SlimefunItemStack WHITE_SLOTS_CHEST_53 = createDefaultPowerMachine("WHITE_SLOTS_CHEST_53",Material.CHEST);
    public static final SlimefunItemStack CARGO_FRAGMENT_EXTRACT = createDefaultPowerMachine("CARGO_FRAGMENT_EXTRACT",Material.STICK);



    public static final SlimefunItemStack GEO_MINER_PLUS = createDefaultGeoResource("GEO_MINER_PLUS",Material.MAGENTA_GLAZED_TERRACOTTA);

    public static final SlimefunItemStack FIVE_ELEMENT_MINER = themedVer2Vertical("FIVE_ELEMENT_MINER",new CustomItemStack(CustomHead.getHead("846472b7bd8de52a101584ea2dd7db190d417ca548bb6568379acebdd02b1799")),
            get("GEO.FIVE_ELEMENT_MINER.Name"),getList("GEO.FIVE_ELEMENT_MINER.Lore"));


    public static final SlimefunItemStack RSC_MAGIC_MINER = themedVer2Vertical("RSC_MAGIC_MINER",new CustomItemStack(CustomHead.getHead("846472b7bd8de52a101584ea2dd7db190d417ca548bb6568379acebdd02b1799")),
            get("GEO.RSC_MAGIC_MINER.Name"),getList("GEO.RSC_MAGIC_MINER.Lore"));

    //GEO资源
    public static final SlimefunItemStack GOLD_ELEMENT = createDefaultGeoResource("GOLD_ELEMENT",Material.YELLOW_DYE);
    public static final SlimefunItemStack WOOD_ELEMENT = createDefaultGeoResource("WOOD_ELEMENT",Material.GREEN_DYE);
    public static final SlimefunItemStack WATER_ELEMENT = createDefaultGeoResource("WATER_ELEMENT",Material.LIGHT_BLUE_DYE);
    public static final SlimefunItemStack EARTH_ELEMENT = createDefaultGeoResource("EARTH_ELEMENT",Material.BROWN_DYE);
    public static final SlimefunItemStack FIRE_ELEMENT = createDefaultGeoResource("FIRE_ELEMENT",Material.RED_DYE);
    public static final SlimefunItemStack RSC_MAGIC_REDSTONE = createDefaultGeoResource("RSC_MAGIC_REDSTONE",Material.REDSTONE);
    public static final SlimefunItemStack RSC_MAGIC_COSMIC_DUST = createDefaultGeoResource("RSC_MAGIC_COSMIC_DUST",Material.GLOWSTONE_DUST);
    public static final SlimefunItemStack RSC_MAGIC_SOUL = createDefaultGeoResource("RSC_MAGIC_SOUL",Material.SOUL_LANTERN);

    public static final SlimefunItemStack FIVE_ELEMENT = themedVer2Vertical("FIVE_ELEMENT",new CustomItemStack(CustomHead.getHead("92b4278edb2672c6b32138a0e61e446420caa7fc1508b88e36eaffb14a69206a")),
            get("GEO.FIVE_ELEMENT.Name"),getList("GEO.FIVE_ELEMENT.Lore"));




    public static final SlimefunItemStack PURE_GOLD = createDefaultResourceGlowV2Vertical("PURE_GOLD",Material.YELLOW_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_IRON = createDefaultResourceGlowV2Vertical("PURE_IRON",Material.WHITE_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_COPPER = createDefaultResourceGlowV2Vertical("PURE_COPPER",Material.ORANGE_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_MAGNESIUM = createDefaultResourceGlowV2Vertical("PURE_MAGNESIUM",Material.PINK_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_TIN = createDefaultResourceGlowV2Vertical("PURE_TIN",Material.CYAN_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_SILVER = createDefaultResourceGlowV2Vertical("PURE_SILVER",Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_LEAD = createDefaultResourceGlowV2Vertical("PURE_LEAD",Material.BLUE_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_ALUMINUM = createDefaultResourceGlowV2Vertical("PURE_ALUMINUM",Material.GRAY_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_ZINC = createDefaultResourceGlowV2Vertical("PURE_ZINC",Material.LIME_GLAZED_TERRACOTTA);
    public static final SlimefunItemStack PURE_ELEMENT_INGOT = createDefaultResourceGlowV2Vertical("PURE_ELEMENT_INGOT",Material.NETHERITE_INGOT);
    public static final SlimefunItemStack PURE_ELEMENT_GOLD = createDefaultResourceGlowV2Vertical("PURE_ELEMENT_GOLD",Material.YELLOW_DYE);
    public static final SlimefunItemStack PURE_ELEMENT_WOOD = createDefaultResourceGlowV2Vertical("PURE_ELEMENT_WOOD",Material.GREEN_DYE);
    public static final SlimefunItemStack PURE_ELEMENT_WATER = createDefaultResourceGlowV2Vertical("PURE_ELEMENT_WATER",Material.LIGHT_BLUE_DYE);
    public static final SlimefunItemStack PURE_ELEMENT_FIRE = createDefaultResourceGlowV2Vertical("PURE_ELEMENT_FIRE",Material.RED_DYE);
    public static final SlimefunItemStack PURE_ELEMENT_EARTH = createDefaultResourceGlowV2Vertical("PURE_ELEMENT_EARTH",Material.BROWN_DYE);
    public static final SlimefunItemStack PURE_FIVE_ELEMENT = themedVer2Vertical("PURE_FIVE_ELEMENT",new CustomItemStack(CustomHead.getHead("846472b7bd8de52a101584ea2dd7db190d417ca548bb6568379acebdd02b1799")),
            get("Resource.PURE_FIVE_ELEMENT.Name"),getList("Resource.PURE_FIVE_ELEMENT.Lore"));

    public static final SlimefunItemStack SPEED_ELEMENT_64 = createDefaultResourceGlowV2Vertical("SPEED_ELEMENT_64",Material.HEART_OF_THE_SEA);


    //米哈游-盲盒-崩铁
    public static final SlimefunItemStack MIHOYO_STAR_RAY_MACHINE = createDefaultMihoyoRole("MIHOYO_STAR_RAY_MACHINE",CustomHead.STAR_HEEAD);
    public static final SlimefunItemStack HONKAI_STAR_RAIL_BOX = createDefaultMihoyoRole("HONKAI_STAR_RAIL_BOX",CustomHead.BLUE_GIFT_BOX);
    public static final SlimefunItemStack JING_LIU = createDefaultMihoyoRole("JING_LIU",CustomHead.JING_LIU);
    public static final SlimefunItemStack SILVER_WOLF = createDefaultMihoyoRole("SILVER_WOLF",CustomHead.SILVER_WOLF);
    public static final SlimefunItemStack KAFKA = createDefaultMihoyoRole("KAFKA",CustomHead.KAFKA);
    public static final SlimefunItemStack SUNDAY = createDefaultMihoyoRole("SUNDAY",CustomHead.SUNDAY);
    public static final SlimefunItemStack LUNAE = createDefaultMihoyoRole("LUNAE",CustomHead.LUNAE);
    public static final SlimefunItemStack HUOHUO = createDefaultMihoyoRole("HUOHUO",CustomHead.HUOHUO);





    //空岛系列
    public static final SlimefunItemStack SINGLE_CUBE_ORIGIN = themedVer2Vertical("SINGLE_CUBE_ORIGIN",Material.DIRT,
            get("SKY_BLOCK.SINGLE_CUBE_ORIGIN.Name"),getList("SKY_BLOCK.SINGLE_CUBE_ORIGIN.Lore"));


    public static final SlimefunItemStack SINGLE_CUBE_ORE = themedVer2Vertical("SINGLE_CUBE_ORE",Material.STONE,
            get("SKY_BLOCK.SINGLE_CUBE_ORE.Name"),getList("SKY_BLOCK.SINGLE_CUBE_ORE.Lore"));



    //空岛系列-钻石镐
    public static final SlimefunItemStack SINGLE_DIAMOND_PICKAXE = themedVer2Vertical("SINGLE_DIAMOND_PICKAXE",Material.DIAMOND_PICKAXE,
            get("SKY_BLOCK.SINGLE_DIAMOND_PICKAXE.Name"),getList("SKY_BLOCK.SINGLE_DIAMOND_PICKAXE.Lore"));
    //空岛系列-钻石斧
    public static final SlimefunItemStack SINGLE_DIAMOND_AXE = themedVer2Vertical("SINGLE_DIAMOND_AXE",Material.DIAMOND_AXE,
            get("SKY_BLOCK.SINGLE_DIAMOND_AXE.Name"),getList("SKY_BLOCK.SINGLE_DIAMOND_AXE.Lore"));
    //空岛系列-钻石铲
    public static final SlimefunItemStack SINGLE_DIAMOND_SHOVEL = themedVer2Vertical("SINGLE_DIAMOND_SHOVEL",Material.DIAMOND_SHOVEL,
            get("SKY_BLOCK.SINGLE_DIAMOND_SHOVEL.Name"),getList("SKY_BLOCK.SINGLE_DIAMOND_SHOVEL.Lore"));
    //空岛系列-钻石锄
    public static final SlimefunItemStack SINGLE_DIAMOND_HOE = themedVer2Vertical("SINGLE_DIAMOND_HOE",Material.DIAMOND_HOE,
            get("SKY_BLOCK.SINGLE_DIAMOND_HOE.Name"),getList("SKY_BLOCK.SINGLE_DIAMOND_HOE.Lore"));





    public static SlimefunItemStack createDefaultItem(String id,Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("Items." + id + ".Name"),
                getList("Items." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createDefaultItemGlow(String id,Material material) {
        return themedVer2Vertical(
                id,
                doGlow(material),
                get("Items." + id + ".Name"),
                getList("Items." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createDefaultItemV2Vertical(String id, Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("Items." + id + ".Name"),
                getList("Items." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createDefaultItemGlowV2Vertical(String id, Material material) {
        return themedVer2Vertical(
                id,
                doGlow(material),
                get("Items." + id + ".Name"),
                getList("Items." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createGeneratorGlowV2Vertical(String id, Material material) {
        return themedVer2Vertical(
                id,
                doGlow(material),
                get("GENERATOR." + id + ".Name"),
                getList("GENERATOR." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createDefaultItemGlowGlassV2Vertical(String id, Material material) {
        return themedVer2Vertical(
                id + "_" + material.name(),
                doGlow(material),
                get("Items." + id + ".Name"),
                getList("Items." + id + ".Lore")
        );
    }

    public static SlimefunItemStack createDefaultResourceGlowV2Vertical(String id, Material material) {
        return themedVer2Vertical(
                id,
                doGlow(material),
                get("Resource." + id + ".Name"),
                getList("Resource." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createDefaultResource(String id, Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("Resource." + id + ".Name"),
                getList("Resource." + id + ".Lore")
        );
    }

    public static SlimefunItemStack createDefaultMachine(String id,Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("MACHINE." + id + ".Name"),
                getList("MACHINE." + id + ".Lore")
        );
    }
    public static SlimefunItemStack createDefaultRecipeMachine(String id,Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("RECIPE_MACHINE." + id + ".Name"),
                getList("RECIPE_MACHINE." + id + ".Lore")
        );
    }

    public static SlimefunItemStack createDefaultPowerMachine(String id,Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("POWER_MACHINE." + id + ".Name"),
                getList("POWER_MACHINE." + id + ".Lore")
        );
    }

    public static SlimefunItemStack createDefaultGeoResource(String id,Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("GEO." + id + ".Name"),
                getList("GEO." + id + ".Lore")
        );
    }

    public static SlimefunItemStack createDefaultTemplateMachine(String id,Material material) {
        return themedVer2Vertical(
                id,
                material,
                get("TEMPLATE." + id + ".Name"),
                getList("TEMPLATE." + id + ".Lore")
        );
    }

    public static SlimefunItemStack createDefaultMihoyoRole(String id,CustomHead head) {
        return themedVer2Vertical(
                id,
                head.getItem(),
                get("MIHOYO_ROLE." + id + ".Name"),
                getList("MIHOYO_ROLE." + id + ".Lore")
        );
    }



}
