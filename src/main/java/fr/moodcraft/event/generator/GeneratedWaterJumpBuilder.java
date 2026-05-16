package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class GeneratedWaterJumpBuilder {

    private static final Random RANDOM = new Random();
    private static final Material[] WOOL = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIGHT_BLUE_WOOL,
            Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };

    private GeneratedWaterJumpBuilder() {
    }

    public static Layout build(Location center, int length) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int safeLength = Math.max(30, Math.min(140, length));
        int finishX = cx + safeLength;

        buildArena(world, cx - 6, finishX + 8, cz - 6, cz + 6, cy - 1, cy + 5);
        buildWaterLane(world, cx, finishX, cy, cz);
        drawLine(world, cx, cy + 1, cz, 4, Material.LIME_WOOL);
        drawLine(world, finishX, cy + 1, cz, 4, Material.RED_WOOL);
        buildPlatforms(world, cx, finishX, cy, cz);

        return new Layout(
                new Location(world, cx + 0.5, cy + 2, cz + 0.5, 90f, 0f),
                new Location(world, finishX + 0.5, cy + 2, cz + 0.5, -90f, 0f)
        );
    }

    private static void buildArena(World world, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(x, y, minZ).setType(y == minY ? Material.PRISMARINE_BRICKS : Material.CYAN_STAINED_GLASS, false);
                world.getBlockAt(x, y, maxZ).setType(y == minY ? Material.PRISMARINE_BRICKS : Material.CYAN_STAINED_GLASS, false);
            }
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(minX, y, z).setType(y == minY ? Material.PRISMARINE_BRICKS : Material.CYAN_STAINED_GLASS, false);
                world.getBlockAt(maxX, y, z).setType(y == minY ? Material.PRISMARINE_BRICKS : Material.CYAN_STAINED_GLASS, false);
            }
        }
    }

    private static void buildWaterLane(World world, int minX, int maxX, int cy, int cz) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = cz - 4; z <= cz + 4; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.PRISMARINE_BRICKS, false);
                world.getBlockAt(x, cy, z).setType(Material.WATER, false);
                world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
                world.getBlockAt(x, cy + 2, z).setType(Material.AIR, false);
            }
        }
    }

    private static void buildPlatforms(World world, int startX, int finishX, int cy, int cz) {
        int index = 0;
        for (int x = startX + 5; x < finishX - 4; x += 5) {
            int z = clamp(cz + RANDOM.nextInt(7) - 3, cz - 3, cz + 3);
            int radius = index % 6 == 0 ? 2 : 1;
            platform(world, x, cy + 1, z, radius, WOOL[index % WOOL.length]);
            index++;
        }
    }

    private static void drawLine(World world, int x, int y, int cz, int halfWidth, Material material) {
        for (int z = cz - halfWidth; z <= cz + halfWidth; z++) {
            world.getBlockAt(x, y, z).setType(material, false);
            world.getBlockAt(x, y + 1, z).setType(Material.AIR, false);
        }
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Layout(Location start, Location finish) {
    }
}
