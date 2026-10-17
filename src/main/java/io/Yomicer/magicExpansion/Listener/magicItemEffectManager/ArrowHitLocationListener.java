package io.Yomicer.magicExpansion.Listener.magicItemEffectManager;

import io.Yomicer.magicExpansion.utils.GiveItem;
import io.Yomicer.magicExpansion.utils.entity.EntityEgg;
import io.Yomicer.magicExpansion.utils.compat.EntityHelper;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;



public class ArrowHitLocationListener implements Listener {

    private static final NamespacedKey WEAPON_ARROW_TP_KEY =
            new NamespacedKey("magicexpansion", "magicexpansion_arrowtp");
    private static final NamespacedKey PROJECTILE_ARROW_TP_KEY =
            new NamespacedKey("magicexpansion", "arrow_tp_projectile");

    @EventHandler(ignoreCancelled = true)
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)
                || !(arrow.getShooter() instanceof Player player)) {
            return;
        }

        if (hasArrowTeleport(player.getInventory().getItemInMainHand())
                || hasArrowTeleport(player.getInventory().getItemInOffHand())) {
            arrow.getPersistentDataContainer().set(
                    PROJECTILE_ARROW_TP_KEY, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)
                || !(arrow.getShooter() instanceof Player player)
                || !player.isOnline()
                || player.isDead()) {
            return;
        }

        Byte marker = arrow.getPersistentDataContainer().get(
                PROJECTILE_ARROW_TP_KEY, PersistentDataType.BYTE);
        if (marker == null || marker == 0) {
            return;
        }

        Location hitLocation;
        if (event.getHitEntity() != null) {
            Entity hitEntity = event.getHitEntity();
            if (!Slimefun.getProtectionManager().hasPermission(
                    player, hitEntity.getLocation(), Interaction.INTERACT_ENTITY)) {
                player.sendMessage("§cThe Ender Pearl Arrow cannot teleport you to that protected target.");
                return;
            }
            hitLocation = hitEntity.getLocation().clone();
        } else if (event.getHitBlock() != null) {
            hitLocation = event.getHitBlock().getLocation().clone();
            if (!Slimefun.getProtectionManager().hasPermission(
                    player, hitLocation, Interaction.INTERACT_BLOCK)) {
                player.sendMessage("§cThe Ender Pearl Arrow cannot teleport you into that protected area.");
                return;
            }
        } else {
            hitLocation = arrow.getLocation().clone();
            if (!Slimefun.getProtectionManager().hasPermission(
                    player, hitLocation, Interaction.INTERACT_BLOCK)) {
                return;
            }
        }

        World world = hitLocation.getWorld();
        if (world == null) {
            return;
        }

        double y = Math.min(world.getMaxHeight() - 2.0D, hitLocation.getY() + 1.0D);
        y = Math.max(world.getMinHeight(), y);
        hitLocation.setY(y);

        if (!world.isChunkLoaded(hitLocation.getBlockX() >> 4, hitLocation.getBlockZ() >> 4)) {
            return;
        }

        if (!hitLocation.getBlock().getType().isAir()
                || !hitLocation.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            player.sendMessage("§cThe Ender Pearl Arrow landing point is obstructed.");
            return;
        }

        // A vertically bounded claim may differ from the arrow's hit location.
        if (!Slimefun.getProtectionManager().hasPermission(
                player, hitLocation, Interaction.INTERACT_BLOCK)) {
            player.sendMessage("§cThe Ender Pearl Arrow landing point is protected.");
            return;
        }

        if (player.teleport(hitLocation)) {
            player.sendMessage("§bTeleported to the Ender Pearl Arrow landing point.");
        }
    }

    private boolean hasArrowTeleport(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        Boolean enabled = meta.getPersistentDataContainer().get(
                WEAPON_ARROW_TP_KEY, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(enabled);
    }







}
