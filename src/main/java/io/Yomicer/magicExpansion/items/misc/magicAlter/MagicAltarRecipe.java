package io.Yomicer.magicExpansion.items.misc.magicAlter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MagicAltarRecipe {
    private final ItemStack[][] dispenserItems;
    private final ItemStack result;
    private final Material[][] baseLayout;


    public MagicAltarRecipe(ItemStack[][] dispenserItems, ItemStack result) {
        this.dispenserItems = dispenserItems;
        this.result = result;
        this.baseLayout = createDefaultBaseLayout();
    }

    public MagicAltarRecipe(ItemStack[][] dispenserItems, ItemStack result, Material[][] baseLayout) {
        this.dispenserItems = dispenserItems;
        this.result = result;
        this.baseLayout = baseLayout;
    }

    private Material[][] createDefaultBaseLayout() {
        return new Material[][]{
                {Material.GLASS, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
                {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
                {Material.OBSIDIAN, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.OBSIDIAN},
                {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
        };
    }

    public ItemStack[][] getDispenserItems() {
        return dispenserItems;
    }

    public ItemStack getResult() {
        return result;
    }

    public Material[][] getBaseLayout() {
        return baseLayout;
    }

    // 检查配方是否匹配 - 强制方位匹配
    public boolean matches(ItemStack[][] inputItems) {
        if (inputItems.length != 9) return false;

        for (int dispenserIndex = 0; dispenserIndex < 9; dispenserIndex++) {
            ItemStack[] recipeSlots = dispenserItems[dispenserIndex];
            ItemStack[] inputSlots = inputItems[dispenserIndex];

            for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                ItemStack recipeItem = recipeSlots[slotIndex];
                ItemStack inputItem = inputSlots[slotIndex];

                if (recipeItem != null) {
                    // 检查输入物品是否精确匹配
                    if (inputItem == null ||
                            inputItem.getType() != recipeItem.getType() ||
                            inputItem.getAmount() != recipeItem.getAmount()) {
                        return false;
                    }

                    // 检查NBT数据(如果有的话)
                    if (recipeItem.hasItemMeta() && !recipeItem.getItemMeta().equals(inputItem.getItemMeta())) {
                        return false;
                    }
                } else {
                    // 配方中该位置为空,但输入有物品
                    if (inputItem != null) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // 获取发射器显示物品
    public ItemStack getDisplayItem(int dispenserIndex) {
        if (dispenserIndex < 0 || dispenserIndex >= 9) return null;

        boolean hasItems = false;
        for (int i = 0; i < 9; i++) {
            if (dispenserItems[dispenserIndex][i] != null) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) return null;

        ItemStack dispenser = new ItemStack(Material.DISPENSER);
        ItemMeta meta = dispenser.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§bDispenser #" + (dispenserIndex + 1));

            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7Click to view its detailed configuration.");

            int itemCount = 0;
            for (int i = 0; i < 9; i++) {
                if (dispenserItems[dispenserIndex][i] != null) {
                    itemCount++;
                }
            }
            lore.add("§7Item Amount: " + itemCount + "/9");

            meta.setLore(lore);
            dispenser.setItemMeta(meta);
        }

        return dispenser;
    }

    // 获取指定发射器槽位的物品
    public ItemStack getDispenserSlotItem(int dispenserIndex, int slotIndex) {
        if (dispenserIndex < 0 || dispenserIndex >= 9 || slotIndex < 0 || slotIndex >= 9) {
            return null;
        }
        return dispenserItems[dispenserIndex][slotIndex];
    }

    // 检查发射器是否为空
    public boolean isDispenserEmpty(int dispenserIndex) {
        for (int i = 0; i < 9; i++) {
            if (dispenserItems[dispenserIndex][i] != null) {
                return false;
            }
        }
        return true;
    }

    // 获取发射器中物品数量
    public int getDispenserItemCount(int dispenserIndex) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (dispenserItems[dispenserIndex][i] != null) {
                count++;
            }
        }
        return count;
    }
}
