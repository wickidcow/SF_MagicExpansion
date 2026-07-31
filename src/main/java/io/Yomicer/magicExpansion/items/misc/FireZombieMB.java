package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.Yomicer.magicExpansion.items.summonBossItem.bossSkill.FireZombieSkill.*;
import static io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils.isItemSimilar;

public class FireZombieMB extends MultiBlockMachine {

    public FireZombieMB(ItemGroup itemGroup, SlimefunItemStack item) {
        super(itemGroup, item, new ItemStack[] {null, MagicExpansionItems.FIREZOMBIE_HEAD, null, null, MagicExpansionItems.FIREZOMBIE_BODY, null, null, MagicExpansionItems.FIREZOMBIE_BODY, null}, BlockFace.SELF);
    }

    @Override
    public void onInteract(@Nonnull Player p, @Nonnull Block b) {
        Location location = b.getLocation().clone();
        Location locationUp = b.getRelative(BlockFace.UP).getLocation().clone();
        Location locationDown = b.getRelative(BlockFace.DOWN).getLocation().clone();
        Block pumpkinHead = b.getRelative(BlockFace.UP);
        Block bottomBlackstone = b.getRelative(BlockFace.DOWN);

        if(!StorageCacheUtils.isBlock(locationUp, "MAGIC_EXPANSION_FIREZOMBIE_HEAD") || !StorageCacheUtils.isBlock(location, "MAGIC_EXPANSION_FIREZOMBIE_BODY") || !StorageCacheUtils.isBlock(locationDown, "MAGIC_EXPANSION_FIREZOMBIE_BODY")) {

            p.sendMessage(ColorGradient.getGradientName("[ BOSS]you requires"));
            p.sendMessage(ColorGradient.getGradientName("[Boss] A powerful enemy is approaching!"));
            p.sendMessage(ColorGradient.getGradientName("[Boss] Prepare for battle."));
            return;
        }

//        p.getWorld().spawnEntity(b.getLocation().add(0.5, -1, 0.5), EntityType.WITHER_SKELETON);

        pumpkinHead.setType(Material.AIR);
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation().clone().add(0, 1, 0));
        b.setType(Material.AIR);
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation().clone());
        bottomBlackstone.setType(Material.AIR);
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation().clone().add(0, -1, 0));
        spawnFireZombie(b.getLocation().clone());
    }



    private void spawnFireZombie(Location location) {

        // 获取玩家位置并生成怪物
        LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        // 设置怪物名称
        String zombieName = "§c§lFlame Zombie";
        mob.setCustomName(zombieName);
        mob.setCustomNameVisible(true);
        // 设置自定义元数据:用于标识这是烈火僵尸
        mob.setMetadata("magicMobType", new FixedMetadataValue(MagicExpansion.getInstance(), "FireZombie"));
        mob.setMetadata("isInvincibleFireZombie", new FixedMetadataValue(MagicExpansion.getInstance(), false)); // 设置无敌元数据
        // 调整最大生命值并设置初始血量
        double maxHealth = 200.0; // 自定义最大生命值
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        mob.setHealth(maxHealth); // 设置初始血量为最大生命值


        // 添加雷击效果
        worldStrikeLightningEffect(mob.getLocation());



        // 定义技能列表
        Runnable[] skills = {
                () -> magicAttackSkill(mob, zombieName),
                () -> redstoneParticleAttackSkill(mob),
                () -> fireParticleAttackSkill(mob),
        };

        // 每隔4-8秒随机释放一个技能
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mob.isDead()) {
                    this.cancel(); // 如果怪物死亡,停止任务
                    return;
                }
                // 修改名称,添加"无法选中·"前缀
                mob.setMetadata("isInvincibleFireZombie", new FixedMetadataValue(MagicExpansion.getInstance(), true));
                mob.setCustomName(zombieName + "§e§l[Unstoppable]");
                mob.setCustomNameVisible(true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // 恢复原始名称
                        mob.setMetadata("isInvincibleFireZombie", new FixedMetadataValue(MagicExpansion.getInstance(), false));
                        mob.setCustomName(zombieName);
                        mob.setCustomNameVisible(true);
                    }
                }.runTaskLater(MagicExpansion.getInstance(), 50L); // 2.5秒后恢复

                // 随机选择一个技能释放
                int randomIndex = new Random().nextInt(skills.length);
                skills[randomIndex].run();
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 80L + new Random().nextInt(81)); // 每4-8秒执行一次

        // 添加:每隔11秒进行一次传送
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
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 220L); // 每11秒执行一次




    }

    // 生成雷击效果
    public void worldStrikeLightningEffect(Location location) {
        location.getWorld().strikeLightningEffect(location); // 只有视觉效果,不会造成伤害
    }

    private static List<Player> getNearbyPlayers(LivingEntity mob) {
        return mob.getWorld().getNearbyEntities(mob.getLocation(), 10, 8, 10).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

}
