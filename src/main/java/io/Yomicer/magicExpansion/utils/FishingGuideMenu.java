package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.misc.WeightedItem;
import io.Yomicer.magicExpansion.items.misc.fish.fishInterface.BaseFish;
import io.Yomicer.magicExpansion.items.misc.fish.fishInterface.FishManager;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static io.Yomicer.magicExpansion.utils.CreateItem.createItem;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;

public class FishingGuideMenu {

    // 主菜单标题(渐变色)
    private static final String MAIN_TITLE = getGradientName("✨ Fishing Guide ✨");

    // 分类数据
    private static final String[] CATEGORIES = {
            "common_fish", "uncommon_fish", "rare_fish",
            "epic_fish", "legendary_fish", "mythical_fish", "junk",
            "magic_sugar", "bread"
    };
    private static final String[] DISPLAY_NAMES = {
            "§f✦ Common Fish", "§a✦ Uncommon Fish", "§9✦ Rare Fish",
            "§5✦ Epic Fish", "§6✦ Legendary Fish", "§e✦ Mythical Fish", "§7✦ Junk",
            "§d✦ Bait: Magic Sugar", "§b✦ Bait: Bread"
    };
    private static final Material[] ICONS = {
            Material.COD, Material.SALMON, Material.PUFFERFISH,
            Material.TROPICAL_FISH, Material.NAUTILUS_SHELL, Material.NETHER_STAR, Material.FISHING_ROD,
            Material.SUGAR, Material.BREAD
    };
    private static final String[] LORES = {
            "§eThe most common catches and easy to find.",
            "§eLess common than ordinary fish, but not truly rare.",
            "§eFound in deeper waters and requires patience.",
            "§eA legendary sea creature that is exceptionally rare.",
            "§eA miraculous catch that appears only in special weather.",
            "§eA fish believed to exist only in myth!",
            "§eMostly junk... but perhaps there is a surprise?",
            "§e— Shows possible catches; strongly targeted bait catches are omitted —",
            "§e— Shows possible catches; strongly targeted bait catches are omitted —"
    };

    // 装饰玻璃
    private static final Material BORDER_GLASS = Material.BLUE_STAINED_GLASS_PANE;
    private static final Material CORNER_GLASS = Material.LIGHT_BLUE_STAINED_GLASS_PANE;

    // 返回按钮
    private static final ItemStack BACK_BUTTON = createItemWithLore(Material.ARROW, "§a← Back");

    /**
     * 创建带有lore的物品
     */
    private static ItemStack createItemWithLore(Material material, String displayName, String... lore) {
        ItemStack item = createItem(material, displayName);
        if (lore != null && lore.length > 0) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setLore(Arrays.asList(lore));
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    /**
     * 创建分类物品
     */
    private static ItemStack createCategoryItem(Material material, String name, String lore) {
        List<String> loreList = Arrays.asList("", lore, "", "§a▶ Click to Enter");
        return createItemWithLore(material, name, loreList.toArray(new String[0]));
    }

    /**
     * 创建装饰玻璃
     */
    private static ItemStack createDecorItem(Material material, String name) {
        return createItemWithLore(material, name, "§7Fishing Guide");
    }

    /**
     * 打开主菜单
     */
    public static void openMainMenu(Player player) {
        ChestMenu menu = new ChestMenu(MAIN_TITLE);

        // 设置边框装饰
        setupMenuBorders(menu);

        // 添加分类按钮(从第一个空位开始)
        int[] categorySlots = getAvailableSlots();
        for (int i = 0; i < CATEGORIES.length && i < categorySlots.length; i++) {
            ItemStack item = createCategoryItem(ICONS[i], DISPLAY_NAMES[i], LORES[i]);
            menu.addItem(categorySlots[i], item);

            final String category = CATEGORIES[i];
            menu.addMenuClickHandler(categorySlots[i], (p, slot, itemStack, click) -> {
                openCategoryPage(p, category);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return false;
            });
        }

        // 添加关闭按钮(49位置)
        menu.addItem(49, createItemWithLore(Material.BARRIER, "§cClose Menu"));
        menu.addMenuClickHandler(49, (p, slot, item, click) -> {
            p.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        int randomSlot = 21 + new Random().nextInt(24);
        // 50%概率添加物品
        if (new Random().nextBoolean()) {
            int choice = new Random().nextInt(3);

            switch (choice) {
                case 0:
                    menu.addItem(randomSlot,
                            new CustomItemStack(Material.BREAD,
                                    getGradientName("Bait: Bread"),
                                    getGradientName("Click to receive one Bread.")),
                            (player1, slot, item, action) -> {
                                ItemStack drop = new ItemStack(Material.BREAD);
                                player1.getWorld().dropItemNaturally(player1.getLocation(), drop);
                                menu.addItem(slot,
                                        new ItemStack(Material.AIR), (player2, slot2, item2, action2) -> false
                                );
                                return false;
                            });
                    break;
                case 1:
                    menu.addItem(randomSlot,
                            new CustomItemStack(Material.SUGAR,
                                    getGradientName("Bait: Magic Sugar"),
                                    getGradientName("Click to receive one Magic Sugar.")),
                            (player1, slot, item, action) -> {
                                SlimefunItem sfItem = SlimefunItem.getByItem(SlimefunItems.MAGIC_SUGAR);
                                ItemStack drop = sfItem.getItem().clone();
                                player1.getWorld().dropItemNaturally(player1.getLocation(), drop);
                                menu.addItem(slot,
                                        new ItemStack(Material.AIR), (player2, slot2, item2, action2) -> false
                                );
                                return false;
                            });
                    break;
                case 2:
                    menu.addItem(randomSlot,
                            new CustomItemStack(Material.BONE_MEAL,
                                    getGradientName("Bait: Basic Magic Bait"),
                                    getGradientName("Click to receive one Basic Magic Bait.")),
                            (player1, slot, item, action) -> {
                                SlimefunItem sfItem = SlimefunItem.getByItem(MagicExpansionItems.FISH_LURE_BASIC);
                                ItemStack drop = sfItem.getItem().clone();
                                player1.getWorld().dropItemNaturally(player1.getLocation(), drop);
                                menu.addItem(slot,
                                        new ItemStack(Material.AIR), (player2, slot2, item2, action2) -> false
                                );
                                return false;
                            });
                    break;
            }
        }

        // 设置不可点击空槽和玩家背包
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(false);

        // 打开菜单
        menu.open(player);
    }

    /**
     * 设置菜单边框装饰
     */
    private static void setupMenuBorders(ChestMenu menu) {
        menu.addItem(0, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(0, (p, slot, item, click) -> false);
        menu.addItem(8, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(8, (p, slot, item, click) -> false);
        menu.addItem(45, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(45, (p, slot, item, click) -> false);
        menu.addItem(53, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(53, (p, slot, item, click) -> false);

        for (int i = 1; i < 8; i++) {
            menu.addItem(i, createDecorItem(BORDER_GLASS, "§9🌊 Ocean Guide"));
            menu.addMenuClickHandler(i, (p, slot, item, click) -> false);
        }

        for (int i = 46; i < 53; i++) {
            menu.addItem(i, createDecorItem(BORDER_GLASS, "§bExploring the depths..."));
            menu.addMenuClickHandler(i, (p, slot, item, click) -> false);
        }

        // 左边框 (9,18,27,36)
        int[] leftBorder = {9, 18, 27, 36};
        for (int slot : leftBorder) {
            menu.addItem(slot, createDecorItem(BORDER_GLASS, "§9🎣"));
            menu.addMenuClickHandler(slot, (p, slot1, item, click) -> false);
        }

        // 右边框 (17,26,35,44)
        int[] rightBorder = {17, 26, 35, 44};
        for (int slot : rightBorder) {
            menu.addItem(slot, createDecorItem(BORDER_GLASS, "§9🐟"));
            menu.addMenuClickHandler(slot, (p, slot1, item, click) -> false);
        }
    }

    /**
     * 获取可用的槽位(排除边框后的空位)
     */
    private static int[] getAvailableSlots() {
        // 所有被占用的边框槽位
        Set<Integer> occupiedSlots = new HashSet<>();

        // 四个角
        occupiedSlots.add(0);
        occupiedSlots.add(8);
        occupiedSlots.add(45);
        occupiedSlots.add(53);

        // 上下边框
        for (int i = 1; i < 8; i++) occupiedSlots.add(i);
        for (int i = 46; i < 53; i++) occupiedSlots.add(i);

        // 左右边框
        int[] leftRightBorder = {9, 17, 18, 26, 27, 35, 36, 44};
        for (int slot : leftRightBorder) occupiedSlots.add(slot);

        // 收集所有可用槽位
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (!occupiedSlots.contains(i)) {
                availableSlots.add(i);
            }
        }

        return availableSlots.stream().mapToInt(i -> i).toArray();
    }

    /**
     * 打开分类详情页(支持分页)
     */
    public static void openCategoryPage(Player player, String category) {
        openCategoryPage(player, category, 0); // 默认打开第0页
    }

    private static void openCategoryPage(Player player, String category, int page) {
        String title = getCategoryTitle(category);
        ChestMenu menu = new ChestMenu(getGradientName("✦ " + title));

        // 设置边框装饰
        setupCategoryBorders(menu, title);

        // 获取该分类下的所有物品
        List<ItemStack> allItems = getItemsByCategory(category);

        // 分页设置 - 使用实际可用槽位数量
        int[] availableSlots = getAvailableSlots();
        int itemsPerPage = availableSlots.length; // 每页物品数量等于可用槽位数
        int totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);

        // 确保页码在有效范围内
        page = Math.max(0, Math.min(page, totalPages - 1));

        // 获取当前页的物品
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allItems.size());
        List<ItemStack> currentPageItems = allItems.subList(startIndex, endIndex);

        // 填充当前页物品到可用槽位
        for (int i = 0; i < currentPageItems.size() && i < availableSlots.length; i++) {
            ItemStack originalItem = currentPageItems.get(i);

            // 克隆物品并设置渐变名称
            ItemStack display = originalItem.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                String displayName = ItemStackHelper.getDisplayName(display);
                meta.setDisplayName("§r"+(displayName));

                List<String> newLore = getStrings(page, meta, totalPages);
                meta.setLore(newLore);

                display.setItemMeta(meta);
            }

            menu.addItem(availableSlots[i], display);
            menu.addMenuClickHandler(availableSlots[i], (p, s, i1, c) -> false);
        }

        // 添加分页导航按钮
        setupCategoryPagination(menu, category, page, totalPages);

        // 禁用点击和背包交互
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(false);

        // 打开菜单
        menu.open(player);
    }

    private static @NotNull List<String> getStrings(int page, ItemMeta meta, int totalPages) {
        List<String> originalLore = meta.getLore();
        List<String> newLore = new ArrayList<>();
        // 如果原有Lore不为空,先添加原有Lore
        if (originalLore != null && !originalLore.isEmpty()) {
            newLore.addAll(originalLore);
        }
        newLore.add("");
        newLore.add("§7This guide entry is for display only.");
        newLore.add("§8— Shows uses and obtainment requirements only —");
        if (totalPages > 1) {
            newLore.add("§6Page: " + (page + 1) + "/" + totalPages); // 多页时才显示页码
        }
        return newLore;
    }

    /**
     * 设置分类页面分页导航
     */
    private static void setupCategoryPagination(ChestMenu menu, String category, int currentPage, int totalPages) {
        // 返回按钮放在49号槽位
        menu.addItem(49, BACK_BUTTON);
        menu.addMenuClickHandler(49, (p, s, i, c) -> {
            openMainMenu(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        // 只有一页时不显示分页按钮,用屏障占位
        if (totalPages <= 1) {
            // 上一页位置放屏障
            menu.addItem(48, createItemWithLore(Material.BARRIER, "§8"));
            menu.addMenuClickHandler(48, (p, s, i, c) -> false);

            // 下一页位置放屏障
            menu.addItem(50, createItemWithLore(Material.BARRIER, "§8"));
            menu.addMenuClickHandler(50, (p, s, i, c) -> false);
            return;
        }

        // 上一页按钮 (放在48号槽位)
        if (currentPage > 0) {
            ItemStack prevButton = createItemWithLore(
                    Material.ARROW,
                    "§6← Previous Page",
                    "§7Click to view the previous page.",
                    "§8Page: " + (currentPage + 1) + " / " + totalPages
            );
            menu.addItem(48, prevButton);
            menu.addMenuClickHandler(48, (p, s, i, c) -> {
                openCategoryPage(p, category, currentPage - 1);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return false;
            });
        } else {
            // 第一页时显示屏障
            menu.addItem(48, createItemWithLore(Material.BARRIER, "§8"));
            menu.addMenuClickHandler(48, (p, s, i, c) -> false);
        }

        // 下一页按钮 (放在50号槽位)
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = createItemWithLore(
                    Material.ARROW,
                    "§6Next Page →",
                    "§7Click to view the next page.",
                    "§8Page: " + (currentPage + 1) + " / " + totalPages
            );
            menu.addItem(50, nextButton);
            menu.addMenuClickHandler(50, (p, s, i, c) -> {
                openCategoryPage(p, category, currentPage + 1);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return false;
            });
        } else {
            // 最后一页时显示屏障
            menu.addItem(50, createItemWithLore(Material.BARRIER, "§8"));
            menu.addMenuClickHandler(50, (p, s, i, c) -> false);
        }
    }

    /**
     * 设置分类页面边框装饰
     */
    private static void setupCategoryBorders(ChestMenu menu, String title) {
        // 四个角
        menu.addItem(0, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(0, (p, slot, item, click) -> false);
        menu.addItem(8, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(8, (p, slot, item, click) -> false);
        menu.addItem(45, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(45, (p, slot, item, click) -> false);
        menu.addItem(53, createDecorItem(CORNER_GLASS, "§b✨"));
        menu.addMenuClickHandler(53, (p, slot, item, click) -> false);


        // 上边框
        for (int i = 1; i < 8; i++) {
            menu.addItem(i, createDecorItem(BORDER_GLASS, "§9" + title));
            menu.addMenuClickHandler(i, (p, slot, item, click) -> false);
        }

        // 下边框 - 跳过48,49,50槽位(用于分页按钮)
        for (int i = 46; i < 53; i++) {
            if (i == 48 || i == 49 || i == 50) continue; // 跳过分页按钮位置
            menu.addItem(i, createDecorItem(BORDER_GLASS, "§bCategory: " + title));
            menu.addMenuClickHandler(i, (p, slot, item, click) -> false);
        }

        // 左边框
        int[] leftBorder = {9, 18, 27, 36};
        for (int slot : leftBorder) {
            menu.addItem(slot, createDecorItem(BORDER_GLASS, "§9📖"));
            menu.addMenuClickHandler(slot, (p, slot1, item, click) -> false);
        }

        // 右边框
        int[] rightBorder = {17, 26, 35, 44};
        for (int slot : rightBorder) {
            menu.addItem(slot, createDecorItem(BORDER_GLASS, "§9📚"));
            menu.addMenuClickHandler(slot, (p, slot1, item, click) -> false);
        }
    }

    /**
     * 获取分类标题
     */
    private static String getCategoryTitle(String category) {
        return switch (category) {
            case "common_fish" -> "Common Fish";
            case "uncommon_fish" -> "Uncommon Fish";
            case "rare_fish" -> "Rare Fish";
            case "epic_fish" -> "Epic Fish";
            case "legendary_fish" -> "Legendary Fish";
            case "mythical_fish" -> "Mythical Fish";
            case "junk" -> "Junk";
            case "magic_sugar" -> "Magic Sugar Catches";
            case "bread" -> "Bread Catches";
            default -> "Unknown Category";
        };
    }

    /**
     * 获取分类物品
     */
    private static List<ItemStack> getItemsByCategory(String category) {
        List<ItemStack> items = new ArrayList<>();

        switch (category) {
            case "common_fish":
                // 添加常见鱼类
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SanWenFish,"",getGradientName("It can be caught with any bait.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.XueFish,"",getGradientName("It can be caught with any bait.")));
                break;
            case "uncommon_fish":
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.HeTun,"",getGradientName("It can be caught with any bait.")));
                break;
            case "rare_fish":
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.ReDaiFish,"",getGradientName("It can be caught with any bait.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.CopperDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.GoldDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.IronDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.TinDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SilverDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.AluminumDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.LeadDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.ZincDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.MagnesiumDustFish,"",getGradientName("Preferred Bait: Mixed Mineral Dust")));
                items.add(FishManager.getFishItemWithLore(BaseFish.CoalFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.EmeraldFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.LapisFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.DiamondFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.QuartzFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.AmethystFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.IronFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.GoldFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.CopperFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.RedstoneFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.NetheriteFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.GlowStoneDustFish,"",getGradientName("Preferred Bait: Mixed Minerals")));
                items.add(FishManager.getFishItemWithLore(BaseFish.ShuLingYu,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SulfateFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SiliconFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.UraniumFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.OilRockFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                break;
            case "epic_fish":
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.FoamCrystalFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.BlackDiamondFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.EnchantedBottleFish,"",getGradientName("Preferred Bait: Basic Magic Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.MYSTIC_EEL,"",getGradientName("Preferred Bait: Any Bait"),getGradientName("Favors more powerful magic fishing rods.")));
                //合金锭鱼
                items.add(FishManager.getFishItemWithLore(BaseFish.ReinforcedAlloyFish,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry"),getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.HardenedMetalFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.DamascusSoulFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SteelSoulFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.BronzeAncientFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.HardlightAluFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SilverCopperFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.BrassResonanceFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.AluminumBrassFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.AluminumBronzeFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.CorinthianBronzeFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SolderFlowFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.NickelSpiritFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.CobaltFlameFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.SiliconIronFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.CarbonSoulFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.GildedIronFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.RedstoneAlloyFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.NeptuniumShadowFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.PlutoniumCoreFish, "", getGradientName("Exclusive Bait: Mixed Alloy Slurry"), getGradientName("Favors more powerful magic fishing rods.")));
                break;
            case "legendary_fish":
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                items.add(FishManager.getFishItemWithLore(BaseFish.LegendaryLuFish,"",getGradientName("Preference: Entangled Knot — Thread of the End, Paradox Hook")));
                items.add(FishManager.getFishItemWithLore(BaseFish.LegendaryEelFish,"",getGradientName("Exclusive Bait: Magic Sugar")));
                break;
            case "mythical_fish":
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                break;
            case "junk":
                items.add(new CustomItemStack(Material.COD,getGradientName("About Fish Preferences"),getGradientName("A preference means this fish is more attracted to that bait."),getGradientName("Other bait can still catch this fish at a lower chance."),getGradientName("Some bait may not be able to catch it at all.")));
                items.add(new CustomItemStack(new ItemStack(Material.PRISMARINE_SHARD,1),getGradientNameVer2("Bait: Memory Fragment"),
                        ("§fThis bait can catch almost anything."),("§fIt exists somewhere in the past—or the future."),("§fWhat you see now may not be its true form."),
                        "",getGradientName("Exclusive Rod: Log Fishing Rod"),getGradientName("Exclusive Rod: Windwhisper Rod"),getGradientName("Exclusive Rod: Entangled Knot — Thread of the End, Paradox Hook")));
                items.add(new CustomItemStack(new ItemStack(Material.COD),"§bLost Raw Cod",getGradientName("Whose cod is this?"),
                        "",getGradientName("Exclusive Bait: Bread")));
                items.add(new CustomItemStack(new ItemStack(Material.SALMON),"§bDizzy Raw Salmon",getGradientName("Have you heard of the Salmon Return Formation?"),
                        "",getGradientName("Exclusive Bait: Bread")));
                items.add(new CustomItemStack(new ItemStack(Material.TROPICAL_FISH),"§bA 1.4 Tropical Fish",getGradientName("How did a tropical fish reach a midstream river?"),
                        "",getGradientName("Exclusive Bait: Bread")));
                items.add(new CustomItemStack(new ItemStack(Material.PUFFERFISH),"§bGreenish Pufferfish",getGradientName("You really should not eat this carelessly."),
                        "",getGradientName("Exclusive Bait: Bread")));
                items.add(new CustomItemStack(new ItemStack(Material.POTION),"§bMysterious Potion",getGradientName("Maybe it is safe to drink?"),
                        "",getGradientName("Exclusive Bait: Bread")));
                items.add(new CustomItemStack(new ItemStack(Material.HONEY_BOTTLE,15),"§eHoney Bottles",getGradientName("This represents 15 vanilla Honey Bottles."),
                        "",getGradientName("Exclusive Bait: Bread")));

                items.add(new CustomItemStack(Material.GLOW_BERRIES, "§aFirefly Grass Spike", getGradientName("It glows faintly at night and is said to lure curious fish."),
                        "",getGradientName("Exclusive Bait: Basic Magic Bait")));
                items.add(new CustomItemStack(Material.MOSS_CARPET, "§aWater Moss Tuft", getGradientName("Soft and damp, a favorite hiding place for small fish."),
                        "",getGradientName("Exclusive Bait: Basic Magic Bait")));
                items.add(new CustomItemStack(Material.SLIME_BALL, "§aCroaking Shell", getGradientName("A gentle squeeze makes it croak, fooling fish into hearing one of their own."),
                        "",getGradientName("Exclusive Bait: Basic Magic Bait")));
                items.add(new CustomItemStack(Material.POPPY, "§aDewdrop Lotus Petal", getGradientName("Its fresh morning scent is said to cleanse murky water."),
                        "",getGradientName("Exclusive Bait: Basic Magic Bait")));
                items.add(new CustomItemStack(Material.PRISMARINE_SHARD, "§aFish Scale Dust", ("§fIt glimmers in sunlight as a recognition signal among schools of fish."),
                        "",getGradientName("Exclusive Bait: Basic Magic Bait")));

                items.add(new CustomItemStack(Material.RED_SAND, "§6Ground Copper Sand", getGradientName("Its faint metallic sheen acts as a beacon for Copper Vein Fish."),
                        "",getGradientName("Exclusive Bait: Mixed Mineral Dust")));
                items.add(new CustomItemStack(Material.RED_DYE, "§6Rust Powder", getGradientName("Rust scraped from abandoned machinery; mineral fish sense it as one of their own."),
                        "",getGradientName("Exclusive Bait: Mixed Mineral Dust")));
                items.add(new CustomItemStack(Material.GLOW_INK_SAC, "§6Gold Dust Residue", getGradientName("Tailings left after panning, still carrying the magic of a rich ore deposit."),
                        "",getGradientName("Exclusive Bait: Mixed Mineral Dust")));
                items.add(new CustomItemStack(Material.QUARTZ, "§6Quartz Shards", getGradientName("Crystal fragments from Nether veins that stabilize the bait's energy field."),
                        "",getGradientName("Exclusive Bait: Mixed Mineral Dust")));
                items.add(new CustomItemStack(Material.COAL, "§6Carbon Crystal Granules", getGradientName("Ancient plant remains from deep underground that provide an energetic base."),
                        "",getGradientName("Exclusive Bait: Mixed Mineral Dust")));
                items.add(new CustomItemStack(Material.NETHER_STAR, "§dStellar Iron Dust", ("§fExtremely rare dust said to come from meteor cores and greatly improve attraction."),
                        "",getGradientName("Exclusive Bait: Mixed Mineral Dust")));

                items.add(new CustomItemStack(Material.COPPER_INGOT, "§bNative Copper Vein Fragment", getGradientName("A naturally conductive ore network stripped directly from rock rather than smelted."),
                        "",getGradientName("Exclusive Bait: Mixed Minerals")));
                items.add(new CustomItemStack(Material.IRON_INGOT, "§bHematite Core", ("§fA high-purity iron core whose intact crystal lattice emits a metallic heartbeat."),
                        "",getGradientName("Exclusive Bait: Mixed Minerals")));
                items.add(new CustomItemStack(Material.GOLD_INGOT, "§bGold Vein Crystal", getGradientName("A web-like gold crystal formed under pressure, serving as a living beacon of rich ore."),
                        "",getGradientName("Exclusive Bait: Mixed Minerals")));
                items.add(new CustomItemStack(Material.AMETHYST_SHARD, "§bDeep Rock Crystal Core", getGradientName("A resonant deep-earth crystal that amplifies the range of ore signals."),
                        "",getGradientName("Exclusive Bait: Mixed Minerals")));
                items.add(new CustomItemStack(Material.COAL_BLOCK, "§bLava Carbon Heart", getGradientName("A wood core carbonized beside lava for centuries, stabilizing ore-core activity with geothermal energy."),
                        "",getGradientName("Exclusive Bait: Mixed Minerals")));
                items.add(new CustomItemStack(Material.NETHER_STAR, "§5Star Core Fragment", getGradientName("A fragment said to come from a fallen star, able to imitate core-level ore signals."),
                        "",getGradientName("Exclusive Bait: Mixed Minerals")));

                items.add(new CustomItemStack(new ItemStack(Material.SUGAR_CANE,2),"§bRotten Sugar Cane",getGradientName("How did sugar cane end up in the river?"),
                        "",getGradientName("Exclusive Bait: Jiang Taigong's Fishing")));
                items.add(new CustomItemStack(new ItemStack(Material.STICK,2),"§bMojibake",getGradientName("What even is this thing?"),
                        "",getGradientName("Exclusive Bait: Jiang Taigong's Fishing")));
                items.add(new CustomItemStack(new ItemStack(Material.INK_SAC,2),"§bFresh Ink Sac",getGradientName("Who throws a perfectly good ink sac into a river?"),
                        "",getGradientName("Exclusive Bait: Jiang Taigong's Fishing")));
                items.add(new CustomItemStack(new ItemStack(Material.CAKE,2),"§bDay-Old Cake",getGradientName("Could not finish the cake?"),
                        "",getGradientName("Exclusive Bait: Jiang Taigong's Fishing")));
                items.add(new CustomItemStack(new ItemStack(Material.REDSTONE,8),"§b8-bit Redstone",getGradientName("Exactly eight in one handful?"),
                        "",getGradientName("Exclusive Bait: Jiang Taigong's Fishing")));
                items.add(new CustomItemStack(new ItemStack(Material.DISPENSER,2),"§bA Dispenser Slimefun Needs",getGradientName("Should you just place it on the ground?"),
                        "",getGradientName("Exclusive Bait: Jiang Taigong's Fishing")));


                items.add(new CustomItemStack(new ItemStack(Material.BOWL),"§6Toilet Seat",getGradientName("A sacred ring from an ancient cleansing rite: open to the spirit realm, closed against filth."), getGradientName("Mortals do not know it once guarded a god's most private moments.")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(new ItemStack(Material.YELLOW_DYE),"§eBanana Peel",getGradientName("It has toppled three kings, two unicorns, and one adventurer who claimed never to fall."),getGradientName("Legend says it came from a golden tree smiling under moonlight, born for fate's stumbles.")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(new ItemStack(Material.IRON_SHOVEL),"§aPlunger",getGradientName("A copy of the Abyssal Soul-Sucker; every plunge seals whispers from the sewer below."),getGradientName("The truly strong use it not only on pipes, but on the spirit realm."),getGradientName("ber~ber~ber~")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(CustomHead.getHead("1421f1514da756c8c6c7c0b83a79265c26c9ece66b3bad8fbd94bd96d7040d7e"),"§bMoray Eel",getGradientName("A living electric whip from deep-sea rifts, carrying the remnant of an ancient thunder god."),getGradientName("Fishers call it \"Wrath of the Black Tide\"; touching it causes convulsions and sparks.")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(CustomHead.getHead("a1f71182915f5f862189a81f690acde4f671075db267eb6128fd1b4a84da8d7c"),"§cLengshang's Wheelchair",getGradientName("Legendary gear made for idle players; equip it and even sleep through the final boss."),getGradientName("— You are not overpowered; the wheelchair carried every difficulty for you.")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(new ItemStack(Material.COCOA_BEANS),"§cPoop"
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(CustomHead.MAGICSOLO.getItem(),getGradientName("magicsolo"),getGradientName("Every dream must end; this fleeting life is empty as a dream."),getGradientName("A fleeting dream of life, dreaming neither the past nor the present.")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));
                items.add(new CustomItemStack(new ItemStack(Material.GOLDEN_SHOVEL),"§eGolden Spatula",getGradientName("Were you hoping for another unit?"),getGradientName("Or perhaps an emblem?")
                        ,"",getGradientName("Exclusive Bait: Mixed Alloy Slurry")));




                break;
            case "magic_sugar":
                items.add(new CustomItemStack(Material.SUGAR,("§6Magic Sugar")," ",("§a§oFeel the power of Hermes!")));
                items.add(MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1);
                items.add(SlimefunItems.MAGIC_LUMP_1);
                items.add(SlimefunItems.MAGIC_LUMP_2);
                items.add(SlimefunItems.MAGIC_LUMP_3);
                items.add(SlimefunItems.ENDER_LUMP_1);
                items.add(SlimefunItems.ENDER_LUMP_2);
                items.add(SlimefunItems.ENDER_LUMP_3);
                items.add(SlimefunItems.MAGICAL_GLASS);
                items.add(SlimefunItems.MAGICAL_BOOK_COVER);
                items.add(SlimefunItems.LAVA_CRYSTAL);
                items.add(SlimefunItems.COMMON_TALISMAN);
                items.add(SlimefunItems.NECROTIC_SKULL);
                items.add(SlimefunItems.ESSENCE_OF_AFTERLIFE);
                items.add(SlimefunItems.SYNTHETIC_SHULKER_SHELL);
                items.add(SlimefunItems.BLANK_RUNE);
                items.add(SlimefunItems.AIR_RUNE);
                items.add(SlimefunItems.EARTH_RUNE);
                items.add(SlimefunItems.FIRE_RUNE);
                items.add(SlimefunItems.WATER_RUNE);
                items.add(SlimefunItems.ENDER_RUNE);
                items.add(SlimefunItems.LIGHTNING_RUNE);
                items.add(SlimefunItems.RAINBOW_RUNE);
                items.add(SlimefunItems.SOULBOUND_RUNE);
                items.add(SlimefunItems.ENCHANTMENT_RUNE);
                items.add(SlimefunItems.VILLAGER_RUNE);
                items.add(SlimefunItems.STRANGE_NETHER_GOO);
                items.add(SlimefunItems.RAINBOW_LEATHER);
                items.add(MagicExpansionItems.RANDOM_FISH_COMMON);
                items.add(MagicExpansionItems.RANDOM_FISH_UNCOMMON);
                items.add(MagicExpansionItems.RANDOM_FISH_RARE_POOL_DUST);
                items.add(MagicExpansionItems.RANDOM_FISH_RARE_POOL_ORE);
                items.add(MagicExpansionItems.RANDOM_FISH_RARE_POOL_INDUSTRY);
                items.add(MagicExpansionItems.RANDOM_FISH_EPIC_POOL_INDUSTRY);
                items.add(MagicExpansionItems.RANDOM_FISH_EPIC);
                items.add(FishManager.getFishItemWithLore(BaseFish.LegendaryEelFish,"",getGradientName("Exclusive Bait: Magic Sugar")));
                items.add(new CustomItemStack(
                        new ItemStack(Material.PRISMARINE_SHARD, 1),
                        getGradientNameVer2("Bait: Memory Fragment"),
                        ("§fThis bait can catch almost anything."),
                        ("§fIt exists somewhere in the past—or the future."),
                        ("§fWhat you see now may not be its true form.")
                ));
                break;
            case "bread":
                items.add(new CustomItemStack(Material.BREAD,("§bBread")," ",getGradientName("This is just an ordinary piece of bread.")));
                items.add(new CustomItemStack(new ItemStack(Material.COD, 3), "§bLost Raw Cod", getGradientName("Whose cod is this?")));
                items.add(new CustomItemStack(new ItemStack(Material.SALMON, 3), "§bDizzy Raw Salmon", getGradientName("Have you heard of the Salmon Return Formation?")));
                items.add(new CustomItemStack(new ItemStack(Material.TROPICAL_FISH, 3), "§bA 1.4 Tropical Fish", getGradientName("How did a tropical fish reach a midstream river?")));
                items.add(new CustomItemStack(new ItemStack(Material.PUFFERFISH, 3), "§bGreenish Pufferfish", getGradientName("You really should not eat this carelessly.")));
                items.add(new CustomItemStack(new ItemStack(Material.POTION, 3), "§bMysterious Potion", getGradientName("Maybe it is safe to drink?")));
                items.add(new ItemStack(Material.HONEY_BOTTLE, 256));
                items.add(MagicExpansionItems.RANDOM_FISH_COMMON);
                items.add(MagicExpansionItems.RANDOM_FISH_UNCOMMON);
                items.add(MagicExpansionItems.RANDOM_FISH_EPIC);
                items.add(MagicExpansionItems.RANDOM_FISH_EPIC_POOL_INDUSTRY);
                items.add(MagicExpansionItems.RANDOM_FISH_RARE_POOL_DUST);
                items.add(MagicExpansionItems.RANDOM_FISH_RARE_POOL_ORE);
                items.add(MagicExpansionItems.RANDOM_FISH_RARE_POOL_INDUSTRY);
                items.add(new CustomItemStack(
                        new ItemStack(Material.PRISMARINE_SHARD, 1),
                        getGradientNameVer2("Bait: Memory Fragment"),
                        ("§fThis bait can catch almost anything."),
                        ("§fIt exists somewhere in the past—or the future."),
                        ("§fWhat you see now may not be its true form.")
                ));
                break;
            default:
                items.add(new ItemStack(Material.BARRIER));
                break;
        }

        return items;
    }
}
