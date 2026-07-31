package io.Yomicer.magicExpansion.items.misc.moreLure;

import io.Yomicer.magicExpansion.MagicExpansion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ZKBTMPlayer {
    private final MagicExpansion plugin; // 替换为你的主类名

    public ZKBTMPlayer(MagicExpansion plugin) {
        this.plugin = plugin;
    }



    /**
     * 音乐播放器 - 播放一段约30秒轻松愉悦的旋律
     */
    public void playCuteMusic(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        // 温馨提示
        player.sendTitle("§e✨", "§eA cute little melody for you~", 10, 60, 20);

        new BukkitRunnable() {
            int beat = 0;
            final int TICKS_PER_BEAT = 12; // ≈100 BPM,轻快可爱节奏

            @Override
            public void run() {
                // === 轻柔鼓点:每拍响一次铃铛,第2、4拍加沙锤 ===
                if (beat % TICKS_PER_BEAT == 0) {
                    world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 0.4f, 0.8f);
                }
                if (beat % (4 * TICKS_PER_BEAT) == TICKS_PER_BEAT * 1 ||
                        beat % (4 * TICKS_PER_BEAT) == TICKS_PER_BEAT * 3) {
                    world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.2f);
                }

                // === 前奏:C大调上行音阶(C D E F G)跳跃感 ===
                if (beat == 0) {
                    player.sendTitle("§a🌸","§a: ~");
                }
                int[] melody = {0,2,4,5,7, 7,5,4,2, 0,2,4,5,7}; // C D E F G, G F E D, C D E F G
                float[] volumes = {1.0f, 0.9f, 1.0f, 0.9f, 1.2f, 1.2f, 0.9f, 0.8f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.3f};
                for (int i = 0; i < melody.length; i++) {
                    if (beat == i * TICKS_PER_BEAT) {
                        float pitch = (float) Math.pow(2, melody[i] / 12.0) * 1.0f; // C4 基准
                        world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, volumes[i], pitch);
                        spawnNoteParticle(world, loc, pitch);
                    }
                }

                // === 主旋律:可爱跳跃小调 ===
                if (beat == 14 * TICKS_PER_BEAT) {
                    player.sendTitle("§d🎀","§d: ~",10,20,20);
                }
                if (beat >= 14 * TICKS_PER_BEAT && beat < 26 * TICKS_PER_BEAT) {
                    int idx = beat / TICKS_PER_BEAT - 14;
                    int[] tune = {7,5,4,2, 0,-1,0,2, 4,5,7,9}; // G E D C, C B C D, E F G A
                    float p = (float) Math.pow(2, tune[idx] / 12.0) * 1.0f;
                    float v = idx % 4 == 0 ? 1.3f : 1.0f; // 强拍加重
                    world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, v, p);
                    spawnNoteParticle(world, loc, p);
                    if (idx == 0 || idx == 4 || idx == 8) {
                        world.spawnParticle(Particle.HEART, loc.clone().add(0,1,0), 1);
                    }
                }

                // === 副歌:欢快重复小段(情感高潮)===
                if (beat == 26 * TICKS_PER_BEAT) {
                    player.sendTitle("§c💖","§cChorus: spinning around with joy!",10,20,20);
                }
                if (beat >= 26 * TICKS_PER_BEAT && beat < 34 * TICKS_PER_BEAT) {
                    int idx = (beat / TICKS_PER_BEAT - 26) % 4;
                    int[] chorus = {7,9,7,5}; // G A G E
                    float p = (float) Math.pow(2, chorus[idx] / 12.0) * 1.0f;
                    world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.4f, p);
                    world.spawnParticle(Particle.FIREWORKS_SPARK, loc.clone().add(0,1.2,0), 2);
                }

                // === 尾声渐弱 ===
                if (beat == 34 * TICKS_PER_BEAT) {
                    player.sendTitle("§7💫","§7Finale: little stars twinkle...",10,20,20);
                    for (int i = 0; i < 3; i++) {
                        int f = i;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            float[] end = {4,2,0}; // E D C
                            float p = (float) Math.pow(2, end[f] / 12.0) * 1.0f;
                            world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f - f * 0.1f, p * 0.9f);
                            world.spawnParticle(Particle.TOWN_AURA, loc.clone().add(0,1,0), 1);
                        }, i * 20L);
                    }
                }

                // === 结束 ===
                if (beat == 40 * TICKS_PER_BEAT) {
                    player.sendTitle("§l🌟🎶\uD83C\uDF39", "You're so cute too!", 0, 60, 20);
                    player.sendMessage("§6🎶 The music has ended. Hope you feel better!");
                    this.cancel();
                }

                beat++;
            }

            // 生成音符粒子
            private void spawnNoteParticle(World w, Location l, float note) {
                Location p = l.clone().add(Math.random() - 0.5, 1.5, Math.random() - 0.5);
                w.spawnParticle(Particle.NOTE, p, 1, 0, 0, 0, note);
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }



}
