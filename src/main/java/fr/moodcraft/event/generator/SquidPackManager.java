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

    private static final int REGION_MIN_X = -100;
    private static final int REGION_MAX_X = 60;
    private static final int REGION_MIN_Y = -1;
    private static final int REGION_MAX_Y = 20;
    private static final int REGION_MIN_Z = -26;
    private static final int REGION_MAX_Z = 64;

    private static final int DORMITORY_X = -76;
    private static final int START_X = -34;
    private static final int PRE_SAS_X = -40;
    private static final int RED_GREEN_FINISH_X = 38;

    private static final int BRIDGE_START_X = -24;
    private static final int BRIDGE_FIRST_GLASS_X = -21;
    private static final int BRIDGE_FINISH_X = 15;
    private static final int BRIDGE_FINISH_ROOM_X = 20;
    private static final int BRIDGE_Z = 46;
    private static final int BRIDGE_LEFT_Z = 44;
    private static final int BRIDGE_RIGHT_Z = 48;
    private static final int GLASS_STEPS = 12;

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
        Location game = origin.clone().add(45, 0, 0);
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
        SquidPremiumLayoutBuilder.build(world, cx, cy, cz);

        Location dormitory = new Location(world, cx + DORMITORY_X + 0.5, cy + 1, cz + 0.5, 90f, 0f);
        Location lobby = dormitory.clone();
        Location start = new Location(world, cx + START_X + 0.5, cy + 1, cz + 0.5, 90f, 0f);
        Location preSas = new Location(world, cx + PRE_SAS_X + 0.5, cy + 1, cz + 0.5, 90f, 0f);
        Location returnSas = dormitory.clone();
        Location bridge = new Location(world, cx + BRIDGE_START_X + 0.5, cy + 2, cz + BRIDGE_Z + 0.5, 90f, 0f);
        Location finish = new Location(world, cx + BRIDGE_FINISH_ROOM_X + 0.5, cy + 2, cz + BRIDGE_Z + 0.5, 90f, 0f);

        SquidWaitingRoomBridge.registerDormitory(dormitory);

        config.set("active", true);
        config.set("stage", "WAITING");
        config.set("layout", "PREMIUM_SINGLE_BUILDER_V2");
        config.set("red-green.green", true);
        config.set("red-green.timer", 0);
        config.set("region.world", world.getName());
        config.set("region.min-x", cx + REGION_MIN_X);
        config.set("region.max-x", cx + REGION_MAX_X);
        config.set("region.min-y", cy + REGION_MIN_Y);
        config.set("region.max-y", cy + REGION_MAX_Y);
        config.set("region.min-z", cz + REGION_MIN_Z);
        config.set("region.max-z", cz + REGION_MAX_Z);
        writeLocation("start", start);
        writeLocation("lobby", lobby);
        writeLocation("dormitory", dormitory);
        writeLocation("pre-sas", preSas);
        writeLocation("return-sas", returnSas);
        writeLocation("bridge-start", bridge);
        writeLocation("bridge-finish", finish);
        config.set("red-green.finish-x", cx + RED_GREEN_FINISH_X);
        config.set("glass.finish-x", cx + BRIDGE_FINISH_X);
        config.set("glass.step-start-x", cx + BRIDGE_FIRST_GLASS_X);
        config.set("glass.steps", GLASS_STEPS);
        config.set("glass.z-left", cz + BRIDGE_LEFT_Z);
        config.set("glass.z-right", cz + BRIDGE_RIGHT_Z);
        config.set("glass.safe", null);
        for (int i = 0; i < GLASS_STEPS; i++) config.set("glass.safe." + i, RANDOM.nextBoolean() ? "LEFT" : "RIGHT");
        config.set("players", null);
        save();

        EventManager.createEvent(null, GAME_NAME);
        EventManager.setDescription(null, "Une suite d'épreuves show : dortoir, Feu Rouge / Feu Vert et Pont de Verre.");
        EventManager.setType(null, "custom");
        player.teleport(start);
        EventManager.setLocation(null);
        player.teleport(origin);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.1f);
        sendSquidMessage(player,
                "§a▶ §fPack §e" + GAME_NAME + " §fgénéré.",
                "§b◆ §fDortoir fermé, sas, arène et pont séparés.",
                "§d➜ §fOuvre la file avec §e/eventouvrir§f."
        );
        EventLogManager.log(player, "Pack " + GAME_NAME, "Pack premium single builder généré");
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
        config.set("layout", null);
        config.set("players", null);
        config.set("backup", null);
        save();
        if (player != null) sendSquidMessage(player, "§a▶ §fPack §e" + GAME_NAME + " §frestauré.", "§b◆ §fBlocs restaurés : §e" + restored);
    }

    public static Location location(String path) {
        ensureLoaded();
        return readLocation(path);
    }

    private static void backup(World world, int cx, int cy, int cz) {
        config.set("backup", null);
        int index = 0;
        for (int x = cx + REGION_MIN_X; x <= cx + REGION_MAX_X; x++) {
            for (int y = cy + REGION_MIN_Y; y <= cy + REGION_MAX_Y; y++) {
                for (int z = cz + REGION_MIN_Z; z <= cz + REGION_MAX_Z; z++) {
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
        for (int x = cx + REGION_MIN_X; x <= cx + REGION_MAX_X; x++) {
            for (int y = cy; y <= cy + REGION_MAX_Y; y++) {
                for (int z = cz + REGION_MIN_Z; z <= cz + REGION_MAX_Z; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
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

    private static void sendSquidMessage(Player player, String... lines) {
        if (player == null) return;
        player.sendMessage("§8----- §c§l✦ SQUID MOOD GAME ✦ §8-----");
        if (lines != null) for (String line : lines) player.sendMessage(line);
        player.sendMessage("§8-----------------------------");
    }

    private static void ensureLoaded() {
        if (config == null) load();
    }
}
