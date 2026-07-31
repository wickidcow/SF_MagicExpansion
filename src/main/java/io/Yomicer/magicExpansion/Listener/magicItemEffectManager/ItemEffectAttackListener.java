package io.Yomicer.magicExpansion.Listener.magicItemEffectManager;

import io.Yomicer.magicExpansion.Listener.weaponApply.SlownessManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

import static io.Yomicer.magicExpansion.items.enchantMachine.EnchantingTable.ATTRIBUTE_POOL;
import static io.Yomicer.magicExpansion.utils.ItemPermissionUtils.hasPermissionOnAttack;

public class ItemEffectAttackListener implements Listener {

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();

            // 获取物品的 PDC
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer() != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                // 遍历所有属性
                for (String attribute : ATTRIBUTE_POOL.keySet()) {
                    NamespacedKey key = new NamespacedKey("magicexpansion", attribute.toLowerCase().replace(".", "_"));

                    if (pdc.has(key, PersistentDataType.INTEGER)) {
                        int value = pdc.get(key, PersistentDataType.INTEGER);
                        applyEffect(attribute, value, event.getEntity(),player);
                   } else if (pdc.has(key, PersistentDataType.BOOLEAN)) {
                        boolean value = pdc.get(key, PersistentDataType.BOOLEAN);
                        applyEffect(attribute, value, event.getEntity(),player);
                    }
                }
            }
        }
    }

    private void applyEffect(String attribute, Object value, Entity target, Player player) {
        if (!(target instanceof LivingEntity livingTarget)) return;
        if(!hasPermissionOnAttack(player)) return;
        switch (attribute) {
            case "MagicExpansion.Knockback": // 击退效果
                if (value instanceof Integer knockbackStrength) {
                    Location location = livingTarget.getLocation();
                    location.setY(location.getY() + knockbackStrength*2);
                    livingTarget.teleport(location);
                    double knockbackStrengthDouble = (double) knockbackStrength;
                    Vector velocity = calculateKnockbackVelocity(livingTarget, knockbackStrengthDouble);

                    // 检查 Vector 是否合法
                    if (isValidVector(velocity)) {
                        livingTarget.setVelocity(velocity);
                    } else {
                    }
                }
                break;

            case "MagicExpansion.Slowness": // 减速效果
                if (value instanceof Integer slownessLevel) {

                    // 施加减速效果(减速等级为 50%,持续 2 秒)
//                    SlownessManager.applySlowness(livingTarget, slownessLevel, slownessLevel); // 40 ticks = 2 秒
                    livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessLevel*3, slownessLevel));

                }
                break;

            case "MagicExpansion.Attraction": // 吸引效果
                if (value instanceof Boolean attractionEnabled && attractionEnabled) {
                    Location targetLoc = target.getLocation();
                    Location playerLoc = player.getLocation();
                    target.setVelocity(playerLoc.toVector().subtract(targetLoc.toVector()).normalize().multiply(2));
                }
                break;

            case "MagicExpansion.FireDamage": // 火焰伤害
                if (value instanceof Integer fireTicks) {
                    target.setFireTicks(fireTicks * 40 ); // 每秒 20 ticks  2*level seconds
                }
                break;

            case "MagicExpansion.Lightning": // 雷击效果
                if (value instanceof Boolean lightningEnabled && lightningEnabled) {
                    target.getWorld().strikeLightning(target.getLocation());
                }
                break;

            case "MagicExpansion.AttackTpToTp": // 换位效果
                if (value instanceof Boolean AttackTpToTp && AttackTpToTp) {
                    swapPositionsAndDirections(target, player);
                }
                break;
        }
    }


    private Vector calculateKnockbackVelocity(LivingEntity target, double strength) {
        Random random = new Random();
        // 示例:根据目标的方向计算击退速度
        Vector direction = target.getLocation().getDirection();
        double speedX = direction.getX() * strength * 0.1;
        double speedY = Math.max(0.1, strength*1.2); // 确保 Y 分量非负
        double speedZ = direction.getZ() * strength * 0.1;

        return new Vector(speedX, speedY, speedZ);
    }

    private boolean isValidVector(Vector vector) {
        return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }


    private void swapPositionsAndDirections(Entity target, Player player) {
        // 获取玩家和目标的位置
        Location playerLocation = player.getLocation();
        Location targetLocation = target.getLocation();

        // 保存玩家和目标的朝向(Yaw 和 Pitch)
        float playerYaw = playerLocation.getYaw();
        float playerPitch = playerLocation.getPitch();
        float targetYaw = targetLocation.getYaw();
        float targetPitch = targetLocation.getPitch();

        // 创建新的位置对象以避免引用问题
        Location newPlayerLocation = targetLocation.clone();
        Location newTargetLocation = playerLocation.clone();

        // 设置新的朝向
        newPlayerLocation.setYaw(targetYaw);
        newPlayerLocation.setPitch(targetPitch);
        newTargetLocation.setYaw(playerYaw);
        newTargetLocation.setPitch(playerPitch);

        // 将玩家传送到目标的位置,并更新朝向
        player.teleport(newPlayerLocation);

        // 将目标传送到玩家的位置,并更新朝向
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).teleport(newTargetLocation);
        } else {
            // 如果目标不是 LivingEntity,则仅传送位置
            target.teleport(newTargetLocation);
        }

        // 发送消息给玩家
        player.sendMessage("§byou target Location!");
    }

}
