package fr.moodcraft.event.util;

import org.bukkit.command.CommandSender;

import java.text.Normalizer;
import java.util.Locale;

public final class MoodStyle {

    private MoodStyle() {
    }

    public static final String BRAND = "§aMood§6Craft";
    public static final String MODULE = "Événement " + BRAND;
    public static final String FRAME = "§8-----------------------------";

    public static String guiTitle(String title) {
        return "§6✦ §8§l" + title + " §6✦";
    }

    public static String cleanTitle(String title) {

        if (title == null) {
            return "";
        }

        String clean = title
                .replaceAll("§.", "")
                .replace("✦", "")
                .trim();

        clean = Normalizer.normalize(clean, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return clean.toLowerCase(Locale.ROOT).trim();
    }

    public static String header(String module) {
        return "§8----- §6✦ " + cleanPrefix(module) + " ✦ §8-----";
    }

    public static String info(String text) {
        return "§e➜ §f" + cleanPrefix(text);
    }

    public static String success(String text) {
        return "§a✔ §f" + cleanPrefix(text);
    }

    public static String error(String text) {
        return "§c✖ §f" + cleanPrefix(text);
    }

    public static String detail(String text) {
        return "§8• §7" + cleanPrefix(text);
    }

    public static void send(
            CommandSender sender,
            String module,
            String... lines
    ) {

        if (sender == null) {
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(header(module));

        if (lines != null) {
            for (String line : lines) {
                sender.sendMessage(normalize(line));
            }
        }

        sender.sendMessage(FRAME);
    }

    public static void infoMessage(
            CommandSender sender,
            String module,
            String message,
            String... details
    ) {
        send(sender, module, concat(info(message), details));
    }

    public static void successMessage(
            CommandSender sender,
            String module,
            String message,
            String... details
    ) {
        send(sender, module, concat(success(message), details));
    }

    public static void errorMessage(
            CommandSender sender,
            String module,
            String message,
            String... details
    ) {
        send(sender, module, concat(error(message), details));
    }

    private static String[] concat(String first, String... rest) {
        int size = 1 + (rest == null ? 0 : rest.length);
        String[] result = new String[size];
        result[0] = first;

        if (rest != null) {
            System.arraycopy(rest, 0, result, 1, rest.length);
        }

        return result;
    }

    private static String normalize(String line) {

        if (line == null || line.isBlank()) {
            return "";
        }

        String trimmed = line.trim().replace("§c✘", "§c✖");

        if (trimmed.startsWith("§8•")
                || trimmed.startsWith("§e➜")
                || trimmed.startsWith("§a✔")
                || trimmed.startsWith("§c✖")
                || trimmed.startsWith("§8-----")
                || trimmed.startsWith("§8----------------")) {
            return trimmed;
        }

        if (trimmed.startsWith("§a")) {
            return success(trimmed);
        }

        if (trimmed.startsWith("§c")) {
            return error(trimmed);
        }

        if (trimmed.startsWith("§7") || trimmed.startsWith("§8")) {
            return detail(trimmed);
        }

        return info(trimmed);
    }

    private static String cleanPrefix(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceFirst("^§[0-9a-fk-or]", "")
                .replaceFirst("^➜\\s*", "")
                .replaceFirst("^✔\\s*", "")
                .replaceFirst("^✘\\s*", "")
                .replaceFirst("^✖\\s*", "")
                .replaceFirst("^•\\s*", "")
                .trim();
    }
}
