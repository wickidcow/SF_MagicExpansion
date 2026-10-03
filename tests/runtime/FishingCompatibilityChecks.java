package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

/** Real Paper item/entity APIs and registered handlers, using a no-network player facade. */
public final class FishingCompatibilityChecks {
    private FishingCompatibilityChecks() {}

    public static void run() throws Exception {
        MagicExpansion plugin = MagicExpansion.getInstance();
        var compatibility = plugin.getFishingCompatibility();
        Object originalMode = plugin.getConfig().get("Fish.Compatibility.mode");
        try {
            for (boolean waterCloud : new boolean[] {false, true}) {
                plugin.getConfig().set("Fish.Compatibility.mode", "COMPATIBILITY");
                compatibility.reload();
                checkCatch(waterCloud, false, false);
                plugin.getConfig().set("Fish.Compatibility.mode", "AUTO");
                compatibility.reload();
                require(compatibility.handlesCatches(), "Unexpected external fishing plugin in isolated server");
                checkCatch(waterCloud, true, false);
                checkCatch(waterCloud, false, true);
            }
        } finally {
            plugin.getConfig().set("Fish.Compatibility.mode", originalMode);
            compatibility.reload();
        }
    }

    private static void checkCatch(boolean waterCloud, boolean expectReplacement, boolean cancelled) throws Exception {
        var world = Bukkit.getWorlds().getFirst();
        var location = world.getSpawnLocation().add(0, 4, 0);
        var block = location.getBlock();
        var originalBlock = block.getBlockData().clone();
        var existingEntities = world.getEntities().stream().map(entity -> entity.getUniqueId()).toList();
        List<String> messages = new ArrayList<>();
        ItemStack[] slots = new ItemStack[41];
        slots[0] = (waterCloud ? MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO
                : MagicExpansionItems.FISHING_ROD_LOG).clone();
        slots[40] = MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA.clone();
        slots[40].setAmount(17);
        ItemStack rodBefore = slots[0].clone();
        ItemStack lureBefore = slots[40].clone();
        PlayerInventory inventory = (PlayerInventory) Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(),
                new Class<?>[] {PlayerInventory.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getItemInMainHand" -> slots[0];
                    case "getItemInOffHand" -> slots[40];
                    case "getContents", "getStorageContents" -> slots;
                    case "getSize" -> slots.length;
                    case "getItem" -> slots[(Integer) args[0]];
                    case "setItem" -> { slots[(Integer) args[0]] = (ItemStack) args[1]; yield null; }
                    case "iterator" -> Arrays.asList(slots).listIterator();
                    default -> throw new UnsupportedOperationException("Unexpected inventory call: " + method);
                });
        UUID uuid = UUID.randomUUID();
        Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "isOnline", "isValid" -> true;
                    case "isDead" -> false;
                    case "getInventory" -> inventory;
                    case "getWorld" -> world;
                    case "getLocation" -> location.clone().add(2, 0, 0);
                    case "getName" -> "FishingCompatibilityAudit";
                    case "sendMessage" -> { messages.add(Arrays.toString(args)); yield null; }
                    case "playSound", "updateInventory" -> null;
                    case "hashCode" -> uuid.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "NoNetworkFishingFixture";
                    default -> throw new UnsupportedOperationException("Unexpected player call: " + method);
                });
        FishHook hook = (FishHook) Proxy.newProxyInstance(FishHook.class.getClassLoader(), new Class<?>[] {FishHook.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getLocation" -> location.clone();
                    default -> throw new UnsupportedOperationException("Unexpected hook call: " + method);
                });
        ItemStack fish = new ItemStack(Material.COD);
        var meta = fish.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("external_fishing", "species"),
                PersistentDataType.STRING, "test-fish");
        fish.setItemMeta(meta);
        // Vanilla dispatches the event before adding its newly created catch to the world.
        Item caught = world.createEntity(location, Item.class);
        caught.setItemStack(fish);
        require(!caught.isValid() && !caught.isDead(), "Expected a pending, living vanilla-style catch");
        var event = new PlayerFishEvent(player, caught, hook, EquipmentSlot.HAND, PlayerFishEvent.State.CAUGHT_FISH);
        event.setCancelled(cancelled);
        event.setExpToDrop(13);
        try {
            block.setType(Material.WATER, false);
            int handlers = 0;
            for (var registered : PlayerFishEvent.getHandlerList().getRegisteredListeners()) {
                if (registered.getPlugin() == MagicExpansion.getInstance()) {
                    registered.callEvent(event);
                    handlers++;
                }
            }
            require(handlers == 1, "Both rod families must use exactly one compatibility entry point");
            require(event.isCancelled() == cancelled && event.getExpToDrop() == 13, "Cancellation or XP changed");
            require(slots[0].equals(rodBefore), "Existing rod data changed");
            if (expectReplacement) {
                require(caught.isDead(), "Full fishing did not replace the catch for rod family " + waterCloud);
                require(!messages.isEmpty(), "Upstream catch behavior was not called");
                require(slots[40].getAmount() == (waterCloud ? 16 : 17), "Unexpected lure consumption");
                require(world.getEntities().stream().anyMatch(entity -> entity instanceof Item
                        && !existingEntities.contains(entity.getUniqueId())), "Missing full-mode fishing reward");
            } else {
                require(!caught.isDead() && fish.equals(caught.getItemStack()), "External catch was changed");
                require(slots[40].equals(lureBefore), "Bait was consumed while fishing was paused/cancelled");
                require(messages.isEmpty(), "Catch effects ran while fishing was paused/cancelled");
                require(world.getEntities().stream().allMatch(entity -> existingEntities.contains(entity.getUniqueId())),
                        "Extra fishing reward or effect was spawned");
            }
        } finally {
            caught.remove();
            world.getEntities().stream().filter(entity -> !existingEntities.contains(entity.getUniqueId()))
                    .forEach(entity -> entity.remove());
            block.setBlockData(originalBlock, false);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
