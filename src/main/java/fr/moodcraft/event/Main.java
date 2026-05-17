package fr.moodcraft.event;

import fr.moodcraft.event.command.EventAdminCommand;
import fr.moodcraft.event.command.EventCommand;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.hook.VaultHook;
import fr.moodcraft.event.listener.EventAdminGUIListener;
import fr.moodcraft.event.listener.EventChatListener;
import fr.moodcraft.event.listener.EventDeathGuard;
import fr.moodcraft.event.listener.EventLootListener;
import fr.moodcraft.event.listener.EventMobGuard;
import fr.moodcraft.event.listener.EventProgressListener;
import fr.moodcraft.event.listener.EventProtectionListener;
import fr.moodcraft.event.listener.GeneratorInputManager;
import fr.moodcraft.event.listener.GoldRushInventoryGuard;
import fr.moodcraft.event.listener.GoldRushPressureReminder;
import fr.moodcraft.event.listener.GoldRushStopGuard;
import fr.moodcraft.event.listener.GoldRushTask;
import fr.moodcraft.event.listener.PrisonEscapeListener;
import fr.moodcraft.event.listener.SurvivalFloorTask;
import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.manager.EventAutoStartTask;
import fr.moodcraft.event.manager.EventHypeAnnouncer;
import fr.moodcraft.event.manager.EventLaunchBufferManager;
import fr.moodcraft.event.manager.EventLogManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.EventReturnSafety;
import fr.moodcraft.event.manager.EventSecurityManager;
import fr.moodcraft.event.manager.RewardManager;
import fr.moodcraft.event.manager.SurvivalFloorLagGuard;
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
        EventLootManager.load();
        EventLogManager.load();
        EventSecurityManager.load();
        EventReturnSafety.start();
        EventAutoStartTask.start();
        EventHypeAnnouncer.start();
        EventLaunchBufferManager.start();
        SurvivalFloorLagGuard.start();

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
        Bukkit.getPluginManager().registerEvents(new EventMobGuard(), this);
        Bukkit.getPluginManager().registerEvents(new EventProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventProgressListener(), this);
        Bukkit.getPluginManager().registerEvents(new PrisonEscapeListener(), this);
        Bukkit.getPluginManager().registerEvents(new EventDeathGuard(), this);
        Bukkit.getPluginManager().registerEvents(new SurvivalFloorTask(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushTask(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushPressureReminder(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushStopGuard(), this);
        Bukkit.getPluginManager().registerEvents(new GoldRushInventoryGuard(), this);

        getLogger().info("MoodEvent active : Mine en folie, Tour Infernale, Water Jump et Prison Escape.");
    }

    @Override
    public void onDisable() {
        EventHypeAnnouncer.stop();
        EventManager.save();
        WaitingRoomManager.save();
        RewardManager.save();
        GeneratedGameManager.save();
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
