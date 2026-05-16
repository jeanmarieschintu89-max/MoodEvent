package fr.moodcraft.event.listener;

import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoldRushPressureReminder implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPressure(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        Material material = event.getClickedBlock().getType();
        if (material != Material.LIGHT_WEIGHTED_PRESSURE_PLATE && material != Material.HEAVY_WEIGHTED_PRESSURE_PLATE) return;
        if (EventManager.getType() != EventType.RUEE_OR) return;
        if (!EventManager.isEventPlayer(player)) return;

        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 2500L) return;
        cooldowns.put(player.getUniqueId(), now);

        int seconds = Math.max(30, GeneratedGameManager.getGoldRushDurationSeconds());
        player.sendTitle("§6§lRUÉE VERS L'OR", "§fTemps de manche : §e" + seconds + "s", 0, 45, 10);
        player.sendMessage("§8----- §6§l✦ RUÉE VERS L'OR ✦ §8-----");
        player.sendMessage("§e★ §fTemps de manche : §e" + seconds + " secondes");
        player.sendMessage("§6⛏ §fMine vite, garde tout ce que tu récoltes.");
        player.sendMessage("§c■ §fLa pioche événement disparaît à la fin.");
        player.sendMessage("§8-----------------------------");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.4f);
    }
}
