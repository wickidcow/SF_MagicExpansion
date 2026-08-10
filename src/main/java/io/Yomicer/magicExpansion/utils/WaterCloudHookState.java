package io.Yomicer.magicExpansion.utils;

import org.bukkit.Particle;
import org.bukkit.Sound;

/** Optional MagicExpansion-owned fishing states used only when no external fishing provider is primary. */
public enum WaterCloudHookState {
    WAITING(0.10, -1, 0.01, null, null, 0, "§7"),
    LIGHT_BITE(0.20, 8, 0.40, Sound.BLOCK_WATER_AMBIENT, Particle.WATER_BUBBLE, 3, "§e"),
    FULL_BITE(0.00, -1, 0.00, Sound.ENTITY_FISHING_BOBBER_SPLASH, Particle.WATER_SPLASH, 5, "§c");

    private final double biteChance;
    private final int maxSeconds;
    private final double catchRate;
    private final Sound sound;
    private final Particle particle;
    private final int particleCount;
    private final String actionBarColor;

    WaterCloudHookState(double biteChance, int maxSeconds, double catchRate,
                        Sound sound, Particle particle, int particleCount, String actionBarColor) {
        this.biteChance = biteChance;
        this.maxSeconds = maxSeconds;
        this.catchRate = catchRate;
        this.sound = sound;
        this.particle = particle;
        this.particleCount = particleCount;
        this.actionBarColor = actionBarColor;
    }

    public double getBiteChance() { return biteChance; }
    public int getMaxSeconds() { return maxSeconds; }
    public double getCatchRate() { return catchRate; }
    public Sound getSound() { return sound; }
    public Particle getParticle() { return particle; }
    public int getParticleCount() { return particleCount; }
    public String getActionBarColor() { return actionBarColor; }
}
