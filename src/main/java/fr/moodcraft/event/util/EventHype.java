package fr.moodcraft.event.util;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class EventHype {

    public static final String HEADER = "§8----- §d§l✦ MOOD EVENT ✦ §8-----";
    public static final String FOOTER = "§8-----------------------------";

    private EventHype() {
    }

    public static void send(Player player, String title, String... lines) {
        if (player == null) return;
        player.sendMessage("");
        player.sendMessage(HEADER);
        player.sendMessage("§e★ §f" + title);
        if (lines != null) for (String line : lines) player.sendMessage(line);
        player.sendMessage(FOOTER);
    }

    public static void broadcast(String title, String... lines) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(HEADER);
        Bukkit.broadcastMessage("§e★ §f" + title);
        if (lines != null) for (String line : lines) Bukkit.broadcastMessage(line);
        Bukkit.broadcastMessage(FOOTER);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.45f, 1.25f);
        }
    }

    public static void joined(Player player, String eventName, int position) {
        send(player,
                "§a§lTu es dans la file !",
                "§e★ §fÉvénement : §e" + eventName,
                "§d➜ §fPosition : §b#" + position,
                "§7Prépare-toi, le départ approche.");
    }

    public static void launch(Player player, String eventName, String objective) {
        send(player,
                "§a§lGO !",
                "§e★ §f" + eventName,
                "§d➜ §f" + objective,
                "§c■ §fJoue fair-play jusqu'au bout.");
    }

    public static void eliminated(Player player, String reason) {
        send(player,
                "§c§lÉlimination !",
                "§c■ §fCause : §e" + reason,
                "§d⌂ §fRetour en zone d'attente.",
                "§7Regarde les survivants, la partie continue.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 0.7f);
    }
}
