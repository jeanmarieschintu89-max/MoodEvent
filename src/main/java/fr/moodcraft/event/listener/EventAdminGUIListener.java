package fr.moodcraft.event.listener;

import fr.moodcraft.event.gui.EventAdminGUI;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.util.MoodStyle;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EventAdminGUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(EventAdminGUI.TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        switch (slot) {

            case 10 -> {
                click(player);
                EventChatListener.startName(player);
            }

            case 12 -> {
                click(player);
                EventChatListener.startDescription(player);
            }

            case 14 -> {
                click(player);
                EventManager.cycleType(player);
                EventAdminGUI.open(player);
            }

            case 16 -> {
                click(player);
                EventManager.setLocation(player);
                EventAdminGUI.open(player);
            }

            case 20 -> {
                click(player);
                if (EventManager.isQueueOpen()) {
                    EventManager.closeQueue(player);
                } else {
                    EventManager.openQueue(player);
                }
                EventAdminGUI.open(player);
            }

            case 22 -> {
                click(player);
                player.closeInventory();
                EventManager.startEvent(player);
            }

            case 24 -> {
                click(player);
                player.closeInventory();
                EventManager.stopEvent(player);
            }

            case 26 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.85f);
                player.closeInventory();
                EventManager.cancelEvent(player);
            }

            case 40 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                player.closeInventory();
            }

            default -> {
            }
        }
    }

    private void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.35f, 1.4f);
    }
}
