#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
python3 apply-magic-guide-name-fix.py "$PWD"
