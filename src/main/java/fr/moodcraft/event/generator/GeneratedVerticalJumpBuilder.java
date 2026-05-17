package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.ArrayList;
import java.util.List;

public final class GeneratedVerticalJumpBuilder {

    private static final int HALF = 11;
    private static final int MAX_SAFE_XZ_GAP = 4;
    private static final int MAX_SAFE_Y_UP = 1;

    private static final Material[] WOOL = {
            Material.WHITE_WOOL,
            Material.YELLOW_WOOL,
            Material.ORANGE_WOOL,
            Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.CYAN_WOOL,
            Material.MAGENTA_WOOL,
            Material.PINK_WOOL,
            Material.PURPLE_WOOL
    };

    private GeneratedVerticalJumpBuilder() {
    }

    public static Layout build(Location center, int platforms) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center, false, 0, 0);

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int safePlatforms = Math.max(12, Math.min(44, platforms));
        int topY = cy + safePlatforms + 9;

        buildWoolArena(world, cx, cy, cz, topY);
        buildStartZone(world, cx, cy, cz);

        List<Node> nodes = route(cx, cy, cz, safePlatforms);
        Validation validation = validateAndCorrect(nodes, cx, cz);
        nodes = validation.nodes();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            boolean finish = i == nodes.size() - 1;
            buildNode(world, node, i, finish);
        }

        Node first = nodes.get(0);
        Node last = nodes.get(nodes.size() - 1);
        buildFinishBeacon(world, last);

        return new Layout(
                new Location(world, first.x() + 0.5, first.y() + 1, first.z() + 0.5, 0f, 0f),
                new Location(world, last.x() + 0.5, last.y() + 1, last.z() + 0.5, 180f, 0f),
                validation.reachable(),
                validation.corrections(),
                nodes.size()
        );
    }

    private static List<Node> route(int cx, int cy, int cz, int platforms) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(cx, cy + 1, cz, 2));

        int[][] pattern = {
                {-7, -7}, {-3, -8}, {2, -7}, {7, -6}, {8, -2}, {5, 2}, {1, 5}, {-4, 7}, {-8, 5}, {-7, 1},
                {-3, -1}, {2, -3}, {6, -1}, {7, 3}, {3, 7}, {-2, 8}, {-7, 6}, {-8, 2}, {-5, -2}, {-1, -5},
                {4, -7}, {8, -5}, {9, 0}, {6, 5}, {2, 8}, {-3, 8}, {-8, 6}, {-9, 1}, {-7, -4}, {-2, -8},
                {3, -8}, {8, -6}, {9, -1}, {5, 4}, {0, 8}, {-5, 7}, {-9, 4}, {-8, -1}, {-4, -6}, {1, -8},
                {6, -7}, {9, -3}, {8, 2}, {4, 7}, {-1, 9}, {-6, 8}
        };

        for (int i = 1; i <= platforms; i++) {
            int[] point = pattern[(i - 1) % pattern.length];
            int radius = i % 11 == 0 ? 2 : 1;
            nodes.add(new Node(cx + point[0], cy + 1 + i, cz + point[1], radius));
        }

        Node previous = nodes.get(nodes.size() - 1);
        nodes.add(new Node(Math.max(cx - 6, Math.min(cx + 6, previous.x())), previous.y() + 1, Math.max(cz - 6, Math.min(cz + 6, previous.z())), 3));
        return nodes;
    }

    private static Validation validateAndCorrect(List<Node> input, int cx, int cz) {
        List<Node> corrected = new ArrayList<>();
        int corrections = 0;
        boolean reachable = true;
        corrected.add(input.get(0));

        for (int i = 1; i < input.size(); i++) {
            Node previous = corrected.get(corrected.size() - 1);
            Node wanted = input.get(i);
            Node next = adapt(previous, wanted, cx, cz);
            if (!same(next, wanted)) corrections++;
            if (!isReachable(previous, next)) reachable = false;
            corrected.add(next);
        }

        return new Validation(corrected, reachable, corrections);
    }

    private static Node adapt(Node previous, Node wanted, int cx, int cz) {
        int x = wanted.x();
        int y = wanted.y();
        int z = wanted.z();
        int radius = wanted.radius();

        int maxMove = MAX_SAFE_XZ_GAP + previous.radius() + radius;
        x = clamp(x, previous.x() - maxMove, previous.x() + maxMove);
        z = clamp(z, previous.z() - maxMove, previous.z() + maxMove);
        y = Math.min(y, previous.y() + MAX_SAFE_Y_UP);

        x = clamp(x, cx - HALF + 2, cx + HALF - 2);
        z = clamp(z, cz - HALF + 2, cz + HALF - 2);

        return new Node(x, y, z, radius);
    }

    private static boolean isReachable(Node from, Node to) {
        int xGap = Math.max(0, Math.abs(to.x() - from.x()) - from.radius() - to.radius());
        int zGap = Math.max(0, Math.abs(to.z() - from.z()) - from.radius() - to.radius());
        int yUp = to.y() - from.y();
        return xGap <= MAX_SAFE_XZ_GAP && zGap <= MAX_SAFE_XZ_GAP && yUp <= MAX_SAFE_Y_UP;
    }

    private static boolean same(Node a, Node b) {
        return a.x() == b.x() && a.y() == b.y() && a.z() == b.z() && a.radius() == b.radius();
    }

    private static void buildWoolArena(World world, int cx, int cy, int cz, int topY) {
        int minX = cx - HALF;
        int maxX = cx + HALF;
        int minZ = cz - HALF;
        int maxZ = cz + HALF;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, cy - 1, z).setType((x + z) % 2 == 0 ? Material.LIGHT_GRAY_WOOL : Material.GRAY_WOOL, false);
            }
        }

        for (int y = cy; y <= topY + 4; y++) {
            Material wall = WOOL[Math.floorMod(y - cy, WOOL.length)];
            for (int x = minX; x <= maxX; x++) {
                world.getBlockAt(x, y, minZ).setType(wall, false);
                world.getBlockAt(x, y, maxZ).setType(wall, false);
            }
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(minX, y, z).setType(wall, false);
                world.getBlockAt(maxX, y, z).setType(wall, false);
            }
            if ((y - cy) % 5 == 0) {
                world.getBlockAt(minX, y, minZ).setType(Material.SEA_LANTERN, false);
                world.getBlockAt(maxX, y, minZ).setType(Material.SEA_LANTERN, false);
                world.getBlockAt(minX, y, maxZ).setType(Material.SEA_LANTERN, false);
                world.getBlockAt(maxX, y, maxZ).setType(Material.SEA_LANTERN, false);
            }
        }
    }

    private static void buildStartZone(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 3; z <= cz + 3; z++) {
                world.getBlockAt(x, cy, z).setType(Material.LIME_WOOL, false);
            }
        }
        world.getBlockAt(cx, cy + 2, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void buildNode(World world, Node node, int index, boolean finish) {
        Material material = materialFor(index, finish);
        platform(world, node.x(), node.y(), node.z(), node.radius(), material);

        if (index > 0 && index % 9 == 0 && !finish) {
            addLadderHint(world, node.x() + node.radius() + 1, node.y(), node.z(), BlockFace.WEST);
        }
        if (index > 0 && index % 12 == 0 && !finish) {
            fenceBack(world, node.x(), node.y(), node.z(), node.radius());
        }
    }

    private static Material materialFor(int index, boolean finish) {
        if (finish) return Material.RED_WOOL;
        if (index == 0) return Material.LIME_WOOL;
        if (index % 10 == 0) return Material.BLUE_ICE;
        if (index % 8 == 0) return Material.SLIME_BLOCK;
        if (index % 7 == 0) return Material.HONEY_BLOCK;
        if (index % 6 == 0) return Material.OAK_PLANKS;
        return WOOL[index % WOOL.length];
    }

    private static void buildFinishBeacon(World world, Node finish) {
        for (int y = finish.y() + 1; y <= finish.y() + 4; y++) {
            world.getBlockAt(finish.x() - 3, y, finish.z() - 3).setType(Material.RED_STAINED_GLASS, false);
            world.getBlockAt(finish.x() + 3, y, finish.z() - 3).setType(Material.RED_STAINED_GLASS, false);
            world.getBlockAt(finish.x() - 3, y, finish.z() + 3).setType(Material.RED_STAINED_GLASS, false);
            world.getBlockAt(finish.x() + 3, y, finish.z() + 3).setType(Material.RED_STAINED_GLASS, false);
        }
        world.getBlockAt(finish.x(), finish.y() + 1, finish.z()).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
        world.getBlockAt(finish.x(), finish.y() + 4, finish.z()).setType(Material.SEA_LANTERN, false);
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }

    private static void fenceBack(World world, int cx, int y, int cz, int radius) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            world.getBlockAt(x, y + 1, cz - radius).setType(Material.OAK_FENCE, false);
        }
    }

    private static void addLadderHint(World world, int x, int y, int z, BlockFace facing) {
        Block support = world.getBlockAt(x, y, z);
        support.setType(Material.OAK_PLANKS, false);
        Block ladder = world.getBlockAt(x, y + 1, z);
        ladder.setType(Material.LADDER, false);
        BlockData data = ladder.getBlockData();
        if (data instanceof Directional directional) {
            directional.setFacing(facing);
            ladder.setBlockData(directional, false);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Node(int x, int y, int z, int radius) {
    }

    private record Validation(List<Node> nodes, boolean reachable, int corrections) {
    }

    public record Layout(Location start, Location finish, boolean reachable, int corrections, int platformCount) {
    }
}
