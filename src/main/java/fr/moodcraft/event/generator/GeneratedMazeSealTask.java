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

public final class GeneratedMazeSealTask {

    private static final String VERSION = "maze-seal-v1";
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
        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        if (world == null || start == null || finish == null) return;

        String key = VERSION + ":" + world.getName() + ":" + start.getBlockX() + ":" + start.getBlockY() + ":" + start.getBlockZ();
        if (key.equals(config.getString("maze-seal-key", ""))) return;

        int minX = config.getInt("region.min-x") + 2;
        int maxX = config.getInt("region.max-x") - 2;
        int minZ = config.getInt("region.min-z") + 2;
        int maxZ = config.getInt("region.max-z") - 2;
        int baseY = start.getBlockY() - 1;

        sealOuterRing(world, minX, maxX, minZ, maxZ, baseY);
        buildInnerStart(world, start, Material.LIME_CONCRETE);
        buildInnerStart(world, finish, Material.RED_CONCRETE);

        config.set("maze-seal-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
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

    private static void buildInnerStart(World world, Location location, Material marker) {
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();
        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                world.getBlockAt(x, cy, z).setType(marker, false);
                for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
        for (int y = cy + 1; y <= cy + 4; y++) {
            world.getBlockAt(cx - 3, y, cz).setType(Material.CHISELED_STONE_BRICKS, false);
            world.getBlockAt(cx + 3, y, cz).setType(Material.CHISELED_STONE_BRICKS, false);
        }
        world.getBlockAt(cx, cy + 4, cz).setType(Material.LANTERN, false);
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }
}
