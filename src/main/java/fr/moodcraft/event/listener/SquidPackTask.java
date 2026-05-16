package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
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

    private int tick;
    private boolean green = true;
    private boolean runtimeStarted;
    private boolean finished;
    private final Map<UUID, Location> lastRedPositions = new HashMap<>();
    private final Set<UUID> qualified = new HashSet<>();
    private final Set<UUID> eliminated = new HashSet<>();

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

        String stage = SquidPackManager.getStage();
        if (stage.equals("WAITING")) {
            startRedGreen();
            return;
        }
        if (stage.equals("RED_GREEN")) {
            redGreenTick();
            return;
        }
        if (stage.equals("GLASS_BRIDGE")) {
            glassBridgeTick();
        }
    }

    private void startRedGreen() {
        tick = 0;
        green = true;
        runtimeStarted = true;
        finished = false;
        lastRedPositions.clear();
        qualified.clear();
        eliminated.clear();
        SquidPackManager.setStage("RED_GREEN");

        Location start = SquidPackManager.location("start");
        if (start != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!EventManager.isParticipant(player)) continue;
                player.teleport(start);
                player.sendTitle("§a§lFEU VERT", "§fCours jusqu'à la ligne rouge !", 0, 45, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.6f);
                player.sendMessage("§8----- §c§l✦ SQUID MOOD GAME ✦ §8-----");
                player.sendMessage("§a▶ §fFEU VERT : §acours !");
                player.sendMessage("§c■ §fFEU ROUGE : §cstop net, sinon dortoir.");
                player.sendMessage("§e★ §fObjectif : §eatteindre la ligne rouge.");
                player.sendMessage("§8-----------------------------");
            }
        }

        flashyBroadcast("§c§lSQUID MOOD GAME", "§aFEU VERT ! §fCours quand c'est vert, fige-toi quand c'est rouge !");
    }

    private void redGreenTick() {
        tick++;
        FileConfiguration config = SquidPackManager.config();
        int finishX = config.getInt("red-green.finish-x");

        if (tick % 5 == 0) {
            green = !green;
            if (!green) {
                lastRedPositions.clear();
                forEachAlive(player -> lastRedPositions.put(player.getUniqueId(), player.getLocation().clone()));
            }
            forEachAlive(player -> {
                player.sendTitle(green ? "§a§lFEU VERT" : "§c§lFEU ROUGE", green ? "§fGO GO GO !" : "§fSTOP !", 0, 25, 5);
                player.playSound(player.getLocation(), green ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, green ? 1.5f : 0.6f);
            });
        }

        forEachAlive(player -> {
            if (!green) {
                Location red = lastRedPositions.get(player.getUniqueId());
                if (hasMoved(red, player.getLocation())) {
                    eliminate(player, "Mouvement pendant Feu Rouge");
                    return;
                }
            }
            if (player.getLocation().getBlockX() >= finishX) {
                qualifyRedGreen(player);
            }
        });

        if (tryFinishLastSurvivor()) return;

        int aliveNotQualified = countAliveNotQualified();
        if (tick >= 90) {
            if (qualified.isEmpty()) finishLastAliveOrNoWinner("Temps écoulé : aucun joueur qualifié.");
            else startGlassBridge();
            return;
        }

        if (aliveNotQualified <= 0) {
            if (qualified.isEmpty()) finishLastAliveOrNoWinner("Tous les joueurs non qualifiés ont été éliminés.");
            else startGlassBridge();
        }
    }

    private void qualifyRedGreen(Player player) {
        if (!qualified.add(player.getUniqueId())) return;
        Location bridge = SquidPackManager.location("bridge-start");
        if (bridge != null) player.teleport(bridge);
        player.sendTitle("§e§lQUALIFIÉ !", "§bPont de verre", 0, 45, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.25f);
        player.sendMessage("§8----- §e§l★ QUALIFICATION ★ §8-----");
        player.sendMessage("§a✔ §fTu passes au §bPont de Verre§f.");
        player.sendMessage("§7Patiente sur la plateforme verte, le show continue.");
        player.sendMessage("§8-----------------------------");
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
        flashyBroadcast("§b§lPONT DE VERRE", "§fChoisis bien. §cUne mauvaise vitre, et retour dortoir !");
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
            if (player == null || !player.isOnline() || eliminated.contains(uuid)) continue;

            Location loc = player.getLocation();
            if (!loc.getWorld().equals(bridgeStart.getWorld())) continue;

            if (loc.getBlockY() < bridgeStart.getBlockY() - 3) {
                eliminate(player, "Chute du pont");
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
            boolean wrongLeft = onLeft && safe.equals("RIGHT");
            boolean wrongRight = onRight && safe.equals("LEFT");

            if (wrongLeft || wrongRight) {
                loc.getBlock().setType(Material.AIR, false);
                eliminate(player, "Mauvaise vitre");
            }
        }

        if (tryFinishLastSurvivor()) return;

        if (countQualifiedAlive() <= 0) {
            finishLastAliveOrNoWinner("Tous les qualifiés ont été éliminés.");
            return;
        }

        if (tick >= 120) {
            Player winner = findAliveQualifiedPlayer();
            if (winner != null) finishPack(winner);
            else finishLastAliveOrNoWinner("Temps écoulé : aucun gagnant.");
        }
    }

    private void finishPack(Player winner) {
        if (finished || winner == null) return;
        finished = true;
        SquidPackManager.setStage("FINISHED");
        String winnerName = winner.getName();
        winner.sendTitle("§6§lVICTOIRE !", "§fSquid Mood Game", 0, 60, 15);
        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.1f);
        EventManager.stopEvent(winner);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> flashyBroadcast("§6§lVICTOIRE SQUID MOOD GAME", "§e" + winnerName + " §fremporte la partie ! §a/event §fpour le prochain show !"), 20L);
    }

    private void finishWithoutWinner(String reason) {
        if (finished) return;
        finished = true;
        SquidPackManager.setStage("FINISHED");
        Player anchor = findOnlineEventPlayer();
        if (anchor != null) {
            EventManager.stopEvent(anchor);
        }
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> flashyBroadcast("§c§lSQUID MOOD GAME TERMINÉ", "§f" + reason + " §e/event §fpour retenter !"), 20L);
    }

    private void finishLastAliveOrNoWinner(String noWinnerReason) {
        Player survivor = findLastAlivePlayer();
        if (survivor != null) finishPack(survivor);
        else finishWithoutWinner(noWinnerReason);
    }

    private boolean tryFinishLastSurvivor() {
        Player survivor = findLastAlivePlayer();
        if (survivor != null && countAliveAll() == 1) {
            finishPack(survivor);
            return true;
        }
        return false;
    }

    private void eliminate(Player player, String reason) {
        if (!eliminated.add(player.getUniqueId())) return;
        teleportToDormitory(player);
        player.sendTitle("§c§lÉLIMINÉ", "§fRetour dortoir", 0, 45, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.9f, 0.65f);
        player.sendMessage("§8----- §c§l✖ ÉLIMINATION ✖ §8-----");
        player.sendMessage("§c■ §fCause : §e" + reason);
        player.sendMessage("§d⌂ §fRetour au dortoir. Regarde les survivants jouer.");
        player.sendMessage("§8-----------------------------");
        Bukkit.broadcastMessage("§c✖ §e" + player.getName() + " §fest éliminé §8• §7" + reason);
    }

    private void teleportToDormitory(Player player) {
        Location dormitory = SquidPackManager.location("dormitory");
        if (dormitory != null && dormitory.getWorld() != null) {
            player.teleport(dormitory);
            return;
        }
        Location lobby = SquidPackManager.location("lobby");
        if (lobby != null && lobby.getWorld() != null) player.teleport(lobby);
    }

    private void forEachAlive(PlayerAction action) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (eliminated.contains(player.getUniqueId())) continue;
            action.accept(player);
        }
    }

    private int countAliveAll() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (eliminated.contains(player.getUniqueId())) continue;
            count++;
        }
        return count;
    }

    private int countAliveNotQualified() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (eliminated.contains(player.getUniqueId())) continue;
            if (qualified.contains(player.getUniqueId())) continue;
            count++;
        }
        return count;
    }

    private int countQualifiedAlive() {
        int count = 0;
        for (UUID uuid : qualified) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            if (eliminated.contains(uuid)) continue;
            count++;
        }
        return count;
    }

    private Player findLastAlivePlayer() {
        Player survivor = null;
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (eliminated.contains(player.getUniqueId())) continue;
            survivor = player;
            count++;
        }
        return count == 1 ? survivor : null;
    }

    private Player findAliveQualifiedPlayer() {
        for (UUID uuid : qualified) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && !eliminated.contains(uuid)) return player;
        }
        return null;
    }

    private boolean hasMoved(Location from, Location to) {
        if (from == null || to == null || from.getWorld() == null || to.getWorld() == null) return false;
        if (!from.getWorld().equals(to.getWorld())) return true;
        return from.distanceSquared(to) > 0.08;
    }

    private Player findOnlineEventPlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (EventManager.isEventPlayer(player)) return player;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            return player;
        }
        return null;
    }

    private void resetRuntime() {
        tick = 0;
        green = true;
        runtimeStarted = false;
        finished = false;
        lastRedPositions.clear();
        qualified.clear();
        eliminated.clear();
    }

    private void flashyBroadcast(String title, String detail) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8----- §c§l✦ SQUID MOOD GAME ✦ §8-----");
        Bukkit.broadcastMessage("§e★ §f" + title);
        Bukkit.broadcastMessage("§d➜ §f" + detail);
        Bukkit.broadcastMessage("§a▶ §fTape §e/event §fpour rejoindre le prochain jeu !");
        Bukkit.broadcastMessage("§8-----------------------------");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.2f);
        }
    }

    @FunctionalInterface
    private interface PlayerAction {
        void accept(Player player);
    }
}
