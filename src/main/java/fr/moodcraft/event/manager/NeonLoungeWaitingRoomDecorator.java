package fr.moodcraft.event.manager;

import org.bukkit.Material;
import org.bukkit.World;

public final class NeonLoungeWaitingRoomDecorator {

    private NeonLoungeWaitingRoomDecorator() {}

    public static void decorate(World world, int cx, int cy, int cz, int radius, int height) {
        buildCheckerFloor(world, cx, cy, cz, radius);
        buildMainBar(world, cx, cy, cz, radius);
        buildNeonWallPanels(world, cx, cy, cz, radius, height);
        buildCeilingFeature(world, cx, cy, cz, radius, height);
        buildCornerLightColumns(world, cx, cy, cz, radius, height);
        buildSideLounges(world, cx, cy, cz, radius);
        buildDisplayFrames(world, cx, cy, cz, radius);
    }

    private static void buildCheckerFloor(World world, int cx, int cy, int cz, int radius) {
        for (int x = cx - radius + 1; x <= cx + radius - 1; x++) {
            for (int z = cz - radius + 1; z <= cz + radius - 1; z++) {
                int dx = Math.abs(x - cx);
                int dz = Math.abs(z - cz);
                if (dx <= 1 && dz <= 1) {
                    world.getBlockAt(x, cy, z).setType(Material.SEA_LANTERN, false);
                } else if (dx == 0 || dz == 0) {
                    world.getBlockAt(x, cy, z).setType(Material.CRYING_OBSIDIAN, false);
                } else if (((x + z) & 1) == 0) {
                    world.getBlockAt(x, cy, z).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
                } else {
                    world.getBlockAt(x, cy, z).setType(Material.SMOOTH_QUARTZ, false);
                }
            }
        }
    }

    private static void buildMainBar(World world, int cx, int cy, int cz, int radius) {
        int barZ = cz - Math.max(2, radius / 3);
        int half = Math.max(4, radius / 2 + 1);

        for (int x = cx - half; x <= cx + half; x++) {
            world.getBlockAt(x, cy + 1, barZ).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            world.getBlockAt(x, cy + 2, barZ).setType((x & 1) == 0 ? Material.CRYING_OBSIDIAN : Material.SEA_LANTERN, false);
            world.getBlockAt(x, cy + 3, barZ).setType((x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
        }

        world.getBlockAt(cx - half - 1, cy + 1, barZ).setType(Material.AMETHYST_BLOCK, false);
        world.getBlockAt(cx + half + 1, cy + 1, barZ).setType(Material.AMETHYST_BLOCK, false);
        world.getBlockAt(cx - half - 1, cy + 2, barZ).setType(Material.END_ROD, false);
        world.getBlockAt(cx + half + 1, cy + 2, barZ).setType(Material.END_ROD, false);

        for (int offset = -6; offset <= 6; offset += 3) {
            buildNeonStool(world, cx + offset, cy, barZ + 3, offset % 2 == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS);
        }
    }

    private static void buildNeonWallPanels(World world, int cx, int cy, int cz, int radius, int height) {
        int panelTop = Math.min(cy + height - 1, cy + 4);
        for (int x = cx - radius + 2; x <= cx + radius - 2; x += 4) {
            buildPanel(world, x, cy + 1, cz - radius, true, panelTop, (x & 1) == 0 ? Material.LIME_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS);
            buildPanel(world, x, cy + 1, cz + radius, true, panelTop, (x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.PURPLE_STAINED_GLASS);
        }
        for (int z = cz - radius + 2; z <= cz + radius - 2; z += 4) {
            buildPanel(world, cx - radius, cy + 1, z, false, panelTop, (z & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS);
            buildPanel(world, cx + radius, cy + 1, z, false, panelTop, (z & 1) == 0 ? Material.LIME_STAINED_GLASS : Material.PURPLE_STAINED_GLASS);
        }
    }

    private static void buildPanel(World world, int x, int baseY, int z, boolean alongX, int topY, Material glass) {
        for (int y = baseY; y <= topY; y++) {
            for (int offset = -1; offset <= 1; offset++) {
                int px = alongX ? x + offset : x;
                int pz = alongX ? z : z + offset;
                world.getBlockAt(px, y, pz).setType(y == topY ? Material.SEA_LANTERN : glass, false);
            }
        }
    }

    private static void buildCeilingFeature(World world, int cx, int cy, int cz, int radius, int height) {
        int y = cy + height;
        int half = Math.min(4, Math.max(2, radius / 3));
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                int adx = Math.abs(dx);
                int adz = Math.abs(dz);
                if (adx == half || adz == half) {
                    world.getBlockAt(cx + dx, y, cz + dz).setType(Material.AMETHYST_BLOCK, false);
                } else if ((dx + dz) % 2 == 0) {
                    world.getBlockAt(cx + dx, y, cz + dz).setType(Material.SEA_LANTERN, false);
                } else {
                    world.getBlockAt(cx + dx, y, cz + dz).setType(Material.PURPLE_STAINED_GLASS, false);
                }
            }
        }
        for (int offset = -2; offset <= 2; offset += 2) {
            world.getBlockAt(cx + offset, y - 1, cz).setType(Material.END_ROD, false);
            world.getBlockAt(cx, y - 1, cz + offset).setType(Material.END_ROD, false);
        }
    }

    private static void buildCornerLightColumns(World world, int cx, int cy, int cz, int radius, int height) {
        int[][] corners = {
                {cx - radius + 1, cz - radius + 1},
                {cx + radius - 1, cz - radius + 1},
                {cx - radius + 1, cz + radius - 1},
                {cx + radius - 1, cz + radius - 1}
        };
        for (int[] point : corners) {
            for (int y = cy + 1; y <= cy + height - 1; y++) {
                world.getBlockAt(point[0], y, point[1]).setType((y & 1) == 0 ? Material.CRYING_OBSIDIAN : Material.SEA_LANTERN, false);
            }
            world.getBlockAt(point[0], cy + height, point[1]).setType(Material.AMETHYST_BLOCK, false);
        }
    }

    private static void buildSideLounges(World world, int cx, int cy, int cz, int radius) {
        int leftX = cx - Math.max(3, radius / 2);
        int rightX = cx + Math.max(3, radius / 2);
        int z = cz + Math.max(2, radius / 3);

        buildBench(world, leftX, cy, z, true, Material.CYAN_STAINED_GLASS);
        buildBench(world, rightX, cy, z, true, Material.MAGENTA_STAINED_GLASS);
        buildBench(world, cx - 3, cy, cz + radius - 3, false, Material.LIME_STAINED_GLASS);
        buildBench(world, cx + 3, cy, cz + radius - 3, false, Material.PURPLE_STAINED_GLASS);
    }

    private static void buildBench(World world, int x, int cy, int z, boolean alongZ, Material glass) {
        for (int i = -1; i <= 1; i++) {
            int bx = alongZ ? x : x + i;
            int bz = alongZ ? z + i : z;
            world.getBlockAt(bx, cy + 1, bz).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            world.getBlockAt(bx, cy + 2, bz).setType(glass, false);
        }
    }

    private static void buildDisplayFrames(World world, int cx, int cy, int cz, int radius) {
        int z = cz + radius - 1;
        for (int x = cx - 4; x <= cx + 4; x += 2) {
            world.getBlockAt(x, cy + 2, z).setType((x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 3, z).setType(Material.SEA_LANTERN, false);
        }
        world.getBlockAt(cx - 5, cy + 1, cz).setType(Material.FLOWER_POT, false);
        world.getBlockAt(cx + 5, cy + 1, cz).setType(Material.FLOWER_POT, false);
        world.getBlockAt(cx - 5, cy + 2, cz).setType(Material.CACTUS, false);
        world.getBlockAt(cx + 5, cy + 2, cz).setType(Material.AZALEA, false);
    }

    private static void buildNeonStool(World world, int x, int cy, int z, Material seat) {
        world.getBlockAt(x, cy + 1, z).setType(Material.END_ROD, false);
        world.getBlockAt(x, cy + 2, z).setType(seat, false);
    }
}
