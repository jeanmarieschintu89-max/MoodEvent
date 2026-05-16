package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public final class GeneratedGameOpeningFixer {

    private GeneratedGameOpeningFixer() {
    }

    public static void open(GeneratedGameType type, Location start, Location finish) {
        if (start != null) {
            openPlatform(start, type == GeneratedGameType.SURVIE_ETAGES ? 4 : 3);
            openCorridor(start, true);
        }
        if (finish != null) {
            openPlatform(finish, 3);
            openCorridor(finish, true);
        }
    }

    private static void openPlatform(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY() - 1;
        int cz = center.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(platformMaterial(center), false);
                for (int y = cy + 1; y <= cy + 4; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void openCorridor(Location center, boolean alongX) {
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY() - 1;
        int cz = center.getBlockZ();

        if (alongX) {
            for (int x = cx - 6; x <= cx + 6; x++) {
                for (int z = cz - 2; z <= cz + 2; z++) {
                    world.getBlockAt(x, cy, z).setType(platformMaterial(center), false);
                    for (int y = cy + 1; y <= cy + 4; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        } else {
            for (int z = cz - 6; z <= cz + 6; z++) {
                for (int x = cx - 2; x <= cx + 2; x++) {
                    world.getBlockAt(x, cy, z).setType(platformMaterial(center), false);
                    for (int y = cy + 1; y <= cy + 4; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static Material platformMaterial(Location center) {
        return center.getBlockX() % 2 == 0 ? Material.SMOOTH_STONE : Material.POLISHED_ANDESITE;
    }
}
