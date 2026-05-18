package fr.moodcraft.event.manager;

import org.bukkit.Material;

import java.util.Locale;

public enum WaitingRoomTheme {

    MOODCRAFT("moodcraft", "MoodCraft", Material.POLISHED_BLACKSTONE, Material.DEEPSLATE_TILES, Material.GRAY_STAINED_GLASS, Material.SOUL_LANTERN),
    ROYAL("royal", "Palais Royal", Material.QUARTZ_BLOCK, Material.GOLD_BLOCK, Material.YELLOW_STAINED_GLASS, Material.LANTERN),
    OCEAN("ocean", "Temple Océan", Material.PRISMARINE_BRICKS, Material.SEA_LANTERN, Material.LIGHT_BLUE_STAINED_GLASS, Material.SEA_LANTERN),
    NETHER("nether", "Forteresse Nether", Material.RED_NETHER_BRICKS, Material.NETHER_BRICKS, Material.RED_STAINED_GLASS, Material.SOUL_LANTERN),
    NATURE("nature", "Jardin Nature", Material.OAK_PLANKS, Material.MOSS_BLOCK, Material.GREEN_STAINED_GLASS, Material.LANTERN),
    ICE("ice", "Palais de Glace", Material.PACKED_ICE, Material.SNOW_BLOCK, Material.LIGHT_BLUE_STAINED_GLASS, Material.SEA_LANTERN),
    DESERT("desert", "Oasis Désert", Material.SMOOTH_SANDSTONE, Material.CHISELED_SANDSTONE, Material.ORANGE_STAINED_GLASS, Material.LANTERN),
    AMETHYST("amethyst", "Salon Améthyste", Material.AMETHYST_BLOCK, Material.PURPUR_BLOCK, Material.PURPLE_STAINED_GLASS, Material.END_ROD),
    COPPER("copper", "Hall Cuivré", Material.CUT_COPPER, Material.COPPER_BLOCK, Material.ORANGE_STAINED_GLASS, Material.LANTERN),
    END("end", "Observatoire de l'End", Material.END_STONE_BRICKS, Material.PURPUR_BLOCK, Material.PURPLE_STAINED_GLASS, Material.END_ROD),
    JUNGLE("jungle", "Pavillon Jungle", Material.JUNGLE_PLANKS, Material.MOSSY_COBBLESTONE, Material.LIME_STAINED_GLASS, Material.GLOWSTONE),
    DEEP_DARK("deep_dark", "Antichambre Sculk", Material.SCULK, Material.DEEPSLATE_BRICKS, Material.BLACK_STAINED_GLASS, Material.SOUL_LANTERN),
    CANDY("candy", "Salon Sucré", Material.WHITE_CONCRETE, Material.PINK_CONCRETE, Material.MAGENTA_STAINED_GLASS, Material.SHROOMLIGHT),
    REDSTONE("redstone", "Atelier Redstone", Material.SMOOTH_STONE, Material.REDSTONE_BLOCK, Material.RED_STAINED_GLASS, Material.REDSTONE_LAMP),
    EMERALD("emerald", "Salon Émeraude", Material.DARK_PRISMARINE, Material.EMERALD_BLOCK, Material.GREEN_STAINED_GLASS, Material.SEA_LANTERN),
    GOLDEN("golden", "Galerie Dorée", Material.SMOOTH_SANDSTONE, Material.GOLD_BLOCK, Material.YELLOW_STAINED_GLASS, Material.LANTERN),
    WARPED("warped", "Refuge Warped", Material.WARPED_PLANKS, Material.WARPED_WART_BLOCK, Material.CYAN_STAINED_GLASS, Material.SOUL_LANTERN),
    CRIMSON("crimson", "Refuge Crimson", Material.CRIMSON_PLANKS, Material.CRIMSON_NYLIUM, Material.RED_STAINED_GLASS, Material.SHROOMLIGHT),
    TOWNY("towny", "Maison Communale", Material.STONE_BRICKS, Material.OAK_LOG, Material.WHITE_STAINED_GLASS, Material.LANTERN),
    FESTIVAL("festival", "Dôme Festival", Material.PURPLE_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.MAGENTA_STAINED_GLASS, Material.SEA_LANTERN),
    NEON_LOUNGE("neon_lounge", "Neon Lounge", Material.POLISHED_BLACKSTONE_BRICKS, Material.CRYING_OBSIDIAN, Material.CYAN_STAINED_GLASS, Material.SEA_LANTERN),
    TRAIN_TUNNEL("train_tunnel", "Train Tunnel", Material.SMOOTH_STONE, Material.RED_CONCRETE, Material.LIGHT_BLUE_STAINED_GLASS, Material.TORCH),
    PRISON_CELL("prison_cell", "Cellule Prison", Material.STONE_BRICKS, Material.POLISHED_DEEPSLATE, Material.IRON_BARS, Material.SOUL_LANTERN);

    private final String key;
    private final String displayName;
    private final Material primary;
    private final Material accent;
    private final Material glass;
    private final Material light;

    WaitingRoomTheme(String key, String displayName, Material primary, Material accent, Material glass, Material light) {
        this.key = key;
        this.displayName = displayName;
        this.primary = primary;
        this.accent = accent;
        this.glass = glass;
        this.light = light;
    }

    public String key() { return key; }
    public String displayName() { return displayName; }
    public Material primary() { return primary; }
    public Material accent() { return accent; }
    public Material glass() { return glass; }
    public Material light() { return light; }

    public WaitingRoomTheme next() {
        WaitingRoomTheme[] themes = values();
        return themes[(ordinal() + 1) % themes.length];
    }

    public static WaitingRoomTheme of(String text) {
        if (text == null || text.isBlank()) return MOODCRAFT;
        String normalized = normalize(text);
        for (WaitingRoomTheme theme : values()) {
            if (theme.key.equals(normalized) || normalize(theme.name()).equals(normalized) || normalize(theme.displayName).equals(normalized)) {
                return theme;
            }
        }
        return MOODCRAFT;
    }

    public static String key(String text) {
        return of(text).key();
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace('é', 'e')
                .replace('è', 'e')
                .replace('ê', 'e')
                .replace('à', 'a')
                .replace('ù', 'u')
                .replace('ç', 'c')
                .replace('-', '_')
                .replace(' ', '_');
    }
}
