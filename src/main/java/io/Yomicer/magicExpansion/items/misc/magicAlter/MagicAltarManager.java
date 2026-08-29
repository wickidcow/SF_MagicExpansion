package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun; // E4: 领地保护检查所需（照抄项目内 getProtectionManager 现有用法）
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction; // E4: 交互权限枚举
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class MagicAltarManager {

    private final MagicExpansion plugin;
    private final Set<Location> activeAltars;
    private final HashMap<Location, BukkitTask> altarTasks;
    private final HashMap<Location, BukkitTask> particleTasks;
    private final Material[][][] altarPatterns;
    private final LinkedHashMap<String, MagicAltarRecipe> recipes;

    public MagicAltarManager(MagicExpansion plugin) {
        this.plugin = plugin;
        this.activeAltars = new HashSet<>();
        this.altarTasks = new HashMap<>();
        this.particleTasks = new HashMap<>();
        this.recipes = new LinkedHashMap<>();
        this.altarPatterns = loadAltarPatterns();
    }

    Material[][] pattern1 = {
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
            {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
    };

    Material[][] customBase = {
            {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.OBSIDIAN, Material.MAGMA_BLOCK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.MAGMA_BLOCK, Material.NETHERRACK},
            {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK}
    };

    Material[][] enchantAlter = {
            {Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.BLUE_CONCRETE, Material.BLUE_TERRACOTTA, Material.BLUE_CONCRETE, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.BLUE_TERRACOTTA, Material.ENCHANTING_TABLE, Material.BLUE_TERRACOTTA, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.BLUE_CONCRETE, Material.BLUE_TERRACOTTA, Material.BLUE_CONCRETE, Material.LAPIS_BLOCK},
            {Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK},
    };

    // 定义祭坛的方块布局模式
    private Material[][][] loadAltarPatterns() {
        List<Material[][]> patternList = new ArrayList<>();
        new DefaultRecipes().registerAltarPatterns(patternList);
        return patternList.toArray(new Material[0][][]);
    }

    // 加载配方
    public void loadRecipes() {
        recipes.clear();
        new DefaultRecipes().registerRecipes(recipes);
        Debug.logInfo("已加载 " + recipes.size() + " 个魔法祭坛配方");
    }

    // 检查祭坛结构是否正确
    public boolean checkAltarStructure(Location centerDispenserLoc) {
        Location altarBase = centerDispenserLoc.clone().subtract(0, 1, 0);

        for (Material[][] pattern : altarPatterns) {
            boolean matches = true;

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = altarBase.clone().add(x, 0, z).getBlock();
                    Material expected = pattern[x + 2][z + 2];
                    Material actual = block.getType();

                    if (actual != expected) {
                        matches = false;
                        break;
                    }
                }
                if (!matches) break;
            }

            if (matches) {
                return true;
            }
        }

        return false;
    }

    // 检查发射器布局
    public boolean checkDispenserLayout(Location centerDispenserLoc) {
        if (centerDispenserLoc.getBlock().getType() != Material.DISPENSER) {
            return false;
        }

        int dispenserCount = 1;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;

                Location loc = centerDispenserLoc.clone().add(x, 0, z);
                if (loc.getBlock().getType() == Material.DISPENSER) {
                    dispenserCount++;
                }
            }
        }

        return dispenserCount == 9;
    }

    // 检查物品展示框
    public boolean checkItemFrame(Location centerDispenserLoc) {
        Location frameLoc = centerDispenserLoc.clone().add(0, 1, 0);

        // E1 修复：原 getWorld().getEntitiesByClass(ItemFrame.class) 会遍历整个世界的实体，
        // 改为参考 completeCrafting 中已有用法 getNearbyEntitiesByType，半径 2（覆盖祭坛结构实际尺寸）局部搜索
        for (org.bukkit.entity.ItemFrame itemFrame : frameLoc.getWorld().getNearbyEntitiesByType(org.bukkit.entity.ItemFrame.class, frameLoc, 2)) {
            Location itemFrameLoc = itemFrame.getLocation();
            if (itemFrameLoc.getBlockX() == frameLoc.getBlockX() &&
                    itemFrameLoc.getBlockY() == frameLoc.getBlockY() &&
                    itemFrameLoc.getBlockZ() == frameLoc.getBlockZ()) {

                ItemStack frameItem = itemFrame.getItem();
                if (frameItem == null || frameItem.getType() == Material.AIR) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    // 获取所有发射器的物品 - 按照固定顺序
    public ItemStack[][] getDispenserItems(Location centerDispenserLoc) {
        ItemStack[][] items = new ItemStack[9][9];

        // 定义发射器位置顺序（固定顺序，从左上开始顺时针）
        int[][] positions = {
                {-1, -1}, // 0: 左上
                {0, -1},  // 1: 中上
                {1, -1},  // 2: 右上
                {-1, 0},  // 3: 左中
                {0, 0},   // 4: 中心
                {1, 0},   // 5: 右中
                {-1, 1},  // 6: 左下
                {0, 1},   // 7: 中下
                {1, 1}    // 8: 右下
        };

        // 按照固定顺序获取所有发射器物品
        for (int i = 0; i < 9; i++) {
            int[] pos = positions[i];
            Location loc = centerDispenserLoc.clone().add(pos[0], 0, pos[1]);

            if (loc.getBlock().getState() instanceof Dispenser) {
                Dispenser dispenser = (Dispenser) loc.getBlock().getState();

                for (int slot = 0; slot < 9; slot++) {
                    ItemStack item = dispenser.getInventory().getItem(slot);
                    items[i][slot] = (item != null) ? item.clone() : null;
                }
            }
        }

        return items;
    }

    // 开始合成过程 - 强制方位匹配
    public boolean startCrafting(Location centerDispenserLoc, Player player) {
        // E4 修复：进入合成流程前先做领地保护检查（用法照抄项目内 getProtectionManager 现有写法），
        // 无交互权限的玩家不能触发祭坛合成
        if (!Slimefun.getProtectionManager().hasPermission(player, centerDispenserLoc, Interaction.INTERACT_BLOCK)) {
            player.sendMessage("§c✗ 你没有权限在此处使用魔法祭坛!");
            return false;
        }

        if (activeAltars.contains(centerDispenserLoc)) {
            player.sendMessage("§c✗ 这个祭坛正在进行合成!");
            return false;
        }

        // 检查结构
        if (!checkAltarStructure(centerDispenserLoc)) {
            player.sendMessage("§c✗ 祭坛底座结构不正确!");
            return false;
        }

        if (!checkDispenserLayout(centerDispenserLoc)) {
            player.sendMessage("§c✗ 发射器布局不正确!");
            return false;
        }

        if (!checkItemFrame(centerDispenserLoc)) {
            player.sendMessage("§c✗ 中心发射器上方需要空的物品展示框!");
            return false;
        }

        ItemStack[][] items = getDispenserItems(centerDispenserLoc);
        MagicAltarRecipe matchedRecipe = null;
        String recipeName = null;

        // 发送发射器物品信息给玩家
        sendDispenserItemsToPlayer(player, items);

        for (java.util.Map.Entry<String, MagicAltarRecipe> entry : recipes.entrySet()) {
            String currentRecipeName = entry.getKey();
            MagicAltarRecipe recipe = entry.getValue();

            if (recipe.matches(items)) {
                matchedRecipe = recipe;
                recipeName = currentRecipeName;
//                Bukkit.getLogger().info("找到匹配的配方: " + recipeName);
                break;
            }
        }

        if (matchedRecipe == null) {
            player.sendMessage("§c✗ 没有找到匹配的配方!");
            player.sendMessage("§7请检查物品数量和位置是否正确");
            return false;
        }

        activeAltars.add(centerDispenserLoc);
        player.sendMessage("§a✓ 发现配方: §e" + recipeName);
        player.sendMessage("§a▶ 开始合成! 请勿移动任何物品...");

        startParticleEffect(centerDispenserLoc);

        // 创建合成任务 - 15秒合成时间
        MagicAltarRecipe finalMatchedRecipe = matchedRecipe;
        BukkitTask task = new BukkitRunnable() {
            int timeLeft = 15; // 15秒合成时间

            @Override
            public void run() {
                // 每秒钟检查一次结构完整性
                if (!checkStructureIntegrity(centerDispenserLoc, player)) {
                    player.sendMessage("§c✗ 合成中断! 祭坛结构被破坏或物品被移动!");
                    stopParticleEffect(centerDispenserLoc);
                    activeAltars.remove(centerDispenserLoc);
                    this.cancel();
                    altarTasks.remove(centerDispenserLoc);
                    return;
                }

                // 进度提示
                if (timeLeft == 11) {
                    player.sendMessage("§6⏳ 合成进度: §e25% §6完成");
                } else if (timeLeft == 8) {
                    player.sendMessage("§6⏳ 合成进度: §e50% §6完成");
                } else if (timeLeft == 4) {
                    player.sendMessage("§6⏳ 合成进度: §e75% §6完成");
                } else if (timeLeft <= 0) {
                    // 合成完成
                    completeCrafting(centerDispenserLoc, finalMatchedRecipe, player);
                    stopParticleEffect(centerDispenserLoc);
                    strikeColoredLightning(centerDispenserLoc, 18);
                    this.cancel();
                    altarTasks.remove(centerDispenserLoc);
                    return;
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次

        altarTasks.put(centerDispenserLoc, task);
        return true;
    }


    public void strikeColoredLightning(Location center, int count) {
        World world = center.getWorld();

        // 将中心位置调整为方块的真正中心
        Location centerLoc = center.clone().add(0.5, 0, 0.5);

        for (int i = 0; i < count; i++) {
            // 生成随机偏移（以方块中心为基准）
            double offsetX = (Math.random() - 0.5) * 8; // 减小偏移范围
            double offsetZ = (Math.random() - 0.5) * 8;
            double offsetY = Math.random() * 2; // 减小高度变化范围

            Location strikeLocation = centerLoc.clone().add(offsetX, offsetY, offsetZ);

            // 生成彩色闪电效果
            createColoredLightningEffect(strikeLocation);

            // 播放雷声
            world.playSound(strikeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
    }

    private void createColoredLightningEffect(Location location) {
        World world = location.getWorld();
        Particle.DustOptions dustOptions = getRandomLightningColor();

        // E3 修复：主闪电束粒子生成限制在落点上方 10 格内（原为 70 格），并降低每格粒子数（约减半）
        for (int y = 10; y >= 0; y--) {
            Location particleLoc = location.clone().add(0, y, 0);

            // 增加粒子数量（E3: 15 → 8，约减半）
            world.spawnParticle(Particle.REDSTONE, particleLoc, 8, 0.2, 0.2, 0.2, 0, dustOptions);

            // 添加分支效果
            if (Math.random() < 0.4) {
                double branchX = (Math.random() - 0.5) * 1.5;
                double branchZ = (Math.random() - 0.5) * 1.5;

                for (int branch = 0; branch < 2; branch++) {
                    Location branchLoc = particleLoc.clone().add(branchX * branch, 0, branchZ * branch);
                    // E3: 6 → 3，约减半
                    world.spawnParticle(Particle.REDSTONE, branchLoc, 3, 0.15, 0.15, 0.15, 0, dustOptions);
                }
            }

            // 火花效果
            if (Math.random() < 0.5) {
                for (int spark = 0; spark < 2; spark++) {
                    Location sparkLoc = particleLoc.clone().add(
                            (Math.random() - 0.5) * 2,
                            (Math.random() - 0.5) * 1.5,
                            (Math.random() - 0.5) * 2
                    );
                    // E3: 火花粒子 4 → 2、暴击粒子 2 → 1，约减半
                    world.spawnParticle(Particle.FIREWORKS_SPARK, sparkLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.CRIT, sparkLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }

        // 地面冲击波效果 - 以闪电落点为中心
        for (int i = 0; i < 2; i++) {
            double radius = 1.0 + i * 0.5;
            for (int angle = 0; angle < 360; angle += 20) {
                double rad = Math.toRadians(angle);
                double x = Math.cos(rad) * radius;
                double z = Math.sin(rad) * radius;
                Location circleLoc = location.clone().add(x, 0.2, z);
                world.spawnParticle(Particle.REDSTONE, circleLoc, 2, 0.1, 0.1, 0.1, 0, dustOptions);
            }
        }
    }

    private Particle.DustOptions getRandomLightningColor() {
        int colorChoice = (int) (Math.random() * 5);
        switch (colorChoice) {
            case 0: // 蓝色闪电
                return new Particle.DustOptions(Color.fromRGB(0, 150, 255), 2.0f);
            case 1: // 紫色闪电
                return new Particle.DustOptions(Color.fromRGB(150, 0, 255), 2.0f);
            case 2: // 青色闪电
                return new Particle.DustOptions(Color.fromRGB(0, 255, 255), 2.0f);
            case 3: // 红色闪电
                return new Particle.DustOptions(Color.fromRGB(255, 50, 50), 2.0f);
            default: // 白色闪电
                return new Particle.DustOptions(Color.fromRGB(255, 255, 255), 2.0f);
        }
    }


    // 检查结构完整性（在合成过程中持续检查）
    private boolean checkStructureIntegrity(Location centerDispenserLoc, Player player) {
        // 检查祭坛底座
        if (!checkAltarStructure(centerDispenserLoc)) {
            return false;
        }

        // 检查发射器布局
        if (!checkDispenserLayout(centerDispenserLoc)) {
            return false;
        }

        // 检查物品展示框
        if (!checkItemFrame(centerDispenserLoc)) {
            return false;
        }

        // 检查配方物品是否还在
        ItemStack[][] currentItems = getDispenserItems(centerDispenserLoc);
        for (MagicAltarRecipe recipe : recipes.values()) {
            if (recipe.matches(currentItems)) {
                return true; // 至少有一个配方匹配，说明物品还在
            }
        }

        return false; // 没有配方匹配，说明物品被移动了
    }

    // 发送发射器物品信息给玩家
    private void sendDispenserItemsToPlayer(Player player, ItemStack[][] items) {
        player.sendMessage("§6=== 发射器物品信息 ===");

        String[] dispenserNames = {
                "§b左上发射器", "§b中上发射器", "§b右上发射器",
                "§b左中发射器", "§b中心发射器", "§b右中发射器",
                "§b左下发射器", "§b中下发射器", "§b右下发射器"
        };

        for (int i = 0; i < 9; i++) {
            boolean hasItems = false;
            StringBuilder itemList = new StringBuilder();

            for (int slot = 0; slot < 9; slot++) {
                if (items[i][slot] != null) {
                    hasItems = true;
                    if (itemList.length() > 0) itemList.append(", ");
                    itemList.append(ItemStackHelper.getDisplayName(items[i][slot])).append("x").append(items[i][slot].getAmount());
                }
            }

            if (hasItems) {
                player.sendMessage(dispenserNames[i] + ": " + itemList.toString());
            } else {
                player.sendMessage(dispenserNames[i] + ": §7空");
            }
        }
        player.sendMessage("§6===================");
    }

    // 开始粒子效果
    private void startParticleEffect(Location centerDispenserLoc) {
        // 目标位置为中心方块的精确中心
        Location targetLoc = centerDispenserLoc.clone().add(0.5, 1.5, 0.5);

        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 半径设为1，以中心方块的真正中心为原点
                double radius = 2.0;
                int particleSources = 12;

                // 从四周生成粒子飞向中心
                for (int i = 0; i < particleSources; i++) {
                    double angle = 2 * Math.PI * i / particleSources;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    // 在垂直方向也生成粒子
                    for (int h = 0; h < 3; h++) {
                        double y = (h - 1) * 0.5; // -0.5, 0, 0.5 三个高度

                        // 源位置相对于目标位置的偏移
                        Location sourceLoc = targetLoc.clone().add(x, y, z);
                        Vector direction = targetLoc.clone().subtract(sourceLoc).toVector().normalize();

                        // 每个源生成粒子
                        for (int j = 0; j < 2; j++) {
                            // 小范围随机偏移
                            double offsetX = (Math.random() - 0.5) * 0.6;
                            double offsetY = (Math.random() - 0.5) * 0.6;
                            double offsetZ = (Math.random() - 0.5) * 0.6;

                            Location particleLoc = sourceLoc.clone().add(offsetX, offsetY, offsetZ);

                            // 使用多种粒子类型
                            Particle particleType = Math.random() < 0.9 ? Particle.ENCHANTMENT_TABLE : Particle.ELECTRIC_SPARK;

                            sourceLoc.getWorld().spawnParticle(
                                    particleType,
                                    particleLoc,
                                    6,
                                    direction.getX() * 0.15,
                                    direction.getY() * 0.15,
                                    direction.getZ() * 0.15,
                                    0.1
                            );
                        }
                    }
                }

                // 中心聚集效果 - 在方块中央
                targetLoc.getWorld().spawnParticle(
                        Particle.SPELL_WITCH,
                        targetLoc,
                        10,
                        0.2, 0.2, 0.2,
                        0.1
                );

                targetLoc.getWorld().spawnParticle(
                        Particle.ENCHANTMENT_TABLE,
                        targetLoc,
                        23,
                        0.15, 0.15, 0.15,
                        0.08
                );
            }
        }.runTaskTimer(plugin, 0L, 4L);

        particleTasks.put(centerDispenserLoc, particleTask);
    }

    // 停止粒子效果
    private void stopParticleEffect(Location centerDispenserLoc) {
        BukkitTask particleTask = particleTasks.remove(centerDispenserLoc);
        if (particleTask != null) {
            particleTask.cancel();
        }
    }

    // 完成合成
    private void completeCrafting(Location centerDispenserLoc, MagicAltarRecipe recipe, Player player) {
        ItemStack[][] dispenserItems = getDispenserItems(centerDispenserLoc);

        // 定义发射器位置顺序（固定顺序）
        int[][] positions = {
                {-1, -1}, // 0: 左上
                {0, -1},  // 1: 中上
                {1, -1},  // 2: 右上
                {-1, 0},  // 3: 左中
                {0, 0},   // 4: 中心
                {1, 0},   // 5: 右中
                {-1, 1},  // 6: 左下
                {0, 1},   // 7: 中下
                {1, 1}    // 8: 右下
        };

        // 直接清空所有发射器内的配方物品（保持方块本体不动）
        for (int i = 0; i < 9; i++) {
            int[] pos = positions[i];
            Location loc = centerDispenserLoc.clone().add(pos[0], 0, pos[1]);

            // 确保位置正确
            if (loc.getBlock().getType() == Material.DISPENSER) {
                BlockState state = loc.getBlock().getState();
                if (state instanceof Dispenser) {
                    Dispenser dispenser = (Dispenser) state;

                    // E2 修复：不再 setType(AIR)+setType(DISPENSER) 重建发射器——
                    // 那会销毁方块实体导致朝向以外的数据（如其他插件写入的 NBT）丢失；
                    // 这里仅清空物品栏即可达成“移除配方物品”的目的
                    Inventory inventory = dispenser.getInventory();
                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        inventory.setItem(slot, null);
                    }
                    dispenser.update(true, false);
                }
            }
        }

        // 将结果物品放入物品展示框
        Location frameLoc = centerDispenserLoc.clone().add(0, 1, 0);
        for (ItemFrame itemFrame : frameLoc.getWorld().getNearbyEntitiesByType(org.bukkit.entity.ItemFrame.class, frameLoc, 0.5)) {
            Location itemFrameLoc = itemFrame.getLocation();
            if (itemFrameLoc.getBlockX() == frameLoc.getBlockX() &&
                    itemFrameLoc.getBlockY() == frameLoc.getBlockY() &&
                    itemFrameLoc.getBlockZ() == frameLoc.getBlockZ()) {

                ItemStack result = recipe.getResult().clone();

                if (result.getAmount() > 1) {
                    // 如果数量大于1，展示框只放1个
                    ItemStack displayItem = result.clone();
                    displayItem.setAmount(1);
                    itemFrame.setItem(displayItem);

                    // 掉落剩余数量的物品（使用分批掉落）
                    ItemStack dropItem = result.clone();
                    dropItem.setAmount(result.getAmount() - 1);
                    dropItemInBatches(player, itemFrame.getLocation(), dropItem);
                } else {
                    // 如果数量只有1个，直接放在展示框上
                    itemFrame.setItem(result);
                }
                break;
            }
        }

        Location targetLoc = centerDispenserLoc.clone().add(0.5, 1.5, 0.5);
        targetLoc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, targetLoc, 30, 0.5, 0.5, 0.5, 0.1);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        player.sendMessage("§a✓ 合成完成! 100% §a获得了 " + recipe.getResult().getType().name());
        activeAltars.remove(centerDispenserLoc);
    }

    // 分批掉落物品的方法
    private static void dropItemInBatches(Player player, Location location, ItemStack item) {
        int totalAmount = item.getAmount();
        int maxDropSize = 64; // 单次最大掉落数量

        while (totalAmount > 0) {
            // 创建一个新的物品副本，设置当前批次的数量
            ItemStack dropItem = item.clone();
            int currentBatchSize = Math.min(totalAmount, maxDropSize);
            dropItem.setAmount(currentBatchSize);

            // 掉落当前批次的物品
            player.getWorld().dropItemNaturally(location, dropItem);

            // 更新剩余数量
            totalAmount -= currentBatchSize;
        }
    }
    // 取消所有任务
    public void cancelAllTasks() {
        for (BukkitTask task : altarTasks.values()) {
            task.cancel();
        }
        altarTasks.clear();

        for (BukkitTask task : particleTasks.values()) {
            task.cancel();
        }
        particleTasks.clear();

        activeAltars.clear();
    }

    public boolean isAltarActive(Location centerDispenserLoc) {
        return activeAltars.contains(centerDispenserLoc);
    }

    public HashMap<String, MagicAltarRecipe> getRecipes() {
        return recipes;
    }
}