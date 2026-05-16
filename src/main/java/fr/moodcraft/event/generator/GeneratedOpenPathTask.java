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

    private static final String VERSION = "open-paths-v9";
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

        String type = config.getString("type", "UNKNOWN");
        if (type.equals("SURVIE_ETAGES")) return;

        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        if (start == null) return;

        String key = VERSION + ":" + type + ":" + start.getWorld().getName() + ":" + start.getBlockX() + ":" + start.getBlockY() + ":" + start.getBlockZ();
        if (key.equals(config.getString("open-path-key", ""))) return;

        GamePalette palette = palette(type);
        openStart(start, finish, palette);
        if (finish != null) openFinish(finish, start, palette);

        config.set("open-path-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void openStart(Location start, Location finish, GamePalette palette) {
        cleanPad(start, palette.start(), palette.floor());
        openCorridor(start, finish, true, palette.path());
        openArch(start, palette.start(), palette.light());
    }

    private static void openFinish(Location finish, Location start, GamePalette palette) {
        cleanPad(finish, palette.finish(), palette.floor());
        openCorridor(finish, start, false, palette.path());
        openArch(finish, palette.finish(), palette.light());
    }

    private static void cleanPad(Location location, Material center, Material floor) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();

        for (int x = cx - 5; x <= cx + 5; x++) {
            for (int z = cz - 5; z <= cz + 5; z++) {
                int dist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                world.getBlockAt(x, cy, z).setType(dist <= 1 ? center : floor, false);
                for (int y = cy + 1; y <= cy + 6; y++) {
                    Material current = world.getBlockAt(x, y, z).getType();
                    if (shouldClear(current)) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openCorridor(Location from, Location toward, boolean forward, Material floor) {
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

        for (int step = 0; step <= 14; step++) {
            int bx = cx + dx * step;
            int bz = cz + dz * step;
            for (int side = -3; side <= 3; side++) {
                int x = dz == 0 ? bx : bx + side;
                int z = dz == 0 ? bz + side : bz;
                world.getBlockAt(x, cy, z).setType(floor, false);
                for (int y = cy + 1; y <= cy + 6; y++) {
                    Material current = world.getBlockAt(x, y, z).getType();
                    if (shouldClear(current)) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openArch(Location location, Material pillar, Material light) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();

        for (int y = cy + 1; y <= cy + 4; y++) {
            world.getBlockAt(cx - 4, y, cz).setType(pillar, false);
            world.getBlockAt(cx + 4, y, cz).setType(pillar, false);
        }
        world.getBlockAt(cx - 4, cy + 5, cz).setType(light, false);
        world.getBlockAt(cx + 4, cy + 5, cz).setType(light, false);
        world.getBlockAt(cx, cy + 5, cz).setType(light, false);
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
                || material == Material.EMERALD_BLOCK
                || material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                || material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }

    private static GamePalette palette(String type) {
        return switch (type) {
            case "JUMP" -> new GamePalette(Material.LIME_WOOL, Material.RED_WOOL, Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.SEA_LANTERN);
            case "WATER_JUMP" -> new GamePalette(Material.LIME_WOOL, Material.RED_WOOL, Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.SEA_LANTERN);
            case "COURSE" -> new GamePalette(Material.LIME_CONCRETE, Material.RED_CONCRETE, Material.SMOOTH_STONE, Material.YELLOW_CONCRETE, Material.SEA_LANTERN);
            case "LABYRINTHE" -> new GamePalette(Material.LIME_CONCRETE, Material.RED_CONCRETE, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.LANTERN);
            case "RUEE_OR" -> new GamePalette(Material.GOLD_BLOCK, Material.RED_CONCRETE, Material.POLISHED_BLACKSTONE, Material.GOLD_BLOCK, Material.SEA_LANTERN);
            default -> new GamePalette(Material.LIME_CONCRETE, Material.RED_CONCRETE, Material.SMOOTH_STONE, Material.YELLOW_CONCRETE, Material.SEA_LANTERN);
        };
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }

    private record GamePalette(Material start, Material finish, Material floor, Material path, Material light) {
    }
}
