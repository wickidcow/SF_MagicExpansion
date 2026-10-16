package io.Yomicer.magicExpansion.items.tools;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.playerMessage.MainHandMessage;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.EntityInteractHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class ItemNameTag extends SimpleSlimefunItem<ItemUseHandler> implements Listener {

    private record RenameSession(UUID token, long startedAt) {}

    private final Map<UUID, RenameSession> renameSessions = new ConcurrentHashMap<>();

    public ItemNameTag(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        Bukkit.getPluginManager().registerEvents(this, MagicExpansion.getInstance());
        addItemHandler((EntityInteractHandler) this::onEntityInteract);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            Player player = e.getPlayer();

            if (e.getHand() != EquipmentSlot.HAND) {
                MainHandMessage.sendMainHandMessage(player);
                return;
            }

            Action action = e.getInteractEvent().getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            if (player.isSneaking()) {
                player.sendMessage(ColorGradient.getGradientNameVer2("Enter the new item name within 60 seconds."));
                player.sendMessage(ColorGradient.getGradientNameVer2("Use & color codes, <g1>text, or <g2>text. Type 'cancel' to stop."));
                UUID uuid = player.getUniqueId();
                RenameSession session = new RenameSession(UUID.randomUUID(), System.currentTimeMillis());
                renameSessions.put(uuid, session);

                // Only the timeout belonging to the current session may close it.
                Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> {
                    if (renameSessions.remove(uuid, session) && player.isOnline()) {
                        player.sendMessage(ColorGradient.getGradientNameVer2("The renaming request timed out."));
                    }
                }, 20 * 60);
            } else {
                // === 普通右键:直接复制主手 displayName 到副手 ===
                ItemStack nameTag = player.getInventory().getItemInMainHand();

                // 检查副手是否有物品
                ItemStack offHand = player.getInventory().getItemInOffHand();
                if (offHand == null || offHand.getType() == Material.AIR) {
                    player.sendMessage(ColorGradient.getGradientNameVer2("There is no item in your off hand!"));
                    return;
                }

                // 获取主手命名签的 displayName
                String displayName = null;
                if (nameTag.hasItemMeta() && nameTag.getItemMeta().hasDisplayName()) {
                    displayName = nameTag.getItemMeta().getDisplayName();
                }
                if (displayName == null || displayName.trim().isEmpty()) {
                    player.sendMessage(ColorGradient.getGradientNameVer2("The name tag has no custom name to copy!"));
                    return;
                }

                // 消耗 1 个命名签
                if (nameTag.getAmount() > 1) {
                    nameTag.setAmount(nameTag.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }

                // 复制 displayName 到副手(保留原有 NBT,只改名字)
                ItemMeta offMeta = offHand.getItemMeta();
                if (offMeta != null) {
                    offMeta.setDisplayName(displayName); // 直接复制,包括 § 颜色
                    offHand.setItemMeta(offMeta);
                    player.sendMessage(ColorGradient.getGradientNameVer2("Copied the name to the off-hand item!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                }
            }
        };
    }

    // Handles Shift-right-click chat input for renaming.
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        RenameSession session = renameSessions.get(uuid);
        if (session == null) {
            return;
        }

        event.setCancelled(true);
        String input = event.getMessage().trim();

        if (System.currentTimeMillis() - session.startedAt() > 60_000L) {
            renameSessions.remove(uuid, session);
            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () ->
                    player.sendMessage(ColorGradient.getGradientNameVer2("The renaming request timed out.")));
            return;
        }

        if (input.equalsIgnoreCase("cancel")) {
            renameSessions.remove(uuid, session);
            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () ->
                    player.sendMessage(ColorGradient.getGradientNameVer2("Renaming cancelled.")));
            return;
        }

        if (input.isEmpty()) {
            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () ->
                    player.sendMessage(ColorGradient.getGradientNameVer2("The name cannot be empty.")));
            return;
        }

        final String finalName;
        if (input.regionMatches(true, 0, "<g2>", 0, 4)) {
            String text = input.substring(4);
            if (text.isEmpty()) {
                Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () ->
                        player.sendMessage(ColorGradient.getGradientNameVer2("Gradient text cannot be empty.")));
                return;
            }
            finalName = ColorGradient.getGradientNameVer2(text);
        } else if (input.regionMatches(true, 0, "<g1>", 0, 4)) {
            String text = input.substring(4);
            if (text.isEmpty()) {
                Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () ->
                        player.sendMessage(ColorGradient.getGradientNameVer2("Gradient text cannot be empty.")));
                return;
            }
            finalName = ColorGradient.getGradientName(text);
        } else {
            finalName = ChatColor.translateAlternateColorCodes('&', input);
        }

        renameSessions.remove(uuid, session);
        Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == Material.AIR || SlimefunItem.getByItem(mainHand) != this) {
                player.sendMessage(ColorGradient.getGradientNameVer2(
                        "Renaming cancelled because the Portable Item Name Tag is no longer in your main hand."));
                return;
            }

            ItemMeta meta = mainHand.getItemMeta();
            if (meta == null) {
                player.sendMessage(ColorGradient.getGradientNameVer2("That item cannot be renamed."));
                return;
            }

            meta.setDisplayName(finalName);
            mainHand.setItemMeta(meta);
            player.sendMessage(ColorGradient.getGradientNameVer2("Item renamed!"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        });
    }

    private void onEntityInteract(
            PlayerInteractEntityEvent event, ItemStack item, boolean offHand) {
        if (offHand || event.isCancelled() || event.getRightClicked() instanceof Player) {
            return;
        }

        Player player = event.getPlayer();
        if (!Slimefun.getProtectionManager().hasPermission(
                player, event.getRightClicked().getLocation(), Interaction.INTERACT_ENTITY)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || meta.getDisplayName().trim().isEmpty()) {
            player.sendMessage(ColorGradient.getGradientNameVer2(
                    "Sneak-right-click the tag first to set a name."));
            return;
        }

        event.setCancelled(true);
        event.getRightClicked().setCustomName(meta.getDisplayName());

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemUtils.consumeItem(item, false);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage(ColorGradient.getGradientNameVer2("Named the creature successfully!"));
    }
}
