package io.Yomicer.magicExpansion.utils.entity;

import io.Yomicer.magicExpansion.utils.log.Debug;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class EntityEgg {


    // 判断某种生物是否有刷怪蛋
    public static Material  hasSpawnEgg(EntityType entityType) {
        try {
            // 将生物类型名称转换为大写,并拼接 "_SPAWN_EGG"
            String spawnEggName = entityType.name().toUpperCase() + "_SPAWN_EGG";

            // 检查是否存在对应的 Material
            return Material.getMaterial(spawnEggName);
        } catch (IllegalArgumentException e) {
            // 如果 Material 不存在,会抛出异常
            return null;
        }
    }


}
