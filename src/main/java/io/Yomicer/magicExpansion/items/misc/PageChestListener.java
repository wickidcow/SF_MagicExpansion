package io.Yomicer.magicExpansion.items.misc;

import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * 翻页储物箱监听器
 * 玩家界面直接使用 BlockMenu, 与货运容器一致
 * 窗口外空白处点击: 左键上一页, 右键下一页
 * 关闭界面时播放关闭动画(数据由 BlockMenu 自动保存)
 */
public class PageChestListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof BlockMenu menu)) {
            return;
        }
        Location location = menu.getLocation();
        if (!(BlockStorage.check(location.getBlock()) instanceof PageChest)) {
            return;
        }
        // 只有点击在界面外的空白处(没有点中任何格子)才进行翻页
        if (e.getClickedInventory() != null) {
            return;
        }
        e.setCancelled(true);
        ClickType click = e.getClick();
        if (click == ClickType.WINDOW_BORDER_LEFT || click == ClickType.LEFT) {
            PageChest.flip(player, location, -1);
        } else if (click == ClickType.WINDOW_BORDER_RIGHT || click == ClickType.RIGHT) {
            PageChest.flip(player, location, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) {
            return;
        }
        Inventory top = e.getInventory();
        if (!(top.getHolder() instanceof BlockMenu menu)) {
            return;
        }
        Location location = menu.getLocation();
        if (BlockStorage.check(location.getBlock()) instanceof PageChest) {
            PageChest.playAnimation(player, location, false);
        }
    }
}
