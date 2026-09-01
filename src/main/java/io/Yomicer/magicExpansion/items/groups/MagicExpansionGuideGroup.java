package io.Yomicer.magicExpansion.items.groups;

import io.Yomicer.magicExpansion.utils.GuideCategoryMenu;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagicExpansionGuideGroup extends FlexItemGroup {

    public MagicExpansionGuideGroup(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    @Override
    public boolean isVisible(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        return mode == SlimefunGuideMode.SURVIVAL_MODE || mode == SlimefunGuideMode.CHEAT_MODE;
    }

    @Override
    public void open(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        GuideCategoryMenu.openCategoryPage(player, profile, mode, 1);
    }
}