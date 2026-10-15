package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlackMarketBootstrapGuardTest {
    @TempDir Path temporary;

    @Test void emptyStoreIsAllowedWithoutCreatingAMarker() throws Exception {
        Path root = Files.createDirectory(temporary.resolve("black_market"));
        Files.createDirectory(root.resolve("players"));
        BlackMarketBootstrapGuard.requireEmptyStore(root);
        assertFalse(Files.exists(root.resolve("cycle.yml")));
    }

    @Test void nonemptyPlayerDirectoryIsNeverTreatedAsFresh() throws Exception {
        for (String name : new String[] {"record.yml", "record.yml.bak", ".black-market-test.pending"}) {
            Path root = Files.createTempDirectory(temporary, "market-");
            Path players = Files.createDirectory(root.resolve("players"));
            Path file = players.resolve(name);
            byte[] original = new byte[] {0, 1, 2, (byte) 0xff};
            Files.write(file, original);
            assertThrows(IOException.class, () -> BlackMarketBootstrapGuard.requireEmptyStore(root));
            assertArrayEquals(original, Files.readAllBytes(file));
            assertFalse(Files.exists(root.resolve("cycle.yml")));
        }
    }

    @Test void emptyNestedDirectoryStillRequiresReconciliation() throws Exception {
        Path root = Files.createDirectory(temporary.resolve("black_market"));
        Files.createDirectories(root.resolve("players/archived"));
        assertThrows(IOException.class, () -> BlackMarketBootstrapGuard.requireEmptyStore(root));
        assertTrue(Files.isDirectory(root.resolve("players/archived")));
    }

    @Test void rootEvidenceIsNotDeletedOrIgnored() throws Exception {
        for (String name : new String[] {".black-market-test.pending", "cycle.yml.bak", "notes.txt"}) {
            Path root = Files.createTempDirectory(temporary, "market-");
            Files.createDirectory(root.resolve("players"));
            Path file = root.resolve(name);
            Files.writeString(file, "unreconciled state");
            assertThrows(IOException.class, () -> BlackMarketBootstrapGuard.requireEmptyStore(root));
            assertEquals("unreconciled state", Files.readString(file));
            assertFalse(Files.exists(root.resolve("cycle.yml")));
        }
    }

    @Test void missingOrNonDirectoryRootIsNotAssumedEmpty() throws Exception {
        Path missing = temporary.resolve("absent");
        assertThrows(IOException.class, () -> BlackMarketBootstrapGuard.requireEmptyStore(missing));
        assertFalse(Files.exists(missing));
        Path regularFile = Files.writeString(temporary.resolve("not-a-directory"), "preserve");
        assertThrows(IOException.class, () -> BlackMarketBootstrapGuard.requireEmptyStore(regularFile));
        assertEquals("preserve", Files.readString(regularFile));
    }

    @Test void playersPathMustBeARealDirectory() throws Exception {
        Path root = Files.createDirectory(temporary.resolve("black_market"));
        Path file = Files.writeString(root.resolve("players"), "preserve");
        assertThrows(IOException.class, () -> BlackMarketBootstrapGuard.requireEmptyStore(root));
        assertEquals("preserve", Files.readString(file));
    }
}
