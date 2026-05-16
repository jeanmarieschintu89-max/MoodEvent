package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.SquidGameCinematic;
import fr.moodcraft.event.generator.SquidPackManager;
import fr.moodcraft.event.manager.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SquidPackTask implements Listener {

    private static final int RED_REACTION_SECONDS = 1;
    private static final int DORMITORY_DELAY_SECONDS = 10;

    private int tick;
    private int redReaction;
    private int initialParticipants;
    private boolean green = true;
    private boolean runtimeStarted;
    private boolean finished;
    private final Map<UUID, Location> lastRedPositions = new HashMap<>();
    private final Set<UUID> qualified = new HashSet<>();
    private final Set<UUID> eliminated = new HashSet<>();
    private final Set<UUID> falling = new HashSet<>();

    public SquidPackTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                runPackTick();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    private void runPackTick() {
        if (!SquidPackManager.hasPack() || !EventManager.isRunning()) {
            resetRuntime();
            return;
        }
        if (EventManager.getParticipantSize() <= 0) {
            tick = 0;
            green = true;
            return;
        }
        if (!runtimeStarted) {
            startRedGreen();
            return;
        }
        switch (SquidPackManager.getStage()) {
            case "WAITING" -> startRedGreen();
            case "RED_GREEN" -> redGreenTick();
            case "DORMITORY_BRIDGE" -> dormitoryBeforeBridgeTick();
            case "GLASS_BRIDGE" -> glassBridgeTick();
            default -> { }
        }
    }

    private void startRedGreen() {
        tick = 0;
        redReaction = 0;
        initialParticipants = Math.max(1, EventManager.getParticipantSize());
        green = true;
        runtimeStarted = true;
        finished = false;
        lastRedPositions.clear();
        qualified.clear();
        eliminated.clear();
        falling.clear();
        SquidPackManager.setStage("RED_GREEN");
        SquidGameCinematic.updateDollAndLights(true);

        Location start = SquidPackManager.location("start");
        if (start != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!EventManager.isParticipant(player)) continue;
                player.teleport(start);
                player.sendTitle("§a§lFEU VERT", "§fLa poupée regarde ailleurs !", 0, 45, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.6f);
                player.sendMessage("§8----- §c§l✦ SQUID MOOD GAME ✦ §8-----");
                player.sendMessage("§a▶ §fFEU VERT : §acours !");
                player.sendMessage("§c■ §fFEU ROUGE : §cstop net quand la poupée se retourne.");
                player.sendMessage("§e★ §fObjectif : §eatteindre la ligne rouge.");
                player.sendMessage("§8-----------------------------");
            }
        }
        SquidGameCinematic.flashyBroadcast("§a§lFEU VERT", "§fLa poupée est tournée. §aCours !");
    }

    private void redGreenTick() {
        tick++;
        FileConfiguration config = SquidPackManager.config();
        int finishX = config.getInt("red-green.finish-x");

        if (tick % 5 == 0) {
            green = !green;
            SquidGameCinematic.updateDollAndLights(green);
            if (!green) {
                redReaction = RED_REACTION_SECONDS;
                lastRedPositions.clear();
            }
            forEachAlive(player -> {
                player.sendTitle(green ? "§a§lFEU VERT" : "§c§lFEU ROUGE", green ? "§fGO GO GO !" : "§fLa poupée tourne...", 0, 25, 5);
                player.playSound(player.getLocation(), green ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, green ? 1.5f : 0.6f);
            });
        }

        if (!green && redReaction > 0) {
            redReaction--;
            if (redReaction == 0) {
                lastRedPositions.clear();
                forEachAlive(player -> lastRedPositions.put(player.getUniqueId(), player.getLocation().clone()));
                forEachAlive(player -> player.sendTitle("§c§lSTOP !", "§fPlus un mouvement", 0, 20, 5));
            }
            return;
        }

        forEachAlive(player -> {
            if (!green) {
                Location red = lastRedPositions.get(player.getUniqueId());
                if (hasMoved(red, player.getLocation())) {
                    eliminate(player, "Mouvement pendant Feu Rouge");
                    return;
                }
            }
            if (player.getLocation().getBlockX() >= finishX) qualifyRedGreen(player);
        });

        if (tryFinishLastSurvivor()) return;
        if (tick >= 90) {
            if (qualified.isEmpty()) finishLastAliveOrNoWinner("Temps écoulé : aucun joueur qualifié.");
            else startDormitoryBeforeBridge();
            return;
        }
        if (countAliveNotQualified() <= 0) {
            if (qualified.isEmpty()) finishLastAliveOrNoWinner("Tous les joueurs non qualifiés ont été éliminés.");
            else startDormitoryBeforeBridge();
        }
    }

    private void qualifyRedGreen(Player player) {
        if (!qualified.add(player.getUniqueId())) return;
        SquidGameCinematic.teleportDormitory(player);
        player.sendTitle("§e§lQUALIFIÉ !", "§dRetour dortoir", 0, 45, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.25f);
        player.sendMessage("§8----- §e§l★ QUALIFICATION ★ §8-----");
        player.sendMessage("§a✔ §fTu es qualifié pour le §bPont de Verre§f.");
        player.sendMessage("§d⌂ §fRetour au dortoir : briefing du prochain jeu.");
        player.sendMessage("§8-----------------------------");
    }

    private void startDormitoryBeforeBridge() {
        tick = 0;
        SquidPackManager.setStage("DORMITORY_BRIDGE");
        for (UUID uuid : new HashSet<>(qualified)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || eliminated.contains(uuid)) continue;
            SquidGameCinematic.teleportDormitory(player);
            player.sendTitle("§d§lDORTOIR", "§fProchaine épreuve dans " + DORMITORY_DELAY_SECONDS + "s", 0, 60, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.25f);
        }
        SquidGameCinematic.announcePrize("§eFin de l'épreuve 1", "§fLes survivants avancent. Prochaine scène : §bPont de Verre§f.", eliminated.size(), 0);
    }

    private void dormitoryBeforeBridgeTick() {
        tick++;
        int remaining = DORMITORY_DELAY_SECONDS - tick;
        for (UUID uuid : new HashSet<>(qualified)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || eliminated.contains(uuid)) continue;
            player.sendTitle("§d§lDORTOIR", "§bPont de Verre dans §e" + Math.max(0, remaining) + "s", 0, 25, 5);
        }
        if (tick >= DORMITORY_DELAY_SECONDS) startGlassBridge();
    }

    private void startGlassBridge() {
        if (qualified.isEmpty()) {
            finishLastAliveOrNoWinner("Aucun joueur qualifié pour le Pont de Verre.");
            return;
        }
        tick = 0;
        SquidPackManager.setStage("GLASS_BRIDGE");
        Location bridge = SquidPackManager.location("bridge-start");
        if (bridge != null) {
            for (UUID uuid : new HashSet<>(qualified)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline() || eliminated.contains(uuid)) continue;
                player.teleport(bridge);
                player.sendTitle("§b§lPONT DE VERRE", "§fChoisis la bonne vitre", 0, 45, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.25f);
            }
        }
        SquidGameCinematic.flashyBroadcast("§b§lPONT DE VERRE", "§fChoisis bien. §cUne mauvaise vitre, tu tombes puis retour dortoir !");
    }

    private void glassBridgeTick() {
        tick++;
        FileConfiguration config = SquidPackManager.config();
        Location bridgeStart = SquidPackManager.location("bridge-start");
        if (bridgeStart == null || bridgeStart.getWorld() == null) {
            finishLastAliveOrNoWinner("Pont de verre introuvable.");
            return;
        }
        int finishX = config.getInt("glass.finish-x");
        int zLeft = config.getInt("glass.z-left");
        int zRight = config.getInt("glass.z-right");
        for (UUID uuid : new HashSet<>(qualified)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || eliminated.contains(uuid) || falling.contains(uuid)) continue;
            Location loc = player.getLocation();
            if (!loc.getWorld().equals(bridgeStart.getWorld())) continue;
            if (loc.getBlockY() < bridgeStart.getBlockY() - 3) {
                delayedGlassEliminate(player, "Chute du pont");
                continue;
            }
            if (loc.getBlockX() >= finishX) {
                finishPack(player);
                return;
            }
            int step = Math.max(0, Math.min(9, (loc.getBlockX() - (bridgeStart.getBlockX() + 3)) / 3));
            String safe = config.getString("glass.safe." + step, "LEFT");
            boolean onLeft = Math.abs(loc.getBlockZ() - zLeft) <= 1;
            boolean onRight = Math.abs(loc.getBlockZ() - zRight) <= 1;
            if ((onLeft && safe.equals("RIGHT")) || (onRight && safe.equals("LEFT"))) {
                breakGlassThenDormitory(player, loc, "Mauvaise vitre");
            }
        }
        if (tryFinishLastSurvivor()) return;
        if (countQualifiedAlive() <= 0 && falling.isEmpty()) finishLastAliveOrNoWinner("Tous les qualifiés ont été éliminés.");
        if (tick >= 120) {
            Player winner = findAliveQualifiedPlayer();
            if (winner != null) finishPack(winner);
            else finishLastAliveOrNoWinner("Temps écoulé : aucun gagnant.");
        }
    }

    private void breakGlassThenDormitory(Player player, Location glassLocation, String reason) {
        UUID uuid = player.getUniqueId();
        if (!falling.add(uuid)) return;
        SquidGameCinematic.breakGlass(player, glassLocation);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> delayedGlassEliminate(player, reason), 45L);
    }

    private void delayedGlassEliminate(Player player, String reason) {
        if (player == null || !player.isOnline()) return;
        falling.remove(player.getUniqueId());
        eliminate(player, reason);
    }

    private void finishPack(Player winner) {
        if (finished || winner == null) return;
        finished = true;
        SquidPackManager.setStage("FINISHED");
        String winnerName = winner.getName();
        winner.sendTitle("§6§lVICTOIRE !", "§fDernier survivant", 0, 60, 15);
        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.1f);
        EventManager.stopEvent(winner);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> SquidGameCinematic.flashyBroadcast("§6§lVICTOIRE SQUID MOOD GAME", "§e" + winnerName + " §fremporte le show. §a/event §fpour le prochain défi !"), 20L);
    }

    private void finishWithoutWinner(String reason) {
        if (finished) return;
        finished = true;
        SquidPackManager.setStage("FINISHED");
        Player anchor = findOnlineEventPlayer();
        if (anchor != null) EventManager.stopEvent(anchor);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> SquidGameCinematic.flashyBroadcast("§c§lSQUID MOOD GAME TERMINÉ", "§f" + reason + " §e/event §fpour retenter !"), 20L);
    }

    private void finishLastAliveOrNoWinner(String noWinnerReason) {
        Player survivor = findLastAlivePlayer();
        if (survivor != null) finishPack(survivor);
        else finishWithoutWinner(noWinnerReason);
    }

    private boolean tryFinishLastSurvivor() {
        if (initialParticipants <= 1) return false;
        if (eliminated.isEmpty()) return false;
        if (!falling.isEmpty()) return false;
        Player survivor = findLastAlivePlayer();
        if (survivor != null && countAliveAll() == 1) {
            finishPack(survivor);
            return true;
        }
        return false;
    }

    private void eliminate(Player player, String reason) {
        if (!eliminated.add(player.getUniqueId())) return;
        falling.remove(player.getUniqueId());
        SquidGameCinematic.teleportDormitory(player);
        player.sendTitle("§c§lÉLIMINÉ", "§fRetour dortoir", 0, 45, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.9f, 0.65f);
        player.sendMessage("§8----- §c§l✖ ÉLIMINATION ✖ §8-----");
        player.sendMessage("§c■ §fCause : §e" + reason);
        player.sendMessage("§d⌂ §fRetour au dortoir. Regarde les survivants jouer.");
        player.sendMessage("§8-----------------------------");
        Bukkit.broadcastMessage("§c✖ §e" + player.getName() + " §fest éliminé §8• §7" + reason);
    }

    private void forEachAlive(PlayerAction action) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (eliminated.contains(player.getUniqueId()) || falling.contains(player.getUniqueId())) continue;
            action.accept(player);
        }
    }

    private int countAliveAll() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) if (EventManager.isParticipant(player) && !eliminated.contains(player.getUniqueId()) && !falling.contains(player.getUniqueId())) count++;
        return count;
    }

    private int countAliveNotQualified() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) if (EventManager.isParticipant(player) && !eliminated.contains(player.getUniqueId()) && !falling.contains(player.getUniqueId()) && !qualified.contains(player.getUniqueId())) count++;
        return count;
    }

    private int countQualifiedAlive() {
        int count = 0;
        for (UUID uuid : qualified) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && !eliminated.contains(uuid) && !falling.contains(uuid)) count++;
        }
        return count;
    }

    private Player findLastAlivePlayer() {
        Player survivor = null;
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player) || eliminated.contains(player.getUniqueId()) || falling.contains(player.getUniqueId())) continue;
            survivor = player;
            count++;
        }
        return count == 1 ? survivor : null;
    }

    private Player findAliveQualifiedPlayer() {
        for (UUID uuid : qualified) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && !eliminated.contains(uuid) && !falling.contains(uuid)) return player;
        }
        return null;
    }

    private boolean hasMoved(Location from, Location to) {
        if (from == null || to == null || from.getWorld() == null || to.getWorld() == null) return false;
        if (!from.getWorld().equals(to.getWorld())) return true;
        return from.distanceSquared(to) > 0.08;
    }

    private Player findOnlineEventPlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) if (EventManager.isEventPlayer(player)) return player;
        for (Player player : Bukkit.getOnlinePlayers()) return player;
        return null;
    }

    private void resetRuntime() {
        tick = 0;
        redReaction = 0;
        initialParticipants = 0;
        green = true;
        runtimeStarted = false;
        finished = false;
        lastRedPositions.clear();
        qualified.clear();
        eliminated.clear();
        falling.clear();
    }

    @FunctionalInterface
    private interface PlayerAction {
        void accept(Player player);
    }
}
