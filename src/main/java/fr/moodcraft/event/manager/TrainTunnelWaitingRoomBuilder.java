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

    private static final int HALF = 24;
    private static final int INNER_HALF = 14;
    private static final int TRACK = 20;
    private static final int FLOOR_Y = 1;
    private static final int TUNNEL_HEIGHT = 6;

    private TrainTunnelWaitingRoomBuilder() {
    }

    public static int radius() {
        return HALF + 4;
    }

    public static int height() {
        return TUNNEL_HEIGHT + 5;
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
        buildTunnelRing(world, cx, cy, cz);
        buildInnerCourtyard(world, cx, cy, cz);
        buildTrackLoop(world, cx, cy, cz);
        buildEntranceSlope(world, cx, cy, cz);
        buildLightsAndDecor(world, cx, cy, cz);
    }

    public static void board(Player player, Location spawn) {
        if (player == null || spawn == null || spawn.getWorld() == null) return;
        Minecart cart = spawn.getWorld().spawn(spawn.clone().add(0, 0.1, 0), Minecart.class);
        cart.setMaxSpeed(0.42D);
        cart.setSlowWhenEmpty(false);
        cart.addPassenger(player);
        cart.setVelocity(new Vector(0.35, 0, 0));
    }

    private static void clearArea(World world, int cx, int cy, int cz) {
        for (int x = cx - HALF - 3; x <= cx + HALF + 3; x++) {
            for (int y = cy - 2; y <= cy + TUNNEL_HEIGHT + 5; y++) {
                for (int z = cz - HALF - 3; z <= cz + HALF + 3; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void buildTunnelRing(World world, int cx, int cy, int cz) {
        for (int x = cx - HALF; x <= cx + HALF; x++) {
            for (int z = cz - HALF; z <= cz + HALF; z++) {
                if (!isTunnelRing(x, z, cx, cz)) continue;

                for (int y = cy; y <= cy + TUNNEL_HEIGHT; y++) {
                    boolean floor = y == cy;
                    boolean roof = y == cy + TUNNEL_HEIGHT;
                    boolean outerWall = Math.abs(x - cx) == HALF || Math.abs(z - cz) == HALF;
                    boolean innerWall = Math.abs(x - cx) == INNER_HALF || Math.abs(z - cz) == INNER_HALF;
                    boolean tunnelWall = outerWall || innerWall;

                    if (floor || roof || tunnelWall) {
                        world.getBlockAt(x, y, z).setType(tunnelMaterial(x, y, z, cx, cy, cz), false);
                    }
                }
            }
        }
    }

    private static void buildInnerCourtyard(World world, int cx, int cy, int cz) {
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

        for (int x = -TRACK + 1; x <= TRACK - 1; x++) {
            boolean power = x % 5 == 0;
            placeRail(world, cx + x, y, cz - TRACK, power, Rail.Shape.EAST_WEST);
            placeRail(world, cx + x, y, cz + TRACK, power, Rail.Shape.EAST_WEST);
            placeRedGuide(world, cx + x, y, cz - TRACK, true);
            placeRedGuide(world, cx + x, y, cz + TRACK, true);
        }

        for (int z = -TRACK + 1; z <= TRACK - 1; z++) {
            boolean power = z % 5 == 0;
            placeRail(world, cx - TRACK, y, cz + z, power, Rail.Shape.NORTH_SOUTH);
            placeRail(world, cx + TRACK, y, cz + z, power, Rail.Shape.NORTH_SOUTH);
            placeRedGuide(world, cx - TRACK, y, cz + z, false);
            placeRedGuide(world, cx + TRACK, y, cz + z, false);
        }

        placeRail(world, cx - TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_EAST);
        placeRail(world, cx + TRACK, y, cz - TRACK, false, Rail.Shape.SOUTH_WEST);
        placeRail(world, cx + TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_WEST);
        placeRail(world, cx - TRACK, y, cz + TRACK, false, Rail.Shape.NORTH_EAST);
    }

    private static void buildEntranceSlope(World world, int cx, int cy, int cz) {
        int z = cz - TRACK;
        int endX = cx - TRACK + 1;
        for (int i = 0; i < 9; i++) {
            int x = endX - 8 + i;
            int y = cy + FLOOR_Y + Math.max(0, 4 - (i / 2));
            world.getBlockAt(x, y - 1, z).setType(Material.SMOOTH_STONE, false);
            placeRail(world, x, y, z, true, i < 8 ? Rail.Shape.ASCENDING_EAST : Rail.Shape.EAST_WEST);
            world.getBlockAt(x, y, z - 1).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(x, y, z + 1).setType(Material.RED_CONCRETE, false);
        }
    }

    private static void buildLightsAndDecor(World world, int cx, int cy, int cz) {
        int floorY = cy + FLOOR_Y;
        int roofY = cy + TUNNEL_HEIGHT;

        for (int i = -TRACK + 3; i <= TRACK - 3; i += 5) {
            setLightBlock(world, cx + i, roofY, cz - TRACK);
            setLightBlock(world, cx + i, roofY, cz + TRACK);
            setLightBlock(world, cx - TRACK, roofY, cz + i);
            setLightBlock(world, cx + TRACK, roofY, cz + i);
        }

        Material[] trophies = {
                Material.GOLD_BLOCK,
                Material.DIAMOND_BLOCK,
                Material.EMERALD_BLOCK,
                Material.REDSTONE_BLOCK,
                Material.LAPIS_BLOCK,
                Material.AMETHYST_BLOCK
        };

        int decoIndex = 0;
        for (int i = -TRACK + 5; i <= TRACK - 5; i += 8) {
            placeDecor(world, cx + i, floorY + 1, cz - INNER_HALF, trophies[decoIndex++ % trophies.length]);
            placeDecor(world, cx + i, floorY + 1, cz + INNER_HALF, trophies[decoIndex++ % trophies.length]);
            placeDecor(world, cx - INNER_HALF, floorY + 1, cz + i, trophies[decoIndex++ % trophies.length]);
            placeDecor(world, cx + INNER_HALF, floorY + 1, cz + i, trophies[decoIndex++ % trophies.length]);
        }

        for (int i = -TRACK + 4; i <= TRACK - 4; i += 6) {
            world.getBlockAt(cx + i, floorY, cz - TRACK - 2).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + i, floorY, cz + TRACK + 2).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx - TRACK - 2, floorY, cz + i).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + TRACK + 2, floorY, cz + i).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void setLightBlock(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.SEA_LANTERN, false);
        world.getBlockAt(x, y - 1, z).setType(Material.CHAIN, false);
    }

    private static void placeDecor(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material, false);
        world.getBlockAt(x, y + 1, z).setType(Material.SEA_LANTERN, false);
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

    private static void placeRedGuide(World world, int x, int y, int z, boolean alongX) {
        if (alongX) {
            world.getBlockAt(x, y, z - 1).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(x, y, z + 1).setType(Material.RED_CONCRETE, false);
        } else {
            world.getBlockAt(x - 1, y, z).setType(Material.RED_CONCRETE, false);
            world.getBlockAt(x + 1, y, z).setType(Material.RED_CONCRETE, false);
        }
    }

    private static boolean isTunnelRing(int x, int z, int cx, int cz) {
        int dx = Math.abs(x - cx);
        int dz = Math.abs(z - cz);
        return dx <= HALF && dz <= HALF && (dx >= INNER_HALF || dz >= INNER_HALF);
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
