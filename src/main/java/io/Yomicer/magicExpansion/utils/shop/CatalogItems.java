package io.Yomicer.magicExpansion.utils.shop;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Resolves canonical templates only; an absent addon can never become a vanilla substitute. */
final class CatalogItems implements MarketCatalog.Resolver {
    @Override public boolean pluginEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    @Override public ItemStack resolve(MarketCatalog.ItemSpec spec) {
        ItemStack result;
        if (spec.slimefun() != null) {
            SlimefunItem item = SlimefunItem.getById(spec.slimefun());
            if (!enabled(item)) return null;
            result = ItemStack.deserialize(item.getItem().serialize());
        } else {
            Material material = Material.getMaterial(spec.material());
            if (material == null || material.isAir() || !material.isItem()) return null;
            result = new ItemStack(material);
        }
        if (result.getType().isAir() || spec.amount() > result.getMaxStackSize()) return null;
        result.setAmount(spec.amount());
        return result;
    }

    static boolean isAvailable(ShopManager.Trade trade) {
        if (trade.catalogPlugins == null) return false;
        // Preserve the treatment of historical/manual trades; this guard applies to catalog offers.
        if (trade.catalogId == null && trade.catalogPlugins.isEmpty()) return true;
        try {
            for (String plugin : trade.catalogPlugins) if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) return false;
            if (!availableStack(trade.result) || trade.costItems == null || trade.costItems.isEmpty()) return false;
            for (ItemStack cost : trade.costItems) if (!availableStack(cost)) return false;
            return true;
        } catch (RuntimeException | LinkageError unavailable) {
            return false;
        }
    }

    private static boolean availableStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) return false;
        var id = Slimefun.getItemDataService().getItemData(stack);
        return id.isEmpty() || enabled(SlimefunItem.getById(id.get()));
    }

    private static boolean enabled(SlimefunItem item) {
        return item != null && !item.isDisabled() && item.getAddon() != null
                && item.getAddon().getJavaPlugin().isEnabled();
    }
}
