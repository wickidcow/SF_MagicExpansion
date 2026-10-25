# Water Cloud rod bonuses — upstream parity (phase 2)

Original source: Yomicer/MagicExpansion `WaterCloudRodEffects.java` at
`8c79515e07da578c4e439f2516f3a7c595184d2` (Build 94).
Target: Slimefun Legacy, Paper/Purpur/Leaf/Folia, Minecraft 1.21.11+.

## Ported numerical definitions

These values are read-only. **They are not yet applied to live fishing.**

| Bonus | Upstream schedule | Maximum (level 8) |
| --- | --- | --- |
| Classic hook wait cap reduction | 60 ticks per two levels | 240 ticks |
| Classic junk-weight reduction | 3% per level starting at level 3 | 18% |
| Classic bait preservation | 8% per level starting at level 4 | 40% |
| Classic double catch | 4% per level starting at level 3 | 24% |
| Alternate waiting-to-light-bite boost | 1.5% per level starting at level 4 | 7.5% |
| Alternate light-to-full-bite boost | 2% per level starting at level 5 | 8% |
| Alternate charge speed boost | 0.5 per second per level starting at level 3 | 3.0/s |
| Alternate double catch | 3% per level starting at level 3 | 18% |
| Alternate full-charge special-catch bonus | Level 8 only | 5% |

The helper caches immutable bonus rows instead of allocating a list
on every fish-hook tick. Saved or corrupted level values are clamped to
the established 1–8 range, preventing arbitrarily large bonuses.

## Before bonuses can affect gameplay

- Merge/reconcile the item-bound proficiency and 20-lure changes in PR #31.
- Merge/reconcile Gen2 fish materialization in PR #32.
- Preserve the existing `Fish.Compatibility` AUTO/COMPATIBILITY/FULL gate.
  If a third-party fishing plugin owns a catch, no XP, bait debit, hook-rate
  changes or custom drops should occur.
- Make bait-preservation/double-catch calculations **after** a successful
  match, using a bounded reward transaction. A stack of 40 must never be
  silently capped or lose paid items when doubled.
- Keep old mode's hook timing, junk-weight treatment and all alternate-state
  features separate. The experimental state machine is OFF by default.
- Validate fishing-rod identity, stored XP, bait-bag priorities, payouts and
  restart persistence on real servers before enabling any gameplay modifiers.
- Do not add GuizhanLib hard dependencies, Citizens/NMS fake players, or
  Geyser/Bedrock integrations.

No version bump, release publication or SF Addons bundle change is included.
