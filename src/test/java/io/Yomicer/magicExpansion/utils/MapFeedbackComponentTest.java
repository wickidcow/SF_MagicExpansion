package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

/** Exact legacy-message formatting regression tests; no world or plugin bootstrap required. */
class MapFeedbackComponentTest {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Test
    void errorMessagePreservesHistoricalRedPrefix() {
        assertEquals("\u00a7cThe target world is unavailable.",
                LEGACY.serialize(MapUtils.coloredFeedback('c', "The target world is unavailable.")));
    }

    @Test
    void successMessagePreservesHistoricalGreenPrefix() {
        assertEquals("\u00a7aMap pasted successfully above the target block!",
                LEGACY.serialize(MapUtils.coloredFeedback('a', "Map pasted successfully above the target block!")));
    }

    @Test
    void dynamicErrorMessageStillInterpretsEmbeddedLegacyFormatting() {
        assertEquals("\u00a7cFailed to paste map: \u00a7efailed name",
                LEGACY.serialize(MapUtils.coloredFeedback('c', "Failed to paste map: \u00a7efailed name")));
    }

    @Test
    void nullFailureMessageMatchesHistoricalStringConcatenation() {
        assertEquals("\u00a7cFailed to paste map: null",
                LEGACY.serialize(MapUtils.coloredFeedback('c', "Failed to paste map: " + (String) null)));
    }

    @Test
    void ampersandCodesRemainLiteralAsBefore() {
        assertEquals("\u00a7cFile not found: &buntouched",
                LEGACY.serialize(MapUtils.coloredFeedback('c', "File not found: &buntouched")));
    }
}
