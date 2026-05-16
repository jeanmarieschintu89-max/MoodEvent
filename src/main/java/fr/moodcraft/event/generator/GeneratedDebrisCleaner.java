package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public final class GeneratedDebrisCleaner {

    private GeneratedDebrisCleaner() {
    }

    public static int purgeGeneratedDebris(Location center, int radius, int down, int up) {
        if (center == null || center.getWorld() == null) return 0;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int minY = Math.max(world.getMinHeight(), cy - Math.max(0, down));
        int maxY = Math.min(world.getMaxHeight() - 1, cy + Math.max(0, up));
        int removed = 0;

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Material material = world.getBlockAt(x, y, z).getType();
                    if (!isKnownEventBlock(material)) continue;
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    removed++;
                }
            }
        }
        return removed;
    }

    private static boolean isKnownEventBlock(Material material) {
        if (material == null || material.isAir()) return false;
        String name = material.name();
        return name.endsWith("_WOOL")
                || name.endsWith("_CONCRETE")
                || name.endsWith("_STAINED_GLASS")
                || name.endsWith("_GLASS")
                || name.endsWith("_BARS")
                || name.endsWith("_FENCE")
                || name.endsWith("_PLANKS")
                || name.endsWith("_SLAB")
                || name.endsWith("_STAIRS")
                || name.endsWith("_TRAPDOOR")
                || name.endsWith("_PRESSURE_PLATE")
                || material == Material.SEA_LANTERN
                || material == Material.GLOWSTONE
                || material == Material.SHROOMLIGHT
                || material == Material.REDSTONE_BLOCK
                || material == Material.EMERALD_BLOCK
                || material == Material.GOLD_BLOCK
                || material == Material.AMETHYST_BLOCK
                || material == Material.MAGMA_BLOCK
                || material == Material.SLIME_BLOCK
                || material == Material.SOUL_SAND
                || material == Material.BLUE_ICE
                || material == Material.LADDER
                || material == Material.HAY_BLOCK
                || material == Material.COBBLESTONE
                || material == Material.SMOOTH_STONE
                || material == Material.POLISHED_ANDESITE
                || material == Material.DEEPSLATE_BRICKS
                || material == Material.CRACKED_DEEPSLATE_BRICKS
                || material == Material.CHISELED_DEEPSLATE
                || material == Material.DEEPSLATE_TILES
                || material == Material.PRISMARINE_BRICKS
                || material == Material.BEDROCK;
    }
}
