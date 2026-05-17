package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class EventHypeAnnouncer {

    private static final int REMINDER_INTERVAL_SECONDS = 120;

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
        Bukkit.broadcastMessage("§d✦ §5RAPPEL EVENT §d✦");
        Bukkit.broadcastMessage("§fÉpreuve : " + type.getDisplayName() + " §8• §7" + shortGoal(type));
        Bukkit.broadcastMessage("§6Top 1 §7» §f" + rewardLine(1));
        Bukkit.broadcastMessage("§aParticipation §7» §f" + participationLine(type));
        Bukkit.broadcastMessage("§e➜ Tape §6/event §epour rejoindre maintenant !");
        Bukkit.broadcastMessage("§7Joueurs en file : §e" + EventManager.getQueueSize());
        Bukkit.broadcastMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        playPing();
    }

    private static String shortGoal(EventType type) {
        return switch (type) {
            case WATER_JUMP -> "saute, arrive, vise le Top 3";
            case LABYRINTHE -> "trouve la sortie avant les autres";
            case SURVIE_ETAGES -> "survis pendant que le sol disparaît";
            case RUEE_OR -> "mine vite et garde tes minerais";
            default -> "joue, gagne, repars récompensé";
        };
    }

    private static String rewardLine(int place) {
        int items = RewardManager.countRewardItems(place);
        double money = RewardManager.getMoney(place);
        String moneyText = money > 0 ? RewardManager.formatMoney(money) : "argent non défini";
        String itemText = items > 0 ? items + " item(s)" : "loot non défini";
        if (money > 0 && items > 0) return moneyText + " + " + itemText;
        if (money > 0) return moneyText;
        if (items > 0) return itemText;
        return "à configurer par le staff";
    }

    private static String participationLine(EventType type) {
        if (type == EventType.RUEE_OR) return "tu gardes les minerais récoltés";
        int items = RewardManager.countRewardItems(RewardManager.PARTICIPATION);
        double money = RewardManager.getMoney(RewardManager.PARTICIPATION);
        if (money <= 0 && items <= 0) return "récompense à configurer";
        String moneyText = money > 0 ? RewardManager.formatMoney(money) : "";
        String itemText = items > 0 ? items + " item(s)" : "";
        if (!moneyText.isBlank() && !itemText.isBlank()) return moneyText + " + " + itemText;
        return moneyText.isBlank() ? itemText : moneyText;
    }

    private static void playPing() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.35f, 1.25f);
        }
    }
}
