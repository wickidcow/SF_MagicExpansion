package audit;

import static io.Yomicer.magicExpansion.utils.itemUtils.newItem.idDecorator;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.misc.MagicCrop;
import io.Yomicer.magicExpansion.items.misc.PageChest;
import io.Yomicer.magicExpansion.items.skyBlock.SingleCubeOre;
import io.Yomicer.magicExpansion.items.skyBlock.SingleCubeOrigin;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Real-server conservation probe for Page Chest and One Block world-mutation primitives.
 */
public final class NoveltyContainerProbe extends JavaPlugin {
    private final List<Chunk> heldChunks = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runProbe();
                publish("NOVELTY_CONTAINER_PASS\n"
                        + "page_chest=exact\n"
                        + "page_corruption=fail_closed\n"
                        + "page_break=exact_once\n"
                        + "single_cube_identity=preserved\n"
                        + "magic_crop=seed_and_3_to_9_fruits\n");
            } catch (Throwable error) {
                getLogger().log(Level.SEVERE, "NOVELTY_CONTAINER_FATAL", error);
                publish("NOVELTY_CONTAINER_FAIL\nfatal=" + error + "\n");
            }
            requestShutdown();
        }, 60L);
    }

    @Override
    public void onDisable() {
        for (Chunk chunk : heldChunks) {
            chunk.removePluginChunkTicket(this);
        }
    }

    private void runProbe() throws Exception {
        check(MagicExpansion.getInstance() != null && MagicExpansion.getInstance().isEnabled(),
                "MagicExpansion is not enabled");

        SlimefunItem pageItem = SlimefunItem.getById(idDecorator("PAGE_CHEST"));
        SlimefunItem originItem = SlimefunItem.getById(idDecorator("SINGLE_CUBE_ORIGIN"));
        SlimefunItem oreItem = SlimefunItem.getById(idDecorator("SINGLE_CUBE_ORE"));
        SlimefunItem cropItem = SlimefunItem.getById(idDecorator("WHEAT_SEEDS"));
        check(pageItem instanceof PageChest, "PAGE_CHEST registration missing");
        check(originItem instanceof SingleCubeOrigin, "SINGLE_CUBE_ORIGIN registration missing");
        check(oreItem instanceof SingleCubeOre, "SINGLE_CUBE_ORE registration missing");
        check(cropItem instanceof MagicCrop, "WHEAT_SEEDS Magic Crop registration missing");

        Location pageLoc = location(18_200, 80, 18_200);
        Fixture page = createFixture(pageItem, pageLoc);
        try {
            auditPageChest(pageLoc, page.menu());
        } finally {
            cleanup(pageLoc);
        }

        Location originLoc = location(18_220, 80, 18_200);
        Fixture origin = createFixture(originItem, originLoc);
        try {
            auditOrigin((SingleCubeOrigin) originItem, originLoc);
        } finally {
            cleanup(originLoc);
        }

        Location oreLoc = location(18_240, 80, 18_200);
        Fixture ore = createFixture(oreItem, oreLoc);
        try {
            auditOre((SingleCubeOre) oreItem, oreLoc);
        } finally {
            cleanup(oreLoc);
        }

        Location cropLoc = location(18_260, 80, 18_200);
        auditCrop((MagicCrop) cropItem, cropLoc);
    }

    private void auditPageChest(Location location, BlockMenu menu) throws Exception {
        ItemStack active = tagged(Material.DIAMOND, 7, "active");
        ItemStack second = tagged(Material.EMERALD, 11, "second");
        ItemStack third = tagged(Material.GOLD_INGOT, 13, "third");

        menu.toInventory().clear();
        menu.replaceExistingItem(0, active.clone());
        BlockStorage.addBlockInfo(location, PageChest.DATA_CURRENT_PAGE, "1");

        ItemStack[] page2 = new ItemStack[PageChest.CHEST_SIZE];
        page2[4] = second.clone();
        PageChest.savePage(location, 2, page2);

        ItemStack[] page3 = new ItemStack[PageChest.CHEST_SIZE];
        page3[8] = third.clone();
        PageChest.savePage(location, 3, page3);

        ItemStack[] loaded2 = PageChest.loadPage(location, 2);
        ItemStack[] loaded3 = PageChest.loadPage(location, 3);
        check(sameExact(second, loaded2[4]), "Page 2 did not round-trip exact item data");
        check(sameExact(third, loaded3[8]), "Page 3 did not round-trip exact item data");

        Method strict = PageChest.class.getDeclaredMethod(
                "loadPageStrict", Location.class, int.class);
        strict.setAccessible(true);

        String page2slot4 = "magicexpansion_pagechest_2_4";
        String validPage2 = BlockStorage.getLocationInfo(location, page2slot4);
        BlockStorage.addBlockInfo(location, page2slot4, "definitely-not-valid-base64");
        Object corrupt = strict.invoke(null, location, 2);
        check(corrupt == null, "Unreadable Page Chest data did not fail closed");
        BlockStorage.addBlockInfo(location, page2slot4, validPage2);

        removeNearbyDrops(location);
        PageChest.dropAllPages(location, menu);

        long diamonds = 0L;
        long emeralds = 0L;
        long gold = 0L;
        for (Entity entity : location.getWorld().getNearbyEntities(
                location.clone().add(0.5, 0.5, 0.5), 4, 4, 4)) {
            if (!(entity instanceof Item item)) continue;
            ItemStack stack = item.getItemStack();
            if (stack.getType() == Material.DIAMOND) diamonds += stack.getAmount();
            if (stack.getType() == Material.EMERALD) emeralds += stack.getAmount();
            if (stack.getType() == Material.GOLD_INGOT) gold += stack.getAmount();
        }

        check(diamonds == 7L, "Active Page Chest page dropped " + diamonds + " diamonds instead of 7");
        check(emeralds == 11L, "Saved Page Chest page dropped " + emeralds + " emeralds instead of 11");
        check(gold == 13L, "Saved Page Chest page dropped " + gold + " gold instead of 13");
        removeNearbyDrops(location);
    }

    private void auditOrigin(SingleCubeOrigin origin, Location location) {
        for (int i = 0; i < 512; i++) {
            Material material = origin.getRandomValidBlock();
            check(!origin.isInvalidBlock(material),
                    "Origin One Block generated invalid material " + material);
            location.getBlock().setType(material, false);
            check(StorageCacheUtils.getSlimefunItem(location) == origin,
                    "Origin One Block lost Slimefun identity after material swap to " + material);
        }
    }

    private void auditOre(SingleCubeOre ore, Location location) {
        for (int i = 0; i < 512; i++) {
            Material material = ore.getRandomOreBlock();
            check(material != null && material.isBlock() && material.isItem(),
                    "Ore One Block generated unsafe material " + material);
            location.getBlock().setType(material, false);
            check(StorageCacheUtils.getSlimefunItem(location) == ore,
                    "Ore One Block lost Slimefun identity after material swap to " + material);
        }
    }

    private void auditCrop(MagicCrop crop, Location location) throws Exception {
        World world = location.getWorld();
        check(world != null, "Magic Crop world missing");
        Chunk chunk = world.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        chunk.addPluginChunkTicket(this);
        heldChunks.add(chunk);

        Location soilLoc = location.clone().add(0, -1, 0);
        soilLoc.getBlock().setType(Material.FARMLAND, false);
        Block block = location.getBlock();
        block.setType(Material.WHEAT, false);

        BlockDataController blocks = Slimefun.getDatabaseManager().getBlockDataController();
        blocks.getChunkData(chunk);
        SlimefunBlockData data = blocks.createBlock(location, crop.getId());
        check(data != null && data.isDataLoaded(), "Magic Crop block data did not load");
        check(StorageCacheUtils.getSlimefunItem(location) == crop,
                "Magic Crop identity was not attached to wheat block");

        Method dropsMethod = MagicCrop.class.getDeclaredMethod(
                "handleCropDrops", Block.class, List.class);
        dropsMethod.setAccessible(true);

        try {
            Ageable ageable = (Ageable) block.getBlockData();

            // Immature crops must return exactly their seed and no fruit roll.
            ageable.setAge(0);
            block.setBlockData(ageable, false);
            List<ItemStack> immature = new ArrayList<>();
            dropsMethod.invoke(crop, block, immature);
            check(immature.size() == 1,
                    "Immature Magic Crop returned " + immature.size() + " drops instead of one seed");
            check(SlimefunItem.getByItem(immature.getFirst()) == crop,
                    "Immature Magic Crop did not return its own seed");

            // Mature crops advertise an inclusive 3-9 fruit roll, plus the baseline seed.
            ageable = (Ageable) block.getBlockData();
            ageable.setAge(ageable.getMaximumAge());
            block.setBlockData(ageable, false);

            int observedMin = Integer.MAX_VALUE;
            int observedMax = Integer.MIN_VALUE;
            for (int i = 0; i < 512; i++) {
                List<ItemStack> mature = new ArrayList<>();
                dropsMethod.invoke(crop, block, mature);
                check(!mature.isEmpty(), "Mature Magic Crop returned no seed");
                check(SlimefunItem.getByItem(mature.getFirst()) == crop,
                        "Mature Magic Crop did not keep its baseline seed");
                int fruits = mature.size() - 1;
                check(fruits >= 3 && fruits <= 9,
                        "Magic Crop fruit roll escaped configured 3-9 range: " + fruits);
                observedMin = Math.min(observedMin, fruits);
                observedMax = Math.max(observedMax, fruits);
            }

            check(observedMin <= 4,
                    "Magic Crop sampling never reached the low end of configured range: " + observedMin);
            check(observedMax >= 8,
                    "Magic Crop sampling never reached the high end of configured range: " + observedMax);
        } finally {
            blocks.removeBlockData(location);
            block.setType(Material.AIR, false);
            soilLoc.getBlock().setType(Material.AIR, false);
        }
    }

    private ItemStack tagged(Material material, int amount, String value) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(
                new NamespacedKey(this, "probe"),
                PersistentDataType.STRING,
                value);
        stack.setItemMeta(meta);
        return stack;
    }

    private boolean sameExact(ItemStack expected, ItemStack actual) {
        return expected != null
                && actual != null
                && expected.getAmount() == actual.getAmount()
                && expected.isSimilar(actual);
    }

    private Fixture createFixture(SlimefunItem item, Location location) {
        World world = location.getWorld();
        check(world != null, "Fixture world missing");
        Chunk chunk = world.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        chunk.addPluginChunkTicket(this);
        heldChunks.add(chunk);

        BlockDataController blocks = Slimefun.getDatabaseManager().getBlockDataController();
        blocks.getChunkData(chunk);

        Block block = location.getBlock();
        Material material = item.getItem().getType();
        check(material.isBlock(), item.getId() + " is not placeable");
        block.setType(material, false);

        SlimefunBlockData data = blocks.createBlock(location, item.getId());
        BlockMenu menu = data.getBlockMenu();
        check(data.isDataLoaded(), item.getId() + " block data did not load");
        if (item instanceof PageChest) {
            check(menu != null, "PAGE_CHEST menu did not initialize");
        }
        return new Fixture(block, data, menu);
    }

    private void cleanup(Location location) {
        try {
            Slimefun.getDatabaseManager().getBlockDataController().removeBlockData(location);
            location.getBlock().setType(Material.AIR, false);
            removeNearbyDrops(location);
        } catch (Throwable error) {
            getLogger().log(Level.WARNING, "Probe cleanup failed at " + location, error);
        }
    }

    private void removeNearbyDrops(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        for (Entity entity : world.getNearbyEntities(
                location.clone().add(0.5, 0.5, 0.5), 5, 5, 5)) {
            if (entity instanceof Item) {
                entity.remove();
            }
        }
    }

    private Location location(int x, int y, int z) {
        return new Location(Bukkit.getWorlds().getFirst(), x, y, z);
    }

    private void publish(String content) {
        try {
            Files.writeString(Path.of("novelty-container-result.txt"), content);
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Could not write novelty-container evidence", error);
        }
    }

    private void requestShutdown() {
        // Finish the current callback and queued chunk listeners before stopping.
        // Paper treats shutdown with a heartbeat older than 1 second as a hard timeout.
        Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 2L);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Fixture(Block block, SlimefunBlockData data, BlockMenu menu) {}
}
