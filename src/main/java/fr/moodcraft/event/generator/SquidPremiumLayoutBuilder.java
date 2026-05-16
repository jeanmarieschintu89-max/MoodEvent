package fr.moodcraft.event.generator;

import org.bukkit.Material;
import org.bukkit.World;

public final class SquidPremiumLayoutBuilder {

    private SquidPremiumLayoutBuilder() {
    }

    public static void build(World world, int cx, int cy, int cz) {
        if (world == null) return;
        buildDormitory(world, cx, cy, cz);
        buildCorridor(world, cx, cy, cz);
        buildPreGameSas(world, cx, cy, cz);
        buildRedGreenArena(world, cx, cy, cz);
        buildBridgeStartRoom(world, cx, cy, cz);
        buildGlassBridge(world, cx, cy, cz);
        buildBridgeFinishRoom(world, cx, cy, cz);
    }

    private static void buildDormitory(World w, int cx, int cy, int cz) {
        room(w, cx - 78, cx - 52, cy, cy + 13, cz - 30, cz - 12,
                Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.LIGHT_GRAY_STAINED_GLASS);

        for (int x = cx - 75; x <= cx - 58; x += 6) {
            bunk(w, x, cy + 1, cz - 28);
            bunk(w, x, cy + 1, cz - 14);
        }
        for (int z = cz - 25; z <= cz - 17; z += 4) {
            bunk(w, cx - 75, cy + 1, z);
            bunk(w, cx - 59, cy + 1, z);
        }

        platform(w, cx - 65, cy, cz - 21, 3, Material.RED_CONCRETE);
        w.getBlockAt(cx - 65, cy + 1, cz - 21).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        w.getBlockAt(cx - 65, cy + 9, cz - 21).setType(Material.SEA_LANTERN, false);
        symbol(w, cx - 77, cy + 6, cz - 27, Material.LIME_CONCRETE);
        symbol(w, cx - 53, cy + 6, cz - 15, Material.RED_CONCRETE);
        openDoorX(w, cx - 52, cy, cz - 21, 4);
    }

    private static void buildCorridor(World w, int cx, int cy, int cz) {
        corridorX(w, cx - 51, cx - 38, cy, cz - 24, cz - 18);
        corridorZ(w, cx - 41, cx - 35, cy, cz - 18, cz - 5);
    }

    private static void buildPreGameSas(World w, int cx, int cy, int cz) {
        room(w, cx - 38, cx - 31, cy, cy + 7, cz - 4, cz + 4,
                Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.RED_STAINED_GLASS);
        platform(w, cx - 34, cy, cz, 2, Material.LIME_CONCRETE);
        openDoorX(w, cx - 31, cy, cz, 3);
        w.getBlockAt(cx - 34, cy + 6, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void buildRedGreenArena(World w, int cx, int cy, int cz) {
        for (int x = cx - 30; x <= cx + 8; x++) {
            for (int z = cz - 7; z <= cz + 7; z++) {
                w.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.LIME_CONCRETE : Material.GREEN_CONCRETE, false);
                w.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
            w.getBlockAt(x, cy + 1, cz - 8).setType(Material.OAK_FENCE, false);
            w.getBlockAt(x, cy + 1, cz + 8).setType(Material.OAK_FENCE, false);
        }
        for (int z = cz - 7; z <= cz + 7; z++) w.getBlockAt(cx + 6, cy + 1, z).setType(Material.RED_CONCRETE, false);
        buildDoll(w, cx + 14, cy, cz);
        trafficLights(w, cx + 10, cy, cz);
    }

    private static void buildBridgeStartRoom(World w, int cx, int cy, int cz) {
        int centerZ = cz + 14;
        room(w, cx - 12, cx - 4, cy + 1, cy + 7, centerZ - 5, centerZ + 5,
                Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.PINK_STAINED_GLASS);
        openDoorX(w, cx - 4, cy + 1, centerZ, 3);
        for (int y = cy + 2; y <= cy + 6; y++) {
            w.getBlockAt(cx - 4, y, centerZ - 3).setType(Material.PURPLE_CONCRETE, false);
            w.getBlockAt(cx - 4, y, centerZ + 3).setType(Material.PURPLE_CONCRETE, false);
        }
        platform(w, cx - 7, cy + 1, centerZ, 3, Material.LIME_CONCRETE);
        w.getBlockAt(cx - 7, cy + 2, centerZ).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void buildGlassBridge(World w, int cx, int cy, int cz) {
        int zLeft = cz + 12;
        int zRight = cz + 15;
        for (int i = 0; i < 10; i++) {
            int x = cx + (i * 3);
            platform(w, x, cy + 1, zLeft, 1, Material.GLASS);
            platform(w, x, cy + 1, zRight, 1, Material.GLASS);
            w.getBlockAt(x, cy, zLeft).setType(Material.SEA_LANTERN, false);
            w.getBlockAt(x, cy, zRight).setType(Material.SEA_LANTERN, false);
        }
        for (int x = cx - 2; x <= cx + 31; x++) {
            w.getBlockAt(x, cy + 2, cz + 8).setType(Material.PURPLE_STAINED_GLASS, false);
            w.getBlockAt(x, cy + 2, cz + 19).setType(Material.PURPLE_STAINED_GLASS, false);
            if (x % 4 == 0) {
                w.getBlockAt(x, cy + 3, cz + 8).setType(Material.SEA_LANTERN, false);
                w.getBlockAt(x, cy + 3, cz + 19).setType(Material.SEA_LANTERN, false);
            }
        }
    }

    private static void buildBridgeFinishRoom(World w, int cx, int cy, int cz) {
        int x = cx + 31;
        int z = cz + 14;
        room(w, x - 4, x + 5, cy + 1, cy + 7, z - 5, z + 5,
                Material.GOLD_BLOCK, Material.RED_CONCRETE, Material.RED_STAINED_GLASS);
        openDoorX(w, x - 4, cy + 1, z, 3);
        platform(w, x, cy + 1, z, 3, Material.GOLD_BLOCK);
        w.getBlockAt(x, cy + 2, z).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        w.getBlockAt(x, cy + 6, z).setType(Material.SEA_LANTERN, false);
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

    private static void corridorX(World w, int minX, int maxX, int cy, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            w.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.PINK_CONCRETE : Material.LIGHT_BLUE_CONCRETE, false);
            for (int y = cy + 1; y <= cy + 5; y++) w.getBlockAt(x, y, z).setType((z == minZ || z == maxZ) ? Material.MAGENTA_CONCRETE : Material.AIR, false);
        }
    }

    private static void corridorZ(World w, int minX, int maxX, int cy, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            w.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.PINK_CONCRETE : Material.LIGHT_BLUE_CONCRETE, false);
            for (int y = cy + 1; y <= cy + 5; y++) w.getBlockAt(x, y, z).setType((x == minX || x == maxX) ? Material.CYAN_CONCRETE : Material.AIR, false);
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
        for (int dy = 1; dy <= 3; dy++) w.getBlockAt(x, y + dy, z).setType(Material.ORANGE_CONCRETE, false);
        w.getBlockAt(x, y + 4, z).setType(Material.YELLOW_CONCRETE, false);
        w.getBlockAt(x, y + 5, z).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x, y + 4, z - 1).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x, y + 4, z + 1).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x - 1, y + 2, z).setType(Material.YELLOW_CONCRETE, false);
        w.getBlockAt(x + 1, y + 2, z).setType(Material.YELLOW_CONCRETE, false);
        w.getBlockAt(x, y, z).setType(Material.OAK_LOG, false);
    }

    private static void trafficLights(World w, int x, int y, int z) {
        for (int dy = 1; dy <= 4; dy++) w.getBlockAt(x, y + dy, z).setType(Material.BLACK_CONCRETE, false);
        w.getBlockAt(x, y + 5, z).setType(Material.LIME_CONCRETE, false);
        w.getBlockAt(x, y + 6, z).setType(Material.RED_CONCRETE, false);
        w.getBlockAt(x, y + 7, z).setType(Material.SEA_LANTERN, false);
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
