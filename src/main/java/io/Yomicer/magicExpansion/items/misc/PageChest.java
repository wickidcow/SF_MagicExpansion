package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.abstracts.AbstractContainer;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Random;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getRandomGradientName;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemToBase64;

/**
 * 翻页储物箱
 * 玩家界面直接使用 Slimefun BlockMenu("items"), 与粘液货运操作的是同一个 inventory,
 * 货运的写入/取走实时可见, 不存在副本导致的刷物/不同步问题
 * 当前页数据在 BlockMenu 中, 其他页存 BlockStorage 自定义键, 翻页时互相读写
 * Shift+右键不会打开界面, 可用于放置粘液货运节点
 */
public class PageChest extends AbstractContainer {

    public static final int TOTAL_PAGES = 5;
    public static final int CHEST_SIZE = 54;

    public static final String DATA_CURRENT_PAGE = "magicexpansion_pagechest_currentpage";
    private static final String DATA_PREFIX = "magicexpansion_pagechest_";

    private static final int[] STORAGE_SLOTS;

    // 翻页音效: 随机音符盒音色
    private static final Sound[] FLIP_SOUNDS = {
            Sound.BLOCK_NOTE_BLOCK_BANJO,
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Sound.BLOCK_NOTE_BLOCK_BIT,
            Sound.BLOCK_NOTE_BLOCK_CHIME,
            Sound.BLOCK_NOTE_BLOCK_COW_BELL,
            Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
            Sound.BLOCK_NOTE_BLOCK_FLUTE,
            Sound.BLOCK_NOTE_BLOCK_GUITAR,
            Sound.BLOCK_NOTE_BLOCK_HARP,
            Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
            Sound.BLOCK_NOTE_BLOCK_PLING,
            Sound.BLOCK_NOTE_BLOCK_SNARE,
            Sound.BLOCK_NOTE_BLOCK_XYLOPHONE
    };

    static {
        STORAGE_SLOTS = new int[CHEST_SIZE];
        for (int i = 0; i < CHEST_SIZE; i++) {
            STORAGE_SLOTS[i] = i;
        }
    }

    public PageChest(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        // 拦截已放置的翻页储物箱的右键交互
        addItemHandler((BlockUseHandler) e -> {
            if (e.getHand() != EquipmentSlot.HAND) {
                return;
            }
            Block block = e.getClickedBlock().orElse(null);
            if (block == null) {
                return;
            }
            if (!(BlockStorage.check(block) instanceof PageChest)) {
                // 点击的不是已放置的翻页储物箱, 放行
                return;
            }
            // 阻止原版箱子界面
            e.setUseBlock(Event.Result.DENY);
            e.setUseItem(Event.Result.DENY);
            if (e.getPlayer().isSneaking()) {
                // Shift+右键: 不打开界面, 让粘液货运节点等物品的绑定逻辑正常执行
                return;
            }
            open(e.getPlayer(), block.getLocation());
        });
    }

    @Override
    protected void setupMenu(@Nonnull BlockMenuPreset preset) {
        // BlockMenu 即为货运容器, 界面由 PageChest.open 直接打开
        preset.setSize(CHEST_SIZE);
    }

    @Override
    protected @Nonnull int[] getInputSlots() {
        return STORAGE_SLOTS;
    }

    @Override
    protected @Nonnull int[] getOutputSlots() {
        return STORAGE_SLOTS;
    }

    @Override
    protected void onPlace(@Nonnull BlockPlaceEvent e, @Nonnull Block b) {
        BlockStorage.addBlockInfo(b.getLocation(), DATA_CURRENT_PAGE, "1");
    }

    @Override
    protected void onBreak(@Nonnull BlockBreakEvent e, @Nonnull BlockMenu menu, @Nonnull Location location) {
        dropAllPages(location, menu);
    }

    // ===================== 打开界面 =====================

    // 直接打开 BlockMenu("items"), 与货运共用同一个 inventory, 天然同步
    public static void open(@Nonnull Player player, @Nonnull Location location) {
        BlockMenu menu = StorageCacheUtils.getMenu(location);
        if (menu == null) {
            return;
        }
        menu.open(player);
        player.getOpenInventory().setTitle(title(currentPage(location)));
        playAnimation(player, location, true);
    }

    private static String title(int page) {
        return getRandomGradientName("跃迁储物箱 · 第 " + page + "/" + TOTAL_PAGES + " 页 (左键上一页 右键下一页)");
    }

    // ===================== 页码 =====================

    public static int currentPage(@Nonnull Location location) {
        String data = BlockStorage.getLocationInfo(location, DATA_CURRENT_PAGE);
        if (data != null && !data.isEmpty()) {
            try {
                return Math.max(1, Math.min(TOTAL_PAGES, Integer.parseInt(data)));
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    // ===================== 翻页 =====================

    public static void flip(@Nonnull Player player, @Nonnull Location location, int direction) {
        BlockMenu menu = StorageCacheUtils.getMenu(location);
        if (menu == null) {
            return;
        }
        int page = currentPage(location);
        int next = page + direction;
        if (next < 1) {
            next = TOTAL_PAGES;
        } else if (next > TOTAL_PAGES) {
            next = 1;
        }

        // 旧页(玩家改动已直接生效于 BlockMenu)写回存档
        savePage(location, page, menu.toInventory().getContents());
        BlockStorage.addBlockInfo(location, DATA_CURRENT_PAGE, String.valueOf(next));

        // 载入新页到 BlockMenu, 界面与货运同时切换到新页
        menu.toInventory().setContents(loadPage(location, next));
        menu.markDirty();
        player.getOpenInventory().setTitle(title(next));

        // 翻页音效: 随机音符盒音色
        Sound flipSound = FLIP_SOUNDS[new Random().nextInt(FLIP_SOUNDS.length)];
        location.getWorld().playSound(location, flipSound, 0.5f, 1.0f);
    }

    // ===================== 数据存取 =====================

    private static String slotKey(int page, int slot) {
        return DATA_PREFIX + page + "_" + slot;
    }

    public static ItemStack[] loadPage(@Nonnull Location location, int page) {
        ItemStack[] contents = new ItemStack[CHEST_SIZE];
        for (int i = 0; i < CHEST_SIZE; i++) {
            String data = BlockStorage.getLocationInfo(location, slotKey(page, i));
            contents[i] = (data == null || data.isEmpty()) ? null : itemFromBase64(data);
        }
        return contents;
    }

    public static void savePage(@Nonnull Location location, int page, @Nonnull ItemStack[] contents) {
        for (int i = 0; i < CHEST_SIZE; i++) {
            ItemStack item = (i < contents.length) ? contents[i] : null;
            if (item == null || item.getType() == Material.AIR) {
                BlockStorage.addBlockInfo(location, slotKey(page, i), "");
            } else {
                BlockStorage.addBlockInfo(location, slotKey(page, i), itemToBase64(item));
            }
        }
    }

    // ===================== 破坏掉落 =====================

    public static void dropAllPages(@Nonnull Location location, @Nonnull BlockMenu menu) {
        // 当前页: BlockMenu("items") 中的数据
        ItemStack[] current = (menu != null) ? menu.toInventory().getContents() : new ItemStack[CHEST_SIZE];
        for (ItemStack item : current) {
            if (item != null && item.getType() != Material.AIR) {
                drop(location, item);
            }
        }

        // 其他页: 自定义键存档
        int currentPage = currentPage(location);
        for (int p = 1; p <= TOTAL_PAGES; p++) {
            if (p == currentPage) {
                continue;
            }
            for (ItemStack item : loadPage(location, p)) {
                if (item != null && item.getType() != Material.AIR) {
                    drop(location, item);
                }
            }
        }
    }

    private static void drop(@Nonnull Location location, @Nonnull ItemStack item) {
        location.getWorld().dropItemNaturally(location, item);
    }

    // ===================== 动画 =====================

    public static void playAnimation(@Nonnull Player player, @Nonnull Location location, boolean open) {
        BlockState state = location.getBlock().getState();
        if (state instanceof Chest chest) {
            if (open) {
                chest.open();
            } else {
                chest.close();
            }
        }
        player.getWorld().playSound(location, open ? Sound.BLOCK_CHEST_OPEN : Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
    }
}
