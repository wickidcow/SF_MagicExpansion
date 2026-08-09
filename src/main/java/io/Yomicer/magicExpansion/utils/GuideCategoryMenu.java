package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.MagicExpansionItemSetup;
import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.items.groups.VirtualGuideGroup;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.CustomHeadUtils.CustomHead;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.chat.ChatInput;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.List;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

/**
 * 魔法2.0 自绘分类页 + 三级菜单
 * UI 完全仿照原生粘液书页面:顶部 createHeader,槽1 返回,槽9~44 分组图标(每页36个),槽46/52 翻页
 * 返回链:每打开一层都写入 GuideHistory,返回按钮左键逐级返回(goBack)、Shift 回主菜单
 */
public final class GuideCategoryMenu {

    private static final int ITEMS_PER_PAGE = 36;
    private static final int FIRST_SLOT = 9;
    private static final int LAST_SLOT = 44;

    private GuideCategoryMenu() {
    }

    // ==================== 一级菜单:54格大箱子 ====================

    /** 白色填充槽位 */
    private static final int[] WHITE_SLOTS = {0, 2, 3, 5, 6, 8, 9, 17, 18, 26, 36, 44, 45, 53};
    /** 彩虹变色槽位(原品红色玻璃板) */
    private static final int[] RAINBOW_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34, 37, 43, 46, 47, 48, 50, 51, 52};
    /** 彩虹色序列(每秒循环) */
    private static final Material[] RAINBOW_PANES = {
            Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE
    };
    /** 蛇形渐变环绕顺序(顺时针, 跳过第49槽的神器小按钮) */
    private static final int[] RAINBOW_RING = {
            10, 11, 12, 13, 14, 15, 16,
            25, 34, 43,
            52, 51, 50, 48, 47, 46,
            37, 28, 19
    };
    /** 蛇形渐变转动速度(每2 tick 挪一格) */
    private static final long RAINBOW_STEP_TICKS = 2L;
    /** 与 RAINBOW_PANES 对应的色相(角度), 用于把渐变色映射到最接近的玻璃板 */
    private static final float[] RAINBOW_HUES = {
            0f, 30f, 60f, 90f, 120f, 180f, 195f, 240f, 270f, 300f, 330f
    };
    /** 大分组展示槽位(20-24,29-33,38-42) */
    private static final int[] BIG_GROUP_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42};
    /** 二级菜单白色填充槽位 */
    private static final int[] SECOND_WHITE_SLOTS = {0, 2, 3, 5, 6, 8, 9, 17, 36, 44, 45, 47, 48, 50, 51, 53};
    /** 二级菜单粉红色填充槽位 */
    private static final int[] SECOND_PINK_SLOTS = {10, 11, 12, 13, 14, 15, 16, 37, 38, 39, 40, 41, 42, 43};
    /** 二级菜单淡蓝色填充槽位 */
    private static final int[] SECOND_LIGHT_BLUE_SLOTS = {18, 26, 27, 35};
    /** 二级菜单品红色填充槽位(固定,不做彩虹) */
    private static final int[] SECOND_MAGENTA_SLOTS = {19, 25, 28, 34};
    /** 二级菜单分组槽位(20-24,29-33) */
    private static final int[] SECOND_GROUP_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    /** 四级菜单(物品列表)每页数量: 4x7 */
    private static final int ITEM_PAGE_SIZE = 28;
    /** 四级菜单物品槽位(4行x7列, 居中) */
    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    /** 四级菜单白色填充(顶行+底角) */
    private static final int[] ITEM_WHITE_SLOTS = {0, 2, 3, 5, 6, 8, 45, 53};
    /** 四级菜单物品区边框(淡蓝) */
    private static final int[] ITEM_FRAME_SLOTS = {9, 17, 18, 26, 27, 35, 36, 44};
    /** 四级菜单底部点缀(粉红) */
    private static final int[] ITEM_BOTTOM_SLOTS = {47, 48, 50, 51};
    /** 四级菜单历史锚点缓存(每个分组+页码一个稳定锚点, 保证返回不重复堆历史) */
    private static final Map<String, VirtualGuideGroup> ITEM_PAGE_ANCHORS = new HashMap<>();


    private static final Map<UUID, Integer> RAINBOW_TASKS = new HashMap<>();

    /** 开发者 magicsolo 头颅材质(与贡献组的 MAGIC_EXPANSION_AUTHOR 一致) */
    private static final ItemStack DEVELOPER_HEAD = CustomHead.getHead("8adb25ab9976d89d0bd8118d72c1c06bb907060c1e02a729b652d1e86b1ebbbc");

    /** 1-5 阶魔法糖 */
    private static final ItemStack[] MAGIC_SUGARS = {
            MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_1,
            MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_2,
            MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_3,
            MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_4,
            MagicExpansionItems.MAGIC_EXPANSION_MAGIC_SUGAR_5
    };

    /** 无名字的染色玻璃板(隐藏原版名称, 用单个空格代替) */
    private static ItemStack plainPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }
    /** 神奇的小按钮:10 个特殊事件随机触发,概率相同 */
    private static void triggerMysteryButton(Player player) {
        final Random random = new Random();
        switch (random.nextInt(10)) {
            case 0 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0));
                player.sendMessage(getGradientNameVer2("MC人能飞！"));
            }
            case 1 -> {
                player.setHealth(0.0);
                player.sendMessage(getGradientNameVer2(player.getName() + " 感受到了魔法"));
            }
            case 2 -> {
                ItemStack sugar = MAGIC_SUGARS[random.nextInt(MAGIC_SUGARS.length)].clone();
                Map<Integer, ItemStack> left = player.getInventory().addItem(sugar);
                if (!left.isEmpty()) {
                    player.getWorld().dropItem(player.getLocation(), left.get(0));
                }
                player.sendMessage(getGradientNameVer2("来吃点糖吧"));
            }
            case 3 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                player.sendMessage(getGradientNameVer2("堕入黑暗吧！"));
            }
            case 4 -> {
                // 饱餐一顿
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 0));
                player.sendMessage(getGradientNameVer2("烟火入腹，人间忽而温柔。"));
            }
            case 5 -> {
                // 空间错位: 周围 5~15 格随机传送(落点取安全高度)
                Location loc = player.getLocation();
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = 5 + random.nextDouble() * 10;
                double x = loc.getX() + Math.cos(angle) * dist;
                double z = loc.getZ() + Math.sin(angle) * dist;
                int y = loc.getWorld().getHighestBlockYAt((int) Math.floor(x), (int) Math.floor(z)) + 1;
                player.teleport(new Location(loc.getWorld(), x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch()));
                player.sendMessage(getGradientNameVer2("袖底生风，山河退让半步。"));
            }
            case 6 -> {
                // 放一场烟花
                Firework firework = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                Color color = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                meta.addEffect(FireworkEffect.builder()
                        .with(FireworkEffect.Type.BURST)
                        .withColor(color)
                        .withFade(Color.WHITE)
                        .build());
                meta.setPower(1);
                firework.setFireworkMeta(meta);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        firework.detonate();
                    }
                }.runTaskLater(MagicExpansion.getInstance(), 2L);
                player.sendMessage(getGradientNameVer2("万籁俱寂，独此一瞬盛放。"));
            }
            case 7 -> {
                // 音效合唱: 从原版全部音效里随机播一种
                Sound[] sounds = Sound.values();
                Sound sound = sounds[random.nextInt(sounds.length)];
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f + random.nextFloat());
                player.sendMessage(getGradientNameVer2("哪来的B动静？"));
            }
            case 8 -> {
                // 按钮低语: 只低语一句, 不再额外提示
                String[] whispers = {
                        "你踩到了时间的衣角。",
                        "今晚的月亮是甜的。",
                        "风把某个名字，吹到了你耳边。",
                        "有些门，只为回头的人开着。",
                        "它记得，你上一世也按过它。",
                        "海在很远的地方，替你翻了个身。"
                };
                player.sendMessage(getGradientNameVer2(whispers[random.nextInt(whispers.length)]));
            }
            case 9 -> {
                // 钓鱼佬的馈赠: 随机 1~3 种鱼饵(织梦者+水云间), 每种 1~5 个
                ItemStack[] baits = {
                        MagicExpansionItems.FISH_LURE_BASIC,
                        MagicExpansionItems.FISH_LURE_DUST,
                        MagicExpansionItems.FISH_LURE_ORE,
                        MagicExpansionItems.FISH_LURE_ALLOY_INGOT,
                        MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_CUIXIA,
                        MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_WEICHEN,
                        MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_RONGHUO,
                        MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_YUEJIN,
                        MagicExpansionItems.FISH_LURE_BETWEEN_WATER_CLOUD_XINGHE
                };
                List<ItemStack> pool = new ArrayList<>(List.of(baits));
                Collections.shuffle(pool, random);
                int kinds = 1 + random.nextInt(3);
                for (int i = 0; i < kinds && i < pool.size(); i++) {
                    int amount = 1 + random.nextInt(5);
                    ItemStack bait = pool.get(i).clone();
                    bait.setAmount(amount);
                    Map<Integer, ItemStack> left = player.getInventory().addItem(bait);
                    if (!left.isEmpty()) {
                        player.getWorld().dropItem(player.getLocation(), left.get(0));
                    }
                }
                player.sendMessage(getGradientNameVer2("这是钓鱼佬的馈赠"));
            }
            default -> { }
        }
    }
    /** 一级菜单:点击魔法2.0进入 */
    public static void openCategoryPage(Player player, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        addHistory(profile, mode, MagicExpansionItemSetup.magicexpansion, page);

        ChestMenu menu = createMainMenu(player, mode);

        // 白色填充
        for (int slot : WHITE_SLOTS) {
            menu.addItem(slot, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        // 彩虹填充(初始品红,打开后每秒变色)
        for (int slot : RAINBOW_SLOTS) {
            menu.addItem(slot, plainPane(Material.MAGENTA_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 1槽:返回(回主菜单)
        menu.addItem(1, ChestMenuUtils.getBackButton(player));
        menu.addMenuClickHandler(1, (p, s, it, a) -> {
            new SurvivalSlimefunGuide().openMainMenu(profile, profile.getGuideHistory().getMainMenuPage());
            return false;
        });

        // 7槽:原版搜索
        menu.addItem(7, ChestMenuUtils.getSearchButton(player));
        menu.addMenuClickHandler(7, (p, s, it, a) -> {
            p.closeInventory();
            Slimefun.getLocalization().sendMessage(p, "guide.search.message");
            ChatInput.waitForPlayer(MagicExpansion.getInstance(), p, message -> {
                SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
                guide.openSearch(profile, message, false);
            });
            return false;
        });

        // 4槽:附属信息(开发者头颅材质,点击由监听器打开二级菜单)
        menu.addItem(4, createVirtualIcon(DEVELOPER_HEAD, ColorGradient.getRandomGradientName("附属信息"), "点击查看贡献与更新日志", "attachmentinfo"));
        menu.addMenuClickHandler(4, (p, s, it, a) -> {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        // 大分组展示(按当前顺序:容器组 + 平铺组,剩余用屏障占位)
        List<ItemGroup> bigGroups = GuideMenuGroups.getTopLevel();
        for (int i = 0; i < BIG_GROUP_SLOTS.length; i++) {
            int slot = BIG_GROUP_SLOTS[i];
            if (i < bigGroups.size()) {
                ItemGroup group = bigGroups.get(i);
                ItemStack icon = group.getItem(player);
                menu.addItem(slot, icon);
                menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    // 容器组由 GuideVirtualGroupClickListener 拦截处理;平铺组直接打开原生物品页
                    if (getVirtualGroupId(it) == null) {
                        openItemGroupPage(p, profile, mode, group, 1);
                    }
                    return false;
                });
            } else {
                menu.addItem(slot, new CustomItemStack(Material.BARRIER, getGradientNameVer2("敬请期待")), ChestMenuUtils.getEmptyClickHandler());
            }
        }

        // 翻页:27上一页 / 35下一页(目前一页,功能已写好)
        // 翻页:27上一页 / 35下一页(无页可翻时显示白色玻璃板)
        int pages = Math.max(1, (bigGroups.size() + BIG_GROUP_SLOTS.length - 1) / BIG_GROUP_SLOTS.length);
        if (page > 1) {
            menu.addItem(27, ChestMenuUtils.getPreviousButton(player, page, pages));
            menu.addMenuClickHandler(27, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openCategoryPage(p, profile, mode, page - 1);
                return false;
            });
        } else {
            menu.addItem(27, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        if (page < pages) {
            menu.addItem(35, ChestMenuUtils.getNextButton(player, page, pages));
            menu.addMenuClickHandler(35, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openCategoryPage(p, profile, mode, page + 1);
                return false;
            });
        } else {
            menu.addItem(35, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        // 49槽:神器小按钮(功能待定,后续单独文件存放)
        menu.addItem(49, new CustomItemStack(Material.NETHER_STAR, getGradientNameVer2("神奇的小按钮"), getGradientNameVer2("点一下会发生什么？")));
        menu.addMenuClickHandler(49, (p, s, it, a) -> {
            triggerMysteryButton(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        menu.open(player);
        startRainbow(player, menu);
    }

    private static ChestMenu createMainMenu(Player player, SlimefunGuideMode mode) {
        String title = Slimefun.getLocalization().getMessage(player, "guide.title.main");
        ChestMenu menu = new ChestMenu(title);
        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f));
        return menu;
    }

    /** 彩虹玻璃板:蛇形渐变环绕, 每2 tick 顺时针挪一格 */
    private static void startRainbow(Player player, ChestMenu menu) {
        cancelRainbow(player);
        final int[] step = {0};
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    RAINBOW_TASKS.remove(player.getUniqueId());
                    return;
                }
                int current = step[0]++;
                for (int p = 0; p < RAINBOW_RING.length; p++) {
                    int slot = RAINBOW_RING[p];
                    int colorIndex = Math.floorMod(p - current, RAINBOW_RING.length);
                    menu.replaceExistingItem(slot, plainPane(rainbowGradientMaterial(colorIndex, RAINBOW_RING.length)));
                }
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, RAINBOW_STEP_TICKS);
        RAINBOW_TASKS.put(player.getUniqueId(), task.getTaskId());
    }

    /** 把环上的等距位置映射为最接近的玻璃板渐变色(19格顺滑衔接头尾) */
    private static Material rainbowGradientMaterial(int index, int size) {
        float hue = index * 360f / size;
        int best = 0;
        float bestDiff = Float.MAX_VALUE;
        for (int i = 0; i < RAINBOW_HUES.length; i++) {
            float diff = Math.abs(RAINBOW_HUES[i] - hue);
            diff = Math.min(diff, 360f - diff);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = i;
            }
        }
        return RAINBOW_PANES[best];
    }

    /** 停止玩家的彩虹任务(菜单关闭时调用) */
    public static void cancelRainbow(Player player) {
        Integer id = RAINBOW_TASKS.remove(player.getUniqueId());
        if (id != null) {
            Bukkit.getScheduler().cancelTask(id);
        }
    }
    public static final NamespacedKey VIRTUAL_KEY = new NamespacedKey(MagicExpansion.getInstance(), "guide_virtual");

    /** 创建带虚拟分组标记的图标(用于原生分组页显示容器组,点击由监听器拦截) */
    public static ItemStack createVirtualIcon(ItemStack icon, String name, String lore, String id) {
        CustomItemStack base = new CustomItemStack(icon, name, lore);
        ItemMeta meta = base.getItemMeta();
        meta.getPersistentDataContainer().set(VIRTUAL_KEY, PersistentDataType.STRING, id);
        base.setItemMeta(meta);
        return base;
    }

    /** 读取虚拟分组标记,非虚拟分组返回 null */
    public static String getVirtualGroupId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(VIRTUAL_KEY, PersistentDataType.STRING);
    }

    /** 按标记打开对应的自绘菜单(容器组查注册表, 平铺组无虚拟标记) */
    public static void openVirtualGroup(Player player, PlayerProfile profile, SlimefunGuideMode mode, String id) {
        if (id == null) return;
        if (GuideMenuGroups.isContainer(id)) {
            openContainer(player, profile, mode, id);
        }
    }

    /* ==================== 容器组的子分组页(统一查注册表) ==================== */

    /** 打开任意容器组的子级页面(层级由 GuideMenuGroups 配置决定) */
    public static void openContainer(Player player, PlayerProfile profile, SlimefunGuideMode mode, String id) {
        VirtualGuideGroup anchor = GuideMenuGroups.getAnchor(id);
        if (anchor == null) return;
        openSecondLevel(player, profile, mode, 1, anchor, GuideMenuGroups.getChildren(id));
    }

    /** 二级菜单:容器组点开后的页面(54格大箱子) */
    private static void openSecondLevel(Player player, PlayerProfile profile, SlimefunGuideMode mode, int page,
                                        ItemGroup historyAnchor, List<ItemGroup> groups) {
        addHistory(profile, mode, historyAnchor, page);
        ChestMenu menu = createMainMenu(player, mode);

        // 固定装饰
        for (int slot : SECOND_WHITE_SLOTS) {
            menu.addItem(slot, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : SECOND_PINK_SLOTS) {
            menu.addItem(slot, plainPane(Material.PINK_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : SECOND_LIGHT_BLUE_SLOTS) {
            menu.addItem(slot, plainPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : SECOND_MAGENTA_SLOTS) {
            menu.addItem(slot, plainPane(Material.MAGENTA_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 1槽:返回(上一级)
        addBack(menu, profile, mode);

        // 4槽:作者头颅(纯展示,无点击)
        menu.addItem(4, new CustomItemStack(DEVELOPER_HEAD, getGradientNameVer2("magicsolo"), getGradientNameVer2("这是魔法作者")), ChestMenuUtils.getEmptyClickHandler());

        // 7槽:原版搜索
        menu.addItem(7, ChestMenuUtils.getSearchButton(player));
        menu.addMenuClickHandler(7, (p, s, it, a) -> {
            p.closeInventory();
            Slimefun.getLocalization().sendMessage(p, "guide.search.message");
            ChatInput.waitForPlayer(MagicExpansion.getInstance(), p, message -> {
                SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
                guide.openSearch(profile, message, false);
            });
            return false;
        });

        // 二级分组槽位(20-24,29-33)
        for (int i = 0; i < SECOND_GROUP_SLOTS.length; i++) {
            int slot = SECOND_GROUP_SLOTS[i];
            if (i < groups.size()) {
                ItemGroup group = groups.get(i);
                menu.addItem(slot, group.getItem(player));
                menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    if (group instanceof FlexItemGroup) {
                        // 容器/虚拟组: 交给原生调度(FlexItemGroup.open -> 自绘三级页)
                        SlimefunGuide.openItemGroup(profile, group, mode, 1);
                    } else {
                        openItemGroupPage(p, profile, mode, group, 1);
                    }
                    return false;
                });
            } else {
                menu.addItem(slot, new CustomItemStack(Material.BARRIER, getGradientNameVer2("敬请期待")), ChestMenuUtils.getEmptyClickHandler());
            }
        }

        // 翻页:46上一页 / 52下一页(无页可翻=白色,有页可翻=黄绿色)
        int pages = Math.max(1, (groups.size() + SECOND_GROUP_SLOTS.length - 1) / SECOND_GROUP_SLOTS.length);
        if (page > 1) {
            menu.addItem(46, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(46, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openSecondLevel(p, profile, mode, page - 1, historyAnchor, groups);
                return false;
            });
        } else {
            menu.addItem(46, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        if (page < pages) {
            menu.addItem(52, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(52, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openSecondLevel(p, profile, mode, page + 1, historyAnchor, groups);
                return false;
            });
        } else {
            menu.addItem(52, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 49槽:神秘按钮(与一级一致,功能待定)
        menu.addItem(49, new CustomItemStack(Material.NETHER_STAR, getGradientNameVer2("神奇的小按钮"), getGradientNameVer2("点一下会发生什么？")));
        menu.addMenuClickHandler(49, (p, s, it, a) -> {
            triggerMysteryButton(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        menu.open(player);
    }

    /** 三级菜单:嵌套容器组(织梦者/水云间等)页面, 风格与二级菜单一致 */
    private static void openThirdLevel(Player player, PlayerProfile profile, SlimefunGuideMode mode, int page,
                                       ItemGroup historyAnchor, List<ItemGroup> groups) {
        addHistory(profile, mode, historyAnchor, page);
        ChestMenu menu = createMainMenu(player, mode);

        // 固定装饰(与二级菜单一致: 白/粉/淡蓝/品红)
        for (int slot : SECOND_WHITE_SLOTS) {
            menu.addItem(slot, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : SECOND_PINK_SLOTS) {
            menu.addItem(slot, plainPane(Material.PINK_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : SECOND_LIGHT_BLUE_SLOTS) {
            menu.addItem(slot, plainPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : SECOND_MAGENTA_SLOTS) {
            menu.addItem(slot, plainPane(Material.MAGENTA_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 1槽:返回上一级
        addBack(menu, profile, mode);

        // 4槽:作者头颅(纯展示)
        menu.addItem(4, new CustomItemStack(DEVELOPER_HEAD, getGradientNameVer2("magicsolo"), getGradientNameVer2("这是魔法作者")), ChestMenuUtils.getEmptyClickHandler());

        // 7槽:原版搜索
        menu.addItem(7, ChestMenuUtils.getSearchButton(player));
        menu.addMenuClickHandler(7, (p, s, it, a) -> {
            p.closeInventory();
            Slimefun.getLocalization().sendMessage(p, "guide.search.message");
            ChatInput.waitForPlayer(MagicExpansion.getInstance(), p, message -> {
                SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
                guide.openSearch(profile, message, false);
            });
            return false;
        });

        // 三级分组槽位(20-24,29-33): 平铺组直接打开物品页, 容器组后续可继续下钻
        for (int i = 0; i < SECOND_GROUP_SLOTS.length; i++) {
            int slot = SECOND_GROUP_SLOTS[i];
            if (i < groups.size()) {
                ItemGroup group = groups.get(i);
                menu.addItem(slot, group.getItem(player));
                menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    if (group instanceof FlexItemGroup) {
                        // 容器/虚拟组: 交给原生调度(FlexItemGroup.open -> 自绘三级页)
                        SlimefunGuide.openItemGroup(profile, group, mode, 1);
                    } else {
                        openItemGroupPage(p, profile, mode, group, 1);
                    }
                    return false;
                });
            } else {
                menu.addItem(slot, new CustomItemStack(Material.BARRIER, getGradientNameVer2("敬请期待")), ChestMenuUtils.getEmptyClickHandler());
            }
        }

        // 翻页:46上一页 / 52下一页(无页=白色, 有页=黄绿)
        int pages = Math.max(1, (groups.size() + SECOND_GROUP_SLOTS.length - 1) / SECOND_GROUP_SLOTS.length);
        if (page > 1) {
            menu.addItem(46, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(46, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openThirdLevel(p, profile, mode, page - 1, historyAnchor, groups);
                return false;
            });
        } else {
            menu.addItem(46, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        if (page < pages) {
            menu.addItem(52, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(52, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openThirdLevel(p, profile, mode, page + 1, historyAnchor, groups);
                return false;
            });
        } else {
            menu.addItem(52, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 49槽:神奇的小按钮(与一二级一致)
        menu.addItem(49, new CustomItemStack(Material.NETHER_STAR, getGradientNameVer2("神奇的小按钮"), getGradientNameVer2("点一下会发生什么？")));
        menu.addMenuClickHandler(49, (p, s, it, a) -> {
            triggerMysteryButton(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        menu.open(player);
    }

    /* ==================== 四级菜单: 平铺组物品列表页(4x7) ==================== */

    /** 四级菜单:平铺组点开后的物品列表页, 风格延续二三级, 淡蓝边框包裹 4x7 物品格 */
    public static void openItemGroupPage(Player player, PlayerProfile profile, SlimefunGuideMode mode, ItemGroup group, int page) {
        if (group == null) return;
        if (group instanceof FlexItemGroup) {
            // 兜底: 容器/虚拟组没有物品列表, 交还原生调度(FlexItemGroup.open -> 自绘子级页)
            SlimefunGuide.openItemGroup(profile, group, mode, 1);
            return;
        }

        addHistory(profile, mode, itemPageAnchor(group, page, player), page);

        String title = group.getDisplayName(player);
        ChestMenu menu = new ChestMenu(title == null ? "物品列表" : title);
        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f));

        // 白色填充
        for (int slot : ITEM_WHITE_SLOTS) {
            menu.addItem(slot, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        // 淡蓝边框
        for (int slot : ITEM_FRAME_SLOTS) {
            menu.addItem(slot, plainPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        // 底部粉红点缀
        for (int slot : ITEM_BOTTOM_SLOTS) {
            menu.addItem(slot, plainPane(Material.PINK_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 1槽:返回上一级
        addBack(menu, profile, mode);

        // 4槽:作者头颅(纯展示)
        menu.addItem(4, new CustomItemStack(DEVELOPER_HEAD, getGradientNameVer2("magicsolo"), getGradientNameVer2("这是魔法作者")), ChestMenuUtils.getEmptyClickHandler());

        // 7槽:原版搜索
        menu.addItem(7, ChestMenuUtils.getSearchButton(player));
        menu.addMenuClickHandler(7, (p, s, it, a) -> {
            p.closeInventory();
            Slimefun.getLocalization().sendMessage(p, "guide.search.message");
            ChatInput.waitForPlayer(MagicExpansion.getInstance(), p, message -> {
                SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
                guide.openSearch(profile, message, false);
            });
            return false;
        });

        // 可见物品过滤(与原版一致: 禁用/隐藏/分组不可访问的不显示)
        List<SlimefunItem> visible = new ArrayList<>();
        for (SlimefunItem item : group.getItems()) {
            if (item == null || item.isDisabledIn(player.getWorld()) || item.isHidden()) continue;
            if (!Slimefun.getConfigManager().isShowHiddenItemGroupsInSearch() && !item.getItemGroup().isAccessible(player)) continue;
            visible.add(item);
        }

        int pages = Math.max(1, (visible.size() + ITEM_PAGE_SIZE - 1) / ITEM_PAGE_SIZE);
        page = Math.max(1, Math.min(page, pages));
        final int currentPage = page;
        int start = (currentPage - 1) * ITEM_PAGE_SIZE;

        // 4x7 物品格
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int slot = ITEM_SLOTS[i];
            int index = start + i;
            if (index < visible.size()) {
                SlimefunItem item = visible.get(index);
                if (isUiSeparatorPane(item)) {
                    // 贡献组的 UI 玻璃分隔板: 纯展示, 不可点击
                    menu.addItem(slot, item.getItem(), ChestMenuUtils.getEmptyClickHandler());
                } else if (isItemClickable(player, profile, mode, item)) {
                    menu.addItem(slot, item.getItem());
                    menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                        SlimefunGuide.displayItem(profile, item, true);
                        return false;
                    });
                } else {
                    menu.addItem(slot, renderLockedIcon(player, profile, mode, item), ChestMenuUtils.getEmptyClickHandler());
                }
            }
        }

        // 翻页:46上一页 / 52下一页(无页=白色, 有页=黄绿)
        if (currentPage > 1) {
            menu.addItem(46, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(46, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openItemGroupPage(p, profile, mode, group, currentPage - 1);
                return false;
            });
        } else {
            menu.addItem(46, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }
        if (currentPage < pages) {
            menu.addItem(52, plainPane(Material.LIME_STAINED_GLASS_PANE));
            menu.addMenuClickHandler(52, (p, s, it, a) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openItemGroupPage(p, profile, mode, group, currentPage + 1);
                return false;
            });
        } else {
            menu.addItem(52, plainPane(Material.WHITE_STAINED_GLASS_PANE), ChestMenuUtils.getEmptyClickHandler());
        }

        // 49槽:神奇的小按钮(与一二级一致)
        menu.addItem(49, new CustomItemStack(Material.NETHER_STAR, getGradientNameVer2("神奇的小按钮"), getGradientNameVer2("点一下会发生什么？")));
        menu.addMenuClickHandler(49, (p, s, it, a) -> {
            triggerMysteryButton(p);
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return false;
        });

        menu.open(player);
    }

    /** 贡献组里的 UI 玻璃分隔板(UI_5~UI_9): 仅展示, 不可点击 */
    private static boolean isUiSeparatorPane(SlimefunItem item) {
        return item != null && item.getId().startsWith("UI_")
                && item.getItem().getType().name().endsWith("STAINED_GLASS_PANE");
    }

    /** 为每个(分组, 页码)缓存一个稳定的历史锚点, 保证返回链不重复堆叠 */
    private static VirtualGuideGroup itemPageAnchor(ItemGroup group, int page, Player player) {
        String rawKey = group.getKey().getKey() + ":" + page;
        String safeKey = rawKey.replaceAll("[^a-z0-9/._-]", "_");
        return ITEM_PAGE_ANCHORS.computeIfAbsent(rawKey, k -> new VirtualGuideGroup(
                new NamespacedKey(MagicExpansion.getInstance(), "itempage_" + safeKey),
                group.getItem(player),
                (p, pr, m) -> openItemGroupPage(p, pr, m, group, page)
        ));
    }

    /** 物品是否可直接点击查看配方(无权限/未研究解锁则不可点击) */
    private static boolean isItemClickable(Player player, PlayerProfile profile, SlimefunGuideMode mode, SlimefunItem item) {
        if (mode != SlimefunGuideMode.SURVIVAL_MODE) return true;
        if (!Slimefun.getPermissionsService().hasPermission(player, item)) return false;
        return item.getResearch() == null || profile.hasUnlocked(item.getResearch());
    }

    /** 无权限/未解锁物品的锁定样式(沿用原版 NoPermission 图标) */
    private static ItemStack renderLockedIcon(Player player, PlayerProfile profile, SlimefunGuideMode mode, SlimefunItem item) {
        List<String> lore = new ArrayList<>();
        if (!Slimefun.getPermissionsService().hasPermission(player, item)) {
            lore.addAll(Slimefun.getPermissionsService().getLore(item));
        }
        if (item.getResearch() != null && !profile.hasUnlocked(item.getResearch())) {
            lore.add("§7需要研究解锁: §e" + item.getResearch().getName(player));
        }
        return new CustomItemStack(ChestMenuUtils.getNoPermissionItem(), item.getItemName(), lore.toArray(new String[0]));
    }

    /* ==================== 公共渲染 ==================== */

    private static void addHistory(PlayerProfile profile, SlimefunGuideMode mode, ItemGroup group, int page) {
        if (mode == SlimefunGuideMode.SURVIVAL_MODE) {
            profile.getGuideHistory().add(group, page);
        }
    }

    private static void addBack(ChestMenu menu, PlayerProfile profile, SlimefunGuideMode mode) {
        Player player = profile.getPlayer();
        GuideHistory history = profile.getGuideHistory();
        // 使用原版 SurvivalSlimefunGuide, 不经过 JEG 等替换引导, 保证与原版返回行为一致
        SlimefunGuideImplementation guide = new SurvivalSlimefunGuide();

        // 完整复刻原版粘液书返回逻辑: 历史>1 时返回上一页(Shift 回主菜单), 否则直接回主菜单
        if (mode == SlimefunGuideMode.SURVIVAL_MODE && history.size() > 1) {
            menu.addItem(1, new CustomItemStack(ChestMenuUtils.getBackButton(player),
                    "",
                    "§f点击: §7返回上一页",
                    "§fShift + 点击: §7返回主菜单"));
            menu.addMenuClickHandler(1, (p, slot, item, clickAction) -> {
                if (clickAction.isShiftClicked()) {
                    guide.openMainMenu(profile, history.getMainMenuPage());
                } else {
                    history.goBack(guide);
                }
                return false;
            });
        } else {
            menu.addItem(1, new CustomItemStack(ChestMenuUtils.getBackButton(player),
                    "",
                    "§7返回主菜单"));
            menu.addMenuClickHandler(1, (p, slot, item, clickAction) -> {
                guide.openMainMenu(profile, history.getMainMenuPage());
                return false;
            });
        }
    }

    private static ChestMenu createBaseMenu(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        String title = Slimefun.getLocalization().getMessage(player, "guide.title.main");
        ChestMenu menu = new ChestMenu(title);
        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f));
        SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
        if (guide instanceof SurvivalSlimefunGuide survival) {
            survival.createHeader(player, profile, menu);
        }
        return menu;
    }

    private static void renderGroupButtons(ChestMenu menu, Player player, PlayerProfile profile, SlimefunGuideMode mode,
                                           List<ItemGroup> groups, int page,
                                           GroupClickHandler clickHandler,
                                           PageReopener pageReopener) {
        int pages = Math.max(1, (groups.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        page = Math.max(1, Math.min(page, pages));
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, groups.size());
        final int currentPage = page;

        int slot = FIRST_SLOT;
        for (int i = start; i < end; i++) {
            ItemGroup sub = groups.get(i);
            int currentSlot = slot;
            menu.addItem(currentSlot, sub.getItem(player));
            menu.addMenuClickHandler(currentSlot, (p, s, item, action) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                clickHandler.onClick(p, profile, mode, sub);
                return false;
            });
            slot++;
        }

        if (page > 1) {
            menu.addItem(46, ChestMenuUtils.getPreviousButton(player, page, pages));
            menu.addMenuClickHandler(46, (p, s, item, action) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                pageReopener.reopen(p, profile, mode, currentPage - 1);
                return false;
            });
        }
        if (page < pages) {
            menu.addItem(52, ChestMenuUtils.getNextButton(player, page, pages));
            menu.addMenuClickHandler(52, (p, s, item, action) -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                pageReopener.reopen(p, profile, mode, currentPage + 1);
                return false;
            });
        }
    }

    /** 复刻原生 getVisibleItemGroups:FlexItemGroup 用三参 isVisible,普通分组只看组本身是否隐藏 */
    private static List<ItemGroup> visibleGroups(List<ItemGroup> groups, Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        List<ItemGroup> result = new ArrayList<>();
        for (ItemGroup group : groups) {
            if (group == null) continue;
            if (group instanceof FlexItemGroup flex) {
                if (flex.isVisible(player, profile, mode)) {
                    result.add(group);
                }
            } else if (group instanceof SubItemGroup sub) {
                // 原生分组页同款判断:isVisibleInNested 直调父类 ItemGroup.isVisible,不受 SubItemGroup 覆写影响
                if (sub.isVisibleInNested(player)) {
                    result.add(group);
                }
            } else {
                if (!group.isHidden(player)) {
                    result.add(group);
                }
            }
        }
        return result;
    }

    private static void openMenu(ChestMenu menu, Player player) {
        menu.open(player);
    }

    @FunctionalInterface
    private interface GroupClickHandler {
        void onClick(Player player, PlayerProfile profile, SlimefunGuideMode mode, ItemGroup group);
    }

    @FunctionalInterface
    private interface PageReopener {
        void reopen(Player player, PlayerProfile profile, SlimefunGuideMode mode, int page);
    }
}
