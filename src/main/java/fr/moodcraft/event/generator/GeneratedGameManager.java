package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.loot.EventLootManager;
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

public final class GeneratedGameManager {

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

    public static FileConfiguration config() {
        ensureLoaded();
        return config;
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

    public static int getGoldRushDurationSeconds() {
        ensureLoaded();
        return config.getInt("gold-rush.duration-seconds", 60);
    }

    public static int destroySurvivalBlocks(int amount) {
        ensureLoaded();
        if (!active || activeType != GeneratedGameType.SURVIE_ETAGES || survivalBlocks.isEmpty()) return 0;
        Collections.shuffle(survivalBlocks);
        int removed = 0;
        while (removed < amount && !survivalBlocks.isEmpty()) {
            Location location = survivalBlocks.remove(survivalBlocks.size() - 1);
            if (location == null || location.getWorld() == null) continue;
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
            case JUMP -> MoodStyle.detail("Hauteur entre §e30 §7et §e250 §7niveaux visuels.");
            case COURSE -> MoodStyle.detail("Longueur entre §e50 §7et §e1000 §7blocs.");
            case WATER_JUMP -> MoodStyle.detail("Longueur entre §e30 §7et §e250 §7blocs.");
            case SURVIE_ETAGES -> MoodStyle.detail("Largeur entre §e15 §7et §e61§7, étages automatiques.");
            case RUEE_OR -> MoodStyle.detail("Largeur mine entre §e15 §7et §e51§7. Temps calculé automatiquement.");
        };
    }

    public static String describeCustom(GeneratedGameType type, int value) {
        Spec spec = Spec.custom(type, value);
        return spec == null ? "Personnalisé" : spec.describe(type);
    }

    public static void restore(Player player) {
        ensureLoaded();
        if (!active) {
            if (player != null) MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune structure générée à restaurer.");
            return;
        }

        Region restoreRegion = readRegion("backup.region");
        if (restoreRegion == null) restoreRegion = activeRegion;
        if (restoreRegion != null) clearRegionToAir(restoreRegion);

        ConfigurationSection blocks = config.getConfigurationSection("backup.blocks");
        int restored = 0;
        if (blocks != null) {
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
        }

        config.set("active", false);
        config.set("type", null);
        config.set("size", null);
        config.set("region", null);
        config.set("backup", null);
        config.set("start", null);
        config.set("finish", null);
        config.set("survival", null);
        config.set("gold-rush", null);
        active = false;
        activeType = null;
        activeRegion = null;
        survivalBlocks.clear();
        EventLootManager.clearGeneratedLoot();
        save();
        if (player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.1f);
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Structure restaurée.", MoodStyle.detail("Blocs originaux restaurés : §e" + restored));
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
        purgeOldDebris(center, type, region);
        backup(region);
        clearRegionToAir(region);
        EventLootManager.clearGeneratedLoot();
        survivalBlocks.clear();

        Points points = switch (type) {
            case LABYRINTHE -> routeMaze(center, spec);
            case JUMP -> routeJump(center, spec);
            case COURSE -> routeRace(center, spec);
            case WATER_JUMP -> routeWaterJump(center, spec);
            case SURVIE_ETAGES -> routeCollapse(center, spec);
            case RUEE_OR -> routeMine(center, spec);
        };

        config.set("style", type == GeneratedGameType.LABYRINTHE ? "LABYRINTHE_PREMIUM" : type == GeneratedGameType.JUMP || type == GeneratedGameType.WATER_JUMP ? "LAINE_CONSTANTE" : "BUILDER_DEDIE");
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
            config.set("survival.elimination-y", points.eliminationY());
            writeSurvivalBlocks();
        }
        if (type == GeneratedGameType.RUEE_OR) config.set("gold-rush.duration-seconds", spec.goldDuration);
        save();

        configureEvent(player, type, points);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.15f);
        MoodStyle.successMessage(player, MoodStyle.MODULE,
                "Mini-jeu généré.",
                MoodStyle.detail("Type : §e" + type.getDisplayName()),
                MoodStyle.detail("Taille : §e" + spec.describe(type)));
    }

    private static void purgeOldDebris(Location center, GeneratedGameType type, Region region) {
        int radius = Math.max(region.maxX - region.minX, region.maxZ - region.minZ) / 2 + 8;
        int down = 6;
        int up = Math.max(20, region.maxY - region.minY + 10);
        GeneratedDebrisCleaner.purgeGeneratedDebris(center, radius, down, up);
    }

    private static Points routeMaze(Location center, Spec spec) {
        GeneratedMazeBuilder.Layout layout = GeneratedMazeBuilder.build(center, spec.width);
        return new Points(layout.start(), layout.finish(), 0);
    }

    private static Points routeJump(Location center, Spec spec) {
        GeneratedVerticalJumpBuilder.Layout layout = GeneratedVerticalJumpBuilder.build(center, spec.platforms);
        return new Points(layout.start(), layout.finish(), 0);
    }

    private static Points routeRace(Location center, Spec spec) {
        GeneratedRaceBuilder.Layout layout = GeneratedRaceBuilder.build(center, spec.length);
        return new Points(layout.start(), layout.finish(), 0);
    }

    private static Points routeWaterJump(Location center, Spec spec) {
        GeneratedWaterJumpBuilder.Layout layout = GeneratedWaterJumpBuilder.build(center, spec.length);
        return new Points(layout.start(), layout.finish(), 0);
    }

    private static Points routeCollapse(Location center, Spec spec) {
        GeneratedCollapseBuilder.Layout layout = GeneratedCollapseBuilder.build(center, spec.width, spec.floors);
        survivalBlocks.addAll(layout.breakableBlocks());
        return new Points(layout.start(), null, layout.eliminationY());
    }

    private static Points routeMine(Location center, Spec spec) {
        GeneratedMineMadnessBuilder.Layout layout = GeneratedMineMadnessBuilder.build(center, spec.width, spec.goldHeight);
        return new Points(layout.start(), null, 0);
    }

    private static void configureEvent(Player player, GeneratedGameType type, Points points) {
        Location back = player.getLocation().clone().add(0, 3, 0);
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
            case LABYRINTHE -> "Trouvez la sortie rouge avant les autres.";
            case JUMP -> "Grimpez le mur jusqu'à la dernière plateforme rouge.";
            case COURSE -> "Restez dans la piste et atteignez la ligne rouge avant les autres.";
            case WATER_JUMP -> "Franchissez les blocs de laine au-dessus de l'eau jusqu'à l'arrivée rouge.";
            case SURVIE_ETAGES -> "Survivez dans la tour pendant que les étages disparaissent.";
            case RUEE_OR -> "Minez un maximum de minerais dans le temps imparti. Vous gardez les minerais.";
        };
    }

    private static Region regionFor(Location center, GeneratedGameType type, Spec spec) {
        String world = center.getWorld().getName();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        return switch (type) {
            case LABYRINTHE -> new Region(world, cx - spec.width / 2 - 12, cy - 1, cz - spec.width / 2 - 5, cx + spec.width / 2 + 12, cy + 9, cz + spec.width / 2 + 5);
            case JUMP -> new Region(world, cx - 12, cy - 2, cz - 12, cx + 12, cy + Math.max(28, spec.platforms + 14), cz + 12);
            case COURSE -> new Region(world, cx - 8, cy - 1, cz - 6, cx + Math.max(50, Math.min(220, spec.length)) + 10, cy + 6, cz + 6);
            case WATER_JUMP -> new Region(world, cx - 8, cy - 2, cz - 8, cx + Math.max(30, Math.min(140, spec.length)) + 10, cy + 8, cz + 8);
            case SURVIE_ETAGES -> new Region(world, cx - spec.width / 2 - 4, cy - 2, cz - spec.width / 2 - 4, cx + spec.width / 2 + 4, cy + 10 + spec.floors * 5, cz + spec.width / 2 + 4);
            case RUEE_OR -> new Region(world, cx - spec.width / 2 - 2, cy - 1, cz - spec.width / 2 - 2, cx + spec.width / 2 + 2, cy + spec.goldHeight + 2, cz + spec.width / 2 + 2);
        };
    }

    private static void backup(Region region) {
        config.set("backup", null);
        config.set("backup.lightweight", true);
        writeRegion("backup.region", region);
        World world = Bukkit.getWorld(region.worldName);
        if (world == null) return;
        int index = 0;
        for (int x = region.minX; x <= region.maxX; x++) for (int y = region.minY; y <= region.maxY; y++) for (int z = region.minZ; z <= region.maxZ; z++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType().isAir()) continue;
            String path = "backup.blocks." + index++;
            config.set(path + ".world", world.getName());
            config.set(path + ".x", x);
            config.set(path + ".y", y);
            config.set(path + ".z", z);
            config.set(path + ".data", block.getBlockData().getAsString());
        }
        config.set("backup.saved-blocks", index);
    }

    private static void clearRegionToAir(Region region) {
        if (region == null) return;
        World world = Bukkit.getWorld(region.worldName);
        if (world == null) return;
        for (int x = region.minX; x <= region.maxX; x++) for (int y = region.minY; y <= region.maxY; y++) for (int z = region.minZ; z <= region.maxZ; z++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
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
        try { return GeneratedGameType.valueOf(name.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { return null; }
    }

    private static void writeLocation(String path, Location location) {
        if (location == null || location.getWorld() == null) { config.set(path, null); return; }
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
        if (region == null) { config.set(path, null); return; }
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

    private static void ensureLoaded() { if (config == null) load(); }

    private record Points(Location start, Location finish, int eliminationY) {}

    private record Region(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        private boolean contains(Location location) {
            if (location == null || location.getWorld() == null || !location.getWorld().getName().equals(worldName)) return false;
            int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }
    }

    private record Spec(int width, int length, int platforms, int floors, int goldHeight, int goldDuration) {
        private static Spec preset(GeneratedGameType type, GeneratedGameSize size) {
            return switch (type) {
                case LABYRINTHE -> new Spec(size.getMazeWidth(), 0, 0, 0, 0, 0);
                case JUMP -> new Spec(0, 0, size.getJumpPlatforms(), 0, 0, 0);
                case COURSE -> new Spec(0, size.getRaceLength(), 0, 0, 0, 0);
                case WATER_JUMP -> new Spec(0, size.getWaterLength(), 0, 0, 0, 0);
                case SURVIE_ETAGES -> new Spec(size.getSurvivalWidth(), 0, 0, size.getSurvivalFloors(), 0, 0);
                case RUEE_OR -> new Spec(size.getGoldRushWidth(), 0, 0, 0, size.getGoldRushHeight(), size.getGoldRushDurationSeconds());
            };
        }

        private static Spec custom(GeneratedGameType type, int value) {
            return switch (type) {
                case LABYRINTHE -> value >= 15 && value <= 101 && value % 2 == 1 ? new Spec(value, 0, 0, 0, 0, 0) : null;
                case JUMP -> value >= 30 && value <= 250 ? new Spec(0, 0, Math.max(8, value / 5), 0, 0, 0) : null;
                case COURSE -> value >= 50 && value <= 1000 ? new Spec(0, value, 0, 0, 0, 0) : null;
                case WATER_JUMP -> value >= 30 && value <= 250 ? new Spec(0, value, 0, 0, 0, 0) : null;
                case SURVIE_ETAGES -> value >= 15 && value <= 61 ? new Spec(value % 2 == 1 ? value : value + 1, 0, 0, floors(value), 0, 0) : null;
                case RUEE_OR -> value >= 15 && value <= 51 ? new Spec(value % 2 == 1 ? value : value + 1, 0, 0, 0, Math.max(9, value / 3), Math.max(60, Math.min(240, value * 4))) : null;
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
                case JUMP -> platforms + " plateformes en hauteur";
                case COURSE, WATER_JUMP -> length + " blocs";
                case SURVIE_ETAGES -> width + "x" + width + " §8• §7" + floors + " étages";
                case RUEE_OR -> width + "x" + goldHeight + " §8• §7" + goldDuration + "s";
            };
        }
    }
}
