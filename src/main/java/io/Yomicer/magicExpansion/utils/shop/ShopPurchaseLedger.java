package io.Yomicer.magicExpansion.utils.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Commits tentative portable-shop counters before authorizing a reward. */
final class ShopPurchaseLedger {
    private ShopPurchaseLedger() {}

    /** Server-thread only. A refused/throwing save restores the original in-memory counters. */
    static boolean commit(UUID player, ShopManager.Trade trade, BooleanSupplier persist) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(trade, "trade");
        Objects.requireNonNull(persist, "persist");
        Map<UUID, Integer> originalPersonal = Objects.requireNonNull(trade.personalUsed, "personalUsed");
        int originalGlobal = trade.globalUsed;
        int used = originalPersonal.getOrDefault(player, 0);
        if (trade.globalLimit > 0 && originalGlobal >= trade.globalLimit) return false;
        if (trade.personalLimit > 0 && used >= trade.personalLimit) return false;

        Map<UUID, Integer> proposed = new HashMap<>(originalPersonal);
        if (trade.personalLimit > 0) proposed.put(player, used + 1);
        if (trade.globalLimit > 0) trade.globalUsed = originalGlobal + 1;
        trade.personalUsed = proposed;
        boolean committed = false;
        try {
            committed = persist.getAsBoolean();
            return committed;
        } finally {
            if (!committed) {
                trade.globalUsed = originalGlobal;
                trade.personalUsed = originalPersonal;
            }
        }
    }
}
