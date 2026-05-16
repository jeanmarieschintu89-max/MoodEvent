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
        // Dortoir blanc, rose et lits empilés façon rayonnages.
        for (int x = cx - 53; x <= cx - 33; x++) {
            for (int z = cz - 16; z <= cz - 9; z++) {
                if ((x + z) % 2 == 0) world.getBlockAt(x, cy, z).setType(Material.WHITE_CONCRETE, false);
                else world.getBlockAt(x, cy, z).setType(Material.LIGHT_GRAY_CONCRETE, false);
            }
        }
        for (int x = cx - 53; x <= cx - 33; x += 5) {
            bunkTower(world, x, cy + 1, cz - 16);
            bunkTower(world, x, cy + 1, cz - 9);
        }
        symbol(world, cx - 53, cy + 5, cz - 12, Material.RED_CONCRETE);
        symbol(world, cx - 33, cy + 5, cz - 13, Material.LIGHT_BLUE_CONCRETE);
        world.getBlockAt(cx - 43, cy + 8, cz - 13).setType(Material.SEA_LANTERN, false);
    }

    private static void buildColorStairCorridor(World world, int cx, int cy, int cz) {
        // Couloir pastel inspiré des escaliers impossibles, lisible en Minecraft.
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
        // Terrain enfantin, clair et ouvert.
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
        // Pont de verre plus show : rails, lumières et scène sombre.
        for (int x = cx - 8; x <= cx + 30; x++) {
            world.getBlockAt(x, cy + 2, cz + 7).setType(Material.PURPLE_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 2, cz + 16).setType(Material.PURPLE_STAINED_GLASS, false);
            if (x % 4 == 0) {
                world.getBlockAt(x, cy + 3, cz + 7).setType(Material.SEA_LANTERN, false);
                world.getBlockAt(x, cy + 3, cz + 16).setType(Material.SEA_LANTERN, false);
            }
        }
        platform(world, cx + 28, cy + 1, cz + 11, 4, Material.GOLD_BLOCK);
        world.getBlockAt(cx + 28, cy + 2, cz + 11).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void buildDoll(World world, int x, int y, int z) {
        // Poupée Young-hee version blocs : corps orange/jaune, tête claire, cheveux noirs.
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
