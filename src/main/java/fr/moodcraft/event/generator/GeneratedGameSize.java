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
            case PETIT -> 15;
            case MOYEN -> 21;
            case GRAND -> 27;
            case GEANT -> 31;
        };
    }

    public int getGoldRushHeight() {
        return switch (this) {
            case PETIT -> 9;
            case MOYEN -> 10;
            case GRAND -> 12;
            case GEANT -> 13;
        };
    }

    public int getGoldRushDurationSeconds() {
        return switch (this) {
            case PETIT -> 60;
            case MOYEN -> 100;
            case GRAND -> 140;
            case GEANT -> 180;
        };
    }

    public String describeFor(GeneratedGameType type) {
        if (type == null) return displayName;
        return switch (type) {
            case SURVIE_ETAGES -> survivalWidth + "x" + survivalWidth + " §8• §7" + survivalFloors + " étages";
            case RUEE_OR -> getGoldRushWidth() + "x" + getGoldRushHeight() + " §8• §7" + getGoldRushDurationSeconds() + "s";
            case WATER_JUMP -> waterLength + " blocs §8• §7hauteur progressive";
            case LABYRINTHE -> mazeWidth + "x" + mazeWidth + " §8• §7sas opposés aléatoires";
        };
    }
}
