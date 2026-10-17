package io.Yomicer.magicExpansion.items.misc.weapon;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

class StarShardsSwordShieldTest {

    @Test
    void allowsOperatorCowHearderMeleeOnly() {
        assertTrue(StarShardsSword.allowsCowHearderMelee(
                true, "COW_HEARDER", EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        assertTrue(StarShardsSword.allowsCowHearderMelee(
                true, "COW_HEARDER", EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK));
    }

    @Test
    void directHealthBleedMustNotBypassAnAuthorizedCowHearder() {
        assertTrue(StarShardsSword.operatorHoldsCowHearder(true, "COW_HEARDER"));
        assertFalse(StarShardsSword.operatorHoldsCowHearder(false, "COW_HEARDER"));
        assertFalse(StarShardsSword.operatorHoldsCowHearder(true, "WEAPON_STAR_SHARDS_SWORD"));
        assertFalse(StarShardsSword.operatorHoldsCowHearder(true, null));
    }

    @Test
    void shieldContinuesBlockingNormalWeaponsAndNonOperators() {
        assertFalse(StarShardsSword.allowsCowHearderMelee(
                false, "COW_HEARDER", EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        assertFalse(StarShardsSword.allowsCowHearderMelee(
                true, "WEAPON_STAR_SHARDS_SWORD", EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        assertFalse(StarShardsSword.allowsCowHearderMelee(
                true, null, EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        assertFalse(StarShardsSword.allowsCowHearderMelee(
                true, "COW_HEARDER", EntityDamageEvent.DamageCause.PROJECTILE));
        assertFalse(StarShardsSword.allowsCowHearderMelee(
                true, "COW_HEARDER", EntityDamageEvent.DamageCause.MAGIC));
        assertFalse(StarShardsSword.allowsCowHearderMelee(
                true, "COW_HEARDER", EntityDamageEvent.DamageCause.VOID));
    }
}
