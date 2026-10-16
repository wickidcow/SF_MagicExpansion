package io.Yomicer.magicExpansion.items.tools;

import io.Yomicer.magicExpansion.items.enchantMachine.EnchantingTable;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnchantmentEraser extends SimpleSlimefunItem<ItemUseHandler> {

    public EnchantmentEraser(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            e.cancel();
            Player player = e.getPlayer();

            // 检查触发事件的手
            if (e.getHand() != EquipmentSlot.HAND) {
                // 如果是副手触发的,不处理(我们希望清除器在主手)
                return;
            }

            // 获取主手物品(应该是清除器)
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();

            // 获取副手物品(需要清除的物品)
            ItemStack offhandItem = player.getInventory().getItemInOffHand();

            // 检查副手是否有物品且不是空气
            if (offhandItem == null || offhandItem.getType() == Material.AIR) {
                player.sendMessage(ColorGradient.getGradientNameVer2("Place the item you want to clear in your off hand!"));
                return;
            }

            // 检查副手物品是否包含赋能属性
            if (!hasEnchantmentAttributesExact(offhandItem)) {
                player.sendMessage(ColorGradient.getGradientNameVer2("The off-hand item has no Magic Empowerment attributes!"));
                return;
            }

            // 清除副手物品的赋能属性
            ItemStack result = removeEnchantmentAttributes(offhandItem.clone());

            if (result != null) {
                // 播放音效
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);

                // 设置副手物品为清除后的物品
                player.getInventory().setItemInOffHand(result);

                // Consume only after a successful erase; Creative keeps the eraser.
                if (player.getGameMode() != GameMode.CREATIVE) {
                    if (mainHandItem.getAmount() > 1) {
                        mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }

                player.sendMessage(ColorGradient.getGradientNameVer2("Magic Empowerment attributes cleared successfully!"));
            } else {
                player.sendMessage(ColorGradient.getGradientNameVer2("Could not clear the item; it may have no Magic Empowerment attributes."));
            }
        };
    }

    /**
     * 清除物品上的所有魔法赋能属性
     */
    public static ItemStack removeEnchantmentAttributes(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        boolean changedPdc = false;

        for (String attribute : EnchantingTable.ATTRIBUTE_POOL.keySet()) {
            NamespacedKey key = new NamespacedKey(
                    "magicexpansion", attribute.toLowerCase().replace(".", "_"));
            if (pdc.has(key, PersistentDataType.INTEGER)
                    || pdc.has(key, PersistentDataType.BOOLEAN)) {
                pdc.remove(key);
                changedPdc = true;
            }
        }

        boolean changedLore = removeEnchantmentLore(meta);
        if (!changedPdc && !changedLore) {
            return null;
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Remove both the current "Magic Empowerment:" lore section and the historical
     * "Random1" header used by older MagicExpansion builds.
     */
    private static boolean removeEnchantmentLore(ItemMeta meta) {
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        List<String> newLore = new ArrayList<>();
        boolean changed = false;
        boolean inSection = false;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            String stripped = stripAllColorCodes(line).trim();

            boolean currentTitle = stripped.contains("Magic Empowerment")
                    || stripped.contains("魔法赋能");
            boolean legacyTitle = stripped.equalsIgnoreCase("Random1")
                    && i + 1 < lore.size()
                    && isEmpowermentAttributeLine(stripAllColorCodes(lore.get(i + 1)).trim());

            if (currentTitle || legacyTitle) {
                inSection = true;
                changed = true;
                continue;
            }

            if (inSection && isEmpowermentAttributeLine(stripped)) {
                changed = true;
                continue;
            }

            if (inSection) {
                inSection = false;
            }

            newLore.add(line);
        }

        if (changed) {
            meta.setLore(newLore.isEmpty() ? null : newLore);
        }
        return changed;
    }

    private static boolean isEmpowermentAttributeLine(String stripped) {
        if (stripped == null || !stripped.startsWith("- ")) {
            return false;
        }

        for (String displayName : EnchantingTable.ATTRIBUTE_NAME_MAP.values()) {
            if (stripped.regionMatches(true, 2, displayName, 0, displayName.length())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEmpowermentLore(ItemMeta meta) {
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        for (int i = 0; i < lore.size(); i++) {
            String stripped = stripAllColorCodes(lore.get(i)).trim();
            if (stripped.contains("Magic Empowerment")
                    || stripped.contains("魔法赋能")) {
                return true;
            }
            if (stripped.equalsIgnoreCase("Random1")
                    && i + 1 < lore.size()
                    && isEmpowermentAttributeLine(
                            stripAllColorCodes(lore.get(i + 1)).trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 去除字符串中的所有Minecraft颜色代码(包括十六进制颜色代码)
     * 处理 §x§F§D§B§7§D§4 这种格式的渐变颜色代码
     */
    private static String stripAllColorCodes(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 先处理十六进制颜色代码 §x§F§D§B§7§D§4 格式
        // 这种格式是 §x 后面跟12个字符(6组 §+十六进制数字)
        String result = input;

        // 循环移除所有颜色代码
        while (result.contains("§")) {
            // 找到第一个 §
            int index = result.indexOf("§");
            if (index < 0 || index >= result.length() - 1) {
                break;
            }

            char nextChar = result.charAt(index + 1);

            // 如果是 §x(十六进制颜色代码开头)
            if (nextChar == 'x' || nextChar == 'X') {
                // §x 后面应该有12个字符(6组 §+十六进制数字)
                // 总共14个字符:§x + 6*(§+数字)
                if (index + 13 < result.length()) {
                    // 检查后面是否都是正确的格式
                    boolean validHexColor = true;
                    for (int i = 0; i < 6; i++) {
                        if (result.charAt(index + 2 + i*2) != '§' ||
                                !isHexDigit(result.charAt(index + 3 + i*2))) {
                            validHexColor = false;
                            break;
                        }
                    }

                    if (validHexColor) {
                        // 移除这14个字符
                        result = result.substring(0, index) + result.substring(index + 14);
                        continue;
                    }
                }
            }

            // 处理普通颜色代码 §0-§9, §a-§f, §k-§o, §r
            if (isColorCodeChar(nextChar)) {
                // 移除这两个字符
                result = result.substring(0, index) + result.substring(index + 2);
            } else {
                // 如果不是颜色代码,只移除 § 字符本身
                result = result.substring(0, index) + result.substring(index + 1);
            }
        }

        return result;
    }

    /**
     * 检查字符是否是有效的十六进制数字(0-9, a-f, A-F)
     */
    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * 检查字符是否是颜色代码字符
     */
    private static boolean isColorCodeChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'k' && c <= 'o') ||
                (c >= 'A' && c <= 'F') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }

    /**
     * 检查物品是否包含魔法赋能属性
     */
    public static boolean hasEnchantmentAttributesExact(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // 首先检查PDC中是否有魔法赋能属性
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        for (String attribute : EnchantingTable.ATTRIBUTE_POOL.keySet()) {
            NamespacedKey key = new NamespacedKey("magicexpansion", attribute.toLowerCase().replace(".", "_"));

            if (pdc.has(key, PersistentDataType.INTEGER) || pdc.has(key, PersistentDataType.BOOLEAN)) {
                return true;
            }
        }

        // Legacy builds may only have the old lore marker.
        return hasEmpowermentLore(meta);
    }
}
