package io.Yomicer.magicExpansion.items.misc.fish;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * English Water Cloud fishing manual, keeping the original upstream item ID.
 * Unlike the unfinished upstream placeholder, it explains the existing rods
 * and lures without creating entities, inventing catches or mutating fish data.
 */
public final class FishingBookWaterCloud extends SimpleSlimefunItem<ItemUseHandler>
        implements NotPlaceable {

    public FishingBookWaterCloud(ItemGroup itemGroup, SlimefunItemStack item,
                                 RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return event -> {
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.DENY);
            var player = event.getPlayer();

            player.sendMessage("§b§l✦ Water Cloud Fishing §7— §fField Guide");
            player.sendMessage("§7Hold your rod in your main hand and use a matching lure.");
            player.sendMessage("§bCyan Bamboo: §fTempered Sunset, Dust, Flame, Gold, Star Core");
            player.sendMessage("§bReedflower: §fJianjia, Reed Snow, Reed Dew, White Dew, Reed Sprout");
            player.sendMessage("§bSnowy River: §fFrostfall, Falling Floss, Ice Soul, Clearing, Line");
            player.sendMessage("§bFine Rain: §fWind Thread, Misty Rain, White Ripples, Morning Wind, Shadow");
            player.sendMessage("§7The bait bag can hold and prioritize your fishing lures.");
            player.sendMessage("§7Advanced rods can gain proficiency and catch Generation 2 fish.");
            player.sendMessage("§7See the Slimefun guide for exact recipes and rod upgrades.");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        };
    }
}
