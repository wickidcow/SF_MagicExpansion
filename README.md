# MagicExpansion Legacy English

**MagicExpansion Legacy English** is a maintained, English-focused fork of MagicExpansion updated for modern Paper servers and compatibility with **Slimefun Legacy**.

This fork preserves the original gameplay systems, items, recipes, machines, cargo features, bosses, fishing content, magic equipment, and saved-world identifiers while improving compatibility, readability, and build stability.

## Features

* Fully translated English item names, lore, menus, messages, commands, and configuration files
* Compatible with Slimefun Legacy and the established Slimefun addon API
* Updated for Paper and Purpur 26.2
* Java 25 build support with Java 21-compatible output
* Preserves existing Slimefun item IDs and persistent-data keys
* Improved cargo fragment and quantum storage compatibility
* Updated modern Paper attributes, enchantments, particles, potion effects, and materials
* Removed unnecessary GuizhanLib, InfinityLib, and Lombok requirements
* Optional AI functionality with safe timeouts and disabled-by-default configuration
* GitHub Actions workflow for automatic builds
* English migration support for older MagicExpansion items
* External fishing-provider compatibility for PyroFishingPro and BetterFishing
* Water Cloud rod proficiency, lure preservation, and MagicExpansion bonus rewards without replacing external-plugin catches

## Included Content

MagicExpansion adds a large collection of magical and technological Slimefun content, including:

* Magic weapons and equipment
* Custom food and consumable effects
* Fishing machines and advanced fishing rewards
* Cargo and quantum storage systems
* Summonable bosses and creatures
* Magic altars and custom recipes
* Generators and automated machines
* Structure-placement tools
* Special materials, fragments, and progression items

## Fishing Integration

External fishing plugins are treated as the primary fishing engine by default. MagicExpansion does not replace or duplicate their caught fish. Instead, Water Cloud rods add their own progression and safe bonus layer after a successful external catch.

Supported provider modes:

* **PyroFishingPro** - detected automatically. MagicExpansion uses PyroFishingPro's catch event reflectively when available, so Pyro remains an optional paid dependency and is never bundled or modified.
* **BetterFishing** - detected automatically and kept authoritative for its fishing flow. MagicExpansion uses the provider-neutral Bukkit catch fallback unless a stable public provider API is available.
* **Vanilla** - used automatically when no supported external fishing provider is active.

Default configuration:

```yaml
fishing-integration:
  external-plugins-primary: true
  provider: AUTO
  priority:
    - PyroFishingPro
    - BetterFishing
    - VANILLA

fishing-system:
  new-system: false
  only-when-no-external-provider: true
```

The optional Water Cloud custom fishing minigame is disabled by default. Even when enabled, it is suppressed while an external fishing provider is primary unless the server owner explicitly changes the compatibility settings.

## Compatibility

| Software                          | Support                     |
| --------------------------------- | --------------------------- |
| Paper 26.2                        | ✅ Supported                 |
| Purpur 26.2                       | ✅ Supported                 |
| Slimefun Legacy                   | ✅ Recommended               |
| SlimefunGugu API-compatible forks | ✅ Expected to work          |
| PyroFishingPro                    | ✅ Optional integration      |
| BetterFishing                     | ✅ Optional integration      |
| Spigot/CraftBukkit                | ❌ Not supported             |
| Folia                             | ⚠️ Not currently guaranteed |

## Existing Worlds

This fork intentionally preserves the original plugin identity, Slimefun item IDs, recipe identifiers, cargo data, and persistent-data keys wherever possible.

Existing MagicExpansion worlds and items should remain recognized after upgrading. Back up the server before replacing any plugin build.

One historical internal Chinese item identifier remains preserved for compatibility with previously created items. Its visible name and lore are displayed in English.

## Requirements

* Paper or Purpur 26.2
* Java 25 runtime
* Slimefun Legacy or another compatible Slimefun API implementation

## Disclaimer

MagicExpansion Legacy English is an independent community-maintained fork. It is not an official release from the original MagicExpansion or Slimefun developers.

The goal of this project is to preserve MagicExpansion, improve English accessibility, maintain compatibility with modern Minecraft servers, and provide a stable version for Slimefun Legacy users.
