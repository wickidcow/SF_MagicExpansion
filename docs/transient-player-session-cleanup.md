# Player-quit transient session cleanup (upstream parity)

Yomicer's upstream MagicExpansion implements an omnibus PlayerCleanupListener.
This Legacy change ports the **safe parts only**, using the existing per-class
listeners rather than importing upstream's global static cleanup hooks.

## Fixed

- `Events`: clear a departing player's three world-selection maps on logout;
  use concurrent UUID maps for safe independent player-region access.
- `ItemNameTag`: discard a departing player's pending rename prompt.
  Existing conditional timeout removal continues to protect newer prompts.
- `ShopGUI`: clean temporary shop-name chat prompts, pending numeric edit
  prompts, and cached player/admin shop/black-market page numbers. Use concurrent
  outer UUID maps for player isolation.

## Deliberately preserved to prevent loss and duplication

- **ShopGUI.currentEditingData** and **ShopGUI.safeClose** are *not* cleared in
  `PlayerQuitEvent`: a still-open editor's `InventoryCloseEvent` must be
  allowed to return its uncommitted input items correctly. Clearing this
  prematurely could lose inventory contents or invoke the wrong close path.
- **BlackMarketManager**: no purchase receipts, locked purchase records,
  four-hour cycle data, or persisted ledgers are reset on logout.
- **ShopManager**: no saved shop/trade limits or monetary transactions altered.
- **StarShardsSword** and **CustomSequenceTool** already have their own quit
  cleanup; no duplicate registrations added.
- **AIManager** has asynchronous requests that may outlive a quit event.
  Its session lifecycle should be addressed separately with a request token/
  cancellation strategy, not a naive `Map.remove` that can race against
  in-flight responses.
- Existing high-risk `SlownessManager` attribute-modifier behavior remains
  outside the scope of this isolated cleanup patch.

## Validation

- JUnit tests check player selection isolation and cache cleanup; the shop
  test verifies active editor refund state survives until InventoryClose.
- Compile with Java 21 bytecode across Paper 1.21.11 and modern Paper targets.
- Run native restart/logout smoke tests with players editing a shop and using
  a Magic rename tag. Inventory items must not disappear or be duplicated.
- Keep GuizhanLib optional/absent, and do not introduce version-specific NMS,
  new features for Bedrock/Geyser, or new release artifacts.

No version bump, release or SF Addons ZIP change.
