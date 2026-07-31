package io.Yomicer.magicExpansion.items.summonBossItem;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.Yomicer.magicExpansion.items.summonBossItem.bossSkill.FireZombieSkill.*;
import static org.bukkit.inventory.EquipmentSlot.HAND;

public class WindElf extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public WindElf(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            // 阻止默认行为(放置方块或使用物品)
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            Player player = e.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            // 检查玩家手上是否有物品
            if (e.getHand()!= HAND) {
                player.sendMessage(ColorGradient.getGradientName("Hold this item in your main hand to use it."));
                return;
            }

            // 减少手上的物品数量
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null); // 如果数量为 1,则直接移除
            }

            spawnWindZombie(e);

        };
    }


    private void spawnWindZombie(PlayerRightClickEvent e) {

        Player player = e.getPlayer();
        // 获取玩家位置并生成怪物
        Location location = player.getLocation();
        LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ALLAY);

        // 设置怪物名称
        String zombieName = "§3§lWind Spirit";
        mob.setCustomName(zombieName);
        mob.setCustomNameVisible(true);
        // 设置自定义元数据:用于标识这是烈火僵尸
        mob.setMetadata("magicMobType", new FixedMetadataValue(MagicExpansion.getInstance(), "WindElf"));
        mob.setMetadata("isInvincibleWindElf", new FixedMetadataValue(MagicExpansion.getInstance(), false)); // 设置无敌元数据
        // 调整最大生命值并设置初始血量
        double maxHealth = 200.0; // 自定义最大生命值
        mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        mob.setHealth(maxHealth); // 设置初始血量为最大生命值


        // 添加雷击效果
        worldStrikeLightningEffect(mob.getLocation());



        // 定义技能列表
        Runnable[] skills = {
                () -> magicAttackSkill(mob, zombieName),
                () -> redstoneParticleAttackSkillWindElf(mob),
                () -> twoWindParticleAttackSkill(mob),
        };

        // 每隔4-6秒随机释放一个技能
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mob.isDead()) {
                    this.cancel(); // 如果怪物死亡,停止任务
                    return;
                }
                // 修改名称,添加"Untargetable ·"前缀
                mob.setMetadata("isInvincibleWindElf", new FixedMetadataValue(MagicExpansion.getInstance(), true));
                mob.setCustomName(zombieName + "§e§l[Invulnerable]");
                mob.setCustomNameVisible(true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // 恢复原始名称
                        mob.setMetadata("isInvincibleWindElf", new FixedMetadataValue(MagicExpansion.getInstance(), false));
                        mob.setCustomName(zombieName+ "§e§d[Weakened]");
                        mob.setCustomNameVisible(true);
                    }
                }.runTaskLater(MagicExpansion.getInstance(), 40L+ new Random().nextInt(20)); // 2~3秒后恢复

                // 随机选择一个技能释放
                int randomIndex = new Random().nextInt(skills.length);
                skills[randomIndex].run();
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 80L + new Random().nextInt(41)); // 每4-6秒执行一次

        // 添加:每隔4秒进行一次传送
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mob.isDead()) {
                    this.cancel(); // 如果怪物死亡,停止任务
                    return;
                }

                // 获取附近玩家
                List<Player> nearbyPlayers = getNearbyPlayers(mob);

                // 如果没有玩家在范围内,取消本次传送
                if (nearbyPlayers.isEmpty()) {
                    return;
                }

                // 随机选择一个附近的玩家
                Player nearestPlayer = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));

                // 获取玩家的位置并计算随机传送位置
                Location playerLocation = nearestPlayer.getLocation();
                Random random = new Random();

                double x = playerLocation.getX() + (random.nextDouble() * 18 - 9); // 半径5范围内的随机X坐标
                double y = playerLocation.getY() + random.nextDouble() * 4;       // Y坐标+0到+4
                double z = playerLocation.getZ() + (random.nextDouble() * 18 - 9); // 半径5范围内的随机Z坐标

                Location newLocation = new Location(mob.getWorld(), x, y, z);

                // 确保新位置是安全的(避免卡在方块中)
                while (!newLocation.getBlock().getType().isAir() || !newLocation.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                    y += 1; // 向上移动直到找到空旷位置
                    newLocation.setY(y);
                }

                // 传送怪物到新位置
                mob.teleport(newLocation);
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 80L); // 每4秒执行一次




    }



    // 生成雷击效果
    public void worldStrikeLightningEffect(Location location) {
        location.getWorld().strikeLightningEffect(location); // 只有视觉效果,不会造成伤害
    }

    private static List<Player> getNearbyPlayers(LivingEntity mob) {
        return mob.getWorld().getNearbyEntities(mob.getLocation(), 15, 8, 15).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

}
