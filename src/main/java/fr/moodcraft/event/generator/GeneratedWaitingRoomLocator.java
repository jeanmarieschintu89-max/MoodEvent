package fr.moodcraft.event.generator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class GeneratedWaitingRoomLocator {

    private static final int GAME_ROOM_BUFFER = 6;

    private GeneratedWaitingRoomLocator() {}

    public static Location nearActiveGame(Player player, String rawSize) {
        if (player == null) return null;

        FileConfiguration config = GeneratedGameManager.config();
        if (config == null || !config.getBoolean("active", false)) return null;

        Bounds game = readRegion(config);
        if (game == null) return null;

        World world = Bukkit.getWorld(game.worldName());
        if (world == null) return null;

        int radius = radius(rawSize);
        Location start = readLocation(config, world, "start");
        Side side = nearestSide(game, start);
        int roomY = floorY(game, start, player);
        int centerX;
        int centerZ;

        switch (side) {
            case WEST -> {
                centerX = game.minX() - radius - GAME_ROOM_BUFFER - 1;
                centerZ = clamp(referenceZ(game, start), game.minZ() + radius, game.maxZ() - radius);
            }
            case EAST -> {
                centerX = game.maxX() + radius + GAME_ROOM_BUFFER + 1;
                centerZ = clamp(referenceZ(game, start), game.minZ() + radius, game.maxZ() - radius);
            }
            case NORTH -> {
                centerX = clamp(referenceX(game, start), game.minX() + radius, game.maxX() - radius);
                centerZ = game.minZ() - radius - GAME_ROOM_BUFFER - 1;
            }
            case SOUTH -> {
                centerX = clamp(referenceX(game, start), game.minX() + radius, game.maxX() - radius);
                centerZ = game.maxZ() + radius + GAME_ROOM_BUFFER + 1;
            }
            default -> {
                centerX = game.minX() - radius - GAME_ROOM_BUFFER - 1;
                centerZ = midpoint(game.minZ(), game.maxZ());
            }
        }

        return new Location(world, centerX + 0.5, roomY, centerZ + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
    }

    private static Bounds readRegion(FileConfiguration config) {
        String world = config.getString("region.world", "");
        if (world == null || world.isBlank()) return null;
        return new Bounds(
                world,
                config.getInt("region.min-x"),
                config.getInt("region.min-y"),
                config.getInt("region.min-z"),
                config.getInt("region.max-x"),
                config.getInt("region.max-y"),
                config.getInt("region.max-z")
        );
    }

    private static Location readLocation(FileConfiguration config, World world, String path) {
        String locationWorld = config.getString(path + ".world", "");
        if (locationWorld == null || !locationWorld.equals(world.getName())) return null;
        return new Location(world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch"));
    }

    private static Side nearestSide(Bounds game, Location start) {
        if (start == null) return Side.WEST;
        int x = start.getBlockX();
        int z = start.getBlockZ();
        int west = Math.abs(x - game.minX());
        int east = Math.abs(game.maxX() - x);
        int north = Math.abs(z - game.minZ());
        int south = Math.abs(game.maxZ() - z);

        int min = Math.min(Math.min(west, east), Math.min(north, south));
        if (min == west) return Side.WEST;
        if (min == east) return Side.EAST;
        if (min == north) return Side.NORTH;
        return Side.SOUTH;
    }

    private static int floorY(Bounds game, Location start, Player player) {
        if (start != null) return Math.max(game.minY() + 1, start.getBlockY() - 1);
        return Math.max(game.minY() + 1, player.getLocation().getBlockY());
    }

    private static int referenceX(Bounds game, Location start) {
        return start == null ? midpoint(game.minX(), game.maxX()) : start.getBlockX();
    }

    private static int referenceZ(Bounds game, Location start) {
        return start == null ? midpoint(game.minZ(), game.maxZ()) : start.getBlockZ();
    }

    private static int midpoint(int min, int max) {
        return min + ((max - min) / 2);
    }

    private static int clamp(int value, int min, int max) {
        if (min > max) return midpoint(max, min);
        return Math.max(min, Math.min(max, value));
    }

    private static int radius(String text) {
        if (text == null) return 5;
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "mini", "7", "7x7" -> 3;
            case "small", "petit", "petite", "9", "9x9" -> 4;
            case "medium", "moyen", "moyenne", "11", "11x11" -> 5;
            case "large", "grand", "grande", "15", "15x15" -> 7;
            case "tresgrande", "tres_grande", "trèsgrande", "très_grande", "19", "19x19" -> 9;
            case "festival", "23", "23x23" -> 11;
            default -> 5;
        };
    }

    private enum Side { WEST, EAST, NORTH, SOUTH }

    private record Bounds(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {}
}
