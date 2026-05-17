package fr.moodcraft.event.listener;

import fr.moodcraft.event.manager.EventManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EventDeathGuard implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!EventManager.isEventPlayer(player)) return;

        event.setCancelled(true);
        event.setDamage(0.0);
        player.setFallDistance(0f);
        player.setFireTicks(0);
        player.setNoDamageTicks(Math.max(player.getNoDamageTicks(), 20));
    }
}
