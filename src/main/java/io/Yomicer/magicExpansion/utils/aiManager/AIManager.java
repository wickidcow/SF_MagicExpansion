package io.Yomicer.magicExpansion.utils.aiManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional Qwen/DashScope chat integration.
 *
 * <p>The feature is disabled by default and never blocks the server thread while waiting for an
 * HTTP response.</p>
 */
public final class AIManager implements Listener {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;
    private static final int MAX_CONTEXT_MESSAGES = 20;

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();
    private final Set<UUID> privateModePlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, JsonArray> privateContexts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> privateCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> publicCooldowns = new ConcurrentHashMap<>();
    private final Object publicContextLock = new Object();
    private final JsonArray publicContext = new JsonArray();

    private volatile boolean enabled;
    private volatile boolean publicMode;
    private volatile String apiKey;
    private volatile String endpoint;
    private volatile String model;
    private volatile String errorMessage;
    private volatile int cooldownSeconds;

    public AIManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        reloadSettings();
        if (!enabled) {
            plugin.getLogger().info("Optional AI chat is disabled in config.yml.");
        } else if (!hasValidApiKey()) {
            plugin.getLogger().warning("AI chat is enabled, but no valid API key is configured.");
        } else {
            plugin.getLogger().info("Optional AI chat is enabled and configured.");
        }
    }

    public void reloadSettings() {
        plugin.reloadConfig();
        enabled = plugin.getConfig().getBoolean("qwen.enabled", false);
        publicMode = plugin.getConfig().getBoolean("qwen.public-mode", false);
        apiKey = plugin.getConfig().getString("qwen.api-key", "");
        endpoint = plugin.getConfig().getString(
                "qwen.endpoint",
                "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation");
        model = plugin.getConfig().getString("qwen.model", "qwen-plus");
        errorMessage = color(plugin.getConfig().getString(
                "qwen.ai-error", "&cAI request failed. Please try again later."));
        cooldownSeconds = Math.max(1, plugin.getConfig().getInt("qwen.cooldown", 3));
    }

    public boolean isConfigured() {
        return enabled && hasValidApiKey() && endpoint != null && !endpoint.isBlank();
    }

    private boolean hasValidApiKey() {
        return apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equalsIgnoreCase("your_api_key_here");
    }

    public boolean enableAI(Player player) {
        if (!isConfigured()) {
            return false;
        }

        privateModePlayers.add(player.getUniqueId());
        privateContexts.computeIfAbsent(player.getUniqueId(), ignored -> newContext(
                "You are a helpful assistant for a Minecraft server. Keep replies clear, safe, "
                        + "and under 100 words unless the player asks for more detail."));
        return true;
    }

    public void disableAI(Player player) {
        privateModePlayers.remove(player.getUniqueId());
    }

    public boolean getPublicMode() {
        return publicMode;
    }

    public void setPublicMode(boolean enabled) {
        if (!isConfigured()) {
            return;
        }

        publicMode = enabled;
        Bukkit.broadcastMessage(enabled
                ? "§a[MagicExpansion AI] Public AI chat enabled."
                : "§e[MagicExpansion AI] Public AI chat disabled.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!privateModePlayers.contains(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        askAI(player, event.getMessage());
    }

    public void askAI(Player player, String message) {
        if (!validateRequest(player, privateCooldowns)) {
            return;
        }

        String cleanMessage = sanitizeMessage(message);
        if (cleanMessage.isEmpty()) {
            player.sendMessage("§cEnter a message for the AI.");
            return;
        }

        UUID playerId = player.getUniqueId();
        privateCooldowns.put(playerId, System.currentTimeMillis());
        player.sendMessage("🧠 " + ColorGradient.getGradientName("You asked: " + cleanMessage));

        CompletableFuture.runAsync(() -> {
            JsonArray context = privateContexts.computeIfAbsent(playerId, ignored -> newContext(
                    "You are a helpful assistant for a Minecraft server. Keep replies clear, safe, "
                            + "and under 100 words unless the player asks for more detail."));
            try {
                String reply;
                synchronized (context) {
                    addMessage(context, "user", cleanMessage);
                    reply = requestReply(context);
                    addMessage(context, "assistant", reply);
                    trimContext(context);
                }
                sendOnMainThread(player, "💬 §b[AI] §f" + reply);
            } catch (Exception ex) {
                logRequestFailure(ex);
                sendOnMainThread(player, errorMessage);
            }
        });
    }

    public void askAIPublic(Player player, String message) {
        if (!publicMode) {
            player.sendMessage("§cPublic AI mode is disabled.");
            return;
        }
        if (!validateRequest(player, publicCooldowns)) {
            return;
        }

        String cleanMessage = sanitizeMessage(message);
        if (cleanMessage.isEmpty()) {
            player.sendMessage("§cEnter a message for the AI.");
            return;
        }

        publicCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(
                ColorGradient.getGradientName(player.getName() + ": " + cleanMessage)));

        CompletableFuture.runAsync(() -> {
            try {
                String reply;
                synchronized (publicContextLock) {
                    if (publicContext.size() == 0) {
                        addMessage(publicContext, "system",
                                "You are the public assistant for a Minecraft server. Multiple players can read "
                                        + "your reply. Keep it safe, friendly, and under 100 words.");
                    }
                    addMessage(publicContext, "user", player.getName() + ": " + cleanMessage);
                    reply = requestReply(publicContext);
                    addMessage(publicContext, "assistant", reply);
                    trimContext(publicContext);
                }
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(
                        "🌐 §b[Public AI] §f" + reply));
            } catch (Exception ex) {
                logRequestFailure(ex);
                sendOnMainThread(player, errorMessage);
            }
        });
    }

    private boolean validateRequest(Player player, Map<UUID, Long> cooldowns) {
        if (!isConfigured()) {
            player.sendMessage("§cAI chat is disabled or not configured on this server.");
            return false;
        }

        long now = System.currentTimeMillis();
        Long previous = cooldowns.get(player.getUniqueId());
        if (previous == null) {
            return true;
        }

        long remainingMillis = cooldownSeconds * 1000L - (now - previous);
        if (remainingMillis <= 0) {
            return true;
        }

        long remainingSeconds = Math.max(1, (remainingMillis + 999L) / 1000L);
        player.sendMessage("§cPlease wait " + remainingSeconds + " seconds before asking again.");
        return false;
    }

    private String requestReply(JsonArray context) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);

            JsonObject input = new JsonObject();
            input.add("messages", context.deepCopy());

            JsonObject parameters = new JsonObject();
            parameters.addProperty("result_format", "message");

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.add("input", input);
            requestBody.add("parameters", parameters);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (status < 200 || status >= 300) {
                throw new IOException("AI endpoint returned HTTP " + status + formatResponseDetail(body));
            }

            JsonObject response = gson.fromJson(body, JsonObject.class);
            return extractReply(response);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String extractReply(JsonObject response) throws IOException {
        try {
            JsonArray choices = response.getAsJsonObject("output").getAsJsonArray("choices");
            JsonElement content = choices.get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content");
            String reply = content.getAsString().trim();
            if (reply.isEmpty()) {
                throw new IOException("AI endpoint returned an empty response");
            }
            return reply;
        } catch (RuntimeException ex) {
            throw new IOException("AI endpoint returned an unexpected response format", ex);
        }
    }

    private static JsonArray newContext(String systemPrompt) {
        JsonArray context = new JsonArray();
        addMessage(context, "system", systemPrompt);
        return context;
    }

    private static void addMessage(JsonArray context, String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        context.add(message);
    }

    private static void trimContext(JsonArray context) {
        while (context.size() > MAX_CONTEXT_MESSAGES + 1) {
            context.remove(1);
        }
    }

    private static String sanitizeMessage(String message) {
        return message == null ? "" : message.strip();
    }

    private static String formatResponseDetail(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String compact = body.replaceAll("\\s+", " ").trim();
        if (compact.length() > 300) {
            compact = compact.substring(0, 300) + "...";
        }
        return ": " + compact;
    }

    private void sendOnMainThread(Player player, String message) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(message);
            }
        });
    }

    private void logRequestFailure(Exception exception) {
        plugin.getLogger().warning("AI request failed: " + exception.getMessage());
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }

    public void shutdown() {
        privateModePlayers.clear();
        privateContexts.clear();
        privateCooldowns.clear();
        publicCooldowns.clear();
        synchronized (publicContextLock) {
            while (publicContext.size() > 0) {
                publicContext.remove(publicContext.size() - 1);
            }
        }
    }
}
