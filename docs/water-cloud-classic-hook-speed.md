# Water Cloud classic hook speed — opt-in port

This is an incremental gameplay port of the upstream Water Cloud progression
already represented in `WaterCloudRodEffects`.

## Default and compatibility

- **OFF by default** under `Fish.WaterCloud.classic-hook-speed-enabled: false`.
- Set it to `true` and use the plugin's normal configuration reload process
  to enable for future casts. Existing deployed configuration files with no
  setting also remain disabled.
- The same `FishingCompatibility` router checks casts as it does catches.
  In `AUTO`, any recognized active fishing plugin receives sole ownership.
  `COMPATIBILITY` always leaves hooks untouched. `FULL` is an explicit
  override that still needs careful operator testing with other plugins.
- Casts cancelled by protection plugins or other listeners are untouched.
- Only the three eligible advanced rods gain this timing bonus; Cyan Bamboo,
  vanilla fishing rods, non-fishing events and offhand casts are unchanged.
- There is no scheduler, NMS reflection, virtual player or global task. A hook
  is adjusted directly from the owning `PlayerFishEvent` callback.

## Numeric progression

The original max-wait cap is 600 ticks (30 s). The old-system upstream hook
bonus subtracts 60 ticks per two rod levels. This gives 540 ticks at level 2,
480 at level 4, 420 at level 6 and 360 at level 8 (18 s).

Never lengthen an already shorter server/plugin-configured hook wait. The new
`WaterCloudClassicHookSpeed.cappedWaitTicks` helper calculates
`min(existingCap, max(100, 600 - bonusTicks))`; invalid non-positive caps
are left untouched.

## Non-changes and follow-up

This does **not** enable double catches, discounted bait, junk reduction, the
alternate charge/aim minigame or the virtual interaction bot. The original
formulas are preserved separately, and these other features need safe payout
and Folia/event-lifecycle tests before activation.

No item, PDC, guide, recipe or Black Market changes. No version bump, release
or canonical SF Addons ZIP update.

## Test coverage

- Exact level 1–8 numeric wait-caps and bounds tests.
- Routing tests for enabled/disabled fishing plugin coexistence, explicit
  COMPATIBILITY/FULL, cast cancellation and separation from catches.
- Build test matrix on Paper 1.21.11, Paper 26.2, Paper 26.3, Purpur 26.3.
- Native 45-addon catalog, Black Market and machine safety checks unchanged.
- Manual smoke test: opt-in + established advanced rod, cast in water, confirm
  lower maximum waiting bound; confirm AUTO with recognized fishing plugin does
  not change hook timing.
