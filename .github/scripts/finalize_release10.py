from pathlib import Path

# ---------- Register the new Release 10 gameplay content in the English fork ----------
p = Path('src/main/java/io/Yomicer/magicExpansion/MagicExpansionItemSetup.java')
s = p.read_text()

if 'import io.Yomicer.magicExpansion.items.electric.FishBreedPool;' not in s:
    s = s.replace('import io.Yomicer.magicExpansion.items.electric.entitykillMachinee.EntityKillMachine;',
                  'import io.Yomicer.magicExpansion.items.electric.FishBreedPool;\nimport io.Yomicer.magicExpansion.items.electric.entitykillMachinee.EntityKillMachine;')
if 'import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;' not in s:
    s = s.replace('import io.Yomicer.magicExpansion.items.misc.fish.FishingBook;',
                  'import io.Yomicer.magicExpansion.items.misc.fish.FishingBook;\nimport io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;')

# Cyan Bamboo now feeds the Release 10 progression by catching Reed Tassels.
for old in [
    'new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_CUIXIA, 10)',
    'new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_WEICHEN, 10)',
    'new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_RONGHUO, 28)',
    'new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_YUEJIN, 25)',
    'new WeightedItem(FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XINGHE, 25)',
]:
    s = s.replace(old, 'new WeightedItem(MagicExpansionItems.REED_TASSEL, 40)')

if 'Release 10: Reedflower / Snowy River / Fine Rain progression' not in s:
    marker = '        //星辰木\n'
    if marker not in s:
        marker = '        // Star Iron\n'
    if marker not in s:
        raise SystemExit('Could not find Water Cloud insertion point')

    block = r'''        // Release 10: Reedflower / Snowy River / Fine Rain progression
        // The fork keeps the original IDs but uses English guide presentation.
        new UnplaceableBlock(magicexpansionfishing, REED_TASSEL, RecipeType.NULL, new ItemStack[] {
                null, null, null, null, new ItemStack(Material.SUGAR_CANE), null, null, null, null
        }).register(plugin);

        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.BOOK), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.SNOWBALL), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.GLASS_BOTTLE), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.WHITE_DYE), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.BAMBOO), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA, 64)).register(plugin);

        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.SNOWBALL), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.FEATHER), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.ICE), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.LIGHT_BLUE_DYE), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.STICK), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN, 64)).register(plugin);

        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.STRING), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.GRAY_DYE), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.LILY_PAD), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.FEATHER), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG, 64)).register(plugin);
        new UnplaceableBlock(magicexpansionfishing, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, new ItemStack(Material.OAK_SAPLING), SlimefunItems.MAGIC_SUGAR,
                SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR, SlimefunItems.MAGIC_SUGAR
        }, sfItemAmount(FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING, 64)).register(plugin);

        // Catch-only progression items are registered so commands/guide lookups can resolve their IDs.
        new BaiLuYu(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU, RecipeType.NULL,
                new ItemStack[] {null, null, null, null, REED_TASSEL, null, null, null, null}).register(plugin);
        new XuePaoZhu(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU, RecipeType.NULL,
                new ItemStack[] {null, null, null, null, FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG, null, null, null, null}).register(plugin);
        new YuPiZhen(magicexpansionfishing, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN, RecipeType.NULL,
                new ItemStack[] {null, null, null, null, FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU, null, null, null, null}).register(plugin);

        new FishingRodWaterCloud(magicexpansionfishing, FISHING_ROD_BETWEEN_WATER_CLOUD_REED, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, null, REED_TASSEL,
                null, FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO, new ItemStack(Material.STRING),
                REED_TASSEL, null, MAGIC_EXPANSION_MAGIC_SUGAR_1
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 2);
            put(Enchantment.LURE, 2);
        }}, false, 1,
                Map.of(
                        "JianJia", List.of(new WeightedItem(SlimefunItems.MAGIC_LUMP_1, 222), new WeightedItem(REED_TASSEL, 40)),
                        "LuXue", FishingRodWaterCloud.buildLootTable("LuXue", Gen2Fish.LUXUE, Gen2Fish.LUWEIYING,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.SNOWBALL), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.WHITE_DYE), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU),
                        "WeiLu", FishingRodWaterCloud.buildLootTable("WeiLu", Gen2Fish.WEILU, Gen2Fish.CHENLULIN,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.GLASS_BOTTLE), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.LILY_PAD), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU),
                        "BaiLu", FishingRodWaterCloud.buildLootTable("BaiLu", Gen2Fish.BAILU, Gen2Fish.SHUANGBAILIAN,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.SNOWBALL), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.FEATHER), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU),
                        "LuYa", FishingRodWaterCloud.buildLootTable("LuYa", Gen2Fish.LUYA, Gen2Fish.XINHUANG,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.BAMBOO), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.LILY_PAD), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU)
                ), Arrays.asList(FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA, FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE,
                        FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU, FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU, FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA)).register(plugin);

        new FishingRodWaterCloud(magicexpansionfishing, FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, null, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU,
                null, FISHING_ROD_BETWEEN_WATER_CLOUD_REED, new ItemStack(Material.STRING),
                FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU, null, MAGIC_EXPANSION_MAGIC_SUGAR_1
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 3);
            put(Enchantment.LURE, 3);
        }}, false, 2,
                Map.of(
                        "NingShuang", FishingRodWaterCloud.buildLootTable("NingShuang", Gen2Fish.SHUANGJIAO, Gen2Fish.NINGBOJING,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.SNOWBALL), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.SNOW_BLOCK), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU),
                        "LuoXu", FishingRodWaterCloud.buildLootTable("LuoXu", Gen2Fish.XUXUE, Gen2Fish.CHENXULIN,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.FEATHER), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.WHITE_WOOL), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU),
                        "BingPo", FishingRodWaterCloud.buildLootTable("BingPo", Gen2Fish.BINGPO, Gen2Fish.XUANBINGLI,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.ICE), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.PACKED_ICE), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU),
                        "ChuJi", FishingRodWaterCloud.buildLootTable("ChuJi", Gen2Fish.JIGUANG, Gen2Fish.NUANYANGLIN,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.GOLD_NUGGET), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.GLOWSTONE_DUST), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU),
                        "ChuiLun", FishingRodWaterCloud.buildLootTable("ChuiLun", Gen2Fish.SUODIAO, Gen2Fish.GUZHOU,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.STICK), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.INK_SAC), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU)
                ), Arrays.asList(FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU,
                        FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI, FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN)).register(plugin);

        new FishingRodWaterCloud(magicexpansionfishing, FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, null, FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU,
                null, FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG, new ItemStack(Material.STRING),
                FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU, null, MAGIC_EXPANSION_MAGIC_SUGAR_1
        }, new HashMap<>() {{
            put(Enchantment.LUCK_OF_THE_SEA, 4);
            put(Enchantment.LURE, 4);
        }}, false, 3,
                Map.of(
                        "FengSi", FishingRodWaterCloud.buildLootTable("FengSi", Gen2Fish.YOUSI, Gen2Fish.FENGXIAO,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.STRING), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.COBWEB), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN),
                        "YanYu", FishingRodWaterCloud.buildLootTable("YanYu", Gen2Fish.YANYU, Gen2Fish.WUYINLU,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.GRAY_DYE), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.LILY_PAD), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN),
                        "LianBai", FishingRodWaterCloud.buildLootTable("LianBai", Gen2Fish.LIANBAI, Gen2Fish.YINGBO,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.WHITE_DYE), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.LILY_PAD), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN),
                        "XiaoFeng", FishingRodWaterCloud.buildLootTable("XiaoFeng", Gen2Fish.XIAOFENG, Gen2Fish.POXIAOLIN,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.FEATHER), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.CLOCK), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN),
                        "XieYing", FishingRodWaterCloud.buildLootTable("XieYing", Gen2Fish.XIEYING, Gen2Fish.ANLIU,
                                Arrays.asList(FishingRodWaterCloud.elementMaterial(new ItemStack(Material.OAK_SAPLING), 300), FishingRodWaterCloud.elementMaterial(new ItemStack(Material.INK_SAC), 304)), FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN)
                ), Arrays.asList(FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU,
                        FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG, FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING)).register(plugin);

        new FishBreedPool(magicexpansionrecipemachine, FISH_BREED_POOL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.GLASS), new ItemStack(Material.GLASS), new ItemStack(Material.GLASS),
                new ItemStack(Material.GLASS), new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.GLASS),
                new ItemStack(Material.GLASS), new ItemStack(Material.GLASS), new ItemStack(Material.GLASS)
        }).register(plugin);

        new PageChest(magicexpansionspecialitem, PAGE_CHEST, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                new ItemStack(Material.CHEST), MAGIC_EXPANSION_MAGIC_SUGAR_1, new ItemStack(Material.CHEST),
                MAGIC_EXPANSION_MAGIC_SUGAR_1, MAGIC_EXPANSION_TO_MAGIC_ITEM_BASIC, MAGIC_EXPANSION_MAGIC_SUGAR_1,
                new ItemStack(Material.CHEST), MAGIC_EXPANSION_MAGIC_SUGAR_1, new ItemStack(Material.CHEST)
        }).register(plugin);

'''
    s = s.replace(marker, block + marker)

p.write_text(s)

# ---------- English player-facing strings for the new Release 10 systems ----------

def replace_file(path, mapping):
    f = Path(path)
    text = f.read_text()
    for old, new in mapping.items():
        text = text.replace(old, new)
    f.write_text(text)

replace_file('src/main/java/io/Yomicer/magicExpansion/Listener/AccelerationUseListener.java', {
    '§b[鱼能] §r已向 §f': '§b[Fish Energy] §rInjected §f',
    ' §r注入加速鱼能（倍率 ×': ' §rwith an acceleration buff (×',
    '，持续 5 分钟）': ', 5 minutes)',
})

replace_file('src/main/java/io/Yomicer/magicExpansion/items/electric/FishBreedPool.java', {
    '育种池·杂交规则': 'Breeding Pool · Crossbreeding Rules',
    '子代元素 = 父①×0.4 + 父②×0.4': 'Offspring elements = Parent 1×0.4 + Parent 2×0.4',
    '20% 概率逐维 ±5% 变异偏移': '20% chance per element for a ±5% mutation',
    '子代品质 = 父母均值 ±0.05': 'Offspring quality = parent average ±0.05',
    '基因型: 30% 继承父母一方, 70% 随机': 'Trait: 30% inherited, 70% random',
    '子代鱼种随机取父本或母本一方': 'Offspring species is inherited from either parent',
    '父鱼①': 'Parent Fish 1',
    '父鱼②': 'Parent Fish 2',
    '开始育种': 'Start Breeding',
    '消耗两条父鱼, 产出一条子代鱼': 'Consumes two parent fish and produces one offspring',
    '制作时间 2 秒': 'Processing time: 2 seconds',
    '育种池：需要放入两条二代鱼': 'Breeding Pool: insert two Generation 2 fish.',
    '育种池：输出槽已满, 请先取出子代': 'Breeding Pool: output slot is full; remove the offspring first.',
    '育种中... 请稍候 2 秒': 'Breeding... processing for 2 seconds.',
    '育种中...': 'Breeding...',
    '请稍候 2 秒': 'Processing for 2 seconds',
    '输出槽为空时才能育种': 'The output slot must be empty to breed.',
    '点击后需等待 2 秒制作': 'Breeding completes 2 seconds after clicking.',
    '育种池：制作期间父鱼被移走, 已取消': 'Breeding Pool: a parent fish was removed; breeding cancelled.',
    '育种池：父鱼数据异常, 无法育种': 'Breeding Pool: invalid parent data; breeding cancelled.',
    '育种完成！': 'Breeding complete!',
})

replace_file('src/main/java/io/Yomicer/magicExpansion/items/misc/PageChest.java', {
    '跃迁储物箱 · 第 ': 'Page Chest · Page ',
    ' 页 (左键上一页 右键下一页)': ' (left click: previous · right click: next)',
})

replace_file('src/main/java/io/Yomicer/magicExpansion/items/misc/baitbag/BaitBagMenu.java', {
    '织梦者': 'Dreamweaver',
    '水云间': 'Between Water and Clouds',
    '记忆碎片': 'Memory Fragment',
    '芦花': 'Reedflower',
    '寒江雪': 'Snowy River',
    '细雨·斜风': 'Fine Rain · Slanting Wind',
    '萌新鱼竿/风语者之竿': 'Starter Rod / Wind Speaker Rod',
    '纠缠之节：终焉之丝·悖论为钩': 'Entangled Knot: Final Thread · Paradox Hook',
    '青竹竿': 'Cyan Bamboo Rod',
    '芦花钓': 'Reedflower Rod',
})

# Allow the guide-button listener to recognize the English fork's rod titles too.
p = Path('src/main/java/io/Yomicer/magicExpansion/Listener/fishingListener/GuidePoolButtonListener.java')
s = p.read_text()
s = s.replace('if (title == null || !ChatColor.stripColor(title).contains("水云间")) {',
              'String strippedTitle = title == null ? null : ChatColor.stripColor(title);\n        if (strippedTitle == null || !(strippedTitle.contains("水云间") || strippedTitle.contains("Between Water and Clouds") || strippedTitle.contains("Water Cloud"))) {')
p.write_text(s)

# Generation 2 fish: English names, rarity/trait labels, and player lore.
gen2 = {
    '云隐鱼':'Cloudveil Fish','浪息鱼':'Wavebreath Fish','痕波鱼':'Rippletrace Fish','雾屿鱼':'Mist Isle Fish',
    '芦雪鱼':'Reed-Snow Fish','芦苇影':'Reed Shadow','苇露鱼':'Reed-Dew Fish','晨露鳞':'Morning Dewscale',
    '白露鱼':'White Dew Fish','霜白鲢':'Frostwhite Carp','芦芽鱼':'Reed-Sprout Fish','新篁鲈':'New Bamboo Perch',
    '霜鲛':'Frostfin','凝波晶':'Stillwave Crystal','絮雪鱼':'Drifting Snow Fish','沉絮鳞':'Sunken Flakefin',
    '冰魄鱼':'Ice-Soul Fish','玄冰鲤':'Dark-Ice Carp','霁光鱼':'Clearsky Gleam Fish','暖阳鳞':'Warm Sunscale',
    '蓑钓鱼':'Raincloak Fish','孤舟影':'Lone Boat Shadow','游丝鱼':'Wispthread Fish','风绡鳞':'Wind-Silk Scale',
    '烟雨鲤':'Mist-Rain Carp','雾隐鲈':'Mistveil Perch','涟白鱼':'White Ripple Fish','映波鳞':'Waveglint Scale',
    '晓风鱼':'Dawnwind Fish','破晓鳞':'Daybreak Scale','斜影鱼':'Slantshadow Fish','暗流鲷':'Undercurrent Bream',
    '§f凡品':'§fCommon','§e奇珍':'§eRare','§d史诗':'§dEpic','§d加速种':'§dAcceleration','§a合成种':'§aSynthesis',
    '藏身云底，身形似有若无，于水面时隐时现':'Hidden beneath the clouds, it flickers in and out of sight at the surface.',
    '逐浪而栖，尾鳍过处，波澜渐息':'It follows the waves, leaving calm water behind its tail.',
    '游过即留一圈电光水痕，转瞬即逝':'Its passing leaves a brief ring of electric light across the water.',
    '栖于雾绕的礁屿，鳞染夜露，沉静如石':'It rests around misty reefs, its scales darkened by night dew.',
    '芦花似雪纷落处，此鱼悄然而至':'It appears quietly where reed flowers fall like snow.',
    '影藏芦苇丛底，人近则潜，风起则现':'It hides beneath reeds, vanishing from people and appearing with the wind.',
    '栖于苇叶凝露处，晨光中背沾露珠':'It gathers beneath dew-covered reeds, sparkling in the morning light.',
    '晓露垂苇，鳞映朝光，晶莹剔透':'Morning dew and sunrise turn its scales crystal-bright.',
    '白露凝江时现世，鳞色皎白如霜':'It appears when white dew settles on the river, with frost-pale scales.',
    '露凝为霜，鲢身素白，游若浮云':'Frost-white and weightless in motion, it drifts like a cloud.',
    '循初生芦芽香气而来，尚带春意':'Drawn by fresh reed shoots, it carries the feeling of spring.',
    '栖于新篁水畔，鳞染竹色，清冷自持':'It lives beside new bamboo, wearing a cool green cast.',
    '生于霜色初凝的江水，鳞面皆结细霜':'Born in the first frost of the river, its scales carry fine ice.',
    '寒波凝成冰晶，冻结处鱼影驻足':'Cold waves crystallize around it as though the water pauses.',
    '随落絮飘入寒江，轻若无物':'It drifts into the cold river with falling flakes, almost weightless.',
    '絮尽而后沉，负满江雪色归于江底':'When the drifting snow ends, it sinks carrying the river winter with it.',
    '魄寄水心千年，寒气透骨，鱼目如冰':'Its icy gaze carries a chill said to have rested in the river for ages.',
    '鲤身覆一层玄冰，游动时粼光如练':'Dark ice coats this carp, flashing as it moves.',
    '雪后初晴，霁光映鳞，暖意暗藏':'After snow, clear sunlight glints across its scales with hidden warmth.',
    '借一缕晴阳回暖水面，游鱼竞逐':'It follows the first warm sunlight returning to the water.',
    '惯于蓑衣孤钓处盘旋，似与人同守寒江':'It circles lonely fishing spots as if keeping watch over the winter river.',
    '一竿垂纶、千山暮雪，影随孤舟而远':'Its shadow follows a lone boat beneath distant evening snow.',
    '循风丝而来，牵一尾涟漪入水':'It follows threads of wind, pulling a fine ripple behind it.',
    '鳞薄如绡，风过即舞，似无形之丝':'Its silk-thin scales dance whenever the wind passes.',
    '惯破烟雨而出，鳞染一江雾色':'It breaks through misty rain with scales colored by the river fog.',
    '雾锁长天时方见其形，行藏莫测':'It appears only beneath heavy mist and never stays where expected.',
    '波心一圈白光浮过，游鱼皆探首':'A pale ring of light crosses the water whenever it surfaces.',
    '细雨斜织，鳞光与波影交映':'Fine rain weaves across its scales and the reflected waves.',
    '破晓风起时结群而出，衔岸草香':'Schools emerge with the dawn wind, carrying the scent of shore grass.',
    '天色初明，一缕暖光先于点过水面':'A warm glimmer touches its scales just before sunrise.',
    '疏影落水、虚实难辨，诱鱼往往在影':'It blends with broken reflections until fish and shadow are hard to separate.',
    '栖于水面下暗流，静候落影入水':'It waits in underwater currents for shadows to fall across the surface.',
}
replace_file('src/main/java/io/Yomicer/magicExpansion/items/misc/fish/Gen2Fish.java', gen2)

replace_file('src/main/java/io/Yomicer/magicExpansion/items/misc/fish/FishAttributeGenerator.java', {
    '§d品级·':'§dGrade · ', '§a品质系数: §r§f':'§aQuality: §r§f',
    '§e元素: §c火 ':'§eElements: §cFire ', ' §b水 ':' §bWater ', ' §d雷 ':' §dLightning ', ' §a风 ':' §aWind ', ' §6地 ':' §6Earth ',
    '§b基因型: ':'§bTrait: ', '§7亲本: §r§f':'§7Parents: §r§f',
    '可作 育种 / 元素附魔 材料':'Usable for breeding / elemental enchanting',
})

# ---------- English language.yml entries for every new Slimefun item ID ----------
p = Path('src/main/resources/language.yml')
s = p.read_text()
if 'FISHING_ROD_BETWEEN_WATER_CLOUD_REED:' not in s:
    marker = '    FISHING_STICK_STAR_IRON:\n'
    if marker not in s:
        raise SystemExit('Could not find language insertion point')
    lang = '''    FISHING_ROD_BETWEEN_WATER_CLOUD_REED:
      Name: Between Water and Clouds - Reedflower Rod
      Lore:
      - A second-tier Water Cloud rod woven from reed tassels.
      - Unlocks Generation 2 fishing and the path toward Snowy River.
    FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG:
      Name: Between Water and Clouds - Snowy River Rod
      Lore:
      - A cold-water rod refined with the White Reed Feather.
      - Improves Generation 2 fish quality and unlocks winter lures.
    FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU:
      Name: Between Water and Clouds - Fine Rain · Slanting Wind
      Lore:
      - A high-tier Water Cloud rod refined with a Snow-Soul Pearl.
      - Draws rarer Generation 2 fish from rain and mist.
    REED_TASSEL:
      Name: Reed Tassel
      Lore:
      - A special catch from the Cyan Bamboo Rod.
      - Used to craft the Reedflower Rod.
    FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA:
      Name: Reedbank Lure
      Lore: [A Reedflower Rod lure for magical materials.]
    FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE:
      Name: Reed Snow
      Lore: [A Reedflower Rod lure attuned to wind and snow.]
    FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU:
      Name: Reed Dew
      Lore: [A Reedflower Rod lure attuned to morning dew.]
    FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU:
      Name: White Dew
      Lore: [A Reedflower Rod lure attuned to frost-white waters.]
    FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA:
      Name: Reed Sprout
      Lore: [A Reedflower Rod lure carrying the scent of new reeds.]
    FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG:
      Name: Frostfall
      Lore: [A Snowy River lure attuned to forming frost.]
    FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU:
      Name: Falling Flakes
      Lore: [A Snowy River lure light enough to drift on cold water.]
    FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO:
      Name: Ice Soul
      Lore: [A Snowy River lure carrying deep frozen energy.]
    FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI:
      Name: First Clearing
      Lore: [A Snowy River lure warmed by the first light after snow.]
    FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN:
      Name: Lone Line
      Lore: [A Snowy River lure for quiet, patient fishing.]
    FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI:
      Name: Wind Thread
      Lore: [A Fine Rain lure carried by thin strands of wind.]
    FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU:
      Name: Mist Rain
      Lore: [A Fine Rain lure for fog-covered waters.]
    FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI:
      Name: White Ripple
      Lore: [A Fine Rain lure that leaves a pale ripple on the surface.]
    FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG:
      Name: Dawn Wind
      Lore: [A Fine Rain lure for the first breeze of morning.]
    FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING:
      Name: Slanting Shadow
      Lore: [A Fine Rain lure made for broken reflections and hidden currents.]
    FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU:
      Name: White Reed Feather
      Lore:
      - A rare Reedflower Rod catch.
      - Required to craft the Snowy River Rod.
    FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU:
      Name: Snow-Soul Pearl
      Lore:
      - A rare Snowy River catch.
      - Required to craft the Fine Rain · Slanting Wind rod.
    FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN:
      Name: Raincloak Needle
      Lore:
      - A rare Fine Rain · Slanting Wind catch.
      - A key material for the next Water Cloud progression tier.
    FISH_BREED_POOL:
      Name: Fish Breeding Pool
      Lore:
      - Breed two Generation 2 fish into one offspring.
      - Offspring inherit species, elements, quality, and traits.
    PAGE_CHEST:
      Name: Page Chest
      Lore:
      - Five pages of storage in a single Slimefun cargo-compatible chest.
      - Click outside the inventory window to change pages.
'''
    s = s.replace(marker, lang + marker)
p.write_text(s)

# Remove all one-shot port/fix tooling so the release branch has only the normal build workflow.
for name in [
    '.github/workflows/fix-release10-integration.yml',
    '.github/workflows/fix-release10-integration-v2.yml',
    '.github/workflows/fix-release10-integration-v3.yml',
    '.github/workflows/fix-release10-integration-v4.yml',
    '.github/scripts/fix_release10.py',
    '.github/scripts/fix_release10_v2.py',
    '.github/scripts/fix_release10_v3.py',
    '.github/scripts/finalize_release10.py',
    '.github/workflows/finalize-release10.yml',
]:
    q = Path(name)
    if q.exists():
        q.unlink()
