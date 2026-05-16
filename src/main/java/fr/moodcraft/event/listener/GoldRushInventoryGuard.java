package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class GoldRushInventoryGuard implements Listener {

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

        if (!GoldRushTask.isRoundActive()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                GoldRushTask.removeEventPickaxes(player);
            }
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (hasPickaxe(player)) continue;
            player.getInventory().addItem(GoldRushTask.createPickaxe());
            player.sendActionBar("§6⛏ §fPioche Ruée vers l'or reçue");
        }
    }

    private boolean hasPickaxe(Player player) {
        for (var item : player.getInventory().getContents()) {
            if (item == null) continue;
            var meta = item.getItemMeta();
            if (meta == null) continue;
            if (GoldRushTask.PICKAXE_NAME.equals(meta.getDisplayName())) return true;
        }
        return false;
    }
}
