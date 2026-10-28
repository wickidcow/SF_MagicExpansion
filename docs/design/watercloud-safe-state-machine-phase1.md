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
