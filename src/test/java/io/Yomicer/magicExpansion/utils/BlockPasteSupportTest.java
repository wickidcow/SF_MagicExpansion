package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.junit.jupiter.api.Test;

/** Fault-injected setter/snapshot tests; this does not claim native world certification. */
class BlockPasteSupportTest {
    @Test
    void successfulPasteDoesNotRestoreSnapshots() {
        List<String> calls = new ArrayList<>();
        Fixture first = new Fixture("first", calls);
        Fixture second = new Fixture("second", calls);

        var result = BlockPasteSupport.apply(List.of(first.target(), second.target()));

        assertTrue(result.success());
        assertEquals(0, result.rollbackFailures());
        assertEquals(List.of("write:first", "write:second"), calls);
        assertEquals("replacement", first.state);
        assertEquals("replacement", second.state);
    }

    @Test
    void exceptionAfterMutationRestoresAttemptedBlockAndEarlierSnapshot() {
        List<String> calls = new ArrayList<>();
        Fixture first = new Fixture("first", calls);
        Fixture second = new Fixture("second", calls);
        Fixture untouched = new Fixture("untouched", calls);
        second.writeFailure = new IllegalStateException("throw after write");

        var result = BlockPasteSupport.apply(
                List.of(first.target(), second.target(), untouched.target()));

        assertFalse(result.success());
        assertEquals(0, result.rollbackFailures());
        assertEquals(List.of("write:first", "write:second", "restore:second", "restore:first"), calls);
        assertEquals("original with tile contents", first.state);
        assertEquals("original with tile contents", second.state);
        assertEquals("original with tile contents", untouched.state);
    }

    @Test
    void failedAndThrowingRestorationsAreCountedWithoutStoppingOtherRestorations() {
        List<String> calls = new ArrayList<>();
        Fixture first = new Fixture("first", calls);
        Fixture second = new Fixture("second", calls);
        Fixture third = new Fixture("third", calls);
        third.writeFailure = new IllegalStateException("throw after write");
        first.restoreFailure = new IllegalStateException("restore unavailable");
        second.restoreAccepted = false;

        var result = BlockPasteSupport.apply(List.of(first.target(), second.target(), third.target()));

        assertFalse(result.success());
        assertEquals(2, result.rollbackFailures());
        assertEquals(List.of("write:first", "write:second", "write:third",
                "restore:third", "restore:second", "restore:first"), calls);
        assertEquals("original with tile contents", third.state);
        assertEquals("replacement", first.state);
        assertEquals("replacement", second.state);
    }

    @Test
    void linkageFailureAfterWriteStillRestoresTheAttempt() {
        List<String> calls = new ArrayList<>();
        Fixture fixture = new Fixture("only", calls);
        fixture.writeFailure = new NoSuchMethodError("native setter failed");

        var result = BlockPasteSupport.apply(List.of(fixture.target()));

        assertFalse(result.success());
        assertEquals(0, result.rollbackFailures());
        assertEquals(List.of("write:only", "restore:only"), calls);
        assertEquals("original with tile contents", fixture.state);
    }

    private static final class Fixture {
        private final String name;
        private final List<String> calls;
        private String state = "original with tile contents";
        private Throwable writeFailure;
        private Throwable restoreFailure;
        private boolean restoreAccepted = true;

        Fixture(String name, List<String> calls) {
            this.name = name;
            this.calls = calls;
        }

        BlockPasteSupport.Target target() {
            Block block = (Block) Proxy.newProxyInstance(Block.class.getClassLoader(),
                    new Class<?>[] {Block.class}, (proxy, method, args) -> {
                        if (!method.getName().equals("setBlockData")) {
                            throw new AssertionError("Unexpected block operation: " + method.getName());
                        }
                        assertEquals(false, args[1]);
                        calls.add("write:" + name);
                        state = "replacement";
                        if (writeFailure != null) {
                            throw writeFailure;
                        }
                        return null;
                    });
            BlockState original = (BlockState) Proxy.newProxyInstance(BlockState.class.getClassLoader(),
                    new Class<?>[] {BlockState.class}, (proxy, method, args) -> {
                        if (!method.getName().equals("update")) {
                            throw new AssertionError("Unexpected snapshot operation: " + method.getName());
                        }
                        assertArrayEquals(new Object[] {true, false}, args);
                        calls.add("restore:" + name);
                        if (restoreFailure != null) {
                            throw restoreFailure;
                        }
                        if (restoreAccepted) {
                            state = "original with tile contents";
                        }
                        return restoreAccepted;
                    });
            return new BlockPasteSupport.Target(block, original, null);
        }
    }
}
