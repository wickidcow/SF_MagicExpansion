package io.Yomicer.magicExpansion.Listener.worldListener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class Events implements Listener {

    public static final Map<UUID, Boolean> selectMode = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> point1 = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> point2 = new ConcurrentHashMap<>();

    /** Reset only the player's temporary world-selection markers on logout. */
    public static void cleanup(UUID uuid) {
        selectMode.remove(uuid);
        point1.remove(uuid);
        point2.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
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

}
