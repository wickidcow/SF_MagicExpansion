package io.Yomicer.magicExpansion.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapUtils {
    private static final Gson gson = new Gson();
    private static final LegacyComponentSerializer LEGACY_MESSAGES = LegacyComponentSerializer.legacySection();

    /**
     * Preserve historical section-sign colors at the player-message boundary.
     * An embedded legacy code in a dynamic failure message must retain its meaning.
     */
    static Component coloredFeedback(char code, String message) {
        return LEGACY_MESSAGES.deserialize("\u00a7" + code + message);
    }

    private static void sendError(Player player, String message) {
        player.sendMessage(coloredFeedback('c', message));
    }

    private static void sendSuccess(Player player, String message) {
        player.sendMessage(coloredFeedback('a', message));
    }

    public static void exportMap(Player player) {
        Location p1 = Events.point1.get(player.getUniqueId());
        Location p2 = Events.point2.get(player.getUniqueId());

        if (p1 == null || p2 == null) {
            player.sendMessage("Please select two points first!");
            return;
        }
        if (p1.getWorld() != p2.getWorld()) {
            sendError(player, "Both selection points must be in the same world.");
            return;
        }

        List<BlockData> blocks = getBlocksInRegion(p1, p2);
        String fileName = "map_" + System.currentTimeMillis() + ".json";
        File file = new File(MagicExpansion.getInstance().getDataFolder(), "maps/" + fileName);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(blocks, writer);
            player.sendMessage("Map exported successfully: " + fileName);
        } catch (IOException e) {
            player.sendMessage("Failed to export map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void pasteMap(Player player, String fileName) {
        File file = new File(MagicExpansion.getInstance().getDataFolder(), "maps/" + fileName + ".json");

        if (!file.exists()) {
            sendError(player, "File not found!");
            return;
        }

        Block targetBlock = getTargetBlock(player);
        if (targetBlock == null) {
            sendError(player, "You need to look at a block!");
            return;
        }

        Location pasteLocation = targetBlock.getLocation().add(0, 1, 0);

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<BlockData>>() {}.getType();
            List<BlockData> blocks = gson.fromJson(reader, listType);
            if (blocks == null || blocks.isEmpty()) {
                sendError(player, "The map file is empty or invalid.");
                return;
            }

            if (pasteBlocks(player, pasteLocation, blocks)) {
                sendSuccess(player, "Map pasted successfully above the target block!");
            }
        } catch (IOException | RuntimeException failure) {
            sendError(player, "Failed to paste map: " + failure.getMessage());
        }
    }

    public static boolean pasteBlocks(
            Player player, Location pasteLocation, List<BlockData> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            sendError(player, "There are no blocks to paste.");
            return false;
        }

        List<BlockPasteSupport.Target> targets = preparePasteTargets(player, pasteLocation, blocks);
        if (targets == null) {
            return false;
        }

        BlockPasteSupport.Outcome outcome = BlockPasteSupport.apply(targets);
        if (outcome.success()) {
            return true;
        }
        if (outcome.rollbackFailures() == 0) {
            sendError(player, "Map paste failed and all attempted block changes were rolled back.");
        } else {
            sendError(player, "Map paste failed; "
                    + outcome.rollbackFailures()
                    + " block(s) could not be restored. Ask an administrator to inspect the area.");
        }
        return false;
    }

    private static List<BlockPasteSupport.Target> preparePasteTargets(
            Player player, Location pasteLocation, List<BlockData> blocks) {
        if (pasteLocation.getWorld() == null) {
            sendError(player, "The target world is unavailable.");
            return null;
        }

        List<BlockPasteSupport.Target> targets = new ArrayList<>(blocks.size());
        Set<String> seen = new HashSet<>();

        for (BlockData blockData : blocks) {
            if (blockData == null || blockData.blockState == null || blockData.blockState.isEmpty()) {
                sendError(player, "The map file contains an invalid block entry.");
                return null;
            }

            int x = pasteLocation.getBlockX() + blockData.x;
            int y = pasteLocation.getBlockY() + blockData.y;
            int z = pasteLocation.getBlockZ() + blockData.z;

            if (y < pasteLocation.getWorld().getMinHeight()
                    || y >= pasteLocation.getWorld().getMaxHeight()) {
                sendError(player, "Part of the map would be outside the world height limit.");
                return null;
            }

            String key = x + ":" + y + ":" + z;
            if (!seen.add(key)) {
                sendError(player, "The map file contains duplicate block coordinates.");
                return null;
            }

            if (!pasteLocation.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                sendError(player, "Load the entire destination area before pasting.");
                return null;
            }

            Location location = new Location(pasteLocation.getWorld(), x, y, z);
            if (!ItemPermissionUtils.hasPermissionPoint(player, location)) {
                sendError(player, "Part of the destination is protected.");
                return null;
            }
            if (StorageCacheUtils.getSlimefunItem(location) != null) {
                sendError(player, "The destination contains a Slimefun block. Move or remove it normally first.");
                return null;
            }

            org.bukkit.block.data.BlockData desired;
            try {
                desired = Bukkit.createBlockData(blockData.blockState);
            } catch (RuntimeException invalidState) {
                sendError(player, "The map contains invalid block-state data.");
                return null;
            }

            Block block = location.getBlock();
            BlockState original = block.getState();
            // Replacing a block entity may eject contents before a later failure.
            // A snapshot cannot safely undo those external drops as well as the block.
            if (original instanceof TileState) {
                sendError(player, "The destination contains a block entity. Move or remove it normally first.");
                return null;
            }
            targets.add(new BlockPasteSupport.Target(block, original, desired));
        }

        return targets;
    }

    private static Block getTargetBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 10);
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!block.getType().isAir()) {
                return block;
            }
        }
        return null;
    }

    public static List<BlockData> getBlocksInRegion(Location p1, Location p2) {
        List<BlockData> blocks = new ArrayList<>();
        if (p1.getWorld() == null || p1.getWorld() != p2.getWorld()) {
            return blocks;
        }
        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = p1.getWorld().getBlockAt(x, y, z);

                    if (block.getType() == Material.AIR) {
                        continue;
                    }

                    String blockState = block.getBlockData().getAsString();
                    blocks.add(new BlockData(
                            x - minX,
                            y - minY,
                            z - minZ,
                            block.getType().name(),
                            blockState
                    ));
                }
            }
        }
        return blocks;
    }

    public static class BlockData {
        public int x, y, z;
        public String type;
        public String blockState;

        public BlockData(int x, int y, int z, String type, String blockState) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.blockState = blockState;
        }

        // 默认构造函数
        public BlockData() {}
    }
}
