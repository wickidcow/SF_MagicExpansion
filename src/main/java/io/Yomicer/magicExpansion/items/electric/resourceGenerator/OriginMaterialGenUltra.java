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
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
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

import static io.Yomicer.magicExpansion.core.MagicExpansionItems.ORIGIN_MATERIAL_GEN_ULTRA;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;

public class OriginMaterialGenUltra extends AbstractElectricResourceMachine implements DistinctiveItem {

    private static final String PENDING_OUTPUT_KEY = "origin_ultra_pending";
    private static final String PENDING_OUTPUT_PDC_KEY = "origin_ultra_pending";
    private static final long OUTPUT_PER_OPERATION = 2_304L;

    private static final int[] BACKGROUND_SLOTS = new int[] { 0, 4, 8, 9, 13, 17 };
    private static final int[] OUTPUT_BORDER_SLOTS = new int[] { 10, 11, 12, 14, 15, 16};
    private static final int[] INPUT_BORDER_SLOTS = new int[] {1, 2, 3, 5, 6, 7};

    private static final int[] OUTPUT_SLOTS = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final ItemStack PROGRESS_ITEM = new ItemStack(Material.SOUL_LANTERN);
    private ItemStack material;

    private static final ItemStack PROGRESS_STACK = new CustomItemStack(Material.SOUL_CAMPFIRE, getGradientNameVer2("Information"), getGradientNameVer2("type:"), getGradientNameVer2("Addon: MagicExpansion"));



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
            lore.add(getGradientNameVer2("Current Item: ") + n);
            meta.setLore(lore);

            NamespacedKey key = new NamespacedKey(MagicExpansion.getInstance(), "origin_material");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, base64Item);

            long pending = getPending(data);
            if (pending > 0L) {
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(MagicExpansion.getInstance(), PENDING_OUTPUT_PDC_KEY),
                        PersistentDataType.LONG,
                        pending);
                lore.add(getGradientNameVer2("Pending Output: " + pending));
                meta.setLore(lore);
            }

            drop.setItemMeta(meta);
        }
        loc.getWorld().dropItemNaturally(loc, drop);
        data.removeData(PENDING_OUTPUT_KEY);
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

        data.setData("origin_material", base64Item);

        Long pending = meta.getPersistentDataContainer().get(
                new NamespacedKey(MagicExpansion.getInstance(), PENDING_OUTPUT_PDC_KEY),
                PersistentDataType.LONG);
        if (pending != null && pending > 0L) {
            data.setData(PENDING_OUTPUT_KEY, Long.toString(pending));
        } else {
            data.removeData(PENDING_OUTPUT_KEY);
        }
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
        SlimefunBlockData data = StorageCacheUtils.getBlock(menu.getBlock().getLocation());
        if (data == null || !flushPending(menu, data)) {
            return null;
        }

        String base64Item = data.getData("origin_material");
        ItemStack prototype = deserializeItemStack(base64Item);
        if (prototype == null || prototype.getType().isAir()) {
            return null;
        }

        return recipes.isEmpty() ? null : recipes.get(0);
    }


    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(b.getLocation());
        if (data == null) {
            return;
        }

        String base64Item = data.getData("origin_material");
        ItemStack prototype = deserializeItemStack(base64Item);
        if (prototype == null || prototype.getType().isAir()) {
            menu.replaceExistingItem(4, PROGRESS_STACK.clone());
            return;
        }

        long pending = getPending(data);
        pending = Long.MAX_VALUE - pending < OUTPUT_PER_OPERATION
                ? Long.MAX_VALUE
                : pending + OUTPUT_PER_OPERATION;
        data.setData(PENDING_OUTPUT_KEY, Long.toString(pending));

        flushPending(menu, data);
        prototype.setAmount(1);
        updateInfoStack(menu, prototype);
    }

    private boolean flushPending(BlockMenu menu, SlimefunBlockData data) {
        long pending = getPending(data);
        if (pending <= 0L) {
            data.removeData(PENDING_OUTPUT_KEY);
            return true;
        }

        ItemStack prototype = deserializeItemStack(data.getData("origin_material"));
        if (prototype == null || prototype.getType().isAir()) {
            return false;
        }
        prototype.setAmount(1);
        int maxStack = Math.max(1, prototype.getMaxStackSize());

        while (pending > 0L) {
            int batch = (int) Math.min((long) maxStack, pending);
            ItemStack output = prototype.clone();
            output.setAmount(batch);

            ItemStack leftover = menu.pushItem(output, getOutputSlots());
            int notStored = leftover == null || leftover.getType().isAir()
                    ? 0
                    : Math.max(0, leftover.getAmount());
            int accepted = batch - notStored;
            if (accepted <= 0) {
                break;
            }
            pending -= accepted;
            if (notStored > 0) {
                break;
            }
        }

        if (pending <= 0L) {
            data.removeData(PENDING_OUTPUT_KEY);
            return true;
        }

        data.setData(PENDING_OUTPUT_KEY, Long.toString(pending));
        return false;
    }

    private static long getPending(SlimefunBlockData data) {
        String raw = data.getData(PENDING_OUTPUT_KEY);
        if (raw == null || raw.isEmpty()) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(raw));
        } catch (NumberFormatException invalid) {
            return 0L;
        }
    }


    @Override
	public List<ItemStack> getDisplayRecipes() {
	    List<ItemStack> display = new ArrayList<>();

        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("Output ⇨"),getGradientNameVer2("Produces 2,304 items per operation."),getGradientNameVer2("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientNameVer2("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
        display.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, getGradientNameVer2("Output ⇨"),getGradientNameVer2("Produces 2,304 items per operation."),getGradientNameVer2("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"),getGradientNameVer2("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s")));
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
        return true;
    }

    private void updateInfoStack(BlockMenu menu, ItemStack output) {
        Inventory inv = menu.toInventory();

        if (inv == null || inv.getViewers().isEmpty()) {
            return;
        }

        ItemStack item = PROGRESS_STACK.clone();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getGradientNameVer2("Information ⇨"));
        ArrayList lore = new ArrayList<>();
        lore.add(getGradientNameVer2("type:"));
        lore.add(getGradientNameVer2("Addon: MagicExpansion"));
        lore.add(getGradientNameVer2("Current Product: ") + ItemStackHelper.getDisplayName(output));
        lore.add(getGradientNameVer2("Production Rate ⇨ ⚙ Every " + getCraftSecond() + " s per operation"));
        lore.add(getGradientNameVer2("Energy Cost ⇨ ⚡ "+ getEnergyConsumption()*2 +" J/s"));
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
