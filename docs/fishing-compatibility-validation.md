# Fishing compatibility validation

The automatic compatibility change routes both existing fishing rod handlers through one event listener before any bait, catch or effect is changed. It does not introduce external plugin API dependencies or change item registrations, stored identifiers, recipes or machines.

## Automated checks

- 97 tests passed against the checksum-pinned Slimefun Legacy 4.1.61 baseline, including 21 new fishing checks; no failures, errors or skips.
- The same 97 tests passed against the published Slimefun Legacy 4.1.68 JAR, SHA-256 `6f934cbdd17cf5a51b9bbace691d7adc45da33c65aaf9fb75c52adbc7c7e50fa`.
- Compilation passed against Paper 1.21.11, 26.2 build 112 and 26.3 build 143. GitHub PR and push runs also passed the repository's baseline, 26.2 range and pinned 26.3 alpha checks.
- The core repository's binary-linkage verifier passed for the baseline-built candidate against 4.1.68: 464 addon classes, 88 baseline-proven Slimefun class references and 420 baseline-proven member references.
- The universal JAR retains Java 21 bytecode and does not include unit-test libraries or runtime probes.

The fishing tests cover every built-in plugin name, case-insensitive names, additional names, unrelated/disabled plugins, both startup orders, multiple fishing plugins, enable/disable transitions, explicit modes, invalid-mode fallback, configuration reload, preservation of typed fish/rod/bait data and XP, cancellation, missing/removed catches, and pending entities that are not yet valid in the world.

## Current-core server checks

The baseline-built fishing candidate was run with the published 4.1.68 core on an isolated Paper 26.3 build 143 server. Both server processes completed successfully. The core JAR and canonical revision-109 addon bundle were downloaded from the v4.1.68 release and checked against the release's published SHA-256 values. The bundle checksum is `c073a02928af712d8372c10a28d94cf338c7d2cf75bc7762122295ba404035e5`.

The retained `FishingCompatibilityChecks` probe dispatches through the actual registered MagicExpansion listener and verifies both rod families. Full mode replaces the catch and produces a reward; Water Cloud consumes exactly one supported bait. Compatibility mode and cancelled events preserve the original catch, bait, rod data and XP without spawning rewards or catch effects. A newly created real Paper item that has not been added to the world verifies that `isValid() == false` does not incorrectly prevent normal catches.

The existing runtime suite also passes its cargo, sword, corrupt-shop preservation, editor, async handoff and two-process persistence checks. `scripts/smoke_shop_safety.sh` includes the new fishing probe. Test fixtures are kept out of the plugin JAR.

## Scope

The server checks use a no-network player facade, while the enabled-plugin identity and lifecycle tests use MockBukkit. They are evidence for MagicExpansion yielding catch control, not proof of actual third-party fishing plugin behavior or a fish-conversion bridge. No usable PyroFishing/BetterFish JARs were resolved from the available uploads. Actual plugin combinations and live-client fishing remain manual checks. The update makes no new Folia, historical-world, arbitrary-concurrency or crash-atomicity claim.

The planned release refresh targets MagicExpansion alone on the unchanged published 4.1.68 core. The other 44 addon JARs must remain byte-identical. The core repository's targeted-refresh guards must reject a changed live release, bundle checksum, core checksum or unrelated source pin.
