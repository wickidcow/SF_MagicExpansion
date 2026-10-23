#!/usr/bin/env python3
"""Guard the resource-generator 54-slot menu contract without launching a server."""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
FOLDER = ROOT / "src/main/java/io/Yomicer/magicExpansion/items/electric/resourceGenerator"

for machine in ("ResourceMachine", "ResourceRandomOneMachine"):
    source = (FOLDER / f"{machine}.java").read_text(encoding="utf-8")
    output = re.search(r"OUTPUT_SLOTS\s*=\s*new int\[\]\s*\{([^}]*)\}", source)
    assert output, f"{machine}: output slot declaration not found"
    slots = [int(s.strip()) for s in output.group(1).split(",") if s.strip()]
    assert slots == list(range(18, 54)), f"{machine}: unexpected output slot contract: {slots}"

    setup = source.split("protected void setupMenu(BlockMenuPreset preset) {", 1)
    assert len(setup) == 2, f"{machine}: no menu setup found"
    body = setup[1].split("\n\t@Override", 1)[0]
    size = re.search(r"preset\.setSize\(54\);", body)
    background = re.search(r"preset\.drawBackground\(", body)
    assert size and background and size.start() < background.start(), (
        f"{machine}: must reserve 54 slots before drawing an initial 18-slot background"
    )
    print(f"PASS: {machine} explicitly reserves six rows for output slots 18-53")

base = (ROOT / "src/main/java/io/Yomicer/magicExpansion/items/abstracts/AbstractMachine.java").read_text(encoding="utf-8")
assert "slotsWithinMenu(size, getOutputSlots())" in base
assert "if (!isMenuLayoutValid(menu))" in base
random_one = (FOLDER / "ResourceRandomOneMachine.java").read_text(encoding="utf-8")
assert random_one.index("if (!isMenuLayoutValid(menu))") < random_one.index("PendingMachineOutput.flush")
print("PASS: invalid legacy menus cannot reach output processing")
