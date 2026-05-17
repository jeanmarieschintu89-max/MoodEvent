package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.model.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class EventHypeAnnouncer {

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
            broadcastOpen();
            return;
        }

        reminderSeconds++;
        if (reminderSeconds % 60 == 0) {
            broadcastReminder();
        }
    }

    private static void broadcastOpen() {
        EventType type = EventManager.getType();
        String eventName = EventManager.getName();
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("§d✦ §5MOOD EVENT §d✦");
        Bukkit.broadcastMessage("§aUn événement vient d'ouvrir !");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§fÉpreuve : " + type.getDisplayName() + " §8• §e" + eventName);
        Bukkit.broadcastMessage(goalLine(type));
        Bukkit.broadcastMessage(objectiveLine(type));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6Récompenses à gagner :");
        Bukkit.broadcastMessage("§e🥇 Top 1 §7» §f" + rewardLine(1));
        Bukkit.broadcastMessage("§e🥈 Top 2 §7» §f" + rewardLine(2));
        Bukkit.broadcastMessage("§e🥉 Top 3 §7» §f" + rewardLine(3));
        Bukkit.broadcastMessage("§aParticipation §7» §f" + participationLine(type));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§e➜ Tape §6/event §epour rejoindre la file !");
        Bukkit.broadcastMessage("§cDépart bientôt, ne rate pas ta place.");
        Bukkit.broadcastMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        playPing();
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
    }

    private static String goalLine(EventType type) {
        return switch (type) {
            case WATER_JUMP -> "§7Saute de plateforme en plateforme au-dessus de l'eau.";
            case LABYRINTHE -> "§7Trouve la sortie avant les autres joueurs.";
            case SURVIE_ETAGES -> "§7Survis dans la tour pendant que les étages disparaissent.";
            case RUEE_OR -> "§7Mine un maximum de minerais pendant le chrono.";
            default -> "§7Participe à l'épreuve et vise la victoire.";
        };
    }

    private static String objectiveLine(EventType type) {
        return switch (type) {
            case WATER_JUMP -> "§7Si tu tombes, retour au départ. Les plus rapides gagnent.";
            case LABYRINTHE -> "§7Les 3 premiers à atteindre l'arrivée gagnent le podium.";
            case SURVIE_ETAGES -> "§7Reste debout le plus longtemps possible pour viser le Top 3.";
            case RUEE_OR -> "§7Tu gardes les minerais récoltés dans la mine spéciale.";
            default -> "§7Objectif : faire mieux que les autres et repartir récompensé.";
        };
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
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.45f, 1.25f);
        }
    }
}
