package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Set;

public class EventMobGuard implements Listener {

    private static final Set<EntityType> BLOCKED = EnumSet.of(
            EntityType.ZOMBIE,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.ENDERMAN,
            EntityType.WITCH,
            EntityType.SLIME,
            EntityType.MAGMA_CUBE,
            EntityType.PHANTOM,
            EntityType.PILLAGER,
            EntityType.VINDICATOR,
            EntityType.EVOKER,
            EntityType.VEX,
            EntityType.RAVAGER,
            EntityType.BLAZE,
            EntityType.GHAST,
            EntityType.WITHER_SKELETON,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.HOGLIN,
            EntityType.ZOGLIN,
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            EntityType.SILVERFISH,
            EntityType.ENDERMITE,
            EntityType.SHULKER,
            EntityType.WARDEN,
            EntityType.BREEZE
    );

    public EventMobGuard() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExistingMobs();
            }
        }.runTaskTimer(Main.getInstance(), 60L, 100L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isBlocked(event.getEntityType())) return;
        if (!isEventArea(event.getLocation())) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!isBlocked(event.getEntityType())) return;
        if (!isEventArea(player.getLocation())) return;
        event.setCancelled(true);
    }

    private void cleanupExistingMobs() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity)) continue;
                if (!isBlocked(entity.getType())) continue;
                if (!isEventArea(entity.getLocation())) continue;
                entity.remove();
            }
        }
    }

    private boolean isBlocked(EntityType type) {
        return type != null && BLOCKED.contains(type);
    }

    private boolean isEventArea(Location location) {
        if (location == null || location.getWorld() == null) return false;
        return GeneratedGameManager.isInsideStructure(location) || isNearWaitingRoom(location);
    }

    private boolean isNearWaitingRoom(Location location) {
        Location spawn = WaitingRoomManager.getSpawn();
        if (spawn == null || spawn.getWorld() == null) return false;
        if (!spawn.getWorld().equals(location.getWorld())) return false;
        return spawn.distanceSquared(location) <= 40 * 40;
    }
}
