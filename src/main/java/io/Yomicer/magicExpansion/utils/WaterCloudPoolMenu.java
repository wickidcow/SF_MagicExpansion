package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.Yomicer.magicExpansion.MagicExpansion.getInstance;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

/**
 * 水云间·鱼饵池菜单
 * 主页面列出鱼竿支持的鱼饵,点击某个鱼饵分页查看其掉落物与概率
 */
public class WaterCloudPoolMenu {

    public static final NamespacedKey POOL_BUTTON_KEY = new NamespacedKey(getInstance(), "watercloud_pool_button");
    public static final NamespacedKey POOL_ROD_KEY = new NamespacedKey(getInstance(), "watercloud_rod");

    /** 每个掉落物页展示的条目数(分页,4 行 × 7 列) */
    private static final int ITEMS_PER_PAGE = 28;

    private static final String[] BAIT_KEYS = {
            "CuiXia", "WeiChen", "RongHuo", "YueJin", "XingHe",                    // 青竹竿 5 饵
            "JianJia", "LuXue", "WeiLu", "BaiLu", "LuYa",                          // 芦花钓 5 饵
            "NingShuang", "LuoXu", "BingPo", "ChuJi", "ChuiLun",                   // 寒江雪 5 饵
            "FengSi", "YanYu", "LianBai", "XiaoFeng", "XieYing"                    // 细雨·斜风 5 饵
    };
    private static final String[] BAIT_NAMES = {
            "淬霞", "微尘", "熔火", "跃金", "星核",
            "蒹葭", "芦雪", "苇露", "白露", "芦芽",
            "凝霜", "落絮", "冰魄", "初霁", "垂纶",
            "风丝", "烟雨", "涟白", "晓风", "斜影"
    };
    private static final String[] BAIT_COLORS = {
            "§c", "§7", "§4", "§6", "§d",
            "§b", "§f", "§3", "§e", "§a",
            "§f", "§b", "§3", "§e", "§8",
            "§7", "§d", "§f", "§b", "§9"
    };
    private static final ItemStack[] BAIT_ICONS = {
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG,
            MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING
    };
    private static final ItemStack[] SPECIAL_ICONS = {
            MagicExpansionItems.REED_TASSEL,  // 淬霞特殊钓物 = 芦穗
            MagicExpansionItems.REED_TASSEL,  // 微尘特殊钓物 = 芦穗
            MagicExpansionItems.REED_TASSEL,  // 熔火特殊钓物 = 芦穗
            MagicExpansionItems.REED_TASSEL,  // 跃金特殊钓物 = 芦穗
            MagicExpansionItems.REED_TASSEL,  // 星核特殊钓物 = 芦穗
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU, // 蒹葭/芦雪/苇露/白露/芦芽特殊钓物(芦花钓统一=白芦羽)
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_BAILUYU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU, // 寒江雪各饵特殊钓物 = 雪魄珠
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_HANJIANG_XUEPOZHU,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN, // 细雨·斜风各饵特殊钓物 = 雨披针
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN,
            MagicExpansionItems.FISH_SPECIAL_ACTION_BETWEEN_WATER_CLOUD_XIYU_YUPIZHEN
    };

    private WaterCloudPoolMenu() {
    }

    /**
     * 创建指南配方页里的"查看鱼饵池"按钮(带 NBT 标记,用于监听器识别与定位)
     */
    public static ItemStack createPoolButton(FishingRodWaterCloud rod) {
        CustomItemStack btn = new CustomItemStack(Material.BOOK,
                getGradientName("✦ 查看鱼饵池 ✦"),
                getGradientName("点击查看当前鱼竿所有可用鱼饵"),
                getGradientName("以及每个鱼饵的掉落物概率"));
        ItemMeta meta = btn.getItemMeta();
        meta.getPersistentDataContainer().set(POOL_BUTTON_KEY, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(POOL_ROD_KEY, PersistentDataType.STRING, rod.getId());
        btn.setItemMeta(meta);
        return btn;
    }

    public static boolean isPoolButton(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(POOL_BUTTON_KEY, PersistentDataType.BOOLEAN);
    }

    public static String getButtonRodId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(POOL_ROD_KEY, PersistentDataType.STRING);
    }

    /**
     * 主页面:鱼饵选择
     */
    public static void openBaitList(Player player, FishingRodWaterCloud rod) {
        ChestMenu menu = new ChestMenu(getGradientName("✦ 水云间 · 鱼饵池 ✦"));
        setupBorders(menu, "§9✦ 水云间");
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(false);

        Map<String, List<WeightedItem>> table = rod.getLootTable();
        // 鱼饵选择槽位(4 行 × 7 列, 最多 28 个; 当前最多 20 饵, 单页足够)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIdx = 0;

        for (int i = 0; i < BAIT_KEYS.length && slotIdx < slots.length; i++) {
            // 只显示该鱼竿实际拥有的鱼饵池
            if (!table.containsKey(BAIT_KEYS[i])) continue;
            final int fi = i;
            int baitSlot = slots[slotIdx];
            slotIdx++;
            List<WeightedItem> pool = table.getOrDefault(BAIT_KEYS[i], List.of());
            int total = pool.stream().mapToInt(WeightedItem::getWeight).sum();
            WeightedItem special = pool.stream().filter(w -> isSpecialItem(w.getItem(), fi)).findFirst().orElse(null);
            int specialWeight = special == null ? 0 : special.getWeight();
            String specialName = special == null ? "§8暂无" : "§6" + ItemStackHelper.getDisplayName(special.getItem());

            ItemStack icon = BAIT_ICONS[i].clone();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(BAIT_COLORS[i] + "✦ " + BAIT_NAMES[i]);
            List<String> lore = new ArrayList<>();
            lore.add("§7可钓获的物品: §f" + pool.size() + "种");
            lore.add("§6 · 特殊物品概率: " + formatPercent(total, specialWeight));
            lore.add("");
            lore.add("§7" + specialName + " §7为特殊钓物");
            lore.add("");
            lore.add("§e点击查看掉落物概率");
            meta.setLore(lore);
            icon.setItemMeta(meta);

            menu.addItem(baitSlot, icon, (p, slot, item, action) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openPool(p, rod, BAIT_KEYS[fi], 0);
                return false;
            });
        }

        // 关闭按钮
        menu.addItem(49, new CustomItemStack(Material.BARRIER, "§c关闭菜单", "§7返回游戏"));
        menu.addMenuClickHandler(49, (p, slot, item, action) -> {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            p.closeInventory();
            return false;
        });

        menu.open(player);
    }

    /**
     * 掉落物详情页(分页)
     */
    public static void openPool(Player player, FishingRodWaterCloud rod, String key, int page) {
        int index = indexOfKey(key);
        if (index < 0) return;

        List<WeightedItem> pool = rod.getLootTable().getOrDefault(key, List.of());
        int total = pool.stream().mapToInt(WeightedItem::getWeight).sum();
        int pages = Math.max(1, (pool.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));
        final int currentPage = page;

        ChestMenu menu = new ChestMenu(getGradientName(BAIT_NAMES[index] + " · 掉落物"));
        setupBorders(menu, BAIT_COLORS[index] + "✦ " + BAIT_NAMES[index]);
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(false);

        // 顶部中间信息栏:使用所点击鱼饵的材质与名字
        ItemStack info = BAIT_ICONS[index].clone();
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(BAIT_COLORS[index] + "✦ " + BAIT_NAMES[index]);
        infoMeta.setLore(List.of("§6第 " + (currentPage + 1) + "/" + pages + " 页"));
        info.setItemMeta(infoMeta);
        menu.addItem(4, info);
        menu.addMenuClickHandler(4, (p, slot, item, action) -> false);

        // 掉落物条目(4 行 × 7 列,末尾不补玻璃板)
        int[] grid = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int start = currentPage * ITEMS_PER_PAGE;
        for (int i = 0; i < grid.length; i++) {
            int poolIndex = start + i;
            if (poolIndex < pool.size()) {
                WeightedItem w = pool.get(poolIndex);
                ItemStack display = buildPoolItem(w, total, index);
                menu.addItem(grid[i], display, (p, slot, item, action) -> false);
            }
        }

        // 底部:上一页 / 下一页 / 返回(45 号槽保持装饰栏)
        if (currentPage > 0) {
            menu.addItem(48, new CustomItemStack(Material.ARROW, "§e上一页", "§7第 " + currentPage + "/" + pages + " 页"));
            menu.addMenuClickHandler(48, (p, slot, item, action) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openPool(p, rod, key, currentPage - 1);
                return false;
            });
        }
        if (currentPage + 1 < pages) {
            menu.addItem(50, new CustomItemStack(Material.ARROW, "§e下一页", "§7第 " + (currentPage + 2) + "/" + pages + " 页"));
            menu.addMenuClickHandler(50, (p, slot, item, action) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openPool(p, rod, key, currentPage + 1);
                return false;
            });
        }

        menu.addItem(49, new CustomItemStack(Material.OAK_DOOR, "§b返回鱼饵列表"));
        menu.addMenuClickHandler(49, (p, slot, item, action) -> {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openBaitList(p, rod);
            return false;
        });

        menu.open(player);
    }

    private static ItemStack buildPoolItem(WeightedItem w, int total, int baitIndex) {
        String name = ItemStackHelper.getDisplayName(w.getItem());
        String displayName;
        if (isSpecialItem(w.getItem(), baitIndex)) {
            displayName = "§6🌟 " + name;
        } else {
            displayName = "§7" + name;
        }
        ItemStack display = w.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(List.of("§e掉落概率: §f" + formatPercent(total, w.getWeight())));
        display.setItemMeta(meta);
        return display;
    }

    private static boolean isSpecialItem(ItemStack item, int baitIndex) {
        return item != null && SlimefunUtils.isItemSimilar(item, SPECIAL_ICONS[baitIndex], true);
    }

    private static String formatPercent(int total, int part) {
        if (total <= 0) return "0.00%";
        return String.format("%.2f%%", part * 100.0 / total);
    }

    private static int indexOfKey(String key) {
        for (int i = 0; i < BAIT_KEYS.length; i++) {
            if (BAIT_KEYS[i].equals(key)) return i;
        }
        return -1;
    }

    private static void setupBorders(ChestMenu menu, String sideLabel) {
        menu.addItem(0, new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§9✦"));
        menu.addMenuClickHandler(0, (p, slot, item, action) -> false);
        menu.addItem(8, new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§9✦"));
        menu.addMenuClickHandler(8, (p, slot, item, action) -> false);
        menu.addItem(45, new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§9✦"));
        menu.addMenuClickHandler(45, (p, slot, item, action) -> false);
        menu.addItem(53, new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§9✦"));
        menu.addMenuClickHandler(53, (p, slot, item, action) -> false);

        for (int i = 1; i < 8; i++) {
            menu.addItem(i, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, sideLabel));
            menu.addMenuClickHandler(i, (p, slot, item, action) -> false);
        }
        for (int i = 46; i < 53; i++) {
            menu.addItem(i, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, sideLabel));
            menu.addMenuClickHandler(i, (p, slot, item, action) -> false);
        }
        int[] left = {9, 18, 27, 36};
        for (int slot : left) {
            menu.addItem(slot, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, "§9🎣"));
            menu.addMenuClickHandler(slot, (p, s, item, action) -> false);
        }
        int[] right = {17, 26, 35, 44};
        for (int slot : right) {
            menu.addItem(slot, new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, "§9🐟"));
            menu.addMenuClickHandler(slot, (p, s, item, action) -> false);
        }
    }
}
