"""Recreate authored fixture data exactly, excluding YAML presentation/comments.

Full serialized custom items are retained beside this script and verified against
the original native-audit fingerprints. No production ledger or config is used.
"""
import copy
import hashlib
import json
from pathlib import Path
import yaml

def digest(value):
    return hashlib.sha256(json.dumps(value,sort_keys=True,ensure_ascii=False,separators=(',',':')).encode()).hexdigest()

DEFINITIONS = {
'Artificer - Machines.yml': [
[['MAGIC_EXPANSION_INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE',1],[['minecraft:iron_block',16],['minecraft:gold_block',8],['minecraft:redstone_block',16],['minecraft:diamond',8]]],
[['MAGIC_EXPANSION_DIRT_MEAL_MACHINE',1],[['minecraft:bone_block',32],['minecraft:dirt',32],['minecraft:emerald',16],['minecraft:diamond',8]]],
[['MAGIC_EXPANSION_LIGHT_EXTRACT_BASIC',1],[['minecraft:glowstone',16],['minecraft:sea_lantern',16],['minecraft:diamond',8],['minecraft:redstone_block',8]]],
[['MAGIC_EXPANSION_LIGHT_TRANSFORM_BASIC',1],[['minecraft:amethyst_block',16],['minecraft:glowstone',16],['minecraft:diamond',8],['minecraft:redstone_block',8]]],
[['MAGIC_EXPANSION_RESOURCE_MACHINE_WOOD_BASIC',1],[['minecraft:oak_log',32],['minecraft:bone_block',32],['minecraft:diamond',8],['MAGIC_EXPANSION_WOOD_ELEMENT',4]]],
[['MAGIC_EXPANSION_FIVE_ELEMENT_MINER',1],[['minecraft:netherite_ingot',2],['minecraft:diamond',16],['minecraft:redstone_block',16],['MAGIC_EXPANSION_PURE_FIVE_ELEMENT',4]]],
[['MAGIC_EXPANSION_FISHING_MACHINE',1],[['minecraft:fishing_rod',1],['minecraft:cod',32],['minecraft:diamond',8],['minecraft:heart_of_the_sea',1]]],
[['MAGIC_EXPANSION_SF_TIMINGS_HOLOGRAM',1],[['minecraft:comparator',8],['minecraft:quartz_block',8],['minecraft:diamond',4]]]],
'Artificer - Materials.yml': [
[['MAGIC_EXPANSION_GOLD_ELEMENT',16],[['minecraft:gold_ingot',32],['minecraft:iron_ingot',16],['minecraft:emerald',8]]],
[['MAGIC_EXPANSION_WOOD_ELEMENT',16],[['minecraft:oak_log',64],['minecraft:emerald',8]]],
[['MAGIC_EXPANSION_WATER_ELEMENT',16],[['minecraft:prismarine_shard',32],['minecraft:lapis_lazuli',16],['minecraft:emerald',8]]],
[['MAGIC_EXPANSION_FIRE_ELEMENT',16],[['minecraft:blaze_powder',32],['minecraft:magma_cream',16],['minecraft:emerald',8]]],
[['MAGIC_EXPANSION_EARTH_ELEMENT',16],[['minecraft:terracotta',32],['minecraft:obsidian',16],['minecraft:emerald',8]]],
[['MAGIC_EXPANSION_MAGIC_SUGAR_1',8],[['minecraft:sugar',64],['minecraft:honey_bottle',8],['minecraft:emerald',8]]],
[['MAGIC_EXPANSION_MAGIC_SUGAR_2',4],[['MAGIC_EXPANSION_MAGIC_SUGAR_1',32],['minecraft:emerald',16]]],
[['MAGIC_EXPANSION_FINAL_STRING_1',8],[['minecraft:string',64],['minecraft:white_wool',16],['minecraft:emerald',8]]]],
'Garran - Supplies.yml': [
[['minecraft:golden_carrot',32],[['minecraft:carrot',32],['minecraft:gold_ingot',16]]],
[['minecraft:ender_pearl',16],[['minecraft:rotten_flesh',32],['minecraft:emerald',16]]],
[['minecraft:saddle',2],[['minecraft:leather',32],['minecraft:iron_ingot',8]]],
[['minecraft:name_tag',4],[['minecraft:paper',32],['minecraft:emerald',8]]],
[['minecraft:experience_bottle',16],[['minecraft:lapis_lazuli',32],['minecraft:emerald',8]]],
[['minecraft:golden_apple',8],[['minecraft:apple',8],['minecraft:gold_ingot',32]]],
[['minecraft:cooked_beef',64],[['minecraft:beef',32],['minecraft:coal',16]]],
[['minecraft:crying_obsidian',16],[['minecraft:obsidian',32],['minecraft:amethyst_shard',16],['minecraft:emerald',4]]]],
'Xur.yml': [
[['minecraft:shulker_shell',2],[['minecraft:ender_pearl',16],['minecraft:diamond',16],['minecraft:chorus_fruit',32]]],
[['minecraft:totem_of_undying',1],[['minecraft:emerald',32],['minecraft:gold_ingot',16],['minecraft:nether_star',1]]],
[['minecraft:elytra',1],[['minecraft:diamond',64],['minecraft:phantom_membrane',16],['minecraft:nether_star',2]]],
[['minecraft:netherite_upgrade_smithing_template',1],[['minecraft:diamond',8],['minecraft:netherite_ingot',2],['minecraft:blackstone',16]]],
[['minecraft:enchanted_golden_apple',1],[['minecraft:gold_block',8],['minecraft:diamond',16],['minecraft:echo_shard',1]]],
[['MAGIC_EXPANSION_FIVE_ELEMENT_TOUCH',1],[['MAGIC_EXPANSION_PURE_FIVE_ELEMENT',4],['minecraft:diamond',16],['minecraft:nether_star',1]]],
[['MAGIC_EXPANSION_ENCHANTMENT_ERASER',1],[['minecraft:lapis_block',16],['minecraft:emerald',16],['minecraft:amethyst_shard',8]]],
[['MAGIC_EXPANSION_PURE_ELEMENT_INGOT',4],[['minecraft:netherite_scrap',16],['MAGIC_EXPANSION_PURE_FIVE_ELEMENT',8],['minecraft:emerald',16]]]]}
REFERENCES = {
'MAGIC_EXPANSION_INTEGRATION_ORIGIN_SLIME_MINERAL_POWDER_LINE':'3cb942e9e383dded26bfa691a209a1b53084abe8aa359046c4d2eb481c6e8846',
'MAGIC_EXPANSION_DIRT_MEAL_MACHINE':'f862446cf3a0bed7480a2cb34fa01e585d21144510666de19c54fabaa4428305',
'MAGIC_EXPANSION_LIGHT_EXTRACT_BASIC':'ffcca82e82ac052f77c9434c7dac0b137cb347825f6df81eae43193c1074a8ce',
'MAGIC_EXPANSION_LIGHT_TRANSFORM_BASIC':'9cbc3e5703340c66dea542fd9a9d986ef3672c5d5f30fa1959a31c9b1e119472',
'MAGIC_EXPANSION_RESOURCE_MACHINE_WOOD_BASIC':'227abaf0d43bf97aa5a56bf66363d8f72c0a49e46c4429e4bfc940bd7163ee38',
'MAGIC_EXPANSION_WOOD_ELEMENT':'330b8002325cfd73335b6d6f1b36952343b3fdb4e4aa6f91581375c01ef0febe',
'MAGIC_EXPANSION_FIVE_ELEMENT_MINER':'3018e32f52fe2292acb49c414251ca6455dc7c2992187487ee1ad760771365a5',
'MAGIC_EXPANSION_PURE_FIVE_ELEMENT':'b214567626adb3234af1769ea1b129ecd4e802c34c4c99efc4957de947f29aa3',
'MAGIC_EXPANSION_FISHING_MACHINE':'6eebd299fffd9388b5f391d1f2c7abeaef190296d32757e236750cf8e907a2d8',
'MAGIC_EXPANSION_SF_TIMINGS_HOLOGRAM':'7f6eac496c55da14094e199d1e92897faeea4c38c2d1d1f6a40b5f2fb5693688',
'MAGIC_EXPANSION_GOLD_ELEMENT':'f8d4ff927c6d94f70609f4a25a702c5165148bf8a5a802f6b738476c724cfb94',
'MAGIC_EXPANSION_WATER_ELEMENT':'478e4231243c603d4f96dcc7ee9fd26b56ccafd9918bd0a26eedc7906b300555',
'MAGIC_EXPANSION_FIRE_ELEMENT':'7c46c50ade739f6984d43481fd229dda757411f12065240ac1163a554b86acaa',
'MAGIC_EXPANSION_EARTH_ELEMENT':'60ba9071ff6a619407cf5e88dc4b26b1c30bdcd51b6033ac006c2d2b30787ec6',
'MAGIC_EXPANSION_MAGIC_SUGAR_1':'27e3b52b49fce505bb6690a08f74e346a6f63a5ce9fab0a6d86cf95dbebea790',
'MAGIC_EXPANSION_MAGIC_SUGAR_2':'562884ee50a93bc0b3db98d2c76736e620f5aab01b85dfdbd8d41ff3ae508d57',
'MAGIC_EXPANSION_FINAL_STRING_1':'a13bceac4dc068ca1b3d52f6f636847af4d3398305d20db6db35f47b130c758f',
'MAGIC_EXPANSION_FIVE_ELEMENT_TOUCH':'8263b2df6c90ce6349ce2f421aa395cdd8418acf849249283c298494a389297e',
'MAGIC_EXPANSION_ENCHANTMENT_ERASER':'afe9a76438c4901eb0bde6497e1c678d66619a00f379450a4fd543527a38d183',
'MAGIC_EXPANSION_PURE_ELEMENT_INGOT':'6e5648f50bf820e0a7bc703b1c93b615f06c7f70668c51503051e9a0d4eb9aa0'}
EXPECTED = {
'Artificer - Machines.yml':'d2587ab9b6012b86243ff4983c6940b14e858748421b1ef89e963e167a5e18bc',
'Artificer - Materials.yml':'892e4fc2a0a94cff85d1e2689570f666c0173dcf57df6f2ad70335caddd38a96',
'Garran - Supplies.yml':'b92c39c49bc66ed4b3e12fbe19f98babc5e758dee2c9c35869c3ad7019e671c0',
'Xur.yml':'146d9f3904fe26e0453531b3354e6d5f1b1c088591ccddacf4299649f1736aab'}

native_items = json.loads(Path(__file__).with_name('native-items.json').read_text(encoding='utf-8'))
assert set(native_items) == set(REFERENCES), 'Native fixture item identities differ'
found = {}
for key, expected in REFERENCES.items():
    value = native_items[key]
    assert digest(value) == expected, 'Native item reference differs: ' + key
    found[expected] = value

def item(spec):
    key,count=spec
    if key in REFERENCES: value=copy.deepcopy(found[REFERENCES[key]])
    else:
        assert key.startswith('minecraft:')
        value={'==':'org.bukkit.inventory.ItemStack','DataVersion':5023,'id':key,'schema_version':1}
    value['count']=count
    return value
for name,offers in DEFINITIONS.items():
    data={'trades':[{'result':item(result),'cost':[item(cost) for cost in costs],
                     'personalLimit':1,'globalLimit':0,'globalUsed':0,'personalUsed':{}}
                    for result,costs in offers]}
    assert digest(data)==EXPECTED[name], 'Authored shop semantic data differs: '+name
    path=Path('audit/fixtures')/name
    assert not path.exists(), 'Never overwrite a live fixture'
    path.write_text(yaml.safe_dump(data,sort_keys=False,allow_unicode=True))
    assert digest(yaml.safe_load(path.read_text()))==EXPECTED[name]
Path('audit/fixture-checksums.json').write_text(json.dumps({'scope':'complete parsed YAML data, not comments/formatting','shops':EXPECTED},indent=2)+'\n')
print('All four full parsed shop definitions exactly match the authored pack, including custom item components')
