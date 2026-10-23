#!/usr/bin/env python3
"""Fail-closed checks for the published-core Black Market full-stack workflow.

Runs without Maven, Minecraft, network access, or changes to release assets.
"""
from __future__ import annotations

import ast
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/blackmarket-full-stack.yml"
source = WORKFLOW.read_text(encoding="utf-8")

baseline = (
    "v4.1.77",
    "a8b62908e6fb8fd949f585faa7de30e863c6deaa",
    "Slimefun-Legacy4.1.77.jar",
    "dc44e130ff1279133992797ab09d518fc55a30d8e2eb0a0f831727064219d044",
    "published-r150.zip",
    "f4aa46b87563ede5c6f80eab7e03fc61d836e1e6bd13fd6636c189672026b9f2",
    "assert manifest['bundle_revision'] == 150",
    "SF_MagicExpansion1.1.18.jar",
    "7c2c1a927176ebd0aaab4481dcc21a51320f7f10",
)
for required in baseline:
    assert required in source, f"Missing verified release pin or check: {required}"

for obsolete in ("v4.1.76", "published-r145.zip", "'1.1.17'"):
    assert obsolete not in source, f"Stale release assumption: {obsolete}"

for contract in (
    "len(manifest['addons']) == 45",
    "len(unchanged) == 44",
    "'publication': 'NOT_PUBLISHED'",
    "manifest['bundle_revision'] = 151",
    "SLIMEFUN_SMOKE_VERSION: '4.1.77'",
    "tuple(map(int, version.split('.'))) >= (1, 1, 18)",
):
    assert contract in source, f"Missing live gameplay or preservation contract: {contract}"

assert "gh release upload" not in source, "The test workflow must never publish a release"
assert "gh api --method DELETE" not in source, "The test workflow must not delete release assets"

lines = source.splitlines()
found = 0
at = 0
while at < len(lines):
    line = lines[at]
    if re.fullmatch(r"\s*python3 - <<'PY'\s*", line):
        indent = len(line) - len(line.lstrip(" "))
        at += 1
        payload = []
        while at < len(lines) and lines[at] != " " * indent + "PY":
            assert lines[at].startswith(" " * indent), "Here-doc indentation changed"
            payload.append(lines[at][indent:])
            at += 1
        assert at < len(lines), "Unterminated Python here-doc"
        ast.parse("\n".join(payload) + "\n", filename=f"{WORKFLOW}:{at + 1}")
        found += 1
    at += 1
assert found == 2, f"Expected two complete Python validation blocks, got {found}"
print("PASS: exact 4.1.77/r150 pins, 44-JAR preservation, no publication, and Python syntax")
