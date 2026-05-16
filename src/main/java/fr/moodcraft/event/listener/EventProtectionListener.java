package fr.moodcraft.event.listener;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Location;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.Set;

public class EventProtectionListener implements Listener {

    private static final String GOLD_RUSH_PICKAXE_NAME = "§6✦ §fPioche Ruée vers l'or §6✦";
    private static final int WAITING_ROOM_PROTECTION_RADIUS = 13;
    private static final int WAITING_ROOM_PROTECTION_HEIGHT = 13;

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
        if (isInsideWaitingRoom(event.getBlock().getLocation())) {
            event.setCancelled(true);
            MoodStyle.errorMessage(event.getPlayer(), MoodStyle.MODULE, "Casse interdite en salle d'attente.", MoodStyle.detail("La zone doit rester propre jusqu'au lancement."));
            return;
        }

        if (!GeneratedGameManager.isInsideStructure(event.getBlock().getLocation())) return;

        if (EventManager.getType().usesTimedMining()
                && GeneratedGameManager.getActiveType() == GeneratedGameType.RUEE_OR
                && isGoldRushMineBlock(event.getBlock().getType())) {

            if (!hasGoldRushPickaxe(event.getPlayer())) {
                event.setCancelled(true);
                MoodStyle.errorMessage(
                        event.getPlayer(),
                        MoodStyle.MODULE,
                        "Pioche événement obligatoire.",
                        MoodStyle.detail("Utilise uniquement la §ePioche Mine en folie§7."),
                        MoodStyle.detail("Les autres pioches sont bloquées pour garder l'épreuve équitable.")
                );
                return;
            }

            return;
        }

        event.setCancelled(true);
        MoodStyle.errorMessage(event.getPlayer(), MoodStyle.MODULE, "Modification interdite dans la structure générée.", MoodStyle.detail("La zone sera restaurée automatiquement à la fin."));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isInsideWaitingRoom(event.getBlockPlaced().getLocation())) {
            event.setCancelled(true);
            MoodStyle.errorMessage(event.getPlayer(), MoodStyle.MODULE, "Pose interdite en salle d'attente.", MoodStyle.detail("La zone doit rester propre jusqu'au lancement."));
            return;
        }

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

    private boolean isInsideWaitingRoom(Location location) {
        Location spawn = WaitingRoomManager.getSpawn();
        if (location == null || location.getWorld() == null || spawn == null || spawn.getWorld() == null) return false;
        if (!location.getWorld().equals(spawn.getWorld())) return false;

        int dx = Math.abs(location.getBlockX() - spawn.getBlockX());
        int dz = Math.abs(location.getBlockZ() - spawn.getBlockZ());
        int dy = location.getBlockY() - (spawn.getBlockY() - 1);

        return dx <= WAITING_ROOM_PROTECTION_RADIUS
                && dz <= WAITING_ROOM_PROTECTION_RADIUS
                && dy >= -1
                && dy <= WAITING_ROOM_PROTECTION_HEIGHT;
    }

    private boolean hasGoldRushPickaxe(Player player) {
        if (player == null) return false;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.DIAMOND_PICKAXE) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && GOLD_RUSH_PICKAXE_NAME.equals(meta.getDisplayName());
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
