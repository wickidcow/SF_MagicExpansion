package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MagicAltarListener implements Listener {

    private final MagicExpansion plugin;

    public MagicAltarListener(MagicExpansion plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // 检查是否右键了发射器
        if (block != null && block.getType() == Material.DISPENSER) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            // 检查是否拿着祭坛法杖
            if (isAltarWand(itemInHand)) {
                event.setCancelled(true);

                // 播放点击音效
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

                // 检查是否是中心发射器
                if (isCenterDispenser(block.getLocation())) {
                    // 开始合成检查
                    boolean success = plugin.getPluginInitializer().getAltarManager().startCrafting(block.getLocation(), player);

                    if (success) {
                        player.sendMessage("§a✓ Magic Altar activated! Checking recipes...");
                    } else {
                        player.sendMessage("§c✗ No matching recipe was found!");
                    }
                } else {
                    player.sendMessage("§e⚠ Right-click the center dispenser to activate the altar!");
                }
            }
        }
    }

    // 检查物品是否是祭坛法杖
    private boolean isAltarWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(
                plugin.getPluginInitializer().getAltarWandKey(),
                PersistentDataType.BYTE
        );
    }

    // 检查是否是中心发射器(周围8格都是发射器)
    private boolean isCenterDispenser(Location location) {
        int dispenserCount = 0;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location checkLoc = location.clone().add(x, 0, z);
                if (checkLoc.getBlock().getType() == Material.DISPENSER) {
                    dispenserCount++;
                }
            }
        }

        return dispenserCount == 9; // 3x3的发射器阵列
    }
}
