package io.Yomicer.magicExpansion.specialActions;

import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
import io.Yomicer.magicExpansion.utils.compat.EntityHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;

public class EntitySpawner {


    private final Random random = new Random();

    /**
     * 在玩家附近随机生成一个生物.
     *
     * @param player 玩家对象
     */
    public boolean spawnRandomEntityNearPlayer(Player player) {
        Location playerLocation = player.getLocation();

        double x = random.nextDouble(-5, 5);
        double y = random.nextDouble(0, 1);
        double z = random.nextDouble(-5, 5);
        Location spawnLocation = playerLocation.clone().add(x, y, z);

        if (!spawnLocation.getWorld().isChunkLoaded(
                spawnLocation.getBlockX() >> 4, spawnLocation.getBlockZ() >> 4)) {
            player.sendMessage(ColorGradient.getGradientName(
                    "The random summon target chunk is not loaded."));
            return false;
        }
        if (!ItemPermissionUtils.hasPermissionPoint(player, spawnLocation)) {
            player.sendMessage(ColorGradient.getGradientName(
                    "You do not have permission to summon an entity there."));
            return false;
        }

        List<EntityType> spawnable = Arrays.stream(EntityType.values())
                .filter(EntityType::isSpawnable)
                .toList();
        if (spawnable.isEmpty()) {
            player.sendMessage(ColorGradient.getGradientName("No spawnable entity types are available."));
            return false;
        }

        EntityType randomEntityType = spawnable.get(random.nextInt(spawnable.size()));
        try {
            Entity spawnedEntity = player.getWorld().spawnEntity(spawnLocation, randomEntityType);
            String entityName = "Magical Entity: " + EntityHelper.getName(spawnedEntity);
            spawnedEntity.setCustomName(ColorGradient.getGradientName(entityName));
            spawnedEntity.setCustomNameVisible(true);
            player.sendMessage(ColorGradient.getGradientName(
                    "Summoned magical entity: " + randomEntityType.name()));
            return true;
        } catch (RuntimeException | LinkageError failure) {
            player.sendMessage(ColorGradient.getGradientName("Failed to summon an entity."));
            return false;
        }
    }

}
