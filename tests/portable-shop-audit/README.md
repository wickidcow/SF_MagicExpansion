# Portable shop native fixtures

`native-items.json` retains the complete serialized custom ItemStacks used by the
four authored shop fixtures. `prepare_fixtures.py` verifies every stack against the
original 20 semantic SHA-256 fingerprints and then verifies each complete parsed
shop against its original fingerprint. Item identities, components, quantities,
prices and limits are unchanged. Fixtures contain no active receipts or live
player configuration.

The original workflow fetched a temporary GitHub Actions artifact that is now
unavailable. These stacks were recovered by registering the checksum-verified
published plugins in a disposable Paper server, converting each registered stack
through `ItemStack.deserialize(item.serialize())`, setting its amount to one, and
saving the native Bukkit YAML representation. All original fingerprints matched;
no expected fingerprint was replaced. Keeping the verified values in source
removes the dependency on Actions artifact retention.

## Recovery inputs

| Input | SHA-256 |
| --- | --- |
| Paper 26.3 build 190 | `db0fd32588e732fd0de57cc95a2076624b0e73c1cb2dba87dc5f5f3340bcbb1b` |
| Slimefun-Legacy 4.1.74 | `72a524d7c0d6f75a9decbf06679515887273ae53673d3f5ee38f355bef952c96` |
| Published SF_MagicExpansion 1.1.14 | `e3552fc5d54dc282194428d593d08c939f71172182075667ac87d10571b192dd` |

The workflow still builds the exact candidate plugin and requires real purchases,
refused-receipt rollback and once-per-player counters across two server processes.
The retained data repairs fixture availability; it does not replace native checks.
