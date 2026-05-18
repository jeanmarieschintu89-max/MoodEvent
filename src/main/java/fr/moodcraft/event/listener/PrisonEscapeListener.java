package fr.moodcraft.event.listener;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.generator.GeneratedGameManager;
import fr.moodcraft.event.manager.EventManager;
import fr.moodcraft.event.model.EventType;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrisonEscapeListener implements Listener {

    private static final int SEARCH_RADIUS = 9;
    private static final int GATE_RADIUS = 3;
    private static final int OPEN_HEIGHT = 5;
    private static final Material PUZZLE_MARKER = Material.LODESTONE;
    private static final Material GATE_MARKER = Material.RESPAWN_ANCHOR;
    private static final Map<UUID, Integer> ROOM_PROGRESS = new HashMap<>();
    private static final Map<UUID, Long> START_TIMES = new HashMap<>();
    private static final Map<UUID, Long> FINISH_ELAPSED = new HashMap<>();
    private static final Map<UUID, Location> CHECKPOINTS = new HashMap<>();
    private static final Map<UUID, BossBar> BOSS_BARS = new HashMap<>();

    public PrisonEscapeListener() {
        new BukkitRunnable() {
            @Override public void run() { tickBossBars(); }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    public static String resultChrono(Player player) {
        if (player == null) return "00:00";
        UUID uuid = player.getUniqueId();
        long elapsed = FINISH_ELAPSED.getOrDefault(uuid, Math.max(0L, (System.currentTimeMillis() - START_TIMES.getOrDefault(uuid, System.currentTimeMillis())) / 1000L));
        return formatElapsed(elapsed);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isPrisonPlayer(player)) return;
        ensureState(player);
        if (EventManager.isAtFinish(player)) finishUi(player);
    }

    @EventHandler
    public void onPuzzleInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.PHYSICAL) return;
        Player player = event.getPlayer();
        if (!isPrisonPlayer(player)) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || !isPuzzleMechanism(clicked.getType())) return;
        if (!GeneratedGameManager.isInsideStructure(clicked.getLocation())) return;
        ensureState(player);
        if (isFinished(player)) return;
        if (!isCorrectMechanism(clicked)) { punishWrongMechanism(player); return; }
        int opened = openMarkedGate(clicked.getLocation());
        if (opened <= 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f);
            player.sendActionBar("§c✖ §fLe mécanisme grince, mais aucune vraie grille n'est liée.");
            return;
        }
        clearPuzzleMarker(clicked);
        advanceRoom(player);
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 0.9f, 1.1f);
        MoodStyle.successMessage(player, MoodStyle.MODULE, "Mécanisme activé.", MoodStyle.detail("La bonne grille s'ouvre."), MoodStyle.detail("Salle : §e" + currentRoom(player) + "§7/§e" + totalRooms()), MoodStyle.info("Continue l'évasion, cherche la sortie rouge."));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPrisonPlayer(player)) return;
        ensureState(player);
        event.setCancelled(true);
        Location checkpoint = CHECKPOINTS.get(player.getUniqueId());
        if (checkpoint != null && checkpoint.getWorld() != null && !isFinished(player)) {
            player.teleport(checkpoint);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.25f);
            player.sendActionBar("§6Checkpoint §8• §fRetour salle " + currentRoom(player));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeState(event.getPlayer().getUniqueId());
    }

    private boolean isPuzzleMechanism(Material material) {
        return material == Material.STONE_BUTTON || material == Material.OAK_BUTTON || material == Material.LEVER || material == Material.STONE_PRESSURE_PLATE || material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE || material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }

    private boolean isPrisonPlayer(Player player) {
        return player != null && EventManager.getType() == EventType.PRISON_BREAK && EventManager.isRunning() && EventManager.isParticipant(player);
    }

    private void ensureState(Player player) {
        UUID uuid = player.getUniqueId();
        ROOM_PROGRESS.putIfAbsent(uuid, 1);
        START_TIMES.putIfAbsent(uuid, System.currentTimeMillis());
        CHECKPOINTS.putIfAbsent(uuid, player.getLocation().clone());
        BOSS_BARS.computeIfAbsent(uuid, ignored -> {
            BossBar bar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SEGMENTED_10);
            bar.addPlayer(player);
            return bar;
        });
        updateBossBar(player);
    }

    private void advanceRoom(Player player) {
        if (isFinished(player)) return;
        UUID uuid = player.getUniqueId();
        ROOM_PROGRESS.put(uuid, Math.min(totalRooms(), currentRoom(player) + 1));
        CHECKPOINTS.put(uuid, player.getLocation().clone());
        updateBossBar(player);
    }

    private void finishUi(Player player) {
        UUID uuid = player.getUniqueId();
        if (FINISH_ELAPSED.containsKey(uuid)) return;
        long elapsed = Math.max(0L, (System.currentTimeMillis() - START_TIMES.getOrDefault(uuid, System.currentTimeMillis())) / 1000L);
        FINISH_ELAPSED.put(uuid, elapsed);
        ROOM_PROGRESS.put(uuid, totalRooms());
        CHECKPOINTS.remove(uuid);
        updateBossBar(player);
        player.sendActionBar("§aÉvasion réussie §8• §fChrono arrêté : §e" + formatElapsed(elapsed));
    }

    private boolean isFinished(Player player) { return FINISH_ELAPSED.containsKey(player.getUniqueId()); }
    private int currentRoom(Player player) { return Math.max(1, ROOM_PROGRESS.getOrDefault(player.getUniqueId(), 1)); }
    private int totalRooms() { return Math.max(1, GeneratedGameManager.config().getInt("prison-break.rooms", GeneratedGameManager.config().getInt("prison-break.cells", 6))); }

    private void tickBossBars() {
        for (UUID uuid : new HashMap<>(BOSS_BARS).keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || EventManager.getType() != EventType.PRISON_BREAK || !EventManager.isRunning()) { removeState(uuid); continue; }
            if (!EventManager.isParticipant(player) && !FINISH_ELAPSED.containsKey(uuid)) { removeState(uuid); continue; }
            ensureState(player);
        }
    }

    private void updateBossBar(Player player) {
        BossBar bar = BOSS_BARS.get(player.getUniqueId());
        if (bar == null) return;
        int current = currentRoom(player);
        int total = totalRooms();
        long elapsed = FINISH_ELAPSED.getOrDefault(player.getUniqueId(), Math.max(0L, (System.currentTimeMillis() - START_TIMES.getOrDefault(player.getUniqueId(), System.currentTimeMillis())) / 1000L));
        String status = isFinished(player) ? " §a✓" : "";
        bar.setTitle("§6Prison Escape §8| §fSalle §e" + current + "§7/§e" + total + " §8| §f" + formatElapsed(elapsed) + status);
        bar.setProgress(Math.max(0.05D, Math.min(1.0D, current / (double) total)));
        if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
    }

    private void removeState(UUID uuid) {
        ROOM_PROGRESS.remove(uuid);
        START_TIMES.remove(uuid);
        FINISH_ELAPSED.remove(uuid);
        CHECKPOINTS.remove(uuid);
        BossBar bar = BOSS_BARS.remove(uuid);
        if (bar != null) bar.removeAll();
    }

    private static String formatElapsed(long elapsed) { return String.format("%02d:%02d", elapsed / 60L, elapsed % 60L); }

    private void punishWrongMechanism(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, false, false, true));
        player.sendActionBar("§c✖ §fMauvais mécanisme... cherche un indice dans la salle.");
    }

    private boolean isCorrectMechanism(Block clicked) { return clicked.getRelative(BlockFace.DOWN, 2).getType() == PUZZLE_MARKER; }
    private void clearPuzzleMarker(Block clicked) { if (clicked.getRelative(BlockFace.DOWN, 2).getType() == PUZZLE_MARKER) clicked.getRelative(BlockFace.DOWN, 2).setType(Material.POLISHED_ANDESITE, false); }

    private int openMarkedGate(Location mechanism) {
        World world = mechanism.getWorld();
        if (world == null) return 0;
        Block marker = null;
        double best = Double.MAX_VALUE;
        int cx = mechanism.getBlockX(), cy = mechanism.getBlockY(), cz = mechanism.getBlockZ();
        for (int x = cx - SEARCH_RADIUS; x <= cx + SEARCH_RADIUS; x++) for (int y = cy - 4; y <= cy + 2; y++) for (int z = cz - SEARCH_RADIUS; z <= cz + SEARCH_RADIUS; z++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != GATE_MARKER || !GeneratedGameManager.isInsideStructure(block.getLocation())) continue;
            double distance = block.getLocation().distanceSquared(mechanism);
            if (distance < best) { best = distance; marker = block; }
        }
        if (marker == null) return 0;
        int opened = openGateAround(marker.getLocation());
        if (opened > 0) marker.setType(Material.POLISHED_ANDESITE, false);
        return opened;
    }

    private int openGateAround(Location markerLocation) {
        World world = markerLocation.getWorld();
        if (world == null) return 0;
        int opened = 0;
        int cx = markerLocation.getBlockX(), cy = markerLocation.getBlockY(), cz = markerLocation.getBlockZ();
        for (int x = cx - GATE_RADIUS; x <= cx + GATE_RADIUS; x++) for (int y = cy; y <= cy + OPEN_HEIGHT; y++) for (int z = cz - GATE_RADIUS; z <= cz + GATE_RADIUS; z++) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != Material.IRON_BARS || !GeneratedGameManager.isInsideStructure(block.getLocation())) continue;
            block.setType(Material.AIR, false);
            opened++;
        }
        return opened;
    }
}
