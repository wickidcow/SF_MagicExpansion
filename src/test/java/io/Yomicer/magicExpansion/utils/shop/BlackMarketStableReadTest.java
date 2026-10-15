package io.Yomicer.magicExpansion.utils.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Deterministic read-boundary races; mutations belong to the test's external writer. */
class BlackMarketStableReadTest {
    @TempDir Path temporary;

    private Path receipt() throws IOException {
        return Files.writeString(temporary.resolve("ledger.yml"), "receipt", StandardCharsets.UTF_8);
    }

    private static BasicFileAttributes attributes(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }

    @Test void sameLengthRewriteAfterInitialInspectionIsRejected() throws Exception {
        Path file = receipt();
        AtomicInteger inspections = new AtomicInteger();
        IOException failure = assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> {
            BasicFileAttributes before = attributes(path);
            if (inspections.getAndIncrement() == 0) {
                Files.writeString(path, "changed", StandardCharsets.UTF_8);
                Files.setLastModifiedTime(path, FileTime.fromMillis(before.lastModifiedTime().toMillis() + 5000));
            }
            return before;
        }));
        assertTrue(failure.getMessage().contains("changed during read"));
        assertEquals(2, inspections.get());
        assertEquals("changed", Files.readString(file), "Do not overwrite the external writer's evidence");
    }

    @Test voidreplacementAfterReadingBytesIsRejected() throws Exception {
        Path file = receipt();
        AtomicInteger inspections = new AtomicInteger();
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> {
            if (inspections.getAndIncrement() == 1) {
                Path staged = Files.writeString(temporary.resolve("external-save"), "different receipt");
                Files.move(staged, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return attributes(path);
        }));
        assertEquals("different receipt", Files.readString(file));
        assertFalse(Files.exists(temporary.resolve("external-save")));
    }

    @Test void deletionAfterReadingBytesIsAnErrorNotAMissingRecord() throws Exception {
        Path file = receipt();
        AtomicInteger inspections = new AtomicInteger();
        assertThrows(NoSuchFileException.class, () -> BlackMarketStateReader.read(file, path -> {
            if (inspections.getAndIncrement() == 1) Files.delete(path);
            return attributes(path);
        }));
        assertFalse(Files.exists(file), "The reader must not create a replacement record");
    }

    @Test void permissionFailureAfterReadingBytesPropagates() throws Exception {
        Path file = receipt();
        AtomicInteger inspections = new AtomicInteger();
        AccessDeniedException denied = new AccessDeniedException(file.toString());
        IOException actual = assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> {
            if (inspections.getAndIncrement() == 1) throw denied;
            return attributes(path);
        }));
        assertSame(denied, actual);
        assertEquals("receipt", Files.readString(file));
    }

    @Test void symlinkIntroducedAfterReadingBytesIsRejectedWithoutFollowingIt() throws Exception {
        Path file = receipt();
        Path target = Files.writeString(temporary.resolve("unrelated.yml"), "keep unrelated receipt");
        AtomicInteger inspections = new AtomicInteger();
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> {
            if (inspections.getAndIncrement() == 1) {
                Files.delete(path);
                Files.createSymbolicLink(path, target);
            }
            return attributes(path);
        }));
        assertTrue(Files.isSymbolicLink(file));
        assertEquals("keep unrelated receipt", Files.readString(target));
    }

    @Test void directoryIntroducedAfterReadingBytesIsRejectedWithoutDeletingEvidence() throws Exception {
        Path file = receipt();
        AtomicInteger inspections = new AtomicInteger();
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> {
            if (inspections.getAndIncrement() == 1) {
                Files.delete(path);
                Files.createDirectory(path);
                Files.writeString(path.resolve("evidence"), "keep");
            }
            return attributes(path);
        }));
        assertEquals("keep", Files.readString(file.resolve("evidence")));
    }

    @Test void accessTimeOnlyChangesAreAllowed() throws Exception {
        Path file = receipt();
        BasicFileAttributes actual = attributes(file);
        AtomicInteger inspections = new AtomicInteger();
        byte[] read = BlackMarketStateReader.read(file, path -> new Sample(actual, actual.fileKey(),
                actual.size(), actual.lastModifiedTime(), actual.creationTime(),
                FileTime.fromMillis(5000L + inspections.getAndIncrement())));
        assertEquals(2, inspections.get());
        assertArrayEquals("receipt".getBytes(StandardCharsets.UTF_8), read);
        assertEquals("receipt", Files.readString(file));
    }

    @Test void stableProviderWithoutFileKeysIsSupported() throws Exception {
        Path file = receipt();
        BasicFileAttributes actual = attributes(file);
        Sample withoutKey = new Sample(actual, null, actual.size(), actual.lastModifiedTime(),
                actual.creationTime(), actual.lastAccessTime());
        assertArrayEquals("receipt".getBytes(StandardCharsets.UTF_8),
                BlackMarketStateReader.read(file, path -> withoutKey));
        assertEquals("receipt", Files.readString(file));
    }

    @Test void changedIdentityWithSameSizeAndTimestampsIsRejected() throws Exception {
        assertMetadataChange((actual, second) -> new Sample(actual, second ? "new-file" : "old-file",
                actual.size(), actual.lastModifiedTime(), actual.creationTime(), actual.lastAccessTime()));
    }

    @Test void lostFileIdentityBetweenInspectionsIsRejected() throws Exception {
        assertMetadataChange((actual, second) -> new Sample(actual, second ? null : "original-file",
                actual.size(), actual.lastModifiedTime(), actual.creationTime(), actual.lastAccessTime()));
    }

    @Test void changedModificationTimeWithSameSizeIsRejected() throws Exception {
        assertMetadataChange((actual, second) -> new Sample(actual, actual.fileKey(), actual.size(),
                FileTime.fromMillis(second ? 6000 : 5000), actual.creationTime(), actual.lastAccessTime()));
    }

    @Test void changedCreationTimeWithSameSizeIsRejected() throws Exception {
        assertMetadataChange((actual, second) -> new Sample(actual, actual.fileKey(), actual.size(),
                actual.lastModifiedTime(), FileTime.fromMillis(second ? 6000 : 5000), actual.lastAccessTime()));
    }

    @Test void changedSizeWithSameIdentityAndTimestampsIsRejected() throws Exception {
        assertMetadataChange((actual, second) -> new Sample(actual, actual.fileKey(),
                actual.size() + (second ? 1 : 0), actual.lastModifiedTime(), actual.creationTime(), actual.lastAccessTime()));
    }

    @Test voidBytesReadMustMatchTheInspectedLength() throws Exception {
        Path file = receipt();
        BasicFileAttributes actual = attributes(file);
        Sample longer = new Sample(actual, actual.fileKey(), actual.size() + 1, actual.lastModifiedTime(),
                actual.creationTime(), actual.lastAccessTime());
        assertThrows(IOException.class, () -> BlackMarketStateReader.read(file, path -> longer));
        assertEquals("receipt", Files.readString(file));
    }

    @FunctionalInterface
    private interface Samples {
        BasicFileAttributes get(BasicFileAttributes actual, boolean second);
    }

    private void assertMetadataChange(Samples samples) throws Exception {
        Path file = receipt();
        BasicFileAttributes actual = attributes(file);
        AtomicInteger inspections = new AtomicInteger();
        IOException failure = assertThrows(IOException.class, () -> BlackMarketStateReader.read(file,
                path -> samples.get(actual, inspections.getAndIncrement() > 0)));
        assertEquals(2, inspections.get());
        assertTrue(failure.getMessage().contains("changed during read"));
        assertEquals("receipt", Files.readString(file));
    }

    private record Sample(BasicFileAttributes base, Object key, long bytes,
                          FileTime modified, FileTime created, FileTime accessed) implements BasicFileAttributes {
        @Override public FileTime lastModifiedTime() { return modified; }
        @Override public FileTime lastAccessTime() { return accessed; }
        @Override public FileTime creationTime() { return created; }
        @Override public boolean isRegularFile() { return base.isRegularFile(); }
        @Override public boolean isDirectory() { return base.isDirectory(); }
        @Override public boolean isSymbolicLink() { return base.isSymbolicLink(); }
        @Override public boolean isOther() { return base.isOther(); }
        @Override public long size() { return bytes; }
        @Override public Object fileKey() { return key; }
    }
}
