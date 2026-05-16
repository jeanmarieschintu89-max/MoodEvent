package fr.moodcraft.event.manager;

import org.bukkit.Material;

public enum WaitingRoomTheme {

    MOODCRAFT("moodcraft", "MoodCraft", Material.POLISHED_BLACKSTONE, Material.DEEPSLATE_TILES, Material.GRAY_STAINED_GLASS);

    private final String key;
    private final String displayName;
    private final Material primary;
    private final Material accent;
    private final Material glass;

    WaitingRoomTheme(String key, String displayName, Material primary, Material accent, Material glass) {
        this.key = key;
        this.displayName = displayName;
        this.primary = primary;
        this.accent = accent;
        this.glass = glass;
    }

    public String key() { return key; }
    public String displayName() { return displayName; }
    public Material primary() { return primary; }
    public Material accent() { return accent; }
    public Material glass() { return glass; }

    public Material light() {
        return Material.SOUL_LANTERN;
    }

    public static WaitingRoomTheme of(String text) {
        return MOODCRAFT;
    }

    public static String key(String text) {
        return "moodcraft";
    }
}
