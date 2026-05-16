package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameSize {

    PETIT("Petit", Material.LIME_CONCRETE, 35, 12, 50, 30, 13, 7),
    MOYEN("Moyen", Material.GOLD_BLOCK, 51, 20, 80, 50, 15, 8),
    GRAND("Grand", Material.ORANGE_CONCRETE, 67, 28, 140, 80, 17, 9),
    GEANT("Géant", Material.REDSTONE_BLOCK, 83, 36, 220, 110, 19, 10);

    private final String displayName;
    private final Material icon;
    private final int mazeWidth;
    private final int jumpPlatforms;
    private final int raceLength;
    private final int waterLength;
    private final int survivalWidth;
    private final int survivalFloors;

    GeneratedGameSize(String displayName, Material icon, int mazeWidth, int jumpPlatforms, int raceLength, int waterLength, int survivalWidth, int survivalFloors) {
        this.displayName = displayName;
        this.icon = icon;
        this.mazeWidth = mazeWidth;
        this.jumpPlatforms = jumpPlatforms;
        this.raceLength = raceLength;
        this.waterLength = waterLength;
        this.survivalWidth = survivalWidth;
        this.survivalFloors = survivalFloors;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public int getMazeWidth() { return mazeWidth; }
    public int getJumpPlatforms() { return jumpPlatforms; }
    public int getRaceLength() { return raceLength; }
    public int getWaterLength() { return waterLength; }
    public int getSurvivalWidth() { return survivalWidth; }
    public int getSurvivalFloors() { return survivalFloors; }

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
            case LABYRINTHE -> mazeWidth + "x" + mazeWidth;
            case JUMP -> jumpPlatforms + " plateformes";
            case COURSE -> raceLength + " blocs";
            case WATER_JUMP -> waterLength + " blocs";
            case SURVIE_ETAGES -> survivalWidth + "x" + survivalWidth + " §8• §7" + survivalFloors + " étages";
            case RUEE_OR -> getGoldRushWidth() + "x" + getGoldRushHeight() + " §8• §7" + getGoldRushDurationSeconds() + "s";
        };
    }
}
