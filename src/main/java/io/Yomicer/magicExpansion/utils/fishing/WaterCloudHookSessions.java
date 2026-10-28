package io.Yomicer.magicExpansion.utils.fishing;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unregistered phase-two lifecycle gate for optional Water Cloud fishing.
 * Keeps only immutable UUIDs, timestamps and generations: no Player, FishHook,
 * World, Location, Bukkit scheduler, inventory or XP-bar references.
 *
 * Short synchronized operations serialize cast, reel, quit and shutdown. All
 * future Bukkit interactions belong to the caller's owning entity/region.
 */
public final class WaterCloudHookSessions {

    public record Handle(UUID playerId, UUID hookId, long generation, long startedAtMillis) {
        public Handle {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(hookId, "hookId");
            if (generation < 1 || startedAtMillis < 0) {
                throw new IllegalArgumentException("Invalid hook generation or timestamp");
            }
        }
    }

    /** An older handle must be cleaned up on its correct player region. */
    public record StartResult(Handle active, Optional<Handle> displaced) {
        public StartResult {
            Objects.requireNonNull(active, "active");
            Objects.requireNonNull(displaced, "displaced");
        }
    }

    private final Map<UUID, Handle> activeByPlayer = new ConcurrentHashMap<>();
    private long generation;
    private volatile boolean closed;

    /** One active hook per player, even on recast of an identical hook ID. */
    public synchronized StartResult start(UUID player, UUID hook, long startedAtMillis) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(hook, "hook");
        if (closed) {
            throw new IllegalStateException("Water Cloud sessions are shut down");
        }
        if (startedAtMillis < 0) {
            throw new IllegalArgumentException("Invalid start timestamp");
        }
        generation = Math.addExact(generation, 1);
        Handle handle = new Handle(player, hook, generation, startedAtMillis);
        return new StartResult(handle, Optional.ofNullable(activeByPlayer.put(player, handle)));
    }

    /**
     * Only for player feedback. Never authorize a catch based on this read:
     * use the atomic one-shot claimForReel immediately before payout checks.
     */
    public boolean isCurrent(Handle handle) {
        Objects.requireNonNull(handle, "handle");
        return !closed && handle.equals(activeByPlayer.get(handle.playerId()));
    }

    /**
     * Consume the exact player/hook/generation once; reject invalid time and
     * sessions at/after the 600-second limit without awarding any catch.
     * The future caller must STILL check compatibility, cancellation, hook
     * water/region, owned bait and permitted payout after a successful claim.
     */
    public synchronized boolean claimForReel(Handle handle, long nowMillis) {
        Objects.requireNonNull(handle, "handle");
        if (closed || !activeByPlayer.remove(handle.playerId(), handle)) {
            return false;
        }
        return nowMillis >= handle.startedAtMillis()
                && nowMillis - handle.startedAtMillis()
                        < WaterCloudHookMath.SESSION_LIMIT_SECONDS * 1000L;
    }

    /** A stale callback cannot cancel its replacement cast. */
    public synchronized boolean cancel(Handle handle) {
        Objects.requireNonNull(handle, "handle");
        return activeByPlayer.remove(handle.playerId(), handle);
    }

    /** Invalid clocks and timed-out exact handles are evicted fail-closed. */
    public synchronized boolean expire(Handle handle, long nowMillis) {
        Objects.requireNonNull(handle, "handle");
        if (closed || !handle.equals(activeByPlayer.get(handle.playerId()))) {
            return false;
        }
        if (nowMillis >= handle.startedAtMillis()
                && nowMillis - handle.startedAtMillis()
                        < WaterCloudHookMath.SESSION_LIMIT_SECONDS * 1000L) {
            return false;
        }
        return activeByPlayer.remove(handle.playerId(), handle);
    }

    /** Quit/death cleanup; return the handle to restore owner-region UI. */
    public synchronized Optional<Handle> cancelPlayer(UUID player) {
        Objects.requireNonNull(player, "player");
        return Optional.ofNullable(activeByPlayer.remove(player));
    }

    /**
     * Disable: no new casts, no late rewards, return all handles for cleanup.
     * Repeated shutdown returns nothing; this registry cannot be reopened.
     */
    public synchronized List<Handle> shutdown() {
        if (closed) {
            return List.of();
        }
        closed = true;
        List<Handle> previous = List.copyOf(activeByPlayer.values());
        activeByPlayer.clear();
        return previous;
    }

    public int activeCount() {
        return activeByPlayer.size();
    }
}
