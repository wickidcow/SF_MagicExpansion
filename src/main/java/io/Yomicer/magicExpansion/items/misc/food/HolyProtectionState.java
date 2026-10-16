package io.Yomicer.magicExpansion.items.misc.food;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Token ownership for refreshable Holy Pie protection windows. */
final class HolyProtectionState {
    private final Map<UUID, UUID> active = new ConcurrentHashMap<>();

    UUID activate(UUID playerId) {
        UUID token = UUID.randomUUID();
        active.put(playerId, token);
        return token;
    }

    boolean expire(UUID playerId, UUID token) {
        return active.remove(playerId, token);
    }

    boolean isActive(UUID playerId) {
        return active.containsKey(playerId);
    }

    boolean owns(UUID playerId, UUID token) {
        return token != null && token.equals(active.get(playerId));
    }

    void clear() {
        active.clear();
    }
}
