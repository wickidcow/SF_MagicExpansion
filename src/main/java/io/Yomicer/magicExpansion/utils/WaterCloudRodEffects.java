package io.Yomicer.magicExpansion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 水云间·鱼竿等级特殊加成(新旧系统分轨奖励表, 经验/等级共享)
 * 每个等级升级后的特殊加成集中配置在此, 后续添加新效果时直接在此扩展
 * 旧系统(原版判定): 上钩速度/杂物降低/省饵/双倍
 * 新系统(状态机判定): 等待缩短/轻咬缩短/蓄力加速/双倍/满级特殊+
 */
public class WaterCloudRodEffects {

    /** 效果类型(后续新效果在此追加) */
    public enum EffectType {
        /** 旧·上钩速度: 值 = 原版等待上限缩短的 tick 数(每 2 级 -60 tick) */
        OLD_HOOK_SPEED,
        /** 旧·杂物概率: 值 = 鱼饵池中原版物品(杂物)条目的权重削减比例(3 级起每级 -3%) */
        OLD_JUNK_REDUCTION,
        /** 旧·省饵: 值 = 消耗鱼饵时跳过消耗的概率(4 级起每级 +8%) */
        OLD_LURE_PRESERVE,
        /** 旧·双倍鱼获: 值 = 一份鱼饵出两份钓物的概率(3 级起每级 +4%) */
        OLD_DOUBLE_CATCH,
        /** 新·等待缩短: 值 = 等待→轻咬的每秒概率加成(4 级起每级 +1.5%) */
        NEW_BITE_SPEED,
        /** 新·轻咬缩短: 值 = 轻咬→完全咬钩的每秒概率加成(5 级起每级 +2%) */
        NEW_LIGHT_BITE_SPEED,
        /** 新·蓄力加速: 值 = 每秒蓄力点数加成(3 级起每级 +0.5 点/秒) */
        NEW_CHARGE_SPEED,
        /** 新·双倍鱼获: 值 = 一份鱼饵出两份钓物的概率(3 级起每级 +3%) */
        NEW_DOUBLE_CATCH,
        /** 新·满级特殊+: 值 = 蓄满时特殊钓物概率额外加成(仅 8 级 +5%) */
        NEW_RARE_BONUS
    }

    /** 单条等级效果 */
    public static final class LevelEffect {

        private final EffectType type;
        private final double value;

        public LevelEffect(EffectType type, double value) {
            this.type = type;
            this.value = value;
        }

        public EffectType getType() {
            return type;
        }

        public double getValue() {
            return value;
        }
    }

    private WaterCloudRodEffects() {
    }

    /**
     * 指定等级对应的特殊加成列表(新旧系统全部生成, 生效点按当前系统取用)
     * 新增效果: 在这里按等级追加 LevelEffect 即可, 各生效点无需改动
     */
    public static List<LevelEffect> getEffects(int level) {
        List<LevelEffect> effects = new ArrayList<>();

        // ===== 旧系统 =====
        // 上钩速度: 每 2 级等待上限 -60 tick (满级 8 级 -240 tick: 30s → 18s)
        effects.add(new LevelEffect(EffectType.OLD_HOOK_SPEED, Math.max(0, level / 2) * 60.0));
        // 杂物概率: 3 级起每级 -3% (满级 -18%)
        effects.add(new LevelEffect(EffectType.OLD_JUNK_REDUCTION, Math.max(0, level - 2) * 0.03));
        // 省饵: 4 级起每级 +8% (满级 +40%)
        effects.add(new LevelEffect(EffectType.OLD_LURE_PRESERVE, Math.max(0, level - 3) * 0.08));
        // 双倍鱼获: 3 级起每级 +4% (满级 +24%)
        effects.add(new LevelEffect(EffectType.OLD_DOUBLE_CATCH, Math.max(0, level - 2) * 0.04));

        // ===== 新系统 =====
        // 等待缩短: 4 级起每级 +1.5% (满级 +7.5%)
        effects.add(new LevelEffect(EffectType.NEW_BITE_SPEED, Math.max(0, level - 3) * 0.015));
        // 轻咬缩短: 5 级起每级 +2% (满级 +8%)
        effects.add(new LevelEffect(EffectType.NEW_LIGHT_BITE_SPEED, Math.max(0, level - 4) * 0.02));
        // 蓄力加速: 3 级起每级 +0.5 点/秒 (满级 +3 点/秒)
        effects.add(new LevelEffect(EffectType.NEW_CHARGE_SPEED, Math.max(0, level - 2) * 0.5));
        // 双倍鱼获: 3 级起每级 +3% (满级 +18%)
        effects.add(new LevelEffect(EffectType.NEW_DOUBLE_CATCH, Math.max(0, level - 2) * 0.03));
        // 满级特殊+: 仅 8 级 +5%
        effects.add(new LevelEffect(EffectType.NEW_RARE_BONUS, level >= 8 ? 0.05 : 0.0));

        return effects;
    }

    // ===== 旧系统取值 =====

    /** 旧·上钩速度: 等待上限缩短的 tick 数 */
    public static int getOldHookSpeedTicks(int level) {
        return (int) sum(level, EffectType.OLD_HOOK_SPEED);
    }

    /** 旧·杂物概率: 鱼饵池中原版物品条目的权重削减比例 */
    public static double getOldJunkReduction(int level) {
        return sum(level, EffectType.OLD_JUNK_REDUCTION);
    }

    /** 旧·省饵概率 */
    public static double getOldLurePreserveChance(int level) {
        return sum(level, EffectType.OLD_LURE_PRESERVE);
    }

    /** 旧·双倍鱼获概率 */
    public static double getOldDoubleCatchChance(int level) {
        return sum(level, EffectType.OLD_DOUBLE_CATCH);
    }

    // ===== 新系统取值 =====

    /** 新·等待缩短: 等待→轻咬的每秒概率加成 */
    public static double getNewBiteChanceBonus(int level) {
        return sum(level, EffectType.NEW_BITE_SPEED);
    }

    /** 新·轻咬缩短: 轻咬→完全咬钩的每秒概率加成 */
    public static double getNewLightBiteChanceBonus(int level) {
        return sum(level, EffectType.NEW_LIGHT_BITE_SPEED);
    }

    /** 新·蓄力加速: 每秒蓄力点数加成 */
    public static double getNewChargeSpeedBonus(int level) {
        return sum(level, EffectType.NEW_CHARGE_SPEED);
    }

    /** 新·双倍鱼获概率 */
    public static double getNewDoubleCatchChance(int level) {
        return sum(level, EffectType.NEW_DOUBLE_CATCH);
    }

    /** 新·满级特殊+概率加成 */
    public static double getNewRareBonus(int level) {
        return sum(level, EffectType.NEW_RARE_BONUS);
    }

    private static double sum(int level, EffectType type) {
        return getEffects(level).stream()
                .filter(e -> e.getType() == type)
                .mapToDouble(LevelEffect::getValue)
                .sum();
    }
}
