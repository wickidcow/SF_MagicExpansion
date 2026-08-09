package io.Yomicer.magicExpansion.items.groups;

import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 虚拟层级分组:仅作为 GuideHistory 条目,让三级菜单返回链逐级生效
 * 不挂载任何父组、不进原生分组页,点击时由 openItemGroup 调用其 open() 打开对应自绘页
 */
public class VirtualGuideGroup extends FlexItemGroup {

    private final GuidePage page;

    public VirtualGuideGroup(NamespacedKey key, ItemStack item, GuidePage page) {
        super(key, item);
        this.page = page;
    }

    @Override
    public boolean isVisible(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        return false;
    }

    @Override
    public void open(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        if (page != null) {
            page.open(player, profile, mode);
        }
    }

    @FunctionalInterface
    public interface GuidePage {
        void open(Player player, PlayerProfile profile, SlimefunGuideMode mode);
    }
}