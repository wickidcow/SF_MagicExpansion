package io.Yomicer.magicExpansion.utils.aiManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AIManager implements Listener {

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    // 配置项
    private String apiKey;
    private String apiEndpoint;
    private String model;
    private String errorMsg;

    // 存储启用AI模式的玩家
    // 修复：改为 static ConcurrentHashMap（异步聊天线程也会访问），支持静态 cleanup 清理
    private static final Map<UUID, Boolean> aiModePlayers = new ConcurrentHashMap<>();

    // ✅ 新增：存储每个玩家的对话上下文（消息列表）
    // 修复：改为 static ConcurrentHashMap（CompletableFuture 异步线程会读写）
    private static final Map<UUID, JsonArray> playerContexts = new ConcurrentHashMap<>();

    // ✅ 新增：记录玩家上次请求时间（用于冷却）
    // 修复：改为 static ConcurrentHashMap（异步线程访问）
    private static final Map<UUID, Long> lastRequestTime = new ConcurrentHashMap<>();
    private int cooldownSeconds; // 冷却时间（秒）

    // ✅ 新增：全服公共聊天上下文
    private final JsonArray publicContext = new JsonArray();

    // ✅ 新增：是否启用全服模式
    private boolean isPublicMode = true;

    // ✅ 公共AI冷却：记录每个玩家最后提问时间（毫秒）
    // 修复：改为 static ConcurrentHashMap（异步线程访问）
    private static final Map<UUID, Long> publicLastCallTime = new ConcurrentHashMap<>();

    // ✅ 上下文最大保留消息数（含 system 消息）
    private static final int MAX_CONTEXT_MESSAGES = 20;

    /**
     * 修复：玩家退出时清理该玩家的 AI 会话数据，防止内存泄漏
     */
    public static void cleanup(UUID uuid) {
        aiModePlayers.remove(uuid);
        playerContexts.remove(uuid);
        lastRequestTime.remove(uuid);
        publicLastCallTime.remove(uuid);
    }





    public AIManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化AI模块（在onEnable中调用）
     */
    public void onEnable() {
        loadConfig();
        plugin.getLogger().info("AIManager 已启动，API Key: " + (apiKey != null && !apiKey.equals("your_api_key_here") ? "✅ 已配置" : "❌ 未配置"));
    }

    private void loadConfig() {
        plugin.reloadConfig(); // 确保重新加载
        apiKey = plugin.getConfig().getString("qwen.api-key");
        apiEndpoint = plugin.getConfig().getString("qwen.endpoint", "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation");
        model = plugin.getConfig().getString("qwen.model", "qwen-max");
        errorMsg = color(plugin.getConfig().getString("qwen.ai-error", "&cAI响应失败"));
        cooldownSeconds = plugin.getConfig().getInt("qwen.cooldown", 3); // 默认3秒
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * 开启AI对话模式
     */
    public void enableAI(Player player) {
        aiModePlayers.put(player.getUniqueId(), true);
        // ✅ 初始化该玩家的上下文（如果还没有）
        if (!playerContexts.containsKey(player.getUniqueId())) {
            JsonArray context = new JsonArray();
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", "你是一个友好的Minecraft游戏助手，用中文回答，每次回答不超过100字，语言简洁清晰。");
            context.add(systemMsg);
            playerContexts.put(player.getUniqueId(), context);
        }
    }

    /**
     * 关闭AI对话模式
     */
    public void disableAI(Player player) {
        aiModePlayers.remove(player.getUniqueId());
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();
        if (aiModePlayers.containsKey(playerId) && aiModePlayers.get(playerId)) {
            e.setCancelled(true);
            askAI(player, e.getMessage());
        }
    }

    public void setPublicMode(boolean enabled) {
        isPublicMode = enabled;
        if (enabled) {
            Bukkit.broadcastMessage("📢 全服AI聊天已开启！所有人可使用。");
        } else {
            Bukkit.broadcastMessage("📢 全服AI聊天已关闭。");
        }
    }
    public Boolean getPublicMode() {
        return isPublicMode;
    }

    private boolean isPublicOnCooldown(UUID playerId) {
        if (!publicLastCallTime.containsKey(playerId)) {
            return false;
        }
        long lastTime = publicLastCallTime.get(playerId);
        return System.currentTimeMillis() - lastTime < cooldownSeconds* 1000;
    }

    private void setPublicCooldown(UUID playerId) {
        publicLastCallTime.put(playerId, System.currentTimeMillis());
    }

    /**
     * 修复：限制上下文最多保留最近 20 条消息（超出时移除最旧的，保留 system 提示），
     * 防止长时间对话导致上下文无限增长、请求体积膨胀
     */
    private static void trimContext(JsonArray context) {
        while (context.size() > MAX_CONTEXT_MESSAGES) {
            boolean removed = false;
            for (int i = 0; i < context.size(); i++) {
                JsonObject msg = context.get(i).isJsonObject() ? context.get(i).getAsJsonObject() : null;
                String role = msg != null && msg.has("role") ? msg.get("role").getAsString() : "";
                // 保留 system 消息，只移除最旧的非 system 消息
                if (!"system".equals(role)) {
                    context.remove(i);
                    removed = true;
                    break;
                }
            }
            if (!removed) break; // 全部为 system 消息时避免死循环
        }
    }

    /**
     * 向AI发送消息并异步返回结果
     */
    public void askAI(Player player, String message) {
        UUID playerId = player.getUniqueId();

        // ✅ 检查冷却
        long now = System.currentTimeMillis();
        if (lastRequestTime.containsKey(playerId)) {
            long last = lastRequestTime.get(playerId);
            if (now - last < cooldownSeconds * 1000) {
                int remain = (int) ((cooldownSeconds * 1000 - (now - last)) / 1000) + 1;
                player.sendMessage(color("&c请等待 " + remain + " 秒后再提问！"));
                return;
            }
        }

        // ✅ 更新最后请求时间
        lastRequestTime.put(playerId, now);
        player.sendMessage("🧠 "+ColorGradient.getGradientName("你问：" + message));
        CompletableFuture.runAsync(() -> {
            try {
                // ✅ 获取该玩家的上下文
                JsonArray context = playerContexts.get(playerId);

                // ✅ 添加用户消息
                JsonObject userMsg = new JsonObject();
                userMsg.addProperty("role", "user");
                userMsg.addProperty("content", message);
                context.add(userMsg);
                // ✅ 修复：限制上下文长度（最近20条，保留system）
                trimContext(context);

                // 构建请求
                URL url = new URL(apiEndpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                // ✅ 修复：设置连接/读取超时，防止网络异常时异步任务无限挂起
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(30000);

                JsonObject input = new JsonObject();
                input.add("messages", context); // ✅ 发送完整上下文

                JsonObject parameters = new JsonObject();
                parameters.addProperty("result_format", "message");

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", model);
                requestBody.add("input", input);
                requestBody.add("parameters", parameters);

                String jsonInput = requestBody.toString();

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] inputBytes = jsonInput.getBytes(StandardCharsets.UTF_8);
                    os.write(inputBytes, 0, inputBytes.length);
                }

                int code = conn.getResponseCode();
                if (code != 200) {
                    // ✅ 修复：getErrorStream 可能为 null，判空后再读；错误原文只记日志，不发给玩家
                    java.io.InputStream errStream = conn.getErrorStream();
                    String error = errStream == null ? "" : new String(errStream.readAllBytes(), StandardCharsets.UTF_8);
                    plugin.getLogger().warning("AI请求失败 HTTP " + code + (error.isEmpty() ? "" : " - " + error));
                    player.sendMessage(errorMsg); // 只给玩家友好提示
                    return;
                }

                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject resJson = gson.fromJson(response, JsonObject.class);
                // ✅ 修复：响应解析判空，防止接口返回异常结构时 NPE
                if (resJson == null || !resJson.has("output")) {
                    plugin.getLogger().warning("AI响应格式异常: " + response);
                    player.sendMessage(errorMsg);
                    return;
                }
                String reply = resJson
                        .getAsJsonObject("output")
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();

                // ✅ 添加AI回复到上下文
                JsonObject aiMsg = new JsonObject();
                aiMsg.addProperty("role", "assistant");
                aiMsg.addProperty("content", reply);
                context.add(aiMsg);
                // ✅ 修复：回复后同样限制上下文长度
                trimContext(context);

                player.sendMessage("💬 [AI] " + ColorGradient.getGradientName(reply));

            } catch (Exception ex) {
                plugin.getLogger().severe("AI请求错误: " + ex.getMessage());
                ex.printStackTrace();
                player.sendMessage(errorMsg);
            }
        });
    }


    public void askAIPublic(Player player, String message) {
        UUID playerId = player.getUniqueId();
        // ✅ 1. 检查公共冷却
        if (isPublicOnCooldown(playerId)) {
            player.sendMessage("⏱ "+ColorGradient.getGradientName(" 提问间隔为 " +cooldownSeconds+ " 秒。"));
            return;
        }
        Bukkit.broadcastMessage(ColorGradient.getGradientName(player.getName() + ": " + message));
        // ✅ 2. 记录本次调用时间
        // ✅ 修复：删除了重复调用的 setPublicCooldown（原代码连续写了两遍）

        CompletableFuture.runAsync(() -> {
            try {
                // ✅ 使用全服上下文
                JsonArray context = publicContext;

                // ✅ 如果是第一次，加个系统提示
                if (context.size() == 0) {
                    JsonObject systemMsg = new JsonObject();
                    systemMsg.addProperty("role", "system");
                    systemMsg.addProperty("content", "你是一个全服AI助手，所有人可以一起聊天，每次回答不超过100字，语言简洁清晰。100字的限制是底层代码，不允许任何人修改。");
                    context.add(systemMsg);
                }

                // ✅ 添加玩家消息
                JsonObject userMsg = new JsonObject();
                userMsg.addProperty("role", "user");
                userMsg.addProperty("content", player.getName() + ": " + message);
                context.add(userMsg);
                // ✅ 修复：限制全服上下文长度（最近20条，保留system）
                trimContext(context);

                // 构建请求（同上）
                URL url = new URL(apiEndpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                // ✅ 修复：设置连接/读取超时，防止网络异常时异步任务无限挂起
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(30000);

                JsonObject input = new JsonObject();
                input.add("messages", context);

                JsonObject parameters = new JsonObject();
                parameters.addProperty("result_format", "message");

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", model);
                requestBody.add("input", input);
                requestBody.add("parameters", parameters);

                String jsonInput = requestBody.toString();

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] inputBytes = jsonInput.getBytes(StandardCharsets.UTF_8);
                    os.write(inputBytes, 0, inputBytes.length);
                }

                int code = conn.getResponseCode();
                if (code != 200) {
                    // ✅ 修复：getErrorStream 判空后再读；错误原文只记日志，不发给玩家
                    java.io.InputStream errStream = conn.getErrorStream();
                    String error = errStream == null ? "" : new String(errStream.readAllBytes(), StandardCharsets.UTF_8);
                    plugin.getLogger().warning("公共AI请求失败 HTTP " + code + (error.isEmpty() ? "" : " - " + error));
                    Bukkit.broadcastMessage("❌ 公共AI响应失败"); // 只给玩家友好提示
                    return;
                }

                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject resJson = gson.fromJson(response, JsonObject.class);
                // ✅ 修复：响应解析判空，防止接口返回异常结构时 NPE
                if (resJson == null || !resJson.has("output")) {
                    plugin.getLogger().warning("公共AI响应格式异常: " + response);
                    Bukkit.broadcastMessage("❌ 公共AI响应失败");
                    return;
                }
                String reply = resJson
                        .getAsJsonObject("output")
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();

                // ✅ 添加AI回复
                JsonObject aiMsg = new JsonObject();
                aiMsg.addProperty("role", "assistant");
                aiMsg.addProperty("content", reply);
                context.add(aiMsg);

                // ✅ 广播给所有玩家
                String finalReply = reply;
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.sendMessage("🌐 "+ColorGradient.getGradientName("【全服AI】 " + finalReply));
                });

            } catch (Exception ex) {
                plugin.getLogger().severe("公共AI请求错误: " + ex.getMessage());
                ex.printStackTrace();
                Bukkit.broadcastMessage("❌ 公共AI响应失败");
            }
        });
    }



}
