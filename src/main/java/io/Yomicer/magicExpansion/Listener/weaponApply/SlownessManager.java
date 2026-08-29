package io.Yomicer.magicExpansion.Listener.weaponApply;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SlownessManager {

    // 用于存储减速修饰符 ID（B2: 已为 ConcurrentHashMap，线程安全）
    private static final Map<UUID, UUID> playerSlownessModifierIds = new ConcurrentHashMap<>();

    // B3: 延迟移除任务引用列表，用于统一取消，防止插件卸载/重载后任务悬挂
    private static final List<BukkitTask> pendingRemoveTasks = new ArrayList<>();

    /**
     * 施加减速效果
     *
     * @param livingTarget 目标生物
     * @param slownessLevel 减速等级（0 ~ 1）
     * @param durationTicks 持续时间（单位：tick，20 ticks = 1 秒）
     */
    public static void applySlowness(LivingEntity livingTarget, double slownessLevel, int durationTicks) {
        AttributeInstance movementSpeed = livingTarget.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed == null) {
            // B2: 目标没有该属性时也移除登记，防止 Map 泄漏
            playerSlownessModifierIds.remove(livingTarget.getUniqueId());
            return;
        }

        // B1: 只移除本插件登记的减速修饰符（不再清空其他来源如装备/药水的属性修饰符）
        removeAllModifiers(movementSpeed);
        UUID playerId = livingTarget.getUniqueId();
        UUID modifierId = playerSlownessModifierIds.get(playerId);

        // 如果已经存在减速修饰符，则先移除
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
        // B3: 保存延迟任务引用，便于 cancelAll 统一取消；同时清理已结束的任务防止列表无限增长
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                removeSlowness(livingTarget);
            }
        }.runTaskLater(MagicExpansion.getInstance(), durationTicks*3);
        pendingRemoveTasks.add(task);
        pendingRemoveTasks.removeIf(BukkitTask::isCancelled);
    }

    /**
     * 移除减速效果
     *
     * @param livingTarget 目标生物
     */
    public static void removeSlowness(LivingEntity livingTarget) {
        AttributeInstance movementSpeed = livingTarget.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        UUID playerId = livingTarget.getUniqueId();
        if (movementSpeed == null) {
            // B2: 目标没有该属性时也移除登记，防止 Map 泄漏
            playerSlownessModifierIds.remove(playerId);
            return;
        }

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
     * B1: 移除目标属性实例上由本插件添加的减速修饰符。
     * 原实现会移除所有修饰符（误伤装备/药水/其他插件加成），
     * 现改为遍历修饰符逐个比对名称，仅移除本插件登记的 "SlownessEffect"。
     *
     * @param attributeInstance 属性实例
     */
    private static void removeAllModifiers(AttributeInstance attributeInstance) {
        Collection<AttributeModifier> modifiers = attributeInstance.getModifiers();
        for (AttributeModifier modifier : modifiers) {
            // 仅移除本插件登记的减速修饰符（与 applySlowness 写入的名称一致）
            if ("SlownessEffect".equals(modifier.getName())) {
                attributeInstance.removeModifier(modifier);
            }
        }
    }

    /**
     * B3: 取消所有待执行的延迟移除任务（插件卸载/重载时调用，防止任务悬挂）
     */
    public static void cancelAll() {
        for (BukkitTask task : pendingRemoveTasks) {
            task.cancel(); // 逐个取消延迟任务
        }
        pendingRemoveTasks.clear(); // 清空任务列表
    }

    /**
     * B4: 玩家退出时的会话数据清理（由 PlayerCleanupListener 统一调用）：
     * 移除该玩家的减速修饰符登记，防止 Map 残留离线玩家数据造成内存泄漏。
     * （实体身上的 modifier 随实体下线自动失效，此处只需清理登记表）
     */
    public static void cleanup(UUID uuid) {
        playerSlownessModifierIds.remove(uuid);
    }

}
