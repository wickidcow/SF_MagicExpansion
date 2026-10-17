package io.Yomicer.magicExpansion.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

/**
 * Applies an already preflighted paste and reports unsuccessful restoration honestly.
 * Callers must exclude block entities whose replacement can emit external side effects.
 */
public final class BlockPasteSupport {
    private BlockPasteSupport() {}

    public record Target(Block block, BlockState original, BlockData desired) {}

    public record Outcome(boolean success, int rollbackFailures) {}

    public static Outcome apply(List<Target> targets) {
        List<Target> attempted = new ArrayList<>();
        try {
            for (Target target : targets) {
                // The native setter can throw after changing the block.
                attempted.add(target);
                target.block().setBlockData(target.desired(), false);
            }
            return new Outcome(true, 0);
        } catch (RuntimeException | LinkageError failure) {
            int rollbackFailures = 0;
            for (int i = attempted.size() - 1; i >= 0; i--) {
                try {
                    // Restore the captured native state, not just its material name.
                    if (!attempted.get(i).original().update(true, false)) {
                        rollbackFailures++;
                    }
                } catch (RuntimeException | LinkageError rollbackFailure) {
                    rollbackFailures++;
                }
            }
            return new Outcome(false, rollbackFailures);
        }
    }
}
