package fr.moodcraft.event;

import fr.moodcraft.event.command.EventAdminCommand;
import fr.moodcraft.event.command.EventCommand;
import fr.moodcraft.event.hook.VaultHook;
import fr.moodcraft.event.listener.EventAdminGUIListener;
import fr.moodcraft.event.listener.EventChatListener;
import fr.moodcraft.event.listener.EventProgressListener;
import fr.moodcraft.event.listener.EventProtectionListener;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.RewardManager;
import fr.moodcraft.event.manager.WaitingRoomManager;

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

        VaultHook.setup();
        EventManager.load();
        WaitingRoomManager.load();
        RewardManager.load();

        EventCommand eventCommand = new EventCommand();
        EventAdminCommand adminCommand = new EventAdminCommand();

        registerCommand("event", eventCommand);

        registerCommand("eventadmin", adminCommand);
        registerCommand("eventmenu", adminCommand);
        registerCommand("eventcreer", adminCommand);
        registerCommand("eventdescription", adminCommand);
        registerCommand("eventtype", adminCommand);
        registerCommand("eventdepart", adminCommand);
        registerCommand("eventarrivee", adminCommand);
        registerCommand("eventsalleattente", adminCommand);
        registerCommand("eventrestaurersalle", adminCommand);
        registerCommand("eventtpsalle", adminCommand);
        registerCommand("eventfinirjoueur", adminCommand);
        registerCommand("eventouvrir", adminCommand);
        registerCommand("eventfermer", adminCommand);
        registerCommand("eventlancer", adminCommand);
        registerCommand("eventstop", adminCommand);
        registerCommand("eventannuler", adminCommand);

        Bukkit.getPluginManager().registerEvents(new EventAdminGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventProgressListener(), this);

        getLogger().info("=================================");
        getLogger().info("✦ MoodEvent activé");
        getLogger().info("Centre événementiel chargé");
        getLogger().info("Salle d'attente restaurable prête");
        getLogger().info("Récompenses participation et Top 3 prêtes");
        getLogger().info("Protections événementielles actives");
        getLogger().info("Détection d'arrivée active");
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        EventManager.save();
        WaitingRoomManager.save();
        RewardManager.save();
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
