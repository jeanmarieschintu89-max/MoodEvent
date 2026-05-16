package fr.moodcraft.event.generator;

import org.bukkit.Material;
import org.bukkit.World;

public final class SquidPremiumLayoutBuilder {

    private SquidPremiumLayoutBuilder() {
    }

    public static void build(World world, int cx, int cy, int cz) {
        if (world == null) return;
        buildDormitory(world, cx, cy, cz);
        buildStartSas(world, cx, cy, cz);
        buildRedGreenArena(world, cx, cy, cz);
        buildBridgeStartRoom(world, cx, cy, cz);
        buildGlassBridge(world, cx, cy, cz);
        buildBridgeFinishRoom(world, cx, cy, cz);
    }

    private static void buildDormitory(World w, int cx, int cy, int cz) {
        room(w, cx - 92, cx - 60, cy, cy + 14, cz - 13, cz + 13,
                Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.LIGHT_GRAY_STAINED_GLASS);

        for (int x = cx - 89; x <= cx - 65; x += 6) {
            bunk(w, x, cy + 1, cz - 10);
            bunk(w, x, cy + 1, cz + 8);
        }
        for (int z = cz - 6; z <= cz + 6; z += 4) {
            bunk(w, cx - 89, cy + 1, z);
            bunk(w, cx - 65, cy + 1, z);
        }

        platform(w, cx - 76, cy, cz, 4, Material.RED_CONCRETE);
        w.getBlockAt(cx - 76, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        w.getBlockAt(cx - 76, cy + 10, cz).setType(Material.SEA_LANTERN, false);
        symbol(w, cx - 90, cy + 7, cz - 11, Material.LIME_CONCRETE);
        symbol(w, cx - 62, cy + 7, cz + 9, Material.RED_CONCRETE);

        for (int z = cz - 4; z <= cz + 4; z++) {
            for (int y = cy + 1; y <= cy + 5; y++) {
                w.getBlockAt(cx - 60, y, z).setType(Material.PINK_CONCRETE, false);
            }
        }
        w.getBlockAt(cx - 60, cy + 6, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void buildStartSas(World w, int cx, int cy, int cz) {
        room(w, cx - 46, cx - 34, cy, cy + 8, cz - 5, cz + 5,
                Material.SMOOTH_QUARTZ, Material.PINK_CONCRETE, Material.RED_STAINED_GLASS);
        platform(w, cx - 40, cy, cz, 2, Material.MAGENTA_CONCRETE);
        w.getBlockAt(cx - 40, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        openDoorX(w, cx - 34, cy, cz, 3);
        for (int y = cy + 1; y <= cy + 6; y++) {
            w.getBlockAt(cx - 34, y, cz - 4).setType(Material.PURPLE_CONCRETE, false);
            w.getBlockAt(cx - 34, y, cz + 4).setType(Material.PURPLE_CONCRETE, false);
        }
    }

    private static void buildRedGreenArena(World w, int cx, int cy, int cz) {
        for (int x = cx - 33; x <= cx + 45; x++) {
            for (int z = cz - 11; z <= cz + 11; z++) {
                w.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.LIME_CONCRETE : Material.GREEN_CONCRETE, false);
                w.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
            wallLine(w, x, cy, cz - 12, Material.WHITE_CONCRETE);
            wallLine(w, x, cy, cz + 12, Material.WHITE_CONCRETE);
        }
        for (int z = cz - 11; z <= cz + 11; z++) {
            w.getBlockAt(cx - 33, cy, z).setType(Material.LIME_CONCRETE, false);
            w.getBlockAt(cx - 32, cy, z).setType(Material.LIME_CONCRETE, false);
            w.getBlockAt(cx - 33, cy + 1, z).setType(Material.AIR, false);
            w.getBlockAt(cx - 32, cy + 1, z).setType(Material.AIR, false);
            w.getBlockAt(cx + 38, cy, z).setType(Material.RED_CONCRETE, false);
            w.getBlockAt(cx + 39, cy, z).setType(Material.RED_CONCRETE, false);
            w.getBlockAt(cx + 38, cy + 1, z).setType(Material.AIR, false);
            w.getBlockAt(cx + 39, cy + 1, z).setType(Material.AIR, false);
        }
        for (int x = cx - 33; x <= cx + 45; x += 6) {
            w.getBlockAt(x, cy + 2, cz - 12).setType(Material.SEA_LANTERN, false);
            w.getBlockAt(x, cy + 2, cz + 12).setType(Material.SEA_LANTERN, false);
        }
        buildDoll(w, cx + 46, cy, cz);
        trafficLights(w, cx + 42, cy, cz);
    }

    private static void buildBridgeStartRoom(World w, int cx, int cy, int cz) {
        int z = cz + 46;
        room(w, cx - 31, cx - 22, cy + 1, cy + 8, z - 7, z + 7,
                Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.PINK_STAINED_GLASS);
        openDoorX(w, cx - 22, cy + 1, z, 3);
        platform(w, cx - 24, cy + 1, z, 3, Material.LIME_CONCRETE);
        w.getBlockAt(cx - 24, cy + 2, z).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        w.getBlockAt(cx - 24, cy + 7, z).setType(Material.SEA_LANTERN, false);
    }

    private static void buildGlassBridge(World w, int cx, int cy, int cz) {
        int zLeft = cz + 44;
        int zRight = cz + 48;
        for (int i = 0; i < 12; i++) {
            int x = cx - 21 + (i * 3);
            platform(w, x, cy + 1, zLeft, 1, Material.GLASS);
            platform(w, x, cy + 1, zRight, 1, Material.GLASS);
            w.getBlockAt(x, cy, zLeft).setType(Material.SEA_LANTERN, false);
            w.getBlockAt(x, cy, zRight).setType(Material.SEA_LANTERN, false);
        }
        for (int x = cx - 25; x <= cx + 18; x++) {
            w.getBlockAt(x, cy + 2, cz + 39).setType(Material.PURPLE_STAINED_GLASS, false);
            w.getBlockAt(x, cy + 2, cz + 53).setType(Material.PURPLE_STAINED_GLASS, false);
            if (x % 4 == 0) {
                w.getBlockAt(x, cy + 3, cz + 39).setType(Material.SEA_LANTERN, false);
                w.getBlockAt(x, cy + 3, cz + 53).setType(Material.SEA_LANTERN, false);
            }
        }
    }

    private static void buildBridgeFinishRoom(World w, int cx, int cy, int cz) {
        int x = cx + 20;
        int z = cz + 46;
        room(w, x - 5, x + 8, cy + 1, cy + 8, z - 7, z + 7,
                Material.GOLD_BLOCK, Material.RED_CONCRETE, Material.RED_STAINED_GLASS);
        openDoorX(w, x - 5, cy + 1, z, 3);
        platform(w, x, cy + 1, z, 3, Material.GOLD_BLOCK);
        w.getBlockAt(x, cy + 2, z).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        w.getBlockAt(x, cy + 7, z).setType(Material.SEA_LANTERN, false);
        symbol(w, x + 5, cy + 4, z - 5, Material.GOLD_BLOCK);
    }

    private static void room(World w, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Material floor, Material wall, Material glass) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                    boolean ground = y == minY;
                    boolean roof = y == maxY;
                    boolean window = edge && (y == minY + 4 || y == minY + 5);
                    if (ground) w.getBlockAt(x, y, z).setType(floor, false);
                    else if (roof) w.getBlockAt(x, y, z).setType(Material.SMOOTH_QUARTZ, false);
                    else if (window) w.getBlockAt(x, y, z).setType(glass, false);
                    else if (edge) w.getBlockAt(x, y, z).setType(wall, false);
                    else w.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openDoorX(World w, int x, int y, int z, int width) {
        for (int dz = -width; dz <= width; dz++) for (int dy = 1; dy <= 5; dy++) w.getBlockAt(x, y + dy, z + dz).setType(Material.AIR, false);
    }

    private static void bunk(World w, int x, int y, int z) {
        for (int level = 0; level < 3; level++) {
            int by = y + level * 2;
            w.getBlockAt(x, by, z).setType(Material.RED_BED, false);
            w.getBlockAt(x + 1, by, z).setType(Material.RED_BED, false);
            w.getBlockAt(x, by + 1, z).setType(Material.IRON_TRAPDOOR, false);
            w.getBlockAt(x + 1, by + 1, z).setType(Material.IRON_TRAPDOOR, false);
        }
        for (int dy = 0; dy <= 5; dy++) w.getBlockAt(x + 2, y + dy, z).setType(Material.LADDER, false);
    }

    private static void buildDoll(World w, int x, int y, int z) {
        for (int dy = 1; dy <= 4; dy++) w.getBlockAt(x, y + dy, z).setType(Material.ORANGE_CONCRETE, false);
        w.getBlockAt(x, y + 5, z).setType(Material.YELLOW_CONCRETE, false);
        w.getBlockAt(x, y + 6, z).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x, y + 5, z - 1).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x, y + 5, z + 1).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x - 1, y + 3, z).setType(Material.YELLOW_CONCRETE, false);
        w.getBlockAt(x + 1, y + 3, z).setType(Material.YELLOW_CONCRETE, false);
        w.getBlockAt(x, y, z).setType(Material.OAK_LOG, false);
    }

    private static void trafficLights(World w, int x, int y, int z) {
        for (int dy = 1; dy <= 5; dy++) w.getBlockAt(x, y + dy, z).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x, y + 6, z - 1).setType(Material.LIME_CONCRETE, false);
        w.getBlockAt(x, y + 6, z + 1).setType(Material.RED_CONCRETE, false);
        w.getBlockAt(x, y + 7, z).setType(Material.SEA_LANTERN, false);
    }

    private static void wallLine(World w, int x, int cy, int z, Material material) {
        for (int y = cy + 1; y <= cy + 3; y++) w.getBlockAt(x, y, z).setType(material, false);
    }

    private static void symbol(World w, int x, int y, int z, Material material) {
        w.getBlockAt(x, y, z).setType(material, false);
        w.getBlockAt(x, y + 1, z).setType(material, false);
        w.getBlockAt(x, y, z + 1).setType(material, false);
        w.getBlockAt(x, y + 1, z + 1).setType(material, false);
    }

    private static void platform(World w, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) w.getBlockAt(x, cy, z).setType(material, false);
    }
}
