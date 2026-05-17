package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EventLaunchBufferManager {

    private static final Map<UUID, Location> LOCKS = new HashMap<>();

    private static boolean started;
    private static boolean bufferedThisRun;
    private static int countdown;

    private EventLaunchBufferManager() {
    }

    public static void start() {
        if (started) return;
        started = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    public static boolean isLocked(Player player) {
        return player != null && LOCKS.containsKey(player.getUniqueId());
    }

    public static boolean isBufferActive() {
        return !LOCKS.isEmpty();
    }

    public static boolean hasBufferedThisRun() {
        return bufferedThisRun;
    }

    public static Location getLockLocation(Player player) {
        if (player == null) return null;
        Location location = LOCKS.get(player.getUniqueId());
        return location == null ? null : location.clone();
    }

    private static void tick() {
        if (!EventManager.isRunning()) {
            reset();
            return;
        }

        if (!bufferedThisRun && EventManager.getParticipantSize() > 0) {
            beginBuffer();
            return;
        }

        if (LOCKS.isEmpty()) return;

        if (countdown > 0) {
            showCount(countdown);
            countdown--;
            return;
        }

        showGo();
        LOCKS.clear();
    }

    private static void beginBuffer() {
        bufferedThisRun = true;
        countdown = 3;
        LOCKS.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            LOCKS.put(player.getUniqueId(), player.getLocation().clone());
        }
        showCount(countdown);
        countdown--;
    }

    private static void showCount(int number) {
        for (UUID uuid : LOCKS.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            player.sendTitle("§6" + number, "§fPrépare-toi", 0, 18, 4);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.2f);
        }
    }

    private static void showGo() {
        for (UUID uuid : LOCKS.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            player.sendTitle("§aGO", "§fBonne chance", 0, 22, 6);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.15f);
        }
    }

    private static void reset() {
        LOCKS.clear();
        bufferedThisRun = false;
        countdown = 0;
    }
}
