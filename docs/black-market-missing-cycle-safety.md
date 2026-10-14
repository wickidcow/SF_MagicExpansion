# Missing Black Market cycle marker: fail-closed startup

## Scope

Follow-up to the read-only diagnostics in 1.1.12. This is an unreleased safety change, not a repair for Albion's unexplained externally changed `cycle.yml`.

Before this change, `loadOrCreate` generated a new cycle whenever `cycle.yml` was absent, even when `players/` still held purchase receipts. Those records then belonged to an old cycle and could be replaced with fresh offers and empty purchase limits after a restart.

Startup now permits a new marker only when the checked Black Market directory contains no state other than an empty real `players/` directory. Any player-directory entry, including malformed files, backups, nested directories and pending writes, blocks bootstrap. Other root entries also block bootstrap when the marker is missing. Directory access failures propagate rather than being treated as an empty store. The guard lists entries only; it does not deserialize, delete, rewrite or adopt any state.

## Preservation

The normal existing-marker startup path, four-hour rotation, exact-byte conflict checks, receipt-before-reward ordering, item/PDC serialization, prices, rewards, configurations, version and release workflows are unchanged. No recovery command or automatic unlock is added.

If startup is blocked, preserve the entire `black_market/` folder and reconcile it against a verified consistent backup while the server is stopped. Do not delete pending files or receipts to make the guard pass. A missing marker alone is not permission to reset purchase limits.

This guard cannot reconstruct deleted records, identify the writer responsible for a live mismatch, detect deletion of the entire data folder, or provide cross-process locking/crash-atomic guarantees. Existing-cycle receipt reconciliation remains a separate operator task.

## Validation

- Regression tests exercise actual `BlackMarketFileStore` startup refusal with persisted purchase/reveal receipts and custom PDC items; missing-marker failure must preserve bytes and reject transactions/refresh. Restoring the exact original synthetic marker must retain the same offers and receipts.
- Additional tests cover orphaned malformed ledgers, interrupted cycle writes and clean first-install/restart behavior.
- Filesystem-only guard tests cover nonempty directories and other state evidence without requiring a server API.
- A separate local Java 21 probe compiled the actual guard with `--release 21 -Xlint:all -Werror` and passed 15 synthetic filesystem cases, including symlinks and byte preservation. This probe is not shipped.
- Full Maven/MockBukkit, supported-version compilation, coordinated addon validation and copied Albion state verification remain release gates. Do not call the local probe a complete plugin build or a live-server fix.

No release asset, version, Slimefun core, Networks code or addon ZIP is changed by this candidate.
