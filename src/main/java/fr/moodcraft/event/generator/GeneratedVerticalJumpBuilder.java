package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

public final class GeneratedVerticalJumpBuilder {

    private static final Material[] WOOL = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIGHT_BLUE_WOOL,
            Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };

    private GeneratedVerticalJumpBuilder() {
    }

    public static Layout build(Location center, int platforms) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int safePlatforms = Math.max(12, Math.min(44, platforms));
        int topY = cy + safePlatforms + 8;

        clearOldTowerArea(world, cx, cy, cz);
        buildGlassCage(world, cx, cy, cz, topY);
        buildCleanStart(world, cx, cy, cz);

        int x = cx;
        int z = cz;
        int y = cy + 1;
        platform(world, x, y, z, 1, Material.LIME_WOOL);

        for (int i = 1; i <= safePlatforms; i++) {
            Step step = stepFor(cx, cz, i);
            x = step.x();
            z = step.z();
            y = cy + 1 + i;
            buildStep(world, x, y, z, i, i == safePlatforms);
        }

        return new Layout(
                new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f),
                new Location(world, x + 0.5, y + 1, z + 0.5, 180f, 0f)
        );
    }

    private static Step stepFor(int cx, int cz, int index) {
        int[][] route = {
                {1, 0}, {2, 1}, {2, 2}, {1, 3}, {0, 3}, {-1, 3}, {-2, 2}, {-2, 1},
                {-2, 0}, {-2, -1}, {-1, -2}, {0, -2}, {1, -2}, {2, -1}, {3, 0}, {3, 1},
                {3, 3}, {1, 4}, {-1, 4}, {-3, 3}, {-4, 1}, {-4, -1}, {-3, -3}, {-1, -4},
                {1, -4}, {3, -3}, {4, -1}, {4, 1}, {3, 4}, {0, 5}, {-3, 4}, {-5, 1},
                {-5, -2}, {-3, -5}, {0, -6}, {3, -5}, {5, -2}, {6, 1}, {4, 5}, {1, 6},
                {-2, 6}, {-5, 4}, {-6, 0}, {-5, -4}
        };
        int[] point = route[(index - 1) % route.length];
        return new Step(cx + point[0], cz + point[1]);
    }

    private static void buildStep(World world, int x, int y, int z, int index, boolean finish) {
        if (finish) {
            platform(world, x, y, z, 1, Material.RED_WOOL);
            return;
        }

        if (index <= 3) {
            platform(world, x, y, z, 1, WOOL[index % WOOL.length]);
            return;
        }
        if (index % 12 == 0) {
            platform(world, x, y, z, 2, Material.OAK_PLANKS);
            fenceBack(world, x, y, z, 2);
            return;
        }
        if (index % 10 == 0) {
            platform(world, x, y, z, 1, Material.BLUE_ICE);
            return;
        }
        if (index % 9 == 0) {
            platform(world, x, y, z, 1, Material.SOUL_SAND);
            return;
        }
        if (index % 8 == 0) {
            platform(world, x, y, z, 1, Material.SLIME_BLOCK);
            return;
        }
        if (index % 7 == 0) {
            platform(world, x, y, z, 1, Material.OAK_PLANKS);
            addLadderSupport(world, x + 1, y, z, BlockFace.WEST);
            return;
        }
        if (index % 6 == 0) {
            platform(world, x, y, z, 1, Material.MAGMA_BLOCK);
            return;
        }
        platform(world, x, y, z, 1, WOOL[index % WOOL.length]);
    }

    private static void clearOldTowerArea(World world, int cx, int cy, int cz) {
        int minY = Math.max(world.getMinHeight(), cy - 5);
        int maxY = Math.min(world.getMaxHeight() - 1, cy + 120);
        for (int x = cx - 18; x <= cx + 18; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = cz - 18; z <= cz + 18; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildGlassCage(World world, int cx, int cy, int cz, int topY) {
        int minX = cx - 9;
        int maxX = cx + 9;
        int minZ = cz - 9;
        int maxZ = cz + 9;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.BLACK_CONCRETE, false);
            }
        }

        for (int y = cy; y <= topY + 3; y++) {
            for (int x = minX; x <= maxX; x++) {
                world.getBlockAt(x, y, minZ).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                world.getBlockAt(x, y, maxZ).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
            }
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(minX, y, z).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                world.getBlockAt(maxX, y, z).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
            }
        }
    }

    private static void buildCleanStart(World world, int cx, int cy, int cz) {
        for (int z = cz - 4; z <= cz + 4; z++) {
            world.getBlockAt(cx, cy, z).setType(Material.LIME_WOOL, false);
            world.getBlockAt(cx, cy + 1, z).setType(Material.AIR, false);
            world.getBlockAt(cx, cy + 2, z).setType(Material.AIR, false);
        }
    }

    private static void fenceBack(World world, int cx, int y, int cz, int radius) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            world.getBlockAt(x, y + 1, cz - radius).setType(Material.OAK_FENCE, false);
        }
    }

    private static void addLadderSupport(World world, int x, int y, int z, BlockFace facing) {
        world.getBlockAt(x, y, z).setType(Material.OAK_PLANKS, false);
        world.getBlockAt(x, y + 1, z).setType(Material.LADDER, false);
        BlockData data = world.getBlockAt(x, y + 1, z).getBlockData();
        if (data instanceof Directional directional) {
            directional.setFacing(facing);
            world.getBlockAt(x, y + 1, z).setBlockData(directional, false);
        }
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }

    private record Step(int x, int z) {
    }

    public record Layout(Location start, Location finish) {
    }
}
