package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class GeneratedPrisonBreakBuilder {

    private static final int WALL_HEIGHT = 5;
    private static final int ROOM_HALF = 4;
    private static final int ROOM_GAP = 11;
    private static final int COLUMNS = 3;
    private static final Material PUZZLE_MARKER = Material.LODESTONE;
    private static final Material GATE_MARKER = Material.RESPAWN_ANCHOR;

    private GeneratedPrisonBreakBuilder() {}

    public static Layout build(Location center, int cellsWide) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center, 0, false);

        int rooms = roomsFor(cellsWide);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        Random random = new Random(System.nanoTime() ^ world.getFullTime() ^ (cx * 31L) ^ (cz * 17L));

        int[] xs = new int[rooms];
        int[] zs = new int[rooms];
        int rows = (int) Math.ceil(rooms / (double) COLUMNS);
        int startZ = ((rows - 1) * ROOM_GAP) / 2;

        for (int i = 0; i < rooms; i++) {
            int row = i / COLUMNS;
            int rawCol = i % COLUMNS;
            int col = row % 2 == 0 ? rawCol : COLUMNS - 1 - rawCol;
            xs[i] = cx + (col - 1) * ROOM_GAP + (i == 0 ? 0 : random.nextInt(3) - 1);
            zs[i] = cz + startZ - row * ROOM_GAP;
        }

        for (int i = 0; i < rooms; i++) {
            buildRoom(world, xs[i], cy, zs[i], floorFor(i));
            if (i == 0) buildStartCell(world, xs[i], cy, zs[i]);
            else decorateRoom(world, xs[i], cy, zs[i], i, random);
            if (i > 0) {
                connectRooms(world, xs[i - 1], cy, zs[i - 1], xs[i], zs[i]);
                buildPuzzleGate(world, xs[i], cy, zs[i], xs[i - 1], zs[i - 1]);
                placeHiddenPuzzle(world, xs[i - 1], cy, zs[i - 1], i, random);
                buildFakeDoor(world, xs[i], cy, zs[i], random);
            }
        }

        buildExit(world, xs[rooms - 1], cy, zs[rooms - 1]);
        Location start = new Location(world, xs[0] + 0.5, cy + 1.0, zs[0] + 0.5, 180f, 0f);
        Location finish = new Location(world, xs[rooms - 1] + 0.5, cy + 1.0, zs[rooms - 1] - ROOM_HALF + 1.5, 0f, 0f);
        return new Layout(start, finish, rooms, verifyFeasible(rooms, xs, zs));
    }

    private static boolean verifyFeasible(int rooms, int[] xs, int[] zs) {
        if (rooms < 2 || xs == null || zs == null || xs.length < rooms || zs.length < rooms) return false;
        for (int i = 1; i < rooms; i++) {
            int dx = Math.abs(xs[i] - xs[i - 1]);
            int dz = Math.abs(zs[i] - zs[i - 1]);
            if (dx > ROOM_GAP + 4 || dz > ROOM_GAP + 4) return false;
        }
        return true;
    }

    private static int roomsFor(int cellsWide) {
        if (cellsWide <= 7) return 6;
        if (cellsWide <= 9) return 8;
        if (cellsWide <= 11) return 10;
        return 12;
    }

    private static Material floorFor(int index) {
        return switch (index % 8) {
            case 0 -> Material.DEEPSLATE_TILES;
            case 1 -> Material.POLISHED_ANDESITE;
            case 2 -> Material.SPRUCE_PLANKS;
            case 3 -> Material.MOSSY_STONE_BRICKS;
            case 4 -> Material.POLISHED_DEEPSLATE;
            case 5 -> Material.MUD_BRICKS;
            case 6 -> Material.DARK_PRISMARINE;
            default -> Material.STONE_BRICKS;
        };
    }

    private static void buildRoom(World world, int cx, int cy, int cz, Material floor) {
        for (int x = cx - ROOM_HALF; x <= cx + ROOM_HALF; x++) {
            for (int z = cz - ROOM_HALF; z <= cz + ROOM_HALF; z++) {
                boolean border = x == cx - ROOM_HALF || x == cx + ROOM_HALF || z == cz - ROOM_HALF || z == cz + ROOM_HALF;
                world.getBlockAt(x, cy - 1, z).setType(Material.POLISHED_ANDESITE, false);
                world.getBlockAt(x, cy, z).setType(floor, false);
                for (int y = cy + 1; y <= cy + WALL_HEIGHT; y++) {
                    world.getBlockAt(x, y, z).setType(border ? wallFor(x + y + z) : Material.AIR, false);
                }
                world.getBlockAt(x, cy + WALL_HEIGHT + 1, z).setType(Material.POLISHED_ANDESITE, false);
            }
        }
        world.getBlockAt(cx, cy + WALL_HEIGHT, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void buildStartCell(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int y = cy + 1; y <= cy + 3; y++) {
                world.getBlockAt(x, y, cz + 2).setType(Material.IRON_BARS, false);
            }
        }
        openDoor(world, cx, cy, cz + 2);
        world.getBlockAt(cx - 2, cy + 1, cz - 2).setType(Material.GRAY_BED, false);
        world.getBlockAt(cx + 2, cy + 1, cz - 2).setType(Material.CAULDRON, false);
        world.getBlockAt(cx - 3, cy + 1, cz + 3).setType(Material.CHEST, false);
        placeHiddenPuzzle(world, cx, cy, cz, 0, new Random(cx ^ cz));
    }

    private static void decorateRoom(World world, int cx, int cy, int cz, int index, Random random) {
        switch (index % 8) {
            case 1 -> guardRoom(world, cx, cy, cz);
            case 2 -> archiveRoom(world, cx, cy, cz, random);
            case 3 -> ventRoom(world, cx, cy, cz);
            case 4 -> courtyardRoom(world, cx, cy, cz);
            case 5 -> sewerRoom(world, cx, cy, cz);
            case 6 -> evidenceRoom(world, cx, cy, cz, random);
            case 7 -> workshopRoom(world, cx, cy, cz);
            default -> world.getBlockAt(cx, cy + 1, cz).setType(Material.LANTERN, false);
        }
        scatterClutter(world, cx, cy, cz, random);
    }

    private static void guardRoom(World world, int cx, int cy, int cz) {
        world.getBlockAt(cx - 3, cy + 1, cz - 2).setType(Material.LECTERN, false);
        world.getBlockAt(cx + 3, cy + 1, cz + 2).setType(Material.CHEST, false);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.REDSTONE_TORCH, false);
        world.getBlockAt(cx - 2, cy + 1, cz + 2).setType(Material.STONE_BUTTON, false);
    }

    private static void archiveRoom(World world, int cx, int cy, int cz, Random random) {
        for (int x = cx - 3; x <= cx + 3; x += 2) {
            world.getBlockAt(x, cy + 1, cz - 3).setType(Material.BOOKSHELF, false);
            world.getBlockAt(x, cy + 1, cz + 3).setType(Material.BOOKSHELF, false);
        }
        world.getBlockAt(cx + random.nextInt(5) - 2, cy + 1, cz + random.nextInt(5) - 2).setType(Material.LEVER, false);
    }

    private static void ventRoom(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) world.getBlockAt(x, cy + 1, cz).setType(Material.IRON_TRAPDOOR, false);
        world.getBlockAt(cx - 3, cy + 2, cz - 3).setType(Material.LEVER, false);
        world.getBlockAt(cx + 3, cy + 1, cz + 3).setType(Material.CHAIN, false);
    }

    private static void courtyardRoom(World world, int cx, int cy, int cz) {
        for (int x = cx - 2; x <= cx + 2; x++) for (int z = cz - 2; z <= cz + 2; z++) world.getBlockAt(x, cy, z).setType(Material.GRASS_BLOCK, false);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.OAK_FENCE, false);
        world.getBlockAt(cx + 2, cy + 1, cz + 2).setType(Material.STONE_BUTTON, false);
        world.getBlockAt(cx - 2, cy + 1, cz - 2).setType(Material.COBWEB, false);
    }

    private static void sewerRoom(World world, int cx, int cy, int cz) {
        for (int z = cz - 3; z <= cz + 3; z++) world.getBlockAt(cx, cy, z).setType(Material.WATER, false);
        world.getBlockAt(cx + 3, cy + 1, cz).setType(Material.LEVER, false);
        world.getBlockAt(cx - 3, cy + 1, cz).setType(Material.MOSSY_COBBLESTONE, false);
    }

    private static void evidenceRoom(World world, int cx, int cy, int cz, Random random) {
        world.getBlockAt(cx - 3, cy + 1, cz).setType(Material.CHEST, false);
        world.getBlockAt(cx + 3, cy + 1, cz).setType(Material.BARREL, false);
        world.getBlockAt(cx + random.nextInt(5) - 2, cy + 1, cz - 3).setType(Material.STONE_BUTTON, false);
    }

    private static void workshopRoom(World world, int cx, int cy, int cz) {
        world.getBlockAt(cx - 2, cy + 1, cz - 2).setType(Material.CRAFTING_TABLE, false);
        world.getBlockAt(cx + 2, cy + 1, cz - 2).setType(Material.ANVIL, false);
        world.getBlockAt(cx, cy + 1, cz + 3).setType(Material.LEVER, false);
    }

    private static void scatterClutter(World world, int cx, int cy, int cz, Random random) {
        for (int i = 0; i < 4; i++) {
            int x = cx + random.nextInt(ROOM_HALF * 2 - 2) - ROOM_HALF + 1;
            int z = cz + random.nextInt(ROOM_HALF * 2 - 2) - ROOM_HALF + 1;
            if (world.getBlockAt(x, cy + 1, z).getType() == Material.AIR) {
                world.getBlockAt(x, cy + 1, z).setType(random.nextBoolean() ? Material.CHAIN : Material.COBWEB, false);
            }
        }
    }

    private static void connectRooms(World world, int x1, int cy, int z1, int x2, int z2) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        if (Math.abs(dx) >= Math.abs(dz)) {
            int sx = x1 + Integer.signum(dx) * (ROOM_HALF + 1);
            int ex = x2 - Integer.signum(dx) * (ROOM_HALF + 1);
            buildCorridorX(world, sx, ex, cy, z1, Material.POLISHED_ANDESITE);
            if (dz != 0) {
                int turnZ = z2 - Integer.signum(dz) * (ROOM_HALF + 1);
                buildCorridorZ(world, ex, cy, z1, turnZ, Material.POLISHED_ANDESITE);
            }
        } else {
            int sz = z1 + Integer.signum(dz) * (ROOM_HALF + 1);
            int ez = z2 - Integer.signum(dz) * (ROOM_HALF + 1);
            buildCorridorZ(world, x1, cy, sz, ez, Material.POLISHED_ANDESITE);
            if (dx != 0) {
                int turnX = x2 - Integer.signum(dx) * (ROOM_HALF + 1);
                buildCorridorX(world, x1, turnX, cy, ez, Material.POLISHED_ANDESITE);
            }
        }
    }

    private static void buildCorridorZ(World world, int x, int cy, int z1, int z2, Material floor) {
        int min = Math.min(z1, z2);
        int max = Math.max(z1, z2);
        for (int z = min; z <= max; z++) buildTunnelSlice(world, x, cy, z, true, floor);
    }

    private static void buildCorridorX(World world, int x1, int x2, int cy, int z, Material floor) {
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);
        for (int x = min; x <= max; x++) buildTunnelSlice(world, x, cy, z, false, floor);
    }

    private static void buildTunnelSlice(World world, int cx, int cy, int cz, boolean alongZ, Material floor) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean inside = alongZ ? Math.abs(dx) <= 1 : Math.abs(dz) <= 1;
                int x = cx + dx;
                int z = cz + dz;
                world.getBlockAt(x, cy - 1, z).setType(Material.POLISHED_ANDESITE, false);
                world.getBlockAt(x, cy, z).setType(inside ? floor : wallFor(x + z), false);
                for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, z).setType(inside ? Material.AIR : wallFor(x + y + z), false);
                world.getBlockAt(x, cy + 4, z).setType(Material.POLISHED_ANDESITE, false);
            }
        }
        if (Math.floorMod(cx + cz, 9) == 0) world.getBlockAt(cx, cy + 4, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void carveFakeSide(World world, int cx, int cy, int cz, Random random) {
        int dir = random.nextBoolean() ? 1 : -1;
        for (int x = cx; x != cx + dir * 7; x += dir) buildTunnelSlice(world, x, cy, cz, false, Material.CRACKED_STONE_BRICKS);
        buildFakeGate(world, cx + dir * 7, cy, cz, true);
        world.getBlockAt(cx + dir * 5, cy + 1, cz).setType(Material.CHEST, false);
    }

    private static void buildPuzzleGate(World world, int cx, int cy, int cz, int previousX, int previousZ) {
        boolean vertical = Math.abs(cx - previousX) <= Math.abs(cz - previousZ);
        int gateZ = previousZ > cz ? cz + ROOM_HALF : cz - ROOM_HALF;
        int gateX = previousX > cx ? cx + ROOM_HALF : cx - ROOM_HALF;
        if (vertical) buildRealGate(world, cx, cy, gateZ, true); else buildRealGate(world, gateX, cy, cz, false);
    }

    private static void buildRealGate(World world, int cx, int cy, int cz, boolean alongX) {
        buildGateBars(world, cx, cy, cz, alongX);
        world.getBlockAt(cx, cy - 1, cz).setType(GATE_MARKER, false);
        world.getBlockAt(cx + (alongX ? 2 : 0), cy + 1, cz + (alongX ? 0 : 2)).setType(Material.REDSTONE_LAMP, false);
    }

    private static void buildFakeGate(World world, int cx, int cy, int cz, boolean alongX) {
        buildGateBars(world, cx, cy, cz, alongX);
        world.getBlockAt(cx + (alongX ? 2 : 0), cy + 1, cz + (alongX ? 0 : 2)).setType(Material.REDSTONE_LAMP, false);
    }

    private static void buildGateBars(World world, int cx, int cy, int cz, boolean alongX) {
        if (alongX) {
            for (int x = cx - 1; x <= cx + 1; x++) for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, cz).setType(Material.IRON_BARS, false);
        } else {
            for (int z = cz - 1; z <= cz + 1; z++) for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(cx, y, z).setType(Material.IRON_BARS, false);
        }
    }

    private static void openDoor(World world, int x, int cy, int z) {
        world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
        world.getBlockAt(x, cy + 2, z).setType(Material.AIR, false);
    }

    private static void placeHiddenPuzzle(World world, int cx, int cy, int cz, int index, Random random) {
        int[][] candidates = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}, {-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        int[] offset = candidates[Math.floorMod(index + random.nextInt(candidates.length), candidates.length)];
        int x = cx + offset[0];
        int z = cz + offset[1];
        Material mechanism = switch (Math.floorMod(index + random.nextInt(3), 3)) {
            case 0 -> Material.STONE_BUTTON;
            case 1 -> Material.LEVER;
            default -> Material.STONE_PRESSURE_PLATE;
        };

        world.getBlockAt(x, cy + 1, z).setType(mechanism, false);
        world.getBlockAt(x, cy, z).setType(floorFor(index), false);
        world.getBlockAt(x, cy - 1, z).setType(PUZZLE_MARKER, false);

        if (world.getBlockAt(x + 1, cy + 1, z).getType() == Material.AIR) world.getBlockAt(x + 1, cy + 1, z).setType(Material.BARREL, false);
        if (world.getBlockAt(x, cy + 1, z + 1).getType() == Material.AIR) world.getBlockAt(x, cy + 1, z + 1).setType(Material.CHAIN, false);
    }

    private static void buildFakeDoor(World world, int cx, int cy, int cz, Random random) {
        int side = random.nextInt(4);
        int x = cx + (side == 0 ? ROOM_HALF : side == 1 ? -ROOM_HALF : random.nextInt(ROOM_HALF * 2) - ROOM_HALF);
        int z = cz + (side == 2 ? ROOM_HALF : side == 3 ? -ROOM_HALF : random.nextInt(ROOM_HALF * 2) - ROOM_HALF);
        buildFakeGate(world, x, cy, z, side <= 1);
    }

    private static void buildExit(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) for (int z = cz - ROOM_HALF; z <= cz - ROOM_HALF + 2; z++) world.getBlockAt(x, cy, z).setType(Material.RED_WOOL, false);
        world.getBlockAt(cx - 4, cy + 1, cz - ROOM_HALF + 1).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(cx + 4, cy + 1, cz - ROOM_HALF + 1).setType(Material.SEA_LANTERN, false);
    }

    private static Material wallFor(int seed) {
        Material[] walls = {Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.POLISHED_ANDESITE};
        return walls[Math.floorMod(seed, walls.length)];
    }

    public record Layout(Location start, Location finish, int cellsWide, boolean reachable) {}
}
