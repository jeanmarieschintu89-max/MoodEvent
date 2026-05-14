package fr.moodcraft.event.model;

public enum EventType {

    MINI_JEU(
            "§dMini-jeu",
            "§d✦"
    ),

    ACTIVITE(
            "§bActivité",
            "§b✦"
    ),

    PVP(
            "§cPvP",
            "§c⚔"
    ),

    BUILD(
            "§6Build",
            "§6✦"
    ),

    AUTRE(
            "§eÉvénement",
            "§e✦"
    );

    private final String displayName;
    private final String icon;

    EventType(
            String displayName,
            String icon
    ) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public static EventType fromText(String text) {

        if (text == null) {
            return AUTRE;
        }

        String clean = text
                .trim()
                .toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("-", "_");

        return switch (clean) {
            case "minijeu", "mini_jeu", "mini", "game", "jeux", "jeu" -> MINI_JEU;
            case "activite", "activity", "event", "animation" -> ACTIVITE;
            case "pvp", "combat" -> PVP;
            case "build", "construction" -> BUILD;
            default -> AUTRE;
        };
    }
}
