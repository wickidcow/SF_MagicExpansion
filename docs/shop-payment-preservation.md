# Portable shop payment and page preservation

The old normal-shop and black-market handlers validated duplicate cost rows separately. A player with 50 diamonds could pass two separate 40-diamond checks, lose only 50 and still receive the reward. The old removal fallback could also inspect slots outside player storage. Regular shop purchases ignored the displayed page offset.

The corrected shared helper reserves all payment quantities in a detached storage-slot snapshot before any inventory write. An incomplete payment leaves the entire inventory unchanged and grants neither reward nor purchase count. Successful payment keeps first-slot order, exact item similarity and metadata; armour/off-hand are not extra payment sources. Free trades, reward overflow and successful limit behavior remain as before. Regular purchases use the clamped displayed page, with overflow-safe index calculation.

No existing item/research ID, item metadata format, recipe, price, shop filename, stored quota key, machine rate or version is changed. This is not a transaction across reward delivery, disk persistence and concurrent external plugins. Existing shop-file failure policies, admin editor selection/identity and asynchronous chat mutations remain separate review work.

## Actual validation

Run36841964750 used the exact revision97 Legacy core artifact11138864515 and completed the full Maven build, 18 permanent JUnit tests, Java21 class checks and test-dependency exclusion. Payment tests include 1,000 deterministic randomized material layouts, duplicate/large costs, partial-payment refusal, exact typed old-item data and slot boundaries. The three page tests cover later pages, invalid slots and overflowing inputs.

Evidence11151444197 SHA256: `0b73e20dfc762d2728241b4246f379216beb22537b5a9c2d33b6bb230ee54040`. The downloaded actual XML and all six reviewed source blobs were independently checked. Exact-core candidate JAR SHA256: `491619a0acf6a564cd2242ab84d62eddb6d7688e8623530af0ef6bc2d71db111`.

A separate real-Paper probe calls the actual handlers with real Bukkit inventories and an explicitly synthetic no-network player facade. Its initial runtime attempt36842857424 stopped before server startup because Paper's API returned504. That setup failure is not accepted as the intended old-code control or successful runtime evidence. Runtime results must be recorded separately once completed.

## Local and normal CI build

The previous maintained Maven coordinate/version4.1.60 was unavailable, including in the normal repository workflow. The build helper now installs the actual published4.1.61 release into the local provided-API coordinate, verified against SHA256 `329e22688557fbd0e0d51dba02fdc1e2e8d9f363774b29dafb0a502f41d6024f`. This is a local compile dependency, not an uploaded Maven artifact or bundled core.

```sh
bash scripts/install_slimefun_api.sh
mvn -B -ntp clean verify
```

The normal workflow retains all three API compilation checks and executes the tests in the final1.21.11 baseline build. Its results for the promoted source remain required. Test libraries are test-only. Version1.1.7 remains an unreleased development candidate; no default-branch merge, stable release or server installation is performed by this change.
