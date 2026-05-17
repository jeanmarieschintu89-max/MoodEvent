package fr.moodcraft.event.loot;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.hook.VaultHook;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class EventLootManager {

    private static final int SIZE = 27;
    private static final Random RANDOM = new Random();
    private static final Map<UUID, LootTier> EDITING_ITEMS = new HashMap<>();
    private static final Map<UUID, LootTier> EDITING_MONEY = new HashMap<>();
    private static File lootFile;
    private static File claimFile;
    private static FileConfiguration lootConfig;
    private static FileConfiguration claimConfig;

    private EventLootManager() {}

    public static void load() {
        lootFile = new File(Main.getInstance().getDataFolder(), "loot.yml");
        claimFile = new File(Main.getInstance().getDataFolder(), "loot-claims.yml");
        createFile(lootFile);
        createFile(claimFile);
        lootConfig = YamlConfiguration.loadConfiguration(lootFile);
        claimConfig = YamlConfiguration.loadConfiguration(claimFile);
    }

    public static void save() {
        saveFile(lootConfig, lootFile);
        saveFile(claimConfig, claimFile);
    }

    public static void openItemEditor(Player player, LootTier tier) {
        if (player == null || tier == null) return;
        Inventory inventory = Bukkit.createInventory(null, SIZE, title(tier));
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = lootConfig.getItemStack(itemPath(tier, slot));
            if (item != null) inventory.setItem(slot, item.clone());
        }
        EDITING_ITEMS.put(player.getUniqueId(), tier);
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
        MoodStyle.infoMessage(
                player,
                MoodStyle.MODULE,
                "Dépose les items de loot.",
                MoodStyle.detail("Tier : §e" + tier.getDisplayName()),
                MoodStyle.detail("Coffres : tirage aléatoire limité."),
                MoodStyle.detail("Ferme le menu pour sauvegarder.")
        );
    }

    public static boolean isEditingItems(Player player) {
        return player != null && EDITING_ITEMS.containsKey(player.getUniqueId());
    }

    public static boolean isEditingMoney(Player player) {
        return player != null && EDITING_MONEY.containsKey(player.getUniqueId());
    }

    public static void saveItemEditor(Player player, Inventory inventory) {
        if (player == null || inventory == null) return;
        LootTier tier = EDITING_ITEMS.remove(player.getUniqueId());
        if (tier == null) return;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = inventory.getItem(slot);
            lootConfig.set(itemPath(tier, slot), item == null ? null : item.clone());
        }
        save();
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.2f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Loot sauvegardé.",
                MoodStyle.detail("Tier : §e" + tier.getDisplayName()),
                MoodStyle.detail("Items disponibles : §e" + countItems(tier)),
                MoodStyle.detail("Un coffre donnera seulement une sélection aléatoire.")
        );
    }

    public static void startMoneyInput(Player player, LootTier tier) {
        if (player == null || tier == null) return;
        EDITING_MONEY.put(player.getUniqueId(), tier);
        player.closeInventory();
        MoodStyle.infoMessage(
                player,
                MoodStyle.MODULE,
                "Écris le montant Vault du loot.",
                MoodStyle.detail("Tier : §e" + tier.getDisplayName()),
                MoodStyle.detail("Montant actuel : §a" + formatMoney(getMoney(tier))),
                MoodStyle.detail("Tape §cannuler §7pour quitter")
        );
    }

    public static boolean handleMoneyChat(Player player, String message) {
        if (player == null || !isEditingMoney(player)) return false;
        LootTier tier = EDITING_MONEY.remove(player.getUniqueId());
        if (message == null || message.equalsIgnoreCase("annuler") || message.equalsIgnoreCase("cancel")) {
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Saisie annulée.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(message.trim().replace("€", "").replace(",", "."));
        } catch (NumberFormatException exception) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Montant invalide.", MoodStyle.detail("Exemple : §e500"));
            return true;
        }
        if (amount < 0 || amount > 1000000) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Montant refusé.", MoodStyle.detail("Limite : §e0 à 1 000 000€"));
            return true;
        }
        lootConfig.set(basePath(tier) + ".money", amount);
        save();
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Montant sauvegardé.", MoodStyle.detail("Tier : §e" + tier.getDisplayName()), MoodStyle.detail("Argent : §a" + formatMoney(amount)));
        return true;
    }

    public static void registerLootChest(Location location, GeneratedGameType type, LootTier tier) {
        if (location == null || location.getWorld() == null || type == null || tier == null) return;
        if (type != GeneratedGameType.WATER_JUMP && type != GeneratedGameType.LABYRINTHE && type != GeneratedGameType.LABYRINTHE_ROND) return;
        String id = key(location);
        claimConfig.set("chests." + id + ".tier", tier.name());
        claimConfig.set("chests." + id + ".type", type.name());
        claimConfig.set("chests." + id + ".world", location.getWorld().getName());
        claimConfig.set("chests." + id + ".x", location.getBlockX());
        claimConfig.set("chests." + id + ".y", location.getBlockY());
        claimConfig.set("chests." + id + ".z", location.getBlockZ());
        save();
    }

    public static boolean handleClaim(Player player, Location location) {
        if (player == null || location == null || location.getWorld() == null) return false;
        String id = key(location);
        ConfigurationSection section = claimConfig.getConfigurationSection("chests." + id);
        if (section == null) return false;
        LootTier tier;
        try {
            tier = LootTier.valueOf(section.getString("tier", "COMMUN"));
        } catch (IllegalArgumentException exception) {
            tier = LootTier.COMMUN;
        }
        String claimPath = "claims." + id + "." + player.getUniqueId();
        if (claimConfig.getBoolean(claimPath, false)) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Loot déjà récupéré.", MoodStyle.detail("Un joueur ne peut prendre ce bonus qu'une fois."));
            return true;
        }
        claimConfig.set(claimPath, true);
        save();
        giveLoot(player, tier);
        return true;
    }

    public static void clearGeneratedLoot() {
        if (claimConfig == null) return;
        claimConfig.set("chests", null);
        claimConfig.set("claims", null);
        save();
    }

    public static void resetGeneratedClaims(Player player) {
        clearGeneratedLoot();
        if (player != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.9f);
            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Coffres générés réinitialisés.",
                    MoodStyle.detail("Fichier : §eloot-claims.yml"),
                    MoodStyle.detail("Anciens coffres et claims supprimés.")
            );
        }
    }

    public static void resetLootConfig(Player player) {
        if (lootConfig == null) return;
        lootConfig.set("tiers", null);
        save();
        if (player != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.75f);
            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Contenu des loots réinitialisé.",
                    MoodStyle.detail("Fichier : §eloot.yml"),
                    MoodStyle.detail("Items et argent des tiers supprimés.")
            );
        }
    }

    public static int countItems(LootTier tier) {
        if (tier == null) return 0;
        int count = 0;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = lootConfig.getItemStack(itemPath(tier, slot));
            if (item != null && !item.getType().isAir()) count += item.getAmount();
        }
        return count;
    }

    public static double getMoney(LootTier tier) {
        return tier == null ? 0 : lootConfig.getDouble(basePath(tier) + ".money", 0.0);
    }

    public static String formatMoney(double amount) {
        if (amount == Math.rint(amount)) return String.format(Locale.FRANCE, "%.0f€", amount);
        return String.format(Locale.FRANCE, "%.2f€", amount);
    }

    private static void giveLoot(Player player, LootTier tier) {
        List<ItemStack> pool = getLootPool(tier);
        Collections.shuffle(pool, RANDOM);

        int maxStacks = maxStacks(tier);
        int givenStacks = 0;
        int items = 0;

        for (ItemStack source : pool) {
            if (givenStacks >= maxStacks) break;
            ItemStack reward = source.clone();
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(reward);
            for (ItemStack leftover : leftovers.values()) player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            items += reward.getAmount();
            givenStacks++;
        }

        double money = getMoney(tier);
        boolean moneyGiven = money > 0 && VaultHook.deposit(player, money);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.25f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Loot récupéré.",
                MoodStyle.detail("Tier : §e" + tier.getDisplayName()),
                items > 0 ? MoodStyle.detail("Items reçus : §e" + items + " §8• §7Stacks : §e" + givenStacks) : MoodStyle.detail("Items : §7aucun"),
                money > 0 && moneyGiven ? MoodStyle.detail("Argent : §a" + formatMoney(money)) : money > 0 ? MoodStyle.detail("Argent : §cVault indisponible") : MoodStyle.detail("Argent : §7aucun")
        );
    }

    private static List<ItemStack> getLootPool(LootTier tier) {
        List<ItemStack> pool = new ArrayList<>();
        if (tier == null) return pool;
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack item = lootConfig.getItemStack(itemPath(tier, slot));
            if (item == null || item.getType().isAir()) continue;
            pool.add(item.clone());
        }
        return pool;
    }

    private static int maxStacks(LootTier tier) {
        return switch (tier) {
            case COMMUN -> 2;
            case RARE, EPIQUE -> 1;
        };
    }

    private static String title(LootTier tier) {
        return MoodStyle.guiTitle("Loot " + tier.getDisplayName());
    }

    private static String basePath(LootTier tier) {
        return "tiers." + tier.getPath();
    }

    private static String itemPath(LootTier tier, int slot) {
        return basePath(tier) + ".slots." + slot;
    }

    private static String key(Location location) {
        World world = location.getWorld();
        return world.getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    private static void createFile(File file) {
        if (file.exists()) return;
        try {
            Main.getInstance().getDataFolder().mkdirs();
            file.createNewFile();
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private static void saveFile(FileConfiguration config, File file) {
        if (config == null || file == null) return;
        try {
            config.save(file);
        } catch (IOException exception) {
            Main.getInstance().getLogger().warning(exception.getMessage());
        }
    }
}
