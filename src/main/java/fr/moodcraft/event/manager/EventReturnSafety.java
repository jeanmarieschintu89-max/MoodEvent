package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EventReturnSafety {

    private static final Map<UUID, Location> ORIGINAL_LOCATIONS = new HashMap<>();
    private static boolean started;

    private EventReturnSafety() {
    }

    public static void remember(Player player) {
        if (player == null || player.getWorld() == null) return;
        ORIGINAL_LOCATIONS.putIfAbsent(player.getUniqueId(), safe(player.getLocation().clone()));
    }

    public static void forget(Player player) {
        if (player != null) ORIGINAL_LOCATIONS.remove(player.getUniqueId());
    }

    public static boolean rescue(Player player) {
        if (player == null || !player.isOnline()) return false;
        Location location = ORIGINAL_LOCATIONS.get(player.getUniqueId());
        if (location == null || location.getWorld() == null) return false;
        Location safe = safe(location);
        if (safe == null || safe.getWorld() == null) return false;
        player.setFallDistance(0f);
        player.setFireTicks(0);
        player.teleport(safe);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
        ORIGINAL_LOCATIONS.remove(player.getUniqueId());
        return true;
    }

    public static void start() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                flushIfEventClosed();
            }
        }.runTaskTimer(Main.getInstance(), 5L, 5L);
    }

    private static void flushIfEventClosed() {
        if (ORIGINAL_LOCATIONS.isEmpty()) return;
        if (EventManager.isCreated() || EventManager.isRunning() || EventManager.isQueueOpen()) return;

        for (Map.Entry<UUID, Location> entry : new HashMap<>(ORIGINAL_LOCATIONS).entrySet()) {
            Player player = Main.getInstance().getServer().getPlayer(entry.getKey());
            Location location = safe(entry.getValue());
            if (player == null || !player.isOnline() || location == null || location.getWorld() == null) continue;
            player.setFallDistance(0f);
            player.setFireTicks(0);
            player.teleport(location);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
        }
        ORIGINAL_LOCATIONS.clear();
    }

    private static Location safe(Location location) {
        if (location == null || location.getWorld() == null) return location;
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = Math.max(world.getMinHeight() + 1, Math.min(world.getMaxHeight() - 3, location.getBlockY()));

        if (isSafe(world, x, y, z)) return location.clone().add(0, 0.1, 0);

        for (int radius = 0; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = 4; dy >= -12; dy--) {
                        int ny = y + dy;
                        if (ny <= world.getMinHeight() + 1 || ny >= world.getMaxHeight() - 2) continue;
                        if (isSafe(world, x + dx, ny, z + dz)) {
                            return new Location(world, x + dx + 0.5, ny, z + dz + 0.5, location.getYaw(), location.getPitch());
                        }
                    }
                }
            }
        }

        int topY = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x + 0.5, topY, z + 0.5, location.getYaw(), location.getPitch());
    }

    private static boolean isSafe(World world, int x, int y, int z) {
        Material feet = world.getBlockAt(x, y, z).getType();
        Material head = world.getBlockAt(x, y + 1, z).getType();
        Material ground = world.getBlockAt(x, y - 1, z).getType();
        return feet.isAir() && head.isAir() && ground.isSolid() && ground != Material.LAVA && ground != Material.MAGMA_BLOCK;
    }
}
