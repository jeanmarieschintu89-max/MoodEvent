package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
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
        if (!hasChangedBlock(event.getFrom(), event.getTo())) {
            return;
        }

        Player player = event.getPlayer();

        if (EventManager.isAtFinish(player)) {
            EventManager.finishPlayer(player);
            return;
        }

        if (shouldResetFall(player)) {
            resetToStart(player);
            return;
        }

        EventManager.checkSurvivalFloorElimination(player);
    }

    private boolean shouldResetFall(Player player) {
        if (player == null || !EventManager.isRunning() || !EventManager.isParticipant(player)) return false;
        EventType type = EventManager.getType();
        if (type != EventType.JUMP && type != EventType.WATER_JUMP) return false;

        Location start = getStartLocation();
        if (start == null || start.getWorld() == null || player.getWorld() == null || !player.getWorld().equals(start.getWorld())) return false;

        long now = System.currentTimeMillis();
        long last = FALL_RESET_COOLDOWN.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 1500L) return false;

        Location location = player.getLocation();
        Material feet = location.getBlock().getType();
        Material below = location.clone().subtract(0, 1, 0).getBlock().getType();
        boolean inWater = feet == Material.WATER || below == Material.WATER;
        boolean tooLow = location.getY() <= start.getY() - 3.5;
        return inWater || tooLow;
    }

    private void resetToStart(Player player) {
        Location start = getStartLocation();
        if (start == null || start.getWorld() == null) return;
        FALL_RESET_COOLDOWN.put(player.getUniqueId(), System.currentTimeMillis());
        player.teleport(start.clone().add(0, 0.25, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.75f, 1.25f);
        player.sendActionBar("§e➜ §fChute détectée §8• §7Retour au départ du parcours");
    }

    private Location getStartLocation() {
        FileConfiguration config = Main.getInstance().getConfig();
        World world = Bukkit.getWorld(config.getString("event.location.world", ""));
        if (world == null) return null;
        return new Location(
                world,
                config.getDouble("event.location.x"),
                config.getDouble("event.location.y"),
                config.getDouble("event.location.z"),
                (float) config.getDouble("event.location.yaw"),
                (float) config.getDouble("event.location.pitch")
        );
    }

    private boolean hasChangedBlock(Location from, Location to) {
        if (to == null || from.getWorld() == null || to.getWorld() == null) {
            return false;
        }
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()
                || !from.getWorld().equals(to.getWorld());
    }
}
