package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class EventManager {

    private EventManager() {
    }

    private static String name = "";
    private static String description = "";
    private static EventType type = EventType.AUTRE;
    private static Location location;
    private static boolean queueOpen = false;
    private static boolean running = false;

    private static final Set<UUID> queue = new LinkedHashSet<>();

    public static void load() {

        FileConfiguration config = Main.getInstance().getConfig();

        name = config.getString("event.name", "");
        description = config.getString("event.description", "");
        type = EventType.fromText(config.getString("event.type", "AUTRE"));
        queueOpen = config.getBoolean("event.queue-open", false);
        running = false;

        String worldName = config.getString("event.location.world", "");
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            location = new Location(
                    world,
                    config.getDouble("event.location.x"),
                    config.getDouble("event.location.y"),
                    config.getDouble("event.location.z"),
                    (float) config.getDouble("event.location.yaw"),
                    (float) config.getDouble("event.location.pitch")
            );
        }
    }

    public static void save() {

        FileConfiguration config = Main.getInstance().getConfig();

        config.set("event.name", name);
        config.set("event.description", description);
        config.set("event.type", type.name());
        config.set("event.queue-open", queueOpen);

        if (location != null && location.getWorld() != null) {
            config.set("event.location.world", location.getWorld().getName());
            config.set("event.location.x", location.getX());
            config.set("event.location.y", location.getY());
            config.set("event.location.z", location.getZ());
            config.set("event.location.yaw", location.getYaw());
            config.set("event.location.pitch", location.getPitch());
        }

        Main.getInstance().saveConfig();
    }

    public static void createEvent(Player player, String rawName) {

        String cleanName = rawName == null ? "" : rawName.trim();

        if (cleanName.length() < 3) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Nom d'événement trop court.", MoodStyle.detail("Exemple : §eTournoi pêche"));
            return;
        }

        name = cleanName;
        description = "Aucune description définie.";
        type = EventType.AUTRE;
        queueOpen = false;
        running = false;
        queue.clear();
        save();

        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Événement créé.",
                MoodStyle.detail("Nom : §e" + name),
                MoodStyle.detail("Description : §e/eventdesc <texte>"),
                MoodStyle.detail("Point de téléportation : §e/eventset")
        );
    }

    public static void setDescription(Player player, String rawDescription) {

        if (!ensureEvent(player)) {
            return;
        }

        String clean = rawDescription == null ? "" : rawDescription.trim();

        if (clean.length() < 5) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Description trop courte.", MoodStyle.detail("Ajoute quelques détails pour les joueurs."));
            return;
        }

        description = clean;
        save();

        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Description mise à jour.",
                MoodStyle.detail(description)
        );
    }

    public static void setType(Player player, String rawType) {

        if (!ensureEvent(player)) {
            return;
        }

        type = EventType.fromText(rawType);
        save();

        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Type d'événement défini.",
                MoodStyle.detail("Type : " + type.getDisplayName())
        );
    }

    public static void setLocation(Player player) {

        if (!ensureEvent(player)) {
            return;
        }

        location = player.getLocation().clone();
        save();

        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Point de téléportation défini.",
                MoodStyle.detail("Monde : §e" + location.getWorld().getName()),
                MoodStyle.detail("Position enregistrée sur votre emplacement")
        );
    }

    public static void openQueue(Player player) {

        if (!ensureEvent(player) || !ensureLocation(player)) {
            return;
        }

        queueOpen = true;
        running = false;
        save();

        broadcastEvent(
                MoodStyle.success("File d'attente ouverte."),
                MoodStyle.detail("Événement : §e" + name),
                MoodStyle.detail("Type : " + type.getDisplayName()),
                MoodStyle.detail("Description : §f" + description),
                MoodStyle.info("Faites §e/event §fpour rejoindre la file")
        );
    }

    public static void closeQueue(Player player) {

        if (!ensureEvent(player)) {
            return;
        }

        queueOpen = false;
        save();

        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "File d'attente fermée.",
                MoodStyle.detail("Joueurs en attente : §e" + queue.size())
        );
    }

    public static void cancelEvent(Player player) {

        if (!ensureEvent(player)) {
            return;
        }

        broadcastEvent(
                MoodStyle.error("Événement annulé."),
                MoodStyle.detail("Événement : §e" + name),
                MoodStyle.detail("Les joueurs ont été retirés de la file")
        );

        clearEvent();
        save();
    }

    public static void stopEvent(Player player) {

        if (!ensureEvent(player)) {
            return;
        }

        broadcastEvent(
                MoodStyle.success("Événement terminé."),
                MoodStyle.detail("Événement : §e" + name),
                MoodStyle.detail("Merci à tous les participants")
        );

        clearEvent();
        save();
    }

    public static void joinQueue(Player player) {

        if (!hasEvent()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement n'est disponible.", MoodStyle.detail("Attends une annonce du staff."));
            return;
        }

        if (!queueOpen) {
            showEvent(player);
            return;
        }

        if (running) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "L'événement a déjà commencé.");
            return;
        }

        if (queue.add(player.getUniqueId())) {

            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Vous avez rejoint la file d'attente.",
                    MoodStyle.detail("Événement : §e" + name),
                    MoodStyle.detail("Type : " + type.getDisplayName()),
                    MoodStyle.detail("Position dans la file : §e" + queue.size()),
                    MoodStyle.detail("Vous serez téléporté au lancement")
            );

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);

        } else {

            MoodStyle.infoMessage(
                    player,
                    MoodStyle.MODULE,
                    "Vous êtes déjà dans la file d'attente.",
                    MoodStyle.detail("Événement : §e" + name),
                    MoodStyle.detail("Joueurs en attente : §e" + queue.size())
            );
        }
    }

    public static void showEvent(Player player) {

        if (!hasEvent()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement n'est disponible.");
            return;
        }

        MoodStyle.send(
                player,
                MoodStyle.MODULE,
                MoodStyle.info("Événement en préparation."),
                MoodStyle.detail("Nom : §e" + name),
                MoodStyle.detail("Type : " + type.getDisplayName()),
                MoodStyle.detail("Description : §f" + description),
                MoodStyle.detail("File ouverte : " + (queueOpen ? "§aoui" : "§cnon")),
                MoodStyle.detail("Joueurs en attente : §e" + queue.size()),
                queueOpen ? MoodStyle.info("Faites §e/event §fpour rejoindre") : MoodStyle.detail("Attendez l'ouverture par le staff")
        );
    }

    public static void startEvent(Player player) {

        if (!ensureEvent(player) || !ensureLocation(player)) {
            return;
        }

        if (queue.isEmpty()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun joueur dans la file d'attente.");
            return;
        }

        queueOpen = false;
        running = true;
        save();

        broadcastEvent(
                MoodStyle.success("L'événement va commencer."),
                MoodStyle.detail("Événement : §e" + name),
                MoodStyle.detail("Participants : §e" + queue.size()),
                MoodStyle.detail("Téléportation dans quelques secondes")
        );

        countdown(3);
    }

    private static void countdown(int number) {

        if (number <= 0) {
            teleportQueue();
            return;
        }

        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            player.sendTitle("§6" + number, "§fPréparez-vous", 0, 20, 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> countdown(number - 1), 20L);
    }

    private static void teleportQueue() {

        int teleported = 0;

        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            player.teleport(location);
            player.sendTitle("§aGOOO!", "§f" + name, 0, 40, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.1f);
            teleported++;
        }

        broadcastEvent(
                MoodStyle.success("Événement lancé."),
                MoodStyle.detail("Événement : §e" + name),
                MoodStyle.detail("Participants téléportés : §e" + teleported)
        );

        queue.clear();
    }

    public static void adminHelp(Player player) {
        MoodStyle.send(
                player,
                MoodStyle.MODULE,
                MoodStyle.info("Commandes événement."),
                MoodStyle.detail("/eventcreate <nom>"),
                MoodStyle.detail("/eventdesc <description>"),
                MoodStyle.detail("/eventtype <mini-jeu|activité|pvp|build|autre>"),
                MoodStyle.detail("/eventset"),
                MoodStyle.detail("/eventopen"),
                MoodStyle.detail("/eventgo"),
                MoodStyle.detail("/eventstop"),
                MoodStyle.detail("/eventcancel")
        );
    }

    private static void broadcastEvent(String... lines) {

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));

        if (lines != null) {
            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        }

        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    private static boolean ensureEvent(Player player) {

        if (!hasEvent()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement créé.", MoodStyle.detail("Utilise §e/eventcreate <nom>"));
            return false;
        }

        return true;
    }

    private static boolean ensureLocation(Player player) {

        if (location == null || location.getWorld() == null) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun point de téléportation défini.", MoodStyle.detail("Utilise §e/eventset à l'endroit de l'événement"));
            return false;
        }

        return true;
    }

    private static boolean hasEvent() {
        return name != null && !name.isBlank();
    }

    private static void clearEvent() {
        name = "";
        description = "";
        type = EventType.AUTRE;
        location = null;
        queueOpen = false;
        running = false;
        queue.clear();
    }
}
