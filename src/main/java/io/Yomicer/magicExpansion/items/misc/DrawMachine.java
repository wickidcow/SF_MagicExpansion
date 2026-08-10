package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSpawnReason;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.chat.ChatInput;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class DrawMachine extends SlimefunItem implements EnergyNetComponent {

    private static final int TEMPLATE_SLOT = 10;
    private static final int SETTINGS_SLOT = 37;
    private static final int INFO_SLOT = 1;
    private static final int OUTPUT_SLOT = 19;

    // 悬浮物存储 - 修改为存储物品实体和盔甲架
    private final Map<Location, Entity> itemDisplayMap = new HashMap<>();
    private final Map<Location, ArmorStand> textDisplayMap = new HashMap<>();
    // 奖券投影掉落物式动画任务(按机器位置管理)
    private static final Map<Location, BukkitTask> HOLOGRAM_ANIMATIONS = new HashMap<>();

    // 奖励槽位范围
    private static final int[] REWARD_SLOTS = {
            3, 4, 5, 6, 7, 8,
            12, 13, 14, 15, 16, 17,
            21, 22, 23, 24, 25, 26,
            30, 31, 32, 33, 34, 35,
            39, 40, 41, 42, 43, 44,
            48, 49, 50, 51, 52, 53
    };

    private final int[] pinkBorder = {0, 1, 2, 9, 11, 18, 20};
    private final int[] blueBorder = {27, 28, 29, 36, 38, 45, 46, 47}; // 改为淡蓝色玻璃板

    // 自动输出状态存储
    private final Map<Location, Boolean> autoOutputMap = new HashMap<>();


    public DrawMachine(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        constructMenu("抽奖机");
        addItemHandler(onBlockPlace(), onBlockBreak());
    }

    @Override
    public void preRegister() {
        addItemHandler(new me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                DrawMachine.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true; // 改为同步，避免异步问题
            }
        });
    }

    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                // 初始化自动输出状态
                autoOutputMap.put(e.getBlock().getLocation(), false);
                // 创建初始悬浮物
                createHologram(e.getBlock());
            }
        };
    }

    // 在破坏机器时也要移除悬浮物 - 修改BlockBreakHandler
    @Nonnull
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            public void onBlockBreak(Block b) {
                BlockMenu menu = StorageCacheUtils.getMenu(b.getLocation());
                if (menu != null) {
                    // 在破坏时掉落所有物品
                    for (int slot : REWARD_SLOTS) {
                        ItemStack item = menu.getItemInSlot(slot);
                        if (item != null && item.getType() != Material.AIR) {
                            b.getWorld().dropItemNaturally(b.getLocation(), item);
                        }
                    }

                    // 掉落模板物品，数量为存储的数量 - 修改为分批掉落
                    int storedAmount = getStoredTemplateAmount(b.getLocation());
                    if (storedAmount > 0) {
                        ItemStack originalItem = getStoredTemplateItemData(b);
                        if (originalItem != null) {
                            // 使用分批掉落方法
                            dropTemplateItemsInBatches(b.getLocation(), originalItem, storedAmount);
                        }
                    }

                    ItemStack outputItem = menu.getItemInSlot(OUTPUT_SLOT);
                    if (outputItem != null && outputItem.getType() != Material.AIR) {
                        b.getWorld().dropItemNaturally(b.getLocation(), outputItem);
                    }
                }
                // 清理自动输出状态和存储的数据
                autoOutputMap.remove(b.getLocation());
                BlockStorage.addBlockInfo(b, "templateAmount", "0");
                BlockStorage.addBlockInfo(b, "templateItemData", ""); // 清除物品数据

                // 移除悬浮物
                removeHologram(b);
            }
        };
    }

    // 分批掉落模板物品的方法
    private void dropTemplateItemsInBatches(Location location, ItemStack templateItem, int totalAmount) {
        int maxDropSize = 64; // 单次最大掉落数量

        while (totalAmount > 0) {
            // 创建一个新的物品副本，设置当前批次的数量
            ItemStack dropItem = templateItem.clone();
            int currentBatchSize = Math.min(totalAmount, maxDropSize);
            dropItem.setAmount(currentBatchSize);

            // 掉落当前批次的物品
            location.getWorld().dropItemNaturally(location, dropItem);

            // 更新剩余数量
            totalAmount -= currentBatchSize;
        }
    }

    protected void tick(Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) return;

        updateMenu(menu, block);

        // 处理自动输出
        if (autoOutputMap.getOrDefault(block.getLocation(), false)) {
            handleAutoOutput(menu, block);
        }

        // 处理抽奖逻辑
        processDraw(block, menu);

        // 更新悬浮物显示
        updateHologram(block);
    }

    public static final String HOLOGRAM_PREFIX = "§d抽奖机 §3显示 - §e";
    // 创建悬浮物 - 修改为创建物品实体和文字显示
    private void createHologram(Block block) {

        removeHologram(block);

        Location baseLoc = block.getLocation().clone().add(0.5, 1.2, 0.5);
        Location itemLoc = baseLoc.clone().add(0, 1.6, 0);   // 物品在 +2.4
        Location line1Loc = baseLoc.clone().add(0, 0.7, 0);  // 第一行 +1.9
        Location line2Loc = baseLoc.clone().add(0, 0.4, 0);  // 第二行 +1.6
        Location line3Loc = baseLoc.clone().add(0, 0.1, 0);  // 第三行 +1.3

        // === 创建物品悬浮体(使用 ItemDisplay 显示实体, 不会被扫地插件当掉落物拾取) ===
        ItemStack placeholder = new CustomItemStack(Material.BARRIER, HOLOGRAM_PREFIX + "ITEM");
        ItemDisplay itemEntity = itemLoc.getWorld().spawn(itemLoc, ItemDisplay.class);
        itemEntity.setItemStack(placeholder);
        // 缩放为掉落物大小(约 0.25 格), 保持之前掉落物的观感
        itemEntity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(0.25f, 0.25f, 0.25f),
                new Quaternionf()
        ));
        itemEntity.setInvulnerable(true);
        itemEntity.setPersistent(true);
        itemEntity.setMetadata("draw-machine-hologram", new org.bukkit.metadata.FixedMetadataValue(MagicExpansion.getInstance(), true));
        itemDisplayMap.put(block.getLocation(), itemEntity);
        BlockStorage.addBlockInfo(block, "hologram_item", itemEntity.getUniqueId().toString());
        startHologramAnimation(block.getLocation(), itemEntity);

//        // 创建物品显示实体
//        Item displayItem = block.getWorld().dropItem(itemLoc, new ItemStack(Material.BARRIER));
//        displayItem.setPickupDelay(Integer.MAX_VALUE);
//        displayItem.setInvulnerable(true);
//        displayItem.setGravity(false);
//        displayItem.setVelocity(new Vector(0, 0, 0));
//        displayItem.setMetadata("draw-machine-hologram", new org.bukkit.metadata.FixedMetadataValue(MagicExpansion.getInstance(), true));

        // 创建三行文字显示的盔甲架
        ArmorStand line1 = createTextArmorStand(line1Loc, "§c未设置模板");
        ArmorStand line2 = createTextArmorStand(line2Loc, "§7请先设置抽奖货币");
        ArmorStand line3 = createTextArmorStand(line3Loc, "§7奖池剩余: 0个");

//        itemDisplayMap.put(block.getLocation(), displayItem);
        textDisplayMap.put(block.getLocation(), line1); // 主文本存储

        // 存储额外的文本行
//        BlockStorage.addBlockInfo(block, "hologram_line", displayItem.getUniqueId().toString());
        BlockStorage.addBlockInfo(block, "hologram_line1", line1.getUniqueId().toString());
        BlockStorage.addBlockInfo(block, "hologram_line2", line2.getUniqueId().toString());
        BlockStorage.addBlockInfo(block, "hologram_line3", line3.getUniqueId().toString());
    }

    /** 掉落物式动画: 缓慢旋转 + 轻微上下浮动, 仍使用 ItemDisplay 不被扫地插件误收 */
    private void startHologramAnimation(Location location, ItemDisplay display) {
        cancelHologramAnimation(location);
        final float[] angle = {0f};
        final int[] tick = {0};
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (display.isDead() || !display.isValid()) {
                    cancel();
                    HOLOGRAM_ANIMATIONS.remove(location);
                    return;
                }
                tick[0]++;
                angle[0] += 0.12f; // 缓慢旋转, 约 7°/tick
                float bob = (float) Math.sin(tick[0] * 0.08) * 0.06f; // 轻微上下浮动
                display.setTransformation(new Transformation(
                        new Vector3f(0, bob, 0),
                        new Quaternionf().rotateY(angle[0]),
                        new Vector3f(0.25f, 0.25f, 0.25f),
                        new Quaternionf()
                ));
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
        HOLOGRAM_ANIMATIONS.put(location, task);
    }

    private static void cancelHologramAnimation(Location location) {
        BukkitTask task = HOLOGRAM_ANIMATIONS.remove(location);
        if (task != null) {
            task.cancel();
        }
    }
    // 创建文本盔甲架的辅助方法
    private ArmorStand createTextArmorStand(Location loc, String text) {
        ArmorStand armorStand = loc.getWorld().spawn(loc, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setPersistent(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(text);
        armorStand.setAI(false);
        armorStand.setCollidable(false);
        armorStand.setMarker(true);
        return armorStand;
    }

    private void updateHologram(Block block) {
        Entity itemEntity = itemDisplayMap.get(block.getLocation());
//        ArmorStand textEntity = textDisplayMap.get(block.getLocation());

        // 获取其他行的盔甲架
        ArmorStand line1 = getHologramLine(block, "hologram_line1");
        ArmorStand line2 = getHologramLine(block, "hologram_line2");
        ArmorStand line3 = getHologramLine(block, "hologram_line3");

        if (itemEntity == null || itemEntity.isDead() ||
                line1 == null || line1.isDead() ||
                line2 == null || line2.isDead() ||
                line3 == null || line3.isDead()) {
            createHologram(block);
            return;
        }

//        if (itemEntity == null || textEntity == null || itemEntity.isDead() || textEntity.isDead()) {
//            createHologram(block);
//            itemEntity = itemDisplayMap.get(block.getLocation());
//            textEntity = textDisplayMap.get(block.getLocation());
//            line2 = getHologramLine(block, "hologram_line2");
//            line3 = getHologramLine(block, "hologram_line3");
//            if (itemEntity == null || textEntity == null) return;
//        }

        ItemStack originalTemplateItem = getStoredTemplateItemData(block);
        int storedAmount = getStoredTemplateAmount(block.getLocation());

        // 获取所需数量
        int requiredAmount = 1;
        try {
            String reqStr = BlockStorage.getLocationInfo(block.getLocation(), "requiredAmount");
            if (reqStr != null) requiredAmount = Integer.parseInt(reqStr);
        } catch (NumberFormatException ignored) {
            requiredAmount = 1;
        }

        // 计算奖品剩余数量
        int totalRewards = 0;
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu != null) {
            for (int slot : REWARD_SLOTS) {
                ItemStack reward = menu.getItemInSlot(slot);
                if (reward != null && reward.getType() != Material.AIR) {
                    totalRewards += reward.getAmount();
                }
            }
        }

        if (originalTemplateItem != null) {
            String itemName = ItemStackHelper.getDisplayName(originalTemplateItem);

            // 更新物品显示 - 显示需要的数量
            if (itemEntity instanceof ItemDisplay itemDisplay) {
                ItemStack displayStack = originalTemplateItem.clone();
                displayStack.setAmount(Math.min(requiredAmount, displayStack.getMaxStackSize()));
                itemDisplay.setItemStack(displayStack);
            }

            // 更新三行文字显示
//            textEntity.setCustomName("§a每次抽奖所需: "+ itemName + "§r§b x " + requiredAmount);
            if (line1 != null) line1.setCustomName("§a每次抽奖所需: " + itemName + " §b x " + requiredAmount);
            if (line2 != null) line2.setCustomName("§b抽奖机内已收集货币: " + storedAmount + "个");
            if (line3 != null) line3.setCustomName("§d奖池奖品剩余: " + totalRewards + "个");
        } else {
            // 更新物品显示为屏障
            if (itemEntity instanceof ItemDisplay itemDisplay) {
                itemDisplay.setItemStack(new ItemStack(Material.BARRIER));
            }

            // 更新三行文字显示
//            textEntity.setCustomName("§c未设置模板");
            if (line1 != null) line1.setCustomName("§c未设置模板");
            if (line2 != null) line2.setCustomName("§7请先设置抽奖货币");
            if (line3 != null) line3.setCustomName("§7奖池剩余: " + totalRewards + "个");
        }
    }

    // 获取特定行的盔甲架
    private ArmorStand getHologramLine(Block block, String storageKey) {
        String uuidStr = BlockStorage.getLocationInfo(block.getLocation(), storageKey);
        if (uuidStr == null) return null;

        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof ArmorStand && !entity.isDead()) {
                return (ArmorStand) entity;
            }
        } catch (IllegalArgumentException e) {
            // UUID格式无效
        }
        return null;
    }
    private Entity getHologramEntityLine(Block block, String storageKey) {
        String uuidStr = BlockStorage.getLocationInfo(block.getLocation(), storageKey);
        if (uuidStr == null) return null;

        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null && !entity.isDead()) {
                return entity;
            }
        } catch (IllegalArgumentException e) {
            // UUID格式无效
        }
        return null;
    }


    // 移除悬浮物 - 修改为移除两个实体
    private void removeHologram(Block block) {
        cancelHologramAnimation(block.getLocation());
        Entity itemEntity = itemDisplayMap.remove(block.getLocation());
        ArmorStand textEntity = textDisplayMap.remove(block.getLocation());

        // 移除其他行的盔甲架
        Entity line = getHologramEntityLine(block, "hologram_item");
        ArmorStand line1 = getHologramLine(block, "hologram_line1");
        ArmorStand line2 = getHologramLine(block, "hologram_line2");
        ArmorStand line3 = getHologramLine(block, "hologram_line3");

        if (itemEntity != null && !itemEntity.isDead()) {
            itemEntity.remove();
        }
        if (textEntity != null && !textEntity.isDead()) {
            textEntity.remove();
        }
        if (line != null && !line.isDead()) {
            line.remove();
        }
        if (line1 != null && !line1.isDead()) {
            line1.remove();
        }
        if (line2 != null && !line2.isDead()) {
            line2.remove();
        }
        if (line3 != null && !line3.isDead()) {
            line3.remove();
        }

        // 清除存储的UUID
        BlockStorage.addBlockInfo(block, "hologram_item", null);
        BlockStorage.addBlockInfo(block, "hologram_line1", null);
        BlockStorage.addBlockInfo(block, "hologram_line2", null);
        BlockStorage.addBlockInfo(block, "hologram_line3", null);
    }

    private void handleAutoOutput(BlockMenu menu, Block block) {
        ItemStack originalTemplateItem = getStoredTemplateItemData(block);
        if (originalTemplateItem == null) {
            return;
        }

        int storedAmount = getStoredTemplateAmount(block.getLocation());
        if (storedAmount <= 0) {
            return;
        }

        outputToSlot(menu, block, originalTemplateItem);
    }

    // 获取存储的模板物品数量
    private int getStoredTemplateAmount(Location location) {
        String amountStr = BlockStorage.getLocationInfo(location, "templateAmount");
        if (amountStr == null) {
            return 0;
        }
        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 设置存储的模板物品数量
    private void setStoredTemplateAmount(Block block, int amount) {
        BlockStorage.addBlockInfo(block, "templateAmount", String.valueOf(Math.max(0, amount)));
    }

    // 存储原始物品数据到BlockStorage（使用完整的序列化方法）
    private void storeTemplateItemData(Block block, ItemStack item) {
        String serializedItem = itemToBase64(item);
        if (serializedItem != null) {
            BlockStorage.addBlockInfo(block, "templateItemData", serializedItem);
        }
    }

    // 从BlockStorage获取原始物品数据（使用完整的反序列化方法）
    private ItemStack getStoredTemplateItemData(Block block) {
        String serializedItem = BlockStorage.getLocationInfo(block.getLocation(), "templateItemData");
        if (serializedItem == null || serializedItem.isEmpty()) {
            return null;
        }
        return itemFromBase64(serializedItem);
    }

    // 获取物品显示名称
    private String getItemDisplayName(ItemStack item) {
        if (item == null) return "未知物品";

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        } else {
            // 将英文名转换为中文显示
            String typeName = item.getType().toString().toLowerCase();
            String[] words = typeName.split("_");
            StringBuilder displayName = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    displayName.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1))
                            .append(" ");
                }
            }
            return displayName.toString().trim();
        }
    }

    // 创建模板物品显示
    private ItemStack createTemplateDisplayItem(ItemStack baseItem, int storedAmount, boolean isAutoOutput, Block block) {
        ItemStack displayItem = baseItem.clone();
        ItemMeta meta = displayItem.getItemMeta();

        List<String> lore = new ArrayList<>();

        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
        }

        // 获取每次抽奖需要的数量
        String requiredAmountStr = BlockStorage.getLocationInfo(block.getLocation(), "requiredAmount");
        int requiredAmount = 1;
        try {
            requiredAmount = Integer.parseInt(requiredAmountStr);
        } catch (NumberFormatException e) {
            requiredAmount = 1;
        }

        // 计算奖品剩余数量
        int totalRewards = 0;
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu != null) {
            for (int slot : REWARD_SLOTS) {
                ItemStack reward = menu.getItemInSlot(slot);
                if (reward != null && reward.getType() != Material.AIR) {
                    totalRewards += reward.getAmount();
                }
            }
        }

        // 添加分隔线和数量显示
        lore.add("");
        lore.add("§7每次抽奖需要: §e" + requiredAmount + " 个");
        lore.add("§7当前存储数量: §e" + storedAmount + " 个");
        lore.add("§7奖池剩余数量: §e" + totalRewards + " 个");
        lore.add("");
        lore.add("§e左键点击: §7设置模板物品");
        lore.add("§eShift+左键: §7" + (isAutoOutput ? "关闭" : "开启") + "自动输出");
        lore.add("§e右键点击: §7提取一组物品");
        lore.add("§eShift+右键: §7填满背包");
        lore.add("");
        lore.add("§c自动输出: " + (isAutoOutput ? "§a开启" : "§7关闭"));

        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        displayItem.setAmount(Math.min(requiredAmount, displayItem.getMaxStackSize()));

        return displayItem;
    }


    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {
            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public void newInstance(BlockMenu menu, Block b) {
                updateMenu(menu, b);
            }

            @Override
            public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                return p.hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager().hasPermission(p, b.getLocation(),
                        Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return getInputSlots();
                } else {
                    return getOutputSlots();
                }
            }
        };
    }

    private int[] getInputSlots() {
        return REWARD_SLOTS;
    }
    private int[] getOutputSlots() {
        return new int[]{OUTPUT_SLOT};
    }

    protected void constructMenu(BlockMenuPreset preset) {
        for (int i : pinkBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.PINK_STAINED_GLASS_PANE)), " "),
                    (p, slot, item, action) -> false);
        }

        for (int i : blueBorder) {
            // 改为淡蓝色玻璃板
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE)), " "),
                    (p, slot, item, action) -> false);
        }

        // 模板槽位点击处理
        preset.addMenuClickHandler(TEMPLATE_SLOT, (player, slot, item, action) -> {
            handleTemplateSlotClick(player, slot, item, action);
            return false;
        });

        // 设置按钮点击处理
        preset.addMenuClickHandler(SETTINGS_SLOT, (player, slot, item, action) -> {
            handleSettingsSlotClick(player, slot, item, action);
            return false;
        });

        // 输出槽位 - 允许玩家取出物品
        preset.addMenuClickHandler(OUTPUT_SLOT, (player, slot, item, action) -> {
            // 允许玩家取出输出槽位的物品
            return true;
        });
    }

    private void handleTemplateSlotClick(Player player, int slot, ItemStack item, ClickAction action) {
//        Block targetBlock = player.getTargetBlockExact(5);
//        if (targetBlock == null) return;
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (!(inv.getHolder() instanceof BlockMenu menu)){
            return;
        }
//        Block targetBlock = inv.getLocation().getBlock();
//        BlockMenu menu = StorageCacheUtils.getMenu(targetBlock.getLocation());
        Block targetBlock = menu.getLocation().getBlock();
        if (menu == null) return;

        int storedAmount = getStoredTemplateAmount(targetBlock.getLocation());
        boolean isTemplateSet = getStoredTemplateItemData(targetBlock) != null;

        // Shift+左键 - 切换自动输出
        if (action.isShiftClicked() && !action.isRightClicked()) {
            Location loc = menu.getLocation();
            boolean currentState = autoOutputMap.getOrDefault(loc, false);
            autoOutputMap.put(loc, !currentState);

            if (!currentState) {
                player.sendMessage(ChatColor.GREEN + "已开启自动输出模式！");
            } else {
                player.sendMessage(ChatColor.YELLOW + "已关闭自动输出模式！");
            }
            updateMenu(menu, targetBlock);
        }
        // 普通左键点击 - 设置模板（只有当存储数量为0时才能重新设置）
        else if (!action.isRightClicked() && !action.isShiftClicked()) {
            ItemStack cursorItem = player.getItemOnCursor();
            if (!cursorItem.getType().isAir() && (storedAmount == 0 || !isTemplateSet)) {
                // 存储原始物品数据
                storeTemplateItemData(targetBlock, cursorItem);
                // 初始化存储数量为0
                setStoredTemplateAmount(targetBlock, 0);
                player.sendMessage(ChatColor.GREEN + "已设置抽奖模板物品！");
                updateMenu(menu, targetBlock);
                updateHologram(targetBlock); // 更新悬浮物
            } else if (storedAmount > 0) {
                player.sendMessage(ChatColor.RED + "请先取出所有模板物品后才能重新设置模板！");
            }
        }
        // 右键点击 - 提取物品
        else if (action.isRightClicked()) {
            ItemStack originalItem = getStoredTemplateItemData(targetBlock);
            if (originalItem != null && storedAmount > 0) {
                // Shift+右键 - 尽可能填满背包
                if (action.isShiftClicked()) {
                    fillPlayerInventory(player, originalItem, targetBlock, menu);
                }
                // 普通右键 - 提取一组物品
                else {
                    extractOneStack(player, originalItem, targetBlock, menu);
                }
                updateMenu(menu, targetBlock);
                updateHologram(targetBlock); // 更新悬浮物
            } else if (originalItem != null && storedAmount == 0) {
                player.sendMessage(ChatColor.RED + "模板中没有物品可提取！");
            }
        }
    }

    // 提取一组物品（最多64个）
    private void extractOneStack(Player player, ItemStack templateItem, Block block, BlockMenu menu) {
        int storedAmount = getStoredTemplateAmount(block.getLocation());
        int maxStackSize = templateItem.getMaxStackSize();
        int canTake = Math.min(maxStackSize, storedAmount);

        // 计算玩家背包能容纳的数量
        int playerCanTake = getMaxCanTake(player, templateItem);
        if (playerCanTake <= 0) {
            player.sendMessage(ChatColor.RED + "背包已满，无法取出物品！");
            return;
        }

        int actualTake = Math.min(canTake, playerCanTake);
        ItemStack toGive = templateItem.clone();
        toGive.setAmount(actualTake);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(toGive);

        if (leftover.isEmpty()) {
            // 成功取出
            setStoredTemplateAmount(block, storedAmount - actualTake);
            player.sendMessage(ChatColor.GREEN + "已取出 " + actualTake + " 个模板物品！");
        } else {
            // 部分取出
            int actuallyTaken = actualTake - leftover.get(0).getAmount();
            if (actuallyTaken > 0) {
                setStoredTemplateAmount(block, storedAmount - actuallyTaken);
                player.sendMessage(ChatColor.YELLOW + "已取出 " + actuallyTaken + " 个模板物品（背包已满）！");
            } else {
                player.sendMessage(ChatColor.RED + "背包已满，无法取出物品！");
            }
        }
    }

    // 尽可能填满玩家背包
    private void fillPlayerInventory(Player player, ItemStack templateItem, Block block, BlockMenu menu) {
        int storedAmount = getStoredTemplateAmount(block.getLocation());
        if (storedAmount <= 0) {
            player.sendMessage(ChatColor.RED + "模板中没有物品可提取！");
            return;
        }

        int totalTaken = 0;
        int playerCanTake = getMaxCanTake(player, templateItem);

        while (playerCanTake > 0 && storedAmount > 0) {
            int thisTake = Math.min(playerCanTake, storedAmount);
            ItemStack toGive = templateItem.clone();
            toGive.setAmount(thisTake);

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(toGive);

            if (leftover.isEmpty()) {
                totalTaken += thisTake;
                storedAmount -= thisTake;
                playerCanTake = getMaxCanTake(player, templateItem);
            } else {
                int actuallyTaken = thisTake - leftover.get(0).getAmount();
                if (actuallyTaken > 0) {
                    totalTaken += actuallyTaken;
                    storedAmount -= actuallyTaken;
                }
                break;
            }
        }

        if (totalTaken > 0) {
            setStoredTemplateAmount(block, storedAmount);
            player.sendMessage(ChatColor.GREEN + "已取出 " + totalTaken + " 个模板物品！");
        } else {
            player.sendMessage(ChatColor.RED + "背包已满，无法取出物品！");
        }
    }

    // 输出模板物品到输出槽位
    private void outputToSlot(BlockMenu menu, Block block, ItemStack templateItem) {
        ItemStack outputItem = menu.getItemInSlot(OUTPUT_SLOT);
        int storedAmount = getStoredTemplateAmount(block.getLocation());

        if (storedAmount <= 0) {
            return;
        }

        // 如果输出槽位为空，直接放入
        if (outputItem == null || outputItem.getType() == Material.AIR) {
            int toOutputAmount = Math.min(storedAmount, templateItem.getMaxStackSize());
            ItemStack toOutput = templateItem.clone();
            toOutput.setAmount(toOutputAmount);
            menu.replaceExistingItem(OUTPUT_SLOT, toOutput);

            // 减少存储的模板数量
            setStoredTemplateAmount(block, storedAmount - toOutputAmount);
            updateHologram(block); // 更新悬浮物
        }
        // 如果输出槽位已经有相同物品，尝试叠加
        else if (outputItem.isSimilar(templateItem)) {
            int maxStackSize = outputItem.getMaxStackSize();
            int currentAmount = outputItem.getAmount();
            int canAdd = maxStackSize - currentAmount;

            if (canAdd > 0) {
                int toAdd = Math.min(canAdd, storedAmount);
                outputItem.setAmount(currentAmount + toAdd);
                menu.replaceExistingItem(OUTPUT_SLOT, outputItem);

                // 减少存储的模板数量
                setStoredTemplateAmount(block, storedAmount - toAdd);
                updateHologram(block); // 更新悬浮物
            }
            // 如果输出槽位已满，不做任何操作
        }
        // 如果输出槽位有不同物品，不做任何操作
    }

    // 计算玩家背包能容纳指定物品的最大数量
    private int getMaxCanTake(Player player, ItemStack item) {
        int totalSpace = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                totalSpace += item.getMaxStackSize();
            } else if (stack.isSimilar(item)) {
                totalSpace += stack.getMaxStackSize() - stack.getAmount();
            }
        }
        return totalSpace;
    }

    private void handleSettingsSlotClick(Player player, int slot, ItemStack item, ClickAction action) {
//        Block targetBlock = player.getTargetBlockExact(5);
//        if (targetBlock == null) return;
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (!(inv.getHolder() instanceof BlockMenu menu)){
            return;
        }
//        Block targetBlock = inv.getLocation().getBlock();

//        BlockMenu menu = StorageCacheUtils.getMenu(targetBlock.getLocation());
        if (menu == null) return;

        // 左键点击 - 设置数量
        if (!action.isRightClicked()) {
            player.closeInventory(); // 关闭当前容器
            player.sendMessage(ChatColor.YELLOW + "请在聊天框中输入每次抽奖需要的物品数量（≥1的整数）：");

            ChatInput.waitForPlayer(Slimefun.instance(), player, message -> {
                try {
                    int amount = Integer.parseInt(message);
                    if (amount < 1) {
                        player.sendMessage(ChatColor.RED + "数量必须大于等于1！已设置为默认值1。");
                        amount = 1; // 强制设置为1
                    }

                    BlockStorage.addBlockInfo(menu.getBlock(), "requiredAmount", String.valueOf(amount));
                    updateMenu(menu, menu.getBlock());
                    player.sendMessage(ChatColor.GREEN + "已设置每次抽奖需要物品数量为：" + amount);

                    // 重新打开菜单
                    Bukkit.getScheduler().runTask(Slimefun.instance(), () -> {
                        menu.open(player);
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "请输入有效的整数！已设置为默认值1。");
                    BlockStorage.addBlockInfo(menu.getBlock(), "requiredAmount", "1");
                    updateMenu(menu, menu.getBlock());

                    // 重新打开菜单
                    Bukkit.getScheduler().runTask(Slimefun.instance(), () -> {
                        menu.open(player);
                    });
                }
            });
        }
        // 右键点击 - 恢复默认
        else if (action.isRightClicked()) {
            BlockStorage.addBlockInfo(menu.getBlock(), "requiredAmount", "1");
            updateMenu(menu, menu.getBlock());
            player.sendMessage(ChatColor.GREEN + "已恢复默认数量：1");
        }
    }

    private void updateMenu(BlockMenu menu, Block block) {
        String requiredAmount = BlockStorage.getLocationInfo(block.getLocation(), "requiredAmount");
        if (requiredAmount == null) {
            requiredAmount = "1";
        }

        // 更新设置按钮显示
        menu.replaceExistingItem(SETTINGS_SLOT, new CustomItemStack(
                Material.COMPARATOR,
                "§6抽奖设置",
                "§e左键点击: §7设置每次抽奖需要的物品数量",
                "§e右键点击: §7恢复默认数量 (1)",
                "",
                "§a当前设置: §e" + requiredAmount + " 个物品/次"
        ));

        // 更新模板槽位显示
        ItemStack originalTemplateItem = getStoredTemplateItemData(block);
        boolean isAutoOutput = autoOutputMap.getOrDefault(menu.getLocation(), false);
        int storedAmount = getStoredTemplateAmount(block.getLocation());

        if (originalTemplateItem == null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7放入需要检测的物品作为模板");
            lore.add("");
            lore.add("§e左键点击: §7设置模板物品");
            lore.add("§eShift+左键: §7" + (isAutoOutput ? "关闭" : "开启") + "自动输出");
            lore.add("§e右键点击: §7提取一组物品");
            lore.add("§eShift+右键: §7填满背包");
            lore.add("");
            lore.add("§c自动输出: " + (isAutoOutput ? "§a开启" : "§7关闭"));

            menu.replaceExistingItem(TEMPLATE_SLOT, new CustomItemStack(
                    Material.BARRIER,
                    "§c抽奖模板物品",
                    lore.toArray(new String[0])
            ));
        } else {
            // 创建显示物品（带有操作提示）
            ItemStack displayItem = createTemplateDisplayItem(originalTemplateItem, storedAmount, isAutoOutput, menu.getBlock());
            menu.replaceExistingItem(TEMPLATE_SLOT, displayItem);
        }

        // 更新信息显示
        updateInfoDisplay(menu, block);
    }

    private void updateInfoDisplay(BlockMenu menu, Block block) {
        ItemStack originalTemplateItem = getStoredTemplateItemData(block);
        String requiredAmount = BlockStorage.getLocationInfo(block.getLocation(), "requiredAmount");
        if (requiredAmount == null) {
            requiredAmount = "1";
        }

        int storedAmount = getStoredTemplateAmount(block.getLocation());
        boolean isAutoOutput = autoOutputMap.getOrDefault(menu.getLocation(), false);

        // 第一行：模板物品名称和需要的数量
        String templateInfo;
        if (originalTemplateItem != null) {
            String itemName = getItemDisplayName(originalTemplateItem);
            templateInfo = "§a模板: §e" + itemName + " §a需要: §e" + requiredAmount + "个";
        } else {
            templateInfo = "§c未设置模板物品";
        }

        // 第二行：当前奖品剩余数量
        int totalRewards = 0;
        for (int slot : REWARD_SLOTS) {
            ItemStack reward = menu.getItemInSlot(slot);
            if (reward != null && reward.getType() != Material.AIR) {
                totalRewards += reward.getAmount();
            }
        }
        String rewardInfo = "§b剩余奖品: §e" + totalRewards + "个";

        // 自动输出状态
        String autoOutputInfo = "§6自动输出: " + (isAutoOutput ? "§a开启" : "§7关闭");

        // 更新信息显示
        List<String> lore = new ArrayList<>();
        lore.add("§6§l抽奖机信息");
        lore.add("");

        if (originalTemplateItem != null) {
            String itemName = getItemDisplayName(originalTemplateItem);
            lore.add("§a模板物品: §e" + itemName);
            lore.add("§a存储数量: §e" + storedAmount);
        } else {
            lore.add("§c未设置模板物品");
        }

        lore.add("§a每次抽奖需要: §e" + requiredAmount + " 个物品");
        lore.add("");
        lore.add(templateInfo);
        lore.add(rewardInfo);
        lore.add(autoOutputInfo);
        lore.add("");
        lore.add("§7检测范围: §b5格半径");
        lore.add("§7奖励槽位: §e" + REWARD_SLOTS.length + " 个");

        menu.replaceExistingItem(INFO_SLOT, new CustomItemStack(
                Material.GHAST_TEAR,
                "§a※※※ 抽奖机信息 ※※※",
                lore.toArray(new String[0])
        ));
    }

    private void processDraw(Block block, BlockMenu menu) {
        // 获取原始模板物品数据
        ItemStack originalTemplateItem = getStoredTemplateItemData(block);
        if (originalTemplateItem == null) {
            return;
        }

        // 获取需要的数量
        String requiredAmountStr = BlockStorage.getLocationInfo(block.getLocation(), "requiredAmount");
        int requiredAmount = 1;
        try {
            requiredAmount = Integer.parseInt(requiredAmountStr);
            if (requiredAmount < 1) requiredAmount = 1;
        } catch (NumberFormatException e) {
            requiredAmount = 1;
        }

        // 计算奖品剩余数量
        int totalRewards = 0;
        for (int slot : REWARD_SLOTS) {
            ItemStack reward = menu.getItemInSlot(slot);
            if (reward != null && reward.getType() != Material.AIR) {
                totalRewards += reward.getAmount();
            }
        }

        // 如果没有奖品，直接返回
        if (totalRewards <= 0) {
            return;
        }

        // 检测周围5格内的物品 - 使用同步方式
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        int totalFound = 0;
        List<Item> matchedItems = new ArrayList<>();

        // 由于现在tick是同步的，可以直接调用getNearbyEntities
        for (Entity entity : block.getWorld().getNearbyEntities(center, 5, 5, 5)) {
            if (!(entity instanceof Item)) continue;

            Item item = (Item) entity;
            if (item.isDead()) continue;

            // 跳过悬浮物物品实体
            if (item.hasMetadata("draw-machine-hologram")) {
                continue;
            }

            ItemStack itemStack = item.getItemStack();
            // 使用原始物品数据进行比较，忽略数量
            if (isSimilarSafe(itemStack, originalTemplateItem)) {
                totalFound += itemStack.getAmount();
                matchedItems.add(item);
            }
        }

        // 检查数量是否足够
        if (totalFound >= requiredAmount) {
            // 计算最大可抽奖次数（考虑奖品数量和货币数量）
            int maxPossibleByCurrency = totalFound / requiredAmount; // 基于货币的最大次数
            int maxPossibleByRewards = totalRewards; // 基于奖品的最大次数
            int multiplier = Math.min(maxPossibleByCurrency, maxPossibleByRewards);

            // 实际需要收集的物品数量
            int itemsToCollect = multiplier * requiredAmount;

            // 检查存储数量是否会超过上限
            int currentStoredAmount = getStoredTemplateAmount(block.getLocation());
            if (currentStoredAmount > Integer.MAX_VALUE - itemsToCollect) {
                // 如果会超过上限，取消这次抽奖
                return;
            }

            // 从地面收集物品
            int collected = collectItems(matchedItems, itemsToCollect);

            if (collected > 0) {
                // 再次检查存储数量上限（防止并发问题）
                if (currentStoredAmount > Integer.MAX_VALUE - collected) {
                    return;
                }

                // 更新存储的模板数量
                int newStoredAmount = currentStoredAmount + collected;
                setStoredTemplateAmount(block, newStoredAmount);
                updateMenu(menu, block); // 更新菜单显示新数量
                updateHologram(block); // 更新悬浮物

                // 触发奖励喷射
                for (int i = 0; i < multiplier; i++) {
                    triggerReward(block, menu);
                }
            }
        }
    }

    public static void cleanupAllHolograms() {
        for (BukkitTask task : HOLOGRAM_ANIMATIONS.values()) {
            task.cancel();
        }
        HOLOGRAM_ANIMATIONS.clear();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata("draw-machine-hologram")) {
                    entity.remove();
                }
            }
        }
    }

    private int collectItems(List<Item> matchedItems, int itemsToCollect) {
        int collected = 0;

        for (Item item : matchedItems) {
            if (collected >= itemsToCollect) break;
            if (item.isDead()) continue;

            ItemStack itemStack = item.getItemStack();
            int amount = itemStack.getAmount();
            int canCollect = Math.min(amount, itemsToCollect - collected);

            if (canCollect >= amount) {
                // 收集整个物品堆
                collected += amount;
                item.remove();
            } else {
                // 收集部分物品
                collected += canCollect;
                itemStack.setAmount(amount - canCollect);
                item.setItemStack(itemStack);
            }
        }

        return collected;
    }

    private void triggerReward(Block block, BlockMenu menu) {
        // 获取所有非空的奖励槽位
        List<Integer> availableSlots = new ArrayList<>();
        for (int slot : REWARD_SLOTS) {
            ItemStack reward = menu.getItemInSlot(slot);
            if (reward != null && reward.getType() != Material.AIR) {
                availableSlots.add(slot);
            }
        }

        if (availableSlots.isEmpty()) {
            return;
        }

        // 随机选择一个奖励槽位
        Random random = new Random();
        int selectedSlot = availableSlots.get(random.nextInt(availableSlots.size()));
        ItemStack rewardItem = menu.getItemInSlot(selectedSlot).clone();

        // 设置为1个，避免喷出整组
        rewardItem.setAmount(1);

        // 从菜单中移除该物品
        ItemStack current = menu.getItemInSlot(selectedSlot);
        if (current.getAmount() > 1) {
            current.setAmount(current.getAmount() - 1);
            menu.replaceExistingItem(selectedSlot, current);
        } else {
            menu.replaceExistingItem(selectedSlot, new ItemStack(Material.AIR));
        }

        // 喷出奖励物品
        Location dropLocation = block.getLocation().add(0.5, 1, 0.5);
        Item droppedItem = block.getWorld().dropItem(dropLocation, rewardItem);

        // 给物品一个随机的速度，使其看起来像是喷出来的
        double x = (random.nextDouble() - 0.5) * 0.5;
        double y = random.nextDouble() * 0.8 + 0.3;
        double z = (random.nextDouble() - 0.5) * 0.5;
        droppedItem.setVelocity(new Vector(x, y, z));

        // 播放抽奖音效
        playDrawSound(block.getLocation());
    }

    // 播放抽奖音效的方法
    private void playDrawSound(Location location) {
        // 使用随机音效增加多样性
        Random random = new Random();
        int soundChoice = random.nextInt(3);

        switch(soundChoice) {
            case 0:
                // 成功音效1 - 升级音效
                location.getWorld().playSound(location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                break;
            case 1:
                // 成功音效2 - 经验音效
                location.getWorld().playSound(location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
                break;
            case 2:
                // 成功音效3 - 铃声音效
                location.getWorld().playSound(location, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                break;
        }

        // 额外添加一个小的烟花爆炸音效
        location.getWorld().playSound(location, org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1.5f);
    }

    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.NONE;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    // 以下是您提供的物品序列化方法
    /**
     * ✅ 将 ItemStack 完整序列化为 Base64 字符串
     * 支持：PDC、附魔、lore、自定义模型、Slimefun物品、NBT等所有数据
     */
    private String itemToBase64(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
             org.bukkit.util.io.BukkitObjectOutputStream dataOutput = new org.bukkit.util.io.BukkitObjectOutputStream(outputStream)) {

            // 先确保数量正确写入
            dataOutput.writeObject(item.clone()); // 使用克隆避免外部修改
            dataOutput.flush();

            byte[] serializedBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(serializedBytes);

        } catch (java.io.IOException e) {
            System.err.println("物品序列化失败: " + item.getType());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ 将 Base64 字符串反序列化为 ItemStack
     * 完全还原原始物品（包括 PDC、附魔、数量、NBT 等）
     */
    private ItemStack itemFromBase64(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try (java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(Base64.getDecoder().decode(data));
             org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(inputStream)) {

            ItemStack item = (ItemStack) dataInput.readObject();
            // 重要：强制刷新 ItemMeta 引用，防止 PDC 缓存问题
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                item.setItemMeta(meta); // 重新设置，确保一致性
            }
            return item;

        } catch (java.io.IOException | ClassNotFoundException e) {
            System.err.println("物品反序列化失败");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ 安全判断两个 ItemStack 是否"相同"（可用于堆叠判断）
     * 使用 Bukkit 内建的 isSimilar 方法，并兼容 Slimefun 物品
     */
    private boolean isSimilarSafe(ItemStack item1, ItemStack item2) {
        if (item1 == item2) return true;
        if (item1 == null || item2 == null) return false;

        // 先检查是否为空气
        if (item1.getType() == Material.AIR || item2.getType() == Material.AIR) {
            return item1.getType() == item2.getType();
        }

        return item1.isSimilar(item2);
    }
}
