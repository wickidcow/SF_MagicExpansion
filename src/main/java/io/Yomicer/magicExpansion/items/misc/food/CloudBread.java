package io.Yomicer.magicExpansion.items.misc.food; // 请修改为你的包名

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemConsumptionHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CloudBread extends SimpleSlimefunItem<ItemConsumptionHandler> {

    public CloudBread(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }
    public CloudBread(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
    }

    @Override
    public @NotNull ItemConsumptionHandler getItemHandler() {
        return (e, p, i) -> {
            // --- 1. 基础恢复 ---
            p.setHealth(Math.min(p.getHealth() + 8.0, p.getMaxHealth())); // 回 4 颗心

            // --- 2. 核心效果:轻盈之身 (15 秒) ---
            // 跳跃提升 III: 跳得非常高
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 300, 2));
            // 缓降: 落得非常慢,且免疫跌落伤害
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 300, 0));

            // --- 3. 特殊机制:即时抵消坠落动能 ---
            // 如果玩家正在下坠 (垂直速度 < 0),立即将其垂直速度设为 0,实现"空中急停"
            Vector velocity = p.getVelocity();
            if (velocity.getY() < -0.5) {
                velocity.setY(0);
                p.setVelocity(velocity);
                // 生成一团云雾表示缓冲
                p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.2, 0), 20, 0.3, 0.1, 0.3, 0.05);
                p.sendMessage("§f☁ A cloud caught you!");
            }

            // --- 4. 特效反馈 ---
            playCloudEffects(p);

            p.sendMessage("§f§l☁ You feel weightless... (§7Jump Boost and Slow Falling for 15 seconds§f)");
        };
    }

    /**
     * 播放云朵特效
     */
    private void playCloudEffects(Player p) {
        Location loc = p.getLocation().add(0, 0.5, 0);
        World world = p.getWorld();

        // 音效:轻柔的风声或气泡声
        p.playSound(loc, Sound.BLOCK_SNOW_PLACE, 1.0f, 1.5f); // 类似踩雪/蓬松的声音
        p.playSound(loc, Sound.ENTITY_GHAST_AMBIENT, 0.5f, 2.0f); // 空灵的背景音

        // 粒子:大量的云团 (CLOUD) 和 烟雾 (SMOKE_NORMAL)
        world.spawnParticle(Particle.CLOUD, loc, 30, 0.5, 0.2, 0.5, 0.05);
        world.spawnParticle(Particle.SMOKE_NORMAL, loc, 15, 0.4, 0.1, 0.4, 0.02);

        // 额外彩蛋:如果在空中食用,生成一圈白色的光环
        if (!p.isOnGround()) {
            for (int i = 0; i < 360; i += 30) {
                double rad = Math.toRadians(i);
                double x = loc.getX() + Math.cos(rad) * 0.6;
                double z = loc.getZ() + Math.sin(rad) * 0.6;
                world.spawnParticle(Particle.FLASH, new Location(world, x, loc.getY(), z), 1, 0, 0, 0, 0);
            }
        }
    }
}
