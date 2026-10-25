# Generation 2 fish payout repair

## Observed defect

`FishingRodWaterCloud.gen2Seed(...)` creates recipe/loot-pool *templates*:
material, display name, and the `magicexpansion:gen2_type` string key.
The existing `PlayerFishingWaterCloudListener` cloned that template and
dropped it without calling `FishAttributeGenerator.generate(...)`.
Consequently, an otherwise successful Generation 2 catch could lack the
five elemental values, quality multiplier and genotype needed downstream.

## Change

- After an eligible weighted fishing payout is selected, transform only the
  unprocessed one-item template into a fully attributed Generation 2 fish.
- Keep existing fish item IDs and `magicexpansion:gen2_*` keys with their
  original PDC data types. Static key construction no longer depends on plugin
  startup order, but the serialized namespace/keys are unchanged.
- Preserve the existing `FishingRodWaterCloud` weight-boost tier so superior
  rods still influence the new fish's quality roll.
- Never reroll completed/bred/partially populated fish, unknown types, stacks
  greater than one, or unrelated items.
- Keep all catches gated by the original `FishingCompatibility` listener.
  No new reward path or additional item drops have been introduced.

## Verification

The MockBukkit tests cover completion of an unprocessed template, preservation
of the source template, valid five-element values, quality/trait metadata,
non-reroll behavior, unknown IDs and unexpected stack quantities.

Production validation still requires normal fishing on a backed-up server,
then persistence and machine/breeding tests with real Gen2 catches across
the supported Paper/Purpur/Leaf/Folia versions.

This changes neither releases nor the SF Addons ZIP.
