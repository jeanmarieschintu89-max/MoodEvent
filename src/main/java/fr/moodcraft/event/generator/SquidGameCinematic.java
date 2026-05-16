package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class SquidGameCinematic {

    private SquidGameCinematic() {
    }

    public static void teleportDormitory(Player player) {
        if (player == null) return;
        Location dormitory = SquidPackManager.location("dormitory");
        if (dormitory != null && dormitory.getWorld() != null) {
            player.teleport(dormitory);
            return;
        }
        Location lobby = SquidPackManager.location("lobby");
        if (lobby != null && lobby.getWorld() != null) player.teleport(lobby);
    }

    public static void updateDollAndLights(boolean green) {
        Location start = SquidPackManager.location("start");
        if (start == null || start.getWorld() == null) return;
        int finishX = SquidPackManager.config().getInt("red-green.finish-x");
        World world = start.getWorld();
        int x = finishX + 5;
        int y = start.getBlockY();
        int z = start.getBlockZ();

        Material lamp = green ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        Material glass = green ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS;
        Material face = green ? Material.YELLOW_CONCRETE : Material.REDSTONE_BLOCK;
        int faceZ = green ? z - 1 : z + 1;

        for (int dx = -1; dx <= 1; dx++) {
            world.getBlockAt(x, y + 1, z + dx).setType(Material.PINK_CONCRETE, false);
            world.getBlockAt(x, y + 2, z + dx).setType(Material.YELLOW_CONCRETE, false);
        }
        world.getBlockAt(x, y + 3, z).setType(face, false);
        world.getBlockAt(x, y + 3, faceZ).setType(Material.BLACK_CONCRETE, false);
        world.getBlockAt(x - 2, y + 3, z).setType(lamp, false);
        world.getBlockAt(x - 3, y + 3, z).setType(lamp, false);
        world.getBlockAt(x - 2, y + 4, z).setType(glass, false);
        world.getBlockAt(x - 3, y + 4, z).setType(glass, false);
    }

    public static void announcePrize(String title, String detail, int eliminated, int prizeEach) {
        int survivors = Math.max(0, Bukkit.getOnlinePlayers().size() - eliminated);
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8----- §d§l✦ DORTOIR SQUID ✦ §8-----");
        Bukkit.broadcastMessage("§e★ §f" + title);
        Bukkit.broadcastMessage("§d➜ §f" + detail);
        Bukkit.broadcastMessage("§c■ §fÉliminés : §c" + eliminated + " §8• §aSurvivants en piste : §a" + survivors);
        Bukkit.broadcastMessage("§e▶ §fLe prochain jeu commence bientôt. Reste prêt.");
        Bukkit.broadcastMessage("§8-----------------------------");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
        }
    }

    public static void flashyBroadcast(String title, String detail) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8----- §c§l✦ SQUID MOOD GAME ✦ §8-----");
        Bukkit.broadcastMessage("§e★ §f" + title);
        Bukkit.broadcastMessage("§d➜ §f" + detail);
        Bukkit.broadcastMessage("§a▶ §fTape §e/event §fpour tenter le prochain jeu !");
        Bukkit.broadcastMessage("§8-----------------------------");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.2f);
        }
    }

    public static void breakGlass(Player player, Location location) {
        if (player == null || location == null || location.getWorld() == null) return;
        clearGlass(location.getBlock());
        clearGlass(location.clone().add(0, 0, 1).getBlock());
        clearGlass(location.clone().add(0, 0, -1).getBlock());
        clearGlass(location.clone().add(1, 0, 0).getBlock());
        clearGlass(location.clone().add(-1, 0, 0).getBlock());
        player.sendTitle("§c§lCRAC !", "§fMauvais choix...", 0, 35, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.75f);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.45f, 1.4f);
    }

    private static void clearGlass(Block block) {
        if (block == null) return;
        String name = block.getType().name();
        if (name.contains("GLASS") || block.getType() == Material.SEA_LANTERN) block.setType(Material.AIR, false);
    }
}
