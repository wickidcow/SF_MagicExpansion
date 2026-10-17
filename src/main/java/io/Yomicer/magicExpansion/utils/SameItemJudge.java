package io.Yomicer.magicExpansion.utils;

import com.google.gson.*;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SameItemJudge {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final Base64.Encoder BASE64_LINES =
            Base64.getMimeEncoder(76, LINE_SEPARATOR.getBytes(StandardCharsets.US_ASCII));

    // Preserve the old writer's 76-column lines, including its final separator.
    static String encodeBase64Lines(byte[] bytes) {
        String encoded = BASE64_LINES.encodeToString(bytes);
        return bytes.length == 0 ? encoded : encoded + LINE_SEPARATOR;
    }

    // Old saved records permit only these four whitespace characters. A MIME
    // decoder would also discard corrupt punctuation instead of refusing it.
    static byte[] decodeBase64Lines(String data) {
        StringBuilder encoded = new StringBuilder(data.length());
        for (int i = 0; i < data.length(); i++) {
            char character = data.charAt(i);
            if (character != ' ' && character != '\t' && character != '\r' && character != '\n') {
                encoded.append(character);
            }
        }
        if (encoded.length() % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        return Base64.getDecoder().decode(encoded.toString());
    }

    /**
     * ✅ 将 ItemStack 完整序列化为 Base64 字符串
     * 支持:PDC、附魔、lore、自定义模型、Slimefun物品、NBT等所有数据
     */
    @Nullable
    public static String itemToBase64(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // 先确保数量正确写入
            dataOutput.writeObject(item.clone()); // 使用克隆避免外部修改
            dataOutput.flush();

            byte[] serializedBytes = outputStream.toByteArray();
            return encodeBase64Lines(serializedBytes);

        } catch (IOException e) {
            Debug.logWarn("Item serialization failed: " + item.getType());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ 将 Base64 字符串反序列化为 ItemStack
     * 完全还原原始物品(包括 PDC、附魔、数量、NBT 等)
     */
    @Nullable
    public static ItemStack itemFromBase64(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodeBase64Lines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            ItemStack item = (ItemStack) dataInput.readObject();
            // 重要:强制刷新 ItemMeta 引用,防止 PDC 缓存问题
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                item.setItemMeta(meta); // 重新设置,确保一致性
            }
            return item;

        } catch (IOException | ClassNotFoundException e) {
            Debug.logWarn("Item deserialization failed.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ 安全判断两个 ItemStack 是否"same"(可用于堆叠判断)
     * 使用 Bukkit 内建的 isSimilar 方法,并兼容 Slimefun 物品
     */
    public static boolean isSimilarSafe(ItemStack item1, ItemStack item2) {
        if (item1 == item2) return true;
        if (item1 == null || item2 == null) return false;

        // 先检查是否为空气
        if (item1.getType() == Material.AIR || item2.getType() == Material.AIR) {
            return item1.getType() == item2.getType();
        }

        // 非 Slimefun 物品:使用 Bukkit 的 isSimilar(忽略数量)
        return item1.isSimilar(item2);
    }

    /**
     * ✅ 判断两个 Base64 字符串对应的物品是否"same"
     */
    public static boolean isSameBase64Item(String data1, String data2) {
        if (data1 == null && data2 == null) return true;
        if (data1 == null || data2 == null) return false;

        ItemStack item1 = itemFromBase64(data1);
        ItemStack item2 = itemFromBase64(data2);

        return isSimilarSafe(item1, item2);
    }
}
