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

public final class GeneratedArenaDesigner {

    private static final Random RANDOM = new Random();
    private static final Material[] WOOL = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };
    private static final int[] LANES = {0, 2, 4, 3, 1, -1, -3, -4, -2, 0, 3, 1};
    private static final int[] HEIGHTS = {0, 1, 1, 2, 1, 0, 1, 2, 1, 0, -1, 0};

    private static boolean started;

    private GeneratedArenaDesigner() {
    }

    public static void startAutoEnhancer() {
        if (started) return;
        started = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                enhanceIfNeeded();
            }
        }.runTaskTimer(Main.getInstance(), 40L, 40L);
    }

    private static void enhanceIfNeeded() {
        File file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("active", false)) return;

        String typeName = config.getString("type", "");
        String key = typeName + ":" + config.getString("start.world", "") + ":" + config.getInt("start.x") + ":" + config.getInt("start.y") + ":" + config.getInt("start.z");
        if (key.equals(config.getString("v2-enhanced-key", ""))) return;

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
                config.getInt("region.min-x"), config.getInt("region.min-y"), config.getInt("region.min-z"),
                config.getInt("region.max-x"), config.getInt("region.max-y"), config.getInt("region.max-z")
        );
        Location start = readLocation(config, "start");
        Location finish = readLocation(config, "finish");
        if (start == null) return;

        switch (type) {
            case LABYRINTHE -> maze(region, start, finish);
            case JUMP -> jump(region, start, finish);
            case COURSE -> race(region, start, finish);
            case WATER_JUMP -> waterJump(region, start, finish);
            case SURVIE_ETAGES -> survival(region, start);
        }

        config.set("v2-enhanced-key", key);
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void maze(Region r, Location start, Location finish) {
        Palette p = randomPalette();
        boxWalls(r, 8, p.wall, p.pillar, p.light);
        floor(r, r.minY + 1, p.floor);

        int cx = (r.minX + r.maxX) / 2;
        int cz = (r.minZ + r.maxZ) / 2;
        for (int x = r.minX + 3; x <= r.maxX - 3; x++) {
            for (int z = r.minZ + 3; z <= r.maxZ - 3; z++) {
                boolean wall = (x % 4 == 0 && z % 3 != 0)
                        || (z % 5 == 0 && x % 3 != 0)
                        || ((x + z + RANDOM.nextInt(3)) % 17 == 0)
                        || x == r.minX + 3 || x == r.maxX - 3 || z == r.minZ + 3 || z == r.maxZ - 3;
                if (near(x, z, start, 4) || (finish != null && near(x, z, finish, 4))) wall = false;
                if (wall) column(r.world, x, r.minY + 2, z, 6, p.wall, p.pillar, p.light);
                else if ((Math.abs(x - cx) + Math.abs(z - cz)) % 19 == 0) r.world.getBlockAt(x, r.minY + 2, z).setType(Material.LANTERN, false);
            }
        }

        plaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
        if (finish != null) plaza(finish, Material.RED_CONCRETE, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);
    }

    private static void jump(Region r, Location start, Location finish) {
        linearArena(r, Material.DARK_OAK_FENCE, Material.SMOOTH_STONE, 7);
        floor(r, r.minY, Material.BLUE_CONCRETE);
        plaza(start, Material.LIME_WOOL, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);

        int x = start.getBlockX();
        int z = start.getBlockZ();
        int y = start.getBlockY();
        int maxX = finish == null ? r.maxX - 8 : finish.getBlockX() - 4;
        int i = 0;
        while (x < maxX) {
            i++;
            x += distance(i);
            z = clamp(start.getBlockZ() + LANES[i % LANES.length], r.minZ + 4, r.maxZ - 4);
            y = clamp(start.getBlockY() + HEIGHTS[i % HEIGHTS.length], start.getBlockY(), start.getBlockY() + 3);
            boolean checkpoint = i % 7 == 0;
            platform(r.world, x, y, z, checkpoint ? 2 : 1, checkpoint ? Material.GOLD_BLOCK : WOOL[i % WOOL.length]);
            support(r.world, x, r.minY + 1, y - 1, z, checkpoint ? Material.QUARTZ_PILLAR : Material.IRON_BARS);
            if (checkpoint) cornerFences(r.world, x, y + 1, z, 2);
            if (i % 5 == 0) sideStep(r.world, x + 1, y, clamp(z + 3, r.minZ + 4, r.maxZ - 4), WOOL[(i + 3) % WOOL.length]);
            if (i % 6 == 0) arch(r.world, x, y, z, Material.SEA_LANTERN);
        }
        if (finish != null) plaza(finish, Material.RED_WOOL, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);
    }

    private static void race(Region r, Location start, Location finish) {
        linearArena(r, Material.IRON_BARS, Material.SMOOTH_STONE, 5);
        for (int x = r.minX + 4; x <= r.maxX - 4; x++) {
            for (int z = r.minZ + 2; z <= r.maxZ - 2; z++) {
                r.world.getBlockAt(x, start.getBlockY() - 1, z).setType((x + z) % 2 == 0 ? Material.SMOOTH_STONE : Material.POLISHED_ANDESITE, false);
            }
        }
        for (int x = start.getBlockX() + 10; x < (finish == null ? r.maxX - 8 : finish.getBlockX()); x += 14) {
            for (int z = start.getBlockZ() - 2; z <= start.getBlockZ() + 2; z++) if (z != start.getBlockZ()) r.world.getBlockAt(x, start.getBlockY(), z).setType(Material.HAY_BLOCK, false);
            r.world.getBlockAt(x + 4, start.getBlockY(), start.getBlockZ()).setType(Material.OAK_FENCE, false);
            r.world.getBlockAt(x + 5, start.getBlockY(), start.getBlockZ()).setType(Material.OAK_FENCE, false);
        }
        plaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
        if (finish != null) plaza(finish, Material.RED_CONCRETE, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);
    }

    private static void waterJump(Region r, Location start, Location finish) {
        linearArena(r, Material.CYAN_STAINED_GLASS, Material.PRISMARINE_BRICKS, 6);
        for (int x = r.minX + 2; x <= r.maxX - 2; x++) {
            for (int z = r.minZ + 2; z <= r.maxZ - 2; z++) {
                r.world.getBlockAt(x, start.getBlockY() - 2, z).setType(Material.PRISMARINE_BRICKS, false);
                r.world.getBlockAt(x, start.getBlockY() - 1, z).setType(Material.WATER, false);
            }
        }
        plaza(start, Material.LIME_WOOL, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
        int x = start.getBlockX();
        int z = start.getBlockZ();
        int y = start.getBlockY();
        int maxX = finish == null ? r.maxX - 8 : finish.getBlockX() - 4;
        int i = 0;
        while (x < maxX) {
            i++;
            x += i % 4 == 0 ? 4 : 3;
            z = clamp(start.getBlockZ() + LANES[(i + 4) % LANES.length], r.minZ + 4, r.maxZ - 4);
            boolean checkpoint = i % 6 == 0;
            platform(r.world, x, y + (checkpoint ? 1 : 0), z, checkpoint ? 2 : 1, checkpoint ? Material.LIGHT_BLUE_CONCRETE : WOOL[(i + 2) % WOOL.length]);
            r.world.getBlockAt(x, y - 1, z).setType(Material.SEA_LANTERN, false);
            if (i % 4 == 0) sideStep(r.world, x + 1, y, clamp(z + 2, r.minZ + 4, r.maxZ - 4), WOOL[(i + 5) % WOOL.length]);
        }
        if (finish != null) plaza(finish, Material.RED_WOOL, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS);
    }

    private static void survival(Region r, Location start) {
        boxWalls(r, 7, Material.PURPLE_STAINED_GLASS, Material.AMETHYST_BLOCK, Material.SHROOMLIGHT);
        plaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
    }

    private static void linearArena(Region r, Material wall, Material base, int height) {
        for (int x = r.minX; x <= r.maxX; x++) {
            r.world.getBlockAt(x, r.minY + 1, r.minZ).setType(base, false);
            r.world.getBlockAt(x, r.minY + 1, r.maxZ).setType(base, false);
            for (int y = r.minY + 2; y <= r.minY + height; y++) {
                r.world.getBlockAt(x, y, r.minZ).setType(wall, false);
                r.world.getBlockAt(x, y, r.maxZ).setType(wall, false);
            }
            if ((x - r.minX) % 12 == 0) {
                r.world.getBlockAt(x, r.minY + height + 1, r.minZ).setType(Material.SEA_LANTERN, false);
                r.world.getBlockAt(x, r.minY + height + 1, r.maxZ).setType(Material.SEA_LANTERN, false);
            }
        }
        for (int z = r.minZ; z <= r.maxZ; z++) {
            for (int y = r.minY + 2; y <= r.minY + height; y++) {
                r.world.getBlockAt(r.minX, y, z).setType(wall, false);
                r.world.getBlockAt(r.maxX, y, z).setType(wall, false);
            }
        }
    }

    private static void boxWalls(Region r, int height, Material wall, Material pillar, Material light) {
        for (int x = r.minX; x <= r.maxX; x++) for (int y = r.minY + 1; y <= r.minY + height; y++) {
            r.world.getBlockAt(x, y, r.minZ).setType(wall, false);
            r.world.getBlockAt(x, y, r.maxZ).setType(wall, false);
        }
        for (int z = r.minZ; z <= r.maxZ; z++) for (int y = r.minY + 1; y <= r.minY + height; y++) {
            r.world.getBlockAt(r.minX, y, z).setType(wall, false);
            r.world.getBlockAt(r.maxX, y, z).setType(wall, false);
        }
        int[][] corners = {{r.minX, r.minZ}, {r.minX, r.maxZ}, {r.maxX, r.minZ}, {r.maxX, r.maxZ}};
        for (int[] c : corners) {
            for (int y = r.minY + 1; y <= r.minY + height + 1; y++) r.world.getBlockAt(c[0], y, c[1]).setType(pillar, false);
            r.world.getBlockAt(c[0], r.minY + height + 2, c[1]).setType(light, false);
        }
    }

    private static void floor(Region r, int y, Material material) {
        for (int x = r.minX + 1; x <= r.maxX - 1; x++) for (int z = r.minZ + 1; z <= r.maxZ - 1; z++) r.world.getBlockAt(x, y, z).setType(material, false);
    }

    private static void wallColumn(World w, int x, int y, int z, Material wall, Material pillar, Material light) {
        for (int dy = 0; dy < 6; dy++) w.getBlockAt(x, y + dy, z).setType(dy == 5 ? pillar : wall, false);
        if ((x + z) % 9 == 0) w.getBlockAt(x, y + 3, z).setType(light, false);
    }

    private static void plaza(Location loc, Material floor, Material pillar, Material glass) {
        World w = loc.getWorld();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY() - 1;
        int cz = loc.getBlockZ();
        platform(w, cx, cy, cz, 3, floor);
        for (int offset = -3; offset <= 3; offset += 6) {
            for (int y = cy + 1; y <= cy + 4; y++) {
                w.getBlockAt(cx, y, cz + offset).setType(pillar, false);
                w.getBlockAt(cx + offset, y, cz).setType(pillar, false);
            }
        }
        for (int z = cz - 3; z <= cz + 3; z++) w.getBlockAt(cx, cy + 4, z).setType(glass, false);
        for (int x = cx - 3; x <= cx + 3; x++) w.getBlockAt(x, cy + 4, cz).setType(glass, false);
    }

    private static void parkourPlatform(World w, int cx, int cy, int cz, int radius, Material material, boolean checkpoint) {
        platform(w, cx, cy, cz, radius, material);
        w.getBlockAt(cx, cy - 1, cz).setType(checkpoint ? Material.SEA_LANTERN : Material.IRON_BARS, false);
        if (checkpoint) cornerFences(w, cx, cy + 1, cz, radius);
    }

    private static void cornerFences(World w, int cx, int cy, int cz, int radius) {
        w.getBlockAt(cx - radius, cy, cz - radius).setType(Material.OAK_FENCE, false);
        w.getBlockAt(cx + radius, cy, cz - radius).setType(Material.OAK_FENCE, false);
        w.getBlockAt(cx - radius, cy, cz + radius).setType(Material.OAK_FENCE, false);
        w.getBlockAt(cx + radius, cy, cz + radius).setType(Material.OAK_FENCE, false);
    }

    private static void support(World w, int x, int fromY, int toY, int z, Material material) {
        for (int y = fromY; y <= toY; y++) w.getBlockAt(x, y, z).setType(material, false);
    }

    private static void sideStep(World w, int x, int y, int z, Material material) {
        w.getBlockAt(x, y, z).setType(material, false);
        w.getBlockAt(x, y - 1, z).setType(Material.IRON_BARS, false);
    }

    private static void arch(World w, int x, int y, int z, Material light) {
        w.getBlockAt(x, y + 2, z - 2).setType(Material.OAK_FENCE, false);
        w.getBlockAt(x, y + 2, z + 2).setType(Material.OAK_FENCE, false);
        w.getBlockAt(x, y + 3, z).setType(light, false);
    }

    private static void smallSideStep(World w, int x, int y, int z, Material material) {
        w.getBlockAt(x, y, z).setType(material, false);
        w.getBlockAt(x, y - 1, z).setType(Material.IRON_BARS, false);
    }

    private static void clearSpace(World w, int x, int y, int z, int height) {
        for (int dy = 0; dy < height; dy++) w.getBlockAt(x, y + dy, z).setType(Material.AIR, false);
    }

    private static void platform(World w, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) w.getBlockAt(x, cy, z).setType(material, false);
    }

    private static Palette randomPalette() {
        return switch (RANDOM.nextInt(5)) {
            case 0 -> new Palette(Material.CRACKED_STONE_BRICKS, Material.STONE_BRICKS, Material.CHISELED_STONE_BRICKS, Material.SEA_LANTERN);
            case 1 -> new Palette(Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES, Material.SOUL_LANTERN);
            case 2 -> new Palette(Material.MOSS_BLOCK, Material.OAK_PLANKS, Material.OAK_LOG, Material.GLOWSTONE);
            case 3 -> new Palette(Material.SMOOTH_QUARTZ, Material.QUARTZ_BLOCK, Material.QUARTZ_PILLAR, Material.SEA_LANTERN);
            default -> new Palette(Material.POLISHED_BLACKSTONE, Material.PURPUR_BLOCK, Material.AMETHYST_BLOCK, Material.SHROOMLIGHT);
        };
    }

    private static int distance(int i) {
        return i % 5 == 0 ? 3 : (i % 3 == 0 ? 4 : 3);
    }

    private static boolean near(int x, int z, Location loc, int radius) {
        return Math.abs(x - loc.getBlockX()) <= radius && Math.abs(z - loc.getBlockZ()) <= radius;
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

    private record Palette(Material floor, Material wall, Material pillar, Material light) {
    }
}
