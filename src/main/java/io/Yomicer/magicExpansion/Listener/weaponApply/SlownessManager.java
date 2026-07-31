package io.Yomicer.magicExpansion.Listener.weaponApply;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SlownessManager {

    // 用于存储减速修饰符 ID
    private static final Map<UUID, UUID> playerSlownessModifierIds = new ConcurrentHashMap<>();

    /**
     * 施加减速效果
     *
     * @param livingTarget 目标生物
     * @param slownessLevel 减速等级(0 ~ 1)
     * @param durationTicks 持续时间(单位:tick,20 ticks = 1 秒)
     */
    public static void applySlowness(LivingEntity livingTarget, double slownessLevel, int durationTicks) {
        AttributeInstance movementSpeed = livingTarget.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        // 先移除所有已有的修饰符
        removeAllModifiers(movementSpeed);
        UUID playerId = livingTarget.getUniqueId();
        UUID modifierId = playerSlownessModifierIds.get(playerId);

        // 如果已经存在减速修饰符,则先移除
        if (modifierId != null) {
            removeExistingModifier(movementSpeed, modifierId);
        }

        // 计算减速比例
        double baseSpeed = movementSpeed.getBaseValue();
        double reducedSpeed = baseSpeed * (1 - slownessLevel*0.0099); // 减速比例

        // 生成新的修饰符 ID
        modifierId = UUID.randomUUID();
        playerSlownessModifierIds.put(playerId, modifierId);

        // 创建减速修饰符
        AttributeModifier modifier = new AttributeModifier(
                modifierId,
                "SlownessEffect",
                reducedSpeed - baseSpeed, // 减速值
                AttributeModifier.Operation.ADD_NUMBER
        );

        // 添加减速修饰符
        movementSpeed.addModifier(modifier);

        // 调试信息
//        Debug.logInfo("Slowness applied to " + livingTarget.getName());

        // 延迟移除减速效果
        new BukkitRunnable() {
            @Override
            public void run() {
                removeSlowness(livingTarget);
            }
        }.runTaskLater(MagicExpansion.getInstance(), durationTicks*3);
    }

    /**
     * 移除减速效果
     *
     * @param livingTarget 目标生物
     */
    public static void removeSlowness(LivingEntity livingTarget) {
        AttributeInstance movementSpeed = livingTarget.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        UUID playerId = livingTarget.getUniqueId();
        UUID modifierId = playerSlownessModifierIds.get(playerId);

        if (modifierId != null) {
            removeExistingModifier(movementSpeed, modifierId);
            playerSlownessModifierIds.remove(playerId);

            // 调试信息
//            Debug.logInfo("Slowness removed from " + livingTarget.getName());
        }
    }

    /**
     * 移除指定的修饰符
     *
     * @param movementSpeed 属性实例
     * @param modifierId    需要移除的修饰符 ID
     */
    private static void removeExistingModifier(AttributeInstance movementSpeed, UUID modifierId) {
        Collection<AttributeModifier> modifiers = movementSpeed.getModifiers();
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getUniqueId().equals(modifierId)) {
                movementSpeed.removeModifier(modifier);
                break;
            }
        }
    }

    /**
     * 移除目标属性实例上的所有修饰符
     *
     * @param attributeInstance 属性实例
     */
    private static void removeAllModifiers(AttributeInstance attributeInstance) {
        Collection<AttributeModifier> modifiers = attributeInstance.getModifiers();
        for (AttributeModifier modifier : modifiers) {
            attributeInstance.removeModifier(modifier);
        }
    }

}
