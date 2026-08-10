package io.Yomicer.magicExpansion.utils;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.plugin.Plugin;

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

    private static MagicExpansion plugin;

    private FishingIntegrationManager() {
    }

    public static void initialize(MagicExpansion instance) {
        plugin = instance;
        logStatus();
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
            case PYRO_FISHING -> isAnyPluginEnabled("PyroFishingPro", "PyroFishing");
            case BETTER_FISHING -> isAnyPluginEnabled("BetterFishing");
            case VANILLA -> true;
        };
    }

    private static boolean isAnyPluginEnabled(String... names) {
        if (plugin == null) {
            return false;
        }

        for (Plugin candidate : plugin.getServer().getPluginManager().getPlugins()) {
            if (!candidate.isEnabled()) {
                continue;
            }
            for (String name : names) {
                if (candidate.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
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
            case "BETTERFISHING", "BETTER" -> Provider.BETTER_FISHING;
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
