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
                "§6✦ §fTableau de bord §6✦",
                MoodStyle.detail("Event : §e" + EventManager.getName()),
                MoodStyle.detail("Type : " + EventManager.getType().getDisplayName()),
                MoodStyle.detail("Salle : " + state(WaitingRoomManager.hasRoom())),
                MoodStyle.detail("Structure : " + state(GeneratedGameManager.hasStructure())),
                MoodStyle.detail("File : " + (EventManager.isQueueOpen() ? "§aouverte" : "§cfermée")),
                MoodStyle.detail("En file : §e" + EventManager.getQueueSize() + " §8• §7En jeu : §e" + EventManager.getParticipantSize()),
                "",
                MoodStyle.info("Workflow recommandé : Pack événement")
        )));

        inv.setItem(20, EventItem.glow(EventItem.item(
                Material.COMPASS,
                "§6✦ §fCréer un Pack Événement §6✦",
                MoodStyle.detail("Génère la salle d'attente."),
                MoodStyle.detail("Génère le mini-jeu à côté."),
                MoodStyle.detail("Départ et arrivée automatiques."),
                MoodStyle.detail("Restauration liée à /eventstop."),
                "",
                MoodStyle.success("Ouvrir le générateur")
        )));

        inv.setItem(22, EventItem.item(
                EventManager.isQueueOpen() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                EventManager.isQueueOpen() ? "§c✦ §fFermer la file §c✦" : "§6✦ §fOuvrir la file §6✦",
                MoodStyle.detail("En file : §e" + EventManager.getQueueSize()),
                EventManager.isQueueOpen()
                        ? MoodStyle.detail("Envoie les joueurs en salle d'attente")
                        : MoodStyle.detail("Les joueurs pourront faire §e/event"),
                "",
                EventManager.isQueueOpen() ? MoodStyle.error("Fermer") : MoodStyle.success("Ouvrir")
        ));

        inv.setItem(24, EventItem.glow(EventItem.item(
                Material.LIME_CONCRETE,
                "§6✦ §fLancer §6✦",
                MoodStyle.detail("Salle d'attente → départ"),
                MoodStyle.detail("Explication automatique"),
                "",
                MoodStyle.success("Démarrer")
        )));

        inv.setItem(29, EventItem.item(
                Material.CHEST,
                "§6✦ §fRécompenses §6✦",
                MoodStyle.detail("Participation + Top 3"),
                MoodStyle.detail("Items + argent"),
                MoodStyle.detail("Ne concerne pas la Ruée vers l'or."),
                "",
                MoodStyle.info("Configurer")
        ));

        inv.setItem(31, EventItem.item(
                Material.ORANGE_CONCRETE,
                "§6✦ §fTerminer §6✦",
                MoodStyle.detail("Retour joueurs ancienne position"),
                MoodStyle.detail("Restaure salle + structure"),
                MoodStyle.detail("Reset l'événement"),
                "",
                MoodStyle.info("Clôturer")
        ));

        inv.setItem(33, EventItem.item(
                Material.COMMAND_BLOCK,
                "§6✦ §fMode avancé §6✦",
                MoodStyle.detail("Réglages manuels."),
                MoodStyle.detail("Nom, type, départ, arrivée."),
                MoodStyle.detail("À utiliser en dépannage."),
                "",
                MoodStyle.info("Ouvrir")
        ));

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
