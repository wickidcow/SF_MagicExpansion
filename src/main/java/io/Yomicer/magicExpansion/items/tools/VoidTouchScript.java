package io.Yomicer.magicExpansion.items.tools;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.Yomicer.magicExpansion.utils.ItemPermissionUtils.hasPermissionRe;

public class VoidTouchScript extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public VoidTouchScript(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    private static final NamespacedKey KEY_X = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
    private static final NamespacedKey KEY_Y = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
    private static final NamespacedKey KEY_Z = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
    private static final NamespacedKey KEY_WORLD = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e->{
            // 阻止默认行为
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            Player player = e.getPlayer();
            if (e.getHand() != EquipmentSlot.HAND) {
                return;
            }

            ItemStack item = e.getItem();
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer container = meta.getPersistentDataContainer();

            // ========================
            // 情况1:Shift + 右键方块 → 记录坐标到物品
            // ========================
            if (player.isSneaking() && e.getClickedBlock().isPresent()) {
                Block block = e.getClickedBlock().get();

                // 写入坐标到物品 NBT
                container.set(KEY_X, PersistentDataType.INTEGER, block.getX());
                container.set(KEY_Y, PersistentDataType.INTEGER, block.getY());
                container.set(KEY_Z, PersistentDataType.INTEGER, block.getZ());
                container.set(KEY_WORLD, PersistentDataType.STRING, block.getWorld().getName());

                // 更新 Lore
                updateLore(meta, block.getLocation());
                item.setItemMeta(meta);

                player.sendMessage("Binding updated: " + formatLocation(block.getLocation()));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            }

            // ========================
            // 情况2:右键任意东西 → 优先尝试远程交互
            // ========================
            else {
                Location targetLoc = null;

                if (container.has(KEY_X, PersistentDataType.INTEGER) &&
                        container.has(KEY_Y, PersistentDataType.INTEGER) &&
                        container.has(KEY_Z, PersistentDataType.INTEGER) &&
                        container.has(KEY_WORLD, PersistentDataType.STRING)) {

                    String worldName = container.get(KEY_WORLD, PersistentDataType.STRING);
                    World world = Bukkit.getWorld(worldName);

                    if (world != null) {
                        int x = container.get(KEY_X, PersistentDataType.INTEGER);
                        int y = container.get(KEY_Y, PersistentDataType.INTEGER);
                        int z = container.get(KEY_Z, PersistentDataType.INTEGER);
                        targetLoc = new Location(world, x, y, z);
                    }
                }

                if (targetLoc != null) {
                    Location playerLoc = player.getLocation();
                    // ✅ 检查是否在同一世界
                    if (!playerLoc.getWorld().equals(targetLoc.getWorld())) {
                        player.sendMessage("⚠️ The target location is unavailable.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    double distance = targetLoc.distance(playerLoc);

                    if (distance > 250) {
                        player.sendMessage("⚠️ The target location is unavailable or more than 100 blocks away.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    Block targetBlock = targetLoc.getBlock();

                    // 模拟右键点击该方块
                    PlayerInteractEvent interactEvent = new PlayerInteractEvent(
                            player,
                            Action.RIGHT_CLICK_BLOCK,
                            new ItemStack(Material.AIR),
                            targetBlock,
                            BlockFace.SELF
                    );

                    Bukkit.getPluginManager().callEvent(interactEvent);

                    player.sendMessage("🔁 Void Touchcan:" + formatLocation(targetLoc));
                    player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
                }
            }
        };
    }

    // 更新物品 Lore 显示绑定位置
    private void updateLore(ItemMeta meta, Location loc) {
        List<String> lore;
        if (meta.hasLore()) {
            List<String> existingLore = meta.getLore();
            int size = Math.min(existingLore.size(), 8);
            lore = new ArrayList<>(existingLore.subList(0, size));
        } else {
            lore = new ArrayList<>();
        }
        lore.add("§bBound Coordinates: §fX:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
        lore.add("§bWorld: §f" + loc.getWorld().getName());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }

    // 格式化位置显示
    private String formatLocation(Location loc) {
        return String.format("X:%d Y:%d Z:%d (World: %s)",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                loc.getWorld().getName());
    }

}
