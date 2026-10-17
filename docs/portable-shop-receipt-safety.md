# Portable shop purchase-counter safety candidate

This repair is included in the coordinated 1.1.16 candidate. The native baseline
evidence below describes the earlier published 1.1.14 behavior and its regression
checks; it is not the identity of the new distributable.

This follow-up to MagicExpansion 1.1.14 is unreleased. It is separate from the randomized Black Market item fix and from stable-read PR #19. Do not distribute a branch JAR as the already-published 1.1.14 release.

## Reproduced published behavior

The regular `portable_shops` purchase handler issued a reward before persisting purchase counters. If the shop save was refused, the original file remained intact but no purchase was recorded on disk. Restarting then restored an unused per-player allowance despite the earlier reward.

The native baseline audit at commit `acea12d4f22b267621c264c419a5cd58a090a819`, run `38735185042`, uses the exact published 1.1.14 JAR, Slimefun 4.1.74 and Paper 26.3 build 190. It verifies all four authored shops and 32 offers, including full custom-item components and quantities. Normal paths passed: 64 paid trades across two independent users, 64 insufficient-payment refusals, and 192 repeat refusals across immediate/reload/separate-JVM-restart checks. A separate controlled save conflict reproduced a reward without a receipt and a repeat after restart, so the safety workflow correctly failed. No production files or real player identities were used.

## Candidate change

A regular shop transaction now requires a successful counter save before issuing its reward. A refused save restores the exact prepayment storage slots, restores tentative counter values and keeps the affected shop's existing read-only conflict behavior. The original shop file and other players' usage are retained. The existing void `recordPurchase` method is kept for binary/source compatibility; reward issuers must use and check `tryRecordPurchase`.

The patch does not change prices, stock, limits, recipes, item IDs or metadata, YAML keys, the four-hour mystery-market rotation, or Black Market receipt handling. It does not edit production data, reset usages or automatically unlock a conflicted shop.

## Validation and limits

Ten permanent counter-commit tests cover success, refusal, throwing saves, independent allowances, duplicate/global-cap refusal, unlimited conventions and maximum counter boundaries. The native portable-shop audit also checks authored custom costs against the actual Slimefun item definitions, restores payments on a deliberately refused receipt and rejects the previously reproduced duplicate-after-restart outcome. Record actual candidate test results in PR #21; the presence of tests is not evidence that they executed or passed.

This is not a cross-file crash-atomic transaction with Minecraft player saves. Native inventories and shop handlers use synthetic player identity/menu handoff, not network-connected clients. Full-inventory reward overflow, abrupt power loss, arbitrary concurrent external writers and all third-party integrations need separate coverage. Existing protection of unreadable records is not authorization to reconstruct or delete their data.

The authored configuration is one purchase per offer per player (`personalLimit: 1`, `globalLimit: 0`). Never reinstall empty starter `personalUsed` maps over shops after players start trading. Missing authored shops do not prevent the separate randomized Black Market from generating offers.
