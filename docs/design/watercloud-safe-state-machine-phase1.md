# Water Cloud alternate fishing — phase 1: deterministic rules only

This is **preparatory, non-active code** for [MagicExpansion issue #30](https://github.com/wickidcow/SF_MagicExpansion/issues/30). It does not add an alternate fishing mode, register listeners, place an item, change saved data, rewrite a recipe or alter existing catch payouts.

## Upstream parity retained

The isolated `WaterCloudHookMath` class implements the upstream three bite stages (WAITING, LIGHT_BITE, FULL_BITE), the original 10% waiting / 20% light-bite base transitions, the rod-proficiency bonuses already ported as `WaterCloudRodEffects`, eight-second light-bite expiry, 1% / 40% / charge-dependent reel probabilities, 15-degree aiming window, 1.25/tick base charge, 100-point cap, 30-second full-bite timeout, 20-block maximum hook distance and 600-second session lifetime. Invalid numeric values fail closed; no random generator or static session map is shared across players.

## Non-negotiable next integration gates

1. **OFF by default.** Add an explicit alternate-mode configuration and command only together with a complete, reviewed handler; never enable upstream's default-on mode.
2. **Folia ownership.** Future hook and player mutations belong to the owning entity/region scheduler, not Bukkit's global synchronous repeating timer or a shared `HashMap<UUID, Player>`. Do not read a player or hook from another region.
3. **No XP bar destruction.** If charge UI borrows a player's XP bar, restore both level and percentage on every quit, swap, reel, hook invalidation, disable or exception path. Consider avoiding XP-bar takeover entirely.
4. **No free or duplicate catches.** Keep `FishingCompatibility.AUTO/COMPATIBILITY/FULL` precedence; preserve the current exact `WaterCloudBaitPayment` and `WaterCloudPayouts` transaction and special-catch guard. No reward without owned bait and no catch outside water.
5. **One active session per player.** Key sessions to both player/hook IDs with lifecycle ownership; verify timeout, expired hook, reconnect and full restart, no shared cross-region mutable iteration.
6. **Safety and acceptance.** Add event-level mock and real server Paper/Purpur tests plus Folia multiple-region tests before touching released behavior. The current numeric tests do not certify gameplay.

No new release/ZIP publication is authorized by this phase. Published MagicExpansion 1.1.19, Slimefun Legacy 4.1.78 and all IDs/storage and Bedrock/Geyser assets remain unchanged.


## Phase 2: per-player session identity (prepared, not activated)

The new `WaterCloudHookSessions` registry holds only immutable UUIDs, start times and
cast-generation tokens, never Bukkit players, hook entities or worlds. `start`
replaces an older cast and returns its handle for region-owned cleanup;
`claimForReel` consumes the exact player/hook/generation at most once, rejecting
stale callbacks and sessions at or beyond 600 seconds. `cancel`, `cancelPlayer`,
`expire` and idempotent `shutdown` handle recasts, quit, hook invalidation and
disable without deleting a newer session. Eight JUnit tests cover simultaneous
reels, re-used hook IDs, timeout, separate players and shutdown. Cross-thread
coordination consists of short in-memory identity operations only.

The future event adapter must own all hook, player, XP-bar and inventory changes
on their proper Paper/Folia entity or region scheduler, **outside** these locks.
One successful session claim is not by itself authorization to pay: require an
uncancelled event, fishing compatibility ownership, water, allowed drops and
the existing checked bait transaction. On replacement or shutdown, schedule
any XP restoration on the owner's region; this registry never restores XP itself.
No listener or configuration setting uses this class yet. Normal fishing, item
IDs, stored data and all published releases remain unchanged.
