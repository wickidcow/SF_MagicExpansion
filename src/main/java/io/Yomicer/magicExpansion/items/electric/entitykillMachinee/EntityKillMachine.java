package io.Yomicer.magicExpansion.items.electric.entitykillMachinee;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Comparator;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class EntityKillMachine extends SlimefunItem implements EnergyNetComponent {


    private final int power;
    private final int craftPerTick;
    private final EntityType entityType;
    private final int[] pinkBorder = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26};
    private final int[] blueBorder = {10,11,12, 14,15,16};
    private final String name;

    public EntityKillMachine(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int power, int craftPerTick, EntityType entityType, String name) {
        super(category, item, recipeType, recipe);

        this.power = power;
        this.craftPerTick = craftPerTick;
        this.entityType = entityType;
        this.name = name;

        constructMenu(name+"EntityKillMachine");
    }



    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                EntityKillMachine.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
    }

    protected void tick(Block block) {

        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if(menu != null && menu.hasViewer()) {
        if (getCharge(block.getLocation()) < getEnergyConsumption()) {
            //电量不足
            menu.addItem(13, new CustomItemStack(new ItemStack (Material.GHAST_TEAR), "§cNot Enough Energy"),
                    (p, slot, item, action) -> false);
            return;
        }
            //电量不足
            menu.addItem(13, new CustomItemStack(new ItemStack(Material.BLUE_BED), "§bSuppressing",
                            "§bType: §e" + name,
                            "§bEnergy Use: §e" + getEnergyConsumption() * 2 + " J/s",
                            "§bStored Energy: §e" + getCharge(block.getLocation()) + " J"),
                    (p, slot, item, action) -> false);

        }
        Location center = block.getLocation();
        int radius = 19;

        Runnable removeEntitiesTask = () -> {
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity.getType() == entityType) {
                    entity.remove();
                }
            }
        };

        if (Bukkit.isPrimaryThread()) {
            // 当前是主线程,直接执行
            removeEntitiesTask.run();
        } else {
            // 当前是异步线程,调度到主线程执行
            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), removeEntitiesTask);
        }



    }



    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                return p.hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager().hasPermission(p, b.getLocation(),
                        Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
                return new int[0];
            }
        };
    }

    protected void constructMenu(BlockMenuPreset preset) {

        for (int i : pinkBorder ) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack (Material.PINK_STAINED_GLASS_PANE)), " "),
                    (p, slot, item, action) -> false);
        }

        for (int i : blueBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack (Material.LIGHT_BLUE_CANDLE)), " "),
                    (p, slot, item, action) -> false);
        }

        preset.addItem(13, new CustomItemStack(new ItemStack (Material.GHAST_TEAR), " "),
                (p, slot, item, action) -> false);

    }


    private int getEnergyConsumption() {
        return craftPerTick;
    }


    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return power;
    }
}
