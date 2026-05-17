package fr.moodcraft.event.model;

public enum EventType {

    SURVIE_ETAGES("§dTour Infernale", "§d▣"),
    RUEE_OR("§6Mine en folie", "§6⛏"),
    WATER_JUMP("§bWater Jump", "§b≈"),
    MUR_ESCALADE("§eMur d'escalade", "§e▲"),
    LABYRINTHE("§aLabyrinthe", "§a⌘"),
    PRISON_BREAK("§8Prison Break", "§8▦"),
    CUSTOM("§fÉvénement libre", "§f✦");

    private final String displayName;
    private final String icon;

    EventType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public boolean usesFinishLine() {
        return this == WATER_JUMP || this == LABYRINTHE || this == MUR_ESCALADE || this == PRISON_BREAK;
    }

    public boolean usesSurvivalRanking() {
        return this == SURVIE_ETAGES;
    }

    public boolean usesTimedMining() {
        return this == RUEE_OR;
    }

    public static EventType fromText(String text) {
        if (text == null) return CUSTOM;
        String clean = text.trim().toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ç", "c")
                .replace("'", "")
                .replace("-", "_")
                .replace(" ", "_");

        return switch (clean) {
            case "tour_infernale", "effondrement", "survie_etages", "survie_des_etages", "etages", "etage", "floor", "floors", "floor_survival" -> SURVIE_ETAGES;
            case "mine_en_folie", "mine_folie", "ruee_or", "ruee_vers_lor", "ruee_vers_or", "or", "gold", "gold_rush", "mine", "minage", "mining" -> RUEE_OR;
            case "waterjump", "water_jump", "water", "eau", "jump_eau", "saut_eau", "water_jumps" -> WATER_JUMP;
            case "mur_escalade", "mur_d_escalade", "escalade", "climb", "climbing", "jump", "jump_vertical", "vertical_jump", "parkour" -> MUR_ESCALADE;
            case "labyrinthe", "laby", "maze", "mazes", "labyrinth", "dedale", "dédale" -> LABYRINTHE;
            case "prison_break", "prison", "prisonbreak", "evasion", "escape_prison", "jailbreak", "jail_break" -> PRISON_BREAK;
            default -> CUSTOM;
        };
    }
}
