package fr.moodcraft.event.model;

public enum EventType {

    SURVIE_ETAGES("§dTour Infernale", "§d▣"),
    RUEE_OR("§6Mine en folie", "§6⛏"),
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
        return false;
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
            default -> CUSTOM;
        };
    }
}
