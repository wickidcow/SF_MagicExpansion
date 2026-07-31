package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MagicGeoResourceDefault extends UnplaceableBlock implements GEOResource {

    private String name = "MagicGeoResourceDefault";
    private boolean isObtainableFromGEOMiner = true;
    private int supply = 0;
    private int MaxDeviation = 0;

    public MagicGeoResourceDefault(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                                   String name, Boolean isObtainableFromGEOMiner, int supply, int MaxDeviation){
        super(itemGroup, item, recipeType, recipe);
        this.name = name;
        this.isObtainableFromGEOMiner = isObtainableFromGEOMiner;
        this.supply = supply;
        this.MaxDeviation = MaxDeviation;
        register();
    }

    @Override
    public int getDefaultSupply(@NotNull World.Environment environment, @NotNull Biome biome) {
        return supply;
    }

    @Override
    public int getMaxDeviation() {
        return MaxDeviation;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean isObtainableFromGEOMiner() {
        return isObtainableFromGEOMiner;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(MagicExpansion.getInstance(), getId());
    }

}
