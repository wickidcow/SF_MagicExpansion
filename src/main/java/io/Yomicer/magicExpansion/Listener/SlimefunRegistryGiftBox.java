package io.Yomicer.magicExpansion.Listener;

import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.MagicExpansionItemSetup.magicexpansionhonkai;

public class SlimefunRegistryGiftBox implements Listener {


    public static final Map<String, SlimefunItem> itemMapMihoyoHonkai = new HashMap<>();

    // 加载物品组内的所有物品到 map 中
    public static void loadItemsFromGroup(@Nonnull ItemGroup group, Map<String, SlimefunItem> itemMap) {
        List<SlimefunItem> items = group.getItems();

        if (items == null || items.isEmpty()) {
            Debug.logInfo("⚠️ Item group [" + group.getKey() + "] contains no loadable items.");
            return;
        }

        for (SlimefunItem item : items) {
            if (item != null && item.getItem() != null) {
                itemMap.put(item.getId(), item);
            }
        }

        Debug.logInfo("✅ Loaded [" + itemMap.size() + "] items into the reward pool.");
    }

    @EventHandler
    public void onSlimefunRegistryFinalized(SlimefunItemRegistryFinalizedEvent event) {

        loadItemsFromGroup(magicexpansionhonkai,itemMapMihoyoHonkai);
        Debug.logInfo("Gift-box reward pool loaded.");


    }

}
