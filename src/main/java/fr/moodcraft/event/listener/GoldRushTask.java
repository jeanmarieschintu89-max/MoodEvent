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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GoldRushTask implements Listener {

    private static final String PICKAXE_NAME = "§6✦ §fPioche Ruée vers l'or §6✦";

    private boolean active;
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
            remaining = 0;
            equipped.clear();
            return;
        }

        if (!active) {
            active = true;
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
                    MoodStyle.detail("Tu gardes les minerais récoltés."),
                    MoodStyle.detail("La pioche event sera reprise à la fin.")
            );
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.info("Ruée vers l'or lancée."));
        Bukkit.broadcastMessage(MoodStyle.detail("Durée : §e" + remaining + " secondes"));
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
        int returned = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
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
                    MoodStyle.detail("Retour en salle d'attente.")
            );
            returned++;
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.success("Ruée vers l'or terminée."));
        Bukkit.broadcastMessage(MoodStyle.detail("Joueurs renvoyés en salle : §e" + returned));
        Bukkit.broadcastMessage(MoodStyle.detail("Aucune récompense ajoutée : les minerais sont le gain."));
        Bukkit.broadcastMessage(MoodStyle.info("Le staff peut restaurer avec §e/eventstop §fou le menu."));
        Bukkit.broadcastMessage(MoodStyle.FRAME);

        clearEventRuntimeOnly();
        active = false;
        remaining = 0;
        equipped.clear();
    }

    private void givePickaxe(Player player) {
        removePickaxes(player);
        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(PICKAXE_NAME);
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            pickaxe.setItemMeta(meta);
        }
        player.getInventory().addItem(pickaxe);
    }

    private void removePickaxes(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.DIAMOND_PICKAXE) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !PICKAXE_NAME.equals(meta.getDisplayName())) continue;
            item.setAmount(0);
        }
    }

    @SuppressWarnings("unchecked")
    private void clearEventRuntimeOnly() {
        try {
            setBoolean("running", false);
            setBoolean("queueOpen", false);
            clearCollection("queue");
            clearCollection("participants");
            clearCollection("finishedPlayers");
            clearCollection("ranking");
            clearCollection("survivalEliminated");
        } catch (ReflectiveOperationException exception) {
            Main.getInstance().getLogger().warning("Impossible de nettoyer la Ruée vers l'or: " + exception.getMessage());
        }
    }

    private void setBoolean(String fieldName, boolean value) throws ReflectiveOperationException {
        Field field = EventManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setBoolean(null, value);
    }

    private void clearCollection(String fieldName) throws ReflectiveOperationException {
        Field field = EventManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object object = field.get(null);
        if (object instanceof java.util.Collection<?> collection) {
            collection.clear();
        }
    }
}
