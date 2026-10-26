package io.Yomicer.magicExpansion.Listener.weaponApply;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.junit.jupiter.api.Test;

class SlownessManagerTest {

    @Test
    void upstreamMagnitudeIsRetainedWithoutNaNOrNegativeLevels() {
        assertEquals(-0.00198D, SlownessManager.modifierAmount(0.2D, 1.0D), 1.0e-12);
        assertEquals(-0.00099D, SlownessManager.modifierAmount(0.2D, 0.5D), 1.0e-12);
        assertEquals(0.0D, SlownessManager.modifierAmount(0.2D, 0.0D), 1.0e-12);
        assertEquals(0.0D, SlownessManager.modifierAmount(0.2D, -1.0D), 1.0e-12);
        assertEquals(-0.00198D, SlownessManager.modifierAmount(0.2D, 999D), 1.0e-12);
        assertEquals(0.0D, SlownessManager.modifierAmount(0.2D, Double.NaN), 1.0e-12);
        assertEquals(0.0D, SlownessManager.modifierAmount(Double.NaN, 1.0D), 1.0e-12);
        assertEquals(0.0D, SlownessManager.modifierAmount(Double.POSITIVE_INFINITY, 1), 1.0e-12);
    }

    @Test
    void expirationRemovesOnlyTheOwnedModifierWithoutDamagingOthers() {
        NamespacedKey pluginKey = new NamespacedKey("magicexpansion", "slowness_test");
        NamespacedKey equipmentKey = new NamespacedKey("test_equipment", "speed");
        NamespacedKey anotherPluginKey = new NamespacedKey("test_plugin", "speed");

        Map<NamespacedKey, AttributeModifier> modifiers = new LinkedHashMap<>();
        for (NamespacedKey key : new NamespacedKey[]{pluginKey, equipmentKey, anotherPluginKey}) {
            modifiers.put(key, new AttributeModifier(key, -0.001D,
                    AttributeModifier.Operation.ADD_NUMBER));
        }

        AttributeInstance instance = (AttributeInstance) Proxy.newProxyInstance(
                AttributeInstance.class.getClassLoader(),
                new Class<?>[]{AttributeInstance.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getModifiers" -> new ArrayList<>(modifiers.values());
                    case "removeModifier" -> {
                        AttributeModifier removed = (AttributeModifier) arguments[0];
                        modifiers.remove(removed.getKey());
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });

        SlownessManager.removeExistingModifier(instance, pluginKey);
        assertFalse(modifiers.containsKey(pluginKey));
        assertTrue(modifiers.containsKey(equipmentKey));
        assertTrue(modifiers.containsKey(anotherPluginKey));

        // A stale expiry cannot erase unrelated modifiers.
        SlownessManager.removeExistingModifier(instance, pluginKey);
        assertEquals(2, modifiers.size());
        assertTrue(modifiers.containsKey(equipmentKey));
        assertTrue(modifiers.containsKey(anotherPluginKey));
    }
}
