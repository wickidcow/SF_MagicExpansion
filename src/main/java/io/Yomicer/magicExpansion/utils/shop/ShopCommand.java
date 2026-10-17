package io.Yomicer.magicExpansion.utils.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/** Player browsing is separate from catalog administration and opening an NPC shop for another player. */
public class ShopCommand implements CommandExecutor, TabCompleter {
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { help(sender, label); return true; }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("generate")) {
            if (args.length != 1) { help(sender, label); return true; }
            MarketCatalog.populate(sender);
            return true;
        }
        if (action.equals("openfor") || action.equals("blackmarketfor")) {
            if (!sender.hasPermission("magicexpansion.shop.open.others")) {
                sender.sendMessage("You do not have permission to open shops for other players.");
                return true;
            }
            if ((action.equals("openfor") && args.length < 3) || (action.equals("blackmarketfor") && args.length != 2)) {
                help(sender, label);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage("That player is not online."); return true; }
            if (action.equals("blackmarketfor")) ShopGUI.openBlackMarket(target);
            else open(sender, target, join(args, 2));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Use openfor <player> <shop>, blackmarketfor <player>, or generate from the console.");
            return true;
        }
        switch (action) {
            case "open" -> {
                if (args.length == 1) ShopGUI.openPlayerMainMenu(player);
                else open(sender, player, join(args, 1));
            }
            case "admin" -> {
                if (player.hasPermission("magicexpansion.shop.admin")) ShopGUI.openAdminMainMenu(player);
                else player.sendMessage("You do not have permission to administer shops.");
            }
            case "blackmarket" -> ShopGUI.openBlackMarket(player);
            default -> help(sender, label);
        }
        return true;
    }

    private static void open(CommandSender sender, Player target, String name) {
        ShopManager.Shop shop = ShopManager.getShop(name);
        if (shop == null || shop.trades.stream().noneMatch(t -> t.result != null && CatalogItems.isAvailable(t))) {
            sender.sendMessage("Shop not found, empty, or unavailable: " + name);
            return;
        }
        ShopGUI.openShopTrades(target, shop.name, 0);
    }
    private static String join(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
    private static void help(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " open [shop name] - Browse the market or a named fixed shop");
        sender.sendMessage("/" + label + " blackmarket - Open the separate rotating mystery market");
        if (sender.hasPermission("magicexpansion.shop.admin")) {
            sender.sendMessage("/" + label + " admin - Shop administration");
            sender.sendMessage("/" + label + " generate - Add missing catalog offers; retain purchase history");
        }
        if (sender.hasPermission("magicexpansion.shop.open.others")) {
            sender.sendMessage("/" + label + " openfor <player> <shop name> - NPC fixed shop");
            sender.sendMessage("/" + label + " blackmarketfor <player> - NPC rotating market");
        }
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return List.of();
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(List.of("open", "blackmarket"));
            if (sender.hasPermission("magicexpansion.shop.admin")) result.addAll(List.of("admin", "generate"));
            if (sender.hasPermission("magicexpansion.shop.open.others")) result.addAll(List.of("openfor", "blackmarketfor"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("openfor") || args[0].equalsIgnoreCase("blackmarketfor"))
                && sender.hasPermission("magicexpansion.shop.open.others")) {
            for (Player player : Bukkit.getOnlinePlayers()) result.add(player.getName());
        } else if (args[0].equalsIgnoreCase("open") || (args[0].equalsIgnoreCase("openfor")
                && sender.hasPermission("magicexpansion.shop.open.others"))) {
            int start = args[0].equalsIgnoreCase("open") ? 1 : 2;
            String typed = join(args, start).toLowerCase(Locale.ROOT);
            String previous = String.join(" ", Arrays.copyOfRange(args, start, args.length - 1));
            int offset = previous.isEmpty() ? 0 : previous.length() + 1;
            for (ShopManager.Shop shop : ShopManager.getShops()) {
                if (shop.name.toLowerCase(Locale.ROOT).startsWith(typed)) result.add(shop.name.substring(offset));
            }
            return result;
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return result.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
