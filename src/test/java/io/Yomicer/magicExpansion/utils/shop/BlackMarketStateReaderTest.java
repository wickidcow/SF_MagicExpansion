package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlackMarketStateReaderTest {
    @TempDir Path temporary;

    @Test void genuinelyMissingRecordReturnsNullWithoutCreatingAnything() throws Exception {
        Path file = temporary.resolve("missing.yml");
        assertNull(BlackMarketStateReader.read(file));
        assertFalse(Files.exists(file));
    }

    @Test void existingBytesAreReturnedUnchanged() throws Exception {
        byte[] original = new byte[] {0, 17, (byte) 0xff, 42};
        Path file = Files.write(temporary.resolve("ledger.yml"), original);
        assertArrayEquals(original, BlackMarketStateReader.read(file));
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test void emptyExistingRecordIsNotReportedMissing() throws Exception {
        Path file = Files.createFile(temporary.resolve("empty.yml"));
        assertArrayEquals(new byte[0], BlackMarketStateReader.read(file));
        assertTrue(Files.exists(file));
    }

    @Test void attributeAccessFailureIsNotReportedMissing() throws Exception {
        Path file = Files.writeString(temporary.resolve("ledger.yml"), "preserve receipt");
        var denied = new AccessDeniedException(file.toString());
        IOException actual = assertThrows(IOException.class,
                () -> BlackMarketStateReader.read(file, path -> { throw denied; }));
        assertSame(denied, actual);
        assertEquals("preserve receipt", Files.readString(file));
    }

    @Test void otherAttributeIoFailuresAreNotReportedMissing() throws Exception {
        Path file = Files.writeString(temporary.resolve("ledger.yml"), "preserve receipt");
        var failure = new IOException("Synthetic storage read failure");
        assertSame(failure, assertThrows(IOException.class,
                () -> BlackMarketStateReader.read(file, path -> { throw failure; })));
        assertEquals("preserve receipt", Files.readString(file));
    }

    @Test void recordDisappearingAfterInspectionFailsInsteadOfReturningNull() throws Exception {
        Path file = Files.writeString(temporary.resolve("ledger.yml"), "receipt");
        assertThrows(NoSuchFileException.class, () -> BlackMarketStateReader.read(file, path -> {
            BasicFileAttributes attributes = Files.readAttributes(path,
                    BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            Files.delete(path); // Synthetic external deletion, not a reader operation.
            return attributes;
        }));
        assertFalse(Files.exists(file), "The reader must not recreate a disappeared record");
    }

    @Test void directoryIsRejectedWithoutChangingItsContents() throws Exception {
        Path directory = Files.createDirectory(temporary.resolve("ledger.yml"));
        Path evidence = Files.writeString(directory.resolve("evidence"), "keep");
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(directory));
        assertEquals("keep", Files.readString(evidence));
    }

    @Test void existingAndDanglingSymlinksAreRejected() throws Exception {
        Path target = Files.writeString(temporary.resolve("target.yml"), "keep");
        Path link = Files.createSymbolicLink(temporary.resolve("ledger.yml"), target);
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(link));
        assertEquals("keep", Files.readString(target));
        Files.delete(target);
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(link));
        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(target));
    }

    @Test void symlinkIntroducedAfterInspectionIsNotFollowed() throws Exception {
        Path target = Files.writeString(temporary.resolve("target.yml"), "unrelated receipt");
        Path file = Files.writeString(temporary.resolve("ledger.yml"), "original");
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> {
            BasicFileAttributes attributes = Files.readAttributes(path,
                    BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            Files.delete(path);
            Files.createSymbolicLink(path, target); // Simulate an external replacement.
            return attributes;
        }));
        assertEquals("unrelated receipt", Files.readString(target));
        assertTrue(Files.isSymbolicLink(file));
    }
}
