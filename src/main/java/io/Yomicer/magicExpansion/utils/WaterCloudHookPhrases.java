package io.Yomicer.magicExpansion.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 水云间·新钓鱼状态文案池(攻略向)
 * 每个状态多段专属描述, 后续可直接整理为钓鱼攻略
 */
public final class WaterCloudHookPhrases {

    private static final Map<WaterCloudHookState, List<String>> PHRASES = Map.of(
            WaterCloudHookState.WAITING, List.of(
                    "浮标静立,水波不兴",
                    "竿梢微垂,四下无声",
                    "水面如镜,唯有风声",
                    "云影徘徊,不见鱼踪"
            ),
            WaterCloudHookState.LIGHT_BITE, List.of(
                    "水面泛起细碎涟漪",
                    "浮标轻轻一颤",
                    "有鱼试探,欲拒还迎",
                    "水下一道黑影掠过"
            ),
            WaterCloudHookState.FULL_BITE, List.of(
                    "浮标猛然沉入水中",
                    "竿身骤紧,水花四溅",
                    "大鱼上钩,力贯竿梢",
                    "水面炸开一团浪花"
            )
    );

    /** 脱钩提示(瞬时状态专用) */
    private static final List<String> ESCAPED_PHRASES = List.of(
            "浮标一沉即起,鱼已脱钩",
            "水面归于平静,徒留涟漪",
            "那鱼挣脱了,仿佛从未存在",
            "水云依旧,此鱼无缘"
    );

    private WaterCloudHookPhrases() {
    }

    /** 按状态随机取一条文案 */
    public static String getRandom(WaterCloudHookState state) {
        List<String> pool = PHRASES.getOrDefault(state, ESCAPED_PHRASES);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    /** 随机取一条脱钩文案 */
    public static String getRandomEscaped() {
        return ESCAPED_PHRASES.get(ThreadLocalRandom.current().nextInt(ESCAPED_PHRASES.size()));
    }
}
