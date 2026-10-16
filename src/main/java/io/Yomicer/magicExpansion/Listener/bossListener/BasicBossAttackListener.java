package io.Yomicer.magicExpansion.Listener.bossListener;

import io.Yomicer.magicExpansion.utils.log.Debug;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class BasicBossAttackListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {


        if (!(event.getEntity() instanceof LivingEntity mob)) {
            return;
        }

        if (mob.getType() == EntityType.ZOMBIE && mob.hasMetadata("isInvincibleFireZombie")) {
            boolean invincible = mob.getMetadata("isInvincibleFireZombie").get(0).asBoolean();
            if (invincible) {
                event.setCancelled(true);
                return;
            }

            double threshold = mob.getMaxHealth() * 0.05;
            if (event.getDamage() > threshold) {
                event.setDamage(threshold);
            }
        }

        if (mob.getType() == EntityType.ALLAY && mob.hasMetadata("isInvincibleWindElf")) {
            boolean invincible = mob.getMetadata("isInvincibleWindElf").stream()
                    .anyMatch(value -> value.asBoolean());

            // Wind Spirit's guide contract is sword-only damage. During the cast
            // phase it is fully invulnerable; during the vulnerable phase only the
            // EntityDamageByEntity handler may approve a coordinated sword hit.
            if (invincible || !(event instanceof EntityDamageByEntityEvent)) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)
                || mob.getType() != EntityType.ALLAY
                || !mob.hasMetadata("isInvincibleWindElf")) {
            return;
        }

        boolean invincible = mob.getMetadata("isInvincibleWindElf").stream()
                .anyMatch(value -> value.asBoolean());
        if (invincible) {
            event.setCancelled(true);
            return;
        }

        Player attacker = resolvePlayerAttacker(event.getDamager());
        if (attacker == null) {
            // Wind Spirit's combat mechanic is explicitly a coordinated player/sword fight.
            event.setDamage(0.0D);
            return;
        }

        int nearbyPlayers = 0;
        Location mobLocation = mob.getLocation();
        World world = mobLocation.getWorld();
        if (world != null) {
            double radiusSquared = 20.0D * 20.0D;
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(mobLocation) <= radiusSquared) {
                    nearbyPlayers++;
                }
            }
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        // Projectile attacks are never sword attacks, even when the shooter currently
        // happens to be holding a sword.
        if (event.getDamager() instanceof Projectile
                || !isSword(weapon)
                || nearbyPlayers < 2
                || nearbyPlayers > 5) {
            event.setDamage(0.0D);
            return;
        }

        double maxHealth = mob.getMaxHealth();
        double threshold = maxHealth * 0.33D;
        if (event.getDamage() > threshold) {
            event.setDamage(threshold);
        }
    }

    private Player resolvePlayerAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }


    /**
     * 判断给定的物品是否为剑
     */
    private boolean isSword(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.WOODEN_SWORD ||
                type == Material.STONE_SWORD ||
                type == Material.GOLDEN_SWORD ||
                type == Material.IRON_SWORD ||
                type == Material.DIAMOND_SWORD ||
                type == Material.NETHERITE_SWORD; // 如果你的服务器版本支持下界合金剑
    }




    }
