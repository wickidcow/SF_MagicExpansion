package io.Yomicer.magicExpansion.items.misc;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.misc.moreLure.ZKBTMPlayer;
import io.Yomicer.magicExpansion.utils.GiveItem;
import io.Yomicer.magicExpansion.utils.preBuildingUtils.PreBuildingsTreeUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.*;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static io.Yomicer.magicExpansion.utils.CreateItem.createItem;

public class TestMusicRightClick extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public TestMusicRightClick(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_MS = 1000*15;


    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {

        return e -> {
            // 阻止默认行为(放置方块或使用物品)
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            // 获取玩家
            Player player = e.getPlayer();
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
            //player.sendTitle("✨🌹", "Listen to the whispers of the stars...", 10, 60, 10);
            // 播放旋律(调用我们封装的函数)
            new ZKBTMPlayer(MagicExpansion.getInstance()).playCuteMusic(player);
            // ✅ 使用成功,更新冷却时间
            cooldowns.put(playerId, now);
        };
    }



}
