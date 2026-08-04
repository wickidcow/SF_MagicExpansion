package io.Yomicer.magicExpansion.items.tools;

import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class FishingRodWaterCloud extends SlimefunItem implements RecipeDisplayItem {

    private final Map<Enchantment, Integer> enchantments;
    private final boolean glow;
    private final Map<String, List<WeightedItem>> lootTable;

    // ✅ 所有可使用的鱼饵定义在这里（Material, 显示名, Lore）
    private final List<ItemStack> USABLE_LURES;


    /**
     * 构造器
     *
     * @param itemGroup   分类
     * @param item        物品
     * @param recipeType  合成方式
     * @param recipe      合成表
     * @param enchantments 要添加的附魔（附魔 -> 等级）
     * @param glow         是否显示附魔光效（true=发光，false=隐藏附魔）
     */
    public FishingRodWaterCloud(ItemGroup itemGroup,
                                SlimefunItemStack item,
                                RecipeType recipeType,
                                ItemStack[] recipe,
                                Map<Enchantment, Integer> enchantments,
                                boolean glow,
                                Map<String, List<WeightedItem>> lootTable,
                                List<ItemStack> USABLE_LURES) {
        super(itemGroup, item, recipeType, recipe);
        this.enchantments = enchantments;
        this.glow = glow;
        this.lootTable = lootTable;
        this.USABLE_LURES = USABLE_LURES;
    }


    @Override
    public void preRegister() {
        ItemStack itemStack = getItem();
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            // 1. 设置无法破坏（原有功能）
            meta.setUnbreakable(true);

            // 2. 添加外部传入的附魔
            if (enchantments != null && !enchantments.isEmpty()) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchant = entry.getKey();
                    int level = entry.getValue();
                    if (enchant != null && level > 0) {
                        meta.addEnchant(enchant, level, true);
                    }
                }
            }
            if (!glow) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
            itemStack.setItemMeta(meta);
        }

    }
    public List<WeightedItem> getLootPoolForLure(Lure lure) {
        String key = lure != null ? lure.getKey() : "default";
        return lootTable.getOrDefault(key, getDefaultLootPool());
    }

    private List<WeightedItem> getDefaultLootPool() {
        return List.of(
                new WeightedItem(new ItemStack(Material.COD), 50),
                new WeightedItem(new ItemStack(Material.INK_SAC), 30)
        );
    }


    public Map<String, List<WeightedItem>> getLootTable() {
        return lootTable;
    }


    @Override
    public @NotNull List<ItemStack> getDisplayRecipes() {
        List<ItemStack> display = new ArrayList<>();
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Supported Lures ⇩"),getGradientName("Lures listed earlier have higher priority.")));
        display.add(new CustomItemStack(Material.AIR));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Supported Lures ⇩"),getGradientName("The off-hand lure always has priority; no lure is always last.")));
        display.add(new CustomItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, getGradientName("No Lure")));
        // 添加所有鱼饵
        for (ItemStack lure : USABLE_LURES) {
            display.add(new CustomItemStack(Material.AIR));
            display.add(lure);
        }


        return display;
    }
}
