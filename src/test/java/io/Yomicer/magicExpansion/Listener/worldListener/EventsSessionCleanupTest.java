package io.Yomicer.magicExpansion.Listener.worldListener;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

class EventsSessionCleanupTest {

    @Test
    void disconnectClearsOnlyDepartingPlayerSelection() {
        UUID departing = UUID.randomUUID();
        UUID remaining = UUID.randomUUID();
        Location first = new Location(null, 1, 64, 1);
        Location second = new Location(null, 3, 64, 3);
        try {
            Events.selectMode.put(departing, true);
            Events.point1.put(departing, first);
            Events.point2.put(departing, second);
            Events.selectMode.put(remaining, true);
            Events.point1.put(remaining, second);

            Events.cleanup(departing);

            assertFalse(Events.selectMode.containsKey(departing));
            assertFalse(Events.point1.containsKey(departing));
            assertFalse(Events.point2.containsKey(departing));
            assertEquals(Boolean.TRUE, Events.selectMode.get(remaining));
            assertSame(second, Events.point1.get(remaining));
        } finally {
            Events.cleanup(departing);
            Events.cleanup(remaining);
        }
    }
}
