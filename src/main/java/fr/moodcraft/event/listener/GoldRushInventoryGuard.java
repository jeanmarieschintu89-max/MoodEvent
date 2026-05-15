package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class GoldRushInventoryGuard implements Listener {

    public static final String PICKAXE_NAME = "§6✦ §fPioche Ruée vers l'or §6✦";

    public GoldRushInventoryGuard() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Main.getInstance(), 10L, 20L);
    }

    private void tick() {
        if (!EventManager.isRunning() || EventManager.getType() != EventType.RUEE_OR) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (hasPickaxe(player)) continue;
            player.getInventory().addItem(createPickaxe());
            player.sendActionBar("§6⛏ §fPioche Ruée vers l'or reçue");
        }
    }

    private boolean hasPickaxe(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.DIAMOND_PICKAXE) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            if (PICKAXE_NAME.equals(meta.getDisplayName())) return true;
        }
        return false;
    }

    public static ItemStack createPickaxe() {
        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(PICKAXE_NAME);
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            pickaxe.setItemMeta(meta);
        }
        return pickaxe;
    }
}
