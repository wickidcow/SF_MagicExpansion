package io.Yomicer.magicExpansion.utils.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Transient identity and editable-field snapshot; never persisted as a new shop or trade ID. */
final class ShopEditSession {
    private final ShopManager.Shop shop;
    private final ShopManager.Trade original;
    private final ItemStack result;
    private final List<ItemStack> costs;
    private final int globalLimit;
    private final int personalLimit;

    ShopEditSession(ShopManager.Shop shop, ShopManager.Trade original) {
        this.shop = Objects.requireNonNull(shop);
        this.original = original;
        this.result = original == null ? null : copy(original.result);
        this.costs = original == null ? List.of() : copies(original.costItems);
        this.globalLimit = original == null ? 0 : original.globalLimit;
        this.personalLimit = original == null ? 0 : original.personalLimit;
    }

    boolean isCurrent(ShopManager.Shop current) {
        return current == shop && (original == null || (shop.trades.stream().anyMatch(t -> t == original)
                && Objects.equals(result, original.result) && costs.equals(copies(original.costItems))
                && globalLimit == original.globalLimit && personalLimit == original.personalLimit));
    }

    ShopManager.Trade selected() { return original; }

    ShopManager.Trade apply(ShopManager.Shop current, ItemStack reward, List<ItemStack> cost,
            int global, int personal) {
        if (!isCurrent(current) || reward == null || reward.getType().isAir() || reward.getAmount() <= 0) return null;
        ItemStack newResult = copy(reward);
        List<ItemStack> newCosts = copies(cost);
        ShopManager.Trade updated = original == null ? new ShopManager.Trade() : original;
        updated.result = newResult;
        updated.costItems = newCosts;
        updated.globalLimit = global;
        updated.personalLimit = personal;
        // Usage changes since the editor opened are not erased by editing definitions.
        if (original == null) shop.trades.add(updated);
        return updated;
    }

    boolean delete(ShopManager.Shop current) {
        if (original == null || !isCurrent(current)) return false;
        return shop.trades.removeIf(trade -> trade == original);
    }

    private static ItemStack copy(ItemStack item) { return item == null ? null : item.clone(); }

    private static List<ItemStack> copies(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();
        if (items != null) for (ItemStack item : items) result.add(copy(item));
        return result;
    }
}
