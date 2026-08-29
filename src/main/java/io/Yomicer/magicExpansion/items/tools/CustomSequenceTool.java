package io.Yomicer.magicExpansion.items.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomSequenceTool extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable, Listener {

    public CustomSequenceTool(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        // 注册事件监听器
        MagicExpansion.getInstance().getServer().getPluginManager().registerEvents(this, MagicExpansion.getInstance());
    }

    // 用于存储VoidTouch坐标的键
    private static final NamespacedKey KEY_X = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
    private static final NamespacedKey KEY_Y = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
    private static final NamespacedKey KEY_Z = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
    private static final NamespacedKey KEY_WORLD = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

    // 存储序列数据的键
    private static final NamespacedKey KEY_SLOT_DATA = new NamespacedKey(MagicExpansion.getInstance(), "slot_data");

    // 在类中添加这个字段
    private static final NamespacedKey KEY_SCRIPT = new NamespacedKey(MagicExpansion.getInstance(), "has_script");

    // Gson实例，用于JSON序列化
    private static final Gson gson = new GsonBuilder().create();

    // 存储正在运行的序列任务
    // 修复：改为 static ConcurrentHashMap（本物品为单例注册），支持静态 cleanup 清理
    private static final Map<UUID, SequenceTask> runningTasks = new ConcurrentHashMap<>();

    // 存储等待输入的玩家
    // 修复：改为 static ConcurrentHashMap，支持静态 cleanup 清理
    private static final Map<UUID, InputRequest> waitingForInput = new ConcurrentHashMap<>();

    /**
     * 修复：玩家退出时清理该玩家的会话数据：
     * 取消正在运行的序列任务并移除等待输入记录，防止内存泄漏与离线任务空跑
     */
    public static void cleanup(UUID uuid) {
        SequenceTask task = runningTasks.remove(uuid);
        if (task != null) {
            task.cancel(); // 取消该玩家未停止的序列任务
        }
        waitingForInput.remove(uuid);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            // 阻止默认行为
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            Player player = e.getPlayer();

            if (e.getHand() != EquipmentSlot.HAND) {
                return;
            }

            // Shift+右键打开存储界面
            if (player.isSneaking() && (e.getInteractEvent().getAction() == Action.RIGHT_CLICK_AIR || e.getInteractEvent().getAction() == Action.RIGHT_CLICK_BLOCK)) {
                openSequenceMenu(player, e.getItem());
            }
            // 直接右键运行序列
            else if (!player.isSneaking() && (e.getInteractEvent().getAction() == Action.RIGHT_CLICK_AIR || e.getInteractEvent().getAction() == Action.RIGHT_CLICK_BLOCK)) {
                startSequence(player, e.getItem());
            }
        };
    }

    private void openSequenceMenu(Player player, ItemStack item) {
        ChestMenu menu = new ChestMenu("序列配置菜单");

        // 设置玩家物品栏可点击
        menu.setPlayerInventoryClickable(true);

        PlayerSequenceData playerData = getPlayerData(item);

        // 添加54个屏障物品，显示步骤信息
        for (int i = 0; i < 54; i++) {
            final int slot = i;
            SlotData slotData = playerData.getSlotData(slot);

            ItemStack barrierItem = createBarrierItem(slot, slotData);
            menu.addItem(i, barrierItem);

            menu.addMenuClickHandler(i, (p, slot1, item1, action) -> {
                handleMenuClick(p, slot, action, playerData, item);
                return false;
            });
        }

        menu.open(player);
    }

    private ItemStack createBarrierItem(int slot, SlotData slotData) {
        List<String> lore = new ArrayList<>();
        lore.add("§7步骤 " + (slot + 1));
        lore.add("");
        // 检查是否有脚本
        boolean hasScript = checkIfSlotHasScript(slotData);

        // 显示当前设置的信息
        if (slotData.leftClickLocation != null) {
            lore.add("§a左键坐标: " + formatLocation(slotData.leftClickLocation));
        } else {
            lore.add("§c左键坐标: 未设置");
        }

        if (slotData.rightClickLocation != null) {
            lore.add("§a右键坐标: " + formatLocation(slotData.rightClickLocation));
        } else {
            lore.add("§c右键坐标: 未设置");
        }

        if (slotData.teleportLocation != null) {
            lore.add("§a传送坐标: " + formatLocation(slotData.teleportLocation));
        } else {
            lore.add("§c传送坐标: 未设置");
        }

        if (slotData.interval > 0) {
            lore.add("§b间隔时间: " + String.format("%.2f", slotData.interval) + "秒");
        } else {
            lore.add("§c间隔时间: 未设置");
        }

        if (slotData.message != null && !slotData.message.isEmpty()) {
            int maxLength = 50;
            String displayMessage = slotData.message.length() > maxLength ?
                    slotData.message.substring(0, maxLength) + "..." : slotData.message;

            if (slotData.message.startsWith("/")) {
                lore.add("§d指令: " + displayMessage);
            } else {
                lore.add("§d消息: " + displayMessage);
            }
        } else {
            lore.add("§c消息/指令: 未设置");
        }

        lore.add("");
        lore.add("§e手持虚空之触时:");
        lore.add("§e- 左键: 设置左键坐标");
        lore.add("§e- 右键: 设置右键坐标");
        lore.add("§e- Shift+左键: 设置传送坐标");
        lore.add("§6空手时:");
        lore.add("§6- 左键: 设置间隔时间");
        lore.add("§6- 右键: 设置消息/指令");
        lore.add("§6- Shift+左键: 重置此格子的脚本");
        lore.add("");
        lore.add("§a直接右键使用物品开始运行序列");

        // 根据是否有脚本选择不同的材质
        Material itemMaterial = hasScript ? Material.NETHER_STAR : Material.BARRIER;
        String itemName = hasScript ? "§6§l步骤 " + (slot + 1) + " §e(脚本)" : "§6步骤 " + (slot + 1);

        return new CustomItemStack(itemMaterial, itemName, lore.toArray(new String[0]));
    }

    // 添加检查脚本的方法
    private boolean checkIfSlotHasScript(SlotData slotData) {
        // 检查是否有任何设置（坐标、消息或间隔时间）
        return slotData.leftClickLocation != null ||
                slotData.rightClickLocation != null ||
                slotData.teleportLocation != null ||
                slotData.interval > 0 ||
                (slotData.message != null && !slotData.message.isEmpty());
    }

    private void handleMenuClick(Player player, int slot, ClickAction action, PlayerSequenceData playerData, ItemStack toolItem) {
        ItemStack cursorItem = player.getItemOnCursor();

        // 检查光标上是否有VoidTouch物品
        boolean hasVoidTouch = isVoidTouchItem(cursorItem);

        if (hasVoidTouch) {
            // 从VoidTouch物品中获取坐标
            Location voidTouchLocation = getVoidTouchLocation(cursorItem);

            if (voidTouchLocation == null) {
                player.sendMessage("§c虚空之触物品没有存储坐标！请先使用Shift+右键方块绑定坐标。");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (action.isShiftClicked()) {
                if (!action.isRightClicked()) {
                    // Shift+左键设置传送坐标
                    playerData.setTeleportLocation(slot, voidTouchLocation);
                    player.sendMessage("§a已设置步骤 " + (slot + 1) + " 的传送坐标: " + formatLocation(voidTouchLocation));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                }
            } else {
                if (!action.isRightClicked()) {
                    // 左键设置左键坐标
                    playerData.setLeftClickLocation(slot, voidTouchLocation);
                    player.sendMessage("§a已设置步骤 " + (slot + 1) + " 的左键坐标: " + formatLocation(voidTouchLocation));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                } else if (action.isRightClicked()) {
                    // 右键设置右键坐标
                    playerData.setRightClickLocation(slot, voidTouchLocation);
                    player.sendMessage("§a已设置步骤 " + (slot + 1) + " 的右键坐标: " + formatLocation(voidTouchLocation));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                }
            }
        } else {
            // 没有物品时的处理
            if (action.isShiftClicked() && !action.isRightClicked()) {
                // 空手Shift+左键重置格子
                resetSlot(player, slot, playerData, toolItem);
            } else {
                // 没有物品时的处理
                if (!action.isRightClicked()) {
                    // 左键设置间隔时间
                    setIntervalTime(player, slot, playerData, toolItem);
                } else if (action.isRightClicked()) {
                    // 右键设置消息/指令
                    setMessageOrCommand(player, slot, playerData, toolItem);
                }
            }
        }

        // 保存数据并更新菜单
        savePlayerData(playerData, toolItem);
        openSequenceMenu(player, toolItem);
    }

    // 重置格子内容
    private void resetSlot(Player player, int slot, PlayerSequenceData playerData, ItemStack toolItem) {
        SlotData slotData = playerData.getSlotData(slot);

        // 重置所有数据
        slotData.leftClickLocation = null;
        slotData.rightClickLocation = null;
        slotData.teleportLocation = null;
        slotData.interval = 0;
        slotData.message = "";

        savePlayerData(playerData, toolItem);
        player.sendMessage("§a已重置步骤 " + (slot + 1) + " 的所有设置");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
    }

    private void startSequence(Player player, ItemStack toolItem) {
        UUID playerId = player.getUniqueId();

        // 如果序列已经在运行，则停止它
        if (runningTasks.containsKey(playerId)) {
            stopSequence(player);
            return;
        }

        PlayerSequenceData playerData = getPlayerData(toolItem);

        // 检查是否有设置任何步骤
        boolean hasSteps = false;
        for (int i = 0; i < 54; i++) {
            SlotData slotData = playerData.getSlotData(i);
            if (slotData.hasAnyAction()) {
                hasSteps = true;
                break;
            }
        }

        if (!hasSteps) {
            player.sendMessage("§c没有设置任何序列步骤！请先使用Shift+右键配置序列。");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // 创建并启动序列任务
        SequenceTask task = new SequenceTask(player, toolItem, playerData);
        runningTasks.put(playerId, task);
        task.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L); // 每tick检查一次

        player.sendMessage("§a序列已开始运行！再次右键停止。");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }

    private void stopSequence(Player player) {
        UUID playerId = player.getUniqueId();
        if (runningTasks.containsKey(playerId)) {
            runningTasks.get(playerId).cancel();
            runningTasks.remove(playerId);
            player.sendMessage("§c序列已停止运行。");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    // 设置间隔时间 - 完整实现
    private void setIntervalTime(Player player, int slot, PlayerSequenceData playerData, ItemStack toolItem) {

        new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
                player.sendMessage("§6请在聊天框中输入间隔时间（秒，最低0.01）:");
                player.sendMessage("§c输入 'cancel' 取消设置");
                // 记录等待输入
                waitingForInput.put(player.getUniqueId(), new InputRequest(InputType.INTERVAL, slot, playerData, toolItem));
            }
        }.runTaskLater(MagicExpansion.getInstance(), 1L);

    }

    // 设置消息/指令 - 完整实现
    private void setMessageOrCommand(Player player, int slot, PlayerSequenceData playerData, ItemStack toolItem) {
        // 延迟一 tick 关闭菜单，确保先处理完点击事件
        new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
                player.sendMessage("§6=== 序列工具设置 ===");
                player.sendMessage("§6请在聊天框中输入消息或指令:");
                player.sendMessage("§7- 输入普通消息: 序列运行时发送聊天消息");
                player.sendMessage("§7- 输入以/开头的指令: 序列运行时执行指令");
                player.sendMessage("§c输入 'cancel' 取消设置");
                player.sendMessage("§e注意: 您现在可以输入指令（如 /help），它会被保存而不会立即执行");

                // 记录等待输入
                waitingForInput.put(player.getUniqueId(), new InputRequest(InputType.MESSAGE, slot, playerData, toolItem));
            }
        }.runTaskLater(MagicExpansion.getInstance(), 1L);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 如果玩家正在等待输入，取消指令执行并处理输入
        if (waitingForInput.containsKey(playerId)) {
            event.setCancelled(true);

            InputRequest request = waitingForInput.get(playerId);

            // 只处理 MESSAGE 类型的输入请求
            if (request.type != InputType.MESSAGE) {
                player.sendMessage("§c请先完成当前设置！输入 'cancel' 取消");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            String command = event.getMessage(); // 这会包含 "/" 前缀

            // 保存指令到序列工具
            request.playerData.setMessage(request.slot, command);
            savePlayerData(request.playerData, request.toolItem);

            player.sendMessage("§a已设置步骤 " + (request.slot + 1) + " 的指令为: " + command);
            player.sendMessage("§7注意: 这个指令将在序列运行时执行");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);

            waitingForInput.remove(playerId);

            // 重新打开菜单
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        openSequenceMenu(player, request.toolItem);
                    }
                }
            }.runTask(MagicExpansion.getInstance());
        }
    }

    // 处理玩家聊天输入
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!waitingForInput.containsKey(playerId)) {
            return;
        }

        // 如果消息以"/"开头，说明是指令，我们不处理（由指令预处理器处理）
        if (event.getMessage().startsWith("/")) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        InputRequest request = waitingForInput.get(playerId);

        // 处理取消
        if (message.equalsIgnoreCase("cancel")) {
            waitingForInput.remove(playerId);
            player.sendMessage("§c已取消设置。");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

            // 取消后重新打开菜单
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        openSequenceMenu(player, request.toolItem);
                    }
                }
            }.runTask(MagicExpansion.getInstance());
            return;
        }

        // 根据输入类型处理
        switch (request.type) {
            case INTERVAL:
                try {
                    double interval = Double.parseDouble(message);
                    if (interval < 0.01) {
                        player.sendMessage("§c间隔时间必须大于等于0.01秒！");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    request.playerData.setInterval(request.slot, interval);
                    savePlayerData(request.playerData, request.toolItem);
                    player.sendMessage("§a已设置步骤 " + (request.slot + 1) + " 的间隔时间为 " + String.format("%.2f", interval) + " 秒");
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c请输入有效的数字！");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                break;

            case MESSAGE:
                // 处理普通消息（非指令）
                request.playerData.setMessage(request.slot, message);
                savePlayerData(request.playerData, request.toolItem);

                player.sendMessage("§a已设置步骤 " + (request.slot + 1) + " 的消息为: " + message);
                player.sendMessage("§7注意: 这个消息将在序列运行时发送");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                break;
        }

        waitingForInput.remove(playerId);

        // 重新打开菜单
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    openSequenceMenu(player, request.toolItem);
                }
            }
        }.runTask(MagicExpansion.getInstance());
    }

    private class SequenceTask extends BukkitRunnable {
        private final Player player;
        private final ItemStack toolItem;
        private final PlayerSequenceData playerData;
        private int currentStep = 0;
        private int delayTicks = 0;
        private boolean isRunningStep = false;
        private final List<Integer> activeSteps = new ArrayList<>();

        public SequenceTask(Player player, ItemStack toolItem, PlayerSequenceData playerData) {
            this.player = player;
            this.toolItem = toolItem;
            this.playerData = playerData;

            // 预先计算有设置的步骤
            for (int i = 0; i < 54; i++) {
                if (playerData.getSlotData(i).hasAnyAction()) {
                    activeSteps.add(i);
                }
            }
        }

        @Override
        public void run() {
            // 检查玩家是否还持有这个物品（只检查主手）
            if (!isPlayerHoldingTool()) {
                stopSequence(player);
                return;
            }

            // 如果正在执行步骤，等待完成
            if (isRunningStep) {
                return;
            }

            // 延迟计数器
            if (delayTicks > 0) {
                delayTicks--;
                return;
            }

            // 如果没有激活的步骤，停止
            if (activeSteps.isEmpty()) {
                stopSequence(player);
                return;
            }

            // 执行当前步骤
            executeStep(activeSteps.get(currentStep));

            // 移动到下一步
            currentStep = (currentStep + 1) % activeSteps.size();
        }

        private boolean isPlayerHoldingTool() {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            return isSameTool(mainHand);
        }

        private boolean isSameTool(ItemStack item) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }

            // 直接比较物品，而不是SlimefunItem类型
            if (item.isSimilar(toolItem)) {
                return true;
            }

            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            return sfItem != null && sfItem instanceof CustomSequenceTool;
        }

        private void executeStep(int step) {
            SlotData slotData = playerData.getSlotData(step);

            if (!slotData.hasAnyAction()) {
                return; // 跳过没有设置的步骤
            }

            isRunningStep = true;

            try {
                // 执行左键操作
                if (slotData.leftClickLocation != null) {
                    simulateLeftClick(slotData.leftClickLocation);
                }

                // 执行右键操作
                if (slotData.rightClickLocation != null) {
                    simulateRightClick(slotData.rightClickLocation);
                }

                // 执行传送操作
                if (slotData.teleportLocation != null) {
                    player.teleport(slotData.teleportLocation);
                    player.sendMessage("§e传送到步骤 " + (step + 1) + " 的坐标");
                }

                // 执行消息/指令
                if (slotData.message != null && !slotData.message.isEmpty()) {
                    if (slotData.message.startsWith("/")) {
                        // 执行指令
                        player.performCommand(slotData.message.substring(1));
                    } else {
                        player.chat(slotData.message);
                    }
                }

                player.sendMessage("§a执行步骤 " + (step + 1));

            } catch (Exception e) {
                player.sendMessage("§c执行步骤 " + (step + 1) + " 时出现错误: " + e.getMessage());
            } finally {
                // 执行完步骤后再次检查是否还持有物品
                if (!isPlayerHoldingTool()) {
                    stopSequence(player);
                    return;
                }
            }

            // 设置延迟（转换为ticks，1秒=20ticks）
            double intervalSeconds = slotData.interval > 0 ? slotData.interval : 0.01;
            delayTicks = (int) Math.max(intervalSeconds * 20, 1); // 最少1tick延迟
            isRunningStep = false;
        }

        private void simulateLeftClick(Location location) {
            Block block = location.getBlock();
            PlayerInteractEvent event = new PlayerInteractEvent(
                    player,
                    Action.LEFT_CLICK_BLOCK,
                    new ItemStack(Material.AIR),
                    block,
                    BlockFace.SELF
            );
            Bukkit.getPluginManager().callEvent(event);
        }

        private void simulateRightClick(Location location) {
            Block block = location.getBlock();
            PlayerInteractEvent event = new PlayerInteractEvent(
                    player,
                    Action.RIGHT_CLICK_BLOCK,
                    new ItemStack(Material.AIR),
                    block,
                    BlockFace.SELF
            );
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    private boolean isVoidTouchItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return sfItem != null && sfItem instanceof VoidTouchScript;
    }

    private Location getVoidTouchLocation(ItemStack voidTouchItem) {
        ItemMeta meta = voidTouchItem.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(KEY_X, PersistentDataType.INTEGER) &&
                container.has(KEY_Y, PersistentDataType.INTEGER) &&
                container.has(KEY_Z, PersistentDataType.INTEGER) &&
                container.has(KEY_WORLD, PersistentDataType.STRING)) {

            String worldName = container.get(KEY_WORLD, PersistentDataType.STRING);
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                int x = container.get(KEY_X, PersistentDataType.INTEGER);
                int y = container.get(KEY_Y, PersistentDataType.INTEGER);
                int z = container.get(KEY_Z, PersistentDataType.INTEGER);
                return new Location(world, x, y, z);
            }
        }

        return null;
    }

    private String formatLocation(Location loc) {
        if (loc == null) return "null";
        return String.format("X:%d Y:%d Z:%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    // 数据管理方法 - 修复了数据存储问题
    private PlayerSequenceData getPlayerData(ItemStack item) {
        PlayerSequenceData data = new PlayerSequenceData();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(KEY_SLOT_DATA, PersistentDataType.STRING)) {
                String jsonData = container.get(KEY_SLOT_DATA, PersistentDataType.STRING);
                try {
                    // 使用Gson反序列化JSON数据
                    Type type = new TypeToken<Map<Integer, SerializableSlotData>>(){}.getType();
                    Map<Integer, SerializableSlotData> serializedData = gson.fromJson(jsonData, type);

                    if (serializedData != null) {
                        for (Map.Entry<Integer, SerializableSlotData> entry : serializedData.entrySet()) {
                            int slot = entry.getKey();
                            SerializableSlotData serializedSlot = entry.getValue();
                            SlotData slotData = data.getSlotData(slot);

                            // 反序列化位置数据
                            if (serializedSlot.leftClickLocation != null) {
                                slotData.leftClickLocation = deserializeLocation(serializedSlot.leftClickLocation);
                            }
                            if (serializedSlot.rightClickLocation != null) {
                                slotData.rightClickLocation = deserializeLocation(serializedSlot.rightClickLocation);
                            }
                            if (serializedSlot.teleportLocation != null) {
                                slotData.teleportLocation = deserializeLocation(serializedSlot.teleportLocation);
                            }

                            // 反序列化其他数据
                            slotData.interval = serializedSlot.interval;
                            slotData.message = serializedSlot.message;
                        }
                    }
                } catch (Exception e) {
                    MagicExpansion.getInstance().getLogger().warning("反序列化物品数据失败: " + e.getMessage());
                }
            }
        }
        return data;
    }

    private void savePlayerData(PlayerSequenceData data, ItemStack item) {
        // 保存到物品NBT
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            try {
                // 创建可序列化的数据结构
                Map<Integer, SerializableSlotData> serializedData = new HashMap<>();

                for (int i = 0; i < 54; i++) {
                    SlotData slotData = data.getSlotData(i);
                    SerializableSlotData serializedSlot = new SerializableSlotData();

                    // 序列化位置数据
                    if (slotData.leftClickLocation != null) {
                        serializedSlot.leftClickLocation = serializeLocation(slotData.leftClickLocation);
                    }
                    if (slotData.rightClickLocation != null) {
                        serializedSlot.rightClickLocation = serializeLocation(slotData.rightClickLocation);
                    }
                    if (slotData.teleportLocation != null) {
                        serializedSlot.teleportLocation = serializeLocation(slotData.teleportLocation);
                    }

                    // 序列化其他数据
                    serializedSlot.interval = slotData.interval;
                    serializedSlot.message = slotData.message;

                    serializedData.put(i, serializedSlot);
                }

                // 使用Gson序列化为JSON字符串
                String jsonData = gson.toJson(serializedData);
                container.set(KEY_SLOT_DATA, PersistentDataType.STRING, jsonData);
                item.setItemMeta(meta);
            } catch (Exception e) {
                MagicExpansion.getInstance().getLogger().warning("序列化物品数据失败: " + e.getMessage());
            }
        }
    }

    // 位置序列化方法
    private String serializeLocation(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
    }

    // 位置反序列化方法
    private Location deserializeLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;
        try {
            String[] parts = locationString.split(":");
            if (parts.length == 6) {
                World world = Bukkit.getWorld(parts[0]);
                if (world != null) {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    float yaw = Float.parseFloat(parts[4]);
                    float pitch = Float.parseFloat(parts[5]);
                    return new Location(world, x, y, z, yaw, pitch);
                }
            }
        } catch (Exception e) {
            MagicExpansion.getInstance().getLogger().warning("反序列化位置失败: " + locationString);
        }
        return null;
    }

    // 数据存储类
    private static class PlayerSequenceData {
        private final SlotData[] slots = new SlotData[54];

        public PlayerSequenceData() {
            for (int i = 0; i < 54; i++) {
                slots[i] = new SlotData();
            }
        }

        public SlotData getSlotData(int slot) {
            if (slot >= 0 && slot < 54) {
                return slots[slot];
            }
            return new SlotData();
        }

        public void setLeftClickLocation(int slot, Location location) {
            if (slot >= 0 && slot < 54) {
                slots[slot].leftClickLocation = location.clone();
            }
        }

        public void setRightClickLocation(int slot, Location location) {
            if (slot >= 0 && slot < 54) {
                slots[slot].rightClickLocation = location.clone();
            }
        }

        public void setTeleportLocation(int slot, Location location) {
            if (slot >= 0 && slot < 54) {
                slots[slot].teleportLocation = location.clone();
            }
        }

        public void setInterval(int slot, double seconds) {
            if (slot >= 0 && slot < 54) {
                slots[slot].interval = seconds;
            }
        }

        public void setMessage(int slot, String message) {
            if (slot >= 0 && slot < 54) {
                slots[slot].message = message;
            }
        }
    }

    private static class SlotData {
        public Location leftClickLocation;
        public Location rightClickLocation;
        public Location teleportLocation;
        public double interval = 0;
        public String message = "";

        public boolean hasAnyAction() {
            return leftClickLocation != null ||
                    rightClickLocation != null ||
                    teleportLocation != null ||
                    (message != null && !message.isEmpty());
        }
    }

    // 可序列化的数据类
    private static class SerializableSlotData {
        public String leftClickLocation;
        public String rightClickLocation;
        public String teleportLocation;
        public double interval = 0;
        public String message = "";
    }

    // 输入请求类型
    private enum InputType {
        INTERVAL, MESSAGE
    }

    // 输入请求类
    private static class InputRequest {
        public final InputType type;
        public final int slot;
        public final PlayerSequenceData playerData;
        public final ItemStack toolItem;

        public InputRequest(InputType type, int slot, PlayerSequenceData playerData, ItemStack toolItem) {
            this.type = type;
            this.slot = slot;
            this.playerData = playerData;
            this.toolItem = toolItem;
        }
    }
}