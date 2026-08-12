package io.Yomicer.magicExpansion.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class WaterCloudHookPhrases {

    private static final Map<WaterCloudHookState, List<String>> PHRASES = Map.of(
            WaterCloudHookState.WAITING, List.of(
                    "The float rests quietly on still water.",
                    "The rod tip hangs low; only the current moves.",
                    "Cloud shadows drift across an empty surface.",
                    "The water is calm. Nothing has committed to the bait yet."
            ),
            WaterCloudHookState.LIGHT_BITE, List.of(
                    "Small ripples gather around the float.",
                    "The float gives a cautious twitch.",
                    "Something below is testing the bait.",
                    "A dark shape passes beneath the surface."
            ),
            WaterCloudHookState.FULL_BITE, List.of(
                    "The float plunges beneath the water!",
                    "The line snaps tight and water sprays around the hook!",
                    "A strong fish takes the bait and pulls hard!",
                    "The surface breaks—the fish is fully committed!"
            )
    );

    private static final List<String> ESCAPED_PHRASES = List.of(
            "The line goes slack; the fish escaped.",
            "The water settles, leaving only widening ripples.",
            "The fish slips free before the hook can hold.",
            "Water and cloud remain, but this catch is gone."
    );

    private WaterCloudHookPhrases() {
    }

    public static String getRandom(WaterCloudHookState state) {
        List<String> pool = PHRASES.getOrDefault(state, ESCAPED_PHRASES);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    public static String getRandomEscaped() {
        return ESCAPED_PHRASES.get(ThreadLocalRandom.current().nextInt(ESCAPED_PHRASES.size()));
    }
}
