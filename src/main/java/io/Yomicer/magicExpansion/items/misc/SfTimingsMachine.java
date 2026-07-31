package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class SfTimingsMachine extends SlimefunItem implements EnergyNetComponent{


    private final int[] pinkBorder = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26};
    private final int[] blueBorder = {10,11,12, 14,15,16};

    public SfTimingsMachine(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        constructMenu("SfTimingsMachine");
        addItemHandler(onBlockPlace(), onBlockBreak());
    }



    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                SfTimingsMachine.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                Location loc = e.getBlock().getLocation().clone().add(SfTimingsMachine.this.getHologramOffsetSfTimings(e.getBlock()));
                Location loc1 = loc.clone().add(0, 1.0, 0);
                Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("Idle..."));
            }
        };
    }
    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            public void onBlockBreak(Block b) {
                removeHologramSfTimings(b);

            }
        };
    }

    protected void tick(Block block) {

        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        updateHologramSfTimings(block);
        if(menu != null && menu.hasViewer()) {


            menu.addItem(13, new CustomItemStack(new ItemStack (Material.GHAST_TEAR), "§a※※※ Performance Analyzer ※※※",
                            "&6Tick: &e" + Slimefun.getProfiler().getTime(),
                    "&dcurrentTick: &e" + Slimefun.getProfiler().getTime(block.getChunk())
                    ),
                    (p, slot, item, action) -> false);

        }

    }
    private Vector getHologramOffsetSfTimings(@Nonnull Block block) {
        return Slimefun.getHologramsService().getDefaultOffset();
    }

    private void updateHologramSfTimings(@Nonnull Block b) {
        Location loc = b.getLocation().add(this.getHologramOffsetSfTimings(b));
        Location loc1 = loc.clone().add(0, 1.0, 0);
        Slimefun.getHologramsService().setHologramLabel(loc1, ColorGradient.getGradientName("currentTick:") +"§r§e"+Slimefun.getProfiler().getTime(b.getChunk()));
        Location loc2 = loc.clone().add(0, 1.5, 0);
        Slimefun.getHologramsService().setHologramLabel(loc2, ColorGradient.getGradientName("Tick:")+"§r§e"+ Slimefun.getProfiler().getTime());
    }
    private void removeHologramSfTimings(@Nonnull Block b) {
        Location loc = b.getLocation().add(this.getHologramOffsetSfTimings(b));
        Location loc1 = loc.clone().add(0, 1.0, 0);
        Slimefun.getHologramsService().removeHologram(loc1);
        Location loc2 = loc.clone().add(0, 1.5, 0);
        Slimefun.getHologramsService().removeHologram(loc2);
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

    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.NONE;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

}
