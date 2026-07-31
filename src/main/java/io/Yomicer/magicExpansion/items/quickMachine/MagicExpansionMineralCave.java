package io.Yomicer.magicExpansion.items.quickMachine;
import io.Yomicer.magicExpansion.utils.GiveItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.*;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.CreateItem.createItem;

public class MagicExpansionMineralCave extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public MagicExpansionMineralCave(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }



    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {

        return e -> {
            // 阻止默认行为(放置方块或使用物品)
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);

            // 获取玩家
            Player player = e.getPlayer();

            // 打开自定义菜单
            openCustomMenu(player);
        };
    }


    private void openCustomMenu(Player player) {
        // 创建一个新的 ChestMenu
        ChestMenu menu = new ChestMenu(getGradientName("Magic Mineral Cave"));
        // 用于记录已点击的槽位
        Set<Integer> clickedSlots = new HashSet<>();
        // 设置菜单槽位内容
        for (int i = 0; i < 54; i++) {
            Random random = new Random();
            // 判断是否满足 80% 概率
            boolean isGenerated = random.nextDouble() < 0.8;
            if (isGenerated||i==53){
            menu.addItem(i, createItem(Material.STONE, getGradientName("Click")), (player1, slot, item, action) -> {

                // 检查该槽位是否已被点击过
                if (clickedSlots.contains(slot)) {
                    player1.sendMessage("§cyou already!");
                    return false; // 返回 false 表示不消耗物品
                }
                // 玩家点击后,将该槽位替换为普通圆石
                ItemStack randomItem = getRandomItem(random);
                ItemStack treasureItem = randomItem.clone();
                ItemMeta meta = treasureItem.getItemMeta();
                meta.setDisplayName("§eTreasure: §b" + ItemStackHelper.getDisplayName(treasureItem));
                treasureItem.setItemMeta(meta);
                menu.replaceExistingItem(slot, treasureItem);
                GiveItem.giveOrDropItem(player1, randomItem);
                // 将槽位标记为已点击
                clickedSlots.add(slot);
                player1.sendMessage("You mined the stone at coordinates " + slot+"!");
                return false; // 返回 false 表示不消耗物品
            });
        }
        }

        // 设置空槽位是否可点击
        menu.setEmptySlotsClickable(false);
        menu.setPlayerInventoryClickable(false);

        // 打开菜单
        menu.open(player);
    }



    // 预设的物品列表(静态常量)
    private static final List<ItemStack> ITEMS;
    static {
        // 初始化物品列表
        ITEMS = new ArrayList<>();
        ITEMS.add(new ItemStack(Material.COBBLESTONE));
        ITEMS.add(new ItemStack(Material.IRON_INGOT));
        ITEMS.add(new ItemStack(Material.GOLD_INGOT));
        ITEMS.add(new ItemStack(Material.COPPER_INGOT));
        ITEMS.add(new ItemStack(Material.COAL));
        ITEMS.add(new ItemStack(Material.RAW_IRON));
        ITEMS.add(new ItemStack(Material.RAW_COPPER));
        ITEMS.add(new ItemStack(Material.RAW_GOLD));
        ITEMS.add(new ItemStack(Material.EMERALD));
        ITEMS.add(new ItemStack(Material.DIAMOND));
        ITEMS.add(new ItemStack(Material.LAPIS_LAZULI));
        ITEMS.add(new ItemStack(Material.QUARTZ));
        ITEMS.add(new ItemStack(Material.AMETHYST_SHARD));
        ITEMS.add(new ItemStack(Material.FLINT));
        ITEMS.add(new ItemStack(Material.BONE));
        ITEMS.add(new ItemStack(Material.CLAY));
        ITEMS.add(new ItemStack(Material.BONE_BLOCK));
        ITEMS.add(new ItemStack(Material.NETHERITE_SCRAP));
        ITEMS.add(new ItemStack(Material.SOUL_SAND));
        ITEMS.add(new ItemStack(Material.GOLD_NUGGET));
        ITEMS.add(new ItemStack(Material.NETHERRACK));
        ITEMS.add(SlimefunItems.TIN_INGOT);
        ITEMS.add(SlimefunItems.SILVER_INGOT);
        ITEMS.add(SlimefunItems.LEAD_INGOT);
        ITEMS.add(SlimefunItems.ALUMINUM_INGOT);
        ITEMS.add(SlimefunItems.ZINC_INGOT);
        ITEMS.add(SlimefunItems.MAGNESIUM_INGOT);
        ITEMS.add(SlimefunItems.MAGNESIUM_DUST);
        ITEMS.add(SlimefunItems.ZINC_DUST);
        ITEMS.add(SlimefunItems.ALUMINUM_DUST);
        ITEMS.add(SlimefunItems.LEAD_DUST);
        ITEMS.add(SlimefunItems.SILVER_DUST);
        ITEMS.add(SlimefunItems.TIN_DUST);
        ITEMS.add(SlimefunItems.COPPER_DUST);
        ITEMS.add(SlimefunItems.IRON_DUST);
        ITEMS.add(SlimefunItems.GOLD_DUST);

    }

    // 随机物品生成方法
    private static ItemStack getRandomItem(Random random) {
        // 随机选择一个物品
        return ITEMS.get(random.nextInt(ITEMS.size()));
    }



}
