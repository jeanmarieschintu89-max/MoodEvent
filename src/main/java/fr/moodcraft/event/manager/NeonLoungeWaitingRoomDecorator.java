package fr.moodcraft.event.manager;

import org.bukkit.Material;
import org.bukkit.World;

public final class NeonLoungeWaitingRoomDecorator {

    private NeonLoungeWaitingRoomDecorator() {}

    public static void decorate(World world, int cx, int cy, int cz, int radius, int height) {
        buildBar(world, cx, cy, cz, radius);
        buildNeonWallPanels(world, cx, cy, cz, radius);
        buildCeilingFeature(world, cx, cy, cz, height);
        buildCornerLightColumns(world, cx, cy, cz, radius, height);
        buildSideDecor(world, cx, cy, cz, radius);
    }

    private static void buildBar(World world, int cx, int cy, int cz, int radius) {
        int barZ = cz - Math.max(2, radius / 3);
        int half = Math.max(3, radius / 2);
        for (int x = cx - half; x <= cx + half; x++) {
            world.getBlockAt(x, cy + 1, barZ).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            world.getBlockAt(x, cy + 2, barZ).setType((x & 1) == 0 ? Material.CRYING_OBSIDIAN : Material.SEA_LANTERN, false);
        }
        for (int offset = -4; offset <= 4; offset += 4) {
            buildNeonStool(world, cx + offset, cy, barZ + 3, Material.CYAN_STAINED_GLASS);
            buildNeonStool(world, cx + offset + 2, cy, barZ + 3, Material.MAGENTA_STAINED_GLASS);
        }
        world.getBlockAt(cx, cy + 3, barZ).setType(Material.AMETHYST_BLOCK, false);
        world.getBlockAt(cx, cy + 4, barZ).setType(Material.END_ROD, false);
    }

    private static void buildNeonWallPanels(World world, int cx, int cy, int cz, int radius) {
        for (int x = cx - radius + 2; x <= cx + radius - 2; x += 3) {
            world.getBlockAt(x, cy + 2, cz - radius).setType((x & 1) == 0 ? Material.LIME_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 3, cz - radius).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(x, cy + 2, cz + radius).setType((x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.PURPLE_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 3, cz + radius).setType(Material.SEA_LANTERN, false);
        }
        for (int z = cz - radius + 2; z <= cz + radius - 2; z += 3) {
            world.getBlockAt(cx - radius, cy + 2, z).setType((z & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
            world.getBlockAt(cx - radius, cy + 3, z).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + radius, cy + 2, z).setType((z & 1) == 0 ? Material.LIME_STAINED_GLASS : Material.PURPLE_STAINED_GLASS, false);
            world.getBlockAt(cx + radius, cy + 3, z).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void buildCeilingFeature(World world, int cx, int cy, int cz, int height) {
        int y = cy + height;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    world.getBlockAt(cx + dx, y, cz + dz).setType(Material.AMETHYST_BLOCK, false);
                }
            }
        }
        world.getBlockAt(cx, y - 1, cz).setType(Material.END_ROD, false);
        world.getBlockAt(cx + 1, y - 1, cz).setType(Material.END_ROD, false);
        world.getBlockAt(cx - 1, y - 1, cz).setType(Material.END_ROD, false);
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
        }
    }

    private static void buildSideDecor(World world, int cx, int cy, int cz, int radius) {
        int z = cz + radius - 1;
        for (int x = cx - 4; x <= cx + 4; x += 2) {
            world.getBlockAt(x, cy + 2, z).setType((x & 1) == 0 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 3, z).setType(Material.SEA_LANTERN, false);
        }
        world.getBlockAt(cx - 5, cy + 1, cz).setType(Material.FLOWER_POT, false);
        world.getBlockAt(cx + 5, cy + 1, cz).setType(Material.FLOWER_POT, false);
    }

    private static void buildNeonStool(World world, int x, int cy, int z, Material seat) {
        world.getBlockAt(x, cy + 1, z).setType(Material.END_ROD, false);
        world.getBlockAt(x, cy + 2, z).setType(seat, false);
    }
}
