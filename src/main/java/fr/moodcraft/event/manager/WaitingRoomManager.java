package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class WaitingRoomManager {

    private static final Map<UUID, String> SELECTED_STYLE = new HashMap<>();
    private static final String DEFAULT_STYLE_KEY = "moodcraft";

    private static File file;
    private static FileConfiguration config;
    private static Location spawn;
    private static boolean active;

    private WaitingRoomManager() {}

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "waiting-room.yml");
        if (!file.exists()) {
            try {
                Main.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                Main.getInstance().getLogger().warning(exception.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        active = config.getBoolean("active", false);
        spawn = readLocation("spawn");
    }

    public static void save() {
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static boolean hasRoom() {
        return active && spawn != null && spawn.getWorld() != null;
    }

    public static Location getSpawn() {
        return hasRoom() ? spawn.clone() : null;
    }

    public static void setSelectedStyle(Player player, String style) {
        if (player == null) return;
        SELECTED_STYLE.put(player.getUniqueId(), WaitingRoomTheme.key(style));
    }

    public static WaitingRoomTheme getSelectedTheme(Player player) {
        if (player == null) return WaitingRoomTheme.MOODCRAFT;
        return WaitingRoomTheme.of(SELECTED_STYLE.getOrDefault(player.getUniqueId(), DEFAULT_STYLE_KEY));
    }

    public static String getSelectedStyle(Player player) {
        return getSelectedTheme(player).key();
    }

    public static WaitingRoomTheme cycleSelectedStyle(Player player) {
        WaitingRoomTheme next = getSelectedTheme(player).next();
        setSelectedStyle(player, next.key());
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Style de salle sélectionné.", MoodStyle.detail("Style : §e" + next.displayName()), MoodStyle.detail("Ce choix touche uniquement la salle d'attente."));
        return next;
    }

    public static String formatStyle(String style) {
        return WaitingRoomTheme.of(style).displayName();
    }

    public static void teleport(Player player) {
        if (!hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune salle d'attente générée.", MoodStyle.detail("Commande : §e/eventsalleattente"));
            return;
        }
        player.teleport(spawn);
        if (currentTheme() == WaitingRoomTheme.TRAIN_TUNNEL) {
            TrainTunnelWaitingRoomBuilder.board(player, spawn);
            player.playSound(player.getLocation(), Sound.ENTITY_MINECART_RIDING, 0.8f, 1.1f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
        }
    }

    public static void build(Player player, String rawSize) {
        if (hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une salle d'attente existe déjà.", MoodStyle.detail("Restaurez-la avec §e/eventrestaurersalle"));
            return;
        }

        WaitingRoomTheme theme = getSelectedTheme(player);
        boolean trainTunnel = theme == WaitingRoomTheme.TRAIN_TUNNEL;
        int radius = trainTunnel ? TrainTunnelWaitingRoomBuilder.radius() : radius(rawSize);
        int height = trainTunnel ? TrainTunnelWaitingRoomBuilder.height() : height(radius);
        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        int cx = center.getBlockX();
        int cz = center.getBlockZ();

        prepareChunks(world, cx, cz, radius);
        backup(center, radius, height);
        if (trainTunnel) {
            TrainTunnelWaitingRoomBuilder.build(center);
            spawn = TrainTunnelWaitingRoomBuilder.spawn(center);
        } else {
            generate(center, radius, height, theme);
            spawn = center.clone().add(0, 1, 0);
        }
        refreshChunks(world, cx, cz, radius);

        spawn.setYaw(player.getLocation().getYaw());
        spawn.setPitch(player.getLocation().getPitch());
        active = true;

        config.set("active", true);
        config.set("radius", radius);
        config.set("height", height);
        config.set("style", theme.key());
        config.set("size-name", trainTunnel ? "train_tunnel" : rawSize == null ? "moyenne" : rawSize.toLowerCase(Locale.ROOT));
        writeLocation("spawn", spawn);
        save();

        player.teleport(spawn);
        if (trainTunnel) TrainTunnelWaitingRoomBuilder.board(player, spawn);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.25f);
        MoodStyle.successMessage(player, MoodStyle.MODULE,
                trainTunnel ? "Salle Train Tunnel générée." : "Salle d'attente générée.",
                trainTunnel ? MoodStyle.detail("Type : §eTunnel carré 3D avec wagons") : MoodStyle.detail("Taille : §e" + ((radius * 2) + 1) + "x" + ((radius * 2) + 1)),
                MoodStyle.detail("Style salle : §e" + theme.displayName()),
                trainTunnel ? MoodStyle.detail("Les joueurs seront placés directement dans un wagon.") : MoodStyle.detail("Zone sauvegardée avant construction."),
                MoodStyle.detail("Restauration : §e/eventrestaurersalle"));
    }

    public static void restore(Player player) {
        ConfigurationSection blocks = config == null ? null : config.getConfigurationSection("backup.blocks");
        if (!active || blocks == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune salle d'attente à restaurer.", MoodStyle.detail("Commande : §e/eventsalleattente"));
            return;
        }

        int restored = 0;
        for (String key : blocks.getKeys(false)) {
            ConfigurationSection section = blocks.getConfigurationSection(key);
            if (section == null) continue;
            World world = Bukkit.getWorld(section.getString("world", ""));
            if (world == null) continue;
            Block block = world.getBlockAt(section.getInt("x"), section.getInt("y"), section.getInt("z"));
            try {
                block.setBlockData(Bukkit.createBlockData(section.getString("data", "minecraft:air")), false);
            } catch (IllegalArgumentException exception) {
                block.setType(Material.AIR, false);
            }
            restored++;
        }

        config.set("active", false);
        config.set("spawn", null);
        config.set("backup", null);
        active = false;
        spawn = null;
        save();

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Salle d'attente restaurée.", MoodStyle.detail("Blocs restaurés : §e" + restored), MoodStyle.detail("La zone d'attente a été supprimée."));
    }

    private static WaitingRoomTheme currentTheme() {
        if (config == null) return WaitingRoomTheme.MOODCRAFT;
        return WaitingRoomTheme.of(config.getString("style", DEFAULT_STYLE_KEY));
    }

    private static void backup(Location center, int radius, int height) {
        config.set("backup", null);
        World world = center.getWorld();
        if (world == null) return;
        int index = 0;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) for (int y = cy - 2; y <= cy + height; y++) for (int z = cz - radius; z <= cz + radius; z++) {
            Block block = world.getBlockAt(x, y, z);
            String path = "backup.blocks." + index++;
            config.set(path + ".world", world.getName());
            config.set(path + ".x", x);
            config.set(path + ".y", y);
            config.set(path + ".z", z);
            config.set(path + ".data", block.getBlockData().getAsString());
        }
    }

    private static void generate(Location center, int radius, int height, WaitingRoomTheme theme) {
        World world = center.getWorld();
        if (world == null) return;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy; y <= cy + height; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    boolean borderX = x == cx - radius || x == cx + radius;
                    boolean borderZ = z == cz - radius || z == cz + radius;
                    boolean border = borderX || borderZ;
                    boolean floor = y == cy;
                    boolean roof = y == cy + height;
                    Block block = world.getBlockAt(x, y, z);
                    if (floor) block.setType(floorMaterial(theme, cx, cz, x, z), false);
                    else if (roof) block.setType(roofMaterial(theme, cx, cz, x, z), false);
                    else if (border) block.setType(wallBlock(theme, x, y, z, cx, cy, cz, radius, height), false);
                    else block.setType(Material.AIR, false);
                }
            }
        }
        decorate(world, cx, cy, cz, radius, height, theme);
    }

    private static Material floorMaterial(WaitingRoomTheme theme, int cx, int cz, int x, int z) {
        int dx = Math.abs(x - cx);
        int dz = Math.abs(z - cz);
        if (dx <= 1 && dz <= 1) return solidLightFor(theme);
        if (dx == dz || dx == 0 || dz == 0) return solidBlockFor(theme.accent());
        return (x + z) % 2 == 0 ? solidBlockFor(theme.primary()) : Material.SMOOTH_STONE;
    }

    private static Material roofMaterial(WaitingRoomTheme theme, int cx, int cz, int x, int z) {
        int dx = Math.abs(x - cx);
        int dz = Math.abs(z - cz);
        if (dx == 0 || dz == 0 || dx == dz) return solidBlockFor(theme.accent());
        return solidBlockFor(theme.primary());
    }

    private static Material wallBlock(WaitingRoomTheme theme, int x, int y, int z, int cx, int cy, int cz, int radius, int height) {
        int relative = y - cy;
        boolean corner = (Math.abs(x - cx) == radius) && (Math.abs(z - cz) == radius);
        boolean post = corner || x == cx || z == cz || isQuarterPost(x, z, cx, cz, radius);
        if (post) return solidBlockFor(theme.accent());
        if (relative == 1 || relative == height - 1) return solidBlockFor(theme.primary());
        if (relative == 2 || (height >= 5 && relative == 3)) return theme.glass();
        return solidBlockFor(theme.primary());
    }

    private static boolean isQuarterPost(int x, int z, int cx, int cz, int radius) {
        if (radius < 7) return false;
        int q = Math.max(2, radius / 2);
        return Math.abs(x - cx) == q || Math.abs(z - cz) == q;
    }

    private static Material solidLightFor(WaitingRoomTheme theme) {
        return switch (theme.light()) {
            case GLOWSTONE, SHROOMLIGHT, SEA_LANTERN, REDSTONE_LAMP -> theme.light();
            default -> Material.SEA_LANTERN;
        };
    }

    private static Material solidBlockFor(Material material) {
        return switch (material) {
            case LANTERN, SOUL_LANTERN, END_ROD -> Material.SEA_LANTERN;
            default -> material;
        };
    }

    private static void decorate(World world, int cx, int cy, int cz, int radius, int height, WaitingRoomTheme theme) {
        int[][] corners = {{cx - radius + 1, cz - radius + 1}, {cx + radius - 1, cz - radius + 1}, {cx - radius + 1, cz + radius - 1}, {cx + radius - 1, cz + radius - 1}};
        for (int[] point : corners) {
            world.getBlockAt(point[0], cy + 1, point[1]).setType(theme.light(), false);
            world.getBlockAt(point[0], cy + height - 1, point[1]).setType(theme.light(), false);
        }
        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
        buildCornerCouches(world, cx, cy, cz, radius, theme);
        if (theme == WaitingRoomTheme.NEON_LOUNGE) {
            NeonLoungeWaitingRoomDecorator.decorate(world, cx, cy, cz, radius, height);
            return;
        }
        if (theme == WaitingRoomTheme.PRISON_CELL) {
            PrisonCellWaitingRoomDecorator.decorate(world, cx, cy, cz, radius, height);
            return;
        }
        if (radius >= 9) {
            world.getBlockAt(cx + radius - 2, cy + 1, cz).setType(solidBlockFor(theme.accent()), false);
            world.getBlockAt(cx - radius + 2, cy + 1, cz).setType(solidBlockFor(theme.accent()), false);
            world.getBlockAt(cx, cy + 1, cz + radius - 2).setType(Material.CRAFTING_TABLE, false);
            world.getBlockAt(cx, cy + 1, cz - radius + 2).setType(Material.CARTOGRAPHY_TABLE, false);
        }
    }

    private static void buildCornerCouches(World world, int cx, int cy, int cz, int radius, WaitingRoomTheme theme) {
        if (radius < 3) return;
        int seatY = cy + 1;
        int westSeatX = cx - radius + 1;
        int eastSeatX = cx + radius - 1;
        int northSeatZ = cz - radius + 1;
        int southSeatZ = cz + radius - 1;
        boolean withInnerArm = radius >= 4;
        buildTwoSeatCouch(world, westSeatX, seatY, northSeatZ, 1, 0, CouchSide.NORTH, theme, withInnerArm);
        buildTwoSeatCouch(world, eastSeatX, seatY, northSeatZ, -1, 0, CouchSide.NORTH, theme, withInnerArm);
        buildTwoSeatCouch(world, westSeatX, seatY, southSeatZ, 1, 0, CouchSide.SOUTH, theme, withInnerArm);
        buildTwoSeatCouch(world, eastSeatX, seatY, southSeatZ, -1, 0, CouchSide.SOUTH, theme, withInnerArm);
    }

    private static void buildTwoSeatCouch(World world, int startX, int seatY, int startZ, int stepX, int stepZ, CouchSide side, WaitingRoomTheme theme, boolean withInnerArm) {
        Material stair = stairFor(theme.primary());
        Material frame = solidBlockFor(theme.primary());
        Material accent = solidBlockFor(theme.accent());
        for (int i = 0; i < 2; i++) {
            int x = startX + (stepX * i);
            int z = startZ + (stepZ * i);
            setCouchSeat(world, x, seatY, z, stair, side);
            setCouchBack(world, x, seatY, z, side, frame);
        }
        if (withInnerArm) {
            int armX = startX + (stepX * 2);
            int armZ = startZ + (stepZ * 2);
            world.getBlockAt(armX, seatY, armZ).setType(accent, false);
            world.getBlockAt(armX, seatY + 1, armZ).setType(accent, false);
        }
    }

    private static void setCouchBack(World world, int x, int y, int z, CouchSide side, Material frame) {
        int backX = x + backOffsetX(side);
        int backZ = z + backOffsetZ(side);
        world.getBlockAt(backX, y, backZ).setType(frame, false);
        world.getBlockAt(backX, y + 1, backZ).setType(frame, false);
    }

    private static int backOffsetX(CouchSide side) { return switch (side) { case WEST -> -1; case EAST -> 1; default -> 0; }; }
    private static int backOffsetZ(CouchSide side) { return switch (side) { case NORTH -> -1; case SOUTH -> 1; default -> 0; }; }

    private static void setCouchSeat(World world, int x, int y, int z, Material stairMaterial, CouchSide side) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(stairMaterial, false);
        BlockData data = block.getBlockData();
        if (data instanceof Stairs stairs) {
            switch (side) { case NORTH -> stairs.setFacing(BlockFace.NORTH); case SOUTH -> stairs.setFacing(BlockFace.SOUTH); case WEST -> stairs.setFacing(BlockFace.WEST); case EAST -> stairs.setFacing(BlockFace.EAST); }
            stairs.setHalf(Stairs.Half.BOTTOM);
            block.setBlockData(stairs, false);
        }
    }

    private static Material stairFor(Material material) {
        return switch (material) {
            case QUARTZ_BLOCK, SMOOTH_QUARTZ, WHITE_CONCRETE -> Material.QUARTZ_STAIRS;
            case PURPUR_BLOCK -> Material.PURPUR_STAIRS;
            case OAK_LOG, OAK_PLANKS, JUNGLE_LOG, JUNGLE_PLANKS -> Material.OAK_STAIRS;
            case SPRUCE_LOG, PACKED_ICE, SNOW_BLOCK -> Material.SPRUCE_STAIRS;
            case SANDSTONE, SMOOTH_SANDSTONE, CHISELED_SANDSTONE -> Material.SANDSTONE_STAIRS;
            case PRISMARINE_BRICKS, DARK_PRISMARINE -> Material.PRISMARINE_STAIRS;
            case CUT_COPPER, COPPER_BLOCK -> Material.CUT_COPPER_STAIRS;
            case POLISHED_BLACKSTONE, BLACKSTONE, POLISHED_BLACKSTONE_BRICKS, GILDED_BLACKSTONE -> Material.BLACKSTONE_STAIRS;
            case WARPED_PLANKS, WARPED_WART_BLOCK -> Material.WARPED_STAIRS;
            case CRIMSON_PLANKS, CRIMSON_NYLIUM -> Material.CRIMSON_STAIRS;
            case STONE_BRICKS, MOSSY_COBBLESTONE, DEEPSLATE_BRICKS -> Material.STONE_BRICK_STAIRS;
            default -> Material.STONE_BRICK_STAIRS;
        };
    }

    private enum CouchSide { NORTH, SOUTH, EAST, WEST }

    private static void prepareChunks(World world, int centerX, int centerZ, int radius) {
        int margin = Math.max(4, radius / 2);
        int minChunkX = (centerX - radius - margin) >> 4;
        int maxChunkX = (centerX + radius + margin) >> 4;
        int minChunkZ = (centerZ - radius - margin) >> 4;
        int maxChunkZ = (centerZ + radius + margin) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) world.loadChunk(chunkX, chunkZ, true);
    }

    private static void refreshChunks(World world, int centerX, int centerZ, int radius) {
        int margin = Math.max(4, radius / 2);
        int minChunkX = (centerX - radius - margin) >> 4;
        int maxChunkX = (centerX + radius + margin) >> 4;
        int minChunkZ = (centerZ - radius - margin) >> 4;
        int maxChunkZ = (centerZ + radius + margin) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) world.refreshChunk(chunkX, chunkZ);
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

    private static int height(int radius) { if (radius >= 11) return 6; if (radius >= 7) return 5; return 4; }

    private static void writeLocation(String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    private static Location readLocation(String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"), (float) config.getDouble(path + ".yaw"), (float) config.getDouble(path + ".pitch"));
    }
}
