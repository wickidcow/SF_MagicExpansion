package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SchrodingerFrame extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public SchrodingerFrame(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, boolean visible, String tag) {
        super(itemGroup, item, recipeType, recipe);
        this.visible = visible;
        this.tag = tag;
    }

    private final boolean visible;
    private final String tag;

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            if (EquipmentSlot.HAND == e.getHand() && Action.RIGHT_CLICK_BLOCK == e.getInteractEvent().getAction()) {
                ItemStack mainItem = e.getPlayer().getInventory().getItemInMainHand();
                    BlockFace blockFace = e.getInteractEvent().getBlockFace();
                    Block block = e.getClickedBlock().get();

                    if (!block.getType().isSolid() || !block.getType().isOccluding()) {
                        e.getPlayer().sendMessage("§cThis block cannot hold an item frame!");
                        return;
                    }
                    Location spawnLocation = getFrameSpawnLocation(block, blockFace);
                    if (hasItemFrameAtLocation(spawnLocation, blockFace)) {
                        e.getPlayer().sendMessage("§cAn item frame already exists at this location!");
                        return;
                    }

                    World world = block.getWorld();
                    ItemFrame itemFrame = world.spawn(spawnLocation, ItemFrame.class);
                    itemFrame.setMetadata(tag, new FixedMetadataValue(MagicExpansion.getInstance(), true));

                    itemFrame.setFacingDirection(blockFace, false);

                    itemFrame.setVisible(visible);
                    itemFrame.setGlowing(true);
                    itemFrame.setSilent(true);
                    itemFrame.setFixed(false);
                    itemFrame.setInvulnerable(false);
                    itemFrame.setItemDropChance(0.5f);

                    world.playSound(spawnLocation, Sound.BLOCK_WOOL_PLACE, 1.0f, 1.0f);
                    consumeItems(e.getPlayer(), mainItem);

                    e.setUseItem(Event.Result.DENY);
                    e.setUseBlock(Event.Result.DENY);
            }
        };
    }

    private Location getFrameSpawnLocation(Block block, BlockFace face) {
        Location blockLocation = block.getLocation();
        double x = blockLocation.getX();
        double y = blockLocation.getY();
        double z = blockLocation.getZ();
        World world = block.getWorld();

        switch (face) {
            case UP:
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            case DOWN:
                return new Location(world, x + 0.5, y - 0.0625, z + 0.5); // 微微突出下方
            case NORTH:
                return new Location(world, x + 0.5, y + 0.5, z - 0.0625); // z-方向突出一点点
            case SOUTH:
                return new Location(world, x + 0.5, y + 0.5, z + 1.0625); // z+方向突出
            case EAST:
                return new Location(world, x + 1.0625, y + 0.5, z + 0.5);
            case WEST:
                return new Location(world, x - 0.0625, y + 0.5, z + 0.5);
            default:
                return block.getLocation().add(0.5, 0.5, 0.5);
        }
    }

    private boolean hasItemFrameAtLocation(Location location, BlockFace face) {
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 0.3, 0.3, 0.3);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof ItemFrame) {
                ItemFrame existingFrame = (ItemFrame) entity;
                if (isSimilarLocation(existingFrame.getLocation(), location, 0.2) &&
                        existingFrame.getFacing() == face) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSimilarLocation(Location loc1, Location loc2, double tolerance) {
        return Math.abs(loc1.getX() - loc2.getX()) <= tolerance &&
                Math.abs(loc1.getY() - loc2.getY()) <= tolerance &&
                Math.abs(loc1.getZ() - loc2.getZ()) <= tolerance;
    }

    private void consumeItems(Player player, ItemStack mainHand) {
        if (mainHand.getAmount() > 1) {
            mainHand.setAmount(mainHand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }
}
