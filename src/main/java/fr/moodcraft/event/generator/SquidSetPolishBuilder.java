package fr.moodcraft.event.generator;

import org.bukkit.Material;
import org.bukkit.World;

public final class SquidSetPolishBuilder {

    private SquidSetPolishBuilder() {
    }

    public static void polish(World world, int cx, int cy, int cz) {
        if (world == null) return;
        polishDormitory(world, cx, cy, cz);
        buildColorStairCorridor(world, cx, cy, cz);
        polishRedGreenArena(world, cx, cy, cz);
        polishGlassBridge(world, cx, cy, cz);
    }

    private static void polishDormitory(World world, int cx, int cy, int cz) {
        int minX = cx - 55;
        int maxX = cx - 25;
        int minZ = cz - 18;
        int maxZ = cz - 4;
        int minY = cy;
        int maxY = cy + 12;

        // Grande salle fermée : sol, murs, vitres hautes, plafond.
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean floor = y == minY;
                    boolean roof = y == maxY;
                    boolean wallX = x == minX || x == maxX;
                    boolean wallZ = z == minZ || z == maxZ;
                    boolean wall = wallX || wallZ;
                    boolean window = wall && (y == minY + 5 || y == minY + 6 || y == minY + 7);

                    if (floor) world.getBlockAt(x, y, z).setType((x + z) % 2 == 0 ? Material.WHITE_CONCRETE : Material.LIGHT_GRAY_CONCRETE, false);
                    else if (roof) world.getBlockAt(x, y, z).setType((x == (minX + maxX) / 2 || z == (minZ + maxZ) / 2) ? Material.SEA_LANTERN : Material.SMOOTH_QUARTZ, false);
                    else if (window) world.getBlockAt(x, y, z).setType(Material.LIGHT_GRAY_STAINED_GLASS, false);
                    else if (wall) world.getBlockAt(x, y, z).setType(y % 2 == 0 ? Material.WHITE_CONCRETE : Material.PINK_CONCRETE, false);
                    else world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }

        // Sortie contrôlée vers le couloir, pas un trou aléatoire.
        openDoor(world, maxX, cy, cz - 11, false);
        for (int y = cy + 1; y <= cy + 5; y++) {
            world.getBlockAt(maxX, y, cz - 13).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(maxX, y, cz - 9).setType(Material.RED_CONCRETE, false);
        }
        for (int z = cz - 13; z <= cz - 9; z++) world.getBlockAt(maxX, cy + 6, z).setType(Material.SEA_LANTERN, false);

        // Lits empilés façon dortoir/entrepôt.
        for (int x = minX + 3; x <= maxX - 6; x += 6) {
            bunkTower(world, x, cy + 1, minZ + 2);
            bunkTower(world, x, cy + 1, maxZ - 2);
        }
        for (int z = minZ + 4; z <= maxZ - 4; z += 4) {
            bunkTower(world, minX + 3, cy + 1, z);
            bunkTower(world, maxX - 5, cy + 1, z);
        }

        // Zone centrale de briefing.
        platform(world, cx - 40, cy, cz - 11, 3, Material.RED_CONCRETE);
        world.getBlockAt(cx - 40, cy + 1, cz - 11).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(cx - 40, cy + 8, cz - 11).setType(Material.SEA_LANTERN, false);

        // Symboles stylisés sur les murs.
        symbol(world, minX + 1, cy + 5, cz - 14, Material.RED_CONCRETE);
        symbol(world, maxX - 2, cy + 5, cz - 8, Material.LIGHT_BLUE_CONCRETE);
        symbol(world, cx - 40, cy + 5, minZ + 1, Material.LIME_CONCRETE);
    }

    private static void buildColorStairCorridor(World world, int cx, int cy, int cz) {
        for (int x = cx - 48; x <= cx - 38; x++) {
            for (int z = cz - 7; z <= cz - 2; z++) {
                world.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.PINK_CONCRETE : Material.LIGHT_BLUE_CONCRETE, false);
                for (int y = cy + 1; y <= cy + 4; y++) {
                    boolean wall = z == cz - 7 || z == cz - 2;
                    if (wall) world.getBlockAt(x, y, z).setType(y % 2 == 0 ? Material.MAGENTA_CONCRETE : Material.CYAN_CONCRETE, false);
                    else world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
            if ((x - (cx - 48)) % 3 == 0) world.getBlockAt(x, cy + 1, cz - 4).setType(Material.QUARTZ_STAIRS, false);
        }
    }

    private static void polishRedGreenArena(World world, int cx, int cy, int cz) {
        int finishX = cx - 10;
        for (int x = cx - 48; x <= cx - 8; x++) {
            for (int z = cz - 5; z <= cz + 5; z++) {
                if (z == cz - 5 || z == cz + 5) world.getBlockAt(x, cy + 1, z).setType(Material.OAK_FENCE, false);
                if (x % 8 == 0 && Math.abs(z - cz) == 4) world.getBlockAt(x, cy + 1, z).setType(Material.LANTERN, false);
            }
        }
        for (int z = cz - 5; z <= cz + 5; z++) world.getBlockAt(finishX, cy + 1, z).setType(Material.RED_CONCRETE, false);
        buildDoll(world, finishX + 6, cy, cz);
        trafficLights(world, finishX + 3, cy, cz);
    }

    private static void polishGlassBridge(World world, int cx, int cy, int cz) {
        int zLeft = cz + 10;
        int zRight = cz + 13;
        int centerZ = cz + 11;

        buildBridgeStartRoom(world, cx, cy, centerZ);

        for (int x = cx - 1; x <= cx + 30; x++) {
            world.getBlockAt(x, cy + 2, cz + 7).setType(Material.PURPLE_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 2, cz + 16).setType(Material.PURPLE_STAINED_GLASS, false);
            if (x % 4 == 0) {
                world.getBlockAt(x, cy + 3, cz + 7).setType(Material.SEA_LANTERN, false);
                world.getBlockAt(x, cy + 3, cz + 16).setType(Material.SEA_LANTERN, false);
            }
        }

        platform(world, cx - 5, cy + 1, centerZ, 4, Material.LIME_CONCRETE);
        laneArrow(world, cx - 3, cy + 2, zLeft, Material.LIGHT_BLUE_CONCRETE);
        laneArrow(world, cx - 3, cy + 2, zRight, Material.MAGENTA_CONCRETE);
        buildBridgeFinishStage(world, cx, cy, centerZ);
    }

    private static void buildBridgeStartRoom(World world, int cx, int cy, int centerZ) {
        int minX = cx - 10;
        int maxX = cx - 3;
        int minZ = centerZ - 4;
        int maxZ = centerZ + 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, cy + 1, z).setType((x + z) % 2 == 0 ? Material.WHITE_CONCRETE : Material.LIGHT_GRAY_CONCRETE, false);
                for (int y = cy + 2; y <= cy + 5; y++) {
                    boolean back = x == minX;
                    boolean side = z == minZ || z == maxZ;
                    boolean front = x == maxX;
                    if (back || side) world.getBlockAt(x, y, z).setType(y == cy + 4 ? Material.PINK_STAINED_GLASS : Material.PINK_CONCRETE, false);
                    else if (front && (z <= centerZ - 2 || z >= centerZ + 2)) world.getBlockAt(x, y, z).setType(Material.CYAN_CONCRETE, false);
                    else world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }

        for (int y = cy + 2; y <= cy + 5; y++) {
            world.getBlockAt(maxX, y, centerZ - 2).setType(Material.PURPLE_CONCRETE, false);
            world.getBlockAt(maxX, y, centerZ + 2).setType(Material.PURPLE_CONCRETE, false);
        }
        for (int z = centerZ - 2; z <= centerZ + 2; z++) world.getBlockAt(maxX, cy + 5, z).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(cx - 5, cy + 2, centerZ).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void buildBridgeFinishStage(World world, int cx, int cy, int centerZ) {
        int finishX = cx + 28;
        platform(world, finishX, cy + 1, centerZ, 4, Material.GOLD_BLOCK);
        for (int z = centerZ - 4; z <= centerZ + 4; z++) {
            world.getBlockAt(finishX + 4, cy + 2, z).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(finishX + 4, cy + 3, z).setType(Material.RED_STAINED_GLASS, false);
            world.getBlockAt(finishX + 4, cy + 4, z).setType(Material.RED_CONCRETE, false);
        }
        for (int y = cy + 2; y <= cy + 5; y++) {
            world.getBlockAt(finishX - 4, y, centerZ - 4).setType(Material.GOLD_BLOCK, false);
            world.getBlockAt(finishX - 4, y, centerZ + 4).setType(Material.GOLD_BLOCK, false);
            world.getBlockAt(finishX + 4, y, centerZ - 4).setType(Material.GOLD_BLOCK, false);
            world.getBlockAt(finishX + 4, y, centerZ + 4).setType(Material.GOLD_BLOCK, false);
        }
        world.getBlockAt(finishX, cy + 2, centerZ).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(finishX, cy + 5, centerZ).setType(Material.SEA_LANTERN, false);
    }

    private static void openDoor(World world, int x, int y, int z, boolean alongZ) {
        for (int dy = 1; dy <= 5; dy++) {
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

    private static void laneArrow(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
        world.getBlockAt(x - 1, y, z - 1).setType(material, false);
        world.getBlockAt(x - 1, y, z + 1).setType(material, false);
    }

    private static void buildDoll(World world, int x, int y, int z) {
        for (int dy = 1; dy <= 3; dy++) world.getBlockAt(x, y + dy, z).setType(Material.ORANGE_CONCRETE, false);
        world.getBlockAt(x, y + 4, z).setType(Material.YELLOW_CONCRETE, false);
        world.getBlockAt(x, y + 5, z).setType(Material.BLACK_CONCRETE, false);
        world.getBlockAt(x, y + 4, z - 1).setType(Material.BLACK_CONCRETE, false);
        world.getBlockAt(x, y + 4, z + 1).setType(Material.BLACK_CONCRETE, false);
        world.getBlockAt(x - 1, y + 2, z).setType(Material.YELLOW_CONCRETE, false);
        world.getBlockAt(x + 1, y + 2, z).setType(Material.YELLOW_CONCRETE, false);
        world.getBlockAt(x, y, z).setType(Material.OAK_LOG, false);
    }

    private static void trafficLights(World world, int x, int y, int z) {
        for (int dy = 1; dy <= 4; dy++) world.getBlockAt(x, y + dy, z).setType(Material.BLACK_CONCRETE, false);
        world.getBlockAt(x, y + 5, z).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(x, y + 6, z).setType(Material.RED_CONCRETE, false);
        world.getBlockAt(x, y + 7, z).setType(Material.SEA_LANTERN, false);
    }

    private static void bunkTower(World world, int x, int y, int z) {
        for (int level = 0; level < 3; level++) {
            int by = y + level * 2;
            world.getBlockAt(x, by, z).setType(Material.RED_BED, false);
            world.getBlockAt(x + 1, by, z).setType(Material.RED_BED, false);
            world.getBlockAt(x, by + 1, z).setType(Material.IRON_TRAPDOOR, false);
            world.getBlockAt(x + 1, by + 1, z).setType(Material.IRON_TRAPDOOR, false);
        }
        for (int dy = 0; dy <= 5; dy++) world.getBlockAt(x + 2, y + dy, z).setType(Material.LADDER, false);
    }

    private static void symbol(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
        world.getBlockAt(x, y + 1, z).setType(material, false);
        world.getBlockAt(x, y, z + 1).setType(material, false);
        world.getBlockAt(x, y + 1, z + 1).setType(material, false);
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) world.getBlockAt(x, cy, z).setType(material, false);
        }
    }
}
