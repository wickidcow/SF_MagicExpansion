# MagicExpansion feature verification

This is the working verification ledger for the maintained fork. The goal is to move from "the plugin loads" to evidence for each item family without changing historical IDs, recipes, costs, storage keys, machine rates or saved data merely to make a test pass.

## Phase 1 — complete item registration/catalog integrity

The real-server `ItemCatalogProbe` reflects every declared `MagicExpansionItems` `SlimefunItemStack`, then compares those templates with the live Slimefun registry. For every registered MagicExpansion item it checks:

- exact ID lookup and ItemStack identity lookup;
- non-AIR template and recipe output;
- registered item group and non-null recipe type;
- normalized nine-slot recipe with valid ingredient amounts;
- ticker metadata;
- energy component type and non-negative long capacity;
- every declared item template is actually registered.

The probe writes a complete TSV catalog so later phases can group verification by implementation class instead of relying on a hand-maintained partial list.

It runs after two clean boots on Paper 1.21.11, Paper 26.2, Paper 26.3 and Purpur 26.3 against the published Slimefun Legacy 4.1.74 core.

Passing this phase proves registration and static/runtime metadata integrity. It does **not** by itself prove every click, machine transfer, combat effect, GUI action or world mutation.

## Existing behavioral evidence before this audit

Already covered by permanent tests or real-server probes:

- CargoStorage exact item/PDC identity, large counts and full-capacity behavior.
- Portable Shop persistence/editor safety and stale/external write refusal.
- Black Market item serialization, receipt/session/restart safeguards and player-flow purchase behavior.
- Both fishing rod families, compatibility mode, cancellation, lure and XP preservation.
- Star Shards Sword cancellation/re-entry guard and Astral Shield projectile reflection.
- Slimefun Tick Monitor automatic sample coordination and stale/missing result handling.
- English language-resource audit.

## Next behavior families

After the catalog is green, add targeted real-server probes in this order:

1. quick/recipe/resource machines: input consumption, exact output, energy use and no duplication;
2. cargo/fragment/portable transport machines beyond the already-tested storage codec;
3. power generators and special energy blocks;
4. magic altar, enchanting and recipe-book interactions;
5. tools and right-click utility items;
6. foods and consumable effects;
7. boss summons, drops and combat effects;
8. fishing breeding/energy machines and bait bag;
9. lottery/random/Honkai/Schrodinger/PageChest novelty and container features;
10. skyblock/prebuilt-world mutation features.

Each family must distinguish automated server evidence from behavior that still needs a live-client/manual check.
