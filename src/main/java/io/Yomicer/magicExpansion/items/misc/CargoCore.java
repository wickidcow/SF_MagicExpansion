package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.tools.VoidTouch;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.SameItemJudge;
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
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.CARGO_FRAGMENT;
import static io.Yomicer.magicExpansion.utils.SameItemJudge.*;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class CargoCore extends SlimefunItem implements EnergyNetComponent{


//    private final int[] pinkBorder = {51,52};
    private final int[] blueBorder = {46,47};
    private final int[] inputSlots = {0,1,2,3,4 ,9,10,11,12,13 ,18,19,20,21,22};
    private final int[] outputSlots = {6,7,8,  15,16,17,  24,25,26};
    private final int[] inputOutputLine = {5,14,23};
    private final int[] arrowSlot = {45,48,50,51,52,53};
    private final int[] storageSlots = {28, 29, 30, 31, 32, 33, 34, 35}; // 显示存储物品
    private final int[] transportSlots = {36, 37, 38, 39}; // 快速合成槽(暂未实现)用于输出
    private final int[] transportSlots2 = { 41, 42, 43, 44}; // 快速合成槽(暂未实现)用于输入


    // 分页设置
    private static final int ITEMS_PER_PAGE = 8;
    private static final int MAX_STORED_ITEMS = 1145; // 最多支持 18 种不同物品
    private static final String OUTPUT_TARGET_KEY = "output_target_slot";

    public CargoCore(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        constructMenu("Magic Storage");
        addItemHandler(onBlockPlace(), onBlockBreak());
    }



    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                CargoCore.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    @Nonnull
    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {



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
                    inv.dropItems(b.getLocation(), getInputSlots());
                    inv.dropItems(b.getLocation(), getOutputSlots());
                }


                Location loc = b.getLocation();
                SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
                if (data == null) return;

                // 获取破坏方块的玩家
                Player player = getNearestPlayer(loc);

                // === 开始处理每个存储槽 ===
                for (int i = 0; i < MAX_STORED_ITEMS; i++) {
                    String jsonData = data.getData("item_type_" + i);
                    String countStr = data.getData("item_count_" + i);

                    if (jsonData == null || countStr == null) continue;

                    ItemStack prototype = itemFromBase64(jsonData);
                    if (prototype == null) continue;

                    long amount;
                    try {
                        amount = Long.parseLong(countStr);
                    } catch (Exception e) {
                        continue;
                    }

                    if (amount <= 0) continue;



                    // === 计算需要掉落多少碎片和每个碎片的数量 ===
                    long remainingAmount = amount;
                    int fragmentCount = 0;
                    long lostAmount = 0;

                    // 最多掉落128个碎片
                    while (remainingAmount > 0 && fragmentCount < 2048) {
                        // 每个碎片最多包含 Integer.MAX_VALUE 个物品
                        int amountInFragment = (int) Math.min(remainingAmount, Integer.MAX_VALUE);

                        ItemStack fragment = createCargoFragment(prototype, amountInFragment);
                        if (fragment != null) {
                            b.getWorld().dropItemNaturally(loc, fragment);
                            fragmentCount++;
                        }

                        remainingAmount -= amountInFragment;
                    }

                    // 如果还有剩余物品无法掉落(因为碎片数量限制)
                    if (remainingAmount > 0) {
                        lostAmount = remainingAmount;
                    }

                    // === 如果丢失了物品,通知玩家 ===
                    if (lostAmount > 0 && player != null) {
                        String itemName = ItemStackHelper.getDisplayName( prototype);
                        player.sendMessage(ChatColor.RED + "Warning: The fragment limit caused the loss of " +
                                lostAmount + " of " + itemName + ".");
                        player.sendMessage("A single item type can drop at most 2,048 Aether Cargo Chests, each holding 2.14 billion items.");
                        player.sendMessage("The maximum dropped amount for one item type is 4,398,046,509,056.");
                    }







//                    // === 创建 CargoFragment ===
//                    ItemStack fragment = createCargoFragment(prototype, (int) Math.min(amount, Integer.MAX_VALUE));
//                    if (fragment != null) {
//                        b.getWorld().dropItemNaturally(loc, fragment);
//                    }
                }

            }
        };
    }

    // 辅助方法:获取最近的玩家(用于发送消息)
    private Player getNearestPlayer(Location location) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if (distance < nearestDistance && distance < 100) { // 10格范围内的玩家
                nearestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    /**
     * 创建一个 CargoFragment 物品,代表某个物品的"存储碎片"
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

    public int[] getInputSlots() {
        return inputSlots;
    }

    public int[] getOutputSlots() {
        return outputSlots;
    }


    private void cleanupOutputState(@Nonnull SlimefunBlockData data, int dataSlot) {
        data.removeData("item_type_" + dataSlot);
        data.removeData("item_count_" + dataSlot);
        data.setData("output_target_slot", "-1");
    }

    /**
     * 处理自动输出逻辑
     * 从内部库存中取出物品,尝试放入前 9 个槽位(0-8)
     */
    private void handleOutput(@Nonnull BlockMenu menu, @Nonnull SlimefunBlockData data) {
        // 获取输出目标 data slot
        String targetStr = data.getData("output_target_slot");
        if (targetStr == null || "-1".equals(targetStr)) {
            return;
        }

        int targetSlot;
        try {
            targetSlot = Integer.parseInt(targetStr);
        } catch (NumberFormatException e) {
            data.setData("output_target_slot", "-1");
            return;
        }

        // 获取物品原型
        String json = data.getData("item_type_" + targetSlot);
        if (json == null || json.isEmpty()) {
            return;
        }

        ItemStack prototype = itemFromBase64(json);
        if (prototype == null || prototype.getType().isAir()) {
            return;
        }

        // 获取库存数量
        long itemCount = 0;
        try {
            String countStr = data.getData("item_count_" + targetSlot);
            if (countStr != null && !countStr.isEmpty()) {
                itemCount = Long.parseLong(countStr);
            }
        } catch (NumberFormatException ignored) {}

        if (itemCount <= 0) {
            return;
        }

        // 执行输出(最多 9 个槽位,索引 0~8)
        int[] outputSlots = getOutputSlots();
        int batchSize = 64*9; // 例如 64, 512...

        int successfullyOutput = tryOutputItems(menu, prototype, itemCount, outputSlots, batchSize);

        if (successfullyOutput > 0) {
            // 更新库存
            itemCount -= successfullyOutput;
            if (itemCount <= 0) {
                cleanupOutputState(data, targetSlot);
            } else {
                data.setData("item_count_" + targetSlot, String.valueOf(itemCount));
            }

            // 更新 UI
            updateStorageDisplay(menu, data);

            // 音效
            Block block = menu.getLocation().getBlock();
//            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2F, 1.2F);
        }
    }

    /**
     * 尝试将物品输出到指定槽位(安全分段版本)
     * 每次最多推送 64 个,避免 pushItem 处理大数量时出错
     *
     * @param menu BlockMenu
     * @param prototype 物品原型(不带数量)
     * @param availableCount 库存中可用数量
     * @param outputSlots 可用的槽位数组(如 0~8)
     * @param maxPerTick 每 tick 最大输出数量(速率限制)
     * @return 实际成功输出的数量
     */
    private int tryOutputItems(@Nonnull BlockMenu menu,
                               @Nonnull ItemStack prototype,
                               long availableCount,
                               int[] outputSlots,
                               int maxPerTick) {
        if (availableCount <= 0 || maxPerTick <= 0) {
            return 0;
        }

        // 限制单次最大处理量(防性能问题)
        int totalToOutput = (int) Math.min(availableCount, maxPerTick);
        if (totalToOutput <= 0) return 0;

        int successful = 0;
        int processed = 0;
        int maxBatch = 64; // 每批最多 64 个

        // 分批推送
        while (processed < totalToOutput) {
            int currentBatch = Math.min(maxBatch, totalToOutput - processed);

            // 创建当前批次物品
            ItemStack batch = prototype.clone().asQuantity(currentBatch);

            // 推送
            ItemStack leftover;
            try {
                leftover = menu.pushItem(batch, outputSlots);
            } catch (Exception e) {
                break; // 出现异常,停止输出
            }

            // 计算本次成功数量
            int batchSuccess = currentBatch;
            if (leftover != null) {
                batchSuccess -= leftover.getAmount();
            }

            successful += batchSuccess;

            // 如果本次没完全放进去,说明满了,停止
            if (batchSuccess < currentBatch) {
                break;
            }

            processed += currentBatch;
        }

        return Math.max(0, successful);
    }

    protected void tick(Block block) {

        Location loc = block.getLocation();
        BlockMenu menu = StorageCacheUtils.getMenu(loc);
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);

        if (menu == null || data == null) return;
        cleanupInvalidSlots(data);
        handleOutput(menu, data);
        handleAllTemplateTransfers(block);
        handleAllInputTransfers(block, data);    // 输入传输(新增)

        // 处理输入槽物品
        for (int slot : inputSlots) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey keyItem = new NamespacedKey(MagicExpansion.getInstance(), "cargo_item_json");
            NamespacedKey keyAmount = new NamespacedKey(MagicExpansion.getInstance(), "cargo_amount");

            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            if(sfItem instanceof CargoFragment) {
                // 判断是否为 CargoFragment:检查是否有 PDC 数据
                if (container.has(keyItem, PersistentDataType.STRING)) {
                    String json = container.get(keyItem, PersistentDataType.STRING);
                    Integer amount = container.get(keyAmount, PersistentDataType.INTEGER);

                    if (json != null && amount != null && amount > 0) {
                        // 反序列化原物品
                        ItemStack originalItem = itemFromBase64(json);
                        if (originalItem != null) {
                            // 存入 CargoCore
                            storeItem(data, originalItem.clone().asQuantity(amount));
                            // 消费这个 CargoFragment
                            menu.consumeItem(slot, 1);
//                            continue; // 处理完 fragment 就跳过后续逻辑
                        }
                    }
                    // 如果解析失败,也当作普通物品处理      //取消非法物品当做普通物品存入
//                    storeItem(data, item);
//                    menu.consumeItem(slot, item.getAmount());
//                    continue;
                }
                // 如果没有PDC数据,消费整个物品但不存储      //取消删除非法物品
//                menu.consumeItem(slot, item.getAmount());
            }else{
                // 如果不是 CargoFragment,按普通物品存储   修改为直接消耗,不存入
                storeItem(data, item);
                menu.consumeItem(slot, item.getAmount());
            }
        }

        if(menu != null && menu.hasViewer()) {
            // 更新存储显示
//            updateArrowButtons(menu, data);

            updateStorageDisplay(menu, data);
            updatePageButtons(menu, data);
            updateInputBindDisplay(menu, block); // 更新输入绑定显示(新增)
            updateTranslateOutPut(menu, block);
        }

    }

    // 在 tick() 中调用 storeItem 时使用:
    private void storeItem(SlimefunBlockData data, ItemStack item) {
        cleanupInvalidSlots(data);
        int slot = findMatchingSlot(data, item);
        if (slot != -1) {
            // 匹配到已有槽位
            long count = 0;

            try {
                count = Long.parseLong(data.getData("item_count_" + slot));
            } catch (Exception ignored) {}

            int amount = item.getAmount();
            if (amount <= 0) return;

            try {
                count = Math.addExact(count, amount);
                data.setData("item_count_" + slot, String.valueOf(count));
            } catch (ArithmeticException e) {
                // 溢出,丢弃
                Location loc = data.getLocation();
                if (loc != null) {
                    loc.getWorld().dropItem(loc, item);
                }
            }

        } else {
            // 找一个空槽位
            slot = findEmptySlot(data);
            if (slot == -1) {
                Location loc = data.getLocation();
                if (loc != null) {
                    loc.getWorld().dropItem(loc, item);
                }
                return;
            }

            // ✅ 使用 JSON 替代 Base64
            String json = itemToBase64(item.clone());
            if (json == null) return;

            data.setData("item_type_" + slot, json);
            data.setData("item_count_" + slot, String.valueOf(item.getAmount()));
        }
    }
    // 查找是否已有相同物品
    private int findMatchingSlot(SlimefunBlockData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return -1;
        }

        ItemStack prototype = item.clone();
        prototype.setAmount(1);

        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String base64Data = data.getData("item_type_" + i);
            if (base64Data == null || base64Data.isEmpty()) continue;

            ItemStack stored;
            try {
                stored = itemFromBase64(base64Data);
            } catch (Exception e) {
                continue; // 或记录日志
            }

            if (stored == null) continue;
            stored.setAmount(1); // 确保比较时不被数量干扰

            if (SameItemJudge.isSimilarSafe(prototype, stored)) {
//            if (SlimefunUtils.isItemSimilar(prototype, stored, true)) {
                return i;
            }
        }
        return -1;
    }
    /**
     * 清理无效槽位:count <= 0 或解析失败 或 数据不匹配
     */
    public void cleanupInvalidSlots(SlimefunBlockData data) {
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String typeKey = "item_type_" + i;
            String countKey = "item_count_" + i;

            String typeStr = data.getData(typeKey);
            String countStr = data.getData(countKey);

            boolean hasType = typeStr != null && !typeStr.trim().isEmpty();
            boolean hasCount = countStr != null && !countStr.trim().isEmpty();

            if (!hasType && !hasCount) continue;

            // 缺失一个 → 删除
            if (hasType && !hasCount) {
                data.removeData(typeKey);
                continue;
            }
            if (!hasType && hasCount) {
                data.removeData(countKey);
                continue;
            }

            // 都有 → 检查 count
            long count = 0;
            try {
                count = Long.parseLong(countStr.trim());
                if (count <= 0) {
                    data.removeData(typeKey);
                    data.removeData(countKey);
                }
            } catch (Exception e) {
                // 只有 count 错误才清理
                data.removeData(typeKey);
                data.removeData(countKey);
            }
        }
    }

    // 查找空槽
    private int findEmptySlot(SlimefunBlockData data) {
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String typeData = data.getData("item_type_" + i);
            String countData = data.getData("item_count_" + i);

            if (typeData == null || typeData.isEmpty()) return i;
            if (countData == null || countData.isEmpty()) return i;

            try {
                long count = Long.parseLong(countData);
                if (count <= 0) return i; // 可被复用
            } catch (Exception e) {
                return i; // 解析失败 → 可复用
            }
        }
        return -1;
    }

    private List<Integer> getStoredItemSlots(SlimefunBlockData data) {
        List<Integer> slots = new ArrayList<>();

        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String countStr = data.getData("item_count_" + i);
            if (countStr != null && !countStr.isEmpty()) {
                try {
                    long count = Long.parseLong(countStr);
                    if (count > 0) {
                        slots.add(i);
                    }
                } catch (Exception ignored) {}
            }
        }

        return slots;
    }

    private void updateStorageDisplay(BlockMenu menu, SlimefunBlockData data) {
        List<Integer> slots = getStoredItemSlots(data);
        int page = getCurrentPage(data);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, slots.size());

        // 获取当前输出目标
        int currentOutputSlot = -1;
        try {
            String outputStr = data.getData("output_target_slot");
            if (outputStr != null && !outputStr.isEmpty()) {
                currentOutputSlot = Integer.parseInt(outputStr);
            }
        } catch (Exception ignored) {}
        // 清空显示槽
        for (int slot : storageSlots) {
            menu.replaceExistingItem(slot, new CustomItemStack(Material.BARRIER, " "));
        }

        // 填充当前页
        for (int i = 0; i < endIndex - startIndex; i++) {
            int dataSlot = slots.get(startIndex + i);
            String jsonData = data.getData("item_type_" + dataSlot);
            ItemStack prototype = itemFromBase64(jsonData); // ✅ 用 JSON 还原
            if (prototype == null) continue;

            long count = 0;
            try {
                count = Long.parseLong(data.getData("item_count_" + dataSlot));
            } catch (Exception ignored) {}

            ItemStack display = prototype.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("§7Stored Amount: §a" + count);
                // ✅ 添加"正在输出"状态提示
                if (dataSlot == currentOutputSlot) {
                    lore.add(""); // 空行分隔
                    lore.add("§6§l▶ §eOutputting"); // 金色箭头 + 黄色文字
                }
                meta.setLore(lore);


                // 写入 PDC 用于反向查找 dataSlot
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(new NamespacedKey(MagicExpansion.getInstance(), "cargo_slot"), PersistentDataType.INTEGER, dataSlot);
                display.setItemMeta(meta);
            }
            display.setAmount(1);

//            menu.replaceExistingItem(storageSlots[i], display);
            // ✅ 一步搞定:设置物品 + 绑定点击逻辑
            int finalCurrentOutputSlot = currentOutputSlot;
            menu.addItem(storageSlots[i], display, (player, slotClicked, clickedItem, clickAction) -> {
                if (clickedItem == null) {
                    player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    return false;
                }
                if (menu == null || data == null) return false;
                ItemMeta itemMeta = clickedItem.getItemMeta();
                if (itemMeta == null) return false;
                NamespacedKey slotKey = new NamespacedKey(MagicExpansion.getInstance(), "cargo_slot");
                if (!itemMeta.getPersistentDataContainer().has(slotKey, PersistentDataType.INTEGER)) return false;
                int targetDataSlot = itemMeta.getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER);
                String json = data.getData("item_type_" + targetDataSlot);
                long itemCount = 0;
                try {
                    itemCount = Long.parseLong(data.getData("item_count_" + targetDataSlot));
                } catch (Exception ignored) {}

                ItemStack itemPrototype = itemFromBase64(json);
                if (itemPrototype == null || itemCount <= 0) return false;
                ItemStack itemPrototypeClone = itemPrototype.clone();
                itemPrototypeClone.setAmount(1);

                // === 处理不同点击方式 ===
                // === 区分点击类型 ===
                if (clickAction.isShiftClicked() && clickAction.isRightClicked()) {
                    // ✅ Shift + 右键:切换持续输出状态

                    if (finalCurrentOutputSlot == targetDataSlot) {
                        // 已在输出 → 停止
                        data.setData("output_target_slot", "-1");
                        player.sendMessage("§aStopped outputting: " + ItemStackHelper.getDisplayName(itemPrototypeClone));
                    } else {
                        // 开始输出新物品
                        data.setData("output_target_slot", String.valueOf(targetDataSlot));
                        player.sendMessage("§eStarted continuous output: " + ItemStackHelper.getDisplayName(itemPrototypeClone));
                    }

                    // 刷新界面(更新 Lore 状态)
                    updateStorageDisplay(menu, data);
                }
                else if (clickAction.isShiftClicked() && !clickAction.isRightClicked()) {
                    // Shift + 左键:存入背包中所有同类物品
                    int moved = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && !item.getType().isAir()) {
                            ItemStack itemClone = item.clone();
                            itemClone.setAmount(1);
                            if (SameItemJudge.isSimilarSafe(itemClone, itemPrototypeClone)) {
//                            if (SlimefunUtils.isItemSimilar(itemClone, itemPrototypeClone, true)) {
                                int amount = item.getAmount();
                                storeItem(data, item.clone());
                                player.getInventory().removeItem(item);
                                moved += amount;
                            }
                        }
                    }
                    if (moved > 0) {
                        player.sendMessage("§aStored §e" + moved + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + "§r §ain storage.");
                        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5F, 1.2F);
                        updateStorageDisplay(menu, data);
                    } else {
                        player.sendMessage("§7Your inventory contains no matching items to store.");
                        player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    }

                } else if (clickAction.isRightClicked() && !clickAction.isShiftClicked()) {
                    // 右键:尽可能填满背包
                    int filled = 0;
                    ItemStack toGive = itemPrototype.clone();
                    for (int invSlot = 0; invSlot < 36; invSlot++) {
                        ItemStack invItem = player.getInventory().getItem(invSlot);
                        if (invItem == null || invItem.getType().isAir()) {
                            int giveAmount = (int) Math.min(64, itemCount);
                            if (giveAmount <= 0) break;
                            toGive.setAmount(giveAmount);
                            player.getInventory().setItem(invSlot, toGive.clone());
                            itemCount -= giveAmount;
                            filled += giveAmount;
                        } else if (SameItemJudge.isSimilarSafe(invItem, itemPrototype) && invItem.getAmount() < 64) {
//                        } else if (SlimefunUtils.isItemSimilar(invItem, itemPrototype, true) && invItem.getAmount() < 64) {
                            int space = 64 - invItem.getAmount();
                            int giveAmount = (int) Math.min(space, itemCount);
                            if (giveAmount <= 0) break;
                            invItem.setAmount(invItem.getAmount() + giveAmount);
                            itemCount -= giveAmount;
                            filled += giveAmount;
                        }
                        if (itemCount <= 0) break;
                    }
                    if (filled > 0) {
                        data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount));
                        player.sendMessage("§aAdded §e" + filled + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + " §ato your inventory.");
                        player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_INSERT, 0.5F, 1.0F);
                        updateStorageDisplay(menu, data);
                    } else {
                        player.sendMessage("§7Inventory full, cannot withdraw any more.");
                        player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    }

                } else if (!clickAction.isRightClicked() && !clickAction.isShiftClicked()) {
                    // 左键:取出 64 个
                    int take = (int) Math.min(64, itemCount);
                    if (take <= 0) return false;
//                    ItemStack toTake = itemPrototype.clone().asQuantity(take);
//                    if (player.getInventory().addItem(toTake).isEmpty()) {
//                        itemCount -= take;
//                        data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount));
//                        player.sendMessage("§a已取出 §e" + take + " §a个 " + ItemStackHelper.getDisplayName(itemPrototypeClone));
//                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
//                        updateStorageDisplay(menu, data);
//                    } else {
//                        player.sendMessage("§c背包空间不足,无法取出物品.");
//                        player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
//                    }

                    ItemStack itemToGive = itemPrototypeClone.clone();
                    itemToGive.setAmount(take); // 尝试取出的数量
                    int maxCanHold = player.getInventory().getMaxStackSize();
                    int totalAvailableSpace = 0;
                    for (ItemStack stack : player.getInventory().getStorageContents()) { // 只看主背包+快捷栏(不含盔甲)
                        if (stack == null || stack.getType() == Material.AIR) {
                            totalAvailableSpace += maxCanHold;
                        } else if (stack.isSimilar(itemToGive)) {
                            totalAvailableSpace += maxCanHold - stack.getAmount();
                        }
                    }
                    int actualTake = Math.min(take, totalAvailableSpace);
                    if (actualTake <= 0) {
                        player.sendMessage("§cThere is not enough inventory space to withdraw the item.");
                        player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    } else {
                        itemToGive.setAmount(actualTake);
                        Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemToGive);
                        if (!leftover.isEmpty()) {
                            itemCount -= actualTake;
                            data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount));
                            player.sendMessage("§aWithdrew §e" + actualTake + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + "§a.");
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
                            updateStorageDisplay(menu, data);
                            player.sendMessage("§cSome items could not fit in your inventory and were dropped on the ground.");
                            for (ItemStack leftoverItem : leftover.values()) {
                                player.getWorld().dropItem(player.getLocation(), leftoverItem);
                            }
                        } else {
                            itemCount -= actualTake;
                            data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount));
                            player.sendMessage("§aWithdrew §e" + actualTake + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + "§a.");
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
                            updateStorageDisplay(menu, data);
                        }
                    }

                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                }

                return false;
            });


            // 假设 blueBorder 是 int[] 数组,包含要设置的槽位
            for (int slot : blueBorder) {
                menu.addItem(slot, new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, "§bView Stored Items"),(player, sloti, itemStack, clickAction) -> {
                    openStorageMenu(player, data,0); // 默认打开第一页
                    return false; // 不消耗物品或默认行为
                });
            }



        }

        updatePageButtons(menu, data);
    }

    private void openStorageMenu(Player player, SlimefunBlockData data, int currentPage) {
        ChestMenu menu = new ChestMenu("§6Storage Overview");
        menu.setSize(9 * 6);
        menu.setEmptySlotsClickable(true);
        // 初始化后直接刷新
        refreshStorageMenu(menu, data, currentPage);
        menu.open(player);
    }
    /**
     * 刷新分页存储菜单(替代旧的 updateStorageDisplay)
     */
    private void refreshStorageMenu(ChestMenu menu, SlimefunBlockData data, int currentPage) {
        if (data == null) return;

        // 重新收集有效槽位
        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String json = data.getData("item_type_" + i);
            String countStr = data.getData("item_count_" + i);
            long count = 0;
            try { count = Long.parseLong(countStr); } catch (Exception ignored) {}
            if (json != null && !json.isEmpty() && count > 0) {
                validSlots.add(i);
            }
        }

        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) validSlots.size() / itemsPerPage));
        if (currentPage >= totalPages) currentPage = Math.max(0, totalPages - 1);

        // === 重新填充 0-44 ===
        int start = currentPage * itemsPerPage;
        for (int i = 0; i < 45; i++) {
            int index = start + i;
            if (index >= validSlots.size()) {
                menu.addItem(i, new CustomItemStack(Material.BARRIER, " "),(p, slot, item, action)-> false);
                continue;
            }

            int targetDataSlot = validSlots.get(index);
            final int finalTargetSlot = targetDataSlot; // ← 明确声明 final
            String json = data.getData("item_type_" + finalTargetSlot);
            long itemCount = 0;
            try { itemCount = Long.parseLong(data.getData("item_count_" + finalTargetSlot)); } catch (Exception ignored) {}

            ItemStack itemPrototype = itemFromBase64(json);
            if (itemPrototype == null || itemCount <= 0) {
                menu.addItem(i, new CustomItemStack(Material.BARRIER, "§cInvalid data"));
                continue;
            }
            ItemStack itemPrototypeClone = itemPrototype.clone();
            itemPrototypeClone.setAmount(1);

            ItemStack display = itemPrototype.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                // 添加 PDC
                NamespacedKey slotKey = new NamespacedKey(MagicExpansion.getInstance(), "cargo_slot");
                meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, targetDataSlot);
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add("§7Stored Amount: §e" + itemCount);

                // ✅ 添加"正在输出"状态提示
                int currentOutput = -1;
                try {
                    String outputStr = data.getData(OUTPUT_TARGET_KEY);
                    if (outputStr != null && !outputStr.isEmpty()) {
                        currentOutput = Integer.parseInt(outputStr);
                    }
                } catch (Exception ignored) { }

                if (targetDataSlot == currentOutput) {
                    lore.add(""); // 空行
                    lore.add("§6§l▶ §eOutputting"); // 金色箭头 + 黄色文字
                }

                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            display.setAmount(1);

            int finalCurrentPage2 = currentPage;
            menu.addItem(i, display,(p, slot, clicked, action) -> {
                if (clicked == null) {
                    p.playSound(p.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    return false;
                }
                if (menu == null || data == null) return false;
                ItemMeta itemMeta = clicked.getItemMeta();

                if (itemMeta == null) return false;
                NamespacedKey slotKey = new NamespacedKey(MagicExpansion.getInstance(), "cargo_slot");

                if (!itemMeta.getPersistentDataContainer().has(slotKey, PersistentDataType.INTEGER)) return false;

                int targetDataSlot1 = itemMeta.getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER);
                String json1 = data.getData("item_type_" + targetDataSlot1);
                long itemCount1 = 0;
                try {
                    itemCount1 = Long.parseLong(data.getData("item_count_" + targetDataSlot1));
                } catch (Exception e) {
                    p.sendMessage("§cFailed to read the amount.");
                    return false;
                }

                if (json1 == null || itemCount1 <= 0) {
                    p.sendMessage("§cItem is no longer valid");
                    openStorageMenu(p, data, finalCurrentPage2); // 刷新
                    return false;
                }


                // === 区分点击类型 ===
                // 获取当前正在输出的目标槽
                int currentOutputTarget = -1;
                try {
                    String outputStr = data.getData("output_target_slot"); // 注意:你用的是 OUTPUT_TARGET_KEY？
                    if (outputStr != null && !outputStr.isEmpty()) {
                        currentOutputTarget = Integer.parseInt(outputStr);
                    }
                } catch (Exception ignored) { }
                if (action.isShiftClicked() && action.isRightClicked()) {
                    if (targetDataSlot1 == currentOutputTarget) {
                        // 当前物品正在被输出 → 停止
                        data.setData("output_target_slot", "-1");
                        p.sendMessage("§aStopped outputting: " + ItemStackHelper.getDisplayName(itemPrototypeClone));
                    } else {
                        // 当前物品不是输出目标 → 开始输出
                        data.setData("output_target_slot", String.valueOf(targetDataSlot1));
                        p.sendMessage("§eStarted continuous output: " + ItemStackHelper.getDisplayName(itemPrototypeClone));
                    }

                    refreshStorageMenu(menu, data, finalCurrentPage2);
                    return false;
                }
                else if (action.isShiftClicked() && !action.isRightClicked()) {
                    // Shift + 左键:存入背包中所有同类物品
                    int moved = 0;
                    for (ItemStack item : p.getInventory().getContents()) {
                        if (item != null && !item.getType().isAir()){
                            ItemStack itemClone = item.clone();
                            if(SameItemJudge.isSimilarSafe(itemClone, itemPrototypeClone)){
//                            if(SlimefunUtils.isItemSimilar(itemClone, itemPrototypeClone, true)){
                            int amount = item.getAmount();
                            storeItem(data, item.clone());
                            p.getInventory().removeItem(item);
                            moved += amount;
                            }
                        }
                    }
                    if (moved > 0) {
                        p.sendMessage("§aStored §e" + moved + " §aof " + ItemStackHelper.getDisplayName(itemPrototype) + "§r §ain storage.");
                        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5F, 1.2F);
                        refreshStorageMenu(menu, data, finalCurrentPage2);
                    } else {
                        p.sendMessage("§7Your inventory contains no matching items to store.");
                        p.playSound(p.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    }

                } else if (action.isRightClicked() && !action.isShiftClicked()) {
                    // 右键:尽可能填满背包
                    int filled = 0;
                    ItemStack toGive = itemPrototype.clone();
                    for (int invSlot = 0; invSlot < 36; invSlot++) {
                        ItemStack invItem = p.getInventory().getItem(invSlot);
                        if (invItem == null || invItem.getType().isAir()) {
                            int giveAmount = (int) Math.min(64, itemCount1);
                            if (giveAmount <= 0) break;
                            toGive.setAmount(giveAmount);
                            p.getInventory().setItem(invSlot, toGive.clone());
                            itemCount1 -= giveAmount;
                            filled += giveAmount;
                        } else if (SameItemJudge.isSimilarSafe(invItem, itemPrototypeClone) && invItem.getAmount() < 64) {
//                        } else if (SlimefunUtils.isItemSimilar(invItem, itemPrototypeClone, true) && invItem.getAmount() < 64) {
                            int space = 64 - invItem.getAmount();
                            int giveAmount = (int) Math.min(space, itemCount1);
                            if (giveAmount <= 0) break;
                            invItem.setAmount(invItem.getAmount() + giveAmount);
                            itemCount1 -= giveAmount;
                            filled += giveAmount;
                        }
                        if (itemCount1 <= 0) break;
                    }
                    if (filled > 0) {
                        data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount1));
                        p.sendMessage("§aAdded §e" + filled + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + " §ato your inventory.");
                        p.playSound(p.getLocation(), Sound.ITEM_BUNDLE_INSERT, 0.5F, 1.0F);
                        refreshStorageMenu(menu, data, finalCurrentPage2);
                    } else {
                        p.sendMessage("§7Inventory full, cannot withdraw any more.");
                        p.playSound(p.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    }

                } else if (!action.isRightClicked() && !action.isShiftClicked()) {
                    // 左键:取出 64 个
                    int take = (int) Math.min(64, itemCount1);
                    if (take <= 0) return false;
//                    ItemStack toTake = itemPrototype.clone().asQuantity(take);
//                    if (p.getInventory().addItem(toTake).isEmpty()) {
//                        itemCount1 -= take;
//                        data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount1));
//                        p.sendMessage("§a已取出 §e" + take + " §a个 " + ItemStackHelper.getDisplayName(itemPrototypeClone));
//                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
//                        refreshStorageMenu(menu, data, finalCurrentPage2);
//                    } else {
//                        p.sendMessage("§c背包空间不足,无法取出物品.");
//                        p.playSound(p.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
//                    }

                    ItemStack itemToGive = itemPrototypeClone.clone();
                    itemToGive.setAmount(take); // 尝试取出的数量
                    int maxCanHold = p.getInventory().getMaxStackSize();
                    int totalAvailableSpace = 0;
                    for (ItemStack stack : p.getInventory().getStorageContents()) { // 只看主背包+快捷栏(不含盔甲)
                        if (stack == null || stack.getType() == Material.AIR) {
                            totalAvailableSpace += maxCanHold;
                        } else if (stack.isSimilar(itemToGive)) {
                            totalAvailableSpace += maxCanHold - stack.getAmount();
                        }
                    }
                    int actualTake = Math.min(take, totalAvailableSpace);
                    if (actualTake <= 0) {
                        p.sendMessage("§cThere is not enough inventory space to withdraw the item.");
                        p.playSound(p.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                    } else {
                        itemToGive.setAmount(actualTake);
                        Map<Integer, ItemStack> leftover = p.getInventory().addItem(itemToGive);
                        if (!leftover.isEmpty()) {
                            itemCount1 -= actualTake;
                            data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount1));
                            p.sendMessage("§aWithdrew §e" + actualTake + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + "§a.");
                            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
                            refreshStorageMenu(menu, data, finalCurrentPage2);
                            p.sendMessage("§cSome items could not fit in your inventory and were dropped on the ground.");
                            for (ItemStack leftoverItem : leftover.values()) {
                                p.getWorld().dropItem(p.getLocation(), leftoverItem);
                            }
                        } else {
                            itemCount1 -= actualTake;
                            data.setData("item_count_" + targetDataSlot, String.valueOf(itemCount1));
                            p.sendMessage("§aWithdrew §e" + actualTake + " §aof " + ItemStackHelper.getDisplayName(itemPrototypeClone) + "§a.");
                            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.0F);
                            refreshStorageMenu(menu, data, finalCurrentPage2);
                        }
                    }

                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
                }

                return false;
            });
        }

        // === 刷新分页按钮 ===
        if (currentPage > 0) {
            int finalCurrentPage = currentPage;
            menu.addItem(45, new CustomItemStack(Material.ARROW, "§aPrevious Page — Current Page: "+(currentPage+1)+"/"+totalPages),(p, s, item, action) -> {
                refreshStorageMenu(menu, data, finalCurrentPage - 1);
                return false;
            });
        } else {
            menu.addItem(45, new CustomItemStack(Material.BARRIER, "§eAlready on the first page — Current Page: "+(currentPage+1)+"/"+totalPages),(p, s, item, action) -> false);
        }

        if (currentPage < totalPages - 1) {
            int finalCurrentPage1 = currentPage;
            menu.addItem(53, new CustomItemStack(Material.SPECTRAL_ARROW, "§aNext Page — Current Page: "+(currentPage+1)+"/"+totalPages),(p, s, item, action) -> {
                refreshStorageMenu(menu, data, finalCurrentPage1 + 1);
                return false;
            });
        } else {
            menu.addItem(53, new CustomItemStack(Material.BARRIER, "§eAlready on the last page — Current Page: "+(currentPage+1)+"/"+totalPages),(p, s, item, action) -> false);
        }

        // 装饰和中心物品不变,可选择性刷新
        ItemStack pinkGlow = doGlow(new ItemStack(Material.PINK_STAINED_GLASS_PANE));
        int[] pinkSlots = {46, 47, 48, 50, 51, 52};
        for (int slot : pinkSlots) {
            menu.addItem(slot, new CustomItemStack(pinkGlow, ColorGradient.getGradientName("Decorative Border")),(p, s, item, action) -> false);
        }

        menu.addItem(49, new CustomItemStack(Material.NETHER_STAR, "§6Storage Overview"),(p, s, item, action) -> false);
    }



    // 更新翻页按钮(带动态点击事件)
    private void updatePageButtons(BlockMenu menu, SlimefunBlockData data) {
        List<Integer> slots = getStoredItemSlots(data);
        int page = getCurrentPage(data);
        int totalPages = (int) Math.ceil((double) slots.size() / ITEMS_PER_PAGE);

        boolean hasPrev = page > 0;
        boolean hasNext = (page + 1) < totalPages;

        // 上一页按钮
        menu.replaceExistingItem(45, hasPrev ?
                new CustomItemStack(Material.ARROW, "§bPrevious Page (Stored Items)","§7Page: "+(page+1)+"/"+totalPages) :
                new CustomItemStack(Material.BARRIER, "§cAlready on the first page (Stored Items)","§7Page: "+(page+1)+"/"+totalPages));

        // ✅ 重新绑定点击处理器
        menu.addMenuClickHandler(45, (player, slot, item, action) -> {
            if (hasPrev) {
                data.setData("storage_page", String.valueOf(page - 1));
                updateStorageDisplay(menu, data); // 这会再次调用 updatePageButtons
                player.playSound(menu.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5F, 1.0F);
            } else {
                player.playSound(menu.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
            }
            return false;
        });

        // 下一页按钮
        menu.replaceExistingItem(48, hasNext ?
                new CustomItemStack(Material.SPECTRAL_ARROW, "§bNext Page (Stored Items)","§7Page: "+(page+1)+"/"+totalPages) :
                new CustomItemStack(Material.BARRIER, "§cAlready on the last page (Stored Items)","§7Page: "+(page+1)+"/"+totalPages));

        menu.addMenuClickHandler(48, (player, slot, item, action) -> {
            if (hasNext) {
                data.setData("storage_page", String.valueOf(page + 1));
                updateStorageDisplay(menu, data);
                player.playSound(menu.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5F, 1.0F);
            } else {
                player.playSound(menu.getLocation(), Sound.BLOCK_METAL_HIT, 0.3F, 0.5F);
            }
            return false;
        });
    }

    // 获取当前页码
    private int getCurrentPage(SlimefunBlockData data) {
        try {
            String pageStr = data.getData("storage_page");
            if (pageStr != null) {
                return Math.max(0, Integer.parseInt(pageStr));
            }
        } catch (Exception ignored) {}
        return 0;
    }


    // 格式化大数字显示
    private String formatNumber(long n) {
        if (n >= 1_000_000_000) return (n / 1_000_000_000) + "B";
        if (n >= 1_000_000) return (n / 1_000_000) + "M";
        if (n >= 1_000) return (n / 1_000) + "K";
        return String.valueOf(n);
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

    protected void constructMenu(BlockMenuPreset preset) {

//        for (int i : pinkBorder ) {
//            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack (Material.PINK_STAINED_GLASS_PANE)), ColorGradient.getGradientName("快捷合成翻页")),
//                    (p, slot, item, action) -> false);
//        }

        for (int i : blueBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack (Material.LIGHT_BLUE_STAINED_GLASS_PANE)), ColorGradient.getGradientName("Item Storage Page Controls")),
                    (p, slot, item, action) -> false);
        }

        for (int i : arrowSlot) {
            preset.addItem(i, new CustomItemStack(new ItemStack (Material.BARRIER), " "),
                    (p, slot, item, action) -> false);
        }
        for (int i : inputOutputLine) {
            preset.addItem(i, new CustomItemStack(new ItemStack (Material.PINK_STAINED_GLASS_PANE), ColorGradient.getGradientName("====== Divider ======="),ColorGradient.getGradientName("←←← Input Slots"),ColorGradient.getGradientName("Output Slots →→→")),
                    (p, slot, item, action) -> false);
        }
        for (int i : storageSlots) {
            preset.addItem(i, new CustomItemStack(new ItemStack (Material.BARRIER), ColorGradient.getGradientName(" ")),
                    (p, slot, item, action) -> false);
        }
        for (int i : transportSlots) {
            preset.addItem(i, new CustomItemStack(new ItemStack (Material.BARRIER), ColorGradient.getGradientName(" ")),
                    (p, slot, item, action) -> false);
        }
        for (int i : transportSlots2) {
            preset.addItem(i, new CustomItemStack(new ItemStack (Material.BARRIER), ColorGradient.getGradientName(" ")),
                    (p, slot, item, action) -> false);
        }
        preset.addItem(27, new CustomItemStack(new ItemStack (Material.LIGHT_BLUE_STAINED_GLASS_PANE), ColorGradient.getGradientName("Item Storage")),
                (p, slot, item, action) -> false);
        preset.addItem(40, new CustomItemStack(new ItemStack (Material.PINK_STAINED_GLASS_PANE),
                        ColorGradient.getGradientName("← Send Items Outward"),
                        ColorGradient.getGradientName("← Maximum 256 Entries"),
                        ColorGradient.getGradientName("Send Items Outward →"),
                        ColorGradient.getGradientName("Maximum 512 Coordinates →"),
                        ColorGradient.getGradientName("Click to open the Transfer Overview.")
                        ),
                (p, slot, item, action) -> false);

        preset.addItem(49, new CustomItemStack(new ItemStack (Material.SOUL_LANTERN), ColorGradient.getGradientName("Magic Storage Terminal"),"§eStored Item Controls","§bLeft-click: Withdraw one stack","§bRight-click fill inventory","§bShift-left-click: Store every matching item from your inventory","§bShift-right-click: Set the output item",
                "§eRemote Transfer Controls","§bSee the detailed description."),
                (p, slot, item, action) -> false);

    }



    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.NONE;
    }

    @Override
    public int getCapacity() {
        return 0;
    }











    /*
    远程传输相关代码
     */

    private static final int[] TARGET_SLOTS = {36, 38};
    private static final int[] CUSTOM_SLOTS = {37, 39};
    private static final int BUTTON_PREV_PAGE = 50;
    private static final int BUTTON_NEXT_PAGE = 51;

    private static final int PAIRS_PER_PAGE = 2; //定义每页显示多少组坐标模板
    private static final int MAX_BIND_PAIRS = 256; //定义最多多少组坐标模板

    private static final int[] TRANSFER_AMOUNTS = {
            1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 3456
    };

    private int translateOutputPage = 0;


    private long getStoredItemCount(@Nonnull Block block, int index) {
        try {
            String countStr = BlockStorage.getLocationInfo(block.getLocation())
                    .getString("stored_item_count_" + index);
            return countStr != null ? Long.parseLong(countStr) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }



    public void updateTranslateOutPut(BlockMenu menu, Block b) {
        int totalPages = Math.max(1, (MAX_BIND_PAIRS + PAIRS_PER_PAGE - 1) / PAIRS_PER_PAGE);

        for (int i = 0; i < PAIRS_PER_PAGE; i++) {
            int pairIndex = translateOutputPage * PAIRS_PER_PAGE + i;
            int targetSlot = TARGET_SLOTS[i];
            int customSlot = CUSTOM_SLOTS[i];

            // ====== 目标坐标 (36/38) ======
            String binding = BlockStorage.getLocationInfo(b.getLocation(), "output_bind_pair_" + pairIndex);
            boolean hasBinding = binding != null && !binding.isEmpty();

            ItemStack targetItem;
            if (hasBinding) {
                Location loc = parseLocation(binding);
                SlimefunItem sfItem = BlockStorage.check(loc);
                if (sfItem != null) {
                    targetItem = sfItem.getItem().clone();
                } else {
                    Block block = loc.getBlock();
                    if (block.getType() != Material.AIR) {
                        // 安全地创建物品堆栈,避免使用无效的物品类型
                        try {
                            // 检查材料是否是有效的物品类型
                            Material blockType = block.getType();
                            if (isValidItemType(blockType)) {
                                targetItem = new ItemStack(blockType);
                                // 现移除在主线程获取blockstate的操作    用处不大
                                // 🔹 修复:在主线程中获取 BlockState
//                                if (Bukkit.isPrimaryThread()) {
//                                    // 如果在主线程,直接获取
//                                    BlockState state = block.getState();
//                                    ItemMeta meta = targetItem.getItemMeta();
//                                    if (meta instanceof BlockStateMeta bsm) {
//                                        bsm.setBlockState(state);
//                                        targetItem.setItemMeta(bsm);
//                                    }
//                                } else {
//                                    // 如果在异步线程,使用同步方式获取
//                                    ItemStack finalTargetItem = targetItem;
//                                    Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
//                                        try {
//                                            BlockState state = block.getState();
//                                            ItemMeta meta = finalTargetItem.getItemMeta();
//                                            if (meta instanceof BlockStateMeta bsm) {
//                                                bsm.setBlockState(state);
//                                                finalTargetItem.setItemMeta(bsm);
//                                            }
//                                        } catch (IllegalStateException e) {
//                                            // 如果仍然失败,记录错误并使用默认方式
//                                            MagicExpansion.getInstance().getLogger().warning(
//                                                    "无法获取方块状态在位置: " + block.getLocation() +
//                                                            ", 类型: " + block.getType()
//                                            );
//                                        }
//                                    });
//                                }
                            } else {
                                // 对于墙上的标志等非物品方块,使用替代的显示物品
                                targetItem = getAlternativeDisplayItem(blockType);
                            }
                        } catch (IllegalArgumentException e) {
                            // 如果创建物品堆栈失败,使用默认的替代物品
                            targetItem = new ItemStack(Material.COMPASS);
                        }
                    } else {
                        targetItem = new ItemStack(Material.COMPASS);
                    }
                }
            } else {
                targetItem = new ItemStack(Material.RECOVERY_COMPASS);
            }

            ItemMeta targetMeta = targetItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ItemStackHelper.getDisplayName(targetItem));
            if (targetMeta != null && targetMeta.hasLore()) {
                lore.addAll(targetMeta.getLore());
            }
            lore.add("");
            if (hasBinding) {
                String[] parts = binding.split(",", 4);
                lore.add("§bBound Coordinates: §e" + parts[0] + ", " + parts[1] + ", " + parts[2]);
                lore.add("§bWorld: §e" + parts[3]);
            } else {
                lore.add("§7No target bound");
            }
            lore.add("");
            lore.add("§fLeft-click: Hold Void Touch and click a block to bind it.");
            lore.add("§fRight-click: Clear");

            if (targetMeta == null) {
                targetMeta = Bukkit.getItemFactory().getItemMeta(Material.RECOVERY_COMPASS);
            }
            targetMeta.setDisplayName("§eTarget Coordinates");
            targetMeta.setLore(lore);
            targetItem.setItemMeta(targetMeta);
            menu.addItem(targetSlot, targetItem, (player, slot, clickedItem, action) -> {
                // 🔹 右键:清除绑定
                if (action.isRightClicked()) {
                    // 清除该槽位的绑定
                    BlockStorage.addBlockInfo(b.getLocation(), "output_bind_pair_" + pairIndex, "");
                    updateTranslateOutPut(menu, b);
                    player.sendMessage("§aCleared binding #" + (pairIndex + 1));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5F, 0.5F);
                    return false;
                }

                // 🔹 左键:绑定(必须光标上有 VoidTouch)
                if (!action.isRightClicked()) {
                    ItemStack cursor = player.getItemOnCursor();
                    if (cursor == null || cursor.getType().isAir()) {
                        player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                        return false;
                    }

                    SlimefunItem sfItem = SlimefunItem.getByItem(cursor);
                    if (!(sfItem instanceof VoidTouch)) {
                        player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                        return false;
                    }

                    ItemMeta meta = cursor.getItemMeta();
                    if (meta == null) {
                        player.sendMessage("§cThis item is missing metadata and its binding cannot be read.");
                        return false;
                    }

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    // 使用常量或统一定义 Keys
                    NamespacedKey keyX = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
                    NamespacedKey keyY = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
                    NamespacedKey keyZ = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
                    NamespacedKey keyWorld = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

                    if (!container.has(keyX, PersistentDataType.INTEGER) ||
                            !container.has(keyY, PersistentDataType.INTEGER) ||
                            !container.has(keyZ, PersistentDataType.INTEGER) ||
                            !container.has(keyWorld, PersistentDataType.STRING)) {
                        player.sendMessage("§cError: §dVoid Touch §cis not bound to any coordinates!");
                        return false;
                    }

                    int x = container.get(keyX, PersistentDataType.INTEGER);
                    int y = container.get(keyY, PersistentDataType.INTEGER);
                    int z = container.get(keyZ, PersistentDataType.INTEGER);
                    String worldName = container.get(keyWorld, PersistentDataType.STRING);

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        player.sendMessage("§cThe bound world §e" + worldName + " §cdoes not exist.");
                        return false;
                    }

                    // ✅ 保存绑定
                    String newValue = x + "," + y + "," + z + "," + worldName;
                    BlockStorage.addBlockInfo(b.getLocation(), "output_bind_pair_" + pairIndex, newValue);

                    // ✅ 更新界面
                    updateTranslateOutPut(menu, b);

                    // ✅ 反馈
                    player.sendMessage("§aSuccessfully bound target location §e#" + (pairIndex + 1));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 0.5F, 1.0F);

                    return false;
                }

                return false; // 其他点击忽略
            });



            // ✅ 使用 addItem!简单直接!
            // ====== 物品模板 (37/39) ======
            ItemStack templateItem = getItemTemplate(b, pairIndex);
            boolean hasTemplate = templateItem != null;
            int amount = getTransferAmount(b, pairIndex);

            ItemStack displayItem = hasTemplate ? templateItem.clone() : new ItemStack(Material.GRAY_DYE);
            ItemMeta customMeta = displayItem.getItemMeta();
            if (customMeta == null) {
                customMeta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
            }

            List<String> customLore = new ArrayList<>();
            customLore.add("§r"+ItemStackHelper.getDisplayName(displayItem));
            if (customMeta.hasLore()) {
                customLore.addAll(customMeta.getLore());
            }
            customLore.add("");

            if (hasTemplate) {
                // 3. 从主存储系统获取当前库存(修复关键问题)
                long currentStock = getStoredItemCountFromMainStorage(Objects.requireNonNull(StorageCacheUtils.getBlock(b.getLocation())), displayItem);
                customLore.add("§bStock: §e" + currentStock);
                customLore.add("");
                customLore.add("§bTransfer Amount: §e" + amount);
                customLore.add("");
                customLore.add("§fLeft-click: Change amount");
                customLore.add("§fRight-click: Clear template");
            } else {
                customLore.add("§7No item template");
                customLore.add("");
                customLore.add("§fLeft-click with a cursor item: Set template");
            }

            customMeta.setDisplayName("§eTransfer Item Template");
            customMeta.setLore(customLore);
            displayItem.setItemMeta(customMeta);

            menu.addItem(customSlot, displayItem, (player, slot, clickedItem, action) -> {
                ItemStack cursor = player.getItemOnCursor();

                // 🔹 右键:清除模板(无论有没有)
                if (action.isRightClicked()) {
                    if (hasTemplate) {
                        setItemTemplate(b, pairIndex, null);
                        updateTranslateOutPut(menu, b);
                        player.sendMessage("§aCleared template #" + (pairIndex + 1));
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.3F, 0.5F);
                    } else {
                        player.sendMessage("§cThis slot has no template to clear.");
                    }
                    return false;
                }

                // 🔹 左键:分两种情况
                if (!action.isRightClicked()) {
                    // 情况1:还没有模板 → 用光标物品设置模板
                    if (!hasTemplate) {
                        if (cursor == null || cursor.getType().isAir()) {
                            player.sendMessage("§cPlace the item to use as the template on your cursor!");
                            return false;
                        }

                        ItemStack newTemplate = cursor.clone();
                        newTemplate.setAmount(1); // 模板统一为1个
                        setItemTemplate(b, pairIndex, newTemplate);
                        setTransferAmount(b, pairIndex, 1); // 默认数量为1

                        updateTranslateOutPut(menu, b);
                        String name = newTemplate.hasItemMeta() && newTemplate.getItemMeta().hasDisplayName() ?
                                newTemplate.getItemMeta().getDisplayName() : newTemplate.getType().name().toLowerCase().replace('_', ' ');

                        player.sendMessage("§aTemplate set: §e" + name);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.8F);
                        return false;
                    }

                    // 情况2:已有模板 → 切换传输数量
                    int current = getTransferAmount(b, pairIndex);
                    int nextIdx = 0;
                    for (int j = 0; j < TRANSFER_AMOUNTS.length; j++) {
                        if (current == TRANSFER_AMOUNTS[j]) {
                            nextIdx = (j + 1) % TRANSFER_AMOUNTS.length;
                            break;
                        }
                    }
                    int newAmount = TRANSFER_AMOUNTS[nextIdx];
                    setTransferAmount(b, pairIndex, newAmount);
                    updateTranslateOutPut(menu, b);

                    player.sendMessage("§eTransfer Amount: §6" + newAmount);
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.3F, 1.0F);
                    return false;
                }

                return false;
            });



        }

        // === 翻页按钮 ===
        ItemStack nextItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta nextMeta = nextItem.getItemMeta();
        nextMeta.setDisplayName("§eNext Page"+" (Output)");
        nextMeta.setLore(List.of("§7Page: " + (translateOutputPage + 1) + "/" + totalPages));
        nextItem.setItemMeta(nextMeta);

        if (translateOutputPage >= totalPages - 1) {
            nextItem = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = nextItem.getItemMeta();
            barrierMeta.setDisplayName("§cNo more pages"+" (Output)");
            nextItem.setItemMeta(barrierMeta);
        }

        menu.addItem(BUTTON_NEXT_PAGE, nextItem, (player, slot, item, action) -> {
            if (translateOutputPage < totalPages - 1) {
                translateOutputPage++;
                updateTranslateOutPut(menu, b);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.3F, 0.6F);
            }
            return false;
        });

        ItemStack prevItem = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevItem.getItemMeta();
        prevMeta.setDisplayName("§ePrevious Page"+" (Input)");
        prevMeta.setLore(List.of("§7Page: " + (translateOutputPage + 1) + "/" + totalPages));
        prevItem.setItemMeta(prevMeta);

        if (translateOutputPage <= 0) {
            prevItem = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = prevItem.getItemMeta();
            barrierMeta.setDisplayName("§cAlready on the first page"+" (Input)");
            prevItem.setItemMeta(barrierMeta);
        }

        menu.addItem(BUTTON_PREV_PAGE, prevItem, (player, slot, item, action) -> {
            if (translateOutputPage > 0) {
                translateOutputPage--;
                updateTranslateOutPut(menu, b);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.3F, 0.6F);
            }
            return false;
        });
    }

    // ====== 辅助方法 ======

    /**
     * 检查材料是否是有效的物品类型
     */
    private boolean isValidItemType(Material material) {
        try {
            // 尝试创建物品堆栈来验证
            new ItemStack(material);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 为无效的物品类型获取替代的显示物品
     */
    private ItemStack getAlternativeDisplayItem(Material originalType) {
        // 先尝试去掉WALL_前缀获取普通版本
        if (originalType.name().startsWith("WALL_")) {
            String baseName = originalType.name().replace("WALL_", "");
            try {
                Material baseMaterial = Material.valueOf(baseName);
                if (isValidItemType(baseMaterial)) {
                    return new ItemStack(baseMaterial);
                }
            } catch (IllegalArgumentException ignored) {
                // 如果转换失败,继续处理特殊情况
            }

            // 如果去掉前缀后无效,处理特殊情况
            if (originalType.name().contains("SIGN")) {
                return new ItemStack(Material.OAK_SIGN);
            }
            if (originalType.name().contains("BANNER")) {
                return new ItemStack(Material.BLACK_BANNER);
            }
        }

        // 默认安全替代品
        return new ItemStack(Material.COMPASS);
    }


    /**
     * 为无效的物品类型提供安全的替代显示物品
     */
    private ItemStack getSafeAlternativeItem(Material originalType) {
        // 先尝试去掉WALL_前缀获取普通版本
        if (originalType.name().startsWith("WALL_")) {
            String baseName = originalType.name().replace("WALL_", "");
            try {
                Material baseMaterial = Material.valueOf(baseName);
                if (isValidItemType(baseMaterial)) {
                    return new ItemStack(baseMaterial);
                }
            } catch (IllegalArgumentException ignored) {
                // 如果转换失败,继续处理特殊情况
            }

            // 如果去掉前缀后无效,处理特殊情况
            if (originalType.name().contains("SIGN")) {
                return new ItemStack(Material.OAK_SIGN);
            }
            if (originalType.name().contains("BANNER")) {
                return new ItemStack(Material.BLACK_BANNER);
            }
        }

        // 默认安全替代品
        return new ItemStack(Material.COMPASS);
    }


    private Location parseLocation(String str) {
        try {
            String[] parts = str.split(",", 4);
            World world = Bukkit.getWorld(parts[3]);
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new Location(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack getItemTemplate(Block b, int index) {
        // 从 BlockStorage 读取配置
        Config config = BlockStorage.getLocationInfo(b.getLocation());
        String key = "template_item_" + index;

        if (!config.contains(key)) {
            return null;
        }

        // 从 Base64 反序列化 ItemStack
        return SameItemJudge.itemFromBase64(config.getString(key));
    }

    private void setItemTemplate(Block b, int index, ItemStack item) {
        String key = "template_item_" + index;

        if (item == null || item.getType().isAir()) {
            // 清除模板
            BlockStorage.addBlockInfo(b.getLocation(), key, ""); // 空字符串表示清除
            return;
        }

        try {
            String base64 = SameItemJudge.itemToBase64(item);
            if (base64 != null && !base64.isEmpty()) {
                BlockStorage.addBlockInfo(b.getLocation(), key, base64);
            } else {
//                Debug.logWarn("无法序列化模板物品 #" + index + " 为 Base64");
            }
        } catch (Exception e) {
//            Debug.logWarn("序列化模板物品失败: " + e.getMessage());
        }
    }

    private int getTransferAmount(Block b, int index) {
        String amountStr = BlockStorage.getLocationInfo(b.getLocation(), "output_amount_" + index);
        try {
            return amountStr != null ? Integer.parseInt(amountStr) : 64;
        } catch (NumberFormatException e) {
            return 64;
        }
    }

    private void setTransferAmount(Block b, int index, int amount) {
        BlockStorage.addBlockInfo(b.getLocation(), "output_amount_" + index, String.valueOf(amount));
    }



    /*
    对外传输 - 修复版本
     */

    private static final int MAX_BATCH_SIZE = 64;

    private void handleAllTemplateTransfers(Block b) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
        if (data == null) return;

        for (int pairIndex = 0; pairIndex < MAX_BIND_PAIRS; pairIndex++) {
            ItemStack templateItem = getItemTemplate(b, pairIndex);
            if (templateItem != null && !templateItem.getType().isAir()) {
                transferTemplateItem(b, pairIndex, data);
            }
            if (templateItem != null && !templateItem.getType().isAir()) {
//                transferToVanillaContainer(b, pairIndex, data);
                if (Bukkit.isPrimaryThread()) {
                    transferToVanillaContainer(b, pairIndex, data);
                } else {
                    int finalPairIndex = pairIndex;
                    Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
                        transferToVanillaContainer(b, finalPairIndex, data);
                    });
                }
            }
        }
    }


    /**
     * 传输物品到原版容器
     */
    private void transferToVanillaContainer(Block sourceBlock, int pairIndex, SlimefunBlockData data) {
        // 获取目标位置
        Location targetLocation = getTargetLocation(sourceBlock, pairIndex);
        if (targetLocation == null) {
            return;
        }

        // 如果是Slimefun方块,不处理
        SlimefunItem sfItem = StorageCacheUtils.getSfItem(targetLocation);
        if (null != sfItem) return;

        // 获取模板物品
        ItemStack template = getItemTemplate(sourceBlock, pairIndex);
        if (template == null || template.getType().isAir()) return;

        // 获取传输数量
        int configuredAmount = getTransferAmount(sourceBlock, pairIndex);
        if (configuredAmount <= 0) {
            return;
        }

        // 从主存储系统获取当前库存
        long currentStock = getStoredItemCountFromMainStorage(data, template);
        if (currentStock <= 0) {
            return;
        }

//        // 验证目标位置是否是原版容器
//        if (!isVanillaContainer(targetLocation)) {
//            return;
//        }

        // 获取目标容器
        Block targetBlock = targetLocation.getBlock();
        BlockState blockState = targetBlock.getState();

        if (!(blockState instanceof InventoryHolder)) {
            return;
        }

        InventoryHolder holder = (InventoryHolder) blockState;
        Inventory inventory = holder.getInventory();

        // 计算实际传输数量
        int actualTransferAmount = (int) Math.min(currentStock, configuredAmount);

        // 预测容器能接收多少物品
        int maxFit = predictMaxFitVanilla(inventory, template, actualTransferAmount);
        if (maxFit <= 0) {
            return;
        }

        // 最终传输量取最小值
        int finalTransferAmount = Math.min(actualTransferAmount, maxFit);

        // 创建要传输的物品
        ItemStack toTransfer = template.clone();
        toTransfer.setAmount(finalTransferAmount);

        // 尝试将物品放入容器
        HashMap<Integer, ItemStack> leftover = inventory.addItem(toTransfer);

        // 计算实际传输的数量
        int actuallyTransferred = finalTransferAmount;
        if (!leftover.isEmpty()) {
            for (ItemStack left : leftover.values()) {
                actuallyTransferred -= left.getAmount();
            }
        }

        if (actuallyTransferred > 0) {
            // 从存储中扣除已传输的物品
            deductStoredItemFromMainStorage(data, template, actuallyTransferred);

            // 显示传输效果
            showTransferParticles(sourceBlock.getLocation(), targetLocation, Particle.VILLAGER_HAPPY);
        }
    }

    /**
     * 预测原版容器最多能接收多少物品
     */
    private int predictMaxFitVanilla(Inventory inventory, ItemStack prototype, int maxAmount) {
        ItemStack singleItem = prototype.clone();
        singleItem.setAmount(1);

        int totalFit = 0;

        // 遍历所有槽位
        for (int i = 0; i < inventory.getSize(); i++) {
            if (totalFit >= maxAmount) break;

            ItemStack existing = inventory.getItem(i);

            if (existing == null || existing.getType().isAir()) {
                // 空槽位,可以放一整组
                totalFit += Math.min(prototype.getMaxStackSize(), maxAmount - totalFit);
            } else if (existing.isSimilar(singleItem)) {
                // 相同物品,计算剩余空间
                int space = existing.getMaxStackSize() - existing.getAmount();
                totalFit += Math.min(space, maxAmount - totalFit);
            }
        }

        return totalFit;
    }

    /**
     * 执行一次物品推送(修复版)
     */
    public void transferTemplateItem(@Nonnull Block sourceBlock, int pairIndex, @Nonnull SlimefunBlockData data) {
        // 1. 获取模板物品
        ItemStack template = getItemTemplate(sourceBlock, pairIndex);
        if (template == null || template.getType().isAir()) return;

        // 2. 获取设定推送数量
        int configuredAmount = getTransferAmount(sourceBlock, pairIndex);
        if (configuredAmount <= 0) return;

        // 3. 从主存储系统获取当前库存(修复关键问题)
        long currentStock = getStoredItemCountFromMainStorage(data, template);
        if (currentStock <= 0) return;

        // 4. 实际推送量 = min(设定量, 库存量)
        int amountToTransfer = (int) Math.min(configuredAmount, currentStock);
        if (amountToTransfer <= 0) return;

        // 5. 获取目标位置(修复键名)
        Location targetLocation = getTargetLocation(sourceBlock, pairIndex);
        if (targetLocation == null) return;

        // 6. 验证目标位置是否有效
        if (!isValidTarget(targetLocation)) {
            // 目标无效,清除绑定
//            data.setData("output_bind_pair_" + pairIndex, "");
            return;
        }

        // 7. 预测目标最多能接收多少
        int maxFit = predictMaxFit(targetLocation, template, amountToTransfer);
        if (maxFit <= 0) return;

        // 8. [从主存储系统扣除库存]
        long deducted = deductStoredItemFromMainStorage(data, template, maxFit);
        if (deducted <= 0) return;

        // 9. 执行推送
        int actualPushed = pushItemsToLocation(sourceBlock, targetLocation, template, (int) deducted);

        // 10. 播放音效和粒子效果
        if (actualPushed > 0) {
//            sourceBlock.getWorld().playSound(
//                    sourceBlock.getLocation(),
//                    Sound.ENTITY_ENDERMAN_TELEPORT,
//                    0.3F,
//                    1.0F
//            );

            // 添加粒子效果显示传输
            showTransferParticles(sourceBlock.getLocation(), targetLocation, Particle.END_ROD);
        }

        // 11. 如果实际推送量小于扣除量,将差额退回存储
        if (deducted > actualPushed) {
            long refundAmount = deducted - actualPushed;
            refundToMainStorage(data, template, refundAmount);
        }
    }

    /**
     * 从主存储系统获取物品数量(修复关键问题)
     */
    private long getStoredItemCountFromMainStorage(@Nonnull SlimefunBlockData data, @Nonnull ItemStack template) {
        if (template.getType() == Material.AIR) return 0;

        ItemStack prototype = template.clone();
        prototype.setAmount(1);

        // 遍历所有存储槽位查找匹配物品
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) continue;

            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem != null && SameItemJudge.isSimilarSafe(storedItem, prototype)) {
//                if (storedItem != null && SlimefunUtils.isItemSimilar(storedItem, prototype, true)) {
                    String countStr = data.getData("item_count_" + i);
                    if (countStr != null && !countStr.isEmpty()) {
                        try {
                            return Long.parseLong(countStr);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        return 0;
    }

    /**
     * 从主存储系统扣除物品(修复关键问题)
     */
    private long deductStoredItemFromMainStorage(@Nonnull SlimefunBlockData data, @Nonnull ItemStack template, long amount) {
        if (template.getType() == Material.AIR || amount <= 0) return 0;

        ItemStack prototype = template.clone();
        prototype.setAmount(1);

        // 查找匹配的存储槽位
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) continue;

            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem != null && SameItemJudge.isSimilarSafe(storedItem, prototype)) {
//                if (storedItem != null && SlimefunUtils.isItemSimilar(storedItem, prototype, true)) {
                    String countStr = data.getData("item_count_" + i);
                    if (countStr == null || countStr.isEmpty()) continue;

                    try {
                        long currentCount = Long.parseLong(countStr);
                        long toDeduct = Math.min(currentCount, amount);

                        if (toDeduct <= 0) return 0;

                        long newCount = currentCount - toDeduct;

                        if (newCount <= 0) {
                            // 数量为0,清除该槽位
                            data.removeData("item_type_" + i);
                            data.removeData("item_count_" + i);
                        } else {
                            data.setData("item_count_" + i, String.valueOf(newCount));
                        }

                        return toDeduct;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        return 0;
    }

    /**
     * 退还物品到主存储系统
     */
    private void refundToMainStorage(@Nonnull SlimefunBlockData data, @Nonnull ItemStack template, long amount) {
        if (amount <= 0) return;

        // 直接调用现有的storeItem方法
        ItemStack refundStack = template.clone();
        refundStack.setAmount((int) Math.min(amount, Integer.MAX_VALUE));
        storeItem(data, refundStack);
    }

    /**
     * 获取目标位置(修复键名匹配问题)
     */
    private Location getTargetLocation(Block block, int index) {
        // 使用正确的键名:output_bind_pair_ 而不是 target_location_
        String locStr = BlockStorage.getLocationInfo(block.getLocation()).getString("output_bind_pair_" + index);
        if (locStr == null || locStr.isEmpty()) return null;

        String[] parts = locStr.split(",");
        if (parts.length != 4) return null;

        try {
            World world = Bukkit.getWorld(parts[3]);
            if (world == null) return null;

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 验证目标位置是否有效
     */
    private boolean isValidTarget(Location location) {
        if (location == null) return false;

        World world = location.getWorld();
        if (world == null) return false;

        // 检查区块是否加载
        if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return false;
        }

        // 检查目标方块是否存在且是Slimefun方块
        Block targetBlock = location.getBlock();
        return BlockStorage.check(targetBlock) != null;
    }

    /**
     * 预测目标最多能接收多少(优化版)
     */
    private int predictMaxFit(Location targetLocation, ItemStack prototype, int maxAmount) {
        Block targetBlock = targetLocation.getBlock();
        BlockMenu targetMenu = BlockStorage.getInventory(targetBlock);

        if (targetMenu == null) return 0;

        int[] inputSlots = targetMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.INSERT);
        if (inputSlots == null || inputSlots.length == 0) return 0;

        int totalFit = 0;
        ItemStack singleItem = prototype.clone();
        singleItem.setAmount(1);

        for (int slot : inputSlots) {
            if (totalFit >= maxAmount) break;

            ItemStack existing = targetMenu.getItemInSlot(slot);
            ItemStack existing2 = null;
            if (existing != null) {
                existing2 = targetMenu.getItemInSlot(slot).clone();
                existing2.setAmount(1);
            }
            if (existing == null || existing.getType().isAir()) {
                // 空槽位,可以放一整组
                totalFit += Math.min(prototype.getMaxStackSize(), maxAmount - totalFit);
            } else if (SameItemJudge.isSimilarSafe(singleItem, existing2)) {
//            } else if (SlimefunUtils.isItemSimilar(singleItem, existing2, true)) {
                // 相同物品,计算剩余空间
                int space = existing.getMaxStackSize() - existing.getAmount();
                totalFit += Math.min(space, maxAmount - totalFit);
            }
        }
        return totalFit;
    }

    /**
     * 推送物品到目标(优化版)
     */
    private int pushItemsToLocation(Block sourceBlock, Location targetLocation, ItemStack prototype, int requestAmount) {
        Block targetBlock = targetLocation.getBlock();
        BlockMenu targetMenu = BlockStorage.getInventory(targetBlock);

        if (targetMenu == null) return 0;

        int[] inputSlots = targetMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.INSERT);
        if (inputSlots == null || inputSlots.length == 0) return 0;

        int totalPushed = 0;
        int remaining = requestAmount;

        while (remaining > 0) {
            int batchSize = Math.min(MAX_BATCH_SIZE, remaining);
            ItemStack toPush = prototype.clone();
            toPush.setAmount(batchSize);

            ItemStack leftover;
            try {
                leftover = targetMenu.pushItem(toPush, inputSlots);
            } catch (Exception e) {
//                Debug.logInfo("推送物品到目标时发生错误: " + e.getMessage());
                break;
            }

            int pushed = batchSize;
            if (leftover != null) {
                pushed = batchSize - leftover.getAmount();
            }

            totalPushed += pushed;
            remaining -= pushed;

            if (pushed == 0 || leftover != null && leftover.getAmount() > 0) {
                // 无法再推送更多物品
                break;
            }
        }

        return totalPushed;
    }

    /**
     * 显示传输粒子效果
     */
    private void showTransferParticles(Location from, Location to, Particle particle) {
        World world = from.getWorld();
        if (world == null) return;

        // 在起点显示粒子
        world.spawnParticle(Particle.PORTAL, from.clone().add(0.5, 1, 0.5), 10, 0.3, 0.3, 0.3, 0.1);

        // 在终点显示粒子
        world.spawnParticle(Particle.REVERSE_PORTAL, to.clone().add(0.5, 1, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
        // 检查两个位置是否在同一世界
        if (!from.getWorld().equals(to.getWorld())) {
            // 如果不在同一世界,可以选择跳过粒子效果或进行其他处理
            return; // 本例中直接跳过粒子效果的生成
        }
        // 显示连接线(简单版本)
        double distance = from.distance(to);
        if (distance < 20) { // 只对短距离显示连线
            int points = (int) (distance * 2);
            for (int i = 0; i <= points; i++) {
                double ratio = (double) i / points;
                Location point = from.clone().add(
                        (to.getX() - from.getX()) * ratio,
                        (to.getY() - from.getY()) * ratio + Math.sin(ratio * Math.PI) * 2,
                        (to.getZ() - from.getZ()) * ratio
                );
                world.spawnParticle(particle, point, 1, 0, 0, 0, 0);
            }
        }
    }


















    /*
    输入传输 - 从其他机器输出槽抽取物品
     */

    private static final int[] INPUT_BIND_SLOTS = {41, 42, 43, 44}; // 输入绑定槽位
    private static final int INPUT_PAIRS_PER_PAGE = 4; // 每页显示4组(对应4个槽位)
    private static final int MAX_INPUT_BIND_PAIRS = 512; // 最大输入绑定数量
    private int inputBindPage = 0; // 输入绑定当前页码

    /**
     * 处理所有输入传输(从绑定机器抽取物品)- 修复版
     */
    private void handleAllInputTransfers(Block b, SlimefunBlockData data) {
        for (int pairIndex = 0; pairIndex < MAX_INPUT_BIND_PAIRS; pairIndex++) {
            Location sourceLocation = getInputSourceLocation(b, pairIndex);
            if (sourceLocation != null && isValidInputSource(sourceLocation)) {
                transferFromSource(b, data, sourceLocation, pairIndex);
            }
        }
    }

    /**
     * 验证输入源是否有效
     */
    private boolean isValidInputSource(Location location) {
        if (location == null) return false;

        World world = location.getWorld();
        if (world == null) return false;

        // 检查区块是否加载
        if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return false;
        }

        // 检查目标方块是否存在且是Slimefun方块
        Block targetBlock = location.getBlock();
        SlimefunItem sfItem = BlockStorage.check(targetBlock);
        if (sfItem == null) {
//            Debug.logInfo("输入源不是Slimefun方块: " + location);
            return false;
        }

        // 检查是否有有效的物品槽位
        BlockMenu sourceMenu = BlockStorage.getInventory(targetBlock);
        if (sourceMenu == null) {
//            Debug.logInfo("输入源没有有效的菜单: " + location);
            return false;
        }

        return true;
    }

    /**
     * 从源机器抽取物品到本机存储 - 修复版
     */
    private void transferFromSource(Block destBlock, SlimefunBlockData data, Location sourceLocation, int pairIndex) {
        Block sourceBlock = sourceLocation.getBlock();
        BlockMenu sourceMenu = BlockStorage.getInventory(sourceBlock);

        if (sourceMenu == null) {
//            Debug.logInfo("源机器菜单为null: " + sourceLocation);
            return;
        }

        // 调试信息:打印源机器信息
//        SlimefunItem sourceSfItem = BlockStorage.check(sourceBlock);
//        Debug.logInfo("尝试从源机器抽取: " + (sourceSfItem != null ? sourceSfItem.getId() : "未知") + " 位置: " + sourceLocation);
//
        // 方法1:先尝试获取输出槽
        int[] outputSlots = sourceMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
//        Debug.logInfo("方法1 - 输出槽数量: " + (outputSlots != null ? outputSlots.length : 0));

        if (outputSlots != null && outputSlots.length > 0) {
            // 从输出槽抽取
            extractFromOutputSlots(sourceMenu, outputSlots, data, pairIndex, destBlock);
            return;
        }

        // 方法2:如果没有明确的输出槽,尝试所有槽位(除了特定类型槽位)
//        Debug.logInfo("方法1失败,尝试方法2 - 扫描所有槽位");
//        extractFromAllSlots(sourceMenu, data, pairIndex, destBlock, sourceSfItem);
    }

    /**
     * 从输出槽抽取物品
     */
    private void extractFromOutputSlots(BlockMenu sourceMenu, int[] outputSlots, SlimefunBlockData destData, int pairIndex, Block destBlock) {
//        Debug.logInfo("开始从输出槽抽取,槽位: " + Arrays.toString(outputSlots));
        boolean hasExtracted = false;

        for (int outputSlot : outputSlots) {
            ItemStack itemToExtract = sourceMenu.getItemInSlot(outputSlot);
            SlimefunItem sfToExtract = SlimefunItem.getByItem(itemToExtract);
            if (sfToExtract instanceof CargoFragment) {
                continue;
            }
            if (itemToExtract == null || itemToExtract.getType() == Material.AIR) {
//                Debug.logInfo("槽位 " + outputSlot + " 为空");
                continue;
            }

//            Debug.logInfo("找到物品: " + itemToExtract.getType() + " 数量: " + itemToExtract.getAmount() + " 在槽位: " + outputSlot);

            // 检查过滤模板
            ItemStack filterTemplate = getInputFilterTemplate(destBlock, pairIndex);
            if (filterTemplate != null && !filterTemplate.getType().isAir()) {
                if (!isItemMatchFilter(itemToExtract, filterTemplate)) {
//                    Debug.logInfo("物品不匹配过滤模板,跳过");
                    continue;
                }
            }

            // 尝试抽取物品
            int extractedAmount = extractItemDirectly(sourceMenu, outputSlot, itemToExtract, destData);
//            Debug.logInfo("抽取结果: " + extractedAmount + " 个物品");

            if (extractedAmount > 0) {
                // 播放抽取音效
//                destBlock.getWorld().playSound(
//                        destBlock.getLocation(),
//                        Sound.ENTITY_ENDER_EYE_LAUNCH,
//                        0.2F,
//                        1.0F
//                );
                hasExtracted = true;
            }

        }
        if (hasExtracted) {
            // 显示粒子效果
            showTransferParticles(sourceMenu.getLocation(), destBlock.getLocation(), Particle.DRAGON_BREATH);
        }

    }


    /**
     * 直接抽取物品 - 修复版(使用更简单直接的方法)
     */
    private int extractItemDirectly(BlockMenu sourceMenu, int sourceSlot, ItemStack sourceItem, SlimefunBlockData destData) {
        if (sourceItem == null || sourceItem.getType() == Material.AIR) {
//            Debug.logInfo("源物品为空");
            return 0;
        }

        // 计算最大抽取数量(每次最多64个)
        int maxExtract = Math.min(sourceItem.getAmount(), 64);
//        Debug.logInfo("准备抽取 " + maxExtract + " 个物品");

        // 创建要抽取的物品
        ItemStack toExtract = sourceItem.clone();
        toExtract.setAmount(maxExtract);

        // 直接存储到目标存储系统
        storeItem(destData, toExtract);
//        Debug.logInfo("物品已存储到目标存储");

        // 更新源槽位:减少数量或清空
        int newAmount = sourceItem.getAmount() - maxExtract;
//        Debug.logInfo("源槽位新数量: " + newAmount);

        if (newAmount <= 0) {
            // 完全抽取完毕,清空槽位
            sourceMenu.replaceExistingItem(sourceSlot, null);
//            Debug.logInfo("清空源槽位");
        } else {
            // 更新数量
            ItemStack updatedItem = sourceItem.clone();
            updatedItem.setAmount(newAmount);
            sourceMenu.replaceExistingItem(sourceSlot, updatedItem);
//            Debug.logInfo("更新源槽位数量");
        }

//        Debug.logInfo("成功抽取 " + maxExtract + " 个 " + sourceItem.getType());
        return maxExtract;
    }

    /**
     * 检查物品是否匹配过滤模板
     */
    private boolean isItemMatchFilter(ItemStack item, ItemStack filter) {
        if (item == null || filter == null) return false;

        ItemStack itemCopy = item.clone();
        itemCopy.setAmount(1);
        ItemStack filterCopy = filter.clone();
        filterCopy.setAmount(1);

        boolean isSimilar = SameItemJudge.isSimilarSafe(itemCopy, filterCopy);
//        boolean isSimilar = SlimefunUtils.isItemSimilar(itemCopy, filterCopy, true);
//        Debug.logInfo("物品匹配检查: " + item.getType() + " vs " + filter.getType() + " = " + isSimilar);

        return isSimilar;
    }

    /**
     * 获取输入源位置
     */
    private Location getInputSourceLocation(Block block, int index) {
        String locStr = BlockStorage.getLocationInfo(block.getLocation()).getString("input_bind_pair_" + index);
        if (locStr == null || locStr.isEmpty()) {
//            Debug.logInfo("输入源绑定 " + index + " 为空");
            return null;
        }

        String[] parts = locStr.split(",");
        if (parts.length != 4) {
//            Debug.logInfo("输入源坐标格式错误: " + locStr);
            return null;
        }

        try {
            World world = Bukkit.getWorld(parts[3]);
            if (world == null) {
//                Debug.logInfo("世界不存在: " + parts[3]);
                return null;
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            Location location = new Location(world, x, y, z);
//            Debug.logInfo("解析输入源位置: " + location);
            return location;
        } catch (NumberFormatException e) {
//            Debug.logInfo("坐标解析错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取输入过滤模板
     */
    private ItemStack getInputFilterTemplate(Block block, int index) {
        Config config = BlockStorage.getLocationInfo(block.getLocation());
        String key = "input_filter_template_" + index;

        if (!config.contains(key)) {
//            Debug.logInfo("过滤模板 " + index + " 不存在");
            return null;
        }

        String base64 = config.getString(key);
        if (base64 == null || base64.isEmpty()) {
//            Debug.logInfo("过滤模板 " + index + " 数据为空");
            return null;
        }

        ItemStack template = SameItemJudge.itemFromBase64(base64);
//        Debug.logInfo("获取过滤模板 " + index + ": " + (template != null ? template.getType() : "null"));
        return template;
    }

    /**
     * 设置输入过滤模板
     */
    private void setInputFilterTemplate(Block block, int index, ItemStack item) {
        String key = "input_filter_template_" + index;

        if (item == null || item.getType().isAir()) {
            BlockStorage.addBlockInfo(block.getLocation(), key, "");
//            Debug.logInfo("清除过滤模板 " + index);
            return;
        }

        try {
            String base64 = SameItemJudge.itemToBase64(item);
            if (base64 != null && !base64.isEmpty()) {
                BlockStorage.addBlockInfo(block.getLocation(), key, base64);
//                Debug.logInfo("设置过滤模板 " + index + ": " + item.getType());
            } else {
//                Debug.logInfo("过滤模板序列化失败");
            }
        } catch (Exception e) {
//            Debug.logInfo("设置过滤模板失败: " + e.getMessage());
//            Debug.logInfo("设置输入过滤模板失败: " + e.getMessage());
        }
    }
    /**
     * 更新输入绑定界面
     */
    private void updateInputBindDisplay(BlockMenu menu, Block b) {
        menu.addItem(40, new CustomItemStack(new ItemStack (Material.PINK_STAINED_GLASS_PANE),
                        ColorGradient.getGradientName("← Send Items Outward"),
                        ColorGradient.getGradientName("← Maximum 256 Entries"),
                        ColorGradient.getGradientName("Extract Items Inward →"),
                        ColorGradient.getGradientName("Maximum 512 Coordinates →"),
                        ColorGradient.getGradientName("Click to open the Transfer Overview.")),
                (player, slot, itemStack, clickAction) -> {

                    openTransportOverviewMenu(player, b);; // 默认打开第一页
                    return false; // 不消耗物品或默认行为
                });
        int totalPages = Math.max(1, (MAX_INPUT_BIND_PAIRS + INPUT_PAIRS_PER_PAGE - 1) / INPUT_PAIRS_PER_PAGE);

        for (int i = 0; i < INPUT_PAIRS_PER_PAGE; i++) {
            int pairIndex = inputBindPage * INPUT_PAIRS_PER_PAGE + i;
            int bindSlot = INPUT_BIND_SLOTS[i];

            // ====== 输入源绑定显示 ======
            String binding = BlockStorage.getLocationInfo(b.getLocation(), "input_bind_pair_" + pairIndex);
            boolean hasBinding = binding != null && !binding.isEmpty();

            ItemStack bindItem;
            if (hasBinding) {
                Location loc = parseLocation(binding);
                SlimefunItem sfItem = BlockStorage.check(loc);
                if (sfItem != null) {
                    bindItem = sfItem.getItem().clone();
                } else {
                    Block block = loc.getBlock();
                    if (block.getType() != Material.AIR) {
                        // 安全地创建物品堆栈
                        try {
                            Material blockType = block.getType();
                            // 检查是否为有效的物品类型
                            if (isValidItemType(blockType)) {
                                bindItem = new ItemStack(blockType);
                                // 现移除在主线程获取blockstate的操作    用处不大
                                // 🔹 同步获取 BlockState - 必须同步
//                                if (Bukkit.isPrimaryThread()) {
//                                    BlockState state = block.getState();
//                                    ItemMeta meta = bindItem.getItemMeta();
//                                    if (meta instanceof BlockStateMeta bsm) {
//                                        bsm.setBlockState(state);
//                                        bindItem.setItemMeta(bsm);
//                                    }
//                                } else {
//                                    // 如果在异步线程,使用同步方式获取
//                                    ItemStack finalBindItem = bindItem;
//                                    Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
//                                        try {
//                                            BlockState state = block.getState();
//                                            ItemMeta meta = finalBindItem.getItemMeta();
//                                            if (meta instanceof BlockStateMeta bsm) {
//                                                bsm.setBlockState(state);
//                                                finalBindItem.setItemMeta(bsm);
//                                            }
//                                        } catch (IllegalStateException e) {
//                                            // 如果仍然失败,记录错误并使用默认方式
//                                            MagicExpansion.getInstance().getLogger().warning(
//                                                    "无法获取方块状态在位置: " + block.getLocation() +
//                                                            ", 类型: " + block.getType()
//                                            );
//                                        }
//                                    });
//                                }
                            } else {
                                // 对于墙上的标志等非物品方块,使用安全的替代品
                                bindItem = getSafeAlternativeItem(blockType);
                            }
                        } catch (IllegalArgumentException e) {
                            // 如果创建失败,使用默认的安全物品
                            bindItem = new ItemStack(Material.COMPASS);
                        }
                    } else {
                        bindItem = new ItemStack(Material.COMPASS);
                    }
                }
            } else {
                bindItem = new ItemStack(Material.RECOVERY_COMPASS);
            }


            ItemMeta bindMeta = bindItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ItemStackHelper.getDisplayName(bindItem));
            if (bindMeta != null && bindMeta.hasLore()) {
                lore.addAll(bindMeta.getLore());
            }
            lore.add("");

            if (hasBinding) {
                String[] parts = binding.split(",", 4);
                lore.add("§aInput Source Coordinates: §e" + parts[0] + ", " + parts[1] + ", " + parts[2]);
                lore.add("§aWorld: §e" + parts[3]);
                Location loc = parseLocation(binding);
                BlockMenu sourceMenu = StorageCacheUtils.getMenu(loc);
                if (sourceMenu != null) {
                    int[] outputSlots = sourceMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                    lore.add("§bOutput Slot Count: §e" + (outputSlots != null ? outputSlots.length : 0));
                }
                // 显示过滤模板信息
                ItemStack filterTemplate = getInputFilterTemplate(b, pairIndex);
                if (filterTemplate != null && !filterTemplate.getType().isAir()) {
                    lore.add("§6Filter Template: §e" + ItemStackHelper.getDisplayName(filterTemplate));
                } else {
                    lore.add("§7No filter template (extract all items)");
                }
            } else {
                lore.add("§7No input source bound");
            }

            lore.add("");
            lore.add("§fLeft-click: Hold Void Touch and click a block to bind it.");
            lore.add("§fRight-click: Clear binding");
            lore.add("§fShift-left-click: Set filter template");
            lore.add("§fShift-right-click: Clear filter template");

            if (bindMeta == null) {
                bindMeta = Bukkit.getItemFactory().getItemMeta(bindItem.getType());
            }
            bindMeta.setDisplayName("§aInput Source Binding #" + (pairIndex + 1));
            bindMeta.setLore(lore);
            bindItem.setItemMeta(bindMeta);

            menu.addItem(bindSlot, bindItem, (player, slot, clickedItem, action) -> {
                // 🔹 右键:清除绑定
                if (action.isRightClicked() && !action.isShiftClicked()) {
                    BlockStorage.addBlockInfo(b.getLocation(), "input_bind_pair_" + pairIndex, "");
                    setInputFilterTemplate(b, pairIndex, null); // 同时清除过滤模板
                    updateInputBindDisplay(menu, b);
                    player.sendMessage("§aCleared input source binding #" + (pairIndex + 1));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5F, 0.5F);
                    return false;
                }

                // 🔹 Shift+右键:清除过滤模板
                if (action.isRightClicked() && action.isShiftClicked()) {
                    setInputFilterTemplate(b, pairIndex, null);
                    updateInputBindDisplay(menu, b);
                    player.sendMessage("§aCleared filter template #" + (pairIndex + 1));
                    player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.3F, 0.5F);
                    return false;
                }

                // 🔹 Shift+左键:设置过滤模板
                if (action.isShiftClicked() && !action.isRightClicked()) {
                    ItemStack cursor = player.getItemOnCursor();
                    if (cursor == null || cursor.getType().isAir()) {
                        player.sendMessage("§cPlace the item to use as the filter template on your cursor!");
                        return false;
                    }

                    ItemStack newTemplate = cursor.clone();
                    newTemplate.setAmount(1);
                    setInputFilterTemplate(b, pairIndex, newTemplate);

                    updateInputBindDisplay(menu, b);
                    String name = newTemplate.hasItemMeta() && newTemplate.getItemMeta().hasDisplayName() ?
                            newTemplate.getItemMeta().getDisplayName() : newTemplate.getType().name().toLowerCase().replace('_', ' ');

                    player.sendMessage("§aFilter template set: §e" + name);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.8F);
                    return false;
                }

                // 🔹 左键:绑定输入源(必须光标上有 VoidTouch)
                if (!action.isShiftClicked() && !action.isRightClicked()) {
                    ItemStack cursor = player.getItemOnCursor();
                    if (cursor == null || cursor.getType().isAir()) {
                        player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                        return false;
                    }

                    SlimefunItem sfItem = SlimefunItem.getByItem(cursor);
                    if (!(sfItem instanceof VoidTouch)) {
                        player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                        return false;
                    }

                    ItemMeta meta = cursor.getItemMeta();
                    if (meta == null) {
                        player.sendMessage("§cThis item is missing metadata and its binding cannot be read.");
                        return false;
                    }

                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    NamespacedKey keyX = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
                    NamespacedKey keyY = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
                    NamespacedKey keyZ = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
                    NamespacedKey keyWorld = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

                    if (!container.has(keyX, PersistentDataType.INTEGER) ||
                            !container.has(keyY, PersistentDataType.INTEGER) ||
                            !container.has(keyZ, PersistentDataType.INTEGER) ||
                            !container.has(keyWorld, PersistentDataType.STRING)) {
                        player.sendMessage("§cError: §dVoid Touch §cis not bound to any coordinates!");
                        return false;
                    }

                    int x = container.get(keyX, PersistentDataType.INTEGER);
                    int y = container.get(keyY, PersistentDataType.INTEGER);
                    int z = container.get(keyZ, PersistentDataType.INTEGER);
                    String worldName = container.get(keyWorld, PersistentDataType.STRING);

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        player.sendMessage("§cThe bound world §e" + worldName + " §cdoes not exist.");
                        return false;
                    }

                    // 验证目标是否为Slimefun机器
                    Location targetLoc = new Location(world, x, y, z);
                    if (BlockStorage.check(targetLoc.getBlock()) == null) {
                        player.sendMessage("§cThe target is not a Slimefun machine and cannot be used as an input source!");
                        return false;
                    }

                    // ✅ 保存绑定
                    String newValue = x + "," + y + "," + z + "," + worldName;
                    BlockStorage.addBlockInfo(b.getLocation(), "input_bind_pair_" + pairIndex, newValue);

                    // ✅ 更新界面
                    updateInputBindDisplay(menu, b);

                    // ✅ 反馈
                    player.sendMessage("§aSuccessfully bound input source §e#" + (pairIndex + 1));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 0.5F, 1.0F);

                    return false;
                }

                return false;
            });
        }

        // === 输入绑定翻页按钮 ===
        updateInputBindPageButtons(menu, b, totalPages);
    }
    /**
     * 更新输入绑定翻页按钮
     */
    private void updateInputBindPageButtons(BlockMenu menu, Block b, int totalPages) {
        // 上一页按钮(使用52槽位)
        ItemStack prevItem = new ItemStack(inputBindPage > 0 ? Material.ARROW : Material.BARRIER);
        ItemMeta prevMeta = prevItem.getItemMeta();
        prevMeta.setDisplayName((inputBindPage > 0 ? "§aPrevious Page" : "§cAlready on the first page")+" (Extract Items from External Machine)");
        prevMeta.setLore(List.of("§7Page: " + (inputBindPage + 1) + "/" + totalPages));
        prevItem.setItemMeta(prevMeta);

        menu.addItem(52, prevItem, (player, slot, item, action) -> {
            if (inputBindPage > 0) {
                inputBindPage--;
                updateInputBindDisplay(menu, b);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.3F, 0.6F);
            }
            return false;
        });

        // 下一页按钮(使用53槽位)
        ItemStack nextItem = new ItemStack(inputBindPage < totalPages - 1 ? Material.SPECTRAL_ARROW : Material.BARRIER);
        ItemMeta nextMeta = nextItem.getItemMeta();
        nextMeta.setDisplayName((inputBindPage < totalPages - 1 ? "§aNext Page" : "§cAlready on the last page")+" (Extract Items from External Machine)");
        nextMeta.setLore(List.of("§7Page: " + (inputBindPage + 1) + "/" + totalPages));
        nextItem.setItemMeta(nextMeta);

        menu.addItem(53, nextItem, (player, slot, item, action) -> {
            if (inputBindPage < totalPages - 1) {
                inputBindPage++;
                updateInputBindDisplay(menu, b);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.3F, 0.6F);
            }
            return false;
        });
    }
















    /*
    传输二级菜单
     */
    private final int[] transportSlotsStep = {0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30, 36,37,38,39}; // 跳转菜单用于展示向外输入的,和transportSlots存的东西一样,只不过更大了
    private final int[] transportSlots2Step = {5,6,7,8, 14,15,16,17, 23,24,25,26, 32,33,34,35, 41,42,43,44}; // 跳转菜单用于展示从外面抽取物品,和transportSlots存的东西一样,只不过更大了
    private final int[] arrowSlot2Step = {45,48, 50,53};
    private final int[] pinkGlassPane2Step = {4,13,22,31,40,49, 46,47, 51,52};
    // 传输菜单页码变量
    private int transportOutputPage = 0;
    private int transportInputPage = 0;

    /**
     * 打开传输总览菜单
     */
    private void openTransportOverviewMenu(Player player, Block b) {
        ChestMenu menu = new ChestMenu("§6§lTransfer Overview Menu");
        menu.setSize(9 * 6);
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(true);
        menu.addMenuCloseHandler(p -> {
            ItemStack cursorItem = p.getItemOnCursor();
            if (cursorItem != null && !cursorItem.getType().isAir()) {
                // 尝试将物品放回背包
                HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(cursorItem);
                // 如果背包满了,掉落物品
                for (ItemStack item : leftover.values()) {
                    p.getWorld().dropItem(p.getLocation(), item);
                }
                // 清空光标
                p.setItemOnCursor(new ItemStack(Material.AIR));
            }
        });


        // 初始化菜单
        refreshTransportOverviewMenu(menu, b, 0, 0);
        menu.open(player);
    }

    /**
     * 刷新传输总览菜单
     */
    private void refreshTransportOverviewMenu(ChestMenu menu, Block b, int outputPage, int inputPage) {
        // 清空菜单
        for (int i = 0; i < 54; i++) {
            menu.replaceExistingItem(i, null);
        }

        // 设置装饰玻璃板
        ItemStack pinkPane = new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, "§dTransfer Overview");
        for (int slot : pinkGlassPane2Step) {
            menu.addItem(slot, pinkPane, (p, s, item, action) -> false);
        }

        // 设置标题
        menu.addItem(4, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE,
                "§6§lOutbound Transfer Configuration", "§7Each pair is one entry: coordinates + template"), (p, s, item, action) -> false);

        menu.addItem(49, new CustomItemStack(Material.NETHER_STAR,
                "§e§lTransfer Overview",
                "§aLeft: §eOutbound configuration (coordinates + template)",
                "§aRight: §eInbound configuration (coordinates + filter)",
                "§7Click to refresh the menu."), (p, s, item, action) -> {
            refreshTransportOverviewMenu(menu, b, outputPage, inputPage);
            return false;
        });

        // 刷新输出配置区域(每2个一组)
        refreshOutputOverview(menu, b, outputPage);

        // 刷新输入配置区域
        refreshInputOverview(menu, b, inputPage);

        // 设置翻页按钮
        setupTransportPagination(menu, b, outputPage, inputPage);
    }

    /**
     * 刷新输出配置区域(每2个为一组:坐标槽+模板槽)
     */
    private void refreshOutputOverview(ChestMenu menu, Block b, int page) {
        int pairsPerPage = transportSlotsStep.length / 2; // 每页10组
        int startIndex = page * pairsPerPage;

        for (int i = 0; i < pairsPerPage; i++) {
            int pairIndex = startIndex + i;

            if (pairIndex >= MAX_BIND_PAIRS) {
                // 超出范围,显示空位
                int coordSlot = transportSlotsStep[i * 2];
                int templateSlot = transportSlotsStep[i * 2 + 1];

                menu.addItem(coordSlot, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "§7Empty Slot"),
                        (p, s, item, action) -> false);
                menu.addItem(templateSlot, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "§7Empty Slot"),
                        (p, s, item, action) -> false);
                continue;
            }

            // 坐标槽位(每个组的第一个槽)
            int coordSlot = transportSlotsStep[i * 2];
            setupOutputCoordSlot(menu, b, pairIndex, coordSlot);

            // 模板槽位(每个组的第二个槽)
            int templateSlot = transportSlotsStep[i * 2 + 1];
            setupOutputTemplateSlot(menu, b, pairIndex, templateSlot);
        }
    }

    /**
     * 设置输出配置的坐标槽位
     */
    private void setupOutputCoordSlot(ChestMenu menu, Block b, int pairIndex, int slot) {
        String binding = BlockStorage.getLocationInfo(b.getLocation(), "output_bind_pair_" + pairIndex);

        ItemStack coordItem;
        List<String> lore = new ArrayList<>();

        if (binding != null && !binding.isEmpty()) {
            Location loc = parseLocation(binding);
            if (loc != null) {
                SlimefunItem sfItem = BlockStorage.check(loc);
                if (sfItem != null) {
                    coordItem = sfItem.getItem().clone();
                } else {
                    Block block = loc.getBlock();
                    if (block != null && block.getType() != Material.AIR) {
                        try {
                            Material blockType = block.getType();
                            // 检查是否为有效的物品类型
                            if (isValidItemType(blockType)) {
                                coordItem = new ItemStack(blockType);
                            } else {
                                // 对于墙上的标志等非物品方块,使用安全的替代品
                                coordItem = getSafeAlternativeItem(blockType);
                            }
                        } catch (IllegalArgumentException e) {
                            // 如果创建失败,使用默认的安全物品
                            coordItem = new ItemStack(Material.COMPASS);
                        }
                          // 现移除在主线程获取blockstate的操作    用处不大
//                        // 🔹 同步获取方块信息
//                        if (Bukkit.isPrimaryThread()) {
//                            try {
//                                BlockState state = block.getState();
//                                if (state instanceof BlockData) {
//                                    ItemMeta meta = coordItem.getItemMeta();
//                                    if (meta instanceof BlockStateMeta bsm) {
//                                        bsm.setBlockState(state);
//                                        coordItem.setItemMeta(bsm);
//                                    }
//                                }
//                            } catch (Exception e) {
//                                // 如果失败,至少显示基本类型
//                            }
//                        } else {
//                            // 如果在异步线程,使用同步方式获取
//                            ItemStack finalCoordItem = coordItem;
//                            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
//                                try {
//                                    BlockState state = block.getState();
//                                    if (state instanceof BlockData) {
//                                        ItemMeta meta = finalCoordItem.getItemMeta();
//                                        if (meta instanceof BlockStateMeta bsm) {
//                                            bsm.setBlockState(state);
//                                            finalCoordItem.setItemMeta(bsm);
//                                        }
//                                    }
//                                } catch (IllegalStateException e) {
//                                    // 如果仍然失败,记录错误
//                                    MagicExpansion.getInstance().getLogger().warning(
//                                            "无法获取方块状态在位置: " + block.getLocation() +
//                                                    ", 类型: " + block.getType()
//                                    );
//                                }
//                            });
//                        }
                    } else {
                        coordItem = new ItemStack(Material.COMPASS);
                    }
                }

                lore.add("§6Template Name: §e" + ItemStackHelper.getDisplayName(coordItem));
                ItemMeta coordItemMeta = coordItem.getItemMeta();
                if (coordItemMeta != null && coordItemMeta.hasLore()) {
                    lore.addAll(coordItemMeta.getLore());
                }

                lore.add("§aTarget Coordinates: §e" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                lore.add("§aWorld: §e" + loc.getWorld().getName());
            } else {
                coordItem = new ItemStack(Material.BARRIER);
                lore.add("§cCoordinate parsing error");
            }
        } else {
            coordItem = new ItemStack(Material.RECOVERY_COMPASS);
            lore.add("§7No target bound");
        }

        lore.add("");
        lore.add("§fLeft-click: Set target coordinates");
        lore.add("§fRight-click: Clear target coordinates");

        ItemMeta meta = coordItem.getItemMeta();
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(coordItem.getType());
        meta.setDisplayName("§eTarget Coordinates #" + (pairIndex + 1));
        meta.setLore(lore);
        coordItem.setItemMeta(meta);
        coordItem.setAmount(1);

        final int finalPairIndex = pairIndex;
        menu.addItem(slot, coordItem, (player, clickedSlot, clickedItem, action) -> {
            handleOutputCoordClick(player, b, finalPairIndex, action, menu);
            return false;
        });
    }

    /**
     * 设置输出配置的模板槽位
     */
    private void setupOutputTemplateSlot(ChestMenu menu, Block b, int pairIndex, int slot) {
        ItemStack template = getItemTemplate(b, pairIndex);
        int amount = getTransferAmount(b, pairIndex);

        ItemStack templateItem;
        List<String> lore = new ArrayList<>();

        if (template != null && !template.getType().isAir()) {
            templateItem = template.clone();
            lore.add("§6Template Name: §e" + ItemStackHelper.getDisplayName(template));
            ItemMeta tempMeta = template.getItemMeta();
            if (tempMeta != null && tempMeta.hasLore()) {
                lore.addAll(tempMeta.getLore());
            }
            lore.add("§6Amount: §e" + amount);

            // 显示当前库存
            SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
            if (data != null) {
                long stock = getStoredItemCountFromMainStorage(data, template);
                lore.add("§bStock: §e" + stock);
            }
        } else {
            templateItem = new ItemStack(Material.GRAY_DYE);
            lore.add("§7No item template set");
        }

        lore.add("");
        lore.add("§fLeft-click: Set item template");
        lore.add("§fRight-click: Clear item template");
        lore.add("§fShift-left-click: Change transfer amount");

        ItemMeta meta = templateItem.getItemMeta();
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(templateItem.getType());
        meta.setDisplayName("§6Item Template #" + (pairIndex + 1));
        meta.setLore(lore);
        templateItem.setItemMeta(meta);
        templateItem.setAmount(1);

        final int finalPairIndex = pairIndex;
        menu.addItem(slot, templateItem, (player, clickedSlot, clickedItem, action) -> {
            handleOutputTemplateClick(player, b, finalPairIndex, action, menu);
            return false;
        });
    }

    /**
     * 处理输出配置坐标槽点击事件
     */
    private void handleOutputCoordClick(Player player, Block b, int pairIndex, ClickAction action, ChestMenu menu) {
        if (action.isRightClicked()) {
            // 右键: 清除目标绑定
            BlockStorage.addBlockInfo(b.getLocation(), "output_bind_pair_" + pairIndex, "");
            player.sendMessage("§aCleared output target #" + (pairIndex + 1));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5F, 0.5F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);

        } else if (!action.isRightClicked() && !action.isShiftClicked()) {
            // 左键: 设置目标绑定(需要虚空之触)
            ItemStack cursor = player.getItemOnCursor();
            if (cursor == null || cursor.getType().isAir()) {
                player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                return;
            }

            SlimefunItem sfItem = SlimefunItem.getByItem(cursor);
            if (!(sfItem instanceof VoidTouch)) {
                player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                return;
            }

            ItemMeta meta = cursor.getItemMeta();
            if (meta == null) {
                player.sendMessage("§cThis item is missing metadata and its binding cannot be read.");
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey keyX = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
            NamespacedKey keyY = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
            NamespacedKey keyZ = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
            NamespacedKey keyWorld = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

            if (!container.has(keyX, PersistentDataType.INTEGER) ||
                    !container.has(keyY, PersistentDataType.INTEGER) ||
                    !container.has(keyZ, PersistentDataType.INTEGER) ||
                    !container.has(keyWorld, PersistentDataType.STRING)) {
                player.sendMessage("§cError: §dVoid Touch §cis not bound to any coordinates!");
                return;
            }

            int x = container.get(keyX, PersistentDataType.INTEGER);
            int y = container.get(keyY, PersistentDataType.INTEGER);
            int z = container.get(keyZ, PersistentDataType.INTEGER);
            String worldName = container.get(keyWorld, PersistentDataType.STRING);

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage("§cThe bound world §e" + worldName + " §cdoes not exist.");
                return;
            }

            // 保存绑定
            String newValue = x + "," + y + "," + z + "," + worldName;
            BlockStorage.addBlockInfo(b.getLocation(), "output_bind_pair_" + pairIndex, newValue);

            player.sendMessage("§aSuccessfully bound output target §e#" + (pairIndex + 1));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 0.5F, 1.0F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);
        }
    }

    /**
     * 处理输出配置模板槽点击事件
     */
    private void handleOutputTemplateClick(Player player, Block b, int pairIndex, ClickAction action, ChestMenu menu) {
        if (action.isShiftClicked() && !action.isRightClicked()) {
            // Shift+左键: 切换传输数量
            ItemStack currentTemplate = getItemTemplate(b, pairIndex);
            if (currentTemplate == null || currentTemplate.getType().isAir()) {
                player.sendMessage("§cSet an item template first!");
                return;
            }

            int current = getTransferAmount(b, pairIndex);
            int nextIdx = 0;
            for (int j = 0; j < TRANSFER_AMOUNTS.length; j++) {
                if (current == TRANSFER_AMOUNTS[j]) {
                    nextIdx = (j + 1) % TRANSFER_AMOUNTS.length;
                    break;
                }
            }
            int newAmount = TRANSFER_AMOUNTS[nextIdx];
            setTransferAmount(b, pairIndex, newAmount);

            player.sendMessage("§eTransfer Amount: §6" + newAmount);
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.3F, 1.0F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);

        } else if (action.isRightClicked() && !action.isShiftClicked()) {
            // 右键: 清除模板
            setItemTemplate(b, pairIndex, null);
            player.sendMessage("§aCleared output template #" + (pairIndex + 1));
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.3F, 0.5F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);

        } else if (!action.isRightClicked() && !action.isShiftClicked()) {
            // 左键: 设置模板
            ItemStack cursor = player.getItemOnCursor();
            if (cursor == null || cursor.getType().isAir()) {
                player.sendMessage("§cPlace the item to use as the template on your cursor!");
                return;
            }

            ItemStack newTemplate = cursor.clone();
            newTemplate.setAmount(1);
            setItemTemplate(b, pairIndex, newTemplate);

            // 如果是第一次设置模板,设置默认数量为1
            if (getTransferAmount(b, pairIndex) <= 0) {
                setTransferAmount(b, pairIndex, 1);
            }

            player.sendMessage("§aOutput template set: §e" + ItemStackHelper.getDisplayName(newTemplate));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.8F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);
        }
    }

    /**
     * 刷新输入配置区域(每个槽位独立)
     */
    private void refreshInputOverview(ChestMenu menu, Block b, int page) {
        int itemsPerPage = transportSlots2Step.length;
        int startIndex = page * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int pairIndex = startIndex + i;
            int displaySlot = transportSlots2Step[i];

            if (pairIndex >= MAX_INPUT_BIND_PAIRS) {
                // 超出范围,显示空位
                menu.addItem(displaySlot, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "§7Empty Slot"),
                        (p, s, item, action) -> false);
                continue;
            }

            // 获取绑定信息
            String binding = BlockStorage.getLocationInfo(b.getLocation(), "input_bind_pair_" + pairIndex);
            ItemStack filterTemplate = getInputFilterTemplate(b, pairIndex);

            ItemStack displayItem;
            List<String> lore = new ArrayList<>();

            if (binding != null && !binding.isEmpty()) {
                // 有绑定的情况
                Location loc = parseLocation(binding);
                if (loc != null) {
                    SlimefunItem sfItem = BlockStorage.check(loc);
                    if (sfItem != null) {
                        displayItem = sfItem.getItem().clone();
                    } else {
                        Block block = loc.getBlock();
                        if (block != null && block.getType() != Material.AIR) {
                            try {
                                Material blockType = block.getType();
                                // 检查是否为有效的物品类型
                                if (isValidItemType(blockType)) {
                                    displayItem = new ItemStack(blockType);
                                } else {
                                    // 对于墙上的标志等非物品方块,使用安全的替代品
                                    displayItem = getSafeAlternativeItem(blockType);
                                }
                            } catch (IllegalArgumentException e) {
                                // 如果创建失败,使用默认的安全物品
                                displayItem = new ItemStack(Material.COMPASS);
                            }
                        }else {
                            displayItem = new ItemStack(Material.COMPASS);
                        }
                        // 现移除在主线程获取blockstate的操作    用处不大
                        // 🔹 同步获取方块信息
//                        if (Bukkit.isPrimaryThread()) {
//                            try {
//                                BlockState state = loc.getBlock().getState();
//                                ItemMeta meta = displayItem.getItemMeta();
//                                if (meta instanceof BlockStateMeta bsm) {
//                                    bsm.setBlockState(state);
//                                    displayItem.setItemMeta(bsm);
//                                }
//                            } catch (Exception e) {
//                                // 如果失败,至少显示基本类型
//                            }
//                        } else {
//                            // 如果在异步线程,使用同步方式获取
//                            ItemStack finalDisplayItem = displayItem;
//                            Block finalBlock = loc.getBlock();
//                            Bukkit.getScheduler().runTask(MagicExpansion.getInstance(), () -> {
//                                try {
//                                    BlockState state = finalBlock.getState();
//                                    ItemMeta meta = finalDisplayItem.getItemMeta();
//                                    if (meta instanceof BlockStateMeta bsm) {
//                                        bsm.setBlockState(state);
//                                        finalDisplayItem.setItemMeta(bsm);
//                                    }
//                                } catch (IllegalStateException e) {
//                                    // 如果仍然失败,记录错误
//                                    MagicExpansion.getInstance().getLogger().warning(
//                                            "无法获取方块状态在位置: " + finalBlock.getLocation() +
//                                                    ", 类型: " + finalBlock.getType()
//                                    );
//                                }
//                            });
//                        }
                    }

                    lore.add(ItemStackHelper.getDisplayName(displayItem));
                    ItemMeta cdisplayItemMeta = displayItem.getItemMeta();
                    if (cdisplayItemMeta != null && cdisplayItemMeta.hasLore()) {
                        lore.addAll(cdisplayItemMeta.getLore());
                    }

                    lore.add("§aInput Source Coordinates: §e" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                    lore.add("§aWorld: §e" + loc.getWorld().getName());

                    // 显示源机器状态
//                    Block sourceBlock = loc.getBlock();
//                    BlockMenu sourceMenu = BlockStorage.getInventory(sourceBlock);
                    BlockMenu sourceMenu = StorageCacheUtils.getMenu(loc);
                    if (sourceMenu != null) {
                        int[] outputSlots = sourceMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                        lore.add("§bOutput Slot Count: §e" + (outputSlots != null ? outputSlots.length : 0));
                    }
                } else {
                    displayItem = new ItemStack(Material.BARRIER);
                    lore.add("§cCoordinate parsing error");
                }
            } else {
                displayItem = new ItemStack(Material.RECOVERY_COMPASS);
                lore.add("§7No input source bound");
            }

            if (filterTemplate != null && !filterTemplate.getType().isAir()) {
                lore.add("§6Filter Template: §e" + ItemStackHelper.getDisplayName(filterTemplate));
            } else {
                lore.add("§7No filter template");
            }

            lore.add("");
            lore.add("§fLeft-click: Set input source");
            lore.add("§fRight-click: Clear input source");
            lore.add("§fShift-left-click: Set filter template");
            lore.add("§fShift-right-click: Clear filter template");

            ItemMeta meta = displayItem.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
            meta.setDisplayName("§aInput Configuration #" + (pairIndex + 1));
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            displayItem.setAmount(1);

            final int finalPairIndex = pairIndex;
            menu.addItem(displaySlot, displayItem, (player, slot, clickedItem, action) -> {
                handleInputConfigClick(player, b, finalPairIndex, action, menu);
                return false;
            });
        }
    }

    /**
     * 处理输入配置点击事件
     */
    private void handleInputConfigClick(Player player, Block b, int pairIndex, ClickAction action, ChestMenu menu) {
        if (action.isShiftClicked() && action.isRightClicked()) {
            // Shift+右键: 清除过滤模板
            setInputFilterTemplate(b, pairIndex, null);
            player.sendMessage("§aCleared input filter #" + (pairIndex + 1));
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.3F, 0.5F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);

        } else if (action.isShiftClicked() && !action.isRightClicked()) {
            // Shift+左键: 设置过滤模板
            ItemStack cursor = player.getItemOnCursor();
            if (cursor == null || cursor.getType().isAir()) {
                player.sendMessage("§cPlace the item to use as the filter template on your cursor!");
                return;
            }

            ItemStack newFilter = cursor.clone();
            newFilter.setAmount(1);
            setInputFilterTemplate(b, pairIndex, newFilter);

            player.sendMessage("§aInput filter set: §e" + ItemStackHelper.getDisplayName(newFilter));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.8F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);

        } else if (action.isRightClicked() && !action.isShiftClicked()) {
            // 右键: 清除输入源绑定
            BlockStorage.addBlockInfo(b.getLocation(), "input_bind_pair_" + pairIndex, "");
            setInputFilterTemplate(b, pairIndex, null); // 同时清除过滤
            player.sendMessage("§aCleared input source #" + (pairIndex + 1));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.5F, 0.5F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);

        } else if (!action.isRightClicked() && !action.isShiftClicked()) {
            // 左键: 设置输入源绑定(需要虚空之触)
            ItemStack cursor = player.getItemOnCursor();
            if (cursor == null || cursor.getType().isAir()) {
                player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                return;
            }

            SlimefunItem sfItem = SlimefunItem.getByItem(cursor);
            if (!(sfItem instanceof VoidTouch)) {
                player.sendMessage("§cPlace §dVoid Touch §con your cursor to bind a location!");
                return;
            }

            ItemMeta meta = cursor.getItemMeta();
            if (meta == null) {
                player.sendMessage("§cThis item is missing metadata and its binding cannot be read.");
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey keyX = new NamespacedKey(MagicExpansion.getInstance(), "touch_x");
            NamespacedKey keyY = new NamespacedKey(MagicExpansion.getInstance(), "touch_y");
            NamespacedKey keyZ = new NamespacedKey(MagicExpansion.getInstance(), "touch_z");
            NamespacedKey keyWorld = new NamespacedKey(MagicExpansion.getInstance(), "touch_world");

            if (!container.has(keyX, PersistentDataType.INTEGER) ||
                    !container.has(keyY, PersistentDataType.INTEGER) ||
                    !container.has(keyZ, PersistentDataType.INTEGER) ||
                    !container.has(keyWorld, PersistentDataType.STRING)) {
                player.sendMessage("§cError: §dVoid Touch §cis not bound to any coordinates!");
                return;
            }

            int x = container.get(keyX, PersistentDataType.INTEGER);
            int y = container.get(keyY, PersistentDataType.INTEGER);
            int z = container.get(keyZ, PersistentDataType.INTEGER);
            String worldName = container.get(keyWorld, PersistentDataType.STRING);

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage("§cThe bound world §e" + worldName + " §cdoes not exist.");
                return;
            }

            // 验证目标是否为Slimefun机器
            Location targetLoc = new Location(world, x, y, z);
            if (BlockStorage.check(targetLoc.getBlock()) == null) {
                player.sendMessage("§cThe target is not a Slimefun machine and cannot be used as an input source!");
                return;
            }

            // 保存绑定
            String newValue = x + "," + y + "," + z + "," + worldName;
            BlockStorage.addBlockInfo(b.getLocation(), "input_bind_pair_" + pairIndex, newValue);

            player.sendMessage("§aSuccessfully bound input source §e#" + (pairIndex + 1));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 0.5F, 1.0F);
            refreshTransportOverviewMenu(menu, b, transportOutputPage, transportInputPage);
        }
    }

    /**
     * 设置传输菜单翻页按钮
     */
    private void setupTransportPagination(ChestMenu menu, Block b, int outputPage, int inputPage) {
        // 保存当前页码
        transportOutputPage = outputPage;
        transportInputPage = inputPage;

        // 输出配置翻页
        int outputTotalPages = (int) Math.ceil((double) MAX_BIND_PAIRS / (transportSlotsStep.length / 2));

        // 上一页按钮 (45)
        if (outputPage > 0) {
            menu.addItem(45, new CustomItemStack(Material.ARROW, "§aPrevious Output Page", "§7Page: " + (outputPage + 1) + "/" + outputTotalPages),
                    (p, s, item, action) -> {
                        refreshTransportOverviewMenu(menu, b, outputPage - 1, inputPage);
                        return false;
                    });
        } else {
            menu.addItem(45, new CustomItemStack(Material.BARRIER, "§cFirst Output Page", "§7Page: " + (outputPage + 1) + "/" + outputTotalPages),
                    (p, s, item, action) -> false);
        }

        // 下一页按钮 (48)
        if (outputPage < outputTotalPages - 1) {
            menu.addItem(48, new CustomItemStack(Material.ARROW, "§aNext Output Page", "§7Page: " + (outputPage + 1) + "/" + outputTotalPages),
                    (p, s, item, action) -> {
                        refreshTransportOverviewMenu(menu, b, outputPage + 1, inputPage);
                        return false;
                    });
        } else {
            menu.addItem(48, new CustomItemStack(Material.BARRIER, "§cLast Output Page", "§7Page: " + (outputPage + 1) + "/" + outputTotalPages),
                    (p, s, item, action) -> false);
        }

        // 输入配置翻页
        int inputTotalPages = (int) Math.ceil((double) MAX_INPUT_BIND_PAIRS / transportSlots2Step.length);

        // 上一页按钮 (50)
        if (inputPage > 0) {
            menu.addItem(50, new CustomItemStack(Material.ARROW, "§aPrevious Input Page", "§7Page: " + (inputPage + 1) + "/" + inputTotalPages),
                    (p, s, item, action) -> {
                        refreshTransportOverviewMenu(menu, b, outputPage, inputPage - 1);
                        return false;
                    });
        } else {
            menu.addItem(50, new CustomItemStack(Material.BARRIER, "§cFirst Input Page", "§7Page: " + (inputPage + 1) + "/" + inputTotalPages),
                    (p, s, item, action) -> false);
        }

        // 下一页按钮 (53)
        if (inputPage < inputTotalPages - 1) {
            menu.addItem(53, new CustomItemStack(Material.ARROW, "§aNext Input Page", "§7Page: " + (inputPage + 1) + "/" + inputTotalPages),
                    (p, s, item, action) -> {
                        refreshTransportOverviewMenu(menu, b, outputPage, inputPage + 1);
                        return false;
                    });
        } else {
            menu.addItem(53, new CustomItemStack(Material.BARRIER, "§cLast Input Page", "§7Page: " + (inputPage + 1) + "/" + inputTotalPages),
                    (p, s, item, action) -> false);
        }
    }


























}
