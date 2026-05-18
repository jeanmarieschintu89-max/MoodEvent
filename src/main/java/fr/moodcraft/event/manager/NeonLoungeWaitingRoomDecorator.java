package fr.moodcraft.event.manager;

import org.bukkit.Material;
import org.bukkit.World;

public final class NeonLoungeWaitingRoomDecorator {

    private NeonLoungeWaitingRoomDecorator() {}

    public static void decorate(World world, int cx, int cy, int cz, int radius, int height) {
        if (world == null || radius < 3) return;
        buildPremiumFloor(world, cx, cy, cz, radius);
        buildRealBar(world, cx, cy, cz, radius);
        buildBackBar(world, cx, cy, cz, radius, height);
        buildBarStools(world, cx, cy, cz, radius);
        buildNeonWallPanels(world, cx, cy, cz, radius, height);
        buildCeilingFeature(world, cx, cy, cz, radius, height);
        buildLoungeSeating(world, cx, cy, cz, radius);
        buildPlantersAndDetails(world, cx, cy, cz, radius);
    }

    private static void buildPremiumFloor(World world, int cx, int cy, int cz, int radius) {
        for (int x = cx - radius + 1; x <= cx + radius - 1; x++) {
            for (int z = cz - radius + 1; z <= cz + radius - 1; z++) {
                int dx = Math.abs(x - cx);
                int dz = Math.abs(z - cz);
                if (dx <= 1 && dz <= 1) world.getBlockAt(x, cy, z).setType(Material.SEA_LANTERN, false);
                else if (dx == 0 || dz == 0) world.getBlockAt(x, cy, z).setType(Material.CRYING_OBSIDIAN, false);
                else if (dx <= 2 && z > cz) world.getBlockAt(x, cy, z).setType(Material.POLISHED_BLACKSTONE, false);
                else world.getBlockAt(x, cy, z).setType(((x + z) & 1) == 0 ? Material.POLISHED_BLACKSTONE_BRICKS : Material.SMOOTH_QUARTZ, false);
            }
        }
    }

    private static void buildRealBar(World world, int cx, int cy, int cz, int radius) {
        int barZ = cz - radius + 3;
        int half = Math.max(2, Math.min(radius - 3, radius / 2 + 2));
        for (int x = cx - half; x <= cx + half; x++) {
            world.getBlockAt(x, cy + 1, barZ).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            world.getBlockAt(x, cy + 2, barZ).setType(Material.SMOOTH_QUARTZ, false);
            world.getBlockAt(x, cy + 1, barZ + 1).setType((x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
        }
        world.getBlockAt(cx - half - 1, cy + 1, barZ).setType(Material.CRYING_OBSIDIAN, false);
        world.getBlockAt(cx + half + 1, cy + 1, barZ).setType(Material.CRYING_OBSIDIAN, false);
        if (radius >= 6) {
            for (int z = barZ + 1; z <= barZ + 2; z++) {
                world.getBlockAt(cx - half, cy + 1, z).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
                world.getBlockAt(cx + half, cy + 1, z).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            }
        }
    }

    private static void buildBackBar(World world, int cx, int cy, int cz, int radius, int height) {
        int wallZ = cz - radius + 1;
        int shelfZ = wallZ + 1;
        int half = Math.max(2, Math.min(radius - 3, radius / 2 + 1));
        for (int x = cx - half; x <= cx + half; x++) {
            world.getBlockAt(x, cy + 1, shelfZ).setType(Material.DARK_OAK_PLANKS, false);
            world.getBlockAt(x, cy + 3, shelfZ).setType(Material.DARK_OAK_PLANKS, false);
            if ((x - cx) % 2 == 0) {
                world.getBlockAt(x, cy + 2, shelfZ).setType(Material.MAGENTA_STAINED_GLASS, false);
            } else {
                world.getBlockAt(x, cy + 2, shelfZ).setType(Material.CYAN_STAINED_GLASS, false);
            }
        }
        for (int x = cx - half; x <= cx + half; x += 3) {
            world.getBlockAt(x, cy + 4, shelfZ).setType(Material.SEA_LANTERN, false);
        }
        if (height >= 5) {
            for (int x = cx - half - 1; x <= cx + half + 1; x++) world.getBlockAt(x, cy + height - 1, wallZ).setType(Material.MAGENTA_STAINED_GLASS, false);
        }
    }

    private static void buildBarStools(World world, int cx, int cy, int cz, int radius) {
        int barZ = cz - radius + 6;
        int half = Math.max(2, Math.min(radius - 4, radius / 2 + 1));
        int step = radius <= 4 ? 3 : 2;
        for (int x = cx - half; x <= cx + half; x += step) {
            world.getBlockAt(x, cy + 1, barZ).setType(Material.END_ROD, false);
            world.getBlockAt(x, cy + 2, barZ).setType((x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
        }
    }

    private static void buildNeonWallPanels(World world, int cx, int cy, int cz, int radius, int height) {
        int panelTop = Math.min(cy + height - 1, cy + 4);
        for (int x = cx - radius + 2; x <= cx + radius - 2; x += 4) {
            buildPanel(world, x, cy + 1, cz + radius, true, panelTop, (x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS);
        }
        if (radius >= 6) {
            for (int z = cz - radius + 3; z <= cz + radius - 3; z += 4) {
                buildPanel(world, cx - radius, cy + 1, z, false, panelTop, Material.PURPLE_STAINED_GLASS);
                buildPanel(world, cx + radius, cy + 1, z, false, panelTop, Material.CYAN_STAINED_GLASS);
            }
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
                int adx = Math.abs(dx), adz = Math.abs(dz);
                if (adx == half || adz == half) world.getBlockAt(cx + dx, y, cz + dz).setType(Material.AMETHYST_BLOCK, false);
                else if ((dx + dz) % 2 == 0) world.getBlockAt(cx + dx, y, cz + dz).setType(Material.SEA_LANTERN, false);
                else world.getBlockAt(cx + dx, y, cz + dz).setType(Material.PURPLE_STAINED_GLASS, false);
            }
        }
    }

    private static void buildLoungeSeating(World world, int cx, int cy, int cz, int radius) {
        int backZ = cz + radius - 3;
        buildSofa(world, cx - Math.max(3, radius / 2), cy, backZ, true, Material.MAGENTA_STAINED_GLASS);
        buildSofa(world, cx + Math.max(3, radius / 2), cy, backZ, true, Material.CYAN_STAINED_GLASS);
        if (radius >= 6) {
            buildCoffeeTable(world, cx, cy, cz + radius - 4);
            buildSofa(world, cx - radius + 3, cy, cz, false, Material.PURPLE_STAINED_GLASS);
            buildSofa(world, cx + radius - 3, cy, cz, false, Material.LIME_STAINED_GLASS);
        }
    }

    private static void buildSofa(World world, int x, int cy, int z, boolean alongZ, Material accent) {
        for (int i = -1; i <= 1; i++) {
            int bx = alongZ ? x : x + i;
            int bz = alongZ ? z + i : z;
            world.getBlockAt(bx, cy + 1, bz).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            world.getBlockAt(bx, cy + 2, bz).setType(accent, false);
        }
    }

    private static void buildCoffeeTable(World world, int cx, int cy, int cz) {
        world.getBlockAt(cx - 1, cy + 1, cz).setType(Material.SMOOTH_QUARTZ, false);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(cx + 1, cy + 1, cz).setType(Material.SMOOTH_QUARTZ, false);
    }

    private static void buildPlantersAndDetails(World world, int cx, int cy, int cz, int radius) {
        if (radius >= 5) {
            buildPlanter(world, cx - radius + 2, cy, cz + 1);
            buildPlanter(world, cx + radius - 2, cy, cz + 1);
        }
        if (radius >= 7) {
            world.getBlockAt(cx - radius + 2, cy + 2, cz - 1).setType(Material.MAGENTA_STAINED_GLASS, false);
            world.getBlockAt(cx + radius - 2, cy + 2, cz - 1).setType(Material.CYAN_STAINED_GLASS, false);
        }
    }

    private static void buildPlanter(World world, int x, int cy, int z) {
        world.getBlockAt(x, cy + 1, z).setType(Material.DARK_OAK_PLANKS, false);
        world.getBlockAt(x, cy + 2, z).setType(Material.AZALEA_LEAVES, false);
    }
}
