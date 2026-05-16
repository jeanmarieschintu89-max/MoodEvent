package fr.moodcraft.event.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class GeneratedGameThemePainter {

    private GeneratedGameThemePainter() {
    }

    public static void paint(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, GeneratedGameStyle style) {
        if (world == null) return;
        GeneratedGameTheme theme = GeneratedGameTheme.from(style);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material material = block.getType();
                    Material replacement = replacement(material, theme, x, y, z);
                    if (replacement != null && replacement != material) block.setType(replacement, false);
                }
            }
        }
    }

    private static Material replacement(Material material, GeneratedGameTheme theme, int x, int y, int z) {
        if (material == null || material.isAir()) return null;
        return switch (material) {
            case STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS, CHISELED_STONE_BRICKS,
                 SMOOTH_STONE, POLISHED_ANDESITE, COBBLESTONE, POLISHED_DEEPSLATE,
                 DEEPSLATE_TILES, POLISHED_BLACKSTONE, POLISHED_BLACKSTONE_BRICKS -> checker(theme.floorA(), theme.floorB(), x, z);
            case GRAY_STAINED_GLASS, TINTED_GLASS, GLASS -> theme.glass();
            case SEA_LANTERN, GLOWSTONE, SHROOMLIGHT, SOUL_LANTERN, REDSTONE_LAMP -> theme.light();
            case IRON_BARS -> Material.IRON_BARS;
            case BEDROCK -> Material.BEDROCK;
            case WATER -> Material.WATER;
            case CHEST -> Material.CHEST;
            default -> null;
        };
    }

    private static Material checker(Material a, Material b, int x, int z) {
        return (x + z) % 2 == 0 ? a : b;
    }
}
