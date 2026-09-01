package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.groups.HiddenNestedItemGroup;
import io.Yomicer.magicExpansion.items.groups.VirtualGuideGroup;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自绘菜单树注册表:
 * 所有一/二/三级分组的定义集中在这里, 按调用顺序构建层级;
 * 平铺组(叶子)点开直接进四级物品列表页, 容器组(带 children)自动生成子级菜单页与历史锚点.
 */
public final class GuideMenuGroups {

    private static final Map<String, SubItemGroup> GROUPS = new LinkedHashMap<>();
    private static final Map<String, VirtualGuideGroup> ANCHORS = new LinkedHashMap<>();
    private static final Map<String, List<ItemGroup>> CHILDREN = new LinkedHashMap<>();
    private static final Map<String, String> NAMES = new LinkedHashMap<>();
    private static final Map<String, String> PARENT = new LinkedHashMap<>();
    private static final List<ItemGroup> TOP_LEVEL = new ArrayList<>();
    private static final List<SubItemGroup> TOP_LEVEL_CONTAINERS = new ArrayList<>();
    private static HiddenNestedItemGroup hiddenContainer;

    private GuideMenuGroups() {
    }

    /** 由 MagicExpansionItemSetup 静态初始化时调用, 构建整棵菜单树 */
    public static void initialize() {
        hiddenContainer = new HiddenNestedItemGroup(
                new NamespacedKey(MagicExpansion.getInstance(), "container"),
                new CustomItemStack(Material.BARRIER)
        );

        // ===== 一级(特殊槽位): 附属信息, 固定显示在第一行第4格, 不进一级主页 =====
        specialContainer("attachmentinfo", "附属信息",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.PAPER),
                        ColorGradient.getRandomGradientName("附属信息"),
                        "点击查看贡献与更新日志", "attachmentinfo"), 0);
        flat("attachmentinfo", "contribution",
                new CustomItemStack(Utils.doGlow(Material.COMMAND_BLOCK), ColorGradient.getGradientNameVer2("贡献")));
        flat("attachmentinfo", "updateinfo",
                new CustomItemStack(Utils.doGlow(Material.PAPER), ColorGradient.getRandomGradientName("更新日志")));

        // ===== 一级: 材料 =====
        topContainer("material", "材料",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.SNOWBALL),
                        ColorGradient.getGradientName("材料"),
                        "点击查看资源与补丁材料", "material"), 1);
        flat("material", "resource",
                new CustomItemStack(Utils.doGlow(Material.SNOWBALL), ColorGradient.getGradientName("资源")));
        flat("material", "resourcedlc",
                new CustomItemStack(Utils.doGlow(Material.TOTEM_OF_UNDYING), ColorGradient.getGradientName("补丁材料")));

        // ===== 一级: 特殊物品 =====
        topContainer("special", "特殊物品",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.SHEARS),
                        ColorGradient.getGradientName("特殊物品"),
                        "点击查看特殊道具与联动分类", "special"), 2);
        flat("special", "specialitem",
                new CustomItemStack(Utils.doGlow(Material.SHEARS), ColorGradient.getGradientName("特殊道具")));
        flat("special", "honkai",
                new CustomItemStack(CustomHead.SILVER_WOLF.getItem(),
                        ColorGradient.getRandomGradientName("Honkai: Star Rail")));
        flat("special", "skyblock",
                new CustomItemStack(CustomHead.getHead("7948942fed672ded57f8cdb169a5076062586e77002ee30e07693c025e6f0db5"),
                        ColorGradient.getGradientName("魔法-SkyBlock")));
        flat("special", "commemorate",
                new CustomItemStack(Utils.doGlow(Material.FIREWORK_ROCKET), ColorGradient.getRandomGradientName("纪念品")));

        // ===== 一级: 特殊机器 =====
        topContainer("machine", "特殊机器",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.MAGENTA_GLAZED_TERRACOTTA),
                        ColorGradient.getGradientName("特殊机器"),
                        "点击查看机器分类", "machine"), 3);
        flat("machine", "quickmachine",
                new CustomItemStack(Utils.doGlow(Material.MAGENTA_GLAZED_TERRACOTTA), ColorGradient.getGradientName("快捷机器")));
        flat("machine", "energy",
                new CustomItemStack(Utils.doGlow(Material.SOUL_LANTERN), ColorGradient.getGradientName("特殊机器")));
        flat("machine", "power",
                new CustomItemStack(CustomHead.getHead("24378b986e358555ee73f09b210d49ec13719de5ea88d75523770d31163f3aef"),
                        ColorGradient.getGradientName("发电机")));
        flat("machine", "electric_bot",
                new CustomItemStack(CustomHead.BOT_PINK.getItem(), ColorGradient.getGradientName("电力机器人")));
        flat("machine", "rscmagic",
                new CustomItemStack(CustomHead.MAGICSOLO.getItem(),
                        ColorGradient.getRandomGradientName("魔法-1.21修复/优化~物品/机器")));

        // ===== 一级: 功能机器 =====
        topContainer("functional", "功能机器",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.FURNACE),
                        ColorGradient.getGradientName("功能机器"),
                        "点击查看生产机器与资源生成器", "functional"), 4);
        flat("functional", "resourcegenerator",
                new CustomItemStack(Utils.doGlow(Material.LANTERN), ColorGradient.getGradientName("资源生成器")));
        flat("functional", "recipemachine",
                new CustomItemStack(Utils.doGlow(Material.SOUL_CAMPFIRE), ColorGradient.getGradientName("生产机器")));

        // ===== 一级(平铺): 魔法BOSS =====
        flat(null, "boss",
                new CustomItemStack(Utils.doGlow(Material.ENDER_DRAGON_SPAWN_EGG), ColorGradient.getGradientName("魔法BOSS")));

        // ===== 一级: 预制菜 =====
        topContainer("prebuild", "预制菜",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.BRICKS),
                        ColorGradient.getGradientName("预制菜"),
                        "点击查看预制菜与建筑材料", "prebuild"), 7);
        flat("prebuild", "prebuilding",
                new CustomItemStack(Utils.doGlow(Material.BRICKS), ColorGradient.getGradientName("预制菜（建筑）")));
        flat("prebuild", "prebuildingresource",
                new CustomItemStack(Utils.doGlow(Material.PRISMARINE_CRYSTALS), ColorGradient.getGradientName("建筑材料")));

        // ===== 一级(平铺): 魔法锻造 =====
        flat(null, "forge",
                new CustomItemStack(Utils.doGlow(Material.ANVIL), ColorGradient.getGradientName("魔法锻造")));

        // ===== 一级: 钓鱼佬 =====
        topContainer("fishing", "钓鱼佬",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.FISHING_ROD),
                        ColorGradient.getGradientName("钓鱼佬"),
                        "点击查看织梦者与水云间", "fishing"), 9);
        // 二级容器: 织梦者
        container("fishing", "dreamer", "织梦者",
                new CustomItemStack(Utils.doGlow(Material.GHAST_TEAR), ColorGradient.getRandomGradientName("织梦者")));
        flat("dreamer", "nonsensical",
                new CustomItemStack(Utils.doGlow(Material.STRING), ColorGradient.getRandomGradientName("合成材料")));
        flat("dreamer", "dreamer_lure",
                new CustomItemStack(Utils.doGlow(Material.ORANGE_DYE), ColorGradient.getRandomGradientName("鱼饵")));
        flat("dreamer", "dreamer_rod",
                new CustomItemStack(Utils.doGlow(Material.FISHING_ROD), ColorGradient.getRandomGradientName("鱼竿")));
        flat("dreamer", "dreamer_guide",
                new CustomItemStack(Utils.doGlow(Material.BOOK), ColorGradient.getRandomGradientName("钓鱼指南")));
        // 二级容器: 水云间
        container("fishing", "watercloud", "水云间",
                new CustomItemStack(Utils.doGlow(Material.CYAN_DYE), ColorGradient.getGradientNameVer2("水云间")));
        flat("watercloud", "watercloud_material",
                new CustomItemStack(Utils.doGlow(Material.BAMBOO), ColorGradient.getGradientNameVer2("合成材料")));
        flat("watercloud", "watercloud_lure",
                new CustomItemStack(Utils.doGlow(Material.ORANGE_DYE), ColorGradient.getGradientNameVer2("鱼饵")));
        flat("watercloud", "watercloud_rod",
                new CustomItemStack(Utils.doGlow(Material.FISHING_ROD), ColorGradient.getGradientNameVer2("鱼竿")));
        flat("watercloud", "watercloud_guide",
                new CustomItemStack(Utils.doGlow(Material.KNOWLEDGE_BOOK), ColorGradient.getGradientNameVer2("钓鱼指南")));

        // ===== 一级: 魔法作物 =====
        topContainer("crop", "魔法作物",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.WHEAT),
                        ColorGradient.getGradientNameVer2("魔法作物"),
                        "点击查看美食与食材原料", "crop"), 10);
        flat("crop", "food",
                new CustomItemStack(Utils.doGlow(Material.CAKE), ColorGradient.getGradientNameVer2("美食")));
        flat("crop", "foodresource",
                new CustomItemStack(Utils.doGlow(Material.WHEAT), ColorGradient.getGradientNameVer2("食材原料")));

        // ===== 一级(平铺): 魔法2.0-共创 =====
        flat(null, "cooperatecreate",
                new CustomItemStack(CustomHead.getHead("7971e55df39a58faad05742d882e9a05ffa511a0c58e4f687777bb74614006eb"),
                        ColorGradient.getGradientNameVer2("魔法2.0-共创")));
    }

    /** 注册一级容器组(显示在一级主页) */
    private static SubItemGroup topContainer(String id, String name, ItemStack icon, int tier) {
        SubItemGroup group = container(null, id, name, icon, tier);
        TOP_LEVEL.add(group);
        TOP_LEVEL_CONTAINERS.add(group);
        return group;
    }

    /** 注册一级特殊容器组(不进一级主页, 如附属信息) */
    private static SubItemGroup specialContainer(String id, String name, ItemStack icon, int tier) {
        SubItemGroup group = container(null, id, name, icon, tier);
        TOP_LEVEL_CONTAINERS.add(group);
        return group;
    }

    /** 注册嵌套容器组(parentId 为父级容器 id, 点开进入下一级菜单页) */
    private static SubItemGroup container(String parentId, String id, String name, ItemStack icon) {
        return container(parentId, id, name, icon, 0);
    }

    private static SubItemGroup container(String parentId, String id, String name, ItemStack icon, int tier) {
        SubItemGroup group = createGroup(id, icon, tier);
        GROUPS.put(id, group);
        NAMES.put(id, name);
        VirtualGuideGroup anchor = new VirtualGuideGroup(
                new NamespacedKey(MagicExpansion.getInstance(), id + "_root"),
                icon,
                (p, pr, m) -> GuideCategoryMenu.openContainer(p, pr, m, id)
        );
        ANCHORS.put(id, anchor);
        if (parentId != null) {
            children(parentId).add(anchor);
        }
        return group;
    }

    /** 注册平铺组(parentId 为 null 时是一级平铺组, 直接进四级物品列表页) */
    private static void flat(String parentId, String id, ItemStack icon) {
        SubItemGroup group = createGroup(id, icon, 0);
        GROUPS.put(id, group);
        if (parentId == null) {
            TOP_LEVEL.add(group);
        } else {
            children(parentId).add(group);
            PARENT.put(id, parentId); // 记录父容器: 四级物品列表页确定性返回时反查上级
        }
    }

    private static SubItemGroup createGroup(String id, ItemStack icon, int tier) {
        return new SubItemGroup(new NamespacedKey(MagicExpansion.getInstance(), id), hiddenContainer, icon, tier);
    }

    private static List<ItemGroup> children(String parentId) {
        return CHILDREN.computeIfAbsent(parentId, k -> new ArrayList<>());
    }

    /** 按 id 取分组对象(供物品注册代码引用) */
    public static SubItemGroup get(String id) {
        return GROUPS.get(id);
    }

    /**
     * 反查平铺组(四级物品列表页)的父容器 id, 作为确定性返回目标。
     * 返回 null 表示该平铺组是一级平铺组(如魔法锻造), 上级即魔法2.0 一级菜单。
     */
    public static String getParentContainerId(String flatId) {
        return PARENT.get(flatId);
    }

    /** 取容器组的历史锚点 */
    public static VirtualGuideGroup getAnchor(String id) {
        return ANCHORS.get(id);
    }

    /** 一级主页分组顺序 */
    public static List<ItemGroup> getTopLevel() {
        return TOP_LEVEL;
    }

    /** 容器组的子分组列表 */
    public static List<ItemGroup> getChildren(String id) {
        return CHILDREN.getOrDefault(id, List.of());
    }

    /** 是否为容器组(有子级页面) */
    public static boolean isContainer(String id) {
        return ANCHORS.containsKey(id);
    }

    /** 为所有一级容器组注册原生占位物品(供原生粘液书页面显示与点击拦截) */
    public static void registerPlaceholders(MagicExpansion plugin) {
        for (SubItemGroup group : TOP_LEVEL_CONTAINERS) {
            String id = group.getKey().getKey();
            String name = NAMES.getOrDefault(id, id);
            String placeholderId = ("VIRTUAL_ENTRY_" + id).toUpperCase();
            SlimefunItemStack placeholder = new SlimefunItemStack(placeholderId,
                    new CustomItemStack(Material.PAPER,
                            ColorGradient.getGradientName(name + " · 入口"),
                            ColorGradient.getGradientName("点击查看子分类")));
            UnplaceableBlock entry = new UnplaceableBlock(group, placeholder, RecipeType.NULL,
                    new ItemStack[]{null, null, null, null, null, null, null, null, null});
            entry.register(plugin);
            // 搜索误命中修复: 占位物品显示名含组名(如"钓鱼佬 · 入口"), 会被"鱼"等关键词搜索命中,
            // 出现在搜索结果页后触发虚拟组监听器的 onOpen 兜底劫持, 导致界面跳转。
            // setHidden 使其从指南与搜索中排除(点击拦截由 onClick 主路径负责, 不受影响)
            entry.setHidden(true);
        }
    }

    /**
     * 判断界面标题是否为某容器组的原生物品列表页(标题 = 容器组显示名, 忽略颜色码)。
     * 供虚拟组点击监听器收窄 onOpen 劫持范围: 只劫持"原生打开容器组页面"的场景,
     * 排除 JEG 搜索结果页(标题为"你正在搜索: xxx")等无关界面,
     * 防止搜索结果中出现 VIRTUAL_ENTRY 占位物品(名字含组名, 可被搜索命中)时被误劫持跳转。
     */
    public static boolean isContainerGroupPageTitle(String title) {
        if (title == null) return false;
        String stripped = ChatColor.stripColor(title).trim();
        if (stripped.isEmpty()) return false;
        for (String name : NAMES.values()) {
            if (stripped.equals(ChatColor.stripColor(name).trim())) {
                return true;
            }
        }
        return false;
    }
}
