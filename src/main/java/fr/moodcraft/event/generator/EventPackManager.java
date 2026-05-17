package fr.moodcraft.event.generator;

import fr.moodcraft.event.manager.EventLogManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.manager.WaitingRoomTheme;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class EventPackManager {

    private static final int WAITING_ROOM_OFFSET_X = -35;
    private static final int GAME_OFFSET_X = 35;

    private EventPackManager() {
    }

    public static void generatePack(Player player, GeneratedGameType type, GeneratedGameSize size) {
        generatePack(player, type, size, 0);
    }

    public static void generatePack(Player player, GeneratedGameType type, GeneratedGameSize size, int goldRushDurationSeconds) {
        if (player == null || type == null || size == null) return;

        if (GeneratedGameManager.hasStructure()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une structure de mini-jeu existe déjà.", MoodStyle.detail("Restaure-la avant de créer un nouveau pack."));
            return;
        }

        if (WaitingRoomManager.hasRoom()) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Une salle d'attente existe déjà.", MoodStyle.detail("Restaure-la avant de créer un nouveau pack."));
            return;
        }

        if (type == GeneratedGameType.RUEE_OR && !validGoldRushDuration(player, goldRushDurationSeconds)) return;

        WaitingRoomTheme waitingTheme = WaitingRoomManager.getSelectedTheme(player);
        String waitingSize = waitingSizeFor(size);

        Location original = player.getLocation().clone();
        Location waitingCenter = original.clone().add(WAITING_ROOM_OFFSET_X, 0, 0);
        Location gameCenter = original.clone().add(GAME_OFFSET_X, 0, 0);

        WaitingRoomManager.setSelectedStyle(player, waitingTheme.key());
        player.teleport(waitingCenter);
        WaitingRoomManager.build(player, waitingSize);

        if (!WaitingRoomManager.hasRoom()) {
            player.teleport(original);
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Pack interrompu.", MoodStyle.detail("La salle d'attente n'a pas pu être générée."));
            return;
        }

        player.teleport(gameCenter);
        GeneratedGameManager.generate(player, type, size);
        if (type == GeneratedGameType.RUEE_OR) {
            GeneratedGameManager.config().set("gold-rush.duration-seconds", goldRushDurationSeconds);
            GeneratedGameManager.save();
        }
        player.teleport(original);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.15f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Pack événement créé.",
                MoodStyle.detail("Mini-jeu : §e" + type.getDisplayName()),
                MoodStyle.detail("Taille jeu : §e" + size.getDisplayName()),
                type == GeneratedGameType.RUEE_OR ? MoodStyle.detail("Durée : §e" + goldRushDurationSeconds + "s") : MoodStyle.detail("Durée : §7standard"),
                MoodStyle.detail("Salle : §e" + waitingSize + " §8• §7" + waitingTheme.displayName()),
                MoodStyle.detail("Style appliqué uniquement à la salle d'attente."),
                MoodStyle.detail("Salle à gauche §8• §7Mini-jeu à droite"),
                MoodStyle.info("Ouvre la file avec §e/eventouvrir")
        );

        String durationLog = type == GeneratedGameType.RUEE_OR ? " - " + goldRushDurationSeconds + "s" : "";
        EventLogManager.log(player, "Pack événement", type.getDisplayName() + " - " + size.getDisplayName() + durationLog + " - salle " + waitingTheme.displayName());
    }

    private static boolean validGoldRushDuration(Player player, int seconds) {
        if (seconds >= 30 && seconds <= 900) return true;
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Durée Mine en folie invalide.", MoodStyle.detail("Choisis une durée entre §e30s §7et §e900s§7."));
        return false;
    }

    private static String waitingSizeFor(GeneratedGameSize size) {
        return switch (size) {
            case PETIT -> "petite";
            case MOYEN -> "moyenne";
            case GRAND -> "grande";
            case GEANT -> "tresgrande";
        };
    }
}
