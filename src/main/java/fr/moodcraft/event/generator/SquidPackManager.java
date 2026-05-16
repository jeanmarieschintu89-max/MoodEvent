package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.EventLogManager;
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

public final class SquidPackManager {

    public static final String GAME_NAME = "SquidMoodGame";

    private static final Random RANDOM = new Random();
    private static File file;
    private static FileConfiguration config;

    private SquidPackManager() {
    }

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "survie-des-jeux.yml");
        if (!file.exists()) {
            try {
                Main.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                Main.getInstance().getLogger().warning(exception.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        if (file == null || config == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static boolean hasPack() {
        ensureLoaded();
        return config.getBoolean("active", false);
    }

    public static String getStage() {
        ensureLoaded();
        return config.getString("stage", "NONE");
    }

    public static void setStage(String stage) {
        ensureLoaded();
        config.set("stage", stage);
        save();
    }

    public static FileConfiguration config() {
        ensureLoaded();
        return config;
    }

    public static void generate(Player player) {
        ensureLoaded();
        if (player == null) return;
        if (hasPack()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Un pack " + GAME_NAME + " existe déjà.", MoodStyle.detail("Restaure-le avant d'en créer un autre."));
            return;
        }
        if (GeneratedGameManager.hasStructure()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une structure générée existe déjà.", MoodStyle.detail("Restaure-la avant de créer ce pack."));
            return;
        }

        Location origin = player.getLocation().clone();
        Location game = origin.clone().add(35, 0, 0);
        World world = game.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        int cx = game.getBlockX();
        int cy = game.getBlockY();
        int cz = game.getBlockZ();
        backup(world, cx, cy, cz);
        clear(world, cx, cy, cz);
        SquidMoodDecorBuilder.buildDormitory(world, cx, cy, cz);
        SquidMoodDecorBuilder.buildPreGameSas(world, cx, cy, cz);
        buildRedGreen(world, cx, cy, cz);
        SquidMoodDecorBuilder.buildReturnSas(world, cx, cy, cz);
        buildGlassBridge(world, cx, cy, cz);
        SquidSetPolishBuilder.polish(world, cx, cy, cz);

        Location start = new Location(world, cx - 52 + 0.5, cy + 1, cz + 0.5, 90f, 0f);
        Location bridge = new Location(world, cx - 4 + 0.5, cy + 2, cz + 11.5, 90f, 0f);
        Location lobby = new Location(world, cx - 47 + 0.5, cy + 1, cz + 13.5, 90f, 0f);
        Location dormitory = new Location(world, cx - 43 + 0.5, cy + 1, cz - 13.5, 0f, 0f);
        Location preSas = new Location(world, cx - 52 + 0.5, cy + 1, cz + 0.5, 90f, 0f);
        Location returnSas = new Location(world, cx - 8 + 0.5, cy + 1, cz + 11.5, 90f, 0f);
        Location finish = new Location(world, cx + 27 + 0.5, cy + 2, cz + 11.5, 90f, 0f);

        SquidWaitingRoomBridge.registerDormitory(dormitory);

        config.set("active", true);
        config.set("stage", "WAITING");
        config.set("red-green.green", true);
        config.set("red-green.timer", 0);
        config.set("region.world", world.getName());
        config.set("region.min-x", cx - 55);
        config.set("region.max-x", cx + 35);
        config.set("region.min-y", cy - 1);
        config.set("region.max-y", cy + 14);
        config.set("region.min-z", cz - 18);
        config.set("region.max-z", cz + 18);
        writeLocation("start", start);
        writeLocation("lobby", lobby);
        writeLocation("dormitory", dormitory);
        writeLocation("pre-sas", preSas);
        writeLocation("return-sas", returnSas);
        writeLocation("bridge-start", bridge);
        writeLocation("bridge-finish", finish);
        config.set("red-green.finish-x", cx - 10);
        config.set("glass.finish-x", cx + 26);
        config.set("glass.z-left", cz + 10);
        config.set("glass.z-right", cz + 13);
        for (int i = 0; i < 10; i++) {
            config.set("glass.safe." + i, RANDOM.nextBoolean() ? "LEFT" : "RIGHT");
        }
        config.set("players", null);
        save();

        EventManager.createEvent(player, GAME_NAME);
        EventManager.setDescription(player, "Une suite d'épreuves Minecraft : dortoir, sas, feu rouge et pont de verre.");
        EventManager.setType(player, "custom");
        player.teleport(start);
        EventManager.setLocation(player);
        player.teleport(origin);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.1f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Pack " + GAME_NAME + " généré.", MoodStyle.detail("Dortoir utilisé comme zone d'attente."), MoodStyle.detail("Aucune salle d'attente classique générée."), MoodStyle.detail("Décor Squid complet appliqué."), MoodStyle.info("Ouvre la file avec §e/eventouvrir"));
        EventLogManager.log(player, "Pack " + GAME_NAME, "Pack spécial généré avec décor Squid complet");
    }

    public static void restore(Player player) {
        ensureLoaded();
        ConfigurationSection blocks = config.getConfigurationSection("backup.blocks");
        if (!hasPack() || blocks == null) {
            if (player != null) MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun pack " + GAME_NAME + " à restaurer.");
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
        config.set("stage", null);
        config.set("players", null);
        config.set("backup", null);
        save();
        if (player != null) MoodStyle.successMessage(player, MoodStyle.MODULE, "Pack " + GAME_NAME + " restauré.", MoodStyle.detail("Blocs restaurés : §e" + restored));
    }

    public static Location location(String path) {
        ensureLoaded();
        return readLocation(path);
    }

    private static void buildRedGreen(World world, int cx, int cy, int cz) {
        for (int x = cx - 48; x <= cx - 8; x++) {
            for (int z = cz - 5; z <= cz + 5; z++) {
                world.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.WHITE_CONCRETE : Material.LIGHT_GRAY_CONCRETE, false);
                world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
        }
        platform(world, cx - 45, cy, cz, 4, Material.LIME_CONCRETE);
        platform(world, cx - 10, cy, cz, 4, Material.RED_CONCRETE);
        line(world, cx - 49, cy + 1, cz - 6, cx - 7, cz - 6, Material.IRON_BARS);
        line(world, cx - 49, cy + 1, cz + 6, cx - 7, cz + 6, Material.IRON_BARS);
        for (int y = cy + 1; y <= cy + 6; y++) world.getBlockAt(cx - 5, y, cz).setType(Material.REDSTONE_BLOCK, false);
        world.getBlockAt(cx - 5, cy + 7, cz).setType(Material.SEA_LANTERN, false);
    }

    private static void buildGlassBridge(World world, int cx, int cy, int cz) {
        int zLeft = cz + 10;
        int zRight = cz + 13;
        platform(world, cx - 5, cy + 1, zLeft + 1, 3, Material.LIME_CONCRETE);
        platform(world, cx + 28, cy + 1, zLeft + 1, 3, Material.RED_CONCRETE);
        for (int i = 0; i < 10; i++) {
            int x = cx - 1 + (i * 3);
            platform(world, x, cy + 1, zLeft, 1, Material.GLASS);
            platform(world, x, cy + 1, zRight, 1, Material.GLASS);
            world.getBlockAt(x, cy, zLeft).setType(Material.SEA_LANTERN, false);
            world.getBlockAt(x, cy, zRight).setType(Material.SEA_LANTERN, false);
        }
        line(world, cx - 8, cy + 1, cz + 7, cx + 30, cz + 7, Material.CYAN_STAINED_GLASS);
        line(world, cx - 8, cy + 1, cz + 16, cx + 30, cz + 16, Material.CYAN_STAINED_GLASS);
    }

    private static void backup(World world, int cx, int cy, int cz) {
        config.set("backup", null);
        int index = 0;
        for (int x = cx - 55; x <= cx + 35; x++) {
            for (int y = cy - 1; y <= cy + 14; y++) {
                for (int z = cz - 18; z <= cz + 18; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    String path = "backup.blocks." + index++;
                    config.set(path + ".world", world.getName());
                    config.set(path + ".x", x);
                    config.set(path + ".y", y);
                    config.set(path + ".z", z);
                    config.set(path + ".data", block.getBlockData().getAsString());
                }
            }
        }
    }

    private static void clear(World world, int cx, int cy, int cz) {
        for (int x = cx - 55; x <= cx + 35; x++) {
            for (int y = cy; y <= cy + 14; y++) {
                for (int z = cz - 18; z <= cz + 18; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    private static void line(World world, int x1, int y, int z1, int x2, int z2, Material material) {
        if (x1 != x2) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) world.getBlockAt(x, y, z1).setType(material, false);
        } else {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) world.getBlockAt(x1, y, z).setType(material, false);
        }
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) world.getBlockAt(x, cy, z).setType(material, false);
    }

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

    private static void ensureLoaded() {
        if (config == null) load();
    }
}
