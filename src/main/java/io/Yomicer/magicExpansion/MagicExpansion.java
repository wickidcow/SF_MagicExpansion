package io.Yomicer.magicExpansion;

import io.Yomicer.magicExpansion.Listener.AccelerationUseListener;
import io.Yomicer.magicExpansion.Listener.GuideVirtualGroupClickListener;
import io.Yomicer.magicExpansion.Listener.RecipePreLoader;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryFinalized;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryGiftBox;
import io.Yomicer.magicExpansion.Listener.SlimefunRegistryListener;
import io.Yomicer.magicExpansion.Listener.bossListener.BasicBossAttackListener;
import io.Yomicer.magicExpansion.Listener.bossListener.BasicBossDropListener;
import io.Yomicer.magicExpansion.Listener.fishingListener.GuidePoolButtonListener;
import io.Yomicer.magicExpansion.Listener.fishingListener.PlayerFishingListener;
import io.Yomicer.magicExpansion.Listener.fishingListener.PlayerFishingWaterCloudListener;
import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ArrowHitLocationListener;
import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ItemEffectAttackListener;
import io.Yomicer.magicExpansion.Listener.magicItemEffectManager.ItemEffectKillListener;
import io.Yomicer.magicExpansion.Listener.miscListener.ItemFrameListener;
import io.Yomicer.magicExpansion.Listener.worldListener.Events;
import io.Yomicer.magicExpansion.items.misc.CargoFragmentDistributor;
import io.Yomicer.magicExpansion.items.misc.DrawMachine;
import io.Yomicer.magicExpansion.items.misc.PageChestListener;
import io.Yomicer.magicExpansion.items.misc.magicAlter.PluginInitializer;
import io.Yomicer.magicExpansion.specialActions.Command.*;
import io.Yomicer.magicExpansion.utils.Language;
import io.Yomicer.magicExpansion.utils.aiManager.AIManager;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.ShopCommand;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import io.Yomicer.magicExpansion.utils.shop.ShopManager;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

import static io.Yomicer.magicExpansion.items.misc.PortableCargoTransporter.onPluginDisable;

public class MagicExpansion extends JavaPlugin implements SlimefunAddon {

    public static boolean testmod = false;
    public static boolean clearConfig = false;

    public static boolean testmode() {
        return testmod;
    }

    private static MagicExpansion instance;
    private PluginInitializer pluginInitializer;
    private AIManager aiManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        getLogger().info("§bLoading MagicExpansion...");

        getLogger().info("Bundled English compatibility helpers are active; GuizhanLibPlugin is not required.");

        ConfigLoader.load(this);
        Language.loadConfig(ConfigLoader.LANGUAGE);
        getLogger().info("§bEnglish language data loaded.");

        // Magic altar
        pluginInitializer = new PluginInitializer(this);
        pluginInitializer.initialize();
        getLogger().info("Magic Altar enabled.");

        // Register items
        MagicExpansionItemSetup.setup(this);
        renameMainGuideCategory();
        MagicExpansionRecipeMachineSetup.setup(this);
        MagicExpansionPowerMachineSetup.setup(this);
        MagicExpansionQuickMachineSetup.setup(this);
        MagicExpansionFoodSetup.setup(this);
        getLogger().info("§bItems registered.");

        getLogger().info("§bLoading AI chat support...");
        aiManager = new AIManager(this);
        aiManager.onEnable();
        getLogger().info("Optional AI chat module initialized; it remains disabled until configured.");

        // Register commands
        this.getCommand("magicexpansion").setExecutor(new MagicExpansionCommand());
        this.getCommand("mxw").setExecutor(new WorldCommand(this));
        this.getCommand("mxf").setExecutor(new FishingGuideCommand());
        this.getCommand("mxf").setTabCompleter(new FishingGuideCommand());
        this.getCommand("mxai").setExecutor(new AIChat(aiManager));
        this.getCommand("magicfish").setExecutor(new MagicFishCommand());

        ShopCommand shopCommand = new ShopCommand();
        this.getCommand("magicshop").setExecutor(shopCommand);

        File mapsDir = new File(getDataFolder(), "maps");
        if (!mapsDir.exists()) {
            mapsDir.mkdirs();
        }

        getLogger().info("§bCommands registered.");

        // Register listeners
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
        getServer().getPluginManager().registerEvents(new AccelerationUseListener(), this);
        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new ItemFrameListener(), this);
        getServer().getPluginManager().registerEvents(new PageChestListener(), this);
        getServer().getPluginManager().registerEvents(aiManager, this);

        ShopManager.load();
        getServer().getPluginManager().registerEvents(new ShopGUI(), this);
        BlackMarketManager.init();

        getLogger().info("Portable Shop loaded.");
        getLogger().info("§bListeners registered.");
        getLogger().info("§bMagicExpansion enabled successfully.");
    }

    /**
     * Replaces the old obfuscated "2.0" guide label while retaining the
     * existing LIGHT icon, category key, tier, subgroups, items and saved data.
     */
    private void renameMainGuideCategory() {
        try {
            Field displayItemField = ItemGroup.class.getDeclaredField("item");
            displayItemField.setAccessible(true);

            ItemStack displayItem =
                    (ItemStack) displayItemField.get(MagicExpansionItemSetup.magicexpansion);
            ItemMeta meta = displayItem.getItemMeta();

            if (meta == null) {
                getLogger().warning("Could not rename the Magic guide category because its icon has no item metadata.");
                return;
            }

            meta.setDisplayName("Magic");
            displayItem.setItemMeta(meta);
            getLogger().info("Magic guide category renamed to \"Magic\".");
        } catch (ReflectiveOperationException | SecurityException exception) {
            getLogger().warning(
                    "Could not rename the Magic guide category: " + exception.getMessage()
            );
        }
    }

    @Override
    public void onDisable() {
        if (pluginInitializer != null) {
            pluginInitializer.getAltarManager().cancelAllTasks();
        }

        getLogger().info("Magic Altar disabled.");

        DrawMachine.cleanupAllHolograms();
        getLogger().info("Cleaned up lottery-machine holograms.");

        if (CargoFragmentDistributor.globalTickTask != null) {
            CargoFragmentDistributor.globalTickTask.cancel();
            CargoFragmentDistributor.globalTickTask = null;
        }

        CargoFragmentDistributor.machineStates.clear();
        getLogger().info("Stopped all cargo transporter tasks.");

        onPluginDisable();
        ShopManager.saveAll();

        if (aiManager != null) {
            aiManager.shutdown();
        }

        getLogger().info("§bMagicExpansion disabled successfully.");
    }

    public PluginInitializer getPluginInitializer() {
        return pluginInitializer;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "";
    }

    public static MagicExpansion getInstance() {
        return instance;
    }
}