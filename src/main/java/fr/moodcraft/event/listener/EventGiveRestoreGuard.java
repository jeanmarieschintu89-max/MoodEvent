package fr.moodcraft.event.listener;

import fr.moodcraft.event.generator.EventGiveStructureManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EventGiveRestoreGuard implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase().trim();
        if (!message.equals("/eventstop")
                && !message.startsWith("/eventstop ")
                && !message.equals("/eventannuler")
                && !message.startsWith("/eventannuler ")
                && !message.equals("/eventrestaurersalle")
                && !message.startsWith("/eventrestaurersalle ")) {
            return;
        }

        if (!EventGiveStructureManager.hasStructure()) {
            return;
        }

        EventGiveStructureManager.restore(event.getPlayer());
    }
}
