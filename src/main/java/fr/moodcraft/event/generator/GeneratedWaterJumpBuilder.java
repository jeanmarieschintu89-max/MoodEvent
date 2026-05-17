package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public final class GeneratedWaterJumpBuilder {

    private static final int LANE_HALF = 12;
    private static final int MAX_SAFE_X_GAP = 4;
    private static final int MAX_SAFE_Z_GAP = 5;
    private static final int MAX_SAFE_Y_UP = 1;

    private GeneratedWaterJumpBuilder() {}

    public static Layout build(Location center, int length) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        List<PlatformNode> nodes = new ArrayList<>();
        boolean reachable = true;
        int corrections = 0;

        for (int x = cx - 8; x <= cx + length + 10; x++) {
            for (int z = cz - LANE_HALF - 4; z <= cz + LANE_HALF + 4; z++) {
                world.getBlockAt(x, cy - 2, z).setType(Material.PRISMARINE_BRICKS, false);
                world.getBlockAt(x, cy - 1, z).setType(Material.WATER, false);
                for (int y = cy; y <= cy + 16; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }

        buildWaterJumpWoolWall(world, cx - 9, cx + length + 11, cy, cz - LANE_HALF - 5, cz + LANE_HALF + 5, 4, Material.WHITE_WOOL);

        for (int x = cx - 4; x <= cx + 4; x++) {
            for (int z = cz - 4; z <= cz + 4; z++) world.getBlockAt(x, cy, z).setType(Material.LIME_CONCRETE, false);
        }
        for (int z = cz - 4; z <= cz + 4; z++) world.getBlockAt(cx - 5, cy + 1, z).setType(Material.LIME_STAINED_GLASS, false);
        for (int x = cx - 5; x <= cx + 4; x++) {
            world.getBlockAt(x, cy + 1, cz - 5).setType(Material.LIME_STAINED_GLASS, false);
            world.getBlockAt(x, cy + 1, cz + 5).setType(Material.LIME_STAINED_GLASS, false);
        }
        for (int z = cz - 4; z <= cz + 4; z++) world.getBlockAt(cx + 5, cy, z).setType(Material.YELLOW_CONCRETE, false);
        world.getBlockAt(cx, cy + 2, cz - 4).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(cx, cy + 2, cz + 4).setType(Material.SEA_LANTERN, false);

        Material[] colors = {
                Material.LIGHT_BLUE_WOOL,
                Material.CYAN_WOOL,
                Material.WHITE_WOOL,
                Material.YELLOW_WOOL,
                Material.ORANGE_WOOL,
                Material.MAGENTA_WOOL,
                Material.PINK_WOOL
        };

        int[] spreadPattern = {0, 4, 8, 11, 7, 2, -3, -8, -11, -6, -1, 5, 10, 6, 0, -5, -10, -7};
        PlatformNode previous = new PlatformNode(cx + 5, cy, cz, 2);
        int x = cx + 8;
        int step = 0;
        int lastY = cy + 1;

        while (x < cx + length) {
            step++;
            int wantedZ = cz + spreadPattern[step % spreadPattern.length];
            int wantedY = cy + 1 + Math.min(8, step / 4) + (step % 8 == 0 ? 1 : 0);
            int radius = step % 6 == 0 ? 2 : 1;
            int wantedX = x;

            PlatformNode next = adaptReachableNode(previous, wantedX, wantedY, wantedZ, radius, cz - LANE_HALF, cz + LANE_HALF);
            if (next.x() != wantedX || next.y() != wantedY || next.z() != wantedZ) corrections++;
            if (!isReachable(previous, next)) reachable = false;

            Material platform = colors[step % colors.length];
            buildPlatform(world, next.x(), next.y(), next.z(), next.radius(), platform);
            nodes.add(next);

            previous = next;
            lastY = Math.max(lastY, next.y());
            x = next.x() + (step % 5 == 0 ? 4 : 3);
        }

        int finishX = cx + length + 4;
        int finishY = Math.max(cy + 2, lastY);
        PlatformNode finishNode = new PlatformNode(finishX, finishY, cz, 4);
        if (!isReachable(previous, finishNode)) {
            finishX = previous.x() + Math.min(MAX_SAFE_X_GAP + previous.radius() + finishNode.radius(), Math.max(3, finishX - previous.x()));
            finishY = Math.min(previous.y() + MAX_SAFE_Y_UP, finishY);
            finishNode = new PlatformNode(finishX, finishY, cz, 4);
            corrections++;
        }
        reachable = reachable && isReachable(previous, finishNode);

        for (int x2 = finishX - 4; x2 <= finishX + 4; x2++) {
            for (int z2 = cz - 4; z2 <= cz + 4; z2++) world.getBlockAt(x2, finishY, z2).setType(Material.RED_CONCRETE, false);
        }
        for (int z = cz - 4; z <= cz + 4; z++) world.getBlockAt(finishX + 5, finishY + 1, z).setType(Material.RED_STAINED_GLASS, false);
        world.getBlockAt(finishX, finishY + 1, cz).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(finishX, finishY + 3, cz - 4).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(finishX, finishY + 3, cz + 4).setType(Material.SEA_LANTERN, false);

        Location start = new Location(world, cx, cy + 1, cz, -90f, 0f);
        Location finish = new Location(world, finishX + 0.5, finishY + 1, cz + 0.5, -90f, 0f);
        return new Layout(start, finish, reachable, corrections, nodes.size());
    }

    private static PlatformNode adaptReachableNode(PlatformNode previous, int wantedX, int wantedY, int wantedZ, int radius, int minZ, int maxZ) {
        int x = wantedX;
        int y = wantedY;
        int z = wantedZ;
        int maxX = previous.x() + MAX_SAFE_X_GAP + previous.radius() + radius;
        if (x > maxX) x = maxX;
        if (y > previous.y() + MAX_SAFE_Y_UP) y = previous.y() + MAX_SAFE_Y_UP;
        int maxZDelta = MAX_SAFE_Z_GAP + previous.radius() + radius;
        if (z > previous.z() + maxZDelta) z = previous.z() + maxZDelta;
        if (z < previous.z() - maxZDelta) z = previous.z() - maxZDelta;
        z = Math.max(minZ, Math.min(maxZ, z));
        return new PlatformNode(x, y, z, radius);
    }

    private static boolean isReachable(PlatformNode from, PlatformNode to) {
        int xGap = Math.max(0, Math.abs(to.x() - from.x()) - from.radius() - to.radius());
        int zGap = Math.max(0, Math.abs(to.z() - from.z()) - from.radius() - to.radius());
        int yUp = to.y() - from.y();
        return xGap <= MAX_SAFE_X_GAP && zGap <= MAX_SAFE_Z_GAP && yUp <= MAX_SAFE_Y_UP;
    }

    private static void buildPlatform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) world.getBlockAt(x, cy, z).setType(material, false);
        }
    }

    private static void buildWaterJumpWoolWall(World world, int minX, int maxX, int baseY, int minZ, int maxZ, int height, Material material) {
        for (int y = baseY; y < baseY + height; y++) {
            for (int x = minX; x <= maxX; x++) {
                world.getBlockAt(x, y, minZ).setType(material, false);
                world.getBlockAt(x, y, maxZ).setType(material, false);
            }
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(minX, y, z).setType(material, false);
                world.getBlockAt(maxX, y, z).setType(material, false);
            }
        }
    }

    public record Layout(Location start, Location finish, boolean reachable, int corrections, int platformCount) {}
    private record PlatformNode(int x, int y, int z, int radius) {}
}
