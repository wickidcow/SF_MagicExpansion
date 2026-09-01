package io.Yomicer.magicExpansion.items.tools;

import io.Yomicer.magicExpansion.items.misc.Lure;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
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
     * 鱼竿稀有度 → 二代鱼属性区间系数(weightBoost)。
     * 越大钓到的鱼五维纯度越高、品质系数越优。青竹最低、太公最高。
     */
    private final double weightBoost;


    /**
     * 构造器
     *
     * @param itemGroup   分类
     * @param item        物品
     * @param recipeType  合成方式
     * @param recipe      合成表
     * @param enchantments 要添加的附魔（附魔 -> 等级）
     * @param glow         是否显示附魔光效（true=发光，false=隐藏附魔）
     * @param weightBoost  二代鱼属性区间系数(0~4 档，青竹最低太公最高)
     */
    /** Backward-compatible constructor used by pre-Release-10 rod registrations. */
    public FishingRodWaterCloud(ItemGroup itemGroup,
                                SlimefunItemStack item,
                                RecipeType recipeType,
                                ItemStack[] recipe,
                                Map<Enchantment, Integer> enchantments,
                                boolean glow,
                                Map<String, List<WeightedItem>> lootTable,
                                List<ItemStack> USABLE_LURES) {
        this(itemGroup, item, recipeType, recipe, enchantments, glow, 0.0D, lootTable, USABLE_LURES);
    }

    public FishingRodWaterCloud(ItemGroup itemGroup,
                                SlimefunItemStack item,
                                RecipeType recipeType,
                                ItemStack[] recipe,
                                Map<Enchantment, Integer> enchantments,
                                boolean glow,
                                double weightBoost,
                                Map<String, List<WeightedItem>> lootTable,
                                List<ItemStack> USABLE_LURES) {
        super(itemGroup, item, recipeType, recipe);
        this.enchantments = enchantments;
        this.glow = glow;
        this.weightBoost = weightBoost;
        this.lootTable = lootTable;
        this.USABLE_LURES = USABLE_LURES;
    }

    /** 读取二代鱼属性区间系数(档位) */
    public double getWeightBoost() {
        return weightBoost;
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

    // ============================================================
    // 二代鱼钓物池构建（共享通用池 + 每饵专用 + 杂物层）
    // ============================================================

    /**
     * 三区共享的"通用鱼池"种子（真鱼图例，钓获时再生成属性）。
     * 权重固定，供所有鱼竿鱼饵注入，保证基础收获一致。
     */
    private static final List<WeightedItem> GEN2_COMMON_POOL = List.of(
            // 通用鱼种子：权重按"常见见底"设计，稀有种略低
            gen2Seed(Gen2Fish.YUNYIN, 450),
            gen2Seed(Gen2Fish.LANGXI, 450),
            gen2Seed(Gen2Fish.HENBO, 400),
            gen2Seed(Gen2Fish.WUYU, 400)
    );

    /**
     * 通用垃圾杂物池（L3-A：纯兜底）。与元素素材层(B)由 buildLootTable 各自注入。
     */
    private static final List<WeightedItem> GEN2_JUNK_POOL = List.of(
            new WeightedItem(new ItemStack(Material.COBBLESTONE, 15), 66),
            new WeightedItem(new ItemStack(Material.GRAVEL, 15), 66),
            new WeightedItem(new ItemStack(Material.SAND, 15), 66),
            new WeightedItem(new ItemStack(Material.FLINT, 15), 66),
            new WeightedItem(new ItemStack(Material.STRING, 15), 66),
            new WeightedItem(new ItemStack(Material.BONE, 15), 66)
    );

    /**
     * 用二代鱼种生成"钓物池种子"（无属性 ItemStack，钓获瞬间由属性生成器注入五维/品质）。
     */
    private static WeightedItem gen2Seed(Gen2Fish type, int weight) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§d品级 · " + type.getRarity().getDisplayName());
        lore.add(type.getLore());
        // 与 FishAttributeGenerator.generate 保持一致的构造方式:
        // 鱼桶材质(COD_BUCKET/PUFFERFISH_BUCKET 等)用 CustomItemStack 固化 name/lore,
        // 避免"裸 new ItemStack + setItemMeta"在桶类物品上丢失 meta(实测 lore 写不入)。
        ItemStack item = new CustomItemStack(type.getMaterial(), type.getGradientName(), lore.toArray(new String[0]));
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            // 标记为二代鱼"种子"：钓获监听据此识别并注入属性
            meta.getPersistentDataContainer().set(
                    io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator.GEN2_TYPE,
                    org.bukkit.persistence.PersistentDataType.STRING,
                    type.getId());
            item.setItemMeta(meta);
        }
        return new WeightedItem(item, weight);
    }

    /**
     * 构建一个鱼饵的二代鱼钓物池：
     * = 共享通用池 + 该饵专用鱼(主力/镇饵) + 通用垃圾兜底 + 元素素材层 + 特殊钓物。
     *
     * @param lureKey          鱼饵 key
     * @param mainFish         该饵主力专属鱼(权重约800)
     * @param epicFish         该饵史诗镇饵鱼(权重约400)
     * @param elementMaterials 该饵元素主题素材(L3-B)，可为空
     * @param specialCatch     该特殊钓物(权重40)
     * @return 组装好的钓物池
     */
    public static List<WeightedItem> buildLootTable(String lureKey,
                                                    Gen2Fish mainFish, Gen2Fish epicFish,
                                                    List<WeightedItem> elementMaterials,
                                                    ItemStack specialCatch) {
        List<WeightedItem> pool = new ArrayList<>();
        // L1 共享通用池
        pool.addAll(GEN2_COMMON_POOL);
        // L2 该饵专属(主力 + 史诗镇饵)
        pool.add(gen2Seed(mainFish, 800));
        pool.add(gen2Seed(epicFish, 400));
        // L3-B 元素素材层(可选) + 通用垃圾兜底
        if (elementMaterials != null && !elementMaterials.isEmpty()) {
            pool.addAll(elementMaterials);
        }
        pool.addAll(GEN2_JUNK_POOL);
        // L4 特殊钓物
        if (specialCatch != null) {
            pool.add(new WeightedItem(specialCatch, 40));
        }
        return pool;
    }

    /**
     * 便捷构造一个元素素材条目。
     */
    public static WeightedItem elementMaterial(ItemStack item, int weight) {
        return new WeightedItem(item, weight);
    }
}
