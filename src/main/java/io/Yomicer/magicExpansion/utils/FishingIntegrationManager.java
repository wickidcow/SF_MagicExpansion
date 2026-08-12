package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * Detects external fishing engines and keeps them authoritative for the actual
 * fishing flow. MagicExpansion may add progression and bonus rewards, but it
 * must not replace hooks/catches owned by another fishing plugin.
 */
public final class FishingIntegrationManager {

    public enum Provider {
        PYRO_FISHING("PyroFishingPro"),
        BETTER_FISHING("BetterFishing"),
        VANILLA("Vanilla");

        private final String displayName;

        Provider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @FunctionalInterface
    public interface ExternalCatchHandler {
        void onExternalCatch(Provider provider, Player player, ItemStack caughtItem, Location rewardLocation);
    }

    private static final Listener REFLECTIVE_EVENT_LISTENER = new Listener() {};

    private static MagicExpansion plugin;
    private static ExternalCatchHandler externalCatchHandler;
    private static boolean pyroCatchBridgeRegistered;

    private FishingIntegrationManager() {
    }

    public static void initialize(MagicExpansion instance) {
        plugin = instance;
        registerPyroCatchBridge();
        logStatus();
    }

    public static void setExternalCatchHandler(ExternalCatchHandler handler) {
        externalCatchHandler = handler;
    }

    public static Provider getPrimaryProvider() {
        if (plugin == null || !plugin.getConfig().getBoolean("fishing-integration.external-plugins-primary", true)) {
            return Provider.VANILLA;
        }

        String configured = plugin.getConfig().getString("fishing-integration.provider", "AUTO");
        Provider forced = parseProvider(configured);
        if (forced != null && forced != Provider.VANILLA && isProviderAvailable(forced)) {
            return forced;
        }
        if (forced == Provider.VANILLA) {
            return Provider.VANILLA;
        }

        List<String> priority = plugin.getConfig().getStringList("fishing-integration.priority");
        if (priority.isEmpty()) {
            priority = List.of("PyroFishingPro", "BetterFishing", "VANILLA");
        }

        for (String entry : priority) {
            Provider provider = parseProvider(entry);
            if (provider == Provider.VANILLA) {
                return Provider.VANILLA;
            }
            if (provider != null && isProviderAvailable(provider)) {
                return provider;
            }
        }

        if (isProviderAvailable(Provider.PYRO_FISHING)) {
            return Provider.PYRO_FISHING;
        }
        if (isProviderAvailable(Provider.BETTER_FISHING)) {
            return Provider.BETTER_FISHING;
        }
        return Provider.VANILLA;
    }

    public static boolean isExternalProviderActive() {
        Provider provider = getPrimaryProvider();
        return provider == Provider.PYRO_FISHING || provider == Provider.BETTER_FISHING;
    }

    public static boolean hasNativeCatchBridge(Provider provider) {
        return provider == Provider.PYRO_FISHING && pyroCatchBridgeRegistered;
    }

    /**
     * The upstream Water Cloud state machine is only allowed to control a hook
     * when no external provider owns fishing, unless a server owner explicitly
     * opts out of external-provider priority.
     */
    public static boolean shouldUseMagicStateMachine() {
        if (plugin == null || !plugin.getConfig().getBoolean("fishing-system.new-system", false)) {
            return false;
        }

        if (plugin.getConfig().getBoolean("fishing-integration.external-plugins-primary", true)
                && isExternalProviderActive()) {
            return false;
        }

        if (plugin.getConfig().getBoolean("fishing-system.only-when-no-external-provider", true)
                && isExternalProviderActive()) {
            return false;
        }

        return true;
    }

    public static boolean isProviderAvailable(Provider provider) {
        return switch (provider) {
            case PYRO_FISHING -> findEnabledPlugin("PyroFishingPro", "PyroFishing") != null;
            case BETTER_FISHING -> findEnabledPlugin("BetterFishing", "FishingBetter") != null;
            case VANILLA -> true;
        };
    }

    private static Plugin findEnabledPlugin(String... names) {
        if (plugin == null) {
            return null;
        }

        for (Plugin candidate : plugin.getServer().getPluginManager().getPlugins()) {
            if (!candidate.isEnabled()) {
                continue;
            }
            for (String name : names) {
                if (candidate.getName().equalsIgnoreCase(name)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * PyroFishingPro exposes PyroFishCatchEvent, but MagicExpansion intentionally
     * does not compile against the paid plugin. Register the event reflectively
     * so Pyro remains optional while still giving us a reliable successful-catch
     * signal after Pyro has produced its own fish.
     */
    @SuppressWarnings("unchecked")
    private static void registerPyroCatchBridge() {
        pyroCatchBridgeRegistered = false;
        Plugin pyro = findEnabledPlugin("PyroFishingPro", "PyroFishing");
        if (pyro == null) {
            return;
        }

        try {
            Class<?> rawEventClass = Class.forName(
                    "me.arsmagica.API.PyroFishCatchEvent",
                    false,
                    pyro.getClass().getClassLoader()
            );
            if (!Event.class.isAssignableFrom(rawEventClass)) {
                plugin.getLogger().warning("PyroFishingPro catch API class is not a Bukkit Event; using PlayerFishEvent fallback.");
                return;
            }

            Class<? extends Event> eventClass = (Class<? extends Event>) rawEventClass;
            Method getPlayer = rawEventClass.getMethod("getPlayer");
            Method getItemStack = rawEventClass.getMethod("getItemStack");

            plugin.getServer().getPluginManager().registerEvent(
                    eventClass,
                    REFLECTIVE_EVENT_LISTENER,
                    EventPriority.MONITOR,
                    (listener, event) -> {
                        if (getPrimaryProvider() != Provider.PYRO_FISHING || externalCatchHandler == null) {
                            return;
                        }
                        try {
                            Player player = (Player) getPlayer.invoke(event);
                            ItemStack caught = (ItemStack) getItemStack.invoke(event);
                            if (player != null) {
                                externalCatchHandler.onExternalCatch(
                                        Provider.PYRO_FISHING,
                                        player,
                                        caught == null ? null : caught.clone(),
                                        player.getLocation()
                                );
                            }
                        } catch (ReflectiveOperationException | ClassCastException exception) {
                            plugin.getLogger().warning("Could not read PyroFishingPro catch event: " + exception.getMessage());
                        }
                    },
                    plugin,
                    true
            );

            pyroCatchBridgeRegistered = true;
            plugin.getLogger().info("PyroFishingPro catch API bridge registered without a hard plugin dependency.");
        } catch (ReflectiveOperationException | LinkageError exception) {
            plugin.getLogger().warning("PyroFishingPro catch API bridge unavailable; using PlayerFishEvent fallback: "
                    + exception.getMessage());
        }
    }

    private static Provider parseProvider(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");

        return switch (normalized) {
            case "PYROFISHING", "PYROFISHINGPRO", "PYRO" -> Provider.PYRO_FISHING;
            case "BETTERFISHING", "FISHINGBETTER", "BETTER" -> Provider.BETTER_FISHING;
            case "VANILLA", "NONE" -> Provider.VANILLA;
            case "AUTO", "" -> null;
            default -> null;
        };
    }

    public static void logStatus() {
        if (plugin == null) {
            return;
        }

        Provider provider = getPrimaryProvider();
        if (provider == Provider.VANILLA) {
            plugin.getLogger().info("Fishing integration: vanilla/MagicExpansion fishing is primary.");
        } else {
            plugin.getLogger().info("Fishing integration: " + provider.getDisplayName()
                    + " is primary; MagicExpansion will preserve its catch flow and add Magic rod rewards only.");
        }

        if (plugin.getConfig().getBoolean("fishing-system.new-system", false)
                && !shouldUseMagicStateMachine()) {
            plugin.getLogger().info("Water Cloud custom fishing state machine is suppressed while an external fishing provider is primary.");
        }
    }
}
