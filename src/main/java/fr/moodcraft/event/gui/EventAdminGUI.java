package fr.moodcraft.event.gui;

import fr.moodcraft.event.manager.EventManager;
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
                MoodStyle.detail("File : " + (EventManager.isQueueOpen() ? "§aouverte" : "§cfermée")),
                MoodStyle.detail("En cours : " + (EventManager.isRunning() ? "§aoui" : "§cnon")),
                MoodStyle.detail("Joueurs en file : §e" + EventManager.getQueueSize()),
                MoodStyle.detail("Point TP : " + (EventManager.hasLocation() ? "§adéfini" : "§cnon défini")),
                "",
                MoodStyle.info("Pilote les événements depuis ce menu")
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
                MoodStyle.detail("Description affichée aux joueurs"),
                MoodStyle.detail(shortText(EventManager.getDescription(), 32)),
                "",
                MoodStyle.info("Modifier la description")
        ));

        inv.setItem(14, EventItem.item(
                Material.NAME_TAG,
                "§6✦ §fType §6✦",
                MoodStyle.detail("Type actuel : " + EventManager.getType().getDisplayName()),
                MoodStyle.detail("Mini-jeu, activité, PvP, build"),
                "",
                MoodStyle.info("Changer le type")
        ));

        inv.setItem(16, EventItem.item(
                Material.ENDER_PEARL,
                "§6✦ §fPoint de téléportation §6✦",
                MoodStyle.detail("Position actuelle du staff"),
                MoodStyle.detail("Point défini : " + (EventManager.hasLocation() ? "§aoui" : "§cnon")),
                "",
                MoodStyle.info("Définir ici")
        ));

        inv.setItem(20, EventItem.item(
                EventManager.isQueueOpen() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                EventManager.isQueueOpen()
                        ? "§c✦ §fFermer la file §c✦"
                        : "§6✦ §fOuvrir la file §6✦",
                MoodStyle.detail("File d'attente joueurs"),
                MoodStyle.detail("Joueurs en file : §e" + EventManager.getQueueSize()),
                "",
                EventManager.isQueueOpen()
                        ? MoodStyle.error("Fermer la file")
                        : MoodStyle.success("Ouvrir la file")
        ));

        inv.setItem(22, EventItem.glow(EventItem.item(
                Material.LIME_CONCRETE,
                "§6✦ §fLancer l'événement §6✦",
                MoodStyle.detail("Téléporte tous les joueurs en file"),
                MoodStyle.detail("Compte à rebours : §e3 §8/ §e2 §8/ §e1"),
                MoodStyle.detail("Participants : §e" + EventManager.getQueueSize()),
                "",
                MoodStyle.success("Démarrer maintenant")
        )));

        inv.setItem(24, EventItem.item(
                Material.ORANGE_CONCRETE,
                "§6✦ §fTerminer §6✦",
                MoodStyle.detail("Annonce la fin de l'événement"),
                MoodStyle.detail("Vide la file et réinitialise"),
                "",
                MoodStyle.info("Terminer proprement")
        ));

        inv.setItem(26, EventItem.item(
                Material.BARRIER,
                "§c✦ §fAnnuler §c✦",
                MoodStyle.detail("Annule l'événement actuel"),
                MoodStyle.detail("Vide la file d'attente"),
                "",
                MoodStyle.error("Action sensible")
        ));

        inv.setItem(40, EventItem.item(
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
