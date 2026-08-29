package io.Yomicer.magicExpansion.utils.preBuildingUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
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
import java.util.List;

public class PreBuildingsTreeUtils {

    private static final Gson gson = new Gson();

    /**
     * 修复(Z)：建筑文件名白名单校验 [a-zA-Z0-9_-]，防止路径穿越读取任意资源
     */
    private static boolean isValidFileName(String fileName) {
        return fileName != null && fileName.matches("[a-zA-Z0-9_-]+");
    }

    /**
     * 粘贴地图到玩家注视的目标方块上方
     */
    public static boolean pasteMap(Player player, String fileName, String originName, String replaceName) {
        // 修复(Z)：文件名白名单校验，非法文件名直接拒绝
        if (!isValidFileName(fileName)) {
            player.sendMessage("§c非法的文件名，请不要随意修改插件设置!");
            return false;
        }

        // 从 resources/buildings 文件夹中读取 JSON 文件
        InputStream inputStream = PreBuildingsTreeUtils.class.getClassLoader().getResourceAsStream("buildings/" + fileName + ".json");

        if (inputStream == null) {
            player.sendMessage("§c路径错误，请不要随意修改插件设置!");
            return false; // 文件不存在，返回 false
        }

        // 获取玩家注视的目标方块
        Block targetBlock = getTargetBlock(player);
        if (targetBlock == null) {
            player.sendMessage("§c你需要看向一个方块!");
            return false; // 没有目标方块，返回 false
        }

        // 粘贴位置为目标方块上方 1 格
        Location pasteLocation = targetBlock.getLocation().add(0, 1, 0);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Type listType = new TypeToken<List<BlockData>>() {}.getType();
            List<BlockData> blocks = gson.fromJson(reader, listType);

            // 修复(Z)：gson.fromJson 可能返回 null（空文件/非法 JSON），判空防止 NPE
            if (blocks == null) {
                player.sendMessage("§c建筑数据为空或格式错误: " + fileName + ".json");
                return false;
            }

            if (!(originName == null) && !(replaceName == null)) {
                // 替换 "OAK" 为 "CHERRY"
                for (BlockData blockData : blocks) {
                    if (blockData.type != null && blockData.type.contains(originName)) {
                        blockData.type = blockData.type.replace(originName, replaceName);
                    }
                }
            }

            // 检查预期空间是否有权限冲突&&空间是否充足
            if (hasPermissionConflicts(blocks, pasteLocation, player.getWorld(),player)) {
                player.sendMessage("§c有部分区域没有权限或者是空间大小不足，请选择其他地方放置。");
                return false; // 存在冲突，返回 false
            }

//            // 检查是否有冲突
//            if (hasConflicts(blocks, pasteLocation, player.getWorld())) {
//                player.sendMessage("§c当前空间大小不足，请选择其他地方放置。");
//                return false; // 存在冲突，返回 false
//            }

            // 如果没有冲突，则开始粘贴
            for (BlockData blockData : blocks) {
                Location location = new Location(
                        player.getWorld(),
                        pasteLocation.getBlockX() + blockData.x,
                        pasteLocation.getBlockY() + blockData.y,
                        pasteLocation.getBlockZ() + blockData.z
                );

                try {
                    if (blockData.blockState != null && !blockData.blockState.isEmpty()) {
                        location.getBlock().setBlockData(Bukkit.createBlockData(blockData.blockState));
                    } else {
                        location.getBlock().setType(Material.valueOf(blockData.type));
                    }
                } catch (IllegalArgumentException e) {
                    location.getBlock().setType(Material.GLASS);
                }
            }


            player.sendMessage("§a预制菜启动！");
            return true; // 成功粘贴，返回 true
        } catch (IOException e) {
            player.sendMessage("§cFailed to create: " + e.getMessage());
            e.printStackTrace();
            return false; // 发生异常，返回 false
        } catch (com.google.gson.JsonParseException e) {
            // 修复(Z)：捕获 JSON 解析异常（含 JsonSyntaxException），避免损坏文件导致未捕获异常
            player.sendMessage("§c建筑文件已损坏或不是合法 JSON: " + fileName + ".json");
            return false;
        }
    }

    public static boolean pasteMap(Player player, String fileName) {
        return pasteMap(player, fileName, null, null);
    }

    /**
     * 检查是否有权限冲突
     */
    private static boolean hasPermissionConflicts(List<BlockData> blocks, Location pasteLocation, org.bukkit.World world, Player p) {
        for (BlockData blockData : blocks) {
            Location location = new Location(
                    world,
                    pasteLocation.getBlockX() + blockData.x,
                    pasteLocation.getBlockY() + blockData.y,
                    pasteLocation.getBlockZ() + blockData.z
            );
            if(!ItemPermissionUtils.hasPermissionPoint(p,location)){
                return true; //发现冲突
            }

            Block block = location.getBlock();
            if (!block.getType().isAir()) {
                return true; // 发现冲突
            }
        }
        return false; // 没有冲突
    }

    /**
     * 检查是否有冲突
     */
    private static boolean hasConflicts(List<BlockData> blocks, Location pasteLocation, org.bukkit.World world) {
        for (BlockData blockData : blocks) {
            Location location = new Location(
                    world,
                    pasteLocation.getBlockX() + blockData.x,
                    pasteLocation.getBlockY() + blockData.y,
                    pasteLocation.getBlockZ() + blockData.z
            );

            Block block = location.getBlock();
            if (!block.getType().isAir()) {
                return true; // 发现冲突
            }
        }
        return false; // 没有冲突
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
        return null; // 如果没有找到非空气方块，返回 null
    }

    /**
     * 内部类：存储单个方块的数据
     */
    private static class BlockData {
        public int x;
        public int y;
        public int z;
        public String type;
        public String blockState; // 新增字段，用于保存方块状态

        // 默认构造函数
        public BlockData() {}

        // 如果需要的话，可以添加带参数的构造函数
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
     * @return int[]{xSize, ySize, zSize}，如果文件不存在或出错返回 null
     */
    public static int[] getBuildingDimensions(String fileName) {
        // 修复(Z)：文件名白名单校验，非法文件名返回 null
        if (!isValidFileName(fileName)) {
            return null;
        }

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
