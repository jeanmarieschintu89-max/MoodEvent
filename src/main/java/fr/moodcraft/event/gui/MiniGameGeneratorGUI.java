package fr.moodcraft.event.gui;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameSize;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.manager.WaitingRoomTheme;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MiniGameGeneratorGUI {

    public static final String MAIN_TITLE = MoodStyle.guiTitle("Générateur de mini jeux");
    public static final String STYLE_TITLE = MoodStyle.guiTitle("Style salle attente");
    public static final String SIZE_TITLE = MoodStyle.guiTitle("Taille pack event");
    public static final String CONFIRM_TITLE = MoodStyle.guiTitle("Confirmation pack event");

    private static final Map<UUID, GeneratedGameType> SELECTED_TYPE = new HashMap<>();
    private static final Map<UUID, PendingGeneration> PENDING = new HashMap<>();

    private MiniGameGeneratorGUI() {}

    public static void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.COMPASS,
                "§6✦ §fCréation de pack événement §6✦",
                MoodStyle.detail("Parcours clair : §ejeu §8→ §estyle salle §8→ §etaille §8→ §econfirmation"),
                MoodStyle.detail("Le style de salle se choisit après le mini-jeu."),
                MoodStyle.detail("Aucun bouton de thème ici pour éviter la confusion."),
                "",
                MoodStyle.info("Sélectionne d'abord une épreuve")
        )));

        addType(inv, 10, GeneratedGameType.SURVIE_ETAGES, "Salle d'attente + Tour Infernale.");
        addType(inv, 12, GeneratedGameType.RUEE_OR, "Salle d'attente + Mine en folie.");
        addType(inv, 14, GeneratedGameType.WATER_JUMP, "Salle d'attente + Water Jump.");
        addType(inv, 16, GeneratedGameType.LABYRINTHE, "Salle d'attente + Labyrinthe avec sas opposés.");

        inv.setItem(29, EventItem.item(
                Material.CHEST,
                "§6✦ §fLoot généré §6✦",
                MoodStyle.detail("Commun, rare, épique."),
                MoodStyle.detail("Items + argent Vault."),
                MoodStyle.detail("Anti double-récupération."),
                "",
                MoodStyle.info("Configurer les récompenses")
        ));

        inv.setItem(33, EventItem.item(
                GeneratedGameManager.hasStructure() ? Material.MAGMA_BLOCK : Material.GRAY_DYE,
                GeneratedGameManager.hasStructure() ? "§c✦ §fRestaurer structure §c✦" : "§6✦ §fAucune structure §6✦",
                GeneratedGameManager.hasStructure() ? MoodStyle.detail("Une structure auto est active.") : MoodStyle.detail("Rien à restaurer."),
                MoodStyle.detail("La salle et le mini-jeu peuvent être restaurés depuis ici."),
                "",
                GeneratedGameManager.hasStructure() ? MoodStyle.error("Restaurer") : MoodStyle.detail("Indisponible")
        ));

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir au centre événementiel")));
        player.openInventory(inv);
    }

    public static void openStyle(Player player, GeneratedGameType type) {
        SELECTED_TYPE.put(player.getUniqueId(), type);
        Inventory inv = Bukkit.createInventory(null, 54, STYLE_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                type.getIcon(),
                "§6✦ §fStyle de salle d'attente §6✦",
                MoodStyle.detail("Mini-jeu choisi : §e" + type.getDisplayName()),
                MoodStyle.detail("Choix 2/4 : §ele thème de la salle"),
                MoodStyle.detail("Le style sera appliqué seulement à la salle."),
                MoodStyle.detail("Aucun style ne modifie les jeux."),
                "",
                MoodStyle.info("Choisis un thème")
        )));

        WaitingRoomTheme[] themes = WaitingRoomTheme.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33};
        for (int i = 0; i < themes.length && i < slots.length; i++) {
            addTheme(inv, slots[i], themes[i], WaitingRoomManager.getSelectedTheme(player) == themes[i]);
        }

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir au choix du mini-jeu")));
        player.openInventory(inv);
    }

    public static void openSize(Player player, GeneratedGameType type) {
        SELECTED_TYPE.put(player.getUniqueId(), type);
        Inventory inv = Bukkit.createInventory(null, 54, SIZE_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                type.getIcon(),
                "§6✦ §fTaille du pack §6✦",
                MoodStyle.detail("Mini-jeu : §e" + type.getDisplayName()),
                MoodStyle.detail("Style salle : §e" + WaitingRoomManager.getSelectedTheme(player).displayName()),
                MoodStyle.detail("Choix 3/4 : §etaille du pack"),
                type == GeneratedGameType.SURVIE_ETAGES ? MoodStyle.detail("Jeu : modèles plus hauts, moins étalés.") : type == GeneratedGameType.WATER_JUMP ? MoodStyle.detail("Jeu : plateformes au-dessus de l'eau.") : type == GeneratedGameType.LABYRINTHE ? MoodStyle.detail("Jeu : sas départ/arrivée opposés aléatoires.") : MoodStyle.detail("Jeu : durée calculée automatiquement."),
                "",
                MoodStyle.info("Choisis la taille")
        )));

        addSize(inv, 10, type, GeneratedGameSize.PETIT);
        addSize(inv, 12, type, GeneratedGameSize.MOYEN);
        addSize(inv, 14, type, GeneratedGameSize.GRAND);
        addSize(inv, 16, type, GeneratedGameSize.GEANT);

        inv.setItem(31, EventItem.item(
                Material.GRAY_DYE,
                "§6✦ §fPersonnalisé retiré §6✦",
                MoodStyle.detail("Désactivé pour éviter les tailles extrêmes."),
                MoodStyle.detail("Utilise les tailles prédéfinies."),
                "",
                MoodStyle.detail("Indisponible")
        ));

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir au style de salle")));
        player.openInventory(inv);
    }

    public static void openConfirm(Player player, GeneratedGameType type, GeneratedGameSize size) {
        PendingGeneration pending = PendingGeneration.preset(type, size);
        PENDING.put(player.getUniqueId(), pending);
        openConfirmInventory(player, pending);
    }

    public static void openConfirmCustom(Player player, GeneratedGameType type, int value) {
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Taille personnalisée désactivée.", MoodStyle.detail("Utilise les tailles prédéfinies."));
        openSize(player, type);
    }

    public static GeneratedGameType getSelectedType(Player player) { return player == null ? null : SELECTED_TYPE.get(player.getUniqueId()); }
    public static PendingGeneration getPending(Player player) { return player == null ? null : PENDING.get(player.getUniqueId()); }
    public static void clearPending(Player player) { if (player != null) PENDING.remove(player.getUniqueId()); }

    private static void openConfirmInventory(Player player, PendingGeneration pending) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM_TITLE);
        fill(inv);

        inv.setItem(13, EventItem.glow(EventItem.item(
                pending.type().getIcon(),
                "§6✦ §fConfirmation du pack §6✦",
                MoodStyle.detail("Choix 4/4 : §evalider la génération"),
                MoodStyle.detail("Mini-jeu : §e" + pending.type().getDisplayName()),
                MoodStyle.detail("Taille jeu : §e" + pending.describe()),
                MoodStyle.detail("Style salle : §e" + WaitingRoomManager.getSelectedTheme(player).displayName()),
                MoodStyle.detail("Le style sera appliqué uniquement à la salle."),
                pending.type() == GeneratedGameType.WATER_JUMP ? MoodStyle.detail("Water Jump : départ, eau, plateformes, arrivée.") : pending.type() == GeneratedGameType.LABYRINTHE ? MoodStyle.detail("Labyrinthe : entrée/sortie opposées et aléatoires.") : pending.size() == GeneratedGameSize.GEANT ? MoodStyle.detail("Géant : prudence.") : MoodStyle.detail("Restauration possible avec /eventstop."),
                "",
                MoodStyle.info("Vérifie avant de générer")
        )));

        inv.setItem(10, EventItem.item(Material.EMERALD_BLOCK, "§a✦ §fConfirmer §a✦", MoodStyle.detail("Génère salle + mini-jeu."), MoodStyle.detail("Configure l'événement automatiquement."), "", MoodStyle.success("Générer le pack")));
        inv.setItem(16, EventItem.item(Material.ARROW, "§6✦ §fRetour taille §6✦", MoodStyle.detail("Modifier la taille avant de générer."), "", MoodStyle.info("Revenir")));
        inv.setItem(22, EventItem.item(Material.BARRIER, "§c✦ §fAnnuler §c✦", MoodStyle.detail("Ne génère rien."), MoodStyle.detail("Retour au générateur."), "", MoodStyle.error("Annuler")));
        player.openInventory(inv);
    }

    private static void addType(Inventory inv, int slot, GeneratedGameType type, String detail) {
        inv.setItem(slot, EventItem.item(
                type.getIcon(),
                "§6✦ §f" + type.getDisplayName() + " §6✦",
                MoodStyle.detail(detail),
                MoodStyle.detail("Étape suivante : §estyle de salle."),
                "",
                MoodStyle.info("Choisir ce jeu")
        ));
    }

    private static void addTheme(Inventory inv, int slot, WaitingRoomTheme theme, boolean selected) {
        inv.setItem(slot, EventItem.item(
                selected ? Material.EMERALD_BLOCK : theme.accent(),
                (selected ? "§a✔ §f" : "§6✦ §f") + theme.displayName() + " §6✦",
                MoodStyle.detail("Salle uniquement."),
                MoodStyle.detail("Après ce choix : §etaille du pack."),
                "",
                selected ? MoodStyle.success("Sélectionné, continuer") : MoodStyle.info("Choisir ce style")
        ));
    }

    private static void addSize(Inventory inv, int slot, GeneratedGameType type, GeneratedGameSize size) {
        inv.setItem(slot, EventItem.item(
                size.getIcon(),
                "§6✦ §f" + size.getDisplayName() + " §6✦",
                MoodStyle.detail("Jeu : §e" + size.describeFor(type)),
                MoodStyle.detail("Salle liée : §e" + waitingSizeLabel(size)),
                size == GeneratedGameSize.GEANT ? MoodStyle.detail("Très lourd : prudence.") : size == GeneratedGameSize.GRAND ? MoodStyle.detail("Plus lourd : prudence.") : MoodStyle.detail("Taille sûre."),
                "",
                MoodStyle.info("Préparer la confirmation")
        ));
    }

    private static String waitingSizeLabel(GeneratedGameSize size) {
        return switch (size) {
            case PETIT -> "Petite 9x9";
            case MOYEN -> "Moyenne 11x11";
            case GRAND -> "Grande 15x15";
            case GEANT -> "Très grande 19x19";
        };
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
    }

    public record PendingGeneration(GeneratedGameType type, GeneratedGameSize size, Integer customValue) {
        public static PendingGeneration preset(GeneratedGameType type, GeneratedGameSize size) { return new PendingGeneration(type, size, null); }
        public static PendingGeneration custom(GeneratedGameType type, int value) { return new PendingGeneration(type, null, value); }
        public boolean isCustom() { return customValue != null; }
        public String describe() { return isCustom() ? GeneratedGameManager.describeCustom(type, customValue) : size.describeFor(type); }
    }
}
