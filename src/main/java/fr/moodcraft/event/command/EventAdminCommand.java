package fr.moodcraft.event.command;

import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.util.MoodStyle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

import java.util.Arrays;

public class EventAdminCommand implements CommandExecutor {

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

        String cmd = command.getName().toLowerCase();

        switch (cmd) {

            case "eventadmin", "eventhelp", "eventsadmin" -> {
                EventManager.adminHelp(player);
                return true;
            }

            case "eventcreate", "eventnew", "eventcreer", "eventcréer" -> {
                if (args.length == 0) {
                    MoodStyle.errorMessage(
                            player,
                            MoodStyle.MODULE,
                            "Nom d'événement manquant.",
                            MoodStyle.detail("Utilisation : §e/eventcreate <nom>")
                    );
                    return true;
                }

                EventManager.createEvent(player, join(args, 0));
                return true;
            }

            case "eventdesc" -> {
                if (args.length == 0) {
                    MoodStyle.errorMessage(
                            player,
                            MoodStyle.MODULE,
                            "Description manquante.",
                            MoodStyle.detail("Utilisation : §e/eventdesc <description>")
                    );
                    return true;
                }

                EventManager.setDescription(player, join(args, 0));
                return true;
            }

            case "eventtype" -> {
                if (args.length == 0) {
                    MoodStyle.errorMessage(
                            player,
                            MoodStyle.MODULE,
                            "Type manquant.",
                            MoodStyle.detail("Types : §emini-jeu§7, §eactivité§7, §epvp§7, §ebuild§7, §eautre")
                    );
                    return true;
                }

                EventManager.setType(player, args[0]);
                return true;
            }

            case "eventset", "eventspawn", "eventpos" -> {
                EventManager.setLocation(player);
                return true;
            }

            case "eventopen" -> {
                EventManager.openQueue(player);
                return true;
            }

            case "eventclose" -> {
                EventManager.closeQueue(player);
                return true;
            }

            case "eventgo", "eventstart" -> {
                EventManager.startEvent(player);
                return true;
            }

            case "eventstop", "eventend", "eventfinish" -> {
                EventManager.stopEvent(player);
                return true;
            }

            case "eventcancel" -> {
                EventManager.cancelEvent(player);
                return true;
            }

            default -> {
                EventManager.adminHelp(player);
                return true;
            }
        }
    }

    private String join(
            String[] args,
            int start
    ) {
        return String.join(
                " ",
                Arrays.copyOfRange(args, start, args.length)
        );
    }
}
