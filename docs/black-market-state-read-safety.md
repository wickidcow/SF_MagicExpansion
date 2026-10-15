# Black Market state-read and session safety

Unreleased follow-up in PR #18. This does not diagnose or repair Albion's original externally changed cycle marker, and does not authorize deleting receipts or bypassing a lock.

## Additional protections

- State reads no longer use `Files.exists` to infer absence: that method also returns false when existence cannot be determined. The reader uses no-follow file attributes and treats only `NoSuchFileException` from that initial inspection as a missing record. Access errors and other storage errors propagate.
- Existing empty files remain existing bytes and fail normal YAML validation. Directories, symlinks and dangling symlinks are not accepted as records. The final file open also refuses symlinks. A file disappearing after successful inspection is an error, not a fresh-record signal.
- Starting `loadOrCreate` invalidates the old cycle snapshot before any potentially failing filesystem work. A failed initialization cannot retain stale authorization after the disk files are subsequently restored. An explicit successful initialization is still required.
- `refresh` now validates the caller's previous cycle ID and start time, as well as the saved snapshot, before generating a new marker. A stale or mismatched request cannot silently reset the current cycle.

Four-hour scheduling, one purchase per slot, the existing operator refresh capability, costs/rewards, player/item identities, persistent format and receipt-before-reward ordering are unchanged. No automatic repair, unlock, version bump or release mutation is added.

## Regression coverage

Nine filesystem reader tests cover genuine absence, byte preservation, empty records, deterministic access-denied and generic I/O failures, disappearance after inspection, directories, existing/dangling symlinks and a deterministic final-component symlink swap. Six store/MockBukkit tests cover failed initialization, directory failure, wrong cycle IDs/timestamps, legitimate rotation followed by a stale request, an empty marker, and preservation of custom-PDC offers and purchase/reveal receipts.

The actual production reader also compiled locally on Java 21 with `--release 21 -Xlint:all -Werror`. A separate non-shipping probe passed 12/12 cases. This was not a full plugin/Maven build; the environment lacks Maven/Java 25 and cannot fetch build dependencies. Exact-head GitHub CI supplies full-build and permanent-test evidence.

The pre-follow-up head `d1c2eda54d47f68b586da5ae60fb2f7001d0caeb` passed workflow `38657376626`, including the supported API compilation checks and baseline JAR build. That green result must not be attributed to the new follow-up commit. Check its own workflow and test reports.

## Boundaries

These are fail-closed safeguards, not cross-process locks or crash-atomic transactions. An external process can still replace a regular file or a parent directory between filesystem operations. This change cannot reconstruct deleted receipts, detect deletion of the whole store, or reconcile a real production backup. Copied Albion state and coordinated Slimefun/addon runtime validation remain separate release gates. Keep published MagicExpansion 1.1.12, Networks 1.0.55, core/addon assets and resource pack unchanged until that review.
