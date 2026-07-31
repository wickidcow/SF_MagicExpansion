package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class WordClear extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {
    public WordClear(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            Player p = e.getPlayer();
            Location loc = p.getLocation();
            UUID uuid = p.getUniqueId();


            // --- 权限检测 ---
            if (!ItemPermissionUtils.hasPermissionPoint(p, loc)) {
                p.sendMessage("§cYou do not have permission to use this item!");
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }

            // --- 查找最近的全息盔甲架 ---
            List<Entity> entities = (List<Entity>) (List<?>) loc.getNearbyEntities(3, 3, 3);
            ArmorStand nearestHologram = null;
            double nearestDistance = 999;

            for (Entity en : entities) {
                if (en.getType() == EntityType.ARMOR_STAND) {
                    ArmorStand as = (ArmorStand) en;
                    // 检查是否是全息显示用的盔甲架 (隐形、无重力、标记)
                    if (as.isInvisible() && !as.hasGravity() && as.isMarker()) {
                        double distance = loc.distance(as.getLocation());
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestHologram = as;
                        }
                    }
                }
            }

            // --- 逻辑处理 ---
            String metaKey = "hologramToRemove";
            List<MetadataValue> meta = p.getMetadata(metaKey);
            ArmorStand pendingStand = null;

            // 检查是否有待确认的目标
            if (meta != null && !meta.isEmpty()) {
                // 尝试获取存储的盔甲架引用 (这里使用了弱引用或直接存储,视具体实现而定,这里假设存储的是实体)
                // 注意:在Java中存储实体引用可能因区块卸载失效,但在短时使用中可行
                try {
                    pendingStand = (ArmorStand) meta.get(0).value();
                } catch (Exception ignored) {}
            }

            // 如果玩家之前选中了一个,且这次点击的还是同一个(或者之前的还有效),则执行删除
            if (pendingStand != null && pendingStand.isValid()) {
                // 如果这次也找到了最近的,且就是之前那个(距离极近),或者是同一个对象引用
                if (nearestHologram != null && nearestHologram.getUniqueId().equals(pendingStand.getUniqueId())) {
                    // 再次检查权限
                    if (!ItemPermissionUtils.hasPermissionPoint(p, nearestHologram.getLocation())) {
                        p.sendMessage("§cyou do not have permission!");
                        p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                        p.removeMetadata(metaKey, MagicExpansion.getInstance());
                        return;
                    }

                    // 执行删除
                    nearestHologram.remove();
                    p.sendMessage("§6Holograms cleared successfully!");
                    p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    p.removeMetadata(metaKey, MagicExpansion.getInstance());
                    return;
                }
            }

            // 如果没有待确认的,或者之前的失效了,或者这次选中的是新的
            if (nearestHologram != null) {
                // 设置元数据标记
                p.setMetadata(metaKey, new FixedMetadataValue(MagicExpansion.getInstance(), nearestHologram));

                String content = nearestHologram.getCustomName();
                if (content == null) content = "§7(No Text)";

                p.sendMessage("§eNearest hologram found: " + content);
                p.sendMessage("§7Distance: " + String.format("%.1f", nearestDistance) + " blocks — right-click again to confirm deletion!");
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
            } else {
                // 范围内没找到
                p.sendMessage("§bNo hologram was found within 3 blocks!");
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                // 清除无效的元数据
                p.removeMetadata(metaKey, MagicExpansion.getInstance());
            }
        };
    }
}
