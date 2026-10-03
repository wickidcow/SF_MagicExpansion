<div align="center">

# ✨🪄 MagicExpansion — Slimefun Legacy

**Magic weapons, fishing, bosses, machines, Cargo, storage, altars, and a huge expansion of Slimefun progression.**

![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-6bd425?style=for-the-badge)
![Paper 26.2](https://img.shields.io/badge/Paper-26.2-blue?style=for-the-badge)
![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge)
![Maintained for AlbionMC.com](https://img.shields.io/badge/Maintained%20for-albionmc.com-7b68ee?style=for-the-badge)

</div>

> [!IMPORTANT]
> MagicExpansion Legacy is an **unofficial English-first maintenance fork** for Slimefun Legacy. It is developed and maintained for use on **albionmc.com** while preserving the original MagicExpansion project, IDs, gameplay systems, and upstream credit.

## 🔮 What does MagicExpansion do?

MagicExpansion is a large Slimefun addon that blends **magic, technology, fishing, combat, automation, storage, and progression**.

Major content areas include:

- magical weapons, armor, and equipment;
- custom foods and consumable effects;
- fishing rods, fishing rewards, lures, and fishing-related machines;
- Cargo fragments and quantum-style storage systems;
- summonable bosses and creatures;
- Magic Altars and custom recipes;
- generators and automated machines;
- structure-placement tools;
- special materials, fragments, and progression items.

## 🧪 Slimefun Legacy maintenance

This fork focuses on an English-first experience and modern Paper compatibility while keeping saved-world identifiers intact wherever practical.

Maintenance work includes:

- English item names, lore, menus, messages, commands, and configuration;
- Slimefun Legacy as the primary runtime target;
- modern Paper/Purpur API compatibility;
- Java 25 build support with compatible addon bytecode targets;
- preservation of existing Slimefun item IDs and persistent-data keys;
- Cargo fragment and quantum-storage compatibility repairs;
- modern Paper attributes, enchantments, particles, potion effects, and materials;
- removal of unnecessary GuizhanLib, InfinityLib, and Lombok requirements where the fork has replaced them;
- optional AI functionality with safety limits;
- English migration support for older MagicExpansion items.

One historical internal non-English identifier may remain where changing it would break existing saved items; visible names/lore can still be presented in English.

## 🎣 Fishing alongside other plugins

`Fish.Compatibility.mode` defaults to `AUTO`, including on existing configurations that do not yet contain this setting:

| Mode | Catch behavior |
| --- | --- |
| `AUTO` | Full MagicExpansion fishing unless a recognized fishing plugin is enabled; then MagicExpansion leaves fishing events untouched. |
| `COMPATIBILITY` | Always leave fishing events untouched. Use this for an unlisted fishing plugin. |
| `FULL` | Explicitly enable MagicExpansion catch effects even with another fishing plugin present. Overlapping catch handlers can conflict. |

Built-in detection recognizes the plugin names `PyroFishing`, `PyroFishingPro`, `BetterFish`, `BetterFishing`, `EvenMoreFish`, `CustomFishing`, and `UltimateFishing`, case-insensitively. Add other exact names from `/plugins` to `Fish.Compatibility.additional-plugins`. These entries extend the built-in list. Detection follows plugin enable/disable events, so load order does not require any fishing plugin as a dependency. Unrelated plugins and disabled fishing plugins do not turn fishing off. An invalid mode falls back to `COMPATIBILITY` and logs a warning.

While compatibility mode is active, both MagicExpansion rod families, including Water Cloud rods, leave catches, XP and cancellation unchanged. They do not consume MagicExpansion bait, replace or add loot, generate fish attributes, or trigger catch messages, TNT or celebration effects. Existing rods keep their item data and enchantments; existing fish, IDs, recipes, guides and fishing machines remain available. This is coexistence, not a conversion or API bridge: the other plugin decides whether it accepts a particular rod, and its fish are not converted into MagicExpansion fish or vice versa. MagicExpansion-exclusive catches become available again when its catch effects are enabled.

Use `/magicexpansion fishing` to see the active mode and detected plugins. After editing `config.yml`, use `/magicexpansion reload fishing` to apply the compatibility settings without resetting fishing items or data. These are operator commands. Cancelled fishing events and missing/removed catches are also ignored when MagicExpansion fishing is enabled.

## ❤️ Credits & project lineage

- **Yomicer** — creator and primary upstream developer of **MagicExpansion**.
- **Yomicer/MagicExpansion** — original and immediate upstream repository from which this fork was created.
- **MagicExpansion contributors and community testers** — fixes, content, testing, and continued development.
- **Slimefun developers and addon contributors** — for the APIs and ecosystem MagicExpansion extends.
- **wickidcow / Slimefun Legacy** — current English-first compatibility and preservation maintenance for modern servers and albionmc.com.

The original MagicExpansion design and code lineage remain credited to Yomicer and upstream contributors. This fork is a compatibility continuation, not a claim of original authorship.

## 📜 GNU General Public License v3.0

MagicExpansion is licensed under the **GNU General Public License v3.0 (GPLv3)**. See `LICENSE` for the complete terms.

If you distribute MagicExpansion or a modified GPL-covered version, comply with GPLv3, including preserving applicable notices, identifying modified versions, licensing covered modified source under GPLv3, and making the required Corresponding Source available when distributing object code.

The software is provided **without warranty** as described by GPLv3.

## ⚖️ Independence & trademark notice

**NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.**

MagicExpansion, Slimefun Legacy, and this maintenance fork are independent community projects. They are not sponsored, endorsed, approved, or operated by Mojang Studios or Microsoft. Minecraft-related names, brands, and assets remain the property of their respective rights holders.

This fork is not represented as an official release of Yomicer, the original Slimefun developers, or any other upstream project unless explicitly stated by those parties.

---

<div align="center">

**✨ Keep the magic. Keep the worlds. Keep expanding. 🪄**

</div>
