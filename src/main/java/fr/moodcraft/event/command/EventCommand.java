package fr.moodcraft.event.command;

import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.EventReturnSafety;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

public class EventCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c✖ §fCommande joueur uniquement.");
            return true;
        }

        EventReturnSafety.remember(player);
        EventManager.joinQueue(player);
        return true;
    }
}
