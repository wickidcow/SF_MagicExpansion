# Water Cloud classic bait preservation and double catches

Source parity with the classic progression path in upstream Build 94.
Both bonuses are optional and **OFF by default** on new and existing
installations.

## Classic bait preservation

`Fish.WaterCloud.classic-lure-preservation-enabled: false`

Advanced rods (Reedflower, Snowy River and Fine Rain) unlock up to 40%
preservation chance at level 8. When a roll succeeds, the original
selected lure is **confirmed still present** in a matching bait bag or
the player's inventory/offhand. Only then is the custom catch awarded
without a debit. If the lure vanished, the event is left as vanilla
fishing, with no Magic reward or proficiency XP.

Unsuccessful rolls still debit one lure through the existing
`WaterCloudBaitPayment` BAG → INVENTORY fallback and reject empty bait.

## Classic double catch

`Fish.WaterCloud.classic-double-catch-enabled: false`

Level 3+ advanced rods unlock the existing upstream 4%-per-level
chance, up to 24% at level 8. A double catch pays **two independently
cloned stacks**, not `setAmount(min(64, old * 2))`, so 40 becomes
two stacks of 40 rather than silently losing 16. Fish buckets remain
two distinct unstackable items. Proficiency is earned once per
original fishing event, not twice.

## Safety requirements

- Only run when MagicExpansion's existing AUTO/COMPATIBILITY/FULL
  ownership gate approves the catch.
- Do not introduce Geyser/Bedrock, Citizens, NMS, or GuizhanLib code.
- No change to item IDs, PDC keys, recipes, loot pool weights or saved
  player data. The existing paid-catch gate remains authoritative.
- Roll and prepare payouts before charging or deleting the vanilla catch.
- Manual smoke testing must include last bait, bag removal, offhand bait,
  advanced/beginner rods, external fishing plugins, cancelled events,
  stacked material and unstackable Generation 2 fish buckets.
- Real-server cross-restart conservation remains a release gate.
- No Magic Expansion or Slimefun Legacy release and no addon ZIP change
  is included in this review.
