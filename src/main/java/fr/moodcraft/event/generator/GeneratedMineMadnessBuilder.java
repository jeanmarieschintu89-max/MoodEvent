package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class GeneratedMineMadnessBuilder {

    private static final Random RANDOM = new Random();

    private GeneratedMineMadnessBuilder() {
    }

    public static Layout build(Location center, int width, int height) {
        World world = center.getWorld();
        if (world == null) return new Layout(center);

        int safeWidth = Math.max(15, width | 1);
        int safeHeight = Math.max(9, height);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int half = safeWidth / 2;
        int seedA = RANDOM.nextInt(10_000);
        int seedB = RANDOM.nextInt(10_000);
        Material shellMaterial = RANDOM.nextBoolean() ? Material.BEDROCK : Material.REINFORCED_DEEPSLATE;

        for (int x = cx - half; x <= cx + half; x++) {
            for (int y = cy; y <= cy + safeHeight; y++) {
                for (int z = cz - half; z <= cz + half; z++) {
                    boolean shell = x == cx - half || x == cx + half || y == cy || y == cy + safeHeight || z == cz - half || z == cz + half;
                    if (shell) {
                        world.getBlockAt(x, y, z).setType(shellMaterial, false);
                        continue;
                    }
                    if (Math.abs(x - cx) <= 2 && Math.abs(z - cz) <= 2 && y <= cy + 3) {
                        world.getBlockAt(x, y, z).setType(Material.AIR, false);
                        continue;
                    }
                    if (shouldCreateCavePocket(x - cx, y - cy, z - cz, seedA, seedB, safeHeight)) {
                        world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    } else {
                        world.getBlockAt(x, y, z).setType(randomMineBlock(x - cx, y - cy, z - cz, seedA, seedB), false);
                    }
                }
            }
        }

        carveRandomTunnels(world, cx, cy, cz, half, safeHeight);
        platform(world, cx, cy, cz, 3, Material.GOLD_BLOCK);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(cx, cy + 3, cz).setType(Material.SEA_LANTERN, false);
        return new Layout(new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f));
    }

    private static boolean shouldCreateCavePocket(int dx, int dy, int dz, int seedA, int seedB, int safeHeight) {
        if (dy <= 2) return false;
        int value = Math.abs((dx + seedA) * 37 + (dz - seedB) * 29 + dy * 19);
        boolean noisePocket = value % 41 == 0 || value % 53 == 0;
        boolean upperAir = dy > safeHeight / 2 && RANDOM.nextInt(1000) < 12;
        return noisePocket || upperAir;
    }

    private static void carveRandomTunnels(World world, int cx, int cy, int cz, int half, int safeHeight) {
        int tunnels = 2 + RANDOM.nextInt(3);
        for (int tunnel = 0; tunnel < tunnels; tunnel++) {
            int x = cx + RANDOM.nextInt(Math.max(1, half * 2 - 4)) - half + 2;
            int y = cy + 2 + RANDOM.nextInt(Math.max(1, safeHeight - 4));
            int z = cz + RANDOM.nextInt(Math.max(1, half * 2 - 4)) - half + 2;
            int length = 5 + RANDOM.nextInt(Math.max(6, half));
            int dx = RANDOM.nextInt(3) - 1;
            int dz = dx == 0 ? (RANDOM.nextBoolean() ? 1 : -1) : 0;

            for (int i = 0; i < length; i++) {
                carveSmallPocket(world, x, y, z);
                x += dx;
                z += dz;
                if (RANDOM.nextInt(4) == 0) y += RANDOM.nextBoolean() ? 1 : -1;
                x = clamp(x, cx - half + 2, cx + half - 2);
                y = clamp(y, cy + 2, cy + safeHeight - 2);
                z = clamp(z, cz - half + 2, cz + half - 2);
            }
        }
    }

    private static void carveSmallPocket(World world, int cx, int cy, int cz) {
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int y = cy - 1; y <= cy + 1; y++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    if (Math.abs(x - cx) + Math.abs(y - cy) + Math.abs(z - cz) > 2) continue;
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static Material randomMineBlock(int dx, int dy, int dz, int seedA, int seedB) {
        int roll = RANDOM.nextInt(1000);
        int veinBias = Math.abs((dx + seedA) * 13 + (dy * 31) + (dz - seedB) * 17) % 100;
        if (veinBias < 3) return Material.DIAMOND_ORE;
        if (veinBias < 7) return Material.GOLD_ORE;
        if (veinBias < 12) return Material.IRON_ORE;
        if (roll < 3) return Material.EMERALD_ORE;
        if (roll < 10) return Material.DIAMOND_ORE;
        if (roll < 30) return Material.GOLD_ORE;
        if (roll < 65) return Material.IRON_ORE;
        if (roll < 110) return Material.COPPER_ORE;
        if (roll < 155) return Material.REDSTONE_ORE;
        if (roll < 200) return Material.LAPIS_ORE;
        if (roll < 300) return Material.COAL_ORE;
        return RANDOM.nextBoolean() ? Material.STONE : Material.DEEPSLATE;
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Layout(Location start) {
    }
}
