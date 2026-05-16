package fr.moodcraft.event.generator;

import org.bukkit.Material;
import org.bukkit.World;

public final class SquidMoodDecorBuilder {

    private SquidMoodDecorBuilder() {
    }

    public static void buildDormitory(World world, int cx, int cy, int cz) {
        room(world, cx - 54, cx - 32, cy, cy + 9, cz - 17, cz - 8, Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.LIGHT_GRAY_STAINED_GLASS);
        for (int x = cx - 52; x <= cx - 34; x += 4) {
            bedPod(world, x, cy + 1, cz - 15);
            bedPod(world, x, cy + 1, cz - 10);
        }
        platform(world, cx - 43, cy, cz - 13, 2, Material.RED_CONCRETE);
        openDoor(world, cx - 43, cy, cz - 8, true);
        world.getBlockAt(cx - 43, cy + 1, cz - 13).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(cx - 43, cy + 5, cz - 13).setType(Material.SEA_LANTERN, false);
    }

    public static void buildPreGameSas(World world, int cx, int cy, int cz) {
        room(world, cx - 55, cx - 49, cy, cy + 6, cz - 4, cz + 4, Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.RED_STAINED_GLASS);
        line(world, cx - 54, cy + 1, cz - 3, cx - 50, cz - 3, Material.IRON_BARS);
        line(world, cx - 54, cy + 1, cz + 3, cx - 50, cz + 3, Material.IRON_BARS);
        platform(world, cx - 52, cy, cz, 2, Material.LIME_CONCRETE);
        openDoor(world, cx - 49, cy, cz, false);
        world.getBlockAt(cx - 48, cy, cz).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(cx - 52, cy + 5, cz).setType(Material.SEA_LANTERN, false);
    }

    public static void buildReturnSas(World world, int cx, int cy, int cz) {
        room(world, cx - 12, cx - 4, cy, cy + 6, cz + 8, cz + 15, Material.LIGHT_GRAY_CONCRETE, Material.WHITE_CONCRETE, Material.CYAN_STAINED_GLASS);
        platform(world, cx - 8, cy, cz + 11, 2, Material.ORANGE_CONCRETE);
        openDoor(world, cx - 12, cy, cz + 11, false);
        openDoor(world, cx - 4, cy, cz + 11, false);
        world.getBlockAt(cx - 8, cy + 5, cz + 11).setType(Material.SEA_LANTERN, false);
    }

    private static void room(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Material floor, Material wall, Material glass) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                    boolean ground = y == minY;
                    boolean roof = y == maxY;
                    if (ground) world.getBlockAt(x, y, z).setType(floor, false);
                    else if (roof) world.getBlockAt(x, y, z).setType(Material.SMOOTH_QUARTZ_SLAB, false);
                    else if (edge && (y == minY + 2 || y == minY + 3)) world.getBlockAt(x, y, z).setType(glass, false);
                    else if (edge) world.getBlockAt(x, y, z).setType(wall, false);
                    else world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openDoor(World world, int x, int y, int z, boolean alongZ) {
        for (int dy = 1; dy <= 3; dy++) {
            world.getBlockAt(x, y + dy, z).setType(Material.AIR, false);
            if (alongZ) {
                world.getBlockAt(x - 1, y + dy, z).setType(Material.AIR, false);
                world.getBlockAt(x + 1, y + dy, z).setType(Material.AIR, false);
            } else {
                world.getBlockAt(x, y + dy, z - 1).setType(Material.AIR, false);
                world.getBlockAt(x, y + dy, z + 1).setType(Material.AIR, false);
            }
        }
    }

    private static void bedPod(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.RED_BED, false);
        world.getBlockAt(x, y + 1, z).setType(Material.IRON_TRAPDOOR, false);
        world.getBlockAt(x, y + 2, z).setType(Material.RED_BED, false);
        world.getBlockAt(x + 1, y, z).setType(Material.WHITE_CONCRETE, false);
        world.getBlockAt(x + 1, y + 1, z).setType(Material.LADDER, false);
        world.getBlockAt(x + 1, y + 2, z).setType(Material.WHITE_CONCRETE, false);
    }

    private static void line(World world, int x1, int y, int z1, int x2, int z2, Material material) {
        if (x1 != x2) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) world.getBlockAt(x, y, z1).setType(material, false);
        } else {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) world.getBlockAt(x1, y, z).setType(material, false);
        }
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }
}
