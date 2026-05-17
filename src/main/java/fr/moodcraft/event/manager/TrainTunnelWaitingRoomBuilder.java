package fr.moodcraft.event.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class TrainTunnelWaitingRoomBuilder {

    private static final int HALF = 17;
    private static final int TRACK = 12;
    private static final int FLOOR_Y = 1;
    private static final int DECOR_HEIGHT = 4;

    private TrainTunnelWaitingRoomBuilder() {
    }

    public static int radius() {
        return HALF + 3;
    }

    public static int height() {
        return DECOR_HEIGHT + 4;
    }

    public static Location spawn(Location center) {
        if (center == null || center.getWorld() == null) return center;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        return new Location(world, cx - TRACK + 1.5, cy + FLOOR_Y + 1.0, cz - TRACK + 0.5, 90f, 0f);
    }

    public static void build(Location center) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        clearArea(world, cx, cy, cz);
        buildOpenFloor(world, cx, cy, cz);
        buildTrackLoop(world, cx, cy, cz);
        buildEntranceSlope(world, cx, cy, cz);
        buildSideDecor(world, cx, cy, cz);
    }

    public static void board(Player player, Location spawn) {
        if (player == null || spawn == null || spawn.getWorld() == null) return;
        Minecart cart = spawn.getWorld().spawn(spawn.clone().add(0, 0.1, 0), Minecart.class);
        cart.setMaxSpeed(0.36D);
        cart.setSlowWhenEmpty(false);
        cart.addPassenger(player);
        cart.setVelocity(new Vector(0.30, 0, 0));
    }

    private static void clearArea(World world, int cx, int cy, int cz) {
        for (int x = cx - HALF - 2; x <= cx + HALF + 2; x++) {
            for (int y = cy - 2; y <= cy + DECOR_HEIGHT + 5; y++) {
                for (int z = cz - HALF - 2; z <= cz + HALF + 2; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildOpenFloor(World world, int cx, int cy, int cz) {
        for (int x = cx - HALF; x <= cx + HALF; x++) {
            for (int z = cz - HALF; z <= cz + HALF; z++) {
                boolean nearTrackRing = Math.abs(Math.abs(x - cx) - TRACK) <= 3 || Math.abs(Math.abs(z - cz) - TRACK) <= 3;
                boolean insideSquare = Math.abs(x - cx) <= HALF && Math.abs(z - cz) <= HALF;
                if (!insideSquare || !nearTrackRing) continue;
                world.getBlockAt(x, cy, z).setType(floorMaterial(x, z, cx, cz), false);
            }
        }
    }

    private static void buildTrackLoop(World world, int cx, int cy, int cz) {
        int y = cy + FLOOR_Y;

        for (int x = -TRACK + 1; x <= TRACK - 1; x++) {
            boolean power = x % 4 == 0;
            placeRail(world, cx + x, y, cz - TRACK, power, Rail.Shape.EAST_WEST);
            placeRail(world, cx + x, y, cz + TRACK, power, Rail.Shape.EAST_WEST);
        }

        for (int z = -TRACK + 1; z <= TRACK - 1; z++) {
            boolean power = z % 4 == 0;
            placeRail(world, cx - TRACK, y, cz + z, power, Rail.Shape.NORTH_SOUTH);
            placeRail(world, cx + TRACK, y, cz + z, power, Rail.Shape.NORTH_SOUTH);
        }

        placeRail(world, cx - TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_EAST);
        placeRail(world, cx + TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_WEST);
        placeRail(world, cx + TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_WEST);
        placeRail(world, cx - TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_EAST);
    }

    private static void buildEntranceSlope(World world, int cx, int cy, int cz) {
        int z = cz - TRACK;
        int endX = cx - TRACK + 1;
        for (int i = 0; i < 7; i++) {
            int x = endX - 6 + i;
            int y = cy + FLOOR_Y + Math.max(0, 3 - (i / 2));
            world.getBlockAt(x, y - 1, z).setType(Material.SMOOTH_STONE, false);
            placeRail(world, x, y, z, true, i < 6 ? Rail.Shape.ASCENDING_EAST : Rail.Shape.EAST_WEST);
        }
    }

    private static void buildSideDecor(World world, int cx, int cy, int cz) {
        int baseY = cy + FLOOR_Y;
        int side = TRACK + 4;

        for (int i = -TRACK; i <= TRACK; i += 4) {
            placeColumn(world, cx + i, baseY, cz - side, i);
            placeColumn(world, cx + i, baseY, cz + side, i + 1);
            placeColumn(world, cx - side, baseY, cz + i, i + 2);
            placeColumn(world, cx + side, baseY, cz + i, i + 3);
        }

        for (int i = -TRACK + 2; i <= TRACK - 2; i += 6) {
            placeDisplay(world, cx + i, baseY, cz - side + 1, trophy(i));
            placeDisplay(world, cx + i, baseY, cz + side - 1, trophy(i + 1));
            placeDisplay(world, cx - side + 1, baseY, cz + i, trophy(i + 2));
            placeDisplay(world, cx + side - 1, baseY, cz + i, trophy(i + 3));
        }
    }

    private static void placeColumn(World world, int x, int y, int z, int seed) {
        for (int dy = 0; dy <= DECOR_HEIGHT; dy++) {
            world.getBlockAt(x, y + dy, z).setType(wallMaterial(seed + dy), false);
        }
        world.getBlockAt(x, y + DECOR_HEIGHT + 1, z).setType(Material.SEA_LANTERN, false);
    }

    private static void placeDisplay(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(Material.SMOOTH_STONE_SLAB, false);
        world.getBlockAt(x, y + 1, z).setType(material, false);
        world.getBlockAt(x, y + 2, z).setType(Material.SEA_LANTERN, false);
    }

    private static Material trophy(int seed) {
        Material[] trophies = {
                Material.GOLD_BLOCK,
                Material.DIAMOND_BLOCK,
                Material.EMERALD_BLOCK,
                Material.REDSTONE_BLOCK,
                Material.LAPIS_BLOCK,
                Material.AMETHYST_BLOCK
        };
        return trophies[Math.floorMod(seed, trophies.length)];
    }

    private static void placeRail(World world, int x, int y, int z, boolean powered, Rail.Shape shape) {
        Block support = world.getBlockAt(x, y - 1, z);
        if (support.getType().isAir()) support.setType(Material.SMOOTH_STONE, false);
        if (powered) world.getBlockAt(x, y - 2, z).setType(Material.REDSTONE_BLOCK, false);

        Block railBlock = world.getBlockAt(x, y, z);
        railBlock.setType(powered ? Material.POWERED_RAIL : Material.RAIL, false);
        BlockData data = railBlock.getBlockData();
        if (data instanceof Rail rail) {
            rail.setShape(shape);
        }
        if (data instanceof Powerable powerable) {
            powerable.setPowered(true);
        }
        railBlock.setBlockData(data, false);
    }

    private static Material floorMaterial(int x, int z, int cx, int cz) {
        Material[] floor = {
                Material.WHITE_WOOL,
                Material.LIGHT_BLUE_WOOL,
                Material.LIME_WOOL,
                Material.YELLOW_WOOL,
                Material.ORANGE_WOOL,
                Material.PINK_WOOL,
                Material.MAGENTA_WOOL,
                Material.BLACK_WOOL
        };
        return floor[Math.floorMod((x - cx) + (z - cz), floor.length)];
    }

    private static Material wallMaterial(int seed) {
        Material[] wool = {
                Material.RED_WOOL,
                Material.ORANGE_WOOL,
                Material.YELLOW_WOOL,
                Material.LIME_WOOL,
                Material.GREEN_WOOL,
                Material.LIGHT_BLUE_WOOL,
                Material.CYAN_WOOL,
                Material.BLUE_WOOL,
                Material.PURPLE_WOOL,
                Material.MAGENTA_WOOL,
                Material.PINK_WOOL,
                Material.WHITE_WOOL,
                Material.BLACK_WOOL
        };
        return wool[Math.floorMod(seed, wool.length)];
    }
}
