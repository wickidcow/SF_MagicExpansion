package io.Yomicer.magicExpansion.utils;

import io.github.thebusybiscuit.slimefun4.core.services.profiler.PerformanceInspector;
import io.github.thebusybiscuit.slimefun4.core.services.profiler.SlimefunProfiler;
import io.github.thebusybiscuit.slimefun4.core.services.profiler.SummaryOrderType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bukkit.Chunk;

/** Shares on-demand, completed profiler samples between all placed tick monitors. */
public final class SfTimingsSampler {
    static final long REFRESH_NANOS = TimeUnit.SECONDS.toNanos(10);
    static final long STALE_NANOS = TimeUnit.SECONDS.toNanos(30);
    static final long RETENTION_NANOS = TimeUnit.SECONDS.toNanos(60);

    private final Supplier<SlimefunProfiler> profilerSupplier;
    private final LongSupplier clock;
    private final Map<ChunkKey, TrackedChunk> observed = new HashMap<>();
    private Snapshot snapshot;
    private Request pending;
    private boolean hasRequested;
    private boolean failed;
    private long lastRequestOrCompletion;

    public SfTimingsSampler() {
        this(Slimefun::getProfiler, System::nanoTime);
    }

    SfTimingsSampler(Supplier<SlimefunProfiler> profilerSupplier, LongSupplier clock) {
        this.profilerSupplier = profilerSupplier;
        this.clock = clock;
    }

    /** Called by the synchronized block ticker; never loads chunks from a profiler thread. */
    public Reading observe(Chunk chunk) {
        long now = clock.getAsLong();
        ChunkKey key = new ChunkKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        Request request = null;
        synchronized (this) {
            observed.put(key, new TrackedChunk(new WeakReference<>(chunk), now));
            if (pending == null && (!hasRequested || now - lastRequestOrCompletion >= REFRESH_NANOS)) {
                prune(now);
                request = new Request(now);
                pending = request;
                hasRequested = true;
                lastRequestOrCompletion = now;
            }
        }

        if (request != null) {
            try {
                request.profiler = profilerSupplier.get();
                request.profiler.requestSummary(request);
            } catch (RuntimeException exception) {
                fail(request, "Unable to request a Slimefun timing sample: " + exception);
            }
        }

        synchronized (this) {
            return reading(key, clock.getAsLong());
        }
    }

    private void prune(long now) {
        observed.entrySet().removeIf(entry -> now - entry.getValue().seenAt >= RETENTION_NANOS
                || entry.getValue().chunk.get() == null);
    }

    private Reading reading(ChunkKey key, long now) {
        boolean overdue = pending != null && now - pending.startedAt >= STALE_NANOS;
        if (snapshot == null) {
            String state = failed || overdue ? "Sample unavailable" : "Waiting for sample";
            return new Reading(state, state, pending == null ? state : state + " (request pending)");
        }

        long age = Math.max(0L, now - snapshot.completedAt);
        boolean stale = failed || overdue || age >= STALE_NANOS;
        String suffix = stale ? " [stale]" : "";
        String chunkTime = snapshot.chunks.get(key);
        String chunkText = chunkTime == null ? "Waiting for sample" : chunkTime + suffix;
        String status = "Sample: " + TimeUnit.NANOSECONDS.toSeconds(age) + "s ago";
        if (stale) {
            status += " (stale; not a live tick)";
        } else if (pending != null) {
            status += " (refresh pending)";
        }
        return new Reading(snapshot.total + suffix, chunkText, status);
    }

    private void capture(Request request) {
        Map<ChunkKey, TrackedChunk> targets;
        synchronized (this) {
            if (pending != request) {
                return;
            }
            prune(clock.getAsLong());
            targets = new HashMap<>(observed);
        }

        // getOrderType is called during delivery of a completed detailed report. Reading here
        // avoids racing normal ticker cycles which clear/repopulate the profiler's live map.
        // Do not hold our lock while the public per-chunk API walks detailed profiler entries.
        try {
            String total = request.profiler.getTime();
            Map<ChunkKey, String> chunks = new HashMap<>();
            for (Map.Entry<ChunkKey, TrackedChunk> entry : targets.entrySet()) {
                Chunk chunk = entry.getValue().chunk.get();
                if (chunk != null) {
                    chunks.put(entry.getKey(), request.profiler.getTime(chunk));
                }
            }
            Snapshot completed = new Snapshot(total, Map.copyOf(chunks), clock.getAsLong());
            synchronized (this) {
                if (pending == request) {
                    snapshot = completed;
                    pending = null;
                    failed = false;
                    lastRequestOrCompletion = completed.completedAt;
                }
            }
        } catch (RuntimeException exception) {
            fail(request, "Unable to read a completed Slimefun timing sample: " + exception);
        }
    }

    private void fail(Request request, String reason) {
        boolean warn;
        synchronized (this) {
            if (pending != request) {
                return;
            }
            warn = !failed;
            failed = true;
            pending = null;
            lastRequestOrCompletion = clock.getAsLong();
        }
        if (warn) {
            Logger.getLogger("magicexpansion").warning(reason);
        }
    }

    public record Reading(String total, String chunk, String status) {}

    private record ChunkKey(UUID world, int x, int z) {}
    private record TrackedChunk(WeakReference<Chunk> chunk, long seenAt) {}
    private record Snapshot(String total, Map<ChunkKey, String> chunks, long completedAt) {}

    private final class Request implements PerformanceInspector {
        private final long startedAt;
        private final AtomicBoolean firstMessage = new AtomicBoolean(true);
        private final AtomicBoolean captured = new AtomicBoolean();
        private volatile SlimefunProfiler profiler;

        private Request(long startedAt) {
            this.startedAt = startedAt;
        }

        @Override
        public boolean isValid() {
            synchronized (SfTimingsSampler.this) {
                return pending == this;
            }
        }

        @Override
        public void sendMessage(String message) {
            // The supported Slimefun PerformanceSummary begins with an empty separator;
            // failure delivery is a non-empty first message and never queries getOrderType.
            // Fail closed if this report protocol changes; never turn an error into 0ms.
            if (firstMessage.compareAndSet(true, false) && !message.isEmpty()) {
                fail(this, "Slimefun tick monitor sample unavailable: " + message);
            }
        }

        @Override
        public boolean isVerbose() {
            return false;
        }

        @Override
        public SummaryOrderType getOrderType() {
            if (captured.compareAndSet(false, true)) {
                capture(this);
            }
            return SummaryOrderType.HIGHEST;
        }
    }
}
