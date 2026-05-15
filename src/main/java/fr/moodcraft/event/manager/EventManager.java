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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EventManager {

    private static String name = "";
    private static String description = "";
    private static EventType type = EventType.CUSTOM;
    private static Location startLocation;
    private static Location finishLocation;
    private static boolean queueOpen = false;
    private static boolean running = false;

    private static final Set<UUID> queue = new LinkedHashSet<>();
    private static final Set<UUID> participants = new LinkedHashSet<>();
    private static final Set<UUID> finishedPlayers = new HashSet<>();
    private static final List<UUID> ranking = new ArrayList<>();
    private static final Map<UUID, Location> returnLocations = new HashMap<>();

    private EventManager() {
    }

    public static void load() {
        FileConfiguration config = Main.getInstance().getConfig();
        name = config.getString("event.name", "");
        description = config.getString("event.description", "");
        type = EventType.fromText(config.getString("event.type", "CUSTOM"));
        queueOpen = config.getBoolean("event.queue-open", false);
        running = false;
        queue.clear();
        participants.clear();
        finishedPlayers.clear();
        ranking.clear();
        returnLocations.clear();
        startLocation = readLocation(config, "event.location");
        finishLocation = readLocation(config, "event.finish-location");
    }

    public static void save() {
        FileConfiguration config = Main.getInstance().getConfig();
        config.set("event.name", name);
        config.set("event.description", description);
        config.set("event.type", getType().name());
        config.set("event.queue-open", queueOpen);
        writeLocation(config, "event.location", startLocation);
        writeLocation(config, "event.finish-location", finishLocation);
        Main.getInstance().saveConfig();
    }

    public static String getName() { return name == null || name.isBlank() ? "Aucun événement" : name; }
    public static String getDescription() { return description == null || description.isBlank() ? "Aucune description définie." : description; }
    public static EventType getType() { return type == null ? EventType.CUSTOM : type; }
    public static int getQueueSize() { return queue.size(); }
    public static int getParticipantSize() { return participants.size(); }
    public static int getFinishedSize() { return finishedPlayers.size(); }
    public static boolean isQueueOpen() { return queueOpen; }
    public static boolean isRunning() { return running; }
    public static boolean hasLocation() { return startLocation != null && startLocation.getWorld() != null; }
    public static boolean hasFinishLocation() { return finishLocation != null && finishLocation.getWorld() != null; }
    public static boolean isCreated() { return hasEvent(); }

    public static boolean isParticipant(Player player) {
        return player != null && participants.contains(player.getUniqueId());
    }

    public static boolean isEventPlayer(Player player) {
        if (player == null) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        return queue.contains(uuid) || participants.contains(uuid) || returnLocations.containsKey(uuid);
    }

    public static boolean isNonPvpEventRunning() {
        return getType() != EventType.PVP && (!queue.isEmpty() || !participants.isEmpty() || !returnLocations.isEmpty());
    }

    public static boolean isAtFinish(Player player) {
        if (player == null || !running || !getType().usesFinishLine() || !hasFinishLocation()) {
            return false;
        }
        if (!participants.contains(player.getUniqueId()) || finishedPlayers.contains(player.getUniqueId())) {
            return false;
        }

        Location playerLocation = player.getLocation();
        if (playerLocation.getWorld() == null || !playerLocation.getWorld().equals(finishLocation.getWorld())) {
            return false;
        }

        double dx = Math.abs(playerLocation.getX() - finishLocation.getX());
        double dy = Math.abs(playerLocation.getY() - finishLocation.getY());
        double dz = Math.abs(playerLocation.getZ() - finishLocation.getZ());
        return dx <= 2.5 && dz <= 2.5 && dy <= 3.0;
    }

    public static void createEvent(Player player, String rawName) {
        String cleanName = rawName == null ? "" : rawName.trim();
        if (cleanName.length() < 3) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Nom d'événement trop court.", MoodStyle.detail("Exemple : §eLabyrinthe du Spawn"));
            return;
        }
        name = cleanName;
        description = "Aucune description définie.";
        type = EventType.CUSTOM;
        queueOpen = false;
        running = false;
        queue.clear();
        participants.clear();
        finishedPlayers.clear();
        ranking.clear();
        returnLocations.clear();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Événement créé.", MoodStyle.detail("Nom : §e" + name), MoodStyle.detail("Type : §e/eventtype <course|jump|labyrinthe>"), MoodStyle.detail("Départ : §e/eventdepart"), MoodStyle.detail("Arrivée : §e/eventarrivee"));
    }

    public static void setDescription(Player player, String rawDescription) {
        if (!ensureEvent(player)) return;
        String clean = rawDescription == null ? "" : rawDescription.trim();
        if (clean.length() < 5) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Description trop courte.", MoodStyle.detail("Ajoute quelques détails pour les joueurs."));
            return;
        }
        description = clean;
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Description mise à jour.", MoodStyle.detail(description));
    }

    public static void setType(Player player, String rawType) {
        if (!ensureEvent(player)) return;
        type = EventType.fromText(rawType);
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Type d'événement défini.", MoodStyle.detail("Type : " + getType().getDisplayName()));
    }

    public static void cycleType(Player player) {
        if (!ensureEvent(player)) return;
        EventType[] values = EventType.values();
        int index = getType().ordinal() + 1;
        if (index >= values.length) index = 0;
        type = values[index];
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Type changé.", MoodStyle.detail("Type : " + getType().getDisplayName()));
    }

    public static void setLocation(Player player) {
        if (!ensureEvent(player)) return;
        startLocation = player.getLocation().clone();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Point de départ défini.", MoodStyle.detail("Les joueurs commenceront ici."));
    }

    public static void setFinishLocation(Player player) {
        if (!ensureEvent(player)) return;
        finishLocation = player.getLocation().clone();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Point d'arrivée défini.", MoodStyle.detail("Course, Jump et Labyrinthe classeront les joueurs ici."));
    }

    public static void openQueue(Player player) {
        if (!ensureEvent(player) || !ensureLocation(player)) return;
        queueOpen = true;
        running = false;
        participants.clear();
        finishedPlayers.clear();
        ranking.clear();
        returnLocations.clear();
        save();
        broadcastEvent(MoodStyle.success("File d'attente ouverte."), MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Type : " + getType().getDisplayName()), MoodStyle.info("Faites §e/event §fpour rejoindre la file"));
    }

    public static void closeQueue(Player player) {
        if (!ensureEvent(player)) return;

        if (queue.isEmpty()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun joueur dans la file d'attente.");
            return;
        }

        if (!WaitingRoomManager.hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune salle d'attente générée.", MoodStyle.detail("Utilise §e/eventsalleattente §7avant de fermer la file."));
            return;
        }

        queueOpen = false;
        int sent = sendQueueToWaitingRoom();
        save();

        broadcastEvent(
                MoodStyle.success("File d'attente fermée."),
                MoodStyle.detail("Événement : §e" + name),
                MoodStyle.detail("Joueurs envoyés en salle d'attente : §e" + sent),
                MoodStyle.info("Lancement possible avec §e/eventlancer")
        );
    }

    public static void cancelEvent(Player player) {
        if (!ensureEvent(player)) return;
        int returned = returnParticipants(false);
        broadcastEvent(MoodStyle.error("Événement annulé."), MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Participants renvoyés : §e" + returned), MoodStyle.detail("Aucune récompense distribuée."));
        clearEvent();
        save();
    }

    public static void stopEvent(Player player) {
        if (!ensureEvent(player)) return;
        broadcastRanking();
        announceAndRewardTopPlayers();
        int returned = returnParticipants(true);
        broadcastEvent(MoodStyle.success("Événement terminé."), MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Participants renvoyés : §e" + returned), MoodStyle.detail("Récompenses distribuées."));
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
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Vous avez rejoint la file d'attente.", MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Position : §e" + queue.size()), MoodStyle.detail("Vous serez envoyé en salle d'attente à la fermeture."), MoodStyle.detail("Une récompense de participation est prévue."));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        } else {
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Vous êtes déjà dans la file d'attente.");
        }
    }

    public static void showEvent(Player player) {
        if (!hasEvent()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement n'est disponible.");
            return;
        }
        MoodStyle.send(player, MoodStyle.MODULE, MoodStyle.info("Événement en préparation."), MoodStyle.detail("Nom : §e" + name), MoodStyle.detail("Type : " + getType().getDisplayName()), MoodStyle.detail("Départ : " + (hasLocation() ? "§adéfini" : "§cnon défini")), MoodStyle.detail("Arrivée : " + (hasFinishLocation() ? "§adéfinie" : "§cnon définie")), MoodStyle.detail("Salle d'attente : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")), queueOpen ? MoodStyle.info("Faites §e/event §fpour rejoindre") : MoodStyle.detail("Attendez l'ouverture par le staff"));
    }

    public static void startEvent(Player player) {
        if (!ensureEvent(player) || !ensureLocation(player)) return;
        if (getType().usesFinishLine() && !ensureFinishLocation(player)) return;
        if (getType().usesFinishLine() && !WaitingRoomManager.hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucune salle d'attente générée.", MoodStyle.detail("Commande : §e/eventsalleattente"));
            return;
        }
        if (running) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "L'événement est déjà lancé.");
            return;
        }
        if (queue.isEmpty()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun joueur dans la file d'attente.");
            return;
        }
        queueOpen = false;
        running = true;
        participants.clear();
        finishedPlayers.clear();
        ranking.clear();
        save();
        broadcastEvent(MoodStyle.success("L'événement va commencer."), MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Participants : §e" + queue.size()), MoodStyle.detail("Départ depuis la salle d'attente."));
        countdown(3);
    }

    public static void finishPlayer(Player player) {
        if (player == null || !running || !participants.contains(player.getUniqueId())) return;
        if (!getType().usesFinishLine()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Ce mode n'utilise pas d'arrivée automatique.");
            return;
        }
        UUID uuid = player.getUniqueId();
        if (finishedPlayers.contains(uuid)) return;
        finishedPlayers.add(uuid);
        ranking.add(uuid);
        int place = ranking.size();
        WaitingRoomManager.teleport(player);
        player.sendTitle("§aArrivée", "§fVous êtes §e" + formatPlace(place), 0, 50, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.1f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Vous avez terminé le mini-jeu.", MoodStyle.detail("Classement : §e" + formatPlace(place)), MoodStyle.detail("Vous êtes envoyé en salle d'attente."), place <= 3 ? MoodStyle.detail("Récompense Top 3 prévue à la fin.") : MoodStyle.detail("Récompense de participation prévue à la fin."));
        broadcastEvent(MoodStyle.info("Un joueur a atteint l'arrivée."), MoodStyle.detail("Joueur : §a" + player.getName()), MoodStyle.detail("Classement : §e" + formatPlace(place)));
    }

    private static int sendQueueToWaitingRoom() {
        int sent = 0;
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            returnLocations.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
            WaitingRoomManager.teleport(player);
            player.sendTitle("§6Salle d'attente", "§fL'événement va bientôt commencer", 0, 45, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "La file d'attente est fermée.", MoodStyle.detail("Vous êtes en salle d'attente."), MoodStyle.detail("Patientez jusqu'au lancement."));
            sent++;
        }
        return sent;
    }

    private static void countdown(int number) {
        if (number <= 0) {
            teleportQueue();
            return;
        }
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            player.sendTitle("§6" + number, "§fPréparez-vous", 0, 20, 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
        }
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> countdown(number - 1), 20L);
    }

    private static void teleportQueue() {
        int teleported = 0;
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            participants.add(player.getUniqueId());
            returnLocations.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
            player.teleport(startLocation);
            player.sendTitle("§aGOOO!", "§f" + name, 0, 40, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.1f);
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Vous êtes entré dans l'événement.", MoodStyle.detail("Ender Pearl interdite."), getType() == EventType.PVP ? MoodStyle.detail("PvP autorisé pour ce mode.") : MoodStyle.detail("PvP désactivé pour ce mode."));
            teleported++;
        }
        broadcastEvent(MoodStyle.success("Événement lancé."), MoodStyle.detail("Participants téléportés : §e" + teleported), getType().usesFinishLine() ? MoodStyle.detail("Atteignez l'arrivée pour être classé.") : MoodStyle.detail("Retour prévu à la fin."));
        queue.clear();
    }

    public static void adminHelp(Player player) {
        MoodStyle.send(player, MoodStyle.MODULE, MoodStyle.info("Commandes événement."), MoodStyle.detail("/eventcreer <nom>"), MoodStyle.detail("/eventtype <course|jump|labyrinthe|pvp|quiz>"), MoodStyle.detail("/eventdepart"), MoodStyle.detail("/eventarrivee"), MoodStyle.detail("/eventsalleattente <mini|petite|moyenne|grande|tresgrande|festival>"), MoodStyle.detail("/eventrestaurersalle"), MoodStyle.detail("/eventfinirjoueur <joueur>"), MoodStyle.detail("/eventouvrir"), MoodStyle.detail("/eventfermer"), MoodStyle.detail("/eventlancer"), MoodStyle.detail("/eventstop"), MoodStyle.detail("/eventmenu"));
    }

    private static int returnParticipants(boolean giveParticipation) {
        int returned = 0;
        for (Map.Entry<UUID, Location> entry : returnLocations.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Location returnLocation = entry.getValue();
            if (player == null || !player.isOnline() || returnLocation == null || returnLocation.getWorld() == null) continue;
            int place = getRankingPlace(player.getUniqueId());
            if (place > 0) {
                MoodStyle.successMessage(player, MoodStyle.MODULE, "Classement final confirmé.", MoodStyle.detail("Votre place : §e" + formatPlace(place)), place <= 3 ? MoodStyle.detail("Récompense Top 3 distribuée.") : MoodStyle.detail("Récompense de participation distribuée."));
            } else if (giveParticipation) {
                MoodStyle.infoMessage(player, MoodStyle.MODULE, "Événement terminé.", MoodStyle.detail("Vous recevez la récompense de participation."));
            }
            if (giveParticipation) {
                RewardManager.giveParticipationReward(player);
            }
            player.teleport(returnLocation);
            player.sendTitle("§aRetour", "§fMerci d'avoir participé", 0, 35, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            returned++;
        }
        returnLocations.clear();
        participants.clear();
        finishedPlayers.clear();
        ranking.clear();
        return returned;
    }

    private static void announceAndRewardTopPlayers() {
        for (int index = 0; index < Math.min(3, ranking.size()); index++) {
            Player player = Bukkit.getPlayer(ranking.get(index));
            if (player == null || !player.isOnline()) continue;
            int place = index + 1;
            player.sendTitle("§6Classement final", "§fVous êtes §e" + formatPlace(place), 0, 50, 10);
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Vous êtes dans le Top 3.", MoodStyle.detail("Place : §e" + formatPlace(place)), MoodStyle.detail("Récompense de classement distribuée."));
            RewardManager.giveTopReward(player, place);
        }
    }

    private static void broadcastRanking() {
        if (ranking.isEmpty()) return;
        broadcastEvent(MoodStyle.info("Classement final de §a" + name), rankLine(1), rankLine(2), rankLine(3));
    }

    private static String rankLine(int place) {
        if (ranking.size() < place) return MoodStyle.detail(place + "e : §7Aucun joueur");
        Player player = Bukkit.getPlayer(ranking.get(place - 1));
        String name = player == null ? "Joueur hors ligne" : player.getName();
        return MoodStyle.detail("§e" + formatPlace(place) + " §8- §a" + name);
    }

    private static int getRankingPlace(UUID uuid) {
        int index = ranking.indexOf(uuid);
        return index < 0 ? 0 : index + 1;
    }

    private static String formatPlace(int place) {
        return place == 1 ? "1er" : place + "e";
    }

    private static void broadcastEvent(String... lines) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        if (lines != null) for (String line : lines) Bukkit.broadcastMessage(line);
        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    private static boolean ensureEvent(Player player) {
        if (!hasEvent()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement créé.", MoodStyle.detail("Utilise §e/eventcreer <nom>"));
            return false;
        }
        return true;
    }

    private static boolean ensureLocation(Player player) {
        if (!hasLocation()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun point de départ défini.", MoodStyle.detail("Utilise §e/eventdepart"));
            return false;
        }
        return true;
    }

    private static boolean ensureFinishLocation(Player player) {
        if (!hasFinishLocation()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun point d'arrivée défini.", MoodStyle.detail("Utilise §e/eventarrivee"));
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
        type = EventType.CUSTOM;
        startLocation = null;
        finishLocation = null;
        queueOpen = false;
        running = false;
        queue.clear();
        participants.clear();
        finishedPlayers.clear();
        ranking.clear();
        returnLocations.clear();
    }

    private static Location readLocation(FileConfiguration config, String path) {
        String worldName = config.getString(path + ".world", "");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"), (float) config.getDouble(path + ".yaw"), (float) config.getDouble(path + ".pitch"));
    }

    private static void writeLocation(FileConfiguration config, String path, Location location) {
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
}
