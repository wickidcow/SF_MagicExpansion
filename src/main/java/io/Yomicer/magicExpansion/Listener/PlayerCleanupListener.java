package io.Yomicer.magicExpansion.Listener;

import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ItemEffectAttackListener;
import io.Yomicer.magicExpansion.Listener.weaponApply.SlownessManager;
import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import io.Yomicer.magicExpansion.items.misc.magicAlter.RecipeBookManager;
import io.Yomicer.magicExpansion.items.misc.weapon.StarShardsSword;
import io.Yomicer.magicExpansion.items.tools.CustomSequenceTool;
import io.Yomicer.magicExpansion.items.tools.ItemNameTag;
import io.Yomicer.magicExpansion.utils.aiManager.AIManager;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.logging.Level;

/**
 * 玩家退出清理监听器：
 * 玩家下线时统一调用各模块暴露的静态 cleanup(UUID)，
 * 清理会话数据，防止各模块 Map 中残留离线玩家数据造成内存泄漏。
 */
public class PlayerCleanupListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // 修复方案：每个模块独立 try-catch，防止单个模块清理失败影响其他模块
        safeCleanup("worldListener.Events", () -> Events.cleanup(uuid));
        safeCleanup("RecipeBookManager", () -> RecipeBookManager.cleanup(uuid));
        // StarShardsSword.cleanup 由另一代理提供（只负责调用）
        safeCleanup("StarShardsSword", () -> StarShardsSword.cleanup(uuid));
        // SlownessManager.cleanup 由另一代理提供（只负责调用）
        safeCleanup("SlownessManager", () -> SlownessManager.cleanup(uuid));
        safeCleanup("CustomSequenceTool", () -> CustomSequenceTool.cleanup(uuid));
        safeCleanup("ShopGUI", () -> ShopGUI.cleanup(uuid));
        safeCleanup("BlackMarketManager", () -> BlackMarketManager.cleanup(uuid));
        safeCleanup("AIManager", () -> AIManager.cleanup(uuid));
        safeCleanup("ItemNameTag", () -> ItemNameTag.cleanup(uuid));
        safeCleanup("ItemEffectAttackListener", () -> ItemEffectAttackListener.cleanup(uuid));
    }

    // 安全执行单个清理任务：捕获异常仅记录日志，不中断其他模块的清理流程
    private void safeCleanup(String module, Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "魔法拓展模块 " + module + " 玩家退出数据清理失败: " + t.getMessage());
        }
    }
}
