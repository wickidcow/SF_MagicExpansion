# Black Market stable-read safety candidate

This is an unreleased follow-up to published 1.1.14. The review branch retains the inherited 1.1.14 build identity for coordinated validation only: do not install its artifact as the published JAR, merge to the publishing default branch, dispatch the release build or replace an official addon ZIP. A distinct version and controlled publication are required after validation.

## Problem and scope

The existing reader inspects a ledger before opening it with NOFOLLOW_LINKS, but returns the bytes without another inspection. A detectable regular-file rewrite or replacement between the initial inspection and completion can therefore be accepted as a successful read. This is a read-boundary gap, not a confirmed explanation of Albion's original cycle.yml mismatch and not proof of player cheating.

The candidate takes a second NOFOLLOW_LINKS attribute sample after reading. It rejects a changed file identity, size, modification time, creation time, nonregular replacement, or a byte count inconsistent with the sampled size. A failed second inspection (including disappearance or denied access) remains an error, never a missing/new ledger. Access time is deliberately ignored because reading can change it. Providers returning null file keys on both inspections remain supported.

The reader writes, deletes and repairs nothing. Existing cycle-byte comparison, purchase-before-reward persistence and fail-closed trading behavior remain in place. It does not adopt an external cycle, reroll offers, reset receipts, unlock trading or change the save schema. Four-hour rotation, ten offers, one purchase per slot, costs, rewards, metadata and item IDs are unchanged.

## Limits

This is additional change detection, not a filesystem lock or proof of an atomic snapshot. Metadata sampling need not be atomic; timestamp resolution, unavailable/reused file keys and writers that preserve or restore metadata can hide changes. A file can also change after the final inspection. Do not run multiple server instances against the same market folder, and do not edit/restore/sync live market records. Preserve the entire stopped-server folder when investigating.

One extra attribute read is performed per existing state-file read. No inventory, machine or reward path is otherwise changed. No measured server performance improvement is claimed.

## Validation and integration

The original disposable Java 21 filesystem probe used baseline reader blob `26d2583ba6ec8b9307bf89479d3c58ffa569f610`. Its recorded baseline result was 7 passing and 11 failing expectations; candidate 18/18 passing. Those were filesystem checks, not Minecraft client tests.

The original PR head `bb44a5bf908e9cf2bfe92ad38661860e4d42d892` also completed build/API workflow `38670500775`. On October 15, the actual report artifact `11943611926` was independently downloaded and its SHA-256 verified as `42cefb20c366e739b0a0bc69de5a007cc6fea20640cbb942ece76367c68a69ea`. Its 15 XML suites contain 160 tests, zero failures/errors/skips, including all 14 `BlackMarketStableReadTest` cases. This is historical evidence for the old head, not evidence for the refreshed integration.

The branch now incorporates current master `481e6c090baa28fc8a4b3a44b6155d236f28bf33`, preserving the published 1.1.14 native full-item serialization fix, its regression tests and runtime probes. The upstream comparison contained no overlapping production/test paths. The stable-read production blob remains `84c0b97ac88bc40344d87d1851a0c1811e2851ba` and its complete test blob remains `5c3d8b099ffc11a969e84e2e5ef55f0a6bb3ae00`; no assertions were removed or weakened.

Fresh exact-head build/API, real-pool/player-flow and coordinated published-core/addon runtime results are required. The inherited audit workflows create disposable candidates only; an artifact carrying revision 138 or version 1.1.14 is not the previously approved published candidate. Preserve source hashes and do not promote these branch artifacts under an existing release identity. The default build workflow publishes on manual dispatch/default-branch/tag execution, so none of those release triggers is authorized by this review.

No release version, release workflow, core source-selection matrix, addon ZIP or production record is changed by this candidate. The original master files, other than this isolated reader, are retained unchanged.
