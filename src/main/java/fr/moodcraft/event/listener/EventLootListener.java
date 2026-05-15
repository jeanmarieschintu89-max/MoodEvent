package fr.moodcraft.event.listener;

import fr.moodcraft.event.loot.EventLootManager;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventLootListener implements Listener {

    @EventHandler
    public void onLootChest(PlayerInteractEvent event) {
        if (!(event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Chest)) {
            return;
        }

        Player player = event.getPlayer();
        if (EventLootManager.handleClaim(player, event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
