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

public final class GeneratedMiniGamePolisher {

    private static final Random RANDOM = new Random();
    private static boolean started;
    private static long lastCheck;

    private GeneratedMiniGamePolisher() {
    }

    public static void startAutoPolisher() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                polishIfNeeded();
            }
        }.runTaskTimer(Main.getInstance(), 160L, 260L);
    }

    private static void polishIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < 12000L) return;
        lastCheck = now;

        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("active", false)) return;

        String key = config.getString("type", "") + ":" + config.getString("start.world", "") + ":" + config.getInt("start.x") + ":" + config.getInt("start.y") + ":" + config.getInt("start.z");
        if (key.equals(config.getString("v3-polished-key", ""))) return;

        GeneratedGameType type;
        try {
            type = GeneratedGameType.valueOf(config.getString("type", ""));
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
            case LABYRINTHE -> polishMaze(region, start, finish);
            case JUMP -> polishJump(region, start, finish);
            case COURSE -> polishRace(region, start, finish);
            case WATER_JUMP -> polishWaterJump(region, start, finish);
            case SURVIE_ETAGES -> polishSurvival(region, start);
            case RUEE_OR -> polishGoldRush(region, start);
        }

        config.set("v3-polished-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void polishMaze(Region r, Location start, Location finish) {
        cornerTowers(r, Material.CHISELED_STONE_BRICKS, Material.SEA_LANTERN);
        gatePlaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS, true);
        if (finish != null) gatePlaza(finish, Material.RED_CONCRETE, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS, false);

        int y = start.getBlockY();
        int centerX = (r.minX + r.maxX) / 2;
        int centerZ = (r.minZ + r.maxZ) / 2;
        for (int x = r.minX + 5; x <= r.maxX - 5; x += 7) {
            for (int z = r.minZ + 5; z <= r.maxZ - 5; z += 7) {
                if (Math.abs(x - centerX) + Math.abs(z - centerZ) > 6 && RANDOM.nextBoolean()) {
                    decorativeColumn(r.world, x, y, z, Material.MOSSY_STONE_BRICKS, Material.LANTERN);
                }
            }
        }
    }

    private static void polishJump(Region r, Location start, Location finish) {
        runwayFrame(r, Material.LIGHT_BLUE_STAINED_GLASS, Material.SEA_LANTERN);
        gatePlaza(start, Material.LIME_WOOL, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS, true);
        if (finish != null) finishPodium(finish, Material.RED_WOOL, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);

        int y = start.getBlockY();
        int z = start.getBlockZ();
        int endX = finish == null ? r.maxX - 8 : finish.getBlockX() - 4;
        int module = 0;
        for (int x = start.getBlockX() + 12; x < endX; x += 18) {
            module++;
            if (module % 3 == 1) jumpRing(r.world, x, y + 2, z, Material.YELLOW_STAINED_GLASS);
            if (module % 3 == 2) checkpointIsland(r.world, x, y + 1, z + ((module % 2 == 0) ? 3 : -3));
            if (module % 3 == 0) archBridge(r.world, x, y, z, Material.CYAN_WOOL, Material.SEA_LANTERN);
        }
    }

    private static void polishRace(Region r, Location start, Location finish) {
        runwayFrame(r, Material.IRON_BARS, Material.SEA_LANTERN);
        gatePlaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS, true);
        if (finish != null) finishPodium(finish, Material.RED_CONCRETE, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);

        int y = start.getBlockY();
        int z = start.getBlockZ();
        int endX = finish == null ? r.maxX - 8 : finish.getBlockX();
        for (int x = start.getBlockX() + 14; x < endX; x += 20) {
            hurdleWall(r.world, x, y, z);
            laneMarkers(r.world, x + 8, y - 1, z);
        }
    }

    private static void polishWaterJump(Region r, Location start, Location finish) {
        runwayFrame(r, Material.CYAN_STAINED_GLASS, Material.SEA_LANTERN);
        gatePlaza(start, Material.LIME_WOOL, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS, true);
        if (finish != null) finishPodium(finish, Material.RED_WOOL, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);

        int y = start.getBlockY();
        int z = start.getBlockZ();
        int endX = finish == null ? r.maxX - 8 : finish.getBlockX();
        for (int x = start.getBlockX() + 10; x < endX; x += 15) {
            waterGate(r.world, x, y + 1, z, Material.PRISMARINE_WALL, Material.SEA_LANTERN);
            buoy(r.world, x + 5, y, z + (RANDOM.nextBoolean() ? 4 : -4));
        }
    }

    private static void polishSurvival(Region r, Location start) {
        cornerTowers(r, Material.AMETHYST_BLOCK, Material.SHROOMLIGHT);
        gatePlaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS, true);
        int cx = start.getBlockX();
        int cz = start.getBlockZ();
        for (int y = r.minY + 4; y <= r.maxY - 3; y += 5) {
            warningRing(r.world, cx, y, cz, 5, Material.RED_STAINED_GLASS);
        }
    }

    private static void polishGoldRush(Region r, Location start) {
        cornerTowers(r, Material.BEDROCK, Material.GOLD_BLOCK);
        gatePlaza(start, Material.GOLD_BLOCK, Material.EMERALD_BLOCK, Material.YELLOW_STAINED_GLASS, true);
        int y = start.getBlockY();
        for (int x = r.minX + 4; x <= r.maxX - 4; x += 7) {
            oreVein(r.world, x, y + 2, r.minZ + 3);
            oreVein(r.world, x, y + 2, r.maxZ - 3);
        }
    }

    private static void gatePlaza(Location loc, Material floor, Material pillar, Material glass, boolean start) {
        World w = loc.getWorld();
        if (w == null) return;
        int cx = loc.getBlockX();
        int cy = loc.getBlockY() - 1;
        int cz = loc.getBlockZ();
        square(w, cx, cy, cz, 5, floor);
        square(w, cx, cy + 1, cz, 2, Material.WHITE_WOOL);
        ring(w, cx, cy + 1, cz, 5, Material.OAK_FENCE);
        for (int y = cy + 1; y <= cy + 5; y++) {
            w.getBlockAt(cx - 4, y, cz).setType(pillar, false);
            w.getBlockAt(cx + 4, y, cz).setType(pillar, false);
        }
        for (int x = cx - 4; x <= cx + 4; x++) w.getBlockAt(x, cy + 5, cz).setType(glass, false);
        w.getBlockAt(cx, cy + 6, cz).setType(Material.SEA_LANTERN, false);
        w.getBlockAt(cx, cy + 1, cz).setType(start ? Material.LIGHT_WEIGHTED_PRESSURE_PLATE : Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void finishPodium(Location loc, Material floor, Material pillar, Material glass) {
        gatePlaza(loc, floor, pillar, glass, false);
        World w = loc.getWorld();
        if (w == null) return;
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();
        square(w, cx, cy + 1, cz, 1, Material.RED_WOOL);
        w.getBlockAt(cx, cy + 2, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void runwayFrame(Region r, Material rail, Material light) {
        for (int x = r.minX + 2; x <= r.maxX - 2; x += 8) {
            r.world.getBlockAt(x, r.minY + 2, r.minZ + 1).setType(light, false);
            r.world.getBlockAt(x, r.minY + 2, r.maxZ - 1).setType(light, false);
        }
        for (int z = r.minZ + 1; z <= r.maxZ - 1; z++) {
            r.world.getBlockAt(r.minX + 1, r.minY + 2, z).setType(rail, false);
            r.world.getBlockAt(r.maxX - 1, r.minY + 2, z).setType(rail, false);
        }
    }

    private static void cornerTowers(Region r, Material pillar, Material light) {
        int[][] corners = {{r.minX, r.minZ}, {r.minX, r.maxZ}, {r.maxX, r.minZ}, {r.maxX, r.maxZ}};
        for (int[] c : corners) {
            for (int y = r.minY + 1; y <= Math.min(r.maxY, r.minY + 8); y++) r.world.getBlockAt(c[0], y, c[1]).setType(pillar, false);
            r.world.getBlockAt(c[0], Math.min(r.maxY, r.minY + 9), c[1]).setType(light, false);
        }
    }

    private static void decorativeColumn(World w, int x, int y, int z, Material block, Material lamp) {
        for (int dy = 0; dy <= 3; dy++) w.getBlockAt(x, y + dy, z).setType(block, false);
        w.getBlockAt(x, y + 4, z).setType(lamp, false);
    }

    private static void jumpRing(World w, int x, int y, int z, Material glass) {
        for (int dy = 0; dy <= 3; dy++) {
            w.getBlockAt(x, y + dy, z - 3).setType(glass, false);
            w.getBlockAt(x, y + dy, z + 3).setType(glass, false);
        }
        for (int dz = -3; dz <= 3; dz++) w.getBlockAt(x, y + 3, z + dz).setType(glass, false);
    }

    private static void checkpointIsland(World w, int x, int y, int z) {
        square(w, x, y, z, 2, Material.GOLD_BLOCK);
        ring(w, x, y + 1, z, 2, Material.OAK_FENCE);
        w.getBlockAt(x, y + 1, z).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void archBridge(World w, int x, int y, int z, Material block, Material light) {
        for (int dz = -2; dz <= 2; dz++) w.getBlockAt(x, y, z + dz).setType(block, false);
        w.getBlockAt(x, y + 2, z - 2).setType(Material.OAK_FENCE, false);
        w.getBlockAt(x, y + 2, z + 2).setType(Material.OAK_FENCE, false);
        w.getBlockAt(x, y + 3, z).setType(light, false);
    }

    private static void hurdleWall(World w, int x, int y, int z) {
        for (int dz = -2; dz <= 2; dz++) if (dz != 0) w.getBlockAt(x, y, z + dz).setType(Material.HAY_BLOCK, false);
    }

    private static void laneMarkers(World w, int x, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            w.getBlockAt(x + dx, y, z - 2).setType(Material.YELLOW_CONCRETE, false);
            w.getBlockAt(x + dx, y, z + 2).setType(Material.YELLOW_CONCRETE, false);
        }
    }

    private static void waterGate(World w, int x, int y, int z, Material wall, Material light) {
        w.getBlockAt(x, y, z - 3).setType(wall, false);
        w.getBlockAt(x, y, z + 3).setType(wall, false);
        w.getBlockAt(x, y + 1, z - 3).setType(light, false);
        w.getBlockAt(x, y + 1, z + 3).setType(light, false);
    }

    private static void buoy(World w, int x, int y, int z) {
        w.getBlockAt(x, y, z).setType(Material.RED_WOOL, false);
        w.getBlockAt(x, y + 1, z).setType(Material.WHITE_WOOL, false);
    }

    private static void warningRing(World w, int cx, int y, int cz, int radius, Material glass) {
        ring(w, cx, y, cz, radius, glass);
    }

    private static void oreVein(World w, int x, int y, int z) {
        Material ore = RANDOM.nextBoolean() ? Material.GOLD_ORE : Material.IRON_ORE;
        for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy <= 1; dy++) w.getBlockAt(x + dx, y + dy, z).setType(ore, false);
    }

    private static void square(World w, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) w.getBlockAt(x, cy, z).setType(material, false);
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"), (float) config.getDouble(path + ".yaw"), (float) config.getDouble(path + ".pitch"));
    }

    private record Region(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
