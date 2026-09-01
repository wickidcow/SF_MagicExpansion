from pathlib import Path
import subprocess

p = Path('src/main/java/io/Yomicer/magicExpansion/core/MagicExpansionItems.java')
s = p.read_text()
if 'FISH_BREED_POOL =' not in s:
    marker = '    public static final SlimefunItemStack ENERGY_CONNECTOR_GLASS_INFO = createDefaultItemGlow("ENERGY_CONNECTOR_GLASS_INFO",Material.GLASS);\n'
    if marker not in s:
        raise SystemExit('Missing ENERGY_CONNECTOR_GLASS_INFO insertion marker')
    s = s.replace(marker,
        '    public static final SlimefunItemStack REED_TASSEL = createDefaultItem("REED_TASSEL",Material.SUGAR_CANE);\n'
        '    public static final SlimefunItemStack FISH_BREED_POOL = createDefaultItemGlow("FISH_BREED_POOL",Material.CAULDRON);\n\n' + marker)
    p.write_text(s)

subprocess.run(['python3', '.github/scripts/fix_release10.py'], check=True)
