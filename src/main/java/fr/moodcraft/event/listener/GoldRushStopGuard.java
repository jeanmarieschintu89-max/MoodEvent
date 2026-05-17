package fr.moodcraft.event.listener;

import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.GoldRushClosure;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class GoldRushStopGuard implements Listener {

    private static final String PICKAXE_NAME = GoldRushTask.PICKAXE_NAME;

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase(Locale.ROOT).trim();

        if (!message.equals("/eventstop") && !message.startsWith("/eventstop ")) {
            return;
        }

        if (EventManager.getType() != EventType.RUEE_OR || !EventManager.isRunning()) {
            return;
        }

        event.setCancelled(true);

        for (Player online : Bukkit.getOnlinePlayers()) {
            removePickaxes(online);
        }

        GoldRushClosure.close(event.getPlayer());
    }

    private void removePickaxes(Player player) {
        if (player == null) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            if (!PICKAXE_NAME.equals(meta.getDisplayName())) {
                continue;
            }

            item.setAmount(0);
        }
    }
}
