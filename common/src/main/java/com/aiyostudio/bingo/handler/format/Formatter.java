package com.aiyostudio.bingo.handler.format;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Blank038
 */
public class Formatter {
    private static final Map<String, BiFunction<Player, String, String>> FORMATTERS = new HashMap<>();
    private static final Pattern RANDOM_AMOUNT = Pattern.compile("\\{\\d+-\\d+}");

    static {
        FORMATTERS.put("default", (player, s) -> player == null ? s : s.replace("%player%", player.getName()));
        FORMATTERS.put("random_value", (player, s) -> {
            String result = s;
            Matcher matcher = RANDOM_AMOUNT.matcher(s);
            while (matcher.find()) {
                String group = matcher.group();
                String[] arr = group.substring(1, group.length() - 1).split("-");
                int min = Integer.parseInt(arr[0]), max = Integer.parseInt(arr[1]);
                int random = (int) (min + Math.random() * (max + 1 - min));
                result = result.replace(group, String.valueOf(random));
            }
            return result;
        });
    }

    public static void register(String label, BiFunction<Player, String, String> function) {
        FORMATTERS.put(label, function);
    }

    public static String format(Player player, String text) {
        if (FORMATTERS.isEmpty()) return text;
        return FORMATTERS.values().stream()
                .reduce(text, (result, function) -> function.apply(player, result), (r1, r2) -> r1);
    }
}
