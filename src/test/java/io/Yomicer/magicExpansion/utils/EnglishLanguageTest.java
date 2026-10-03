package io.Yomicer.magicExpansion.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class EnglishLanguageTest {
    private static final Pattern NON_ENGLISH_SCRIPT = Pattern.compile("[\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}]");
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "A magical item from|Used for |Information and instructions for|Places or operates|"
            + "A fishing item from|A MagicExpansion processing machine|An automated MagicExpansion|"
            + "Enjoy the effects of|Processes materials for|Produces or processes resources related to");

    @Test
    void bundledNamesAndLoreAreEnglishAndHaveNoPlaceholderDescriptions() throws Exception {
        YamlConfiguration language = loadLanguage();
        int entries = 0;
        for (String key : language.getKeys(true)) {
            if (key.endsWith(".Name")) {
                entries++;
                assertInstanceOf(String.class, language.get(key), key);
                checkText(key, language.getString(key));
                Object lore = language.get(key.substring(0, key.length() - 4) + "Lore");
                assertInstanceOf(List.class, lore, key);
                for (Object line : (List<?>) lore) {
                    assertInstanceOf(String.class, line, key);
                    checkText(key, (String) line);
                }
            }
        }
        assertTrue(entries >= 475, "Missing bundled language entries");
    }

    @Test
    void historicalFinalRodLookupStillResolvesToEnglish() throws Exception {
        YamlConfiguration language = loadLanguage();
        String path = "en_US.Items.南柯一梦终须醒_浮生若梦皆是空";
        assertEquals("Dreams Must End", language.getString(path + ".Name"));
        assertFalse(language.getStringList(path + ".Lore").isEmpty());
    }

    private static void checkText(String key, String text) {
        assertFalse(NON_ENGLISH_SCRIPT.matcher(text).find(), key + ": " + text);
        assertFalse(PLACEHOLDER.matcher(text).find(), key + ": " + text);
    }

    private static YamlConfiguration loadLanguage() throws Exception {
        try (var reader = new InputStreamReader(Objects.requireNonNull(
                EnglishLanguageTest.class.getResourceAsStream("/language.yml")), StandardCharsets.UTF_8)) {
            YamlConfiguration language = new YamlConfiguration();
            language.load(reader);
            return language;
        }
    }
}
