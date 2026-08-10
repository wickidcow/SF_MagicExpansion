package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.MagicExpansionItemSetup;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.fish.FishingBook;
import io.Yomicer.magicExpansion.utils.FishingGuideMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FishingGuideCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查发送者是否是玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用这个命令！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open_guide":
                // 图鉴界面: /mxf open_guide <ZhiMeng|ShuiYunJian>
                if (args.length < 2) {
                    player.sendMessage("§e用法: /mxf open_guide <ZhiMeng|ShuiYunJian>");
                    player.sendMessage("§7ZhiMeng     - 打开织梦者钓鱼图鉴");
                    player.sendMessage("§7ShuiYunJian - 打开水云间钓鱼图鉴");
                    break;
                }
                switch (args[1].toLowerCase()) {
                    case "zhimeng" -> {
                        FishingGuideMenu.openMainMenu(player);
                        player.sendMessage("§a已打开织梦者钓鱼图鉴！");
                    }
                    case "shuiyunjian" -> player.sendMessage("§b水云间钓鱼图鉴制作中, 敬请期待！");
                    default -> player.sendMessage("§c未知图鉴: " + args[1] + " §7(可选: ZhiMeng / ShuiYunJian)");
                }
                break;

            case "guide":
                // 指南书: /mxf guide <ZhiMeng|ShuiYunJian>
                if (args.length < 2) {
                    player.sendMessage("§e用法: /mxf guide <ZhiMeng|ShuiYunJian>");
                    player.sendMessage("§7ZhiMeng     - 获取织梦者系列指南");
                    player.sendMessage("§7ShuiYunJian - 获取水云间系列指南");
                    break;
                }
                switch (args[1].toLowerCase()) {
                    case "zhimeng" -> {
                        giveItem(player, MagicExpansionItems.FISHING_BOOK);
                        player.sendMessage("§a已获得织梦者系列指南！");
                    }
                    case "shuiyunjian" -> {
                        giveItem(player, MagicExpansionItems.FISHING_BOOK_BETWEEN_WATER_CLOUD);
                        player.sendMessage("§a已获得水云间系列指南！");
                    }
                    default -> player.sendMessage("§c未知指南: " + args[1] + " §7(可选: ZhiMeng / ShuiYunJian)");
                }
                break;

            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数补全
            String[] subCommands = {"open_guide", "guide"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("guide") || args[0].equalsIgnoreCase("open_guide"))) {
            // 第二个参数补全: 指南/图鉴系列
            String[] guides = {"ZhiMeng", "ShuiYunJian"};
            for (String guide : guides) {
                if (guide.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(guide);
                }
            }
        }

        return completions;
    }

    /**
     * 发送命令用法
     */
    private void sendUsage(Player player) {
        player.sendMessage("§6=== 钓鱼图鉴命令 ===");
        player.sendMessage("§a/mxf open_guide <ZhiMeng|ShuiYunJian> §7- 打开钓鱼图鉴界面");
        player.sendMessage("§a/mxf guide ZhiMeng §7- 获取织梦者系列指南");
        player.sendMessage("§a/mxf guide ShuiYunJian §7- 获取水云间系列指南");
        player.sendMessage("§6===================");
    }

    /**
     * 给予物品(背包满则掉落地上)
     */
    private void giveItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage("§e你的背包已满，物品已掉落在地上！");
        } else {
            player.getInventory().addItem(item);
        }
    }
}