# Slimefun Tick Monitor sampling repair

## Confirmed report
The existing SF Timings Machine displayed `Tick:0ms` and `currentTick:0ms`. The operator confirmed that running `/sf tick top` made readings appear. The old monitor called the profiler getters without requesting detailed samples; lightweight analytics telemetry does not refresh those detailed getter results.

## Candidate behavior
- One coordinator belongs to the registered monitor item and serves every placed instance.
- A ticking monitor requests a detailed sample automatically. Requests are separated by at least ten seconds after the previous request completes or reports failure.
- At most one request remains outstanding. An unanswered request is not repeatedly appended to the core queue, even after becoming overdue.
- Total and chunk readings are copied during completed-report delivery and stored together. Each distinct monitored world/chunk is queried once per sample, not once per monitor tick. Public chunk getters still walk detailed timing entries; this is not a constant-cost bulk snapshot API or an Albion-scale performance benchmark.
- No sample is `Waiting for sample`; an error or overdue request is `Sample unavailable`. Genuine measured zero remains valid. Failed or thirty-second-old cached readings are explicitly marked stale on the next monitor tick.
- Labels are `Slimefun total` and `This chunk`. Menu lore states sample age and that these are sampled Slimefun processing durations, not server TPS or MSPT.
- GUI and hologram updates use the synchronized block ticker. Existing two hologram positions and removal behavior are retained. No extra repeating task is installed, and no new samples are requested when no monitor ticks. If the entire Slimefun ticker is paused, the monitor also stops updating; this is not an independent server watchdog.
- Inactive chunk subscriptions expire after sixty seconds during subsequent activity; weak chunk references do not keep unloaded worlds alive.

## Compatibility boundary
Slimefun's public inspector API delivers text rather than a typed result callback. The adapter uses `getOrderType()` during completed-summary rendering to copy the public timing getters exactly once. Successful summaries begin with an empty separator; failure delivery is a non-empty first message. A first-message protocol change fails closed instead of publishing stale data as a fresh sample. The permanent test instantiates the actual dependency's `PerformanceSummary` and verifies this protocol. No reflection is used in production code; reflection is confined to this test of a package-private core class.

No core code, item or research IDs, recipes, storage keys, energy behavior, shop code, configuration defaults or release identity are changed. This candidate must not be confused with the published same-version JAR. Keep separate from active shop-safety branches and do not release until coordinated verification is complete.

## Validation
The new regression suite covers first-use sampling, shared requests and chunk deduplication, world/negative-coordinate isolation, cooldown timing, unanswered requests, timeout recovery, stale and partial samples, legitimate zeros, newly observed chunks, inactive subscriptions, and the real core report protocol. CI results must be checked at the exact candidate head; tests being present is not evidence that they passed.

Live acceptance remains required: restart with an existing monitor, wait without `/sf tick top`, observe automatic updates with working machines, compare against a nearby manual sample (not necessarily the identical cycle), test multiple monitors/chunks, then break/re-place and unload/reload monitors. Automated tests do not certify visual client output, Folia region-thread correctness, or large-server profiler cost.
