package audit;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ADataContainer;
import io.Yomicer.magicExpansion.items.misc.weapon.StarShardsSword;
import io.Yomicer.magicExpansion.utils.CargoStorage;
import io.Yomicer.magicExpansion.utils.SameItemJudge;
import io.Yomicer.magicExpansion.utils.SwordAttackGuard;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

/** Real server/item APIs with an explicit no-network player facade; no live-client claim. */
public final class Upstream13Checks {
    private Upstream13Checks() {}

    public static void run() throws Exception {
        MemoryData data = new MemoryData();
        ItemStack item = new ItemStack(Material.DIAMOND, 7);
        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        pdc.set(NamespacedKey.fromString("slimefun:slimefun_item"), PersistentDataType.STRING, "OLD_UNREGISTERED_ID");
        pdc.set(NamespacedKey.fromString("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);
        pdc.set(NamespacedKey.fromString("old:count"), PersistentDataType.LONG, 9007199254740993L);
        var nested = pdc.getAdapterContext().newPersistentDataContainer();
        nested.set(NamespacedKey.fromString("old:owner"), PersistentDataType.STRING, "retained");
        pdc.set(NamespacedKey.fromString("old:nested"), PersistentDataType.TAG_CONTAINER, nested);
        item.setItemMeta(meta);
        require(CargoStorage.store(data, item, Integer.MAX_VALUE, 1) == Integer.MAX_VALUE, "Large cargo import failed");
        require(item.equals(SameItemJudge.itemFromBase64(data.getData("item_type_0"))), "Typed cargo item changed");
        var before = Map.copyOf(data.getAllData());
        require(CargoStorage.store(data, new ItemStack(Material.STONE), 64, 1) == 0, "Full cargo accepted new type");
        require(before.equals(data.getAllData()), "Full cargo modified its existing record");
        data.setData("item_count_0", Long.toString(Long.MAX_VALUE - 2));
        require(CargoStorage.store(data, item, 7, 1) == 2, "Long cargo capacity was not bounded");
        require(Long.toString(Long.MAX_VALUE).equals(data.getData("item_count_0")), "Cargo overflowed");

        var sword = (StarShardsSword) SlimefunItem.getById("MAGIC_EXPANSION_WEAPON_STAR_SHARDS_SWORD");
        require(sword != null, "Sword registration missing");
        var world = Bukkit.getWorlds().getFirst();
        var location = world.getSpawnLocation().add(0, 3, 0);
        UUID uuid = UUID.randomUUID();
        Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "isOnline", "isValid" -> true;
                    case "isDead", "isInvulnerable" -> false;
                    case "getWorld" -> world;
                    case "getLocation" -> location.clone();
                    case "getName" -> "Upstream13Audit";
                    case "sendMessage", "playSound" -> null;
                    case "hashCode" -> uuid.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "NoNetworkSwordFixture";
                    default -> throw new UnsupportedOperationException("Unexpected sword fixture call: " + method);
                });
        Zombie target = world.spawn(location, Zombie.class, zombie -> zombie.setAI(false));
        Arrow arrow = world.spawn(location, Arrow.class);
        try {
            var cancelled = new EntityDamageByEntityEvent(player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
            cancelled.setCancelled(true);
            // Dispatch only the sword listener's registered handler, with Bukkit's cancellation rules.
            for (var registered : EntityDamageByEntityEvent.getHandlerList().getRegisteredListeners()) {
                if (registered.getListener() == sword) registered.callEvent(cancelled);
            }
            require(target.getHealth() == target.getMaxHealth(), "Cancelled sword attack applied damage");
            var active = new EntityDamageByEntityEvent(player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
            SwordAttackGuard.run(() -> sword.onPlayerAttack(active));
            require(target.getHealth() == target.getMaxHealth(), "Nested sword attack applied damage");
            var shield = StarShardsSword.class.getDeclaredMethod("useAstralShield", Player.class);
            shield.setAccessible(true);
            shield.invoke(sword, player);
            arrow.setVelocity(new Vector(1, 0, 0));
            var hit = new EntityDamageByEntityEvent(arrow, player, EntityDamageEvent.DamageCause.PROJECTILE, 1);
            sword.onEntityDamage(hit);
            require(hit.isCancelled() && arrow.getVelocity().getX() < 0, "Shield did not reflect projectile");
            StarShardsSword.cleanup(uuid);
            var after = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, 1);
            sword.onEntityDamage(after);
            require(!after.isCancelled(), "Player cleanup retained shield protection");
        } finally {
            StarShardsSword.cleanup(uuid);
            target.remove();
            arrow.remove();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class MemoryData extends ADataContainer {
        private MemoryData() { super("upstream13-audit"); setIsDataLoaded(true); }
        @Override public void setData(String key, String value) { setCacheInternal(key, value, true); }
        @Override public void removeData(String key) { removeCacheInternal(key); }
    }
}
