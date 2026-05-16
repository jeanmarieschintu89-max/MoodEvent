package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EventManager {

    private static String name = "";
    private static String description = "";
    private static EventType type = EventType.CUSTOM;
    private static Location startLocation;
    private static boolean queueOpen;
    private static boolean running;
    private static boolean autoClosing;
    private static final Set<UUID> queue = new LinkedHashSet<>();
    private static final Set<UUID> participants = new LinkedHashSet<>();
    private static final Set<UUID> eliminated = new HashSet<>();
    private static final Map<UUID, Location> returnLocations = new HashMap<>();
    private static BukkitTask survivalTask;

    private EventManager() {
    }

    public static void load() {
        FileConfiguration config = Main.getInstance().getConfig();
        name = config.getString("event.name", "");
        description = config.getString("event.description", "");
        type = sanitizeType(EventType.fromText(config.getString("event.type", "CUSTOM")));
        queueOpen = config.getBoolean("event.queue-open", false);
        running = false;
        autoClosing = false;
        queue.clear();
        participants.clear();
        eliminated.clear();
        returnLocations.clear();
        cancelSurvivalTask();
        startLocation = readLocation(config, "event.location");
    }

    public static void save() {
        FileConfiguration config = Main.getInstance().getConfig();
        config.set("event.name", name);
        config.set("event.description", description);
        config.set("event.type", getType().name());
        config.set("event.queue-open", queueOpen);
        writeLocation(config, "event.location", startLocation);
        config.set("event.finish-location", null);
        Main.getInstance().saveConfig();
    }

    public static String getName() { return name == null || name.isBlank() ? "Aucun événement" : name; }
    public static String getDescription() { return description == null || description.isBlank() ? "Aucune description définie." : description; }
    public static EventType getType() { return type == null ? EventType.CUSTOM : type; }
    public static int getQueueSize() { return queue.size(); }
    public static int getParticipantSize() { return participants.size(); }
    public static int getFinishedSize() { return eliminated.size(); }
    public static boolean isQueueOpen() { return queueOpen; }
    public static boolean isRunning() { return running; }
    public static boolean hasLocation() { return startLocation != null && startLocation.getWorld() != null; }
    public static boolean hasFinishLocation() { return false; }
    public static boolean isCreated() { return hasEvent(); }

    public static boolean isParticipant(Player player) {
        return player != null && participants.contains(player.getUniqueId());
    }

    public static boolean isEventPlayer(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        return queue.contains(uuid) || participants.contains(uuid) || returnLocations.containsKey(uuid);
    }

    public static boolean isNonPvpEventRunning() {
        return !queue.isEmpty() || !participants.isEmpty() || !returnLocations.isEmpty();
    }

    public static boolean isAtFinish(Player player) {
        return false;
    }

    public static void createEvent(Player player, String rawName) {
        String cleanName = rawName == null ? "" : rawName.trim();
        if (cleanName.length() < 3) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Nom d'événement trop court.", MoodStyle.detail("Exemple : §eMine en folie"));
            return;
        }
        name = cleanName;
        description = "Aucune description définie.";
        type = EventType.CUSTOM;
        startLocation = null;
        clearRuntime();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Événement créé.", MoodStyle.detail("Nom : §e" + name), MoodStyle.detail("Types disponibles : §emine_en_folie §7ou §etour_infernale"), MoodStyle.detail("Départ : §e/eventdepart"));
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
        EventType next = sanitizeType(EventType.fromText(rawType));
        if (next == EventType.CUSTOM) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Type non disponible.", MoodStyle.detail("Modes actifs : §emine_en_folie §7ou §etour_infernale"));
            return;
        }
        type = next;
        clearFinishLocation(player);
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Type d'événement défini.", MoodStyle.detail("Type : " + getType().getDisplayName()));
    }

    public static void cycleType(Player player) {
        if (!ensureEvent(player)) return;
        type = getType() == EventType.SURVIE_ETAGES ? EventType.RUEE_OR : EventType.SURVIE_ETAGES;
        clearFinishLocation(player);
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
        clearFinishLocation(player);
        MoodStyle.infoMessage(player, MoodStyle.MODULE, "Arrivée classique désactivée.", MoodStyle.detail("MoodEvent garde uniquement Mine en folie et Tour Infernale."));
    }

    public static void clearFinishLocation(Player player) {
        FileConfiguration config = Main.getInstance().getConfig();
        config.set("event.finish-location", null);
        Main.getInstance().saveConfig();
    }

    public static void openQueue(Player player) {
        if (!ensureEvent(player) || !ensureLocation(player)) return;
        if (getType() == EventType.CUSTOM) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Type d'événement non défini.", MoodStyle.detail("Utilise le générateur ou §e/eventtype mine_en_folie|tour_infernale"));
            return;
        }
        queueOpen = true;
        running = false;
        autoClosing = false;
        participants.clear();
        eliminated.clear();
        returnLocations.clear();
        cancelSurvivalTask();
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
        broadcastEvent(MoodStyle.success("File d'attente fermée."), MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Joueurs envoyés en salle d'attente : §e" + sent), MoodStyle.info("Lancement possible avec §e/eventlancer"));
    }

    public static void cancelEvent(Player player) {
        Player actor = resolveActor(player);
        if (actor == null || !ensureEvent(actor)) return;
        autoClosing = false;
        int returned = returnParticipants(false);
        restoreGeneratedZones(actor);
        broadcastEvent(MoodStyle.error("Événement annulé."), MoodStyle.detail("Participants renvoyés : §e" + returned), MoodStyle.detail("Aucune récompense distribuée."));
        clearEvent();
        save();
    }

    public static void stopEvent(Player player) {
        Player actor = resolveActor(player);
        if (actor == null || !ensureEvent(actor)) return;
        autoClosing = false;
        int returned = returnParticipants(true);
        restoreGeneratedZones(actor);
        broadcastEvent(
                MoodStyle.success("Événement terminé."),
                MoodStyle.detail("Participants renvoyés : §e" + returned),
                MoodStyle.detail("Récompenses de participation distribuées."),
                MoodStyle.detail("Aucun podium ni Top 3 n'est utilisé.")
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
            MoodStyle.successMessage(player, MoodStyle.MODULE, "Vous avez rejoint la file d'attente.", MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Position : §e" + queue.size()), MoodStyle.detail("Vous serez envoyé en salle d'attente à la fermeture."));
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
        MoodStyle.send(player, MoodStyle.MODULE,
                MoodStyle.info("Événement en préparation."),
                MoodStyle.detail("Nom : §e" + name),
                MoodStyle.detail("Type : " + getType().getDisplayName()),
                MoodStyle.detail("Départ : " + (hasLocation() ? "§adéfini" : "§cnon défini")),
                MoodStyle.detail("Arrivée : §7non utilisée"),
                MoodStyle.detail("Salle d'attente : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")),
                queueOpen ? MoodStyle.info("Faites §e/event §fpour rejoindre") : MoodStyle.detail("Attendez l'ouverture par le staff"));
    }

    public static void startEvent(Player player) {
        if (!ensureEvent(player) || !ensureLocation(player)) return;
        if (!WaitingRoomManager.hasRoom()) {
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
        autoClosing = false;
        participants.clear();
        eliminated.clear();
        save();
        broadcastEvent(MoodStyle.success("L'événement va commencer."), MoodStyle.detail("Événement : §e" + name), MoodStyle.detail("Participants : §e" + queue.size()), MoodStyle.detail("Départ depuis la salle d'attente."));
        countdown(3);
    }

    public static void finishPlayer(Player player) {
        MoodStyle.infoMessage(player, MoodStyle.MODULE, "Arrivée manuelle désactivée.", MoodStyle.detail("MoodEvent garde uniquement Mine en folie et Tour Infernale."));
    }

    public static void checkSurvivalFloorElimination(Player player) {
        if (player == null || !running || !getType().usesSurvivalRanking()) return;
        UUID uuid = player.getUniqueId();
        if (!participants.contains(uuid) || eliminated.contains(uuid)) return;
        if (!GeneratedGameManager.isSurvivalFall(player.getLocation())) return;
        participants.remove(uuid);
        eliminated.add(uuid);
        WaitingRoomManager.teleport(player);
        int remaining = participants.size();
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Vous êtes éliminé.", MoodStyle.detail("Vous êtes renvoyé en salle d'attente."), MoodStyle.detail("Joueurs encore en jeu : §e" + remaining));
        broadcastEvent(MoodStyle.info("Tour Infernale."), MoodStyle.detail("§c" + player.getName() + " §7est tombé."), MoodStyle.detail("Joueurs restants : §e" + remaining));
        if (remaining <= 0 || (remaining == 1 && eliminated.size() > 0)) {
            for (UUID survivor : new HashSet<>(participants)) {
                Player survivorPlayer = Bukkit.getPlayer(survivor);
                if (survivorPlayer != null && survivorPlayer.isOnline()) {
                    WaitingRoomManager.teleport(survivorPlayer);
                    MoodStyle.successMessage(survivorPlayer, MoodStyle.MODULE, "Vous êtes le dernier survivant.", MoodStyle.detail("Retour en salle d'attente."));
                }
            }
            cancelSurvivalTask();
            scheduleAutoStop(player, "Tour Infernale terminée.");
        }
    }

    public static void adminHelp(Player player) {
        MoodStyle.send(player, MoodStyle.MODULE,
                MoodStyle.info("Commandes événement."),
                MoodStyle.detail("/eventcreer <nom>"),
                MoodStyle.detail("/eventtype <mine_en_folie|tour_infernale>"),
                MoodStyle.detail("/eventdepart"),
                MoodStyle.detail("/eventsalleattente <mini|petite|moyenne|grande|tresgrande|festival>"),
                MoodStyle.detail("/eventrestaurersalle"),
                MoodStyle.detail("/eventouvrir"),
                MoodStyle.detail("/eventfermer"),
                MoodStyle.detail("/eventlancer"),
                MoodStyle.detail("/eventstop"),
                MoodStyle.detail("/eventmenu"));
    }

    private static int sendQueueToWaitingRoom() {
        int sent = 0;
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            returnLocations.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
            WaitingRoomManager.teleport(player);
            player.sendTitle("§6Salle d'attente", "§fL'événement va bientôt commencer", 0, 45, 10);
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
            sendLaunchInstructions(player);
            teleported++;
        }
        broadcastEvent(MoodStyle.success("Événement lancé."), MoodStyle.detail("Participants téléportés : §e" + teleported), getType().usesSurvivalRanking() ? MoodStyle.detail("Restez le dernier survivant.") : MoodStyle.detail("Minez un maximum avant la fin."));
        queue.clear();
        if (getType().usesSurvivalRanking()) startSurvivalFloorTask();
    }

    private static void sendLaunchInstructions(Player player) {
        switch (getType()) {
            case SURVIE_ETAGES -> MoodStyle.send(player, MoodStyle.MODULE, MoodStyle.info("Tour Infernale lancée."), MoodStyle.detail("Objectif : restez en jeu le plus longtemps possible."), MoodStyle.detail("Les étages disparaissent progressivement."), MoodStyle.detail("Pas de podium : participation uniquement."));
            case RUEE_OR -> MoodStyle.send(player, MoodStyle.MODULE, MoodStyle.info("Mine en folie lancée."), MoodStyle.detail("Objectif : minez un maximum de minerais."), MoodStyle.detail("Vous gardez les minerais obtenus."), MoodStyle.detail("Pas de podium : participation uniquement."));
            default -> MoodStyle.infoMessage(player, MoodStyle.MODULE, "Vous êtes entré dans l'événement.");
        }
    }

    private static void startSurvivalFloorTask() {
        cancelSurvivalTask();
        survivalTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (!running || !getType().usesSurvivalRanking()) { cancelSurvivalTask(); return; }
            GeneratedGameManager.destroySurvivalBlocks(Math.max(2, participants.size() + 2));
            for (UUID uuid : new HashSet<>(participants)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) checkSurvivalFloorElimination(player);
            }
        }, 40L, 35L);
    }

    private static int returnParticipants(boolean giveParticipation) {
        int returned = 0;
        for (Map.Entry<UUID, Location> entry : returnLocations.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Location returnLocation = entry.getValue();
            if (player == null || !player.isOnline() || returnLocation == null || returnLocation.getWorld() == null) continue;
            if (giveParticipation) {
                RewardManager.giveParticipationReward(player);
                MoodStyle.infoMessage(player, MoodStyle.MODULE, "Événement terminé.", MoodStyle.detail("Vous recevez la récompense de participation."));
            }
            player.teleport(returnLocation);
            player.sendTitle("§aRetour", "§fMerci d'avoir participé", 0, 35, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            returned++;
        }
        clearRuntime();
        return returned;
    }

    private static void restoreGeneratedZones(Player actor) {
        boolean restoredRoom = WaitingRoomManager.hasRoom();
        boolean restoredGenerated = GeneratedGameManager.hasStructure();
        if (restoredRoom) WaitingRoomManager.restore(actor);
        if (restoredGenerated) GeneratedGameManager.restore(actor);
    }

    private static void scheduleAutoStop(Player actor, String reason) {
        if (!running || autoClosing) return;
        autoClosing = true;
        broadcastEvent(MoodStyle.success("Épreuve terminée."), MoodStyle.detail(reason), MoodStyle.info("Clôture automatique dans §e3 secondes"));
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!running || !hasEvent()) {
                autoClosing = false;
                return;
            }
            stopEvent(resolveActor(actor));
        }, 60L);
    }

    private static Player resolveActor(Player player) {
        if (player != null && player.isOnline()) return player;
        for (UUID uuid : returnLocations.keySet()) {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null && online.isOnline()) return online;
        }
        for (UUID uuid : participants) {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null && online.isOnline()) return online;
        }
        for (Player online : Bukkit.getOnlinePlayers()) return online;
        return null;
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

    private static boolean hasEvent() {
        return name != null && !name.isBlank();
    }

    private static void clearRuntime() {
        queueOpen = false;
        running = false;
        autoClosing = false;
        queue.clear();
        participants.clear();
        eliminated.clear();
        returnLocations.clear();
        cancelSurvivalTask();
    }

    private static void clearEvent() {
        name = "";
        description = "";
        type = EventType.CUSTOM;
        startLocation = null;
        clearRuntime();
    }

    private static void cancelSurvivalTask() {
        if (survivalTask != null) {
            survivalTask.cancel();
            survivalTask = null;
        }
    }

    private static EventType sanitizeType(EventType raw) {
        if (raw == EventType.SURVIE_ETAGES || raw == EventType.RUEE_OR) return raw;
        return EventType.CUSTOM;
    }

    private static void broadcastEvent(String... lines) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        if (lines != null) for (String line : lines) Bukkit.broadcastMessage(line);
        Bukkit.broadcastMessage(MoodStyle.FRAME);
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

    private static Location readLocation(FileConfiguration config, String path) {
        World world = Bukkit.getWorld(config.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"), (float) config.getDouble(path + ".yaw"), (float) config.getDouble(path + ".pitch"));
    }
}
