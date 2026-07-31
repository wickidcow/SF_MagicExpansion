package io.Yomicer.magicExpansion.Listener.miscListener;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import javax.swing.*;

public class ItemFrameListener implements Listener {

    @EventHandler
    public void onItemFrameDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame itemFrame)) return;

        if (!(e.getDamager() instanceof Player p)) return;

        if (hasTapeTag(itemFrame) || hasInfiniteTag(itemFrame)) {

            ItemStack itemInside = itemFrame.getItem();
//            SlimefunItem sfItem = SlimefunItem.getByItem(itemInside);
            if (itemInside.hasItemMeta()) {
                e.setCancelled(true);
                ItemStack doubleDrop = itemInside.clone();
                itemFrame.getWorld().dropItemNaturally(itemFrame.getLocation(), doubleDrop);
                itemFrame.setItem(null);
                p.sendMessage("This item cannot be placed in an item frame because it contains protected data.");
                return;
            }


            if (null != itemInside && !itemInside.getType().isAir()) {
                e.setCancelled(true);
                float dropChance = itemFrame.getItemDropChance();
                boolean willDrop = Math.random() < dropChance;
                if (willDrop) {
                    ItemStack doubleDrop = itemInside.clone();
                    doubleDrop.setAmount(itemInside.getAmount() * 2);
                    itemFrame.getWorld().dropItemNaturally(itemFrame.getLocation(), doubleDrop);
                    itemFrame.setItem(null);
                    if (!hasInfiniteTag(itemFrame)) {
                        itemFrame.remove();
                    }
                    p.sendMessage("✅ Schrödinger favored you! Double drop: " + ItemStackHelper.getDisplayName(doubleDrop) + " x" + doubleDrop.getAmount());
                } else {
                    itemFrame.setItem(null);
                    if (!hasInfiniteTag(itemFrame)) {
                        itemFrame.remove();
                    }
                    p.sendMessage("❌ Schrödinger did not favor you this time.");
                }
            }
        }

    }






    private boolean hasTapeTag(ItemFrame frame) {
        return (frame.hasMetadata("schrodinger_frame") &&
                frame.getMetadata("schrodinger_frame").get(0).asBoolean());
    }
    private boolean hasInfiniteTag(ItemFrame frame) {
        return frame.hasMetadata("schrodinger_frame_infinite") &&
                frame.getMetadata("schrodinger_frame_infinite").get(0).asBoolean();
    }



}
