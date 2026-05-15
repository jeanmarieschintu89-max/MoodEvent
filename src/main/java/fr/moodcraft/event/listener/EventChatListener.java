package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.gui.EventAdminGUI;
import fr.moodcraft.event.gui.EventLootGUI;
import fr.moodcraft.event.gui.RewardGUI;
import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.RewardManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventChatListener implements Listener {

    private static final Map<UUID, Mode> WAITING = new HashMap<>();
    private static final long TIMEOUT_TICKS = 20L * 60L;

    public static void startName(Player player) {
        start(player, Mode.NAME);
    }

    public static void startDescription(Player player) {
        start(player, Mode.DESCRIPTION);
    }

    private static void start(Player player, Mode mode) {
        if (player == null || mode == null) return;
        WAITING.put(player.getUniqueId(), mode);
        player.closeInventory();

        if (mode == Mode.NAME) {
            MoodStyle.send(player, MoodStyle.MODULE, MoodStyle.info("Écris le nom de l'événement dans le chat."), MoodStyle.detail("Exemple : §eLabyrinthe du Spawn"), MoodStyle.detail("Minimum : §e3 caractères"), MoodStyle.detail("Tape §cannuler §7pour quitter"), MoodStyle.detail("Annulation auto dans §e60 secondes"));
        } else {
            MoodStyle.send(player, MoodStyle.MODULE, MoodStyle.info("Écris la description de l'événement."), MoodStyle.detail("Elle sera visible aux joueurs avec §e/event"), MoodStyle.detail("Minimum : §e5 caractères"), MoodStyle.detail("Tape §cannuler §7pour quitter"), MoodStyle.detail("Annulation auto dans §e60 secondes"));
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            Mode current = WAITING.get(player.getUniqueId());
            if (current == null || current != mode) return;
            WAITING.remove(player.getUniqueId());
            if (player.isOnline()) MoodStyle.infoMessage(player, MoodStyle.MODULE, "Saisie annulée : temps écoulé.");
        }, TIMEOUT_TICKS);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        WAITING.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (RewardManager.isEditingMoney(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                RewardManager.handleMoneyChat(player, message);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
                RewardGUI.open(player);
            });
            return;
        }

        if (EventLootManager.isEditingMoney(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                EventLootManager.handleMoneyChat(player, message);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
                EventLootGUI.open(player);
            });
            return;
        }

        Mode mode = WAITING.get(player.getUniqueId());
        if (mode == null) return;
        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> handle(player, mode, message));
    }

    private void handle(Player player, Mode mode, String message) {
        if (message.equalsIgnoreCase("annuler") || message.equalsIgnoreCase("cancel")) {
            WAITING.remove(player.getUniqueId());
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Saisie annulée.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            return;
        }

        WAITING.remove(player.getUniqueId());
        if (mode == Mode.NAME) EventManager.createEvent(player, message); else EventManager.setDescription(player, message);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
        EventAdminGUI.open(player);
    }

    private enum Mode {
        NAME,
        DESCRIPTION
    }
}
