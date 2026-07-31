package io.Yomicer.magicExpansion.specialActions;

import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.compat.EntityHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.Random;
import org.bukkit.Location;

public class EntitySpawner {


    private final Random random = new Random();

    /**
     * 在玩家附近随机生成一个生物.
     *
     * @param player 玩家对象
     */
    public void spawnRandomEntityNearPlayer(Player player) {
        // 获取玩家位置
        Location playerLocation = player.getLocation();

        // 计算随机偏移(3 格范围内,包括斜向)
        double x = random.nextDouble(-5, 5); // 随机 X 偏移
        double y = random.nextDouble(0, 1); // 随机 Y 偏移(避免过高或过低)
        double z = random.nextDouble(-5, 5); // 随机 Z 偏移

        // 创建目标位置
        Location spawnLocation = playerLocation.clone().add(x, y, z);

        // 随机选择一个生物类型
        EntityType[] entityTypes = EntityType.values();
        EntityType randomEntityType = entityTypes[random.nextInt(entityTypes.length)];

        // 确保选择的是可生成的实体
        if (randomEntityType.isSpawnable()) {
            Entity spawnedEntity = player.getWorld().spawnEntity(spawnLocation, randomEntityType);
            // 检查生成的实体是否是生物(LivingEntity)
            if (spawnedEntity instanceof org.bukkit.entity.LivingEntity) {
                // 设置自定义名称
                String entityName = "Magical Entity: " + EntityHelper.getName(spawnedEntity); // 使用随机实体类型的名字作为基础
                spawnedEntity.setCustomName(ColorGradient.getGradientName(entityName)); // 应用渐变颜色
                spawnedEntity.setCustomNameVisible(true); // 让名字在游戏中可见
            }
            player.sendMessage(ColorGradient.getGradientName("Summoned magical entity: " + randomEntityType.name()));
        } else {
            player.sendMessage(ColorGradient.getGradientName("Failed to summon an entity."));
        }
    }

}
