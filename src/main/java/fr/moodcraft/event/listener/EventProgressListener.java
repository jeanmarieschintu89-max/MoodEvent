package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.EventLaunchBufferManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventProgressListener implements Listener {

    private static final Map<UUID, Long> FALL_RESET_COOLDOWN = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!hasChangedBlock(event.getFrom(), event.getTo())) return;

        Player player = event.getPlayer();

        if (EventLaunchBufferManager.isLocked(player)) {
            Location locked = EventLaunchBufferManager.getLockLocation(player);
            if (locked != null && locked.getWorld() != null) {
                event.setTo(locked.clone());
                player.sendActionBar("§6✦ §fDépart imminent");
            }
            return;
        }

        if (EventManager.isAtFinish(player) || isOnFinishZone(player)) {
            EventManager.finishPlayer(player);
            return;
        }

        if (shouldResetFall(player)) {
            resetToStart(player);
            return;
        }

        EventManager.checkSurvivalFloorElimination(player);
    }

    private boolean isOnFinishZone(Player player) {
        if (player == null || !EventManager.isRunning() || !EventManager.isParticipant(player)) return false;
        EventType type = EventManager.getType();
        if (type != EventType.WATER_JUMP && type != EventType.JUMP && type != EventType.COURSE && type != EventType.LABYRINTHE) return false;

        Location finish = getFinishLocation();
        if (finish == null || finish.getWorld() == null || player.getWorld() == null || !player.getWorld().equals(finish.getWorld())) return false;

        Location location = player.getLocation();
        if (Math.abs(location.getX() - finish.getX()) > 6.0 || Math.abs(location.getZ() - finish.getZ()) > 6.0 || Math.abs(location.getY() - finish.getY()) > 5.0) return false;

        Material below = location.clone().subtract(0, 1, 0).getBlock().getType();
        Material feet = location.getBlock().getType();
        return isFinishMaterial(below) || isFinishMaterial(feet);
    }

    private boolean isFinishMaterial(Material material) {
        return material == Material.RED_WOOL
                || material == Material.RED_CONCRETE
                || material == Material.REDSTONE_BLOCK
                || material == Material.RED_STAINED_GLASS
                || material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                || material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
    }

    private boolean shouldResetFall(Player player) {
        if (player == null || !EventManager.isRunning() || !EventManager.isParticipant(player)) return false;
        EventType type = EventManager.getType();
        if (type != EventType.JUMP && type != EventType.WATER_JUMP) return false;

        Location start = getStartLocation();
        if (start == null || start.getWorld() == null || player.getWorld() == null || !player.getWorld().equals(start.getWorld())) return false;

        long now = System.currentTimeMillis();
        long last = FALL_RESET_COOLDOWN.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 1200L) return false;

        Location location = player.getLocation();
        Material feet = location.getBlock().getType();
        Material below = location.clone().subtract(0, 1, 0).getBlock().getType();

        if (type == EventType.WATER_JUMP) {
            boolean inWater = feet == Material.WATER || below == Material.WATER;
            boolean tooLow = location.getY() <= start.getY() - 2.0;
            return inWater || tooLow;
        }

        boolean landedBelowCourse = location.getY() <= start.getY() - 1.25;
        boolean onSafetyFloor = below == Material.BLUE_CONCRETE
                || below == Material.LIGHT_BLUE_CONCRETE
                || below == Material.SMOOTH_STONE
                || below == Material.STONE
                || below == Material.BLACK_CONCRETE;

        return landedBelowCourse || onSafetyFloor;
    }

    private void resetToStart(Player player) {
        Location start = getStartLocation();
        if (start == null || start.getWorld() == null) return;
        FALL_RESET_COOLDOWN.put(player.getUniqueId(), System.currentTimeMillis());
        player.setFallDistance(0f);
        player.teleport(start.clone().add(0, 0.25, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.75f, 1.25f);
        player.sendActionBar("§e➜ §fChute détectée §8• §7Retour au départ de l'épreuve");
    }

    private Location getStartLocation() {
        FileConfiguration config = Main.getInstance().getConfig();
        World world = Bukkit.getWorld(config.getString("event.location.world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble("event.location.x"), config.getDouble("event.location.y"), config.getDouble("event.location.z"), (float) config.getDouble("event.location.yaw"), (float) config.getDouble("event.location.pitch"));
    }

    private Location getFinishLocation() {
        FileConfiguration config = Main.getInstance().getConfig();
        World world = Bukkit.getWorld(config.getString("event.finish-location.world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble("event.finish-location.x"), config.getDouble("event.finish-location.y"), config.getDouble("event.finish-location.z"), (float) config.getDouble("event.finish-location.yaw"), (float) config.getDouble("event.finish-location.pitch"));
    }

    private boolean hasChangedBlock(Location from, Location to) {
        if (to == null || from.getWorld() == null || to.getWorld() == null) return false;
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()
                || !from.getWorld().equals(to.getWorld());
    }
}
