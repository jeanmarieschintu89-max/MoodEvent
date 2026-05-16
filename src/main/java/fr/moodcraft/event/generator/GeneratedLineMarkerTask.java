package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public final class GeneratedLineMarkerTask {

    private static boolean started;

    private GeneratedLineMarkerTask() {
    }

    public static void start() {
        if (started) return;
        started = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Main.getInstance(), 40L, 40L);
    }

    private static void tick() {
        if (!GeneratedGameManager.hasStructure()) return;

        GeneratedGameType activeType = GeneratedGameManager.getActiveType();
        if (activeType == null) return;

        EventType eventType = activeType.getEventType();
        if (eventType != EventType.COURSE && eventType != EventType.JUMP && eventType != EventType.WATER_JUMP) return;

        FileConfiguration config = GeneratedGameManager.config();
        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        if (start == null || finish == null || start.getWorld() == null) return;

        switch (eventType) {
            case COURSE -> {
                drawGroundLine(start, 2, Material.LIME_CONCRETE);
                drawGroundLine(finish, 2, Material.RED_CONCRETE);
            }
            case JUMP -> {
                drawGroundLine(start, 3, Material.LIME_WOOL);
                drawGroundLine(finish, 3, Material.RED_WOOL);
            }
            case WATER_JUMP -> {
                drawGroundLine(start, 4, Material.LIME_WOOL);
                drawGroundLine(finish, 4, Material.RED_WOOL);
            }
            default -> { }
        }
    }

    private static void drawGroundLine(Location location, int halfWidth, Material material) {
        if (location == null || location.getWorld() == null || material == null) return;
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = Math.max(world.getMinHeight(), location.getBlockY() - 1);
        int z = location.getBlockZ();

        for (int dz = -halfWidth; dz <= halfWidth; dz++) {
            world.getBlockAt(x, y, z + dz).setType(material, false);
            world.getBlockAt(x, y + 1, z + dz).setType(Material.AIR, false);
        }
    }

    private static Location readLocation(FileConfiguration config, String path) {
        if (config == null) return null;
        World world = org.bukkit.Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }
}
