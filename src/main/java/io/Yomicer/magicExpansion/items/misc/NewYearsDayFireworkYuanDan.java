package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.inventory.EquipmentSlot.HAND;

public class NewYearsDayFireworkYuanDan extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    private final Random random = new Random();
    private static final int EXPLOSION_HEIGHT = 130; // 爆炸高度
    private static final int EXPLOSION_RADIUS = 80; // 爆炸半径

    private final Color[] NEW_YEAR_COLORS = {
            Color.RED, Color.WHITE, Color.YELLOW, Color.AQUA,
            Color.LIME, Color.FUCHSIA, Color.ORANGE,
            Color.fromRGB(255, 20, 147), // 深粉色
            Color.fromRGB(0, 255, 255),   // 青色
            Color.fromRGB(255, 215, 0),   // 金色
            Color.fromRGB(50, 205, 50),   // 酸橙绿
            Color.fromRGB(186, 85, 211)   // 中紫色
    };

    public NewYearsDayFireworkYuanDan(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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

            if (!e.getClickedBlock().isEmpty()){
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

                Location blockLoc = e.getClickedBlock().get().getLocation().clone().add(0,1,0);



                // 播放开始音效
                world.playSound(playerLoc, Sound.ITEM_TOTEM_USE, 2.0f, 0.8f);
                world.playSound(playerLoc, Sound.BLOCK_BELL_USE, 1.5f, 0.9f);

                // 创建地面标记
                createGroundMark(playerLoc);

                // 创建中央上升光柱
                createCenterBeacon(playerLoc);

                for (int i = 0; i < 5; i++) {
                    final int currentIteration = i;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // 可选:根据迭代次数稍微偏移位置
                            Location launchLoc = blockLoc.clone();
                            if (currentIteration > 0) {
                                double angle = currentIteration * (2 * Math.PI / 5); // 均匀分布在圆周上
                                double x = Math.cos(angle) * 2;
                                double z = Math.sin(angle) * 2;
                                launchLoc.add(x, 2, z);
                            } else {
                                launchLoc.add(0, 2, 0);
                            }

                            launchMassFireworks(launchLoc);
                        }
                    }.runTaskLater(MagicExpansion.getInstance(), 40L + (i * 40L)); // 每次间隔40tick
                }
            }
        };
    }

    private void createGroundMark(Location location) {
        World world = location.getWorld();

        // 创建五层同心圆标记
        for (int layer = 1; layer <= 5; layer++) {
            final int currentLayer = layer;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double radius = currentLayer * 2.0;

                    for (int i = 0; i < 72; i++) {
                        double angle = i * 5;
                        double radians = Math.toRadians(angle);

                        double x = radius * Math.cos(radians);
                        double z = radius * Math.sin(radians);

                        Location markLoc = location.clone().add(x, 0.2, z);

                        Color color = getRainbowColor(currentLayer / 5.0);
                        world.spawnParticle(Particle.REDSTONE, markLoc, 1,
                                new Particle.DustOptions(color, 2.0f));
                    }

                    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 0.8f + currentLayer * 0.1f);
                }
            }.runTaskLater(MagicExpansion.getInstance(), currentLayer * 3L);
        }
    }

    private void createCenterBeacon(Location location) {
        World world = location.getWorld();

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick++ >= 60) {
                    this.cancel();
                    return;
                }

                // 创建垂直光柱
                for (double y = 0; y < EXPLOSION_HEIGHT; y += 0.8) {
                    double radius = Math.sin(y * 0.1 + tick * 0.1) * 2 + 0.5;

                    for (int i = 0; i < 12; i++) {
                        double angle = i * Math.PI / 6;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);

                        Location beamLoc = location.clone().add(x, y, z);

                        Color color = getRainbowColor((y / EXPLOSION_HEIGHT + tick * 0.02) % 1.0);
                        world.spawnParticle(Particle.REDSTONE, beamLoc, 1,
                                new Particle.DustOptions(color, 1.2f));
                    }
                }

                // 顶部闪光
                if (tick % 5 == 0) {
                    Location top = location.clone().add(0, EXPLOSION_HEIGHT, 0);
                    world.spawnParticle(Particle.FLASH, top, 2, 1, 1, 1, 0);
                    world.playSound(top, Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
                }
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
    }

    private void launchMassFireworks(Location center) {
        World world = center.getWorld();

        // 播放史诗音效
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.4f);
        world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.5f, 0.7f);

        // 创建中心主烟花(调整数量:原来5,可以增加到8)
        for (int i = 0; i < 8; i++) { // 增加中心烟花数量
            final int index = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    launchCentralFireworkSingle(center, index);
                }
            }.runTaskLater(MagicExpansion.getInstance(), i * 1L); // 间隔1 tick
        }

        // 第一层:近距离小烟花(调整数量:12 -> 24)
        launchRingFireworks(center.clone().add(0, EXPLOSION_HEIGHT, 0), 10, 30, 0); // 12改为24

        // 第二层:中距离烟花(调整数量:16 -> 32)
        launchRingFireworks(center.clone().add(0, EXPLOSION_HEIGHT, 0), 20, 36, 5); // 16改为32

        // 第三层:远距离大烟花(调整数量:20 -> 40)
        launchRingFireworks(center.clone().add(0, EXPLOSION_HEIGHT, 0), 30, 42, 10); // 20改为40

        // 第四层:超级大烟花(调整数量:8 -> 16)
        launchSuperFireworks(center.clone().add(0, EXPLOSION_HEIGHT, 0), 40, 24, 15);
    }

    private void launchCentralFireworkSingle(Location center, int index) {
        World world = center.getWorld();

        Firework firework = world.spawn(center.clone().add(0, 1, 0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        // 设置强力烟花
        meta.setPower(4);

        // 多色组合
        List<Color> colors = new ArrayList<>();
        for (int j = 0; j < 3; j++) {
            colors.add(NEW_YEAR_COLORS[(index * 3 + j) % NEW_YEAR_COLORS.length]);
        }

        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(colors)
                .trail(true)
                .flicker(index % 2 == 0)
                .build());

        firework.setFireworkMeta(meta);
        firework.setVelocity(new Vector(0, 1.8, 0));

        // 创建强尾迹
        createPowerfulTrail(firework);

        // 设置爆炸 - 减少延迟
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!firework.isDead()) {
                    firework.detonate();
                    createCentralExplosion(firework.getLocation());
                }
            }
        }.runTaskLater(MagicExpansion.getInstance(), 20 + random.nextInt(10)); // 从45L减少到40L
    }


    private void launchRingFireworks(Location center, double radius, int count, int delayOffset) {
        World world = center.getWorld();

        // 发射环形烟花
        for (int i = 0; i < count; i++) {
            final int index = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double angle = index * (2 * Math.PI / count);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spawnLoc = center.clone().add(x, 0, z);

                    // 从地面发射
                    Firework firework = world.spawn(spawnLoc.clone().subtract(0, EXPLOSION_HEIGHT, 0), Firework.class);

                    // 计算发射方向(指向爆炸点)
                    Vector direction = spawnLoc.clone().subtract(firework.getLocation()).toVector().normalize();
                    direction.setY(1.0); // 保持向上分量
                    direction.multiply(1.5);

                    // 设置烟花
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.setPower(3);

                    List<Color> colors = new ArrayList<>();
                    colors.add(NEW_YEAR_COLORS[index % NEW_YEAR_COLORS.length]);
                    colors.add(NEW_YEAR_COLORS[(index + 4) % NEW_YEAR_COLORS.length]);

                    meta.addEffect(FireworkEffect.builder()
                            .with(getRandomType())
                            .withColor(colors)
                            .trail(true)
                            .flicker(random.nextBoolean())
                            .build());

                    firework.setFireworkMeta(meta);
                    firework.setVelocity(direction);

                    // 尾迹
                    createRocketTrail(firework);

                    // 同时爆炸
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!firework.isDead()) {
                                firework.detonate();
                                createRingExplosion(spawnLoc, index % 4);
                            }
                        }
                    }.runTaskLater(MagicExpansion.getInstance(), 20 + random.nextInt(10));
                }
            }.runTaskLater(MagicExpansion.getInstance(), delayOffset);
        }
    }

    private void launchSuperFireworks(Location center, double radius, int count, int delayOffset) {
        World world = center.getWorld();

        // 发射超级大烟花
        for (int i = 0; i < count; i++) {
            final int index = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double angle = index * (2 * Math.PI / count);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spawnLoc = center.clone().add(x, 0, z);

                    // 从更低位置发射
                    Firework firework = world.spawn(spawnLoc.clone().subtract(0, EXPLOSION_HEIGHT + 20, 0), Firework.class);

                    // 强力发射
                    Vector direction = new Vector(0, 2.0, 0);
                    firework.setVelocity(direction);

                    // 设置超级烟花
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.setPower(4);

                    List<Color> colors = new ArrayList<>();
                    for (int j = 0; j < 4; j++) {
                        colors.add(NEW_YEAR_COLORS[(index * 2 + j) % NEW_YEAR_COLORS.length]);
                    }

                    meta.addEffect(FireworkEffect.builder()
                            .with(FireworkEffect.Type.BURST)
                            .withColor(colors)
                            .withFade(Color.WHITE)
                            .trail(true)
                            .flicker(true)
                            .build());

                    firework.setFireworkMeta(meta);

                    // 超级尾迹
                    createSuperTrail(firework);

                    // 同时爆炸
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!firework.isDead()) {
                                firework.detonate();
                                createSuperExplosion(spawnLoc);
                            }
                        }
                    }.runTaskLater(MagicExpansion.getInstance(), 20 + random.nextInt(10));
                }
            }.runTaskLater(MagicExpansion.getInstance(), delayOffset);
        }
    }

    private void createPowerfulTrail(Firework firework) {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (firework.isDead()) {
                    this.cancel();
                    return;
                }

                Location loc = firework.getLocation();
                World world = loc.getWorld();

                // 多重粒子
                world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 8, 0.3, 0.3, 0.3, 0.1);

                // 彩虹尾迹
                double hue = (tick / 30.0) % 1.0;
                Color color = getRainbowColor(hue);
                world.spawnParticle(Particle.REDSTONE, loc, 5,
                        new Particle.DustOptions(color, 2.0f));

                // 闪电效果
                if (tick % 4 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double distance = random.nextDouble() * 2;

                        double x = distance * Math.cos(angle);
                        double z = distance * Math.sin(angle);

                        Location sparkLoc = loc.clone().add(x, 0, z);
                        world.spawnParticle(Particle.FLASH, sparkLoc, 1);
                    }
                    world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.8f);
                }

                tick++;
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
    }

    private void createRocketTrail(Firework firework) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (firework.isDead()) {
                    this.cancel();
                    return;
                }

                Location loc = firework.getLocation();
                World world = loc.getWorld();

                world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 4, 0.2, 0.2, 0.2, 0.05);

                Color color = NEW_YEAR_COLORS[random.nextInt(NEW_YEAR_COLORS.length)];
                world.spawnParticle(Particle.REDSTONE, loc, 2,
                        new Particle.DustOptions(color, 1.5f));
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
    }

    private void createSuperTrail(Firework firework) {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (firework.isDead()) {
                    this.cancel();
                    return;
                }

                Location loc = firework.getLocation();
                World world = loc.getWorld();

                // 大量火花
                world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 12, 0.4, 0.4, 0.4, 0.15);

                // 双重彩虹
                double hue1 = (tick / 25.0) % 1.0;
                double hue2 = (hue1 + 0.5) % 1.0;

                Color color1 = getRainbowColor(hue1);
                Color color2 = getRainbowColor(hue2);

                world.spawnParticle(Particle.REDSTONE, loc, 4,
                        new Particle.DustOptions(color1, 2.0f));
                world.spawnParticle(Particle.REDSTONE, loc, 4,
                        new Particle.DustOptions(color2, 2.0f));

                // 闪光和音效
                if (tick % 3 == 0) {
                    world.spawnParticle(Particle.FLASH, loc, 2, 0.5, 0.5, 0.5, 0);
                    world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.5f);
                }

                tick++;
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
    }

    private void createCentralExplosion(Location center) {
        World world = center.getWorld();

        // 播放强力爆炸音效
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.5f);
        world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 3.0f, 0.7f);

        // 多层爆炸效果
        for (int layer = 1; layer <= 12; layer++) {
            final int currentLayer = layer;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double radius = currentLayer * 15;
                    int particles = 200 + currentLayer * 50;

                    for (int i = 0; i < particles; i++) {
                        double phi = random.nextDouble() * Math.PI;
                        double theta = random.nextDouble() * 2 * Math.PI;

                        double x = radius * Math.sin(phi) * Math.cos(theta);
                        double y = radius * Math.cos(phi);
                        double z = radius * Math.sin(phi) * Math.sin(theta);

                        Location explosionLoc = center.clone().add(x, y, z);

                        // 彩虹色
                        double hue = (currentLayer + i / (double) particles) % 1.0;
                        Color color = getRainbowColor(hue);

                        world.spawnParticle(Particle.REDSTONE, explosionLoc, 2,
                                new Particle.DustOptions(color, 3.5f));

                        // 闪光
                        if (random.nextDouble() < 0.25) {
                            world.spawnParticle(Particle.FLASH, explosionLoc, 2);
                        }
                    }

                    // 音效
                    world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST,
                            2.0f, 0.6f + currentLayer * 0.05f);
                }
            }.runTaskLater(MagicExpansion.getInstance(), layer * 2L);
        }

        // 最终数字效果
        new BukkitRunnable() {
            @Override
            public void run() {
                create2026Text(center.clone().add(0, 25, 0));
            }
        }.runTaskLater(MagicExpansion.getInstance(), 20L);
    }

    private void createRingExplosion(Location center, int type) {
        World world = center.getWorld();

        world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 1.2f);

        switch (type) {
            case 0: // 球形爆炸
                createSphereExplosion(center, 15);
                break;
            case 1: // 环形爆炸
                createRingPattern(center, 12);
                break;
            case 2: // 星形爆炸
                createStarExplosion(center, 15);
                break;
            case 3: // 心形爆炸
                createHeartExplosion(center, 12);
                break;
        }

//        createEnhancedShockwave(center, 20);
    }

    private void createSuperExplosion(Location center) {
        World world = center.getWorld();

        // 播放增强的爆炸音效
        world.playSound(center, Sound.ENTITY_WITHER_DEATH, 3.0f, 0.3f);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.5f, 0.5f);

        // 更大的爆炸效果
        for (int layer = 1; layer <= 15; layer++) { // 从8增加到15
            final int currentLayer = layer;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double radius = currentLayer * 12; // 从7增加到12
                    int particles = 200 + currentLayer * 40; // 从120+增加到200+

                    for (int i = 0; i < particles; i++) {
                        double phi = random.nextDouble() * Math.PI;
                        double theta = random.nextDouble() * 2 * Math.PI;

                        double x = radius * Math.sin(phi) * Math.cos(theta);
                        double y = radius * Math.cos(phi);
                        double z = radius * Math.sin(phi) * Math.sin(theta);

                        Location explosionLoc = center.clone().add(x, y, z);

                        Color color;
                        if (currentLayer % 3 == 0) {
                            color = Color.fromRGB(255, 215, 0); // 金色
                        } else if (currentLayer % 3 == 1) {
                            color = Color.fromRGB(255, 50, 50); // 红色
                        } else {
                            color = Color.fromRGB(100, 200, 255); // 蓝色
                        }

                        world.spawnParticle(Particle.REDSTONE, explosionLoc, 3, // 从1增加到3
                                new Particle.DustOptions(color, 3.2f)); // 从2.5f增加到3.2f

                        // 更强的闪光效果
                        if (random.nextDouble() < 0.3) {
                            world.spawnParticle(Particle.FLASH, explosionLoc, 2);
                        }

                        // 更多的火花
                        if (random.nextDouble() < 0.4) {
                            world.spawnParticle(Particle.FIREWORKS_SPARK, explosionLoc,
                                    3, 0.3, 0.3, 0.3, 0.15);
                        }
                    }

                    // 增强的音效
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE,
                            1.8f, 0.5f + currentLayer * 0.04f);

                    // 添加冲击波效果
                    if (currentLayer % 4 == 0) {
                        createEnhancedShockwave(center, radius);
                    }
                }
            }.runTaskLater(MagicExpansion.getInstance(), layer * 1L);
        }
    }

    private void createEnhancedShockwave(Location center, double radius) {
        World world = center.getWorld();

        // 多层冲击波
        for (int wave = 0; wave < 3; wave++) {
            final int currentWave = wave;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double waveRadius = radius * (1 + currentWave * 0.3);

                    // 增加冲击波粒子密度
                    for (int i = 0; i < 144; i++) { // 从72增加到144
                        double angle = i * 2.5;
                        double radians = Math.toRadians(angle);

                        double x = waveRadius * Math.cos(radians);
                        double z = waveRadius * Math.sin(radians);

                        Location waveLoc = center.clone().add(x, 0, z);

                        // 彩色冲击波
                        Color color = getRainbowColor(i / 144.0);
                        world.spawnParticle(Particle.REDSTONE, waveLoc, 2,
                                new Particle.DustOptions(color, 2.8f));

                        // 垂直冲击波层
                        for (int h = -3; h <= 3; h++) {
                            Location verticalWave = waveLoc.clone().add(0, h * 2, 0);
                            world.spawnParticle(Particle.REDSTONE, verticalWave, 1,
                                    new Particle.DustOptions(color, 2.0f));
                        }
                    }

                    // 冲击波音效
                    world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                            1.2f + currentWave * 0.3f, 0.8f);
                }
            }.runTaskLater(MagicExpansion.getInstance(), currentWave * 5L);
        }
    }

    private void createSphereExplosion(Location center, double radius) {
        World world = center.getWorld();

        // 大幅增加粒子数量
        for (int i = 0; i < 400; i++) { // 从150增加到400
            double phi = random.nextDouble() * Math.PI;
            double theta = random.nextDouble() * 2 * Math.PI;
            double r = radius * (0.8 + random.nextDouble() * 0.4); // 增加填充密度

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.cos(phi);
            double z = r * Math.sin(phi) * Math.sin(theta);

            Location sphereLoc = center.clone().add(x, y, z);

            Color color = NEW_YEAR_COLORS[random.nextInt(NEW_YEAR_COLORS.length)];
            world.spawnParticle(Particle.REDSTONE, sphereLoc, 2, // 增加粒子数量
                    new Particle.DustOptions(color, 2.5f)); // 增大粒子大小

            // 添加内部填充粒子
            if (random.nextDouble() < 0.3) {
                double innerR = radius * random.nextDouble() * 0.7;
                double innerX = innerR * Math.sin(phi) * Math.cos(theta);
                double innerY = innerR * Math.cos(phi);
                double innerZ = innerR * Math.sin(phi) * Math.sin(theta);

                Location innerLoc = center.clone().add(innerX, innerY, innerZ);
                world.spawnParticle(Particle.REDSTONE, innerLoc, 1,
                        new Particle.DustOptions(color, 1.8f));
            }
        }

        // 添加爆炸边缘效果
        for (int i = 0; i < 100; i++) {
            double phi = random.nextDouble() * Math.PI;
            double theta = random.nextDouble() * 2 * Math.PI;

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi);
            double z = radius * Math.sin(phi) * Math.sin(theta);

            Location edgeLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.FIREWORKS_SPARK, edgeLoc, 1);
        }
    }

    private void createRingPattern(Location center, double radius) {
        World world = center.getWorld();

        // 增加环的层数
        for (int ring = 1; ring <= 5; ring++) { // 从3增加到5
            double ringRadius = radius * ring / 5.0;

            // 增加每环的粒子密度
            for (int i = 0; i < 96; i++) { // 从48增加到96
                double angle = i * 3.75; // 减小角度间隔,增加密度
                double radians = Math.toRadians(angle);

                double x = ringRadius * Math.cos(radians);
                double z = ringRadius * Math.sin(radians);

                Location ringLoc = center.clone().add(x, 0, z);

                Color color = getRainbowColor(i / 96.0);
                world.spawnParticle(Particle.REDSTONE, ringLoc, 2, // 增加粒子数量
                        new Particle.DustOptions(color, 2.2f)); // 增大粒子大小

                // 增加垂直环的厚度
                for (int v = -2; v <= 2; v++) {
                    Location verticalLoc = center.clone().add(0, x + v, z);
                    world.spawnParticle(Particle.REDSTONE, verticalLoc, 1,
                            new Particle.DustOptions(color, 1.8f));
                }
            }
        }
    }

    private void createStarExplosion(Location center, double size) {
        World world = center.getWorld();

        // 增加星形的密度
        for (double t = 0; t < 2 * Math.PI; t += 0.02) { // 从0.04减小到0.02,增加密度
            double r = size * (0.4 + 0.6 * Math.cos(5 * t));
            double x = r * Math.cos(t);
            double y = r * Math.sin(t);

            // 增加旋转平面的数量
            for (int rotation = 0; rotation < 4; rotation++) { // 从2增加到4
                double rotAngle = rotation * Math.PI / 4;
                double xRot = x * Math.cos(rotAngle) - y * Math.sin(rotAngle);
                double yRot = x * Math.sin(rotAngle) + y * Math.cos(rotAngle);

                // 主星形
                Location starLoc = center.clone().add(xRot, yRot, 0);
                Color color = Color.fromRGB(255, 255, 150 + rotation * 30);
                world.spawnParticle(Particle.REDSTONE, starLoc, 2, // 增加粒子数量
                        new Particle.DustOptions(color, 2.2f));

                // 另一平面
                Location starLoc2 = center.clone().add(0, yRot, xRot);
                world.spawnParticle(Particle.REDSTONE, starLoc2, 2,
                        new Particle.DustOptions(color, 2.2f));

                // 第三平面
                Location starLoc3 = center.clone().add(yRot, 0, xRot);
                world.spawnParticle(Particle.REDSTONE, starLoc3, 1,
                        new Particle.DustOptions(color, 1.8f));
            }
        }

        // 添加星形闪光点
        for (int i = 0; i < 12; i++) {
            double angle = i * 30;
            double radians = Math.toRadians(angle);
            double tipRadius = size * 1.2; // 超出主星形的尖端点

            double x = tipRadius * Math.cos(radians);
            double y = tipRadius * Math.sin(radians);

            Location tipLoc = center.clone().add(x, y, 0);
            world.spawnParticle(Particle.FLASH, tipLoc, 2);
            world.playSound(tipLoc, Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, 1.5f);
        }
    }

    private void createHeartExplosion(Location center, double size) {
        World world = center.getWorld();

        // 心形公式
        for (double t = 0; t < 2 * Math.PI; t += 0.02) {
            double x = size * Math.pow(Math.sin(t), 3);
            double y = size * (13 * Math.cos(t) - 5 * Math.cos(2*t) - 2 * Math.cos(3*t) - Math.cos(4*t)) / 16;

            // 旋转心形
            for (int rotation = 0; rotation < 4; rotation++) {
                double angle = rotation * Math.PI / 2;
                double xRot = x * Math.cos(angle);
                double zRot = x * Math.sin(angle);

                Location heartLoc = center.clone().add(xRot, y, zRot);

                Color color = Color.fromRGB(255, 105, 180); // 粉色
                world.spawnParticle(Particle.REDSTONE, heartLoc, 1,
                        new Particle.DustOptions(color, 2.0f));
            }
        }
    }

    private void createShockwave(Location center, double radius) {
        World world = center.getWorld();

        // 创建冲击波环
        for (int i = 0; i < 72; i++) {
            double angle = i * 5;
            double radians = Math.toRadians(angle);

            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            Location waveLoc = center.clone().add(x, 0, z);

            Color color = Color.WHITE;
            world.spawnParticle(Particle.REDSTONE, waveLoc, 3,
                    new Particle.DustOptions(color, 3.0f));

            // 垂直冲击波
            for (int h = -5; h <= 5; h += 2) {
                Location verticalWave = waveLoc.clone().add(0, h, 0);
                world.spawnParticle(Particle.REDSTONE, verticalWave, 2,
                        new Particle.DustOptions(color, 2.0f));
            }
        }

        world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.7f);
    }

    private void create2026Text(Location center) {
        World world = center.getWorld();

        // 播放文字音效
        world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 3.0f, 0.7f);
        world.playSound(center, Sound.BLOCK_BELL_USE, 2.0f, 1.0f);

        String number = "2026";

        for (int i = 0; i < number.length(); i++) {
            final int digitIndex = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    char digit = number.charAt(digitIndex);
                    int[][] pattern = getNumberPattern(digit);

                    for (int[] point : pattern) {
                        double x = center.getX() + (digitIndex * 15) + point[0] - 22.5;
                        double y = center.getY() + point[1];
                        double z = center.getZ() + point[2];

                        Location digitLoc = new Location(world, x, y, z);

                        // 立即显示
                        Color color = Color.fromRGB(255, 215, 0); // 金色
                        world.spawnParticle(Particle.REDSTONE, digitLoc, 2,
                                new Particle.DustOptions(color, 2.5f));

                        // 光晕效果
                        for (int j = 0; j < 8; j++) {
                            double angle = j * 45;
                            double radians = Math.toRadians(angle);
                            double radius = 0.8;

                            double haloX = radius * Math.cos(radians);
                            double haloZ = radius * Math.sin(radians);

                            Location haloLoc = digitLoc.clone().add(haloX, 0, haloZ);
                            world.spawnParticle(Particle.REDSTONE, haloLoc, 1,
                                    new Particle.DustOptions(Color.WHITE, 1.2f));
                        }

                        world.playSound(digitLoc, Sound.BLOCK_NOTE_BLOCK_BIT, 0.3f, 1.8f);
                    }
                }
            }.runTaskLater(MagicExpansion.getInstance(), i * 2L);
        }

        // 文字闪烁
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int blink = 0; blink < 5; blink++) {
                    final int currentBlink = blink;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            boolean show = currentBlink % 2 == 0;
                            if (show) {
                                for (int i = 0; i < number.length(); i++) {
                                    int[][] pattern = getNumberPattern(number.charAt(i));
                                    for (int[] point : pattern) {
                                        double x = center.getX() + (i * 15) + point[0] - 22.5;
                                        double y = center.getY() + point[1];
                                        double z = center.getZ() + point[2];

                                        Location digitLoc = new Location(world, x, y, z);
                                        Color color = getRainbowColor(currentBlink / 5.0);
                                        world.spawnParticle(Particle.REDSTONE, digitLoc, 1,
                                                new Particle.DustOptions(color, 3.0f));
                                    }
                                }
                                world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.9f);
                            }
                        }
                    }.runTaskLater(MagicExpansion.getInstance(), blink * 8L);
                }
            }
        }.runTaskLater(MagicExpansion.getInstance(), 30L);
    }

    private FireworkEffect.Type getRandomType() {
        FireworkEffect.Type[] types = {
                FireworkEffect.Type.BALL,
                FireworkEffect.Type.BALL_LARGE,
                FireworkEffect.Type.STAR,
                FireworkEffect.Type.CREEPER,
                FireworkEffect.Type.BURST
        };
        return types[random.nextInt(types.length)];
    }

    private int[][] getNumberPattern(char digit) {
        switch (digit) {
            case '2':
                return new int[][]{
                        {0,0,0},{1,0,0},{2,0,0},{3,0,0},{4,0,0},
                        {4,1,0},{4,2,0},{4,3,0},
                        {3,3,0},{2,3,0},{1,3,0},{0,3,0},
                        {0,4,0},{0,5,0},{0,6,0},
                        {1,6,0},{2,6,0},{3,6,0},{4,6,0}
                };
            case '0':
                return new int[][]{
                        {0,0,0},{1,0,0},{2,0,0},{3,0,0},{4,0,0},
                        {0,1,0},{4,1,0},{0,2,0},{4,2,0},
                        {0,3,0},{4,3,0},{0,4,0},{4,4,0},
                        {0,5,0},{4,5,0},{0,6,0},{4,6,0},
                        {0,6,0},{1,6,0},{2,6,0},{3,6,0},{4,6,0}
                };
            case '6':
                return new int[][]{
                        {2,0,0},{3,0,0},{4,0,0},
                        {1,1,0},{0,2,0},
                        {0,3,0},{1,3,0},{2,3,0},{3,3,0},{4,3,0},
                        {0,4,0},{4,4,0},{0,5,0},{4,5,0},
                        {0,6,0},{1,6,0},{2,6,0},{3,6,0},{4,6,0}
                };
            default:
                return new int[][]{{0,0,0}};
        }
    }

    private Color getRainbowColor(double position) {
        position = position % 1.0;

        if (position < 0.166) {
            return Color.fromRGB(255, (int)(position * 6 * 255), 0);
        } else if (position < 0.333) {
            return Color.fromRGB(255, 255, 0);
        } else if (position < 0.5) {
            position = (position - 0.333) * 6;
            return Color.fromRGB((int)((1-position) * 255), 255, 0);
        } else if (position < 0.666) {
            position = (position - 0.5) * 6;
            return Color.fromRGB(0, 255, (int)(position * 255));
        } else if (position < 0.833) {
            position = (position - 0.666) * 6;
            return Color.fromRGB(0, (int)((1-position) * 255), 255);
        } else {
            position = (position - 0.833) * 6;
            return Color.fromRGB((int)(position * 255), 0, 255);
        }
    }
}
