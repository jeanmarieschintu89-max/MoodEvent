package fr.moodcraft.event.generator;

import fr.moodcraft.event.Main;
import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GeneratedGameStyleManager {

    private static final Map<UUID, GeneratedGameStyle> SELECTED = new HashMap<>();

    private GeneratedGameStyleManager() {
    }

    public static GeneratedGameStyle get(Player player) {
        if (player == null) return GeneratedGameStyle.CLASSIQUE;
        return SELECTED.getOrDefault(player.getUniqueId(), GeneratedGameStyle.CLASSIQUE);
    }

    public static GeneratedGameStyle cycle(Player player) {
        GeneratedGameStyle next = get(player).next();
        if (player != null) {
            SELECTED.put(player.getUniqueId(), next);
            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Style de génération changé.",
                    MoodStyle.detail("Style : §e" + next.getDisplayName())
            );
        }
        return next;
    }
}
