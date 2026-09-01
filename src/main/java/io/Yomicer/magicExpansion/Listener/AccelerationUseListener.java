package io.Yomicer.magicExpansion.Listener;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.Yomicer.magicExpansion.utils.MachineBuffManager;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Applies the Release 10 Gen-2 acceleration-fish buff to a Slimefun machine.
 * The buff is currently stored in memory for five minutes and is intentionally
 * kept separate from the machine-specific speed integration layer.
 */
public class AccelerationUseListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClickMachine(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) return;

        if (!FishAttributeGenerator.isGen2Fish(held)) return;
        Gen2Fish type = FishAttributeGenerator.getType(held);
        if (type == null) return;
        Gen2Fish.Trait trait = FishAttributeGenerator.getTrait(held);
        Gen2Fish.Trait effectiveTrait = trait != null ? trait : type.getDefaultTrait();
        if (effectiveTrait != Gen2Fish.Trait.ACCEL) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        SlimefunItem target = StorageCacheUtils.getSfItem(clicked.getLocation());
        if (target == null) return;

        double quality = FishAttributeGenerator.getQuality(held);
        double multiplier = MachineBuffManager.BASE_MULTIPLIER * quality;

        player.getInventory().setItemInMainHand(null);
        MachineBuffManager.apply(clicked.getLocation(), MachineBuffManager.DEFAULT_DURATION_SECONDS, multiplier);

        e.setCancelled(true);
        player.sendMessage("§b[Fish Energy] §rInjected §f" + ItemStackHelper.getDisplayName(target.getItem())
                + " §rwith an acceleration buff (×" + String.format("%.2f", multiplier) + ", 5 minutes).");
    }
}