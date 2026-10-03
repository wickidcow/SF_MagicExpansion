# 1.1.8 - Upstream Build 93 / Release 13 integration (2026-10-03)

- Ported the October 1 upstream update (`39471f40826fcc815b64c3b01ade20ddcb9360ad`) into the English maintained fork.
- Magic Storage - Rebuilt now refuses new item types when full. Input and extraction paths consume only successfully stored quantities; unavailable space does not cause asynchronous world drops or discard excess items.
- Preserved large cargo counts, existing typed item metadata, storage keys, reserved slots, and unreadable records. Partial cargo-fragment imports leave their remainder intact; stacked fragments wait when only a partial fragment would fit.
- Added Star Shards Sword projectile reflection, Blazing Slash flame/explosion effects and configurable bonus damage, animated Arcane Blast with configurable proportional damage, and Shadow Blink shockwave/slow/knockback effects.
- Added defaults of `0.8` for `StarShardsSword.StarShards_Atk_Fire` and `0.6` for `StarShardsSword.StarShards_ArcaneBlast_Mult`; existing config files receive the same fallback behavior without being rewritten.
- Prevented nested sword-generated damage from triggering the sword again. Cancelled attacks and Slimefun protection checks are respected; timed shields no longer set a persistent vanilla invulnerability flag. Expired bleed task handles and player session state are cleaned up.
- Retained modern Paper APIs, English messages, item/research IDs, namespaced attribute keys, recipes, storage schema, machine rates, and all previously merged shop preservation fixes.
- Updated the guide's upstream-content information and corrected the sword's Mercy description to describe its nonlethal bleeding effect.

- Replaced placeholder English lore with meaningful item descriptions, controls, warnings, machine functions, and historical notes. Preserved all 475 language entries and the historical final-rod key.
- Polished displayed names and aligned food, hologram-eraser, and sword wording with their implementation.
- Enabled detailed deprecation reporting. Replaced deprecated maximum-health and PvP access and removed redundant transient metadata tracking in the sword; legacy compatibility APIs elsewhere remain documented.

# 1.1.4 - Slimefun Legacy Re-entry / Guide Order Fix (2026-09-13)

- Re-verified the maintained fork for the Minecraft/Paper 1.21.11 baseline and Paper/Purpur 26.2 using the Java 25 CI toolchain.
- Kept the existing Java 21 bytecode target for broad modern-server compatibility.
- Moved the top-level Magic guide category from tier `0` to Slimefun's normal addon tier `3`, so MagicExpansion no longer forces itself to the first guide position.
- Preserved the Magic category key, icon, subgroups, Slimefun item IDs, recipes, persistent-data keys, and existing saved-world compatibility.
- Prepared MagicExpansion to return to the maintained Slimefun Legacy addon bundle.

# 1.1.2 - Upstream Release 10 / Build 90 Port (2026-09-01)

- Ported upstream MagicExpansion commit `6d78c65eb8b60e785daa8a27dd606dc7993375a1` while preserving the Legacy fork's English presentation and compatibility work.
- Added the Generation 2 fish attribute and breeding systems, including the Fish Breeding Pool.
- Added the Hanjiang and Xiyu Water Cloud fishing progression, their new lures, and progression catches.
- Added the five-page Page Chest with Slimefun cargo-compatible storage behavior.
- Added acceleration-fish machine buff registration and retained the upstream five-minute in-memory buff model.
- Added Release 10 storage/output fixes for large quantities, quantum storage, Cargo Core handling, leftovers, and duplicate energy deductions.
- Added tighter Magic guide/menu event scoping and the special-catch clone fix.
- Kept GuizhanLibPlugin optional by routing the new display-name use through the fork's bundled compatibility helper.
- Retained Java 21 bytecode and the 1.21.11 through Paper/Purpur 26.2 compatibility target.

# 1.1.0 - Upstream Build 84 Port (2026-08-04)

- Ported the important changes from Yomicer/MagicExpansion upstream commit `b56aad4`.
- Added the Between Water and Clouds fishing series, Cyan Bamboo Rod, five lures, and special catches.
- Added optional Networks/NetworksExpansion quantum-storage support through reflection and PDC compatibility keys.
- Kept the plugin fully independent of GuizhanLib by using the local compatibility helper.
- Added vivarium output capacity limits, numerical overflow protection, and high-output lag safeguards.
- Added quantum-storage support to the Etheric Vivarium Array.
- Fixed stale Draw Machine hologram cleanup and upstream output/energy handling issues.
- Preserved English presentation, the `Magic` guide category, existing item IDs, Java 21 bytecode, and Paper/Purpur 26.2 support.

# Changelog

## 1.0.3 - Paper 26.2 compilation and cargo accessor fix

- Updated removed Paper 26.2 attribute constants from `GENERIC_*` names to their modern registry field names.
- Updated enchantment aliases: `LUCK` to `LUCK_OF_THE_SEA`, `DAMAGE_ALL` to `SHARPNESS`, and `LOOT_BONUS_MOBS` to `LOOTING`.
- Updated the renamed chain material from `CHAIN` to `IRON_CHAIN` in the three fishing-machine interfaces.
- Replaced Lombok-generated `QuantumCache` accessors with explicit long-safe methods, fixing cargo fragment and quantum storage compilation.
- Removed the remaining Lombok usage and dependency from the project to avoid Java 25 annotation-processing differences.
- Translated 256 remaining Chinese Java string literals, including cargo messages, quick-machine instructions, boss status text, debug logs, and commented examples.
- Retained only the historical final-fishing-rod item ID and matching language key for saved-item compatibility; its visible name and lore remain English.
- Preserved all plugin identity values, Slimefun item IDs, recipes, namespaced data keys, and serialized quantum-storage formats.

## 1.0.2 - Paper particle and potion API hotfix

- Updated removed potion-effect and particle enum aliases for Paper 26.2.
- Replaced the deprecated crop-growth effect with a modern particle implementation.
- Preserved item behavior and existing data identifiers.

## 1.0.1 - Build dependency hotfix

- Replaced the nonexistent `com.github.SlimefunGuguProject:Slimefun4:2026.1` dependency with the published `2025.1.2` API baseline.
- Kept Slimefun Legacy as the intended runtime core; the fork preserves the established Gugu/Slimefun addon API.
- No item IDs, persistent-data keys, recipes, or player-facing behavior changed in this hotfix.

## 1.0.0 — Legacy English maintenance release

### English conversion

- Converted player-facing item names, lore, menus, chat messages, command help, logs, and configuration notes to English.
- Rebuilt `language.yml` under the `en_US` namespace.
- Corrected fused or literal translations in cargo, fishing, machine, boss, shop, and tool interfaces.
- Preserved the one historical Chinese Slimefun item ID required for existing saved items while displaying an English name and lore.

### Slimefun Legacy and Paper maintenance

- Updated the build to use a Java 25 toolchain and the Paper 26.2 API while emitting Java 21 bytecode.
- Updated the provided Slimefun API dependency to the published Gugu 2025.1.2 addon baseline for Slimefun Legacy runtime compatibility.
- Retained the modern `SlimefunBlockData` block-ticker overloads already used by the project.
- Preserved original Bukkit plugin identity, Slimefun IDs, namespaced keys, and persistent-data keys.
- Added Maven build enforcement and a GitHub Actions build workflow.

### Dependency cleanup

- Removed the hard GuizhanLibPlugin and InfinityLib build/runtime requirements.
- Added local item/entity display-name compatibility helpers.
- Added the explicit `javax.annotation-api` dependency required by existing source annotations.
- Kept MorePersistentDataTypes shaded and relocated inside the plugin JAR.

### Stability and migration safeguards

- Added compatibility checks for English and legacy Chinese Magic Empowerment lore.
- Added compatibility checks for English and legacy Chinese cargo amount lore.
- Kept invalid cargo chat input pending so players can correct it instead of restarting setup.
- Moved item-name, power-card, and power-eel inventory operations to the server thread.
- Reworked the optional AI manager so it is disabled by default, validates configuration, uses network timeouts, and does not block the server thread.
- Added shutdown cleanup for AI, portable cargo, cargo distributor tasks, shops, altar tasks, and holograms.