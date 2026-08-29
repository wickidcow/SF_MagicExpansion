package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
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
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.Yomicer.magicExpansion.items.summonBossItem.bossSkill.FireZombieSkill.*;

public class WindElfMB extends MultiBlockMachine {

    // 修复(J3)：召唤冷却表（key=玩家UUID, value=上次召唤时间戳），60 秒冷却
    private static final Map<UUID, Long> SPAWN_COOLDOWNS = new ConcurrentHashMap<>();
    // 修复(J1/J2)：Boss 活动任务引用表（key=Boss实体UUID），死亡/移除时统一取消，防止任务泄漏
    private static final Map<UUID, List<BukkitTask>> BOSS_TASKS = new ConcurrentHashMap<>();

    private static final long SPAWN_COOLDOWN_MS = 60_000L;          // 修复(J3)：召唤冷却 60 秒
    private static final long MAX_ALIVE_TIME_MS = 5 * 60 * 1000L;   // 修复(J1)：Boss 最长存在 5 分钟
    private static final int MAX_NEARBY_SAME_BOSS = 3;              // 修复(J3)：同区域并存上限

    public WindElfMB(ItemGroup itemGroup, SlimefunItemStack item) {
        super(itemGroup, item, new ItemStack[] {null, MagicExpansionItems.WIND_ELF_HEAD, null, null, MagicExpansionItems.WIND_ELF_BODY, null, null, MagicExpansionItems.WIND_ELF_BODY, null}, BlockFace.SELF);
    }

    @Override
    public void onInteract(@Nonnull Player p, @Nonnull Block b) {
        Location location = b.getLocation().clone();
        Location locationUp = b.getRelative(BlockFace.UP).getLocation().clone();
        Location locationDown = b.getRelative(BlockFace.DOWN).getLocation().clone();
        Block pumpkinHead = b.getRelative(BlockFace.UP);
        Block bottomBlackstone = b.getRelative(BlockFace.DOWN);

        if(!StorageCacheUtils.isBlock(locationUp, "MAGIC_EXPANSION_WIND_ELF_HEAD") || !StorageCacheUtils.isBlock(location, "MAGIC_EXPANSION_WIND_ELF_BODY") || !StorageCacheUtils.isBlock(locationDown, "MAGIC_EXPANSION_WIND_ELF_BODY")) {

            p.sendMessage(ColorGradient.getGradientName("[魔法·BOSS召唤]你需要使用正确的搭建方式"));
            p.sendMessage(ColorGradient.getGradientName("[魔法·BOSS召唤]请检查你放置的方块"));
            p.sendMessage(ColorGradient.getGradientName("[魔法·BOSS召唤]他可能是一个粘液物品"));
            return;
        }

        // 修复(J3)：召唤冷却校验（60 秒），放在拆除搭建方块之前
        long now = System.currentTimeMillis();
        Long lastSpawn = SPAWN_COOLDOWNS.get(p.getUniqueId());
        if (lastSpawn != null && now - lastSpawn < SPAWN_COOLDOWN_MS) {
            long remain = (SPAWN_COOLDOWN_MS - (now - lastSpawn)) / 1000 + 1;
            p.sendMessage(ColorGradient.getGradientName("[魔法·BOSS召唤]BOSS召唤冷却中，还需等待 " + remain + " 秒！"));
            return;
        }

        // 修复(J3)：同区域并存上限 —— 20 格内同类 Boss 数量 >= 3 时拒绝召唤
        if (countNearbyBosses(location, "WindElf") >= MAX_NEARBY_SAME_BOSS) {
            p.sendMessage(ColorGradient.getGradientName("[魔法·BOSS召唤]附近同类BOSS过多（20格内已达 " + MAX_NEARBY_SAME_BOSS + " 只），无法召唤！"));
            return;
        }

        // 修复(J3)：通过校验后记录冷却时间
        SPAWN_COOLDOWNS.put(p.getUniqueId(), now);

        pumpkinHead.setType(Material.AIR);
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation().clone().add(0, 1, 0));
        b.setType(Material.AIR);
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation().clone());
        bottomBlackstone.setType(Material.AIR);
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation().clone().add(0, -1, 0));
        spawnWindZombie(b.getLocation().clone());
    }

    /**
     * 修复(J3)：统计 20 格内指定类型的本插件 BOSS 数量（依据 magicMobType 元数据识别）
     */
    private static int countNearbyBosses(Location center, String mobType) {
        return (int) center.getWorld().getNearbyEntities(center, 20, 20, 20).stream()
                .filter(en -> en.hasMetadata("magicMobType")
                        && !en.getMetadata("magicMobType").isEmpty()
                        && mobType.equals(en.getMetadata("magicMobType").get(0).value()))
                .count();
    }

    /**
     * 修复(J1/J2)：取消指定 Boss 的所有任务并从任务表移除
     */
    private static void cancelBossTasks(UUID bossId) {
        List<BukkitTask> tasks = BOSS_TASKS.remove(bossId);
        if (tasks != null) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
    }

    /**
     * 修复(J1)：看门狗校验 —— Boss 已死亡 / 存活超过 5 分钟 / 60 格内无玩家 时返回 true
     */
    private static boolean watchdogExpired(LivingEntity mob, long spawnTime) {
        if (mob.isDead()) return true;
        if (System.currentTimeMillis() - spawnTime > MAX_ALIVE_TIME_MS) return true;
        // 60 格内无玩家 → 超时
        boolean hasPlayerNearby = mob.getWorld().getNearbyEntities(mob.getLocation(), 60, 60, 60).stream()
                .anyMatch(en -> en instanceof Player);
        return !hasPlayerNearby;
    }

    private void spawnWindZombie(Location location) {

        // 获取玩家位置并生成怪物
        LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ALLAY);

        // 设置怪物名称
        String zombieName = "§3§l风灵";
        mob.setCustomName(zombieName);
        mob.setCustomNameVisible(true);
        // 设置自定义元数据：用于标识这是烈火僵尸
        mob.setMetadata("magicMobType", new FixedMetadataValue(MagicExpansion.getInstance(), "WindElf"));
        mob.setMetadata("isInvincibleWindElf", new FixedMetadataValue(MagicExpansion.getInstance(), false)); // 设置无敌元数据
        // 调整最大生命值并设置初始血量
        double maxHealth = 200.0; // 自定义最大生命值
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        mob.setHealth(maxHealth); // 设置初始血量为最大生命值

        // 修复(J4)：禁止实体因远离玩家被自动清理（BOSS 由任务与看门狗管理生命周期）
        mob.setRemoveWhenFarAway(false);

        // 添加雷击效果
        worldStrikeLightningEffect(mob.getLocation());

        // 修复(J1)：记录生成时间，供看门狗判断超时
        final long spawnTime = System.currentTimeMillis();

        // 定义技能列表
        Runnable[] skills = {
                () -> magicAttackSkill(mob, zombieName),
                () -> redstoneParticleAttackSkillWindElf(mob),
                () -> twoWindParticleAttackSkill(mob),
        };

        // 修复(J2)：收集本 Boss 的所有任务引用，统一管理
        List<BukkitTask> tasks = new ArrayList<>();

        // 每隔4-6秒随机释放一个技能
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // 修复(J1)：看门狗 —— 死亡/超时(5分钟)/60格无玩家 → 移除Boss并取消全部任务
                if (watchdogExpired(mob, spawnTime)) {
                    this.cancel();
                    cancelBossTasks(mob.getUniqueId());
                    if (!mob.isDead()) {
                        mob.remove();
                    }
                    return;
                }
                // 修改名称，添加“无法选中·”前缀
                mob.setMetadata("isInvincibleWindElf", new FixedMetadataValue(MagicExpansion.getInstance(), true));
                mob.setCustomName(zombieName + "§e§l[无敌]");
                mob.setCustomNameVisible(true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // 恢复原始名称
                        mob.setMetadata("isInvincibleWindElf", new FixedMetadataValue(MagicExpansion.getInstance(), false));
                        mob.setCustomName(zombieName+ "§e§d[虚弱]");
                        mob.setCustomNameVisible(true);
                    }
                }.runTaskLater(MagicExpansion.getInstance(), 40L+ new Random().nextInt(20)); // 2.5秒后恢复

                // 随机选择一个技能释放
                int randomIndex = new Random().nextInt(skills.length);
                skills[randomIndex].run();
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 80L + new Random().nextInt(41))); // 每4-6秒执行一次

        // 添加：每隔4秒进行一次传送
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                // 修复(J1)：看门狗 —— 死亡/超时/无玩家 → 移除Boss并取消全部任务
                if (watchdogExpired(mob, spawnTime)) {
                    this.cancel();
                    cancelBossTasks(mob.getUniqueId());
                    if (!mob.isDead()) {
                        mob.remove();
                    }
                    return;
                }

                // 获取附近玩家
                List<Player> nearbyPlayers = getNearbyPlayers(mob);

                // 如果没有玩家在范围内，取消本次传送
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

                // 确保新位置是安全的（避免卡在方块中）
                while (!newLocation.getBlock().getType().isAir() || !newLocation.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                    y += 1; // 向上移动直到找到空旷位置
                    newLocation.setY(y);
                }

                // 传送怪物到新位置
                mob.teleport(newLocation);
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 80L)); // 每4秒执行一次

        // 修复(J2)：将任务引用登记到任务表，Boss 死亡/移除时统一取消
        BOSS_TASKS.put(mob.getUniqueId(), tasks);
    }



    // 生成雷击效果
    public void worldStrikeLightningEffect(Location location) {
        location.getWorld().strikeLightningEffect(location); // 只有视觉效果，不会造成伤害
    }

    private static List<Player> getNearbyPlayers(LivingEntity mob) {
        return mob.getWorld().getNearbyEntities(mob.getLocation(), 10, 8, 10).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

}
