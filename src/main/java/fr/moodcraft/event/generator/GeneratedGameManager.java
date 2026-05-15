package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.loot.LootTier;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class GeneratedGameManager {

    private static final Random RANDOM = new Random();
    private static final Material[] WOOL = {
            Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.LIME_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.CYAN_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL
    };

    private static File file;
    private static FileConfiguration config;
    private static boolean active;
    private static GeneratedGameType activeType;
    private static Region activeRegion;
    private static final List<Location> survivalBlocks = new ArrayList<>();

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
        activeType = readType(config.getString("type", ""));
        activeRegion = readRegion("region");
        loadSurvivalBlocks();
    }

    public static void save() {
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static boolean hasStructure() {
        ensureLoaded();
        return active && activeRegion != null;
    }

    public static GeneratedGameType getActiveType() {
        ensureLoaded();
        return activeType;
    }

    public static boolean isInsideStructure(Location location) {
        ensureLoaded();
        return hasStructure() && activeRegion.contains(location);
    }

    public static boolean isSurvivalFall(Location location) {
        ensureLoaded();
        if (location == null || location.getWorld() == null || activeType != GeneratedGameType.SURVIE_ETAGES) return false;
        int y = config.getInt("survival.elimination-y", Integer.MIN_VALUE);
        return y != Integer.MIN_VALUE && location.getBlockY() <= y;
    }

    public static int destroySurvivalBlocks(int amount) {
        ensureLoaded();
        if (!active || activeType != GeneratedGameType.SURVIE_ETAGES || survivalBlocks.isEmpty()) return 0;
        Collections.shuffle(survivalBlocks, RANDOM);
        int removed = 0;
        while (removed < amount && !survivalBlocks.isEmpty()) {
            Location location = survivalBlocks.remove(survivalBlocks.size() - 1);
            if (location.getWorld() == null) continue;
            Block block = location.getBlock();
            if (!block.getType().isAir()) {
                block.setType(Material.AIR, false);
                removed++;
            }
        }
        writeSurvivalBlocks();
        save();
        return removed;
    }

    public static void generate(Player player, GeneratedGameType type, GeneratedGameSize size) {
        ensureLoaded();
        if (player == null || type == null || size == null) return;
        generateInternal(player, type, size.getDisplayName(), Spec.preset(type, size));
    }

    public static void generateCustom(Player player, GeneratedGameType type, int value) {
        ensureLoaded();
        if (player == null || type == null) return;
        Spec spec = Spec.custom(type, value);
        if (spec == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Taille personnalisée invalide.", customRule(type));
            return;
        }
        generateInternal(player, type, "Personnalisé", spec);
    }

    public static String customRule(GeneratedGameType type) {
        return switch (type) {
            case LABYRINTHE -> MoodStyle.detail("Largeur impaire entre §e15 §7et §e101§7.");
            case JUMP -> MoodStyle.detail("Longueur entre §e30 §7et §e250 §7blocs.");
            case COURSE -> MoodStyle.detail("Longueur entre §e50 §7et §e1000 §7blocs.");
            case WATER_JUMP -> MoodStyle.detail("Longueur entre §e30 §7et §e250 §7blocs.");
            case SURVIE_ETAGES -> MoodStyle.detail("Largeur entre §e15 §7et §e61§7, étages automatiques.");
        };
    }

    public static String describeCustom(GeneratedGameType type, int value) {
        Spec spec = Spec.custom(type, value);
        return spec == null ? "Personnalisé" : spec.describe(type);
    }

    public static void restore(Player player) {
        ensureLoaded();
        ConfigurationSection blocks = config.getConfigurationSection("backup.blocks");
        if (!active || blocks == null) {
            if (player != null) MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune structure générée à restaurer.");
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
        config.set("type", null);
        config.set("size", null);
        config.set("region", null);
        config.set("backup", null);
        config.set("start", null);
        config.set("finish", null);
        config.set("survival", null);
        active = false;
        activeType = null;
        activeRegion = null;
        survivalBlocks.clear();
        EventLootManager.clearGeneratedLoot();
        save();

        if (player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Structure restaurée.", MoodStyle.detail("Blocs restaurés : §e" + restored));
        }
    }

    private static void generateInternal(Player player, GeneratedGameType type, String sizeName, Spec spec) {
        if (active) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une structure générée existe déjà.", MoodStyle.detail("Restaure-la avant d'en créer une autre."));
            return;
        }
        Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }
        Region region = regionFor(center, type, spec);
        backup(region);
        clearWorkArea(world, region);
        EventLootManager.clearGeneratedLoot();
        survivalBlocks.clear();

        Points points = switch (type) {
            case LABYRINTHE -> generateMaze(center, spec);
            case JUMP -> generateJump(center, spec);
            case COURSE -> generateRace(center, spec);
            case WATER_JUMP -> generateWaterJump(center, spec);
            case SURVIE_ETAGES -> generateSurvival(center, spec);
        };

        active = true;
        activeType = type;
        activeRegion = region;
        config.set("active", true);
        config.set("type", type.name());
        config.set("size", sizeName);
        writeRegion("region", region);
        writeLocation("start", points.start());
        writeLocation("finish", points.finish());
        if (type == GeneratedGameType.SURVIE_ETAGES) {
            config.set("survival.elimination-y", center.getBlockY());
            writeSurvivalBlocks();
        }
        save();

        configureEvent(player, type, points);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.15f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Mini-jeu généré.", MoodStyle.detail("Type : §e" + type.getDisplayName()), MoodStyle.detail("Taille : §e" + spec.describe(type)), MoodStyle.detail("Départ automatique défini."), type.getEventType().usesFinishLine() ? MoodStyle.detail("Arrivée automatique définie.") : MoodStyle.detail("Classement par survie activé."));
    }

    private static void configureEvent(Player player, GeneratedGameType type, Points points) {
        Location back = player.getLocation().clone();
        EventManager.createEvent(player, type.getDisplayName());
        EventManager.setDescription(player, description(type));
        EventManager.setType(player, type.getEventType().name());
        player.teleport(points.start());
        EventManager.setLocation(player);
        if (points.finish() != null && type.getEventType().usesFinishLine()) {
            player.teleport(points.finish());
            EventManager.setFinishLocation(player);
        }
        player.teleport(back);
    }

    private static String description(GeneratedGameType type) {
        return switch (type) {
            case LABYRINTHE -> "Trouvez la sortie avant les autres.";
            case JUMP -> "Sautez de laine en laine jusqu'à l'arrivée.";
            case COURSE -> "Atteignez la ligne rouge avant les autres.";
            case WATER_JUMP -> "Franchissez les blocs de laine au-dessus de l'eau.";
            case SURVIE_ETAGES -> "Restez le plus longtemps possible pendant que les étages disparaissent.";
        };
    }

    private static Points generateMaze(Location center, Spec spec) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        int half = spec.width / 2;
        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                boolean border = x == cx - half || x == cx + half || z == cz - half || z == cz + half;
                boolean wall = border || (x % 4 == 0 && z % 3 != 0) || (z % 5 == 0 && x % 3 != 0);
                world.getBlockAt(x, cy, z).setType(Material.STONE_BRICKS, false);
                world.getBlockAt(x, cy + 1, z).setType(wall ? Material.MOSSY_STONE_BRICKS : Material.AIR, false);
                world.getBlockAt(x, cy + 2, z).setType(wall ? Material.STONE_BRICKS : Material.AIR, false);
                world.getBlockAt(x, cy + 3, z).setType(Material.AIR, false);
            }
        }
        Location start = new Location(world, cx - half + 1.5, cy + 1, cz - half + 1.5);
        Location finish = new Location(world, cx + half - 1.5, cy + 1, cz + half - 1.5);
        platform(world, start.getBlockX(), cy, start.getBlockZ(), 1, Material.LIME_CONCRETE);
        platform(world, finish.getBlockX(), cy, finish.getBlockZ(), 1, Material.RED_CONCRETE);
        addLoot(world, GeneratedGameType.LABYRINTHE, cx, cy + 1, cz, Math.max(8, spec.width - 4));
        return new Points(start, finish);
    }

    private static Points generateJump(Location center, Spec spec) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        platform(world, cx, cy, cz, 2, Material.LIME_WOOL);
        int x = cx, z = cz, y = cy;
        for (int i = 1; i <= spec.platforms; i++) {
            x += 4 + RANDOM.nextInt(2);
            z += RANDOM.nextInt(5) - 2;
            y = cy + 1 + RANDOM.nextInt(3);
            platform(world, x, y, z, i % 5 == 0 ? 2 : 1, WOOL[i % WOOL.length]);
            if (i % 9 == 0) placeLoot(world, GeneratedGameType.JUMP, LootTier.COMMUN, x, y + 1, z + 2);
        }
        platform(world, x + 5, cy + 1, z, 2, Material.RED_WOOL);
        return new Points(new Location(world, cx + 0.5, cy + 1, cz + 0.5), new Location(world, x + 5.5, cy + 2, z + 0.5));
    }

    private static Points generateRace(Location center, Spec spec) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        for (int x = cx; x <= cx + spec.length; x++) {
            for (int z = cz - 2; z <= cz + 2; z++) {
                world.getBlockAt(x, cy, z).setType((x + z) % 2 == 0 ? Material.SMOOTH_STONE : Material.POLISHED_ANDESITE, false);
                world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
            if (x % 18 == 0) {
                world.getBlockAt(x, cy + 1, cz - 1).setType(Material.HAY_BLOCK, false);
                world.getBlockAt(x, cy + 1, cz + 1).setType(Material.HAY_BLOCK, false);
            }
        }
        platform(world, cx, cy, cz, 3, Material.LIME_CONCRETE);
        platform(world, cx + spec.length, cy, cz, 3, Material.RED_CONCRETE);
        return new Points(new Location(world, cx + 0.5, cy + 1, cz + 0.5), new Location(world, cx + spec.length + 0.5, cy + 1, cz + 0.5));
    }

    private static Points generateWaterJump(Location center, Spec spec) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        for (int x = cx; x <= cx + spec.length; x++) {
            for (int z = cz - 4; z <= cz + 4; z++) {
                world.getBlockAt(x, cy - 1, z).setType(Material.PRISMARINE_BRICKS, false);
                world.getBlockAt(x, cy, z).setType(Material.WATER, false);
                world.getBlockAt(x, cy + 1, z).setType(Material.AIR, false);
            }
        }
        platform(world, cx, cy + 1, cz, 3, Material.LIME_WOOL);
        for (int x = cx + 5, i = 0; x < cx + spec.length; x += 5, i++) {
            int z = cz + RANDOM.nextInt(7) - 3;
            platform(world, x, cy + 1, z, 1, WOOL[i % WOOL.length]);
            if (i % 7 == 0) placeLoot(world, GeneratedGameType.WATER_JUMP, LootTier.COMMUN, x, cy + 2, z + 2);
        }
        platform(world, cx + spec.length, cy + 1, cz, 3, Material.RED_WOOL);
        return new Points(new Location(world, cx + 0.5, cy + 2, cz + 0.5), new Location(world, cx + spec.length + 0.5, cy + 2, cz + 0.5));
    }

    private static Points generateSurvival(Location center, Spec spec) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        int half = spec.width / 2;
        for (int floor = 0; floor < spec.floors; floor++) {
            int y = cy + 4 + (floor * 5);
            Material material = WOOL[floor % WOOL.length];
            for (int x = cx - half; x <= cx + half; x++) {
                for (int z = cz - half; z <= cz + half; z++) {
                    if (Math.abs(x - cx) <= 1 && Math.abs(z - cz) <= 1) continue;
                    if ((x + z + floor) % 7 == 0) continue;
                    world.getBlockAt(x, y, z).setType(material, false);
                    survivalBlocks.add(new Location(world, x, y, z));
                }
            }
            platform(world, cx, y, cz, 1, Material.SEA_LANTERN);
        }
        int topY = cy + 4 + ((spec.floors - 1) * 5);
        return new Points(new Location(world, cx + 0.5, topY + 1, cz + 0.5), null);
    }

    private static void addLoot(World world, GeneratedGameType type, int cx, int cy, int cz, int spread) {
        placeLoot(world, type, LootTier.COMMUN, cx, cy, cz + Math.max(3, spread / 3));
        placeLoot(world, type, LootTier.RARE, cx + Math.max(3, spread / 3), cy, cz);
        placeLoot(world, type, LootTier.EPIQUE, cx, cy, cz - Math.max(3, spread / 3));
    }

    private static void placeLoot(World world, GeneratedGameType type, LootTier tier, int x, int y, int z) {
        Location location = new Location(world, x, y, z);
        location.getBlock().setType(Material.CHEST, false);
        EventLootManager.registerLootChest(location, type, tier);
    }

    private static Region regionFor(Location center, GeneratedGameType type, Spec spec) {
        String world = center.getWorld().getName();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        return switch (type) {
            case LABYRINTHE -> new Region(world, cx - spec.width / 2 - 3, cy - 1, cz - spec.width / 2 - 3, cx + spec.width / 2 + 3, cy + 6, cz + spec.width / 2 + 3);
            case JUMP -> new Region(world, cx - 6, cy - 1, cz - 14, cx + spec.length + 15, cy + 10, cz + 14);
            case COURSE -> new Region(world, cx - 6, cy - 1, cz - 8, cx + spec.length + 8, cy + 5, cz + 8);
            case WATER_JUMP -> new Region(world, cx - 6, cy - 2, cz - 9, cx + spec.length + 8, cy + 6, cz + 9);
            case SURVIE_ETAGES -> new Region(world, cx - spec.width / 2 - 3, cy - 2, cz - spec.width / 2 - 3, cx + spec.width / 2 + 3, cy + 8 + spec.floors * 5, cz + spec.width / 2 + 3);
        };
    }

    private static void backup(Region region) {
        config.set("backup", null);
        World world = Bukkit.getWorld(region.worldName);
        if (world == null) return;
        int index = 0;
        for (int x = region.minX; x <= region.maxX; x++) for (int y = region.minY; y <= region.maxY; y++) for (int z = region.minZ; z <= region.maxZ; z++) {
            Block block = world.getBlockAt(x, y, z);
            String path = "backup.blocks." + index++;
            config.set(path + ".world", world.getName());
            config.set(path + ".x", x);
            config.set(path + ".y", y);
            config.set(path + ".z", z);
            config.set(path + ".data", block.getBlockData().getAsString());
        }
    }

    private static void clearWorkArea(World world, Region region) {
        for (int x = region.minX; x <= region.maxX; x++) for (int y = region.minY + 1; y <= region.maxY; y++) for (int z = region.minZ; z <= region.maxZ; z++) {
            world.getBlockAt(x, y, z).setType(Material.AIR, false);
        }
    }

    private static void platform(World world, int cx, int cy, int cz, int radius, Material material) {
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) world.getBlockAt(x, cy, z).setType(material, false);
    }

    private static void loadSurvivalBlocks() {
        survivalBlocks.clear();
        ConfigurationSection section = config.getConfigurationSection("survival.blocks");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Location location = readLocation("survival.blocks." + key);
            if (location != null) survivalBlocks.add(location);
        }
    }

    private static void writeSurvivalBlocks() {
        config.set("survival.blocks", null);
        for (int i = 0; i < survivalBlocks.size(); i++) writeLocation("survival.blocks." + i, survivalBlocks.get(i));
    }

    private static GeneratedGameType readType(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return GeneratedGameType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void writeLocation(String path, Location location) {
        if (location == null || location.getWorld() == null) {
            config.set(path, null);
            return;
        }
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

    private static void writeRegion(String path, Region region) {
        config.set(path + ".world", region.worldName);
        config.set(path + ".min-x", region.minX);
        config.set(path + ".min-y", region.minY);
        config.set(path + ".min-z", region.minZ);
        config.set(path + ".max-x", region.maxX);
        config.set(path + ".max-y", region.maxY);
        config.set(path + ".max-z", region.maxZ);
    }

    private static Region readRegion(String path) {
        String world = config.getString(path + ".world", "");
        if (world.isBlank()) return null;
        return new Region(world, config.getInt(path + ".min-x"), config.getInt(path + ".min-y"), config.getInt(path + ".min-z"), config.getInt(path + ".max-x"), config.getInt(path + ".max-y"), config.getInt(path + ".max-z"));
    }

    private static void ensureLoaded() {
        if (config == null) load();
    }

    private record Points(Location start, Location finish) {
    }

    private record Region(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        private boolean contains(Location location) {
            if (location == null || location.getWorld() == null || !location.getWorld().getName().equals(worldName)) return false;
            int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }
    }

    private record Spec(int width, int length, int platforms, int floors) {
        private static Spec preset(GeneratedGameType type, GeneratedGameSize size) {
            return switch (type) {
                case LABYRINTHE -> new Spec(size.getMazeWidth(), 0, 0, 0);
                case JUMP -> new Spec(0, size.getJumpPlatforms() * 5, size.getJumpPlatforms(), 0);
                case COURSE -> new Spec(0, size.getRaceLength(), 0, 0);
                case WATER_JUMP -> new Spec(0, size.getWaterLength(), 0, 0);
                case SURVIE_ETAGES -> new Spec(size.getSurvivalWidth(), 0, 0, size.getSurvivalFloors());
            };
        }

        private static Spec custom(GeneratedGameType type, int value) {
            return switch (type) {
                case LABYRINTHE -> value >= 15 && value <= 101 && value % 2 == 1 ? new Spec(value, 0, 0, 0) : null;
                case JUMP -> value >= 30 && value <= 250 ? new Spec(0, value, Math.max(8, value / 5), 0) : null;
                case COURSE -> value >= 50 && value <= 1000 ? new Spec(0, value, 0, 0) : null;
                case WATER_JUMP -> value >= 30 && value <= 250 ? new Spec(0, value, 0, 0) : null;
                case SURVIE_ETAGES -> value >= 15 && value <= 61 ? new Spec(value % 2 == 1 ? value : value + 1, 0, 0, floors(value)) : null;
            };
        }

        private static int floors(int width) {
            if (width >= 55) return 8;
            if (width >= 45) return 7;
            if (width >= 35) return 6;
            if (width >= 25) return 5;
            return 4;
        }

        private String describe(GeneratedGameType type) {
            return switch (type) {
                case LABYRINTHE -> width + "x" + width;
                case JUMP -> platforms + " plateformes";
                case COURSE, WATER_JUMP -> length + " blocs";
                case SURVIE_ETAGES -> width + "x" + width + " §8• §7" + floors + " étages";
            };
        }
    }
}
