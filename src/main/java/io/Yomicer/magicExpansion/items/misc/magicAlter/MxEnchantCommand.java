package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MxEnchantCommand implements TabExecutor {

    private final MagicExpansion plugin;

    public MxEnchantCommand(MagicExpansion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§6/mxalter guide §7- 获取配方书");
            player.sendMessage("§6/mxalter open_guide §7- 打开配方书");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "guide":
                // 修复(N)：addItem 返回剩余物品时掉落地面，背包满不吞物品
                java.util.Map<Integer, ItemStack> overflow =
                        player.getInventory().addItem(plugin.getPluginInitializer().getRecipeBookManager().createRecipeBook());
                for (ItemStack rest : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), rest);
                }
                player.sendMessage("§a已获得魔法祭坛配方指南!");
                break;

            case "open_guide":
                plugin.getPluginInitializer().getRecipeBookManager().openRecipeBook(player);
                break;

            default:
                player.sendMessage("§c未知子命令! 使用 /mxenchant 查看可用命令");
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数补全
            String[] subCommands = {"open_guide", "guide"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }

        return completions;
    }
}
