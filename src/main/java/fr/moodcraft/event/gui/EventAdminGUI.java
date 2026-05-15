package fr.moodcraft.event.gui;

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
                "§6✦ §fCentre Événementiel §6✦",
                MoodStyle.detail("Nom : §e" + EventManager.getName()),
                MoodStyle.detail("Type : " + EventManager.getType().getDisplayName()),
                MoodStyle.detail("Départ : " + (EventManager.hasLocation() ? "§adéfini" : "§cnon défini")),
                MoodStyle.detail("Arrivée : " + (EventManager.hasFinishLocation() ? "§adéfinie" : "§cnon définie")),
                MoodStyle.detail("Salle d'attente : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")),
                MoodStyle.detail("File : " + (EventManager.isQueueOpen() ? "§aouverte" : "§cfermée")),
                MoodStyle.detail("Participants : §e" + EventManager.getParticipantSize()),
                "",
                MoodStyle.info("Création et lancement depuis ce menu")
        )));

        inv.setItem(10, EventItem.item(
                Material.WRITABLE_BOOK,
                "§6✦ §fNom de l'événement §6✦",
                MoodStyle.detail("Nom actuel : §e" + EventManager.getName()),
                MoodStyle.detail("Saisie dans le chat"),
                "",
                MoodStyle.info("Définir le nom")
        ));

        inv.setItem(12, EventItem.item(
                Material.BOOK,
                "§6✦ §fDescription §6✦",
                MoodStyle.detail("Visible avec §e/event"),
                MoodStyle.detail(shortText(EventManager.getDescription(), 32)),
                "",
                MoodStyle.info("Modifier la description")
        ));

        inv.setItem(14, EventItem.item(
                Material.NAME_TAG,
                "§6✦ §fType de mini-jeu §6✦",
                MoodStyle.detail("Type actuel : " + EventManager.getType().getDisplayName()),
                MoodStyle.detail("Course, Jump, Labyrinthe, PvP..."),
                "",
                MoodStyle.info("Changer le type")
        ));

        inv.setItem(16, EventItem.item(
                Material.LIME_WOOL,
                "§6✦ §fPoint de départ §6✦",
                MoodStyle.detail("Position actuelle du staff"),
                MoodStyle.detail("Départ : " + (EventManager.hasLocation() ? "§aoui" : "§cnon")),
                "",
                MoodStyle.info("Définir ici")
        ));

        inv.setItem(18, EventItem.item(
                Material.RED_WOOL,
                "§6✦ §fPoint d'arrivée §6✦",
                MoodStyle.detail("Utilisé par Course, Jump, Labyrinthe"),
                MoodStyle.detail("Arrivée : " + (EventManager.hasFinishLocation() ? "§aoui" : "§cnon")),
                "",
                MoodStyle.info("Définir ici")
        ));

        inv.setItem(20, EventItem.item(
                WaitingRoomManager.hasRoom() ? Material.ENDER_EYE : Material.DARK_OAK_PLANKS,
                "§6✦ §fSalle d'attente §6✦",
                MoodStyle.detail("Salle auto-générée restaurable"),
                MoodStyle.detail("État : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")),
                "",
                WaitingRoomManager.hasRoom() ? MoodStyle.info("Téléporter à la salle") : MoodStyle.success("Générer ici")
        ));

        inv.setItem(29, EventItem.item(
                Material.CHEST,
                "§6✦ §fRécompenses §6✦",
                MoodStyle.detail("Participation + Top 3"),
                MoodStyle.detail("Items + argent"),
                "",
                MoodStyle.info("Ouvrir le menu")
        ));

        inv.setItem(31, EventItem.item(
                EventManager.isQueueOpen() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                EventManager.isQueueOpen() ? "§c✦ §fFermer la file §c✦" : "§6✦ §fOuvrir la file §6✦",
                MoodStyle.detail("Joueurs en file : §e" + EventManager.getQueueSize()),
                "",
                EventManager.isQueueOpen() ? MoodStyle.error("Fermer la file") : MoodStyle.success("Ouvrir la file")
        ));

        inv.setItem(33, EventItem.glow(EventItem.item(
                Material.LIME_CONCRETE,
                "§6✦ §fLancer l'événement §6✦",
                MoodStyle.detail("Téléporte les joueurs au départ"),
                MoodStyle.detail("Participants : §e" + EventManager.getQueueSize()),
                "",
                MoodStyle.success("Démarrer maintenant")
        )));

        inv.setItem(35, EventItem.item(
                Material.ORANGE_CONCRETE,
                "§6✦ §fTerminer §6✦",
                MoodStyle.detail("Annonce le classement"),
                MoodStyle.detail("Donne les récompenses"),
                MoodStyle.detail("Retour des participants"),
                "",
                MoodStyle.info("Terminer proprement")
        ));

        inv.setItem(38, EventItem.item(
                Material.MAGMA_BLOCK,
                "§c✦ §fRestaurer la salle §c✦",
                MoodStyle.detail("Supprime la salle d'attente"),
                MoodStyle.detail("Restaure les anciens blocs"),
                "",
                MoodStyle.error("Action sensible")
        ));

        inv.setItem(42, EventItem.item(
                Material.BARRIER,
                "§c✦ §fAnnuler l'événement §c✦",
                MoodStyle.detail("Vide la file et arrête l'event"),
                "",
                MoodStyle.error("Action sensible")
        ));

        inv.setItem(44, EventItem.item(
                Material.BARRIER,
                "§c✦ §fFermer §c✦",
                MoodStyle.detail("Fermer ce menu")
        ));

        player.openInventory(inv);
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }

    private static String shortText(String text, int max) {
        if (text == null || text.isBlank()) {
            return "Aucune description";
        }
        String clean = text.replaceAll("§.", "").trim();
        if (clean.length() <= max) {
            return clean;
        }
        return clean.substring(0, Math.max(1, max - 3)) + "...";
    }
}
