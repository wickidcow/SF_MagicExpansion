package io.Yomicer.magicExpansion.items.misc.fish;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 二代鱼个体属性生成器。
 * <p>
 * 职责：钓获二代鱼时，依据鱼竿稀有度系数(weightBoost)决定该鱼个体的
 * <ul>
 *   <li>五维元素实值（越好的鱼竿，越贴近鱼种基准主元素、方差越小→纯度越高）；</li>
 *   <li>品质系数（0.7~2.5 · 由鱼竿系数放大优质区间）。</li>
 * </ul>
 * 属性全部写入物品 PDC，便于后续读取与展示。五维恒按 火·水·雷·风·地 顺序，sum=1。
 */
public class FishAttributeGenerator {

    // 二代鱼专用 PDC key（与织梦者 FishKeys 隔离，避免数据串扰）
    public static final NamespacedKey GEN2_TYPE = new NamespacedKey(MagicExpansion.getInstance(), "gen2_type");
    public static final NamespacedKey GEN2_ELEMENTS = new NamespacedKey(MagicExpansion.getInstance(), "gen2_elements");
    public static final NamespacedKey GEN2_QUALITY = new NamespacedKey(MagicExpansion.getInstance(), "gen2_quality");
    // 第一层补全: 个体基因型(加速种/合成种) 与 亲本追溯(父母鱼种ID, 逗号分隔)
    public static final NamespacedKey GEN2_TRAIT = new NamespacedKey(MagicExpansion.getInstance(), "gen2_trait");
    public static final NamespacedKey GEN2_PARENTS = new NamespacedKey(MagicExpansion.getInstance(), "gen2_parents");

    /** 鱼竿稀有度系数 → 五维纯度指数(越大越贴近主元素)与数值手感 */
    private static final double[] PURITY_EXP = {1.5, 2.0, 2.5, 3.0, 3.6}; // 青竹/芦花/寒江雪/细雨·斜风/太公

    /**
     * 生成带属性的二代鱼成品(野生钓获):
     * 个体基因型 = 鱼种默认基因型, 无亲本。
     *
     * @param type       鱼种
     * @param boostIndex 鱼竿稀有度档位(0=最低,4=最高)，见 {@link #PURITY_EXP} 下标
     * @return 带 PDC 属性的成品鱼物品
     */
    public static ItemStack generate(Gen2Fish type, int boostIndex) {
        return generate(type, boostIndex, type.getDefaultTrait(), null, null);
    }

    /**
     * 生成带属性的二代鱼成品(供育种等玩法覆写个体基因与亲本)。
     *
     * @param type       鱼种
     * @param boostIndex 鱼竿稀有度档位(0=最低,4=最高)
     * @param trait      个体基因型(加速种/合成种), null 时回退鱼种默认基因
     * @param parent1Id  父本鱼种ID(如 GEN2_LUXUE), 野生鱼传 null
     * @param parent2Id  母本鱼种ID(如 GEN2_WEILU), 野生鱼传 null
     * @return 带 PDC 属性的成品鱼物品
     */
    public static ItemStack generate(Gen2Fish type, int boostIndex, Gen2Fish.Trait trait, String parent1Id, String parent2Id) {
        double purityExp = PURITY_EXP[Math.max(0, Math.min(boostIndex, PURITY_EXP.length - 1))];
        double[] elements = rollElements(type, purityExp);
        double quality = rollQuality(boostIndex);
        return buildItem(type, elements, quality, trait, parent1Id, parent2Id);
    }

    /**
     * 育种专用入口: 直接用计算好的五维/品质生成子代(不再随机roll)。
     * 内部会钳制负值并重新归一化(sum=1), 保证数据合法。
     *
     * @param type     子代鱼种(随父母外观)
     * @param elements 子代五维(火·水·雷·风·地), 会被归一化
     * @param quality  子代品质系数(0.7~2.5, 越界自动钳制)
     * @param trait    子代基因型(30%继承/70%随机的结果), null 回退鱼种默认基因
     * @param parent1Id 父本鱼种ID
     * @param parent2Id 母本鱼种ID
     */
    public static ItemStack breed(Gen2Fish type, double[] elements, double quality, Gen2Fish.Trait trait, String parent1Id, String parent2Id) {
        double[] safe = new double[5];
        if (elements != null) {
            double sum = 0;
            for (int i = 0; i < 5; i++) {
                safe[i] = Math.max(0.0, elements[i]);
                sum += safe[i];
            }
            if (sum <= 0) {
                safe = type.getBaseElements().clone(); // 兜底: 父母数据异常时回到鱼种基准
            } else {
                for (int i = 0; i < 5; i++) {
                    safe[i] = safe[i] / sum;
                }
            }
        } else {
            safe = type.getBaseElements().clone();
        }
        double q = Math.max(0.7, Math.min(2.5, quality));
        return buildItem(type, safe, q, trait, parent1Id, parent2Id);
    }

    /** 构造成品鱼物品(名称/lore/PDC), 五维与品质由调用方保证合法 */
    private static ItemStack buildItem(Gen2Fish type, double[] elements, double quality,
                                       Gen2Fish.Trait trait, String parent1Id, String parent2Id) {
        Gen2Fish.Trait effectiveTrait = trait != null ? trait : type.getDefaultTrait();

        // 名称 + 稀有度
        String displayName = type.getGradientName() + " " + type.getRarity().getColorCode() + "§l✦";

        // Lore：品级/品质系数/元素/基因型 采用固定颜色方案(前缀色 + 档位色区分)，
        // 描述/可作材料 两行沿用 §x 全渐变。避免把含颜色码的 displayName 传给渐变方法(会产生 § 字符错位)。
        List<String> lore = new ArrayList<>();
        lore.add("");
        // 品级：前缀品红(§d)，后缀随稀有度变色(凡品§f白/奇珍§e黄/史诗§d品红)，与一代鱼"鱼种稀有度"行同款布局
        lore.add("§d品级·" + type.getRarity().getDisplayName());
        // 品质系数：前缀绿(§a)，与一代鱼"重量"行同款
        lore.add("§a品质系数: §r§f" + String.format("%.2f", quality));
        // 元素：百分比格式，五行各用醒目颜色区分(火红/水蓝/雷紫/风绿/地金)；
        // 后续杂交机器以 元素实值 × 品质系数 累加五种 算出最终值(此处仅展示，数值仍存于 PDC)
        lore.add("§e元素: §c火 " + pct(elements[0])
                + " §b水 " + pct(elements[1])
                + " §d雷 " + pct(elements[2])
                + " §a风 " + pct(elements[3])
                + " §6地 " + pct(elements[4]));
        // 基因型：前缀青蓝(§b)，后缀随基因型变色(加速种§d粉/合成种§a绿)，决定功能流向
        lore.add("§b基因型: " + effectiveTrait.getColorCode() + effectiveTrait.getDisplayName());
        lore.add("");
        lore.add(ColorGradient.getGradientNameVer2(type.getLore()));
        // 亲本追溯：仅育种子代显示, 野生鱼不显示；以鱼种中文名展示(不显示内部 ID)
        if (parent1Id != null && parent2Id != null) {
            String n1 = fishNameOf(parent1Id);
            String n2 = fishNameOf(parent2Id);
            lore.add("");
            lore.add("§7亲本: §r§f" + n1 + " §7× §r§f" + n2);
        }
        lore.add("");
        lore.add(ColorGradient.getGradientNameVer2("可作 育种 / 元素附魔 材料"));

        // 关键：用 Slimefun 的 CustomItemStack 构造（与一代鱼 RANDOM_FISH_* 一致的方式）。
        // 鱼桶材质(COD_BUCKET/PUFFERFISH_BUCKET 等)若用"裸 new ItemStack + setItemMeta"，
        // 其 ItemMeta 在写入 lore 时会丢失(实测写入前后 getLore 均为 null)；
        // CustomItemStack 由内部 ItemMeta 正确固化 name/lore，再叠加 PDC 即可稳定显示。
        ItemStack item = new CustomItemStack(type.getMaterial(), displayName, lore.toArray(new String[0]));

        // 写入属性 PDC（在 CustomItemStack 基础上叠加）
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(GEN2_TYPE, PersistentDataType.STRING, type.getId());
            pdc.set(GEN2_ELEMENTS, PersistentDataType.STRING, join(elements));
            pdc.set(GEN2_QUALITY, PersistentDataType.DOUBLE, quality);
            // 第一层补全: 写入个体基因型; 仅育种子代写入亲本(野生不写=无亲本)
            pdc.set(GEN2_TRAIT, PersistentDataType.STRING, effectiveTrait.name());
            if (parent1Id != null && parent2Id != null) {
                pdc.set(GEN2_PARENTS, PersistentDataType.STRING, parent1Id + "," + parent2Id);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 生成五维元素实值。
     * 以鱼种基准向量为期望，纯度指数越大越贴近基准（方差小），总和归一化恒为 1。
     */
    private static double[] rollElements(Gen2Fish type, double purityExp) {
        double[] base = type.getBaseElements(); // 火·水·雷·风·地, sum=1
        double[] out = new double[5];
        double sum = 0;

        // 在基准值附近做纯度为依赖的扰动
        for (int i = 0; i < 5; i++) {
            double noise = (ThreadLocalRandom.current().nextDouble() - 0.5) * (1.0 / purityExp) * 0.4;
            double v = Math.max(0.0, base[i] + noise);
            out[i] = v;
            sum += v;
        }

        // 归一化到总和=1（主元素保留最高占比）
        for (int i = 0; i < 5; i++) {
            out[i] = out[i] / sum;
        }
        return out;
    }

    /**
     * 生成品质系数(0.7 ~ 2.5)，鱼竿稀有度越高优质区间越大(影响加速效率与合成纯度)。
     */
    private static double rollQuality(int boostIndex) {
        // 底限 0.7; 上限随鱼竿档位提高: 0档≈1.3, 4档≈2.5
        double maxBonus = 0.6 + boostIndex * 0.3;
        return Math.min(2.5, 0.7 + ThreadLocalRandom.current().nextDouble() * maxBonus);
    }

    /** 判断物品是否为二代鱼 */
    public static boolean isGen2Fish(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(GEN2_TYPE, PersistentDataType.STRING);
    }

    /** 读取品质系数(便捷访问) */
    public static double getQuality(ItemStack item) {
        if (!isGen2Fish(item)) return 0.0;
        Double q = item.getItemMeta().getPersistentDataContainer()
                .get(GEN2_QUALITY, PersistentDataType.DOUBLE);
        return q == null ? 0.0 : q;
    }

    /** 读取个体基因型; 老鱼(无 GEN2_TRAIT)或非法值返回 null, 由调用方按鱼种默认基因兜底 */
    public static Gen2Fish.Trait getTrait(ItemStack item) {
        if (!isGen2Fish(item)) return null;
        String t = item.getItemMeta().getPersistentDataContainer()
                .get(GEN2_TRAIT, PersistentDataType.STRING);
        if (t == null) return null;
        try {
            return Gen2Fish.Trait.valueOf(t);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 读取亲本追溯("父ID,母ID"), 野生鱼/老鱼返回 null */
    public static String getParents(ItemStack item) {
        if (!isGen2Fish(item)) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(GEN2_PARENTS, PersistentDataType.STRING);
    }

    /** 读取个体五维实值(火·水·雷·风·地, sum=1), 数据缺失/损坏返回 null */
    public static double[] getElements(ItemStack item) {
        if (!isGen2Fish(item)) return null;
        String raw = item.getItemMeta().getPersistentDataContainer()
                .get(GEN2_ELEMENTS, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return null;
        try {
            String[] parts = raw.split(",");
            if (parts.length != 5) return null;
            double[] out = new double[5];
            for (int i = 0; i < 5; i++) {
                out[i] = Double.parseDouble(parts[i]);
                if (Double.isNaN(out[i]) || Double.isInfinite(out[i])) return null;
            }
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 读取个体鱼种类型(备用于父鱼反查), 非二代鱼返回 null */
    public static Gen2Fish getType(ItemStack item) {
        if (!isGen2Fish(item)) return null;
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(GEN2_TYPE, PersistentDataType.STRING);
        return Gen2Fish.byId(id);
    }

    /** 数组转逗号串 */
    private static String join(double[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format("%.4f", arr[i]));
        }
        return sb.toString();
    }

    /** 0~1 的元素实值 → 百分比(整数)字符串，如 0.29 → "29%" */
    private static String pct(double v) {
        return String.format("%d%%", Math.round(v * 100));
    }

    /** 由鱼种 ID(GEN2_xxx) 反查中文显示名；未知/非法 ID 回退原 ID 字符串 */
    private static String fishNameOf(String fishId) {
        if (fishId == null) return "";
        Gen2Fish fish = Gen2Fish.byId(fishId);
        if (fish == null) return fishId; // 兜底: 显示原始 ID, 避免空串
        return fish.getDisplayName();
    }
}
