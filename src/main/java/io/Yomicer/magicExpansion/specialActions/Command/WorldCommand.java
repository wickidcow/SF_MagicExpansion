package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldCommand implements CommandExecutor, TabCompleter {

    private final MagicExpansion plugin;

    public WorldCommand(MagicExpansion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // 权限检查
        if (!player.isOp() && !player.hasPermission("MagicExpansion.use")) {
            player.sendMessage("§cYou do not have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§bUsage: /mxw <select|exitselect|exportmap|paste|clear|ctrlv>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "select":
                Events.selectMode.put(player.getUniqueId(), true);
                player.sendMessage("§aYou are now in selection mode.");
                break;

            case "exitselect":
                Events.selectMode.remove(player.getUniqueId());
                player.sendMessage("§aExited selection mode.");
                break;

            case "exportmap":
                MapUtils.exportMap(player);
                break;

            case "paste":
                // 【B1修复】paste 子命令需要 magicexpansion.world.modify 权限，无权限提示并 return
                if (!player.hasPermission("magicexpansion.world.modify")) {
                    player.sendMessage("§c你没有权限修改世界！(需要 magicexpansion.world.modify)");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mxw paste <filename>");
                    return true;
                }
                MapUtils.pasteMap(player, args[1]);
                // 【B3修复】记录 paste 操作日志：操作者名与所粘贴的地图文件
                Bukkit.getLogger().info("[MagicExpansion] " + player.getName()
                        + " pasted map file '" + args[1] + "'");
                break;

            case "clear":
                // 【B1修复】clear 子命令需要 magicexpansion.world.modify 权限，无权限提示并 return
                if (!player.hasPermission("magicexpansion.world.modify")) {
                    player.sendMessage("§c你没有权限修改世界！(需要 magicexpansion.world.modify)");
                    return true;
                }
                clearSelectedArea(player);
                break;

            case "ctrlv":
                // 【B1修复】ctrlv 与 paste 同为粘贴/修改世界功能，同样需要 magicexpansion.world.modify 权限
                if (!player.hasPermission("magicexpansion.world.modify")) {
                    player.sendMessage("§c你没有权限修改世界！(需要 magicexpansion.world.modify)");
                    return true;
                }
                pasteSelectedAreaToTargetBlock(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown command!");
        }
        return true;
    }


    /**
     * 清除所选区域的方块
     */
    private void clearSelectedArea(Player player) {
        Location p1 = Events.point1.get(player.getUniqueId());
        Location p2 = Events.point2.get(player.getUniqueId());

        if (p1 == null || p2 == null) {
            player.sendMessage(ChatColor.RED + "Please select two points first!");
            return;
        }

        // 【B2修复】校验两个选点必须位于同一世界，否则提示并 return
        if (p1.getWorld() == null || !p1.getWorld().equals(p2.getWorld())) {
            player.sendMessage(ChatColor.RED + "The two selected points must be in the same world!");
            return;
        }

        // 【B2修复】体积上限校验：区域尺寸不得超过 32*256*32，超限提示并 return
        int sizeX = Math.abs(p1.getBlockX() - p2.getBlockX()) + 1;
        int sizeY = Math.abs(p1.getBlockY() - p2.getBlockY()) + 1;
        int sizeZ = Math.abs(p1.getBlockZ() - p2.getBlockZ()) + 1;
        if ((long) sizeX * sizeY * sizeZ > 32L * 256L * 32L) {
            player.sendMessage(ChatColor.RED + "Selected area too large! Max: 32x256x32, current: "
                    + sizeX + "x" + sizeY + "x" + sizeZ);
            return;
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
                    block.setType(org.bukkit.Material.AIR); // 将方块设置为空气
                }
            }
        }

        player.sendMessage("§aCleared the selected area.");

        // 【B3修复】记录清除操作日志：操作者名 + 区域坐标范围
        Bukkit.getLogger().info("[MagicExpansion] " + player.getName() + " cleared area in world '"
                + p1.getWorld().getName() + "' from (" + minX + "," + minY + "," + minZ
                + ") to (" + maxX + "," + maxY + "," + maxZ + ")");
    }

    /**
     * 将所选区域粘贴到玩家注视的方块上方 1 格
     */
    private void pasteSelectedAreaToTargetBlock(Player player) {
        Location p1 = Events.point1.get(player.getUniqueId());
        Location p2 = Events.point2.get(player.getUniqueId());

        if (p1 == null || p2 == null) {
            player.sendMessage("§cPlease select two points first!");
            return;
        }

        // 【B2修复】校验两个选点必须位于同一世界，否则提示并 return
        if (p1.getWorld() == null || !p1.getWorld().equals(p2.getWorld())) {
            player.sendMessage("§cThe two selected points must be in the same world!");
            return;
        }

        // 【B2修复】体积上限校验：区域尺寸不得超过 32*256*32，超限提示并 return
        int sizeX = Math.abs(p1.getBlockX() - p2.getBlockX()) + 1;
        int sizeY = Math.abs(p1.getBlockY() - p2.getBlockY()) + 1;
        int sizeZ = Math.abs(p1.getBlockZ() - p2.getBlockZ()) + 1;
        if ((long) sizeX * sizeY * sizeZ > 32L * 256L * 32L) {
            player.sendMessage("§cSelected area too large! Max: 32x256x32, current: "
                    + sizeX + "x" + sizeY + "x" + sizeZ);
            return;
        }

        // 获取玩家注视的方块
        Block targetBlock = getTargetBlock(player);
        if (targetBlock == null) {
            player.sendMessage("§cYou need to look at a block!");
            return;
        }

        Location pasteLocation = targetBlock.getLocation().add(0, 1, 0); // 粘贴到目标方块上方 1 格

        List<MapUtils.BlockData> blocks = MapUtils.getBlocksInRegion(p1, p2);
        for (MapUtils.BlockData blockData : blocks) {
            Location location = new Location(
                    player.getWorld(),
                    pasteLocation.getBlockX() + blockData.x,
                    pasteLocation.getBlockY() + blockData.y,
                    pasteLocation.getBlockZ() + blockData.z
            );

            if (blockData.blockState != null && !blockData.blockState.isEmpty()) {
                // 如果有 blockState，则使用它来设置方块数据
                try {
                    location.getBlock().setBlockData(Bukkit.createBlockData(blockData.blockState));
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cError setting block data: " + e.getMessage());
                }
            } else {
                // 如果没有 blockState，则仅设置方块类型
                location.getBlock().setType(Material.valueOf(blockData.type));
            }
        }

        player.sendMessage("§aPasted the selected area above the target block.");

        // 【B3修复】记录粘贴操作日志：操作者名 + 源区域坐标范围与目标世界
        Bukkit.getLogger().info("[MagicExpansion] " + player.getName() + " pasted selected region ("
                + p1.getBlockX() + "," + p1.getBlockY() + "," + p1.getBlockZ() + ") to ("
                + p2.getBlockX() + "," + p2.getBlockY() + "," + p2.getBlockZ() + ") from world '"
                + p1.getWorld().getName() + "' into world '" + player.getWorld().getName() + "'");
    }

    /**
     * 获取玩家注视的方块
     */
    private Block getTargetBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 10); // 最远检测 5 格
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!block.getType().isAir()) {
                return block;
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // 提供第一级补全：子命令
            suggestions.addAll(Arrays.asList("select", "exitselect", "exportmap", "paste", "clear", "ctrlv"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("paste")) {
            // 如果是 "paste" 子命令，提供文件名补全
            File mapsDir = new File(plugin.getDataFolder(), "maps");
            if (mapsDir.exists() && mapsDir.isDirectory()) {
                for (File file : mapsDir.listFiles()) {
                    if (file.getName().endsWith(".json")) {
                        suggestions.add(file.getName().replace(".json", ""));
                    }
                }
            }
        }

        // 过滤补全建议，只显示与当前输入匹配的内容
        return filterSuggestions(suggestions, args[args.length - 1]);
    }

    private List<String> filterSuggestions(List<String> suggestions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }

}
