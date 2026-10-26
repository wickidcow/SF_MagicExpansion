package io.Yomicer.magicExpansion.Listener.fishingListener;

import io.Yomicer.magicExpansion.items.tools.FishingRodWaterCloud;
import io.Yomicer.magicExpansion.utils.WaterCloudRodEffects;
import io.Yomicer.magicExpansion.utils.WaterCloudRodProficiency;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Objects;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Upstream Water Cloud classic rod hook-speed bonus, opt-in and region-local.
 *
 * <p>The existing FishingCompatibility router exclusively dispatches cast
 * events when MagicExpansion owns the fishing interaction. This handler never
 * schedules a global task, touches another region or changes catch payouts.
 */
public final class WaterCloudClassicHookSpeed {

    public static final String ENABLED_PATH = "Fish.WaterCloud.classic-hook-speed-enabled";

    private final JavaPlugin plugin;

    public WaterCloudClassicHookSpeed(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void onCast(PlayerFishEvent event) {
        if (!plugin.getConfig().getBoolean(ENABLED_PATH, false)
                || event.isCancelled()
                || event.getState() != PlayerFishEvent.State.FISHING
                || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        FishHook hook = event.getHook();
        if (hook == null) {
            return;
        }

        ItemStack heldRod = event.getPlayer().getInventory().getItemInMainHand();
        SlimefunItem registered = SlimefunItem.getByItem(heldRod);
        if (!(registered instanceof FishingRodWaterCloud)
                || !WaterCloudRodProficiency.supportsRodId(registered.getId())) {
            return;
        }

        int currentMaximum = hook.getMaxWaitTime();
        int adjustedMaximum = cappedWaitTicks(currentMaximum,
                WaterCloudRodProficiency.getLevel(heldRod));
        if (adjustedMaximum > 0 && adjustedMaximum < currentMaximum) {
            hook.setMaxWaitTime(adjustedMaximum);
        }
    }

    /**
     * Original upstream rule: 60 ticks less per two levels, from a
     * 600-tick default (level 8 => 360 ticks). Never lengthen an existing
     * shorter cap from server configuration or another authorized handler.
     *
     * @return an unchanged nonpositive value if a hook's cap is invalid
     */
    public static int cappedWaitTicks(int currentMaximum, int level) {
        if (currentMaximum <= 0) {
            return currentMaximum;
        }

        int reduction = WaterCloudRodEffects.getOldHookSpeedTicks(level);
        if (reduction <= 0) {
            return currentMaximum;
        }

        int target = Math.max(100, 600 - reduction);
        return Math.min(currentMaximum, target);
    }
}
