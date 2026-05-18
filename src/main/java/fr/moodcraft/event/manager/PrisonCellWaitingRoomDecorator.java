package fr.moodcraft.event.manager;

import org.bukkit.Material;
import org.bukkit.World;

public final class PrisonCellWaitingRoomDecorator {

    private PrisonCellWaitingRoomDecorator() {}

    public static void decorate(World world, int cx, int cy, int cz, int radius, int height) {
        if (world == null || radius < 3) return;

        buildMainCellBars(world, cx, cy, cz, radius, height);
        buildBedCorner(world, cx, cy, cz, radius);
        buildSinkCorner(world, cx, cy, cz, radius);
        buildStorageCorner(world, cx, cy, cz, radius);
        buildChainsAndLights(world, cx, cy, cz, radius, height);

        if (radius >= 5) buildSecondCell(world, cx, cy, cz, radius, height);
        if (radius >= 7) buildGuardDesk(world, cx, cy, cz, radius);
        if (radius >= 9) buildLargeCellDetails(world, cx, cy, cz, radius);
    }

    private static void buildMainCellBars(World world, int cx, int cy, int cz, int radius, int height) {
        int z = cz - radius + 1;
        for (int x = cx - Math.min(3, radius - 1); x <= cx + Math.min(3, radius - 1); x++) {
            for (int y = cy + 1; y <= cy + Math.min(3, height - 1); y++) {
                world.getBlockAt(x, y, z).setType(Material.IRON_BARS, false);
            }
        }
        world.getBlockAt(cx, cy + 1, z).setType(Material.AIR, false);
        world.getBlockAt(cx, cy + 2, z).setType(Material.AIR, false);
    }

    private static void buildBedCorner(World world, int cx, int cy, int cz, int radius) {
        int x = cx - radius + 2;
        int z = cz + radius - 2;
        world.getBlockAt(x, cy + 1, z).setType(Material.GRAY_BED, false);
        if (radius >= 4) world.getBlockAt(x + 1, cy + 1, z).setType(Material.GRAY_CARPET, false);
        if (radius >= 5) world.getBlockAt(x, cy + 1, z - 1).setType(Material.BARREL, false);
    }

    private static void buildSinkCorner(World world, int cx, int cy, int cz, int radius) {
        int x = cx + radius - 2;
        int z = cz + radius - 2;
        world.getBlockAt(x, cy + 1, z).setType(Material.CAULDRON, false);
        if (radius >= 4) world.getBlockAt(x - 1, cy + 1, z).setType(Material.IRON_TRAPDOOR, false);
        if (radius >= 5) world.getBlockAt(x, cy + 2, z - 1).setType(Material.CHAIN, false);
    }

    private static void buildStorageCorner(World world, int cx, int cy, int cz, int radius) {
        int x = cx - radius + 2;
        int z = cz - radius + 2;
        world.getBlockAt(x, cy + 1, z).setType(Material.CHEST, false);
        if (radius >= 4) world.getBlockAt(x + 1, cy + 1, z).setType(Material.BARREL, false);
        if (radius >= 5) world.getBlockAt(x, cy + 2, z + 1).setType(Material.CHAIN, false);
    }

    private static void buildChainsAndLights(World world, int cx, int cy, int cz, int radius, int height) {
        int roof = cy + Math.max(3, height - 1);
        world.getBlockAt(cx, roof, cz).setType(Material.SOUL_LANTERN, false);
        if (radius >= 4) {
            world.getBlockAt(cx - radius + 2, roof, cz).setType(Material.CHAIN, false);
            world.getBlockAt(cx + radius - 2, roof, cz).setType(Material.CHAIN, false);
        }
        if (radius >= 6) {
            world.getBlockAt(cx, roof, cz - radius + 2).setType(Material.CHAIN, false);
            world.getBlockAt(cx, roof, cz + radius - 2).setType(Material.CHAIN, false);
        }
    }

    private static void buildSecondCell(World world, int cx, int cy, int cz, int radius, int height) {
        int x = cx + radius - 2;
        for (int z = cz - 2; z <= cz + 2; z++) {
            for (int y = cy + 1; y <= cy + Math.min(3, height - 1); y++) {
                world.getBlockAt(x, y, z).setType(Material.IRON_BARS, false);
            }
        }
        world.getBlockAt(x, cy + 1, cz).setType(Material.AIR, false);
        world.getBlockAt(x, cy + 2, cz).setType(Material.AIR, false);
        world.getBlockAt(x - 1, cy + 1, cz + 2).setType(Material.GRAY_BED, false);
    }

    private static void buildGuardDesk(World world, int cx, int cy, int cz, int radius) {
        int z = cz - radius + 3;
        world.getBlockAt(cx - 1, cy + 1, z).setType(Material.LECTERN, false);
        world.getBlockAt(cx, cy + 1, z).setType(Material.DARK_OAK_STAIRS, false);
        world.getBlockAt(cx + 1, cy + 1, z).setType(Material.BARREL, false);
        world.getBlockAt(cx + 2, cy + 1, z).setType(Material.REDSTONE_TORCH, false);
    }

    private static void buildLargeCellDetails(World world, int cx, int cy, int cz, int radius) {
        world.getBlockAt(cx - 2, cy + 1, cz + radius - 3).setType(Material.COBWEB, false);
        world.getBlockAt(cx + 2, cy + 1, cz + radius - 3).setType(Material.COBWEB, false);
        world.getBlockAt(cx - 2, cy + 1, cz - radius + 3).setType(Material.IRON_BARS, false);
        world.getBlockAt(cx + 2, cy + 1, cz - radius + 3).setType(Material.IRON_BARS, false);
    }
}
