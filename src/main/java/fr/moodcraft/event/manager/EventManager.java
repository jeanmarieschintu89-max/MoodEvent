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
    private static boolean queueOpen;
    private static boolean running;
    private static boolean autoClosing;
    private static final Set<UUID> queue = new LinkedHashSet<>();
    private static final Set<UUID> participants = new LinkedHashSet<>();
    private static final Set<UUID> eliminated = new HashSet<>();
    private static final Set<UUID> finishedPlayers = new HashSet<>();
    private static final List<UUID> eliminationOrder = new ArrayList<>();
    private static final List<UUID> finalRanking = new ArrayList<>();
    private static final Map<UUID, Location> returnLocations = new HashMap<>();
    private static BukkitTask survivalTask;

    private EventManager() {}

    public static void load() {
        FileConfiguration config = Main.getInstance().getConfig();
        name = config.getString("event.name", "");
        description = config.getString("event.description", "");
        type = sanitizeType(EventType.fromText(config.getString("event.type", "CUSTOM")));
        queueOpen = config.getBoolean("event.queue-open", false);
        running = false;
        autoClosing = false;
        clearCollections();
        cancelSurvivalTask();
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
    public static int getFinishedSize() { return finishedPlayers.size() + eliminated.size(); }
    public static boolean isQueueOpen() { return queueOpen; }
    public static boolean isRunning() { return running; }
    public static boolean hasLocation() { return startLocation != null && startLocation.getWorld() != null; }
    public static boolean hasFinishLocation() { return finishLocation != null && finishLocation.getWorld() != null; }
    public static boolean isCreated() { return hasEvent(); }
    public static boolean isParticipant(Player player) { return player != null && participants.contains(player.getUniqueId()); }

    public static boolean isEventPlayer(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        return queue.contains(uuid) || participants.contains(uuid) || returnLocations.containsKey(uuid);
    }

    public static boolean isNonPvpEventRunning() {
        return !queue.isEmpty() || !participants.isEmpty() || !returnLocations.isEmpty();
    }

    public static boolean isAtFinish(Player player) {
        if (player == null || !running || !getType().usesFinishLine() || !hasFinishLocation()) return false;
        UUID uuid = player.getUniqueId();
        if (!participants.contains(uuid) || finishedPlayers.contains(uuid)) return false;
        Location location = player.getLocation();
        if (location.getWorld() == null || !location.getWorld().equals(finishLocation.getWorld())) return false;
        return Math.abs(location.getX() - finishLocation.getX()) <= 4.5
                && Math.abs(location.getZ() - finishLocation.getZ()) <= 4.5
                && Math.abs(location.getY() - finishLocation.getY()) <= 3.0;
    }

    public static void resetPlayerToStart(Player player) {
        if (player == null || startLocation == null || startLocation.getWorld() == null) return;
        player.setFallDistance(0f);
        player.teleport(startLocation.clone().add(0, 0.15, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.2f);
        player.sendActionBar("§b≈ §fChute détectée §8• §7Retour au départ");
    }

    public static void createEvent(Player player, String rawName) {
        String cleanName = rawName == null ? "" : rawName.trim();
        if (cleanName.length() < 3) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Nom d'événement trop court.", MoodStyle.detail("Exemple : §eLabyrinthe"));
            return;
        }
        name = cleanName;
        description = "Aucune description définie.";
        type = EventType.CUSTOM;
        startLocation = null;
        finishLocation = null;
        clearRuntime();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE,
                "Événement créé.",
                MoodStyle.detail("Nom : §e" + name),
                MoodStyle.detail("Prochaine étape : §e/eventtype §7puis §e/eventdepart"));
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
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Type non disponible.", MoodStyle.detail("Modes : §emine_en_folie§7, §etour_infernale§7, §ewater_jump§7, §emur_escalade§7, §elabyrinthe§7, §eprison_break"));
            return;
        }
        type = next;
        if (!type.usesFinishLine()) finishLocation = null;
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Type d'événement défini.", MoodStyle.detail("Épreuve : " + getType().getDisplayName()));
    }

    public static void cycleType(Player player) {
        if (!ensureEvent(player)) return;
        type = switch (getType()) {
            case SURVIE_ETAGES -> EventType.RUEE_OR;
            case RUEE_OR -> EventType.WATER_JUMP;
            case WATER_JUMP -> EventType.MUR_ESCALADE;
            case MUR_ESCALADE -> EventType.LABYRINTHE;
            case LABYRINTHE -> EventType.PRISON_BREAK;
            default -> EventType.SURVIE_ETAGES;
        };
        if (!type.usesFinishLine()) finishLocation = null;
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Type changé.", MoodStyle.detail("Épreuve : " + getType().getDisplayName()));
    }

    public static void setLocation(Player player) {
        if (!ensureEvent(player)) return;
        startLocation = player.getLocation().clone();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Point de départ défini.", MoodStyle.detail("Les joueurs commenceront ici."));
    }

    public static void setFinishLocation(Player player) {
        if (!ensureEvent(player)) return;
        if (!getType().usesFinishLine()) {
            clearFinishLocation(player);
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Arrivée non utilisée pour cette épreuve.", MoodStyle.detail("Water Jump, Mur d'escalade, Labyrinthe et Prison Break utilisent une arrivée."));
            return;
        }
        finishLocation = player.getLocation().clone();
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Point d'arrivée défini.", MoodStyle.detail("Le Top 3 sera classé ici."));
    }

    public static void clearFinishLocation(Player player) {
        finishLocation = null;
        FileConfiguration config = Main.getInstance().getConfig();
        config.set("event.finish-location", null);
        Main.getInstance().saveConfig();
    }

    public static void openQueue(Player player) {
        if (!ensureEvent(player) || !ensureLocation(player)) return;
        if (getType() == EventType.CUSTOM) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Type d'événement non défini.", MoodStyle.detail("Utilise §e/eventtype mine_en_folie|tour_infernale|water_jump|mur_escalade|labyrinthe|prison_break"));
            return;
        }
        queueOpen = true;
        running = false;
        autoClosing = false;
        clearCollections();
        cancelSurvivalTask();
        save();
        broadcastOpenQueue();
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
        broadcastEvent(MoodStyle.success("La file est fermée."),
                MoodStyle.detail("Joueurs prêts : §e" + sent),
                MoodStyle.detail("Départ dans : §e60 secondes"));
    }

    public static void cancelEvent(Player player) {
        Player actor = resolveActor(player);
        if (actor == null || !ensureEvent(actor)) return;
        autoClosing = false;
        returnParticipants(false);
        restoreGeneratedZones(actor);
        broadcastEvent(MoodStyle.error("Événement annulé."),
                MoodStyle.detail("Les joueurs ont été renvoyés."),
                MoodStyle.detail("Aucune récompense distribuée."));
        clearEvent();
        save();
    }

    public static void stopEvent(Player player) {
        Player actor = resolveActor(player);
        if (actor == null || !ensureEvent(actor)) return;
        autoClosing = false;
        boolean rankedMode = getType().usesSurvivalRanking() || getType().usesFinishLine();
        if (getType().usesSurvivalRanking()) finalizeInfernalRanking();
        if (getType().usesSurvivalRanking()) announceAndRewardTopPlayers(true);
        String line1 = rankedMode ? rankLine(1) : MoodStyle.detail("Mine en folie terminée.");
        String line2 = rankedMode ? rankLine(2) : MoodStyle.detail("Les minerais restent aux joueurs.");
        String line3 = rankedMode ? rankLine(3) : MoodStyle.detail("Merci à tous les participants.");
        returnParticipants(true);
        restoreGeneratedZones(actor);
        broadcastEvent(MoodStyle.success("Événement terminé !"),
                MoodStyle.detail("Merci à tous les participants."),
                line1,
                line2,
                line3,
                MoodStyle.info("Les joueurs sont renvoyés à leur position."));
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
            MoodStyle.successMessage(player, MoodStyle.MODULE,
                    "Inscription confirmée !",
                    MoodStyle.detail("Épreuve : " + getType().getDisplayName()),
                    MoodStyle.detail("But : §f" + shortGoal()),
                    MoodStyle.detail("Position : §e" + queuePositionLabel(player)),
                    MoodStyle.info("Reste prêt, tu seras téléporté en salle d'attente."));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        } else {
            MoodStyle.infoMessage(player, MoodStyle.MODULE,
                    "Tu es déjà inscrit à l'événement.",
                    MoodStyle.detail("Position : §e" + queuePositionLabel(player)));
        }
    }

    public static void showEvent(Player player) {
        if (!hasEvent()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement n'est disponible.");
            return;
        }
        MoodStyle.send(player, MoodStyle.MODULE,
                queueOpen ? MoodStyle.hype("Nouvel événement disponible !") : MoodStyle.info("Événement en préparation."),
                MoodStyle.detail("Épreuve : " + getType().getDisplayName()),
                MoodStyle.detail("But : §f" + shortGoal()),
                MoodStyle.detail("Description : §7" + getDescription()),
                MoodStyle.detail("Top 1 : §6" + rewardLine(1)),
                MoodStyle.detail("Top 2 : §e" + rewardLine(2)),
                MoodStyle.detail("Top 3 : §e" + rewardLine(3)),
                MoodStyle.detail("Participation : §a" + participationLine()),
                queueOpen ? MoodStyle.info("Rejoins maintenant avec §e/event") : MoodStyle.info("Attends l'ouverture de la file."));
    }

    public static void startEvent(Player player) {
        if (!ensureEvent(player) || !ensureLocation(player)) return;
        if (getType().usesFinishLine() && !ensureFinishLocation(player)) return;
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
        finishedPlayers.clear();
        eliminationOrder.clear();
        finalRanking.clear();
        save();
        broadcastEvent(MoodStyle.hype("L'événement commence !"),
                MoodStyle.detail("Épreuve : " + getType().getDisplayName()),
                MoodStyle.detail("Participants : §e" + queue.size()),
                MoodStyle.detail("Objectif : §f" + shortGoal()),
                MoodStyle.info("Bonne chance à tous !"));
        teleportQueue();
    }

    public static void finishPlayer(Player player) {
        if (player == null || !running || !getType().usesFinishLine()) return;
        UUID uuid = player.getUniqueId();
        if (!participants.contains(uuid) || finishedPlayers.contains(uuid)) return;
        finishedPlayers.add(uuid);
        finalRanking.add(uuid);
        int place = finalRanking.size();
        WaitingRoomManager.teleport(player);
        if (place <= 3) {
            player.sendTitle("§6Top " + place, "§fRécompense reçue", 0, 50, 10);
            player.sendActionBar("§6" + formatPlace(place) + " §8• §fBravo !");
            RewardManager.giveTopReward(player, place);
            MoodStyle.successMessage(player, MoodStyle.MODULE,
                    "Tu as terminé !",
                    MoodStyle.detail("Classement : §6Top " + place),
                    MoodStyle.detail("Récompense reçue."),
                    MoodStyle.info("Retour en salle d'attente."));
        } else {
            player.sendTitle("§aArrivée", "§fBien joué !", 0, 50, 10);
            player.sendActionBar("§aArrivée §8• §7Retour en salle d'attente");
        }
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.1f);
        if (finishedPlayers.containsAll(participants)) scheduleAutoStop(player, "Tous les joueurs ont terminé " + cleanDisplay(getType().getDisplayName()) + ".");
    }

    public static void checkSurvivalFloorElimination(Player player) {
        if (player == null || !running || !getType().usesSurvivalRanking()) return;
        UUID uuid = player.getUniqueId();
        if (!participants.contains(uuid) || eliminated.contains(uuid)) return;
        if (!GeneratedGameManager.isSurvivalFall(player.getLocation())) return;
        participants.remove(uuid);
        eliminated.add(uuid);
        eliminationOrder.add(uuid);
        WaitingRoomManager.teleport(player);
        int remaining = participants.size();
        MoodStyle.errorMessage(player, MoodStyle.MODULE,
                "Tu es éliminé.",
                MoodStyle.detail("Retour en salle d'attente."),
                MoodStyle.detail("Joueurs encore en jeu : §e" + remaining));
        broadcastEvent(MoodStyle.info("Tour Infernale"),
                MoodStyle.detail("§c" + player.getName() + " §fest tombé."),
                MoodStyle.detail("Joueurs restants : §e" + remaining));
        if (remaining <= 0 || (remaining == 1 && eliminated.size() > 0)) {
            for (UUID survivor : new HashSet<>(participants)) {
                Player survivorPlayer = Bukkit.getPlayer(survivor);
                if (survivorPlayer != null && survivorPlayer.isOnline()) {
                    WaitingRoomManager.teleport(survivorPlayer);
                    survivorPlayer.sendTitle("§6Victoire", "§fDernier survivant", 0, 55, 12);
                    survivorPlayer.playSound(survivorPlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.15f);
                    survivorPlayer.sendActionBar("§6Victoire §8• §fTop 3 calculé à la fin");
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
                MoodStyle.detail("/eventtype <mine_en_folie|tour_infernale|water_jump|mur_escalade|labyrinthe|prison_break>"),
                MoodStyle.detail("/eventdepart"),
                MoodStyle.detail("/eventsalleattente <mini|petite|moyenne|grande|tresgrande|festival>"),
                MoodStyle.detail("/eventouvrir"),
                MoodStyle.detail("/eventfermer"),
                MoodStyle.detail("/eventlancer"),
                MoodStyle.detail("/eventstop"),
                MoodStyle.detail("/eventmenu"));
    }

    private static void broadcastOpenQueue() {
        broadcastEvent(MoodStyle.hype("Nouvel événement disponible !"),
                MoodStyle.detail("Épreuve : " + getType().getDisplayName()),
                MoodStyle.detail("But : §f" + shortGoal()),
                MoodStyle.detail("Top 1 : §6" + rewardLine(1)),
                MoodStyle.detail("Top 2 : §e" + rewardLine(2)),
                MoodStyle.detail("Top 3 : §e" + rewardLine(3)),
                MoodStyle.detail("Participation : §a" + participationLine()),
                MoodStyle.info("Rejoins maintenant avec §e/event"));
    }

    private static int sendQueueToWaitingRoom() {
        int sent = 0;
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            returnLocations.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
            WaitingRoomManager.teleport(player);
            player.sendTitle("§6Salle d'attente", "§fPrépare-toi, ça commence bientôt", 0, 45, 10);
            MoodStyle.successMessage(player, MoodStyle.MODULE,
                    "Tu es en salle d'attente.",
                    MoodStyle.detail("Épreuve : " + getType().getDisplayName()),
                    MoodStyle.detail("But : §f" + shortGoal()),
                    MoodStyle.detail("Départ automatique : §e60 secondes"),
                    MoodStyle.info("Reste prêt, le départ approche."));
            sent++;
        }
        return sent;
    }

    private static void teleportQueue() {
        int teleported = 0;
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            participants.add(player.getUniqueId());
            returnLocations.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
            player.teleport(startLocation);
            player.sendTitle("§aGO !", "§fBonne chance", 0, 40, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.1f);
            sendLaunchInstructions(player);
            teleported++;
        }
        queue.clear();
        if (getType().usesSurvivalRanking()) startSurvivalFloorTask();
        broadcastEvent(MoodStyle.success("Départ lancé."), MoodStyle.detail("Participants téléportés : §e" + teleported));
    }

    private static void sendLaunchInstructions(Player player) {
        switch (getType()) {
            case SURVIE_ETAGES -> MoodStyle.send(player, MoodStyle.MODULE,
                    MoodStyle.info("Tour Infernale lancée."),
                    MoodStyle.detail("Le sol va disparaître petit à petit."),
                    MoodStyle.detail("Reste en vie le plus longtemps possible."),
                    MoodStyle.detail("Le Top 3 sera récompensé."));
            case RUEE_OR -> MoodStyle.send(player, MoodStyle.MODULE,
                    MoodStyle.info("Mine en folie lancée."),
                    MoodStyle.detail("Mine un maximum de minerais."),
                    MoodStyle.detail("Tu gardes tout ce que tu récoltes."),
                    MoodStyle.detail("Une pioche spéciale t'est donnée."));
            case WATER_JUMP -> MoodStyle.send(player, MoodStyle.MODULE,
                    MoodStyle.info("Water Jump lancé."),
                    MoodStyle.detail("Saute de plateforme en plateforme."),
                    MoodStyle.detail("Si tu tombes, retour au départ."),
                    MoodStyle.detail("Les 3 premiers à l'arrivée gagnent."));
            case LABYRINTHE -> MoodStyle.send(player, MoodStyle.MODULE,
                    MoodStyle.info("Labyrinthe lancé."),
                    MoodStyle.detail("Trouve la sortie avant les autres."),
                    MoodStyle.detail("Les 3 premiers à l'arrivée gagnent."),
                    MoodStyle.detail("Reste rapide et attentif."));
            case PRISON_BREAK -> MoodStyle.send(player, MoodStyle.MODULE,
                    MoodStyle.info("Prison Escape lancé."),
                    MoodStyle.detail("Explore les salles une par une."),
                    MoodStyle.detail("Cherche les boutons, leviers et indices."),
                    MoodStyle.detail("Les fausses portes peuvent te faire perdre du temps."),
                    MoodStyle.detail("Objectif final : §catteindre la sortie rouge."),
                    MoodStyle.detail("Les 3 premiers évadés gagnent."));
            default -> MoodStyle.infoMessage(player, MoodStyle.MODULE, "Tu es entré dans l'événement.");
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

    private static void finalizeInfernalRanking() {
        if (!getType().usesSurvivalRanking()) return;
        finalRanking.clear();
        for (UUID survivor : participants) if (!finalRanking.contains(survivor)) finalRanking.add(survivor);
        for (int i = eliminationOrder.size() - 1; i >= 0; i--) {
            UUID uuid = eliminationOrder.get(i);
            if (!finalRanking.contains(uuid)) finalRanking.add(uuid);
        }
    }

    private static void announceAndRewardTopPlayers(boolean reward) {
        for (int index = 0; index < Math.min(3, finalRanking.size()); index++) {
            Player player = Bukkit.getPlayer(finalRanking.get(index));
            if (player == null || !player.isOnline()) continue;
            int place = index + 1;
            player.sendTitle(place == 1 ? "§6Victoire" : "§6Top " + place, "§fRécompense reçue", 0, 50, 10);
            player.sendActionBar("§6" + formatPlace(place) + " §8• §fBravo !");
            if (reward) RewardManager.giveTopReward(player, place);
        }
    }

    private static String rankLine(int place) {
        if (finalRanking.size() < place) return MoodStyle.detail("Top " + place + " : §7Aucun joueur");
        Player player = Bukkit.getPlayer(finalRanking.get(place - 1));
        return MoodStyle.detail("Top " + place + " : " + (place == 1 ? "§6" : "§e") + (player == null ? "Joueur hors ligne" : player.getName()));
    }

    private static String formatPlace(int place) {
        return switch (place) {
            case 1 -> "Top 1";
            case 2 -> "Top 2";
            case 3 -> "Top 3";
            default -> "Top " + place;
        };
    }

    private static String cleanDisplay(String text) {
        return text == null ? "Épreuve" : text.replaceAll("§.", "");
    }

    private static String shortGoal() {
        return switch (getType()) {
            case WATER_JUMP -> "saute de plateforme en plateforme et atteins l'arrivée";
            case LABYRINTHE -> "trouve la sortie avant les autres";
            case PRISON_BREAK -> "résous les salles et atteins la sortie rouge";
            case SURVIE_ETAGES -> "reste en vie pendant que le sol disparaît";
            case RUEE_OR -> "mine un maximum de minerais pendant le chrono";
            default -> "participe et vise la victoire";
        };
    }

    private static String rewardLine(int place) {
        int items = RewardManager.countRewardItems(place);
        double money = RewardManager.getMoney(place);
        String moneyText = money > 0 ? RewardManager.formatMoney(money) : "";
        String itemText = items > 0 ? items + " item(s)" : "";
        if (!moneyText.isBlank() && !itemText.isBlank()) return moneyText + " + " + itemText;
        if (!moneyText.isBlank()) return moneyText;
        if (!itemText.isBlank()) return itemText;
        return "à configurer";
    }

    private static String participationLine() {
        if (getType() == EventType.RUEE_OR) return "Tu gardes les minerais récoltés";
        int items = RewardManager.countRewardItems(RewardManager.PARTICIPATION);
        double money = RewardManager.getMoney(RewardManager.PARTICIPATION);
        String moneyText = money > 0 ? RewardManager.formatMoney(money) : "";
        String itemText = items > 0 ? items + " item(s)" : "";
        if (!moneyText.isBlank() && !itemText.isBlank()) return moneyText + " + " + itemText;
        if (!moneyText.isBlank()) return moneyText;
        if (!itemText.isBlank()) return itemText;
        return "à configurer";
    }

    private static String queuePositionLabel(Player player) {
        int position = 1;
        for (UUID uuid : queue) {
            if (uuid.equals(player.getUniqueId())) return "#" + position + " / " + Math.max(queue.size(), position);
            position++;
        }
        return "#" + Math.max(1, queue.size()) + " / " + Math.max(1, queue.size());
    }

    private static int returnParticipants(boolean giveParticipation) {
        int returned = 0;
        for (Map.Entry<UUID, Location> entry : returnLocations.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Location returnLocation = entry.getValue();
            if (player == null || !player.isOnline() || returnLocation == null || returnLocation.getWorld() == null) continue;
            if (giveParticipation) RewardManager.giveParticipationReward(player);
            player.teleport(returnLocation);
            player.sendTitle("§aRetour", "§fMerci d'avoir participé", 0, 35, 10);
            player.sendActionBar("§aRetour effectué §8• §7À bientôt pour le prochain event");
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
        broadcastEvent(MoodStyle.success("Épreuve terminée."), MoodStyle.detail(reason), MoodStyle.info("Retour automatique dans §e30 secondes"));
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!running || !hasEvent()) { autoClosing = false; return; }
            Player closer = resolveActor(actor);
            if (closer != null) stopEvent(closer);
            autoClosing = false;
        }, 20L * 30L);
    }

    private static void clearEvent() {
        name = "";
        description = "";
        type = EventType.CUSTOM;
        startLocation = null;
        finishLocation = null;
        clearRuntime();
    }

    private static void clearRuntime() {
        queueOpen = false;
        running = false;
        autoClosing = false;
        clearCollections();
        cancelSurvivalTask();
    }

    private static void clearCollections() {
        queue.clear();
        participants.clear();
        eliminated.clear();
        finishedPlayers.clear();
        eliminationOrder.clear();
        finalRanking.clear();
        returnLocations.clear();
    }

    private static void cancelSurvivalTask() {
        if (survivalTask != null) {
            survivalTask.cancel();
            survivalTask = null;
        }
    }

    private static boolean hasEvent() {
        return name != null && !name.isBlank();
    }

    private static boolean ensureEvent(Player player) {
        if (hasEvent()) return true;
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Aucun événement configuré.");
        return false;
    }

    private static boolean ensureLocation(Player player) {
        if (hasLocation()) return true;
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Point de départ manquant.", MoodStyle.detail("Utilise §e/eventdepart"));
        return false;
    }

    private static boolean ensureFinishLocation(Player player) {
        if (hasFinishLocation()) return true;
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Point d'arrivée manquant.", MoodStyle.detail("Utilise §e/eventarrivee"));
        return false;
    }

    private static Player resolveActor(Player player) {
        if (player != null && player.isOnline()) return player;
        for (Player online : Bukkit.getOnlinePlayers()) return online;
        return null;
    }

    private static void broadcastEvent(String... lines) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            MoodStyle.send(online, MoodStyle.MODULE, lines);
        }
    }

    private static EventType sanitizeType(EventType value) {
        return value == null ? EventType.CUSTOM : value;
    }

    private static Location readLocation(FileConfiguration config, String path) {
        String worldName = config.getString(path + ".world", "");
        if (worldName == null || worldName.isBlank()) return null;
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
