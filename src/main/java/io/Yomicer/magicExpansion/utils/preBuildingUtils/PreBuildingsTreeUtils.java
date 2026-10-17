package io.Yomicer.magicExpansion.utils.preBuildingUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.Yomicer.magicExpansion.utils.BlockPasteSupport;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreBuildingsTreeUtils {

    private static final Gson gson = new Gson();

    /**
     * 粘贴地图到玩家注视的目标方块上方
     */
    public static boolean pasteMap(
            Player player, String fileName, String originName, String replaceName) {
        InputStream inputStream = PreBuildingsTreeUtils.class.getClassLoader()
                .getResourceAsStream("buildings/" + fileName + ".json");

        if (inputStream == null) {
            player.sendMessage("§cThis prefabricated structure is not configured correctly!");
            return false;
        }

        Block targetBlock = getTargetBlock(player);
        if (targetBlock == null) {
            player.sendMessage("§cYou need to look at a block!");
            return false;
        }

        Location pasteLocation = targetBlock.getLocation().add(0, 1, 0);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Type listType = new TypeToken<List<BlockData>>() {}.getType();
            List<BlockData> blocks = gson.fromJson(reader, listType);
            if (blocks == null || blocks.isEmpty()) {
                player.sendMessage("§cThe prefabricated structure file is empty or invalid.");
                return false;
            }

            if (originName != null && replaceName != null) {
                for (BlockData blockData : blocks) {
                    if (blockData != null
                            && blockData.type != null
                            && blockData.type.contains(originName)) {
                        blockData.type = blockData.type.replace(originName, replaceName);
                    }
                }
            }

            List<BlockPasteSupport.Target> targets = prepareTargets(blocks, pasteLocation, player);
            if (targets == null) {
                return false;
            }

            BlockPasteSupport.Outcome outcome = BlockPasteSupport.apply(targets);
            if (!outcome.success()) {
                if (outcome.rollbackFailures() == 0) {
                    player.sendMessage("§cThe structure could not be placed safely; all attempted changes were rolled back.");
                } else {
                    player.sendMessage("§cStructure placement failed; " + outcome.rollbackFailures()
                            + " block(s) could not be restored. Ask an administrator to inspect the area.");
                }
                return false;
            }

            player.sendMessage("§aPrefabricated structure placed successfully!");
            return true;
        } catch (IOException | RuntimeException failure) {
            player.sendMessage("§cFailed to create structure: " + failure.getMessage());
            return false;
        }
    }

    private static List<BlockPasteSupport.Target> prepareTargets(
            List<BlockData> blocks, Location pasteLocation, Player player) {
        org.bukkit.World world = pasteLocation.getWorld();
        if (world == null) {
            player.sendMessage("§cThe target world is unavailable.");
            return null;
        }

        List<BlockPasteSupport.Target> targets = new ArrayList<>(blocks.size());
        Set<String> coordinates = new HashSet<>();

        for (BlockData blockData : blocks) {
            if (blockData == null) {
                player.sendMessage("§cThe structure file contains an invalid block entry.");
                return null;
            }

            int x = pasteLocation.getBlockX() + blockData.x;
            int y = pasteLocation.getBlockY() + blockData.y;
            int z = pasteLocation.getBlockZ() + blockData.z;

            if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                player.sendMessage("§cPart of this structure would be outside the world height limit.");
                return null;
            }

            String coordinateKey = x + ":" + y + ":" + z;
            if (!coordinates.add(coordinateKey)) {
                player.sendMessage("§cThe structure file contains duplicate block coordinates.");
                return null;
            }

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                player.sendMessage("§cLoad the entire target area before placing this structure.");
                return null;
            }

            Location location = new Location(world, x, y, z);
            if (!ItemPermissionUtils.hasPermissionPoint(player, location)) {
                player.sendMessage("§cPart of the target area is protected.");
                return null;
            }

            Block block = location.getBlock();
            if (!block.getType().isAir()) {
                player.sendMessage("§cThere is not enough empty space for this structure.");
                return null;
            }

            org.bukkit.block.data.BlockData desired;
            try {
                if (blockData.blockState != null && !blockData.blockState.isEmpty()) {
                    desired = Bukkit.createBlockData(blockData.blockState);
                } else {
                    Material material = Material.valueOf(blockData.type);
                    if (!material.isBlock()) {
                        throw new IllegalArgumentException(material + " is not a block");
                    }
                    desired = material.createBlockData();
                }
            } catch (RuntimeException invalidState) {
                // Preserve the historical fallback, but decide it before any world mutation.
                desired = Material.GLASS.createBlockData();
            }

            targets.add(new BlockPasteSupport.Target(block, block.getState(), desired));
        }

        return targets;
    }

    public static boolean pasteMap(Player player, String fileName) {
        return pasteMap(player, fileName, null, null);
    }

    /**
     * 获取玩家注视的目标方块
     */
    private static Block getTargetBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 10); // 最远检测 5 格
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!block.getType().isAir()) {
                return block;
            }
        }
        return null; // 如果没有找到非空气方块,返回 null
    }

    /**
     * 内部类:存储单个方块的数据
     */
    private static class BlockData {
        public int x;
        public int y;
        public int z;
        public String type;
        public String blockState; // 新增字段,用于保存方块状态

        // 默认构造函数
        public BlockData() {}

        // 如果需要的话,可以添加带参数的构造函数
        public BlockData(int x, int y, int z, String type, String blockState) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.blockState = blockState;
        }
    }


    /**
     * 获取建筑的尺寸 (X, Y, Z)
     * @param fileName 文件名 (不带 .json)
     * @return int[]{xSize, ySize, zSize},如果文件不存在或出错返回 null
     */
    public static int[] getBuildingDimensions(String fileName) {
        InputStream inputStream = PreBuildingsTreeUtils.class.getClassLoader().getResourceAsStream("buildings/" + fileName + ".json");
        if (inputStream == null) {
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Type listType = new TypeToken<List<BlockData>>() {}.getType();
            List<BlockData> blocks = gson.fromJson(reader, listType);

            if (blocks == null || blocks.isEmpty()) {
                return null;
            }

            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

            for (BlockData block : blocks) {
                if (block.x < minX) minX = block.x;
                if (block.x > maxX) maxX = block.x;

                if (block.y < minY) minY = block.y;
                if (block.y > maxY) maxY = block.y;

                if (block.z < minZ) minZ = block.z;
                if (block.z > maxZ) maxZ = block.z;
            }

            // 计算尺寸 (最大值 - 最小值 + 1)
            int xSize = maxX - minX + 1;
            int ySize = maxY - minY + 1;
            int zSize = maxZ - minZ + 1;

            return new int[]{xSize, ySize, zSize};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }






}
