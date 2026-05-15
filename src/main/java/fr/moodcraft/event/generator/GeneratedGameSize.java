package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameSize {

    PETIT("Petit", Material.LIME_CONCRETE, 17, 12),
    MOYEN("Moyen", Material.GOLD_BLOCK, 29, 24),
    GRAND("Grand", Material.ORANGE_CONCRETE, 41, 36),
    GEANT("Géant", Material.REDSTONE_BLOCK, 57, 52);

    private final String displayName;
    private final Material icon;
    private final int squareSize;
    private final int length;

    GeneratedGameSize(String displayName, Material icon, int squareSize, int length) {
        this.displayName = displayName;
        this.icon = icon;
        this.squareSize = squareSize;
        this.length = length;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSquareSize() {
        return squareSize;
    }

    public int getLength() {
        return length;
    }
}
