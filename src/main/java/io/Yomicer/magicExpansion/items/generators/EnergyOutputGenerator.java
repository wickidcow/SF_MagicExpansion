package io.Yomicer.magicExpansion.items.generators;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.machineLore.ChargeLore;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.List;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;


public class EnergyOutputGenerator extends MenuBlock implements EnergyNetProvider {


    private static final NamespacedKey MaxPower = new NamespacedKey(MagicExpansion.getInstance(), "max_power");
    private static final NamespacedKey NowPower = new NamespacedKey(MagicExpansion.getInstance(), "now_power");
    private static final int[] INPUT_SLOTS = new int[]{19};
    private static final int[] OUTPUT_SLOTS = new int[]{25};

    public EnergyOutputGenerator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @ParametersAreNonnullByDefault
    protected void onBreak(BlockBreakEvent e, BlockMenu menu) {
        Location l = menu.getLocation();
        menu.dropItems(l, this.getInputSlots());
        menu.dropItems(l, this.getOutputSlots());
        removeHologramSfTimings(e.getBlock());
    }
    @ParametersAreNonnullByDefault
    protected void onPlace(BlockPlaceEvent e, Block b) {
        Location loc = e.getBlock().getLocation().clone().add(EnergyOutputGenerator.this.getHologramOffsetSfTimings(e.getBlock()));
        Location loc1 = loc.clone().add(0, 1.0, 0);
        Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("待机中..."));
    }

    private Vector getHologramOffsetSfTimings(@Nonnull Block block) {
        return Slimefun.getHologramsService().getDefaultOffset();
    }
    private void removeHologramSfTimings(@Nonnull Block b) {
        Location loc = b.getLocation().clone().add(this.getHologramOffsetSfTimings(b));
        Location loc1 = loc.clone().add(0, 1.0, 0);
        Slimefun.getHologramsService().removeHologram(loc1);
        Location loc2 = loc.clone().add(0, 1.5, 0);
        Slimefun.getHologramsService().removeHologram(loc2);
    }

    private void updateHologramSfTimings(@Nonnull Block b,@Nonnull long po1,@Nonnull long po2,@Nonnull int takePower) {
        Location loc = b.getLocation().clone().add(this.getHologramOffsetSfTimings(b));
        Location loc1 = loc.clone().add(0, 1.0, 0);
        Location loc2 = loc.clone().add(0, 1.5, 0);
        Slimefun.getHologramsService().setHologramLabel(loc2, getGradientName("发电效率: " + ChargeLore.formatEnergy(takePower) + " J/s"));
        Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("内部电力存储: ")+"§r§e"+ po1 +"/"+po2+" J");
    }

    @Override
    protected void setup(BlockMenuPreset blockMenuPreset) {
        blockMenuPreset.drawBackground(new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE," "),new int[] {
                0, 1, 2, 3,
                9,10,11,12,
                18,  20,21,
                27,28,29,30,
                36,37,38,39,
        });
        blockMenuPreset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE," "),new int[] {
                5, 6, 7, 8,
                14,15,16,17,
                23,24,  26,
                32,33,34,35,
                41,42,43,44,
        });
        blockMenuPreset.drawBackground(new CustomItemStack(Material.LIME_STAINED_GLASS_PANE," "),new int[] {
                4,
                13,
                22,
                31,
                40,
                45,46,47,48,49,50,51,52,53
        });
    }


    @Override
    protected int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    protected int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    public int getGeneratedOutput(Location l, SlimefunBlockData data) {

        int gen;
        gen = 0;
        Location loc = l.clone().add(EnergyOutputGenerator.this.getHologramOffsetSfTimings(l.getBlock()));
        Location loc1 = loc.clone().add(0, 1.0, 0);
        Location loc2 = loc.clone().add(0, 1.5, 0);

        BlockMenu menu = StorageCacheUtils.getMenu(l);
        if (menu == null) {
            Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("待机中..."));
            Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("不存在菜单..."));
            return gen;
        }
        ItemStack PowerCard = menu.getItemInSlot(19);
        if (PowerCard == null || PowerCard.getType().isAir()) {
            if (menu != null && menu.hasViewer()) {
                menu.replaceExistingItem(22, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("不在工作状态")
                ));
            }
            Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("待机中..."));
            Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("待机中..."));
            return gen;
        }
        if (PowerCard.getAmount() != 1){
            if (menu != null && menu.hasViewer()) {
                menu.replaceExistingItem(22, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("不在工作状态")
                ));
            }
            Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("待机中..."));
            Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("待机中..."));
            return gen;
        }
        ItemMeta meta = PowerCard.getItemMeta();
        if (meta == null) {
            if (menu != null && menu.hasViewer()) {
                menu.replaceExistingItem(22, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("不在工作状态")
                ));
            }
            Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("待机中..."));
            Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("待机中..."));
            return gen;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(MaxPower, PersistentDataType.LONG) ||
                !pdc.has(NowPower, PersistentDataType.LONG)) {
            if (menu != null && menu.hasViewer()) {
                menu.replaceExistingItem(22, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("不在工作状态")
                ));
            }
            Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("待机中..."));
            Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("待机中..."));
            return gen;
        }

        long nowPower = pdc.get(NowPower, PersistentDataType.LONG);
        long maxPower = pdc.get(MaxPower, PersistentDataType.LONG);

        int machineCharge = getCharge(l);
        int machineMaxCharge = getCapacity();

        long newNowPower;
        if (nowPower <= 0) {
            if(null == menu.getItemInSlot(25)) {
                menu.replaceExistingItem(19, null);
                menu.replaceExistingItem(25, PowerCard);
            }else{
            }
        }
        long availablePower = nowPower;
        int spaceLeftInMachine = machineMaxCharge - machineCharge;

        if (spaceLeftInMachine <= 0) {
            newNowPower = nowPower;
        } else if (availablePower > spaceLeftInMachine) {
            gen = spaceLeftInMachine;
            newNowPower = nowPower - gen;
        } else {
            // K 修复：先钳制到 int 上限再强转，且 gen 不会超过机器剩余容量 spaceLeftInMachine
            gen = (int) Math.min(Math.min(availablePower, Integer.MAX_VALUE), (long) spaceLeftInMachine);
            newNowPower = nowPower - gen;
        }

        int num = 3;
        if(SlimefunUtils.isItemSimilar(PowerCard, MagicExpansionItems.POWER_CARD, false,true,true)){
            num = 3;
        } else if (SlimefunUtils.isItemSimilar(PowerCard, MagicExpansionItems.FISH_LEGENDARY_EEL_POWER, false,true,true)){
            num = 16;
        }


        // K 修复：每 tick 状态无变化时不重写 PDC 与 lore（对比后再写），减少持久化与 NBT 写入开销
        if (newNowPower != nowPower) {
            pdc.set(NowPower, PersistentDataType.LONG, newNowPower);
            updatePowerCardLore(meta, newNowPower, maxPower, num);
            PowerCard.setItemMeta(meta);
        }

        if (menu != null && menu.hasViewer()) {
            if(gen <= 0){
                menu.replaceExistingItem(22, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("不在工作状态")
                ));
                Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("中转站工作已完成..."));
                Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("中转站工作已完成..."));
            }
            else {
                menu.replaceExistingItem(22, new CustomItemStack(
                        Material.SOUL_LANTERN,
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("正在发电中: " + ChargeLore.formatEnergy(gen) + " J/s"),
                        getGradientName("电量剩余: " + newNowPower + "/" + maxPower + " J")
                ));
                updateHologramSfTimings(l.getBlock(), newNowPower, maxPower, gen);
            }
        }




        return gen;
    }


    private void updatePowerCardLore(ItemMeta meta, long nowPower, long maxPower, int num) {
        List<String> lore = new ArrayList<>();

        // 保留原有 Lore 前11行（参考你之前的逻辑）
        if (meta.hasLore()) {
            List<String> existingLore = meta.getLore();
            int linesToKeep = Math.min(num, existingLore.size());
            for (int i = 0; i < linesToKeep; i++) {
                lore.add(existingLore.get(i));
            }
        }

        // 添加电量行
        String powerLine = ColorGradient.getGradientName("当前储电量: " + nowPower + "/" + maxPower + " J");
        lore.add(powerLine);

        meta.setLore(lore);
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }



    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.GENERATOR;
    }

}
