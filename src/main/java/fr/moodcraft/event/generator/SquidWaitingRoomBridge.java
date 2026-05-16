package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.manager.WaitingRoomManager;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;

public final class SquidWaitingRoomBridge {

    private SquidWaitingRoomBridge() {
    }

    public static void registerDormitory(Location location) {
        if (location == null || location.getWorld() == null) return;
        try {
            Field spawnField = WaitingRoomManager.class.getDeclaredField("spawn");
            Field activeField = WaitingRoomManager.class.getDeclaredField("active");
            Field configField = WaitingRoomManager.class.getDeclaredField("config");
            spawnField.setAccessible(true);
            activeField.setAccessible(true);
            configField.setAccessible(true);

            FileConfiguration config = (FileConfiguration) configField.get(null);
            if (config == null) {
                WaitingRoomManager.load();
                config = (FileConfiguration) configField.get(null);
            }
            if (config == null) return;

            spawnField.set(null, location.clone());
            activeField.setBoolean(null, true);
            config.set("active", true);
            config.set("external", true);
            config.set("external-name", SquidPackManager.GAME_NAME + " Dortoir");
            config.set("backup", null);
            config.createSection("backup.blocks");
            config.set("spawn.world", location.getWorld().getName());
            config.set("spawn.x", location.getX());
            config.set("spawn.y", location.getY());
            config.set("spawn.z", location.getZ());
            config.set("spawn.yaw", location.getYaw());
            config.set("spawn.pitch", location.getPitch());
            WaitingRoomManager.save();
        } catch (ReflectiveOperationException exception) {
            Main.getInstance().getLogger().warning("Impossible d'enregistrer le dortoir Squid comme zone d'attente: " + exception.getMessage());
        }
    }
}
