package io.Yomicer.magicExpansion.items.electric.recipeMachine;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricRecipeMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN_LITE;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;

public class OriginMaterialGenMakerLite extends AbstractElectricRecipeMachine {

    private static final int[] INPUT_SLOTS = new int[] { 0,1,2, 9,10,11,12, 18,19,20,21, 27,28,29,30, 36,37,38,39, 45,46,47,48  };
    private static final int[] OUTPUT_SLOTS = new int[] { 6,7,8, 14,15,16,17, 23,24,25,26, 32,33,34,35, 41,42,43,44, 50,51,52,53 };

    private static final int[] INPUT_BORDER_SLOTS = new int[] { 13 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 40 };

    private static final int[] ORIGIN_MATERIAL_BORDER_SLOTS = new int[] { 3, 5 };
    private static final int ORIGIN_MATERIAL_SLOTS = 4;

    private static final int[] BACKGROUND_SLOTS = new int[] { 49 };

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientName("Information"), getGradientName("type:"), getGradientName("Addon: MagicExpansion"));

    public OriginMaterialGenMakerLite(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }


    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> display = new ArrayList<>();
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Input Materials ⇩"),getGradientName("recipe")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Output Items ⇩"),getGradientName("recipe")));
        // 遍历所有配方并动态生成展示内容
        for (MachineRecipe recipe : recipes) {
            ItemStack[] inputs = recipe.getInput();
            ItemStack[] outputs = recipe.getOutput();
            int productionTime = recipe.getTicks()/2;

            int maxItems = Math.max(inputs.length, outputs.length);
            for (int i = 0; i < maxItems; i++) {
                if (i < inputs.length) {
                    display.add(addLore(inputs[i], "§7Processing Time: §e" + productionTime + " seconds"));
                } else {
                    display.add(new ItemStack(Material.AIR));
                }

                if (i < outputs.length) {
                    display.add(addLore(outputs[i], "§7Processing Time: §e" + productionTime + " seconds"));
                } else {
                    display.add(new ItemStack(Material.AIR));
                }
            }
            display.add(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, getGradientName("Recipe"),getGradientName("Input Materials")));
            display.add(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, getGradientName("Recipe"),getGradientName("Output Items")));
        }
        return display;
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

            ItemStack item = menu.getItemInSlot(ORIGIN_MATERIAL_SLOTS);
            if (item == null) {
                return null;
            }

            // [核心修改]调用生成 Lite 版本的方法
            ItemStack[] outputs = new ItemStack[]{OriginMaterialGenLite(item)};

            if (found.size() == recipe.getInput().length) {
                if(!InvUtils.fitAll(menu.toInventory(), outputs, getOutputSlots())) {
                    return null;
                }

                if(item == null){
                    return null;
                }

                for (Map.Entry<Integer, Integer> entry : found.entrySet()) {
                    menu.consumeItem(entry.getKey(), entry.getValue());
                }

                menu.consumeItem(ORIGIN_MATERIAL_SLOTS, 1);
                MachineRecipe newRecipe = new MachineRecipe(recipe.getTicks()/2, recipe.getInput(), outputs);

                return newRecipe;
            } else {
                found.clear();
            }
        }

        return null;
    }

    /**
     * [核心修改]生成 OriginMaterialGenLite 物品并写入 NBT
     */
    private ItemStack OriginMaterialGenLite(ItemStack item) {

        String n = ItemStackHelper.getName(item);
        // 注意:这里建议使用 ORIGIN_MATERIAL_GEN_LITE,如果你没有定义这个常量,可以先用 ORIGIN_MATERIAL_GEN 代替,或者在显示名称里修改
        ItemStack itemOutput = ORIGIN_MATERIAL_GEN_LITE.clone();
        ItemMeta meta = itemOutput.getItemMeta();
        if (meta != null) {
            String originalName = meta.getDisplayName();
            meta.setDisplayName(originalName + " - " + n);
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(getGradientNameVer2("Current Item: " + n));
            meta.setLore(lore);

            NamespacedKey key = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");
            // 写入 NBT,机器识别这个 key 来决定产出
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, item.getType().name());
            itemOutput.setItemMeta(meta);
        }
        return itemOutput;
    }

    /**
     * 为物品添加描述(lore)
     */
    private ItemStack addLore(ItemStack item, String loreText) {
        ItemStack newItem = item.clone();
        ItemMeta meta = newItem.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(loreText);
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }

    protected int getProgressSlot() {
        return 31;
    }

    @Override
    protected void setupMenu(BlockMenuPreset preset) {

        preset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE," "), BACKGROUND_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,getGradientName("←Input Slots")), INPUT_BORDER_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,getGradientName("Output Slots →")), OUTPUT_BORDER_SLOTS);

        preset.drawBackground(new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE,getGradientName("item")), ORIGIN_MATERIAL_BORDER_SLOTS);

        preset.addItem(getProgressSlot(), new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(22, PROGRESS_STACK, ChestMenuUtils.getEmptyClickHandler());

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

    @Override
    protected int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    protected int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }


    @Override
    public ItemStack getProgressBar() {
        return PROGRESS_ITEM;
    }
}
