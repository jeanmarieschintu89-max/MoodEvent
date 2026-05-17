package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GeneratedCollapseBuilder {

    private static final Random RANDOM = new Random();

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
        int seedOffsetX = RANDOM.nextInt(10_000);
        int seedOffsetZ = RANDOM.nextInt(10_000);
        int rotation = RANDOM.nextInt(4);
        List<Location> breakableBlocks = new ArrayList<>();

        buildCage(world, cx - half - 2, cx + half + 2, cz - half - 2, cz + half + 2, cy, topY + 5);
        safeFloor(world, cx - half - 2, cx + half + 2, cz - half - 2, cz + half + 2, cy - 1, Material.BLACK_CONCRETE);

        for (int floor = 0; floor < safeFloors; floor++) {
            int y = cy + 4 + (floor * 5);
            Material material = FLOOR_MATERIALS[(floor + RANDOM.nextInt(FLOOR_MATERIALS.length)) % FLOOR_MATERIALS.length];
            int safeRadius = floor == safeFloors - 1 ? 2 : 1;
            for (int x = cx - half; x <= cx + half; x++) {
                for (int z = cz - half; z <= cz + half; z++) {
                    if (Math.abs(x - cx) <= safeRadius && Math.abs(z - cz) <= safeRadius) {
                        world.getBlockAt(x, y, z).setType(Material.LIME_CONCRETE, false);
                        breakableBlocks.add(new Location(world, x, y, z));
                        continue;
                    }
                    if (shouldCreateHole(x - cx, z - cz, floor, seedOffsetX, seedOffsetZ, rotation)) continue;
                    world.getBlockAt(x, y, z).setType(material, false);
                    breakableBlocks.add(new Location(world, x, y, z));
                }
            }
            addRandomReinforcements(world, cx, y, cz, half, material, breakableBlocks);
        }

        buildStartMark(world, cx, topY, cz);
        return new Layout(new Location(world, cx + 0.5, topY + 1, cz + 0.5, 0f, 0f), breakableBlocks, cy);
    }

    private static boolean shouldCreateHole(int dx, int dz, int floor, int seedOffsetX, int seedOffsetZ, int rotation) {
        int rx = dx;
        int rz = dz;
        for (int i = 0; i < rotation; i++) {
            int oldX = rx;
            rx = -rz;
            rz = oldX;
        }
        int value = Math.abs((rx + seedOffsetX) * 31 + (rz + seedOffsetZ) * 17 + floor * 43);
        boolean patternedHole = value % 11 == 0 || (Math.abs(rx) + Math.abs(rz) + floor + seedOffsetX) % 13 == 0;
        boolean randomHole = RANDOM.nextInt(100) < 5 + Math.min(8, floor);
        return patternedHole || randomHole;
    }

    private static void addRandomReinforcements(World world, int cx, int y, int cz, int half, Material material, List<Location> breakableBlocks) {
        int patches = Math.max(2, half / 3);
        for (int i = 0; i < patches; i++) {
            int px = cx - half + RANDOM.nextInt(half * 2 + 1);
            int pz = cz - half + RANDOM.nextInt(half * 2 + 1);
            int radius = RANDOM.nextBoolean() ? 1 : 0;
            for (int x = px - radius; x <= px + radius; x++) {
                for (int z = pz - radius; z <= pz + radius; z++) {
                    if (x < cx - half || x > cx + half || z < cz - half || z > cz + half) continue;
                    if (Math.abs(x - cx) <= 1 && Math.abs(z - cz) <= 1) continue;
                    if (!world.getBlockAt(x, y, z).getType().isAir()) continue;
                    world.getBlockAt(x, y, z).setType(material, false);
                    breakableBlocks.add(new Location(world, x, y, z));
                }
            }
        }
    }

    private static void buildCage(World world, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        Material glass = RANDOM.nextBoolean() ? Material.PURPLE_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS;
        Material pillar = RANDOM.nextBoolean() ? Material.AMETHYST_BLOCK : Material.PURPUR_BLOCK;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(x, y, minZ).setType(glass, false);
                world.getBlockAt(x, y, maxZ).setType(glass, false);
            }
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                world.getBlockAt(minX, y, z).setType(glass, false);
                world.getBlockAt(maxX, y, z).setType(glass, false);
            }
        }
        for (int y = minY; y <= maxY; y += 5) {
            world.getBlockAt(minX, y, minZ).setType(pillar, false);
            world.getBlockAt(maxX, y, minZ).setType(pillar, false);
            world.getBlockAt(minX, y, maxZ).setType(pillar, false);
            world.getBlockAt(maxX, y, maxZ).setType(pillar, false);
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
