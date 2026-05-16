package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public final class GeneratedCollapseBuilder {

    private static final Material[] FLOOR_MATERIALS = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };

    private GeneratedCollapseBuilder() {
    }

    public static Layout build(Location center, int width, int floors) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, List.of(), center.getBlockY());

        int safeWidth = Math.max(13, width | 1);
        int safeFloors = Math.max(4, floors);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int half = safeWidth / 2;
        int topY = cy + 4 + ((safeFloors - 1) * 5);
        List<Location> breakableBlocks = new ArrayList<>();

        buildCage(world, cx - half - 2, cx + half + 2, cz - half - 2, cz + half + 2, cy, topY + 5);
        safeFloor(world, cx - half - 2, cx + half + 2, cz - half - 2, cz + half + 2, cy - 1, Material.BLACK_CONCRETE);

        for (int floor = 0; floor < safeFloors; floor++) {
            int y = cy + 4 + (floor * 5);
            Material material = FLOOR_MATERIALS[floor % FLOOR_MATERIALS.length];
            for (int x = cx - half; x <= cx + half; x++) {
                for (int z = cz - half; z <= cz + half; z++) {
                    if ((x + z + floor) % 7 == 0) continue;
                    world.getBlockAt(x, y, z).setType(material, false);
                    breakableBlocks.add(new Location(world, x, y, z));
                }
            }
        }

        buildStartMark(world, cx, topY, cz);
        return new Layout(new Location(world, cx + 0.5, topY + 1, cz + 0.5, 0f, 0f), breakableBlocks, cy);
    }

    private static void buildCage(World world, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(x, y, minZ).setType(Material.PURPLE_STAINED_GLASS, false);
                world.getBlockAt(x, y, maxZ).setType(Material.PURPLE_STAINED_GLASS, false);
            }
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(minX, y, z).setType(Material.PURPLE_STAINED_GLASS, false);
                world.getBlockAt(maxX, y, z).setType(Material.PURPLE_STAINED_GLASS, false);
            }
        }
        for (int y = minY; y <= maxY; y += 5) {
            world.getBlockAt(minX, y, minZ).setType(Material.AMETHYST_BLOCK, false);
            world.getBlockAt(maxX, y, minZ).setType(Material.AMETHYST_BLOCK, false);
            world.getBlockAt(minX, y, maxZ).setType(Material.AMETHYST_BLOCK, false);
            world.getBlockAt(maxX, y, maxZ).setType(Material.AMETHYST_BLOCK, false);
        }
    }

    private static void safeFloor(World world, int minX, int maxX, int minZ, int maxZ, int y, Material material) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, y, z).setType(material, false);
            }
        }
    }

    private static void buildStartMark(World world, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.LIME_CONCRETE, false);
            }
        }
        world.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
    }

    public record Layout(Location start, List<Location> breakableBlocks, int eliminationY) {
    }
}
