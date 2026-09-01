package io.Yomicer.magicExpansion.items.misc.fish;

import io.Yomicer.magicExpansion.utils.ColorGradient;
import org.bukkit.Material;

/**
 * 水云间二代鱼注册表（真·鱼物品）。
 * <p>
 * 设计约束（与一代织梦者 Fish 体系隔离）：
 * <ul>
 *   <li>鱼种数据只描述"基准五维向量(主元素倾向) + 稀有度 + 模型材质 + Lore"；</li>
 *   <li>个体属性(五维实值 + 品质系数)在钓获瞬间由 {@link FishAttributeGenerator} 生成并写入 PDC；</li>
 *   <li>通用鱼(isCommon=true)三区共享一套；专用鱼(isCommon=false)只由对应鱼饵钓起。</li>
 *   <li>基因素(Trait): 每个鱼种标注默认基因型(加速种/合成种), 野生钓获沿用默认; 育种子代按遗传规则覆写。</li>
 * </ul>
 * 五维顺序恒为：火·水·雷·风·地，sum=1.0。
 */
public enum Gen2Fish {

    // ==================== 通用鱼(三区共享一套) ====================
    // 火·水·雷·风·地
    YUNYIN("Cloudveil Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.15, 0.15, 0.15}, true, Trait.ACCEL,
            "Hidden beneath the clouds, it flickers in and out of sight at the surface."),
    LANGXI("Wavebreath Fish", Rarity.COMMON, Material.SALMON_BUCKET,
            new double[]{0.05, 0.25, 0.10, 0.50, 0.10}, true, Trait.SYNTH,
            "It follows the waves, leaving calm water behind its tail."),
    HENBO("Rippletrace Fish", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.30, 0.45, 0.15, 0.05}, true, Trait.ACCEL,
            "Its passing leaves a brief ring of electric light across the water."),
    WUYU("Mist Isle Fish", Rarity.RARE, Material.TROPICAL_FISH_BUCKET,
            new double[]{0.05, 0.20, 0.10, 0.20, 0.45}, true, Trait.SYNTH,
            "It rests around misty reefs, its scales darkened by night dew."),

    // ==================== 芦花钓 · 芦雪(专用) ====================
    LUXUE("Reed-Snow Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.55, 0.10}, false, Trait.ACCEL,
            "It appears quietly where reed flowers fall like snow."),
    LUWEIYING("Reed Shadow", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.50, 0.05, 0.25, 0.15}, false, Trait.SYNTH,
            "It hides beneath reeds, vanishing from people and appearing with the wind."),

    // ==================== 芦花钓 · 苇露(专用) ====================
    WEILU("Reed-Dew Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.55, 0.05, 0.15, 0.20}, false, Trait.SYNTH,
            "It gathers beneath dew-covered reeds, sparkling in the morning light."),
    CHENLULIN("Morning Dewscale", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.50, 0.25, 0.15, 0.05}, false, Trait.ACCEL,
            "Morning dew and sunrise turn its scales crystal-bright."),

    // ==================== 芦花钓 · 白露(专用) ====================
    BAILU("White Dew Fish", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.15, 0.50}, false, Trait.ACCEL,
            "It appears when white dew settles on the river, with frost-pale scales."),
    SHUANGBAILIAN("Frostwhite Carp", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.60, 0.05, 0.10, 0.20}, false, Trait.SYNTH,
            "Frost-white and weightless in motion, it drifts like a cloud."),

    // ==================== 芦花钓 · 芦芽(专用) ====================
    LUYA("Reed-Sprout Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.50, 0.15}, false, Trait.SYNTH,
            "Drawn by fresh reed shoots, it carries the feeling of spring."),
    XINHUANG("New Bamboo Perch", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.20, 0.10, 0.15, 0.50}, false, Trait.ACCEL,
            "It lives beside new bamboo, wearing a cool green cast."),

    // ==================== 寒江雪 · 凝霜(专用) ====================
    SHUANGJIAO("Frostfin", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.60, 0.05, 0.20, 0.10}, false, Trait.ACCEL,
            "Born in the first frost of the river, its scales carry fine ice."),
    NINGBOJING("Stillwave Crystal", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.55, 0.05, 0.10, 0.25}, false, Trait.SYNTH,
            "Cold waves crystallize around it as though the water pauses."),

    // ==================== 寒江雪 · 落絮(专用) ====================
    XUXUE("Drifting Snow Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.55, 0.10}, false, Trait.SYNTH,
            "It drifts into the cold river with falling flakes, almost weightless."),
    CHENXULIN("Sunken Flakefin", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.30, 0.05, 0.10, 0.50}, false, Trait.ACCEL,
            "When the drifting snow ends, it sinks carrying the river winter with it."),

    // ==================== 寒江雪 · 冰魄(专用) ====================
    BINGPO("Ice-Soul Fish", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.65, 0.05, 0.10, 0.15}, false, Trait.ACCEL,
            "Its icy gaze carries a chill said to have rested in the river for ages."),
    XUANBINGLI("Dark-Ice Carp", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.60, 0.25, 0.05, 0.05}, false, Trait.SYNTH,
            "Dark ice coats this carp, flashing as it moves."),

    // ==================== 寒江雪 · 初霁(专用) ====================
    JIGUANG("Clearsky Gleam Fish", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.45, 0.30, 0.05, 0.15, 0.05}, false, Trait.SYNTH,
            "After snow, clear sunlight glints across its scales with hidden warmth."),
    NUANYANGLIN("Warm Sunscale", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.50, 0.20, 0.05, 0.15, 0.10}, false, Trait.ACCEL,
            "It follows the first warm sunlight returning to the water."),

    // ==================== 寒江雪 · 垂纶(专用) ====================
    SUODIAO("Raincloak Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.05, 0.15, 0.25}, false, Trait.ACCEL,
            "It circles lonely fishing spots as if keeping watch over the winter river."),
    GUZHOU("Lone Boat Shadow", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.30, 0.05, 0.50, 0.10}, false, Trait.SYNTH,
            "Its shadow follows a lone boat beneath distant evening snow."),

    // ==================== 细雨·斜风 · 风丝(专用) ====================
    YOUSI("Wispthread Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.20, 0.10, 0.60, 0.05}, false, Trait.SYNTH,
            "It follows threads of wind, pulling a fine ripple behind it."),
    FENGXIAO("Wind-Silk Scale", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.15, 0.20, 0.55, 0.05}, false, Trait.ACCEL,
            "Its silk-thin scales dance whenever the wind passes."),

    // ==================== 细雨·斜风 · 烟雨(专用) ====================
    YANYU("Mist-Rain Carp", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.05, 0.30, 0.10}, false, Trait.ACCEL,
            "It breaks through misty rain with scales colored by the river fog."),
    WUYINLU("Mistveil Perch", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.55, 0.20, 0.15, 0.05}, false, Trait.SYNTH,
            "It appears only beneath heavy mist and never stays where expected."),

    // ==================== 细雨·斜风 · 涟白(专用) ====================
    LIANBAI("White Ripple Fish", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.60, 0.20, 0.10, 0.05}, false, Trait.SYNTH,
            "A pale ring of light crosses the water whenever it surfaces."),
    YINGBO("Waveglint Scale", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.15, 0.25, 0.05}, false, Trait.ACCEL,
            "Fine rain weaves across its scales and the reflected waves."),

    // ==================== 细雨·斜风 · 晓风(专用) ====================
    XIAOFENG("Dawnwind Fish", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.55, 0.10}, false, Trait.ACCEL,
            "Schools emerge with the dawn wind, carrying the scent of shore grass."),
    POXIAOLIN("Daybreak Scale", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.45, 0.10, 0.05, 0.35, 0.05}, false, Trait.SYNTH,
            "A warm glimmer touches its scales just before sunrise."),

    // ==================== 细雨·斜风 · 斜影(专用) ====================
    XIEYING("Slantshadow Fish", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.50, 0.20, 0.10, 0.15}, false, Trait.SYNTH,
            "It blends with broken reflections until fish and shadow are hard to separate."),
    ANLIU("Undercurrent Bream", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.35, 0.10, 0.05, 0.45}, false, Trait.ACCEL,
            "It waits in underwater currents for shadows to fall across the surface.");

    /** 稀有度(二代鱼独立，不依赖织梦者 Fish.Rarity) */
    public enum Rarity {
        COMMON("§fCommon", "§f", Material.COD_BUCKET),
        RARE("§eRare", "§e", Material.PUFFERFISH_BUCKET),
        EPIC("§dEpic", "§d", Material.AXOLOTL_BUCKET);
        private final String displayName;
        private final String colorCode;
        private final Material defaultMaterial;
        Rarity(String displayName, String colorCode, Material defaultMaterial) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.defaultMaterial = defaultMaterial;
        }
        public String getDisplayName() { return displayName; }
        public String getColorCode() { return colorCode; }
        public Material getDefaultMaterial() { return defaultMaterial; }
    }

    /** 基因型(决定功能流向): 加速种 → 机器加速消耗; 合成种 → 育种/附魔合成消耗 */
    public enum Trait {
        ACCEL("§dAcceleration", "§d"),
        SYNTH("§aSynthesis", "§a");
        private final String displayName;
        private final String colorCode;
        Trait(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }
        public String getDisplayName() { return displayName; }
        public String getColorCode() { return colorCode; }
    }

    private final String displayName;
    private final Rarity rarity;
    private final Material material;
    private final double[] baseElements; // 火·水·雷·风·地, sum=1
    private final boolean common;        // true=通用鱼(进共享池)
    private final Trait trait;           // 鱼种默认基因型(野生钓获沿用), 育种子代按遗传规则覆写
    private final String lore;

    Gen2Fish(String displayName, Rarity rarity, Material material,
             double[] baseElements, boolean common, Trait trait, String lore) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.material = material;
        this.baseElements = baseElements;
        this.common = common;
        this.trait = trait;
        this.lore = lore;
    }

    public String getDisplayName() { return displayName; }
    public String getId() { return "GEN2_" + name(); }
    public Rarity getRarity() { return rarity; }
    public Material getMaterial() { return material; }
    public double[] getBaseElements() { return baseElements; }
    public boolean isCommon() { return common; }
    public Trait getDefaultTrait() { return trait; }
    public String getLore() { return lore; }

    /** 渐变着色后的显示名 */
    public String getGradientName() {
        return ColorGradient.getGradientNameVer2(displayName);
    }

    /** 按 id(GEN2_xxx) 反查鱼种，非法/未知返回 null */
    public static Gen2Fish byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id.replace("GEN2_", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}