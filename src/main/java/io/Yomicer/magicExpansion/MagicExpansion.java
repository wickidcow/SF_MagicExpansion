package io.Yomicer.magicExpansion;

import io.Yomicer.magicExpansion.Listener.RecipePreLoader;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryFinalized;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryGiftBox;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryListener;
import io.Yomicer.magicExpansion.Listener.bossListener.BasicBossAttackListener;
import io.Yomicer.magicExpansion.Listener.bossListener.BasicBossDropListener;
import io.Yomicer.magicExpansion.Listener.fishingListener.GuidePoolButtonListener;
import io.Yomicer.magicExpansion.Listener.GuideVirtualGroupClickListener;
import io.Yomicer.magicExpansion.Listener.fishingListener.PlayerFishingListener;
import io.Yomicer.magicExpansion.Listener.fishingListener.PlayerFishingWaterCloudListener;
import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ArrowHitLocationListener;
import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ItemEffectAttackListener;
import io.Yomicer.magicExpansion.Listener.miscListener.ItemFrameListener;
import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import io.Yomicer.magicExpansion.items.misc.CargoFragmentDistributor;
import io.Yomicer.magicExpansion.items.misc.DrawMachine;
import io.Yomicer.magicExpansion.items.misc.magicAlter.PluginInitializer;
import io.Yomicer.magicExpansion.specialActions.Command.*;
import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ItemEffectKillListener;
import io.Yomicer.magicExpansion.utils.FishingGuideMenu;
import io.Yomicer.magicExpansion.utils.Language;
import io.Yomicer.magicExpansion.utils.aiManager.AIManager;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.ShopCommand;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import io.Yomicer.magicExpansion.utils.shop.ShopManager;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import lombok.SneakyThrows;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.plugin.java.JavaPlugin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.logging.Level;

import static io.Yomicer.magicExpansion.items.misc.PortableCargoTransporter.onPluginDisable;

public class MagicExpansion extends JavaPlugin implements SlimefunAddon {
    public static boolean testmod=false;
    public static boolean clearConfig=false;
    public static boolean testmode(){
        return testmod;
    }
    private static MagicExpansion instance;
    private PluginInitializer pluginInitializer;




    @SneakyThrows
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        // Read something from your config.yml
        Config cfg = new Config(this);

        getLogger().info("§b魔法拓展加载中！");

        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50l.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (cfg.getBoolean("options.auto-update") && getDescription().getVersion().startsWith("Build ")) {
            getLogger().info("§b正在加载更新！");
            GuizhanUpdater.start(this, getFile(), "Yomicer", "MagicExpansion", "master");
            getLogger().info("§b更新完毕！");
        }else{
            getLogger().info("§b未启用自动更新！");
        }
        ConfigLoader.load(this);
        Language.loadConfig(ConfigLoader.LANGUAGE);
        getLogger().info("§b语言包加载完毕！");

        // 魔法祭坛
        pluginInitializer = new PluginInitializer(this);
        pluginInitializer.initialize();
        getLogger().info("魔法2.0-魔法祭坛 已启用!");

        // Registering Items
        MagicExpansionItemSetup.setup(this);
        MagicExpansionRecipeMachineSetup.setup(this);
        MagicExpansionPowerMachineSetup.setup(this);
        MagicExpansionQuickMachineSetup.setup(this);
        MagicExpansionFoodSetup.setup(this);
        getLogger().info("§b物品注册完毕！");


        getLogger().info("§bAI聊天功能加载中！");
        // 创建并启用 AI 子模块
        AIManager aiManager = new AIManager(this);
        aiManager.onEnable();  // ✅ 启动 AI 模块
        getLogger().info("✅ 魔法2.0 ai聊天功能加载完毕！使用 /mxai chaton 开启AI聊天。");


        // Registering Command
        this.getCommand("magicexpansion").setExecutor(new MagicExpansionCommand());
        this.getCommand("mxw").setExecutor(new WorldCommand(this));
        this.getCommand("mxf").setExecutor(new FishingGuideCommand());
        this.getCommand("mxf").setTabCompleter(new FishingGuideCommand());
        this.getCommand("mxai").setExecutor(new AIChat(aiManager));
        this.getCommand("magicfish").setExecutor(new MagicFishCommand());
        ShopCommand shopCommand = new ShopCommand();
        this.getCommand("magicshop").setExecutor(shopCommand);
//        this.getCommand("mxai").setTabCompleter(new AIChat());

        // 创建地图保存目录
        File mapsDir = new File(getDataFolder(), "maps");
        if (!mapsDir.exists()) {
            mapsDir.mkdirs();
        }
        getLogger().info("§b指令注册完毕");
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new SlimefunRegistryFinalized(), this);
        getServer().getPluginManager().registerEvents(new SlimefunRegistryListener(), this);
        getServer().getPluginManager().registerEvents(new RecipePreLoader(), this);
        getServer().getPluginManager().registerEvents(new SlimefunRegistryGiftBox(), this);
        getServer().getPluginManager().registerEvents(new ItemEffectAttackListener(), this);
        getServer().getPluginManager().registerEvents(new ItemEffectKillListener(), this);
        getServer().getPluginManager().registerEvents(new ArrowHitLocationListener(), this);
        getServer().getPluginManager().registerEvents(new BasicBossAttackListener(), this);
        getServer().getPluginManager().registerEvents(new BasicBossDropListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerFishingListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerFishingWaterCloudListener(), this);
        getServer().getPluginManager().registerEvents(new GuidePoolButtonListener(), this);
        getServer().getPluginManager().registerEvents(new GuideVirtualGroupClickListener(), this);
        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new ItemFrameListener(), this);
        getServer().getPluginManager().registerEvents(aiManager, this);
        ShopManager.load();
        getServer().getPluginManager().registerEvents(new ShopGUI(), this);
        BlackMarketManager.init();
        getLogger().info("便携商店系统已加载！");
        getLogger().info("§b监听注册完毕！");












        getLogger().info("§b魔法拓展已成功启用！");










    }

    @Override
    public void onDisable() {
        if (pluginInitializer != null) {
            pluginInitializer.getAltarManager().cancelAllTasks();
        }
        getLogger().info("魔法2.0-魔法祭坛 已禁用!");
        DrawMachine.cleanupAllHolograms();
        getLogger().info("已清理所有抽奖机悬浮物！");

        if (CargoFragmentDistributor.globalTickTask != null) {
            CargoFragmentDistributor.globalTickTask.cancel();
            CargoFragmentDistributor.globalTickTask = null;
        }
        CargoFragmentDistributor.machineStates.clear();
        getLogger().info("已结束所有以太秘匣传输器进程！");

        //便携式以太秘匣传输器
        onPluginDisable();
        ShopManager.saveAll();


        // Plugin shutdown logic
        getLogger().info("§b魔法拓展已成功卸载！");
    }
    public PluginInitializer getPluginInitializer() {
        return pluginInitializer;
    }
    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return "";
    }

    public static MagicExpansion getInstance() {
        return instance;
    }
}
