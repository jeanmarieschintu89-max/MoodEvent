package fr.moodcraft.event.listener;

import fr.moodcraft.event.manager.EventManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventProgressListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!hasChangedBlock(event.getFrom(), event.getTo())) {
            return;
        }

        Player player = event.getPlayer();

        if (EventManager.isAtFinish(player)) {
            EventManager.finishPlayer(player);
            return;
        }

        EventManager.checkSurvivalFloorElimination(player);
    }

    private boolean hasChangedBlock(Location from, Location to) {
        if (to == null || from.getWorld() == null || to.getWorld() == null) {
            return false;
        }
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()
                || !from.getWorld().equals(to.getWorld());
    }
}
