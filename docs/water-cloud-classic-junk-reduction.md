# Water Cloud classic junk catch reduction (opt-in)

Restores the upstream rod-proficiency reward that reduces the *weights*
of junk entries in an eligible Water Cloud lure pool.

## Behavior

`Fish.WaterCloud.classic-junk-reduction-enabled: false` keeps the feature OFF
by default, including on existing servers that do not have this key.

When enabled, levels 3–8 of Reedflower, Snowy River and Fine Rain rods lower
six upstream-designated vanilla junk weights by 3% per level from level 3,
capped at an 18% reduction at level 8.

The original math rounds `originalWeight * (1 - reduction)` to the nearest
integer with a minimum positive junk weight of 1.

The six original junk materials are cobblestone, flint, gravel, sand, string
and bone. Material resemblance is not sufficient for registered Slimefun
items: these retain their original weights, as do Generation 2 fish seeds,
special-catch items, ordinary rewards and higher-grade fish.

The original loot table is never mutated. A temporary adjusted view of the
selected lure's pool is used for the next weighted draw, using the existing
long-weight, empty-pool-safe selector.

## Protections

- No additional items or rewards are spawned by the bonus.
- No item IDs, PDC keys, machine recipes or per-player purchase records change.
- Only catches already allowed by the MagicExpansion fishing coexistence gate
  can receive the effect; when another plugin owns fishing, all effects pause.
- Starter Cyan Bamboo rods are excluded.
- The six junk entries are the *only* weighting targets; no heuristic such as
  'all vanilla items are junk' is used.
- No GuizhanLib, NMS, Citizens, Geyser/Bedrock work.
- No new release or change to the SF Addons ZIP.

## Validation

Tests verify the exact 3%–18% upstream schedule, integer rounding, weight=1
minimum, source item categories and preservation of registered Slimefun
item weights. Validate with live catch distribution samples on a
backed-up server before enabling as a gameplay setting.
