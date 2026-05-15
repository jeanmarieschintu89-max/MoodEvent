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
        Palette palette = randomPalette();
        boxWalls(r, 8, palette.wall(), palette.pillar(), palette.light());
        floor(r, r.minY() + 1, palette.floor());

        int centerX = (r.minX() + r.maxX()) / 2;
        int centerZ = (r.minZ() + r.maxZ()) / 2;

        for (int x = r.minX() + 3; x <= r.maxX() - 3; x++) {
            for (int z = r.minZ() + 3; z <= r.maxZ() - 3; z++) {
                boolean wall = (x % 4 == 0 && z % 3 != 0)
                        || (z % 5 == 0 && x % 3 != 0)
                        || ((x + z + RANDOM.nextInt(4)) % 17 == 0)
                        || x == r.minX() + 3
                        || x == r.maxX() - 3
                        || z == r.minZ() + 3
                        || z == r.maxZ() - 3;

                if (near(x, z, start, 4) || (finish != null && near(x, z, finish, 5))) {
                    wall = false;
                }

                if (wall) {
                    wallColumn(r.world(), x, r.minY() + 2, z, 6, palette.wall(), palette.pillar(), palette.light());
                } else if ((Math.abs(x - centerX) + Math.abs(z - centerZ)) % 19 == 0) {
                    r.world().getBlockAt(x, r.minY() + 2, z).setType(Material.LANTERN, false);
                }
            }
        }

        plaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
        if (finish != null) finishZone(finish, false);
    }

    private static void jump(Region r, Location start, Location finish) {
        int variant = RANDOM.nextInt(4);
        Material wall = switch (variant) {
            case 0 -> Material.DARK_OAK_FENCE;
            case 1 -> Material.SPRUCE_FENCE;
            case 2 -> Material.LIGHT_BLUE_STAINED_GLASS;
            default -> Material.IRON_BARS;
        };

        linearArena(r, wall, Material.SMOOTH_STONE, 7);
        floor(r, r.minY(), variant == 2 ? Material.CYAN_CONCRETE : Material.BLUE_CONCRETE);
        plaza(start, Material.LIME_WOOL, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);

        int x = start.getBlockX();
        int z = start.getBlockZ();
        int y = start.getBlockY();
        int maxX = finish == null ? r.maxX() - 8 : finish.getBlockX() - 5;
        int step = 0;

        while (x < maxX) {
            step++;
            x += distance(step);
            z = clamp(start.getBlockZ() + LANES[(step + variant) % LANES.length], r.minZ() + 4, r.maxZ() - 4);
            y = clamp(start.getBlockY() + HEIGHTS[(step + variant) % HEIGHTS.length], start.getBlockY(), start.getBlockY() + 3);

            boolean checkpoint = step % 7 == 0;
            Material material = checkpoint ? Material.GOLD_BLOCK : WOOL[(step + variant) % WOOL.length];
            parkourPlatform(r.world(), x, y, z, checkpoint ? 2 : 1, material, checkpoint);
            support(r.world(), x, r.minY() + 1, y - 1, z, checkpoint ? Material.QUARTZ_PILLAR : Material.IRON_BARS);

            if (step % 5 == 0) sideStep(r.world(), x + 1, y, clamp(z + 3, r.minZ() + 4, r.maxZ() - 4), WOOL[(step + 3) % WOOL.length]);
            if (step % 6 == 0) arch(r.world(), x, y, z, Material.SEA_LANTERN);
            if (step % 9 == 0) miniBridge(r.world(), x, y, z, variant);
        }

        if (finish != null) finishZone(finish, true);
    }

    private static void race(Region r, Location start, Location finish) {
        linearArena(r, Material.IRON_BARS, Material.SMOOTH_STONE, 5);

        for (int x = r.minX() + 4; x <= r.maxX() - 4; x++) {
            for (int z = r.minZ() + 2; z <= r.maxZ() - 2; z++) {
                r.world().getBlockAt(x, start.getBlockY() - 1, z).setType((x + z) % 2 == 0 ? Material.SMOOTH_STONE : Material.POLISHED_ANDESITE, false);
            }
        }

        int end = finish == null ? r.maxX() - 8 : finish.getBlockX();
        int variant = RANDOM.nextInt(3);
        for (int x = start.getBlockX() + 10; x < end; x += 12 + variant) {
            for (int z = start.getBlockZ() - 2; z <= start.getBlockZ() + 2; z++) {
                if (z != start.getBlockZ()) r.world().getBlockAt(x, start.getBlockY(), z).setType(Material.HAY_BLOCK, false);
            }
            r.world().getBlockAt(x + 4, start.getBlockY(), start.getBlockZ()).setType(Material.OAK_FENCE, false);
            r.world().getBlockAt(x + 5, start.getBlockY(), start.getBlockZ()).setType(Material.OAK_FENCE, false);
            if (variant == 2) {
                r.world().getBlockAt(x + 7, start.getBlockY(), start.getBlockZ() - 2).setType(Material.SLIME_BLOCK, false);
                r.world().getBlockAt(x + 7, start.getBlockY(), start.getBlockZ() + 2).setType(Material.SLIME_BLOCK, false);
            }
        }

        plaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
        if (finish != null) finishZone(finish, false);
    }

    private static void waterJump(Region r, Location start, Location finish) {
        int variant = RANDOM.nextInt(4);
        linearArena(r, Material.CYAN_STAINED_GLASS, Material.PRISMARINE_BRICKS, 6);

        for (int x = r.minX() + 2; x <= r.maxX() - 2; x++) {
            for (int z = r.minZ() + 2; z <= r.maxZ() - 2; z++) {
                r.world().getBlockAt(x, start.getBlockY() - 2, z).setType(Material.PRISMARINE_BRICKS, false);
                r.world().getBlockAt(x, start.getBlockY() - 1, z).setType(Material.WATER, false);
            }
        }

        plaza(start, Material.LIME_WOOL, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);

        int x = start.getBlockX();
        int z = start.getBlockZ();
        int y = start.getBlockY();
        int maxX = finish == null ? r.maxX() - 8 : finish.getBlockX() - 5;
        int step = 0;

        while (x < maxX) {
            step++;
            x += step % 4 == 0 ? 4 : 3;
            z = clamp(start.getBlockZ() + LANES[(step + variant + 4) % LANES.length], r.minZ() + 4, r.maxZ() - 4);

            boolean checkpoint = step % 6 == 0;
            int platformY = y + (checkpoint ? 1 : 0);
            platform(r.world(), x, platformY, z, checkpoint ? 2 : 1, checkpoint ? Material.LIGHT_BLUE_CONCRETE : WOOL[(step + 2 + variant) % WOOL.length]);
            r.world().getBlockAt(x, platformY - 1, z).setType(Material.SEA_LANTERN, false);

            if (step % 4 == 0) sideStep(r.world(), x + 1, y, clamp(z + 2, r.minZ() + 4, r.maxZ() - 4), WOOL[(step + 5) % WOOL.length]);
            if (step % 5 == 0) waterObstacle(r.world(), x + 2, y, z, variant);
        }

        if (finish != null) finishZone(finish, true);
    }

    private static void survival(Region r, Location start) {
        boxWalls(r, 7, Material.PURPLE_STAINED_GLASS, Material.AMETHYST_BLOCK, Material.SHROOMLIGHT);
        plaza(start, Material.LIME_CONCRETE, Material.EMERALD_BLOCK, Material.LIME_STAINED_GLASS);
    }

    private static void finishZone(Location location, boolean woolStyle) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();

        Material base = woolStyle ? Material.RED_WOOL : Material.RED_CONCRETE;
        platform(world, cx, cy, cz, 4, base);
        platform(world, cx, cy + 1, cz, 2, Material.WHITE_WOOL);
        platform(world, cx, cy + 2, cz, 1, Material.RED_WOOL);

        for (int x = cx - 4; x <= cx + 4; x++) {
            world.getBlockAt(x, cy + 1, cz - 4).setType(Material.OAK_FENCE, false);
            world.getBlockAt(x, cy + 1, cz + 4).setType(Material.OAK_FENCE, false);
        }
        for (int z = cz - 4; z <= cz + 4; z++) {
            world.getBlockAt(cx - 4, cy + 1, z).setType(Material.OAK_FENCE, false);
            world.getBlockAt(cx + 4, cy + 1, z).setType(Material.OAK_FENCE, false);
        }

        for (int y = cy + 1; y <= cy + 5; y++) {
            world.getBlockAt(cx - 4, y, cz).setType(Material.REDSTONE_BLOCK, false);
            world.getBlockAt(cx + 4, y, cz).setType(Material.REDSTONE_BLOCK, false);
        }
        for (int x = cx - 4; x <= cx + 4; x++) world.getBlockAt(x, cy + 5, cz).setType(Material.RED_STAINED_GLASS, false);
        world.getBlockAt(cx, cy + 6, cz).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void linearArena(Region r, Material wall, Material base, int height) {
        for (int x = r.minX(); x <= r.maxX(); x++) {
            r.world().getBlockAt(x, r.minY() + 1, r.minZ()).setType(base, false);
            r.world().getBlockAt(x, r.minY() + 1, r.maxZ()).setType(base, false);
            for (int y = r.minY() + 2; y <= r.minY() + height; y++) {
                r.world().getBlockAt(x, y, r.minZ()).setType(wall, false);
                r.world().getBlockAt(x, y, r.maxZ()).setType(wall, false);
            }
            if ((x - r.minX()) % 12 == 0) {
                r.world().getBlockAt(x, r.minY() + height + 1, r.minZ()).setType(Material.SEA_LANTERN, false);
                r.world().getBlockAt(x, r.minY() + height + 1, r.maxZ()).setType(Material.SEA_LANTERN, false);
            }
        }
        for (int z = r.minZ(); z <= r.maxZ(); z++) {
            for (int y = r.minY() + 2; y <= r.minY() + height; y++) {
                r.world().getBlockAt(r.minX(), y, z).setType(wall, false);
                r.world().getBlockAt(r.maxX(), y, z).setType(wall, false);
            }
        }
    }

    private static void boxWalls(Region r, int height, Material wall, Material pillar, Material light) {
        for (int x = r.minX(); x <= r.maxX(); x++) {
            for (int y = r.minY() + 1; y <= r.minY() + height; y++) {
                r.world().getBlockAt(x, y, r.minZ()).setType(wall, false);
                r.world().getBlockAt(x, y, r.maxZ()).setType(wall, false);
            }
        }
        for (int z = r.minZ(); z <= r.maxZ(); z++) {
            for (int y = r.minY() + 1; y <= r.minY() + height; y++) {
                r.world().getBlockAt(r.minX(), y, z).setType(wall, false);
                r.world().getBlockAt(r.maxX(), y, z).setType(wall, false);
            }
        }
        int[][] corners = {{r.minX(), r.minZ()}, {r.minX(), r.maxZ()}, {r.maxX(), r.minZ()}, {r.maxX(), r.maxZ()}};
        for (int[] corner : corners) {
            for (int y = r.minY() + 1; y <= r.minY() + height + 1; y++) r.world().getBlockAt(corner[0], y, corner[1]).setType(pillar, false);
            r.world().getBlockAt(corner[0], r.minY() + height + 2, corner[1]).setType(light, false);
        }
    }

    private static void floor(Region r, int y, Material material) {
        for (int x = r.minX() + 1; x <= r.maxX() - 1; x++) {
            for (int z = r.minZ() + 1; z <= r.maxZ() - 1; z++) {
                r.world().getBlockAt(x, y, z).setType(material, false);
            }
        }
    }

    private static void wallColumn(World world, int x, int y, int z, int height, Material wall, Material pillar, Material light) {
        for (int dy = 0; dy < height; dy++) world.getBlockAt(x, y + dy, z).setType(dy == height - 1 ? pillar : wall, false);
        if ((x + z) % 9 == 0) world.getBlockAt(x, y + Math.min(3, height - 1), z).setType(light, false);
    }

    private static void plaza(Location location, Material floor, Material pillar, Material glass) {
        World world = location.getWorld();
        if (world == null) return;
        int cx = location.getBlockX();
        int cy = location.getBlockY() - 1;
        int cz = location.getBlockZ();
        platform(world, cx, cy, cz, 3, floor);
        for (int offset = -3; offset <= 3; offset += 6) {
            for (int y = cy + 1; y <= cy + 4; y++) {
                world.getBlockAt(cx, y, cz + offset).setType(pillar, false);
                world.getBlockAt(cx + offset, y, cz).setType(pillar, false);
            }
        }
        for (int z = cz - 3; z <= cz + 3; z++) world.getBlockAt(cx, cy + 4, z).setType(glass, false);
        for (int x = cx - 3; x <= cx + 3; x++) world.getBlockAt(x, cy + 4, cz).setType(glass, false);
    }

    private static void parkourPlatform(World world, int cx, int cy, int cz, int radius, Material material, boolean checkpoint) {
        platform(world, cx, cy, cz, radius, material);
        world.getBlockAt(cx, cy - 1, cz).setType(checkpoint ? Material.SEA_LANTERN : Material.IRON_BARS, false);
        if (checkpoint) cornerFences(world, cx, cy + 1, cz, radius);
    }

    private static void cornerFences(World world, int cx, int cy, int cz, int radius) {
        world.getBlockAt(cx - radius, cy, cz - radius).setType(Material.OAK_FENCE, false);
        world.getBlockAt(cx + radius, cy, cz - radius).setType(Material.OAK_FENCE, false);
        world.getBlockAt(cx - radius, cy, cz + radius).setType(Material.OAK_FENCE, false);
        world.getBlockAt(cx + radius, cy, cz + radius).setType(Material.OAK_FENCE, false);
    }

    private static void support(World world, int x, int fromY, int toY, int z, Material material) {
        if (toY < fromY) return;
        for (int y = fromY; y <= toY; y++) world.getBlockAt(x, y, z).setType(material, false);
    }

    private static void sideStep(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
        world.getBlockAt(x, y - 1, z).setType(Material.IRON_BARS, false);
    }

    private static void arch(World world, int x, int y, int z, Material light) {
        world.getBlockAt(x, y + 2, z - 2).setType(Material.OAK_FENCE, false);
        world.getBlockAt(x, y + 2, z + 2).setType(Material.OAK_FENCE, false);
        world.getBlockAt(x, y + 3, z).setType(light, false);
    }

    private static void miniBridge(World world, int x, int y, int z, int variant) {
        Material material = variant % 2 == 0 ? Material.BIRCH_SLAB : Material.SMOOTH_STONE_SLAB;
        for (int dx = -1; dx <= 1; dx++) world.getBlockAt(x + dx, y, z).setType(material, false);
    }

    private static void waterObstacle(World world, int x, int y, int z, int variant) {
        Material material = variant % 2 == 0 ? Material.PRISMARINE_WALL : Material.SEA_LANTERN;
        world.getBlockAt(x, y, z).setType(material, false);
        world.getBlockAt(x, y + 1, z).setType(Material.CHAIN, false);
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

    private static int distance(int step) {
        return step % 5 == 0 ? 3 : (step % 3 == 0 ? 4 : 3);
    }

    private static boolean near(int x, int z, Location location, int radius) {
        return Math.abs(x - location.getBlockX()) <= radius && Math.abs(z - location.getBlockZ()) <= radius;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
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
