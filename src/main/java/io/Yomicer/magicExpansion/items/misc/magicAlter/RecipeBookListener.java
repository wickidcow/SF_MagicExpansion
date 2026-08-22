package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Locale;

public class RecipeBookListener implements Listener {

    private final MagicExpansion plugin;

    public RecipeBookListener(MagicExpansion plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (isRecipeBookGui(event.getView().getTitle())) {
            // The recipe guide is display-only. Cancelling the complete click event
            // blocks normal pickup, shift-click, number-key swaps, double-click
            // collection, cursor swaps, drops, and other InventoryClick actions.
            event.setCancelled(true);

            // Only process navigation/buttons from the top guide inventory.
            if (event.getRawSlot() >= 0
                    && event.getRawSlot() < event.getView().getTopInventory().getSize()
                    && event.getCurrentItem() != null) {
                plugin.getPluginInitializer()
                        .getRecipeBookManager()
                        .handleInventoryClick(player, event.getRawSlot(), event.getCurrentItem());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (isRecipeBookGui(event.getView().getTitle())) {
            // Prevent dragging items into, out of, or across the display-only guide.
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        if (plugin.getPluginInitializer().getRecipeBookManager().isRecipeBook(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            plugin.getPluginInitializer().getRecipeBookManager().openRecipeBook(player);
        }
    }

    private boolean isRecipeBookGui(String title) {
        if (title == null) return false;

        // Inventory titles include legacy color codes and capitalization differs
        // between the main menu and detail screens. Normalizing the title keeps
        // every Magic Altar recipe screen protected without affecting normal
        // player inventories.
        String normalizedTitle = title.toLowerCase(Locale.ROOT);

        return normalizedTitle.contains("magic altar recipe guide")
                || normalizedTitle.contains("recipe details")
                || normalizedTitle.contains("dispenser items")
                || normalizedTitle.contains("dispenser list")
                || normalizedTitle.contains("altar base");
    }
}
