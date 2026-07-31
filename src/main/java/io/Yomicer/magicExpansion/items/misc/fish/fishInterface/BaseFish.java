package io.Yomicer.magicExpansion.items.misc.fish.fishInterface;

import io.Yomicer.magicExpansion.items.misc.fish.Fish;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseFish {
    private final Fish originalFish;
    private Material material;
    private List<String> additionalLore = new ArrayList<>();

    // 私有构造函数
    private BaseFish(Fish originalFish) {
        this.originalFish = originalFish;
        this.material = getMaterialByRarity(originalFish.getRarity());
    }

    /**
     * 静态方法创建BaseFish实例
     */
    public static BaseFish of(Fish fish) {
        return new BaseFish(fish);
    }

    /**
     * 为每个鱼类创建静态实例(方便直接调用)
     */
    public static final BaseFish SanWenFish = of(Fish.SanWenFish);
    public static final BaseFish XueFish = of(Fish.XueFish);
    public static final BaseFish HeTun = of(Fish.HeTun);
    public static final BaseFish ReDaiFish = of(Fish.ReDaiFish);
    public static final BaseFish TestFish = of(Fish.TestFish);
    public static final BaseFish CopperDustFish = of(Fish.CopperDustFish);
    public static final BaseFish GoldDustFish = of(Fish.GoldDustFish);
    public static final BaseFish IronDustFish = of(Fish.IronDustFish);
    public static final BaseFish TinDustFish = of(Fish.TinDustFish);
    public static final BaseFish SilverDustFish = of(Fish.SilverDustFish);
    public static final BaseFish AluminumDustFish = of(Fish.AluminumDustFish);
    public static final BaseFish LeadDustFish = of(Fish.LeadDustFish);
    public static final BaseFish ZincDustFish = of(Fish.ZincDustFish);
    public static final BaseFish MagnesiumDustFish = of(Fish.MagnesiumDustFish);
    public static final BaseFish CoalFish = of(Fish.CoalFish);
    public static final BaseFish EmeraldFish = of(Fish.EmeraldFish);
    public static final BaseFish LapisFish = of(Fish.LapisFish);
    public static final BaseFish DiamondFish = of(Fish.DiamondFish);
    public static final BaseFish QuartzFish = of(Fish.QuartzFish);
    public static final BaseFish AmethystFish = of(Fish.AmethystFish);
    public static final BaseFish IronFish = of(Fish.IronFish);
    public static final BaseFish GoldFish = of(Fish.GoldFish);
    public static final BaseFish CopperFish = of(Fish.CopperFish);
    public static final BaseFish RedstoneFish = of(Fish.RedstoneFish);
    public static final BaseFish NetheriteFish = of(Fish.NetheriteFish);
    public static final BaseFish GlowStoneDustFish = of(Fish.GlowStoneDustFish);
    public static final BaseFish ShuLingYu = of(Fish.ShuLingYu);
    public static final BaseFish UraniumFish = of(Fish.UraniumFish);
    public static final BaseFish OilRockFish = of(Fish.OilRockFish);
    public static final BaseFish FoamCrystalFish = of(Fish.FoamCrystalFish);
    public static final BaseFish BlackDiamondFish = of(Fish.BlackDiamondFish);
    public static final BaseFish SulfateFish = of(Fish.SulfateFish);
    public static final BaseFish SiliconFish = of(Fish.SiliconFish);
    public static final BaseFish EnchantedBottleFish = of(Fish.EnchantedBottleFish);


    //合金锭鱼
    public static final BaseFish ReinforcedAlloyFish = of(Fish.ReinforcedAlloyFish);
    public static final BaseFish HardenedMetalFish = of(Fish.HardenedMetalFish);
    public static final BaseFish DamascusSoulFish = of(Fish.DamascusSoulFish);
    public static final BaseFish SteelSoulFish = of(Fish.SteelSoulFish);
    public static final BaseFish BronzeAncientFish = of(Fish.BronzeAncientFish);
    public static final BaseFish HardlightAluFish = of(Fish.HardlightAluFish);
    public static final BaseFish SilverCopperFish = of(Fish.SilverCopperFish);
    public static final BaseFish BrassResonanceFish = of(Fish.BrassResonanceFish);
    public static final BaseFish AluminumBrassFish = of(Fish.AluminumBrassFish);
    public static final BaseFish AluminumBronzeFish = of(Fish.AluminumBronzeFish);
    public static final BaseFish CorinthianBronzeFish = of(Fish.CorinthianBronzeFish);
    public static final BaseFish SolderFlowFish = of(Fish.SolderFlowFish);
    public static final BaseFish NickelSpiritFish = of(Fish.NickelSpiritFish);
    public static final BaseFish CobaltFlameFish = of(Fish.CobaltFlameFish);
    public static final BaseFish SiliconIronFish = of(Fish.SiliconIronFish);
    public static final BaseFish CarbonSoulFish = of(Fish.CarbonSoulFish);
    public static final BaseFish GildedIronFish = of(Fish.GildedIronFish);
    public static final BaseFish RedstoneAlloyFish = of(Fish.RedstoneAlloyFish);
    public static final BaseFish NeptuniumShadowFish = of(Fish.NeptuniumShadowFish);
    public static final BaseFish PlutoniumCoreFish = of(Fish.PlutoniumCoreFish);





    public static final BaseFish LegendaryLuFish = of(Fish.LegendaryLuFish);
    public static final BaseFish LegendaryEelFish = of(Fish.LegendaryEelFish);


    public static final BaseFish MYSTIC_EEL = of(Fish.MYSTIC_EEL);

    /**
     * 根据稀有度获取材质
     */
    private Material getMaterialByRarity(Fish.Rarity originalRarity) {
        return switch (originalRarity) {
            case COMMON -> Material.COD_BUCKET;           // 普通 - 鳕鱼桶
            case UNCOMMON -> Material.SALMON_BUCKET;      // 不常见 - 鲑鱼桶
            case RARE, RARE_POOL_DUST, RARE_POOL_ORE, RARE_POOL_INDUSTRY ->
                    Material.PUFFERFISH_BUCKET;               // 稀有 - 河豚桶
            case EPIC, EPIC_POOL_INDUSTRY, EPIC_POOL_ALLOY_INGOT ->
                    Material.TROPICAL_FISH_BUCKET;            // 史诗 - 热带鱼桶
            case LEGENDARY, LEGENDARY_EEL ->
                    Material.AXOLOTL_BUCKET;                  // 传说 - 美西螈桶
            case MYTHICAL ->
                Material.NAUTILUS_SHELL;                    // 神话 鹦鹉螺壳
            default -> Material.COD_BUCKET;
        };
    }

    /**
     * 创建一个当前实例的副本
     */
    public BaseFish copy() {
        BaseFish copy = new BaseFish(this.originalFish);
        copy.material = this.material;
        copy.additionalLore = new ArrayList<>(this.additionalLore);
        return copy;
    }

    /**
     * 链式添加Lore
     */
    public BaseFish addLore(String... lines) {
        additionalLore.addAll(Arrays.asList(lines));
        return this;
    }

    /**
     * 清空额外Lore
     */
    public BaseFish clearLore() {
        additionalLore.clear();
        return this;
    }

    /**
     * 设置材质(覆盖根据稀有度设置的材质)
     */
    public BaseFish setMaterial(Material material) {
        this.material = material;
        return this;
    }

    /**
     * 构建ItemStack
     */
    public ItemStack build() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(originalFish.getDisplayName());

            // 合并原有Lore和额外添加的Lore
            List<String> lore = new ArrayList<>();
            lore.addAll(Arrays.asList(originalFish.getLoreLines()));
            if (!additionalLore.isEmpty()) {
                lore.addAll(additionalLore);
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 快速获取ItemStack(不添加额外Lore)
     */
    public ItemStack toItemStack() {
        return build();
    }
}
