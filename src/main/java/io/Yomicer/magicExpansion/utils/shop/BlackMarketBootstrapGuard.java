package io.Yomicer.magicExpansion.utils.shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/** Checks only for existing state; never reads, deletes or repairs purchase records. */
final class BlackMarketBootstrapGuard {
    private BlackMarketBootstrapGuard() {}

    /**
     * A missing cycle marker is a fresh install only when no state remains.
     * Called after the store has checked/created its real root and players directories.
     * Any entry, including a malformed ledger or an interrupted-write file, is evidence
     * that must be reconciled offline instead of silently invalidating purchase limits.
     */
    static void requireEmptyStore(Path root) throws IOException {
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Black Market storage is not a readable directory");
        }
        try (var entries = Files.newDirectoryStream(root)) {
            for (Path entry : entries) {
                if (!entry.getFileName().toString().equals("players")
                        || !Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                    throw existingState();
                }
                try (var ledgers = Files.newDirectoryStream(entry)) {
                    if (ledgers.iterator().hasNext()) throw existingState();
                }
            }
        }
    }

    private static IOException existingState() {
        return new IOException("Black Market cycle marker is missing but existing state remains; "
                + "refusing to reset purchase limits. Restore a consistent black_market backup.");
    }
}
