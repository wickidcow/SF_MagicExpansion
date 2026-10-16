package io.Yomicer.magicExpansion.items.misc.food;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class HolyProtectionStateTest {

    @Test
    void olderExpiryCannotCancelNewerProtection() {
        HolyProtectionState state = new HolyProtectionState();
        UUID player = UUID.randomUUID();

        UUID first = state.activate(player);
        UUID second = state.activate(player);

        assertFalse(state.expire(player, first));
        assertTrue(state.isActive(player));
        assertTrue(state.owns(player, second));

        assertTrue(state.expire(player, second));
        assertFalse(state.isActive(player));
    }

    @Test
    void protectionIsIndependentPerPlayer() {
        HolyProtectionState state = new HolyProtectionState();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();

        UUID firstToken = state.activate(firstPlayer);
        UUID secondToken = state.activate(secondPlayer);

        assertTrue(state.expire(firstPlayer, firstToken));
        assertFalse(state.isActive(firstPlayer));
        assertTrue(state.isActive(secondPlayer));
        assertTrue(state.owns(secondPlayer, secondToken));
    }

    @Test
    void clearRemovesEveryProtection() {
        HolyProtectionState state = new HolyProtectionState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        state.activate(first);
        state.activate(second);

        state.clear();

        assertFalse(state.isActive(first));
        assertFalse(state.isActive(second));
    }
}
