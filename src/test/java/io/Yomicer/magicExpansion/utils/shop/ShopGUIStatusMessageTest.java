package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

/**
 * Locks down the shop editor's historical legacy section-code status presentation.
 * These are messages only; saved shop titles, lore, item metadata and receipt IDs
 * must never be rewritten through the chat-delivery adapter.
 */
class ShopGUIStatusMessageTest {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Test
    void eightEditorAndPurchaseLimitMessagesKeepTheirOriginalSectionCodes() {
        Map<String, Component> messages = new LinkedHashMap<>();
        messages.put("\u00a7eShop creation cancelled.",
            ShopGUI.statusMessage(NamedTextColor.YELLOW, "Shop creation cancelled."));
        messages.put("\u00a7aShop Albion_42 created successfully!",
            ShopGUI.statusMessage(NamedTextColor.GREEN, "Shop Albion_42 created successfully!"));
        messages.put("\u00a7cShop creation refused: the name is invalid, already used, or its file is protected. Check the console, choose another name, or type 'cancel'.",
            ShopGUI.statusMessage(NamedTextColor.RED,
                "Shop creation refused: the name is invalid, already used, or its file is protected. Check the console, choose another name, or type 'cancel'."));
        messages.put("\u00a7eLimit setting cancelled.",
            ShopGUI.statusMessage(NamedTextColor.YELLOW, "Limit setting cancelled."));
        messages.put("\u00a7aServer-wide purchase limit set to: Unlimited",
            ShopGUI.statusMessage(NamedTextColor.GREEN, "Server-wide purchase limit set to: Unlimited"));
        messages.put("\u00a7aPer-player purchase limit set to: 12",
            ShopGUI.statusMessage(NamedTextColor.GREEN, "Per-player purchase limit set to: 12"));
        messages.put("\u00a7cInvalid input; enter a number!",
            ShopGUI.statusMessage(NamedTextColor.RED, "Invalid input; enter a number!"));
        messages.put("\u00a7cShop save failed; the original file was retained. Check the console and repair/reload before retrying.",
            ShopGUI.statusMessage(NamedTextColor.RED,
                "Shop save failed; the original file was retained. Check the console and repair/reload before retrying."));
        assertEquals(8, messages.size());
        messages.forEach((legacy, component) -> assertEquals(legacy, LEGACY.serialize(component)));
    }

    @Test
    void dynamicCountsAndShopNamesRemainLiteralWithExactVisibleColor() {
        for (int count : new int[] {0, 1, 99, Integer.MAX_VALUE}) {
            String rendered = count == 0 ? "Unlimited" : Integer.toString(count);
            String text = "Server-wide purchase limit set to: " + rendered;
            assertEquals("\u00a7a" + text, LEGACY.serialize(ShopGUI.statusMessage(NamedTextColor.GREEN, text)));
            String personal = "Per-player purchase limit set to: " + rendered;
            assertEquals("\u00a7a" + personal, LEGACY.serialize(ShopGUI.statusMessage(NamedTextColor.GREEN, personal)));
        }
        for (String name : new String[] {"Town", "Albion_42", "X-3", "Cafeé"}) {
            String line = "Shop " + name + " created successfully!";
            assertEquals("\u00a7a" + line, LEGACY.serialize(ShopGUI.statusMessage(NamedTextColor.GREEN, line)));
        }
    }

    @Test
    void noLegacyColorCodesAreStoredInComponentText() {
        Component message = ShopGUI.statusMessage(NamedTextColor.YELLOW, "Limit setting cancelled.");
        assertEquals(Component.text("Limit setting cancelled.", NamedTextColor.YELLOW), message);
    }
}
