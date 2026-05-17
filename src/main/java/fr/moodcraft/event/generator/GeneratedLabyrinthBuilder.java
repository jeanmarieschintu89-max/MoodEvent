package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class GeneratedLabyrinthBuilder {
    private static final Random RANDOM = new Random();
    private static final int WALL_HEIGHT = 3;
    private GeneratedLabyrinthBuilder() {}

    public static Layout build(Location center, int rawSize) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        int size = rawSize % 2 == 1 ? rawSize : rawSize + 1;
        int half = size / 2, minX = cx - half, minZ = cz - half, maxX = cx + half, maxZ = cz + half;
        int sasRadius = sasRadius(size);
        boolean[][] path = null;
        Gate entryGate = null, exitGate = null;
        int side = 0;
        boolean reachable = false;
        for (int attempt = 0; attempt < 8 && !reachable; attempt++) {
            path = generateMazeGrid(size);
            side = RANDOM.nextInt(4);
            entryGate = gateForSide(side, minX, maxX, minZ, maxZ, size, randomOdd(size), cy, sasRadius);
            exitGate = gateForSide(opposite(side), minX, maxX, minZ, maxZ, size, randomOdd(size), cy, sasRadius);
            reachable = isReachable(path, entryGate.insideGridX(), entryGate.insideGridZ(), exitGate.insideGridX(), exitGate.insideGridZ());
        }
        if (!reachable && path != null && entryGate != null && exitGate != null) {
            carveFallbackPath(path, entryGate.insideGridX(), entryGate.insideGridZ(), exitGate.insideGridX(), exitGate.insideGridZ());
            reachable = true;
        }
        int clear = sasRadius * 2 + 3;
        for (int x = minX - clear; x <= maxX + clear; x++) for (int z = minZ - clear; z <= maxZ + clear; z++) {
            world.getBlockAt(x, cy - 1, z).setType(Material.SMOOTH_STONE, false);
            for (int y = cy; y <= cy + 5; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
        }
        for (int gx = 0; gx < size; gx++) for (int gz = 0; gz < size; gz++) {
            int wx = minX + gx, wz = minZ + gz;
            world.getBlockAt(wx, cy - 1, wz).setType(path[gx][gz] ? Material.SMOOTH_STONE : Material.POLISHED_BLACKSTONE_BRICKS, false);
            if (!path[gx][gz]) for (int y = cy; y <= cy + WALL_HEIGHT - 1; y++) world.getBlockAt(wx, y, wz).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
        }
        openGate(world, entryGate.mazeX(), cy, entryGate.mazeZ());
        openGate(world, exitGate.mazeX(), cy, exitGate.mazeZ());
        buildSas(world, entryGate, Material.LIME_CONCRETE, Material.LIME_STAINED_GLASS, true);
        buildSas(world, exitGate, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, false);
        buildCornerLights(world, minX, maxX, minZ, maxZ, cy);
        GeneratedLabyrinthLootBuilder.build(world, path, minX, minZ, cy, size, entryGate.insideGridX(), entryGate.insideGridZ(), exitGate.insideGridX(), exitGate.insideGridZ());
        Location start = new Location(world, entryGate.spawnX() + 0.5, cy + 1, entryGate.spawnZ() + 0.5, yawForDirection(entryGate.dx(), entryGate.dz()), 0f);
        Location finish = new Location(world, exitGate.spawnX() + 0.5, cy + 1, exitGate.spawnZ() + 0.5, yawForDirection(exitGate.dx(), exitGate.dz()), 0f);
        return new Layout(start, finish, side, opposite(side), (sasRadius * 2) + 1, reachable);
    }

    private static boolean[][] generateMazeGrid(int size) {
        boolean[][] path = new boolean[size][size];
        boolean[][] visited = new boolean[size][size];
        Deque<int[]> stack = new ArrayDeque<>();
        int sx = randomOdd(size), sz = randomOdd(size);
        visited[sx][sz] = true; path[sx][sz] = true; stack.push(new int[]{sx, sz});
        int[][] dirs = {{2,0},{-2,0},{0,2},{0,-2}};
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            List<int[]> options = new ArrayList<>();
            for (int[] dir : dirs) {
                int nx = current[0] + dir[0], nz = current[1] + dir[1];
                if (nx > 0 && nz > 0 && nx < size - 1 && nz < size - 1 && !visited[nx][nz]) options.add(dir);
            }
            if (options.isEmpty()) { stack.pop(); continue; }
            int[] dir = options.get(RANDOM.nextInt(options.size()));
            int nx = current[0] + dir[0], nz = current[1] + dir[1];
            path[current[0] + dir[0] / 2][current[1] + dir[1] / 2] = true;
            path[nx][nz] = true; visited[nx][nz] = true; stack.push(new int[]{nx, nz});
        }
        return path;
    }

    private static boolean isReachable(boolean[][] path, int sx, int sz, int ex, int ez) {
        if (path == null || sx < 0 || sz < 0 || ex < 0 || ez < 0 || sx >= path.length || ex >= path.length || sz >= path[0].length || ez >= path[0].length) return false;
        if (!path[sx][sz] || !path[ex][ez]) return false;
        boolean[][] seen = new boolean[path.length][path[0].length];
        Deque<int[]> queue = new ArrayDeque<>(); queue.add(new int[]{sx, sz}); seen[sx][sz] = true;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            if (point[0] == ex && point[1] == ez) return true;
            for (int[] dir : dirs) {
                int nx = point[0] + dir[0], nz = point[1] + dir[1];
                if (nx < 0 || nz < 0 || nx >= path.length || nz >= path[0].length || seen[nx][nz] || !path[nx][nz]) continue;
                seen[nx][nz] = true; queue.add(new int[]{nx, nz});
            }
        }
        return false;
    }

    private static void carveFallbackPath(boolean[][] path, int sx, int sz, int ex, int ez) {
        int x = sx, z = sz;
        while (x != ex) { path[x][z] = true; x += x < ex ? 1 : -1; }
        while (z != ez) { path[x][z] = true; z += z < ez ? 1 : -1; }
        path[ex][ez] = true;
    }

    private static int sasRadius(int mazeSize) { return mazeSize >= 43 ? 4 : 3; }
    private static int randomOdd(int size) { int cells = Math.max(1, (size - 1) / 2); return 1 + RANDOM.nextInt(cells) * 2; }

    private static Gate gateForSide(int side, int minX, int maxX, int minZ, int maxZ, int size, int index, int y, int sasRadius) {
        int offset = sasRadius + 1;
        return switch (side) {
            case 0 -> new Gate(minX + index, minZ, minX + index, minZ - offset, index, 1, 0, 1, y, sasRadius);
            case 1 -> new Gate(minX + index, maxZ, minX + index, maxZ + offset, index, size - 2, 0, -1, y, sasRadius);
            case 2 -> new Gate(minX, minZ + index, minX - offset, minZ + index, 1, index, 1, 0, y, sasRadius);
            default -> new Gate(maxX, minZ + index, maxX + offset, minZ + index, size - 2, index, -1, 0, y, sasRadius);
        };
    }

    private static int opposite(int side) { return switch (side) { case 0 -> 1; case 1 -> 0; case 2 -> 3; default -> 2; }; }
    private static void openGate(World world, int x, int cy, int z) { for (int y = cy; y <= cy + WALL_HEIGHT; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false); world.getBlockAt(x, cy - 1, z).setType(Material.SMOOTH_STONE, false); }

    private static void buildSas(World world, Gate gate, Material floor, Material glass, boolean start) {
        int cx = gate.spawnX(), cz = gate.spawnZ(), radius = gate.sasRadius();
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) {
            boolean border = x == cx - radius || x == cx + radius || z == cz - radius || z == cz + radius;
            world.getBlockAt(x, gate.y() - 1, z).setType(floor, false);
            if (border) for (int y = gate.y(); y <= gate.y() + 2; y++) world.getBlockAt(x, y, z).setType(glass, false);
            else for (int y = gate.y(); y <= gate.y() + 3; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
        }
        for (int i = 0; i <= radius + 1; i++) openGate(world, gate.spawnX() + gate.dx() * i, gate.y(), gate.spawnZ() + gate.dz() * i);
        world.getBlockAt(cx, gate.y(), cz).setType(start ? Material.LIME_CONCRETE : Material.RED_CONCRETE, false);
        world.getBlockAt(cx, gate.y() + 1, cz).setType(start ? Material.LIGHT_WEIGHTED_PRESSURE_PLATE : Material.HEAVY_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static void buildCornerLights(World world, int minX, int maxX, int minZ, int maxZ, int cy) { int[][] corners = {{minX,minZ},{maxX,minZ},{minX,maxZ},{maxX,maxZ}}; for (int[] point : corners) world.getBlockAt(point[0], cy + 3, point[1]).setType(Material.SEA_LANTERN, false); }
    private static float yawForDirection(int dx, int dz) { if (dx > 0) return -90f; if (dx < 0) return 90f; if (dz > 0) return 0f; return 180f; }

    public record Layout(Location start, Location finish, int entrySide, int exitSide, int sasSize, boolean reachable) {}
    private record Gate(int mazeX, int mazeZ, int spawnX, int spawnZ, int insideGridX, int insideGridZ, int dx, int dz, int y, int sasRadius) {}
}
