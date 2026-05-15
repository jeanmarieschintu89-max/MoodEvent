package fr.moodcraft.event.listener;

import fr.moodcraft.event.gui.EventAdminGUI;
import fr.moodcraft.event.gui.RewardGUI;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.RewardManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EventAdminGUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = MoodStyle.cleanTitle(event.getView().getTitle());

        if (title.equals("centre evenementiel")) {
            handleAdminClick(event, player);
            return;
        }

        if (title.equals("recompenses event")) {
            handleRewardClick(event, player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = MoodStyle.cleanTitle(event.getView().getTitle());
        if (!title.startsWith("recompense ")) {
            return;
        }

        if (RewardManager.isEditingItems(player)) {
            RewardManager.saveItemEditor(player, event.getInventory());
        }
    }

    private void handleAdminClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        switch (slot) {
            case 10 -> { click(player); EventChatListener.startName(player); }
            case 12 -> { click(player); EventChatListener.startDescription(player); }
            case 14 -> { click(player); EventManager.cycleType(player); EventAdminGUI.open(player); }
            case 16 -> { click(player); EventManager.setLocation(player); EventAdminGUI.open(player); }
            case 18 -> { click(player); EventManager.setFinishLocation(player); EventAdminGUI.open(player); }
            case 20 -> {
                click(player);
                if (WaitingRoomManager.hasRoom()) {
                    WaitingRoomManager.teleport(player);
                } else {
                    WaitingRoomManager.build(player, "medium");
                }
                EventAdminGUI.open(player);
            }
            case 29 -> { click(player); RewardGUI.open(player); }
            case 31 -> {
                click(player);
                if (EventManager.isQueueOpen()) {
                    EventManager.closeQueue(player);
                } else {
                    EventManager.openQueue(player);
                }
                EventAdminGUI.open(player);
            }
            case 33 -> { click(player); player.closeInventory(); EventManager.startEvent(player); }
            case 35 -> { click(player); player.closeInventory(); EventManager.stopEvent(player); }
            case 38 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.85f);
                WaitingRoomManager.restore(player);
                EventAdminGUI.open(player);
            }
            case 42 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.85f);
                player.closeInventory();
                EventManager.cancelEvent(player);
            }
            case 44 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                player.closeInventory();
            }
            default -> {
            }
        }
    }

    private void handleRewardClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        switch (slot) {
            case 11 -> openItems(player, RewardManager.PARTICIPATION);
            case 12 -> editMoney(player, RewardManager.PARTICIPATION);
            case 20 -> openItems(player, 1);
            case 21 -> editMoney(player, 1);
            case 29 -> openItems(player, 2);
            case 30 -> editMoney(player, 2);
            case 38 -> openItems(player, 3);
            case 39 -> editMoney(player, 3);
            case 49 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void openItems(Player player, int place) {
        click(player);
        RewardManager.openItemEditor(player, place);
    }

    private void editMoney(Player player, int place) {
        click(player);
        RewardManager.startMoneyInput(player, place);
    }

    private void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.35f, 1.4f);
    }
}
