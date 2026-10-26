# Temporary slowness modifier ownership and region safety

## Existing bug

`SlownessManager.applySlowness` previously called
`removeAllModifiers(movementSpeed)`, which removed **all** active
movement-speed modifiers before applying Magic Expansion's slow effect.
That can erase permanent equipment bonuses, vanilla effects or modifiers
owned by other addons (including RPG attribute systems).

An older delayed `BukkitRunnable` also used the global scheduler and could
remove a *newer* slowness effect when the earlier timer expired.

## Fix

- Remove only the modifier identified by Magic Expansion's own
  per-entity `NamespacedKey`.
- Never alter other modifiers, even if their type or operation matches.
- Replace an existing Magic-only modifier atomically using a UUID-key map.
- Expiry tasks carry their own modifier key and only clear it if it is still
  the current active value; overlapping effects cannot prematurely cancel
  each other.
- Use Paper's entity scheduler so delayed removal follows a moving
  entity's owning region (instead of a globally scheduled Bukkit task).
- Clean up the temporary key when an entity is retired.
- Preserve the existing magnitude and original three-times duration schedule.
  Any gameplay rebalance needs a separate, explicit change.
- Clamp nonfinite or out-of-range input values rather than inserting NaNs
  or amplifying movement speed.

## Scope

No new Slimefun IDs, lore, prices, power use, fishing mechanics, saved player
data, GuizhanLib dependency, NMS, virtual entity or Bedrock/Geyser feature.
No release is created or included in the SF Addons ZIP.

## Testing

JUnit checks original magnitude, nonfinite values and targeted modifier
removal while preserving equipment/other-plugin modifiers. The normal Java 21
build and supported Paper/Purpur/Leaf/Folia compatibility checks must pass.
Native and Manual tests should include overlapping sword slows, permanent
movement-speed bonuses, entity death/unload and Folia region ownership.
