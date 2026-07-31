package io.Yomicer.magicExpansion.items.skyBlock;

import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
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
import java.util.*;

import static io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils.isItemSimilar;

public class SingleCubeOre extends SlimefunItem implements HologramOwner{


    private final Random random = new Random();

    private final List<Material> oreMaterials;




    public SingleCubeOre(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(onBlockPlace(), onBlockBreak());
        this.oreMaterials = loadOreMaterials();
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

                updateHologram(e.getBlock(), ColorGradient.getGradientName( "Single Block: Ore Vein Form: "+ displayName));
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

                Material newMaterial = getRandomOreBlock();
                block.setType(newMaterial);

//                ItemStack blockItemStack = new ItemStack(block.getType());
//                String displayName = ItemStackHelper.getDisplayName(blockItemStack);
                String displayName = ItemStackHelper.getDisplayName(new ItemStack(newMaterial));
                updateHologram(block, ColorGradient.getGradientName("Single Block: Ore Vein Form: " + displayName));
                }else{
                removeHologram(block);
                }

            }

        };
    }

    // 获取随机矿石方块(直接从缓存中取)
    public Material getRandomOreBlock() {
        return oreMaterials.get(random.nextInt(oreMaterials.size()));
    }

    private List<Material> loadOreMaterials() {
        List<Material> ores = new ArrayList<>();

        for (Material material : Material.values()) {
            if (material != null && material.isBlock() && material.name().endsWith("_ORE")) {
                ores.add(material);
            }
        }
        // 添加额外的基础方块
        ores.add(Material.STONE);
        ores.add(Material.COBBLESTONE);
        ores.add(Material.DIRT);
        ores.add(Material.DEEPSLATE);
        ores.add(Material.COBBLED_DEEPSLATE);
        ores.add(Material.ANCIENT_DEBRIS);

        return ores;
    }



    @Override
    public void updateHologram(@NotNull Block block, @NotNull String text) {
        HologramOwner.super.updateHologram(block, text);
    }



}
