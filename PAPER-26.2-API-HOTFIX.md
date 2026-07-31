# Paper 26.2 API Hotfix — v1.0.2

This maintenance hotfix updates removed Bukkit/Paper enum aliases used by the original MagicExpansion source.

## Compile fixes

- `PotionEffectType.JUMP` → `JUMP_BOOST`
- `PotionEffectType.DAMAGE_RESISTANCE` → `RESISTANCE`
- `PotionEffectType.CONFUSION` → `NAUSEA`
- `PotionEffectType.SLOW` → `SLOWNESS`
- `PotionEffectType.SLOW_DIGGING` → `MINING_FATIGUE`
- Updated legacy particle names to their modern Paper equivalents.
- Replaced the deprecated villager plant effect with `Particle.HAPPY_VILLAGER`.
- Added required color data to modern `Particle.FLASH` calls.
- Replaced generic uses of typed particles with safe untyped visual equivalents.

No Slimefun item IDs, recipe IDs, persistent-data keys, or plugin identity values were changed.
