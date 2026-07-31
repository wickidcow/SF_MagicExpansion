# Paper 26.2 Compilation Fix — v1.0.3

This maintenance update resolves the 41 Java compilation errors reported after v1.0.2.

## Paper API renames

- `Enchantment.LUCK` → `Enchantment.LUCK_OF_THE_SEA`
- `Enchantment.DAMAGE_ALL` → `Enchantment.SHARPNESS`
- `Enchantment.LOOT_BONUS_MOBS` → `Enchantment.LOOTING`
- `Attribute.GENERIC_MOVEMENT_SPEED` → `Attribute.MOVEMENT_SPEED`
- `Attribute.GENERIC_MAX_HEALTH` → `Attribute.MAX_HEALTH`
- `Attribute.GENERIC_ATTACK_DAMAGE` → `Attribute.ATTACK_DAMAGE`
- `Attribute.GENERIC_ATTACK_SPEED` → `Attribute.ATTACK_SPEED`
- `Attribute.GENERIC_ARMOR` → `Attribute.ARMOR`
- `Attribute.GENERIC_ARMOR_TOUGHNESS` → `Attribute.ARMOR_TOUGHNESS`
- `Attribute.GENERIC_FLYING_SPEED` → `Attribute.FLYING_SPEED`
- `Material.CHAIN` → `Material.IRON_CHAIN`

## Quantum storage compatibility

`QuantumCache` now declares explicit `getAmount()`, `isVoidExcess()`, `setVoidExcess(...)`, and `setLimit(...)` methods. The values remain `long` internally and the persistent-data layout is unchanged.

Lombok was removed from the three utility/cache classes and from Maven. This avoids generated-method differences under Java 25 while preserving the same public behavior.

## Migration safety

This update does not change:

- Bukkit plugin name or main class
- Slimefun item IDs
- recipes
- persistent-data keys
- cargo fragment serialization
- quantum storage amount or capacity data types

## English completion pass

The final validation found 256 additional Chinese Java string literals left from the original source. They have now been translated, including player-facing cargo, quick-machine, boss, permission, and structure-placement messages. The only remaining Chinese text is the historical final-fishing-rod Slimefun ID and its matching `language.yml` key, retained so existing saved items continue to resolve. The displayed item name and lore are English.
