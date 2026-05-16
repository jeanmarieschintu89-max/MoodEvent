package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameSize {

    PETIT("Petit", Material.LIME_CONCRETE, 17, 12, 50, 30, 13, 4),
    MOYEN("Moyen", Material.GOLD_BLOCK, 29, 24, 100, 60, 15, 6),
    GRAND("Grand", Material.ORANGE_CONCRETE, 41, 36, 200, 100, 17, 8),
    GEANT("Géant", Material.REDSTONE_BLOCK, 57, 52, 400, 150, 19, 10);

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
            case MOYEN -> 23;
            case GRAND -> 31;
            case GEANT -> 41;
        };
    }

    public int getGoldRushHeight() {
        return switch (this) {
            case PETIT -> 9;
            case MOYEN -> 11;
            case GRAND -> 13;
            case GEANT -> 15;
        };
    }

    public int getGoldRushDurationSeconds() {
        return switch (this) {
            case PETIT -> 60;
            case MOYEN -> 120;
            case GRAND -> 180;
            case GEANT -> 240;
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
