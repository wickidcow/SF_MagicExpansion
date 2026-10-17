package io.Yomicer.magicExpansion.items.misc;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSpawnReason;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.FireworkUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.Yomicer.magicExpansion.Listener.SlimefunRegistryGiftBox.itemMapMihoyoHonkai;

public class HonkaiStarRailBox extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public HonkaiStarRailBox(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }



    // 随机获取一个物品的 ItemStack
    public ItemStack getRandomItemStack(Map<String, SlimefunItem> itemMap) {
        if (itemMap.isEmpty()) {
            return null;
        }

        Collection<SlimefunItem> values = itemMap.values();
        List<SlimefunItem> list = new ArrayList<>(values);

        SlimefunItem randomItem = list.get(new Random().nextInt(list.size()));
        return randomItem.getItem().clone();
    }

    private boolean spawnGift(Location location, Player player, ItemStack gift) {
        if (gift == null || gift.getType().isAir()) {
            return false;
        }

        try {
            if (SlimefunUtils.spawnItem(
                    location, gift, ItemSpawnReason.CHRISTMAS_PRESENT_OPENED, true, player) == null) {
                return false;
            }

            String name = gift.hasItemMeta() && gift.getItemMeta().hasDisplayName()
                    ? gift.getItemMeta().getDisplayName()
                    : gift.getType().name();
            player.sendMessage("§aYou opened the Honkai: Star Rail box and received " + name + "!");
            return true;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }



    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.cancel();

            if (e.getHand() != EquipmentSlot.HAND || e.getClickedBlock().isEmpty()) {
                return;
            }

            Player player = e.getPlayer();
            Block clicked = e.getClickedBlock().get();
            if (!Slimefun.getProtectionManager().hasPermission(
                    player, clicked.getLocation(), Interaction.INTERACT_BLOCK)) {
                player.sendMessage("§cYou do not have permission to open this box here.");
                return;
            }

            ItemStack gift = getRandomItemStack(itemMapMihoyoHonkai);
            if (gift == null) {
                player.sendMessage("§cThis gift box has no available rewards right now.");
                return;
            }

            Block destination = clicked.getRelative(e.getClickedFace());
            if (!spawnGift(destination.getLocation().add(0.5, 0.25, 0.5), player, gift)) {
                player.sendMessage("§cThe reward could not be spawned, so the box was not consumed.");
                return;
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemUtils.consumeItem(e.getItem(), false);
            }
            FireworkUtils.launchRandom(player, 3);
        };
    }



}
