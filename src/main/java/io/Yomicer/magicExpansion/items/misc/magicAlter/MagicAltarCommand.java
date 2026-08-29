package io.Yomicer.magicExpansion.items.misc.magicAlter;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagicAltarCommand implements TabExecutor {

    private final MagicExpansion plugin;

    public MagicAltarCommand(MagicExpansion plugin) {
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
            player.sendMessage("§6/mxwand wand §7- 获取魔法祭坛法杖");
            return true;
        }

        if (args[0].equalsIgnoreCase("wand") && player.hasPermission("mxwand.wand")) {
            // 修复(N)：addItem 返回剩余物品时掉落地面，背包满不吞物品
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(createAltarWand());
            for (ItemStack rest : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), rest);
            }
            player.sendMessage("§a已获得魔法祭坛法杖!");
            return true;
        }

        player.sendMessage("§c未知子命令! 使用 /mxwand 查看可用命令");
        return true;
    }

    // 创建特殊物品（祭坛法杖）
    private ItemStack createAltarWand() {
        ItemStack wand = new ItemStack(org.bukkit.Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6魔法祭坛法杖");
            meta.getPersistentDataContainer().set(
                    plugin.getPluginInitializer().getAltarWandKey(),
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            // 添加Lore说明
            List<String> lore = new ArrayList<>();
            lore.add("§7右键祭坛发射器来激活合成");
            lore.add("§7需要正确的方块布局和物品配方");
            meta.setLore(lore);

            wand.setItemMeta(meta);
        }

        return wand;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数补全
            String[] subCommands = {"wand"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }

        return completions;
    }
}
