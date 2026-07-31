# MagicExpansion Legacy English

An English-first maintenance fork of **MagicExpansion**, updated for modern Paper servers and designed to run with **Slimefun Legacy**.

## Requirements

- Paper or Purpur 26.2.x
- Java 25 or newer
- Slimefun Legacy (recommended) or a compatible Slimefun 4 API provider

GuizhanLibPlugin is no longer required by this fork. The small item/entity-name helpers used by the original project are implemented locally, and the automatic Guizhan updater has been removed.

## Main features

- Portable and placeable quick-recipe machines
- Resource generators and electric processing machines
- Magic empowerment and combat items
- Fishing progression, fish generators, and lure systems
- Prefabricated trees and structures
- Cargo and storage utilities
- Magic bosses, SkyBlock tools, shops, and novelty items

## English conversion

Player-facing item names, lore, menus, messages, command descriptions, configuration notes, and startup logs are maintained in English. Existing Slimefun item IDs remain unchanged so items and recipes keep their stable identifiers.

## Slimefun Legacy compatibility

This fork uses the modern `SlimefunBlockData` ticker overload already provided by Slimefun Legacy while retaining established Slimefun addon entry points. It builds with a Java 25 toolchain against the Paper 26.2 API while emitting Java 21 bytecode, matching Slimefun Legacy's current build strategy.

## Building

```bash
mvn -DskipTests clean package
```

The shaded JAR is written to `target/`.

## Important

Back up your server before replacing an existing build. Test machines, cargo links, custom items, fishing, bosses, and prefabricated buildings on a staging server before production use. Do not use `/reload`.

## Credits and license

Original project by Yomicer. This is an unofficial English maintenance fork. The original license remains in effect; retain attribution and provide source code when distribution requires it.
