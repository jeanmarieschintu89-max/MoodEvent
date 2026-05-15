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
                MoodStyle.detail("Nom : §e" + EventManager.getName()),
                MoodStyle.detail("Type : " + EventManager.getType().getDisplayName()),
                MoodStyle.detail("Départ : " + state(EventManager.hasLocation())),
                MoodStyle.detail("Arrivée : " + state(EventManager.hasFinishLocation())),
                MoodStyle.detail("Salle : " + state(WaitingRoomManager.hasRoom())),
                MoodStyle.detail("Structure auto : " + state(GeneratedGameManager.hasStructure())),
                MoodStyle.detail("File : " + (EventManager.isQueueOpen() ? "§aouverte" : "§cfermée")),
                MoodStyle.detail("En file : §e" + EventManager.getQueueSize() + " §8• §7En jeu : §e" + EventManager.getParticipantSize()),
                "",
                MoodStyle.info("Préparer, lancer, récompenser")
        )));

        inv.setItem(10, EventItem.item(Material.WRITABLE_BOOK, "§6✦ §fNom §6✦", MoodStyle.detail("Actuel : §e" + EventManager.getName()), MoodStyle.detail("Saisie dans le chat"), "", MoodStyle.info("Modifier le nom")));
        inv.setItem(11, EventItem.item(Material.NAME_TAG, "§6✦ §fType §6✦", MoodStyle.detail("Actuel : " + EventManager.getType().getDisplayName()), MoodStyle.detail("Course, Jump, Labyrinthe..."), "", MoodStyle.info("Changer le type")));
        inv.setItem(12, EventItem.item(Material.BOOK, "§6✦ §fDescription §6✦", MoodStyle.detail(shortText(EventManager.getDescription(), 36)), MoodStyle.detail("Visible avec §e/event"), "", MoodStyle.info("Modifier la description")));

        inv.setItem(14, EventItem.item(Material.LIME_WOOL, "§6✦ §fDépart §6✦", MoodStyle.detail("État : " + state(EventManager.hasLocation())), MoodStyle.detail("Position actuelle du staff"), "", MoodStyle.success("Définir ici")));
        inv.setItem(15, EventItem.item(Material.RED_WOOL, "§6✦ §fArrivée §6✦", MoodStyle.detail("État : " + state(EventManager.hasFinishLocation())), MoodStyle.detail("Course, Jump, Water Jump, Labyrinthe"), "", MoodStyle.success("Définir ici")));
        inv.setItem(16, EventItem.item(WaitingRoomManager.hasRoom() ? Material.ENDER_EYE : Material.DARK_OAK_PLANKS, "§6✦ §fSalle d'attente §6✦", MoodStyle.detail("État : " + state(WaitingRoomManager.hasRoom())), MoodStyle.detail("Style + taille au choix"), "", WaitingRoomManager.hasRoom() ? MoodStyle.info("Téléporter à la salle") : MoodStyle.success("Générer ici")));

        inv.setItem(20, EventItem.item(Material.CHEST, "§6✦ §fRécompenses §6✦", MoodStyle.detail("Participation + Top 3"), MoodStyle.detail("Items + argent"), "", MoodStyle.info("Configurer")));
        inv.setItem(21, EventItem.glow(EventItem.item(Material.COMPASS, "§6✦ §fGénérateur de mini-jeux §6✦", MoodStyle.detail("Crée une structure temporaire."), MoodStyle.detail("Départ et arrivée automatiques."), MoodStyle.detail("Restauration possible."), "", MoodStyle.info("Ouvrir"))));

        inv.setItem(23, EventItem.item(EventManager.isQueueOpen() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK, EventManager.isQueueOpen() ? "§c✦ §fFermer la file §c✦" : "§6✦ §fOuvrir la file §6✦", MoodStyle.detail("En file : §e" + EventManager.getQueueSize()), EventManager.isQueueOpen() ? MoodStyle.detail("Envoie les joueurs en salle d'attente") : MoodStyle.detail("Les joueurs pourront faire §e/event"), "", EventManager.isQueueOpen() ? MoodStyle.error("Fermer") : MoodStyle.success("Ouvrir")));
        inv.setItem(25, EventItem.glow(EventItem.item(Material.LIME_CONCRETE, "§6✦ §fLancer §6✦", MoodStyle.detail("Salle d'attente → départ"), MoodStyle.detail("Explication automatique"), "", MoodStyle.success("Démarrer"))));

        inv.setItem(31, EventItem.item(Material.ORANGE_CONCRETE, "§6✦ §fTerminer §6✦", MoodStyle.detail("Classement + récompenses"), MoodStyle.detail("Retour joueurs puis restauration"), "", MoodStyle.info("Clôturer")));
        inv.setItem(37, EventItem.item(Material.MAGMA_BLOCK, "§c✦ §fRestaurer la salle §c✦", MoodStyle.detail("Supprime la salle d'attente"), MoodStyle.detail("Remet les anciens blocs"), "", MoodStyle.error("Action sensible")));
        inv.setItem(40, EventItem.item(Material.BARRIER, "§c✦ §fAnnuler l'événement §c✦", MoodStyle.detail("Arrête sans récompense"), MoodStyle.detail("Retour des joueurs inscrits"), "", MoodStyle.error("Annuler")));
        inv.setItem(43, EventItem.item(Material.BARRIER, "§c✦ §fFermer §c✦", MoodStyle.detail("Fermer ce menu")));

        player.openInventory(inv);
    }

    private static String state(boolean value) {
        return value ? "§aoui" : "§cnon";
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }

    private static String shortText(String text, int max) {
        if (text == null || text.isBlank()) return "Aucune description";
        String clean = text.replaceAll("§.", "").trim();
        return clean.length() <= max ? clean : clean.substring(0, Math.max(1, max - 3)) + "...";
    }
}
