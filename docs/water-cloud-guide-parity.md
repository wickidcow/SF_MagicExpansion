# Water Cloud upstream guide item parity

This ports the two missing upstream guide item IDs without changing the
working Magic Expansion fishing recipes or mechanics:

- `MAGIC_EXPANSION_FISHING_GUIDE_BETWEEN_WATER_CLOUD` (paper reference)
- `MAGIC_EXPANSION_FISHING_BOOK_BETWEEN_WATER_CLOUD` (knowledge-book manual)

Upstream introduced both IDs, but its book currently only displays an
unfinished-message placeholder. The Legacy implementation provides useful
English rod/lure instructions on right-click, using ordinary player messages
and a sound instead of a recurring task or a menu that might collide with JEG.

Both are added to the established Magic Expansion **Fishing** subgroup and
have `RecipeType.NULL`, like upstream. They are informational; they do
not grant fishing loot, auto-craft ingredients, or alter rod requirements.
Server operators can inspect them through the Slimefun guide, where their
IDs are also available for existing item migration.

### Saved world and recipe policy

Do not rename any existing rods, lures, fish, or items. Upstream
`BAMBOO_JOINT` and `MAGIC_THREAD` remain deferred: the maintained
Legacy recipes currently use historical sticks/string and must not be
silently rewritten. Adding the guides does not change any recipes.

### Validation and activation

- Native item-catalog probe requires both IDs to register exactly once in
  the Fishing subgroup, with the clickable book handler available.
- Existing guide category and item-catalog tests must remain green.
- Test manually with the maintained JEG guide and verify right-click does
  not place blocks or interfere with regular held items.
- Intended to merge alongside the current Water Cloud catch/progression
  feature set (PRs #31–#33); do not publish alone or update the addons ZIP.
- No GuizhanLib, InfinityLib, Citizens/NMS, or Geyser/Bedrock changes.
