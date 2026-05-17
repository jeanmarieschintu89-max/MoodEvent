package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public final class GeneratedVerticalJumpBuilder {

    private static final int MAX_SAFE_XZ_GAP = 4;
    private static final int MAX_SAFE_Y_UP = 1;
    private static final int TOWER_RADIUS = 15;
    private static final int SPREAD_LIMIT = 12;
    private static final Material[] WOOL = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };

    private static final int[][] SPREAD_PATH = {
            {0, 0}, {3, 0}, {6, 2}, {9, 5}, {12, 8}, {8, 12}, {4, 10}, {0, 12},
            {-4, 10}, {-8, 12}, {-12, 8}, {-9, 4}, {-12, 0}, {-9, -4}, {-12, -8}, {-7, -12},
            {-2, -10}, {3, -12}, {8, -10}, {12, -6}, {10, -1}, {12, 4}, {7, 8}, {2, 11},
            {-3, 8}, {-8, 5}, {-11, 1}, {-8, -3}, {-4, -7}, {1, -9}, {6, -7}, {10, -3}
    };

    private GeneratedVerticalJumpBuilder() {}

    public static Layout build(Location center, int platforms) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center, false, 0, 0);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int safePlatforms = Math.max(8, Math.min(42, platforms));
        int topY = cy + 4 + safePlatforms;
        boolean reachable = true;
        int corrections = 0;
        List<JumpNode> nodes = new ArrayList<>();

        buildSafetyTower(world, cx, cy, cz, topY);
        drawGroundLine(world, cx, cy, cz, 5, Material.LIME_WOOL);
        buildStartMark(world, cx, cy, cz);

        JumpNode previous = new JumpNode(cx, cy, cz, 4);

        for (int i = 1; i <= safePlatforms; i++) {
            int[] offset = SPREAD_PATH[i % SPREAD_PATH.length];
            int wantedX = cx + offset[0];
            int wantedZ = cz + offset[1];
            int wantedY = cy + i;
            int radius = i % 8 == 0 ? 2 : 1;

            JumpNode wanted = new JumpNode(
                    clamp(wantedX, cx - SPREAD_LIMIT, cx + SPREAD_LIMIT),
                    wantedY,
                    clamp(wantedZ, cz - SPREAD_LIMIT, cz + SPREAD_LIMIT),
                    radius
            );

            JumpNode next = adaptReachableNode(previous, wanted, cx, cz);
            if (!sameNode(wanted, next)) corrections++;
            if (!isReachable(previous, next)) reachable = false;

            Material material = i == safePlatforms ? Material.RED_WOOL : WOOL[i % WOOL.length];
            platform(world, next.x(), next.y(), next.z(), next.radius(), material);
            if (i < safePlatforms) addPlatformMarker(world, next, i);
            nodes.add(next);
            previous = next;
        }

        return new Layout(
                new Location(world, cx + 0.5, cy + 1, cz + 0.5, 0f, 0f),
                new Location(world, previous.x() + 0.5, previous.y() + 1, previous.z() + 0.5, 180f, 0f),
                reachable,
                corrections,
                nodes.size()
        );
    }

    private static JumpNode adaptReachableNode(JumpNode previous, JumpNode wanted, int centerX, int centerZ) {
        int x = wanted.x();
        int y = wanted.y();
        int z = wanted.z();
        int radius = wanted.radius();

        int maxDelta = MAX_SAFE_XZ_GAP + previous.radius() + radius;
        if (x > previous.x() + maxDelta) x = previous.x() + maxDelta;
        if (x < previous.x() - maxDelta) x = previous.x() - maxDelta;
        if (z > previous.z() + maxDelta) z = previous.z() + maxDelta;
        if (z < previous.z() - maxDelta) z = previous.z() - maxDelta;
        if (y > previous.y() + MAX_SAFE_Y_UP) y = previous.y() + MAX_SAFE_Y_UP;

        x = clamp(x, centerX - SPREAD_LIMIT, centerX + SPREAD_LIMIT);
        z = clamp(z, centerZ - SPREAD_LIMIT, centerZ + SPREAD_LIMIT);
        return new JumpNode(x, y, z, radius);
    }

    private static boolean isReachable(JumpNode from, JumpNode to) {
        int xGap = Math.max(0, Math.abs(to.x() - from.x()) - from.radius() - to.radius());
        int zGap = Math.max(0, Math.abs(to.z() - from.z()) - from.radius() - to.radius());
        int yUp = to.y() - from.y();
        return xGap <= MAX_SAFE_XZ_GAP && zGap <= MAX_SAFE_XZ_GAP && yUp <= MAX_SAFE_Y_UP;
    }

    private static boolean sameNode(JumpNode first, JumpNode second) {
        return first.x() == second.x()
                && first.y() == second.y()
                && first.z() == second.z()
                && first.radius() == second.radius();
    }

    private static void buildSafetyTower(World world, int cx, int cy, int cz, int topY) {
        int minX = cx - TOWER_RADIUS;
        int maxX = cx + TOWER_RADIUS;
        int minZ = cz - TOWER_RADIUS;
        int maxZ = cz + TOWER_RADIUS;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.BLUE_CONCRETE, false);
            }
        }
        for (int y = cy; y <= topY + 5; y++) {
            for (int x = minX; x <= maxX; x++) {
                world.getBlockAt(x, y, minZ).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                world.getBlockAt(x, y, maxZ).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
            }
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(minX, y, z).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                world.getBlockAt(maxX, y, z).setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
            }
        }
        for (int y = cy; y <= topY + 5; y += 5) {
            world.getBlockAt(minX, y, minZ).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(maxX, y, minZ).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(minX, y, maxZ).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(maxX, y, maxZ).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void drawGroundLine(World world, int x, int y, int z, int halfWidth, Material material) {
        for (int dx = -halfWidth; dx <= halfWidth; dx++) {
            for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                if (Math.abs(dx) == halfWidth || Math.abs(dz) == halfWidth || dx == 0 || dz == 0) {
                    world.getBlockAt(x + dx, y, z + dz).setType(material, false);
                    world.getBlockAt(x + dx, y + 1, z + dz).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildStartMark(World world, int x, int y, int z) {
        for (int dz = -5; dz <= 5; dz++) world.getBlockAt(x - 1, y, z + dz).setType(Material.LIME_WOOL, false);
        world.getBlockAt(x, y + 3, z).setType(Material.SEA_LANTERN, false);
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }

    private static void addPlatformMarker(World world, JumpNode node, int index) {
        if (index % 4 == 0) world.getBlockAt(node.x(), node.y() + 1, node.z()).setType(Material.SEA_LANTERN, false);
        if (index % 5 == 0) world.getBlockAt(node.x(), node.y() - 1, node.z()).setType(Material.GLOWSTONE, false);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Layout(Location start, Location finish, boolean reachable, int corrections, int platformCount) {}
    private record JumpNode(int x, int y, int z, int radius) {}
}
