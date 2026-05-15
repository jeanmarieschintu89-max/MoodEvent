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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class WaitingRoomManager {

    private static final Map<UUID, String> SELECTED_STYLE = new HashMap<>();

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

    public static void setSelectedStyle(Player player, String style) {
        if (player == null) {
            return;
        }
        SELECTED_STYLE.put(player.getUniqueId(), normalizeStyle(style));
    }

    public static String getSelectedStyle(Player player) {
        if (player == null) {
            return "sombre";
        }
        return SELECTED_STYLE.getOrDefault(player.getUniqueId(), "sombre");
    }

    public static String formatStyle(String style) {
        return switch (normalizeStyle(style)) {
            case "lumineux" -> "Lumineux";
            case "joyeux" -> "Joyeux";
            case "royal" -> "Royal";
            case "nature" -> "Nature";
            case "neige" -> "Neige";
            default -> "Sombre";
        };
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

        String style = getSelectedStyle(player);
        int radius = radius(rawSize);
        int height = height(radius);
        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        backup(center, radius, height);
        generate(center, radius, height, style);

        spawn = center.clone().add(0, 1, 0);
        spawn.setYaw(player.getLocation().getYaw());
        spawn.setPitch(player.getLocation().getPitch());
        active = true;

        config.set("active", true);
        config.set("radius", radius);
        config.set("height", height);
        config.set("style", style);
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
                MoodStyle.detail("Style : §e" + formatStyle(style)),
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

    private static void generate(Location center, int radius, int height, String style) {
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
                        block.setType(floorMaterial(style, cx, cz, x, z), false);
                    } else if (roof) {
                        block.setType(roofMaterial(style, cx, cz, x, z), false);
                    } else if (corner) {
                        block.setType(cornerMaterial(style), false);
                    } else if (border) {
                        block.setType(wallMaterial(style, y, cy, height), false);
                    } else if (inner) {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }

        decorate(world, cx, cy, cz, radius, height, style);
    }

    private static Material floorMaterial(String style, int cx, int cz, int x, int z) {
        int dx = Math.abs(x - cx);
        int dz = Math.abs(z - cz);
        if (dx <= 1 && dz <= 1) {
            return switch (normalizeStyle(style)) {
                case "lumineux" -> Material.SEA_LANTERN;
                case "joyeux" -> Material.YELLOW_CONCRETE;
                case "royal" -> Material.GOLD_BLOCK;
                case "nature" -> Material.MOSS_BLOCK;
                case "neige" -> Material.PACKED_ICE;
                default -> Material.GOLD_BLOCK;
            };
        }
        if (dx == dz || dx == 0 || dz == 0) {
            return switch (normalizeStyle(style)) {
                case "lumineux" -> Material.SMOOTH_QUARTZ;
                case "joyeux" -> Material.ORANGE_CONCRETE;
                case "royal" -> Material.PURPUR_BLOCK;
                case "nature" -> Material.OAK_PLANKS;
                case "neige" -> Material.BLUE_ICE;
                default -> Material.POLISHED_BLACKSTONE_BRICKS;
            };
        }
        return switch (normalizeStyle(style)) {
            case "lumineux" -> (x + z) % 2 == 0 ? Material.QUARTZ_BLOCK : Material.SMOOTH_STONE;
            case "joyeux" -> (x + z) % 2 == 0 ? Material.LIME_CONCRETE : Material.LIGHT_BLUE_CONCRETE;
            case "royal" -> (x + z) % 2 == 0 ? Material.POLISHED_DEEPSLATE : Material.AMETHYST_BLOCK;
            case "nature" -> (x + z) % 2 == 0 ? Material.MOSS_BLOCK : Material.GRASS_BLOCK;
            case "neige" -> (x + z) % 2 == 0 ? Material.SNOW_BLOCK : Material.QUARTZ_BLOCK;
            default -> (x + z) % 2 == 0 ? Material.POLISHED_DEEPSLATE : Material.DEEPSLATE_TILES;
        };
    }

    private static Material roofMaterial(String style, int cx, int cz, int x, int z) {
        if (x == cx || z == cz) {
            return switch (normalizeStyle(style)) {
                case "lumineux" -> Material.QUARTZ_SLAB;
                case "joyeux" -> Material.BIRCH_SLAB;
                case "royal" -> Material.PURPUR_SLAB;
                case "nature" -> Material.OAK_SLAB;
                case "neige" -> Material.SPRUCE_SLAB;
                default -> Material.DARK_OAK_SLAB;
            };
        }
        return switch (normalizeStyle(style)) {
            case "lumineux" -> Material.SMOOTH_STONE_SLAB;
            case "joyeux" -> Material.OAK_SLAB;
            case "royal" -> Material.SMOOTH_STONE_SLAB;
            case "nature" -> Material.MOSS_CARPET;
            case "neige" -> Material.SNOW_BLOCK;
            default -> Material.SMOOTH_STONE_SLAB;
        };
    }

    private static Material cornerMaterial(String style) {
        return switch (normalizeStyle(style)) {
            case "lumineux" -> Material.QUARTZ_PILLAR;
            case "joyeux" -> Material.STRIPPED_BIRCH_LOG;
            case "royal" -> Material.PURPUR_PILLAR;
            case "nature" -> Material.OAK_LOG;
            case "neige" -> Material.STRIPPED_SPRUCE_LOG;
            default -> Material.STRIPPED_DARK_OAK_LOG;
        };
    }

    private static Material wallMaterial(String style, int y, int cy, int height) {
        int relative = y - cy;
        if (relative == 1 || relative == height - 1) {
            return switch (normalizeStyle(style)) {
                case "lumineux" -> Material.QUARTZ_BLOCK;
                case "joyeux" -> Material.BIRCH_PLANKS;
                case "royal" -> Material.PURPUR_BLOCK;
                case "nature" -> Material.OAK_PLANKS;
                case "neige" -> Material.SPRUCE_PLANKS;
                default -> Material.DARK_OAK_PLANKS;
            };
        }
        if (relative == 2 || relative == 3) {
            return switch (normalizeStyle(style)) {
                case "lumineux" -> Material.WHITE_STAINED_GLASS;
                case "joyeux" -> Material.LIGHT_BLUE_STAINED_GLASS;
                case "royal" -> Material.PURPLE_STAINED_GLASS;
                case "nature" -> Material.GREEN_STAINED_GLASS;
                case "neige" -> Material.LIGHT_BLUE_STAINED_GLASS;
                default -> Material.TINTED_GLASS;
            };
        }
        return switch (normalizeStyle(style)) {
            case "lumineux" -> Material.SMOOTH_QUARTZ;
            case "joyeux" -> Material.YELLOW_TERRACOTTA;
            case "royal" -> Material.AMETHYST_BLOCK;
            case "nature" -> Material.OAK_LEAVES;
            case "neige" -> Material.SNOW_BLOCK;
            default -> Material.SPRUCE_PLANKS;
        };
    }

    private static void decorate(World world, int cx, int cy, int cz, int radius, int height, String style) {
        int[][] corners = {
                {cx - radius + 1, cz - radius + 1},
                {cx + radius - 1, cz - radius + 1},
                {cx - radius + 1, cz + radius - 1},
                {cx + radius - 1, cz + radius - 1}
        };

        for (int[] point : corners) {
            world.getBlockAt(point[0], cy + 1, point[1]).setType(lightMaterial(style), false);
            if (height >= 7) {
                world.getBlockAt(point[0], cy + height - 1, point[1]).setType(ceilingLightMaterial(style), false);
            }
        }

        world.getBlockAt(cx, cy + 1, cz).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);

        if (radius >= 5) {
            Material stair = stairMaterial(style);
            world.getBlockAt(cx + 2, cy + 1, cz).setType(stair, false);
            world.getBlockAt(cx - 2, cy + 1, cz).setType(stair, false);
            world.getBlockAt(cx, cy + 1, cz + 2).setType(stair, false);
            world.getBlockAt(cx, cy + 1, cz - 2).setType(stair, false);
        }

        if (radius >= 9) {
            world.getBlockAt(cx + radius - 2, cy + 1, cz).setType(Material.BARREL, false);
            world.getBlockAt(cx - radius + 2, cy + 1, cz).setType(Material.BARREL, false);
            world.getBlockAt(cx, cy + 1, cz + radius - 2).setType(Material.CRAFTING_TABLE, false);
            world.getBlockAt(cx, cy + 1, cz - radius + 2).setType(Material.CARTOGRAPHY_TABLE, false);
        }
    }

    private static Material lightMaterial(String style) {
        return switch (normalizeStyle(style)) {
            case "lumineux", "neige" -> Material.SEA_LANTERN;
            case "joyeux" -> Material.GLOWSTONE;
            case "royal" -> Material.AMETHYST_CLUSTER;
            case "nature" -> Material.LANTERN;
            default -> Material.LANTERN;
        };
    }

    private static Material ceilingLightMaterial(String style) {
        return switch (normalizeStyle(style)) {
            case "lumineux", "neige" -> Material.SEA_LANTERN;
            case "joyeux" -> Material.GLOWSTONE;
            case "royal" -> Material.SHROOMLIGHT;
            case "nature" -> Material.GLOWSTONE;
            default -> Material.SEA_LANTERN;
        };
    }

    private static Material stairMaterial(String style) {
        return switch (normalizeStyle(style)) {
            case "lumineux" -> Material.QUARTZ_STAIRS;
            case "joyeux" -> Material.BIRCH_STAIRS;
            case "royal" -> Material.PURPUR_STAIRS;
            case "nature" -> Material.OAK_STAIRS;
            case "neige" -> Material.SPRUCE_STAIRS;
            default -> Material.DARK_OAK_STAIRS;
        };
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

    private static String normalizeStyle(String style) {
        if (style == null) {
            return "sombre";
        }
        String clean = style.toLowerCase(Locale.ROOT)
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace(" ", "")
                .replace("_", "");
        return switch (clean) {
            case "clair", "light", "lumineux" -> "lumineux";
            case "joyeux", "color", "couleur", "colore", "coloré" -> "joyeux";
            case "royal", "prestige" -> "royal";
            case "nature", "foret", "forêt" -> "nature";
            case "neige", "glace", "snow" -> "neige";
            default -> "sombre";
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
