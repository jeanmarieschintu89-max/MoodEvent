package fr.moodcraft.event.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class EventItem {

    private EventItem() {
    }

    public static ItemStack item(
            Material material,
            String name,
            String... lore
    ) {

        ItemStack item = new ItemStack(material == null ? Material.BARRIER : material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(name == null ? " " : name);
        meta.setLore(normalizeLore(lore));
        hide(meta);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack glow(ItemStack item) {

        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        hide(meta);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> normalizeLore(String... lore) {

        List<String> lines = new ArrayList<>();

        if (lore == null) {
            return lines;
        }

        for (String line : lore) {
            lines.add(normalizeLine(line));
        }

        return lines;
    }

    private static String normalizeLine(String line) {

        if (line == null || line.isBlank()) {
            return "";
        }

        String trimmed = line.trim().replace("§c✘", "§c✖");

        if (trimmed.startsWith("§8•")
                || trimmed.startsWith("§e➜")
                || trimmed.startsWith("§a✔")
                || trimmed.startsWith("§c✖")) {
            return trimmed;
        }

        if (trimmed.startsWith("§a")) {
            return MoodStyle.success(trimmed);
        }

        if (trimmed.startsWith("§c")) {
            return MoodStyle.error(trimmed);
        }

        if (trimmed.startsWith("§e")) {
            return MoodStyle.info(trimmed);
        }

        if (trimmed.startsWith("§7") || trimmed.startsWith("§8")) {
            return MoodStyle.detail(trimmed);
        }

        return MoodStyle.detail(trimmed);
    }

    private static void hide(ItemMeta meta) {
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_PLACED_ON,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP
        );

        try {
            meta.addItemFlags(ItemFlag.valueOf("HIDE_ITEM_SPECIFICS"));
        } catch (IllegalArgumentException ignored) {
        }
    }
}
