package fr.moodcraft.event.command;

import fr.moodcraft.event.gui.EventAdminGUI;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.MoodStyle;

import org.bukkit.Bukkit;
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

            case "eventmenu", "eventgui", "eventpanel" -> {
                EventAdminGUI.open(player);
                return true;
            }

            case "eventadmin", "eventaide", "eventhelp", "eventsadmin" -> {
                EventManager.adminHelp(player);
                return true;
            }

            case "eventcreer", "eventcréer", "eventcreate", "eventnew" -> {
                if (args.length == 0) {
                    MoodStyle.errorMessage(player, MoodStyle.MODULE, "Nom d'événement manquant.", MoodStyle.detail("Utilisation : §e/eventcreer <nom>"));
                    return true;
                }
                EventManager.createEvent(player, join(args, 0));
                return true;
            }

            case "eventdescription", "eventdesc" -> {
                if (args.length == 0) {
                    MoodStyle.errorMessage(player, MoodStyle.MODULE, "Description manquante.", MoodStyle.detail("Utilisation : §e/eventdescription <description>"));
                    return true;
                }
                EventManager.setDescription(player, join(args, 0));
                return true;
            }

            case "eventtype", "eventmode", "eventjeu" -> {
                if (args.length == 0) {
                    MoodStyle.errorMessage(player, MoodStyle.MODULE, "Type manquant.", MoodStyle.detail("Types : §ecourse§7, §ejump§7, §elabyrinthe§7, §epvp§7, §equiz§7, §ebuild"));
                    return true;
                }
                EventManager.setType(player, args[0]);
                return true;
            }

            case "eventdepart", "eventdépart", "eventset", "eventspawn", "eventpos" -> {
                EventManager.setLocation(player);
                return true;
            }

            case "eventarrivee", "eventarrivée", "eventsetfinish", "eventfinishset" -> {
                EventManager.setFinishLocation(player);
                return true;
            }

            case "eventsalleattente", "eventsalle", "eventattente", "eventbuildwaiting", "eventgenerersalle", "eventgénérersalle" -> {
                WaitingRoomManager.build(player, args.length == 0 ? "medium" : args[0]);
                return true;
            }

            case "eventrestaurersalle", "eventrestaurerattente", "eventrestorewaiting", "eventclearwaiting" -> {
                WaitingRoomManager.restore(player);
                return true;
            }

            case "eventtpsalle", "eventtpattente", "eventwaitingtp" -> {
                WaitingRoomManager.teleport(player);
                MoodStyle.successMessage(player, MoodStyle.MODULE, "Téléportation en salle d'attente.");
                return true;
            }

            case "eventfinirjoueur", "eventfinishplayer" -> {
                if (args.length == 0) {
                    EventManager.finishPlayer(player);
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    MoodStyle.errorMessage(player, MoodStyle.MODULE, "Joueur introuvable.", MoodStyle.detail("Nom : §e" + args[0]));
                    return true;
                }

                EventManager.finishPlayer(target);
                MoodStyle.successMessage(player, MoodStyle.MODULE, "Arrivée validée manuellement.", MoodStyle.detail("Joueur : §e" + target.getName()));
                return true;
            }

            case "eventouvrir", "eventopen" -> {
                EventManager.openQueue(player);
                return true;
            }

            case "eventfermer", "eventclose" -> {
                EventManager.closeQueue(player);
                return true;
            }

            case "eventlancer", "eventgo", "eventstart" -> {
                EventManager.startEvent(player);
                return true;
            }

            case "eventstop", "eventterminer", "eventend", "eventfinish" -> {
                EventManager.stopEvent(player);
                return true;
            }

            case "eventannuler", "eventcancel" -> {
                EventManager.cancelEvent(player);
                return true;
            }

            default -> {
                EventManager.adminHelp(player);
                return true;
            }
        }
    }

    private String join(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
}
