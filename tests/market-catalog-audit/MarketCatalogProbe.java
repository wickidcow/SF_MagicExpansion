package audit;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.shop.MarketCatalog;
import io.Yomicer.magicExpansion.utils.shop.ShopGUI;
import io.Yomicer.magicExpansion.utils.shop.ShopManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/** Native item/payment/receipt audit with synthetic identities; no network client is simulated. */
public final class MarketCatalogProbe extends JavaPlugin {
    private final ShopGUI gui=new ShopGUI();
    private final Deque<Step> pending=new ArrayDeque<>();
    private Method purchase;
    private int[] slots;
    private Path shops;
    private String phase;
    private int expected, purchases, refused, insufficient;
    private BukkitTask task;
    @FunctionalInterface private interface Step { void run() throws Exception; }
    @Override public void onEnable() {
        if(!Boolean.getBoolean("market.catalog.audit")||!Bukkit.getOnlinePlayers().isEmpty()) throw new IllegalStateException("Isolated audit only");
        Bukkit.getScheduler().runTaskLater(this,this::prepare,60L);
    }
    private void prepare() {
        try {
            phase=System.getProperty("market.catalog.phase","invalid");
            check(Set.of("first","restart").contains(phase),"Missing phase");
            boolean optional=Boolean.getBoolean("market.catalog.optional");
            check(optional==Bukkit.getPluginManager().isPluginEnabled("InfinityExpansion2"),"Wrong optional-addon environment");
            expected=optional?229:173;
            shops=MagicExpansion.getInstance().getDataFolder().toPath().resolve("portable_shops");
            purchase=ShopGUI.class.getDeclaredMethod("handlePurchase",Player.class,String.class,int.class,ItemStack.class);
            purchase.setAccessible(true);
            Field slotField=ShopGUI.class.getDeclaredField("CONTENT_SLOTS");slotField.setAccessible(true);slots=((int[])slotField.get(null)).clone();
            if(phase.equals("first")) {
                check(ShopManager.getShops().isEmpty(),"No existing shops permitted in disposable audit");
                MarketCatalog.populate(Bukkit.getConsoleSender());
            }
            int actual=ShopManager.getShops().stream().mapToInt(s->s.trades.size()).sum();
            Files.writeString(Path.of("catalog-loaded-count.txt"),actual+" of "+expected+"\n");
            check(actual==expected,"Wrong catalog offer count: "+actual+" expected "+expected);
            validateDefinitions();
            for(String user:List.of("catalog-alice","catalog-bob")) {
                Client client=new Client(user);
                for(var shop:List.copyOf(ShopManager.getShops())) for(int i=0;i<shop.trades.size();i++) {
                    String name=shop.name;int index=i;
                    pending.add(()->exercise(client,name,index));
                }
            }
            pending.add(()->{
                Map<Path,byte[]> snapshots=new LinkedHashMap<>();
                try(var paths=Files.list(shops)){for(Path p:paths.filter(p->p.toString().endsWith(".yml")).toList()) snapshots.put(p,Files.readAllBytes(p));}
                MarketCatalog.populate(Bukkit.getConsoleSender());
                for(var e:snapshots.entrySet())check(Arrays.equals(e.getValue(),Files.readAllBytes(e.getKey())),"Regeneration rewrote existing offers/receipts");
                ShopManager.reload();
                for(String user:List.of("catalog-alice","catalog-bob"))for(var shop:ShopManager.getShops())for(var trade:shop.trades)
                    check(trade.personalUsed.getOrDefault(UUID.nameUUIDFromBytes(("market-catalog-audit:"+user).getBytes(StandardCharsets.UTF_8)),0)==trade.personalLimit,"Reload lost usage");
            });
            pending.add(this::finish);
            task=Bukkit.getScheduler().runTaskTimer(this,()->{
                try {check(!pending.isEmpty(),"Empty audit queue");pending.removeFirst().run();}
                catch(Throwable failure){fail(failure);}
            },1L,2L);
        } catch(Throwable failure){fail(failure);}
    }
    private void validateDefinitions() throws Exception {
        Set<String> ids=new HashSet<>();
        for(var shop:ShopManager.getShops())for(var trade:shop.trades) {
            check(trade.catalogId!=null&&ids.add(trade.catalogId)&&shop.catalogHistory.contains(trade.catalogId),"Bad stable catalog identity");
            check(trade.personalLimit>0&&trade.globalLimit==0,"Unbounded or shared-server limit");
            SlimefunItem registered=SlimefunItem.getByItem(trade.result);
            check(registered!=null&&!registered.isDisabled(),"Reward is not registered");
            check(trade.result.isSimilar(nativeStack(registered.getItem())),"Reward metadata differs from canonical item: "+registered.getId());
            check(!trade.costItems.isEmpty(),"Free offer");
            for(ItemStack cost:trade.costItems) {
                check(cost!=null&&!cost.getType().isAir()&&cost.getAmount()>0,"Missing cost");
                var sf=SlimefunItem.getByItem(cost);
                if(sf!=null)check(cost.isSimilar(nativeStack(sf.getItem())),"Payment metadata differs from canonical item");
            }
        }
    }
    private void exercise(Client client,String name,int index) throws Exception {
        var shop=ShopManager.getShop(name);check(shop!=null,"Shop unavailable");var trade=shop.trades.get(index);
        if(phase.equals("first")) {
            check(ShopManager.canPurchase(client.id,trade),"Other player spent allowance or item gate rejected valid offer");
            client.storage.clear();
            ItemStack partial=nativeStack(trade.costItems.getFirst());partial.setAmount(partial.getAmount()-1);
            if(partial.getAmount()>0)client.storage.addItem(partial);
            ItemStack[] before=copy(client.storage.getStorageContents());byte[] disk=Files.readAllBytes(shops.resolve(name+".yml"));
            click(client,name,index,trade.result);
            check(Arrays.equals(before,client.storage.getStorageContents()),"Insufficient payment changed inventory");
            check(Arrays.equals(disk,Files.readAllBytes(shops.resolve(name+".yml"))),"Unpaid trade changed receipt");insufficient++;
            for(int count=0;count<trade.personalLimit;count++) {
                supply(client,trade);click(client,name,index,trade.result);
                check(trade.personalUsed.getOrDefault(client.id,0)==count+1,"Receipt count incorrect");
                assertContents(client.storage,List.of(nativeStack(trade.result)));purchases++;
            }
        }
        check(trade.personalUsed.getOrDefault(client.id,0)==trade.personalLimit,"Persisted cap incorrect");
        check(!ShopManager.canPurchase(client.id,trade),"Spent offer remains authorized");
        supply(client,trade);ItemStack[] before=copy(client.storage.getStorageContents());
        byte[] disk=Files.readAllBytes(shops.resolve(name+".yml"));click(client,name,index,trade.result);
        check(Arrays.equals(before,client.storage.getStorageContents()),"Over-limit trade changed inventory");
        check(Arrays.equals(disk,Files.readAllBytes(shops.resolve(name+".yml"))),"Over-limit trade changed receipt");refused++;
    }
    private void supply(Client c,ShopManager.Trade trade) {
        c.storage.clear();for(var cost:trade.costItems)check(c.storage.addItem(nativeStack(cost)).isEmpty(),"Payment fixture overflow");
    }
    private void click(Client c,String name,int index,ItemStack reward) throws Exception {
        ShopGUI.openShopTrades(c.player,name,index/slots.length);
        check(c.menu!=null&&c.menu.getItem(slots[index%slots.length])!=null,"Offer icon missing");
        purchase.invoke(gui,c.player,name,slots[index%slots.length],reward);
    }
    private void finish() throws Exception {
        check(refused==expected*2,"Every offer must refuse repeats for two players");
        if(phase.equals("first"))check(insufficient==expected*2&&purchases>expected*2,"All payment and bounded bulk tests required");
        YamlConfiguration result=new YamlConfiguration();
        result.set("passed",true);result.set("phase",phase);result.set("pid",ProcessHandle.current().pid());
        result.set("offers",expected);result.set("successful-purchases",purchases);result.set("repeat-refusals",refused);result.set("insufficient-payment-refusals",insufficient);
        result.save(Path.of("catalog-"+phase+"-result.yml").toFile());
        task.cancel();Bukkit.shutdown();
    }
    private void fail(Throwable failure) {
        failure.printStackTrace();
        try{Files.writeString(Path.of("catalog-fatal.txt"),failure.toString());}catch(Exception ignored){}
        if(task!=null)task.cancel();Bukkit.shutdown();
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static ItemStack nativeStack(ItemStack i){return ItemStack.deserialize(i.serialize());}
    private static ItemStack[] copy(ItemStack[] input){return Arrays.stream(input).map(i->i==null?null:nativeStack(i)).toArray(ItemStack[]::new);}
    private static void assertContents(Inventory inventory,List<ItemStack> expected) {
        List<ItemStack> remaining=new ArrayList<>();
        for(var i:inventory.getStorageContents())if(i!=null&&!i.getType().isAir())remaining.add(nativeStack(i));
        for(var item:expected){int needed=item.getAmount();for(var stored:remaining)if(needed>0&&stored.isSimilar(item)){int take=Math.min(needed,stored.getAmount());needed-=take;stored.setAmount(stored.getAmount()-take);}check(needed==0,"Item identity or quantity lost");}
        check(remaining.stream().allMatch(i->i.getAmount()==0),"Unexpected reward/payment remainder");
    }
    private final class Client {
        final UUID id;final Inventory storage=Bukkit.createInventory(null,36);final PlayerInventory inventory;final Player player;Inventory menu;
        Client(String name){
            id=UUID.nameUUIDFromBytes(("market-catalog-audit:"+name).getBytes(StandardCharsets.UTF_8));
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
                case "playSound","sendMessage"->null;case "getName","toString"->name;case "isOnline"->true;case "hasPermission","isOp"->false;
                default->throw new UnsupportedOperationException("Unexpected player call "+m.getName());
            });
        }
    }
}
