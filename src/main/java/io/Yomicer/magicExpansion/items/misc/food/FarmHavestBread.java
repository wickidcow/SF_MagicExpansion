package io.Yomicer.magicExpansion.items.misc.food;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemConsumptionHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FarmHavestBread extends SimpleSlimefunItem<ItemConsumptionHandler> {

    private static final Random RANDOM = new Random();

    public FarmHavestBread(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public @NotNull ItemConsumptionHandler getItemHandler() {
        return (e, p, i)->{
            double maxHealth = p.getMaxHealth();
            double currentHealth = p.getHealth();
            double newHealth = Math.min(currentHealth + 8.0, maxHealth);
            p.setHealth(newHealth);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 300, 2)); // 等级 2 对应 III (0=I, 1=II, 2=III)
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 0)); // 等级 0 对应 I
            if (RANDOM.nextDouble() < 0.20) {
                ItemStack seeds = MagicExpansionItems.WHEAT_SEEDS;
                p.getWorld().dropItemNaturally(p.getLocation(), seeds);
                p.sendMessage("§e✨!you type.");
            }
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);
            p.getWorld().playEffect(p.getLocation().add(0, 1, 0), Effect.VILLAGER_PLANT_GROW, 10);
        };
    }
}
