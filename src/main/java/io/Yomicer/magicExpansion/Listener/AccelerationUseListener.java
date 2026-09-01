package io.Yomicer.magicExpansion.Listener;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.Yomicer.magicExpansion.utils.MachineBuffManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.Yomicer.magicExpansion.utils.compat.ItemStackHelper;
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
        // 只对 Slimefun 方块(可放置的机器等)注入, 避免对着泥土/空气浪费鱼
        SlimefunItem target = StorageCacheUtils.getSfItem(clicked.getLocation());
        if (target == null) return;

        double quality = FishAttributeGenerator.getQuality(held);
        double multiplier = MachineBuffManager.BASE_MULTIPLIER * quality;

        // 消耗整条加速种(二代鱼为不可堆叠的个体物品)
        player.getInventory().setItemInMainHand(null);

        // 登记 5 分钟 Buff(效率接入待后续机器读取)
        MachineBuffManager.apply(clicked.getLocation(), MachineBuffManager.DEFAULT_DURATION_SECONDS, multiplier);

        e.setCancelled(true); // 阻止打开机器菜单/放置, 视为一次"注入"交互
        player.sendMessage("§b[Fish Energy] §rInjected §f" + ItemStackHelper.getDisplayName(target.getItem())
                + " §rwith an acceleration buff (×" + String.format("%.2f", multiplier) + ", 5 minutes)");
    }
}