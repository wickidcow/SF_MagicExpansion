package io.Yomicer.magicExpansion.utils;

import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 机器加速 Buff 追踪(第二层·加速玩法基础设施)。
 * <p>
 * 以"机器方块坐标"为键, 追踪每台机器的加速 Buff 与过期时间:
 * <ul>
 *   <li>基础倍率 {@link #BASE_MULTIPLIER} = 1.5, 实际倍率 = 1.5 × 鱼品质系数(0.7~2.5)；</li>
 *   <li>内存存储, 服务器重启后 Buff 全部失效(第一版约定)；</li>
 *   <li>过期清理为惰性: 查询时顺手移除过期条目。</li>
 * </ul>
 * 当前第一版只登记/查询 Buff 数据, 机器效率接入(生态缸等)留待后续统一接入。
 */
public final class MachineBuffManager {

    /** Acceleration base multiplier. */
    public static final double BASE_MULTIPLIER = 1.5;

    /** Gen2 quality is capped at 2.5, so a legal acceleration buff tops out at 3.75x. */
    public static final double MAX_MULTIPLIER = BASE_MULTIPLIER * 2.5;

    /** 默认持续时间 5 分钟(秒) */
    public static final long DEFAULT_DURATION_SECONDS = 300;

    private record Buff(long expireAtMillis, double multiplier) {
    }

    private static final Map<Location, Buff> BUFFS = new ConcurrentHashMap<>();

    private MachineBuffManager() {
    }

    /** 给指定坐标的机器登记加速 Buff */
    public static void apply(Location loc, long durationSeconds, double multiplier) {
        if (loc == null || !Double.isFinite(multiplier)) {
            return;
        }

        double safeMultiplier = Math.max(1.0D, Math.min(MAX_MULTIPLIER, multiplier));
        long safeSeconds = Math.max(1L, durationSeconds);
        long now = System.currentTimeMillis();
        long durationMillis = safeSeconds > Long.MAX_VALUE / 1000L
                ? Long.MAX_VALUE
                : safeSeconds * 1000L;
        long expireAt = durationMillis > Long.MAX_VALUE - now
                ? Long.MAX_VALUE
                : now + durationMillis;

        BUFFS.put(loc.clone(), new Buff(expireAt, safeMultiplier));
    }

    /** 查询生效中的倍率; 无 Buff/已过期返回 1.0(不加速) */
    public static double getMultiplier(Location loc) {
        Buff buff = BUFFS.get(loc);
        if (buff == null) return 1.0;
        if (System.currentTimeMillis() >= buff.expireAtMillis()) {
            BUFFS.remove(loc);
            return 1.0;
        }
        return buff.multiplier();
    }

    /**
     * Scales a non-negative production amount by the live acceleration multiplier
     * without allowing long overflow.
     */
    public static long scaleAmount(Location loc, long baseAmount) {
        if (baseAmount <= 0L) {
            return baseAmount;
        }

        double multiplier = getMultiplier(loc);
        if (!Double.isFinite(multiplier) || multiplier <= 1.0D) {
            return baseAmount;
        }

        double scaled = baseAmount * multiplier;
        if (!Double.isFinite(scaled) || scaled >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(baseAmount, (long) Math.floor(scaled));
    }

    /** 是否处于加速 Buff 生效中 */
    public static boolean hasBuff(Location loc) {
        Buff buff = BUFFS.get(loc);
        if (buff == null) return false;
        if (System.currentTimeMillis() >= buff.expireAtMillis()) {
            BUFFS.remove(loc);
            return false;
        }
        return true;
    }

    /** 剩余生效秒数(无 Buff/已过期返回 0) */
    public static long getRemainingSeconds(Location loc) {
        Buff buff = BUFFS.get(loc);
        if (buff == null) return 0;
        long left = (buff.expireAtMillis() - System.currentTimeMillis()) / 1000L;
        if (left <= 0) {
            BUFFS.remove(loc);
            return 0;
        }
        return left;
    }

    /** 主动移除某台机器的 Buff */
    public static void remove(Location loc) {
        BUFFS.remove(loc);
    }
}