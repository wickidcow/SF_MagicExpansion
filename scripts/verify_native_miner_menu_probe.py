#!/usr/bin/env python3
"""Verify the native Mining Robot first-created-menu regression is wired into CI."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
PROBE = ROOT / "tests/runtime/MachineTransactionProbe.java"
WORKFLOW = ROOT / ".github/workflows/item-catalog-runtime.yml"

source = PROBE.read_text(encoding="utf-8")
workflow = WORKFLOW.read_text(encoding="utf-8")

# The check runs against actual newly created Slimefun block/menu instances
# before item insertion, ticking, viewer opens or preset reloads.
creation = source.split("private Fixture createFixture(SlimefunItem item, Location location)", 1)
assert len(creation) == 2, "Native fixture creation method missing"
method = creation[1].split("private Location location(", 1)[0]
assert method.index("BlockMenu menu = data.getBlockMenu();") < method.index(
    "menu.toInventory().getSize() == 54") < method.index(
    "Slimefun.getTickerTask().pauseMachineTicker(location)")
for guard in (
    "item instanceof ResourceMachine || item instanceof ResourceRandomOneMachine",
    "outputs.length == 36",
    "outputs[i] == i + 18",
    "ItemStack initialLastOutput = menu.getItemInSlot(53)",
    "initialLastOutput == null || initialLastOutput.getType().isAir()",
):
    assert guard in method, f"Missing six-row/output-slot assertion: {guard}"

assert 'initial_six_row_resource_menu_checks=' in source
assert 'resourceMenuLayoutChecks++' in source
assert 'No initial six-row resource-generator menu layout was verified' in source

for required in (
    "tests/runtime/MachineTransactionProbe.java",
    "a8b62908e6fb8fd949f585faa7de30e863c6deaa",
    "Slimefun-Legacy4.1.77.jar",
    "dc44e130ff1279133992797ab09d518fc55a30d8e2eb0a0f831727064219d044",
    "f4aa46b87563ede5c6f80eab7e03fc61d836e1e6bd13fd6636c189672026b9f2",
    "manifest['bundle_revision']==150",
    "SLIMEFUN_SMOKE_VERSION: '4.1.77'",
    "MACHINE_TRANSACTION_PASS",
):
    assert required in workflow, f"Missing exact runtime evidence check: {required}"

assert len(re.findall(r"software: (?:paper|purpur), minecraft:", workflow)) == 4
assert "gh release upload" not in workflow
assert "v4.1.76" not in workflow
print("PASS: first-created 54-slot resource menu is validated on four exact-source runtime lanes")
