# Water Cloud fishing — weighted loot hardening

The original Legacy Water Cloud loot picker used an `int` total and
`new Random().nextInt(total)`. An empty pool, zero total or overflow to a
non-positive total could throw an exception during an otherwise valid cast,
leaving the reward transaction in an uncertain state.

This fix uses `WaterCloudWeightedLoot.selectRandom` and safely returns
`null` for empty or invalid pools before bait has been debited or the
vanilla catch removed.

- Positive weights retain their exact proportional probabilities.
- `long` cumulative totals prevent an int overflow with large pools.
- Unusable entries (null, air, nonpositive weights) cannot yield a payout.
- Selected stacks are cloned before attaching Generation 2 attributes.
- A deterministic ticket-based API verifies all original pool boundaries.
- No item IDs, PDC keys, loot weights or reward amounts are changed.

The existing fishing-compatibility gate and paid-bait transaction remain the
only ways to receive a Magic Expansion catch. If the loot pool is unusable,
the original vanilla catch is left untouched.

Tests cover empty pools, zero/negative weights, large weights, exact
selection boundaries and source-template isolation.

No release, version bump or SF Addons ZIP update. No GuizhanLib, NMS,
Citizens or Geyser/Bedrock features.
