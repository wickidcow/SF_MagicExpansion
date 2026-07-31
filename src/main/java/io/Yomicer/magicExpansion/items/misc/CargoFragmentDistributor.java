package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.misc.CargoFragment;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class CargoFragmentDistributor extends SlimefunItem implements EnergyNetComponent {

    private final int inputSlot = 10; // CargoFragment 输入槽
    private final int playerHeadSlot = 16; // 玩家头颅设置槽
    private final int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    private final int[] infoSlots = {27, 28, 29, 30, 31, 32, 33, 34, 35}; // 信息显示槽

    // 存储每个机器的状态
    public static final Map<Location, MachineState> machineStates = new HashMap<>();
    private static final int SLOW_CHECK_INTERVAL = 100; // 慢速检测:60 tick = 5 秒
    private static final int FAST_CHECK_INTERVAL = 1;   // 快速检测:1 tick = 0.1秒
    public static BukkitRunnable globalTickTask;


    // 头颅皮肤缓存
    private static final Map<String, ItemStack> playerHeadCache = new HashMap<>();
    private static final Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_DURATION = 10 * 60 * 1000; // 10分钟缓存


    public CargoFragmentDistributor(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        constructMenu("Aether Cargo Chest");
        addItemHandler(onBlockPlace(), onBlockBreak());

        // 启动全局 tick 任务
        startGlobalTickTask();
    }

    /**
     * 机器状态类
     */
    private static class MachineState {
        int tickCount = 0;
        String targetPlayerName = "";
        boolean hasValidFragment = false;
        boolean isActive = false;
        int currentCheckInterval = SLOW_CHECK_INTERVAL;

        void updateState(boolean hasFragment, boolean playerValid) {
            boolean shouldBeActive = hasFragment && playerValid;

            if (shouldBeActive != isActive) {
                isActive = shouldBeActive;
                currentCheckInterval = shouldBeActive ? FAST_CHECK_INTERVAL : SLOW_CHECK_INTERVAL;
                tickCount = 0; // 重置计数器
            }
        }

        boolean shouldProcess() {
            tickCount++;
            if (tickCount >= currentCheckInterval) {
                tickCount = 0;
                return true;
            }
            return false;
        }
    }

    /**
     * 启动全局 tick 任务
     */
    private void startGlobalTickTask() {
        if (globalTickTask != null && !globalTickTask.isCancelled()) {
            return;
        }

        globalTickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 遍历所有已注册的机器
                for (Map.Entry<Location, MachineState> entry : new HashMap<>(machineStates).entrySet()) {
                    Location location = entry.getKey();
                    MachineState state = entry.getValue();

                    // 检查方块是否还存在
                    if (!isBlockValid(location)) {
                        machineStates.remove(location);
                        continue;
                    }

                    // 检查是否应该处理
                    if (state.shouldProcess()) {
                        processMachine(location, state);
                    }
                }
            }
        };
        globalTickTask.runTaskTimer(MagicExpansion.getInstance(), 1L, 1L);
    }

    /**
     * 检查方块是否有效
     */
    private boolean isBlockValid(Location location) {
        if (!location.getChunk().isLoaded()) return false;

        Block block = location.getBlock();
        if (block.getType() == Material.AIR) return false;

        SlimefunItem sfItem = BlockStorage.check(location);
        return sfItem instanceof CargoFragmentDistributor;
    }

    /**
     * 处理机器逻辑
     */
    private void processMachine(Location location, MachineState state) {
        BlockMenu menu = StorageCacheUtils.getMenu(location);
        if (menu == null) return;

        // 检查玩家设置
        String playerName = getPlayerNameFromStorage(location);
        boolean playerValid = isPlayerValid(playerName);

        // 检查 CargoFragment
        ItemStack fragment = menu.getItemInSlot(inputSlot);
        boolean hasFragment = isCargoFragmentValid(fragment);

        // 更新机器状态
        state.updateState(hasFragment, playerValid);

        // 更新显示信息
        updateDisplayInfo(menu, state, playerName, hasFragment);

        // 使用安全方法更新玩家头颅显示
        updatePlayerHeadSafely(menu, state, playerName);

        // 如果条件满足,尝试分发
        if (state.isActive && hasFragment && playerValid) {
            distributeFragment(fragment, playerName, menu, location);
        }
    }

    /**
     * 分发 CargoFragment 中的物品 - 尽量填满背包
     */
    private void distributeFragment(ItemStack fragment, String playerName, BlockMenu menu, Location machineLoc) {
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) return;

        // 从 CargoFragment 获取存储的物品和数量
        ItemStack storedItem = getStoredItemFromFragment(fragment);
        int storedAmount = getStoredAmountFromFragment(fragment);

        if (storedItem == null || storedAmount <= 0) {
            // 无效的 CargoFragment,删除它
            menu.consumeItem(inputSlot, 1);
            return;
        }

        // 计算背包剩余空间能容纳多少物品
        int availableSpace = calculateAvailableSpace(targetPlayer, storedItem);
        if (availableSpace <= 0) {
            // 背包已满
            return;
        }

        // 计算实际能分发的数量
        int amountToGive = Math.min(storedAmount, availableSpace);

        // 尝试填满背包
        int actuallyGiven = fillPlayerInventory(targetPlayer, storedItem, amountToGive);

        if (actuallyGiven > 0) {
            // 更新 CargoFragment 中的数量
            int newAmount = storedAmount - actuallyGiven;
            if (newAmount <= 0) {
                // 数量为0,删除 CargoFragment
                menu.consumeItem(inputSlot, 1);
                targetPlayer.sendMessage("§a✓ Received all items: " + getItemDisplayName(storedItem));
            } else {
                // 更新 CargoFragment 的数量
                updateFragmentAmount(fragment, newAmount);
                targetPlayer.sendMessage("§a✓ Received item: " + getItemDisplayName(storedItem) + " §7(Remaining: " + newAmount + ")");
            }
        }
    }

    /**
     * 计算玩家背包剩余空间
     */
    private int calculateAvailableSpace(Player player, ItemStack item) {
        int totalSpace = 0;

        // 检查背包所有槽位
        for (ItemStack inventoryItem : player.getInventory().getStorageContents()) {
            if (inventoryItem == null || inventoryItem.getType() == Material.AIR) {
                // 空槽位,可以放一整组
                totalSpace += item.getMaxStackSize();
            } else if (SlimefunUtils.isItemSimilar(inventoryItem, item, true, false)) {
                // 相同物品,计算剩余空间
                int space = inventoryItem.getMaxStackSize() - inventoryItem.getAmount();
                if (space > 0) {
                    totalSpace += space;
                }
            }
            // 不同物品的槽位不能堆叠,不计入可用空间
        }

        return totalSpace;
    }


    /**
     * 填满玩家背包
     */
    private int fillPlayerInventory(Player player, ItemStack item, int maxAmount) {
        int totalGiven = 0;
        ItemStack singleItem = item.clone();
        singleItem.setAmount(1);

        // 先填充已有物品的槽位(相同物品可以堆叠)
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (totalGiven >= maxAmount) break;

            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem != null && !slotItem.getType().isAir() &&
                    SlimefunUtils.isItemSimilar(slotItem, singleItem, true) &&
                    slotItem.getAmount() < slotItem.getMaxStackSize()) {

                int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                int toAdd = Math.min(space, maxAmount - totalGiven);

                slotItem.setAmount(slotItem.getAmount() + toAdd);
                totalGiven += toAdd;
            }
        }

        // 再填充空槽位
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (totalGiven >= maxAmount) break;

            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem == null || slotItem.getType().isAir()) {
                int toAdd = Math.min(item.getMaxStackSize(), maxAmount - totalGiven);

                ItemStack newStack = item.clone();
                newStack.setAmount(toAdd);
                player.getInventory().setItem(i, newStack);

                totalGiven += toAdd;
            }
        }

        return totalGiven;
    }

    /**
     * 检查玩家是否有效
     */
    private boolean isPlayerValid(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;

        Player player = Bukkit.getPlayerExact(playerName);
        return player != null && player.isOnline();
    }

    /**
     * 检查 CargoFragment 是否有效
     */
    private boolean isCargoFragmentValid(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (!(sfItem instanceof CargoFragment)) return false;
        // 检查物品堆叠数量是否为1
        if (item.getAmount() != 1) return false;
        // 检查是否有存储物品
        ItemStack storedItem = getStoredItemFromFragment(item);
        int storedAmount = getStoredAmountFromFragment(item);

        return storedItem != null && storedAmount > 0;
    }

    /**
     * 从 CargoFragment 获取存储的物品
     */
    private ItemStack getStoredItemFromFragment(ItemStack fragment) {
        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey keyItem = new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json");
        String json = container.get(keyItem, PersistentDataType.STRING);
        if (json == null) return null;

        return itemFromBase64(json);
    }

    /**
     * 从 CargoFragment 获取存储的数量
     */
    private int getStoredAmountFromFragment(ItemStack fragment) {
        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey keyAmount = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
        Integer amount = container.get(keyAmount, PersistentDataType.INTEGER);
        return amount == null ? 0 : amount;
    }

    /**
     * 更新 CargoFragment 的数量
     */
    private void updateFragmentAmount(ItemStack fragment, int newAmount) {
        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey keyAmount = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
        container.set(keyAmount, PersistentDataType.INTEGER, newAmount);

        // 更新 Lore 显示
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).startsWith("§fAmount: §a") || lore.get(i).startsWith("§f\u6570\u91cf: §a")) {
                    lore.set(i, "§fAmount: §a" + newAmount);
                    break;
                }
            }
            meta.setLore(lore);
        }

        fragment.setItemMeta(meta);
    }

    /**
     * 从存储中获取玩家名
     */
    private String getPlayerNameFromStorage(Location location) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(location);
        if (data == null) return null;

        return data.getData("target_player");
    }
    /*
    优化玩家头颅获取
     */
    private ItemStack createPlayerHeadWithCache(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return createPlayerHeadDisplay(null);
        }

        // 检查缓存是否有效
        long currentTime = System.currentTimeMillis();
        if (playerHeadCache.containsKey(playerName)) {
            Long timestamp = cacheTimestamps.get(playerName);
            if (timestamp != null && (currentTime - timestamp) < CACHE_DURATION) {
                // 返回缓存的副本
                return playerHeadCache.get(playerName).clone();
            } else {
                // 缓存过期,移除
                playerHeadCache.remove(playerName);
                cacheTimestamps.remove(playerName);
            }
        }

        // 创建新的头颅
        ItemStack head = createPlayerHeadDisplay(playerName);

        // 缓存新创建的头颅
        if (head != null) {
            playerHeadCache.put(playerName, head.clone());
            cacheTimestamps.put(playerName, currentTime);
        }

        return head;
    }
    private void updatePlayerHeadSafely(BlockMenu menu, MachineState state, String playerName) {
        ItemStack currentHead = menu.getItemInSlot(playerHeadSlot);

        // 确定应该显示的玩家名
        String expectedPlayerName = (playerName != null && !playerName.isEmpty()) ? playerName : null;

        // 检查是否需要更新
        boolean needsUpdate = false;

        if (currentHead == null || currentHead.getType() != Material.PLAYER_HEAD) {
            // 当前不是玩家头颅,需要更新
            needsUpdate = true;
        } else if (currentHead.hasItemMeta()) {
            ItemMeta meta = currentHead.getItemMeta();
            String currentDisplayName = meta.getDisplayName();

            // 根据玩家名确定期望的显示名
            String expectedDisplayName = expectedPlayerName != null ?
                    "§aTarget Player: " + expectedPlayerName : "§aClick to Set Target Player";

            // 如果显示名不匹配,需要更新
            if (!currentDisplayName.equals(expectedDisplayName)) {
                needsUpdate = true;
            }

            // 检查Lore是否匹配
            if (!needsUpdate && meta.hasLore()) {
                List<String> lore = meta.getLore();
                boolean hasPlayerSet = expectedPlayerName != null;
                boolean loreMatches = lore.size() >= 1 &&
                        lore.get(0).equals(hasPlayerSet ? "§7Target player is configured" : "§7No target player configured");

                if (!loreMatches) {
                    needsUpdate = true;
                }
            }
        } else {
            // 没有元数据,需要更新
            needsUpdate = true;
        }

        // 如果需要更新,使用缓存方法创建新头颅
        if (needsUpdate) {
            ItemStack newHead = createPlayerHeadWithCache(expectedPlayerName);
            menu.replaceExistingItem(playerHeadSlot, newHead);
        }
    }

    /**
     * 保存玩家名到存储
     */
    private void savePlayerNameToStorage(Location location, String playerName) {
        if (location == null) return;

        SlimefunBlockData data = StorageCacheUtils.getBlock(location);
        if (data == null) return;

        if (playerName == null) {
            data.removeData("target_player");
        } else {
            data.setData("target_player", playerName);
        }
    }

    /**
     * 更新显示信息
     */
    private void updateDisplayInfo(BlockMenu menu, MachineState state, String playerName, boolean hasFragment) {
        String status;
        String details;
        Material displayMaterial;

        if (state.isActive) {
            status = "§a§lActive";
            details = "§7Distributing items to the target player";
            displayMaterial = Material.LIME_STAINED_GLASS_PANE;
        } else if (hasFragment && playerName != null && !playerName.isEmpty()) {
            status = "§eTarget Player Offline";
            details = "§7Target: " + playerName;
            displayMaterial = Material.YELLOW_STAINED_GLASS_PANE;
        } else if (hasFragment) {
            status = "§cTarget Player Required";
            details = "§7Click the player-head slot to set a target";
            displayMaterial = Material.ORANGE_STAINED_GLASS_PANE;
        } else if (playerName != null && !playerName.isEmpty()) {
            status = "§cCargo Chest Required";
            details = "§7Insert an Aether Cargo Chest into the input slot";
            displayMaterial = Material.ORANGE_STAINED_GLASS_PANE;
        } else {
            status = "§7Idle";
            details = "§7Set a target player and insert an Aether Cargo Chest";
            displayMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }

        // 更新信息显示槽
        ItemStack infoItem = new CustomItemStack(
                displayMaterial,
                "§6Aether Cargo Chest",
                "",
                "§fStatus: " + status,
                "§f" + details,
                "",
                "§eInput Slots: §aAether Cargo Chest",
                "§ePlayer Slot: §aClick to set player",
                "",
                "§7High-Speed Mode: distributes every Slimefun tick",
                "§7Low-Speed Mode: checks every 5 seconds"
        );

        for (int slot : infoSlots) {
            menu.replaceExistingItem(slot, infoItem);
        }
    }

    /**
     * 获取物品显示名
     */
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().toLowerCase().replace('_', ' ');
    }

    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                Location loc = e.getBlock().getLocation();
                machineStates.put(loc, new MachineState());
                // 如果机器数量增加很多,检查缓存大小
                if (machineStates.size() % 10 == 0) { // 每10台机器检查一次
                    cleanExpiredCache();
                }
            }
        };
    }

    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(Block b) {
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
                if (inv != null) {
                    inv.dropItems(b.getLocation(), new int[]{inputSlot});
                }
                // 如果机器数量增加很多,检查缓存大小
                if (machineStates.size() % 10 == 0) { // 每10台机器检查一次
                    cleanExpiredCache();
                }
                // 从状态映射中移除
                machineStates.remove(b.getLocation());
            }
        };
    }

    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {
            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                return p.hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager().hasPermission(p, b.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
                if (itemTransportFlow == ItemTransportFlow.INSERT) {
                    return new int[]{10};
                } else {
                    return new int[]{10};
                }
            }

        };
    }

    protected void constructMenu(BlockMenuPreset preset) {
        // 设置边框
        ItemStack borderItem = new CustomItemStack(Material.PURPLE_STAINED_GLASS_PANE, "§5Aether Cargo Chest");
        for (int slot : borderSlots) {
            preset.addItem(slot, borderItem, (p, slot1, item, action) -> false);
        }

//        // 设置输入槽 - 空槽位,可以直接放置物品
//        preset.addItem(inputSlot, null, (p, slot, item, action) -> {
//            // 允许放置任何物品
//            return true;
//        });

        // 设置玩家头颅槽 - 点击设置玩家
        ItemStack playerHeadItem = createPlayerHeadDisplay(null);

        preset.addItem(playerHeadSlot, playerHeadItem, (p, slot, item, action) -> {
            // 使用玩家打开菜单时所在的方块位置
            Block b = p.getTargetBlockExact(5);
            if (b == null) return false;

            Location location = b.getLocation();

            // 获取 BlockMenu 实例
            BlockMenu menu = BlockStorage.getInventory(b);
            if (menu == null) return false;

            if (action.isRightClicked()) {
                // 右键清除玩家设置
                savePlayerNameToStorage(location, null);
                p.sendMessage("§aCleared the target player.");

                // 更新显示 - 使用 BlockMenu 更新
                menu.replaceExistingItem(playerHeadSlot, createPlayerHeadDisplay(null));
            } else {
                // 左键设置当前玩家
                savePlayerNameToStorage(location, p.getName());
                p.sendMessage("§aTarget player set to: " + p.getName());

                // 更新显示 - 使用 BlockMenu 更新
                menu.replaceExistingItem(playerHeadSlot, createPlayerHeadDisplay(p.getName()));
            }
            return false;
        });

        // 初始化信息显示
        ItemStack initialInfo = new CustomItemStack(
                Material.GRAY_STAINED_GLASS_PANE,
                "§6Aether Cargo Chest",
                "",
                "§fStatus:  §7 -",
                "§f5",
                "",
                "§eInput Slots: §aAether Cargo Chest",
                "§ePlayer Slot: §aClick to set player",
                "",
                "§7High-Speed Mode: distributes every Slimefun tick",
                "§7Low-Speed Mode: checks every 5 seconds"
        );

        for (int slot : infoSlots) {
            preset.addItem(slot, initialInfo, (p, slot1, item, action) -> false);
        }
    }

    /**
     * 创建玩家头颅显示物品 - 修复版本,避免429错误
     */
    private ItemStack createPlayerHeadDisplay(String playerName) {
        ItemStack headItem;

        if (playerName != null && !playerName.isEmpty()) {
            // 创建玩家头颅
            headItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();

            if (skullMeta != null) {
                // 安全地设置玩家皮肤
                try {
                    Player targetPlayer = Bukkit.getPlayerExact(playerName);
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        // 使用在线玩家的皮肤
                        skullMeta.setOwningPlayer(targetPlayer);
                    } else {
                        // 对于离线玩家,使用默认皮肤避免API调用
                        // 这里可以使用一个已知的默认玩家
                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer("Steve"));
                    }
                } catch (Exception e) {
                    // 如果出现异常,记录日志但不中断程序
                    MagicExpansion.getInstance().getLogger().warning("Only players can perform this action." + e.getMessage());
                }

                skullMeta.setDisplayName("§aTarget Player: " + playerName);
                skullMeta.setLore(Arrays.asList(
                        "§7Target player is configured",
                        "",
                        "§eLeft-click: set yourself as the target",
                        "§eRight-click: clear the target"
                ));

                headItem.setItemMeta(skullMeta);
            }
        } else {
            // 未设置玩家
            headItem = new CustomItemStack(Material.PLAYER_HEAD, "§aClick to Set Target Player",
                    "§7No target player configured",
                    "",
                    "§eLeft-click: set yourself as the target",
                    "§eRight-click: clear the target");
        }

        return headItem;
    }

    /**
     * 清理过期缓存
     */
    public static void cleanExpiredCache() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = cacheTimestamps.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > CACHE_DURATION) {
                playerHeadCache.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    /**
     * 更新玩家头颅显示
     */
    private void updatePlayerHeadDisplay(BlockMenuPreset preset, Location location, String playerName) {
        ItemStack headItem;

        if (playerName != null && !playerName.isEmpty()) {
            // 创建玩家头颅
            headItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();

            // 设置玩家皮肤
            Player targetPlayer = Bukkit.getPlayerExact(playerName);
            if (targetPlayer != null) {
                skullMeta.setOwningPlayer(targetPlayer);
            }

            skullMeta.setDisplayName("§aTarget Player: " + playerName);
            skullMeta.setLore(List.of(
                    "§7Target player is configured",
                    "",
                    "§eLeft-click: set yourself as the target",
                    "§eRight-click: clear the target"
            ));

            headItem.setItemMeta(skullMeta);
        } else {
            // 未设置玩家
            headItem = new CustomItemStack(Material.PLAYER_HEAD, "§aClick to Set Target Player",
                    "§7No target player configured",
                    "",
                    "§eLeft-click: set yourself as the target",
                    "§eRight-click: clear the target");
        }

        preset.addItem(playerHeadSlot, headItem, (p, slot, item, action) -> {
            // 使用玩家打开菜单时所在的方块位置
            Block b = p.getTargetBlockExact(5);
            if (b == null) return false;

            Location loc = b.getLocation();

            if (action.isRightClicked()) {
                // 右键清除玩家设置
                savePlayerNameToStorage(loc, null);
                p.sendMessage("§aCleared the target player.");

                // 更新显示
                updatePlayerHeadDisplay(preset, loc, null);
            } else {
                // 左键设置当前玩家
                savePlayerNameToStorage(loc, p.getName());
                p.sendMessage("§aTarget player set to: " + p.getName());

                // 更新显示
                updatePlayerHeadDisplay(preset, loc, p.getName());
            }
            return false;
        });
    }

    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.NONE;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    // 从 Base64 还原物品的方法
    private ItemStack itemFromBase64(String data) {
        try {
            // 使用您项目中现有的 itemFromBase64 方法
            return io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64(data);
        } catch (Exception e) {
            return null;
        }
    }

}
