package io.Yomicer.magicExpansion.Listener;

import io.Yomicer.magicExpansion.MagicExpansionItemSetup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.Yomicer.magicExpansion.utils.GuideCategoryMenu;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 原生粘液书分组页的容器组点击拦截:
 * 容器组图标带 guide_virtual 标记,点击后取消原生物品页,改为打开自绘三级菜单
 */
public class GuideVirtualGroupClickListener implements Listener {

    /** 兜底拦截:即使原生点击处理先打开了容器组物品页,打开瞬间也会被替换为自绘三级菜单 */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        Inventory inv = e.getInventory();
        String vid = findVirtualId(inv);
        if (vid == null) return;
        e.setCancelled(true);
        openVirtualMenu(player, vid);
    }

    /** 关闭菜单时停止彩虹变色任务 */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            GuideCategoryMenu.cancelRainbow(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        String vid = GuideCategoryMenu.getVirtualGroupId(clicked);
        if (vid == null) return;

        e.setCancelled(true);
        openVirtualMenu(player, vid);
    }

    /** 在物品页中查找占位物品对应的虚拟分组 id */
    private String findVirtualId(Inventory inv) {
        for (ItemStack it : inv.getContents()) {
            if (it == null) continue;
            SlimefunItem sf = SlimefunItem.getByItem(it);
            if (sf != null && sf.getId().startsWith("VIRTUAL_ENTRY_")) {
                return sf.getId().substring("VIRTUAL_ENTRY_".length()).toLowerCase();
            }
        }
        return null;
    }

    /** 重建干净历史链并打开自绘三级菜单 */
    private void openVirtualMenu(Player player, String vid) {
        PlayerProfile.get(player, profile -> {
            GuideHistory history = profile.getGuideHistory();
            history.clear();
            history.add(MagicExpansionItemSetup.magicexpansion, 1);
            GuideCategoryMenu.openVirtualGroup(player, profile, SlimefunGuideMode.SURVIVAL_MODE, vid);
        });
    }
}