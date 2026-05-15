package fr.moodcraft.event.listener;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Set;

public class EventProtectionListener implements Listener {

    private static final Set<String> BLOCKED_COMMANDS = Set.of(
            "/spawn", "/home", "/homes", "/tpa", "/tpahere", "/warp", "/warps", "/rtp", "/tpr"
    );

    @EventHandler
    public void onEnderPearlUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!EventManager.isEventPlayer(player)) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENDER_PEARL) return;
        event.setCancelled(true);
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Ender Pearl interdite pendant l'événement.", MoodStyle.detail("Les parcours et la salle d'attente doivent rester équitables."));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player damager = findDamager(event.getDamager());
        if (damager == null) return;
        if (!EventManager.isEventPlayer(victim) && !EventManager.isEventPlayer(damager)) return;
        if (!EventManager.isNonPvpEventRunning()) return;
        event.setCancelled(true);
        MoodStyle.errorMessage(damager, MoodStyle.MODULE, "PvP désactivé pendant cet événement.", MoodStyle.detail("Seul le mode §cCombat PvP §7autorise les dégâts entre joueurs."));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!GeneratedGameManager.isInsideStructure(event.getBlock().getLocation())) return;

        if (EventManager.getType().usesTimedMining()
                && GeneratedGameManager.getActiveType() == GeneratedGameType.RUEE_OR
                && isGoldRushMineBlock(event.getBlock().getType())) {
            return;
        }

        event.setCancelled(true);
        MoodStyle.errorMessage(event.getPlayer(), MoodStyle.MODULE, "Modification interdite dans la structure générée.", MoodStyle.detail("La zone sera restaurée automatiquement à la fin."));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!GeneratedGameManager.isInsideStructure(event.getBlockPlaced().getLocation())) return;
        event.setCancelled(true);
        MoodStyle.errorMessage(event.getPlayer(), MoodStyle.MODULE, "Pose interdite dans la structure générée.", MoodStyle.detail("Garde le mini-jeu propre et équitable."));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!EventManager.isEventPlayer(player)) return;
        String message = event.getMessage().toLowerCase(Locale.ROOT).trim();
        for (String blocked : BLOCKED_COMMANDS) {
            if (message.equals(blocked) || message.startsWith(blocked + " ")) {
                event.setCancelled(true);
                MoodStyle.errorMessage(player, MoodStyle.MODULE, "Commande bloquée pendant l'événement.", MoodStyle.detail("Commande : §e" + blocked), MoodStyle.detail("Le retour est géré automatiquement à la fin."));
                return;
            }
        }
    }

    private boolean isGoldRushMineBlock(Material material) {
        if (material == null) return false;
        return material == Material.STONE
                || material == Material.DEEPSLATE
                || material == Material.COBBLESTONE
                || material == Material.COBBLED_DEEPSLATE
                || material == Material.TUFF
                || material == Material.COAL_ORE
                || material == Material.COPPER_ORE
                || material == Material.IRON_ORE
                || material == Material.GOLD_ORE
                || material == Material.REDSTONE_ORE
                || material == Material.LAPIS_ORE
                || material == Material.DIAMOND_ORE
                || material == Material.EMERALD_ORE
                || material == Material.DEEPSLATE_COAL_ORE
                || material == Material.DEEPSLATE_COPPER_ORE
                || material == Material.DEEPSLATE_IRON_ORE
                || material == Material.DEEPSLATE_GOLD_ORE
                || material == Material.DEEPSLATE_REDSTONE_ORE
                || material == Material.DEEPSLATE_LAPIS_ORE
                || material == Material.DEEPSLATE_DIAMOND_ORE
                || material == Material.DEEPSLATE_EMERALD_ORE;
    }

    private Player findDamager(org.bukkit.entity.Entity entity) {
        if (entity instanceof Player player) return player;
        if (entity instanceof org.bukkit.entity.Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }
}
