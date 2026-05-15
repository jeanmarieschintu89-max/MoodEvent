package fr.moodcraft.event.gui;

import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class WaitingRoomGUI {

    public static final String TITLE = MoodStyle.guiTitle("Salle d'attente");

    private WaitingRoomGUI() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.ENDER_EYE,
                "§6✦ §fSalle d'attente §6✦",
                MoodStyle.detail("État : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")),
                MoodStyle.detail("Les joueurs y attendent le lancement"),
                MoodStyle.detail("Les arrivants y reviennent après la fin"),
                "",
                MoodStyle.info("Choisis une taille ou une action")
        )));

        inv.setItem(11, EventItem.item(
                Material.OAK_DOOR,
                "§6✦ §fPetite salle §6✦",
                MoodStyle.detail("Taille : §e7x7"),
                MoodStyle.detail("Pour petits events"),
                MoodStyle.detail("5 à 10 joueurs"),
                "",
                WaitingRoomManager.hasRoom() ? MoodStyle.error("Restaure d'abord l'ancienne salle") : MoodStyle.success("Générer ici")
        ));

        inv.setItem(13, EventItem.item(
                Material.DARK_OAK_DOOR,
                "§6✦ §fSalle moyenne §6✦",
                MoodStyle.detail("Taille : §e11x11"),
                MoodStyle.detail("Taille conseillée"),
                MoodStyle.detail("10 à 25 joueurs"),
                "",
                WaitingRoomManager.hasRoom() ? MoodStyle.error("Restaure d'abord l'ancienne salle") : MoodStyle.success("Générer ici")
        ));

        inv.setItem(15, EventItem.item(
                Material.IRON_DOOR,
                "§6✦ §fGrande salle §6✦",
                MoodStyle.detail("Taille : §e15x15"),
                MoodStyle.detail("Pour gros events"),
                MoodStyle.detail("25 joueurs et plus"),
                "",
                WaitingRoomManager.hasRoom() ? MoodStyle.error("Restaure d'abord l'ancienne salle") : MoodStyle.success("Générer ici")
        ));

        inv.setItem(29, EventItem.item(
                Material.ENDER_PEARL,
                "§6✦ §fTéléporter à la salle §6✦",
                MoodStyle.detail("État : " + (WaitingRoomManager.hasRoom() ? "§adisponible" : "§cindisponible")),
                "",
                MoodStyle.info("Y aller maintenant")
        ));

        inv.setItem(31, EventItem.item(
                Material.MAGMA_BLOCK,
                "§c✦ §fRestaurer la zone §c✦",
                MoodStyle.detail("Supprime la salle générée"),
                MoodStyle.detail("Remet les anciens blocs"),
                "",
                MoodStyle.error("Action sensible")
        ));

        inv.setItem(33, EventItem.item(
                Material.ARROW,
                "§6✦ §fRetour §6✦",
                MoodStyle.detail("Revenir au centre événementiel")
        ));

        player.openInventory(inv);
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }
}
