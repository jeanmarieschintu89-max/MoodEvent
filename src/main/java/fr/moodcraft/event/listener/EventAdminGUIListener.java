package fr.moodcraft.event.listener;

import fr.moodcraft.event.generator.EventPackManager;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.generator.GeneratedGameSize;
import fr.moodcraft.event.generator.GeneratedGameType;
import fr.moodcraft.event.gui.EventAdminGUI;
import fr.moodcraft.event.gui.EventAdvancedGUI;
import fr.moodcraft.event.gui.EventLootGUI;
import fr.moodcraft.event.gui.MiniGameGeneratorGUI;
import fr.moodcraft.event.gui.RewardGUI;
import fr.moodcraft.event.gui.WaitingRoomGUI;
import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.loot.LootTier;
import fr.moodcraft.event.manager.EventLogManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.RewardManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.manager.WaitingRoomTheme;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EventAdminGUIListener implements Listener {

    private static final int[] THEME_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38};

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = MoodStyle.cleanTitle(event.getView().getTitle());

        if (title.equals("centre evenementiel")) { handleAdminClick(event, player); return; }
        if (title.equals("mode avance")) { handleAdvancedClick(event, player); return; }
        if (title.equals("recompenses event")) { handleRewardClick(event, player); return; }
        if (title.equals("salle dattente")) { handleWaitingRoomClick(event, player); return; }
        if (title.equals("style salle manuel")) { handleManualWaitingRoomStyleClick(event, player); return; }
        if (title.equals("generateur de mini jeux")) { handleGeneratorMainClick(event, player); return; }
        if (title.equals("style salle attente")) { handleGeneratorStyleClick(event, player); return; }
        if (title.equals("taille pack event")) { handleGeneratorSizeClick(event, player); return; }
        if (title.equals("duree mine en folie")) { handleGoldDurationClick(event, player); return; }
        if (title.equals("confirmation pack event")) { handleGeneratorConfirmClick(event, player); return; }
        if (title.equals("loot mini jeux")) { handleLootClick(event, player); }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = MoodStyle.cleanTitle(event.getView().getTitle());
        if (title.startsWith("recompense ") && RewardManager.isEditingItems(player)) {
            RewardManager.saveItemEditor(player, event.getInventory());
            return;
        }
        if (title.startsWith("loot ") && EventLootManager.isEditingItems(player)) {
            EventLootManager.saveItemEditor(player, event.getInventory());
        }
    }

    private void handleAdminClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;

        switch (slot) {
            case 20 -> { click(player); MiniGameGeneratorGUI.openMain(player); }
            case 22 -> {
                click(player);
                if (EventManager.isQueueOpen()) EventManager.closeQueue(player); else EventManager.openQueue(player);
                EventAdminGUI.open(player);
            }
            case 24 -> {
                click(player);
                EventManager.startEvent(player);
                EventAdminGUI.open(player);
            }
            case 29 -> { click(player); RewardGUI.open(player); }
            case 30 -> { click(player); EventLootGUI.open(player); }
            case 31 -> {
                click(player);
                EventManager.stopEvent(player);
                EventAdminGUI.open(player);
            }
            case 33 -> { click(player); EventAdvancedGUI.open(player); }
            case 38 -> {
                click(player);
                EventManager.joinQueue(player);
                EventAdminGUI.open(player);
            }
            case 40 -> { click(player); player.closeInventory(); }
            default -> { }
        }
    }

    private void handleAdvancedClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;

        switch (slot) {
            case 10 -> { click(player); EventChatListener.startName(player); }
            case 11 -> { click(player); EventManager.cycleType(player); EventAdvancedGUI.open(player); }
            case 12 -> { click(player); EventChatListener.startDescription(player); }
            case 14 -> { click(player); EventManager.setLocation(player); EventAdvancedGUI.open(player); }
            case 15 -> { click(player); EventManager.setFinishLocation(player); EventAdvancedGUI.open(player); }
            case 16 -> { click(player); WaitingRoomGUI.openStyle(player); }
            case 22 -> { no(player); WaitingRoomManager.restore(player); EventAdvancedGUI.open(player); }
            case 31 -> {
                no(player);
                EventManager.cancelEvent(player);
                EventAdminGUI.open(player);
            }
            case 49 -> { click(player); EventAdminGUI.open(player); }
            default -> { }
        }
    }

    private void handleGeneratorMainClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;

        switch (slot) {
            case 10 -> openStyle(player, GeneratedGameType.SURVIE_ETAGES);
            case 12 -> openStyle(player, GeneratedGameType.RUEE_OR);
            case 14 -> openStyle(player, GeneratedGameType.WATER_JUMP);
            case 16 -> openStyle(player, GeneratedGameType.MUR_ESCALADE);
            case 20 -> openStyle(player, GeneratedGameType.PRISON_BREAK);
            case 22 -> openStyle(player, GeneratedGameType.LABYRINTHE);
            case 24 -> openStyle(player, GeneratedGameType.LABYRINTHE_ROND);
            case 33 -> {
                no(player);
                if (WaitingRoomManager.hasRoom()) WaitingRoomManager.restore(player);
                if (GeneratedGameManager.hasStructure()) GeneratedGameManager.restore(player);
                EventLogManager.log(player, "Restauration", "Zone d'attente et structure restaurées depuis le menu");
                MiniGameGeneratorGUI.openMain(player);
            }
            case 49 -> { click(player); EventAdminGUI.open(player); }
            default -> { }
        }
    }

    private void handleGeneratorStyleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;
        GeneratedGameType type = MiniGameGeneratorGUI.getSelectedType(player);
        if (type == null) { MiniGameGeneratorGUI.openMain(player); return; }

        WaitingRoomTheme[] themes = WaitingRoomTheme.values();
        for (int i = 0; i < THEME_SLOTS.length && i < themes.length; i++) {
            if (slot == THEME_SLOTS[i]) {
                click(player);
                WaitingRoomManager.setSelectedStyle(player, themes[i].key());
                MiniGameGeneratorGUI.openSize(player, type);
                return;
            }
        }

        if (slot == 49) { click(player); MiniGameGeneratorGUI.openMain(player); }
    }

    private void handleManualWaitingRoomStyleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;

        WaitingRoomTheme[] themes = WaitingRoomTheme.values();
        for (int i = 0; i < THEME_SLOTS.length && i < themes.length; i++) {
            if (slot == THEME_SLOTS[i]) {
                click(player);
                WaitingRoomManager.setSelectedStyle(player, themes[i].key());
                WaitingRoomGUI.open(player);
                return;
            }
        }

        if (slot == 49) {
            click(player);
            WaitingRoomGUI.open(player);
        }
    }

    private void handleGeneratorSizeClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;
        GeneratedGameType type = MiniGameGeneratorGUI.getSelectedType(player);
        if (type == null) { MiniGameGeneratorGUI.openMain(player); return; }

        switch (slot) {
            case 10 -> openConfirm(player, type, GeneratedGameSize.PETIT);
            case 12 -> openConfirm(player, type, GeneratedGameSize.MOYEN);
            case 14 -> openConfirm(player, type, GeneratedGameSize.GRAND);
            case 16 -> openConfirm(player, type, GeneratedGameSize.GEANT);
            case 31 -> MoodStyle.errorMessage(player, MoodStyle.MODULE, "Taille personnalisée retirée.", MoodStyle.detail("Utilise Petit, Moyen, Grand ou Géant."));
            case 49 -> { click(player); MiniGameGeneratorGUI.openStyle(player, type); }
            default -> { }
        }
    }

    private void handleGoldDurationClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;
        switch (slot) {
            case 10 -> openGoldConfirm(player, 60);
            case 11 -> openGoldConfirm(player, 90);
            case 12 -> openGoldConfirm(player, 120);
            case 14 -> openGoldConfirm(player, 180);
            case 15 -> openGoldConfirm(player, 240);
            case 16 -> openGoldConfirm(player, 300);
            case 22 -> {
                click(player);
                MiniGameGeneratorGUI.openSize(player, GeneratedGameType.RUEE_OR);
            }
            default -> { }
        }
    }

    private void handleGeneratorConfirmClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;
        MiniGameGeneratorGUI.PendingGeneration pending = MiniGameGeneratorGUI.getPending(player);
        if (pending == null) { MiniGameGeneratorGUI.openMain(player); return; }

        switch (slot) {
            case 10 -> {
                click(player);
                if (pending.isCustom()) GeneratedGameManager.generateCustom(player, pending.type(), pending.customValue());
                else if (pending.type() == GeneratedGameType.RUEE_OR && pending.goldDurationSeconds() != null) EventPackManager.generatePack(player, pending.type(), pending.size(), pending.goldDurationSeconds());
                else EventPackManager.generatePack(player, pending.type(), pending.size());
                EventLogManager.log(player, "Pack généré", pending.type().getDisplayName() + " - " + pending.describe() + (pending.goldDurationSeconds() != null ? " - " + pending.goldDurationSeconds() + "s" : "") + " - salle " + WaitingRoomManager.getSelectedTheme(player).displayName());
                MiniGameGeneratorGUI.clearPending(player);
                MiniGameGeneratorGUI.openMain(player);
            }
            case 16 -> {
                click(player);
                if (pending.type() == GeneratedGameType.RUEE_OR) MiniGameGeneratorGUI.openDuration(player, pending.size());
                else MiniGameGeneratorGUI.openSize(player, pending.type());
            }
            case 22 -> {
                no(player);
                MiniGameGeneratorGUI.clearPending(player);
                MiniGameGeneratorGUI.openMain(player);
            }
            default -> { }
        }
    }

    private void handleWaitingRoomClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;

        boolean trainTunnel = WaitingRoomManager.getSelectedTheme(player) == WaitingRoomTheme.TRAIN_TUNNEL;
        if (trainTunnel) {
            switch (slot) {
                case 14 -> buildRoom(player, "train_tunnel");
                case 22 -> { click(player); WaitingRoomGUI.openStyle(player); }
                case 33 -> { click(player); WaitingRoomManager.teleport(player); WaitingRoomGUI.open(player); }
                case 35 -> { no(player); WaitingRoomManager.restore(player); WaitingRoomGUI.open(player); }
                case 49 -> { click(player); EventAdvancedGUI.open(player); }
                default -> { }
            }
            return;
        }

        switch (slot) {
            case 10 -> buildRoom(player, "mini");
            case 12 -> buildRoom(player, "petite");
            case 14 -> buildRoom(player, "moyenne");
            case 16 -> buildRoom(player, "grande");
            case 22 -> { click(player); WaitingRoomGUI.openStyle(player); }
            case 28 -> buildRoom(player, "tresgrande");
            case 30 -> buildRoom(player, "festival");
            case 33 -> { click(player); WaitingRoomManager.teleport(player); WaitingRoomGUI.open(player); }
            case 35 -> { no(player); WaitingRoomManager.restore(player); WaitingRoomGUI.open(player); }
            case 49 -> { click(player); EventAdvancedGUI.open(player); }
            default -> { }
        }
    }

    private void handleLootClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;
        switch (slot) {
            case 11, 12 -> { click(player); EventLootManager.openItemEditor(player, LootTier.COMMUN); }
            case 13 -> { click(player); EventLootManager.startMoneyInput(player, LootTier.COMMUN); }
            case 20, 21 -> { click(player); EventLootManager.openItemEditor(player, LootTier.RARE); }
            case 22 -> { click(player); EventLootManager.startMoneyInput(player, LootTier.RARE); }
            case 29, 30 -> { click(player); EventLootManager.openItemEditor(player, LootTier.EPIQUE); }
            case 31 -> { click(player); EventLootManager.startMoneyInput(player, LootTier.EPIQUE); }
            case 42 -> { no(player); EventLootManager.resetGeneratedClaims(player); EventLootGUI.open(player); }
            case 43 -> { no(player); EventLootManager.resetLootConfig(player); }
            case 49 -> { click(player); EventAdminGUI.open(player); }
            default -> { }
        }
    }

    private void handleRewardClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!top(event, slot)) return;
        switch (slot) {
            case 11 -> openItems(player, RewardManager.PARTICIPATION);
            case 12 -> editMoney(player, RewardManager.PARTICIPATION);
            case 20 -> openItems(player, 1);
            case 21 -> editMoney(player, 1);
            case 29 -> openItems(player, 2);
            case 30 -> editMoney(player, 2);
            case 38 -> openItems(player, 3);
            case 39 -> editMoney(player, 3);
            case 49 -> EventAdminGUI.open(player);
            default -> { }
        }
    }

    private void buildRoom(Player player, String size) {
        click(player);
        WaitingRoomManager.build(player, size);
        WaitingRoomGUI.open(player);
    }

    private void openStyle(Player player, GeneratedGameType type) {
        click(player);
        MiniGameGeneratorGUI.openStyle(player, type);
    }

    private void openConfirm(Player player, GeneratedGameType type, GeneratedGameSize size) {
        click(player);
        MiniGameGeneratorGUI.openConfirm(player, type, size);
    }

    private void openGoldConfirm(Player player, int seconds) {
        click(player);
        MiniGameGeneratorGUI.openConfirmGoldDuration(player, seconds);
    }

    private void openItems(Player player, int place) {
        click(player);
        RewardManager.openItemEditor(player, place);
    }

    private void editMoney(Player player, int place) {
        click(player);
        RewardManager.startMoneyInput(player, place);
    }

    private boolean top(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        return top(event, slot);
    }

    private boolean top(InventoryClickEvent event, int slot) {
        return slot >= 0 && slot < event.getView().getTopInventory().getSize();
    }

    private void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.35f, 1.4f);
    }

    private void no(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.85f);
    }
}
