package io.Yomicer.magicExpansion.items.preBuildings;

import io.Yomicer.magicExpansion.utils.preBuildingUtils.PreBuildingsTreeUtils;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.Yomicer.magicExpansion.utils.ColorGradient.*;
import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static org.bukkit.inventory.EquipmentSlot.HAND;

public class PreBuildingTree extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {


    private final String buildingName;
    private final String originName;
    private final String replaceName;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private final long COOLDOWN_MS;

    public PreBuildingTree(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,String buildingName,long timeCd) {
        super(category, item, recipeType, recipe);
        this.buildingName = buildingName;
        this.originName = null;
        this.replaceName = null;
        this.COOLDOWN_MS = timeCd*1000;

    }

    public PreBuildingTree(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,String buildingName, String originName,String replaceName,long timeCd) {
        super(category, item, recipeType, recipe);
        this.buildingName = buildingName;
        this.originName = originName;
        this.replaceName = replaceName;
        this.COOLDOWN_MS = timeCd*1000;

    }


    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            Player player = e.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            // 检查玩家手上是否有物品
            if (e.getHand()!= HAND) {
                player.sendMessage(getGradientName("Hold this item in your main hand to use it."));
                return;
            }
            // 检查是否按住 Shift
            if (player.isSneaking()) {

                String fileName = buildingName;
                // 调用工具类获取尺寸
                int[] dims = PreBuildingsTreeUtils.getBuildingDimensions(fileName);
                if (dims == null) {
                    player.sendMessage(getGradientNameVer2("The structure file could not be loaded: resources/buildings/" + fileName + ".json"));
                    return;
                }

                int x = dims[0];
                int y = dims[1];
                int z = dims[2];

                // 发送消息给玩家
                player.sendMessage(getGradientNameVer2("========================="));
                player.sendMessage(getGradientNameVer2("Structure Preview:"));
                player.sendMessage(getGradientNameVer2("File Name: " + fileName + ".json"));
                player.sendMessage(getGradientNameVer2("Dimensions — X: " + x + " | Y: " + y + " | Z: " + z));
                player.sendMessage(getGradientNameVer2("Footprint: " + (x * z) + " blocks"));
                player.sendMessage(getGradientNameVer2("Volume: " + (x * y * z) + " blocks"));
                player.sendMessage(getGradientNameVer2("========================="));

                return; // 处理完 Shift+右键 后直接返回,不再执行放置逻辑
            }

            if(!ItemPermissionUtils.hasPermissionRe(player)){
                return;
            }
            if (player.isOp()) {
                if (!PreBuildingsTreeUtils.pasteMap(player, buildingName, originName, replaceName)) {
                    player.sendMessage(getGradientNameVer2("The structure could not be placed at this location."));
                    return;
                }
                player.sendMessage(getRandomGradientName("Placed " + ItemStackHelper.getDisplayName(itemInHand) + " with operator cooldown bypass."));
                return;
            }

            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();
            // 每次使用时清理过期的冷却记录
            cooldowns.entrySet().removeIf(entry -> now - entry.getValue() >= COOLDOWN_MS);
            // 检查冷却
            if (cooldowns.containsKey(playerId)) {
                long lastUsed = cooldowns.get(playerId);
                if (now - lastUsed < COOLDOWN_MS) {
                    long remaining = (COOLDOWN_MS - (now - lastUsed)) / 1000 + 1;
                    player.sendMessage("§cThis item is on cooldown. Wait " + remaining + " seconds before using it again.");
                    return;
                }
            }


            if(!PreBuildingsTreeUtils.pasteMap(player,buildingName, originName, replaceName)){
                return;
            }
            // ✅ 使用成功,更新冷却时间
            cooldowns.put(playerId, now);

            // 减少手上的物品数量
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null); // 如果数量为 1,则直接移除
            }
        };
    }


}
