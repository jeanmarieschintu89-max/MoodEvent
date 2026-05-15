package fr.moodcraft.event.hook;

import fr.moodcraft.event.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultHook {

    private static Economy economy;

    private VaultHook() {
    }

    public static void setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Main.getInstance().getLogger().warning("Vault introuvable : recompenses argent desactivees.");
            return;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            Main.getInstance().getLogger().warning("Aucun fournisseur economie Vault trouve.");
            return;
        }

        economy = provider.getProvider();
        Main.getInstance().getLogger().info("Vault connecte pour les recompenses argent.");
    }

    public static boolean isReady() {
        return economy != null;
    }

    public static boolean deposit(Player player, double amount) {
        if (player == null || amount <= 0 || economy == null) {
            return false;
        }
        economy.depositPlayer(player, amount);
        return true;
    }
}
