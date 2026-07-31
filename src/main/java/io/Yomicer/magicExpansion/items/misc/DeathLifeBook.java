package io.Yomicer.magicExpansion.items.misc;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.FishingGuideMenu;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.Yomicer.magicExpansion.utils.compat.EntityHelper.getDisplayName;

public class DeathLifeBook extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public DeathLifeBook(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);
            Player player = e.getPlayer();
            if (!ItemPermissionUtils.hasPermissionOnlyOnAttackEntity(player)){
                return;
            }
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            openEntityMenu(player, 0);
        };
    }

    private void openEntityMenu(Player player, int page) {
        // 扫描半径20格内的活体生物(排除玩家、盔甲架、掉落物等非生物实体)
        List<LivingEntity> nearbyEntities = player.getLocation().getWorld()
                .getNearbyEntities(player.getLocation(), 20, 20, 20)
                .stream()
                .filter(entity -> {
                    // 只保留活体生物,排除玩家、盔甲架、物品展示框、掉落物等
                    if (!(entity instanceof LivingEntity)) return false;
                    if (entity instanceof Player) return false;
                    if (entity instanceof ArmorStand) return false;
                    if (entity instanceof ItemFrame) return false;
                    if (entity instanceof Item) return false;
                    if (entity instanceof ExperienceOrb) return false;
                    if (entity instanceof Projectile) return false;
                    if (entity instanceof Vehicle) return false;
                    if (entity instanceof Hanging) return false;
                    if (entity instanceof EnderCrystal) return false;
                    if (entity instanceof TNTPrimed) return false;
                    if (entity instanceof FallingBlock) return false;
                    if (entity instanceof AreaEffectCloud) return false;

                    // 确保是有生命的生物
                    LivingEntity living = (LivingEntity) entity;
                    return living.getHealth() > 0;
                })
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toList());

        if (nearbyEntities.isEmpty()) {
            player.sendMessage("§cNo living creatures were found nearby!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }

        // 分页设置
        int entitiesPerPage = 45; // 每页显示45个生物
        int totalPages = (int) Math.ceil((double) nearbyEntities.size() / entitiesPerPage);

        // 确保页码在有效范围内
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        // 计算当前页的生物范围
        int startIndex = page * entitiesPerPage;
        int endIndex = Math.min(startIndex + entitiesPerPage, nearbyEntities.size());

        // 创建菜单 - 保存标题用于后续解析
        final String menuTitle = "§5Book of Life and Death §7- Page " + (page + 1) + "/" + totalPages + "";
        ChestMenu menu = new ChestMenu(menuTitle);

        // 使用final变量来避免lambda问题
        final List<LivingEntity> finalEntities = nearbyEntities;
        final int finalPage = page;

        // 添加当前页的生物到菜单
        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            LivingEntity entity = nearbyEntities.get(i);
            addEntityToMenu(menu, slot, entity, player, menuTitle);
        }

        // 添加上一页按钮(如果有上一页)
        if (page > 0) {
            menu.addItem(45, new CustomItemStack(
                    Material.ARROW,
                    "§6Previous Page",
                    "§7Click to view the previous page.",
                    "§eCurrent: Page " + (page + 1) + "",
                    "§aCreatures Found: " + nearbyEntities.size() + " creatures"
            ), (p, slot, item, action) -> {
                p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
                openEntityMenu(p, finalPage - 1);
                return false;
            });
        }

        // 添加一键清除按钮
        menu.addItem(49, new CustomItemStack(
                Material.TNT,
                "§c§lRemove All Creatures",
                "§7Click to remove every creature within 20 blocks.",
                "§eAmount: §6" + nearbyEntities.size() + " creatures",
                "§4Warning: this action cannot be undone!"
        ), (p, slot, item, action) -> {
            // 添加更强的失明效果
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2)); // 3秒更强的失明
            p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 80, 2)); // 4秒更强的反胃效果

            // 清除所有生物
            int killedCount = 0;
            for (LivingEntity entity : finalEntities) {
                if (!entity.isDead()) {
                    entity.remove();
                    killedCount++;
                }
            }

            p.sendMessage("§aRemoved §e" + killedCount + " §acreatures.");
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            p.closeInventory();
            return false;
        });

        // 添加下一页按钮(如果有下一页)
        if (page < totalPages - 1) {
            menu.addItem(53, new CustomItemStack(
                    Material.ARROW,
                    "§6Next Page",
                    "§7Click to view the next page.",
                    "§eCurrent: Page " + (page + 1) + "",
                    "§aCreatures Found: " + nearbyEntities.size() + " creatures"
            ), (p, slot, item, action) -> {
                p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
                openEntityMenu(p, finalPage + 1);
                return false;
            });
        }

        // 添加页码信息
        menu.addItem(48, new CustomItemStack(
                Material.BOOK,
                "§5Book of Life and Death",
                "§7Current Page: §e" + (page + 1) + "§7/§e" + totalPages,
                "§7Creatures Found: §a" + nearbyEntities.size(),
                "§7Scan Radius: §b20 blocks"
        ), (p, slot, item, action) -> false);

        // 打开菜单
        menu.open(player);
    }

    private void addEntityToMenu(ChestMenu menu, int slot, LivingEntity entity, Player player, String menuTitle) {
        // 获取生物信息
        String entityName = getDisplayName(entity);
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();
        Location loc = entity.getLocation();
        String worldName = loc.getWorld().getName();
        double distance = player.getLocation().distance(loc);

        // 根据生命值设置颜色
        String healthColor = "§a"; // 绿色 - 健康
        if (health < maxHealth * 0.3) {
            healthColor = "§c"; // 红色 - 濒死
        } else if (health < maxHealth * 0.7) {
            healthColor = "§6"; // 金色 - 受伤
        }

        // 创建显示物品
        Material icon = getEntityIcon(entity);
        List<String> lore = Arrays.asList(
                "§7Health: " + healthColor + String.format("%.1f", health) + "§7/§a" + String.format("%.1f", maxHealth),
                "§7Coordinates: §eX: " + (int)loc.getX() + " Y: " + (int)loc.getY() + " Z: " + (int)loc.getZ(),
                "§7World: §b" + worldName,
                "§7Distance: §6" + String.format("%.1f", distance) + " blocks",
                "",
                "§aLeft-click §8-> §eTeleport to creature",
                "§cRight-click §8-> §4Remove creature"
        );

        CustomItemStack menuItem = new CustomItemStack(icon, "§6" + entityName, lore.toArray(new String[0]));

        // 使用final变量避免lambda问题
        final LivingEntity finalEntity = entity;
        final String finalEntityName = entityName;

        // 添加点击事件
        menu.addItem(slot, menuItem, (p, menuSlot, itemStack, clickAction) -> {
            if (!clickAction.isRightClicked()) {
                // 左键传送
                if (!finalEntity.isDead()) {
                    // 添加短暂失明效果
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1)); // 2秒失明 (40 ticks)
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 1)); // 3秒反胃效果,增强传送感

                    p.teleport(finalEntity.getLocation().add(0, 1, 0));
                    p.sendMessage("§aTeleported to §e" + finalEntityName + "§a!");
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                } else {
                    p.sendMessage("§cThat creature no longer exists!");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
                p.closeInventory();
            } else if (clickAction.isRightClicked()) {
                // 右键杀死
                if (!finalEntity.isDead()) {
                    // 添加短暂失明效果
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1)); // 1秒失明 (20 ticks)

                    // 播放死亡音效
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_DEATH, 0.8f, 1.0f);

                    // 延迟一 tick 执行,让音效先播放
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            finalEntity.remove();
                            p.sendMessage("§aRemoved from the Book of Life and Death: §e" + finalEntityName + "§a!");

                            // 刷新当前页菜单
                            int currentPage = getCurrentPageFromTitle(menuTitle);
                            openEntityMenu(p, currentPage);
                        }
                    }.runTaskLater(MagicExpansion.getInstance(), 1L);
                } else {
                    p.sendMessage("§cThat creature no longer exists!");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    p.closeInventory();
                }
            }
            return false;
        });
    }

    private Material getEntityIcon(LivingEntity entity) {
        // 根据生物类型名称获取对应的生物蛋
        String entityTypeName = entity.getType().name();

        try {
            // 尝试获取对应生物蛋
            Material spawnEgg = Material.valueOf(entityTypeName + "_SPAWN_EGG");
            return spawnEgg;
        } catch (IllegalArgumentException e) {
            // 如果找不到对应的生物蛋,返回NAME_TAG
            return Material.NAME_TAG;
        }
    }

    // 从菜单标题中提取当前页码
    private int getCurrentPageFromTitle(String title) {
        try {
            // 标题格式: "§5Book of Life and Death §7- Page X/Y"
            String[] parts = title.split(" ");
            for (String part : parts) {
                if (part.contains("/")) {
                    String pageInfo = part;
                    String currentPage = pageInfo.split("/")[0];
                    return Integer.parseInt(currentPage) - 1; // 返回0-based页码
                }
            }
            return 0; // 如果解析失败,返回第一页
        } catch (Exception e) {
            return 0; // 如果解析失败,返回第一页
        }
    }
}
