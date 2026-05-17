package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class EventHypeAnnouncer {

    private static final int REMINDER_INTERVAL_SECONDS = 300;

    private static BukkitTask task;
    private static boolean wasQueueOpen;
    private static int reminderSeconds;
    private static String lastEventKey = "";

    private EventHypeAnnouncer() {}

    public static void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), EventHypeAnnouncer::tick, 20L, 20L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        wasQueueOpen = false;
        reminderSeconds = 0;
        lastEventKey = "";
    }

    private static void tick() {
        boolean open = EventManager.isCreated() && EventManager.isQueueOpen() && !EventManager.isRunning();
        String eventKey = EventManager.getName() + "|" + EventManager.getType().name();

        if (!open) {
            wasQueueOpen = false;
            reminderSeconds = 0;
            return;
        }

        if (!wasQueueOpen || !eventKey.equals(lastEventKey)) {
            wasQueueOpen = true;
            reminderSeconds = 0;
            lastEventKey = eventKey;
            return;
        }

        reminderSeconds++;
        if (reminderSeconds % REMINDER_INTERVAL_SECONDS == 0) {
            broadcastReminder();
        }
    }

    private static void broadcastReminder() {
        EventType type = EventManager.getType();
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("§d§l✦ Mood Event ✦");
        Bukkit.broadcastMessage("§e★ §fNouvel événement disponible !");
        Bukkit.broadcastMessage("§d➜ §fÉpreuve : " + type.getDisplayName());
        Bukkit.broadcastMessage("§d➜ §fBut : §f" + shortGoal(type));
        if (type == EventType.RUEE_OR) {
            Bukkit.broadcastMessage("§d➜ §fParticipation : §aTu gardes les minerais récoltés");
        } else {
            Bukkit.broadcastMessage("§d➜ §fTop 1 : §6" + rewardLine(1));
            Bukkit.broadcastMessage("§d➜ §fTop 2 : §e" + rewardLine(2));
            Bukkit.broadcastMessage("§d➜ §fTop 3 : §e" + rewardLine(3));
            Bukkit.broadcastMessage("§d➜ §fParticipation : §a" + participationLine(type));
        }
        Bukkit.broadcastMessage("§bℹ §fRejoins maintenant avec §e/event");
        Bukkit.broadcastMessage("§7Joueurs en file : §e" + EventManager.getQueueSize());
        Bukkit.broadcastMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        playPing();
    }

    private static String shortGoal(EventType type) {
        return switch (type) {
            case WATER_JUMP -> "saute de plateforme en plateforme et atteins l'arrivée";
            case LABYRINTHE -> "trouve la sortie avant les autres";
            case SURVIE_ETAGES -> "reste en vie pendant que le sol disparaît";
            case RUEE_OR -> "mine un maximum de minerais pendant le chrono";
            default -> "participe et vise la victoire";
        };
    }

    private static String rewardLine(int place) {
        int items = RewardManager.countRewardItems(place);
        double money = RewardManager.getMoney(place);
        String moneyText = money > 0 ? RewardManager.formatMoney(money) : "";
        String itemText = items > 0 ? items + " item(s)" : "";
        if (!moneyText.isBlank() && !itemText.isBlank()) return moneyText + " + " + itemText;
        if (!moneyText.isBlank()) return moneyText;
        if (!itemText.isBlank()) return itemText;
        return "à configurer";
    }

    private static String participationLine(EventType type) {
        if (type == EventType.RUEE_OR) return "Tu gardes les minerais récoltés";
        int items = RewardManager.countRewardItems(RewardManager.PARTICIPATION);
        double money = RewardManager.getMoney(RewardManager.PARTICIPATION);
        String moneyText = money > 0 ? RewardManager.formatMoney(money) : "";
        String itemText = items > 0 ? items + " item(s)" : "";
        if (!moneyText.isBlank() && !itemText.isBlank()) return moneyText + " + " + itemText;
        if (!moneyText.isBlank()) return moneyText;
        if (!itemText.isBlank()) return itemText;
        return "à configurer";
    }

    private static void playPing() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.35f, 1.25f);
        }
    }
}
