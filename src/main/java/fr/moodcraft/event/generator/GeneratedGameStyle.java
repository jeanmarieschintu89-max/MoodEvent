package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameStyle {

    CLASSIQUE("Classique", Material.STONE_BRICKS, Material.SMOOTH_STONE, Material.SEA_LANTERN),
    ROYAL("Royal", Material.PURPUR_BLOCK, Material.GOLD_BLOCK, Material.SHROOMLIGHT),
    NATURE("Nature", Material.MOSS_BLOCK, Material.OAK_LOG, Material.GLOWSTONE),
    NEIGE("Neige", Material.SNOW_BLOCK, Material.SPRUCE_LOG, Material.SEA_LANTERN),
    SOMBRE("Sombre", Material.POLISHED_BLACKSTONE, Material.DEEPSLATE_TILES, Material.SOUL_LANTERN),
    FESTIVAL("Festival", Material.YELLOW_CONCRETE, Material.MAGENTA_WOOL, Material.GLOWSTONE);

    private final String displayName;
    private final Material primary;
    private final Material accent;
    private final Material light;

    GeneratedGameStyle(String displayName, Material primary, Material accent, Material light) {
        this.displayName = displayName;
        this.primary = primary;
        this.accent = accent;
        this.light = light;
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

    public GeneratedGameStyle next() {
        GeneratedGameStyle[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
