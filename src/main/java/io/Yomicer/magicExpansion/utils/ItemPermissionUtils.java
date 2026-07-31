package io.Yomicer.magicExpansion.utils;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class ItemPermissionUtils {

    /**
     * 判断玩家是否有权限进行操作
     *
     * @param player 玩家对象
     * @return 如果玩家有权限,返回 true;否则返回 false
     */
    public static boolean hasPermissionRe(Player player) {
        // 检查玩家是否有 "slimefun.inventory.bypass" 权限
        if (player.hasPermission("slimefun.inventory.bypass")) {
            return true;
        }

        // 检查保护插件的权限(如 WorldGuard)
        if (Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.INTERACT_BLOCK)&&
                Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.BREAK_BLOCK)&&
                Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.PLACE_BLOCK)) {
            return true;
        }
        player.sendMessage(getGradientName("[Magic] You do not have permission to do that."));

        return false;
    }

    /**
     * 判断玩家是否有权限进行操作
     *
     * @param player 玩家对象
     * @return 如果玩家有权限,返回 true;否则返回 false
     */
    public static boolean hasPermissionOnAttack(Player player) {
        // 检查玩家是否有 "slimefun.inventory.bypass" 权限
        if (player.hasPermission("slimefun.inventory.bypass")) {
            return true;
        }

        // 检查保护插件的权限(如 WorldGuard)
        if (Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.ATTACK_PLAYER)&&
                Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.ATTACK_ENTITY)&&
                Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.INTERACT_BLOCK)&&
                Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.BREAK_BLOCK)&&
                Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.PLACE_BLOCK)) {
            return true;
        }
        return false;
    }

    public static boolean hasPermissionOnlyOnAttackEntity(Player player) {
        // 检查玩家是否有 "slimefun.inventory.bypass" 权限
        if (player.hasPermission("slimefun.inventory.bypass")) {
            return true;
        }
        // 检查保护插件的权限(如 WorldGuard)
        if (Slimefun.getProtectionManager().hasPermission(player, player.getLocation(), Interaction.ATTACK_ENTITY)) {
            return true;
        }
        return false;
    }


    /**
     * 判断玩家是否有权限进行操作
     *
     * @param player 玩家对象
     * @return 如果玩家有权限,返回 true;否则返回 false
     */
    public static boolean hasPermissionPoint(Player player, Location l) {
        // 检查玩家是否有 "slimefun.inventory.bypass" 权限
        if (player.hasPermission("slimefun.inventory.bypass")) {
            return true;
        }

        // 检查保护插件的权限(如 WorldGuard)
        if (Slimefun.getProtectionManager().hasPermission(player, l, Interaction.INTERACT_BLOCK)&&
                Slimefun.getProtectionManager().hasPermission(player, l, Interaction.BREAK_BLOCK)&&
                Slimefun.getProtectionManager().hasPermission(player, l, Interaction.PLACE_BLOCK)) {
            return true;
        }
//        player.sendMessage(getGradientName("[Magic] You do not have permission to do that."));

        return false;
    }




}
