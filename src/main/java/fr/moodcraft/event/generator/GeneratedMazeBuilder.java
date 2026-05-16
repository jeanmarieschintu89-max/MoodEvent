package fr.moodcraft.event.generator;

import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.loot.LootTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GeneratedMazeBuilder {

    private static final Random RANDOM = new Random();
    private static final int WALL_HEIGHT = 5;

    private GeneratedMazeBuilder() {
    }

    public static Layout build(Location center, int requestedWidth) {
        World world = center.getWorld();
        int width = requestedWidth % 2 == 1 ? requestedWidth : requestedWidth + 1;
        width = Math.max(15, Math.min(101, width));

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int half = width / 2;
        int minX = cx - half;
        int maxX = cx + half;
        int minZ = cz - half;
        int maxZ = cz + half;

        boolean[][] open = generateMaze(width);
        open[1][1] = true;
        open[2][1] = true;
        open[3][1] = true;
        open[width - 2][width - 2] = true;
        open[width - 3][width - 2] = true;
        open[width - 4][width - 2] = true;

        buildOuterShell(world, minX, maxX, cy, minZ, maxZ);
        buildMazeBlocks(world, minX, cy, minZ, width, open);
        decorate(world, minX, maxX, cy, minZ, maxZ);

        int startX = minX - 5;
        int startZ = minZ + 1;
        int entryX = minX + 1;
        int entryZ = minZ + 1;
        int finishX = maxX + 5;
        int finishZ = maxZ - 1;
        int exitX = maxX - 1;
        int exitZ = maxZ - 1;

        clearInternalOldMarker(world, entryX, cy, entryZ);
        clearInternalOldMarker(world, exitX, cy, exitZ);
        buildClosedSas(world, startX, entryX, cy, startZ, true);
        buildClosedSas(world, exitX, finishX, cy, finishZ, false);
        openMazeDoor(world, minX - 2, cy, entryZ);
        openMazeDoor(world, maxX + 2, cy, exitZ);

        Location start = new Location(world, startX + 0.5, cy + 1, startZ + 0.5, 90f, 0f);
        Location finish = new Location(world, finishX + 0.5, cy + 1, finishZ + 0.5, -90f, 0f);

        placeLoot(world, LootTier.COMMUN, cx, cy + 1, minZ + Math.max(4, width / 4));
        placeLoot(world, LootTier.RARE, minX + Math.max(4, width / 3), cy + 1, cz);
        placeLoot(world, LootTier.EPIQUE, maxX - Math.max(4, width / 4), cy + 1, maxZ - Math.max(4, width / 4));

        return new Layout(start, finish);
    }

    private static boolean[][] generateMaze(int width) {
        boolean[][] open = new boolean[width][width];
        boolean[][] visited = new boolean[width][width];
        carve(1, 1, width, open, visited);
        open[1][1] = true;
        open[width - 2][width - 2] = true;
        return open;
    }

    private static void carve(int x, int z, int width, boolean[][] open, boolean[][] visited) {
        visited[x][z] = true;
        open[x][z] = true;

        List<int[]> directions = new ArrayList<>();
        directions.add(new int[]{2, 0});
        directions.add(new int[]{-2, 0});
        directions.add(new int[]{0, 2});
        directions.add(new int[]{0, -2});
        Collections.shuffle(directions, RANDOM);

        for (int[] direction : directions) {
            int nx = x + direction[0];
            int nz = z + direction[1];
            if (nx <= 0 || nz <= 0 || nx >= width - 1 || nz >= width - 1) continue;
            if (visited[nx][nz]) continue;
            open[x + direction[0] / 2][z + direction[1] / 2] = true;
            carve(nx, nz, width, open, visited);
        }
    }

    private static void buildOuterShell(World world, int minX, int maxX, int cy, int minZ, int maxZ) {
        for (int x = minX - 2; x <= maxX + 2; x++) {
            for (int z = minZ - 2; z <= maxZ + 2; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.DEEPSLATE_TILES, false);
            }
        }
        for (int x = minX - 2; x <= maxX + 2; x++) {
            for (int y = cy; y <= cy + WALL_HEIGHT + 2; y++) {
                world.getBlockAt(x, y, minZ - 2).setType(Material.CRACKED_DEEPSLATE_BRICKS, false);
                world.getBlockAt(x, y, maxZ + 2).setType(Material.CRACKED_DEEPSLATE_BRICKS, false);
            }
        }
        for (int z = minZ - 2; z <= maxZ + 2; z++) {
            for (int y = cy; y <= cy + WALL_HEIGHT + 2; y++) {
                world.getBlockAt(minX - 2, y, z).setType(Material.CRACKED_DEEPSLATE_BRICKS, false);
                world.getBlockAt(maxX + 2, y, z).setType(Material.CRACKED_DEEPSLATE_BRICKS, false);
            }
        }
    }

    private static void buildMazeBlocks(World world, int minX, int cy, int minZ, int width, boolean[][] open) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                int x = minX + dx;
                int z = minZ + dz;
                world.getBlockAt(x, cy, z).setType(open[dx][dz] ? Material.POLISHED_ANDESITE : Material.DEEPSLATE_BRICKS, false);
                if (open[dx][dz]) {
                    for (int y = cy + 1; y <= cy + WALL_HEIGHT + 1; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                } else {
                    for (int y = cy + 1; y <= cy + WALL_HEIGHT; y++) {
                        Material material = y == cy + WALL_HEIGHT ? Material.CHISELED_DEEPSLATE : Material.MOSSY_STONE_BRICKS;
                        world.getBlockAt(x, y, z).setType(material, false);
                    }
                }
            }
        }
    }

    private static void clearInternalOldMarker(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 3; z <= cz + 3; z++) {
                world.getBlockAt(x, cy, z).setType(Material.POLISHED_ANDESITE, false);
                for (int y = cy + 1; y <= cy + WALL_HEIGHT + 1; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void decorate(World world, int minX, int maxX, int cy, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x += 8) {
            for (int z = minZ; z <= maxZ; z += 8) {
                if (!world.getBlockAt(x, cy + 1, z).getType().isAir()) continue;
                world.getBlockAt(x, cy + WALL_HEIGHT + 1, z).setType(Material.SEA_LANTERN, false);
            }
        }
    }

    private static void buildClosedSas(World world, int minX, int maxX, int cy, int centerZ, boolean start) {
        Material floor = start ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        Material wall = start ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
        Material glass = start ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS;
        Material pillar = start ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;

        int fromX = Math.min(minX, maxX);
        int toX = Math.max(minX, maxX);
        int minZ = centerZ - 3;
        int maxZ = centerZ + 3;

        for (int x = fromX; x <= toX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.DEEPSLATE_TILES, false);
                world.getBlockAt(x, cy, z).setType(floor, false);
                for (int y = cy + 1; y <= cy + WALL_HEIGHT; y++) {
                    boolean side = z == minZ || z == maxZ;
                    boolean end = x == fromX || x == toX;
                    boolean door = z >= centerZ - 1 && z <= centerZ + 1 && y <= cy + 3;
                    if (side) world.getBlockAt(x, y, z).setType(wall, false);
                    else if (end && !door) world.getBlockAt(x, y, z).setType(wall, false);
                    else world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
                world.getBlockAt(x, cy + WALL_HEIGHT + 1, z).setType(glass, false);
            }
        }

        for (int y = cy + 1; y <= cy + WALL_HEIGHT; y++) {
            world.getBlockAt(fromX, y, minZ).setType(pillar, false);
            world.getBlockAt(fromX, y, maxZ).setType(pillar, false);
            world.getBlockAt(toX, y, minZ).setType(pillar, false);
            world.getBlockAt(toX, y, maxZ).setType(pillar, false);
        }
        world.getBlockAt(start ? fromX + 1 : toX - 1, cy + WALL_HEIGHT, centerZ).setType(Material.SEA_LANTERN, false);
    }

    private static void openMazeDoor(World world, int wallX, int cy, int centerZ) {
        for (int z = centerZ - 1; z <= centerZ + 1; z++) {
            for (int y = cy + 1; y <= cy + 3; y++) {
                world.getBlockAt(wallX, y, z).setType(Material.AIR, false);
            }
            world.getBlockAt(wallX, cy, z).setType(Material.POLISHED_ANDESITE, false);
        }
    }

    private static void placeLoot(World world, LootTier tier, int x, int y, int z) {
        while (y > world.getMinHeight() && !world.getBlockAt(x, y - 1, z).getType().isSolid()) y--;
        world.getBlockAt(x, y, z).setType(Material.CHEST, false);
        EventLootManager.registerLootChest(new Location(world, x, y, z), GeneratedGameType.LABYRINTHE, tier);
    }

    public record Layout(Location start, Location finish) {
    }
}
