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

    private static final int TRACK = 16;
    private static final int TUNNEL_WIDTH = 4;
    private static final int FLOOR_Y = 2;
    private static final int TUNNEL_HEIGHT = 6;

    private TrainTunnelWaitingRoomBuilder() {
    }

    public static int radius() {
        return TRACK + TUNNEL_WIDTH + 4;
    }

    public static int height() {
        return FLOOR_Y + TUNNEL_HEIGHT + 4;
    }

    public static Location spawn(Location center) {
        if (center == null || center.getWorld() == null) return center;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        return new Location(world, cx - TRACK + 2.5, cy + FLOOR_Y + 0.2, cz - TRACK + 0.5, -90f, 4f);
    }

    public static void build(Location center) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        clearArea(world, cx, cy, cz);
        buildLoopTunnel(world, cx, cy, cz);
        buildLoopTrack(world, cx, cy, cz);
        buildStartPlatform(world, cx, cy, cz);
        buildLights(world, cx, cy, cz);
    }

    public static void board(Player player, Location spawn) {
        if (player == null || spawn == null || spawn.getWorld() == null) return;
        Minecart cart = spawn.getWorld().spawn(spawn.clone().add(0, 0.05, 0), Minecart.class);
        cart.setMaxSpeed(0.42D);
        cart.setSlowWhenEmpty(false);
        cart.addPassenger(player);
        cart.setVelocity(new Vector(0.55, 0, 0));
    }

    private static void clearArea(World world, int cx, int cy, int cz) {
        int radius = radius();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - 2; y <= cy + height(); y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildLoopTunnel(World world, int cx, int cy, int cz) {
        for (int x = -TRACK; x <= TRACK; x++) {
            buildTunnelSliceX(world, cx + x, cy, cz - TRACK, x);
            buildTunnelSliceX(world, cx + x, cy, cz + TRACK, x + 9);
        }

        for (int z = -TRACK; z <= TRACK; z++) {
            buildTunnelSliceZ(world, cx - TRACK, cy, cz + z, z + 18);
            buildTunnelSliceZ(world, cx + TRACK, cy, cz + z, z + 27);
        }
    }

    private static void buildTunnelSliceX(World world, int x, int cy, int centerZ, int seed) {
        for (int y = 0; y <= TUNNEL_HEIGHT; y++) {
            for (int dz = -TUNNEL_WIDTH; dz <= TUNNEL_WIDTH; dz++) {
                boolean floor = y == 0;
                boolean roof = y == TUNNEL_HEIGHT;
                boolean wall = Math.abs(dz) == TUNNEL_WIDTH;
                if (floor || roof || wall) {
                    world.getBlockAt(x, cy + FLOOR_Y - 1 + y, centerZ + dz).setType(tunnelMaterial(seed + y + dz), false);
                } else {
                    world.getBlockAt(x, cy + FLOOR_Y - 1 + y, centerZ + dz).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildTunnelSliceZ(World world, int centerX, int cy, int z, int seed) {
        for (int y = 0; y <= TUNNEL_HEIGHT; y++) {
            for (int dx = -TUNNEL_WIDTH; dx <= TUNNEL_WIDTH; dx++) {
                boolean floor = y == 0;
                boolean roof = y == TUNNEL_HEIGHT;
                boolean wall = Math.abs(dx) == TUNNEL_WIDTH;
                if (floor || roof || wall) {
                    world.getBlockAt(centerX + dx, cy + FLOOR_Y - 1 + y, z).setType(tunnelMaterial(seed + y + dx), false);
                } else {
                    world.getBlockAt(centerX + dx, cy + FLOOR_Y - 1 + y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildLoopTrack(World world, int cx, int cy, int cz) {
        int y = cy + FLOOR_Y;

        for (int x = -TRACK + 1; x <= TRACK - 1; x++) {
            placeRail(world, cx + x, y, cz - TRACK, true, Rail.Shape.EAST_WEST);
            placeRail(world, cx + x, y, cz + TRACK, true, Rail.Shape.EAST_WEST);
        }

        for (int z = -TRACK + 1; z <= TRACK - 1; z++) {
            placeRail(world, cx - TRACK, y, cz + z, true, Rail.Shape.NORTH_SOUTH);
            placeRail(world, cx + TRACK, y, cz + z, true, Rail.Shape.NORTH_SOUTH);
        }

        placeRail(world, cx - TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_EAST);
        placeRail(world, cx + TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_WEST);
        placeRail(world, cx + TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_WEST);
        placeRail(world, cx - TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_EAST);
    }

    private static void buildStartPlatform(World world, int cx, int cy, int cz) {
        int y = cy + FLOOR_Y - 1;
        int startX = cx - TRACK + 1;
        int startZ = cz - TRACK;

        for (int x = startX; x <= startX + 5; x++) {
            world.getBlockAt(x, y, startZ - 1).setType(Material.SMOOTH_STONE, false);
            world.getBlockAt(x, y, startZ + 1).setType(Material.SMOOTH_STONE, false);
        }

        world.getBlockAt(startX + 1, y + 1, startZ - 2).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(startX + 1, y + 1, startZ + 2).setType(Material.SEA_LANTERN, false);
    }

    private static void buildLights(World world, int cx, int cy, int cz) {
        int roofY = cy + FLOOR_Y - 1 + TUNNEL_HEIGHT;

        for (int x = -TRACK + 4; x <= TRACK - 4; x += 6) {
            world.getBlockAt(cx + x, roofY, cz - TRACK).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + x, roofY, cz + TRACK).setType(Material.SEA_LANTERN, false);
        }

        for (int z = -TRACK + 4; z <= TRACK - 4; z += 6) {
            world.getBlockAt(cx - TRACK, roofY, cz + z).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + TRACK, roofY, cz + z).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void placeRail(World world, int x, int y, int z, boolean powered, Rail.Shape shape) {
        world.getBlockAt(x, y - 1, z).setType(powered ? Material.REDSTONE_BLOCK : Material.SMOOTH_STONE, false);

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

    private static Material tunnelMaterial(int seed) {
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
