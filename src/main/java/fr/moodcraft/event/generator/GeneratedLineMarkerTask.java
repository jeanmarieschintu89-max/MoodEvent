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
                buildFlatStartZone(start, 2, Material.LIME_CONCRETE, Material.SMOOTH_STONE, true);
                buildFlatStartZone(finish, 2, Material.RED_CONCRETE, Material.SMOOTH_STONE, false);
            }
            case JUMP -> {
                buildFlatStartZone(start, 4, Material.LIME_WOOL, Material.BLUE_CONCRETE, true);
                buildFlatStartZone(finish, 4, Material.RED_WOOL, Material.BLUE_CONCRETE, false);
            }
            case WATER_JUMP -> {
                buildFlatStartZone(start, 4, Material.LIME_WOOL, Material.WATER, true);
                buildFlatStartZone(finish, 4, Material.RED_WOOL, Material.WATER, false);
            }
            default -> { }
        }
    }

    private static void buildFlatStartZone(Location location, int halfWidth, Material lineMaterial, Material baseMaterial, boolean start) {
        if (location == null || location.getWorld() == null || lineMaterial == null || baseMaterial == null) return;
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = Math.max(world.getMinHeight(), location.getBlockY() - 1);
        int z = location.getBlockZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                Material floor = dx == 0 ? lineMaterial : baseMaterial;
                world.getBlockAt(x + dx, y, z + dz).setType(floor, false);
                world.getBlockAt(x + dx, y + 1, z + dz).setType(Material.AIR, false);
                world.getBlockAt(x + dx, y + 2, z + dz).setType(Material.AIR, false);
            }
        }

        Material pillar = start ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        for (int dy = 1; dy <= 3; dy++) {
            world.getBlockAt(x, y + dy, z - halfWidth).setType(pillar, false);
            world.getBlockAt(x, y + dy, z + halfWidth).setType(pillar, false);
        }
        world.getBlockAt(x, y + 4, z).setType(Material.SEA_LANTERN, false);
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
