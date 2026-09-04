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
        specialContainer("attachmentinfo", "Addon Information",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.PAPER),
                        ColorGradient.getRandomGradientName("Addon Information"),
                        "Click to view credits and the changelog", "attachmentinfo"), 0);
        flat("attachmentinfo", "contribution",
                new CustomItemStack(Utils.doGlow(Material.COMMAND_BLOCK), ColorGradient.getGradientNameVer2("Credits")));
        flat("attachmentinfo", "updateinfo",
                new CustomItemStack(Utils.doGlow(Material.PAPER), ColorGradient.getRandomGradientName("Changelog")));

        // ===== 一级: 材料 =====
        topContainer("material", "Materials",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.SNOWBALL),
                        ColorGradient.getGradientName("Materials"),
                        "Click to view resources and patch materials", "material"), 1);
        flat("material", "resource",
                new CustomItemStack(Utils.doGlow(Material.SNOWBALL), ColorGradient.getGradientName("Resources")));
        flat("material", "resourcedlc",
                new CustomItemStack(Utils.doGlow(Material.TOTEM_OF_UNDYING), ColorGradient.getGradientName("Patch Materials")));

        // ===== 一级: 特殊物品 =====
        topContainer("special", "Special Items",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.SHEARS),
                        ColorGradient.getGradientName("Special Items"),
                        "Click to view special items and integrations", "special"), 2);
        flat("special", "specialitem",
                new CustomItemStack(Utils.doGlow(Material.SHEARS), ColorGradient.getGradientName("Special Tools")));
        flat("special", "honkai",
                new CustomItemStack(CustomHead.SILVER_WOLF.getItem(),
                        ColorGradient.getRandomGradientName("Honkai: Star Rail")));
        flat("special", "skyblock",
                new CustomItemStack(CustomHead.getHead("7948942fed672ded57f8cdb169a5076062586e77002ee30e07693c025e6f0db5"),
                        ColorGradient.getGradientName("Magic-SkyBlock")));
        flat("special", "commemorate",
                new CustomItemStack(Utils.doGlow(Material.FIREWORK_ROCKET), ColorGradient.getRandomGradientName("Souvenirs")));

        // ===== 一级: 特殊机器 =====
        topContainer("machine", "Special Machines",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.MAGENTA_GLAZED_TERRACOTTA),
                        ColorGradient.getGradientName("Special Machines"),
                        "Click to view machine categories", "machine"), 3);
        flat("machine", "quickmachine",
                new CustomItemStack(Utils.doGlow(Material.MAGENTA_GLAZED_TERRACOTTA), ColorGradient.getGradientName("Quick Machines")));
        flat("machine", "energy",
                new CustomItemStack(Utils.doGlow(Material.SOUL_LANTERN), ColorGradient.getGradientName("Special Machines")));
        flat("machine", "power",
                new CustomItemStack(CustomHead.getHead("24378b986e358555ee73f09b210d49ec13719de5ea88d75523770d31163f3aef"),
                        ColorGradient.getGradientName("Generators")));
        flat("machine", "electric_bot",
                new CustomItemStack(CustomHead.BOT_PINK.getItem(), ColorGradient.getGradientName("Electric Robots")));
        flat("machine", "rscmagic",
                new CustomItemStack(CustomHead.MAGICSOLO.getItem(),
                        ColorGradient.getRandomGradientName("Magic - 1.21 Fixes/Optimizations ~ Items/Machines")));

        // ===== 一级: 功能机器 =====
        topContainer("functional", "Functional Machines",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.FURNACE),
                        ColorGradient.getGradientName("Functional Machines"),
                        "Click to view production machines and resource generators", "functional"), 4);
        flat("functional", "resourcegenerator",
                new CustomItemStack(Utils.doGlow(Material.LANTERN), ColorGradient.getGradientName("Resource Generators")));
        flat("functional", "recipemachine",
                new CustomItemStack(Utils.doGlow(Material.SOUL_CAMPFIRE), ColorGradient.getGradientName("Production Machines")));

        // ===== 一级(平铺): 魔法BOSS =====
        flat(null, "boss",
                new CustomItemStack(Utils.doGlow(Material.ENDER_DRAGON_SPAWN_EGG), ColorGradient.getGradientName("Magic Bosses")));

        // ===== 一级: 预制菜 =====
        topContainer("prebuild", "Prefabs",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.BRICKS),
                        ColorGradient.getGradientName("Prefabs"),
                        "Click to view prefabs and building materials", "prebuild"), 7);
        flat("prebuild", "prebuilding",
                new CustomItemStack(Utils.doGlow(Material.BRICKS), ColorGradient.getGradientName("Prefabs (Building)")));
        flat("prebuild", "prebuildingresource",
                new CustomItemStack(Utils.doGlow(Material.PRISMARINE_CRYSTALS), ColorGradient.getGradientName("Building Materials")));

        // ===== 一级(平铺): 魔法锻造 =====
        flat(null, "forge",
                new CustomItemStack(Utils.doGlow(Material.ANVIL), ColorGradient.getGradientName("Magic Forging")));

        // ===== 一级: 钓鱼佬 =====
        topContainer("fishing", "Fishing",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.FISHING_ROD),
                        ColorGradient.getGradientName("Fishing"),
                        "Click to view Dreamweaver and Water Cloud", "fishing"), 9);
        // 二级容器: 织梦者
        container("fishing", "dreamer", "Dreamweaver",
                new CustomItemStack(Utils.doGlow(Material.GHAST_TEAR), ColorGradient.getRandomGradientName("Dreamweaver")));
        flat("dreamer", "nonsensical",
                new CustomItemStack(Utils.doGlow(Material.STRING), ColorGradient.getRandomGradientName("Crafting Materials")));
        flat("dreamer", "dreamer_lure",
                new CustomItemStack(Utils.doGlow(Material.ORANGE_DYE), ColorGradient.getRandomGradientName("Lures")));
        flat("dreamer", "dreamer_rod",
                new CustomItemStack(Utils.doGlow(Material.FISHING_ROD), ColorGradient.getRandomGradientName("Fishing Rods")));
        flat("dreamer", "dreamer_guide",
                new CustomItemStack(Utils.doGlow(Material.BOOK), ColorGradient.getRandomGradientName("Fishing Guide")));
        // 二级容器: 水云间
        container("fishing", "watercloud", "Water Cloud",
                new CustomItemStack(Utils.doGlow(Material.CYAN_DYE), ColorGradient.getGradientNameVer2("Water Cloud")));
        flat("watercloud", "watercloud_material",
                new CustomItemStack(Utils.doGlow(Material.BAMBOO), ColorGradient.getGradientNameVer2("Crafting Materials")));
        flat("watercloud", "watercloud_lure",
                new CustomItemStack(Utils.doGlow(Material.ORANGE_DYE), ColorGradient.getGradientNameVer2("Lures")));
        flat("watercloud", "watercloud_rod",
                new CustomItemStack(Utils.doGlow(Material.FISHING_ROD), ColorGradient.getGradientNameVer2("Fishing Rods")));
        flat("watercloud", "watercloud_guide",
                new CustomItemStack(Utils.doGlow(Material.KNOWLEDGE_BOOK), ColorGradient.getGradientNameVer2("Fishing Guide")));

        // ===== 一级: 魔法作物 =====
        topContainer("crop", "Magic Crops",
                GuideCategoryMenu.createVirtualIcon(Utils.doGlow(Material.WHEAT),
                        ColorGradient.getGradientNameVer2("Magic Crops"),
                        "Click to view foods and ingredients", "crop"), 10);
        flat("crop", "food",
                new CustomItemStack(Utils.doGlow(Material.CAKE), ColorGradient.getGradientNameVer2("Foods")));
        flat("crop", "foodresource",
                new CustomItemStack(Utils.doGlow(Material.WHEAT), ColorGradient.getGradientNameVer2("Ingredients")));

        // ===== 一级(平铺): 魔法2.0-共创 =====
        flat(null, "cooperatecreate",
                new CustomItemStack(CustomHead.getHead("7971e55df39a58faad05742d882e9a05ffa511a0c58e4f687777bb74614006eb"),
                        ColorGradient.getGradientNameVer2("Magic 2.0 - Community")));
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
                            ColorGradient.getGradientName(name + " · Entry"),
                            ColorGradient.getGradientName("Click to view subcategories")));
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
