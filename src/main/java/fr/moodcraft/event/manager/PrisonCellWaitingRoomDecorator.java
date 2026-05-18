package fr.moodcraft.event.manager;

import org.bukkit.Material;
import org.bukkit.World;

public final class PrisonCellWaitingRoomDecorator {

    private PrisonCellWaitingRoomDecorator() {}

    public static void decorate(World world, int cx, int cy, int cz, int radius, int height) {
        if (world == null || radius < 3) return;

        buildFrontBars(world, cx, cy, cz, radius, height);
        buildBedCorner(world, cx, cy, cz, radius);
        buildHygieneCorner(world, cx, cy, cz, radius);
        buildStorageCorner(world, cx, cy, cz, radius);
        buildCeilingDetails(world, cx, cy, cz, radius, height);

        if (radius >= 5) buildSideCell(world, cx, cy, cz, radius, height);
        if (radius >= 7) buildGuardDesk(world, cx, cy, cz, radius);
        if (radius >= 9) buildLargeDetails(world, cx, cy, cz, radius, height);
    }

    private static void buildFrontBars(World world, int cx, int cy, int cz, int radius, int height) {
        int z = cz - radius + 1;
        int width = Math.min(4, radius - 1);
        for (int x = cx - width; x <= cx + width; x++) {
            for (int y = cy + 1; y <= cy + Math.min(4, height - 1); y++) {
                world.getBlockAt(x, y, z).setType(Material.IRON_BARS, false);
            }
        }
        world.getBlockAt(cx, cy + 1, z).setType(Material.AIR, false);
        world.getBlockAt(cx, cy + 2, z).setType(Material.AIR, false);
        world.getBlockAt(cx - width, cy + 1, z + 1).setType(Material.CHAIN, false);
        world.getBlockAt(cx + width, cy + 1, z + 1).setType(Material.CHAIN, false);
    }

    private static void buildBedCorner(World world, int cx, int cy, int cz, int radius) {
        int x = cx - radius + 2;
        int z = cz + radius - 2;
        world.getBlockAt(x, cy + 1, z).setType(Material.GRAY_BED, false);
        if (radius >= 4) {
            world.getBlockAt(x + 1, cy + 1, z).setType(Material.GRAY_CARPET, false);
            world.getBlockAt(x + 2, cy + 1, z).setType(Material.IRON_TRAPDOOR, false);
        }
        if (radius >= 5) {
            world.getBlockAt(x, cy + 1, z - 1).setType(Material.BARREL, false);
            world.getBlockAt(x + 1, cy + 1, z - 1).setType(Material.CHEST, false);
        }
    }

    private static void buildHygieneCorner(World world, int cx, int cy, int cz, int radius) {
        int x = cx + radius - 2;
        int z = cz + radius - 2;
        world.getBlockAt(x, cy + 1, z).setType(Material.CAULDRON, false);
        if (radius >= 4) {
            world.getBlockAt(x - 1, cy + 1, z).setType(Material.IRON_TRAPDOOR, false);
            world.getBlockAt(x, cy + 1, z - 1).setType(Material.STONE_BUTTON, false);
        }
        if (radius >= 5) {
            world.getBlockAt(x - 1, cy + 1, z - 1).setType(Material.POLISHED_ANDESITE, false);
            world.getBlockAt(x, cy + 2, z - 1).setType(Material.CHAIN, false);
        }
    }

    private static void buildStorageCorner(World world, int cx, int cy, int cz, int radius) {
        int x = cx - radius + 2;
        int z = cz - radius + 2;
        world.getBlockAt(x, cy + 1, z).setType(Material.CHEST, false);
        if (radius >= 4) world.getBlockAt(x + 1, cy + 1, z).setType(Material.BARREL, false);
        if (radius >= 5) {
            world.getBlockAt(x, cy + 1, z + 1).setType(Material.CHAIN, false);
            world.getBlockAt(x + 1, cy + 1, z + 1).setType(Material.COBWEB, false);
        }
    }

    private static void buildCeilingDetails(World world, int cx, int cy, int cz, int radius, int height) {
        int roof = cy + Math.max(3, height - 1);
        world.getBlockAt(cx, roof, cz).setType(Material.SOUL_LANTERN, false);
        if (radius >= 4) {
            world.getBlockAt(cx - radius + 2, roof, cz).setType(Material.CHAIN, false);
            world.getBlockAt(cx + radius - 2, roof, cz).setType(Material.CHAIN, false);
        }
        if (radius >= 6) {
            world.getBlockAt(cx, roof, cz - radius + 2).setType(Material.CHAIN, false);
            world.getBlockAt(cx, roof, cz + radius - 2).setType(Material.CHAIN, false);
            world.getBlockAt(cx, roof - 1, cz - radius + 2).setType(Material.SOUL_LANTERN, false);
        }
    }

    private static void buildSideCell(World world, int cx, int cy, int cz, int radius, int height) {
        int x = cx + radius - 2;
        int half = Math.min(3, radius - 2);
        for (int z = cz - half; z <= cz + half; z++) {
            for (int y = cy + 1; y <= cy + Math.min(4, height - 1); y++) {
                world.getBlockAt(x, y, z).setType(Material.IRON_BARS, false);
            }
        }
        world.getBlockAt(x, cy + 1, cz).setType(Material.AIR, false);
        world.getBlockAt(x, cy + 2, cz).setType(Material.AIR, false);
        world.getBlockAt(x - 1, cy + 1, cz + half - 1).setType(Material.GRAY_BED, false);
        world.getBlockAt(x - 1, cy + 1, cz - half + 1).setType(Material.CAULDRON, false);
    }

    private static void buildGuardDesk(World world, int cx, int cy, int cz, int radius) {
        int z = cz - radius + 3;
        world.getBlockAt(cx - 2, cy + 1, z).setType(Material.LECTERN, false);
        world.getBlockAt(cx - 1, cy + 1, z).setType(Material.DARK_OAK_STAIRS, false);
        world.getBlockAt(cx, cy + 1, z).setType(Material.BARREL, false);
        world.getBlockAt(cx + 1, cy + 1, z).setType(Material.CHEST, false);
        world.getBlockAt(cx + 2, cy + 1, z).setType(Material.REDSTONE_TORCH, false);
        world.getBlockAt(cx, cy + 2, z + 1).setType(Material.CHAIN, false);
    }

    private static void buildLargeDetails(World world, int cx, int cy, int cz, int radius, int height) {
        int north = cz - radius + 3;
        int south = cz + radius - 3;
        for (int x = cx - 3; x <= cx + 3; x += 3) {
            world.getBlockAt(x, cy + 1, north).setType(Material.IRON_BARS, false);
            world.getBlockAt(x, cy + 2, north).setType(Material.IRON_BARS, false);
            world.getBlockAt(x, cy + 1, south).setType(Material.COBWEB, false);
        }
        world.getBlockAt(cx - 3, cy + 1, cz).setType(Material.BARREL, false);
        world.getBlockAt(cx + 3, cy + 1, cz).setType(Material.CHEST, false);
        world.getBlockAt(cx, cy + Math.max(2, height - 2), cz + radius - 3).setType(Material.CHAIN, false);
    }
}
