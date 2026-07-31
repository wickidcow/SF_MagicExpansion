#!/usr/bin/env python3
"""Change MagicExpansion's main Slimefun guide category title to 'Magic'."""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path


SOURCE_PATH = Path(
    "src/main/java/io/Yomicer/magicExpansion/MagicExpansionItemSetup.java"
)

BLOCK_PATTERN = re.compile(
    r"""(
        public\s+static\s+final\s+NestedItemGroup\s+magicexpansion
        \s*=\s*new\s+NestedItemGroup\s*\(
        \s*new\s+NamespacedKey\(
            MagicExpansion\.getInstance\(\),
            \s*"magicexpansion"
        \),
        \s*
    )
    new\s+CustomItemStack\(
        doGlow\(Material\.LIGHT\),
        \s*"[^"]*"
    \)
    \s*,\s*0
    """,
    re.VERBOSE | re.DOTALL,
)

ALREADY_FIXED_PATTERN = re.compile(
    r"""public\s+static\s+final\s+NestedItemGroup\s+magicexpansion
        .*?
        new\s+CustomItemStack\(
            doGlow\(Material\.LIGHT\),
            \s*"Magic"
        \)
        \s*,\s*0
    """,
    re.VERBOSE | re.DOTALL,
)


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Set MagicExpansion's main Slimefun guide category title to Magic."
    )
    parser.add_argument(
        "repository",
        nargs="?",
        default=".",
        help="MagicExpansion repository root (default: current directory)",
    )
    args = parser.parse_args()

    repository = Path(args.repository).expanduser().resolve()
    source_file = repository / SOURCE_PATH

    if not source_file.is_file():
        print(f"ERROR: Source file not found:\n{source_file}", file=sys.stderr)
        print(
            "Place this fixer in the root of the SF_MagicExpansion repository "
            "or pass the repository path as an argument.",
            file=sys.stderr,
        )
        return 1

    original = source_file.read_text(encoding="utf-8")

    if ALREADY_FIXED_PATTERN.search(original):
        print("No change needed: the main guide category is already named Magic.")
        return 0

    replacement = r'\1new CustomItemStack(doGlow(Material.LIGHT), "Magic"), 0'
    updated, replacements = BLOCK_PATTERN.subn(replacement, original, count=1)

    if replacements != 1:
        print(
            "ERROR: Could not safely locate the main MagicExpansion guide category.",
            file=sys.stderr,
        )
        print("No files were changed.", file=sys.stderr)
        return 2

    backup_file = source_file.with_name(source_file.name + ".bak")
    shutil.copy2(source_file, backup_file)
    source_file.write_text(updated, encoding="utf-8")

    print("Updated MagicExpansion guide category title:")
    print('  Material.LIGHT icon + title "Magic"')
    print(f"Changed: {source_file}")
    print(f"Backup:  {backup_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
