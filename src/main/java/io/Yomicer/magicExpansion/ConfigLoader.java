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
import java.io.Reader;
import java.nio.file.Files;

public class ConfigLoader {


    public static boolean TESTMODE = MagicExpansion.testmode();

    public static void load(Plugin plugin) {
        ConfigLoader.plugin=plugin;
        init();
        //final File scAddonFile = new File(plugin.getDataFolder(), "language.yml");
        //copyFile(scAddonFile, "language");
        CONFIG=loadExternalConfig("config");
        INNERCONFIG=loadInternalConfig("config");
        if(INNERCONFIG.getBoolean("options.test")) {
            MagicExpansion.testmod=true;
            TESTMODE=true;
            // 修复(P2)：Logger.getLogger 参数是"名称"而非消息，原用法不会输出任何内容；改用主类 logger
            MagicExpansion.getInstance().getLogger().info("Addon is running on TEST MODE");
        }
        if(INNERCONFIG.getBoolean("options.clear-old-config")) {
            // 修复(P1)：clear-old-config 分支误设了 testmod，应设置 clearConfig（清空旧配置标记）
            MagicExpansion.clearConfig=true;
        }
        LANGUAGE=loadInternalConfig("language");   //new Config(plugin,"language.yml");

    }
    public static Plugin plugin;
    public static Config CONFIG;
    public static Config INNERCONFIG;
    public static Config LANGUAGE;
    public static Config SERVER_CONFIG;
    public static void init() {
        SERVER_CONFIG=new Config(plugin);
    }
    public static void copyFile(File file, String name) {
        if(MagicExpansion.clearConfig){

            try{
                Files.delete(file.toPath());
            }catch(Throwable e){
                // 修复(P2)：原 Logger.getLogger 用法错误，改用主类 logger 输出警告
                MagicExpansion.getInstance().getLogger().warning("[TEST MODE] FAILED TO DELETE FILE: "+file.getAbsolutePath());
            }
        }
        if (!file.exists()) {
            try {
                if(!file.toPath().getParent().toFile().exists()) {
                    Files.createDirectories(file.toPath().getParent());
                }
                Files.copy(plugin.getClass().getResourceAsStream("/"+ name + ".yml"),file.toPath());
            } catch (Throwable e) {

                // 修复(P2)：原 Logger.getLogger 用法错误，改用主类 logger 输出警告
                MagicExpansion.getInstance().getLogger().warning("创建配置文件时找不到相关默认配置文件,即将生成空文件");
                try{
                    Files.createDirectories(file.toPath().getParent());
                    Files.createFile(file.toPath());
                }catch (IOException e1){
                    // 修复(P2)：原 Logger.getLogger 用法错误，改用主类 logger 输出警告
                    MagicExpansion.getInstance().getLogger().warning("创建空配置文件失败!");
                }
            }

        }
    }
    public static Config loadInternalConfig(String name){
        FileConfiguration config = new YamlConfiguration();
        // 修复(P3)：getResourceAsStream 可能返回 null，需抛出明确异常而不是让后续 NPE
        InputStream in = plugin.getClass().getResourceAsStream("/"+ name + ".yml");
        if (in == null) {
            throw new RuntimeException("内部配置资源不存在: /" + name + ".yml（请检查插件打包是否完整）");
        }
        // 修复(P3)：改用 try-with-resources 自动关闭流，避免资源泄漏
        try (Reader reader = new InputStreamReader(in, Charsets.UTF_8)) {
            config.load(reader);
            config.getString("options.test");

        }catch (Throwable e){
            // 修复(P3)：加载失败抛出 RuntimeException（带原因），由主类启用隔离逻辑禁用插件；
            // 原 return null 会导致调用方 NPE 且插件处于"半启用"状态
            throw new RuntimeException("加载内部配置 " + name + ".yml 失败: " + e.getMessage(), e);
        }
        return new Config(null,config);
    }
    public static Config loadExternalConfig(String name){
        FileConfiguration config = new YamlConfiguration();
        final File cfgFile = new File(plugin.getDataFolder(), "%s.yml".formatted(name));
        copyFile(cfgFile, name);
        return new Config(plugin, "%s.yml".formatted(name));
    }




}
