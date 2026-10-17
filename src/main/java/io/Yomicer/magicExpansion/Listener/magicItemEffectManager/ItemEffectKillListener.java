package io.Yomicer.magicExpansion.Listener.magicItemEffectManager;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.GiveItem;
import io.Yomicer.magicExpansion.utils.compat.EntityHelper;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import io.Yomicer.magicExpansion.utils.entity.EntityEgg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;
import java.util.UUID;

public class ItemEffectKillListener implements Listener {

    private static final String KILL_SNAPSHOT_KEY = "magicexpansion_entity_spawn_kill";
    private static final NamespacedKey ENTITY_SPAWN_KEY =
            new NamespacedKey("magicexpansion", "magicexpansion_entityspawn");

    private record KillSnapshot(UUID killer, int level, String weaponName) {}

    /**
     * Snapshot the actual empowered weapon that delivered a lethal direct hit.
     * The snapshot is removed one tick later if the target somehow survives.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLethalHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)
                || !(event.getEntity() instanceof LivingEntity target)
                || event.getFinalDamage() + 1.0E-9D < target.getHealth()) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        ItemMeta meta = weapon == null ? null : weapon.getItemMeta();
        Integer level = meta == null ? null : meta.getPersistentDataContainer().get(
                ENTITY_SPAWN_KEY, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        KillSnapshot snapshot = new KillSnapshot(
                player.getUniqueId(),
                level,
                ItemStackHelper.getDisplayName(weapon));
        target.setMetadata(
                KILL_SNAPSHOT_KEY,
                new FixedMetadataValue(MagicExpansion.getInstance(), snapshot));

        Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
            if (target.isValid() && !target.isDead()) {
                target.removeMetadata(KILL_SNAPSHOT_KEY, MagicExpansion.getInstance());
            }
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }

        KillSnapshot snapshot = getSnapshot(entity);
        entity.removeMetadata(KILL_SNAPSHOT_KEY, MagicExpansion.getInstance());
        if (snapshot == null || !snapshot.killer().equals(killer.getUniqueId())) {
            return;
        }

        applyEntitySpawnEffect(snapshot.level(), entity, killer, snapshot.weaponName());
    }

    private KillSnapshot getSnapshot(LivingEntity entity) {
        for (MetadataValue value : entity.getMetadata(KILL_SNAPSHOT_KEY)) {
            if (value.getOwningPlugin() == MagicExpansion.getInstance()
                    && value.value() instanceof KillSnapshot snapshot) {
                return snapshot;
            }
        }
        return null;
    }

    private void applyEntitySpawnEffect(
            int level, Entity target, Player player, String weaponName) {
        Material spawnEggMaterial = EntityEgg.hasSpawnEgg(target.getType());
        if (spawnEggMaterial == null) {
            return;
        }

        double chance = Math.max(0.0D, Math.min(1.0D, level * 0.0005D));
        if (new Random().nextDouble() >= chance) {
            return;
        }

        ItemStack spawnEgg = new ItemStack(spawnEggMaterial);
        player.sendMessage("§bYou defeated §d" + EntityHelper.getName(target) + "§b.");
        player.sendMessage("§bYour weapon §e[" + weaponName
                + "§e] §bused §cLife Reconstruction §bon §d"
                + EntityHelper.getName(target) + "§b.");
        player.sendMessage("§bYou received a §e" + EntityHelper.getName(target)
                + " §bspawn egg.");

        GiveItem.giveOrDropItem(player, spawnEgg);
    }
}
