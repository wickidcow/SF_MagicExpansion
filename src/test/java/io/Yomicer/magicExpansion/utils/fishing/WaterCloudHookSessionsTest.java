package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Callback identity, timeout and duplicate-reel safety before opt-in events. */
class WaterCloudHookSessionsTest {
    private static final long T = 1_000_000L;

    @Test
    void replacementReturnsOldHandleAndUsesNewGenerationEvenForSameHook() {
        var sessions = new WaterCloudHookSessions();
        UUID player = UUID.randomUUID(), hook = UUID.randomUUID();
        var first = sessions.start(player, hook, T);
        assertTrue(first.displaced().isEmpty());
        var second = sessions.start(player, hook, T + 1);
        assertEquals(first.active(), second.displaced().orElseThrow());
        assertNotEquals(first.active().generation(), second.active().generation());
        assertFalse(sessions.isCurrent(first.active()));
        assertTrue(sessions.isCurrent(second.active()));
        assertEquals(1, sessions.activeCount());
    }

    @Test
    void oldCastCannotCompleteCancelOrExpireTheReplacement() {
        var sessions = new WaterCloudHookSessions();
        UUID player = UUID.randomUUID();
        var old = sessions.start(player, UUID.randomUUID(), T).active();
        var newer = sessions.start(player, UUID.randomUUID(), T + 5).active();
        assertFalse(sessions.claimForReel(old, T + 10));
        assertFalse(sessions.cancel(old));
        assertFalse(sessions.expire(old, T + 800_000));
        assertTrue(sessions.isCurrent(newer));
        assertTrue(sessions.claimForReel(newer, T + 100));
        assertFalse(sessions.claimForReel(newer, T + 100));
    }

    @Test
    void eightConcurrentReelsCannotDuplicateRewards() throws Exception {
        var sessions = new WaterCloudHookSessions();
        var handle = sessions.start(UUID.randomUUID(), UUID.randomUUID(), T).active();
        ExecutorService workers = Executors.newFixedThreadPool(8);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                results.add(workers.submit(() -> {
                    go.await();
                    return sessions.claimForReel(handle, T + 2);
                }));
            }
            go.countDown();
            int succeeded = 0;
            for (var result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    succeeded++;
                }
            }
            assertEquals(1, succeeded);
            assertEquals(0, sessions.activeCount());
        } finally {
            go.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    void anotherHookCannotClaimAnotherPlayersSession() {
        var sessions = new WaterCloudHookSessions();
        UUID player = UUID.randomUUID(), otherPlayer = UUID.randomUUID(), hook = UUID.randomUUID();
        var mine = sessions.start(player, hook, T).active();
        var other = sessions.start(otherPlayer, hook, T).active();
        var forged = new WaterCloudHookSessions.Handle(otherPlayer, UUID.randomUUID(),
                other.generation(), other.startedAtMillis());
        assertFalse(sessions.claimForReel(forged, T + 50));
        assertTrue(sessions.isCurrent(other));
        assertTrue(sessions.claimForReel(mine, T + 50));
        assertTrue(sessions.claimForReel(other, T + 50));
    }

    @Test
    void sixHundredSecondTimeoutAndClockRegressionFailClosed() {
        var sessions = new WaterCloudHookSessions();
        var within = sessions.start(UUID.randomUUID(), UUID.randomUUID(), T).active();
        assertTrue(sessions.claimForReel(within, T + 599_999));
        var expired = sessions.start(UUID.randomUUID(), UUID.randomUUID(), T).active();
        assertFalse(sessions.claimForReel(expired, T + 600_000));
        var regressed = sessions.start(UUID.randomUUID(), UUID.randomUUID(), T).active();
        assertFalse(sessions.claimForReel(regressed, T - 1));
        assertEquals(0, sessions.activeCount());
    }

    @Test
    void expiryOfOldHandleDoesNotDeleteNewSession() {
        var sessions = new WaterCloudHookSessions();
        UUID player = UUID.randomUUID();
        var old = sessions.start(player, UUID.randomUUID(), T).active();
        var newer = sessions.start(player, UUID.randomUUID(), T + 599_000).active();
        assertFalse(sessions.expire(old, T + 700_000));
        assertFalse(sessions.expire(newer, T + 599_500));
        assertTrue(sessions.isCurrent(newer));
        assertTrue(sessions.expire(newer, T + 1_200_000));
        assertEquals(0, sessions.activeCount());
    }

    @Test
    void quitAndShutdownReturnCleanupHandlesWithoutPayingCatches() {
        var sessions = new WaterCloudHookSessions();
        UUID player = UUID.randomUUID();
        var first = sessions.start(player, UUID.randomUUID(), T).active();
        var second = sessions.start(UUID.randomUUID(), UUID.randomUUID(), T).active();
        assertEquals(first, sessions.cancelPlayer(player).orElseThrow());
        assertTrue(sessions.cancelPlayer(player).isEmpty());
        assertEquals(List.of(second), sessions.shutdown());
        assertEquals(0, sessions.activeCount());
        assertTrue(sessions.shutdown().isEmpty());
        assertFalse(sessions.claimForReel(second, T + 10));
        assertThrows(IllegalStateException.class,
                () -> sessions.start(UUID.randomUUID(), UUID.randomUUID(), T + 11));
    }

    @Test
    void badParametersDoNotCreateSessions() {
        var sessions = new WaterCloudHookSessions();
        assertThrows(NullPointerException.class,
                () -> sessions.start(null, UUID.randomUUID(), T));
        assertThrows(NullPointerException.class,
                () -> sessions.start(UUID.randomUUID(), null, T));
        assertThrows(IllegalArgumentException.class,
                () -> sessions.start(UUID.randomUUID(), UUID.randomUUID(), -1));
        assertEquals(0, sessions.activeCount());
    }
}
