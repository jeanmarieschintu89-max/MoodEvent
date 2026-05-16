package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import org.bukkit.Location;
import org.bukkit.Sound;
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
        ORIGINAL_LOCATIONS.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
    }

    public static void forget(Player player) {
        if (player != null) ORIGINAL_LOCATIONS.remove(player.getUniqueId());
    }

    public static void start() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                flushIfEventClosed();
            }
        }.runTaskTimer(Main.getInstance(), 40L, 40L);
    }

    private static void flushIfEventClosed() {
        if (ORIGINAL_LOCATIONS.isEmpty()) return;
        if (EventManager.isCreated() || EventManager.isRunning() || EventManager.isQueueOpen()) return;

        for (Map.Entry<UUID, Location> entry : new HashMap<>(ORIGINAL_LOCATIONS).entrySet()) {
            Player player = Main.getInstance().getServer().getPlayer(entry.getKey());
            Location location = entry.getValue();
            if (player == null || !player.isOnline() || location == null || location.getWorld() == null) continue;
            player.teleport(location);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
        }
        ORIGINAL_LOCATIONS.clear();
    }
}
