# Black Market stable-read safety candidate

This follow-up to published 1.1.13 is unreleased. Do not install a branch artifact as the published 1.1.13 JAR or replace the official addon ZIP with it.

## Problem and scope

The existing reader inspects a ledger before opening it with NOFOLLOW_LINKS, but returns the bytes without another inspection. A detectable regular-file rewrite or replacement between the initial inspection and completion can therefore be accepted as a successful read. This is a read-boundary gap, not a confirmed explanation of Albion's original cycle.yml mismatch and not proof of player cheating.

The candidate takes a second NOFOLLOW_LINKS attribute sample after reading. It rejects a changed file identity, size, modification time, creation time, nonregular replacement, or a byte count inconsistent with the sampled size. A failed second inspection (including disappearance or denied access) remains an error, never a missing/new ledger. Access time is deliberately ignored because reading can change it. Providers returning null file keys on both inspections remain supported.

The reader writes, deletes and repairs nothing. Existing cycle-byte comparison, purchase-before-reward persistence and fail-closed trading behavior remain in place. It does not adopt an external cycle, reroll offers, reset receipts, unlock trading or change the save schema. Four-hour rotation, ten offers, one purchase per slot, costs, rewards, metadata and item IDs are unchanged.

## Limits

This is additional change detection, not a filesystem lock or proof of an atomic snapshot. Metadata sampling need not be atomic; timestamp resolution, unavailable/reused file keys and writers that preserve or restore metadata can hide changes. A file can also change after the final inspection. Do not run multiple server instances against the same market folder, and do not edit/restore/sync live market records. Preserve the entire stopped-server folder when investigating.

One extra attribute read is performed per existing state-file read. No inventory, machine or reward path is otherwise changed. No measured server performance improvement is claimed.

## Validation

A disposable Java 21 filesystem probe uses the exact baseline reader blob `26d2583ba6ec8b9307bf89479d3c58ffa569f610`. It reproduces a same-length rewrite after initial inspection that the baseline accepts and the candidate rejects. The probe also exercises deterministic post-read failure boundaries and synthetic metadata-provider changes. Baseline result: 7 passing and 11 failing new/retained expectations; candidate result: 18/18 passing. These are local filesystem checks, not Maven/JUnit or Minecraft runtime results.

`BlackMarketStableReadTest` adds 14 permanent JUnit tests for rewrites/replacements, post-read deletion/access failures, symlink/directory substitutions, optional identities, access-time-only changes and byte-count/metadata disagreements. Existing reader and persistence tests are retained. Record actual exact-head CI results in the PR; do not infer their success from the local probe.

Release still requires the complete source test/build workflow, supported API compilation and coordinated Slimefun/addon runtime validation. No release version, release workflow, core, source-selection matrix, addon ZIP or production record is changed by this candidate.
