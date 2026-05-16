package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameStyle {

    MOODCRAFT(
            "MoodCraft",
            Material.POLISHED_BLACKSTONE,
            Material.DEEPSLATE_TILES,
            Material.SOUL_LANTERN,
            Material.GRAY_STAINED_GLASS,
            "moodcraft"
    );

    private final String displayName;
    private final Material primary;
    private final Material accent;
    private final Material light;
    private final Material glass;
    private final String waitingRoomStyle;

    GeneratedGameStyle(String displayName, Material primary, Material accent, Material light, Material glass, String waitingRoomStyle) {
        this.displayName = displayName;
        this.primary = primary;
        this.accent = accent;
        this.light = light;
        this.glass = glass;
        this.waitingRoomStyle = waitingRoomStyle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getPrimary() {
        return primary;
    }

    public Material getAccent() {
        return accent;
    }

    public Material getLight() {
        return light;
    }

    public Material getGlass() {
        return glass;
    }

    public String getWaitingRoomStyle() {
        return waitingRoomStyle;
    }

    public GeneratedGameStyle next() {
        return MOODCRAFT;
    }
}
