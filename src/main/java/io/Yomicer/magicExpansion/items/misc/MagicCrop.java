package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.electric.entitykillMachinee.EntityKillMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Placeable;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.guizhanss.guizhanlib.slimefun.utils.BlockStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;

public class MagicCrop extends SlimefunItem implements Listener, RecipeDisplayItem, Placeable {

    // D 修复：本插件魔法作物可能占据的方块材质集合（种子物品 WHEAT_SEEDS/POTATO/CARROT/BEETROOT_SEEDS
    // 播种后对应的小麦/土豆/胡萝卜/甜菜根方块），用于 BlockPhysicsEvent 的快速材质过滤
    private static final Set<Material> CROP_BLOCK_MATERIALS = new HashSet<>(Arrays.asList(
            Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.BEETROOTS
    ));

    private final ItemStack seedDrop;
//    private final ItemStack[] fruitDrops;
    private final List<WeightedDrop> weightedDrops;
    private final int totalWeight;
    private final int min;
    private final int minDisplay;
    private final int maxDisplay;
    private final int bound;

    public static class WeightedDrop {
        public final ItemStack item;
        public final int weight;

        public WeightedDrop(ItemStack item, int weight) {
            this.item = item;
            this.weight = weight;
        }
    }

    public MagicCrop(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, List<WeightedDrop> drops, int min, int max) {
        super(itemGroup, item, recipeType, recipe);
        this.seedDrop = item;
        this.weightedDrops = new ArrayList<>(drops);
        this.min = min-1;
        this.minDisplay = min;
        this.maxDisplay = max;
        this.bound = max - min;
        int sum = 0;
        for (WeightedDrop drop : weightedDrops) {
            if (drop.weight > 0) {
                sum += drop.weight;
            }
        }
        this.totalWeight = sum;
        addItemHandler(onBlockBreak(), onBlockPlace());
        Bukkit.getPluginManager().registerEvents(this, MagicExpansion.getInstance());
    }
    /**
     * 处理玩家破坏 (正常收获)
     */
    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                Block block = e.getBlock();
                SlimefunItem sfItem = StorageCacheUtils.getSfItem(block.getLocation());
                if (sfItem != MagicCrop.this) {
                    return;
                }
                handleCropDrops(block, drops);
            }
        };
    }
    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(true) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                StorageCacheUtils.setData(e.getBlockPlaced().getLocation(), "type", "none");
            }
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent e) {
        Block block = e.getBlock();
        // D 修复：BlockPhysicsEvent 触发极其频繁，先按作物方块材质快速过滤，
        // 非作物方块直接返回，避免对每个物理更新都去查询 BlockStorage（高性能开销）
        if (!CROP_BLOCK_MATERIALS.contains(block.getType())) {
            return;
        }
        Block below = block.getRelative(0, -1, 0);
        SlimefunItem sfItem = StorageCacheUtils.getSfItem(block.getLocation());
        if (sfItem != MagicCrop.this) {
            return;
        }
        if (block.getType() == Material.WATER || block.getType() == Material.AIR) {
            handleDestructionByWater(block, e);
            return;
        }

        if (below.getType() == Material.FARMLAND || below.getType() == Material.SOUL_SAND) {
            return;
        }
        handleDestruction(block, e);
    }

    private void handleDestructionByWater(Block block, BlockPhysicsEvent e) {
        e.setCancelled(true);
        Location loc = block.getLocation();
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(loc.clone());
        block.setType(Material.AIR, false);
    }

    /**
     * 统一的销毁与掉落逻辑
     */
    private void handleDestruction(Block block, BlockPhysicsEvent e) {
        Location loc = block.getLocation();

        // A. 清理 Slimefun 数据
        Slimefun.getDatabaseManager().getBlockDataController().removeBlock(loc.clone());

        // B. 获取生长阶段以计算掉落
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            // 如果数据损坏或不是作物，直接清空
            block.setType(Material.AIR, false);
            return;
        }

        int age = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        // C. 取消原版物理行为 (防止原版把方块冲走但不掉落，或者变成其他方块)
        e.setCancelled(true);

        // D. 生成自定义掉落物
        Location dropLoc = loc.clone().add(0.5, 0.5, 0.5);

        if (age < maxAge) {
            // 未成熟：只掉种子
            block.getWorld().dropItemNaturally(dropLoc, new CustomItemStack(seedDrop, 1));
        } else {
            // 成熟：掉种子 + 果实
            block.getWorld().dropItemNaturally(dropLoc, new CustomItemStack(seedDrop, 1));

            // 生成指定数量的果实
            int fruitCount = min + ThreadLocalRandom.current().nextInt(bound);
            for (int i = 0; i < fruitCount; i++) {
                if (!weightedDrops.isEmpty()) {
                    ItemStack selected = getWeightedRandomItem();
                    if (selected != null) {
                        block.getWorld().dropItemNaturally(dropLoc, new CustomItemStack(selected, 1));
                    }
                }
            }
        }

        // E. 移除方块 (使用 false 避免递归触发物理事件)
        block.setType(Material.AIR, false);
    }

    /**
     * 玩家破坏时的掉落计算 (复用逻辑)
     */
    private void handleCropDrops(Block block, List<ItemStack> drops) {
        if (!(block.getBlockData() instanceof Ageable ageable)) return;

        int age = ageable.getAge();
        int maxAge = ageable.getMaximumAge();
        drops.clear();
        if (age >= maxAge) {
            drops.add(new CustomItemStack(seedDrop, 1));

            int fruitCount = min + ThreadLocalRandom.current().nextInt(bound);
            for (int i = 0; i < fruitCount; i++) {
                if (!weightedDrops.isEmpty()) {
                    ItemStack selected = getWeightedRandomItem();
                    if (selected != null) {
                        drops.add(new CustomItemStack(selected, 1));
                    }
                }
            }
        }
    }

    /**
     * 核心算法：根据权重随机获取物品
     * 算法逻辑：生成一个 [1, totalWeight] 的随机数，遍历累加权重，落在哪个区间就返回哪个物品
     */
    private ItemStack getWeightedRandomItem() {
        if (totalWeight <= 0 || weightedDrops.isEmpty()) return null;

        int randomValue = ThreadLocalRandom.current().nextInt(totalWeight) + 1;
        int currentSum = 0;

        for (WeightedDrop drop : weightedDrops) {
            currentSum += drop.weight;
            if (randomValue <= currentSum) {
                return drop.item;
            }
        }
        // 理论上不会运行到这里，除非权重计算有误
        return weightedDrops.get(weightedDrops.size() - 1).item;
    }

    @Override
    public @NotNull List<ItemStack> getDisplayRecipes() {
        List<ItemStack> display = new ArrayList<>();

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("丰收时刻 · 掉落一览➵"),
                getGradientNameVer2("每次成熟后采集随机掉落" + minDisplay + "~" + maxDisplay + "个果实"),
                getGradientNameVer2("植物特性：保底掉落一个本体植物种子"),
                getGradientNameVer2("可使用骨粉对其进行催熟"),
                getGradientNameVer2("不同果实拥有不同的掉落权重")));

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("丰收时刻 · 掉落一览➵"),
                getGradientNameVer2("每次成熟后采集随机掉落" + minDisplay + "~" + maxDisplay + "个果实"),
                getGradientNameVer2("植物特性：保底掉落一个本体植物种子"),
                getGradientNameVer2("可使用骨粉对其进行催熟"),
                getGradientNameVer2("不同果实拥有不同的掉落权重")));

        if (weightedDrops.isEmpty()) {
            return display;
        }

        // 计算并添加每个果实的详细概率
        for (WeightedDrop drop : weightedDrops) {
            if (drop.item != null) {
                ItemStack displayFruit = drop.item.clone();
                ItemMeta meta = displayFruit.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) {
                        lore = new ArrayList<>();
                    }

                    lore.add(getGradientNameVer2("------------------"));

                    // 计算百分比：(权重 / 总权重) * 100
                    double probability = (double) drop.weight / totalWeight * 100.0;
                    String probString;

                    // 格式化：如果是整数则显示整数，否则保留两位小数
                    if (probability == (long) probability) {
                        probString = String.format("%d%%", (long) probability);
                    } else {
                        probString = String.format("%.2f%%", probability);
                    }

                    lore.add(getGradientNameVer2("掉落权重: " + drop.weight));
                    lore.add(getGradientNameVer2("掉落概率: " + probString));

                    meta.setLore(lore);
                    displayFruit.setItemMeta(meta);
                }
                // 为了在 Slimefun 机器界面中正确显示，通常需要一个占位符或者配对项
                // 这里简单起见，直接添加物品，Slimefun 会尝试配对，如果奇数个最后一个可能不显示配对
                // 如果需要严格配对，可以在这里添加一个空气或者重复添加
                display.add(new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " "));
                display.add(displayFruit);
            }
        }
        return display;
    }
}