package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static org.bukkit.inventory.EquipmentSlot.HAND;

public class BigFireworksYuanDan extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    private final Random random = new Random();
    private static final int EXPLOSION_HEIGHT = 30; // 爆炸高度
    private static final int EXPLOSION_RADIUS = 50; // 爆炸半径

    // 超级亮丽的颜色
    private final Color[] SUPER_COLORS = {
            Color.fromRGB(255, 50, 50),     // 亮红
            Color.fromRGB(255, 255, 100),   // 亮黄
            Color.fromRGB(100, 255, 100),   // 亮绿
            Color.fromRGB(100, 200, 255),   // 亮蓝
            Color.fromRGB(255, 100, 255),   // 亮紫
            Color.fromRGB(255, 150, 50),    // 亮橙
            Color.fromRGB(255, 255, 255),   // 纯白
            Color.fromRGB(100, 255, 255),   // 青色
            Color.fromRGB(255, 100, 100),   // 粉色
            Color.fromRGB(255, 255, 0)      // 纯黄
    };

    // 粒子类型
    private final Particle[] EXPLOSION_PARTICLES = {
            Particle.FIREWORKS_SPARK,
            Particle.FLAME,
            Particle.DRAGON_BREATH,
            Particle.END_ROD,
            Particle.SPELL_WITCH,
            Particle.SPELL_INSTANT,
            Particle.CLOUD,
            Particle.SPELL_MOB
    };

    public BigFireworksYuanDan(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);

            Player player = e.getPlayer();
            Location playerLoc = player.getLocation();
            World world = player.getWorld();

            // 检查是否使用主手
            if (e.getHand() != HAND) {
                player.sendMessage(ColorGradient.getGradientName("请使用主手使用~"));
                return;
            }

            // 检查手上是否有物品
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand == null || itemInHand.getAmount() == 0) {
                return;
            }

            // 减少物品数量
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            // 发射超级烟花
            launchUltimateFirework(player, playerLoc, world);
        };
    }

    private void launchUltimateFirework(Player player, Location startLoc, World world) {
        // 播放发射音效
        world.playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 3.0f, 0.5f);

        // 给玩家提示
        player.sendMessage(ColorGradient.getGradientName("☄ 飞饼发射！ ☄"));

        // 计算目标位置（正上方）
        Location targetLoc = startLoc.clone().add(0, EXPLOSION_HEIGHT, 0);

        // 创建引导粒子轨迹
        createTrail(startLoc, targetLoc, world);

        // 延迟爆炸效果
        int delayTicks = (int) (EXPLOSION_HEIGHT * 0.5); // 飞行时间

        new BukkitRunnable() {
            @Override
            public void run() {
                // 播放爆炸音效
                world.playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.3f);
                world.playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 4.0f, 0.5f);

                // 创建超级爆炸效果
                createSuperExplosionEffect(targetLoc, world, player);

                // 发送屏幕标题
                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distance(targetLoc) < 200) {
                        p.sendTitle(
                                ColorGradient.getGradientName("🎇 飞饼 🎇"),
                                ColorGradient.getGradientName("震撼全场！"),
                                10, 60, 20
                        );
                    }
                }
            }
        }.runTaskLater(MagicExpansion.getInstance(), delayTicks);
    }

    private void createTrail(Location start, Location end, World world) {
        new BukkitRunnable() {
            double progress = 0;
            Location current = start.clone();

            @Override
            public void run() {
                if (progress > 1.0) {
                    this.cancel();
                    return;
                }

                // 计算当前位置
                double x = start.getX() + (end.getX() - start.getX()) * progress;
                double y = start.getY() + (end.getY() - start.getY()) * progress;
                double z = start.getZ() + (end.getZ() - start.getZ()) * progress;

                current.setX(x);
                current.setY(y);
                current.setZ(z);

                // 创建粒子轨迹
                world.spawnParticle(Particle.FIREWORKS_SPARK, current, 10, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.FLAME, current, 5, 0.1, 0.1, 0.1, 0);

                // 播放飞行音效
                if (random.nextInt(3) == 0) {
                    world.playSound(current, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5f, 2.0f);
                }

                progress += 0.05;
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
    }

    private void createSuperExplosionEffect(Location center, World world, Player player) {
        // 第一步：核心大爆炸（白色闪光）
        createCoreExplosion(center, world);

        // 第二步：彩色爆炸环
        createColorRings(center, world);

        // 第三步：流星雨效果
        createMeteorShower(center, world);

        // 第四步：最后的余辉
        new BukkitRunnable() {
            @Override
            public void run() {
                createAfterglow(center, world);
            }
        }.runTaskLater(MagicExpansion.getInstance(), 40L);
    }

    private void createCoreExplosion(Location center, World world) {
        // 强烈的白色闪光
        for (int i = 0; i < 3; i++) {
            double radius = (i + 1) * 10;
            createExplosionSphere(center, world, radius, Particle.FLASH, 50, Color.WHITE);
        }

        // 冲击波效果
        for (int r = 1; r <= 10; r++) {
            final int radius = r * 5;
            new BukkitRunnable() {
                @Override
                public void run() {
                    createExplosionRing(center, world, radius, Particle.CLOUD, 100);
                }
            }.runTaskLater(MagicExpansion.getInstance(), r * 2L);
        }
    }

    private void createColorRings(Location center, World world) {
        // 创建多层彩色环
        for (int layer = 0; layer < 5; layer++) {
            final int currentLayer = layer;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double radius = 15 + currentLayer * 7;
                    int particleCount = 200 + currentLayer * 50;

                    // 使用不同的颜色
                    Color color = SUPER_COLORS[currentLayer % SUPER_COLORS.length];

                    // 创建彩色环
                    for (int i = 0; i < particleCount; i++) {
                        double angle = (2 * Math.PI * i) / particleCount;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location particleLoc = center.clone().add(x, 0, z);

                        // 向上喷射粒子
                        for (int h = 0; h < 3; h++) {
                            Location loc = particleLoc.clone().add(0, h * 2, 0);
                            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 3.0f);
                            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions);
                        }
                    }

                    // 同步创建向下的环
                    for (int i = 0; i < particleCount; i++) {
                        double angle = (2 * Math.PI * i) / particleCount;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location particleLoc = center.clone().add(x, 0, z);

                        // 向下喷射粒子
                        for (int h = 0; h < 3; h++) {
                            Location loc = particleLoc.clone().add(0, -h * 2, 0);
                            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 3.0f);
                            world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions);
                        }
                    }
                }
            }.runTaskLater(MagicExpansion.getInstance(), layer * 10L);
        }
    }

    private void createMeteorShower(Location center, World world) {
        // 创建流星雨效果
        for (int i = 0; i < 100; i++) {
            final int index = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    // 随机方向
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double distance = 20 + random.nextDouble() * 30;

                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    Location startLoc = center.clone().add(x, 0, z);

                    // 随机颜色
                    Color color = SUPER_COLORS[random.nextInt(SUPER_COLORS.length)];
                    Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2.0f);

                    // 创建流星轨迹
                    createMeteorTrail(startLoc, center, world, dustOptions);
                }
            }.runTaskLater(MagicExpansion.getInstance(), random.nextInt(40));
        }
    }

    private void createMeteorTrail(Location start, Location target, World world, Particle.DustOptions dustOptions) {
        new BukkitRunnable() {
            double progress = 0;
            Location current = start.clone();

            @Override
            public void run() {
                if (progress > 1.0) {
                    // 在目标位置创建小爆炸
                    world.spawnParticle(Particle.EXPLOSION_LARGE, target, 1, 0, 0, 0, 0);
                    this.cancel();
                    return;
                }

                // 计算当前位置
                double x = start.getX() + (target.getX() - start.getX()) * progress;
                double y = start.getY() + (target.getY() - start.getY()) * progress;
                double z = start.getZ() + (target.getZ() - start.getZ()) * progress;

                current.setX(x);
                current.setY(y);
                current.setZ(z);

                // 创建粒子
                world.spawnParticle(Particle.REDSTONE, current, 3, 0.1, 0.1, 0.1, 0, dustOptions);
                world.spawnParticle(Particle.FIREWORKS_SPARK, current, 2, 0, 0, 0, 0.1);

                progress += 0.1;
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
    }

    private void createAfterglow(Location center, World world) {
        // 创建余辉效果
        for (int i = 0; i < 50; i++) {
            double radius = random.nextDouble() * 40;
            double angle = random.nextDouble() * 2 * Math.PI;
            double height = random.nextDouble() * 40 - 20;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = center.clone().add(x, height, z);

            // 随机颜色（偏暖色）- 使用 fromRGB 方法
            Color color = Color.fromRGB(
                    255,
                    random.nextInt(100) + 155,
                    random.nextInt(100)
            );

            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.5f);

            // 缓慢下落的余辉粒子
            world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0, dustOptions);
        }
    }

    // 辅助方法：创建爆炸球体
    private void createExplosionSphere(Location center, World world, double radius, Particle particle, int count, Color color) {
        for (int i = 0; i < count; i++) {
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double theta = 2 * Math.PI * random.nextDouble();

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            Location particleLoc = center.clone().add(x, y, z);

            if (particle == Particle.REDSTONE && color != null) {
                Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2.0f);
                world.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0, dustOptions);
            } else if (particle == Particle.FLASH && color != null) {
                world.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0, color);
            } else {
                world.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    // 辅助方法：创建爆炸环
    private void createExplosionRing(Location center, World world, double radius, Particle particle, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = center.clone().add(x, 0, z);

            // 随机高度
            particleLoc.add(0, random.nextDouble() * 20 - 10, 0);

            world.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
        }
    }
}
