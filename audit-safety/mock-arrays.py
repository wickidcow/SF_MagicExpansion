from pathlib import Path
import hashlib
p=Path('src/test/java/io/Yomicer/magicExpansion/utils/shop/ShopFileStoreTest.java')
b=p.read_bytes();blob=lambda b:hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
assert blob(b)=='fe66f9bad99466c72d6383ab4c32a4a230561642'
s=b.decode()
s=s.replace('        assertArrayEquals(new byte[] {0, -1, 4}, pdc.get(key("old:bytes"), PersistentDataType.BYTE_ARRAY));\n','')
s=s.replace('        data.set(key("old:bytes"), PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 4});\n','')
s=s.replace('// FLOAT and nested PDC success are tested on real Paper; this mock changes their YAML types.', '// FLOAT, byte-array and nested PDC success are tested on real Paper; this mock has distinct type/equality behavior.')
needle='    private Path write(String name, String contents) throws IOException {'
addition='''    @Test void mockArrayReferenceEqualityCannotBypassExactRoundTripGuard() throws Exception {
        store.load();
        var shop = richShop("ArrayMock");
        assertTrue(store.save(shop), () -> errors.toString());
        byte[] original = Files.readAllBytes(directory.resolve("ArrayMock.yml"));
        ItemStack item = shop.trades.getFirst().result;
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key("old:bytes"), PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 4});
        item.setItemMeta(meta);
        ItemStack before = item.clone();
        var encoded = new YamlConfiguration();
        encoded.set("item", item);
        var decoded = new YamlConfiguration();
        decoded.loadFromString(encoded.saveToString());
        // The pinned mock compares its backing Map values by equals, so arrays compare
        // by reference even though their bytes survive. Production must not special-case it.
        assertArrayEquals(new byte[] {0, -1, 4}, decoded.getItemStack("item").getItemMeta()
                .getPersistentDataContainer().get(key("old:bytes"), PersistentDataType.BYTE_ARRAY));
        assertNotEquals(item, decoded.getItemStack("item"));
        assertFalse(store.save(shop));
        assertEquals(before, item);
        assertArrayEquals(original, Files.readAllBytes(directory.resolve("ArrayMock.yml")));
        assertFalse(store.isAvailable(shop));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Trade data changed during serialization")), () -> errors.toString());
    }

'''
assert s.count(needle)==1
s=s.replace(needle,addition+needle);p.write_text(s)
assert blob(p.read_bytes())=='bfdfafeb7907a9b7a1d9d1fffa6f2a30b283e902'
