package fr.moodcraft.event.generator;

import fr.moodcraft.event.model.EventType;
import org.bukkit.Material;

public enum GeneratedGameType {

    SURVIE_ETAGES("Tour Infernale", Material.MAGENTA_WOOL, EventType.SURVIE_ETAGES),
    RUEE_OR("Mine en folie", Material.GOLDEN_PICKAXE, EventType.RUEE_OR),
    WATER_JUMP("Water Jump", Material.WATER_BUCKET, EventType.WATER_JUMP),
    MUR_ESCALADE("Mur d'escalade", Material.LADDER, EventType.MUR_ESCALADE),
    LABYRINTHE("Labyrinthe carré", Material.MAP, EventType.LABYRINTHE),
    LABYRINTHE_ROND("Labyrinthe rond", Material.COMPASS, EventType.LABYRINTHE),
    PRISON_BREAK("Prison Break", Material.IRON_DOOR, EventType.PRISON_BREAK);

    private final String displayName;
    private final Material icon;
    private final EventType eventType;

    GeneratedGameType(String displayName, Material icon, EventType eventType) {
        this.displayName = displayName;
        this.icon = icon;
        this.eventType = eventType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public EventType getEventType() {
        return eventType;
    }
}
