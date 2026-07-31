package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.utils.aiManager.AIManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class AIChat implements CommandExecutor, TabCompleter {

    private final AIManager aiManager;

    public AIChat(AIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        if (!player.hasPermission("mxai.use")) {
            player.sendMessage("§cYou do not have permission to use AI commands.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "chaton" -> {
                if (aiManager.enableAI(player)) {
                    player.sendMessage("§aPersonal AI chat enabled. Your next chat message will be sent to the AI.");
                } else {
                    player.sendMessage("§cAI chat is disabled or not configured on this server.");
                }
            }
            case "chatoff" -> {
                aiManager.disableAI(player);
                player.sendMessage("§ePersonal AI chat disabled.");
            }
            case "public" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mxai public <message>");
                } else {
                    aiManager.askAIPublic(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                }
            }
            case "publicmode" -> handlePublicMode(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handlePublicMode(Player player, String[] args) {
        if (!player.hasPermission("mxai.op") && !player.isOp()) {
            player.sendMessage("§cYou do not have permission to change public AI mode.");
            return;
        }
        if (!aiManager.isConfigured()) {
            player.sendMessage("§cAI chat is disabled or not configured on this server.");
            return;
        }
        if (args.length == 1) {
            player.sendMessage("§7Public AI mode: " + (aiManager.getPublicMode() ? "§aEnabled" : "§cDisabled"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "on" -> aiManager.setPublicMode(true);
            case "off" -> aiManager.setPublicMode(false);
            default -> player.sendMessage("§cUsage: /mxai publicmode <on|off>");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6========== MagicExpansion AI ==========");
        player.sendMessage("§e/mxai chaton §7- Enable personal AI chat");
        player.sendMessage("§e/mxai chatoff §7- Disable personal AI chat");
        player.sendMessage("§e/mxai public <message> §7- Ask in public AI mode");
        if (player.hasPermission("mxai.op") || player.isOp()) {
            player.sendMessage("§e/mxai publicmode <on|off> §7- Control public AI mode");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("chaton", "chatoff", "public", "publicmode");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("publicmode")) {
            return List.of("on", "off");
        }
        return Collections.emptyList();
    }
}
