package io.Yomicer.magicExpansion.items.misc.fish;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Resolves Water Cloud generation-two loot seeds into real, attributed fish
 * at catch time. This deliberately does not reroll fish that already have
 * individual attributes or rewrite any unrelated Slimefun item.
 */
public final class WaterCloudCatchMaterializer {

    private WaterCloudCatchMaterializer() {
    }

    public static ItemStack materialize(ItemStack selected, int rodQualityTier) {
        if (!isRawSeed(selected)) {
            return selected;
        }

        String fishId = selected.getItemMeta().getPersistentDataContainer()
                .get(FishAttributeGenerator.GEN2_TYPE, PersistentDataType.STRING);
        Gen2Fish fish = Gen2Fish.byId(fishId);
        if (fish == null) {
            // Unknown or corrupt IDs must not silently turn into a different fish.
            return selected;
        }

        int tier = Math.max(0, Math.min(4, rodQualityTier));
        return FishAttributeGenerator.generate(fish, tier);
    }

    public static boolean isRawSeed(ItemStack item) {
        if (item == null || item.getAmount() != 1 || !item.hasItemMeta()) {
            return false;
        }

        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        // Raw seeds from FishingRodWaterCloud.gen2Seed have only GEN2_TYPE.
        // Never reroll completed, partially migrated or bred player fish.
        return data.has(FishAttributeGenerator.GEN2_TYPE, PersistentDataType.STRING)
                && !data.has(FishAttributeGenerator.GEN2_ELEMENTS, PersistentDataType.STRING)
                && !data.has(FishAttributeGenerator.GEN2_QUALITY, PersistentDataType.DOUBLE)
                && !data.has(FishAttributeGenerator.GEN2_TRAIT, PersistentDataType.STRING)
                && !data.has(FishAttributeGenerator.GEN2_PARENTS, PersistentDataType.STRING);
    }
}
