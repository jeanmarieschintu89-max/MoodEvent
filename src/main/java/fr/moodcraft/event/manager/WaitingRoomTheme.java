package fr.moodcraft.event.manager;

import org.bukkit.Material;

import java.util.Locale;

public enum WaitingRoomTheme {

    CLASSIQUE("classique", "Classique Premium", Material.SMOOTH_QUARTZ, Material.QUARTZ_BLOCK, Material.WHITE_STAINED_GLASS),
    ROYAL("royal", "Royal Doré", Material.PURPUR_BLOCK, Material.GOLD_BLOCK, Material.PURPLE_STAINED_GLASS),
    NATURE("nature", "Forêt Ancienne", Material.MOSS_BLOCK, Material.OAK_LOG, Material.GREEN_STAINED_GLASS),
    NEIGE("neige", "Neige Polaire", Material.SNOW_BLOCK, Material.PACKED_ICE, Material.LIGHT_BLUE_STAINED_GLASS),
    SOMBRE("sombre", "Sombre Blackstone", Material.POLISHED_BLACKSTONE, Material.DEEPSLATE_TILES, Material.TINTED_GLASS),
    FESTIVAL("festival", "Festival Coloré", Material.YELLOW_CONCRETE, Material.MAGENTA_WOOL, Material.PINK_STAINED_GLASS),
    NEON("neon", "Néon Arcade", Material.BLACK_CONCRETE, Material.CYAN_CONCRETE, Material.MAGENTA_STAINED_GLASS),
    CYBERPUNK("cyberpunk", "Cyberpunk", Material.GRAY_CONCRETE, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS),
    TEMPLE("temple", "Temple Ancien", Material.SANDSTONE, Material.CHISELED_SANDSTONE, Material.YELLOW_STAINED_GLASS),
    SAKURA("sakura", "Japon Sakura", Material.CHERRY_PLANKS, Material.CHERRY_LOG, Material.PINK_STAINED_GLASS),
    MEDIEVAL("medieval", "Forteresse Médiévale", Material.STONE_BRICKS, Material.OAK_LOG, Material.BROWN_STAINED_GLASS),
    ATLANTIS("atlantis", "Atlantis Océan", Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, Material.CYAN_STAINED_GLASS),
    DESERT("desert", "Désert Oasis", Material.SMOOTH_SANDSTONE, Material.TERRACOTTA, Material.LIME_STAINED_GLASS),
    VOLCAN("volcan", "Volcan", Material.BLACKSTONE, Material.MAGMA_BLOCK, Material.ORANGE_STAINED_GLASS),
    CRISTAL("cristal", "Cristal Améthyste", Material.AMETHYST_BLOCK, Material.CALCITE, Material.PURPLE_STAINED_GLASS),
    STEAMPUNK("steampunk", "Steampunk Cuivre", Material.CUT_COPPER, Material.COPPER_BLOCK, Material.ORANGE_STAINED_GLASS),
    ENDER("ender", "Ender Astral", Material.END_STONE_BRICKS, Material.PURPUR_BLOCK, Material.PURPLE_STAINED_GLASS),
    NETHER("nether", "Nether Bastion", Material.POLISHED_BLACKSTONE_BRICKS, Material.GILDED_BLACKSTONE, Material.RED_STAINED_GLASS),
    JUNGLE("jungle", "Ruines Jungle", Material.MOSSY_COBBLESTONE, Material.JUNGLE_LOG, Material.LIME_STAINED_GLASS),
    LABORATOIRE("laboratoire", "Laboratoire Futuriste", Material.WHITE_CONCRETE, Material.IRON_BLOCK, Material.LIGHT_BLUE_STAINED_GLASS);

    private final String key;
    private final String displayName;
    private final Material primary;
    private final Material accent;
    private final Material glass;

    WaitingRoomTheme(String key, String displayName, Material primary, Material accent, Material glass) {
        this.key = key;
        this.displayName = displayName;
        this.primary = primary;
        this.accent = accent;
        this.glass = glass;
    }

    public String key() { return key; }
    public String displayName() { return displayName; }
    public Material primary() { return primary; }
    public Material accent() { return accent; }
    public Material glass() { return glass; }

    public Material light() {
        return this == SOMBRE || this == NETHER ? Material.SOUL_LANTERN : this == ENDER ? Material.END_ROD : Material.SEA_LANTERN;
    }

    public static WaitingRoomTheme of(String text) {
        String key = key(text);
        for (WaitingRoomTheme theme : values()) if (theme.key.equals(key)) return theme;
        return SOMBRE;
    }

    public static String key(String text) {
        if (text == null) return "sombre";
        String clean = text.toLowerCase(Locale.ROOT)
                .replace("é", "e").replace("è", "e").replace("ê", "e")
                .replace("à", "a").replace("ç", "c")
                .replace(" ", "").replace("_", "").replace("-", "");
        return switch (clean) {
            case "classique", "premium", "classic", "lumineux", "clair", "light" -> "classique";
            case "joyeux", "color", "couleur", "colore", "festival" -> "festival";
            case "royal", "prestige", "dore", "gold" -> "royal";
            case "nature", "foret", "forest" -> "nature";
            case "neige", "glace", "snow", "polaire" -> "neige";
            case "neon", "neonarcade", "arcade" -> "neon";
            case "cyberpunk", "cyber" -> "cyberpunk";
            case "temple", "templeancien" -> "temple";
            case "sakura", "japon", "japonais", "cherry" -> "sakura";
            case "medieval", "forteresse", "castle", "chateau" -> "medieval";
            case "atlantis", "ocean", "oceanique", "mer" -> "atlantis";
            case "desert", "oasis", "desertoasis" -> "desert";
            case "volcan", "volcanique" -> "volcan";
            case "cristal", "crystal", "amethyste" -> "cristal";
            case "steampunk", "cuivre", "copper" -> "steampunk";
            case "ender", "astral", "enderastral" -> "ender";
            case "nether", "bastion", "netherbastion" -> "nether";
            case "jungle", "ruines", "ruinesjungle" -> "jungle";
            case "laboratoire", "labo", "futuriste", "lab" -> "laboratoire";
            default -> "sombre";
        };
    }
}
