package fr.moodcraft.event;

import fr.moodcraft.event.command.EventAdminCommand;
import fr.moodcraft.event.command.EventCommand;
import fr.moodcraft.event.listener.EventAdminGUIListener;
import fr.moodcraft.event.listener.EventChatListener;
import fr.moodcraft.event.manager.EventManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        EventManager.load();

        EventCommand eventCommand = new EventCommand();
        EventAdminCommand adminCommand = new EventAdminCommand();

        registerCommand("event", eventCommand);

        registerCommand("eventadmin", adminCommand);
        registerCommand("eventgui", adminCommand);
        registerCommand("eventcreate", adminCommand);
        registerCommand("eventdesc", adminCommand);
        registerCommand("eventtype", adminCommand);
        registerCommand("eventset", adminCommand);
        registerCommand("eventopen", adminCommand);
        registerCommand("eventclose", adminCommand);
        registerCommand("eventgo", adminCommand);
        registerCommand("eventstop", adminCommand);
        registerCommand("eventcancel", adminCommand);

        Bukkit.getPluginManager().registerEvents(new EventAdminGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventChatListener(), this);

        getLogger().info("=================================");
        getLogger().info("✦ MoodEvent activé");
        getLogger().info("Centre événementiel chargé");
        getLogger().info("File d'attente événementielle prête");
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        EventManager.save();
        getLogger().info("✦ MoodEvent désactivé");
    }

    private void registerCommand(
            String name,
            org.bukkit.command.CommandExecutor executor
    ) {

        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        }
    }
}
