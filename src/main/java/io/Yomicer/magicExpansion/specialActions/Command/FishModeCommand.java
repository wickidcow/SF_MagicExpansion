package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.FishingIntegrationManager;
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

/** Admin toggle for the optional MagicExpansion-owned Water Cloud minigame. */
public final class FishModeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             @Nonnull String label, @Nonnull String[] args) {
        if (args.length != 1 || (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false"))) {
            sender.sendMessage("§eUsage: /mxfishmode <true|false>");
            sender.sendMessage("§7External providers such as PyroFishingPro and BetterFishing remain primary when configured.");
            return true;
        }

        boolean enable = Boolean.parseBoolean(args[0]);
        WaterCloudHookManager.setEnabled(enable);

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && SlimefunItem.getByItem(item) instanceof FishingRodWaterCloud) {
                    WaterCloudRodProficiency.updateRewardLore(item);
                }
            }
        }

        if (enable && FishingIntegrationManager.isExternalProviderActive()) {
            sender.sendMessage("§eWater Cloud custom fishing was requested, but "
                    + FishingIntegrationManager.getPrimaryProvider().getDisplayName()
                    + " is configured as the primary fishing provider.");
            sender.sendMessage("§7MagicExpansion will keep applying rod progression and safe bonus rewards without taking over fishing.");
        } else {
            sender.sendMessage(enable
                    ? "§bWater Cloud custom fishing enabled."
                    : "§7Water Cloud custom fishing disabled; vanilla/external fishing flow remains active.");
        }
        return true;
    }
}
