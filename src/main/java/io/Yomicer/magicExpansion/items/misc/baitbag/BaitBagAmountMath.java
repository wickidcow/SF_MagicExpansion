package io.Yomicer.magicExpansion.items.misc.baitbag;

/** Overflow-safe accounting for ordinary lure deposits into a Lure Bag. */
final class BaitBagAmountMath {
    private BaitBagAmountMath() {}

    static Transfer deposit(long current, int offered) {
        long safeCurrent = Math.max(0L, current);
        if (offered <= 0 || safeCurrent >= Long.MAX_VALUE) {
            return new Transfer(safeCurrent, 0, Math.max(0, offered));
        }

        long free = Long.MAX_VALUE - safeCurrent;
        int accepted = (int) Math.min((long) offered, free);
        return new Transfer(safeCurrent + accepted, accepted, offered - accepted);
    }

    record Transfer(long stored, int accepted, int remainder) {}
}
