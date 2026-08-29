package io.Yomicer.magicExpansion.items.generators;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.utils.machineLore.ChargeLore;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;


public class NumberEnergyGenerator extends MenuBlock implements EnergyNetProvider {


    private final int Capacity;
    private final int power;
    private final int power2;
    private final Material material;
    private final String key;
    private final String key2;

    public NumberEnergyGenerator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                                 Material material, int Capacity, int power, int power2, String key,String key2) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
        this.power = Math.min(power,power2);
        this.power2 = Math.max(power2,power);
        this.material = material;
        this.key = key;
        this.key2 = key2;
    }

    public NumberEnergyGenerator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                                 Material material, int Capacity, int power, int power2, String key) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
        this.power = Math.min(power,power2);
        this.power2 = Math.max(power2,power);
        this.material = material;
        this.key = key;
        this.key2 = null;
    }

    public NumberEnergyGenerator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                                 Material material, int Capacity, int power, String key) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
        this.power = power;
        power2 = 0;
        this.material = material;
        this.key = key;
        this.key2 = null;
    }

    @Override
    protected void setup(BlockMenuPreset blockMenuPreset) {
        blockMenuPreset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE," "),new int[] {

                10,11,12,13,14,15,16,
                18,19,20,21,22,23,24,25,26
        });
        blockMenuPreset.drawBackground(new CustomItemStack(Material.MAGENTA_GLAZED_TERRACOTTA,getGradientName("§b↑请在上方填入密钥↑"),
                getGradientName("§b密钥为9位二进制码"),getGradientName("§e有物品代表1"),getGradientName("§d无物品代表0")),new int[] {

                9,   17
        });

    }

    @Nonnull
    @Override
    protected int[] getInputSlots(DirtyChestMenu dirtyChestMenu, ItemStack itemStack) {
        return new int[]{0,1,2,3,4,5,6,7,8};
    }

    @Override
    protected int[] getInputSlots() {
        return new int[]{0,1,2,3,4,5,6,7,8};
    }

    @Override
    protected int[] getOutputSlots() {
        return new int[]{0,1,2,3,4,5,6,7,8};
    }

    @Override
    public int getGeneratedOutput(Location l, SlimefunBlockData data) {

        BlockMenu inv = StorageCacheUtils.getMenu(l);
        // H 修复：区块加载时序窗口内 inv 可能为 null，立即按发电量 0 返回，避免下方 getItemInSlot 抛 NPE
        if (inv == null) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 8; i++) {
            if (inv.getItemInSlot(i) != null && inv.getItemInSlot(i).getType() != Material.AIR) {
                sb.append('1');
            } else {
                sb.append('0');
            }
        }

        // 比较当前槽位状态和预设模式
        String currentState = sb.toString();
        int ifKey2 = 0;
        int gen;
        if(currentState.equals(key2)){
            gen = (int) (((power2+power))*0.35);
            ifKey2 = 1;
        }
        else if (!currentState.equals(key)) {
            gen = 0;
        }else {
            if (power2 > 0) {
                double r = Math.random();
                double genDouble = power + (power2 - power) * Math.pow(r, 3);
                gen = (int) genDouble;
//                gen = new Random().nextInt(power, power2);
            } else {
                gen = power;
            }
        }
        if (inv != null && inv.hasViewer()) {
            if (gen == 0) {
                inv.replaceExistingItem(13, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("口令错误，不允许发射"),
                        getGradientName("已储存: " + ChargeLore.format(getCharge(l)) + " J")
                ));
            }
            else if(ifKey2 == 1&&power2 > 0){
                inv.replaceExistingItem(13, new CustomItemStack(
                        Material.SOUL_LANTERN,
                        getGradientName("密钥正确，允许发射，本期密钥之一为:"+key2),
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("发电速度: " + ChargeLore.formatEnergy(gen) + " J/s "),
                        getGradientName("已储存: " + ChargeLore.format(getCharge(l)) + " J")
                ));
            }else{
                inv.replaceExistingItem(13, new CustomItemStack(
                        Material.SOUL_LANTERN,
                        getGradientName("密钥正确，允许发射，本期密钥之一为:"+key),
                        getGradientName("类型: " + getPowerType()),
                        getGradientName("功率波动: " + ChargeLore.formatEnergy(Math.min(power,power2)) + " ~ " + ChargeLore.formatEnergy(Math.max(power,power2)) +" J/s "),
                        getGradientName("发电速度: " + ChargeLore.formatEnergy(gen) + " J/s "),
                        getGradientName("已储存: " + ChargeLore.format(getCharge(l)) + " J")
                        ));
            }
        }

        return gen;
    }

    @Override
    public int getCapacity() {
        return this.Capacity;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.GENERATOR;
    }

}
