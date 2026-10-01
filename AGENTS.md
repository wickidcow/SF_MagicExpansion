# MagicExpansion maintenance

Preserve existing item/research IDs, persistent keys and types, inventories, shop identities, recipes, costs and machine rates. Minecraft 1.21.11/Java 21 is the runtime floor, not an age cutoff for saved items. Keep optional integrations optional.

Build with Java 25/Maven 3.9+, run `bash scripts/install_slimefun_api.sh`, then `mvn -B -ntp clean verify`. The helper installs a checksum-verified public stable release into the local Maven cache only. Candidate-core validation remains separate from this stable baseline.

Ship the baseline-built raw `SF_MagicExpansion<version>.jar`, not test/source archives. Run the permanent tests and supported API checks. Do not disable assertions, suppress all warnings, erase unreadable data or rewrite item identities to obtain a successful build. Do not claim mock/server API tests prove live-client, concurrency, crash-atomic or historical-world behavior.

Keep changes isolated from other active branches. Do not merge, publish or bump the release version before coordinated Slimefun/addon validation. Temporary audit runners are not part of the distributable plugin.
