package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public final class GeneratedOpenPathTask {

    private static final String VERSION = "open-paths-v5";
    private static boolean started;

    private GeneratedOpenPathTask() {
    }

    public static void start() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                applyOnce();
            }
        }.runTaskTimer(Main.getInstance(), 60L, 80L);
    }

    private static void applyOnce() {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("active", false)) return;

        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        if (start == null) return;

        String type = config.getString("type", "UNKNOWN");
        String key = VERSION + ":" + type + ":" + start.getWorld().getName() + ":" + start.getBlockX() + ":" + start.getBlockY() + ":" + start.getBlockZ();
        if (key.equals(config.getString("open-path-key", ""))) return;

        openStart(start, finish);
        if (finish != null) openFinish(finish, start);

        config.set("open-path-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void openStart(Location start, Location finish) {
        openPad(start, Material.LIME_CONCRETE);
        openCorridor(start, finish, true);
    }

    private static void openFinish(Location finish, Location start) {
        openPad(finish, Material.RED_CONCRETE);
        openCorridor(finish, start, false);
    }

    private static void openPad(Location location, Material marker) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();

        for (int x = cx - 5; x <= cx + 5; x++) {
            for (int z = cz - 5; z <= cz + 5; z++) {
                int dist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                world.getBlockAt(x, cy, z).setType(dist <= 1 ? marker : Material.SMOOTH_STONE, false);
                for (int y = cy + 1; y <= cy + 5; y++) {
                    Material current = world.getBlockAt(x, y, z).getType();
                    if (shouldClear(current)) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openCorridor(Location from, Location toward, boolean forward) {
        World world = from.getWorld();
        if (world == null) return;
        int cx = from.getBlockX();
        int cy = from.getBlockY() - 1;
        int cz = from.getBlockZ();

        int dx = 1;
        int dz = 0;
        if (toward != null && toward.getWorld() != null && toward.getWorld().equals(world)) {
            int rawX = toward.getBlockX() - cx;
            int rawZ = toward.getBlockZ() - cz;
            if (Math.abs(rawZ) > Math.abs(rawX)) {
                dx = 0;
                dz = rawZ >= 0 ? 1 : -1;
            } else {
                dx = rawX >= 0 ? 1 : -1;
                dz = 0;
            }
        }
        if (!forward) {
            dx = -dx;
            dz = -dz;
        }

        for (int step = 0; step <= 12; step++) {
            int bx = cx + dx * step;
            int bz = cz + dz * step;
            for (int side = -3; side <= 3; side++) {
                int x = dz == 0 ? bx : bx + side;
                int z = dz == 0 ? bz + side : bz;
                world.getBlockAt(x, cy, z).setType(Material.SMOOTH_STONE, false);
                for (int y = cy + 1; y <= cy + 5; y++) {
                    Material current = world.getBlockAt(x, y, z).getType();
                    if (shouldClear(current)) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static boolean shouldClear(Material material) {
        if (material == null || material.isAir()) return false;
        String name = material.name();
        return name.contains("FENCE")
                || name.contains("WALL")
                || name.contains("GLASS")
                || material == Material.IRON_BARS
                || material == Material.HAY_BLOCK
                || material == Material.SPRUCE_TRAPDOOR
                || material == Material.OAK_TRAPDOOR
                || material == Material.REDSTONE_BLOCK
                || material == Material.EMERALD_BLOCK;
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }
}
