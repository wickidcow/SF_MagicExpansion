package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.Action; // 【E修复】用于判定右键动作类型
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
        if (title.contains("祭坛配方") ||
                title.contains("配方详情") ||
                title.contains("发射器物品") ||
                title.contains("发射器列表") ||
                title.contains("祭坛底座")) {

            // 完全取消事件，防止任何物品被移动
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
        if (title.contains("祭坛配方") ||
                title.contains("配方详情") ||
                title.contains("发射器物品") ||
                title.contains("发射器列表") ||
                title.contains("祭坛底座")) {

            // 取消拖拽事件，防止物品被拖拽
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 【E修复】仅处理主手(HAND)的右键操作；左键挖掘/攻击、副手等其他情况直接放行，不取消事件
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        // 检查是否右键了配方书
        if (plugin.getPluginInitializer().getRecipeBookManager().isRecipeBook(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            plugin.getPluginInitializer().getRecipeBookManager().openRecipeBook(player);
        }
    }
}