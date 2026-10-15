package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.github.thebusybiscuit.slimefun4.core.services.profiler.PerformanceInspector;
import io.github.thebusybiscuit.slimefun4.core.services.profiler.SlimefunProfiler;
import io.github.thebusybiscuit.slimefun4.core.services.profiler.SummaryOrderType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SfTimingsSamplerTest {
    private final AtomicLong clock = new AtomicLong();
    private final FakeProfiler profiler = new FakeProfiler();
    private final SfTimingsSampler sampler = new SfTimingsSampler(() -> profiler, clock::get);
    private final Chunk chunk = chunk(UUID.randomUUID(), -3, 7);

    @AfterEach
    void cleanup() {
        profiler.kill();
    }

    @Test
    void requestsFirstSampleWithoutAPlayerCommand() {
        assertEquals("Waiting for sample", sampler.observe(chunk).total());
        assertEquals(1, profiler.requests.size());
        assertEquals(0, profiler.totalReads);
        profiler.complete();
        assertEquals("12.34ms", sampler.observe(chunk).total());
        assertEquals("0.42ms", sampler.observe(chunk).chunk());
    }

    @Test
    void manyMonitorsShareOneRequestAndOneReadPerChunk() {
        for (int i = 0; i < 1000; i++) {
            sampler.observe(chunk);
        }
        assertEquals(1, profiler.requests.size());
        profiler.complete();
        for (int i = 0; i < 1000; i++) {
            sampler.observe(chunk);
        }
        assertEquals(1, profiler.requests.size());
        assertEquals(1, profiler.totalReads);
        assertEquals(1, profiler.chunkReads);
    }

    @Test
    void distinctChunkWrappersAreDeduplicatedByWorldAndCoordinates() {
        sampler.observe(chunk);
        Chunk secondWrapper = chunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        sampler.observe(secondWrapper);
        profiler.complete();
        assertEquals(1, profiler.chunkReads);
        assertEquals("0.42ms", sampler.observe(secondWrapper).chunk());
    }

    @Test
    void differentWorldsAndNegativeChunksRemainSeparate() {
        Chunk otherWorld = chunk(UUID.randomUUID(), -3, 7);
        Chunk otherChunk = chunk(chunk.getWorld().getUID(), 3, -7);
        profiler.times.put(key(chunk), "1.23ms");
        profiler.times.put(key(otherWorld), "2.34ms");
        profiler.times.put(key(otherChunk), "3.45ms");
        sampler.observe(chunk);
        sampler.observe(otherWorld);
        sampler.observe(otherChunk);
        profiler.complete();
        assertEquals("1.23ms", sampler.observe(chunk).chunk());
        assertEquals("2.34ms", sampler.observe(otherWorld).chunk());
        assertEquals("3.45ms", sampler.observe(otherChunk).chunk());
        assertEquals(3, profiler.chunkReads);
    }

    @Test
    void refreshesOnlyAfterTenSecondsFromCompletion() {
        sampler.observe(chunk);
        clock.addAndGet(TimeUnit.SECONDS.toNanos(2));
        profiler.complete();
        clock.addAndGet(SfTimingsSampler.REFRESH_NANOS - 1);
        sampler.observe(chunk);
        assertEquals(1, profiler.requests.size());
        clock.incrementAndGet();
        sampler.observe(chunk);
        assertEquals(2, profiler.requests.size());
    }

    @Test
    void neverPilesUpRequestsWhenTheTickerDoesNotAnswer() {
        sampler.observe(chunk);
        for (int i = 0; i < 100; i++) {
            clock.addAndGet(TimeUnit.SECONDS.toNanos(60));
            assertEquals("Sample unavailable", sampler.observe(chunk).total());
        }
        assertEquals(1, profiler.requests.size());
        profiler.complete();
        assertEquals("12.34ms", sampler.observe(chunk).total());
        assertEquals(1, profiler.requests.size());
    }

    @Test
    void timeoutIsUnavailableRatherThanAFabricatedZeroAndRetriesAreLimited() {
        sampler.observe(chunk);
        profiler.requests.getLast().sendMessage("Your timings report has timed out");
        assertEquals("Sample unavailable", sampler.observe(chunk).total());
        clock.addAndGet(SfTimingsSampler.REFRESH_NANOS);
        sampler.observe(chunk);
        assertEquals(2, profiler.requests.size());
        profiler.complete();
        assertEquals("12.34ms", sampler.observe(chunk).total());
    }

    @Test
    void failedRefreshKeepsOldResultsExplicitlyStale() {
        sampler.observe(chunk);
        profiler.complete();
        clock.addAndGet(SfTimingsSampler.REFRESH_NANOS);
        sampler.observe(chunk);
        profiler.requests.getLast().sendMessage("Profiler could not complete its sample");
        assertEquals("12.34ms [stale]", sampler.observe(chunk).total());
        assertEquals("0.42ms [stale]", sampler.observe(chunk).chunk());
        assertTrue(sampler.observe(chunk).status().contains("stale"));
    }

    @Test
    void thirtySecondOldSampleIsMarkedStale() {
        sampler.observe(chunk);
        profiler.complete();
        clock.addAndGet(SfTimingsSampler.STALE_NANOS);
        assertEquals("12.34ms [stale]", sampler.observe(chunk).total());
    }

    @Test
    void doesNotReadPartiallyUpdatedLiveProfilerMaps() {
        sampler.observe(chunk);
        PerformanceInspector request = profiler.requests.getLast();
        request.sendMessage("");
        request.sendMessage("Report header");
        assertEquals(0, profiler.totalReads);
        request.getOrderType();
        profiler.total = "99ms";
        profiler.times.put(key(chunk), "88ms");
        assertEquals("12.34ms", sampler.observe(chunk).total());
        assertEquals("0.42ms", sampler.observe(chunk).chunk());
        request.getOrderType();
        assertEquals(1, profiler.totalReads);
    }

    @Test
    void zeroIsAllowedWhenItComesFromACompletedSample() {
        profiler.total = "0ms";
        profiler.times.put(key(chunk), "0ms");
        sampler.observe(chunk);
        profiler.complete();
        assertEquals("0ms", sampler.observe(chunk).total());
        assertEquals("0ms", sampler.observe(chunk).chunk());
    }

    @Test
    void newlyObservedChunkWaitsInsteadOfBorrowingAnotherChunksTiming() {
        sampler.observe(chunk);
        profiler.complete();
        Chunk newcomer = chunk(UUID.randomUUID(), 50, 60);
        assertEquals("Waiting for sample", sampler.observe(newcomer).chunk());
        clock.addAndGet(SfTimingsSampler.REFRESH_NANOS);
        sampler.observe(newcomer);
        profiler.complete();
        assertEquals("0.42ms", sampler.observe(newcomer).chunk());
    }

    @Test
    void inactiveChunksArePrunedBeforeTheNextCapture() {
        sampler.observe(chunk);
        profiler.complete();
        int reads = profiler.chunkReads;
        clock.addAndGet(SfTimingsSampler.RETENTION_NANOS);
        Chunk active = chunk(UUID.randomUUID(), 99, 99);
        sampler.observe(active);
        profiler.complete();
        assertEquals(reads + 1, profiler.chunkReads);
    }

    @Test
    void readFailureDoesNotPublishHalfASnapshot() {
        sampler.observe(chunk);
        profiler.failChunkRead = true;
        profiler.complete();
        assertEquals("Sample unavailable", sampler.observe(chunk).total());
        assertEquals("Sample unavailable", sampler.observe(chunk).chunk());
    }

    @Test
    void actualSlimefunSummaryUsesTheExpectedCompletedReportProtocol() throws Exception {
        sampler.observe(chunk);
        Class<?> summaryType = Class.forName(
                "io.github.thebusybiscuit.slimefun4.core.services.profiler.PerformanceSummary");
        Constructor<?> constructor = summaryType.getDeclaredConstructor(SlimefunProfiler.class, long.class, int.class);
        constructor.setAccessible(true);
        Object summary = constructor.newInstance(profiler, 0L, 0);
        Method send = summaryType.getDeclaredMethod("send", PerformanceInspector.class);
        send.setAccessible(true);
        send.invoke(summary, profiler.requests.getLast());
        assertEquals("12.34ms", sampler.observe(chunk).total());
        assertEquals("0.42ms", sampler.observe(chunk).chunk());
        assertEquals(1, profiler.totalReads);
        assertEquals(1, profiler.chunkReads);
    }

    private static String key(Chunk chunk) {
        return chunk.getWorld().getUID() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    private static Chunk chunk(UUID worldId, int x, int z) {
        World world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUID" -> worldId;
                    case "getName" -> worldId.toString();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "test-world";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        return (Chunk) Proxy.newProxyInstance(Chunk.class.getClassLoader(), new Class<?>[]{Chunk.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getWorld" -> world;
                    case "getX" -> x;
                    case "getZ" -> z;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "test-chunk " + x + "," + z;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static final class FakeProfiler extends SlimefunProfiler {
        private final List<PerformanceInspector> requests = new ArrayList<>();
        private final Map<String, String> times = new HashMap<>();
        private String total = "12.34ms";
        private int totalReads;
        private int chunkReads;
        private boolean failChunkRead;

        @Override
        public void requestSummary(PerformanceInspector inspector) {
            requests.add(inspector);
        }

        @Override
        public String getTime() {
            totalReads++;
            return total;
        }

        @Override
        public String getTime(Chunk chunk) {
            chunkReads++;
            if (failChunkRead) {
                throw new IllegalStateException("Deliberate test read failure");
            }
            return times.getOrDefault(key(chunk), "0.42ms");
        }

        @Override
        public int getTickRate() {
            return 10;
        }

        private void complete() {
            PerformanceInspector inspector = requests.getLast();
            inspector.sendMessage("");
            assertEquals(SummaryOrderType.HIGHEST, inspector.getOrderType());
            inspector.getOrderType();
        }
    }
}
