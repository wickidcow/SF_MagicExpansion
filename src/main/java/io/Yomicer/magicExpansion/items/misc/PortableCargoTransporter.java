package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortableCargoTransporter extends SlimefunItem implements Listener {

    // 传输状态管理
    private static final Map<UUID, Boolean> playerTransmissionStatus = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> activeTransmissions = new HashMap<>();

    // 上次检查时间记录，避免频繁检查
    private static final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public PortableCargoTransporter(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        // 注册事件监听器
        MagicExpansion.getInstance().getServer().getPluginManager().registerEvents(this, MagicExpansion.getInstance());

        // 添加物品使用处理器
        addItemHandler(onItemUse());

        // 启动全局清理任务
        startCleanupTask();
    }

    /**
     * 物品使用处理器
     */
    @Nonnull
    private ItemUseHandler onItemUse() {
        return e -> {
            e.cancel();
            Player player = e.getPlayer();
            ItemStack item = e.getItem();

            if (player.isSneaking()) {
                // Shift+右键：显示使用说明
                showUsage(player);
            } else {
                // 普通右键：切换传输状态
                toggleTransmission(player);
            }
        };
    }

    /**
     * 显示使用说明
     */
    private void showUsage(Player player) {
        player.sendMessage("§6§l便携以太秘匣传输器使用说明");
        player.sendMessage("§7§m--------------------------------");
        player.sendMessage("§f右键: §a启动传输");
        player.sendMessage("§f再次右键: §c停止传输");
        player.sendMessage("§fShift+右键: §e显示此说明");
        player.sendMessage("");
        player.sendMessage("§7● 自动检测背包中的以太秘匣");
        player.sendMessage("§7● 只处理堆叠数为1的以太秘匣");
        player.sendMessage("§7● 每次处理背包中最前面的以太秘匣");
        player.sendMessage("§7● 数量为0时自动删除以太秘匣");
        player.sendMessage("§7● 传输间隔: 0.05秒");
        player.sendMessage("§7● 玩家离线自动停止传输");
        player.sendMessage("§7§m--------------------------------");
    }

    /**
     * 切换传输状态（右键触发）
     */
    private void toggleTransmission(Player player) {
        UUID playerId = player.getUniqueId();

        // 检查当前状态
        boolean isActive = playerTransmissionStatus.getOrDefault(playerId, false);

        if (isActive) {
            // 停止传输
            stopTransmission(player);
            player.sendMessage("§c✗ 已停止传输！");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
        } else {
            // 启动传输
            startTransmission(player);
        }
    }

    /**
     * 启动传输
     */
    private void startTransmission(Player player) {
        UUID playerId = player.getUniqueId();

        // 检查是否已经在传输中
        if (activeTransmissions.containsKey(playerId)) {
            return;
        }

        // 检查背包中是否有以太秘匣
        if (!hasValidCargoFragment(player)) {
            player.sendMessage("§c背包中没有有效的以太秘匣！");
            player.sendMessage("§7请确保以太秘匣堆叠数为1");
            return;
        }

        // 创建新的传输任务
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // 检查玩家是否在线 - 如果不在线则停止传输
                if (!player.isOnline()) {
                    stopTransmission(player);
//                    MagicExpansion.getInstance().getLogger().info("玩家 " + player.getName() + " 离线，自动停止传输");
                    return;
                }

                // 检查传输状态
                if (!playerTransmissionStatus.getOrDefault(playerId, false)) {
                    return;
                }

                // 执行传输
                performTransmission(player);
            }
        };

        // 每秒执行一次（20 tick = 1秒）
        task.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
        activeTransmissions.put(playerId, task);
        playerTransmissionStatus.put(playerId, true);

        player.sendMessage("§a✓ 传输已启动！");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }

    /**
     * 停止传输
     */
    private void stopTransmission(Player player) {
        UUID playerId = player.getUniqueId();

        if (activeTransmissions.containsKey(playerId)) {
            activeTransmissions.get(playerId).cancel();
            activeTransmissions.remove(playerId);
        }

        playerTransmissionStatus.put(playerId, false);
    }

    /**
     * 执行传输逻辑
     */
    private void performTransmission(Player player) {

        // 查找背包中最前面的以太秘匣
        ItemStack cargoFragment = findFirstValidCargoFragment(player);
        if (cargoFragment == null) {
            // 没有有效的以太秘匣，不停止传输（等待玩家放入新的以太秘匣）
            return;
        }

        // 获取以太秘匣中的物品和数量
        ItemStack storedItem = getStoredItemFromFragment(cargoFragment);
        int storedAmount = getStoredAmountFromFragment(cargoFragment);

        if (storedItem == null || storedAmount <= 0) {
            // 空的以太秘匣，移除它
            removeCargoFragment(player, cargoFragment);
            player.sendMessage("§c发现空的以太秘匣，已移除！");
            return;
        }

        // 计算背包剩余空间
        int availableSpace = calculateAvailableSpace(player, storedItem);
        if (availableSpace <= 0) {
            // 背包已满，跳过本次传输
            return;
        }

        // 计算实际能分发的数量
        int amountToGive = Math.min(storedAmount, availableSpace);

        // 填满玩家背包
        int actuallyGiven = fillPlayerInventory(player, storedItem, amountToGive);

        if (actuallyGiven > 0) {
            int newAmount = storedAmount - actuallyGiven;

            if (newAmount <= 0) {
                // 数量为0，删除该以太秘匣
                removeCargoFragment(player, cargoFragment);
                player.sendMessage("§a✓ 已完成传输: " + getItemDisplayName(storedItem));
            } else {
                // 更新以太秘匣数量
                updateFragmentAmount(cargoFragment, newAmount);
                player.sendMessage("§a✓ 正在传输: " + getItemDisplayName(storedItem) + " §7(剩余: " + newAmount + ")");
            }
        }
    }

    /**
     * 检查背包中是否有有效的以太秘匣
     */
    private boolean hasValidCargoFragment(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isCargoFragment(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找背包中最前面的以太秘匣
     */
    private ItemStack findFirstValidCargoFragment(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (isCargoFragment(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 移除以太秘匣
     */
    private void removeCargoFragment(Player player, ItemStack cargoFragment) {
        ItemStack[] inventory = player.getInventory().getContents();

        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].equals(cargoFragment)) {
                player.getInventory().setItem(i, null);
                break;
            }
        }
    }

    /**
     * 检查是否为以太秘匣
     */
    private boolean isCargoFragment(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (!(sfItem instanceof CargoFragment)) return false;

        // 检查堆叠数量是否为1
        if (item.getAmount() != 1) return false;

        // 检查是否有存储物品
        ItemStack storedItem = getStoredItemFromFragment(item);
        int storedAmount = getStoredAmountFromFragment(item);

        return storedItem != null && storedAmount > 0;
    }

    /**
     * 从以太秘匣获取存储的物品
     */
    private ItemStack getStoredItemFromFragment(ItemStack fragment) {
        if (fragment == null || !fragment.hasItemMeta()) return null;

        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey keyItem = new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json");
        String json = container.get(keyItem, PersistentDataType.STRING);
        if (json == null) return null;

        return itemFromBase64(json);
    }

    /**
     * 从以太秘匣获取存储的数量
     */
    private int getStoredAmountFromFragment(ItemStack fragment) {
        if (fragment == null || !fragment.hasItemMeta()) return 0;

        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey keyAmount = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
        Integer amount = container.get(keyAmount, PersistentDataType.INTEGER);
        return amount != null ? amount : 0;
    }

    /**
     * 更新以太秘匣的数量
     */
    private void updateFragmentAmount(ItemStack fragment, int newAmount) {
        if (fragment == null || !fragment.hasItemMeta()) return;

        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey keyAmount = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
        container.set(keyAmount, PersistentDataType.INTEGER, newAmount);

        // 更新Lore显示数量
        if (meta.hasLore()) {
            java.util.List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).startsWith("§f数量: §a")) {
                    lore.set(i, "§f数量: §a" + newAmount);
                    break;
                }
            }
            meta.setLore(lore);
        }

        fragment.setItemMeta(meta);
    }

    /**
     * 计算玩家背包剩余空间
     */
    private int calculateAvailableSpace(Player player, ItemStack item) {
        int totalSpace = 0;

        for (ItemStack inventoryItem : player.getInventory().getStorageContents()) {
            if (inventoryItem == null || inventoryItem.getType() == Material.AIR) {
                totalSpace += item.getMaxStackSize();
            } else if (SlimefunUtils.isItemSimilar(inventoryItem, item, true, false)) {
                int space = inventoryItem.getMaxStackSize() - inventoryItem.getAmount();
                if (space > 0) {
                    totalSpace += space;
                }
            }
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

        // 修复(V)：统一使用 getStorageContents() 主存储区（36格）进行填充循环与空间计算，
        // 原getSize()为41格（含盔甲槽/副手索引），存在物品被写入盔甲槽的风险
        ItemStack[] storage = player.getInventory().getStorageContents();

        // 先填充已有物品的槽位
        for (int i = 0; i < storage.length; i++) {
            if (totalGiven >= maxAmount) break;

            ItemStack slotItem = storage[i];
            if (slotItem != null && !slotItem.getType().isAir() &&
                    SlimefunUtils.isItemSimilar(slotItem, singleItem, true) &&
                    slotItem.getAmount() < slotItem.getMaxStackSize()) {

                int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                int toAdd = Math.min(space, maxAmount - totalGiven);

                slotItem.setAmount(slotItem.getAmount() + toAdd);
                player.getInventory().setItem(i, slotItem); // 修复(V)：显式写回主存储区槽位
                totalGiven += toAdd;
            }
        }

        // 再填充空槽位
        for (int i = 0; i < storage.length; i++) {
            if (totalGiven >= maxAmount) break;

            ItemStack slotItem = storage[i];
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
     * 获取物品显示名
     */
    private String getItemDisplayName(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item != null ? item.getType().name().toLowerCase().replace('_', ' ') : "未知物品";
    }

    /**
     * 启动清理任务 - 清理离线玩家数据
     */
    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 清理不在线的玩家数据
                playerTransmissionStatus.keySet().removeIf(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        // 玩家不在线，停止传输任务
                        BukkitRunnable task = activeTransmissions.get(uuid);
                        if (task != null) {
                            task.cancel();
                        }
                        activeTransmissions.remove(uuid);
                        lastCheckTime.remove(uuid);
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 6000L, 6000L); // 每5分钟清理一次
    }

    /**
     * 事件监听器：玩家退出时停止传输
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        stopTransmission(player);
//        MagicExpansion.getInstance().getLogger().info("玩家 " + player.getName() + " 退出游戏，停止传输");
    }

    /**
     * 从Base64还原物品
     */
    private ItemStack itemFromBase64(String data) {
        try {
            return io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64(data);
        } catch (Exception e) {
            MagicExpansion.getInstance().getLogger().warning("物品反序列化失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 服务器关闭时清理所有任务
     */
    public static void onPluginDisable() {
        // 停止所有传输任务
        for (BukkitRunnable task : activeTransmissions.values()) {
            task.cancel();
        }
        activeTransmissions.clear();
        playerTransmissionStatus.clear();
        lastCheckTime.clear();
    }
}