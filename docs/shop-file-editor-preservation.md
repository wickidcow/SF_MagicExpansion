# Shop file and editor preservation — October 1, 2026

## Preserved contract

Continue the tested payment/page branch `61a0ccfa67dcf7c74ffc2aa67bcc2bdfdbbd358e`. Preserve item/research IDs, typed item data, prices, recipes, shop filenames, persisted quota keys, successful purchase semantics and machine rates. No new persistent trade IDs, schema migration, automatic recovery or plugin version bump. Public ShopManager methods and nested record classes remain available; boolean safety methods are additive. Version remains 1.1.7.

## Corrections

The historical forgiving YAML loader could turn corrupt input into an empty shop. A subsequent saveAll could then replace the original file. ShopFileStore now strictly reads the complete UTF-8/YAML document and refuses invalid records without manufacturing empty data. Healthy shops remain usable beside a rejected file. Failed directory enumeration is not treated as an empty directory.

Original filenames and source bytes are bound to loaded object identities. Changed, missing, externally edited, symbolic-link, stale or unreadable sources are not overwritten or deleted. New names cannot alias an existing file through sanitization. Reload retires old object identities. Valid empty trade lists remain supported.

Writes preserve unknown root/trade extension fields, validate complete item/cost/quota round trips, stage in the same directory, force staged file contents, recheck original bytes, and require atomic replacement. Failed staging/validation/replacement leaves the original untouched and locks the affected shop. Deletion removes memory only after the owned, unchanged file is successfully deleted.

Editors capture the selected trade identity and editable fields, not the first trade with a matching reward. Page selection and clamp agree; edits preserve purchase usage accumulated while the editor was open; new same-reward trades append; deletion targets the selected record. Reload, deletion or definition changes reject a stale editor instead of applying it to another record.

Chat cancellation happens synchronously on the chat event, but shop state and UI effects run in the Bukkit server task. Unique pending tokens prevent duplicate queued messages or old requests from applying twice. This does not make arbitrary external off-thread access to the public mutable shop list safe or establish Folia region ownership.

## Operator recovery

A refused shop is unavailable, not silently reset. Back up its original file and retained logs, correct the reported YAML/item/filename conflict, then explicitly reload the shop system or restart normally. A stale editor must be reopened. On a filesystem without atomic rename support, saves fail safely rather than silently switching to a lossy fallback. Do not delete the file to make an error disappear.

## Actual project validation

[Final validation 36857208372](https://github.com/wickidcow/SF_MagicExpansion/actions/runs/36857208372) ran two complete Maven clean-verify builds: stable Legacy 4.1.61 and the exact revision 98 core. Each passed all 61 tests with zero failures/errors/skips: 31 file cases, 12 editor cases, and the existing 18 payment/page cases. Every distributable base class passed the Java 21 ceiling (457 classes); no test/probe libraries were shipped.

Evidence artifact 11159054228 SHA-256: `3c63c4abe84abffc33713b11d6d35b550e2c02e0e4fa28126c3864b48554e7ea`. Downloaded XML and all six source Git blobs were independently inspected. The project build still emits pre-existing deprecation/unchecked notes; no zero-warning claim or blanket suppression is made.

Earlier mock tests exposed real differences in pinned MockBukkit 4.110.0: nested containers emit an unsafe YAML Java tag, FLOAT becomes DOUBLE, and backing-Map equality compares byte arrays by reference. Production validation was not relaxed. The permanent suite explicitly tests rejection of those mock representations while exact FLOAT/array/nested-PDC successful persistence is checked on real Paper. Unknown untyped YAML scalars are compared to their actual persisted baseline, not an invented Java type tag.

## Real server evidence

[Run 36856039051](https://github.com/wickidcow/SF_MagicExpansion/actions/runs/36856039051) used the same four production-source blobs now promoted here and the same addon JAR on Paper 1.21.11 stable132/Java21, Paper26.2 stable129/Java25 and Paper26.3 beta140/Java25. It deliberately reproduced the original destructive overwrite, then passed ten first-boot checks and a separate second-server-process persistence check in every lane.

The probe calls actual ShopManager and GUI inventory/chat handlers, real Bukkit inventories, real files and the actual scheduler with explicitly synthetic no-network player/view facades. Checks cover refused corrupt sources, external edits, stale handles, exact rich item round trips, second-page/same-reward editing and deletion, retained live purchase counters, and single main-thread application of async creation/limit messages. The second process verifies durable identities, counts, quotas and rich item data. The unchanged corrupt bytes are retained.

Candidate runtime JAR SHA-256: `10bb30f6e06aa0c2b96f2864dc42d2b03db23049978e6344ee082bef79b0b697`. Exact core SHA-256: `55be5129bfb70e64fc16fbe53147c7ee6d3cccf7734844e6f1dda539444a59e0` (development provenance, not a stable-release certificate).

Downloaded and inspected runtime artifacts:

| Paper | Artifact | Archive SHA-256 |
| --- | --- | --- |
| 1.21.11 | 11158462793 | `3a9f864576fe2a3eb86a20cd44e44c542ddf8e07887016e919620cb1ddcd2b78` |
| 26.2 | 11159032502 | `76ba1757c42013523b77f045cfb4b8ec10c5a54ff2d20cf337f956e4b701b0c9` |
| 26.3 | 11158877670 | `6c859ef3878a01a3ef995fa5faa1a8c20b22f8835e26d6cb95e17463bd024347` |

Actual saved/reloaded YAML preserves the unregistered old item ID, display/lore, FLOAT 123.4567, LONG 9007199254740993, byte-array content, nested owner data, quantities and limits. Expected refused-load diagnostics are deliberately present; these are not globally error-free logs or a captured historical-world certificate.

## Re-running the server regression

The exact tested probe is retained outside production source at `tests/runtime/ShopSafetyProbe.java`. It is not packaged. The helper allocates only a fresh disposable server directory and leaves evidence intact. With Java25/Maven3.9+ and independently verified local server/core/addon JARs:

```sh
bash scripts/smoke_shop_safety.sh /absolute/core.jar /absolute/addon.jar /absolute/paper.jar
```

Set `SHOP_TEST_JAVA` to the Java21 executable to test the 1.21.11 runtime floor while keeping Java25 for Maven compilation. The helper's shell syntax was checked locally; the already-completed runtime evidence above used the isolated Actions runner with the identical probe/production source. Normal promoted-head CI and subsequent helper runs remain separately attributable.

## Limits and release gate

Atomic replacement is not a cross-process filesystem compare-and-swap, a directory-fsync power-loss guarantee, or a transaction spanning reward delivery, player inventory, quotas and disk. External plugin mutations, Folia concurrency, interrupted payments and corrupted black-market files are outside this patch. No server TPS improvement is claimed for strict serialization validation.

Only the six reviewed files, retained non-shipped runtime test and documentation/helper are promoted. Temporary transport and diagnostic workflows are excluded. No default-branch merge, stable publication, live-server installation or automatic conversion. The coordinated addon manifest must select this exact reviewed source and validate it independently before a new versioned core/bundle release.
