package fr.moodcraft.event.listener;

import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EventProtectionListener implements Listener {

    @EventHandler
    public void onEnderPearlUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!EventManager.isEventPlayer(player)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENDER_PEARL) {
            return;
        }

        event.setCancelled(true);
        MoodStyle.errorMessage(
                player,
                MoodStyle.MODULE,
                "Ender Pearl interdite pendant l'événement.",
                MoodStyle.detail("Les parcours et la salle d'attente doivent rester équitables.")
        );
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player damager = findDamager(event.getDamager());
        if (damager == null) {
            return;
        }

        if (!EventManager.isEventPlayer(victim) && !EventManager.isEventPlayer(damager)) {
            return;
        }

        if (!EventManager.isNonPvpEventRunning()) {
            return;
        }

        event.setCancelled(true);
        MoodStyle.errorMessage(
                damager,
                MoodStyle.MODULE,
                "PvP désactivé pendant cet événement.",
                MoodStyle.detail("Seul le mode §cCombat PvP §7autorise les dégâts entre joueurs.")
        );
    }

    private Player findDamager(org.bukkit.entity.Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        if (entity instanceof org.bukkit.entity.Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
