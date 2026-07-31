package io.Yomicer.magicExpansion;

import com.google.common.base.Charsets;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public final class ConfigLoader {

    public static boolean TESTMODE = MagicExpansion.testmode();

    private ConfigLoader() {
    }

    public static Plugin plugin;
    public static Config CONFIG;
    public static Config INNERCONFIG;
    public static Config LANGUAGE;
    public static Config SERVER_CONFIG;

    public static void load(Plugin plugin) {
        ConfigLoader.plugin = plugin;
        init();
        CONFIG = loadExternalConfig("config");
        INNERCONFIG = loadInternalConfig("config");

        if (INNERCONFIG != null && INNERCONFIG.getBoolean("options.test")) {
            MagicExpansion.testmod = true;
            TESTMODE = true;
            plugin.getLogger().warning("MagicExpansion is running in test mode.");
        }
        if (INNERCONFIG != null && INNERCONFIG.getBoolean("options.clear-old-config")) {
            MagicExpansion.testmod = true;
        }

        LANGUAGE = loadInternalConfig("language");
    }

    public static void init() {
        SERVER_CONFIG = new Config(plugin);
    }

    public static void copyFile(File file, String name) {
        if (MagicExpansion.clearConfig) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                plugin.getLogger().warning("[TEST MODE] Could not delete " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }

        if (file.exists()) {
            return;
        }

        try {
            Files.createDirectories(file.toPath().getParent());
            try (InputStream resource = plugin.getResource(name + ".yml")) {
                if (resource == null) {
                    throw new IOException("Bundled resource " + name + ".yml was not found");
                }
                Files.copy(resource, file.toPath());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not copy the default " + name + ".yml: " + e.getMessage());
            try {
                Files.createDirectories(file.toPath().getParent());
                Files.createFile(file.toPath());
            } catch (IOException createError) {
                plugin.getLogger().severe("Could not create " + file.getAbsolutePath() + ": " + createError.getMessage());
            }
        }
    }

    public static Config loadInternalConfig(String name) {
        FileConfiguration config = new YamlConfiguration();
        try (InputStream resource = plugin.getResource(name + ".yml")) {
            if (resource == null) {
                throw new IOException("Bundled resource " + name + ".yml was not found");
            }
            config.load(new InputStreamReader(resource, Charsets.UTF_8));
            return new Config(null, config);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load bundled " + name + ".yml: " + e.getMessage());
            return null;
        }
    }

    public static Config loadExternalConfig(String name) {
        File cfgFile = new File(plugin.getDataFolder(), name + ".yml");
        copyFile(cfgFile, name);
        return new Config(plugin, name + ".yml");
    }
}
