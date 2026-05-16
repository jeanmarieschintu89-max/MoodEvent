package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GoldRushTask implements Listener {

    private static final String PICKAXE_NAME = "§6✦ §fPioche Ruée vers l'or §6✦";

    private boolean active;
    private boolean finishedAwaitingStop;
    private boolean autoCloseScheduled;
    private int remaining;
    private final Set<UUID> equipped = new HashSet<>();

    public GoldRushTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    private void tick() {
        if (!EventManager.isRunning() || EventManager.getType() != EventType.RUEE_OR) {
            active = false;
            finishedAwaitingStop = false;
            autoCloseScheduled = false;
            remaining = 0;
            equipped.clear();
            return;
        }

        if (finishedAwaitingStop) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!EventManager.isEventPlayer(player)) continue;
                player.sendActionBar("§6⛏ §fRuée vers l'or terminée §8• §7Clôture automatique");
            }
            return;
        }

        if (!active) {
            active = true;
            autoCloseScheduled = false;
            remaining = Math.max(30, GeneratedGameManager.getGoldRushDurationSeconds());
            startRound();
            return;
        }

        remaining--;
        if (remaining == 60 || remaining == 30 || remaining == 10 || remaining <= 5 && remaining > 0) {
            broadcastTime();
        }

        if (remaining <= 0) {
            finishRound();
        }
    }

    private void startRound() {
        equipped.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            givePickaxe(player);
            equipped.add(player.getUniqueId());
            player.sendTitle("§6Ruée vers l'or", "§fMine un maximum de minerais", 0, 45, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.2f);
            MoodStyle.send(
                    player,
                    MoodStyle.MODULE,
                    MoodStyle.info("Ruée vers l'or lancée."),
                    MoodStyle.detail("Objectif : mine un maximum de minerais."),
                    MoodStyle.detail("Temps : §e" + remaining + " secondes"),
                    MoodStyle.detail("C'est le moment d'en profiter."),
                    MoodStyle.detail("Tu gardes les minerais récoltés."),
                    MoodStyle.detail("La pioche event sera reprise à la fin.")
            );
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.info("Ruée vers l'or lancée."));
        Bukkit.broadcastMessage(MoodStyle.detail("Durée : §e" + remaining + " secondes"));
        Bukkit.broadcastMessage(MoodStyle.detail("Mine un maximum de minerais : c'est le moment d'en profiter."));
        Bukkit.broadcastMessage(MoodStyle.detail("Les minerais minés sont la récompense."));
        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    private void broadcastTime() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            player.sendActionBar("§6⛏ §fRuée vers l'or §8• §e" + remaining + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.55f, 1.15f);
        }
    }

    private void finishRound() {
        int sent = 0;
        Player closer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (closer == null) closer = player;
            removePickaxes(player);
            WaitingRoomManager.teleport(player);
            player.sendTitle("§6Fin", "§fMinerais conservés", 0, 45, 10);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.1f);
            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Ruée vers l'or terminée.",
                    MoodStyle.detail("Vous gardez les minerais récoltés."),
                    MoodStyle.detail("Pioche événement reprise."),
                    MoodStyle.detail("Retour en salle d'attente."),
                    MoodStyle.detail("Clôture automatique en cours.")
            );
            sent++;
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.success("Ruée vers l'or terminée."));
        Bukkit.broadcastMessage(MoodStyle.detail("Joueurs envoyés en salle : §e" + sent));
        Bukkit.broadcastMessage(MoodStyle.detail("Aucune récompense ajoutée : les minerais sont le gain."));
        Bukkit.broadcastMessage(MoodStyle.info("Clôture automatique dans §e3 secondes"));
        Bukkit.broadcastMessage(MoodStyle.FRAME);

        active = false;
        finishedAwaitingStop = true;
        remaining = 0;
        equipped.clear();
        scheduleAutoClose(closer);
    }

    private void scheduleAutoClose(Player closer) {
        if (autoCloseScheduled) return;
        autoCloseScheduled = true;
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!EventManager.isRunning() || EventManager.getType() != EventType.RUEE_OR) return;
            Player actor = closer != null && closer.isOnline() ? closer : findEventPlayer();
            if (actor != null) EventManager.cancelEvent(actor);
        }, 60L);
    }

    private Player findEventPlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (EventManager.isEventPlayer(player)) return player;
        }
        for (Player player : Bukkit.getOnlinePlayers()) return player;
        return null;
    }

    private void givePickaxe(Player player) {
        removePickaxes(player);
        player.getInventory().addItem(createPickaxe());
    }

    public static ItemStack createPickaxe() {
        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(PICKAXE_NAME);
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            pickaxe.setItemMeta(meta);
        }
        return pickaxe;
    }

    private void removePickaxes(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.DIAMOND_PICKAXE) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !PICKAXE_NAME.equals(meta.getDisplayName())) continue;
            item.setAmount(0);
        }
    }
}
