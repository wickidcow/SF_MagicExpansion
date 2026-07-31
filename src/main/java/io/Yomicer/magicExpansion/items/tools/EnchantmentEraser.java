package io.Yomicer.magicExpansion.items.tools;

import io.Yomicer.magicExpansion.items.enchantMachine.EnchantingTable;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
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

                // 消耗主手的清除器
                if (mainHandItem.getAmount() > 1) {
                    mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
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
        Set<String> attributesToRemove = new HashSet<>();
        boolean hasRemovedAttributes = false;

        // 检查并移除所有魔法赋能属性
        for (String attribute : EnchantingTable.ATTRIBUTE_POOL.keySet()) {
            NamespacedKey key = new NamespacedKey("magicexpansion", attribute.toLowerCase().replace(".", "_"));

            if (pdc.has(key, PersistentDataType.INTEGER) || pdc.has(key, PersistentDataType.BOOLEAN)) {
                pdc.remove(key);
                attributesToRemove.add(attribute);
                hasRemovedAttributes = true;
            }
        }

        // 如果没有属性被移除,返回原物品
        if (!hasRemovedAttributes) {
            return item;
        }

        // 更新Lore,移除整个魔法赋能段落
        removeEnchantmentLore(meta, attributesToRemove);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 从Lore中移除整个魔法赋能段落
     */
    private static void removeEnchantmentLore(ItemMeta meta, Set<String> attributesToRemove) {
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }

        List<String> newLore = new ArrayList<>();
        boolean inEnchantmentSection = false;
        int enchantmentSectionStart = -1;
        int sectionCount = 0;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            String strippedLine = stripAllColorCodes(line);

            // 检查是否是"魔法赋能:"标题行
            if ((strippedLine.contains("Magic Empowerment") || strippedLine.contains("\u9b54\u6cd5\u8d4b\u80fd")) && (strippedLine.contains(":") || strippedLine.contains(":"))) {
                // 如果已经在赋能段落中,先结束之前的段落
                if (inEnchantmentSection) {
                    // 跳过之前段落的所有行
//                    MagicExpansion.getInstance().getLogger().info("结束魔法赋能段落 " + sectionCount + ",从行 " + enchantmentSectionStart + " 到 " + (i-1));
                }

                // 开始新的赋能段落
                inEnchantmentSection = true;
                enchantmentSectionStart = i;
                sectionCount++;
//                MagicExpansion.getInstance().getLogger().info("发现魔法赋能段落 " + sectionCount + ",标题行: " + strippedLine);
                continue; // 跳过标题行
            }

            // 如果在赋能段落中
            if (inEnchantmentSection) {
                // 检查是否是属性行(去色后以"- "开头)
                if (strippedLine.startsWith("- ")) {
//                    MagicExpansion.getInstance().getLogger().info("跳过属性行: " + strippedLine);
                    continue; // 跳过属性行
                }

                // 如果不是属性行,结束当前赋能段落
//                MagicExpansion.getInstance().getLogger().info("结束魔法赋能段落 " + sectionCount + ",从行 " + enchantmentSectionStart + " 到 " + (i-1));
                inEnchantmentSection = false;

                // 当前行不是属性行,添加到新Lore
                newLore.add(line);
            } else {
                // 不在赋能段落中,直接添加
                newLore.add(line);
            }
        }

        // 如果最后还在赋能段落中,结束它
        if (inEnchantmentSection) {
//            MagicExpansion.getInstance().getLogger().info("结束魔法赋能段落 " + sectionCount + ",从行 " + enchantmentSectionStart + " 到末尾");
        }

//        MagicExpansion.getInstance().getLogger().info("原始Lore行数: " + lore.size() + ", 新Lore行数: " + newLore.size());

        // 如果移除了整个赋能段落导致Lore为空,设为null
        if (newLore.isEmpty()) {
            meta.setLore(null);
        } else {
            meta.setLore(newLore);
        }
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

        // 如果PDC中没有,检查Lore中是否有"魔法赋能:"字样
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (String line : lore) {
                String strippedLine = stripAllColorCodes(line);
                if ((strippedLine.contains("Magic Empowerment") || strippedLine.contains("\u9b54\u6cd5\u8d4b\u80fd")) && (strippedLine.contains(":") || strippedLine.contains(":"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
