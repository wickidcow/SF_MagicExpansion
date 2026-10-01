from pathlib import Path
import hashlib
path=Path('src/test/java/io/Yomicer/magicExpansion/utils/shop/ShopFileStoreTest.java')
data=path.read_bytes()
blob=lambda b:hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
assert blob(data)=='bbc69822f9d2573138546ebbdb9e5c31541fd47b'
text=data.decode()
needle='        data.set(key("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);\n'
assert text.count(needle)==1
text=text.replace(needle,'        // FLOAT and nested PDC success are tested on real Paper; this mock changes their YAML types.\n')
needle='        assertEquals(Float.floatToRawIntBits(123.4567F), Float.floatToRawIntBits(pdc.get(key("slimefun:item_charge"), PersistentDataType.FLOAT)));'
assert text.count(needle)==1
text=text.replace(needle,'        assertEquals("UNREGISTERED_OLD_ID", pdc.get(key("slimefun:slimefun_item"), PersistentDataType.STRING));\n        assertArrayEquals(new byte[] {0, -1, 4}, pdc.get(key("old:bytes"), PersistentDataType.BYTE_ARRAY));')
needle='    private Path write(String name, String contents) throws IOException {'
addition='''    @Test void mockFloatTypeCoercionIsRefusedWithoutMutatingSourceOrFile() throws Exception {
        store.load();
        var shop = richShop("LossyMock");
        assertTrue(store.save(shop), () -> errors.toString());
        byte[] original = Files.readAllBytes(directory.resolve("LossyMock.yml"));
        ItemStack item = shop.trades.getFirst().result;
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key("slimefun:item_charge"), PersistentDataType.FLOAT, 123.4567F);
        item.setItemMeta(meta);
        ItemStack before = item.clone();
        var encoded = new YamlConfiguration();
        encoded.set("item", item);
        var decoded = new YamlConfiguration();
        decoded.loadFromString(encoded.saveToString());
        var decodedData = decoded.getItemStack("item").getItemMeta().getPersistentDataContainer();
        assertFalse(decodedData.has(key("slimefun:item_charge"), PersistentDataType.FLOAT));
        assertTrue(decodedData.has(key("slimefun:item_charge"), PersistentDataType.DOUBLE));
        assertNotEquals(item, decoded.getItemStack("item"));
        assertFalse(store.save(shop));
        assertEquals(before, item);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("LossyMock.yml")));
        assertFalse(store.isAvailable(shop));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Trade data changed during serialization")), () -> errors.toString());
    }

'''
assert text.count(needle)==1
text=text.replace(needle,addition+needle)
path.write_text(text)
assert blob(path.read_bytes())=='fe66f9bad99466c72d6383ab4c32a4a230561642'
