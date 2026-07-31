package io.Yomicer.magicExpansion.utils.shop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("this player!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Portable Shop command help:");
            sender.sendMessage(ChatColor.AQUA + "/" + label + " open " + ChatColor.GRAY + "- Open the player shop");
            sender.sendMessage(ChatColor.AQUA + "/" + label + " admin " + ChatColor.GRAY + "- openShop (requires)");
            return true;
        }

        if (args[0].equalsIgnoreCase("open")) {
            ShopGUI.openPlayerMainMenu(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("admin")) {
            if (player.hasPermission("magicexpansion.shop.admin")) {
                ShopGUI.openAdminMainMenu(player);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command argument. Use /" + label + " to view help.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("open");
            if (sender.hasPermission("magicexpansion.shop.admin")) {
                completions.add("admin");
            }
        }

        // 过滤出当前输入的前缀匹配项
        List<String> filtered = new ArrayList<>();
        for (String s : completions) {
            if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                filtered.add(s);
            }
        }

        return filtered;
    }
}
