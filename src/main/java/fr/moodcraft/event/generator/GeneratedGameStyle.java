package fr.moodcraft.event.generator;

import org.bukkit.Material;

public enum GeneratedGameStyle {

    CLASSIQUE("Classique Premium", Material.STONE_BRICKS, Material.SMOOTH_STONE, Material.SEA_LANTERN, Material.LIGHT_GRAY_STAINED_GLASS, "classique"),
    ROYAL("Royal Doré", Material.PURPUR_BLOCK, Material.GOLD_BLOCK, Material.SHROOMLIGHT, Material.PURPLE_STAINED_GLASS, "royal"),
    NATURE("Forêt Ancienne", Material.MOSS_BLOCK, Material.OAK_LOG, Material.GLOWSTONE, Material.GREEN_STAINED_GLASS, "nature"),
    NEIGE("Neige Polaire", Material.SNOW_BLOCK, Material.PACKED_ICE, Material.SEA_LANTERN, Material.LIGHT_BLUE_STAINED_GLASS, "neige"),
    SOMBRE("Sombre Blackstone", Material.POLISHED_BLACKSTONE, Material.DEEPSLATE_TILES, Material.SOUL_LANTERN, Material.GRAY_STAINED_GLASS, "sombre"),
    FESTIVAL("Festival Coloré", Material.YELLOW_CONCRETE, Material.MAGENTA_WOOL, Material.GLOWSTONE, Material.PINK_STAINED_GLASS, "festival"),
    NEON_ARCADE("Néon Arcade", Material.BLACK_CONCRETE, Material.CYAN_CONCRETE, Material.SEA_LANTERN, Material.MAGENTA_STAINED_GLASS, "neon"),
    CYBERPUNK("Cyberpunk", Material.GRAY_CONCRETE, Material.YELLOW_CONCRETE, Material.REDSTONE_LAMP, Material.YELLOW_STAINED_GLASS, "cyberpunk"),
    TEMPLE_ANCIEN("Temple Ancien", Material.SANDSTONE, Material.CHISELED_SANDSTONE, Material.LANTERN, Material.YELLOW_STAINED_GLASS, "temple"),
    SAKURA("Japon Sakura", Material.CHERRY_PLANKS, Material.CHERRY_LOG, Material.LANTERN, Material.PINK_STAINED_GLASS, "sakura"),
    MEDIEVAL("Forteresse Médiévale", Material.STONE_BRICKS, Material.OAK_LOG, Material.LANTERN, Material.BROWN_STAINED_GLASS, "medieval"),
    ATLANTIS("Atlantis Océan", Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, Material.SEA_LANTERN, Material.CYAN_STAINED_GLASS, "atlantis"),
    DESERT_OASIS("Désert Oasis", Material.SMOOTH_SANDSTONE, Material.TERRACOTTA, Material.GLOWSTONE, Material.LIME_STAINED_GLASS, "desert"),
    VOLCAN("Volcan Infernal", Material.BLACKSTONE, Material.MAGMA_BLOCK, Material.SHROOMLIGHT, Material.ORANGE_STAINED_GLASS, "volcan"),
    CRISTAL("Cristal Améthyste", Material.AMETHYST_BLOCK, Material.CALCITE, Material.SEA_LANTERN, Material.PURPLE_STAINED_GLASS, "cristal"),
    STEAMPUNK("Steampunk Cuivre", Material.CUT_COPPER, Material.COPPER_BLOCK, Material.LANTERN, Material.ORANGE_STAINED_GLASS, "steampunk"),
    ENDER_ASTRAL("Ender Astral", Material.END_STONE_BRICKS, Material.PURPUR_BLOCK, Material.END_ROD, Material.PURPLE_STAINED_GLASS, "ender"),
    NETHER_BASTION("Nether Bastion", Material.POLISHED_BLACKSTONE_BRICKS, Material.GILDED_BLACKSTONE, Material.SOUL_LANTERN, Material.RED_STAINED_GLASS, "nether"),
    JUNGLE_RUINES("Ruines Jungle", Material.MOSSY_COBBLESTONE, Material.JUNGLE_LOG, Material.GLOWSTONE, Material.LIME_STAINED_GLASS, "jungle"),
    LABORATOIRE("Laboratoire Futuriste", Material.WHITE_CONCRETE, Material.IRON_BLOCK, Material.SEA_LANTERN, Material.LIGHT_BLUE_STAINED_GLASS, "laboratoire");

    private final String displayName;
    private final Material primary;
    private final Material accent;
    private final Material light;
    private final Material glass;
    private final String waitingRoomStyle;

    GeneratedGameStyle(String displayName, Material primary, Material accent, Material light, Material glass, String waitingRoomStyle) {
        this.displayName = displayName;
        this.primary = primary;
        this.accent = accent;
        this.light = light;
        this.glass = glass;
        this.waitingRoomStyle = waitingRoomStyle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getPrimary() {
        return primary;
    }

    public Material getAccent() {
        return accent;
    }

    public Material getLight() {
        return light;
    }

    public Material getGlass() {
        return glass;
    }

    public String getWaitingRoomStyle() {
        return waitingRoomStyle;
    }

    public GeneratedGameStyle next() {
        GeneratedGameStyle[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
