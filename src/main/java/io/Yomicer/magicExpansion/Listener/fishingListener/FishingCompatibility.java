package io.Yomicer.magicExpansion.Listener.fishingListener;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Routes both rod families through one decision before any catch or bait is changed. */
public final class FishingCompatibility implements Listener {
    private static final String CONFIG_PATH = "Fish.Compatibility.";
    private static final Set<String> KNOWN_PLUGINS = Set.of(
            "pyrofishing", "pyrofishingpro", "betterfish", "betterfishing",
            "evenmorefish", "customfishing", "ultimatefishing");

    private enum Mode { AUTO, FULL, COMPATIBILITY }

    private final JavaPlugin owner;
    private final List<Consumer<PlayerFishEvent>> catchHandlers;
    private Set<String> pluginNames = KNOWN_PLUGINS;
    private Mode mode = Mode.AUTO;
    private volatile State state = new State(false, List.of());

    private record State(boolean handlesCatches, List<String> detectedPlugins) {}

    @SafeVarargs
    public FishingCompatibility(JavaPlugin owner, Consumer<PlayerFishEvent>... catchHandlers) {
        this.owner = owner;
        this.catchHandlers = List.of(catchHandlers);
        reload();
    }

    /** Reads only the fishing compatibility settings; callers may reload config.yml first. */
    public void reload() {
        FileConfiguration config = owner.getConfig();
        String configured = config.getString(CONFIG_PATH + "mode", "AUTO");
        try {
            mode = Mode.valueOf(configured.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException invalidMode) {
            mode = Mode.COMPATIBILITY;
            owner.getLogger().warning("Invalid Fish.Compatibility.mode: " + configured
                    + ". Using COMPATIBILITY to leave fishing catches untouched.");
        }
        Set<String> names = new LinkedHashSet<>(KNOWN_PLUGINS);
        for (String name : config.getStringList(CONFIG_PATH + "additional-plugins")) {
            if (!name.isBlank()) {
                names.add(name.trim().toLowerCase(Locale.ROOT));
            }
        }
        pluginNames = Set.copyOf(names);
        refresh(null, true);
    }

    private void refresh(Plugin disabling, boolean logUnchanged) {
        List<String> detected = Arrays.stream(owner.getServer().getPluginManager().getPlugins())
                .filter(plugin -> plugin != owner && plugin != disabling && plugin.isEnabled())
                .filter(plugin -> pluginNames.contains(plugin.getName().toLowerCase(Locale.ROOT)))
                .map(Plugin::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        State next = new State(mode == Mode.FULL || (mode == Mode.AUTO && detected.isEmpty()), detected);
        boolean changed = !next.equals(state);
        state = next;
        if (changed || logUnchanged) {
            owner.getLogger().info(getStatus());
            if (mode == Mode.FULL && !detected.isEmpty()) {
                owner.getLogger().warning("FULL fishing was explicitly selected with another fishing plugin active. "
                        + "Use AUTO to prevent competing catch replacements.");
            }
        }
    }

    public boolean handlesCatches() {
        return state.handlesCatches();
    }

    public String getStatus() {
        State current = state;
        return "Fishing mode: " + mode + "; MagicExpansion catch effects: "
                + (current.handlesCatches() ? "enabled" : "paused")
                + "; detected fishing plugins: "
                + (current.detectedPlugins().isEmpty() ? "none" : String.join(", ", current.detectedPlugins()))
                + ".";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        refresh(null, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != owner) {
            // Bukkit fires this event before isEnabled() necessarily becomes false.
            refresh(event.getPlugin(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (!handlesCatches() || event.isCancelled()
                || event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                || !(event.getCaught() instanceof Item caught)
                || caught.isDead()) {
            return;
        }
        for (Consumer<PlayerFishEvent> handler : catchHandlers) {
            // Do not pass a removed/cancelled catch on to another rod handler.
            // A new vanilla catch has not been added to the world yet: isValid() may be false.
            if (event.isCancelled() || caught.isDead()) {
                return;
            }
            handler.accept(event);
        }
    }
}
