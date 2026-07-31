package io.Yomicer.magicExpansion.Listener.bossListener;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BasicBossDropListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // 获取怪物的类型标识(从元数据中读取)
        String mobType = "";
        if (entity.hasMetadata("magicMobType")) {
            mobType = entity.getMetadata("magicMobType").get(0).asString();
        } else {
            return; // 如果没有 mobType 元数据,直接返回
        }

        // 清除默认掉落物
//        event.getDrops().clear();

        // 根据怪物类型执行不同的掉落逻辑
        switch (mobType) {
            case "FireZombie":
                dropFireZombieLoot(entity);
                break;
            case "WindElf":
                dropWindElfLoot(entity);
                break;
            default:
//                Debug.logWarn("Unknown mob type: " + mobType);
                break;
        }
    }

    /**
     * 烈火僵尸死亡时的掉落逻辑
     *
     * @param entity 死亡的实体
     */
    private void dropWindElfLoot(LivingEntity entity) {
        // 清除默认掉落物(可选)
        entity.getWorld().getEntitiesByClass(Item.class).forEach(Entity::remove);

        // 获取死亡位置
        Location dropLocation = entity.getLocation();

        if(new Random().nextInt(100000)>91888){
            return;
        }
        ItemStack DropItem = MagicExpansionItems.WIND_SPIRIT;
        ItemStack DropItem2 = SlimefunItems.ADVANCED_CIRCUIT_BOARD;

        // 掉落物品到世界中
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem);
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem2);
        // 示例:掉落一些经验值
        entity.getWorld().spawnEntity(dropLocation, EntityType.EXPERIENCE_ORB);


    }


    /**
     * 烈火僵尸死亡时的掉落逻辑
     *
     * @param entity 死亡的实体
     */
    private void dropFireZombieLoot(LivingEntity entity) {
        // 清除默认掉落物(可选)
        entity.getWorld().getEntitiesByClass(Item.class).forEach(Entity::remove);

        // 获取死亡位置
        Location dropLocation = entity.getLocation();

        if(new Random().nextInt(100000)>91888){
            return;
        }
        ItemStack DropItem = MagicExpansionItems.BASIC_ENCHANT_STONE;
        ItemStack DropItem2 = SlimefunItems.BASIC_CIRCUIT_BOARD;

        // 掉落物品到世界中
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem);
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem2);

        // 示例:掉落一些经验值
        entity.getWorld().spawnEntity(dropLocation, EntityType.EXPERIENCE_ORB);


    }



}
