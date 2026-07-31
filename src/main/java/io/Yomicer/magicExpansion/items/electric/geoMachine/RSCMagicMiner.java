package io.Yomicer.magicExpansion.items.electric.geoMachine;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunChunkData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.*;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class RSCMagicMiner extends AContainer implements RecipeDisplayItem , HologramOwner{

    private static final String HOLOGRAM_ENABLED_KEY = "hologram_enabled";
    private static final ItemStack TOGGLE_ON = new CustomItemStack(Material.GREEN_DYE, "&aEnabled", "&7Click to disable");
    private static final ItemStack TOGGLE_OFF = new CustomItemStack(Material.GRAY_DYE, "&cDisabled", "&7Click to enable");

    private static final int[] INPUT_SLOTS = new int[] { 0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30, 36,37,38,39, 45,46,47,48  };
    private static final int[] OUTPUT_SLOTS = new int[] { 5,6,7,8, 14,15,16,17, 23,24,25,26, 32,33,34,35, 41,42,43,44, 50,51,52,53 };

    private static final int[] INPUT_BORDER_SLOTS = new int[] { 13 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 40 };
    private static final int[] BACKGROUND_SLOTS = new int[] { 49 };

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientName("Information"), getGradientName("type:"), getGradientName("Addon: MagicExpansion"));

    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this);


    private final GEOResource redstone;
    private final GEOResource dust;
    private final GEOResource soul;

    private final ItemStack redstone_input = new ItemStack(Material.REDSTONE);
    private final ItemStack dust_input = new ItemStack(Material.GUNPOWDER);
    private final ItemStack soul_input = new ItemStack(Material.SOUL_SAND);


    @ParametersAreNonnullByDefault
    public RSCMagicMiner(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        processor.setProgressBar(getProgressBar());

//        redstone = Slimefun.getRegistry()
//                .getGEOResources()
//                .get(new NamespacedKey("rykenslimefuncustomizer", "magic_redstone"))
//                .orElse(null);
//        dust = Slimefun.getRegistry()
//                .getGEOResources()
//                .get(new NamespacedKey("rykenslimefuncustomizer", "magic_cosmic_dust"))
//                .orElse(null);
//        soul = Slimefun.getRegistry()
//                .getGEOResources()
//                .get(new NamespacedKey("rykenslimefuncustomizer", "magic_soul"))
//                .orElse(null);

        redstone = Slimefun.getRegistry()
                .getGEOResources()
                .get(new NamespacedKey(MagicExpansion.getInstance(), "magic_expansion_rsc_magic_redstone"))
                .orElse(null);
        dust = Slimefun.getRegistry()
                .getGEOResources()
                .get(new NamespacedKey(MagicExpansion.getInstance(), "magic_expansion_rsc_magic_cosmic_dust"))
                .orElse(null);
        soul = Slimefun.getRegistry()
                .getGEOResources()
                .get(new NamespacedKey(MagicExpansion.getInstance(), "magic_expansion_rsc_magic_soul"))
                .orElse(null);

        addItemHandler(onBlockPlace(), onBlockBreak());


        new BlockMenuPreset(getId(), getInventoryTitle()) {

            @Override
            public void init() {
                setupMenu(this);
            }

            @Override
            public boolean canOpen(Block b, Player p) {
                if (!(p.hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager()
                        .hasPermission(p, b.getLocation(), Interaction.INTERACT_BLOCK))) {
                    return false;
                }

                return true;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return getInputSlots();
                } else {
                    return getOutputSlots();
                }
            }
        };
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                RSCMagicMiner.this.tick(b);
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
                SlimefunBlockData data = StorageCacheUtils.getBlock(e.getBlock().getLocation());
                if (data != null) {
                    data.setData(HOLOGRAM_ENABLED_KEY, "true"); // 显式初始化
                }
                updateHologram(e.getBlock(), "&7Idle...");
            }
        };
    }

    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            public void onBlockBreak(Block b) {
                removeHologram(b);
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
                if (inv != null) {
                    inv.dropItems(b.getLocation(), getInputSlots());
                    inv.dropItems(b.getLocation(), getOutputSlots());
                }

                processor.endOperation(b);
            }
        };
    }


    protected void tick(Block b) {

        Slimefun.getDatabaseManager()
                .getBlockDataController()
                .getChunkDataAsync(b.getChunk(), new IAsyncReadCallback<>() {
                    @Override
                    public void onResult(SlimefunChunkData result) {
                        if (result.getAllData().isEmpty()) {
                            updateHologram(b, "&4requires!");
                        }else{
                            startTick(b);
                        }
                    }
                });
    }

    private void startTick(Block b){
        BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
        CraftingOperation currentOperation = processor.getOperation(b);

        if (currentOperation != null) {
            if (takeCharge(b.getLocation())) {

                if (!currentOperation.isFinished()) {
                    processor.updateProgressBar(inv, 31, currentOperation);
                    currentOperation.addProgress(1);
                } else {
                    inv.replaceExistingItem(31, PROGRESS_ITEM);

                    for (ItemStack output : currentOperation.getResults()) {
                        inv.pushItem(output.clone(), getOutputSlots());
                    }

                    processor.endOperation(b);
                }
            }
        }else {
            MachineRecipe next = findNextRecipe(inv);

            if (next != null) {
                currentOperation = new CraftingOperation(next);
                processor.startOperation(b, currentOperation);

                // Fixes #3534 - Update indicator immediately
                processor.updateProgressBar(inv, 31, currentOperation);
            }
        }
    }

    @Override
    protected MachineRecipe findNextRecipe(BlockMenu inv) {

        MachineRecipe recipe;

        recipe = tryProcessEarthLikeRecipe(inv, redstone_input, RSC_MAGIC_REDSTONE, redstone, 5);
        if (recipe != null) {
            return recipe;
        }
        recipe = tryProcessEarthLikeRecipe(inv, soul_input, RSC_MAGIC_SOUL, soul, 5);
        if (recipe != null) {
            return recipe;
        }
        recipe = tryProcessEarthLikeRecipe(inv, dust_input, RSC_MAGIC_COSMIC_DUST, dust, 5);
        return recipe;

    }


    private MachineRecipe tryProcessEarthLikeRecipe(BlockMenu inv, ItemStack inputItem, SlimefunItemStack outputItem, GEOResource resourceType, int seconds) {
        Block b = inv.getBlock();

        // 检查输出槽是否能容纳结果
        if (!inv.fits(outputItem, getOutputSlots())) {
            return null;
        }

        for (int slot : getInputSlots()) {
            ItemStack itemInSlot = inv.getItemInSlot(slot);

            // 检查输入槽是否有匹配的物品
            if (SlimefunUtils.isItemSimilar(itemInSlot, inputItem, true, false)) {
                OptionalInt supplies = Slimefun.getGPSNetwork()
                        .getResourceManager()
                        .getSupplies(resourceType, b.getWorld(), b.getX() >> 4, b.getZ() >> 4);


                if (supplies.isPresent() && supplies.getAsInt() > 0) {
                    // 创建配方并消耗输入物品
                    MachineRecipe recipe = new MachineRecipe(seconds, new ItemStack[] {inputItem}, new ItemStack[] {outputItem});
                    inv.consumeItem(slot);

                    // 扣除资源
                    Slimefun.getGPSNetwork()
                            .getResourceManager()
                            .setSupplies(resourceType, b.getWorld(), b.getX() >> 4, b.getZ() >> 4, supplies.getAsInt() - 1);
                    this.updateHologram(b, "&7: &r" + resourceType.getName());

                    return recipe;
                } else {
                    // 资源不足,把空桶之类的东西移到输出槽,避免重复尝试(防卡顿)
                    ItemStack item = itemInSlot.clone();
                    inv.replaceExistingItem(slot, null);
                    inv.pushItem(item, getOutputSlots());
                    this.updateHologram(b, resourceType.getName()+ "&7 Mining Complete");
                    return null;
                }
            }

        }

        return null; // 没有匹配的物品或资源不足
    }

    @Override
    public void updateHologram(@NotNull Block block, @NotNull String text) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());
        if (data != null && !isHologramEnabled(data)) {
            return;
        }
        HologramOwner.super.updateHologram(block, text);
    }


    protected void setupMenu(BlockMenuPreset preset) {

        preset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, " "), BACKGROUND_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " "), INPUT_BORDER_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, " "), OUTPUT_BORDER_SLOTS);

        preset.addItem(getProgressSlot(), new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(22, PROGRESS_STACK, ChestMenuUtils.getEmptyClickHandler());

        // === 新增:开关按钮在 slot 4 ===
        preset.addItem(4, TOGGLE_ON,
                new ChestMenu.AdvancedMenuClickHandler() {
                    @Override
                    public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor, ClickAction action) {
                        if (!(e.getInventory().getHolder() instanceof BlockMenu menu)) return false;
                        Block b = menu.getBlock();
                        SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
                        if (data == null) return false;

                        boolean enabled = "true".equals(data.getData(HOLOGRAM_ENABLED_KEY));
                        boolean now = !enabled;
                        data.setData(HOLOGRAM_ENABLED_KEY, String.valueOf(now));

                        // 更新图标
                        menu.replaceExistingItem(4,
                                now ? TOGGLE_ON
                                        : TOGGLE_OFF
                        );

                        // 控制全息显示
                        if (now) {
                        } else {
                            removeHologram(b);
                        }

                        return false;
                    }

                    @Override
                    public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
                        return false;
                    }
                }
        );

        for (int slot : getOutputSlots()) {
            preset.addMenuClickHandler(slot,new ChestMenu.AdvancedMenuClickHandler() {
                @Override
                public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor, ClickAction action) {
                    return cursor.getType().isAir();
                }

                @Override
                public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
                    return false;
                }
            });
        }
    }

    private boolean isHologramEnabled(SlimefunBlockData data) {
        String value = data.getData(HOLOGRAM_ENABLED_KEY);
        return value == null || Boolean.parseBoolean(value); // 默认开启
    }


    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> display = new ArrayList<>();
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Input Materials ⇩")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Output (Mining Time: 5 seconds) ⇨")));
        display.add(redstone_input);
        display.add(RSC_MAGIC_REDSTONE);
        display.add(soul_input);
        display.add(RSC_MAGIC_SOUL);
        display.add(dust_input);
        display.add(RSC_MAGIC_COSMIC_DUST);





        return display;
    }



    protected int getProgressSlot() {
        return 31;
    }

    @Override
    public int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }


    @Override
    public ItemStack getProgressBar() {
        return PROGRESS_ITEM;
    }


    @Override
    public @NotNull String getMachineIdentifier() {
        return "MAGIC_EXPANSION_FIVE_ELEMENT_MINER";
    }
}
