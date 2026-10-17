package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.shop.BlackMarketManager;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import io.Yomicer.magicExpansion.utils.shop.ShopManager;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/** Disposable native-server test of authored shops; never distributed with the addon. */
public final class PortableShopProbe extends JavaPlugin {
    private final ShopGUI gui = new ShopGUI();
    private final Deque<Step> pending = new ArrayDeque<>();
    private final List<String> names = List.of("Artificer - Machines", "Artificer - Materials", "Garran - Supplies", "Xur");
    private Path shops, evidence;
    private Method purchase;
    private int[] slots;
    private int purchases, rejections, insufficient, noShopsUsers, reloadedRejections;
    private boolean unsafeReward, duplicateAfterFailedReceipt;
    private String phase, stage = "initialization";
    private BukkitTask task;
    @FunctionalInterface private interface Step { void run() throws Exception; }

    @Override public void onEnable() {
        if (!Boolean.getBoolean("portable.shop.audit") || !Bukkit.getOnlinePlayers().isEmpty()) {
            throw new IllegalStateException("Requires isolated audit flag and no online players");
        }
        Bukkit.getScheduler().runTaskLater(this, this::prepare, 40L);
    }

    private void prepare() {
        try {
            phase = System.getProperty("portable.shop.phase", "invalid");
            check(Set.of("first", "restart").contains(phase), "Explicit phase required");
            shops = MagicExpansion.getInstance().getDataFolder().toPath().resolve("portable_shops");
            evidence = getDataFolder().toPath(); Files.createDirectories(evidence);
            purchase = ShopGUI.class.getDeclaredMethod("handlePurchase", Player.class, String.class, int.class, ItemStack.class);
            purchase.setAccessible(true);
            Field field = ShopGUI.class.getDeclaredField("CONTENT_SLOTS"); field.setAccessible(true);
            slots = ((int[]) field.get(null)).clone();
            if (phase.equals("first")) {
                check(ShopManager.getShops().isEmpty(), "No preexisting shop may enter the audit");
                for (int i=0; i<3; i++) {
                    Client c = new Client("no-shops-" + i);
                    check(BlackMarketManager.getTodayTrades(c.player).size()==10, "Black Market needs no authored shops");
                    noShopsUsers++;
                }
                check(BlackMarketManager.getCycleStatusForOperator().stream().anyMatch(s -> s.endsWith("§f0")), "Unexpected blocked players");
                for (String name : names) {
                    Path input = Path.of("../fixtures", name+".yml");
                    Files.copy(input, shops.resolve(name+".yml"));
                }
                ShopManager.reload();
                check(ShopManager.getShops().size()==4, "All four new shops must load");
                validateDefinitions();
                for (String user : List.of("alice", "bob")) enqueueNormalPurchases(user);
                pending.add(() -> {
                    ShopManager.reload();
                    for (String user : List.of("alice", "bob")) for (String name : names) for (int i=0;i<8;i++) {
                        Client c=new Client(user); rejectRepeat(c,name,i); reloadedRejections++;
                    }
                });
                pending.add(this::characterizeFailedReceipt);
            } else {
                check(Files.isRegularFile(Path.of("shop-first-result.yml")), "First-process evidence required");
                for (String user : List.of("alice", "bob")) {
                    Client c=new Client(user);
                    for (String name : names) for (int i=0;i<8;i++) {
                        int index=i; pending.add(() -> { rejectRepeat(c,name,index); rejections++; });
                    }
                }
                pending.add(this::checkFailedReceiptAfterRestart);
            }
            pending.add(this::finish);
            task=Bukkit.getScheduler().runTaskTimer(this, () -> {
                try { check(!pending.isEmpty(),"Empty audit queue"); pending.removeFirst().run(); }
                catch(Throwable e) { fail(e); }
            },1L,2L);
        } catch(Throwable e) { fail(e); }
    }

    private void validateDefinitions() {
        for(String name:names) {
            var shop=ShopManager.getShop(name); check(shop!=null&&shop.trades.size()==8,"Bad shop: "+name);
            for(var trade:shop.trades) {
                check(trade.personalLimit==1&&trade.globalLimit==0,"Wrong once-per-player limit");
                check(trade.personalUsed.isEmpty()&&trade.globalUsed==0,"Must be unused authored fixtures");
                check(trade.result!=null&&!trade.result.getType().isAir()&&trade.result.getAmount()>0,"Invalid reward");
                check(!trade.costItems.isEmpty()&&trade.costItems.stream().allMatch(i->i!=null&&!i.getType().isAir()&&i.getAmount()>0),"Invalid payment");
                for (ItemStack cost : trade.costItems) {
                    var registered = io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem.getByItem(cost);
                    if (registered != null) {
                        check(cost.isSimilar(nativeStack(registered.getItem())), "Authored custom cost differs from the actual registered item: " + registered.getId());
                    }
                }
            }
        }
    }

    private void enqueueNormalPurchases(String user) {
        Client c=new Client(user);
        for(String name:names) for(int i=0;i<8;i++) {
            int index=i;
            pending.add(() -> {
                stage=user+"/"+name+"/"+index;
                var shop=ShopManager.getShop(name); check(shop!=null,"Shop became unavailable");
                var trade=shop.trades.get(index);
                ShopGUI.openShopTrades(c.player,name);
                check(c.menu!=null,"Shop menu did not open");
                check(c.menu.getItem(slots[index]).isSimilar(trade.result)||c.menu.getItem(slots[index]).getType()==trade.result.getType(),"Missing shop offer icon");
                check(ShopManager.canPurchase(c.id,trade),"Another player spent this player's allowance");
                c.storage.clear();
                ItemStack partial=nativeStack(trade.costItems.get(0)); partial.setAmount(partial.getAmount()-1);
                if(partial.getAmount()>0) check(c.storage.addItem(partial).isEmpty(),"Fixture overflow");
                var before=copy(c.storage.getStorageContents()); byte[] disk=Files.readAllBytes(shopFile(name));
                click(c,name,index,trade.result);
                check(Arrays.equals(before,c.storage.getStorageContents()),"Incomplete payment removed items or gave reward");
                check(Arrays.equals(disk,Files.readAllBytes(shopFile(name))),"Unpaid trade changed receipt");
                insufficient++;
                c.storage.clear();
                for(ItemStack cost:trade.costItems) check(c.storage.addItem(nativeStack(cost)).isEmpty(),"Payment fixture overflow");
                ItemStack expected=nativeStack(trade.result);
                click(c,name,index,trade.result);
                check(usage(name,index,c.id)==1,"Purchase was not saved; last player message: " + c.lastMessage);
                assertContents(c.storage,List.of(expected));
                check(!ShopManager.canPurchase(c.id,trade),"Repeat purchase remains authorized");
                purchases++;
                rejectRepeat(c,name,index); rejections++;
            });
        }
    }

    private void rejectRepeat(Client c,String name,int index) throws Exception {
        stage="duplicate/"+name+"/"+index;
        var shop=ShopManager.getShop(name); check(shop!=null,"Shop unavailable after restart/reload");
        var t=shop.trades.get(index);
        check(usage(name,index,c.id)==1,"Missing personal counter");
        c.storage.clear();
        for(ItemStack cost:t.costItems) check(c.storage.addItem(nativeStack(cost)).isEmpty(),"Repeat fixture overflow");
        var before=copy(c.storage.getStorageContents()); byte[] disk=Files.readAllBytes(shopFile(name));
        click(c,name,index,t.result);
        check(Arrays.equals(before,c.storage.getStorageContents()),"Duplicate changed inventory");
        check(Arrays.equals(disk,Files.readAllBytes(shopFile(name))),"Duplicate changed receipt");
    }

    private void characterizeFailedReceipt() throws Exception {
        stage="refused-receipt";
        String name="Audit Refused Receipt";
        check(ShopManager.tryCreateShop(name),"Create isolated fault fixture");
        var shop=ShopManager.getShop(name); var source=ShopManager.getShop("Garran - Supplies").trades.get(0);
        var t=new ShopManager.Trade(); t.result=nativeStack(source.result); t.personalLimit=1;
        for(var cost:source.costItems)t.costItems.add(nativeStack(cost));
        shop.trades.add(t); check(ShopManager.trySaveShop(shop),"Save fault fixture");
        Client c=new Client("receipt-fault");
        for(ItemStack cost:t.costItems)check(c.storage.addItem(nativeStack(cost)).isEmpty(),"Fault payment overflow");
        Files.writeString(shopFile(name),"\n# controlled external audit writer\n",StandardOpenOption.APPEND);
        byte[] external=Files.readAllBytes(shopFile(name));
        click(c,name,0,t.result);
        check(Arrays.equals(external,Files.readAllBytes(shopFile(name))),"Failure overwrote external evidence");
        check(usage(name,0,c.id)==0,"Fault unexpectedly committed a receipt");
        unsafeReward=Arrays.stream(c.storage.getStorageContents()).filter(Objects::nonNull).anyMatch(i->i.isSimilar(t.result));
        if(!unsafeReward)assertContents(c.storage,t.costItems);
        check(ShopManager.getShop("Xur")!=null,"Fault disabled unrelated shops");
    }

    private void checkFailedReceiptAfterRestart() throws Exception {
        String name="Audit Refused Receipt"; Client c=new Client("receipt-fault");
        var shop=ShopManager.getShop(name); check(shop!=null,"Readable unchanged shop should load after restart");
        var t=shop.trades.get(0);
        check(usage(name,0,c.id)==0,"Fault fixture has a prior saved receipt");
        for(var cost:t.costItems)check(c.storage.addItem(nativeStack(cost)).isEmpty(),"Restart fault overflow");
        click(c,name,0,t.result); assertContents(c.storage,List.of(t.result));
        check(usage(name,0,c.id)==1,"Normal post-restart receipt not persisted");
        var previous=new YamlConfiguration();previous.loadFromString(Files.readString(Path.of("shop-first-result.yml")));
        duplicateAfterFailedReceipt=previous.getBoolean("unsafe-reward-without-receipt");
    }

    private void finish() throws Exception {
        if(phase.equals("first"))check(purchases==64&&rejections==64&&insufficient==64&&reloadedRejections==64&&noShopsUsers==3,"Incomplete first-phase coverage");
        else check(rejections==64,"Incomplete restart coverage");
        YamlConfiguration result=new YamlConfiguration();
        result.set("normal-paths-passed",true);result.set("phase",phase);result.set("pid",ProcessHandle.current().pid());
        result.set("server",Bukkit.getVersion());result.set("addon",MagicExpansion.getInstance().getDescription().getVersion());
        result.set("new-purchases",purchases);result.set("duplicate-rejections",rejections);
        result.set("reload-duplicate-rejections",reloadedRejections);result.set("insufficient-payments",insufficient);
        result.set("black-market-users-without-authored-shops",noShopsUsers);
        result.set("unsafe-reward-without-receipt",unsafeReward);result.set("repeat-after-refused-receipt-and-restart",duplicateAfterFailedReceipt);
        Files.writeString(Path.of("shop-"+phase+"-result.yml"),result.saveToString());
        getLogger().info(result.saveToString());task.cancel();Bukkit.shutdown();
    }
    private void fail(Throwable e) {
        if(task!=null)task.cancel();while(e instanceof InvocationTargetException&&e.getCause()!=null)e=e.getCause();e.printStackTrace();
        try{Files.writeString(Path.of("shop-"+phase+"-fatal.txt"),stage+": "+e);}catch(Exception ignored){}
        Bukkit.shutdown();
    }
    private Path shopFile(String name){return shops.resolve(name+".yml");}
    private int usage(String name,int index,UUID player) throws Exception {
        var y=new YamlConfiguration();y.loadFromString(Files.readString(shopFile(name)));
        var trade=y.getMapList("trades").get(index);var used=(Map<?,?>)trade.get("personalUsed");
        Object n=used.get(player.toString());return n==null?0:((Number)n).intValue();
    }
    private void click(Client c,String shop,int index,ItemStack item)throws Exception{purchase.invoke(gui,c.player,shop,slots[index],item);}
    private static void check(boolean pass,String message){if(!pass)throw new AssertionError(message);}
    private static ItemStack nativeStack(ItemStack i){return ItemStack.deserialize(i.serialize());}
    private static ItemStack[] copy(ItemStack[] input){return Arrays.stream(input).map(i->i==null?null:nativeStack(i)).toArray(ItemStack[]::new);}
    private static void assertContents(Inventory inventory,List<ItemStack> expected) {
        List<ItemStack> remaining=new ArrayList<>();
        for(var i:inventory.getStorageContents())if(i!=null&&!i.getType().isAir())remaining.add(nativeStack(i));
        for(var item:expected){int needed=item.getAmount();for(var stored:remaining)if(needed>0&&stored.isSimilar(item)){int take=Math.min(needed,stored.getAmount());needed-=take;stored.setAmount(stored.getAmount()-take);}check(needed==0,"Item identity or quantity lost");}
        check(remaining.stream().allMatch(i->i.getAmount()==0),"Unexpected reward/payment remainder");
    }
    private final class Client {
        final UUID id;final Inventory storage=Bukkit.createInventory(null,36);final PlayerInventory inventory;final Player player;Inventory menu;String lastMessage;
        Client(String name){
            id=UUID.nameUUIDFromBytes(("portable-shop-audit:"+name).getBytes(StandardCharsets.UTF_8));
            inventory=(PlayerInventory)Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(),new Class<?>[]{PlayerInventory.class},(p,m,a)->switch(m.getName()){
                case "getStorageContents","getContents"->storage.getStorageContents();
                case "setStorageContents","setContents"->{storage.setStorageContents((ItemStack[])a[0]);yield null;}
                case "addItem"->storage.addItem((ItemStack[])a[0]);
                default->throw new UnsupportedOperationException("Unexpected inventory call "+m.getName());
            });
            player=(Player)Proxy.newProxyInstance(Player.class.getClassLoader(),new Class<?>[]{Player.class},(p,m,a)->switch(m.getName()){
                case "getUniqueId"->id;case "getInventory"->inventory;
                case "getLocation"->Bukkit.getWorlds().get(0).getSpawnLocation();case "getWorld"->Bukkit.getWorlds().get(0);
                case "openInventory"->{menu=(Inventory)a[0];yield null;}
                case "sendMessage"->{lastMessage=String.valueOf(a[0]);yield null;}
                case "playSound"->null;case "getName","toString"->name;case "isOnline"->true;case "hasPermission","isOp"->false;
                default->throw new UnsupportedOperationException("Unexpected player call "+m.getName());
            });
        }
    }
}
