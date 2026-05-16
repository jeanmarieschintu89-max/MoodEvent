package fr.moodcraft.event.listener;

import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.EventReturnSafety;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EventDeathGuard implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!EventManager.isEventPlayer(player)) return;
        if (event.getFinalDamage() < player.getHealth()) return;

        event.setCancelled(true);
        player.setHealth(Math.max(1.0, player.getHealth()));
        player.setFallDistance(0f);
        player.setFireTicks(0);

        if (EventReturnSafety.rescue(player)) {
            player.sendTitle("§aRetour sécurisé", "§fTa participation est protégée", 0, 35, 8);
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
        player.sendActionBar("§a✔ §fMort évitée pendant l'événement");
    }
}
