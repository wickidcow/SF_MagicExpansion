package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResearchUnlocker extends SimpleSlimefunItem<ItemUseHandler> {

    private final Random random = new Random();
    private final ConcurrentHashMap<UUID, UUID> pendingUnlocks = new ConcurrentHashMap<>();

    public ResearchUnlocker(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return event -> {
            event.setUseItem(Event.Result.DENY);
            event.cancel();

            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }

            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            UUID requestId = UUID.randomUUID();
            if (pendingUnlocks.putIfAbsent(playerId, requestId) != null) {
                player.sendMessage(ChatColor.YELLOW + "A research page is already being read...");
                return;
            }

            ItemStack expected = event.getItem().clone();

            Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> {
                if (pendingUnlocks.remove(playerId, requestId) && player.isOnline()) {
                    player.sendMessage(ChatColor.RED
                            + "Research unlock timed out. The Lost Page was not consumed.");
                }
            }, 20L * 30L);

            PlayerProfile.get(player, profile -> Bukkit.getScheduler().runTask(
                    MagicExpansion.getInstance(),
                    () -> {
                        try {
                            if (!requestId.equals(pendingUnlocks.get(playerId))) {
                                return;
                            }
                            if (!player.isOnline()) {
                                return;
                            }

                            ItemStack live = player.getInventory().getItemInMainHand();
                            if (live == null
                                    || live.getType().isAir()
                                    || SlimefunItem.getByItem(live) != this
                                    || !live.isSimilar(expected)) {
                                player.sendMessage(ChatColor.RED
                                        + "Research unlock cancelled because the Lost Page moved or changed.");
                                return;
                            }

                            List<Research> allResearches =
                                    new ArrayList<>(Slimefun.getRegistry().getResearches());
                            List<Research> lockedResearches = new ArrayList<>();
                            for (Research research : allResearches) {
                                if (!profile.hasUnlocked(research)) {
                                    lockedResearches.add(research);
                                }
                            }

                            if (lockedResearches.isEmpty()) {
                                // There is no successful effect, so do not consume the page.
                                sendAllUnlockedMessage(player);
                                return;
                            }

                            Research target =
                                    lockedResearches.get(random.nextInt(lockedResearches.size()));
                            profile.setResearched(target, true);
                            profile.save();

                            if (player.getGameMode() != GameMode.CREATIVE) {
                                if (live.getAmount() > 1) {
                                    live.setAmount(live.getAmount() - 1);
                                } else {
                                    player.getInventory().setItemInMainHand(null);
                                }
                            }

                            player.playSound(
                                    player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            player.sendMessage(ChatColor.GREEN + "✓ Research unlocked: "
                                    + ChatColor.YELLOW + target.getName(player));
                        } finally {
                            pendingUnlocks.remove(playerId, requestId);
                        }
                    }));
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
