from pathlib import Path
import hashlib, shutil


def edit(name, expected, transform):
    path = Path(name)
    data = path.read_bytes()
    assert hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest() == expected, name
    path.write_text(transform(data.decode()), encoding='utf-8')


def gui(text):
    start = text.index('        if (!trade.isFree && trade.costs != null) {', text.index('    private void handleBlackMarketPurchase'))
    end = text.index('        HashMap<Integer, ItemStack> overflow', start)
    text = text[:start] + '''        if (trade.result == null || trade.result.getType().isAir() || trade.result.getAmount() <= 0) return;
        ItemStack reward = trade.result.clone();
        if (!trade.isFree && !takePayment(player, trade.costs)) return;

''' + text[end:]
    assert text.count('player.getInventory().addItem(trade.result.clone())') == 2
    text = text.replace('player.getInventory().addItem(trade.result.clone())', 'player.getInventory().addItem(reward)')
    old = '''        int index = getTradeIndexBySlot(slot);
        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null || index == -1 || index >= shop.trades.size()) return;

        ShopManager.Trade trade = shop.trades.get(index);
        if (trade.result == null) return;'''
    new = '''        ShopManager.Shop shop = ShopManager.getShop(shopName);
        if (shop == null) return;
        int page = shopTradesPage.getOrDefault(player.getUniqueId(), Collections.emptyMap()).getOrDefault(shopName, 0);
        int index = purchaseIndex(page, slot, shop.trades.size());
        if (index == -1) return;

        ShopManager.Trade trade = shop.trades.get(index);
        if (trade.result == null || trade.result.getType().isAir() || trade.result.getAmount() <= 0) return;
        ItemStack reward = trade.result.clone();'''
    assert text.count(old) == 1
    text = text.replace(old, new)
    start = text.index('        for (ItemStack cost : trade.costItems) {', text.index('    private void handlePurchase'))
    end = text.index('        HashMap<Integer, ItemStack> overflow', start)
    text = text[:start] + '        if (!takePayment(player, trade.costItems)) return;\n\n' + text[end:]
    start = text.index('    private void removeItems(')
    end = text.index('    private int getTradeIndexBySlot', start)
    text = text[:start] + '''    private boolean takePayment(Player player, List<ItemStack> costs) {
        final ItemStack missing;
        try {
            missing = InventoryPayment.debit(player.getInventory(), costs);
        } catch (IllegalArgumentException invalidCost) {
            player.sendMessage(ChatColor.RED + "This trade has an invalid cost; no items were taken.");
            return false;
        }
        if (missing == null) return true;
        player.sendMessage(ChatColor.RED + "Missing items: " + ItemStackHelper.getDisplayName(missing)
                + " x" + missing.getAmount() + ". No items were taken.");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        return false;
    }

    static int purchaseIndex(int page, int slot, int tradeCount) {
        if (page < 0 || tradeCount <= 0) return -1;
        for (int offset = 0; offset < CONTENT_SLOTS.length; offset++) {
            if (CONTENT_SLOTS[offset] == slot) {
                long index = (long) page * CONTENT_SLOTS.length + offset;
                return index < tradeCount ? (int) index : -1;
            }
        }
        return -1;
    }

''' + text[end:]
    start = text.index('    public static void openShopTrades(Player player, String shopName, int page) {')
    end = text.index('    // =================', start)
    section = text[start:end]
    section = section.replace('        shopTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(shopName, page);\n', '')
    old = '        page = Math.min(page, maxPage);'
    assert section.count(old) == 1
    section = section.replace(old, '''        page = Math.max(0, Math.min(page, maxPage));
        shopTradesPage.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(shopName, page);''')
    return text[:start] + section + text[end:]


def pom(text):
    text = text.replace('        <plugins>', '''        <plugins>
            <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>3.5.2</version></plugin>''', 1)
    return text.replace('    <dependencies>', '''    <dependencies>
        <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.12.2</version><scope>test</scope></dependency>
        <dependency><groupId>org.mockbukkit.mockbukkit</groupId><artifactId>mockbukkit-v1.21</artifactId><version>4.110.0</version><scope>test</scope></dependency>''', 1)


def workflow(text):
    old = 'run: mvn --batch-mode --no-transfer-progress -DskipTests -Dpaper.version=1.21.11-R0.1-SNAPSHOT clean package'
    assert text.count(old) == 1
    return text.replace(old, 'run: mvn --batch-mode --no-transfer-progress -Dpaper.version=1.21.11-R0.1-SNAPSHOT clean verify')


edit('src/main/java/io/Yomicer/magicExpansion/utils/shop/ShopGUI.java', '6fd8ac3a940d995e0a2d13a41c42531e22aff240', gui)
edit('pom.xml', '953c7c9e8a919128e1582e6e06b2f2f68dc71966', pom)
edit('.github/workflows/build.yml', '0762505983cc51020f9312a4c2a22277a885a5d6', workflow)
for name, source_set in [('InventoryPayment.java', 'main'), ('InventoryPaymentTest.java', 'test'), ('ShopPageTest.java', 'test')]:
    target = Path('src') / source_set / 'java/io/Yomicer/magicExpansion/utils/shop' / name
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(Path('.audit') / name, target)
