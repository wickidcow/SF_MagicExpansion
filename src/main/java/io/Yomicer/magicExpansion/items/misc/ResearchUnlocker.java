package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ResearchUnlocker extends SimpleSlimefunItem<ItemUseHandler> {

    private final Random random = new Random();

    public ResearchUnlocker(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.cancel();

            // 只在主手触发
            if (e.getHand() != EquipmentSlot.HAND) {
                return;
            }

            Player player = e.getPlayer();

            // 异步获取玩家档案(避免卡顿)
            PlayerProfile.get(player, profile -> {
                Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
                    // 获取所有研究
                    List<Research> allResearches = new ArrayList<>(Slimefun.getRegistry().getResearches());

                    // 获取玩家已解锁的研究
                    List<Research> unlockedResearches = new ArrayList<>();
                    for (Research research : allResearches) {
                        if (profile.hasUnlocked(research)) {
                            unlockedResearches.add(research);
                        }
                    }

                    // 判断是否全解锁
                    if (unlockedResearches.size() >= allResearches.size()) {
                        // 全解锁提示
                        sendAllUnlockedMessage(player);
                    } else {
                        // 找出未解锁的研究
                        List<Research> lockedResearches = new ArrayList<>(allResearches);
                        lockedResearches.removeAll(unlockedResearches);

                        if (!lockedResearches.isEmpty()) {
                            // 随机选择一个未解锁的研究
                            Research target = lockedResearches.get(random.nextInt(lockedResearches.size()));

                            // 使用 API 解锁研究(不再使用命令)
                            profile.setResearched(target, true);
                            profile.save(); // 立即保存更改

                            // 播放音效
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            player.sendMessage(ChatColor.GREEN + "✓ Research unlocked: " + ChatColor.YELLOW + target.getName(player));
                        }
                    }

                    // 消耗物品(可选)
                    ItemStack item = e.getItem();
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                });
            });
        };
    }

    /**
     * 发送全解锁提示消息
     */
    private void sendAllUnlockedMessage(Player player) {
        // 发送醒目提示
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "   You already possess every research!  ");
        player.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        player.sendMessage("");

        // 播放特殊音效
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        // 发送标题(1.8+支持)
        player.sendTitle(
                ChatColor.GOLD + "Omniscience",
                ChatColor.YELLOW + "You already possess every research.",
                10, 70, 20
        );
    }

    /**
     * 解锁指定研究
     */
    private void unlockResearch(Player player, Research research) {
        // 方法1:通过执行命令解锁
        String command = "sf research " + player.getName() + " " + research.getKey().getKey();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

    }
}
