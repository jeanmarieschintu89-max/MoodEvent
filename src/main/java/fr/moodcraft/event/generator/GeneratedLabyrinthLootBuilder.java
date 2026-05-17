package fr.moodcraft.event.generator;

import fr.moodcraft.event.loot.EventLootManager;
import fr.moodcraft.event.loot.LootTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class GeneratedLabyrinthLootBuilder {

    private static final Random RANDOM = new Random();

    private GeneratedLabyrinthLootBuilder() {}

    public static void build(World world, boolean[][] path, int minX, int minZ, int y, int size, int entryX, int entryZ, int exitX, int exitZ) {
        if (world == null || path == null) return;
        List<int[]> candidates = new ArrayList<>();
        for (int x = 1; x < size - 1; x++) {
            for (int z = 1; z < size - 1; z++) {
                if (!path[x][z]) continue;
                if (near(x, z, entryX, entryZ, 6) || near(x, z, exitX, exitZ, 6)) continue;
                if (neighbours(path, x, z) > 1 && RANDOM.nextInt(100) < 65) continue;
                candidates.add(new int[]{x, z});
            }
        }
        if (candidates.isEmpty()) return;
        Collections.shuffle(candidates, RANDOM);
        int amount = Math.min(amount(size), candidates.size());
        for (int i = 0; i < amount; i++) {
            int[] point = candidates.get(i);
            int wx = minX + point[0];
            int wz = minZ + point[1];
            world.getBlockAt(wx, y, wz).setType(Material.CHEST, false);
            EventLootManager.registerLootChest(new Location(world, wx, y, wz), GeneratedGameType.LABYRINTHE, tier(i, amount));
        }
    }

    private static int amount(int size) {
        if (size >= 55) return 6;
        if (size >= 43) return 5;
        if (size >= 31) return 4;
        if (size >= 21) return 3;
        return 2;
    }

    private static LootTier tier(int index, int amount) {
        if (amount >= 5 && index == 0) return LootTier.EPIQUE;
        if (amount >= 3 && index <= 1) return LootTier.RARE;
        return LootTier.COMMUN;
    }

    private static boolean near(int x, int z, int ox, int oz, int distance) {
        return Math.abs(x - ox) + Math.abs(z - oz) <= distance;
    }

    private static int neighbours(boolean[][] path, int x, int z) {
        int count = 0;
        if (x > 0 && path[x - 1][z]) count++;
        if (z > 0 && path[x][z - 1]) count++;
        if (x + 1 < path.length && path[x + 1][z]) count++;
        if (z + 1 < path[0].length && path[x][z + 1]) count++;
        return count;
    }
}
