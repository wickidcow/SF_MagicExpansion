package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class SameItemJudgeTest {
    // Recorded from the former SnakeYAML 2.2 Base64Coder writer for bytes 0..63.
    private static final String OLD_FIRST_LINE =
            "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4";
    private static final String OLD_SECOND_LINE = "OTo7PD0+Pw==";
    private static final NamespacedKey BYTE_ARRAY_KEY = new NamespacedKey("magicexpansion", "bytes");

    @BeforeEach void start() { MockBukkit.mock(); }
    @AfterEach void stop() { MockBukkit.unmock(); }

    @Test void writerMatchesOldWrappedGoldenBytesAndTrailingSeparator() {
        byte[] bytes = goldenBytes();
        String separator = System.lineSeparator();
        assertEquals(OLD_FIRST_LINE + separator + OLD_SECOND_LINE + separator,
                SameItemJudge.encodeBase64Lines(bytes));
        assertEquals(OLD_FIRST_LINE + separator,
                SameItemJudge.encodeBase64Lines(Arrays.copyOf(bytes, 57)));
        assertEquals("", SameItemJudge.encodeBase64Lines(new byte[0]));
        assertEquals("Zg==" + separator,
                SameItemJudge.encodeBase64Lines(new byte[] {'f'}));
    }

    @Test void readerAcceptsOldUnixWindowsAndMixedWhitespaceRecords() {
        for (String separator : new String[] {"\n", "\r\n", "\r", " \t\r\n"}) {
            String saved = " \t" + OLD_FIRST_LINE + separator + OLD_SECOND_LINE + separator;
            assertArrayEquals(goldenBytes(), SameItemJudge.decodeBase64Lines(saved));
        }
        assertArrayEquals(new byte[] {'f'}, SameItemJudge.decodeBase64Lines(" Z\tg\r=\n= "));
    }

    @Test void corruptEncodingStillThrowsInsteadOfBecomingAMissingOrMatchingItem() {
        for (String corrupt : new String[] {
                "Zg", "Zg=", "AA?=", "Zg==\u000b", "Zg==\u000c", "Zg==#???", "AA==\u00a0"}) {
            assertThrows(IllegalArgumentException.class, () -> SameItemJudge.decodeBase64Lines(corrupt));
            assertThrows(IllegalArgumentException.class, () -> SameItemJudge.itemFromBase64(corrupt));
            assertThrows(IllegalArgumentException.class, () -> SameItemJudge.isSameBase64Item(corrupt, corrupt));
        }
    }

    @Test void itemRoundTripPreservesAmountDamageLoreEnchantmentsAndTypedIdentity() {
        ItemStack original = historicalItem();
        ItemStack before = original.clone();
        String encoded = SameItemJudge.itemToBase64(original);
        assertNotNull(encoded);
        assertTrue(encoded.endsWith(System.lineSeparator()));
        assertEquals(before, original);
        ItemStack restored = SameItemJudge.itemFromBase64(encoded);
        assertStoredItemEquals(before, restored);
        assertNotSame(original, restored);
        assertEquals(9007199254740993L, restored.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey("magicexpansion", "amount"), PersistentDataType.LONG));
    }

    @Test void legacyBukkitObjectStreamEnvelopeRemainsReadableWithWindowsWrapping() throws Exception {
        ItemStack original = historicalItem();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes)) {
            output.writeObject(original.clone());
        }
        String basic = Base64.getEncoder().encodeToString(bytes.toByteArray());
        StringBuilder wrapped = new StringBuilder(" \t");
        for (int i = 0; i < basic.length(); i += 76) {
            wrapped.append(basic, i, Math.min(i + 76, basic.length())).append("\r\n");
        }
        String saved = wrapped.toString();
        assertStoredItemEquals(original, SameItemJudge.itemFromBase64(saved));
        // MockBukkit compares array-valued PDC by reference in isSimilar. The exact
        // array round trip is checked above; compare ordinary metadata separately.
        String comparable = SameItemJudge.itemToBase64(withoutByteArray(original));
        assertTrue(SameItemJudge.isSameBase64Item(comparable.replace("\n", "\r\n"), comparable));
    }

    @Test void emptyItemAndSavedValueBehaviorRemainsUnchanged() {
        assertNull(SameItemJudge.itemToBase64(null));
        assertNull(SameItemJudge.itemToBase64(new ItemStack(Material.AIR)));
        assertNull(SameItemJudge.itemFromBase64(null));
        assertNull(SameItemJudge.itemFromBase64(""));
        assertNull(SameItemJudge.itemFromBase64(" \t\r\n"));
        assertTrue(SameItemJudge.isSameBase64Item(null, null));
    }

    private static byte[] goldenBytes() {
        byte[] bytes = new byte[64];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
        return bytes;
    }

    private static void assertStoredItemEquals(ItemStack expected, ItemStack actual) {
        assertNotNull(actual);
        var expectedData = expected.getItemMeta().getPersistentDataContainer();
        var actualData = actual.getItemMeta().getPersistentDataContainer();
        assertEquals(expectedData.getKeys(), actualData.getKeys());
        assertTrue(actualData.has(BYTE_ARRAY_KEY, PersistentDataType.BYTE_ARRAY));
        assertArrayEquals(expectedData.get(BYTE_ARRAY_KEY, PersistentDataType.BYTE_ARRAY),
                actualData.get(BYTE_ARRAY_KEY, PersistentDataType.BYTE_ARRAY));
        // Keep equality checks for every other field, type and PDC value while
        // avoiding MockBukkit's reference-only byte[] equality.
        assertEquals(withoutByteArray(expected), withoutByteArray(actual));
    }

    private static ItemStack withoutByteArray(ItemStack original) {
        ItemStack copy = original.clone();
        var meta = copy.getItemMeta();
        meta.getPersistentDataContainer().remove(BYTE_ARRAY_KEY);
        copy.setItemMeta(meta);
        return copy;
    }

    private static ItemStack historicalItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 3);
        var meta = item.getItemMeta();
        meta.setDisplayName("Old saved blade");
        meta.setLore(List.of("Original lore", "Identity retained"));
        meta.setCustomModelData(12345);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        ((Damageable) meta).setDamage(17);
        var data = meta.getPersistentDataContainer();
        data.set(new NamespacedKey("slimefun", "slimefun_item"), PersistentDataType.STRING, "UNREGISTERED_OLD_ID");
        data.set(new NamespacedKey("magicexpansion", "owner"), PersistentDataType.STRING, "old-owner");
        data.set(new NamespacedKey("magicexpansion", "amount"), PersistentDataType.LONG, 9007199254740993L);
        data.set(BYTE_ARRAY_KEY, PersistentDataType.BYTE_ARRAY,
                new byte[] {0, 127, -128, -1});
        item.setItemMeta(meta);
        return item;
    }
}
