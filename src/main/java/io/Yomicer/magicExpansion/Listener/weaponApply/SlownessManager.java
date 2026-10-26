package io.Yomicer.magicExpansion.Listener.weaponApply;

import io.Yomicer.magicExpansion.MagicExpansion;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

/**
 * Owns only MagicExpansion's temporary movement-speed modifier.
 *
 * <p>Never remove arbitrary modifiers created by vanilla gameplay, Slimefun,
 * equipment, ValhallaMMO, or other plugins. Entity-owned delayed scheduling
 * avoids Bukkit global scheduling across Folia region threads.
 */
public final class SlownessManager {

    private static final Map<UUID, NamespacedKey> ACTIVE = new ConcurrentHashMap<>();

    private SlownessManager() {
    }

    /**
     * Apply the original slowness multiplier and replace this addon's
     * previous modifier, without touching any other plugin's effects.
     *
     * <p>Preserves the existing 3x tick-duration behavior until balance is
     * reviewed separately. Must be called from the entity's owning context.
     */
    public static void applySlowness(LivingEntity target, double slownessLevel, int durationTicks) {
        if (target == null) {
            return;
        }
        AttributeInstance speed = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }

        UUID entityId = target.getUniqueId();
        NamespacedKey oldKey = ACTIVE.remove(entityId);
        if (oldKey != null) {
            removeExistingModifier(speed, oldKey);
        }

        double modifier = modifierAmount(speed.getBaseValue(), slownessLevel);
        if (durationTicks <= 0 || modifier >= 0.0D) {
            return;
        }

        MagicExpansion plugin = MagicExpansion.getInstance();
        NamespacedKey newKey = new NamespacedKey(plugin, "slowness_" + UUID.randomUUID());
        AttributeModifier applied = new AttributeModifier(newKey, modifier,
                AttributeModifier.Operation.ADD_NUMBER);

        speed.addModifier(applied);
        ACTIVE.put(entityId, newKey);

        long delay = Math.max(1L, (long) durationTicks * 3L);
        try {
            // Each expiry is bound to the same moving entity/region.
            var scheduled = target.getScheduler().runDelayed(plugin, task -> {
                if (ACTIVE.remove(entityId, newKey)) {
                    AttributeInstance current = target.getAttribute(Attribute.MOVEMENT_SPEED);
                    if (current != null) {
                        removeExistingModifier(current, newKey);
                    }
                }
            }, () -> ACTIVE.remove(entityId, newKey), delay);

            if (scheduled == null) {
                // Entity retired before the task could be accepted.
                // Do not leave our modifier permanently installed.
                removeSlowness(target);
            }
        } catch (IllegalStateException failure) {
            removeSlowness(target);
            plugin.getLogger().warning("Could not schedule slowness expiry for "
                    + entityId + ": " + failure.getMessage());
        }
    }

    /**
     * Preserve the original magnitude: -baseSpeed * clampedLevel * 0.0099.
     * Non-finite inputs fail closed instead of adding a corrupt modifier.
     */
    public static double modifierAmount(double baseSpeed, double slownessLevel) {
        if (!Double.isFinite(baseSpeed) || baseSpeed <= 0.0D
                || !Double.isFinite(slownessLevel) || slownessLevel <= 0.0D) {
            return 0.0D;
        }
        double level = Math.min(1.0D, slownessLevel);
        return -baseSpeed * level * 0.0099D;
    }

    /** Remove only the modifier whose key this addon currently owns. */
    public static void removeSlowness(LivingEntity target) {
        if (target == null) {
            return;
        }

        NamespacedKey key = ACTIVE.remove(target.getUniqueId());
        if (key == null) {
            return;
        }

        AttributeInstance speed = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            removeExistingModifier(speed, key);
        }
    }

    private static void removeExistingModifier(AttributeInstance speed, NamespacedKey key) {
        // Snapshot: avoid mutating a live collection while iterating.
        for (AttributeModifier modifier : java.util.List.copyOf(speed.getModifiers())) {
            if (key.equals(modifier.getKey())) {
                speed.removeModifier(modifier);
                return;
            }
        }
    }
}
