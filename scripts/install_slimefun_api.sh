#!/usr/bin/env bash
# Install only the published, checksum-pinned stable API into the local Maven cache.
# This does not install a plugin on a server or publish a Maven artifact.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
VERSION=4.1.61
EXPECTED=329e22688557fbd0e0d51dba02fdc1e2e8d9f363774b29dafb0a502f41d6024f
DIRECTORY="$(pwd)/.build-deps"
JAR="$DIRECTORY/Slimefun-Legacy${VERSION}.jar"
mkdir -p "$DIRECTORY"
if ! printf '%s  %s\n' "$EXPECTED" "$JAR" | sha256sum --check --status; then
    TEMP="$(mktemp "$DIRECTORY/.slimefun-download.XXXXXX")"
    trap 'rm -f "$TEMP"' EXIT
    curl --fail --location --silent --show-error --retry 3 --max-time 180 \
        -H 'User-Agent: SF_MagicExpansion-build (https://github.com/wickidcow/SF_MagicExpansion)' \
        "https://github.com/wickidcow/Slimefun-Legacy/releases/download/v${VERSION}/Slimefun-Legacy${VERSION}.jar" \
        --output "$TEMP"
    printf '%s  %s\n' "$EXPECTED" "$TEMP" | sha256sum --check
    mv "$TEMP" "$JAR"
    trap - EXIT
fi
# Keep the maintained coordinate as a local provided-API alias for this exact release.
mvn --batch-mode --no-transfer-progress org.apache.maven.plugins:maven-install-plugin:3.1.3:install-file \
    "-Dfile=$JAR" -DgroupId=com.github.wickidcow -DartifactId=Slimefun-Legacy \
    "-Dversion=$VERSION" -Dpackaging=jar -DgeneratePom=true
