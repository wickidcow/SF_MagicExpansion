package io.Yomicer.magicExpansion.utils.energy;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getRandomGradientName;

/**
 * Owns the short-lived chat configuration session used by Power Card style items.
 */
public final class PowerCardConfigurationManager {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private PowerCardConfigurationManager() {}

    public static void begin(
            Player player,
            EquipmentSlot hand,
            ItemStack clickedItem,
            String expectedItemId,
            int preservedLoreLines) {
        if (clickedItem == null || clickedItem.getAmount() != 1) {
            player.sendMessage("§cSeparate the energy-storage item into a stack of one before configuring it.");
            return;
        }

        SlimefunItem sfItem = SlimefunItem.getByItem(clickedItem);
        if (sfItem == null || !expectedItemId.equals(sfItem.getId())) {
            player.sendMessage("§cThe energy-storage item could not be verified.");
            return;
        }

        Session previous = SESSIONS.remove(player.getUniqueId());
        if (previous != null) {
            previous.close(false);
        }

        Session session = new Session(player, hand, expectedItemId, preservedLoreLines);
        SESSIONS.put(player.getUniqueId(), session);
        MagicExpansion plugin = MagicExpansion.getInstance();
        plugin.getServer().getPluginManager().registerEvents(session, plugin);
        session.timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (SESSIONS.get(player.getUniqueId()) == session) {
                session.close(true);
                if (player.isOnline()) {
                    player.sendMessage(getRandomGradientName(
                            "[MagicExpansion] Input timed out after 15 seconds; cancelled."));
                }
            }
        }, 300L);

        player.sendMessage(getRandomGradientName(
                "[MagicExpansion] Enter maximum energy storage (1 to " + Long.MAX_VALUE + "):"));
        player.sendMessage(getRandomGradientName("Type 'cancel' to cancel."));
    }

    public static void shutdown() {
        for (Session session : new ArrayList<>(SESSIONS.values())) {
            session.close(false);
        }
        SESSIONS.clear();
    }

    static int activeSessionCount() {
        return SESSIONS.size();
    }

    private static ItemStack heldItem(Player player, EquipmentSlot hand) {
        return hand == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
    }

    private static final class Session implements Listener {
        private final UUID playerId;
        private final EquipmentSlot hand;
        private final String expectedItemId;
        private final int preservedLoreLines;
        private BukkitTask timeoutTask;
        private boolean closed;

        private Session(Player player, EquipmentSlot hand, String expectedItemId, int preservedLoreLines) {
            this.playerId = player.getUniqueId();
            this.hand = hand;
            this.expectedItemId = expectedItemId;
            this.preservedLoreLines = Math.max(0, preservedLoreLines);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().getUniqueId().equals(playerId)) {
                return;
            }

            event.setCancelled(true);
            String message = event.getMessage().trim();
            MagicExpansion plugin = MagicExpansion.getInstance();
            plugin.getServer().getScheduler().runTask(plugin, () -> handleMessage(event.getPlayer(), message));
        }

        private void handleMessage(Player player, String message) {
            if (closed || SESSIONS.get(playerId) != this) {
                return;
            }

            if ("cancel".equalsIgnoreCase(message)) {
                player.sendMessage(getRandomGradientName("[MagicExpansion] Configuration cancelled."));
                close(false);
                return;
            }

            final long maximum;
            try {
                maximum = Long.parseLong(message);
            } catch (NumberFormatException exception) {
                player.sendMessage(getRandomGradientName(
                        "Error: '" + message + "' is not a valid integer. Enter a whole number."));
                return;
            }

            if (maximum <= 0L) {
                player.sendMessage(getRandomGradientName("Error: Enter a whole number greater than 0."));
                return;
            }

            ItemStack item = heldItem(player, hand);
            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            if (item == null || item.getAmount() != 1 || sfItem == null || !expectedItemId.equals(sfItem.getId())) {
                player.sendMessage(getRandomGradientName(
                        "Configuration cancelled because the original energy-storage item is no longer in that hand."));
                close(false);
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                player.sendMessage(getRandomGradientName("Configuration cancelled because the item metadata is unavailable."));
                close(false);
                return;
            }

            MagicExpansion plugin = MagicExpansion.getInstance();
            NamespacedKey maxPower = new NamespacedKey(plugin, "max_power");
            NamespacedKey nowPower = new NamespacedKey(plugin, "now_power");
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            long stored = pdc.getOrDefault(nowPower, PersistentDataType.LONG, 0L);

            if (stored < 0L) {
                player.sendMessage(getRandomGradientName(
                        "Configuration cancelled because the stored-energy value is invalid."));
                close(false);
                return;
            }

            if (!PowerRelayMath.canSetMaximum(stored, maximum)) {
                player.sendMessage(getRandomGradientName(
                        "The maximum cannot be lower than the card's currently stored energy (" + stored + " J)."));
                return;
            }

            pdc.set(maxPower, PersistentDataType.LONG, maximum);
            pdc.set(nowPower, PersistentDataType.LONG, stored);

            List<String> lore = new ArrayList<>();
            if (meta.hasLore() && meta.getLore() != null) {
                for (String line : meta.getLore()) {
                    if (line != null && line.contains("Stored Energy:")) {
                        continue;
                    }
                    if (lore.size() >= preservedLoreLines) {
                        break;
                    }
                    lore.add(line);
                }
            }
            lore.add("§x§F§F§3§2§C§EStored Energy: " + stored + "/" + maximum + " J");
            meta.setLore(lore);
            item.setItemMeta(meta);

            player.sendMessage(getRandomGradientName(
                    "✔ Maximum energy storage set to: " + maximum + " J"));
            player.updateInventory();
            close(false);
        }

        private void close(boolean fromTimeout) {
            if (closed) {
                return;
            }
            closed = true;
            SESSIONS.remove(playerId, this);
            if (!fromTimeout && timeoutTask != null && !timeoutTask.isCancelled()) {
                timeoutTask.cancel();
            }
            HandlerList.unregisterAll(this);
        }
    }
}
