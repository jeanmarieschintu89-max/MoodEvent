package fr.moodcraft.event.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class TrainTunnelWaitingRoomBuilder {

    private static final int HALF = 22;
    private static final int INNER_HALF = 13;
    private static final int FLOOR_Y = 1;
    private static final int TUNNEL_HEIGHT = 5;

    private TrainTunnelWaitingRoomBuilder() {
    }

    public static int radius() {
        return HALF + 3;
    }

    public static int height() {
        return TUNNEL_HEIGHT + 4;
    }

    public static Location spawn(Location center) {
        if (center == null || center.getWorld() == null) return center;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        return new Location(world, cx - HALF + 2.5, cy + FLOOR_Y + 1.0, cz - HALF + 2.5, 90f, 0f);
    }

    public static void build(Location center) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - HALF - 2; x <= cx + HALF + 2; x++) {
            for (int y = cy; y <= cy + TUNNEL_HEIGHT + 3; y++) {
                for (int z = cz - HALF - 2; z <= cz + HALF + 2; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }

        buildOuterShell(world, cx, cy, cz);
        buildInnerVoid(world, cx, cy, cz);
        buildTrackLoop(world, cx, cy, cz);
        buildEntranceSlope(world, cx, cy, cz);
        buildLights(world, cx, cy, cz);
    }

    public static void board(Player player, Location spawn) {
        if (player == null || spawn == null || spawn.getWorld() == null) return;
        Minecart cart = spawn.getWorld().spawn(spawn.clone().add(0, 0.1, 0), Minecart.class);
        cart.setMaxSpeed(0.34D);
        cart.setSlowWhenEmpty(false);
        cart.addPassenger(player);
        cart.setVelocity(new Vector(0.28, 0, 0));
    }

    private static void buildOuterShell(World world, int cx, int cy, int cz) {
        for (int x = cx - HALF; x <= cx + HALF; x++) {
            for (int z = cz - HALF; z <= cz + HALF; z++) {
                boolean inTrackRing = isInRing(x, z, cx, cz);
                if (!inTrackRing) continue;

                for (int y = cy; y <= cy + TUNNEL_HEIGHT; y++) {
                    boolean floor = y == cy;
                    boolean roof = y == cy + TUNNEL_HEIGHT;
                    boolean wall = isOuterWall(x, z, cx, cz) || isInnerWall(x, z, cx, cz);
                    if (!floor && !roof && !wall) continue;

                    Material material = tunnelMaterial(x, y, z, cx, cy, cz);
                    world.getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
    }

    private static void buildInnerVoid(World world, int cx, int cy, int cz) {
        for (int x = cx - INNER_HALF + 1; x <= cx + INNER_HALF - 1; x++) {
            for (int z = cz - INNER_HALF + 1; z <= cz + INNER_HALF - 1; z++) {
                world.getBlockAt(x, cy, z).setType(Material.GRASS_BLOCK, false);
                for (int y = cy + 1; y <= cy + TUNNEL_HEIGHT; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildTrackLoop(World world, int cx, int cy, int cz) {
        int y = cy + FLOOR_Y;
        int min = -HALF + 2;
        int max = HALF - 2;

        for (int x = min; x <= max; x++) {
            placeTrack(world, cx + x, y, cz - max, x % 5 == 0);
            placeTrack(world, cx + x, y, cz + max, x % 5 == 0);
            placeRedGuide(world, cx + x, y, cz - max, true);
            placeRedGuide(world, cx + x, y, cz + max, true);
        }

        for (int z = min; z <= max; z++) {
            placeTrack(world, cx - max, y, cz + z, z % 5 == 0);
            placeTrack(world, cx + max, y, cz + z, z % 5 == 0);
            placeRedGuide(world, cx - max, y, cz + z, false);
            placeRedGuide(world, cx + max, y, cz + z, false);
        }
    }

    private static void buildEntranceSlope(World world, int cx, int cy, int cz) {
        int startX = cx - HALF + 2;
        int z = cz - HALF + 2;
        for (int i = 0; i < 8; i++) {
            int x = startX - 7 + i;
            int y = cy + FLOOR_Y + Math.max(0, 3 - (i / 2));
            world.getBlockAt(x, y - 1, z).setType(Material.SMOOTH_STONE, false);
            placeTrack(world, x, y, z, true);
            world.getBlockAt(x, y, z - 1).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(x, y, z + 1).setType(Material.RED_CONCRETE, false);
        }
        placeTrack(world, startX, cy + FLOOR_Y, z, true);
    }

    private static void buildLights(World world, int cx, int cy, int cz) {
        int y = cy + 2;
        for (int i = -HALF + 4; i <= HALF - 4; i += 6) {
            world.getBlockAt(cx + i, y, cz - HALF).setType(Material.TORCH, false);
            world.getBlockAt(cx + i, y, cz + HALF).setType(Material.TORCH, false);
            world.getBlockAt(cx - HALF, y, cz + i).setType(Material.TORCH, false);
            world.getBlockAt(cx + HALF, y, cz + i).setType(Material.TORCH, false);
        }
    }

    private static void placeTrack(World world, int x, int y, int z, boolean powered) {
        Block support = world.getBlockAt(x, y - 1, z);
        if (support.getType().isAir()) support.setType(Material.SMOOTH_STONE, false);
        world.getBlockAt(x, y, z).setType(powered ? Material.POWERED_RAIL : Material.RAIL, false);
        if (powered) world.getBlockAt(x, y - 2, z).setType(Material.REDSTONE_BLOCK, false);
    }

    private static void placeRedGuide(World world, int x, int y, int z, boolean alongX) {
        if (alongX) {
            world.getBlockAt(x, y, z - 1).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(x, y, z + 1).setType(Material.RED_CONCRETE, false);
        } else {
            world.getBlockAt(x - 1, y, z).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(x + 1, y, z).setType(Material.RED_CONCRETE, false);
        }
    }

    private static boolean isInRing(int x, int z, int cx, int cz) {
        int dx = Math.abs(x - cx);
        int dz = Math.abs(z - cz);
        return dx <= HALF && dz <= HALF && (dx >= INNER_HALF || dz >= INNER_HALF);
    }

    private static boolean isOuterWall(int x, int z, int cx, int cz) {
        return Math.abs(x - cx) == HALF || Math.abs(z - cz) == HALF;
    }

    private static boolean isInnerWall(int x, int z, int cx, int cz) {
        return Math.abs(x - cx) == INNER_HALF || Math.abs(z - cz) == INNER_HALF;
    }

    private static Material tunnelMaterial(int x, int y, int z, int cx, int cy, int cz) {
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
        int index = Math.floorMod((x - cx) + (z - cz) + (y - cy) * 3, wool.length);
        return wool[index];
    }
}
