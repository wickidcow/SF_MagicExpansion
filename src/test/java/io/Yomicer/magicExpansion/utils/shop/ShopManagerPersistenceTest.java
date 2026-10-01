package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShopManagerPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void malformedYamlIsRejectedInsteadOfBecomingAnEmptyShop() throws Exception {
        Path file = tempDir.resolve("broken.yml");
        Files.writeString(file, "trades:\n  - result: [\n", StandardCharsets.UTF_8);

        assertThrows(InvalidConfigurationException.class, () -> ShopManager.loadShop(file.toFile()));
    }

    @Test
    void malformedTradeShapeIsRejectedWithoutPartialLoading() throws Exception {
        Path file = tempDir.resolve("broken-shape.yml");
        Files.writeString(file, "trades:\n  - definitely-not-a-trade\n", StandardCharsets.UTF_8);

        ShopManager.ShopFormatException error = assertThrows(
                ShopManager.ShopFormatException.class,
                () -> ShopManager.loadShop(file.toFile())
        );
        assertTrue(error.getMessage().contains("not a map"));
    }

    @Test
    void validEmptyShopStillLoadsAsEmptyForBackwardCompatibility() throws Exception {
        Path file = tempDir.resolve("empty.yml");
        Files.writeString(file, "trades: []\n", StandardCharsets.UTF_8);

        ShopManager.Shop shop = ShopManager.loadShop(file.toFile());

        assertEquals("empty", shop.name);
        assertTrue(shop.trades.isEmpty());
    }

    @Test
    void atomicWriterReplacesExistingFileWithCompleteYaml() throws Exception {
        File file = tempDir.resolve("shop.yml").toFile();
        Files.writeString(file.toPath(), "trades: old\n", StandardCharsets.UTF_8);

        String replacement = "trades:\n  - globalLimit: 0\n";
        ShopManager.saveYamlAtomically(file, replacement);

        assertEquals(replacement, Files.readString(file.toPath(), StandardCharsets.UTF_8));
        try (var stream = Files.list(tempDir)) {
            assertEquals(1L, stream.count(), "temporary shop-save files must be cleaned up");
        }
    }
}
