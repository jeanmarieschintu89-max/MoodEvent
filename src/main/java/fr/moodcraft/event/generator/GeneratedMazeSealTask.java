package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;

public final class GeneratedMazeSealTask {

    private static final String VERSION = "maze-random-seal-v2";
    private static final Random RANDOM = new Random();
    private static boolean started;

    private GeneratedMazeSealTask() {
    }

    public static void start() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                applyOnce();
            }
        }.runTaskTimer(Main.getInstance(), 100L, 120L);
    }

    private static void applyOnce() {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("active", false)) return;
        if (!"LABYRINTHE".equals(config.getString("type", ""))) return;

        World world = Bukkit.getWorld(config.getString("region.world", ""));
        Location oldStart = readLocation(config, "start");
        if (world == null || oldStart == null) return;

        int minX = config.getInt("region.min-x") + 2;
        int maxX = config.getInt("region.max-x") - 2;
        int minZ = config.getInt("region.min-z") + 2;
        int maxZ = config.getInt("region.max-z") - 2;
        int baseY = oldStart.getBlockY() - 1;

        String key = VERSION + ":" + world.getName() + ":" + minX + ":" + maxX + ":" + minZ + ":" + maxZ;
        if (key.equals(config.getString("maze-seal-key", ""))) return;

        Endpoint start = randomEndpoint(minX, maxX, minZ, maxZ, baseY, -1);
        Endpoint finish = randomEndpoint(minX, maxX, minZ, maxZ, baseY, opposite(start.side()));

        sealOuterRing(world, minX, maxX, minZ, maxZ, baseY);
        buildEndpoint(world, start, Material.LIME_CONCRETE);
        buildEndpoint(world, finish, Material.RED_CONCRETE);

        Location startLocation = new Location(world, start.x() + 0.5, baseY + 1, start.z() + 0.5, yawFor(start), 0f);
        Location finishLocation = new Location(world, finish.x() + 0.5, baseY + 1, finish.z() + 0.5, yawFor(finish), 0f);

        writeLocation(config, "start", startLocation);
        writeLocation(config, "finish", finishLocation);
        config.set("maze-seal-key", key);
        saveGenerated(file, config);
        updateLiveEventLocations(startLocation, finishLocation);
    }

    private static Endpoint randomEndpoint(int minX, int maxX, int minZ, int maxZ, int y, int preferredSide) {
        int side = preferredSide >= 0 ? preferredSide : RANDOM.nextInt(4);
        int margin = 5;
        int x;
        int z;
        int dx;
        int dz;

        switch (side) {
            case 0 -> {
                x = randomBetween(minX + margin, maxX - margin);
                z = minZ + 3;
                dx = 0;
                dz = 1;
            }
            case 1 -> {
                x = maxX - 3;
                z = randomBetween(minZ + margin, maxZ - margin);
                dx = -1;
                dz = 0;
            }
            case 2 -> {
                x = randomBetween(minX + margin, maxX - margin);
                z = maxZ - 3;
                dx = 0;
                dz = -1;
            }
            default -> {
                x = minX + 3;
                z = randomBetween(minZ + margin, maxZ - margin);
                dx = 1;
                dz = 0;
                side = 3;
            }
        }
        return new Endpoint(x, y, z, dx, dz, side);
    }

    private static int randomBetween(int min, int max) {
        if (max <= min) return min;
        return min + RANDOM.nextInt(max - min + 1);
    }

    private static int opposite(int side) {
        return (side + 2) % 4;
    }

    private static float yawFor(Endpoint endpoint) {
        if (endpoint.dx() > 0) return -90f;
        if (endpoint.dx() < 0) return 90f;
        if (endpoint.dz() > 0) return 0f;
        return 180f;
    }

    private static void sealOuterRing(World world, int minX, int maxX, int minZ, int maxZ, int y) {
        for (int x = minX; x <= maxX; x++) {
            wall(world, x, y, minZ);
            wall(world, x, y, maxZ);
        }
        for (int z = minZ; z <= maxZ; z++) {
            wall(world, minX, y, z);
            wall(world, maxX, y, z);
        }
    }

    private static void wall(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.STONE_BRICKS, false);
        world.getBlockAt(x, y + 1, z).setType(Material.MOSSY_STONE_BRICKS, false);
        world.getBlockAt(x, y + 2, z).setType(Material.STONE_BRICKS, false);
        world.getBlockAt(x, y + 3, z).setType(Material.CRACKED_STONE_BRICKS, false);
    }

    private static void buildEndpoint(World world, Endpoint endpoint, Material marker) {
        int cx = endpoint.x();
        int cy = endpoint.y();
        int cz = endpoint.z();

        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                world.getBlockAt(x, cy, z).setType(marker, false);
                for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }

        for (int step = 0; step <= 5; step++) {
            int bx = cx + endpoint.dx() * step;
            int bz = cz + endpoint.dz() * step;
            for (int side = -1; side <= 1; side++) {
                int x = endpoint.dz() == 0 ? bx : bx + side;
                int z = endpoint.dz() == 0 ? bz + side : bz;
                world.getBlockAt(x, cy, z).setType(marker, false);
                for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }

        for (int y = cy + 1; y <= cy + 4; y++) {
            if (endpoint.dz() == 0) {
                world.getBlockAt(cx, y, cz - 3).setType(Material.CHISELED_STONE_BRICKS, false);
                world.getBlockAt(cx, y, cz + 3).setType(Material.CHISELED_STONE_BRICKS, false);
            } else {
                world.getBlockAt(cx - 3, y, cz).setType(Material.CHISELED_STONE_BRICKS, false);
                world.getBlockAt(cx + 3, y, cz).setType(Material.CHISELED_STONE_BRICKS, false);
            }
        }
        world.getBlockAt(cx, cy + 4, cz).setType(Material.LANTERN, false);
    }

    private static void updateLiveEventLocations(Location start, Location finish) {
        try {
            setEventField("startLocation", start.clone());
            setEventField("finishLocation", finish.clone());
            FileConfiguration mainConfig = Main.getInstance().getConfig();
            writeLocation(mainConfig, "event.location", start);
            writeLocation(mainConfig, "event.finish-location", finish);
            Main.getInstance().saveConfig();
        } catch (ReflectiveOperationException exception) {
            Main.getInstance().getLogger().warning("Maze endpoint update failed: " + exception.getMessage());
        }
    }

    private static void setEventField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = EventManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static void saveGenerated(File file, FileConfiguration config) {
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void writeLocation(FileConfiguration config, String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }

    private record Endpoint(int x, int y, int z, int dx, int dz, int side) {
    }
}
