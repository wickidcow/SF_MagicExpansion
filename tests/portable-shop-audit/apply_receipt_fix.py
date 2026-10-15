"""One-shot, exact-baseline edits on the isolated test branch; no production deployment."""
from pathlib import Path
import subprocess
root=Path('src/main/java/io/Yomicer/magicExpansion/utils/shop')
for name,sha in {'ShopManager.java':'755dbba6fb29f87562239caab30d3b2aaa7e12b7','ShopGUI.java':'50e386094ebb30669680eeff14a35dcd11520424'}.items():
    assert subprocess.check_output(['git','hash-object',str(root/name)],text=True).strip()==sha,'Source moved; refuse to overwrite '+name
manager=root/'ShopManager.java'
text=manager.read_text()
old='''    public static void recordPurchase(UUID playerId, Shop shop, Trade trade) {
        if (trade.globalLimit > 0) trade.globalUsed++;
        if (trade.personalLimit > 0) {
            trade.personalUsed.put(playerId, trade.personalUsed.getOrDefault(playerId, 0) + 1);
        }
        saveShop(shop);
    }
'''
new='''    /** Compatibility entry point. Reward issuers must use tryRecordPurchase and check its result. */
    public static void recordPurchase(UUID playerId, Shop shop, Trade trade) {
        tryRecordPurchase(playerId, shop, trade);
    }

    /** Persist the purchase before delivering rewards; stale/read-only shops cannot authorize one. */
    public static boolean tryRecordPurchase(UUID playerId, Shop shop, Trade trade) {
        if (playerId == null || shop == null || trade == null || !isAvailable(shop)
                || !shop.trades.contains(trade) || !canPurchase(playerId, trade)) return false;
        return ShopPurchaseLedger.commit(playerId, trade, () -> trySaveShop(shop));
    }
'''
assert text.count(old)==1;manager.write_text(text.replace(old,new))
gui=root/'ShopGUI.java';text=gui.read_text()
old='''        if (!takePayment(player, trade.costItems)) return;

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
'''
new='''        // Snapshot only storage slots, the same slots used by InventoryPayment.
        // This server-thread path invokes no inventory callbacks between debit and save.
        ItemStack[] beforePayment = java.util.Arrays.stream(player.getInventory().getStorageContents())
                .map(item -> item == null ? null : item.clone()).toArray(ItemStack[]::new);
        if (!takePayment(player, trade.costItems)) return;

        if (!ShopManager.tryRecordPurchase(playerId, shop, trade)) {
            player.getInventory().setStorageContents(beforePayment);
            player.sendMessage(ChatColor.RED + "Trade cancelled: purchase history could not be saved. Payment restored; no reward was issued. Contact an administrator.");
            return;
        }

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
'''
assert text.count(old)==1;text=text.replace(old,new)
old='''        ShopManager.recordPurchase(playerId, shop, trade);

'''
assert text.count(old)==1;gui.write_text(text.replace(old,''))
# Candidate-only audit: retain the published-JAR checksum check, then deliberately test
# the built source candidate rather than claiming the published JAR contains this fix.
f=Path('.github/workflows/portable-shop-audit.yml');text=f.read_text()
needle="          echo 'e3552fc5d54dc282194428d593d08c939f71172182075667ac87d10571b192dd  audit/server/plugins/MagicExpansion.jar' | sha256sum -c -\n"
assert text.count(needle)==1
f.write_text(text.replace(needle,needle+'          cp target/SF_MagicExpansion1.1.14.jar audit/server/plugins/MagicExpansion.jar\n          sha256sum audit/server/plugins/MagicExpansion.jar > audit/candidate-jar.sha256\n'))
# Add real item-definition checks for every authored custom payment.
f=Path('tests/portable-shop-audit/PortableShopProbe.java');text=f.read_text()
needle='                check(!trade.costItems.isEmpty()&&trade.costItems.stream().allMatch(i->i!=null&&!i.getType().isAir()&&i.getAmount()>0),"Invalid payment");\n'
addition='''                for (ItemStack cost : trade.costItems) {
                    var registered = io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem.getByItem(cost);
                    if (registered != null) {
                        check(cost.isSimilar(nativeStack(registered.getItem())), "Authored custom cost differs from the actual registered item: " + registered.getId());
                    }
                }
'''
assert text.count(needle)==1;f.write_text(text.replace(needle,needle+addition))
# Fix test declaration spacing before compilation.
f=Path('src/test/java/io/Yomicer/magicExpansion/utils/shop/ShopPurchaseLedgerTest.java');text=f.read_text()
text=text.replace('@Test voidUnlimited','@Test void unlimited').replace('@Test voidMaximum','@Test void maximum').replace('@Test voidNull','@Test void null')
f.write_text(text)
subprocess.run(['git','diff','--check'],check=True)
