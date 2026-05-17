package fr.moodcraft.event.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class GeneratedPrisonBreakBuilder {

    private static final int WALL_HEIGHT = 5;
    private static final int ROOM_HALF = 6;
    private static final int ROOM_GAP = 15;

    private GeneratedPrisonBreakBuilder() {}

    public static Layout build(Location center, int cellsWide) {
        World world = center.getWorld();
        if (world == null) return new Layout(center, center, 0, false);

        int rooms = Math.max(5, Math.min(8, (cellsWide + 1) / 2));
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        Random random = new Random(System.nanoTime() ^ world.getFullTime() ^ (cx * 31L) ^ (cz * 17L));

        int[] xs = new int[rooms];
        int[] zs = new int[rooms];
        for (int i = 0; i < rooms; i++) {
            xs[i] = cx + zigzagX(i, random);
            zs[i] = cz + ((rooms - 1) / 2 - i) * ROOM_GAP;
        }

        buildRoom(world, xs[0], cy, zs[0], Material.DEEPSLATE_TILES);
        buildStartCell(world, xs[0], cy, zs[0]);

        for (int i = 1; i < rooms; i++) {
            Material floor = switch (i % 5) {
                case 1 -> Material.POLISHED_ANDESITE;
                case 2 -> Material.SPRUCE_PLANKS;
                case 3 -> Material.MOSSY_STONE_BRICKS;
                case 4 -> Material.POLISHED_DEEPSLATE;
                default -> Material.STONE_BRICKS;
            };
            buildRoom(world, xs[i], cy, zs[i], floor);
            connectRooms(world, xs[i - 1], cy, zs[i - 1], xs[i], zs[i], random);
            decorateRoom(world, xs[i], cy, zs[i], i, random);
            buildPuzzleGate(world, xs[i], cy, zs[i] + ROOM_HALF + 1);
            placePuzzleClue(world, xs[i - 1], cy, zs[i - 1], i, random);
            buildFakeDoor(world, xs[i], cy, zs[i], random);
        }

        buildExit(world, xs[rooms - 1], cy, zs[rooms - 1] - ROOM_HALF + 2);
        Location start = new Location(world, xs[0] + 0.5, cy + 1.0, zs[0] + 0.5, 180f, 0f);
        Location finish = new Location(world, xs[rooms - 1] + 0.5, cy + 1.0, zs[rooms - 1] - ROOM_HALF + 2.5, 0f, 0f);
        return new Layout(start, finish, rooms, true);
    }

    private static int zigzagX(int index, Random random) {
        if (index == 0) return 0;
        int wave = index % 3;
        int base = wave == 0 ? 0 : wave == 1 ? -ROOM_GAP : ROOM_GAP;
        return base + random.nextInt(5) - 2;
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
            }
        }
        world.getBlockAt(cx, cy + WALL_HEIGHT, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void buildStartCell(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int y = cy + 1; y <= cy + 3; y++) {
                world.getBlockAt(x, y, cz - 3).setType(Material.IRON_BARS, false);
            }
        }
        openDoor(world, cx, cy, cz - 3);
        world.getBlockAt(cx - 2, cy + 1, cz + 2).setType(Material.GRAY_BED, false);
        world.getBlockAt(cx + 2, cy + 1, cz + 2).setType(Material.CAULDRON, false);
        world.getBlockAt(cx, cy + 1, cz - 2).setType(Material.STONE_BUTTON, false);
        world.getBlockAt(cx, cy + 2, cz + 4).setType(Material.OAK_SIGN, false);
    }

    private static void decorateRoom(World world, int cx, int cy, int cz, int index, Random random) {
        switch (index % 6) {
            case 1 -> {
                world.getBlockAt(cx - 3, cy + 1, cz - 2).setType(Material.LECTERN, false);
                world.getBlockAt(cx + 3, cy + 1, cz + 2).setType(Material.CHEST, false);
                world.getBlockAt(cx, cy + 1, cz).setType(Material.REDSTONE_TORCH, false);
            }
            case 2 -> {
                for (int x = cx - 4; x <= cx + 4; x += 2) world.getBlockAt(x, cy + 1, cz - 3).setType(Material.BOOKSHELF, false);
                world.getBlockAt(cx + random.nextInt(5) - 2, cy + 1, cz + random.nextInt(5) - 2).setType(Material.STONE_BUTTON, false);
            }
            case 3 -> {
                for (int x = cx - 4; x <= cx + 4; x++) world.getBlockAt(x, cy + 1, cz).setType(Material.IRON_TRAPDOOR, false);
                world.getBlockAt(cx - 4, cy + 2, cz - 4).setType(Material.LEVER, false);
            }
            case 4 -> {
                for (int x = cx - 3; x <= cx + 3; x++) for (int z = cz - 3; z <= cz + 3; z++) world.getBlockAt(x, cy, z).setType(Material.GRASS_BLOCK, false);
                world.getBlockAt(cx + 2, cy + 1, cz + 2).setType(Material.STONE_BUTTON, false);
                world.getBlockAt(cx - 2, cy + 1, cz - 2).setType(Material.COBWEB, false);
            }
            case 5 -> {
                for (int z = cz - 4; z <= cz + 4; z++) world.getBlockAt(cx, cy, z).setType(Material.WATER, false);
                world.getBlockAt(cx + 4, cy + 1, cz).setType(Material.LEVER, false);
            }
            default -> world.getBlockAt(cx, cy + 1, cz).setType(Material.LANTERN, false);
        }
        for (int i = 0; i < 3; i++) {
            int x = cx + random.nextInt(ROOM_HALF * 2 - 2) - ROOM_HALF + 1;
            int z = cz + random.nextInt(ROOM_HALF * 2 - 2) - ROOM_HALF + 1;
            if (world.getBlockAt(x, cy + 1, z).getType() == Material.AIR) {
                world.getBlockAt(x, cy + 1, z).setType(random.nextBoolean() ? Material.CHAIN : Material.COBWEB, false);
            }
        }
    }

    private static void connectRooms(World world, int x1, int cy, int z1, int x2, int z2, Random random) {
        carveCorridorZ(world, x1, cy, z1 - ROOM_HALF, z2 + ROOM_HALF, 2, Material.POLISHED_ANDESITE);
        carveCorridorX(world, x1, x2, cy, z2 + ROOM_HALF, 2, Material.POLISHED_ANDESITE);
        if (random.nextBoolean()) carveFakeSide(world, x1, cy, (z1 + z2) / 2, random);
    }

    private static void carveCorridorZ(World world, int x, int cy, int z1, int z2, int halfWidth, Material floor) {
        int min = Math.min(z1, z2);
        int max = Math.max(z1, z2);
        for (int z = min; z <= max; z++) for (int dx = -halfWidth; dx <= halfWidth; dx++) carveColumn(world, x + dx, cy, z, floor);
    }

    private static void carveCorridorX(World world, int x1, int x2, int cy, int z, int halfWidth, Material floor) {
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);
        for (int x = min; x <= max; x++) for (int dz = -halfWidth; dz <= halfWidth; dz++) carveColumn(world, x, cy, z + dz, floor);
    }

    private static void carveColumn(World world, int x, int cy, int z, Material floor) {
        world.getBlockAt(x, cy, z).setType(floor, false);
        for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
    }

    private static void carveFakeSide(World world, int cx, int cy, int cz, Random random) {
        int dir = random.nextBoolean() ? 1 : -1;
        for (int x = cx; x != cx + dir * 8; x += dir) carveColumn(world, x, cy, cz, Material.CRACKED_STONE_BRICKS);
        buildPuzzleGate(world, cx + dir * 8, cy, cz);
        world.getBlockAt(cx + dir * 6, cy + 1, cz).setType(Material.CHEST, false);
    }

    private static void buildPuzzleGate(World world, int cx, int cy, int cz) {
        for (int x = cx - 2; x <= cx + 2; x++) for (int y = cy + 1; y <= cy + 3; y++) world.getBlockAt(x, y, cz).setType(Material.IRON_BARS, false);
        openDoor(world, cx, cy, cz);
        world.getBlockAt(cx + 2, cy + 1, cz - 1).setType(Material.REDSTONE_LAMP, false);
    }

    private static void openDoor(World world, int x, int cy, int z) {
        world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
        world.getBlockAt(x, cy + 2, z).setType(Material.AIR, false);
    }

    private static void placePuzzleClue(World world, int cx, int cy, int cz, int index, Random random) {
        int x = cx + random.nextInt(ROOM_HALF * 2 - 2) - ROOM_HALF + 1;
        int z = cz + random.nextInt(ROOM_HALF * 2 - 2) - ROOM_HALF + 1;
        world.getBlockAt(x, cy + 1, z).setType(index % 2 == 0 ? Material.STONE_BUTTON : Material.LEVER, false);
        world.getBlockAt(x, cy, z).setType(Material.GOLD_BLOCK, false);
    }

    private static void buildFakeDoor(World world, int cx, int cy, int cz, Random random) {
        int side = random.nextInt(4);
        int x = cx + (side == 0 ? ROOM_HALF : side == 1 ? -ROOM_HALF : random.nextInt(ROOM_HALF * 2) - ROOM_HALF);
        int z = cz + (side == 2 ? ROOM_HALF : side == 3 ? -ROOM_HALF : random.nextInt(ROOM_HALF * 2) - ROOM_HALF);
        buildPuzzleGate(world, x, cy, z);
    }

    private static void buildExit(World world, int cx, int cy, int cz) {
        for (int x = cx - 3; x <= cx + 3; x++) for (int z = cz - 1; z <= cz + 1; z++) world.getBlockAt(x, cy, z).setType(Material.RED_WOOL, false);
        world.getBlockAt(cx - 4, cy + 1, cz).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(cx + 4, cy + 1, cz).setType(Material.SEA_LANTERN, false);
    }

    private static Material wallFor(int seed) {
        Material[] walls = {Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.POLISHED_ANDESITE};
        return walls[Math.floorMod(seed, walls.length)];
    }

    public record Layout(Location start, Location finish, int cellsWide, boolean reachable) {}
}
