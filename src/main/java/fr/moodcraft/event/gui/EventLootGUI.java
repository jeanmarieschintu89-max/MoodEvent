package fr.moodcraft.event.gui;

import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.loot.LootTier;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EventLootGUI {

    public static final String TITLE = MoodStyle.guiTitle("Loot mini jeux");

    private EventLootGUI() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.CHEST,
                "§6✦ §fLoot généré §6✦",
                MoodStyle.detail("Labyrinthe, Jump, Water Jump."),
                MoodStyle.detail("Loot individuel par joueur."),
                MoodStyle.detail("Anti double-récupération."),
                MoodStyle.detail("Coffre : sélection aléatoire limitée."),
                "",
                MoodStyle.info("Commun, rare, épique")
        )));

        addTier(inv, 11, LootTier.COMMUN);
        addTier(inv, 20, LootTier.RARE);
        addTier(inv, 29, LootTier.EPIQUE);

        inv.setItem(42, EventItem.item(
                Material.LAVA_BUCKET,
                "§c✦ §fReset coffres générés §c✦",
                MoodStyle.detail("Supprime les anciens coffres enregistrés."),
                MoodStyle.detail("Supprime les claims joueurs."),
                MoodStyle.detail("Ne touche pas aux items configurés."),
                "",
                MoodStyle.error("Reset claims")
        ));

        inv.setItem(43, EventItem.item(
                Material.TNT,
                "§c✦ §fReset contenu loot §c✦",
                MoodStyle.detail("Supprime les items des tiers."),
                MoodStyle.detail("Supprime les montants Vault."),
                MoodStyle.detail("Action sensible."),
                "",
                MoodStyle.error("Reset loot.yml")
        ));

        inv.setItem(49, EventItem.item(
                Material.ARROW,
                "§6✦ §fRetour §6✦",
                MoodStyle.detail("Revenir au générateur")
        ));

        player.openInventory(inv);
    }

    private static void addTier(Inventory inv, int slot, LootTier tier) {
        inv.setItem(slot, EventItem.glow(EventItem.item(
                tier.getIcon(),
                "§6✦ §f" + tier.getDisplayName() + " §6✦",
                MoodStyle.detail("Items : §e" + EventLootManager.countItems(tier)),
                MoodStyle.detail("Argent : §a" + EventLootManager.formatMoney(EventLootManager.getMoney(tier))),
                MoodStyle.detail(tier == LootTier.COMMUN ? "Coffre : §e2 stacks max" : "Coffre : §e1 stack max"),
                "",
                MoodStyle.info("Modifier les items")
        )));

        inv.setItem(slot + 1, EventItem.item(
                Material.BARREL,
                "§6✦ §fItems " + tier.getDisplayName() + " §6✦",
                MoodStyle.detail("Dépose les objets possibles."),
                MoodStyle.detail("Le coffre tirera au hasard."),
                MoodStyle.detail("Fermeture = sauvegarde."),
                "",
                MoodStyle.info("Modifier")
        ));

        inv.setItem(slot + 2, EventItem.item(
                Material.GOLD_NUGGET,
                "§6✦ §fArgent " + tier.getDisplayName() + " §6✦",
                MoodStyle.detail("Montant : §a" + EventLootManager.formatMoney(EventLootManager.getMoney(tier))),
                MoodStyle.detail("Saisie dans le chat."),
                "",
                MoodStyle.info("Modifier")
        ));
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }
}
