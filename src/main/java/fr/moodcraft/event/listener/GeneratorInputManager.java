package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.gui.MiniGameGeneratorGUI;
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

public class GeneratorInputManager implements Listener {

    private static final Map<UUID, GeneratedGameType> WAITING_CUSTOM = new HashMap<>();
    private static final long TIMEOUT_TICKS = 20L * 60L;

    public static void startCustom(Player player, GeneratedGameType type) {
        if (player == null || type == null) {
            return;
        }

        WAITING_CUSTOM.put(player.getUniqueId(), type);
        player.closeInventory();
        MoodStyle.send(
                player,
                MoodStyle.MODULE,
                MoodStyle.info("Écris la taille personnalisée dans le chat."),
                GeneratedGameManager.customRule(type),
                MoodStyle.detail("Type : §e" + type.getDisplayName()),
                MoodStyle.detail("Tape §cannuler §7pour quitter"),
                MoodStyle.detail("Annulation auto dans §e60 secondes")
        );
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);

        Bukkit.getScheduler().runTaskLater(
                Main.getInstance(),
                () -> {
                    GeneratedGameType current = WAITING_CUSTOM.get(player.getUniqueId());
                    if (current == null || current != type) {
                        return;
                    }
                    WAITING_CUSTOM.remove(player.getUniqueId());
                    if (player.isOnline()) {
                        MoodStyle.infoMessage(player, MoodStyle.MODULE, "Saisie personnalisée annulée : temps écoulé.");
                    }
                },
                TIMEOUT_TICKS
        );
    }

    public static boolean isWaiting(Player player) {
        return player != null && WAITING_CUSTOM.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        WAITING_CUSTOM.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GeneratedGameType type = WAITING_CUSTOM.get(player.getUniqueId());
        if (type == null) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> handle(player, type, message));
    }

    private void handle(Player player, GeneratedGameType type, String message) {
        if (message == null || message.equalsIgnoreCase("annuler") || message.equalsIgnoreCase("cancel")) {
            WAITING_CUSTOM.remove(player.getUniqueId());
            MoodStyle.infoMessage(player, MoodStyle.MODULE, "Saisie personnalisée annulée.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            MiniGameGeneratorGUI.openSize(player, type);
            return;
        }

        int value;
        try {
            value = Integer.parseInt(message.trim());
        } catch (NumberFormatException exception) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Nombre invalide.", GeneratedGameManager.customRule(type));
            return;
        }

        String description = GeneratedGameManager.describeCustom(type, value);
        if (description.equals("Personnalisé")) {
            MoodStyle.errorMessage(player, MoodStyle.MODULE, "Taille refusée.", GeneratedGameManager.customRule(type));
            return;
        }

        WAITING_CUSTOM.remove(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.35f);
        MiniGameGeneratorGUI.openConfirmCustom(player, type, value);
    }
}
