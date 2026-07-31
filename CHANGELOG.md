# Changelog

## 1.0.0 — Legacy English maintenance release

### English conversion

- Converted player-facing item names, lore, menus, chat messages, command help, logs, and configuration notes to English.
- Rebuilt `language.yml` under the `en_US` namespace.
- Corrected fused or literal translations in cargo, fishing, machine, boss, shop, and tool interfaces.
- Preserved the one historical Chinese Slimefun item ID required for existing saved items while displaying an English name and lore.

### Slimefun Legacy and Paper maintenance

- Updated the build to use a Java 25 toolchain and the Paper 26.2 API while emitting Java 21 bytecode.
- Updated the provided Slimefun API dependency to the 2026.1 Gugu/Legacy-compatible baseline.
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
