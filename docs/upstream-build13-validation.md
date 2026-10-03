# Upstream Build 13 integration validation

This integrates Yomicer/MagicExpansion `39471f40826fcc815b64c3b01ade20ddcb9360ad`
(October 1, 2026; GitHub Build 13, internal Build 93) into the maintained English
fork. Production changes were reviewed at `32ed60f5d1a0abdf62434ec0da027b3f636ea91d`.

## Build and identity checks

- All 74 unit tests passed: 61 existing shop regressions, 10 cargo regressions,
  and 3 sword attack re-entry regressions. No failures, errors, or skips.
- Compiled against Paper 1.21.11, the maintained 26.2 API range, and
  `26.3-rc-3.build.1-alpha` using Java 25 and Java 21 bytecode.
- Full tests passed against both checksum-pinned Slimefun Legacy 4.1.61 and
  public Slimefun Legacy 4.1.66.
- The existing sword attribute constructor is unchanged apart from comments.
  Item/recipe registration and plugin identity files are unchanged.
- Added production text is English. Existing historical IDs and stored keys
  are retained. The sword's Mercy lore now correctly describes its nonlethal bleed.
- Initial independent GitHub builds passed: push run 37080066461 and PR run
  37080068151. Subsequent probe/documentation commits receive their own CI checks.

## Actual Paper runtime and restart

`scripts/smoke_shop_safety.sh` was executed in a newly allocated disposable
server directory, using Java 25, Paper 26.3 beta build 143, and Legacy 4.1.66.
Both server processes completed successfully.

Input SHA-256 values:

| Input | SHA-256 |
| --- | --- |
| Paper 26.3 build 143 | `32cf4a93545e218525bc4536b017c6b5d5085d27d449d64266a6b23ba4d0cbb9` |
| Slimefun Legacy 4.1.66 | `514730d71bd276b5ddcd6f46a665605be122e568f838458f66527ad396c22649` |
| Tested MagicExpansion JAR | `f9045c726341e6122790f632db76df5db771d1747c6754def799c04fe3fa032f` |

The new probe checks real Bukkit item serialization with an unregistered old
Slimefun ID, FLOAT charge, a large LONG, and nested owner data. It checks full
storage refusal, large-count acceptance, long overflow boundaries, the registered
sword's cancelled/nested attack handling, shield projectile reflection, and
shield cleanup. The retained shop probe also checks invalid-file preservation,
editor identity, usage counters, async chat handoff, and a separate-process restart.

The sword player is explicitly a no-network facade. Cargo uses an in-memory
Slimefun data container with real Paper item serialization. These checks do not
constitute live-client combat/visual testing, concurrent external-plugin transfer
testing, crash-atomicity, historical-world certification, or Folia region testing.
Expected corrupt-shop refusal diagnostics and unrelated test-environment warnings
were present; the logs are not described as globally warning/error-free.

The import preserves unaccepted items at their source instead of copying
upstream's 64-stack capped emergency drop routine. It retains the original
percentage bleed and adds protection checks; the new direct/area attack damage
uses upstream's Bukkit damage-event approach. The release version is unchanged.
