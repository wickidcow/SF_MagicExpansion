from pathlib import Path
import hashlib
path=Path('src/test/java/io/Yomicer/magicExpansion/utils/shop/ShopFileStoreTest.java')
data=path.read_bytes()
assert hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()=='05fcffbac3c1180b7f4b05d6ccc3a2a9fe9e78a8'
text=data.decode().replace('assertTrue(store.save(shop));','assertTrue(store.save(shop), () -> errors.toString());')
needle='''        write("Extended.yml", config.saveToString());
        var shop = store.load().getFirst();'''
replacement='''        write("Extended.yml", config.saveToString());
        // YAML scalars have no Java Integer/Long tag; compare the actual persisted baseline.
        var persistedBefore = new YamlConfiguration();
        persistedBefore.load(directory.resolve("Extended.yml").toFile());
        var shop = store.load().getFirst();'''
assert text.count(needle)==1
text=text.replace(needle,replacement).replace('assertEquals(config.get("owner-extension.payload"), reloaded.get("owner-extension.payload"));','assertEquals(persistedBefore.get("owner-extension.payload"), reloaded.get("owner-extension.payload"));')
needle='''        var nested = data.getAdapterContext().newPersistentDataContainer();
        nested.set(key("old:nested-owner"), PersistentDataType.STRING, "unchanged");
        data.set(key("old:nested"), PersistentDataType.TAG_CONTAINER, nested);
'''
assert text.count(needle)==1
text=text.replace(needle,'')
needle='    private Path write(String name, String contents) throws IOException {'
addition='''    @Test void unsupportedMockContainerIsRefusedWithoutReplacingExistingData() throws Exception {
        // MockBukkit emits an unsafe Java YAML tag for nested PDC. Real Paper is covered
        // by the server regression; do not weaken production YAML validation for this fixture.
        store.load();
        var shop = richShop("UnsupportedMock");
        assertTrue(store.save(shop), () -> errors.toString());
        byte[] original = Files.readAllBytes(directory.resolve("UnsupportedMock.yml"));
        ItemStack item = shop.trades.getFirst().result;
        var meta = item.getItemMeta();
        var data = meta.getPersistentDataContainer();
        var nested = data.getAdapterContext().newPersistentDataContainer();
        nested.set(key("old:nested-owner"), PersistentDataType.STRING, "unchanged");
        data.set(key("old:nested"), PersistentDataType.TAG_CONTAINER, nested);
        item.setItemMeta(meta);
        ItemStack before = item.clone();
        assertFalse(store.save(shop));
        assertEquals(before, item);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("UnsupportedMock.yml")));
        assertFalse(store.isAvailable(shop));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Global tag is not allowed")), () -> errors.toString());
    }

'''
assert text.count(needle)==1
text=text.replace(needle,addition+needle)
path.write_text(text)
assert hashlib.sha1(b'blob '+str(len(text.encode())).encode()+b'\0'+text.encode()).hexdigest()=='bbc69822f9d2573138546ebbdb9e5c31541fd47b'
