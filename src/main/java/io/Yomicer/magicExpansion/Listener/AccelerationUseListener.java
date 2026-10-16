package io.Yomicer.magicExpansion.Listener;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.Yomicer.magicExpansion.utils.AcceleratableMachine;
import io.Yomicer.magicExpansion.utils.MachineBuffManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * 第二层·单机加速(注册阶段): 手持【加速种】右键机器 → 消耗该鱼并给目标机器注入 5 分钟加速 Buff。
 * <p>
 * 第一版只登记 Buff 数据(见 {@link MachineBuffManager}), 机器效率接入留待后续;
 * Buff 记录在内存, 重启失效。
 */
public class AccelerationUseListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClickMachine(PlayerInteractEvent e) {
        // 仅主手右键方块(副手事件重复触发, 过滤)
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) return;

        // 只处理二代鱼; 仅"加速种"能注入加速
        if (!FishAttributeGenerator.isGen2Fish(held)) return;
        Gen2Fish type = FishAttributeGenerator.getType(held);
        if (type == null) return;
        Gen2Fish.Trait trait = FishAttributeGenerator.getTrait(held);
        Gen2Fish.Trait effectiveTrait = trait != null ? trait : type.getDefaultTrait();
        if (effectiveTrait != Gen2Fish.Trait.ACCEL) {
            return; // 合成种/普通鱼不干预
        }

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        SlimefunItem target = StorageCacheUtils.getSlimefunItem(clicked.getLocation());
        if (target == null) {
            return;
        }

        if (!(target instanceof AcceleratableMachine)) {
            e.setCancelled(true);
            player.sendMessage("§e[Fish Energy] §rThis machine does not support acceleration yet. The fish was not consumed.");
            return;
        }

        if (!player.hasPermission("slimefun.inventory.bypass")
                && !Slimefun.getProtectionManager().hasPermission(
                        player, clicked.getLocation(), Interaction.INTERACT_BLOCK)) {
            e.setCancelled(true);
            player.sendMessage("§c[Fish Energy] §rYou do not have permission to accelerate this machine.");
            return;
        }

        double quality = FishAttributeGenerator.getQuality(held);
        if (!Double.isFinite(quality) || quality < 0.7D || quality > 2.5D) {
            e.setCancelled(true);
            player.sendMessage("§c[Fish Energy] §rThis acceleration fish has invalid quality data.");
            return;
        }

        double multiplier = MachineBuffManager.BASE_MULTIPLIER * quality;
        MachineBuffManager.apply(
                clicked.getLocation(),
                MachineBuffManager.DEFAULT_DURATION_SECONDS,
                multiplier);

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemUtils.consumeItem(held, false);
        }

        e.setCancelled(true);
        player.sendMessage("§b[Fish Energy] §rInjected §f"
                + ItemStackHelper.getDisplayName(target.getItem())
                + " §rwith an acceleration buff (×"
                + String.format("%.2f", multiplier) + ", 5 minutes)");
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMachineBreak(BlockBreakEvent event) {
        MachineBuffManager.remove(event.getBlock().getLocation());
    }
}
}