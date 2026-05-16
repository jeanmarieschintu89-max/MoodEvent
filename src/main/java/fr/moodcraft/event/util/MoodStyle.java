package fr.moodcraft.event.util;

import org.bukkit.command.CommandSender;

import java.text.Normalizer;
import java.util.Locale;

public final class MoodStyle {

    private MoodStyle() {
    }

    public static final String BRAND = "§d§lMood§5§lEvent";
    public static final String MODULE = "Mood Event";
    public static final String FRAME = "§8-----------------------------";

    public static String guiTitle(String title) {
        return "§d✦ §8§l" + title + " §d✦";
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
        return "§8----- §d§l✦ " + cleanPrefix(module).replace("Événement ", "") + " ✦ §8-----";
    }

    public static String info(String text) {
        return "§b◆ §f" + cleanPrefix(text);
    }

    public static String success(String text) {
        return "§a▶ §f" + cleanPrefix(text);
    }

    public static String error(String text) {
        return "§c■ §f" + cleanPrefix(text);
    }

    public static String detail(String text) {
        return "§d➜ §f" + cleanPrefix(text);
    }

    public static String hype(String text) {
        return "§e★ §f" + cleanPrefix(text);
    }

    public static void send(CommandSender sender, String module, String... lines) {
        if (sender == null) return;

        sender.sendMessage("");
        sender.sendMessage(header(module));

        if (lines != null) {
            for (String line : lines) {
                String normalized = normalize(line);
                if (!normalized.isBlank()) sender.sendMessage(normalized);
            }
        }

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

        String trimmed = line.trim().replace("§c✘", "§c✖");
        String rewritten = rewriteLaunchObjective(trimmed);
        if (rewritten != null) return rewritten;
        if (isExtraLaunchRule(trimmed)) return "";

        if (trimmed.startsWith("§d➜")
                || trimmed.startsWith("§b◆")
                || trimmed.startsWith("§a▶")
                || trimmed.startsWith("§c■")
                || trimmed.startsWith("§e★")
                || trimmed.startsWith("§8-----")
                || trimmed.startsWith("§8----------------")) {
            return trimmed;
        }

        if (trimmed.startsWith("§8•")
                || trimmed.startsWith("§e➜")
                || trimmed.startsWith("§a✔")
                || trimmed.startsWith("§c✖")) {
            return rewriteOldMoodLine(trimmed);
        }

        if (trimmed.startsWith("§a")) return success(trimmed);
        if (trimmed.startsWith("§c")) return error(trimmed);
        if (trimmed.startsWith("§7") || trimmed.startsWith("§8")) return detail(trimmed);
        if (trimmed.toLowerCase(Locale.ROOT).contains("objectif")) return hype(trimmed);

        return info(trimmed);
    }

    private static String rewriteOldMoodLine(String line) {
        String clean = stripDecorations(line);
        if (line.startsWith("§a✔")) return success(clean);
        if (line.startsWith("§c✖")) return error(clean);
        if (line.startsWith("§8•")) return detail(clean);
        return info(clean);
    }

    private static String rewriteLaunchObjective(String line) {
        String clean = stripDecorations(line).toLowerCase(Locale.ROOT);

        if (clean.equals("objectif : atteignez la ligne rouge avant les autres.")) {
            return hype("Objectif : fonce jusqu'à la zone rouge avant les autres.");
        }

        if (clean.equals("objectif : sautez de laine en laine jusqu'à l'arrivée.")) {
            return hype("Objectif : termine le parcours sans tomber.");
        }

        if (clean.equals("objectif : trouvez la sortie avant les autres.")) {
            return hype("Objectif : trouve la sortie, vite et proprement.");
        }

        if (clean.equals("objectif : franchissez les blocs de laine au-dessus de l'eau.")) {
            return hype("Objectif : traverse au-dessus de l'eau sans plonger.");
        }

        if (clean.equals("objectif : restez le plus longtemps possible.")) {
            return hype("Objectif : reste debout, deviens le dernier survivant.");
        }

        return null;
    }

    private static boolean isExtraLaunchRule(String line) {
        String clean = stripDecorations(line).toLowerCase(Locale.ROOT);
        return clean.equals("les 3 premiers seront récompensés.")
                || clean.equals("ender pearl interdite.")
                || clean.equals("les étages disparaissent progressivement.")
                || clean.equals("le premier en bas est perdant.")
                || clean.equals("les derniers survivants sont récompensés.");
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
                .trim();
    }

    private static String cleanPrefix(String text) {
        if (text == null) return "";

        return text
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
                .trim();
    }
}