package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GeneratedRoundLabyrinthBuilder {

    private static final Random RANDOM = new Random();
    private static final int WALL_HEIGHT = 3;
    private static final int CENTER_RADIUS = 2;
    private static final int RING_STEP = 2;

    private GeneratedRoundLabyrinthBuilder() {}

    public static Layout build(Location center, int rawSize) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int diameter = rawSize % 2 == 1 ? rawSize : rawSize + 1;
        int outerRadius = Math.max(10, diameter / 2);
        int rings = Math.max(4, (outerRadius - CENTER_RADIUS - 2) / RING_STEP);
        int segments = segmentsFor(diameter);
        int startSegment = RANDOM.nextInt(segments);
        int exitSegment = (startSegment + segments / 2 + RANDOM.nextInt(Math.max(1, segments / 4))) % segments;

        Set<Edge> passages = carveTopology(rings, segments, startSegment);
        clearRoundArea(world, cx, cy, cz, outerRadius);
        carveStart(world, cx, cy, cz);
        carveRoundMaze(world, cx, cy, cz, rings, segments, startSegment, passages);
        carveExit(world, cx, cy, cz, outerRadius, rings, segments, exitSegment);
        decorate(world, cx, cy, cz, outerRadius, rings, segments, startSegment, exitSegment);

        Location start = new Location(world, cx + 0.5, cy + 1, cz + 0.5, yawForSegment(startSegment, segments), 0f);
        int finishX = cx + pointX(exitSegment, segments, outerRadius + 2);
        int finishZ = cz + pointZ(exitSegment, segments, outerRadius + 2);
        Location finish = new Location(world, finishX + 0.5, cy + 1, finishZ + 0.5, yawForSegment(exitSegment, segments) + 180f, 0f);
        return new Layout(start, finish, sideFor(exitSegment, segments), rings, segments, true);
    }

    private static Set<Edge> carveTopology(int rings, int segments, int startSegment) {
        Set<Edge> passages = new HashSet<>();
        boolean[][] visited = new boolean[rings][segments];
        ArrayDeque<Cell> stack = new ArrayDeque<>();
        Cell start = new Cell(0, startSegment);
        visited[start.ring()][start.segment()] = true;
        stack.push(start);

        while (!stack.isEmpty()) {
            Cell current = stack.peek();
            List<Cell> options = neighbours(current, visited, rings, segments);
            if (options.isEmpty()) {
                stack.pop();
                continue;
            }
            Cell next = options.get(RANDOM.nextInt(options.size()));
            passages.add(new Edge(current, next));
            visited[next.ring()][next.segment()] = true;
            stack.push(next);
        }
        return passages;
    }

    private static List<Cell> neighbours(Cell cell, boolean[][] visited, int rings, int segments) {
        List<Cell> list = new ArrayList<>();
        addNeighbour(list, visited, cell.ring() - 1, cell.segment(), rings, segments);
        addNeighbour(list, visited, cell.ring() + 1, cell.segment(), rings, segments);
        addNeighbour(list, visited, cell.ring(), wrap(cell.segment() - 1, segments), rings, segments);
        addNeighbour(list, visited, cell.ring(), wrap(cell.segment() + 1, segments), rings, segments);
        Collections.shuffle(list, RANDOM);
        return list;
    }

    private static void addNeighbour(List<Cell> list, boolean[][] visited, int ring, int segment, int rings, int segments) {
        if (ring < 0 || ring >= rings) return;
        int wrapped = wrap(segment, segments);
        if (!visited[ring][wrapped]) list.add(new Cell(ring, wrapped));
    }

    private static void clearRoundArea(World world, int cx, int cy, int cz, int outerRadius) {
        int clearRadius = outerRadius + 5;
        for (int x = cx - clearRadius; x <= cx + clearRadius; x++) {
            for (int z = cz - clearRadius; z <= cz + clearRadius; z++) {
                double distance = distance(cx, cz, x, z);
                world.getBlockAt(x, cy - 1, z).setType(distance <= outerRadius + 2 ? Material.SMOOTH_STONE : Material.AIR, false);
                for (int y = cy; y <= cy + 5; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
                if (distance <= outerRadius) {
                    for (int y = cy; y < cy + WALL_HEIGHT; y++) world.getBlockAt(x, y, z).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
                }
            }
        }
    }

    private static void carveStart(World world, int cx, int cy, int cz) {
        carveDisk(world, cx, cy, cz, CENTER_RADIUS);
        world.getBlockAt(cx, cy - 1, cz).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(cx, cy, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void carveRoundMaze(World world, int cx, int cy, int cz, int rings, int segments, int startSegment, Set<Edge> passages) {
        carveRadial(world, cx, cy, cz, CENTER_RADIUS, radiusForRing(0), startSegment, segments);
        carveCell(world, cx, cy, cz, new Cell(0, startSegment), segments);
        for (Edge edge : passages) {
            carveCell(world, cx, cy, cz, edge.a(), segments);
            carveCell(world, cx, cy, cz, edge.b(), segments);
            if (edge.a().ring() == edge.b().ring()) carveArc(world, cx, cy, cz, edge.a(), edge.b(), segments);
            else carveRadialBetween(world, cx, cy, cz, edge.a(), edge.b(), segments);
        }
    }

    private static void carveCell(World world, int cx, int cy, int cz, Cell cell, int segments) {
        int radius = radiusForRing(cell.ring());
        carveDisk(world, cx + pointX(cell.segment(), segments, radius), cy, cz + pointZ(cell.segment(), segments, radius), 1);
    }

    private static void carveRadialBetween(World world, int cx, int cy, int cz, Cell first, Cell second, int segments) {
        Cell inner = first.ring() <= second.ring() ? first : second;
        Cell outer = first.ring() <= second.ring() ? second : first;
        carveRadial(world, cx, cy, cz, radiusForRing(inner.ring()), radiusForRing(outer.ring()), inner.segment(), segments);
    }

    private static void carveRadial(World world, int cx, int cy, int cz, int fromRadius, int toRadius, int segment, int segments) {
        for (int radius = Math.min(fromRadius, toRadius); radius <= Math.max(fromRadius, toRadius); radius++) {
            carveDisk(world, cx + pointX(segment, segments, radius), cy, cz + pointZ(segment, segments, radius), 1);
        }
    }

    private static void carveArc(World world, int cx, int cy, int cz, Cell first, Cell second, int segments) {
        int radius = radiusForRing(first.ring());
        int direction = wrap(second.segment() - first.segment(), segments) == 1 ? 1 : -1;
        int steps = Math.max(5, (int) Math.ceil((2 * Math.PI * radius) / segments));
        double start = angleForSegment(first.segment(), segments);
        double end = angleForSegment(first.segment() + direction, segments);
        for (int i = 0; i <= steps; i++) {
            double angle = start + ((end - start) * i / steps);
            carveDisk(world, cx + (int) Math.round(Math.cos(angle) * radius), cy, cz + (int) Math.round(Math.sin(angle) * radius), 1);
        }
    }

    private static void carveExit(World world, int cx, int cy, int cz, int outerRadius, int rings, int segments, int exitSegment) {
        for (int radius = radiusForRing(rings - 1); radius <= outerRadius + 2; radius++) {
            int x = cx + pointX(exitSegment, segments, radius);
            int z = cz + pointZ(exitSegment, segments, radius);
            carveDisk(world, x, cy, z, 1);
            world.getBlockAt(x, cy - 1, z).setType(Material.SMOOTH_STONE, false);
        }
        int finishX = cx + pointX(exitSegment, segments, outerRadius + 2);
        int finishZ = cz + pointZ(exitSegment, segments, outerRadius + 2);
        world.getBlockAt(finishX, cy - 1, finishZ).setType(Material.RED_CONCRETE, false);
        world.getBlockAt(finishX, cy, finishZ).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void decorate(World world, int cx, int cy, int cz, int outerRadius, int rings, int segments, int startSegment, int exitSegment) {
        for (int segment = 0; segment < segments; segment += Math.max(4, segments / 8)) {
            int x = cx + (int) Math.round(Math.cos(angleForSegment(segment, segments)) * outerRadius * 0.96);
            int z = cz + (int) Math.round(Math.sin(angleForSegment(segment, segments)) * outerRadius * 0.96);
            world.getBlockAt(x, cy + WALL_HEIGHT, z).setType(Material.SEA_LANTERN, false);
        }
        world.getBlockAt(cx + pointX(startSegment, segments, radiusForRing(0)), cy - 1, cz + pointZ(startSegment, segments, radiusForRing(0))).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(cx + pointX(exitSegment, segments, radiusForRing(rings - 1)), cy - 1, cz + pointZ(exitSegment, segments, radiusForRing(rings - 1))).setType(Material.RED_TERRACOTTA, false);
    }

    private static void carveDisk(World world, int cx, int cy, int cz, int radius) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (distance(cx, cz, x, z) > radius + 0.35) continue;
                world.getBlockAt(x, cy - 1, z).setType(Material.SMOOTH_STONE, false);
                for (int y = cy; y <= cy + WALL_HEIGHT; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
    }

    private static int radiusForRing(int ring) {
        return CENTER_RADIUS + 2 + ring * RING_STEP;
    }

    private static int segmentsFor(int diameter) {
        if (diameter >= 55) return 36;
        if (diameter >= 43) return 32;
        if (diameter >= 31) return 24;
        return 16;
    }

    private static int pointX(int segment, int segments, int radius) {
        return (int) Math.round(Math.cos(angleForSegment(segment, segments)) * radius);
    }

    private static int pointZ(int segment, int segments, int radius) {
        return (int) Math.round(Math.sin(angleForSegment(segment, segments)) * radius);
    }

    private static double angleForSegment(int segment, int segments) {
        return ((Math.PI * 2) * wrap(segment, segments) / segments) - (Math.PI / 2);
    }

    private static float yawForSegment(int segment, int segments) {
        double angle = angleForSegment(segment, segments);
        return (float) Math.toDegrees(Math.atan2(-Math.cos(angle), Math.sin(angle)));
    }

    private static int sideFor(int segment, int segments) {
        double angle = angleForSegment(segment, segments);
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        if (Math.abs(dz) >= Math.abs(dx)) return dz < 0 ? 0 : 1;
        return dx < 0 ? 2 : 3;
    }

    private static int wrap(int value, int max) {
        int result = value % max;
        return result < 0 ? result + max : result;
    }

    private static double distance(int cx, int cz, int x, int z) {
        double dx = x - cx;
        double dz = z - cz;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public record Layout(Location start, Location finish, int exitSide, int rings, int segments, boolean reachable) {}
    private record Cell(int ring, int segment) {}
    private record Edge(Cell a, Cell b) {
        private Edge {
            if (compare(a, b) > 0) {
                Cell tmp = a;
                a = b;
                b = tmp;
            }
        }
        private static int compare(Cell first, Cell second) {
            int ring = Integer.compare(first.ring(), second.ring());
            return ring != 0 ? ring : Integer.compare(first.segment(), second.segment());
        }
    }
}
