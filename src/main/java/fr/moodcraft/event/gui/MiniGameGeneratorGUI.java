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
    public static final String GOLD_DURATION_TITLE = MoodStyle.guiTitle("Durée Mine en folie");
    public static final String CONFIRM_TITLE = MoodStyle.guiTitle("Confirmation pack event");

    private static final int[] STYLE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38};
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
                MoodStyle.detail("Mine en folie ajoute une étape durée."),
                MoodStyle.detail("Le style de salle se choisit après le mini-jeu."),
                "",
                MoodStyle.info("Sélectionne d'abord une épreuve")
        )));

        addType(inv, 10, GeneratedGameType.SURVIE_ETAGES, "Salle d'attente + Tour Infernale.");
        addType(inv, 12, GeneratedGameType.RUEE_OR, "Salle d'attente + Mine en folie avec durée au choix.");
        addType(inv, 14, GeneratedGameType.WATER_JUMP, "Salle d'attente + Water Jump.");
        addType(inv, 16, GeneratedGameType.MUR_ESCALADE, "Salle d'attente + Mur d'escalade en hauteur.");
        addType(inv, 20, GeneratedGameType.PRISON_BREAK, "Prison aléatoire avec cellules, couloirs et sortie rouge.");
        addType(inv, 22, GeneratedGameType.LABYRINTHE, "Labyrinthe carré avec sas opposés.");
        addType(inv, 24, GeneratedGameType.LABYRINTHE_ROND, "Labyrinthe rond, départ au centre et sortie extérieure.");

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
                MoodStyle.detail("23 styles disponibles."),
                "",
                MoodStyle.info("Choisis un thème")
        )));

        WaitingRoomTheme[] themes = WaitingRoomTheme.values();
        for (int i = 0; i < themes.length && i < STYLE_SLOTS.length; i++) {
            addTheme(inv, STYLE_SLOTS[i], themes[i], WaitingRoomManager.getSelectedTheme(player) == themes[i]);
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
                type == GeneratedGameType.SURVIE_ETAGES ? MoodStyle.detail("Jeu : modèles plus hauts, moins étalés.") : type == GeneratedGameType.WATER_JUMP ? MoodStyle.detail("Jeu : plateformes au-dessus de l'eau.") : type == GeneratedGameType.MUR_ESCALADE ? MoodStyle.detail("Jeu : plateformes verticales en hauteur.") : type == GeneratedGameType.LABYRINTHE ? MoodStyle.detail("Jeu : forme carrée avec sas opposés.") : type == GeneratedGameType.LABYRINTHE_ROND ? MoodStyle.detail("Jeu : forme ronde, départ au centre.") : type == GeneratedGameType.PRISON_BREAK ? MoodStyle.detail("Jeu : prison différente à chaque génération.") : MoodStyle.detail("Jeu : durée choisie à l'étape suivante."),
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

    public static void openDuration(Player player, GeneratedGameSize size) {
        PendingGeneration pending = PendingGeneration.preset(GeneratedGameType.RUEE_OR, size);
        PENDING.put(player.getUniqueId(), pending);

        Inventory inv = Bukkit.createInventory(null, 27, GOLD_DURATION_TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.CLOCK,
                "§6✦ §fDurée Mine en folie §6✦",
                MoodStyle.detail("Taille mine : §e" + size.getDisplayName()),
                MoodStyle.detail("Choisis le chrono de cette génération."),
                MoodStyle.detail("La durée sera enregistrée dans la structure générée."),
                "",
                MoodStyle.info("Choix 4/5 : durée")
        )));

        addDuration(inv, 10, 60);
        addDuration(inv, 11, 90);
        addDuration(inv, 12, 120);
        addDuration(inv, 14, 180);
        addDuration(inv, 15, 240);
        addDuration(inv, 16, 300);

        inv.setItem(22, EventItem.item(Material.ARROW, "§6✦ §fRetour taille §6✦", MoodStyle.detail("Modifier la taille avant la durée.")));
        player.openInventory(inv);
    }

    public static void openConfirm(Player player, GeneratedGameType type, GeneratedGameSize size) {
        if (type == GeneratedGameType.RUEE_OR) {
            openDuration(player, size);
            return;
        }
        PendingGeneration pending = PendingGeneration.preset(type, size);
        PENDING.put(player.getUniqueId(), pending);
        openConfirmInventory(player, pending);
    }

    public static void openConfirmGoldDuration(Player player, int seconds) {
        PendingGeneration pending = getPending(player);
        if (pending == null || pending.type() != GeneratedGameType.RUEE_OR || pending.size() == null) {
            openMain(player);
            return;
        }
        PendingGeneration next = pending.withGoldDuration(seconds);
        PENDING.put(player.getUniqueId(), next);
        openConfirmInventory(player, next);
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
                pending.type() == GeneratedGameType.RUEE_OR ? MoodStyle.detail("Choix 5/5 : §evalider la génération") : MoodStyle.detail("Choix 4/4 : §evalider la génération"),
                MoodStyle.detail("Mini-jeu : §e" + pending.type().getDisplayName()),
                MoodStyle.detail("Taille jeu : §e" + pending.describe()),
                pending.goldDurationSeconds() != null ? MoodStyle.detail("Durée : §e" + pending.goldDurationSeconds() + "s") : MoodStyle.detail("Durée : §7non utilisée"),
                MoodStyle.detail("Style salle : §e" + WaitingRoomManager.getSelectedTheme(player).displayName()),
                MoodStyle.detail("Le style sera appliqué uniquement à la salle."),
                confirmDetail(pending),
                "",
                MoodStyle.info("Vérifie avant de générer")
        )));

        inv.setItem(10, EventItem.item(Material.EMERALD_BLOCK, "§a✦ §fConfirmer §a✦", MoodStyle.detail("Génère salle + mini-jeu."), MoodStyle.detail("Configure l'événement automatiquement."), "", MoodStyle.success("Générer le pack")));
        inv.setItem(16, EventItem.item(Material.ARROW, pending.type() == GeneratedGameType.RUEE_OR ? "§6✦ §fRetour durée §6✦" : "§6✦ §fRetour taille §6✦", pending.type() == GeneratedGameType.RUEE_OR ? MoodStyle.detail("Modifier la durée avant de générer.") : MoodStyle.detail("Modifier la taille avant de générer."), "", MoodStyle.info("Revenir")));
        inv.setItem(22, EventItem.item(Material.BARRIER, "§c✦ §fAnnuler §c✦", MoodStyle.detail("Ne génère rien."), MoodStyle.detail("Retour au générateur."), "", MoodStyle.error("Annuler")));
        player.openInventory(inv);
    }

    private static String confirmDetail(PendingGeneration pending) {
        return switch (pending.type()) {
            case WATER_JUMP -> MoodStyle.detail("Water Jump : départ, eau, plateformes, arrivée.");
            case MUR_ESCALADE -> MoodStyle.detail("Mur d'escalade : montée verticale, arrivée au sommet.");
            case LABYRINTHE -> MoodStyle.detail("Labyrinthe carré : sas opposés.");
            case LABYRINTHE_ROND -> MoodStyle.detail("Labyrinthe rond : centre vers sortie rouge.");
            case PRISON_BREAK -> MoodStyle.detail("Prison Break : cellules, couloirs aléatoires et sortie rouge.");
            case RUEE_OR -> MoodStyle.detail("Mine en folie : chrono choisi dans le menu.");
            default -> pending.size() == GeneratedGameSize.GEANT ? MoodStyle.detail("Géant : prudence.") : MoodStyle.detail("Restauration possible avec /eventstop.");
        };
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
                theme == WaitingRoomTheme.PRISON_CELL ? MoodStyle.detail("Cellule décorée pour Prison Escape.") : MoodStyle.detail("Après ce choix : §etaille du pack."),
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
                type == GeneratedGameType.RUEE_OR ? MoodStyle.detail("Étape suivante : §edurée") : "",
                "",
                MoodStyle.info("Préparer la confirmation")
        ));
    }

    private static void addDuration(Inventory inv, int slot, int seconds) {
        inv.setItem(slot, EventItem.item(
                Material.CLOCK,
                "§6✦ §f" + seconds + " secondes §6✦",
                MoodStyle.detail("Durée de Mine en folie."),
                seconds <= 90 ? MoodStyle.detail("Format court et nerveux.") : seconds >= 240 ? MoodStyle.detail("Format long pour grosse mine.") : MoodStyle.detail("Format équilibré."),
                "",
                MoodStyle.info("Choisir " + seconds + "s")
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

    public record PendingGeneration(GeneratedGameType type, GeneratedGameSize size, Integer customValue, Integer goldDurationSeconds) {
        public static PendingGeneration preset(GeneratedGameType type, GeneratedGameSize size) { return new PendingGeneration(type, size, null, null); }
        public static PendingGeneration custom(GeneratedGameType type, int value) { return new PendingGeneration(type, null, value, null); }
        public boolean isCustom() { return customValue != null; }
        public PendingGeneration withGoldDuration(int seconds) { return new PendingGeneration(type, size, customValue, seconds); }
        public String describe() { return isCustom() ? GeneratedGameManager.describeCustom(type, customValue) : size.describeFor(type); }
    }
}
