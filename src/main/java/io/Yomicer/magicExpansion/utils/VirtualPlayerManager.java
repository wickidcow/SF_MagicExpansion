package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 虚拟玩家管理器（RightClickMan 交互机器人专用）。
 *
 * 双方案（按用户需求实现 A+B）：
 *  方案 A：Citizens NPC —— 检测到 Citizens2 前置时，用反射创建一个隐形 NPC，
 *          其 getEntity() 即 Player 实例，可直接构造 PlayerInteractEvent；
 *  方案 B：NMS 反射假玩家 —— 无 Citizens 时，用反射向世界注入一个 ServerPlayer 假玩家
 *          （Paper 1.20.4 实测路径，版本敏感：服务端升级后可能需要适配映射）；
 *  回退  ：两者都失败时返回 null，调用方自动回退到"附近真实玩家"模式。
 *
 * 虚拟玩家全局仅创建一个（所有交互机器人共用，tick 均在主线程串行执行，
 * 每次交互前将其传送到目标方块旁，因此不存在并发竞争）。
 */
public final class VirtualPlayerManager {

    /** 虚拟玩家显示名（GameProfile 名限制 16 字符 ASCII，含颜色码前缀仅用于显示名） */
    public static final String BOT_NAME = "MagicBot";

    /** 当前生效模式：citizens / nms / none（全部失败，回退真实玩家） */
    private static String mode = "none";

    /** 缓存的虚拟玩家 Bukkit 实体（ Citizens 时为 NPC 的 Player 实体；NMS 时为 CraftPlayer） */
    private static Player virtualPlayer;

    /** Citizens NPC 对象（反射持有，避免编译期依赖 Citizens） */
    private static Object citizensNpc;

    /** NMS 假玩家对象（反射持有 ServerPlayer） */
    private static Object nmsFakePlayer;

    /** 是否已完成初始化（只尝试一次，失败后进入回退模式） */
    private static boolean initialized = false;

    /** join 隐藏监听器是否已注册（虚拟玩家需要对所有在线/新加入玩家隐藏） */
    private static boolean joinListenerRegistered = false;

    private VirtualPlayerManager() {
    }

    /**
     * 获取虚拟玩家；三种模式按优先级初始化，失败自动降级。
     *
     * @return 虚拟玩家 Player 实例；全部方案失败时返回 null（调用方回退附近真实玩家）
     */
    public static synchronized Player obtainVirtual() {
        if (!initialized) {
            initialized = true;
            // 方案 A：优先尝试 Citizens NPC（稳定、版本无关）
            if (tryInitCitizens()) {
                mode = "citizens";
            } else if (tryInitNmsFakePlayer()) {
                // 方案 B：无 Citizens 时尝试 NMS 反射假玩家（Paper 1.20.4 实测路径）
                mode = "nms";
            } else {
                // 双双失败：进入回退模式，由调用方使用附近真实玩家
                mode = "none";
                log("虚拟玩家初始化失败（Citizens 未安装且 NMS 反射不适配当前服务端），交互机器人回退为附近玩家模式");
            }
            if (virtualPlayer != null) {
                log("虚拟玩家初始化成功，模式=" + mode + "，名称=" + BOT_NAME);
                hideFromAll(); // 生成后对所有在线玩家隐藏
                registerJoinHider(); // 新玩家加入时也隐藏
            }
        }
        return virtualPlayer;
    }

    /** 当前虚拟玩家模式（供 UI 显示与调试） */
    public static String getMode() {
        return mode;
    }

    /** 虚拟玩家是否可用 */
    public static boolean isAvailable() {
        return obtainVirtual() != null;
    }

    /**
     * 将虚拟玩家传送到目标位置（每次交互前调用，保证事件处理器读到的位置正确）。
     */
    public static void moveVirtual(Location anchor) {
        if (virtualPlayer != null && virtualPlayer.isOnline()) {
            try {
                // 传送到目标方块旁；不设置视角旋转，保持默认朝向
                virtualPlayer.teleport(anchor);
            } catch (Throwable ignored) {
                // 传送失败不影响本次模拟右键
            }
        }
    }

    // ==================== 方案 A：Citizens NPC（反射实现） ====================

    /**
     * 方案 A：通过反射调用 Citizens2 API 创建隐形 NPC。
     * 反射调用链：CitizensAPI.getNPCRegistry() → registry.createNPC(PLAYER, BOT_NAME)
     *             → npc.setPersistent(false) → npc.setProtected(true) → npc.spawn(loc) → npc.getEntity()
     */
    private static boolean tryInitCitizens() {
        try {
            // 前置检查：Citizens 插件必须已启用
            if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
                return false;
            }
            Class<?> citizensApi = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = citizensApi.getMethod("getNPCRegistry").invoke(null);

            // 创建 NPC：类型 PLAYER，名字为 BOT_NAME
            Method createNpc = findMethod(registry.getClass(), "createNPC", 2);
            citizensNpc = createNpc.invoke(registry, EntityType.PLAYER, BOT_NAME);
            if (citizensNpc == null) return false;
            Class<?> npcClass = citizensNpc.getClass();

            // 不持久化：防止 NPC 被写入 Citizens 存储文件（插件卸载后不残留）
            invokeIfExists(npcClass, citizensNpc, "setPersistent", boolean.class, false);
            // 受保护：不会被玩家/怪物伤害
            invokeIfExists(npcClass, citizensNpc, "setProtected", boolean.class, true);

            // 在主世界出生点附近生成 NPC（随后每次交互前会被传送到目标方块旁）
            World world = Bukkit.getWorlds().get(0);
            Location spawnLoc = world.getSpawnLocation().add(0, 1, 0);
            Object spawned = findMethod(npcClass, "spawn", 1).invoke(citizensNpc, spawnLoc);
            if (spawned == null || !((Boolean) spawned)) return false;

            // NPC 实体即 Player 实例（Citizens 的玩家型 NPC 实现了完整 Player 接口）
            Object entity = findMethod(npcClass, "getEntity", 0).invoke(citizensNpc);
            if (!(entity instanceof Player player)) return false;

            // 隐形 + 无碰撞 + 免疫伤害 + 不消失
            setInvisibleAndImmortal(player);

            virtualPlayer = player;
            return true;
        } catch (Throwable t) {
            log("方案 A（Citizens NPC）初始化失败：" + t);
            return false;
        }
    }

    // ==================== 方案 B：NMS 反射假玩家 ====================

    /**
     * 方案 B：通过反射向世界注入一个 NMS ServerPlayer 假玩家（Paper 1.20.4 实测路径）。
     *
     * 反射调用链：
     *   CraftServer.getServer() → MinecraftServer
     *   CraftWorld.getHandle() → ServerLevel
     *   new ServerPlayer(minecraftServer, serverLevel, GameProfile, ClientInformation.createDefault())
     *   fakePlayer.connection = new ServerGamePacketListenerImpl(minecraftServer, new Connection(SERVERBOUND), fakePlayer)
     *   serverLevel.addNewPlayer(fakePlayer)
     *   fakePlayer.getBukkitEntity() → CraftPlayer（即 Player）
     *
     * ⚠ 版本敏感：Paper 1.20.4 使用 Mojang 类名；服务端大版本升级后本方法可能需要适配。
     *   所有反射调用均逐步容错，任一步失败即整体回退。
     */
    private static boolean tryInitNmsFakePlayer() {
        try {
            // 1) Bukkit server → NMS MinecraftServer（CraftServer.getServer()）
            Class<?> craftServerClass = getCraftClass("org.bukkit.craftbukkit.CraftServer");
            Object nmsServer = craftServerClass.getMethod("getServer").invoke(Bukkit.getServer());

            // 2) CraftWorld → NMS ServerLevel
            World world = Bukkit.getWorlds().get(0);
            Class<?> craftWorldClass = getCraftClass("org.bukkit.craftbukkit.CraftWorld");
            Object serverLevel = craftWorldClass.getMethod("getHandle").invoke(world);

            // 3) GameProfile（名字限制 16 字符 ASCII）
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Object gameProfile = gameProfileClass
                    .getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), BOT_NAME);

            // 4) ClientInformation.createDefault()（1.19.3+ ServerPlayer 构造必需）
            Class<?> clientInfoClass = Class.forName("net.minecraft.server.level.ClientInformation");
            Object clientInfo = clientInfoClass.getMethod("createDefault").invoke(null);

            // 5) new ServerPlayer(minecraftServer, serverLevel, gameProfile, clientInfo)
            Class<?> serverPlayerClass = Class.forName("net.minecraft.server.level.ServerPlayer");
            nmsFakePlayer = serverPlayerClass
                    .getConstructor(
                            Class.forName("net.minecraft.server.MinecraftServer"),
                            Class.forName("net.minecraft.server.level.ServerLevel"),
                            gameProfileClass,
                            clientInfoClass)
                    .newInstance(nmsServer, serverLevel, gameProfile, clientInfo);

            // 6) 构造空的网络连接并挂到假玩家上（ServerGamePacketListenerImpl）
            Class<?> connectionClass = Class.forName("net.minecraft.network.Connection");
            Class<?> packetFlowClass = Class.forName("net.minecraft.network.protocol.PacketFlow");
            Object serverboundFlow = packetFlowClass.getField("SERVERBOUND").get(null);
            Object connection = connectionClass.getConstructor(packetFlowClass).newInstance(serverboundFlow);
            Class<?> listenerClass = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
            Object packetListener = listenerClass
                    .getConstructor(
                            Class.forName("net.minecraft.server.MinecraftServer"),
                            connectionClass,
                            serverPlayerClass)
                    .newInstance(nmsServer, connection, nmsFakePlayer);
            serverPlayerClass.getField("connection").set(nmsFakePlayer, packetListener);

            // 7) 将假玩家加入世界（ServerLevel.addNewPlayer）
            serverLevel.getClass().getMethod("addNewPlayer", serverPlayerClass)
                    .invoke(serverLevel, nmsFakePlayer);

            // 8) 获取 Bukkit 侧 Player 实体（CraftPlayer）
            Object bukkitEntity = serverPlayerClass.getMethod("getBukkitEntity").invoke(nmsFakePlayer);
            if (!(bukkitEntity instanceof Player player)) return false;

            // 隐形 + 无碰撞 + 免疫伤害
            setInvisibleAndImmortal(player);

            virtualPlayer = player;
            return true;
        } catch (Throwable t) {
            log("方案 B（NMS 反射假玩家）初始化失败：" + t);
            nmsFakePlayer = null;
            return false;
        }
    }

    // ==================== 公共工具方法 ====================

    /** 设置隐形 + 无碰撞 + 免疫伤害 + 不自动消失（NPC 与 NMS 假玩家共用） */
    private static void setInvisibleAndImmortal(Player player) {
        try {
            player.setInvisible(true);          // 隐形
            player.setCollidable(false);        // 无碰撞，不阻挡玩家移动
            player.setInvulnerable(true);       // 免疫伤害
            player.setRemoveWhenFarAway(false); // 不因玩家远离而消失
            player.setSilent(true);             // 不发出声音
            player.setAllowFlight(true);        // 允许飞行（传送时不受重力下落影响）
            player.setFlying(true);             // 悬浮，避免传送后坠落
        } catch (Throwable t) {
            log("虚拟玩家属性设置部分失败：" + t);
        }
    }

    /** 对所有在线玩家隐藏虚拟玩家 */
    private static void hideFromAll() {
        if (virtualPlayer == null) return;
        for (Player online : Bukkit.getOnlinePlayers()) {
            try {
                online.hidePlayer(MagicExpansion.getInstance(), virtualPlayer);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * 注册 PlayerJoinEvent 监听器：新玩家加入服务器时也隐藏虚拟玩家。
     * 仅在虚拟玩家成功创建后注册一次。
     */
    private static void registerJoinHider() {
        if (joinListenerRegistered) return;
        joinListenerRegistered = true;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                if (virtualPlayer != null) {
                    try {
                        event.getPlayer().hidePlayer(MagicExpansion.getInstance(), virtualPlayer);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }, MagicExpansion.getInstance());
    }

    /** 反射查找指定名称与参数个数的方法（跨 Citizens 版本兼容） */
    private static Method findMethod(Class<?> clazz, String name, int paramCount) throws NoSuchMethodException {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                m.setAccessible(true);
                return m;
            }
        }
        throw new NoSuchMethodException(clazz.getSimpleName() + "." + name + "/" + paramCount);
    }

    /** 反射调用存在则执行、不存在则跳过的方法（如 setPersistent，跨版本容错） */
    private static void invokeIfExists(Class<?> clazz, Object target, String name, Class<?> paramType, Object arg) {
        try {
            Method m = clazz.getMethod(name, paramType);
            m.setAccessible(true);
            m.invoke(target, arg);
        } catch (Throwable ignored) {
            // 方法不存在或调用失败：跳过（仅影响非关键属性）
        }
    }

    /**
     * 获取 CraftBukkit 类：优先无版本包名（Paper 1.20.2+），
     * 失败则尝试带版本后缀（旧版 Spigot/Paper，如 v1_20_R3）。
     */
    private static Class<?> getCraftClass(String path) throws ClassNotFoundException {
        try {
            return Class.forName(path); // Paper 1.20.2+：无版本包
        } catch (ClassNotFoundException ignored) {
            // 旧版：插入 v1_20_R3 版本段（org.bukkit.craftbukkit.v1_20_R3.CraftServer）
            String pkg = path.substring("org.bukkit.craftbukkit.".length());
            String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            return Class.forName("org.bukkit.craftbukkit." + ver + "." + pkg);
        }
    }

    /** 输出日志（带插件前缀） */
    private static void log(String message) {
        Logger logger = MagicExpansion.getInstance().getLogger();
        logger.info("[虚拟玩家] " + message);
    }

    /**
     * 插件卸载时的清理：反生成 Citizens NPC（setPersistent(false) 不会存盘，此处仅移除实体）。
     * NMS 假玩家随服务器关闭自动清理，无需额外处理。
     */
    public static synchronized void shutdown() {
        if (citizensNpc != null) {
            try {
                findMethod(citizensNpc.getClass(), "despawn", 0).invoke(citizensNpc);
            } catch (Throwable ignored) {
            }
        }
        virtualPlayer = null;
        citizensNpc = null;
        nmsFakePlayer = null;
        initialized = false;
    }
}
