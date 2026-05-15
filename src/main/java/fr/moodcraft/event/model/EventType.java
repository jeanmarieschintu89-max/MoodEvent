package fr.moodcraft.event.model;

public enum EventType {

    COURSE(
            "§aCourse",
            "§a➜"
    ),

    JUMP(
            "§eParcours Jump",
            "§e⬆"
    ),

    LABYRINTHE(
            "§5Labyrinthe",
            "§5✦"
    ),

    SPLEEF(
            "§bSpleef",
            "§b❄"
    ),

    KOTH(
            "§6Roi de la Colline",
            "§6♛"
    ),

    PVP(
            "§cCombat PvP",
            "§c⚔"
    ),

    QUIZ(
            "§dQuiz",
            "§d?"
    ),

    CHASSE_TRESOR(
            "§eChasse au trésor",
            "§e✦"
    ),

    BUILD(
            "§6Concours de build",
            "§6▣"
    ),

    CUSTOM(
            "§fÉvénement libre",
            "§f✦"
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

    public boolean usesFinishLine() {
        return this == COURSE || this == JUMP || this == LABYRINTHE;
    }

    public static EventType fromText(String text) {

        if (text == null) {
            return CUSTOM;
        }

        String clean = text
                .trim()
                .toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ç", "c")
                .replace("'", "")
                .replace("-", "_")
                .replace(" ", "_");

        return switch (clean) {
            case "course", "race", "running" -> COURSE;
            case "jump", "parcours", "parkour", "parcours_jump", "saut" -> JUMP;
            case "labyrinthe", "maze", "labyrinth" -> LABYRINTHE;
            case "spleef" -> SPLEEF;
            case "koth", "roi_colline", "roi_de_la_colline", "king_of_the_hill" -> KOTH;
            case "pvp", "combat", "duel" -> PVP;
            case "quiz", "question", "questions" -> QUIZ;
            case "chasse_tresor", "chasse_au_tresor", "tresor", "treasure" -> CHASSE_TRESOR;
            case "build", "construction", "concours_build" -> BUILD;
            case "minijeu", "mini_jeu", "mini", "game", "jeu", "jeux", "activite", "activity", "event", "animation", "autre", "custom", "libre" -> CUSTOM;
            default -> CUSTOM;
        };
    }
}
