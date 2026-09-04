package io.Yomicer.magicExpansion.items.misc.baitbag;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.CargoFragment;
import io.Yomicer.magicExpansion.items.tools.VoidTouch;
import io.Yomicer.magicExpansion.utils.NetworkStorage;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_REED;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemToBase64;

/**
 * 饵料袋菜单: 固定槽位存储Dreamweaver/Between Water and Clouds/Memory Fragment鱼饵, 支持优先级与外部存储输入
 */
public final class BaitBagMenu {

    /** 鱼饵注册表(id / 钓鱼逻辑 key / 系列 / 展示物品) */
    public record BaitEntry(String id, String key, String series, ItemStack item) {
    }

    private static final List<BaitEntry> BAITS = List.of(
            new BaitEntry("fishlurebasic", "fishLureBasic", "Dreamweaver", MagicExpansionItems.FISH_LURE_BASIC),
            new BaitEntry("fishluredust", "fishLureDust", "Dreamweaver", MagicExpansionItems.FISH_LURE_DUST),
            new BaitEntry("fishlureore", "fishLureOre", "Dreamweaver", MagicExpansionItems.FISH_LURE_ORE),
            new BaitEntry("fishlurealloy", "fishLureAlloyIngot", "Dreamweaver", MagicExpansionItems.FISH_LURE_ALLOY_INGOT),
            new BaitEntry("cuixia", "CuiXia", "Between Water and Clouds", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA),
            new BaitEntry("weichen", "WeiChen", "Between Water and Clouds", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN),
            new BaitEntry("ronghuo", "RongHuo", "Between Water and Clouds", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO),
            new BaitEntry("yuejin", "YueJin", "Between Water and Clouds", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN),
            new BaitEntry("xinghe", "XingHe", "Between Water and Clouds", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE),
            new BaitEntry("memory", "fishLureFinal", "Memory Fragment", memoryFragment()),
            new BaitEntry("jianjia", "JianJia", "Reedflower", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_JIANJIA),
            new BaitEntry("luxue", "LuXue", "Reedflower", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUXUE),
            new BaitEntry("weilu", "WeiLu", "Reedflower", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_WEILU),
            new BaitEntry("bailu", "BaiLu", "Reedflower", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_BAILU),
            new BaitEntry("luya", "LuYa", "Reedflower", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_REED_LUYA),
            new BaitEntry("ningshuang", "NingShuang", "Snowy River", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_NINGSHUANG),
            new BaitEntry("luoxu", "LuoXu", "Snowy River", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_LUOXU),
            new BaitEntry("bingpo", "BingPo", "Snowy River", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_BINGPO),
            new BaitEntry("chuji", "ChuJi", "Snowy River", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUJI),
            new BaitEntry("chuilun", "ChuiLun", "Snowy River", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_HANJIANG_CHUILUN),
            new BaitEntry("fengsi", "FengSi", "Fine Rain · Slanting Wind", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_FENGSI),
            new BaitEntry("yanyu", "YanYu", "Fine Rain · Slanting Wind", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_YANYU),
            new BaitEntry("lianbai", "LianBai", "Fine Rain · Slanting Wind", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_LIANBAI),
            new BaitEntry("xiaofeng", "XiaoFeng", "Fine Rain · Slanting Wind", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIAOFENG),
            new BaitEntry("xieying", "XieYing", "Fine Rain · Slanting Wind", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XIYU_XIEYING),
            new BaitEntry("magicsugar", "magic_sugar", "Dreamweaver", SlimefunItems.MAGIC_SUGAR)
    );

    // ==================== 4×7 布局数据模型(系列 → 鱼竿 → 鱼饵, 数据驱动, 新增鱼竿在此登记即可自动排版) ====================

    /** 鱼竿布局: 展示名 / 鱼竿图标 / 该竿消耗的鱼饵 key 列表 */
    private record RodLayout(String rodName, ItemStack rodIcon, List<String> lureKeys) {
    }

    /** 系列布局: 系列名 / 系列标识图标 / 该系列下的鱼竿列表 */
    private record SeriesLayout(String seriesName, Material seriesIcon, List<RodLayout> rods) {
    }

    /** 中间 4×7 存储区每行起始槽位 */
    private static final int[] GRID_ROWS = {10, 19, 28, 37};

    // ==================== 分页辅助(先铺行 → 按行分页 → 跨页重放系列头) ====================

    /** 每页行数 = 4 行 */
    private static final int PAGE_ROWS = GRID_ROWS.length;

    /** 当前页 鱼饵 → 槽位 映射(翻页/刷新时重建) */
    private static final Map<BaitEntry, Integer> PAGE_SLOTS = new java.util.LinkedHashMap<>();

    /** 渲染单元格类型 */
    private enum CellType { SERIES, ROD, BAIT }

    /** 一个占据单格的渲染单元(系列头 / 鱼竿展示 / 鱼饵) */
    private record GridCell(CellType type, String seriesName, Material seriesIcon,
                            RodLayout rod, BaitEntry bait) {
        static GridCell series(String name, Material icon) {
            return new GridCell(CellType.SERIES, name, icon, null, null);
        }
        static GridCell rod(RodLayout rod) {
            return new GridCell(CellType.ROD, null, null, rod, null);
        }
        static GridCell bait(BaitEntry bait) {
            return new GridCell(CellType.BAIT, null, null, null, bait);
        }
    }

    /** 一行(7 列): 行首可有系列头锚点, 其余为"该竿 + 其饵" */
    private record Row(GridCell seriesAnchor, List<GridCell> cells) {
    }

    /** 把 LAYOUT 铺成行: 每个系列从"系列头行"开始, 该系列每一根鱼竿都独占一行(行首=系列头(仅首竿)或空) */
    private static List<Row> buildRows() {
        List<Row> rows = new ArrayList<>();
        for (SeriesLayout series : LAYOUT) {
            GridCell anchor = GridCell.series(series.seriesName(), series.seriesIcon());
            boolean firstRod = true;
            for (RodLayout rod : series.rods()) {
                // 每根竿单独一行: 该系列首竿行首放系列头, 后续竿行首留空
                Row row = new Row(firstRod ? anchor : null, new ArrayList<>());
                row.cells().add(GridCell.rod(rod));
                for (String key : rod.lureKeys()) {
                    BaitEntry entry = findEntryByKey(key);
                    if (entry != null) {
                        row.cells().add(GridCell.bait(entry));
                    }
                }
                rows.add(row);
                firstRod = false;
            }
        }
        return rows;
    }

    /** 把铺好的行按每页 PAGE_ROWS 行直接切页(不跨行重放, 每竿一行天然整洁) */
    private static List<List<Row>> buildPages() {
        List<Row> rows = ROWS;
        List<List<Row>> pages = new ArrayList<>();
        int idx = 0;
        while (idx < rows.size()) {
            List<Row> page = new ArrayList<>();
            int count = 0;
            while (idx < rows.size() && count < PAGE_ROWS) {
                page.add(rows.get(idx));
                count++;
                idx++;
            }
            pages.add(page);
        }
        if (pages.isEmpty()) {
            pages.add(List.of());
        }
        return pages;
    }

    /** 系列 → 鱼竿 → 鱼饵 布局(新增鱼竿按此处登记, 自动排版) */
    private static final List<SeriesLayout> LAYOUT = List.of(
            new SeriesLayout("Dreamweaver", Material.GHAST_TEAR, List.of(
                    new RodLayout("Starter Rod / Wind Speaker Rod", MagicExpansionItems.FISHING_ROD_WIND_SPEAKER,
                            List.of("fishLureBasic", "fishLureDust", "fishLureOre", "fishLureAlloyIngot", "magic_sugar")),
                    new RodLayout("Entangled Knot: Final Thread · Paradox Hook", MagicExpansionItems.FISHING_ROD_FINAL_STICK,
                            List.of("fishLureFinal"))
            )),
            new SeriesLayout("Between Water and Clouds", Material.CYAN_DYE, List.of(
                    new RodLayout("Cyan Bamboo Rod", MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_CYAN_BAMBOO,
                            List.of("CuiXia", "WeiChen", "RongHuo", "YueJin", "XingHe")),
                    new RodLayout("Reedflower Rod", FISHING_ROD_BETWEEN_WATER_CLOUD_REED,
                            List.of("JianJia", "LuXue", "WeiLu", "BaiLu", "LuYa")),
                    new RodLayout("Snowy River", MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_HANJIANG,
                            List.of("NingShuang", "LuoXu", "BingPo", "ChuJi", "ChuiLun")),
                    new RodLayout("Fine Rain · Slanting Wind", MagicExpansionItems.FISHING_ROD_BETWEEN_WATER_CLOUD_XIYU,
                            List.of("FengSi", "YanYu", "LianBai", "XiaoFeng", "XieYing"))
            ))
    );

    /** 铺好的行(在 LAYOUT 之后初始化以保证可见) */
    private static final List<Row> ROWS = buildRows();

    /** 按行分页得到的页列表 */
    private static final List<List<Row>> PAGES = buildPages();

    /** 总页数 */
    private static int pageCount() {
        return PAGES.size();
    }

    /** 鱼饵 → 存储槽位 映射(静态构建, 与渲染共用) */
    private static final Map<BaitEntry, Integer> ENTRY_SLOTS = new java.util.LinkedHashMap<>();

    static {
        int row = 0;
        int col = 0;
        for (SeriesLayout series : LAYOUT) {
            if (col != 0) {
                row++;
                col = 0;
            }
            col = 1; // 槽0 = 系列标识格
            for (RodLayout rod : series.rods()) {
                if (col > 6) {
                    // 本行已满: 换行, 行首为装饰格(系列标识不重复)
                    row++;
                    col = 1;
                }
                // 修复(L2)：边界保护 —— 行号溢出时放在最后一行而不是抛数组越界异常
                if (row >= GRID_ROWS.length) row = GRID_ROWS.length - 1;
                col++; // 鱼竿展示格占 1 格
                for (String key : rod.lureKeys()) {
                    if (col > 6) {
                        row++;
                        col = 1;
                        // 修复(L2)：边界保护 —— 行号溢出时放在最后一行
                        if (row >= GRID_ROWS.length) row = GRID_ROWS.length - 1;
                    }
                    BaitEntry entry = findEntryByKey(key);
                    if (entry != null) {
                        ENTRY_SLOTS.put(entry, GRID_ROWS[row] + col);
                    }
                    col++;
                }
            }
            row++;
            col = 0;
        }
    }

    private static BaitEntry findEntryByKey(String key) {
        for (BaitEntry entry : BAITS) {
            if (entry.key().equals(key)) return entry;
        }
        return null;
    }

    /** 旧固定槽位布局已由 LAYOUT 数据驱动取代(保留常量以兼容遗留代码) */
    private static final int INPUT_SLOT = 49;

    // 以太秘匣内部数据 key(与 CargoFragment 一致)
    private static final NamespacedKey KEY_FRAGMENT_ITEM = new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json");
    private static final NamespacedKey KEY_FRAGMENT_AMOUNT = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
    // 虚空之触绑定坐标 key(与生态缸一致)
    private static final NamespacedKey KEY_X = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
    private static final NamespacedKey KEY_Y = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
    private static final NamespacedKey KEY_Z = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
    private static final NamespacedKey KEY_WORLD = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");
    private static final NamespacedKey KEY_INPUT_ITEM = new NamespacedKey(MagicExpansion.getInstance(), "baitbag_input_item");
    private static final NamespacedKey KEY_BAG_ID = new NamespacedKey(MagicExpansion.getInstance(), "baitbag_id");
    private static final String QUANTUM_STORAGE_BLOCK_CLASS =
            "io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage";

    private BaitBagMenu() {
    }

    // ==================== Memory Fragment(直接沿用钓鱼池里的生成方式) ====================

    private static ItemStack memoryFragment() {
        return new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD),
                getGradientNameVer2("Lure · Memory Fragment"),
                "§fThis lure can catch almost anything.",
                "§fIt exists somewhere in the past—or the future.",
                "§fWhat you see now may not be its true form.");
    }

    // ==================== PDC 存储 ====================

    private static NamespacedKey amountKey(String id) {
        return new NamespacedKey(MagicExpansion.getInstance(), "baitbag_" + id + "_amount");
    }

    private static NamespacedKey priorityKey(String id) {
        return new NamespacedKey(MagicExpansion.getInstance(), "baitbag_" + id + "_priority");
    }

    /** 读取数量(long 存储, 兼容旧版 int 数据并自动迁移) */
    public static long getAmount(ItemStack bag, BaitEntry entry) {
        if (bag == null || !bag.hasItemMeta()) return 0;
        PersistentDataContainer c = bag.getItemMeta().getPersistentDataContainer();
        Long v = c.get(amountKey(entry.id()), PersistentDataType.LONG);
        if (v != null) return v;
        Integer old = c.get(amountKey(entry.id()), PersistentDataType.INTEGER);
        return old == null ? 0 : old;
    }

    private static void setAmount(ItemStack bag, BaitEntry entry, long amount) {
        if (amount < 0) amount = 0; // 溢出保护: 禁止负数
        ItemMeta meta = bag.getItemMeta();
        meta.getPersistentDataContainer().set(amountKey(entry.id()), PersistentDataType.LONG, amount);
        bag.setItemMeta(meta);
    }

    private static int getPriority(ItemStack bag, BaitEntry entry) {
        if (bag == null || !bag.hasItemMeta()) return defaultPriority(entry);
        Integer v = bag.getItemMeta().getPersistentDataContainer().get(priorityKey(entry.id()), PersistentDataType.INTEGER);
        return v == null ? defaultPriority(entry) : v;
    }

    /** 默认优先级: 同系列内从左到右依次 1~X */
    private static int defaultPriority(BaitEntry entry) {
        int order = 0;
        for (BaitEntry other : BAITS) {
            if (!other.series().equals(entry.series())) continue;
            order++;
            if (other == entry) return order;
        }
        return 1;
    }

    private static void setPriority(ItemStack bag, BaitEntry entry, int priority) {
        ItemMeta meta = bag.getItemMeta();
        meta.getPersistentDataContainer().set(priorityKey(entry.id()), PersistentDataType.INTEGER, priority);
        bag.setItemMeta(meta);
    }

    /** 该系列在鱼饵注册表(BAITS)中的条目数, 用作优先级分母(动态统计, 新增系列自动适配) */
    private static int seriesSize(String series) {
        int size = 0;
        for (BaitEntry entry : BAITS) {
            if (entry.series().equals(series)) {
                size++;
            }
        }
        return Math.max(1, size);
    }

    // ==================== 袋子定位 ====================

    public static boolean isBaitBag(ItemStack item) {
        return item != null && SlimefunItem.getByItem(item) instanceof BaitBag;
    }

    private static int findBagSlot(Player player, ItemStack target) {
        PlayerInventory inv = player.getInventory();
        if (target != null) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack it = inv.getItem(i);
                if (it != null && SlimefunUtils.isItemSimilar(it, target, true)) return i;
            }
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (isBaitBag(inv.getItem(i))) return i;
        }
        return -1;
    }

    private static int findBagSlotById(Player player, String id) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (it != null && id.equals(getBagId(it))) return i;
        }
        return -1;
    }

    private static int findFreshBagSlot(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (isBaitBag(it) && getBagId(it) == null) return i;
        }
        return -1;
    }

    private static String getBagId(ItemStack bag) {
        if (bag == null || !bag.hasItemMeta()) return null;
        return bag.getItemMeta().getPersistentDataContainer().get(KEY_BAG_ID, PersistentDataType.STRING);
    }

    private static void setBagId(ItemStack bag, String id) {
        ItemMeta meta = bag.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_BAG_ID, PersistentDataType.STRING, id);
        bag.setItemMeta(meta);
    }

    // ==================== 钓鱼消耗(供监听器调用) ====================

    /**
     * 从玩家背包的饵料袋中按优先级消耗 1 个可用鱼饵, 返回被消耗鱼饵的 key(数值越小越先消耗)
     *
     * @return 被消耗鱼饵的 key(如 CuiXia / fishLureBasic / fishLureFinal), 无袋或无可用鱼饵时返回 null
     */
    public static String tryConsumeFromBag(Player player, Set<String> supportedKeys) {
        // 主手 → 副手 → 快捷栏(左到右) → 背包(上到下,左到右), 只消耗第一个有可用鱼饵的袋子
        for (int slot : bagSlotsInOrder(player)) {
            ItemStack bag = player.getInventory().getItem(slot);
            if (!isBaitBag(bag)) continue;

            List<BaitEntry> candidates = new ArrayList<>();
            for (BaitEntry entry : BAITS) {
                if (!supportedKeys.contains(entry.key())) continue;
                if (getAmount(bag, entry) > 0) candidates.add(entry);
            }
            if (candidates.isEmpty()) continue;

            candidates.sort((a, b) -> {
                int pa = getPriority(bag, a);
                int pb = getPriority(bag, b);
                if (pa != pb) return Integer.compare(pa, pb); // 优先级数值小的先消耗
                return Integer.compare(BAITS.indexOf(a), BAITS.indexOf(b)); // 同级按 Dreamweaver→Between Water and Clouds→Memory Fragment
            });

            BaitEntry chosen = candidates.get(0);
            setAmount(bag, chosen, getAmount(bag, chosen) - 1);
            player.getInventory().setItem(slot, bag);
            return chosen.key();
        }
        return null;
    }

    /**
     * 只读锁定: 按优先级探测袋中第一个可用鱼饵的 key(不消耗, 供中鱼结算前锁定鱼获池)
     *
     * @return 可用鱼饵的 key(如 CuiXia / fishLureBasic), 无袋或无可用鱼饵时返回 null
     */
    public static String peekFromBag(Player player, Set<String> supportedKeys) {
        for (int slot : bagSlotsInOrder(player)) {
            ItemStack bag = player.getInventory().getItem(slot);
            if (!isBaitBag(bag)) continue;

            List<BaitEntry> candidates = new ArrayList<>();
            for (BaitEntry entry : BAITS) {
                if (!supportedKeys.contains(entry.key())) continue;
                if (getAmount(bag, entry) > 0) candidates.add(entry);
            }
            if (candidates.isEmpty()) continue;

            candidates.sort((a, b) -> {
                int pa = getPriority(bag, a);
                int pb = getPriority(bag, b);
                if (pa != pb) return Integer.compare(pa, pb); // 优先级数值小的先消耗
                return Integer.compare(BAITS.indexOf(a), BAITS.indexOf(b)); // 同级按 Dreamweaver→Between Water and Clouds→Memory Fragment
            });
            return candidates.get(0).key();
        }
        return null;
    }

    /**
     * 按指定 key 从袋中消耗 1 个鱼饵(袋子已丢失或袋中无该鱼饵时不消耗)
     *
     * @return 是否成功消耗
     */
    public static boolean consumeFromBagByKey(Player player, String key) {
        for (int slot : bagSlotsInOrder(player)) {
            ItemStack bag = player.getInventory().getItem(slot);
            if (!isBaitBag(bag)) continue;
            for (BaitEntry entry : BAITS) {
                if (!entry.key().equals(key)) continue;
                long amount = getAmount(bag, entry);
                if (amount <= 0) continue;
                setAmount(bag, entry, amount - 1);
                player.getInventory().setItem(slot, bag);
                return true;
            }
        }
        return false;
    }

    /** 主手 → 副手 → 快捷栏 → 背包 的槽位顺序 */
    private static List<Integer> bagSlotsInOrder(Player player) {
        PlayerInventory inv = player.getInventory();
        List<Integer> slots = new ArrayList<>();
        int held = inv.getHeldItemSlot();
        slots.add(held);
        slots.add(40);
        for (int i = 0; i < 9; i++) {
            if (i != held) slots.add(i);
        }
        for (int i = 9; i < 36; i++) {
            slots.add(i);
        }
        return slots;
    }

    // ==================== 菜单 ====================

    public static void open(Player player, ItemStack bag) {
        openPage(player, bag, 0);
    }

    private static void openPage(Player player, ItemStack bag, int page) {
        String bagId = getBagId(bag);
        int slot = bagId != null ? findBagSlotById(player, bagId) : -1;
        if (slot < 0) {
            // 新袋子: 定位第一个还没有唯一ID的云梦袋(即被点击的袋子)
            slot = findFreshBagSlot(player);
        }
        if (slot < 0) {
            slot = findBagSlot(player, bag); // 兜底
        }
        if (slot < 0) {
            player.sendMessage(getGradientNameVer2("The Lure Bag is not in your inventory."));
            return;
        }
        ItemStack current = player.getInventory().getItem(slot);
        if (current == null) return;
        if (bagId == null) {
            // 首次打开: 写入唯一ID, 之后按ID精确定位, 避免与其它云梦袋混淆
            setBagId(current, UUID.randomUUID().toString());
            player.getInventory().setItem(slot, current);
        }
        final int bagSlot = slot;

        ChestMenu menu = new ChestMenu(getGradientNameVer2("✦ Lure Bag ✦"));
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(true);
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f));

        // 白色填充(外围装饰, 不含中间 4×7 存储区)
        for (int s : new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53}) {
            menu.addItem(s, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        // 粉红输入槽(命名)
        for (int s : new int[]{47, 48, 50, 51}) {
            menu.addItem(s, new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, getGradientNameVer2("Input Slot")), ChestMenuUtils.getEmptyClickHandler());
        }
        // 中间 4×7 存储区: 先铺淡蓝装饰, 再由 renderGrid 覆盖(系列标识/鱼竿展示/鱼饵存储)
        for (int s : new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43}) {
            menu.addItem(s, plainPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        renderGrid(menu, current, bagSlot, page);

        // 4槽: 使用说明
        menu.addItem(4, new CustomItemStack(Material.BOOK,
                getGradientNameVer2("Cloud Dream Bag"),
                getGradientNameVer2("Left-click to insert the matching lure"),
                getGradientNameVer2("Left-click with an empty hand to take 1"),
                getGradientNameVer2("Shift + Left-click to take all (max 3456)"),
                getGradientNameVer2("Right-click to cycle priority")), ChestMenuUtils.getEmptyClickHandler());

        // 系列标签与鱼饵槽由 renderGrid 统一渲染(数据驱动)

        // 翻页槽(46=上一页 / 52=下一页): 有页可翻=黄绿玻璃板, 无页=白色玻璃板(沿用指南菜单模板)
        int totalPages = Math.max(1, pageCount());
        boolean hasPrev = page > 0;
        boolean hasNext = page < totalPages - 1;
        if (hasPrev) {
            menu.addItem(46, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(46, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openPage(p, bag, page - 1);
                return false;
            });
        } else {
            menu.addItem(46, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        if (hasNext) {
            menu.addItem(52, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(52, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openPage(p, bag, page + 1);
                return false;
            });
        } else {
            menu.addItem(52, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 输入口: 默认空槽, 满足条件才执行存入
        ItemStack inputItem = getInputItem(current);
        if (inputItem != null) {
            menu.addItem(INPUT_SLOT, inputItem, ChestMenuUtils.getEmptyClickHandler());
        }
        menu.addMenuClickHandler(INPUT_SLOT, (p, s, it, a) -> {
            handleInput(p, menu, current, bagSlot, a);
            return false;
        });
        // Shift+左键点击背包物品: 快速放入输入槽(输入槽为空时)
        menu.addPlayerInventoryClickHandler((p, s, it, a) -> {
            if (!a.isShiftClicked() || it == null || it.getType().isAir()) return true;
            if (getInputItem(current) != null) return true;
            fastInputFromInventory(p, menu, current, bagSlot, it, s);
            return false;
        });

        // 鱼饵槽: 由 renderGrid 按布局注册(数据驱动)

        menu.open(player);
    }

    /**
     * 渲染中间 4×7 存储区的某一页(按行渲染, 页首保系列头)。
     * 每页最多 PAGE_ROWS 行; 翻页通过 46/52 槽切换。刷新时重建当前页 鱼饵→槽位 映射。
     */
    private static void renderGrid(ChestMenu menu, ItemStack bag, int bagSlot, int page) {
        if (page < 0 || page >= PAGES.size()) {
            return;
        }

        // 重置当前页 鱼饵→槽位 映射(供刷新定位)
        PAGE_SLOTS.clear();

        List<Row> pageRows = PAGES.get(page);
        int rowIdx = 0;
        for (Row rw : pageRows) {
            int row = rowIdx;
            if (row >= GRID_ROWS.length) {
                // 防御: 行数超 4 行时丢弃剩余(理论不会发生)
                break;
            }
            int col = 0;
            if (rw.seriesAnchor() != null) {
                GridCell anchor = rw.seriesAnchor();
                menu.addItem(GRID_ROWS[row], new CustomItemStack(anchor.seriesIcon(),
                        getGradientNameVer2(anchor.seriesName()),
                        getGradientNameVer2("Priority 1~" + seriesSize(anchor.seriesName()))), ChestMenuUtils.getEmptyClickHandler());
            } else {
                // 行首无系列头: 放一个空占位格(与系列头顶格对齐, 用存储区同款淡蓝色玻璃板填充)
                menu.addItem(GRID_ROWS[row], plainPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
            }
            col = 1;
            for (GridCell cell : rw.cells()) {
                if (cell.type() == CellType.ROD) {
                    menu.addItem(GRID_ROWS[row] + col, rodDisplay(cell.rod()), ChestMenuUtils.getEmptyClickHandler());
                } else if (cell.type() == CellType.BAIT) {
                    BaitEntry e = cell.bait();
                    int slot = GRID_ROWS[row] + col;
                    PAGE_SLOTS.put(e, slot);
                    menu.addItem(slot, baitIcon(bag, e), ChestMenuUtils.getEmptyClickHandler());
                    menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                        handleBaitClick(p, menu, bag, bagSlot, e, a);
                        return false;
                    });
                }
                col++;
            }
            rowIdx++;
        }
    }

    /** 鱼竿展示格: 竿材质 + 发光 + 展示名 + 说明(告诉玩家该竿消耗哪些鱼饵) */
    private static ItemStack rodDisplay(RodLayout rod) {
        ItemStack icon = rod.rodIcon().clone();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            // 发光: 按 key 运行时解析(兼容 1.20.4 DURABILITY 与 1.21 UNBREAKING)
            Enchantment glow = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            if (glow != null) {
                meta.addEnchant(glow, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.setDisplayName(getGradientNameVer2(rod.rodName()));
            meta.setLore(List.of(getGradientNameVer2("This rod uses the following lures")));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private static ItemStack plainPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private static ItemStack baitIcon(ItemStack bag, BaitEntry entry) {
        long amount = getAmount(bag, entry);
        int priority = getPriority(bag, entry);
        ItemStack icon = entry.item().clone();
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasLore()) lore.addAll(meta.getLore());
        lore.add("");
        lore.add(getGradientNameVer2("Stored: " + amount));
        lore.add(getGradientNameVer2("Priority: " + priority + " / " + seriesSize(entry.series())));
        lore.add(getGradientNameVer2("Empty-hand Left-click: take 1 · Left-click: insert · Shift: take all (≤3456) · Right-click: priority"));
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private static void handleBaitClick(Player player, ChestMenu menu, ItemStack bag, int bagSlot, BaitEntry entry, me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction action) {
        if (action.isShiftClicked()) {
            withdraw(player, menu, bag, bagSlot, entry);
        } else if (action.isRightClicked()) {
            cyclePriority(player, menu, bag, bagSlot, entry);
        } else {
            depositOrTake(player, menu, bag, bagSlot, entry);
        }
    }

    /** 右键循环调整优先级, 同系列内保证不重复(被占用的值与被点击项互换) */
    private static void cyclePriority(Player player, ChestMenu menu, ItemStack bag, int bagSlot, BaitEntry entry) {
        int size = seriesSize(entry.series());
        int current = getPriority(bag, entry);
        int next = current % size + 1;
        for (BaitEntry other : BAITS) {
            if (other == entry || !other.series().equals(entry.series())) continue;
            if (getPriority(bag, other) == next) {
                setPriority(bag, other, current);
                // 修复: 被互换方优先级已变, 同步刷新其槽位图标
                refreshBaitSlot(menu, bag, other);
                break;
            }
        }
        setPriority(bag, entry, next);
        player.getInventory().setItem(bagSlot, bag);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        refreshBaitSlot(menu, bag, entry);
    }

    private static void depositOrTake(Player player, ChestMenu menu, ItemStack bag, int bagSlot, BaitEntry entry) {
        ItemStack cursor = player.getItemOnCursor();
        // 空手左键: 取出 1 个到光标
        if (cursor == null || cursor.getType().isAir()) {
            long have = getAmount(bag, entry);
            if (have <= 0) return;
            setAmount(bag, entry, have - 1);
            ItemStack one = entry.item().clone();
            one.setAmount(1);
            player.setItemOnCursor(one);
            player.getInventory().setItem(bagSlot, bag);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
            refreshBaitSlot(menu, bag, entry);
            return;
        }
        BaitEntry match = matchBait(cursor);
        if (match == null) {
            player.sendMessage(getGradientNameVer2("Hold the matching lure and click the slot."));
            return;
        }
        if (!match.id().equals(entry.id())) {
            player.sendMessage(getGradientNameVer2("That lure does not belong in this slot."));
            return;
        }
        long cur = getAmount(bag, entry);
        long add = cursor.getAmount();
        long nv = cur + add;
        if (nv < cur) nv = Long.MAX_VALUE; // 溢出保护
        setAmount(bag, entry, nv);
        player.setItemOnCursor(null);
        player.getInventory().setItem(bagSlot, bag);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
        refreshBaitSlot(menu, bag, entry);
    }

    private static void withdraw(Player player, ChestMenu menu, ItemStack bag, int bagSlot, BaitEntry entry) {
        long remaining = getAmount(bag, entry);
        if (remaining <= 0) return;
        long toTake = Math.min(remaining, 3456L); // 最多取出 3456 个
        long left = toTake;
        while (left > 0) {
            ItemStack stack = entry.item().clone();
            int give = (int) Math.min(left, 64);
            stack.setAmount(give);
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
            // 修复(L1)：addItem 部分放入时会返回 overflow，必须按"实际放入数量"扣减 left，
            // 否则（尝试数-未扣减的left）会导致袋中数量少扣，重复取出刷物品
            int overflowTotal = 0;
            for (ItemStack rest : leftovers.values()) {
                overflowTotal += rest.getAmount();
            }
            int actuallyAdded = give - overflowTotal;
            left -= actuallyAdded;
            if (!leftovers.isEmpty()) break; // 背包已满（有溢出），结束循环
        }
        setAmount(bag, entry, remaining - (toTake - left));
        player.getInventory().setItem(bagSlot, bag);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
        refreshBaitSlot(menu, bag, entry);
    }

    // ==================== 输入口 ====================

    /** 真实输入槽: 点击拿起/放下, Shift+点击移入背包, 全部原位刷新不闪烁 */
    private static void handleInput(Player player, ChestMenu menu, ItemStack bag, int bagSlot, me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction action) {
        ItemStack cursor = player.getItemOnCursor();
        ItemStack stored = getInputItem(bag);

        // Shift+点击: 槽内物品移到背包
        if (action.isShiftClicked()) {
            if (stored != null) {
                Map<Integer, ItemStack> left = player.getInventory().addItem(stored);
                // 修复: addItem 返回的 Map 键不保证为 0, 用 values 取首个剩余,
                // 避免 left.get(0) 为 null 时把"背包装不下的部分"静默清空吞掉
                ItemStack leftover = left.isEmpty() ? null : left.values().iterator().next();
                setInputItem(bag, leftover);
                player.getInventory().setItem(bagSlot, bag);
                refreshInputSlot(menu, bag);
            }
            return;
        }
        // 空手点击: 拿起槽内物品到光标, 自行找位置放下
        if (cursor == null || cursor.getType().isAir()) {
            if (stored != null) {
                player.setItemOnCursor(stored);
                setInputItem(bag, null);
                player.getInventory().setItem(bagSlot, bag);
                refreshInputSlot(menu, bag);
            }
            return;
        }
        // 槽内已有物品时不处理, 原样保留
        if (stored != null) {
            return;
        }

        placeIntoInput(player, menu, bag, bagSlot, cursor, true);
    }

    /** 放入输入槽: 鱼饵直接吃掉存入, 其他物品持久化在槽中 */
    private static void placeIntoInput(Player player, ChestMenu menu, ItemStack bag, int bagSlot, ItemStack item, boolean fromCursor) {
        BaitEntry direct = matchBait(item);
        if (direct != null) {
            // 鱼饵: 不持久化, 直接吃掉并存入对应槽位
            long cur = getAmount(bag, direct);
            long add = item.getAmount();
            long nv = cur + add;
            if (nv < cur) nv = Long.MAX_VALUE;
            setAmount(bag, direct, nv);
            player.getInventory().setItem(bagSlot, bag);
            if (fromCursor) {
                player.setItemOnCursor(null);
            }
            refreshBaitSlot(menu, bag, direct);
            return;
        }

        // 其他物品: 持久化到输入槽, 关闭界面也不会吞掉
        setInputItem(bag, item.clone());
        if (fromCursor) {
            player.setItemOnCursor(null);
        }
        player.getInventory().setItem(bagSlot, bag);
        processInputSource(player, menu, bag, bagSlot, item);
        refreshInputSlot(menu, bag);
    }

    /** Shift+左键背包物品快速放入输入槽 */
    private static void fastInputFromInventory(Player player, ChestMenu menu, ItemStack bag, int bagSlot, ItemStack item, int invSlot) {
        player.getInventory().setItem(invSlot, null);
        placeIntoInput(player, menu, bag, bagSlot, item, false);
    }

    /** 处理输入槽内持久化的来源物品(以太秘匣/量子存储/虚空之触) */
    private static void processInputSource(Player player, ChestMenu menu, ItemStack bag, int bagSlot, ItemStack source) {
        SlimefunItem sf = SlimefunItem.getByItem(source);

        // ① 以太秘匣(CargoFragment)
        if (sf instanceof CargoFragment) {
            ItemMeta meta = source.getItemMeta();
            String json = meta == null ? null : meta.getPersistentDataContainer().get(KEY_FRAGMENT_ITEM, PersistentDataType.STRING);
            Integer fragAmount = meta == null ? null : meta.getPersistentDataContainer().get(KEY_FRAGMENT_AMOUNT, PersistentDataType.INTEGER);
            if (json != null && fragAmount != null && fragAmount > 0) {
                BaitEntry fragBait = matchBait(itemFromBase64(json));
                if (fragBait != null) {
                    long cur = getAmount(bag, fragBait);
                    long free = Long.MAX_VALUE - cur;
                    int take = (int) Math.min(fragAmount, free);
                    if (take <= 0) return;
                    setAmount(bag, fragBait, cur + take);
                    int newAmount = fragAmount - take;
                    if (newAmount <= 0) {
                        // 秘匣内容已全部取出, 清空输入槽
                        setInputItem(bag, null);
                    } else {
                        // 更新秘匣剩余数量并持久化
                        meta.getPersistentDataContainer().set(KEY_FRAGMENT_AMOUNT, PersistentDataType.INTEGER, newAmount);
                        source.setItemMeta(meta);
                        setInputItem(bag, source);
                    }
                    player.getInventory().setItem(bagSlot, bag);
                    player.sendMessage(getGradientNameVer2("Lure withdrawn from the Aether Cache."));
                    refreshBaitSlot(menu, bag, fragBait);
                    return;
                }
            }
            return;
        }

        // ② 量子存储物品
        if (NetworkStorage.isQuantumStorageItem(source)) {
            ItemMeta meta = source.getItemMeta();
            NetworkStorage.QuantumCache qc = NetworkStorage.getQuantumCache(meta);
            if (qc == null || qc.getItemStack() == null) return;
            BaitEntry qBait = matchBait(qc.getItemStack());
            if (qBait == null) return;
            long bagAmount = getAmount(bag, qBait);
            long take = Math.min(qc.getAmountLong(), Long.MAX_VALUE - bagAmount);
            if (take <= 0) return;
            setAmount(bag, qBait, bagAmount + take);
            qc.setAmount(qc.getAmountLong() - take);
            NamespacedKey storageKey = findQuantumKey(meta);
            if (storageKey != null) {
                NetworkStorage.setCustom(meta, storageKey, NetworkStorage.QUANTUM_STORAGE_TYPE, qc);
                NetworkStorage.refreshLore(meta, qc);
            }
            source.setItemMeta(meta);
            // 更新输入槽内保存的量子存储物品(已扣减)
            setInputItem(bag, source);
            player.getInventory().setItem(bagSlot, bag);
            player.sendMessage(getGradientNameVer2("Lure withdrawn from Quantum Storage."));
            refreshBaitSlot(menu, bag, qBait);
            return;
        }

        // ③ 虚空之触 → 绑定的量子存储方块
        if (sf instanceof VoidTouch) {
            handleVoidTouchInput(player, menu, bag, bagSlot, source);
        }
    }

    /** 鱼饵存储槽位映射(当前页动态构建, 分页后只定位本页已显示的鱼饵) */
    private static int baitSlotFor(BaitEntry entry) {
        Integer slot = PAGE_SLOTS.get(entry);
        return slot != null ? slot : -1;
    }

    private static void refreshBaitSlot(ChestMenu menu, ItemStack bag, BaitEntry entry) {
        int slot = baitSlotFor(entry);
        if (slot >= 0) {
            menu.replaceExistingItem(slot, baitIcon(bag, entry));
        }
    }

    private static void refreshInputSlot(ChestMenu menu, ItemStack bag) {
        ItemStack input = getInputItem(bag);
        menu.replaceExistingItem(INPUT_SLOT, input == null ? new ItemStack(Material.AIR) : input);
    }

    private static ItemStack getInputItem(ItemStack bag) {
        if (bag == null || !bag.hasItemMeta()) return null;
        String json = bag.getItemMeta().getPersistentDataContainer().get(KEY_INPUT_ITEM, PersistentDataType.STRING);
        if (json == null || json.isEmpty()) return null;
        return itemFromBase64(json);
    }

    private static void setInputItem(ItemStack bag, ItemStack item) {
        ItemMeta meta = bag.getItemMeta();
        if (item == null || item.getType().isAir()) {
            meta.getPersistentDataContainer().remove(KEY_INPUT_ITEM);
        } else {
            meta.getPersistentDataContainer().set(KEY_INPUT_ITEM, PersistentDataType.STRING, itemToBase64(item));
        }
        bag.setItemMeta(meta);
    }

    private static void handleVoidTouchInput(Player player, ChestMenu menu, ItemStack bag, int bagSlot, ItemStack voidTouch) {
        ItemMeta meta = voidTouch.getItemMeta();
        if (meta == null) return;
        var container = meta.getPersistentDataContainer();
        if (!container.has(KEY_X, PersistentDataType.INTEGER) || !container.has(KEY_Y, PersistentDataType.INTEGER)
                || !container.has(KEY_Z, PersistentDataType.INTEGER) || !container.has(KEY_WORLD, PersistentDataType.STRING)) {
            return;
        }
        org.bukkit.World world = Bukkit.getWorld(container.get(KEY_WORLD, PersistentDataType.STRING));
        if (world == null) {
            return;
        }
        Location target = new Location(world,
                container.get(KEY_X, PersistentDataType.INTEGER),
                container.get(KEY_Y, PersistentDataType.INTEGER),
                container.get(KEY_Z, PersistentDataType.INTEGER));
        SlimefunItem targetSf = StorageCacheUtils.getSfItem(target);
        if (targetSf == null || !NetworkStorage.isQuantumStorageBlock(targetSf)) {
            return;
        }

        try {
            Class<?> cls = Class.forName(QUANTUM_STORAGE_BLOCK_CLASS);
            Object cache = ((Map<?, ?>) cls.getMethod("getCaches").invoke(null)).get(target);
            if (cache == null) {
                return;
            }
            ItemStack stored = (ItemStack) cache.getClass().getMethod("getItemStack").invoke(cache);
            BaitEntry vBait = matchBait(stored);
            if (vBait == null) {
                return;
            }
            long amount = ((Number) cache.getClass().getMethod("getAmount").invoke(cache)).longValue();
            long bagAmount = getAmount(bag, vBait);
            long take = Math.min(amount, Long.MAX_VALUE - bagAmount);
            if (take <= 0) return;
            try {
                cache.getClass().getMethod("setAmount", long.class).invoke(cache, amount - take);
            } catch (NoSuchMethodException e) {
                cache.getClass().getMethod("setAmount", int.class).invoke(cache, (int) (amount - take));
            }
            cls.getMethod("syncBlock", Location.class, cache.getClass()).invoke(null, target, cache);

            setAmount(bag, vBait, getAmount(bag, vBait) + take);
            player.setItemOnCursor(null);
            player.getInventory().setItem(bagSlot, bag);
            player.sendMessage(getGradientNameVer2("Lure withdrawn from Quantum Storage."));
            refreshBaitSlot(menu, bag, vBait);
        } catch (Throwable t) {
            // 修复(L3)：空吞异常改为输出警告日志，方便排查量子存储兼容问题
            MagicExpansion.getInstance().getLogger().warning("Lure Bag failed to read Quantum Storage data: " + t.getMessage());
        }
    }

    private static BaitEntry matchBait(ItemStack item) {
        if (item == null) return null;
        SlimefunItem sf = SlimefunItem.getByItem(item);
        if (sf != null) {
            for (BaitEntry entry : BAITS) {
                SlimefunItem entrySf = SlimefunItem.getByItem(entry.item());
                if (entrySf != null && entrySf.getId().equals(sf.getId())) return entry;
            }
        }
        for (BaitEntry entry : BAITS) {
            if (SlimefunUtils.isItemSimilar(item, entry.item(), true)) return entry;
        }
        return null;
    }

    /** 兼容官方/中文/新版三种量子存储 key */
    private static NamespacedKey findQuantumKey(ItemMeta meta) {
        if (meta == null) return null;
        var container = meta.getPersistentDataContainer();
        if (container.has(NetworkStorage.QUANTUM_STORAGE_INSTANCE, NetworkStorage.QUANTUM_STORAGE_TYPE)) {
            return NetworkStorage.QUANTUM_STORAGE_INSTANCE;
        }
        if (container.has(NetworkStorage.QUANTUM_STORAGE_INSTANCE2, NetworkStorage.QUANTUM_STORAGE_TYPE)) {
            return NetworkStorage.QUANTUM_STORAGE_INSTANCE2;
        }
        if (container.has(NetworkStorage.QUANTUM_STORAGE_INSTANCE3, NetworkStorage.QUANTUM_STORAGE_TYPE)) {
            return NetworkStorage.QUANTUM_STORAGE_INSTANCE3;
        }
        return null;
    }
}
