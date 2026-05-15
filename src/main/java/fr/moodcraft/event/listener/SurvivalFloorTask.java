package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class SurvivalFloorTask implements Listener {

    private int tick = 0;
    private int wave = 0;

    public SurvivalFloorTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                runSurvivalTick();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    private void runSurvivalTick() {
        if (!EventManager.isRunning() || EventManager.getType() != EventType.SURVIE_ETAGES || !GeneratedGameManager.hasStructure()) {
            tick = 0;
            wave = 0;
            return;
        }

        tick++;

        if (tick == 1) {
            forEachSurvivor(player -> {
                player.sendTitle("§dSurvie", "§fLes étages vont disparaître", 0, 35, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 0.8f);
            });
            broadcastWave("Les étages commencent à disparaître.");
        }

        forEachSurvivor(EventManager::checkSurvivalFloorElimination);

        if (tick < 4) {
            return;
        }

        wave++;
        int players = countSurvivors();
        int amount = Math.max(8, 6 + players * 4 + wave);
        int destroyed = GeneratedGameManager.destroySurvivalBlocks(amount);

        forEachSurvivor(player -> {
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 0.65f, 0.8f + Math.min(0.6f, wave * 0.02f));
            if (wave % 3 == 0) {
                player.sendActionBar("§d▣ §fLes étages disparaissent §8• §e" + destroyed + " §7blocs");
            }
        });

        if (wave % 5 == 0) {
            broadcastWave("Vague §e" + wave + " §7• §e" + destroyed + " §7blocs retirés.");
        }

        forEachSurvivor(EventManager::checkSurvivalFloorElimination);
    }

    private void forEachSurvivor(PlayerAction action) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (!GeneratedGameManager.isInsideStructure(player.getLocation())) continue;
            action.accept(player);
        }
    }

    private int countSurvivors() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (!GeneratedGameManager.isInsideStructure(player.getLocation())) continue;
            count++;
        }
        return count;
    }

    private void broadcastWave(String message) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.info("Survie des étages."));
        Bukkit.broadcastMessage(MoodStyle.detail(message));
        Bukkit.broadcastMessage(MoodStyle.detail("Restez en hauteur. Le premier en bas est perdant."));
        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    @FunctionalInterface
    private interface PlayerAction {
        void accept(Player player);
    }
}
