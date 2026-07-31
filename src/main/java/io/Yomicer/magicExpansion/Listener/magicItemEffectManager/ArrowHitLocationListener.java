package io.Yomicer.magicExpansion.Listener.magicItemEffectManager;

import io.Yomicer.magicExpansion.utils.GiveItem;
import io.Yomicer.magicExpansion.utils.entity.EntityEgg;
import io.Yomicer.magicExpansion.utils.compat.EntityHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

import static io.Yomicer.magicExpansion.items.enchantMachine.EnchantingTable.ATTRIBUTE_POOL;

public class ArrowHitLocationListener implements Listener {


    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        // 检查投射物是否是箭
        if (event.getEntity().getType() == EntityType.ARROW) {
            Arrow arrow = (Arrow) event.getEntity();

            // 获取射出箭的玩家(如果有的话)
            if (arrow.getShooter() instanceof Player player) {

                ItemStack item = player.getInventory().getItemInMainHand().clone();
                // 获取物品的 PDC
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer() != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();

                    // 遍历所有属性
                    for (String attribute : ATTRIBUTE_POOL.keySet()) {
                        NamespacedKey key = new NamespacedKey("magicexpansion", attribute.toLowerCase().replace(".", "_"));

                        if (pdc.has(key, PersistentDataType.INTEGER)) {
                            int value = pdc.get(key, PersistentDataType.INTEGER);
                            applyEffect(attribute, value, event, player, arrow);
                        } else if (pdc.has(key, PersistentDataType.BOOLEAN)) {
                            boolean value = pdc.get(key, PersistentDataType.BOOLEAN);
                            applyEffect(attribute, value, event, player, arrow);
                        }
                    }
                }
            }
        }
    }



    private void applyEffect(String attribute, Object value, ProjectileHitEvent event, Player player, Arrow arrow) {
        switch (attribute) {
            case "MagicExpansion.ArrowTp": // 击退效果
                if (value instanceof Boolean ArrowTp && ArrowTp) {

                    // 获取箭的落点位置
                    Location hitLocation = null;

                    // 检查箭是否击中了方块
                    if (event.getHitBlock() != null) {
                        hitLocation = event.getHitBlock().getLocation();
                    }

                    // 检查箭是否击中了实体
                    if (event.getHitEntity() != null) {
                        hitLocation = event.getHitEntity().getLocation();
                    }

                    // 如果既没有击中方块也没有击中实体,可能是射到了空气
                    if (hitLocation == null) {
                        hitLocation = arrow.getLocation(); // 获取箭的最终位置
                    }

                    // 将玩家传送到箭的落点位置
                    if (hitLocation != null) {
                        hitLocation.add(0,1,0);
                        player.teleport(hitLocation);
                        player.sendMessage("§byou to Location");
                    }




                }
                break;


        }


    }








}
