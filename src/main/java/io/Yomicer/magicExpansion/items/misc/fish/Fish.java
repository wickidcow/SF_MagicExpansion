package io.Yomicer.magicExpansion.items.misc.fish;

import io.Yomicer.magicExpansion.utils.ColorGradient;

import java.util.Random;

public enum Fish {
    SanWenFish(Rarity.COMMON.colorCode+"Salmon", 5.0, 31.0, Rarity.COMMON,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§B§F§A§A§D§0It has unusual magical traits."),
            ("§x§F§F§3§2§C§EWeight range: 5.0 - 31.0 kg.")
    ),
    XueFish(Rarity.COMMON.colorCode+"Cod", 2.0, 96.0, Rarity.COMMON,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§B§F§A§A§D§0It has unusual magical traits."),
            ("§x§F§F§3§2§C§EWeight range: 2.0 - 96.0 kg.")
    ),
    HeTun(Rarity.UNCOMMON.colorCode+"Pufferfish", 0.2, 1.5, Rarity.UNCOMMON,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§B§F§A§A§D§0It has unusual magical traits."),
            ("§x§F§F§3§2§C§EWeight range: 0.2 - 1.5 kg.")
    ),
    ReDaiFish(Rarity.RARE.colorCode+"Tropical Fish", 0.1, 4, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§B§F§A§A§D§0It has unusual magical traits."),
            ("§x§F§F§3§2§C§EWeight range: 0.1 - 4 kg.")
    ),

    TestFish(Rarity.RARE.colorCode+"Test Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§B§0§C§5§D§0It has unusual magical traits."),
            ("§x§C§E§8§7§A§8Weight range: 1.0 - 64.0 kg."),
            ("§x§F§F§3§2§C§EA rare and unusual catch.")
    ),


    /**
     * 矿粉鱼-稀有
     */

    CopperDustFish(Rarity.RARE.colorCode+"Copper Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Copper."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Copper."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    GoldDustFish(Rarity.RARE.colorCode + "Gold Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Gold."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Gold."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    IronDustFish(Rarity.RARE.colorCode + "Iron Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Iron."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Iron."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    TinDustFish(Rarity.RARE.colorCode + "Tin Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Tin."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Tin."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    SilverDustFish(Rarity.RARE.colorCode + "Silver Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Silver."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Silver."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    AluminumDustFish(Rarity.RARE.colorCode + "Aluminum Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Aluminum."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Aluminum."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    LeadDustFish(Rarity.RARE.colorCode + "Lead Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Lead."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Lead."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    ZincDustFish(Rarity.RARE.colorCode + "Zinc Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Zinc."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Zinc."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    MagnesiumDustFish(Rarity.RARE.colorCode + "Magnesium Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Magnesium."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Magnesium."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),


    /**
     * 矿物鱼-稀有
     */

    // [CoalFish]用于生产:煤炭(Coal)
    CoalFish(Rarity.RARE.colorCode + "Coal Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Coal."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Coal."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [EmeraldFish]用于生产:绿宝石(Emerald)
    EmeraldFish(Rarity.RARE.colorCode + "Emerald Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Emerald."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Emerald."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [LapisFish]用于生产:青金石(Lapis Lazuli)
    LapisFish(Rarity.RARE.colorCode + "Lapis Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Lapis."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Lapis."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [DiamondFish]用于生产:钻石(Diamond)
    DiamondFish(Rarity.RARE.colorCode + "Diamond Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Diamond."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Diamond."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [QuartzFish]用于生产:下界石英(Nether Quartz)
    QuartzFish(Rarity.RARE.colorCode + "Quartz Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Quartz."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Quartz."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [AmethystFish]用于生产:紫水晶碎片(Amethyst Shard)
    AmethystFish(Rarity.RARE.colorCode + "Amethyst Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Amethyst."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Amethyst."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [IronFish]用于生产:铁锭(Iron Ingot)
    IronFish(Rarity.RARE.colorCode + "Iron Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Iron."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Iron."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [GoldFish]用于生产:金锭(Gold Ingot)
    GoldFish(Rarity.RARE.colorCode + "Gold Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Gold."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Gold."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [CopperFish]用于生产:铜锭(Copper Ingot)
    CopperFish(Rarity.RARE.colorCode + "Copper Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Copper."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Copper."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [脉动鱼]用于生产:红石粉(Redstone Dust)
    RedstoneFish(Rarity.RARE.colorCode + "Redstone Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Redstone."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Redstone."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [NetheriteFish]用于生产:下界合金锭(Netherite Ingot)
    NetheriteFish(Rarity.RARE.colorCode + "Netherite Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Netherite."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Netherite."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [GlowStoneFish]用于生产:萤石粉(Glowstone Dust)
    GlowStoneDustFish(Rarity.RARE.colorCode + "Glow Stone Dust Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Glow Stone."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Glow Stone."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [塑灵鱼]用于生产:塑料纸(Plastic Paper)
    ShuLingYu(Rarity.RARE.colorCode + "Spirit-Mold Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Spirit-Mold."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Spirit-Mold."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [铀核鱼]用于生产:铀锭(Uranium Ingot)
    UraniumFish(Rarity.RARE.colorCode + "Uranium Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Uranium."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Uranium."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [油岩鱼]用于生产:原油桶(Crude Oil Barrel)
    OilRockFish(Rarity.RARE.colorCode + "Oil Rock Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Oil Rock."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Oil Rock."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [泡晶鱼]用于生产:起泡锭(Foamium Ingot)
    FoamCrystalFish(Rarity.EPIC.colorCode + "Foam Crystal Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Foam Crystal."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Foam Crystal."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [黑曜鱼]用于生产:黑金刚石(Black Diamond)
    BlackDiamondFish(Rarity.EPIC.colorCode + "Black Diamond Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Black Diamond."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Black Diamond."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [酸晶鱼]用于生产:硫酸盐(Sulfate Crystals)
    SulfateFish(Rarity.RARE.colorCode + "Sulfate Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Sulfate."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Sulfate."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [晶鳞鱼]用于生产:硅(Silicon)
    SiliconFish(Rarity.RARE.colorCode + "Silicon Fish", 1.0, 64.0, Rarity.RARE,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§A§A§C§D§D§0Its magical traits are linked to Silicon."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§9§C§5§2§8§BSpecial output: Silicon."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),
    // [灵咒鱼]用于生产:附魔瓶(Bottled Enchanting)
    EnchantedBottleFish(Rarity.EPIC.colorCode + "Enchanted Bottle Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Enchanted Bottle."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Enchanted Bottle."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),




    //[合金锭鱼]

    // [合金灵鱼]用于生产:强化合金锭
    ReinforcedAlloyFish(Rarity.EPIC.colorCode + "Reinforced Alloy Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Reinforced Alloy."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Reinforced Alloy."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [硬化灵鱼]用于生产:硬化金属
    HardenedMetalFish(Rarity.EPIC.colorCode + "Hardened Metal Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Hardened Metal."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Hardened Metal."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [大马士革灵鱼]用于生产:大马士革钢锭
    DamascusSoulFish(Rarity.EPIC.colorCode + "Damascus Soul Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Damascus Soul."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Damascus Soul."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [钢魄鱼]用于生产:钢锭
    SteelSoulFish(Rarity.EPIC.colorCode + "Steel Soul Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Steel Soul."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Steel Soul."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [青铜古影鱼]用于生产:青铜锭
    BronzeAncientFish(Rarity.EPIC.colorCode + "Bronze Ancient Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Bronze Ancient."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Bronze Ancient."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [硬铝天翔鱼]用于生产:硬铝锭
    HardlightAluFish(Rarity.EPIC.colorCode + "Hardlight Alu Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Hardlight Alu."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Hardlight Alu."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [银铜灵鱼]用于生产:银铜合金锭
    SilverCopperFish(Rarity.EPIC.colorCode + "Silver Copper Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Silver Copper."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Silver Copper."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [黄铜鸣音鱼]用于生产:黄铜锭
    BrassResonanceFish(Rarity.EPIC.colorCode + "Brass Resonance Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Brass Resonance."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Brass Resonance."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [铝黄铜灵鱼]用于生产:铝黄铜锭
    AluminumBrassFish(Rarity.EPIC.colorCode + "Aluminum Brass Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Aluminum Brass."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Aluminum Brass."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [铝青铜灵鱼]用于生产:铝青铜锭
    AluminumBronzeFish(Rarity.EPIC.colorCode + "Aluminum Bronze Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Aluminum Bronze."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Aluminum Bronze."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [科林斯青铜灵鱼]用于生产:科林斯青铜锭
    CorinthianBronzeFish(Rarity.EPIC.colorCode + "Corinthian Bronze Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Corinthian Bronze."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Corinthian Bronze."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [焊锡灵鱼]用于生产:焊锡锭
    SolderFlowFish(Rarity.EPIC.colorCode + "Solder Flow Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Solder Flow."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Solder Flow."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [镍魄鱼]用于生产:镍锭
    NickelSpiritFish(Rarity.EPIC.colorCode + "Nickel Spirit Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Nickel Spirit."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Nickel Spirit."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [钴焰鱼]用于生产:钴锭
    CobaltFlameFish(Rarity.EPIC.colorCode + "Cobalt Flame Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Cobalt Flame."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Cobalt Flame."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [硅铁灵鱼]用于生产:硅铁
    SiliconIronFish(Rarity.EPIC.colorCode + "Silicon Iron Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Silicon Iron."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Silicon Iron."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [碳魂鱼]用于生产:碳块
    CarbonSoulFish(Rarity.EPIC.colorCode + "Carbon Soul Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Carbon Soul."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Carbon Soul."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [镀金灵鱼]用于生产:镀金铁锭
    GildedIronFish(Rarity.EPIC.colorCode + "Gilded Iron Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Gilded Iron."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Gilded Iron."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [红石合金灵鱼]用于生产:红石合金锭
    RedstoneAlloyFish(Rarity.EPIC.colorCode + "Redstone Alloy Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Redstone Alloy."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Redstone Alloy."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [镎影鱼]用于生产:镎
    NeptuniumShadowFish(Rarity.EPIC.colorCode + "Neptunium Shadow Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Neptunium Shadow."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Neptunium Shadow."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),

    // [钚心鱼]用于生产:钚
    PlutoniumCoreFish(Rarity.EPIC.colorCode + "Plutonium Core Fish", 1.0, 64.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0Its magical traits are linked to Plutonium Core."),
            ("§x§B§F§B§A§D§0Weight range: 1.0 - 64.0 kg."),
            ("§x§A§D§A§2§9§ESpecial output: Plutonium Core."),
            ("§x§C§B§4§2§A§6Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),





    LegendaryLuFish(Rarity.LEGENDARY.colorCode+"Legendary Bass", 2.0, 31.0, Rarity.LEGENDARY,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§B§0§C§5§D§0It has unusual magical traits."),
            ("§x§C§E§8§7§A§8Weight range: 2.0 - 31.0 kg."),
            ("§x§F§F§3§2§C§EA rare and unusual catch.")
    ),
    LegendaryEelFish(Rarity.LEGENDARY.colorCode+"Legendary Eel", 666.666, 888.888, Rarity.LEGENDARY,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§8§A§E§0§D§1It has unusual magical traits."),
            ("§x§A§A§C§D§D§0Weight range: 666.666 - 888.888 kg."),
            ("§x§B§7§C§2§D§0A rare and unusual catch."),
            ("§x§B§F§B§A§D§0Use it in compatible fishing or generator recipes."),
            ("§x§B§5§9§F§B§0Use it in compatible fishing or generator recipes."),
            ("§x§9§C§5§2§8§BUse it in compatible fishing or generator recipes."+ Long.MAX_VALUE +"Use it in compatible fishing or generator recipes."),
            ("§x§B§1§4§2§9§AUse it in compatible fishing or generator recipes."),
            ("§x§C§6§3§2§A§9Use it in compatible fishing or generator recipes."),
            "Use it in compatible fishing or generator recipes.",
            ("§x§D§B§2§2§B§8Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    ),


    MYSTIC_EEL(Rarity.EPIC.colorCode+"Mystic Eel", 2.0, 21.0, Rarity.EPIC,
            ("§x§6§B§E§E§D§1A magical fish from the MagicExpansion fishing system."),
            ("§x§9§5§C§4§D§0It has unusual magical traits."),
            ("§x§B§F§B§A§D§0Weight range: 2.0 - 21.0 kg."),
            ("A rare and unusual catch."),
            ("Use it in compatible fishing or generator recipes."),
            ("§x§F§F§3§2§C§EUse it in compatible fishing or generator recipes.")
    );

    private final String displayName;
    private final double minWeight;
    private final double maxWeight;
    private final Rarity rarity;
    private final String[] loreLines;

    Fish(String displayName, double minWeight, double maxWeight, Rarity rarity, String... loreLines) {
        this.displayName = displayName;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.rarity = rarity;
        this.loreLines = loreLines;
    }

    // Getter 省略(同上)
    public String getDisplayName() { return displayName; }
    public double getMinWeight() { return minWeight; }
    public double getMaxWeight() { return maxWeight; }
    public Rarity getRarity() { return rarity; }
    public String[] getLoreLines() { return loreLines.clone(); }



    public static Fish fromString(String name) {
        try {
            return Fish.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null; // 或抛出异常
        }
    }


    /**
     * 按"越轻越稀有"原则生成重量
     * 使用偏态分布:更大概率生成接近 minWeight 的小重量
     */
    public double rollWeight() {
        Random random = new Random();
        double unit = 1 - random.nextDouble();
        double skewed = Math.pow(unit, 3.9);
        return minWeight + (skewed * (maxWeight - minWeight));
    }

    public double rollWeightNew() {
        Random random = new Random();
        double unit = 1 - random.nextDouble();
        double skewed = Math.pow(unit, 4.5);
        double rand = random.nextDouble();
        double multiplier;
        if (rand < 0.40) {
            multiplier = 0.3;
        } else if (rand < 0.80) {
            multiplier = 0.7;
        } else if (rand < 0.90) {
            multiplier = 0.95;
        } else if (rand < 0.98) {
            multiplier = 0.97;
        } else {
            multiplier = 1.0;
        }
        return minWeight + (skewed * (maxWeight - minWeight)*multiplier);
    }
    public double rollWeightAdvanced() {
        Random random = new Random();
        double unit = 1 - random.nextDouble();
        double skewed = Math.pow(unit, 3.1);
        double rand = random.nextDouble();
        double multiplier;
        if (rand < 0.20) {
            multiplier = 0.3;
        } else if (rand < 0.40) {
            multiplier = 0.8;
        } else if (rand < 0.70) {
            multiplier = 0.97;
        } else if (rand < 0.80) {
            multiplier = 0.99;
        } else {
            multiplier = 1.0;
        }
        return minWeight + (skewed * (maxWeight - minWeight)*multiplier);
    }

    public double rollWeightUltra() {
        Random random = new Random();
        double unit = 1 - random.nextDouble();
        double skewed = Math.pow(unit, 2.1);
        double rand = random.nextDouble();
        double multiplier;
        if (rand < 0.10) {
            multiplier = 0.3;
        } else if (rand < 0.30) {
            multiplier = 0.8;
        } else if (rand < 0.60) {
            multiplier = 0.97;
        } else if (rand < 0.70) {
            multiplier = 0.99;
        } else {
            multiplier = 1.0;
        }
        return minWeight + (skewed * (maxWeight - minWeight)*multiplier);
    }

    /**
     * 计算重量在范围内的"归一化百分比"(0.0 ~ 1.0)
     * 注意:值越小表示越轻
     */
    public double getWeightPercent(double weight) {
        if (weight >= maxWeight) return 1.0;
        if (weight <= minWeight) return 0.0;
        return (weight - minWeight) / (maxWeight - minWeight);
    }

    /**
     * 根据重量判断"重量稀有度"
     */
    public WeightRarity getWeightRarity(double weight) {
        double percent = getWeightPercent(weight);
        return WeightRarity.fromPercent(percent);
    }

    // ======================
    // 内部枚举:重量稀有度(独立于基础稀有度)
    // ======================
    public enum WeightRarity {
        COMMON_FISH("§fCommon Fish", "§f",1),
        RARE_FISH("§eRare Fish", "§e",7),
        SUPER_RARE_FISH("§bUltra Rare Fish", "§b",15),
        MAX_WEIGHT_FISH("§c§lFish", "§c§l", 9999);

        private final String displayName;
        private final String colorCode;
        private final int multiplier;

        WeightRarity(String displayName, String colorCode, int multiplier) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.multiplier = multiplier;
        }

        public String getDisplayName() { return displayName; }
        public String getColorCode() { return colorCode; }
        public int getMultiplier() {
            return multiplier;
        }

        public static int getMultiplierByName(String name) {
            if (name == null) {
                return 1; // 默认倍率
            }
            try {
                WeightRarity rarity = WeightRarity.valueOf(name);
                return rarity.getMultiplier();
            } catch (IllegalArgumentException e) {
                return 1; // 名称无效,默认普通
            }
        }

        /**
         * 根据归一化百分比(0.0~1.0)判断重量稀有度
         * 注意:越轻越稀有 → 百分比越小越稀有
         */
        public static WeightRarity fromPercent(double percent) {
            if (percent == 1){
                return MAX_WEIGHT_FISH;  //满重量
            }
            else if (percent > 0.93) {
                return SUPER_RARE_FISH;  // 最重的 7% → 超级稀有
            } else if (percent > 0.85) {
                return RARE_FISH;        // 中间的 8% → 稀有鱼
            } else {
                return COMMON_FISH;      // 最轻的 85% → 普通鱼
            }
        }
    }



    // ======================
    // 基础稀有度(原系统)
    // ======================
    public enum Rarity {
        COMMON("§fCommon", "§f"),
        UNCOMMON("§a", "§a"),
        RARE("§bRare", "§b"),
        RARE_POOL_DUST("§bRare -POOL- Mineral Dust", "§b"),
        RARE_POOL_ORE("§bRare -POOL-", "§b"),
        RARE_POOL_INDUSTRY("§bRare -POOL- Material", "§b"),
        EPIC("§dEpic", "§d"),
        EPIC_POOL_INDUSTRY("§dEpic -POOL- Material", "§d"),
        EPIC_POOL_ALLOY_INGOT("§dEpic -POOL- Gold", "§d"),
        LEGENDARY("§cLegendary", "§c"),
        LEGENDARY_EEL("§cLegendary -", "§c"),
        MYTHICAL("§eMythic", "§e");


        private final String displayName;
        private final String colorCode;

        Rarity(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() { return displayName; }
        public String getColorCode() { return colorCode; }
    }
}
