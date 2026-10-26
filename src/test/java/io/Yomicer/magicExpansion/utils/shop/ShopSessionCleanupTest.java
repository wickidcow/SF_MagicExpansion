package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Regression for player-scoped menu/chat state. Deliberately leaves the live
 * editor session intact until the inventory-close callback returns any items.
 */
class ShopSessionCleanupTest {

    @SuppressWarnings("unchecked")
    private static <T> Map<UUID, T> map(String name) throws ReflectiveOperationException {
        Field field = ShopGUI.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<UUID, T>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private static Set<UUID> closeGuard() throws ReflectiveOperationException {
        Field field = ShopGUI.class.getDeclaredField("safeClose");
        field.setAccessible(true);
        return (Set<UUID>) field.get(null);
    }

    @Test
    void removesOnlyTransientStateAndPreservesUnsavedEditorRefundGuard() throws Exception {
        UUID quitter = UUID.randomUUID();
        UUID active = UUID.randomUUID();
        Map<UUID, UUID> pendingNames = map("pendingShopNameCreation");
        Map<UUID, Integer> playerPages = map("playerMainPage");
        Map<UUID, Integer> adminPages = map("adminMainPage");
        Map<UUID, Map<String, Integer>> tradePages = map("shopTradesPage");
        Map<UUID, Map<String, Integer>> adminTradePages = map("adminTradesPage");
        Map<UUID, Integer> probabilityPages = map("probPage");
        Map<UUID, Object> pendingEdits = map("pendingEditData");
        Map<UUID, Object> editing = map("currentEditingData");
        Set<UUID> safeClose = closeGuard();

        Class<?> editType = Class.forName("io.Yomicer.magicExpansion.utils.shop.ShopGUI$ShopEditData");
        Constructor<?> ctor = editType.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object unfinishedEditor = ctor.newInstance();
        Object unfinishedPending = ctor.newInstance();
        UUID pendingToken = UUID.randomUUID();
        try {
            pendingNames.put(quitter, pendingToken);
            pendingNames.put(active, UUID.randomUUID());
            playerPages.put(quitter, 3);
            playerPages.put(active, 4);
            adminPages.put(quitter, 1);
            tradePages.put(quitter, Map.of("example", 2));
            adminTradePages.put(quitter, Map.of("example", 1));
            probabilityPages.put(quitter, 2);
            pendingEdits.put(quitter, unfinishedPending);
            editing.put(quitter, unfinishedEditor);
            safeClose.add(quitter);

            ShopGUI.cleanupTransientPlayerState(quitter);

            assertFalse(pendingNames.containsKey(quitter));
            assertFalse(playerPages.containsKey(quitter));
            assertFalse(adminPages.containsKey(quitter));
            assertFalse(tradePages.containsKey(quitter));
            assertFalse(adminTradePages.containsKey(quitter));
            assertFalse(probabilityPages.containsKey(quitter));
            assertFalse(pendingEdits.containsKey(quitter));
            assertTrue(pendingNames.containsKey(active), "Other players' chat prompts must remain");
            assertEquals(4, playerPages.get(active));

            assertSame(unfinishedEditor, editing.get(quitter),
                    "Live editor state must survive until InventoryClose refunds its inputs");
            assertTrue(safeClose.contains(quitter),
                    "InventoryClose navigation guard must not be reset prematurely");
        } finally {
            pendingNames.remove(quitter);
            pendingNames.remove(active);
            playerPages.remove(quitter);
            playerPages.remove(active);
            adminPages.remove(quitter);
            tradePages.remove(quitter);
            adminTradePages.remove(quitter);
            probabilityPages.remove(quitter);
            pendingEdits.remove(quitter);
            editing.remove(quitter);
            safeClose.remove(quitter);
        }
    }
}
