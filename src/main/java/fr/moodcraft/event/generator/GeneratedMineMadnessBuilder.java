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

        for (int x = cx - half; x <= cx + half; x++) {
            for (int y = cy; y <= cy + safeHeight; y++) {
                for (int z = cz - half; z <= cz + half; z++) {
                    boolean shell = x == cx - half || x == cx + half || y == cy || y == cy + safeHeight || z == cz - half || z == cz + half;
                    if (shell) world.getBlockAt(x, y, z).setType(Material.BEDROCK, false);
                    else if (Math.abs(x - cx) <= 2 && Math.abs(z - cz) <= 2 && y <= cy + 3) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    else world.getBlockAt(x, y, z).setType(randomMineBlock(), false);
                }
            }
        }

        platform(world, cx, cy, cz, 3, Material.GOLD_BLOCK);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(cx, cy + 3, cz).setType(Material.SEA_LANTERN, false);
        return new Layout(new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f));
    }

    private static Material randomMineBlock() {
        int roll = RANDOM.nextInt(1000);
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

    public record Layout(Location start) {
    }
}
