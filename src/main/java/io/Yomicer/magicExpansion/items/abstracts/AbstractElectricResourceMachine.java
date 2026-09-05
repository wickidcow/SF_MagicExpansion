package io.Yomicer.magicExpansion.items.abstracts;

import com.google.common.base.Preconditions;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractElectricResourceMachine extends AbstractMachine implements EnergyNetComponent {

    protected List<MachineRecipe> recipes = new ArrayList<>();

    private int energyConsumedPerTick = -1;
    private int energyCapacity = -1;
    private int processingSpeed = -1;
    private ItemStack[] ItemStackOutputs = new ItemStack[]{new ItemStack(Material.AIR)};
    private int craftSecond = 0;

    protected AbstractElectricResourceMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    protected boolean checkCraftPreconditions(Block b) {
        return takeCharge(b.getLocation());
    }

    @Override
    protected boolean checkCraftPreconditions(Block b, @Nullable SlimefunBlockData data) {
        if (data == null) {
            return checkCraftPreconditions(b);
        }

        return takeCharge(b.getLocation(), data);
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    public int getCapacity() {
        return energyCapacity;
    }

    public int getEnergyConsumption() {
        return energyConsumedPerTick;
    }

    public int getSpeed() {
        return processingSpeed;
    }


    public int getCraftSecond() {
        return craftSecond;
    }

    public ItemStack[] getItemStackOutputs() {
        return ItemStackOutputs;
    }


    public List<MachineRecipe> getRecipes() {
        return recipes;
    }

    public final AbstractElectricResourceMachine setCraftSecond(int craftSecond) {
        Preconditions.checkArgument(craftSecond >= 0, "craftSecond must be greater than or equal zero!");

        this.craftSecond = craftSecond;
        return this;
    }

    public final AbstractElectricResourceMachine setCapacity(int capacity) {
        Preconditions.checkArgument(capacity > 0, "The capacity must be greater than 0");

        this.energyCapacity = capacity;
        return this;
    }

    public final AbstractElectricResourceMachine setItemStackOutputs(ItemStack[] outputs) {
        Preconditions.checkArgument(outputs.length > 0, "Outputs cannot be empty");

        this.ItemStackOutputs = outputs;
        return this;

    }

    public final AbstractElectricResourceMachine setConsumption(int consumption) {
        Preconditions.checkArgument(getCapacity() > 0, "Capacity must be set before consumption");
        Preconditions.checkArgument(consumption < getCapacity() && consumption != 0, "Consumption cannot be greater than capacity");
        this.energyConsumedPerTick = consumption;
        return this;
    }

    public final AbstractElectricResourceMachine setProcessingSpeed(int speed) {
        Preconditions.checkArgument(speed > 0, "Speed must be greater than zero!");

        this.processingSpeed = speed;
        return this;
    }

    public List<MachineRecipe> getMachineRecipes() {
        return recipes;
    }

    public void registerRecipe(MachineRecipe recipe) {
        recipe.setTicks(recipe.getTicks() / getSpeed());
        recipes.add(recipe);
    }

    public void registerRecipe(int seconds, ItemStack[] inputs, ItemStack[] outputs) {
        registerRecipe(new MachineRecipe(seconds, inputs, outputs));
    }

    public void registerRecipe(int seconds, ItemStack input, ItemStack outputs) {
        registerRecipe(seconds, new ItemStack[] { input }, new ItemStack[] { outputs });
    }

    protected boolean takeCharge(Location l) {
        Preconditions.checkNotNull(l, "Can't take energy from a null location");

        if (isChargeable()) {
            long charge = getChargeLong(l);

             if (charge < getEnergyConsumption()) {
                return false;
             }

             setCharge(l, charge - getEnergyConsumption());
        }
        return true;
    }

    /**
     * Reuses the block data that Slimefun already resolved for this ticker execution. Mine Man machines are
     * numerous and tick frequently, so avoiding duplicate storage-cache lookups here removes a large amount of
     * repeated work without changing their energy cost or processing behaviour.
     */
    protected boolean takeCharge(Location l, SlimefunBlockData data) {
        Preconditions.checkNotNull(l, "Can't take energy from a null location");
        Preconditions.checkNotNull(data, "Can't take energy from null block data");

        if (isChargeable()) {
            long charge = getChargeLong(l, data);

            if (charge < getEnergyConsumption()) {
                return false;
            }

            // This class is always an energy CONSUMER, so no capacitor texture update is needed here.
            // Writing through the already-loaded data object avoids another StorageCache lookup and charge read.
            data.setData("energy-charge", String.valueOf(charge - getEnergyConsumption()));
        }
        return true;
    }

    @Nullable
    @Override
	public MachineRecipe findNextRecipe(BlockMenu menu) {
        Map<Integer, ItemStack> inv = new HashMap<>();

        for (int slot : getInputSlots()) {
            ItemStack item = menu.getItemInSlot(slot);

            if (item != null) {
                inv.put(slot, ItemStackWrapper.wrap(item));
            }
        }

        int maxedSlots = 0;
        for (int slot : getOutputSlots()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && item.getAmount() == item.getMaxStackSize()) {
                maxedSlots += 1;
            }
        }

        if (maxedSlots == getOutputSlots().length) { return null; }

        Map<Integer, Integer> found = new HashMap<>();

        for (MachineRecipe recipe : recipes) {
            for (ItemStack input : recipe.getInput()) {
                for (int slot : getInputSlots()) {
                    if (SlimefunUtils.isItemSimilar(inv.get(slot), input, true)) {
                        found.put(slot, input.getAmount());
                        break;
                    }
                }
            }

            if (found.size() == recipe.getInput().length) {
                if(!InvUtils.fitAll(menu.toInventory(), recipe.getOutput(), getOutputSlots())) {
                        return null;
                }

                for (Map.Entry<Integer, Integer> entry : found.entrySet()) {
                    menu.consumeItem(entry.getKey(), entry.getValue());
                }

                return recipe;
            } else {
                found.clear();
            }
        }

        return null;
    }

}
