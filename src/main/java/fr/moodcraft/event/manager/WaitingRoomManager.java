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
        int height = height(radius);
        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        backup(center, radius, height);
        generate(center, radius, height);

        spawn = center.clone().add(0, 1, 0);
        spawn.setYaw(player.getLocation().getYaw());
        spawn.setPitch(player.getLocation().getPitch());
        active = true;

        config.set("active", true);
        config.set("radius", radius);
        config.set("height", height);
        config.set("size-name", rawSize == null ? "moyenne" : rawSize.toLowerCase(Locale.ROOT));
        writeLocation("spawn", spawn);
        save();

        player.teleport(spawn);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.25f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Salle d'attente générée.",
                MoodStyle.detail("Taille : §e" + ((radius * 2) + 1) + "x" + ((radius * 2) + 1)),
                MoodStyle.detail("Zone sauvegardée avant construction."),
                MoodStyle.detail("Restauration : §e/eventrestaurersalle")
        );
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

    private static void backup(Location center, int radius, int height) {
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
            for (int y = cy; y <= cy + height; y++) {
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

    private static void generate(Location center, int radius, int height) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy; y <= cy + height; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    boolean borderX = x == cx - radius || x == cx + radius;
                    boolean borderZ = z == cz - radius || z == cz + radius;
                    boolean border = borderX || borderZ;
                    boolean corner = borderX && borderZ;
                    boolean floor = y == cy;
                    boolean roof = y == cy + height;
                    boolean inner = !border && !floor && !roof;

                    if (floor) {
                        block.setType(floorMaterial(cx, cz, x, z), false);
                    } else if (roof) {
                        block.setType(roofMaterial(cx, cz, x, z), false);
                    } else if (corner) {
                        block.setType(Material.STRIPPED_DARK_OAK_LOG, false);
                    } else if (border) {
                        block.setType(wallMaterial(y, cy, height), false);
                    } else if (inner) {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }

        decorate(world, cx, cy, cz, radius, height);
    }

    private static Material floorMaterial(int cx, int cz, int x, int z) {
        int dx = Math.abs(x - cx);
        int dz = Math.abs(z - cz);
        if (dx <= 1 && dz <= 1) {
            return Material.GOLD_BLOCK;
        }
        if (dx == dz || dx == 0 || dz == 0) {
            return Material.POLISHED_BLACKSTONE_BRICKS;
        }
        if ((x + z) % 2 == 0) {
            return Material.POLISHED_DEEPSLATE;
        }
        return Material.DEEPSLATE_TILES;
    }

    private static Material roofMaterial(int cx, int cz, int x, int z) {
        if (x == cx || z == cz) {
            return Material.DARK_OAK_SLAB;
        }
        return Material.SMOOTH_STONE_SLAB;
    }

    private static Material wallMaterial(int y, int cy, int height) {
        int relative = y - cy;
        if (relative == 1 || relative == height - 1) {
            return Material.DARK_OAK_PLANKS;
        }
        if (relative == 2 || relative == 3) {
            return Material.TINTED_GLASS;
        }
        return Material.SPRUCE_PLANKS;
    }

    private static void decorate(World world, int cx, int cy, int cz, int radius, int height) {
        int[][] corners = {
                {cx - radius + 1, cz - radius + 1},
                {cx + radius - 1, cz - radius + 1},
                {cx - radius + 1, cz + radius - 1},
                {cx + radius - 1, cz + radius - 1}
        };

        for (int[] point : corners) {
            world.getBlockAt(point[0], cy + 1, point[1]).setType(Material.LANTERN, false);
            if (height >= 7) {
                world.getBlockAt(point[0], cy + height - 1, point[1]).setType(Material.SEA_LANTERN, false);
            }
        }

        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);

        if (radius >= 5) {
            world.getBlockAt(cx + 2, cy + 1, cz).setType(Material.OAK_STAIRS, false);
            world.getBlockAt(cx - 2, cy + 1, cz).setType(Material.OAK_STAIRS, false);
            world.getBlockAt(cx, cy + 1, cz + 2).setType(Material.OAK_STAIRS, false);
            world.getBlockAt(cx, cy + 1, cz - 2).setType(Material.OAK_STAIRS, false);
        }

        if (radius >= 9) {
            world.getBlockAt(cx + radius - 2, cy + 1, cz).setType(Material.BARREL, false);
            world.getBlockAt(cx - radius + 2, cy + 1, cz).setType(Material.BARREL, false);
            world.getBlockAt(cx, cy + 1, cz + radius - 2).setType(Material.CRAFTING_TABLE, false);
            world.getBlockAt(cx, cy + 1, cz - radius + 2).setType(Material.CARTOGRAPHY_TABLE, false);
        }
    }

    private static int radius(String text) {
        if (text == null) {
            return 5;
        }
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

    private static int height(int radius) {
        if (radius >= 11) {
            return 9;
        }
        if (radius >= 9) {
            return 8;
        }
        if (radius >= 7) {
            return 7;
        }
        return 6;
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
