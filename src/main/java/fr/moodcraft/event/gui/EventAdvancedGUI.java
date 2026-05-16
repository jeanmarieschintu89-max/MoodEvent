package fr.moodcraft.event.gui;

import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EventAdvancedGUI {

    public static final String TITLE = MoodStyle.guiTitle("Mode avancé");

    private EventAdvancedGUI() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.COMMAND_BLOCK,
                "§6✦ §fMode avancé §6✦",
                MoodStyle.detail("Réglages manuels de l'événement."),
                MoodStyle.detail("Modes actifs : Mine en folie / Tour Infernale."),
                MoodStyle.detail("Le mode Pack reste recommandé."),
                "",
                MoodStyle.info("Outils staff avancés")
        )));

        inv.setItem(10, EventItem.item(Material.WRITABLE_BOOK, "§6✦ §fNom §6✦", MoodStyle.detail("Actuel : §e" + EventManager.getName()), MoodStyle.detail("Saisie dans le chat"), "", MoodStyle.info("Modifier le nom")));
        inv.setItem(11, EventItem.item(Material.NAME_TAG, "§6✦ §fType §6✦", MoodStyle.detail("Actuel : " + EventManager.getType().getDisplayName()), MoodStyle.detail("Alterne Mine en folie / Tour Infernale"), "", MoodStyle.info("Changer")));
        inv.setItem(12, EventItem.item(Material.BOOK, "§6✦ §fDescription §6✦", MoodStyle.detail(shortText(EventManager.getDescription(), 36)), MoodStyle.detail("Visible avec §e/event"), "", MoodStyle.info("Modifier")));

        inv.setItem(14, EventItem.item(Material.LIME_WOOL, "§6✦ §fDépart manuel §6✦", MoodStyle.detail("État : " + state(EventManager.hasLocation())), MoodStyle.detail("Position actuelle du staff"), "", MoodStyle.success("Définir ici")));
        inv.setItem(15, EventItem.item(Material.GRAY_DYE, "§6✦ §fArrivée désactivée §6✦", MoodStyle.detail("Les anciens parcours sont supprimés."), MoodStyle.detail("Aucun podium ni ligne d'arrivée."), "", MoodStyle.detail("Indisponible")));
        inv.setItem(16, EventItem.item(WaitingRoomManager.hasRoom() ? Material.ENDER_EYE : Material.DARK_OAK_PLANKS, "§6✦ §fSalle d'attente manuelle §6✦", MoodStyle.detail("État : " + state(WaitingRoomManager.hasRoom())), MoodStyle.detail("Zone temporaire restaurable"), "", MoodStyle.info("Ouvrir")));

        inv.setItem(22, EventItem.item(Material.MAGMA_BLOCK, "§c✦ §fRestaurer salle §c✦", MoodStyle.detail("Remet les anciens blocs"), MoodStyle.detail("Action sensible"), "", MoodStyle.error("Restaurer")));
        inv.setItem(31, EventItem.item(Material.BARRIER, "§c✦ §fAnnuler l'événement §c✦", MoodStyle.detail("Arrête sans récompense"), MoodStyle.detail("Retour des joueurs inscrits"), "", MoodStyle.error("Annuler")));

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir au centre événementiel")));
        player.openInventory(inv);
    }

    private static String state(boolean value) {
        return value ? "§aoui" : "§cnon";
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
    }

    private static String shortText(String text, int max) {
        if (text == null || text.isBlank()) return "Aucune description";
        String clean = text.replaceAll("§.", "").trim();
        return clean.length() <= max ? clean : clean.substring(0, Math.max(1, max - 3)) + "...";
    }
}
