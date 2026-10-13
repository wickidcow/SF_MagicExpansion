package io.Yomicer.magicExpansion.items.misc.fish;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Privacy-safe catch announcements. Old configs that lack these keys default
 * to no public broadcast and no coordinate sharing.
 */
public final class FishingAnnouncements {
    private static final String CONFIG_PATH = "fishing.announcements.";

    private FishingAnnouncements() {
    }

    public static void announce(Player player, FileConfiguration config,
                                String fishName, String rarityName, String rarityColor) {
        boolean broadcast = config.getBoolean(CONFIG_PATH + "enabled", false);
        boolean notifyPlayer = config.getBoolean(CONFIG_PATH + "notify-player", true);
        if (!broadcast && !notifyPlayer) {
            return;
        }

        String catchDescription = rarityColor + rarityName + "§a: " + fishName + "§a!";
        if (broadcast) {
            String message = "§a[Magic Fishing] §e" + player.getName() + " §acaught "
                    + catchDescription;
            if (config.getBoolean(CONFIG_PATH + "show-coordinates", false)) {
                Location location = player.getLocation();
                message += " §7(" + player.getWorld().getName() + " X:"
                        + location.getBlockX() + " Y:" + location.getBlockY()
                        + " Z:" + location.getBlockZ() + ")";
            }
            Bukkit.broadcastMessage(message);
        } else {
            // A global broadcast already reaches the angler; do not send a duplicate.
            player.sendMessage("§a[Magic Fishing] §fYou caught " + catchDescription);
        }
    }
}
