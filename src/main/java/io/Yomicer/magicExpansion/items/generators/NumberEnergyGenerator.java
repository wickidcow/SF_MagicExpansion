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
        blockMenuPreset.drawBackground(new CustomItemStack(Material.MAGENTA_GLAZED_TERRACOTTA,getGradientName("§b↑ Place the number item above ↑"),
                getGradientName("§bFilled slot = 1"),getGradientName("§eEmpty slot = 0"),getGradientName("§dMatch the required 9-bit pattern")),new int[] {

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
                        getGradientName("Invalid number pattern"),
                        getGradientName("Stored: " + ChargeLore.format(getCharge(l)) + " J")
                ));
            }
            else if(ifKey2 == 1&&power2 > 0){
                inv.replaceExistingItem(13, new CustomItemStack(
                        Material.SOUL_LANTERN,
                        getGradientName("Valid key. Launch authorized. One current key is: "+key2),
                        getGradientName("Type: " + getPowerType()),
                        getGradientName("Generation Rate: " + ChargeLore.formatEnergy(gen) + " J/s "),
                        getGradientName("Stored: " + ChargeLore.format(getCharge(l)) + " J")
                ));
            }else{
                inv.replaceExistingItem(13, new CustomItemStack(
                        Material.SOUL_LANTERN,
                        getGradientName("Valid key. Launch authorized. One current key is: "+key),
                        getGradientName("Type: " + getPowerType()),
                        getGradientName("Power Fluctuation: " + ChargeLore.formatEnergy(Math.min(power,power2)) + " ~ " + ChargeLore.formatEnergy(Math.max(power,power2)) +" J/s "),
                        getGradientName("Generation Rate: " + ChargeLore.formatEnergy(gen) + " J/s "),
                        getGradientName("Stored: " + ChargeLore.format(getCharge(l)) + " J")
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
