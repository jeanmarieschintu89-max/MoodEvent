package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public final class GeneratedGameManager {

    private static final Random RANDOM = new Random();
    private static File file;
    private static FileConfiguration config;
    private static boolean active;

    private GeneratedGameManager() {
    }

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "generated-game.yml");
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
    }

    public static void save() {
        if (config == null || file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static boolean hasStructure() {
        ensureLoaded();
        return active;
    }

    public static void generate(Player player, GeneratedGameType type, GeneratedGameSize size) {
        ensureLoaded();

        if (player == null || type == null || size == null) {
            return;
        }

        if (active) {
            MoodStyle.errorMessage(
                    player,
                    MoodStyle.MODULE,
                    "Une structure générée existe déjà.",
                    MoodStyle.detail("Restaure-la avant d'en créer une autre.")
            );
            return;
        }

        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        Dimensions dimensions = dimensions(type, size);
        backup(center, dimensions);

        Location start = switch (type) {
            case LABYRINTHE -> generateMaze(center, size, dimensions);
            case JUMP -> generateJump(center, size, dimensions);
            case COURSE -> generateRace(center, size, dimensions);
            case WATER_JUMP -> generateWaterJump(center, size, dimensions);
        };

        Location finish = finishLocation(center, type, dimensions);

        active = true;
        config.set("active", true);
        config.set("type", type.name());
        config.set("size", size.name());
        writeLocation("start", start);
        writeLocation("finish", finish);
        save();

        configureEvent(player, type, start, finish, center);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.15f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Mini-jeu généré.",
                MoodStyle.detail("Type : §e" + type.getDisplayName()),
                MoodStyle.detail("Taille : §e" + size.getDisplayName()),
                MoodStyle.detail("Départ et arrivée définis automatiquement."),
                MoodStyle.detail("Restauration possible depuis le générateur.")
        );
    }

    public static void restore(Player player) {
        ensureLoaded();

        ConfigurationSection blocks = config.getConfigurationSection("backup.blocks");
        if (!active || blocks == null) {
            if (player != null) {
                MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune structure générée à restaurer.");
            }
            return;
        }

        int restored = 0;
        for (String key : blocks.getKeys(false)) {
            ConfigurationSection section = blocks.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            World world = Bukkit.getWorld(section.getString("world", ""));
            if (world == null) {
                continue;
            }

            Block block = world.getBlockAt(section.getInt("x"), section.getInt("y"), section.getInt("z"));
            String data = section.getString("data", "minecraft:air");
            try {
                block.setBlockData(Bukkit.createBlockData(data), false);
            } catch (IllegalArgumentException exception) {
                block.setType(Material.AIR, false);
            }
            restored++;
        }

        config.set("active", false);
        config.set("backup", null);
        config.set("start", null);
        config.set("finish", null);
        active = false;
        save();

        if (player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Structure restaurée.",
                    MoodStyle.detail("Blocs restaurés : §e" + restored)
            );
        }
    }

    private static void configureEvent(Player player, GeneratedGameType type, Location start, Location finish, Location returnLocation) {
        EventManager.createEvent(player, type.getDisplayName());
        EventManager.setType(player, type.getEventType().name());

        player.teleport(start);
        EventManager.setLocation(player);

        player.teleport(finish);
        EventManager.setFinishLocation(player);

        player.teleport(returnLocation.clone().add(0, 2, 0));
    }

    private static Location generateMaze(Location center, GeneratedGameSize size, Dimensions dimensions) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int half = dimensions.width / 2;

        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                boolean border = x == cx - half || x == cx + half || z == cz - half || z == cz + half;
                boolean wall = border || (x % 4 == 0 && z % 3 != 0) || (z % 5 == 0 && x % 3 != 0);
                world.getBlockAt(x, cy, z).setType(Material.STONE_BRICKS, false);
                world.getBlockAt(x, cy + 1, z).setType(wall ? Material.MOSSY_STONE_BRICKS : Material.AIR, false);
                world.getBlockAt(x, cy + 2, z).setType(wall ? Material.MOSSY_STONE_BRICKS : Material.AIR, false);
                world.getBlockAt(x, cy + 3, z).setType(Material.AIR, false);
            }
        }

        clearColumn(world, cx - half + 1, cy, cz - half + 1);
        clearColumn(world, cx + half - 1, cy, cz + half - 1);
        world.getBlockAt(cx - half + 1, cy, cz - half + 1).setType(Material.LIME_CONCRETE, false);
        world.getBlockAt(cx + half - 1, cy, cz + half - 1).setType(Material.RED_CONCRETE, false);
        return new Location(world, cx - half + 1.5, cy + 1, cz - half + 1.5);
    }

    private static Location generateJump(Location center, GeneratedGameSize size, Dimensions dimensions) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int length = dimensions.length;

        platform(world, cx, cy, cz, 2, Material.LIME_CONCRETE);
        int x = cx;
        int z = cz;
        for (int i = 1; i <= length / 4; i++) {
            x += 3 + RANDOM.nextInt(2);
            z += RANDOM.nextBoolean() ? 1 : -1;
            int y = cy + 1 + RANDOM.nextInt(3);
            platform(world, x, y, z, i % 4 == 0 ? 2 : 1, Material.SLIME_BLOCK);
        }
        platform(world, x + 4, cy + 1, z, 2, Material.RED_CONCRETE);
        return new Location(world, cx + 0.5, cy + 1, cz + 0.5);
    }

    private static Location generateRace(Location center, GeneratedGameSize size, Dimensions dimensions) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int length = dimensions.length;

        for (int x = cx; x <= cx + length; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                world.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.SMOOTH_STONE : Material.POLISHED_ANDESITE, false);
                world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
            if (x % 12 == 0) {
                world.getBlockAt(x, cy + 1, cz - 1).setType(Material.HAY_BLOCK, false);
                world.getBlockAt(x, cy + 1, cz + 1).setType(Material.HAY_BLOCK, false);
            }
        }
        platform(world, cx, cy, cz, 3, Material.LIME_CONCRETE);
        platform(world, cx + length, cy, cz, 3, Material.RED_CONCRETE);
        return new Location(world, cx + 0.5, cy + 1, cz + 0.5);
    }

    private static Location generateWaterJump(Location center, GeneratedGameSize size, Dimensions dimensions) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int length = dimensions.length;

        for (int x = cx; x <= cx + length; x++) {
            for (int z = cz - 4; z <= cz + 4; z++) {
                world.getBlockAt(x, cy, z).setType(Material.WATER, false);
                world.getBlockAt(x, cy - 1, z).setType(Material.PRISMARINE_BRICKS, false);
                world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
        }
        platform(world, cx, cy + 1, cz, 3, Material.LIME_CONCRETE);
        for (int x = cx + 5; x < cx + length; x += 5) {
            int z = cz + (RANDOM.nextInt(7) - 3);
            platform(world, x, cy + 1, z, 1, Material.OAK_PLANKS);
        }
        platform(world, cx + length, cy + 1, cz, 3, Material.RED_CONCRETE);
        return new Location(world, cx + 0.5, cy + 2, cz + 0.5);
    }

    private static Location finishLocation(Location center, GeneratedGameType type, Dimensions dimensions) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        if (type == GeneratedGameType.LABYRINTHE) {
            int half = dimensions.width / 2;
            return new Location(world, cx + half - 1.5, cy + 1, cz + half - 1.5);
        }
        return new Location(world, cx + dimensions.length + 0.5, cy + 2, cz + 0.5);
    }

    private static void backup(Location center, Dimensions dimensions) {
        config.set("backup", null);
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int index = 0;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = cx - dimensions.backupRadius; x <= cx + dimensions.length + dimensions.backupRadius; x++) {
            for (int y = cy - 1; y <= cy + dimensions.height; y++) {
                for (int z = cz - dimensions.backupRadius; z <= cz + dimensions.backupRadius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    String path = "backup.blocks." + index;
                    config.set(path + ".world", world.getName());
                    config.set(path + ".x", x);
                    config.set(path + ".y", y);
                    config.set(path + ".z", z);
                    config.set(path + ".data", block.getBlockData().getAsString());
                    index++;
                }
            }
        }
    }

    private static Dimensions dimensions(GeneratedGameType type, GeneratedGameSize size) {
        if (type == GeneratedGameType.LABYRINTHE) {
            int width = size.getSquareSize();
            return new Dimensions(width, width, 5, width / 2 + 4);
        }
        int length = size.getLength();
        return new Dimensions(9, length, 8, 8);
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                world.getBlockAt(x, cy, z).setType(material, false);
            }
        }
    }

    private static void clearColumn(World world, int x, int y, int z) {
        world.getBlockAt(x, y + 1, z).setType(Material.AIR, false);
        world.getBlockAt(x, y + 2, z).setType(Material.AIR, false);
        world.getBlockAt(x, y + 3, z).setType(Material.AIR, false);
    }

    private static void writeLocation(String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    private static void ensureLoaded() {
        if (config == null) {
            load();
        }
    }

    private record Dimensions(int width, int length, int height, int backupRadius) {
    }
}
