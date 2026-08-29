package io.Yomicer.magicExpansion.Listener.miscListener;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
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

// E3: 删除了 import javax.swing.*（GUI 库不应出现在 Bukkit 插件中）

public class ItemFrameListener implements Listener {

    @EventHandler
    public void onItemFrameDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame itemFrame)) return;

        if (!(e.getDamager() instanceof Player p)) return;

        if (hasTapeTag(itemFrame) || hasInfiniteTag(itemFrame)) {

            ItemStack itemInside = itemFrame.getItem();
//            SlimefunItem sfItem = SlimefunItem.getByItem(itemInside);
            // E1: 先判空/空气，再调 hasItemMeta()，防止 itemInside 为 null 时 NPE
            if (itemInside != null && !itemInside.getType().isAir() && itemInside.hasItemMeta()) {
                e.setCancelled(true);
                ItemStack doubleDrop = itemInside.clone();
                itemFrame.getWorld().dropItemNaturally(itemFrame.getLocation(), doubleDrop);
                itemFrame.setItem(null);
                p.sendMessage("请不要将携带NBT的物品放上去");
                return;
            }


            if (null != itemInside && !itemInside.getType().isAir()) {
                e.setCancelled(true);
                // E2: 不再读 itemFrame.getItemDropChance()——其他途径放置的展示框默认 1.0（必双倍）。
                // 改为读取展示框上本插件的薛定谔标记（SchrodingerFrame 以同名 key 写入 metadata：
                // "schrodinger_frame" / "schrodinger_frame_infinite"，即下方 hasTapeTag/hasInfiniteTag 同 key），
                // 有标记按设计概率 0.5 判定；无标记同样按固定 0.5 概率，保证双倍概率恒定可控
                boolean hasSchrodingerMark = hasTapeTag(itemFrame) || hasInfiniteTag(itemFrame);
                boolean willDrop = Math.random() < (hasSchrodingerMark ? 0.5f : 0.5f);
                if (willDrop) {
                    ItemStack doubleDrop = itemInside.clone();
                    doubleDrop.setAmount(itemInside.getAmount() * 2);
                    itemFrame.getWorld().dropItemNaturally(itemFrame.getLocation(), doubleDrop);
                    itemFrame.setItem(null);
                    if (!hasInfiniteTag(itemFrame)) {
                        itemFrame.remove();
                    }
                    p.sendMessage("✅ 被薛定谔眷顾的人！恭喜掉落掉落双倍物品：" + ItemStackHelper.getDisplayName(doubleDrop) + " x" + doubleDrop.getAmount());
                } else {
                    itemFrame.setItem(null);
                    if (!hasInfiniteTag(itemFrame)) {
                        itemFrame.remove();
                    }
                    p.sendMessage("❌ 薛定谔没有眷顾于你");
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