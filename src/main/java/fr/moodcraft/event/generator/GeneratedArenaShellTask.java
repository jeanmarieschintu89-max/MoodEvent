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

public final class GeneratedArenaShellTask {

    private static final String VERSION = "arena-shell-v1";
    private static boolean started;

    private GeneratedArenaShellTask() {
    }

    public static void start() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                applyOnce();
            }
        }.runTaskTimer(Main.getInstance(), 80L, 120L);
    }

    private static void applyOnce() {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("active", false)) return;

        String type = config.getString("type", "UNKNOWN");
        World world = Bukkit.getWorld(config.getString("region.world", ""));
        if (world == null) return;

        int minX = config.getInt("region.min-x");
        int minY = config.getInt("region.min-y");
        int minZ = config.getInt("region.min-z");
        int maxX = config.getInt("region.max-x");
        int maxY = config.getInt("region.max-y");
        int maxZ = config.getInt("region.max-z");

        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        String key = VERSION + ":" + type + ":" + minX + ":" + minY + ":" + minZ + ":" + maxX + ":" + maxY + ":" + maxZ;
        if (key.equals(config.getString("arena-shell-key", ""))) return;

        buildFoundation(world, minX, minY, minZ, maxX, maxZ, type);
        buildSideWalls(world, minX, minY, minZ, maxX, maxY, maxZ, type, start, finish);
        buildEntranceFrames(world, minX, minY, minZ, maxX, maxY, maxZ, type, start, finish);

        config.set("arena-shell-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void buildFoundation(World world, int minX, int minY, int minZ, int maxX, int maxZ, String type) {
        Material main = switch (type) {
            case "JUMP" -> Material.BLUE_CONCRETE;
            case "WATER_JUMP" -> Material.PRISMARINE_BRICKS;
            case "COURSE" -> Material.POLISHED_ANDESITE;
            case "LABYRINTHE" -> Material.STONE_BRICKS;
            case "RUEE_OR" -> Material.POLISHED_BLACKSTONE;
            case "SURVIE_ETAGES" -> Material.BLACK_CONCRETE;
            default -> Material.SMOOTH_STONE;
        };
        Material accent = switch (type) {
            case "JUMP" -> Material.LIGHT_BLUE_CONCRETE;
            case "WATER_JUMP" -> Material.DARK_PRISMARINE;
            case "COURSE" -> Material.YELLOW_CONCRETE;
            case "LABYRINTHE" -> Material.MOSSY_STONE_BRICKS;
            case "RUEE_OR" -> Material.GOLD_BLOCK;
            case "SURVIE_ETAGES" -> Material.PURPLE_CONCRETE;
            default -> Material.POLISHED_ANDESITE;
        };

        int baseY = minY;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean border = x == minX || x == maxX || z == minZ || z == maxZ;
                boolean stripe = (x + z) % 9 == 0;
                world.getBlockAt(x, baseY, z).setType(border || stripe ? accent : main, false);
            }
        }
    }

    private static void buildSideWalls(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String type, Location start, Location finish) {
        Material wall = wallMaterial(type);
        Material light = lightMaterial(type);
        int wallTop = Math.min(maxY, minY + 7);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY + 1; y <= wallTop; y++) {
                if (!isGate(world, x, y, minZ, start, finish)) world.getBlockAt(x, y, minZ).setType(y % 5 == 0 ? light : wall, false);
                if (!isGate(world, x, y, maxZ, start, finish)) world.getBlockAt(x, y, maxZ).setType(y % 5 == 0 ? light : wall, false);
            }
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY + 1; y <= wallTop; y++) {
                if (!isGate(world, minX, y, z, start, finish)) world.getBlockAt(minX, y, z).setType(y % 5 == 0 ? light : wall, false);
                if (!isGate(world, maxX, y, z, start, finish)) world.getBlockAt(maxX, y, z).setType(y % 5 == 0 ? light : wall, false);
            }
        }
    }

    private static void buildEntranceFrames(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String type, Location start, Location finish) {
        if (start != null) buildFrame(world, clamp(start.getBlockX(), minX + 2, maxX - 2), minY + 1, clamp(start.getBlockZ(), minZ + 2, maxZ - 2), frameMaterial(type), lightMaterial(type));
        if (finish != null) buildFrame(world, clamp(finish.getBlockX(), minX + 2, maxX - 2), minY + 1, clamp(finish.getBlockZ(), minZ + 2, maxZ - 2), Material.RED_CONCRETE, lightMaterial(type));
    }

    private static void buildFrame(World world, int cx, int y, int cz, Material pillar, Material light) {
        for (int dy = 0; dy <= 3; dy++) {
            world.getBlockAt(cx - 4, y + dy, cz).setType(pillar, false);
            world.getBlockAt(cx + 4, y + dy, cz).setType(pillar, false);
        }
        for (int x = cx - 4; x <= cx + 4; x++) {
            if (Math.abs(x - cx) <= 1) continue;
            world.getBlockAt(x, y + 4, cz).setType(light, false);
        }
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                for (int dy = 0; dy <= 4; dy++) {
                    Material current = world.getBlockAt(x, y + dy, z).getType();
                    if (current.name().contains("FENCE") || current.name().contains("WALL") || current == Material.IRON_BARS) world.getBlockAt(x, y + dy, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static boolean isGate(World world, int x, int y, int z, Location start, Location finish) {
        return nearGate(x, y, z, start) || nearGate(x, y, z, finish);
    }

    private static boolean nearGate(int x, int y, int z, Location location) {
        if (location == null || location.getWorld() == null) return false;
        return Math.abs(x - location.getBlockX()) <= 4 && Math.abs(z - location.getBlockZ()) <= 4 && y <= location.getBlockY() + 5;
    }

    private static Material wallMaterial(String type) {
        return switch (type) {
            case "JUMP" -> Material.LIGHT_BLUE_STAINED_GLASS;
            case "WATER_JUMP" -> Material.CYAN_STAINED_GLASS;
            case "COURSE" -> Material.IRON_BARS;
            case "LABYRINTHE" -> Material.STONE_BRICKS;
            case "RUEE_OR" -> Material.POLISHED_BLACKSTONE_BRICKS;
            case "SURVIE_ETAGES" -> Material.PURPLE_STAINED_GLASS;
            default -> Material.GRAY_STAINED_GLASS;
        };
    }

    private static Material frameMaterial(String type) {
        return switch (type) {
            case "JUMP", "WATER_JUMP" -> Material.LIME_WOOL;
            case "RUEE_OR" -> Material.GOLD_BLOCK;
            default -> Material.LIME_CONCRETE;
        };
    }

    private static Material lightMaterial(String type) {
        return switch (type) {
            case "RUEE_OR" -> Material.SHROOMLIGHT;
            case "LABYRINTHE" -> Material.LANTERN;
            default -> Material.SEA_LANTERN;
        };
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
