package fr.moodcraft.event.gui;

import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.manager.WaitingRoomTheme;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class WaitingRoomGUI {

    public static final String TITLE = MoodStyle.guiTitle("Salle d'attente");
    public static final String STYLE_TITLE = MoodStyle.guiTitle("Style salle manuel");

    private static final int[] STYLE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38};

    private WaitingRoomGUI() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        WaitingRoomTheme selectedTheme = WaitingRoomManager.getSelectedTheme(player);
        String selectedStyle = selectedTheme.displayName();
        boolean trainTunnel = selectedTheme == WaitingRoomTheme.TRAIN_TUNNEL;

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.ENDER_EYE,
                "§6✦ §fSalle d'attente §6✦",
                MoodStyle.detail("État : " + (WaitingRoomManager.hasRoom() ? "§agénérée" : "§cnon générée")),
                MoodStyle.detail("Style choisi : §e" + selectedStyle),
                trainTunnel ? MoodStyle.detail("Train Tunnel utilise une taille unique optimisée.") : MoodStyle.detail("Même choix de styles que le générateur"),
                "",
                trainTunnel ? MoodStyle.info("Génère la taille unique Train Tunnel") : MoodStyle.info("Choisis un style puis une taille")
        )));

        if (trainTunnel) {
            addTrainSize(inv, 14, selectedStyle);
        } else {
            addSize(inv, 10, Material.OAK_DOOR, "Mini", "7x7", "3 à 8 joueurs", selectedStyle);
            addSize(inv, 12, Material.SPRUCE_DOOR, "Petite", "9x9", "5 à 15 joueurs", selectedStyle);
            addSize(inv, 14, Material.DARK_OAK_DOOR, "Moyenne", "11x11", "10 à 25 joueurs", selectedStyle);
            addSize(inv, 16, Material.IRON_DOOR, "Grande", "15x15", "20 à 40 joueurs", selectedStyle);
            addSize(inv, 28, Material.COPPER_DOOR, "Très grande", "19x19", "40 à 70 joueurs", selectedStyle);
            addSize(inv, 30, Material.WARPED_DOOR, "Festival", "23x23", "70 joueurs et plus", selectedStyle);
        }

        inv.setItem(22, EventItem.glow(EventItem.item(
                selectedTheme.accent(),
                "§6✦ §fChoisir le style §6✦",
                MoodStyle.detail("Actuel : §e" + selectedStyle),
                MoodStyle.detail("Ouvre la grille complète des styles."),
                MoodStyle.detail("23 styles disponibles."),
                "",
                MoodStyle.success("Ouvrir les styles")
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

    public static void openStyle(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, STYLE_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.PAINTING,
                "§6✦ §fStyle de salle d'attente §6✦",
                MoodStyle.detail("Choix manuel de la salle."),
                MoodStyle.detail("Même grille que le générateur de pack."),
                MoodStyle.detail("Après ce choix : retour aux tailles."),
                MoodStyle.detail("23 styles disponibles."),
                "",
                MoodStyle.info("Choisis un thème")
        )));

        WaitingRoomTheme[] themes = WaitingRoomTheme.values();
        for (int i = 0; i < themes.length && i < STYLE_SLOTS.length; i++) {
            addTheme(inv, STYLE_SLOTS[i], themes[i], WaitingRoomManager.getSelectedTheme(player) == themes[i]);
        }

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir aux tailles de salle")));
        player.openInventory(inv);
    }

    private static void addTheme(Inventory inv, int slot, WaitingRoomTheme theme, boolean selected) {
        inv.setItem(slot, EventItem.item(
                selected ? Material.EMERALD_BLOCK : theme.accent(),
                (selected ? "§a✔ §f" : "§6✦ §f") + theme.displayName() + " §6✦",
                MoodStyle.detail("Salle uniquement."),
                theme == WaitingRoomTheme.TRAIN_TUNNEL ? MoodStyle.detail("Taille unique optimisée.") : theme == WaitingRoomTheme.PRISON_CELL ? MoodStyle.detail("Cellule décorée pour Prison Escape.") : MoodStyle.detail("Après ce choix : §etaille de salle."),
                "",
                selected ? MoodStyle.success("Sélectionné") : MoodStyle.info("Choisir ce style")
        ));
    }

    private static void addTrainSize(Inventory inv, int slot, String selectedStyle) {
        inv.setItem(slot, EventItem.glow(EventItem.item(
                Material.MINECART,
                "§6✦ §fTrain Tunnel §6✦",
                MoodStyle.detail("Taille : §eunique optimisée"),
                MoodStyle.detail("Style : §e" + selectedStyle),
                MoodStyle.detail("Format léger et stable."),
                "",
                WaitingRoomManager.hasRoom() ? MoodStyle.error("Restaure d'abord l'ancienne salle") : MoodStyle.success("Générer la salle Train")
        )));
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
