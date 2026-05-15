package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameSize {

    PETIT("Petit", Material.LIME_CONCRETE, 17, 12, 50, 30, 15, 3),
    MOYEN("Moyen", Material.GOLD_BLOCK, 29, 24, 100, 60, 25, 4),
    GRAND("Grand", Material.ORANGE_CONCRETE, 41, 36, 200, 100, 37, 5),
    GEANT("Géant", Material.REDSTONE_BLOCK, 57, 52, 400, 150, 51, 7);

    private final String displayName;
    private final Material icon;
    private final int mazeWidth;
    private final int jumpPlatforms;
    private final int raceLength;
    private final int waterLength;
    private final int survivalWidth;
    private final int survivalFloors;

    GeneratedGameSize(
            String displayName,
            Material icon,
            int mazeWidth,
            int jumpPlatforms,
            int raceLength,
            int waterLength,
            int survivalWidth,
            int survivalFloors
    ) {
        this.displayName = displayName;
        this.icon = icon;
        this.mazeWidth = mazeWidth;
        this.jumpPlatforms = jumpPlatforms;
        this.raceLength = raceLength;
        this.waterLength = waterLength;
        this.survivalWidth = survivalWidth;
        this.survivalFloors = survivalFloors;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getMazeWidth() {
        return mazeWidth;
    }

    public int getJumpPlatforms() {
        return jumpPlatforms;
    }

    public int getRaceLength() {
        return raceLength;
    }

    public int getWaterLength() {
        return waterLength;
    }

    public int getSurvivalWidth() {
        return survivalWidth;
    }

    public int getSurvivalFloors() {
        return survivalFloors;
    }

    public String describeFor(GeneratedGameType type) {
        if (type == null) {
            return displayName;
        }
        return switch (type) {
            case LABYRINTHE -> mazeWidth + "x" + mazeWidth;
            case JUMP -> jumpPlatforms + " plateformes";
            case COURSE -> raceLength + " blocs";
            case WATER_JUMP -> waterLength + " blocs";
            case SURVIE_ETAGES -> survivalFloors + " étages";
        };
    }
}
