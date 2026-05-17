package fr.moodcraft.event.util;

import org.bukkit.command.CommandSender;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MoodStyle {

    private static final ThreadLocal<Boolean> SILENT = ThreadLocal.withInitial(() -> false);

    private MoodStyle() {
    }

    public static final String BRAND = "§d§lMood§5§lEvent";
    public static final String MODULE = "Mood Event";
    public static final String FRAME = "§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    public static void silence(Runnable action) {
        if (action == null) return;
        boolean previous = Boolean.TRUE.equals(SILENT.get());
        SILENT.set(true);
        try {
            action.run();
        } finally {
            SILENT.set(previous);
        }
    }

    public static String guiTitle(String title) {
        return "§d✦ §8§l" + cleanPrefix(title) + " §d✦";
    }

    public static String cleanTitle(String title) {
        if (title == null) return "";

        String clean = title
                .replaceAll("§.", "")
                .replace("✦", "")
                .trim();

        clean = Normalizer.normalize(clean, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("'", "")
                .replace("’", "")
                .replace("`", "");

        return clean.toLowerCase(Locale.ROOT).trim();
    }

    public static String header(String module) {
        String cleanModule = cleanPrefix(module);
        if (cleanModule.isBlank()) cleanModule = MODULE;
        return "§8━━━━━━ §d§l✦ " + cleanModule + " ✦ §8━━━━━━";
    }

    public static String info(String text) {
        return "§bℹ §f" + cleanPrefix(text);
    }

    public static String success(String text) {
        return "§a✔ §f" + cleanPrefix(text);
    }

    public static String error(String text) {
        return "§c✖ §f" + cleanPrefix(text);
    }

    public static String detail(String text) {
        String prepared = cleanPrefix(text);
        if (prepared.isBlank()) return "";
        return "§d➜ §f" + prepared;
    }

    public static String hype(String text) {
        return "§e★ §f" + cleanPrefix(text);
    }

    public static void send(CommandSender sender, String module, String... lines) {
        if (sender == null || Boolean.TRUE.equals(SILENT.get())) return;

        List<String> normalizedLines = new ArrayList<>();
        if (lines != null) {
            for (String line : lines) {
                String normalized = normalize(line);
                if (!normalized.isBlank()) normalizedLines.add(normalized);
            }
        }
        if (normalizedLines.isEmpty()) return;

        sender.sendMessage("");
        sender.sendMessage(header(module));
        for (String line : normalizedLines) sender.sendMessage(line);
        sender.sendMessage(FRAME);
    }

    public static void infoMessage(CommandSender sender, String module, String message, String... details) {
        send(sender, module, concat(info(message), details));
    }

    public static void successMessage(CommandSender sender, String module, String message, String... details) {
        send(sender, module, concat(success(message), details));
    }

    public static void errorMessage(CommandSender sender, String module, String message, String... details) {
        send(sender, module, concat(error(message), details));
    }

    private static String[] concat(String first, String... rest) {
        int size = 1 + (rest == null ? 0 : rest.length);
        String[] result = new String[size];
        result[0] = first;
        if (rest != null) System.arraycopy(rest, 0, result, 1, rest.length);
        return result;
    }

    private static String normalize(String line) {
        if (line == null || line.isBlank()) return "";

        String trimmed = rewriteOldNames(line.trim().replace("§c✘", "§c✖"));
        if (trimmed.isBlank()) return "";

        if (trimmed.startsWith("§d➜") || trimmed.startsWith("§8•") || trimmed.startsWith("§e➜")) return detail(trimmed);
        if (trimmed.startsWith("§b◆") || trimmed.startsWith("§bℹ")) return info(trimmed);
        if (trimmed.startsWith("§a▶") || trimmed.startsWith("§a✔")) return success(trimmed);
        if (trimmed.startsWith("§c■") || trimmed.startsWith("§c✖")) return error(trimmed);
        if (trimmed.startsWith("§e★")) return hype(trimmed);
        if (trimmed.startsWith("§8-----") || trimmed.startsWith("§8━━━━") || trimmed.startsWith("§8----------------")) return trimmed;

        if (trimmed.startsWith("§a")) return success(trimmed);
        if (trimmed.startsWith("§c")) return error(trimmed);
        if (trimmed.startsWith("§7") || trimmed.startsWith("§8")) return detail(trimmed);
        if (stripDecorations(trimmed).toLowerCase(Locale.ROOT).startsWith("objectif")) return hype(trimmed);

        return info(trimmed);
    }

    private static String rewriteOldNames(String line) {
        if (line == null) return "";
        return line
                .replace("Parcours Jump", "Water Jump")
                .replace("parcours Jump", "Water Jump")
                .replace("Survie des étages", "Tour Infernale")
                .replace("survie des étages", "Tour Infernale")
                .replace("Effondrement", "Tour Infernale")
                .replace("effondrement", "Tour Infernale")
                .replace("RUÉE VERS L'OR", "MINE EN FOLIE")
                .replace("Ruée vers l'or", "Mine en folie")
                .replace("Ruée vers l’or", "Mine en folie")
                .replace("ruée vers l'or", "Mine en folie")
                .replace("ruée vers l’or", "Mine en folie")
                .replace("Pioche Ruée vers l'or", "Pioche Mine en folie")
                .replace("Pioche Ruée vers l’or", "Pioche Mine en folie");
    }

    private static String stripDecorations(String text) {
        if (text == null) return "";
        return text
                .replaceAll("§.", "")
                .replace("➜", "")
                .replace("✔", "")
                .replace("✘", "")
                .replace("✖", "")
                .replace("•", "")
                .replace("▶", "")
                .replace("◆", "")
                .replace("■", "")
                .replace("★", "")
                .replace("ℹ", "")
                .trim();
    }

    private static String cleanPrefix(String text) {
        if (text == null) return "";

        String clean = rewriteOldNames(text).trim();
        boolean changed;
        do {
            String before = clean;
            clean = clean
                    .replaceFirst("^§[0-9a-fk-or]", "")
                    .replaceFirst("^➜\\s*", "")
                    .replaceFirst("^✔\\s*", "")
                    .replaceFirst("^✘\\s*", "")
                    .replaceFirst("^✖\\s*", "")
                    .replaceFirst("^•\\s*", "")
                    .replaceFirst("^▶\\s*", "")
                    .replaceFirst("^◆\\s*", "")
                    .replaceFirst("^■\\s*", "")
                    .replaceFirst("^★\\s*", "")
                    .replaceFirst("^ℹ\\s*", "")
                    .trim();
            changed = !before.equals(clean);
        } while (changed);

        return clean;
    }
}
