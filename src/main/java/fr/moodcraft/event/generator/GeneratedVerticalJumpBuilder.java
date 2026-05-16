package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class GeneratedVerticalJumpBuilder {

    private static final Random RANDOM = new Random();
    private static final Material[] WOOL = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };

    private GeneratedVerticalJumpBuilder() {
    }

    public static Layout build(Location center, int platforms) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int safePlatforms = Math.max(8, Math.min(42, platforms));
        int topY = cy + 4 + safePlatforms;

        buildSafetyTower(world, cx, cy, cz, topY);
        drawGroundLine(world, cx, cy, cz, 4, Material.LIME_WOOL);
        buildStartMark(world, cx, cy, cz);

        int x = cx;
        int z = cz;
        int y = cy + 2;
        for (int i = 1; i <= safePlatforms; i++) {
            int direction = i % 4;
            if (direction == 0) x = cx + 3 + RANDOM.nextInt(3);
            if (direction == 1) z = cz + 3 + RANDOM.nextInt(3);
            if (direction == 2) x = cx - 3 - RANDOM.nextInt(3);
            if (direction == 3) z = cz - 3 - RANDOM.nextInt(3);
            x = clamp(x, cx - 6, cx + 6);
            z = clamp(z, cz - 6, cz + 6);
            y = cy + 2 + i;
            platform(world, x, y, z, i % 6 == 0 ? 2 : 1, WOOL[i % WOOL.length]);
        }

        int finishY = y + 1;
        drawGroundLine(world, cx, finishY, cz, 4, Material.RED_WOOL);
        buildFinishMark(world, cx, finishY, cz);
        return new Layout(new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f), new Location(world, cx + 0.5, finishY + 1, cz + 0.5, 180f, 0f));
    }

    private static void buildSafetyTower(World world, int cx, int cy, int cz, int topY) {
        int minX = cx - 9;
        int maxX = cx + 9;
        int minZ = cz - 9;
        int maxZ = cz + 9;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.BLUE_CONCRETE, false);
            }
        }
        for (int y = cy; y <= topY + 5; y++) {
            for (int x = minX; x <= maxX; x++) {
                world.getBlockAt(x, y, minZ).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                world.getBlockAt(x, y, maxZ).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
            }
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(minX, y, z).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                world.getBlockAt(maxX, y, z).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
            }
        }
        for (int y = cy; y <= topY + 5; y += 5) {
            world.getBlockAt(minX, y, minZ).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(maxX, y, minZ).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(minX, y, maxZ).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(maxX, y, maxZ).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void drawGroundLine(World world, int x, int y, int z, int halfWidth, Material material) {
        for (int dz = -halfWidth; dz <= halfWidth; dz++) {
            world.getBlockAt(x, y, z + dz).setType(material, false);
            world.getBlockAt(x, y + 1, z + dz).setType(Material.AIR, false);
        }
    }

    private static void buildStartMark(World world, int x, int y, int z) {
        for (int dz = -4; dz <= 4; dz++) world.getBlockAt(x - 1, y, z + dz).setType(Material.LIME_WOOL, false);
        world.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
    }

    private static void buildFinishMark(World world, int x, int y, int z) {
        for (int dz = -4; dz <= 4; dz++) world.getBlockAt(x + 1, y, z + dz).setType(Material.RED_WOOL, false);
        world.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
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
