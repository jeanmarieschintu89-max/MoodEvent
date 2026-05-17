package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameSize {

    PETIT("Petit", Material.LIME_CONCRETE, 13, 7, 60, 21),
    MOYEN("Moyen", Material.GOLD_BLOCK, 15, 8, 90, 31),
    GRAND("Grand", Material.ORANGE_CONCRETE, 17, 9, 125, 43),
    GEANT("Géant", Material.REDSTONE_BLOCK, 19, 10, 160, 55);

    private final String displayName;
    private final Material icon;
    private final int survivalWidth;
    private final int survivalFloors;
    private final int waterLength;
    private final int mazeWidth;

    GeneratedGameSize(String displayName, Material icon, int survivalWidth, int survivalFloors, int waterLength, int mazeWidth) {
        this.displayName = displayName;
        this.icon = icon;
        this.survivalWidth = survivalWidth;
        this.survivalFloors = survivalFloors;
        this.waterLength = waterLength;
        this.mazeWidth = mazeWidth;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public int getSurvivalWidth() { return survivalWidth; }
    public int getSurvivalFloors() { return survivalFloors; }
    public int getWaterLength() { return waterLength; }
    public int getMazeWidth() { return mazeWidth; }

    public int getGoldRushWidth() {
        return switch (this) {
            case PETIT -> 19;
            case MOYEN -> 25;
            case GRAND -> 33;
            case GEANT -> 39;
        };
    }

    public int getGoldRushHeight() {
        return switch (this) {
            case PETIT -> 11;
            case MOYEN -> 13;
            case GRAND -> 15;
            case GEANT -> 17;
        };
    }

    public String getConfigKey() {
        return switch (this) {
            case PETIT -> "petit";
            case MOYEN -> "moyen";
            case GRAND -> "grand";
            case GEANT -> "geant";
        };
    }

    public String describeFor(GeneratedGameType type) {
        if (type == null) return displayName;
        return switch (type) {
            case SURVIE_ETAGES -> survivalWidth + "x" + survivalWidth + " §8• §7" + survivalFloors + " étages";
            case RUEE_OR -> getGoldRushWidth() + "x" + getGoldRushHeight() + " §8• §7durée config";
            case WATER_JUMP -> waterLength + " blocs §8• §7hauteur progressive";
            case LABYRINTHE -> mazeWidth + "x" + mazeWidth + " §8• §7carré avec sas";
            case LABYRINTHE_ROND -> mazeWidth + " blocs §8• §7rond, départ au centre";
        };
    }
}
