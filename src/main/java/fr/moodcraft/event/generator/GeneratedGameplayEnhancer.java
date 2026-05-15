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
import java.util.Random;

public final class GeneratedGameplayEnhancer {

    private static final Random RANDOM = new Random();
    private static final Material[] WOOL = {
            Material.WHITE_WOOL,
            Material.YELLOW_WOOL,
            Material.ORANGE_WOOL,
            Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.CYAN_WOOL,
            Material.MAGENTA_WOOL,
            Material.PINK_WOOL
    };
    private static final int[] LANES = {0, 2, 4, 3, 1, -1, -3, -4, -2, 0, 3, 1};

    private static boolean started;

    private GeneratedGameplayEnhancer() {
    }

    public static void start() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                enhanceOnce();
            }
        }.runTaskTimer(Main.getInstance(), 80L, 120L);
    }

    private static void enhanceOnce() {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("active", false)) return;

        String typeName = config.getString("type", "");
        String key = typeName + ":" + config.getString("start.world", "") + ":" + config.getInt("start.x") + ":" + config.getInt("start.y") + ":" + config.getInt("start.z");
        if (key.equals(config.getString("gameplay-enhanced-key", ""))) return;

        GeneratedGameType type;
        try {
            type = GeneratedGameType.valueOf(typeName);
        } catch (IllegalArgumentException exception) {
            return;
        }

        World world = Bukkit.getWorld(config.getString("region.world", ""));
        if (world == null) return;
        Region region = new Region(
                world,
                config.getInt("region.min-x"),
                config.getInt("region.min-y"),
                config.getInt("region.min-z"),
                config.getInt("region.max-x"),
                config.getInt("region.max-y"),
                config.getInt("region.max-z")
        );
        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        if (start == null) return;

        switch (type) {
            case COURSE -> enhanceRace(region, start, finish);
            case JUMP -> enhanceJump(region, start, finish);
            case WATER_JUMP -> enhanceWaterJump(region, start, finish);
            case LABYRINTHE -> enhanceMaze(region, start, finish);
            default -> {
                return;
            }
        }

        config.set("gameplay-enhanced-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void enhanceRace(Region r, Location start, Location finish) {
        int y = start.getBlockY();
        int z = start.getBlockZ();
        int endX = finish == null ? r.maxX - 8 : finish.getBlockX();

        for (int x = start.getBlockX() + 8; x < endX - 4; x += 8) {
            int module = Math.abs(x / 8) % 5;
            switch (module) {
                case 0 -> hurdle(r.world, x, y, z);
                case 1 -> slalom(r.world, x, y, z);
                case 2 -> lowWall(r.world, x, y, z);
                case 3 -> laneChoice(r.world, x, y, z);
                default -> speedStrip(r.world, x, y - 1, z);
            }
        }

        startPodium(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK);
        if (finish != null) finishPodium(finish, Material.RED_CONCRETE, Material.REDSTONE_BLOCK);
    }

    private static void enhanceJump(Region r, Location start, Location finish) {
        int y = start.getBlockY();
        int baseZ = start.getBlockZ();
        int endX = finish == null ? r.maxX - 8 : finish.getBlockX() - 4;
        int x = start.getBlockX() + 6;
        int step = 0;

        while (x < endX) {
            step++;
            int z = clamp(baseZ + LANES[step % LANES.length], r.minZ + 4, r.maxZ - 4);
            int py = y + ((step % 6 == 0) ? 2 : (step % 3 == 0 ? 1 : 0));
            boolean checkpoint = step % 7 == 0;
            platform(r.world, x, py, z, checkpoint ? 2 : 1, checkpoint ? Material.GOLD_BLOCK : WOOL[step % WOOL.length]);
            support(r.world, x, r.minY + 1, py - 1, z, checkpoint ? Material.QUARTZ_PILLAR : Material.IRON_BARS);
            if (checkpoint) ring(r.world, x, py + 1, z, 2, Material.OAK_FENCE);
            if (step % 5 == 0) sidePlatform(r.world, x + 2, py, clamp(z + 3, r.minZ + 4, r.maxZ - 4), WOOL[(step + 2) % WOOL.length]);
            if (step % 6 == 0) glassArch(r.world, x, py, z, Material.YELLOW_STAINED_GLASS);
            x += step % 4 == 0 ? 4 : 3;
        }

        startPodium(start, Material.LIME_WOOL, Material.EMERALD_BLOCK);
        if (finish != null) finishPodium(finish, Material.RED_WOOL, Material.REDSTONE_BLOCK);
    }

    private static void enhanceWaterJump(Region r, Location start, Location finish) {
        int y = start.getBlockY();
        int baseZ = start.getBlockZ();
        int endX = finish == null ? r.maxX - 8 : finish.getBlockX() - 4;
        int x = start.getBlockX() + 5;
        int step = 0;

        while (x < endX) {
            step++;
            int z = clamp(baseZ + LANES[(step + 3) % LANES.length], r.minZ + 4, r.maxZ - 4);
            boolean checkpoint = step % 6 == 0;
            platform(r.world, x, y + (checkpoint ? 1 : 0), z, checkpoint ? 2 : 1, checkpoint ? Material.LIGHT_BLUE_CONCRETE : WOOL[(step + 3) % WOOL.length]);
            r.world.getBlockAt(x, y - 1, z).setType(Material.SEA_LANTERN, false);
            if (step % 4 == 0) waterGate(r.world, x + 2, y + 1, baseZ);
            if (step % 5 == 0) buoy(r.world, x + 3, y - 1, clamp(z + 3, r.minZ + 4, r.maxZ - 4));
            x += step % 4 == 0 ? 4 : 3;
        }

        startPodium(start, Material.LIME_WOOL, Material.EMERALD_BLOCK);
        if (finish != null) finishPodium(finish, Material.RED_WOOL, Material.REDSTONE_BLOCK);
    }

    private static void enhanceMaze(Region r, Location start, Location finish) {
        startPodium(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK);
        if (finish != null) finishPodium(finish, Material.RED_CONCRETE, Material.REDSTONE_BLOCK);
        for (int x = r.minX + 5; x <= r.maxX - 5; x += 8) {
            for (int z = r.minZ + 5; z <= r.maxZ - 5; z += 8) {
                if (RANDOM.nextBoolean()) decorativeColumn(r.world, x, start.getBlockY(), z);
            }
        }
    }

    private static void hurdle(World w, int x, int y, int z) {
        for (int dz = -2; dz <= 2; dz++) {
            if (dz == 0) continue;
            w.getBlockAt(x, y, z + dz).setType(Material.HAY_BLOCK, false);
        }
    }

    private static void slalom(World w, int x, int y, int z) {
        w.getBlockAt(x, y, z - 2).setType(Material.OAK_FENCE, false);
        w.getBlockAt(x + 2, y, z + 2).setType(Material.OAK_FENCE, false);
        w.getBlockAt(x + 4, y, z - 2).setType(Material.OAK_FENCE, false);
    }

    private static void lowWall(World w, int x, int y, int z) {
        for (int dz = -2; dz <= 2; dz++) {
            if (dz == -1 || dz == 1) continue;
            w.getBlockAt(x, y, z + dz).setType(Material.SPRUCE_TRAPDOOR, false);
        }
    }

    private static void laneChoice(World w, int x, int y, int z) {
        platform(w, x, y, z - 2, 1, Material.ORANGE_CONCRETE);
        platform(w, x, y, z + 2, 1, Material.LIGHT_BLUE_CONCRETE);
        w.getBlockAt(x + 2, y + 1, z).setType(Material.OAK_FENCE, false);
    }

    private static void speedStrip(World w, int x, int y, int z) {
        for (int dx = 0; dx <= 4; dx++) {
            w.getBlockAt(x + dx, y, z - 1).setType(Material.YELLOW_CONCRETE, false);
            w.getBlockAt(x + dx, y, z + 1).setType(Material.YELLOW_CONCRETE, false);
        }
    }

    private static void waterGate(World w, int x, int y, int z) {
        for (int dy = 0; dy <= 2; dy++) {
            w.getBlockAt(x, y + dy, z - 4).setType(Material.PRISMARINE_WALL, false);
            w.getBlockAt(x, y + dy, z + 4).setType(Material.PRISMARINE_WALL, false);
        }
        w.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
    }

    private static void buoy(World w, int x, int y, int z) {
        w.getBlockAt(x, y, z).setType(Material.RED_WOOL, false);
        w.getBlockAt(x, y + 1, z).setType(Material.WHITE_WOOL, false);
    }

    private static void sidePlatform(World w, int x, int y, int z, Material material) {
        platform(w, x, y, z, 0, material);
        w.getBlockAt(x, y - 1, z).setType(Material.IRON_BARS, false);
    }

    private static void glassArch(World w, int x, int y, int z, Material glass) {
        w.getBlockAt(x, y + 2, z - 3).setType(glass, false);
        w.getBlockAt(x, y + 2, z + 3).setType(glass, false);
        for (int dz = -3; dz <= 3; dz++) w.getBlockAt(x, y + 4, z + dz).setType(glass, false);
    }

    private static void startPodium(Location loc, Material floor, Material pillar) {
        World w = loc.getWorld();
        if (w == null) return;
        int cx = loc.getBlockX();
        int cy = loc.getBlockY() - 1;
        int cz = loc.getBlockZ();
        platform(w, cx, cy, cz, 4, floor);
        ring(w, cx, cy + 1, cz, 4, Material.OAK_FENCE);
        arch(w, cx, cy, cz, pillar, Material.LIME_STAINED_GLASS);
        w.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void finishPodium(Location loc, Material floor, Material pillar) {
        World w = loc.getWorld();
        if (w == null) return;
        int cx = loc.getBlockX();
        int cy = loc.getBlockY() - 1;
        int cz = loc.getBlockZ();
        platform(w, cx, cy, cz, 4, floor);
        platform(w, cx, cy + 1, cz, 2, Material.WHITE_WOOL);
        ring(w, cx, cy + 1, cz, 4, Material.OAK_FENCE);
        arch(w, cx, cy, cz, pillar, Material.RED_STAINED_GLASS);
        w.getBlockAt(cx, cy + 1, cz).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void decorativeColumn(World w, int x, int y, int z) {
        for (int dy = 0; dy <= 3; dy++) w.getBlockAt(x, y + dy, z).setType(Material.MOSSY_STONE_BRICKS, false);
        w.getBlockAt(x, y + 4, z).setType(Material.LANTERN, false);
    }

    private static void support(World w, int x, int fromY, int toY, int z, Material material) {
        if (toY < fromY) return;
        for (int y = fromY; y <= toY; y++) w.getBlockAt(x, y, z).setType(material, false);
    }

    private static void arch(World w, int cx, int cy, int cz, Material pillar, Material glass) {
        for (int y = cy + 1; y <= cy + 4; y++) {
            w.getBlockAt(cx - 4, y, cz).setType(pillar, false);
            w.getBlockAt(cx + 4, y, cz).setType(pillar, false);
        }
        for (int x = cx - 4; x <= cx + 4; x++) w.getBlockAt(x, cy + 4, cz).setType(glass, false);
        w.getBlockAt(cx, cy + 5, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void ring(World w, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            w.getBlockAt(x, cy, cz - radius).setType(material, false);
            w.getBlockAt(x, cy, cz + radius).setType(material, false);
        }
        for (int z = cz - radius; z <= cz + radius; z++) {
            w.getBlockAt(cx - radius, cy, z).setType(material, false);
            w.getBlockAt(cx + radius, cy, z).setType(material, false);
        }
    }

    private static void platform(World w, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) w.getBlockAt(x, cy, z).setType(material, false);
        }
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Region(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
