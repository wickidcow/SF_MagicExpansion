package io.Yomicer.magicExpansion.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SwordAttackGuardTest {
    @Test
    void nestedDamageCannotActivateAnotherSwordAttack() {
        AtomicInteger attacks = new AtomicInteger();
        SwordAttackGuard.run(() -> {
            assertTrue(SwordAttackGuard.isActive());
            attacks.incrementAndGet();
            SwordAttackGuard.run(attacks::incrementAndGet);
        });
        assertEquals(1, attacks.get());
        assertFalse(SwordAttackGuard.isActive());
        SwordAttackGuard.run(attacks::incrementAndGet);
        assertEquals(2, attacks.get());
    }

    @Test
    void failedAttackAlwaysReleasesTheGuard() {
        assertThrows(IllegalStateException.class, () -> SwordAttackGuard.run(() -> {
            throw new IllegalStateException("Synthetic damage-handler failure");
        }));
        assertFalse(SwordAttackGuard.isActive());
        AtomicInteger attacks = new AtomicInteger();
        SwordAttackGuard.run(attacks::incrementAndGet);
        assertEquals(1, attacks.get());
    }

    @Test
    void anotherEventThreadDoesNotInheritTheGuard() throws Exception {
        AtomicInteger attacks = new AtomicInteger();
        SwordAttackGuard.run(() -> {
            Thread other = new Thread(() -> SwordAttackGuard.run(attacks::incrementAndGet));
            other.start();
            try {
                other.join();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                fail(interrupted);
            }
        });
        assertEquals(1, attacks.get());
    }
}
