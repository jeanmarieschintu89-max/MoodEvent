package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.GoldRushClosure;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GoldRushTask implements Listener {

    public static final String PICKAXE_NAME = "§6✦ §fPioche Mine en folie §6✦";

    private static boolean roundActive;

    private boolean active;
    private boolean finishedAwaitingStop;
    private boolean autoCloseScheduled;
    private int remaining;
    private final Set<UUID> equipped = new HashSet<>();
    private final Set<UUID> nightVisionPlayers = new HashSet<>();

    public GoldRushTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    public static boolean isRoundActive() {
        return roundActive;
    }

    public static void removeEventPickaxes(Player player) {
        if (player == null) return;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.DIAMOND_PICKAXE) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !PICKAXE_NAME.equals(meta.getDisplayName())) continue;
            item.setAmount(0);
        }
    }

    private void tick() {
        if (!EventManager.isRunning() || EventManager.getType() != EventType.RUEE_OR) {
            active = false;
            finishedAwaitingStop = false;
            autoCloseScheduled = false;
            roundActive = false;
            remaining = 0;
            equipped.clear();
            cleanupAllOnlinePickaxes();
            clearNightVision();
            return;
        }

        applyNightVision();

        if (finishedAwaitingStop) {
            roundActive = false;
            cleanupAllOnlinePickaxes();
            clearNightVision();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!EventManager.isEventPlayer(player)) continue;
                player.sendActionBar("§6⛏ §fMine en folie terminée §8• §7Retour imminent");
            }
            return;
        }

        if (!active) {
            active = true;
            autoCloseScheduled = false;
            roundActive = true;
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
            giveNightVision(player);
            equipped.add(player.getUniqueId());
            player.sendTitle("§6§lMINE EN FOLIE", "§fMine vite, garde tout !", 0, 45, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.2f);
            MoodStyle.send(
                    player,
                    MoodStyle.MODULE,
                    MoodStyle.info("Mine en folie lancée."),
                    MoodStyle.detail("Mine un maximum de minerais."),
                    MoodStyle.detail("Tu gardes tout ce que tu récoltes."),
                    MoodStyle.detail("Une pioche spéciale t'est donnée.")
            );
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.hype("Mine en folie lancée !"));
        Bukkit.broadcastMessage(MoodStyle.detail("Mine un maximum de minerais pendant le chrono."));
        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    private void broadcastTime() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            giveNightVision(player);
            player.sendActionBar("§6⛏ §fMine en folie §8• §e" + remaining + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.55f, 1.15f);
        }
    }

    private void finishRound() {
        Player closer = null;
        roundActive = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (closer == null) closer = player;
            removeEventPickaxes(player);
            removeNightVision(player);
            player.sendTitle("§6§lFIN DE MANCHE", "§fMinerais conservés", 0, 45, 10);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.1f);
            MoodStyle.send(
                    player,
                    MoodStyle.MODULE,
                    MoodStyle.success("Mine en folie terminée !"),
                    MoodStyle.detail("Tu gardes les minerais récoltés."),
                    MoodStyle.info("Merci d'avoir participé.")
            );
        }

        active = false;
        finishedAwaitingStop = true;
        remaining = 0;
        equipped.clear();
        cleanupAllOnlinePickaxes();
        clearNightVision();
        scheduleAutoClose(closer);
    }

    private void scheduleAutoClose(Player closer) {
        if (autoCloseScheduled) return;
        autoCloseScheduled = true;
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            cleanupAllOnlinePickaxes();
            clearNightVision();
            if (!EventManager.isRunning() || EventManager.getType() != EventType.RUEE_OR) return;
            Player actor = closer != null && closer.isOnline() ? closer : findEventPlayer();
            if (actor != null) GoldRushClosure.close(actor);
            cleanupAllOnlinePickaxes();
            clearNightVision();
        }, 60L);
    }

    private Player findEventPlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (EventManager.isEventPlayer(player)) return player;
        }
        for (Player player : Bukkit.getOnlinePlayers()) return player;
        return null;
    }

    private void cleanupAllOnlinePickaxes() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeEventPickaxes(player);
        }
    }

    private void applyNightVision() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            giveNightVision(player);
        }
    }

    private void giveNightVision(Player player) {
        if (player == null) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 18, 0, true, false, true));
        nightVisionPlayers.add(player.getUniqueId());
    }

    private void removeNightVision(Player player) {
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        nightVisionPlayers.remove(player.getUniqueId());
    }

    private void clearNightVision() {
        for (UUID uuid : new HashSet<>(nightVisionPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        nightVisionPlayers.clear();
    }

    private void givePickaxe(Player player) {
        removeEventPickaxes(player);
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
}
