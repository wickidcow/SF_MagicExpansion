package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class RecipeBookListener implements Listener {

    private final MagicExpansion plugin;

    public RecipeBookListener(MagicExpansion plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // 检查是否在配方书相关的GUI中
        if (title.contains("recipe") ||
                title.contains("Recipe Details") ||
                title.contains("Dispenser Items") ||
                title.contains("Dispenser List") ||
                title.contains("Altar Base")) {

            // 完全取消事件,防止任何物品被移动
            event.setCancelled(true);

            // 只处理有物品的点击
            if (event.getCurrentItem() != null) {
                plugin.getPluginInitializer().getRecipeBookManager().handleInventoryClick(player, event.getRawSlot(), event.getCurrentItem());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        String title = event.getView().getTitle();

        // 检查是否在配方书相关的GUI中
        if (title.contains("recipe") ||
                title.contains("Recipe Details") ||
                title.contains("Dispenser Items") ||
                title.contains("Dispenser List") ||
                title.contains("Altar Base")) {

            // 取消拖拽事件,防止物品被拖拽
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        // 检查是否右键了配方书
        if (plugin.getPluginInitializer().getRecipeBookManager().isRecipeBook(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            plugin.getPluginInitializer().getRecipeBookManager().openRecipeBook(player);
        }
    }
}
