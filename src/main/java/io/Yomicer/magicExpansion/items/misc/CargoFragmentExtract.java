package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.SameItemJudge;
import io.Yomicer.magicExpansion.utils.networksUtils.DataTypeMethods;
import io.Yomicer.magicExpansion.utils.networksUtils.NetworksKeys;
import io.Yomicer.magicExpansion.utils.networksUtils.PersistentQuantumStorageType;
import io.Yomicer.magicExpansion.utils.networksUtils.QuantumCache;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.CARGO_FRAGMENT;

public class CargoFragmentExtract extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    // 等待输入的玩家列表
    private static final Map<UUID, ExtractOperation> pendingExtracts = new HashMap<>();
    private static Listener chatListener;

    public CargoFragmentExtract(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);

            Player player = e.getPlayer();

            // 只响应主手
            if (e.getHand() != EquipmentSlot.HAND) {
                return;
            }

            ItemStack extractItem = e.getItem(); // 主手的CargoFragmentExtract
            ItemStack offhandItem = player.getInventory().getItemInOffHand(); // 副手物品

            // 1. 检查主手和副手物品数量必须为1
            if (extractItem.getAmount() != 1 || offhandItem.getAmount() != 1) {
                if (extractItem.getAmount() != 1) {
                    player.sendMessage(ChatColor.RED + "Hold exactly one extractor!");
                    player.sendMessage(ChatColor.GRAY + "Make sure you are using only one extractor.");
                }
                if (offhandItem.getAmount() != 1) {
                    player.sendMessage(ChatColor.RED + "Hold exactly one storage container in your off hand!");
                    player.sendMessage(ChatColor.GRAY + "Make sure you are holding only one storage container.");
                }
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f);
                return;
            }

            // 2. 检查副手物品
            if (offhandItem == null || offhandItem.getType().isAir()) {
                sendError(player, "Place a compatible quantum storage container in your off hand!");
                return;
            }

            // 3. 从副手物品获取QuantumCache
            ItemMeta offhandMeta = offhandItem.getItemMeta();
            if (offhandMeta == null) {
                sendError(player, "The off-hand item's data is invalid!");
                return;
            }

            // 使用你提供的方式获取QuantumCache
            QuantumCache quantumCache = DataTypeMethods.getCustom(offhandMeta,
                    NetworksKeys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);

            if (quantumCache == null || quantumCache.getItemStack() == null) {
                sendError(player, "The off-hand item has no valid quantum-storage data!");
                return;
            }

            // 4. 获取量子存储中的物品和数量
            ItemStack storedItem = quantumCache.getItemStack();
            long storedAmount = quantumCache.getAmount();

            if (storedItem == null || storedAmount <= 1) {
                // 存储中数量≤1时不能提取(需要至少保留1个)
                sendError(player, "Quantum storage must contain at least 2 items so one can remain inside.");
                return;
            }

            // 5. 计算最大可提取数量(当前数量-1)
            long maxExtract = storedAmount - 1;

            // 6. 创建等待输入状态
            ExtractOperation operation = new ExtractOperation(
                    player.getUniqueId(),
                    extractItem,
                    offhandItem,
                    storedItem,
                    storedAmount,
                    quantumCache,
                    maxExtract
            );

            pendingExtracts.put(player.getUniqueId(), operation);

            // 7. 显示提取提示
            showExtractPrompt(player, storedItem, storedAmount, maxExtract);

            // 8. 注册聊天监听器
            registerChatListener();

            // 9. 设置超时
            scheduleTimeout(player.getUniqueId());
        };
    }

    /**
     * 显示提取提示信息
     */
    private void showExtractPrompt(Player player, ItemStack storedItem, long storedAmount, long maxExtract) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "══════ " + ChatColor.BOLD + "Item Extraction" +
                ChatColor.RESET + ChatColor.GOLD + " ══════");
        player.sendMessage(ChatColor.YELLOW + "Item Type: " + ChatColor.WHITE +
                ItemStackHelper.getDisplayName(storedItem));
        player.sendMessage(ChatColor.YELLOW + "Total Stored: " + ChatColor.AQUA + storedAmount + " items");
        player.sendMessage(ChatColor.YELLOW + "Maximum Extractable: " + ChatColor.GOLD + maxExtract + " items");
        player.sendMessage(ChatColor.YELLOW + "Minimum Remaining: " + ChatColor.GREEN + "1 item");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Enter an amount (" + ChatColor.GREEN + "1-" +
                maxExtract + ChatColor.YELLOW + ")");
        player.sendMessage(ChatColor.GRAY + "Enter " + ChatColor.RED + "'cancel'" +
                ChatColor.GRAY + " to cancel (30-second timeout)");
        player.sendMessage("");

        playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f);
    }

    /**
     * 注册聊天事件监听器
     */
    private void registerChatListener() {
        if (chatListener != null) return;

        chatListener = new Listener() {
            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent event) {
                Player player = event.getPlayer();
                ExtractOperation operation = pendingExtracts.get(player.getUniqueId());

                if (operation == null) return;

                event.setCancelled(true);

                String input = event.getMessage().trim();

                Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
                    processExtractInput(player, operation, input);
                });
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                pendingExtracts.remove(event.getPlayer().getUniqueId());
                cleanupListenerIfNeeded();
            }
        };

        Bukkit.getPluginManager().registerEvents(chatListener, MagicExpansion.getInstance());
    }

    /**
     * 处理玩家提取输入
     */
    private void processExtractInput(Player player, ExtractOperation operation, String input) {
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "Extraction cancelled.");
            finishExtractInput(player.getUniqueId());
            return;
        }

        long extractAmount;
        try {
            extractAmount = Long.parseLong(input);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Enter a valid number, or type 'cancel'.");
            return;
        }

        if (extractAmount <= 0) {
            player.sendMessage(ChatColor.RED + "Enter an amount greater than 0.");
            return;
        }

        if (extractAmount > operation.maxExtract) {
            player.sendMessage(ChatColor.RED + "The maximum extractable amount is " + operation.maxExtract + ".");
            return;
        }

        if (operation.storedAmount - extractAmount < 1) {
            player.sendMessage(ChatColor.RED + "At least one item must remain in quantum storage.");
            return;
        }

        if (extractAmount > Integer.MAX_VALUE) {
            player.sendMessage(ChatColor.RED + "The extractor supports a maximum amount of " + Integer.MAX_VALUE + ".");
            return;
        }

        if (!validateItemsUnchanged(player, operation)) {
            player.sendMessage(ChatColor.RED + "Extraction cancelled because one of the items changed!");
            finishExtractInput(player.getUniqueId());
            return;
        }

        executeExtract(player, operation, extractAmount);
        finishExtractInput(player.getUniqueId());
    }

    private void finishExtractInput(UUID playerId) {
        pendingExtracts.remove(playerId);
        cleanupListenerIfNeeded();
    }

    /**
     * 验证物品在等待输入期间是否发生变化
     */
    private boolean validateItemsUnchanged(Player player, ExtractOperation operation) {
        // 1. 检查玩家是否在线
        if (!player.isOnline()) {
            return false;
        }

        // 2. 检查主手物品是否还是提取器
        ItemStack currentMainHand = player.getInventory().getItemInMainHand();
        if (currentMainHand == null || currentMainHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "The item in your main hand is gone!");
            return false;
        }

        // 检查是否是同一个物品(根据类型、名称等)
        if (!isSameExtractorItem(currentMainHand, operation.extractItem)) {
            player.sendMessage(ChatColor.RED + "The item in your main hand changed!");
            return false;
        }

        // 检查数量是否为1
        if (currentMainHand.getAmount() != 1) {
            player.sendMessage(ChatColor.RED + "The item amount changed while waiting for input!");
            return false;
        }

        // 3. 检查副手物品是否还是量子存储物品
        ItemStack currentOffhand = player.getInventory().getItemInOffHand();
        if (currentOffhand == null || currentOffhand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "The item in your off hand is gone!");
            return false;
        }
        // 检查数量是否为1
        if (currentOffhand.getAmount() != 1) {
            player.sendMessage(ChatColor.RED + "The item amount changed while waiting for input!");
            return false;
        }

        // 检查是否是同一个量子存储物品(通过PDC中的唯一标识)
        if (!isSameQuantumStorageItem(currentOffhand, operation.offhandItem)) {
            player.sendMessage(ChatColor.RED + "The item in your off hand changed!");
            return false;
        }

        // 4. 检查量子存储数据是否发生变化
        ItemMeta currentOffhandMeta = currentOffhand.getItemMeta();
        if (currentOffhandMeta == null) {
            player.sendMessage(ChatColor.RED + "The off-hand item's data is invalid!");
            return false;
        }

        QuantumCache currentCache = DataTypeMethods.getCustom(currentOffhandMeta,
                NetworksKeys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);

        if (currentCache == null) {
            player.sendMessage(ChatColor.RED + "The off-hand item's quantum storage data is missing!");
            return false;
        }

        // 检查存储的物品是否相同
        ItemStack currentStoredItem = currentCache.getItemStack();
        long currentStoredAmount = currentCache.getAmount();

        if (currentStoredItem == null) {
            player.sendMessage(ChatColor.RED + "The item stored in quantum storage is gone!");
            return false;
        }

        // 使用SameItemJudge比较物品是否相同
        if (!SameItemJudge.isSimilarSafe(currentStoredItem, operation.storedItem)) {
            player.sendMessage(ChatColor.RED + "The item in quantum storage has changed!");
            return false;
        }

        // 检查数量是否相同(不能少于之前记录的数量)
        if (currentStoredAmount < operation.storedAmount) {
            player.sendMessage(ChatColor.RED + "The item amount changed while waiting for input!");
            return false;
        }

        // 如果数量增加了,使用新的数量进行计算
        if (currentStoredAmount > operation.storedAmount) {
            // 更新操作中的数量信息
            operation.storedAmount = currentStoredAmount;
            operation.maxExtract = currentStoredAmount - 1;

            // 通知玩家数量已更新
            player.sendMessage(ChatColor.GREEN + "The stored amount increased; the extractable amount was recalculated.");
            player.sendMessage(ChatColor.YELLOW + "New Total Stored: " + ChatColor.AQUA + currentStoredAmount + " items");
            player.sendMessage(ChatColor.YELLOW + "New Maximum Extractable: " + ChatColor.GOLD + operation.maxExtract + " items");

            // 更新缓存引用
            operation.quantumCache = currentCache;
        }

        return true;
    }

    /**
     * 检查是否同一个提取器物品
     */
    private boolean isSameExtractorItem(ItemStack item1, ItemStack item2) {
        // 简单比较:类型和自定义名称
        if (item1.getType() != item2.getType()) {
            return false;
        }

        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 == null || meta2 == null) {
            return meta1 == meta2;
        }

        // 比较显示名称
        String name1 = meta1.getDisplayName();
        String name2 = meta2.getDisplayName();

        if (name1 == null && name2 == null) {
            return true;
        }

        if (name1 == null || name2 == null) {
            return false;
        }

        return name1.equals(name2);
    }

    /**
     * 检查是否同一个量子存储物品
     */
    private boolean isSameQuantumStorageItem(ItemStack item1, ItemStack item2) {
        // 首先比较基础信息
        if (item1.getType() != item2.getType()) {
            return false;
        }

        // 获取两个物品的PDC数据
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 == null || meta2 == null) {
            return meta1 == meta2;
        }

        // 从PDC获取QuantumCache进行比较
        QuantumCache cache1 = DataTypeMethods.getCustom(meta1,
                NetworksKeys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);

        QuantumCache cache2 = DataTypeMethods.getCustom(meta2,
                NetworksKeys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);

        // 如果有一个没有量子缓存,则不同
        if (cache1 == null || cache2 == null) {
            return false;
        }

        if (cache1.getAmount() != 0 && cache2.getAmount() != 0){
            return cache1.getAmount() == cache2.getAmount();
        }

        // 比较缓存ID或其他唯一标识(如果QuantumCache有getId方法)
        try {
            // 如果有getId方法
            if (cache1.getItemStack()!= null && cache2.getItemStack() != null) {
                return cache1.getItemStack().equals(cache2.getItemStack());
            }
        } catch (Exception e) {
            // 如果QuantumCache没有getId方法,使用hashCode作为备用
            return cache1.hashCode() == cache2.hashCode();
        }

        return false;
    }


    /**
     * 执行提取操作
     */
    private void executeExtract(Player player, ExtractOperation operation, long extractAmount) {
        try {
            // 1. 减少量子存储的数量
            long newStoredAmount = operation.storedAmount - extractAmount;
//            operation.quantumCache.setAmount((int) newStoredAmount);
            operation.quantumCache.setAmount(newStoredAmount);

            // 2. 更新副手物品的QuantumCache数据
            updateQuantumCacheInItem(operation.offhandItem, operation.quantumCache);

            // 3. 创建以太秘匣
            ItemStack cargoFragment = createCargoFragment(operation.storedItem, (int) extractAmount);
            if (cargoFragment == null) {
                player.sendMessage(ChatColor.RED + "Could not create the Aether Cargo Chest!");
                return;
            }

            // 4. 给予玩家以太秘匣
            giveCargoFragmentToPlayer(player, cargoFragment, operation.storedItem, extractAmount, newStoredAmount);

            // 5. 播放成功音效
            playSound(player, Sound.ENTITY_ITEM_PICKUP, 1.2f);

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Extraction failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建一个 CargoFragment 物品,代表某个物品的"Storage Fragment"
     */
    private ItemStack createCargoFragment(ItemStack original, int amount) {
        ItemStack fragment = CARGO_FRAGMENT.clone();
        fragment.setType(original.getType());
        ItemMeta meta = fragment.getItemMeta();
        if (meta == null) return null;

        // === 显示名:存储碎片: 原物品名 ===
        String itemName = ItemStackHelper.getDisplayName(original);
        if (original.hasItemMeta() && original.getItemMeta().hasDisplayName()) {
            itemName = original.getItemMeta().getDisplayName();
        }
        meta.setDisplayName("§bAether Cargo Chest");

        // === Lore:原物品 Lore + 数量 ===
        List<String> lore = new ArrayList<>();

        lore.add(itemName);

        if (original.hasItemMeta() && original.getItemMeta().hasLore()) {
            lore.addAll(original.getItemMeta().getLore());
            lore.add("");
        }

        lore.add("§fAmount: §a" + amount);
        meta.setLore(lore);

        // === 写入 PDC:原物品(JSON)+ 数量 ===
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // 存储原物品 JSON(便于还原)
        String json = itemToBase64(original.clone());
        if (json != null) {
            NamespacedKey keyItem = new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json");
            container.set(keyItem, PersistentDataType.STRING, json);
        }

        // 存储数量
        NamespacedKey keyAmount = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");
        container.set(keyAmount, PersistentDataType.INTEGER, amount);

        fragment.setItemMeta(meta);
        return fragment;
    }

    /**
     * 将物品序列化为Base64字符串
     */
    private String itemToBase64(ItemStack item) {
        try {
            return SameItemJudge.itemToBase64(item);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 更新QuantumCache数据到物品
     */
    private void updateQuantumCacheInItem(ItemStack item, QuantumCache cache) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 更新QuantumCache到物品的PDC
        DataTypeMethods.setCustom(meta, NetworksKeys.QUANTUM_STORAGE_INSTANCE,
                PersistentQuantumStorageType.TYPE, cache);
        cache.updateMetaLore(meta);
        item.setItemMeta(meta);
    }

    /**
     * 给予玩家以太秘匣
     */
    private void giveCargoFragmentToPlayer(Player player, ItemStack cargoFragment, ItemStack storedItem,
                                           long extractedAmount, long remainingAmount) {
        // 尝试添加到玩家背包
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(cargoFragment);

        // 如果有剩余物品(背包满了)
        if (!leftover.isEmpty()) {
            // 掉落在地上
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), item);
            }

            player.sendMessage(ChatColor.GREEN + "✓ Extracted " + ChatColor.YELLOW + extractedAmount +
                    ChatColor.GREEN + " items of " + ItemStackHelper.getDisplayName(storedItem));
            player.sendMessage(ChatColor.GRAY + "Inventory full; the Aether Cargo Chest was dropped at your feet!");
        } else {
            player.sendMessage(ChatColor.GREEN + "✓ Extracted " + ChatColor.YELLOW + extractedAmount +
                    ChatColor.GREEN + " items of " + ItemStackHelper.getDisplayName(storedItem));
            player.sendMessage(ChatColor.GRAY + "Aether Cargo Chest added to your inventory!");
        }

        // 显示量子存储剩余数量
        player.sendMessage(ChatColor.GRAY + "Remaining in quantum storage: " + remainingAmount + " items");

        // 更新副手物品显示
        player.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
    }

    /**
     * 设置超时任务
     */
    private void scheduleTimeout(UUID playerId) {
        Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> {
            ExtractOperation operation = pendingExtracts.remove(playerId);
            if (operation != null) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Extraction timed out and was cancelled.");
                }
                cleanupListenerIfNeeded();
            }
        }, 20 * 30); // 30秒
    }

    /**
     * 清理监听器
     */
    private void cleanupListenerIfNeeded() {
        if (pendingExtracts.isEmpty() && chatListener != null) {
            HandlerList.unregisterAll(chatListener);
            chatListener = null;
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(Player player, String message) {
        player.sendMessage(ChatColor.RED + message);
        playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f);
    }

    /**
     * 播放音效
     */
    private void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 0.8f, pitch);
    }

    /**
     * 提取操作信息类
     */
    private static class ExtractOperation {
        final UUID playerId;
        final ItemStack extractItem;
        final ItemStack offhandItem;
        ItemStack storedItem; // 改为非final,可能更新
        long storedAmount; // 改为非final,可能更新
        QuantumCache quantumCache; // 改为非final,可能更新
        long maxExtract; // 改为非final,可能更新

        ExtractOperation(UUID playerId, ItemStack extractItem, ItemStack offhandItem,
                         ItemStack storedItem, long storedAmount,
                         QuantumCache quantumCache, long maxExtract) {
            this.playerId = playerId;
            this.extractItem = extractItem;
            this.offhandItem = offhandItem;
            this.storedItem = storedItem;
            this.storedAmount = storedAmount;
            this.quantumCache = quantumCache;
            this.maxExtract = maxExtract;
        }
    }
}
