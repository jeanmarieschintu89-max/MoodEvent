package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class SurvivalFloorTask implements Listener {

    private int tick = 0;
    private int wave = 0;

    public SurvivalFloorTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                runSurvivalTick();
            }
        }.runTaskTimer(Main.getInstance(), 10L, 10L);
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
                player.sendTitle("§dSurvie", "§fLes étages vont céder", 0, 35, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 0.8f);
            });
            broadcastWave("Les étages vont disparaître très vite.");
        }

        forEachSurvivor(EventManager::checkSurvivalFloorElimination);

        if (tick < 3) {
            return;
        }

        wave++;
        int players = Math.max(1, countSurvivors());
        int acceleration = Math.min(90, wave * 2);
        int amount = Math.max(35, 28 + players * 10 + acceleration);
        int destroyed = GeneratedGameManager.destroySurvivalBlocks(amount);
        destroyed += breakCenterIslands();

        int finalDestroyed = destroyed;
        forEachSurvivor(player -> {
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 0.85f, 0.75f + Math.min(0.7f, wave * 0.025f));
            player.sendActionBar("§d▣ §fSol instable §8• §e" + finalDestroyed + " §7blocs retirés");
            if (wave % 6 == 0) {
                player.sendTitle("§d▣", "§fLes étages s'effondrent", 0, 18, 6);
            }
        });

        if (wave % 8 == 0) {
            broadcastWave("Vague §e" + wave + " §7• §e" + destroyed + " §7blocs retirés.");
        }

        forEachSurvivor(EventManager::checkSurvivalFloorElimination);
    }

    private int breakCenterIslands() {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return 0;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!"SURVIE_ETAGES".equalsIgnoreCase(config.getString("type", ""))) return 0;

        World world = Bukkit.getWorld(config.getString("region.world", ""));
        if (world == null) return 0;

        int centerX = config.getInt("start.x");
        int centerZ = config.getInt("start.z");
        int minY = config.getInt("region.min-y");
        int maxY = config.getInt("region.max-y");
        int removed = 0;

        for (int y = minY + 1; y <= maxY; y++) {
            for (int x = centerX - 2; x <= centerX + 2; x++) {
                for (int z = centerZ - 2; z <= centerZ + 2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!isCenterFloorBlock(block.getType())) continue;
                    block.setType(Material.AIR, false);
                    removed++;
                }
            }
        }
        return removed;
    }

    private boolean isCenterFloorBlock(Material material) {
        if (material == null || material.isAir()) return false;
        String name = material.name();
        return name.endsWith("_WOOL")
                || name.endsWith("_CONCRETE")
                || material == Material.SEA_LANTERN
                || material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                || material == Material.GOLD_BLOCK
                || material == Material.EMERALD_BLOCK
                || material == Material.REDSTONE_BLOCK;
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
