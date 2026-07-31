package io.Yomicer.magicExpansion.items.skyBlock;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils.isItemSimilar;

public class SingleCubeOrigin extends SlimefunItem implements HologramOwner{


    private final Random random = new Random();

    public SingleCubeOrigin(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(onBlockPlace(), onBlockBreak());
    }


    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                // 根据 Material 创建 ItemStack
                ItemStack blockItemStack = new ItemStack(e.getBlock().getType());
                // 使用 ItemStackHelper 获取显示名称
                String displayName = ItemStackHelper.getDisplayName(blockItemStack);

                updateHologram(e.getBlock(), ColorGradient.getGradientName( "Single Block: Origin Form: "+ displayName));
            }
        };
    }

    @Nonnull
    protected BlockBreakHandler onBlockBreak() {

        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@NotNull Block block) {
            }

            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                Block block = e.getBlock();
                Player player = e.getPlayer();
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();

                SlimefunItem tool1 = SlimefunItem.getById("MAGIC_EXPANSION_SINGLE_DIAMOND_PICKAXE");
                ItemStack targetItem1 = tool1 != null ? tool1.getItem().clone() : null;
                SlimefunItem tool2 = SlimefunItem.getById("MAGIC_EXPANSION_SINGLE_DIAMOND_AXE");
                ItemStack targetItem2 = tool2 != null ? tool2.getItem().clone() : null;
                SlimefunItem tool3 = SlimefunItem.getById("MAGIC_EXPANSION_SINGLE_DIAMOND_SHOVEL");
                ItemStack targetItem3 = tool3 != null ? tool3.getItem().clone() : null;
                SlimefunItem tool4 = SlimefunItem.getById("MAGIC_EXPANSION_SINGLE_DIAMOND_HOE");
                ItemStack targetItem4 = tool4 != null ? tool4.getItem().clone() : null;

                if(isItemSimilar(mainHandItem, targetItem1, false)||
                        isItemSimilar(mainHandItem, targetItem2, false)||
                        isItemSimilar(mainHandItem, targetItem3, false)||
                        isItemSimilar(mainHandItem, targetItem4, false)){


                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
                e.setCancelled(true);

                Material newMaterial = getRandomValidBlock();
                block.setType(newMaterial);

//                ItemStack blockItemStack = new ItemStack(block.getType());
//                String displayName = ItemStackHelper.getDisplayName(blockItemStack);
                String displayName = ItemStackHelper.getDisplayName(new ItemStack(newMaterial));
                updateHologram(block, ColorGradient.getGradientName("Single Block: Origin Form: " + displayName));
                }else{
                removeHologram(block);
                }

            }

        };
    }

    // 获取一个合法的、可以自然生成的完整方块
    public Material getRandomValidBlock() {
        Material material;
        do {
            material = Material.values()[random.nextInt(Material.values().length)];
        } while (isInvalidBlock(material));
        return material;
    }

    // 判断是否为无效方块(排除不可放置、非完整碰撞箱、特殊功能方块等)
    public boolean isInvalidBlock(Material material) {
        if (material == null || material == Material.AIR) {
            return true;
        }

        // 基础排除条件
        if (!material.isBlock() || !material.isSolid() || !material.isItem()) {
            return true;
        }

        // 黑名单:排除特殊功能方块、非完整方块、流体等
        Set<Material> blockedMaterials = new HashSet<>(Set.of(
                Material.BEDROCK,
                Material.BARRIER,
                Material.COMMAND_BLOCK,
                Material.REPEATING_COMMAND_BLOCK,
                Material.CHAIN_COMMAND_BLOCK,
                Material.STRUCTURE_BLOCK,
                Material.JIGSAW,
                Material.SPAWNER,
                Material.END_PORTAL_FRAME,
                Material.END_PORTAL,
                Material.NETHER_PORTAL,
                Material.WATER,
                Material.LAVA,
                Material.FIRE,
                Material.CAVE_AIR,
                Material.VOID_AIR,
                Material.LIGHT,
                Material.POWDER_SNOW_CAULDRON,  // ✅ 新增排除项
                Material.SCULK_SENSOR,          // 可选:排除侦测器
                Material.MOVING_PISTON,         // 排除移动中的活塞
                Material.BUBBLE_COLUMN          // 排除气泡柱
        ));

        if (blockedMaterials.contains(material)) {
            return true;
        }

        // 排除门、栅栏、墙等非完整碰撞箱方块
        if (material.name().contains("DOOR") ||
                material.name().contains("FENCE") ||
                material.name().contains("WALL") ||
                material.name().contains("GATE")||
                material.name().contains("DRAGON")||
                material.name().contains("PORTAL")) {
            return true;
        }

        // 排除楼梯、台阶等非完整方块
        BlockData data = material.createBlockData();
        if (data instanceof Slab || data instanceof Stairs) {
            return true;
        }

        // 排除植物类、地毯、装饰性小物品
        if (material.isBurnable() ||
                material.toString().contains("CARPET") ||
                material.toString().contains("FLOWER") ||
                material.toString().contains("PLANT") ||
                material.toString().contains("LEAVES")||
                material.toString().contains("POTTED")||
                material.toString().contains("SPAWNER")) {
            return true;
        }

        return false;
    }



    @Override
    public void updateHologram(@NotNull Block block, @NotNull String text) {
        HologramOwner.super.updateHologram(block, text);
    }



}
