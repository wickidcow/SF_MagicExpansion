package io.Yomicer.magicExpansion.utils.shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Reads existing state without treating an indeterminate file check as an empty ledger.
 * Detectable changes between inspections fail closed. This is not a filesystem lock:
 * providers can omit file keys and external writers can preserve or restore metadata.
 */
final class BlackMarketStateReader {
    private BlackMarketStateReader() {}

    @FunctionalInterface
    interface AttributeReader {
        BasicFileAttributes read(Path path) throws IOException;
    }

    static byte[] read(Path path) throws IOException {
        return read(path, candidate -> Files.readAttributes(candidate,
                BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
    }

    static byte[] read(Path path, AttributeReader attributes) throws IOException {
        final BasicFileAttributes existing;
        try {
            existing = attributes.read(path);
        } catch (NoSuchFileException missing) {
            // Only a positively absent path may be treated as a new record. Access
            // failures and other I/O errors must propagate, never become null.
            return null;
        }
        if (!existing.isRegularFile()) {
            throw new IOException("Black Market state is not a regular file: " + path);
        }
        // Keep the open outside the missing-file catch: a file which disappears
        // after inspection is a failed read, not permission to create a new record.
        // Refuse a final-component symlink introduced between inspection and open.
        final byte[] contents;
        try (var input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
            contents = input.readAllBytes();
        }

        // Recheck outside the missing-file catch. A replacement, rewrite or failed
        // second inspection is not permission to adopt a different purchase history.
        // Ignore access time: this read itself may legitimately change it.
        BasicFileAttributes after = attributes.read(path);
        if (!after.isRegularFile()
                || !Objects.equals(existing.fileKey(), after.fileKey())
                || existing.size() != after.size()
                || after.size() != contents.length
                || !existing.lastModifiedTime().equals(after.lastModifiedTime())
                || !existing.creationTime().equals(after.creationTime())) {
            throw new IOException("Black Market state changed during read; refusing inconsistent record: " + path);
        }
        return contents;
    }
}
