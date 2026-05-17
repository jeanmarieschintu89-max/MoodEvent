package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.manager.EventLaunchBufferManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
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
    private boolean announcedStart;

    public SurvivalFloorTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                runSurvivalTick();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    private void runSurvivalTick() {
        if (!EventManager.isRunning()
                || EventManager.getType() != EventType.SURVIE_ETAGES
                || !GeneratedGameManager.hasStructure()
                || GeneratedGameManager.getActiveType() != GeneratedGameType.SURVIE_ETAGES) {
            tick = 0;
            wave = 0;
            announcedStart = false;
            return;
        }

        forEachSurvivor(EventManager::checkSurvivalFloorElimination);

        if (!EventLaunchBufferManager.hasBufferedThisRun() || EventLaunchBufferManager.isBufferActive()) {
            return;
        }

        tick++;

        if (!announcedStart) {
            announcedStart = true;
            forEachSurvivor(player -> {
                player.sendActionBar("§d▣ §fEffondrement lancé §8• §7reste en vie");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 0.9f);
            });
        }

        wave++;
        int players = Math.max(1, countSurvivors());
        int acceleration = Math.min(45, wave);
        int amount = Math.max(8, 6 + players * 2 + acceleration);
        int destroyed = GeneratedGameManager.destroySurvivalBlocks(amount);

        if (wave >= 5) destroyed += breakLooseSurvivalBlocks(Math.max(4, players + wave / 2));
        if (wave >= 12 && wave % 2 == 0) destroyed += breakLooseSurvivalBlocks(Math.max(10, wave));
        if (wave >= 24 && wave % 3 == 0) destroyed += breakLooseSurvivalBlocks(35);

        int finalDestroyed = destroyed;
        forEachSurvivor(player -> {
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 0.45f, 0.85f + Math.min(0.35f, wave * 0.01f));
            player.sendActionBar("§d▣ §fSol instable §8• §e" + finalDestroyed + " §7blocs retirés");
            if (wave % 12 == 0) player.sendTitle("§d▣", "§fLes étages s'effondrent", 0, 18, 6);
        });

        forEachSurvivor(EventManager::checkSurvivalFloorElimination);
    }

    private int breakLooseSurvivalBlocks(int maxRemoved) {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return 0;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!"SURVIE_ETAGES".equalsIgnoreCase(config.getString("type", ""))) return 0;

        World world = Bukkit.getWorld(config.getString("region.world", ""));
        if (world == null) return 0;

        int minX = config.getInt("region.min-x") + 2;
        int maxX = config.getInt("region.max-x") - 2;
        int minY = config.getInt("region.min-y") + 1;
        int maxY = config.getInt("region.max-y");
        int minZ = config.getInt("region.min-z") + 2;
        int maxZ = config.getInt("region.max-z") - 2;
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;
        int removed = 0;

        for (int y = maxY; y >= minY; y--) {
            int radius = Math.max(maxX - minX, maxZ - minZ) / 2;
            for (int distance = 0; distance <= radius; distance++) {
                for (int x = centerX - distance; x <= centerX + distance; x++) {
                    for (int z = centerZ - distance; z <= centerZ + distance; z++) {
                        if (removed >= maxRemoved) return removed;
                        if (x < minX || x > maxX || z < minZ || z > maxZ) continue;
                        if (Math.abs(x - centerX) != distance && Math.abs(z - centerZ) != distance) continue;
                        Block block = world.getBlockAt(x, y, z);
                        if (!isBreakableSurvivalBlock(block.getType())) continue;
                        block.setType(Material.AIR, false);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    private boolean isBreakableSurvivalBlock(Material material) {
        if (material == null || material.isAir()) return false;
        String name = material.name();
        return name.endsWith("_WOOL")
                || material == Material.LIME_CONCRETE
                || material == Material.RED_CONCRETE
                || material == Material.GOLD_BLOCK
                || material == Material.EMERALD_BLOCK
                || material == Material.REDSTONE_BLOCK
                || material == Material.SEA_LANTERN
                || material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                || material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
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

    @FunctionalInterface
    private interface PlayerAction {
        void accept(Player player);
    }
}
