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
        int topY = cy + 4 + safePlatforms;

        buildSafetyTower(world, cx, cy, cz, topY);
        drawStartLine(world, cx, cy, cz);

        int x = cx;
        int z = cz;
        int y = cy + 1;
        platform(world, x, y, z, 1, Material.LIME_WOOL);

        for (int i = 1; i <= safePlatforms; i++) {
            Step step = nextStep(cx, cz, i);
            x = step.x();
            z = step.z();
            y = cy + 1 + i;
            buildChallengePlatform(world, x, y, z, i, i == safePlatforms);
        }

        Location start = new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f);
        Location finish = new Location(world, x + 0.5, y + 1, z + 0.5, 180f, 0f);
        return new Layout(start, finish);
    }

    private static Step nextStep(int cx, int cz, int index) {
        int ring = Math.min(7, 2 + (index / 7));
        int phase = index % 16;
        int x;
        int z;

        if (phase < 4) {
            x = cx - ring + (phase * 2);
            z = cz - ring;
        } else if (phase < 8) {
            x = cx + ring;
            z = cz - ring + ((phase - 4) * 2);
        } else if (phase < 12) {
            x = cx + ring - ((phase - 8) * 2);
            z = cz + ring;
        } else {
            x = cx - ring;
            z = cz + ring - ((phase - 12) * 2);
        }

        return new Step(clamp(x, cx - 7, cx + 7), clamp(z, cz - 7, cz + 7));
    }

    private static void buildChallengePlatform(World world, int x, int y, int z, int index, boolean finish) {
        if (finish) {
            platform(world, x, y, z, 2, Material.RED_WOOL);
            buildFinishFlag(world, x, y, z);
            return;
        }

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
            platform(world, x, y, z, 1, WOOL[index % WOOL.length]);
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

    private static void drawStartLine(World world, int x, int y, int z) {
        for (int dz = -4; dz <= 4; dz++) {
            world.getBlockAt(x, y, z + dz).setType(Material.LIME_WOOL, false);
            world.getBlockAt(x, y + 1, z + dz).setType(Material.AIR, false);
        }
        world.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
    }

    private static void buildFinishFlag(World world, int x, int y, int z) {
        for (int dy = 1; dy <= 3; dy++) {
            world.getBlockAt(x - 2, y + dy, z - 2).setType(Material.REDSTONE_BLOCK, false);
            world.getBlockAt(x + 2, y + dy, z + 2).setType(Material.REDSTONE_BLOCK, false);
        }
        world.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
    }

    private static void addFenceRails(World world, int cx, int y, int cz, int radius) {
        for (int ix = cx - radius; ix <= cx + radius; ix++) {
            world.getBlockAt(ix, y + 1, cz - radius).setType(Material.OAK_FENCE, false);
            world.getBlockAt(ix, y + 1, cz + radius).setType(Material.OAK_FENCE, false);
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

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int ix = cx - radius; ix <= cx + radius; ix++) {
            for (int iz = cz - radius; iz <= cz + radius; iz++) {
                world.getBlockAt(ix, cy, iz).setType(material, false);
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Step(int x, int z) {
    }

    public record Layout(Location start, Location finish) {
    }
}
