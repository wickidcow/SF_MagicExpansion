package io.Yomicer.magicExpansion.Listener.fishingListener;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.FishHookMock;
import org.mockbukkit.mockbukkit.entity.ItemEntityMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

class FishingCompatibilityTest {
    private ServerMock server;
    private PluginMock owner;
    private FishingCompatibility compatibility;
    private final AtomicInteger regularCatches = new AtomicInteger();
    private final AtomicInteger waterCloudCatches = new AtomicInteger();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        owner = MockBukkit.createMockPlugin("FishingCompatibilityTest");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void start() {
        compatibility = new FishingCompatibility(owner,
                event -> regularCatches.incrementAndGet(), event -> waterCloudCatches.incrementAndGet());
        server.getPluginManager().registerEvents(compatibility, owner);
    }

    @Test
    void oldConfigWithoutNewSettingsRetainsBothUpstreamRodHandlers() {
        start();
        assertTrue(compatibility.handlesCatches());
        server.getPluginManager().callEvent(catchEvent());
        assertEquals(1, regularCatches.get());
        assertEquals(1, waterCloudCatches.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PyroFishing", "PyroFishingPro", "BetterFish", "BetterFishing",
            "EvenMoreFish", "CustomFishing", "UltimateFishing", "pYrOfIsHiNgPrO"})
    void recognizedPluginsLeaveCatchBaitRodXpAndWorldUntouched(String name) {
        MockBukkit.createMockPlugin(name);
        start();
        PlayerFishEvent event = catchEvent();
        Item caught = (Item) event.getCaught();
        ItemStack originalFish = caught.getItemStack().clone();
        var inventory = event.getPlayer().getInventory();
        inventory.setItemInMainHand(tagged(Material.FISHING_ROD, "slimefun", "id", "EXISTING_ROD"));
        inventory.setItemInOffHand(tagged(Material.PRISMARINE_SHARD, "slimefun", "id", "EXISTING_BAIT"));
        inventory.getItemInOffHand().setAmount(17);
        var rod = inventory.getItemInMainHand().clone();
        var bait = inventory.getItemInOffHand().clone();
        int entities = caught.getWorld().getEntities().size();

        server.getPluginManager().callEvent(event);

        assertFalse(compatibility.handlesCatches());
        assertTrue(compatibility.getStatus().contains(name));
        assertEquals(0, regularCatches.get());
        assertEquals(0, waterCloudCatches.get());
        assertTrue(caught.isValid());
        assertFalse(event.isCancelled());
        assertEquals(13, event.getExpToDrop());
        assertEquals(originalFish, caught.getItemStack());
        assertEquals(rod, inventory.getItemInMainHand());
        assertEquals(bait, inventory.getItemInOffHand());
        assertEquals(entities, caught.getWorld().getEntities().size());
        assertNull(((PlayerMock) event.getPlayer()).nextMessage());
    }

    @Test
    void pluginLoadedLaterPausesBothHandlersAndDisablingItResumesThem() {
        start();
        PluginMock fishing = MockBukkit.createMockPlugin("BetterFish");
        assertFalse(compatibility.handlesCatches());
        server.getPluginManager().callEvent(catchEvent());
        assertEquals(0, regularCatches.get());
        server.getPluginManager().disablePlugin(fishing);
        assertTrue(compatibility.handlesCatches());
        server.getPluginManager().callEvent(catchEvent());
        assertEquals(1, regularCatches.get());
        assertEquals(1, waterCloudCatches.get());
        server.getPluginManager().enablePlugin(fishing);
        assertFalse(compatibility.handlesCatches());
    }

    @Test
    void disablingOneOfTwoFishingPluginsDoesNotResumeCatchEffects() {
        PluginMock first = MockBukkit.createMockPlugin("BetterFish");
        PluginMock second = MockBukkit.createMockPlugin("PyroFishingPro");
        start();
        server.getPluginManager().disablePlugin(first);
        assertFalse(compatibility.handlesCatches());
        assertTrue(compatibility.getStatus().contains("PyroFishingPro"));
        assertFalse(compatibility.getStatus().contains("BetterFish"));
        server.getPluginManager().disablePlugin(second);
        assertTrue(compatibility.handlesCatches());
    }

    @Test
    void disableEventExcludesPluginEvenBeforeItsEnabledFlagChanges() {
        PluginMock fishing = MockBukkit.createMockPlugin("BetterFish");
        start();
        assertTrue(fishing.isEnabled());
        compatibility.onPluginDisable(new PluginDisableEvent(fishing));
        assertTrue(compatibility.handlesCatches());
    }

    @Test
    void disabledAndUnrelatedPluginsDoNotDisableMagicFishing() {
        PluginMock fishing = MockBukkit.createMockPlugin("PyroFishingPro");
        server.getPluginManager().disablePlugin(fishing);
        MockBukkit.createMockPlugin("FishStatistics");
        MockBukkit.createMockPlugin("Essentials");
        start();
        assertTrue(compatibility.handlesCatches());
    }

    @Test
    void customNamesExtendBuiltInDetectionAndReloadWithoutRestart() {
        MockBukkit.createMockPlugin("MyFishingPlugin");
        start();
        assertTrue(compatibility.handlesCatches());
        owner.getConfig().set("Fish.Compatibility.additional-plugins", List.of("  MYFISHINGPLUGIN  ", " "));
        compatibility.reload();
        assertFalse(compatibility.handlesCatches());
        PluginMock known = MockBukkit.createMockPlugin("BetterFish");
        owner.getConfig().set("Fish.Compatibility.additional-plugins", List.of());
        compatibility.reload();
        assertFalse(compatibility.handlesCatches(), "Extra names must not replace built-in detection");
        server.getPluginManager().disablePlugin(known);
        assertTrue(compatibility.handlesCatches());
    }

    @Test
    void explicitCompatibilityPausesFishingWithNoExternalPlugin() {
        owner.getConfig().set("Fish.Compatibility.mode", " compatibility ");
        start();
        server.getPluginManager().callEvent(catchEvent());
        assertFalse(compatibility.handlesCatches());
        assertEquals(0, regularCatches.get());
        assertEquals(0, waterCloudCatches.get());
    }

    @Test
    void explicitFullModeKeepsHandlersAvailableWithAnExternalPlugin() {
        MockBukkit.createMockPlugin("PyroFishingPro");
        owner.getConfig().set("Fish.Compatibility.mode", "full");
        start();
        server.getPluginManager().callEvent(catchEvent());
        assertTrue(compatibility.handlesCatches());
        assertEquals(1, regularCatches.get());
        assertEquals(1, waterCloudCatches.get());
        owner.getConfig().set("Fish.Compatibility.mode", "AUTO");
        compatibility.reload();
        assertFalse(compatibility.handlesCatches());
    }

    @Test
    void invalidModeFailsClosedInsteadOfOverwritingCatches() {
        owner.getConfig().set("Fish.Compatibility.mode", "COMPATIBLITY");
        start();
        assertFalse(compatibility.handlesCatches());
        assertTrue(compatibility.getStatus().contains("COMPATIBILITY"));
    }

    @Test
    void cancellationByAnotherListenerIsRespectedInFullMode() {
        owner.getConfig().set("Fish.Compatibility.mode", "FULL");
        start();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.NORMAL)
            public void blockFishing(PlayerFishEvent event) {
                event.setCancelled(true);
            }
        }, owner);
        PlayerFishEvent event = catchEvent();
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled());
        assertEquals(0, regularCatches.get());
        assertEquals(0, waterCloudCatches.get());
        compatibility.onFish(event);
        assertEquals(0, regularCatches.get(), "Direct calls must also respect cancellation");
    }

    @Test
    void missingRemovedAndNonItemCatchesDoNotTriggerRewards() {
        start();
        PlayerFishEvent removed = catchEvent();
        removed.getCaught().remove();
        compatibility.onFish(removed);
        PlayerFishEvent sample = catchEvent();
        compatibility.onFish(new PlayerFishEvent(sample.getPlayer(), null, sample.getHook(),
                EquipmentSlot.HAND, PlayerFishEvent.State.CAUGHT_FISH));
        compatibility.onFish(new PlayerFishEvent(sample.getPlayer(), sample.getPlayer(), sample.getHook(),
                EquipmentSlot.HAND, PlayerFishEvent.State.CAUGHT_ENTITY));
        compatibility.onFish(new PlayerFishEvent(sample.getPlayer(), sample.getCaught(), sample.getHook(),
                EquipmentSlot.HAND, PlayerFishEvent.State.FISHING));
        assertEquals(0, regularCatches.get());
        assertEquals(0, waterCloudCatches.get());
    }

    @Test
    void newVanillaCatchNeedNotBeAddedToWorldBeforeTheEvent() {
        start();
        PlayerFishEvent sample = catchEvent();
        Item caught = new ItemEntityMock(server, UUID.randomUUID(), new ItemStack(Material.COD)) {
            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public boolean isDead() {
                return false;
            }
        };
        compatibility.onFish(new PlayerFishEvent(sample.getPlayer(), caught, sample.getHook(),
                EquipmentSlot.HAND, PlayerFishEvent.State.CAUGHT_FISH));
        assertEquals(1, regularCatches.get());
        assertEquals(1, waterCloudCatches.get());
    }

    @Test
    void removedCatchIsNotPassedToASecondRodHandler() {
        compatibility = new FishingCompatibility(owner,
                event -> event.getCaught().remove(), event -> waterCloudCatches.incrementAndGet());
        compatibility.onFish(catchEvent());
        assertEquals(0, waterCloudCatches.get());
    }

    private PlayerFishEvent catchEvent() {
        PlayerMock player = server.addPlayer();
        Item fish = player.getWorld().dropItem(player.getLocation(),
                tagged(Material.COD, "externalfishing", "fish_id", "rare-fish-42"));
        var hook = new FishHookMock(server, UUID.randomUUID());
        var event = new PlayerFishEvent(player, fish, hook, EquipmentSlot.HAND, PlayerFishEvent.State.CAUGHT_FISH);
        event.setExpToDrop(13);
        return event;
    }

    private ItemStack tagged(Material material, String namespace, String key, String value) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(namespace, key), PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }
}
