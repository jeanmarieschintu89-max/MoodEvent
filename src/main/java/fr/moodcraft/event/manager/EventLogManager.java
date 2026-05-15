package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class EventLogManager {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int MAX_ENTRIES = 80;

    private static File file;
    private static FileConfiguration config;

    private EventLogManager() {
    }

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "event-history.yml");
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

    public static void log(Player actor, String action, String detail) {
        ensureLoaded();
        String id = String.valueOf(System.currentTimeMillis());
        config.set("entries." + id + ".date", LocalDateTime.now().format(FORMATTER));
        config.set("entries." + id + ".actor", actor == null ? "Console" : actor.getName());
        config.set("entries." + id + ".actor-uuid", actor == null ? "" : actor.getUniqueId().toString());
        config.set("entries." + id + ".action", action == null ? "Action" : action);
        config.set("entries." + id + ".detail", detail == null ? "" : detail);
        prune();
        save();
    }

    public static void logSystem(String action, String detail) {
        log(null, action, detail);
    }

    public static List<Entry> latest(int limit) {
        ensureLoaded();
        ConfigurationSection section = config.getConfigurationSection("entries");
        if (section == null) return List.of();
        List<String> keys = new ArrayList<>(section.getKeys(false));
        Collections.sort(keys);
        Collections.reverse(keys);
        List<Entry> entries = new ArrayList<>();
        for (String key : keys) {
            if (entries.size() >= limit) break;
            entries.add(new Entry(
                    config.getString("entries." + key + ".date", "Date inconnue"),
                    config.getString("entries." + key + ".actor", "Inconnu"),
                    config.getString("entries." + key + ".action", "Action"),
                    config.getString("entries." + key + ".detail", "")
            ));
        }
        return entries;
    }

    public static String[] shortHistoryLore(int limit) {
        List<Entry> entries = latest(limit);
        if (entries.isEmpty()) return new String[]{MoodStyle.detail("Aucun événement enregistré.")};
        List<String> lore = new ArrayList<>();
        for (Entry entry : entries) {
            lore.add(MoodStyle.detail("§e" + entry.date() + " §8• §7" + entry.action()));
            lore.add(MoodStyle.detail("Par : §a" + entry.actor()));
        }
        return lore.toArray(new String[0]);
    }

    private static void prune() {
        ConfigurationSection section = config.getConfigurationSection("entries");
        if (section == null) return;
        List<String> keys = new ArrayList<>(section.getKeys(false));
        Collections.sort(keys);
        while (keys.size() > MAX_ENTRIES) {
            String oldest = keys.remove(0);
            config.set("entries." + oldest, null);
        }
    }

    private static void ensureLoaded() {
        if (config == null) load();
    }

    public record Entry(String date, String actor, String action, String detail) {
    }
}
