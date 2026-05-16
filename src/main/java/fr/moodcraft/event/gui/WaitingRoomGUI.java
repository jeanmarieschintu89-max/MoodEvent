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
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        String selectedStyle = WaitingRoomManager.getSelectedTheme(player).displayName();

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.ENDER_EYE,
                "§6✦ §fSalle d'attente §6✦",
                MoodStyle.detail("État : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")),
                MoodStyle.detail("Style choisi : §e" + selectedStyle),
                MoodStyle.detail("Zone temporaire restaurable"),
                MoodStyle.detail("Style appliqué uniquement à la salle"),
                "",
                MoodStyle.info("Choisis un style puis une taille")
        )));

        addSize(inv, 10, Material.OAK_DOOR, "Mini", "7x7", "3 à 8 joueurs", selectedStyle);
        addSize(inv, 12, Material.SPRUCE_DOOR, "Petite", "9x9", "5 à 15 joueurs", selectedStyle);
        addSize(inv, 14, Material.DARK_OAK_DOOR, "Moyenne", "11x11", "10 à 25 joueurs", selectedStyle);
        addSize(inv, 16, Material.IRON_DOOR, "Grande", "15x15", "20 à 40 joueurs", selectedStyle);
        addSize(inv, 28, Material.COPPER_DOOR, "Très grande", "19x19", "40 à 70 joueurs", selectedStyle);
        addSize(inv, 30, Material.WARPED_DOOR, "Festival", "23x23", "70 joueurs et plus", selectedStyle);

        inv.setItem(22, EventItem.glow(EventItem.item(
                WaitingRoomManager.getSelectedTheme(player).accent(),
                "§6✦ §fChanger le style §6✦",
                MoodStyle.detail("Actuel : §e" + selectedStyle),
                MoodStyle.detail("Clique pour passer au style suivant."),
                MoodStyle.detail("20 styles disponibles."),
                "",
                MoodStyle.success("Style salle uniquement")
        )));

        inv.setItem(33, EventItem.item(
                Material.ENDER_PEARL,
                "§6✦ §fTéléporter à la salle §6✦",
                MoodStyle.detail("État : " + (WaitingRoomManager.hasRoom() ? "§adisponible" : "§cindisponible")),
                "",
                MoodStyle.info("Y aller maintenant")
        ));

        inv.setItem(35, EventItem.item(
                Material.MAGMA_BLOCK,
                "§c✦ §fRestaurer la zone §c✦",
                MoodStyle.detail("Supprime la salle générée"),
                MoodStyle.detail("Remet les anciens blocs"),
                "",
                MoodStyle.error("Action sensible")
        ));

        inv.setItem(49, EventItem.item(
                Material.ARROW,
                "§6✦ §fRetour §6✦",
                MoodStyle.detail("Revenir au centre événementiel")
        ));

        player.openInventory(inv);
    }

    private static void addSize(Inventory inv, int slot, Material material, String name, String size, String capacity, String selectedStyle) {
        inv.setItem(slot, EventItem.item(
                material,
                "§6✦ §f" + name + " §6✦",
                MoodStyle.detail("Taille : §e" + size),
                MoodStyle.detail("Capacité : §e" + capacity),
                MoodStyle.detail("Style : §e" + selectedStyle),
                "",
                WaitingRoomManager.hasRoom() ? MoodStyle.error("Restaure d'abord l'ancienne salle") : MoodStyle.success("Générer ici")
        ));
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }
}
