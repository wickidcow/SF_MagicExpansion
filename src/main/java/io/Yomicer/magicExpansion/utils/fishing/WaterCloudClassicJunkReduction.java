package io.Yomicer.magicExpansion.utils.fishing;

import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.utils.WaterCloudRodEffects;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Classic Water Cloud level bonus: reduce ONLY six upstream-designated
 * vanilla junk entries in the fishing loot table.
 *
 * <p>The source pool, item templates, special rewards and other Slimefun
 * materials are never mutated. No new items or extra payouts are created.
 */
public final class WaterCloudClassicJunkReduction {

    public static final String ENABLED_PATH = "Fish.WaterCloud.classic-junk-reduction-enabled";

    private static final Set<Material> UPSTREAM_JUNK = EnumSet.of(
            Material.COBBLESTONE,
            Material.FLINT,
            Material.GRAVEL,
            Material.SAND,
            Material.STRING,
            Material.BONE
    );

    private WaterCloudClassicJunkReduction() {
    }

    public static List<WeightedItem> adjust(List<WeightedItem> source, int level) {
        if (source == null || source.isEmpty()
                || WaterCloudRodEffects.getOldJunkReduction(level) <= 0) {
            return source;
        }

        List<WeightedItem> adjusted = new ArrayList<>(source.size());
        for (WeightedItem item : source) {
            if (item == null || item.getItem() == null || item.getWeight() <= 0) {
                adjusted.add(item);
                continue;
            }

            ItemStack stack = item.getItem();
            boolean registeredSlimefun = UPSTREAM_JUNK.contains(stack.getType())
                    && SlimefunItem.getByItem(stack) != null;
            int weight = adjustedWeight(item.getWeight(), stack.getType(),
                    registeredSlimefun, level);
            adjusted.add(weight == item.getWeight()
                    ? item : new WeightedItem(stack, weight));
        }
        return adjusted;
    }

    /**
     * Pure policy used by unit tests. Matches upstream: round(original *
     * (1 - reduction)), minimum positive junk weight of one.
     */
    public static int adjustedWeight(int weight, Material material,
                                     boolean isRegisteredSlimefun, int level) {
        if (weight <= 0 || material == null || isRegisteredSlimefun
                || !UPSTREAM_JUNK.contains(material)) {
            return weight;
        }
        double reduction = WaterCloudRodEffects.getOldJunkReduction(level);
        if (reduction <= 0) {
            return weight;
        }
        return Math.max(1, (int) Math.round(weight * (1.0D - reduction)));
    }
}
