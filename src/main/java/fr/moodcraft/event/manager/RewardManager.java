package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RewardManager {

    private static final int SIZE = 27;
    private static final Map<UUID, Integer> EDITING = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    private RewardManager() {
    }

    public static void load() {
        file = new File(Main.getInstance().getDataFolder(), "recompenses.yml");
        if (!file.exists()) {
            try {
                Main.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                Main.getInstance().getLogger().warning(exception.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        if (config == null || file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static void openEditor(Player player, int place) {
        if (!isValidPlace(place)) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Place invalide.", MoodStyle.detail("Utilise §e/eventrecompense <1|2|3>"));
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, SIZE, title(place));
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = config.getItemStack(path(place, slot));
            if (item != null) {
                inventory.setItem(slot, item.clone());
            }
        }

        EDITING.put(player.getUniqueId(), place);
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);

        MoodStyle.infoMessage(
                player,
                MoodStyle.MODULE,
                "Dépose les récompenses dans le coffre.",
                MoodStyle.detail("Place : §e" + formatPlace(place)),
                MoodStyle.detail("Ferme le menu pour sauvegarder.")
        );
    }

    public static boolean isEditing(Player player) {
        return player != null && EDITING.containsKey(player.getUniqueId());
    }

    public static void saveEditor(Player player, Inventory inventory) {
        if (player == null || inventory == null) {
            return;
        }

        Integer place = EDITING.remove(player.getUniqueId());
        if (place == null) {
            return;
        }

        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = inventory.getItem(slot);
            config.set(path(place, slot), item == null ? null : item.clone());
        }
        save();

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Récompense sauvegardée.",
                MoodStyle.detail("Place : §e" + formatPlace(place)),
                MoodStyle.detail("Items déposés : §e" + countRewardItems(place))
        );
    }

    public static void giveReward(Player player, int place) {
        if (player == null || !isValidPlace(place)) {
            return;
        }

        int given = 0;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = config.getItemStack(path(place, slot));
            if (item == null || item.getType().isAir()) {
                continue;
            }

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            given += item.getAmount();
        }

        if (given <= 0) {
            MoodStyle.infoMessage(
                    player,
                    MoodStyle.MODULE,
                    "Aucune récompense item définie pour votre place.",
                    MoodStyle.detail("Place : §e" + formatPlace(place))
            );
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.25f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Récompense reçue.",
                MoodStyle.detail("Place : §e" + formatPlace(place)),
                MoodStyle.detail("Items donnés : §e" + given),
                MoodStyle.detail("Si votre inventaire est plein, le surplus tombe au sol.")
        );
    }

    public static int countRewardItems(int place) {
        if (!isValidPlace(place)) {
            return 0;
        }

        int amount = 0;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = config.getItemStack(path(place, slot));
            if (item != null && !item.getType().isAir()) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public static String formatPlace(int place) {
        return place == 1 ? "1er" : place + "e";
    }

    private static boolean isValidPlace(int place) {
        return place >= 1 && place <= 3;
    }

    private static String title(int place) {
        return MoodStyle.guiTitle("Récompense " + formatPlace(place));
    }

    private static String path(int place, int slot) {
        return "recompenses." + place + ".slots." + slot;
    }
}
