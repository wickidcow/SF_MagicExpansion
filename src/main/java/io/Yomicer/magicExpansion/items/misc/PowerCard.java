package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.utils.energy.PowerCardConfigurationManager;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class PowerCard extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public PowerCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, org.bukkit.inventory.ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return event -> {
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.DENY);

            if (!event.getPlayer().isSneaking()) {
                event.getPlayer().sendMessage("§eSneak-right-click to configure maximum energy storage.");
                return;
            }

            PowerCardConfigurationManager.begin(
                    event.getPlayer(),
                    event.getHand(),
                    event.getItem(),
                    getId(),
                    3);
        };
    }
}
