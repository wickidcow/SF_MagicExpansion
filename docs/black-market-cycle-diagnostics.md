# Black Market cycle-integrity diagnostics (draft)

MagicExpansion 1.1.10+ persists the four-hour Black Market cycle marker and per-player offers, reveal flags and purchase receipts in `plugins/magicexpansion/black_market/`.

## Why a lock is better than a silent reset

The plugin compares the live `cycle.yml` bytes against the exact cycle marker it loaded. Another process or plugin instance can rewrite this file even if the decoded cycle ID and timestamp remain unchanged. Silently accepting such an edit could conceal a concurrent writer or inconsistent player purchase receipts. Therefore Black Market purchase/reveal commits and forced refreshes **must fail closed** on a changed marker.

The 2026-10-14 log sequence showed a purchase blocked by a changed cycle marker, followed by an operator using `/magicshop admin` and a failed refresh. The admin refresh did not cause the first mismatch.

## Operator-only read-only status

On a build containing this patch, run:

```text
/magicexpansion blackmarket status
```

The command requires OP access, reads the current `cycle.yml` only, and reports:

- Trading state (AVAILABLE or LOCKED).
- `MATCH`: marker unchanged since load.
- `SAME_CYCLE_DIFFERENT_BYTES`: valid marker with the same cycle ID and time, but different file bytes. **Still locked.**
- `DIFFERENT_CYCLE`: the file decodes to a different cycle ID/time. **Still locked.**
- `MISSING`: no cycle marker on disk.
- `UNREADABLE`: malformed file or an I/O/read failure.
- `NOT_LOADED`: no live in-memory cycle snapshot.
- Loaded and on-disk cycle IDs/start times, if safely readable, plus the number of player ledgers locked in memory.

This command does **not** regenerate player offers, accept a modified cycle, change receipt state, fix the mismatch, or perform any write.

## Recovery runbook for a real server

1. Stop player Black Market activity; keep trades locked, and stop the server normally.
2. Make a complete backup of `plugins/magicexpansion/black_market/` including `cycle.yml`, `players/` and any remaining pending files.
3. Record the file modification times and compare the cycle marker to the latest verified backup. Identify external editors, automatic restore/sync tools, simultaneous server instances or plugin reloaders before changing anything.
4. Check valid cycle ID and `started-at` fields and player receipt consistency. Do not delete `cycle.yml` or per-player YAML records.
5. Only after the discrepancy is understood, decide whether to restore a consistent complete folder from a verified backup, preserve the current state and restart, or keep trading disabled for manual reconciliation.
6. Reopen the market only after a clean restart with confirmed player receipts and no unexpected item reissue. The command reports diagnosis, not authorization to reset a cycle.

This runbook deliberately avoids automatic reconciliation: a complete player ledger cannot be inferred safely from `cycle.yml` alone.

## Test coverage

Tests verify that the read-only inspection classifies unchanged, same-cycle rewritten, different-cycle, missing and unreadable markers without mutating files. Existing transaction rejection and purchase receipts remain fail closed.
