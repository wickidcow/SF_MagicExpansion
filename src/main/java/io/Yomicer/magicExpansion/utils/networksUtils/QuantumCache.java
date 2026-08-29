package io.Yomicer.magicExpansion.utils.networksUtils;

import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class QuantumCache extends ItemStackCache {

    @Nullable
    private final ItemMeta storedItemMeta;

    private final boolean supportsCustomMaxAmount;

    // 原 Lombok @Setter 手工展开：写入库存上限
    private long limit;

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
    }

    public long getLimitLong() {
        return limit;
    }

    // 原 Lombok @Getter 手工展开：读取当前库存数量
    private long amount;

    public long getAmount() {
        return amount;
    }

    public int getAmountInt() {
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    public long getAmountLong() {
        return amount;
    }

    // 原 Lombok @Setter/@Getter 手工展开：超出上限的溢出标记
    private boolean voidExcess;

    public boolean isVoidExcess() {
        return voidExcess;
    }

    public void setVoidExcess(boolean voidExcess) {
        this.voidExcess = voidExcess;
    }

    public QuantumCache(
            @Nullable ItemStack storedItem,
            long amount,
            int limit,
            boolean voidExcess,
            boolean supportsCustomMaxAmount) {
        this(storedItem, amount, (long) limit, voidExcess, supportsCustomMaxAmount);
    }

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

    @Nullable
    public ItemMeta getStoredItemMeta() {
        return this.storedItemMeta;
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
        return this.supportsCustomMaxAmount;
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
        lore.add(String.format(
                "§e数量: §f%s", this.getAmount()));
        if (this.supportsCustomMaxAmount) {
            lore.add(String.format(
                    "§e当前容量限制: §c%s", this.getLimit()));
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
        lore.set(
                lore.size() - 2 - loreIndexModifier,
                String.format("§e物品: %s", itemName));
        lore.set(
                lore.size() - 1 - loreIndexModifier,
                String.format(
                        "§e数量: §f%s", this.getAmount()));
        if (this.supportsCustomMaxAmount) {
            lore.set(
                    lore.size() - loreIndexModifier,
                    String.format(
                            "§e当前容量限制: §c%s",
                            this.getLimit()));
        }

        itemMeta.setLore(lore);
    }
}
