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

    private static final int WALL = 17;
    private static final int TRACK = 14;
    private static final int FLOOR_Y = 2;
    private static final int WALL_HEIGHT = 6;

    private TrainTunnelWaitingRoomBuilder() {
    }

    public static int radius() {
        return WALL + 3;
    }

    public static int height() {
        return WALL_HEIGHT + 6;
    }

    public static Location spawn(Location center) {
        if (center == null || center.getWorld() == null) return center;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        return new Location(world, cx - TRACK - 6 + 0.5, cy + FLOOR_Y + 4.0, cz - TRACK + 0.5, 90f, 25f);
    }

    public static void build(Location center) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        clearArea(world, cx, cy, cz);
        buildClosedOuterWalls(world, cx, cy, cz);
        buildWallLedgeTrack(world, cx, cy, cz);
        buildEntranceDescent(world, cx, cy, cz);
        buildWallLightsAndDecor(world, cx, cy, cz);
        clearRidingSpace(world, cx, cy, cz);
    }

    public static void board(Player player, Location spawn) {
        if (player == null || spawn == null || spawn.getWorld() == null) return;
        Minecart cart = spawn.getWorld().spawn(spawn.clone().add(0, 0.1, 0), Minecart.class);
        cart.setMaxSpeed(0.38D);
        cart.setSlowWhenEmpty(false);
        cart.addPassenger(player);
        cart.setVelocity(new Vector(0.35, -0.06, 0));
    }

    private static void clearArea(World world, int cx, int cy, int cz) {
        for (int x = cx - WALL - 4; x <= cx + WALL + 4; x++) {
            for (int y = cy - 2; y <= cy + WALL_HEIGHT + 7; y++) {
                for (int z = cz - WALL - 4; z <= cz + WALL + 4; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildClosedOuterWalls(World world, int cx, int cy, int cz) {
        for (int i = -WALL; i <= WALL; i++) {
            for (int y = cy; y <= cy + WALL_HEIGHT; y++) {
                world.getBlockAt(cx + i, y, cz - WALL).setType(wallMaterial(i + y), false);
                world.getBlockAt(cx + i, y, cz + WALL).setType(wallMaterial(i + y + 5), false);
                world.getBlockAt(cx - WALL, y, cz + i).setType(wallMaterial(i + y + 10), false);
                world.getBlockAt(cx + WALL, y, cz + i).setType(wallMaterial(i + y + 15), false);
            }
        }
    }

    private static void buildWallLedgeTrack(World world, int cx, int cy, int cz) {
        int y = cy + FLOOR_Y;

        for (int x = -TRACK + 1; x <= TRACK - 1; x++) {
            boolean power = x % 4 == 0;
            buildLedge(world, cx + x, y, cz - TRACK, true);
            buildLedge(world, cx + x, y, cz + TRACK, true);
            placeRail(world, cx + x, y, cz - TRACK, power, Rail.Shape.EAST_WEST);
            placeRail(world, cx + x, y, cz + TRACK, power, Rail.Shape.EAST_WEST);
        }

        for (int z = -TRACK + 1; z <= TRACK - 1; z++) {
            boolean power = z % 4 == 0;
            buildLedge(world, cx - TRACK, y, cz + z, false);
            buildLedge(world, cx + TRACK, y, cz + z, false);
            placeRail(world, cx - TRACK, y, cz + z, power, Rail.Shape.NORTH_SOUTH);
            placeRail(world, cx + TRACK, y, cz + z, power, Rail.Shape.NORTH_SOUTH);
        }

        buildLedge(world, cx - TRACK, y, cz - TRACK, true);
        buildLedge(world, cx + TRACK, y, cz - TRACK, true);
        buildLedge(world, cx + TRACK, y, cz + TRACK, true);
        buildLedge(world, cx - TRACK, y, cz + TRACK, true);
        placeRail(world, cx - TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_EAST);
        placeRail(world, cx + TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_WEST);
        placeRail(world, cx + TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_WEST);
        placeRail(world, cx - TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_EAST);
    }

    private static void buildEntranceDescent(World world, int cx, int cy, int cz) {
        int z = cz - TRACK;
        int endX = cx - TRACK + 1;
        for (int i = 0; i < 8; i++) {
            int x = endX - 7 + i;
            int y = cy + FLOOR_Y + Math.max(0, 4 - (i / 2));
            buildLedge(world, x, y, z, true);
            placeRail(world, x, y, z, true, i < 7 ? Rail.Shape.ASCENDING_EAST : Rail.Shape.EAST_WEST);
            clearColumn(world, x, y, z);
        }
    }

    private static void buildWallLightsAndDecor(World world, int cx, int cy, int cz) {
        int lightY = cy + FLOOR_Y + 2;
        int decorY = cy + WALL_HEIGHT - 1;

        for (int i = -TRACK + 2; i <= TRACK - 2; i += 5) {
            placeWallLight(world, cx + i, lightY, cz - WALL);
            placeWallLight(world, cx + i, lightY, cz + WALL);
            placeWallLight(world, cx - WALL, lightY, cz + i);
            placeWallLight(world, cx + WALL, lightY, cz + i);
        }

        for (int i = -TRACK + 4; i <= TRACK - 4; i += 8) {
            world.getBlockAt(cx + i, decorY, cz - WALL).setType(trophy(i), false);
            world.getBlockAt(cx + i, decorY, cz + WALL).setType(trophy(i + 1), false);
            world.getBlockAt(cx - WALL, decorY, cz + i).setType(trophy(i + 2), false);
            world.getBlockAt(cx + WALL, decorY, cz + i).setType(trophy(i + 3), false);
        }
    }

    private static void buildLedge(World world, int x, int y, int z, boolean alongX) {
        world.getBlockAt(x, y - 1, z).setType(Material.SMOOTH_STONE, false);
        if (alongX) {
            world.getBlockAt(x, y - 1, z - 1).setType(Material.SMOOTH_STONE_SLAB, false);
            world.getBlockAt(x, y - 1, z + 1).setType(Material.SMOOTH_STONE_SLAB, false);
        } else {
            world.getBlockAt(x - 1, y - 1, z).setType(Material.SMOOTH_STONE_SLAB, false);
            world.getBlockAt(x + 1, y - 1, z).setType(Material.SMOOTH_STONE_SLAB, false);
        }
    }

    private static void clearRidingSpace(World world, int cx, int cy, int cz) {
        for (int x = -TRACK - 1; x <= TRACK + 1; x++) {
            clearColumn(world, cx + x, cy + FLOOR_Y, cz - TRACK);
            clearColumn(world, cx + x, cy + FLOOR_Y, cz + TRACK);
        }
        for (int z = -TRACK - 1; z <= TRACK + 1; z++) {
            clearColumn(world, cx - TRACK, cy + FLOOR_Y, cz + z);
            clearColumn(world, cx + TRACK, cy + FLOOR_Y, cz + z);
        }
    }

    private static void clearColumn(World world, int x, int y, int z) {
        for (int dy = 1; dy <= 4; dy++) {
            world.getBlockAt(x, y + dy, z).setType(Material.AIR, false);
            world.getBlockAt(x - 1, y + dy, z).setType(Material.AIR, false);
            world.getBlockAt(x + 1, y + dy, z).setType(Material.AIR, false);
            world.getBlockAt(x, y + dy, z - 1).setType(Material.AIR, false);
            world.getBlockAt(x, y + dy, z + 1).setType(Material.AIR, false);
        }
    }

    private static void placeWallLight(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.SEA_LANTERN, false);
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
