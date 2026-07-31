package io.Yomicer.magicExpansion.specialActions.Command;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.fish.Fish;
import io.Yomicer.magicExpansion.items.misc.fish.FishKeys;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MagicFishCommand implements CommandExecutor, TabCompleter {

    /** 配置文件中的重量上限键,默认 100000.0 */
    private static final String CFG_MAX_WEIGHT = "Fish.admin-give.max-weight";
    private static final double DEFAULT_MAX_WEIGHT = 100000.0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGive(player, args);
                break;
            case "list":
                handleList(player);
                break;
            default:
                sendUsage(player);
                break;
        }
        return true;
    }

    private void handleGive(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /magicfish give <type> [weight] [amount]");
            return;
        }

        String typeArg = args[1];
        Fish fish = Fish.fromString(typeArg);
        if (fish == null) {
            player.sendMessage("§cUnknown fish type: " + typeArg);
            player.sendMessage("§7Use /magicfish list to view all fish types.");
            return;
        }

        // 解析重量(可选,默认取该鱼种的 maxWeight)
        double weight = fish.getMaxWeight();
        if (args.length >= 3) {
            try {
                weight = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cWeight must be a number. You entered: " + args[2]);
                return;
            }
        }

        // 解析数量(可选,默认1)
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cAmount must be a whole number. You entered: " + args[3]);
                return;
            }
        }
        if (amount <= 0 || amount > 64) {
            player.sendMessage("§cAmount must be between 1 and 64!");
            return;
        }

        // === 重量上限校验 ===
        double configMax = getConfigMaxWeight();
        if (weight > configMax) {
            player.sendMessage("§cWeight " + weight + " exceeds the configured maximum of " + configMax + "!");
            player.sendMessage("§7Adjust Fish.admin-give.max-weight in config.yml.");
            return;
        }
        // 同时不允许超过该鱼种自身设定的 maxWeight(防止产出"超规格"鱼)
        if (weight > fish.getMaxWeight()) {
            player.sendMessage("§cWeight " + weight + " exceeds this fish's maximum weight of " + fish.getMaxWeight() + "!");
            return;
        }
        if (weight < fish.getMinWeight()) {
            player.sendMessage("§cWeight " + weight + " is below this fish's minimum weight of " + fish.getMinWeight() + "!");
            return;
        }

        // 构造鱼物品
        ItemStack fishItem = buildFishItem(fish, weight);
        if (fishItem == null) {
            player.sendMessage("§cCould not create the fish item. Check the console for details.");
            return;
        }
        fishItem.setAmount(amount);

        // 发放
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), fishItem);
            player.sendMessage("§eYour inventory was full, so the fish was dropped at your feet.");
        } else {
            player.getInventory().addItem(fishItem);
        }

        Fish.WeightRarity wr = fish.getWeightRarity(weight);
        player.sendMessage("§aReceived: §f" + fish.getDisplayName()
                + " §aWeight: §f" + String.format("%.3f", weight)
                + " §aRarity: §f" + wr.getDisplayName()
                + " §aAmount: §f" + amount);
    }

    private void handleList(Player player) {
        player.sendMessage("§6=== Available Fish Types ===");
        for (Fish f : Fish.values()) {
            player.sendMessage("§e" + f.name()
                    + " §7| §f" + f.getDisplayName()
                    + " §7| Weight Range: §a" + f.getMinWeight() + " ~ " + f.getMaxWeight()
                    + " §7| Rarity: §b" + f.getRarity().getDisplayName());
        }
        player.sendMessage("§6===================");
    }

    /**
     * 以与 FishKeys.enchantDropWithFishData 一致的方式构造鱼物品.
     * 基础物品按稀有度选取对应的 RANDOM_FISH_* 物品,无法匹配时回退到 Material.COD.
     */
    private ItemStack buildFishItem(Fish fish, double weight) {
        ItemStack base = getBaseItemByRarity(fish.getRarity());
        if (base == null) {
            base = new ItemStack(Material.COD);
        }
        ItemStack item = base.clone();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(FishKeys.FISH_TYPE, PersistentDataType.STRING, fish.name());
        pdc.set(FishKeys.FISH_WEIGHT, PersistentDataType.DOUBLE, weight);

        Fish.WeightRarity weightRarity = fish.getWeightRarity(weight);
        pdc.set(FishKeys.FISH_WEIGHT_RARITY, PersistentDataType.STRING, weightRarity.name());

        // 重量稀有度标记符号(与原钓鱼逻辑保持一致)
        String weightRareThis = "";
        if (weightRarity == Fish.WeightRarity.RARE_FISH) {
            weightRareThis = "§e§l⭐";
        } else if (weightRarity == Fish.WeightRarity.SUPER_RARE_FISH) {
            weightRareThis = "§b§l💎";
        } else if (weightRarity == Fish.WeightRarity.MAX_WEIGHT_FISH) {
            weightRareThis = "§c§l🎶";
        }

        meta.setDisplayName(fish.getDisplayName() + " " + weightRareThis);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§dFish Rarity: §r§f" + fish.getRarity().getDisplayName());
        lore.add("§aWeight: §r§f" + String.format("%.3f", weight) + " kg");
        lore.add("§eWeight Rarity: §r" + weightRarity.getDisplayName() + " " + weightRareThis);
        if (fish.getLoreLines() != null && fish.getLoreLines().length > 0) {
            lore.add("");
            lore.addAll(Arrays.asList(fish.getLoreLines()));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 稀有度 → 基础物品的反向映射.
     * 与 FishKeys.RARITY_ITEM_MAP 保持一致,用于让产出的鱼在视觉与钓鱼系统上对齐.
     */
    private ItemStack getBaseItemByRarity(Fish.Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return MagicExpansionItems.RANDOM_FISH_COMMON;
            case UNCOMMON:
                return MagicExpansionItems.RANDOM_FISH_UNCOMMON;
            case RARE:
                return MagicExpansionItems.RANDOM_FISH_RARE;
            case RARE_POOL_DUST:
                return MagicExpansionItems.RANDOM_FISH_RARE_POOL_DUST;
            case RARE_POOL_ORE:
                return MagicExpansionItems.RANDOM_FISH_RARE_POOL_ORE;
            case RARE_POOL_INDUSTRY:
                return MagicExpansionItems.RANDOM_FISH_RARE_POOL_INDUSTRY;
            case EPIC_POOL_INDUSTRY:
                return MagicExpansionItems.RANDOM_FISH_EPIC_POOL_INDUSTRY;
            case EPIC_POOL_ALLOY_INGOT:
                return MagicExpansionItems.RANDOM_FISH_EPIC_POOL_ALLOY_INGOT;
            case EPIC:
                return MagicExpansionItems.RANDOM_FISH_EPIC;
            case LEGENDARY:
                return MagicExpansionItems.RANDOM_FISH_LEGENDARY;
            case LEGENDARY_EEL:
                return MagicExpansionItems.FISH_LEGENDARY_EEL_POWER;
            default:
                return null;
        }
    }

    private double getConfigMaxWeight() {
        return MagicExpansion.getInstance().getConfig()
                .getDouble(CFG_MAX_WEIGHT, DEFAULT_MAX_WEIGHT);
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6=== Magic Fish Administration ===");
        player.sendMessage("§a/magicfish give <type> [weight] [amount] §7- Give a fish");
        player.sendMessage("§a/magicfish list §7- List all fish types");
        player.sendMessage("§7Maximum admin weight: Fish.admin-give.max-weight in config.yml");
        player.sendMessage("§6=========================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String[] subs = {"give", "list"};
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Fish f : Fish.values()) {
                if (f.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(f.name());
                }
            }
        }
        return completions;
    }
}
