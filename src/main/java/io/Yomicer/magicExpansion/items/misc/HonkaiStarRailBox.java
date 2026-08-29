package io.Yomicer.magicExpansion.items.misc;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSpawnReason;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.FireworkUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.Yomicer.magicExpansion.Listener.SlimefunRegistryGiftBox.itemMapMihoyoHonkai;

public class HonkaiStarRailBox extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    // 修复(M)：Random 改为共享静态实例，避免每次开盒新建 Random
    private static final Random RANDOM = new Random();

    public HonkaiStarRailBox(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }



    // 随机获取一个物品的 ItemStack
    public ItemStack getRandomItemStack(Map<String, SlimefunItem> itemMap) {
        if (itemMap.isEmpty()) {
            return null;
        }

        Collection<SlimefunItem> values = itemMap.values();
        List<SlimefunItem> list = new ArrayList<>(values);

        // 修复(M)：使用共享静态 Random
        SlimefunItem randomItem = list.get(RANDOM.nextInt(list.size()));
        return randomItem.getItem().clone();
    }

    // 在指定位置生成一个随机物品
    public void spawnRandomItem(Location location, Player player) {
        ItemStack gift = getRandomItemStack(itemMapMihoyoHonkai);

        if (gift != null) {
            SlimefunUtils.spawnItem(location, gift, ItemSpawnReason.CHRISTMAS_PRESENT_OPENED, true, player);
            player.sendMessage("§a恭喜你，从打开 Honkai: Star Rail 盲盒中开出了" + gift.getItemMeta().getDisplayName() + "！");
        }
    }



    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return (e) -> {
            e.cancel();
            e.getClickedBlock().ifPresent((block) -> {
                // 修复(M)：先判奖池非空再消耗盲盒，防止奖池为空时吞掉物品
                ItemStack gift = getRandomItemStack(itemMapMihoyoHonkai);
                if (gift == null) {
                    e.getPlayer().sendMessage("§c奖池为空，盲盒未消耗，无法开出物品！");
                    return;
                }

                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    ItemUtils.consumeItem(e.getItem(), false);
                }

                FireworkUtils.launchRandom(e.getPlayer(), 3);
                Block b = block.getRelative(e.getClickedFace());
                SlimefunUtils.spawnItem(b.getLocation(), gift, ItemSpawnReason.CHRISTMAS_PRESENT_OPENED, true, e.getPlayer());
                e.getPlayer().sendMessage("§a恭喜你，从打开 Honkai: Star Rail 盲盒中开出了" + gift.getItemMeta().getDisplayName() + "！");

            });
        };
    }



}
