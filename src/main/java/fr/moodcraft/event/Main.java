package fr.moodcraft.event;

import fr.moodcraft.event.command.EventAdminCommand;
import fr.moodcraft.event.command.EventCommand;
import fr.moodcraft.event.generator.EventGiveStructureManager;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.hook.VaultHook;
import fr.moodcraft.event.listener.EventAdminGUIListener;
import fr.moodcraft.event.listener.EventChatListener;
import fr.moodcraft.event.listener.EventGiveRestoreGuard;
import fr.moodcraft.event.listener.EventLootListener;
import fr.moodcraft.event.listener.EventProgressListener;
import fr.moodcraft.event.listener.EventProtectionListener;
import fr.moodcraft.event.listener.GeneratorInputManager;
import fr.moodcraft.event.listener.GoldRushInventoryGuard;
import fr.moodcraft.event.listener.GoldRushStopGuard;
import fr.moodcraft.event.listener.GoldRushTask;
import fr.moodcraft.event.listener.SurvivalFloorTask;
import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.manager.EventLogManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.EventSecurityManager;
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
        GeneratedGameManager.load();
        EventGiveStructureManager.load();
        EventLootManager.load();
        EventLogManager.load();
        EventSecurityManager.load();

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
        Bukkit.getPluginManager().registerEvents(new GeneratorInputManager(), this);
        Bukkit.getPluginManager().registerEvents(new EventLootListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventProgressListener(), this);
        Bukkit.getPluginManager().registerEvents(new SurvivalFloorTask(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushTask(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushStopGuard(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushInventoryGuard(), this);
        Bukkit.getPluginManager().registerEvents(new EventGiveRestoreGuard(), this);

        getLogger().info("MoodEvent active en mode securite TPS.");
    }

    @Override
    public void onDisable() {
        EventManager.save();
        WaitingRoomManager.save();
        RewardManager.save();
        GeneratedGameManager.save();
        EventGiveStructureManager.save();
        EventLootManager.save();
        EventLogManager.save();
        EventSecurityManager.save();
        getLogger().info("MoodEvent desactive.");
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        }
    }
}
