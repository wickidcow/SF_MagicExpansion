package io.Yomicer.magicExpansion.items.electric.resourceGenerator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.AbstractElectricResourceMachine;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN;
import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN_ULTRA;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;

public class OriginMaterialGenUltra extends AbstractElectricResourceMachine implements DistinctiveItem {

    private static final int[] BACKGROUND_SLOTS = new int[] { 0, 4, 8, 9, 13, 17 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 10, 11, 12, 14, 15, 16};
    private static final int[] INPUT_BORDER_SLOTS = new int[] {1, 2, 3, 5, 6, 7};

    private static final int[] OUTPUT_SLOTS = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private ItemStack material;

    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientNameVer2("信息"), getGradientNameVer2("类型：资源生成器"), getGradientNameVer2("所属附属：魔法"));



    public OriginMaterialGenUltra(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

    }


    protected void onBreak(BlockBreakEvent e, BlockMenu menu, Location l) {
        super.onBreak(e, menu, l);
        e.setDropItems(false);
        Location loc = e.getBlock().getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        if (data == null) return;

        // 获取序列化的 Base64 字符串
        String base64Item = data.getData("origin_material");
        if (base64Item == null) {
            data.removeData("origin_material");
            return;
        }

        // 反序列化获取原始物品
        ItemStack originalItem = deserializeItemStack(base64Item);
        if (originalItem == null) return;

        String n = ItemStackHelper.getDisplayName(originalItem);
        ItemStack drop = ORIGIN_MATERIAL_GEN_ULTRA.clone();
        if (drop == null || !drop.hasItemMeta()) return;
        ItemMeta meta = drop.getItemMeta();
        if (meta != null) {
            String originalName = meta.getDisplayName();
            meta.setDisplayName(originalName + " - " + n);
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(getGradientNameVer2("当前推演物品：" )+ n);
            meta.setLore(lore);

            NamespacedKey key = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");
            // 将 Base64 字符串存储到掉落物的 PDC 中
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, base64Item);
            drop.setItemMeta(meta);
        }
        loc.getWorld().dropItemNaturally(loc, drop);
        data.removeData("origin_material");
    }


    protected void onPlace(BlockPlaceEvent e, Block b) {
        super.onPlace(e, b);
        ItemStack item = e.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");

        // 获取 Base64 字符串
        String base64Item = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (base64Item == null) return;

        Location loc = e.getBlockPlaced().getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        if (data == null) return;

        // 存入 Base64 字符串
        data.setData("origin_material", base64Item);
    }



    @Override
    public void postRegister() {
        registerRecipe(getCraftSecond(), new ItemStack[] { this.material }, getItemStackOutputs());
    }
    
//    @Override
//    public void tick(BlockMenu menu, Block b) {
////            updateInfoStack(menu);
//        super.tick(menu, b);
//    }

    @Override
	public MachineRecipe findNextRecipe(BlockMenu menu) {
        
        int maxedSlots = 0; 
        for (int slots : getOutputSlots()) {
            ItemStack item = menu.getItemInSlot(slots);
            if (item != null && item.getMaxStackSize() == item.getAmount()) {
                maxedSlots += 1;
            }
        }

        if (maxedSlots == getOutputSlots().length) { return null; }

        // L1 修复：配方列表为空时直接返回 null，避免 recipes.get(0) 抛 IndexOutOfBoundsException
        if (recipes == null || recipes.isEmpty()) { return null; }

        MachineRecipe recipe = recipes.get(0);

        return recipe;

    }


    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        Location loc = b.getLocation();
        SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
        if (data == null) return;

        // 取出 Base64 字符串
        String base64Item = data.getData("origin_material");
        ItemStack output = null;
        if (base64Item != null) {
            // 反序列化得到带有完整属性的原始物品
            output = deserializeItemStack(base64Item);
            if (output != null) {
                output.setAmount(64); // 设定单次推出的数量
            }
        }

        if (output != null) {
            // L3 修复：大批量推送（36×64）前先用 fits 逻辑计算输出槽可容纳量，
            // 能放多少放多少，剩余留到下个生产周期，不再静默丢弃
            int totalToPush = Math.min(36 * output.getAmount(), calcOutputFit(menu, output));
            int remaining = totalToPush;
            while (remaining > 0) {
                int batch = Math.min(remaining, output.getMaxStackSize());
                ItemStack batchStack = output.clone();
                batchStack.setAmount(batch);
                menu.pushItem(batchStack, getOutputSlots());
                remaining -= batch;
            }
            updateInfoStack(menu, output);
        } else {
            ItemStack item = PROGRESS_STACK.clone();
            menu.replaceExistingItem(4, item);
        }
    }

    // L3: 计算输出槽对指定物品还能容纳多少个（空槽按最大堆叠算，同类槽按剩余空间算）
    private int calcOutputFit(BlockMenu menu, ItemStack output) {
        int fit = 0;
        for (int slot : getOutputSlots()) {
            ItemStack cur = menu.getItemInSlot(slot);
            if (cur == null || cur.getType().isAir()) {
                fit += output.getMaxStackSize();
            } else if (cur.isSimilar(output)) {
                fit += cur.getMaxStackSize() - cur.getAmount();
            }
        }
        return fit;
    }



    @Override
	public List<ItemStack> getDisplayRecipes() {
	    List<ItemStack> display = new ArrayList<>();

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("产物⇨"),getGradientNameVer2("每次生产 2304 个"),getGradientNameVer2("生产效率⇨ ⚙ 每 " + getCraftSecond() + " s生成一次"),getGradientNameVer2("生产能耗⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("产物⇨"),getGradientNameVer2("每次生产 2304 个"),getGradientNameVer2("生产效率⇨ ⚙ 每 " + getCraftSecond() + " s生成一次"),getGradientNameVer2("生产能耗⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
        // 将输出物品数组中的所有物品添加到显示列表
        display.addAll(Arrays.asList(getItemStackOutputs()));

        return display;
	}

	@Override
	protected ItemStack getProgressBar() {
		return PROGRESS_ITEM;
	}

    protected int getProgressSlot() {
        return 13;
    }

	@Override
	protected void setupMenu(BlockMenuPreset preset) {
	    preset.drawBackground(new CustomItemStack(Material.PINK_STAINED_GLASS_PANE," "), BACKGROUND_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE," "), INPUT_BORDER_SLOTS);
        preset.drawBackground(new CustomItemStack(Material.LIME_STAINED_GLASS_PANE," "), OUTPUT_BORDER_SLOTS);
        preset.addItem(getProgressSlot(), new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, " "), ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(4, PROGRESS_STACK, ChestMenuUtils.getEmptyClickHandler());
 
    }

	@Override
	protected int[] getInputSlots() {
		return new int[] {};
	}

	@Override
	protected int[] getOutputSlots() {
		return OUTPUT_SLOTS;
	}
    
    @Override
    public boolean isSynchronized() {
        return false;
    }

    private void updateInfoStack(BlockMenu menu, ItemStack output) {
        Inventory inv = menu.toInventory();

        if (inv == null || inv.getViewers().isEmpty()) {
            return;
        }

        ItemStack item = PROGRESS_STACK.clone();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getGradientNameVer2("信息⇨"));
        ArrayList lore = new ArrayList<>();
        lore.add(getGradientNameVer2("类型：资源生成器"));
        lore.add(getGradientNameVer2("所属附属：魔法"));
        lore.add(getGradientNameVer2("当前生产物品：") + ItemStackHelper.getDisplayName(output));
        lore.add(getGradientNameVer2("生产效率⇨ ⚙ 每 " + getCraftSecond() + " s生成一次"));
        lore.add(getGradientNameVer2("生产能耗⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s"));
        meta.setLore(lore);
        item.setItemMeta(meta);

        menu.replaceExistingItem(4, item);
    }

    @Override
    public boolean canStack(@NotNull ItemMeta itemMeta, @NotNull ItemMeta itemMeta1) {
        return false;
    }


    /**
     * 将 Base64 字符串反序列化回 ItemStack
     */
    public static ItemStack deserializeItemStack(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }







}
