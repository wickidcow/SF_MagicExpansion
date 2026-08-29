package io.Yomicer.magicExpansion.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapUtils {
    private static final Gson gson = new Gson();

    public static void exportMap(Player player) {
        Location p1 = Events.point1.get(player.getUniqueId());
        Location p2 = Events.point2.get(player.getUniqueId());

        if (p1 == null || p2 == null) {
            player.sendMessage("Please select two points first!");
            return;
        }

        List<BlockData> blocks;
        try {
            // 修复(Y)：getBlocksInRegion 内置体积上限校验，捕获异常提示玩家（防止超大区域拖垮服务器）
            blocks = getBlocksInRegion(p1, p2);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "导出失败: " + e.getMessage());
            return;
        }
        String fileName = "map_" + System.currentTimeMillis() + ".json";
        File file = new File(player.getServer().getPluginManager().getPlugin("MagicExpansion").getDataFolder(), "maps/" + fileName);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(blocks, writer);
            player.sendMessage("Map exported successfully: " + fileName);
        } catch (IOException e) {
            player.sendMessage("Failed to export map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void pasteMap(Player player, String fileName) {
        // 修复(Y)：文件名白名单校验 [a-zA-Z0-9_-]，防止路径穿越（如 ../../config）读取任意文件
        if (fileName == null || !fileName.matches("[a-zA-Z0-9_-]+")) {
            player.sendMessage(ChatColor.RED + "非法的文件名！");
            return;
        }
        File file = new File(player.getServer().getPluginManager().getPlugin("MagicExpansion").getDataFolder(), "maps/" + fileName + ".json");

        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "File not found!");
            return;
        }

        Block targetBlock = getTargetBlock(player);
        if (targetBlock == null) {
            player.sendMessage(ChatColor.RED + "You need to look at a block!");
            return;
        }

        Location pasteLocation = targetBlock.getLocation().add(0, 1, 0);

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<BlockData>>() {}.getType();
            List<BlockData> blocks = gson.fromJson(reader, listType);

            // 修复(Y)：gson.fromJson 可能返回 null（空文件/非法 JSON），判空防止 NPE
            if (blocks == null) {
                player.sendMessage(ChatColor.RED + "Failed to paste map: 地图数据为空或格式错误！");
                return;
            }

            for (BlockData blockData : blocks) {
                Location location = new Location(
                        player.getWorld(),
                        pasteLocation.getBlockX() + blockData.x,
                        pasteLocation.getBlockY() + blockData.y,
                        pasteLocation.getBlockZ() + blockData.z
                );
                location.getBlock().setBlockData(Bukkit.createBlockData(blockData.blockState));
            }

            player.sendMessage(ChatColor.GREEN + "Map pasted successfully above the target block!");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to paste map: " + e.getMessage());
            e.printStackTrace();
        } catch (com.google.gson.JsonParseException e) {
            // 修复(Y)：捕获 JSON 解析异常，避免恶意/损坏文件导致未捕获异常
            player.sendMessage(ChatColor.RED + "Failed to paste map: 地图文件已损坏或不是合法 JSON！");
        }
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
        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        // 修复(Y)：体积上限 32x256x32 保护，防止选区过大导致内存溢出/服务器卡死
        long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > 32L * 256L * 32L) {
            throw new IllegalArgumentException("选区过大（体积 " + volume + " 超过上限 32x256x32），请缩小选区！");
        }

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
