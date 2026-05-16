package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;

public final class SurvivalFloorLagGuard {

    private static boolean started;
    private static BukkitTask optimizedTask;

    private SurvivalFloorLagGuard() {
    }

    public static void start() {
        if (started) return;
        started = true;
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), SurvivalFloorLagGuard::watch, 40L, 40L);
    }

    private static void watch() {
        if (!EventManager.isRunning() || EventManager.getType() != EventType.SURVIE_ETAGES) {
            cancelOptimized();
            return;
        }
        cancelInternalTask();
        if (optimizedTask == null || optimizedTask.isCancelled()) {
            optimizedTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), SurvivalFloorLagGuard::tickOptimized, 60L, 100L);
        }
    }

    private static void tickOptimized() {
        if (!EventManager.isRunning() || EventManager.getType() != EventType.SURVIE_ETAGES) {
            cancelOptimized();
            return;
        }
        int amount = Math.max(1, Math.min(4, EventManager.getParticipantSize()));
        GeneratedGameManager.destroySurvivalBlocks(amount);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (EventManager.isParticipant(player)) EventManager.checkSurvivalFloorElimination(player);
        }
    }

    private static void cancelOptimized() {
        if (optimizedTask != null) {
            optimizedTask.cancel();
            optimizedTask = null;
        }
    }

    private static void cancelInternalTask() {
        try {
            Field field = EventManager.class.getDeclaredField("survivalTask");
            field.setAccessible(true);
            Object value = field.get(null);
            if (value instanceof BukkitTask task && !task.isCancelled()) task.cancel();
            field.set(null, null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
