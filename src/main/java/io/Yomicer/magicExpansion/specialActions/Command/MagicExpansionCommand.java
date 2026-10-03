package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.MagicExpansionSlimefunItemCache;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MagicExpansionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§a/magicexpansion reload slimefun §f- Reload the Slimefun item cache");
            sender.sendMessage("§a/magicexpansion fishing §f- Show fishing compatibility status");
            sender.sendMessage("§a/magicexpansion reload fishing §f- Reload fishing compatibility settings");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "fishing":
                sender.sendMessage("§a" + MagicExpansion.getInstance().getFishingCompatibility().getStatus());
                break;
            case "reload":
                if (args.length == 2 && args[1].equalsIgnoreCase("slimefun")) {
                    MagicExpansionSlimefunItemCache.reloadCache();
                    sender.sendMessage("§aReloaded the Slimefun item cache.");
                } else if (args.length == 2 && args[1].equalsIgnoreCase("fishing")) {
                    MagicExpansion plugin = MagicExpansion.getInstance();
                    plugin.reloadConfig();
                    plugin.getFishingCompatibility().reload();
                    sender.sendMessage("§a" + plugin.getFishingCompatibility().getStatus());
                } else {
                    sender.sendMessage("§cUsage: /magicexpansion reload <slimefun|fishing>");
                }
                break;

            default:
                sender.sendMessage("§cUnknown subcommand. Use /magicexpansion for help.");
                break;
        }

        return true;
    }



}
