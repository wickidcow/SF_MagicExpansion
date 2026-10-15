# Magic Market: Slimefun catalog and NPC access

## Status and scope

Feature candidate, not a released plugin. Built on the portable-shop receipt-safety
candidate from PR #21. That prerequisite and coordinated core/addon validation must
be completed before release. The release version is intentionally unchanged here;
do not distribute a candidate as the published 1.1.14 JAR. There is no Albion deployment.

The bundled catalog contains **207 distinct reward offers in 19 named shops**:
151 core-Slimefun offers and 56 optional InfinityExpansion2 offers. Every reward is
resolved by its exact registered Slimefun ID and copied with its real metadata.
Missing or disabled rewards or costs refuse that entire new offer. An absent optional
addon does not create an empty shop or a vanilla substitute. Installed catalog offers
are also checked before display/purchase; removing an addon does not erase receipts.

Existing Garran, Artificer and Xur shop files are not replaced. This expands the
market with additional named shops. It does not replace the old four-shop pack or
change the separate randomized Black Market pool, cycle, prices or player ledgers.

## Sunflower and commands

The old sunflower only reloaded existing shop files. It never generated inventory.
The new **Update / Populate Shops** sunflower in `/magicshop admin` has two actions:

- **Left-click:** add missing offers from `plugins/magicexpansion/market_catalog.yml`.
- **Right-click:** reload saved native shop files only, with no generation or reset.

`/magicshop generate` invokes the same append-only installer and requires
`magicexpansion.shop.admin`. On its first use the bundled catalog is copied only if
no catalog file exists. Existing catalog files are never replaced automatically.
The result reports added, already installed, missing-addon, unresolved-item and
refused-save counts. No left-click means no automatic new economy is enabled.

The skull remains the independent, deliberate randomized-market refresh control.
The rotating market still allows one purchase per slot **per cycle**. The lifetime
limits below belong to the new **fixed catalog offers**, not that rotating market.

## Purchase allowances

All new offers have no shared server cap (`globalLimit: 0`). Other players do not
spend your allowance. Counts are per player UUID and per offer, not account identity.
All non-infrastructure offers allow exactly one purchase. The six exceptions are:

| Item | Items per transaction | Transactions per player | Maximum items |
|---|---:|---:|---:|
| Cargo Connector Node | 16 | 16 | 256 |
| Cargo Input Node | 8 | 16 | 128 |
| Cargo Output Node | 8 | 16 | 128 |
| Advanced Cargo Output Node | 4 | 16 | 64 |
| Cargo Manager | 1 | 4 | 4 |
| Energy Connector | 8 | 16 | 128 |

The catalog contains no duplicate reward IDs. Existing independent manually authored
shops remain independent; the installer does not impose a global per-item quota.
Purchases survive ordinary restarts and population clicks. This is not an alt-account
restriction and does not prevent player trading or crafting additional equipment.

## Catalog and pricing

The shops cover dusts/metals, components, electric processing machines, power and
capacitors, cargo, tools, storage/travel, and rune/relic stock. Complete Damascus,
Reinforced Alloy, Hazmat and Soulbound armor sets are offered piece by piece. With
InfinityExpansion2 enabled, its materials, production machines, storage tiers, tools,
Infinity armor, solar/void/Infinity panels and capacitors are included as well.

Prices use **physical inventory items**, not Vault or a bank balance. They are starter
prices for review, not a measured Albion valuation. The example offers eight Magic
Lumps I for four emeralds, once per player. High-end machinery and armor are priced
above basic supplies. Existing research, operating, power and world rules still apply;
buying a machine does not automatically unlock its research or power its network.

`market_catalog.yml` is a human-readable definition file, not a native portable shop.
Do not put it in `portable_shops/`. Published pre-feature builds ignore it.

## Safe updates and deletion

Each new offer has a permanent catalog ID. Population only appends IDs that have
never been installed; it never edits prices, results, limits or counters on an
installed offer. Use the existing admin editor to change those installed definitions
without clearing usage. New catalog rows should use new IDs; renaming an ID grants
a new independent allowance and is not a safe way to edit prices.

Native shop files retain `catalogId`, `catalogPlugins` and `marketCatalogHistory`.
The history also remembers deleted catalog offers. Deleting a single offer in the
editor and clicking populate therefore does not recreate it with unused counts.
Deleting a whole shop file or using an explicit admin counter reset remains a
deliberate destructive operation; this feature is not a backup of deleted files.
Do not manually remove the history or overwrite used shop files with starter copies.

Each affected shop is saved atomically through the existing strict ShopFileStore.
A refused save rolls back that shop's pending additions and retains its prior bytes
and counters. Completed writes to earlier shops are not rolled back; the result
reports partial failures. This is not a multi-file or Minecraft-player-data crash
transaction. Catalog generation does not clear locked Black Market receipts.

## NPC examples

New commands support names with spaces without quotes:

```text
magicshop openfor <player> Artificer - Power
magicshop openfor <player> Xur - Soulbound Armor
magicshop openfor <player> Xur - Infinity Armor
magicshop blackmarketfor <player>
```

Opening for another player requires `magicexpansion.shop.open.others` (default op).
Console NPC execution avoids temporarily making the player an operator. Self-browse
commands are `/magicshop open <shop name>` and `/magicshop blackmarket`.

For a standalone Citizens NPC, use its documented `<p>` player placeholder:

```text
/npc command add magicshop openfor <p> Xur - Infinity Armor
```

For an existing BetonQuest conversation, use a dialogue option invoking the command
instead of also attaching a click command to the same NPC. An example event is:

```yaml
events:
  open_soulbound_market: "command magicshop openfor %player% Xur - Soulbound Armor"
```

Use your installed BetonQuest version's player-name variable where required. This
snippet is not an automatic edit of Albion's existing Xur conversation. No Citizens
NPC assignment, BetonQuest file, permission grant or player data is changed here.

## Verification boundaries

Permanent tests exercise schema validation, duplicate IDs, bounded limits, missing
plugins/items/costs, history retention, refused saves and append-only updates. The
native audit targets pinned Paper 26.3 build 190 and Slimefun Legacy 4.1.74, with and
without InfinityExpansion2 2.0.13. It requires all 151/207 expected offers, real
registered-item metadata, two independent synthetic buyers, insufficient-payment
refusals, exhaustion of every allowance, repeated-population byte preservation and
a separate-JVM restart. Read the actual CI artifacts for pass/fail results.

Synthetic buyers/native inventories do not establish network-client, live NPC,
full-inventory overflow, arbitrary concurrent writers, abrupt-power-loss or Albion
full-stack behavior. The tests do not justify merging/publishing automatically.

Item-name source pins used when authoring the allowlist:
- SlimefunItems.java blob `92915d749775c3835fcdab5b6554bbac9a0e7284` in wickidcow/Slimefun-Legacy.
- IEItems.kt blob `49adba8d50aca35f7af8f201f41c50287e403f9c` in wickidcow/SF_InfinityExpansion2.
