package fr.moodcraft.event.gui;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameSize;
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
                MoodStyle.detail("Crée une structure temporaire."),
                MoodStyle.detail("Départ et arrivée automatiques."),
                MoodStyle.detail("Restauration possible."),
                "",
                MoodStyle.info("Choisis un mini-jeu")
        )));

        addType(inv, 10, GeneratedGameType.LABYRINTHE, "Murs texturés et sortie rouge.");
        addType(inv, 12, GeneratedGameType.JUMP, "Plateformes en laine colorée.");
        addType(inv, 14, GeneratedGameType.COURSE, "Piste décorée avec obstacles.");
        addType(inv, 16, GeneratedGameType.WATER_JUMP, "Laine colorée au-dessus de l'eau.");
        addType(inv, 22, GeneratedGameType.SURVIE_ETAGES, "Étages qui disparaissent progressivement.");
        addType(inv, 24, GeneratedGameType.RUEE_OR, "Mine bedrock, minerais, temps limité.");

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
                type == GeneratedGameType.RUEE_OR ? MoodStyle.detail("La taille définit aussi le temps.") : MoodStyle.detail("Une confirmation sera demandée."),
                MoodStyle.detail("La zone sera sauvegardée avant génération."),
                "",
                MoodStyle.info("Tailles prédéfinies + personnalisé")
        )));

        addSize(inv, 10, type, GeneratedGameSize.PETIT);
        addSize(inv, 12, type, GeneratedGameSize.MOYEN);
        addSize(inv, 14, type, GeneratedGameSize.GRAND);
        addSize(inv, 16, type, GeneratedGameSize.GEANT);

        inv.setItem(31, EventItem.item(
                Material.WRITABLE_BOOK,
                "§6✦ §fPersonnalisé §6✦",
                customLore(type),
                "",
                MoodStyle.info("Saisir dans le chat")
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
        PendingGeneration pending = PendingGeneration.custom(type, value);
        PENDING.put(player.getUniqueId(), pending);
        openConfirmInventory(player, pending);
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
                MoodStyle.detail("Monde : §e" + player.getWorld().getName()),
                pending.type() == GeneratedGameType.RUEE_OR ? MoodStyle.detail("Mine bedrock + minerais + timer.") : MoodStyle.detail("Zone sauvegardée avant construction."),
                MoodStyle.detail("Restauration possible avec /eventstop ou le menu."),
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
        inv.setItem(slot, EventItem.item(size.getIcon(), "§6✦ §f" + size.getDisplayName() + " §6✦", MoodStyle.detail("Format : §e" + size.describeFor(type)), MoodStyle.detail("Confirmation avant génération."), "", MoodStyle.info("Préparer")));
    }

    private static String customLore(GeneratedGameType type) {
        return switch (type) {
            case LABYRINTHE -> MoodStyle.detail("Largeur impaire : §e15 à 101§7.");
            case JUMP -> MoodStyle.detail("Longueur : §e30 à 250 §7blocs.");
            case COURSE -> MoodStyle.detail("Longueur : §e50 à 1000 §7blocs.");
            case WATER_JUMP -> MoodStyle.detail("Longueur : §e30 à 250 §7blocs.");
            case SURVIE_ETAGES -> MoodStyle.detail("Largeur : §e15 à 61§7, étages auto.");
            case RUEE_OR -> MoodStyle.detail("Largeur mine : §e15 à 51§7, temps auto.");
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
