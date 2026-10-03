#!/usr/bin/env bash
# Destructive fixtures are created only in a newly allocated temporary directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CORE="$(realpath "${1:?Usage: smoke_shop_safety.sh <core.jar> <addon.jar> <paper.jar>}")"
ADDON="$(realpath "${2:?Pass the candidate addon JAR}")"
PAPER="$(realpath "${3:?Pass an independently verified Paper server JAR}")"
for TOOL in java javac jar mvn timeout sha256sum; do command -v "$TOOL" >/dev/null; done
for INPUT in "$CORE" "$ADDON" "$PAPER"; do test -s "$INPUT"; done
WORK="$(mktemp -d "${TMPDIR:-/tmp}/magic-shop-safety.XXXXXX")"
printf 'Disposable shop test and retained evidence: %s\n' "$WORK"
cd "$ROOT"
mvn -B -ntp org.apache.maven.plugins:maven-install-plugin:3.1.3:install-file \
    "-Dfile=$CORE" -DgroupId=com.github.wickidcow -DartifactId=Slimefun-Legacy \
    -Dversion=shop-safety-test -Dpackaging=jar -DgeneratePom=true > "$WORK/core-install.log" 2>&1
mvn -B -ntp -Dslimefun.version=shop-safety-test dependency:build-classpath \
    "-Dmdep.outputFile=$WORK/classpath.txt" > "$WORK/classpath.log" 2>&1
mkdir -p "$WORK/probe-classes" "$WORK/server/plugins/magicexpansion/portable_shops"
javac --release 21 -cp "$ADDON:$(cat "$WORK/classpath.txt")" -d "$WORK/probe-classes" \
    tests/runtime/ShopSafetyProbe.java tests/runtime/Upstream13Checks.java \
    tests/runtime/FishingCompatibilityChecks.java > "$WORK/probe-compile.log" 2>&1
printf "name: ShopSafetyProbe\nmain: audit.ShopSafetyProbe\nversion: '1'\napi-version: '1.21.11'\ndepend: [Slimefun, magicexpansion]\n" > "$WORK/probe-classes/plugin.yml"
jar --create --file "$WORK/server/plugins/ShopSafetyProbe.jar" -C "$WORK/probe-classes" .
cp "$CORE" "$WORK/server/plugins/Slimefun.jar"
cp "$ADDON" "$WORK/server/plugins/MagicExpansion.jar"
cp "$PAPER" "$WORK/server/server.jar"
sha256sum "$WORK/server/server.jar" "$WORK/server/plugins/"*.jar > "$WORK/input-sha256.txt"
printf 'trades: [unterminated\n' > "$WORK/server/plugins/magicexpansion/portable_shops/Blocked.yml"
printf 'eula=true\n' > "$WORK/server/eula.txt"
printf 'online-mode=false\nserver-ip=127.0.0.1\nserver-port=0\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\npause-when-empty-seconds=-1\n' > "$WORK/server/server.properties"
for PHASE in 1 2; do
    if [[ "$PHASE" == 1 ]]; then LABEL=first; MARKER=SHOP_SAFETY_FIRST_BOOT_PASS; else LABEL=second; MARKER=SHOP_SAFETY_SECOND_BOOT_PASS; fi
    STATUS=0
    (cd "$WORK/server" && timeout --kill-after=20s 240s "${SHOP_TEST_JAVA:-java}" -Xms512M -Xmx2G \
        -Dshop.safety.phase="$PHASE" -jar server.jar --nogui > "$LABEL.log" 2>&1) || STATUS=$?
    cp -r "$WORK/server/plugins/magicexpansion/portable_shops" "$WORK/shops-$LABEL"
    if [[ "$STATUS" != 0 ]] || ! grep -Fxq "$MARKER" "$WORK/server/safety-result-$LABEL.txt"; then
        tail -n 120 "$WORK/server/$LABEL.log" >&2
        printf 'Shop safety test failed; retained evidence: %s\n' "$WORK" >&2
        exit 1
    fi
    if grep -Eq 'SHOP_SAFETY_RUNTIME_FAIL|Error occurred while enabling|NoClassDefFoundError|NoSuchMethodError' "$WORK/server/$LABEL.log"; then
        tail -n 120 "$WORK/server/$LABEL.log" >&2; exit 1
    fi
    cat "$WORK/server/safety-result-$LABEL.txt"
done
printf 'PASS: real shop file/editor/chat checks and separate server restart. Evidence: %s\n' "$WORK"
