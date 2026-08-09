package io.Yomicer.magicExpansion.items.misc.baitbag;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.CargoFragment;
import io.Yomicer.magicExpansion.items.tools.VoidTouch;
import io.Yomicer.magicExpansion.utils.NetworkStorage;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
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
import org.bukkit.entity.Player;
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

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemToBase64;

/**
 * 饵料袋菜单: 固定槽位存储织梦者/水云间/记忆碎片鱼饵, 支持优先级与外部存储输入
 */
public final class BaitBagMenu {

    /** 鱼饵注册表(id / 钓鱼逻辑 key / 系列 / 展示物品) */
    public record BaitEntry(String id, String key, String series, ItemStack item) {
    }

    private static final List<BaitEntry> BAITS = List.of(
            new BaitEntry("fishlurebasic", "fishLureBasic", "织梦者", MagicExpansionItems.FISH_LURE_BASIC),
            new BaitEntry("fishluredust", "fishLureDust", "织梦者", MagicExpansionItems.FISH_LURE_DUST),
            new BaitEntry("fishlureore", "fishLureOre", "织梦者", MagicExpansionItems.FISH_LURE_ORE),
            new BaitEntry("fishlurealloy", "fishLureAlloyIngot", "织梦者", MagicExpansionItems.FISH_LURE_ALLOY_INGOT),
            new BaitEntry("cuixia", "CuiXia", "水云间", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA),
            new BaitEntry("weichen", "WeiChen", "水云间", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN),
            new BaitEntry("ronghuo", "RongHuo", "水云间", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO),
            new BaitEntry("yuejin", "YueJin", "水云间", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN),
            new BaitEntry("xinghe", "XingHe", "水云间", MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE),
            new BaitEntry("memory", "fishLureFinal", "记忆碎片", memoryFragment())
    );

    private static final int[] DREAMER_SLOTS = {11, 12, 13, 14};
    private static final int[] WATER_SLOTS = {20, 21, 22, 23, 24};
    private static final int MEMORY_SLOT = 15;
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

    // ==================== 记忆碎片(直接沿用钓鱼池里的生成方式) ====================

    private static ItemStack memoryFragment() {
        return new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD),
                getGradientNameVer2("鱼饵·记忆碎片"),
                "§f这个鱼饵可以钓到任何物品",
                "§f他存在于过去或者是未来",
                "§f你现在看到的他并非真正的他");
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

    private static int seriesSize(String series) {
        return switch (series) {
            case "织梦者" -> 4;
            case "水云间" -> 5;
            default -> 1;
        };
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
                return Integer.compare(BAITS.indexOf(a), BAITS.indexOf(b)); // 同级按 织梦者→水云间→记忆碎片
            });

            BaitEntry chosen = candidates.get(0);
            setAmount(bag, chosen, getAmount(bag, chosen) - 1);
            player.getInventory().setItem(slot, bag);
            return chosen.key();
        }
        return null;
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
            player.sendMessage(getGradientNameVer2("饵料袋不在背包中"));
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

        ChestMenu menu = new ChestMenu(getGradientNameVer2("✦ 饵料袋 ✦"));
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(true);
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f));

        // 白色填充(含第1行7槽装饰、取消返回按钮后的1槽、各行行首与行尾)
        for (int s : new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53}) {
            menu.addItem(s, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        // 粉红输入槽(命名)
        for (int s : new int[]{47, 48, 50, 51}) {
            menu.addItem(s, new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, getGradientNameVer2("输入槽")), ChestMenuUtils.getEmptyClickHandler());
        }
        // 淡蓝填充(水云间区 + 下半区)
        for (int s : new int[]{16, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43}) {
            menu.addItem(s, plainPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 4槽: 使用说明
        menu.addItem(4, new CustomItemStack(Material.BOOK,
                getGradientNameVer2("云梦袋"),
                getGradientNameVer2("左键放入对应鱼饵"),
                getGradientNameVer2("空手左键取出1个"),
                getGradientNameVer2("Shift+左键取出全部(最多3456)"),
                getGradientNameVer2("右键循环调整优先级")), ChestMenuUtils.getEmptyClickHandler());

        // 系列标签
        menu.addItem(10, new CustomItemStack(Material.GHAST_TEAR, getGradientNameVer2("织梦者"), getGradientNameVer2("优先级 1~4")), ChestMenuUtils.getEmptyClickHandler());
        menu.addItem(19, new CustomItemStack(Material.CYAN_DYE, getGradientNameVer2("水云间"), getGradientNameVer2("优先级 1~5")), ChestMenuUtils.getEmptyClickHandler());

        // 翻页槽: 无页可翻时显示白色玻璃板
        menu.addItem(46, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        menu.addItem(52, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());

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

        // 织梦者鱼饵槽
        for (int i = 0; i < DREAMER_SLOTS.length; i++) {
            int baitSlot = DREAMER_SLOTS[i];
            BaitEntry entry = BAITS.get(i);
            menu.addItem(baitSlot, baitIcon(current, entry), ChestMenuUtils.getEmptyClickHandler());
            menu.addMenuClickHandler(baitSlot, (p, s, it, a) -> {
                handleBaitClick(p, menu, current, bagSlot, entry, a);
                return false;
            });
        }
        // 水云间鱼饵槽
        for (int i = 0; i < WATER_SLOTS.length; i++) {
            int baitSlot = WATER_SLOTS[i];
            BaitEntry entry = BAITS.get(4 + i);
            menu.addItem(baitSlot, baitIcon(current, entry), ChestMenuUtils.getEmptyClickHandler());
            menu.addMenuClickHandler(baitSlot, (p, s, it, a) -> {
                handleBaitClick(p, menu, current, bagSlot, entry, a);
                return false;
            });
        }
        // 记忆碎片槽
        BaitEntry memory = BAITS.get(BAITS.size() - 1);
        menu.addItem(MEMORY_SLOT, baitIcon(current, memory), ChestMenuUtils.getEmptyClickHandler());
        menu.addMenuClickHandler(MEMORY_SLOT, (p, s, it, a) -> {
            handleBaitClick(p, menu, current, bagSlot, memory, a);
            return false;
        });

        menu.open(player);
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
        lore.add(getGradientNameVer2("库存：" + amount));
        lore.add(getGradientNameVer2("优先级：" + priority + " / " + seriesSize(entry.series())));
        lore.add(getGradientNameVer2("空手左键取出1 · 左键放入 · Shift取出(≤3456) · 右键优先级"));
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
            player.sendMessage(getGradientNameVer2("请手持对应的鱼饵点击槽位"));
            return;
        }
        if (!match.id().equals(entry.id())) {
            player.sendMessage(getGradientNameVer2("这不是该槽位的鱼饵"));
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
            if (!leftovers.isEmpty()) break;
            left -= give;
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
                setInputItem(bag, left.isEmpty() ? null : left.get(0));
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
                    player.sendMessage(getGradientNameVer2("已从以太秘匣取出鱼饵"));
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
            player.sendMessage(getGradientNameVer2("已从量子存储取出鱼饵"));
            refreshBaitSlot(menu, bag, qBait);
            return;
        }

        // ③ 虚空之触 → 绑定的量子存储方块
        if (sf instanceof VoidTouch) {
            handleVoidTouchInput(player, menu, bag, bagSlot, source);
        }
    }

    private static int baitSlotFor(BaitEntry entry) {
        int idx = BAITS.indexOf(entry);
        if (idx >= 0 && idx < 4) return DREAMER_SLOTS[idx];
        if (idx >= 4 && idx < 9) return WATER_SLOTS[idx - 4];
        return MEMORY_SLOT;
    }

    private static void refreshBaitSlot(ChestMenu menu, ItemStack bag, BaitEntry entry) {
        menu.replaceExistingItem(baitSlotFor(entry), baitIcon(bag, entry));
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
            player.sendMessage(getGradientNameVer2("已从量子存储取出鱼饵"));
            refreshBaitSlot(menu, bag, vBait);
        } catch (Throwable t) {
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
