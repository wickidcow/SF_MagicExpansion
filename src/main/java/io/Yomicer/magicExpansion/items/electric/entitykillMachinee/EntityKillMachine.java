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

        constructMenu(name+"抑制器");
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

    // F2: 实体扫描节流计数器——每 20 tick 才执行一次 getNearbyEntities(19,19,19) 清除逻辑
    private int scanTickCounter = 0;

    protected void tick(Block block) {

        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());

        // F3: 同一 tick 内的 getCharge 调用合并为一次局部变量，避免重复查询
        int charge = getCharge(block.getLocation());

        // F1: 电量检查移到 hasViewer() 判断之外——电量不足时无论是否有观察者都不清除实体，
        // 防止"没人看 UI 就免费清除实体"的漏洞；仅在有观察者时更新 UI 提示
        if (charge < getEnergyConsumption()) {
            if (menu != null && menu.hasViewer()) {
                menu.addItem(13, new CustomItemStack(new ItemStack (Material.GHAST_TEAR), "§c电量不足"),
                        (p, slot, item, action) -> false);
            }
            return;
        }

        // UI 状态提示仍每 tick 更新（仅有观察者时），复用 F3 的局部变量 charge
        if (menu != null && menu.hasViewer()) {
            menu.addItem(13, new CustomItemStack(new ItemStack(Material.BLUE_BED), "§b抑制中",
                            "§b类型：§e" + name,
                            "§b耗电速度：§e" + getEnergyConsumption() * 2 + " J/s",
                            "§b电量存储：§e" + charge + " J"),
                    (p, slot, item, action) -> false);

        }

        // F2: 实体扫描节流——每 20 tick 才执行一次清除逻辑，UI 更新不受影响
        scanTickCounter++;
        if (scanTickCounter < 20) {
            return;
        }
        scanTickCounter = 0;

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
            // 当前是主线程，直接执行
            removeEntitiesTask.run();
        } else {
            // 当前是异步线程，调度到主线程执行
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
