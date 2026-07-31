package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.utils.MagicExpansionSlimefunItemCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MagicExpansionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // 如果没有参数,显示帮助信息
            sender.sendMessage("§a/magicexpansion reload slimefun §f- Reload the Slimefun item cache");
            return true;
        }

        // 检查权限(需要 OP 权限)
        if (!sender.isOp()) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        // 解析子命令
        switch (args[0].toLowerCase()) {
            case "reload":
                if (args.length == 2 && args[1].equalsIgnoreCase("slimefun")) {
                    // 清空缓存并重新加载所有 Slimefun 物品
                    MagicExpansionSlimefunItemCache.reloadCache();
                    sender.sendMessage("§aReloaded the Slimefun item cache.");
                } else {
                    sender.sendMessage("§cUsage: /magicexpansion reload slimefun");
                }
                break;

            default:
                sender.sendMessage("§cUnknown subcommand. Use /magicexpansion for help.");
                break;
        }

        return true;
    }



}
