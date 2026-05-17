package fr.moodcraft.event.util;

import org.bukkit.command.CommandSender;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MoodStyle {

    private static final ThreadLocal<Boolean> SILENT = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> GOLD_RUSH_CONTEXT = ThreadLocal.withInitial(() -> false);

    private MoodStyle() {
    }

    public static final String BRAND = "§d§lMood§5§lEvent";
    public static final String MODULE = "Mood Event";
    public static final String FRAME = "§8-----------------------------";

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
        String prepared = prepareDetail(cleanPrefix(text));
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
        if (isEndSpam(trimmed)) return "";

        String rewritten = rewriteLaunchObjective(trimmed);
        if (rewritten != null) return rewritten;
        if (isExtraLaunchRule(trimmed)) return "";

        if (trimmed.startsWith("§d➜")) {
            String prepared = prepareDetail(trimmed.substring(3).trim());
            return prepared.isBlank() ? "" : "§d➜ §f" + prepared;
        }

        if (trimmed.startsWith("§b◆")
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

    private static String prepareDetail(String text) {
        if (text == null || text.isBlank()) return "";

        String clean = stripDecorations(text);
        String lower = clean.toLowerCase(Locale.ROOT);

        if (lower.startsWith("épreuve :") || lower.startsWith("epreuve :")) {
            boolean goldRush = lower.contains("mine en folie");
            GOLD_RUSH_CONTEXT.set(goldRush);
            int coloredSplit = text.indexOf("§8•");
            if (coloredSplit >= 0) return text.substring(0, coloredSplit).trim();
            int split = clean.indexOf("•");
            if (split >= 0) return clean.substring(0, split).trim();
            return text;
        }

        if (isEmptyRewardLine(lower)) return "";

        if (Boolean.TRUE.equals(GOLD_RUSH_CONTEXT.get())) {
            if (lower.startsWith("participation :") && lower.contains("minerais")) return "Butin : §6tu gardes les minerais minés";
            if (isPodiumOrRewardLine(lower)) return "";
        }

        return text;
    }

    private static boolean isEmptyRewardLine(String lower) {
        if (lower == null) return true;
        return lower.equals("top 1 :")
                || lower.equals("top 2 :")
                || lower.equals("top 3 :")
                || lower.equals("participation :")
                || lower.equals("récompense possible :")
                || lower.equals("recompense possible :")
                || lower.contains("à configurer par le staff")
                || lower.contains("a configurer par le staff");
    }

    private static boolean isPodiumOrRewardLine(String lower) {
        return lower.startsWith("top 1 :")
                || lower.startsWith("top 2 :")
                || lower.startsWith("top 3 :")
                || lower.startsWith("récompense possible :")
                || lower.startsWith("recompense possible :");
    }

    private static String rewriteOldNames(String line) {
        if (line == null) return "";
        return line
                .replace("Parcours Jump", "Mur d'escalade")
                .replace("parcours Jump", "Mur d'escalade")
                .replace("Survie des étages", "Tour Infernale")
                .replace("survie des étages", "Tour Infernale")
                .replace("Effondrement", "Tour Infernale")
                .replace("effondrement", "Tour Infernale")
                .replace("Ruée vers l'or", "Mine en folie")
                .replace("Ruée vers l’or", "Mine en folie")
                .replace("ruée vers l'or", "Mine en folie")
                .replace("ruée vers l’or", "Mine en folie");
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

        if (clean.equals("objectif : atteignez la ligne rouge avant les autres.")) return hype("Objectif : fonce jusqu'à la zone rouge avant les autres.");
        if (clean.equals("objectif : sautez de laine en laine jusqu'à l'arrivée.")) return hype("Objectif : gravis le Mur d'escalade jusqu'à la plateforme rouge.");
        if (clean.equals("objectif : trouvez la sortie avant les autres.")) return hype("Objectif : trouve la sortie, vite et proprement.");
        if (clean.equals("objectif : franchissez les blocs de laine au-dessus de l'eau.")) return hype("Objectif : traverse au-dessus de l'eau sans plonger.");
        if (clean.equals("objectif : restez le plus longtemps possible.")) return hype("Objectif : survis à la Tour Infernale, reste debout.");

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

    private static boolean isEndSpam(String line) {
        String clean = stripDecorations(line).toLowerCase(Locale.ROOT);
        return clean.startsWith("événement créé")
                || clean.startsWith("description mise à jour")
                || clean.startsWith("type d'événement défini")
                || clean.startsWith("point de départ défini")
                || clean.startsWith("point d'arrivée défini")
                || clean.startsWith("nom :")
                || clean.startsWith("type :")
                || clean.startsWith("départ :")
                || clean.startsWith("les joueurs commenceront ici")
                || clean.startsWith("course, jump, water jump")
                || clean.startsWith("participants renvoyés")
                || clean.equals("récompenses distribuées.")
                || clean.contains("récompense de participation")
                || clean.contains("récompense top 3")
                || clean.contains("récompense de classement")
                || clean.contains("vous recevez la récompense")
                || clean.startsWith("salle d'attente restaurée")
                || clean.startsWith("structure générée restaurée")
                || clean.startsWith("aucune salle à restaurer")
                || clean.startsWith("aucune structure générée")
                || clean.startsWith("aucun pack spécial")
                || clean.contains("restauré automatiquement")
                || clean.startsWith("votre place")
                || clean.startsWith("place :")
                || clean.startsWith("vous êtes dans le top 3")
                || clean.startsWith("classement final confirmé");
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
