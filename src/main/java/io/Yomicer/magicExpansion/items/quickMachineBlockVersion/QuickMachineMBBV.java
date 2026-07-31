package io.Yomicer.magicExpansion.items.quickMachineBlockVersion;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.utils.quickMachine.QuickMachineMBUtilsMBVersion.addAvailableRecipesToMenu;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class QuickMachineMBBV extends SlimefunItem implements EnergyNetComponent {


    private final int[] inputInfo = {};
    private final int[] inputSlots = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35};
    private final int[] outputInfo = {};
    private final int[] outputSlots = {35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1};
    private final int[] starslot = {49};
    private final int[] arrowslots = {45,53};
    private final int[] iBorder = {46,47,48,50,51,52};
    private final List<Map<String, Integer>> receivedMBRecipes;

    public QuickMachineMBBV(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, String displayName, List<Map<String, Integer>> receivedMBRecipes) {
        super(category, item, recipeType, recipe);


        this.receivedMBRecipes = receivedMBRecipes;

        constructMenu(ColorGradient.getGradientName(displayName));
        addItemHandler(onBreak());


    }

//    @Override
//    public void preRegister() {
//        addItemHandler(new BlockTicker() {
//
//            @Override
//            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
//                QuickMachineMBBV.this.tick(b);
//            }
//
//            @Override
//            public boolean isSynchronized() {
//                return false;
//            }
//        });
//    }
//    protected void tick(Block block) {
//        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
//
//        QuickMachineMBUtilsMBVersion.addAvailableRecipesToMenu(menu,SMELTERY_RECIPES);

//        craftIfValid(block);
//    }



    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {

            @Override
            public void init() {
                constructMenu(this);

            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                menu.addMenuOpeningHandler((player -> {
                    addAvailableRecipesToMenu(menu, receivedMBRecipes);
                }));
                // 初始化菜单内容
                for (int i : starslot) {
                menu.addItem(i, new CustomItemStack(Material.NETHER_STAR, "§x§F§D§B§7§D§4",
                            "§x§F§D§B§7§D§4§lRefresh Recipes",
                            "§eRight-click: §bView required materials",
                            "§eLeft-click: §bCraft one batch",
                            "§eShift-right-click: §bCraft 32 batches",
                            "§eShift-left-click: §bCraft all possible batches",
                            "§7Slots 1–4 are used for machine input and output."),
                        (player1, slot, item, action) -> {
                            addAvailableRecipesToMenu(menu, receivedMBRecipes);
                            return false;
                        });
                }
                for (int i : arrowslots) {
                    menu.addItem(i, new CustomItemStack(Material.ARROW, "&aClick"),
                            (player, slot, item, action) -> false);
                }

                addAvailableRecipesToMenu(menu, receivedMBRecipes);


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
            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                return getCustomItemTransport(menu, flow, item);
            }
        };
    }


    protected void constructMenu(BlockMenuPreset preset) {
        borders(preset,inputInfo,outputInfo,arrowslots,starslot,iBorder);

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, new ChestMenu.AdvancedMenuClickHandler() {

                @Override
                public boolean onClick(Player p, int slot, ItemStack cursor, ClickAction action) {
                    return false;
                }

                @Override
                public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor,
                                       ClickAction action) {
                    if (cursor == null) return true;
                    return cursor.getType() == Material.AIR;
                }
            });
        }

        for (int inputSlot : getInputSlots()) {
            preset.addMenuClickHandler(inputSlot, new ChestMenu.AdvancedMenuClickHandler() {

                @Override
                public boolean onClick(Player p, int slot, ItemStack cursor, ClickAction action) {
                    return false;
                }
                @Override
                public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor, ClickAction action) {

                        if (e.getAction() != InventoryAction.NOTHING){
                        Block block = ((BlockMenu) e.getInventory().getHolder()).getBlock();
                        if (block != null) {
                            Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> {
                                BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
                                if (menu != null) {
                                    addAvailableRecipesToMenu(menu, receivedMBRecipes);
                                }
                            }, 1L);
                        }
                    }
                    return true;
                }
            });
        }



    }

    static void borders(BlockMenuPreset preset, int[] inputInfo, int[] outputInfo, int[] arrow, int[] star, int[] iBorder) {

//        for (int i : outputInfo) {
//            preset.addItem(i, new CustomItemStack(new ItemStack (Material.YELLOW_STAINED_GLASS_PANE), "§b产物输入槽→","§b产物列表↓"),
//                    (p, slot, item, action) -> false);
//        }


//        for (int i : arrow) {
//            preset.addItem(i, new CustomItemStack(Material.ARROW, "&a"),
//                    (player, slot, item, action) -> false);
//        }

//        for (int i : star) {
//            preset.addItem(i, new CustomItemStack(Material.NETHER_STAR, "§x§F§D§B§7§D§4使用说明",
//                            "§x§F§D§B§7§D§4§l首次打开需要手动放入一个材料来初始化机器",
//                            "§e右键 §b查看单次合成所需的材料",
//                            "§e左键 §b制作一次",
//                            "§b按住 §eShift 右键 §b一次制作32个物品",
//                            "§b按住 §eShift 左键 §b制作所有可制作的物品"),
//                    (player1, slot, item, action) -> false);
//        }

        for (int i : iBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(Material.PINK_STAINED_GLASS_PANE), "&a"),
                    (player, slot, item, action) -> false);
        }

    }

    protected int[] getCustomItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) {
            return getOutputSlots();
        }else{
            return getInputSlots();
        }
    }


    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack i, @Nonnull List<ItemStack> list) {
                Block b = e.getBlock();
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());

                if (inv != null) {
                    inv.dropItems(b.getLocation(), getInputSlots());
                    inv.dropItems(b.getLocation(), getOutputSlots());
                }

            }
        };
    }

    public int[] getInputSlots() {
        return inputSlots;
    }

    public int[] getOutputSlots() {
        return outputSlots;
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
