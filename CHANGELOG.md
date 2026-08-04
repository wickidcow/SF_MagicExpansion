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
