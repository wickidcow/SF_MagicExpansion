package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.WaterCloudHookManager;
import io.Yomicer.magicExpansion.utils.WaterCloudRodProficiency;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * /mxfishmode <true|false>
 * 切换水云间系列鱼竿的钓鱼系统:
 * true = 新钓鱼系统(状态机判定, 脱离原版); false = 旧系统(原版判定)
 * 切换后立即刷新全服在线玩家背包中所有水云间鱼竿的奖励 lore
 */
public class FishModeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             @Nonnull String label, @Nonnull String[] args) {
        // 【A修复】权限检查：控制台(ConsoleCommandSender)等非玩家发送者直接允许；玩家需拥有 magicexpansion.fishmode 权限
        if ((sender instanceof Player) && !sender.hasPermission("magicexpansion.fishmode")) {
            sender.sendMessage("§c你没有权限使用此命令！(需要 magicexpansion.fishmode)");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("§e用法: /mxfishmode <true|false>");
            sender.sendMessage("§7true  = 开启水云间新钓鱼系统(状态机判定)");
            sender.sendMessage("§7false = 关闭新系统, 使用旧系统");
            return true;
        }
        boolean enable = Boolean.parseBoolean(args[0]);
        WaterCloudHookManager.setEnabled(enable);

        // 切换后立即刷新全服在线玩家背包中水云间鱼竿的奖励 lore(奖励描述跟随系统)
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && SlimefunItem.getByItem(item) instanceof FishingRodWaterCloud) {
                    WaterCloudRodProficiency.updateRewardLore(item);
                }
            }
        }

        sender.sendMessage(enable
                ? "§b✦ 水云间新钓鱼系统已开启, 所有水云间鱼竿切换为状态机判定"
                : "§7水云间新钓鱼系统已关闭, 恢复旧系统");
        return true;
    }
}
