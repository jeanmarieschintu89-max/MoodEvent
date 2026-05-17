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
    private static final int RING_STEP = 3;

    private GeneratedRoundLabyrinthBuilder() {}

    public static Layout build(Location center, int rawSize) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int diameter = rawSize % 2 == 1 ? rawSize : rawSize + 1;
        int targetRadius = Math.max(10, diameter / 2);
        int rings = Math.max(3, Math.min(8, (targetRadius - CENTER_RADIUS - 1) / RING_STEP));
        int segments = segmentsFor(diameter);
        int outerRadius = boundaryRadius(rings);
        int startSegment = RANDOM.nextInt(segments);
        int exitSegment = (startSegment + segments / 2 + RANDOM.nextInt(Math.max(1, segments / 5))) % segments;

        Set<Edge> passages = carveTopology(rings, segments, startSegment);
        clearRoundArea(world, cx, cy, cz, outerRadius);
        drawWalls(world, cx, cy, cz, rings, segments, passages);
        openCenter(world, cx, cy, cz, startSegment, segments);
        openRingPassages(world, cx, cy, cz, passages, segments);
        openExit(world, cx, cy, cz, rings, segments, exitSegment);
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
        int clearRadius = outerRadius + 4;
        for (int x = cx - clearRadius; x <= cx + clearRadius; x++) {
            for (int z = cz - clearRadius; z <= cz + clearRadius; z++) {
                double distance = distance(cx, cz, x, z);
                world.getBlockAt(x, cy - 1, z).setType(distance <= outerRadius + 2 ? Material.SMOOTH_STONE : Material.AIR, false);
                for (int y = cy; y <= cy + 5; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
    }

    private static void drawWalls(World world, int cx, int cy, int cz, int rings, int segments, Set<Edge> passages) {
        for (int boundary = 0; boundary <= rings; boundary++) drawCircleWall(world, cx, cy, cz, boundaryRadius(boundary));

        for (int ring = 0; ring < rings; ring++) {
            int fromRadius = boundaryRadius(ring) + 1;
            int toRadius = boundaryRadius(ring + 1) - 1;
            for (int segment = 0; segment < segments; segment++) {
                int next = wrap(segment + 1, segments);
                if (passages.contains(new Edge(new Cell(ring, segment), new Cell(ring, next)))) continue;
                drawRadialWall(world, cx, cy, cz, fromRadius, toRadius, boundaryAngle(segment, segments));
            }
        }
    }

    private static void openRingPassages(World world, int cx, int cy, int cz, Set<Edge> passages, int segments) {
        for (Edge edge : passages) {
            if (edge.a().ring() == edge.b().ring()) continue;
            Cell outer = edge.a().ring() > edge.b().ring() ? edge.a() : edge.b();
            clearGap(world, cx, cy, cz, boundaryRadius(outer.ring()), outer.segment(), segments, 1);
        }
    }

    private static void openCenter(World world, int cx, int cy, int cz, int startSegment, int segments) {
        clearDisk(world, cx, cy, cz, CENTER_RADIUS);
        clearRadial(world, cx, cy, cz, 0, boundaryRadius(0) + 2, startSegment, segments, 1);
        clearGap(world, cx, cy, cz, boundaryRadius(0), startSegment, segments, 2);
        world.getBlockAt(cx, cy - 1, cz).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(cx, cy, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void openExit(World world, int cx, int cy, int cz, int rings, int segments, int exitSegment) {
        int outerRadius = boundaryRadius(rings);
        clearGap(world, cx, cy, cz, outerRadius, exitSegment, segments, 2);
        clearRadial(world, cx, cy, cz, boundaryRadius(rings - 1), outerRadius + 2, exitSegment, segments, 1);
        int finishX = cx + pointX(exitSegment, segments, outerRadius + 2);
        int finishZ = cz + pointZ(exitSegment, segments, outerRadius + 2);
        clearDisk(world, finishX, cy, finishZ, 1);
        world.getBlockAt(finishX, cy - 1, finishZ).setType(Material.RED_CONCRETE, false);
        world.getBlockAt(finishX, cy, finishZ).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void decorate(World world, int cx, int cy, int cz, int outerRadius, int rings, int segments, int startSegment, int exitSegment) {
        for (int segment = 0; segment < segments; segment += Math.max(4, segments / 8)) {
            int x = cx + pointX(segment, segments, outerRadius);
            int z = cz + pointZ(segment, segments, outerRadius);
            world.getBlockAt(x, cy + WALL_HEIGHT, z).setType(Material.SEA_LANTERN, false);
        }
        world.getBlockAt(cx + pointX(startSegment, segments, cellRadius(0)), cy - 1, cz + pointZ(startSegment, segments, cellRadius(0))).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(cx + pointX(exitSegment, segments, cellRadius(rings - 1)), cy - 1, cz + pointZ(exitSegment, segments, cellRadius(rings - 1))).setType(Material.RED_TERRACOTTA, false);
    }

    private static void drawCircleWall(World world, int cx, int cy, int cz, int radius) {
        int steps = Math.max(96, radius * 18);
        for (int i = 0; i < steps; i++) {
            double angle = (Math.PI * 2) * i / steps;
            int x = cx + (int) Math.round(Math.cos(angle) * radius);
            int z = cz + (int) Math.round(Math.sin(angle) * radius);
            setWall(world, x, cy, z);
        }
    }

    private static void drawRadialWall(World world, int cx, int cy, int cz, int fromRadius, int toRadius, double angle) {
        for (int radius = fromRadius; radius <= toRadius; radius++) {
            int x = cx + (int) Math.round(Math.cos(angle) * radius);
            int z = cz + (int) Math.round(Math.sin(angle) * radius);
            setWall(world, x, cy, z);
        }
    }

    private static void clearGap(World world, int cx, int cy, int cz, int radius, int segment, int segments, int gapRadius) {
        clearDisk(world, cx + pointX(segment, segments, radius), cy, cz + pointZ(segment, segments, radius), gapRadius);
    }

    private static void clearRadial(World world, int cx, int cy, int cz, int fromRadius, int toRadius, int segment, int segments, int radius) {
        for (int distance = Math.min(fromRadius, toRadius); distance <= Math.max(fromRadius, toRadius); distance++) {
            clearDisk(world, cx + pointX(segment, segments, distance), cy, cz + pointZ(segment, segments, distance), radius);
        }
    }

    private static void setWall(World world, int x, int cy, int z) {
        for (int y = cy; y < cy + WALL_HEIGHT; y++) world.getBlockAt(x, y, z).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
    }

    private static void clearDisk(World world, int cx, int cy, int cz, int radius) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if (distance(cx, cz, x, z) > radius + 0.35) continue;
                world.getBlockAt(x, cy - 1, z).setType(Material.SMOOTH_STONE, false);
                for (int y = cy; y <= cy + WALL_HEIGHT; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
    }

    private static int boundaryRadius(int boundary) { return CENTER_RADIUS + 1 + boundary * RING_STEP; }
    private static int cellRadius(int ring) { return boundaryRadius(ring) + (RING_STEP / 2) + 1; }

    private static int segmentsFor(int diameter) {
        if (diameter >= 55) return 36;
        if (diameter >= 43) return 32;
        if (diameter >= 31) return 24;
        return 16;
    }

    private static int pointX(int segment, int segments, int radius) { return (int) Math.round(Math.cos(angleForSegment(segment, segments)) * radius); }
    private static int pointZ(int segment, int segments, int radius) { return (int) Math.round(Math.sin(angleForSegment(segment, segments)) * radius); }
    private static double angleForSegment(int segment, int segments) { return ((Math.PI * 2) * wrap(segment, segments) / segments) - (Math.PI / 2); }
    private static double boundaryAngle(int segment, int segments) { return ((Math.PI * 2) * (segment + 0.5) / segments) - (Math.PI / 2); }

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
