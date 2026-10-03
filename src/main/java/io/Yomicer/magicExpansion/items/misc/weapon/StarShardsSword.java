package io.Yomicer.magicExpansion.items.misc.weapon;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerQuitEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.Yomicer.magicExpansion.utils.SwordAttackGuard;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StarShardsSword extends SimpleSlimefunItem<ItemUseHandler> implements RecipeDisplayItem, Listener {

    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    private static final Map<UUID, Long> invulnerableUntil = new ConcurrentHashMap<>();

    private static final Set<Runnable> bleedCleanup = new HashSet<>();

    Config cfg = new Config(MagicExpansion.getInstance());
    Double StarShards_Atk_Mix = cfg.getDouble("StarShardsSword.StarShards_Atk_Mix");
    Double StarShards_Atk_Add = cfg.getDouble("StarShardsSword.StarShards_Atk_Add");
    Double StarShards_Atk_Mult = cfg.getDouble("StarShardsSword.StarShards_Atk_Mult");
    Double StarShards_Atk_Speed = cfg.getDouble("StarShardsSword.StarShards_Atk_Speed");
    Double StarShards_Atk_ExtraPercent = cfg.getDouble("StarShardsSword.StarShards_Atk_ExtraPercent");
    Double StarShards_Atk_Blood = cfg.getDouble("StarShardsSword.StarShards_Atk_Blood");
    Double StarShards_Atk_Fire = damageMultiplier("StarShardsSword.StarShards_Atk_Fire", 0.8);
    Double StarShards_ArcaneBlast_Mult = damageMultiplier("StarShardsSword.StarShards_ArcaneBlast_Mult", 0.6);

    private double damageMultiplier(String key, double fallback) {
        double value = cfg.contains(key) ? cfg.getDouble(key) : fallback;
        return Double.isFinite(value) && value >= 0 ? value : fallback;
    }
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

            NamespacedKey atk1Id = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_atk_add");
            meta.addAttributeModifier(
                    Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(atk1Id, StarShards_Atk_Add, AttributeModifier.Operation.ADD_NUMBER)
            );

            NamespacedKey atk2Id = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_atk_mult");
            meta.addAttributeModifier(
                    Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(atk2Id, StarShards_Atk_Mult, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            NamespacedKey atkSpeedId = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_atk_speed");
            meta.addAttributeModifier(
                    Attribute.ATTACK_SPEED,
                    new AttributeModifier(atkSpeedId, StarShards_Atk_Speed, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            NamespacedKey health1Id = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_health_add");
            meta.addAttributeModifier(
                    Attribute.MAX_HEALTH,
                    new AttributeModifier(health1Id, StarShards_Health_Add, AttributeModifier.Operation.ADD_NUMBER)
            );

            NamespacedKey health2Id = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_health_mult");
            meta.addAttributeModifier(
                    Attribute.MAX_HEALTH,
                    new AttributeModifier(health2Id, StarShards_Health_Mult, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            NamespacedKey moveSpeedId = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_move_speed");
            meta.addAttributeModifier(
                    Attribute.MOVEMENT_SPEED,
                    new AttributeModifier(moveSpeedId, StarShards_MoveSpeed, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );

            NamespacedKey armorId = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_armor");
            meta.addAttributeModifier(
                    Attribute.ARMOR,
                    new AttributeModifier(armorId, StarShards_Armor, AttributeModifier.Operation.ADD_NUMBER)
            );

            NamespacedKey toughnessId = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_toughness");
            meta.addAttributeModifier(
                    Attribute.ARMOR_TOUGHNESS,
                    new AttributeModifier(toughnessId, StarShards_Toughness, AttributeModifier.Operation.ADD_NUMBER)
            );

            NamespacedKey flySpeedId = new NamespacedKey(MagicExpansion.getInstance(), namespace + "_fly_speed");
            meta.addAttributeModifier(
                    Attribute.FLYING_SPEED,
                    new AttributeModifier(flySpeedId, StarShards_FlySpeed, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();

        Long until = invulnerableUntil.get(p.getUniqueId());
        boolean shieldActive = until != null && System.currentTimeMillis() < until;
        if (shieldActive) {
            event.setCancelled(true);
            if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {

                if (event instanceof EntityDamageByEntityEvent byEntity
                        && byEntity.getDamager() instanceof Projectile projectile) {
                    Vector vel = projectile.getVelocity();
                    if (vel.lengthSquared() > 0.001) {
                        projectile.setVelocity(vel.normalize().multiply(2.2).multiply(-1));
                        p.getWorld().spawnParticle(Particle.ENCHANTED_HIT, projectile.getLocation(), 12, 0.2, 0.2, 0.2, 0.1);
                        p.getWorld().playSound(projectile.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.8f);
                    }
                } else {
                    p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }

        if (until != null && !shieldActive) {
            invulnerableUntil.remove(p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {

        if (SwordAttackGuard.isActive()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        SlimefunItem handSfItem = getByItem(hand);
        if (!(handSfItem instanceof StarShardsSword) || !canAttack(player, target)) return;

        SwordAttackGuard.run(() -> {

            double damageToDeal = event.getDamage() * StarShards_Atk_Mix
                    + Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue() * (StarShards_Atk_ExtraPercent);

            target.damage(damageToDeal, player);

            if (target.isDead()) return;

            applyBleedEffect(player, target);

            double baseDamage = event.getDamage();
            if (player.isSneaking()) {
                castArcaneBlast(player, event.getEntity().getLocation(), baseDamage);
            } else {
                castBlazingSlash(player, event.getEntity().getLocation(), baseDamage);
            }
        });
    }

    private void applyBleedEffect(Player damager, LivingEntity target) {

        double damagePerSecond = Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue() * StarShards_Atk_Blood;

        var effect = new BukkitRunnable() {
            int ticksPassed = 0;

            private final Runnable cleanup = this::finishAndCleanup;

            private void finishAndCleanup() {
                this.cancel();
                bleedCleanup.remove(cleanup);

            }

            @Override
            public void run() {

                if (!damager.isOnline() || !target.isValid() || target.isDead()
                        || !damager.getWorld().equals(target.getWorld()) || !canAttack(damager, target)) {
                    finishAndCleanup();
                    return;
                }

                if (ticksPassed % 20 == 0) {

                    double newHealth = target.getHealth() - damagePerSecond;

                    if (newHealth <= 0.0) {
                        newHealth = 0.1;
                    }

                    target.setHealth(newHealth);

                    target.getWorld().spawnParticle(
                            Particle.DUST,
                            target.getLocation().add(0, 1, 0),
                            5,
                            0.3, 0.3, 0.3,
                            0,
                            new Particle.DustOptions(Color.RED, 1.5F)
                    );
                }

                ticksPassed++;

                if (ticksPassed >= 160) {
                    finishAndCleanup();
                }
            }
        };
        effect.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);
        bleedCleanup.add(effect.cleanup);
    }

    private boolean checkCooldown(Player player, String skill, long seconds) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        cooldowns.putIfAbsent(id, new ConcurrentHashMap<>());
        Map<String, Long> map = cooldowns.get(id);

        if (map.containsKey(skill)) {
            long last = map.get(skill);
            if (now < last + seconds * 1000L) {

                Long lastMsg = lastMessageTime.getOrDefault(id, 0L);
                if (now - lastMsg > 500) {
                    long remain = ((last + seconds * 1000L - now) + 999) / 1000;
                    player.sendMessage("§cSkill on cooldown. Remaining: " + remain + " seconds");
                    lastMessageTime.put(id, now);
                }
                return false;
            }
        }
        map.put(skill, now);
        return true;
    }

    private void castBlazingSlash(Player player, Location hitLoc, double baseDamage) {
        if (!checkCooldown(player, "blazing_slash", StarShards_BlazingSlash_CD)) return;

        player.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.3f);

        Vector slashDir = hitLoc.toVector().subtract(player.getLocation().toVector());
        double slashDist = slashDir.length();
        if (slashDist > 0.01) slashDir.normalize();
        for (double d = 0; d <= slashDist; d += 0.35) {
            Location p = player.getLocation().clone().add(slashDir.clone().multiply(d)).add(0, 1, 0);
            player.getWorld().spawnParticle(Particle.FLAME, p, 3, 0.06, 0.06, 0.06, 0.02);
        }

        player.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 10, 0.2, 0.2, 0.2, 0);
        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, hitLoc, 3, 0.1, 0.1, 0.1, 0);
        player.getWorld().spawnParticle(Particle.FLAME, hitLoc, 30, 0.5, 0.5, 0.5, 0.1);

        double fireDamage = baseDamage * StarShards_Atk_Fire;

        for (Entity e : hitLoc.getWorld().getNearbyEntities(hitLoc, 2.8, 2.8, 2.8)) {
            if (e instanceof LivingEntity le && e != player && le.isValid() && canAttack(player, le)) {

                if (fireDamage > 0 && !le.isDead()) {
                    le.damage(fireDamage, player);
                }

                le.setFireTicks(80);

                Location entityLoc = e.getLocation();
                Vector toEntity = entityLoc.toVector().subtract(hitLoc.toVector());
                double distance = toEntity.length();

                if (distance < 0.1) {

                    double angle = Math.random() * 2 * Math.PI;
                    toEntity = new Vector(Math.cos(angle), 0, Math.sin(angle));
                } else {
                    toEntity.normalize();
                }

                toEntity.multiply(0.9).setY(0.5);
                le.setVelocity(toEntity);
            }
        }
    }

    private void castArcaneBlast(Player player, Location origin, double baseDamage) {
        if (!checkCooldown(player, "arcane_blast", StarShards_ArcaneBlast_CD)) return;

        Vector playerForward = player.getEyeLocation().getDirection().normalize();
        Location playerOrigin = player.getEyeLocation();

        double coneAngleCos = Math.cos(Math.toRadians(25));
        List<LivingEntity> targets = new ArrayList<>();

        for (LivingEntity entity : player.getWorld().getNearbyLivingEntities(playerOrigin, 8.0)) {
            if (entity == player || !entity.isValid() || !canAttack(player, entity)) continue;

            Vector toEntity = entity.getLocation().toVector().subtract(playerOrigin.toVector());
            double distance = toEntity.length();

            if (distance == 0) continue;

            toEntity.normalize();
            double dot = playerForward.dot(toEntity);

            if (dot >= coneAngleCos) {
                targets.add(entity);
            }
        }

        player.getWorld().playSound(origin, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.7f);

        BukkitRunnable beam = new BukkitRunnable() {
            int segment = 0;
            final int maxSegment = 20;

            @Override
            public void run() {
                segment++;
                if (segment > maxSegment || !player.isOnline() || !player.getWorld().equals(origin.getWorld())) {
                    this.cancel();
                    return;
                }
                Location p = origin.clone().add(playerForward.clone().multiply(segment * 0.4));
                origin.getWorld().spawnParticle(Particle.END_ROD, p, 2, 0.05, 0.05, 0.05, 0);
                origin.getWorld().spawnParticle(Particle.WITCH, p, 2, 0.05, 0.05, 0.05, 0);
                if (segment % 4 == 0) {
                    origin.getWorld().playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 0.4f, 1.6f);
                }
            }
        };
        beam.runTaskTimer(MagicExpansion.getInstance(), 0L, 1L);

        double arcaneDamage = baseDamage * StarShards_ArcaneBlast_Mult;
        for (LivingEntity target : targets) {
            target.damage(arcaneDamage, player);

            Vector knockback = playerForward.clone().multiply(1.1).setY(0.3);
            target.setVelocity(knockback);

            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));

            target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, target.getLocation(), 5, 0.1, 0.1, 0.1, 0);
            target.getWorld().spawnParticle(Particle.FIREWORK, target.getLocation(), 10, 0.2, 0.2, 0.2, 0.05);
        }

        if (targets.isEmpty()) {

            player.sendMessage("§7Arcane Blast was released but hit no target.");
        }
    }

    private void useAstralShield(Player player) {
        if (!checkCooldown(player, "astral_shield", StarShards_AstralShield_CD)) return;
        player.sendMessage("§b✨ Astral Shield activated!");
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);

        invulnerableUntil.put(player.getUniqueId(),
                System.currentTimeMillis() + StarShards_AstralShield_During * 1000L);

        new BukkitRunnable() {
            @Override
            public void run() {
                Long expires = invulnerableUntil.get(player.getUniqueId());
                if (expires == null || expires > System.currentTimeMillis()) return;
                invulnerableUntil.remove(player.getUniqueId(), expires);
                if (player.isOnline()) {
                    player.sendMessage("§7Astral Shield faded...");
                }
            }
        }.runTaskLater(MagicExpansion.getInstance(), StarShards_AstralShield_During*20L);

    }

    private void useInstantBlink(Player player) {
        if (!checkCooldown(player, "instant_blink", StarShards_InstantBlink_CD)) return;
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();
        Location dest = null;
        for (double d = 1.0; d <= 15; d += 0.5) {
            Location point = eye.clone().add(dir.clone().multiply(d));
            if (point.getBlock().getType().isSolid()) {
                dest = point.add(0, 1, 0);
                break;
            }
        }
        if (dest == null) {
            player.sendMessage("§cThere is no obstacle ahead to teleport through!");
            return;
        }
        Location departure = player.getLocation();
        if (!player.teleport(dest)) return;

        departure.getWorld().spawnParticle(Particle.PORTAL, departure, 40, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(dest, Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 1.2f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, dest, 12, 0.3, 0.3, 0.3, 0);
        player.getWorld().spawnParticle(Particle.PORTAL, dest, 40, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.CLOUD, dest, 15, 0.2, 0.2, 0.2, 0.01);
        for (Entity e : dest.getWorld().getNearbyEntities(dest, 2.0, 2.0, 2.0)) {
            if (e instanceof LivingEntity le && e != player && canAttack(player, le)) {

                le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));

                Vector k = e.getLocation().toVector().subtract(dest.toVector());
                if (k.lengthSquared() < 0.01) {
                    k = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
                }
                k.normalize().multiply(0.8).setY(0.4);
                le.setVelocity(k);
            }
        }
    }

    public static void cleanup(UUID uuid) {
        cooldowns.remove(uuid);
        lastMessageTime.remove(uuid);
        invulnerableUntil.remove(uuid);

    }

    private static boolean canAttack(Player player, LivingEntity target) {
        if (player == target || !target.isValid() || target.isDead() || target.isInvulnerable()) return false;
        Long shieldExpiry = invulnerableUntil.get(target.getUniqueId());
        if (shieldExpiry != null && System.currentTimeMillis() < shieldExpiry) return false;
        if (target instanceof Player && !Boolean.TRUE.equals(
                target.getWorld().getGameRuleValue(GameRules.PVP))) return false;
        return Slimefun.getProtectionManager().hasPermission(player, target.getLocation(),
                target instanceof Player ? Interaction.ATTACK_PLAYER : Interaction.ATTACK_ENTITY);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer().getUniqueId());
    }

    public static void shutdown() {
        for (Runnable cleanup : new ArrayList<>(bleedCleanup)) cleanup.run();
        bleedCleanup.clear();
        cooldowns.clear();
        lastMessageTime.clear();
        invulnerableUntil.clear();
    }

    @Override
    public @NotNull List<ItemStack> getDisplayRecipes() {
        return List.of();
    }
}
