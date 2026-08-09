package io.Yomicer.magicExpansion.utils;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.Yomicer.magicExpansion.utils.SameItemJudge.itemFromBase64;

/**
 * 网络量子存储统一封装类。
 * <p>
 * 整合 Networks / NetworksExpansion 插件中与量子存储相关的全部代码：
 * NamespacedKey、PDC 读写、量子存储数据类型、QuantumCache，
 * 并提供带"最大值保护 / 数据溢出保护"的存入方法，以及生态缸输出格剩余容量计算。
 */
public final class NetworkStorage {

    /** NetworksExpansion 量子存储方块类名（反射使用，避免硬依赖） */
    private static final String QUANTUM_STORAGE_BLOCK_CLASS =
            "io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage";

    private NetworkStorage() {
    }

    // ==================== NamespacedKeys（兼容 官方 / 中文 / 新版 三种 key） ====================

    public static final String NETWORKS_ID = "networks"; // Official version / Chinese localized version
    public static final String NETWORKS_CHANGED_ID = "networks-changed"; // Xinzi version

    public static final NamespacedKey ITEM = newKey("item");
    public static final NamespacedKey ITEM2 = customNewKey(NETWORKS_ID, "item");
    public static final NamespacedKey ITEM3 = customNewKey(NETWORKS_CHANGED_ID, "item");

    public static final NamespacedKey AMOUNT = newKey("amount");
    public static final NamespacedKey AMOUNT2 = customNewKey(NETWORKS_ID, "amount");
    public static final NamespacedKey AMOUNT3 = customNewKey(NETWORKS_CHANGED_ID, "amount");

    public static final NamespacedKey MAX_AMOUNT = newKey("max_amount");
    public static final NamespacedKey MAX_AMOUNT2 = customNewKey(NETWORKS_ID, "max_amount");
    public static final NamespacedKey MAX_AMOUNT3 = customNewKey(NETWORKS_CHANGED_ID, "max_amount");

    public static final NamespacedKey VOID = newKey("void");
    public static final NamespacedKey VOID2 = customNewKey(NETWORKS_ID, "void");
    public static final NamespacedKey VOID3 = customNewKey(NETWORKS_CHANGED_ID, "void");

    public static final NamespacedKey SUPPORTS_CUSTOM_MAX_AMOUNT = newKey("supports_custom_max_amount");

    public static final NamespacedKey QUANTUM_STORAGE_INSTANCE = newKey("quantum_storage");
    public static final NamespacedKey QUANTUM_STORAGE_INSTANCE2 = customNewKey(NETWORKS_ID, "quantum_storage");
    public static final NamespacedKey QUANTUM_STORAGE_INSTANCE3 = customNewKey(NETWORKS_CHANGED_ID, "quantum_storage");

    public static NamespacedKey newKey(@NotNull String key) {
        return new NamespacedKey("networks", key);
    }

    public static NamespacedKey customNewKey(@NotNull String namespace, @NotNull String key) {
        return new NamespacedKey(namespace, key);
    }

    // ==================== PDC 读写工具（原 DataTypeMethods） ====================

    @Nullable
    public static <T, Z> Z getCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().get(key, type);
    }

    @NotNull
    public static <T, Z> Optional<Z> getOptionalCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return Optional.ofNullable(getCustom(holder, key, type));
    }

    public static <T, Z> @NotNull Z getCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultVal) {
        return holder.getPersistentDataContainer().getOrDefault(key, type, defaultVal);
    }

    public static <T, Z> boolean hasCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().has(key, type);
    }

    public static <T, Z> void setCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z obj) {
        holder.getPersistentDataContainer().set(key, type, obj);
    }

    public static <T, Z> void removeCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        holder.getPersistentDataContainer().remove(key);
    }

    // ==================== 量子存储 PDC 数据类型（原 PersistentQuantumStorageType） ====================

    public static final PersistentDataType<PersistentDataContainer, QuantumCache> QUANTUM_STORAGE_TYPE =
            new PersistentDataType<>() {
                @Override
                public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
                    return PersistentDataContainer.class;
                }

                @Override
                public @NotNull Class<QuantumCache> getComplexType() {
                    return QuantumCache.class;
                }

                @Override
                public @NotNull PersistentDataContainer toPrimitive(@NotNull QuantumCache complex, @NotNull PersistentDataAdapterContext context) {
                    final PersistentDataContainer container = context.newPersistentDataContainer();

                    if (complex.getItemStack() != null) {
                        container.set(ITEM, DataType.ITEM_STACK, complex.getItemStack());
                    }
                    container.set(AMOUNT, DataType.LONG, complex.getAmountLong());
                    container.set(MAX_AMOUNT, DataType.LONG, complex.getLimitLong());
                    container.set(VOID, DataType.BOOLEAN, complex.isVoidExcess());
                    container.set(SUPPORTS_CUSTOM_MAX_AMOUNT, DataType.BOOLEAN, complex.supportsCustomMaxAmount());
                    return container;
                }

                @Override
                public @NotNull QuantumCache fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
                    ItemStack item = primitive.get(ITEM, DataType.ITEM_STACK);
                    if (item == null) {
                        item = primitive.get(ITEM2, DataType.ITEM_STACK);
                    }
                    if (item == null) {
                        item = primitive.get(ITEM3, DataType.ITEM_STACK);
                    }

                    long amount = readAmount(primitive);
                    long limit = readLimit(primitive);
                    boolean voidExcess = readVoidExcess(primitive);
                    boolean supportsCustomMaxAmount =
                            primitive.getOrDefault(SUPPORTS_CUSTOM_MAX_AMOUNT, DataType.BOOLEAN, false);

                    return new QuantumCache(item, amount, limit, voidExcess, supportsCustomMaxAmount);
                }
            };

    private static long readAmount(@NotNull PersistentDataContainer primitive) {
        Long amount;
        try {
            amount = primitive.get(AMOUNT, DataType.LONG);
            if (amount == null) {
                amount = primitive.get(AMOUNT2, DataType.LONG);
            }
            if (amount == null) {
                amount = primitive.getOrDefault(AMOUNT3, DataType.LONG, 0L);
            }
        } catch (Throwable ignored) {
            Integer amountI;
            amountI = primitive.get(AMOUNT, DataType.INTEGER);
            if (amountI == null) {
                amountI = primitive.get(AMOUNT2, DataType.INTEGER);
            }
            if (amountI == null) {
                amountI = primitive.getOrDefault(AMOUNT3, DataType.INTEGER, 0);
            }
            amount = amountI.longValue();
        }
        return amount;
    }

    private static long readLimit(@NotNull PersistentDataContainer primitive) {
        Long limit;
        try {
            limit = primitive.get(MAX_AMOUNT, DataType.LONG);
            if (limit == null) {
                limit = primitive.get(MAX_AMOUNT2, DataType.LONG);
            }
            if (limit == null) {
                limit = primitive.getOrDefault(MAX_AMOUNT3, DataType.LONG, 64L);
            }
        } catch (Throwable ignored) {
            Integer limitI;
            limitI = primitive.get(MAX_AMOUNT, DataType.INTEGER);
            if (limitI == null) {
                limitI = primitive.get(MAX_AMOUNT2, DataType.INTEGER);
            }
            if (limitI == null) {
                limitI = primitive.getOrDefault(MAX_AMOUNT3, DataType.INTEGER, 64);
            }
            limit = limitI.longValue();
        }
        return limit;
    }

    private static boolean readVoidExcess(@NotNull PersistentDataContainer primitive) {
        Boolean voidExcess = primitive.get(VOID, DataType.BOOLEAN);
        if (voidExcess == null) {
            voidExcess = primitive.get(VOID2, DataType.BOOLEAN);
        }
        if (voidExcess == null) {
            voidExcess = primitive.getOrDefault(VOID3, DataType.BOOLEAN, false);
        }
        return voidExcess;
    }

    // ==================== ItemStack 缓存（原 ItemStackCache） ====================

    public static class ItemStackCache {

        private @Nullable ItemStack itemStack;
        private @Nullable ItemMeta itemMeta = null;
        private boolean metaCached = false;

        public ItemStackCache(@Nullable ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Nullable
        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public void setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            this.metaCached = false;
            this.itemMeta = null;
        }

        @Nullable
        public ItemMeta getItemMeta() {
            if (this.itemMeta == null && !this.metaCached) {
                this.itemMeta = itemStack == null ? null : itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;
                this.metaCached = true;
            }
            return this.itemMeta;
        }

        @Nullable
        public Material getItemType() {
            return this.itemStack == null ? null : this.itemStack.getType();
        }
    }

    // ==================== 量子缓存（原 QuantumCache） ====================

    public static class QuantumCache extends ItemStackCache {

        @Nullable
        private final ItemMeta storedItemMeta;

        private final boolean supportsCustomMaxAmount;

        private long limit;

        private long amount;

        private boolean voidExcess;

        public QuantumCache(
                @Nullable ItemStack storedItem,
                long amount,
                long limit,
                boolean voidExcess,
                boolean supportsCustomMaxAmount) {
            super(storedItem);
            this.storedItemMeta = storedItem == null ? null : storedItem.getItemMeta();
            this.amount = amount;
            this.limit = limit;
            this.voidExcess = voidExcess;
            this.supportsCustomMaxAmount = supportsCustomMaxAmount;
        }

        public int getLimit() {
            return limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
        }

        public long getLimitLong() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public long getAmount() {
            return amount;
        }

        public long getAmountLong() {
            return amount;
        }

        public int getAmountInt() {
            return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        }

        public boolean isVoidExcess() {
            return voidExcess;
        }

        public void setVoidExcess(boolean voidExcess) {
            this.voidExcess = voidExcess;
        }

        @Nullable
        public ItemMeta getStoredItemMeta() {
            return storedItemMeta;
        }

        public void setAmount(int amount) {
            if (amount < -2_000_000_000) {
                this.amount = -amount; // just for data fix in some case, normally nothing will reach -2B
            } else {
                this.amount = amount;
            }
        }

        public void setAmount(long amount) {
            if (amount < -2_000_000_000) {
                this.amount = -amount; // just for data fix in some case, normally nothing will reach -2B
            } else {
                this.amount = amount;
            }
        }

        public boolean supportsCustomMaxAmount() {
            return supportsCustomMaxAmount;
        }

        public int increaseAmount(int amount) {
            long total = this.amount + (long) amount;
            if (total > this.limit) {
                this.amount = this.limit;
                if (!this.voidExcess) {
                    return (int) (total - this.limit);
                }
            } else {
                this.amount = this.amount + amount;
            }
            return 0;
        }

        public void reduceAmount(int amount) {
            this.amount = this.amount - amount;
        }

        @Nullable
        public ItemStack withdrawItem(int amount) {
            if (this.getItemStack() == null) {
                return null;
            }
            final ItemStack clone = this.getItemStack().clone();
            clone.setAmount((int) Math.min(this.amount, amount));
            reduceAmount(clone.getAmount());
            return clone;
        }

        @Nullable
        public ItemStack withdrawItem() {
            if (this.getItemStack() == null) {
                return null;
            }
            return withdrawItem(this.getItemStack().getMaxStackSize());
        }

        public void addMetaLore(@NotNull ItemMeta itemMeta) {
            List<String> old = itemMeta.getLore();
            final List<String> lore = old != null ? new ArrayList<>(old) : new ArrayList<>();
            String itemName = "空";
            if (getItemStack() != null) {
                itemName = ItemStackHelper.getDisplayName(this.getItemStack());
            }
            lore.add("");
            lore.add(String.format("§e物品: %s", itemName));
            lore.add(String.format("§e数量: §f%s", this.getAmount()));
            if (this.supportsCustomMaxAmount) {
                lore.add(String.format("§e当前容量限制: §c%s", this.getLimit()));
            }

            itemMeta.setLore(lore);
        }

        public void updateMetaLore(@NotNull ItemMeta itemMeta) {
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            String itemName = "空";
            if (getItemStack() != null) {
                itemName = ItemStackHelper.getDisplayName(this.getItemStack());
            }
            final int loreIndexModifier = this.supportsCustomMaxAmount ? 1 : 0;
            lore.set(lore.size() - 2 - loreIndexModifier, String.format("§e物品: %s", itemName));
            lore.set(lore.size() - 1 - loreIndexModifier, String.format("§e数量: §f%s", this.getAmount()));
            if (this.supportsCustomMaxAmount) {
                lore.set(lore.size() - loreIndexModifier, String.format("§e当前容量限制: §c%s", this.getLimit()));
            }

            itemMeta.setLore(lore);
        }
    }

    // ==================== 高层 API（生态缸等机器调用） ====================

    private static @Nullable NamespacedKey findStorageKey(@Nullable ItemMeta meta) {
        if (meta == null) {
            return null;
        }
        if (hasCustom(meta, QUANTUM_STORAGE_INSTANCE, QUANTUM_STORAGE_TYPE)) {
            return QUANTUM_STORAGE_INSTANCE;
        }
        if (hasCustom(meta, QUANTUM_STORAGE_INSTANCE2, QUANTUM_STORAGE_TYPE)) {
            return QUANTUM_STORAGE_INSTANCE2;
        }
        if (hasCustom(meta, QUANTUM_STORAGE_INSTANCE3, QUANTUM_STORAGE_TYPE)) {
            return QUANTUM_STORAGE_INSTANCE3;
        }
        return null;
    }

    /**
     * 从物品的 ItemMeta 中读取网络量子存储缓存（兼容三种 key 变体）。
     *
     * @param meta 物品元数据
     * @return 量子缓存；不存在时返回 null
     */
    @Nullable
    public static QuantumCache getQuantumCache(@NotNull ItemMeta meta) {
        NamespacedKey key = findStorageKey(meta);
        return key == null ? null : getCustom(meta, key, QUANTUM_STORAGE_TYPE);
    }

    /**
     * 判断物品是否为"已绑定物品类型"的网络量子存储物品。
     *
     * @param item 槽位中的物品
     * @return true 表示已连接量子存储（可接收指定物品）
     */
    public static boolean isQuantumStorageItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        QuantumCache cache = getQuantumCache(item.getItemMeta());
        return cache != null && cache.getItemStack() != null && !cache.getItemStack().getType().isAir();
    }

    /**
     * 判断方块是否为 NetworksExpansion 的量子存储方块（NetworkQuantumStorage）。
     *
     * @param sfItem 目标方块对应的 SlimefunItem
     * @return true 表示是量子存储方块
     */
    public static boolean isQuantumStorageBlock(@Nullable SlimefunItem sfItem) {
        return sfItem != null && QUANTUM_STORAGE_BLOCK_CLASS.equals(sfItem.getClass().getName());
    }

    /**
     * 虚空之触绑定到量子存储方块时，把产物直接存入该方块的量子缓存。
     * <p>
     * 通过反射调用 NetworksExpansion 的 getCaches / tryInputItem / syncBlock，
     * 最大值与溢出保护由插件自身的 QuantumCache 处理（容量封顶、可配 voidExcess）。
     *
     * @param location 量子存储方块坐标
     * @param output   本次产出的物品
     * @return 未能存入的剩余数量；0 表示全部存入成功
     */
    public static long storeToQuantumStorageBlock(@NotNull Location location, @NotNull ItemStack output) {
        try {
            Class<?> cls = Class.forName(QUANTUM_STORAGE_BLOCK_CLASS);

            Object cache = ((Map<?, ?>) cls.getMethod("getCaches").invoke(null)).get(location);
            if (cache == null) {
                return output.getAmount();
            }

            ItemStack stored = (ItemStack) cache.getClass().getMethod("getItemStack").invoke(cache);
            if (stored == null || stored.getType().isAir()) {
                return output.getAmount();
            }
            if (!SlimefunUtils.isItemSimilar(output, stored, true)) {
                return output.getAmount();
            }

            // tryInputItem 会把"未能存入的剩余数量"写回 input[0]，
            // 因此必须传克隆，避免改动调用方（机器）的 outItems
            ItemStack[] input = new ItemStack[]{ output.clone() };
            cls.getMethod("tryInputItem", Location.class, ItemStack[].class, cache.getClass())
                    .invoke(null, location, input, cache);
            cls.getMethod("syncBlock", Location.class, cache.getClass()).invoke(null, location, cache);

            ItemStack leftover = input[0];
            return leftover == null ? 0 : leftover.getAmount();
        } catch (Throwable t) {
            // 反射失败（未安装 NetworksExpansion 等）：视为未存入
            return output.getAmount();
        }
    }

    /**
     * 虚空之触绑定魔法存储终端（CargoCoreMore）时，把产物存入终端已有物品的槽位。
     * <p>
     * 与生态缸行为一致：仅当终端里已存在该物品种类时才存入（不创建新槽位）。
     *
     * @param location 魔法存储终端坐标
     * @param item     本次产出的物品
     * @return true 表示已存入终端
     */
    public static boolean storeToCargoCore(@NotNull Location location, @NotNull ItemStack item) {
        BlockMenu inv = StorageCacheUtils.getMenu(location);
        if (inv == null) {
            return false;
        }
        SlimefunBlockData data = StorageCacheUtils.getBlock(location);
        if (data == null) {
            return false;
        }
        if (hasStoredItem(data, item)) {
            storeItemToExistingSlot(data, item);
            return true;
        }
        return false;
    }

    /** 检查 CargoCoreMore 中是否已经有该物品（有且数量>0） */
    private static boolean hasStoredItem(SlimefunBlockData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemStack prototype = item.clone();
        prototype.setAmount(1);

        for (int i = 0; i < CARGO_CORE_MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) {
                continue;
            }

            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem != null && storedItem.getType() != Material.AIR) {
                    storedItem.setAmount(1);
                    if (SlimefunUtils.isItemSimilar(prototype, storedItem, true)) {
                        String countStr = data.getData("item_count_" + i);
                        if (countStr != null && !countStr.isEmpty()) {
                            try {
                                if (Long.parseLong(countStr) > 0) {
                                    return true;
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /** 只向已有物品的槽位存储（不创建新槽位），并做最大值保护 */
    private static void storeItemToExistingSlot(SlimefunBlockData data, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemStack prototype = item.clone();
        prototype.setAmount(1);
        long amountToStore = item.getAmount();

        for (int i = 0; i < CARGO_CORE_MAX_STORED_ITEMS; i++) {
            String jsonData = data.getData("item_type_" + i);
            if (jsonData == null || jsonData.isEmpty()) {
                continue;
            }

            try {
                ItemStack storedItem = itemFromBase64(jsonData);
                if (storedItem != null && storedItem.getType() != Material.AIR) {
                    storedItem.setAmount(1);
                    if (SlimefunUtils.isItemSimilar(prototype, storedItem, true)) {
                        long currentCount = 0;
                        String countStr = data.getData("item_count_" + i);
                        if (countStr != null && !countStr.isEmpty()) {
                            try {
                                currentCount = Long.parseLong(countStr);
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        // 最大值保护：新数量不溢出
                        long newCount = amountToStore > Long.MAX_VALUE - currentCount
                                ? Long.MAX_VALUE : currentCount + amountToStore;
                        data.setData("item_count_" + i, String.valueOf(newCount));

                        // 数量上限保护
                        String maxStr = data.getData("item_max_" + i);
                        if (maxStr != null && !maxStr.isEmpty()) {
                            try {
                                long maxCount = Long.parseLong(maxStr);
                                if (maxCount != -1 && newCount > maxCount) {
                                    data.setData("item_count_" + i, String.valueOf(maxCount));
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static final int CARGO_CORE_MAX_STORED_ITEMS = 1145;

    /**
     * 向量子存储物品中存入产物（最大值保护 / 数据溢出保护）。
     * <p>
     * 只写入"剩余空间"以内且不超过 int 上限的数量，不会超出容量，也不会发生数值溢出；
     * 写入成功后会同步更新物品 Lore 并写回 ItemMeta。
     *
     * @param storageItem 量子存储物品（调用方需要把写入后的物品放回槽位）
     * @param output      本次产出的物品
     * @return 未能存入的剩余数量；0 表示全部存入成功
     */
    public static long store(@NotNull ItemStack storageItem, @NotNull ItemStack output) {
        if (!storageItem.hasItemMeta()) {
            return output.getAmount();
        }
        ItemMeta meta = storageItem.getItemMeta();
        NamespacedKey key = findStorageKey(meta);
        if (key == null) {
            return output.getAmount();
        }
        QuantumCache cache = getCustom(meta, key, QUANTUM_STORAGE_TYPE);
        if (cache == null || cache.getItemStack() == null) {
            return output.getAmount();
        }
        // 物品类型不一致时无法存入
        if (!SlimefunUtils.isItemSimilar(output, cache.getItemStack(), true)) {
            return output.getAmount();
        }

        long stored = cache.getAmountLong();
        long limit = cache.getLimitLong();

        // 最大值保护：剩余空间不为负数
        long remaining = limit > stored ? limit - stored : 0L;
        // 数据溢出保护：单次最多存入 output 数量 / 剩余空间，且不超过 int 上限（increaseAmount 只接受 int）
        long toAdd = Math.min(output.getAmount(), remaining);
        if (toAdd > Integer.MAX_VALUE) {
            toAdd = Integer.MAX_VALUE;
        }
        if (toAdd <= 0) {
            return output.getAmount();
        }

        cache.increaseAmount((int) toAdd);
        setCustom(meta, key, QUANTUM_STORAGE_TYPE, cache);
        refreshLore(meta, cache);
        storageItem.setItemMeta(meta);

        return output.getAmount() - toAdd;
    }

    /**
     * 计算输出槽位还能放入多少个物品（方案A：只统计空槽，每个空槽 = 64 个产出位）。
     * <p>
     * 该限制只在"未连接外部存储"时生效；连接量子存储/魔法存储终端时不做此限制。
     *
     * @param menu        机器菜单
     * @param outputSlots 输出槽位
     * @param output      本次产出的物品（方案A下不参与计算，保留参数仅为兼容调用方）
     * @return 还能容纳的数量（含整数溢出保护）
     */
    public static int calculateFitAmount(@NotNull BlockMenu menu, int[] outputSlots, @NotNull ItemStack output) {
        int emptySlots = 0;
        for (int slot : outputSlots) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) {
                emptySlots++;
            }
        }
        // 整数溢出保护（48 槽最多 3072，实际不会触发）
        return emptySlots > Integer.MAX_VALUE / 64 ? Integer.MAX_VALUE : emptySlots * 64;
    }

    public static void refreshLore(@NotNull ItemMeta meta, @NotNull QuantumCache cache) {
        try {
            cache.updateMetaLore(meta);
        } catch (Throwable t) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            int suffix = cache.supportsCustomMaxAmount() ? 3 : 2;
            while (suffix-- > 0 && !lore.isEmpty()) {
                lore.remove(lore.size() - 1);
            }
            meta.setLore(lore);
            cache.addMetaLore(meta);
        }
    }
}
