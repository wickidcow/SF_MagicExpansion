package io.Yomicer.magicExpansion.items.generators;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.items.misc.fish.Fish;
import io.Yomicer.magicExpansion.items.misc.fish.FishKeys;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.Yomicer.magicExpansion.utils.machineLore.ChargeLore;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.xml.stream.events.Namespace;

import java.util.*;

import static io.Yomicer.magicExpansion.items.misc.fish.Fish.MYSTIC_EEL;
import static io.Yomicer.magicExpansion.items.misc.fish.Fish.XueFish;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;


public class FishEnergyGenerator extends MenuBlock implements EnergyNetProvider, RecipeDisplayItem {


    private final int Capacity;
    private final int power = 50000;
    private final String fishTypeTarget = "MYSTIC_EEL";
    private final int power2 = 200;
    private final String fishTypeTarget2 = "XueFish";

    // 1. 定义可扩展的鱼能量产出表(集中管理,易扩展)
    private final Map<String, Integer> FISH_POWER_MAP = new LinkedHashMap<>() {{
        put("MYSTIC_EEL",     50000);  // 神秘鳗鱼:高能量
        put("XueFish",        200);    // 雪鱼:低能量
        put("GlowStoneDustFish",      3000);    // 雪鱼:低能量

    }};
    // ✅ 鱼类型 -> 显示用的桶材质
    private final Map<String, Material> FISH_BUCKET_MAP = new HashMap<>() {{
        put("MYSTIC_EEL",   Material.TROPICAL_FISH_BUCKET);
        put("XueFish",      Material.COD_BUCKET);
        put("GlowStoneDustFish",      Material.PUFFERFISH_BUCKET);
        // 👉 新增鱼？直接加:put("GlowStoneFish", Material.SALMON_BUCKET);
    }};


    public FishEnergyGenerator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int Capacity) {
        super(category, item, recipeType, recipe);
        this.Capacity = Capacity;
    }

    @Override
    protected void setup(BlockMenuPreset blockMenuPreset) {
        blockMenuPreset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE," "),new int[] {

                0,1,2,3        ,5,6,7,8,
                9,10,11,         15,16,17,
                18,19,20,21,  23,24,25,26,
                27,28,29,30,  32,33,34,35,
                36,37,38,39,  41,42,43,44,
                45,46,47,48,49,50,51,52,53
        });
        blockMenuPreset.drawBackground(new CustomItemStack(CustomHead.getHead("7aa17a1abe18d3830391e970a582553ffe0b8afe36ea3c74b5eb521f9c5a54c0") ,getGradientName("Fish Bucket Map")),
                new int[] {
                22,   31
        });
        blockMenuPreset.drawBackground(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71") ,getGradientName("Fish Bucket Map")),
                new int[] {
                        12
                });
        blockMenuPreset.drawBackground(new CustomItemStack(CustomHead.getHead("5fa22cc6ddd569a6ce894aab906b73db8ba89f6a2bb071bab22e57a4f0885abf") ,getGradientName("Fish Bucket Map")),
                new int[] {
                        14
                });
        blockMenuPreset.drawBackground(new CustomItemStack(CustomHead.getHead("ab93edba42c7bbfa94b12f89bd55d95862259cdb6293c83b90b931ae4d139088") ,getGradientName("Fish Bucket Map")),
                new int[] {
                        4
                });
        blockMenuPreset.drawBackground(new CustomItemStack(new ItemStack(Material.RED_STAINED_GLASS_PANE),"Fish Bucket Map"),
                new int[] {
                        40
                });

    }

    @Nonnull
    @Override
    protected int[] getInputSlots(DirtyChestMenu dirtyChestMenu, ItemStack itemStack) {
        return new int[]{13};
    }

    @Override
    protected int[] getInputSlots() {
        return new int[]{13};
    }

    @Override
    protected int[] getOutputSlots() {
        return new int[]{13};
    }

    @Override
    public int getGeneratedOutput(Location l, SlimefunBlockData data) {

        BlockMenu inv = StorageCacheUtils.getMenu(l);

        int gen = 0;
        ItemStack fish = null;
        ItemMeta meta = null;
        if (inv != null) {
            fish = inv.getItemInSlot(13);
            if (fish != null && !fish.getType().isAir()) {
                fish = fish.clone();
            }
        }
        if(fish != null) {
            meta = fish.getItemMeta();
        }
        if(meta != null) {

            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            String fishType = null;
            double weight = 0.0;
            String weightRarityName = null;

            if (pdc.has(FishKeys.FISH_TYPE, PersistentDataType.STRING)) {
                fishType = pdc.get(FishKeys.FISH_TYPE, PersistentDataType.STRING);
            }

            if (pdc.has(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE)) {
                weight = pdc.get(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE);
            }

            if (pdc.has(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING)) {
                weightRarityName = pdc.get(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING);
            }

            // ✅ 核心逻辑:从映射表中查找该鱼的能量基础值
            if (fishType != null && weight != 0.0 && weightRarityName != null) {
                Integer basePower = FISH_POWER_MAP.get(fishType);
                if (basePower != null) {
                    double multiplier = Fish.WeightRarity.getMultiplierByName(weightRarityName);
                    gen = (int) (basePower * weight * multiplier);
                }
            }
        }
        if (inv != null && inv.hasViewer()) {
            if (gen == 0) {
                inv.replaceExistingItem(40, new CustomItemStack(
                        Material.LANTERN,
                        getGradientName("Fish Bucket Map"),
                        getGradientName("Stored: " + ChargeLore.format(getCharge(l)) + " J")
                ));
            } else if (gen > 0) {
                inv.replaceExistingItem(40, new CustomItemStack(
                        Material.SOUL_LANTERN,
                        getGradientName("Generating"),
                        getGradientName("Type: " + getPowerType()),
                        getGradientName("Generation Rate: " + ChargeLore.formatEnergy(gen) + " J/s "),
                        getGradientName("Stored: " + ChargeLore.format(getCharge(l)) + " J")
                ));
            }
        }
        return gen;
    }

    @Override
    public int getCapacity() {
        return this.Capacity;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.GENERATOR;
    }

    @Override
    public @NotNull List<ItemStack> getDisplayRecipes() {

        List<ItemStack> display = new ArrayList<>();
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Instructions ⇩"),getGradientName("Please read carefully.")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("Usage:"),getGradientName("to the machine slot")
                ,getGradientName("The electric eel's size affects power generation.")
                ,getGradientName("The electric eel's rarity also affects power generation.")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Instructions ⇩"),getGradientName("Please read carefully.")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("Generation formula:"),getGradientName("Each machine can hold only one electric eel.")
                ,getGradientName("Power = eel weight × power per weight unit × eel rarity")
                ,getGradientName("Common / Rare / Super Rare: 1 / 4 / 11")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientName("Instructions ⇩"),getGradientName("Please read carefully.")));
        display.add(new CustomItemStack(Material.BOOK, getGradientName("Generator update notes:"),getGradientName("Any special Magic Fish may be able to generate power.")
                ,getGradientName("Power = fish weight × "+ " power per unit weight " +"× weight rarity")
                ,getGradientName("Common / Rare / Super Rare: 1 / 4 / 11")));
        display.add(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71"),getGradientName("Used by the MagicExpansion fishing system.")));
        display.add(new CustomItemStack(CustomHead.getHead("26314d31b095e4d421760497be6a156f459d8c9957b7e6b1c12deb4e47860d71"),getGradientName("Used by the MagicExpansion fishing system.")));
//        display.add(new CustomItemStack(Material.TROPICAL_FISH_BUCKET,MYSTIC_EEL.getDisplayName(),getGradientName("Energy per kg per second: "+ChargeLore.formatEnergy(power)+" J")));
//        display.add(new CustomItemStack(Material.COD_BUCKET,XueFish.getDisplayName(),getGradientName("Energy per kg per second: "+ChargeLore.formatEnergy(power2)+" J")));
        // ✅ 自动为所有在 FISH_POWER_MAP 中注册的鱼生成展示项
        for (Map.Entry<String, Integer> entry : FISH_POWER_MAP.entrySet()) {

            String fishTypeName = entry.getKey();
            Fish fish = Fish.fromString(fishTypeName);
            if (fish == null) {
                continue; // 跳过无效类型
            }
            Material displayMaterial = switch (fish.getRarity()) {
                case COMMON -> Material.COD_BUCKET;           // 普通 - 鳕鱼桶
                case UNCOMMON -> Material.SALMON_BUCKET;     // 不常见 - 鲑鱼桶
                case RARE -> Material.PUFFERFISH_BUCKET;  // 稀有 - 河豚
                case EPIC -> Material.TROPICAL_FISH_BUCKET;     // 史诗 - 热带鱼
                case LEGENDARY -> Material.AXOLOTL_BUCKET;   // 传说 - 用美西螈
                case MYTHICAL -> Material.NETHER_STAR;       // 神话 - 下界之星(最稀有)
                default -> Material.COD_BUCKET;
            };

            String displayName = fish.getDisplayName();
            int power = entry.getValue();
            display.add(new CustomItemStack(
                    displayMaterial,
                    displayName,
                    getGradientName("Energy per kg per second:" + ChargeLore.formatEnergy(power) + " J")
            ));
        }

        return display;
    }

}
