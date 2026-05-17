package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.hook.VaultHook;
import fr.moodcraft.event.model.EventType;
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

    public static final int PARTICIPATION = 0;
    private static final int SIZE = 27;
    private static final Map<UUID, Integer> EDITING_ITEMS = new HashMap<>();
    private static final Map<UUID, Integer> EDITING_MONEY = new HashMap<>();
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
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    public static void openItemEditor(Player player, int place) {
        if (!isValidReward(place)) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Récompense invalide.");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, SIZE, title(place));
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = config.getItemStack(itemPath(place, slot));
            if (item != null) inventory.setItem(slot, item.clone());
        }

        EDITING_ITEMS.put(player.getUniqueId(), place);
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);

        MoodStyle.infoMessage(
                player,
                MoodStyle.MODULE,
                "Dépose les items de récompense.",
                MoodStyle.detail("Récompense : §e" + formatReward(place)),
                MoodStyle.detail("Ferme le menu pour sauvegarder.")
        );
    }

    public static void startMoneyInput(Player player, int place) {
        if (!isValidReward(place)) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Récompense invalide.");
            return;
        }

        EDITING_MONEY.put(player.getUniqueId(), place);
        player.closeInventory();
        MoodStyle.infoMessage(
                player,
                MoodStyle.MODULE,
                "Écris le montant en argent dans le chat.",
                MoodStyle.detail("Récompense : §e" + formatReward(place)),
                MoodStyle.detail("Montant actuel : §a" + formatMoney(getMoney(place))),
                MoodStyle.detail("Tape §cannuler §7pour quitter")
        );
    }

    public static boolean isEditingItems(Player player) {
        return player != null && EDITING_ITEMS.containsKey(player.getUniqueId());
    }

    public static boolean isEditingMoney(Player player) {
        return player != null && EDITING_MONEY.containsKey(player.getUniqueId());
    }

    public static boolean handleMoneyChat(Player player, String message) {
        if (player == null || !isEditingMoney(player)) return false;

        int place = EDITING_MONEY.remove(player.getUniqueId());

        if (message == null || message.equalsIgnoreCase("annuler") || message.equalsIgnoreCase("cancel")) {
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Saisie annulée.");
            return true;
        }

        String clean = message.trim().replace("€", "").replace(",", ".");
        double amount;
        try {
            amount = Double.parseDouble(clean);
        } catch (NumberFormatException exception) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Montant invalide.", MoodStyle.detail("Exemple : §e15000"));
            return true;
        }

        if (amount < 0) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Le montant ne peut pas être négatif.");
            return true;
        }

        setMoney(place, amount);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Montant sauvegardé.",
                MoodStyle.detail("Récompense : §e" + formatReward(place)),
                MoodStyle.detail("Argent : §a" + formatMoney(amount))
        );
        return true;
    }

    public static void saveItemEditor(Player player, Inventory inventory) {
        if (player == null || inventory == null) return;

        Integer place = EDITING_ITEMS.remove(player.getUniqueId());
        if (place == null || !isValidReward(place)) return;

        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = inventory.getItem(slot);
            config.set(itemPath(place, slot), item == null ? null : item.clone());
        }
        save();

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Items sauvegardés.",
                MoodStyle.detail("Récompense : §e" + formatReward(place)),
                MoodStyle.detail("Items déposés : §e" + countRewardItems(place))
        );
    }

    public static void giveParticipationReward(Player player) {
        if (EventManager.getType() == EventType.RUEE_OR) return;
        giveReward(player, PARTICIPATION, true);
    }

    public static void giveTopReward(Player player, int place) {
        if (place < 1 || place > 3) return;
        giveReward(player, place, false);
    }

    private static void giveReward(Player player, int place, boolean participation) {
        if (player == null || !isValidReward(place)) return;

        int items = giveItems(player, place);
        double money = getMoney(place);
        boolean moneyGiven = false;

        if (money > 0) moneyGiven = VaultHook.deposit(player, money);

        if (items <= 0 && money <= 0) return;

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.65f, 1.25f);
        player.sendActionBar("§a+ §f" + compactReward(place, items, money, moneyGiven));
        if (!participation && place <= 3) {
            player.sendTitle("§6" + formatReward(place), "§fRécompense reçue", 0, 30, 8);
        }
    }

    private static String compactReward(int place, int items, double money, boolean moneyGiven) {
        StringBuilder builder = new StringBuilder(formatReward(place)).append(" §8• §f");
        boolean hasPart = false;
        if (money > 0) {
            builder.append(moneyGiven ? "§a" : "§c").append(formatMoney(money));
            hasPart = true;
        }
        if (items > 0) {
            if (hasPart) builder.append(" §7+ ");
            builder.append("§e").append(items).append(" item(s)");
            hasPart = true;
        }
        if (!hasPart) builder.append("§7récompense reçue");
        return builder.toString();
    }

    private static int giveItems(Player player, int place) {
        int given = 0;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = config.getItemStack(itemPath(place, slot));
            if (item == null || item.getType().isAir()) continue;

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
            for (ItemStack leftover : leftovers.values()) player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            given += item.getAmount();
        }
        return given;
    }

    public static int countRewardItems(int place) {
        if (!isValidReward(place)) return 0;

        int amount = 0;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = config.getItemStack(itemPath(place, slot));
            if (item != null && !item.getType().isAir()) amount += item.getAmount();
        }
        return amount;
    }

    public static double getMoney(int place) {
        if (!isValidReward(place)) return 0;
        return config.getDouble(basePath(place) + ".money", 0.0);
    }

    private static void setMoney(int place, double amount) {
        config.set(basePath(place) + ".money", amount);
        save();
    }

    public static String formatReward(int place) {
        return switch (place) {
            case PARTICIPATION -> "Participation";
            case 1 -> "Top 1";
            case 2 -> "Top 2";
            case 3 -> "Top 3";
            default -> "Inconnue";
        };
    }

    public static String formatMoney(double amount) {
        if (amount == Math.rint(amount)) return String.format("%.0f€", amount);
        return String.format("%.2f€", amount);
    }

    private static boolean isValidReward(int place) {
        return place >= 0 && place <= 3;
    }

    private static String title(int place) {
        return MoodStyle.guiTitle("Récompense " + formatReward(place));
    }

    private static String basePath(int place) {
        return place == PARTICIPATION ? "participation" : "top." + place;
    }

    private static String itemPath(int place, int slot) {
        return basePath(place) + ".slots." + slot;
    }
}
