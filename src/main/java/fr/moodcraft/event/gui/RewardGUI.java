package fr.moodcraft.event.gui;

import fr.moodcraft.event.manager.RewardManager;
import fr.moodcraft.event.util.EventItem;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class RewardGUI {

    public static final String TITLE = MoodStyle.guiTitle("Récompenses Event");

    private RewardGUI() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        inv.setItem(4, EventItem.glow(EventItem.item(
                Material.CHEST,
                "§6✦ §fRécompenses Event §6✦",
                MoodStyle.detail("Participation pour tous."),
                MoodStyle.detail("Top 3 pour la Tour Infernale."),
                MoodStyle.detail("Items + argent configurables."),
                "",
                MoodStyle.info("Tout se règle depuis ce menu")
        )));

        addReward(inv, 10, RewardManager.PARTICIPATION, Material.EMERALD, "Participation");
        addReward(inv, 19, 1, Material.NETHER_STAR, "Top 1");
        addReward(inv, 28, 2, Material.GOLD_INGOT, "Top 2");
        addReward(inv, 37, 3, Material.COPPER_INGOT, "Top 3");

        inv.setItem(49, EventItem.item(
                Material.BARRIER,
                "§c✦ §fFermer §c✦",
                MoodStyle.detail("Retour au jeu")
        ));

        player.openInventory(inv);
    }

    private static void addReward(Inventory inv, int baseSlot, int place, Material icon, String label) {
        inv.setItem(baseSlot, EventItem.glow(EventItem.item(
                icon,
                "§6✦ §f" + label + " §6✦",
                MoodStyle.detail("Items : §e" + RewardManager.countRewardItems(place)),
                MoodStyle.detail("Argent : §a" + RewardManager.formatMoney(RewardManager.getMoney(place))),
                "",
                MoodStyle.info("Résumé de la récompense")
        )));

        inv.setItem(baseSlot + 1, EventItem.item(
                Material.BARREL,
                "§6✦ §fItems " + label + " §6✦",
                MoodStyle.detail("Dépose les objets à donner"),
                MoodStyle.detail("Fermeture du coffre = sauvegarde"),
                "",
                MoodStyle.info("Modifier les items")
        ));

        inv.setItem(baseSlot + 2, EventItem.item(
                Material.GOLD_NUGGET,
                "§6✦ §fArgent " + label + " §6✦",
                MoodStyle.detail("Montant actuel : §a" + RewardManager.formatMoney(RewardManager.getMoney(place))),
                MoodStyle.detail("Saisie du montant dans le chat"),
                "",
                MoodStyle.info("Modifier l'argent")
        ));
    }

    private static void fill(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, EventItem.item(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }
}
