package io.Yomicer.magicExpansion.items.misc.fish;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class FishingAnnouncementsTest {
    private ServerMock server;
    private PlayerMock angler;
    private PlayerMock observer;
    private YamlConfiguration config;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        angler = server.addPlayer("Angler");
        observer = server.addPlayer("Observer");
        angler.teleport(new Location(angler.getWorld(), 1234.5, 70, -5678.5));
        config = new YamlConfiguration();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @ValueSource(strings = {"rare fish", "super-rare fish", "Fish Emperor"})
    void missingSettingsDefaultToPrivateCatchNoticeWithoutCoordinates(String rarity) {
        FishingAnnouncements.announce(angler, config, "Golden Fish", rarity, "§e");

        String message = angler.nextMessage();
        assertNotNull(message);
        assertTrue(message.contains("Golden Fish"));
        assertTrue(message.contains(rarity));
        assertFalse(message.contains("1234"));
        assertFalse(message.contains("-5678"));
        assertNull(observer.nextMessage(), "Other players must not learn about the catch");
    }

    @Test
    void allAnnouncementsCanBeDisabled() {
        config.set("fishing.announcements.enabled", false);
        config.set("fishing.announcements.notify-player", false);
        FishingAnnouncements.announce(angler, config, "Golden Fish", "rare fish", "§e");

        assertNull(angler.nextMessage());
        assertNull(observer.nextMessage());
    }

    @Test
    void publicMessageDoesNotRevealLocationUnlessOptedIn() {
        config.set("fishing.announcements.enabled", true);
        FishingAnnouncements.announce(angler, config, "Golden Fish", "super-rare fish", "§b");

        String anglerMessage = angler.nextMessage();
        String observerMessage = observer.nextMessage();
        assertEquals(anglerMessage, observerMessage);
        assertTrue(observerMessage.contains("Angler"));
        assertTrue(observerMessage.contains("Golden Fish"));
        assertFalse(observerMessage.contains("1234"));
        assertFalse(observerMessage.contains("-5678"));
        assertFalse(observerMessage.contains("X:"));
        assertNull(angler.nextMessage(), "Public announcements should not duplicate private notices");
    }

    @Test
    void coordinatesAppearOnlyWithExplicitPublicOptIn() {
        config.set("fishing.announcements.enabled", true);
        config.set("fishing.announcements.show-coordinates", true);
        FishingAnnouncements.announce(angler, config, "Golden Fish", "Fish Emperor", "§c");

        String message = observer.nextMessage();
        assertNotNull(message);
        assertTrue(message.contains("X:1234"));
        assertTrue(message.contains("Y:70"));
        assertTrue(message.contains("Z:-5679"));
        assertEquals(message, angler.nextMessage());
    }

    @Test
    void coordinateSettingCannotLeakLocationWhileBroadcastsAreOff() {
        config.set("fishing.announcements.show-coordinates", true);
        FishingAnnouncements.announce(angler, config, "Golden Fish", "rare fish", "§e");

        assertFalse(angler.nextMessage().contains("1234"));
        assertNull(observer.nextMessage());
    }

    @Test
    void settingsTakeEffectOnTheNextCatchWithoutRestartingFishing() {
        FishingAnnouncements.announce(angler, config, "Golden Fish", "rare fish", "§e");
        assertNotNull(angler.nextMessage());
        assertNull(observer.nextMessage());

        config.set("fishing.announcements.enabled", true);
        FishingAnnouncements.announce(angler, config, "Golden Fish", "rare fish", "§e");
        assertNotNull(angler.nextMessage());
        assertNotNull(observer.nextMessage());
    }

    @Test
    void bundledConfigDefaultsToPrivacy() throws Exception {
        try (var stream = new InputStreamReader(Objects.requireNonNull(
                getClass().getResourceAsStream("/config.yml")), StandardCharsets.UTF_8)) {
            YamlConfiguration bundled = new YamlConfiguration();
            bundled.load(stream);
            assertFalse(bundled.getBoolean("fishing.announcements.enabled"));
            assertFalse(bundled.getBoolean("fishing.announcements.show-coordinates"));
            assertTrue(bundled.getBoolean("fishing.announcements.notify-player"));
        }
    }
}
