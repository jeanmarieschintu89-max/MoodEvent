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
                drawCleanFloorLine(start, 2, Material.LIME_CONCRETE);
                drawCleanFloorLine(finish, 2, Material.RED_CONCRETE);
            }
            case JUMP -> {
                drawCleanFloorLine(start, 4, Material.LIME_WOOL);
                drawCleanFloorLine(finish, 4, Material.RED_WOOL);
            }
            case WATER_JUMP -> {
                drawCleanFloorLine(start, 4, Material.LIME_WOOL);
                drawCleanFloorLine(finish, 4, Material.RED_WOOL);
            }
            default -> { }
        }
    }

    private static void drawCleanFloorLine(Location location, int halfWidth, Material lineMaterial) {
        if (location == null || location.getWorld() == null || lineMaterial == null) return;
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = Math.max(world.getMinHeight(), location.getBlockY() - 1);
        int z = location.getBlockZ();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -halfWidth - 1; dz <= halfWidth + 1; dz++) {
                removeMarkerGarbage(world, x + dx, y, z + dz);
            }
        }

        for (int dz = -halfWidth; dz <= halfWidth; dz++) {
            world.getBlockAt(x, y, z + dz).setType(lineMaterial, false);
            world.getBlockAt(x, y + 1, z + dz).setType(Material.AIR, false);
        }
    }

    private static void removeMarkerGarbage(World world, int x, int y, int z) {
        for (int dy = 1; dy <= 5; dy++) {
            Material type = world.getBlockAt(x, y + dy, z).getType();
            if (type == Material.EMERALD_BLOCK
                    || type == Material.REDSTONE_BLOCK
                    || type == Material.SEA_LANTERN
                    || type == Material.LIME_STAINED_GLASS
                    || type == Material.RED_STAINED_GLASS
                    || type == Material.LIGHT_BLUE_STAINED_GLASS
                    || type == Material.OAK_FENCE
                    || type == Material.IRON_BARS) {
                world.getBlockAt(x, y + dy, z).setType(Material.AIR, false);
            }
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
