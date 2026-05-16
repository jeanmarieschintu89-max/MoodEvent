package fr.moodcraft.event.generator;

import org.bukkit.Material;

public record GeneratedGameTheme(
        Material primary,
        Material accent,
        Material light,
        Material glass
) {

    public static GeneratedGameTheme from(GeneratedGameStyle style) {
        return new GeneratedGameTheme(
                GeneratedGameStyle.MOODCRAFT.getPrimary(),
                GeneratedGameStyle.MOODCRAFT.getAccent(),
                GeneratedGameStyle.MOODCRAFT.getLight(),
                GeneratedGameStyle.MOODCRAFT.getGlass()
        );
    }

    public Material floorA() {
        return primary;
    }

    public Material floorB() {
        return accent;
    }

    public Material wall() {
        return primary;
    }

    public Material pillar() {
        return accent;
    }

    public Material start() {
        return Material.LIME_CONCRETE;
    }

    public Material finish() {
        return Material.RED_CONCRETE;
    }
}
