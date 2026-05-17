package fr.moodcraft.event.gui;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EventAdminGUI {

    public static final String TITLE = MoodStyle.guiTitle("Centre Événementiel");

    private EventAdminGUI() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.NETHER_STAR,
                "§d✦ §fTableau de bord §d✦",
                MoodStyle.detail("Event : §e" + EventManager.getName()),
                MoodStyle.detail("Type : " + EventManager.getType().getDisplayName()),
                MoodStyle.detail("Salle : " + state(WaitingRoomManager.hasRoom())),
                MoodStyle.detail("Structure : " + state(GeneratedGameManager.hasStructure())),
                MoodStyle.detail("File : " + (EventManager.isQueueOpen() ? "§aouverte" : "§cfermée")),
                MoodStyle.detail("En file : §e" + EventManager.getQueueSize() + " §8• §7En jeu : §e" + EventManager.getParticipantSize()),
                "",
                MoodStyle.info("Centre de contrôle des événements")
        )));

        inv.setItem(20, EventItem.glow(EventItem.item(
                Material.COMPASS,
                "§d✦ §fCréer un Pack Événement §d✦",
                MoodStyle.detail("Génère la zone d'attente."),
                MoodStyle.detail("Génère le mini-jeu sélectionné."),
                MoodStyle.detail("Départ automatique."),
                MoodStyle.detail("Restauration liée à /eventstop."),
                "",
                MoodStyle.success("Ouvrir le générateur")
        )));

        inv.setItem(22, EventItem.item(
                EventManager.isQueueOpen() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                EventManager.isQueueOpen() ? "§c✦ §fFermer la file §c✦" : "§d✦ §fOuvrir la file §d✦",
                MoodStyle.detail("En file : §e" + EventManager.getQueueSize()),
                EventManager.isQueueOpen()
                        ? MoodStyle.detail("Envoie les joueurs en zone d'attente")
                        : MoodStyle.detail("Les joueurs pourront faire §e/event"),
                "",
                EventManager.isQueueOpen() ? MoodStyle.error("Fermer") : MoodStyle.success("Ouvrir")
        ));

        inv.setItem(24, EventItem.glow(EventItem.item(
                Material.LIME_CONCRETE,
                "§d✦ §fLancer §d✦",
                MoodStyle.detail("Zone d'attente → départ"),
                MoodStyle.detail("Explication automatique"),
                "",
                MoodStyle.success("Démarrer")
        )));

        inv.setItem(29, EventItem.item(
                Material.CHEST,
                "§d✦ §fRécompenses §d✦",
                MoodStyle.detail("Participation + Top 3"),
                MoodStyle.detail("Items + argent"),
                MoodStyle.detail("Mine en folie garde ses minerais."),
                "",
                MoodStyle.info("Configurer")
        ));

        inv.setItem(30, EventItem.item(
                Material.ENDER_CHEST,
                "§d✦ §fLoot coffres mini-jeux §d✦",
                MoodStyle.detail("Coffres du Labyrinthe et autres jeux."),
                MoodStyle.detail("Commun, rare, épique."),
                MoodStyle.detail("Items + argent Vault."),
                "",
                MoodStyle.info("Configurer les coffres")
        ));

        inv.setItem(31, EventItem.item(
                Material.ORANGE_CONCRETE,
                "§d✦ §fTerminer §d✦",
                MoodStyle.detail("Retour joueurs ancienne position"),
                MoodStyle.detail("Restaure salle + structure"),
                MoodStyle.detail("Reset l'événement"),
                "",
                MoodStyle.info("Clôturer")
        ));

        inv.setItem(33, EventItem.item(
                Material.COMMAND_BLOCK,
                "§d✦ §fMode avancé §d✦",
                MoodStyle.detail("Réglages manuels."),
                MoodStyle.detail("Nom, type, départ, arrivée Water Jump."),
                MoodStyle.detail("Le générateur reste recommandé."),
                "",
                MoodStyle.info("Ouvrir")
        ));

        inv.setItem(38, EventItem.glow(EventItem.item(
                Material.FIREWORK_ROCKET,
                "§d✦ §fRejoindre l'événement §d✦",
                MoodStyle.detail("Raccourci test : §e/event"),
                MoodStyle.detail("La position finale est mémorisée à la fermeture de file."),
                EventManager.isQueueOpen()
                        ? MoodStyle.detail("File ouverte : tu rejoins directement.")
                        : MoodStyle.detail("File fermée : affiche les infos du jeu."),
                "",
                MoodStyle.success("Entrer dans la file")
        )));

        inv.setItem(40, EventItem.item(
                Material.BARRIER,
                "§c✦ §fFermer §c✦",
                MoodStyle.detail("Fermer ce menu")
        ));

        player.openInventory(inv);
    }

    private static String state(boolean value) {
        return value ? "§aoui" : "§cnon";
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
    }
}
