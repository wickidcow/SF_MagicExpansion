package io.Yomicer.magicExpansion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockbukkit.mockbukkit.MockBukkit;

/**
 * Bundled config UTF-8 must retain its exact text, even when an old Guava
 * charset alias is no longer available. No persistent files are touched.
 */
class ConfigLoaderUtf8Test {

    @BeforeEach
    void prepareServerLogger() {
        MockBukkit.mock();
    }

    @AfterEach
    void cleanUpServer() {
        MockBukkit.unmock();
    }

    @Test
    void bundledUnicodeYamlPreservesItsTextAndClosesTheResource() {
        String value = "Crème brûlée — 雪 — 🐻";
        String yaml = "greeting: \"" + value + "\"\n";
        TrackingStream resource = new TrackingStream(yaml.getBytes(StandardCharsets.UTF_8));
        Plugin previous = ConfigLoader.plugin;
        try {
            ConfigLoader.plugin = stub(resource);
            var loaded = ConfigLoader.loadInternalConfig("unicode");
            assertNotNull(loaded);
            assertEquals(value, loaded.getString("greeting"));
            assertTrue(resource.closed, "The bundled input stream must be closed");
        } finally {
            ConfigLoader.plugin = previous;
        }
    }

    @Test
    void missingBundledResourceStillReturnsNullWithoutCreatingAnything() {
        Plugin previous = ConfigLoader.plugin;
        try {
            ConfigLoader.plugin = stub(null);
            assertNull(ConfigLoader.loadInternalConfig("missing"));
        } finally {
            ConfigLoader.plugin = previous;
        }
    }

    private static Plugin stub(TrackingStream resource) {
        return (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class<?>[] {Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getResource" -> resource;
                    case "getLogger" -> Logger.getLogger("MagicExpansion.ConfigLoaderUtf8Test");
                    case "toString" -> "ConfigLoaderUtf8TestPlugin";
                    default -> throw new AssertionError("Unexpected Plugin API: " + method.getName());
                });
    }

    private static final class TrackingStream extends ByteArrayInputStream {
        private boolean closed;

        private TrackingStream(byte[] content) {
            super(content);
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }
}
