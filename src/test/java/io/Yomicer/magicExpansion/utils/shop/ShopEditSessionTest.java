package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class ShopEditSessionTest {
    private ShopManager.Shop shop;
    private ShopManager.Trade first;
    private ShopManager.Trade second;

    @BeforeEach void setUp() {
        MockBukkit.mock();
        shop = new ShopManager.Shop(); shop.name = "Existing";
        first = trade(); second = trade();
        shop.trades.add(first); shop.trades.add(second);
    }

    @AfterEach void tearDown() { MockBukkit.unmock(); }

    @Test void editingOneOfTwoIdenticalRewardsDoesNotReplaceTheOther() {
        var session = new ShopEditSession(shop, second);
        assertSame(second, session.apply(shop, new ItemStack(Material.EMERALD, 3), List.of(), 2, 3));
        assertEquals(Material.DIAMOND, first.result.getType());
        assertEquals(Material.EMERALD, second.result.getType());
        assertEquals(2, shop.trades.size());
    }

    @Test void purchasesSinceEditorOpenedRetainTheirCountsAndOwners() {
        UUID owner = UUID.randomUUID();
        var session = new ShopEditSession(shop, first);
        first.globalUsed = 17; first.personalUsed.put(owner, 13);
        assertSame(first, session.apply(shop, first.result, first.costItems, 40, 30));
        assertEquals(17, first.globalUsed);
        assertEquals(13, first.personalUsed.get(owner));
    }

    @Test void changingRewardUpdatesTheSelectedTradeRatherThanCreatingADuplicate() {
        var session = new ShopEditSession(shop, first);
        assertSame(first, session.apply(shop, new ItemStack(Material.NETHER_STAR), List.of(), 0, 0));
        assertEquals(2, shop.trades.size());
        assertSame(first, shop.trades.getFirst());
    }

    @Test void reorderingTradesDoesNotChangeWhichTradeWasSelected() {
        var session = new ShopEditSession(shop, second);
        Collections.swap(shop.trades, 0, 1);
        assertSame(second, session.apply(shop, new ItemStack(Material.EMERALD), List.of(), 0, 0));
        assertEquals(Material.DIAMOND, first.result.getType());
    }

    @Test void removedOrReplacedTradeMakesTheEditorStale() {
        var session = new ShopEditSession(shop, first);
        shop.trades.set(0, trade());
        assertFalse(session.isCurrent(shop));
        assertNull(session.apply(shop, first.result, List.of(), 1, 2));
        assertFalse(session.delete(shop));
    }

    @Test void reloadedShopWithTheSameNameDoesNotAcceptOldSession() {
        var session = new ShopEditSession(shop, first);
        var reloaded = new ShopManager.Shop(); reloaded.name = shop.name; reloaded.trades.add(first);
        assertFalse(session.isCurrent(reloaded));
        assertNull(session.apply(reloaded, first.result, List.of(), 1, 2));
    }

    @Test void changedPriceRewardOrLimitsCannotBeSilentlyOverwritten() {
        var price = new ShopEditSession(shop, first);
        first.costItems.getFirst().setAmount(4);
        assertFalse(price.isCurrent(shop));
        var reward = new ShopEditSession(shop, first);
        first.result.setAmount(2);
        assertFalse(reward.isCurrent(shop));
        var global = new ShopEditSession(shop, first);
        first.globalLimit++;
        assertFalse(global.isCurrent(shop));
        var personal = new ShopEditSession(shop, first);
        first.personalLimit++;
        assertFalse(personal.isCurrent(shop));
    }

    @Test void deleteRemovesOnlyTheExactSelectedTrade() {
        var session = new ShopEditSession(shop, second);
        assertTrue(session.delete(shop));
        assertEquals(List.of(first), shop.trades);
        assertFalse(session.delete(shop));
    }

    @Test void newTradeDoesNotOverwriteAnExistingTradeWithIdenticalReward() {
        var session = new ShopEditSession(shop, null);
        var created = session.apply(shop, first.result, first.costItems, 7, 8);
        assertNotNull(created);
        assertNotSame(first, created);
        assertEquals(3, shop.trades.size());
        assertEquals(0, first.globalLimit);
    }

    @Test void newTradeCannotDeleteAnotherDefinition() {
        assertFalse(new ShopEditSession(shop, null).delete(shop));
        assertEquals(2, shop.trades.size());
    }

    @Test void appliedInputsAreDetachedFromEditorItems() {
        ItemStack result = new ItemStack(Material.EMERALD, 3);
        ItemStack cost = new ItemStack(Material.GOLD_INGOT, 4);
        new ShopEditSession(shop, first).apply(shop, result, List.of(cost), 10, 20);
        result.setAmount(19); cost.setAmount(20);
        assertEquals(3, first.result.getAmount());
        assertEquals(4, first.costItems.getFirst().getAmount());
    }

    @Test void emptyResultsAndMissingShopsDoNotMutateDefinitions() {
        var session = new ShopEditSession(shop, first);
        assertNull(session.apply(shop, null, List.of(), 1, 2));
        assertNull(session.apply(shop, new ItemStack(Material.AIR), List.of(), 1, 2));
        assertNull(session.apply(null, new ItemStack(Material.EMERALD), List.of(), 1, 2));
        assertEquals(Material.DIAMOND, first.result.getType());
        assertEquals(0, first.globalLimit);
    }

    private static ShopManager.Trade trade() {
        var trade = new ShopManager.Trade();
        trade.result = new ItemStack(Material.DIAMOND);
        trade.costItems.add(new ItemStack(Material.GOLD_INGOT));
        return trade;
    }
}
