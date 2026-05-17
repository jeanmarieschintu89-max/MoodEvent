package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum LabyrinthShape {

    SQUARE("Carré", Material.MAP),
    ROUND("Rond", Material.COMPASS);

    private final String displayName;
    private final Material icon;

    LabyrinthShape(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }
}
