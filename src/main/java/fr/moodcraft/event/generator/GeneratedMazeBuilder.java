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

        buildOuterShell(world, minX, maxX, cy, minZ, maxZ);
        buildMazeBlocks(world, minX, cy, minZ, width, open);
        decorate(world, minX, maxX, cy, minZ, maxZ);

        Location start = new Location(world, minX + 1.5, cy + 1, minZ + 1.5, -45f, 0f);
        Location finish = new Location(world, maxX - 1.5, cy + 1, maxZ - 1.5, 135f, 0f);

        clearRoom(world, start.getBlockX(), cy, start.getBlockZ(), Material.LIME_CONCRETE);
        clearRoom(world, finish.getBlockX(), cy, finish.getBlockZ(), Material.RED_CONCRETE);
        gate(world, start.getBlockX(), cy, start.getBlockZ(), Material.LIME_CONCRETE, Material.EMERALD_BLOCK);
        gate(world, finish.getBlockX(), cy, finish.getBlockZ(), Material.RED_CONCRETE, Material.REDSTONE_BLOCK);

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

    private static void decorate(World world, int minX, int maxX, int cy, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x += 8) {
            for (int z = minZ; z <= maxZ; z += 8) {
                if (!world.getBlockAt(x, cy + 1, z).getType().isAir()) continue;
                world.getBlockAt(x, cy + WALL_HEIGHT + 1, z).setType(Material.SEA_LANTERN, false);
            }
        }
    }

    private static void clearRoom(World world, int cx, int cy, int cz, Material floor) {
        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                world.getBlockAt(x, cy, z).setType(floor, false);
                for (int y = cy + 1; y <= cy + WALL_HEIGHT + 1; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
    }

    private static void gate(World world, int cx, int cy, int cz, Material floor, Material pillar) {
        for (int x = cx - 2; x <= cx + 2; x++) for (int z = cz - 2; z <= cz + 2; z++) world.getBlockAt(x, cy, z).setType(floor, false);
        for (int y = cy + 1; y <= cy + 5; y++) {
            world.getBlockAt(cx - 2, y, cz - 2).setType(pillar, false);
            world.getBlockAt(cx + 2, y, cz - 2).setType(pillar, false);
            world.getBlockAt(cx - 2, y, cz + 2).setType(pillar, false);
            world.getBlockAt(cx + 2, y, cz + 2).setType(pillar, false);
        }
        world.getBlockAt(cx, cy + 5, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void placeLoot(World world, LootTier tier, int x, int y, int z) {
        while (y > world.getMinHeight() && !world.getBlockAt(x, y - 1, z).getType().isSolid()) y--;
        world.getBlockAt(x, y, z).setType(Material.CHEST, false);
        EventLootManager.registerLootChest(new Location(world, x, y, z), GeneratedGameType.LABYRINTHE, tier);
    }

    public record Layout(Location start, Location finish) {
    }
}
