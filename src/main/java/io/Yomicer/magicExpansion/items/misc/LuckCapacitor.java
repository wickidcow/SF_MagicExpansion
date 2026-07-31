package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.generators.EnergyInputGenerator;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LuckCapacitor extends SlimefunItem implements EnergyNetComponent {

    private final int capacity;
    private Random random = new Random();
    private final int pow1;
    private final int pow2;
    private final int sign;
    public static final NamespacedKey CHARGE_KEY = new NamespacedKey(MagicExpansion.getInstance(), "capacitor_charge");
    private static final Map<Location, Integer> CHARGE_CACHE = new ConcurrentHashMap<>();

    @ParametersAreNonnullByDefault
    public LuckCapacitor(ItemGroup itemGroup, int capacity, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int pow1, int pow2, int sign) {
        super(itemGroup, item, recipeType, recipe);
        this.capacity = capacity;
        this.pow1 = pow1;
        this.pow2 = pow2;
        this.sign = sign;
        addItemHandler(onBlockPlace(), onBlockBreak());
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                LuckCapacitor.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    protected void tick(Block block) {

        int randomCharge;
        if (pow1 == pow2){
            randomCharge = pow1;
        } else {
            randomCharge = random.nextInt(pow1,pow2);
        }
        if (sign != 1 && sign != -1){
            randomCharge = randomCharge * (random.nextBoolean() ? 1 : -1);
        } else {
            randomCharge = randomCharge * sign;
        }
        if (randomCharge>0) {
            addCharge(block.getLocation(), randomCharge);
        } else if (randomCharge < 0) {
            removeCharge(block.getLocation(), -randomCharge);
        }
        CHARGE_CACHE.put(block.getLocation(), getCharge(block.getLocation()));
    }


    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new BlockBreakHandler(false,false) {
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                e.setDropItems(false);
                int charge = CHARGE_CACHE.getOrDefault(e.getBlock().getLocation(), 0);
                ItemStack itemStack = LuckCapacitor.this.getItem();
                ItemStack drop = itemStack.clone();
                ItemMeta meta = drop.getItemMeta();
                if (charge != 0){
                    if (meta != null) {
                        meta.setDisplayName(meta.getDisplayName()+" §r§b(§e⚡ §bEnergy: §e" + charge + " §bJ)");
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(CHARGE_KEY, PersistentDataType.INTEGER, charge);
                        drop.setItemMeta(meta);
                    }
                }
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);
                CHARGE_CACHE.remove(e.getBlock().getLocation());
            }
        };
    }

    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                ItemStack itemInHand = e.getItemInHand();
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta == null) return;
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                Integer storedCharge = pdc.getOrDefault(CHARGE_KEY, PersistentDataType.INTEGER,0);

                if (storedCharge != null && storedCharge != 0) {
                    addCharge(e.getBlockPlaced().getLocation(), storedCharge);
                }
                CHARGE_CACHE.put(e.getBlockPlaced().getLocation(), getCharge(e.getBlockPlaced().getLocation()));
            }
        };
    }


    @Nonnull
    public final EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CAPACITOR;
    }

    public int getCapacity() {
        return this.capacity;
    }


}
