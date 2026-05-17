package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class TrainTunnelWaitingRoomBuilder {

    private static final int TRACK = 9;
    private static final int TUNNEL_WIDTH = 3;
    private static final int FLOOR_Y = 2;
    private static final int TUNNEL_HEIGHT = 5;
    private static final double CART_SPEED = 0.48D;

    private TrainTunnelWaitingRoomBuilder() {
    }

    public static int radius() {
        return TRACK + TUNNEL_WIDTH + 2;
    }

    public static int height() {
        return FLOOR_Y + TUNNEL_HEIGHT + 3;
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
        buildClosedTube(world, cx, cy, cz);
        buildLoopTrack(world, cx, cy, cz);
        buildLights(world, cx, cy, cz);
    }

    public static void board(Player player, Location spawn) {
        if (player == null || spawn == null || spawn.getWorld() == null) return;
        if (!EventManager.isEventPlayer(player)) return;

        Minecart cart = spawn.getWorld().spawn(spawn.clone().add(0, 0.05, 0), Minecart.class);
        cart.setMaxSpeed(0.55D);
        cart.setSlowWhenEmpty(false);
        cart.addPassenger(player);
        cart.setVelocity(new Vector(CART_SPEED, 0, 0));
        keepCartMovingAndReleaseOnStart(player, cart, spawn);
    }

    private static void keepCartMovingAndReleaseOnStart(Player player, Minecart cart, Location spawn) {
        new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                ticks += 5;
                if (!player.isOnline() || cart.isDead()) {
                    cancel();
                    return;
                }

                if (EventManager.isRunning() && EventManager.isParticipant(player)) {
                    cart.eject();
                    player.leaveVehicle();
                    cart.remove();
                    EventManager.resetPlayerToStart(player);
                    cancel();
                    return;
                }

                if (!cart.getPassengers().contains(player)) {
                    if (ticks > 20) cart.remove();
                    cancel();
                    return;
                }

                if (cart.getVelocity().lengthSquared() < 0.04D) {
                    cart.setVelocity(loopVelocity(cart.getLocation(), spawn));
                }
            }
        }.runTaskTimer(Main.getInstance(), 5L, 5L);
    }

    private static Vector loopVelocity(Location cartLocation, Location spawn) {
        int centerX = spawn.getBlockX() + TRACK - 2;
        int centerZ = spawn.getBlockZ() + TRACK;
        double x = cartLocation.getX() - centerX;
        double z = cartLocation.getZ() - centerZ;

        if (z <= -TRACK + 0.75 && x < TRACK - 0.5) return new Vector(CART_SPEED, 0, 0);
        if (x >= TRACK - 0.75 && z < TRACK - 0.5) return new Vector(0, 0, CART_SPEED);
        if (z >= TRACK - 0.75 && x > -TRACK + 0.5) return new Vector(-CART_SPEED, 0, 0);
        return new Vector(0, 0, -CART_SPEED);
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

    private static void buildClosedTube(World world, int cx, int cy, int cz) {
        int radius = TRACK + TUNNEL_WIDTH;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (!isTunnelCell(x, z)) continue;
                for (int y = 0; y <= TUNNEL_HEIGHT; y++) {
                    boolean floor = y == 0;
                    boolean roof = y == TUNNEL_HEIGHT;
                    boolean wall = isTunnelWall(x, z);
                    Block block = world.getBlockAt(cx + x, cy + FLOOR_Y - 1 + y, cz + z);
                    block.setType(floor || roof || wall ? tunnelMaterial(x + z + y) : Material.AIR, false);
                }
            }
        }
    }

    private static boolean isTunnelCell(int x, int z) {
        return isHorizontalTunnel(x, z) || isVerticalTunnel(x, z);
    }

    private static boolean isHorizontalTunnel(int x, int z) {
        boolean north = Math.abs(z + TRACK) <= TUNNEL_WIDTH;
        boolean south = Math.abs(z - TRACK) <= TUNNEL_WIDTH;
        return (north || south) && x >= -TRACK - TUNNEL_WIDTH && x <= TRACK + TUNNEL_WIDTH;
    }

    private static boolean isVerticalTunnel(int x, int z) {
        boolean west = Math.abs(x + TRACK) <= TUNNEL_WIDTH;
        boolean east = Math.abs(x - TRACK) <= TUNNEL_WIDTH;
        return (west || east) && z >= -TRACK - TUNNEL_WIDTH && z <= TRACK + TUNNEL_WIDTH;
    }

    private static boolean isTunnelWall(int x, int z) {
        return !isTunnelCell(x + 1, z)
                || !isTunnelCell(x - 1, z)
                || !isTunnelCell(x, z + 1)
                || !isTunnelCell(x, z - 1);
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

    private static void buildLights(World world, int cx, int cy, int cz) {
        int roofY = cy + FLOOR_Y - 1 + TUNNEL_HEIGHT;
        for (int x = -TRACK + 3; x <= TRACK - 3; x += 5) {
            world.getBlockAt(cx + x, roofY, cz - TRACK).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + x, roofY, cz + TRACK).setType(Material.SEA_LANTERN, false);
        }
        for (int z = -TRACK + 3; z <= TRACK - 3; z += 5) {
            world.getBlockAt(cx - TRACK, roofY, cz + z).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(cx + TRACK, roofY, cz + z).setType(Material.SEA_LANTERN, false);
        }
    }

    private static void placeRail(World world, int x, int y, int z, boolean powered, Rail.Shape shape) {
        world.getBlockAt(x, y - 1, z).setType(powered ? Material.REDSTONE_BLOCK : Material.SMOOTH_STONE, false);
        Block railBlock = world.getBlockAt(x, y, z);
        railBlock.setType(powered ? Material.POWERED_RAIL : Material.RAIL, false);
        BlockData data = railBlock.getBlockData();
        if (data instanceof Rail rail) rail.setShape(shape);
        if (data instanceof Powerable powerable) powerable.setPowered(true);
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
