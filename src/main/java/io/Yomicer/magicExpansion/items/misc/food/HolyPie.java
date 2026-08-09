package io.Yomicer.magicExpansion.items.misc.food; // 请修改为你的包名

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemConsumptionHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HolyPie extends SimpleSlimefunItem<ItemConsumptionHandler> implements Listener {

    private static final Set<UUID> holyProtectedPlayers = ConcurrentHashMap.newKeySet();

    public HolyPie(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        Bukkit.getPluginManager().registerEvents(this, MagicExpansion.getInstance());
    }
    public HolyPie(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
        Bukkit.getPluginManager().registerEvents(this, MagicExpansion.getInstance());
    }

    @Override
    public @NotNull ItemConsumptionHandler getItemHandler() {
        return (e, p, i) -> {
            double maxHealth = p.getMaxHealth();
            p.setHealth(Math.min(p.getHealth() + 12.0, maxHealth)); // 回 6 颗心
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30, 254));
            removeNegativeEffects(p);
            holyProtectedPlayers.add(p.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    holyProtectedPlayers.remove(p.getUniqueId());
                    if (p.isOnline()) {
                        p.sendMessage(ChatColor.GRAY + "§7神圣庇护已消散...");
                    }
                }
            }.runTaskLater(MagicExpansion.getInstance(), 30L);
            playHolyEffects(p);
            p.sendMessage(ChatColor.GOLD + "✨ " + ChatColor.BOLD + "神圣庇护激活！" + ChatColor.RESET + ChatColor.GRAY + " (免疫一切伤害)");
        };
    }

    /**
     * 播放神圣特效
     */
    private void playHolyEffects(Player p) {
        Location loc = p.getLocation().add(0, 1, 0);
        World world = p.getWorld();
        p.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 1.5f); // 空灵感
        p.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f); // 达成感
        p.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f); // 升级感
        world.spawnParticle(Particle.END_ROD, loc, 30, 0.5, 0.5, 0.5, 0.05);
        world.spawnParticle(Particle.HEART, loc, 15, 0.4, 0.4, 0.4, 0.1);
        world.spawnParticle(Particle.VILLAGER_HAPPY, loc, 20, 0.5, 0.5, 0.5, 0.1);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!p.isOnline() || !holyProtectedPlayers.contains(p.getUniqueId())) {
                    this.cancel();
                    return;
                }
                Location currentLoc = p.getLocation().add(0, 1.5, 0);
                // 画圈
                for (double angle = 0; angle < 360; angle += 45) {
                    double rad = Math.toRadians(angle);
                    double x = currentLoc.getX() + (Math.cos(rad) * 0.8);
                    double z = currentLoc.getZ() + (Math.sin(rad) * 0.8);
                    world.spawnParticle(Particle.FLASH, new Location(world, x, currentLoc.getY(), z), 1, 0, 0, 0, 0, Color.WHITE);
                }
                ticks++;
                if (ticks >= 10) this.cancel();
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 2L);
    }

    /**
     * 清除负面效果
     */
    private void removeNegativeEffects(Player p) {
        List<PotionEffectType> toRemove = new ArrayList<>();
        for (PotionEffect effect : p.getActivePotionEffects()) {
            if (isNegative(effect.getType())) {
                toRemove.add(effect.getType());
            }
        }
        for (PotionEffectType type : toRemove) {
            p.removePotionEffect(type);
        }
    }

    private boolean isNegative(PotionEffectType type) {
        String name = type.getName();
        return name.contains("POISON") || name.contains("WITHER") || name.contains("BLINDNESS")
                || name.contains("CONFUSION") || name.contains("HUNGER") || name.contains("WEAKNESS")
                || name.contains("SLOW") || name.contains("DIGGING") || name.contains("LEVITATION")
                || name.contains("UNLUCK") || name.contains("BAD_OMEN") || name.contains("DARKNESS");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        if (holyProtectedPlayers.contains(p.getUniqueId())) {
            event.setCancelled(true);
            if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
                p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }

}
