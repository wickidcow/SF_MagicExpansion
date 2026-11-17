#!/usr/bin/env python3
"""Fail-closed integrity gate for the canonical, immutable MagicExpansion raw JAR.

Works in GitHub Actions on the exact same-run artifact and again on the
downloaded official release. It never mutates saved items or GitHub resources.
"""
from __future__ import annotations

import hashlib
from pathlib import Path
import re
import sys
import zipfile


def verify_jar(path: Path, version: str, expected_sha256: str) -> None:
    if re.fullmatch(r"[0-9]+(?:\.[0-9]+)+", version) is None:
        raise ValueError("Release version must be numeric and stable")
    name = f"SF_MagicExpansion{version}.jar"
    if path.name != name or not path.is_file() or path.stat().st_size == 0:
        raise ValueError("Expected the canonical nonempty raw MagicExpansion JAR")
    if re.fullmatch(r"[0-9a-f]{64}", expected_sha256) is None:
        raise ValueError("Release requires a verified SHA-256 digest")
    with path.open("rb") as source:
        actual_sha256 = hashlib.file_digest(source, "sha256").hexdigest()
    if actual_sha256 != expected_sha256:
        raise ValueError("Canonical raw JAR SHA-256 mismatch")

    main = "net/guizhanss/fastmachines/MagicExpansion.class"
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        if archive.testzip() is not None:
            raise ValueError("JAR contains a corrupt ZIP entry")
        if not {"plugin.yml", main}.issubset(names):
            raise ValueError("Missing MagicExpansion plugin identity or main class")
        plugin = archive.read("plugin.yml").decode("utf-8")
        if re.search(r"(?m)^name:\s*['\"]?magicexpansion['\"]?\s*$", plugin) is None:
            raise ValueError("Plugin name changed unexpectedly")
        if re.search(r"(?m)^main:\s*['\"]?net\.guizhanss\.fastmachines\.MagicExpansion['\"]?\s*$", plugin) is None:
            raise ValueError("MagicExpansion entrypoint changed unexpectedly")
        if re.search(r"(?m)^version:\s*['\"]?" + re.escape(version) + r"['\"]?\s*$", plugin) is None:
            raise ValueError("Plugin version does not match release")
        if "api-version: '1.21.11'" not in plugin:
            raise ValueError("Minecraft minimum API version changed")
        if any(name.startswith(("org/junit/", "org/mockbukkit/")) for name in names):
            raise ValueError("Test-only classes leaked into distributable")
        classes = 0
        for entry in names:
            if not entry.endswith(".class") or entry.startswith("META-INF/versions/"):
                continue
            header = archive.read(entry)[:8]
            if len(header) < 8 or header[:4] != b"\xca\xfe\xba\xbe":
                raise ValueError(f"Invalid Java class file: {entry}")
            major = int.from_bytes(header[6:8], "big")
            if major > 65:
                raise ValueError(f"Class {entry} targets Java newer than 21: {major}")
            classes += 1
        if classes == 0:
            raise ValueError("Plugin has no Java classes")
    print(f"PASS {name}: sha256={actual_sha256}, classes={classes}, Java <=21", flush=True)


def main() -> None:
    if len(sys.argv) != 4:
        raise SystemExit("Usage: verify_immutable_release_jar.py <jar> <numeric-version> <sha256>")
    try:
        verify_jar(Path(sys.argv[1]), sys.argv[2], sys.argv[3])
    except (ValueError, OSError, zipfile.BadZipFile) as exc:
        raise SystemExit(f"FAIL immutable MagicExpansion release artifact: {exc}") from exc


if __name__ == "__main__":
    main()
