package fr.moodcraft.event.generator;

import fr.moodcraft.event.util.MoodStyle;
import org.bukkit.entity.Player;

public final class GeneratedGameStyleManager {

    private GeneratedGameStyleManager() {
    }

    public static GeneratedGameStyle get(Player player) {
        return GeneratedGameStyle.MOODCRAFT;
    }

    public static GeneratedGameStyle cycle(Player player) {
        if (player != null) {
            MoodStyle.successMessage(
                    player,
                    MoodStyle.MODULE,
                    "Style unique activé.",
                    MoodStyle.detail("Style : §e" + GeneratedGameStyle.MOODCRAFT.getDisplayName()),
                    MoodStyle.detail("Les anciens thèmes ont été retirés pour éviter les conflits.")
            );
        }
        return GeneratedGameStyle.MOODCRAFT;
    }
}
