package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.Random;

public final class GeneratedVerticalJumpBuilder {

    private static final Random RANDOM = new Random();
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
        int topY = cy + 3 + safePlatforms;

        buildSafetyTower(world, cx, cy, cz, topY);
        drawGroundLine(world, cx, cy, cz, 4, Material.LIME_WOOL);
        buildStartMark(world, cx, cy, cz);
        buildStarterPath(world, cx, cy, cz);

        int x = cx + 2;
        int z = cz;
        int y = cy + 1;
        for (int i = 1; i <= safePlatforms; i++) {
            Step step = nextStep(cx, cz, x, z, cy + i, i);
            x = step.x();
            z = step.z();
            y = step.y();
            buildChallengePlatform(world, x, y, z, i);
        }

        int finishY = y + 1;
        drawGroundLine(world, cx, finishY, cz, 4, Material.RED_WOOL);
        buildFinishMark(world, cx, finishY, cz);
        return new Layout(new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f), new Location(world, cx + 0.5, finishY + 1, cz + 0.5, 180f, 0f));
    }

    private static void buildStarterPath(World world, int cx, int cy, int cz) {
        platform(world, cx + 1, cy, cz, 1, Material.LIME_WOOL);
        platform(world, cx + 2, cy + 1, cz, 1, Material.WHITE_WOOL);
        world.getBlockAt(cx + 1, cy + 1, cz).setType(Material.AIR, false);
        world.getBlockAt(cx + 2, cy + 2, cz).setType(Material.AIR, false);
    }

    private static Step nextStep(int cx, int cz, int previousX, int previousZ, int y, int index) {
        int[][] pattern = {
                {1, 1}, {2, 0}, {1, -1}, {0, -2}, {-1, -1}, {-2, 0}, {-1, 1}, {0, 2},
                {2, 1}, {-2, 1}, {-2, -1}, {2, -1}
        };
        int[] delta = pattern[index % pattern.length];
        int x = clamp(previousX + delta[0], cx - 6, cx + 6);
        int z = clamp(previousZ + delta[1], cz - 6, cz + 6);
        return new Step(x, y, z);
    }

    private static void buildChallengePlatform(World world, int x, int y, int z, int index) {
        if (index <= 2) {
            platform(world, x, y, z, 1, WOOL[index % WOOL.length]);
            return;
        }
        if (index % 12 == 0) {
            platform(world, x, y, z, 2, Material.OAK_PLANKS);
            addFenceRails(world, x, y, z, 2);
            addMiniLadder(world, x - 2, y, z, BlockFace.EAST);
            return;
        }
        if (index % 10 == 0) {
            platform(world, x, y, z, 1, Material.SPRUCE_SLAB);
            addFencePosts(world, x, y, z, 1);
            return;
        }
        if (index % 9 == 0) {
            platform(world, x, y, z, 1, Material.BLUE_ICE);
            addFencePosts(world, x, y, z, 1);
            return;
        }
        if (index % 8 == 0) {
            platform(world, x, y, z, 1, Material.SLIME_BLOCK);
            world.getBlockAt(x, y + 2, z).setType(Material.OAK_TRAPDOOR, false);
            return;
        }
        if (index % 7 == 0) {
            platform(world, x, y, z, 1, Material.SOUL_SAND);
            addLowWall(world, x, y, z, Material.OAK_FENCE);
            return;
        }
        if (index % 6 == 0) {
            platform(world, x, y, z, 1, Material.OAK_FENCE);
            world.getBlockAt(x, y, z).setType(WOOL[index % WOOL.length], false);
            addFencePosts(world, x, y, z, 1);
            return;
        }
        if (index % 5 == 0) {
            platform(world, x, y, z, 0, Material.MAGMA_BLOCK);
            addSingleSupport(world, x, y - 1, z);
            return;
        }
        if (index % 4 == 0) {
            platform(world, x, y, z, 1, Material.OAK_PLANKS);
            addMiniLadder(world, x + 1, y, z, BlockFace.WEST);
            return;
        }
        platform(world, x, y, z, 1, WOOL[index % WOOL.length]);
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

    private static void addFenceRails(World world, int cx, int y, int cz, int radius) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            world.getBlockAt(x, y + 1, cz - radius).setType(Material.OAK_FENCE, false);
            world.getBlockAt(x, y + 1, cz + radius).setType(Material.OAK_FENCE, false);
        }
    }

    private static void addFencePosts(World world, int cx, int y, int cz, int radius) {
        world.getBlockAt(cx - radius, y + 1, cz - radius).setType(Material.OAK_FENCE, false);
        world.getBlockAt(cx + radius, y + 1, cz + radius).setType(Material.OAK_FENCE, false);
    }

    private static void addLowWall(World world, int cx, int y, int cz, Material material) {
        world.getBlockAt(cx + 1, y + 1, cz).setType(material, false);
        world.getBlockAt(cx - 1, y + 1, cz).setType(material, false);
    }

    private static void addSingleSupport(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.OAK_FENCE, false);
    }

    private static void addMiniLadder(World world, int x, int y, int z, BlockFace facing) {
        world.getBlockAt(x, y, z).setType(Material.OAK_PLANKS, false);
        world.getBlockAt(x, y + 1, z).setType(Material.LADDER, false);
        BlockData data = world.getBlockAt(x, y + 1, z).getBlockData();
        if (data instanceof Directional directional) {
            directional.setFacing(facing);
            world.getBlockAt(x, y + 1, z).setBlockData(directional, false);
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

    private record Step(int x, int y, int z) {
    }

    public record Layout(Location start, Location finish) {
    }
}
