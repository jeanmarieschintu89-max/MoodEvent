package fr.moodcraft.event.manager;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class GoldRushClosure {

    private GoldRushClosure() {
    }

    public static void close(Player actor) {
        if (EventManager.getType() != EventType.RUEE_OR) return;

        int returned = returnPlayers();
        boolean restoredRoom = WaitingRoomManager.hasRoom();
        boolean restoredGenerated = GeneratedGameManager.hasStructure();

        if (restoredRoom && actor != null) WaitingRoomManager.restore(actor);
        if (restoredGenerated && actor != null) GeneratedGameManager.restore(actor);

        clearEventManagerRuntime();
        EventManager.save();

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.success("Mine en folie clôturée."));
        Bukkit.broadcastMessage(MoodStyle.detail("Joueurs renvoyés : §e" + returned));
        Bukkit.broadcastMessage(MoodStyle.detail("Les minerais restent aux joueurs."));
        Bukkit.broadcastMessage(MoodStyle.detail("La zone a été restaurée."));
        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    @SuppressWarnings("unchecked")
    private static int returnPlayers() {
        int returned = 0;
        try {
            Field returnField = EventManager.class.getDeclaredField("returnLocations");
            returnField.setAccessible(true);
            Map<UUID, Location> returnLocations = (Map<UUID, Location>) returnField.get(null);
            for (Map.Entry<UUID, Location> entry : returnLocations.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                Location location = entry.getValue();
                if (player == null || !player.isOnline() || location == null || location.getWorld() == null) continue;
                player.teleport(location);
                player.sendTitle("§aRetour", "§fMerci d'avoir participé", 0, 35, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
                returned++;
            }
        } catch (ReflectiveOperationException exception) {
            Main.getInstance().getLogger().warning("GoldRushClosure return failed: " + exception.getMessage());
        }
        return returned;
    }

    @SuppressWarnings("unchecked")
    private static void clearEventManagerRuntime() {
        try {
            set("name", "");
            set("description", "");
            set("type", EventType.CUSTOM);
            set("startLocation", null);
            set("finishLocation", null);
            set("queueOpen", false);
            set("running", false);
            set("autoClosing", false);
            clearCollection("queue");
            clearCollection("participants");
            clearCollection("eliminated");
            clearCollection("finishedPlayers");
            clearCollection("eliminationOrder");
            clearCollection("finalRanking");
            clearMap("returnLocations");
        } catch (ReflectiveOperationException exception) {
            Main.getInstance().getLogger().warning("GoldRushClosure reset failed: " + exception.getMessage());
        }
    }

    private static void set(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = EventManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static void clearCollection(String fieldName) throws ReflectiveOperationException {
        Field field = EventManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(null);
        if (value instanceof Collection<?> collection) collection.clear();
    }

    private static void clearMap(String fieldName) throws ReflectiveOperationException {
        Field field = EventManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(null);
        if (value instanceof Map<?, ?> map) map.clear();
    }
}
