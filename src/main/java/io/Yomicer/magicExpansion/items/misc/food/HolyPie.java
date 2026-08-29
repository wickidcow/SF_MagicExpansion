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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HolyPie extends SimpleSlimefunItem<ItemConsumptionHandler> implements Listener {

    // 修复(R)：改为 uuid -> 庇护到期时间戳(毫秒)。连续食用时新时间戳覆盖旧值，
    // 移除任务只在自己时间戳一致时清除，防止连续食用互相撤销庇护
    private static final Map<UUID, Long> holyProtectedPlayers = new ConcurrentHashMap<>();

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
            // 修复(R)：记录庇护到期时间戳（30 ticks = 1500ms）；连续食用会覆盖为新时间戳
            long expireAt = System.currentTimeMillis() + 1500L;
            holyProtectedPlayers.put(p.getUniqueId(), expireAt);
            new BukkitRunnable() {
                @Override
                public void run() {
                    // 修复(R)：仅当记录的到期时间戳与本次任务一致时才清除，
                    // 防止连续食用第二颗时被第一颗的过期任务提前撤销庇护
                    Long current = holyProtectedPlayers.get(p.getUniqueId());
                    if (current != null && current == expireAt) {
                        holyProtectedPlayers.remove(p.getUniqueId());
                        if (p.isOnline()) {
                            p.sendMessage(ChatColor.GRAY + "§7神圣庇护已消散...");
                        }
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
                    // 修复(R)：集合类型改为 Map，使用 containsKey 判断庇护是否有效
                    if (!p.isOnline() || !holyProtectedPlayers.containsKey(p.getUniqueId())) {
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
        // 修复(R)：显式排除缓降(SLOW_FALLING)——它包含 SLOW 关键字但属于增益效果，不应被清除
        if (name.contains("SLOW")) {
            return !name.contains("SLOW_FALLING");
        }
        return name.contains("POISON") || name.contains("WITHER") || name.contains("BLINDNESS")
                || name.contains("CONFUSION") || name.contains("HUNGER") || name.contains("WEAKNESS")
                || name.contains("DIGGING") || name.contains("LEVITATION")
                || name.contains("UNLUCK") || name.contains("BAD_OMEN") || name.contains("DARKNESS");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        if (holyProtectedPlayers.containsKey(p.getUniqueId())) {
            // 修复(R)：VOID 伤害不取消（否则庇护期间悬空会不死、卡死虚空），仅取消其他伤害
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                return;
            }
            event.setCancelled(true);
            p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
        }
    }

}
