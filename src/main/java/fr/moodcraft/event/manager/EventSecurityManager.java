package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class EventSecurityManager {

    private static final Set<String> DEFAULT_BLOCKED = new LinkedHashSet<>(List.of(
            "/spawn", "/home", "/homes", "/tpa", "/tpahere", "/warp", "/warps", "/rtp", "/tpr", "/back", "/ec", "/enderchest", "/fly", "/gamemode"
    ));

    private static File file;
    private static FileConfiguration config;

    private EventSecurityManager() {
    }

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "security.yml");
        if (!file.exists()) {
            try {
                Main.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                Main.getInstance().getLogger().warning(exception.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (!config.isSet("blocked-commands")) {
            config.set("blocked-commands", new ArrayList<>(DEFAULT_BLOCKED));
            config.set("block-build-in-generated-structure", true);
            config.set("block-ender-pearl", true);
            config.set("block-non-pvp-damage", true);
            save();
        }
    }

    public static void save() {
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static boolean shouldBlockPearl() {
        ensureLoaded();
        return config.getBoolean("block-ender-pearl", true);
    }

    public static boolean shouldBlockNonPvpDamage() {
        ensureLoaded();
        return config.getBoolean("block-non-pvp-damage", true);
    }

    public static boolean shouldBlockGeneratedBuild() {
        ensureLoaded();
        return config.getBoolean("block-build-in-generated-structure", true);
    }

    public static boolean isCommandBlocked(String rawMessage) {
        ensureLoaded();
        if (rawMessage == null || rawMessage.isBlank()) return false;
        String message = rawMessage.toLowerCase(Locale.ROOT).trim();
        for (String blocked : getBlockedCommands()) {
            String clean = normalizeCommand(blocked);
            if (message.equals(clean) || message.startsWith(clean + " ")) return true;
        }
        return false;
    }

    public static String matchedCommand(String rawMessage) {
        ensureLoaded();
        if (rawMessage == null || rawMessage.isBlank()) return "commande";
        String message = rawMessage.toLowerCase(Locale.ROOT).trim();
        for (String blocked : getBlockedCommands()) {
            String clean = normalizeCommand(blocked);
            if (message.equals(clean) || message.startsWith(clean + " ")) return clean;
        }
        return "commande";
    }

    public static List<String> getBlockedCommands() {
        ensureLoaded();
        List<String> commands = config.getStringList("blocked-commands");
        if (commands.isEmpty()) commands = new ArrayList<>(DEFAULT_BLOCKED);
        List<String> normalized = new ArrayList<>();
        for (String command : commands) normalized.add(normalizeCommand(command));
        return normalized;
    }

    public static void toggleCommand(Player player, String rawCommand) {
        ensureLoaded();
        String command = normalizeCommand(rawCommand);
        List<String> commands = new ArrayList<>(getBlockedCommands());
        if (commands.contains(command)) {
            commands.remove(command);
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Commande autorisée pendant les events.", MoodStyle.detail("Commande : §e" + command));
            EventLogManager.log(player, "Sécurité", "Commande autorisée : " + command);
        } else {
            commands.add(command);
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Commande bloquée pendant les events.", MoodStyle.detail("Commande : §e" + command));
            EventLogManager.log(player, "Sécurité", "Commande bloquée : " + command);
        }
        config.set("blocked-commands", commands);
        save();
    }

    public static void toggleBoolean(Player player, String path, String label) {
        ensureLoaded();
        boolean value = !config.getBoolean(path, true);
        config.set(path, value);
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Sécurité mise à jour.", MoodStyle.detail(label + " : " + (value ? "§aactivé" : "§cdésactivé")));
        EventLogManager.log(player, "Sécurité", label + " : " + (value ? "activé" : "désactivé"));
    }

    public static String state(String path) {
        ensureLoaded();
        return config.getBoolean(path, true) ? "§aactivé" : "§cdésactivé";
    }

    private static String normalizeCommand(String command) {
        if (command == null || command.isBlank()) return "/commande";
        String clean = command.toLowerCase(Locale.ROOT).trim();
        return clean.startsWith("/") ? clean : "/" + clean;
    }

    private static void ensureLoaded() {
        if (config == null) load();
    }
}
