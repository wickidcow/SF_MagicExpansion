package io.Yomicer.magicExpansion.items.misc.weapon;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.utils.log.Debug;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StarShardsSword extends SimpleSlimefunItem<ItemUseHandler> implements RecipeDisplayItem, Listener {


    //新增自定义倍率
//    public static final double DAMAGE_MULTIPLIER = 61.8;
    // A5: 技能冷却表改为 static + ConcurrentHashMap，保证多线程安全且可被静态 cleanup 清理
    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    // A5: 冷却提示限频时间戳改为 static + ConcurrentHashMap
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    // A3: 星界护盾无敌期表——记录玩家 UUID → 无敌到期时间戳，由 onEntityDamage 的事件取消逻辑实现无敌
    private static final Map<UUID, Long> invulnerableUntil = new ConcurrentHashMap<>();
    // A6: 删除了未使用的流血任务死字段 bleedingTasks（流血任务实际存放在目标实体的 metadata 中）

    Config cfg = new Config(MagicExpansion.getInstance());
    Double StarShards_Atk_Mix = cfg.getDouble("StarShardsSword.StarShards_Atk_Mix");
    Double StarShards_Atk_Add = cfg.getDouble("StarShardsSword.StarShards_Atk_Add");
    Double StarShards_Atk_Mult = cfg.getDouble("StarShardsSword.StarShards_Atk_Mult");
    Double StarShards_Atk_Speed = cfg.getDouble("StarShardsSword.StarShards_Atk_Speed");
    Double StarShards_Atk_ExtraPercent = cfg.getDouble("StarShardsSword.StarShards_Atk_ExtraPercent");
    Double StarShards_Atk_Blood = cfg.getDouble("StarShardsSword.StarShards_Atk_Blood");
    Double StarShards_Health_Add = cfg.getDouble("StarShardsSword.StarShards_Health_Add");
    Double StarShards_Health_Mult = cfg.getDouble("StarShardsSword.StarShards_Health_Mult");
    Double StarShards_MoveSpeed = cfg.getDouble("StarShardsSword.StarShards_MoveSpeed");
    Double StarShards_Armor = cfg.getDouble("StarShardsSword.StarShards_Armor");
    Double StarShards_Toughness = cfg.getDouble("StarShardsSword.StarShards_Toughness");
    Double StarShards_FlySpeed = cfg.getDouble("StarShardsSword.StarShards_FlySpeed");
    Long StarShards_BlazingSlash_CD = cfg.getLong("StarShardsSword.StarShards_BlazingSlash_CD");
    Long StarShards_ArcaneBlast_CD = cfg.getLong("StarShardsSword.StarShards_ArcaneBlast_CD");
    Long StarShards_AstralShield_CD = cfg.getLong("StarShardsSword.StarShards_AstralShield_CD");
    Long StarShards_AstralShield_During = cfg.getLong("StarShardsSword.StarShards_AstralShield_During");
    Long StarShards_InstantBlink_CD = cfg.getLong("StarShardsSword.StarShards_InstantBlink_CD");



    public StarShardsSword(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        ItemMeta meta = getItem().getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);

            String namespace = "star_shards_sword";

            // 💥 攻击力 +1314（固定值）
            UUID atk1Id = UUID.nameUUIDFromBytes((namespace + "_atk_add").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(atk1Id, "StarShards_Atk_Add", StarShards_Atk_Add, AttributeModifier.Operation.ADD_NUMBER)
            );

            // 💥 攻击力 +618%（乘法）
            UUID atk2Id = UUID.nameUUIDFromBytes((namespace + "_atk_mult").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(atk2Id, "StarShards_Atk_Mult", StarShards_Atk_Mult, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            // ⚡ 攻击速度 +2000% → 最终速度 = 原速 × (1 + 20.0) = 21倍！
            UUID atkSpeedId = UUID.nameUUIDFromBytes((namespace + "_atk_speed").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(atkSpeedId, "StarShards_AtkSpeed", StarShards_Atk_Speed, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            // ❤️ 生命值 +1314（固定值，单位是“半心”，所以 +1314 = +657 颗心！）
            UUID health1Id = UUID.nameUUIDFromBytes((namespace + "_health_add").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_MAX_HEALTH,
                    new AttributeModifier(health1Id, "StarShards_Health_Add", StarShards_Health_Add, AttributeModifier.Operation.ADD_NUMBER)
            );

            // ❤️ 生命值 +618%（乘法）
            UUID health2Id = UUID.nameUUIDFromBytes((namespace + "_health_mult").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_MAX_HEALTH,
                    new AttributeModifier(health2Id, "StarShards_Health_Mult", StarShards_Health_Mult, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            // 🏃 移动速度 +1314% → 最终速度 = 原速 × (1 + 13.14) = 14.14倍！
            UUID moveSpeedId = UUID.nameUUIDFromBytes((namespace + "_move_speed").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_MOVEMENT_SPEED,
                    new AttributeModifier(moveSpeedId, "StarShards_MoveSpeed", StarShards_MoveSpeed, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            // 🛡️ 护甲值 +200（固定值）
            UUID armorId = UUID.nameUUIDFromBytes((namespace + "_armor").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_ARMOR,
                    new AttributeModifier(armorId, "StarShards_Armor", StarShards_Armor, AttributeModifier.Operation.ADD_NUMBER)
            );

            // 🧱 护甲韧性 +200（固定值）
            UUID toughnessId = UUID.nameUUIDFromBytes((namespace + "_toughness").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_ARMOR_TOUGHNESS,
                    new AttributeModifier(toughnessId, "StarShards_Toughness", StarShards_Toughness, AttributeModifier.Operation.ADD_NUMBER)
            );

            // ✈️ 飞行速度 +1314%
            UUID flySpeedId = UUID.nameUUIDFromBytes((namespace + "_fly_speed").getBytes());
            meta.addAttributeModifier(
                    Attribute.GENERIC_FLYING_SPEED,
                    new AttributeModifier(flySpeedId, "StarShards_FlySpeed", StarShards_FlySpeed, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );


            getItem().setItemMeta(meta);
        }
        Bukkit.getPluginManager().registerEvents(this, MagicExpansion.getInstance());
    }

    @Override
    public @NotNull ItemUseHandler getItemHandler() {
        return e -> {
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);

            if (e.getHand() != EquipmentSlot.HAND) return;

            Player player = e.getPlayer();
            boolean isSneaking = player.isSneaking();
            Action action = e.getInteractEvent().getAction();

            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                if (isSneaking) {
                    useInstantBlink(player);
                } else {
                    useAstralShield(player);
                }
            }
        };
    }

    private static final Set<UUID> holyProtectedPlayers = ConcurrentHashMap.newKeySet();
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();
        // A3: 星界护盾无敌期判定（替代原 player.setInvulnerable 方案）
        Long until = invulnerableUntil.get(p.getUniqueId());
        boolean shieldActive = until != null && System.currentTimeMillis() < until;
        if (shieldActive || holyProtectedPlayers.contains(p.getUniqueId())) {
            event.setCancelled(true);
            if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
                p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
            }
        }
        // A3: 无敌期已过期的条目顺手移除，防止 Map 残留
        if (until != null && !shieldActive) {
            invulnerableUntil.remove(p.getUniqueId());
        }
    }


    // ✅ 攻击事件监听（SF9 唯一方式）
    // A1: 加 ignoreCancelled = true，被领地保护等插件取消的攻击事件不再重复结算伤害
    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // 判断玩家主手是否持有本 Slimefun 物品
        ItemStack hand = player.getInventory().getItemInMainHand();
        SlimefunItem handSfItem = getByItem(hand);
        if (!(handSfItem instanceof StarShardsSword)) return;
        // 应用伤害倍率
        // *新增固定百分比伤害
        // 1. 计算本次要附加的真实伤害值（保留原有公式）
        double damageToDeal = event.getDamage() * StarShards_Atk_Mix
                + target.getMaxHealth() * (StarShards_Atk_ExtraPercent);

        // A2: 原先 target.setHealth(...) 直接扣血会绕过事件系统（无视领地保护/Boss伤害上限），
        // 改为 target.damage(damage, player) 走标准伤害事件，总伤害效果保持接近
        target.damage(damageToDeal, player);


//        double damage =  event.getDamage();
//        String formatted = String.format("%.2f", damage);
//        Bukkit.broadcastMessage(ChatColor.GOLD + "⚔ " + ChatColor.YELLOW + player.getName()
//                + ChatColor.GOLD + " 使用 " + ChatColor.AQUA + handSfItem.getItemName()
//                + ChatColor.GOLD + " 对 " + ChatColor.RED + target.getName()
//                + ChatColor.GOLD + " 造成了 " + ChatColor.WHITE + formatted
//                + ChatColor.GOLD + " 点真实伤害！");

        if (target.isDead()) return;

        // --- *新增 触发流血效果 (Bleed Effect) ---
        //流血简化
        applyBleedEffect(player, target);

        // 触发技能
        if (player.isSneaking()) {
            castArcaneBlast(player, event.getEntity().getLocation());
        } else {
            castBlazingSlash(player, event.getEntity().getLocation());
        }
    }

    // 修改后：支持多层叠加、直接扣血、保底0.1血量的流血效果
    private void applyBleedEffect(Player damager, LivingEntity target) {
        // 1. 计算每秒造成的伤害 (目标最大生命值的 StarShards_Atk_Blood 百分比)
        double damagePerSecond = target.getMaxHealth() * StarShards_Atk_Blood;

        // 2. 为流血效果创建一个唯一的标识符，用于在目标身上打标签
        // 格式为 "MagicExpansion_BLEED_<攻击者UUID>"，确保来自不同玩家的流血效果可以叠加
        String bleedTagKey = "MagicExpansion_BLEED_" + damager.getUniqueId();

        // 3. 启动一个持续8秒的异步任务
        BukkitTask bleedTask = new BukkitRunnable() {
            int ticksPassed = 0; // 记录已过去的游戏刻

            // A6: 任务结束时从目标 metadata 的任务列表中移除自身；列表为空则移除整个 metadata 标签，防止残留
            private void finishAndCleanup() {
                this.cancel();
                if (target.hasMetadata(bleedTagKey)) {
                    List<BukkitTask> tasks = (List<BukkitTask>) target.getMetadata(bleedTagKey).get(0).value();
                    if (tasks != null) {
                        tasks.remove(this);
                        if (tasks.isEmpty()) {
                            target.removeMetadata(bleedTagKey, MagicExpansion.getInstance());
                        }
                    }
                }
            }

            @Override
            public void run() {
                // 如果目标已死亡或无效，则清理 metadata 并取消任务
                if (!target.isValid() || target.isDead()) {
                    finishAndCleanup();
                    return;
                }

                // 每秒执行一次 (20游戏刻)
                if (ticksPassed % 20 == 0) {
                    // --- 核心修改：使用 setHealth 直接扣血 ---
                    double newHealth = target.getHealth() - damagePerSecond;

                    // --- 核心修改：如果血量小于等于0，则强制设为0.1 ---
                    if (newHealth <= 0.0) {
                        newHealth = 0.1;
                    }

                    target.setHealth(newHealth);

                    // 在目标位置生成血粒子效果
                    target.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            target.getLocation().add(0, 1, 0),
                            5,
                            0.3, 0.3, 0.3,
                            0,
                            new Particle.DustOptions(Color.RED, 1.5F) // 新增的颜色与大小参数
                    );
                }

                ticksPassed++;

                // 8秒后 (160游戏刻) 结束任务并清理 metadata
                if (ticksPassed >= 160) {
                    finishAndCleanup();
                }
            }
        }.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L); // 立即开始，每1游戏刻检查一次

        // 4. 将此任务存储在目标的元数据中，以便实现效果叠加
        List<BukkitTask> targetBleedTasks;

        // 检查目标身上是否已经存在该流血标签
        if (target.hasMetadata(bleedTagKey)) {
            // 如果存在，安全地提取出原来的任务列表
            targetBleedTasks = (List<BukkitTask>) target.getMetadata(bleedTagKey).get(0).value();
        } else {
            // 如果不存在，创建一个全新的列表
            targetBleedTasks = new ArrayList<>();
        }

        // 将新启动的流血任务添加到列表中
        targetBleedTasks.add(bleedTask);

        // 更新元数据（覆盖旧数据）
        target.setMetadata(bleedTagKey, new FixedMetadataValue(MagicExpansion.getInstance(), targetBleedTasks));
    }


    // ========== 冷却与技能方法（保持不变）==========
    private boolean checkCooldown(Player player, String skill, long seconds) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        cooldowns.putIfAbsent(id, new ConcurrentHashMap<>()); // A5: 内层 Map 同步改为 ConcurrentHashMap
        Map<String, Long> map = cooldowns.get(id);

        if (map.containsKey(skill)) {
            long last = map.get(skill);
            if (now < last + seconds * 1000L) {
                // 防止刷屏：500ms 内不再提示
                Long lastMsg = lastMessageTime.getOrDefault(id, 0L);
                if (now - lastMsg > 500) {
                    long remain = ((last + seconds * 1000L - now) + 999) / 1000;
                    player.sendMessage("§c技能冷卻中，還需 " + remain + " 秒");
                    lastMessageTime.put(id, now);
                }
                return false;
            }
        }
        map.put(skill, now);
        return true;
    }

    private void castBlazingSlash(Player player, Location hitLoc) {
        if (!checkCooldown(player, "blazing_slash", StarShards_BlazingSlash_CD)) return;

        player.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.3f);

        player.getWorld().spawnParticle(Particle.FLAME, hitLoc, 30, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, hitLoc, 8, 0.1, 0.1, 0.1, 0);

//        hitLoc.getWorld().createExplosion(hitLoc, 0.3f, false, false);

        for (Entity e : hitLoc.getWorld().getNearbyEntities(hitLoc, 2.8, 2.8, 2.8)) {
            if (e instanceof LivingEntity le && e != player && e.isValid()) {
                // 🔥 点燃
                le.setFireTicks(80);

                // 🧨 安全计算击退方向
                Location entityLoc = e.getLocation();
                Vector toEntity = entityLoc.toVector().subtract(hitLoc.toVector());
                double distance = toEntity.length();

                // 如果距离太近（< 0.1），就用一个随机水平方向代替，避免 NaN
                if (distance < 0.1) {
                    // 随机水平方向（XZ 平面）
                    double angle = Math.random() * 2 * Math.PI;
                    toEntity = new Vector(Math.cos(angle), 0, Math.sin(angle));
                } else {
                    toEntity.normalize();
                }

                // 应用击退：水平方向 + 固定向上
                toEntity.multiply(0.9).setY(0.5);
                le.setVelocity(toEntity); // ✅ 现在安全了！
            }
        }
    }

    private void castArcaneBlast(Player player, Location origin) {
        if (!checkCooldown(player, "arcane_blast", StarShards_ArcaneBlast_CD)) return;

        Vector playerForward = player.getEyeLocation().getDirection().normalize();
        Location playerOrigin = player.getEyeLocation();

        double coneAngleCos = Math.cos(Math.toRadians(25)); // ±25度锥形
        List<LivingEntity> targets = new ArrayList<>();

        for (LivingEntity entity : player.getWorld().getNearbyLivingEntities(playerOrigin, 8.0)) {
            if (entity == player || !entity.isValid()) continue;

            Vector toEntity = entity.getLocation().toVector().subtract(playerOrigin.toVector());
            double distance = toEntity.length();

            if (distance == 0) continue;

            toEntity.normalize();
            double dot = playerForward.dot(toEntity); // 夹角余弦值

            // 如果在锥形内（角度 ≤ 25°）
            if (dot >= coneAngleCos) {
                targets.add(entity);
            }
        }

        // 🎵 音效：魔法释放 + 冲击波
        player.getWorld().playSound(origin, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.7f);
        Bukkit.getScheduler().runTaskLater(getAddon().getJavaPlugin(), () -> {
            player.getWorld().playSound(origin, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.8f);
        }, 2L);

        // ✨ 粒子：沿方向发射光束 + 命中闪光
        for (int i = 1; i <= 20; i++) {
            Location p = origin.clone().add(playerForward.clone().multiply(i * 0.4));
            player.getWorld().spawnParticle(Particle.END_ROD, p, 1, 0.05, 0.05, 0.05, 0);
            player.getWorld().spawnParticle(Particle.SPELL_WITCH, p, 1, 0.05, 0.05, 0.05, 0);
        }

        // 💥 对每个目标：伤害 + 击退 + 弱化 + 缓慢
        for (LivingEntity target : targets) {
            // 造成魔法伤害（可调整）
            target.damage(10.0, player);

            // 击退（沿光束方向）
            Vector knockback = playerForward.clone().multiply(1.1).setY(0.3);
            target.setVelocity(knockback);

            // 状态效果
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0)); // 4秒
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1));     // 4秒

            // 命中闪光
            target.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, target.getLocation(), 5, 0.1, 0.1, 0.1, 0);
            target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, target.getLocation(), 10, 0.2, 0.2, 0.2, 0.05);
        }

        if (targets.isEmpty()) {
            // 即使没打中也播放尾音
            player.sendMessage("§7奧爆衝擊釋放，但未命中目標。");
        }
    }

    private void useAstralShield(Player player) {
        if (!checkCooldown(player, "astral_shield", StarShards_AstralShield_CD)) return;
        player.sendMessage("§b✨ 星界護盾已激活！");
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        // A3: 删除原 player.setInvulnerable(true/false) 方案，改为记录无敌到期时间戳，
        // 由 onEntityDamage 中的事件取消逻辑实现无敌期（避免长期占用原版 invulnerable 标记）
        invulnerableUntil.put(player.getUniqueId(),
                System.currentTimeMillis() + StarShards_AstralShield_During * 1000L);

        holyProtectedPlayers.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                holyProtectedPlayers.remove(player.getUniqueId());
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.GRAY + "§7星界護盾已消散...");
                }
            }
        }.runTaskLater(MagicExpansion.getInstance(), StarShards_AstralShield_During*20L);

    }

    private void useInstantBlink(Player player) {
        if (!checkCooldown(player, "instant_blink", StarShards_InstantBlink_CD)) return;
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();
        Location target = null;
        for (double d = 1.0; d <= 15; d += 0.5) {
            Location point = eye.clone().add(dir.clone().multiply(d));
            if (point.getBlock().getType().isSolid()) {
                target = point.add(0, 1, 0);
                break;
            }
        }
        if (target == null) {
            player.sendMessage("§c前方無障礙物，無法傳送！");
            return;
        }
        player.teleport(target);
        player.getWorld().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.PORTAL, target, 50, 0.5, 0.5, 0.5, 0.1);
        for (Entity e : target.getWorld().getNearbyEntities(target, 1.5, 1.5, 1.5)) {
            if (e instanceof LivingEntity le && e != player) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20, 0));
            }
        }
    }

    /**
     * A4: 玩家退出时的会话数据清理（由 PlayerCleanupListener 统一调用）：
     * 清除技能冷却、冷却提示限频时间戳、星界护盾无敌期、圣盾名单等 per-player 状态，
     * 防止 Map 残留离线玩家数据造成内存泄漏。
     */
    public static void cleanup(UUID uuid) {
        cooldowns.remove(uuid);           // 清理技能冷却数据
        lastMessageTime.remove(uuid);     // 清理冷却提示限频时间戳
        invulnerableUntil.remove(uuid);   // 清理星界护盾无敌期时间戳
        holyProtectedPlayers.remove(uuid);// 清理圣盾玩家名单
    }

    @Override
    public @NotNull List<ItemStack> getDisplayRecipes() {
        return List.of();
    }
}