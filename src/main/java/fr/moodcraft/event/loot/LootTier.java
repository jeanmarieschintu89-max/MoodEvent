package fr.moodcraft.event.loot;

import org.bukkit.Material;

public enum LootTier {

    COMMUN("Commun", Material.CHEST, "commun"),
    RARE("Rare", Material.ENDER_CHEST, "rare"),
    EPIQUE("Épique", Material.SHULKER_BOX, "epique");

    private final String displayName;
    private final Material icon;
    private final String path;

    LootTier(String displayName, Material icon, String path) {
        this.displayName = displayName;
        this.icon = icon;
        this.path = path;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getPath() {
        return path;
    }

    public static LootTier fromCleanTitle(String title) {
        if (title == null) {
            return null;
        }
        String clean = title.toLowerCase();
        if (clean.contains("commun")) return COMMUN;
        if (clean.contains("rare")) return RARE;
        if (clean.contains("epique")) return EPIQUE;
        return null;
    }
}
