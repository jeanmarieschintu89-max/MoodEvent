package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
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
import java.util.Locale;

public final class WaitingRoomManager {

    private static File file;
    private static FileConfiguration config;
    private static Location spawn;
    private static boolean active;

    private WaitingRoomManager() {
    }

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
        if (config == null || file == null) {
            return;
        }
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

    public static void teleport(Player player) {
        if (!hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune salle d'attente générée.", MoodStyle.detail("Commande : §e/eventsalleattente"));
            return;
        }
        player.teleport(spawn);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
    }

    public static void build(Player player, String rawSize) {
        if (hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une salle d'attente existe déjà.", MoodStyle.detail("Restaurez-la avec §e/eventrestaurersalle"));
            return;
        }

        int radius = radius(rawSize);
        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        backup(center, radius);
        generate(center, radius);

        spawn = center.clone().add(0, 1, 0);
        spawn.setYaw(player.getLocation().getYaw());
        spawn.setPitch(player.getLocation().getPitch());
        active = true;

        config.set("active", true);
        config.set("radius", radius);
        writeLocation("spawn", spawn);
        save();

        player.teleport(spawn);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.25f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Salle d'attente générée.", MoodStyle.detail("Zone sauvegardée avant construction."), MoodStyle.detail("Restauration : §e/eventrestaurersalle"));
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
        config.set("spawn", null);
        config.set("backup", null);
        active = false;
        spawn = null;
        save();

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Salle d'attente restaurée.", MoodStyle.detail("Blocs restaurés : §e" + restored), MoodStyle.detail("La zone d'attente a été supprimée."));
    }

    private static void backup(Location center, int radius) {
        config.set("backup", null);
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int index = 0;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy; y <= cy + 5; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
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

    private static void generate(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy; y <= cy + 5; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    boolean border = x == cx - radius || x == cx + radius || z == cz - radius || z == cz + radius;
                    boolean floor = y == cy;
                    boolean roof = y == cy + 5;
                    Block block = world.getBlockAt(x, y, z);
                    if (floor) {
                        block.setType(Material.POLISHED_DEEPSLATE, false);
                    } else if (roof) {
                        block.setType(Material.DARK_OAK_SLAB, false);
                    } else if (border) {
                        block.setType(y == cy + 2 || y == cy + 3 ? Material.TINTED_GLASS : Material.DARK_OAK_PLANKS, false);
                    } else {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }
        world.getBlockAt(cx - radius + 1, cy + 1, cz - radius + 1).setType(Material.LANTERN, false);
        world.getBlockAt(cx + radius - 1, cy + 1, cz - radius + 1).setType(Material.LANTERN, false);
        world.getBlockAt(cx - radius + 1, cy + 1, cz + radius - 1).setType(Material.LANTERN, false);
        world.getBlockAt(cx + radius - 1, cy + 1, cz + radius - 1).setType(Material.LANTERN, false);
        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
    }

    private static int radius(String text) {
        if (text == null) {
            return 5;
        }
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "small", "petit", "7", "7x7" -> 3;
            case "large", "grand", "15", "15x15" -> 7;
            default -> 5;
        };
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
        if (world == null) {
            return null;
        }
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"), (float) config.getDouble(path + ".yaw"), (float) config.getDouble(path + ".pitch"));
    }
}
