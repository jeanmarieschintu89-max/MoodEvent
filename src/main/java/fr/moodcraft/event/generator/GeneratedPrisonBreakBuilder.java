package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GeneratedPrisonBreakBuilder {

    private static final int CELL_SIZE = 3;
    private static final int WALL_HEIGHT = 4;

    private GeneratedPrisonBreakBuilder() {}

    public static Layout build(Location center, int cellsWide) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center, 0, false);

        int safeCells = Math.max(5, Math.min(15, cellsWide));
        if (safeCells % 2 == 0) safeCells++;
        int blocks = safeCells * CELL_SIZE;
        int half = blocks / 2;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        Random random = new Random(System.nanoTime() ^ world.getFullTime() ^ cx ^ cz);
        boolean[][] open = maze(safeCells, random);

        buildShell(world, cx, cy, cz, half);
        buildMaze(world, cx, cy, cz, half, safeCells, open, random);
        buildCells(world, cx, cy, cz, half, safeCells, random);
        buildStartCell(world, cx, cy, cz, half);
        buildExit(world, cx, cy, cz, half);
        decorate(world, cx, cy, cz, half, random);

        Location start = new Location(world, cx + 0.5, cy + 1.0, cz + half - 2.5, 180f, 0f);
        Location finish = new Location(world, cx + 0.5, cy + 1.0, cz - half + 2.5, 0f, 0f);
        return new Layout(start, finish, safeCells, true);
    }

    private static boolean[][] maze(int size, Random random) {
        boolean[][] open = new boolean[size][size];
        carve(open, size / 2, size - 2, random);
        open[size / 2][size - 1] = true;
        open[size / 2][0] = true;
        return open;
    }

    private static void carve(boolean[][] open, int x, int z, Random random) {
        open[x][z] = true;
        List<int[]> dirs = new ArrayList<>();
        dirs.add(new int[]{2, 0});
        dirs.add(new int[]{-2, 0});
        dirs.add(new int[]{0, 2});
        dirs.add(new int[]{0, -2});
        Collections.shuffle(dirs, random);
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int nz = z + dir[1];
            if (nx <= 0 || nz <= 0 || nx >= open.length - 1 || nz >= open.length - 1 || open[nx][nz]) continue;
            open[x + dir[0] / 2][z + dir[1] / 2] = true;
            carve(open, nx, nz, random);
        }
    }

    private static void buildShell(World world, int cx, int cy, int cz, int half) {
        for (int x = cx - half - 2; x <= cx + half + 2; x++) {
            for (int z = cz - half - 2; z <= cz + half + 2; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.POLISHED_ANDESITE, false);
                world.getBlockAt(x, cy, z).setType(Material.STONE_BRICKS, false);
            }
        }

        for (int y = cy + 1; y <= cy + WALL_HEIGHT; y++) {
            for (int x = cx - half - 2; x <= cx + half + 2; x++) {
                world.getBlockAt(x, y, cz - half - 2).setType(wallFor(y + x), false);
                world.getBlockAt(x, y, cz + half + 2).setType(wallFor(y + x + 4), false);
            }
            for (int z = cz - half - 2; z <= cz + half + 2; z++) {
                world.getBlockAt(cx - half - 2, y, z).setType(wallFor(y + z + 8), false);
                world.getBlockAt(cx + half + 2, y, z).setType(wallFor(y + z + 12), false);
            }
        }
    }

    private static void buildMaze(World world, int cx, int cy, int cz, int half, int size, boolean[][] open, Random random) {
        int originX = cx - half;
        int originZ = cz - half;
        for (int gx = 0; gx < size; gx++) {
            for (int gz = 0; gz < size; gz++) {
                int bx = originX + gx * CELL_SIZE;
                int bz = originZ + gz * CELL_SIZE;
                if (open[gx][gz]) {
                    for (int dx = 0; dx < CELL_SIZE; dx++) {
                        for (int dz = 0; dz < CELL_SIZE; dz++) {
                            world.getBlockAt(bx + dx, cy + 1, bz + dz).setType(Material.AIR, false);
                            world.getBlockAt(bx + dx, cy + 2, bz + dz).setType(Material.AIR, false);
                            world.getBlockAt(bx + dx, cy + 3, bz + dz).setType(Material.AIR, false);
                        }
                    }
                    if (random.nextInt(7) == 0) world.getBlockAt(bx + 1, cy + 1, bz + 1).setType(Material.LANTERN, false);
                } else {
                    for (int dx = 0; dx < CELL_SIZE; dx++) {
                        for (int dz = 0; dz < CELL_SIZE; dz++) {
                            for (int y = cy + 1; y <= cy + 3; y++) {
                                world.getBlockAt(bx + dx, y, bz + dz).setType(random.nextBoolean() ? Material.CRACKED_STONE_BRICKS : Material.STONE_BRICKS, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void buildCells(World world, int cx, int cy, int cz, int half, int size, Random random) {
        int minX = cx - half + 2;
        int maxX = cx + half - 2;
        int minZ = cz - half + 2;
        int maxZ = cz + half - 2;

        for (int i = 0; i < size; i += 2) {
            int x = minX + i * 2;
            buildBarCell(world, clamp(x, minX, maxX), cy, maxZ + 2, BlockFace.NORTH, random);
            buildBarCell(world, clamp(x, minX, maxX), cy, minZ - 2, BlockFace.SOUTH, random);
        }
    }

    private static void buildStartCell(World world, int cx, int cy, int cz, int half) {
        int z = cz + half - 3;
        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int y = cy + 1; y <= cy + 3; y++) {
                world.getBlockAt(x, y, z).setType(Material.IRON_BARS, false);
            }
        }
        world.getBlockAt(cx, cy + 1, z).setType(Material.AIR, false);
        world.getBlockAt(cx, cy + 2, z).setType(Material.AIR, false);
        placeDoorHint(world, cx, cy + 1, z - 1, BlockFace.NORTH);
    }

    private static void buildExit(World world, int cx, int cy, int cz, int half) {
        int z = cz - half + 2;
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(x, cy, z + dz).setType(Material.RED_WOOL, false);
            }
        }
        for (int y = cy + 1; y <= cy + 3; y++) {
            world.getBlockAt(cx - 4, y, z).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + 4, y, z).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void decorate(World world, int cx, int cy, int cz, int half, Random random) {
        for (int i = 0; i < 20; i++) {
            int x = cx - half + random.nextInt(half * 2 + 1);
            int z = cz - half + random.nextInt(half * 2 + 1);
            Material type = random.nextBoolean() ? Material.COBWEB : Material.CHAIN;
            if (world.getBlockAt(x, cy + 1, z).getType() == Material.AIR) world.getBlockAt(x, cy + 2, z).setType(type, false);
        }
    }

    private static void buildBarCell(World world, int x, int cy, int z, BlockFace facing, Random random) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int y = cy + 1; y <= cy + 3; y++) {
                world.getBlockAt(x + dx, y, z).setType(Material.IRON_BARS, false);
            }
        }
        if (random.nextBoolean()) {
            world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            world.getBlockAt(x, cy + 2, z).setType(Material.AIR, false);
        }
        placeDoorHint(world, x, cy + 1, facing == BlockFace.NORTH ? z - 1 : z + 1, facing);
    }

    private static void placeDoorHint(World world, int x, int y, int z, BlockFace facing) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(Material.IRON_DOOR, false);
        BlockData data = block.getBlockData();
        if (data instanceof Directional directional) {
            directional.setFacing(facing);
            block.setBlockData(data, false);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Material wallFor(int seed) {
        Material[] walls = {
                Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS,
                Material.POLISHED_ANDESITE
        };
        return walls[Math.floorMod(seed, walls.length)];
    }

    public record Layout(Location start, Location finish, int cellsWide, boolean reachable) {}
}
