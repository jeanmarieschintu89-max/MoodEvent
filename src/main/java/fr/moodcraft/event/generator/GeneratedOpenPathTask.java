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

    private static final String VERSION = "open-paths-v10";
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
        Direction startDir = direction(start, finish, true);
        openStart(start, startDir, palette);
        if (finish != null) openFinish(finish, direction(finish, start, false), palette);

        config.set("open-path-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void openStart(Location start, Direction direction, GamePalette palette) {
        cleanPad(start, palette.start(), palette.floor());
        buildStartRoom(start, direction, palette);
        openCorridor(start, direction, true, palette.path());
    }

    private static void openFinish(Location finish, Direction direction, GamePalette palette) {
        cleanPad(finish, palette.finish(), palette.floor());
        buildFinishStage(finish, direction, palette);
        openCorridor(finish, direction, true, palette.path());
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

    private static void buildStartRoom(Location location, Direction direction, GamePalette palette) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();
        int backX = cx - direction.dx * 5;
        int backZ = cz - direction.dz * 5;

        for (int side = -5; side <= 5; side++) {
            for (int h = 1; h <= 4; h++) {
                int sx1 = cx + side * sideX(direction) - direction.dx * 5;
                int sz1 = cz + side * sideZ(direction) - direction.dz * 5;
                world.getBlockAt(sx1, cy + h, sz1).setType(palette.wall(), false);

                int leftX = cx - sideX(direction) * 5 + direction.dx * side;
                int leftZ = cz - sideZ(direction) * 5 + direction.dz * side;
                int rightX = cx + sideX(direction) * 5 + direction.dx * side;
                int rightZ = cz + sideZ(direction) * 5 + direction.dz * side;
                world.getBlockAt(leftX, cy + h, leftZ).setType(palette.wall(), false);
                world.getBlockAt(rightX, cy + h, rightZ).setType(palette.wall(), false);
            }
        }

        for (int side = -2; side <= 2; side++) {
            for (int h = 1; h <= 4; h++) {
                int x = backX + side * sideX(direction);
                int z = backZ + side * sideZ(direction);
                world.getBlockAt(x, cy + h, z).setType(palette.wall(), false);
            }
        }

        openArch(location, direction, palette.start(), palette.light());
    }

    private static void buildFinishStage(Location location, Direction direction, GamePalette palette) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();
        int backX = cx - direction.dx * 5;
        int backZ = cz - direction.dz * 5;

        for (int side = -5; side <= 5; side++) {
            for (int h = 1; h <= 4; h++) {
                int x = backX + side * sideX(direction);
                int z = backZ + side * sideZ(direction);
                world.getBlockAt(x, cy + h, z).setType(palette.finish(), false);
            }
        }
        openArch(location, direction, palette.finish(), palette.light());
    }

    private static void openCorridor(Location from, Direction direction, boolean forward, Material floor) {
        World world = from.getWorld();
        if (world == null) return;
        int cx = from.getBlockX();
        int cy = from.getBlockY() - 1;
        int cz = from.getBlockZ();
        int dir = forward ? 1 : -1;

        for (int step = 0; step <= 14; step++) {
            int bx = cx + direction.dx * step * dir;
            int bz = cz + direction.dz * step * dir;
            for (int side = -3; side <= 3; side++) {
                int x = bx + side * sideX(direction);
                int z = bz + side * sideZ(direction);
                world.getBlockAt(x, cy, z).setType(floor, false);
                for (int y = cy + 1; y <= cy + 6; y++) {
                    Material current = world.getBlockAt(x, y, z).getType();
                    if (shouldClear(current)) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openArch(Location location, Direction direction, Material pillar, Material light) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();
        int frontX = cx + direction.dx * 4;
        int frontZ = cz + direction.dz * 4;

        for (int y = cy + 1; y <= cy + 4; y++) {
            world.getBlockAt(frontX + sideX(direction) * -4, y, frontZ + sideZ(direction) * -4).setType(pillar, false);
            world.getBlockAt(frontX + sideX(direction) * 4, y, frontZ + sideZ(direction) * 4).setType(pillar, false);
        }
        for (int side = -4; side <= 4; side++) {
            if (Math.abs(side) <= 1) continue;
            world.getBlockAt(frontX + sideX(direction) * side, cy + 4, frontZ + sideZ(direction) * side).setType(light, false);
        }
    }

    private static Direction direction(Location from, Location toward, boolean defaultForward) {
        if (toward == null || toward.getWorld() == null || !toward.getWorld().equals(from.getWorld())) return new Direction(defaultForward ? 1 : -1, 0);
        int rawX = toward.getBlockX() - from.getBlockX();
        int rawZ = toward.getBlockZ() - from.getBlockZ();
        if (Math.abs(rawZ) > Math.abs(rawX)) return new Direction(0, rawZ >= 0 ? 1 : -1);
        return new Direction(rawX >= 0 ? 1 : -1, 0);
    }

    private static int sideX(Direction direction) {
        return direction.dz == 0 ? 0 : 1;
    }

    private static int sideZ(Direction direction) {
        return direction.dx == 0 ? 0 : 1;
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
            case "JUMP" -> new GamePalette(Material.LIME_WOOL, Material.RED_WOOL, Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.SEA_LANTERN, Material.LIGHT_BLUE_WOOL);
            case "WATER_JUMP" -> new GamePalette(Material.LIME_WOOL, Material.RED_WOOL, Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.SEA_LANTERN, Material.BLUE_WOOL);
            case "COURSE" -> new GamePalette(Material.LIME_CONCRETE, Material.RED_CONCRETE, Material.SMOOTH_STONE, Material.YELLOW_CONCRETE, Material.SEA_LANTERN, Material.GRAY_CONCRETE);
            case "LABYRINTHE" -> new GamePalette(Material.LIME_CONCRETE, Material.RED_CONCRETE, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.LANTERN, Material.CRACKED_STONE_BRICKS);
            case "RUEE_OR" -> new GamePalette(Material.GOLD_BLOCK, Material.RED_CONCRETE, Material.POLISHED_BLACKSTONE, Material.GOLD_BLOCK, Material.SEA_LANTERN, Material.POLISHED_BLACKSTONE_BRICKS);
            default -> new GamePalette(Material.LIME_CONCRETE, Material.RED_CONCRETE, Material.SMOOTH_STONE, Material.YELLOW_CONCRETE, Material.SEA_LANTERN, Material.GRAY_CONCRETE);
        };
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }

    private record GamePalette(Material start, Material finish, Material floor, Material path, Material light, Material wall) {
    }

    private record Direction(int dx, int dz) {
    }
}
