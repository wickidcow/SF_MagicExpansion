package io.Yomicer.magicExpansion.Listener.worldListener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Events implements Listener {

    // 修复：HashMap 改为 ConcurrentHashMap，避免多线程/异步访问时的并发问题
    public static final Map<UUID, Boolean> selectMode = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> point1 = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> point2 = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 修复：副手交互会触发两次事件，过滤 OFF_HAND 防止重复选点
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (!selectMode.getOrDefault(player.getUniqueId(), false)) {
            return; // 玩家不在选择模式
        }

        if (event.getAction().name().contains("LEFT_CLICK_BLOCK")) {
            Block block = event.getClickedBlock();
            point1.put(player.getUniqueId(), block.getLocation());
            player.sendMessage("Point 1 selected at " + block.getLocation());
        } else if (event.getAction().name().contains("RIGHT_CLICK_BLOCK")) {
            Block block = event.getClickedBlock();
            point2.put(player.getUniqueId(), block.getLocation());
            player.sendMessage("Point 2 selected at " + block.getLocation());
        }

        event.setCancelled(true); // 阻止破坏方块或打开容器
    }

    /**
     * 修复：玩家退出时清理该玩家的选择模式与选点数据，防止内存泄漏
     */
    public static void cleanup(UUID uuid) {
        selectMode.remove(uuid);
        point1.remove(uuid);
        point2.remove(uuid);
    }

}
