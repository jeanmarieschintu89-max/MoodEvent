package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class EventAutoStartTask {

    private static final int AUTO_START_SECONDS = 60;

    private static int closedSeconds;
    private static int lastQueueSize;
    private static boolean started;

    private EventAutoStartTask() {
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

    private static void tick() {
        if (!EventManager.isCreated() || EventManager.isRunning() || EventManager.isQueueOpen() || EventManager.getQueueSize() <= 0) {
            reset();
            return;
        }

        int currentQueueSize = EventManager.getQueueSize();
        if (currentQueueSize != lastQueueSize) {
            lastQueueSize = currentQueueSize;
            closedSeconds = 0;
        }

        closedSeconds++;
        announceCountdown();

        if (closedSeconds < AUTO_START_SECONDS) return;

        Player actor = findActor();
        if (actor == null) {
            reset();
            return;
        }

        reset();
        EventManager.startEvent(actor);
    }

    private static void announceCountdown() {
        int remaining = AUTO_START_SECONDS - closedSeconds;
        if (remaining != 30 && remaining != 10) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isEventPlayer(player)) continue;
            player.sendTitle("§6" + remaining + "s", "§fPrépare-toi", 0, 30, 8);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.1f);
            if (remaining == 10) {
                MoodStyle.send(player, MoodStyle.MODULE,
                        MoodStyle.hype("Départ dans §e10 secondes !"),
                        MoodStyle.detail("Prépare-toi."));
            } else {
                MoodStyle.send(player, MoodStyle.MODULE,
                        MoodStyle.info("Départ dans §e30 secondes."),
                        MoodStyle.detail("Prépare-toi."));
            }
        }
    }

    private static Player findActor() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (EventManager.isEventPlayer(player)) return player;
        }
        for (Player player : Bukkit.getOnlinePlayers()) return player;
        return null;
    }

    private static void reset() {
        closedSeconds = 0;
        lastQueueSize = EventManager.getQueueSize();
    }
}
