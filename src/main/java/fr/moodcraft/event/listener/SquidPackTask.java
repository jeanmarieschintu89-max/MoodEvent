package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.SquidPackManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.manager.WaitingRoomManager;
import fr.moodcraft.event.util.MoodStyle;
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
                player.sendTitle("§aFeu vert", "§fAvancez jusqu'à la ligne rouge", 0, 45, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.4f);
                MoodStyle.infoMessage(
                        player,
                        MoodStyle.MODULE,
                        SquidPackManager.GAME_NAME + " lancé.",
                        MoodStyle.detail("Épreuve : §eFeu Rouge / Feu Vert"),
                        MoodStyle.detail("Feu vert : avancez."),
                        MoodStyle.detail("Feu rouge : ne bougez plus."),
                        MoodStyle.detail("Objectif : atteindre la ligne rouge.")
                );
            }
        }

        broadcast(SquidPackManager.GAME_NAME, "Feu Rouge / Feu Vert : avancez quand c'est vert, ne bougez pas quand c'est rouge.");
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
                player.sendTitle(green ? "§aFEU VERT" : "§cFEU ROUGE", green ? "§fAvancez" : "§fNe bougez plus", 0, 25, 5);
                player.playSound(player.getLocation(), green ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, green ? 1.4f : 0.7f);
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

        int aliveNotQualified = countAliveNotQualified();
        if (tick >= 90) {
            if (qualified.isEmpty()) finishWithoutWinner("Temps écoulé : aucun joueur qualifié.");
            else startGlassBridge();
            return;
        }

        if (aliveNotQualified <= 0) {
            if (qualified.isEmpty()) finishWithoutWinner("Tous les joueurs ont été éliminés.");
            else startGlassBridge();
        }
    }

    private void qualifyRedGreen(Player player) {
        if (!qualified.add(player.getUniqueId())) return;
        Location bridge = SquidPackManager.location("bridge-start");
        if (bridge != null) player.teleport(bridge);
        player.sendTitle("§aQualifié", "§fPont de verre", 0, 45, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
        MoodStyle.successMessage(
                player,
                MoodStyle.MODULE,
                "Vous êtes qualifié.",
                MoodStyle.detail("Jeu : §e" + SquidPackManager.GAME_NAME),
                MoodStyle.detail("Prochaine épreuve : §ePont de verre"),
                MoodStyle.detail("Patientez sur la plateforme verte.")
        );
    }

    private void startGlassBridge() {
        if (qualified.isEmpty()) {
            finishWithoutWinner("Aucun joueur qualifié pour le Pont de Verre.");
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
                player.sendTitle("§bPont de verre", "§fChoisissez la bonne vitre", 0, 45, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
            }
        }
        broadcast(SquidPackManager.GAME_NAME, "Pont de verre : choisissez la bonne vitre. La mauvaise casse et élimine.");
    }

    private void glassBridgeTick() {
        tick++;
        FileConfiguration config = SquidPackManager.config();
        Location bridgeStart = SquidPackManager.location("bridge-start");
        if (bridgeStart == null || bridgeStart.getWorld() == null) {
            finishWithoutWinner("Pont de verre introuvable.");
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

        if (countQualifiedAlive() <= 0) {
            finishWithoutWinner("Tous les qualifiés ont été éliminés.");
            return;
        }

        if (tick >= 120) {
            Player winner = qualified.stream()
                    .map(Bukkit::getPlayer)
                    .filter(player -> player != null && player.isOnline() && !eliminated.contains(player.getUniqueId()))
                    .findFirst()
                    .orElse(null);
            if (winner != null) finishPack(winner);
            else finishWithoutWinner("Temps écoulé : aucun gagnant.");
        }
    }

    private void finishPack(Player winner) {
        if (finished || winner == null) return;
        finished = true;
        SquidPackManager.setStage("FINISHED");
        broadcast(SquidPackManager.GAME_NAME + " terminé", "Gagnant : §a" + winner.getName());
        winner.sendTitle("§6Victoire", "§f" + SquidPackManager.GAME_NAME, 0, 60, 15);
        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.1f);
        EventManager.stopEvent(winner);
    }

    private void finishWithoutWinner(String reason) {
        if (finished) return;
        finished = true;
        SquidPackManager.setStage("FINISHED");
        broadcast(SquidPackManager.GAME_NAME + " terminé", reason);

        Player anchor = findOnlineEventPlayer();
        if (anchor != null) {
            EventManager.stopEvent(anchor);
        }
    }

    private void eliminate(Player player, String reason) {
        if (!eliminated.add(player.getUniqueId())) return;
        WaitingRoomManager.teleport(player);
        player.sendTitle("§cÉliminé", "§f" + reason, 0, 45, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 0.7f);
        MoodStyle.errorMessage(player, MoodStyle.MODULE, "Vous êtes éliminé.", MoodStyle.detail("Jeu : §e" + SquidPackManager.GAME_NAME), MoodStyle.detail(reason), MoodStyle.detail("Vous êtes envoyé en salle d'attente."));
    }

    private void forEachAlive(PlayerAction action) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!EventManager.isParticipant(player)) continue;
            if (eliminated.contains(player.getUniqueId())) continue;
            action.accept(player);
        }
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

    private void broadcast(String title, String detail) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MoodStyle.header(MoodStyle.MODULE));
        Bukkit.broadcastMessage(MoodStyle.info(title));
        Bukkit.broadcastMessage(MoodStyle.detail(detail));
        Bukkit.broadcastMessage(MoodStyle.FRAME);
    }

    @FunctionalInterface
    private interface PlayerAction {
        void accept(Player player);
    }
}
