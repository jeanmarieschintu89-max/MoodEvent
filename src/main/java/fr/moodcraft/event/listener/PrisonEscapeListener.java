package fr.moodcraft.event.listener;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PrisonEscapeListener implements Listener {

    private static final int OPEN_RADIUS = 7;
    private static final int OPEN_HEIGHT = 4;

    @EventHandler
    public void onPuzzleInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (EventManager.getType() != EventType.PRISON_BREAK) return;
        if (!EventManager.isParticipant(player)) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        Material type = clicked.getType();
        if (type != Material.STONE_BUTTON && type != Material.LEVER) return;
        if (!GeneratedGameManager.isInsideStructure(clicked.getLocation())) return;

        int opened = openNearbyGate(clicked.getLocation());
        if (opened <= 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f);
            player.sendActionBar("§c✖ §fRien ne bouge... mauvais mécanisme ?");
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 0.9f, 1.1f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Mécanisme activé.",
                MoodStyle.detail("Une grille s'ouvre plus loin."),
                MoodStyle.info("Continue l'évasion, cherche la sortie rouge.")
        );
    }

    private int openNearbyGate(Location center) {
        World world = center.getWorld();
        if (world == null) return 0;
        int opened = 0;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - OPEN_RADIUS; x <= cx + OPEN_RADIUS; x++) {
            for (int y = cy - 1; y <= cy + OPEN_HEIGHT; y++) {
                for (int z = cz - OPEN_RADIUS; z <= cz + OPEN_RADIUS; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.IRON_BARS) continue;
                    if (!GeneratedGameManager.isInsideStructure(block.getLocation())) continue;
                    block.setType(Material.AIR, false);
                    opened++;
                }
            }
        }

        return opened;
    }
}
