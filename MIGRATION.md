# Migrating to MagicExpansion Legacy English

This fork keeps the original Bukkit plugin name (`magicexpansion`), Java package, Slimefun item IDs, recipe keys, and persistent-data keys wherever they are used by existing worlds.

## Before replacing the JAR

1. Stop the server completely. Do not use `/reload`.
2. Back up the server, including the world folders and `plugins/magicexpansion`.
3. Keep the existing `plugins/magicexpansion` data folder unless you intentionally want a clean configuration.
4. Replace the old JAR with the newly built English fork.
5. Start the server and inspect the complete startup log before allowing players to join.

## Requirements

- Paper or Purpur 26.2.x
- Java 25 or newer
- Slimefun Legacy, or another compatible Slimefun 4 API provider

GuizhanLibPlugin is no longer required by this fork. Remove it only after confirming that no other installed addon depends on it.

## Compatibility retained

- Original plugin identity and data folder
- Existing Slimefun item IDs and recipe keys
- The historical Chinese internal ID used by `Dreams Must End`
- Legacy `Magic Empowerment` enchantment lore recognition
- Legacy cargo-fragment amount lore recognition
- Existing serialized cargo contents and persistent-data keys

The old Chinese strings retained for migration checks are encoded internally or used only as stable IDs. Players see English names and lore.

## Recommended staging checks

Test these systems before production use:

- Existing placed machines and energy connections
- Quick machines and multiblock recipes
- Cargo fragments, extraction, distribution, and portable transport
- Fishing rods, fish weights, lures, and fish generators
- Magic Altar recipes and recipe book menus
- Boss summoning, attacks, and drops
- Prefabricated buildings and trees
- Shop data and hologram cleanup
- Previously created enchanted or named items

## Optional AI module

AI chat is disabled by default. It remains inactive unless `qwen.enabled` is set to `true` and a valid API key is supplied. Requests use connection/read timeouts and run away from the primary server thread.
