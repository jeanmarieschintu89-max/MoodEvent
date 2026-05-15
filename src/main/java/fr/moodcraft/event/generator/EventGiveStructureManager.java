package fr.moodcraft.event.generator;

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

public final class EventGiveStructureManager {

    private static File file;
    private static FileConfiguration config;

    private EventGiveStructureManager() {
    }

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "event-give.yml");
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
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static boolean hasStructure() {
        ensureLoaded();
        return config.getBoolean("active", false);
    }

    public static void generate(Player player) {
        ensureLoaded();
        if (player == null) return;
        if (hasStructure()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une structure Event Give existe déjà.", MoodStyle.detail("Restaure-la avant d'en créer une autre."));
            return;
        }

        Location center = player.getLocation().getBlock().getLocation();
        World world = center.getWorld();
        if (world == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Monde introuvable.");
            return;
        }

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        backup(world, cx, cy, cz, 7, 6);

        for (int x = cx - 6; x <= cx + 6; x++) {
            for (int z = cz - 6; z <= cz + 6; z++) {
                Material floor = (Math.abs(x - cx) <= 2 && Math.abs(z - cz) <= 2) ? Material.GOLD_BLOCK : ((x + z) % 2 == 0 ? Material.POLISHED_ANDESITE : Material.SMOOTH_STONE);
                world.getBlockAt(x, cy, z).setType(floor, false);
                for (int y = cy + 1; y <= cy + 5; y++) world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }

        for (int x = cx - 6; x <= cx + 6; x++) {
            world.getBlockAt(x, cy + 1, cz - 6).setType(Material.OAK_FENCE, false);
            world.getBlockAt(x, cy + 1, cz + 6).setType(Material.OAK_FENCE, false);
        }
        for (int z = cz - 6; z <= cz + 6; z++) {
            world.getBlockAt(cx - 6, cy + 1, z).setType(Material.OAK_FENCE, false);
            world.getBlockAt(cx + 6, cy + 1, z).setType(Material.OAK_FENCE, false);
        }

        pillar(world, cx - 6, cy + 1, cz - 6);
        pillar(world, cx + 6, cy + 1, cz - 6);
        pillar(world, cx - 6, cy + 1, cz + 6);
        pillar(world, cx + 6, cy + 1, cz + 6);

        world.getBlockAt(cx, cy + 1, cz).setType(Material.CHEST, false);
        world.getBlockAt(cx - 2, cy + 1, cz).setType(Material.BARREL, false);
        world.getBlockAt(cx + 2, cy + 1, cz).setType(Material.BARREL, false);
        world.getBlockAt(cx, cy + 1, cz - 2).setType(Material.CHEST, false);
        world.getBlockAt(cx, cy + 1, cz + 2).setType(Material.CHEST, false);
        world.getBlockAt(cx, cy + 3, cz).setType(Material.SEA_LANTERN, false);

        config.set("active", true);
        config.set("center.world", world.getName());
        config.set("center.x", cx);
        config.set("center.y", cy);
        config.set("center.z", cz);
        save();

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.15f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Structure Event Give générée.", MoodStyle.detail("Dépose les items dans les coffres/barils."), MoodStyle.detail("Les joueurs peuvent venir récupérer les lots."), MoodStyle.detail("Restauration possible depuis le menu."));
    }

    public static void restore(Player player) {
        ensureLoaded();
        ConfigurationSection blocks = config.getConfigurationSection("backup.blocks");
        if (!hasStructure() || blocks == null) {
            if (player != null) MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune structure Event Give à restaurer.");
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
        config.set("center", null);
        config.set("backup", null);
        save();
        if (player != null) MoodStyle.successMessage(player, MoodStyle.MODULE, "Structure Event Give restaurée.", MoodStyle.detail("Blocs restaurés : §e" + restored));
    }

    private static void pillar(World world, int x, int y, int z) {
        for (int dy = 0; dy <= 3; dy++) world.getBlockAt(x, y + dy, z).setType(Material.GOLD_BLOCK, false);
        world.getBlockAt(x, y + 4, z).setType(Material.SEA_LANTERN, false);
    }

    private static void backup(World world, int cx, int cy, int cz, int radius, int height) {
        config.set("backup", null);
        int index = 0;
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy; y <= cy + height; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
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

    private static void ensureLoaded() {
        if (config == null) load();
    }
}
