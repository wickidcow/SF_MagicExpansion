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
    YUNYIN("云隐鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.15, 0.15, 0.15}, true, Trait.ACCEL,
            "藏身云底，身形似有若无，于水面时隐时现"),
    LANGXI("浪息鱼", Rarity.COMMON, Material.SALMON_BUCKET,
            new double[]{0.05, 0.25, 0.10, 0.50, 0.10}, true, Trait.SYNTH,
            "逐浪而栖，尾鳍过处，波澜渐息"),
    HENBO("痕波鱼", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.30, 0.45, 0.15, 0.05}, true, Trait.ACCEL,
            "游过即留一圈电光水痕，转瞬即逝"),
    WUYU("雾屿鱼", Rarity.RARE, Material.TROPICAL_FISH_BUCKET,
            new double[]{0.05, 0.20, 0.10, 0.20, 0.45}, true, Trait.SYNTH,
            "栖于雾绕的礁屿，鳞染夜露，沉静如石"),

    // ==================== 芦花钓 · 芦雪(专用) ====================
    LUXUE("芦雪鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.55, 0.10}, false, Trait.ACCEL,
            "芦花似雪纷落处，此鱼悄然而至"),
    LUWEIYING("芦苇影", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.50, 0.05, 0.25, 0.15}, false, Trait.SYNTH,
            "影藏芦苇丛底，人近则潜，风起则现"),

    // ==================== 芦花钓 · 苇露(专用) ====================
    WEILU("苇露鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.55, 0.05, 0.15, 0.20}, false, Trait.SYNTH,
            "栖于苇叶凝露处，晨光中背沾露珠"),
    CHENLULIN("晨露鳞", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.50, 0.25, 0.15, 0.05}, false, Trait.ACCEL,
            "晓露垂苇，鳞映朝光，晶莹剔透"),

    // ==================== 芦花钓 · 白露(专用) ====================
    BAILU("白露鱼", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.15, 0.50}, false, Trait.ACCEL,
            "白露凝江时现世，鳞色皎白如霜"),
    SHUANGBAILIAN("霜白鲢", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.60, 0.05, 0.10, 0.20}, false, Trait.SYNTH,
            "露凝为霜，鲢身素白，游若浮云"),

    // ==================== 芦花钓 · 芦芽(专用) ====================
    LUYA("芦芽鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.50, 0.15}, false, Trait.SYNTH,
            "循初生芦芽香气而来，尚带春意"),
    XINHUANG("新篁鲈", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.20, 0.10, 0.15, 0.50}, false, Trait.ACCEL,
            "栖于新篁水畔，鳞染竹色，清冷自持"),

    // ==================== 寒江雪 · 凝霜(专用) ====================
    SHUANGJIAO("霜鲛", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.60, 0.05, 0.20, 0.10}, false, Trait.ACCEL,
            "生于霜色初凝的江水，鳞面皆结细霜"),
    NINGBOJING("凝波晶", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.55, 0.05, 0.10, 0.25}, false, Trait.SYNTH,
            "寒波凝成冰晶，冻结处鱼影驻足"),

    // ==================== 寒江雪 · 落絮(专用) ====================
    XUXUE("絮雪鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.55, 0.10}, false, Trait.SYNTH,
            "随落絮飘入寒江，轻若无物"),
    CHENXULIN("沉絮鳞", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.30, 0.05, 0.10, 0.50}, false, Trait.ACCEL,
            "絮尽而后沉，负满江雪色归于江底"),

    // ==================== 寒江雪 · 冰魄(专用) ====================
    BINGPO("冰魄鱼", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.65, 0.05, 0.10, 0.15}, false, Trait.ACCEL,
            "魄寄水心千年，寒气透骨，鱼目如冰"),
    XUANBINGLI("玄冰鲤", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.60, 0.25, 0.05, 0.05}, false, Trait.SYNTH,
            "鲤身覆一层玄冰，游动时粼光如练"),

    // ==================== 寒江雪 · 初霁(专用) ====================
    JIGUANG("霁光鱼", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.45, 0.30, 0.05, 0.15, 0.05}, false, Trait.SYNTH,
            "雪后初晴，霁光映鳞，暖意暗藏"),
    NUANYANGLIN("暖阳鳞", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.50, 0.20, 0.05, 0.15, 0.10}, false, Trait.ACCEL,
            "借一缕晴阳回暖水面，游鱼竞逐"),

    // ==================== 寒江雪 · 垂纶(专用) ====================
    SUODIAO("蓑钓鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.05, 0.15, 0.25}, false, Trait.ACCEL,
            "惯于蓑衣孤钓处盘旋，似与人同守寒江"),
    GUZHOU("孤舟影", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.30, 0.05, 0.50, 0.10}, false, Trait.SYNTH,
            "一竿垂纶、千山暮雪，影随孤舟而远"),

    // ==================== 细雨·斜风 · 风丝(专用) ====================
    YOUSI("游丝鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.20, 0.10, 0.60, 0.05}, false, Trait.SYNTH,
            "循风丝而来，牵一尾涟漪入水"),
    FENGXIAO("风绡鳞", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.15, 0.20, 0.55, 0.05}, false, Trait.ACCEL,
            "鳞薄如绡，风过即舞，似无形之丝"),

    // ==================== 细雨·斜风 · 烟雨(专用) ====================
    YANYU("烟雨鲤", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.05, 0.30, 0.10}, false, Trait.ACCEL,
            "惯破烟雨而出，鳞染一江雾色"),
    WUYINLU("雾隐鲈", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.55, 0.20, 0.15, 0.05}, false, Trait.SYNTH,
            "雾锁长天时方见其形，行藏莫测"),

    // ==================== 细雨·斜风 · 涟白(专用) ====================
    LIANBAI("涟白鱼", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.60, 0.20, 0.10, 0.05}, false, Trait.SYNTH,
            "波心一圈白光浮过，游鱼皆探首"),
    YINGBO("映波鳞", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.50, 0.15, 0.25, 0.05}, false, Trait.ACCEL,
            "细雨斜织，鳞光与波影交映"),

    // ==================== 细雨·斜风 · 晓风(专用) ====================
    XIAOFENG("晓风鱼", Rarity.COMMON, Material.COD_BUCKET,
            new double[]{0.05, 0.25, 0.05, 0.55, 0.10}, false, Trait.ACCEL,
            "破晓风起时结群而出，衔岸草香"),
    POXIAOLIN("破晓鳞", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.45, 0.10, 0.05, 0.35, 0.05}, false, Trait.SYNTH,
            "天色初明，一缕暖光先于点过水面"),

    // ==================== 细雨·斜风 · 斜影(专用) ====================
    XIEYING("斜影鱼", Rarity.EPIC, Material.AXOLOTL_BUCKET,
            new double[]{0.05, 0.50, 0.20, 0.10, 0.15}, false, Trait.SYNTH,
            "疏影落水、虚实难辨，诱鱼往往在影"),
    ANLIU("暗流鲷", Rarity.RARE, Material.PUFFERFISH_BUCKET,
            new double[]{0.05, 0.35, 0.10, 0.05, 0.45}, false, Trait.ACCEL,
            "栖于水面下暗流，静候落影入水");

    /** 稀有度(二代鱼独立，不依赖织梦者 Fish.Rarity) */
    public enum Rarity {
        COMMON("§f凡品", "§f", Material.COD_BUCKET),
        RARE("§e奇珍", "§e", Material.PUFFERFISH_BUCKET),
        EPIC("§d史诗", "§d", Material.AXOLOTL_BUCKET);
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
        ACCEL("§d加速种", "§d"),
        SYNTH("§a合成种", "§a");
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