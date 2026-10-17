package io.Yomicer.magicExpansion.specialActions.Command;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.MapUtils;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
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
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mxw paste <filename>");
                    return true;
                }
                MapUtils.pasteMap(player, args[1]);
                break;

            case "clear":
                clearSelectedArea(player);
                break;

            case "ctrlv":
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
        if (p1.getWorld() == null || p1.getWorld() != p2.getWorld()) {
            player.sendMessage(ChatColor.RED + "Both selection points must be in the same world.");
            return;
        }

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        // Preflight the complete selection before changing even one block.
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!p1.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                        player.sendMessage(ChatColor.RED + "Load the entire selected area before clearing it.");
                        return;
                    }
                    Location location = new Location(p1.getWorld(), x, y, z);
                    if (!ItemPermissionUtils.hasPermissionPoint(player, location)) {
                        player.sendMessage(ChatColor.RED + "Part of the selected area is protected.");
                        return;
                    }
                    if (StorageCacheUtils.getSlimefunItem(location) != null) {
                        player.sendMessage(ChatColor.RED
                                + "The selection contains a Slimefun block. Remove it normally first.");
                        return;
                    }
                }
            }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    p1.getWorld().getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }

        player.sendMessage("§aCleared the selected area.");
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
        if (p1.getWorld() == null || p1.getWorld() != p2.getWorld()) {
            player.sendMessage(ChatColor.RED + "Both selection points must be in the same world.");
            return;
        }

        Block targetBlock = getTargetBlock(player);
        if (targetBlock == null) {
            player.sendMessage("§cYou need to look at a block!");
            return;
        }

        Location pasteLocation = targetBlock.getLocation().add(0, 1, 0);
        List<MapUtils.BlockData> blocks = MapUtils.getBlocksInRegion(p1, p2);
        if (MapUtils.pasteBlocks(player, pasteLocation, blocks)) {
            player.sendMessage("§aPasted the selected area above the target block.");
        }
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
            // 提供第一级补全:子命令
            suggestions.addAll(Arrays.asList("select", "exitselect", "exportmap", "paste", "clear", "ctrlv"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("paste")) {
            // 如果是 "paste" 子命令,提供文件名补全
            File mapsDir = new File(plugin.getDataFolder(), "maps");
            if (mapsDir.exists() && mapsDir.isDirectory()) {
                for (File file : mapsDir.listFiles()) {
                    if (file.getName().endsWith(".json")) {
                        suggestions.add(file.getName().replace(".json", ""));
                    }
                }
            }
        }

        // 过滤补全建议,只显示与当前输入匹配的内容
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
