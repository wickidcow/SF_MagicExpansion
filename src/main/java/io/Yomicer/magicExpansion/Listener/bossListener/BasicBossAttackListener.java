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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class BasicBossAttackListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {


        if (event.getEntity() instanceof LivingEntity mob && mob.getType() == EntityType.ZOMBIE) {

            if (mob.hasMetadata("isInvincibleFireZombie")) {
                boolean isInvincibleFireZombie = mob.getMetadata("isInvincibleFireZombie").get(0).asBoolean();

                if (isInvincibleFireZombie) {
                    event.setDamage(0.01);
                }else{
                    double damage = event.getDamage();
                    double maxHealth = mob.getMaxHealth();
                    double threshold = maxHealth * 0.05;
                    if (damage > threshold) {
                        event.setDamage(threshold);
                    }
                }

            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

        // G: 短路判定①——攻击者必须是玩家，否则直接返回（原实现无条件遍历统计附近玩家，浪费性能）
        if (!(e.getDamager() instanceof Player player)) return;

        // G: 短路判定②——目标必须是生物实体
        if (!(e.getEntity() instanceof LivingEntity mob)) return;

        // G: 短路判定③——目标必须带本插件 Boss 元数据标记，普通生物不再做后续统计
        if (!mob.hasMetadata("isInvincibleWindElf")) return;

        boolean isInvincibleWindElf = mob.getMetadata("isInvincibleWindElf").get(0).asBoolean();

        // G: 短路判定通过后才遍历附近玩家统计（原来无论是否命中 Boss 都会执行）
        Location mobLocation = mob.getLocation();
        World world = mobLocation.getWorld();
        double radius = 20.0; // 范围：20 方块
        double radiusSquared = radius * radius;
        int playerCount = 0;
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(mobLocation) <= radiusSquared) {
                playerCount++;
            }
        }

        // G: 仅对风精灵 Boss（ALLAY 类型）应用伤害规则
        if (mob.getType() == EntityType.ALLAY) {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (isInvincibleWindElf || !isSword(weapon) || playerCount < 2 || playerCount > 5) {
                e.setDamage(0.0);
            } else {
                double damage = e.getDamage();
                double maxHealth = mob.getMaxHealth();
                double threshold = maxHealth * 0.33;
                if (damage > threshold) {
                    e.setDamage(threshold);
                }
            }
        }

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
