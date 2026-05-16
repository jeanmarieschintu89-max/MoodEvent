package fr.moodcraft.event.gui;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameSize;
import fr.moodcraft.event.generator.GeneratedGameStyleManager;
import fr.moodcraft.event.generator.GeneratedGameType;
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
    public static final String SIZE_TITLE = MoodStyle.guiTitle("Taille mini jeu");
    public static final String CONFIRM_TITLE = MoodStyle.guiTitle("Confirmation mini jeu");

    private static final Map<UUID, GeneratedGameType> SELECTED_TYPE = new HashMap<>();
    private static final Map<UUID, PendingGeneration> PENDING = new HashMap<>();

    private MiniGameGeneratorGUI() {
    }

    public static void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.COMPASS,
                "§6✦ §fGénérateur de mini-jeux §6✦",
                MoodStyle.detail("Modes disponibles : §e2"),
                MoodStyle.detail("Tour Infernale et Mine en folie."),
                MoodStyle.detail("Style unique : §e" + GeneratedGameStyleManager.get(player).getDisplayName()),
                "",
                MoodStyle.info("Sélectionne une épreuve")
        )));

        addType(inv, 10, GeneratedGameType.SURVIE_ETAGES, "Objectif : survivre aux étages qui tombent.");
        addType(inv, 12, GeneratedGameType.RUEE_OR, "Objectif : miner un maximum de minerais.");

        inv.setItem(28, EventItem.item(
                Material.DEEPSLATE_TILES,
                "§6✦ §fStyle unique §6✦",
                MoodStyle.detail("Actuel : §e" + GeneratedGameStyleManager.get(player).getDisplayName()),
                MoodStyle.detail("Les anciens thèmes ont été retirés."),
                MoodStyle.detail("Tous les anciens jeux ont été supprimés."),
                "",
                MoodStyle.success("Stable")
        ));

        inv.setItem(29, EventItem.item(
                Material.CHEST,
                "§6✦ §fLoot généré §6✦",
                MoodStyle.detail("Commun, rare, épique."),
                MoodStyle.detail("Items + argent Vault."),
                MoodStyle.detail("Anti double-récupération."),
                "",
                MoodStyle.info("Configurer")
        ));

        inv.setItem(33, EventItem.item(
                GeneratedGameManager.hasStructure() ? Material.MAGMA_BLOCK : Material.GRAY_DYE,
                GeneratedGameManager.hasStructure() ? "§c✦ §fRestaurer structure §c✦" : "§6✦ §fAucune structure §6✦",
                GeneratedGameManager.hasStructure() ? MoodStyle.detail("Une structure auto est active.") : MoodStyle.detail("Rien à restaurer."),
                MoodStyle.detail("La zone sauvegardée sera remise."),
                "",
                GeneratedGameManager.hasStructure() ? MoodStyle.error("Restaurer") : MoodStyle.detail("Indisponible")
        ));

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir au centre événementiel")));
        player.openInventory(inv);
    }

    public static void openSize(Player player, GeneratedGameType type) {
        SELECTED_TYPE.put(player.getUniqueId(), type);
        Inventory inv = Bukkit.createInventory(null, 54, SIZE_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                type.getIcon(),
                "§6✦ §f" + type.getDisplayName() + " §6✦",
                MoodStyle.detail("Choisis une taille."),
                type == GeneratedGameType.SURVIE_ETAGES ? MoodStyle.detail("Modèles plus hauts, moins étalés.") : MoodStyle.detail("Durée calculée automatiquement."),
                MoodStyle.detail("Style unique : §e" + GeneratedGameStyleManager.get(player).getDisplayName()),
                "",
                MoodStyle.info("Taille de génération")
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

        inv.setItem(49, EventItem.item(Material.ARROW, "§6✦ §fRetour §6✦", MoodStyle.detail("Revenir au générateur")));
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

    public static GeneratedGameType getSelectedType(Player player) {
        return player == null ? null : SELECTED_TYPE.get(player.getUniqueId());
    }

    public static PendingGeneration getPending(Player player) {
        return player == null ? null : PENDING.get(player.getUniqueId());
    }

    public static void clearPending(Player player) {
        if (player != null) PENDING.remove(player.getUniqueId());
    }

    private static void openConfirmInventory(Player player, PendingGeneration pending) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM_TITLE);
        fill(inv);

        inv.setItem(13, EventItem.glow(EventItem.item(
                pending.type().getIcon(),
                "§6✦ §fConfirmation §6✦",
                MoodStyle.detail("Type : §e" + pending.type().getDisplayName()),
                MoodStyle.detail("Taille : §e" + pending.describe()),
                MoodStyle.detail("Style unique : §e" + GeneratedGameStyleManager.get(player).getDisplayName()),
                pending.size() == GeneratedGameSize.GEANT ? MoodStyle.detail("Géant : prudence.") : MoodStyle.detail("Restauration possible avec /eventstop."),
                "",
                MoodStyle.info("Confirmer la génération")
        )));

        inv.setItem(11, EventItem.item(Material.EMERALD_BLOCK, "§6✦ §fConfirmer §6✦", MoodStyle.detail("Génère la structure ici."), MoodStyle.detail("Configure l'événement automatiquement."), "", MoodStyle.success("Générer")));
        inv.setItem(15, EventItem.item(Material.BARRIER, "§c✦ §fAnnuler §c✦", MoodStyle.detail("Ne génère rien."), "", MoodStyle.error("Retour")));
        player.openInventory(inv);
    }

    private static void addType(Inventory inv, int slot, GeneratedGameType type, String detail) {
        inv.setItem(slot, EventItem.item(type.getIcon(), "§6✦ §f" + type.getDisplayName() + " §6✦", MoodStyle.detail(detail), "", MoodStyle.info("Choisir")));
    }

    private static void addSize(Inventory inv, int slot, GeneratedGameType type, GeneratedGameSize size) {
        inv.setItem(slot, EventItem.item(size.getIcon(), "§6✦ §f" + size.getDisplayName() + " §6✦", MoodStyle.detail("Format : §e" + size.describeFor(type)), size == GeneratedGameSize.GEANT ? MoodStyle.detail("Très lourd : prudence.") : size == GeneratedGameSize.GRAND ? MoodStyle.detail("Plus lourd : prudence.") : MoodStyle.detail("Taille sûre."), "", MoodStyle.info("Préparer")));
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
