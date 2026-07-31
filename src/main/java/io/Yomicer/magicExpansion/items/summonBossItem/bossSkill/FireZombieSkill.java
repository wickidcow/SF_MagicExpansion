package io.Yomicer.magicExpansion.items.summonBossItem.bossSkill;

import io.Yomicer.magicExpansion.utils.log.Debug;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FireZombieSkill {



    // 生成粒子特效
    private static void spawnParticleEffects(Location location,Player player) {
        // 火焰粒子
        location.getWorld().spawnParticle(Particle.FLAME, location, 1000, 10, 8, 10, 0.1);
//        player.sendMessage("FLAME triggered");

        // 烟雾粒子
        location.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 1000, 10, 8, 10, 0.1);
//        player.sendMessage("CAMPFIRE_COSY_SMOKE triggered");
        // 魔法粒子
        // 定义粒子的颜色(青色)
//        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.0f);
        location.getWorld().spawnParticle(Particle.ENCHANT, location, 1000, 10, 8, 10, 0.1);
//        player.sendMessage("ENCHANT triggered");
    }

    // 生成粒子特效
    private static void spawnOneParticle(Location location,Player player, Particle particle, int num) {
        // 火焰粒子
        location.getWorld().spawnParticle(particle, location, num, 10, 8, 10, 0.1);
//        player.sendMessage("FLAME triggered");
    }
    private static void spawnOneParticle(Location location,Player player, Particle particle, int num, double x, double y, double z) {
        // 火焰粒子
        location.getWorld().spawnParticle(particle, location, num, x, y, z, 0.1);
//        player.sendMessage("FLAME triggered");
    }

    private static void spawnOneParticle(Location location, Player player, Particle particle, Particle.DustOptions extraInfo){
        location.getWorld().spawnParticle(particle, location, 1000, 10, 8, 10, 0.1,extraInfo);
    }


    /**
     * 获取附近的玩家
     */
    private static List<Player> getNearbyPlayers(LivingEntity mob, double x,double y) {
        return mob.getWorld().getNearbyEntities(mob.getLocation(), x, y, x).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

    /**
     * 技能:魔法攻击
     */
    public static void magicAttackSkill(LivingEntity mob, String bossName) {
        for (Player nearbyPlayer : getNearbyPlayers(mob,10,5)) {
            nearbyPlayer.damage(10, mob); // 造成5点伤害
            nearbyPlayer.sendMessage(bossName+" launched a magical attack against you!");
            spawnOneParticle(nearbyPlayer.getLocation(), nearbyPlayer, Particle.BUBBLE,2500);
        }
    }

    /**
     * 技能:红石粒子攻击 僵尸大招,精神攻击
     */
    public static void redstoneParticleAttackSkill(LivingEntity mob) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.0f);
        for (Player nearbyPlayer : getNearbyPlayers(mob,10,5)) {
            nearbyPlayer.damage(8, mob); // 造成5点伤害
            nearbyPlayer.sendMessage("§b§lThe Flame Zombie has cursed you!");
            spawnOneParticle(nearbyPlayer.getLocation(), nearbyPlayer, Particle.DUST, dustOptions);
            // 添加负面效果(持续2秒,等级10)
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 10)); // 攻击缓慢
            nearbyPlayer.sendMessage(ChatColor.RED + "§lYour arms become unbearably heavy," + ChatColor.YELLOW + "§land your attack speed drops sharply!");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 10));         // 移动缓慢
            nearbyPlayer.sendMessage(ChatColor.DARK_BLUE + "§lYour legs feel weighted with lead," + ChatColor.GOLD + "§land your movement slows to a crawl!");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 10));    // 致盲
            nearbyPlayer.sendMessage(ChatColor.BLACK + "§lDarkness covers your vision," + ChatColor.LIGHT_PURPLE + "§land you can barely see!");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 180, 10));    // 反胃
            nearbyPlayer.sendMessage(ChatColor.GREEN + "§lA violent wave of dizziness strikes," + ChatColor.DARK_GREEN + "§land the world begins to spin!");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 10));       // 饥饿
            nearbyPlayer.sendMessage(ChatColor.DARK_RED + "§lYour stomach twists in pain," + ChatColor.GRAY + "§land hunger drains your strength!");
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 10));     // 失明(1.19+版本支持)
            nearbyPlayer.sendMessage(ChatColor.DARK_GRAY + "§lDeep darkness consumes your soul," + ChatColor.WHITE + "§land you fall into an endless void!");
        }
    }
    /**
     * 技能:絮风
     */
    public static void redstoneParticleAttackSkillWindElf(LivingEntity mob) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 105, 180), 5.0f);
        for (Player nearbyPlayer : getNearbyPlayers(mob,15,8)) {
            nearbyPlayer.damage(21, mob); // 造成21点伤害
            nearbyPlayer.sendMessage("§3§lThe Wind Spirit has struck you!");
            spawnOneParticle(nearbyPlayer.getLocation(), nearbyPlayer, Particle.DUST, dustOptions);
            // 1. 失衡:跳跃提升(反向效果)→ 模拟"Powerful winds lift you off the ground!"
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 5)); // 被气流托起,漂浮
            nearbyPlayer.sendMessage(ChatColor.AQUA + "§l↑↑↑ A violent current hurls you into the air!" + ChatColor.WHITE + " §lYou float upward uncontrollably...");

            // 2. 减速:缓慢 + 缓慢挖掘(双重迟滞)
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 3));
            nearbyPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "§l🌀 The wind forms shackles around you," + ChatColor.GRAY + "§land your movements become slow and weak...");

            // 3. 视觉干扰:反胃(模拟眩晕 + 视野扭曲)
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 250, 3));
            nearbyPlayer.sendMessage(ChatColor.DARK_AQUA + "§l🌪️ The world spins wildly before your eyes," + ChatColor.YELLOW + "§land you can no longer tell sky from ground...");

            // 4. 呼吸困难:饥饿效果(象征体力流失)
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80, 3));
            nearbyPlayer.sendMessage(ChatColor.DARK_RED + "§l💨 Powerful winds steal your breath," + ChatColor.GOLD + "§land your strength fades with every gasp...");

            // 5. 风暴之眼: blindness(短暂致盲,模拟沙尘迷眼)
            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 100));
            nearbyPlayer.sendMessage(ChatColor.BLACK + "§l🌫️ Pale windblown sand strikes your face," + ChatColor.WHITE + "§land your eyes can barely see...");

            // 可选:1.19+ 支持 DARKNESS(更契合"The storm obscures the light!")
            if (Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20") || Bukkit.getVersion().contains("1.21")) {
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 100));
                nearbyPlayer.sendMessage(ChatColor.DARK_GRAY + "🌑 The Wind Spirit's fury blocks out the light," + ChatColor.GRAY + "§land darkness corrodes even your soul...");
            }
        }
    }

    /**
     * 技能:火焰攻击
     */
    public static void fireParticleAttackSkill(LivingEntity mob) {

        // 获取附近的玩家
        List<Player> nearbyPlayers = getNearbyPlayers(mob,10,5);

        // 如果没有玩家在范围内,直接返回
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        // 随机选择一个目标玩家
        Player targetPlayer = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));

        // 对目标玩家造成伤害
        targetPlayer.damage(88, mob); // 造成88点伤害

        // 发送提示信息给目标玩家
        targetPlayer.sendMessage("§c§lFlame Zombieyou!");
        targetPlayer.sendMessage("§e§lScorching flames consume your soul...");
        targetPlayer.sendMessage("§c§lYou took " +"§e§l88 damage§c§l" +"!");

        // 添加链接线效果
        spawnLinkEffect(mob.getLocation(), targetPlayer.getLocation(),Color.RED);
        // 在目标玩家位置生成火焰粒子效果
        spawnOneParticle(targetPlayer.getLocation(), targetPlayer, Particle.FLAME, 200, 1, 1, 1);

        // 广播消息给范围内的其他玩家(非目标玩家)
        for (Player nearbyPlayer : nearbyPlayers) {
            if (!nearbyPlayer.equals(targetPlayer)) {
                nearbyPlayer.sendMessage("§e§lFlame Zombie!");
                nearbyPlayer.sendMessage("§e§lIt locked onto §b"+ targetPlayer.getName() + "§e§l!");
            }
        }
    }
    /**
     * 技能:双子风攻击
     */
    public static void twoWindParticleAttackSkill(LivingEntity mob) {

        // 获取附近的玩家
        List<Player> nearbyPlayers = getNearbyPlayers(mob,15,8);

        // 如果没有玩家在范围内,直接返回
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        // 随机选择一个目标玩家
        Player targetPlayer = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));
        Player targetPlayer2 = nearbyPlayers.get(new Random().nextInt(nearbyPlayers.size()));

        // 对目标玩家造成伤害
        targetPlayer.damage(68, mob); // 造成78点伤害
        targetPlayer2.damage(136, mob); // 造成78点伤害

        // 发送提示信息给目标玩家
        targetPlayer.sendMessage("§b§lCrosswind has locked onto you!");
        targetPlayer.sendMessage("§e§lA raging wind tears at your defenses...");
        targetPlayer.sendMessage("§b§lYou suffered " + "§e§l68 damage§b§l" + "!");

        targetPlayer2.sendMessage("§b§lHexwind has locked onto you!");
        targetPlayer2.sendMessage("§e§lA raging wind cuts through your armor and tears at you...");
        targetPlayer2.sendMessage("§b§lYou suffered " + "§e§l136 damage§b§l" + "!");

        // 添加链接线效果
        spawnLinkEffect(mob.getLocation(), targetPlayer.getLocation(), Color.AQUA);
        spawnLinkEffect(mob.getLocation(), targetPlayer2.getLocation(), Color.AQUA);
        // 在目标玩家位置生成火焰粒子效果
        spawnOneParticle(targetPlayer.getLocation(), targetPlayer, Particle.SPLASH, 300, 1, 1, 1);
        spawnOneParticle(targetPlayer2.getLocation(), targetPlayer, Particle.SPLASH, 300, 1, 1, 1);

        // 广播消息给范围内的其他玩家(非目标玩家)
        for (Player nearbyPlayer : nearbyPlayers) {
            if (!nearbyPlayer.equals(targetPlayer)&&!nearbyPlayer.equals(targetPlayer2)) {
                nearbyPlayer.sendMessage("§e§lWind Spirit!");
                nearbyPlayer.sendMessage("§e§lCrosswind locked onto §b"+ targetPlayer.getName() + "§e§l!");
                nearbyPlayer.sendMessage("§e§lHexwind locked onto §b"+ targetPlayer2.getName() + "§e§l!");
            }
        }
    }



    /**
     * 在两个位置之间生成链接线效果
     *
     * @param start 起始位置(例如烈火僵尸的位置)
     * @param end   结束位置(例如目标玩家的位置)
     */
    public static void spawnLinkEffect(Location start, Location end,Color color) {
        // 获取起始和结束位置的坐标
        double startX = start.getX();
        double startY = start.getY();
        double startZ = start.getZ();

        double endX = end.getX();
        double endY = end.getY();
        double endZ = end.getZ();

        // 计算两点之间的距离
        double distance = start.distance(end);

        // 设置粒子的数量和间隔
        int particleCount = (int) (distance * 5); // 每单位距离生成 5 个粒子
        double deltaX = (endX - startX) / particleCount;
        double deltaY = (endY - startY) / particleCount;
        double deltaZ = (endZ - startZ) / particleCount;

        // 在两点之间生成粒子
        for (int i = 0; i < particleCount; i++) {
            double x = startX + deltaX * i;
            double y = startY + deltaY * i;
            double z = startZ + deltaZ * i;

            // 创建粒子位置
            Location particleLocation = new Location(start.getWorld(), x, y, z);

            // 生成粒子效果(例如红线或能量线)
            start.getWorld().spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(color, 1.0f));
        }
    }




}
