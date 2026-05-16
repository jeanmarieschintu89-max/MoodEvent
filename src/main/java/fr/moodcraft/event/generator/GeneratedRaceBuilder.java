package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public final class GeneratedRaceBuilder {

    private GeneratedRaceBuilder() {
    }

    public static Layout build(Location center, int length) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int safeLength = Math.max(50, Math.min(220, length));
        int finishX = cx + safeLength;

        buildArena(world, cx - 6, finishX + 8, cz - 4, cz + 4, cy, cy + 4);
        buildTrack(world, cx, finishX, cy, cz);
        buildObstacles(world, cx + 8, finishX - 8, cy, cz);
        drawLine(world, cx, cy, cz, 2, Material.LIME_CONCRETE);
        drawLine(world, finishX, cy, cz, 2, Material.RED_CONCRETE);

        return new Layout(
                new Location(world, cx + 0.5, cy + 1, cz + 0.5, 90f, 0f),
                new Location(world, finishX + 0.5, cy + 1, cz + 0.5, -90f, 0f)
        );
    }

    private static void buildArena(World world, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(x, y, minZ).setType(y == minY ? Material.SMOOTH_STONE : Material.IRON_BARS, false);
                world.getBlockAt(x, y, maxZ).setType(y == minY ? Material.SMOOTH_STONE : Material.IRON_BARS, false);
            }
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(minX, y, z).setType(y == minY ? Material.SMOOTH_STONE : Material.IRON_BARS, false);
                world.getBlockAt(maxX, y, z).setType(y == minY ? Material.SMOOTH_STONE : Material.IRON_BARS, false);
            }
        }
    }

    private static void buildTrack(World world, int minX, int maxX, int y, int cz) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                world.getBlockAt(x, y, z).setType((x + z) % 2 == 0 ? Material.SMOOTH_STONE : Material.POLISHED_ANDESITE, false);
                world.getBlockAt(x, y + 1, z).setType(Material.AIR, false);
                world.getBlockAt(x, y + 2, z).setType(Material.AIR, false);
                world.getBlockAt(x, y + 3, z).setType(Material.AIR, false);
            }
        }
    }

    private static void buildObstacles(World world, int minX, int maxX, int y, int cz) {
        int index = 0;
        for (int x = minX; x <= maxX; x += 12) {
            switch (index % 6) {
                case 0 -> buildHayHurdles(world, x, y, cz);
                case 1 -> buildSlabWave(world, x, y, cz);
                case 2 -> buildFenceGate(world, x, y, cz);
                case 3 -> buildZigZagBlocks(world, x, y, cz);
                case 4 -> buildLowWall(world, x, y, cz);
                default -> buildSpeedBumps(world, x, y, cz);
            }
            index++;
        }
    }

    private static void buildHayHurdles(World world, int x, int y, int cz) {
        world.getBlockAt(x, y + 1, cz - 2).setType(Material.HAY_BLOCK, false);
        world.getBlockAt(x, y + 1, cz + 2).setType(Material.HAY_BLOCK, false);
    }

    private static void buildSlabWave(World world, int x, int y, int cz) {
        world.getBlockAt(x, y + 1, cz - 1).setType(Material.OAK_SLAB, false);
        world.getBlockAt(x + 1, y + 1, cz).setType(Material.OAK_SLAB, false);
        world.getBlockAt(x + 2, y + 1, cz + 1).setType(Material.OAK_SLAB, false);
    }

    private static void buildFenceGate(World world, int x, int y, int cz) {
        for (int z = cz - 2; z <= cz + 2; z++) {
            if (z == cz) continue;
            world.getBlockAt(x, y + 1, z).setType(Material.OAK_FENCE, false);
        }
    }

    private static void buildZigZagBlocks(World world, int x, int y, int cz) {
        world.getBlockAt(x, y + 1, cz - 2).setType(Material.COBBLESTONE, false);
        world.getBlockAt(x + 2, y + 1, cz).setType(Material.COBBLESTONE, false);
        world.getBlockAt(x + 4, y + 1, cz + 2).setType(Material.COBBLESTONE, false);
    }

    private static void buildLowWall(World world, int x, int y, int cz) {
        world.getBlockAt(x, y + 1, cz - 1).setType(Material.SMOOTH_STONE_SLAB, false);
        world.getBlockAt(x, y + 1, cz).setType(Material.SMOOTH_STONE_SLAB, false);
        world.getBlockAt(x, y + 1, cz + 1).setType(Material.SMOOTH_STONE_SLAB, false);
    }

    private static void buildSpeedBumps(World world, int x, int y, int cz) {
        world.getBlockAt(x, y + 1, cz - 2).setType(Material.YELLOW_CARPET, false);
        world.getBlockAt(x, y + 1, cz).setType(Material.YELLOW_CARPET, false);
        world.getBlockAt(x, y + 1, cz + 2).setType(Material.YELLOW_CARPET, false);
    }

    private static void drawLine(World world, int x, int y, int cz, int halfWidth, Material material) {
        for (int z = cz - halfWidth; z <= cz + halfWidth; z++) {
            world.getBlockAt(x, y, z).setType(material, false);
            world.getBlockAt(x, y + 1, z).setType(Material.AIR, false);
            world.getBlockAt(x, y + 2, z).setType(Material.AIR, false);
            world.getBlockAt(x, y + 3, z).setType(Material.AIR, false);
        }
    }

    public record Layout(Location start, Location finish) {
    }
}
