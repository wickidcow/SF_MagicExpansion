package io.Yomicer.magicExpansion.items.misc;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.items.electric.entitykillMachinee.EntityKillMachine;
import io.Yomicer.magicExpansion.utils.VirtualPlayerManager; // G1v3：虚拟玩家管理器
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.*;

import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

public class RightClickMan extends SlimefunItem implements EnergyNetComponent {


    public RightClickMan(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        constructMenu("交互机器人");
        addItemHandler(onBreak());
    }

    private final int[] upBorder = {0,18};
    private final int[] downBorder = {1,19};
    private final int[] eastBorder = {2,20};
    private final int[] southBorder = {3,21};
    private final int[] westBorder = {4,22};
    private final int[] northBorder = {5,23};


    private final int[] inputSlots = {};

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                RightClickMan.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
    }
    protected void tick(Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if(menu != null) {
            // 定义：逻辑槽位 → 显示槽位 → 方向
            Map<Integer, Map.Entry<Integer, BlockFace>> directionMap = new HashMap<>();
            directionMap.put(9,  new AbstractMap.SimpleImmutableEntry<>(0, BlockFace.UP));     // 9 → 0 → UP
            directionMap.put(10, new AbstractMap.SimpleImmutableEntry<>(1, BlockFace.DOWN));   // 10→ 1 → DOWN
            directionMap.put(11, new AbstractMap.SimpleImmutableEntry<>(2, BlockFace.EAST));   // 11→ 2 → EAST
            directionMap.put(12, new AbstractMap.SimpleImmutableEntry<>(3, BlockFace.SOUTH));  // 12→ 3 → SOUTH
            directionMap.put(13, new AbstractMap.SimpleImmutableEntry<>(4, BlockFace.WEST));   // 13→ 4 → WEST
            directionMap.put(14, new AbstractMap.SimpleImmutableEntry<>(5, BlockFace.NORTH));  // 14→ 5 → NORTH

            // 遍历每个方向
            for (Map.Entry<Integer, Map.Entry<Integer, BlockFace>> entry : directionMap.entrySet()) {
                int logicSlot = entry.getKey();           // 控制逻辑的槽位 (9~14)
                int displaySlot = entry.getValue().getKey(); // 显示用的槽位 (0~5)
                BlockFace face = entry.getValue().getValue(); // 对应方向

                Block targetBlock = block.getRelative(face);
                Location targetLocation = targetBlock.getLocation();

                boolean isEnabled = isButtonOn(menu, logicSlot);
                String statusText = isEnabled ? "§a已启用" : "§7未启用";

                ItemStack displayItem;

                SlimefunItem sfItem = StorageCacheUtils.getSfItem(targetLocation);
                if (sfItem != null) {
                    displayItem = sfItem.getItem().clone();
                }
                else if (targetBlock.getType() != Material.AIR) {
                    // 检查材料是否可以作为物品
                    Material blockType = targetBlock.getType();
                    if (isValidItemMaterial(blockType)) {
                        displayItem = new ItemStack(blockType);

                        BlockState state = targetBlock.getState();
                        ItemMeta meta = displayItem.getItemMeta();
                        if (meta instanceof BlockStateMeta blockStateMeta) {
                            blockStateMeta.setBlockState(state);
                            blockStateMeta.setLore(null);
                            displayItem.setItemMeta(blockStateMeta);
                        }
                    } else {
                        // 对于不能作为物品的方块，使用屏障并显示方块信息
                        displayItem = createBlockDisplayItem(blockType, targetBlock, statusText);
                    }
                }
                else {
                    displayItem = new ItemStack(Material.BARRIER);
                    ItemMeta meta = displayItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName("§7空气");
                        meta.setLore(Arrays.asList(
                                "§7坐标: " + targetBlock.getX() + "," + targetBlock.getY() + "," + targetBlock.getZ(),
                                "§7状态: " + statusText,
                                "§8（此处为空气）"
                        ));
                        displayItem.setItemMeta(meta);
                    }
                    menu.replaceExistingItem(displaySlot, displayItem);
                    continue;
                }

                // ====== 4. 对非 AIR 的正常方块，统一设置显示信息 ======
                setBlockDisplayWithInfo(displayItem, targetBlock, statusText);

                // 更新 GUI
                menu.replaceExistingItem(displaySlot, displayItem);
            }

            // 定义槽位与方向的映射（槽位 → 是否启用）
            boolean up = isButtonOn(menu, 9);
            boolean down = isButtonOn(menu, 10);
            boolean east = isButtonOn(menu, 11);
            boolean south = isButtonOn(menu, 12);
            boolean west = isButtonOn(menu, 13);
            boolean north = isButtonOn(menu, 14);

            // G2 修复：全量 getPlayers() 距离过滤+排序降频为每 10 tick 一次，结果缓存复用
            NearestPlayerCache playerCache = nearestPlayerCache.computeIfAbsent(
                    block.getLocation(), k -> new NearestPlayerCache());
            long now = System.currentTimeMillis();
            if (now >= playerCache.nextScanAt) {
                playerCache.nextScanAt = now + PLAYER_SCAN_INTERVAL_MS;
                playerCache.player = findNearestPlayer(block.getLocation());
            }
            Player nearestPlayer = playerCache.player;

            // G1v3（按需求）：优先使用虚拟玩家执行模拟右键；
            // 虚拟玩家不可用时回退"附近真实玩家"，两者皆无才跳过本 tick
            boolean hasVirtual = VirtualPlayerManager.obtainVirtual() != null;
            if (!hasVirtual && nearestPlayer == null) return;

            String directions = "";

            if (up) {
                interactTarget(nearestPlayer, block.getLocation().clone().add(0, 1, 0).getBlock());
                directions += "上 ";
            }

            if (down) {
                interactTarget(nearestPlayer, block.getLocation().clone().add(0, -1, 0).getBlock());
                directions += "下 ";
            }

            if (east) {
                interactTarget(nearestPlayer, block.getLocation().clone().add(1, 0, 0).getBlock());
                directions += "东 ";
            }

            if (south) {
                interactTarget(nearestPlayer, block.getLocation().clone().add(0, 0, 1).getBlock());
                directions += "南 ";
            }

            if (west) {
                interactTarget(nearestPlayer, block.getLocation().clone().add(-1, 0, 0).getBlock());
                directions += "西 ";
            }

            if (north) {
                interactTarget(nearestPlayer, block.getLocation().clone().add(0, 0, -1).getBlock());
                directions += "北";
            }

            // 更新状态显示
            if (menu.hasViewer()) {
                // G1v3：模拟玩家显示——优先展示虚拟玩家及其模式，其次展示回退用的附近真实玩家
                String actorLine;
                if (hasVirtual) {
                    // 虚拟玩家模式：显示傀儡名与实现方式（citizens / nms）
                    actorLine = "§d" + VirtualPlayerManager.BOT_NAME + " §7(虚拟·" + VirtualPlayerManager.getMode() + ")";
                } else if (nearestPlayer != null) {
                    // 回退模式：显示附近真实玩家名
                    actorLine = nearestPlayer.getName() + " §7(回退)";
                } else {
                    actorLine = null;
                }
                if (actorLine != null) {
                    menu.replaceExistingItem(16, new CustomItemStack(Material.PINK_CANDLE, "§b交互机器人",
                            "§b工作类型：§e右键交互方块",
                            "§b交互速度：§e1次/粘液刻",
                            "§b模拟玩家：§e" + actorLine,
                            "§b模拟方向：§e" + directions.trim(),
                            "§b耗电速度：§e这个机器人不花电的",
                            "§b电量存储：§e这个机器人不储存电"));
                } else {
                    menu.replaceExistingItem(16, new CustomItemStack(Material.PINK_CANDLE, "§b交互机器人",
                            "§b工作类型：§e右键交互方块",
                            "§b交互速度：§e1次/粘液刻",
                            "§c未检测到虚拟玩家或附近玩家",
                            "§b耗电速度：§e这个机器人不花电的",
                            "§b电量存储：§e这个机器人不储存电"));
                }
            }
        }
    }

    // ==================== G2 性能修复：邻近玩家扫描节流 ====================
    private static final long PLAYER_SCAN_INTERVAL_MS = 500; // 扫描间隔 ≈ 10 tick
    // 按机器位置缓存最近玩家：nextScanAt 为下次扫描时间，player 为缓存结果
    private final Map<Location, NearestPlayerCache> nearestPlayerCache = new HashMap<>();

    private static final class NearestPlayerCache {
        long nextScanAt;
        Player player; // 可能为 null 表示附近没有玩家
    }

    // G2: 全量 getPlayers() 距离过滤 + 排序（取最近一名玩家）
    private Player findNearestPlayer(Location center) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Player player : center.getWorld().getPlayers()) {
            double dist = player.getLocation().distanceSquared(center);
            if (dist <= 2500 && dist < nearestDist) { // 与原逻辑一致：50 格内取最近
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    /**
     * G1v3（按需求）：模拟右键改为由"虚拟玩家"发起。
     * 优先级：Citizens NPC（方案A）→ NMS 反射假玩家（方案B）→ 附近真实玩家（回退）。
     * 虚拟玩家在每次交互前被传送到目标方块旁，保证事件处理器读到的位置正确；
     * 事件仍为 PlayerInteractEvent(RIGHT_CLICK_BLOCK)，由各插件交互监听器自行响应。
     *
     * @param fallbackPlayer 虚拟玩家不可用时的回退对象（附近真实玩家，可能为 null）
     * @param targetBlock    目标方块
     */
    private void interactTarget(Player fallbackPlayer, Block targetBlock) {
        // 1) 获取执行者：优先虚拟玩家
        Player actor = VirtualPlayerManager.obtainVirtual();
        boolean isVirtual = actor != null;
        if (!isVirtual) {
            // 回退模式：使用附近真实玩家
            actor = fallbackPlayer;
            if (actor == null) return;
        }

        // 2) 领地保护检查：无交互权限的玩家不触发。
        //    虚拟玩家（NPC/假玩家）的权限语义由领地插件的 NPC 组配置决定，
        //    检查抛异常时：真实玩家按拒绝处理，虚拟玩家按放行处理（避免 NPC 组配置导致功能整体失效）
        try {
            if (!Slimefun.getProtectionManager().hasPermission(actor, targetBlock.getLocation(),
                    Interaction.INTERACT_BLOCK)) {
                return;
            }
        } catch (Throwable t) {
            if (!isVirtual) return;
        }

        // 3) 虚拟玩家传送到目标方块上方（无真实玩家依附，机器人可独立于真人运行）
        if (isVirtual) {
            VirtualPlayerManager.moveVirtual(targetBlock.getLocation().add(0.5, 1.0, 0.5));
        }

        // 4) 模拟主手右键目标方块（手持 AIR、BlockFace.SELF，与原实现一致）
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(
                actor,
                Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.AIR),
                targetBlock,
                BlockFace.SELF
        );
        Bukkit.getPluginManager().callEvent(interactEvent);
    }

    /**
     * 检查材料是否可以作为有效的物品
     */
    private boolean isValidItemMaterial(Material material) {
        // 墙上的标志牌、双台阶等不能作为物品
        return material.isItem() && !material.name().contains("WALL_") && !material.name().contains("DOUBLE_");
    }

    /**
     * 为不能作为物品的方块创建显示物品
     */
    private ItemStack createBlockDisplayItem(Material blockType, Block block, String statusText) {
        ItemStack displayItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = displayItem.getItemMeta();

        if (meta != null) {
            // 获取方块的友好名称
            String blockName = blockType.name().toLowerCase().replace("_", " ");
            meta.setDisplayName("§7" + blockName);

            List<String> lore = new ArrayList<>();
            lore.add("§7坐标: " + block.getX() + "," + block.getY() + "," + block.getZ());
            lore.add("§7状态: " + statusText);
            lore.add("§8（此方块无法作为物品显示）");

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                return p.hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager().hasPermission(p, b.getLocation(),
                        Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
                return new int[]{

                };
            }
        };
    }



    protected void constructMenu(BlockMenuPreset preset) {
        // 按钮状态
        final Material BUTTON_OFF = Material.BARRIER;
        final Material BUTTON_ON = Material.LANTERN;

        int[] controlSlots = {9, 10, 11, 12, 13, 14};
        int[] directionSlots = {6, 7, 8, 24, 25, 26};

        for (int slot : controlSlots) {
            preset.addItem(slot, new CustomItemStack(BUTTON_OFF, "§7未激活", "§e点击激活"),
                    (player, slot1, item, action) -> {
                        // G3 修复：删除原此处 getTargetBlock 结果从未使用的死代码行
                        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BlockMenu menu)) {
                            return false;
                        }
                        ItemStack currentItem = menu.getItemInSlot(slot1);

                        if (currentItem != null && currentItem.getType() == BUTTON_ON) {
                            // ON → OFF
                            menu.replaceExistingItem(slot1, new CustomItemStack(BUTTON_OFF, "§7未激活", "§e点击激活"));
                        } else {
                            // OFF 或空 → ON
                            menu.replaceExistingItem(slot1, new CustomItemStack(BUTTON_ON, "§a已激活", "§e点击关闭"));
                        }

                        return false; // 阻止拿走
                    });
        }



        for (int i : directionSlots) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.BLUE_CANDLE)), "§9装饰蜡烛", "§7仅用于美观","§7中心为机器状态"),
                    (p, slot, item, action) -> false);
        }

        preset.addItem(15, new CustomItemStack(Material.BLUE_CANDLE, "§9装饰蜡烛", "§7仅用于美观","§7中心为机器状态"),
                (player, slot, item, action) -> false); // 点击无反应

        preset.addItem(17, new CustomItemStack(Material.BLUE_CANDLE, "§9装饰蜡烛", "§7仅用于美观","§7中心为机器状态"),
                (player, slot, item, action) -> false); // 点击无反应

        // ====== 边框说明文字（不可点击）======
        for (int i : upBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.WHITE_CONCRETE)), "§e向上", "§b此处点击，机器人与上方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : downBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.BLACK_CONCRETE)), "§e向下", "§b此处点击，机器人与下方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : eastBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.RED_CONCRETE)), "§e向东", "§b此处点击，机器人与东方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : southBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.BLUE_CONCRETE)), "§e向南", "§b此处点击，机器人与南方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : westBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.GREEN_CONCRETE)), "§e向西", "§b此处点击，机器人与西方方块交互"),
                    (p, slot, item, action) -> false);
        }
        for (int i : northBorder) {
            preset.addItem(i, new CustomItemStack(doGlow(new ItemStack(Material.YELLOW_CONCRETE)), "§e向北", "§b此处点击，机器人与北方方块交互"),
                    (p, slot, item, action) -> false);
        }

        // 状态显示槽（中间）
        preset.addItem(16, new CustomItemStack(Material.RED_CANDLE, " "),
                (p, slot, item, action) -> false);
    }
    private boolean isButtonOn(BlockMenu menu, int slot) {
        ItemStack item = menu.getItemInSlot(slot);
        return item != null && item.getType() == Material.LANTERN; // 注意：这里我们直接用 LANTERN 判断
    }

    /**
     * 设置物品显示：继承原显示名和Lore，并追加坐标和状态信息
     */
    private void setBlockDisplayWithInfo(ItemStack item, Block block, String statusLine) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 1. 设置显示名（使用 ItemStackHelper 获取友好名称）
        String displayName = ItemStackHelper.getDisplayName(item);
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();

        // 2. 保留原有 Lore（如果有）
        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
        }

        // 3. 添加分隔线（如果已有 Lore 或要添加新信息）
        if (!lore.isEmpty() || statusLine != null) {
            lore.add("§8---");
        }

        // 4. 追加坐标
        lore.add("§7坐标: " + block.getX() + "," + block.getY() + "," + block.getZ());

        // 5. 追加状态（可选）
        if (statusLine != null) {
            lore.add("§7状态: " + statusLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }



    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack i, @Nonnull List<ItemStack> list) {
                Block b = e.getBlock();
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());

                if (inv != null) {
                    inv.dropItems(b.getLocation(), inputSlots);
                }

            }
        };
    }

    @Override
    public @NotNull EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.NONE;
    }

    @Override
    public int getCapacity() {
        return 0;
    }
}
