package io.Yomicer.magicExpansion.utils;

/** Prevents sword-generated damage from activating the sword again on the same event thread. */
public final class SwordAttackGuard {
    private static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<>();

    private SwordAttackGuard() {}

    public static boolean isActive() {
        return Boolean.TRUE.equals(ACTIVE.get());
    }

    public static void run(Runnable attack) {
        if (isActive()) return;
        ACTIVE.set(true);
        try {
            attack.run();
        } finally {
            ACTIVE.remove();
        }
    }
}
