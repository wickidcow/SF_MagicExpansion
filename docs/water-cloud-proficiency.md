# Water Cloud progression — compatibility-preserving port

This phase ports the original eight-level item-bound fishing proficiency into
**Slimefun Legacy** without changing existing rods, recipes, registration IDs,
loot weights or saved data.

## Supported rods

- Reedflower (`FISHING_ROD_BETWEEN_WATER_CLOUD_REED`)
- Snowy River (`FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG`)
- Fine Rain · Slanting Wind (`FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU`)

The starting Cyan Bamboo rod does not gain levels, matching the upstream
progression design. All four rod families now recognize their five respective
lures (20 in total) during the legacy catch flow.

## Existing-item compatibility

- Rod progression stays **on the physical item**, not on the player account.
- Reads and writes upstream's exact `magicexpansion:watercloud_lv` and
  `magicexpansion:watercloud_xp` PDC keys with INTEGER data types.
- Existing rods without those keys initialize at level 1 with 0 XP.
- Existing item IDs, display names, enchantments and unrelated lore/PDC values
  are preserved. Only the Water Cloud proficiency lore line is updated.
- An accepted ordinary catch grants 1 XP; an accepted special catch grants 5 XP.
- XP thresholds to reach levels 2–8: 300, 600, 1200, 2200, 3200,
  5200, 7200. XP is capped and discarded at level 8.

## Safety and scope

The existing `FishingCompatibility` gate still controls whether
MagicExpansion intercepts any catch. An external fishing plugin in AUTO or
COMPATIBILITY mode, or a cancelled catch, leaves rods, bait, XP and world
drops untouched. XP is granted only after the existing accepted payout path.

This phase deliberately **does not** introduce upstream's alternate 20 Hz
water-hook state machine, virtual NPCs, boosted loot rolls, bait discounts,
new recipes, or changed economy rules. Those need separate behavior,
Folia-region, item-transaction and saved-world tests before activation.

## Verification

- Unit tests cover level thresholds, overflow, item-bound PDC, duplicate lore,
  original metadata preservation and eligible rod IDs.
- Existing fishing coexistence and cancellation regression tests must pass.
- Validate on a backed-up test server before merging into release packaging:
  Paper 1.21.11, Paper 26.2, Paper 26.3, Purpur, Leaf and Folia.
- No release or update of the SF Addons ZIP is authorized by this change.
