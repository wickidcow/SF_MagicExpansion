package io.Yomicer.magicExpansion.items.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.Yomicer.magicExpansion.utils.ColorGradient;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.junit.jupiter.api.Test;

/** Regression contract for the historical firework title without invoking a live world. */
class BigFireworksYuanDanTitleTest {

    @Test
    void fireworkTitleRetainsBothOriginalRgbGradientLines() {
        Component expected = LegacyComponentSerializer.legacySection()
                .deserialize(ColorGradient.getGradientName("Explosion Particles"));
        Title title = BigFireworksYuanDan.explosionTitle();
        assertEquals(expected, title.title());
        assertEquals(expected, title.subtitle());
        // Visual content is exactly the component produced by the historical gradient.
    }

    @Test
    void fireworkTitleRetainsTenSixtyTwentyClientTicks() {
        Title title = BigFireworksYuanDan.explosionTitle();
        Title.Times times = title.times();
        assertNotNull(times);
        assertEquals(Duration.ofMillis(500), times.fadeIn());
        assertEquals(Duration.ofMillis(3000), times.stay());
        assertEquals(Duration.ofMillis(1000), times.fadeOut());
    }

}
