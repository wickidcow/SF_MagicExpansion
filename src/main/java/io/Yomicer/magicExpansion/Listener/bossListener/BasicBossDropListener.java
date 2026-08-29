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

    // F4: 共享静态随机数实例，避免每次掉落都新建 Random
    private static final Random RANDOM = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // 获取怪物的类型标识（从元数据中读取）
        String mobType = "";
        if (entity.hasMetadata("magicMobType")) {
            mobType = entity.getMetadata("magicMobType").get(0).asString();
        } else {
            return; // 如果没有 mobType 元数据，直接返回
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
//                Debug.logWarn("未知的怪物类型: " + mobType);
                break;
        }
    }

    /**
     * 烈火僵尸死亡时的掉落逻辑
     *
     * @param entity 死亡的实体
     */
    private void dropWindElfLoot(LivingEntity entity) {
        // F1: 原先 getEntitiesByClass(Item.class).forEach(Entity::remove) 会清除全世界的掉落物（严重误伤），
        // 改为仅清理死亡点 3 格范围内可能干扰本 Boss 掉落的掉落物
        entity.getWorld().getNearbyEntitiesByType(Item.class, entity.getLocation(), 3).forEach(Entity::remove);

        // 获取死亡位置
        Location dropLocation = entity.getLocation();

        // F3: 仅当击杀者为玩家时才判定稀有掉落，环境死亡不掉
        // F4: 改用共享随机数实例
        if (entity.getKiller() == null || RANDOM.nextInt(100000) > 91888) {
            return;
        }
        // F2: 掉落前 clone 常量物品，避免修改/损耗共享的 SlimefunItemStack 实例
        ItemStack DropItem = MagicExpansionItems.WIND_SPIRIT.clone();
        ItemStack DropItem2 = SlimefunItems.ADVANCED_CIRCUIT_BOARD.clone();

        // 掉落物品到世界中
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem);
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem2);
        // 示例：掉落一些经验值
        entity.getWorld().spawnEntity(dropLocation, EntityType.EXPERIENCE_ORB);


    }


    /**
     * 烈火僵尸死亡时的掉落逻辑
     *
     * @param entity 死亡的实体
     */
    private void dropFireZombieLoot(LivingEntity entity) {
        // F1: 原先 getEntitiesByClass(Item.class).forEach(Entity::remove) 会清除全世界的掉落物（严重误伤），
        // 改为仅清理死亡点 3 格范围内可能干扰本 Boss 掉落的掉落物
        entity.getWorld().getNearbyEntitiesByType(Item.class, entity.getLocation(), 3).forEach(Entity::remove);

        // 获取死亡位置
        Location dropLocation = entity.getLocation();

        // F3: 仅当击杀者为玩家时才判定稀有掉落，环境死亡不掉
        // F4: 改用共享随机数实例
        if (entity.getKiller() == null || RANDOM.nextInt(100000) > 91888) {
            return;
        }
        // F2: 掉落前 clone 常量物品，避免修改/损耗共享的 SlimefunItemStack 实例
        ItemStack DropItem = MagicExpansionItems.BASIC_ENCHANT_STONE.clone();
        ItemStack DropItem2 = SlimefunItems.BASIC_CIRCUIT_BOARD.clone();

        // 掉落物品到世界中
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem);
        dropLocation.getWorld().dropItemNaturally(dropLocation, DropItem2);

        // 示例：掉落一些经验值
        entity.getWorld().spawnEntity(dropLocation, EntityType.EXPERIENCE_ORB);


    }



}
